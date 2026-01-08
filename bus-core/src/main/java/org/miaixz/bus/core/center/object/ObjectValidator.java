/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.object;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.text.CharsValidator;
import org.miaixz.bus.core.xyz.*;

/**
 * Object validation utility class, providing checks for object blankness and emptiness.
 * <ul>
 * <li>Empty definition: {@code null} or empty string: {@code ""}</li>
 * <li>Blank definition: {@code null} or empty string: {@code ""} or invisible characters like spaces, full-width
 * spaces, tabs, newlines.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ObjectValidator {

    /**
     * Checks if the given object is {@code null}.
     *
     * @param object The object to check.
     * @return {@code true} if the object is {@code null}, {@code false} otherwise.
     */
    public static boolean isNull(final Object object) {
        return null == object;
    }

    /**
     * Checks if the given object is not {@code null}.
     *
     * @param object The object to check.
     * @return {@code true} if the object is not {@code null}, {@code false} otherwise.
     */
    public static boolean isNotNull(final Object object) {
        return null != object;
    }

    /**
     * Determines if the specified object is empty. Supports the following types:
     * <ul>
     * <li>{@code null}: Returns {@code true} by default.</li>
     * <li>Array: Equivalent to {@link ArrayKit#isEmpty(Object)}.</li>
     * <li>{@link CharSequence}: Equivalent to {@link CharsBacker#isEmpty(CharSequence)}.</li>
     * <li>{@link Collection}: Equivalent to {@link CollKit#isEmpty(Collection)}.</li>
     * <li>{@link Map}: Equivalent to {@link MapKit#isEmpty(Map)}.</li>
     * <li>{@link Iterator} or {@link Iterable}: Equivalent to {@link IteratorKit#isEmpty(Iterator)},
     * {@link IteratorKit#isEmpty(Iterable)}.</li>
     * </ul>
     *
     * @param object The object to be checked.
     * @return {@code true} if the object is empty or its type is not supported (in which case it's considered not
     *         empty), {@code false} otherwise.
     * @see StringKit#isEmpty(CharSequence)
     * @see MapKit#isEmpty(Map)
     * @see IteratorKit#isEmpty(Iterable)
     * @see IteratorKit#isEmpty(Iterator)
     * @see ArrayKit#isEmpty(Object)
     */
    public static boolean isEmpty(final Object object) {
        if (null == object) {
            return true;
        }

        if (object instanceof CharSequence) {
            return StringKit.isEmpty((CharSequence) object);
        } else if (object instanceof Collection) {
            return CollKit.isEmpty((Collection) object);
        } else if (object instanceof Map) {
            return MapKit.isEmpty((Map) object);
        } else if (object instanceof Iterable) {
            return IteratorKit.isEmpty((Iterable) object);
        } else if (object instanceof Iterator) {
            return IteratorKit.isEmpty((Iterator) object);
        } else if (ArrayKit.isArray(object)) {
            return ArrayKit.isEmpty(object);
        }

        return false;
    }

    /**
     * Determines if the specified object is not empty.
     *
     * @param object The object to be checked.
     * @return {@code true} if the object is not empty, {@code false} otherwise.
     * @see #isEmpty(Object)
     */
    public static boolean isNotEmpty(final Object object) {
        return !isEmpty(object);
    }

    /**
     * Checks if an object, if it is a string, is blank. The definition of blank is as follows:
     * <ol>
     * <li>{@code null}</li>
     * <li>Empty string: {@code ""}</li>
     * <li>Invisible characters such as spaces, full-width spaces, tabs, newlines.</li>
     * </ol>
     * Examples:
     * <ul>
     * <li>{@code isBlankIfString(null)     // true}</li>
     * <li>{@code isBlankIfString("")       // true}</li>
     * <li>{@code isBlankIfString(" \t\n")  // true}</li>
     * <li>{@code isBlankIfString("abc")    // false}</li>
     * </ul>
     * Note: The difference between this method and {@link #isEmptyIfString(Object)} is that this method checks for
     * blank characters, and its performance is slightly slower than {@link #isEmptyIfString(Object)}.
     *
     * @param object The object to check.
     * @return {@code true} if the object is {@code null} or a blank {@link CharSequence}, {@code false} otherwise.
     * @see CharsValidator#isBlank(CharSequence)
     */
    public static boolean isBlankIfString(final Object object) {
        if (null == object) {
            return true;
        } else if (object instanceof CharSequence) {
            return CharsValidator.isBlank((CharSequence) object);
        }
        return false;
    }

    /**
     * Checks if an object, if it is a string, is empty. The definition of empty is as follows:
     * <ol>
     * <li>{@code null}</li>
     * <li>Empty string: {@code ""}</li>
     * </ol>
     * Examples:
     * <ul>
     * <li>{@code isEmptyIfString(null)     // true}</li>
     * <li>{@code isEmptyIfString("")       // true}</li>
     * <li>{@code isEmptyIfString(" \t\n")  // false}</li>
     * <li>{@code isEmptyIfString("abc")    // false}</li>
     * </ul>
     *
     * <p>
     * Note: The difference between this method and {@link #isBlankIfString(Object)} is that this method does not check
     * for blank characters.
     * 
     *
     * @param object The object to check.
     * @return {@code true} if the object is {@code null} or an empty {@link CharSequence}, {@code false} otherwise.
     */
    public static boolean isEmptyIfString(final Object object) {
        if (null == object) {
            return true;
        } else if (object instanceof CharSequence) {
            return ((CharSequence) object).isEmpty();
        }
        return false;
    }

    /**
     * Returns a default value if the given object is {@code null}.
     * 
     * <pre>{@code
     * ObjectKit.defaultIfNull(null, null);      // = null
     * ObjectKit.defaultIfNull(null, "");        // = ""
     * ObjectKit.defaultIfNull(null, "zz");      // = "zz"
     * ObjectKit.defaultIfNull("abc", *);        // = "abc"
     * ObjectKit.defaultIfNull(Boolean.TRUE, *); // = Boolean.TRUE
     * }</pre>
     *
     * @param <T>          The type of the object.
     * @param object       The object to check, which may be {@code null}.
     * @param defaultValue The default value to return if the object is {@code null}. Can be {@code null}.
     * @return The object itself if it is not {@code null}, otherwise the default value.
     */
    public static <T> T defaultIfNull(final T object, final T defaultValue) {
        return isNull(object) ? defaultValue : object;
    }

    /**
     * Returns the original value if the given object is not {@code null}, otherwise returns the default value provided
     * by {@link Supplier#get()}.
     *
     * @param <T>             The type of the object being checked.
     * @param source          The object to check, which may be {@code null}.
     * @param defaultSupplier The supplier for the default value when the object is {@code null}.
     * @return The object itself if it is not {@code null}, otherwise the default value from the supplier.
     */
    public static <T> T defaultIfNull(final T source, final Supplier<? extends T> defaultSupplier) {
        if (isNotNull(source)) {
            return source;
        }
        return defaultSupplier.get();
    }

    /**
     * Returns the result of a custom handler if the given object is not {@code null}, otherwise returns a default
     * value.
     *
     * @param <R>          The return type.
     * @param <T>          The type of the object being checked.
     * @param source       The object to check, which may be {@code null}.
     * @param handler      The custom handler method to apply when the object is not {@code null}.
     * @param defaultValue The default return value when the object is {@code null}.
     * @return The result of the handler if the object is not {@code null}, otherwise the default value.
     */
    public static <T, R> R defaultIfNull(
            final T source,
            final Function<? super T, ? extends R> handler,
            final R defaultValue) {
        return isNull(source) ? defaultValue : handler.apply(source);
    }

    /**
     * Returns the result of a custom handler if the given object is not {@code null}, otherwise returns the default
     * value provided by {@link Supplier#get()}.
     *
     * @param <R>             The return type.
     * @param <T>             The type of the object being checked.
     * @param source          The object to check, which may be {@code null}.
     * @param handler         The custom handler method to apply when the object is not {@code null}.
     * @param defaultSupplier The supplier for the default value when the object is {@code null}.
     * @return The result of the handler if the object is not {@code null}, otherwise the default value from the
     *         supplier.
     */
    public static <T, R> R defaultIfNull(
            final T source,
            final Function<? super T, ? extends R> handler,
            final Supplier<? extends R> defaultSupplier) {
        if (isNotNull(source)) {
            return handler.apply(source);
        }
        return defaultSupplier.get();
    }

    /**
     * Compares two objects for equality. Returns {@code true} if any of the following conditions are met:
     * <ul>
     * <li>If both objects are {@link BigDecimal}, and {@code 0 == obj1.compareTo(obj2)}.</li>
     * <li>If both objects are arrays, comparison is done using {@link ArrayKit#equals(Object, Object)}.</li>
     * <li>{@code obj1 == null && obj2 == null}.</li>
     * <li>{@code obj1.equals(obj2)}.</li>
     * </ul>
     *
     * @param obj1 The first object to compare.
     * @param obj2 The second object to compare.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    public static boolean equals(final Object obj1, final Object obj2) {
        if (obj1 instanceof Number && obj2 instanceof Number) {
            return MathKit.equals((Number) obj1, (Number) obj2);
        } else if (ArrayKit.isArray(obj1) && ArrayKit.isArray(obj2)) {
            return ArrayKit.equals(obj1, obj2);
        }
        return Objects.equals(obj1, obj2);
    }

    /**
     * Checks if an object is equal to any of the given objects in an array.
     *
     * @param object  The object to check.
     * @param objects The array of objects to compare against.
     * @return {@code true} if the object is equal to any object in the array, {@code false} otherwise.
     * @throws IllegalArgumentException If the objects array is {@code null} or empty.
     */
    public static boolean equalsAny(Object object, Object... objects) {
        if (objects == null || objects.length == 0) {
            throw new IllegalArgumentException("objects must not be null or empty.");
        }
        for (Object o : objects) {
            if (Objects.equals(object, o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compares two objects for inequality.
     *
     * @param obj1 The first object to compare.
     * @param obj2 The second object to compare.
     * @return {@code true} if the objects are not equal, {@code false} otherwise.
     * @see #equals(Object, Object)
     */
    public static boolean notEquals(final Object obj1, final Object obj2) {
        return !equals(obj1, obj2);
    }

    /**
     * Checks if the given object is a basic type, including wrapper types and primitive types.
     *
     * @param object The object to check. Returns {@code false} if {@code null}.
     * @return {@code true} if the object is a basic type, {@code false} otherwise.
     * @see ClassKit#isBasicType(Class)
     */
    public static boolean isBasicType(final Object object) {
        if (null == object) {
            return false;
        }
        return ClassKit.isBasicType(object.getClass());
    }

    /**
     * Checks if the given object is a valid number, primarily used to check if floating-point numbers are meaningful
     * values. If the object is not of type {@link Number}, it directly returns {@code true}. Otherwise:
     * <ul>
     * <li>If the object type is {@link Double}, it checks {@link Double#isInfinite()} or {@link Double#isNaN()}.</li>
     * <li>If the object type is {@link Float}, it checks {@link Float#isInfinite()} or {@link Float#isNaN()}.</li>
     * </ul>
     *
     * @param object The object to check.
     * @return {@code true} if the object is a valid number or not a number type, {@code false} otherwise.
     * @see MathKit#isValidNumber(Number)
     */
    public static boolean isValidIfNumber(final Object object) {
        if (object instanceof Number) {
            return MathKit.isValidNumber((Number) object);
        }
        return true;
    }

}
