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

import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;

/**
 * Provides read-only access to the currently committed Library, Provider, and Source Blueprint roster.
 * <p>
 * The {@link #snapshot()} method exposes detached framework Blueprint entries. Implementations atomically replace
 * immutable roster snapshots and preserve the previously committed Roster when validation or compilation fails.
 * Capability execution belongs exclusively to {@link Dispatcher}; this contract never registers, compiles, persists,
 * synchronizes, or executes a Blueprint entry.
 * </p>
 *
 * @author Kimi Liu
 */
public interface Roster {

    /**
     * Returns the complete Blueprint snapshot associated with the currently committed Roster.
     * <p>
     * The snapshot owns an unmodifiable record list. Each record returns a detached framework entity copy.
     * </p>
     *
     * @return current immutable Blueprint snapshot
     */
    Snapshot snapshot();

    /**
     * Returns the revision of the currently committed Roster.
     *
     * @return current Roster snapshot revision
     */
    Revision revision();

    /**
     * Returns every configured Source owned by one Provider in snapshot order.
     *
     * @param providerId owning Provider identifier
     * @return detached configured Source entries, including disabled entries
     */
    List<Blueprint.SourceEntry> sources(String providerId);

    /**
     * Returns enabled Source entries owned by one Provider in snapshot order.
     *
     * @param providerId owning Provider identifier
     * @return detached enabled Source entries
     */
    List<Blueprint.SourceEntry> enabledSources(String providerId);

    /**
     * Identifies the monotonically increasing version of a complete committed Roster snapshot.
     *
     * @param value non-negative snapshot revision
     * @author Kimi Liu
     */
    record Revision(long value) {

        /**
         * Creates a Roster snapshot revision.
         *
         * @param value non-negative snapshot revision
         * @throws IllegalArgumentException if the revision is negative
         */
        public Revision {
            Assert.isTrue(value >= 0, "Roster revision must not be negative");
        }

    }

    /**
     * Identifies one invocable Source Blueprint entry without exposing its runtime implementation.
     *
     * @param kind invocable Blueprint kind, restricted to Source
     * @param id   managed resource identifier
     * @author Kimi Liu
     */
    record Reference(Blueprint.Kind kind, String id) {

        /**
         * Creates an invocable Roster reference.
         *
         * @param kind Source Blueprint kind
         * @param id   non-blank managed resource identifier
         * @throws IllegalArgumentException if the kind is not Source or the identifier is blank
         */
        public Reference {
            Assert.notNull(kind, "Roster reference kind must not be null");
            Assert.isTrue(kind == Blueprint.Kind.SOURCE, "Roster references only support Source Blueprint entries");
            Assert.notBlank(id, "Roster reference id must not be blank");
        }

        /**
         * Creates a reference to one client-side Source Blueprint entry.
         *
         * @param id non-blank Source resource identifier
         * @return Source Roster reference
         */
        public static Reference source(final String id) {
            return new Reference(Blueprint.Kind.SOURCE, id);
        }

    }

    /**
     * Carries one complete immutable desired Blueprint state loaded by an external project.
     *
     * @param revision version assigned to the complete snapshot
     * @param entries  all Library, Provider, and Source Blueprint entries in loading order
     * @author Kimi Liu
     */
    record Snapshot(Revision revision, List<Blueprint.Entry> entries) {

        /**
         * Creates a snapshot with a detached unmodifiable entry list.
         *
         * @param revision snapshot revision
         * @param entries  complete Blueprint entries
         * @throws IllegalArgumentException if a component or entry is {@code null}
         */
        public Snapshot {
            Assert.notNull(revision, "Roster snapshot revision must not be null");
            Assert.notNull(entries, "Roster snapshot entries must not be null");
            final List<Blueprint.Entry> copy = new ArrayList<>(entries.size());
            for (Blueprint.Entry entry : entries) {
                copy.add(Assert.notNull(entry, "Roster snapshot entry must not be null"));
            }
            entries = List.copyOf(copy);
        }

    }

