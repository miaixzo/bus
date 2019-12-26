/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.image;

import org.aoju.bus.core.io.resource.Resource;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.FileType;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.*;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.*;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Path;

/**
 * 图像编辑器
 *
 * @author Kimi Liu
 * @version 5.3.8
 * @since JDK 1.8+
 */
public class Image implements Serializable {

    private static final long serialVersionUID = 1L;

    private BufferedImage srcImage;
    private java.awt.Image targetImage;
    /**
     * 目标图片文件格式,用于写出
     */
    private String targetImageType = FileType.TYPE_JPG;
    /**
     * 计算x,y坐标的时候是否从中心做为原始坐标开始计算
     */
    private boolean positionBaseCentre = true;
    /**
     * 图片输出质量,用于压缩
     */
    private float quality = -1;

    /**
     * 构造
     *
     * @param srcImage 来源图片
     */
    public Image(BufferedImage srcImage) {
        this.srcImage = srcImage;
    }

    /**
     * 从Path读取图片并开始处理
     *
     * @param imagePath 图片文件路径
     * @return {@link Image}
     */
    public static Image from(Path imagePath) {
        return from(imagePath.toFile());
    }

    /**
     * 从文件读取图片并开始处理
     *
     * @param imageFile 图片文件
     * @return {@link Image}
     */
    public static Image from(File imageFile) {
        return new Image(ImageUtils.read(imageFile));
    }

    /**
     * 从资源对象中读取图片并开始处理
     *
     * @param resource 图片资源对象
     * @return {@link Image}
     */
    public static Image from(Resource resource) {
        return from(resource.getStream());
    }

    /**
     * 从流读取图片并开始处理
     *
     * @param in 图片流
     * @return {@link Image}
     */
    public static Image from(InputStream in) {
        return new Image(ImageUtils.read(in));
    }

    /**
     * 从ImageInputStream取图片并开始处理
     *
     * @param imageStream 图片流
     * @return {@link Image}
     */
    public static Image from(ImageInputStream imageStream) {
        return new Image(ImageUtils.read(imageStream));
    }

    /**
     * 从URL取图片并开始处理
     *
     * @param imageUrl 图片URL
     * @return {@link Image}
     */
    public static Image from(URL imageUrl) {
        return new Image(ImageUtils.read(imageUrl));
    }

    /**
     * 从Image取图片并开始处理
     *
     * @param image 图片
     * @return {@link Image}
     */
    public static Image from(java.awt.Image image) {
        return new Image(ImageUtils.toBufferedImage(image));
    }

    /**
     * 将图片绘制在背景上
     *
     * @param backgroundImg 背景图片
     * @param image         要绘制的图片
     * @param rectangle     矩形对象,表示矩形区域的x,y,width,height,x,y从背景图片中心计算
     * @return 绘制后的背景
     */
    private static BufferedImage draw(BufferedImage backgroundImg, java.awt.Image image, Rectangle rectangle, float alpha) {
        final Graphics2D g = backgroundImg.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        g.drawImage(image, rectangle.x, rectangle.y, rectangle.width, rectangle.height, null); // 绘制切割后的图
        g.dispose();
        return backgroundImg;
    }

    /**
     * 计算旋转后的图片尺寸
     *
     * @param width  宽度
     * @param height 高度
     * @param degree 旋转角度
     * @return 计算后目标尺寸
     */
    private static Rectangle calcRotatedSize(int width, int height, int degree) {
        if (degree >= 90) {
            if (degree / 90 % 2 == 1) {
                int temp = height;
                height = width;
                width = temp;
            }
            degree = degree % 90;
        }
        double r = Math.sqrt(height * height + width * width) / 2;
        double len = 2 * Math.sin(Math.toRadians(degree) / 2) * r;
        double angel_alpha = (Math.PI - Math.toRadians(degree)) / 2;
        double angel_dalta_width = Math.atan((double) height / width);
        double angel_dalta_height = Math.atan((double) width / height);
        int len_dalta_width = (int) (len * Math.cos(Math.PI - angel_alpha - angel_dalta_width));
        int len_dalta_height = (int) (len * Math.cos(Math.PI - angel_alpha - angel_dalta_height));
        int des_width = width + len_dalta_width * 2;
        int des_height = height + len_dalta_height * 2;

        return new Rectangle(des_width, des_height);
    }

