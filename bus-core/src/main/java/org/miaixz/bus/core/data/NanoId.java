/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.data;

import org.miaixz.bus.core.xyz.RandomKit;

import java.security.SecureRandom;
import java.util.Random;

/**
 * NanoId，一个小型、安全、对 URL友好的唯一字符串 ID 生成器，特点：
 *
 * <ul>
 *     <li>安全：它使用加密、强大的随机 API，并保证符号的正确分配</li>
 *     <li>体积小：只有 258 bytes 大小（压缩后）、无依赖</li>
 *     <li>紧凑：它使用比 UUID (A-Za-z0-9_~)更多的符号</li>
 * </ul>
 *
 * <p>
 * 此实现的逻辑基于JavaScript的NanoId实现，见：<a href="https://github.com/ai/nanoid">https://github.com/ai/nanoid</a>
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NanoId {

    /**
     * 默认长度
     */
    public static final int DEFAULT_SIZE = 21;
    /**
     * 默认随机数生成器，使用{@link SecureRandom}确保健壮性
     */
    private static final SecureRandom DEFAULT_NUMBER_GENERATOR = RandomKit.getSecureRandom();
    /**
     * 默认随机字母表，使用URL安全的Base64字符
     */
    private static final char[] DEFAULT_ALPHABET = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    /**
     * 生成伪随机的NanoId字符串，长度为默认的{@link #DEFAULT_SIZE}，使用密码安全的伪随机生成器
     *
     * @return 伪随机的NanoId字符串
     */
    public static String randomNanoId() {
        return randomNanoId(DEFAULT_SIZE);
    }

    /**
     * 生成伪随机的NanoId字符串
     *
     * @param size ID长度
     * @return 伪随机的NanoId字符串
     */
    public static String randomNanoId(final int size) {
        return randomNanoId(null, null, size);
    }

    /**
     * 生成伪随机的NanoId字符串
     *
     * @param random   随机数生成器
     * @param alphabet 随机字母表
     * @param size     ID长度
     * @return 伪随机的NanoId字符串
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
