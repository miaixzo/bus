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
import java.io.Serializable;

import org.bouncycastle.crypto.AlphabetMapper;
import org.bouncycastle.jcajce.spec.FPEParameterSpec;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.Padding;

/**
 * FPE (Format Preserving Encryption) implementation, supporting FF1 and FF3-1 modes. For a related introduction, see:
 * https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-38G.pdf
 *
 * <p>
 * FPE is a type of encryption that preserves the format of the plaintext, commonly used in data masking because it
 * needs to maintain the same format for both plaintext and ciphertext. For example, a social security number, after
 * encryption, is not a fixed-length string of random characters, but a shuffled number in the same format, still
 * resembling a social security number.
 * </p>
 * <p>
 * The FPE algorithm guarantees:
 *
 * <ul>
 * <li>The data length remains unchanged. If the length before encryption is N, the length after encryption is still
 * N.</li>
 * <li>The data type remains unchanged. If it was a numeric type before encryption, it remains a numeric type after
 * encryption.</li>
 * <li>The encryption process is reversible. The encrypted data can be decrypted back to the original data using the
 * key.</li>
 * </ul>
 * <p>
 * This class is implemented based on BouncyCastle.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FPE implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852289932187L;

    /**
     * The AES cipher instance.
     */
    private final AES aes;
    /**
     * The alphabet mapper.
     */
    private final AlphabetMapper mapper;

    /**
     * Constructor, uses an empty Tweak.
     *
     * @param mode   FPE mode enum, can be FF1 or FF3-1.
     * @param key    The secret key. {@code null} indicates a random key. The length must be 16, 24, or 32 bytes.
     * @param mapper The alphabet mapping. The character range of the data to be encrypted must be consistent with this
     *               mapping. For example, fields like phone numbers or bank card numbers can use a numeric alphabet
     *               table.
     */
    public FPE(final FPEMode mode, final byte[] key, final AlphabetMapper mapper) {
        this(mode, key, mapper, null);
    }

    /**
     * Constructor.
     *
     * @param mode   FPE mode enum, can be FF1 or FF3-1.
     * @param key    The secret key. {@code null} indicates a random key. The length must be 16, 24, or 32 bytes.
     * @param mapper The alphabet mapping. The character range of the data to be encrypted must be consistent with this
     *               mapping. For example, fields like phone numbers or bank card numbers can use a numeric alphabet
     *               table.
     * @param tweak  The Tweak is used to solve result collision problems caused by partial encryption. Typically, the
     *               immutable part of the data is used as the Tweak. {@code null} uses a default-length byte array of
     *               all zeros.
     */
    public FPE(FPEMode mode, final byte[] key, final AlphabetMapper mapper, byte[] tweak) {
        if (null == mode) {
            mode = FPEMode.FF1;
        }

        if (null == tweak) {
            switch (mode) {
                case FF1:
                    tweak = new byte[0];
                    break;

                case FF3_1:
                    // FF3-1 requires the tweak to be 56 bits (7 bytes)
                    tweak = new byte[7];
                    break;
            }
        }
        this.aes = new AES(mode.value, Padding.NoPadding.name(), Keeper.generateKey(mode.value, key),
                new FPEParameterSpec(mapper.getRadix(), tweak));
        this.mapper = mapper;
    }

    /**
     * Encrypts data.
     *
     * @param data The data, which must be within the range defined in the {@link AlphabetMapper} passed to the
     *             constructor.
     * @return The ciphertext result.
     */
    public String encrypt(final String data) {
        if (null == data) {
            return null;
        }
        return new String(encrypt(data.toCharArray()));
    }

    /**
     * Encrypts data.
     *
     * @param data The data, which must be within the range defined in the {@link AlphabetMapper} passed to the
     *             constructor.
     * @return The ciphertext result.
     */
    public char[] encrypt(final char[] data) {
        if (null == data) {
            return null;
        }
        // Use the mapper to convert the ciphertext output back to the original format
        return mapper.convertToChars(aes.encrypt(mapper.convertToIndexes(data)));
    }

    /**
     * Decrypts data.
     *
     * @param data The ciphertext data, which must be within the range defined in the {@link AlphabetMapper} passed to
     *             the constructor.
     * @return The plaintext result.
     */
    public String decrypt(final String data) {
        if (null == data) {
            return null;
        }
        return new String(decrypt(data.toCharArray()));
    }

    /**
     * Decrypts data.
     *
     * @param data The ciphertext data, which must be within the range defined in the {@link AlphabetMapper} passed to
     *             the constructor.
     * @return The plaintext result.
     */
    public char[] decrypt(final char[] data) {
        if (null == data) {
            return null;
        }
        // Use the mapper to convert the ciphertext output back to the original format
        return mapper.convertToChars(aes.decrypt(mapper.convertToIndexes(data)));
    }

    /**
     * FPE Mode. FPE includes two modes: FF1 and FF3 (FF2 is deprecated). Both are based on a Feistel network structure.
     */
    public enum FPEMode {

        /**
         * FF1 Mode.
         */
        FF1("FF1"),
        /**
         * FF3-1 Mode.
         */
        FF3_1("FF3-1");

        /**
         * The FPE mode value.
         */
        private final String value;

        FPEMode(final String name) {
            this.value = name;
        }

        /**
         * Gets the mode name.
         *
         * @return The mode name.
         */
        public String getValue() {
            return value;
        }
    }

}
