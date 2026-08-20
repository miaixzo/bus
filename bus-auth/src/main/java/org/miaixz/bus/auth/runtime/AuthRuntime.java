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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.core.Lifecycle;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Owns the bus-auth framework lifecycle and exposes its Registry and explicit snapshot reload entry.
 * <p>
 * ExecutionServices remain owned by the external project. Closing this runtime stops the framework Registry only and
 * never closes the caller's executor, Fabric context, stores, resolvers, JSON provider, audit sink, or consent service.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AuthRuntime implements Lifecycle, AutoCloseable {

    /**
     * Only public gateway to compiled Source capabilities.
     */
    private final Registry registry;

    /**
     * Explicit complete-snapshot reload orchestrator.
     */
    private final RuntimeReloadService reloadService;

    /**
     * Atomic externally observable runtime lifecycle state.
     */
    private final AtomicReference<Lifecycle.State> lifecycle;

    /**
     * Creates a running runtime from completely assembled framework services.
     *
     * @param registry      initialized revision-zero Registry
     * @param reloadService complete-snapshot reload service
     * @throws IllegalArgumentException if a dependency is {@code null}
     */
    AuthRuntime(final Registry registry, final RuntimeReloadService reloadService) {
        this.registry = Assert.notNull(registry, "Authentication Registry must not be null");
        this.reloadService = Assert.notNull(reloadService, "Runtime reload service must not be null");
        this.lifecycle = new AtomicReference<>(Lifecycle.State.RUNNING);
    }

    /**
     * Returns the only public execution gateway for compiled authentication capabilities.
     *
     * @return runtime Registry
     */
    public Registry registry() {
        return registry;
    }

    /**
     * Loads, validates, compiles, and atomically publishes one complete external registration snapshot.
     *
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing the validation or commit report for the attempted snapshot
     * @throws IllegalArgumentException if an argument is {@code null}
     */
    public CompletionStage<Registry.Report> reload(final Context context, final Timeout.Budget timeout) {
        Assert.notNull(context, "Runtime reload context must not be null");
        Assert.notNull(timeout, "Runtime reload budget must not be null");
        if (lifecycle.get() != Lifecycle.State.RUNNING) {
            return CompletableFuture.failedFuture(new ValidateException("Authentication runtime is not running"));
        }
        return reloadService.reload(context, timeout);
    }

    /**
     * Returns the current externally observable runtime lifecycle state.
     *
     * @return RUNNING before close, CLOSING during close, or CLOSED afterward
     */
    @Override
    public Lifecycle.State state() {
        return lifecycle.get();
    }

    /**
     * Idempotently closes the Registry without closing any externally owned ExecutionServices dependency.
     */
    @Override
    public void close() {
        if (!lifecycle.compareAndSet(Lifecycle.State.RUNNING, Lifecycle.State.CLOSING)) {
            return;
        }
        try {
            registry.close();
        } catch (RuntimeException ignored) {
            // The lifecycle boundary must not transfer a Registry close failure to caller-owned resource management.
        } finally {
            lifecycle.set(Lifecycle.State.CLOSED);
        }
    }

}
