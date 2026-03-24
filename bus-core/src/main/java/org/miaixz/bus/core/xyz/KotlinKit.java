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
 * @since Java 21+
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
