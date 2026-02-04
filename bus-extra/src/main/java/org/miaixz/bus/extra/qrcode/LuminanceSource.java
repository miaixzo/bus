/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.qrcode;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * {@link BufferedImage} QR code luminance source.
 * <p>
 * Adapted from: http://blog.csdn.net/yangxin_blog/article/details/50850701 This class is also provided in the
 * zxing-j2se package.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class LuminanceSource extends com.google.zxing.LuminanceSource {

    /**
     * The source buffered image.
     */
    private final BufferedImage image;
    /**
     * The left offset of the region.
     */
    private final int left;
    /**
     * The top offset of the region.
     */
    private final int top;

    /**
     * Constructs a new LuminanceSource from the entire image.
     *
     * @param image the {@link BufferedImage} to read
     */
    public LuminanceSource(final BufferedImage image) {
        this(image, 0, 0, image.getWidth(), image.getHeight());
    }

    /**
     * Constructs a new LuminanceSource from a region of the image.
     *
     * @param image  the {@link BufferedImage} to read
     * @param left   the left offset of the region
     * @param top    the top offset of the region
     * @param width  the width of the region
     * @param height the height of the region
     */
    public LuminanceSource(final BufferedImage image, final int left, final int top, final int width,
            final int height) {
        super(width, height);

        final int sourceWidth = image.getWidth();
        final int sourceHeight = image.getHeight();
        if (left + width > sourceWidth || top + height > sourceHeight) {
            throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
        }

        for (int y = top; y < top + height; y++) {
            for (int x = left; x < left + width; x++) {
                if ((image.getRGB(x, y) & 0xFF000000) == 0) {
                    image.setRGB(x, y, 0xFFFFFFFF); // = white
                }
            }
        }

        this.image = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_BYTE_GRAY);
        this.image.getGraphics().drawImage(image, 0, 0, null);
        this.left = left;
        this.top = top;
    }

    @Override
    public byte[] getRow(final int y, byte[] row) {
        if (y < 0 || y >= getHeight()) {
            throw new IllegalArgumentException("Requested row is outside the image: " + y);
        }
        final int width = getWidth();
        if (row == null || row.length < width) {
            row = new byte[width];
        }
        image.getRaster().getDataElements(left, top + y, width, 1, row);
        return row;
    }

    @Override
    public byte[] getMatrix() {
        final int width = getWidth();
        final int height = getHeight();
        final int area = width * height;
        final byte[] matrix = new byte[area];
        image.getRaster().getDataElements(left, top, width, height, matrix);
        return matrix;
    }

    @Override
    public boolean isCropSupported() {
        return true;
    }

    @Override
    public com.google.zxing.LuminanceSource crop(final int left, final int top, final int width, final int height) {
        return new LuminanceSource(image, this.left + left, this.top + top, width, height);
    }

    @Override
    public boolean isRotateSupported() {
        return true;
    }

    @Override
    public com.google.zxing.LuminanceSource rotateCounterClockwise() {

        final int sourceWidth = image.getWidth();
        final int sourceHeight = image.getHeight();

        final AffineTransform transform = new AffineTransform(0.0, -1.0, 1.0, 0.0, 0.0, sourceWidth);

        final BufferedImage rotatedImage = new BufferedImage(sourceHeight, sourceWidth, BufferedImage.TYPE_BYTE_GRAY);

        final Graphics2D g = rotatedImage.createGraphics();
        g.drawImage(image, transform, null);
        g.dispose();

        final int width = getWidth();
        return new LuminanceSource(rotatedImage, top, sourceWidth - (left + width), getHeight(), width);
    }

}
