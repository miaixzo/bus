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
package org.miaixz.bus.core.lang.reflect.kotlin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.MethodKit;

/**
 * Wrapper class for Kotlin's {@code kotlin.reflect.KCallable} methods. This class provides static utility methods to
 * interact with Kotlin callable entities (classes, methods, constructors) via reflection.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KCallable {

    /**
     * The {@link Method} object for {@code KCallable.getParameters()}.
     */
    private static final Method METHOD_GET_PARAMETERS;
    /**
     * The {@link Method} object for {@code KCallable.call()}.
     */
    private static final Method METHOD_CALL;

    static {
        final Class<?> kFunctionClass = ClassKit.loadClass("kotlin.reflect.KCallable");
        METHOD_GET_PARAMETERS = MethodKit.getMethod(kFunctionClass, "getParameters");
        METHOD_CALL = MethodKit.getMethodByName(kFunctionClass, "call");
    }

    /**
     * Retrieves the list of parameters for a given Kotlin callable entity.
     *
     * @param kCallable The Kotlin callable entity (class, method, or constructor) as an {@code Object}.
     * @return A {@link List} of {@link KParameter} objects representing the parameters of the callable.
     */
    public static List<KParameter> getParameters(final Object kCallable) {
        final List<?> parameters = MethodKit.invoke(kCallable, METHOD_GET_PARAMETERS);
        final List<KParameter> result = new ArrayList<>(parameters.size());
        for (final Object parameter : parameters) {
            result.add(new KParameter(parameter));
        }
        return result;
    }

    /**
     * Invokes the {@code call} method on a Kotlin callable entity, effectively instantiating an object or executing a
     * function/method.
     *
     * @param kCallable The Kotlin callable entity (class, method, or constructor) as an {@code Object}.
     * @param args      The arguments to be passed to the {@code call} method.
     * @return The result of the {@code call} method invocation.
     */
    public static Object call(final Object kCallable, final Object... args) {
        return MethodKit.invoke(kCallable, METHOD_CALL, new Object[] { args });
    }

}