    /**
     * 设置目标图片文件格式,用于写出
     *
     * @param imgType 图片格式
     * @return this
     * @see FileType#TYPE_JPG
     * @see FileType#TYPE_PNG
     */
    public Image setTargetImageType(String imgType) {
        this.targetImageType = imgType;
        return this;
    }

    /**
     * 计算x,y坐标的时候是否从中心做为原始坐标开始计算
     *
     * @param positionBaseCentre 是否从中心做为原始坐标开始计算
     * @return the image
     */
    public Image setPositionBaseCentre(boolean positionBaseCentre) {
        this.positionBaseCentre = positionBaseCentre;
        return this;
    }

    /**
     * 设置图片输出质量,数字为0~1（不包括0和1）表示质量压缩比,除此数字外设置表示不压缩
     *
     * @param quality 质量,数字为0~1（不包括0和1）表示质量压缩比,除此数字外设置表示不压缩
     * @return the image
     */
    public Image setQuality(double quality) {
        return setQuality((float) quality);
    }

    /**
     * 设置图片输出质量,数字为0~1（不包括0和1）表示质量压缩比,除此数字外设置表示不压缩
     *
     * @param quality 质量,数字为0~1（不包括0和1）表示质量压缩比,除此数字外设置表示不压缩
     * @return image
     */
    public Image setQuality(float quality) {
        if (quality > 0 && quality < 1) {
            this.quality = quality;
        } else {
            this.quality = 1;
        }
        return this;
    }

