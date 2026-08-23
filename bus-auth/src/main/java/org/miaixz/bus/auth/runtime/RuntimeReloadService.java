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
package org.miaixz.bus.auth.runtime;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Roster;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.registry.SnapshotValidator;
import org.miaixz.bus.auth.worker.loader.BlueprintLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.logger.Logger;

/**
 * Loads, validates, compiles, and atomically publishes complete external Blueprint snapshots.
 * <p>
 * Every call captures one expected runtime container before loading. A candidate becomes visible only through a
 * successful compare-and-set replacement, so validation, compilation, timeout, revision, and concurrency failures
 * preserve the complete previous view. A successful commit changes the protocol-state generation for every Source to
 * the candidate revision, making Code, Token, Session, State, Nonce, and related cache entries from older revisions
 * unreachable to new invocations. Listener failures are isolated after the commit or validation decision.
 * </p>
 *
 * @author Kimi Liu
 */
final class RuntimeReloadService {

    /**
     * External complete Blueprint snapshot loader.
     */
    private final BlueprintLoader loader;

    /**
     * Framework candidate Snapshot validator.
     */
    private final SnapshotValidator validator;

    /**
     * Pure compiler for already validated complete snapshots.
     */
    private final SnapshotCompiler compiler;

    /**
     * Atomic cell holding the current executable runtime container.
     */
    private final RuntimeContainer.Cell containers;

    /**
     * Immutable Roster listener list in declaration order.
     */
    private final RosterNotifier notifier;

    /**
     * Single lifecycle gate shared by the complete runtime.
     */
    private final RuntimeLifecycle lifecycle;

    /**
     * Creates the single complete-snapshot reload orchestrator.
     *
     * @param loader     external complete Blueprint snapshot loader
     * @param validator  candidate Snapshot validator
     * @param compiler   validated snapshot compiler
     * @param containers atomic committed-container cell
     * @param notifier   ordered Roster observation dispatcher
     * @param lifecycle  shared runtime lifecycle gate
     * @throws IllegalArgumentException if a dependency, list, or listener is {@code null}
     */
    RuntimeReloadService(final BlueprintLoader loader, final SnapshotValidator validator,
            final SnapshotCompiler compiler, final RuntimeContainer.Cell containers, final RosterNotifier notifier,
            final RuntimeLifecycle lifecycle) {
        this.loader = Assert.notNull(loader, "Runtime reload loader must not be null");
        this.validator = Assert.notNull(validator, "Runtime reload validator must not be null");
        this.compiler = Assert.notNull(compiler, "Runtime reload compiler must not be null");
        this.containers = Assert.notNull(containers, "Runtime reload containers must not be null");
        this.notifier = Assert.notNull(notifier, "Runtime reload Roster notifier must not be null");
        this.lifecycle = Assert.notNull(lifecycle, "Runtime lifecycle must not be null");
    }

    /**
     * Creates one non-sensitive rejection report for a failed reload stage.
     *
     * @param revision    attempted or currently visible revision
     * @param stage       reload stage that rejected the candidate
     * @param field       logical input or boundary responsible for the rejection
     * @param error       stable framework error code
     * @param description safe non-sensitive failure description
     * @return immutable rejection report
     */
    private static Roster.Report report(
            final long revision,
            final Roster.Fault.Stage stage,
            final String field,
            final Errors error,
            final String description) {
        return new Roster.Report(new Roster.Revision(Math.max(0L, revision)),
                List.of(Roster.Fault.snapshot(stage, Optional.ofNullable(field), error, description)));
    }

