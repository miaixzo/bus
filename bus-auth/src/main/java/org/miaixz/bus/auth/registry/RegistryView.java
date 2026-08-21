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
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.core.lang.Optional;

/**
 * Exposes one immutable and revision-consistent Registry view.
 * <p>
 * Runtime publication, Registry queries, and Authenticator routing share this view so every operation observes one
 * complete generation. The view does not mutate registrations or execute capabilities.
 * </p>
 *
 * @author Kimi Liu
 */
public interface RegistryView {

    /**
     * Acquires this exact compiled generation for one operation.
     *
     * @return generation lease, or {@code null} after this view has been retired
     */
    Lease acquire();

    /**
     * Prevents new leases and closes generation-owned workers after existing leases finish.
     */
    void retire();

    /**
     * Returns the revision shared by all indexes and the source snapshot.
     *
     * @return immutable view revision
     */
    Registry.Revision revision();

    /**
     * Returns the complete source snapshot from which this view was compiled.
     *
     * @return complete registration snapshot with an unmodifiable record list
     */
    Registry.Snapshot snapshot();

    /**
     * Returns every Source entry owned by one Provider in snapshot order.
     *
     * @param providerId Provider identifier
     * @return immutable Source entry list
     */
    List<Registration.SourceEntry> sources(String providerId);

    /**
     * Returns enabled Source entries owned by one Provider in snapshot order.
     *
     * @param providerId Provider identifier
     * @return immutable enabled Source entry list
     */
    List<Registration.SourceEntry> enabledSources(String providerId);

    /**
     * Looks up one compiled Source worker by its kind-safe Registry reference.
     *
     * @param reference Source Registry reference
     * @return Source worker when the enabled record compiled in this revision
     */
    Optional<SourceWorker> worker(Registry.Reference reference);

    /**
     * Represents one invocation holding the exact Registry generation from lookup through asynchronous completion.
     */
    interface Lease extends AutoCloseable {

        /**
         * Returns the immutable view held by this lease.
         *
         * @return leased Registry view
         */
        RegistryView view();

        /** Releases this generation lease exactly once. */
        @Override
        void close();
    }

}
