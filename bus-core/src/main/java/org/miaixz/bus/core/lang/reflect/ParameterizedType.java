/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.reflect;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Type;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * An implementation of the {@link java.lang.reflect.ParameterizedType} interface, used to programmatically define
 * generic types.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ParameterizedType implements java.lang.reflect.ParameterizedType, Serializable {

    @Serial
    private static final long serialVersionUID = 2852276963509L;

    /**
     * The actual type arguments for this parameterized type.
     */
    private final Type[] actualTypeArguments;
    /**
     * The owner type of this parameterized type, or {@code null} if this is a top-level type.
     */
    private final Type ownerType;
    /**
     * The raw type (typically a class) of this parameterized type.
     */
    private final Type rawType;

    /**
     * Constructor.
     *
     * @param actualTypeArguments The actual generic type arguments.
     * @param ownerType           The owner type (for inner classes).
     * @param rawType             The raw type.
     */
    public ParameterizedType(final Type[] actualTypeArguments, final Type ownerType, final Type rawType) {
        this.actualTypeArguments = actualTypeArguments;
        this.ownerType = ownerType;
        this.rawType = rawType;
    }

    /**
     * Appends the string representation of types to a `StringBuilder`, separated by a separator.
     *
     * @param buf   The target `StringBuilder`.
     * @param sep   The separator.
     * @param types The types to append.
     * @return The `StringBuilder`.
     */
    private static StringBuilder appendAllTo(final StringBuilder buf, final String sep, final Type... types) {
        if (ArrayKit.isNotEmpty(types)) {
            boolean isFirst = true;
            for (final Type type : types) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    buf.append(sep);
                }

                final String typeStr;
                if (type instanceof Class) {
                    typeStr = ((Class<?>) type).getName();
                } else {
                    typeStr = StringKit.toString(type);
                }

                buf.append(typeStr);
            }
        }
        return buf;
    }

    /**
     * Returns the actual type arguments for this parameterized type.
     *
     * @return An array of the actual type arguments.
     */
    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    /**
     * Returns the owner type of this parameterized type.
     * <p>
     * Returns {@code null} if this is a top-level type or a static nested type.
     *
     * @return The owner type, or {@code null} if there is no owner.
     */
    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    /**
     * Returns the raw type of this parameterized type.
     * <p>
     * This is typically the {@link Class} object that declares the type.
     *
     * @return The raw type.
     */
    @Override
    public Type getRawType() {
        return rawType;
    }

    /**
     * Returns the string representation of this parameterized type.
     * <p>
     * The format is: {@code owner.raw<arg1, arg2, ...>} or {@code raw<arg1, arg2, ...>}
     *
     * @return The string representation of this parameterized type.
     */
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();

        final Type useOwner = this.ownerType;
        final Class<?> raw = (Class<?>) this.rawType;
        if (useOwner == null) {
            buf.append(raw.getName());
        } else {
            if (useOwner instanceof Class<?>) {
                buf.append(((Class<?>) useOwner).getName());
            } else {
                buf.append(useOwner);
            }
            buf.append(Symbol.C_DOT).append(raw.getSimpleName());
        }

        appendAllTo(buf.append(Symbol.C_LT), Symbol.COMMA + Symbol.SPACE, this.actualTypeArguments).append(Symbol.C_GT);
        return buf.toString();
    }

}
