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
package org.miaixz.bus.core.xyz;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.bean.copier.ValueProvider;
import org.miaixz.bus.core.bean.copier.provider.MapValueProvider;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.reflect.kotlin.KCallable;
import org.miaixz.bus.core.lang.reflect.kotlin.KClassImpl;
import org.miaixz.bus.core.lang.reflect.kotlin.KParameter;

/**
 * Utility class for Kotlin reflection wrapper.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KotlinKit {

    /**
     * The Kotlin Metadata annotation class.
     */
    private static final Class<? extends Annotation> META_DATA_CLASS = (Class<? extends Annotation>) Optional
            .ofTry(() -> Class.forName("kotlin.Metadata")).getOrNull();

    /**
     * Whether the Kotlin environment is provided or enabled.
     */
    public static final boolean IS_KOTLIN_ENABLE = null != META_DATA_CLASS;

    /**
     * Checks if the given class is a Kotlin class. Kotlin classes are annotated with @kotlin.Metadata.
     *
     * @param clazz the class to check
     * @return {@code true} if it is a Kotlin class, {@code false} otherwise
     */
    public static boolean isKotlinClass(final Class<?> clazz) {
        return IS_KOTLIN_ENABLE && clazz.isAnnotationPresent(META_DATA_CLASS);
    }

    /**
     * Gets all constructors of a Kotlin class.
     *
     * @param targetType the Kotlin class
     * @return a list of constructors
     */
    public static List<?> getConstructors(final Class<?> targetType) {
        return KClassImpl.getConstructors(targetType);
    }

    /**
     * Gets the parameter list of a Kotlin class, method, or constructor.
     *
     * @param kCallable the Kotlin class, method, or constructor
     * @return a list of parameters
     */
    public static List<KParameter> getParameters(final Object kCallable) {
        return KCallable.getParameters(kCallable);
    }

    /**
     * Extracts parameter values from a {@link ValueProvider} corresponding to their names.
     *
     * @param kCallable     the Kotlin class, method, or constructor
     * @param valueProvider the {@link ValueProvider}
     * @return an array of parameter values
     */
    public static Object[] getParameterValues(final Object kCallable, final ValueProvider<String> valueProvider) {
        final List<KParameter> parameters = getParameters(kCallable);
        final Object[] args = new Object[parameters.size()];
        KParameter kParameter;
        for (int i = 0; i < parameters.size(); i++) {
            kParameter = parameters.get(i);
            args[i] = valueProvider.value(kParameter.getName(), kParameter.getType());
        }
        return args;
    }

    /**
     * Instantiates a Kotlin object.
     *
     * @param <T>        the object type
     * @param targetType the target class type
     * @param map        a map of parameter names to parameter values
     * @return the instantiated object
     */
    public static <T> T newInstance(final Class<T> targetType, final Map<String, ?> map) {
        return newInstance(targetType, new MapValueProvider(map));
    }

    /**
     * Instantiates a Kotlin object.
     *
     * @param <T>           the object type
     * @param targetType    the target class type
     * @param valueProvider a value provider for constructor parameters
     * @return the instantiated object
     */
    public static <T> T newInstance(final Class<T> targetType, final ValueProvider<String> valueProvider) {
        final List<?> constructors = getConstructors(targetType);
        RuntimeException exception = null;
        for (final Object constructor : constructors) {
            final Object[] parameterValues = getParameterValues(constructor, valueProvider);
            try {
                return (T) KCallable.call(constructor, parameterValues);
            } catch (final RuntimeException e) {
                exception = e;
            }
        }
        if (exception != null) {
            throw exception;
        }
        return null;
    }

}
