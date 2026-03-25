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
package org.miaixz.bus.core.codec.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * An implementation of the Ketama hash algorithm, commonly used in consistent hashing to map keys to servers (e.g., in
 * Memcached).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class KetamaHash implements Hash64<byte[]>, Hash32<byte[]> {

    /**
     * Calculates the MD5 hash of the given key.
     *
     * @param key The key to be hashed.
     * @return The MD5 hash as a byte array.
     */
    private static byte[] md5(final byte[] key) {
        final MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (final NoSuchAlgorithmException e) {
            throw new InternalException("MD5 algorithm not supported!", e);
        }
        return md5.digest(key);
    }

    /**
     * Computes the 64-bit Ketama hash value.
     *
     * @param key The input byte array.
     * @return The 64-bit hash value.
     */
    @Override
    public long hash64(final byte[] key) {
        final byte[] bKey = md5(key);
        return ((long) (bKey[3] & 0xFF) << 24) | ((long) (bKey[2] & 0xFF) << 16) | ((long) (bKey[1] & 0xFF) << 8)
                | (bKey[0] & 0xFF);
    }

    /**
     * Computes the 32-bit Ketama hash value.
     *
     * @param key The input byte array.
     * @return The 32-bit hash value.
     */
    @Override
    public int hash32(final byte[] key) {
        return (int) (hash64(key) & 0xffffffffL);
    }

    /**
     * Computes the 64-bit Ketama hash and returns it as a {@link Number}.
     *
     * @param key The input byte array.
     * @return The 64-bit hash as a {@code Long}.
     */
    @Override
    public Number encode(final byte[] key) {
        return hash64(key);
    }

}
