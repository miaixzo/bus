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
