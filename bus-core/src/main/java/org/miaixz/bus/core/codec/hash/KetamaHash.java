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
package org.miaixz.bus.core.codec.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Ketama算法，用于在一致性Hash中快速定位服务器位置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KetamaHash implements Hash64<byte[]>, Hash32<byte[]> {

    /**
     * 计算MD5值，使用UTF-8编码
     *
     * @param key 被计算的键
     * @return MD5值
     */
    private static byte[] md5(final byte[] key) {
        final MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (final NoSuchAlgorithmException e) {
            throw new InternalException("MD5 algorithm not suooport!", e);
        }
        return md5.digest(key);
    }

    @Override
    public long hash64(final byte[] key) {
        final byte[] bKey = md5(key);
        return ((long) (bKey[3] & 0xFF) << 24) | ((long) (bKey[2] & 0xFF) << 16) | ((long) (bKey[1] & 0xFF) << 8)
                | (bKey[0] & 0xFF);
    }

    @Override
    public int hash32(final byte[] key) {
        return (int) (hash64(key) & 0xffffffffL);
    }

    @Override
    public Number encode(final byte[] key) {
        return hash64(key);
    }

}
