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
package org.miaixz.bus.extra.qrcode.render;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

import org.miaixz.bus.extra.image.ImageKit;
import org.miaixz.bus.extra.image.Images;
import org.miaixz.bus.extra.qrcode.QrConfig;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;

/**
 * QR code image renderer.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageRender implements BitMatrixRender {

    /**
     * The QR code configuration used for rendering.
     */
    private final QrConfig config;
    /**
     * The image type (e.g., "png", "jpg").
     */
    private final String imageType;

    /**
     * Constructs an {@code ImageRender} with the specified QR code configuration and image type.
     *
     * @param config    The QR code configuration.
     * @param imageType The type of the image (e.g., "png", "jpg").
     */
    public ImageRender(final QrConfig config, final String imageType) {
        this.config = config;
        this.imageType = imageType;
    }

    /**
     * Description inherited from parent class or interface.
     *
     */
    @Override
    public void render(final BitMatrix matrix, final OutputStream out) {
        BufferedImage img = null;
        try {
            img = render(matrix);
            ImageKit.write(img, imageType, out);
        } finally {
            ImageKit.flush(img);
        }
    }

    /**
     * Renders the given {@link BitMatrix} into a {@link BufferedImage}.
     *
     * @param matrix The {@link BitMatrix} representing the QR code.
     * @return The rendered {@link BufferedImage}.
     */
    public BufferedImage render(final BitMatrix matrix) {
        final BufferedImage image = getBufferedImage(matrix);

        final Image logo = config.getImg();
        if (null != logo && BarcodeFormat.QR_CODE == config.getFormat()) {
            pressLogo(image, logo);
        }
        return image;
    }

    /**
     * Retrieves a {@link BufferedImage} from the given {@link BitMatrix}.
     *
     * @param matrix The {@link BitMatrix} to convert.
     * @return The {@link BufferedImage} representation of the {@link BitMatrix}.
     */
    private BufferedImage getBufferedImage(final BitMatrix matrix) {
        final BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(),
                null == config.getBackColor() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

        final int width = matrix.getWidth();
        final int height = matrix.getHeight();
        final Integer foreColor = config.getForeColor();
        final Integer backColor = config.getBackColor();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (matrix.get(x, y)) {
                    image.setRGB(x, y, foreColor);
                } else if (null != backColor) {
                    image.setRGB(x, y, backColor);
                }
            }
        }
        return image;
    }

    /**
     * Presses a logo image onto the QR code image.
     *
     * @param image   The QR code image onto which the logo will be pressed.
     * @param logoImg The logo image to press.
     */
    private void pressLogo(final BufferedImage image, final Image logoImg) {
        // Only QR codes can have logos pressed onto them
        final int qrWidth = image.getWidth();
        final int qrHeight = image.getHeight();
        final int imgWidth;
        final int imgHeight;
        // Scale proportionally based on the shortest side
        if (qrWidth < qrHeight) {
            imgWidth = qrWidth / config.getRatio();
            imgHeight = logoImg.getHeight(null) * imgWidth / logoImg.getWidth(null);
        } else {
            imgHeight = qrHeight / config.getRatio();
            imgWidth = logoImg.getWidth(null) * imgHeight / logoImg.getHeight(null);
        }

        // Draw watermark directly on the original image
        Images.from(image).pressImage(//
                Images.from(logoImg).round(config.getImgRound()).getImg(), // Rounded corners
                new Rectangle(imgWidth, imgHeight), // Position
                1// Opacity
        );
    }

}
