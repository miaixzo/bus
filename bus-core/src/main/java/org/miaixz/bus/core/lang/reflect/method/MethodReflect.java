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
package org.miaixz.bus.core.lang.reflect.method;

import org.miaixz.bus.core.center.set.UniqueKeySet;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.ArrayKit;
import org.miaixz.bus.core.toolkit.ModifierKit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * 方法反射相关操作类。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MethodReflect {

    private final Class<?> clazz;
    private volatile Method[] publicMethods;
    private volatile Method[] declaredMethods;
    private volatile Method[] allMethods;

    /**
     * 构造
     *
     * @param clazz 类
     */
    public MethodReflect(final Class<?> clazz) {
        this.clazz = Assert.notNull(clazz);
    }

    /**
     * 获取反射对象
     *
     * @param clazz 类
     * @return MethodReflect
     */
    public static MethodReflect of(final Class<?> clazz) {
        return new MethodReflect(clazz);
    }

    /**
     * 获取方法的唯一键，结构为:
     * <pre>
     *     返回类型#方法名:参数1类型,参数2类型...
     * </pre>
     *
     * @param method 方法
     * @return 方法唯一键
     */
    private static String getUniqueKey(final Method method) {
        final StringBuilder sb = new StringBuilder();
        sb.append(method.getReturnType().getName()).append(Symbol.C_SHAPE);
        sb.append(method.getName());
        final Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            if (i == 0) {
                sb.append(Symbol.C_COLON);
            } else {
                sb.append(',');
            }
            sb.append(parameters[i].getName());
        }
        return sb.toString();
    }

    /**
     * 获取类对应接口中的非抽象方法（default方法）
     *
     * @param clazz 类
     * @return 方法列表
     */
    private static List<Method> getDefaultMethodsFromInterface(final Class<?> clazz) {
        final List<Method> result = new ArrayList<>();
        for (final Class<?> ifc : clazz.getInterfaces()) {
            for (final Method m : ifc.getMethods()) {
                if (!ModifierKit.isAbstract(m)) {
                    result.add(m);
                }
            }
        }
        return result;
    }

    /**
     * 获取当前类
     *
     * @return 当前类
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * 清空缓存
     */
    synchronized public void clearCaches() {
        publicMethods = null;
        declaredMethods = null;
        allMethods = null;
    }

    /**
     * 获取当前类及父类的所有公共方法，等同于{@link Class#getMethods()}
     *
     * @param predicate 方法过滤器，{@code null}表示无过滤
     * @return 当前类及父类的所有公共方法
     */
    public Method[] getPublicMethods(final Predicate<Method> predicate) {
        if (null == publicMethods) {
            synchronized (MethodReflect.class) {
                if (null == publicMethods) {
                    publicMethods = clazz.getMethods();
                }
            }
        }
        return ArrayKit.filter(publicMethods, predicate);
    }

    /**
     * 获取当前类直接声明的所有方法，等同于{@link Class#getDeclaredMethods()}
     *
     * @param predicate 方法过滤器，{@code null}表示无过滤
     * @return 当前类及父类的所有公共方法
     */
    public Method[] getDeclaredMethods(final Predicate<Method> predicate) {
        if (null == declaredMethods) {
            synchronized (MethodReflect.class) {
                if (null == declaredMethods) {
                    declaredMethods = clazz.getDeclaredMethods();
                }
            }
        }
        return ArrayKit.filter(declaredMethods, predicate);
    }

    /**
     * 获取当前类层级结构中的所有方法。
     * 等同于按广度优先遍历类及其所有父类与接口，并依次调用{@link Class#getDeclaredMethods()}。
     * 返回的方法排序规则如下：
     * <ul>
     *     <li>离{@code type}距离越近，则顺序越靠前；</li>
     *     <li>与{@code type}距离相同，直接实现的接口方法优先于父类方法；</li>
     *     <li>与{@code type}距离相同的接口，则顺序遵循接口在{@link Class#getInterfaces()}的顺序；</li>
     * </ul>
     *
     * @param predicate 方法过滤器，{@code null}表示无过滤
     * @return 当前类及父类的所有公共方法
     */
    public Method[] getAllMethods(final Predicate<Method> predicate) {
        if (null == allMethods) {
            synchronized (MethodReflect.class) {
                if (null == allMethods) {
                    allMethods = getMethodsDirectly(true, true);
                }
            }
        }
        return ArrayKit.filter(allMethods, predicate);
    }

    /**
     * 获得一个类中所有方法列表，直接反射获取，无缓存
     * 接口获取方法和默认方法，获取的方法包括：
     * <ul>
     *     <li>本类中的所有方法（包括static方法）</li>
     *     <li>父类中的所有方法（包括static方法）</li>
     *     <li>Object中（包括static方法）</li>
     * </ul>
     *
     * @param withSupers           是否包括父类或接口的方法列表
     * @param withMethodFromObject 是否包括Object中的方法
     * @return 方法列表
     * @throws SecurityException 安全检查异常
     */
    public Method[] getMethodsDirectly(final boolean withSupers, final boolean withMethodFromObject) throws SecurityException {
        final Class<?> clazz = this.clazz;

        if (clazz.isInterface()) {
            // 对于接口，直接调用Class.getMethods方法获取所有方法，因为接口都是public方法
            return withSupers ? clazz.getMethods() : clazz.getDeclaredMethods();
        }

        final UniqueKeySet<String, Method> result = new UniqueKeySet<>(true, MethodReflect::getUniqueKey);
        Class<?> searchType = clazz;
        while (searchType != null) {
            if (!withMethodFromObject && Object.class == searchType) {
                break;
            }
            // 本类所有方法
            result.addAllIfAbsent(Arrays.asList(searchType.getDeclaredMethods()));
            // 实现接口的所有默认方法
            result.addAllIfAbsent(getDefaultMethodsFromInterface(searchType));


            searchType = (withSupers && !searchType.isInterface()) ? searchType.getSuperclass() : null;
        }

        return result.toArray(new Method[0]);
    }

}
