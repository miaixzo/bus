/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.*;

import org.miaixz.bus.core.center.function.LambdaFactory;
import org.miaixz.bus.core.center.function.LambdaX;
import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.reflect.method.MethodInvoker;

/**
 * Utility class for Lambda expressions.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LambdaKit {

    private static final WeakConcurrentMap<Object, LambdaX> CACHE = new WeakConcurrentMap<>();

    /**
     * Gets the implementation class of a lambda expression from an object's method or a class's static method
     * reference. This method works for lambdas with no parameters but a return value, such as:
     * <ul>
     * <li>Reference to an instance method of a particular object:
     * 
     * <pre>{@code
     * MyTeacher myTeacher = new MyTeacher();
     * Class<MyTeacher> supplierClass = LambdaKit.getRealClass(myTeacher::getAge);
     * Assert.assertEquals(MyTeacher.class, supplierClass);
     * }</pre>
     * 
     * </li>
     * <li>Reference to a static method with no parameters:
     * 
     * <pre>{@code
     * Class<MyTeacher> staticSupplierClass = LambdaKit.getRealClass(MyTeacher::takeAge);
     * Assert.assertEquals(MyTeacher.class, staticSupplierClass);
     * }</pre>
     * 
     * </li>
     * </ul>
     * Note: In some cases, the exact type cannot be retrieved:
     * 
     * <pre>{@code
     * // Enum test, can only get the enum type
     * Class<Enum<?>> enumSupplierClass = LambdaKit.getRealClass(LambdaKit.LambdaKindEnum.REF_NONE::ordinal);
     * Assert.assertEquals(Enum.class, enumSupplierClass);
     * // Calling a superclass method, can only get the superclass type
     * Class<Entity<?>> superSupplierClass = LambdaKit.getRealClass(myTeacher::getId);
     * Assert.assertEquals(Entity.class, superSupplierClass);
     * // Reference to a superclass static method with parameters, can only get the superclass type
     * Class<Entity<?>> staticSuperFunctionClass = LambdaKit.getRealClass(MyTeacher::takeId);
     * Assert.assertEquals(Entity.class, staticSuperFunctionClass);
     * }</pre>
     *
     * @param func The lambda.
     * @param <R>  The type of the class.
     * @param <T>  The type of the lambda.
     * @return The lambda implementation class.
     */
    public static <R, T extends Serializable> Class<R> getRealClass(final T func) {
        final LambdaX lambdaX = resolve(func);
        return (Class<R>) Optional.of(lambdaX).map(LambdaX::getInstantiatedMethodParameterTypes)
                .filter(types -> types.length != 0).map(types -> types[types.length - 1]).orElseGet(lambdaX::getClazz);
    }

    /**
     * Resolves a lambda expression, with caching. The cache may be cleared at any time.
     *
     * @param func The lambda object to resolve.
     * @param <T>  The type of the lambda.
     * @return The resolved lambda information.
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
     * Gets the method name of a lambda expression.
     *
     * @param func The function (method reference).
     * @param <T>  The type of the lambda.
     * @return The method name.
     */
    public static <T extends Serializable> String getMethodName(final T func) {
        return resolve(func).getName();
    }

    /**
     * Gets the field name corresponding to a lambda expression of a Getter or Setter method. The rules are:
     * <ul>
     * <li>`getXxxx` becomes `xxxx` (e.g., `getName` becomes `name`).</li>
     * <li>`setXxxx` becomes `xxxx` (e.g., `setName` becomes `name`).</li>
     * <li>`isXxxx` becomes `xxxx` (e.g., `isName` becomes `name`).</li>
     * <li>Other method names that do not follow these rules will throw an {@link IllegalArgumentException}.</li>
     * </ul>
     *
     * @param func The function.
     * @param <T>  The type of the lambda.
     * @return The field name.
     * @throws IllegalArgumentException if the method is not a standard getter or setter.
     */
    public static <T extends Serializable> String getFieldName(final T func) throws IllegalArgumentException {
        return BeanKit.getFieldName(getMethodName(func));
    }

    /**
     * Builds a `Function` equivalent to a getter method reference (`Obj::getXxx`).
     *
     * @param getMethod The getter method.
     * @param <T>       The type of the object calling the getter.
     * @param <R>       The return type of the getter.
     * @return A `Function` representing `Obj::getXxx`.
     */
    public static <T, R> Function<T, R> buildGetter(final Method getMethod) {
        return LambdaFactory.build(Function.class, getMethod);
    }

    /**
     * Builds a `Function` equivalent to a getter method reference (`Obj::getXxx`).
     *
     * @param clazz     The class of the object calling the getter.
     * @param fieldName The name of the field.
     * @param <T>       The type of the object calling the getter.
     * @param <R>       The return type of the getter.
     * @return A `Function` representing `Obj::getXxx`.
     */
    public static <T, R> Function<T, R> buildGetter(final Class<T> clazz, final String fieldName) {
        final MethodInvoker getter = (MethodInvoker) BeanKit.getBeanDesc(clazz).getGetter(fieldName);
        return buildGetter(getter.getMethod());
    }

    /**
     * Builds a `BiConsumer` equivalent to a setter method reference (`Obj::setXxx`).
     *
     * @param setMethod The setter method.
     * @param <T>       The type of the object calling the setter.
     * @param <P>       The type of the parameter of the setter.
     * @return A `BiConsumer` representing `Obj::setXxx`.
     */
    public static <T, P> BiConsumer<T, P> buildSetter(final Method setMethod) {
        final Class<?> returnType = setMethod.getReturnType();
        if (Void.TYPE == returnType) {
            return LambdaFactory.build(BiConsumer.class, setMethod);
        }

        // Handle fluent setters that return 'this'
        final BiFunction<T, P, ?> biFunction = LambdaFactory.build(BiFunction.class, setMethod);
        return biFunction::apply;
    }

    /**
     * Builds a `BiConsumer` equivalent to a setter method reference (`Obj::setXxx`).
     *
     * @param clazz     The class of the object calling the setter.
     * @param fieldName The name of the field.
     * @param <T>       The type of the object calling the setter.
     * @param <P>       The type of the parameter of the setter.
     * @return A `BiConsumer` representing `Obj::setXxx`.
     */
    public static <T, P> BiConsumer<T, P> buildSetter(final Class<T> clazz, final String fieldName) {
        final MethodInvoker setter = (MethodInvoker) BeanKit.getBeanDesc(clazz).getSetter(fieldName);
        return buildSetter(setter.getMethod());
    }

    /**
     * Builds a functional interface instance for a method reference (`Obj::method`).
     *
     * @param lambdaType  The functional interface type to build.
     * @param clazz       The class containing the method.
     * @param methodName  The method name.
     * @param paramsTypes The parameter types of the method.
     * @param <F>         The type of the functional interface.
     * @return An instance of the functional interface.
     */
    public static <F> F build(
            final Class<F> lambdaType,
            final Class<?> clazz,
            final String methodName,
            final Class<?>... paramsTypes) {
        return LambdaFactory.build(lambdaType, clazz, methodName, paramsTypes);
    }

    /**
     * Converts a {@link BiFunction} to a {@link Function} by fixing the second parameter.
     *
     * @param biFunction The {@link BiFunction}.
     * @param param      The fixed second parameter.
     * @param <T>        The type of the first parameter.
     * @param <U>        The type of the second parameter.
     * @param <R>        The return type.
     * @return A {@link Function}.
     */
    public static <T, U, R> Function<T, R> toFunction(final BiFunction<T, U, R> biFunction, final U param) {
        return (t) -> biFunction.apply(t, param);
    }

    /**
     * Converts a {@link BiPredicate} to a {@link Predicate} by fixing the second parameter.
     *
     * @param biPredicate The {@link BiPredicate}.
     * @param param       The fixed second parameter.
     * @param <T>         The type of the first parameter.
     * @param <U>         The type of the second parameter.
     * @return A {@link Predicate}.
     */
    public static <T, U> Predicate<T> toPredicate(final BiPredicate<T, U> biPredicate, final U param) {
        return (t) -> biPredicate.test(t, param);
    }

    /**
     * Converts a {@link BiConsumer} to a {@link Consumer} by fixing the second parameter.
     *
     * @param biConsumer The {@link BiConsumer}.
     * @param param      The fixed second parameter.
     * @param <T>        The type of the first parameter.
     * @param <U>        The type of the second parameter.
     * @return A {@link Consumer}.
     */
    public static <T, U> Consumer<T> toPredicate(final BiConsumer<T, U> biConsumer, final U param) {
        return (t) -> biConsumer.accept(t, param);
    }

    /**
     * Gets the abstract method of a functional interface.
     *
     * @param funcType The functional interface class.
     * @return The {@link Method}.
     */
    public static Method getInvokeMethod(final Class<?> funcType) {
        final Method[] abstractMethods = MethodKit.getPublicMethods(funcType, ModifierKit::isAbstract);
        Assert.equals(abstractMethods.length, 1, "Not a function class: " + funcType.getName());
        return abstractMethods[0];
    }

    /**
     * Resolves a lambda expression without caching.
     *
     * <p>
     * It reflectively calls the `writeReplace` method on the serializable lambda to obtain the
     * {@link SerializedLambda}, which contains most of the lambda's information.
     *
     * @param func The lambda object to resolve.
     * @param <T>  The type of the lambda.
     * @return The resolved `SerializedLambda`.
     */
    private static <T extends Serializable> SerializedLambda _resolve(final T func) {
        if (func instanceof SerializedLambda) {
            return (SerializedLambda) func;
        }
        if (func instanceof Proxy) {
            throw new IllegalArgumentException("Proxy not supported at this time.");
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
