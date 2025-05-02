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

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.sensitive.magic.annotation.Condition;
import org.miaixz.bus.sensitive.magic.annotation.Entry;
import org.miaixz.bus.sensitive.magic.annotation.Shield;
import org.miaixz.bus.sensitive.metric.ConditionProvider;
import org.miaixz.bus.sensitive.metric.StrategyProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 敏感数据处理的上下文过滤器，不依赖第三方 JSON 库。 处理带有 @Entry 或 @Shield 注解的字段脱敏。 通过最小化对象创建和高效的类型检查优化性能。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Filter {

    /**
     * 脱敏上下文
     */
    private final Context sensitiveContext;

    /**
     * 构造函数，初始化上下文
     *
     * @param context 脱敏上下文
     */
    public Filter(Context context) {
        this.sensitiveContext = context;
    }

    /**
     * 从注解中获取自定义条件提供者
     *
     * @param annotations 字段注解数组
     * @return 条件提供者实例，若无则返回 null
     */
    private static ConditionProvider getConditionOpt(final Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Condition sensitiveCondition = annotation.annotationType().getAnnotation(Condition.class);
            if (ObjectKit.isNotNull(sensitiveCondition)) {
                return ReflectKit.newInstance(sensitiveCondition.value());
            }
        }
        return null;
    }

    /**
     * 处理对象字段进行脱敏
     *
     * @param object 被处理的对象
     * @param field  被处理的字段
     * @param value  字段值
     * @return 脱敏后的值
     */
    public Object process(Object object, Field field, Object value) {
        // 初始化上下文
        sensitiveContext.setCurrentField(field);
        sensitiveContext.setCurrentObject(object);
        sensitiveContext.setBeanClass(object.getClass());
        sensitiveContext.setAllFieldList(ListKit.of(FieldKit.getFields(object.getClass())));

        // 处理 @Entry 注解
        Entry sensitiveEntry = field.getAnnotation(Entry.class);
        if (ObjectKit.isNull(sensitiveEntry)) {
            sensitiveContext.setEntry(value);
            return handleSensitive(sensitiveContext, field);
        }

        // 处理字段类型
        final Class<?> fieldTypeClass = field.getType();
        if (TypeKit.isJavaBean(fieldTypeClass) || TypeKit.isMap(fieldTypeClass)) {
            return value; // JavaBean 和 Map 递归处理或跳过
        }

        if (TypeKit.isArray(fieldTypeClass)) {
            return processArray(fieldTypeClass, (Object[]) value);
        }

        if (TypeKit.isCollection(fieldTypeClass)) {
            return processCollection((Collection<?>) value);
        }

        return value; // 无需处理时返回原值
    }

    /**
     * 处理数组类型
     *
     * @param fieldTypeClass 数组元素类型
     * @param arrays         要处理的数组
     * @return 处理后的数组
     */
    private Object processArray(Class<?> fieldTypeClass, Object[] arrays) {
        if (ArrayKit.isEmpty(arrays)) {
            return arrays;
        }

        Object firstArrayEntry = ArrayKit.firstNonNull(arrays);
        if (firstArrayEntry == null) {
            return arrays;
        }

        final Class<?> entryFieldClass = firstArrayEntry.getClass();
        if (isBaseType(entryFieldClass)) {
            Object newArray = Array.newInstance(entryFieldClass, arrays.length);
            for (int i = 0; i < arrays.length; i++) {
                sensitiveContext.setEntry(arrays[i]);
                Array.set(newArray, i, handleSensitive(sensitiveContext, sensitiveContext.getCurrentField()));
            }
            return newArray;
        }
        return arrays;
    }

    /**
     * 处理集合类型
     *
     * @param collection 要处理的集合
     * @return 处理后的集合
     */
    private Object processCollection(Collection<?> collection) {
        if (CollKit.isEmpty(collection)) {
            return collection;
        }

        Object firstCollectionEntry = ArrayKit.firstNonNull(collection);
        if (firstCollectionEntry == null) {
            return collection;
        }

        if (isBaseType(firstCollectionEntry.getClass())) {
            List<Object> newResultList = new ArrayList<>(collection.size());
            for (Object entry : collection) {
                sensitiveContext.setEntry(entry);
                newResultList.add(handleSensitive(sensitiveContext, sensitiveContext.getCurrentField()));
            }
            return newResultList;
        }
        return collection;
    }

    /**
     * 处理脱敏信息
     *
     * @param context 上下文
     * @param field   当前字段
     * @return 脱敏后的值
     */
    private Object handleSensitive(final Context context, final Field field) {
        try {
            final Object originalFieldVal = context.getEntry();

            // 处理 @Shield 注解
            Shield sensitive = field.getAnnotation(Shield.class);
            if (ObjectKit.isNotNull(sensitive)) {
                ConditionProvider condition = ReflectKit.newInstance(sensitive.condition());
                if (condition.valid(context)) {
                    StrategyProvider strategy = Registry.require(sensitive.type());
                    if (ObjectKit.isEmpty(strategy)) {
                        strategy = ReflectKit.newInstance(sensitive.strategy());
                    }
                    context.setEntry(null);
                    return strategy.build(originalFieldVal, context);
                }
            }

            // 处理自定义注解
            Annotation[] annotations = field.getAnnotations();
            if (ArrayKit.isNotEmpty(annotations)) {
                ConditionProvider condition = getConditionOpt(annotations);
                if (ObjectKit.isNotEmpty(condition) && condition.valid(context)) {
                    StrategyProvider strategy = Registry.require(annotations);
                    if (ObjectKit.isNotEmpty(strategy)) {
                        context.setEntry(null);
                        return strategy.build(originalFieldVal, context);
                    }
                }
            }
            context.setEntry(null);
            return originalFieldVal;
        } catch (Exception e) {
            throw new InternalException("脱敏处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 判断是否为基本类型
     *
     * @param fieldTypeClass 字段类型
     * @return 是否为基本类型
     */
    private boolean isBaseType(final Class<?> fieldTypeClass) {
        return TypeKit.isBase(fieldTypeClass) && !TypeKit.isJavaBean(fieldTypeClass) && !TypeKit.isArray(fieldTypeClass)
                && !TypeKit.isCollection(fieldTypeClass) && !TypeKit.isMap(fieldTypeClass);
    }

}