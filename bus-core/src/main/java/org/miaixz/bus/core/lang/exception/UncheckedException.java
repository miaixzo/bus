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

import lombok.Getter;
import lombok.Setter;

import org.miaixz.bus.core.basic.normal.Errors;

/**
 * Represents an unchecked exception, extending {@link RuntimeException}, that can carry an error code and message. This
 * class provides various constructors for flexibility in error reporting and supports internationalization for error
 * messages.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class UncheckedException extends RuntimeException {

    /**
     * The serialization version identifier.
     */
    @Serial
    private static final long serialVersionUID = 2852266759151L;

    /**
     * The error code associated with this exception.
     */
    protected String errcode;

    /**
     * The error message associated with this exception.
     */
    protected String errmsg;

    /**
     * Constructs a new UncheckedException with no detail message.
     */
    protected UncheckedException() {
        super();
    }

    /**
     * Constructs a new UncheckedException with the specified cause.
     *
     * @param cause The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    protected UncheckedException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new UncheckedException with the specified detail message.
     *
     * @param errmsg The detail message.
     */
    protected UncheckedException(final String errmsg) {
        super(errmsg);
        this.errmsg = errmsg;
    }

    /**
     * Constructs a new UncheckedException with the specified error object.
     *
     * @param errors The error object containing error code and message.
     */
    protected UncheckedException(final Errors errors) {
        super(errors.getValue());
        this.errcode = errors.getKey();
        this.errmsg = errors.getValue();
    }

    /**
     * Constructs a new UncheckedException with the specified error object and detail message.
     *
     * @param errors The error object containing error code.
     * @param errmsg The detail message.
     */
    public UncheckedException(final Errors errors, final String errmsg) {
        this(errors.getKey(), errmsg);
    }

    /**
     * Constructs a new UncheckedException with the specified detail message and cause.
     *
     * @param errmsg The detail message.
     * @param cause  The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    protected UncheckedException(final String errmsg, final Throwable cause) {
        super(errmsg, cause);
        this.errmsg = errmsg;
    }

    /**
     * Constructs a new UncheckedException with the specified error code and detail message.
     *
     * @param errcode The error code.
     * @param errmsg  The detail message.
     */
    protected UncheckedException(final String errcode, final String errmsg) {
        super(errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    /**
     * Constructs a new UncheckedException with the specified error code, detail message, and cause.
     *
     * @param errcode   The error code.
     * @param errmsg    The detail message.
     * @param cause     The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    protected UncheckedException(final String errcode, final String errmsg, final Throwable cause) {
        super(errmsg, cause);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    /**
     * Constructs a new UncheckedException with the specified error object and cause.
     *
     * @param errors The error object containing error code and message.
     * @param cause  The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    protected UncheckedException(final Errors errors, final Throwable cause) {
        this(errors.getKey(), errors.getValue(), cause);
    }

    /**
     * Constructs a new UncheckedException with the specified error object, detail message, and cause.
     *
     * @param errors The error object containing error code.
     * @param errmsg The detail message.
     * @param cause  The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public UncheckedException(final Errors errors, final String errmsg, final Throwable cause) {
        this(errors.getKey(), errmsg, cause);
    }

    /**
     * Constructs a new UncheckedException with the specified detail message format and arguments.
     *
     * @param format The format string for the detail message.
     * @param args   The arguments referenced by the format specifiers in the format string.
     */
    protected UncheckedException(final String format, final Object... args) {
        this(String.format(format, args));
    }

    /**
     * Constructs a new UncheckedException with the specified cause, detail message format, and arguments.
     *
     * @param cause  The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     * @param format The format string for the detail message.
     * @param args   The arguments referenced by the format specifiers in the format string.
     */
    protected UncheckedException(final Throwable cause, final String format, final Object... args) {
        this(String.format(format, args), cause);
    }

    /**
     * Constructs a new UncheckedException with the specified detail message, cause, suppression enabled or disabled,
     * and writable stack trace enabled or disabled.
     *
     * @param errmsg             The detail message.
     * @param cause              The cause (which is saved for later retrieval by the {@link Throwable#getCause()}
     *                           method).
     * @param enableSuppression  Whether or not suppression is enabled or disabled.
     * @param writableStackTrace Whether or not the stack trace should be writable.
     */
    protected UncheckedException(final String errmsg, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(errmsg, cause, enableSuppression, writableStackTrace);
        this.errmsg = errmsg;
    }

    /**
     * Returns the resolved exception message.
     *
     * @return The resolved exception message.
     */
    @Override
    public String getMessage() {
        return Errors.message(this.errcode, this.errmsg, super.getMessage());
    }

    /**
     * Returns the localized exception message.
     *
     * @return The localized exception message.
     */
    @Override
    public String getLocalizedMessage() {
        return Errors.localizedMessage(this.errcode, this.errmsg, super.getLocalizedMessage());
    }

}
