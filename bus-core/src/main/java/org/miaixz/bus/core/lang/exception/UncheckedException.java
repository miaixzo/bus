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

    @Override
    public String getMessage() {
        if (this.errcode != null) {
            return this.errmsg;
        }
        return super.getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        if (errcode != null) {
            try {
                Locale locale = new Locale(I18n.AUTO_DETECT.lang());
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
