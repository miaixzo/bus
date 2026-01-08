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
package org.miaixz.bus.crypto.center;

import java.io.Serial;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.Padding;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;

/**
 * SM4 symmetric encryption algorithm implementation.
 * <p>
 * The suite of national cryptographic algorithms (Guomi algorithms) includes:
 * </p>
 * <ol>
 * <li>Asymmetric encryption and signature: SM2 (asymmetric)</li>
 * <li>Digest signature algorithm: SM3 (digest)</li>
 * <li>Symmetric encryption: SM4 (symmetric)</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SM4 extends Crypto {

    @Serial
    private static final long serialVersionUID = 2852290629929L;

    /**
     * Constructs an SM4 encryptor/decryptor with a randomly generated key.
     */
    public SM4() {
        super(Algorithm.SM4.getValue());
    }

    /**
     * Constructs an SM4 encryptor/decryptor with the given key.
     *
     * @param key The SM4 key as a byte array.
     */
    public SM4(final byte[] key) {
        super(Algorithm.SM4.getValue(), key);
    }

    /**
     * Constructs an SM4 encryptor/decryptor with the specified mode and padding. A random key is generated.
     *
     * @param mode    The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     */
    public SM4(final Algorithm.Mode mode, final Padding padding) {
        this(mode.name(), padding.name());
    }

    /**
     * Constructs an SM4 encryptor/decryptor with the specified mode, padding, and key.
     *
     * @param mode    The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     * @param key     The SM4 key as a byte array. Supports a key length of 128 bits.
     */
    public SM4(final Algorithm.Mode mode, final Padding padding, final byte[] key) {
        this(mode, padding, key, null);
    }

    /**
     * Constructs an SM4 encryptor/decryptor with the specified mode, padding, key, and initialization vector (IV).
     *
     * @param mode    The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     * @param key     The SM4 key as a byte array. Supports a key length of 128 bits.
     * @param iv      The initialization vector (IV) as a byte array. Used for modes like CBC.
     */
    public SM4(final Algorithm.Mode mode, final Padding padding, final byte[] key, final byte[] iv) {
        this(mode.name(), padding.name(), key, iv);
    }

    /**
     * Constructs an SM4 encryptor/decryptor with the specified mode, padding, and {@link SecretKey}.
     *
     * @param mode    The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     * @param key     The SM4 {@link SecretKey}. Supports a key length of 128 bits.
     */
    public SM4(final Algorithm.Mode mode, final Padding padding, final SecretKey key) {
        this(mode, padding, key, (IvParameterSpec) null);
    }

    /**
     * Constructs an SM4 encryptor/decryptor with the specified mode, padding, {@link SecretKey}, and initialization
     * vector (IV).
     *
     * @param mode    The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     * @param key     The SM4 {@link SecretKey}. Supports a key length of 128 bits.
     * @param iv      The initialization vector (IV) as a byte array. If empty or null, no IV is used.
     */
    public SM4(final Algorithm.Mode mode, final Padding padding, final SecretKey key, final byte[] iv) {
        this(mode, padding, key, ArrayKit.isEmpty(iv) ? null : new IvParameterSpec(iv));
    }

    /**
     * Constructs an SM4 encryptor/decryptor with the specified mode, padding, {@link SecretKey}, and initialization
     * vector (IV).
     *
     * @param mode    The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     * @param key     The SM4 {@link SecretKey}. Supports a key length of 128 bits.
     * @param iv      The initialization vector (IV) as an {@link IvParameterSpec}. If null, no IV is used.
     */
    public SM4(final Algorithm.Mode mode, final Padding padding, final SecretKey key, final IvParameterSpec iv) {
        this(mode.name(), padding.name(), key, iv);
    }

    /**
     * Constructs an SM4 encryptor/decryptor with the specified mode and padding names. A random key is generated.
     *
     * @param mode    The name of the encryption mode (e.g., "CBC", "CTR").
     * @param padding The name of the padding scheme (e.g., "PKCS5Padding", "NoPadding").
     */
    public SM4(final String mode, final String padding) {
        this(mode, padding, (byte[]) null);
    }

    /**
     * Constructs an SM4 encryptor/decryptor with the specified mode, padding name, and key.
     *
     * @param mode    The name of the encryption mode (e.g., "CBC", "CTR").
     * @param padding The name of the padding scheme (e.g., "PKCS5Padding", "NoPadding").
     * @param key     The SM4 key as a byte array. Supports a key length of 128 bits.
     */
    public SM4(final String mode, final String padding, final byte[] key) {
        this(mode, padding, key, null);
    }

    /**
     * Constructs an SM4 encryptor/decryptor with the specified mode, padding name, key, and initialization vector (IV).
     *
     * @param mode    The name of the encryption mode (e.g., "CBC", "CTR").
     * @param padding The name of the padding scheme (e.g., "PKCS5Padding", "NoPadding").
     * @param key     The SM4 key as a byte array. Supports a key length of 128 bits.
     * @param iv      The initialization vector (IV) as a byte array. If empty or null, no IV is used.
     */
    public SM4(final String mode, final String padding, final byte[] key, final byte[] iv) {
        this(mode, padding, //
                Keeper.generateKey(Algorithm.SM4.getValue(), key),
                ArrayKit.isEmpty(iv) ? null : new IvParameterSpec(iv));
    }

    /**
     * Constructs an SM4 encryptor/decryptor with the specified mode, padding name, and {@link SecretKey}.
     *
     * @param mode    The name of the encryption mode (e.g., "CBC", "CTR").
     * @param padding The name of the padding scheme (e.g., "PKCS5Padding", "NoPadding").
     * @param key     The SM4 {@link SecretKey}. Supports a key length of 128 bits.
     */
    public SM4(final String mode, final String padding, final SecretKey key) {
        this(mode, padding, key, null);
    }

    /**
     * Constructs an SM4 encryptor/decryptor with the specified mode, padding name, {@link SecretKey}, and
     * initialization vector (IV).
     *
     * @param mode    The name of the encryption mode (e.g., "CBC", "CTR").
     * @param padding The name of the padding scheme (e.g., "PKCS5Padding", "NoPadding").
     * @param key     The SM4 {@link SecretKey}. Supports a key length of 128 bits.
     * @param iv      The initialization vector (IV) as an {@link IvParameterSpec}. If null, no IV is used.
     */
    public SM4(final String mode, final String padding, final SecretKey key, final IvParameterSpec iv) {
        super(StringKit.format("SM4/{}/{}", mode, padding), key, iv);
    }

}
