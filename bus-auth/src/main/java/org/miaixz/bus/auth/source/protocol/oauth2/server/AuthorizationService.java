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
package org.miaixz.bus.auth.source.protocol.oauth2.server;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.protocol.oauth2.AuthorizationRequest;
import org.miaixz.bus.auth.source.protocol.oauth2.AuthorizationResponse;
import org.miaixz.bus.auth.source.protocol.oauth2.grant.AuthorizationCodeIssuer;
import org.miaixz.bus.core.lang.Assert;

/**
 * Exposes the single standard OAuth 2.x authorization server operation.
 *
 * @author Kimi Liu
 */
public class AuthorizationService {

    /**
     * Internal authorization-code flow implementation.
     */
    private final AuthorizationCodeIssuer issuer;

    /**
     * Creates the standard authorization service facade.
     *
     * @param issuer internal validated authorization-code issuer
     * @throws IllegalArgumentException if the issuer is {@code null}
     */
    public AuthorizationService(final AuthorizationCodeIssuer issuer) {
        this.issuer = Assert.notNull(issuer, "OAuth 2.x authorization code issuer must not be null");
    }

    /**
     * Executes one authorization-code request for the authenticated subject in the invocation context.
     *
     * @param request standard authorization request
     * @param context invocation context carrying the authenticated subject
     * @param timeout shared end-to-end timeout
     * @return asynchronous standard authorization response outcome
     */
    public CompletionStage<Outcome<AuthorizationResponse>> authorize(
            final AuthorizationRequest request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "OAuth 2.x authorization request must not be null");
        Assert.notNull(context, "OAuth 2.x authorization context must not be null");
        Assert.notNull(timeout, "OAuth 2.x authorization timeout must not be null");
        return issuer.authorize(AuthorizationCodeIssuer.Request.oauth(request), context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<AuthorizationCodeIssuer.Result> success -> Outcome
                            .succeeded(success.value().response());
                    case Outcome.Rejected<AuthorizationCodeIssuer.Result> rejected -> Outcome
                            .rejected(rejected.failure());
                    case Outcome.Failed<AuthorizationCodeIssuer.Result> failed -> Outcome.failed(failed.failure());
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Executes authorization while preserving the validated transport redirect target for the HTTP adapter.
     *
     * @param request standard authorization request
     * @param context invocation context carrying the authenticated subject
     * @param timeout shared end-to-end timeout
     * @return asynchronous internal result containing standard response and redirect target
     */
    CompletionStage<Outcome<AuthorizationCodeIssuer.Result>> authorizeEndpoint(
            final AuthorizationRequest request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "OAuth 2.x authorization request must not be null");
        Assert.notNull(context, "OAuth 2.x authorization context must not be null");
        Assert.notNull(timeout, "OAuth 2.x authorization timeout must not be null");
        return issuer.authorize(AuthorizationCodeIssuer.Request.oauth(request), context, timeout);
    }

}
