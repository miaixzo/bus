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
package org.miaixz.bus.core.basic.normal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.exception.AlreadyExistsException;

/**
 * An interface for defining error codes and their corresponding messages. It provides methods for retrieving,
 * registering, and managing error codes in a unified manner.
 *
 * @author Kimi Liu
 * @since Java 17+
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
