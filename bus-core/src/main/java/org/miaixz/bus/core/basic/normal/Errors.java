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

import org.miaixz.bus.core.lang.exception.AlreadyExistsException;

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
     * An inner class representing an error code entry, which stores the error code and message. This class implements
     * the {@link Errors} interface.
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
