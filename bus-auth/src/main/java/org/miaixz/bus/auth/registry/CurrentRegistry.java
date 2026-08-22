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
import java.util.function.Supplier;

import org.miaixz.bus.auth.Blueprint;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.core.lang.Assert;

/**
 * Implements the read-only public gateway to the currently committed Snapshot Registry.
 * <p>
 * This class exposes only the current snapshot, revision, and registration indexes. It never routes capabilities,
 * compiles registrations, performs security checks, emits audit events, or accesses persistence.
 * </p>
 *
 * @author Kimi Liu
 */
public class CurrentRegistry implements Registry {

    /**
     * Supplier of the fixed Registry retained by the current runtime container.
     */
    private final Supplier<Registry> current;

    /**
     * Creates a current Registry gateway over the runtime's published Snapshot Registry supplier.
     *
     * @param current supplier of the fixed Registry retained by the current runtime container
     * @throws IllegalArgumentException if the supplier is {@code null}
     */
    public CurrentRegistry(final Supplier<Registry> current) {
        this.current = Assert.notNull(current, "Current Registry supplier must not be null");
    }

    /**
     * Returns the complete snapshot from the current fixed Registry.
     *
     * @return current complete registration snapshot
     */
    @Override
    public Registry.Snapshot snapshot() {
        return current.get().snapshot();
    }

    /**
     * Returns the revision from the current fixed Registry.
     *
     * @return current committed revision
     */
    @Override
    public Registry.Revision revision() {
        return current.get().revision();
    }

    /**
     * Returns every configured Source owned by one Provider from a single current fixed Registry.
     *
     * @param providerId owning Provider identifier
     * @return immutable configured Source entries
     */
    @Override
    public List<Blueprint.SourceEntry> sources(final String providerId) {
        return current.get().sources(providerId);
    }

    /**
     * Returns enabled Sources owned by one Provider from a single current fixed Registry.
     *
     * @param providerId owning Provider identifier
     * @return immutable enabled Source entries
     */
    @Override
    public List<Blueprint.SourceEntry> enabledSources(final String providerId) {
        return current.get().enabledSources(providerId);
    }

}
