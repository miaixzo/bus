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

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Dispatcher;
import org.miaixz.bus.auth.Roster;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.core.Lifecycle;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;

/**
 * Owns the bus-auth framework lifecycle and exposes separate Roster and execution entries.
 * <p>
 * RuntimeServices remain owned by the external project. Closing this runtime rejects new authentication and reload
 * operations, but preserves the last immutable Roster snapshot for read-only inspection. It never closes the caller's
 * executor, cache backend, loaders, audit sink, or consent service, and it does not alter the application-wide
 * {@link JsonKit} provider selection.
 * </p>
 *
 * @author Kimi Liu
 */
public class RuntimeManager implements Lifecycle, AutoCloseable {

    /**
     * Read-only gateway to the currently committed Blueprint state.
     */
    private final Roster roster;

    /**
     * Capability dispatch gateway, kept separate from Roster state access.
     */
    private final Dispatcher dispatcher;

    /**
     * Explicit complete-snapshot reload orchestrator.
     */
    private final RuntimeReloadService reloadService;

    /**
     * Immutable implementation description assembled for this runtime.
     */
    private final RuntimeDescriptor descriptor;

    /**
     * Atomic externally observable runtime lifecycle state.
     */
    private final RuntimeLifecycle lifecycle;

    /**
     * Atomic executable container cell whose workers are retired during close.
     */
    private final RuntimeContainer.Cell containers;

    /**
     * Creates a running runtime from completely assembled framework services.
     *
     * @param roster        initialized revision-zero Roster
     * @param dispatcher    capability dispatch gateway
     * @param reloadService complete-snapshot reload service
     * @param descriptor    implementation-neutral description of all assembled Source choices
     * @param lifecycle     shared runtime lifecycle gate
     * @param containers    current executable container cell
     * @throws IllegalArgumentException if a dependency is {@code null}
     */
    public RuntimeManager(final Roster roster, final Dispatcher dispatcher, final RuntimeReloadService reloadService,
            final RuntimeDescriptor descriptor, final RuntimeLifecycle lifecycle,
            final RuntimeContainer.Cell containers) {
        this.roster = Assert.notNull(roster, "Authentication Roster must not be null");
        this.dispatcher = Assert.notNull(dispatcher, "Capability dispatcher must not be null");
        this.reloadService = Assert.notNull(reloadService, "Runtime reload service must not be null");
        this.descriptor = Assert.notNull(descriptor, "Runtime descriptor must not be null");
        this.lifecycle = Assert.notNull(lifecycle, "Runtime lifecycle must not be null");
        this.containers = Assert.notNull(containers, "Runtime containers must not be null");
    }

    /**
     * Returns the read-only Blueprint state gateway.
     *
     * @return runtime Roster
     */
    public Roster roster() {
        return roster;
    }

    /**
     * Returns the capability dispatch gateway.
     *
     * @return runtime capability dispatcher
     */
    public Dispatcher dispatcher() {
        return dispatcher;
    }

    /**
     * Returns the implementation description selected at runtime assembly.
     *
     * @return immutable runtime descriptor
     */
    public RuntimeDescriptor descriptor() {
        return descriptor;
    }

    /**
     * Loads, validates, compiles, and atomically publishes one complete external Blueprint snapshot.
     * <p>
     * Reload is an explicit security boundary, not a passive Roster refresh. On a successful commit, the strictly
     * increasing snapshot revision becomes the cache generation of every compiled Source. New invocations can no longer
     * redeem or validate authorization codes, device codes, access tokens, refresh tokens, framework protocol sessions,
     * callback states, nonces, authorization lifecycle entries, or ID Token logout bindings created by an older
     * revision. Old backend entries are left unreachable until their existing TTL expires; reload never performs a
     * distributed key scan or bulk deletion.
     * </p>
     * <p>
     * Failed loading, validation, compilation, timeout, lifecycle, or concurrent-commit attempts leave both the active
     * runtime and its generation unchanged. Replay markers deliberately remain shared across revisions until their TTL
     * expires, so reload cannot reopen a replay window. Project-owned web or business sessions are not bulk-deleted;
     * only framework protocol Session state is generation-bound.
     * </p>
     *
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation timeout
     * @return stage containing the validation or commit report for the attempted snapshot
     * @throws IllegalArgumentException if an argument is {@code null}
     */
    public CompletionStage<Roster.Report> reload(final Context context, final Timeout timeout) {
        Assert.notNull(context, "Runtime reload context must not be null");
        Assert.notNull(timeout, "Runtime reload timeout must not be null");
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
     * Roster remains readable and continues to expose the last successfully committed immutable snapshot.
     */
    @Override
    public void close() {
        Logger.info(
                true,
                "Auth",
                "Authentication runtime close started: revision={}, state={}",
                roster.revision().value(),
                lifecycle.state());
        lifecycle.close();
        containers.current().retire();
        reloadService.close();
        Logger.info(
                false,
                "Auth",
                "Authentication runtime close completed: revision={}, state={}",
                roster.revision().value(),
                lifecycle.state());
    }

}
