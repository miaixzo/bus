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
 * ASCII Art renderer.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AsciiArtRender implements BitMatrixRender {

    /**
     * The QR code configuration used for rendering.
     */
    private final QrConfig config;

    /**
     * Constructs an {@code AsciiArtRender} with the specified QR code configuration.
     *
     * @param config The QR code configuration.
     */
    public AsciiArtRender(final QrConfig config) {
        this.config = config;
    }

    /**
     * Description inherited from parent class or interface.
     *
     */
    @Override
    public void render(final BitMatrix matrix, final OutputStream out) {
        // Renders the BitMatrix to the given OutputStream, using the configured charset.
        render(matrix, new OutputStreamWriter(out, config.getCharset()));
    }

    /**
     * Renders the given {@link BitMatrix} as ASCII Art to the specified {@link Appendable}.
     *
     * @param matrix The {@link BitMatrix} representing the QR code.
     * @param writer The {@link Appendable} to which the ASCII Art will be written.
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
