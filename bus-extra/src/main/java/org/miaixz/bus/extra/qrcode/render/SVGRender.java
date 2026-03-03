/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.qrcode.render;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ColorKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.image.ImageKit;
import org.miaixz.bus.extra.qrcode.QrConfig;

import com.google.zxing.common.BitMatrix;

/**
 * SVG renderer for QR codes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SVGRender implements BitMatrixRender {

    /**
     * The QR code configuration used for rendering.
     */
    private final QrConfig qrConfig;

    /**
     * Constructs an {@code SVGRender} with the specified QR code configuration.
     *
     * @param qrConfig The QR code configuration.
     */
    public SVGRender(final QrConfig qrConfig) {
        this.qrConfig = qrConfig;
    }

    /**
     * Description inherited from parent class or interface.
     *
     */
    @Override
    public void render(final BitMatrix matrix, final OutputStream out) {
        // Renders the BitMatrix to the given OutputStream, using the configured charset.
        render(matrix, new OutputStreamWriter(out, qrConfig.getCharset()));
    }

    /**
     * Renders the given {@link BitMatrix} as SVG to the specified {@link Appendable}.
     *
     * @param matrix The {@link BitMatrix} representing the QR code.
     * @param writer The {@link Appendable} to which the SVG will be written.
     * @throws InternalException if an {@link IOException} occurs during writing.
     */
    public void render(final BitMatrix matrix, final Appendable writer) {
        final Image logoImg = qrConfig.getImg();
        final Integer foreColor = qrConfig.getForeColor();
        final Integer backColor = qrConfig.getBackColor();
        final int ratio = qrConfig.getRatio();

        final int qrWidth = matrix.getWidth();
        int qrHeight = matrix.getHeight();
        final int moduleHeight = (qrHeight == 1) ? qrWidth / 2 : 1;

        qrHeight *= moduleHeight;
        String logoBase64 = "";
        int logoWidth = 0;
        int logoHeight = 0;
        int logoX = 0;
        int logoY = 0;
        if (logoImg != null) {
            logoBase64 = ImageKit.toBase64DataUri(logoImg, "png");
            // Scale according to the shortest side
            if (qrWidth < qrHeight) {
                logoWidth = qrWidth / ratio;
                logoHeight = logoImg.getHeight(null) * logoWidth / logoImg.getWidth(null);
            } else {
                logoHeight = qrHeight / ratio;
                logoWidth = logoImg.getWidth(null) * logoHeight / logoImg.getHeight(null);
            }
            logoX = (qrWidth - logoWidth) / 2;
            logoY = (qrHeight - logoHeight) / 2;

        }

        try {
            writer.append("<svg width=\"").append(String.valueOf(qrWidth)).append("\" height=\"")
                    .append(String.valueOf(qrHeight)).append("\" \n");
            if (backColor != null) {
                final Color back = new Color(backColor, true);
                writer.append("style=\"background-color:").append(ColorKit.toCssRgba(back)).append("\"\n");
            }
            writer.append("viewBox=\"0 0 ").append(String.valueOf(qrWidth)).append(Symbol.SPACE)
                    .append(String.valueOf(qrHeight)).append("\" \n");
            writer.append("xmlns=\"http://www.w3.org/2000/svg\" \n");
            writer.append("xmlns:xlink=\"http://www.w3.org/1999/xlink\" >\n");
            writer.append("<path d=\"");

            // Data
            for (int y = 0; y < qrHeight; y++) {
                for (int x = 0; x < qrWidth; x++) {
                    if (matrix.get(x, y)) {
                        writer.append(" M").append(String.valueOf(x)).append(Symbol.COMMA).append(String.valueOf(y))
                                .append("h1v").append(String.valueOf(moduleHeight)).append("h-1z");
                    }
                }
            }

            writer.append("\" ");
            if (foreColor != null) {
                final Color fore = new Color(foreColor, true);
                writer.append("stroke=\"").append(ColorKit.toCssRgba(fore)).append("\"");
            }
            writer.append(" /> \n");
            if (StringKit.isNotBlank(logoBase64)) {
                writer.append("<image xlink:href=\"").append(logoBase64).append("\" height=\"")
                        .append(String.valueOf(logoHeight)).append("\" width=\"").append(String.valueOf(logoWidth))
                        .append("\" y=\"").append(String.valueOf(logoY)).append("\" x=\"").append(String.valueOf(logoX))
                        .append("\" />\n");
            }
            writer.append("</svg>");
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

}
