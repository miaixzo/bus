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
package org.miaixz.bus.core.xyz;

import org.miaixz.bus.core.codec.No128;
import org.miaixz.bus.core.codec.hash.CityHash;
import org.miaixz.bus.core.codec.hash.MurmurHash;
import org.miaixz.bus.core.codec.hash.metro.MetroHash128;
import org.miaixz.bus.core.codec.hash.metro.MetroHash64;

/**
 * A collection of various hash algorithms. FNV1 is recommended for general use.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HashKit {

    /**
     * Additive hash.
     *
     * @param key   The string.
     * @param prime A prime number.
     * @return The hash result.
     */
    public static int additiveHash(final String key, final int prime) {
        int hash, i;
        for (hash = key.length(), i = 0; i < key.length(); i++) {
            hash += key.charAt(i);
        }
        return hash % prime;
    }

    /**
     * Rotating hash.
     *
     * @param key   The input string.
     * @param prime A prime number.
     * @return The hash value.
     */
    public static int rotatingHash(final String key, final int prime) {
        int hash, i;
        for (hash = key.length(), i = 0; i < key.length(); ++i) {
            hash = (hash << 4) ^ (hash >> 28) ^ key.charAt(i);
        }
        return hash % prime;
    }

    /**
     * One-at-a-time hash.
     *
     * @param key The input string.
     * @return The hash value.
     */
    public static int oneByOneHash(final String key) {
        int hash, i;
        for (hash = 0, i = 0; i < key.length(); ++i) {
            hash += key.charAt(i);
            hash += (hash << 10);
            hash ^= (hash >> 6);
        }
        hash += (hash << 3);
        hash ^= (hash >> 11);
        hash += (hash << 15);
        return hash;
    }

    /**
     * Bernstein's hash.
     *
     * @param key The input string.
     * @return The hash result.
     */
    public static int bernstein(final String key) {
        int hash = 0;
        int i;
        for (i = 0; i < key.length(); ++i) {
            hash = 33 * hash + key.charAt(i);
        }
        return hash;
    }

    /**
     * Universal Hashing.
     *
     * @param key  The char array.
     * @param mask The mask.
     * @param tab  The table.
     * @return The hash value.
     */
    public static int universal(final char[] key, final int mask, final int[] tab) {
        int hash = key.length;
        int i;
        final int len = key.length;
        for (i = 0; i < (len << 3); i += 8) {
            final char k = key[i >> 3];
            if ((k & 0x01) == 0)
                hash ^= tab[i];
            if ((k & 0x02) == 0)
                hash ^= tab[i + 1];
            if ((k & 0x04) == 0)
                hash ^= tab[i + 2];
            if ((k & 0x08) == 0)
                hash ^= tab[i + 3];
            if ((k & 0x10) == 0)
                hash ^= tab[i + 4];
            if ((k & 0x20) == 0)
                hash ^= tab[i + 5];
            if ((k & 0x40) == 0)
                hash ^= tab[i + 6];
            if ((k & 0x80) == 0)
                hash ^= tab[i + 7];
        }
        return (hash & mask);
    }

    /**
     * Zobrist Hashing.
     *
     * @param key  The char array.
     * @param mask The mask.
     * @param tab  The table.
     * @return The hash value.
     */
    public static int zobrist(final char[] key, final int mask, final int[][] tab) {
        int hash, i;
        for (hash = key.length, i = 0; i < key.length; ++i) {
            hash ^= tab[i][key[i]];
        }
        return (hash & mask);
    }

    /**
     * Modified 32-bit FNV-1a hash.
     *
     * @param data The byte array.
     * @return The hash result.
     */
    public static int fnvHash(final byte[] data) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (final byte b : data) {
            hash = (hash ^ b) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return Math.abs(hash);
    }

    /**
     * Modified 32-bit FNV-1a hash.
     *
     * @param data The string.
     * @return The hash result.
     */
    public static int fnvHash(final String data) {
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
        return Math.abs(hash);
    }

    /**
     * Thomas Wang's integer hash.
     *
     * @param key The integer.
     * @return The hash value.
     */
    public static int intHash(int key) {
        key += ~(key << 15);
        key ^= (key >>> 10);
        key += (key << 3);
        key ^= (key >>> 6);
        key += ~(key << 11);
        key ^= (key >>> 16);
        return key;
    }

    /**
     * RS hash algorithm.
     *
     * @param text The string.
     * @return The hash value.
     */
    public static int rsHash(final String text) {
        final int b = 378551;
        int a = 63689;
        int hash = 0;
        for (int i = 0; i < text.length(); i++) {
            hash = hash * a + text.charAt(i);
            a = a * b;
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * JS hash algorithm.
     *
     * @param text The string.
     * @return The hash value.
     */
    public static int jsHash(final String text) {
        int hash = 1315423911;
        for (int i = 0; i < text.length(); i++) {
            hash ^= ((hash << 5) + text.charAt(i) + (hash >> 2));
        }
        return Math.abs(hash) & 0x7FFFFFFF;
    }

    /**
     * PJW hash algorithm.
     *
     * @param text The string.
     * @return The hash value.
     */
    public static int pjwHash(final String text) {
        final int bitsInUnsignedInt = 32;
        final int threeQuarters = (bitsInUnsignedInt * 3) / 4;
        final int oneEighth = bitsInUnsignedInt / 8;
        final int highBits = 0xFFFFFFFF << (bitsInUnsignedInt - oneEighth);
        int hash = 0;
        int test;
        for (int i = 0; i < text.length(); i++) {
            hash = (hash << oneEighth) + text.charAt(i);
            if ((test = hash & highBits) != 0) {
                hash = ((hash ^ (test >> threeQuarters)) & (~highBits));
            }
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * ELF hash algorithm.
     *
     * @param text The string.
     * @return The hash value.
     */
    public static int elfHash(final String text) {
        int hash = 0;
        int x;
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
     * BKDR hash algorithm.
     *
     * @param text The string.
     * @return The hash value.
     */
    public static int bkdrHash(final String text) {
        final int seed = 131; // 31, 131, 1313, 13131, 131313, etc.
        int hash = 0;
        for (int i = 0; i < text.length(); i++) {
            hash = (hash * seed) + text.charAt(i);
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * SDBM hash algorithm.
     *
     * @param text The string.
     * @return The hash value.
     */
    public static int sdbmHash(final String text) {
        int hash = 0;
        for (int i = 0; i < text.length(); i++) {
            hash = text.charAt(i) + (hash << 6) + (hash << 16) - hash;
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * DJB hash algorithm.
     *
     * @param text The string.
     * @return The hash value.
     */
    public static int djbHash(final String text) {
        int hash = 5381;
        for (int i = 0; i < text.length(); i++) {
            hash = ((hash << 5) + hash) + text.charAt(i);
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * DEK hash algorithm.
     *
     * @param text The string.
     * @return The hash value.
     */
    public static int dekHash(final String text) {
        int hash = text.length();
        for (int i = 0; i < text.length(); i++) {
            hash = ((hash << 5) ^ (hash >> 27)) ^ text.charAt(i);
        }
        return hash & 0x7FFFFFFF;
    }

    /**
     * AP hash algorithm.
     *
     * @param text The string.
     * @return The hash value.
     */
    public static int apHash(final String text) {
        int hash = 0;
        for (int i = 0; i < text.length(); i++) {
            hash ^= ((i & 1) == 0) ? ((hash << 7) ^ text.charAt(i) ^ (hash >> 3))
                    : (~((hash << 11) ^ text.charAt(i) ^ (hash >> 5)));
        }
        return hash;
    }

    /**
     * TianL Hash algorithm.
     *
     * @param text The string.
     * @return The hash value.
     */
    public static long tianlHash(final String text) {
        long hash;
        final int iLength = text.length();
        if (iLength == 0) {
            return 0;
        }

        if (iLength <= 256) {
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
                    ucChar = (char) (ucChar + 32);
                }
                hash += (3L * i * ucChar * ucChar + 5L * i * ucChar + 7L * i + 11 * ucChar) % 16777216;
            }
        } else {
            for (i = 1; i <= 96; i++) {
                ucChar = text.charAt(i + iLength - 96 - 1);
                if (ucChar <= 'Z' && ucChar >= 'A') {
                    ucChar = (char) (ucChar + 32);
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
     * Java's default `String.hashCode()` algorithm.
     *
     * @param text The string.
     * @return The hash value.
     */
    public static int javaDefaultHash(final String text) {
        int h = 0;
        int off = 0;
        final int len = text.length();
        for (int i = 0; i < len; i++) {
            h = 31 * h + text.charAt(off++);
        }
        return h;
    }

    /**
     * Mixed hash algorithm, producing a 64-bit value.
     *
     * @param text The string.
     * @return The hash value.
     */
    public static long mixHash(final String text) {
        long hash = text.hashCode();
        hash <<= 32;
        hash |= fnvHash(text);
        return hash;
    }

    /**
     * Gets the hash code from `System.identityHashCode()`.
     *
     * @param object The object.
     * @return The hash code.
     */
    public static int identityHashCode(final Object object) {
        return System.identityHashCode(object);
    }

    /**
     * MurmurHash 32-bit implementation.
     *
     * @param data The data.
     * @return The hash value.
     */
    public static int murmur32(final byte[] data) {
        return MurmurHash.INSTANCE.hash32(data);
    }

    /**
     * MurmurHash 64-bit implementation.
     *
     * @param data The data.
     * @return The hash value.
     */
    public static long murmur64(final byte[] data) {
        return MurmurHash.INSTANCE.hash64(data);
    }

    /**
     * MurmurHash 128-bit implementation.
     *
     * @param data The data.
     * @return The 128-bit hash value.
     */
    public static No128 murmur128(final byte[] data) {
        return MurmurHash.INSTANCE.hash128(data);
    }

    /**
     * CityHash 32-bit implementation.
     *
     * @param data The data.
     * @return The hash value.
     */
    public static int cityHash32(final byte[] data) {
        return CityHash.INSTANCE.hash32(data);
    }

    /**
     * CityHash 64-bit implementation.
     *
     * @param data The data.
     * @param seed A seed value.
     * @return The hash value.
     */
    public static long cityHash64(final byte[] data, final long seed) {
        return CityHash.INSTANCE.hash64(data, seed);
    }

    /**
     * CityHash 64-bit implementation.
     *
     * @param data  The data.
     * @param seed0 The first seed value.
     * @param seed1 The second seed value.
     * @return The hash value.
     */
    public static long cityHash64(final byte[] data, final long seed0, final long seed1) {
        return CityHash.INSTANCE.hash64(data, seed0, seed1);
    }

    /**
     * CityHash 64-bit implementation.
     *
     * @param data The data.
     * @return The hash value.
     */
    public static long cityHash64(final byte[] data) {
        return CityHash.INSTANCE.hash64(data);
    }

    /**
     * CityHash 128-bit implementation.
     *
     * @param data The data.
     * @return The 128-bit hash value.
     */
    public static No128 cityHash128(final byte[] data) {
        return CityHash.INSTANCE.hash128(data);
    }

    /**
     * CityHash 128-bit implementation.
     *
     * @param data The data.
     * @param seed A 128-bit seed.
     * @return The 128-bit hash value.
     */
    public static No128 cityHash128(final byte[] data, final No128 seed) {
        return CityHash.INSTANCE.hash128(data, seed);
    }

    /**
     * MetroHash 64-bit implementation.
     *
     * @param data The data.
     * @param seed A seed value.
     * @return The hash value.
     */
    public static long metroHash64(final byte[] data, final long seed) {
        return MetroHash64.of(seed).hash64(data);
    }

    /**
     * MetroHash 128-bit implementation.
     *
     * @param data The data.
     * @param seed A seed value.
     * @return The 128-bit hash value.
     */
    public static No128 metroHash128(final byte[] data, final long seed) {
        return MetroHash128.of(seed).hash128(data);
    }

    /**
     * HF Hash algorithm.
     *
     * @param data The string.
     * @return The hash result.
     */
    public static long hfHash(final String data) {
        final int length = data.length();
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
     * HFIP Hash algorithm.
     *
     * @param data The string.
     * @return The hash result.
     */
    public static long hfIpHash(final String data) {
        final int length = data.length();
        long hash = 0;
        for (int i = 0; i < length; i++) {
            hash += data.charAt(i % 4) ^ data.charAt(i);
        }
        return hash;
    }

}
