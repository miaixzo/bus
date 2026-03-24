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
 * @since Java 21+
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
