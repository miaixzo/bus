/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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
package org.aoju.bus.metric.support;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ReflectUtils;
import org.aoju.bus.metric.annotation.Service;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 反射相关
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8++
 */
public class ReflectUtil extends ReflectUtils {

    private static final String[] EMPTY_STRING_ARRAY = {};

    private static Map<String, Class<?>> classGenricTypeCache = new HashMap<>(16);

    /**
     * key:obj.getClass().getName() + genericClass.getName()
     */
    private static Map<String, Field> genericTypeFieldCache = new HashMap<>();

    /**
     * 设置某个字段的值
     *
     * @param target    实体类，必须有字段的set方法
     * @param fieldName 字段名
     * @param val       值
     */
    public static void invokeFieldValue(Object target, String fieldName, Object val) {
        String setMethodName = getSetMethodName(fieldName);
        Method[] methods = target.getClass().getDeclaredMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            Class<?>[] methodParams = method.getParameterTypes();

            if (setMethodName.equals(methodName)) {
                // 能否拷贝
                boolean canCopy =
                        // 并且只有一个参数
                        methodParams.length == 1
                                // val是methodParams[0]或他的子类
                                && methodParams[0].isInstance(val) || Number.class.isInstance(val);

                if (canCopy) {
                    try {
                        if (!Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
                            method.setAccessible(true);
                        }
                        method.invoke(target, val);
                        break;
                    } catch (Throwable ex) {
                        throw new InstrumentException("Could not set property '" + fieldName + "' value to target", ex);
                    }
                }
            }
        }
    }

    /**
     * 返回实体类中具有指定泛型的字段
     *
     * @param obj          实体类
     * @param genericClass 指定泛型
     * @return 没有返回null
     */
    public static Field getListFieldWithGeneric(Object obj, Class<?> genericClass) {
        Class<?> objClass = obj.getClass();
        String key = objClass.getName() + genericClass.getName();
        Field value = genericTypeFieldCache.get(key);
        if (value != null) {
            return value;
        }
        Field[] fields = objClass.getDeclaredFields();
        for (Field field : fields) {
            Type genericType = getListGenericType(field);
            if (genericType == genericClass) {
                genericTypeFieldCache.put(key, field);
                return field;
            }
        }
        return null;
    }

    /**
     * 返回集合字段的泛型类型
     * 如：List&lt;User&gt; list;返回User.class
     *
     * @param field 类中的一个属性
     * @return 返回类型
     */
    public static Type getListGenericType(Field field) {
        if (isListType(field.getType())) {
            Type genericType = field.getGenericType();

            if (genericType instanceof ParameterizedType) {
                Type[] params = ((ParameterizedType) genericType).getActualTypeArguments();
                if (params.length == 1) {
                    return params[0];
                }
            }
        }
        return Object.class;
    }

    public static boolean isListType(Type type) {
        return type == List.class;
    }

    /**
     * 返回set方法名 name - setName
     *
     * @param fieldName 属性名称
     * @return 返回方法名
     */
    public static String getSetMethodName(String fieldName) {
        return ReflectUtils.SETTER_PREFIX + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    /**
     * 返回定义类时的泛型参数的类型. <br>
     * 如:定义一个BookManager类<br>
     * <code>{@literal public BookManager extends GenricManager<Book,Address>}{...} </code>
     * <br>
     * 调用getSuperClassGenricType(getClass(),0)将返回Book的Class类型<br>
     * 调用getSuperClassGenricType(getClass(),1)将返回Address的Class类型
     *
     * @param clazz 从哪个类中获取
     * @param index 泛型参数索引,从0开始
     * @return 返回泛型参数类型
     */
    public static Class<?> getSuperClassGenricType(Class<?> clazz, int index) throws IndexOutOfBoundsException {
        String cacheKey = clazz.getName() + index;
        Class<?> cachedClass = classGenricTypeCache.get(cacheKey);
        if (cachedClass != null) {
            return cachedClass;
        }

        Type genType = clazz.getGenericSuperclass();

        // 没有泛型参数
        if (!(genType instanceof ParameterizedType)) {
            throw new RuntimeException("class " + clazz.getName() + " 没有指定父类泛型");
        } else {
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

            if (index >= params.length || index < 0) {
                throw new RuntimeException("泛型索引不正确，index:" + index);
            }
            if (!(params[index] instanceof Class)) {
                throw new RuntimeException(params[index] + "不是Class类型");
            }

            Class<?> retClass = (Class<?>) params[index];
            // 缓存起来
            classGenricTypeCache.put(cacheKey, retClass);

            return retClass;
        }
    }

    /**
     * 找到所有ApiService的类名
     *
     * @param ctx ApplicationContext
     * @return 返回类名称数组
     */
    public static String[] findApiServiceNames(ApplicationContext ctx) {
        return findBeanNamesByAnnotationClass(ctx, Service.class);
    }

    /**
     * 找到所有被注解标记的类名
     *
     * @param ctx             ApplicationContext
     * @param annotationClass 注解class
     * @return 返回类名称数组，没有返回空数组
     */
    public static String[] findBeanNamesByAnnotationClass(ApplicationContext ctx, Class<? extends Annotation> annotationClass) {
        String[] beans = ctx.getBeanNamesForAnnotation(annotationClass);
        // 如果没找到，去父容器找
        if (beans == null || beans.length == 0) {
            ApplicationContext parentCtx = ctx.getParent();
            if (parentCtx != null) {
                beans = parentCtx.getBeanNamesForAnnotation(annotationClass);
            }
        }
        if (beans == null) {
            beans = EMPTY_STRING_ARRAY;
        }
        return beans;
    }

}
