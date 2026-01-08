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
 * Utility class for formatting strings with placeholders. This class provides methods to replace placeholders in a
 * string template with provided arguments, supporting both indexed and named placeholders.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringFormatter {

    /**
     * Cache for compiled {@link StringTemplate} instances. The cache uses a weak-concurrent map to store templates,
     * preventing memory leaks when templates are no longer referenced. The key is a combination of the template pattern
     * and the placeholder (for single placeholder templates) or a flag for named placeholders.
     */
    private static final WeakConcurrentMap<Map.Entry<CharSequence, Object>, StringTemplate> CACHE = new WeakConcurrentMap<>();

    /**
     * Formats a string by replacing placeholders `{}` with provided arguments in order. To output `{}` literally,
     * escape it with `\\{}`. To output a literal `\` before `{}`, use `\\\\{}`.
     * <p>
     * Example:
     * <ul>
     * <li>Normal usage: `format("this is {} for {}", "a", "b")` returns `"this is a for b"`</li>
     * <li>Escaping `{}`: `format("this is \\{} for {}", "a", "b")` returns `"this is {} for a"`</li>
     * <li>Escaping `\`: `format("this is \\\\{} for {}", "a", "b")` returns `"this is \a for b"`</li>
     * </ul>
     *
     * @param strPattern The string template with `{}` placeholders.
     * @param argArray   The array of arguments to replace the placeholders.
     * @return The formatted string.
     */
    public static String format(final String strPattern, final Object... argArray) {
        return formatWith(strPattern, Symbol.DELIM, argArray);
    }

    /**
     * Formats a string by replacing specified placeholders with provided arguments in order. To output the placeholder
     * literally, escape it with `\\` (e.g., `\\{}`). To output a literal `\` before the placeholder, use `\\\\` (e.g.,
     * `\\\\{}`).
     * <p>
     * Example:
     * <ul>
     * <li>Normal usage: `formatWith("this is {} for {}", "{}", "a", "b")` returns `"this is a for b"`</li>
     * <li>Escaping placeholder: `formatWith("this is \\{} for {}", "{}", "a", "b")` returns `"this is {} for a"`</li>
     * <li>Escaping `\`: `formatWith("this is \\\\{} for {}", "{}", "a", "b")` returns `"this is \a for b"`</li>
     * </ul>
     *
     * @param strPattern  The string template.
     * @param placeHolder The placeholder string, e.g., `{}`.
     * @param argArray    The array of arguments to replace the placeholders.
     * @return The formatted string.
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
     * Formats text using named placeholders like `{varName}` and a JavaBean or Map as arguments.
     * <p>
     * Example: `bean = User:{a: "aValue", b: "bValue"}` `formatByBean("{a} and {b}", bean, false)` returns `"aValue and
     * bValue"`
     *
     * @param template   The text template, where parts to be replaced are denoted by `{key}`.
     * @param bean       The JavaBean or Map containing the values for replacement.
     * @param ignoreNull If {@code true}, {@code null} values in the bean will not replace their corresponding
     *                   placeholders. If {@code false}, {@code null} values will replace placeholders with an empty
     *                   string.
     * @return The formatted text.
     */
    public static String formatByBean(final CharSequence template, final Object bean, final boolean ignoreNull) {
        if (null == template) {
            return null;
        }

        if (bean instanceof Map) {
            if (MapKit.isEmpty((Map<?, ?>) bean)) {
                return template.toString();
            }
        }
        // Bean's null check requires reflection, which is very slow, so it is not checked here.
        return ((NamedPlaceholderString) CACHE.computeIfAbsent(MutableEntry.of(template, ignoreNull), k -> {
            final NamedPlaceholderString.Builder builder = StringTemplate.ofNamed(template.toString());
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
     * many different templates have been used.
     */
    public static void clear() {
        CACHE.clear();
    }

}
