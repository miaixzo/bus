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
package org.miaixz.bus.limiter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.reflect.JdkProxy;

/**
 * Utility class for building and resolving information related to method calls and classes. This class provides methods
 * to extract the real user class from a potentially proxied class and to generate a unique string representation for a
 * given method.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * A concurrent hash map to cache method names for performance. The key is the {@link Method} object, and the value
     * is its string representation.
     */
    private static final Map<Method, String> MAP = new ConcurrentHashMap<>();
    /**
     * A lock object used for synchronizing access to the {@link #MAP} when resolving method names.
     */
    private static final Object LOCK = new Object();

    /**
     * Retrieves the actual user class from a given class, handling CGLIB proxies. If the provided class is a CGLIB
     * proxy, it recursively gets the superclass until the non-proxied user class is found.
     *
     * @param clazz The class to inspect.
     * @return The actual user class, or the original class if it's not a CGLIB proxy.
     */
    public static Class<?> getUserClass(Class<?> clazz) {
        if (JdkProxy.isCglibProxyClass(clazz)) {
            Class<?> superclass = clazz.getSuperclass();
            return getUserClass(superclass);
        }
        return clazz;
    }

    /**
     * Resolves and caches a unique string representation for the given method. The method name is constructed using the
     * declaring class's name, the method's name, and the canonical names of its parameter types. This string is then
     * cached to avoid repeated computation.
     *
     * @param method The method instance for which to resolve the name.
     * @return A unique string representation of the method.
     * @throws IllegalArgumentException if the provided method is {@code null}.
     */
    public static String resolveMethodName(Method method) {
        if (method == null) {
            throw new IllegalArgumentException("Null method");
        }
        String methodName = MAP.get(method);
        if (methodName == null) {
            synchronized (LOCK) {
                methodName = MAP.get(method);
                if (methodName == null) {
                    StringBuilder sb = new StringBuilder();

                    String className = method.getDeclaringClass().getName();
                    String name = method.getName();
                    Class<?>[] params = method.getParameterTypes();
                    sb.append(className).append(Symbol.COLON).append(name);
                    sb.append(Symbol.PARENTHESE_LEFT);

                    int paramPos = 0;
                    for (Class<?> clazz : params) {
                        sb.append(clazz.getCanonicalName());
                        if (++paramPos < params.length) {
                            sb.append(Symbol.COMMA);
                        }
                    }
                    sb.append(Symbol.PARENTHESE_RIGHT);
                    methodName = sb.toString();

                    MAP.put(method, methodName);
                }
            }
        }
        return methodName;
    }

}
