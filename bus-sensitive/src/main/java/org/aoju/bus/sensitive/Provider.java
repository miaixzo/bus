/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.sensitive;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.ContextValueFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.*;
import org.aoju.bus.sensitive.annotation.Condition;
import org.aoju.bus.sensitive.annotation.*;
import org.aoju.bus.sensitive.provider.ConditionProvider;
import org.aoju.bus.sensitive.provider.StrategyProvider;
import org.aoju.bus.sensitive.strategy.BuiltInStrategy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * 脱敏接口
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
     * 深度复制
     * 1. 为了避免深拷贝要求用户实现 clone 和 序列化的相关接口
     * 2. 为了避免使用 dozer 这种比较重的工具
     * 3. 自己实现暂时工作量比较大
     * <p>
     * 暂时使用 fastJson 作为实现深度拷贝的方式
     *
     * @param object 对象
     * @param <T>    泛型
     * @return 深拷贝后的对象
     */
    public static <T> T clone(T object) {
        final Class clazz = object.getClass();
        String jsonString = JSON.toJSONString(object);
        return (T) JSON.parseObject(jsonString, clazz);
    }

    /**
     * 是否已经是脱敏过的内容了
     *
     * @param object 原始数据
     * @return 是否已经脱敏了
     */
    public static boolean alreadyBeSentisived(Object object) {
        return null == object || object.toString().indexOf(Symbol.STAR) > 0;
    }

    /**
     * 将json字符串转化为StringObject类型的map
     *
     * @param jsonStr json字符串
     * @return map
     */
    public static Map<String, Object> parseToObjectMap(String jsonStr) {
        return JSON.parseObject(jsonStr, new TypeReference<LinkedHashMap<String, Object>>() {
        });
    }

    /**
     * 将map转化为json字符串
     *
     * @param params 参数集合
     * @return json
     */
    public static String parseMaptoJSONString(Map<String, Object> params) {
        return JSON.toJSONString(params, SerializerFeature.WriteMapNullValue);
    }

    /**
     * 对象进行脱敏操作
     * 原始对象不变,返回脱敏后的新对象
     * 1. 为什么这么设计？
     * 不能因为脱敏,就导致代码中的对象被改变 否则代码逻辑会出现问题
     *
     * @param object     原始对象
     * @param annotation 注解信息
     * @param clone      是否克隆
     * @return 脱敏后的新对象
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

        if (clone) {
            // 2. 深度复制,不改变原始对象
            T copy = clone(object);
            handleClassField(context, copy, clazz);
            return copy;
        }

        // 3. 脱敏处理
        handleClassField(context, object, clazz);
        return object;
    }

    /**
     * 返回脱敏后的 json
     * 1. 避免 desCopy 造成的对象新建的性能浪费
     *
     * @param object     对象
     * @param annotation 注解
     * @return json
     */
    public String json(T object, Annotation annotation) {
        if (ObjectKit.isEmpty(object)) {
            return JSON.toJSONString(object);
        }

        if (ObjectKit.isNotEmpty(annotation)) {
            Sensitive sensitive = (Sensitive) annotation;
            this.value = sensitive.field();
        }

        final Context context = new Context();
        ContextValueFilter filter = new Filter(context);
        return JSON.toJSONString(object, filter);
    }

    /**
     * 处理脱敏相关信息
     *
     * @param context    执行上下文
     * @param copyObject 拷贝的新对象
     * @param clazz      class 类型
     */
    private void handleClassField(final Context context,
                                  final Object copyObject,
                                  final Class clazz) {
        // 每一个实体对应的字段,只对当前 clazz 生效
        List<Field> fieldList = ClassKit.getAllFieldList(clazz);
        context.setAllFieldList(fieldList);
        context.setCurrentObject(copyObject);

        try {
            for (Field field : fieldList) {
                if (ArrayKit.isNotEmpty(this.value)) {
                    if (!Arrays.asList(this.value).contains(field.getName())) {
                        continue;
                    }
                }
                // 设置当前处理的字段
                final Class fieldTypeClass = field.getType();
                context.setCurrentField(field);

                // 处理 @Entry 注解
                Entry sensitiveEntry = field.getAnnotation(Entry.class);
                if (ObjectKit.isNotNull(sensitiveEntry)) {
                    if (TypeKit.isJavaBean(fieldTypeClass)) {
                        // 为普通 javabean 对象
                        final Object fieldNewObject = field.get(copyObject);
                        handleClassField(context, fieldNewObject, fieldTypeClass);
                    } else if (TypeKit.isArray(fieldTypeClass)) {
                        // 为数组类型
                        Object[] arrays = (Object[]) field.get(copyObject);
                        if (ArrayKit.isNotEmpty(arrays)) {
                            Object firstArrayEntry = arrays[0];
                            final Class entryFieldClass = firstArrayEntry.getClass();

                            //1. 如果需要特殊处理,则循环特殊处理
                            if (needHandleEntryType(entryFieldClass)) {
                                for (Object arrayEntry : arrays) {
                                    handleClassField(context, arrayEntry, entryFieldClass);
                                }
                            } else {
                                //2, 基础值,直接循环设置即可
                                final int arrayLength = arrays.length;
                                Object newArray = Array.newInstance(entryFieldClass, arrayLength);
                                for (int i = 0; i < arrayLength; i++) {
                                    Object entry = arrays[i];
                                    Object result = handleSensitiveEntry(context, entry, field);
                                    Array.set(newArray, i, result);
                                }
                                field.set(copyObject, newArray);
                            }
                        }
                    } else if (TypeKit.isCollection(fieldTypeClass)) {
                        // Collection 接口的子类
                        final Collection<Object> entryCollection = (Collection<Object>) field.get(copyObject);
                        if (CollKit.isNotEmpty(entryCollection)) {
                            Object firstCollectionEntry = entryCollection.iterator().next();
                            Class collectionEntryClass = firstCollectionEntry.getClass();

                            //1. 如果需要特殊处理,则循环特殊处理
                            if (needHandleEntryType(collectionEntryClass)) {
                                for (Object collectionEntry : entryCollection) {
                                    handleClassField(context, collectionEntry, collectionEntryClass);
                                }
                            } else {
                                //2, 基础值,直接循环设置即可
                                List<Object> newResultList = new ArrayList<>(entryCollection.size());
                                for (Object entry : entryCollection) {
                                    Object result = handleSensitiveEntry(context, entry, field);
                                    newResultList.add(result);
                                }
                                field.set(copyObject, newResultList);
                            }
                        }
                    } else {
                        // 1. 常见的基本类型,不做处理
                        // 2. 如果为 map,暂时不支持处理 后期可以考虑支持 value 的脱敏,或者 key 的脱敏
                        // 3. 其他
                        // 处理单个字段脱敏信息
                        handleSensitive(context, copyObject, field);
                    }
                } else {
                    handleSensitive(context, copyObject, field);
                }
            }

        } catch (IllegalAccessException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 处理需脱敏的单个对象
     * <p>
     * 1. 为了简化操作,所有的自定义注解使用多个,不生效
     * 2. 生效顺序如下：
     * (1)Sensitive
     * (2)系统内置自定义注解
     * (3)用户自定义注解
     *
     * @param context 上下文
     * @param entry   明细
     * @param field   字段信息
     * @return 处理后的信息
     */
    private Object handleSensitiveEntry(final Context context,
                                        final Object entry,
                                        final Field field) {
        try {
            //处理 @Field
            Shield sensitive = field.getAnnotation(Shield.class);
            if (ObjectKit.isNotNull(sensitive)) {
                Class<? extends ConditionProvider> conditionClass = sensitive.condition();
                ConditionProvider condition = conditionClass.getConstructor().newInstance();
                if (condition.valid(context)) {
                    context.setShield(sensitive);
                    Class<? extends StrategyProvider> strategyClass = sensitive.strategy();
                    StrategyProvider strategy = strategyClass.getConstructor().newInstance();
                    return strategy.build(entry, context);
                }
            }

            // 获取所有的注解
            Annotation[] annotations = field.getAnnotations();
            if (ArrayKit.isNotEmpty(annotations)) {
                ConditionProvider condition = getCondition(annotations);
                if (ObjectKit.isNull(condition)
                        || condition.valid(context)) {
                    StrategyProvider strategy = getStrategy(annotations);
                    if (ObjectKit.isNotNull(strategy)) {
                        return strategy.build(entry, context);
                    }
                }
            }
            return entry;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InternalException(e);
        } catch (InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理脱敏信息
     *
     * @param context    上下文
     * @param copyObject 复制的对象
     * @param field      当前字段
     */
    private void handleSensitive(final Context context,
                                 final Object copyObject,
                                 final Field field) {
        try {
            //处理 @Field
            Shield sensitive = field.getAnnotation(Shield.class);
            if (null != sensitive) {
                Class<? extends ConditionProvider> conditionClass = sensitive.condition();
                ConditionProvider condition = conditionClass.getConstructor().newInstance();
                if (condition.valid(context)) {
                    context.setShield(sensitive);
                    Class<? extends StrategyProvider> strategyClass = sensitive.strategy();
                    StrategyProvider strategy = strategyClass.getConstructor().newInstance();
                    final Object originalFieldVal = field.get(copyObject);
                    final Object result = strategy.build(originalFieldVal, context);
                    field.set(copyObject, result);
                }
            }

            // 系统内置自定义注解的处理,获取所有的注解
            Annotation[] annotations = field.getAnnotations();
            if (ArrayKit.isNotEmpty(annotations)) {
                ConditionProvider condition = getCondition(annotations);
                if (ObjectKit.isNull(condition)
                        || condition.valid(context)) {
                    StrategyProvider strategy = getStrategy(annotations);
                    if (ObjectKit.isNotNull(strategy)) {
                        final Object originalFieldVal = field.get(copyObject);
                        final Object result = strategy.build(originalFieldVal, context);
                        field.set(copyObject, result);
                    }
                }
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取策略
     *
     * @param annotations 字段对应注解
     * @return 策略
     */
    private StrategyProvider getStrategy(final Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Strategy strategy = annotation.annotationType().getAnnotation(Strategy.class);
            if (ObjectKit.isNotNull(strategy)) {
                Class<? extends StrategyProvider> clazz = strategy.value();
                if (BuiltInStrategy.class.equals(clazz)) {
                    return Registry.require(annotation.annotationType());
                } else {
                    return ClassKit.newInstance(clazz);
                }
            }
        }
        return null;
    }

    /**
     * 获取用户自定义条件
     *
     * @param annotations 字段上的注解
     * @return 对应的用户自定义条件
     */
    private ConditionProvider getCondition(final Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Condition condition = annotation.annotationType().getAnnotation(Condition.class);
            if (ObjectKit.isNotNull(condition)) {
                Class<? extends ConditionProvider> clazz = condition.value();
                return ClassKit.newInstance(clazz);
            }
        }
        return null;
    }

    /**
     * 需要特殊处理的列表/对象类型
     *
     * @param fieldTypeClass 字段类型
     * @return 是否
     */
    private boolean needHandleEntryType(final Class fieldTypeClass) {
        if (TypeKit.isBase(fieldTypeClass)
                || TypeKit.isMap(fieldTypeClass)) {
            return false;
        }

        if (TypeKit.isJavaBean(fieldTypeClass)
                || TypeKit.isArray(fieldTypeClass)
                || TypeKit.isCollection(fieldTypeClass)) {
            return true;
        }
        return false;
    }

}
