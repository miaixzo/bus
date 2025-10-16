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
package org.miaixz.bus.core.lang;

import java.util.Map;
import java.util.function.Supplier;

import org.miaixz.bus.core.xyz.*;

/**
 * Assertion utility class that assists in validating arguments. Throws an {@link IllegalArgumentException} if an
 * argument fails an assertion. This class is commonly used for parameter checking.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Assert {

    /**
     * Template for error messages when a value is out of bounds.
     */
    private static final String TEMPLATE_VALUE_MUST_BE_BETWEEN_AND = "The value must be between {} and {}.";

    /**
     * Asserts that an expression is true. If the expression is {@code false}, a custom exception provided by the
     * supplier is thrown.
     *
     * <pre class="code">
     * Assert.isTrue(i &gt; 0, IllegalArgumentException::new);
     * </pre>
     *
     * @param <X>        The type of exception to throw.
     * @param expression A boolean expression.
     * @param supplier   A supplier for the exception to throw if the expression is {@code false}.
     * @throws X if expression is {@code false}
     */
    public static <X extends Throwable> void isTrue(final boolean expression, final Supplier<? extends X> supplier)
            throws X {
        if (!expression) {
            throw supplier.get();
        }
    }

    /**
     * Asserts that an expression is true. If the expression is {@code false}, an {@link IllegalArgumentException} is
     * thrown with a formatted error message.
     *
     * <pre class="code">
     * Assert.isTrue(i &gt; 0, "The value must be greater than zero");
     * </pre>
     *
     * @param expression A boolean expression.
     * @param format     The error message template, with `{}` as placeholders for arguments.
     * @param args       The arguments to fill into the error message template.
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isTrue(final boolean expression, final String format, final Object... args)
            throws IllegalArgumentException {
        isTrue(expression, () -> new IllegalArgumentException(StringKit.format(format, args)));
    }

    /**
     * Asserts that an expression is true. If the expression is {@code false}, an {@link IllegalArgumentException} is
     * thrown with a default error message.
     *
     * <pre class="code">
     * Assert.isTrue(i &gt; 0);
     * </pre>
     *
     * @param expression A boolean expression.
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isTrue(final boolean expression) throws IllegalArgumentException {
        isTrue(expression, "[Assertion failed] - this expression must be true");
    }

    /**
     * Asserts that an expression is false. If the expression is {@code true}, a custom exception provided by the
     * supplier is thrown.
     * 
     * <pre class="code">
     * Assert.isFalse(i &gt; 0, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return");
     * });
     * </pre>
     *
     * @param <X>           The type of exception to throw.
     * @param expression    A boolean expression.
     * @param errorSupplier A supplier for the exception to throw if the expression is {@code true}.
     * @throws X if expression is {@code true}
     */
    public static <X extends Throwable> void isFalse(final boolean expression, final Supplier<X> errorSupplier)
            throws X {
        if (expression) {
            throw errorSupplier.get();
        }
    }

    /**
     * Asserts that an expression is false. If the expression is {@code true}, an {@link IllegalArgumentException} is
     * thrown with a formatted error message.
     *
     * <pre class="code">
     * Assert.isFalse(i &lt; 0, "The value must not be negative");
     * </pre>
     *
     * @param expression A boolean expression.
     * @param format     The error message template, with `{}` as placeholders for arguments.
     * @param args       The arguments to fill into the error message template.
     * @throws IllegalArgumentException if expression is {@code true}
     */
    public static void isFalse(final boolean expression, final String format, final Object... args)
            throws IllegalArgumentException {
        isFalse(expression, () -> new IllegalArgumentException(StringKit.format(format, args)));
    }

    /**
     * Asserts that an expression is false. If the expression is {@code true}, an {@link IllegalArgumentException} is
     * thrown with a default error message.
     *
     * <pre class="code">
     * Assert.isFalse(i &lt; 0);
     * </pre>
     *
     * @param expression A boolean expression.
     * @throws IllegalArgumentException if expression is {@code true}
     */
    public static void isFalse(final boolean expression) throws IllegalArgumentException {
        isFalse(expression, "[Assertion failed] - this expression must be false");
    }

    /**
     * Asserts that an object is {@code null}. If the object is not {@code null}, a custom exception provided by the
     * supplier is thrown.
     * 
     * <pre class="code">
     * Assert.isNull(value, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return");
     * });
     * </pre>
     *
     * @param <X>           The type of exception to throw.
     * @param object        The object to check.
     * @param errorSupplier A supplier for the exception to throw if the object is not {@code null}.
     * @throws X if the object is not {@code null}
     */
    public static <X extends Throwable> void isNull(final Object object, final Supplier<X> errorSupplier) throws X {
        if (null != object) {
            throw errorSupplier.get();
        }
    }

    /**
     * Asserts that an object is {@code null}. If the object is not {@code null}, an {@link IllegalArgumentException} is
     * thrown with a formatted error message.
     * 
     * <pre class="code">
     * Assert.isNull(value, "The value must be null");
     * </pre>
     *
     * @param object The object to check.
     * @param format The error message template, with `{}` as placeholders for arguments.
     * @param args   The arguments to fill into the error message template.
     * @throws IllegalArgumentException if the object is not {@code null}
     */
    public static void isNull(final Object object, final String format, final Object... args)
            throws IllegalArgumentException {
        isNull(object, () -> new IllegalArgumentException(StringKit.format(format, args)));
    }

    /**
     * Asserts that an object is {@code null}. If the object is not {@code null}, an {@link IllegalArgumentException} is
     * thrown with a default error message.
     * 
     * <pre class="code">
     * Assert.isNull(value);
     * </pre>
     *
     * @param object The object to check.
     * @throws IllegalArgumentException if the object is not {@code null}
     */
    public static void isNull(final Object object) throws IllegalArgumentException {
        isNull(object, "[Assertion failed] - the object argument must be null");
    }

    /**
     * Asserts that an object is not {@code null}. If the object is {@code null}, a custom exception provided by the
     * supplier is thrown.
     * 
     * <pre class="code">
     * Assert.notNull(clazz, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return");
     * });
     * </pre>
     *
     * @param <T>           The type of the object being checked.
     * @param <X>           The type of exception to throw.
     * @param object        The object to check.
     * @param errorSupplier A supplier for the exception to throw if the object is {@code null}.
     * @return The non-{@code null} object.
     * @throws X if the object is {@code null}
     */
    public static <T, X extends Throwable> T notNull(final T object, final Supplier<X> errorSupplier) throws X {
        if (null == object) {
            throw errorSupplier.get();
        }
        return object;
    }

    /**
     * Asserts that an object is not {@code null}. If the object is {@code null}, an {@link IllegalArgumentException} is
     * thrown with a formatted error message.
     * 
     * <pre>{@code
     * Assert.notNull(clazz, "The class must not be null");
     * }</pre>
     *
     * @param <T>    The type of the object being checked.
     * @param object The object to check.
     * @param format The error message template, with `{}` as placeholders for arguments.
     * @param args   The arguments to fill into the error message template.
     * @return The non-{@code null} object.
     * @throws IllegalArgumentException if the object is {@code null}
     */
    public static <T> T notNull(final T object, final String format, final Object... args)
            throws IllegalArgumentException {
        if (null == object) {
            throw new IllegalArgumentException(StringKit.format(format, args));
        }
        return object;
    }

    /**
     * Asserts that an object is not {@code null}. If the object is {@code null}, an {@link IllegalArgumentException} is
     * thrown with a default error message.
     * 
     * <pre>{@code
     * Assert.notNull(clazz);
     * }</pre>
     *
     * @param <T>    The type of the object being checked.
     * @param object The object to check.
     * @return The non-{@code null} object.
     * @throws IllegalArgumentException if the object is {@code null}
     */
    public static <T> T notNull(final T object) throws IllegalArgumentException {
        if (null == object) {
            throw new IllegalArgumentException("[Assertion failed] - this argument is required; it must not be null");
        }
        return object;
    }

    /**
     * Asserts that an array is not empty (i.e., not {@code null} and contains at least one element). If the array is
     * empty, a custom exception provided by the supplier is thrown.
     * 
     * <pre class="code">
     * Assert.notEmpty(array, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return");
     * });
     * </pre>
     *
     * @param <T>           The component type of the array.
     * @param <X>           The type of exception to throw.
     * @param array         The array to check.
     * @param errorSupplier A supplier for the exception to throw if the array is empty.
     * @return The non-empty array.
     * @throws X if the object array is {@code null} or has no elements
     * @see ArrayKit#isNotEmpty(Object[])
     */
    public static <T, X extends Throwable> T[] notEmpty(final T[] array, final Supplier<X> errorSupplier) throws X {
        if (ArrayKit.isEmpty(array)) {
            throw errorSupplier.get();
        }
        return array;
    }

    /**
     * Asserts that an array is not empty (i.e., not {@code null} and contains at least one element). If the array is
     * empty, an {@link IllegalArgumentException} is thrown with a formatted error message.
     * 
     * <pre class="code">
     * Assert.notEmpty(array, "The array must have elements");
     * </pre>
     *
     * @param <T>    The component type of the array.
     * @param array  The array to check.
     * @param format The error message template, with `{}` as placeholders for arguments.
     * @param args   The arguments to fill into the error message template.
     * @return The non-empty array.
     * @throws IllegalArgumentException if the object array is {@code null} or has no elements
     */
    public static <T> T[] notEmpty(final T[] array, final String format, final Object... args)
            throws IllegalArgumentException {
        return notEmpty(array, () -> new IllegalArgumentException(StringKit.format(format, args)));
    }

    /**
     * Asserts that an array is not empty (i.e., not {@code null} and contains at least one element). If the array is
     * empty, an {@link IllegalArgumentException} is thrown with a default error message.
     * 
     * <pre class="code">
     * Assert.notEmpty(array, "The array must have elements");
     * </pre>
     *
     * @param <T>   The component type of the array.
     * @param array The array to check.
     * @return The non-empty array.
     * @throws IllegalArgumentException if the object array is {@code null} or has no elements
     */
    public static <T> T[] notEmpty(final T[] array) throws IllegalArgumentException {
        return notEmpty(array, "[Assertion failed] - this array must not be empty: it must contain at least 1 element");
    }

    /**
     * Asserts that a collection is not empty (i.e., not {@code null} and contains at least one element). If the
     * collection is empty, a custom exception provided by the supplier is thrown.
     * 
     * <pre class="code">
     * Assert.notEmpty(collection, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return");
     * });
     * </pre>
     *
     * @param <E>           The type of elements in the collection.
     * @param <T>           The type of the collection.
     * @param <X>           The type of exception to throw.
     * @param collection    The collection to check.
     * @param errorSupplier A supplier for the exception to throw if the collection is empty.
     * @return The non-empty collection.
     * @throws X if the collection is {@code null} or has no elements
     * @see CollKit#isNotEmpty(Iterable)
     */
    public static <E, T extends Iterable<E>, X extends Throwable> T notEmpty(
            final T collection,
            final Supplier<X> errorSupplier) throws X {
        if (CollKit.isEmpty(collection)) {
            throw errorSupplier.get();
        }
        return collection;
    }

    /**
     * Asserts that a collection is not empty (i.e., not {@code null} and contains at least one element). If the
     * collection is empty, an {@link IllegalArgumentException} is thrown with a formatted error message.
     * 
     * <pre class="code">
     * Assert.notEmpty(collection, "Collection must have elements");
     * </pre>
     *
     * @param <E>        The type of elements in the collection.
     * @param <T>        The type of the collection.
     * @param collection The collection to check.
     * @param format     The error message template, with `{}` as placeholders for arguments.
     * @param args       The arguments to fill into the error message template.
     * @return The non-empty collection.
     * @throws IllegalArgumentException if the collection is {@code null} or has no elements
     */
    public static <E, T extends Iterable<E>> T notEmpty(final T collection, final String format, final Object... args)
            throws IllegalArgumentException {
        return notEmpty(collection, () -> new IllegalArgumentException(StringKit.format(format, args)));
    }

    /**
     * Asserts that a collection is not empty (i.e., not {@code null} and contains at least one element). If the
     * collection is empty, an {@link IllegalArgumentException} is thrown with a default error message.
     * 
     * <pre class="code">
     * Assert.notEmpty(collection);
     * </pre>
     *
     * @param <E>        The type of elements in the collection.
     * @param <T>        The type of the collection.
     * @param collection The collection to check.
     * @return The non-empty collection.
     * @throws IllegalArgumentException if the collection is {@code null} or has no elements
     */
    public static <E, T extends Iterable<E>> T notEmpty(final T collection) throws IllegalArgumentException {
        return notEmpty(
                collection,
                "[Assertion failed] - this collection must not be empty: it must contain at least 1 element");
    }

    /**
     * Asserts that a map is not empty (i.e., not {@code null} and contains at least one entry). If the map is empty, a
     * custom exception provided by the supplier is thrown.
     * 
     * <pre class="code">
     * Assert.notEmpty(map, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return");
     * });
     * </pre>
     *
     * @param <K>           The type of keys in the map.
     * @param <V>           The type of values in the map.
     * @param <T>           The type of the map.
     * @param <X>           The type of exception to throw.
     * @param map           The map to check.
     * @param errorSupplier A supplier for the exception to throw if the map is empty.
     * @return The non-empty map.
     * @throws X if the map is {@code null} or has no entries
     * @see MapKit#isNotEmpty(Map)
     */
    public static <K, V, T extends Map<K, V>, X extends Throwable> T notEmpty(
            final T map,
            final Supplier<X> errorSupplier) throws X {
        if (MapKit.isEmpty(map)) {
            throw errorSupplier.get();
        }
        return map;
    }

    /**
     * Asserts that a map is not empty (i.e., not {@code null} and contains at least one entry). If the map is empty, an
     * {@link IllegalArgumentException} is thrown with a formatted error message.
     * 
     * <pre class="code">
     * Assert.notEmpty(map, "Map must have entries");
     * </pre>
     *
     * @param <K>    The type of keys in the map.
     * @param <V>    The type of values in the map.
     * @param <T>    The type of the map.
     * @param map    The map to check.
     * @param format The error message template, with `{}` as placeholders for arguments.
     * @param args   The arguments to fill into the error message template.
     * @return The non-empty map.
     * @throws IllegalArgumentException if the map is {@code null} or has no entries
     */
    public static <K, V, T extends Map<K, V>> T notEmpty(final T map, final String format, final Object... args)
            throws IllegalArgumentException {
        return notEmpty(map, () -> new IllegalArgumentException(StringKit.format(format, args)));
    }

    /**
     * Asserts that a map is not empty (i.e., not {@code null} and contains at least one entry). If the map is empty, an
     * {@link IllegalArgumentException} is thrown with a default error message.
     * 
     * <pre class="code">
     * Assert.notEmpty(map, "Map must have entries");
     * </pre>
     *
     * @param <K> The type of keys in the map.
     * @param <V> The type of values in the map.
     * @param <T> The type of the map.
     * @param map The map to check.
     * @return The non-empty map.
     * @throws IllegalArgumentException if the map is {@code null} or has no entries
     */
    public static <K, V, T extends Map<K, V>> T notEmpty(final T map) throws IllegalArgumentException {
        return notEmpty(map, "[Assertion failed] - this map must not be empty; it must contain at least one entry");
    }

    /**
     * Checks that the given character sequence is not empty (i.e., not {@code null} and has a length greater than
     * zero). If the character sequence is empty, a custom exception provided by the supplier is thrown.
     * 
     * <pre class="code">
     * Assert.notEmpty(name, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return");
     * });
     * </pre>
     *
     * @param <X>           The type of exception to throw.
     * @param <T>           The type of the character sequence.
     * @param text          The character sequence to check.
     * @param errorSupplier A supplier for the exception to throw if the character sequence is empty.
     * @return The non-empty character sequence.
     * @throws X if the checked character sequence is empty.
     * @see StringKit#isNotEmpty(CharSequence)
     */
    public static <T extends CharSequence, X extends Throwable> T notEmpty(
            final T text,
            final Supplier<X> errorSupplier) throws X {
        if (StringKit.isEmpty(text)) {
            throw errorSupplier.get();
        }
        return text;
    }

    /**
     * Checks that the given character sequence is not empty (i.e., not {@code null} and has a length greater than
     * zero). If the character sequence is empty, an {@link IllegalArgumentException} is thrown with a formatted error
     * message.
     *
     * <pre class="code">
     * Assert.notEmpty(name, "Name must not be empty");
     * </pre>
     *
     * @param <T>    The type of the character sequence.
     * @param text   The character sequence to check.
     * @param format The error message template, with `{}` as placeholders for arguments.
     * @param args   The arguments to fill into the error message template.
     * @return The non-empty character sequence.
     * @throws IllegalArgumentException if the checked character sequence is empty.
     * @see StringKit#isNotEmpty(CharSequence)
     */
    public static <T extends CharSequence> T notEmpty(final T text, final String format, final Object... args)
            throws IllegalArgumentException {
        return notEmpty(text, () -> new IllegalArgumentException(StringKit.format(format, args)));
    }

    /**
     * Checks that the given character sequence is not empty (i.e., not {@code null} and has a length greater than
     * zero). If the character sequence is empty, an {@link IllegalArgumentException} is thrown with a default error
     * message.
     *
     * <pre class="code">
     * Assert.notEmpty(name);
     * </pre>
     *
     * @param <T>  The type of the character sequence.
     * @param text The character sequence to check.
     * @return The non-empty character sequence.
     * @throws IllegalArgumentException if the checked character sequence is empty.
     * @see StringKit#isNotEmpty(CharSequence)
     */
    public static <T extends CharSequence> T notEmpty(final T text) throws IllegalArgumentException {
        return notEmpty(
                text,
                "[Assertion failed] - this String argument must have length; it must not be null or empty");
    }

    /**
     * Checks that the given character sequence is not blank (i.e., not {@code null}, not empty, and not containing only
     * whitespace). If the character sequence is blank, a custom exception provided by the supplier is thrown.
     * 
     * <pre class="code">
     * Assert.notBlank(name, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return");
     * });
     * </pre>
     *
     * @param <X>              The type of exception to throw.
     * @param <T>              The type of the character sequence.
     * @param text             The character sequence to check.
     * @param errorMsgSupplier A supplier for the exception to throw if the character sequence is blank.
     * @return The non-blank character sequence.
     * @throws X if the checked character sequence is blank.
     * @see StringKit#isNotBlank(CharSequence)
     */
    public static <T extends CharSequence, X extends Throwable> T notBlank(
            final T text,
            final Supplier<X> errorMsgSupplier) throws X {
        if (StringKit.isBlank(text)) {
            throw errorMsgSupplier.get();
        }
        return text;
    }

    /**
     * Checks that the given character sequence is not blank (i.e., not {@code null}, not empty, and not containing only
     * whitespace). If the character sequence is blank, an {@link IllegalArgumentException} is thrown with a formatted
     * error message.
     *
     * <pre class="code">
     * Assert.notBlank(name, "Name must not be blank");
     * </pre>
     *
     * @param <T>    The type of the character sequence.
     * @param text   The character sequence to check.
     * @param format The error message template, with `{}` as placeholders for arguments.
     * @param args   The arguments to fill into the error message template.
     * @return The non-blank character sequence.
     * @throws IllegalArgumentException if the checked character sequence is blank.
     * @see StringKit#isNotBlank(CharSequence)
     */
    public static <T extends CharSequence> T notBlank(final T text, final String format, final Object... args)
            throws IllegalArgumentException {
        return notBlank(text, () -> new IllegalArgumentException(StringKit.format(format, args)));
    }

    /**
     * Checks that the given character sequence is not blank (i.e., not {@code null}, not empty, and not containing only
     * whitespace). If the character sequence is blank, an {@link IllegalArgumentException} is thrown with a default
     * error message.
     *
     * <pre class="code">
     * Assert.notBlank(name);
     * </pre>
     *
     * @param <T>  The type of the character sequence.
     * @param text The character sequence to check.
     * @return The non-blank character sequence.
     * @throws IllegalArgumentException if the checked character sequence is blank.
     * @see StringKit#isNotBlank(CharSequence)
     */
    public static <T extends CharSequence> T notBlank(final T text) throws IllegalArgumentException {
        return notBlank(
                text,
                "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
    }

    /**
     * Asserts that a given string does not contain a specified substring. If it does, a custom exception provided by
     * the supplier is thrown.
     * 
     * <pre class="code">
     * Assert.notContain(name, "rod", () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return ");
     * });
     * </pre>
     *
     * @param <T>           The type of the substring.
     * @param <X>           The type of exception to throw.
     * @param textToSearch  The string to search within.
     * @param substring     The substring to check for absence.
     * @param errorSupplier A supplier for the exception to throw if the substring is found.
     * @return The checked substring.
     * @throws X if the substring is found within the textToSearch.
     * @see StringKit#contains(CharSequence, CharSequence)
     */
    public static <T extends CharSequence, X extends Throwable> T notContain(
            final CharSequence textToSearch,
            final T substring,
            final Supplier<X> errorSupplier) throws X {
        if (StringKit.contains(textToSearch, substring)) {
            throw errorSupplier.get();
        }
        return substring;
    }

    /**
     * Asserts that a given string does not contain a specified substring. If it does, an
     * {@link IllegalArgumentException} is thrown with a formatted error message.
     * 
     * <pre class="code">
     * Assert.notContain(name, "rod", "Name must not contain 'rod'");
     * </pre>
     *
     * @param textToSearch The string to search within.
     * @param subString    The substring to check for absence.
     * @param format       The error message template, with `{}` as placeholders for arguments.
     * @param args         The arguments to fill into the error message template.
     * @return The checked substring.
     * @throws IllegalArgumentException if the substring is found within the textToSearch.
     */
    public static String notContain(
            final String textToSearch,
            final String subString,
            final String format,
            final Object... args) throws IllegalArgumentException {
        return notContain(textToSearch, subString, () -> new IllegalArgumentException(StringKit.format(format, args)));
    }

    /**
     * Asserts that a given string does not contain a specified substring. If it does, an
     * {@link IllegalArgumentException} is thrown with a default error message.
     * 
     * <pre class="code">
     * Assert.notContain(name, "rod");
     * </pre>
     *
     * @param textToSearch The string to search within.
     * @param subString    The substring to check for absence.
     * @return The checked substring.
     * @throws IllegalArgumentException if the substring is found within the textToSearch.
     */
    public static String notContain(final String textToSearch, final String subString) throws IllegalArgumentException {
        return notContain(
                textToSearch,
                subString,
                "[Assertion failed] - this String argument must not contain the substring [{}]",
                subString);
    }

    /**
     * Asserts that an array does not contain any {@code null} elements. If it does, a custom exception provided by the
     * supplier is thrown. An empty or {@code null} array is considered not to contain {@code null} elements.
     * 
     * <pre class="code">
     * Assert.noNullElements(array, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return ");
     * });
     * </pre>
     *
     * @param <T>           The component type of the array.
     * @param <X>           The type of exception to throw.
     * @param array         The array to check.
     * @param errorSupplier A supplier for the exception to throw if a {@code null} element is found.
     * @return The checked array.
     * @throws X if the object array contains a {@code null} element
     * @see ArrayKit#hasNull(Object[])
     */
    public static <T, X extends Throwable> T[] noNullElements(final T[] array, final Supplier<X> errorSupplier)
            throws X {
        if (ArrayKit.hasNull(array)) {
            throw errorSupplier.get();
        }
        return array;
    }

    /**
     * Asserts that an array does not contain any {@code null} elements. If it does, an {@link IllegalArgumentException}
     * is thrown with a formatted error message. An empty or {@code null} array is considered not to contain
     * {@code null} elements.
     * 
     * <pre class="code">
     * Assert.noNullElements(array, "The array must not have null elements");
     * </pre>
     *
     * @param <T>    The component type of the array.
     * @param array  The array to check.
     * @param format The error message template, with `{}` as placeholders for arguments.
     * @param args   The arguments to fill into the error message template.
     * @return The checked array.
     * @throws IllegalArgumentException if the object array contains a {@code null} element
     */
    public static <T> T[] noNullElements(final T[] array, final String format, final Object... args)
            throws IllegalArgumentException {
        return noNullElements(array, () -> new IllegalArgumentException(StringKit.format(format, args)));
    }

    /**
     * Asserts that an array does not contain any {@code null} elements. If it does, an {@link IllegalArgumentException}
     * is thrown with a default error message. An empty or {@code null} array is considered not to contain {@code null}
     * elements.
     * 
     * <pre class="code">
     * Assert.noNullElements(array);
     * </pre>
     *
     * @param <T>   The component type of the array.
     * @param array The array to check.
     * @return The checked array.
     * @throws IllegalArgumentException if the object array contains a {@code null} element
     */
    public static <T> T[] noNullElements(final T[] array) throws IllegalArgumentException {
        return noNullElements(array, "[Assertion failed] - this array must not contain any null elements");
    }

    /**
     * Asserts that the given object is an instance of the specified class. If it is not, an
     * {@link IllegalArgumentException} is thrown.
     * 
     * <pre class="code">
     * Assert.instanceOf(Foo.class, foo);
     * </pre>
     *
     * @param <T>    The type of the object being checked.
     * @param type   The class to check against.
     * @param object The object to check.
     * @return The checked object.
     * @throws IllegalArgumentException if the object is not an instance of clazz
     * @see Class#isInstance(Object)
     */
    public static <T> T isInstanceOf(final Class<?> type, final T object) {
        return isInstanceOf(type, object, "Object [{}] is not instanceof [{}]", object, type);
    }

    /**
     * Asserts that the given object is an instance of the specified class. If it is not, an
     * {@link IllegalArgumentException} is thrown with a formatted error message.
     * 
     * <pre class="code">
     * Assert.instanceOf(Foo.class, foo, "foo must be an instance of class Foo");
     * </pre>
     *
     * @param <T>    The type of the object being checked.
     * @param type   The class to check against.
     * @param object The object to check.
     * @param format The error message template, with `{}` as placeholders for arguments.
     * @param args   The arguments to fill into the error message template.
     * @return The checked object.
     * @throws IllegalArgumentException if the object is not an instance of clazz
     * @see Class#isInstance(Object)
     */
    public static <T> T isInstanceOf(final Class<?> type, final T object, final String format, final Object... args)
            throws IllegalArgumentException {
        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(object)) {
            throw new IllegalArgumentException(StringKit.format(format, args));
        }
        return object;
    }

    /**
     * Asserts that the given object is not an instance of the specified class. If it is, an
     * {@link IllegalArgumentException} is thrown.
     * 
     * <pre class="code">
     * Assert.isNotInstanceOf(Foo.class, foo);
     * </pre>
     *
     * @param <T>  The type of the object being checked.
     * @param type The class to check against.
     * @param obj  The object to check.
     * @return The checked object.
     * @throws IllegalArgumentException if the object is an instance of clazz
     * @see Class#isInstance(Object)
     */
    public static <T> T isNotInstanceOf(final Class<?> type, final T obj) {
        return isNotInstanceOf(type, obj, "Object [{}] must be not instanceof [{}]", obj, type);
    }

    /**
     * Asserts that the given object is not an instance of the specified class. If it is, an
     * {@link IllegalArgumentException} is thrown with a formatted error message.
     * 
     * <pre class="code">
     * Assert.isNotInstanceOf(Foo.class, foo, "foo must be not an Foo");
     * </pre>
     *
     * @param <T>    The type of the object being checked.
     * @param type   The class to check against.
     * @param obj    The object to check.
     * @param format The error message template, with `{}` as placeholders for arguments.
     * @param args   The arguments to fill into the error message template.
     * @return The checked object.
     * @throws IllegalArgumentException if the object is an instance of clazz
     * @see Class#isInstance(Object)
     */
    public static <T> T isNotInstanceOf(final Class<?> type, final T obj, final String format, final Object... args)
            throws IllegalArgumentException {
        notNull(type, "Type to check against must not be null");
        if (type.isInstance(obj)) {
            throw new IllegalArgumentException(StringKit.format(format, args));
        }
        return obj;
    }

    /**
     * Asserts that {@code superType.isAssignableFrom(subType)} is {@code true}. That is, {@code subType} is assignable
     * to {@code superType}. If not, an {@link IllegalArgumentException} is thrown.
     * 
     * <pre class="code">
     * Assert.isAssignable(Number.class, myClass);
     * </pre>
     *
     * @param superType The supertype to check against.
     * @param subType   The subtype to check.
     * @throws IllegalArgumentException if the subtype is not assignable to the supertype.
     */
    public static void isAssignable(final Class<?> superType, final Class<?> subType) throws IllegalArgumentException {
        isAssignable(superType, subType, "{} is not assignable to {})", subType, superType);
    }

    /**
     * Asserts that {@code superType.isAssignableFrom(subType)} is {@code true}. That is, {@code subType} is assignable
     * to {@code superType}. If not, an {@link IllegalArgumentException} is thrown with a formatted error message.
     * 
     * <pre class="code">
     * Assert.isAssignable(Number.class, myClass, "myClass must can be assignable to class Number");
     * </pre>
     *
     * @param superType The supertype to check against.
     * @param subType   The subtype to check.
     * @param format    The error message template, with `{}` as placeholders for arguments.
     * @param args      The arguments to fill into the error message template.
     * @throws IllegalArgumentException if the subtype is not assignable to the supertype.
     */
    public static void isAssignable(
            final Class<?> superType,
            final Class<?> subType,
            final String format,
            final Object... args) throws IllegalArgumentException {
        notNull(superType, "Type to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            throw new IllegalArgumentException(StringKit.format(format, args));
        }
    }

    /**
     * Checks a boolean expression. If the expression is {@code false}, an {@link IllegalStateException} is thrown, with
     * the error message provided by the supplier.
     * 
     * <pre class="code">
     * Assert.state(id == null, () -&gt; {
     *     // to query relation message
     *     return "relation message to return ";
     * });
     * </pre>
     *
     * @param expression       The boolean expression to check.
     * @param errorMsgSupplier A supplier for the error message to use if the expression is {@code false}.
     * @throws IllegalStateException if the expression is {@code false}
     */
    public static void state(final boolean expression, final Supplier<String> errorMsgSupplier)
            throws IllegalStateException {
        if (!expression) {
            throw new IllegalStateException(errorMsgSupplier.get());
        }
    }

    /**
     * Checks a boolean expression. If the expression is {@code false}, an {@link IllegalStateException} is thrown with
     * a formatted error message.
     * 
     * <pre class="code">
     * Assert.state(id == null, "The id property must not already be initialized");
     * </pre>
     *
     * @param expression The boolean expression to check.
     * @param format     The error message template, with `{}` as placeholders for arguments.
     * @param args       The arguments to fill into the error message template.
     * @throws IllegalStateException if the expression is {@code false}
     */
    public static void state(final boolean expression, final String format, final Object... args)
            throws IllegalStateException {
        if (!expression) {
            throw new IllegalStateException(StringKit.format(format, args));
        }
    }

    /**
     * Checks a boolean expression. If the expression is {@code false}, an {@link IllegalStateException} is thrown with
     * a default error message.
     * 
     * <pre class="code">
     * Assert.state(id == null);
     * </pre>
     *
     * @param expression The boolean expression to check.
     * @throws IllegalStateException if the expression is {@code false}
     */
    public static void state(final boolean expression) throws IllegalStateException {
        state(expression, "[Assertion failed] - this state invariant must be true");
    }

    /**
     * Checks if an index is within the bounds of a given size. The index must satisfy:
     *
     * <pre>
     * 0 &le; index &lt; size
     * </pre>
     *
     * @param index The index to check.
     * @param size  The size of the array, collection, or string.
     * @return The validated index.
     * @throws IllegalArgumentException  if size &lt; 0.
     * @throws IndexOutOfBoundsException if index &lt; 0 or index &ge; size.
     */
    public static int checkIndex(final int index, final int size)
            throws IllegalArgumentException, IndexOutOfBoundsException {
        return checkIndex(index, size, "[Assertion failed]");
    }

    /**
     * Checks if a long index is within the bounds of a given size. The index must satisfy:
     *
     * <pre>
     * 0 &le; index &lt; size
     * </pre>
     *
     * @param index The long index to check.
     * @param size  The size of the array, collection, or string.
     * @return The validated long index.
     * @throws IllegalArgumentException  if size &lt; 0.
     * @throws IndexOutOfBoundsException if index &lt; 0 or index &ge; size.
     * @see java.util.Objects#checkIndex(long, long)
     */
    public static long checkIndex(final long index, final long size)
            throws IllegalArgumentException, IndexOutOfBoundsException {
        return checkIndex(index, size, "[Assertion failed]");
    }

    /**
     * Checks if an index is within the bounds of a given size. The index must satisfy:
     *
     * <pre>
     * 0 &le; index &lt; size
     * </pre>
     *
     * @param index  The index to check.
     * @param size   The size of the array, collection, or string.
     * @param format The error message template, with `{}` as placeholders for arguments.
     * @param args   The arguments to fill into the error message template.
     * @return The validated index.
     * @throws IllegalArgumentException  if size &lt; 0.
     * @throws IndexOutOfBoundsException if index &lt; 0 or index &ge; size.
     */
    public static int checkIndex(final int index, final int size, final String format, final Object... args)
            throws IllegalArgumentException, IndexOutOfBoundsException {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(badIndex(index, size, format, args));
        }
        return index;
    }

    /**
     * Checks if a long index is within the bounds of a given size. The index must satisfy:
     *
     * <pre>
     * 0 &le; index &lt; size
     * </pre>
     *
     * @param index  The long index to check.
     * @param size   The size of the array, collection, or string.
     * @param format The error message template, with `{}` as placeholders for arguments.
     * @param args   The arguments to fill into the error message template.
     * @return The validated long index.
     * @throws IllegalArgumentException  if size &lt; 0.
     * @throws IndexOutOfBoundsException if index &lt; 0 or index &ge; size.
     */
    public static long checkIndex(final long index, final long size, final String format, final Object... args)
            throws IllegalArgumentException, IndexOutOfBoundsException {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(badIndex(index, size, format, args));
        }
        return index;
    }

    /**
     * Checks if a value is within a specified range (inclusive). If the value is out of bounds, a custom exception
     * provided by the supplier is thrown.
     *
     * @param <X>           The type of exception to throw.
     * @param value         The value to check.
     * @param min           The minimum allowed value (inclusive).
     * @param max           The maximum allowed value (inclusive).
     * @param errorSupplier A supplier for the exception to throw if the value is out of bounds.
     * @return The validated value.
     * @throws X if value is out of bound
     */
    public static <X extends Throwable> int checkBetween(
            final int value,
            final int min,
            final int max,
            final Supplier<? extends X> errorSupplier) throws X {
        if (value < min || value > max) {
            throw errorSupplier.get();
        }

        return value;
    }

    /**
     * Checks if a value is within a specified range (inclusive). If the value is out of bounds, an
     * {@link IllegalArgumentException} is thrown with a formatted error message.
     *
     * @param value  The value to check.
     * @param min    The minimum allowed value (inclusive).
     * @param max    The maximum allowed value (inclusive).
     * @param format The error message template, with `{}` as placeholders for arguments.
     * @param args   The arguments to fill into the error message template.
     * @return The validated value.
     */
    public static int checkBetween(
            final int value,
            final int min,
            final int max,
            final String format,
            final Object... args) {
        return checkBetween(value, min, max, () -> new IllegalArgumentException(StringKit.format(format, args)));
    }

    /**
     * Checks if a value is within a specified range (inclusive). If the value is out of bounds, an
     * {@link IllegalArgumentException} is thrown with a default error message.
     *
     * @param value The value to check.
     * @param min   The minimum allowed value (inclusive).
     * @param max   The maximum allowed value (inclusive).
     * @return The validated value.
     */
    public static int checkBetween(final int value, final int min, final int max) {
        return checkBetween(value, min, max, TEMPLATE_VALUE_MUST_BE_BETWEEN_AND, min, max);
    }

    /**
     * Checks if a long value is within a specified range (inclusive). If the value is out of bounds, a custom exception
     * provided by the supplier is thrown.
     *
     * @param <X>           The type of exception to throw.
     * @param value         The long value to check.
     * @param min           The minimum allowed long value (inclusive).
     * @param max           The maximum allowed long value (inclusive).
     * @param errorSupplier A supplier for the exception to throw if the value is out of bounds.
     * @return The validated long value.
     * @throws X if value is out of bound
     */
    public static <X extends Throwable> long checkBetween(
            final long value,
            final long min,
            final long max,
            final Supplier<? extends X> errorSupplier) throws X {
        if (value < min || value > max) {
            throw errorSupplier.get();
        }

        return value;
    }

    /**
     * Checks if a long value is within a specified range (inclusive). If the value is out of bounds, an
     * {@link IllegalArgumentException} is thrown with a formatted error message.
     *
     * @param value  The long value to check.
     * @param min    The minimum allowed long value (inclusive).
     * @param max    The maximum allowed long value (inclusive).
     * @param format The error message template, with `{}` as placeholders for arguments.
     * @param args   The arguments to fill into the error message template.
     * @return The validated long value.
     */
    public static long checkBetween(
            final long value,
            final long min,
            final long max,
            final String format,
            final Object... args) {
        return checkBetween(value, min, max, () -> new IllegalArgumentException(StringKit.format(format, args)));
    }

    /**
     * Checks if a long value is within a specified range (inclusive). If the value is out of bounds, an
     * {@link IllegalArgumentException} is thrown with a default error message.
     *
     * @param value The long value to check.
     * @param min   The minimum allowed long value (inclusive).
     * @param max   The maximum allowed long value (inclusive).
     * @return The validated long value.
     */
    public static long checkBetween(final long value, final long min, final long max) {
        return checkBetween(value, min, max, TEMPLATE_VALUE_MUST_BE_BETWEEN_AND, min, max);
    }

    /**
     * Checks if a double value is within a specified range (inclusive). If the value is out of bounds, a custom
     * exception provided by the supplier is thrown.
     *
     * @param <X>           The type of exception to throw.
     * @param value         The double value to check.
     * @param min           The minimum allowed double value (inclusive).
     * @param max           The maximum allowed double value (inclusive).
     * @param errorSupplier A supplier for the exception to throw if the value is out of bounds.
     * @return The validated double value.
     * @throws X if value is out of bound
     */
    public static <X extends Throwable> double checkBetween(
            final double value,
            final double min,
            final double max,
            final Supplier<? extends X> errorSupplier) throws X {
        if (value < min || value > max) {
            throw errorSupplier.get();
        }

        return value;
    }

    /**
     * Checks if a double value is within a specified range (inclusive). If the value is out of bounds, an
     * {@link IllegalArgumentException} is thrown with a formatted error message.
     *
     * @param value  The double value to check.
     * @param min    The minimum allowed double value (inclusive).
     * @param max    The maximum allowed double value (inclusive).
     * @param format The error message template, with `{}` as placeholders for arguments.
     * @param args   The arguments to fill into the error message template.
     * @return The validated double value.
     */
    public static double checkBetween(
            final double value,
            final double min,
            final double max,
            final String format,
            final Object... args) {
        return checkBetween(value, min, max, () -> new IllegalArgumentException(StringKit.format(format, args)));
    }

    /**
     * Checks if a double value is within a specified range (inclusive). If the value is out of bounds, an
     * {@link IllegalArgumentException} is thrown with a default error message.
     *
     * @param value The double value to check.
     * @param min   The minimum allowed double value (inclusive).
     * @param max   The maximum allowed double value (inclusive).
     * @return The validated double value.
     */
    public static double checkBetween(final double value, final double min, final double max) {
        return checkBetween(value, min, max, TEMPLATE_VALUE_MUST_BE_BETWEEN_AND, min, max);
    }

    /**
     * Checks if a {@link Number} value is within a specified range (inclusive). If the value is out of bounds, an
     * {@link IllegalArgumentException} is thrown with a default error message.
     *
     * @param value The {@link Number} value to check.
     * @param min   The minimum allowed {@link Number} value (inclusive).
     * @param max   The maximum allowed {@link Number} value (inclusive).
     * @return The validated {@link Number} value.
     * @throws IllegalArgumentException if the value is out of bounds or any of the arguments are {@code null}.
     */
    public static Number checkBetween(final Number value, final Number min, final Number max) {
        notNull(value);
        notNull(min);
        notNull(max);
        final double valueDouble = value.doubleValue();
        final double minDouble = min.doubleValue();
        final double maxDouble = max.doubleValue();
        if (valueDouble < minDouble || valueDouble > maxDouble) {
            throw new IllegalArgumentException(StringKit.format(TEMPLATE_VALUE_MUST_BE_BETWEEN_AND, min, max));
        }
        return value;
    }

    /**
     * Asserts that two objects are not equal. If they are equal, an {@link IllegalArgumentException} is thrown with a
     * default error message.
     * 
     * <pre class="code">
     * Assert.notEquals(obj1, obj2);
     * </pre>
     *
     * @param obj1 The first object.
     * @param obj2 The second object.
     * @throws IllegalArgumentException if obj1 is equal to obj2.
     */
    public static void notEquals(final Object obj1, final Object obj2) {
        notEquals(obj1, obj2, "({}) must be not equals ({})", obj1, obj2);
    }

    /**
     * Asserts that two objects are not equal. If they are equal, an {@link IllegalArgumentException} is thrown with a
     * formatted error message.
     * 
     * <pre class="code">
     * Assert.notEquals(obj1, obj2, "obj1 must be not equals obj2");
     * </pre>
     *
     * @param obj1   The first object.
     * @param obj2   The second object.
     * @param format The error message template, with `{}` as placeholders for arguments.
     * @param args   The arguments to fill into the error message template.
     * @throws IllegalArgumentException if obj1 is equal to obj2.
     */
    public static void notEquals(final Object obj1, final Object obj2, final String format, final Object... args)
            throws IllegalArgumentException {
        notEquals(obj1, obj2, () -> new IllegalArgumentException(StringKit.format(format, args)));
    }

    /**
     * Asserts that two objects are not equal. If they are equal, a custom exception provided by the supplier is thrown.
     *
     * @param obj1          The first object.
     * @param obj2          The second object.
     * @param errorSupplier A supplier for the exception to throw if obj1 is equal to obj2.
     * @param <X>           The type of exception to throw.
     * @throws X if obj1 is equal to obj2.
     */
    public static <X extends Throwable> void notEquals(
            final Object obj1,
            final Object obj2,
            final Supplier<X> errorSupplier) throws X {
        if (ObjectKit.equals(obj1, obj2)) {
            throw errorSupplier.get();
        }
    }

    /**
     * Asserts that two objects are equal. If they are not equal, an {@link IllegalArgumentException} is thrown with a
     * default error message.
     * 
     * <pre class="code">
     * Assert.isEquals(obj1, obj2);
     * </pre>
     *
     * @param obj1 The first object.
     * @param obj2 The second object.
     * @throws IllegalArgumentException if obj1 is not equal to obj2.
     */
    public static void equals(final Object obj1, final Object obj2) {
        equals(obj1, obj2, "({}) must be equals ({})", obj1, obj2);
    }

    /**
     * Asserts that two objects are equal. If they are not equal, an {@link IllegalArgumentException} is thrown with a
     * formatted error message.
     * 
     * <pre class="code">
     * Assert.isEquals(obj1, obj2, "obj1 must be equals obj2");
     * </pre>
     *
     * @param obj1   The first object.
     * @param obj2   The second object.
     * @param format The error message template, with `{}` as placeholders for arguments.
     * @param args   The arguments to fill into the error message template.
     * @throws IllegalArgumentException if obj1 is not equal to obj2.
     */
    public static void equals(final Object obj1, final Object obj2, final String format, final Object... args)
            throws IllegalArgumentException {
        equals(obj1, obj2, () -> new IllegalArgumentException(StringKit.format(format, args)));
    }

    /**
     * Asserts that two objects are equal. If they are not equal, a custom exception provided by the supplier is thrown.
     *
     * @param obj1          The first object.
     * @param obj2          The second object.
     * @param errorSupplier A supplier for the exception to throw if obj1 is not equal to obj2.
     * @param <X>           The type of exception to throw.
     * @throws X if obj1 is not equal to obj2.
     */
    public static <X extends Throwable> void equals(
            final Object obj1,
            final Object obj2,
            final Supplier<X> errorSupplier) throws X {
        if (ObjectKit.notEquals(obj1, obj2)) {
            throw errorSupplier.get();
        }
    }

    /**
     * Generates an error message for an invalid index.
     *
     * @param index The invalid index.
     * @param size  The size of the array, collection, or string.
     * @param desc  The description for the error message.
     * @param args  The arguments to fill into the description template.
     * @return The formatted error message.
     */
    private static String badIndex(final long index, final long size, final String desc, final Object... args) {
        if (index < 0) {
            return StringKit.format("{} ({}) must not be negative", StringKit.format(desc, args), index);
        } else if (size < 0) {
            throw new IllegalArgumentException("negative size: " + size);
        } else { // index >= size
            return StringKit.format("{} ({}) must be less than size ({})", StringKit.format(desc, args), index, size);
        }
    }

}
