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

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.core.lang.Optional;

/**
 * Executes strongly typed capabilities against the current compiled authentication runtime.
 * <p>
 * This contract owns capability routing and invocation only. It does not load, register, validate, persist, or expose
 * registration data, and it does not perform project authorization, account binding, session management, or auditing.
 * </p>
 *
 * @author Kimi Liu
 */
public interface Authenticator {

    /**
     * Reports whether the current generation contains a compiled worker for a Source reference.
     *
     * @param reference Source reference
     * @return {@code true} when the reference is currently invocable
     */
    boolean available(Registry.Reference reference);

    /**
     * Returns the capabilities exposed by a Source in the current generation.
     *
     * @param reference Source reference
     * @return immutable manifest, or empty when the Source is unavailable
     */
    Optional<Capability.Manifest> manifest(Registry.Reference reference);

    /**
     * Executes one capability declared by the referenced Source.
     *
     * @param reference  registered Source reference
     * @param capability strongly typed requested capability
     * @param request    request matching the capability request type
     * @param context    immutable invocation context
     * @param timeout    shared decreasing operation budget
     * @param <Q>        request type
     * @param <S>        success type
     * @return asynchronous protocol-neutral outcome
     */
    <Q, S> CompletionStage<Outcome<S>> invoke(
            Registry.Reference reference,
            Capability<Q, S> capability,
            Q request,
            Context context,
            Timeout.Budget timeout);

}
