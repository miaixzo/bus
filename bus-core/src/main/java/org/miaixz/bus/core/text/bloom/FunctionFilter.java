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
import java.util.function.Function;

/**
 * A Bloom filter implementation based on a custom hash function provided by a {@link Function}. This filter extends
 * {@link AbstractFilter} and uses a user-defined hash function to determine the bit positions for elements.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FunctionFilter extends AbstractFilter {

    /**
     * The serial version UID for serialization.
     */
    @Serial
    private static final long serialVersionUID = 2852235578011L;

    /**
     * The hash function used to generate hash values for input strings. This function maps a string to a numeric value.
     */
    private final Function<String, Number> hashFunc;

    /**
     * Constructs a new {@code FunctionFilter} with the specified size and hash function.
     *
     * @param size     The capacity of the Bloom filter (number of bits in the bit set).
     * @param hashFunc The hash function to be used for mapping strings to numeric values.
     */
    public FunctionFilter(final int size, final Function<String, Number> hashFunc) {
        super(size);
        this.hashFunc = hashFunc;
    }

    /**
     * Creates a new {@code FunctionFilter} instance.
     *
     * @param size     The capacity of the Bloom filter (number of bits in the bit set).
     * @param hashFunc The hash function to be used for mapping strings to numeric values.
     * @return A new {@code FunctionFilter} instance.
     */
    public static FunctionFilter of(final int size, final Function<String, Number> hashFunc) {
        return new FunctionFilter(size, hashFunc);
    }

    @Override
    public int hash(final String text) {
        // Applies the custom hash function and then takes the modulo of the filter's size
        // to ensure the hash value fits within the bit set's bounds.
        return hashFunc.apply(text).intValue() % size;
    }

}
