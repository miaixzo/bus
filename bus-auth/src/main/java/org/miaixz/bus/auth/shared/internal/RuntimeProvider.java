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
package org.miaixz.bus.auth.shared.internal;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;

/**
 * Defines the non-exported executable capability contract shared by protocol, Vendor, runtime, and Registry layers.
 * <p>
 * The contract is deliberately outside Registry so protocol and Vendor implementations never depend on Registry
 * internals. Business callers cannot access this package and must invoke compiled registrations through Registry.
 * </p>
 *
 * @author Kimi Liu
 */
public interface RuntimeProvider {

    /**
     * Returns the immutable capabilities implemented by this compiled registration.
     *
     * @return exact immutable capability manifest
     */
    Capability.Manifest manifest();

    /**
     * Executes one declared strongly typed capability within the caller's existing time budget.
     *
     * @param capability declared capability
     * @param request    exact capability request
     * @param context    current non-secret invocation context
     * @param timeout    shared end-to-end time budget
     * @param <Q>        request type
     * @param <S>        success type
     * @return asynchronous internal outcome
     */
    <Q, S> CompletionStage<Outcome<S>> invoke(
            Capability<Q, S> capability,
            Q request,
            Context context,
            Timeout.Budget timeout);

}
