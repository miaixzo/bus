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
 * @since Java 21+
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
