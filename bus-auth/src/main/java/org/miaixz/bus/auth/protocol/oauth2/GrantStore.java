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
 * Product-owned persistence port for immutable OAuth 2.0 authorization grants. Implementations must isolate data by the
 * supplied root {@link Context}, return non-null stages, and make revocation idempotent.
 *
 * @author Kimi Liu
 */
public interface GrantStore {

    /**
     * Persists or replaces one immutable grant using its exact identifier.
     *
     * @param context non-null tenant-scoped authentication context
     * @param grant   non-null immutable authorization grant
     * @return non-null completion stage
     */
    CompletionStage<Void> save(Context context, AuthorizationGrant grant);

    /**
     * Finds one unexpired or historical grant according to the product retention policy.
     *
     * @param context non-null tenant-scoped authentication context
     * @param grantId non-blank exact grant identifier
     * @return non-null stage containing the grant or an empty optional
     */
    CompletionStage<Optional<AuthorizationGrant>> find(Context context, String grantId);

    /**
     * Idempotently revokes one exact grant identifier.
     *
     * @param context non-null tenant-scoped authentication context
     * @param grantId non-blank exact grant identifier
     * @return non-null stage containing whether stored state changed
     */
    CompletionStage<Boolean> revoke(Context context, String grantId);

}
