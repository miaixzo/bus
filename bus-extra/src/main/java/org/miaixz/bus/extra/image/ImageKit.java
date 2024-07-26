/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.image;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.io.stream.FastByteArrayOutputStream;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.xyz.*;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.util.Iterator;

/**
 * 图片处理工具类： 功能：缩放图像、切割图像、旋转、图像类型转换、彩色转黑白、文字水印、图片水印等 参考：<a href=
 * "http://blog.csdn.net/zhangzhikaixinya/article/details/8459400">http://blog.csdn.net/zhangzhikaixinya/article/details/8459400</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageKit {

    /**
     * 图形交换格式：GIF
     */
    public static final String IMAGE_TYPE_GIF = "gif";
    /**
     * 联合照片专家组：JPG
     */
    public static final String IMAGE_TYPE_JPG = "jpg";
    /**
     * 联合照片专家组：JPEG
     */
    public static final String IMAGE_TYPE_JPEG = "jpeg";
    /**
     * 英文Bitmap（位图）的简写，它是Windows操作系统中的标准图像文件格式：BMP
     */
    public static final String IMAGE_TYPE_BMP = "bmp";
    /**
     * 可移植网络图形：PNG
     */
    public static final String IMAGE_TYPE_PNG = "png";
    /**
     * Photoshop的专用格式：PSD
     */
    public static final String IMAGE_TYPE_PSD = "psd";

    /**
     * 缩放图像（按比例缩放），目标文件的扩展名决定目标文件类型
     *
     * @param srcImageFile  源图像文件
     * @param destImageFile 缩放后的图像文件，扩展名决定目标类型
     * @param scale         缩放比例。比例大于1时为放大，小于1大于0为缩小
     */
    public static void scale(final File srcImageFile, final File destImageFile, final float scale) {
        BufferedImage image = null;
        try {
            image = read(srcImageFile);
            scale(image, destImageFile, scale);
        } finally {
            flush(image);
        }
    }

    /**
     * 缩放图像（按比例缩放） 缩放后默认为jpeg格式，此方法并不关闭流
     *
     * @param srcStream    源图像来源流
     * @param targetStream 缩放后的图像写出到的流
     * @param scale        缩放比例。比例大于1时为放大，小于1大于0为缩小
     */
    public static void scale(final InputStream srcStream, final OutputStream targetStream, final float scale) {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            scale(image, targetStream, scale);
        } finally {
            flush(image);
        }
    }

    /**
     * 缩放图像（按比例缩放） 缩放后默认为jpeg格式，此方法并不关闭流
     *
     * @param srcStream    源图像来源流
     * @param targetStream 缩放后的图像写出到的流
     * @param scale        缩放比例。比例大于1时为放大，小于1大于0为缩小
     */
    public static void scale(final ImageInputStream srcStream, final ImageOutputStream targetStream,
            final float scale) {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            scale(image, targetStream, scale);
        } finally {
            flush(image);
        }
    }

    /**
     * 缩放图像（按比例缩放） 缩放后默认为jpeg格式，此方法并不关闭流
     *
     * @param srcImg   源图像来源流，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param destFile 缩放后的图像写出到的流
     * @param scale    缩放比例。比例大于1时为放大，小于1大于0为缩小
     * @throws InternalException IO异常
     */
    public static void scale(final Image srcImg, final File destFile, final float scale) throws InternalException {
        Images.from(srcImg).setTargetImageType(FileName.extName(destFile)).scale(scale).write(destFile);
    }

    /**
     * 缩放图像（按比例缩放） 缩放后默认为jpeg格式，此方法并不关闭流
     *
     * @param srcImg 源图像来源流，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param out    缩放后的图像写出到的流
     * @param scale  缩放比例。比例大于1时为放大，小于1大于0为缩小
     * @throws InternalException IO异常
     */
    public static void scale(final Image srcImg, final OutputStream out, final float scale) throws InternalException {
        scale(srcImg, getImageOutputStream(out), scale);
    }

    /**
     * 缩放图像（按比例缩放） 缩放后默认为jpeg格式，此方法并不关闭流
     *
     * @param srcImg          源图像来源流，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param destImageStream 缩放后的图像写出到的流
     * @param scale           缩放比例。比例大于1时为放大，小于1大于0为缩小
     * @throws InternalException IO异常
     */
    public static void scale(final Image srcImg, final ImageOutputStream destImageStream, final float scale)
            throws InternalException {
        writeJpg(scale(srcImg, scale), destImageStream);
    }

    /**
     * 缩放图像（按比例缩放）
     *
     * @param srcImg 源图像来源流，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param scale  缩放比例。比例大于1时为放大，小于1大于0为缩小
     * @return {@link Image}
     */
    public static Image scale(final Image srcImg, final float scale) {
        return Images.from(srcImg).scale(scale).getImg();
    }

    /**
     * 缩放图像（按长宽缩放） 注意：目标长宽与原图不成比例会变形
     *
     * @param srcImg 源图像来源流，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param width  目标宽度
     * @param height 目标高度
     * @return {@link Image}
     */
    public static Image scale(final Image srcImg, final int width, final int height) {
        return Images.from(srcImg).scale(width, height).getImg();
    }

    /**
     * 缩放图像（按高度和宽度缩放） 缩放后默认格式与源图片相同，无法识别原图片默认JPG
     *
     * @param srcImageFile    源图像文件地址
     * @param targetImageFile 缩放后的图像地址
     * @param width           缩放后的宽度
     * @param height          缩放后的高度
     * @param fixedColor      补充的颜色，不补充为{@code null}
     * @throws InternalException IO异常
     */
    public static void scale(final File srcImageFile, final File targetImageFile, final int width, final int height,
            final Color fixedColor) throws InternalException {
        Images images = null;
        try {
            images = Images.from(srcImageFile);
            images.setTargetImageType(FileName.extName(targetImageFile)).scale(width, height, fixedColor)//
                    .write(targetImageFile);
        } finally {
            IoKit.flush(images);
        }
    }

    /**
     * 缩放图像（按高度和宽度缩放） 缩放后默认为jpeg格式，此方法并不关闭流
     *
     * @param srcStream    源图像流
     * @param targetStream 缩放后的图像目标流
     * @param width        缩放后的宽度
     * @param height       缩放后的高度
     * @param fixedColor   比例不对时补充的颜色，不补充为{@code null}
     * @throws InternalException IO异常
     */
    public static void scale(final InputStream srcStream, final OutputStream targetStream, final int width,
            final int height, final Color fixedColor) throws InternalException {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            scale(image, getImageOutputStream(targetStream), width, height, fixedColor);
        } finally {
            flush(image);
        }
    }

    /**
     * 缩放图像（按高度和宽度缩放） 缩放后默认为jpeg格式，此方法并不关闭流
     *
     * @param srcStream    源图像流
     * @param targetStream 缩放后的图像目标流
     * @param width        缩放后的宽度
     * @param height       缩放后的高度
     * @param fixedColor   比例不对时补充的颜色，不补充为{@code null}
     * @throws InternalException IO异常
     */
    public static void scale(final ImageInputStream srcStream, final ImageOutputStream targetStream, final int width,
            final int height, final Color fixedColor) throws InternalException {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            scale(image, targetStream, width, height, fixedColor);
        } finally {
            flush(image);
        }
    }

    /**
     * 缩放图像（按高度和宽度缩放） 缩放后默认为jpeg格式，此方法并不关闭流
     *
     * @param srcImage          源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param targetImageStream 缩放后的图像目标流
     * @param width             缩放后的宽度
     * @param height            缩放后的高度
     * @param fixedColor        比例不对时补充的颜色，不补充为{@code null}
     * @throws InternalException IO异常
     */
    public static void scale(final Image srcImage, final ImageOutputStream targetImageStream, final int width,
            final int height, final Color fixedColor) throws InternalException {
        writeJpg(scale(srcImage, width, height, fixedColor), targetImageStream);
    }

    /**
     * 缩放图像（按高度和宽度缩放） 缩放后默认为jpeg格式
     *
     * @param srcImage   源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param width      缩放后的宽度
     * @param height     缩放后的高度
     * @param fixedColor 比例不对时补充的颜色，不补充为{@code null}
     * @return {@link Image}
     */
    public static Image scale(final Image srcImage, final int width, final int height, final Color fixedColor) {
        return Images.from(srcImage).scale(width, height, fixedColor).getImg();
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割)
     *
     * @param srcImgFile    源图像文件
     * @param targetImgFile 切片后的图像文件
     * @param rectangle     矩形对象，表示矩形区域的x，y，width，height
     */
    public static void cut(final File srcImgFile, final File targetImgFile, final Rectangle rectangle) {
        BufferedImage image = null;
        try {
            image = read(srcImgFile);
            cut(image, targetImgFile, rectangle);
        } finally {
            flush(image);
        }
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割)，此方法并不关闭流
     *
     * @param srcStream    源图像流
     * @param targetStream 切片后的图像输出流
     * @param rectangle    矩形对象，表示矩形区域的x，y，width，height
     */
    public static void cut(final InputStream srcStream, final OutputStream targetStream, final Rectangle rectangle) {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            cut(image, targetStream, rectangle);
        } finally {
            flush(image);
        }
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割)，此方法并不关闭流
     *
     * @param srcStream    源图像流
     * @param targetStream 切片后的图像输出流
     * @param rectangle    矩形对象，表示矩形区域的x，y，width，height
     */
    public static void cut(final ImageInputStream srcStream, final ImageOutputStream targetStream,
            final Rectangle rectangle) {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            cut(image, targetStream, rectangle);
        } finally {
            flush(image);
        }
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割)，此方法并不关闭流
     *
     * @param srcImage  源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param destFile  输出的文件
     * @param rectangle 矩形对象，表示矩形区域的x，y，width，height
     * @throws InternalException IO异常
     */
    public static void cut(final Image srcImage, final File destFile, final Rectangle rectangle)
            throws InternalException {
        write(cut(srcImage, rectangle), destFile);
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割)，此方法并不关闭流
     *
     * @param srcImage  源图像
     * @param out       切片后的图像输出流
     * @param rectangle 矩形对象，表示矩形区域的x，y，width，height
     * @throws InternalException IO异常
     */
    public static void cut(final Image srcImage, final OutputStream out, final Rectangle rectangle)
            throws InternalException {
        cut(srcImage, getImageOutputStream(out), rectangle);
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割)，此方法并不关闭流
     *
     * @param srcImage          源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param targetImageStream 切片后的图像输出流
     * @param rectangle         矩形对象，表示矩形区域的x，y，width，height
     * @throws InternalException IO异常
     */
    public static void cut(final Image srcImage, final ImageOutputStream targetImageStream, final Rectangle rectangle)
            throws InternalException {
        writeJpg(cut(srcImage, rectangle), targetImageStream);
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割)
     *
     * @param srcImage  源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param rectangle 矩形对象，表示矩形区域的x，y，width，height
     * @return {@link BufferedImage}
     */
    public static Image cut(final Image srcImage, final Rectangle rectangle) {
        return Images.from(srcImage).setPositionBaseCentre(false).cut(rectangle).getImg();
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割)，填充满整个图片（直径取长宽最小值）
     *
     * @param srcImage 源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param x        原图的x坐标起始位置
     * @param y        原图的y坐标起始位置
     * @return {@link Image}
     */
    public static Image cut(final Image srcImage, final int x, final int y) {
        return cut(srcImage, x, y, -1);
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割)
     *
     * @param srcImage 源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param x        原图的x坐标起始位置
     * @param y        原图的y坐标起始位置
     * @param radius   半径，小于0表示填充满整个图片（直径取长宽最小值）
     * @return {@link Image}
     */
    public static Image cut(final Image srcImage, final int x, final int y, final int radius) {
        return Images.from(srcImage).cut(x, y, radius).getImg();
    }

    /**
     * 图像切片（指定切片的宽度和高度）
     *
     * @param srcImageFile 源图像
     * @param descDir      切片目标文件夹
     * @param targetWidth  目标切片宽度。默认200
     * @param targetHeight 目标切片高度。默认150
     */
    public static void slice(final File srcImageFile, final File descDir, final int targetWidth,
            final int targetHeight) {
        BufferedImage image = null;
        try {
            image = read(srcImageFile);
            slice(image, descDir, targetWidth, targetHeight);
        } finally {
            flush(image);
        }
    }

    /**
     * 图像切片（指定切片的宽度和高度）
     *
     * @param srcImage     源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param descDir      切片目标文件夹
     * @param targetWidth  目标切片宽度。默认200
     * @param targetHeight 目标切片高度。默认150
     */
    public static void slice(final Image srcImage, final File descDir, int targetWidth, int targetHeight) {
        if (targetWidth <= 0) {
            targetWidth = 200; // 切片宽度
        }
        if (targetHeight <= 0) {
            targetHeight = 150; // 切片高度
        }
        final int srcWidth = srcImage.getWidth(null); // 源图宽度
        final int srcHeight = srcImage.getHeight(null); // 源图高度

        if (srcWidth < targetWidth) {
            targetWidth = srcWidth;
        }
        if (srcHeight < targetHeight) {
            targetHeight = srcHeight;
        }

        final int cols; // 切片横向数量
        final int rows; // 切片纵向数量
        // 计算切片的横向和纵向数量
        if (srcWidth % targetWidth == 0) {
            cols = srcWidth / targetWidth;
        } else {
            cols = (int) Math.floor((double) srcWidth / targetWidth) + 1;
        }
        if (srcHeight % targetHeight == 0) {
            rows = srcHeight / targetHeight;
        } else {
            rows = (int) Math.floor((double) srcHeight / targetHeight) + 1;
        }
        // 循环建立切片
        Image tag;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // 四个参数分别为图像起点坐标和宽高
                // 即: CropImageFilter(int x,int y,int width,int height)
                tag = cut(srcImage, new Rectangle(j * targetWidth, i * targetHeight, targetWidth, targetHeight));
                // 输出为文件
                write(tag, FileKit.file(descDir, "_r" + i + "_c" + j + ".jpg"));
            }
        }
    }

    /**
     * 图像切割（指定切片的行数和列数）
     *
     * @param srcImageFile 源图像文件
     * @param targetDir    切片目标文件夹
     * @param formatName   格式名称，即图片格式后缀
     * @param rows         目标切片行数。默认2，必须是范围 [1, 20] 之内
     * @param cols         目标切片列数。默认2，必须是范围 [1, 20] 之内
     */
    public static void sliceByRowsAndCols(final File srcImageFile, final File targetDir, final String formatName,
            final int rows, final int cols) {
        BufferedImage image = null;
        try {
            image = read(srcImageFile);
            sliceByRowsAndCols(image, targetDir, formatName, rows, cols);
        } finally {
            flush(image);
        }
    }

    /**
     * 图像切割（指定切片的行数和列数），默认RGB模式
     *
     * @param srcImage   源图像，如果非{@link BufferedImage}，则默认使用RGB模式，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param destDir    切片目标文件夹
     * @param formatName 格式名称，即图片格式后缀
     * @param rows       目标切片行数。默认2，必须是范围 [1, 20] 之内
     * @param cols       目标切片列数。默认2，必须是范围 [1, 20] 之内
     */
    public static void sliceByRowsAndCols(final Image srcImage, final File destDir, final String formatName, int rows,
            int cols) {
        if (!destDir.exists()) {
            FileKit.mkdir(destDir);
        } else if (!destDir.isDirectory()) {
            throw new IllegalArgumentException("Destination must be a Directory !");
        }

        if (rows <= 0 || rows > 20) {
            rows = 2; // 切片行数
        }
        if (cols <= 0 || cols > 20) {
            cols = 2; // 切片列数
        }
        // 读取源图像
        final int srcWidth = srcImage.getWidth(null); // 源图宽度
        final int srcHeight = srcImage.getHeight(null); // 源图高度

        final int targetWidth = MathKit.partValue(srcWidth, cols); // 每张切片的宽度
        final int targetHeight = MathKit.partValue(srcHeight, rows); // 每张切片的高度

        // 循环建立切片
        Image tag;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                tag = cut(srcImage, new Rectangle(j * targetWidth, i * targetHeight, targetWidth, targetHeight));
                // 输出为文件
                write(tag, new File(destDir, "_r" + i + "_c" + j + "." + formatName));
            }
        }
    }

    /**
     * 图像类型转换：GIF=JPG、GIF=PNG、PNG=JPG、PNG=GIF(X)、BMP=PNG
     *
     * @param srcImageFile    源图像文件
     * @param targetImageFile 目标图像文件
     */
    public static void convert(final File srcImageFile, final File targetImageFile) {
        Assert.notNull(srcImageFile);
        Assert.notNull(targetImageFile);
        Assert.isFalse(srcImageFile.equals(targetImageFile), "Src file is equals to dest file!");

        // 通过扩展名检查图片类型，相同类型直接复制
        final String srcExtName = FileName.extName(srcImageFile);
        final String destExtName = FileName.extName(targetImageFile);
        if (StringKit.equalsIgnoreCase(srcExtName, destExtName)) {
            // 扩展名相同直接复制文件
            FileKit.copy(srcImageFile, targetImageFile, true);
        }

        Images images = null;
        try {
            images = Images.from(srcImageFile);
            images.write(targetImageFile);
        } finally {
            IoKit.flush(images);
        }
    }

    /**
     * 图像类型转换：GIF=JPG、GIF=PNG、PNG=JPG、PNG=GIF(X)、BMP=PNG 此方法并不关闭流
     *
     * @param srcStream    源图像流
     * @param formatName   包含格式非正式名称的 String：如JPG、JPEG、GIF等
     * @param targetStream 目标图像输出流
     */
    public static void convert(final InputStream srcStream, final String formatName, final OutputStream targetStream) {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            write(image, formatName, getImageOutputStream(targetStream));
        } finally {
            flush(image);
        }
    }

    /**
     * 图像类型转换：GIF=JPG、GIF=PNG、PNG=JPG、PNG=GIF(X)、BMP=PNG 此方法并不关闭流
     *
     * @param srcImage          源图像流，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param formatName        包含格式非正式名称的 String：如JPG、JPEG、GIF等
     * @param targetImageStream 目标图像输出流
     */
    public static void convert(final Image srcImage, final String formatName,
            final ImageOutputStream targetImageStream) {
        Images.from(srcImage).setTargetImageType(formatName).write(targetImageStream);
    }

    /**
     * 彩色转为黑白
     *
     * @param srcImageFile    源图像地址
     * @param targetImageFile 目标图像地址
     */
    public static void gray(final File srcImageFile, final File targetImageFile) {
        BufferedImage image = null;
        try {
            image = read(srcImageFile);
            gray(image, targetImageFile);
        } finally {
            flush(image);
        }
    }

    /**
     * 彩色转为黑白 此方法并不关闭流
     *
     * @param srcStream    源图像流
     * @param targetStream 目标图像流
     */
    public static void gray(final InputStream srcStream, final OutputStream targetStream) {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            gray(image, targetStream);
        } finally {
            flush(image);
        }
    }

    /**
     * 彩色转为黑白 此方法并不关闭流
     *
     * @param srcStream    源图像流
     * @param targetStream 目标图像流
     */
    public static void gray(final ImageInputStream srcStream, final ImageOutputStream targetStream) {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            gray(image, targetStream);
        } finally {
            flush(image);
        }
    }

    /**
     * 彩色转为黑白
     *
     * @param srcImage 源图像流，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param outFile  目标文件
     */
    public static void gray(final Image srcImage, final File outFile) {
        write(gray(srcImage), outFile);
    }

    /**
     * 彩色转为黑白 此方法并不关闭流
     *
     * @param srcImage 源图像流，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param out      目标图像流
     */
    public static void gray(final Image srcImage, final OutputStream out) {
        gray(srcImage, getImageOutputStream(out));
    }

    /**
     * 彩色转为黑白 此方法并不关闭流
     *
     * @param srcImage          源图像流，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param targetImageStream 目标图像流
     * @throws InternalException IO异常
     */
    public static void gray(final Image srcImage, final ImageOutputStream targetImageStream) throws InternalException {
        writeJpg(gray(srcImage), targetImageStream);
    }

    /**
     * 彩色转为黑白
     *
     * @param srcImage 源图像流，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @return {@link Image}灰度后的图片
     */
    public static Image gray(final Image srcImage) {
        return Images.from(srcImage).gray().getImg();
    }

    /**
     * 彩色转为黑白二值化图片，根据目标文件扩展名确定转换后的格式
     *
     * @param srcImageFile    源图像地址
     * @param targetImageFile 目标图像地址
     */
    public static void binary(final File srcImageFile, final File targetImageFile) {
        BufferedImage image = null;
        try {
            image = read(srcImageFile);
            binary(image, targetImageFile);
        } finally {
            flush(image);
        }
    }

    /**
     * 彩色转为黑白二值化图片 此方法并不关闭流
     *
     * @param srcStream    源图像流
     * @param targetStream 目标图像流
     * @param imageType    图片格式(扩展名)
     */
    public static void binary(final InputStream srcStream, final OutputStream targetStream, final String imageType) {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            binary(image, getImageOutputStream(targetStream), imageType);
        } finally {
            flush(image);
        }
    }

    /**
     * 彩色转为黑白黑白二值化图片 此方法并不关闭流
     *
     * @param srcStream    源图像流
     * @param targetStream 目标图像流
     * @param imageType    图片格式(扩展名)
     */
    public static void binary(final ImageInputStream srcStream, final ImageOutputStream targetStream,
            final String imageType) {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            binary(image, targetStream, imageType);
        } finally {
            flush(image);
        }
    }

    /**
     * 彩色转为黑白二值化图片，根据目标文件扩展名确定转换后的格式
     *
     * @param srcImage 源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param outFile  目标文件
     */
    public static void binary(final Image srcImage, final File outFile) {
        write(binary(srcImage), outFile);
    }

    /**
     * 彩色转为黑白二值化图片 此方法并不关闭流，输出JPG格式
     *
     * @param srcImage  源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param out       目标图像流
     * @param imageType 图片格式(扩展名)
     */
    public static void binary(final Image srcImage, final OutputStream out, final String imageType) {
        binary(srcImage, getImageOutputStream(out), imageType);
    }

    /**
     * 彩色转为黑白二值化图片 此方法并不关闭流，输出JPG格式
     *
     * @param srcImage          源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param targetImageStream 目标图像流
     * @param imageType         图片格式(扩展名)
     * @throws InternalException IO异常
     */
    public static void binary(final Image srcImage, final ImageOutputStream targetImageStream, final String imageType)
            throws InternalException {
        write(binary(srcImage), imageType, targetImageStream);
    }

    /**
     * 彩色转为黑白二值化图片
     *
     * @param srcImage 源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @return {@link Image}二值化后的图片
     */
    public static Image binary(final Image srcImage) {
        return Images.from(srcImage).binary().getImg();
    }

    /**
     * 给图片添加文字水印
     *
     * @param imageFile  源图像文件
     * @param targetFile 目标图像文件
     * @param pressText  水印文字
     */
    public static void pressText(final File imageFile, final File targetFile, final ImageText pressText) {
        BufferedImage image = null;
        try {
            image = read(imageFile);
            pressText(image, targetFile, pressText);
        } finally {
            flush(image);
        }
    }

    /**
     * 给图片添加文字水印 此方法并不关闭流
     *
     * @param srcStream    源图像流
     * @param targetStream 目标图像流
     * @param pressText    水印文本信息
     */
    public static void pressText(final InputStream srcStream, final OutputStream targetStream,
            final ImageText pressText) {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            pressText(image, getImageOutputStream(targetStream), pressText);
        } finally {
            flush(image);
        }
    }

    /**
     * 给图片添加文字水印 此方法并不关闭流
     *
     * @param srcImage   源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param targetFile 目标流
     * @param pressText  水印文字信息
     * @throws InternalException IO异常
     */
    public static void pressText(final Image srcImage, final File targetFile, final ImageText pressText)
            throws InternalException {
        write(pressText(srcImage, pressText), targetFile);
    }

    /**
     * 给图片添加文字水印 此方法并不关闭流
     *
     * @param srcImage  源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param to        目标流
     * @param pressText 水印文字信息
     * @throws InternalException IO异常
     */
    public static void pressText(final Image srcImage, final OutputStream to, final ImageText pressText)
            throws InternalException {
        pressText(srcImage, getImageOutputStream(to), pressText);
    }

    /**
     * 给图片添加文字水印 此方法并不关闭流
     *
     * @param srcImage          源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param targetImageStream 目标图像流
     * @param pressText         水印文字信息
     * @throws InternalException IO异常
     */
    public static void pressText(final Image srcImage, final ImageOutputStream targetImageStream,
            final ImageText pressText) throws InternalException {
        writeJpg(pressText(srcImage, pressText), targetImageStream);
    }

    /**
     * 给图片添加文字水印 此方法并不关闭流
     *
     * @param srcImage  源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param pressText 水印文字信息
     * @return 处理后的图像
     */
    public static Image pressText(final Image srcImage, final ImageText pressText) {
        return Images.from(srcImage).pressText(pressText).getImg();
    }

    /**
     * 给图片添加全屏文字水印 此方法并不关闭流
     *
     * @param srcImage   源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param pressText  水印文字
     * @param color      水印的字体颜色
     * @param font       {@link Font} 字体相关信息，如果默认则为{@code null}
     * @param lineHeight 行高
     * @param degree     旋转角度，（单位：弧度），以圆点（0,0）为圆心，正代表顺时针，负代表逆时针
     * @param alpha      透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 处理后的图像
     */
    public static Image pressTextFull(final Image srcImage, final String pressText, final Color color, final Font font,
            final int lineHeight, final int degree, final float alpha) {
        return Images.from(srcImage).pressTextFull(pressText, color, font, lineHeight, degree, alpha).getImg();
    }

    /**
     * 给图片添加图片水印
     *
     * @param srcImageFile    源图像文件
     * @param targetImageFile 目标图像文件
     * @param pressImg        水印图片，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param x               修正值。 默认在中间，偏移量相对于中间偏移
     * @param y               修正值。 默认在中间，偏移量相对于中间偏移
     * @param alpha           透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     */
    public static void pressImage(final File srcImageFile, final File targetImageFile, final Image pressImg,
            final int x, final int y, final float alpha) {
        BufferedImage image = null;
        try {
            image = read(srcImageFile);
            pressImage(image, targetImageFile, pressImg, x, y, alpha);
        } finally {
            flush(image);
        }
    }

    /**
     * 给图片添加图片水印 此方法并不关闭流
     *
     * @param srcStream    源图像流
     * @param targetStream 目标图像流
     * @param pressImg     水印图片，可以使用{@link ImageIO#read(File)}方法读取文件
     * @param x            修正值。 默认在中间，偏移量相对于中间偏移
     * @param y            修正值。 默认在中间，偏移量相对于中间偏移
     * @param alpha        透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     */
    public static void pressImage(final InputStream srcStream, final OutputStream targetStream, final Image pressImg,
            final int x, final int y, final float alpha) {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            pressImage(image, getImageOutputStream(targetStream), pressImg, x, y, alpha);
        } finally {
            flush(image);
        }
    }

    /**
     * 给图片添加图片水印 此方法并不关闭流
     *
     * @param srcImage 源图像流，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param outFile  写出文件
     * @param pressImg 水印图片，可以使用{@link ImageIO#read(File)}方法读取文件
     * @param x        修正值。 默认在中间，偏移量相对于中间偏移
     * @param y        修正值。 默认在中间，偏移量相对于中间偏移
     * @param alpha    透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @throws InternalException IO异常
     */
    public static void pressImage(final Image srcImage, final File outFile, final Image pressImg, final int x,
            final int y, final float alpha) throws InternalException {
        write(pressImage(srcImage, pressImg, x, y, alpha), outFile);
    }

    /**
     * 给图片添加图片水印，写出目标图片格式为JPG 此方法并不关闭流
     *
     * @param srcImage 源图像流，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param out      目标图像流
     * @param pressImg 水印图片，可以使用{@link ImageIO#read(File)}方法读取文件
     * @param x        修正值。 默认在中间，偏移量相对于中间偏移
     * @param y        修正值。 默认在中间，偏移量相对于中间偏移
     * @param alpha    透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @throws InternalException IO异常
     */
    public static void pressImage(final Image srcImage, final OutputStream out, final Image pressImg, final int x,
            final int y, final float alpha) throws InternalException {
        pressImage(srcImage, getImageOutputStream(out), pressImg, x, y, alpha);
    }

    /**
     * 给图片添加图片水印，写出目标图片格式为JPG 此方法并不关闭流
     *
     * @param srcImage          源图像流，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param targetImageStream 目标图像流
     * @param pressImg          水印图片，可以使用{@link ImageIO#read(File)}方法读取文件
     * @param x                 修正值。 默认在中间，偏移量相对于中间偏移
     * @param y                 修正值。 默认在中间，偏移量相对于中间偏移
     * @param alpha             透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @throws InternalException IO异常
     */
    public static void pressImage(final Image srcImage, final ImageOutputStream targetImageStream, final Image pressImg,
            final int x, final int y, final float alpha) throws InternalException {
        writeJpg(pressImage(srcImage, pressImg, x, y, alpha), targetImageStream);
    }

    /**
     * 给图片添加图片水印 此方法并不关闭流
     *
     * @param srcImage 源图像流，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param pressImg 水印图片，可以使用{@link ImageIO#read(File)}方法读取文件
     * @param x        修正值。 默认在中间，偏移量相对于中间偏移
     * @param y        修正值。 默认在中间，偏移量相对于中间偏移
     * @param alpha    透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 结果图片
     */
    public static Image pressImage(final Image srcImage, final Image pressImg, final int x, final int y,
            final float alpha) {
        return Images.from(srcImage).pressImage(pressImg, x, y, alpha).getImg();
    }

    /**
     * 给图片添加图片水印 此方法并不关闭流
     *
     * @param srcImage  源图像流，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param pressImg  水印图片，可以使用{@link ImageIO#read(File)}方法读取文件
     * @param rectangle 矩形对象，表示矩形区域的x，y，width，height，x,y从背景图片中心计算
     * @param alpha     透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 结果图片
     */
    public static Image pressImage(final Image srcImage, final Image pressImg, final Rectangle rectangle,
            final float alpha) {
        return Images.from(srcImage).pressImage(pressImg, rectangle, alpha).getImg();
    }

    /**
     * 给图片添加全屏图片水印
     *
     * @param imageFile      源图像文件
     * @param targetFile     目标图像文件
     * @param pressImageFile 水印图像文件
     * @param lineHeight     行高
     * @param degree         水印图像旋转角度，（单位：弧度），以圆点（0,0）为圆心，正代表顺时针，负代表逆时针
     * @param alpha          透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @throws InternalException IO异常
     */
    public static void pressImageFull(final File imageFile, final File targetFile, final File pressImageFile,
            final int lineHeight, final int degree, final float alpha) throws InternalException {
        BufferedImage image = null;
        try {
            image = read(imageFile);
            write(pressImageFull(image, read(pressImageFile), lineHeight, degree, alpha), targetFile);
        } finally {
            flush(image);
        }
    }

    /**
     * 给图片添加全屏图像水印，写出格式为JPG 此方法并不关闭流
     *
     * @param srcStream    源图像流
     * @param targetStream 目标图像流
     * @param pressStream  水印图像流
     * @param lineHeight   行高
     * @param degree       水印图像旋转角度，（单位：弧度），以圆点（0,0）为圆心，正代表顺时针，负代表逆时针
     * @param alpha        透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @throws InternalException IO异常
     */
    public static void pressImageFull(final InputStream srcStream, final OutputStream targetStream,
            final InputStream pressStream, final int lineHeight, final int degree, final float alpha)
            throws InternalException {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            writeJpg(pressImageFull(image, read(pressStream), lineHeight, degree, alpha), targetStream);
        } finally {
            flush(image);
        }
    }

    /**
     * 给图片添加全屏图像水印 此方法并不关闭流
     *
     * @param srcImage   源图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param pressImage 水印图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param lineHeight 行高
     * @param degree     水印图像旋转角度，（单位：弧度），以圆点（0,0）为圆心，正代表顺时针，负代表逆时针，（单位：弧度），以圆点（0,0）为圆心，正代表顺时针，负代表逆时针
     * @param alpha      透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 处理后的图像
     */
    public static Image pressImageFull(final Image srcImage, final Image pressImage, final int lineHeight,
            final int degree, final float alpha) {
        return Images.from(srcImage).pressImageFull(pressImage, lineHeight, degree, alpha).getImg();
    }

    /**
     * 旋转图片为指定角度 此方法不会关闭输出流
     *
     * @param imageFile 被旋转图像文件
     * @param degree    旋转角度
     * @param outFile   输出文件
     * @throws InternalException IO异常
     */
    public static void rotate(final File imageFile, final int degree, final File outFile) throws InternalException {
        BufferedImage image = null;
        try {
            image = read(imageFile);
            rotate(image, degree, outFile);
        } finally {
            flush(image);
        }
    }

    /**
     * 旋转图片为指定角度 此方法不会关闭输出流
     *
     * @param image   需要旋转的图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param degree  旋转角度
     * @param outFile 输出文件
     * @throws InternalException IO异常
     */
    public static void rotate(final Image image, final int degree, final File outFile) throws InternalException {
        write(rotate(image, degree), outFile);
    }

    /**
     * 旋转图片为指定角度 此方法不会关闭输出流
     *
     * @param image  需要旋转的图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param degree 旋转角度
     * @param out    输出流
     * @throws InternalException IO异常
     */
    public static void rotate(final Image image, final int degree, final OutputStream out) throws InternalException {
        writeJpg(rotate(image, degree), getImageOutputStream(out));
    }

    /**
     * 旋转图片为指定角度 此方法不会关闭输出流，输出格式为JPG
     *
     * @param image  需要旋转的图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param degree 旋转角度
     * @param out    输出图像流
     * @throws InternalException IO异常
     */
    public static void rotate(final Image image, final int degree, final ImageOutputStream out)
            throws InternalException {
        writeJpg(rotate(image, degree), out);
    }

    /**
     * 旋转图片为指定角度 来自：<a href="http://blog.51cto.com/cping1982/130066">http://blog.51cto.com/cping1982/130066</a>
     *
     * @param image  需要旋转的图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Image rotate(final Image image, final int degree) {
        return Images.from(image).rotate(degree).getImg();
    }

    /**
     * 水平翻转图像
     *
     * @param imageFile 图像文件
     * @param outFile   输出文件
     * @throws InternalException IO异常
     */
    public static void flip(final File imageFile, final File outFile) throws InternalException {
        BufferedImage image = null;
        try {
            image = read(imageFile);
            flip(image, outFile);
        } finally {
            flush(image);
        }
    }

    /**
     * 水平翻转图像
     *
     * @param image   图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param outFile 输出文件
     * @throws InternalException IO异常
     */
    public static void flip(final Image image, final File outFile) throws InternalException {
        write(flip(image), outFile);
    }

    /**
     * 水平翻转图像
     *
     * @param image 图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param out   输出
     * @throws InternalException IO异常
     */
    public static void flip(final Image image, final OutputStream out) throws InternalException {
        flip(image, getImageOutputStream(out));
    }

    /**
     * 水平翻转图像，写出格式为JPG
     *
     * @param image 图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param out   输出
     * @throws InternalException IO异常
     */
    public static void flip(final Image image, final ImageOutputStream out) throws InternalException {
        writeJpg(flip(image), out);
    }

    /**
     * 水平翻转图像
     *
     * @param image 图像，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @return 翻转后的图片
     */
    public static Image flip(final Image image) {
        return Images.from(image).flip().getImg();
    }

    /**
     * 压缩图像，输出图像只支持jpg文件
     *
     * @param imageFile 图像文件
     * @param outFile   输出文件，只支持jpg文件
     * @param quality   压缩比例，必须为0~1
     * @throws InternalException IO异常
     */
    public static void compress(final File imageFile, final File outFile, final float quality)
            throws InternalException {
        Images images = null;
        try {
            images = Images.from(imageFile);
            images.setQuality(quality).write(outFile);
        } finally {
            IoKit.flush(images);
        }
    }

    /**
     * {@link Image} 转 {@link RenderedImage} 首先尝试强转，否则新建一个{@link BufferedImage}后重新绘制，使用
     * {@link BufferedImage#TYPE_INT_RGB} 模式。
     *
     * @param img       {@link Image}
     * @param imageType 目标图片类型，例如jpg或png等
     * @return {@link BufferedImage}
     */
    public static RenderedImage castToRenderedImage(final Image img, final String imageType) {
        if (img instanceof RenderedImage) {
            return (RenderedImage) img;
        }

        return toBufferedImage(img, imageType);
    }

    /**
     * {@link Image} 转 {@link BufferedImage} 首先尝试强转，否则新建一个{@link BufferedImage}后重新绘制，使用 imageType 模式
     *
     * @param img       {@link Image}
     * @param imageType 目标图片类型，例如jpg或png等
     * @return {@link BufferedImage}
     */
    public static BufferedImage castToBufferedImage(final Image img, final String imageType) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        return toBufferedImage(img, imageType);
    }

    /**
     * {@link Image} 转 {@link BufferedImage} 如果源图片的RGB模式与目标模式一致，则直接转换，否则重新绘制 默认的，png图片使用
     * {@link BufferedImage#TYPE_INT_ARGB}模式，其它使用 {@link BufferedImage#TYPE_INT_RGB} 模式
     *
     * @param image     {@link Image}
     * @param imageType 目标图片类型，例如jpg或png等
     * @return {@link BufferedImage}
     */
    public static BufferedImage toBufferedImage(final Image image, final String imageType) {
        return toBufferedImage(image, imageType, null);
    }

    /**
     * {@link Image} 转 {@link BufferedImage} 如果源图片的RGB模式与目标模式一致，则直接转换，否则重新绘制 默认的，png图片使用
     * {@link BufferedImage#TYPE_INT_ARGB}模式，其它使用 {@link BufferedImage#TYPE_INT_RGB} 模式
     *
     * @param image           {@link Image}
     * @param imageType       目标图片类型，例如jpg或png等
     * @param backgroundColor 背景色{@link Color}，{@code null} 表示默认背景色（黑色或者透明）
     * @return {@link BufferedImage}
     */
    public static BufferedImage toBufferedImage(final Image image, final String imageType,
            final Color backgroundColor) {
        final int type = IMAGE_TYPE_PNG.equalsIgnoreCase(imageType) ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB;
        return toBufferedImage(image, type, backgroundColor);
    }

    /**
     * {@link Image} 转 {@link BufferedImage} 如果源图片的RGB模式与目标模式一致，则直接转换，否则重新绘制
     *
     * @param image     {@link Image}
     * @param imageType 目标图片类型，{@link BufferedImage}中的常量，例如黑白等
     * @return {@link BufferedImage}
     */
    public static BufferedImage toBufferedImage(final Image image, final int imageType) {
        return toBufferedImage(image, imageType, null);
    }

    /**
     * {@link Image} 转 {@link BufferedImage} 如果源图片的RGB模式与目标模式一致，则直接转换，否则重新绘制
     *
     * @param image           {@link Image}
     * @param imageType       目标图片类型，{@link BufferedImage}中的常量，例如黑白等
     * @param backgroundColor 背景色{@link Color}，{@code null} 表示默认背景色（黑色或者透明）
     * @return {@link BufferedImage}
     */
    public static BufferedImage toBufferedImage(final Image image, final int imageType, final Color backgroundColor) {
        BufferedImage bufferedImage;
        if (image instanceof BufferedImage) {
            bufferedImage = (BufferedImage) image;
            if (imageType != bufferedImage.getType()) {
                bufferedImage = copyImage(image, imageType, backgroundColor);
            }
            return bufferedImage;
        }

        bufferedImage = copyImage(image, imageType, backgroundColor);
        return bufferedImage;
    }

    /**
     * 将已有Image复制新的一份出来
     *
     * @param img       {@link Image}
     * @param imageType 目标图片类型，{@link BufferedImage}中的常量，例如黑白等
     * @return {@link BufferedImage}
     * @see BufferedImage#TYPE_INT_RGB
     * @see BufferedImage#TYPE_INT_ARGB
     * @see BufferedImage#TYPE_INT_ARGB_PRE
     * @see BufferedImage#TYPE_INT_BGR
     * @see BufferedImage#TYPE_3BYTE_BGR
     * @see BufferedImage#TYPE_4BYTE_ABGR
     * @see BufferedImage#TYPE_4BYTE_ABGR_PRE
     * @see BufferedImage#TYPE_BYTE_GRAY
     * @see BufferedImage#TYPE_USHORT_GRAY
     * @see BufferedImage#TYPE_BYTE_BINARY
     * @see BufferedImage#TYPE_BYTE_INDEXED
     * @see BufferedImage#TYPE_USHORT_565_RGB
     * @see BufferedImage#TYPE_USHORT_555_RGB
     */
    public static BufferedImage copyImage(final Image img, final int imageType) {
        return copyImage(img, imageType, null);
    }

    /**
     * 将已有Image复制新的一份出来
     *
     * @param img             {@link Image}
     * @param imageType       目标图片类型，{@link BufferedImage}中的常量，例如黑白等
     * @param backgroundColor 背景色，{@code null} 表示默认背景色（黑色或者透明）
     * @return {@link BufferedImage}
     * @see BufferedImage#TYPE_INT_RGB
     * @see BufferedImage#TYPE_INT_ARGB
     * @see BufferedImage#TYPE_INT_ARGB_PRE
     * @see BufferedImage#TYPE_INT_BGR
     * @see BufferedImage#TYPE_3BYTE_BGR
     * @see BufferedImage#TYPE_4BYTE_ABGR
     * @see BufferedImage#TYPE_4BYTE_ABGR_PRE
     * @see BufferedImage#TYPE_BYTE_GRAY
     * @see BufferedImage#TYPE_USHORT_GRAY
     * @see BufferedImage#TYPE_BYTE_BINARY
     * @see BufferedImage#TYPE_BYTE_INDEXED
     * @see BufferedImage#TYPE_USHORT_565_RGB
     * @see BufferedImage#TYPE_USHORT_555_RGB
     */
    public static BufferedImage copyImage(Image img, final int imageType, final Color backgroundColor) {
        img = new ImageIcon(img).getImage();

        final BufferedImage bImage = new BufferedImage(img.getWidth(null), img.getHeight(null), imageType);

        final Graphics2D bGr = createGraphics(bImage, backgroundColor);
        try {
            drawImg(bGr, img, new Point());
        } finally {
            bGr.dispose();
        }

        return bImage;
    }

    /**
     * 将Base64编码的图像信息转为 {@link BufferedImage}
     *
     * @param base64 图像的Base64表示
     * @return {@link BufferedImage}
     * @throws InternalException IO异常
     */
    public static BufferedImage toImage(final String base64) throws InternalException {
        return toImage(Base64.decode(base64));
    }

    /**
     * 将的图像bytes转为 {@link BufferedImage}
     *
     * @param imageBytes 图像bytes
     * @return {@link BufferedImage}
     * @throws InternalException IO异常
     */
    public static BufferedImage toImage(final byte[] imageBytes) throws InternalException {
        return read(new ByteArrayInputStream(imageBytes));
    }

    /**
     * 将图片对象转换为InputStream形式
     *
     * @param image     图片对象
     * @param imageType 图片类型
     * @return Base64的字符串表现形式
     */
    public static ByteArrayInputStream toStream(final Image image, final String imageType) {
        return IoKit.toStream(toBytes(image, imageType));
    }

    /**
     * 将图片对象转换为Base64的Data URI形式，格式为：data:image/[imageType];base64,[data]
     *
     * @param image     图片对象
     * @param imageType 图片类型
     * @return Base64的字符串表现形式
     */
    public static String toBase64DataUri(final Image image, final String imageType) {
        return UrlKit.getDataUri("image/" + imageType, "base64", toBase64(image, imageType));
    }

    /**
     * 将图片对象转换为Base64形式
     *
     * @param image     图片对象
     * @param imageType 图片类型
     * @return Base64的字符串表现形式
     */
    public static String toBase64(final Image image, final String imageType) {
        return Base64.encode(toBytes(image, imageType));
    }

    /**
     * 将图片对象转换为bytes形式
     *
     * @param image     图片对象
     * @param imageType 图片类型
     * @return Base64的字符串表现形式
     */
    public static byte[] toBytes(final Image image, final String imageType) {
        final FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        write(image, imageType, out);
        return out.toByteArrayZeroCopyIfPossible();
    }

    /**
     * 创建与当前设备颜色模式兼容的 {@link BufferedImage}
     *
     * @param width        宽度
     * @param height       高度
     * @param transparency 透明模式，见 {@link Transparency}
     * @return {@link BufferedImage}
     */
    public static BufferedImage createCompatibleImage(final int width, final int height, final int transparency) {
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice gs = ge.getDefaultScreenDevice();
        final GraphicsConfiguration gc = gs.getDefaultConfiguration();
        return gc.createCompatibleImage(width, height, transparency);
    }

    /**
     * 根据文字创建透明背景的PNG图片
     *
     * @param text      文字
     * @param font      字体{@link Font}
     * @param fontColor 字体颜色，默认黑色
     * @param out       图片输出地
     * @throws InternalException IO异常
     */
    public static void createTransparentImage(final String text, final Font font, final Color fontColor,
            final ImageOutputStream out) throws InternalException {
        BufferedImage image = null;
        try {
            image = createImage(text, font, null, fontColor, BufferedImage.TYPE_INT_ARGB);
            writePng(image, out);
        } finally {
            flush(image);
        }
    }

    /**
     * 根据文字创建PNG图片
     *
     * @param text            文字
     * @param font            字体{@link Font}
     * @param backgroundColor 背景颜色，默认透明
     * @param fontColor       字体颜色，默认黑色
     * @param out             图片输出地
     * @throws InternalException IO异常
     */
    public static void createImage(final String text, final Font font, final Color backgroundColor,
            final Color fontColor, final ImageOutputStream out) throws InternalException {
        BufferedImage image = null;
        try {
            image = createImage(text, font, backgroundColor, fontColor, BufferedImage.TYPE_INT_ARGB);
            writePng(image, out);
        } finally {
            flush(image);
        }
    }

    /**
     * 根据文字创建图片
     *
     * @param text            文字
     * @param font            字体{@link Font}
     * @param backgroundColor 背景颜色，默认透明
     * @param fontColor       字体颜色，默认黑色
     * @param imageType       图片类型，见：{@link BufferedImage}
     * @return 图片
     * @throws InternalException IO异常
     */
    public static BufferedImage createImage(final String text, final Font font, final Color backgroundColor,
            final Color fontColor, final int imageType) throws InternalException {
        // 获取font的样式应用在str上的整个矩形
        final Rectangle2D r = getRectangle(text, font);
        // 获取单个字符的高度
        final int unitHeight = (int) Math.floor(r.getHeight());
        // 获取整个str用了font样式的宽度这里用四舍五入后+1保证宽度绝对能容纳这个字符串作为图片的宽度
        final int width = (int) Math.round(r.getWidth()) + 1;
        // 把单个字符的高度+3保证高度绝对能容纳字符串作为图片的高度
        final int height = unitHeight + 3;

        // 创建图片
        final BufferedImage image = new BufferedImage(width, height, imageType);
        final Graphics g = createGraphics(image, backgroundColor);
        drawString(g, text, font, fontColor, new Point(0, font.getSize()));
        g.dispose();

        return image;
    }

    /**
     * 写出图像为JPG格式
     *
     * @param image             {@link Image}
     * @param targetImageStream 写出到的目标流
     * @throws InternalException IO异常
     */
    public static void writeJpg(final Image image, final ImageOutputStream targetImageStream) throws InternalException {
        write(image, IMAGE_TYPE_JPG, targetImageStream);
    }

    /**
     * 写出图像为PNG格式
     *
     * @param image             {@link Image}，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param targetImageStream 写出到的目标流
     * @throws InternalException IO异常
     */
    public static void writePng(final Image image, final ImageOutputStream targetImageStream) throws InternalException {
        write(image, IMAGE_TYPE_PNG, targetImageStream);
    }

    /**
     * 写出图像为JPG格式
     *
     * @param image {@link Image}，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param out   写出到的目标流
     * @throws InternalException IO异常
     */
    public static void writeJpg(final Image image, final OutputStream out) throws InternalException {
        write(image, IMAGE_TYPE_JPG, out);
    }

    /**
     * 写出图像为PNG格式
     *
     * @param image {@link Image}，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param out   写出到的目标流
     * @throws InternalException IO异常
     */
    public static void writePng(final Image image, final OutputStream out) throws InternalException {
        write(image, IMAGE_TYPE_PNG, out);
    }

    /**
     * 按照目标格式写出图像：GIF=JPG、GIF=PNG、PNG=JPG、PNG=GIF(X)、BMP=PNG 此方法并不关闭流
     *
     * @param srcStream    源图像流
     * @param formatName   包含格式非正式名称的 String：如JPG、JPEG、GIF等
     * @param targetStream 目标图像输出流
     */
    public static void write(final ImageInputStream srcStream, final String formatName,
            final ImageOutputStream targetStream) {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            write(image, formatName, targetStream);
        } finally {
            flush(image);
        }
    }

    /**
     * 写出图像：GIF=JPG、GIF=PNG、PNG=JPG、PNG=GIF(X)、BMP=PNG 此方法并不关闭流
     *
     * @param image     {@link Image}，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param imageType 图片类型（图片扩展名）
     * @param out       写出到的目标流
     * @throws InternalException IO异常
     */
    public static void write(final Image image, final String imageType, final OutputStream out)
            throws InternalException {
        write(image, imageType, getImageOutputStream(out));
    }

    /**
     * 写出图像为目标文件扩展名对应的格式
     *
     * @param image      {@link Image}，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param targetFile 目标文件
     * @throws InternalException IO异常
     */
    public static void write(final Image image, final File targetFile) throws InternalException {
        ImageWriter.of(image, FileName.extName(targetFile)).write(targetFile);
    }

    /**
     * 写出图像为指定格式：GIF=JPG、GIF=PNG、PNG=JPG、PNG=GIF(X)、BMP=PNG 此方法并不关闭流
     *
     * @param image             {@link Image}，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param imageType         图片类型（图片扩展名）
     * @param targetImageStream 写出到的目标流
     * @throws InternalException IO异常
     */
    public static void write(final Image image, final String imageType, final ImageOutputStream targetImageStream)
            throws InternalException {
        write(image, imageType, targetImageStream, 1);
    }

    /**
     * 写出图像为指定格式
     *
     * @param image           {@link Image}，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param imageType       图片类型（图片扩展名），{@code null}表示使用RGB模式（JPG）
     * @param out             写出到的目标流
     * @param quality         质量，数字为0~1（不包括0和1）表示质量压缩比，除此数字外设置表示不压缩
     * @param backgroundColor 背景色{@link Color}
     * @throws InternalException IO异常
     */
    public static void write(final Image image, final String imageType, final ImageOutputStream out,
            final float quality, final Color backgroundColor) throws InternalException {
        final BufferedImage bufferedImage = toBufferedImage(image, imageType, backgroundColor);
        write(bufferedImage, imageType, out, quality);
    }

    /**
     * 通过{@link javax.imageio.ImageWriter}写出图片到输出流
     *
     * @param image     图片，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param imageType 图片类型
     * @param output    输出的Image流{@link ImageOutputStream}
     * @param quality   质量，数字为0~1（不包括0和1）表示质量压缩比，除此数字外设置表示不压缩
     */
    public static void write(final Image image, final String imageType, final ImageOutputStream output,
            final float quality) {
        ImageWriter.of(image, imageType).setQuality(quality).write(output);
    }

    /**
     * 根据给定的Image对象和格式获取对应的{@link javax.imageio.ImageWriter}，如果未找到合适的Writer，返回null
     *
     * @param img        {@link Image}，使用结束后需手动调用{@link #flush(Image)}释放资源
     * @param formatName 图片格式，例如"jpg"、"png"，{@code null}则使用默认值"jpg"
     * @return {@link javax.imageio.ImageWriter}
     */
    public static javax.imageio.ImageWriter getWriter(final Image img, String formatName) {
        if (null == formatName) {
            formatName = IMAGE_TYPE_JPG;
        }
        final ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(toBufferedImage(img, formatName));
        final Iterator<javax.imageio.ImageWriter> iter = ImageIO.getImageWriters(type, formatName);
        return iter.hasNext() ? iter.next() : null;
    }

    /**
     * 根据给定的图片格式或者扩展名获取{@link javax.imageio.ImageWriter}，如果未找到合适的Writer，返回null
     *
     * @param formatName 图片格式或扩展名，例如"jpg"、"png"，{@code null}则使用默认值"jpg"
     * @return {@link javax.imageio.ImageWriter}
     */
    public static javax.imageio.ImageWriter getWriter(String formatName) {
        if (null == formatName) {
            formatName = IMAGE_TYPE_JPG;
        }

        javax.imageio.ImageWriter writer = null;
        Iterator<javax.imageio.ImageWriter> iter = ImageIO.getImageWritersByFormatName(formatName);
        if (iter.hasNext()) {
            writer = iter.next();
        }
        if (null == writer) {
            // 尝试扩展名获取
            iter = ImageIO.getImageWritersBySuffix(formatName);
            if (iter.hasNext()) {
                writer = iter.next();
            }
        }
        return writer;
    }

    /**
     * 从文件中读取图片，请使用绝对路径，使用相对路径会相对于ClassPath
     *
     * @param imageFilePath 图片文件路径
     * @return 图片
     */
    public static BufferedImage read(final String imageFilePath) {
        return read(FileKit.file(imageFilePath));
    }

    /**
     * 从文件中读取图片
     *
     * @param imageFile 图片文件
     * @return 图片
     */
    public static BufferedImage read(final File imageFile) {
        final BufferedImage result;
        try {
            result = ImageIO.read(imageFile);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        if (null == result) {
            throw new IllegalArgumentException("Image type of file [" + imageFile.getName() + "] is not supported!");
        }

        return result;
    }

    /**
     * 从{@link Resource}中读取图片
     *
     * @param resource 图片资源
     * @return 图片
     */
    public static BufferedImage read(final Resource resource) {
        return read(resource.getStream());
    }

    /**
     * 从流中读取图片
     *
     * @param imageStream 图片文件
     * @return 图片
     */
    public static BufferedImage read(final InputStream imageStream) {
        final BufferedImage result;
        try {
            result = ImageIO.read(imageStream);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        if (null == result) {
            throw new IllegalArgumentException("Image type is not supported!");
        }

        return result;
    }

    /**
     * 从图片流中读取图片
     *
     * @param imageStream 图片文件
     * @return 图片
     */
    public static BufferedImage read(final ImageInputStream imageStream) {
        final BufferedImage result;
        try {
            result = ImageIO.read(imageStream);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        if (null == result) {
            throw new IllegalArgumentException("Image type is not supported!");
        }

        return result;
    }

    /**
     * 从URL中读取图片
     *
     * @param imageUrl 图片文件
     * @return 图片
     */
    public static BufferedImage read(final URL imageUrl) {
        final BufferedImage result;
        try {
            result = ImageIO.read(imageUrl);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        if (null == result) {
            throw new IllegalArgumentException("Image type of [" + imageUrl + "] is not supported!");
        }

        return result;
    }

    /**
     * 获得{@link ImageReader}
     *
     * @param type 图片文件类型，例如 "jpeg" 或 "tiff"
     * @return {@link ImageReader}
     */
    public static ImageReader getReader(final String type) {
        final Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName(type);
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    /**
     * 通过 {@link ImageInputStream} 获取对应类型的宽和高
     *
     * @param imageStream {@link ImageInputStream}
     * @param type        图片类型
     * @return 宽和高
     */
    public static Pair<Integer, Integer> getWidthAndHeight(final InputStream imageStream, final String type) {
        return getWidthAndHeight(getImageInputStream(imageStream), type);
    }

    /**
     * 通过 {@link ImageInputStream} 获取对应类型的宽和高
     *
     * @param imageStream {@link ImageInputStream}
     * @param type        图片类型
     * @return 宽和高
     */
    public static Pair<Integer, Integer> getWidthAndHeight(final ImageInputStream imageStream, final String type) {
        final ImageReader reader = getReader(type);
        if (null != reader) {
            reader.setInput(imageStream, true);
            try {
                return Pair.of(reader.getWidth(0), reader.getHeight(0));
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }
        return null;
    }

    /**
     * 从URL中获取或读取图片对象
     *
     * @param url URL
     * @return {@link Image}
     */
    public static Image getImage(final URL url) {
        return Toolkit.getDefaultToolkit().getImage(url);
    }

    /**
     * 获取{@link ImageOutputStream}
     *
     * @param out {@link OutputStream}
     * @return {@link ImageOutputStream}
     * @throws InternalException IO异常
     */
    public static ImageOutputStream getImageOutputStream(final OutputStream out) throws InternalException {
        final ImageOutputStream result;
        try {
            result = ImageIO.createImageOutputStream(out);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        if (null == result) {
            throw new IllegalArgumentException("Image type is not supported!");
        }

        return result;
    }

    /**
     * 获取{@link ImageOutputStream}
     *
     * @param outFile {@link File}
     * @return {@link ImageOutputStream}
     * @throws InternalException IO异常
     */
    public static ImageOutputStream getImageOutputStream(final File outFile) throws InternalException {
        final ImageOutputStream result;
        try {
            result = ImageIO.createImageOutputStream(outFile);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        if (null == result) {
            throw new IllegalArgumentException("Image type of file [" + outFile.getName() + "] is not supported!");
        }

        return result;
    }

    /**
     * 获取{@link ImageInputStream}
     *
     * @param in {@link InputStream}
     * @return {@link ImageInputStream}
     * @throws InternalException IO异常
     */
    public static ImageInputStream getImageInputStream(final InputStream in) throws InternalException {
        final ImageOutputStream result;
        try {
            result = ImageIO.createImageOutputStream(in);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        if (null == result) {
            throw new IllegalArgumentException("Image type is not supported!");
        }

        return result;
    }

    /**
     * 获得修正后的矩形坐标位置，变为以背景中心为基准坐标（即x,y == 0,0时，处于背景正中）
     *
     * @param rectangle        矩形
     * @param backgroundWidth  参考宽（背景宽）
     * @param backgroundHeight 参考高（背景高）
     * @return 修正后的{@link Point}
     */
    public static Point getPointBaseCentre(final Rectangle rectangle, final int backgroundWidth,
            final int backgroundHeight) {
        return new Point(rectangle.x + (Math.abs(backgroundWidth - rectangle.width) / 2), //
                rectangle.y + (Math.abs(backgroundHeight - rectangle.height) / 2)//
        );
    }

    /**
     * 背景移除 图片去底工具 将 "纯色背景的图片" 还原成 "透明背景的图片" 将纯色背景的图片转成矢量图 取图片边缘的像素点和获取到的图片主题色作为要替换的背景色 再加入一定的容差值,然后将所有像素点与该颜色进行比较
     * 发现相同则将颜色不透明度设置为0,使颜色完全透明.
     *
     * @param inputPath  要处理图片的路径
     * @param outputPath 输出图片的路径
     * @param tolerance  容差值[根据图片的主题色,加入容差值,值的范围在0~255之间]
     */
    public static void backgroundRemoval(final String inputPath, final String outputPath, final int tolerance) {
        ImageRemoval.backgroundRemoval(inputPath, outputPath, tolerance);
    }

    /**
     * 背景移除 图片去底工具 将 "纯色背景的图片" 还原成 "透明背景的图片" 将纯色背景的图片转成矢量图 取图片边缘的像素点和获取到的图片主题色作为要替换的背景色 再加入一定的容差值,然后将所有像素点与该颜色进行比较
     * 发现相同则将颜色不透明度设置为0,使颜色完全透明.
     *
     * @param input     需要进行操作的图片
     * @param output    最后输出的文件
     * @param tolerance 容差值[根据图片的主题色,加入容差值,值的取值范围在0~255之间]
     */
    public static void backgroundRemoval(final File input, final File output, final int tolerance) {
        ImageRemoval.backgroundRemoval(input, output, tolerance);
    }

    /**
     * 背景移除 图片去底工具 将 "纯色背景的图片" 还原成 "透明背景的图片" 将纯色背景的图片转成矢量图 取图片边缘的像素点和获取到的图片主题色作为要替换的背景色 再加入一定的容差值,然后将所有像素点与该颜色进行比较
     * 发现相同则将颜色不透明度设置为0,使颜色完全透明.
     *
     * @param input     需要进行操作的图片
     * @param output    最后输出的文件
     * @param override  指定替换成的背景颜色 为null时背景为透明
     * @param tolerance 容差值[根据图片的主题色,加入容差值,值的取值范围在0~255之间]
     */
    public static void backgroundRemoval(final File input, final File output, final Color override,
            final int tolerance) {
        ImageRemoval.backgroundRemoval(input, output, override, tolerance);
    }

    /**
     * 背景移除 图片去底工具 将 "纯色背景的图片" 还原成 "透明背景的图片" 将纯色背景的图片转成矢量图 取图片边缘的像素点和获取到的图片主题色作为要替换的背景色 再加入一定的容差值,然后将所有像素点与该颜色进行比较
     * 发现相同则将颜色不透明度设置为0,使颜色完全透明.
     *
     * @param bufferedImage 需要进行处理的图片流
     * @param override      指定替换成的背景颜色 为null时背景为透明
     * @param tolerance     容差值[根据图片的主题色,加入容差值,值的取值范围在0~255之间]
     * @return 返回处理好的图片流
     */
    public static BufferedImage backgroundRemoval(final BufferedImage bufferedImage, final Color override,
            final int tolerance) {
        return ImageRemoval.backgroundRemoval(bufferedImage, override, tolerance);
    }

    /**
     * 背景移除 图片去底工具 将 "纯色背景的图片" 还原成 "透明背景的图片" 将纯色背景的图片转成矢量图 取图片边缘的像素点和获取到的图片主题色作为要替换的背景色 再加入一定的容差值,然后将所有像素点与该颜色进行比较
     * 发现相同则将颜色不透明度设置为0,使颜色完全透明.
     *
     * @param outputStream 需要进行处理的图片字节数组流
     * @param override     指定替换成的背景颜色 为null时背景为透明
     * @param tolerance    容差值[根据图片的主题色,加入容差值,值的取值范围在0~255之间]
     * @return 返回处理好的图片流
     */
    public static BufferedImage backgroundRemoval(final ByteArrayOutputStream outputStream, final Color override,
            final int tolerance) {
        return ImageRemoval.backgroundRemoval(outputStream, override, tolerance);
    }

    /**
     * 图片颜色转换 可以使用灰度 (gray)等
     *
     * @param colorSpace 颜色模式，如灰度等
     * @param image      被转换的图片
     * @return 转换后的图片
     */
    public static BufferedImage colorConvert(final ColorSpace colorSpace, final BufferedImage image) {
        return filter(new ColorConvertOp(colorSpace, null), image);
    }

    /**
     * 转换图片 可以使用一系列平移 (translation)、缩放 (scale)、翻转 (flip)、旋转 (rotation) 和错切 (shear) 来构造仿射变换。
     *
     * @param xform 2D仿射变换，它执行从 2D 坐标到其他 2D 坐标的线性映射，保留了线的“直线性”和“平行性”。
     * @param image 被转换的图片
     * @return 转换后的图片
     */
    public static BufferedImage transform(final AffineTransform xform, final BufferedImage image) {
        return filter(new AffineTransformOp(xform, null), image);
    }

    /**
     * 图片过滤转换
     *
     * @param op    过滤操作实现，如二维转换可传入{@link AffineTransformOp}
     * @param image 原始图片
     * @return 过滤后的图片
     */
    public static BufferedImage filter(final BufferedImageOp op, final BufferedImage image) {
        return op.filter(image, null);
    }

    /**
     * 图片滤镜，借助 {@link ImageFilter}实现，实现不同的图片滤镜
     *
     * @param filter 滤镜实现
     * @param image  图片
     * @return 滤镜后的图片
     */
    public static Image filter(final ImageFilter filter, final Image image) {
        return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), filter));
    }

    /**
     * 刷新和释放{@link Image} 资源
     *
     * @param image {@link Image}
     */
    public static void flush(final Image image) {
        if (null != image) {
            image.flush();
        }
    }

    /**
     * 创建{@link Graphics2D}
     *
     * @param image {@link BufferedImage}
     * @param color {@link Color}背景颜色以及当前画笔颜色，{@code null}表示不设置背景色
     * @return {@link Graphics2D}
     */
    public static Graphics2D createGraphics(final BufferedImage image, final Color color) {
        final Graphics2D g = image.createGraphics();
        if (null != color) {
            // 填充背景
            g.setColor(color);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
        }

        return g;
    }

    /**
     * 获取文字居中高度的Y坐标（距离上边距距离） 此方法依赖FontMetrics，如果获取失败，默认为背景高度的1/3
     *
     * @param g                {@link Graphics2D}画笔
     * @param backgroundHeight 背景高度
     * @return 最小高度，-1表示无法获取
     */
    public static int getCenterY(final Graphics g, final int backgroundHeight) {
        // 获取允许文字最小高度
        FontMetrics metrics = null;
        try {
            metrics = g.getFontMetrics();
        } catch (final Exception e) {
            // 此处报告bug某些情况下会抛出IndexOutOfBoundsException，在此做容错处理
        }
        final int y;
        if (null != metrics) {
            y = (backgroundHeight - metrics.getHeight()) / 2 + metrics.getAscent();
        } else {
            y = backgroundHeight / 3;
        }
        return y;
    }

    /**
     * 绘制字符串，使用随机颜色，默认抗锯齿
     *
     * @param g      {@link Graphics}画笔
     * @param text   字符串
     * @param font   字体
     * @param width  字符串总宽度
     * @param height 字符串背景高度
     * @return 画笔对象
     */
    public static Graphics drawStringColourful(final Graphics g, final String text, final Font font, final int width,
            final int height) {
        return drawString(g, text, font, null, width, height);
    }

    /**
     * 绘制字符串，默认抗锯齿
     *
     * @param g      {@link Graphics}画笔
     * @param text   字符串
     * @param font   字体
     * @param color  字体颜色，{@code null} 表示使用随机颜色（每个字符单独随机）
     * @param width  字符串背景的宽度
     * @param height 字符串背景的高度
     * @return 画笔对象
     */
    public static Graphics drawString(final Graphics g, final String text, final Font font, final Color color,
            final int width, final int height) {
        // 抗锯齿
        enableAntialias(g);
        // 创建字体
        g.setFont(font);

        // 文字高度（必须在设置字体后调用）
        final int midY = getCenterY(g, height);
        if (null != color) {
            g.setColor(color);
        }

        final int len = text.length();
        final int charWidth = width / len;
        for (int i = 0; i < len; i++) {
            if (null == color) {
                // 产生随机的颜色值，让输出的每个字符的颜色值都将不同。
                g.setColor(ColorKit.randomColor());
            }
            g.drawString(String.valueOf(text.charAt(i)), i * charWidth, midY);
        }
        return g;
    }

    /**
     * 绘制字符串，默认抗锯齿。 此方法定义一个矩形区域和坐标，文字基于这个区域中间偏移x,y绘制。
     *
     * @param g         {@link Graphics}画笔
     * @param text      字符串
     * @param font      字体，字体大小决定了在背景中绘制的大小
     * @param color     字体颜色，{@code null} 表示使用黑色
     * @param rectangle 字符串绘制坐标和大小，此对象定义了绘制字符串的区域大小和偏移位置
     * @return 画笔对象
     */
    public static Graphics drawString(final Graphics g, final String text, final Font font, final Color color,
            final Rectangle rectangle) {
        // 背景长宽
        final int backgroundWidth = rectangle.width;
        final int backgroundHeight = rectangle.height;

        // 获取字符串本身的长宽
        Dimension dimension;
        try {
            dimension = getDimension(g.getFontMetrics(font), text);
        } catch (final Exception e) {
            // 此处报告bug某些情况下会抛出IndexOutOfBoundsException，在此做容错处理
            dimension = new Dimension(backgroundWidth / 3, backgroundHeight / 3);
        }

        rectangle.setSize(dimension.width, dimension.height);
        final Point point = getPointBaseCentre(rectangle, backgroundWidth, backgroundHeight);

        return drawString(g, text, font, color, point);
    }

    /**
     * 绘制字符串，默认抗锯齿
     *
     * @param g     {@link Graphics}画笔
     * @param text  字符串
     * @param font  字体，字体大小决定了在背景中绘制的大小
     * @param color 字体颜色，{@code null} 表示使用黑色
     * @param point 绘制字符串的位置坐标
     * @return 画笔对象
     */
    public static Graphics drawString(final Graphics g, final String text, final Font font, final Color color,
            final Point point) {
        // 抗锯齿
        enableAntialias(g);

        g.setFont(font);
        g.setColor(ObjectKit.defaultIfNull(color, Color.BLACK));
        g.drawString(text, point.x, point.y);

        return g;
    }

    /**
     * 绘制图片
     *
     * @param g     画笔
     * @param img   要绘制的图片
     * @param point 绘制的位置，基于左上角
     * @return 画笔对象
     */
    public static Graphics drawImg(final Graphics g, final Image img, final Point point) {
        g.drawImage(img, point.x, point.y, null);
        return g;
    }

    /**
     * 绘制图片
     *
     * @param g         画笔
     * @param img       要绘制的图片
     * @param rectangle 矩形对象，表示矩形区域的x，y，width，height,，基于左上角
     * @return 画笔对象
     */
    public static Graphics drawImg(final Graphics g, final Image img, final Rectangle rectangle) {
        g.drawImage(img, rectangle.x, rectangle.y, rectangle.width, rectangle.height, null); // 绘制切割后的图
        return g;
    }

    /**
     * 设置画笔透明度
     *
     * @param g     画笔
     * @param alpha 透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 画笔
     */
    public static Graphics2D setAlpha(final Graphics2D g, final float alpha) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        return g;
    }

    /**
     * 打开抗锯齿和文本抗锯齿
     *
     * @param g {@link Graphics}
     */
    private static void enableAntialias(final Graphics g) {
        if (g instanceof Graphics2D) {
            ((Graphics2D) g)
                    .setRenderingHints(RenderingHintsBuilder.of().setAntialiasing(RenderingHintsBuilder.Antialias.ON)
                            .setTextAntialias(RenderingHintsBuilder.TextAntialias.ON).build());
        }
    }

    /**
     * 创建默认字体
     *
     * @return 默认字体
     */
    public static Font createFont() {
        return new Font(null);
    }

    /**
     * 创建SansSerif字体
     *
     * @param size 字体大小
     * @return 字体
     */
    public static Font createSansSerifFont(final int size) {
        return createFont(Font.SANS_SERIF, size);
    }

    /**
     * 创建指定名称的字体
     *
     * @param name 字体名称
     * @param size 字体大小
     * @return 字体
     */
    public static Font createFont(final String name, final int size) {
        return new Font(name, Font.PLAIN, size);
    }

    /**
     * 根据文件创建字体 首先尝试创建{@link Font#TRUETYPE_FONT}字体，此类字体无效则创建{@link Font#TYPE1_FONT}
     *
     * @param fontFile 字体文件
     * @return {@link Font}
     */
    public static Font createFont(final File fontFile) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, fontFile);
        } catch (final FontFormatException e) {
            // True Type字体无效时使用Type1字体
            try {
                return Font.createFont(Font.TYPE1_FONT, fontFile);
            } catch (final Exception e1) {
                throw ExceptionKit.wrapRuntime(e);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 根据文件创建字体 首先尝试创建{@link Font#TRUETYPE_FONT}字体，此类字体无效则创建{@link Font#TYPE1_FONT}
     *
     * @param fontStream 字体流
     * @return {@link Font}
     */
    public static Font createFont(final InputStream fontStream) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, fontStream);
        } catch (final FontFormatException e) {
            // True Type字体无效时使用Type1字体
            try {
                return Font.createFont(Font.TYPE1_FONT, fontStream);
            } catch (final Exception e1) {
                throw ExceptionKit.wrapRuntime(e1);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获得字体对应字符串的长宽信息
     *
     * @param metrics {@link FontMetrics}
     * @param text    字符串
     * @return 长宽信息
     */
    public static Dimension getDimension(final FontMetrics metrics, final String text) {
        final int width = metrics.stringWidth(text);
        final int height = metrics.getAscent() - metrics.getLeading() - metrics.getDescent();
        return new Dimension(width, height);
    }

    /**
     * 获取font的样式应用在str上的整个矩形
     *
     * @param text 字符串，必须非空
     * @param font 字体，必须非空
     * @return {@link Rectangle2D}
     */
    public static Rectangle2D getRectangle(final String text, final Font font) {
        return font.getStringBounds(text, new FontRenderContext(AffineTransform.getScaleInstance(1, 1), false, false));
    }
}
