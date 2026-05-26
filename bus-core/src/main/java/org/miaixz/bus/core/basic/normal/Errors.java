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
package org.miaixz.bus.core.basic.normal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.I18n;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.exception.AlreadyExistsException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * An interface for defining error codes and their corresponding messages. It provides methods for retrieving,
 * registering, and managing error codes in a unified manner.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Errors {

    /**
     * A global cache for storing all registered error code entries. It uses a thread-safe {@link ConcurrentHashMap}
     * where the key is the error code (String) and the value is the {@link Entry} object.
     */
    Map<String, Entry> ERRORS_CACHE = new ConcurrentHashMap<>();

    /**
     * Gets the unique error code.
     *
     * @return The error code string, used to uniquely identify an error.
     */
    String getKey();

    /**
     * Gets the detailed error message.
     *
     * @return The error message string, describing the error in detail.
     */
    String getValue();

    /**
     * Registers the error code into the global cache. If the error code already exists in the cache, this method will
     * throw an exception to prevent duplicates.
     *
     * @throws AlreadyExistsException if attempting to register a duplicate error code.
     */
    default void register() {
        if (ERRORS_CACHE.containsKey(getKey())) {
            throw new AlreadyExistsException("Key already exists for : " + getKey());
        }
        ERRORS_CACHE.putIfAbsent(getKey(), new Entry(getKey(), getValue()));
    }

    /**
     * Checks if the global cache contains the specified error code.
     *
     * @param code The error code to check.
     * @return {@code true} if the cache contains the error code, {@code false} otherwise.
     */
    static boolean contains(String code) {
        return ERRORS_CACHE.containsKey(code);
    }

    /**
     * Retrieves the error code entry for the specified key from the global cache.
     *
     * @param key The error code.
     * @return The corresponding {@link Entry} object, or {@code null} if it does not exist.
     */
    static Entry require(String key) {
        return ERRORS_CACHE.get(key);
    }

    /**
     * Resolves an exception message from an error code, explicit message, and fallback message.
     *
     * @param errcode  The error code.
     * @param errmsg   The explicit error message.
     * @param fallback The fallback message.
     * @return The resolved message.
     */
    static String message(final String errcode, final String errmsg, final String fallback) {
        return null != errcode && null != errmsg ? errmsg : fallback;
    }

    /**
     * Resolves a localized exception message using auto-detected locale.
     *
     * @param errcode  The error code.
     * @param errmsg   The explicit error message.
     * @param fallback The fallback message.
     * @return The resolved localized message.
     */
    static String localizedMessage(final String errcode, final String errmsg, final String fallback) {
        return localizedMessage(I18n.AUTO_DETECT, errcode, errmsg, fallback);
    }

    /**
     * Resolves a localized exception message from a resource bundle, explicit message, registered error entry, or
     * fallback message.
     *
     * @param i18n     The locale enumeration.
     * @param errcode  The error code.
     * @param errmsg   The explicit error message.
     * @param fallback The fallback message.
     * @return The resolved localized message.
     */
    static String localizedMessage(final I18n i18n, final String errcode, final String errmsg, final String fallback) {
        if (null == errcode) {
            return fallback;
        }
        try {
            final String message = I18n.message(i18n, Keys.BUNDLE_NAME, errcode);
            if (!errcode.equals(message)) {
                return message;
            }
        } catch (final Exception ignored) {
            // Fall back to registered errors or the supplied exception message.
        }
        if (StringKit.isNotBlank(errmsg)) {
            return errmsg;
        }
        final Entry entry = require(errcode);
        return null != entry ? entry.getValue() : fallback;
    }

    /**
     * An inner class representing an error code entry, which stores the error code and message. This class implements
     * the {@link Errors} interface.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class Entry implements Errors {

        /**
         * The unique error code.
         */
        private final String key;

        /**
         * The detailed error message.
         */
        private final String value;

        /**
         * Constructs a new error code entry.
         *
         * @param key   The error code.
         * @param value The error message.
         */
        public Entry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Gets the unique error code.
         *
         * @return The error code string.
         */
        @Override
        public String getKey() {
            return key;
        }

        /**
         * Gets the detailed error message.
         *
         * @return The error message string.
         */
        @Override
        public String getValue() {
            return value;
        }

    }

}
