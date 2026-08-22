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

/**
 * Loads the complete desired authentication registration state from an integrating project.
 * <p>
 * Implementations may obtain records from databases, files, remote services, or project settings, but those loading
 * details remain outside bus-auth. Before returning, an implementation must convert every persisted Source options
 * representation into the matching typed {@link org.miaixz.bus.auth.Options} value. Every invocation returns one
 * complete batch whose entry list is structurally frozen; incremental events, persistence decoding, and
 * protocol-specific resource loading are not part of the framework boundary. Each Registration entry immediately
 * detaches the framework fields from the project entity, so later project mutation cannot alter the returned batch.
 * </p>
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface RegistrationLoader extends Loader<RegistrationLoader.Request, RegistrationLoader.Batch> {

    /**
     * Identifies the complete desired registration state requested by one reload operation.
     *
     * @param currentRevision current committed Registry revision
     * @author Kimi Liu
     */
    record Request(long currentRevision) {

        /**
         * Validates one registration-loading request.
         */
        public Request {
            if (currentRevision < 0L) {
                throw new IllegalArgumentException("Current Registry revision must not be negative");
            }
        }

    }

    /**
     * Carries one complete externally loaded registration revision without constructing Registry state.
     *
     * @param revision      monotonically increasing external data revision
     * @param registrations complete desired registration entries
     * @author Kimi Liu
     */
    record Batch(long revision, List<Blueprint.Entry> registrations) {

        /**
         * Creates an immutable externally loaded registration batch.
         */
        public Batch {
            if (revision < 0L) {
                throw new IllegalArgumentException("Registration revision must not be negative");
            }
            if (registrations == null) {
                throw new IllegalArgumentException("Registration entries must not be null");
            }
            for (Blueprint.Entry registration : registrations) {
                if (registration == null) {
                    throw new IllegalArgumentException("Registration entry must not be null");
                }
            }
            registrations = List.copyOf(registrations);
        }

    }

}
