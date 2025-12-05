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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import org.miaixz.bus.core.center.object.ObjectValidator;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Object utility class, including null checks, cloning, serialization, etc. For array-related operations, see:
 * {@link ArrayKit}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ObjectKit extends ObjectValidator {

    /**
     * Calculates the length of an object. Supported types include:
     * <ul>
     * <li>{@code null}: returns {@code 0} by default.</li>
     * <li>Array: returns the array length.</li>
     * <li>{@link CharSequence}: returns {@link CharSequence#length()}.</li>
     * <li>{@link Collection}: returns {@link Collection#size()}.</li>
     * <li>{@link Map}: returns {@link Map#size()}.</li>
     * <li>{@link Iterator} or {@link Iterable}: returns the count of iterable elements. Side effect: {@link Iterator}
     * can only be iterated once.</li>
     * <li>{@link Enumeration}: returns the count of iterable elements. Side effect: {@link Enumeration} can only be
     * iterated once.</li>
     * </ul>
     *
     * @param object The object whose length is to be calculated.
     * @return The length of the object, or -1 if the type is not supported.
     */
    public static int length(final Object object) {
        if (object == null) {
            return 0;
        }
        if (object instanceof CharSequence) {
            return ((CharSequence) object).length();
        }
        if (object instanceof Collection) {
            return ((Collection<?>) object).size();
        }
        if (object instanceof Map) {
            return ((Map<?, ?>) object).size();
        }

        int count = 0;
        if (object instanceof Iterator || object instanceof Iterable) {
            final Iterator<?> iter = (object instanceof Iterator) ? (Iterator<?>) object
                    : ((Iterable<?>) object).iterator();
            while (iter.hasNext()) {
                count++;
                iter.next();
            }
            return count;
        }
        if (object.getClass().isArray()) {
            return Array.getLength(object);
        }
        if (object instanceof final Enumeration<?> enumeration) {
            while (enumeration.hasMoreElements()) {
                count++;
                enumeration.nextElement();
            }
            return count;
        }
        return -1;
    }

    /**
     * Checks if `object` contains `element`. Supported types include:
     * <ul>
     * <li>{@code null}: always returns {@code false}.</li>
     * <li>{@link String}: equivalent to {@link String#contains(CharSequence)}.</li>
     * <li>{@link Collection}: equivalent to {@link Collection#contains(Object)}.</li>
     * <li>{@link Map}: equivalent to {@link Map#containsValue(Object)}.</li>
     * <li>{@link Iterator}, {@link Iterable}, {@link Enumeration}, or Array: iterates and checks for equality.</li>
     * </ul>
     *
     * @param object  The object to check within.
     * @param element The element to find.
     * @return `true` if the object contains the element.
     */
    public static boolean contains(final Object object, final Object element) {
        if (object == null) {
            return false;
        }
        if (object instanceof CharSequence) {
            if (!(element instanceof CharSequence)) {
                return false;
            }
            final String elementStr;
            try {
                elementStr = element.toString();
                // Check if toString() returns null
            } catch (final Exception e) {
                // If toString throws an exception, treat as not contained
                return false;
            }
            if (null == elementStr) {
                return false;
            }
            return object.toString().contains(elementStr);
        }
        if (object instanceof Collection) {
            return ((Collection<?>) object).contains(element);
        }
        if (object instanceof Map) {
            return ((Map<?, ?>) object).containsValue(element);
        }

        if (object instanceof Iterator || object instanceof Iterable) {
            final Iterator<?> iter = object instanceof Iterator ? (Iterator<?>) object
                    : ((Iterable<?>) object).iterator();
            while (iter.hasNext()) {
                final Object o = iter.next();
                if (equals(o, element)) {
                    return true;
                }
            }
            return false;
        }
        if (object instanceof final Enumeration<?> enumeration) {
            while (enumeration.hasMoreElements()) {
                final Object o = enumeration.nextElement();
                if (equals(o, element)) {
                    return true;
                }
            }
            return false;
        }
        if (ArrayKit.isArray(object)) {
            final int len = Array.getLength(object);
            for (int i = 0; i < len; i++) {
                final Object o = Array.get(object, i);
                if (equals(o, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * If the specified object is not `null`, applies the provided mapping function and returns the result, otherwise
     * returns `null`.
     *
     * @param source  The object to check.
     * @param handler The mapping function to apply.
     * @param <T>     The type of the input object.
     * @param <R>     The return type of the mapping function.
     * @return The result of the mapping function, or `null` if the input object is `null`.
     */
    public static <T, R> R apply(final T source, final Function<T, R> handler) {
        return defaultIfNull(source, handler, (R) null);
    }

    /**
     * If the specified object is not `null`, executes the {@link Consumer} on it.
     *
     * @param source   The object to check.
     * @param consumer The logic to execute on the source object.
     * @param <T>      The type of the input object.
     */
    public static <T> void accept(final T source, final Consumer<T> consumer) {
        if (null != source) {
            consumer.accept(source);
        }
    }

    /**
     * Clones an object.
     * <ol>
     * <li>If the object is an array, it is cloned using {@link ArrayKit#clone(Object)}.</li>
     * <li>If the object implements {@link Cloneable}, `Object.clone()` is called.</li>
     * <li>If the object implements {@link Serializable}, a deep clone is performed via serialization.</li>
     * <li>Otherwise, returns `null`.</li>
     * </ol>
     *
     * @param <T>    The type of the object.
     * @param object The object to be cloned.
     * @return The cloned object.
     */
    public static <T> T clone(final T object) {
        final T result = ArrayKit.clone(object);
        if (null != result) {
            // Array
            return result;
        }

        if (object instanceof Cloneable) {
            try {
                return MethodKit.invoke(object, "clone");
            } catch (final InternalException e) {
                // In JDK9+, access may be denied.
                if (e.getCause() instanceof IllegalAccessException) {
                    return cloneByStream(object);
                } else {
                    throw e;
                }
            }
        }

        return cloneByStream(object);
    }

    /**
     * Returns a clone of the object if possible, otherwise returns the original object.
     *
     * @param <T>    The type of the object.
     * @param object The object.
     * @return The cloned object or the original object if cloning fails.
     * @see #clone(Object)
     */
    public static <T> T cloneIfPossible(final T object) {
        T clone = null;
        try {
            clone = clone(object);
        } catch (final Exception e) {
            // pass
        }
        return clone == null ? object : clone;
    }

    /**
     * Clones an object via serialization. Returns `null` if the object does not implement {@link Serializable}.
     *
     * @param <T>    The type of the object.
     * @param object The object to be cloned.
     * @return The cloned object.
     * @throws InternalException wrapping IOExceptions and ClassNotFoundExceptions.
     * @see SerializeKit#clone(Object)
     */
    public static <T> T cloneByStream(final T object) {
        return SerializeKit.clone(object);
    }

    /**
     * Gets the first generic type argument of the given object's class.
     *
     * @param object The object to inspect.
     * @return The {@link Class} of the generic type argument.
     */
    public static Class<?> getTypeArgument(final Object object) {
        return getTypeArgument(object, 0);
    }

    /**
     * Gets the generic type argument of the given object's class at a specified index.
     *
     * @param object The object to inspect.
     * @param index  The index of the generic type argument.
     * @return The {@link Class} of the generic type argument.
     * @see ClassKit#getTypeArgument(Class, int)
     */
    public static Class<?> getTypeArgument(final Object object, final int index) {
        return ClassKit.getTypeArgument(object.getClass(), index);
    }

    /**
     * Determines if two objects are equal, handling `null`s safely.
     *
     * @param o1 The first object to compare.
     * @param o2 The second object to compare.
     * @return `true` if the given objects are equal.
     * @see Object#equals(Object)
     * @see Arrays#equals
     */
    public static boolean nullSafeEquals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (null == o1 || null == o2) {
            return false;
        }
        if (o1.equals(o2)) {
            return true;
        }
        if (o1.getClass().isArray() && o2.getClass().isArray()) {
            return ArrayKit.arrayEquals(o1, o2);
        }
        return false;
    }

}
