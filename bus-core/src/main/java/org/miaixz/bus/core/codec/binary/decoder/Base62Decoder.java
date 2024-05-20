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
package org.miaixz.bus.core.codec.binary.decoder;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.binary.encoder.Base62Encoder;
import org.miaixz.bus.core.codec.binary.provider.Base62Provider;
import org.miaixz.bus.core.lang.Normal;

/**
 * Base62解码器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base62Decoder implements Decoder<byte[], byte[]> {

    /**
     * GMP风格解码器
     */
    public static Base62Decoder GMP_DECODER = new Base62Decoder(Base62Encoder.GMP);
    /**
     * 反转风格，即将GMP风格中的大小写做转换解码器
     */
    public static Base62Decoder INVERTED_DECODER = new Base62Decoder(Base62Encoder.INVERTED);

    private final byte[] lookupTable;

    /**
     * 构造
     *
     * @param alphabet 字母表
     */
    public Base62Decoder(final byte[] alphabet) {
        lookupTable = new byte['z' + 1];
        for (int i = 0; i < alphabet.length; i++) {
            lookupTable[alphabet[i]] = (byte) i;
        }
    }


    @Override
    public byte[] decode(final byte[] encoded) {
        final byte[] prepared = Base62Provider.translate(encoded, lookupTable);
        return Base62Provider.convert(prepared, 62, Normal._256);
    }

}
