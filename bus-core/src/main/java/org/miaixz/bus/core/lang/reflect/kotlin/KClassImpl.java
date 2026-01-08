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
