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
package org.miaixz.bus.core.codec.hash.metro;

import java.nio.ByteBuffer;

/**
 * Abstract implementation of the MetroHash algorithm. MetroHash is a set of state-of-the-art hash functions for
 * non-cryptographic use cases.
 *
 * <p>
 * Official implementation:
 * <a href="https://github.com/jandrewrogers/MetroHash">https://github.com/jandrewrogers/MetroHash</a><br>
 * Official documentation: <a href=
 * "http://www.jandrewrogers.com/2015/05/27/metrohash/">http://www.jandrewrogers.com/2015/05/27/metrohash/</a><br>
 * Ported from: <a href="https://github.com/postamar/java-metrohash/">https://github.com/postamar/java-metrohash/</a>
 *
 * @param <R> The return type of the fluent API, which is the concrete implementation class.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractMetroHash<R extends AbstractMetroHash<R>> implements MetroHash<R> {

    /**
     * The seed for the hash function.
     */
    final long seed;
    /**
     * Internal state variables for the hash calculation.
     */
    long v0, v1, v2, v3;
    /**
     * The number of 32-byte chunks processed.
     */
    long nChunks;

    /**
     * Constructs a new MetroHash instance with a specified seed.
     *
     * @param seed The seed value.
     */
    public AbstractMetroHash(final long seed) {
        this.seed = seed;
        reset();
    }

    /**
     * Grabs a long value of a specified length from the ByteBuffer in little-endian order.
     *
     * @param bb     The ByteBuffer to read from.
     * @param length The number of bytes to read (up to 8).
     * @return The resulting long value.
     */
    static long grab(final ByteBuffer bb, final int length) {
        long result = bb.get() & 0xFFL;
        for (int i = 1; i < length; i++) {
            result |= (bb.get() & 0xFFL) << (i << 3);
        }
        return result;
    }

    /**
     * Writes a long value to the ByteBuffer in little-endian order.
     *
     * @param hash   The long value to write.
     * @param output The ByteBuffer to write to.
     */
    static void writeLittleEndian(final long hash, final ByteBuffer output) {
        for (int i = 0; i < 8; i++) {
            output.put((byte) (hash >>> (i * 8)));
        }
    }

    /**
     * Apply method.
     *
     * @return the R value
     */
    @Override
    public R apply(final ByteBuffer input) {
        reset();
        while (input.remaining() >= 32) {
            partialApply32ByteChunk(input);
        }
        return partialApplyRemaining(input);
    }

    /**
     * Processes a 32-byte chunk from the input and updates the hash state.
     *
     * @param partialInput The byte buffer, which must have at least 32 bytes remaining.
     * @return this instance for chaining.
     */
    abstract R partialApply32ByteChunk(ByteBuffer partialInput);

    /**
     * Processes the remaining bytes (less than 32) from the input and finalizes the hash state.
     *
     * @param partialInput The byte buffer, which has fewer than 32 bytes remaining.
     * @return this instance for chaining.
     */
    abstract R partialApplyRemaining(ByteBuffer partialInput);

}
