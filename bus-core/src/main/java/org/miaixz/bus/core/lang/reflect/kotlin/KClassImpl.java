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
import java.util.List;

import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.ReflectKit;

/**
 * Wrapper for {@code kotlin.reflect.jvm.internal.KClassImpl}. This class provides utility methods to interact with
 * Kotlin's internal KClass implementation for reflection purposes, specifically to retrieve constructors.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KClassImpl {

    /**
     * The {@link Class} object for {@code kotlin.reflect.jvm.internal.KClassImpl}.
     */
    private static final Class<?> KCLASS_IMPL_CLASS;
    /**
     * The {@link Method} object for {@code KClassImpl.getConstructors()}.
     */
    private static final Method METHOD_GET_CONSTRUCTORS;

    static {
        KCLASS_IMPL_CLASS = ClassKit.loadClass("kotlin.reflect.jvm.internal.KClassImpl");
        METHOD_GET_CONSTRUCTORS = MethodKit.getMethod(KCLASS_IMPL_CLASS, "getConstructors");
    }

    /**
     * Retrieves all constructors of a given Kotlin class. This method internally creates an instance of
     * {@code KClassImpl} and invokes its {@code getConstructors()} method.
     *
     * @param targetType The Kotlin class for which to retrieve constructors.
     * @return A {@link List} of {@code Object} representing the constructors of the Kotlin class.
     */
    public static List<?> getConstructors(final Class<?> targetType) {
        final Object kClassImpl = ReflectKit.newInstance(KCLASS_IMPL_CLASS, targetType);
        return MethodKit.invoke(kClassImpl, METHOD_GET_CONSTRUCTORS);
    }

}
