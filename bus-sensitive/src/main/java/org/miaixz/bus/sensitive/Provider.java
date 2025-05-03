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
package org.miaixz.bus.sensitive;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.sensitive.magic.annotation.*;
import org.miaixz.bus.sensitive.metric.BuiltInProvider;
import org.miaixz.bus.sensitive.metric.ConditionProvider;
import org.miaixz.bus.sensitive.metric.StrategyProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 敏感数据处理提供者， 提供对象脱敏功能，支持深度拷贝和 JSON 输出。 通过反射和原生 Java 实现，确保安全性和性能。
 *
 * @param <T> 参数类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class Provider<T> {

    /**
     * 脱敏属性
     */
    private String[] value;

    /**
     * 深度拷贝对象
     *
     * @param object 要拷贝的对象
     * @return 深拷贝后的对象
     */
    public static <T> T clone(T object) {
        if (object == null) {
            return null;
        }
        try {
            // 使用序列化进行深拷贝
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
            oos.writeObject(object);
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new InternalException("深度拷贝失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查对象是否已脱敏
     *
     * @param object 原始数据
     * @return 是否已脱敏
     */
    public static boolean alreadyBeSentisived(Object object) {
        return object == null || object.toString().contains(Symbol.STAR);
    }

    /**
     * 对对象进行脱敏处理
     *
     * @param object     原始对象
     * @param annotation 注解信息
     * @param clone      是否进行克隆
     * @return 脱敏后的对象
     */
    public T on(T object, Annotation annotation, boolean clone) {
        if (ObjectKit.isEmpty(object)) {
            return object;
        }

        if (ObjectKit.isNotEmpty(annotation)) {
            Sensitive sensitive = (Sensitive) annotation;
            this.value = sensitive.field();
        }

        // 1. 初始化
        final Class clazz = object.getClass();
        final Context context = new Context();

        T result = clone ? clone(object) : object;
        handleClassField(context, result, clazz);
        return result;
    }

    /**
     * 返回脱敏后的 JSON 字符串
     *
     * @param object     对象
     * @param annotation 注解
     * @return 脱敏后的 JSON 字符串
     */
    public String json(T object, Annotation annotation) {
        if (ObjectKit.isEmpty(object)) {
            return JsonKit.toJsonString(null);
        }

        if (ObjectKit.isNotEmpty(annotation)) {
            Sensitive sensitive = (Sensitive) annotation;
            this.value = sensitive.field();
        }

        final Context context = new Context();
        T copy = clone(object);
        handleClassField(context, copy, copy.getClass());
        return JsonKit.toJsonString(copy);
    }

    /**
     * 处理类字段的脱敏
     *
     * @param context    执行上下文
     * @param copyObject 拷贝的对象
     * @param clazz      类类型
     */
    private void handleClassField(final Context context, final Object copyObject, final Class<?> clazz) {
        List<Field> fieldList = ListKit.of(FieldKit.getFields(clazz));
        context.setAllFieldList(fieldList);
        context.setCurrentObject(copyObject);

        try {
            for (Field field : fieldList) {
                if (ArrayKit.isNotEmpty(this.value) && !Arrays.asList(this.value).contains(field.getName())) {
                    continue;
                }

                final Class<?> fieldTypeClass = field.getType();
                context.setCurrentField(field);

                Entry sensitiveEntry = field.getAnnotation(Entry.class);
                if (ObjectKit.isNotNull(sensitiveEntry)) {
                    if (TypeKit.isJavaBean(fieldTypeClass)) {
                        Object fieldNewObject = field.get(copyObject);
                        handleClassField(context, fieldNewObject, fieldTypeClass);
                    } else if (TypeKit.isArray(fieldTypeClass)) {
                        processArrayField(context, copyObject, field);
                    } else if (TypeKit.isCollection(fieldTypeClass)) {
                        processCollectionField(context, copyObject, field);
                    } else {
                        handleSensitive(context, copyObject, field);
                    }
                } else {
                    handleSensitive(context, copyObject, field);
                }
            }
        } catch (IllegalAccessException e) {
            throw new InternalException("字段访问失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理数组字段
     *
     * @param context    上下文
     * @param copyObject 对象
     * @param field      字段
     */
    private void processArrayField(final Context context, final Object copyObject, final Field field)
            throws IllegalAccessException {
        Object[] arrays = (Object[]) field.get(copyObject);
        if (ArrayKit.isEmpty(arrays)) {
            return;
        }

        Object firstArrayEntry = ArrayKit.firstNonNull(arrays);
        if (firstArrayEntry == null) {
            return;
        }

        final Class<?> entryFieldClass = firstArrayEntry.getClass();
        if (needHandleEntryType(entryFieldClass)) {
            for (Object arrayEntry : arrays) {
                handleClassField(context, arrayEntry, entryFieldClass);
            }
        } else {
            Object newArray = Array.newInstance(entryFieldClass, arrays.length);
            for (int i = 0; i < arrays.length; i++) {
                Object result = handleSensitiveEntry(context, arrays[i], field);
                Array.set(newArray, i, result);
            }
            field.set(copyObject, newArray);
        }
    }

    /**
     * 处理集合字段
     *
     * @param context    上下文
     * @param copyObject 对象
     * @param field      字段
     */
    private void processCollectionField(final Context context, final Object copyObject, final Field field)
            throws IllegalAccessException {
        Collection<Object> entryCollection = (Collection<Object>) field.get(copyObject);
        if (CollKit.isEmpty(entryCollection)) {
            return;
        }

        Object firstCollectionEntry = entryCollection.iterator().next();
        Class<?> collectionEntryClass = firstCollectionEntry.getClass();

        if (needHandleEntryType(collectionEntryClass)) {
            for (Object collectionEntry : entryCollection) {
                handleClassField(context, collectionEntry, collectionEntryClass);
            }
        } else {
            List<Object> newResultList = new ArrayList<>(entryCollection.size());
            for (Object entry : entryCollection) {
                newResultList.add(handleSensitiveEntry(context, entry, field));
            }
            field.set(copyObject, newResultList);
        }
    }

    /**
     * 处理单个对象的脱敏
     *
     * @param context 上下文
     * @param entry   明细
     * @param field   字段信息
     * @return 处理后的信息
     */
    private Object handleSensitiveEntry(final Context context, final Object entry, final Field field) {
        try {
            Shield sensitive = field.getAnnotation(Shield.class);
            if (ObjectKit.isNotNull(sensitive)) {
                ConditionProvider condition = ReflectKit.newInstance(sensitive.condition());
                if (condition.valid(context)) {
                    context.setShield(sensitive);
                    StrategyProvider strategy = ReflectKit.newInstance(sensitive.strategy());
                    return strategy.build(entry, context);
                }
            }

            Annotation[] annotations = field.getAnnotations();
            if (ArrayKit.isNotEmpty(annotations)) {
                ConditionProvider condition = getCondition(annotations);
                if (ObjectKit.isNull(condition) || condition.valid(context)) {
                    StrategyProvider strategy = getStrategy(annotations);
                    if (ObjectKit.isNotNull(strategy)) {
                        return strategy.build(entry, context);
                    }
                }
            }
            return entry;
        } catch (Exception e) {
            throw new InternalException("脱敏处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理脱敏信息
     *
     * @param context    上下文
     * @param copyObject 复制的对象
     * @param field      当前字段
     */
    private void handleSensitive(final Context context, final Object copyObject, final Field field) {
        try {
            Shield sensitive = field.getAnnotation(Shield.class);
            if (ObjectKit.isNotNull(sensitive)) {
                ConditionProvider condition = ReflectKit.newInstance(sensitive.condition());
                if (condition.valid(context)) {
                    context.setShield(sensitive);
                    StrategyProvider strategy = ReflectKit.newInstance(sensitive.strategy());
                    final Object originalFieldVal = field.get(copyObject);
                    final Object result = strategy.build(originalFieldVal, context);
                    field.set(copyObject, result);
                }
            }

            // 系统内置自定义注解的处理,获取所有的注解
            Annotation[] annotations = field.getAnnotations();
            if (ArrayKit.isNotEmpty(annotations)) {
                ConditionProvider condition = getCondition(annotations);
                if (ObjectKit.isNull(condition) || condition.valid(context)) {
                    StrategyProvider strategy = getStrategy(annotations);
                    if (ObjectKit.isNotNull(strategy)) {
                        final Object originalFieldVal = field.get(copyObject);
                        final Object result = strategy.build(originalFieldVal, context);
                        field.set(copyObject, result);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取策略
     *
     * @param annotations 字段注解
     * @return 策略提供者
     */
    private StrategyProvider getStrategy(final Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Strategy strategy = annotation.annotationType().getAnnotation(Strategy.class);
            if (ObjectKit.isNotNull(strategy)) {
                Class<? extends StrategyProvider> clazz = strategy.value();
                if (BuiltInProvider.class.equals(clazz)) {
                    return Registry.require(annotation.annotationType());
                }
                return ReflectKit.newInstance(clazz);
            }
        }
        return null;
    }

    /**
     * 获取用户自定义条件
     *
     * @param annotations 字段注解
     * @return 条件提供者
     */
    private ConditionProvider getCondition(final Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Condition condition = annotation.annotationType().getAnnotation(Condition.class);
            if (ObjectKit.isNotNull(condition)) {
                return ReflectKit.newInstance(condition.value());
            }
        }
        return null;
    }

    /**
     * 判断是否需要特殊处理的类型
     *
     * @param fieldTypeClass 字段类型
     * @return 是否需要特殊处理
     */
    private boolean needHandleEntryType(final Class<?> fieldTypeClass) {
        return (TypeKit.isJavaBean(fieldTypeClass) || TypeKit.isArray(fieldTypeClass)
                || TypeKit.isCollection(fieldTypeClass)) && !TypeKit.isBase(fieldTypeClass)
                && !TypeKit.isMap(fieldTypeClass);
    }

}