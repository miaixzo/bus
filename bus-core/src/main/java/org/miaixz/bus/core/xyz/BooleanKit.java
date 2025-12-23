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

import java.lang.reflect.Type;
import java.util.Set;

import org.miaixz.bus.core.lang.Normal;

/**
 * Utility class for Boolean operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BooleanKit {

    /**
     * Constructs a new BooleanKit. Utility class constructor for static access.
     */
    private BooleanKit() {
    }

    /**
     * A set of strings that represent the boolean value {@code true}.
     */
    private static final Set<String> TRUE_SET = SetKit.of(Normal.TRUE_ARRAY);
    /**
     * A set of strings that represent the boolean value {@code false}.
     */
    private static final Set<String> FALSE_SET = SetKit.of(Normal.FALSE_ARRAY);

    /**
     * Negates the given boolean.
     *
     * @param bool The Boolean to negate.
     * @return The negated Boolean, or {@code null} if the input is {@code null}.
     */
    public static Boolean negate(final Boolean bool) {
        if (bool == null) {
            return null;
        }
        return bool ? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * Checks if a {@code Boolean} value is {@code true}.
     *
     * <pre>
     * BooleanKit.isTrue(Boolean.TRUE)  = true
     * BooleanKit.isTrue(Boolean.FALSE) = false
     * BooleanKit.isTrue(null)          = false
     * </pre>
     *
     * @param bool The Boolean to check.
     * @return {@code true} if the Boolean is not {@code null} and is {@code true}.
     */
    public static boolean isTrue(final Boolean bool) {
        return Boolean.TRUE.equals(bool);
    }

    /**
     * Checks if a {@code Boolean} value is {@code false}.
     *
     * <pre>
     * BooleanKit.isFalse(Boolean.TRUE)  = false
     * BooleanKit.isFalse(Boolean.FALSE) = true
     * BooleanKit.isFalse(null)          = false
     * </pre>
     *
     * @param bool The Boolean to check.
     * @return {@code true} if the Boolean is not {@code null} and is {@code false}.
     */
    public static boolean isFalse(final Boolean bool) {
        return Boolean.FALSE.equals(bool);
    }

    /**
     * Negates the given primitive boolean.
     *
     * @param bool The boolean to negate.
     * @return The negated boolean.
     */
    public static boolean negate(final boolean bool) {
        return !bool;
    }

    /**
     * Converts a String to a boolean.
     * <p>
     * Returns {@code true} if the trimmed, lower-cased string is in {@link #TRUE_SET}, otherwise {@code false}.
     *
     * @param values The string to convert, case-insensitive, with leading/trailing whitespace trimmed.
     * @return The boolean value.
     */
    public static boolean toBoolean(final String values) {
        if (StringKit.isNotBlank(values)) {
            return TRUE_SET.contains(values.trim().toLowerCase());
        }
        return false;
    }

    /**
     * Converts a String to a Boolean object. If the trimmed, lower-cased string is in {@link #TRUE_SET}, returns
     * {@link Boolean#TRUE}. If the trimmed, lower-cased string is in {@link #FALSE_SET}, returns {@link Boolean#FALSE}.
     * Otherwise, returns {@code null}.
     *
     * @param values The string to convert, case-insensitive, with leading/trailing whitespace trimmed.
     * @return the Boolean object, or {@code null}.
     */
    public static Boolean toBooleanObject(String values) {
        if (StringKit.isNotBlank(values)) {
            values = values.trim().toLowerCase();
            if (TRUE_SET.contains(values)) {
                return Boolean.TRUE;
            } else if (FALSE_SET.contains(values)) {
                return Boolean.FALSE;
            }
        }
        return null;
    }

    /**
     * Converts a boolean to an int.
     *
     * @param value The boolean to convert.
     * @return 1 if {@code true}, 0 if {@code false}.
     */
    public static int toInt(final boolean value) {
        return value ? 1 : 0;
    }

    /**
     * Converts a boolean to an Integer.
     *
     * @param value The boolean to convert.
     * @return The Integer representation (1 for true, 0 for false).
     */
    public static Integer toInteger(final boolean value) {
        return toInt(value);
    }

    /**
     * Converts a boolean to a char.
     *
     * @param value The boolean to convert.
     * @return The char representation (char)1 for true, (char)0 for false.
     */
    public static char toChar(final boolean value) {
        return (char) toInt(value);
    }

    /**
     * Converts a boolean to a Character.
     *
     * @param value The boolean to convert.
     * @return The Character representation.
     */
    public static Character toCharacter(final boolean value) {
        return toChar(value);
    }

    /**
     * Converts a boolean to a byte.
     *
     * @param value The boolean to convert.
     * @return The byte representation (1 for true, 0 for false).
     */
    public static byte toByte(final boolean value) {
        return (byte) toInt(value);
    }

    /**
     * Converts a boolean to a Byte.
     *
     * @param value The boolean to convert.
     * @return The Byte representation.
     */
    public static Byte toByteObject(final boolean value) {
        return toByte(value);
    }

    /**
     * Converts a boolean to a long.
     *
     * @param value The boolean to convert.
     * @return The long representation (1 for true, 0 for false).
     */
    public static long toLong(final boolean value) {
        return toInt(value);
    }

    /**
     * Converts a boolean to a Long.
     *
     * @param value The boolean to convert.
     * @return The Long representation.
     */
    public static Long toLongObject(final boolean value) {
        return toLong(value);
    }

    /**
     * Converts a boolean to a short.
     *
     * @param value The boolean to convert.
     * @return The short representation (1 for true, 0 for false).
     */
    public static short toShort(final boolean value) {
        return (short) toInt(value);
    }

    /**
     * Converts a boolean to a Short.
     *
     * @param value The boolean to convert.
     * @return The Short representation.
     */
    public static Short toShortObject(final boolean value) {
        return toShort(value);
    }

    /**
     * Converts a boolean to a float.
     *
     * @param value The boolean to convert.
     * @return The float representation (1.0f for true, 0.0f for false).
     */
    public static float toFloat(final boolean value) {
        return (float) toInt(value);
    }

    /**
     * Converts a boolean to a Float.
     *
     * @param value The boolean to convert.
     * @return The Float representation.
     */
    public static Float toFloatObject(final boolean value) {
        return toFloat(value);
    }

    /**
     * Converts a boolean to a double.
     *
     * @param value The boolean to convert.
     * @return The double representation (1.0 for true, 0.0 for false).
     */
    public static double toDouble(final boolean value) {
        return toInt(value);
    }

    /**
     * Converts a boolean to a Double.
     *
     * @param value The boolean to convert.
     * @return The Double representation.
     */
    public static Double toDoubleObject(final boolean value) {
        return toDouble(value);
    }

    /**
     * Converts a boolean to the string {@code "true"} or {@code "false"}.
     *
     * <pre>
     * BooleanKit.toStringTrueFalse(true)   = "true"
     * BooleanKit.toStringTrueFalse(false)  = "false"
     * </pre>
     *
     * @param bool The boolean to convert.
     * @return {@code "true"} or {@code "false"}.
     */
    public static String toStringTrueFalse(final boolean bool) {
        return toString(bool, "true", "false");
    }

    /**
     * Converts a boolean to the string {@code "on"} or {@code "off"}.
     *
     * <pre>
     * BooleanKit.toStringOnOff(true)   = "on"
     * BooleanKit.toStringOnOff(false)  = "off"
     * </pre>
     *
     * @param bool The boolean to convert.
     * @return {@code "on"} or {@code "off"}.
     */
    public static String toStringOnOff(final boolean bool) {
        return toString(bool, "on", "off");
    }

    /**
     * Converts a boolean to the string {@code "yes"} or {@code "no"}.
     *
     * <pre>
     * BooleanKit.toStringYesNo(true)   = "yes"
     * BooleanKit.toStringYesNo(false)  = "no"
     * </pre>
     *
     * @param bool The boolean to convert.
     * @return {@code "yes"} or {@code "no"}.
     */
    public static String toStringYesNo(final boolean bool) {
        return toString(bool, "yes", "no");
    }

    /**
     * Converts a boolean to a String, returning one of two specified strings.
     *
     * <pre>
     * BooleanKit.toString(true, "true", "false")   = "true"
     * BooleanKit.toString(false, "true", "false")  = "false"
     * </pre>
     *
     * @param bool        The boolean to convert.
     * @param trueString  The String to return if the boolean is {@code true}, may be {@code null}.
     * @param falseString The String to return if the boolean is {@code false}, may be {@code null}.
     * @return The resulting String.
     */
    public static String toString(final boolean bool, final String trueString, final String falseString) {
        return bool ? trueString : falseString;
    }

    /**
     * Converts a Boolean to a String, returning one of three specified strings.
     *
     * <pre>
     * BooleanKit.toString(Boolean.TRUE, "true", "false", "null") = "true"
     * BooleanKit.toString(Boolean.FALSE, "true", "false", "null") = "false"
     * BooleanKit.toString(null, "true", "false", "null") = "null"
     * </pre>
     *
     * @param bool        The Boolean to convert.
     * @param trueString  The String to return if the Boolean is {@code true}, may be {@code null}.
     * @param falseString The String to return if the Boolean is {@code false}, may be {@code null}.
     * @param nullString  The String to return if the Boolean is {@code null}, may be {@code null}.
     * @return The resulting String.
     */
    public static String toString(
            final Boolean bool,
            final String trueString,
            final String falseString,
            final String nullString) {
        if (bool == null) {
            return nullString;
        }
        return bool ? trueString : falseString;
    }

    /**
     * Performs a logical AND operation on an array of booleans.
     *
     * <pre>
     * BooleanKit.and(true, true)         = true
     * BooleanKit.and(false, false)       = false
     * BooleanKit.and(true, false)        = false
     * BooleanKit.and(true, true, false)  = false
     * BooleanKit.and(true, true, true)   = true
     * </pre>
     *
     * @param array An array of {@code boolean}s.
     * @return The result of the logical AND.
     * @throws IllegalArgumentException if the array is empty.
     */
    public static boolean and(final boolean... array) {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException("The Array must not be empty !");
        }
        for (final boolean element : array) {
            if (!element) {
                return false;
            }
        }
        return true;
    }

    /**
     * Performs a logical AND operation on an array of Booleans.
     * <p>
     * Note: {@code null} elements are treated as {@code false}.
     *
     * <pre>
     * BooleanKit.andOfWrap(Boolean.TRUE, Boolean.TRUE)                 = Boolean.TRUE
     * BooleanKit.andOfWrap(Boolean.FALSE, Boolean.FALSE)               = Boolean.FALSE
     * BooleanKit.andOfWrap(Boolean.TRUE, Boolean.FALSE)                = Boolean.FALSE
     * BooleanKit.andOfWrap(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE)   = Boolean.TRUE
     * BooleanKit.andOfWrap(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE) = Boolean.FALSE
     * BooleanKit.andOfWrap(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE)  = Boolean.FALSE
     * BooleanKit.andOfWrap(Boolean.TRUE, null)                         = Boolean.FALSE
     * </pre>
     *
     * @param array An array of {@code Boolean}s.
     * @return The result of the logical AND.
     * @throws IllegalArgumentException if the array is empty.
     */
    public static Boolean andOfWrap(final Boolean... array) {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException("The Array must not be empty !");
        }

        for (final Boolean b : array) {
            if (!isTrue(b)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Performs a logical OR operation on an array of booleans.
     *
     * <pre>
     * BooleanKit.or(true, true)          = true
     * BooleanKit.or(false, false)        = false
     * BooleanKit.or(true, false)         = true
     * BooleanKit.or(true, true, false)   = true
     * BooleanKit.or(true, true, true)    = true
     * BooleanKit.or(false, false, false) = false
     * </pre>
     *
     * @param array An array of {@code boolean}s.
     * @return The result of the logical OR.
     * @throws IllegalArgumentException if the array is empty.
     */
    public static boolean or(final boolean... array) {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException("The Array must not be empty !");
        }
        for (final boolean element : array) {
            if (element) {
                return true;
            }
        }
        return false;
    }

    /**
     * Performs a logical OR operation on an array of Booleans.
     * <p>
     * Note: {@code null} elements are treated as {@code false}.
     *
     * <pre>
     * BooleanKit.orOfWrap(Boolean.TRUE, Boolean.TRUE)                  = Boolean.TRUE
     * BooleanKit.orOfWrap(Boolean.FALSE, Boolean.FALSE)                = Boolean.FALSE
     * BooleanKit.orOfWrap(Boolean.TRUE, Boolean.FALSE)                 = Boolean.TRUE
     * BooleanKit.orOfWrap(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE)    = Boolean.TRUE
     * BooleanKit.orOfWrap(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE)  = Boolean.TRUE
     * BooleanKit.orOfWrap(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE)   = Boolean.TRUE
     * BooleanKit.orOfWrap(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE) = Boolean.FALSE
     * BooleanKit.orOfWrap(Boolean.FALSE, null)                         = Boolean.FALSE
     * </pre>
     *
     * @param array An array of {@code Boolean}s.
     * @return The result of the logical OR.
     * @throws IllegalArgumentException if the array is empty.
     */
    public static Boolean orOfWrap(final Boolean... array) {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException("The Array must not be empty !");
        }

        for (final Boolean b : array) {
            if (isTrue(b)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Performs a logical XOR operation on an array of booleans.
     *
     * <pre>
     * BooleanKit.xor(true, true)   = false
     * BooleanKit.xor(false, false) = false
     * BooleanKit.xor(true, false)  = true
     * BooleanKit.xor(true, true, true)   = true
     * BooleanKit.xor(false, false, false) = false
     * BooleanKit.xor(true, true, false)  = false
     * BooleanKit.xor(true, false, false)  = true
     * </pre>
     *
     * @param array An array of {@code boolean}s.
     * @return {@code true} if the result of the XOR operation is true.
     * @throws IllegalArgumentException if the array is empty.
     */
    public static boolean xor(final boolean... array) {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException("The Array must not be empty");
        }

        boolean result = false;
        for (final boolean element : array) {
            result ^= element;
        }

        return result;
    }

    /**
     * Performs a logical XOR operation on an array of Booleans.
     *
     * <pre>
     * BooleanKit.xorOfWrap(Boolean.TRUE, Boolean.TRUE)                  = Boolean.FALSE
     * BooleanKit.xorOfWrap(Boolean.FALSE, Boolean.FALSE)                = Boolean.FALSE
     * BooleanKit.xorOfWrap(Boolean.TRUE, Boolean.FALSE)                 = Boolean.TRUE
     * BooleanKit.xorOfWrap(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE)    = Boolean.TRUE
     * BooleanKit.xorOfWrap(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE) = Boolean.FALSE
     * BooleanKit.xorOfWrap(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE)   = Boolean.FALSE
     * BooleanKit.xorOfWrap(Boolean.TRUE, Boolean.FALSE, Boolean.FALSE)  = Boolean.TRUE
     * </pre>
     *
     * @param array An array of {@code Boolean}s.
     * @return {@code true} if the result of the XOR operation is true.
     * @throws IllegalArgumentException if the array is empty.
     * @throws NullPointerException     if the array contains {@code null}.
     * @see #xor(boolean...)
     */
    public static Boolean xorOfWrap(final Boolean... array) {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException("The Array must not be empty !");
        }

        boolean result = false;
        for (final Boolean element : array) {
            result ^= element;
        }

        return result;
    }

    /**
     * Checks if the given type is either {@code Boolean.class} or {@code boolean.class}.
     *
     * @param type The type to check.
     * @return {@code true} if the type is a boolean type, {@code false} otherwise.
     */
    public static boolean isBoolean(final Type type) {
        return (type == Boolean.class || type == boolean.class);
    }

}
