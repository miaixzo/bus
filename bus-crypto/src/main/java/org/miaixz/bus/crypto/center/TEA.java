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
package org.miaixz.bus.crypto.center;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.crypto.builtin.symmetric.Decryptor;
import org.miaixz.bus.crypto.builtin.symmetric.Encryptor;

/**
 * TEA (Corrected Block Tiny Encryption Algorithm) implementation. This implementation is adapted from:
 * <a href="https://github.com/xxtea/xxtea-java">https://github.com/xxtea/xxtea-java</a>
 * <p>
 * TEA is a block cipher notable for its simplicity and small code footprint. It operates on 64-bit blocks of plaintext
 * using a 128-bit key.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TEA implements Encryptor, Decryptor, Serializable {

    @Serial
    private static final long serialVersionUID = 2852291067505L;

    /**
     * Key scheduling constant used in the TEA algorithm.
     */
    private static final int DELTA = 0x9E3779B9;

    /**
     * The encryption key.
     */
    private final byte[] key;

    /**
     * Constructs a TEA encryptor/decryptor with the specified key.
     *
     * @param key The encryption key, expected to be 16 bytes long.
     */
    public TEA(final byte[] key) {
        this.key = key;
    }

    /**
     * Encrypts a block of data using the TEA algorithm.
     *
     * @param v The data block to encrypt, represented as an array of integers.
     * @param k The key, represented as an array of integers.
     * @return The encrypted data block.
     */
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

    /**
     * Decrypts a block of data using the TEA algorithm.
     *
     * @param v The data block to decrypt, represented as an array of integers.
     * @param k The key, represented as an array of integers.
     * @return The decrypted data block.
     */
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

    /**
     * The mixing function used in the TEA algorithm.
     *
     * @param sum The current sum in the TEA round.
     * @param y   The previous word.
     * @param z   The current word.
     * @param p   The current block index.
     * @param e   A derived value from the sum.
     * @param k   The key array.
     * @return The result of the mixing operation.
     */
    private static int mx(final int sum, final int y, final int z, final int p, final int e, final int[] k) {
        return (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[p & 3 ^ e] ^ z);
    }

    /**
     * Fixes the key length to 16 bytes. If the provided key is shorter, it's padded with zeros. If longer, it's
     * truncated.
     *
     * @param key The original key byte array.
     * @return A 16-byte fixed-length key.
     */
    private static byte[] fixKey(final byte[] key) {
        if (key.length == 16) {
            return key;
        }
        final byte[] fixedkey = new byte[16];
        System.arraycopy(key, 0, fixedkey, 0, Math.min(key.length, 16));
        return fixedkey;
    }

    /**
     * Converts a byte array to an integer array for TEA processing.
     *
     * @param data          The byte array to convert.
     * @param includeLength If {@code true}, the last element of the integer array will store the original byte array
     *                      length.
     * @return The converted integer array.
     */
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

    /**
     * Converts an integer array (processed by TEA) back to a byte array.
     *
     * @param data          The integer array to convert.
     * @param includeLength If {@code true}, the last element of the integer array is assumed to store the original byte
     *                      array length.
     * @return The converted byte array, or {@code null} if the length information is invalid.
     */
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

    /**
     * Description inherited from parent class or interface.
     *
     * @param data Description inherited from parent class or interface.
     * @return Description inherited from parent class or interface.
     */
    @Override
    public byte[] encrypt(final byte[] data) {
        if (data.length == 0) {
            return data;
        }
        return toByteArray(encrypt(toIntArray(data, true), toIntArray(fixKey(key), false)), false);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param data    Description inherited from parent class or interface.
     * @param out     Description inherited from parent class or interface.
     * @param isClose Description inherited from parent class or interface.
     */
    @Override
    public void encrypt(final InputStream data, final OutputStream out, final boolean isClose) {
        IoKit.write(out, isClose, encrypt(IoKit.readBytes(data)));
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param data Description inherited from parent class or interface.
     * @return Description inherited from parent class or interface.
     */
    @Override
    public byte[] decrypt(final byte[] data) {
        if (data.length == 0) {
            return data;
        }
        return toByteArray(decrypt(toIntArray(data, false), toIntArray(fixKey(key), false)), true);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param data    Description inherited from parent class or interface.
     * @param out     Description inherited from parent class or interface.
     * @param isClose Description inherited from parent class or interface.
     */
    @Override
    public void decrypt(final InputStream data, final OutputStream out, final boolean isClose) {
        IoKit.write(out, isClose, decrypt(IoKit.readBytes(data)));
    }

}
