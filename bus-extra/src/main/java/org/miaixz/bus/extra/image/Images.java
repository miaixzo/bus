/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.ImageFilter;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * An image editor for performing various image manipulations. This class provides a fluent API for chaining operations
 * like scaling, cutting, watermarking, and more.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Images implements Flushable, Serializable {

    @Serial
    private static final long serialVersionUID = 2852293153163L;

    /**
     * The source image.
     */
    private final BufferedImage srcImage;
    /**
     * The target image.
     */
    private Image targetImage;
    /**
     * The target image type.
     */
    private String targetImageType;
    /**
     * Whether to position from the center.
     */
    private boolean positionBaseCentre = true;
    /**
     * The quality of the output image.
     */
    private float quality = -1;
    /**
     * The background color.
     */
    private Color backgroundColor;

    /**
     * Constructs a new {@code Images} instance from a {@link BufferedImage}. The target image type is determined by the
     * source image type.
     *
     * @param srcImage The source {@link BufferedImage}.
     */
    public Images(final BufferedImage srcImage) {
        this(srcImage, null);
    }

    /**
     * Constructs a new {@code Images} instance.
     *
     * @param srcImage        The source {@link BufferedImage}.
     * @param targetImageType The target image type (e.g., "jpg", "png"). If null, it is inferred from the source image.
     */
    public Images(final BufferedImage srcImage, String targetImageType) {
        this.srcImage = srcImage;
        if (null == targetImageType) {
            if (srcImage.getType() == BufferedImage.TYPE_INT_ARGB
                    || srcImage.getType() == BufferedImage.TYPE_INT_ARGB_PRE
                    || srcImage.getType() == BufferedImage.TYPE_4BYTE_ABGR
                    || srcImage.getType() == BufferedImage.TYPE_4BYTE_ABGR_PRE) {
                targetImageType = ImageKit.IMAGE_TYPE_PNG;
            } else {
                targetImageType = ImageKit.IMAGE_TYPE_JPG;
            }
        }
        this.targetImageType = targetImageType;
    }

    /**
     * Creates an {@code Images} instance from an image file path.
     *
     * @param imagePath The path to the image file.
     * @return A new {@code Images} instance.
     */
    public static Images from(final Path imagePath) {
        return from(imagePath.toFile());
    }

    /**
     * Creates an {@code Images} instance from an image file.
     *
     * @param imageFile The image file.
     * @return A new {@code Images} instance.
     */
    public static Images from(final File imageFile) {
        return new Images(ImageKit.read(imageFile));
    }

    /**
     * Creates an {@code Images} instance from a resource.
     *
     * @param resource The image resource.
     * @return A new {@code Images} instance.
     */
    public static Images from(final Resource resource) {
        return from(resource.getStream());
    }

    /**
     * Creates an {@code Images} instance from an input stream.
     *
     * @param in The input stream of the image.
     * @return A new {@code Images} instance.
     */
    public static Images from(final InputStream in) {
        return new Images(ImageKit.read(in));
    }

    /**
     * Creates an {@code Images} instance from an {@link ImageInputStream}.
     *
     * @param imageStream The image input stream.
     * @return A new {@code Images} instance.
     */
    public static Images from(final ImageInputStream imageStream) {
        return new Images(ImageKit.read(imageStream));
    }

    /**
     * Creates an {@code Images} instance from a URL.
     *
     * @param imageUrl The URL of the image.
     * @return A new {@code Images} instance.
     */
    public static Images from(final URL imageUrl) {
        return new Images(ImageKit.read(imageUrl));
    }

    /**
     * Creates an {@code Images} instance from an {@link Image}.
     *
     * @param image The image.
     * @return A new {@code Images} instance.
     */
    public static Images from(final Image image) {
        return new Images(ImageKit.castToBufferedImage(image, ImageKit.IMAGE_TYPE_JPG));
    }

    /**
     * Calculates the dimensions of an image after rotation.
     *
     * @param width  The original width.
     * @param height The original height.
     * @param degree The rotation angle in degrees.
     * @return A {@link Rectangle} representing the new dimensions.
     */
    private static Rectangle calcRotatedSize(int width, int height, int degree) {
        if (degree < 0) {
            degree += 360;
        }
        if (degree >= 90) {
            if (degree / 90 % 2 == 1) {
                final int temp = height;
                height = width;
                width = temp;
            }
            degree = degree % 90;
        }
        final double r = Math.sqrt(height * height + width * width) / 2;
        final double len = 2 * Math.sin(Math.toRadians(degree) / 2) * r;
        final double angel_alpha = (Math.PI - Math.toRadians(degree)) / 2;
        final double angel_dalta_width = Math.atan((double) height / width);
        final double angel_dalta_height = Math.atan((double) width / height);
        final int len_dalta_width = (int) (len * Math.cos(Math.PI - angel_alpha - angel_dalta_width));
        final int len_dalta_height = (int) (len * Math.cos(Math.PI - angel_alpha - angel_dalta_height));
        final int des_width = width + len_dalta_width * 2;
        final int des_height = height + len_dalta_height * 2;

        return new Rectangle(des_width, des_height);
    }

    /**
     * Sets the target image file format for writing.
     *
     * @param imgType The image format (e.g., "jpg", "png").
     * @return This {@code Images} instance for method chaining.
     */
    public Images setTargetImageType(final String imgType) {
        this.targetImageType = imgType;
        return this;
    }

    /**
     * Sets whether to calculate x, y coordinates from the center as the origin.
     *
     * @param positionBaseCentre {@code true} to use the center as the origin, {@code false} for the top-left corner.
     * @return This {@code Images} instance for method chaining.
     */
    public Images setPositionBaseCentre(final boolean positionBaseCentre) {
        this.positionBaseCentre = positionBaseCentre;
        return this;
    }

    /**
     * Sets the image output quality for compression (0.0 to 1.0).
     *
     * @param quality The quality, a float between 0.0 and 1.0.
     * @return This {@code Images} instance for method chaining.
     */
    public Images setQuality(final double quality) {
        return setQuality((float) quality);
    }

    /**
     * Sets the image output quality for compression (0.0 to 1.0).
     *
     * @param quality The quality, a float between 0.0 and 1.0.
     * @return This {@code Images} instance for method chaining.
     */
    public Images setQuality(final float quality) {
        if (quality > 0 && quality < 1) {
            this.quality = quality;
        } else {
            this.quality = 1;
        }
        return this;
    }

    /**
     * Sets the background color of the image.
     *
     * @param backgroundColor The background color.
     * @return This {@code Images} instance for method chaining.
     */
    public Images setBackgroundColor(final Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    /**
     * Scales the image by a given ratio.
     *
     * @param scale The scaling ratio. Greater than 1 for enlargement, between 0 and 1 for reduction.
     * @return This {@code Images} instance for method chaining.
     */
    public Images scale(float scale) {
        if (scale < 0) {
            scale = -scale;
        }
        final Image srcImg = getValidSrcImg();

        if (ImageKit.IMAGE_TYPE_PNG.equals(this.targetImageType)) {
            final double scaleDouble = MathKit.toDouble(scale);
            this.targetImage = ImageKit.transform(
                    AffineTransform.getScaleInstance(scaleDouble, scaleDouble),
                    ImageKit.toBufferedImage(srcImg, this.targetImageType));
        } else {
            final int width = MathKit.mul(srcImg.getWidth(null), scale).intValue();
            final int height = MathKit.mul(srcImg.getHeight(null), scale).intValue();
            scale(width, height);
        }
        return this;
    }

    /**
     * Scales the image to the specified width and height. This may cause distortion.
     *
     * @param width  The target width.
     * @param height The target height.
     * @return This {@code Images} instance for method chaining.
     */
    public Images scale(final int width, final int height) {
        return scale(width, height, Image.SCALE_SMOOTH);
    }

    /**
     * Scales the image to the specified width and height with a specific scaling algorithm.
     *
     * @param width     The target width.
     * @param height    The target height.
     * @param scaleType The scaling algorithm (e.g., {@link Image#SCALE_SMOOTH}).
     * @return This {@code Images} instance for method chaining.
     */
    public Images scale(final int width, final int height, final int scaleType) {
        final Image srcImg = getValidSrcImg();

        final int srcHeight = srcImg.getHeight(null);
        final int srcWidth = srcImg.getWidth(null);
        if (srcHeight == height && srcWidth == width) {
            this.targetImage = srcImg;
            return this;
        }

        if (ImageKit.IMAGE_TYPE_PNG.equals(this.targetImageType)) {
            final double sx = MathKit.div(width, srcWidth).doubleValue();
            final double sy = MathKit.div(height, srcHeight).doubleValue();
            this.targetImage = ImageKit.transform(
                    AffineTransform.getScaleInstance(sx, sy),
                    ImageKit.toBufferedImage(srcImg, this.targetImageType));
        } else {
            this.targetImage = srcImg.getScaledInstance(width, height, scaleType);
        }

        return this;
    }

    /**
     * Scales the image proportionally to fit within the given dimensions, filling blank space with a fixed color.
     *
     * @param width      The target width.
     * @param height     The target height.
     * @param fixedColor The color to fill the blank space, or null for no fill.
     * @return This {@code Images} instance for method chaining.
     */
    public Images scale(final int width, final int height, final Color fixedColor) {
        Image srcImage = getValidSrcImg();
        int srcHeight = srcImage.getHeight(null);
        int srcWidth = srcImage.getWidth(null);
        final double heightRatio = MathKit.div(height, srcHeight).doubleValue();
        final double widthRatio = MathKit.div(width, srcWidth).doubleValue();

        if (MathKit.equals(heightRatio, widthRatio)) {
            scale(width, height);
        } else if (widthRatio < heightRatio) {
            scale(width, (int) (srcHeight * widthRatio));
        } else {
            scale((int) (srcWidth * heightRatio), height);
        }

        srcImage = getValidSrcImg();
        srcHeight = srcImage.getHeight(null);
        srcWidth = srcImage.getWidth(null);

        final BufferedImage image = new BufferedImage(width, height, getTypeInt());
        final Graphics2D g = image.createGraphics();

        if (null != fixedColor) {
            g.setBackground(fixedColor);
            g.clearRect(0, 0, width, height);
        }

        g.drawImage(srcImage, (width - srcWidth) / 2, (height - srcHeight) / 2, srcWidth, srcHeight, fixedColor, null);

        g.dispose();
        this.targetImage = image;
        return this;
    }

    /**
     * Crops the image to the specified rectangle.
     *
     * @param rectangle The {@link Rectangle} defining the crop area.
     * @return This {@code Images} instance for method chaining.
     */
    public Images cut(final Rectangle rectangle) {
        final Image srcImage = getValidSrcImg();
        fixRectangle(rectangle, srcImage.getWidth(null), srcImage.getHeight(null));

        final ImageFilter cropFilter = new CropImageFilter(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        this.targetImage = ImageKit.filter(cropFilter, srcImage);
        return this;
    }

    /**
     * Cuts a circular area from the image, with the diameter being the smaller of the image's width and height.
     *
     * @param x The starting x-coordinate.
     * @param y The starting y-coordinate.
     * @return This {@code Images} instance for method chaining.
     */
    public Images cut(final int x, final int y) {
        return cut(x, y, -1);
    }

    /**
     * Cuts a circular area from the image with a specified radius.
     *
     * @param x      The starting x-coordinate.
     * @param y      The starting y-coordinate.
     * @param radius The radius of the circle. If less than 0, the diameter is the smaller of the image's width and
     *               height.
     * @return This {@code Images} instance for method chaining.
     */
    public Images cut(int x, int y, final int radius) {
        final Image srcImage = getValidSrcImg();
        final int width = srcImage.getWidth(null);
        final int height = srcImage.getHeight(null);

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
     * Applies rounded corners to the image.
     *
     * @param arc The arc ratio for the corners (0.0 to 1.0), relative to the smaller of the image's width and height.
     * @return This {@code Images} instance for method chaining.
     */
    public Images round(double arc) {
        final Image srcImage = getValidSrcImg();
        final int width = srcImage.getWidth(null);
        final int height = srcImage.getHeight(null);

        arc = MathKit.mul(arc, Math.min(width, height)).doubleValue();

        final BufferedImage targetImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = targetImage.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, arc, arc));
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(srcImage, 0, 0, null);
        g2.dispose();
        this.targetImage = targetImage;
        return this;
    }

    /**
     * Converts the image to grayscale.
     *
     * @return This {@code Images} instance for method chaining.
     */
    public Images gray() {
        this.targetImage = ImageKit.colorConvert(ColorSpace.getInstance(ColorSpace.CS_GRAY), getValidSrcBufferedImg());
        return this;
    }

    /**
     * Converts the image to a binary (black and white) image.
     *
     * @return This {@code Images} instance for method chaining.
     */
    public Images binary() {
        this.targetImage = ImageKit.copyImage(getValidSrcImg(), BufferedImage.TYPE_BYTE_BINARY);
        return this;
    }

    /**
     * Adds a text watermark to the image.
     *
     * @param pressText The watermark text.
     * @param color     The color of the text.
     * @param font      The font of the text.
     * @param x         The x-coordinate offset.
     * @param y         The y-coordinate offset.
     * @param alpha     The opacity of the text (0.0 to 1.0).
     * @return This {@code Images} instance for method chaining.
     */
    public Images pressText(
            final String pressText,
            final Color color,
            final Font font,
            final int x,
            final int y,
            final float alpha) {
        return pressText(ImageText.of(pressText, color, font, new Point(x, y), alpha));
    }

    /**
     * Adds a text watermark to the image using an {@link ImageText} object.
     *
     * @param imageText The {@link ImageText} object containing all text watermark information.
     * @return This {@code Images} instance for method chaining.
     */
    public Images pressText(final ImageText imageText) {
        final BufferedImage targetImage = ImageKit.toBufferedImage(getValidSrcImg(), this.targetImageType);

        Font font = imageText.getFont();
        if (null == font) {
            font = ImageKit.createSansSerifFont((int) (targetImage.getHeight() * 0.75));
        }

        final Graphics2D g = targetImage.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, imageText.getAlpha()));

        final Point point = imageText.getPoint();
        if (positionBaseCentre) {
            ImageKit.drawString(
                    g,
                    imageText.getPressText(),
                    font,
                    imageText.getColor(),
                    new Rectangle(point.x, point.y, targetImage.getWidth(), targetImage.getHeight()));
        } else {
            ImageKit.drawString(g, imageText.getPressText(), font, imageText.getColor(), point);
        }

        g.dispose();
        this.targetImage = targetImage;

        return this;
    }

    /**
     * Adds a full-screen text watermark to the image.
     *
     * @param pressText  The watermark text.
     * @param color      The color of the text.
     * @param font       The font of the text.
     * @param lineHeight The line height.
     * @param degree     The rotation angle in degrees.
     * @param alpha      The opacity of the text (0.0 to 1.0).
     * @return This {@code Images} instance for method chaining.
     */
    public Images pressTextFull(
            final String pressText,
            final Color color,
            Font font,
            final int lineHeight,
            final int degree,
            final float alpha) {
        final BufferedImage targetImage = ImageKit.toBufferedImage(getValidSrcImg(), this.targetImageType);

        if (null == font) {
            font = ImageKit.createSansSerifFont((int) (targetImage.getHeight() * 0.75));
        }
        final int targetHeight = targetImage.getHeight();
        final int targetWidth = targetImage.getWidth();

        final Graphics2D g = targetImage.createGraphics();
        g.setColor(color);
        g.rotate(Math.toRadians(degree), targetWidth >> 1, targetHeight >> 1);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));

        Dimension dimension;
        try {
            dimension = ImageKit.getDimension(g.getFontMetrics(font), pressText);
        } catch (final Exception e) {
            dimension = new Dimension(targetWidth / 3, targetHeight / 3);
        }
        final int intervalHeight = dimension.height * lineHeight;
        int y = -targetHeight >> 1;
        while (y < targetHeight * 1.5) {
            int x = -targetWidth >> 1;
            while (x < targetWidth * 1.5) {
                ImageKit.drawString(g, pressText, font, color, new Point(x, y));
                x += dimension.width;
            }
            y += intervalHeight;
        }
        g.dispose();

        this.targetImage = targetImage;
        return this;
    }

    /**
     * Adds an image watermark to the image.
     *
     * @param pressImg The watermark image.
     * @param x        The x-coordinate offset.
     * @param y        The y-coordinate offset.
     * @param alpha    The opacity of the watermark (0.0 to 1.0).
     * @return This {@code Images} instance for method chaining.
     */
    public Images pressImage(final Image pressImg, final int x, final int y, final float alpha) {
        final int pressImgWidth = pressImg.getWidth(null);
        final int pressImgHeight = pressImg.getHeight(null);
        return pressImage(pressImg, new Rectangle(x, y, pressImgWidth, pressImgHeight), alpha);
    }

    /**
     * Adds an image watermark to the image within a specified rectangle.
     *
     * @param pressImg  The watermark image.
     * @param rectangle The {@link Rectangle} defining the position and size of the watermark.
     * @param alpha     The opacity of the watermark (0.0 to 1.0).
     * @return This {@code Images} instance for method chaining.
     */
    public Images pressImage(final Image pressImg, final Rectangle rectangle, final float alpha) {
        final Image targetImg = getValidSrcImg();

        this.targetImage = draw(ImageKit.toBufferedImage(targetImg, this.targetImageType), pressImg, rectangle, alpha);
        return this;
    }

    /**
     * Adds a full-screen image watermark to the image.
     *
     * @param pressImage The watermark image.
     * @param lineHeight The line height.
     * @param degree     The rotation angle in degrees.
     * @param alpha      The opacity of the watermark (0.0 to 1.0).
     * @return This {@code Images} instance for method chaining.
     */
    public Images pressImageFull(final Image pressImage, final int lineHeight, final int degree, final float alpha) {
        final BufferedImage targetImage = ImageKit.toBufferedImage(getValidSrcImg(), this.targetImageType);

        final int targetHeight = targetImage.getHeight();
        final int targetWidth = targetImage.getWidth();

        final Graphics2D g = targetImage.createGraphics();
        g.rotate(Math.toRadians(degree), targetWidth >> 1, targetHeight >> 1);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));

        final int pressImageWidth = pressImage.getWidth(null);
        final int pressImageHeight = pressImage.getHeight(null);
        final Dimension dimension = new Dimension(pressImageWidth, pressImageHeight);
        final int intervalHeight = dimension.height * lineHeight;
        int y = -targetHeight >> 1;
        while (y < targetHeight * 1.5) {
            int x = -targetWidth >> 1;
            while (x < targetWidth * 1.5) {
                ImageKit.drawImg(g, pressImage, new Point(x, y));
                x += dimension.width;
            }
            y += intervalHeight;
        }
        g.dispose();
        this.targetImage = targetImage;
        return this;
    }

    /**
     * Rotates the image by a specified angle.
     *
     * @param degree The rotation angle in degrees.
     * @return This {@code Images} instance for method chaining.
     */
    public Images rotate(final int degree) {
        if (0 == degree) {
            return this;
        }
        final Image image = getValidSrcImg();
        final int width = image.getWidth(null);
        final int height = image.getHeight(null);
        final Rectangle rectangle = calcRotatedSize(width, height, degree);

        final BufferedImage targetImg = new BufferedImage(rectangle.width, rectangle.height, getTypeInt());
        final Graphics2D graphics2d = ImageKit.createGraphics(targetImg, this.backgroundColor);

        graphics2d.setRenderingHints(
                RenderHintsBuilder.of().setAntialiasing(RenderHintsBuilder.Antialias.ON)
                        .setInterpolation(RenderHintsBuilder.Interpolation.BILINEAR).build());

        graphics2d.translate((rectangle.width - width) / 2D, (rectangle.height - height) / 2D);
        graphics2d.rotate(Math.toRadians(degree), width / 2D, height / 2D);

        graphics2d.drawImage(image, 0, 0, null);
        graphics2d.dispose();
        this.targetImage = targetImg;
        return this;
    }

    /**
     * Flips the image horizontally.
     *
     * @return This {@code Images} instance for method chaining.
     */
    public Images flip() {
        final Image image = getValidSrcImg();
        final int width = image.getWidth(null);
        final int height = image.getHeight(null);
        final BufferedImage targetImg = new BufferedImage(width, height, getTypeInt());
        final Graphics2D graphics2d = targetImg.createGraphics();
        graphics2d.drawImage(image, 0, 0, width, height, width, 0, 0, height, null);
        graphics2d.dispose();

        this.targetImage = targetImg;
        return this;
    }

    /**
     * Adds a stroke (border) to the image.
     *
     * @param color The color of the stroke.
     * @param width The width of the stroke.
     * @return This {@code Images} instance for method chaining.
     */
    public Images stroke(final Color color, final float width) {
        return stroke(color, new BasicStroke(width));
    }

    /**
     * Adds a stroke (border) to the image with a specified {@link Stroke} object.
     *
     * @param color  The color of the stroke.
     * @param stroke The {@link Stroke} object defining the border properties.
     * @return This {@code Images} instance for method chaining.
     */
    public Images stroke(final Color color, final Stroke stroke) {
        final BufferedImage image = ImageKit.toBufferedImage(getValidSrcImg(), this.targetImageType);
        final int width = image.getWidth();
        final int height = image.getHeight();
        final Graphics2D g = image.createGraphics();

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
     * Retrieves the processed image.
     *
     * @return The processed {@link Image}.
     */
    public Image getImg() {
        return getValidSrcImg();
    }

    /**
     * Writes the processed image to an {@link OutputStream}.
     *
     * @param out The output stream to write to.
     * @return This {@code Images} instance for method chaining.
     * @throws InternalException if an I/O error occurs.
     */
    public Images write(final OutputStream out) throws InternalException {
        write(ImageKit.getImageOutputStream(out));
        return this;
    }

    /**
     * Writes the processed image to an {@link ImageOutputStream}.
     *
     * @param targetImageStream The target image output stream.
     * @return This {@code Images} instance for method chaining.
     * @throws InternalException if an I/O error occurs.
     */
    public Images write(final ImageOutputStream targetImageStream) throws InternalException {
        Assert.notBlank(this.targetImageType, "Target image type is blank !");
        Assert.notNull(targetImageStream, "Target output stream is null !");

        final Image targetImage = (null == this.targetImage) ? this.srcImage : this.targetImage;
        Assert.notNull(targetImage, "Target image is null !");

        ImageKit.write(targetImage, this.targetImageType, targetImageStream, this.quality, this.backgroundColor);

        return this;
    }

    /**
     * Writes the processed image to a file.
     *
     * @param destFile The destination file.
     * @return This {@code Images} instance for method chaining.
     * @throws InternalException if an I/O error occurs.
     */
    public Images write(final File destFile) throws InternalException {
        final String formatName = FileName.extName(destFile);
        if (StringKit.isNotBlank(formatName)) {
            this.targetImageType = formatName;
        }

        if (destFile.exists()) {
            destFile.delete();
        }

        ImageOutputStream out = null;
        try {
            out = ImageKit.getImageOutputStream(destFile);
            write(out);
        } finally {
            IoKit.closeQuietly(out);
        }
        return this;
    }

    /**
     * Flushes the source and target images to free resources. This method is designed to be overridden by subclasses
     * for custom resource cleanup. When overriding, ensure all image resources are properly released and the method is
     * idempotent (safe to call multiple times).
     *
     * Subclasses should call {@code super.flush()} to ensure proper cleanup of inherited resources.
     */
    @Override
    public void flush() {
        ImageKit.flush(this.srcImage);
        ImageKit.flush(this.targetImage);
    }

    /**
     * Draws an image onto a background image.
     *
     * @param backgroundImg The background image.
     * @param img           The image to draw.
     * @param rectangle     The position and size of the image to draw.
     * @param alpha         The opacity of the image.
     * @return The resulting image.
     */
    private BufferedImage draw(
            final BufferedImage backgroundImg,
            final Image img,
            final Rectangle rectangle,
            final float alpha) {
        final Graphics2D g = backgroundImg.createGraphics();
        ImageKit.setAlpha(g, alpha);

        fixRectangle(rectangle, backgroundImg.getWidth(), backgroundImg.getHeight());
        ImageKit.drawImg(g, img, rectangle);

        g.dispose();
        return backgroundImg;
    }

    /**
     * Gets the integer representation of the image type.
     *
     * @return The image type as an integer.
     */
    private int getTypeInt() {
        return switch (this.targetImageType) {
            case ImageKit.IMAGE_TYPE_PNG -> BufferedImage.TYPE_INT_ARGB;
            default -> BufferedImage.TYPE_INT_RGB;
        };
    }

    /**
     * Gets the valid source image, which is the result of the previous operation or the original source image.
     *
     * @return The valid source image.
     */
    private Image getValidSrcImg() {
        return ObjectKit.defaultIfNull(this.targetImage, this.srcImage);
    }

    /**
     * Gets the valid source image as a {@link BufferedImage}.
     *
     * @return The valid source image as a {@link BufferedImage}.
     */
    private BufferedImage getValidSrcBufferedImg() {
        return ImageKit.toBufferedImage(getValidSrcImg(), this.targetImageType);
    }

    /**
     * Adjusts the rectangle's position based on whether the coordinate system is center-based.
     *
     * @param rectangle  The rectangle to adjust.
     * @param baseWidth  The reference width.
     * @param baseHeight The reference height.
     * @return The adjusted {@link Rectangle}.
     */
    private Rectangle fixRectangle(final Rectangle rectangle, final int baseWidth, final int baseHeight) {
        if (this.positionBaseCentre) {
            final Point pointBaseCentre = ImageKit.getPointBaseCentre(rectangle, baseWidth, baseHeight);
            rectangle.setLocation(pointBaseCentre.x, pointBaseCentre.y);
        }
        return rectangle;
    }

}
