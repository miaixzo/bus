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
 * Represents an exception that occurs during application launch or initialization.
 *
 * @author Kimi Liu
 */
public class LaunchException extends UncheckedException {

    @Serial
    private static final long serialVersionUID = 2536820061185L;

    /**
     * Constructs a new LaunchException with no detail message.
     */
    public LaunchException() {
        super();
    }

    /**
     * Constructs a new LaunchException with the specified cause.
     *
     * @param e The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public LaunchException(final Throwable e) {
        super(e);
    }

    /**
     * Constructs a new LaunchException with the specified detail message.
     *
     * @param message The detail message.
     */
    public LaunchException(final String message) {
        super(message);
    }

    /**
     * Constructs a new LaunchException with the specified error object.
     *
     * @param errors The error object containing error code and message.
     */
    public LaunchException(final Errors errors) {
        super(errors);
    }

    /**
     * Constructs a new LaunchException with the specified error object and detail message.
     *
     * @param errors The error object containing error code.
     * @param desc   The detail message.
     */
    public LaunchException(final Errors errors, final String desc) {
        super(errors, desc);
    }

    /**
     * Constructs a new LaunchException with the specified detail message and arguments for formatting.
     *
     * @param format The format string for the detail message.
     * @param args   The arguments referenced by the format specifiers in the format string.
     */
    public LaunchException(final String format, final Object... args) {
        super(format, args);
    }

    /**
     * Constructs a new LaunchException with the specified error code and detail message.
     *
     * @param code The error code.
     * @param desc The detail message.
     */
    public LaunchException(final String code, final String desc) {
        super(code, desc);
    }

    /**
     * Constructs a new LaunchException with the specified detail message and cause.
     *
     * @param desc  The detail message.
     * @param cause The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public LaunchException(final String desc, final Throwable cause) {
        super(desc, cause);
    }

    /**
     * Constructs a new LaunchException with the specified cause, detail message, and arguments for formatting.
     *
     * @param cause  The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     * @param format The format string for the detail message.
     * @param args   The arguments referenced by the format specifiers in the format string.
     */
    public LaunchException(final Throwable cause, final String format, final Object... args) {
        super(cause, format, args);
    }

    /**
     * Constructs a new LaunchException with the specified error code, detail message, and cause.
     *
     * @param code  The error code.
     * @param desc  The detail message.
     * @param cause The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public LaunchException(final String code, final String desc, final Throwable cause) {
        super(code, desc, cause);
    }

    /**
     * Constructs a new LaunchException with the specified detail message, cause, suppression enabled or disabled, and
     * writable stack trace enabled or disabled.
     *
     * @param message            The detail message.
     * @param cause              The cause (which is saved for later retrieval by the {@link Throwable#getCause()}
     *                           method).
     * @param enableSuppression  Whether or not suppression is enabled or disabled.
     * @param writableStackTrace Whether or not the stack trace should be writable.
     */
    public LaunchException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
