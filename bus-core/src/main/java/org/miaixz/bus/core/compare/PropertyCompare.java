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

import org.miaixz.bus.core.xyz.BeanKit;

/**
 * A comparator for sorting beans by a specified property. Supports reading properties from multiple levels of a bean.
 *
 * @param <T> the type of the bean to be compared.
 * @author Kimi Liu
 * @since Java 17+
 */
public class PropertyCompare<T> extends FunctionCompare<T> {

    @Serial
    private static final long serialVersionUID = 2852262713689L;

    /**
     * Constructs a new {@code PropertyCompare}, with {@code null} values placed at the end (ascending order).
     *
     * @param property the name of the property to compare by.
     */
    public PropertyCompare(final String property) {
        this(property, true);
    }

    /**
     * Constructs a new {@code PropertyCompare}.
     *
     * @param property      the name of the property to compare by.
     * @param isNullGreater whether {@code null} values should be placed at the end (for ascending order).
     */
    public PropertyCompare(final String property, final boolean isNullGreater) {
        this(property, true, isNullGreater);
    }

    /**
     * Constructs a new {@code PropertyCompare}.
     *
     * @param property      the name of the property to compare by.
     * @param compareSelf   if {@code true}, and the property values are equal, the objects themselves will be compared.
     *                      This prevents objects with the same sort key from being treated as equal, which can avoid
     *                      deduplication.
     * @param isNullGreater whether {@code null} values should be placed at the end (for ascending order).
     */
    public PropertyCompare(final String property, final boolean compareSelf, final boolean isNullGreater) {
        super(isNullGreater, compareSelf, (bean) -> (Comparable<?>) BeanKit.getProperty(bean, property));
    }

}
