/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.core.image;

import org.aoju.bus.core.image.element.AbstractElement;
import org.aoju.bus.core.image.element.ImageElement;
import org.aoju.bus.core.image.element.TextElement;
import org.aoju.bus.core.image.painter.Painter;
import org.aoju.bus.core.image.painter.PainterFactory;
import org.aoju.bus.core.image.painter.TextPainter;
import org.aoju.bus.core.io.resource.Resource;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.FileType;
import org.aoju.bus.core.lang.Scale;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.*;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 图像编辑器
 *
 * @author Kimi Liu
 * @version 6.2.5
 * @since JDK 1.8+
 */
public class Images implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 待绘制的元素集合
     */
    private final List<AbstractElement> list = new ArrayList<>();
    /**
     * 图片来源
     */
    private BufferedImage srcImage;
    /**
     * 合成图片
     */
    private Image targetImage;
    /**
     * 输出质量
     */
    private float quality = -1;
    /**
     * 画布宽度
     */
    private int canvasWidth;
    /**
     * 画布高度
     */
    private int canvasHeight;
    /**
     * 输出格式
     */
    private String fileType = FileType.TYPE_JPG;
    /**
     * 计算x,y坐标的时候是否从中心做为原始坐标开始计算
     */
    private boolean positionBaseCentre = true;

    /**
     * 构造
     *
     * @param srcImage 来源图片
     */
    public Images(BufferedImage srcImage) {
        this.srcImage = srcImage;
    }

    /**
     * 构造
     *
     * @param imageUrl 背景图片地址（画布以背景图宽高为基准）
     * @param fileType 输出图片格式
     */
    public Images(String imageUrl, String fileType) {
        ImageElement bgImageElement = new ImageElement(imageUrl, 0, 0);
        this.list.add(bgImageElement);
        this.canvasWidth = bgImageElement.getImage().getWidth();
        this.canvasHeight = bgImageElement.getImage().getHeight();
        this.fileType = fileType;
    }

    /**
     * 构造
     *
     * @param srcImage 来源图片
     * @param fileType 目标图片类型，null则读取来源图片类型
     */
    public Images(BufferedImage srcImage, String fileType) {
        ImageElement bgImageElement = new ImageElement(srcImage, 0, 0);
        this.list.add(bgImageElement);
        this.canvasWidth = srcImage.getWidth();
        this.canvasHeight = srcImage.getHeight();
        this.fileType = fileType;
        this.srcImage = srcImage;
        if (null == this.fileType) {
            if (srcImage.getType() == BufferedImage.TYPE_INT_ARGB
                    || srcImage.getType() == BufferedImage.TYPE_INT_ARGB_PRE
                    || srcImage.getType() == BufferedImage.TYPE_4BYTE_ABGR
                    || srcImage.getType() == BufferedImage.TYPE_4BYTE_ABGR_PRE
            ) {
                this.fileType = FileType.TYPE_PNG;
            } else {
                this.fileType = FileType.TYPE_JPG;
            }
        }
    }

    /**
     * 构造:图片合成专用
     *
     * @param srcImage 背景图片对象（画布以背景图宽高为基准）
     * @param imageUrl 背景图片地址（画布以背景图宽高为基准）
     * @param fileType 输出图片格式
     */
    public Images(BufferedImage srcImage, String imageUrl, String fileType) {
        ImageElement imageElement;
        if (StringKit.isNotEmpty(imageUrl)) {
            imageElement = new ImageElement(imageUrl, 0, 0);
            try {
                this.canvasWidth = imageElement.getImage().getWidth();
                this.canvasHeight = imageElement.getImage().getHeight();
            } catch (Exception e) {
                throw new InstrumentException(e.getMessage());
            }
        } else {
            imageElement = new ImageElement(srcImage, 0, 0);
            this.canvasWidth = imageElement.getWidth();
            this.canvasHeight = imageElement.getHeight();
        }
        this.list.add(imageElement);
        this.fileType = fileType;
    }

    /**
     * @param bgImageUrl 背景图片地址
     * @param width      背景图宽度
     * @param height     背景图高度
     * @param zoomMode   缩放模式
     * @param fileType   输出图片格式
     * @throws Exception
     */
    public Images(String bgImageUrl, int width, int height, Scale.Mode zoomMode, String fileType) throws Exception {
        ImageElement bgImageElement = new ImageElement(bgImageUrl, 0, 0, width, height, zoomMode);
        this.list.add(bgImageElement);
        this.canvasWidth = width;
        this.canvasHeight = height;
        this.fileType = fileType;
    }

    /**
     * @param bgImage  背景图片对象
     * @param width    背景图宽度
     * @param height   背景图高度
     * @param zoomMode 缩放模式
     * @param fileType 输出图片格式
     * @throws Exception
     */
    public Images(BufferedImage bgImage, int width, int height, Scale.Mode zoomMode, String fileType) throws Exception {
        ImageElement bgImageElement = new ImageElement(bgImage, 0, 0, width, height, zoomMode);
        // 计算画布新宽高
        int canvasWidth = 0;
        int canvasHeight = 0;

        switch (zoomMode) {
            case ORIGIN:
                canvasWidth = bgImage.getWidth();
                canvasHeight = bgImage.getHeight();
                break;
            case WIDTH:
                canvasWidth = width;
                canvasHeight = bgImage.getHeight() * canvasWidth / bgImage.getWidth();
                break;
            case HEIGHT:
                canvasHeight = height;
                canvasWidth = bgImage.getWidth() * canvasHeight / bgImage.getHeight();
                break;
            case OPTIONAL:
                canvasHeight = width;
                canvasWidth = height;
                break;
        }

        this.list.add(bgImageElement);
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.fileType = fileType;
    }

    /**
     * 从Path读取图片并开始处理
     *
     * @param imagePath 图片文件路径
     * @return {@link Images}
     */
    public static Images from(Path imagePath) {
        return from(imagePath.toFile());
    }

    /**
     * 从文件读取图片并开始处理
     *
     * @param imageFile 图片文件
     * @return {@link Images}
     */
    public static Images from(File imageFile) {
        return new Images(ImageKit.read(imageFile));
    }

    /**
     * 从资源对象中读取图片并开始处理
     *
     * @param resource 图片资源对象
     * @return {@link Images}
     */
    public static Images from(Resource resource) {
        return from(resource.getStream());
    }

    /**
     * 从流读取图片并开始处理
     *
     * @param in 图片流
     * @return {@link Images}
     */
    public static Images from(InputStream in) {
        return new Images(ImageKit.read(in));
    }

    /**
     * 从ImageInputStream取图片并开始处理
     *
     * @param imageStream 图片流
     * @return {@link Images}
     */
    public static Images from(ImageInputStream imageStream) {
        return new Images(ImageKit.read(imageStream));
    }

    /**
     * 从URL取图片并开始处理
     *
     * @param imageUrl 图片URL
     * @return {@link Images}
     */
    public static Images from(URL imageUrl) {
        return new Images(ImageKit.read(imageUrl));
    }

    /**
     * 从Image取图片并开始处理
     *
     * @param image 图片
     * @return {@link Images}
     */
    public static Images from(java.awt.Image image) {
        return new Images(ImageKit.toBufferedImage(image));
    }

    /**
     * 图片合成专用-读取图片
     *
     * @param srcImage 背景图片对象（画布以背景图宽高为基准）
     * @param imageUrl 背景图片地址（画布以背景图宽高为基准）
     * @param fileType 输出图片格式
     * @return {@link Images}
     */
    public static Images from(BufferedImage srcImage, String imageUrl, String fileType) {
        return new Images(srcImage, imageUrl, fileType);
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
    public Images setTargetImageType(String imgType) {
        this.fileType = imgType;
        return this;
    }

    /**
     * 计算x,y坐标的时候是否从中心做为原始坐标开始计算
     *
     * @param positionBaseCentre 是否从中心做为原始坐标开始计算
     * @return the image
     */
    public Images setPositionBaseCentre(boolean positionBaseCentre) {
        this.positionBaseCentre = positionBaseCentre;
        return this;
    }

    /**
     * 设置图片输出质量,数字为0~1(不包括0和1)表示质量压缩比,除此数字外设置表示不压缩
     *
     * @param quality 质量,数字为0~1(不包括0和1)表示质量压缩比,除此数字外设置表示不压缩
     * @return the image
     */
    public Images setQuality(double quality) {
        return setQuality((float) quality);
    }

    /**
     * 设置图片输出质量,数字为0~1(不包括0和1)表示质量压缩比,除此数字外设置表示不压缩
     *
     * @param quality 质量,数字为0~1(不包括0和1)表示质量压缩比,除此数字外设置表示不压缩
     * @return image
     */
    public Images setQuality(float quality) {
        if (quality > 0 && quality < 1) {
            this.quality = quality;
        } else {
            this.quality = 1;
        }
        return this;
    }

    /**
     * 缩放图像(按比例缩放)
     *
     * @param scale 缩放比例 比例大于1时为放大,小于1大于0为缩小
     * @return this
     */
    public Images scale(float scale) {
        if (scale < 0) {
            // 自动修正负数
            scale = -scale;
        }

        final java.awt.Image srcImage = getValidSrcImg();

        // PNG图片特殊处理
        if (FileType.TYPE_PNG.equals(this.fileType)) {
            final AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(scale, scale), null);
            this.targetImage = op.filter(ImageKit.toBufferedImage(srcImage), null);
        } else {
            final String scaleStr = Float.toString(scale);
            // 缩放后的图片宽
            int width = MathKit.mul(Integer.toString(srcImage.getWidth(null)), scaleStr).intValue();
            // 缩放后的图片高
            int height = MathKit.mul(Integer.toString(srcImage.getHeight(null)), scaleStr).intValue();
            scale(width, height);
        }
        return this;
    }

    /**
     * 缩放图像(按长宽缩放)
     * 注意：目标长宽与原图不成比例会变形
     *
     * @param width  目标宽度
     * @param height 目标高度
     * @return this
     */
    public Images scale(int width, int height) {
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

        double sx = MathKit.div(width, srcWidth);
        double sy = MathKit.div(height, srcHeight);

        if (FileType.TYPE_PNG.equals(this.fileType)) {
            final AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(sx, sy), null);
            this.targetImage = op.filter(ImageKit.toBufferedImage(srcImage), null);
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
    public Images scale(int width, int height, Color fixedColor) {
        java.awt.Image srcImage = getValidSrcImg();
        int srcHeight = srcImage.getHeight(null);
        int srcWidth = srcImage.getWidth(null);
        double heightRatio = MathKit.div(height, srcHeight);
        double widthRatio = MathKit.div(width, srcWidth);

        // 浮点数之间的等值判断,基本数据类型不能用==比较,包装数据类型不能用equals来判断
        if (MathKit.equals(heightRatio, widthRatio)) {
            // 长宽都按照相同比例缩放时，返回缩放后的图片
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

        final BufferedImage image = new BufferedImage(width, height, getTypeInt());
        Graphics2D g = image.createGraphics();

        // 设置背景
        if (null != fixedColor) {
            g.setBackground(fixedColor);
            g.clearRect(0, 0, width, height);
        }

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
    public Images cut(Rectangle rectangle) {
        final java.awt.Image srcImage = getValidSrcImg();
        rectangle = fixRectangle(rectangle, srcImage.getWidth(null), srcImage.getHeight(null));

        final ImageFilter cropFilter = new CropImageFilter(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        final java.awt.Image image = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(srcImage.getSource(), cropFilter));
        this.targetImage = ImageKit.toBufferedImage(image);
        return this;
    }

    /**
     * 图像切割为圆形(按指定起点坐标和半径切割),填充满整个图片(直径取长宽最小值)
     *
     * @param x 原图的x坐标起始位置
     * @param y 原图的y坐标起始位置
     * @return this
     */
    public Images cut(int x, int y) {
        return cut(x, y, -1);
    }

    /**
     * 图像切割为圆形(按指定起点坐标和半径切割)
     *
     * @param x      原图的x坐标起始位置
     * @param y      原图的y坐标起始位置
     * @param radius 半径,小于0表示填充满整个图片(直径取长宽最小值)
     * @return this
     */
    public Images cut(int x, int y, int radius) {
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
    public Images round(double arc) {
        final java.awt.Image srcImage = getValidSrcImg();
        final int width = srcImage.getWidth(null);
        final int height = srcImage.getHeight(null);

        // 通过弧度占比计算弧度
        arc = MathKit.mul(arc, Math.min(width, height));

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
    public Images gray() {
        final ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        this.targetImage = op.filter(ImageKit.toBufferedImage(getValidSrcImg()), null);
        return this;
    }

    /**
     * 彩色转为黑白二值化图片
     *
     * @return this
     */
    public Images binary() {
        this.targetImage = ImageKit.copyImage(getValidSrcImg(), BufferedImage.TYPE_BYTE_BINARY);
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
     * @param alpha     透明度：alpha 必须是范围 [0.0, 1.0] 之内(包含边界值)的一个浮点数字
     * @return 处理后的图像
     */
    public Images pressText(String pressText, Color color, Font font, int x, int y, float alpha) {
        final BufferedImage targetImage = ImageKit.toBufferedImage(getValidSrcImg());
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
     * @param alpha      透明度：alpha 必须是范围 [0.0, 1.0] 之内(包含边界值)的一个浮点数字
     * @return this
     */
    public Images pressImage(java.awt.Image pressImage, int x, int y, float alpha) {
        final int pressImgWidth = pressImage.getWidth(null);
        final int pressImgHeight = pressImage.getHeight(null);

        return pressImage(pressImage, new Rectangle(x, y, pressImgWidth, pressImgHeight), alpha);
    }

    /**
     * 给图片添加图片水印
     *
     * @param pressImage 水印图片,可以使用{@link ImageIO#read(File)}方法读取文件
     * @param rectangle  矩形对象,表示矩形区域的x,y,width,height,x,y从背景图片中心计算
     * @param alpha      透明度：alpha 必须是范围 [0.0, 1.0] 之内(包含边界值)的一个浮点数字
     * @return this
     */
    public Images pressImage(java.awt.Image pressImage, Rectangle rectangle, float alpha) {
        final java.awt.Image targetImage = getValidSrcImg();

        this.targetImage = draw(ImageKit.toBufferedImage(targetImage, this.fileType), pressImage, rectangle, alpha);
        return this;
    }

    /**
     * 旋转图片为指定角度
     * 来自：http://blog.51cto.com/cping1982/130066
     *
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public Images rotate(int degree) {
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
    public Images flip() {
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
        return null == this.targetImage ? this.srcImage : this.targetImage;
    }

    /**
     * 写出图像
     *
     * @param out 写出到的目标流
     * @return 是否成功写出, 如果返回false表示未找到合适的Writer
     * @throws InstrumentException IO异常
     */
    public boolean write(OutputStream out) throws InstrumentException {
        return write(ImageKit.getImageOutputStream(out));
    }

    /**
     * 写出图像为PNG格式
     *
     * @param targetImageStream 写出到的目标流
     * @return 是否成功写出, 如果返回false表示未找到合适的Writer
     * @throws InstrumentException IO异常
     */
    public boolean write(ImageOutputStream targetImageStream) throws InstrumentException {
        Assert.notBlank(this.fileType, "Target image type is blank !");
        Assert.notNull(targetImageStream, "Target output stream is null !");

        final java.awt.Image targetImage = (null == this.targetImage) ? this.srcImage : this.targetImage;
        Assert.notNull(targetImage, "Target image is null !");

        return ImageKit.write(targetImage, this.fileType, targetImageStream, this.quality);
    }

    /**
     * 写出图像为目标文件扩展名对应的格式
     *
     * @param targetFile 目标文件
     * @return 是否成功写出, 如果返回false表示未找到合适的Writer
     * @throws InstrumentException IO异常
     */
    public boolean write(File targetFile) throws InstrumentException {
        final String formatName = FileKit.extName(targetFile);
        if (StringKit.isNotBlank(formatName)) {
            this.fileType = formatName;
        }

        if (targetFile.exists()) {
            targetFile.delete();
        }

        ImageOutputStream out = null;
        try {
            out = ImageKit.getImageOutputStream(targetFile);
            return write(out);
        } finally {
            IoKit.close(out);
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
        switch (this.fileType) {
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
        return ObjectKit.defaultIfNull(this.targetImage, this.srcImage);
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

    /**
     * 描边，此方法为向内描边，会覆盖图片相应的位置
     *
     * @param color 描边颜色，默认黑色
     * @param width 边框粗细
     * @return this
     */
    public Images stroke(Color color, float width) {
        return stroke(color, new BasicStroke(width));
    }

    /**
     * 描边，此方法为向内描边，会覆盖图片相应的位置
     *
     * @param color  描边颜色，默认黑色
     * @param stroke 描边属性，包括粗细、线条类型等，见{@link BasicStroke}
     * @return this
     */
    public Images stroke(Color color, Stroke stroke) {
        final BufferedImage image = ImageKit.toBufferedImage(getValidSrcImg());
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        Graphics2D g = image.createGraphics();

        g.setColor(ObjectKit.defaultIfNull(color, Color.BLACK));
        if (null != stroke) {
            g.setStroke(stroke);
        }

        g.drawRect(0, 0, width - 1, height - 1);

        g.dispose();
        this.targetImage = image;

        return this;
    }

    /**
     * 合成图片，返回图片对象
     *
     * @return {@link BufferedImage}
     * @throws Exception 异常
     */
    public BufferedImage merge() throws Exception {
        this.srcImage = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = this.srcImage.createGraphics();

        // PNG要做透明度处理，否则背景图透明部分会变黑
        if (this.fileType == FileType.TYPE_PNG) {
            this.srcImage = g.getDeviceConfiguration().createCompatibleImage(canvasWidth, canvasHeight, Transparency.TRANSLUCENT);
            g = this.srcImage.createGraphics();
        }

        // 抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 循环绘制
        for (AbstractElement element : this.list) {
            Painter painter = PainterFactory.newInstance(element);
            painter.draw(g, element, canvasWidth);
        }
        g.dispose();

        return this.srcImage;
    }

    /**
     * 保存合成后的图片
     *
     * @param filePath 完整保存路径，如 “E://123.jpg”
     * @throws IOException 异常
     */
    public void out(String filePath) throws Exception {
        if (null != this.srcImage) {
            ImageIO.write(this.srcImage, fileType, new File(filePath));
        } else {
            throw new Exception("尚未执行图片合成，无法保存文件");
        }
    }

    /**
     * 获取合成后的图片对象
     *
     * @return {@link BufferedImage}
     */
    public BufferedImage getBufferedImage() {
        return this.srcImage;
    }

    /**
     * 获取合成后的图片流
     *
     * @return {@link InputStream}
     * @throws Exception 异常
     */
    public InputStream getInputStream() throws Exception {
        if (null != this.srcImage) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(this.srcImage, fileType, os);
            return new ByteArrayInputStream(os.toByteArray());
        } else {
            throw new Exception("尚未执行图片合成，无法输出文件流");
        }
    }

    /**
     * 计算多行文本高度
     *
     * @param textElement 文本元素
     * @return 高度数值
     */
    public int getLineHeight(TextElement textElement) {
        TextPainter textPainter = new TextPainter();
        List<TextElement> textElements = textPainter.getBreakLineElements(textElement);
        return textElement.getLineHeight() * textElements.size();
    }

    /**
     * 计算文本宽度
     *
     * @param textElement 文本元素
     * @return 高度数值
     */
    public int getFrontWidth(TextElement textElement) {
        TextPainter textPainter = new TextPainter();
        return textPainter.getFrontWidth(textElement.getText(), textElement.getFont());
    }

    /**
     * 添加元素（图片或文本）
     *
     * @param element 图片或文本元素
     */
    public void addElement(AbstractElement element) {
        this.list.add(element);
    }

    /**
     * 添加图片元素
     *
     * @param imgUrl 图片url
     * @param x      x坐标
     * @param y      y坐标
     * @return {@link ImageElement}
     */
    public ImageElement addImageElement(String imgUrl, int x, int y) {
        ImageElement imageElement = new ImageElement(imgUrl, x, y);
        this.list.add(imageElement);
        return imageElement;
    }

    /**
     * 添加图片元素
     *
     * @param image 图片对象
     * @param x     x坐标
     * @param y     y坐标
     * @return {@link ImageElement}
     */
    public ImageElement addImageElement(BufferedImage image, int x, int y) {
        ImageElement imageElement = new ImageElement(image, x, y);
        this.list.add(imageElement);
        return imageElement;
    }

    /**
     * 添加图片元素
     *
     * @param imgUrl 图片rul
     * @param x      x坐标
     * @param y      y坐标
     * @param width  宽度
     * @param height 高度
     * @param mode   缩放模式
     * @return {@link ImageElement}
     */
    public ImageElement addImageElement(String imgUrl, int x, int y, int width, int height, Scale.Mode mode) {
        ImageElement imageElement = new ImageElement(imgUrl, x, y, width, height, mode);
        this.list.add(imageElement);
        return imageElement;
    }

    /**
     * 添加图片元素
     *
     * @param image  图片对象
     * @param x      x坐标
     * @param y      y坐标
     * @param width  宽度
     * @param height 高度
     * @param mode   缩放模式
     * @return {@link ImageElement}
     */
    public ImageElement addImageElement(BufferedImage image, int x, int y, int width, int height, Scale.Mode mode) {
        ImageElement imageElement = new ImageElement(image, x, y, width, height, mode);
        this.list.add(imageElement);
        return imageElement;
    }

    /**
     * 添加文本元素
     *
     * @param text 文本
     * @param font Font对象
     * @param x    x坐标
     * @param y    y坐标
     * @return {@link TextElement}
     */
    public TextElement addTextElement(String text, Font font, int x, int y) {
        TextElement textElement = new TextElement(text, font, x, y);
        this.list.add(textElement);
        return textElement;
    }

    /**
     * 添加文本元素
     *
     * @param text     文本
     * @param fontSize 字体大小
     * @param x        x坐标
     * @param y        y坐标
     * @return {@link TextElement}
     */
    public TextElement addTextElement(String text, int fontSize, int x, int y) {
        TextElement textElement = new TextElement(text, fontSize, x, y);
        this.list.add(textElement);
        return textElement;
    }

    /**
     * 添加文本元素
     *
     * @param text     文本
     * @param fontName 字体名称
     * @param fontSize 字体大小
     * @param x        x坐标
     * @param y        y坐标
     * @return {@link TextElement}
     */
    public TextElement addTextElement(String text, String fontName, int fontSize, int x, int y) {
        TextElement textElement = new TextElement(text, fontName, fontSize, x, y);
        this.list.add(textElement);
        return textElement;
    }

    /**
     * 设置背景高斯模糊
     *
     * @param blur 模糊值
     */
    public void setBackgroundBlur(int blur) {
        ImageElement bgElement = (ImageElement) list.get(0);
        bgElement.setBlur(blur);
    }

}
