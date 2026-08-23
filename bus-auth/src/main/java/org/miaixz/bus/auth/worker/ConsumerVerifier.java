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

import org.miaixz.bus.auth.Blueprint;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.shared.SecretLease;

/**
 * Verifies submitted server-side consumer evidence without exposing the stored credential.
 * <p>
 * Implementations locate their verifier by Source and consumer id. If verification is asynchronous, the minimum
 * representation needed by the backend must be captured before this method returns; the lease is closed immediately by
 * the caller and must never cross the asynchronous boundary.
 * </p>
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface ConsumerVerifier {

    /**
     * Verifies one submitted client secret using the project-owned credential backend.
     *
     * @param source     immutable Source Blueprint entry
     * @param consumerId public consumer identifier
     * @param method     submitted endpoint authentication method
     * @param evidence   short-lived submitted secret lease
     * @param context    immutable non-secret invocation context
     * @param timeout    shared end-to-end operation timeout
     * @return asynchronous success, stable rejection, or operational failure without credential material
     */
    CompletionStage<Outcome<Void>> verify(
            Blueprint.SourceEntry source,
            String consumerId,
            Endpoint.Authentication method,
            SecretLease evidence,
            Context context,
            Timeout timeout);

}
