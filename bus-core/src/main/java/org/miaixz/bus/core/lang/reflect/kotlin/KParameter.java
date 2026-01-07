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
package org.miaixz.bus.core.lang.reflect.kotlin;

import java.lang.reflect.Method;
import java.util.Objects;

import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.MethodKit;

/**
 * Represents a Kotlin {@code kotlin.reflect.KParameter} instance. This class provides a wrapper to access properties of
 * a Kotlin KParameter using reflection, such as its name and type.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KParameter {

    /**
     * The {@link Method} object for {@code KParameter.getName()}.
     */
    private static final Method METHOD_GET_NAME;
    /**
     * The {@link Method} object for {@code KParameter.getType()}.
     */
    private static final Method METHOD_GET_TYPE;
    /**
     * The {@link Method} object for {@code KTypeImpl.getJavaType()}.
     */
    private static final Method METHOD_GET_JAVA_TYPE;

    static {
        final Class<?> kParameterClass = ClassKit.loadClass("kotlin.reflect.KParameter");
        METHOD_GET_NAME = MethodKit.getMethod(kParameterClass, "getName");
        METHOD_GET_TYPE = MethodKit.getMethod(kParameterClass, "getType");

        Class<?> kTypeClass;
        try {
            // Kotlin 2.3.0+
            kTypeClass = ClassKit.loadClass("kotlin.reflect.jvm.internal.types.AbstractKType");
        } catch (final Exception e) {
            kTypeClass = ClassKit.loadClass("kotlin.reflect.jvm.internal.KTypeImpl");
        }
        METHOD_GET_JAVA_TYPE = MethodKit.getMethod(kTypeClass, "getJavaType");
    }

    /**
     * The name of the Kotlin parameter.
     */
    private final String name;
    /**
     * The Java {@link Class} representing the type of the Kotlin parameter.
     */
    private final Class<?> type;

    /**
     * Constructs a new {@code KParameter} instance from a Kotlin {@code kotlin.reflect.KParameter} object.
     *
     * @param kParameterInstance The instance of {@code kotlin.reflect.KParameter}.
     */
    public KParameter(final Object kParameterInstance) {
        this.name = MethodKit.invoke(kParameterInstance, METHOD_GET_NAME);
        final Object kType = MethodKit.invoke(kParameterInstance, METHOD_GET_TYPE);
        this.type = MethodKit.invoke(kType, METHOD_GET_JAVA_TYPE);
    }

    /**
     * Retrieves the name of the parameter.
     *
     * @return The parameter name as a {@code String}.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the Java {@link Class} representing the type of the parameter.
     *
     * @return The parameter type as a {@link Class} object.
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Compares this {@code KParameter} to the specified object. The result is {@code true} if and only if the argument
     * is not {@code null} and is a {@code KParameter} object that has the same name and type as this object.
     *
     * @param o The object to compare this {@code KParameter} against.
     * @return {@code true} if the given object represents a {@code KParameter} equivalent to this {@code KParameter},
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final KParameter that = (KParameter) o;
        return Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    /**
     * Returns a hash code value for this {@code KParameter}.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    /**
     * Returns a string representation of this {@code KParameter}. The string representation includes the parameter's
     * name and type.
     *
     * @return A string representation of this {@code KParameter}.
     */
    @Override
    public String toString() {
        return "KotlinParameter{" + "name='" + name + '\'' + ", type=" + type + '}';
    }

}
