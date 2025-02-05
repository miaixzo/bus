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

import java.lang.reflect.Field;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Bean字段排序器 参阅feilong-core中的PropertyComparator
 *
 * @param <T> 被比较的Bean
 * @author Kimi Liu
 * @since Java 17+
 */
public class FieldCompare<T> extends FunctionCompare<T> {

    private static final long serialVersionUID = -1L;

    /**
     * 构造
     *
     * @param beanClass Bean类
     * @param fieldName 字段名
     */
    public FieldCompare(final Class<T> beanClass, final String fieldName) {
        this(getNonNullField(beanClass, fieldName));
    }

    /**
     * 构造
     *
     * @param field 字段
     */
    public FieldCompare(final Field field) {
        this(true, true, field);
    }

    /**
     * 构造
     *
     * @param nullGreater 是否{@code null}在后
     * @param compareSelf 在字段值相同情况下，是否比较对象本身。 如果此项为{@code false}，字段值比较后为0会导致对象被认为相同，可能导致被去重。
     * @param field       字段
     */
    public FieldCompare(final boolean nullGreater, final boolean compareSelf, final Field field) {
        super(nullGreater, compareSelf, (bean) -> (Comparable<?>) FieldKit.getFieldValue(bean,
                Assert.notNull(field, "Field must be not null!")));
    }

    /**
     * 获取字段，附带检查字段不存在的问题。
     *
     * @param beanClass Bean类
     * @param fieldName 字段名
     * @return 非null字段
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
