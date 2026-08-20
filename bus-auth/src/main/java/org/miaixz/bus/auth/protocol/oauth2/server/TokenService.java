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
package org.miaixz.bus.auth.protocol.oauth2.server;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.oauth2.RefreshTokenGrant;
import org.miaixz.bus.auth.protocol.oauth2.TokenEndpointResponse;
import org.miaixz.bus.auth.protocol.oauth2.TokenRequest;
import org.miaixz.bus.auth.protocol.oauth2.TokenResponse;
import org.miaixz.bus.auth.protocol.oauth2.grant.AccessTokenIssuer;
import org.miaixz.bus.auth.protocol.oauth2.grant.RefreshTokenRotator;
import org.miaixz.bus.core.lang.Assert;

/**
 * Routes every supported OAuth 2.x grant through the single standard token operation.
 *
 * @author Kimi Liu
 */
public final class TokenService {

    /**
     * Internal issuer for initial and exchanged access-token grants.
     */
    private final AccessTokenIssuer issuer;

    /**
     * Internal atomic refresh-token rotation implementation.
     */
    private final RefreshTokenRotator rotator;

    /**
     * Creates the token service from its two internal grant processors.
     *
     * @param issuer  initial access-token issuer
     * @param rotator refresh-token family rotator
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public TokenService(final AccessTokenIssuer issuer, final RefreshTokenRotator rotator) {
        this.issuer = Assert.notNull(issuer, "OAuth 2.x access token issuer must not be null");
        this.rotator = Assert.notNull(rotator, "OAuth 2.x refresh token rotator must not be null");
    }

    /**
     * Widens an internal ordinary token outcome to the standard token endpoint success union.
     *
     * @param outcome internal token processing outcome
     * @return outcome retaining the same success or failure branch
     */
    private static Outcome<TokenEndpointResponse> endpoint(final Outcome<TokenResponse> outcome) {
        return switch (outcome) {
            case Outcome.Succeeded<TokenResponse> success -> Outcome.succeeded(success.value());
            case Outcome.Rejected<TokenResponse> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<TokenResponse> failed -> Outcome.failed(failed.failure());
        };
    }

    /**
     * Executes exactly one grant at the standard token endpoint.
     *
     * @param request standard token request
     * @param context invocation context carrying a verified client identifier
     * @param timeout shared end-to-end time budget
     * @return asynchronous standard token response outcome
     */
    public CompletionStage<Outcome<TokenEndpointResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "OAuth 2.x token request must not be null");
        Assert.notNull(context, "OAuth 2.x token context must not be null");
        Assert.notNull(timeout, "OAuth 2.x token time budget must not be null");
        if (request.grant() instanceof RefreshTokenGrant) {
            return rotator.token(request, context, timeout).thenApply(TokenService::endpoint);
        }
        return issuer.token(request, context, timeout);
    }

}
