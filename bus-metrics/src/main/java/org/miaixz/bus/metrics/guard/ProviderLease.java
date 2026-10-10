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
package org.miaixz.bus.metrics.guard;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.metrics.Provider;

/**
 * Idempotent ownership token for a temporary installation in the static Metrics facade.
 * <p>
 * Closing a lease only attempts to restore the previous facade state; it never closes either provider instance.
 *
 * @author Kimi Liu
 */
public final class ProviderLease implements AutoCloseable {

    /**
     * Identity token of the installation owner.
     */
    private final Object owner;
    /**
     * Provider installed by the owner.
     */
    private final Provider provider;
    /**
     * Opaque facade state retained until release.
     */
    private final Object previousState;
    /**
     * Compare-and-restore action supplied by the facade.
     */
    private final Runnable releaser;
    /**
     * Idempotent close state.
     */
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * Creates a lease. This constructor is intended for the Metrics facade state machine.
     *
     * @param owner         owner identity token
     * @param provider      installed provider
     * @param previousState opaque state to retain until release
     * @param releaser      compare-and-restore action
     */
    public ProviderLease(Object owner, Provider provider, Object previousState, Runnable releaser) {
        this.owner = Objects.requireNonNull(owner, "Provider owner must not be null");
        this.provider = Objects.requireNonNull(provider, "Provider must not be null");
        this.previousState = Objects.requireNonNull(previousState, "Previous provider state must not be null");
        this.releaser = Objects.requireNonNull(releaser, "Provider releaser must not be null");
    }

    /**
     * Returns the installed provider.
     *
     * @return provider instance
     */
    public Provider provider() {
        return provider;
    }

    /**
     * Checks owner and provider by object identity.
     *
     * @param candidateOwner    owner token
     * @param candidateProvider provider instance
     * @return whether both identities match this lease
     */
    public boolean matches(Object candidateOwner, Provider candidateProvider) {
        return owner == candidateOwner && provider == candidateProvider;
    }

    /**
     * Returns whether this lease has already been closed.
     *
     * @return close state
     */
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            releaser.run();
        }
    }

}
