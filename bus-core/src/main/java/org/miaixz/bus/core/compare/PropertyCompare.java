/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
