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

import java.io.Serial;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.Padding;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;
import org.miaixz.bus.crypto.cipher.JceCipher;

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
 */
public class AES extends Crypto {

    @Serial
    private static final long serialVersionUID = 2539228818001L;

    /**
     * Length of the initialization vector used in GCM payloads.
     */
    private static final int GCM_IV_BYTES = Normal._12;

    /**
     * Length of the authentication tag used in GCM payloads.
     */
    private static final int GCM_TAG_BYTES = Normal._16;

    /**
     * Number of extra bytes in a GCM payload for its initialization vector and authentication tag.
     */
    public static final int GCM_OVERHEAD_BYTES = GCM_IV_BYTES + GCM_TAG_BYTES;

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

    /**
     * Encrypts plaintext with AES-GCM using a fresh 96-bit initialization vector and a 128-bit authentication tag.
     *
     * @param key       raw AES key of 16, 24, or 32 bytes
     * @param aad       additional authenticated data, which may be empty
     * @param plaintext plaintext to encrypt
     * @return initialization vector followed by ciphertext and authentication tag
     * @throws IllegalArgumentException if the key is null or empty
     * @throws GeneralSecurityException if the cipher cannot encrypt the plaintext
     */
    public static byte[] sealGcm(final byte[] key, final byte[] aad, final byte[] plaintext)
            throws GeneralSecurityException {
        byte[] iv = RandomKit.randomBytes(GCM_IV_BYTES, RandomKit.getSecureRandom());
        byte[] ciphertext = processGcm(Algorithm.Type.ENCRYPT, key, iv, aad, plaintext, 0);
        byte[] payload = Arrays.copyOf(iv, iv.length + ciphertext.length);
        System.arraycopy(ciphertext, 0, payload, iv.length, ciphertext.length);
        return payload;
    }

    /**
     * Decrypts and authenticates a payload produced by {@link #sealGcm(byte[], byte[], byte[])}.
     *
     * @param key     raw AES key of 16, 24, or 32 bytes
     * @param aad     additional authenticated data supplied on encryption
     * @param payload initialization vector followed by ciphertext and authentication tag
     * @return authenticated plaintext
     * @throws IllegalArgumentException if the key is null or empty
     * @throws GeneralSecurityException if the payload is truncated or authentication fails
     */
    public static byte[] openGcm(final byte[] key, final byte[] aad, final byte[] payload)
            throws GeneralSecurityException {
        if (payload.length < GCM_OVERHEAD_BYTES) {
            throw new GeneralSecurityException("Truncated AES-GCM payload");
        }
        return processGcm(
                Algorithm.Type.DECRYPT,
                key,
                Arrays.copyOf(payload, GCM_IV_BYTES),
                aad,
                payload,
                GCM_IV_BYTES);
    }

    /**
     * Processes one authenticated AES-GCM message using the platform security provider.
     *
     * @param mode   cipher operation mode
     * @param key    raw AES key
     * @param iv     initialization vector for the message
     * @param aad    additional authenticated data
     * @param input  plaintext or encrypted payload
     * @param offset index where the cipher input begins
     * @return ciphertext with authentication tag or authenticated plaintext
     * @throws GeneralSecurityException if the cipher cannot process the input
     */
    private static byte[] processGcm(
            final Algorithm.Type mode,
            final byte[] key,
            final byte[] iv,
            final byte[] aad,
            final byte[] input,
            final int offset) throws GeneralSecurityException {
        JceCipher cipher = new JceCipher(Cipher.getInstance("AES/GCM/NoPadding"));
        cipher.init(
                mode.getValue(),
                new JceCipher.JceParameters(new SecretKeySpec(key, Algorithm.AES.getValue()),
                        new GCMParameterSpec(GCM_TAG_BYTES * Byte.SIZE, iv), null));
        try {
            cipher.updateAad(aad);
            return cipher.processFinal(input, offset, input.length - offset);
        } catch (CryptoException failure) {
            if (failure.getCause() instanceof GeneralSecurityException cause) {
                throw cause;
            }
            throw new GeneralSecurityException("AES-GCM operation failed", failure);
        }
    }

}
