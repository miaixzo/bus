/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.Padding;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;

/**
 * AES (Advanced Encryption Standard) encryption algorithm implementation. Also known as Rijndael in cryptography.
 * <p>
 * The default mode for AES in Java is AES/ECB/PKCS5Padding. If using CryptoJS, it should be adjusted to `padding:
 * CryptoJS.pad.Pkcs7`.
 * </p>
 *
 * <p>
 * Related concepts:
 * </p>
 * <ul>
 * <li><b>Mode:</b> Encryption algorithm mode, used to describe how a block cipher (specifically, not stream ciphers)
 * processes plaintext blocks during encryption. It represents different block processing methods.</li>
 * <li><b>Padding:</b> A padding scheme is used in block ciphers when the plaintext length is not an integer multiple of
 * the block length. Data is appended to the last block to make its length a multiple of the block size.</li>
 * <li><b>IV (Initialization Vector):</b> When encrypting plaintext blocks, each block is XORed with the previous
 * ciphertext block. For the first plaintext block, there is no "previous ciphertext block," so an Initialization Vector
 * (IV) of the same length as the block is used as a substitute.</li>
 * </ul>
 * <p>
 * For more details on these concepts, refer to: <a href=
 * "https://blog.csdn.net/OrangeJack/article/details/82913804">https://blog.csdn.net/OrangeJack/article/details/82913804</a>
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AES extends Crypto {

    @Serial
    private static final long serialVersionUID = 2852289387881L;

    /**
     * Constructs an AES encryptor/decryptor with default settings: AES/ECB/PKCS5Padding. A random key is generated.
     */
    public AES() {
        super(Algorithm.AES);
    }

    /**
     * Constructs an AES encryptor/decryptor with the given key and default settings: AES/ECB/PKCS5Padding.
     *
     * @param key The AES key as a byte array.
     */
    public AES(final byte[] key) {
        super(Algorithm.AES, key);
    }

    /**
     * Constructs an AES encryptor/decryptor with the given {@link SecretKey} and default settings:
     * AES/ECB/PKCS5Padding.
     *
     * @param key The AES {@link SecretKey}.
     */
    public AES(final SecretKey key) {
        super(Algorithm.AES, key);
    }

    /**
     * Constructs an AES encryptor/decryptor with the specified mode and padding. A random key is generated.
     *
     * @param mode    The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     */
    public AES(final Algorithm.Mode mode, final Padding padding) {
        this(mode.name(), padding.name());
    }

    /**
     * Constructs an AES encryptor/decryptor with the specified mode, padding, and key.
     *
     * @param mode    The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     * @param key     The AES key as a byte array. Supports key lengths of 128, 192, or 256 bits.
     */
    public AES(final Algorithm.Mode mode, final Padding padding, final byte[] key) {
        this(mode, padding, key, null);
    }

    /**
     * Constructs an AES encryptor/decryptor with the specified mode, padding, key, and initialization vector (IV).
     *
     * @param mode    The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     * @param key     The AES key as a byte array. Supports key lengths of 128, 192, or 256 bits.
     * @param iv      The initialization vector (IV) as a byte array. Used for modes like CBC.
     */
    public AES(final Algorithm.Mode mode, final Padding padding, final byte[] key, final byte[] iv) {
        this(mode.name(), padding.name(), key, iv);
    }

    /**
     * Constructs an AES encryptor/decryptor with the specified mode, padding, and {@link SecretKey}.
     *
     * @param mode    The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     * @param key     The AES {@link SecretKey}. Supports key lengths of 128, 192, or 256 bits.
     */
    public AES(final Algorithm.Mode mode, final Padding padding, final SecretKey key) {
        this(mode, padding, key, (IvParameterSpec) null);
    }

    /**
     * Constructs an AES encryptor/decryptor with the specified mode, padding, {@link SecretKey}, and initialization
     * vector (IV).
     *
     * @param mode    The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     * @param key     The AES {@link SecretKey}. Supports key lengths of 128, 192, or 256 bits.
     * @param iv      The initialization vector (IV) as a byte array. If empty or null, no IV is used.
     */
    public AES(final Algorithm.Mode mode, final Padding padding, final SecretKey key, final byte[] iv) {
        this(mode, padding, key, ArrayKit.isEmpty(iv) ? null : new IvParameterSpec(iv));
    }

    /**
     * Constructs an AES encryptor/decryptor with the specified mode, padding, {@link SecretKey}, and algorithm
     * parameters.
     *
     * @param mode       The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding    The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     * @param key        The AES {@link SecretKey}. Supports key lengths of 128, 192, or 256 bits.
     * @param paramsSpec Algorithm-specific parameters, such as an {@link IvParameterSpec} for IV.
     */
    public AES(final Algorithm.Mode mode, final Padding padding, final SecretKey key,
            final AlgorithmParameterSpec paramsSpec) {
        this(mode.name(), padding.name(), key, paramsSpec);
    }

    /**
     * Constructs an AES encryptor/decryptor with the specified mode and padding names. A random key is generated.
     *
     * @param mode    The name of the encryption mode (e.g., "CBC", "CTR").
     * @param padding The name of the padding scheme (e.g., "PKCS5Padding", "NoPadding").
     */
    public AES(final String mode, final String padding) {
        this(mode, padding, (byte[]) null);
    }

    /**
     * Constructs an AES encryptor/decryptor with the specified mode, padding name, and key.
     *
     * @param mode    The name of the encryption mode (e.g., "CBC", "CTR").
     * @param padding The name of the padding scheme (e.g., "PKCS5Padding", "NoPadding").
     * @param key     The AES key as a byte array. Supports key lengths of 128, 192, or 256 bits.
     */
    public AES(final String mode, final String padding, final byte[] key) {
        this(mode, padding, key, null);
    }

    /**
     * Constructs an AES encryptor/decryptor with the specified mode, padding name, key, and initialization vector (IV).
     *
     * @param mode    The name of the encryption mode (e.g., "CBC", "CTR").
     * @param padding The name of the padding scheme (e.g., "PKCS5Padding", "NoPadding").
     * @param key     The AES key as a byte array. Supports key lengths of 128, 192, or 256 bits.
     * @param iv      The initialization vector (IV) as a byte array. If empty or null, no IV is used.
     */
    public AES(final String mode, final String padding, final byte[] key, final byte[] iv) {
        this(mode, padding, Keeper.generateKey(Algorithm.AES.getValue(), key),
                ArrayKit.isEmpty(iv) ? null : new IvParameterSpec(iv));
    }

    /**
     * Constructs an AES encryptor/decryptor with the specified mode, padding name, and {@link SecretKey}.
     *
     * @param mode    The name of the encryption mode (e.g., "CBC", "CTR").
     * @param padding The name of the padding scheme (e.g., "PKCS5Padding", "NoPadding").
     * @param key     The AES {@link SecretKey}. Supports key lengths of 128, 192, or 256 bits.
     */
    public AES(final String mode, final String padding, final SecretKey key) {
        this(mode, padding, key, null);
    }

    /**
     * Constructs an AES encryptor/decryptor with the specified mode, padding name, {@link SecretKey}, and algorithm
     * parameters.
     *
     * @param mode       The name of the encryption mode (e.g., "CBC", "CTR").
     * @param padding    The name of the padding scheme (e.g., "PKCS5Padding", "NoPadding").
     * @param key        The AES {@link SecretKey}. Supports key lengths of 128, 192, or 256 bits.
     * @param paramsSpec Algorithm-specific parameters, such as an {@link IvParameterSpec} for IV.
     */
    public AES(final String mode, final String padding, final SecretKey key, final AlgorithmParameterSpec paramsSpec) {
        super(StringKit.format("AES/{}/{}", mode, padding), key, paramsSpec);
    }

}
