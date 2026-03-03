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
 * Represents an exception that carries a status code, indicating a specific state or error condition.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StatefulException extends UncheckedException {

    /**
     * The serialization version identifier.
     */
    @Serial
    private static final long serialVersionUID = 2852265690567L;

    /**
     * The status code associated with this exception.
     */
    private int status;

    /**
     * Constructs a new StatefulException with no detail message.
     */
    public StatefulException() {
        super();
    }

    /**
     * Constructs a new StatefulException with the specified cause.
     *
     * @param e The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public StatefulException(final Throwable e) {
        super(e);
    }

    /**
     * Constructs a new StatefulException with the specified detail message.
     *
     * @param errmsg The detail message.
     */
    public StatefulException(final String errmsg) {
        super(errmsg);
    }

    /**
     * Constructs a new StatefulException with the specified error object.
     *
     * @param errors The error object containing error code and message.
     */
    public StatefulException(final Errors errors) {
        super(errors);
    }

    /**
     * Constructs a new StatefulException with the specified error object and detail message.
     *
     * @param errors The error object containing error code.
     * @param errmsg The detail message.
     */
    public StatefulException(final Errors errors, String errmsg) {
        super(errors.getKey(), errmsg);
    }

    /**
     * Constructs a new StatefulException with the specified detail message format and arguments.
     *
     * @param errmsgTemplate The format string for the detail message.
     * @param args           The arguments referenced by the format specifiers in the format string.
     */
    public StatefulException(final String errmsgTemplate, final Object... args) {
        super(errmsgTemplate, args);
    }

    /**
     * Constructs a new StatefulException with the specified detail message and cause.
     *
     * @param errmsg The detail message.
     * @param cause  The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public StatefulException(final String errmsg, final Throwable cause) {
        super(errmsg, cause);
    }

    /**
     * Constructs a new StatefulException with the specified detail message, cause, suppression enabled or disabled, and
     * writable stack trace enabled or disabled.
     *
     * @param errmsg             The detail message.
     * @param cause              The cause (which is saved for later retrieval by the {@link Throwable#getCause()}
     *                           method).
     * @param enableSuppression  Whether or not suppression is enabled or disabled.
     * @param writableStackTrace Whether or not the stack trace should be writable.
     */
    public StatefulException(final String errmsg, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(errmsg, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Constructs a new StatefulException with the specified cause, detail message format, and arguments.
     *
     * @param cause          The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     * @param errmsgTemplate The format string for the detail message.
     * @param args           The arguments referenced by the format specifiers in the format string.
     */
    public StatefulException(final Throwable cause, final String errmsgTemplate, final Object... args) {
        super(cause, errmsgTemplate, args);
    }

    /**
     * Constructs a new StatefulException with the specified status code and detail message.
     *
     * @param status The status code.
     * @param msg    The detail message.
     */
    public StatefulException(final int status, final String msg) {
        super(msg);
        this.status = status;
    }

    /**
     * Constructs a new StatefulException with the specified status code and cause.
     *
     * @param status    The status code.
     * @param throwable The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public StatefulException(final int status, final Throwable throwable) {
        super(throwable);
        this.status = status;
    }

    /**
     * Constructs a new StatefulException with the specified status code, detail message, and cause.
     *
     * @param status    The status code.
     * @param msg       The detail message.
     * @param throwable The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public StatefulException(final int status, final String msg, final Throwable throwable) {
        super(msg, throwable);
        this.status = status;
    }

    /**
     * Retrieves the status code associated with this exception.
     *
     * @return The status code.
     */
    public int getStatus() {
        return status;
    }

}
