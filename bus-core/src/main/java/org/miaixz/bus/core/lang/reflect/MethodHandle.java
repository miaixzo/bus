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
package org.miaixz.bus.core.lang.reflect;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.toolkit.ReflectKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * 方法句柄{@link java.lang.invoke.MethodHandle}封装工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MethodHandle {

    /**
     * jdk8中如果直接调用{@link MethodHandles#lookup()}获取到的{@link MethodHandles.Lookup}在调用findSpecial和unreflectSpecial
     * 时会出现权限不够问题，抛出"no private access for invokespecial"异常，因此针对JDK8及JDK9+分别封装lookup方法。
     *
     * @param callerClass 被调用的类或接口
     * @return {@link MethodHandles.Lookup}
     */
    public static MethodHandles.Lookup lookup(Class<?> callerClass) {
        return LookupFactory.lookup(callerClass);
    }

    /**
     * 查找指定方法的方法句柄
     * 此方法只会查找：
     * <ul>
     *     <li>当前类的方法（包括构造方法和private方法）</li>
     *     <li>父类的方法（包括构造方法和private方法）</li>
     *     <li>当前类的static方法</li>
     * </ul>
     *
     * @param callerClass 方法所在类或接口
     * @param name        方法名称，{@code null}或者空则查找构造方法
     * @param type        返回类型和参数类型
     * @return 方法句柄 {@link java.lang.invoke.MethodHandle}，{@code null}表示未找到方法
     */
    public static java.lang.invoke.MethodHandle findMethod(Class<?> callerClass, String name, MethodType type) {
        if (StringKit.isBlank(name)) {
            return findConstructor(callerClass, type);
        }

        java.lang.invoke.MethodHandle handle = null;
        final MethodHandles.Lookup lookup = lookup(callerClass);
        try {
            handle = lookup.findVirtual(callerClass, name, type);
        } catch (IllegalAccessException | NoSuchMethodException ignore) {
            //ignore
        }

        // static方法
        if (null == handle) {
            try {
                handle = lookup.findStatic(callerClass, name, type);
            } catch (IllegalAccessException | NoSuchMethodException ignore) {
                //ignore
            }
        }

        // 特殊方法，包括构造方法、私有方法等
        if (null == handle) {
            try {
                handle = lookup.findSpecial(callerClass, name, type, callerClass);
            } catch (NoSuchMethodException ignore) {
                //ignore
            } catch (IllegalAccessException e) {
                throw new InternalException(e);
            }
        }

        return handle;
    }

    /**
     * 查找指定的构造方法
     *
     * @param callerClass 类
     * @param args        参数
     * @return 构造方法句柄
     */
    public static java.lang.invoke.MethodHandle findConstructor(Class<?> callerClass, Class<?>... args) {
        return findConstructor(callerClass, MethodType.methodType(void.class, args));
    }

    /**
     * 查找指定的构造方法
     *
     * @param callerClass 类
     * @param type        参数类型，此处返回类型应为void.class
     * @return 构造方法句柄
     */
    public static java.lang.invoke.MethodHandle findConstructor(Class<?> callerClass, MethodType type) {
        final MethodHandles.Lookup lookup = lookup(callerClass);
        try {
            return lookup.findConstructor(callerClass, type);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (IllegalAccessException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 执行接口或对象中的特殊方法（private、static等）
     *
     * <pre class="code">
     *     interface Duck {
     *         default String quack() {
     *             return "Quack";
     *         }
     *     }
     *
     *     Duck duck = (Duck) Proxy.newProxyInstance(
     *         ClassKit.getClassLoader(),
     *         new Class[] { Duck.class },
     *         MethodHandle::invokeDefault);
     * </pre>
     *
     * @param <T>        返回结果类型
     * @param object     接口的子对象或代理对象
     * @param methodName 方法名称
     * @param args       参数
     * @return 结果
     */
    public static <T> T invokeSpecial(Object object, String methodName, Object... args) {
        Assert.notNull(object, "Object to get method must be not null!");
        Assert.notBlank(methodName, "Method name must be not blank!");

        final Method method = ReflectKit.getMethodOfObject(object, methodName, args);
        if (null == method) {
            throw new InternalException("No such method: [{}] from [{}]", methodName, object.getClass());
        }
        return invokeSpecial(object, method, args);
    }

    /**
     * 执行接口或对象中的方法
     *
     * @param <T>    返回结果类型
     * @param object 接口的子对象或代理对象
     * @param method 方法
     * @param args   参数
     * @return 结果
     */
    public static <T> T invoke(Object object, Method method, Object... args) {
        return invoke(false, object, method, args);
    }

    /**
     * 执行接口或对象中的特殊方法（private、static等）
     *
     * <pre class="code">
     *     interface Duck {
     *         default String quack() {
     *             return "Quack";
     *         }
     *     }
     *
     *     Duck duck = (Duck) Proxy.newProxyInstance(
     *         ClassKit.getClassLoader(),
     *         new Class[] { Duck.class },
     *         MethodHandle::invoke);
     * </pre>
     *
     * @param <T>    返回结果类型
     * @param object 接口的子对象或代理对象
     * @param method 方法
     * @param args   参数
     * @return 结果
     */
    public static <T> T invokeSpecial(Object object, Method method, Object... args) {
        return invoke(true, object, method, args);
    }

    /**
     * 执行接口或对象中的方法
     *
     * <pre class="code">
     *     interface Duck {
     *         default String quack() {
     *             return "Quack";
     *         }
     *     }
     *
     *     Duck duck = (Duck) Proxy.newProxyInstance(
     *         ClassKit.getClassLoader(),
     *         new Class[] { Duck.class },
     *         MethodHandle::invoke);
     * </pre>
     *
     * @param <T>       返回结果类型
     * @param isSpecial 是否为特殊方法（private、static等）
     * @param object    接口的子对象或代理对象
     * @param method    方法
     * @param args      参数
     * @return 结果
     */
    public static <T> T invoke(boolean isSpecial, Object object, Method method, Object... args) {
        Assert.notNull(method, "Method must be not null!");
        final Class<?> declaringClass = method.getDeclaringClass();
        final MethodHandles.Lookup lookup = lookup(declaringClass);
        try {
            java.lang.invoke.MethodHandle handle = isSpecial ? lookup.unreflectSpecial(method, declaringClass)
                    : lookup.unreflect(method);
            if (null != object) {
                handle = handle.bindTo(object);
            }
            return (T) handle.invokeWithArguments(args);
        } catch (Throwable e) {
            throw new InternalException(e);
        }
    }

}
