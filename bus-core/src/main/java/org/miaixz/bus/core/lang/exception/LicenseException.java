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
package org.miaixz.bus.core.lang.exception;

import java.io.Serial;

import org.miaixz.bus.core.basic.normal.Errors;

/**
 * Represents an exception that occurs due to license-related issues.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LicenseException extends UncheckedException {

    /**
     * The serialization version identifier.
     */
    @Serial
    private static final long serialVersionUID = 2852252876910L;

    /**
     * Constructs a new LicenseException with no detail message.
     */
    public LicenseException() {
        super();
    }

    /**
     * Constructs a new LicenseException with the specified cause.
     *
     * @param e The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public LicenseException(final Throwable e) {
        super(e);
    }

    /**
     * Constructs a new LicenseException with the specified detail message.
     *
     * @param errmsg The detail message.
     */
    public LicenseException(final String errmsg) {
        super(errmsg);
    }

    /**
     * Constructs a new LicenseException with the specified error object.
     *
     * @param errors The error object containing error code and message.
     */
    public LicenseException(final Errors errors) {
        super(errors);
    }

    /**
     * Constructs a new LicenseException with the specified error object and detail message.
     *
     * @param errors The error object containing error code.
     * @param errmsg The detail message.
     */
    public LicenseException(final Errors errors, String errmsg) {
        super(errors.getKey(), errmsg);
    }

    /**
     * Constructs a new LicenseException with the specified detail message and arguments for formatting.
     *
     * @param format The format string for the detail message.
     * @param args   The arguments referenced by the format specifiers in the format string.
     */
    public LicenseException(final String format, final Object... args) {
        super(format, args);
    }

    /**
     * Constructs a new LicenseException with the specified error code and detail message.
     *
     * @param errcode The error code.
     * @param errmsg  The detail message.
     */
    public LicenseException(final String errcode, final String errmsg) {
        super(errcode, errmsg);
    }

    /**
     * Constructs a new LicenseException with the specified error code and cause.
     *
     * @param errcode   The error code.
     * @param throwable The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public LicenseException(final String errcode, final Throwable throwable) {
        super(errcode, throwable);
    }

    /**
     * Constructs a new LicenseException with the specified cause, detail message, and arguments for formatting.
     *
     * @param cause  The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     * @param format The format string for the detail message.
     * @param args   The arguments referenced by the format specifiers in the format string.
     */
    public LicenseException(final Throwable cause, final String format, final Object... args) {
        super(cause, format, args);
    }

    /**
     * Constructs a new LicenseException with the specified error code, detail message, and cause.
     *
     * @param errcode   The error code.
     * @param errmsg    The detail message.
     * @param throwable The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public LicenseException(final String errcode, final String errmsg, final Throwable throwable) {
        super(errcode, errmsg, throwable);
    }

    /**
     * Constructs a new LicenseException with the specified detail message, cause, suppression enabled or disabled, and
     * writable stack trace enabled or disabled.
     *
     * @param errmsg             The detail message.
     * @param cause              The cause (which is saved for later retrieval by the {@link Throwable#getCause()}
     *                           method).
     * @param enableSuppression  Whether or not suppression is enabled or disabled.
     * @param writableStackTrace Whether or not the stack trace should be writable.
     */
    public LicenseException(final String errmsg, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(errmsg, cause, enableSuppression, writableStackTrace);
    }

}
