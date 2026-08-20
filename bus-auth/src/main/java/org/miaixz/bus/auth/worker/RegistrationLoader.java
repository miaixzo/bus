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
package org.miaixz.bus.auth.worker;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.Timeout;

/**
 * Loads the complete desired authentication registration state from an integrating project.
 * <p>
 * Implementations may obtain records from databases, files, remote services, or project settings, but those loading
 * details remain outside bus-auth. Before returning, an implementation must convert every persisted Source options
 * representation into the matching typed {@link org.miaixz.bus.auth.Options} value. Every invocation returns one
 * complete batch whose record list is structurally frozen; incremental events, persistence decoding, and
 * protocol-specific resource loading are not part of the framework boundary. The loader retains ownership of the
 * mutable project entities referenced by those records.
 * </p>
 *
 * @author Kimi Liu
 */
public interface RegistrationLoader {

    /**
     * Loads one complete desired registration batch within the caller's existing time budget.
     *
     * @param context current non-secret authentication call context
     * @param timeout shared end-to-end time budget
     * @return asynchronous stage containing the complete desired registration batch
     */
    CompletionStage<Batch> load(Context context, Timeout.Budget timeout);

    /**
     * Carries one complete externally loaded registration revision without constructing Registry state.
     *
     * @param revision      monotonically increasing external data revision
     * @param registrations complete desired registration records
     */
    record Batch(long revision, List<Registration.Record<?>> registrations) {

        /** Creates an immutable externally loaded registration batch. */
        public Batch {
            registrations = List.copyOf(registrations);
        }
    }

}
