/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.compare;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.toolkit.ClassKit;
import org.miaixz.bus.core.toolkit.ReflectKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.lang.reflect.Field;

/**
 * Bean字段排序器
 *
 * @param <T> 被比较的Bean
 * @author Kimi Liu
 * @since Java 17+
 */
public class FieldCompare<T> extends FuncCompare<T> {

    private static final long serialVersionUID = 1L;

    /**
     * 构造
     *
     * @param beanClass Bean类
     * @param fieldName 字段名
     */
    public FieldCompare(Class<T> beanClass, String fieldName) {
        this(getNonNullField(beanClass, fieldName));
    }

    /**
     * 构造
     *
     * @param field 字段
     */
    public FieldCompare(Field field) {
        this(true, field);
    }

    /**
     * 构造
     *
     * @param nullGreater 是否{@code null}在后
     * @param field       字段
     */
    public FieldCompare(boolean nullGreater, Field field) {
        super(nullGreater, (bean) ->
                (Comparable<?>) ReflectKit.getFieldValue(bean,
                        Assert.notNull(field, "Field must be not null!")));
    }

    /**
     * 获取字段，附带检查字段不存在的问题。
     *
     * @param beanClass Bean类
     * @param fieldName 字段名
     * @return 非null字段
     */
    private static Field getNonNullField(Class<?> beanClass, String fieldName) {
        final Field field = ClassKit.getDeclaredField(beanClass, fieldName);
        if (field == null) {
            throw new IllegalArgumentException(StringKit.format("Field [{}] not found in Class [{}]", fieldName, beanClass.getName()));
        }
        return field;
    }

}
