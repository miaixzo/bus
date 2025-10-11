/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.lang.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ClassKit;

/**
 * Utility class for JDK's {@link Proxy} related operations. This class provides methods for creating dynamic proxy
 * objects and checking if an object or class is a proxy.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkProxy {

    /**
     * Creates a dynamic proxy object. The creation principle of a dynamic proxy object is as follows: Assuming the
     * created proxy object is named $Proxy0:
     * <ol>
     * <li>A class is dynamically generated based on the provided interfaces, implementing the interfaces.</li>
     * <li>The generated class is loaded into the JVM via the provided classloader.</li>
     * <li>The constructor $Proxy0(InvocationHandler) of $Proxy0 is called to create an instance of $Proxy0. It iterates
     * through all interface methods, and their implementations essentially invoke the methods of the proxied object via
     * reflection.</li>
     * <li>The instance of $Proxy0 is returned to the client.</li>
     * <li>When a corresponding method of the proxy class is called, it is equivalent to calling the
     * {@link InvocationHandler#invoke(Object, java.lang.reflect.Method, Object[])} method.</li>
     * </ol>
     *
     * @param <T>               The type of the proxied object.
     * @param classloader       The ClassLoader corresponding to the proxied class.
     * @param invocationHandler The {@link InvocationHandler} that provides dynamic proxy functionality by implementing
     *                          this interface.
     * @param interfaces        The interfaces that the proxy class needs to implement.
     * @return The proxy class instance.
     */
    public static <T> T newProxyInstance(
            final ClassLoader classloader,
            final InvocationHandler invocationHandler,
            final Class<?>... interfaces) {
        return (T) Proxy.newProxyInstance(classloader, interfaces, invocationHandler);
    }

    /**
     * Creates a dynamic proxy object using the default class loader.
     *
     * @param <T>               The type of the proxied object.
     * @param invocationHandler The {@link InvocationHandler} that provides dynamic proxy functionality.
     * @param interfaces        The interfaces that the proxy class needs to implement.
     * @return The proxy class instance.
     */
    public static <T> T newProxyInstance(final InvocationHandler invocationHandler, final Class<?>... interfaces) {
        return newProxyInstance(ClassKit.getClassLoader(), invocationHandler, interfaces);
    }

    /**
     * Checks if the given object is a proxy object, including JDK proxy or Cglib proxy.
     *
     * @param object The object to be checked. Must not be {@code null}.
     * @return {@code true} if the object is a proxy object, {@code false} otherwise.
     * @throws IllegalArgumentException if the object is {@code null}.
     */
    public static boolean isProxy(final Object object) {
        Assert.notNull(object);
        return isProxyClass(object.getClass());
    }

    /**
     * Checks if the given object is a JDK proxy object.
     *
     * @param object The object to be checked. Must not be {@code null}.
     * @return {@code true} if the object is a JDK proxy object, {@code false} otherwise.
     * @throws IllegalArgumentException if the object is {@code null}.
     */
    public static boolean isJdkProxy(final Object object) {
        Assert.notNull(object);
        return isJdkProxyClass(object.getClass());
    }

    /**
     * Checks if the given object is a Cglib proxy object.
     *
     * @param object The object to be checked. Must not be {@code null}.
     * @return {@code true} if the object is a Cglib proxy object, {@code false} otherwise.
     * @throws IllegalArgumentException if the object is {@code null}.
     */
    public static boolean isCglibProxy(final Object object) {
        Assert.notNull(object);
        return isCglibProxyClass(object.getClass());
    }

    /**
     * Checks if the given class is a proxy class, including JDK proxy or Cglib proxy.
     *
     * @param clazz The class to be checked. Must not be {@code null}.
     * @return {@code true} if the class is a proxy class, {@code false} otherwise.
     * @throws IllegalArgumentException if the class is {@code null}.
     */
    public static boolean isProxyClass(final Class<?> clazz) {
        return isJdkProxyClass(clazz) || isCglibProxyClass(clazz);
    }

    /**
     * Checks if the given class is a JDK proxy class.
     *
     * @param clazz The class to be checked. Must not be {@code null}.
     * @return {@code true} if the class is a JDK proxy class, {@code false} otherwise.
     * @throws IllegalArgumentException if the class is {@code null}.
     */
    public static boolean isJdkProxyClass(final Class<?> clazz) {
        return Proxy.isProxyClass(Assert.notNull(clazz));
    }

    /**
     * Checks if the given class is a Cglib proxy class. Cglib proxy classes typically contain "$$" in their names.
     *
     * @param clazz The class to be checked. Must not be {@code null}.
     * @return {@code true} if the class is a Cglib proxy class, {@code false} otherwise.
     * @throws IllegalArgumentException if the class is {@code null}.
     */
    public static boolean isCglibProxyClass(final Class<?> clazz) {
        return Assert.notNull(clazz).getName().contains(Symbol.DOLLAR + Symbol.DOLLAR);
    }

    /**
     * Retrieves the actual class of a Cglib proxy. If the provided class is a Cglib proxy, this method traverses the
     * superclass hierarchy until the non-proxy superclass is found.
     *
     * @param clazz The proxy class. Must not be {@code null}.
     * @return The actual class, which is the non-proxy superclass if {@code clazz} was a Cglib proxy, or {@code clazz}
     *         itself if it was not a Cglib proxy.
     * @throws IllegalArgumentException if the class is {@code null}.
     */
    public static Class<?> getCglibActualClass(Class<?> clazz) {
        Class<?> actualClass = clazz;
        while (isCglibProxyClass(actualClass)) {
            actualClass = actualClass.getSuperclass();
        }
        return actualClass;
    }

}
