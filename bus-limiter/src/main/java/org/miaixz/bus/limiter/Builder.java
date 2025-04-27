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
 * 构造信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    private static final Map<Method, String> MAP = new ConcurrentHashMap<>();
    private static final Object LOCK = new Object();

    public static Class<?> getUserClass(Class<?> clazz) {
        if (JdkProxy.isCglibProxyClass(clazz)) {
            Class<?> superclass = clazz.getSuperclass();
            return getUserClass(superclass);
        }
        return clazz;
    }

    /**
     * 解析并解析方法名，然后缓存到map中
     *
     * @param method 方法实例
     * @return the string
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
