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
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;

import org.miaixz.bus.core.lang.reflect.TypeReference;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * Converts an object to a {@link Collection} of a specified type.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CollectionConverter implements MatcherConverter, Serializable {

    /**
     * Constructs a new CollectionConverter. Utility class constructor for static access.
     */
    CollectionConverter() {
    }

    /**
     * Singleton instance.
     */
    public static final CollectionConverter INSTANCE = new CollectionConverter();
    /**
     * The serial version UID.
     */
    @Serial
    private static final long serialVersionUID = 2852266827800L;

    /**
     * Checks if this converter can handle the conversion to the specified target type.
     *
     * @param targetType The target type.
     * @param rawType    The raw class of the target type.
     * @param value      The value to be converted.
     * @return {@code true} if the raw type is assignable from {@link Collection}, {@code false} otherwise.
     */
    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return Collection.class.isAssignableFrom(rawType);
    }

    /**
     * Converts the given value to a {@link Collection}.
     *
     * @param targetType The target collection type.
     * @param value      The value to convert.
     * @return The converted {@link Collection}.
     */
    @Override
    public Collection<?> convert(Type targetType, final Object value) {
        if (targetType instanceof TypeReference) {
            targetType = ((TypeReference<?>) targetType).getType();
        }

        return convert(targetType, TypeKit.getTypeArgument(targetType), value);
    }

    /**
     * Converts the given value to a {@link Collection} with a specific element type.
     *
     * @param collectionType The type of the collection (e.g., {@code List.class}).
     * @param elementType    The type of the elements in the collection.
     * @param value          The value to be converted (can be an array, an {@link Iterable}, an
     *                       {@link java.util.Iterator}, or a single element).
     * @return The converted collection object.
     */
    public Collection<?> convert(final Type collectionType, final Type elementType, final Object value) {
        // Create a new collection instance, compatible with EnumSet.
        final Collection<?> collection = CollKit
                .create(TypeKit.getClass(collectionType), TypeKit.getClass(elementType));
        // Add all elements from the value, converting them to the target element type.
        return CollKit.addAll(collection, value, elementType);
    }

}
