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
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.lang.Normal;

/**
 * Hash算法大全
 * 推荐使用FNV1算法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HashKit {

    /**
     * 加法hash
     *
     * @param key   字符串
     * @param prime 一个质数
     * @return hash结果
     */
    public static int additiveHash(String key, int prime) {
        int hash, i;
        for (hash = key.length(), i = 0; i < key.length(); i++) {
            hash += key.charAt(i);
        }
        return hash % prime;
    }

    /**
     * 旋转hash
     *
     * @param key   输入字符串
     * @param prime 质数
     * @return hash值
     */
    public static int rotatingHash(String key, int prime) {
        int hash, i;
        for (hash = key.length(), i = 0; i < key.length(); ++i) {
            hash = (hash << 4) ^ (hash >> 28) ^ key.charAt(i);
        }

        // 使用：hash = (hash ^ (hash>>10) ^ (hash>>20)) & mask;
        // 替代：hash %= prime;
        // return (hash ^ (hash>>10) ^ (hash>>20));
        return hash % prime;
    }

    /**
     * 一次一个hash
     *
     * @param key 输入字符串
     * @return 输出hash值
     */
    public static int oneByOneHash(String key) {
        int hash, i;
        for (hash = 0, i = 0; i < key.length(); ++i) {
            hash += key.charAt(i);
            hash += (hash << 10);
            hash ^= (hash >> 6);
        }
        hash += (hash << 3);
        hash ^= (hash >> 11);
        hash += (hash << 15);
        // return (hash & M_MASK);
        return hash;
    }

    /**
     * Bernstein's hash
     *
     * @param key 输入字节数组
     * @return 结果hash
     */
    public static int bernstein(String key) {
        int hash = 0;
        int i;
        for (i = 0; i < key.length(); ++i) {
            hash = 33 * hash + key.charAt(i);
        }
        return hash;
    }

    /**
     * Universal Hashing
     *
     * @param key  字节数组
     * @param mask 掩码
     * @param tab  tab
     * @return hash值
     */
    public static int universal(char[] key, int mask, int[] tab) {
        int hash = key.length, i, len = key.length;
        for (i = 0; i < (len << 3); i += 8) {
            char k = key[i >> 3];
            if ((k & 0x01) == 0) {
                hash ^= tab[i + 0];
            }
            if ((k & 0x02) == 0) {
                hash ^= tab[i + 1];
            }
            if ((k & 0x04) == 0) {
                hash ^= tab[i + 2];
            }
            if ((k & 0x08) == 0) {
                hash ^= tab[i + 3];
            }
            if ((k & 0x10) == 0) {
                hash ^= tab[i + 4];
            }
            if ((k & 0x20) == 0) {
                hash ^= tab[i + 5];
            }
            if ((k & 0x40) == 0) {
                hash ^= tab[i + 6];
            }
            if ((k & 0x80) == 0) {
                hash ^= tab[i + 7];
            }
        }
        return (hash & mask);
    }

    /**
     * Zobrist Hashing
     *
     * @param key  字节数组
     * @param mask 掩码
     * @param tab  tab
     * @return hash值
     */
    public static int zobrist(char[] key, int mask, int[][] tab) {
        int hash, i;
        for (hash = key.length, i = 0; i < key.length; ++i) {
            hash ^= tab[i][key[i]];
        }
        return (hash & mask);
    }

    /**
     * 改进的32位FNV算法1
     *
     * @param data 数组
     * @return hash结果
     */
    public static int fnvHash(byte[] data) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (byte b : data) {
            hash = (hash ^ b) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return hash;
    }

    /**
     * 改进的32位FNV算法1
     *
     * @param data 字符串
     * @return hash结果
     */
    public static int fnvHash(String data) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < data.length(); i++) {
            hash = (hash ^ data.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return hash;
    }

    /**
     * Thomas Wang的算法,整数hash
     *
     * @param key 整数
     * @return hash值
     */
    public static int intHash(int key) {
        key += ~(key << 15);
        key ^= (key >>> 10);
        key += (key << 3);
        key ^= (key >>> 6);
        key += ~(key << 11);
        key ^= (key >>> Normal._16);
        return key;
    }

    /**
     * RS算法hash
     *
     * @param text 字符串
     * @return hash值
     */
    public static int rsHash(String text) {
        int b = 378551;
        int a = 63689;
        int hash = 0;

        for (int i = 0; i < text.length(); i++) {
            hash = hash * a + text.charAt(i);
            a = a * b;
        }

        return hash & 0x7FFFFFFF;
    }

    /**
     * JS算法
     *
     * @param text 字符串
     * @return hash值
     */
    public static int jsHash(String text) {
        int hash = 1315423911;

        for (int i = 0; i < text.length(); i++) {
            hash ^= ((hash << 5) + text.charAt(i) + (hash >> 2));
        }

        return Math.abs(hash) & 0x7FFFFFFF;
    }

    /**
     * PJW算法
     *
     * @param text 字符串
     * @return hash值
     */
    public static int pjwHash(String text) {
        int bitsInUnsignedInt = Normal._32;
        int threeQuarters = (bitsInUnsignedInt * 3) / 4;
        int oneEighth = bitsInUnsignedInt / 8;
        int highBits = 0xFFFFFFFF << (bitsInUnsignedInt - oneEighth);
        int hash = 0;
        int test = 0;

        for (int i = 0; i < text.length(); i++) {
            hash = (hash << oneEighth) + text.charAt(i);

            if ((test = hash & highBits) != 0) {
                hash = ((hash ^ (test >> threeQuarters)) & (~highBits));
            }
        }

        return hash & 0x7FFFFFFF;
    }

    /**
     * ELF算法
     *
     * @param text 字符串
     * @return hash值
     */
    public static int elfHash(String text) {
        int hash = 0;
        int x = 0;

        for (int i = 0; i < text.length(); i++) {
            hash = (hash << 4) + text.charAt(i);
            if ((x = (int) (hash & 0xF0000000L)) != 0) {
                hash ^= (x >> 24);
                hash &= ~x;
            }
        }

        return hash & 0x7FFFFFFF;
    }

    /**
     * BKDR算法
     *
     * @param text 字符串
     * @return hash值
     */
    public static int bkdrHash(String text) {
        int seed = 131; // 31 131 1313 13131 131313 etc..
        int hash = 0;

        for (int i = 0; i < text.length(); i++) {
            hash = (hash * seed) + text.charAt(i);
        }

        return hash & 0x7FFFFFFF;
    }

    /**
     * SDBM算法
     *
     * @param text 字符串
     * @return hash值
     */
    public static int sdbmHash(String text) {
        int hash = 0;

        for (int i = 0; i < text.length(); i++) {
            hash = text.charAt(i) + (hash << 6) + (hash << Normal._16) - hash;
        }

        return hash & 0x7FFFFFFF;
    }

    /**
     * DJB算法
     *
     * @param text 字符串
     * @return hash值
     */
    public static int djbHash(String text) {
        int hash = 5381;

        for (int i = 0; i < text.length(); i++) {
            hash = ((hash << 5) + hash) + text.charAt(i);
        }

        return hash & 0x7FFFFFFF;
    }

    /**
     * DEK算法
     *
     * @param text 字符串
     * @return hash值
     */
    public static int dekHash(String text) {
        int hash = text.length();

        for (int i = 0; i < text.length(); i++) {
            hash = ((hash << 5) ^ (hash >> 27)) ^ text.charAt(i);
        }

        return hash & 0x7FFFFFFF;
    }

    /**
     * AP算法
     *
     * @param text 字符串
     * @return hash值
     */
    public static int apHash(String text) {
        int hash = 0;

        for (int i = 0; i < text.length(); i++) {
            hash ^= ((i & 1) == 0) ? ((hash << 7) ^ text.charAt(i) ^ (hash >> 3)) : (~((hash << 11) ^ text.charAt(i) ^ (hash >> 5)));
        }

        // return (hash & 0x7FFFFFFF);
        return hash;
    }

    /**
     * TianL Hash算法
     *
     * @param text 字符串
     * @return Hash值
     */
    public static long tianlHash(String text) {
        long hash = 0;

        int iLength = text.length();
        if (iLength == 0) {
            return 0;
        }

        if (iLength <= Normal._256) {
            hash = 16777216L * (iLength - 1);
        } else {
            hash = 4278190080L;
        }

        int i;

        char ucChar;

        if (iLength <= 96) {
            for (i = 1; i <= iLength; i++) {
                ucChar = text.charAt(i - 1);
                if (ucChar <= 'Z' && ucChar >= 'A') {
                    ucChar = (char) (ucChar + Normal._32);
                }
                hash += (3L * i * ucChar * ucChar + 5L * i * ucChar + 7L * i + 11 * ucChar) % 16777216;
            }
        } else {
            for (i = 1; i <= 96; i++) {
                ucChar = text.charAt(i + iLength - 96 - 1);
                if (ucChar <= 'Z' && ucChar >= 'A') {
                    ucChar = (char) (ucChar + Normal._32);
                }
                hash += (3L * i * ucChar * ucChar + 5L * i * ucChar + 7L * i + 11 * ucChar) % 16777216;
            }
        }
        if (hash < 0) {
            hash *= -1;
        }
        return hash;
    }

    /**
     * JAVA自己带的算法
     *
     * @param text 字符串
     * @return hash值
     */
    public static int javaDefaultHash(String text) {
        int h = 0;
        int off = 0;
        int len = text.length();
        for (int i = 0; i < len; i++) {
            h = 31 * h + text.charAt(off++);
        }
        return h;
    }

    /**
     * 混合hash算法,输出64位的值
     *
     * @param text 字符串
     * @return hash值
     */
    public static long mixHash(String text) {
        long hash = text.hashCode();
        hash <<= Normal._32;
        hash |= fnvHash(text);
        return hash;
    }

    /**
     * HF Hash算法
     *
     * @param data 字符串
     * @return hash结果
     */
    public static long hfHash(String data) {
        int length = data.length();
        long hash = 0;

        for (int i = 0; i < length; i++) {
            hash += (long) data.charAt(i) * 3 * i;
        }

        if (hash < 0) {
            hash = -hash;
        }

        return hash;
    }

    /**
     * HFIP Hash算法
     *
     * @param data 字符串
     * @return hash结果
     */
    public static long hfIpHash(String data) {
        int length = data.length();
        long hash = 0;
        for (int i = 0; i < length; i++) {
            hash += data.charAt(i % 4) ^ data.charAt(i);
        }
        return hash;
    }

}
