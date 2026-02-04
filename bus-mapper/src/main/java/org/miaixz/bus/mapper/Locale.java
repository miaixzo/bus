/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
            Logger.warn(false, "Locale", "Failed to load resource bundle: {} for locale: {}", bundleName, locale);
        }
        try {
            return MessageFormat.format(bundle.getString(key), args);
        } catch (MissingResourceException e) {
            Logger.warn(false, "Locale", "Resource key not found: {} in bundle: {}", key, bundleName);
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
