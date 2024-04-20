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
package org.miaixz.bus.sensitive;

import com.alibaba.fastjson.serializer.BeanContext;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.toolkit.*;
import org.miaixz.bus.sensitive.annotation.Condition;
import org.miaixz.bus.sensitive.annotation.Entry;
import org.miaixz.bus.sensitive.annotation.Shield;
import org.miaixz.bus.sensitive.provider.ConditionProvider;
import org.miaixz.bus.sensitive.provider.StrategyProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 默认的上下文过滤器
 * <p>
 * {@link Entry} 放在对象时,则不用特殊处理
 * 只需要处理 集合、数组集合
 * 注意： 和 {@link Builder#on(Object)} 的区别
 * 因为 FastJSON 本身的转换问题,如果对象中存储的是集合对象列表,会导致显示不是信息本身
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Filter implements com.alibaba.fastjson.serializer.ContextValueFilter {

    /**
     * 脱敏上下文
     */
    private final Context sensitiveContext;

    public Filter(Context context) {
        this.sensitiveContext = context;
    }

    /**
     * 获取用户自定义条件
     *
     * @param annotations 字段上的注解
     * @return 对应的用户自定义条件
     */
    private static ConditionProvider getConditionOpt(final Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            org.miaixz.bus.sensitive.annotation.Condition sensitiveCondition = annotation.annotationType().getAnnotation(Condition.class);
            if (ObjectKit.isNotNull(sensitiveCondition)) {
                return ClassKit.newInstance(sensitiveCondition.value());
            }
        }
        return null;
    }

    @Override
    public Object process(BeanContext context, Object object, String name, Object value) {
        // 对象为 MAP 的时候,FastJson map 对应的 context 为 NULL
        if (ObjectKit.isNull(context)) {
            return value;
        }

        // 信息初始化
        final java.lang.reflect.Field field = context.getField();
        final Class clazz = context.getBeanClass();
        final List<java.lang.reflect.Field> fieldList = ClassKit.getAllFieldList(clazz);
        sensitiveContext.setCurrentField(field);
        sensitiveContext.setCurrentObject(object);
        sensitiveContext.setBeanClass(clazz);
        sensitiveContext.setAllFieldList(fieldList);

        // 这里将缺少对于列表/集合/数组 的处理 可以单独实现
        // 设置当前处理的字段
        Entry sensitiveEntry = field.getAnnotation(Entry.class);
        if (ObjectKit.isNull(sensitiveEntry)) {
            sensitiveContext.setEntry(value);
            return handleSensitive(sensitiveContext, field);
        }

        //2. 处理 @Entry 注解
        final Class fieldTypeClass = field.getType();
        if (TypeKit.isJavaBean(fieldTypeClass)) {
            //不作处理,因为 json 本身就会进行递归处理
            return value;
        }
        if (TypeKit.isMap(fieldTypeClass)) {
            return value;
        }

        if (TypeKit.isArray(fieldTypeClass)) {
            // 为数组类型
            Object[] arrays = (Object[]) value;
            if (ArrayKit.isNotEmpty(arrays)) {
                Object firstArrayEntry = ArrayKit.firstNotNull(arrays).get();
                final Class entryFieldClass = firstArrayEntry.getClass();

                if (isBaseType(entryFieldClass)) {
                    //2, 基础值,直接循环设置即可
                    final int arrayLength = arrays.length;
                    Object newArray = Array.newInstance(entryFieldClass, arrayLength);
                    for (int i = 0; i < arrayLength; i++) {
                        Object entry = arrays[i];
                        sensitiveContext.setEntry(entry);
                        Object result = handleSensitive(sensitiveContext, field);
                        Array.set(newArray, i, result);
                    }

                    return newArray;
                }
            }
        }
        if (TypeKit.isCollection(fieldTypeClass)) {
            // Collection 接口的子类
            final Collection<?> entryCollection = (Collection<?>) value;
            if (CollKit.isNotEmpty(entryCollection)) {
                Object firstCollectionEntry = CollKit.firstNotNullElem(entryCollection).get();

                if (isBaseType(firstCollectionEntry.getClass())) {
                    //2, 基础值,直接循环设置即可
                    List<Object> newResultList = new ArrayList<>(entryCollection.size());
                    for (Object entry : entryCollection) {
                        sensitiveContext.setEntry(entry);
                        Object result = handleSensitive(sensitiveContext, field);
                        newResultList.add(result);
                    }
                    return newResultList;
                }
            }
        }

        // 默认返回原来的值
        return value;
    }

    /**
     * 处理脱敏信息
     *
     * @param context 上下文
     * @param field   当前字段
     */
    private Object handleSensitive(final Context context,
                                   final java.lang.reflect.Field field) {
        try {
            // 原始字段值
            final Object originalFieldVal = context.getEntry();

            //处理 @Sensitive
            Shield sensitive = field.getAnnotation(Shield.class);
            if (ObjectKit.isNotNull(sensitive)) {
                Class<? extends ConditionProvider> conditionClass = sensitive.condition();
                ConditionProvider condition = conditionClass.getConstructor().newInstance();
                if (condition.valid(context)) {
                    StrategyProvider strategy = Registry.require(sensitive.type());
                    if (ObjectKit.isEmpty(strategy)) {
                        Class<? extends StrategyProvider> strategyClass = sensitive.strategy();
                        strategy = strategyClass.getConstructor().newInstance();
                    }
                    sensitiveContext.setEntry(null);
                    return strategy.build(originalFieldVal, context);
                }
            }

            // 系统内置自定义注解的处理,获取所有的注解
            Annotation[] annotations = field.getAnnotations();
            if (ArrayKit.isNotEmpty(annotations)) {
                ConditionProvider conditionOptional = getConditionOpt(annotations);
                if (ObjectKit.isNotEmpty(conditionOptional)) {
                    final StrategyProvider strategyProvider = Registry.require(annotations);
                    if (ObjectKit.isEmpty(strategyProvider)) {
                        sensitiveContext.setEntry(null);
                        return strategyProvider.build(originalFieldVal, context);
                    }
                }
            }
            sensitiveContext.setEntry(null);
            return originalFieldVal;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 特殊类型
     * (1)map
     * (2)对象
     * (3)集合/数组
     *
     * @param fieldTypeClass 字段类型
     * @return 是否
     */
    private boolean isBaseType(final Class fieldTypeClass) {
        if (TypeKit.isBase(fieldTypeClass)) {
            return true;
        }

        if (TypeKit.isJavaBean(fieldTypeClass)
                || TypeKit.isArray(fieldTypeClass)
                || TypeKit.isCollection(fieldTypeClass)
                || TypeKit.isMap(fieldTypeClass)) {
            return false;
        }
        return true;
    }

}
