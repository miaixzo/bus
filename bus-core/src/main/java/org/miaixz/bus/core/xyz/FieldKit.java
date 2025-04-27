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
package org.miaixz.bus.core.xyz;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.convert.CompositeConverter;
import org.miaixz.bus.core.convert.Converter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.annotation.Alias;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.reflect.field.FieldInvoker;
import org.miaixz.bus.core.lang.reflect.field.FieldReflect;

/**
 * 反射中{@link Field}字段工具类，包括字段获取和字段赋值。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FieldKit {

    /**
     * 字段缓存
     */
    private static final WeakConcurrentMap<Class<?>, FieldReflect> FIELDS_CACHE = new WeakConcurrentMap<>();

    /**
     * 清除方法缓存
     */
    synchronized static void clearCache() {
        FIELDS_CACHE.clear();
    }

    /**
     * 是否为父类引用字段 当字段所在类是对象子类时（对象中定义的非static的class），会自动生成一个以"this$0"为名称的字段，指向父类对象
     *
     * @param field 字段
     * @return 是否为父类引用字段
     */
    public static boolean isOuterClassField(final Field field) {
        return "this$0".equals(field.getName());
    }

    /**
     * 查找指定类中是否包含指定名称对应的字段，包括所有字段（包括非public字段），也包括父类和Object类的字段
     *
     * @param beanClass 被查找字段的类,不能为null
     * @param name      字段名
     * @return 是否包含字段
     * @throws SecurityException 安全异常
     */
    public static boolean hasField(final Class<?> beanClass, final String name) throws SecurityException {
        return null != getField(beanClass, name);
    }

    /**
     * 获取字段名，如果存在{@link Alias}注解，读取注解的值作为名称
     *
     * @param field 字段
     * @return 字段名
     */
    public static String getFieldName(final Field field) {
        return getFieldName(field, true);
    }

    /**
     * 获取字段名，可选是否使用{@link Alias}注解，读取注解的值作为名称
     *
     * @param field    字段
     * @param useAlias 是否检查并使用{@link Alias}注解
     * @return 字段名
     */
    public static String getFieldName(final Field field, final boolean useAlias) {
        if (null == field) {
            return null;
        }

        if (useAlias) {
            final Alias alias = field.getAnnotation(Alias.class);
            if (null != alias) {
                return alias.value();
            }
        }

        return field.getName();
    }

    /**
     * 获取本类定义的指定名称的字段，包括私有字段，但是不包括父类字段
     *
     * @param beanClass Bean的Class
     * @param name      字段名称
     * @return 字段对象，如果未找到返回{@code null}
     */
    public static Field getDeclaredField(final Class<?> beanClass, final String name) {
        final Field[] fields = getDeclaredFields(beanClass, (field -> StringKit.equals(name, field.getName())));
        return ArrayKit.isEmpty(fields) ? null : fields[0];
    }

    /**
     * 查找指定类中的指定name的字段（包括非public字段），也包括父类和Object类的字段， 字段不存在则返回{@code null}
     *
     * @param beanClass 被查找字段的类,不能为null
     * @param name      字段名
     * @return 字段
     * @throws SecurityException 安全异常
     */
    public static Field getField(final Class<?> beanClass, final String name) throws SecurityException {
        final Field[] fields = getFields(beanClass, (field -> StringKit.equals(name, field.getName())));
        return ArrayKit.isEmpty(fields) ? null : fields[0];
    }

    /**
     * 获取指定类中字段名和字段对应的有序Map，包括其父类中的字段 如果子类与父类中存在同名字段，则父类字段忽略
     *
     * @param beanClass 类
     * @return 字段名和字段对应的Map，有序
     */
    public static Map<String, Field> getFieldMap(final Class<?> beanClass) {
        final Field[] fields = getFields(beanClass);
        final HashMap<String, Field> map = MapKit.newHashMap(fields.length, true);
        for (final Field field : fields) {
            map.putIfAbsent(field.getName(), field);
        }
        return map;
    }

    /**
     * 获得一个类中所有字段列表，包括其父类中的字段 如果子类与父类中存在同名字段，则这两个字段同时存在，子类字段在前，父类字段在后。
     *
     * @param beanClass 类
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static Field[] getFields(final Class<?> beanClass) throws SecurityException {
        return getFields(beanClass, null);
    }

    /**
     * 获得一个类中所有满足条件的字段列表，包括其父类中的字段 如果子类与父类中存在同名字段，则这两个字段同时存在，子类字段在前，父类字段在后。
     *
     * @param beanClass 类
     * @param filter    field过滤器，过滤掉不需要的field，{@link Predicate#test(Object)}为{@code true}保留，null表示全部保留
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static Field[] getFields(final Class<?> beanClass, final Predicate<Field> filter) throws SecurityException {
        Assert.notNull(beanClass);
        return FIELDS_CACHE.computeIfAbsent(beanClass, FieldReflect::of).getAllFields(filter);
    }

    /**
     * 获得当前类声明的所有字段（包括非public字段），但不包括父类的字段
     *
     * @param beanClass 类
     * @param filter    field过滤器，过滤掉不需要的field，{@link Predicate#test(Object)}为{@code true}保留，null表示全部保留
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static Field[] getDeclaredFields(final Class<?> beanClass, final Predicate<Field> filter)
            throws SecurityException {
        Assert.notNull(beanClass);
        return FIELDS_CACHE.computeIfAbsent(beanClass, FieldReflect::of).getDeclaredFields(filter);
    }

    /**
     * 获得一个类中所有字段列表，直接反射获取，无缓存 如果子类与父类中存在同名字段，则这两个字段同时存在，子类字段在前，父类字段在后。
     *
     * @param beanClass            类
     * @param withSuperClassFields 是否包括父类的字段列表
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static Field[] getFieldsDirectly(final Class<?> beanClass, final boolean withSuperClassFields)
            throws SecurityException {
        return FieldReflect.of(beanClass).getFieldsDirectly(withSuperClassFields);
    }

    /**
     * 获取字段值
     *
     * @param object    对象，如果static字段，此处为类
     * @param fieldName 字段名
     * @return 字段值
     * @throws InternalException 包装IllegalAccessException异常
     */
    public static Object getFieldValue(final Object object, final String fieldName) throws InternalException {
        if (null == object || StringKit.isBlank(fieldName)) {
            return null;
        }
        return getFieldValue(object,
                getField(object instanceof Class ? (Class<?>) object : object.getClass(), fieldName));
    }

    /**
     * 获取静态字段值
     *
     * @param field 字段
     * @return 字段值
     * @throws InternalException 包装IllegalAccessException异常
     */
    public static Object getStaticFieldValue(final Field field) throws InternalException {
        return getFieldValue(null, field);
    }

    /**
     * 获取字段值
     *
     * @param object 对象，static字段则此字段为null
     * @param field  字段
     * @return 字段值
     * @throws InternalException 包装IllegalAccessException异常
     */
    public static Object getFieldValue(Object object, final Field field) throws InternalException {
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
     * 获取所有字段的值
     *
     * @param object bean对象，如果是static字段，此处为类class
     * @return 字段值数组
     */
    public static Object[] getFieldsValue(final Object object) {
        return getFieldsValue(object, null);
    }

    /**
     * 获取所有字段的值
     *
     * @param object bean对象，如果是static字段，此处为类class
     * @param filter 字段过滤器，{@code null}返回原集合
     * @return 字段值数组
     */
    public static Object[] getFieldsValue(final Object object, final Predicate<Field> filter) {
        if (null != object) {
            final Field[] fields = getFields(object instanceof Class ? (Class<?>) object : object.getClass(), filter);
            if (null != fields) {
                return ArrayKit.mapToArray(fields, field -> getFieldValue(object, field), Object[]::new);
            }
        }
        return null;
    }

    /**
     * 获取所有字段及对应值
     *
     * @param object bean对象，如果是static字段，此处为类class
     * @return 字段值数组
     */
    public static Object getFieldsAndValue(final Object object) {
        if (null != object) {
            final Field[] fields = getFields(object instanceof Class ? (Class<?>) object : object.getClass());
            if (null != fields) {
                Map<String, Object> map = new HashMap<>();
                for (Field field : fields) {
                    Object obj = getFieldValue(object, field);
                    if (ObjectKit.isNotEmpty(obj) && !isSerialVersionUID(field)) {
                        map.put(field.getName(), getFieldValue(object, field));
                    }
                }
                return map;
            }
        }
        return null;
    }

    /**
     * 获取所有字段及对应值
     *
     * @param object bean对象，如果是static字段，此处为类class
     * @param filter 字段过滤器，{@code null}返回原集合
     * @return 字段值数组
     */
    public static Object getFieldsAndValue(final Object object, final Predicate<Field> filter) {
        if (null != object) {
            final Field[] fields = getFields(object instanceof Class ? (Class<?>) object : object.getClass(), filter);
            if (null != fields) {
                Map<String, Object> map = new HashMap<>();
                for (Field field : fields) {
                    Object obj = getFieldValue(object, field);
                    if (ObjectKit.isNotEmpty(obj) && !isSerialVersionUID(field)) {
                        map.put(field.getName(), getFieldValue(object, field));
                    }
                }
                return map;
            }
        }
        return null;
    }

    /**
     * 设置字段值
     *
     * @param object    对象,static字段则此处传Class
     * @param fieldName 字段名
     * @param value     值，值类型必须与字段类型匹配，不会自动转换对象类型
     * @throws InternalException 包装IllegalAccessException异常
     */
    public static void setFieldValue(final Object object, final String fieldName, final Object value)
            throws InternalException {
        Assert.notNull(object, "Object must be not null !");
        Assert.notBlank(fieldName);

        final Field field = getField((object instanceof Class) ? (Class<?>) object : object.getClass(), fieldName);
        Assert.notNull(field, "Field [{}] is not exist in [{}]", fieldName, object.getClass().getName());
        setFieldValue(object, field, value);
    }

    /**
     * 设置静态（static）字段值
     *
     * @param field 字段
     * @param value 值，值类型必须与字段类型匹配，不会自动转换对象类型
     * @throws InternalException 包装IllegalAccessException异常
     */
    public static void setStaticFieldValue(final Field field, final Object value) throws InternalException {
        setFieldValue(null, field, value);
    }

    /**
     * 设置字段值，如果值类型必须与字段类型匹配，会自动转换对象类型
     *
     * @param object 对象，如果是static字段，此参数为null
     * @param field  字段
     * @param value  值，类型不匹配会自动转换对象类型
     * @throws InternalException 包装IllegalAccessException异常
     */
    public static void setFieldValue(final Object object, final Field field, Object value) throws InternalException {
        setFieldValue(object, field, value, CompositeConverter.getInstance());
    }

    /**
     * 设置字段值，如果值类型必须与字段类型匹配，会自动转换对象类型
     *
     * @param object    对象，如果是static字段，此参数为null
     * @param field     字段
     * @param value     值，类型不匹配会自动转换对象类型
     * @param converter 转换器，用于转换给定value为字段类型，{@code null}表示不转换
     * @throws InternalException 包装IllegalAccessException异常
     */
    public static void setFieldValue(final Object object, final Field field, final Object value,
            final Converter converter) throws InternalException {
        Assert.notNull(field, "Field in [{}] not exist !", object);

        FieldInvoker.of(field).setConverter(converter).invokeSet(object, value);
    }

    /**
     * 给定类是否存在serialVersionUID字段
     *
     * @param field 字段
     * @return 是否存在serialVersionUID字段
     */
    public static boolean isSerialVersionUID(Field field) {
        return "serialVersionUID".equals(field.getName())
                && (Long.class.equals(field.getType()) || long.class.equals(field.getType()))
                && field.getModifiers() == (Modifier.PRIVATE + Modifier.STATIC + Modifier.FINAL);
    }

}
