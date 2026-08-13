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
package org.miaixz.bus.auth.protocol.oauth2;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;

/**
 * Product-owned port that resolves one trusted OAuth 2.0 client registration by its exact protocol identifier.
 * Implementations must return a non-null stage containing a non-null optional and must not perform implicit client
 * creation.
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface ClientResolver {

    /**
     * Resolves one immutable registered client within the tenant supplied by the operation context.
     *
     * @param context  non-null tenant-scoped authentication context
     * @param clientId non-blank exact OAuth client identifier
     * @return non-null stage containing the trusted registration or an empty optional
     */
    CompletionStage<Optional<RegisteredClient>> resolve(Context context, String clientId);

}
