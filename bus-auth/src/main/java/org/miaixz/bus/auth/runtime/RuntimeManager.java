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

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Authenticator;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.registry.AtomicRegistryState;
import org.miaixz.bus.core.Lifecycle;
import org.miaixz.bus.core.lang.Assert;

/**
 * Owns the bus-auth framework lifecycle and exposes separate registration and execution entries.
 * <p>
 * RuntimeServices remain owned by the external project. Closing this runtime rejects new authentication and reload
 * operations, but preserves the last immutable Registry snapshot for read-only inspection. It never closes the caller's
 * executor, Fabric context, cache backend, loaders, JSON provider, audit sink, or consent service.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RuntimeManager implements Lifecycle, AutoCloseable {

    /**
     * Only public gateway to compiled Source capabilities.
     */
    private final Registry registry;

    /**
     * Capability execution gateway, kept separate from registration state access.
     */
    private final Authenticator authenticator;

    /**
     * Explicit complete-snapshot reload orchestrator.
     */
    private final RuntimeReloadService reloadService;

    /**
     * Immutable implementation inventory assembled for this runtime.
     */
    private final RuntimeDescriptor descriptor;

    /**
     * Atomic externally observable runtime lifecycle state.
     */
    private final RuntimeLifecycle lifecycle;

    private final AtomicRegistryState registryState;

    /**
     * Creates a running runtime from completely assembled framework services.
     *
     * @param registry      initialized revision-zero Registry
     * @param authenticator capability execution gateway
     * @param reloadService complete-snapshot reload service
     * @param descriptor    frozen description of the assembled schemes and Vendors
     * @throws IllegalArgumentException if a dependency is {@code null}
     */
    RuntimeManager(final Registry registry, final Authenticator authenticator, final RuntimeReloadService reloadService,
            final RuntimeDescriptor descriptor, final RuntimeLifecycle lifecycle, final AtomicRegistryState registryState) {
        this.registry = Assert.notNull(registry, "Authentication Registry must not be null");
        this.authenticator = Assert.notNull(authenticator, "Authentication executor must not be null");
        this.reloadService = Assert.notNull(reloadService, "Runtime reload service must not be null");
        this.descriptor = Assert.notNull(descriptor, "Runtime descriptor must not be null");
        this.lifecycle = Assert.notNull(lifecycle, "Runtime lifecycle must not be null");
        this.registryState = Assert.notNull(registryState, "Runtime Registry state must not be null");
    }

    /**
     * Returns the read-only registration state gateway.
     *
     * @return runtime Registry
     */
    public Registry registry() {
        return registry;
    }

    /**
     * Returns the capability execution gateway.
     *
     * @return runtime authenticator
     */
    public Authenticator authenticator() {
        return authenticator;
    }

    /**
     * Returns the implementation inventory selected at runtime assembly.
     *
     * @return immutable runtime descriptor
     */
    public RuntimeDescriptor descriptor() {
        return descriptor;
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
        return reloadService.reload(context, timeout);
    }

    /**
     * Returns the current externally observable runtime lifecycle state.
     *
     * @return RUNNING before close, CLOSING during close, or CLOSED afterward
     */
    @Override
    public Lifecycle.State state() {
        return lifecycle.state();
    }

    /**
     * Idempotently rejects new authentication and reload operations without closing externally owned dependencies. The
     * Registry remains readable and continues to expose the last successfully committed immutable snapshot.
     */
    @Override
    public void close() {
        lifecycle.close();
        registryState.current().retire();
        reloadService.close();
    }

}
