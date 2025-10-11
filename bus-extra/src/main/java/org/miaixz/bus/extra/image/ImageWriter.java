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
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * Image writer wrapper.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageWriter implements Flushable {

    private final RenderedImage image;
    private final javax.imageio.ImageWriter writer;
    private ImageWriteParam writeParam;

    /**
     * Constructor.
     *
     * @param image     The {@link Image}.
     * @param imageType The image type (extension), or {@code null} for RGB mode (JPG).
     */
    public ImageWriter(final Image image, final String imageType) {
        this.image = ImageKit.castToRenderedImage(image, imageType);
        this.writer = ImageKit.getWriter(image, imageType);
    }

    /**
     * Creates an ImageWriter.
     *
     * @param image           The image.
     * @param imageType       The image type (extension), or {@code null} for RGB mode (JPG).
     * @param backgroundColor The background color {@link Color}, or {@code null} for black or transparent.
     * @return An {@code ImageWriter}.
     */
    public static ImageWriter of(final Image image, final String imageType, final Color backgroundColor) {
        return of(ImageKit.toBufferedImage(image, imageType, backgroundColor), imageType);
    }

    /**
     * Creates an ImageWriter.
     *
     * @param image     The image.
     * @param imageType The image type (extension), or {@code null} for RGB mode (JPG).
     * @return An {@code ImageWriter}.
     */
    public static ImageWriter of(final Image image, final String imageType) {
        return new ImageWriter(image, imageType);
    }

    /**
     * Builds image write parameters.
     *
     * @param renderedImage The image.
     * @param writer        The {@link javax.imageio.ImageWriter}.
     * @param quality       The quality, from 0 to 1.
     * @return An {@link ImageWriteParam} or {@code null}.
     */
    private static ImageWriteParam buildParam(
            final RenderedImage renderedImage,
            final javax.imageio.ImageWriter writer,
            final float quality) {
        // Set quality
        ImageWriteParam imgWriteParams = null;
        if (quality > 0 && quality < 1) {
            imgWriteParams = writer.getDefaultWriteParam();
            if (imgWriteParams.canWriteCompressed()) {
                imgWriteParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                imgWriteParams.setCompressionQuality(quality);
                final ColorModel colorModel = renderedImage.getColorModel();
                imgWriteParams.setDestinationType(
                        new ImageTypeSpecifier(colorModel, colorModel.createCompatibleSampleModel(16, 16)));
            }
        }
        return imgWriteParams;
    }

    /**
     * Sets the write quality. A value between 0 and 1 (exclusive) indicates a compression ratio; other values indicate
     * no compression.
     *
     * @param quality The write quality, a value between 0 and 1 (exclusive) for compression.
     * @return this
     */
    public ImageWriter setQuality(final float quality) {
        this.writeParam = buildParam(this.image, this.writer, quality);
        return this;
    }

    /**
     * Writes the image. This method does not close the stream.
     *
     * @param out The target output stream.
     * @throws InternalException if an I/O error occurs.
     */
    public void write(final OutputStream out) throws InternalException {
        write(ImageKit.getImageOutputStream(out));
    }

    /**
     * Writes the image in the format corresponding to the destination file's extension.
     *
     * @param destFile The destination file.
     * @throws InternalException if an I/O error occurs.
     */
    public void write(final File destFile) throws InternalException {
        FileKit.touch(destFile);
        ImageOutputStream out = null;
        try {
            out = ImageKit.getImageOutputStream(destFile);
            write(out);
        } finally {
            IoKit.closeQuietly(out);
        }
    }

    /**
     * Writes the image to an output stream using an {@link javax.imageio.ImageWriter}.
     *
     * @param output The {@link ImageOutputStream} to write to (not null).
     */
    public void write(final ImageOutputStream output) {
        Assert.notNull(output);

        final javax.imageio.ImageWriter writer = this.writer;
        final RenderedImage image = this.image;
        writer.setOutput(output);
        // Set quality
        try {
            if (null != this.writeParam) {
                writer.write(null, new IIOImage(image, null, null), this.writeParam);
            } else {
                writer.write(image);
            }
            output.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            writer.dispose();
            // FileCacheImageOutputStream creates a temporary file, which is cleaned up here
            IoKit.closeQuietly(output);
        }
    }

    @Override
    public void flush() {
        final RenderedImage renderedImage = this.image;
        if (renderedImage instanceof BufferedImage) {
            ImageKit.flush((BufferedImage) renderedImage);
        } else if (renderedImage instanceof Image) {
            ImageKit.flush((Image) renderedImage);
        }
    }

}