    /**
     * Attempts to publish one complete desired Blueprint snapshot.
     *
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation timeout
     * @return stage containing the candidate validation report after rejection or successful commit
     * @throws IllegalArgumentException if an argument is {@code null}
     */
    public CompletionStage<Roster.Report> reload(final Context context, final Timeout timeout) {
        Assert.notNull(context, "Runtime reload context must not be null");
        Assert.notNull(timeout, "Runtime reload timeout must not be null");
        Logger.info(
                true,
                "Auth",
                "Runtime reload started: requestId={}, currentRevision={}",
                context.requestId().value(),
                containers.current().roster().revision().value());
        final RuntimeLifecycle.Lease lease = lifecycle.enter();
        if (lease == null) {
            return CompletableFuture.completedFuture(
                    report(
                            containers.current().roster().revision().value(),
                            Roster.Fault.Stage.COMMIT,
                            "lifecycle",
                            ErrorCode._503,
                            "Authentication runtime is not running"));
        }
        if (timeout.expired()) {
            lease.close();
            return reject(
                    report(
                            containers.current().roster().revision().value(),
                            Roster.Fault.Stage.LOAD,
                            "timeout",
                            ErrorCode._408,
                            "Runtime reload timeout has expired"));
        }
        final RuntimeContainer expected = containers.current();
        final CompletionStage<Outcome<BlueprintLoader.Snapshot>> loading;
        try {
            Logger.debug(
                    false,
                    "Auth",
                    "Blueprint snapshot load started: currentRevision={}",
                    expected.roster().revision().value());
            loading = loader.load(new BlueprintLoader.Request(expected.roster().revision().value()), context, timeout);
        } catch (RuntimeException cause) {
            Logger.error(
                    false,
                    "Auth",
                    cause,
                    "Blueprint loader invocation failed: revision={}, exception={}",
                    expected.roster().revision().value() + 1L,
                    cause.getClass().getSimpleName());
            final Roster.Report report = report(
                    expected.roster().revision().value() + 1L,
                    Roster.Fault.Stage.LOAD,
                    "loader",
                    ErrorCode._500,
                    "Blueprint loader failed before returning a stage");
            rejected(report);
            lease.close();
            return CompletableFuture.completedFuture(report);
        }
        if (loading == null) {
            final Roster.Report report = report(
                    expected.roster().revision().value() + 1L,
                    Roster.Fault.Stage.LOAD,
                    "loader",
                    ErrorCode._500,
                    "Blueprint loader returned no stage");
            rejected(report);
            lease.close();
            return CompletableFuture.completedFuture(report);
        }
        return loading
                .handle(
                        (outcome, cause) -> cause == null ? process(outcome, expected, timeout)
                                : reject(
                                        report(
                                                expected.roster().revision().value() + 1L,
                                                Roster.Fault.Stage.LOAD,
                                                "loader",
                                                ErrorCode._500,
                                                "Blueprint loader stage failed")))
                .thenCompose(Function.identity()).whenComplete((ignored, cause) -> lease.close());
    }

    /**
     * Converts one closed Blueprint-loading outcome into reload processing or a safe rejection report.
     *
     * @param outcome  external Blueprint-loading outcome
     * @param expected runtime container captured before loading
     * @param timeout  shared operation timeout
     * @return stage containing rejection or candidate processing report
     */
    private CompletionStage<Roster.Report> process(
            final Outcome<BlueprintLoader.Snapshot> outcome,
            final RuntimeContainer expected,
            final Timeout timeout) {
        if (outcome == null) {
            return reject(
                    report(
                            expected.roster().revision().value() + 1L,
                            Roster.Fault.Stage.LOAD,
                            "loader",
                            ErrorCode._500,
                            "Blueprint loader returned no outcome"));
        }
        return switch (outcome) {
            case Outcome.Succeeded<BlueprintLoader.Snapshot> succeeded -> process(succeeded.value(), expected, timeout);
            case Outcome.Rejected<BlueprintLoader.Snapshot> rejected -> reject(
                    report(
                            expected.roster().revision().value() + 1L,
                            Roster.Fault.Stage.LOAD,
                            "loader",
                            rejected.failure().error(),
                            rejected.failure().safeDescription()));
            case Outcome.Failed<BlueprintLoader.Snapshot> failed -> reject(
                    report(
                            expected.roster().revision().value() + 1L,
                            Roster.Fault.Stage.LOAD,
                            "loader",
                            failed.failure().error(),
                            failed.failure().safeDescription()));
            default -> reject(
                    report(
                            expected.roster().revision().value() + 1L,
                            Roster.Fault.Stage.LOAD,
                            "loader",
                            ErrorCode._500,
                            "Blueprint loader returned an unsupported outcome"));
        };
    }

