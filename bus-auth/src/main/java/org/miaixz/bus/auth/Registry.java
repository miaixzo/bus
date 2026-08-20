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
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.registry.RegistryIssue;
import org.miaixz.bus.core.Lifecycle;
import org.miaixz.bus.core.lang.Assert;

/**
 * Provides the public runtime-registration entry to compiled Source capabilities.
 * <p>
 * Callers identify a registered resource with {@link Reference} and request one strongly typed {@link Capability}. The
 * Capability dispatch never returns runtime providers, protocol executors, or Vendor adapters. The diagnostic
 * {@link #snapshot()} method exposes the committed registration records, including their persistence entities.
 * Implementations atomically replace structurally immutable compiled views and preserve the previous view when
 * validation or compilation fails.
 * </p>
 *
 * @author Kimi Liu
 */
public interface Registry extends Lifecycle, AutoCloseable {

    /**
     * Invokes one declared capability through the currently committed immutable Registry view.
     *
     * @param reference  registered Source reference
     * @param capability strongly typed capability implemented by the referenced runtime provider
     * @param request    formal standard request value
     * @param context    immutable invocation context
     * @param timeout    shared decreasing operation time budget
     * @param <Q>        formal request type
     * @param <S>        formal success value type
     * @return asynchronous internal outcome without exposing the selected runtime provider
     */
    <Q, S> CompletionStage<Outcome<S>> invoke(
            Reference reference,
            Capability<Q, S> capability,
            Q request,
            Context context,
            Timeout.Budget timeout);

    /**
     * Returns the complete registration snapshot associated with the committed Registry view.
     * <p>
     * The snapshot owns an unmodifiable record list, but each record still references the mutable persistence entity
     * supplied by the external project.
     * </p>
     *
     * @return current structurally frozen registration snapshot
     */
    Snapshot snapshot();

    /**
     * Returns the revision of the currently committed Registry view.
     *
     * @return current Registry snapshot revision
     */
    Revision revision();

    /**
     * Closes the Registry without transferring ownership of externally supplied stores, resolvers, or transports.
     */
    @Override
    void close();

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
    record Snapshot(Revision revision, List<Registration.Record<?>> records) {

        /**
         * Creates a snapshot with a detached unmodifiable record list.
         * <p>
         * Record wrappers and contained persistence entities are not deep-copied.
         * </p>
         *
         * @param revision snapshot revision
         * @param records  complete registration records
         * @throws IllegalArgumentException if a component or record is {@code null}
         */
        public Snapshot {
            Assert.notNull(revision, "Registry snapshot revision must not be null");
            Assert.notNull(records, "Registry snapshot records must not be null");
            final List<Registration.Record<?>> copy = new ArrayList<>(records.size());
            for (Registration.Record<?> record : records) {
                copy.add(Assert.notNull(record, "Registry snapshot record must not be null"));
            }
            records = List.copyOf(copy);
        }

    }

    /**
     * Carries immutable validation or compilation issues for a rejected Registry snapshot.
     *
     * @param revision rejected snapshot revision
     * @param issues   ordered safe issues that identify their resource and processing stage
     * @author Kimi Liu
     */
    record Report(Revision revision, List<RegistryIssue> issues) {

        /**
         * Creates an immutable rejected-snapshot report.
         *
         * @param revision rejected snapshot revision
         * @param issues   ordered Registry issues
         * @throws IllegalArgumentException if a component or issue is {@code null}
         */
        public Report {
            Assert.notNull(revision, "Registry report revision must not be null");
            Assert.notNull(issues, "Registry report issues must not be null");
            final List<RegistryIssue> copy = new ArrayList<>(issues.size());
            for (RegistryIssue issue : issues) {
                copy.add(Assert.notNull(issue, "Registry report issue must not be null"));
            }
            issues = List.copyOf(copy);
        }

    }

}
