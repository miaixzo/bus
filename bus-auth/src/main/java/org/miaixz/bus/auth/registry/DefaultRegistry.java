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
package org.miaixz.bus.auth.registry;

import java.util.List;

import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.core.lang.Assert;

/**
 * Implements the read-only public view of committed registration state.
 * <p>
 * This class exposes only the current snapshot, revision, and registration indexes. It never routes capabilities,
 * compiles registrations, performs security checks, emits audit events, or accesses persistence.
 * </p>
 *
 * @author Kimi Liu
 */
public final class DefaultRegistry implements Registry {

    /**
     * Atomically published immutable Registry state shared with the reload service.
     */
    private final AtomicRegistryState registryState;

    /**
     * Creates a running Registry over an already complete initial view.
     *
     * @param registryState atomic state initialized by runtime assembly
     * @throws IllegalArgumentException if the state holder is {@code null}
     */
    public DefaultRegistry(final AtomicRegistryState registryState) {
        this.registryState = Assert.notNull(registryState, "Atomic Registry state must not be null");
    }

    /**
     * Returns the complete snapshot from the current immutable view.
     *
     * @return current complete registration snapshot
     */
    @Override
    public Registry.Snapshot snapshot() {
        return registryState.current().snapshot();
    }

    /**
     * Returns the revision from the current immutable view.
     *
     * @return current committed revision
     */
    @Override
    public Registry.Revision revision() {
        return registryState.current().revision();
    }

    /**
     * Returns every configured Source owned by one Provider from a single current immutable view.
     *
     * @param providerId owning Provider identifier
     * @return immutable configured Source entries
     */
    @Override
    public List<Registration.SourceEntry> sources(final String providerId) {
        return registryState.current().sources(providerId);
    }

    /**
     * Returns enabled Sources owned by one Provider from a single current immutable view.
     *
     * @param providerId owning Provider identifier
     * @return immutable enabled Source entries
     */
    @Override
    public List<Registration.SourceEntry> enabledSources(final String providerId) {
        return registryState.current().enabledSources(providerId);
    }

}
