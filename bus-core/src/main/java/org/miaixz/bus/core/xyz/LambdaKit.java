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

import org.miaixz.bus.core.center.function.LambdaFactory;
import org.miaixz.bus.core.center.function.LambdaX;
import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.InternalException;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.*;

/**
 * Lambda相关工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LambdaKit {

    private static final WeakConcurrentMap<Object, LambdaX> CACHE = new WeakConcurrentMap<>();

    /**
     * 通过对象的方法或类的静态方法引用，获取lambda实现类
     * 传入lambda无参数但含有返回值的情况能够匹配到此方法：
     * <ul>
     * 		<li>引用特定对象的实例方法：<pre>{@code
     * 			MyTeacher myTeacher = new MyTeacher();
     * 			Class<MyTeacher> supplierClass = LambdaKit.getRealClass(myTeacher::getAge);
     * 			Assert.assertEquals(MyTeacher.class, supplierClass);
     *            }</pre>
     * 		</li>
     * 		<li>引用静态无参方法：<pre>{@code
     * 			Class<MyTeacher> staticSupplierClass = LambdaKit.getRealClass(MyTeacher::takeAge);
     * 			Assert.assertEquals(MyTeacher.class, staticSupplierClass);
     *            }</pre>
     * 		</li>
     * </ul>
     * 在以下场景无法获取到正确类型
     * <pre>{@code
     * 		// 枚举测试，只能获取到枚举类型
     * 		Class<Enum<?>> enumSupplierClass = LambdaKit.getRealClass(LambdaKit.LambdaKindEnum.REF_NONE::ordinal);
     * 		Assert.assertEquals(Enum.class, enumSupplierClass);
     * 		// 调用父类方法，只能获取到父类类型
     * 		Class<Entity<?>> superSupplierClass = LambdaKit.getRealClass(myTeacher::getId);
     * 		Assert.assertEquals(Entity.class, superSupplierClass);
     * 		// 引用父类静态带参方法，只能获取到父类类型
     * 		Class<Entity<?>> staticSuperFunctionClass = LambdaKit.getRealClass(MyTeacher::takeId);
     * 		Assert.assertEquals(Entity.class, staticSuperFunctionClass);
     * }</pre>
     *
     * @param func lambda
     * @param <R>  类型
     * @param <T>  lambda的类型
     * @return lambda实现类
     */
    public static <R, T extends Serializable> Class<R> getRealClass(final T func) {
        final LambdaX lambdaX = resolve(func);
        return (Class<R>) Optional.of(lambdaX)
                .map(LambdaX::getInstantiatedMethodParameterTypes)
                .filter(types -> types.length != 0).map(types -> types[types.length - 1])
                .orElseGet(lambdaX::getClazz);
    }

    /**
     * 解析lambda表达式,加了缓存。
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象（无参方法）
     * @param <T>  lambda的类型
     * @return 返回解析后的结果
     */
    public static <T extends Serializable> LambdaX resolve(final T func) {
        return CACHE.computeIfAbsent(func, (key) -> {
            final SerializedLambda serializedLambda = _resolve(func);
            final String methodName = serializedLambda.getImplMethodName();
            final Class<?> implClass = ClassKit.loadClass(serializedLambda.getImplClass(), true);
            if ("<init>".equals(methodName)) {
                for (final Constructor<?> constructor : implClass.getDeclaredConstructors()) {
                    if (ReflectKit.getDesc(constructor, false).equals(serializedLambda.getImplMethodSignature())) {
                        return new LambdaX(constructor, serializedLambda);
                    }
                }
            } else {
                final Method[] methods = MethodKit.getMethods(implClass);
                for (final Method method : methods) {
                    if (method.getName().equals(methodName)
                            && ReflectKit.getDesc(method, false).equals(serializedLambda.getImplMethodSignature())) {
                        return new LambdaX(method, serializedLambda);
                    }
                }
            }
            throw new IllegalStateException("No lambda method found.");
        });
    }

    /**
     * 获取lambda表达式函数（方法）名称
     *
     * @param func 函数（无参方法）
     * @param <T>  lambda的类型
     * @return 函数名称
     */
    public static <T extends Serializable> String getMethodName(final T func) {
        return resolve(func).getName();
    }

    /**
     * 获取lambda表达式Getter或Setter函数（方法）对应的字段名称，规则如下：
     * <ul>
     *     <li>getXxxx获取为xxxx，如getName得到name。</li>
     *     <li>setXxxx获取为xxxx，如setName得到name。</li>
     *     <li>isXxxx获取为xxxx，如isName得到name。</li>
     *     <li>其它不满足规则的方法名抛出{@link IllegalArgumentException}</li>
     * </ul>
     *
     * @param func 函数
     * @param <T>  lambda的类型
     * @return 方法名称
     * @throws IllegalArgumentException 非Getter或Setter方法
     */
    public static <T extends Serializable> String getFieldName(final T func) throws IllegalArgumentException {
        return BeanKit.getFieldName(getMethodName(func));
    }

    /**
     * 等效于 Obj::getXxx
     *
     * @param getMethod getter方法
     * @param <T>       调用getter方法对象类型
     * @param <R>       getter方法返回值类型
     * @return Obj::getXxx
     */
    public static <T, R> Function<T, R> buildGetter(final Method getMethod) {
        return LambdaFactory.build(Function.class, getMethod);
    }

    /**
     * 等效于 Obj::getXxx
     *
     * @param clazz     调用getter方法对象类
     * @param fieldName 字段名称
     * @param <T>       调用getter方法对象类型
     * @param <R>       getter方法返回值类型
     * @return Obj::getXxx
     */
    public static <T, R> Function<T, R> buildGetter(final Class<T> clazz, final String fieldName) {
        return LambdaFactory.build(Function.class, BeanKit.getBeanDesc(clazz).getGetter(fieldName));
    }

    /**
     * 等效于 Obj::setXxx
     *
     * @param setMethod setter方法
     * @param <T>       调用setter方法对象类型
     * @param <P>       setter方法返回的值类型
     * @return Obj::setXxx
     */
    public static <T, P> BiConsumer<T, P> buildSetter(final Method setMethod) {
        final Class<?> returnType = setMethod.getReturnType();
        if (Void.TYPE == returnType) {
            return LambdaFactory.build(BiConsumer.class, setMethod);
        }

        // 返回this的setter
        final BiFunction<T, P, ?> biFunction = LambdaFactory.build(BiFunction.class, setMethod);
        return biFunction::apply;
    }

    /**
     * Obj::setXxx
     *
     * @param clazz     调用setter方法对象类
     * @param fieldName 字段名称
     * @param <T>       调用setter方法对象类型
     * @param <P>       setter方法返回的值类型
     * @return Obj::setXxx
     */
    public static <T, P> BiConsumer<T, P> buildSetter(final Class<T> clazz, final String fieldName) {
        return LambdaFactory.build(BiConsumer.class, BeanKit.getBeanDesc(clazz).getSetter(fieldName));
    }

    /**
     * 等效于 Obj::method
     *
     * @param lambdaType  接受lambda的函数式接口类型
     * @param clazz       调用类
     * @param methodName  方法名
     * @param paramsTypes 方法参数类型数组
     * @param <F>         函数式接口类型
     * @return Obj::method
     */
    public static <F> F build(final Class<F> lambdaType, final Class<?> clazz, final String methodName, final Class<?>... paramsTypes) {
        return LambdaFactory.build(lambdaType, clazz, methodName, paramsTypes);
    }

    /**
     * 通过自定义固定参数，将{@link BiFunction}转换为{@link Function}
     *
     * @param biFunction {@link BiFunction}
     * @param param      参数
     * @param <T>        参数类型
     * @param <U>        参数2类型
     * @param <R>        返回值类型
     * @return {@link Function}
     */
    public static <T, U, R> Function<T, R> toFunction(final BiFunction<T, U, R> biFunction, final U param) {
        return (t) -> biFunction.apply(t, param);
    }

    /**
     * 通过自定义固定参数，将{@link BiPredicate}转换为{@link Predicate}
     *
     * @param biPredicate {@link BiFunction}
     * @param param       参数
     * @param <T>         参数类型
     * @param <U>         参数2类型
     * @return {@link Predicate}
     */
    public static <T, U> Predicate<T> toPredicate(final BiPredicate<T, U> biPredicate, final U param) {
        return (t) -> biPredicate.test(t, param);
    }

    /**
     * 通过自定义固定参数，将{@link BiConsumer}转换为{@link Consumer}
     *
     * @param biConsumer {@link BiConsumer}
     * @param param      参数
     * @param <T>        参数类型
     * @param <U>        参数2类型
     * @return {@link Consumer}
     */
    public static <T, U> Consumer<T> toPredicate(final BiConsumer<T, U> biConsumer, final U param) {
        return (t) -> biConsumer.accept(t, param);
    }

    /**
     * 获取函数的执行方法
     *
     * @param funcType 函数接口类
     * @return {@link Method}
     */
    public static Method getInvokeMethod(final Class<?> funcType) {
        // 获取Lambda函数
        final Method[] abstractMethods = MethodKit.getPublicMethods(funcType, ModifierKit::isAbstract);
        Assert.equals(abstractMethods.length, 1, "Not a function class: " + funcType.getName());

        return abstractMethods[0];
    }

    /**
     * 解析lambda表达式,没加缓存
     *
     * <p>
     * 通过反射调用实现序列化接口函数对象的writeReplace方法，从而拿到{@link SerializedLambda}
     * 该对象中包含了lambda表达式的大部分信息。
     * </p>
     *
     * @param func 需要解析的 lambda 对象
     * @param <T>  lambda的类型
     * @return 返回解析后的结果
     */
    private static <T extends Serializable> SerializedLambda _resolve(final T func) {
        if (func instanceof SerializedLambda) {
            return (SerializedLambda) func;
        }
        if (func instanceof Proxy) {
            throw new IllegalArgumentException("not support proxy, just for now");
        }
        final Class<? extends Serializable> clazz = func.getClass();
        if (!clazz.isSynthetic()) {
            throw new IllegalArgumentException("Not a lambda expression: " + clazz.getName());
        }
        final Object serLambda = MethodKit.invoke(func, "writeReplace");
        if (serLambda instanceof SerializedLambda) {
            return (SerializedLambda) serLambda;
        }
        throw new InternalException("writeReplace result value is not java.lang.invoke.SerializedLambda");
    }

}
