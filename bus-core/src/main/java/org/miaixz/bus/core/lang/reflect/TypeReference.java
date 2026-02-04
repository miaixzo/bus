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

import java.lang.reflect.Type;

import org.miaixz.bus.core.xyz.TypeKit;

/**
 * A utility class for obtaining generic type information at runtime. By creating an anonymous subclass of
 * {@code TypeReference}, the actual generic type arguments can be captured and retrieved.
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * 
 * TypeReference<List<String>> listRef = new TypeReference<List<String>>() {
 * };
 * Type type = listRef.getType(); // type will be ParameterizedType representing List<String>
 * }</pre>
 * <p>
 * This class cannot be used with wildcard generic parameters (e.g., {@code Class<?>} or
 * {@code List<? extends CharSequence>}).
 * <p>
 * This implementation is inspired by the {@code TypeReference} in FastJSON.
 *
 * @param <T> The custom reference type whose generic information is to be captured.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class TypeReference<T> implements Type {

    /**
     * The actual generic type captured from the subclass.
     */
    private final Type type;

    /**
     * Constructs a new {@code TypeReference}. This constructor captures the generic type argument {@code T} from the
     * anonymous subclass.
     */
    public TypeReference() {
        this.type = TypeKit.getTypeArgument(getClass());
    }

    /**
     * Retrieves the actual generic type argument {@code T} that this {@code TypeReference} represents.
     *
     * @return The {@link Type} representing the generic type argument.
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Returns a string representation of the generic type captured by this {@code TypeReference}.
     *
     * @return A string representation of the captured {@link Type}.
     */
    @Override
    public String toString() {
        return this.type.toString();
    }

}
