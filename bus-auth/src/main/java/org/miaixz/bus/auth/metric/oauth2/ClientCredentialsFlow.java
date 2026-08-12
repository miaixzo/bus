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
package org.miaixz.bus.auth.metric.oauth2;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Client;
import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.OAuth2.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Implements the confidential-client credentials grant without issuing refresh tokens.
 * <p>
 * The flow resolves the registered client, requires a secret-based token endpoint authentication method, authenticates
 * through the runtime secret port, validates the requested scopes, and persists one opaque access-token grant. Fields
 * belonging to other grants are rejected before any credential lookup or state change.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ClientCredentialsFlow {

    /**
     * Registered client attribute selecting token-endpoint authentication.
     */
    private static final String TOKEN_ENDPOINT_AUTH_METHOD = "token_endpoint_auth_method";

    /**
     * Confidential HTTP Basic client authentication method.
     */
    private static final String AUTH_SECRET_BASIC = "client_secret_basic";

    /**
     * Confidential form client authentication method.
     */
    private static final String AUTH_SECRET_POST = "client_secret_post";

    /**
     * Trusted OAuth policy.
     */
    private final Policy policy;

    /**
     * Validated authentication runtime.
     */
    private final Runtime runtime;

    /**
     * Creates one client-credentials state machine.
     *
     * @param policy  trusted OAuth policy
     * @param runtime validated authentication runtime
     */
    public ClientCredentialsFlow(final Policy policy, final Runtime runtime) {
        this.policy = Assert.notNull(policy, "OAuth policy must be not null!");
        this.runtime = Assert.notNull(runtime, "Authentication runtime must be not null!");
    }

    /**
     * Rejects fields belonging to any grant other than client credentials.
     *
     * @param request token request
     */
    private static void validate(final TokenRequest request) {
        if (request.grantType() != GrantType.CLIENT_CREDENTIALS || StringKit.isBlank(request.clientId())
                || request.code() != null || request.redirectUri() != null || request.codeVerifier() != null
                || request.refreshToken() != null || request.deviceCode() != null) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
    }

    /**
     * Requires a registered secret-based authentication method.
     *
     * @param client resolved registered client
     */
    private static void confidential(final Client client) {
        final Object configured = client.attributes().get(TOKEN_ENDPOINT_AUTH_METHOD);
        if (!(configured instanceof String method)
                || !AUTH_SECRET_BASIC.equals(method) && !AUTH_SECRET_POST.equals(method)) {
            throw new ProtocolException(ProtocolError.UNAUTHORIZED_CLIENT);
        }
    }

    /**
     * Authenticates one confidential client and issues one access token.
     *
     * @param invocation tenant-scoped operation context
     * @param request    client-credentials token request
     * @return stage containing the issued opaque access token
     */
    public CompletionStage<TokenResponse> exchange(final Invocation invocation, final TokenRequest request) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final TokenRequest input = Assert.notNull(request, "Token request must be not null!");
        OAuth2Validator.grant(input.grantType(), policy.grants());
        validate(input);
        final CompletionStage<Optional<Client>> resolved = Assert.notNull(
                runtime.clients().resolve(context, input.clientId()),
                "Client resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final Client client = OAuth2Validator.client(optional, input.clientId());
            confidential(client);
            final Set<String> scopes = OAuth2Validator.scopes(input.scopes(), policy.scopes(), runtime.limits());
            return OAuth2Validator.authenticate(context, client, input.clientSecret(), runtime).thenCompose(
                    authenticated -> AuthorizationCodeFlow.issueTokens(
                            context,
                            authenticated.id(),
                            authenticated.id(),
                            scopes,
                            false,
                            null,
                            policy,
                            runtime));
        });
    }

}
