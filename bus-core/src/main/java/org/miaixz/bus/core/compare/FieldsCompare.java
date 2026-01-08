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
