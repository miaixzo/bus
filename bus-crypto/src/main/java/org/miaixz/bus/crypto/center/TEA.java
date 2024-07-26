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
package org.miaixz.bus.crypto.center;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.crypto.builtin.symmetric.Decryptor;
import org.miaixz.bus.crypto.builtin.symmetric.Encryptor;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * TEA（Corrected Block Tiny Encryption Algorithm）算法实现 来自：https://github.com/xxtea/xxtea-java
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TEA implements Encryptor, Decryptor, Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 密钥调度常数
     */
    private static final int DELTA = 0x9E3779B9;

    /**
     * 密钥
     */
    private final byte[] key;

    /**
     * 构造
     *
     * @param key 密钥，16位
     */
    public TEA(final byte[] key) {
        this.key = key;
    }

    private static int[] encrypt(final int[] v, final int[] k) {
        final int n = v.length - 1;

        if (n < 1) {
            return v;
        }
        int p, q = 6 + 52 / (n + 1);
        int z = v[n], y, sum = 0, e;

        while (q-- > 0) {
            sum = sum + DELTA;
            e = sum >>> 2 & 3;
            for (p = 0; p < n; p++) {
                y = v[p + 1];
                z = v[p] += mx(sum, y, z, p, e, k);
            }
            y = v[0];
            z = v[n] += mx(sum, y, z, p, e, k);
        }
        return v;
    }

    private static int[] decrypt(final int[] v, final int[] k) {
        final int n = v.length - 1;

        if (n < 1) {
            return v;
        }
        int p;
        final int q = 6 + 52 / (n + 1);
        int z, y = v[0], sum = q * DELTA, e;

        while (sum != 0) {
            e = sum >>> 2 & 3;
            for (p = n; p > 0; p--) {
                z = v[p - 1];
                y = v[p] -= mx(sum, y, z, p, e, k);
            }
            z = v[n];
            y = v[0] -= mx(sum, y, z, p, e, k);
            sum = sum - DELTA;
        }
        return v;
    }

    private static int mx(final int sum, final int y, final int z, final int p, final int e, final int[] k) {
        return (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[p & 3 ^ e] ^ z);
    }

    private static byte[] fixKey(final byte[] key) {
        if (key.length == 16) {
            return key;
        }
        final byte[] fixedkey = new byte[16];
        System.arraycopy(key, 0, fixedkey, 0, Math.min(key.length, 16));
        return fixedkey;
    }

    private static int[] toIntArray(final byte[] data, final boolean includeLength) {
        int n = (((data.length & 3) == 0) ? (data.length >>> 2) : ((data.length >>> 2) + 1));
        final int[] result;

        if (includeLength) {
            result = new int[n + 1];
            result[n] = data.length;
        } else {
            result = new int[n];
        }
        n = data.length;
        for (int i = 0; i < n; ++i) {
            result[i >>> 2] |= (0x000000ff & data[i]) << ((i & 3) << 3);
        }
        return result;
    }

    private static byte[] toByteArray(final int[] data, final boolean includeLength) {
        int n = data.length << 2;

        if (includeLength) {
            final int m = data[data.length - 1];
            n -= 4;
            if ((m < n - 3) || (m > n)) {
                return null;
            }
            n = m;
        }
        final byte[] result = new byte[n];

        for (int i = 0; i < n; ++i) {
            result[i] = (byte) (data[i >>> 2] >>> ((i & 3) << 3));
        }
        return result;
    }

    @Override
    public byte[] encrypt(final byte[] data) {
        if (data.length == 0) {
            return data;
        }
        return toByteArray(encrypt(toIntArray(data, true), toIntArray(fixKey(key), false)), false);
    }

    @Override
    public void encrypt(final InputStream data, final OutputStream out, final boolean isClose) {
        IoKit.write(out, isClose, encrypt(IoKit.readBytes(data)));
    }

    @Override
    public byte[] decrypt(final byte[] data) {
        if (data.length == 0) {
            return data;
        }
        return toByteArray(decrypt(toIntArray(data, false), toIntArray(fixKey(key), false)), true);
    }

    @Override
    public void decrypt(final InputStream data, final OutputStream out, final boolean isClose) {
        IoKit.write(out, isClose, decrypt(IoKit.readBytes(data)));
    }

}
