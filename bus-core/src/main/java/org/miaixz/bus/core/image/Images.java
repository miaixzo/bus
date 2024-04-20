/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.image;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.image.element.AbstractElement;
import org.miaixz.bus.core.image.element.ImageElement;
import org.miaixz.bus.core.image.element.RectangleElement;
import org.miaixz.bus.core.image.element.TextElement;
import org.miaixz.bus.core.image.painter.Painter;
import org.miaixz.bus.core.image.painter.PainterFactory;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.Graphics;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.toolkit.*;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.ImageFilter;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 图像编辑器
 *
 * @author Kimi Liu
 * @since Java 17+
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
     * 画布圆角（针对整图）
     */
    private Integer roundCorner;

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
     * @param canvasWidth  画布宽
     * @param canvasHeight 画布高
     * @param fileType     输出图片格式
     */
    public Images(int canvasWidth, int canvasHeight, String fileType) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.fileType = fileType;
    }

    /**
     * @param canvasWidth  画布宽
     * @param canvasHeight 画布高
     * @param bgColor      画布颜色（如果需要透明背景，不要设这个参数，比方图片边缘是圆角的场景）
     * @param fileType     输出图片格式
     */
    public Images(int canvasWidth, int canvasHeight, Color bgColor, String fileType) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.fileType = fileType;
        RectangleElement bgElement = new RectangleElement(0, 0, canvasWidth, canvasHeight);
        bgElement.setColor(bgColor);
        this.list.add(bgElement);
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
                throw new InternalException(e.getMessage());
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
     * @param zoom       缩放模式
     * @param fileType   输出图片格式
     */
    public Images(String bgImageUrl, int width, int height, Scale.Zoom zoom, String fileType) {
        ImageElement bgImageElement = new ImageElement(bgImageUrl, 0, 0, width, height, zoom);
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
     */
    public Images(BufferedImage bgImage, int width, int height, Scale.Zoom zoomMode, String fileType) {
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
     * 高斯模糊（毛玻璃效果）
     *
     * @param srcImage 图片流
     * @param radius   半径
     * @return 图片流
     */
    public static BufferedImage makeBlur(BufferedImage srcImage, int radius) {

        if (radius < 1) {
            return srcImage;
        }

        int w = srcImage.getWidth();
        int h = srcImage.getHeight();

        int[] pix = new int[w * h];
        srcImage.getRGB(0, 0, w, h, pix, 0, w);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[Normal._256 * divsum];
        for (i = 0; i < Normal._256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> Normal._16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> Normal._16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << Normal._16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        srcImage.setRGB(0, 0, w, h, pix, 0, w);
        return srcImage;
    }

    /**
     * 圆角
     *
     * @param srcImage 图片流
     * @param width    宽度
     * @param height   高度
     * @param radius   半径
     * @return 图片流
     */
    public static BufferedImage makeRoundCorner(BufferedImage srcImage, int width, int height, int radius) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRoundRect(0, 0, width, height, radius, radius);
        g.setComposite(AlphaComposite.SrcIn);
        g.drawImage(srcImage, 0, 0, width, height, null);
        g.dispose();
        return image;
    }

    /**
     * 将图片绘制在背景上
     *
     * @param backgroundImg 背景图片
     * @param image         要绘制的图片
     * @param rectangle     矩形对象,表示矩形区域的x,y,width,height,x,y从背景图片中心计算
     * @return 绘制后的背景
     */
    private BufferedImage draw(BufferedImage backgroundImg, java.awt.Image image, Rectangle rectangle, float alpha) {
        final Graphics2D g = backgroundImg.createGraphics();
        Graphics.setAlpha(g, alpha);

        fixRectangle(rectangle, backgroundImg.getWidth(), backgroundImg.getHeight());
        Graphics.drawImg(g, image, rectangle);

        g.dispose();
        return backgroundImg;
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
        final Image srcImg = getValidSrcImg();

        // PNG图片特殊处理
        if (FileType.TYPE_PNG.equals(this.fileType)) {
            // 修正float转double导致的精度丢失
            final double scaleDouble = MathKit.toDouble(scale);
            this.targetImage = ImageKit.filter(AffineTransform.getScaleInstance(scaleDouble, scaleDouble),
                    ImageKit.toBufferedImage(srcImg, this.fileType));
        } else {
            // 缩放后的图片宽
            final int width = MathKit.mul((Number) srcImg.getWidth(null), scale).intValue();
            // 缩放后的图片高
            final int height = MathKit.mul((Number) srcImg.getHeight(null), scale).intValue();
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
        return scale(width, height, Image.SCALE_SMOOTH);
    }

    /**
     * 缩放图像(按长宽缩放)
     * 注意：目标长宽与原图不成比例会变形
     *
     * @param width     目标宽度
     * @param height    目标高度
     * @param scaleType 缩放类型，可选{@link Image#SCALE_SMOOTH}平滑模式或{@link Image#SCALE_DEFAULT}默认模式
     * @return this
     */
    public Images scale(int width, int height, int scaleType) {
        final Image srcImage = getValidSrcImg();

        int srcHeight = srcImage.getHeight(null);
        int srcWidth = srcImage.getWidth(null);
        if (srcHeight == height && srcWidth == width) {
            // 源与目标长宽一致返回原图
            this.targetImage = srcImage;
            return this;
        }

        if (FileType.TYPE_PNG.equals(this.fileType)) {
            // 宽度缩放比
            final double sx = MathKit.div(width, srcWidth);
            // 高度缩放比
            final double sy = MathKit.div(height, srcHeight);
            this.targetImage = ImageKit.filter(AffineTransform.getScaleInstance(sx, sy),
                    ImageKit.toBufferedImage(srcImage, this.fileType));
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
        this.targetImage = ImageKit.filter(cropFilter, srcImage);
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
     * 彩色转为灰度
     *
     * @return this
     */
    public Images gray() {
        this.targetImage = ImageKit.filter(ColorSpace.getInstance(ColorSpace.CS_GRAY), getValidSrcBufferedImg());
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
     * 此方法只在给定位置写出一个水印字符串
     *
     * @param pressText 水印文字
     * @param color     水印的字体颜色
     * @param font      {@link Font} 字体相关信息
     * @param x         修正值。 默认在中间，偏移量相对于中间偏移
     * @param y         修正值。 默认在中间，偏移量相对于中间偏移
     * @param alpha     透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 处理后的图像
     */
    public Images pressText(String pressText, Color color, Font font, int x, int y, float alpha) {
        return pressText(pressText, color, font, new Point(x, y), alpha);
    }

    /**
     * 给图片添加文字水印
     * 此方法只在给定位置写出一个水印字符串
     *
     * @param pressText 水印文字
     * @param color     水印的字体颜色
     * @param font      {@link Font} 字体相关信息
     * @param point     绘制字符串的位置坐标
     * @param alpha     透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 处理后的图像
     */
    public Images pressText(String pressText, Color color, Font font, Point point, float alpha) {
        final BufferedImage targetImage = ImageKit.toBufferedImage(getValidSrcImg(), this.fileType);

        if (null == font) {
            // 默认字体
            font = Fonts.createSansSerifFont((int) (targetImage.getHeight() * 0.75));
        }

        final Graphics2D g = targetImage.createGraphics();
        // 透明度
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));

        // 绘制
        if (positionBaseCentre) {
            // 基于中心绘制
            Graphics.drawString(g, pressText, font, color,
                    new Rectangle(point.x, point.y, targetImage.getWidth(), targetImage.getHeight()));
        } else {
            // 基于左上角绘制
            Graphics.drawString(g, pressText, font, color, point);
        }

        // 收笔
        g.dispose();
        this.targetImage = targetImage;

        return this;
    }

    /**
     * 给图片添加全屏文字水印
     *
     * @param pressText  水印文字，文件间的间隔使用尾部添加空格方式实现
     * @param color      水印的字体颜色
     * @param font       {@link Font} 字体相关信息
     * @param lineHeight 行高
     * @param degree     旋转角度，（单位：弧度），以圆点（0,0）为圆心，正代表顺时针，负代表逆时针
     * @param alpha      透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 处理后的图像
     */
    public Images pressTextFull(String pressText, Color color, Font font, int lineHeight, int degree, float alpha) {
        final BufferedImage targetImage = ImageKit.toBufferedImage(getValidSrcImg(), this.fileType);

        if (null == font) {
            // 默认字体
            font = Fonts.createSansSerifFont((int) (targetImage.getHeight() * 0.75));
        }
        final int targetHeight = targetImage.getHeight();
        final int targetWidth = targetImage.getWidth();

        // 创建画笔，并设置透明度和角度
        final Graphics2D g = targetImage.createGraphics();
        g.setColor(color);
        // 基于图片中心旋转
        g.rotate(Math.toRadians(degree), targetWidth >> 1, targetHeight >> 1);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));

        //获取字符串本身的长宽
        Dimension dimension;
        try {
            dimension = Fonts.getDimension(g.getFontMetrics(font), pressText);
        } catch (Exception e) {
            // 此处报告bug某些情况下会抛出IndexOutOfBoundsException，在此做容错处理
            dimension = new Dimension(targetWidth / 3, targetHeight / 3);
        }
        final int intervalHeight = dimension.height * lineHeight;
        // 在画笔按照画布中心旋转后，达到45度时，上下左右会出现空白区，此处各延申长款的1.5倍实现全覆盖
        int y = -targetHeight >> 1;
        while (y < targetHeight * 1.5) {
            int x = -targetWidth >> 1;
            while (x < targetWidth * 1.5) {
                Graphics.drawString(g, pressText, font, color, new Point(x, y));
                x += dimension.width;
            }
            y += intervalHeight;
        }
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
        if (0 == degree) {
            return this;
        }
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
        return getValidSrcImg();
    }

    /**
     * 写出图像为结果设置格式
     * 结果类型设定见{@link #setTargetImageType(String)}
     *
     * @param out 写出到的目标流
     * @return 是否成功写出, 如果返回false表示未找到合适的Writer
     * @throws InternalException IO异常
     */
    public boolean write(OutputStream out) throws InternalException {
        return write(ImageKit.getImageOutputStream(out));
    }

    /**
     * 写出图像为结果设置格式
     * 结果类型设定见{@link #setTargetImageType(String)}
     *
     * @param targetImageStream 写出到的目标流
     * @return 是否成功写出, 如果返回false表示未找到合适的Writer
     * @throws InternalException IO异常
     */
    public boolean write(ImageOutputStream targetImageStream) throws InternalException {
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
     * @throws InternalException IO异常
     */
    public boolean write(File targetFile) throws InternalException {
        final String formatName = FileKit.getSuffix(targetFile);
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
        final BufferedImage image = ImageKit.toBufferedImage(getValidSrcImg(), this.fileType);
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
     * 获取有效的源{@link BufferedImage}图片，首先检查上一次处理的结果图片，如无则使用用户传入的源图片
     *
     * @return 有效的源图片
     */
    private BufferedImage getValidSrcBufferedImg() {
        return ImageKit.toBufferedImage(getValidSrcImg(), this.fileType);
    }

    /**
     * 修正矩形框位置，如果{@link Images#setPositionBaseCentre(boolean)} 设为{@code true}，
     * 则坐标修正为基于图形中心，否则基于左上角
     *
     * @param rectangle  矩形
     * @param baseWidth  参考宽
     * @param baseHeight 参考高
     * @return 修正后的{@link Rectangle}
     */
    private Rectangle fixRectangle(Rectangle rectangle, int baseWidth, int baseHeight) {
        if (this.positionBaseCentre) {
            final Point pointBaseCentre = ImageKit.getPointCentre(rectangle, baseWidth, baseHeight);
            // 修正图片位置从背景的中心计算
            rectangle.setLocation(pointBaseCentre.x, pointBaseCentre.y);
        }
        return rectangle;
    }

    /**
     * 获取合成后的图片流
     *
     * @return {@link InputStream}
     * @throws Exception 异常
     */
    public InputStream getInputStream() throws Exception {
        if (null != this.srcImage) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                ImageIO.write(srcImage, fileType, os);
                return new ByteArrayInputStream(os.toByteArray());
            } catch (Exception e) {
                throw new Exception("执行图片合成失败，无法输出文件流");
            }
        } else {
            throw new Exception("尚未执行图片合成，无法输出文件流");
        }
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
        if (fileType == FileType.TYPE_PNG) {
            this.srcImage = g.getDeviceConfiguration().createCompatibleImage(canvasWidth, canvasHeight, Transparency.TRANSLUCENT);
            g = this.srcImage.createGraphics();
        }
        // 抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.white);

        // 循环绘制各元素
        for (AbstractElement element : this.list) {
            Painter painter = PainterFactory.newInstance(element);
            if (element.isRepeat()) {
                // 平铺绘制
                painter.drawRepeat(g, element, canvasWidth, canvasHeight);
            } else {
                // 正常绘制
                painter.draw(g, element, canvasWidth);
            }
        }
        g.dispose();

        // 处理整图圆角
        if (roundCorner != null) {
            this.srcImage = this.makeRoundCorner(this.srcImage, canvasWidth, canvasHeight, roundCorner);
        }

        return this.srcImage;
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
     * 保存合成后的图片
     *
     * @param filePath 完整保存路径，如 “d://123.jpg”
     * @throws IOException
     */
    public void out(String filePath) throws Exception {
        if (srcImage != null) {
            ImageIO.write(this.srcImage, fileType, new File(filePath));
        } else {
            throw new Exception("尚未执行图片合成，无法保存文件");
        }
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
     * @param imgUrl 图片地址
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
     * @param imgUrl 图片地址
     * @param x      x坐标
     * @param y      y坐标
     * @param width  宽度
     * @param height 高度
     * @param zoom   缩放模式
     * @return {@link ImageElement}
     */
    public ImageElement addImageElement(String imgUrl, int x, int y, int width, int height, Scale.Zoom zoom) {
        ImageElement imageElement = new ImageElement(imgUrl, x, y, width, height, zoom);
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
     * 添加矩形元素
     *
     * @param x      x坐标
     * @param y      y坐标
     * @param width  宽度
     * @param height 高度
     * @return the object
     */
    public RectangleElement addRectangleElement(int x, int y, int width, int height) {
        RectangleElement rectangleElement = new RectangleElement(x, y, width, height);
        this.list.add(rectangleElement);
        return rectangleElement;
    }

    /**
     * 添加图片元素
     *
     * @param image  图片对象
     * @param x      x坐标
     * @param y      y坐标
     * @param width  宽度
     * @param height 高度
     * @param zoom   缩放模式
     * @return {@link ImageElement}
     */
    public ImageElement addImageElement(BufferedImage image, int x, int y, int width, int height, Scale.Zoom zoom) {
        ImageElement imageElement = new ImageElement(image, x, y, width, height, zoom);
        this.list.add(imageElement);
        return imageElement;
    }

    /**
     * 设置背景高斯模糊
     *
     * @param blur 模糊值
     */
    public void setBackgroundBlur(int blur) {
        ImageElement bgElement = (ImageElement) this.list.get(0);
        bgElement.setBlur(blur);
    }

    /**
     * 设置画布圆角（针对整图）
     *
     * @param roundCorner 模糊值
     */
    public void setCanvasRoundCorner(Integer roundCorner) throws Exception {
        if (fileType != FileType.TYPE_PNG) {
            throw new Exception("整图圆角，输出格式必须设置为PNG");
        }
        this.roundCorner = roundCorner;
    }

    /**
     * 获取画布宽度
     *
     * @return the int
     */
    public int getCanvasWidth() {
        return canvasWidth;
    }

    /**
     * 获取画布高度
     *
     * @return the int
     */
    public int getCanvasHeight() {
        return canvasHeight;
    }

    /**
     * 获取合成后的图片对象
     *
     * @return {@link BufferedImage}
     */
    public BufferedImage getSrcImage() {
        return srcImage;
    }

}
