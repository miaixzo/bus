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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.ansi.AnsiElement;
import org.miaixz.bus.core.lang.ansi.AnsiEncoder;
import org.miaixz.bus.core.xyz.ColorKit;

import com.google.zxing.common.BitMatrix;

/**
 * 二维码的AsciiArt表示
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QrAsciiArt {

    private final BitMatrix matrix;
    private final QrConfig qrConfig;

    /**
     * 构造
     *
     * @param matrix   {@link BitMatrix}
     * @param qrConfig {@link QrConfig}
     */
    public QrAsciiArt(final BitMatrix matrix, final QrConfig qrConfig) {
        this.matrix = matrix;
        this.qrConfig = qrConfig;
    }

    @Override
    public String toString() {
        final int width = matrix.getWidth();
        final int height = matrix.getHeight();

        final AnsiElement foreground = qrConfig.foreColor == null ? null
                : ColorKit.toAnsiColor(qrConfig.foreColor, true, false);
        final AnsiElement background = qrConfig.backColor == null ? null
                : ColorKit.toAnsiColor(qrConfig.backColor, true, true);

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= height; i += 2) {
            final StringBuilder rowBuilder = new StringBuilder();
            for (int j = 0; j < width; j++) {
                final boolean tp = matrix.get(i, j);
                final boolean bt = i + 1 >= height || matrix.get(i + 1, j);
                if (tp && bt) {
                    rowBuilder.append(Symbol.C_SPACE);// '\u0020'
                } else if (tp) {
                    rowBuilder.append('▄');// '\u2584'
                } else if (bt) {
                    rowBuilder.append('▀');// '\u2580'
                } else {
                    rowBuilder.append('█');// '\u2588'
                }
            }
            builder.append(AnsiEncoder.encode(foreground, background, rowBuilder)).append('\n');
        }
        return builder.toString();
    }
}
