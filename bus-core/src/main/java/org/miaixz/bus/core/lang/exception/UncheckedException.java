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
import java.util.Locale;
import java.util.ResourceBundle;

import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.I18n;
import org.miaixz.bus.core.lang.Keys;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents an unchecked exception, extending {@link RuntimeException}, that can carry an error code and message. This
 * class provides various constructors for flexibility in error reporting and supports internationalization for error
 * messages.
 *
 * @author Kimi Liu
 * @since Java 17+
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
    public UncheckedException(final Errors errors, String errmsg) {
        super(errmsg);
        this.errcode = errors.getKey();
        this.errmsg = errmsg;
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
     * Constructs a new UncheckedException with the specified detail message format and arguments.
     *
     * @param format The format string for the detail message.
     * @param args   The arguments referenced by the format specifiers in the format string.
     */
    protected UncheckedException(final String format, final Object... args) {
        super(String.format(format, args));
        this.errmsg = String.format(format, args);
    }

    /**
     * Constructs a new UncheckedException with the specified cause, detail message format, and arguments.
     *
     * @param cause  The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     * @param format The format string for the detail message.
     * @param args   The arguments referenced by the format specifiers in the format string.
     */
    protected UncheckedException(final Throwable cause, final String format, final Object... args) {
        super(String.format(format, args), cause);
        this.errmsg = String.format(format, args);
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
     * Getmessage method.
     *
     * @return the String value
     */
    @Override
    public String getMessage() {
        if (this.errcode != null) {
            return this.errmsg;
        }
        return super.getMessage();
    }

    /**
     * Getlocalizedmessage method.
     *
     * @return the String value
     */
    @Override
    public String getLocalizedMessage() {
        if (errcode != null) {
            try {
                Locale locale = Locale.forLanguageTag(I18n.AUTO_DETECT.lang());
                ResourceBundle bundle = ResourceBundle.getBundle(Keys.BUNDLE_NAME, locale);
                return bundle.getString(this.errcode);
            } catch (Exception e) {
                // Fallback to the error message registered in ERRORS_CACHE
                Errors.Entry entry = Errors.require(this.errcode);
                return entry != null ? entry.getValue() : this.getMessage();
            }

        }
        return super.getLocalizedMessage();
    }

}
