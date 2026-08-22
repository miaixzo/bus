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

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;

/**
 * Defines the executable capability worker produced for one compiled Source registration.
 * <p>
 * Protocol and Vendor {@code SourceDriver} implementations return this contract to runtime compilation. The runtime
 * retains each instance in one runtime container and invokes it only after reference lookup, lifecycle, timeout,
 * manifest, and request-type checks. Applications execute registered capabilities through
 * {@link org.miaixz.bus.auth.Dispatcher}; they do not construct or invoke Source workers directly.
 * </p>
 * <p>
 * This interface owns only the immutable capability declaration and typed execution entry. It does not load or mutate
 * registrations, select a Source, manage lifecycle, perform project authorization, create sessions, or expose protocol
 * implementation details.
 * </p>
 *
 * @author Kimi Liu
 */
public interface SourceWorker extends AutoCloseable {

    /**
     * Returns the immutable capabilities implemented by this compiled registration.
     *
     * @return exact immutable capability manifest
     */
    Capability.Manifest manifest();

    /**
     * Executes one declared strongly typed capability within the caller's existing timeout.
     *
     * @param capability declared capability
     * @param request    exact capability request
     * @param context    current non-secret invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        success type
     * @return asynchronous internal outcome
     */
    <Q, S> CompletionStage<Outcome<S>> invoke(Capability<Q, S> capability, Q request, Context context, Timeout timeout);

    /**
     * Releases resources owned exclusively by this compiled Source worker.
     * <p>
     * Stateless workers use the default no-op implementation. A worker that owns a connection pool, remote client, or
     * another container-scoped resource overrides this method. Caller-owned execution services and project workers must
     * never be closed here.
     * </p>
     */
    @Override
    default void close() {
        // Most compiled workers are immutable and stateless.
    }

}
