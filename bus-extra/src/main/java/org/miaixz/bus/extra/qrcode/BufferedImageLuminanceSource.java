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
package org.miaixz.bus.extra.qrcode;

import com.google.zxing.LuminanceSource;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * {@link BufferedImage} 图片二维码源 来自：http://blog.csdn.net/yangxin_blog/article/details/50850701 此类同样在zxing-j2se包中也有提供
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class BufferedImageLuminanceSource extends LuminanceSource {

    private final BufferedImage image;
    private final int left;
    private final int top;

    /**
     * 构造
     *
     * @param image {@link BufferedImage}
     */
    public BufferedImageLuminanceSource(final BufferedImage image) {
        this(image, 0, 0, image.getWidth(), image.getHeight());
    }

    /**
     * 构造
     *
     * @param image  {@link BufferedImage}
     * @param left   左边间隔
     * @param top    顶部间隔
     * @param width  宽度
     * @param height 高度
     */
    public BufferedImageLuminanceSource(final BufferedImage image, final int left, final int top, final int width,
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
    public LuminanceSource crop(final int left, final int top, final int width, final int height) {
        return new BufferedImageLuminanceSource(image, this.left + left, this.top + top, width, height);
    }

    @Override
    public boolean isRotateSupported() {
        return true;
    }

    @Override
    public LuminanceSource rotateCounterClockwise() {

        final int sourceWidth = image.getWidth();
        final int sourceHeight = image.getHeight();

        final AffineTransform transform = new AffineTransform(0.0, -1.0, 1.0, 0.0, 0.0, sourceWidth);

        final BufferedImage rotatedImage = new BufferedImage(sourceHeight, sourceWidth, BufferedImage.TYPE_BYTE_GRAY);

        final Graphics2D g = rotatedImage.createGraphics();
        g.drawImage(image, transform, null);
        g.dispose();

        final int width = getWidth();
        return new BufferedImageLuminanceSource(rotatedImage, top, sourceWidth - (left + width), getHeight(), width);
    }

}
