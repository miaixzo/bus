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
import java.lang.reflect.Field;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Comparator for sorting beans based on a specific field.
 *
 * @param <T> the type of the bean to be compared.
 * @author Kimi Liu
 * @since Java 17+
 */
public class FieldCompare<T> extends FunctionCompare<T> {

    @Serial
    private static final long serialVersionUID = 2852260880753L;

    /**
     * Constructs a new {@code FieldCompare}.
     *
     * @param beanClass the class of the bean.
     * @param fieldName the name of the field to compare by.
     */
    public FieldCompare(final Class<T> beanClass, final String fieldName) {
        this(getNonNullField(beanClass, fieldName));
    }

    /**
     * Constructs a new {@code FieldCompare}.
     *
     * @param field the field to compare by.
     */
    public FieldCompare(final Field field) {
        this(true, true, field);
    }

    /**
     * Constructs a new {@code FieldCompare}.
     *
     * @param nullGreater whether {@code null} values should be placed at the end.
     * @param compareSelf whether to compare the objects themselves if the field values are equal. If {@code false},
     *                    objects with the same field value will be considered equal, which may lead to deduplication.
     * @param field       the field to compare by.
     */
    public FieldCompare(final boolean nullGreater, final boolean compareSelf, final Field field) {
        super(nullGreater, compareSelf, (bean) -> (Comparable<?>) FieldKit
                .getFieldValue(bean, Assert.notNull(field, "Field must be not null!")));
    }

    /**
     * Gets the specified field and checks if it exists.
     *
     * @param beanClass the class of the bean.
     * @param fieldName the name of the field.
     * @return the non-null {@link Field}.
     * @throws IllegalArgumentException if the field is not found.
     */
    private static Field getNonNullField(final Class<?> beanClass, final String fieldName) {
        final Field field = FieldKit.getField(beanClass, fieldName);
        if (field == null) {
            throw new IllegalArgumentException(
                    StringKit.format("Field [{}] not found in Class [{}]", fieldName, beanClass.getName()));
        }
        return field;
    }

}
