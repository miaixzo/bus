/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.text.placeholder;

import java.util.Map;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.mutable.MutableEntry;
import org.miaixz.bus.core.text.placeholder.template.NamedPlaceholderString;
import org.miaixz.bus.core.text.placeholder.template.SinglePlaceholderString;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Formats strings with placeholders. This class provides methods to replace placeholders in a string template with
 * provided arguments with indexed or named placeholders.
 *
 * @author Kimi Liu
 */
public class StringFormatter {

    /**
     * Best-effort cache for compiled {@link StringTemplate} instances. Both composite keys and compiled templates are
     * weakly held, so entries may be reclaimed even while the original pattern remains in use. Keys combine immutable
     * pattern text with either the single placeholder text or the named-placeholder null-handling flag.
     */
    private static final WeakConcurrentMap<Map.Entry<CharSequence, Object>, StringTemplate> CACHE = new WeakConcurrentMap<>();

    /**
     * Constructs a new string formatter.
     */
    public StringFormatter() {
        // No initialization required.
    }

    /**
     * Formats a string by replacing {@code {}} placeholders with provided arguments in order. A backslash escapes a
     * placeholder, and a doubled backslash preserves a backslash while allowing placeholder substitution.
     * <p>
     * Example:
     * <ul>
     * <li>{@code format("this is {} for {}", "a", "b")} returns {@code "this is a for b"}.</li>
     * <li>{@code format("this is \\{} for {}", "a", "b")} returns {@code "this is {} for a"}.</li>
     * <li>{@code format("this is \\\\{} for {}", "a", "b")} returns text with a literal backslash before the
     * substituted {@code a}.</li>
     * </ul>
     *
     * @param strPattern The string template with {@code {}} placeholders.
     * @param argArray   The array of arguments to replace the placeholders.
     * @return The formatted string, or the original pattern if it is blank or no arguments exist.
     */
    public static String format(final String strPattern, final Object... argArray) {
        return formatWith(strPattern, Symbol.DELIM, argArray);
    }

    /**
     * Formats a string by replacing the specified placeholder with provided arguments in order. A backslash escapes a
     * placeholder, and a doubled backslash preserves a backslash while allowing placeholder substitution.
     * <p>
     * Example:
     * <ul>
     * <li>{@code formatWith("this is {} for {}", "{}", "a", "b")} returns {@code "this is a for b"}.</li>
     * <li>{@code formatWith("this is \\{} for {}", "{}", "a", "b")} returns {@code "this is {} for a"}.</li>
     * <li>{@code formatWith("this is \\\\{} for {}", "{}", "a", "b")} preserves a literal backslash before the
     * substituted {@code a}.</li>
     * </ul>
     *
     * @param strPattern  The string template.
     * @param placeHolder The placeholder string, e.g., {@code {}}.
     * @param argArray    The array of arguments to replace the placeholders.
     * @return The formatted string, or the original pattern if it or the placeholder is blank, or no arguments exist.
     */
    public static String formatWith(final String strPattern, final String placeHolder, final Object... argArray) {
        if (StringKit.isBlank(strPattern) || StringKit.isBlank(placeHolder) || ArrayKit.isEmpty(argArray)) {
            return strPattern;
        }
        return ((SinglePlaceholderString) CACHE.computeIfAbsent(
                MutableEntry.of(strPattern, placeHolder),
                k -> StringTemplate.of(strPattern).placeholder(placeHolder).build())).format(argArray);
    }

    /**
     * Formats text using named placeholders like {@code {varName}} and a JavaBean or Map as arguments. The supplied
     * template is converted to an immutable string exactly once per non-null call; the same snapshot is used for cache
     * lookup and compilation, so a mutable CharSequence cannot provide different patterns for those operations.
     * <p>
     * For a bean or map with properties {@code a = "aValue"} and {@code b = "bValue"},
     * {@code formatByBean("{a} and {b}", bean, false)} returns {@code "aValue and bValue"}.
     *
     * @param template   The text template, where parts to be replaced are denoted by {@code {key}}.
     * @param bean       The JavaBean or Map containing the values for replacement.
     * @param ignoreNull If {@code true}, {@code null} values in the bean will not replace their corresponding
     *                   placeholders. If {@code false}, {@code null} values will replace placeholders with an empty
     *                   string.
     * @return The formatted text, null for a null template, or the unchanged snapshot when the supplied map is empty.
     */
    public static String formatByBean(final CharSequence template, final Object bean, final boolean ignoreNull) {
        if (null == template) {
            return null;
        }

        final String pattern = template.toString();
        if (bean instanceof Map) {
            if (MapKit.isEmpty((Map<?, ?>) bean)) {
                return pattern;
            }
        }
        // Leave bean-property and null-value handling to the compiled named template.
        return ((NamedPlaceholderString) CACHE.computeIfAbsent(MutableEntry.of(pattern, ignoreNull), k -> {
            final NamedPlaceholderString.Builder builder = StringTemplate.ofNamed(pattern);
            if (ignoreNull) {
                builder.addFeatures(StringTemplate.Feature.FORMAT_NULL_VALUE_TO_WHOLE_PLACEHOLDER);
            } else {
                builder.addFeatures(StringTemplate.Feature.FORMAT_NULL_VALUE_TO_EMPTY);
            }
            return builder.build();
        })).format(bean);
    }

    /**
     * Clears the internal cache of compiled {@link StringTemplate} instances. This can be used to free up memory if
     * many different templates have been used. Concurrent formatting calls may populate the cache again.
     */
    public static void clear() {
        CACHE.clear();
    }

}
