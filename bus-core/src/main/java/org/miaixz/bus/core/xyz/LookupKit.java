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
package org.miaixz.bus.core.xyz;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.reflect.lookup.ConstructorLookupFactory;
import org.miaixz.bus.core.lang.reflect.lookup.LookupFactory;
import org.miaixz.bus.core.lang.reflect.lookup.MethodLookupFactory;

/**
 * {@link MethodHandles.Lookup}工具 {@link MethodHandles.Lookup}是一个方法句柄查找对象，用于在指定类中查找符合给定方法名称、方法类型的方法句柄。
 *
 * <p>
 * jdk8中如果直接调用{@link MethodHandles#lookup()}获取到的{@link MethodHandles.Lookup}在调用findSpecial和unreflectSpecial
 * 时会出现权限不够问题，抛出"no private access for invokespecial"异常，因此针对JDK8及JDK9+分别封装lookup方法。
 * 参考：https://blog.csdn.net/u013202238/article/details/108687086
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LookupKit {

    private static final LookupFactory factory;

    static {
        if (Keys.IS_JDK8) {
            factory = new ConstructorLookupFactory();
        } else {
            factory = new MethodLookupFactory();
        }
    }

    /**
     * jdk8中如果直接调用{@link MethodHandles#lookup()}获取到的{@link MethodHandles.Lookup}在调用findSpecial和unreflectSpecial
     * 时会出现权限不够问题，抛出"no private access for invokespecial"异常，因此针对JDK8及JDK9+分别封装lookup方法。
     *
     * @return {@link MethodHandles.Lookup}
     */
    public static MethodHandles.Lookup lookup() {
        return lookup(CallerKit.getCaller());
    }

    /**
     * jdk8中如果直接调用{@link MethodHandles#lookup()}获取到的{@link MethodHandles.Lookup}在调用findSpecial和unreflectSpecial
     * 时会出现权限不够问题，抛出"no private access for invokespecial"异常，因此针对JDK8及JDK9+分别封装lookup方法。
     *
     * @param callerClass 被调用的类或接口
     * @return {@link MethodHandles.Lookup}
     */
    public static MethodHandles.Lookup lookup(final Class<?> callerClass) {
        return factory.lookup(callerClass);
    }

    /**
     * 将{@link Method}或者{@link Constructor} 包装为方法句柄{@link MethodHandle}
     *
     * @param methodOrConstructor {@link Method}或者{@link Constructor}
     * @return 方法句柄{@link MethodHandle}
     * @throws InternalException {@link IllegalAccessException} 包装
     */
    public static MethodHandle unreflect(final Member methodOrConstructor) throws InternalException {
        try {
            if (methodOrConstructor instanceof Method) {
                return unreflectMethod((Method) methodOrConstructor);
            } else {
                return lookup().unreflectConstructor((Constructor<?>) methodOrConstructor);
            }
        } catch (final IllegalAccessException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 将{@link Method} 转换为方法句柄{@link MethodHandle}
     *
     * @param method {@link Method}
     * @return {@link MethodHandles}
     * @throws IllegalAccessException 无权访问
     */
    public static MethodHandle unreflectMethod(final Method method) throws IllegalAccessException {
        final Class<?> caller = method.getDeclaringClass();
        final MethodHandles.Lookup lookup = lookup(caller);
        if (ModifierKit.isDefault(method)) {
            // 当方法是default方法时，尤其对象是代理对象，需使用句柄方式执行
            // 代理对象情况下调用method.invoke会导致循环引用执行，最终栈溢出
            return lookup.unreflectSpecial(method, caller);
        }

        try {
            return lookup.unreflect(method);
        } catch (final Exception ignore) {
            // 某些情况下，无权限执行方法则尝试执行特殊方法
            return lookup.unreflectSpecial(method, caller);
        }
    }

    /**
     * 查找指定方法的方法句柄 此方法只会查找：
     * <ul>
     * <li>当前类的方法（包括构造方法和private方法）</li>
     * <li>父类的方法（包括构造方法和private方法）</li>
     * <li>当前类的static方法</li>
     * </ul>
     *
     * @param callerClass 方法所在类或接口
     * @param name        方法名称，{@code null}或者空则查找构造方法
     * @param returnType  返回值类型
     * @param argTypes    返回类型和参数类型列表
     * @return 方法句柄 {@link MethodHandle}，{@code null}表示未找到方法
     */
    public static MethodHandle findMethod(final Class<?> callerClass, final String name, final Class<?> returnType,
            final Class<?>... argTypes) {
        return findMethod(callerClass, name, MethodType.methodType(returnType, argTypes));
    }

    /**
     * 查找指定方法的方法句柄 此方法只会查找：
     * <ul>
     * <li>当前类的方法（包括构造方法和private方法）</li>
     * <li>父类的方法（包括构造方法和private方法）</li>
     * <li>当前类的static方法</li>
     * </ul>
     *
     * @param callerClass 方法所在类或接口
     * @param name        方法名称，{@code null}或者空则查找构造方法
     * @param type        返回类型和参数类型，可以使用{@code MethodType#methodType}构建
     * @return 方法句柄 {@link MethodHandle}，{@code null}表示未找到方法
     */
    public static MethodHandle findMethod(final Class<?> callerClass, final String name, final MethodType type) {
        if (StringKit.isBlank(name)) {
            return findConstructor(callerClass, type);
        }

        MethodHandle handle = null;
        final MethodHandles.Lookup lookup = LookupKit.lookup(callerClass);
        // 成员方法
        try {
            handle = lookup.findVirtual(callerClass, name, type);
        } catch (final IllegalAccessException | NoSuchMethodException ignore) {
            // ignore
        }

        // static方法
        if (null == handle) {
            try {
                handle = lookup.findStatic(callerClass, name, type);
            } catch (final IllegalAccessException | NoSuchMethodException ignore) {
                // ignore
            }
        }

        // 特殊方法，包括构造方法、私有方法等
        if (null == handle) {
            try {
                handle = lookup.findSpecial(callerClass, name, type, callerClass);
            } catch (final NoSuchMethodException ignore) {
                // ignore
            } catch (final IllegalAccessException e) {
                throw new InternalException(e);
            }
        }

        return handle;
    }

    /**
     * 查找指定的构造方法
     *
     * @param callerClass 类
     * @param argTypes    参数类型列表
     * @return 构造方法句柄
     */
    public static MethodHandle findConstructor(final Class<?> callerClass, final Class<?>... argTypes) {
        final Constructor<?> constructor = ReflectKit.getConstructor(callerClass, argTypes);
        if (null != constructor) {
            return LookupKit.unreflect(constructor);
        }
        return null;
    }

    /**
     * 查找指定的构造方法，给定的参数类型必须完全匹配，不能有拆装箱或继承关系等/
     *
     * @param callerClass 类
     * @param argTypes    参数类型列表，完全匹配
     * @return 构造方法句柄
     */
    public static MethodHandle findConstructorExact(final Class<?> callerClass, final Class<?>... argTypes) {
        return findConstructor(callerClass, MethodType.methodType(void.class, argTypes));
    }

    /**
     * 查找指定的构造方法
     *
     * @param callerClass 类
     * @param type        参数类型，此处返回类型应为void.class
     * @return 构造方法句柄
     */
    public static MethodHandle findConstructor(final Class<?> callerClass, final MethodType type) {
        final MethodHandles.Lookup lookup = lookup(callerClass);
        try {
            return lookup.findConstructor(callerClass, type);
        } catch (final NoSuchMethodException e) {
            return null;
        } catch (final IllegalAccessException e) {
            throw new InternalException(e);
        }
    }

}
