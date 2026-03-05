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
package org.miaixz.bus.core.text.bloom;

import java.io.Serial;
import java.util.BitSet;

import org.miaixz.bus.core.lang.Assert;

/**
 * Abstract Bloom filter implementation. This class provides a basic structure for Bloom filters, managing a
 * {@link BitSet} to store the hashed values and defining the common operations for Bloom filters.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractFilter implements BloomFilter {

    @Serial
    private static final long serialVersionUID = 2852235255163L;

    /**
     * The underlying {@link BitSet} used to store the presence of elements. This bit set represents the Bloom filter's
     * bit array.
     */
    protected final BitSet bitSet;
    /**
     * The capacity of the Bloom filter, representing the number of bits in the {@link BitSet}.
     */
    protected int size;

    /**
     * Constructs a new AbstractFilter with the specified size.
     *
     * @param size The capacity of the Bloom filter, must be greater than 0.
     * @throws IllegalArgumentException if the size is not greater than 0.
     */
    public AbstractFilter(final int size) {
        Assert.isTrue(size > 0, "Size must be greater than 0.");
        this.size = size;
        this.bitSet = new BitSet(size);
    }

    /**
     * Contains method.
     *
     * @return the boolean value
     */
    @Override
    public boolean contains(final String text) {
        return bitSet.get(hash(text));
    }

    /**
     * Add method.
     *
     * @return the boolean value
     */
    @Override
    public boolean add(final String text) {
        final int hash = hash(text);
        if (bitSet.get(hash)) {
            return false;
        }

        bitSet.set(hash);
        return true;
    }

    /**
     * Abstract method to define a custom hash function for the Bloom filter. Implementations should provide a hash
     * value for the given text.
     *
     * @param text The string to be hashed.
     * @return The hash value (an integer) for the given string.
     */
    public abstract int hash(String text);

}
