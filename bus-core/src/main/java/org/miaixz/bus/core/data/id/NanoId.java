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
package org.miaixz.bus.core.data.id;

import java.security.SecureRandom;
import java.util.Random;

import org.miaixz.bus.core.xyz.RandomKit;

/**
 * NanoId, a small, secure, URL-friendly unique string ID generator.
 *
 * Features:
 * <ul>
 * <li><b>Secure:</b> It uses a cryptographically strong random API and guarantees proper symbol distribution.</li>
 * <li><b>Small:</b> Only 258 bytes in size (minified and gzipped), with no dependencies.</li>
 * <li><b>Compact:</b> It uses more symbols than UUID (A-Za-z0-9_~).</li>
 * </ul>
 *
 * <p>
 * The logic of this implementation is based on the JavaScript NanoId implementation, see:
 * <a href="https://github.com/ai/nanoid">https://github.com/ai/nanoid</a>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NanoId {

    /**
     * The default length of the ID.
     */
    public static final int DEFAULT_SIZE = 21;
    /**
     * The default random number generator, using {@link SecureRandom} for robustness.
     */
    private static final SecureRandom DEFAULT_NUMBER_GENERATOR = RandomKit.getSecureRandom();
    /**
     * The default alphabet, using URL-safe Base64 characters.
     */
    private static final char[] DEFAULT_ALPHABET = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            .toCharArray();

    /**
     * Generates a pseudo-random NanoId string of the default length {@link #DEFAULT_SIZE}, using a cryptographically
     * secure pseudo-random number generator.
     *
     * @return A pseudo-random NanoId string.
     */
    public static String randomNanoId() {
        return randomNanoId(DEFAULT_SIZE);
    }

    /**
     * Generates a pseudo-random NanoId string.
     *
     * @param size The length of the ID.
     * @return A pseudo-random NanoId string.
     */
    public static String randomNanoId(final int size) {
        return randomNanoId(null, null, size);
    }

    /**
     * Generates a pseudo-random NanoId string.
     *
     * @param random   The random number generator to use.
     * @param alphabet The alphabet to use for generating the ID.
     * @param size     The length of the ID.
     * @return A pseudo-random NanoId string.
     */
    public static String randomNanoId(Random random, char[] alphabet, final int size) {
        if (random == null) {
            random = DEFAULT_NUMBER_GENERATOR;
        }

        if (alphabet == null) {
            alphabet = DEFAULT_ALPHABET;
        }

        if (alphabet.length == 0 || alphabet.length >= 256) {
            throw new IllegalArgumentException("Alphabet must contain between 1 and 255 symbols.");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than zero.");
        }

        final int mask = (2 << (int) Math.floor(Math.log(alphabet.length - 1) / Math.log(2))) - 1;
        final int step = (int) Math.ceil(1.6 * mask * size / alphabet.length);

        final StringBuilder idBuilder = new StringBuilder();

        while (true) {
            final byte[] bytes = new byte[step];
            random.nextBytes(bytes);
            for (int i = 0; i < step; i++) {
                final int alphabetIndex = bytes[i] & mask;
                if (alphabetIndex < alphabet.length) {
                    idBuilder.append(alphabet[alphabetIndex]);
                    if (idBuilder.length() == size) {
                        return idBuilder.toString();
                    }
                }
            }
        }
    }

}
