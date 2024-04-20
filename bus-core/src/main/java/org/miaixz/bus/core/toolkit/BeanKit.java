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
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.beans.*;
import org.miaixz.bus.core.beans.copier.BeanCopier;
import org.miaixz.bus.core.beans.copier.CopyOptions;
import org.miaixz.bus.core.beans.copier.ValueProvider;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Editor;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.mutable.MutableEntry;
import org.miaixz.bus.core.map.CaseInsensitiveMap;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Bean工具类
 * 把一个拥有对属性进行set和get方法的类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanKit {

    /**
     * 判断是否为Bean对象
     * 判定方法是否存在只有一个参数的setXXX方法
     *
     * @param clazz 待测试类
     * @return 是否为Bean对象
     */
    public static boolean isBean(Class<?> clazz) {
        if (ClassKit.isNormalClass(clazz)) {
            final Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getParameterTypes().length == 1 && method.getName().startsWith(Normal.SET)) {
                    // 检测包含标准的setXXX方法即视为标准的JavaBean
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否为可读的Bean对象，判定方法是：
     *
     * <pre>
     *     1、是否存在只有无参数的getXXX方法或者isXXX方法
     *     2、是否存在public类型的字段
     * </pre>
     *
     * @param clazz 待测试类
     * @return 是否为可读的Bean对象
     * @see #hasGetter(Class)
     * @see #hasPublicField(Class)
     */
    public static boolean isReadable(Class<?> clazz) {
        return hasGetter(clazz) || hasPublicField(clazz);
    }

    /**
     * 判断Bean是否为空对象，空对象表示本身为<code>null</code>或者所有属性都为<code>null</code>
     *
     * @param bean             Bean对象
     * @param ignoreFieldNames 忽略检查的字段名
     * @return 是否为空，<code>true</code> - 空 / <code>false</code> - 非空
     */
    public static boolean isEmpty(Object bean, String... ignoreFieldNames) {
        if (null != bean) {
            for (Field field : ReflectKit.getFields(bean.getClass())) {
                if (isStatic(field)) {
                    continue;
                }
                if ((false == ArrayKit.contains(ignoreFieldNames, field.getName()))
                        && null != ReflectKit.getFieldValue(bean, field)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断Bean是否为非空对象，非空对象表示本身不为<code>null</code>或者含有非<code>null</code>属性的对象
     *
     * @param bean             Bean对象
     * @param ignoreFieldNames 忽略检查的字段名
     * @return 是否为空，<code>true</code> - 空 / <code>false</code> - 非空
     */
    public static boolean isNotEmpty(Object bean, String... ignoreFieldNames) {
        return false == isEmpty(bean, ignoreFieldNames);
    }

    /**
     * 判断Bean中是否有值为null的字段
     *
     * @param bean Bean
     * @return 是否有值为null的字段
     */
    public static boolean hasNull(Object bean) {
        final Field[] fields = ClassKit.getDeclaredFields(bean.getClass());

        Object fieldValue = null;
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                fieldValue = field.get(bean);
            } catch (Exception e) {

            }
            if (null == fieldValue) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否有Setter方法
     * 判定方法是否存在只有一个参数的setXXX方法
     *
     * @param clazz 待测试类
     * @return 是否为Bean对象
     */
    public static boolean hasSetter(Class<?> clazz) {
        if (ClassKit.isNormalClass(clazz)) {
            for (Method method : clazz.getMethods()) {
                if (method.getParameterCount() == 1 && method.getName().startsWith(Normal.SET)) {
                    // 检测包含标准的setXXX方法即视为标准的JavaBean
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否为Bean对象
     * 判定方法是否存在只有一个参数的getXXX方法
     *
     * @param clazz 待测试类
     * @return 是否为Bean对象
     */
    public static boolean hasGetter(Class<?> clazz) {
        if (ClassKit.isNormalClass(clazz)) {
            for (Method method : clazz.getMethods()) {
                if (method.getParameterCount() == 0) {
                    if (method.getName().startsWith(Normal.GET) || method.getName().startsWith(Normal.IS)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 指定类中是否有public类型字段(static字段除外)
     *
     * @param clazz 待测试类
     * @return 是否有public类型字段
     */
    public static boolean hasPublicField(Class<?> clazz) {
        if (ClassKit.isNormalClass(clazz)) {
            for (Field field : clazz.getFields()) {
                if (isPublic(field) && false == isStatic(field)) {
                    //非static的public字段
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断Bean是否包含值为<code>null</code>的属性
     * 对象本身为<code>null</code>也返回true
     *
     * @param bean Bean对象
     * @return 是否包含值为<code>null</code>的属性,<code>true</code> - 包含 / <code>false</code> - 不包含
     */
    public static boolean hasNullField(Object bean) {
        if (null == bean) {
            return true;
        }
        for (Field field : ReflectKit.getFields(bean.getClass())) {
            if (null == ReflectKit.getFieldValue(bean, field)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断Bean是否包含值为<code>null</code>的属性
     * 对象本身为<code>null</code>也返回true
     *
     * @param bean             Bean对象
     * @param ignoreFieldNames 忽略检查的字段名
     * @return 是否包含值为<code>null</code>的属性，<code>true</code> - 包含 / <code>false</code> - 不包含
     */
    public static boolean hasNullField(Object bean, String... ignoreFieldNames) {
        if (null == bean) {
            return true;
        }
        for (Field field : ReflectKit.getFields(bean.getClass())) {
            if (isStatic(field)) {
                continue;
            }
            if ((false == ArrayKit.contains(ignoreFieldNames, field.getName()))
                    && null == ReflectKit.getFieldValue(bean, field)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 把Bean里面的String属性做trim操作
     * <p>
     * 通常bean直接用来绑定页面的input,用户的输入可能首尾存在空格,通常保存数据库前需要把首尾空格去掉
     *
     * @param <T>         Bean类型
     * @param bean        Bean对象
     * @param ignoreField 不需要trim的Field名称列表(不区分大小写)
     * @return the object
     */
    public static <T> T trimStrField(T bean, String... ignoreField) {
        if (null == bean) {
            return bean;
        }

        final Field[] fields = ReflectKit.getFields(bean.getClass());
        for (Field field : fields) {
            if (null != ignoreField && ArrayKit.containsIgnoreCase(ignoreField, field.getName())) {
                // 不处理忽略的Fields
                continue;
            }
            if (String.class.equals(field.getType())) {
                // 只有String的Field才处理
                final String val = (String) ReflectKit.getFieldValue(bean, field);
                if (null != val) {
                    final String trimVal = StringKit.trim(val);
                    if (false == val.equals(trimVal)) {
                        // Field Value不为null,且首尾有空格才处理
                        ReflectKit.setFieldValue(bean, field, trimVal);
                    }
                }
            }
        }
        return bean;
    }

    /**
     * 创建动态Bean
     *
     * @param bean 普通Bean或Map
     * @return {@link DynamicBean}
     */
    public static DynamicBean create(Object bean) {
        return new DynamicBean(bean);
    }

    /**
     * 查找类型转换器 {@link PropertyEditor}
     *
     * @param type 需要转换的目标类型
     * @return {@link PropertyEditor}
     */
    public static PropertyEditor findEditor(Class<?> type) {
        return PropertyEditorManager.findEditor(type);
    }

    /**
     * 获取{@link BeanDesc} Bean描述信息
     *
     * @param clazz Bean类
     * @return {@link BeanDesc}
     */
    public static BeanDesc getBeanDesc(Class<?> clazz) {
        return BeanCache.INSTANCE.getBeanDesc(clazz, () -> new BeanDesc(clazz));
    }

    /**
     * 遍历Bean的属性
     *
     * @param clazz  Bean类
     * @param action 每个元素的处理类
     */
    public static void descForEach(Class<?> clazz, Consumer<? super PropertyDesc> action) {
        getBeanDesc(clazz).getProps().forEach(action);
    }

    /**
     * 获得Bean字段描述数组
     *
     * @param clazz Bean类
     * @return 字段描述数组
     * @throws InternalException 获取属性异常
     */
    public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) throws InternalException {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new InternalException(e);
        }
        return ArrayKit.filter(beanInfo.getPropertyDescriptors(), (Predicate<PropertyDescriptor>) t -> {
            // 过滤掉getClass方法
            return false == "class".equals(t.getName());
        });
    }

    /**
     * 获得字段名和字段描述Map,获得的结果会缓存在 {@link PropertyCache}中
     *
     * @param clazz      Bean类
     * @param ignoreCase 是否忽略大小写
     * @return 字段名和字段描述Map
     */
    public static Map<String, PropertyDescriptor> getPropertyDescriptorMap(Class<?> clazz, boolean ignoreCase) {
        return PropertyCache.INSTANCE.getPropertyDescriptorMap(clazz, ignoreCase, () -> internalGetPropertyDescriptorMap(clazz, ignoreCase));
    }

    /**
     * 获得字段名和字段描述Map 内部使用,直接获取Bean类的PropertyDescriptor
     *
     * @param clazz      Bean类
     * @param ignoreCase 是否忽略大小写
     * @return 字段名和字段描述Map
     */
    private static Map<String, PropertyDescriptor> internalGetPropertyDescriptorMap(Class<?> clazz, boolean ignoreCase) {
        final PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(clazz);
        final Map<String, PropertyDescriptor> map = ignoreCase ? new CaseInsensitiveMap<>(propertyDescriptors.length, 1)
                : new HashMap<>(propertyDescriptors.length, 1);

        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            map.put(propertyDescriptor.getName(), propertyDescriptor);
        }
        return map;
    }

    /**
     * 获得Bean类属性描述,大小写敏感
     *
     * @param clazz     Bean类
     * @param fieldName 字段名
     * @return PropertyDescriptor
     */
    public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, final String fieldName) {
        return getPropertyDescriptor(clazz, fieldName, false);
    }

    /**
     * 获得Bean类属性描述
     *
     * @param clazz      Bean类
     * @param fieldName  字段名
     * @param ignoreCase 是否忽略大小写
     * @return PropertyDescriptor
     */
    public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, final String fieldName, boolean ignoreCase) {
        final Map<String, PropertyDescriptor> map = getPropertyDescriptorMap(clazz, ignoreCase);
        return (null == map) ? null : map.get(fieldName);
    }

    /**
     * 获得字段值,通过反射直接获得字段值,并不调用getXXX方法
     * 对象同样支持Map类型,fieldNameOrIndex即为key
     *
     * <ul>
     *     <li>Map: fieldNameOrIndex需为key，获取对应value</li>
     *     <li>Collection: fieldNameOrIndex当为数字，返回index对应值，非数字遍历集合返回子bean对应name值</li>
     *     <li>Array: fieldNameOrIndex当为数字，返回index对应值，非数字遍历数组返回子bean对应name值</li>
     * </ul>
     *
     * @param bean             Bean对象
     * @param fieldNameOrIndex 字段名或序号,序号支持负数
     * @return 字段值
     */
    public static Object getFieldValue(Object bean, String fieldNameOrIndex) {
        if (null == bean || null == fieldNameOrIndex) {
            return null;
        }

        if (bean instanceof Map) {
            return ((Map<?, ?>) bean).get(fieldNameOrIndex);
        } else if (bean instanceof Collection) {
            try {
                return CollKit.get((Collection<?>) bean, Integer.parseInt(fieldNameOrIndex));
            } catch (NumberFormatException e) {
                return CollKit.map((Collection<?>) bean, (beanEle) -> getFieldValue(beanEle, fieldNameOrIndex), false);
            }
        } else if (ArrayKit.isArray(bean)) {
            try {
                return ArrayKit.get(bean, Integer.parseInt(fieldNameOrIndex));
            } catch (NumberFormatException e) {
                return ArrayKit.map(bean, Object.class, (beanEle) -> getFieldValue(beanEle, fieldNameOrIndex));
            }
        } else {
            return ReflectKit.getFieldValue(bean, fieldNameOrIndex);
        }
    }

    /**
     * 设置字段值,,通过反射设置字段值,并不调用setXXX方法
     * 对象同样支持Map类型,fieldNameOrIndex即为key
     *
     * @param bean             Bean
     * @param fieldNameOrIndex 字段名或序号,序号支持负数
     * @param value            值
     */
    public static void setFieldValue(Object bean, String fieldNameOrIndex, Object value) {
        if (bean instanceof Map) {
            ((Map) bean).put(fieldNameOrIndex, value);
        } else if (bean instanceof List) {
            CollKit.setOrAppend((List) bean, Convert.toInt(fieldNameOrIndex), value);
        } else if (ArrayKit.isArray(bean)) {
            ArrayKit.setOrAppend(bean, Convert.toInt(fieldNameOrIndex), value);
        } else {
            // 普通Bean对象
            ReflectKit.setFieldValue(bean, fieldNameOrIndex, value);
        }
    }

    /**
     * 解析Bean中的属性值
     *
     * @param <T>        对象信息
     * @param bean       Bean对象,支持Map、List、Collection、Array
     * @param expression 表达式,例如：person.friend[5].name
     * @return Bean属性值
     * @see BeanPath#get(Object)
     */
    public static <T> T getProperty(Object bean, String expression) {
        return (T) BeanPath.of(expression).get(bean);
    }

    /**
     * 解析Bean中的属性值
     *
     * @param bean       Bean对象,支持Map、List、Collection、Array
     * @param expression 表达式,例如：person.friend[5].name
     * @param value      值
     * @see BeanPath#get(Object)
     */
    public static void setProperty(Object bean, String expression, Object value) {
        BeanPath.of(expression).set(bean, value);
    }

    /**
     * 对象或Map转Bean
     *
     * @param <T>    Bean类型
     * @param source Bean对象或Map
     * @param clazz  目标的Bean类型
     * @return Bean对象
     */
    public static <T> T toBean(Object source, Class<T> clazz) {
        return toBean(source, clazz, null);
    }

    /**
     * 对象或Map转Bean
     *
     * @param <T>     转换的Bean类型
     * @param source  Bean对象或Map
     * @param clazz   目标的Bean类型
     * @param options 属性拷贝选项
     * @return Bean对象
     */
    public static <T> T toBean(Object source, Class<T> clazz, CopyOptions options) {
        return toBean(source, () -> ReflectKit.newInstanceIfPossible(clazz), options);
    }

    /**
     * 对象或Map转Bean
     *
     * @param <T>            转换的Bean类型
     * @param source         Bean对象或Map
     * @param targetSupplier 目标的Bean创建器
     * @param options        属性拷贝选项
     * @return Bean对象
     */
    public static <T> T toBean(Object source, Supplier<T> targetSupplier, CopyOptions options) {
        if (null == source || null == targetSupplier) {
            return null;
        }
        final T target = targetSupplier.get();
        copyProperties(source, target, options);
        return target;
    }

    /**
     * ServletRequest 参数转Bean
     *
     * @param <T>           Bean类型
     * @param beanClass     Bean Class
     * @param valueProvider 值提供者
     * @param copyOptions   拷贝选项,见 {@link CopyOptions}
     * @return Bean
     */
    public static <T> T toBean(Class<T> beanClass, ValueProvider<String> valueProvider, CopyOptions copyOptions) {
        if (null == beanClass || null == valueProvider) {
            return null;
        }
        return fillBean(ReflectKit.newInstance(beanClass), valueProvider, copyOptions);
    }

    /**
     * 对象或Map转Bean，忽略字段转换时发生的异常
     *
     * @param <T>    转换的Bean类型
     * @param source Bean对象或Map
     * @param clazz  目标的Bean类型
     * @return Bean对象
     */
    public static <T> T toBeanIgnoreError(Object source, Class<T> clazz) {
        return toBean(source, clazz, CopyOptions.of().setIgnoreError(true));
    }

    /**
     * 对象或Map转Bean，忽略字段转换时发生的异常
     *
     * @param <T>         转换的Bean类型
     * @param source      Bean对象或Map
     * @param clazz       目标的Bean类型
     * @param ignoreError 是否忽略注入错误
     * @return Bean对象
     */
    public static <T> T toBeanIgnoreCase(Object source, Class<T> clazz, boolean ignoreError) {
        return toBean(source, clazz,
                CopyOptions.of()
                        .setIgnoreCase(true)
                        .setIgnoreError(ignoreError));
    }

    /**
     * 填充Bean的核心方法
     *
     * @param <T>           Bean类型
     * @param bean          Bean
     * @param valueProvider 值提供者
     * @param copyOptions   拷贝选项,见 {@link CopyOptions}
     * @return Bean
     */
    public static <T> T fillBean(T bean, ValueProvider<String> valueProvider, CopyOptions copyOptions) {
        if (null == valueProvider) {
            return bean;
        }

        return BeanCopier.of(valueProvider, bean, copyOptions).copy();
    }

    /**
     * 使用Map填充Bean对象
     *
     * @param <T>           Bean类型
     * @param map           Map
     * @param bean          Bean
     * @param isIgnoreError 是否忽略注入错误
     * @return Bean
     */
    public static <T> T fillBeanWithMap(Map<?, ?> map, T bean, boolean isIgnoreError) {
        return fillBeanWithMap(map, bean, false, isIgnoreError);
    }

    /**
     * 使用Map填充Bean对象,可配置将下划线转换为驼峰
     *
     * @param <T>           Bean类型
     * @param map           Map
     * @param bean          Bean
     * @param isToCamelCase 是否将下划线模式转换为驼峰模式
     * @param isIgnoreError 是否忽略注入错误
     * @return Bean
     */
    public static <T> T fillBeanWithMap(Map<?, ?> map, T bean, boolean isToCamelCase, boolean isIgnoreError) {
        return fillBeanWithMap(map, bean, isToCamelCase, CopyOptions.of().setIgnoreError(isIgnoreError));
    }

    /**
     * 使用Map填充Bean对象,忽略大小写
     *
     * @param <T>           Bean类型
     * @param map           Map
     * @param bean          Bean
     * @param isIgnoreError 是否忽略注入错误
     * @return Bean
     */
    public static <T> T fillBeanWithMapIgnoreCase(Map<?, ?> map, T bean, boolean isIgnoreError) {
        return fillBeanWithMap(map, bean, CopyOptions.of().setIgnoreCase(true).setIgnoreError(isIgnoreError));
    }

    /**
     * 使用Map填充Bean对象
     *
     * @param <T>         Bean类型
     * @param map         Map
     * @param bean        Bean
     * @param copyOptions 属性复制选项 {@link CopyOptions}
     * @return Bean
     */
    public static <T> T fillBeanWithMap(Map<?, ?> map, T bean, CopyOptions copyOptions) {
        return fillBeanWithMap(map, bean, false, copyOptions);
    }

    /**
     * 使用Map填充Bean对象
     *
     * @param <T>           Bean类型
     * @param map           Map
     * @param bean          Bean
     * @param isToCamelCase 是否将Map中的下划线风格key转换为驼峰风格
     * @param copyOptions   属性复制选项 {@link CopyOptions}
     * @return Bean
     */
    public static <T> T fillBeanWithMap(Map<?, ?> map, T bean, boolean isToCamelCase, CopyOptions copyOptions) {
        if (MapKit.isEmpty(map)) {
            return bean;
        }
        if (isToCamelCase) {
            map = MapKit.toCamelCaseMap(map);
        }
        return BeanCopier.of(map, bean, copyOptions).copy();
    }

    /**
     * 对象转Map,不进行驼峰转下划线,不忽略值为空的字段
     *
     * @param bean bean对象
     * @return Map
     */
    public static Map<String, Object> beanToMap(Object bean) {
        return beanToMap(bean, false, false);
    }

    /**
     * 将bean的部分属性转换成map
     * 可选拷贝哪些属性值，默认是不忽略值为{@code null}的值的。
     *
     * @param bean       bean
     * @param properties 需要拷贝的属性值，{@code null}或空表示拷贝所有值
     * @return Map
     */
    public static Map<String, Object> beanToMap(final Object bean, final String... properties) {
        int mapSize = 16;
        UnaryOperator<MutableEntry<String, Object>> editor = null;
        if (ArrayKit.isNotEmpty(properties)) {
            mapSize = properties.length;
            final Set<String> propertiesSet = CollKit.newHashSet(false, properties);
            editor = entry -> {
                final String key = entry.getKey();
                entry.setKey(propertiesSet.contains(key) ? key : null);
                return entry;
            };
        }

        // 指明了要复制的属性 所以不忽略null值
        return beanToMap(bean, new LinkedHashMap<>(mapSize, 1), false, editor);
    }

    /**
     * 对象转Map
     *
     * @param bean              bean对象
     * @param isToUnderlineCase 是否转换为下划线模式
     * @param ignoreNullValue   是否忽略值为空的字段
     * @return Map
     */
    public static Map<String, Object> beanToMap(final Object bean, final boolean isToUnderlineCase, final boolean ignoreNullValue) {
        if (null == bean) {
            return null;
        }
        return beanToMap(bean, new LinkedHashMap<>(), isToUnderlineCase, ignoreNullValue);
    }

    /**
     * 对象转Map
     *
     * @param bean              bean对象
     * @param targetMap         目标的Map
     * @param isToUnderlineCase 是否转换为下划线模式
     * @param ignoreNullValue   是否忽略值为空的字段
     * @return Map
     */
    public static Map<String, Object> beanToMap(final Object bean, final Map<String, Object> targetMap, final boolean isToUnderlineCase, final boolean ignoreNullValue) {
        if (null == bean) {
            return null;
        }

        return beanToMap(bean, targetMap, ignoreNullValue, entry -> {
            final String key = entry.getKey();
            entry.setKey(isToUnderlineCase ? StringKit.toUnderlineCase(key) : key);
            return entry;
        });
    }

    /**
     * 对象转Map
     * 通过实现{@link UnaryOperator} 可以自定义字段值,如果这个Editor返回null则忽略这个字段,以便实现：
     *
     * <pre>
     * 1. 字段筛选,可以去除不需要的字段
     * 2. 字段变换,例如实现驼峰转下划线
     * 3. 自定义字段前缀或后缀等等
     * </pre>
     *
     * @param bean            bean对象
     * @param targetMap       目标的Map
     * @param ignoreNullValue 是否忽略值为空的字段
     * @param keyEditor       属性字段(Map的key)编辑器,用于筛选、编辑key,如果这个Editor返回null则忽略这个字段
     * @return Map
     */
    public static Map<String, Object> beanToMap(final Object bean, final Map<String, Object> targetMap,
                                                final boolean ignoreNullValue, final UnaryOperator<MutableEntry<String, Object>> keyEditor) {
        if (null == bean) {
            return null;
        }

        return BeanCopier.of(bean, targetMap,
                CopyOptions.of()
                        .setIgnoreNullValue(ignoreNullValue)
                        .setFieldEditor(keyEditor)
        ).copy();
    }

    /**
     * 对象转Map
     * 通过自定义{@link CopyOptions} 完成抓换选项，以便实现：
     *
     * <pre>
     * 1. 字段筛选，可以去除不需要的字段
     * 2. 字段变换，例如实现驼峰转下划线
     * 3. 自定义字段前缀或后缀等等
     * 4. 字段值处理
     * ...
     * </pre>
     *
     * @param bean        bean对象
     * @param targetMap   目标的Map
     * @param copyOptions 拷贝选项
     * @return the Map
     */
    public static Map<String, Object> beanToMap(final Object bean, final Map<String, Object> targetMap, final CopyOptions copyOptions) {
        if (null == bean) {
            return null;
        }

        return BeanCopier.of(bean, targetMap, copyOptions).copy();
    }

    /**
     * 按照Bean对象属性创建对应的Class对象，并忽略某些属性
     *
     * @param <T>              对象类型
     * @param source           源Bean对象
     * @param clazz            目标Class
     * @param ignoreProperties 不拷贝的的属性列表
     * @return 目标对象
     */
    public static <T> T copyProperties(Object source, Class<T> clazz, String... ignoreProperties) {
        if (null == source) {
            return null;
        }
        T target = ReflectKit.newInstanceIfPossible(clazz);
        copyProperties(source, target, CopyOptions.of().setIgnoreProperties(ignoreProperties));
        return target;
    }

    /**
     * 复制Bean对象属性
     * 限制类用于限制拷贝的属性,例如一个类我只想复制其父类的一些属性,就可以将editable设置为父类
     *
     * @param source           源Bean对象
     * @param target           目标Bean对象
     * @param ignoreProperties 不拷贝的的属性列表
     */
    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
        copyProperties(source, target, CopyOptions.of().setIgnoreProperties(ignoreProperties));
    }

    /**
     * 复制Bean对象属性
     *
     * @param source     源Bean对象
     * @param target     目标Bean对象
     * @param ignoreCase 是否忽略大小写
     */
    public static void copyProperties(Object source, Object target, boolean ignoreCase) {
        BeanCopier.of(source, target, CopyOptions.of().setIgnoreCase(ignoreCase)).copy();
    }

    /**
     * 复制Bean对象属性
     * 限制类用于限制拷贝的属性,例如一个类我只想复制其父类的一些属性,就可以将editable设置为父类
     *
     * @param source      源Bean对象
     * @param target      目标Bean对象
     * @param copyOptions 拷贝选项,见 {@link CopyOptions}
     */
    public static void copyProperties(Object source, Object target, CopyOptions copyOptions) {
        BeanCopier.of(source, target, ObjectKit.defaultIfNull(copyOptions, CopyOptions::of)).copy();
    }

    /**
     * 复制集合中的Bean属性
     * 此方法遍历集合中每个Bean，复制其属性后加入一个新的{@link List}中
     *
     * @param collection  原Bean集合
     * @param targetType  目标Bean类型
     * @param copyOptions 拷贝选项
     * @param <T>         Bean类型
     * @return the list
     */
    public static <T> List<T> copyToList(Collection<?> collection, Class<T> targetType, CopyOptions copyOptions) {
        if (null == collection) {
            return null;
        }
        if (collection.isEmpty()) {
            return new ArrayList<>(0);
        }
        return collection.stream().map((source) -> {
            final T target = ReflectKit.newInstanceIfPossible(targetType);
            copyProperties(source, target, copyOptions);
            return target;
        }).collect(Collectors.toList());
    }

    /**
     * 复制集合中的Bean属性
     * 此方法遍历集合中每个Bean，复制其属性后加入一个新的{@link List}中
     *
     * @param collection 原Bean集合
     * @param targetType 目标Bean类型
     * @param <T>        Bean类型
     * @return 复制后的List
     */
    public static <T> List<T> copyToList(Collection<?> collection, Class<T> targetType) {
        return copyToList(collection, targetType, CopyOptions.of());
    }

    /**
     * 给定的Bean的类名是否匹配指定类名字符串
     * 如果isSimple为{@code true},则只匹配类名而忽略包名
     * 如果isSimple为{@code false},则匹配包括包名的全类名
     *
     * @param bean          Bean
     * @param beanClassName Bean的类名
     * @param isSimple      是否只匹配类名而忽略包名,true表示忽略包名
     * @return 是否匹配
     */
    public static boolean isMatchName(Object bean, String beanClassName, boolean isSimple) {
        return ClassKit.getClassName(bean, isSimple).equals(isSimple ? StringKit.upperFirst(beanClassName) : beanClassName);
    }

    /**
     * 把Bean里面的String属性做trim操作。此方法直接对传入的Bean做修改。
     * 通常bean直接用来绑定页面的input，用户的输入可能首尾存在空格，通常保存数据库前需要把首尾空格去掉
     *
     * @param <T>          Bean类型
     * @param bean         Bean对象
     * @param ignoreFields 不需要trim的Field名称列表(不区分大小写)
     * @return 处理后的Bean对象
     */
    public static <T> T trimStrFields(T bean, String... ignoreFields) {
        return edit(bean, (field) -> {
            if (ignoreFields != null && ArrayKit.containsIgnoreCase(ignoreFields, field.getName())) {
                // 不处理忽略的Fields
                return field;
            }
            if (String.class.equals(field.getType())) {
                // 只有String的Field才处理
                final String val = (String) ReflectKit.getFieldValue(bean, field);
                if (null != val) {
                    final String trimVal = StringKit.trim(val);
                    if (false == val.equals(trimVal)) {
                        // Field Value不为null，且首尾有空格才处理
                        ReflectKit.setFieldValue(bean, field, trimVal);
                    }
                }
            }
            return field;
        });
    }

    /**
     * 转义bean中所有属性为字符串的
     *
     * @param bean {@link Object}
     */
    public static void trimAllFields(Object bean) {
        try {
            if (null != bean) {
                // 获取所有的字段包括public,private,protected,private
                Field[] fields = bean.getClass().getDeclaredFields();
                for (Field f : fields) {
                    if ("java.lang.String".equals(f.getType().getName())) {
                        // 获取字段名
                        String key = f.getName();
                        Object value = getFieldValue(bean, key);

                        if (null == value) {
                            continue;
                        }
                        setFieldValue(bean, key, EscapeKit.escapeXml11(value.toString()));
                    }
                }
            }
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * bean 中所有属性为字符串的进行\n\t\s处理
     *
     * @param bean {@link Object}
     */
    public static void replaceStrFields(Object bean) {
        try {
            if (null != bean) {
                // 获取所有的字段包括public,private,protected,private
                Field[] fields = bean.getClass().getDeclaredFields();
                for (Field f : fields) {
                    if ("java.lang.String".equals(f.getType().getName())) {
                        // 获取字段名
                        String key = f.getName();
                        Object value = getFieldValue(bean, key);
                        if (null == value) {
                            continue;
                        }
                        setFieldValue(bean, key, StringKit.replace(value.toString()));
                    }
                }
            }
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * 是否同时存在一个或多个修饰符(可能有多个修饰符,如果有指定的修饰符则返回true)
     *
     * @param clazz         类
     * @param modifierTypes 修饰符枚举
     * @return 是否有指定修饰符, 如果有返回true, 否则false, 如果提供参数为null返回false
     */
    public static boolean hasModifier(Class<?> clazz, ModifierType... modifierTypes) {
        if (null == clazz || ArrayKit.isEmpty(modifierTypes)) {
            return false;
        }
        return 0 != (clazz.getModifiers() & modifiersToInt(modifierTypes));
    }

    /**
     * 是否同时存在一个或多个修饰符(可能有多个修饰符,如果有指定的修饰符则返回true)
     *
     * @param constructor   构造方法
     * @param modifierTypes 修饰符枚举
     * @return 是否有指定修饰符, 如果有返回true, 否则false, 如果提供参数为null返回false
     */
    public static boolean hasModifier(Constructor<?> constructor, ModifierType... modifierTypes) {
        if (null == constructor || ArrayKit.isEmpty(modifierTypes)) {
            return false;
        }
        return 0 != (constructor.getModifiers() & modifiersToInt(modifierTypes));
    }

    /**
     * 是否同时存在一个或多个修饰符(可能有多个修饰符,如果有指定的修饰符则返回true)
     *
     * @param method        方法
     * @param modifierTypes 修饰符枚举
     * @return 是否有指定修饰符, 如果有返回true, 否则false, 如果提供参数为null返回false
     */
    public static boolean hasModifier(Method method, ModifierType... modifierTypes) {
        if (null == method || ArrayKit.isEmpty(modifierTypes)) {
            return false;
        }
        return 0 != (method.getModifiers() & modifiersToInt(modifierTypes));
    }

    /**
     * 是否同时存在一个或多个修饰符(可能有多个修饰符,如果有指定的修饰符则返回true)
     *
     * @param field         字段
     * @param modifierTypes 修饰符枚举
     * @return 是否有指定修饰符, 如果有返回true, 否则false, 如果提供参数为null返回false
     */
    public static boolean hasModifier(Field field, ModifierType... modifierTypes) {
        if (null == field || ArrayKit.isEmpty(modifierTypes)) {
            return false;
        }
        return 0 != (field.getModifiers() & modifiersToInt(modifierTypes));
    }

    /**
     * 是否同时存在一个或多个修饰符（可能有多个修饰符，如果有指定的修饰符则返回true）
     *
     * @param member        构造、字段或方法
     * @param modifierTypes 修饰符枚举
     * @return 是否有指定修饰符，如果有返回true，否则false，如果提供参数为null返回false
     */
    public static boolean hasModifier(Member member, final ModifierType... modifierTypes) {
        if (null == member || ArrayKit.isEmpty(modifierTypes)) {
            return false;
        }
        return 0 != (member.getModifiers() & modifiersToInt(modifierTypes));
    }

    /**
     * 是否是Public成员，可检测包括构造、字段和方法
     *
     * @param member 构造、字段或方法
     * @return 是否是Public
     */
    public static boolean isPublic(final Member member) {
        return hasModifier(member, ModifierType.PUBLIC);
    }

    /**
     * 是否是Public类
     *
     * @param clazz 类
     * @return 是否是Public
     */
    public static boolean isPublic(final Class<?> clazz) {
        return hasModifier(clazz, ModifierType.PUBLIC);
    }

    /**
     * 是否是Protected成员，可检测包括构造、字段和方法
     *
     * @param member 构造、字段或方法
     * @return 是否是Protected
     */
    public static boolean isProtected(final Member member) {
        return hasModifier(member, ModifierType.PROTECTED);
    }

    /**
     * 是否是static成员，包括构造、字段或方法
     *
     * @param member 构造、字段或方法
     * @return 是否是static
     */
    public static boolean isStatic(final Member member) {
        return hasModifier(member, ModifierType.STATIC);
    }

    /**
     * 是否是static类
     *
     * @param clazz 类
     * @return 是否是static
     */
    public static boolean isStatic(final Class<?> clazz) {
        return hasModifier(clazz, ModifierType.STATIC);
    }

    /**
     * 是否是合成成员（由java编译器生成的）
     *
     * @param member 构造、字段或方法
     * @return 是否是合成字段
     */
    public static boolean isSynthetic(final Member member) {
        return member.isSynthetic();
    }

    /**
     * 是否是合成类（由java编译器生成的）
     *
     * @param clazz 类
     * @return 是否是合成
     */
    public static boolean isSynthetic(final Class<?> clazz) {
        return clazz.isSynthetic();
    }

    /**
     * 是否抽象成员
     *
     * @param member 构造、字段或方法
     * @return 是否抽象方法
     */
    public static boolean isAbstract(final Member member) {
        return hasModifier(member, ModifierType.ABSTRACT);
    }

    /**
     * 是否抽象类
     *
     * @param clazz 构造、字段或方法
     * @return 是否抽象类
     */
    public static boolean isAbstract(final Class<?> clazz) {
        return hasModifier(clazz, ModifierType.ABSTRACT);
    }

    /**
     * 是否抽象类
     *
     * @param clazz 构造、字段或方法
     * @return 是否抽象类
     */
    public static boolean isInterface(final Class<?> clazz) {
        return null != clazz && clazz.isInterface();
    }

    /**
     * 多个修饰符做“与”操作,表示同时存在多个修饰符
     *
     * @param modifierTypes 修饰符列表,元素不能为空
     * @return “与”之后的修饰符
     */
    private static int modifiersToInt(ModifierType... modifierTypes) {
        int modifier = modifierTypes[0].getValue();
        for (int i = 1; i < modifierTypes.length; i++) {
            modifier |= modifierTypes[i].getValue();
        }
        return modifier;
    }

    /**
     * 遍历Bean的属性
     *
     * @param clazz  Bean类
     * @param action 每个元素的处理类
     */
    public static void forEach(Class<?> clazz, Consumer<? super PropertyDesc> action) {
        getBeanDesc(clazz).getProps().forEach(action);
    }

    /**
     * 编辑Bean的字段，static字段不会处理
     * 例如需要对指定的字段做判空操作、null转""操作等等。
     *
     * @param bean   bean
     * @param editor 编辑器函数
     * @param <T>    被编辑的Bean类型
     * @return the object
     */
    public static <T> T edit(T bean, Editor<Field> editor) {
        if (bean == null) {
            return null;
        }

        final Field[] fields = ReflectKit.getFields(bean.getClass());
        for (Field field : fields) {
            if (isStatic(field)) {
                continue;
            }
            editor.edit(field);
        }
        return bean;
    }

    /**
     * 获取Getter或Setter方法名对应的字段名称，规则如下：
     * <ul>
     *     <li>getXxxx获取为xxxx，如getName得到name</li>
     *     <li>setXxxx获取为xxxx，如setName得到name</li>
     *     <li>isXxxx获取为xxxx，如isName得到name</li>
     *     <li>其它不满足规则的方法名抛出{@link IllegalArgumentException}</li>
     * </ul>
     *
     * @param getterOrSetterName Getter或Setter方法名
     * @return 字段名称
     * @throws IllegalArgumentException 非Getter或Setter方法
     */
    public static String getFieldName(String getterOrSetterName) {
        if (getterOrSetterName.startsWith("get") || getterOrSetterName.startsWith("set")) {
            return StringKit.removePreAndLowerFirst(getterOrSetterName, 3);
        } else if (getterOrSetterName.startsWith("is")) {
            return StringKit.removePreAndLowerFirst(getterOrSetterName, 2);
        } else {
            throw new IllegalArgumentException("Invalid Getter or Setter name: " + getterOrSetterName);
        }
    }

    /**
     * 判断source与target的所有公共字段的值是否相同
     *
     * @param source           待检测对象1
     * @param target           待检测对象2
     * @param ignoreProperties 不需要检测的字段
     * @return 判断结果，如果为true则证明所有字段的值都相同
     */
    public static boolean isCommonFieldsEqual(Object source, Object target, String... ignoreProperties) {
        if (null == source && null == target) {
            return true;
        }
        if (null == source || null == target) {
            return false;
        }

        Map<String, Object> sourceFieldsMap = beanToMap(source);
        Map<String, Object> targetFieldsMap = beanToMap(target);

        Set<String> sourceFields = sourceFieldsMap.keySet();
        sourceFields.removeAll(Arrays.asList(ignoreProperties));

        for (String field : sourceFields) {
            if (ObjectKit.notEquals(sourceFieldsMap.get(field), targetFieldsMap.get(field))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 修饰符枚举
     */
    public enum ModifierType {
        /**
         * public修饰符,所有类都能访问
         */
        PUBLIC(Modifier.PUBLIC),
        /**
         * private修饰符,只能被自己访问和修改
         */
        PRIVATE(Modifier.PRIVATE),
        /**
         * protected修饰符,自身、子类及同一个包中类可以访问
         */
        PROTECTED(Modifier.PROTECTED),
        /**
         * static修饰符,(静态修饰符)指定变量被所有对象共享,即所有实例都可以使用该变量 变量属于这个类
         */
        STATIC(Modifier.STATIC),
        /**
         * final修饰符,最终修饰符,指定此变量的值不能变,使用在方法上表示不能被重载
         */
        FINAL(Modifier.FINAL),
        /**
         * synchronized,同步修饰符,在多个线程中,该修饰符用于在运行前,对他所属的方法加锁,以防止其他线程的访问,运行结束后解锁
         */
        SYNCHRONIZED(Modifier.SYNCHRONIZED),
        /**
         * (易失修饰符)指定该变量可以同时被几个线程控制和修改
         */
        VOLATILE(Modifier.VOLATILE),
        /**
         * (过度修饰符)指定该变量是系统保留,暂无特别作用的临时性变量,序列化时忽略
         */
        TRANSIENT(Modifier.TRANSIENT),
        /**
         * native,本地修饰符 指定此方法的方法体是用其他语言在程序外部编写的
         */
        NATIVE(Modifier.NATIVE),

        /**
         * abstract,将一个类声明为抽象类,没有实现的方法,需要子类提供方法实现
         */
        ABSTRACT(Modifier.ABSTRACT),
        /**
         * strictfp,一旦使用了关键字strictfp来声明某个类、接口或者方法时,那么在这个关键字所声明的范围内所有浮点运算都是精确的,符合IEEE-754规范的
         */
        STRICT(Modifier.STRICT);

        /**
         * 修饰符枚举对应的int修饰符值
         */
        private final int value;

        /**
         * 构造
         *
         * @param modifier 修饰符int表示,见{@link Modifier}
         */
        ModifierType(int modifier) {
            this.value = modifier;
        }

        /**
         * 获取修饰符枚举对应的int修饰符值,值见{@link Modifier}
         *
         * @return 修饰符枚举对应的int修饰符值
         */
        public int getValue() {
            return this.value;
        }
    }

}
