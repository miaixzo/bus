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
package org.miaixz.bus.auth.guard;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.resolver.ClientResolver;

/**
 * Authenticates a protocol client from that protocol's real authentication-evidence boundary.
 * <p>
 * OAuth client secret methods, private-key assertions, mutual TLS, SAML, and LDAP do not share one wire credential
 * shape, so this strategy remains generic in {@code Q}. Implementations select only registered methods, resolve the
 * client through ClientResolver, keep secret material inside SecretLease scope, and use SecretGuard for comparisons.
 * Expected authentication refusal is returned as Outcome rather than encoded as a protocol response by this layer.
 * </p>
 *
 * @param <Q> formal protocol request or existing transport type carrying that protocol's authentication evidence
 * @author Kimi Liu
 */
@FunctionalInterface
public interface ClientAuthenticator<Q> {

    /**
     * Authenticates and resolves the protocol client represented by a real protocol evidence value.
     *
     * @param request formal protocol request or existing transport containing standard authentication evidence
     * @param context current non-secret invocation context
     * @param timeout shared end-to-end time budget
     * @return asynchronous internal outcome containing the immutable resolved client view
     */
    CompletionStage<Outcome<ClientResolver.Client>> authenticate(Q request, Context context, Timeout.Budget timeout);

}
