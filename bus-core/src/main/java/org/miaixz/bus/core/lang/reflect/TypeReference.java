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
