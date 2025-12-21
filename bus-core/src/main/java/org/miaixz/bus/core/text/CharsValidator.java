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
package org.miaixz.bus.core.text;

import java.util.function.Predicate;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.CollKit;

/**
 * String validation utility class, providing checks for blank and empty strings.
 * <ul>
 * <li>Empty definition: {@code null} or empty string: {@code ""}</li>
 * <li>Blank definition: {@code null} or empty string: {@code ""} or spaces, full-width spaces, tabs, newlines, and
 * other invisible characters</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CharsValidator {

    /**
     * Constructs a new CharsValidator. Utility class constructor for static access.
     */
    public CharsValidator() {
    }

    /**
     * Checks if a string is blank. A string is considered blank if it is:
     * <ol>
     * <li>{@code null}</li>
     * <li>An empty string: {@code ""}</li>
     * <li>Contains only whitespace characters (spaces, full-width spaces, tabs, newlines, etc.)</li>
     * </ol>
     * <ul>
     * <li>{@code isBlank(null)     // true}</li>
     * <li>{@code isBlank("")       // true}</li>
     * <li>{@code isBlank(" \t\n")  // true}</li>
     * <li>{@code isBlank("abc")    // false}</li>
     * </ul>
     *
     * <p>
     * Note: The difference between this method and {@link #isEmpty(CharSequence)} is that this method checks for
     * whitespace characters and is slightly slower than {@link #isEmpty(CharSequence)}.
     *
     * <p>
     * Recommendation:
     *
     * <ul>
     * <li>This method is recommended only for parameters passed from clients (or third-party interfaces).</li>
     * <li>When checking multiple strings simultaneously, it is recommended to use
     * {@link ArrayKit#hasBlank(CharSequence...)} or {@link ArrayKit#isAllBlank(CharSequence...)}.</li>
     * </ul>
     *
     * @param text The {@link CharSequence} to check.
     * @return {@code true} if the string is blank, {@code false} otherwise.
     * @see #isEmpty(CharSequence)
     */
    public static boolean isBlank(final CharSequence text) {
        final int length;

        if ((text == null) || ((length = text.length()) == 0)) {
            return true;
        }

        for (int i = 0; i < length; i++) {
            // As long as there is one non-blank character, it is not a blank string.
            if (!CharKit.isBlankChar(text.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if a string is not blank. A string is considered not blank if it is:
     * <ol>
     * <li>Not {@code null}</li>
     * <li>Not an empty string: {@code ""}</li>
     * <li>Does not contain only whitespace characters (spaces, full-width spaces, tabs, newlines, etc.)</li>
     * </ol>
     * <ul>
     * <li>{@code isNotBlank(null)     // false}</li>
     * <li>{@code isNotBlank("")       // false}</li>
     * <li>{@code isNotBlank(" \t\n")  // false}</li>
     * <li>{@code isNotBlank("abc")    // true}</li>
     * </ul>
     *
     * <p>
     * Note: The difference between this method and {@link #isNotEmpty(CharSequence)} is that this method checks for
     * whitespace characters and is slightly slower than {@link #isNotEmpty(CharSequence)}.
     *
     * <p>
     * Recommendation: This method is recommended only for parameters passed from clients (or third-party interfaces).
     *
     * @param text The {@link CharSequence} to check.
     * @return {@code true} if the string is not blank, {@code false} otherwise.
     * @see #isBlank(CharSequence)
     */
    public static boolean isNotBlank(final CharSequence text) {
        final int length;

        if ((text == null) || ((length = text.length()) == 0)) {
            // empty
            return false;
        }

        for (int i = 0; i < length; i++) {
            // As long as there is one non-blank character, it is not a blank string.
            if (!CharKit.isBlankChar(text.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a string is empty. A string is considered empty if it is:
     * <ol>
     * <li>{@code null}</li>
     * <li>An empty string: {@code ""}</li>
     * </ol>
     * <ul>
     * <li>{@code isEmpty(null)     // true}</li>
     * <li>{@code isEmpty("")       // true}</li>
     * <li>{@code isEmpty(" \t\n")  // false}</li>
     * <li>{@code isEmpty("abc")    // false}</li>
     * </ul>
     *
     * <p>
     * Note: The difference between this method and {@link #isBlank(CharSequence)} is that this method does not check
     * for whitespace characters.
     *
     * <p>
     * Recommendation:
     *
     * <ul>
     * <li>This method is recommended for use in utility classes or for validating method parameters where the expected
     * input is known.</li>
     * <li>When checking multiple strings simultaneously, it is recommended to use {@link #hasEmpty(CharSequence...)} or
     * {@link #isAllEmpty(CharSequence...)}.</li>
     * </ul>
     *
     * @param text The {@link CharSequence} to check.
     * @return {@code true} if the string is empty, {@code false} otherwise.
     * @see #isBlank(CharSequence)
     */
    public static boolean isEmpty(final CharSequence text) {
        return text == null || text.isEmpty();
    }

    /**
     * Checks if a string is not empty. A string is considered not empty if it is:
     * <ol>
     * <li>Not {@code null}</li>
     * <li>Not an empty string: {@code ""}</li>
     * </ol>
     * <ul>
     * <li>{@code isNotEmpty(null)     // false}</li>
     * <li>{@code isNotEmpty("")       // false}</li>
     * <li>{@code isNotEmpty(" \t\n")  // true}</li>
     * <li>{@code isNotEmpty("abc")    // true}</li>
     * </ul>
     *
     * <p>
     * Note: The difference between this method and {@link #isNotBlank(CharSequence)} is that this method does not check
     * for whitespace characters.
     *
     * <p>
     * Recommendation: This method is recommended for use in utility classes or for validating method parameters where
     * the expected input is known.
     *
     * @param text The {@link CharSequence} to check.
     * @return {@code true} if the string is not empty, {@code false} otherwise.
     * @see #isEmpty(CharSequence)
     */
    public static boolean isNotEmpty(final CharSequence text) {
        return !isEmpty(text);
    }

    /**
     * Checks if any string in the given array is blank. Returns {@code true} if the specified string array has a length
     * of 0, or if any element is a blank string.
     * <ul>
     * <li>{@code hasBlank()                  // true}</li>
     * <li>{@code hasBlank("", null, " ")     // true}</li>
     * <li>{@code hasBlank("123", " ")        // true}</li>
     * <li>{@code hasBlank("123", "abc")      // false}</li>
     * </ul>
     *
     * <p>
     * Note: The difference between this method and {@link #isAllBlank(CharSequence...)} is:
     *
     * <ul>
     * <li>{@code hasBlank(CharSequence...)} is equivalent to {@code isBlank(...) || isBlank(...) || ...}</li>
     * <li>{@link #isAllBlank(CharSequence...)} is equivalent to {@code isBlank(...) && isBlank(...) && ...}</li>
     * </ul>
     *
     * @param strs An array of {@link CharSequence}s to check.
     * @return {@code true} if any string in the array is blank, {@code false} otherwise.
     */
    public static boolean hasBlank(final CharSequence... strs) {
        return ArrayKit.hasBlank(strs);
    }

    /**
     * Checks if all provided {@link CharSequence} objects are not {@code null}, not empty, and not blank.
     *
     * @param args One or more {@link CharSequence} objects to check.
     * @return {@code true} if all objects are not blank, {@code false} otherwise.
     */
    public static boolean isAllNotBlank(final CharSequence... args) {
        return ArrayKit.isAllNotBlank(args);
    }

    /**
     * Checks if all strings in the given array are blank. Returns {@code true} if the specified string array has a
     * length of 0, or if all elements are blank strings.
     * <ul>
     * <li>{@code isAllBlank()                  // true}</li>
     * <li>{@code isAllBlank("", null, " ")     // true}</li>
     * <li>{@code isAllBlank("123", " ")        // false}</li>
     * <li>{@code isAllBlank("123", "abc")      // false}</li>
     * </ul>
     *
     * <p>
     * Note: The difference between this method and {@link #hasBlank(CharSequence...)} is:
     *
     * <ul>
     * <li>{@link #hasBlank(CharSequence...)} is equivalent to {@code isBlank(...) || isBlank(...) || ...}</li>
     * <li>{@code isAllBlank(CharSequence...)} is equivalent to {@code isBlank(...) && isBlank(...) && ...}</li>
     * </ul>
     *
     * @param strs An array of {@link CharSequence}s to check.
     * @return {@code true} if all strings in the array are blank, {@code false} otherwise.
     */
    public static boolean isAllBlank(final CharSequence... strs) {
        return ArrayKit.isAllBlank(strs);
    }

    /**
     * Checks if any string in the given array is empty. Returns {@code true} if the specified string array has a length
     * of 0, or if any element is an empty string.
     * <ul>
     * <li>{@code hasEmpty()                  // true}</li>
     * <li>{@code hasEmpty("", null)          // true}</li>
     * <li>{@code hasEmpty("123", "")         // true}</li>
     * <li>{@code hasEmpty("123", "abc")      // false}</li>
     * <li>{@code hasEmpty(" ", "\t", "\n")   // false}</li>
     * </ul>
     *
     * <p>
     * Note: The difference between this method and {@link #isAllEmpty(CharSequence...)} is:
     *
     * <ul>
     * <li>{@code hasEmpty(CharSequence...)} is equivalent to {@code isEmpty(...) || isEmpty(...) || ...}</li>
     * <li>{@link #isAllEmpty(CharSequence...)} is equivalent to {@code isEmpty(...) && isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param args An array of {@link CharSequence}s to check.
     * @return {@code true} if any string in the array is empty, {@code false} otherwise.
     */
    public static boolean hasEmpty(final CharSequence... args) {
        if (ArrayKit.isEmpty(args)) {
            return true;
        }

        for (final CharSequence text : args) {
            if (isEmpty(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if any string in the given {@link Iterable} is empty. Returns {@code true} if the specified
     * {@link Iterable} is empty, or if any element is an empty string.
     *
     * @param args An {@link Iterable} of {@link CharSequence}s to check.
     * @return {@code true} if any string in the iterable is empty, {@code false} otherwise.
     */
    public static boolean hasEmpty(final Iterable<? extends CharSequence> args) {
        if (CollKit.isEmpty(args)) {
            return true;
        }

        for (final CharSequence text : args) {
            if (isEmpty(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if all strings in the given array are empty. Returns {@code true} if the specified string array has a
     * length of 0, or if all elements are empty strings.
     * <ul>
     * <li>{@code isAllEmpty()                  // true}</li>
     * <li>{@code isAllEmpty("", null)          // true}</li>
     * <li>{@code isAllEmpty("123", "")         // false}</li>
     * <li>{@code isAllEmpty("123", "abc")      // false}</li>
     * <li>{@code isAllEmpty(" ", "\t", "\n")   // false}</li>
     * </ul>
     *
     * <p>
     * Note: The difference between this method and {@link #hasEmpty(CharSequence...)} is:
     *
     * <ul>
     * <li>{@link #hasEmpty(CharSequence...)} is equivalent to {@code isEmpty(...) || isEmpty(...) || ...}</li>
     * <li>{@code isAllEmpty(CharSequence...)} is equivalent to {@code isEmpty(...) && isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param args An array of {@link CharSequence}s to check.
     * @return {@code true} if all strings in the array are empty, {@code false} otherwise.
     */
    public static boolean isAllEmpty(final CharSequence... args) {
        if (ArrayKit.isNotEmpty(args)) {
            for (final CharSequence text : args) {
                if (isNotEmpty(text)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks if all strings in the given {@link Iterable} are empty. Returns {@code true} if the specified
     * {@link Iterable} is empty, or if all elements are empty strings.
     *
     * @param args An {@link Iterable} of {@link CharSequence}s to check.
     * @return {@code true} if all strings in the iterable are empty, {@code false} otherwise.
     */
    public static boolean isAllEmpty(final Iterable<? extends CharSequence> args) {
        if (CollKit.isNotEmpty(args)) {
            for (final CharSequence text : args) {
                if (isNotEmpty(text)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks if all strings in the given array are not empty. Returns {@code true} if the specified string array has a
     * length greater than 0, and all elements are not empty strings.
     * <ul>
     * <li>{@code isAllNotEmpty()                  // false}</li>
     * <li>{@code isAllNotEmpty("", null)          // false}</li>
     * <li>{@code isAllNotEmpty("123", "")         // false}</li>
     * <li>{@code isAllNotEmpty("123", "abc")      // true}</li>
     * <li>{@code isAllNotEmpty(" ", "\t", "\n")   // true}</li>
     * </ul>
     *
     * <p>
     * Note: The difference between this method and {@link #isAllEmpty(CharSequence...)} is:
     *
     * <ul>
     * <li>{@link #isAllEmpty(CharSequence...)} is equivalent to {@code isEmpty(...) && isEmpty(...) && ...}</li>
     * <li>{@code isAllNotEmpty(CharSequence...)} is equivalent to {@code !isEmpty(...) && !isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param args An array of {@link CharSequence}s to check.
     * @return {@code true} if all strings in the array are not empty, {@code false} otherwise.
     */
    public static boolean isAllNotEmpty(final CharSequence... args) {
        return !hasEmpty(args);
    }

    /**
     * Checks if a string is {@code null}, the string "null", or the string "undefined".
     *
     * @param text The {@link CharSequence} to check.
     * @return {@code true} if the string is {@code null}, "null", or "undefined", {@code false} otherwise.
     */
    public static boolean isNullOrUndefined(final CharSequence text) {
        if (null == text) {
            return true;
        }
        return isNullOrUndefinedString(text);
    }

    /**
     * Checks if a string is empty ({@code null} or ""), the string "null", or the string "undefined".
     *
     * @param text The {@link CharSequence} to check.
     * @return {@code true} if the string is empty, "null", or "undefined", {@code false} otherwise.
     */
    public static boolean isEmptyOrUndefined(final CharSequence text) {
        if (isEmpty(text)) {
            return true;
        }
        return isNullOrUndefinedString(text);
    }

    /**
     * Checks if a string is blank ({@code null}, "", or whitespace only), the string "null", or the string "undefined".
     *
     * @param text The {@link CharSequence} to check.
     * @return {@code true} if the string is blank, "null", or "undefined", {@code false} otherwise.
     */
    public static boolean isBlankOrUndefined(final CharSequence text) {
        if (isBlank(text)) {
            return true;
        }
        return isNullOrUndefinedString(text);
    }

    /**
     * Checks if a string is "null" or "undefined", without performing a null pointer check on the input
     * {@link CharSequence}.
     *
     * @param text The {@link CharSequence} to check.
     * @return {@code true} if the string is "null" or "undefined", {@code false} otherwise.
     */
    private static boolean isNullOrUndefinedString(final CharSequence text) {
        final String strString = text.toString().trim();
        return Normal.NULL.equals(strString) || "undefined".equals(strString);
    }

    /**
     * Checks if every character in the given string matches the provided {@link Predicate}.
     *
     * @param value   The {@link CharSequence} to check.
     * @param matcher The {@link Predicate} to test each character against.
     * @return {@code true} if all characters match the predicate, {@code false} otherwise.
     */
    public static boolean isAllCharMatch(final CharSequence value, final Predicate<Character> matcher) {
        if (isBlank(value)) {
            return false;
        }
        for (int i = value.length(); --i >= 0;) {
            if (!matcher.test(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
