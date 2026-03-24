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

import java.io.IOException;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.I18n;
import org.miaixz.bus.core.lang.Keys;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents an exception related to I/O operations or other relevant system issues.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class RelevantException extends IOException {

    /**
     * The serialization version identifier.
     */
    @Serial
    private static final long serialVersionUID = 2852263392627L;

    /**
     * The error code associated with this exception.
     */
    protected String errcode;
    /**
     * The error message associated with this exception.
     */
    protected String errmsg;

    /**
     * Constructs a new RelevantException with no detail message.
     */
    protected RelevantException() {
        super();
    }

    /**
     * Constructs a new RelevantException with the specified cause.
     *
     * @param cause The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    protected RelevantException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new RelevantException with the specified detail message.
     *
     * @param errmsg The detail message.
     */
    protected RelevantException(final String errmsg) {
        super(errmsg);
    }

    /**
     * Constructs a new RelevantException with the specified error object.
     *
     * @param errors The error object containing error code and message.
     */
    protected RelevantException(final Errors errors) {
        super(errors.getValue());
        this.errcode = errors.getKey();
        this.errmsg = errors.getValue();
    }

    /**
     * Constructs a new RelevantException with the specified error object and detail message.
     *
     * @param errors The error object containing error code.
     * @param errmsg The detail message.
     */
    public RelevantException(final Errors errors, String errmsg) {
        super(errmsg);
        this.errcode = errors.getKey();
        this.errmsg = errmsg;
    }

    /**
     * Constructs a new RelevantException with the specified detail message and cause.
     *
     * @param errmsg The detail message.
     * @param cause  The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    protected RelevantException(final String errmsg, final Throwable cause) {
        super(errmsg, cause);
    }

    /**
     * Constructs a new RelevantException with the specified error code and detail message.
     *
     * @param errcode The error code.
     * @param errmsg  The detail message.
     */
    protected RelevantException(final String errcode, final String errmsg) {
        super(errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    /**
     * Constructs a new RelevantException with the specified detail message format and arguments.
     *
     * @param format The format string for the detail message.
     * @param args   The arguments referenced by the format specifiers in the format string.
     */
    protected RelevantException(final String format, final Object... args) {
        super(String.format(format, args));
    }

    /**
     * Constructs a new RelevantException with the specified cause, detail message format, and arguments.
     *
     * @param e    The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     * @param fmt  The format string for the detail message.
     * @param args The arguments referenced by the format specifiers in the format string.
     */
    protected RelevantException(final Throwable e, String fmt, final Object... args) {
        super(String.format(fmt, args), e);
    }

    /**
     * Creates a new RelevantException indicating an unimplemented feature.
     *
     * @return A new RelevantException instance.
     */
    protected static RelevantException noImplement() {
        return new RelevantException("Not implement yet!");
    }

    /**
     * Creates a new RelevantException indicating an impossible scenario.
     *
     * @return A new RelevantException instance.
     */
    protected static RelevantException impossible() {
        return new RelevantException("r u kidding me?! It is impossible!");
    }

    /**
     * Unwraps the given throwable to its root cause if it's an {@link InvocationTargetException} or a
     * {@link RuntimeException} with a cause.
     *
     * @param e The throwable to unwrap.
     * @return The unwrapped throwable, or null if the input is null.
     */
    protected static Throwable unwrapThrow(final Throwable e) {
        if (null == e) {
            return null;
        }
        if (e instanceof InvocationTargetException) {
            InvocationTargetException itE = (InvocationTargetException) e;
            if (null != itE.getTargetException())
                return unwrapThrow(itE.getTargetException());
        }
        if (e instanceof RuntimeException && null != e.getCause()) {
            return unwrapThrow(e.getCause());
        }
        return e;
    }

    /**
     * Checks if the given throwable or any of its causes is of the specified cause type.
     *
     * @param e         The throwable to check.
     * @param causeType The class of the cause type to look for.
     * @return True if the throwable or any of its causes is of the specified type, false otherwise.
     */
    protected static boolean isCauseBy(final Throwable e, final Class<? extends Throwable> causeType) {
        if (e.getClass() == causeType)
            return true;
        Throwable cause = e.getCause();
        if (null == cause)
            return false;
        return isCauseBy(cause, causeType);
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
