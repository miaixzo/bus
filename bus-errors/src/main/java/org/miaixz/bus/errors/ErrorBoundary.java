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
package org.miaixz.bus.errors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.miaixz.bus.errors.internal.ErrorIncident;
import org.miaixz.bus.errors.internal.ThrowableNormalizer;
import org.miaixz.bus.errors.policy.ErrorLogPolicy;
import org.miaixz.bus.errors.spi.ErrorLogSink;
import org.miaixz.bus.errors.spi.ErrorObserver;

/**
 * Coordinates pure error decisions with an operation's transport and observation lifecycle.
 *
 * @author Kimi Liu
 */
public final class ErrorBoundary {

    /**
     * Registrations for the standard pipeline, snapshotted by the list for concurrent publication.
     */
    private static final CopyOnWriteArrayList<Registration> OBSERVERS = new CopyOnWriteArrayList<>();

    /**
     * Error log sinks independent of the observer chain, shared by standard and custom boundaries.
     */
    private static final CopyOnWriteArrayList<LogRegistration> LOGS = new CopyOnWriteArrayList<>();

    /**
     * Per-thread guard that prevents logging, tracing or metrics failures from reentering event publication.
     */
    private static final ThreadLocal<Boolean> PUBLISHING = new ThreadLocal<>();

    /**
     * Prevents diagnostic sinks and their fallback from recursively reporting their own failures.
     */
    private static final ThreadLocal<Boolean> DIAGNOSING = new ThreadLocal<>();

    /**
     * Last-resort sink when a configured backend fails or a secondary failure has no backend.
     */
    private static final ErrorLogSink FALLBACK = new SystemErrorLogSink();

    /**
     * Shared boundary used by built-in adapters and library components.
     */
    private static final ErrorBoundary STANDARD = new ErrorBoundary(ErrorEngine.standard(), ErrorObserver.NOOP);

    /**
     * Allows stable identifiers without control characters or unbounded log values.
     */
    private static final Pattern SAFE_TOKEN = Pattern.compile("[A-Za-z0-9_.$<>-]{1,192}");

    /**
     * Classifies original failures and projects approved fields before observation.
     */
    private final ErrorEngine engine;
    /**
     * Observer attached to this boundary.
     */
    private final ErrorObserver observer;

    /**
     * Creates a boundary with explicit, independently replaceable decision and observation logic.
     *
     * @param engine   error classification and decision engine
     * @param observer observer for safe attempt and terminal events
     */
    public ErrorBoundary(ErrorEngine engine, ErrorObserver observer) {
        this.engine = Objects.requireNonNull(engine, "engine");
        this.observer = Objects.requireNonNull(observer, "observer");
    }

    /**
     * Returns the shared boundary used by built-in adapters and component integrations.
     *
     * @return standard boundary shared by built-in compatibility adapters
     */
    public static ErrorBoundary standard() {
        return STANDARD;
    }

    /**
     * Registers a sink for built-in component boundaries. The caller owns the subscription and should close it when its
     * logging, tracing or metrics lifecycle ends.
     *
     * @param observer sink receiving safe failure events on the reporting thread
     * @return idempotent subscription for this registration
     */
    public static Subscription subscribeStandard(ErrorObserver observer) {
        var registration = new Registration(Objects.requireNonNull(observer, "observer"));
        OBSERVERS.add(registration);
        return registration;
    }

    /**
     * Registers a log backend for both primary failures and isolated handler diagnostics.
     *
     * @param sink backend receiving policy-selected log records
     * @return independently closeable registration
     */
    public static Subscription subscribeLogging(ErrorLogSink sink) {
        var registration = new LogRegistration(Objects.requireNonNull(sink, "sink"));
        LOGS.add(registration);
        return () -> LOGS.removeIf(candidate -> candidate == registration);
    }

    /**
     * Reports a secondary failure without dispatching it to error observers or invoking classifiers again.
     *
     * @param context   context of the original operation
     * @param stage     stable name of the failing handler stage
     * @param failure   error raised by the handler
     * @param requestId server-generated correlation identifier, or null
     */
    public static void diagnose(ErrorContext context, String stage, Throwable failure, String requestId) {
        diagnose(context, stage, failure, requestId, null, true);
    }

    /**
     * Reports a backend-internal failure through the independent sink, bypassing all logging plugins.
     *
     * @param context context of the original logging operation
     * @param stage   stable name of the failed backend stage
     * @param failure error raised by the backend extension
     */
    public static void diagnoseLoggingFailure(ErrorContext context, String stage, Throwable failure) {
        diagnose(context, stage, failure, null, null, false);
    }

