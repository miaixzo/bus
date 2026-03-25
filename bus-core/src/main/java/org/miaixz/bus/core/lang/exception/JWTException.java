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
package org.miaixz.bus.core.lang.exception;

import java.io.Serial;

import org.miaixz.bus.core.basic.normal.Errors;

/**
 * Represents an exception that occurs during JSON Web Token (JWT) processing.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JWTException extends UncheckedException {

    /**
     * The serialization version identifier.
     */
    @Serial
    private static final long serialVersionUID = 2852257851297L;

    /**
     * Constructs a new JWTException with no detail message.
     */
    public JWTException() {
        super();
    }

    /**
     * Constructs a new JWTException with the specified cause.
     *
     * @param e The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public JWTException(final Throwable e) {
        super(e);
    }

    /**
     * Constructs a new JWTException with the specified detail message.
     *
     * @param errmsg The detail message.
     */
    public JWTException(final String errmsg) {
        super(errmsg);
    }

    /**
     * Constructs a new JWTException with the specified error object.
     *
     * @param errors The error object containing error code and message.
     */
    public JWTException(final Errors errors) {
        super(errors);
    }

    /**
     * Constructs a new JWTException with the specified error object and detail message.
     *
     * @param errors The error object containing error code.
     * @param errmsg The detail message.
     */
    public JWTException(final Errors errors, String errmsg) {
        super(errors.getKey(), errmsg);
    }

    /**
     * Constructs a new JWTException with the specified detail message and arguments for formatting.
     *
     * @param format The format string for the detail message.
     * @param args   The arguments referenced by the format specifiers in the format string.
     */
    public JWTException(final String format, final Object... args) {
        super(format, args);
    }

    /**
     * Constructs a new JWTException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param cause   The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public JWTException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new JWTException with the specified detail message, cause, suppression enabled or disabled, and
     * writable stack trace enabled or disabled.
     *
     * @param message            The detail message.
     * @param cause              The cause (which is saved for later retrieval by the {@link Throwable#getCause()}
     *                           method).
     * @param enableSuppression  Whether or not suppression is enabled or disabled.
     * @param writableStackTrace Whether or not the stack trace should be writable.
     */
    public JWTException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Constructs a new JWTException with the specified cause, detail message, and arguments for formatting.
     *
     * @param cause  The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     * @param format The format string for the detail message.
     * @param args   The arguments referenced by the format specifiers in the format string.
     */
    public JWTException(final Throwable cause, final String format, final Object... args) {
        super(cause, format, args);
    }

}
