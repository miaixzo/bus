/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.codec.binary.encoder;

import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.codec.binary.provider.Base62Provider;
import org.miaixz.bus.core.lang.Normal;

/**
 * Base62编码器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base62Encoder implements Encoder<byte[], byte[]> {

    /**
     * GMP 编码器
     */
    public static Base62Encoder GMP_ENCODER = new Base62Encoder(Normal.UPPER_LOWER_NUMBER.getBytes());
    /**
     * INVERTED 编码器
     */
    public static Base62Encoder INVERTED_ENCODER = new Base62Encoder(Normal.LOWER_UPPER_NUMBER.getBytes());
    /**
     * 字符信息
     */
    private final byte[] alphabet;

    /**
     * 构造
     *
     * @param alphabet 字符表
     */
    public Base62Encoder(final byte[] alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public byte[] encode(final byte[] data) {
        final byte[] indices = Base62Provider.convert(data, Base62Provider.STANDARD_BASE, Base62Provider.TARGET_BASE);
        return Base62Provider.translate(indices, alphabet);
    }
}