    /**
     * Describes one safe, resource-addressable fault that rejected a Roster snapshot.
     * <p>
     * The value uses shared Bus errors and contains no raw options, credentials, tokens, private payloads, exception
     * stack, or implementation class names. It is diagnostic data and does not define a protocol error response.
     * </p>
     *
     * @param kind            optional kind of the Blueprint entry that failed
     * @param id              optional resource identifier of the failing entry
     * @param stage           snapshot processing stage that detected the issue
     * @param standard        optional formal standard reference relevant to the issue
     * @param field           optional safe Blueprint or standard field name
     * @param error           shared Bus error code
     * @param safeDescription non-sensitive diagnostic description
     * @author Kimi Liu
     */
    record Fault(Optional<Blueprint.Kind> kind, Optional<String> id, Stage stage, Optional<String> standard,
            Optional<String> field, Errors error, String safeDescription) {

        /**
         * Creates an immutable safe Roster fault.
         *
         * @throws IllegalArgumentException if a required value is missing or optional text is blank
         */
        public Fault {
            Assert.notNull(kind, "Roster fault kind container must not be null");
            kind = Optional.ofNullable(kind.getOrNull());
            Assert.notNull(id, "Roster fault resource id container must not be null");
            if (!id.isEmpty()) {
                Assert.notBlank(id.getOrNull(), "Roster fault resource id must not be blank");
            }
            id = Optional.ofNullable(id.getOrNull());
            Assert.notNull(stage, "Roster fault stage must not be null");
            Assert.notNull(standard, "Roster fault standard container must not be null");
            if (!standard.isEmpty()) {
                Assert.notBlank(standard.getOrNull(), "Roster fault standard must not be blank");
            }
            standard = Optional.ofNullable(standard.getOrNull());
            Assert.notNull(field, "Roster fault field container must not be null");
            if (!field.isEmpty()) {
                Assert.notBlank(field.getOrNull(), "Roster fault field must not be blank");
            }
            field = Optional.ofNullable(field.getOrNull());
            Assert.notNull(error, "Roster fault Bus error must not be null");
            Assert.notBlank(safeDescription, "Roster fault safe description must not be blank");
        }

        /**
         * Creates a fault associated with one exact Blueprint entry.
         *
         * @param kind        Blueprint kind
         * @param id          Blueprint entry identifier
         * @param stage       processing stage
         * @param field       optional failing field
         * @param error       stable error classification
         * @param description safe description
         * @return structured entry fault
         */
        public static Fault entry(
                final Blueprint.Kind kind,
                final String id,
                final Stage stage,
                final Optional<String> field,
                final Errors error,
                final String description) {
            return new Fault(Optional.of(kind), Optional.of(id), stage, Optional.empty(), field, error, description);
        }

        /**
         * Creates a fault associated with the complete reload attempt.
         *
         * @param stage       processing stage
         * @param field       optional failing field
         * @param error       stable error classification
         * @param description safe description
         * @return structured snapshot fault
         */
        public static Fault snapshot(
                final Stage stage,
                final Optional<String> field,
                final Errors error,
                final String description) {
            return new Fault(Optional.empty(), Optional.empty(), stage, Optional.empty(), field, error, description);
        }

        /**
         * Identifies the atomic snapshot processing stage that detected an issue.
         *
         * @author Kimi Liu
         */
        public enum Stage {

            /**
             * External snapshot loading failed.
             */
            LOAD,

            /**
             * Raw Blueprint validation failed.
             */
            VALIDATE,

            /**
             * Source worker compilation failed.
             */
            COMPILE,

            /**
             * Atomic view publication failed.
             */
            COMMIT

        }

    }

    /**
     * Carries immutable faults for one attempted Roster snapshot.
     * <p>
     * A successful validation and commit is represented by an empty fault list for the committed revision.
     * </p>
     *
     * @param revision attempted snapshot revision
     * @param faults   ordered safe faults that identify their resource and processing stage
     * @author Kimi Liu
     */
    record Report(Revision revision, List<Fault> faults) {

        /**
         * Creates an immutable snapshot-processing report.
         *
         * @param revision attempted snapshot revision
         * @param faults   ordered Snapshot faults
         * @throws IllegalArgumentException if a component or fault is {@code null}
         */
        public Report {
            Assert.notNull(revision, "Roster report revision must not be null");
            Assert.notNull(faults, "Roster report faults must not be null");
            final List<Fault> copy = new ArrayList<>(faults.size());
            for (Fault fault : faults) {
                copy.add(Assert.notNull(fault, "Roster report fault must not be null"));
            }
            faults = List.copyOf(copy);
        }

    }

}
