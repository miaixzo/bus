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
package org.miaixz.bus.extra.qrcode;

import java.io.Serial;

import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Exception for QR code related operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QrCodeException extends InternalException {

    @Serial
    private static final long serialVersionUID = 2852287692888L;

    /**
     * Constructs a new QrCodeException with the specified cause.
     *
     * @param e The cause of the exception.
     */
    public QrCodeException(final Throwable e) {
        super(e);
    }

    /**
     * Constructs a new QrCodeException with the specified detail message.
     *
     * @param message The detail message.
     */
    public QrCodeException(final String message) {
        super(message);
    }

    /**
     * Constructs a new QrCodeException with a formatted detail message.
     *
     * @param format The format string for the detail message.
     * @param args   The arguments referenced by the format specifiers in the format string.
     */
    public QrCodeException(final String format, final Object... args) {
        super(format, args);
    }

    /**
     * Constructs a new QrCodeException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param cause   The cause of the exception.
     */
    public QrCodeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new QrCodeException with the specified detail message, cause, suppression enabled or disabled, and
     * writable stack trace enabled or disabled.
     *
     * @param message            The detail message.
     * @param cause              The cause of the exception.
     * @param enableSuppression  Whether or not suppression is enabled or disabled.
     * @param writableStackTrace Whether or not the stack trace should be writable.
     */
    public QrCodeException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Constructs a new QrCodeException with the specified cause and a formatted detail message.
     *
     * @param cause  The cause of the exception.
     * @param format The format string for the detail message.
     * @param args   The arguments referenced by the format specifiers in the format string.
     */
    public QrCodeException(final Throwable cause, final String format, final Object... args) {
        super(cause, format, args);
    }

}
