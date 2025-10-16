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
package org.miaixz.bus.proxy;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ClassKit;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * A utility class for creating and working with dynamic proxies.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Builder {

    /**
     * Gets the default dynamic proxy provider.
     *
     * @return The default {@link Provider} instance.
     */
    public static Provider getEngine() {
        return Factory.getEngine();
    }

    /**
     * Creates a proxy for the given target using the specified aspect class. The aspect class will be instantiated
     * automatically.
     *
     * @param <T>         The type of the target object.
     * @param target      The object to be proxied.
     * @param aspectClass The class of the aspect to apply.
     * @return The proxied object.
     */
    public static <T> T proxy(final T target, final Class<? extends Aspect> aspectClass) {
        return getEngine().proxy(target, aspectClass);
    }

    /**
     * Creates a proxy for the given target using the specified aspect instance.
     *
     * @param <T>    The type of the target object.
     * @param target The object to be proxied.
     * @param aspect The aspect instance to apply.
     * @return The proxied object.
     */
    public static <T> T proxy(final T target, final Aspect aspect) {
        return getEngine().proxy(target, aspect);
    }

    /**
     * Creates a new dynamic proxy object using the JDK's {@link Proxy} class. The creation of a dynamic proxy object
     * involves the following steps:
     * <ol>
     * <li>A new class (e.g., $Proxy0) is dynamically generated that implements the specified interfaces.</li>
     * <li>This new class is loaded into the JVM using the provided class loader.</li>
     * <li>An instance of the new class is created by calling its constructor, which takes an
     * {@link InvocationHandler}.</li>
     * <li>The methods of the implemented interfaces are overridden to delegate their calls to the
     * {@link InvocationHandler#invoke} method.</li>
     * <li>The new proxy instance is returned.</li>
     * </ol>
     *
     * @param <T>               The type of the proxy object.
     * @param classloader       The class loader to define the proxy class in.
     * @param invocationHandler The invocation handler to dispatch method invocations to.
     * @param interfaces        An array of interfaces that the proxy class will implement.
     * @return A proxy instance that implements the specified interfaces.
     */
    public static <T> T newProxyInstance(
            final ClassLoader classloader,
            final InvocationHandler invocationHandler,
            final Class<?>... interfaces) {
        return (T) Proxy.newProxyInstance(classloader, interfaces, invocationHandler);
    }

    /**
     * Creates a new dynamic proxy object using the current thread's context class loader.
     *
     * @param <T>               The type of the proxy object.
     * @param invocationHandler The invocation handler to dispatch method invocations to.
     * @param interfaces        An array of interfaces that the proxy class will implement.
     * @return A proxy instance that implements the specified interfaces.
     */
    public static <T> T newProxyInstance(final InvocationHandler invocationHandler, final Class<?>... interfaces) {
        return newProxyInstance(ClassKit.getClassLoader(), invocationHandler, interfaces);
    }

    /**
     * Checks if an object is a proxy created by either the JDK dynamic proxy mechanism or CGLIB.
     *
     * @param object The object to check.
     * @return {@code true} if the object is a proxy, {@code false} otherwise.
     */
    public static boolean isProxy(final Object object) {
        return isJdkProxy(object) || isCglibProxy(object);
    }

    /**
     * Checks if an object is a JDK dynamic proxy.
     *
     * @param object The object to check.
     * @return {@code true} if the object is a JDK dynamic proxy, {@code false} otherwise.
     */
    public static boolean isJdkProxy(final Object object) {
        return Proxy.isProxyClass(object.getClass());
    }

    /**
     * Checks if an object is a CGLIB proxy by inspecting its class name for the CGLIB naming convention (which
     * typically contains "$$").
     *
     * @param object The object to check.
     * @return {@code true} if the object appears to be a CGLIB proxy, {@code false} otherwise.
     */
    public static boolean isCglibProxy(final Object object) {
        return (object.getClass().getName().contains(Symbol.DOLLAR + Symbol.DOLLAR));
    }

}
