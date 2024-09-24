/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.lang.reflect.field;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.miaixz.bus.core.convert.Converter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.reflect.Invoker;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.ReflectKit;

/**
 * 字段调用器 通过反射读取或赋值字段 读取字段值：
 * 
 * <pre>{@code
 * FieldInvoker.of(Field).invoke(object);
 * }</pre>
 * <p>
 * 赋值字段值：
 * 
 * <pre>{@code
 * FieldInvoker.of(Field).invoke(object, value);
 * }</pre>
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class FieldInvoker implements Invoker {

    private final Field field;
    private Converter converter;

    /**
     * 构造
     *
     * @param field 字段
     */
    public FieldInvoker(final Field field) {
        this.field = Assert.notNull(field);
    }

    /**
     * 创建字段调用器
     *
     * @param field 字段
     * @return {@code FieldInvoker}
     */
    public static FieldInvoker of(final Field field) {
        return new FieldInvoker(field);
    }

    /**
     * 获取字段
     *
     * @return 字段
     */
    public Field getField() {
        return this.field;
    }

    @Override
    public String getName() {
        return this.field.getName();
    }

    @Override
    public Type getType() {
        return field.getGenericType();
    }

    @Override
    public Class<?> getTypeClass() {
        return field.getType();
    }

    /**
     * 设置字段值转换器
     *
     * @param converter 转换器，{@code null}表示不转换
     * @return this
     */
    public FieldInvoker setConverter(final Converter converter) {
        this.converter = converter;
        return this;
    }

    @Override
    public <T> T invoke(final Object target, final Object... args) {
        if (ArrayKit.isEmpty(args)) {
            // 默认取值
            return (T) invokeGet(target);
        } else if (args.length == 1) {
            invokeSet(target, args[0]);
            return null;
        }

        throw new InternalException("Field [{}] cannot be set with [{}] args", field.getName(), args.length);
    }

    /**
     * 获取字段值
     *
     * @param object 对象，static字段则此字段为null
     * @return 字段值
     * @throws InternalException 包装IllegalAccessException异常
     */
    public Object invokeGet(Object object) throws InternalException {
        if (null == field) {
            return null;
        }
        if (object instanceof Class) {
            // 静态字段获取时对象为null
            object = null;
        }

        ReflectKit.setAccessible(field);
        final Object result;
        try {
            result = field.get(object);
        } catch (final IllegalAccessException e) {
            throw new InternalException(e, "IllegalAccess for {}.{}", field.getDeclaringClass(), field.getName());
        }
        return result;
    }

    /**
     * 设置字段值，传入的字段值必须和字段类型一致，否则抛出异常
     *
     * @param object 对象，如果是static字段，此参数为null
     * @param value  值，值类型必须与字段类型匹配
     * @throws InternalException 包装IllegalAccessException异常
     */
    public void invokeSet(final Object object, final Object value) throws InternalException {
        ReflectKit.setAccessible(field);
        try {
            field.set(object instanceof Class ? null : object, convertValue(value));
        } catch (final IllegalAccessException e) {
            throw new InternalException(e, "IllegalAccess for [{}.{}]",
                    null == object ? field.getDeclaringClass() : object, field.getName());
        }
    }

    /**
     * 转换值类型
     *
     * @param value 值
     * @return 转换后的值
     */
    private Object convertValue(final Object value) {
        if (null == converter) {
            return value;
        }

        // 值类型检查和转换
        final Class<?> fieldType = field.getType();
        if (null != value) {
            if (!fieldType.isAssignableFrom(value.getClass())) {
                // 对于类型不同的字段，尝试转换，转换失败则使用原对象类型
                final Object targetValue = converter.convert(fieldType, value);
                if (null != targetValue) {
                    return targetValue;
                }
            }
        } else {
            // 获取null对应默认值，防止原始类型造成空指针问题
            return ClassKit.getDefaultValue(fieldType);
        }

        return value;
    }

}
