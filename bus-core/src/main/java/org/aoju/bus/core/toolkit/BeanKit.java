/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.toolkit;

import org.aoju.bus.core.beans.*;
import org.aoju.bus.core.beans.copier.BeanCopier;
import org.aoju.bus.core.beans.copier.CopyOptions;
import org.aoju.bus.core.beans.copier.ValueProvider;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.lang.Editor;
import org.aoju.bus.core.lang.Filter;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.map.CaseInsensitiveMap;

import java.beans.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Bean工具类
 * 把一个拥有对属性进行set和get方法的类
 *
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8+
 */
public class BeanKit {

    /**
     * 判断是否为Bean对象
     * 判定方法是是否存在只有一个参数的setXXX方法
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
     * @param ignoreFiledNames 忽略检查的字段名
     * @return 是否为空，<code>true</code> - 空 / <code>false</code> - 非空
     */
    public static boolean isEmpty(Object bean, String... ignoreFiledNames) {
        if (null != bean) {
            for (Field field : ReflectKit.getFields(bean.getClass())) {
                if (isStatic(field)) {
                    continue;
                }
                if ((false == ArrayKit.contains(ignoreFiledNames, field.getName()))
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
     * @param ignoreFiledNames 忽略检查的字段名
     * @return 是否为空，<code>true</code> - 空 / <code>false</code> - 非空
     */
    public static boolean isNotEmpty(Object bean, String... ignoreFiledNames) {
        return false == isEmpty(bean, ignoreFiledNames);
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
     * 判定方法是是否存在只有一个参数的setXXX方法
     *
     * @param clazz 待测试类
     * @return 是否为Bean对象
     */
    public static boolean hasSetter(Class<?> clazz) {
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
     * 判断是否为Bean对象
     * 判定方法是是否存在只有一个参数的setXXX方法
     *
     * @param clazz 待测试类
     * @return 是否为Bean对象
     */
    public static boolean hasGetter(Class<?> clazz) {
        if (ClassKit.isNormalClass(clazz)) {
            for (Method method : clazz.getMethods()) {
                if (method.getParameterTypes().length == 0) {
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
                if (BeanKit.isPublic(field) && false == BeanKit.isStatic(field)) {
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
     * @param bean             Bean对象
     * @param ignoreFiledNames 忽略检查的字段名
     * @return 是否包含值为<code>null</code>的属性，<code>true</code> - 包含 / <code>false</code> - 不包含
     */
    public static boolean hasNullField(Object bean, String... ignoreFiledNames) {
        if (null == bean) {
            return true;
        }
        for (Field field : ReflectKit.getFields(bean.getClass())) {
            if (isStatic(field)) {
                continue;
            }
            if ((false == ArrayKit.contains(ignoreFiledNames, field.getName()))
                    && null == ReflectKit.getFieldValue(bean, field)) {
                return true;
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
        if (bean == null) {
            return bean;
        }

        final Field[] fields = ReflectKit.getFields(bean.getClass());
        for (Field field : fields) {
            if (ignoreField != null && ArrayKit.containsIgnoreCase(ignoreField, field.getName())) {
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
     * @return {@link DynaBean}
     */
    public static DynaBean create(Object bean) {
        return new DynaBean(bean);
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
        BeanDesc beanDesc = BeanDescCache.INSTANCE.getBeanDesc(clazz);
        if (null == beanDesc) {
            beanDesc = new BeanDesc(clazz);
            BeanDescCache.INSTANCE.putBeanDesc(clazz, beanDesc);
        }
        return beanDesc;
    }

    /**
     * 获得Bean字段描述数组
     *
     * @param clazz Bean类
     * @return 字段描述数组
     * @throws InstrumentException 获取属性异常
     */
    public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) throws InstrumentException {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new InstrumentException(e);
        }
        return ArrayKit.filter(beanInfo.getPropertyDescriptors(), (Filter<PropertyDescriptor>) t -> {
            // 过滤掉getClass方法
            return false == "class".equals(t.getName());
        });
    }

    /**
     * 获得字段名和字段描述Map,获得的结果会缓存在 {@link BeanInfoCache}中
     *
     * @param clazz      Bean类
     * @param ignoreCase 是否忽略大小写
     * @return 字段名和字段描述Map
     */
    public static Map<String, PropertyDescriptor> getPropertyDescriptorMap(Class<?> clazz, boolean ignoreCase) {
        Map<String, PropertyDescriptor> map = BeanInfoCache.INSTANCE.getPropertyDescriptorMap(clazz, ignoreCase);
        if (null == map) {
            map = internalGetPropertyDescriptorMap(clazz, ignoreCase);
            BeanInfoCache.INSTANCE.putPropertyDescriptorMap(clazz, map, ignoreCase);
        }
        return map;
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
            return CollKit.get((Collection<?>) bean, Integer.parseInt(fieldNameOrIndex));
        } else if (ArrayKit.isArray(bean)) {
            return ArrayKit.get(bean, Integer.parseInt(fieldNameOrIndex));
        } else {// 普通Bean对象
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
     * @param bean       Bean对象,支持Map、List、Collection、Array
     * @param expression 表达式,例如：person.friend[5].name
     * @return Bean属性值
     * @see BeanPath#get(Object)
     */
    public static Object getProperty(Object bean, String expression) {
        return BeanPath.create(expression).get(bean);
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
        BeanPath.create(expression).set(bean, value);
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
        final T target = ReflectKit.newInstanceIfPossible(clazz);
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
        return fillBean(ReflectKit.newInstance(beanClass), valueProvider, copyOptions);
    }

    /**
     * Map转换为Bean对象
     *
     * @param <T>           Bean类型
     * @param map           {@link Map}
     * @param beanClass     Bean Class
     * @param isIgnoreError 是否忽略注入错误
     * @return Bean
     */
    public static <T> T mapToBean(Map<?, ?> map, Class<T> beanClass, boolean isIgnoreError) {
        return fillBeanWithMap(map, ReflectKit.newInstance(beanClass), isIgnoreError);
    }

    /**
     * Map转换为Bean对象
     *
     * @param <T>         Bean类型
     * @param map         {@link Map}
     * @param beanClass   Bean Class
     * @param copyOptions 转Bean选项
     * @return Bean
     */
    public static <T> T mapToBean(Map<?, ?> map, Class<T> beanClass, CopyOptions copyOptions) {
        return fillBeanWithMap(map, ReflectKit.newInstance(beanClass), copyOptions);
    }

    /**
     * Map转换为Bean对象
     * 忽略大小写
     *
     * @param <T>           Bean类型
     * @param map           Map
     * @param beanClass     Bean Class
     * @param isIgnoreError 是否忽略注入错误
     * @return Bean
     */
    public static <T> T mapToBeanIgnoreCase(Map<?, ?> map, Class<T> beanClass, boolean isIgnoreError) {
        return fillBeanWithMapIgnoreCase(map, ReflectKit.newInstance(beanClass), isIgnoreError);
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

        return BeanCopier.create(valueProvider, bean, copyOptions).copy();
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
        return fillBeanWithMap(map, bean, isToCamelCase, CopyOptions.create().setIgnoreError(isIgnoreError));
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
        return fillBeanWithMap(map, bean, CopyOptions.create().setIgnoreCase(true).setIgnoreError(isIgnoreError));
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
        return BeanCopier.create(map, bean, copyOptions).copy();
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
     * 对象转Map
     *
     * @param bean              bean对象
     * @param isToUnderlineCase 是否转换为下划线模式
     * @param ignoreNullValue   是否忽略值为空的字段
     * @return Map
     */
    public static Map<String, Object> beanToMap(Object bean, boolean isToUnderlineCase, boolean ignoreNullValue) {
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
    public static Map<String, Object> beanToMap(Object bean, Map<String, Object> targetMap, final boolean isToUnderlineCase, boolean ignoreNullValue) {
        if (bean == null) {
            return null;
        }

        return beanToMap(bean, targetMap, ignoreNullValue, key -> isToUnderlineCase ? StringKit.toUnderlineCase(key) : key);
    }

    /**
     * 对象转Map
     * 通过实现{@link Editor} 可以自定义字段值,如果这个Editor返回null则忽略这个字段,以便实现：
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
     * @param keyEditor       属性字段(Map的key)编辑器,用于筛选、编辑key
     * @return Map
     */
    public static Map<String, Object> beanToMap(Object bean, Map<String, Object> targetMap, boolean ignoreNullValue, Editor<String> keyEditor) {
        if (bean == null) {
            return null;
        }

        final Collection<BeanDesc.PropDesc> props = BeanKit.getBeanDesc(bean.getClass()).getProps();

        String key;
        Method getter;
        Object value;
        for (BeanDesc.PropDesc prop : props) {
            key = prop.getFieldName();
            // 过滤class属性
            // 得到property对应的getter方法
            getter = prop.getGetter();
            if (null != getter) {
                // 只读取有getter方法的属性
                try {
                    value = getter.invoke(bean);
                } catch (Exception ignore) {
                    continue;
                }
                if (false == ignoreNullValue || (null != value && false == value.equals(bean))) {
                    key = keyEditor.edit(key);
                    if (null != key) {
                        targetMap.put(key, value);
                    }
                }
            }
        }
        return targetMap;
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
        T target = ReflectKit.newInstanceIfPossible(clazz);
        copyProperties(source, target, CopyOptions.create().setIgnoreProperties(ignoreProperties));
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
        copyProperties(source, target, CopyOptions.create().setIgnoreProperties(ignoreProperties));
    }

    /**
     * 复制Bean对象属性
     *
     * @param source     源Bean对象
     * @param target     目标Bean对象
     * @param ignoreCase 是否忽略大小写
     */
    public static void copyProperties(Object source, Object target, boolean ignoreCase) {
        BeanCopier.create(source, target, CopyOptions.create().setIgnoreCase(ignoreCase)).copy();
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
        if (null == copyOptions) {
            copyOptions = new CopyOptions();
        }
        BeanCopier.create(source, target, copyOptions).copy();
    }

    /**
     * 给定的Bean的类名是否匹配指定类名字符串
     * 如果isSimple为{@code false},则只匹配类名而忽略包名
     * 如果isSimple为{@code true},则匹配包括包名的全类名
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
        if (bean == null) {
            return null;
        }

        final Field[] fields = ReflectKit.getFields(bean.getClass());
        for (Field field : fields) {
            if (isStatic(field)) {
                continue;
            }
            if (ignoreFields != null && ArrayKit.containsIgnoreCase(ignoreFields, field.getName())) {
                // 不处理忽略的Fields
                continue;
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
        }

        return bean;
    }

    /**
     * 是否同时存在一个或多个修饰符(可能有多个修饰符,如果有指定的修饰符则返回true)
     *
     * @param clazz         类
     * @param modifierTypes 修饰符枚举
     * @return 是否有指定修饰符, 如果有返回true, 否则false, 如果提供参数为null返回false
     */
    public static boolean hasModifier(Class<?> clazz, BeanKit.ModifierType... modifierTypes) {
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
    public static boolean hasModifier(Constructor<?> constructor, BeanKit.ModifierType... modifierTypes) {
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
    public static boolean hasModifier(Method method, BeanKit.ModifierType... modifierTypes) {
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
    public static boolean hasModifier(Field field, BeanKit.ModifierType... modifierTypes) {
        if (null == field || ArrayKit.isEmpty(modifierTypes)) {
            return false;
        }
        return 0 != (field.getModifiers() & modifiersToInt(modifierTypes));
    }

    /**
     * 是否是Public字段
     *
     * @param field 字段
     * @return 是否是Public
     */
    public static boolean isPublic(Field field) {
        return hasModifier(field, BeanKit.ModifierType.PUBLIC);
    }

    /**
     * 是否是Public方法
     *
     * @param method 方法
     * @return 是否是Public
     */
    public static boolean isPublic(Method method) {
        return hasModifier(method, BeanKit.ModifierType.PUBLIC);
    }

    /**
     * 是否是Public类
     *
     * @param clazz 类
     * @return 是否是Public
     */
    public static boolean isPublic(Class<?> clazz) {
        return hasModifier(clazz, BeanKit.ModifierType.PUBLIC);
    }

    /**
     * 是否是Public构造
     *
     * @param constructor 构造
     * @return 是否是Public
     */
    public static boolean isPublic(Constructor<?> constructor) {
        return hasModifier(constructor, BeanKit.ModifierType.PUBLIC);
    }

    /**
     * 是否是static字段
     *
     * @param field 字段
     * @return 是否是static
     */
    public static boolean isStatic(Field field) {
        return hasModifier(field, BeanKit.ModifierType.STATIC);
    }

    /**
     * 是否是static方法
     *
     * @param method 方法
     * @return 是否是static
     */
    public static boolean isStatic(Method method) {
        return hasModifier(method, BeanKit.ModifierType.STATIC);
    }

    /**
     * 是否是static类
     *
     * @param clazz 类
     * @return 是否是static
     */
    public static boolean isStatic(Class<?> clazz) {
        return hasModifier(clazz, BeanKit.ModifierType.STATIC);
    }

    /**
     * 多个修饰符做“与”操作,表示同时存在多个修饰符
     *
     * @param modifierTypes 修饰符列表,元素不能为空
     * @return “与”之后的修饰符
     */
    private static int modifiersToInt(BeanKit.ModifierType... modifierTypes) {
        int modifier = modifierTypes[0].getValue();
        for (int i = 1; i < modifierTypes.length; i++) {
            modifier &= modifierTypes[i].getValue();
        }
        return modifier;
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
        private int value;

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
