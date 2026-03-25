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

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.Padding;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;

/**
 * Triple Data Encryption Algorithm (TDEA), also known as 3DES (Triple DES). TDEA is a mechanism that encrypts data
 * three times with a 168-bit key, typically (but not always) providing extremely strong security. If all three 56-bit
 * sub-keys are identical, Triple DES is backward compatible with DES. The default implementation in Java is
 * DESede/ECB/PKCS5Padding.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TDEA extends Crypto {

    @Serial
    private static final long serialVersionUID = 2852290890720L;

    /**
     * Constructs a TDEA encryptor/decryptor with default settings: DESede/ECB/PKCS5Padding. A random key is generated.
     */
    public TDEA() {
        super(Algorithm.DESEDE);
    }

    /**
     * Constructs a TDEA encryptor/decryptor with the given key and default settings: DESede/ECB/PKCS5Padding.
     *
     * @param key The TDEA key as a byte array.
     */
    public TDEA(final byte[] key) {
        super(Algorithm.DESEDE, key);
    }

    /**
     * Constructs a TDEA encryptor/decryptor with the specified mode and padding. A random key is generated.
     *
     * @param mode    The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     */
    public TDEA(final Algorithm.Mode mode, final Padding padding) {
        this(mode.name(), padding.name());
    }

    /**
     * Constructs a TDEA encryptor/decryptor with the specified mode, padding, and key.
     *
     * @param mode    The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     * @param key     The TDEA key as a byte array. Expected length is 24 bits.
     */
    public TDEA(final Algorithm.Mode mode, final Padding padding, final byte[] key) {
        this(mode, padding, key, null);
    }

    /**
     * Constructs a TDEA encryptor/decryptor with the specified mode, padding, key, and initialization vector (IV).
     *
     * @param mode    The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     * @param key     The TDEA key as a byte array. Expected length is 24 bits.
     * @param iv      The initialization vector (IV) as a byte array. Used for modes like CBC.
     */
    public TDEA(final Algorithm.Mode mode, final Padding padding, final byte[] key, final byte[] iv) {
        this(mode.name(), padding.name(), key, iv);
    }

    /**
     * Constructs a TDEA encryptor/decryptor with the specified mode, padding, and {@link SecretKey}.
     *
     * @param mode    The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     * @param key     The TDEA {@link SecretKey}. Expected key length is 24 bits.
     */
    public TDEA(final Algorithm.Mode mode, final Padding padding, final SecretKey key) {
        this(mode, padding, key, null);
    }

    /**
     * Constructs a TDEA encryptor/decryptor with the specified mode, padding, {@link SecretKey}, and initialization
     * vector (IV).
     *
     * @param mode    The encryption mode (e.g., CBC, CTR) as defined in {@link Algorithm.Mode}.
     * @param padding The padding scheme (e.g., PKCS5Padding, NoPadding) as defined in {@link Padding}.
     * @param key     The TDEA {@link SecretKey}. Expected key length is 24 bits.
     * @param iv      The initialization vector (IV) as an {@link IvParameterSpec}. Used for modes like CBC.
     */
    public TDEA(final Algorithm.Mode mode, final Padding padding, final SecretKey key, final IvParameterSpec iv) {
        this(mode.name(), padding.name(), key, iv);
    }

    /**
     * Constructs a TDEA encryptor/decryptor with the specified mode and padding names. A random key is generated.
     *
     * @param mode    The name of the encryption mode (e.g., "CBC", "CTR").
     * @param padding The name of the padding scheme (e.g., "PKCS5Padding", "NoPadding").
     */
    public TDEA(final String mode, final String padding) {
        this(mode, padding, (byte[]) null);
    }

    /**
     * Constructs a TDEA encryptor/decryptor with the specified mode, padding name, and key.
     *
     * @param mode    The name of the encryption mode (e.g., "CBC", "CTR").
     * @param padding The name of the padding scheme (e.g., "PKCS5Padding", "NoPadding").
     * @param key     The TDEA key as a byte array. Expected length is 24 bits.
     */
    public TDEA(final String mode, final String padding, final byte[] key) {
        this(mode, padding, key, null);
    }

    /**
     * Constructs a TDEA encryptor/decryptor with the specified mode, padding name, key, and initialization vector (IV).
     *
     * @param mode    The name of the encryption mode (e.g., "CBC", "CTR").
     * @param padding The name of the padding scheme (e.g., "PKCS5Padding", "NoPadding").
     * @param key     The TDEA key as a byte array. Expected length is 24 bits.
     * @param iv      The initialization vector (IV) as a byte array. If null, no IV is used.
     */
    public TDEA(final String mode, final String padding, final byte[] key, final byte[] iv) {
        this(mode, padding, Keeper.generateKey(Algorithm.DESEDE.getValue(), key),
                null == iv ? null : new IvParameterSpec(iv));
    }

    /**
     * Constructs a TDEA encryptor/decryptor with the specified mode, padding name, and {@link SecretKey}.
     *
     * @param mode    The name of the encryption mode (e.g., "CBC", "CTR").
     * @param padding The name of the padding scheme (e.g., "PKCS5Padding", "NoPadding").
     * @param key     The TDEA {@link SecretKey}. Expected key length is 24 bits.
     */
    public TDEA(final String mode, final String padding, final SecretKey key) {
        this(mode, padding, key, null);
    }

    /**
     * Constructs a TDEA encryptor/decryptor with the specified mode, padding name, {@link SecretKey}, and
     * initialization vector (IV).
     *
     * @param mode    The name of the encryption mode (e.g., "CBC", "CTR").
     * @param padding The name of the padding scheme (e.g., "PKCS5Padding", "NoPadding").
     * @param key     The TDEA {@link SecretKey}. Expected key length is 24 bits.
     * @param iv      The initialization vector (IV) as an {@link IvParameterSpec}. If null, no IV is used.
     */
    public TDEA(final String mode, final String padding, final SecretKey key, final IvParameterSpec iv) {
        super(StringKit.format("{}/{}/{}", Algorithm.DESEDE.getValue(), mode, padding), key, iv);
    }

}
