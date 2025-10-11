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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.text.MessageFormat;
import java.text.Normalizer;
import java.util.*;
import java.util.function.*;
import java.util.regex.Matcher;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.compare.VersionCompare;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.finder.*;
import org.miaixz.bus.core.text.placeholder.StringFormatter;
import org.miaixz.bus.core.text.replacer.CharRangeReplacer;
import org.miaixz.bus.core.text.replacer.SearchReplacer;
import org.miaixz.bus.core.text.replacer.StringRangeReplacer;
import org.miaixz.bus.core.xyz.*;

/**
 * Utility class for {@link CharSequence} operations, including but not limited to:
 * <ul>
 * <li>Adding prefixes or suffixes to strings: {@code addXXX}</li>
 * <li>Padding strings to a specific length: {@code padXXX}</li>
 * <li>Checking string containment: {@code containsXXX}</li>
 * <li>Providing default values for strings: {@code defaultIfXXX}</li>
 * <li>Finding substrings: {@code indexOf}</li>
 * <li>Checking if a string ends with a specific sequence: {@code endWith}</li>
 * <li>Checking if a string starts with a specific sequence: {@code startWith}</li>
 * <li>Comparing strings: {@code equals}</li>
 * <li>Formatting strings: {@code format}</li>
 * <li>Removing parts of strings: {@code removeXXX}</li>
 * <li>Repeating strings: {@code repeat}</li>
 * <li>Extracting substrings: {@code sub}</li>
 * <li>Stripping specified strings from both ends (once): {@code strip}</li>
 * <li>Trimming all specified characters from both ends: {@code trim}</li>
 * <li>Wrapping and unwrapping strings with specified characters: {@code wrap}, {@code unWrap}</li>
 * </ul>
 * <p>
 * Note the different strategies for {@code strip}, {@code trim}, and {@code wrap} ({@code unWrap}):
 * <ul>
 * <li>{@code strip}: Emphasizes removing a *specific string* from one or both ends. It will not repeatedly remove the
 * string. If one side does not exist, the other side's removal is unaffected.</li>
 * <li>{@code trim}: Emphasizes removing *specified characters* from both ends. If there are multiple occurrences of the
 * character, all are removed (e.g., trimming all whitespace characters).</li>
 * <li>{@code unWrap}: Emphasizes unwrapping, requiring both the prefix and suffix characters to be present. If only one
 * is present, no action is taken (e.g., removing double quotes).</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CharsBacker extends CharsValidator {

    /**
     * Calls the {@code toString()} method of an object. If the object is {@code null}, it returns the string "null".
     *
     * @param object The object to convert to a string.
     * @return The string representation of the object, or "null" if the object is {@code null}.
     * @see String#valueOf(Object)
     */
    public static String toString(final Object object) {
        return String.valueOf(object);
    }

    /**
     * Calls the {@code toString()} method of an object. If the object is {@code null}, it returns {@code null}.
     *
     * @param object The object to convert to a string.
     * @return The string representation of the object, or {@code null} if the object is {@code null}.
     */
    public static String toStringOrNull(final Object object) {
        return null == object ? null : object.toString();
    }

    /**
     * Calls the {@code toString()} method of an object. If the object is {@code null}, it returns an empty string "".
     * If only processing {@link CharSequence}, please use {@link #emptyIfNull(CharSequence)}.
     *
     * @param object The object to convert to a string.
     * @return The string representation of the object, or an empty string "" if the object is {@code null}.
     * @see #emptyIfNull(CharSequence)
     */
    public static String toStringOrEmpty(final Object object) {
        return null == object ? Normal.EMPTY : object.toString();
    }

    /**
     * Converts the given {@link CharSequence} to an empty string "" if it is {@code null}. This method differs from
     * {@link #toStringOrEmpty(Object)} in that if the provided {@link CharSequence} is not a {@link String}, it remains
     * unchanged.
     *
     * @param text The {@link CharSequence} to convert.
     * @return The converted {@link CharSequence}, or an empty string "" if the input is {@code null}.
     * @see #toStringOrEmpty(Object)
     */
    public static CharSequence emptyIfNull(final CharSequence text) {
        return null == text ? Normal.EMPTY : text;
    }

    /**
     * Returns {@code null} if the given {@link CharSequence} is empty.
     *
     * @param <T>  The type of the {@link CharSequence}.
     * @param text The {@link CharSequence} to check.
     * @return {@code null} if the input {@link CharSequence} is empty, otherwise the original {@link CharSequence}.
     */
    public static <T extends CharSequence> T nullIfEmpty(final T text) {
        return isEmpty(text) ? null : text;
    }

    /**
     * Returns {@code null} if the given {@link CharSequence} is blank (contains only whitespace).
     *
     * @param <T>  The type of the {@link CharSequence}.
     * @param text The {@link CharSequence} to check.
     * @return {@code null} if the input {@link CharSequence} is blank, otherwise the original {@link CharSequence}.
     */
    public static <T extends CharSequence> T nullIfBlank(final T text) {
        return isBlank(text) ? null : text;
    }

    /**
     * Returns a default value if the given {@link CharSequence} is {@code null}.
     *
     * <pre>{@code
     *   defaultIfNull(null, null);       = null
     *   defaultIfNull(null, "");         = ""
     *   defaultIfNull(null, "zz");       = "zz"
     *   defaultIfNull("abc", *);         = "abc"
     * }</pre>
     *
     * @param <T>          The type of the {@link CharSequence}.
     * @param text         The {@link CharSequence} to check, may be {@code null}.
     * @param defaultValue The default value to return if the {@link CharSequence} is {@code null}, may be {@code null}.
     * @return The original {@link CharSequence} if it is not {@code null}, otherwise the default value.
     * @see ObjectKit#defaultIfNull(Object, Object)
     */
    public static <T extends CharSequence> T defaultIfNull(final T text, final T defaultValue) {
        return ObjectKit.defaultIfNull(text, defaultValue);
    }

    /**
     * Returns the original value if the given {@link CharSequence} is not {@code null}, otherwise returns the default
     * value provided by the {@link Supplier}.
     *
     * @param <T>             The type of the {@link CharSequence}.
     * @param source          The {@link CharSequence} to check, may be {@code null}.
     * @param defaultSupplier The supplier for the default value when the source is {@code null}.
     * @return The original {@link CharSequence} if it is not {@code null}, otherwise the default value from the
     *         supplier.
     * @see ObjectKit#defaultIfNull(Object, Supplier)
     */
    public static <T extends CharSequence> T defaultIfNull(
            final T source,
            final Supplier<? extends T> defaultSupplier) {
        return ObjectKit.defaultIfNull(source, defaultSupplier);
    }

    /**
     * Returns the result of a custom handler if the given {@link CharSequence} is not {@code null}, otherwise returns
     * the default value provided by the {@link Supplier}.
     *
     * @param <R>             The return type.
     * @param <T>             The type of the {@link CharSequence}.
     * @param source          The {@link CharSequence} to check, may be {@code null}.
     * @param handler         The custom function to apply if the source is not {@code null}.
     * @param defaultSupplier The supplier for the default value when the source is {@code null}.
     * @return The result of the handler if the source is not {@code null}, otherwise the default value from the
     *         supplier.
     * @see ObjectKit#defaultIfNull(Object, Function, Supplier)
     */
    public static <T extends CharSequence, R> R defaultIfNull(
            final T source,
            final Function<? super T, ? extends R> handler,
            final Supplier<? extends R> defaultSupplier) {
        return ObjectKit.defaultIfNull(source, handler, defaultSupplier);
    }

    /**
     * Returns a default value if the given {@link CharSequence} is {@code null} or empty.
     *
     * <pre>
     *   defaultIfEmpty(null, null)      = null
     *   defaultIfEmpty(null, "")        = ""
     *   defaultIfEmpty("", "zz")        = "zz"
     *   defaultIfEmpty(" ", "zz")       = " "
     *   defaultIfEmpty("abc", *)        = "abc"
     * </pre>
     *
     * @param <T>          The type of the {@link CharSequence}.
     * @param text         The {@link CharSequence} to check, may be {@code null}.
     * @param defaultValue The default value to return if the {@link CharSequence} is {@code null} or empty, may be
     *                     {@code null} or "".
     * @return The default value if the {@link CharSequence} is {@code null} or empty, otherwise the original value.
     */
    public static <T extends CharSequence> T defaultIfEmpty(final T text, final T defaultValue) {
        return isEmpty(text) ? defaultValue : text;
    }

    /**
     * Returns the original value if the given {@link CharSequence} is not {@code null} or empty, otherwise returns the
     * default value provided by the {@link Supplier}.
     *
     * @param <T>             The type of the {@link CharSequence}.
     * @param text            The {@link CharSequence} to check.
     * @param defaultSupplier The supplier for the default value when the text is empty.
     * @return The original {@link CharSequence} if it is not empty, otherwise the default value from the supplier.
     */
    public static <T extends CharSequence> T defaultIfEmpty(final T text, final Supplier<? extends T> defaultSupplier) {
        return isEmpty(text) ? defaultSupplier.get() : text;
    }

    /**
     * Returns the result of a custom handler if the given {@link CharSequence} is not {@code null} or empty, otherwise
     * returns the default value provided by the {@link Supplier}.
     *
     * @param <T>             The type of the {@link CharSequence}.
     * @param <V>             The result type.
     * @param text            The {@link CharSequence} to check.
     * @param handler         The custom function to apply if the text is not empty.
     * @param defaultSupplier The supplier for the default value when the text is empty.
     * @return The result of the handler if the text is not empty, otherwise the default value from the supplier.
     */
    public static <T extends CharSequence, V> V defaultIfEmpty(
            final T text,
            final Function<T, V> handler,
            final Supplier<? extends V> defaultSupplier) {
        return isEmpty(text) ? defaultSupplier.get() : handler.apply(text);
    }

    /**
     * Returns a default value if the given {@link CharSequence} is {@code null}, empty, or blank (contains only
     * whitespace).
     *
     * <pre>
     *   defaultIfBlank(null, null)      = null
     *   defaultIfBlank(null, "")        = ""
     *   defaultIfBlank("", "zz")        = "zz"
     *   defaultIfBlank(" ", "zz")       = "zz"
     *   defaultIfBlank("abc", *)        = "abc"
     * </pre>
     *
     * @param <T>          The type of the {@link CharSequence}.
     * @param text         The {@link CharSequence} to check, may be {@code null}.
     * @param defaultValue The default value to return if the {@link CharSequence} is {@code null} or empty, or blank,
     *                     may be {@code null} or "" or blank.
     * @return The default value if the {@link CharSequence} is {@code null}, empty, or blank, otherwise the original
     *         value.
     */
    public static <T extends CharSequence> T defaultIfBlank(final T text, final T defaultValue) {
        return isBlank(text) ? defaultValue : text;
    }

    /**
     * Returns the result of a custom handler if the given {@link CharSequence} is not {@code null}, empty, or blank,
     * otherwise returns the default value provided by the {@link Supplier}.
     *
     * @param text            The {@link CharSequence} to check.
     * @param handler         The custom function to apply if the text is not blank.
     * @param defaultSupplier The supplier for the default value when the text is blank.
     * @param <T>             The type of the {@link CharSequence}.
     * @param <V>             The result type.
     * @return The result of the handler if the text is not blank, otherwise the default value from the supplier.
     * @throws NullPointerException if {@code defaultValueSupplier} is {@code null}.
     */
    public static <T extends CharSequence, V> V defaultIfBlank(
            final T text,
            final Function<T, V> handler,
            final Supplier<? extends V> defaultSupplier) {
        if (isBlank(text)) {
            return defaultSupplier.get();
        }
        return handler.apply(text);
    }

    /**
     * Removes leading and trailing whitespace from a string. If the string is {@code null}, {@code null} is returned.
     *
     * <p>
     * Note that unlike {@link String#trim()}, this method uses {@link CharKit#isBlankChar(char)} to determine what
     * constitutes whitespace, thus it can remove other whitespace characters beyond the English character set, such as
     * Chinese spaces.
     * <ul>
     * <li>Related whitespace removal methods:</li>
     * <li>{@link #trimPrefix(CharSequence)} removes leading whitespace.</li>
     * <li>{@link #trimSuffix(CharSequence)} removes trailing whitespace.</li>
     * <li>{@link #cleanBlank(CharSequence)} removes leading, trailing, and internal whitespace.</li>
     * </ul>
     *
     * <pre>
     * trim(null)                         = null
     * trim("")                           = ""
     * trim("     ")                      = ""
     * trim("abc")                        = "abc"
     * trim("    abc    ")                = "abc"
     * </pre>
     *
     * @param text The string to process.
     * @return The string with leading and trailing whitespace removed, or {@code null} if the original string was
     *         {@code null}.
     */
    public static String trim(final CharSequence text) {
        return StringTrimer.TRIM_BLANK.apply(text);
    }

    /**
     * Removes leading and trailing whitespace from a string. If the string is {@code null}, an empty string "" is
     * returned.
     *
     * <pre>
     * trimToEmpty(null)                  = ""
     * trimToEmpty("")                    = ""
     * trimToEmpty("     ")               = ""
     * trimToEmpty("abc")                 = "abc"
     * trimToEmpty("    abc    ")         = "abc"
     * </pre>
     *
     * @param text The string to process.
     * @return The string with leading and trailing whitespace removed, or an empty string "" if the input is
     *         {@code null}.
     */
    public static String trimToEmpty(final CharSequence text) {
        return text == null ? Normal.EMPTY : trim(text);
    }

    /**
     * Removes leading and trailing whitespace from a string. If the string is {@code null} or empty, {@code null} is
     * returned.
     *
     * <pre>
     * trimToNull(null)                   = null
     * trimToNull("")                     = null
     * trimToNull("     ")                = null
     * trimToNull("abc")                  = "abc"
     * trimToEmpty("    abc    ")         = "abc"
     * </pre>
     *
     * @param text The string to process.
     * @return The string with leading and trailing whitespace removed, or {@code null} if the input is empty or
     *         {@code null}.
     */
    public static String trimToNull(final CharSequence text) {
        final String trim = trim(text);
        return Normal.EMPTY.equals(trim) ? null : trim;
    }

    /**
     * Removes leading whitespace from a string. If the string is {@code null}, {@code null} is returned.
     *
     * <p>
     * Note that unlike {@link String#trim()}, this method uses {@link CharKit#isBlankChar(char)} to determine what
     * constitutes whitespace, thus it can remove other whitespace characters beyond the English character set, such as
     * Chinese spaces.
     *
     * <pre>
     * trimPrefix(null)                   = null
     * trimPrefix("")                     = ""
     * trimPrefix("abc")                  = "abc"
     * trimPrefix("  abc")                = "abc"
     * trimPrefix("abc  ")                = "abc  "
     * trimPrefix(" abc ")                = "abc "
     * </pre>
     *
     * @param text The string to process.
     * @return The string with leading whitespace removed, or {@code null} if the original string was {@code null} or
     *         the result is an empty string.
     */
    public static String trimPrefix(final CharSequence text) {
        return StringTrimer.TRIM_PREFIX_BLANK.apply(text);
    }

    /**
     * Removes trailing whitespace from a string. If the string is {@code null}, {@code null} is returned.
     *
     * <p>
     * Note that unlike {@link String#trim()}, this method uses {@link CharKit#isBlankChar(char)} to determine what
     * constitutes whitespace, thus it can remove other whitespace characters beyond the English character set, such as
     * Chinese spaces.
     *
     * <pre>
     * trimSuffix(null)                   = null
     * trimSuffix("")                     = ""
     * trimSuffix("abc")                  = "abc"
     * trimSuffix("  abc")                = "  abc"
     * trimSuffix("abc  ")                = "abc"
     * trimSuffix(" abc ")                = " abc"
     * </pre>
     *
     * @param text The string to process.
     * @return The string with trailing whitespace removed, or {@code null} if the original string was {@code null} or
     *         the result is an empty string.
     */
    public static String trimSuffix(final CharSequence text) {
        return StringTrimer.TRIM_SUFFIX_BLANK.apply(text);
    }

    /**
     * Removes leading and trailing whitespace characters from a string based on the specified trim mode. If the string
     * is {@code null}, {@code null} is returned.
     *
     * @param text The string to process.
     * @param mode The trim mode, specifying whether to trim leading, trailing, or both.
     * @return The string with specified characters removed, or {@code null} if the original string was {@code null}.
     */
    public static String trim(final CharSequence text, final StringTrimer.TrimMode mode) {
        return new StringTrimer(mode, CharKit::isBlankChar).apply(text);
    }

    /**
     * Removes leading and trailing characters from a string based on a given predicate. If the string is {@code null},
     * {@code null} is returned.
     *
     * @param text      The string to process.
     * @param mode      The trim mode, specifying whether to trim leading, trailing, or both.
     * @param predicate A predicate to determine if a character should be trimmed. Returns {@code true} to trim,
     *                  {@code false} to keep.
     * @return The string with specified characters removed, or {@code null} if the original string was {@code null}.
     */
    public static String trim(
            final CharSequence text,
            final StringTrimer.TrimMode mode,
            final Predicate<Character> predicate) {
        return new StringTrimer(mode, predicate).apply(text);
    }

    /**
     * Checks if a string starts with a given character.
     *
     * @param text The string to check.
     * @param c    The character to check for.
     * @return {@code true} if the string starts with the character, {@code false} otherwise.
     */
    public static boolean startWith(final CharSequence text, final char c) {
        if (isEmpty(text)) {
            return false;
        }
        return c == text.charAt(0);
    }

    /**
     * Checks if a string starts with a specified prefix.
     *
     * @param text   The string to check.
     * @param prefix The prefix to check for.
     * @return {@code true} if the string starts with the prefix, {@code false} otherwise.
     */
    public static boolean startWith(final CharSequence text, final CharSequence prefix) {
        return startWith(text, prefix, false);
    }

    /**
     * Checks if a string starts with a specified prefix, ignoring the case where the string and prefix are equal.
     *
     * @param text   The string to check.
     * @param prefix The prefix to check for.
     * @return {@code true} if the string starts with the prefix and they are not equal, {@code false} otherwise.
     */
    public static boolean startWithIgnoreEquals(final CharSequence text, final CharSequence prefix) {
        return startWith(text, prefix, false, true);
    }

    /**
     * Checks if a string starts with a specified prefix, ignoring case.
     *
     * @param text   The string to check.
     * @param prefix The prefix to check for.
     * @return {@code true} if the string starts with the prefix (case-insensitive), {@code false} otherwise.
     */
    public static boolean startWithIgnoreCase(final CharSequence text, final CharSequence prefix) {
        return startWith(text, prefix, true);
    }

    /**
     * Checks if a given string starts with any of the specified prefixes. Returns {@code false} if the given string or
     * the array of prefixes is empty.
     *
     * @param text     The string to check.
     * @param prefixes An array of prefixes to check against.
     * @return {@code true} if the string starts with any of the prefixes, {@code false} otherwise.
     */
    public static boolean startWithAny(final CharSequence text, final CharSequence... prefixes) {
        if (isEmpty(text) || ArrayKit.isEmpty(prefixes)) {
            return false;
        }

        for (CharSequence prefix : prefixes) {
            if (startWith(text, prefix, false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a given string starts with any of the specified prefixes, ignoring case. Returns {@code false} if the
     * given string or the array of prefixes is empty.
     *
     * @param text     The string to check.
     * @param prefixes An array of prefixes to check against.
     * @return {@code true} if the string starts with any of the prefixes (case-insensitive), {@code false} otherwise.
     */
    public static boolean startWithAnyIgnoreCase(final CharSequence text, final CharSequence... prefixes) {
        if (isEmpty(text) || ArrayKit.isEmpty(prefixes)) {
            return false;
        }

        for (final CharSequence prefix : prefixes) {
            if (startWith(text, prefix, true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a string starts with a specified prefix. If both the given string and prefix are {@code null}, returns
     * {@code true}. If either is {@code null} (but not both), returns {@code false}.
     *
     * @param text       The string to check.
     * @param prefix     The prefix to check for.
     * @param ignoreCase Whether to ignore case during comparison.
     * @return {@code true} if the string starts with the prefix, {@code false} otherwise.
     */
    public static boolean startWith(final CharSequence text, final CharSequence prefix, final boolean ignoreCase) {
        return startWith(text, prefix, ignoreCase, false);
    }

    /**
     * Checks if a string starts with a specified prefix. If both the given string and prefix are {@code null}, returns
     * {@code true}. If either is {@code null} (but not both), returns {@code false}.
     *
     * <pre>
     *     CharsBacker.startWith("123", "123", false, true);   -- false
     *     CharsBacker.startWith("ABCDEF", "abc", true, true); -- true
     *     CharsBacker.startWith("abc", "abc", true, true);    -- false
     * </pre>
     *
     * @param text         The string to check.
     * @param prefix       The prefix to check for.
     * @param ignoreCase   Whether to ignore case during comparison.
     * @param ignoreEquals Whether to ignore the case where the string and prefix are equal.
     * @return {@code true} if the string starts with the prefix (and optionally not equal), {@code false} otherwise.
     */
    public static boolean startWith(
            final CharSequence text,
            final CharSequence prefix,
            final boolean ignoreCase,
            final boolean ignoreEquals) {
        return new OffsetMatcher(ignoreCase, ignoreEquals, true).test(text, prefix);
    }

    /**
     * Checks if a string ends with a given character.
     *
     * @param text The string to check.
     * @param c    The character to check for.
     * @return {@code true} if the string ends with the character, {@code false} otherwise.
     */
    public static boolean endWith(final CharSequence text, final char c) {
        if (isEmpty(text)) {
            return false;
        }
        return c == text.charAt(text.length() - 1);
    }

    /**
     * Checks if a string ends with a specified suffix.
     *
     * @param text   The string to check.
     * @param suffix The suffix to check for.
     * @return {@code true} if the string ends with the suffix, {@code false} otherwise.
     */
    public static boolean endWith(final CharSequence text, final CharSequence suffix) {
        return endWith(text, suffix, false);
    }

    /**
     * Checks if a string ends with a specified suffix, ignoring case.
     *
     * @param text   The string to check.
     * @param suffix The suffix to check for.
     * @return {@code true} if the string ends with the suffix (case-insensitive), {@code false} otherwise.
     */
    public static boolean endWithIgnoreCase(final CharSequence text, final CharSequence suffix) {
        return endWith(text, suffix, true);
    }

    /**
     * Checks if a given string ends with any of the specified suffixes. Returns {@code false} if the given string or
     * the array of suffixes is empty.
     *
     * @param text     The string to check.
     * @param suffixes An array of suffixes to check against.
     * @return {@code true} if the string ends with any of the suffixes, {@code false} otherwise.
     */
    public static boolean endWithAny(final CharSequence text, final CharSequence... suffixes) {
        if (isEmpty(text) || ArrayKit.isEmpty(suffixes)) {
            return false;
        }

        for (final CharSequence suffix : suffixes) {
            if (endWith(text, suffix, false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a given string ends with any of the specified suffixes, ignoring case. Returns {@code false} if the
     * given string or the array of suffixes is empty.
     *
     * @param text     The string to check.
     * @param suffixes An array of suffixes to check against.
     * @return {@code true} if the string ends with any of the suffixes (case-insensitive), {@code false} otherwise.
     */
    public static boolean endWithAnyIgnoreCase(final CharSequence text, final CharSequence... suffixes) {
        if (isEmpty(text) || ArrayKit.isEmpty(suffixes)) {
            return false;
        }

        for (final CharSequence suffix : suffixes) {
            if (endWith(text, suffix, true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a string ends with a specified suffix. If both the given string and suffix are {@code null}, returns
     * {@code true}. If either is {@code null} (but not both), returns {@code false}.
     *
     * @param text       The string to check.
     * @param suffix     The suffix to check for.
     * @param ignoreCase Whether to ignore case during comparison.
     * @return {@code true} if the string ends with the suffix, {@code false} otherwise.
     */
    public static boolean endWith(final CharSequence text, final CharSequence suffix, final boolean ignoreCase) {
        return endWith(text, suffix, ignoreCase, false);
    }

    /**
     * Checks if a string ends with a specified suffix. If both the given string and suffix are {@code null}, returns
     * {@code true}. If either is {@code null} (but not both), returns {@code false}.
     *
     * @param text         The string to check.
     * @param suffix       The suffix to check for.
     * @param ignoreCase   Whether to ignore case during comparison.
     * @param ignoreEquals Whether to ignore the case where the string and suffix are equal.
     * @return {@code true} if the string ends with the suffix (and optionally not equal), {@code false} otherwise.
     */
    public static boolean endWith(
            final CharSequence text,
            final CharSequence suffix,
            final boolean ignoreCase,
            final boolean ignoreEquals) {
        return new OffsetMatcher(ignoreCase, ignoreEquals, false).test(text, suffix);
    }

    /**
     * Checks if a specified character appears in a string.
     *
     * @param text The string to search within.
     * @param args The character to search for.
     * @return {@code true} if the character is found, {@code false} otherwise.
     */
    public static boolean contains(final CharSequence text, final char args) {
        return indexOf(text, args) > -1;
    }

    /**
     * Checks if a specified string appears in another string.
     *
     * @param text The string to search within.
     * @param args The string to search for.
     * @return {@code true} if the string is found, {@code false} otherwise.
     */
    public static boolean contains(final CharSequence text, final CharSequence args) {
        if (null == text || null == args) {
            return false;
        }
        return text.toString().contains(args);
    }

    /**
     * Finds if the specified string contains any of the strings in a given list.
     *
     * @param text The string to check.
     * @param args An array of strings to check for.
     * @return {@code true} if the string contains any of the specified strings, {@code false} otherwise.
     */
    public static boolean containsAny(final CharSequence text, final CharSequence... args) {
        return null != getContainsString(text, args);
    }

    /**
     * Finds if the specified string contains any of the characters in a given list.
     *
     * @param text The string to check.
     * @param args An array of characters to check for.
     * @return {@code true} if the string contains any of the specified characters, {@code false} otherwise.
     */
    public static boolean containsAny(final CharSequence text, final char... args) {
        if (isNotEmpty(text)) {
            final int len = text.length();
            for (int i = 0; i < len; i++) {
                if (ArrayKit.contains(args, text.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the specified string contains only the given characters. This means that all characters in the string
     * must be present in the {@code args} set. {@code args} acts as a limiting set; characters in the string must be
     * within this set.
     * <ul>
     * <li>If {@code text} is {@code null} and {@code args} is {@code null}, returns {@code true}.</li>
     * <li>If {@code text} is {@code null} and {@code args} is not {@code null}, returns {@code true}.</li>
     * <li>If {@code text} is not {@code null} and {@code args} is {@code null}, returns {@code false}.</li>
     * </ul>
     *
     * @param text The string to check.
     * @param args The characters allowed in the string.
     * @return {@code true} if the string contains only the specified characters, {@code false} otherwise.
     */
    public static boolean containsOnly(final CharSequence text, final char... args) {
        if (isNotEmpty(text)) {
            final int len = text.length();
            for (int i = 0; i < len; i++) {
                if (!ArrayKit.contains(args, text.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if the given string contains any whitespace characters (including space, tab, full-width space, and
     * non-breaking space). Returns {@code false} if the given string is {@code null} or empty.
     *
     * @param text The string to check.
     * @return {@code true} if the string contains whitespace, {@code false} otherwise.
     */
    public static boolean containsBlank(final CharSequence text) {
        if (null == text) {
            return false;
        }
        final int length = text.length();
        if (0 == length) {
            return false;
        }

        for (int i = 0; i < length; i += 1) {
            if (CharKit.isBlankChar(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds if the specified string contains any of the strings in a given list. If found, returns the first matching
     * string.
     *
     * @param text The string to check.
     * @param args An array of strings to check for.
     * @return The first string from {@code args} found within {@code text}, or {@code null} if none are found.
     */
    public static String getContainsString(final CharSequence text, final CharSequence... args) {
        if (isEmpty(text) || ArrayKit.isEmpty(args)) {
            return null;
        }
        for (final CharSequence checkStr : args) {
            if (contains(text, checkStr)) {
                return checkStr.toString();
            }
        }
        return null;
    }

    /**
     * Checks if a string contains another string, ignoring case. If both parameters are {@code null}, returns
     * {@code true}.
     *
     * @param text The string to search within.
     * @param args The string to search for.
     * @return {@code true} if the string contains the other string (case-insensitive), {@code false} otherwise.
     */
    public static boolean containsIgnoreCase(final CharSequence text, final CharSequence args) {
        if (null == text) {
            // If the monitored string and
            return null == args;
        }
        return indexOfIgnoreCase(text, args) > -1;
    }

    /**
     * Finds if the specified string contains any of the strings in a given list, ignoring case.
     *
     * @param text The string to check.
     * @param args An array of strings to check for.
     * @return {@code true} if the string contains any of the specified strings (case-insensitive), {@code false}
     *         otherwise.
     */
    public static boolean containsAnyIgnoreCase(final CharSequence text, final CharSequence... args) {
        return null != getContainsStrIgnoreCase(text, args);
    }

    /**
     * Finds if the specified string contains any of the strings in a given list, ignoring case. If found, returns the
     * first matching string.
     *
     * @param text The string to check.
     * @param args An array of strings to check for.
     * @return The first string from {@code args} found within {@code text} (case-insensitive), or {@code null} if none
     *         are found.
     */
    public static String getContainsStrIgnoreCase(final CharSequence text, final CharSequence... args) {
        if (isEmpty(text) || ArrayKit.isEmpty(args)) {
            return null;
        }
        for (final CharSequence testStr : args) {
            if (containsIgnoreCase(text, testStr)) {
                return testStr.toString();
            }
        }
        return null;
    }

    /**
     * Checks if the specified string contains all of the given strings.
     *
     * @param text The string to check.
     * @param args An array of strings to check for.
     * @return {@code true} if the string contains all of the specified strings, {@code false} otherwise.
     */
    public static boolean containsAll(final CharSequence text, final CharSequence... args) {
        if (isBlank(text) || ArrayKit.isEmpty(args)) {
            return false;
        }
        for (final CharSequence testChar : args) {
            if (!contains(text, testChar)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds the first occurrence of a specified character in a string.
     *
     * @param text The string to search within.
     * @param args The character to search for.
     * @return The index of the first occurrence of the character, or -1 if not found.
     */
    public static int indexOf(final CharSequence text, final char args) {
        return indexOf(text, args, 0);
    }

    /**
     * Finds the first occurrence of a specified character in a string, starting from a given index.
     *
     * @param text  The string to search within.
     * @param args  The character to search for.
     * @param start The starting index for the search. If less than 0, the search starts from index 0.
     * @return The index of the first occurrence of the character, or -1 if not found.
     */
    public static int indexOf(final CharSequence text, final char args, final int start) {
        if (text instanceof String) {
            return ((String) text).indexOf(args, start);
        } else {
            return indexOf(text, args, start, -1);
        }
    }

    /**
     * Finds the first occurrence of a specified character in a string within a given range.
     *
     * @param text  The string to search within.
     * @param args  The character to search for.
     * @param start The starting index for the search. If less than 0, the search starts from index 0.
     * @param end   The ending index for the search. If greater than {@code text.length()}, it defaults to
     *              {@code text.length()}.
     * @return The index of the first occurrence of the character, or -1 if not found.
     */
    public static int indexOf(final CharSequence text, final char args, final int start, final int end) {
        if (isEmpty(text)) {
            return Normal.__1;
        }
        return new CharFinder(args).setText(text).setEndIndex(end).start(start);
    }

    /**
     * Finds the first occurrence of a character matching a predicate in a string within a given range.
     *
     * @param text    The string to search within.
     * @param matcher The character predicate to match.
     * @param start   The starting index for the search. If less than 0, the search starts from index 0.
     * @param end     The ending index for the search. If greater than {@code text.length()}, it defaults to
     *                {@code text.length()}.
     * @return The index of the first matching character, or -1 if not found.
     */
    public static int indexOf(
            final CharSequence text,
            final Predicate<Character> matcher,
            final int start,
            final int end) {
        if (isEmpty(text)) {
            return Normal.__1;
        }
        return new MatcherFinder(matcher).setText(text).setEndIndex(end).start(start);
    }

    /**
     * Finds the first occurrence of a specified string in another string, ignoring case.
     *
     * <pre>
     * indexOfIgnoreCase(null, *, *)          = -1
     * indexOfIgnoreCase(*, null, *)          = -1
     * indexOfIgnoreCase("", "", 0)           = 0
     * indexOfIgnoreCase("aabaabaa", "A", 0)  = 0
     * indexOfIgnoreCase("aabaabaa", "B", 0)  = 2
     * indexOfIgnoreCase("aabaabaa", "AB", 0) = 1
     * indexOfIgnoreCase("aabaabaa", "B", 3)  = 5
     * indexOfIgnoreCase("aabaabaa", "B", 9)  = -1
     * indexOfIgnoreCase("aabaabaa", "B", -1) = 2
     * indexOfIgnoreCase("aabaabaa", "", 2)   = 2
     * indexOfIgnoreCase("abc", "", 9)        = -1
     * </pre>
     *
     * @param text The string to search within.
     * @param args The string to search for.
     * @return The index of the first occurrence of the string (case-insensitive), or -1 if not found.
     */
    public static int indexOfIgnoreCase(final CharSequence text, final CharSequence args) {
        return indexOfIgnoreCase(text, args, 0);
    }

    /**
     * Finds the first occurrence of a specified string in another string, ignoring case, starting from a given index.
     *
     * <pre>
     * indexOfIgnoreCase(null, *, *)          = -1
     * indexOfIgnoreCase(*, null, *)          = -1
     * indexOfIgnoreCase("", "", 0)           = 0
     * indexOfIgnoreCase("aabaabaa", "A", 0)  = 0
     * indexOfIgnoreCase("aabaabaa", "B", 0)  = 2
     * indexOfIgnoreCase("aabaabaa", "AB", 0) = 1
     * indexOfIgnoreCase("aabaabaa", "B", 3)  = 5
     * indexOfIgnoreCase("aabaabaa", "B", 9)  = -1
     * indexOfIgnoreCase("aabaabaa", "B", -1) = 2
     * indexOfIgnoreCase("aabaabaa", "", 2)   = 2
     * indexOfIgnoreCase("abc", "", 9)        = -1
     * </pre>
     *
     * @param text      The string to search within.
     * @param args      The string to search for.
     * @param fromIndex The starting index for the search.
     * @return The index of the first occurrence of the string (case-insensitive), or -1 if not found.
     */
    public static int indexOfIgnoreCase(final CharSequence text, final CharSequence args, final int fromIndex) {
        return indexOf(text, args, fromIndex, true);
    }

    /**
     * Finds the first occurrence of a specified string in another string, with optional case-insensitivity.
     *
     * @param text       The string to search within. Returns -1 if empty.
     * @param args       The string to search for. Returns -1 if empty.
     * @param from       The starting index (inclusive).
     * @param ignoreCase Whether to ignore case during comparison.
     * @return The index of the first occurrence of the string, or -1 if not found.
     */
    public static int indexOf(
            final CharSequence text,
            final CharSequence args,
            final int from,
            final boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(args)) {
            if (equals(text, args)) {
                return 0;
            } else {
                return Normal.__1;
            }
        }
        return StringFinder.of(args, ignoreCase).setText(text).start(from);
    }

    /**
     * Finds the last occurrence of a specified string in another string, ignoring case.
     *
     * @param text The string to search within.
     * @param args The string to search for.
     * @return The index of the last occurrence of the string (case-insensitive), or -1 if not found.
     */
    public static int lastIndexOfIgnoreCase(final CharSequence text, final CharSequence args) {
        return lastIndexOfIgnoreCase(text, args, text.length());
    }

    /**
     * Finds the last occurrence of a specified string in another string, ignoring case, searching backwards from a
     * given index.
     *
     * @param text      The string to search within.
     * @param args      The string to search for.
     * @param fromIndex The starting index for the backward search.
     * @return The index of the last occurrence of the string (case-insensitive), or -1 if not found.
     */
    public static int lastIndexOfIgnoreCase(final CharSequence text, final CharSequence args, final int fromIndex) {
        return lastIndexOf(text, args, fromIndex, true);
    }

    /**
     * Finds the last occurrence of a specified string in another string, with optional case-insensitivity, searching
     * backwards from a given index.
     *
     * @param text       The string to search within.
     * @param args       The string to search for.
     * @param from       The starting index for the backward search.
     * @param ignoreCase Whether to ignore case during comparison.
     * @return The index of the last occurrence of the string, or -1 if not found.
     */
    public static int lastIndexOf(
            final CharSequence text,
            final CharSequence args,
            final int from,
            final boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(args)) {
            if (equals(text, args)) {
                return 0;
            } else {
                return Normal.__1;
            }
        }
        return StringFinder.of(args, ignoreCase).setText(text).setNegative(true).start(from);
    }

    /**
     * Returns the index of the {@code ordinal}-th occurrence of the string {@code args} in the string {@code text}.
     *
     * <p>
     * If {@code text} is {@code null}, {@code args} is {@code null}, or {@code ordinal <= 0}, returns -1. This method
     * is inspired by Apache-Commons-Lang.
     * <p>
     * Examples (* represents any character):
     *
     * <pre>
     * ordinalIndexOf(null, *, *)          = -1
     * ordinalIndexOf(*, null, *)          = -1
     * ordinalIndexOf("", "", *)           = 0
     * ordinalIndexOf("aabaabaa", "a", 1)  = 0
     * ordinalIndexOf("aabaabaa", "a", 2)  = 1
     * ordinalIndexOf("aabaabaa", "b", 1)  = 2
     * ordinalIndexOf("aabaabaa", "b", 2)  = 5
     * ordinalIndexOf("aabaabaa", "ab", 1) = 1
     * ordinalIndexOf("aabaabaa", "ab", 2) = 4
     * ordinalIndexOf("aabaabaa", "", 1)   = 0
     * ordinalIndexOf("aabaabaa", "", 2)   = 0
     * </pre>
     *
     * @param text    The string to search within, may be {@code null}.
     * @param args    The string to search for, may be {@code null}.
     * @param ordinal The occurrence number (e.g., 1st, 2nd).
     * @return The index of the {@code ordinal}-th occurrence, or -1 if not found or invalid input.
     */
    public static int ordinalIndexOf(final CharSequence text, final CharSequence args, final int ordinal) {
        if (text == null || args == null || ordinal <= 0) {
            return Normal.__1;
        }
        if (args.isEmpty()) {
            return 0;
        }
        int found = 0;
        int index = Normal.__1;
        do {
            index = indexOf(text, args, index + 1, false);
            if (index < 0) {
                return index;
            }
            found++;
        } while (found < ordinal);
        return index;
    }

    /**
     * Removes all occurrences of a specified string from another string. Example: {@code removeAll("aa-bb-cc-dd", "-")}
     * returns {@code "aabbccdd"}.
     *
     * @param text The string to modify.
     * @param args The string to remove.
     * @return The string with all occurrences of {@code args} removed.
     */
    public static String removeAll(final CharSequence text, final CharSequence args) {
        // If args is empty, no need to proceed.
        if (isEmpty(text) || isEmpty(args)) {
            return toStringOrNull(text);
        }
        return text.toString().replace(args, Normal.EMPTY);
    }

    /**
     * Removes all occurrences of multiple specified strings from another string. Example:
     * {@code removeAll("aa-bb-cc-dd", "a", "b")} returns {@code "--cc-dd"}.
     *
     * @param text The string to modify.
     * @param args An array of strings to remove.
     * @return The string with all occurrences of the specified strings removed.
     */
    public static String removeAll(final CharSequence text, final CharSequence... args) {
        String result = toStringOrNull(text);
        if (isNotEmpty(text)) {
            for (final CharSequence remove : args) {
                result = removeAll(result, remove);
            }
        }
        return result;
    }

    /**
     * Removes all occurrences of specified characters from a string.
     *
     * @param text  The string to modify.
     * @param chars An array of characters to remove.
     * @return The string with all occurrences of the specified characters removed.
     */
    public static String removeAll(final CharSequence text, final char... chars) {
        if (isEmpty(text) || ArrayKit.isEmpty(chars)) {
            return toStringOrNull(text);
        }
        return filter(text, (c) -> !ArrayKit.contains(chars, c));
    }

    /**
     * Removes all line breaks from a string, including:
     *
     * <pre>
     * 1. \r (carriage return)
     * 2. \n (newline)
     * </pre>
     *
     * @param text The string to process.
     * @return The string with all line breaks removed.
     */
    public static String removeAllLineBreaks(final CharSequence text) {
        return removeAll(text, Symbol.C_CR, Symbol.C_LF);
    }

    /**
     * Removes a specified length from the beginning of a string and converts the first character of the remaining
     * string to lowercase. Example: {@code text="setName", preLength=3} returns {@code "name"}.
     *
     * @param text      The string to process.
     * @param preLength The length to remove from the beginning.
     * @return The processed string, or {@code null} if the input is {@code null}.
     */
    public static String removePreAndLowerFirst(final CharSequence text, final int preLength) {
        if (text == null) {
            return null;
        }
        if (text.length() > preLength) {
            final char first = Character.toLowerCase(text.charAt(preLength));
            if (text.length() > preLength + 1) {
                return first + text.toString().substring(preLength + 1);
            }
            return String.valueOf(first);
        } else {
            return text.toString();
        }
    }

    /**
     * Removes a specified prefix from a string and converts the first character of the remaining string to lowercase.
     * Example: {@code text="setName", prefix="set"} returns {@code "name"}.
     *
     * @param text   The string to process.
     * @param prefix The prefix to remove.
     * @return The processed string, or {@code null} if the input is {@code null}.
     */
    public static String removePreAndLowerFirst(final CharSequence text, final CharSequence prefix) {
        return lowerFirst(removePrefix(text, prefix));
    }

    /**
     * Removes a specified prefix from a string.
     *
     * @param text   The string to modify.
     * @param prefix The prefix to remove.
     * @return The string with the prefix removed. If the string does not start with the prefix, the original string is
     *         returned.
     */
    public static String removePrefix(final CharSequence text, final CharSequence prefix) {
        return removePrefix(text, prefix, false);
    }

    /**
     * Removes a specified prefix from a string, ignoring case.
     *
     * @param text   The string to modify.
     * @param prefix The prefix to remove.
     * @return The string with the prefix removed. If the string does not start with the prefix (case-insensitive), the
     *         original string is returned.
     */
    public static String removePrefixIgnoreCase(final CharSequence text, final CharSequence prefix) {
        return removePrefix(text, prefix, true);
    }

    /**
     * Removes a specified prefix from a string, with optional case-insensitivity.
     *
     * @param text       The string to modify.
     * @param prefix     The prefix to remove.
     * @param ignoreCase Whether to ignore case during comparison.
     * @return The string with the prefix removed. If the string does not start with the prefix, the original string is
     *         returned.
     */
    public static String removePrefix(final CharSequence text, final CharSequence prefix, final boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(prefix)) {
            return toStringOrNull(text);
        }

        final String text2 = text.toString();
        if (startWith(text, prefix, ignoreCase)) {
            return subSuf(text2, prefix.length());// Extract the latter part
        }
        return text2;
    }

    /**
     * Removes all occurrences of a specified suffix from a string.
     *
     * <pre>{@code
     *     str=11abab, suffix=ab => return 11
     *     str=11ab, suffix=ab => return 11
     *     str=11ab, suffix="" => return 11ab
     *     str=11ab, suffix=null => return 11ab
     * }</pre>
     *
     * @param text   The string to modify. Returns the original string if empty.
     * @param suffix The suffix to remove. Returns the original string if empty.
     * @return The string with all occurrences of the suffix removed. If the string does not end with the suffix, the
     *         original string is returned.
     */
    public static String removeAllSuffix(final CharSequence text, final CharSequence suffix) {
        if (isEmpty(text) || isEmpty(suffix)) {
            return toStringOrNull(text);
        }

        final String suffixStr = suffix.toString();
        final int suffixLength = suffixStr.length();

        final String str2 = text.toString();
        int toIndex = str2.length();
        while (str2.startsWith(suffixStr, toIndex - suffixLength)) {
            toIndex -= suffixLength;
        }
        return subPre(str2, toIndex);
    }

    /**
     * Removes a specified suffix from a string.
     *
     * @param text   The string to modify.
     * @param suffix The suffix to remove.
     * @return The string with the suffix removed. If the string does not end with the suffix, the original string is
     *         returned.
     */
    public static String removeSuffix(final CharSequence text, final CharSequence suffix) {
        if (isEmpty(text) || isEmpty(suffix)) {
            return toStringOrNull(text);
        }

        final String text2 = text.toString();
        if (text2.endsWith(suffix.toString())) {
            // Extract the former part
            return subPre(text2, text2.length() - suffix.length());
        }
        return text2;
    }

    /**
     * Removes a specified suffix from a string and converts the first character of the remaining string to lowercase.
     *
     * @param text   The string to process.
     * @param suffix The suffix to remove.
     * @return The processed string. If the string does not end with the suffix, the original string is returned.
     */
    public static String removeSufAndLowerFirst(final CharSequence text, final CharSequence suffix) {
        return lowerFirst(removeSuffix(text, suffix));
    }

    /**
     * Removes a specified suffix from a string, ignoring case.
     *
     * @param text   The string to modify.
     * @param suffix The suffix to remove.
     * @return The string with the suffix removed. If the string does not end with the suffix (case-insensitive), the
     *         original string is returned.
     */
    public static String removeSuffixIgnoreCase(final CharSequence text, final CharSequence suffix) {
        if (isEmpty(text) || isEmpty(suffix)) {
            return toStringOrNull(text);
        }

        final String text2 = text.toString();
        if (endWithIgnoreCase(text, suffix)) {
            return subPre(text2, text2.length() - suffix.length());
        }
        return text2;
    }

    /**
     * Cleans blank characters from a string.
     *
     * @param text The string to clean.
     * @return The string with blank characters removed.
     */
    public static String cleanBlank(final CharSequence text) {
        return filter(text, c -> !CharKit.isBlankChar(c));
    }

    /**
     * Strips a specified prefix or suffix string from both ends of a string.
     *
     * @param text           The string to process.
     * @param prefixOrSuffix The prefix or suffix to strip.
     * @return The processed string.
     */
    public static String strip(final CharSequence text, final CharSequence prefixOrSuffix) {
        if (equals(text, prefixOrSuffix)) {
            // Special handling for stripping identical strings
            return Normal.EMPTY;
        }
        return strip(text, prefixOrSuffix, prefixOrSuffix);
    }

    /**
     * Strips a specified prefix and suffix string from both ends of a string. If the characters exist on both sides,
     * they are removed; otherwise, no action is taken.
     *
     * @param text   The string to process. {@code null} is ignored.
     * @param prefix The prefix to strip. {@code null} is ignored.
     * @param suffix The suffix to strip. {@code null} is ignored.
     * @return The processed string.
     */
    public static String strip(final CharSequence text, final CharSequence prefix, final CharSequence suffix) {
        return strip(text, prefix, suffix, false);
    }

    /**
     * Strips a specified prefix and suffix string from both ends of a string. If the characters exist on both sides,
     * they are removed; otherwise, no action is taken.
     *
     * <pre>{@code
     *  "aaa_STRIPPED_bbb", "a", "b"       -> "aa_STRIPPED_bb"
     *  "aaa_STRIPPED_bbb", null, null     -> "aaa_STRIPPED_bbb"
     *  "aaa_STRIPPED_bbb", "", ""         -> "aaa_STRIPPED_bbb"
     *  "aaa_STRIPPED_bbb", "", "b"        -> "aaa_STRIPPED_bb"
     *  "aaa_STRIPPED_bbb", null, "b"      -> "aaa_STRIPPED_bb"
     *  "aaa_STRIPPED_bbb", "a", ""        -> "aa_STRIPPED_bbb"
     *  "aaa_STRIPPED_bbb", "a", null      -> "aa_STRIPPED_bbb"
     *
     *  "a", "a", "a"  -> ""
     * }</pre>
     *
     * @param text       The string to process.
     * @param prefix     The prefix to strip. {@code null} is ignored.
     * @param suffix     The suffix to strip. {@code null} is ignored.
     * @param ignoreCase Whether to ignore case during comparison.
     * @return The processed string.
     */
    public static String strip(
            final CharSequence text,
            final CharSequence prefix,
            final CharSequence suffix,
            final boolean ignoreCase) {
        if (isEmpty(text)) {
            return toStringOrNull(text);
        }

        int from = 0;
        int to = text.length();

        final String text2 = text.toString();
        if (startWith(text2, prefix, ignoreCase)) {
            from = prefix.length();
        }
        if (endWith(text2, suffix, ignoreCase)) {
            to -= suffix.length();
        }

        return text2.substring(Math.min(from, to), Math.max(from, to));
    }

    /**
     * Strips a specified prefix or suffix string from both ends of a string, ignoring case.
     *
     * @param text           The string to process.
     * @param prefixOrSuffix The prefix or suffix to strip.
     * @return The processed string.
     */
    public static String stripIgnoreCase(final CharSequence text, final CharSequence prefixOrSuffix) {
        return stripIgnoreCase(text, prefixOrSuffix, prefixOrSuffix);
    }

    /**
     * Strips a specified prefix and suffix string from both ends of a string, ignoring case.
     *
     * @param text   The string to process.
     * @param prefix The prefix to strip.
     * @param suffix The suffix to strip.
     * @return The processed string.
     */
    public static String stripIgnoreCase(
            final CharSequence text,
            final CharSequence prefix,
            final CharSequence suffix) {
        return strip(text, prefix, suffix, true);
    }

    /**
     * Adds a prefix to a string if it doesn't already start with that prefix.
     *
     * @param text   The string to modify.
     * @param prefix The prefix to add.
     * @return The string with the prefix added if missing.
     * @see #prependIfMissing(CharSequence, CharSequence, CharSequence...)
     */
    public static String addPrefixIfNot(final CharSequence text, final CharSequence prefix) {
        return prependIfMissing(text, prefix, prefix);
    }

    /**
     * Adds a suffix to a string if it doesn't already end with that suffix.
     *
     * @param text   The string to modify.
     * @param suffix The suffix to add.
     * @return The string with the suffix added if missing.
     * @see #appendIfMissing(CharSequence, CharSequence, CharSequence...)
     */
    public static String addSuffixIfNot(final CharSequence text, final CharSequence suffix) {
        return appendIfMissing(text, suffix);
    }

    /**
     * Cuts a string into N equal parts.
     *
     * @param text       The string to cut.
     * @param partLength The length of each part.
     * @return An array of strings representing the cut parts.
     */
    public static String[] cut(final CharSequence text, final int partLength) {
        return CharsBacker.splitByLength(text, partLength);
    }

    /**
     * Improved JDK substring method.
     * <ul>
     * <li>Index starts from 0, the last character is -1, e.g., {@code sub("miaixz", 0, -1)} returns "miaixz".</li>
     * <li>If {@code fromIndexInclude} and {@code toIndexExclude} are the same, returns "".</li>
     * <li>If {@code fromIndexInclude} or {@code toIndexExclude} is negative, the position is counted from the end of
     * the string. If the absolute value is greater than the string length, {@code fromIndexInclude} defaults to 0, and
     * {@code toIndexExclude} defaults to length.</li>
     * <li>If the adjusted {@code fromIndexInclude} is greater than {@code toIndexExclude}, they are swapped. E.g.,
     * {@code "abcdefgh", 2, 3} returns "c", {@code "abcdefgh", 2, -3} returns "cde".</li>
     * </ul>
     *
     * @param text             The string to extract from.
     * @param fromIndexInclude The starting index (inclusive).
     * @param toIndexExclude   The ending index (exclusive).
     * @return The extracted substring.
     */
    public static String sub(final CharSequence text, int fromIndexInclude, int toIndexExclude) {
        if (isEmpty(text)) {
            return toStringOrNull(text);
        }
        final int len = text.length();

        if (fromIndexInclude < 0) {
            fromIndexInclude = len + fromIndexInclude;
            if (fromIndexInclude < 0) {
                fromIndexInclude = 0;
            }
        } else if (fromIndexInclude > len) {
            fromIndexInclude = len;
        }

        if (toIndexExclude < 0) {
            toIndexExclude = len + toIndexExclude;
            if (toIndexExclude < 0) {
                toIndexExclude = len;
            }
        } else if (toIndexExclude > len) {
            toIndexExclude = len;
        }

        if (toIndexExclude < fromIndexInclude) {
            final int tmp = fromIndexInclude;
            fromIndexInclude = toIndexExclude;
            toIndexExclude = tmp;
        }

        if (fromIndexInclude == toIndexExclude) {
            return Normal.EMPTY;
        }

        return text.toString().substring(fromIndexInclude, toIndexExclude);
    }

    /**
     * Extracts a substring based on CodePoints, which can handle Emoji characters.
     *
     * @param text      The string to extract from.
     * @param fromIndex The starting CodePoint index (inclusive).
     * @param toIndex   The ending CodePoint index (exclusive).
     * @return The extracted substring.
     * @throws IllegalArgumentException if {@code fromIndex} is negative or {@code fromIndex > toIndex}.
     */
    public static String subByCodePoint(final CharSequence text, final int fromIndex, final int toIndex) {
        if (isEmpty(text)) {
            return toStringOrNull(text);
        }

        if (fromIndex < 0 || fromIndex > toIndex) {
            throw new IllegalArgumentException();
        }

        if (fromIndex == toIndex) {
            return Normal.EMPTY;
        }

        final StringBuilder sb = new StringBuilder();
        final int subLen = toIndex - fromIndex;
        text.toString().codePoints().skip(fromIndex).limit(subLen).forEach(v -> sb.append(Character.toChars(v)));
        return sb.toString();
    }

    /**
     * Extracts a substring, treating each Chinese character as having a length of 2 bytes (GBK encoding).
     *
     * @param text   The string to extract from.
     * @param len    The byte length to cut to (inclusive).
     * @param suffix The suffix to append after cutting.
     * @return The extracted substring with the suffix appended.
     */
    public static String subPreGbk(final CharSequence text, final int len, final CharSequence suffix) {
        return subPreGbk(text, len, true) + suffix;
    }

    /**
     * Extracts a substring, treating each Chinese character as having a length of 2 bytes (GBK encoding). Customizes
     * {@code halfUp}: if {@code len} is 10 and the last character is a half-character after cutting, {@code true} means
     * keep it (length becomes 11), otherwise discard it (length becomes 9).
     *
     * @param text   The string to extract from.
     * @param len    The byte length to cut to (inclusive).
     * @param halfUp Whether to keep a half-cut GBK character.
     * @return The extracted substring.
     */
    public static String subPreGbk(final CharSequence text, int len, final boolean halfUp) {
        if (isEmpty(text)) {
            return toStringOrNull(text);
        }

        int counterOfDoubleByte = 0;
        final byte[] b = ByteKit.toBytes(text, Charset.GBK);
        if (b.length <= len) {
            return text.toString();
        }
        for (int i = 0; i < len; i++) {
            if (b[i] < 0) {
                counterOfDoubleByte++;
            }
        }

        if (counterOfDoubleByte % 2 != 0) {
            if (halfUp) {
                len += 1;
            } else {
                len -= 1;
            }
        }
        return new String(b, 0, len, Charset.GBK);
    }

    /**
     * Extracts the substring before a specified index. Safe substring method, allowing {@code null} string and string
     * length less than {@code toIndexExclude}.
     *
     * <pre>{@code
     * Assert.assertEquals(subPre(null, 3), null);
     * Assert.assertEquals(subPre("ab", 3), "ab");
     * Assert.assertEquals(subPre("abc", 3), "abc");
     * Assert.assertEquals(subPre("abcd", 3), "abc");
     * Assert.assertEquals(subPre("abcd", -3), "a");
     * Assert.assertEquals(subPre("ab", 3), "ab");
     * }</pre>
     *
     * @param text           The string to extract from.
     * @param toIndexExclude The ending index (exclusive).
     * @return The substring before the specified index.
     */
    public static String subPre(final CharSequence text, final int toIndexExclude) {
        if (isEmpty(text) || text.length() == toIndexExclude) {
            return toStringOrNull(text);
        }
        return sub(text, 0, toIndexExclude);
    }

    /**
     * Extracts the substring after a specified index.
     * <ul>
     * <li>If {@code fromIndex} is 0 or the string is empty, returns the original string.</li>
     * <li>If {@code fromIndex} is greater than the string's length, returns "".</li>
     * <li>{@code fromIndex} supports negative values, where -1 means {@code length - 1}.</li>
     * </ul>
     *
     * @param text      The string to extract from.
     * @param fromIndex The starting index (inclusive).
     * @return The substring after the specified index.
     */
    public static String subSuf(final CharSequence text, final int fromIndex) {
        if (0 == fromIndex || isEmpty(text)) {
            return toStringOrNull(text);
        }
        return sub(text, fromIndex, text.length());
    }

    /**
     * Extracts a substring of a specified length from the end of a string.
     *
     * <pre>
     * subSufByLength("abcde", 3)      =    "cde"
     * subSufByLength("abcde", 0)      =    ""
     * subSufByLength("abcde", -5)     =    ""
     * subSufByLength("abcde", -1)     =    ""
     * subSufByLength("abcde", 5)       =    "abcde"
     * subSufByLength("abcde", 10)     =    "abcde"
     * subSufByLength(null, 3)               =    null
     * </pre>
     *
     * @param text   The string to extract from.
     * @param length The length of the substring to extract from the end.
     * @return The extracted substring from the end.
     */
    public static String subSufByLength(final CharSequence text, final int length) {
        if (isEmpty(text)) {
            return null;
        }
        if (length <= 0) {
            return Normal.EMPTY;
        }
        return sub(text, -length, text.length());
    }

    /**
     * Extracts a substring of a specified length from a given starting position. When {@code fromIndex} is positive, it
     * refers to an insertion point, as follows:
     *
     * <pre>
     *     0   1   2   3   4
     *       A   B   C   D
     * </pre>
     *
     * When {@code fromIndex} is negative, it refers to a reverse insertion point, where -1 is the position before the
     * last character:
     *
     * <pre>
     *       -3   -2   -1   length
     *     A    B    C    D
     * </pre>
     *
     * @param input     The original string.
     * @param fromIndex The starting index (inclusive), can be negative.
     * @param length    The length of the substring to extract.
     * @return The extracted substring.
     */
    public static String subByLength(final String input, final int fromIndex, final int length) {
        if (isEmpty(input)) {
            return null;
        }
        if (length <= 0) {
            return Normal.EMPTY;
        }

        final int toIndex;
        if (fromIndex < 0) {
            toIndex = fromIndex - length;
        } else {
            toIndex = fromIndex + length;
        }
        return sub(input, fromIndex, toIndex);
    }

    /**
     * Extracts the substring before the first or last occurrence of a separator. The separator itself is not included.
     * If the given string is empty ({@code null} or ""), or the separator is {@code null}, the original string is
     * returned. If the separator is an empty string "", an empty string is returned. If the separator is not found, the
     * original string is returned. Examples:
     *
     * <pre>
     * subBefore(null, *, false)      = null
     * subBefore("", *, false)        = ""
     * subBefore("abc", "a", false)   = ""
     * subBefore("abcba", "b", false) = "a"
     * subBefore("abc", "c", false)   = "ab"
     * subBefore("abc", "d", false)   = "abc"
     * subBefore("abc", "", false)    = ""
     * subBefore("abc", null, false)  = "abc"
     * </pre>
     *
     * @param text            The string to search within.
     * @param separator       The separator string (exclusive).
     * @param isLastSeparator Whether to find the last occurrence of the separator (if multiple exist).
     * @return The substring before the separator.
     */
    public static String subBefore(
            final CharSequence text,
            final CharSequence separator,
            final boolean isLastSeparator) {
        if (isEmpty(text) || separator == null) {
            return null == text ? null : text.toString();
        }

        final String string = text.toString();
        final String sep = separator.toString();
        if (sep.isEmpty()) {
            return Normal.EMPTY;
        }
        final int pos = isLastSeparator ? string.lastIndexOf(sep) : string.indexOf(sep);
        if (Normal.__1 == pos) {
            return string;
        }
        if (0 == pos) {
            return Normal.EMPTY;
        }
        return string.substring(0, pos);
    }

    /**
     * Extracts the substring before the first or last occurrence of a separator character. The separator itself is not
     * included. If the given string is empty ({@code null} or ""), or the separator is {@code null}, the original
     * string is returned. If the separator is not found, the original string is returned. Examples:
     *
     * <pre>
     * subBefore(null, *, false)      = null
     * subBefore("", *, false)        = ""
     * subBefore("abc", 'a', false)   = ""
     * subBefore("abcba", 'b', false) = "a"
     * subBefore("abc", 'c', false)   = "ab"
     * subBefore("abc", 'd', false)   = "abc"
     * </pre>
     *
     * @param text            The string to search within.
     * @param separator       The separator character (exclusive).
     * @param isLastSeparator Whether to find the last occurrence of the separator (if multiple exist).
     * @return The substring before the separator.
     */
    public static String subBefore(final CharSequence text, final char separator, final boolean isLastSeparator) {
        if (isEmpty(text)) {
            return null == text ? null : Normal.EMPTY;
        }

        final String newText = text.toString();
        final int pos = isLastSeparator ? newText.lastIndexOf(separator) : newText.indexOf(separator);
        if (Normal.__1 == pos) {
            return newText;
        }
        if (0 == pos) {
            return Normal.EMPTY;
        }
        return newText.substring(0, pos);
    }

    /**
     * Extracts the substring after the first or last occurrence of a separator. The separator itself is not included.
     * If the given string is empty ({@code null} or ""), the original string is returned. If the separator is
     * {@code null} or "", an empty string is returned. If the separator is not found, an empty string is returned.
     * Examples:
     *
     * <pre>
     * subAfter(null, *, false)      = null
     * subAfter("", *, false)        = ""
     * subAfter(*, null, false)      = ""
     * subAfter("abc", "a", false)   = "bc"
     * subAfter("abcba", "b", false) = "cba"
     * subAfter("abc", "c", false)   = ""
     * subAfter("abc", "d", false)   = ""
     * subAfter("abc", "", false)    = "abc"
     * </pre>
     *
     * @param text            The string to search within.
     * @param separator       The separator string (exclusive).
     * @param isLastSeparator Whether to find the last occurrence of the separator (if multiple exist).
     * @return The substring after the separator.
     */
    public static String subAfter(
            final CharSequence text,
            final CharSequence separator,
            final boolean isLastSeparator) {
        if (isEmpty(text)) {
            return null == text ? null : Normal.EMPTY;
        }
        if (separator == null) {
            return Normal.EMPTY;
        }
        final String newText = text.toString();
        final String sep = separator.toString();
        final int pos = isLastSeparator ? newText.lastIndexOf(sep) : newText.indexOf(sep);
        if (Normal.__1 == pos || (text.length() - 1) == pos) {
            return Normal.EMPTY;
        }
        return newText.substring(pos + separator.length());
    }

    /**
     * Extracts the substring after the first or last occurrence of a separator character. The separator itself is not
     * included. If the given string is empty ({@code null} or ""), the original string is returned. If the separator is
     * not found, an empty string is returned. Examples:
     *
     * <pre>
     * subAfter(null, *, false)      = null
     * subAfter("", *, false)        = ""
     * subAfter("abc", 'a', false)   = "bc"
     * subAfter("abcba", 'b', false) = "cba"
     * subAfter("abc", 'c', false)   = ""
     * subAfter("abc", 'd', false)   = ""
     * </pre>
     *
     * @param text            The string to search within.
     * @param separator       The separator character (exclusive).
     * @param isLastSeparator Whether to find the last occurrence of the separator (if multiple exist).
     * @return The substring after the separator.
     */
    public static String subAfter(final CharSequence text, final char separator, final boolean isLastSeparator) {
        if (isEmpty(text)) {
            return null == text ? null : Normal.EMPTY;
        }
        final String newText = text.toString();
        final int pos = isLastSeparator ? newText.lastIndexOf(separator) : newText.indexOf(separator);
        if (Normal.__1 == pos) {
            return Normal.EMPTY;
        }
        return newText.substring(pos + 1);
    }

    /**
     * Extracts the substring between two specified marker strings. The markers themselves are not included.
     *
     * <pre>
     * subBetween("wx[b]yz", "[", "]") = "b"
     * subBetween(null, *, *)          = null
     * subBetween(*, null, *)          = null
     * subBetween(*, *, null)          = null
     * subBetween("", "", "")          = ""
     * subBetween("", "", "]")         = null
     * subBetween("", "[", "]")        = null
     * subBetween("yabcz", "", "")     = ""
     * subBetween("yabcz", "y", "z")   = "abc"
     * subBetween("yabczyabcz", "y", "z")   = "abc"
     * </pre>
     *
     * @param text   The string to extract from.
     * @param before The starting marker string.
     * @param after  The ending marker string.
     * @return The substring between the markers, or {@code null} if not found or invalid input.
     */
    public static String subBetween(final CharSequence text, final CharSequence before, final CharSequence after) {
        if (text == null || before == null || after == null) {
            return null;
        }

        final String text2 = text.toString();
        final String before2 = before.toString();
        final String after2 = after.toString();

        final int start = text2.indexOf(before2);
        if (start != Normal.__1) {
            final int end = text2.indexOf(after2, start + before2.length());
            if (end != Normal.__1) {
                return text2.substring(start + before2.length(), end);
            }
        }
        return null;
    }

    /**
     * Extracts the substring between two identical specified marker strings. The markers themselves are not included.
     *
     * <pre>
     * subBetween(null, *)            = null
     * subBetween("", "")             = ""
     * subBetween("", "tag")          = null
     * subBetween("tagabctag", null)  = null
     * subBetween("tagabctag", "")    = ""
     * subBetween("tagabctag", "tag") = "abc"
     * </pre>
     *
     * @param text           The string to extract from.
     * @param beforeAndAfter The identical starting and ending marker string.
     * @return The substring between the markers, or {@code null} if not found or invalid input.
     */
    public static String subBetween(final CharSequence text, final CharSequence beforeAndAfter) {
        return subBetween(text, beforeAndAfter, beforeAndAfter);
    }

    /**
     * Extracts multiple substrings between specified marker strings. The markers themselves are not included.
     *
     * <pre>
     * subBetweenAll("wx[b]y[z]", "[", "]") 		= ["b","z"]
     * subBetweenAll(null, *, *)          		= []
     * subBetweenAll(*, null, *)          		= []
     * subBetweenAll(*, *, null)          		= []
     * subBetweenAll("", "", "")          		= []
     * subBetweenAll("", "", "]")         		= []
     * subBetweenAll("", "[", "]")        		= []
     * subBetweenAll("yabcz", "", "")     		= []
     * subBetweenAll("yabcz", "y", "z")   		= ["abc"]
     * subBetweenAll("yabczyabcz", "y", "z")   	= ["abc","abc"]
     * subBetweenAll("[yabc[zy]abcz]", "[", "]");   = ["zy"]           // Only extracts the innermost when overlapping
     * </pre>
     *
     * @param text   The string to extract from.
     * @param prefix The starting marker string.
     * @param suffix The ending marker string.
     * @return An array of extracted substrings. Returns an empty array if no matches or invalid input.
     */
    public static String[] subBetweenAll(
            final CharSequence text,
            final CharSequence prefix,
            final CharSequence suffix) {
        if (hasEmpty(text, prefix, suffix) ||
        // If the starting string is not contained, there is certainly no substring.
                !contains(text, prefix)) {
            return new String[0];
        }

        final List<String> result = new LinkedList<>();
        final String[] split = splitToArray(text, prefix);
        if (prefix.equals(suffix)) {
            // Special handling for identical prefix and suffix
            for (int i = 1, length = split.length - 1; i < length; i += 2) {
                result.add(split[i]);
            }
        } else {
            int suffixIndex;
            String fragment;
            for (int i = 1; i < split.length; i++) {
                fragment = split[i];
                suffixIndex = fragment.indexOf(suffix.toString());
                if (suffixIndex > 0) {
                    result.add(fragment.substring(0, suffixIndex));
                }
            }
        }

        return result.toArray(new String[0]);
    }

    /**
     * Extracts multiple substrings between identical specified marker strings. The markers themselves are not included.
     *
     * <pre>
     * subBetweenAll(null, *)          		= []
     * subBetweenAll(*, null)          		= []
     * subBetweenAll(*, *)          		= []
     * subBetweenAll("", "")          		= []
     * subBetweenAll("", "#")         		= []
     * subBetweenAll("gotanks", "")     		= []
     * subBetweenAll("#gotanks#", "#")   	= ["gotanks"]
     * subBetweenAll("#hello# #world#!", "#")   = ["hello", "world"]
     * subBetweenAll("#hello# world#!", "#");   = ["hello"]
     * </pre>
     *
     * @param text            The string to extract from.
     * @param prefixAndSuffix The identical starting and ending marker string.
     * @return An array of extracted substrings. Returns an empty array if no matches or invalid input.
     */
    public static String[] subBetweenAll(final CharSequence text, final CharSequence prefixAndSuffix) {
        return subBetweenAll(text, prefixAndSuffix, prefixAndSuffix);
    }

    /**
     * Repeats a character a specified number of times.
     *
     * <pre>
     * repeat('e', 0)  = ""
     * repeat('e', 3)  = "eee"
     * repeat('e', -2) = ""
     * </pre>
     *
     * @param c     The character to repeat.
     * @param count The number of times to repeat. If less than or equal to 0, returns "".
     * @return The string consisting of the repeated character.
     */
    public static String repeat(final char c, final int count) {
        if (count <= 0) {
            return Normal.EMPTY;
        }
        return StringRepeater.of(count).repeat(c);
    }

    /**
     * Repeats a string a specified number of times.
     *
     * @param text  The string to repeat.
     * @param count The number of times to repeat.
     * @return The string consisting of the repeated string.
     */
    public static String repeat(final CharSequence text, final int count) {
        if (null == text) {
            return null;
        }
        return StringRepeater.of(count).repeat(text);
    }

    /**
     * Repeats a string to a specified total length.
     * <ul>
     * <li>If the specified length is not an integer multiple of the string's length, it is truncated to the fixed
     * length.</li>
     * <li>If the specified length is less than the string's own length, it is truncated.</li>
     * </ul>
     *
     * @param text   The string to repeat.
     * @param padLen The target total length.
     * @return The string repeated to the specified length.
     */
    public static String repeatByLength(final CharSequence text, final int padLen) {
        if (null == text) {
            return null;
        }
        if (padLen <= 0) {
            return Normal.EMPTY;
        }
        return StringRepeater.of(padLen).repeatByLength(text);
    }

    /**
     * Repeats a string and joins the repetitions with a delimiter.
     *
     * <pre>
     * repeatAndJoin("?", 5, ",")   = "?,?,?,?,?"
     * repeatAndJoin("?", 0, ",")   = ""
     * repeatAndJoin("?", 5, null) = "?????"
     * </pre>
     *
     * @param text      The string to repeat.
     * @param count     The number of times to repeat.
     * @param delimiter The delimiter to use between repetitions.
     * @return The joined string.
     */
    public static String repeatAndJoin(final CharSequence text, final int count, final CharSequence delimiter) {
        if (count <= 0) {
            return Normal.EMPTY;
        }
        return StringRepeater.of(count).repeatAndJoin(text, delimiter);
    }

    /**
     * Compares two strings for equality (case-sensitive).
     *
     * <pre>
     * equals(null, null)   = true
     * equals(null, "abc")  = false
     * equals("abc", null)  = false
     * equals("abc", "abc") = true
     * equals("abc", "ABC") = false
     * </pre>
     *
     * @param text1 The first string to compare.
     * @param text2 The second string to compare.
     * @return {@code true} if the strings are equal or both are {@code null}, {@code false} otherwise.
     */
    public static boolean equals(final CharSequence text1, final CharSequence text2) {
        return equals(text1, text2, false);
    }

    /**
     * Compares two strings for equality (case-insensitive).
     *
     * <pre>
     * equalsIgnoreCase(null, null)   = true
     * equalsIgnoreCase(null, "abc")  = false
     * equalsIgnoreCase("abc", null)  = false
     * equalsIgnoreCase("abc", "abc") = true
     * equalsIgnoreCase("abc", "ABC") = true
     * </pre>
     *
     * @param text1 The first string to compare.
     * @param text2 The second string to compare.
     * @return {@code true} if the strings are equal (case-insensitive) or both are {@code null}, {@code false}
     *         otherwise.
     */
    public static boolean equalsIgnoreCase(final CharSequence text1, final CharSequence text2) {
        return equals(text1, text2, true);
    }

    /**
     * Compares two strings for equality based on the following rules:
     * <ul>
     * <li>If both {@code text1} and {@code text2} are {@code null}, they are considered equal.</li>
     * <li>If {@code ignoreCase} is {@code true}, uses {@link String#equalsIgnoreCase(String)}.</li>
     * <li>If {@code ignoreCase} is {@code false}, uses {@link String#contentEquals(CharSequence)}.</li>
     * </ul>
     *
     * @param text1      The first string to compare.
     * @param text2      The second string to compare.
     * @param ignoreCase Whether to ignore case during comparison.
     * @return {@code true} if the strings are equal (optionally case-insensitive) or both are {@code null},
     *         {@code false} otherwise.
     */
    public static boolean equals(final CharSequence text1, final CharSequence text2, final boolean ignoreCase) {
        if (null == text1) {
            // Only if both are null are they considered equal
            return text2 == null;
        }
        if (null == text2) {
            // If string2 is null and string1 is not null, return false
            return false;
        }

        if (ignoreCase) {
            return text1.toString().equalsIgnoreCase(text2.toString());
        } else {
            return text1.toString().contentEquals(text2);
        }
    }

    /**
     * Checks if the given string is equal to any of the provided strings (case-insensitive). Returns {@code true} if a
     * match is found, {@code false} otherwise. If the comparison list is empty, returns {@code false}.
     *
     * @param text1 The string to check.
     * @param strs  The list of strings to compare against.
     * @return {@code true} if {@code text1} is equal to any string in {@code strs} (case-insensitive), {@code false}
     *         otherwise.
     */
    public static boolean equalsAnyIgnoreCase(final CharSequence text1, final CharSequence... strs) {
        return equalsAny(text1, true, strs);
    }

    /**
     * Checks if the given string is equal to any of the provided strings. Returns {@code true} if a match is found,
     * {@code false} otherwise. If the comparison list is empty, returns {@code false}.
     *
     * @param text1 The string to check.
     * @param strs  The list of strings to compare against.
     * @return {@code true} if {@code text1} is equal to any string in {@code strs}, {@code false} otherwise.
     */
    public static boolean equalsAny(final CharSequence text1, final CharSequence... strs) {
        return equalsAny(text1, false, strs);
    }

    /**
     * Checks if the given string is equal to any of the provided strings, with optional case-insensitivity. Returns
     * {@code true} if a match is found, {@code false} otherwise. If the comparison list is empty, returns
     * {@code false}.
     *
     * @param text       The string to check.
     * @param ignoreCase Whether to ignore case during comparison.
     * @param args       The list of strings to compare against.
     * @return {@code true} if {@code text} is equal to any string in {@code args} (optionally case-insensitive),
     *         {@code false} otherwise.
     */
    public static boolean equalsAny(final CharSequence text, final boolean ignoreCase, final CharSequence... args) {
        if (ArrayKit.isEmpty(args)) {
            return false;
        }

        for (final CharSequence cs : args) {
            if (equals(text, cs, ignoreCase)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the character at a specified position in a string is equal to a given character. Returns {@code false}
     * if the string is {@code null}, or the position is out of bounds.
     *
     * @param text     The string to check.
     * @param position The position to check.
     * @param c        The character to compare against.
     * @return {@code true} if the character at the position matches, {@code false} otherwise.
     */
    public static boolean equalsCharAt(final CharSequence text, final int position, final char c) {
        if (null == text || position < 0) {
            return false;
        }
        return text.length() > position && c == text.charAt(position);
    }

    /**
     * Compares a substring of the first string with the entire second string (length-matched), with optional
     * case-insensitivity. Returns {@code false} if either string is {@code null}.
     *
     * @param text1      The first string.
     * @param offset1    The starting offset in the first string.
     * @param text2      The second string.
     * @param ignoreCase Whether to ignore case during comparison.
     * @return {@code true} if the substrings are equal, {@code false} otherwise.
     * @see String#regionMatches(boolean, int, String, int, int)
     */
    public static boolean isSubEquals(
            final CharSequence text1,
            final int offset1,
            final CharSequence text2,
            final boolean ignoreCase) {
        return isSubEquals(text1, offset1, text2, 0, text2.length(), ignoreCase);
    }

    /**
     * Compares different parts of two strings (with equal length), with optional case-insensitivity. Returns
     * {@code false} if either string is {@code null}.
     *
     * @param text1      The first string.
     * @param offset1    The starting offset in the first string.
     * @param text2      The second string.
     * @param offset2    The starting offset in the second string.
     * @param length     The length of the substring to compare.
     * @param ignoreCase Whether to ignore case during comparison.
     * @return {@code true} if the substrings are equal, {@code false} otherwise.
     * @see String#regionMatches(boolean, int, String, int, int)
     */
    public static boolean isSubEquals(
            final CharSequence text1,
            final int offset1,
            final CharSequence text2,
            final int offset2,
            final int length,
            final boolean ignoreCase) {
        if (null == text1 || null == text2) {
            return false;
        }

        return text1.toString().regionMatches(ignoreCase, offset1, text2.toString(), offset2, length);
    }

    /**
     * Formats text using "{}" as placeholders. This method simply replaces placeholders "{}" in order with the provided
     * arguments. To output "{}", use "\\{}" to escape. To output "\" before "{}", use "\\\\{}" to double escape.
     * Example: Normal usage: {@code format("this is {} for {}", "a", "b")} returns "this is a for b". Escaping {}:
     * {@code format("this is \\{} for {}", "a", "b")} returns "this is {} for a". Escaping \: {@code format("this is
     * \\\\{} for {}", "a", "b")} returns "this is \a for b".
     *
     * @param format The text template, with "{}" representing placeholders. If {@code null}, returns "null".
     * @param args   The argument values.
     * @return The formatted text. If the template is {@code null}, returns "null".
     */
    public static String format(final CharSequence format, final Object... args) {
        if (null == format) {
            return Normal.NULL;
        }
        if (ArrayKit.isEmpty(args) || isBlank(format)) {
            return format.toString();
        }
        return StringFormatter.format(format.toString(), args);
    }

    /**
     * Formats text using indexed placeholders, e.g., "{0}", "{1}". Normal usage: {@code indexedFormat("this is {0} for
     * {1}", "a", "b")} returns "this is a for b".
     *
     * @param pattern   The text pattern.
     * @param arguments The arguments to fill the placeholders.
     * @return The formatted text.
     */
    public static String indexedFormat(final CharSequence pattern, final Object... arguments) {
        return MessageFormat.format(pattern.toString(), arguments);
    }

    /**
     * Wraps a specified string with identical prefix and suffix.
     *
     * @param text            The string to wrap.
     * @param prefixAndSuffix The prefix and suffix string.
     * @return The wrapped string.
     */
    public static String wrap(final CharSequence text, final CharSequence prefixAndSuffix) {
        return wrap(text, prefixAndSuffix, prefixAndSuffix);
    }

    /**
     * Wraps a specified string with a prefix and a suffix.
     *
     * @param text   The string to wrap.
     * @param prefix The prefix string.
     * @param suffix The suffix string.
     * @return The wrapped string.
     */
    public static String wrap(final CharSequence text, final CharSequence prefix, final CharSequence suffix) {
        return toStringOrEmpty(prefix).concat(toStringOrEmpty(text)).concat(toStringOrEmpty(suffix));
    }

    /**
     * Wraps a specified string with a prefix character and a suffix character.
     *
     * @param text   The string to wrap.
     * @param prefix The prefix character.
     * @param suffix The suffix character.
     * @return The wrapped string.
     */
    public static String wrap(final CharSequence text, final char prefix, final char suffix) {
        return prefix + toStringOrEmpty(text) + suffix;
    }

    /**
     * Wraps multiple strings with a single pair of prefix and suffix characters.
     *
     * @param prefixAndSuffix The prefix and suffix string.
     * @param strs            Multiple strings to wrap.
     * @return An array of wrapped strings.
     */
    public static String[] wrapAllWithPair(final CharSequence prefixAndSuffix, final CharSequence... strs) {
        return wrapAll(prefixAndSuffix, prefixAndSuffix, strs);
    }

    /**
     * Wraps multiple strings with a specified prefix and suffix.
     *
     * @param prefix The prefix string.
     * @param suffix The suffix string.
     * @param strs   Multiple strings to wrap.
     * @return An array of wrapped strings.
     */
    public static String[] wrapAll(final CharSequence prefix, final CharSequence suffix, final CharSequence... strs) {
        final String[] results = new String[strs.length];
        for (int i = 0; i < strs.length; i++) {
            results[i] = wrap(strs[i], prefix, suffix);
        }
        return results;
    }

    /**
     * Wraps a specified string with a prefix and suffix, but only if the string is not already wrapped.
     *
     * @param text   The string to wrap.
     * @param prefix The prefix string.
     * @param suffix The suffix string.
     * @return The wrapped string, or the original string if already wrapped.
     */
    public static String wrapIfMissing(final CharSequence text, final CharSequence prefix, final CharSequence suffix) {
        int len = 0;
        if (isNotEmpty(text)) {
            len += text.length();
        }
        if (isNotEmpty(prefix)) {
            len += prefix.length();
        }
        if (isNotEmpty(suffix)) {
            len += suffix.length();
        }
        final StringBuilder sb = new StringBuilder(len);
        if (isNotEmpty(prefix) && !startWith(text, prefix)) {
            sb.append(prefix);
        }
        if (isNotEmpty(text)) {
            sb.append(text);
        }
        if (isNotEmpty(suffix) && !endWith(text, suffix)) {
            sb.append(suffix);
        }
        return sb.toString();
    }

    /**
     * Wraps multiple strings with a single pair of prefix and suffix characters, but only if the strings are not
     * already wrapped.
     *
     * @param prefixAndSuffix The prefix and suffix string.
     * @param strs            Multiple strings to wrap.
     * @return An array of wrapped strings.
     */
    public static String[] wrapAllWithPairIfMissing(final CharSequence prefixAndSuffix, final CharSequence... strs) {
        return wrapAllIfMissing(prefixAndSuffix, prefixAndSuffix, strs);
    }

    /**
     * Wraps multiple strings with a specified prefix and suffix, but only if the strings are not already wrapped.
     *
     * @param prefix The prefix string.
     * @param suffix The suffix string.
     * @param strs   Multiple strings to wrap.
     * @return An array of wrapped strings.
     */
    public static String[] wrapAllIfMissing(
            final CharSequence prefix,
            final CharSequence suffix,
            final CharSequence... strs) {
        final String[] results = new String[strs.length];
        for (int i = 0; i < strs.length; i++) {
            results[i] = wrapIfMissing(strs[i], prefix, suffix);
        }
        return results;
    }

    /**
     * Unwraps a string by removing the specified prefix and suffix. If the string is not wrapped, the original string
     * is returned. This method requires both the prefix and suffix to be present; if only one is found, no removal
     * occurs.
     *
     * @param text   The string to unwrap.
     * @param prefix The prefix string to remove.
     * @param suffix The suffix string to remove.
     * @return The unwrapped string.
     */
    public static String unWrap(final CharSequence text, final String prefix, final String suffix) {
        if (isWrap(text, prefix, suffix)) {
            return sub(text, prefix.length(), text.length() - suffix.length());
        }
        return text.toString();
    }

    /**
     * Unwraps a string by removing the specified prefix and suffix characters. If the string is not wrapped, the
     * original string is returned.
     *
     * @param text   The string to unwrap.
     * @param prefix The prefix character to remove.
     * @param suffix The suffix character to remove.
     * @return The unwrapped string.
     */
    public static String unWrap(final CharSequence text, final char prefix, final char suffix) {
        if (isEmpty(text)) {
            return toStringOrNull(text);
        }
        if (isWrap(text, prefix, suffix)) {
            return sub(text, 1, text.length() - 1);
        }
        return text.toString();
    }

    /**
     * Unwraps a string by removing identical prefix and suffix characters. If the string is not wrapped, the original
     * string is returned.
     *
     * @param text            The string to unwrap.
     * @param prefixAndSuffix The identical prefix and suffix character to remove.
     * @return The unwrapped string.
     */
    public static String unWrap(final CharSequence text, final char prefixAndSuffix) {
        return unWrap(text, prefixAndSuffix, prefixAndSuffix);
    }

    /**
     * Checks if a specified string is wrapped by a given prefix and suffix.
     *
     * @param text   The string to check.
     * @param prefix The prefix string.
     * @param suffix The suffix string.
     * @return {@code true} if the string is wrapped, {@code false} otherwise.
     */
    public static boolean isWrap(final CharSequence text, final CharSequence prefix, final CharSequence suffix) {
        if (ArrayKit.hasNull(text, prefix, suffix)) {
            return false;
        }
        if (text.length() < (prefix.length() + suffix.length())) {
            return false;
        }

        final String text2 = text.toString();
        return text2.startsWith(prefix.toString()) && text2.endsWith(suffix.toString());
    }

    /**
     * Checks if a specified string is wrapped by an identical wrapper string (both prefix and suffix).
     *
     * @param text    The string to check.
     * @param wrapper The wrapper string.
     * @return {@code true} if the string is wrapped, {@code false} otherwise.
     */
    public static boolean isWrap(final CharSequence text, final String wrapper) {
        return isWrap(text, wrapper, wrapper);
    }

    /**
     * Checks if a specified string is wrapped by an identical wrapper character (both prefix and suffix).
     *
     * @param text    The string to check.
     * @param wrapper The wrapper character.
     * @return {@code true} if the string is wrapped, {@code false} otherwise.
     */
    public static boolean isWrap(final CharSequence text, final char wrapper) {
        return isWrap(text, wrapper, wrapper);
    }

    /**
     * Checks if a specified string is wrapped by a given prefix character and suffix character.
     *
     * @param text       The string to check.
     * @param prefixChar The prefix character.
     * @param suffixChar The suffix character.
     * @return {@code true} if the string is wrapped, {@code false} otherwise.
     */
    public static boolean isWrap(final CharSequence text, final char prefixChar, final char suffixChar) {
        if (null == text || text.length() < 2) {
            return false;
        }

        return text.charAt(0) == prefixChar && text.charAt(text.length() - 1) == suffixChar;
    }

    /**
     * Gets the leftmost {@code len} characters from a string.
     *
     * <pre>
     *  left(null, *)    = null
     *  left(*, -ve)     = ""
     *  left("", *)      = ""
     *  left("abc", 0)   = ""
     *  left("abc", 2)   = "ab"
     *  left("abc", 4)   = "abc"
     * </pre>
     *
     * @param text The string to extract from, may be {@code null}.
     * @param len  The desired length of the string.
     * @return The leftmost characters, or {@code null} if the input string is {@code null}.
     */
    public static String left(final String text, final int len) {
        if (null == text) {
            return null;
        }
        if (len < 0) {
            return Normal.EMPTY;
        }
        if (text.length() <= len) {
            return text;
        }
        return text.substring(0, len);
    }

    /**
     * Gets the rightmost {@code len} characters from a string.
     *
     * <pre>
     *  right(null, *)    = null
     *  right(*, -ve)     = ""
     *  right("", *)      = ""
     *  right("abc", 0)   = ""
     *  right("abc", 2)   = "bc"
     *  right("abc", 4)   = "abc"
     * </pre>
     *
     * @param text The string to extract from, may be {@code null}.
     * @param len  The desired length of the string.
     * @return The rightmost characters, or {@code null} if the input string is {@code null}.
     */
    public static String right(final String text, final int len) {
        if (null == text) {
            return null;
        }
        if (len < 0) {
            return Normal.EMPTY;
        }
        if (text.length() <= len) {
            return text;
        }
        return text.substring(text.length() - len);
    }

    /**
     * Gets {@code len} characters from the middle of a string.
     *
     * <pre>
     *  mid(null, *, *)    = null
     *  mid(*, *, -ve)     = ""
     *  mid("", 0, *)      = ""
     *  mid("abc", 0, 2)   = "ab"
     *  mid("abc", 0, 4)   = "abc"
     *  mid("abc", 2, 4)   = "c"
     *  mid("abc", 4, 2)   = ""
     *  mid("abc", -2, 2)  = "ab"
     * </pre>
     *
     * @param text The string to extract from, may be {@code null}.
     * @param pos  The starting position. Negative values are treated as 0.
     * @param len  The desired length of the string.
     * @return The middle characters, or {@code null} if the input string is {@code null}.
     */
    public static String mid(final String text, int pos, final int len) {
        if (null == text) {
            return null;
        }
        if (len < 0 || pos > text.length()) {
            return Normal.EMPTY;
        }
        if (pos < 0) {
            pos = 0;
        }
        if (text.length() <= pos + len) {
            return text.substring(pos);
        }
        return text.substring(pos, pos + len);
    }

    /**
     * Pads the beginning of a string with a specified padding string to reach a target length. If the string's length
     * is greater than the target length, it is truncated. Similar to {@code leftPad} in Apache Commons Lang.
     *
     * <pre>
     * padPre(null, *, *);//null
     * padPre("1", 3, "ABC");//"AB1"
     * padPre("123", 2, "ABC");//"12"
     * padPre("1039", -1, "0");//"103"
     * </pre>
     *
     * @param text   The string to pad.
     * @param length The target total length.
     * @param padStr The string to use for padding.
     * @return The padded string.
     */
    public static String padPre(final CharSequence text, final int length, final CharSequence padStr) {
        if (null == text) {
            return null;
        }
        final int strLen = text.length();
        if (strLen == length) {
            return text.toString();
        } else if (strLen > length) {
            // If the provided string is longer than the specified length, truncate it.
            return subPre(text, length);
        }

        return repeatByLength(padStr, length - strLen).concat(text.toString());
    }

    /**
     * Pads the beginning of a string with a specified padding character to reach a target length. If the string's
     * length is greater than the target length, it is truncated. Similar to {@code leftPad} in Apache Commons Lang.
     *
     * <pre>
     * padPre(null, *, *);//null
     * padPre("1", 3, '0');//"001"
     * padPre("123", 2, '0');//"12"
     * </pre>
     *
     * @param text    The string to pad.
     * @param length  The target total length.
     * @param padChar The character to use for padding.
     * @return The padded string.
     */
    public static String padPre(final CharSequence text, final int length, final char padChar) {
        if (null == text) {
            return null;
        }
        final int strLen = text.length();
        if (strLen == length) {
            return text.toString();
        } else if (strLen > length) {
            // If the provided string is longer than the specified length, truncate it.
            return subPre(text, length);
        }

        return repeat(padChar, length - strLen).concat(text.toString());
    }

    /**
     * Pads the end of a string with a specified padding character to reach a target length. If the string's length is
     * greater than the target length, it is truncated.
     *
     * <pre>
     * padAfter(null, *, *);//null
     * padAfter("1", 3, '0');//"100"
     * padAfter("123", 2, '0');//"23"
     * padAfter("123", -1, '0')//"" Empty string
     * </pre>
     *
     * @param text    The string to pad. If {@code null}, returns {@code null}.
     * @param length  The target total length.
     * @param padChar The character to use for padding.
     * @return The padded string.
     */
    public static String padAfter(final CharSequence text, final int length, final char padChar) {
        if (null == text) {
            return null;
        }
        final int strLen = text.length();
        if (strLen == length) {
            return text.toString();
        } else if (strLen > length) {
            // If the provided string is longer than the specified length, truncate it.
            return sub(text, strLen - length, strLen);
        }

        return text.toString().concat(repeat(padChar, length - strLen));
    }

    /**
     * Pads the end of a string with a specified padding string to reach a target length. If the string's length is
     * greater than the target length, it is truncated.
     *
     * <pre>
     * padAfter(null, *, *);//null
     * padAfter("1", 3, "ABC");//"1AB"
     * padAfter("123", 2, "ABC");//"23"
     * </pre>
     *
     * @param text   The string to pad. If {@code null}, returns {@code null}.
     * @param length The target total length.
     * @param padStr The string to use for padding.
     * @return The padded string.
     */
    public static String padAfter(final CharSequence text, final int length, final CharSequence padStr) {
        if (null == text) {
            return null;
        }
        final int strLen = text.length();
        if (strLen == length) {
            return text.toString();
        } else if (strLen > length) {
            // If the provided string is longer than the specified length, truncate it.
            return subSufByLength(text, length);
        }

        return text.toString().concat(repeatByLength(padStr, length - strLen));
    }

    /**
     * Centers a string by padding both sides with spaces to reach a specified length. If the specified length is less
     * than the string's length, the original string is returned.
     *
     * <pre>
     * center(null, *)   = null
     * center("", 4)     = "    "
     * center("ab", -1)  = "ab"
     * center("ab", 4)   = " ab "
     * center("abcd", 2) = "abcd"
     * center("a", 4)    = " a  "
     * </pre>
     *
     * @param text The string to center.
     * @param size The target total length.
     * @return The centered string.
     */
    public static String center(final CharSequence text, final int size) {
        return center(text, size, Symbol.C_SPACE);
    }

    /**
     * Centers a string by padding both sides with a specified character to reach a specified length. If the specified
     * length is less than the string's length, the original string is returned.
     *
     * <pre>
     * center(null, *, *)     = null
     * center("", 4, ' ')     = "    "
     * center("ab", -1, ' ')  = "ab"
     * center("ab", 4, ' ')   = " ab "
     * center("abcd", 2, ' ') = "abcd"
     * center("a", 4, ' ')    = " a  "
     * center("a", 4, 'y')    = "yayy"
     * center("abc", 7, ' ')  = "  abc  "
     * </pre>
     *
     * @param text    The string to center.
     * @param size    The target total length.
     * @param padChar The character to use for padding.
     * @return The centered string.
     */
    public static String center(CharSequence text, final int size, final char padChar) {
        if (text == null || size <= 0) {
            return toStringOrNull(text);
        }
        final int strLen = text.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return text.toString();
        }
        text = padPre(text, strLen + pads / 2, padChar);
        text = padAfter(text, size, padChar);
        return text.toString();
    }

    /**
     * Centers a string by padding both sides with a specified padding string to reach a specified length. If the
     * specified length is less than the string's length, the original string is returned.
     *
     * <pre>
     * center(null, *, *)     = null
     * center("", 4, " ")     = "    "
     * center("ab", -1, " ")  = "ab"
     * center("ab", 4, " ")   = " ab "
     * center("abcd", 2, " ") = "abcd"
     * center("a", 4, " ")    = " a  "
     * center("a", 4, "yz")   = "yayz"
     * center("abc", 7, null) = "  abc  "
     * center("abc", 7, "")   = "  abc  "
     * </pre>
     *
     * @param text   The string to center.
     * @param size   The target total length.
     * @param padStr The string to use for padding.
     * @return The centered string.
     */
    public static String center(CharSequence text, final int size, CharSequence padStr) {
        if (text == null || size <= 0) {
            return toStringOrNull(text);
        }
        if (isEmpty(padStr)) {
            padStr = Symbol.SPACE;
        }
        final int strLen = text.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return text.toString();
        }
        text = padPre(text, strLen + pads / 2, padStr);
        text = padAfter(text, size, padStr);
        return text.toString();
    }

    /**
     * Counts the occurrences of a specified substring within a string. Returns {@code 0} if any parameter is
     * {@code null} or empty.
     *
     * <pre>
     * count(null, *)       = 0
     * count("", *)         = 0
     * count("abba", null)  = 0
     * count("abba", "")    = 0
     * count("abba", "a")   = 2
     * count("abba", "ab")  = 1
     * count("abba", "xxx") = 0
     * </pre>
     *
     * @param content      The string to search within.
     * @param strForSearch The substring to count.
     * @return The number of occurrences of the substring.
     */
    public static int count(final CharSequence content, final CharSequence strForSearch) {
        if (hasEmpty(content, strForSearch) || strForSearch.length() > content.length()) {
            return 0;
        }

        int count = 0;
        int idx = 0;
        final String content2 = content.toString();
        final String strForSearch2 = strForSearch.toString();
        while ((idx = content2.indexOf(strForSearch2, idx)) > -1) {
            count++;
            idx += strForSearch.length();
        }
        return count;
    }

    /**
     * Counts the occurrences of a specified character within a string.
     *
     * @param content       The string to search within.
     * @param charForSearch The character to count.
     * @return The number of occurrences of the character.
     */
    public static int count(final CharSequence content, final char charForSearch) {
        int count = 0;
        if (isEmpty(content)) {
            return 0;
        }
        final int contentLength = content.length();
        for (int i = 0; i < contentLength; i++) {
            if (charForSearch == content.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Compares two strings for sorting purposes.
     *
     * <pre>
     * compare(null, null, *)     = 0
     * compare(null , "a", true)  &lt; 0
     * compare(null , "a", false) &gt; 0
     * compare("a", null, true)   &gt; 0
     * compare("a", null, false)  &lt; 0
     * compare("abc", "abc", *)   = 0
     * compare("a", "b", *)       &lt; 0
     * compare("b", "a", *)       &gt; 0
     * compare("a", "B", *)       &gt; 0
     * compare("ab", "abc", *)    &lt; 0
     * </pre>
     *
     * @param text1      The first string.
     * @param text2      The second string.
     * @param nullIsLess If {@code true}, {@code null} values are considered smaller than non-null values.
     * @return A negative integer, zero, or a positive integer as the first string is less than, equal to, or greater
     *         than the second.
     */
    public static int compare(final CharSequence text1, final CharSequence text2, final boolean nullIsLess) {
        if (text1 == text2) {
            return 0;
        }
        if (text1 == null) {
            return nullIsLess ? -1 : 1;
        }
        if (text2 == null) {
            return nullIsLess ? 1 : -1;
        }
        return text1.toString().compareTo(text2.toString());
    }

    /**
     * Compares two strings for sorting purposes, ignoring case.
     *
     * <pre>
     * compareIgnoreCase(null, null, *)     = 0
     * compareIgnoreCase(null , "a", true)  &lt; 0
     * compareIgnoreCase(null , "a", false) &gt; 0
     * compareIgnoreCase("a", null, true)   &gt; 0
     * compareIgnoreCase("a", null, false)  &lt; 0
     * compareIgnoreCase("abc", "abc", *)   = 0
     * compareIgnoreCase("abc", "ABC", *)   = 0
     * compareIgnoreCase("a", "b", *)       &lt; 0
     * compareIgnoreCase("b", "a", *)       &gt; 0
     * compareIgnoreCase("a", "B", *)       &lt; 0
     * compareIgnoreCase("A", "b", *)       &lt; 0
     * compareIgnoreCase("ab", "abc", *)    &lt; 0
     * </pre>
     *
     * @param text1      The first string.
     * @param text2      The second string.
     * @param nullIsLess If {@code true}, {@code null} values are considered smaller than non-null values.
     * @return A negative integer, zero, or a positive integer as the first string is less than, equal to, or greater
     *         than the second (case-insensitive).
     */
    public static int compareIgnoreCase(final CharSequence text1, final CharSequence text2, final boolean nullIsLess) {
        if (text1 == text2) {
            return 0;
        }
        if (text1 == null) {
            return nullIsLess ? -1 : 1;
        }
        if (text2 == null) {
            return nullIsLess ? 1 : -1;
        }
        return text1.toString().compareToIgnoreCase(text2.toString());
    }

    /**
     * Compares two version strings. {@code null} versions are considered the smallest.
     *
     * <pre>
     * compareVersion(null, "v1") &lt; 0
     * compareVersion("v1", "v1")  = 0
     * compareVersion(null, null)   = 0
     * compareVersion("v1", null) &gt; 0
     * compareVersion("1.0.0", "1.0.2") &lt; 0
     * compareVersion("1.0.2", "1.0.2a") &lt; 0
     * compareVersion("1.13.0", "1.12.1c") &gt; 0
     * compareVersion("V0.0.20170102", "V0.0.20170101") &gt; 0
     * </pre>
     *
     * @param version1 The first version string.
     * @param version2 The second version string.
     * @return A negative integer, zero, or a positive integer as the first version is less than, equal to, or greater
     *         than the second.
     */
    public static int compareVersion(final CharSequence version1, final CharSequence version2) {
        return VersionCompare.INSTANCE.compare(toStringOrNull(version1), toStringOrNull(version2));
    }

    /**
     * Appends a suffix to a string if it does not already end with the specified suffix or any of the additional
     * suffixes. Case-sensitive comparison.
     *
     * @param text     The string to check.
     * @param suffix   The suffix to append.
     * @param suffixes Additional suffixes to check against. If the string ends with any of these, no suffix is
     *                 appended.
     * @return The string with the suffix appended if missing, otherwise the original string.
     */
    public static String appendIfMissing(
            final CharSequence text,
            final CharSequence suffix,
            final CharSequence... suffixes) {
        return appendIfMissing(text, suffix, false, suffixes);
    }

    /**
     * Appends a suffix to a string if it does not already end with the specified suffix or any of the additional
     * suffixes. Case-insensitive comparison.
     *
     * @param text     The string to check.
     * @param suffix   The suffix to append.
     * @param suffixes Additional suffixes to check against. If the string ends with any of these, no suffix is
     *                 appended.
     * @return The string with the suffix appended if missing, otherwise the original string.
     */
    public static String appendIfMissingIgnoreCase(
            final CharSequence text,
            final CharSequence suffix,
            final CharSequence... suffixes) {
        return appendIfMissing(text, suffix, true, suffixes);
    }

    /**
     * Appends a suffix to a string if it does not already end with the specified suffix or any of the additional
     * suffixes.
     *
     * @param text         The string to check.
     * @param suffix       The suffix to append. This suffix is not used for checking existing endings.
     * @param ignoreCase   Whether to ignore case when checking for existing endings.
     * @param testSuffixes Additional suffixes to check against. If the string ends with any of these, no suffix is
     *                     appended.
     * @return The string with the suffix appended if missing, otherwise the original string.
     */
    public static String appendIfMissing(
            final CharSequence text,
            final CharSequence suffix,
            final boolean ignoreCase,
            final CharSequence... testSuffixes) {
        if (text == null || isEmpty(suffix) || endWith(text, suffix, ignoreCase)) {
            return toStringOrNull(text);
        }
        if (ArrayKit.isNotEmpty(testSuffixes)) {
            for (final CharSequence testSuffix : testSuffixes) {
                if (endWith(text, testSuffix, ignoreCase)) {
                    return text.toString();
                }
            }
        }
        return text.toString().concat(suffix.toString());
    }

    /**
     * Prepends a prefix to a string if it does not already start with the specified prefix or any of the additional
     * prefixes. Case-sensitive comparison.
     *
     * @param text     The string to check.
     * @param prefix   The prefix to prepend.
     * @param prefixes Additional prefixes to check against. If the string starts with any of these, no prefix is
     *                 prepended.
     * @return The string with the prefix prepended if missing, otherwise the original string.
     */
    public static String prependIfMissing(
            final CharSequence text,
            final CharSequence prefix,
            final CharSequence... prefixes) {
        return prependIfMissing(text, prefix, false, prefixes);
    }

    /**
     * Prepends a prefix to a string if it does not already start with the specified prefix or any of the additional
     * prefixes. Case-insensitive comparison.
     *
     * @param text     The string to check.
     * @param prefix   The prefix to prepend.
     * @param prefixes Additional prefixes to check against. If the string starts with any of these, no prefix is
     *                 prepended.
     * @return The string with the prefix prepended if missing, otherwise the original string.
     */
    public static String prependIfMissingIgnoreCase(
            final CharSequence text,
            final CharSequence prefix,
            final CharSequence... prefixes) {
        return prependIfMissing(text, prefix, true, prefixes);
    }

    /**
     * Prepends a prefix to a string if it does not already start with the specified prefix or any of the additional
     * prefixes.
     *
     * @param text       The string to check.
     * @param prefix     The prefix to prepend.
     * @param ignoreCase Whether to ignore case when checking for existing beginnings.
     * @param prefixes   Additional prefixes to check against. If the string starts with any of these, no prefix is
     *                   prepended.
     * @return The string with the prefix prepended if missing, otherwise the original string.
     */
    public static String prependIfMissing(
            final CharSequence text,
            final CharSequence prefix,
            final boolean ignoreCase,
            final CharSequence... prefixes) {
        if (text == null || isEmpty(prefix) || startWith(text, prefix, ignoreCase)) {
            return toStringOrNull(text);
        }
        if (ArrayKit.isNotEmpty(prefixes)) {
            for (final CharSequence s : prefixes) {
                if (startWith(text, s, ignoreCase)) {
                    return text.toString();
                }
            }
        }
        return prefix.toString().concat(text.toString());
    }

    /**
     * Replaces the first occurrence of a specified substring in a string.
     *
     * @param text        The string to modify.
     * @param searchStr   The substring to search for.
     * @param replacedStr The replacement string.
     * @param ignoreCase  Whether to ignore case during the search.
     * @return The string with the first occurrence replaced.
     */
    public static String replaceFirst(
            final CharSequence text,
            final CharSequence searchStr,
            final CharSequence replacedStr,
            final boolean ignoreCase) {
        if (isEmpty(text)) {
            return toStringOrNull(text);
        }
        final int startInclude = indexOf(text, searchStr, 0, ignoreCase);
        if (Normal.__1 == startInclude) {
            return toStringOrNull(text);
        }
        return replaceByCodePoint(text, startInclude, startInclude + searchStr.length(), replacedStr);
    }

    /**
     * Replaces the last occurrence of a specified substring in a string.
     *
     * @param text        The string to modify.
     * @param searchStr   The substring to search for.
     * @param replacedStr The replacement string.
     * @param ignoreCase  Whether to ignore case during the search.
     * @return The string with the last occurrence replaced.
     */
    public static String replaceLast(
            final CharSequence text,
            final CharSequence searchStr,
            final CharSequence replacedStr,
            final boolean ignoreCase) {
        if (isEmpty(text)) {
            return toStringOrNull(text);
        }
        final int lastIndex = lastIndexOf(text, searchStr, text.length(), ignoreCase);
        if (Normal.__1 == lastIndex) {
            return toStringOrNull(text);
        }
        return replace(text, lastIndex, searchStr, replacedStr, ignoreCase);
    }

    /**
     * Replaces all occurrences of a specified substring in a string, ignoring case.
     *
     * @param text        The string to modify.
     * @param searchStr   The substring to search for.
     * @param replacement The replacement string.
     * @return The string with all occurrences replaced (case-insensitive).
     */
    public static String replaceIgnoreCase(
            final CharSequence text,
            final CharSequence searchStr,
            final CharSequence replacement) {
        return replace(text, 0, searchStr, replacement, true);
    }

    /**
     * Replaces all occurrences of a specified substring in a string.
     *
     * @param text        The string to modify.
     * @param searchStr   The substring to search for.
     * @param replacement The replacement string.
     * @return The string with all occurrences replaced.
     */
    public static String replace(
            final CharSequence text,
            final CharSequence searchStr,
            final CharSequence replacement) {
        return replace(text, 0, searchStr, replacement, false);
    }

    /**
     * Replaces all occurrences of a specified substring in a string, with optional case-insensitivity.
     *
     * @param text        The string to modify.
     * @param searchStr   The substring to search for.
     * @param replacement The replacement string.
     * @param ignoreCase  Whether to ignore case during the search.
     * @return The string with all occurrences replaced.
     */
    public static String replace(
            final CharSequence text,
            final CharSequence searchStr,
            final CharSequence replacement,
            final boolean ignoreCase) {
        return replace(text, 0, searchStr, replacement, ignoreCase);
    }

    /**
     * Replaces all occurrences of a specified substring in a string, starting from a given index, with optional
     * case-insensitivity.
     *
     * @param text        The string to modify.
     * @param fromIndex   The starting position (inclusive).
     * @param searchStr   The substring to search for.
     * @param replacement The replacement string.
     * @param ignoreCase  Whether to ignore case during the search.
     * @return The string with all occurrences replaced.
     */
    public static String replace(
            final CharSequence text,
            final int fromIndex,
            final CharSequence searchStr,
            final CharSequence replacement,
            final boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(searchStr)) {
            return toStringOrNull(text);
        }
        return new SearchReplacer(fromIndex, searchStr, replacement, ignoreCase).apply(text);
    }

    /**
     * Replaces characters within a specified range of a string with a fixed character. The length of the replaced
     * section remains the same; the replacement character is repeated. This method uses {@link String#codePoints()} for
     * splitting and replacement.
     *
     * @param text         The string to modify.
     * @param beginInclude The starting position (inclusive).
     * @param endExclude   The ending position (exclusive).
     * @param replacedChar The character to replace with.
     * @return The string with the specified range replaced.
     */
    public static String replaceByCodePoint(
            final CharSequence text,
            final int beginInclude,
            final int endExclude,
            final char replacedChar) {
        return new CharRangeReplacer(beginInclude, endExclude, replacedChar, true).apply(text);
    }

    /**
     * Replaces characters within a specified range of a string with a specified string. The replacement string is used
     * once. This method uses {@link String#codePoints()} for splitting and replacement.
     *
     * @param text         The string to modify.
     * @param beginInclude The starting position (inclusive).
     * @param endExclude   The ending position (exclusive).
     * @param replacedStr  The string to replace with.
     * @return The string with the specified range replaced.
     */
    public static String replaceByCodePoint(
            final CharSequence text,
            final int beginInclude,
            final int endExclude,
            final CharSequence replacedStr) {
        return new StringRangeReplacer(beginInclude, endExclude, replacedStr, true).apply(text);
    }

    /**
     * Replaces all regex-matched text using a custom function to determine the replacement. The {@code replaceFun} can
     * extract different parts of the matched content, then reprocess and assemble them into new content to be put back
     * in place.
     *
     * <pre class="code">
     * replace(this.content, "(\d+)", parameters -> "-" + parameters.group(1) + "-")
     * // Result: "ZZZaaabbbccc中文-1234-"
     * </pre>
     *
     * @param text       The string to replace.
     * @param pattern    The regular expression pattern for matching.
     * @param replaceFun The function to determine how to replace.
     * @return The string with replacements applied.
     * @see PatternKit#replaceAll(CharSequence, java.util.regex.Pattern, FunctionX)
     */
    public static String replace(
            final CharSequence text,
            final java.util.regex.Pattern pattern,
            final FunctionX<Matcher, String> replaceFun) {
        return PatternKit.replaceAll(text, pattern, replaceFun);
    }

    /**
     * Replaces all regex-matched text using a custom function to determine the replacement.
     *
     * @param text       The string to replace.
     * @param regex      The regular expression string for matching.
     * @param replaceFun The function to determine how to replace.
     * @return The string with replacements applied.
     * @see PatternKit#replaceAll(CharSequence, String, FunctionX)
     */
    public static String replace(
            final CharSequence text,
            final String regex,
            final FunctionX<Matcher, String> replaceFun) {
        return PatternKit.replaceAll(text, regex, replaceFun);
    }

    /**
     * Replaces characters within a specified range of a string with "*".
     *
     * @param text         The string to modify.
     * @param startInclude The starting position (inclusive).
     * @param endExclude   The ending position (exclusive).
     * @return The string with the specified range hidden.
     */
    public static String hide(final CharSequence text, final int startInclude, final int endExclude) {
        return replaceByCodePoint(text, startInclude, endExclude, Symbol.C_STAR);
    }

    /**
     * Replaces all characters specified in a string of characters with a replacement string. For example, if
     * {@code chars} is "\\r\\n", then both "\\r" and "\\n" will be replaced, even if they appear individually.
     *
     * @param text        The string to modify.
     * @param chars       A string containing all characters to be replaced.
     * @param replacedStr The replacement string.
     * @return The new string with replacements.
     */
    public static String replaceChars(final CharSequence text, final String chars, final CharSequence replacedStr) {
        if (isEmpty(text) || isEmpty(chars)) {
            return toStringOrNull(text);
        }
        return replaceChars(text, chars.toCharArray(), replacedStr);
    }

    /**
     * Replaces all characters specified in a character array with a replacement string.
     *
     * @param text        The string to modify.
     * @param chars       An array of characters to be replaced.
     * @param replacedStr The replacement string.
     * @return The new string with replacements.
     */
    public static String replaceChars(final CharSequence text, final char[] chars, final CharSequence replacedStr) {
        if (isEmpty(text) || ArrayKit.isEmpty(chars)) {
            return toStringOrNull(text);
        }

        final Set<Character> set = new HashSet<>(chars.length);
        for (final char c : chars) {
            set.add(c);
        }
        final int strLen = text.length();
        final StringBuilder builder = new StringBuilder();
        char c;
        for (int i = 0; i < strLen; i++) {
            c = text.charAt(i);
            builder.append(set.contains(c) ? replacedStr : c);
        }
        return builder.toString();
    }

    /**
     * Replaces the character at a specified position in a string using a custom operator. For example, to change case.
     *
     * @param text     The string to modify.
     * @param index    The position to replace. Supports negative values, where -1 means the last character.
     * @param operator The replacement logic, which takes the original character and returns the new character.
     * @return The string with the character replaced.
     */
    public static String replaceAt(final CharSequence text, int index, final UnaryOperator<Character> operator) {
        if (text == null) {
            return null;
        }

        // Support negative indices
        final int length = text.length();
        if (index < 0) {
            index += length;
        }

        final String string = text.toString();
        if (index < 0 || index >= length) {
            return string;
        }

        // Check if there is any change before and after conversion. If no change, return the original string.
        final char c = string.charAt(index);
        final Character newC = operator.apply(c);
        if (c == newC) {
            // No change, return original string
            return string;
        }

        // Do not reuse the incoming CharSequence to prevent modifying the original object.
        final char[] chars = string.toCharArray();
        chars[index] = newC;
        return new String(chars);
    }

    /**
     * Gets the length of a {@link CharSequence}. If {@code null}, returns 0.
     *
     * @param cs The {@link CharSequence}.
     * @return The length of the {@link CharSequence}, or 0 if {@code null}.
     */
    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    /**
     * Gets the Unicode character length of a {@link CharSequence}. If {@code null}, returns 0. Unicode character length
     * refers to the actual number of Unicode characters, e.g., an emoji counts as one character.
     *
     * @param cs The {@link CharSequence}.
     * @return The Unicode character length of the {@link CharSequence}, or 0 if {@code null}.
     */
    public static int codeLength(final CharSequence cs) {
        return cs == null ? 0 : cs.toString().codePointCount(0, cs.length());
    }

    /**
     * Gets the byte length of a {@link CharSequence} after encoding with a specified charset.
     *
     * @param cs      The {@link CharSequence}.
     * @param charset The charset to use for encoding.
     * @return The byte length.
     */
    public static int byteLength(final CharSequence cs, final java.nio.charset.Charset charset) {
        return cs == null ? 0 : cs.toString().getBytes(charset).length;
    }

    /**
     * Gets the total length of an array of {@link CharSequence}s. The length of a {@code null} string is defined as 0.
     *
     * @param args An array of {@link CharSequence}s.
     * @return The total length.
     */
    public static int totalLength(final CharSequence... args) {
        int totalLength = 0;
        for (final CharSequence text : args) {
            totalLength += length(text);
        }
        return totalLength;
    }

    /**
     * Limits the length of a string. If it exceeds the specified maximum length, it is truncated to that length and
     * "..." is appended to the end.
     *
     * @param text   The string to limit.
     * @param length The maximum length.
     * @return The truncated string with "..." appended if necessary.
     * @throws IllegalArgumentException if {@code length} is not positive.
     */
    public static String limitLength(final CharSequence text, final int length) {
        Assert.isTrue(length > 0);
        if (null == text) {
            return null;
        }
        if (text.length() <= length) {
            return text.toString();
        }
        return sub(text, 0, length) + "...";
    }

    /**
     * Truncates a string so that its UTF-8 encoded byte length does not exceed {@code maxBytesLength}.
     *
     * @param text           The original string.
     * @param maxBytesLength The maximum byte length.
     * @param appendDots     Whether to append an ellipsis ("...") after truncation.
     * @return The truncated string.
     */
    public static String limitByteLengthUtf8(
            final CharSequence text,
            final int maxBytesLength,
            final boolean appendDots) {
        return limitByteLength(text, Charset.UTF_8, maxBytesLength, 4, appendDots);
    }

    /**
     * Truncates a string so that its encoded byte length (using a specified charset) does not exceed
     * {@code maxBytesLength}. This method is used to truncate the total number of bytes to a specified length. If the
     * string does not exceed the original length, it is output as is. If it exceeds, the excess part is truncated, and
     * an optional ellipsis ("...") can be added, but the total length including "..." must also not exceed the limit.
     *
     * @param text           The original string.
     * @param charset        The charset to use for encoding.
     * @param maxBytesLength The maximum byte length.
     * @param factor         A quick calculation factor, representing the maximum possible bytes for a single character
     *                       in this encoding.
     * @param appendDots     Whether to append an ellipsis ("...") after truncation.
     * @return The truncated string.
     */
    public static String limitByteLength(
            final CharSequence text,
            final java.nio.charset.Charset charset,
            final int maxBytesLength,
            final int factor,
            final boolean appendDots) {
        // Character count * quick calculation factor <= maximum byte count
        if (text == null || text.length() * factor <= maxBytesLength) {
            return toStringOrNull(text);
        }
        final byte[] sba = ByteKit.toBytes(text, charset);
        if (sba.length <= maxBytesLength) {
            return toStringOrNull(text);
        }
        // Limit byte count
        final int limitBytes;
        if (appendDots) {
            limitBytes = maxBytesLength - "...".getBytes(charset).length;
        } else {
            limitBytes = maxBytesLength;
        }
        final ByteBuffer bb = ByteBuffer.wrap(sba, 0, limitBytes);
        final CharBuffer cb = CharBuffer.allocate(limitBytes);
        final CharsetDecoder decoder = charset.newDecoder();
        // Ignore truncated characters
        decoder.onMalformedInput(CodingErrorAction.IGNORE);
        decoder.decode(bb, cb, true);
        decoder.flush(cb);
        final String result = new String(cb.array(), 0, cb.position());
        if (appendDots) {
            return result + "...";
        }
        return result;
    }

    /**
     * Returns the first non-{@code null} element from a variadic array of {@link CharSequence}s.
     *
     * @param args Multiple {@link CharSequence} elements.
     * @param <T>  The type of the elements.
     * @return The first non-{@code null} element, or {@code null} if the array is empty or all elements are
     *         {@code null}.
     */
    public static <T extends CharSequence> T firstNonNull(final T... args) {
        return ArrayKit.firstNonNull(args);
    }

    /**
     * Returns the first non-empty element from a variadic array of {@link CharSequence}s.
     *
     * @param args Multiple {@link CharSequence} elements.
     * @param <T>  The type of the elements.
     * @return The first non-empty element, or {@code null} if the array is empty or all elements are empty.
     * @see #isNotEmpty(CharSequence)
     */
    public static <T extends CharSequence> T firstNonEmpty(final T... args) {
        return ArrayKit.firstMatch(CharsBacker::isNotEmpty, args);
    }

    /**
     * Returns the first non-blank element from a variadic array of {@link CharSequence}s.
     *
     * @param args Multiple {@link CharSequence} elements.
     * @param <T>  The type of the elements.
     * @return The first non-blank element, or {@code null} if the array is empty or all elements are blank.
     * @see #isNotBlank(CharSequence)
     */
    public static <T extends CharSequence> T firstNonBlank(final T... args) {
        return ArrayKit.firstMatch(CharsBacker::isNotBlank, args);
    }

    /**
     * Converts a string to lowercase using the default locale.
     *
     * @param text The string to convert.
     * @return The converted string in lowercase.
     * @see String#toLowerCase()
     */
    public static String toLowerCase(final CharSequence text) {
        return toLowerCase(text, Locale.getDefault());
    }

    /**
     * Converts a string to lowercase using the specified locale.
     *
     * @param text   The string to convert.
     * @param locale The locale to use for conversion.
     * @return The converted string in lowercase.
     * @see String#toLowerCase()
     */
    public static String toLowerCase(final CharSequence text, final Locale locale) {
        if (null == text) {
            return null;
        }
        if (0 == text.length()) {
            return Normal.EMPTY;
        }
        return text.toString().toLowerCase(locale);
    }

    /**
     * Converts a string to uppercase using the default locale.
     *
     * @param text The string to convert.
     * @return The converted string in uppercase.
     * @see String#toUpperCase()
     */
    public static String toUpperCase(final CharSequence text) {
        return toUpperCase(text, Locale.getDefault());
    }

    /**
     * Converts a string to uppercase using the specified locale.
     *
     * @param text   The string to convert.
     * @param locale The locale to use for conversion.
     * @return The converted string in uppercase.
     * @see String#toUpperCase()
     */
    public static String toUpperCase(final CharSequence text, final Locale locale) {
        if (null == text) {
            return null;
        }
        if (text.isEmpty()) {
            return Normal.EMPTY;
        }
        return text.toString().toUpperCase();
    }

    /**
     * Converts the first letter of a string to uppercase and prepends a specified string. Example:
     * {@code text="name", preString="get"} returns {@code "getName"}.
     *
     * @param text      The string to process.
     * @param preString The string to prepend.
     * @return The processed string.
     */
    public static String upperFirstAndAddPre(final CharSequence text, final String preString) {
        if (text == null || preString == null) {
            return null;
        }
        return preString + upperFirst(text);
    }

    /**
     * Converts the first letter of a string to uppercase. Example: {@code text = "name"} returns {@code "Name"}.
     *
     * @param text The string to process.
     * @return The string with its first letter capitalized.
     */
    public static String upperFirst(final CharSequence text) {
        return upperAt(text, 0);
    }

    /**
     * Converts the character at a specified index in a string to uppercase.
     *
     * <pre>
     * Example: text = "name", index = 1, returns "nAme"
     * </pre>
     *
     * @param text  The string to process.
     * @param index The index of the character to capitalize. Supports negative values, where -1 means the last
     *              character.
     * @return The string with the character at the specified index capitalized.
     */
    public static String upperAt(final CharSequence text, final int index) {
        return replaceAt(text, index, Character::toUpperCase);
    }

    /**
     * Converts the first letter of a string to lowercase. Example: {@code text = "Name"} returns {@code "name"}.
     *
     * @param text The string to process.
     * @return The string with its first letter lowercased.
     */
    public static String lowerFirst(final CharSequence text) {
        return lowerAt(text, 0);
    }

    /**
     * Converts the character at a specified index in a string to lowercase. Example: {@code text = "NAME", index = 1},
     * returns {@code "NaME"}.
     *
     * @param text  The string to process.
     * @param index The index of the character to lowercase. Supports negative values, where -1 means the last
     *              character.
     * @return The string with the character at the specified index lowercased.
     */
    public static String lowerAt(final CharSequence text, final int index) {
        return replaceAt(text, index, Character::toLowerCase);
    }

    /**
     * Filters characters in a string based on a predicate.
     *
     * @param text      The string to filter.
     * @param predicate The filter predicate. Characters for which {@link Predicate#test(Object)} returns {@code true}
     *                  are retained.
     * @return The filtered string.
     */
    public static String filter(final CharSequence text, final Predicate<Character> predicate) {
        if (text == null || predicate == null) {
            return toStringOrNull(text);
        }

        final int len = text.length();
        final StringBuilder sb = new StringBuilder(len);
        char c;
        for (int i = 0; i < len; i++) {
            c = text.charAt(i);
            if (predicate.test(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Checks if all letters in the given string are uppercase. The criteria are as follows:
     * 
     * <pre>
     * 1. Uppercase letters include A-Z.
     * 2. Other non-letter Unicode characters are also considered uppercase.
     * </pre>
     *
     * @param text The string to check.
     * @return {@code true} if all letters are uppercase, {@code false} otherwise.
     */
    public static boolean isUpperCase(final CharSequence text) {
        return NamingCase.isUpperCase(text);
    }

    /**
     * Checks if all letters in the given string are lowercase. The criteria are as follows:
     * 
     * <pre>
     * 1. Lowercase letters include a-z.
     * 2. Other non-letter Unicode characters are also considered lowercase.
     * </pre>
     *
     * @param text The string to check.
     * @return {@code true} if all letters are lowercase, {@code false} otherwise.
     */
    public static boolean isLowerCase(final CharSequence text) {
        return NamingCase.isLowerCase(text);
    }

    /**
     * Swaps the case of letters in the given string. Uppercase becomes lowercase, and lowercase becomes uppercase.
     *
     * <pre>
     * swapCase(null)                 = null
     * swapCase("")                   = ""
     * swapCase("The dog has a BONE") = "tHE DOG HAS A bone"
     * </pre>
     *
     * @param text The string to swap case.
     * @return The string with swapped case.
     */
    public static String swapCase(final String text) {
        return NamingCase.swapCase(text);
    }

    /**
     * Converts a camel case string to an underscore case. If the input camel case string is empty, an empty string is
     * returned. Example:
     *
     * <pre>
     * HelloWorld = hello_world
     * Hello_World = hello_world
     * HelloWorld_test = hello_world_test
     * </pre>
     *
     * @param text The camel case string to convert, can also be in underscore format.
     * @return The converted string in underscore case.
     * @see NamingCase#toUnderlineCase(CharSequence)
     */
    public static String toUnderlineCase(final CharSequence text) {
        return NamingCase.toUnderlineCase(text);
    }

    /**
     * Converts a camel case string to a symbol-separated string. If the input camel case string is empty, an empty
     * string is returned.
     *
     * @param text   The camel case string to convert, can also be in symbol-separated format.
     * @param symbol The separator character.
     * @return The converted string in symbol-separated format.
     * @see NamingCase#toSymbolCase(CharSequence, char)
     */
    public static String toSymbolCase(final CharSequence text, final char symbol) {
        return NamingCase.toSymbolCase(text, symbol);
    }

    /**
     * Converts an underscore-separated string to camel case. If the input underscore-separated string is empty, an
     * empty string is returned. Example: {@code hello_world} returns {@code helloWorld}.
     *
     * @param name The underscore-separated string to convert.
     * @return The converted string in camel case.
     * @see NamingCase#toCamelCase(CharSequence)
     */
    public static String toCamelCase(final CharSequence name) {
        return NamingCase.toCamelCase(name);
    }

    /**
     * Converts a symbol-separated string to camel case. If the input string is empty, an empty string is returned.
     * Example: {@code hello_world} returns {@code helloWorld}; {@code hello-world} returns {@code helloWorld}.
     *
     * @param name   The symbol-separated string to convert.
     * @param symbol The separator character.
     * @return The converted string in camel case.
     * @see NamingCase#toCamelCase(CharSequence, char)
     */
    public static String toCamelCase(final CharSequence name, final char symbol) {
        return NamingCase.toCamelCase(name, symbol);
    }

    /**
     * Creates a {@link StringBuilder} object. If the object itself is already a {@link StringBuilder}, it is returned
     * directly; otherwise, a new {@link StringBuilder} is created.
     *
     * @param text The {@link CharSequence} to use for the {@link StringBuilder}.
     * @return A {@link StringBuilder} object.
     */
    public static StringBuilder builder(final CharSequence text) {
        return text instanceof StringBuilder ? (StringBuilder) text : new StringBuilder(text);
    }

    /**
     * Creates a {@link StringBuilder} object from a list of initial strings.
     *
     * @param args An array of {@link CharSequence}s to append.
     * @return A {@link StringBuilder} object.
     */
    public static StringBuilder builder(final CharSequence... args) {
        final StringBuilder sb = new StringBuilder();
        for (final CharSequence text : args) {
            sb.append(text);
        }
        return sb;
    }

    /**
     * Gets the standard field name corresponding to a "set", "get", or "is" method name. Example: {@code setName}
     * returns {@code name}.
     *
     * <pre>
     * getName = name
     * setName = name
     * isName  = name
     * </pre>
     *
     * @param getOrSetMethodName The "get", "set", or "is" method name.
     * @return The field name if it's a standard getter/setter/isser, otherwise {@code null}.
     */
    public static String getGeneralField(final CharSequence getOrSetMethodName) {
        final String getOrSetMethodNameStr = getOrSetMethodName.toString();
        if (getOrSetMethodNameStr.startsWith("get") || getOrSetMethodNameStr.startsWith("set")) {
            return removePreAndLowerFirst(getOrSetMethodName, 3);
        } else if (getOrSetMethodNameStr.startsWith("is")) {
            return removePreAndLowerFirst(getOrSetMethodName, 2);
        }
        return null;
    }

    /**
     * Generates a setter method name from a field name. Example: {@code name} returns {@code setName}.
     *
     * @param fieldName The field name.
     * @return The setter method name (e.g., setXxx).
     */
    public static String genSetter(final CharSequence fieldName) {
        return upperFirstAndAddPre(fieldName, "set");
    }

    /**
     * Generates a getter method name from a field name.
     *
     * @param fieldName The field name.
     * @return The getter method name (e.g., getXxx).
     */
    public static String genGetter(final CharSequence fieldName) {
        return upperFirstAndAddPre(fieldName, "get");
    }

    /**
     * Concatenates multiple strings into one.
     *
     * @param isNullToEmpty If {@code true}, {@code null} strings are converted to empty strings "".
     * @param args          An array of strings to concatenate.
     * @return The concatenated string.
     */
    public static String concat(final boolean isNullToEmpty, final CharSequence... args) {
        final StringBuilder sb = new StringBuilder();
        for (final CharSequence text : args) {
            sb.append(isNullToEmpty ? toStringOrEmpty(text) : text);
        }
        return sb.toString();
    }

    /**
     * Converts a given string into a "xxx...xxx" format.
     *
     * <ul>
     * <li>abcdefgh 9 - abcdefgh</li>
     * <li>abcdefgh 8 - abcdefgh</li>
     * <li>abcdefgh 7 - ab...gh</li>
     * <li>abcdefgh 6 - ab...h</li>
     * <li>abcdefgh 5 - a...h</li>
     * <li>abcdefgh 4 - a..h</li>
     * <li>abcdefgh 3 - a.h</li>
     * <li>abcdefgh 2 - a.</li>
     * <li>abcdefgh 1 - a</li>
     * <li>abcdefgh 0 - abcdefgh</li>
     * <li>abcdefgh -1 - abcdefgh</li>
     * </ul>
     *
     * @param text      The string.
     * @param maxLength The maximum length of the result.
     * @return The truncated string.
     */
    public static String brief(final CharSequence text, final int maxLength) {
        if (null == text) {
            return null;
        }
        final int strLength = text.length();
        if (maxLength <= 0 || strLength <= maxLength) {
            return text.toString();
        }

        // Special lengths
        switch (maxLength) {
            case 1:
                return String.valueOf(text.charAt(0));

            case 2:
                return text.charAt(0) + ".";

            case 3:
                return text.charAt(0) + "." + text.charAt(strLength - 1);

            case 4:
                return text.charAt(0) + ".." + text.charAt(strLength - 1);
        }

        final int suffixLength = (maxLength - 3) / 2;
        final int preLength = suffixLength + (maxLength - 3) % 2; // suffixLength or suffixLength + 1
        final String text2 = text.toString();
        return format("{}...{}", text2.substring(0, preLength), text2.substring(strLength - suffixLength));
    }

    /**
     * Joins multiple objects into a string using a specified conjunction as a delimiter.
     *
     * @param conjunction The delimiter, e.g., {@link Symbol#COMMA}.
     * @param objs        An array of objects to join.
     * @return The joined string.
     * @see ArrayKit#join(Object, CharSequence)
     */
    public static String join(final CharSequence conjunction, final Object... objs) {
        return ArrayKit.join(objs, conjunction);
    }

    /**
     * Joins elements of an {@link Iterable} into a string using a specified conjunction as a delimiter.
     *
     * @param <T>         The type of the elements.
     * @param conjunction The delimiter, e.g., {@link Symbol#COMMA}.
     * @param iterable    The iterable collection.
     * @return The joined string.
     * @see CollKit#join(Iterable, CharSequence)
     */
    public static <T> String join(final CharSequence conjunction, final Iterable<T> iterable) {
        return CollKit.join(iterable, conjunction);
    }

    /**
     * Joins elements of a {@link Collection} into a string using a specified delimiter, and converts each element to a
     * string using a provided function.
     *
     * @param <T>       The type of elements in the collection.
     * @param delimiter The delimiter to use for joining elements.
     * @param objs      The collection of elements to join.
     * @param function  The function to convert each element to a string.
     * @return The joined string. Returns an empty string if the collection is empty.
     */
    public static <T> String join(String delimiter, Collection<T> objs, Function<T, String> function) {
        if (CollKit.isEmpty(objs)) {
            return Normal.EMPTY;
        } else if (objs.size() == 1) {
            T next = objs.iterator().next();
            return String.valueOf(function.apply(next));
        } else {
            String[] strings = new String[objs.size()];
            int index = 0;
            for (T obj : objs) {
                strings[index++] = function.apply(obj);
            }
            return String.join(delimiter, strings);
        }
    }

    /**
     * Checks if a string consists entirely of digits.
     *
     * @param text The string to check.
     * @return {@code true} if the string contains only digits, {@code false} otherwise.
     */
    public static boolean isNumeric(final CharSequence text) {
        return isAllCharMatch(text, Character::isDigit);
    }

    /**
     * Cyclically shifts a substring within a string by a specified distance. If {@code moveLength} is positive, it
     * shifts to the right; if negative, to the left; if 0, no shift. If {@code moveLength} is greater than the string
     * length, it performs a cyclic shift, meaning it wraps around. For example, a length of 10 and a shift of 13 is
     * equivalent to a shift of 3.
     *
     * @param text         The string to modify.
     * @param startInclude The starting position of the substring (inclusive).
     * @param endExclude   The ending position of the substring (exclusive).
     * @param moveLength   The distance to move. Negative for left shift, positive for right shift.
     * @return The string with the substring shifted.
     */
    public static String move(final CharSequence text, final int startInclude, final int endExclude, int moveLength) {
        if (isEmpty(text)) {
            return toStringOrNull(text);
        }
        final int len = text.length();
        if (Math.abs(moveLength) > len) {
            // Cyclic shift: wrap around if out of bounds.
            moveLength = moveLength % len;
        }
        final StringBuilder strBuilder = new StringBuilder(len);
        if (moveLength > 0) {
            final int endAfterMove = Math.min(endExclude + moveLength, text.length());
            strBuilder.append(text.subSequence(0, startInclude)).append(text.subSequence(endExclude, endAfterMove))
                    .append(text.subSequence(startInclude, endExclude))
                    .append(text.subSequence(endAfterMove, text.length()));
        } else if (moveLength < 0) {
            final int startAfterMove = Math.max(startInclude + moveLength, 0);
            strBuilder.append(text.subSequence(0, startAfterMove)).append(text.subSequence(startInclude, endExclude))
                    .append(text.subSequence(startAfterMove, startInclude))
                    .append(text.subSequence(endExclude, text.length()));
        } else {
            return toStringOrNull(text);
        }
        return strBuilder.toString();
    }

    /**
     * Checks if all characters in the given string are identical.
     *
     * @param text The string to check.
     * @return {@code true} if all characters in the string are the same, {@code false} otherwise.
     * @throws IllegalArgumentException if the text is empty.
     */
    public static boolean isCharEquals(final CharSequence text) {
        Assert.notEmpty(text, "Text to check must be not empty!");
        return count(text, text.charAt(0)) == text.length();
    }

    /**
     * Normalizes a string. For example, "Á" can be represented as "u00C1" or "u0041u0301". This method normalizes to a
     * single representation, typically NFC as recommended by W3C.
     *
     * @param text The string to normalize.
     * @return The normalized string.
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static String normalize(final CharSequence text) {
        return Normalizer.normalize(text, Normalizer.Form.NFC);
    }

    /**
     * Pads the end of a string with a specified character to reach a given length. If the string's length is already
     * greater than or equal to the target length, the original string is returned.
     *
     * @param text      The string to pad.
     * @param fixedChar The character to use for padding.
     * @param length    The target total length.
     * @return The padded string.
     */
    public static String fixLength(final CharSequence text, final char fixedChar, final int length) {
        final int fixedLength = length - text.length();
        if (fixedLength <= 0) {
            return text.toString();
        }
        return text + repeat(fixedChar, fixedLength);
    }

    /**
     * Gets the common prefix of two strings.
     *
     * <pre>{@code
     * commonPrefix("abb", "acc") // "a"
     * }</pre>
     *
     * @param text1 The first string.
     * @param text2 The second string.
     * @return The common prefix of the two strings.
     */
    public static CharSequence commonPrefix(final CharSequence text1, final CharSequence text2) {
        if (isEmpty(text1) || isEmpty(text2)) {
            return Normal.EMPTY;
        }
        final int minLength = Math.min(text1.length(), text2.length());
        int index = 0;
        for (; index < minLength; index++) {
            if (text1.charAt(index) != text2.charAt(index)) {
                break;
            }
        }
        return text1.subSequence(0, index);
    }

    /**
     * Gets the common suffix of two strings.
     *
     * <pre>{@code
     * commonSuffix("aba", "cba") // "ba"
     * }</pre>
     *
     * @param text1 The first string.
     * @param text2 The second string.
     * @return The common suffix of the two strings.
     */
    public static CharSequence commonSuffix(final CharSequence text1, final CharSequence text2) {
        if (isEmpty(text1) || isEmpty(text2)) {
            return Normal.EMPTY;
        }
        int str1Index = text1.length() - 1;
        int str2Index = text2.length() - 1;
        for (; str1Index >= 0 && str2Index >= 0; str1Index--, str2Index--) {

            if (text1.charAt(str1Index) != text2.charAt(str2Index)) {
                break;
            }

        }
        return text1.subSequence(str1Index + 1, text1.length());
    }

    /**
     * Splits a string, trims whitespace from each resulting element, removes empty elements, and converts to a
     * specified result type.
     *
     * @param <T>        The result type.
     * @param text       The string to split.
     * @param separator  The separator string.
     * @param resultType The class of the result type, can be an array or collection.
     * @return The split and converted result.
     */
    public static <T> T splitTo(final CharSequence text, final CharSequence separator, final Class<T> resultType) {
        return Convert.convert(resultType, splitTrim(text, separator));
    }

    /**
     * Splits a string, trims whitespace from each resulting element, and removes empty elements.
     *
     * @param text      The string to split.
     * @param separator The separator string.
     * @return A list of split strings.
     */
    public static List<String> splitTrim(final CharSequence text, final CharSequence separator) {
        return split(text, separator, true, true);
    }

    /**
     * Splits a string into an array of strings. If the separator is not found, the original string is returned as a
     * single-element array. This method does not trim whitespace from elements after splitting and does not ignore
     * empty strings.
     *
     * @param text      The string to split.
     * @param separator The separator string.
     * @return An array of split strings. Returns an empty array if the input text is {@code null}.
     */
    public static String[] splitToArray(final CharSequence text, final CharSequence separator) {
        if (text == null) {
            return new String[] {};
        }
        return split(text, separator).toArray(new String[0]);
    }

    /**
     * Splits a string using {@link Symbol#COMMA} as the separator.
     *
     * @param text The string to split.
     * @return The split string.
     */
    public static String split(String text) {
        return split(text, Symbol.COMMA, Symbol.COMMA);
    }

    /**
     * Splits a string using a specified separator and replaces it with a reserve string.
     *
     * @param text      The string to split.
     * @param separator The separator string.
     * @param reserve   The string to replace the separator with.
     * @return The split and modified string.
     */
    public static String split(String text, String separator, String reserve) {
        StringBuffer sb = new StringBuffer();
        if (isNotEmpty(text)) {
            String[] arr = splitToArray(text, separator);
            for (int i = 0; i < arr.length; i++) {
                if (i == 0) {
                    sb.append(Symbol.SINGLE_QUOTE).append(arr[i]).append(Symbol.SINGLE_QUOTE);
                } else {
                    sb.append(reserve).append(Symbol.SINGLE_QUOTE).append(arr[i]).append(Symbol.SINGLE_QUOTE);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Splits a string. If the separator is not found, the original string is returned as a single-element list. This
     * method does not trim whitespace from elements after splitting and does not ignore empty strings.
     *
     * @param text      The string to split.
     * @param separator The separator string.
     * @return A list of split strings.
     */
    public static List<String> split(final CharSequence text, final CharSequence separator) {
        return split(text, separator, false, false);
    }

    /**
     * Splits a string, with optional trimming of whitespace from elements and optional ignoring of empty strings.
     * Case-sensitive.
     *
     * @param text        The string to split.
     * @param separator   The separator string.
     * @param isTrim      Whether to trim whitespace from each split element.
     * @param ignoreEmpty Whether to ignore empty strings after splitting.
     * @return A list of split strings.
     */
    public static List<String> split(
            final CharSequence text,
            final CharSequence separator,
            final boolean isTrim,
            final boolean ignoreEmpty) {
        return split(text, separator, Normal.__1, isTrim, ignoreEmpty, false);
    }

    /**
     * Splits a string, with a limit on the number of resulting parts, optional trimming of whitespace from elements,
     * and optional ignoring of empty strings. Case-sensitive.
     *
     * @param text        The string to split.
     * @param separator   The separator string.
     * @param limit       The maximum number of parts to return. If less than or equal to 0, no limit.
     * @param isTrim      Whether to trim whitespace from each split element.
     * @param ignoreEmpty Whether to ignore empty strings after splitting.
     * @return A list of split strings.
     */
    public static List<String> split(
            final CharSequence text,
            final CharSequence separator,
            final int limit,
            final boolean isTrim,
            final boolean ignoreEmpty) {
        return split(text, separator, limit, isTrim, ignoreEmpty, false);
    }

    /**
     * Splits a string. If the provided string is {@code null}, returns an empty {@link ArrayList}. If the provided
     * string is "", and {@code ignoreEmpty} is {@code true}, returns an empty {@link ArrayList}; otherwise, returns an
     * {@link ArrayList} containing only "".
     *
     * @param text        The string to split.
     * @param separator   The separator string.
     * @param limit       The maximum number of parts to return. If less than or equal to 0, no limit.
     * @param isTrim      Whether to trim whitespace from each split element.
     * @param ignoreEmpty Whether to ignore empty strings after splitting.
     * @param ignoreCase  Whether to ignore case when searching for the separator.
     * @return A list of split strings.
     */
    public static List<String> split(
            final CharSequence text,
            final CharSequence separator,
            final int limit,
            final boolean isTrim,
            final boolean ignoreEmpty,
            final boolean ignoreCase) {
        return split(text, separator, limit, ignoreEmpty, ignoreCase, trimFunc(isTrim));
    }

    /**
     * Splits a string and maps each resulting element using a provided function. If the provided string is
     * {@code null}, returns an empty {@link ArrayList}. If the provided string is "", and {@code ignoreEmpty} is
     * {@code true}, returns an empty {@link ArrayList}; otherwise, returns an {@link ArrayList} containing only ""
     * (mapped).
     *
     * @param <R>         The type of the mapped elements.
     * @param text        The string to split.
     * @param separator   The separator string.
     * @param limit       The maximum number of parts to return. If less than or equal to 0, no limit.
     * @param ignoreEmpty Whether to ignore empty strings after splitting.
     * @param ignoreCase  Whether to ignore case when searching for the separator.
     * @param mapping     The function to map each split string element.
     * @return A list of mapped objects.
     * @throws IllegalArgumentException if the separator is empty.
     */
    public static <R> List<R> split(
            final CharSequence text,
            final CharSequence separator,
            final int limit,
            final boolean ignoreEmpty,
            final boolean ignoreCase,
            final Function<String, R> mapping) {
        if (null == text) {
            return ListKit.zero();
        } else if (0 == text.length() && ignoreEmpty) {
            return ListKit.zero();
        }
        Assert.notEmpty(separator, "Separator must be not empty!");

        // Method to find separator
        final TextFinder finder = separator.length() == 1 ? new CharFinder(separator.charAt(0), ignoreCase)
                : StringFinder.of(separator, ignoreCase);

        final StringSplitter stringSplitter = new StringSplitter(text, finder, limit, ignoreEmpty);
        return stringSplitter.toList(mapping);
    }

    /**
     * Splits a path string. If the string is empty or {@code null}, returns an empty collection. Empty path segments
     * are ignored.
     *
     * @param text The path string to split.
     * @return A list of path segments.
     */
    public static List<String> splitPath(final CharSequence text) {
        return splitPath(text, Normal.__1);
    }

    /**
     * Splits a path string with a limit on the number of resulting parts. If the string is empty or {@code null},
     * returns an empty collection. Empty path segments are ignored.
     *
     * @param text  The path string to split.
     * @param limit The maximum number of parts to return. If less than or equal to 0, no limit.
     * @return A list of path segments.
     */
    public static List<String> splitPath(final CharSequence text, final int limit) {
        if (isBlank(text)) {
            return ListKit.zero();
        }

        final StringSplitter stringSplitter = new StringSplitter(text,
                new MatcherFinder((c) -> c == Symbol.C_SLASH || c == Symbol.C_BACKSLASH),
                // Spaces are allowed in paths
                limit, true);
        return stringSplitter.toList(false);
    }

    /**
     * Splits a string by whitespace characters. Whitespace is trimmed from each resulting element, and empty or blank
     * elements are not included. If the string is empty or {@code null}, returns an empty collection.
     *
     * @param text The string to split.
     * @return A list of split strings.
     */
    public static List<String> splitByBlank(final CharSequence text) {
        return splitByBlank(text, Normal.__1);
    }

    /**
     * Splits a string by whitespace characters, with a limit on the number of resulting parts. Whitespace is trimmed
     * from each resulting element, and empty or blank elements are not included. If the string is empty or
     * {@code null}, returns an empty collection.
     *
     * @param text  The string to split.
     * @param limit The maximum number of parts to return. If less than or equal to 0, no limit.
     * @return A list of split strings.
     */
    public static List<String> splitByBlank(final CharSequence text, final int limit) {
        if (isBlank(text)) {
            return ListKit.zero();
        }
        final StringSplitter stringSplitter = new StringSplitter(text, new MatcherFinder(CharKit::isBlankChar), limit,
                true);
        return stringSplitter.toList(false);
    }

    /**
     * Splits a string by whitespace characters into an array of strings.
     *
     * @param text  The string to split.
     * @param limit The maximum number of parts to return. If less than or equal to 0, no limit.
     * @return An array of split strings.
     */
    public static String[] splitByBlankToArray(final CharSequence text, final int limit) {
        return splitByBlank(text, limit).toArray(new String[0]);
    }

    /**
     * Splits a string by a regular expression. Rules:
     * <ul>
     * <li>If {@code str} is {@code null}, returns {@code new ArrayList(0)}.</li>
     * <li>If {@code str} is "", returns {@code [""]}.</li>
     * <li>If {@code separatorRegex} is empty ({@code null} or ""), returns {@code [text]} (a single-element array
     * containing the original string).</li>
     * </ul>
     *
     * @param text           The string to split.
     * @param separatorRegex The regular expression for the separator.
     * @param limit          The maximum number of parts to return. If less than or equal to 0, no limit.
     * @param isTrim         Whether to trim whitespace from each split element.
     * @param ignoreEmpty    Whether to ignore empty strings after splitting.
     * @return A list of split strings.
     */
    public static List<String> splitByRegex(
            final CharSequence text,
            final String separatorRegex,
            final int limit,
            final boolean isTrim,
            final boolean ignoreEmpty) {
        return splitByRegex(
                text,
                // If the given string or regex is empty, no need to parse the pattern.
                (isEmpty(text) || isEmpty(separatorRegex)) ? null : Pattern.get(separatorRegex),
                limit,
                isTrim,
                ignoreEmpty);
    }

    /**
     * Splits a string by a regular expression pattern. Rules:
     * <ul>
     * <li>If {@code str} is {@code null}, returns {@code new ArrayList(0)}.</li>
     * <li>If {@code str} is "", returns {@code [""]}.</li>
     * <li>If {@code separatorPattern} is {@code null}, returns {@code [text]} (a single-element array containing the
     * original string).</li>
     * </ul>
     *
     * @param text             The string to split.
     * @param separatorPattern The regular expression pattern for the separator.
     * @param limit            The maximum number of parts to return. If less than or equal to 0, no limit.
     * @param isTrim           Whether to trim whitespace from each split element.
     * @param ignoreEmpty      Whether to ignore empty strings after splitting.
     * @return A list of split strings.
     */
    public static List<String> splitByRegex(
            final CharSequence text,
            final java.util.regex.Pattern separatorPattern,
            final int limit,
            final boolean isTrim,
            final boolean ignoreEmpty) {
        if (null == text) {
            return ListKit.zero();
        }
        if (0 == text.length()) {
            return ignoreEmpty ? ListKit.zero() : ListKit.of(Normal.EMPTY);
        }
        if (null == separatorPattern) {
            final String result = text.toString();
            if (isEmpty(result)) {
                return ignoreEmpty ? ListKit.zero() : ListKit.of(Normal.EMPTY);
            }
            return ListKit.of(result);
        }
        final StringSplitter stringSplitter = new StringSplitter(text, new PatternFinder(separatorPattern), limit,
                ignoreEmpty);
        return stringSplitter.toList(isTrim);
    }

    /**
     * Splits a string by a regular expression pattern into an array of strings.
     *
     * @param text             The string to split.
     * @param separatorPattern The regular expression pattern for the separator.
     * @param limit            The maximum number of parts to return. If less than or equal to 0, no limit.
     * @param isTrim           Whether to trim whitespace from each split element.
     * @param ignoreEmpty      Whether to ignore empty strings after splitting.
     * @return An array of split strings.
     */
    public static String[] splitByRegexToArray(
            final CharSequence text,
            final java.util.regex.Pattern separatorPattern,
            final int limit,
            final boolean isTrim,
            final boolean ignoreEmpty) {
        return splitByRegex(text, separatorPattern, limit, isTrim, ignoreEmpty).toArray(new String[0]);
    }

    /**
     * Splits a string into multiple parts based on a given length.
     *
     * @param text The string to split.
     * @param len  The length of each segment. Must be greater than 0.
     * @return An array of split strings.
     */
    public static String[] splitByLength(final CharSequence text, final int len) {
        if (isEmpty(text)) {
            return new String[0];
        }
        final StringSplitter stringSplitter = new StringSplitter(text, new LengthFinder(len), -1, false);
        return stringSplitter.toArray(false);
    }

    /**
     * Returns a function that either trims a string or returns it as is, based on the {@code isTrim} flag.
     *
     * @param isTrim Whether to trim the string.
     * @return A {@link Function} that takes a string and returns a string.
     */
    public static Function<String, String> trimFunc(final boolean isTrim) {
        return isTrim ? CharsBacker::trim : Function.identity();
    }

    /**
     * Converts a string to an array of characters (integers representing Unicode code points).
     *
     * @param text        The string to convert.
     * @param isCodePoint If {@code true}, converts to Unicode code points (supports multi-char characters like emoji).
     * @return An array of integers representing the characters.
     */
    public static int[] toChars(final CharSequence text, final boolean isCodePoint) {
        if (null == text) {
            return null;
        }
        return (isCodePoint ? text.codePoints() : text.chars()).toArray();
    }

    /**
     * Iterates over each character of a string and applies a consumer.
     *
     * @param str      The string to iterate.
     * @param consumer The consumer to apply to each character.
     */
    public static void forEach(final CharSequence str, final Consumer<Character> consumer) {
        forEach(str, false, (cInt) -> consumer.accept((char) cInt));
    }

    /**
     * Iterates over each character (or Unicode code point) of a string and applies an {@link IntConsumer}.
     *
     * @param str         The string to iterate.
     * @param isCodePoint If {@code true}, iterates over Unicode code points (supports multi-char characters like
     *                    emoji).
     * @param consumer    The {@link IntConsumer} to apply to each character/code point.
     */
    public static void forEach(final CharSequence str, final boolean isCodePoint, final IntConsumer consumer) {
        if (null == str) {
            return;
        }
        (isCodePoint ? str.codePoints() : str.chars()).forEach(consumer);
    }

    /**
     * Formats text using "{varName}" placeholders, where values are retrieved from a map. Example: {@code map = {a:
     * "aValue", b: "bValue"}} {@code format("{a} and {b}", map)} returns {@code "aValue and bValue"}.
     *
     * @param template The text template, with "{key}" representing placeholders.
     * @param map      The map containing parameter key-value pairs.
     * @return The formatted text.
     */
    public static String formatByMap(final CharSequence template, final Map<?, ?> map) {
        return formatByMap(template, map, true);
    }

    /**
     * Formats text using "{varName}" placeholders, where values are retrieved from a map. Example: {@code map = {a:
     * "aValue", b: "bValue"}} {@code format("{a} and {b}", map)} returns {@code "aValue and bValue"}.
     *
     * @param template   The text template, with "{key}" representing placeholders.
     * @param map        The map containing parameter key-value pairs.
     * @param ignoreNull Whether to ignore {@code null} values. If {@code true}, variables corresponding to {@code null}
     *                   values are not replaced; otherwise, they are replaced with "".
     * @return The formatted text.
     */
    public static String formatByMap(final CharSequence template, final Map<?, ?> map, final boolean ignoreNull) {
        return StringFormatter.formatByBean(template, map, ignoreNull);
    }

    /**
     * Formats text using "{varName}" placeholders, where values are retrieved from a bean's properties. Example:
     * {@code bean = User:{a: "aValue", b: "bValue"}} {@code format("{a} and {b}", bean)} returns
     * {@code "aValue and bValue"}.
     *
     * @param template   The text template, with "{key}" representing placeholders.
     * @param bean       The bean object containing parameter properties.
     * @param ignoreNull Whether to ignore {@code null} values. If {@code true}, variables corresponding to {@code null}
     *                   values are not replaced; otherwise, they are replaced with "".
     * @return The formatted text.
     */
    public static String formatByBean(final CharSequence template, final Object bean, final boolean ignoreNull) {
        return StringFormatter.formatByBean(template, bean, ignoreNull);
    }

}
