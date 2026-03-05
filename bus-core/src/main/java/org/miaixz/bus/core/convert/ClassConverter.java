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
package org.miaixz.bus.core.convert;

import java.io.Serial;
import java.lang.reflect.Type;

import org.miaixz.bus.core.xyz.ClassKit;

/**
 * Converts an object to a {@link Class}. The input is typically a string representing the fully qualified class name.
 * By default, the class is initialized (i.e., its static block is executed).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ClassConverter extends AbstractConverter implements MatcherConverter {

    @Serial
    private static final long serialVersionUID = 2852266707267L;

    /**
     * Singleton instance.
     */
    public static ClassConverter INSTANCE = new ClassConverter();

    /**
     * Whether to initialize the loaded class.
     */
    private final boolean isInitialized;

    /**
     * Constructs a new {@code ClassConverter} that initializes the loaded class by default.
     */
    public ClassConverter() {
        this(true);
    }

    /**
     * Constructs a new {@code ClassConverter}.
     *
     * @param isInitialized If {@code true}, the class will be initialized upon loading (its static initializers will be
     *                      run).
     */
    public ClassConverter(final boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    /**
     * Checks if this converter can handle the conversion to a {@link Class}.
     *
     * @param targetType The target type.
     * @param rawType    The raw class of the target type.
     * @param value      The value to be converted.
     * @return {@code true} if the raw type is {@code java.lang.Class}, {@code false} otherwise.
     */
    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return "java.lang.Class".equals(rawType.getName());
    }

    /**
     * Internally converts the given value to a {@link Class}.
     *
     * @param targetClass The target class, which should be {@link Class}.
     * @param value       The value to be converted, typically a class name.
     * @return The loaded {@link Class} object.
     */
    @Override
    protected Class<?> convertInternal(final Class<?> targetClass, final Object value) {
        return ClassKit.loadClass(convertToString(value), isInitialized);
    }

}
