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
package org.miaixz.bus.mapper;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.miaixz.bus.logger.Logger;

/**
 * A utility class for multi-language support (i18n). The locale can be set via JVM arguments, for example:
 * {@code -Duser.country=US -Duser.language=en}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Locale {

    /**
     * Gets a formatted string for a specific locale from a resource bundle. If the resource bundle or key is not found,
     * it returns the result of {@code MessageFormat.format(key, args)}.
     *
     * @param locale     The locale for which to get the message.
     * @param bundleName The name of the resource bundle.
     * @param key        The key for the desired string.
     * @param args       An array of objects to be formatted and substituted in the message.
     * @return The formatted string.
     */
    public static String message(java.util.Locale locale, String bundleName, String key, Object... args) {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(bundleName, locale);
        } catch (Exception e) {
            bundle = null;
            Logger.warn("Failed to load resource bundle: " + bundleName + " for locale: " + locale);
        }
        try {
            return MessageFormat.format(bundle.getString(key), args);
        } catch (MissingResourceException e) {
            Logger.warn("Resource key not found: " + key + " in bundle: " + bundleName);
            return MessageFormat.format(key, args);
        }
    }

    /**
     * Gets a formatted string for the default locale from a resource bundle. If the resource bundle or key is not
     * found, it returns the result of {@code MessageFormat.format(key, args)}.
     *
     * @param bundleName The name of the resource bundle.
     * @param key        The key for the desired string.
     * @param args       An array of objects to be formatted and substituted in the message.
     * @return The formatted string.
     */
    public static String message(String bundleName, String key, Object... args) {
        return message(java.util.Locale.getDefault(), bundleName, key, args);
    }

    /**
     * Returns a {@link Language} instance for the specified locale and bundle name.
     *
     * @param locale     The locale for the language pack.
     * @param bundleName The name of the resource bundle.
     * @return A {@link Language} instance.
     */
    public static Language language(java.util.Locale locale, String bundleName) {
        return (key, args) -> message(locale, bundleName, key, args);
    }

    /**
     * Returns a {@link Language} instance for the default locale and specified bundle name.
     *
     * @param bundleName The name of the resource bundle.
     * @return A {@link Language} instance.
     */
    public static Language language(String bundleName) {
        return language(java.util.Locale.getDefault(), bundleName);
    }

    /**
     * A functional interface for retrieving formatted text from a language pack.
     */
    @FunctionalInterface
    public interface Language {

        /**
         * Gets the formatted text for the corresponding language.
         *
         * @param key  The key for the desired string.
         * @param args An array of objects to be formatted and substituted in the message.
         * @return The formatted string.
         */
        String message(String key, Object... args);
    }

}
