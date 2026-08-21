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

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.shared.SecretLease;

/**
 * Loads a fresh externally owned secret lease.
 */
@FunctionalInterface
public interface SecretLoader {

    /**
     * Loads a fresh secret lease within the exact Source registration scope.
     *
     * @param registration exact Source registration requesting the secret
     * @param reference    exact project credential reference
     * @param context      immutable non-secret invocation context
     * @param timeout      shared end-to-end operation budget
     * @return asynchronous project loading outcome
     */
    CompletionStage<Outcome<Record>> load(
            Registration.SourceEntry registration,
            Credential.Reference reference,
            Context context,
            Timeout.Budget timeout);

    /**
     * Loaded secret lease paired with its exact external reference.
     *
     * @param sourceId  exact Source identifier that owns the returned data
     * @param reference exact credential reference resolved by the project
     * @param lease     fresh closeable secret lease
     */
    record Record(String sourceId, Credential.Reference reference, SecretLease lease) {

    }

}
