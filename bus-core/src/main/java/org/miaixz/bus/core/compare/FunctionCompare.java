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
