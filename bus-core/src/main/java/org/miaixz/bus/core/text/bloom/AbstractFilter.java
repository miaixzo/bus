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

    /**
     * The serial version UID for serialization.
     */
    @Serial
    private static final long serialVersionUID = 2852235255163L;

    /**
     * The underlying {@link BitSet} used to store the presence of elements. This bit set represents the Bloom filter's
     * bit array.
     */
    private final BitSet bitSet;
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

    @Override
    public boolean contains(final String text) {
        return bitSet.get(Math.abs(hash(text)));
    }

    @Override
    public boolean add(final String text) {
        final int hash = Math.abs(hash(text));
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