    /**
     * Dispatches a secondary failure outside the observer pipeline and excludes a failing log backend when applicable.
     *
     * @param context    context of the original operation
     * @param stage      stable name of the failing handler stage
     * @param failure    error raised by the handler
     * @param requestId  server-generated correlation identifier, or null
     * @param excluded   backend that caused the failure, or null
     * @param configured whether the configured logging backends can safely be called
     */
    private static void diagnose(
            ErrorContext context,
            String stage,
            Throwable failure,
            String requestId,
            ErrorLogSink excluded,
            boolean configured) {
        ThrowableNormalizer.rethrowFatal(failure);
        if (Boolean.TRUE.equals(DIAGNOSING.get())) {
            return;
        }
        DIAGNOSING.set(true);
        try {
            var diagnostic = new ErrorContext(context.listener(), stage, context.phase(), context.policyVersion());
            var event = new ErrorObserver.Event(diagnostic, ErrorType.CALLBACK_FAILURE, 0, true);
            var decision = new ErrorDecision(ErrorDecision.Action.PROPAGATE,
                    ErrorEngine.descriptor(ErrorType.CALLBACK_FAILURE), 0, ErrorDecision.Basis.VERIFIED_FACT,
                    diagnostic.policyVersion());
            boolean accepted = false;
            for (var registration : LOGS) {
                if (configured && registration.sink() != excluded) {
                    accepted |= writeLog(registration.sink(), event, decision, failure, requestId, null);
                }
            }
            if (!accepted) {
                writeLog(FALLBACK, event, decision, failure, requestId, null);
            }
        } finally {
            DIAGNOSING.remove();
        }
    }

    /**
     * Emits one policy-selected log record and isolates a recoverable failure of its backend.
     *
     * @param sink      selected log backend
     * @param event     structured failure event
     * @param decision  classification and disposition
     * @param failure   original error for bounded stack extraction, or null
     * @param requestId server-generated correlation identifier, or null
     * @param outcome   response write signal, or null
     * @return whether the backend handled the event, including applying its threshold
     */
    private static boolean writeLog(
            ErrorLogSink sink,
            ErrorObserver.Event event,
            ErrorDecision decision,
            Throwable failure,
            String requestId,
            String outcome) {
        try {
            int limit = failure == null ? 0 : ErrorLogPolicy.stackFrameLimit(sink, event);
            ErrorLogPolicy.write(sink, event, details(decision, failure, requestId, outcome, limit));
            return true;
        } catch (RuntimeException | Error backendFailure) {
            diagnose(event.context(), "log-sink", backendFailure, requestId, sink, true);
            return false;
        }
    }

    /**
     * Delivers a primary event and isolates observer failures from the original outcome and remaining observers.
     *
     * @param observer  recipient of the approved event
     * @param event     original event contract
     * @param decision  classification and disposition for this operation
     * @param failure   diagnostic exception, or null
     * @param requestId server-generated identifier, or null
     * @param outcome   response write signal, or null
     */
    private static void notifyObserver(
            ErrorObserver observer,
            ErrorObserver.Event event,
            ErrorDecision decision,
            Throwable failure,
            String requestId,
            String outcome) {
        try {
            int limit = failure == null ? 0
                    : Math.min(ErrorLogPolicy.MAX_STACK_FRAMES, observer.stackFrameLimit(event));
            observer.observe(event, details(decision, failure, requestId, outcome, limit));
        } catch (RuntimeException | Error observerFailure) {
            diagnose(event.context(), "error-observer", observerFailure, requestId);
        }
    }

    /**
     * Projects a failure and correlation data to bounded diagnostic fields.
     *
     * @param decision  classification and disposition
     * @param failure   original exception, or null
     * @param requestId server-generated correlation identifier, or null
     * @param outcome   response write signal, or null
     * @param limit     maximum number of stack entries to examine
     * @return diagnostic data without raw request content or exception messages
     */
    private static ErrorObserver.Details details(
            ErrorDecision decision,
            Throwable failure,
            String requestId,
            String outcome,
            int limit) {
        return new ErrorObserver.Details(decision.action(), decision.basis(), safeToken(requestId), safeToken(outcome),
                failure == null ? null : safeToken(failure.getClass().getName()), stackFrames(failure, limit));
    }

