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

import org.miaixz.bus.core.codec.No128;
import org.miaixz.bus.core.codec.hash.Hash128;

/**
 * 128-bit implementation of the Apache MetroHash algorithm, a set of state-of-the-art hash functions for
 * non-cryptographic use cases. Besides their excellent performance, they are also known for being algorithmically
 * generated.
 *
 * <p>
 * Official implementation:
 * <a href="https://github.com/jandrewrogers/MetroHash">https://github.com/jandrewrogers/MetroHash</a> Official
 * documentation:
 * <a href="http://www.jandrewrogers.com/2015/05/27/metrohash/">http://www.jandrewrogers.com/2015/05/27/metrohash/</a>
 * Adapted from: <a href="https://github.com/postamar/java-metrohash/">https://github.com/postamar/java-metrohash/</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MetroHash128 extends AbstractMetroHash<MetroHash128> implements Hash128<byte[]> {

    /**
     * Constant K0 used in the MetroHash128 algorithm.
     */
    private static final long K0 = 0xC83A91E1L;
    /**
     * Constant K1 used in the MetroHash128 algorithm.
     */
    private static final long K1 = 0x8648DBDBL;
    /**
     * Constant K2 used in the MetroHash128 algorithm.
     */
    private static final long K2 = 0x7BDEC03BL;
    /**
     * Constant K3 used in the MetroHash128 algorithm.
     */
    private static final long K3 = 0x2F5870A5L;

    /**
     * Constructs a new {@code MetroHash128} instance with the specified seed.
     *
     * @param seed The seed value for the hash function.
     */
    public MetroHash128(final long seed) {
        super(seed);
    }

    /**
     * Creates a new {@code MetroHash128} object with the given seed.
     *
     * @param seed The seed value for the hash function.
     * @return A new {@code MetroHash128} object.
     */
    public static MetroHash128 of(final long seed) {
        return new MetroHash128(seed);
    }

    /**
     * Resets the hash function to its initial state, allowing the object to be reused for a new hash calculation. The
     * internal state variables (v0, v1, v2, v3, nChunks) are reinitialized based on the seed.
     *
     * @return This {@code MetroHash128} instance for method chaining.
     */
    @Override
    public MetroHash128 reset() {
        v0 = (seed - K0) * K3;
        v1 = (seed + K1) * K2;
        v2 = (seed + K0) * K2;
        v3 = (seed - K1) * K3;
        nChunks = 0;
        return this;
    }

    /**
     * Retrieves the computed 128-bit hash value as a {@link No128} object. The most significant bits are stored in
     * {@code v0} and the least significant bits in {@code v1}.
     *
     * @return The 128-bit hash value encapsulated in a {@link No128} object.
     */
    public No128 get() {
        return new No128(v0, v1);
    }

    /**
     * Computes the 128-bit MetroHash for the given byte array. This method wraps the byte array in a {@link ByteBuffer}
     * and then applies the hash function.
     *
     * @param bytes The byte array for which to compute the hash.
     * @return The 128-bit hash value encapsulated in a {@link No128} object.
     */
    @Override
    public No128 hash128(final byte[] bytes) {
        return apply(ByteBuffer.wrap(bytes)).get();
    }

    /**
     * Writes the calculated 128-bit hash value to the provided {@link ByteBuffer}. The byte order for writing can be
     * specified.
     *
     * @param output    The {@link ByteBuffer} to write the hash result to.
     * @param byteOrder The desired byte order for the output hash.
     * @return This {@code MetroHash128} instance for method chaining.
     */
    @Override
    public MetroHash128 write(final ByteBuffer output, final ByteOrder byteOrder) {
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            writeLittleEndian(v0, output);
            writeLittleEndian(v1, output);
        } else {
            output.asLongBuffer().put(v1).put(v0);
        }
        return this;
    }

    /**
     * Processes a 32-byte chunk of input data for the MetroHash128 algorithm. This method updates the internal state
     * variables (v0, v1, v2, v3) based on the input chunk.
     *
     * @param partialInput The {@link ByteBuffer} containing at least 32 bytes of data.
     * @return This {@code MetroHash128} instance for method chaining.
     */
    @Override
    MetroHash128 partialApply32ByteChunk(final ByteBuffer partialInput) {
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
     * Processes any remaining bytes (less than 32) after full 32-byte chunks have been processed. This method handles
     * the finalization steps of the MetroHash128 algorithm, including mixing the internal state and processing the tail
     * bytes.
     *
     * @param partialInput The {@link ByteBuffer} containing the remaining data (less than 32 bytes).
     * @return This {@code MetroHash128} instance for method chaining.
     */
    @Override
    MetroHash128 partialApplyRemaining(final ByteBuffer partialInput) {
        assert partialInput.remaining() < 32;
        if (nChunks > 0) {
            metroHash128_32();
        }
        if (partialInput.remaining() >= 16) {
            metroHash128_16(partialInput);
        }
        if (partialInput.remaining() >= 8) {
            metroHash128_8(partialInput);
        }
        if (partialInput.remaining() >= 4) {
            metroHash128_4(partialInput);
        }
        if (partialInput.remaining() >= 2) {
            metroHash128_2(partialInput);
        }
        if (partialInput.remaining() >= 1) {
            metroHash128_1(partialInput);
        }
        v0 += Long.rotateRight(v0 * K0 + v1, 13);
        v1 += Long.rotateRight(v1 * K1 + v0, 37);
        v0 += Long.rotateRight(v0 * K2 + v1, 13);
        v1 += Long.rotateRight(v1 * K3 + v0, 37);
        return this;
    }

    /**
     * Performs a 32-byte finalization step for MetroHash128. This mixes the internal state variables (v0, v1, v2, v3)
     * to contribute to the final hash.
     */
    private void metroHash128_32() {
        v2 ^= Long.rotateRight((v0 + v3) * K0 + v1, 21) * K1;
        v3 ^= Long.rotateRight((v1 + v2) * K1 + v0, 21) * K0;
        v0 ^= Long.rotateRight((v0 + v2) * K0 + v3, 21) * K1;
        v1 ^= Long.rotateRight((v1 + v3) * K1 + v2, 21) * K0;
    }

    /**
     * Processes a 16-byte tail chunk for MetroHash128.
     *
     * @param bb The {@link ByteBuffer} containing the 16-byte tail.
     */
    private void metroHash128_16(final ByteBuffer bb) {
        v0 += grab(bb, 8) * K2;
        v0 = Long.rotateRight(v0, 33) * K3;
        v1 += grab(bb, 8) * K2;
        v1 = Long.rotateRight(v1, 33) * K3;
        v0 ^= Long.rotateRight(v0 * K2 + v1, 45) * K1;
        v1 ^= Long.rotateRight(v1 * K3 + v0, 45) * K0;
    }

    /**
     * Processes an 8-byte tail chunk for MetroHash128.
     *
     * @param bb The {@link ByteBuffer} containing the 8-byte tail.
     */
    private void metroHash128_8(final ByteBuffer bb) {
        v0 += grab(bb, 8) * K2;
        v0 = Long.rotateRight(v0, 33) * K3;
        v0 ^= Long.rotateRight(v0 * K2 + v1, 27) * K1;
    }

    /**
     * Processes a 4-byte tail chunk for MetroHash128.
     *
     * @param bb The {@link ByteBuffer} containing the 4-byte tail.
     */
    private void metroHash128_4(final ByteBuffer bb) {
        v1 += grab(bb, 4) * K2;
        v1 = Long.rotateRight(v1, 33) * K3;
        v1 ^= Long.rotateRight(v1 * K3 + v0, 46) * K0;
    }

    /**
     * Processes a 2-byte tail chunk for MetroHash128.
     *
     * @param bb The {@link ByteBuffer} containing the 2-byte tail.
     */
    private void metroHash128_2(final ByteBuffer bb) {
        v0 += grab(bb, 2) * K2;
        v0 = Long.rotateRight(v0, 33) * K3;
        v0 ^= Long.rotateRight(v0 * K2 + v1, 22) * K1;
    }

    /**
     * Processes a 1-byte tail chunk for MetroHash128.
     *
     * @param bb The {@link ByteBuffer} containing the 1-byte tail.
     */
    private void metroHash128_1(final ByteBuffer bb) {
        v1 += grab(bb, 1) * K2;
        v1 = Long.rotateRight(v1, 33) * K3;
        v1 ^= Long.rotateRight(v1 * K3 + v0, 58) * K0;
    }

}
