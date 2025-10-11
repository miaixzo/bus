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
package org.miaixz.bus.core.lang.reflect.creator;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.*;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.ReflectKit;

/**
 * An object instantiator that attempts to create objects by judging the type or calling possible constructors. It
 * supports:
 * <ul>
 * <li>Primitive types</li>
 * <li>Interface or abstract types (with default implementations)</li>
 * <li>Enums</li>
 * <li>Arrays</li>
 * <li>Constructors with default parameters</li>
 * </ul>
 * <p>
 * For interfaces or abstract types, it constructs their default implementations:
 * 
 * <pre>
 *     Map       - HashMap
 *     Collection - ArrayList
 *     List      - ArrayList
 *     Set       - HashSet
 *     SortedSet - TreeSet
 * </pre>
 *
 * @param <T> The type of the object to be created.
 * @author Kimi Liu
 * @since Java 17+
 */
public class PossibleObjectCreator<T> implements ObjectCreator<T> {

    /**
     * The class to be instantiated.
     */
    final Class<T> clazz;

    /**
     * Constructs a new {@code PossibleObjectCreator} for the given class.
     *
     * @param clazz The class to be instantiated. Must not be {@code null}.
     * @throws IllegalArgumentException if {@code clazz} is {@code null}.
     */
    public PossibleObjectCreator(final Class<T> clazz) {
        this.clazz = Assert.notNull(clazz);
    }

    /**
     * Creates a new {@code PossibleObjectCreator} instance for the given class.
     *
     * @param clazz The class to be instantiated.
     * @param <T>   The type of the object to be created.
     * @return A new {@code PossibleObjectCreator} instance.
     */
    public static <T> PossibleObjectCreator<T> of(final Class<T> clazz) {
        return new PossibleObjectCreator<>(clazz);
    }

    /**
     * Resolves certain special interfaces to their default concrete implementations.
     *
     * @param type The type to resolve.
     * @return The default concrete implementation class for the given interface type, or the original type if no
     *         default is found.
     */
    private static Class<?> resolveType(final Class<?> type) {
        if (type.isAssignableFrom(AbstractMap.class)) {
            return HashMap.class;
        } else if (type.isAssignableFrom(List.class)) {
            return ArrayList.class;
        } else if (type == SortedSet.class) {
            return TreeSet.class;
        } else if (type.isAssignableFrom(Set.class)) {
            return HashSet.class;
        }

        return type;
    }

    /**
     * Creates a new instance of the object based on its type and available constructors. This method attempts to create
     * an instance in the following order:
     * <ol>
     * <li>If it's a primitive type, return its default value.</li>
     * <li>If it's an interface or abstract class, resolve to a default concrete implementation (e.g., {@code List} to
     * {@code ArrayList}).</li>
     * <li>Attempt to instantiate using a no-argument constructor.</li>
     * <li>If it's an enum, return the first enum constant.</li>
     * <li>If it's an array, return an empty array of that component type.</li>
     * <li>Iterate through all available constructors and attempt to instantiate using default values for
     * parameters.</li>
     * </ol>
     *
     * @return A new instance of type {@code T}, or {@code null} if instantiation fails.
     */
    @Override
    public T create() {
        Class<T> type = this.clazz;

        // Primitive types
        if (type.isPrimitive()) {
            return (T) ClassKit.getPrimitiveDefaultValue(type);
        }

        // Handle default implementations for interfaces and abstract classes
        type = (Class<T>) resolveType(type);

        // Attempt instantiation using a default (no-arg) constructor
        try {
            return DefaultObjectCreator.of(type).create();
        } catch (final Exception e) {
            // ignore
        }

        // Enums
        if (type.isEnum()) {
            return type.getEnumConstants()[0];
        }

        // Arrays
        if (type.isArray()) {
            return (T) Array.newInstance(type.getComponentType(), 0);
        }

        // Find a suitable constructor
        final Constructor<T>[] constructors = ReflectKit.getConstructors(type);
        Class<?>[] parameterTypes;
        for (final Constructor<T> constructor : constructors) {
            parameterTypes = constructor.getParameterTypes();
            if (0 == parameterTypes.length) {
                continue;
            }
            ReflectKit.setAccessible(constructor);
            try {
                return constructor.newInstance(ClassKit.getDefaultValues(parameterTypes));
            } catch (final Exception ignore) {
                // If constructor fails, continue to try the next one.
            }
        }
        return null;
    }

}
