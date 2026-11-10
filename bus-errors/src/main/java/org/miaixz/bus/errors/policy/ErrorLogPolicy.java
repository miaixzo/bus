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
package org.miaixz.bus.errors.policy;

import java.util.regex.Pattern;

import org.miaixz.bus.errors.ErrorType;
import org.miaixz.bus.errors.spi.ErrorLogSink;
import org.miaixz.bus.errors.spi.ErrorObserver.Details;
import org.miaixz.bus.errors.spi.ErrorObserver.Event;

/**
 * Selects error log severity, diagnostic detail and bounded output independently of the logging backend.
 *
 * @author Kimi Liu
 */
public final class ErrorLogPolicy {

    /**
     * Accepted component and operation labels in the log output.
     */
    private static final Pattern LABEL = Pattern.compile("[A-Za-z0-9_.$-]{1,128}");

    /**
     * Compact fields shared by every error log level.
     */
    private static final String SUMMARY =
            "Bus error component={} operation={} type={} status={} phase={} terminal={} requestId={} outcome={}";

    /**
     * Diagnostic fields used for attempts, unexpected failures and trace-level inspection.
     */
    private static final String DETAIL = SUMMARY + " action={} basis={} policyVersion={} exception={} stack={}";

    /**
     * Maximum number of stack frames used for ordinary error output.
     */
    private static final int ERROR_FRAMES = 4;

    /**
     * Maximum number of stack frames collected at the most detailed logging threshold.
     */
    public static final int MAX_STACK_FRAMES = 12;

    /**
     * Prevents construction of the stateless log policy.
     */
    private ErrorLogPolicy() {
    }

    /**
     * Severity chosen by the Bus error policy.
     *
     * @author Kimi Liu
     */
    public enum Level {

        /** Detailed diagnostic signal. */
        TRACE,
        /** Expected rejection or recoverable attempt. */
        DEBUG,
        /** Observable operational rejection. */
        INFO,
        /** Failure requiring operational attention. */
        WARN,
        /** Unexpected terminal or handler failure. */
        ERROR

    }

    /**
     * Selects the stack detail supported by the backend threshold for the given event.
     *
     * @param sink selected logging backend
     * @param event structured failure event
     * @return number of safe stack frames to collect
     */
    public static int stackFrameLimit(ErrorLogSink sink, Event event) {
        Level level = levelFor(event);
        if (!sink.isEnabled(level)) {
            return 0;
        }
        return sink.isEnabled(Level.TRACE) ? MAX_STACK_FRAMES : level == Level.ERROR ? ERROR_FRAMES : 0;
    }

    /**
     * Writes the format and fields chosen for the severity and current backend threshold.
     *
     * @param sink selected logging backend
     * @param event structured failure event
     * @param details safe diagnostic snapshot
     */
    public static void write(ErrorLogSink sink, Event event, Details details) {
        Level level = levelFor(event);
        if (!sink.isEnabled(level)) {
            return;
        }
        var context = event.context();
        String component = label(context.listener());
        String operation = label(context.protocol());
        String type = label(event.type().id());
        Object status = event.status() == 0 ? null : event.status();
        if (level == Level.INFO && !sink.isEnabled(Level.TRACE)) {
            sink.write(level, SUMMARY, component, operation, type, status, context.phase(), event.terminal(),
                    details.requestId(), details.outcome());
            return;
        }
        sink.write(level, DETAIL, component, operation, type, status, context.phase(), event.terminal(),
                details.requestId(), details.outcome(), details.action(), details.basis(),
                context.policyVersion() == 0 ? null : context.policyVersion(), details.exceptionType(),
                details.frames());
    }

    /**
     * Separates failures that need intervention from expected rejections and transient attempts.
     *
     * @param event structured failure event
     * @return log level used for the event
     */
    public static Level levelFor(Event event) {
        ErrorType type = event.type();
        if (ErrorType.CANCELLED.equals(type)) {
            return Level.TRACE;
        }
        if (!event.terminal()) {
            return Level.DEBUG;
        }
        if (ErrorType.GATEWAY_OVERLOADED.equals(type) || ErrorType.OUTCOME_UNKNOWN.equals(type)) {
            return Level.WARN;
        }
        if (ErrorType.PAYLOAD_TOO_LARGE.equals(type)) {
            return Level.INFO;
        }
        if ((event.status() >= 400 && event.status() < 500) || ErrorType.INVALID_REQUEST.equals(type)
                || ErrorType.UNAUTHENTICATED.equals(type) || ErrorType.FORBIDDEN.equals(type)) {
            return Level.DEBUG;
        }
        return Level.ERROR;
    }

    /**
     * Filters component and operation labels supplied by extension boundaries.
     *
     * @param value candidate label
     * @return bounded identifier, or a placeholder for an invalid value
     */
    private static String label(String value) {
        return value != null && value.length() <= 128 && LABEL.matcher(value).matches() ? value : "unknown";
    }

}
