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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Dispatcher;
import org.miaixz.bus.auth.Roster;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.registry.CurrentRoster;
import org.miaixz.bus.auth.registry.SnapshotValidator;
import org.miaixz.bus.auth.registry.SourceValidator;
import org.miaixz.bus.auth.source.DriverDirectory;
import org.miaixz.bus.auth.source.SourceModule;
import org.miaixz.bus.auth.worker.RosterListener;
import org.miaixz.bus.auth.worker.loader.BlueprintLoader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Performs deterministic one-time assembly of the bus-auth Roster and runtime lifecycle.
 * <p>
 * The builder accepts only complete Source modules already composed by a root facade or an integrating project.
 * Connector callbacks occur before this runtime-neutral boundary and perform no network connection or runtime Roster
 * mutation. Build freezes the resulting indexes and validates driver and descriptor uniqueness. Normal build loads and
 * commits the project's first complete Blueprint snapshot before exposing the runtime. Empty startup is available only
 * through the explicitly named {@link #buildEmpty()} method. BlueprintLoader and RosterListener are direct assembly
 * inputs and never become protocol execution services.
 * </p>
 *
 * @author Kimi Liu
 */
public class RuntimeBuilder {

    /**
     * Formats the first structured, non-sensitive startup fault for an actionable build failure.
     *
     * @param report rejected initial Roster report containing at least one fault
     * @return safe startup rejection description
     */
    private static String rejection(final Roster.Report report) {
        final Roster.Fault fault = report.faults().getFirst();
        final String resource = fault.id().isPresent() ? fault.id().getOrNull() : "snapshot";
        final String field = fault.field().isPresent() ? fault.field().getOrNull() : "unknown";
        return "Initial authentication Blueprint snapshot was rejected: " + report.faults().size() + " fault(s); first="
                + fault.stage().name() + "/" + resource + "/" + field + ": " + fault.safeDescription();
    }

    /**
     * Externally supplied protocol execution service set.
     */
    private final RuntimeServices services;

    /**
     * External complete Blueprint snapshot loader.
     */
    private final BlueprintLoader blueprintLoader;

    /**
     * Explicit Source modules retained in caller-provided order.
     */
    private final List<SourceModule> modules;

    /**
     * Explicit Roster listeners retained in caller-provided order.
     */
    private final List<RosterListener> listeners;

    /**
     * Whether the one-shot build process has begun.
     */
    private boolean built;

    /**
     * Creates an empty private builder used only by the two named assembly factories.
     *
     * @param services        complete externally owned execution services
     * @param blueprintLoader project Blueprint input
     */
    public RuntimeBuilder(final RuntimeServices services, final BlueprintLoader blueprintLoader) {
        this.services = Assert.notNull(services, "Runtime execution services must not be null");
        this.blueprintLoader = Assert.notNull(blueprintLoader, "Blueprint loader must not be null");
        this.modules = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    /**
     * Creates an empty one-shot builder for an explicitly selected implementation set.
     *
     * @param services        complete externally owned execution services
     * @param blueprintLoader project Blueprint input
     * @return empty custom builder
     * @throws IllegalArgumentException if an argument is {@code null}
     */
    public static RuntimeBuilder custom(final RuntimeServices services, final BlueprintLoader blueprintLoader) {
        return new RuntimeBuilder(services, blueprintLoader);
    }

    /**
     * Adds one complete Source module before build.
     *
     * @param module Source module
     * @return this builder
     * @throws IllegalArgumentException if {@code module} is {@code null}
     * @throws ValidateException        if build has already begun
     */
    public synchronized RuntimeBuilder module(final SourceModule module) {
        mutable();
        modules.add(Assert.notNull(module, "Source module must not be null"));
        return this;
    }

    /**
     * Adds complete Source modules in caller-provided deterministic order.
     *
     * @param modules Source modules
     * @return this builder
     * @throws IllegalArgumentException if the collection or an entry is {@code null}
     * @throws ValidateException        if build has already begun
     */
    public synchronized RuntimeBuilder modules(final Collection<? extends SourceModule> modules) {
        mutable();
        Assert.notNull(modules, "Source module collection must not be null");
        for (SourceModule module : modules) {
            this.modules.add(Assert.notNull(module, "Source module must not be null"));
        }
        return this;
    }

    /**
     * Adds one externally implemented Roster commit listener before build.
     *
     * @param listener Roster listener
     * @return this builder
     * @throws IllegalArgumentException if {@code listener} is {@code null}
     * @throws ValidateException        if build has already begun
     */
    public synchronized RuntimeBuilder listener(final RosterListener listener) {
        mutable();
        listeners.add(Assert.notNull(listener, "Roster listener must not be null"));
        return this;
    }

    /**
     * Freezes Source modules, loads the initial project Blueprint, and exposes the runtime only after a successful
     * atomic commit.
     *
     * @param context immutable non-secret startup context
     * @param timeout shared end-to-end startup timeout
     * @return stage containing the fully initialized RuntimeManager
     * @throws ValidateException        if build was already attempted or module declarations conflict
     * @throws IllegalArgumentException if a driver or listener is invalid
     */
    public synchronized CompletionStage<RuntimeManager> build(final Context context, final Timeout timeout) {
        Assert.notNull(context, "Runtime startup context must not be null");
        Assert.notNull(timeout, "Runtime startup timeout must not be null");
        final RuntimeManager runtime = assemble();
        return runtime.reload(context, timeout).thenApply(report -> {
            if (!report.faults().isEmpty()) {
                runtime.close();
                throw new ValidateException(rejection(report));
            }
            return runtime;
        }).whenComplete((started, failure) -> {
            if (failure != null) {
                runtime.close();
            }
        });
    }

    /**
     * Assembles a running revision-zero runtime without loading a project Blueprint.
     * <p>
     * This entry is intentionally explicit for administrative processes that must construct the framework before any
     * Blueprint source is available. Authentication calls will find no Sources until a successful reload.
     * </p>
     *
     * @return fully assembled empty runtime
     */
    public synchronized RuntimeManager buildEmpty() {
        return assemble();
    }

    /**
     * Freezes Source modules and assembles the shared revision-zero runtime state.
     *
     * @return assembled runtime awaiting either an initial or later reload
     */
    private RuntimeManager assemble() {
        mutable();
        built = true;
        final DriverDirectory directory = new DriverDirectory(List.copyOf(modules));
        final RuntimeDescriptor descriptor = new RuntimeDescriptor(directory);
        final SnapshotCompiler snapshotCompiler = new SnapshotCompiler(directory, services);
        final Roster.Revision revision = new Roster.Revision(0L);
        final Roster.Snapshot snapshot = new Roster.Snapshot(revision, List.of());
        final RuntimeContainer initial = snapshotCompiler.compile(snapshot);
        final RuntimeContainer.Cell containers = new RuntimeContainer.Cell(initial);
        final RuntimeLifecycle lifecycle = new RuntimeLifecycle();
        final Roster roster = new CurrentRoster(() -> containers.current().roster());
        final Dispatcher dispatcher = new DefaultDispatcher(containers, lifecycle);
        final RosterNotifier notifier = new RosterNotifier(List.copyOf(listeners), services.executor());
        final RuntimeReloadService reloadService = new RuntimeReloadService(blueprintLoader,
                new SnapshotValidator(new SourceValidator(directory)), snapshotCompiler, containers, notifier,
                lifecycle);
        return new RuntimeManager(roster, dispatcher, reloadService, descriptor, lifecycle, containers);
    }

    /**
     * Rejects mutation after the one-shot build process has begun.
     *
     * @throws ValidateException if this builder is frozen
     */
    private void mutable() {
        if (built) {
            throw new ValidateException("Runtime builder is already frozen");
        }
    }

}
