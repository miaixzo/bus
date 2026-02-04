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
package org.miaixz.bus.crypto.builtin.digest.mac;

import java.security.Key;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * MAC algorithm implementation using {@link org.bouncycastle.crypto.macs.CBCBlockCipherMac}, which operates in CBC
 * (Cipher Block Chaining) mode.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CBCBlockCipherMac extends BCMac {

    /**
     * Constructs a {@code CBCBlockCipherMac} instance.
     *
     * @param digest        The digest algorithm, an implementation of {@link BlockCipher}.
     * @param macSizeInBits The desired MAC result length in bits, which must be a multiple of 8.
     * @param key           The cryptographic key.
     * @param iv            The initialization vector (IV) as a byte array.
     */
    public CBCBlockCipherMac(final BlockCipher digest, final int macSizeInBits, final Key key, final byte[] iv) {
        this(digest, macSizeInBits, key.getEncoded(), iv);
    }

    /**
     * Constructs a {@code CBCBlockCipherMac} instance.
     *
     * @param digest        The digest algorithm, an implementation of {@link BlockCipher}.
     * @param macSizeInBits The desired MAC result length in bits, which must be a multiple of 8.
     * @param key           The cryptographic key as a byte array.
     * @param iv            The initialization vector (IV) as a byte array.
     */
    public CBCBlockCipherMac(final BlockCipher digest, final int macSizeInBits, final byte[] key, final byte[] iv) {
        this(digest, macSizeInBits, new ParametersWithIV(new KeyParameter(key), iv));
    }

    /**
     * Constructs a {@code CBCBlockCipherMac} instance.
     *
     * @param cipher        The cipher algorithm, an implementation of {@link BlockCipher}.
     * @param macSizeInBits The desired MAC result length in bits, which must be a multiple of 8.
     * @param key           The cryptographic key.
     */
    public CBCBlockCipherMac(final BlockCipher cipher, final int macSizeInBits, final Key key) {
        this(cipher, macSizeInBits, key.getEncoded());
    }

    /**
     * Constructs a {@code CBCBlockCipherMac} instance.
     *
     * @param cipher        The cipher algorithm, an implementation of {@link BlockCipher}.
     * @param macSizeInBits The desired MAC result length in bits, which must be a multiple of 8.
     * @param key           The cryptographic key as a byte array.
     */
    public CBCBlockCipherMac(final BlockCipher cipher, final int macSizeInBits, final byte[] key) {
        this(cipher, macSizeInBits, new KeyParameter(key));
    }

    /**
     * Constructs a {@code CBCBlockCipherMac} instance.
     *
     * @param cipher        The cipher algorithm, an implementation of {@link BlockCipher}.
     * @param macSizeInBits The desired MAC result length in bits, which must be a multiple of 8.
     * @param params        The {@link CipherParameters} for initializing the MAC, e.g., a {@link KeyParameter} for the
     *                      key.
     */
    public CBCBlockCipherMac(final BlockCipher cipher, final int macSizeInBits, final CipherParameters params) {
        this(new org.bouncycastle.crypto.macs.CBCBlockCipherMac(cipher, macSizeInBits), params);
    }

    /**
     * Constructs a {@code CBCBlockCipherMac} instance with an existing BouncyCastle
     * {@link org.bouncycastle.crypto.macs.CBCBlockCipherMac} and cipher parameters.
     *
     * @param mac    The BouncyCastle {@link org.bouncycastle.crypto.macs.CBCBlockCipherMac} instance.
     * @param params The {@link CipherParameters} for initializing the MAC, e.g., a {@link KeyParameter} for the key.
     */
    public CBCBlockCipherMac(final org.bouncycastle.crypto.macs.CBCBlockCipherMac mac, final CipherParameters params) {
        super(mac, params);
    }

}
