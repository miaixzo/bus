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
package org.miaixz.bus.extra.qrcode;

import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

/**
 * Encoder for QR codes (and other barcodes), used to convert text content into a BitMatrix.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QrEncoder implements Encoder<CharSequence, BitMatrix> {

    /**
     * QR code configuration.
     */
    private final QrConfig config;

    /**
     * Constructor.
     *
     * @param config {@link QrConfig}
     */
    public QrEncoder(final QrConfig config) {
        this.config = ObjectKit.defaultIfNull(config, QrConfig::of);
    }

    /**
     * Creates a QrEncoder.
     *
     * @param config {@link QrConfig}
     * @return QrEncoder
     */
    public static QrEncoder of(final QrConfig config) {
        return new QrEncoder(config);
    }

    /**
     * Encodes the given content into a {@link BitMatrix}.
     *
     * @param content The content to encode.
     * @return The encoded {@link BitMatrix}.
     * @throws QrCodeException if a {@link WriterException} occurs during encoding.
     */
    @Override
    public BitMatrix encode(final CharSequence content) {
        final MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        final BitMatrix bitMatrix;
        try {
            bitMatrix = multiFormatWriter
                    .encode(StringKit.toString(content), config.format, config.width, config.height, config.toHints());
        } catch (final WriterException e) {
            throw new QrCodeException(e);
        }

        return bitMatrix;
    }

}
