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
package org.miaixz.bus.core.builder;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 使用反射帮助实现{@link Object#toString()}方法
 * <p>
 * 该类使用反射来确定要追加的字段。因为这些字段通常是私有的，所以该类使用
 * {@link AccessibleObject#setAccessible(AccessibleObject[], boolean)}
 * 来更改字段的可见性。在安全管理器下，除非正确设置了适当的权限，否则此操作将失败
 * </p>
 *
 * <p>
 * 此方法的典型调用如下所示:
 * </p>
 * <pre>
 * public String toString() {
 *     return ReflectionToStringBuilder.toString(this);
 * }
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReflectionToStringBuilder extends ToStringBuilder {

    /**
     * 要从输出中排除哪些字段名。适用于 <code>"password"</code>等字段
     */
    protected String[] excludeFieldNames;
    /**
     * 是否附加静态字段
     */
    private boolean appendStatics = false;
    /**
     * 是否附加忽略字段
     */
    private boolean appendTransients = false;
    /**
     * 是否附加为空的字段
     */
    private boolean excludeNullValues;
    /**
     * 停止添加字段的最后一个超类
     */
    private Class<?> upToClass = null;

    public ReflectionToStringBuilder(final Object object) {
        super(checkNotNull(object));
    }

    public ReflectionToStringBuilder(final Object object,
                                     final ToStringStyle style) {
        super(checkNotNull(object), style);
    }

    public ReflectionToStringBuilder(final Object object,
                                     final ToStringStyle style,
                                     final StringBuffer buffer) {
        super(checkNotNull(object), style, buffer);
    }

    /**
     * 构造函数
     *
     * @param <T>              对象的类型
     * @param object           对象来构建一个 <code>toString</code>
     * @param style            创建的<code>toString</code>，可以是null
     * @param buffer           要填充的StringBuffer，可以是null
     * @param reflectUpToClass 要反映到(包括)的超类可以是null
     * @param outputTransients 是否包含忽略字段
     * @param outputStatics    是否包含静态字段
     */
    public <T> ReflectionToStringBuilder(
            final T object, final ToStringStyle style, final StringBuffer buffer,
            final Class<? super T> reflectUpToClass, final boolean outputTransients, final boolean outputStatics) {
        super(checkNotNull(object), style, buffer);
        this.setUpToClass(reflectUpToClass);
        this.setAppendTransients(outputTransients);
        this.setAppendStatics(outputStatics);
    }


    /**
     * 构造函数
     *
     * @param <T>               对象的类型
     * @param object            对象来构建一个 <code>toString</code>
     * @param style             创建的<code>toString</code>，可以是null
     * @param buffer            要填充的StringBuffer，可以是null
     * @param reflectUpToClass  要反映到(包括)的超类可以是null
     * @param outputTransients  是否包含忽略字段
     * @param outputStatics     是否包含静态字段
     * @param excludeNullValues 是否排除值为空的字段
     */
    public <T> ReflectionToStringBuilder(
            final T object, final ToStringStyle style,
            final StringBuffer buffer,
            final Class<? super T> reflectUpToClass,
            final boolean outputTransients,
            final boolean outputStatics,
            final boolean excludeNullValues) {
        super(checkNotNull(object), style, buffer);
        this.setUpToClass(reflectUpToClass);
        this.setAppendTransients(outputTransients);
        this.setAppendStatics(outputStatics);
        this.setExcludeNullValues(excludeNullValues);
    }

    public static String toString(final Object object) {
        return toString(object, null, false, false, null);
    }

    public static String toString(final Object object, final ToStringStyle style) {
        return toString(object, style, false, false, null);
    }

    public static String toString(final Object object, final ToStringStyle style, final boolean outputTransients) {
        return toString(object, style, outputTransients, false, null);
    }

    public static String toString(final Object object, final ToStringStyle style, final boolean outputTransients, final boolean outputStatics) {
        return toString(object, style, outputTransients, outputStatics, null);
    }

    public static <T> String toString(
            final T object, final ToStringStyle style, final boolean outputTransients,
            final boolean outputStatics, final Class<? super T> reflectUpToClass) {
        return new ReflectionToStringBuilder(object, style, null, reflectUpToClass, outputTransients, outputStatics)
                .toString();
    }

    public static <T> String toString(
            final T object, final ToStringStyle style, final boolean outputTransients,
            final boolean outputStatics, final boolean excludeNullValues, final Class<? super T> reflectUpToClass) {
        return new ReflectionToStringBuilder(object, style, null, reflectUpToClass, outputTransients, outputStatics, excludeNullValues)
                .toString();
    }

    public static String toStringExclude(final Object object, final Collection<String> excludeFieldNames) {
        return toStringExclude(object, toNoNullStringArray(excludeFieldNames));
    }

    static String[] toNoNullStringArray(final Collection<String> collection) {
        if (null == collection) {
            return Normal.EMPTY_STRING_ARRAY;
        }
        return toNoNullStringArray(collection.toArray());
    }

    static String[] toNoNullStringArray(final Object[] array) {
        final List<String> list = new ArrayList<>(array.length);
        for (final Object e : array) {
            if (null != e) {
                list.add(e.toString());
            }
        }
        return list.toArray(Normal.EMPTY_STRING_ARRAY);
    }

    public static String toStringExclude(final Object object, final String... excludeFieldNames) {
        return new ReflectionToStringBuilder(object).setExcludeFieldNames(excludeFieldNames).toString();
    }

    private static Object checkNotNull(final Object object) {
        Assert.isTrue(null != object, "The Object passed in should not be null.");
        return object;
    }

    /**
     * 返回是否附加给定的字段
     *
     * <ul>
     * <li>Transient fields are appended only if {@link #isAppendTransients()} returns <code>true</code>.
     * <li>Static fields are appended only if {@link #isAppendStatics()} returns <code>true</code>.
     * <li>Inner class fields are not appended.</li>
     * </ul>
     *
     * @param field 字段属性.
     * @return 是否附加给定的字段.
     */
    protected boolean accept(final Field field) {
        if (field.getName().indexOf(Symbol.C_DOLLAR) != -1) {
            return false;
        }
        if (Modifier.isTransient(field.getModifiers()) && !this.isAppendTransients()) {
            return false;
        }
        if (Modifier.isStatic(field.getModifiers()) && !this.isAppendStatics()) {
            return false;
        }
        if (null != this.excludeFieldNames
                && Arrays.binarySearch(this.excludeFieldNames, field.getName()) >= 0) {
            return false;
        }
        return !field.isAnnotationPresent(ToStringExclude.class);
    }

    /**
     * 附加由给定类的给定对象定义的字段和值.
     *
     * @param clazz 对象参数的类
     */
    protected void appendFieldsIn(final Class<?> clazz) {
        if (clazz.isArray()) {
            this.reflectionAppendArray(this.getObject());
            return;
        }
        final Field[] fields = clazz.getDeclaredFields();
        AccessibleObject.setAccessible(fields, true);
        for (final Field field : fields) {
            final String fieldName = field.getName();
            if (this.accept(field)) {
                try {
                    final Object fieldValue = this.getValue(field);
                    if (!excludeNullValues || null != fieldValue) {
                        this.append(fieldName, fieldValue, !field.isAnnotationPresent(ToStringSummary.class));
                    }
                } catch (final IllegalAccessException ex) {
                    throw new InternalError("Unexpected IllegalAccessException: " + ex.getMessage());
                }
            }
        }
    }

    public String[] getExcludeFieldNames() {
        return this.excludeFieldNames.clone();
    }

    public ReflectionToStringBuilder setExcludeFieldNames(final String... excludeFieldNamesParam) {
        if (null == excludeFieldNamesParam) {
            this.excludeFieldNames = null;
        } else {
            //clone and remove nulls
            this.excludeFieldNames = toNoNullStringArray(excludeFieldNamesParam);
            Arrays.sort(this.excludeFieldNames);
        }
        return this;
    }

    public Class<?> getUpToClass() {
        return this.upToClass;
    }

    public void setUpToClass(final Class<?> clazz) {
        if (null != clazz) {
            final Object object = getObject();
            if (null != object && !clazz.isInstance(object)) {
                throw new IllegalArgumentException("Specified class is not a superclass of the object");
            }
        }
        this.upToClass = clazz;
    }

    protected Object getValue(final Field field) throws IllegalAccessException {
        return field.get(this.getObject());
    }

    public boolean isAppendStatics() {
        return this.appendStatics;
    }

    public void setAppendStatics(final boolean appendStatics) {
        this.appendStatics = appendStatics;
    }

    public boolean isAppendTransients() {
        return this.appendTransients;
    }

    public void setAppendTransients(final boolean appendTransients) {
        this.appendTransients = appendTransients;
    }

    public boolean isExcludeNullValues() {
        return this.excludeNullValues;
    }

    public void setExcludeNullValues(final boolean excludeNullValues) {
        this.excludeNullValues = excludeNullValues;
    }

    public ReflectionToStringBuilder reflectionAppendArray(final Object array) {
        this.getStyle().reflectionAppendArrayDetail(this.getStringBuffer(), null, array);
        return this;
    }

    @Override
    public String toString() {
        if (null == this.getObject()) {
            return this.getStyle().getNullText();
        }
        Class<?> clazz = this.getObject().getClass();
        this.appendFieldsIn(clazz);
        while (null != clazz.getSuperclass() && clazz != this.getUpToClass()) {
            clazz = clazz.getSuperclass();
            this.appendFieldsIn(clazz);
        }
        return super.toString();
    }

}
