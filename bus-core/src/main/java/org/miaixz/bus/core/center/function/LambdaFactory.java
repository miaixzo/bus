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
package org.miaixz.bus.core.center.function;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.mutable.MutableEntry;
import org.miaixz.bus.core.toolkit.*;

import java.lang.invoke.*;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 以类似反射的方式动态创建Lambda，在性能上有一定优势，同时避免每次调用Lambda时创建匿名内部类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LambdaFactory {

    private static final Map<MutableEntry<Class<?>, Executable>, Object> CACHE = new WeakConcurrentMap<>();

    private LambdaFactory() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    /**
     * 构建Lambda
     * <pre>{@code
     * class Something {
     *     private Long data;
     *     private String name;
     *     // ... 省略GetterSetter方法
     * }
     * Function<Something, Long> getIdFunction = LambdaFactory.buildLambda(Function.class, Something.class, "getId");
     * BiConsumer<Something, String> setNameConsumer = LambdaFactory.buildLambda(BiConsumer.class, Something.class, "setName", String.class);
     * }
     * </pre>
     *
     * @param functionInterfaceType 接受Lambda的函数式接口类型
     * @param methodClass           声明方法的类的类型
     * @param methodName            方法名称
     * @param paramTypes            方法参数数组
     * @param <F>                   Function类型
     * @return 接受Lambda的函数式接口对象
     */
    public static <F> F build(final Class<F> functionInterfaceType, final Class<?> methodClass, final String methodName, final Class<?>... paramTypes) {
        return build(functionInterfaceType, MethodKit.getMethod(methodClass, methodName, paramTypes));
    }

    /**
     * 根据提供的方法或构造对象，构建对应的Lambda函数
     * 调用函数相当于执行对应的方法或构造
     *
     * @param functionInterfaceType 接受Lambda的函数式接口类型
     * @param executable            方法对象，支持构造器
     * @param <F>                   Function类型
     * @return 接受Lambda的函数式接口对象
     */
    public static <F> F build(final Class<F> functionInterfaceType, final Executable executable) {
        Assert.notNull(functionInterfaceType);
        Assert.notNull(executable);

        final MutableEntry<Class<?>, Executable> cacheKey = new MutableEntry<>(functionInterfaceType, executable);
        return (F) CACHE.computeIfAbsent(cacheKey,
                key -> doBuildWithoutCache(functionInterfaceType, executable));
    }

    /**
     * 根据提供的方法或构造对象，构建对应的Lambda函数，即通过Lambda函数代理方法或构造
     * 调用函数相当于执行对应的方法或构造
     *
     * @param funcType   接受Lambda的函数式接口类型
     * @param executable 方法对象，支持构造器
     * @param <F>        Function类型
     * @return 接受Lambda的函数式接口对象
     */
    private static <F> F doBuildWithoutCache(final Class<F> funcType, final Executable executable) {
        ReflectKit.setAccessible(executable);

        // 获取Lambda函数
        final Method invokeMethod = LambdaKit.getInvokeMethod(funcType);
        try {
            return (F) metaFactory(funcType, invokeMethod, executable)
                    .getTarget().invoke();
        } catch (final Throwable e) {
            throw new InternalException(e);
        }
    }

    /**
     * 通过Lambda函数代理方法或构造
     *
     * @param funcType   函数类型
     * @param funcMethod 函数执行的方法
     * @param executable 被代理的方法或构造
     * @return {@link CallSite}
     * @throws LambdaConversionException 权限等异常
     */
    private static CallSite metaFactory(final Class<?> funcType, final Method funcMethod,
                                        final Executable executable) throws LambdaConversionException {
        // 查找上下文与调用者的访问权限
        final MethodHandles.Lookup caller = LookupKit.lookup(executable.getDeclaringClass());
        // 要实现的方法的名字
        final String invokeName = funcMethod.getName();
        // 调用点期望的方法参数的类型和返回值的类型(方法signature)
        final MethodType invokedType = MethodType.methodType(funcType);

        final Class<?>[] paramTypes = funcMethod.getParameterTypes();
        // 函数对象将要实现的接口方法类型
        final MethodType samMethodType = MethodType.methodType(funcMethod.getReturnType(), paramTypes);
        // 一个直接方法句柄(DirectMethodHandle), 描述调用时将被执行的具体实现方法
        final MethodHandle implMethodHandle = LookupKit.unreflect(executable);

        if (ClassKit.isSerializable(funcType)) {
            return LambdaMetafactory.altMetafactory(
                    caller,
                    invokeName,
                    invokedType,
                    samMethodType,
                    implMethodHandle,
                    MethodKit.methodType(executable),
                    LambdaMetafactory.FLAG_SERIALIZABLE
            );
        }

        return LambdaMetafactory.metafactory(
                caller,
                invokeName,
                invokedType,
                samMethodType,
                implMethodHandle,
                MethodKit.methodType(executable)
        );
    }

}
