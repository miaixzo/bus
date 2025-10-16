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
package org.miaixz.bus.core.codec.hash.metro;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.miaixz.bus.core.codec.hash.Hash64;

/**
 * A 64-bit implementation of the MetroHash algorithm, a set of state-of-the-art hash functions for non-cryptographic
 * use cases.
 *
 * <p>
 * Official implementation:
 * <a href="https://github.com/jandrewrogers/MetroHash">https://github.com/jandrewrogers/MetroHash</a><br>
 * Official documentation: <a href=
 * "http://www.jandrewrogers.com/2015/05/27/metrohash/">http://www.jandrewrogers.com/2015/05/27/metrohash/</a><br>
 * Ported from: <a href="https://github.com/postamar/java-metrohash/">https://github.com/postamar/java-metrohash/</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MetroHash64 extends AbstractMetroHash<MetroHash64> implements Hash64<byte[]> {

    /**
     * Predefined constants for the MetroHash64 algorithm.
     */
    private static final long K0 = 0xD6D018F5L;
    private static final long K1 = 0xA2AA033BL;
    private static final long K2 = 0x62992FC1L;
    private static final long K3 = 0x30BC5B29L;

    /**
     * The current hash value.
     */
    private long hash;

    /**
     * Constructs a new {@code MetroHash64} instance with a specified seed.
     *
     * @param seed The seed value.
     */
    public MetroHash64(final long seed) {
        super(seed);
    }

    /**
     * Creates a new {@code MetroHash64} instance.
     *
     * @param seed The seed value.
     * @return A new {@code MetroHash64} instance.
     */
    public static MetroHash64 of(final long seed) {
        return new MetroHash64(seed);
    }

    /**
     * Resets the hash state to its initial values based on the seed.
     *
     * @return this instance for chaining.
     */
    @Override
    public MetroHash64 reset() {
        hash = (seed + K2) * K0;
        v0 = v1 = v2 = v3 = hash;
        nChunks = 0;
        return this;
    }

    /**
     * Gets the final 64-bit hash value after all data has been processed.
     *
     * @return The 64-bit hash value.
     */
    public long get() {
        return hash;
    }

    /**
     * Calculates the 64-bit hash for the given byte array.
     *
     * @param bytes The input byte array.
     * @return The 64-bit hash value.
     */
    @Override
    public long hash64(final byte[] bytes) {
        return apply(ByteBuffer.wrap(bytes)).get();
    }

    /**
     * Writes the final 64-bit hash value to the given {@link ByteBuffer} in the specified byte order.
     *
     * @param output    The buffer to write to.
     * @param byteOrder The byte order to use.
     * @return this instance for chaining.
     */
    @Override
    public MetroHash64 write(final ByteBuffer output, final ByteOrder byteOrder) {
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            writeLittleEndian(hash, output);
        } else {
            output.asLongBuffer().put(hash);
        }
        return this;
    }

    /**
     * Processes a 32-byte chunk from the input and updates the hash state.
     *
     * @param partialInput The byte buffer, which must have at least 32 bytes remaining.
     * @return this instance for chaining.
     */
    @Override
    MetroHash64 partialApply32ByteChunk(final ByteBuffer partialInput) {
        assert partialInput.remaining() >= 32;
        v0 += grab(partialInput, 8) * K0;
        v0 = Long.rotateRight(v0, 29) + v2;
        v1 += grab(partialInput, 8) * K1;
        v1 = Long.rotateRight(v1, 29) + v3;
        v2 += grab(partialInput, 8) * K2;
        v2 = Long.rotateRight(v2, 29) + v0;
        v3 += grab(partialInput, 8) * K3;
        v3 = Long.rotateRight(v3, 29) + v1;
        ++nChunks;
        return this;
    }

    /**
     * Processes the remaining bytes (less than 32) from the input and finalizes the hash state.
     *
     * @param partialInput The byte buffer, which has fewer than 32 bytes remaining.
     * @return this instance for chaining.
     */
    @Override
    MetroHash64 partialApplyRemaining(final ByteBuffer partialInput) {
        assert partialInput.remaining() < 32;
        if (nChunks > 0) {
            metroHash64_32();
        }
        if (partialInput.remaining() >= 16) {
            metroHash64_16(partialInput);
        }
        if (partialInput.remaining() >= 8) {
            metroHash64_8(partialInput);
        }
        if (partialInput.remaining() >= 4) {
            metroHash64_4(partialInput);
        }
        if (partialInput.remaining() >= 2) {
            metroHash64_2(partialInput);
        }
        if (partialInput.remaining() >= 1) {
            metroHash64_1(partialInput);
        }
        hash ^= Long.rotateRight(hash, 28);
        hash *= K0;
        hash ^= Long.rotateRight(hash, 29);
        return this;
    }

    /**
     * Applies the finalization logic for inputs that were larger than 32 bytes.
     */
    private void metroHash64_32() {
        v2 ^= Long.rotateRight(((v0 + v3) * K0) + v1, 37) * K1;
        v3 ^= Long.rotateRight(((v1 + v2) * K1) + v0, 37) * K0;
        v0 ^= Long.rotateRight(((v0 + v2) * K0) + v3, 37) * K1;
        v1 ^= Long.rotateRight(((v1 + v3) * K1) + v2, 37) * K0;
        hash += v0 ^ v1;
    }

    /**
     * Applies the hashing logic for a 16-byte chunk.
     * 
     * @param bb The ByteBuffer containing the data.
     */
    private void metroHash64_16(final ByteBuffer bb) {
        v0 = hash + grab(bb, 8) * K2;
        v0 = Long.rotateRight(v0, 29) * K3;
        v1 = hash + grab(bb, 8) * K2;
        v1 = Long.rotateRight(v1, 29) * K3;
        v0 ^= Long.rotateRight(v0 * K0, 21) + v1;
        v1 ^= Long.rotateRight(v1 * K3, 21) + v0;
        hash += v1;
    }

    /**
     * Applies the hashing logic for an 8-byte chunk.
     * 
     * @param bb The ByteBuffer containing the data.
     */
    private void metroHash64_8(final ByteBuffer bb) {
        hash += grab(bb, 8) * K3;
        hash ^= Long.rotateRight(hash, 55) * K1;
    }

    /**
     * Applies the hashing logic for a 4-byte chunk.
     * 
     * @param bb The ByteBuffer containing the data.
     */
    private void metroHash64_4(final ByteBuffer bb) {
        hash += grab(bb, 4) * K3;
        hash ^= Long.rotateRight(hash, 26) * K1;
    }

    /**
     * Applies the hashing logic for a 2-byte chunk.
     * 
     * @param bb The ByteBuffer containing the data.
     */
    private void metroHash64_2(final ByteBuffer bb) {
        hash += grab(bb, 2) * K3;
        hash ^= Long.rotateRight(hash, 48) * K1;
    }

    /**
     * Applies the hashing logic for a 1-byte chunk.
     * 
     * @param bb The ByteBuffer containing the data.
     */
    private void metroHash64_1(final ByteBuffer bb) {
        hash += grab(bb, 1) * K3;
        hash ^= Long.rotateRight(hash, 37) * K1;
    }

}
