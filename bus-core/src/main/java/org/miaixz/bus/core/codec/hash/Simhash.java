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
package org.miaixz.bus.core.codec.hash;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.locks.StampedLock;

/**
 * Simhash is a locality-sensitive hash used for large-scale text deduplication.
 *
 * <p>
 * This implementation is adapted from:
 * <a href="https://github.com/xlturing/Simhash4J">https://github.com/xlturing/Simhash4J</a>
 *
 * <p>
 * A locality-sensitive hash (LSH) is defined as a hash function where, if two strings have a certain degree of
 * similarity, they will remain similar after being hashed.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Simhash implements Hash64<Collection<? extends CharSequence>> {

    /**
     * The number of bits in the Simhash, typically 64.
     */
    private final int bitNum = 64;
    /**
     * The number of segments to divide the Simhash into for storage, defaulting to 4.
     */
    private final int fracCount;
    /**
     * The number of bits in each segment of the Simhash.
     */
    private final int fracBitNum;
    /**
     * The Hamming distance threshold. Hashes with a distance less than this are considered similar.
     */
    private final int hammingThresh;

    /**
     * A storage mechanism that segments the Simhash for faster lookups. It is a list of maps, where each map stores a
     * fraction of the hash as a key.
     */
    private final List<Map<String, List<Long>>> storage;
    /**
     * A lock to ensure thread-safe access to the storage.
     */
    private final StampedLock lock = new StampedLock();

    /**
     * Constructs a new {@code Simhash} instance with default settings (4 segments, Hamming threshold of 3).
     */
    public Simhash() {
        this(4, 3);
    }

    /**
     * Constructs a new {@code Simhash} instance with the specified number of segments and Hamming distance threshold.
     *
     * @param fracCount     The number of segments to divide the Simhash into.
     * @param hammingThresh The Hamming distance threshold for considering documents similar.
     */
    public Simhash(final int fracCount, final int hammingThresh) {
        this.fracCount = fracCount;
        this.fracBitNum = bitNum / fracCount;
        this.hammingThresh = hammingThresh;
        this.storage = new ArrayList<>(fracCount);
        for (int i = 0; i < fracCount; i++) {
            storage.add(new HashMap<>());
        }
    }

    /**
     * Computes the 64-bit Simhash value for a given list of tokens (words). The process involves hashing each token,
     * weighting the bits, and then generating the final hash.
     *
     * @param segList A collection of character sequences (tokens) from a document.
     * @return The 64-bit Simhash value.
     */
    @Override
    public long hash64(final Collection<? extends CharSequence> segList) {
        final int bitNum = this.bitNum;
        // Calculate Simhash weight based on the hash of each word (aligned to the least significant bit)
        final int[] weight = new int[bitNum];
        long wordHash;
        for (final CharSequence seg : segList) {
            wordHash = MurmurHash.INSTANCE.hash64(seg);
            for (int i = 0; i < bitNum; i++) {
                if (((wordHash >> i) & 1) == 1)
                    weight[i] += 1;
                else
                    weight[i] -= 1;
            }
        }

        // Generate the final Simhash value
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bitNum; i++) {
            sb.append((weight[i] > 0) ? 1 : 0);
        }

        return new BigInteger(sb.toString(), 2).longValue();
    }

    /**
     * Determines if a given text is a duplicate of any text already stored, based on the Simhash and the configured
     * Hamming distance threshold.
     *
     * @param segList The tokenized text to check for duplicates.
     * @return {@code true} if the text is considered a duplicate, {@code false} otherwise.
     */
    public boolean equals(final Collection<? extends CharSequence> segList) {
        final long simhash = hash64(segList);
        final List<String> fracList = splitSimhash(simhash);
        final int hammingThresh = this.hammingThresh;

        String frac;
        Map<String, List<Long>> fracMap;
        final long stamp = this.lock.readLock();
        try {
            for (int i = 0; i < fracCount; i++) {
                frac = fracList.get(i);
                fracMap = storage.get(i);
                if (fracMap.containsKey(frac)) {
                    for (final Long simhash2 : fracMap.get(frac)) {
                        // If the Hamming distance is less than the threshold, they are similar
                        if (hamming(simhash, simhash2) < hammingThresh) {
                            return true;
                        }
                    }
                }
            }
        } finally {
            this.lock.unlockRead(stamp);
        }
        return false;
    }

    /**
     * Stores a Simhash value in the segmented storage for future comparisons. The hash is split into segments, and each
     * segment is stored as a key in the corresponding map.
     *
     * @param simhash The Simhash value to store.
     */
    public void store(final Long simhash) {
        final int fracCount = this.fracCount;
        final List<Map<String, List<Long>>> storage = this.storage;
        final List<String> lFrac = splitSimhash(simhash);

        String frac;
        Map<String, List<Long>> fracMap;
        final long stamp = this.lock.writeLock();
        try {
            for (int i = 0; i < fracCount; i++) {
                frac = lFrac.get(i);
                fracMap = storage.get(i);
                if (fracMap.containsKey(frac)) {
                    fracMap.get(frac).add(simhash);
                } else {
                    final List<Long> ls = new ArrayList<>();
                    ls.add(simhash);
                    fracMap.put(frac, ls);
                }
            }
        } finally {
            this.lock.unlockWrite(stamp);
        }
    }

    /**
     * Calculates the Hamming distance between two 64-bit Simhash values. The Hamming distance is the number of bit
     * positions at which the corresponding bits are different.
     *
     * @param s1 The first Simhash value.
     * @param s2 The second Simhash value.
     * @return The Hamming distance between the two values.
     */
    private int hamming(final Long s1, final Long s2) {
        final int bitNum = this.bitNum;
        int dis = 0;
        for (int i = 0; i < bitNum; i++) {
            if ((s1 >> i & 1) != (s2 >> i & 1))
                dis++;
        }
        return dis;
    }

    /**
     * Splits a 64-bit Simhash value into a specified number of segments. This is used to create keys for the segmented
     * storage, enabling faster lookups.
     *
     * @param simhash The Simhash value to split.
     * @return A list of strings, where each string is a binary representation of a segment of the Simhash.
     */
    private List<String> splitSimhash(final Long simhash) {
        final int bitNum = this.bitNum;
        final int fracBitNum = this.fracBitNum;

        final List<String> ls = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bitNum; i++) {
            sb.append(simhash >> i & 1);
            if ((i + 1) % fracBitNum == 0) {
                ls.add(sb.toString());
                sb.setLength(0);
            }
        }
        return ls;
    }

}
