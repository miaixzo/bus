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
package org.miaixz.bus.auth.worker.loader;

import java.util.List;

import org.miaixz.bus.auth.Blueprint;
import org.miaixz.bus.auth.Loader;
import org.miaixz.bus.auth.Scheme;

/**
 * Loads the complete desired authentication Blueprint from an integrating project.
 * <p>
 * Implementations may obtain records from databases, files, remote services, or project settings, but those loading
 * details remain outside bus-auth. Before returning, an implementation must convert every persisted Source options
 * representation into the matching typed {@link Scheme.Options} value. Every invocation returns one complete snapshot
 * whose entry list is structurally frozen; incremental events, persistence decoding, and protocol-specific resource
 * loading are not part of the framework boundary. Each Blueprint entry immediately detaches the framework fields from
 * the project entity, so later project mutation cannot alter the returned snapshot.
 * </p>
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface BlueprintLoader extends Loader<BlueprintLoader.Request, BlueprintLoader.Snapshot> {

    /**
     * Identifies the complete desired Blueprint requested by one reload operation.
     *
     * @param currentRevision current committed Roster revision
     * @author Kimi Liu
     */
    record Request(long currentRevision) {

        /**
         * Validates one Blueprint-loading request.
         */
        public Request {
            if (currentRevision < 0L) {
                throw new IllegalArgumentException("Current Roster revision must not be negative");
            }
        }

    }

    /**
     * Carries one complete externally loaded Blueprint revision without constructing Roster state.
     *
     * @param revision monotonically increasing external data revision
     * @param entries  complete desired Blueprint entries
     * @author Kimi Liu
     */
    record Snapshot(long revision, List<Blueprint.Entry> entries) {

        /**
         * Creates an immutable externally loaded Blueprint snapshot.
         */
        public Snapshot {
            if (revision < 0L) {
                throw new IllegalArgumentException("Blueprint revision must not be negative");
            }
            if (entries == null) {
                throw new IllegalArgumentException("Blueprint entries must not be null");
            }
            for (Blueprint.Entry entry : entries) {
                if (entry == null) {
                    throw new IllegalArgumentException("Blueprint entry must not be null");
                }
            }
            entries = List.copyOf(entries);
        }

    }

}
