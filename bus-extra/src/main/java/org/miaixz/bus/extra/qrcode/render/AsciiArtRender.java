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
package org.miaixz.bus.extra.qrcode.render;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.miaixz.bus.core.lang.ansi.AnsiElement;
import org.miaixz.bus.core.lang.ansi.AnsiEncoder;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ColorKit;
import org.miaixz.bus.extra.qrcode.QrConfig;

import com.google.zxing.common.BitMatrix;

/**
 * ASCII Art渲染
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AsciiArtRender implements BitMatrixRender {

    private final QrConfig config;

    /**
     * 构造
     *
     * @param config 二维码配置
     */
    public AsciiArtRender(final QrConfig config) {
        this.config = config;
    }

    @Override
    public void render(final BitMatrix matrix, final OutputStream out) {
        render(matrix, new OutputStreamWriter(out, config.getCharset()));
    }

    /**
     * 渲染SVG
     *
     * @param matrix 二维码
     * @param writer 输出
     */
    public void render(final BitMatrix matrix, final Appendable writer) {
        final int width = matrix.getWidth();
        final int height = matrix.getHeight();

        final Integer foreColor = config.getForeColor();
        final AnsiElement foreground = foreColor == null ? null : ColorKit.toAnsiColor(foreColor, true, false);
        final Integer backColor = config.getBackColor();
        final AnsiElement background = backColor == null ? null : ColorKit.toAnsiColor(backColor, true, true);

        try {
            for (int i = 0; i <= height; i += 2) {
                final StringBuilder rowBuilder = new StringBuilder();
                for (int j = 0; j < width; j++) {
                    final boolean tp = matrix.get(i, j);
                    final boolean bt = i + 1 >= height || matrix.get(i + 1, j);
                    if (tp && bt) {
                        rowBuilder.append(' ');// '\u0020'
                    } else if (tp) {
                        rowBuilder.append('▄');// '\u2584'
                    } else if (bt) {
                        rowBuilder.append('▀');// '\u2580'
                    } else {
                        rowBuilder.append('█');// '\u2588'
                    }
                }
                writer.append(AnsiEncoder.encode(foreground, background, rowBuilder)).append('\n');
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

}
