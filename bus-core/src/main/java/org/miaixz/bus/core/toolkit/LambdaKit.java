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
package org.miaixz.bus.core.toolkit;

import lombok.Getter;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.function.LambdaFactory;
import org.miaixz.bus.core.map.WeakMap;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.*;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Lambda相关工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LambdaKit {

    private static final WeakMap<String, Info> CACHE = new WeakMap<>();

    /**
     * 通过对象的方法或类的静态方法引用，获取lambda实现类
     *
     * @param func lambda
     * @param <R>  类型
     * @param <T>  lambda的类型
     * @return lambda实现类
     */
    public static <R, T extends Serializable> Class<R> getRealClass(final T func) {
        final Info lambdaInfo = resolve(func);
        return (Class<R>) Optional.of(lambdaInfo)
                .map(Info::getInstantiatedMethodParameterTypes)
                .filter(types -> types.length != 0).map(types -> types[types.length - 1])
                .orElseGet(lambdaInfo::getClazz);
    }

    /**
     * 解析lambda表达式,加了缓存。
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象（无参方法）
     * @param <T>  lambda的类型
     * @return 返回解析后的结果
     */
    public static <T extends Serializable> Info resolve(final T func) {
        return CACHE.computeIfAbsent(func.getClass().getName(), (key) -> {
            final SerializedLambda serializedLambda = _resolve(func);
            final String methodName = serializedLambda.getImplMethodName();
            final Class<?> implClass;
            ClassKit.loadClass(serializedLambda.getImplClass().replace("/", "."), true);
            try {
                implClass = Class.forName(serializedLambda.getImplClass().replace("/", "."), true, Thread.currentThread().getContextClassLoader());
            } catch (final ClassNotFoundException e) {
                throw new InternalException(e);
            }
            if ("<init>".equals(methodName)) {
                for (final Constructor<?> constructor : implClass.getDeclaredConstructors()) {
                    if (ReflectKit.getDescriptor(constructor).equals(serializedLambda.getImplMethodSignature())) {
                        return new Info(constructor, serializedLambda);
                    }
                }
            } else {
                final Method[] methods = ReflectKit.getMethods(implClass);
                for (final Method method : methods) {
                    if (method.getName().equals(methodName)
                            && ReflectKit.getDescriptor(method).equals(serializedLambda.getImplMethodSignature())) {
                        return new Info(method, serializedLambda);
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
     * 解析lambda表达式,没加缓存
     *
     * <p>
     * 通过反射调用实现序列化接口函数对象的writeReplace方法，从而拿到{@link SerializedLambda}<br>
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
        final Object serLambda = ReflectKit.invoke(func, "writeReplace");
        if (Objects.nonNull(serLambda) && serLambda instanceof SerializedLambda) {
            return (SerializedLambda) serLambda;
        }
        throw new InternalException("writeReplace result value is not java.lang.invoke.SerializedLambda");
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
        return LambdaFactory.build(BiConsumer.class, setMethod);
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
    public static <F> F lambda(final Class<F> lambdaType, Class<?> clazz, String methodName, Class... paramsTypes) {
        return LambdaFactory.build(lambdaType, clazz, methodName, paramsTypes);
    }

    @Getter
    public static class Info {

        private static final Type[] EMPTY_TYPE = new Type[0];
        // 实例对象的方法参数类型
        private final Type[] instantiatedMethodParameterTypes;
        // 方法或构造的参数类型
        private final Type[] parameterTypes;
        private final Type returnType;
        // 方法名或构造名称
        private final String name;
        private final Executable executable;
        private final Class<?> clazz;
        private final SerializedLambda lambda;

        /**
         * 构造
         *
         * @param executable 构造对象{@link Constructor}或方法对象{@link Method}
         * @param lambda     实现了序列化接口的lambda表达式
         */
        public Info(final Executable executable, final SerializedLambda lambda) {
            Assert.notNull(executable, "executable must be not null!");
            // return type
            final boolean isMethod = executable instanceof Method;
            final boolean isConstructor = executable instanceof Constructor;
            Assert.isTrue(isMethod || isConstructor, "Unsupported executable type: " + executable.getClass());
            this.returnType = isMethod ?
                    ((Method) executable).getGenericReturnType() : ((Constructor<?>) executable).getDeclaringClass();

            // lambda info
            this.parameterTypes = executable.getGenericParameterTypes();
            this.name = executable.getName();
            this.clazz = executable.getDeclaringClass();
            this.executable = executable;
            this.lambda = lambda;

            // types
            final String instantiatedMethodType = lambda.getInstantiatedMethodType();
            final int index = instantiatedMethodType.indexOf(";)");
            this.instantiatedMethodParameterTypes = (index > -1) ?
                    getInstantiatedMethodParamTypes(instantiatedMethodType.substring(1, index + 1)) : EMPTY_TYPE;
        }

        /**
         * 根据lambda对象的方法签名信息，解析获得实际的参数类型
         */
        private static Type[] getInstantiatedMethodParamTypes(final String className) {
            final String[] instantiatedTypeNames = className.split(";");
            final Type[] types = new Type[instantiatedTypeNames.length];
            for (int i = 0; i < instantiatedTypeNames.length; i++) {
                final boolean isArray = instantiatedTypeNames[i].startsWith(Symbol.BRACKET_LEFT);
                if (isArray && !instantiatedTypeNames[i].endsWith(";")) {
                    // 如果是数组，需要以 ";" 结尾才能加载
                    instantiatedTypeNames[i] += ";";
                } else {
                    if (instantiatedTypeNames[i].startsWith("L")) {
                        // 如果以 "L" 开头，删除 L
                        instantiatedTypeNames[i] = instantiatedTypeNames[i].substring(1);
                    }
                    if (instantiatedTypeNames[i].endsWith(";")) {
                        // 如果以 ";" 结尾，删除 ";"
                        instantiatedTypeNames[i] = instantiatedTypeNames[i].substring(0, instantiatedTypeNames[i].length() - 1);
                    }
                }
                types[i] = ClassKit.loadClass(instantiatedTypeNames[i]);
            }
            return types;
        }

        /**
         * 实例方法参数类型
         *
         * @return 实例方法参数类型
         */
        public Type[] getInstantiatedMethodParameterTypes() {
            return instantiatedMethodParameterTypes;
        }

        /**
         * 获得构造或方法参数类型列表
         *
         * @return 参数类型列表
         */
        public Type[] getParameterTypes() {
            return parameterTypes;
        }

        /**
         * 获取返回值类型（方法引用）
         *
         * @return 返回值类型
         */
        public Type getReturnType() {
            return returnType;
        }

        /**
         * 方法或构造名称
         *
         * @return 方法或构造名称
         */
        public String getName() {
            return name;
        }

        /**
         * 字段名称，主要用于方法名称截取，方法名称必须为getXXX、isXXX、setXXX
         *
         * @return getter或setter对应的字段名称
         */
        public String getFieldName() {
            return BeanKit.getFieldName(getName());
        }

        /**
         * 方法或构造对象
         *
         * @return 方法或构造对象
         */
        public Executable getExecutable() {
            return executable;
        }

        /**
         * 方法或构造所在类
         *
         * @return 方法或构造所在类
         */
        public Class<?> getClazz() {
            return clazz;
        }

        /**
         * 获得Lambda表达式对象
         *
         * @return 获得Lambda表达式对象
         */
        public SerializedLambda getLambda() {
            return lambda;
        }

    }

}
