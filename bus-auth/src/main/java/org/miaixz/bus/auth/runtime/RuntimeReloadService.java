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
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.registry.SnapshotFault;
import org.miaixz.bus.auth.registry.SnapshotValidator;
import org.miaixz.bus.auth.worker.RegistrationLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;

/**
 * Loads, validates, compiles, and atomically publishes complete external registration snapshots.
 * <p>
 * Every call captures one expected runtime container before loading. A candidate becomes visible only through a
 * successful compare-and-set replacement, so validation, compilation, timeout, revision, and concurrency failures
 * preserve the complete previous view. Listener failures are isolated after the commit or validation decision.
 * </p>
 *
 * @author Kimi Liu
 */
final class RuntimeReloadService {

    /**
     * External complete registration batch loader.
     */
    private final RegistrationLoader loader;

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
     * Immutable Registry listener list in registration order.
     */
    private final RegistryNotifier notifier;

    /**
     * Single lifecycle gate shared by the complete runtime.
     */
    private final RuntimeLifecycle lifecycle;

    /**
     * Creates the single complete-snapshot reload orchestrator.
     *
     * @param loader     external complete registration batch loader
     * @param validator  candidate Snapshot validator
     * @param compiler   validated snapshot compiler
     * @param containers atomic committed-container cell
     * @param notifier   ordered Registry observation dispatcher
     * @param lifecycle  shared runtime lifecycle gate
     * @throws IllegalArgumentException if a dependency, list, or listener is {@code null}
     */
    RuntimeReloadService(final RegistrationLoader loader, final SnapshotValidator validator,
            final SnapshotCompiler compiler, final RuntimeContainer.Cell containers, final RegistryNotifier notifier,
            final RuntimeLifecycle lifecycle) {
        this.loader = Assert.notNull(loader, "Runtime reload loader must not be null");
        this.validator = Assert.notNull(validator, "Runtime reload validator must not be null");
        this.compiler = Assert.notNull(compiler, "Runtime reload compiler must not be null");
        this.containers = Assert.notNull(containers, "Runtime reload containers must not be null");
        this.notifier = Assert.notNull(notifier, "Runtime reload Registry notifier must not be null");
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
    private static Registry.Report report(
            final long revision,
            final SnapshotFault.Stage stage,
            final String field,
            final Errors error,
            final String description) {
        return new Registry.Report(new Registry.Revision(Math.max(0L, revision)),
                List.of(SnapshotFault.snapshot(stage, Optional.ofNullable(field), error, description)));
    }

    /**
     * Attempts to publish one complete desired registration snapshot.
     *
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing the candidate validation report after rejection or successful commit
     * @throws IllegalArgumentException if an argument is {@code null}
     */
    public CompletionStage<Registry.Report> reload(final Context context, final Timeout.Budget timeout) {
        Assert.notNull(context, "Runtime reload context must not be null");
        Assert.notNull(timeout, "Runtime reload budget must not be null");
        final RuntimeLifecycle.Lease lease = lifecycle.enter();
        if (lease == null) {
            return CompletableFuture.completedFuture(
                    report(
                            containers.current().registry().revision().value(),
                            SnapshotFault.Stage.COMMIT,
                            "lifecycle",
                            ErrorCode._503,
                            "Authentication runtime is not running"));
        }
        if (timeout.expired()) {
            lease.close();
            return reject(
                    report(
                            containers.current().registry().revision().value(),
                            SnapshotFault.Stage.LOAD,
                            "timeout",
                            ErrorCode._408,
                            "Runtime reload time budget has expired"));
        }
        final RuntimeContainer expected = containers.current();
        final CompletionStage<RegistrationLoader.Batch> loading;
        try {
            loading = loader.load(context, timeout);
        } catch (RuntimeException cause) {
            final Registry.Report report = report(
                    expected.registry().revision().value() + 1L,
                    SnapshotFault.Stage.LOAD,
                    "loader",
                    ErrorCode._500,
                    "Registration loader failed before returning a stage");
            rejected(report);
            lease.close();
            return CompletableFuture.completedFuture(report);
        }
        if (loading == null) {
            final Registry.Report report = report(
                    expected.registry().revision().value() + 1L,
                    SnapshotFault.Stage.LOAD,
                    "loader",
                    ErrorCode._500,
                    "Registration loader returned no stage");
            rejected(report);
            lease.close();
            return CompletableFuture.completedFuture(report);
        }
        return loading
                .handle(
                        (batch, cause) -> cause == null ? process(batch, expected, timeout)
                                : reject(
                                        report(
                                                expected.registry().revision().value() + 1L,
                                                SnapshotFault.Stage.LOAD,
                                                "loader",
                                                ErrorCode._500,
                                                "Registration loader stage failed")))
                .thenCompose(Function.identity()).whenComplete((ignored, cause) -> lease.close());
    }

    /**
     * Validates, compiles, and atomically commits one loaded snapshot.
     *
     * @param batch    externally loaded complete candidate batch
     * @param expected runtime container captured before loading
     * @param timeout  shared operation budget
     * @return stage containing validation or successful commit report
     */
    private CompletionStage<Registry.Report> process(
            final RegistrationLoader.Batch batch,
            final RuntimeContainer expected,
            final Timeout.Budget timeout) {
        if (batch == null) {
            return reject(
                    report(
                            expected.registry().revision().value() + 1L,
                            SnapshotFault.Stage.LOAD,
                            "batch",
                            ErrorCode._500,
                            "Registration loader returned no batch"));
        }
        final Registry.Snapshot snapshot = new Registry.Snapshot(new Registry.Revision(batch.revision()),
                batch.registrations());
        if (snapshot.revision().value() <= expected.registry().revision().value()) {
            return reject(
                    report(
                            snapshot.revision().value(),
                            SnapshotFault.Stage.VALIDATE,
                            "revision",
                            ErrorCode._409,
                            "Registry snapshot revision must increase monotonically"));
        }
        if (timeout.expired()) {
            return reject(
                    report(
                            snapshot.revision().value(),
                            SnapshotFault.Stage.VALIDATE,
                            "timeout",
                            ErrorCode._408,
                            "Runtime reload time budget expired after loading"));
        }
        final Registry.Report report;
        try {
            report = validator.validate(snapshot);
        } catch (RuntimeException cause) {
            return reject(
                    report(
                            snapshot.revision().value(),
                            SnapshotFault.Stage.VALIDATE,
                            "validator",
                            ErrorCode._500,
                            "Registry snapshot validation failed operationally"));
        }
        if (!report.faults().isEmpty()) {
            rejected(report);
            return CompletableFuture.completedFuture(report);
        }
        final RuntimeContainer replacement;
        try {
            replacement = compiler.compile(snapshot);
        } catch (SnapshotCompiler.CompilationFailure failure) {
            return reject(new Registry.Report(snapshot.revision(), List.of(failure.fault())));
        } catch (RuntimeException cause) {
            return reject(
                    report(
                            snapshot.revision().value(),
                            SnapshotFault.Stage.COMPILE,
                            "snapshot",
                            ErrorCode._500,
                            "Registry snapshot compilation failed"));
        }
        if (timeout.expired()) {
            replacement.retire();
            return reject(
                    report(
                            snapshot.revision().value(),
                            SnapshotFault.Stage.COMMIT,
                            "timeout",
                            ErrorCode._408,
                            "Runtime reload time budget expired before commit"));
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
                            SnapshotFault.Stage.COMMIT,
                            "lifecycle",
                            ErrorCode._503,
                            "Authentication runtime closed before commit"));
        }
        if (!replaced.get()) {
            replacement.retire();
            return reject(
                    report(
                            snapshot.revision().value(),
                            SnapshotFault.Stage.COMMIT,
                            "revision",
                            ErrorCode._409,
                            "Concurrent Registry reload prevented atomic commit"));
        }
        expected.retire();
        notifier.dispatch();
        return CompletableFuture.completedFuture(report);
    }

    /**
     * Returns a completed report after scheduling rejection observation.
     *
     * @param report rejection report
     * @return completed report stage
     */
    private CompletionStage<Registry.Report> reject(final Registry.Report report) {
        rejected(report);
        return CompletableFuture.completedFuture(report);
    }

    /**
     * Schedules one rejected report for isolated listener delivery.
     *
     * @param report rejection report
     */
    private void rejected(final Registry.Report report) {
        notifier.rejected(report);
    }

    /** Closes Registry notification delivery owned by this reload service. */
    void close() {
        notifier.close();
    }

}
