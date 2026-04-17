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

import org.miaixz.bus.core.lang.I18n;
import org.miaixz.bus.core.lang.Keys;

/**
 * A base class for registering error codes, implementing the {@link Errors} interface.
 * <p>
 * This class provides functionality for registering, building, and internationalizing error codes. Each error code
 * consists of a unique key and a corresponding error message (value). It supports retrieving localized error messages
 * based on different locales.
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * 
 * Errors error = ErrorRegistry.builder().key("AUTH_001").value("Authentication failed").build();
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ErrorRegistry implements Errors {

    /**
     * The unique key for the error code.
     */
    private final String key;

    /**
     * The default error message, used when a localized message is not found.
     */
    private final String value;

    /**
     * Private constructor to be used by the {@link Builder}.
     *
     * @param builder The builder object containing the construction parameters.
     * @throws IllegalArgumentException if the key or value is null.
     */
    public ErrorRegistry(Builder builder) {
        if (builder.key == null || builder.value == null) {
            throw new IllegalArgumentException("Key and value cannot be null");
        }
        this.key = builder.key;
        this.value = builder.value;
        // Register the error code upon construction.
        this.register();
    }

    /**
     * Gets a new {@link Builder} instance for constructing an {@code ErrorRegistry} object.
     *
     * @return A new {@link Builder} instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the unique error code key.
     *
     * @return The error code key.
     */
    @Override
    public String getKey() {
        return this.key;
    }

    /**
     * Gets the error message using the auto-detected locale.
     *
     * @return The error message string.
     */
    @Override
    public String getValue() {
        return getValue(I18n.AUTO_DETECT);
    }

    /**
     * Gets the localized error message for the specified locale.
     *
     * @param i18n The locale enumeration; use {@link I18n#AUTO_DETECT} for auto-detection.
     * @return The localized error message.
     */
    public String getValue(I18n i18n) {
        try {
            String message = I18n.message(i18n, Keys.BUNDLE_NAME, this.key);
            if (!this.key.equals(message)) {
                return message;
            }
        } catch (Exception e) {
        }
        // Fallback to the error message registered in the ERRORS_CACHE.
        Entry entry = Errors.require(this.key);
        return entry != null ? entry.getValue() : this.value;
    }

    /**
     * A builder for {@link ErrorRegistry}, supporting a fluent, chainable interface.
     */
    public static class Builder {

        /**
         * Constructs a new Builder.
         */
        public Builder() {
        }

        /**
         * The unique key for the error code.
         */
        private String key;

        /**
         * The default error message, used when a localized message is not found.
         */
        private String value;

        /**
         * Sets the error code key.
         *
         * @param key The error code key.
         * @return The current {@link Builder} instance.
         */
        public Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * Sets the default error message.
         *
         * @param value The error message.
         * @return The current {@link Builder} instance.
         */
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        /**
         * Builds a new {@link ErrorRegistry} instance.
         *
         * @return A new {@link ErrorRegistry} instance.
         */
        public ErrorRegistry build() {
            return new ErrorRegistry(this);
        }
    }

}