    /**
     * 缩放图像（按比例缩放）
     *
     * @param scale 缩放比例 比例大于1时为放大,小于1大于0为缩小
     * @return this
     */
    public Image scale(float scale) {
        if (scale < 0) {
            // 自动修正负数
            scale = -scale;
        }

        final java.awt.Image srcImage = getValidSrcImg();

        // PNG图片特殊处理
        if (FileType.TYPE_PNG.equals(this.targetImageType)) {
            final AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance((double) scale, (double) scale), null);
            this.targetImage = op.filter(ImageUtils.toBufferedImage(srcImage), null);
        } else {
            final String scaleStr = Float.toString(scale);
            // 缩放后的图片宽
            int width = NumberUtils.mul(Integer.toString(srcImage.getWidth(null)), scaleStr).intValue();
            // 缩放后的图片高
            int height = NumberUtils.mul(Integer.toString(srcImage.getHeight(null)), scaleStr).intValue();
            scale(width, height);
        }
        return this;
    }

    /**
     * 缩放图像（按长宽缩放）
     * 注意：目标长宽与原图不成比例会变形
     *
     * @param width  目标宽度
     * @param height 目标高度
     * @return this
     */
    public Image scale(int width, int height) {
        final java.awt.Image srcImage = getValidSrcImg();

        int srcHeight = srcImage.getHeight(null);
        int srcWidth = srcImage.getWidth(null);
        int scaleType;
        if (srcHeight == height && srcWidth == width) {
            // 源与目标长宽一致返回原图
            this.targetImage = srcImage;
            return this;
        } else if (srcHeight < height || srcWidth < width) {
            // 放大图片使用平滑模式
            scaleType = java.awt.Image.SCALE_SMOOTH;
        } else {
            scaleType = java.awt.Image.SCALE_DEFAULT;
        }

        double sx = NumberUtils.div(width, srcWidth);
        double sy = NumberUtils.div(height, srcHeight);

        if (FileType.TYPE_PNG.equals(this.targetImageType)) {
            final AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(sx, sy), null);
            this.targetImage = op.filter(ImageUtils.toBufferedImage(srcImage), null);
        } else {
            this.targetImage = srcImage.getScaledInstance(width, height, scaleType);
        }

        return this;
    }

    /**
     * 等比缩放图像,此方法按照按照给定的长宽等比缩放图片,按照长宽缩放比最多的一边等比缩放,空白部分填充背景色
     * 缩放后默认为jpeg格式
     *
     * @param width      缩放后的宽度
     * @param height     缩放后的高度
     * @param fixedColor 比例不对时补充的颜色,不补充为<code>null</code>
     * @return this
     */
    public Image scale(int width, int height, Color fixedColor) {
        java.awt.Image srcImage = getValidSrcImg();
        int srcHeight = srcImage.getHeight(null);
        int srcWidth = srcImage.getWidth(null);
        double heightRatio = NumberUtils.div(height, srcHeight);
        double widthRatio = NumberUtils.div(width, srcWidth);

        if (widthRatio == heightRatio) {
            // 长宽都按照相同比例缩放时,返回缩放后的图片
            scale(width, height);
        } else if (widthRatio < heightRatio) {
            // 宽缩放比例多就按照宽缩放
            scale(width, (int) (srcHeight * widthRatio));
        } else {
            // 否则按照高缩放
            scale((int) (srcWidth * heightRatio), height);
        }

        // 获取缩放后的新的宽和高
        srcImage = getValidSrcImg();
        srcHeight = srcImage.getHeight(null);
        srcWidth = srcImage.getWidth(null);

        if (null == fixedColor) {// 补白
            fixedColor = Color.WHITE;
        }
        final BufferedImage image = new BufferedImage(width, height, getTypeInt());
        Graphics2D g = image.createGraphics();

        // 设置背景
        g.setBackground(fixedColor);
        g.clearRect(0, 0, width, height);

        // 在中间贴图
        g.drawImage(srcImage, (width - srcWidth) / 2, (height - srcHeight) / 2, srcWidth, srcHeight, fixedColor, null);

        g.dispose();
        this.targetImage = image;
        return this;
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割)
     *
     * @param rectangle 矩形对象,表示矩形区域的x,y,width,height
     * @return this
     */
    public Image cut(Rectangle rectangle) {
        final java.awt.Image srcImage = getValidSrcImg();
        rectangle = fixRectangle(rectangle, srcImage.getWidth(null), srcImage.getHeight(null));

        final ImageFilter cropFilter = new CropImageFilter(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        final java.awt.Image image = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(srcImage.getSource(), cropFilter));
        this.targetImage = ImageUtils.toBufferedImage(image);
        return this;
    }

    /**
     * 图像切割为圆形(按指定起点坐标和半径切割),填充满整个图片（直径取长宽最小值）
     *
     * @param x 原图的x坐标起始位置
     * @param y 原图的y坐标起始位置
     * @return this
     */
    public Image cut(int x, int y) {
        return cut(x, y, -1);
    }

    /**
     * 图像切割为圆形(按指定起点坐标和半径切割)
     *
     * @param x      原图的x坐标起始位置
     * @param y      原图的y坐标起始位置
     * @param radius 半径,小于0表示填充满整个图片（直径取长宽最小值）
     * @return this
     */
    public Image cut(int x, int y, int radius) {
        final java.awt.Image srcImage = getValidSrcImg();
        final int width = srcImage.getWidth(null);
        final int height = srcImage.getHeight(null);

        // 计算直径
        final int diameter = radius > 0 ? radius * 2 : Math.min(width, height);
        final BufferedImage targetImage = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = targetImage.createGraphics();
        g.setClip(new Ellipse2D.Double(0, 0, diameter, diameter));

        if (this.positionBaseCentre) {
            x = x - width / 2 + diameter / 2;
            y = y - height / 2 + diameter / 2;
        }
        g.drawImage(srcImage, x, y, null);
        g.dispose();
        this.targetImage = targetImage;
        return this;
    }

    /**
     * 图片圆角处理
     *
     * @param arc 圆角弧度,0~1,为长宽占比
     * @return this
     */
    public Image round(double arc) {
        final java.awt.Image srcImage = getValidSrcImg();
        final int width = srcImage.getWidth(null);
        final int height = srcImage.getHeight(null);

        // 通过弧度占比计算弧度
        arc = NumberUtils.mul(arc, Math.min(width, height));

        final BufferedImage targetImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = targetImage.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        // 抗锯齿
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, arc, arc));
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(srcImage, 0, 0, null);
        g2.dispose();
        this.targetImage = targetImage;
        return this;
    }

    /**
     * 彩色转为黑白
     *
     * @return this
     */
    public Image gray() {
        final ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        this.targetImage = op.filter(ImageUtils.toBufferedImage(getValidSrcImg()), null);
        return this;
    }

    /**
     * 彩色转为黑白二值化图片
     *
     * @return this
     */
    public Image binary() {
        this.targetImage = ImageUtils.copyImage(getValidSrcImg(), BufferedImage.TYPE_BYTE_BINARY);
        return this;
    }

    /**
     * 给图片添加文字水印
     * 此方法并不关闭流
     *
     * @param pressText 水印文字
     * @param color     水印的字体颜色
     * @param font      {@link Font} 字体相关信息
     * @param x         修正值  默认在中间,偏移量相对于中间偏移
     * @param y         修正值  默认在中间,偏移量相对于中间偏移
     * @param alpha     透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 处理后的图像
     */
    public Image pressText(String pressText, Color color, Font font, int x, int y, float alpha) {
        final BufferedImage targetImage = ImageUtils.toBufferedImage(getValidSrcImg());
        final Graphics2D g = targetImage.createGraphics();

        if (null == font) {
            // 默认字体
            font = new Font("Courier", Font.PLAIN, (int) (targetImage.getHeight() * 0.75));
        }

        // 抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.setFont(font);
        // 透明度
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        // 在指定坐标绘制水印文字
        final FontMetrics metrics = g.getFontMetrics(font);
        final int textLength = metrics.stringWidth(pressText);
        final int textHeight = metrics.getAscent() - metrics.getLeading() - metrics.getDescent();
        g.drawString(pressText, Math.abs(targetImage.getWidth() - textLength) / 2 + x, Math.abs(targetImage.getHeight() + textHeight) / 2 + y);
        g.dispose();
        this.targetImage = targetImage;

        return this;
    }

    /**
     * 给图片添加图片水印
     *
     * @param pressImage 水印图片,可以使用{@link ImageIO#read(File)}方法读取文件
     * @param x          修正值  默认在中间,偏移量相对于中间偏移
     * @param y          修正值  默认在中间,偏移量相对于中间偏移
     * @param alpha      透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return this
     */
    public Image pressImage(java.awt.Image pressImage, int x, int y, float alpha) {
        final int pressImgWidth = pressImage.getWidth(null);
        final int pressImgHeight = pressImage.getHeight(null);

        return pressImage(pressImage, new Rectangle(x, y, pressImgWidth, pressImgHeight), alpha);
    }

    /**
     * 给图片添加图片水印
     *
     * @param pressImage 水印图片,可以使用{@link ImageIO#read(File)}方法读取文件
     * @param rectangle  矩形对象,表示矩形区域的x,y,width,height,x,y从背景图片中心计算
     * @param alpha      透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return this
     */
    public Image pressImage(java.awt.Image pressImage, Rectangle rectangle, float alpha) {
        final java.awt.Image targetImage = getValidSrcImg();

        rectangle = fixRectangle(rectangle, targetImage.getWidth(null), targetImage.getHeight(null));
        this.targetImage = draw(ImageUtils.toBufferedImage(targetImage), pressImage, rectangle, alpha);
        return this;
    }

    /**
     * 旋转图片为指定角度
     * 来自：http://blog.51cto.com/cping1982/130066
     *
     * @param degree 旋转角度
     * @return 旋转后的图片
     * @since 5.3.8
     */
    public Image rotate(int degree) {
        final java.awt.Image image = getValidSrcImg();
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        final Rectangle rectangle = calcRotatedSize(width, height, degree);
        final BufferedImage targetImg = new BufferedImage(rectangle.width, rectangle.height, getTypeInt());
        Graphics2D graphics2d = targetImg.createGraphics();
        // 抗锯齿
        graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        // 从中心旋转
        graphics2d.translate((rectangle.width - width) / 2, (rectangle.height - height) / 2);
        graphics2d.rotate(Math.toRadians(degree), width / 2, height / 2);
        graphics2d.drawImage(image, 0, 0, null);
        graphics2d.dispose();
        this.targetImage = targetImg;
        return this;
    }

    /**
     * 水平翻转图像
     *
     * @return this
     */
    public Image flip() {
        final java.awt.Image image = getValidSrcImg();
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        final BufferedImage targetImg = new BufferedImage(width, height, getTypeInt());
        Graphics2D graphics2d = targetImg.createGraphics();
        graphics2d.drawImage(image, 0, 0, width, height, width, 0, 0, height, null);
        graphics2d.dispose();
        this.targetImage = targetImg;
        return this;
    }

    /**
     * 获取处理过的图片
     *
     * @return 处理过的图片
     */
    public java.awt.Image getImg() {
        return this.targetImage;
    }

    /**
     * 写出图像
     *
     * @param out 写出到的目标流
     * @return 是否成功写出, 如果返回false表示未找到合适的Writer
     * @throws InstrumentException IO异常
     */
    public boolean write(OutputStream out) throws InstrumentException {
        return write(ImageUtils.getImageOutputStream(out));
    }

    /**
     * 写出图像为PNG格式
     *
     * @param targetImageStream 写出到的目标流
     * @return 是否成功写出, 如果返回false表示未找到合适的Writer
     * @throws InstrumentException IO异常
     */
    public boolean write(ImageOutputStream targetImageStream) throws InstrumentException {
        Assert.notBlank(this.targetImageType, "Target image type is blank !");
        Assert.notNull(targetImageStream, "Target output stream is null !");

        final java.awt.Image targetImage = (null == this.targetImage) ? this.srcImage : this.targetImage;
        Assert.notNull(targetImage, "Target image is null !");

        return ImageUtils.write(targetImage, this.targetImageType, targetImageStream, this.quality);
    }

    /**
     * 写出图像为目标文件扩展名对应的格式
     *
     * @param targetFile 目标文件
     * @return 是否成功写出, 如果返回false表示未找到合适的Writer
     * @throws InstrumentException IO异常
     */
    public boolean write(File targetFile) throws InstrumentException {
        final String formatName = FileUtils.extName(targetFile);
        if (StringUtils.isNotBlank(formatName)) {
            this.targetImageType = formatName;
        }

        if (targetFile.exists()) {
            targetFile.delete();
        }

        ImageOutputStream out = null;
        try {
            out = ImageUtils.getImageOutputStream(targetFile);
            return write(out);
        } finally {
            IoUtils.close(out);
        }
    }

    /**
     * 获取int类型的图片类型
     *
     * @return 图片类型
     * @see BufferedImage#TYPE_INT_ARGB
     * @see BufferedImage#TYPE_INT_RGB
     */
    private int getTypeInt() {
        switch (this.targetImageType) {
            case FileType.TYPE_PNG:
                return BufferedImage.TYPE_INT_ARGB;
            default:
                return BufferedImage.TYPE_INT_RGB;
        }
    }

    /**
     * 获取有效的源图片,首先检查上一次处理的结果图片,如无则使用用户传入的源图片
     *
     * @return 有效的源图片
     */
    private java.awt.Image getValidSrcImg() {
        return ObjectUtils.defaultIfNull(this.targetImage, this.srcImage);
    }

    /**
     * 修正矩形框位置
     *
     * @param rectangle  矩形
     * @param baseWidth  参考宽
     * @param baseHeight 参考高
     * @return 修正后的{@link Rectangle}
     */
    private Rectangle fixRectangle(Rectangle rectangle, int baseWidth, int baseHeight) {
        if (this.positionBaseCentre) {
            // 修正图片位置从背景的中心计算
            rectangle.setLocation(//
                    rectangle.x + (Math.abs(baseWidth - rectangle.width) / 2),
                    rectangle.y + (Math.abs(baseHeight - rectangle.height) / 2)
            );
        }
        return rectangle;
    }

}
