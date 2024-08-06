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

import java.awt.*;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ColorKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.image.ImageKit;

import com.google.zxing.common.BitMatrix;

/**
 * 二维码的SVG表示
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QrSVG {

    private final BitMatrix matrix;
    private final QrConfig qrConfig;

    /**
     * 构造
     *
     * @param matrix   {@link BitMatrix}
     * @param qrConfig {@link QrConfig}
     */
    public QrSVG(final BitMatrix matrix, final QrConfig qrConfig) {
        this.matrix = matrix;
        this.qrConfig = qrConfig;
    }

    @Override
    public String toString() {
        final Image logoImg = qrConfig.img;
        final Integer foreColor = qrConfig.foreColor;
        final Integer backColor = qrConfig.backColor;
        final int ratio = qrConfig.ratio;

        final StringBuilder sb = new StringBuilder();
        final int qrWidth = matrix.getWidth();
        int qrHeight = matrix.getHeight();
        final int moduleHeight = (qrHeight == 1) ? qrWidth / 2 : 1;
        for (int y = 0; y < qrHeight; y++) {
            for (int x = 0; x < qrWidth; x++) {
                if (matrix.get(x, y)) {
                    sb.append(" M").append(x).append(Symbol.COMMA).append(y).append("h1v").append(moduleHeight)
                            .append("h-1z");
                }
            }
        }
        qrHeight *= moduleHeight;
        String logoBase64 = "";
        int logoWidth = 0;
        int logoHeight = 0;
        int logoX = 0;
        int logoY = 0;
        if (logoImg != null) {
            logoBase64 = ImageKit.toBase64DataUri(logoImg, "png");
            // 按照最短的边做比例缩放
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

        final StringBuilder result = StringKit.builder();
        result.append("<svg width=\"").append(qrWidth).append("\" height=\"").append(qrHeight).append("\" \n");
        if (backColor != null) {
            final Color back = new Color(backColor, true);
            result.append("style=\"background-color:").append(ColorKit.toCssRgba(back)).append("\"\n");
        }
        result.append("viewBox=\"0 0 ").append(qrWidth).append(Symbol.SPACE).append(qrHeight).append("\" \n");
        result.append("xmlns=\"http://www.w3.org/2000/svg\" \n");
        result.append("xmlns:xlink=\"http://www.w3.org/1999/xlink\" >\n");
        result.append("<path d=\"").append(sb).append("\" ");
        if (foreColor != null) {
            final Color fore = new Color(foreColor, true);
            result.append("stroke=\"").append(ColorKit.toCssRgba(fore)).append("\"");
        }
        result.append(" /> \n");
        if (StringKit.isNotBlank(logoBase64)) {
            result.append("<image xlink:href=\"").append(logoBase64).append("\" height=\"").append(logoHeight)
                    .append("\" width=\"").append(logoWidth).append("\" y=\"").append(logoY).append("\" x=\"")
                    .append(logoX).append("\" />\n");
        }
        result.append("</svg>");
        return result.toString();
    }
}
