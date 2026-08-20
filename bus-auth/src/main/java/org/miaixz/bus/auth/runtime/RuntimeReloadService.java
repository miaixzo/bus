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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.registry.RegistrationLoader;
import org.miaixz.bus.auth.registry.RegistrationValidator;
import org.miaixz.bus.auth.registry.RegistryListener;
import org.miaixz.bus.auth.registry.internal.AtomicRegistryState;
import org.miaixz.bus.auth.registry.spi.RegistryView;
import org.miaixz.bus.auth.runtime.internal.SnapshotCompiler;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Loads, validates, compiles, and atomically publishes complete external registration snapshots.
 * <p>
 * Every call captures one expected immutable view before loading. A candidate becomes visible only through a successful
 * compare-and-set replacement, so validation, compilation, timeout, revision, and concurrency failures preserve the
 * complete previous view. Listener failures are isolated after the commit or validation decision.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RuntimeReloadService {

    /**
     * External complete registration snapshot loader.
     */
    private final RegistrationLoader loader;

    /**
     * Framework raw registration validator.
     */
    private final RegistrationValidator validator;

    /**
     * Pure compiler for already validated complete snapshots.
     */
    private final SnapshotCompiler compiler;

    /**
     * Atomic holder of the currently committed immutable Registry view.
     */
    private final AtomicRegistryState registryState;

    /**
     * Immutable Registry listener list in registration order.
     */
    private final List<RegistryListener> listeners;

    /**
     * Creates the single complete-snapshot reload orchestrator.
     *
     * @param loader        external complete snapshot loader
     * @param validator     raw registration validator
     * @param compiler      validated snapshot compiler
     * @param registryState atomic committed-view holder
     * @param listeners     Registry listeners in notification order
     * @throws IllegalArgumentException if a dependency, list, or listener is {@code null}
     */
    public RuntimeReloadService(final RegistrationLoader loader, final RegistrationValidator validator,
            final SnapshotCompiler compiler, final AtomicRegistryState registryState,
            final List<RegistryListener> listeners) {
        this.loader = Assert.notNull(loader, "Runtime reload loader must not be null");
        this.validator = Assert.notNull(validator, "Runtime reload validator must not be null");
        this.compiler = Assert.notNull(compiler, "Runtime reload compiler must not be null");
        this.registryState = Assert.notNull(registryState, "Runtime reload Registry state must not be null");
        Assert.notNull(listeners, "Runtime reload listener list must not be null");
        final List<RegistryListener> copy = new ArrayList<>(listeners.size());
        for (RegistryListener listener : listeners) {
            copy.add(Assert.notNull(listener, "Runtime reload listener must not be null"));
        }
        this.listeners = List.copyOf(copy);
    }

    /**
     * Creates an exceptionally completed stage using the shared Bus validation exception.
     *
     * @param message safe non-sensitive failure description
     * @param <T>     stage result type
     * @return failed stage
     */
    private static <T> CompletionStage<T> failed(final String message) {
        return CompletableFuture.failedFuture(new ValidateException(message));
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
        if (timeout.expired()) {
            return failed("Runtime reload time budget has expired");
        }
        final RegistryView expected = registryState.current();
        final CompletionStage<Registry.Snapshot> loading;
        try {
            loading = loader.load(context, timeout);
        } catch (RuntimeException cause) {
            return failed("Registration loader failed before returning a stage");
        }
        if (loading == null) {
            return failed("Registration loader returned no stage");
        }
        return loading
                .handle(
                        (snapshot, cause) -> cause == null ? process(snapshot, expected, timeout)
                                : RuntimeReloadService.<Registry.Report>failed("Registration loader stage failed"))
                .thenCompose(Function.identity());
    }

    /**
     * Validates, compiles, and atomically commits one loaded snapshot.
     *
     * @param snapshot loaded complete candidate snapshot
     * @param expected Registry view captured before loading
     * @param timeout  shared operation budget
     * @return stage containing validation or successful commit report
     */
    private CompletionStage<Registry.Report> process(
            final Registry.Snapshot snapshot,
            final RegistryView expected,
            final Timeout.Budget timeout) {
        if (snapshot == null) {
            return failed("Registration loader returned no snapshot");
        }
        if (snapshot.revision().value() <= expected.revision().value()) {
            return failed("Registry snapshot revision must increase monotonically");
        }
        if (timeout.expired()) {
            return failed("Runtime reload time budget expired after loading");
        }
        final Registry.Report report;
        try {
            report = validator.validate(snapshot);
        } catch (RuntimeException cause) {
            return failed("Registry snapshot validation failed operationally");
        }
        if (!report.issues().isEmpty()) {
            rejected(report);
            return CompletableFuture.completedFuture(report);
        }
        final RegistryView replacement;
        try {
            replacement = compiler.compile(snapshot);
        } catch (RuntimeException cause) {
            return failed("Registry snapshot compilation failed");
        }
        if (timeout.expired()) {
            return failed("Runtime reload time budget expired before commit");
        }
        if (!registryState.replace(expected, replacement)) {
            return failed("Concurrent Registry reload prevented atomic commit");
        }
        committed(snapshot.revision());
        return CompletableFuture.completedFuture(report);
    }

    /**
     * Notifies listeners of a committed revision while isolating listener failures.
     *
     * @param revision committed Registry revision
     */
    private void committed(final Registry.Revision revision) {
        for (RegistryListener listener : listeners) {
            try {
                listener.committed(revision);
            } catch (RuntimeException ignored) {
                // Listener observations cannot roll back or invalidate an already committed immutable view.
            }
        }
    }

    /**
     * Notifies listeners of a validation-rejected snapshot while isolating listener failures.
     *
     * @param report safe validation report
     */
    private void rejected(final Registry.Report report) {
        for (RegistryListener listener : listeners) {
            try {
                listener.rejected(report);
            } catch (RuntimeException ignored) {
                // Listener observations cannot change the rejected snapshot decision.
            }
        }
    }

}
