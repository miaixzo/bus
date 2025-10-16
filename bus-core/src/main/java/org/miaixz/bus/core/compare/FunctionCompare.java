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
package org.miaixz.bus.core.compare;

import java.io.Serial;
import java.util.function.Function;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CompareKit;

/**
 * A comparator that sorts objects based on a value extracted by a specified function.
 *
 * @param <T> the type of objects to be compared.
 * @author Kimi Liu
 * @since Java 17+
 */
public class FunctionCompare<T> extends NullCompare<T> {

    @Serial
    private static final long serialVersionUID = 2852261525153L;

    /**
     * Constructs a new {@code FunctionCompare}.
     *
     * @param nullGreater whether {@code null} values should be placed at the end.
     * @param compareSelf if {@code true}, and the extracted values are equal, the objects themselves will be compared.
     *                    This prevents objects with the same sort key from being treated as equal, which can avoid
     *                    deduplication in sets.
     * @param func        the function to extract the {@link Comparable} value from the object.
     */
    public FunctionCompare(final boolean nullGreater, final boolean compareSelf,
            final Function<T, Comparable<?>> func) {
        super(nullGreater, (a, b) -> {
            // Extract comparable values using the provided function
            final Comparable<?> v1;
            final Comparable<?> v2;
            try {
                v1 = func.apply(a);
                v2 = func.apply(b);
            } catch (final Exception e) {
                throw new InternalException(e);
            }

            // First, compare the extracted values. If they are equal, decide whether to compare the objects themselves.
            // `compareSelf=false` is useful for multi-level comparisons, such as sorting by multiple fields.
            int result = CompareKit.compare(v1, v2, nullGreater);
            if (compareSelf && 0 == result) {
                // Avoid filtering out objects with the same sort key in a TreeSet/TreeMap
                result = CompareKit.compare(a, b, nullGreater);
            }
            return result;
        });
    }

}
