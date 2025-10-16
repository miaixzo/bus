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
package org.miaixz.bus.core.basic.normal;

import java.util.Locale;
import java.util.ResourceBundle;

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
 * @since Java 17+
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
            // Determine the locale: use default if auto-detect, otherwise create a new locale.
            Locale locale = i18n == I18n.AUTO_DETECT ? Locale.getDefault() : new Locale(i18n.lang());
            // Get the resource bundle for the specified locale.
            ResourceBundle bundle = ResourceBundle.getBundle(Keys.BUNDLE_NAME, locale);
            // Return the localized message from the bundle.
            return bundle.getString(this.key);
        } catch (Exception e) {
            // Fallback to the error message registered in the ERRORS_CACHE.
            Entry entry = Errors.require(this.key);
            return entry != null ? entry.getValue() : this.value;
        }
    }

    /**
     * A builder for {@link ErrorRegistry}, supporting a fluent, chainable interface.
     */
    public static class Builder {

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
