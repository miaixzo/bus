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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.http.accord.platform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 方法的Duck-typing:表示对象上可能存在也可能不存在的方法
 *
 * @param <T> 方法所在的对象类型，通常是接口或基类
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public class OptionalMethod<T> {

    /**
     * 方法的返回类型
     */
    private final Class<?> returnType;

    /**
     * 方法的名称
     */
    private final String methodName;

    /**
     * 方法参数类型
     */
    private final Class[] methodParams;

    /**
     * 创建一个可选的方法.
     *
     * @param returnType   返回类型为required，如果不重要，则为null
     * @param methodName   方法的名称
     * @param methodParams 方法参数类型
     */
    OptionalMethod(Class<?> returnType, String methodName, Class... methodParams) {
        this.returnType = returnType;
        this.methodName = methodName;
        this.methodParams = methodParams;
    }

    private static Method getPublicMethod(Class<?> clazz, String methodName, Class[] parameterTypes) {
        Method method = null;
        try {
            method = clazz.getMethod(methodName, parameterTypes);
            if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                method = null;
            }
        } catch (NoSuchMethodException e) {
            // None.
        }
        return method;
    }

    /**
     * 如果方法存在于提供的{@code target}上，则返回true
     *
     * @param target 对象
     * @return the true/false
     */
    public boolean isSupported(T target) {
        return getMethod(target.getClass()) != null;
    }

    /**
     * 使用{@code args}调用{@code target}上的方法。如果该方法不存在或不是公共的，
     * 则返回{@code null}。参见{@link #invokeOptionalWithoutCheckedException}
     *
     * @param target 对象
     * @param args   参数信息
     * @return the object
     * @throws InvocationTargetException 如果调用引发异常
     */
    public Object invokeOptional(T target, Object... args) throws InvocationTargetException {
        Method m = getMethod(target.getClass());
        if (m == null) {
            return null;
        }
        try {
            return m.invoke(target, args);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * 调用{@code target}上的方法。如果该方法不存在或不是公共的，
     * 则返回{@code null}。方法抛出的任何RuntimeException都会被抛出，
     * 已检查的异常被包装在{@link AssertionError}中
     *
     * @param target 对象
     * @param args   参数信息
     * @return the object
     */
    public Object invokeOptionalWithoutCheckedException(T target, Object... args) {
        try {
            return invokeOptional(target, args);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            }
            AssertionError error = new AssertionError("Unexpected exception");
            error.initCause(targetException);
            throw error;
        }
    }

    /**
     * 使用{@code args}调用{@code target}上的方法。如果不支持该方法，则引发错误。
     * 参见{@link #invokeWithoutCheckedException(Object, Object...)}
     *
     * @param target 对象
     * @param args   参数信息
     * @return the object
     * @throws InvocationTargetException 如果调用引发异常
     */
    public Object invoke(T target, Object... args) throws InvocationTargetException {
        Method m = getMethod(target.getClass());
        if (m == null) {
            throw new AssertionError("Method " + methodName + " not supported for object " + target);
        }
        try {
            return m.invoke(target, args);
        } catch (IllegalAccessException e) {
            // Method should be public: we checked.
            AssertionError error = new AssertionError("Unexpectedly could not call: " + m);
            error.initCause(e);
            throw error;
        }
    }

    /**
     * 调用{@code target}上的方法。如果不支持该方法，则引发错误。
     * 方法抛出的任何RuntimeException都会被抛出，已检查的异常被包装在{@link AssertionError}中
     *
     * @param target 对象
     * @param args   参数信息
     * @return the object
     */
    public Object invokeWithoutCheckedException(T target, Object... args) {
        try {
            return invoke(target, args);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            }
            AssertionError error = new AssertionError("Unexpected exception");
            error.initCause(targetException);
            throw error;
        }
    }

    /**
     * 为该方法执行查找。没有缓存。为了返回一个方法，
     * 方法名和参数必须与创建{@link OptionalMethod}时指定的参数匹配。
     * 如果指定了返回类型(即非null)，那么它也必须是兼容的。该方法也必须是公共的
     *
     * @param clazz class
     * @return the method
     */
    private Method getMethod(Class<?> clazz) {
        Method method = null;
        if (methodName != null) {
            method = getPublicMethod(clazz, methodName, methodParams);
            if (method != null
                    && returnType != null
                    && !returnType.isAssignableFrom(method.getReturnType())) {

                // 如果返回类型是非空的，那么它必须是兼容的.
                method = null;
            }
        }
        return method;
    }

}

