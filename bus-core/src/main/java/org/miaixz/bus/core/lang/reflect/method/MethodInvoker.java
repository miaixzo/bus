/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.lang.reflect.method;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.reflect.Invoker;
import org.miaixz.bus.core.xyz.*;

/**
 * 方法调用器，通过反射调用方法。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MethodInvoker implements Invoker {

    private final Method method;
    private final Class<?>[] paramTypes;
    private final Class<?> type;
    private boolean checkArgs;

    /**
     * 构造
     *
     * @param method 方法
     */
    public MethodInvoker(final Method method) {
        this.method = method;

        this.paramTypes = method.getParameterTypes();
        if (paramTypes.length == 1) {
            // setter方法读取参数类型
            type = paramTypes[0];
        } else {
            type = method.getReturnType();
        }
    }

    /**
     * 创建方法调用器
     *
     * @param method 方法
     * @return 方法调用器
     */
    public static MethodInvoker of(final Method method) {
        return new MethodInvoker(method);
    }

    /**
     * 执行方法句柄，{@link MethodHandle#invokeWithArguments(Object...)}包装
     * 非static方法需先调用{@link MethodHandle#bindTo(Object)}绑定执行对象。
     *
     * <p>
     * 需要注意的是，此处没有使用{@link MethodHandle#invoke(Object...)}，因为其参数第一个必须为对象或类。
     * {@link MethodHandle#invokeWithArguments(Object...)}只需传参数即可。
     * </p>
     *
     * @param methodHandle {@link java.lang.invoke.MethodHandle}
     * @param args         方法参数值，支持子类转换和自动拆装箱
     * @param <T>          返回值类型
     * @return 方法返回值
     */
    public static <T> T invokeHandle(final MethodHandle methodHandle, final Object... args) {
        try {
            return (T) methodHandle.invokeWithArguments(args);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

    /**
     * 执行接口或对象中的方法
     *
     * <pre class="code">
     * interface Duck {
     *     default String quack() {
     *         return "Quack";
     *     }
     * }
     * 
     * Duck duck = (Duck) Proxy.newProxyInstance(ClassKit.getClassLoader(), new Class[] { Duck.class },
     *         MethodInvoker::invoke);
     * </pre>
     *
     * @param <T>    返回结果类型
     * @param obj    接口的子对象或代理对象
     * @param method 方法
     * @param args   参数，自动根据{@link Method}定义类型转换
     * @return 结果
     * @throws InternalException 执行异常包装
     */
    public static <T> T invoke(final Object obj, final Method method, final Object... args) throws InternalException {
        Assert.notNull(method, "Method must be not null!");
        return invokeExact(obj, method, MethodKit.actualArgs(method, args));
    }

    /**
     * 执行接口或对象中的方法，参数类型不做转换，必须与方法参数类型完全匹配
     *
     * <pre class="code">
     * interface Duck {
     *     default String quack() {
     *         return "Quack";
     *     }
     * }
     * 
     * Duck duck = (Duck) Proxy.newProxyInstance(MethodInvoker.getClassLoader(), new Class[] { Duck.class },
     *         MethodInvoker::invoke);
     * </pre>
     *
     * @param <T>    返回结果类型
     * @param obj    接口的子对象或代理对象
     * @param method 方法
     * @param args   参数
     * @return 结果
     * @throws InternalException 执行异常包装
     */
    public static <T> T invokeExact(final Object obj, final Method method, final Object... args)
            throws InternalException {
        Assert.notNull(method, "Method must be not null!");
        java.lang.invoke.MethodHandle handle;
        try {
            handle = LookupKit.unreflectMethod(method);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }

        if (null != obj) {
            handle = handle.bindTo(obj);
        }
        return invokeHandle(handle, args);
    }

    /**
     * 设置是否检查参数
     * 
     * <pre>
     * 1. 参数个数是否与方法参数个数一致
     * 2. 如果某个参数为null但是方法这个位置的参数为原始类型，则赋予原始类型默认值
     * </pre>
     *
     * @param checkArgs 是否检查参数
     * @return this
     */
    public MethodInvoker setCheckArgs(final boolean checkArgs) {
        this.checkArgs = checkArgs;
        return this;
    }

    @Override
    public <T> T invoke(Object target, final Object... args) throws InternalException {
        if (this.checkArgs) {
            checkArgs(args);
        }

        final Method method = this.method;
        // static方法调用则target为null
        if (ModifierKit.isStatic(method)) {
            target = null;
        }
        // 根据方法定义的参数类型，将用户传入的参数规整和转换
        final Object[] actualArgs = MethodKit.actualArgs(method, args);
        try {
            return invokeExact(target, method, actualArgs);
        } catch (final Exception e) {
            // 传统反射方式执行方法
            try {
                return (T) method.invoke(target, actualArgs);
            } catch (final IllegalAccessException | InvocationTargetException ex) {
                throw new InternalException(ex);
            }
        }
    }

    /**
     * 执行静态方法
     *
     * @param <T>  对象类型
     * @param args 参数对象
     * @return 结果
     * @throws InternalException 多种异常包装
     */
    public <T> T invokeStatic(final Object... args) throws InternalException {
        return invoke(null, args);
    }

    @Override
    public Class<?> getType() {
        return this.type;
    }

    /**
     * 检查传入参数的有效性。
     *
     * @param args 传入的参数数组，不能为空。
     * @throws IllegalArgumentException 如果参数数组为空或长度为0，则抛出此异常。
     */
    private void checkArgs(final Object[] args) {
        final Class<?>[] paramTypes = this.paramTypes;
        if (null != args) {
            Assert.isTrue(args.length == paramTypes.length,
                    "Params length [{}] is not fit for param length [{}] of method !", args.length, paramTypes.length);
            Class<?> type;
            for (int i = 0; i < args.length; i++) {
                type = paramTypes[i];
                if (type.isPrimitive() && null == args[i]) {
                    // 参数是原始类型，而传入参数为null时赋予默认值
                    args[i] = ClassKit.getDefaultValue(type);
                }
            }
        }
    }

}
