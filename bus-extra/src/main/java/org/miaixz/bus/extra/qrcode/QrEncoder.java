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

import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 二维码（条形码等）编码器，用于将文本内容转换为二维码
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QrEncoder implements Encoder<CharSequence, BitMatrix> {

    private final QrConfig config;

    /**
     * 构造
     *
     * @param config {@link QrConfig}
     */
    public QrEncoder(final QrConfig config) {
        this.config = ObjectKit.defaultIfNull(config, QrConfig::of);
    }

    /**
     * 创建QrEncoder
     *
     * @param config {@link QrConfig}
     * @return QrEncoder
     */
    public static QrEncoder of(final QrConfig config) {
        return new QrEncoder(config);
    }

    @Override
    public BitMatrix encode(final CharSequence content) {
        final MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        final BitMatrix bitMatrix;
        try {
            bitMatrix = multiFormatWriter.encode(StringKit.toString(content), config.format, config.width,
                    config.height, config.toHints());
        } catch (final WriterException e) {
            throw new QrCodeException(e);
        }

        return bitMatrix;
    }
}
