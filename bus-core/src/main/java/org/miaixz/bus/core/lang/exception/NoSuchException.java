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
package org.miaixz.bus.core.lang.exception;

import java.io.Serial;

import org.miaixz.bus.core.basic.normal.Errors;

/**
 * Represents an exception indicating that a requested element or method does not exist.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NoSuchException extends UncheckedException {

    /**
     * The serialization version identifier.
     */
    @Serial
    private static final long serialVersionUID = 2852261630680L;

    /**
     * Constructs a new NoSuchException with no detail message.
     */
    public NoSuchException() {
        super();
    }

    /**
     * Constructs a new NoSuchException with the specified cause.
     *
     * @param e The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public NoSuchException(final Throwable e) {
        super(e);
    }

    /**
     * Constructs a new NoSuchException with the specified detail message.
     *
     * @param errmsg The detail message.
     */
    public NoSuchException(final String errmsg) {
        super(errmsg);
    }

    /**
     * Constructs a new NoSuchException with the specified error object.
     *
     * @param errors The error object containing error code and message.
     */
    public NoSuchException(final Errors errors) {
        super(errors);
    }

    /**
     * Constructs a new NoSuchException with the specified error object and detail message.
     *
     * @param errors The error object containing error code.
     * @param errmsg The detail message.
     */
    public NoSuchException(final Errors errors, String errmsg) {
        super(errors.getKey(), errmsg);
    }

    /**
     * Constructs a new NoSuchException with the specified detail message and arguments for formatting.
     *
     * @param format The format string for the detail message.
     * @param args   The arguments referenced by the format specifiers in the format string.
     */
    public NoSuchException(final String format, final Object... args) {
        super(format, args);
    }

    /**
     * Constructs a new NoSuchException with the specified error code and detail message.
     *
     * @param errcode The error code.
     * @param errmsg  The detail message.
     */
    public NoSuchException(final String errcode, final String errmsg) {
        super(errcode, errmsg);
    }

    /**
     * Constructs a new NoSuchException with the specified error code and cause.
     *
     * @param errcode   The error code.
     * @param throwable The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public NoSuchException(final String errcode, final Throwable throwable) {
        super(errcode, throwable);
    }

    /**
     * Constructs a new NoSuchException with the specified cause, detail message, and arguments for formatting.
     *
     * @param cause  The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     * @param format The format string for the detail message.
     * @param args   The arguments referenced by the format specifiers in the format string.
     */
    public NoSuchException(final Throwable cause, final String format, final Object... args) {
        super(cause, format, args);
    }

    /**
     * Constructs a new NoSuchException with the specified error code, detail message, and cause.
     *
     * @param errcode   The error code.
     * @param errmsg    The detail message.
     * @param throwable The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public NoSuchException(final String errcode, final String errmsg, final Throwable throwable) {
        super(errcode, errmsg, throwable);
    }

    /**
     * Constructs a new NoSuchException with the specified detail message, cause, suppression enabled or disabled, and
     * writable stack trace enabled or disabled.
     *
     * @param errmsg             The detail message.
     * @param cause              The cause (which is saved for later retrieval by the {@link Throwable#getCause()}
     *                           method).
     * @param enableSuppression  Whether or not suppression is enabled or disabled.
     * @param writableStackTrace Whether or not the stack trace should be writable.
     */
    public NoSuchException(final String errmsg, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(errmsg, cause, enableSuppression, writableStackTrace);
    }

}
