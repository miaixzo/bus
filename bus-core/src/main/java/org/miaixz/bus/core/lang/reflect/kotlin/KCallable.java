/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 21+
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