    /**
     * Extracts bounded class and method locations without file names, exception messages or nested causes.
     *
     * @param failure source exception, or null when no frames are requested
     * @param limit   maximum number of stack entries to examine
     * @return safe stack locations
     */
    private static List<String> stackFrames(Throwable failure, int limit) {
        if (failure == null || limit <= 0) {
            return List.of();
        }
        var frames = new ArrayList<String>(limit);
        StackTraceElement[] trace;
        try {
            trace = failure.getStackTrace();
        } catch (RuntimeException | Error unreadable) {
            ThrowableNormalizer.rethrowFatal(unreadable);
            return List.of();
        }
        if (trace == null) {
            return List.of();
        }
        for (int index = 0; index < Math.min(trace.length, limit); index++) {
            var frame = trace[index];
            if (frame != null) {
                String owner = safeToken(frame.getClassName());
                String method = safeToken(frame.getMethodName());
                if (owner != null && method != null) {
                    frames.add(owner + "." + method + ":" + frame.getLineNumber());
                }
            }
        }
        return frames;
    }

    /**
     * Accepts one bounded identifier suitable for diagnostic logs.
     *
     * @param value candidate identifier
     * @return approved value, or null
     */
    private static String safeToken(String value) {
        return value != null && value.length() <= 192 && SAFE_TOKEN.matcher(value).matches() ? value : null;
    }

    /**
     * Lifecycle of a standard error observer.
     *
     * @author Kimi Liu
     */
    public interface Subscription extends AutoCloseable {

        /**
         * Removes the registered observer. An in-progress notification may still complete.
         */
        @Override
        void close();

    }

    /**
     * Represents one observer registration and removes it by identity on close.
     *
     * @author Kimi Liu
     */
    private static final class Registration implements Subscription {

        /**
         * Sink owned by this registration.
         */
        private final ErrorObserver observer;

        /**
         * Creates one independently removable observer registration.
         *
         * @param observer sink owned by this registration
         */
        private Registration(ErrorObserver observer) {
            this.observer = observer;
        }

        /**
         * Removes this registration from subsequent standard notifications.
         */
        @Override
        public void close() {
            OBSERVERS.remove(this);
        }

    }

    /**
     * Stores one independent log backend subscription.
     *
     * @param sink registered log backend
     * @author Kimi Liu
     */
    private record LogRegistration(ErrorLogSink sink) {
    }

    /**
     * Writes diagnostics even if the configured logging backend is unavailable.
     *
     * @author Kimi Liu
     */
    private static final class SystemErrorLogSink implements ErrorLogSink {

        /**
         * JDK logging category for diagnostics outside the normal event pipeline.
         */
        private static final String CATEGORY = "org.miaixz.bus.errors.diagnostics";

        /**
         * Creates the independent diagnostic sink.
         */
        private SystemErrorLogSink() {
        }

        /**
         * Checks the JDK logging threshold for a policy-selected severity.
         *
         * @param level policy-selected severity
         * @return whether the JDK logger accepts the level
         */
        @Override
        public boolean isEnabled(ErrorLogPolicy.Level level) {
            return System.getLogger(CATEGORY).isLoggable(systemLevel(level));
        }

        /**
         * Formats approved values for the JDK logger, which uses indexed placeholders.
         *
         * @param level     policy-selected severity
         * @param format    template using sequential placeholders
         * @param arguments approved diagnostic fields
         */
        @Override
        public void write(ErrorLogPolicy.Level level, String format, Object... arguments) {
            var message = new StringBuilder();
            int offset = 0;
            for (Object argument : arguments) {
                int placeholder = format.indexOf("{}", offset);
                message.append(format, offset, placeholder).append(argument);
                offset = placeholder + 2;
            }
            message.append(format, offset, format.length());
            System.getLogger(CATEGORY).log(systemLevel(level), message.toString());
        }

        /**
         * Converts a policy severity to its corresponding JDK logging level.
         *
         * @param level policy-selected severity
         * @return corresponding JDK logging level
         */
        private static System.Logger.Level systemLevel(ErrorLogPolicy.Level level) {
            return level == ErrorLogPolicy.Level.WARN ? System.Logger.Level.WARNING
                    : System.Logger.Level.valueOf(level.name());
        }

    }

    /**
     * Executes a handler callback whose recoverable failure must leave the original operation available.
     *
     * @param context   context of the original operation
     * @param stage     stable handler stage
     * @param requestId server-generated correlation identifier, or null
     * @param callback  handler side effect
     */
    public static void runCallback(ErrorContext context, String stage, String requestId, Runnable callback) {
        try {
            callback.run();
        } catch (RuntimeException | Error failure) {
            diagnose(context, stage, failure, requestId);
        }
    }

