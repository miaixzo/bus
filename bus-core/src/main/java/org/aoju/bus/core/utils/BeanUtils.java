/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.utils;

import org.aoju.bus.core.beans.*;
import org.aoju.bus.core.beans.copier.BeanCopier;
import org.aoju.bus.core.beans.copier.CopyOptions;
import org.aoju.bus.core.beans.copier.ValueProvider;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.lang.Editor;
import org.aoju.bus.core.lang.Filter;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.map.CaseInsensitiveMap;

import java.beans.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Bean工具类
 * 把一个拥有对属性进行set和get方法的类,我们就可以称之为JavaBean
 *
 * @author Kimi Liu
 * @version 5.6.0
 * @since JDK 1.8+
 */
public class BeanUtils {

    /**
     * 判断是否为Bean对象
     * 判定方法是是否存在只有一个参数的setXXX方法
     *
     * @param clazz 待测试类
     * @return 是否为Bean对象
     */
    public static boolean isBean(Class<?> clazz) {
        if (ClassUtils.isNormalClass(clazz)) {
            final Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getParameterTypes().length == 1 && method.getName().startsWith("set")) {
                    // 检测包含标准的setXXX方法即视为标准的JavaBean
                    return true;
                }
            }
        }
        return false;
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
            for (Field field : ReflectUtils.getFields(bean.getClass())) {
                if ((false == ArrayUtils.contains(ignoreFiledNames, field.getName()))
                        && null != ReflectUtils.getFieldValue(bean, field)) {
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
     * @since 5.0.7
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
        final Field[] fields = ClassUtils.getDeclaredFields(bean.getClass());

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
        if (ClassUtils.isNormalClass(clazz)) {
            final Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getParameterTypes().length == 1 && method.getName().startsWith("set")) {
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
        if (ClassUtils.isNormalClass(clazz)) {
            for (Method method : clazz.getMethods()) {
                if (method.getParameterTypes().length == 0) {
                    if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
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
        if (ClassUtils.isNormalClass(clazz)) {
            for (Field field : clazz.getFields()) {
                if (ModifierUtils.isPublic(field) && false == ModifierUtils.isStatic(field)) {
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
        for (Field field : ReflectUtils.getFields(bean.getClass())) {
            if ((false == ArrayUtils.contains(ignoreFiledNames, field.getName()))//
                    && null == ReflectUtils.getFieldValue(bean, field)) {
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
        for (Field field : ReflectUtils.getFields(bean.getClass())) {
            if (null == ReflectUtils.getFieldValue(bean, field)) {
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
     * @param ignoreField 不需要trim的Field名称列表（不区分大小写）
     * @return the object
     */
    public static <T> T trimStrField(T bean, String... ignoreField) {
        if (bean == null) {
            return bean;
        }

        final Field[] fields = ReflectUtils.getFields(bean.getClass());
        for (Field field : fields) {
            if (ignoreField != null && ArrayUtils.containsIgnoreCase(ignoreField, field.getName())) {
                // 不处理忽略的Fields
                continue;
            }
            if (String.class.equals(field.getType())) {
                // 只有String的Field才处理
                final String val = (String) ReflectUtils.getFieldValue(bean, field);
                if (null != val) {
                    final String trimVal = StringUtils.trim(val);
                    if (false == val.equals(trimVal)) {
                        // Field Value不为null,且首尾有空格才处理
                        ReflectUtils.setFieldValue(bean, field, trimVal);
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
     * @since 3.1.9
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
        return ArrayUtils.filter(beanInfo.getPropertyDescriptors(), (Filter<PropertyDescriptor>) t -> {
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
            return CollUtils.get((Collection<?>) bean, Integer.parseInt(fieldNameOrIndex));
        } else if (ArrayUtils.isArray(bean)) {
            return ArrayUtils.get(bean, Integer.parseInt(fieldNameOrIndex));
        } else {// 普通Bean对象
            return ReflectUtils.getFieldValue(bean, fieldNameOrIndex);
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
            CollUtils.setOrAppend((List) bean, Convert.toInt(fieldNameOrIndex), value);
        } else if (ArrayUtils.isArray(bean)) {
            ArrayUtils.setOrAppend(bean, Convert.toInt(fieldNameOrIndex), value);
        } else {
            // 普通Bean对象
            ReflectUtils.setFieldValue(bean, fieldNameOrIndex, value);
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
        final T target = ReflectUtils.newInstance(clazz);
        copyProperties(source, target);
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
        return fillBean(ReflectUtils.newInstance(beanClass), valueProvider, copyOptions);
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
        return fillBeanWithMap(map, ReflectUtils.newInstance(beanClass), isIgnoreError);
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
        return fillBeanWithMap(map, ReflectUtils.newInstance(beanClass), copyOptions);
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
        return fillBeanWithMapIgnoreCase(map, ReflectUtils.newInstance(beanClass), isIgnoreError);
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
     * @since 3.3.1
     */
    public static <T> T fillBeanWithMap(Map<?, ?> map, T bean, boolean isToCamelCase, CopyOptions copyOptions) {
        if (MapUtils.isEmpty(map)) {
            return bean;
        }
        if (isToCamelCase) {
            map = MapUtils.toCamelCaseMap(map);
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
     * @since 3.2.3
     */
    public static Map<String, Object> beanToMap(Object bean, Map<String, Object> targetMap, final boolean isToUnderlineCase, boolean ignoreNullValue) {
        if (bean == null) {
            return null;
        }

        return beanToMap(bean, targetMap, ignoreNullValue, key -> isToUnderlineCase ? StringUtils.toUnderlineCase(key) : key);
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
     * @param keyEditor       属性字段（Map的key）编辑器,用于筛选、编辑key
     * @return Map
     */
    public static Map<String, Object> beanToMap(Object bean, Map<String, Object> targetMap, boolean ignoreNullValue, Editor<String> keyEditor) {
        if (bean == null) {
            return null;
        }

        final Collection<BeanDesc.PropDesc> props = BeanUtils.getBeanDesc(bean.getClass()).getProps();

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
     * 复制Bean对象属性
     *
     * @param source 源Bean对象
     * @param target 目标Bean对象
     */
    public static void copyProperties(Object source, Object target) {
        copyProperties(source, target, CopyOptions.create());
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
        return ClassUtils.getClassName(bean, isSimple).equals(isSimple ? StringUtils.upperFirst(beanClassName) : beanClassName);
    }

}
