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
package org.miaixz.bus.core.xyz;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.io.stream.FastByteArrayOutputStream;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Exception utility class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExceptionKit {

    /**
     * Gets the full message, including the exception name, in the format: {SimpleClassName}: {ThrowableMessage}.
     *
     * @param e The exception.
     * @return The full message.
     */
    public static String getMessage(final Throwable e) {
        if (null == e) {
            return Normal.NULL;
        }
        return StringKit.format("{}: {}", e.getClass().getSimpleName(), e.getMessage());
    }

    /**
     * Gets the message from an exception by calling `e.getMessage()`.
     *
     * @param e The exception.
     * @return The message.
     */
    public static String getSimpleMessage(final Throwable e) {
        return (null == e) ? Normal.NULL : e.getMessage();
    }

    /**
     * Wraps a checked exception with a runtime exception. If the provided throwable is already a `RuntimeException`, it
     * is returned directly.
     *
     * @param throwable The throwable.
     * @return A `RuntimeException`.
     */
    public static RuntimeException wrapRuntime(final Throwable throwable) {
        if (throwable instanceof IOException) {
            return new InternalException(throwable);
        }
        if (throwable instanceof RuntimeException) {
            return (RuntimeException) throwable;
        }
        return new InternalException(throwable);
    }

    /**
     * Wraps a message in a `RuntimeException`.
     *
     * @param message The exception message.
     * @return A `RuntimeException`.
     */
    public static RuntimeException wrapRuntime(final String message) {
        return new RuntimeException(message);
    }

    /**
     * Wraps a throwable and a formatted message in a `RuntimeException`.
     *
     * @param throwable The throwable.
     * @param message   The formatted message.
     * @param args      The message arguments.
     * @return A `RuntimeException`.
     */
    public static RuntimeException wrapRuntime(final Throwable throwable, final String message, final Object... args) {
        return new RuntimeException(StringKit.format(message, args), throwable);
    }

    /**
     * Wraps a throwable in another exception type.
     *
     * @param <T>           The type of the wrapper exception.
     * @param throwable     The throwable to wrap.
     * @param wrapThrowable The class of the wrapper exception.
     * @return The wrapped exception.
     */
    public static <T extends Throwable> T wrap(final Throwable throwable, final Class<T> wrapThrowable) {
        if (wrapThrowable.isInstance(throwable)) {
            return (T) throwable;
        }
        return ReflectKit.newInstance(wrapThrowable, throwable);
    }

    /**
     * Wraps and re-throws a throwable. `RuntimeException` and `Error` are re-thrown directly; other checked exceptions
     * are wrapped in `UndeclaredThrowableException`.
     *
     * @param throwable The throwable.
     */
    public static void wrapAndThrow(final Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        throw new UndeclaredThrowableException(throwable);
    }

    /**
     * Wraps a message in a `RuntimeException` and throws it.
     *
     * @param message The exception message.
     */
    public static void wrapRuntimeAndThrow(final String message) {
        throw new RuntimeException(message);
    }

    /**
     * Unwraps nested exceptions like `InvocationTargetException` and `UndeclaredThrowableException` to get the
     * underlying business exception.
     *
     * @param wrapped The wrapped exception.
     * @return The unwrapped (original) exception.
     */
    public static Throwable unwrap(final Throwable wrapped) {
        Throwable unwrapped = wrapped;
        while (true) {
            if (unwrapped instanceof InvocationTargetException) {
                unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
            } else if (unwrapped instanceof UndeclaredThrowableException) {
                unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
            } else {
                return unwrapped;
            }
        }
    }

    /**
     * Gets the current thread's stack trace.
     *
     * @return An array of `StackTraceElement`.
     */
    public static StackTraceElement[] getStackElements() {
        return Thread.currentThread().getStackTrace();
    }

    /**
     * Gets a specific `StackTraceElement` by its depth.
     *
     * @param i The depth.
     * @return The `StackTraceElement`.
     */
    public static StackTraceElement getStackElement(final int i) {
        return Thread.currentThread().getStackTrace()[i];
    }

    /**
     * Gets a `StackTraceElement` relative to a specific fully qualified class name.
     *
     * @param fqcn The fully qualified class name.
     * @param i    The relative depth from the specified class.
     * @return The `StackTraceElement`.
     */
    public static StackTraceElement getStackElement(final String fqcn, final int i) {
        final StackTraceElement[] stackTraceArray = Thread.currentThread().getStackTrace();
        final int index = ArrayKit.matchIndex((ele) -> StringKit.equals(fqcn, ele.getClassName()), stackTraceArray);
        if (index > 0) {
            return stackTraceArray[index + i];
        }
        return null;
    }

    /**
     * Gets the root `StackTraceElement` (the entry point of the thread).
     *
     * @return The root `StackTraceElement`.
     */
    public static StackTraceElement getRootStackElement() {
        final StackTraceElement[] stackElements = Thread.currentThread().getStackTrace();
        return Thread.currentThread().getStackTrace()[stackElements.length - 1];
    }

    /**
     * Converts a stack trace to a single-line string.
     *
     * @param throwable The throwable.
     * @return The stack trace as a string.
     */
    public static String stacktraceToOneLineString(final Throwable throwable) {
        return stacktraceToOneLineString(throwable, 3000);
    }

    /**
     * Converts a stack trace to a single-line string with a character limit.
     *
     * @param throwable The throwable.
     * @param limit     The maximum length.
     * @return The stack trace as a string.
     */
    public static String stacktraceToOneLineString(final Throwable throwable, final int limit) {
        final Map<Character, String> replaceCharToStrMap = new HashMap<>();
        replaceCharToStrMap.put(Symbol.C_CR, Symbol.SPACE);
        replaceCharToStrMap.put(Symbol.C_LF, Symbol.SPACE);
        replaceCharToStrMap.put(Symbol.C_TAB, Symbol.SPACE);

        return stacktraceToString(throwable, limit, replaceCharToStrMap);
    }

    /**
     * Converts a stack trace to a full string.
     *
     * @param throwable The throwable.
     * @return The stack trace as a string.
     */
    public static String stacktraceToString(final Throwable throwable) {
        return stacktraceToString(throwable, 3000);
    }

    /**
     * Converts a stack trace to a full string with a character limit.
     *
     * @param throwable The throwable.
     * @param limit     The maximum length.
     * @return The stack trace as a string.
     */
    public static String stacktraceToString(final Throwable throwable, final int limit) {
        return stacktraceToString(throwable, limit, null);
    }

    /**
     * Converts a stack trace to a full string with a character limit and custom character replacements.
     *
     * @param throwable           The throwable.
     * @param limit               The maximum length.
     * @param replaceCharToStrMap A map for character replacements.
     * @return The stack trace as a string.
     */
    public static String stacktraceToString(
            final Throwable throwable,
            int limit,
            final Map<Character, String> replaceCharToStrMap) {
        final FastByteArrayOutputStream baos = new FastByteArrayOutputStream();
        throwable.printStackTrace(new PrintStream(baos));

        final String exceptionStr = baos.toString();
        final int length = exceptionStr.length();
        if (limit < 0 || limit > length) {
            limit = length;
        }

        if (MapKit.isNotEmpty(replaceCharToStrMap)) {
            final StringBuilder sb = StringKit.builder();
            char c;
            String value;
            for (int i = 0; i < limit; i++) {
                c = exceptionStr.charAt(i);
                value = replaceCharToStrMap.get(c);
                if (null != value) {
                    sb.append(value);
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        } else {
            if (limit == length) {
                return exceptionStr;
            }
            return StringKit.subPre(exceptionStr, limit);
        }
    }

    /**
     * Checks if an exception was caused by any of the specified exception types.
     *
     * @param throwable    The exception.
     * @param causeClasses The exception types to check for in the cause chain.
     * @return `true` if a cause matches.
     */
    public static boolean isCausedBy(final Throwable throwable, final Class<? extends Exception>... causeClasses) {
        return null != getCausedBy(throwable, causeClasses);
    }

    /**
     * Gets the first exception in the cause chain that matches one of the specified types.
     *
     * @param throwable    The exception.
     * @param causeClasses The exception types to check for.
     * @return The matching exception, or `null`.
     */
    public static Throwable getCausedBy(final Throwable throwable, final Class<? extends Exception>... causeClasses) {
        Throwable cause = throwable;
        while (cause != null) {
            for (final Class<? extends Exception> causeClass : causeClasses) {
                if (causeClass.isInstance(cause)) {
                    return cause;
                }
            }
            cause = cause.getCause();
        }
        return null;
    }

    /**
     * Checks if a throwable is an instance of, is caused by, or has a suppressed exception of a specific type.
     *
     * @param throwable      The throwable.
     * @param exceptionClass The exception class to check for.
     * @return `true` if found.
     */
    public static boolean isFromOrSuppressedThrowable(
            final Throwable throwable,
            final Class<? extends Throwable> exceptionClass) {
        return convertFromOrSuppressedThrowable(throwable, exceptionClass, true) != null;
    }

    /**
     * Checks if a throwable is an instance of, is caused by, or has a suppressed exception of a specific type.
     *
     * @param throwable      The throwable.
     * @param exceptionClass The exception class to check for.
     * @param checkCause     Whether to check the cause chain.
     * @return `true` if found.
     */
    public static boolean isFromOrSuppressedThrowable(
            final Throwable throwable,
            final Class<? extends Throwable> exceptionClass,
            final boolean checkCause) {
        return convertFromOrSuppressedThrowable(throwable, exceptionClass, checkCause) != null;
    }

    /**
     * Finds and returns the first throwable in the hierarchy (self, cause, suppressed) that matches a specific type.
     *
     * @param <T>            The exception type.
     * @param throwable      The throwable.
     * @param exceptionClass The exception class to find.
     * @return The matching throwable, or `null`.
     */
    public static <T extends Throwable> T convertFromOrSuppressedThrowable(
            final Throwable throwable,
            final Class<T> exceptionClass) {
        return convertFromOrSuppressedThrowable(throwable, exceptionClass, true);
    }

    /**
     * Finds and returns the first throwable in the hierarchy (self, cause, suppressed) that matches a specific type.
     *
     * @param <T>            The exception type.
     * @param throwable      The throwable.
     * @param exceptionClass The exception class to find.
     * @param checkCause     Whether to check the cause chain.
     * @return The matching throwable, or `null`.
     */
    public static <T extends Throwable> T convertFromOrSuppressedThrowable(
            final Throwable throwable,
            final Class<T> exceptionClass,
            final boolean checkCause) {
        if (throwable == null || exceptionClass == null) {
            return null;
        }
        if (exceptionClass.isAssignableFrom(throwable.getClass())) {
            return (T) throwable;
        }
        if (checkCause) {
            final Throwable cause = throwable.getCause();
            if (cause != null && exceptionClass.isAssignableFrom(cause.getClass())) {
                return (T) cause;
            }
        }
        final Throwable[] throwables = throwable.getSuppressed();
        if (ArrayKit.isNotEmpty(throwables)) {
            for (final Throwable throwable1 : throwables) {
                if (exceptionClass.isAssignableFrom(throwable1.getClass())) {
                    return (T) throwable1;
                }
            }
        }
        return null;
    }

    /**
     * Gets a list of all exceptions in the cause chain.
     *
     * @param throwable The throwable.
     * @return A list of all throwables in the chain.
     */
    public static List<Throwable> getThrowableList(Throwable throwable) {
        final List<Throwable> list = new ArrayList<>();
        while (throwable != null && !list.contains(throwable)) {
            list.add(throwable);
            throwable = throwable.getCause();
        }
        return list;
    }

    /**
     * Gets the root cause of an exception.
     *
     * @param throwable The throwable.
     * @return The root cause.
     */
    public static Throwable getRootCause(final Throwable throwable) {
        final Throwable cause = throwable.getCause();
        if (null == cause) {
            return throwable;
        }
        return getRootCause(cause);
    }

    /**
     * Gets the message of the root cause of an exception.
     *
     * @param th The throwable.
     * @return The root cause message.
     */
    public static String getRootCauseMessage(final Throwable th) {
        return getMessage(getRootCause(th));
    }

}