    /**
     * Validates, compiles, and atomically commits one loaded snapshot.
     *
     * @param loaded   externally loaded complete Blueprint snapshot
     * @param expected runtime container captured before loading
     * @param timeout  shared operation timeout
     * @return stage containing validation or successful commit report
     */
    private CompletionStage<Roster.Report> process(
            final BlueprintLoader.Snapshot loaded,
            final RuntimeContainer expected,
            final Timeout timeout) {
        if (loaded == null) {
            return reject(
                    report(
                            expected.roster().revision().value() + 1L,
                            Roster.Fault.Stage.LOAD,
                            "snapshot",
                            ErrorCode._500,
                            "Blueprint loader returned no snapshot"));
        }
        final Roster.Snapshot snapshot = new Roster.Snapshot(new Roster.Revision(loaded.revision()), loaded.entries());
        Logger.debug(
                false,
                "Auth",
                "Blueprint snapshot loaded: revision={}, entries={}",
                snapshot.revision().value(),
                snapshot.entries().size());
        if (snapshot.revision().value() <= expected.roster().revision().value()) {
            return reject(
                    report(
                            snapshot.revision().value(),
                            Roster.Fault.Stage.VALIDATE,
                            "revision",
                            ErrorCode._409,
                            "Roster snapshot revision must increase monotonically"));
        }
        if (timeout.expired()) {
            return reject(
                    report(
                            snapshot.revision().value(),
                            Roster.Fault.Stage.VALIDATE,
                            "timeout",
                            ErrorCode._408,
                            "Runtime reload timeout expired after loading"));
        }
        final Roster.Report report;
        try {
            report = validator.validate(snapshot);
        } catch (RuntimeException cause) {
            Logger.error(
                    false,
                    "Auth",
                    cause,
                    "Roster snapshot validation failed: revision={}, exception={}",
                    snapshot.revision().value(),
                    cause.getClass().getSimpleName());
            return reject(
                    report(
                            snapshot.revision().value(),
                            Roster.Fault.Stage.VALIDATE,
                            "validator",
                            ErrorCode._500,
                            "Roster snapshot validation failed operationally"));
        }
        if (!report.faults().isEmpty()) {
            rejected(report);
            return CompletableFuture.completedFuture(report);
        }
        final RuntimeContainer replacement;
        try {
            replacement = compiler.compile(snapshot);
        } catch (SnapshotCompiler.CompilationFailure failure) {
            Logger.error(
                    false,
                    "Auth",
                    "Roster snapshot Source compilation rejected: revision={}, sourceId={}",
                    snapshot.revision().value(),
                    failure.fault().id().getOrNull());
            return reject(new Roster.Report(snapshot.revision(), List.of(failure.fault())));
        } catch (RuntimeException cause) {
            Logger.error(
                    false,
                    "Auth",
                    cause,
                    "Roster snapshot compilation failed: revision={}, exception={}",
                    snapshot.revision().value(),
                    cause.getClass().getSimpleName());
            return reject(
                    report(
                            snapshot.revision().value(),
                            Roster.Fault.Stage.COMPILE,
                            "snapshot",
                            ErrorCode._500,
                            "Roster snapshot compilation failed"));
        }
        if (timeout.expired()) {
            replacement.retire();
            return reject(
                    report(
                            snapshot.revision().value(),
                            Roster.Fault.Stage.COMMIT,
                            "timeout",
                            ErrorCode._408,
                            "Runtime reload timeout expired before commit"));
        }
        final AtomicBoolean replaced = new AtomicBoolean();
        final boolean admitted = lifecycle.commit(() -> {
            replaced.set(containers.replace(expected, replacement));
            if (replaced.get()) {
                notifier.enqueueCommitted(snapshot.revision());
            }
        });
        if (!admitted) {
            replacement.retire();
            return CompletableFuture.completedFuture(
                    report(
                            snapshot.revision().value(),
                            Roster.Fault.Stage.COMMIT,
                            "lifecycle",
                            ErrorCode._503,
                            "Authentication runtime closed before commit"));
        }
        if (!replaced.get()) {
            replacement.retire();
            return reject(
                    report(
                            snapshot.revision().value(),
                            Roster.Fault.Stage.COMMIT,
                            "revision",
                            ErrorCode._409,
                            "Concurrent Roster reload prevented atomic commit"));
        }
        expected.retire();
        notifier.dispatch();
        Logger.info(
                false,
                "Auth",
                "Runtime reload committed: revision={}, entries={}",
                snapshot.revision().value(),
                snapshot.entries().size());
        return CompletableFuture.completedFuture(report);
    }

    /**
     * Returns a completed report after scheduling rejection observation.
     *
     * @param report rejection report
     * @return completed report stage
     */
    private CompletionStage<Roster.Report> reject(final Roster.Report report) {
        rejected(report);
        return CompletableFuture.completedFuture(report);
    }

    /**
     * Schedules one rejected report for isolated listener delivery.
     *
     * @param report rejection report
     */
    private void rejected(final Roster.Report report) {
        final Roster.Fault fault = report.faults().isEmpty() ? null : report.faults().getFirst();
        if (fault != null) {
            Logger.warn(
                    false,
                    "Auth",
                    "Runtime reload rejected: revision={}, stage={}, field={}, error={}",
                    report.revision().value(),
                    fault.stage(),
                    fault.field().getOrNull(),
                    fault.error());
        }
        notifier.rejected(report);
    }

    /**
     * Closes Roster notification delivery owned by this reload service.
     */
    void close() {
        notifier.close();
        Logger.debug(false, "Auth", "Runtime reload service closed");
    }

}
