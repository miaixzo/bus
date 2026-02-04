/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.codec.hash.metro;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Interface for Apache MetroHash algorithm, a set of state-of-the-art hash functions for non-cryptographic use cases.
 * Besides their excellent performance, they are also known for being algorithmically generated.
 *
 * <p>
 * Official implementation:
 * <a href="https://github.com/jandrewrogers/MetroHash">https://github.com/jandrewrogers/MetroHash</a> Official
 * documentation:
 * <a href="http://www.jandrewrogers.com/2015/05/27/metrohash/">http://www.jandrewrogers.com/2015/05/27/metrohash/</a>
 * Adapted from: <a href="https://github.com/postamar/java-metrohash/">https://github.com/postamar/java-metrohash/</a>
 *
 * @param <R> The type of the implementing class, allowing for method chaining.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface MetroHash<R extends MetroHash<R>> {

    /**
     * Creates a {@code MetroHash} object based on the provided seed and hash size.
     *
     * @param seed  The seed value for the hash function.
     * @param is128 A boolean indicating whether to create a 128-bit hash (true) or a 64-bit hash (false).
     * @return A new {@code MetroHash} object, either {@link MetroHash128} or {@link MetroHash64}.
     */
    static MetroHash<?> of(final long seed, final boolean is128) {
        return is128 ? new MetroHash128(seed) : new MetroHash64(seed);
    }

    /**
     * Appends data from the given {@link ByteBuffer} to the hash calculation. This method updates the internal state of
     * the hash function.
     *
     * @param input The {@link ByteBuffer} containing the data to be hashed.
     * @return This {@code MetroHash} instance for method chaining.
     */
    R apply(final ByteBuffer input);

    /**
     * Writes the calculated hash value to the provided {@link ByteBuffer}. The byte order for writing can be specified.
     *
     * @param output    The {@link ByteBuffer} to write the hash result to.
     * @param byteOrder The desired byte order for the output hash.
     * @return This {@code MetroHash} instance for method chaining.
     */
    R write(ByteBuffer output, final ByteOrder byteOrder);

    /**
     * Resets the hash function to its initial state, allowing the object to be reused for a new hash calculation.
     *
     * @return This {@code MetroHash} instance for method chaining.
     */
    R reset();

}
