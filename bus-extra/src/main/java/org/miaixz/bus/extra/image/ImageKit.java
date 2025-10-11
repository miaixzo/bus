/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.io.stream.FastByteArrayOutputStream;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.xyz.*;

/**
 * Image processing utility class.
 * <p>
 * Features include: scaling, cropping, rotating, converting image types, converting to grayscale, adding text
 * watermarks, adding image watermarks, etc. Reference: <a href=
 * "http://blog.csdn.net/zhangzhikaixinya/article/details/8459400">http://blog.csdn.net/zhangzhikaixinya/article/details/8459400</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageKit {

    /**
     * Graphics Interchange Format: GIF
     */
    public static final String IMAGE_TYPE_GIF = "gif";
    /**
     * Joint Photographic Experts Group: JPG
     */
    public static final String IMAGE_TYPE_JPG = "jpg";
    /**
     * Joint Photographic Experts Group: JPEG
     */
    public static final String IMAGE_TYPE_JPEG = "jpeg";
    /**
     * Bitmap, the standard image file format in Windows OS: BMP
     */
    public static final String IMAGE_TYPE_BMP = "bmp";
    /**
     * Portable Network Graphics: PNG
     */
    public static final String IMAGE_TYPE_PNG = "png";
    /**
     * Photoshop's proprietary format: PSD
     */
    public static final String IMAGE_TYPE_PSD = "psd";

    /**
     * Scales an image by a given ratio. The target file type is determined by the extension of the destination file.
     *
     * @param srcImageFile  The source image file.
     * @param destImageFile The destination image file for the scaled image. Its extension determines the output format.
     * @param scale         The scaling ratio. A ratio greater than 1 enlarges the image, while a ratio between 0 and 1
     *                      shrinks it.
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
     * Scales an image by a given ratio. The default output format is JPEG. This method does not close the streams.
     *
     * @param srcStream    The input stream of the source image.
     * @param targetStream The output stream for the scaled image.
     * @param scale        The scaling ratio. A ratio greater than 1 enlarges the image, while a ratio between 0 and 1
     *                     shrinks it.
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
     * Scales an image by a given ratio. The default output format is JPEG. This method does not close the streams.
     *
     * @param srcStream    The source {@link ImageInputStream}.
     * @param targetStream The destination {@link ImageOutputStream}.
     * @param scale        The scaling ratio. A ratio greater than 1 enlarges the image, while a ratio between 0 and 1
     *                     shrinks it.
     */
    public static void scale(
            final ImageInputStream srcStream,
            final ImageOutputStream targetStream,
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
     * Scales an image by a given ratio. The default output format is JPEG. Remember to manually call
     * {@link #flush(Image)} to release resources.
     *
     * @param srcImg   The source image.
     * @param destFile The destination file for the scaled image.
     * @param scale    The scaling ratio. A ratio greater than 1 enlarges the image, while a ratio between 0 and 1
     *                 shrinks it.
     * @throws InternalException if an I/O error occurs.
     */
    public static void scale(final Image srcImg, final File destFile, final float scale) throws InternalException {
        Images.from(srcImg).setTargetImageType(FileName.extName(destFile)).scale(scale).write(destFile);
    }

    /**
     * Scales an image by a given ratio. The default output format is JPEG. This method does not close the output
     * stream. Remember to manually call {@link #flush(Image)} to release resources.
     *
     * @param srcImg The source image.
     * @param out    The output stream for the scaled image.
     * @param scale  The scaling ratio. A ratio greater than 1 enlarges the image, while a ratio between 0 and 1 shrinks
     *               it.
     * @throws InternalException if an I/O error occurs.
     */
    public static void scale(final Image srcImg, final OutputStream out, final float scale) throws InternalException {
        scale(srcImg, getImageOutputStream(out), scale);
    }

    /**
     * Scales an image by a given ratio. The default output format is JPEG. This method does not close the output
     * stream. Remember to manually call {@link #flush(Image)} to release resources.
     *
     * @param srcImg          The source image.
     * @param destImageStream The destination {@link ImageOutputStream}.
     * @param scale           The scaling ratio. A ratio greater than 1 enlarges the image, while a ratio between 0 and
     *                        1 shrinks it.
     * @throws InternalException if an I/O error occurs.
     */
    public static void scale(final Image srcImg, final ImageOutputStream destImageStream, final float scale)
            throws InternalException {
        writeJpg(scale(srcImg, scale), destImageStream);
    }

    /**
     * Scales an image by a given ratio. Remember to manually call {@link #flush(Image)} to release resources.
     *
     * @param srcImg The source image.
     * @param scale  The scaling ratio. A ratio greater than 1 enlarges the image, while a ratio between 0 and 1 shrinks
     *               it.
     * @return The scaled {@link Image}.
     */
    public static Image scale(final Image srcImg, final float scale) {
        return Images.from(srcImg).scale(scale).getImg();
    }

    /**
     * Scales an image to a specific width and height. Note: This may distort the image if the aspect ratio is not
     * maintained. Remember to manually call {@link #flush(Image)} to release resources.
     *
     * @param srcImg The source image.
     * @param width  The target width.
     * @param height The target height.
     * @return The scaled {@link Image}.
     */
    public static Image scale(final Image srcImg, final int width, final int height) {
        return Images.from(srcImg).scale(width, height).getImg();
    }

    /**
     * Scales an image to a specific width and height. The output format is determined by the destination file's
     * extension.
     *
     * @param srcImageFile    The source image file.
     * @param targetImageFile The destination file for the scaled image.
     * @param width           The target width.
     * @param height          The target height.
     * @param fixedColor      The color to fill the background if the aspect ratio changes, or {@code null} for no fill.
     * @throws InternalException if an I/O error occurs.
     */
    public static void scale(
            final File srcImageFile,
            final File targetImageFile,
            final int width,
            final int height,
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
     * Scales an image to a specific width and height. The default output format is JPEG. This method does not close the
     * streams.
     *
     * @param srcStream    The input stream of the source image.
     * @param targetStream The output stream for the scaled image.
     * @param width        The target width.
     * @param height       The target height.
     * @param fixedColor   The color to fill the background if the aspect ratio changes, or {@code null} for no fill.
     * @throws InternalException if an I/O error occurs.
     */
    public static void scale(
            final InputStream srcStream,
            final OutputStream targetStream,
            final int width,
            final int height,
            final Color fixedColor) throws InternalException {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            scale(image, getImageOutputStream(targetStream), width, height, fixedColor);
        } finally {
            flush(image);
        }
    }

    /**
     * Scales an image to a specific width and height. The default output format is JPEG. This method does not close the
     * streams.
     *
     * @param srcStream    The source {@link ImageInputStream}.
     * @param targetStream The destination {@link ImageOutputStream}.
     * @param width        The target width.
     * @param height       The target height.
     * @param fixedColor   The color to fill the background if the aspect ratio changes, or {@code null} for no fill.
     * @throws InternalException if an I/O error occurs.
     */
    public static void scale(
            final ImageInputStream srcStream,
            final ImageOutputStream targetStream,
            final int width,
            final int height,
            final Color fixedColor) throws InternalException {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            scale(image, targetStream, width, height, fixedColor);
        } finally {
            flush(image);
        }
    }

    /**
     * Scales an image to a specific width and height. The default output format is JPEG. This method does not close the
     * output stream. Remember to manually call {@link #flush(Image)} to release resources.
     *
     * @param srcImage          The source image.
     * @param targetImageStream The destination {@link ImageOutputStream}.
     * @param width             The target width.
     * @param height            The target height.
     * @param fixedColor        The color to fill the background if the aspect ratio changes, or {@code null} for no
     *                          fill.
     * @throws InternalException if an I/O error occurs.
     */
    public static void scale(
            final Image srcImage,
            final ImageOutputStream targetImageStream,
            final int width,
            final int height,
            final Color fixedColor) throws InternalException {
        writeJpg(scale(srcImage, width, height, fixedColor), targetImageStream);
    }

    /**
     * Scales an image to a specific width and height. The default output format is JPEG. Remember to manually call
     * {@link #flush(Image)} to release resources.
     *
     * @param srcImage   The source image.
     * @param width      The target width.
     * @param height     The target height.
     * @param fixedColor The color to fill the background if the aspect ratio changes, or {@code null} for no fill.
     * @return The scaled {@link Image}.
     */
    public static Image scale(final Image srcImage, final int width, final int height, final Color fixedColor) {
        return Images.from(srcImage).scale(width, height, fixedColor).getImg();
    }

    /**
     * Crops an image using the specified coordinates and dimensions.
     *
     * @param srcImgFile    The source image file.
     * @param targetImgFile The destination file for the cropped image.
     * @param rectangle     A {@link Rectangle} object specifying the x, y, width, and height of the crop area.
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
     * Crops an image using the specified coordinates and dimensions. This method does not close the streams.
     *
     * @param srcStream    The input stream of the source image.
     * @param targetStream The output stream for the cropped image.
     * @param rectangle    A {@link Rectangle} object specifying the x, y, width, and height of the crop area.
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
     * Crops an image using the specified coordinates and dimensions. This method does not close the streams.
     *
     * @param srcStream    The source {@link ImageInputStream}.
     * @param targetStream The destination {@link ImageOutputStream}.
     * @param rectangle    A {@link Rectangle} object specifying the x, y, width, and height of the crop area.
     */
    public static void cut(
            final ImageInputStream srcStream,
            final ImageOutputStream targetStream,
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
     * Crops an image using the specified coordinates and dimensions. This method does not close the output stream.
     * Remember to manually call {@link #flush(Image)} to release resources.
     *
     * @param srcImage  The source image.
     * @param destFile  The destination file for the cropped image.
     * @param rectangle A {@link Rectangle} object specifying the x, y, width, and height of the crop area.
     * @throws InternalException if an I/O error occurs.
     */
    public static void cut(final Image srcImage, final File destFile, final Rectangle rectangle)
            throws InternalException {
        write(cut(srcImage, rectangle), destFile);
    }

    /**
     * Crops an image using the specified coordinates and dimensions. This method does not close the output stream.
     *
     * @param srcImage  The source image.
     * @param out       The output stream for the cropped image.
     * @param rectangle A {@link Rectangle} object specifying the x, y, width, and height of the crop area.
     * @throws InternalException if an I/O error occurs.
     */
    public static void cut(final Image srcImage, final OutputStream out, final Rectangle rectangle)
            throws InternalException {
        cut(srcImage, getImageOutputStream(out), rectangle);
    }

    /**
     * Crops an image using the specified coordinates and dimensions. This method does not close the output stream.
     * Remember to manually call {@link #flush(Image)} to release resources.
     *
     * @param srcImage          The source image.
     * @param targetImageStream The destination {@link ImageOutputStream}.
     * @param rectangle         A {@link Rectangle} object specifying the x, y, width, and height of the crop area.
     * @throws InternalException if an I/O error occurs.
     */
    public static void cut(final Image srcImage, final ImageOutputStream targetImageStream, final Rectangle rectangle)
            throws InternalException {
        writeJpg(cut(srcImage, rectangle), targetImageStream);
    }

    /**
     * Crops an image using the specified coordinates and dimensions. Remember to manually call {@link #flush(Image)} to
     * release resources.
     *
     * @param srcImage  The source image.
     * @param rectangle A {@link Rectangle} object specifying the x, y, width, and height of the crop area.
     * @return The cropped {@link BufferedImage}.
     */
    public static Image cut(final Image srcImage, final Rectangle rectangle) {
        return Images.from(srcImage).setPositionBaseCentre(false).cut(rectangle).getImg();
    }

    /**
     * Crops a circular area from the image. The diameter is the smaller of the image's width and height. Remember to
     * manually call {@link #flush(Image)} to release resources.
     *
     * @param srcImage The source image.
     * @param x        The x-coordinate of the top-left corner of the crop area.
     * @param y        The y-coordinate of the top-left corner of the crop area.
     * @return The cropped {@link Image}.
     */
    public static Image cut(final Image srcImage, final int x, final int y) {
        return cut(srcImage, x, y, -1);
    }

    /**
     * Crops a circular area from the image. Remember to manually call {@link #flush(Image)} to release resources.
     *
     * @param srcImage The source image.
     * @param x        The x-coordinate of the center of the circle.
     * @param y        The y-coordinate of the center of the circle.
     * @param radius   The radius of the circle. If less than 0, the diameter will be the smaller of the image's width
     *                 and height.
     * @return The cropped {@link Image}.
     */
    public static Image cut(final Image srcImage, final int x, final int y, final int radius) {
        return Images.from(srcImage).cut(x, y, radius).getImg();
    }

    /**
     * Slices an image into smaller pieces of a specified width and height.
     *
     * @param srcImageFile The source image file.
     * @param descDir      The destination directory for the sliced images.
     * @param targetWidth  The width of each slice (default is 200).
     * @param targetHeight The height of each slice (default is 150).
     */
    public static void slice(
            final File srcImageFile,
            final File descDir,
            final int targetWidth,
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
     * Slices an image into smaller pieces of a specified width and height. Remember to manually call
     * {@link #flush(Image)} to release resources.
     *
     * @param srcImage     The source image.
     * @param descDir      The destination directory for the sliced images.
     * @param targetWidth  The width of each slice (default is 200).
     * @param targetHeight The height of each slice (default is 150).
     */
    public static void slice(final Image srcImage, final File descDir, int targetWidth, int targetHeight) {
        if (targetWidth <= 0) {
            targetWidth = 200; // Slice width
        }
        if (targetHeight <= 0) {
            targetHeight = 150; // Slice height
        }
        final int srcWidth = srcImage.getWidth(null); // Source image width
        final int srcHeight = srcImage.getHeight(null); // Source image height

        if (srcWidth < targetWidth) {
            targetWidth = srcWidth;
        }
        if (srcHeight < targetHeight) {
            targetHeight = srcHeight;
        }

        final int cols; // Number of horizontal slices
        final int rows; // Number of vertical slices
        // Calculate the number of horizontal and vertical slices
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
        // Loop to create slices
        Image tag;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // The four parameters are the starting coordinates and the width/height of the image
                // i.e., CropImageFilter(int x, int y, int width, int height)
                tag = cut(srcImage, new Rectangle(j * targetWidth, i * targetHeight, targetWidth, targetHeight));
                // Output to file
                write(tag, FileKit.file(descDir, "_r" + i + "_c" + j + ".jpg"));
            }
        }
    }

    /**
     * Slices an image into a specified number of rows and columns.
     *
     * @param srcImageFile The source image file.
     * @param targetDir    The destination directory for the sliced images.
     * @param formatName   The format name (e.g., "jpg", "png").
     * @param rows         The number of rows (default is 2, must be between 1 and 20).
     * @param cols         The number of columns (default is 2, must be between 1 and 20).
     */
    public static void sliceByRowsAndCols(
            final File srcImageFile,
            final File targetDir,
            final String formatName,
            final int rows,
            final int cols) {
        BufferedImage image = null;
        try {
            image = read(srcImageFile);
            sliceByRowsAndCols(image, targetDir, formatName, rows, cols);
        } finally {
            flush(image);
        }
    }

    /**
     * Slices an image into a specified number of rows and columns, with default RGB mode. Remember to manually call
     * {@link #flush(Image)} to release resources.
     *
     * @param srcImage   The source image. If not a {@link BufferedImage}, it defaults to RGB mode.
     * @param destDir    The destination directory for the sliced images.
     * @param formatName The format name (e.g., "jpg", "png").
     * @param rows       The number of rows (default is 2, must be between 1 and 20).
     * @param cols       The number of columns (default is 2, must be between 1 and 20).
     */
    public static void sliceByRowsAndCols(
            final Image srcImage,
            final File destDir,
            final String formatName,
            int rows,
            int cols) {
        if (!destDir.exists()) {
            FileKit.mkdir(destDir);
        } else if (!destDir.isDirectory()) {
            throw new IllegalArgumentException("Destination must be a Directory !");
        }

        if (rows <= 0 || rows > 20) {
            rows = 2; // Number of rows for slicing
        }
        if (cols <= 0 || cols > 20) {
            cols = 2; // Number of columns for slicing
        }
        // Read the source image
        final int srcWidth = srcImage.getWidth(null); // Source image width
        final int srcHeight = srcImage.getHeight(null); // Source image height

        final int targetWidth = MathKit.partValue(srcWidth, cols); // Width of each slice
        final int targetHeight = MathKit.partValue(srcHeight, rows); // Height of each slice

        // Loop to create slices
        Image tag;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                tag = cut(srcImage, new Rectangle(j * targetWidth, i * targetHeight, targetWidth, targetHeight));
                // Output to file
                write(tag, new File(destDir, "_r" + i + "_c" + j + "." + formatName));
            }
        }
    }

    /**
     * Converts the image type, e.g., GIF to JPG, GIF to PNG, PNG to JPG, etc.
     *
     * @param srcImageFile    The source image file.
     * @param targetImageFile The destination image file.
     */
    public static void convert(final File srcImageFile, final File targetImageFile) {
        Assert.notNull(srcImageFile);
        Assert.notNull(targetImageFile);
        Assert.isFalse(srcImageFile.equals(targetImageFile), "Src file is equals to dest file!");

        // Check image type by extension; if the same, just copy the file
        final String srcExtName = FileName.extName(srcImageFile);
        final String destExtName = FileName.extName(targetImageFile);
        if (StringKit.equalsIgnoreCase(srcExtName, destExtName)) {
            // If extensions are the same, just copy the file
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
     * Converts the image type. This method does not close the streams.
     *
     * @param srcStream    The source image stream.
     * @param formatName   A string containing the informal format name, e.g., "JPG", "JPEG", "GIF".
     * @param targetStream The destination image output stream.
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
     * Converts the image type. This method does not close the streams. Remember to manually call {@link #flush(Image)}
     * to release resources.
     *
     * @param srcImage          The source image.
     * @param formatName        A string containing the informal format name, e.g., "JPG", "JPEG", "GIF".
     * @param targetImageStream The destination image output stream.
     */
    public static void convert(
            final Image srcImage,
            final String formatName,
            final ImageOutputStream targetImageStream) {
        Images.from(srcImage).setTargetImageType(formatName).write(targetImageStream);
    }

    /**
     * Converts a color image to grayscale.
     *
     * @param srcImageFile    The source image file.
     * @param targetImageFile The destination file for the grayscale image.
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
     * Converts a color image to grayscale. This method does not close the streams.
     *
     * @param srcStream    The source image stream.
     * @param targetStream The destination image stream.
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
     * Converts a color image to grayscale. This method does not close the streams.
     *
     * @param srcStream    The source {@link ImageInputStream}.
     * @param targetStream The destination {@link ImageOutputStream}.
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
     * Converts a color image to grayscale. Remember to manually call {@link #flush(Image)} to release resources.
     *
     * @param srcImage The source image.
     * @param outFile  The destination file.
     */
    public static void gray(final Image srcImage, final File outFile) {
        write(gray(srcImage), outFile);
    }

    /**
     * Converts a color image to grayscale. This method does not close the output stream. Remember to manually call
     * {@link #flush(Image)} to release resources.
     *
     * @param srcImage The source image.
     * @param out      The destination output stream.
     */
    public static void gray(final Image srcImage, final OutputStream out) {
        gray(srcImage, getImageOutputStream(out));
    }

    /**
     * Converts a color image to grayscale. This method does not close the output stream. Remember to manually call
     * {@link #flush(Image)} to release resources.
     *
     * @param srcImage          The source image.
     * @param targetImageStream The destination image stream.
     * @throws InternalException if an I/O error occurs.
     */
    public static void gray(final Image srcImage, final ImageOutputStream targetImageStream) throws InternalException {
        writeJpg(gray(srcImage), targetImageStream);
    }

    /**
     * Converts a color image to grayscale. Remember to manually call {@link #flush(Image)} to release resources.
     *
     * @param srcImage The source image.
     * @return The grayscaled {@link Image}.
     */
    public static Image gray(final Image srcImage) {
        return Images.from(srcImage).gray().getImg();
    }

    /**
     * Converts a color image to a binary (black and white) image. The output format is determined by the destination
     * file's extension.
     *
     * @param srcImageFile    The source image file.
     * @param targetImageFile The destination file for the binary image.
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
     * Converts a color image to a binary (black and white) image. This method does not close the streams.
     *
     * @param srcStream    The source image stream.
     * @param targetStream The destination image stream.
     * @param imageType    The image format (extension).
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
     * Converts a color image to a binary (black and white) image. This method does not close the streams.
     *
     * @param srcStream    The source {@link ImageInputStream}.
     * @param targetStream The destination {@link ImageOutputStream}.
     * @param imageType    The image format (extension).
     */
    public static void binary(
            final ImageInputStream srcStream,
            final ImageOutputStream targetStream,
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
     * Converts a color image to a binary (black and white) image. The output format is determined by the destination
     * file's extension. Remember to manually call {@link #flush(Image)} to release resources.
     *
     * @param srcImage The source image.
     * @param outFile  The destination file.
     */
    public static void binary(final Image srcImage, final File outFile) {
        write(binary(srcImage), outFile);
    }

    /**
     * Converts a color image to a binary (black and white) image. The output format is JPG. This method does not close
     * the output stream. Remember to manually call {@link #flush(Image)} to release resources.
     *
     * @param srcImage  The source image.
     * @param out       The destination output stream.
     * @param imageType The image format (extension).
     */
    public static void binary(final Image srcImage, final OutputStream out, final String imageType) {
        binary(srcImage, getImageOutputStream(out), imageType);
    }

    /**
     * Converts a color image to a binary (black and white) image. The output format is JPG. This method does not close
     * the output stream. Remember to manually call {@link #flush(Image)} to release resources.
     *
     * @param srcImage          The source image.
     * @param targetImageStream The destination image stream.
     * @param imageType         The image format (extension).
     * @throws InternalException if an I/O error occurs.
     */
    public static void binary(final Image srcImage, final ImageOutputStream targetImageStream, final String imageType)
            throws InternalException {
        write(binary(srcImage), imageType, targetImageStream);
    }

    /**
     * Converts a color image to a binary (black and white) image. Remember to manually call {@link #flush(Image)} to
     * release resources.
     *
     * @param srcImage The source image.
     * @return The binary {@link Image}.
     */
    public static Image binary(final Image srcImage) {
        return Images.from(srcImage).binary().getImg();
    }

    /**
     * Adds a text watermark to an image.
     *
     * @param imageFile The source image file.
     * @param destFile  The destination file for the watermarked image.
     * @param pressText The text watermark information.
     */
    public static void pressText(final File imageFile, final File destFile, final ImageText pressText) {
        BufferedImage image = null;
        try {
            image = read(imageFile);
            pressText(image, destFile, pressText);
        } finally {
            flush(image);
        }
    }

    /**
     * Adds a text watermark to an image. This method does not close the streams.
     *
     * @param srcStream    The source image stream.
     * @param targetStream The destination image stream.
     * @param pressText    The text watermark information.
     */
    public static void pressText(
            final InputStream srcStream,
            final OutputStream targetStream,
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
     * Adds a text watermark to an image. This method does not close the output stream. Remember to manually call
     * {@link #flush(Image)} to release resources.
     *
     * @param srcImage  The source image.
     * @param destFile  The destination file.
     * @param pressText The text watermark information.
     * @throws InternalException if an I/O error occurs.
     */
    public static void pressText(final Image srcImage, final File destFile, final ImageText pressText)
            throws InternalException {
        write(pressText(srcImage, pressText), destFile);
    }

    /**
     * Adds a text watermark to an image. This method does not close the output stream. Remember to manually call
     * {@link #flush(Image)} to release resources.
     *
     * @param srcImage  The source image.
     * @param to        The destination output stream.
     * @param pressText The text watermark information.
     * @throws InternalException if an I/O error occurs.
     */
    public static void pressText(final Image srcImage, final OutputStream to, final ImageText pressText)
            throws InternalException {
        pressText(srcImage, getImageOutputStream(to), pressText);
    }

    /**
     * Adds a text watermark to an image. This method does not close the output stream. Remember to manually call
     * {@link #flush(Image)} to release resources.
     *
     * @param srcImage          The source image.
     * @param targetImageStream The destination image stream.
     * @param pressText         The text watermark information.
     * @throws InternalException if an I/O error occurs.
     */
    public static void pressText(
            final Image srcImage,
            final ImageOutputStream targetImageStream,
            final ImageText pressText) throws InternalException {
        writeJpg(pressText(srcImage, pressText), targetImageStream);
    }

    /**
     * Adds a text watermark to an image. This method does not close the output stream. Remember to manually call
     * {@link #flush(Image)} to release resources.
     *
     * @param srcImage  The source image.
     * @param pressText The text watermark information.
     * @return The watermarked image.
     */
    public static Image pressText(final Image srcImage, final ImageText pressText) {
        return Images.from(srcImage).pressText(pressText).getImg();
    }

    /**
     * Adds a full-screen text watermark to an image. This method does not close the output stream.
     *
     * @param srcImage   The source image. Remember to manually call {@link #flush(Image)} to release resources.
     * @param pressText  The watermark text.
     * @param color      The font color of the watermark.
     * @param font       The {@link Font} information. If null, a default font is used.
     * @param lineHeight The line height.
     * @param degree     The rotation angle in degrees. Positive values are clockwise, negative are counter-clockwise
     *                   from the origin (0,0).
     * @param alpha      The transparency, a float between 0.0 and 1.0 (inclusive).
     * @return The processed image.
     */
    public static Image pressTextFull(
            final Image srcImage,
            final String pressText,
            final Color color,
            final Font font,
            final int lineHeight,
            final int degree,
            final float alpha) {
        return Images.from(srcImage).pressTextFull(pressText, color, font, lineHeight, degree, alpha).getImg();
    }

    /**
     * Adds an image watermark to an image.
     *
     * @param srcImageFile    The source image file.
     * @param targetImageFile The destination file for the watermarked image.
     * @param pressImg        The watermark image. Remember to manually call {@link #flush(Image)} to release resources.
     * @param x               The x-offset from the center.
     * @param y               The y-offset from the center.
     * @param alpha           The transparency, a float between 0.0 and 1.0 (inclusive).
     */
    public static void pressImage(
            final File srcImageFile,
            final File targetImageFile,
            final Image pressImg,
            final int x,
            final int y,
            final float alpha) {
        BufferedImage image = null;
        try {
            image = read(srcImageFile);
            pressImage(image, targetImageFile, pressImg, x, y, alpha);
        } finally {
            flush(image);
        }
    }

    /**
     * Adds an image watermark to an image. This method does not close the streams.
     *
     * @param srcStream    The source image stream.
     * @param targetStream The destination image stream.
     * @param pressImg     The watermark image, which can be read using {@link ImageIO#read(File)}.
     * @param x            The x-offset from the center.
     * @param y            The y-offset from the center.
     * @param alpha        The transparency, a float between 0.0 and 1.0 (inclusive).
     */
    public static void pressImage(
            final InputStream srcStream,
            final OutputStream targetStream,
            final Image pressImg,
            final int x,
            final int y,
            final float alpha) {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            pressImage(image, getImageOutputStream(targetStream), pressImg, x, y, alpha);
        } finally {
            flush(image);
        }
    }

    /**
     * Adds an image watermark to an image. This method does not close the output stream. Remember to manually call
     * {@link #flush(Image)} to release resources.
     *
     * @param srcImage The source image.
     * @param outFile  The destination file.
     * @param pressImg The watermark image, which can be read using {@link ImageIO#read(File)}.
     * @param x        The x-offset from the center.
     * @param y        The y-offset from the center.
     * @param alpha    The transparency, a float between 0.0 and 1.0 (inclusive).
     * @throws InternalException if an I/O error occurs.
     */
    public static void pressImage(
            final Image srcImage,
            final File outFile,
            final Image pressImg,
            final int x,
            final int y,
            final float alpha) throws InternalException {
        write(pressImage(srcImage, pressImg, x, y, alpha), outFile);
    }

    /**
     * Adds an image watermark to an image, writing the output in JPG format. This method does not close the output
     * stream. Remember to manually call {@link #flush(Image)} to release resources.
     *
     * @param srcImage The source image.
     * @param out      The destination output stream.
     * @param pressImg The watermark image, which can be read using {@link ImageIO#read(File)}.
     * @param x        The x-offset from the center.
     * @param y        The y-offset from the center.
     * @param alpha    The transparency, a float between 0.0 and 1.0 (inclusive).
     * @throws InternalException if an I/O error occurs.
     */
    public static void pressImage(
            final Image srcImage,
            final OutputStream out,
            final Image pressImg,
            final int x,
            final int y,
            final float alpha) throws InternalException {
        pressImage(srcImage, getImageOutputStream(out), pressImg, x, y, alpha);
    }

    /**
     * Adds an image watermark to an image, writing the output in JPG format. This method does not close the output
     * stream. Remember to manually call {@link #flush(Image)} to release resources.
     *
     * @param srcImage          The source image.
     * @param targetImageStream The destination image stream.
     * @param pressImg          The watermark image, which can be read using {@link ImageIO#read(File)}.
     * @param x                 The x-offset from the center.
     * @param y                 The y-offset from the center.
     * @param alpha             The transparency, a float between 0.0 and 1.0 (inclusive).
     * @throws InternalException if an I/O error occurs.
     */
    public static void pressImage(
            final Image srcImage,
            final ImageOutputStream targetImageStream,
            final Image pressImg,
            final int x,
            final int y,
            final float alpha) throws InternalException {
        writeJpg(pressImage(srcImage, pressImg, x, y, alpha), targetImageStream);
    }

    /**
     * Adds an image watermark to an image. This method does not close the output stream. Remember to manually call
     * {@link #flush(Image)} to release resources.
     *
     * @param srcImage The source image.
     * @param pressImg The watermark image, which can be read using {@link ImageIO#read(File)}.
     * @param x        The x-offset from the center.
     * @param y        The y-offset from the center.
     * @param alpha    The transparency, a float between 0.0 and 1.0 (inclusive).
     * @return The resulting watermarked image.
     */
    public static Image pressImage(
            final Image srcImage,
            final Image pressImg,
            final int x,
            final int y,
            final float alpha) {
        return Images.from(srcImage).pressImage(pressImg, x, y, alpha).getImg();
    }

    /**
     * Adds an image watermark to an image. This method does not close the output stream. Remember to manually call
     * {@link #flush(Image)} to release resources.
     *
     * @param srcImage  The source image.
     * @param pressImg  The watermark image, which can be read using {@link ImageIO#read(File)}.
     * @param rectangle A {@link Rectangle} object specifying the x, y, width, and height of the watermark, with x and y
     *                  calculated from the center of the background image.
     * @param alpha     The transparency, a float between 0.0 and 1.0 (inclusive).
     * @return The resulting watermarked image.
     */
    public static Image pressImage(
            final Image srcImage,
            final Image pressImg,
            final Rectangle rectangle,
            final float alpha) {
        return Images.from(srcImage).pressImage(pressImg, rectangle, alpha).getImg();
    }

    /**
     * Adds a full-screen image watermark to an image.
     *
     * @param imageFile      The source image file.
     * @param destFile       The destination file for the watermarked image.
     * @param pressImageFile The watermark image file.
     * @param lineHeight     The line height.
     * @param degree         The rotation angle of the watermark image in degrees. Positive values are clockwise,
     *                       negative are counter-clockwise from the origin (0,0).
     * @param alpha          The transparency, a float between 0.0 and 1.0 (inclusive).
     * @throws InternalException if an I/O error occurs.
     */
    public static void pressImageFull(
            final File imageFile,
            final File destFile,
            final File pressImageFile,
            final int lineHeight,
            final int degree,
            final float alpha) throws InternalException {
        BufferedImage image = null;
        try {
            image = read(imageFile);
            write(pressImageFull(image, read(pressImageFile), lineHeight, degree, alpha), destFile);
        } finally {
            flush(image);
        }
    }

    /**
     * Adds a full-screen image watermark to an image, writing in JPG format. This method does not close the streams.
     *
     * @param srcStream    The source image stream.
     * @param targetStream The destination image stream.
     * @param pressStream  The watermark image stream.
     * @param lineHeight   The line height.
     * @param degree       The rotation angle of the watermark image in degrees. Positive values are clockwise, negative
     *                     are counter-clockwise from the origin (0,0).
     * @param alpha        The transparency, a float between 0.0 and 1.0 (inclusive).
     * @throws InternalException if an I/O error occurs.
     */
    public static void pressImageFull(
            final InputStream srcStream,
            final OutputStream targetStream,
            final InputStream pressStream,
            final int lineHeight,
            final int degree,
            final float alpha) throws InternalException {
        BufferedImage image = null;
        try {
            image = read(srcStream);
            writeJpg(pressImageFull(image, read(pressStream), lineHeight, degree, alpha), targetStream);
        } finally {
            flush(image);
        }
    }

    /**
     * Adds a full-screen image watermark to an image. This method does not close the output stream.
     *
     * @param srcImage   The source image. Remember to manually call {@link #flush(Image)} to release resources.
     * @param pressImage The watermark image. Remember to manually call {@link #flush(Image)} to release resources.
     * @param lineHeight The line height.
     * @param degree     The rotation angle of the watermark image in degrees. Positive values are clockwise, negative
     *                   are counter-clockwise from the origin (0,0).
     * @param alpha      The transparency, a float between 0.0 and 1.0 (inclusive).
     * @return The processed image.
     */
    public static Image pressImageFull(
            final Image srcImage,
            final Image pressImage,
            final int lineHeight,
            final int degree,
            final float alpha) {
        return Images.from(srcImage).pressImageFull(pressImage, lineHeight, degree, alpha).getImg();
    }

    /**
     * Rotates an image by a specified angle. This method does not close the output stream.
     *
     * @param imageFile The image file to be rotated.
     * @param degree    The rotation angle in degrees.
     * @param outFile   The output file.
     * @throws InternalException if an I/O error occurs.
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
     * Rotates an image by a specified angle. This method does not close the output stream.
     *
     * @param image   The image to be rotated. Remember to manually call {@link #flush(Image)} to release resources.
     * @param degree  The rotation angle in degrees.
     * @param outFile The output file.
     * @throws InternalException if an I/O error occurs.
     */
    public static void rotate(final Image image, final int degree, final File outFile) throws InternalException {
        write(rotate(image, degree), outFile);
    }

    /**
     * Rotates an image by a specified angle. This method does not close the output stream.
     *
     * @param image  The image to be rotated. Remember to manually call {@link #flush(Image)} to release resources.
     * @param degree The rotation angle in degrees.
     * @param out    The output stream.
     * @throws InternalException if an I/O error occurs.
     */
    public static void rotate(final Image image, final int degree, final OutputStream out) throws InternalException {
        writeJpg(rotate(image, degree), getImageOutputStream(out));
    }

    /**
     * Rotates an image by a specified angle. This method does not close the output stream, and the output format is
     * JPG.
     *
     * @param image  The image to be rotated. Remember to manually call {@link #flush(Image)} to release resources.
     * @param degree The rotation angle in degrees.
     * @param out    The output image stream.
     * @throws InternalException if an I/O error occurs.
     */
    public static void rotate(final Image image, final int degree, final ImageOutputStream out)
            throws InternalException {
        writeJpg(rotate(image, degree), out);
    }

    /**
     * Rotates an image by a specified angle. From:
     * <a href="http://blog.51cto.com/cping1982/130066">http://blog.51cto.com/cping1982/130066</a>
     *
     * @param image  The image to be rotated. Remember to manually call {@link #flush(Image)} to release resources.
     * @param degree The rotation angle in degrees.
     * @return The rotated image.
     */
    public static Image rotate(final Image image, final int degree) {
        return Images.from(image).rotate(degree).getImg();
    }

    /**
     * Flips an image horizontally.
     *
     * @param imageFile The image file.
     * @param outFile   The output file.
     * @throws InternalException if an I/O error occurs.
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
     * Flips an image horizontally.
     *
     * @param image   The image. Remember to manually call {@link #flush(Image)} to release resources.
     * @param outFile The output file.
     * @throws InternalException if an I/O error occurs.
     */
    public static void flip(final Image image, final File outFile) throws InternalException {
        write(flip(image), outFile);
    }

    /**
     * Flips an image horizontally.
     *
     * @param image The image. Remember to manually call {@link #flush(Image)} to release resources.
     * @param out   The output stream.
     * @throws InternalException if an I/O error occurs.
     */
    public static void flip(final Image image, final OutputStream out) throws InternalException {
        flip(image, getImageOutputStream(out));
    }

    /**
     * Flips an image horizontally, writing in JPG format.
     *
     * @param image The image. Remember to manually call {@link #flush(Image)} to release resources.
     * @param out   The output stream.
     * @throws InternalException if an I/O error occurs.
     */
    public static void flip(final Image image, final ImageOutputStream out) throws InternalException {
        writeJpg(flip(image), out);
    }

    /**
     * Flips an image horizontally.
     *
     * @param image The image. Remember to manually call {@link #flush(Image)} to release resources.
     * @return The flipped image.
     */
    public static Image flip(final Image image) {
        return Images.from(image).flip().getImg();
    }

    /**
     * Compresses an image. The output image only supports JPG format.
     *
     * @param imageFile The image file.
     * @param outFile   The output file (only JPG is supported).
     * @param quality   The compression quality, a float between 0 and 1.
     * @throws InternalException if an I/O error occurs.
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
     * Converts an {@link Image} to a {@link RenderedImage}. It first attempts a direct cast; otherwise, it creates a
     * new {@link BufferedImage} and redraws the image using {@link BufferedImage#TYPE_INT_RGB} mode.
     *
     * @param img       The {@link Image}.
     * @param imageType The target image type, e.g., "jpg" or "png".
     * @return A {@link BufferedImage}.
     */
    public static RenderedImage castToRenderedImage(final Image img, final String imageType) {
        if (img instanceof RenderedImage) {
            return (RenderedImage) img;
        }

        return toBufferedImage(img, imageType);
    }

    /**
     * Converts an {@link Image} to a {@link BufferedImage}. It first attempts a direct cast; otherwise, it creates a
     * new {@link BufferedImage} and redraws the image using the specified image type.
     *
     * @param img       The {@link Image}.
     * @param imageType The target image type, e.g., "jpg" or "png".
     * @return A {@link BufferedImage}.
     */
    public static BufferedImage castToBufferedImage(final Image img, final String imageType) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        return toBufferedImage(img, imageType);
    }

    /**
     * Converts an {@link Image} to a {@link BufferedImage}. If the source image's RGB mode matches the target mode, it
     * converts directly; otherwise, it redraws. By default, PNG images use {@link BufferedImage#TYPE_INT_ARGB} mode,
     * while others use {@link BufferedImage#TYPE_INT_RGB}.
     *
     * @param image     The {@link Image}.
     * @param imageType The target image type, e.g., "jpg" or "png".
     * @return A {@link BufferedImage}.
     */
    public static BufferedImage toBufferedImage(final Image image, final String imageType) {
        return toBufferedImage(image, imageType, null);
    }

    /**
     * Converts an {@link Image} to a {@link BufferedImage}. If the source image's RGB mode matches the target mode, it
     * converts directly; otherwise, it redraws. By default, PNG images use {@link BufferedImage#TYPE_INT_ARGB} mode,
     * while others use {@link BufferedImage#TYPE_INT_RGB}.
     *
     * @param image           The {@link Image}.
     * @param imageType       The target image type, e.g., "jpg" or "png".
     * @param backgroundColor The background color {@link Color}. {@code null} indicates the default background (black
     *                        or transparent).
     * @return A {@link BufferedImage}.
     */
    public static BufferedImage toBufferedImage(
            final Image image,
            final String imageType,
            final Color backgroundColor) {
        final int type = IMAGE_TYPE_PNG.equalsIgnoreCase(imageType) ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB;
        return toBufferedImage(image, type, backgroundColor);
    }

    /**
     * Converts an {@link Image} to a {@link BufferedImage}. If the source image's RGB mode matches the target mode, it
     * converts directly; otherwise, it redraws.
     *
     * @param image     The {@link Image}.
     * @param imageType The target image type, a constant from {@link BufferedImage} (e.g., for grayscale).
     * @return A {@link BufferedImage}.
     */
    public static BufferedImage toBufferedImage(final Image image, final int imageType) {
        return toBufferedImage(image, imageType, null);
    }

    /**
     * Converts an {@link Image} to a {@link BufferedImage}. If the source image's RGB mode matches the target mode, it
     * converts directly; otherwise, it redraws.
     *
     * @param image           The {@link Image}.
     * @param imageType       The target image type, a constant from {@link BufferedImage} (e.g., for grayscale).
     * @param backgroundColor The background color {@link Color}. {@code null} indicates the default background (black
     *                        or transparent).
     * @return A {@link BufferedImage}.
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
     * Creates a new copy of an existing {@link Image}.
     *
     * @param img       The {@link Image}.
     * @param imageType The target image type, a constant from {@link BufferedImage}.
     * @return A {@link BufferedImage}.
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
     * Creates a new copy of an existing {@link Image}.
     *
     * @param img             The {@link Image}.
     * @param imageType       The target image type, a constant from {@link BufferedImage}.
     * @param backgroundColor The background color, or {@code null} for the default (black or transparent).
     * @return A {@link BufferedImage}.
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
     * Converts a Base64 encoded image string to a {@link BufferedImage}.
     *
     * @param base64 The Base64 representation of the image.
     * @return A {@link BufferedImage}.
     * @throws InternalException if an I/O error occurs.
     */
    public static BufferedImage toImage(final String base64) throws InternalException {
        return toImage(Base64.decode(base64));
    }

    /**
     * Converts image bytes to a {@link BufferedImage}.
     *
     * @param imageBytes The image bytes.
     * @return A {@link BufferedImage}.
     * @throws InternalException if an I/O error occurs.
     */
    public static BufferedImage toImage(final byte[] imageBytes) throws InternalException {
        return read(new ByteArrayInputStream(imageBytes));
    }

    /**
     * Converts an {@link Image} object to an {@link InputStream}.
     *
     * @param image     The image object.
     * @param imageType The image type.
     * @return The Base64 string representation.
     */
    public static ByteArrayInputStream toStream(final Image image, final String imageType) {
        return IoKit.toStream(toBytes(image, imageType));
    }

    /**
     * Converts an {@link Image} object to a Base64 Data URI, in the format: data:image/[imageType];base64,[data].
     *
     * @param image     The image object.
     * @param imageType The image type.
     * @return The Base64 string representation.
     */
    public static String toBase64DataUri(final Image image, final String imageType) {
        return UrlKit.getDataUri("image/" + imageType, "base64", toBase64(image, imageType));
    }

    /**
     * Converts an {@link Image} object to a Base64 string.
     *
     * @param image     The image object.
     * @param imageType The image type.
     * @return The Base64 string representation.
     */
    public static String toBase64(final Image image, final String imageType) {
        return Base64.encode(toBytes(image, imageType));
    }

    /**
     * Converts an {@link Image} object to a byte array.
     *
     * @param image     The image object.
     * @param imageType The image type.
     * @return The Base64 string representation.
     */
    public static byte[] toBytes(final Image image, final String imageType) {
        final FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        write(image, imageType, out);
        return out.toByteArrayZeroCopyIfPossible();
    }

    /**
     * Creates a {@link BufferedImage} compatible with the current device's color model.
     *
     * @param width        The width.
     * @param height       The height.
     * @param transparency The transparency mode, see {@link Transparency}.
     * @return A {@link BufferedImage}.
     */
    public static BufferedImage createCompatibleImage(final int width, final int height, final int transparency) {
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice gs = ge.getDefaultScreenDevice();
        final GraphicsConfiguration gc = gs.getDefaultConfiguration();
        return gc.createCompatibleImage(width, height, transparency);
    }

    /**
     * Creates a PNG image with a transparent background from text.
     *
     * @param text      The text.
     * @param font      The {@link Font}.
     * @param fontColor The font color (defaults to black).
     * @param out       The image output destination.
     * @throws InternalException if an I/O error occurs.
     */
    public static void createTransparentImage(
            final String text,
            final Font font,
            final Color fontColor,
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
     * Creates a PNG image from text.
     *
     * @param text            The text.
     * @param font            The {@link Font}.
     * @param backgroundColor The background color (defaults to transparent).
     * @param fontColor       The font color (defaults to black).
     * @param out             The image output destination.
     * @throws InternalException if an I/O error occurs.
     */
    public static void createImage(
            final String text,
            final Font font,
            final Color backgroundColor,
            final Color fontColor,
            final ImageOutputStream out) throws InternalException {
        BufferedImage image = null;
        try {
            image = createImage(text, font, backgroundColor, fontColor, BufferedImage.TYPE_INT_ARGB);
            writePng(image, out);
        } finally {
            flush(image);
        }
    }

    /**
     * Creates an image from text.
     *
     * @param text            The text.
     * @param font            The {@link Font}.
     * @param backgroundColor The background color (defaults to transparent).
     * @param fontColor       The font color (defaults to black).
     * @param imageType       The image type, see {@link BufferedImage}.
     * @return The image.
     * @throws InternalException if an I/O error occurs.
     */
    public static BufferedImage createImage(
            final String text,
            final Font font,
            final Color backgroundColor,
            final Color fontColor,
            final int imageType) throws InternalException {
        // Get the bounding rectangle of the string with the given font
        final Rectangle2D r = getRectangle(text, font);
        // Get the height of a single character
        final int unitHeight = (int) Math.floor(r.getHeight());
        // Get the width of the entire string with the font style, rounded up +1 to ensure it fits
        final int width = (int) Math.round(r.getWidth()) + 1;
        // Add 3 to the character height to ensure it fits
        final int height = unitHeight + 3;

        // Create the image
        final BufferedImage image = new BufferedImage(width, height, imageType);
        final Graphics g = createGraphics(image, backgroundColor);
        drawString(g, text, font, fontColor, new Point(0, font.getSize()));
        g.dispose();

        return image;
    }

    /**
     * Writes an image in JPG format.
     *
     * @param image             The {@link Image}.
     * @param targetImageStream The target output stream.
     * @throws InternalException if an I/O error occurs.
     */
    public static void writeJpg(final Image image, final ImageOutputStream targetImageStream) throws InternalException {
        write(image, IMAGE_TYPE_JPG, targetImageStream);
    }

    /**
     * Writes an image in PNG format.
     *
     * @param image             The {@link Image}. Remember to manually call {@link #flush(Image)} to release resources.
     * @param targetImageStream The target output stream.
     * @throws InternalException if an I/O error occurs.
     */
    public static void writePng(final Image image, final ImageOutputStream targetImageStream) throws InternalException {
        write(image, IMAGE_TYPE_PNG, targetImageStream);
    }

    /**
     * Writes an image in JPG format.
     *
     * @param image The {@link Image}. Remember to manually call {@link #flush(Image)} to release resources.
     * @param out   The target output stream.
     * @throws InternalException if an I/O error occurs.
     */
    public static void writeJpg(final Image image, final OutputStream out) throws InternalException {
        write(image, IMAGE_TYPE_JPG, out);
    }

    /**
     * Writes an image in PNG format.
     *
     * @param image The {@link Image}. Remember to manually call {@link #flush(Image)} to release resources.
     * @param out   The target output stream.
     * @throws InternalException if an I/O error occurs.
     */
    public static void writePng(final Image image, final OutputStream out) throws InternalException {
        write(image, IMAGE_TYPE_PNG, out);
    }

    /**
     * Writes an image in the specified format. This method does not close the streams.
     *
     * @param srcStream    The source image stream.
     * @param formatName   A string containing the informal format name, e.g., "JPG", "JPEG", "GIF".
     * @param targetStream The target image output stream.
     */
    public static void write(
            final ImageInputStream srcStream,
            final String formatName,
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
     * Writes an image in the specified format. This method does not close the stream.
     *
     * @param image     The {@link Image}. Remember to manually call {@link #flush(Image)} to release resources.
     * @param imageType The image type (extension).
     * @param out       The target output stream.
     * @throws InternalException if an I/O error occurs.
     */
    public static void write(final Image image, final String imageType, final OutputStream out)
            throws InternalException {
        write(image, imageType, getImageOutputStream(out));
    }

    /**
     * Writes an image in the format corresponding to the destination file's extension.
     *
     * @param image    The {@link Image}. Remember to manually call {@link #flush(Image)} to release resources.
     * @param destFile The destination file.
     * @throws InternalException if an I/O error occurs.
     */
    public static void write(final Image image, final File destFile) throws InternalException {
        ImageWriter.of(image, FileName.extName(destFile)).write(destFile);
    }

    /**
     * Writes an image in the specified format. This method does not close the stream.
     *
     * @param image             The {@link Image}. Remember to manually call {@link #flush(Image)} to release resources.
     * @param imageType         The image type (extension).
     * @param targetImageStream The target output stream.
     * @throws InternalException if an I/O error occurs.
     */
    public static void write(final Image image, final String imageType, final ImageOutputStream targetImageStream)
            throws InternalException {
        write(image, imageType, targetImageStream, 1);
    }

    /**
     * Writes an image in the specified format.
     *
     * @param image           The {@link Image}. Remember to manually call {@link #flush(Image)} to release resources.
     * @param imageType       The image type (extension), or {@code null} for RGB mode (JPG).
     * @param out             The target output stream.
     * @param quality         The quality, a float between 0 and 1 for compression, or any other number for no
     *                        compression.
     * @param backgroundColor The background color {@link Color}.
     * @throws InternalException if an I/O error occurs.
     */
    public static void write(
            final Image image,
            final String imageType,
            final ImageOutputStream out,
            final float quality,
            final Color backgroundColor) throws InternalException {
        final BufferedImage bufferedImage = toBufferedImage(image, imageType, backgroundColor);
        write(bufferedImage, imageType, out, quality);
    }

    /**
     * Writes an image to an output stream using an {@link javax.imageio.ImageWriter}.
     *
     * @param image     The image. Remember to manually call {@link #flush(Image)} to release resources.
     * @param imageType The image type.
     * @param output    The {@link ImageOutputStream}.
     * @param quality   The quality, a float between 0 and 1 for compression, or any other number for no compression.
     */
    public static void write(
            final Image image,
            final String imageType,
            final ImageOutputStream output,
            final float quality) {
        ImageWriter.of(image, imageType).setQuality(quality).write(output);
    }

    /**
     * Gets the appropriate {@link javax.imageio.ImageWriter} for a given image and format. Returns null if no suitable
     * writer is found.
     *
     * @param img        The {@link Image}. Remember to manually call {@link #flush(Image)} to release resources.
     * @param formatName The image format, e.g., "jpg", "png". If {@code null}, "jpg" is used by default.
     * @return An {@link javax.imageio.ImageWriter}.
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
     * Gets the appropriate {@link javax.imageio.ImageWriter} for a given format or extension. Returns null if no
     * suitable writer is found.
     *
     * @param formatName The image format or extension, e.g., "jpg", "png". If {@code null}, "jpg" is used by default.
     * @return An {@link javax.imageio.ImageWriter}.
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
            // Try getting by suffix
            iter = ImageIO.getImageWritersBySuffix(formatName);
            if (iter.hasNext()) {
                writer = iter.next();
            }
        }
        return writer;
    }

    /**
     * Reads an image from a file. Use an absolute path; relative paths are resolved relative to the classpath.
     *
     * @param imageFilePath The path to the image file.
     * @return The image.
     */
    public static BufferedImage read(final String imageFilePath) {
        return read(FileKit.file(imageFilePath));
    }

    /**
     * Reads an image from a file.
     *
     * @param imageFile The image file.
     * @return The image.
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
     * Reads an image from a {@link Resource}.
     *
     * @param resource The image resource.
     * @return The image.
     */
    public static BufferedImage read(final Resource resource) {
        return read(resource.getStream());
    }

    /**
     * Reads an image from a stream.
     *
     * @param imageStream The image stream.
     * @return The image.
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
     * Reads an image from an image stream.
     *
     * @param imageStream The image stream.
     * @return The image.
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
     * Reads an image from a URL.
     *
     * @param imageUrl The image URL.
     * @return The image.
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
     * Gets an {@link ImageReader}.
     *
     * @param type The image file type, e.g., "jpeg" or "tiff".
     * @return An {@link ImageReader}.
     */
    public static ImageReader getReader(final String type) {
        final Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName(type);
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    /**
     * Gets the width and height of an image from an {@link ImageInputStream}.
     *
     * @param imageStream The {@link InputStream}.
     * @param type        The image type.
     * @return The width and height as a {@link Pair}.
     */
    public static Pair<Integer, Integer> getWidthAndHeight(final InputStream imageStream, final String type) {
        return getWidthAndHeight(getImageInputStream(imageStream), type);
    }

    /**
     * Gets the width and height of an image from an {@link ImageInputStream}.
     *
     * @param imageStream The {@link ImageInputStream}.
     * @param type        The image type.
     * @return The width and height as a {@link Pair}.
     */
    public static Pair<Integer, Integer> getWidthAndHeight(final ImageInputStream imageStream, final String type) {
        final ImageReader reader = getReader(type);
        if (null != reader) {
            try {
                reader.setInput(imageStream, true);
                return Pair.of(reader.getWidth(0), reader.getHeight(0));
            } catch (final IOException e) {
                throw new InternalException(e);
            } finally {
                reader.dispose();
            }
        }
        return null;
    }

    /**
     * Gets or reads an image object from a URL.
     *
     * @param url The URL.
     * @return An {@link Image}.
     */
    public static Image getImage(final URL url) {
        return Toolkit.getDefaultToolkit().getImage(url);
    }

    /**
     * Gets an {@link ImageOutputStream}.
     *
     * @param out The {@link OutputStream}.
     * @return An {@link ImageOutputStream}.
     * @throws InternalException if an I/O error occurs.
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
     * Gets an {@link ImageOutputStream}.
     *
     * @param outFile The {@link File}.
     * @return An {@link ImageOutputStream}.
     * @throws InternalException if an I/O error occurs.
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
     * Gets an {@link ImageInputStream}.
     *
     * @param in The {@link InputStream}.
     * @return An {@link ImageInputStream}.
     * @throws InternalException if an I/O error occurs.
     */
    public static ImageInputStream getImageInputStream(final InputStream in) throws InternalException {
        final ImageInputStream result;
        try {
            result = ImageIO.createImageInputStream(in);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        if (null == result) {
            throw new IllegalArgumentException("Image type is not supported!");
        }

        return result;
    }

    /**
     * Gets the corrected coordinates of a rectangle, making them relative to the center of the background.
     *
     * @param rectangle        The rectangle.
     * @param backgroundWidth  The reference width (background width).
     * @param backgroundHeight The reference height (background height).
     * @return The corrected {@link Point}.
     */
    public static Point getPointBaseCentre(
            final Rectangle rectangle,
            final int backgroundWidth,
            final int backgroundHeight) {
        return new Point(rectangle.x + (Math.abs(backgroundWidth - rectangle.width) / 2), //
                rectangle.y + (Math.abs(backgroundHeight - rectangle.height) / 2)//
        );
    }

    /**
     * Removes the background of an image, converting a solid-color background to transparent.
     *
     * @param inputPath  The path to the image to process.
     * @param outputPath The path to save the output image.
     * @param tolerance  The tolerance value (0-255) for color matching.
     */
    public static void backgroundRemoval(final String inputPath, final String outputPath, final int tolerance) {
        ImagePlace.backgroundRemoval(inputPath, outputPath, tolerance);
    }

    /**
     * Removes the background of an image, converting a solid-color background to transparent.
     *
     * @param input     The image file to process.
     * @param output    The output file.
     * @param tolerance The tolerance value (0-255) for color matching.
     */
    public static void backgroundRemoval(final File input, final File output, final int tolerance) {
        ImagePlace.backgroundRemoval(input, output, tolerance);
    }

    /**
     * Removes the background of an image, replacing it with a specified color or transparency.
     *
     * @param input     The image file to process.
     * @param output    The output file.
     * @param override  The color to replace the background with. If null, the background becomes transparent.
     * @param tolerance The tolerance value (0-255) for color matching.
     */
    public static void backgroundRemoval(
            final File input,
            final File output,
            final Color override,
            final int tolerance) {
        ImagePlace.backgroundRemoval(input, output, override, tolerance);
    }

    /**
     * Removes the background of an image, replacing it with a specified color or transparency.
     *
     * @param bufferedImage The image stream to process.
     * @param override      The color to replace the background with. If null, the background becomes transparent.
     * @param tolerance     The tolerance value (0-255) for color matching.
     * @return The processed image stream.
     */
    public static BufferedImage backgroundRemoval(
            final BufferedImage bufferedImage,
            final Color override,
            final int tolerance) {
        return ImagePlace.backgroundRemoval(bufferedImage, override, tolerance);
    }

    /**
     * Removes the background of an image, replacing it with a specified color or transparency.
     *
     * @param outputStream The byte array output stream of the image to process.
     * @param override     The color to replace the background with. If null, the background becomes transparent.
     * @param tolerance    The tolerance value (0-255) for color matching.
     * @return The processed image stream.
     */
    public static BufferedImage backgroundRemoval(
            final ByteArrayOutputStream outputStream,
            final Color override,
            final int tolerance) {
        return ImagePlace.backgroundRemoval(outputStream, override, tolerance);
    }

    /**
     * Converts the color space of an image (e.g., to grayscale).
     *
     * @param colorSpace The target color space.
     * @param image      The image to convert.
     * @return The converted image.
     */
    public static BufferedImage colorConvert(final ColorSpace colorSpace, final BufferedImage image) {
        return filter(new ColorConvertOp(colorSpace, null), image);
    }

    /**
     * Applies an affine transformation to an image (e.g., translation, scale, flip, rotation, shear).
     *
     * @param xform The 2D affine transform.
     * @param image The image to transform.
     * @return The transformed image.
     */
    public static BufferedImage transform(final AffineTransform xform, final BufferedImage image) {
        return filter(new AffineTransformOp(xform, null), image);
    }

    /**
     * Applies a filter to an image.
     *
     * @param op    The filter operation (e.g., {@link AffineTransformOp}).
     * @param image The original image.
     * @return The filtered image.
     */
    public static BufferedImage filter(final BufferedImageOp op, final BufferedImage image) {
        return op.filter(image, null);
    }

    /**
     * Applies a filter to an image using an {@link ImageFilter}.
     *
     * @param filter The filter implementation.
     * @param image  The image.
     * @return The filtered image.
     */
    public static Image filter(final ImageFilter filter, final Image image) {
        return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), filter));
    }

    /**
     * Flushes and releases the resources of an {@link Image}.
     *
     * @param image The {@link Image}.
     */
    public static void flush(final Image image) {
        if (null != image) {
            image.flush();
        }
    }

    /**
     * Creates a {@link Graphics2D} object for a {@link BufferedImage}.
     *
     * @param image The {@link BufferedImage}.
     * @param color The background and current drawing color. If {@code null}, no background is set.
     * @return A {@link Graphics2D} object.
     */
    public static Graphics2D createGraphics(final BufferedImage image, final Color color) {
        final Graphics2D g = image.createGraphics();
        if (null != color) {
            // Fill background
            g.setColor(color);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
        }

        return g;
    }

    /**
     * Gets the Y-coordinate for vertically centering text. This method relies on {@link FontMetrics}.
     *
     * @param g                The {@link Graphics2D} object.
     * @param backgroundHeight The height of the background.
     * @return The minimum height, or -1 if it cannot be determined.
     */
    public static int getCenterY(final Graphics g, final int backgroundHeight) {
        // Get the minimum text height
        FontMetrics metrics = null;
        try {
            metrics = g.getFontMetrics();
        } catch (final Exception e) {
            // Handle exceptions like IndexOutOfBoundsException
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
     * Draws a string with random colors and default anti-aliasing.
     *
     * @param g      The {@link Graphics} object.
     * @param text   The string.
     * @param font   The font.
     * @param width  The total width for the string.
     * @param height The background height for the string.
     * @return The {@link Graphics} object.
     */
    public static Graphics drawStringColourful(
            final Graphics g,
            final String text,
            final Font font,
            final int width,
            final int height) {
        return drawStringColourful(g, text, font, width, height, null, 0);
    }

    /**
     * Draws a string with random colors and default anti-aliasing.
     *
     * @param g                The {@link Graphics} object.
     * @param str              The string.
     * @param font             The font.
     * @param width            The total width for the string.
     * @param height           The background height for the string.
     * @param compareColor     A color to compare against for ensuring minimum color distance.
     * @param minColorDistance The minimum color distance from the compare color.
     * @return The {@link Graphics} object.
     */
    public static Graphics drawStringColourful(
            final Graphics g,
            final String str,
            final Font font,
            final int width,
            final int height,
            final Color compareColor,
            final int minColorDistance) {
        // Anti-aliasing
        enableAntialias(g);
        // Create font
        g.setFont(font);

        // Text height (must be called after setting font)
        final int midY = getCenterY(g, height);

        final int len = str.length();
        final int charWidth = width / len;
        for (int i = 0; i < len; i++) {
            // Generate a random color for each character
            g.setColor(ColorKit.randomColor(compareColor, minColorDistance));
            g.drawString(String.valueOf(str.charAt(i)), i * charWidth, midY);
        }
        return g;
    }

    /**
     * Draws a string with default anti-aliasing.
     *
     * @param g      The {@link Graphics} object.
     * @param text   The string.
     * @param font   The font.
     * @param color  The font color, or {@code null} for random colors.
     * @param width  The background width for the string.
     * @param height The background height for the string.
     * @return The {@link Graphics} object.
     */
    public static Graphics drawString(
            final Graphics g,
            final String text,
            final Font font,
            final Color color,
            final int width,
            final int height) {
        // Anti-aliasing
        enableAntialias(g);
        // Create font
        g.setFont(font);

        // Text height (must be called after setting font)
        final int midY = getCenterY(g, height);
        if (null != color) {
            g.setColor(color);
        }

        final int len = text.length();
        final int charWidth = width / len;
        for (int i = 0; i < len; i++) {
            g.drawString(String.valueOf(text.charAt(i)), i * charWidth, midY);
        }
        return g;
    }

    /**
     * Draws a string with default anti-aliasing within a specified rectangle.
     *
     * @param g         The {@link Graphics} object.
     * @param text      The string.
     * @param font      The font.
     * @param color     The font color, or {@code null} for black.
     * @param rectangle The rectangle defining the drawing area and position.
     * @return The {@link Graphics} object.
     */
    public static Graphics drawString(
            final Graphics g,
            final String text,
            final Font font,
            final Color color,
            final Rectangle rectangle) {
        // Background width and height
        final int backgroundWidth = rectangle.width;
        final int backgroundHeight = rectangle.height;

        // Get string dimensions
        Dimension dimension;
        try {
            dimension = getDimension(g.getFontMetrics(font), text);
        } catch (final Exception e) {
            // Handle exceptions like IndexOutOfBoundsException
            dimension = new Dimension(backgroundWidth / 3, backgroundHeight / 3);
        }

        rectangle.setSize(dimension.width, dimension.height);
        final Point point = getPointBaseCentre(rectangle, backgroundWidth, backgroundHeight);

        return drawString(g, text, font, color, point);
    }

    /**
     * Draws a string with default anti-aliasing.
     *
     * @param g     The {@link Graphics} object.
     * @param text  The string.
     * @param font  The font.
     * @param color The font color, or {@code null} for black.
     * @param point The position to draw the string.
     * @return The {@link Graphics} object.
     */
    public static Graphics drawString(
            final Graphics g,
            final String text,
            final Font font,
            final Color color,
            final Point point) {
        // Anti-aliasing
        enableAntialias(g);

        g.setFont(font);
        g.setColor(ObjectKit.defaultIfNull(color, Color.BLACK));
        g.drawString(text, point.x, point.y);

        return g;
    }

    /**
     * Draws an image.
     *
     * @param g     The graphics context.
     * @param img   The image to draw.
     * @param point The top-left position to draw at.
     * @return The {@link Graphics} object.
     */
    public static Graphics drawImg(final Graphics g, final Image img, final Point point) {
        g.drawImage(img, point.x, point.y, null);
        return g;
    }

    /**
     * Draws an image.
     *
     * @param g         The graphics context.
     * @param img       The image to draw.
     * @param rectangle The rectangle defining the drawing area and position.
     * @return The graphics object.
     */
    public static Graphics drawImg(final Graphics g, final Image img, final Rectangle rectangle) {
        g.drawImage(img, rectangle.x, rectangle.y, rectangle.width, rectangle.height, null); // Draw the cropped image
        return g;
    }

    /**
     * Sets the transparency of the graphics context.
     *
     * @param g     The graphics context.
     * @param alpha The transparency, a float between 0.0 and 1.0 (inclusive).
     * @return The graphics context.
     */
    public static Graphics2D setAlpha(final Graphics2D g, final float alpha) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        return g;
    }

    /**
     * Enables anti-aliasing and text anti-aliasing.
     *
     * @param g The {@link Graphics} object.
     */
    private static void enableAntialias(final Graphics g) {
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setRenderingHints(
                    RenderHintsBuilder.of().setAntialiasing(RenderHintsBuilder.Antialias.ON)
                            .setTextAntialias(RenderHintsBuilder.TextAntialias.ON).build());
        }
    }

    /**
     * Gets all fonts available in the system.
     *
     * @return An array of fonts.
     */
    public static Font[] getAllFonts() {
        final GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return e.getAllFonts();
    }

    /**
     * Registers a font with the system.
     *
     * @param font The font to register.
     */
    public static void registerFont(final Font font) {
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(font);
    }

    /**
     * Creates a default font.
     *
     * @return The default font.
     */
    public static Font createFont() {
        return new Font(null);
    }

    /**
     * Creates a SansSerif font.
     *
     * @param size The font size.
     * @return The font.
     */
    public static Font createSansSerifFont(final int size) {
        return createFont(Font.SANS_SERIF, size);
    }

    /**
     * Creates a font with the specified name.
     *
     * @param name The font name.
     * @param size The font size.
     * @return The font.
     */
    public static Font createFont(final String name, final int size) {
        return new Font(name, Font.PLAIN, size);
    }

    /**
     * Creates a font from a file.
     *
     * @param fontFile The font file.
     * @return A {@link Font}.
     */
    public static Font createFont(final File fontFile) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, fontFile);
        } catch (final FontFormatException e) {
            // Use Type1 font if TrueType is invalid
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
     * Creates a font from a stream.
     *
     * @param fontStream The font stream.
     * @return A {@link Font}.
     */
    public static Font createFont(final InputStream fontStream) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, fontStream);
        } catch (final FontFormatException e) {
            // Use Type1 font if TrueType is invalid
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
     * Gets the dimensions of a string with a given font.
     *
     * @param metrics The {@link FontMetrics}.
     * @param text    The string.
     * @return The dimensions.
     */
    public static Dimension getDimension(final FontMetrics metrics, final String text) {
        final int width = metrics.stringWidth(text);
        final int height = metrics.getAscent() - metrics.getLeading() - metrics.getDescent();
        return new Dimension(width, height);
    }

    /**
     * Gets the bounding rectangle of a string with a given font.
     *
     * @param text The string (must not be null).
     * @param font The font (must not be null).
     * @return A {@link Rectangle2D}.
     */
    public static Rectangle2D getRectangle(final String text, final Font font) {
        return font.getStringBounds(text, new FontRenderContext(AffineTransform.getScaleInstance(1, 1), false, false));
    }

}