    /**
     * Executes a transport or response effect that may raise a checked failure.
     *
     * @param <T> transport result
     * @param <X> checked exception preserved for the caller
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface Effect<T, X extends Exception> {

        /**
         * Runs the transport effect.
         *
         * @return result supplied by the effect
         * @throws X if the effect fails
         */
        T run() throws X;

    }

    /**
     * Creates error handling state for a single operation.
     *
     * @return a new operation with independent terminal action and observation claims
     */
    public Operation open() {
        return new Operation();
    }

    /**
     * Operation-local processing state. Keep one instance per request, message, or task.
     *
     * @author Kimi Liu
     */
    public final class Operation {

        /**
         * Operation-local claims for terminal actions and final observation.
         */
        private final ErrorIncident incident = new ErrorIncident();

        /**
         * Creates independent claims for one operation.
         */
        private Operation() {
        }

        /**
         * Evaluates one signal, without claiming a transport write or observing the result.
         *
         * @param context current phase and boundary
         * @param facts   verified facts
         * @param failure original failure, possibly {@code null}
         * @return classified decision
         */
        public ErrorDecision decide(ErrorContext context, FailureFacts facts, Throwable failure) {
            return engine.decide(
                    context,
                    facts,
                    failure,
                    secondary -> diagnose(context, "error-classifier", secondary, null));
        }

        /**
         * Atomically claims the terminal response or protocol action for this operation.
         *
         * @return whether this caller owns the operation's terminal transport action
         */
        public boolean claimTermination() {
            return this.incident.claimTermination();
        }

        /**
         * Executes a selected response and reports its own failure separately from the original error.
         *
         * @param context   execution boundary
         * @param decision  selected original error decision
         * @param failure   original error, or null
         * @param requestId server-generated correlation identifier, or null
         * @param effect    response construction or write
         * @param <T>       transport result
         * @param <X>       checked exception preserved for the transport
         * @return result of the response effect
         * @throws X if the response effect fails
         */
        public <T, X extends Exception> T complete(
                ErrorContext context,
                ErrorDecision decision,
                Throwable failure,
                String requestId,
                Effect<T, X> effect) throws X {
            try {
                return effect.run();
            } catch (Exception | Error secondary) {
                diagnose(context, "error-response", secondary, requestId);
                throw secondary;
            } finally {
                observe(context, decision, true, failure, requestId);
            }
        }

        /**
         * Reports an attempt or claims the final observation. Observer failure is isolated from the caller.
         *
         * @param context  execution boundary
         * @param decision previously classified decision
         * @param terminal whether the operation reached its final failure
         */
        public void observe(ErrorContext context, ErrorDecision decision, boolean terminal) {
            observe(context, decision, terminal, null, null);
        }

        /**
         * Observes the failure with bounded diagnostic metadata for subscribers that request it.
         *
         * @param context   execution boundary
         * @param decision  previously classified decision
         * @param terminal  whether the operation reached its final failure
         * @param failure   diagnostic exception, or null
         * @param requestId server-generated request identifier, or null
         */
        public void observe(
                ErrorContext context,
                ErrorDecision decision,
                boolean terminal,
                Throwable failure,
                String requestId) {
            observe(context, decision, terminal, failure, requestId, null);
        }

        /**
         * Observes the failure with safe correlation and the completion signal from a response write.
         *
         * @param context   execution boundary
         * @param decision  previously classified decision
         * @param terminal  whether the operation reached its final failure
         * @param failure   diagnostic exception, or null
         * @param requestId server-generated request identifier, or null
         * @param outcome   response write signal, or null
         */
        public void observe(
                ErrorContext context,
                ErrorDecision decision,
                boolean terminal,
                Throwable failure,
                String requestId,
                String outcome) {
            boolean standard = ErrorBoundary.this == STANDARD;
            if (decision.error() == null || Boolean.TRUE.equals(PUBLISHING.get())
                    || Boolean.TRUE.equals(DIAGNOSING.get()) || (terminal && !this.incident.claimObservation())
                    || (standard && OBSERVERS.isEmpty() && LOGS.isEmpty())) {
                return;
            }
            PUBLISHING.set(true);
            try {
                var event = new ErrorObserver.Event(context, engine.publicError(decision.error()).type(),
                        decision.status(), terminal);
                var logBackends = LOGS.iterator();
                if (logBackends.hasNext()) {
                    boolean accepted = false;
                    while (logBackends.hasNext()) {
                        accepted |= writeLog(logBackends.next().sink(), event, decision, failure, requestId, outcome);
                    }
                    if (!accepted) {
                        writeLog(FALLBACK, event, decision, failure, requestId, outcome);
                    }
                }
                if (standard) {
                    for (var registration : OBSERVERS) {
                        notifyObserver(registration.observer, event, decision, failure, requestId, outcome);
                    }
                } else {
                    notifyObserver(observer, event, decision, failure, requestId, outcome);
                }
            } finally {
                PUBLISHING.remove();
            }
        }

    }

}
