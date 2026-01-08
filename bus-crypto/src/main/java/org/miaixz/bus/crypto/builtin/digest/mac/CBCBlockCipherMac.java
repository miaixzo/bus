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
