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

/**
 * Comparator for sorting beans based on multiple fields.
 *
 * @param <T> the type of the bean to be compared.
 * @author Kimi Liu
 * @since Java 17+
 */
public class FieldsCompare<T> extends NullCompare<T> {

    @Serial
    private static final long serialVersionUID = 2852261195893L;

    /**
     * Constructs a new {@code FieldsCompare}.
     *
     * @param beanClass  the class of the bean.
     * @param fieldNames the names of the fields to compare by, in order of priority.
     */
    public FieldsCompare(final Class<T> beanClass, final String... fieldNames) {
        this(true, beanClass, fieldNames);
    }

    /**
     * Constructs a new {@code FieldsCompare}.
     *
     * @param nullGreater whether {@code null} values should be placed at the end.
     * @param beanClass   the class of the bean.
     * @param fieldNames  the names of the fields to compare by, in order of priority.
     */
    public FieldsCompare(final boolean nullGreater, final Class<T> beanClass, final String... fieldNames) {
        super(nullGreater, (a, b) -> {
            Field field;
            for (final String fieldName : fieldNames) {
                field = FieldKit.getField(beanClass, fieldName);
                Assert.notNull(field, "Field [{}] not found in Class [{}]", fieldName, beanClass.getName());
                final int compare = new FieldCompare<>(true, false, field).compare(a, b);
                if (0 != compare) {
                    return compare;
                }
            }
            return 0;
        });
    }

}
