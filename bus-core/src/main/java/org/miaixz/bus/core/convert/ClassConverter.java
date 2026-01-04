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
