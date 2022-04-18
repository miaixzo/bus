/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.toolkit;

import org.aoju.bus.core.lang.function.Func0;
import org.aoju.bus.core.lang.function.Func1;
import org.aoju.bus.core.map.WeakMap;

import java.io.Serializable;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;

/**
 * Lambda相关工具类
 *
 * @author Kimi Liu
 * @version 6.5.0
 * @since Java 17+
 */
public class LambdaKit {

    private static final WeakMap<String, SerializedLambda> cache = new WeakMap<>();

    /**
     * 解析lambda表达式,加了缓存
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param <T>  Lambda类型
     * @param func 需要解析的 lambda 对象（无参方法）
     * @return 返回解析后的结果
     */
    public static <T> SerializedLambda resolve(Func1<T, ?> func) {
        return _resolve(func);
    }

    /**
     * 解析lambda表达式,加了缓存
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param <R>  Lambda返回类型
     * @param func 需要解析的 lambda 对象（无参方法）
     * @return 返回解析后的结果
     */
    public static <R> SerializedLambda resolve(Func0<R> func) {
        return _resolve(func);
    }

    /**
     * 获取lambda表达式函数（方法）名称
     *
     * @param <P>  Lambda参数类型
     * @param func 函数（无参方法）
     * @return 函数名称
     */
    public static <P> String getMethodName(Func1<P, ?> func) {
        return resolve(func).getImplMethodName();
    }

    /**
     * 获取lambda表达式函数（方法）名称
     *
     * @param <R>  Lambda返回类型
     * @param func 函数（无参方法）
     * @return 函数名称
     */
    public static <R> String getMethodName(Func0<R> func) {
        return resolve(func).getImplMethodName();
    }

    /**
     * 通过对象的方法或类的静态方法引用，获取lambda实现类
     *
     * @param func lambda
     * @param <R>  类型
     * @return lambda实现类
     * @throws IllegalArgumentException 如果是不支持的方法引用，抛出该异常，见{@link LambdaKit#checkLambdaTypeCanGetClass}
     */
    public static <R> Class<R> getRealClass(Func0<?> func) {
        final SerializedLambda lambda = resolve(func);
        checkLambdaTypeCanGetClass(lambda.getImplMethodKind());
        return ClassKit.loadClass(lambda.getImplClass());
    }

    /**
     * 通过对象的方法或类的静态方法引用，然后根据{@link SerializedLambda#getInstantiatedMethodType()}获取lambda实现类
     * 传入lambda有参数且含有返回值的情况能够匹配到此方法：
     *
     * @param func lambda
     * @param <P>  方法调用方类型
     * @param <R>  返回值类型
     * @return lambda实现类
     * @throws IllegalArgumentException 如果是不支持的方法引用，抛出该异常，见{@link LambdaKit#checkLambdaTypeCanGetClass}
     */
    public static <P, R> Class<P> getRealClass(Func1<P, R> func) {
        final SerializedLambda lambda = resolve(func);
        checkLambdaTypeCanGetClass(lambda.getImplMethodKind());
        final String instantiatedMethodType = lambda.getInstantiatedMethodType();
        return ClassKit.loadClass(StringKit.sub(instantiatedMethodType, 2, StringKit.indexOf(instantiatedMethodType, ';')));
    }

    /**
     * 获取lambda表达式Getter或Setter函数（方法）对应的字段名称，规则如下：
     * <ul>
     *     <li>getXxxx获取为xxxx，如getName得到name</li>
     *     <li>setXxxx获取为xxxx，如setName得到name</li>
     *     <li>isXxxx获取为xxxx，如isName得到name</li>
     *     <li>其它不满足规则的方法名抛出{@link IllegalArgumentException}</li>
     * </ul>
     *
     * @param <T>  Lambda类型
     * @param func 函数（无参方法）
     * @return 方法名称
     * @throws IllegalArgumentException 非Getter或Setter方法
     */
    public static <T> String getFieldName(Func1<T, ?> func) throws IllegalArgumentException {
        return BeanKit.getFieldName(getMethodName(func));
    }

    /**
     * 获取lambda表达式Getter或Setter函数（方法）对应的字段名称，规则如下：
     * <ul>
     *     <li>getXxxx获取为xxxx，如getName得到name</li>
     *     <li>setXxxx获取为xxxx，如setName得到name</li>
     *     <li>isXxxx获取为xxxx，如isName得到name</li>
     *     <li>其它不满足规则的方法名抛出{@link IllegalArgumentException}</li>
     * </ul>
     *
     * @param <T>  Lambda类型
     * @param func 函数（无参方法）
     * @return 方法名称
     * @throws IllegalArgumentException 非Getter或Setter方法
     */
    public static <T> String getFieldName(Func0<T> func) throws IllegalArgumentException {
        return BeanKit.getFieldName(getMethodName(func));
    }

    /**
     * 检查是否为支持的类型
     *
     * @param implMethodKind 支持的lambda类型
     * @throws IllegalArgumentException 如果是不支持的方法引用，抛出该异常
     */
    private static void checkLambdaTypeCanGetClass(int implMethodKind) {
        if (implMethodKind != MethodHandleInfo.REF_invokeVirtual &&
                implMethodKind != MethodHandleInfo.REF_invokeStatic) {
            throw new IllegalArgumentException("该lambda不是合适的方法引用");
        }
    }

    /**
     * 解析lambda表达式,加了缓存
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象
     * @return 返回解析后的结果
     */
    private static SerializedLambda _resolve(Serializable func) {
        return cache.computeIfAbsent(func.getClass().getName(), () -> ReflectKit.invoke(func, "writeReplace"));
    }

}
