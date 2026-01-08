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
 * @since Java 17+
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
