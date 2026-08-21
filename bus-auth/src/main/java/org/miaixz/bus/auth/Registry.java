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
package org.miaixz.bus.auth;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.registry.SnapshotFault;
import org.miaixz.bus.core.lang.Assert;

/**
 * Provides read-only access to the currently committed registration state.
 * <p>
 * The {@link #snapshot()} method exposes detached framework registration records. Implementations atomically replace
 * immutable Snapshot Registries and preserve the previous Registry when validation or compilation fails. Capability
 * execution belongs exclusively to {@link Dispatcher}.
 * </p>
 *
 * @author Kimi Liu
 */
public interface Registry {

    /**
     * Returns the complete registration snapshot associated with the currently committed Registry.
     * <p>
     * The snapshot owns an unmodifiable record list. Each record returns a detached framework entity copy.
     * </p>
     *
     * @return current immutable registration snapshot
     */
    Snapshot snapshot();

    /**
     * Returns the revision of the currently committed Registry.
     *
     * @return current Registry snapshot revision
     */
    Revision revision();

    /**
     * Returns every configured Source owned by one Provider in snapshot order.
     *
     * @param providerId owning Provider identifier
     * @return detached configured Source registrations, including disabled records
     */
    List<Registration.SourceEntry> sources(String providerId);

    /**
     * Returns enabled Source registrations owned by one Provider in snapshot order.
     *
     * @param providerId owning Provider identifier
     * @return detached enabled Source registrations
     */
    List<Registration.SourceEntry> enabledSources(String providerId);

    /**
     * Identifies the monotonically increasing version of a complete committed Registry snapshot.
     *
     * @param value non-negative snapshot revision
     * @author Kimi Liu
     */
    record Revision(long value) {

        /**
         * Creates a Registry snapshot revision.
         *
         * @param value non-negative snapshot revision
         * @throws IllegalArgumentException if the revision is negative
         */
        public Revision {
            Assert.isTrue(value >= 0, "Registry revision must not be negative");
        }

    }

    /**
     * Identifies one invocable Source registration without exposing its runtime implementation.
     *
     * @param kind invocable registration kind, restricted to Source
     * @param id   managed resource identifier
     * @author Kimi Liu
     */
    record Reference(Registration.Kind kind, String id) {

        /**
         * Creates an invocable Registry reference.
         *
         * @param kind Source registration kind
         * @param id   non-blank managed resource identifier
         * @throws IllegalArgumentException if the kind is not Source or the identifier is blank
         */
        public Reference {
            Assert.notNull(kind, "Registry reference kind must not be null");
            Assert.isTrue(kind == Registration.Kind.SOURCE, "Registry references only support Source registrations");
            Assert.notBlank(id, "Registry reference id must not be blank");
        }

        /**
         * Creates a reference to one client-side Source registration.
         *
         * @param id non-blank Source resource identifier
         * @return Source Registry reference
         */
        public static Reference source(final String id) {
            return new Reference(Registration.Kind.SOURCE, id);
        }

    }

    /**
     * Carries one complete immutable desired registration state loaded by an external project.
     *
     * @param revision version assigned to the complete snapshot
     * @param records  all Library, Provider, and Source records in loading order
     * @author Kimi Liu
     */
    record Snapshot(Revision revision, List<Registration.Entry> records) {

        /**
         * Creates a snapshot with a detached unmodifiable record list.
         *
         * @param revision snapshot revision
         * @param records  complete registration records
         * @throws IllegalArgumentException if a component or record is {@code null}
         */
        public Snapshot {
            Assert.notNull(revision, "Registry snapshot revision must not be null");
            Assert.notNull(records, "Registry snapshot records must not be null");
            final List<Registration.Entry> copy = new ArrayList<>(records.size());
            for (Registration.Entry record : records) {
                copy.add(Assert.notNull(record, "Registry snapshot entry must not be null"));
            }
            records = List.copyOf(copy);
        }

    }

    /**
     * Carries immutable faults for one attempted Registry snapshot.
     * <p>
     * A successful validation and commit is represented by an empty fault list for the committed revision.
     * </p>
     *
     * @param revision attempted snapshot revision
     * @param faults   ordered safe faults that identify their resource and processing stage
     * @author Kimi Liu
     */
    record Report(Revision revision, List<SnapshotFault> faults) {

        /**
         * Creates an immutable snapshot-processing report.
         *
         * @param revision attempted snapshot revision
         * @param faults   ordered Snapshot faults
         * @throws IllegalArgumentException if a component or fault is {@code null}
         */
        public Report {
            Assert.notNull(revision, "Registry report revision must not be null");
            Assert.notNull(faults, "Registry report faults must not be null");
            final List<SnapshotFault> copy = new ArrayList<>(faults.size());
            for (SnapshotFault fault : faults) {
                copy.add(Assert.notNull(fault, "Registry report fault must not be null"));
            }
            faults = List.copyOf(copy);
        }

    }

}
