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

import java.security.SecureRandom;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.*;
import org.miaixz.bus.auth.resolver.SecretResolver;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Clock;

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
    private final OAuth2Dependencies dependencies;

    /**
     * Tenant-isolated state store passed to the common opaque-token issuer.
     */
    private final StateStore states;

    /**
     * Product secret resolver used for confidential-client authentication.
     */
    private final SecretResolver secrets;

    /**
     * Fabric clock used by the common token issuer.
     */
    private final Clock clock;

    /**
     * Secure random source used by the common token issuer.
     */
    private final SecureRandom random;

    /**
     * Explicit JSON provider used for bounded token state.
     */
    private final JsonProvider json;

    /**
     * Closed protocol parser and allocation limits.
     */
    private final Limits limits;

    /**
     * Creates one client-credentials state machine.
     *
     * @param policy       trusted OAuth policy
     * @param dependencies registered-client and authorization-grant product ports
     * @param states       tenant-isolated atomic state store
     * @param secrets      confidential-client secret resolver
     * @param clock        Fabric clock used for token lifetime calculation
     * @param random       secure random source used for opaque credentials
     * @param json         explicit JSON provider for bounded state
     * @param limits       closed parser and allocation limits
     */
    public ClientCredentialsFlow(final Policy policy, final OAuth2Dependencies dependencies, final StateStore states,
            final SecretResolver secrets, final Clock clock, final SecureRandom random, final JsonProvider json,
            final Limits limits) {
        this.policy = Assert.notNull(policy, "OAuth policy must be not null!");
        this.dependencies = Assert.notNull(dependencies, "OAuth dependencies must be not null!");
        this.states = Assert.notNull(states, "State store must be not null!");
        this.secrets = Assert.notNull(secrets, "Secret resolver must be not null!");
        this.clock = Assert.notNull(clock, "Clock must be not null!");
        this.random = Assert.notNull(random, "Secure random must be not null!");
        this.json = Assert.notNull(json, "JSON provider must be not null!");
        this.limits = Assert.notNull(limits, "Limits must be not null!");
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
    private static void confidential(final RegisteredClient client) {
        final Object configured = client.tokenEndpointAuthMethod();
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
    public CompletionStage<TokenResponse> exchange(final Context invocation, final TokenRequest request) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final TokenRequest input = Assert.notNull(request, "Token request must be not null!");
        OAuth2Validator.grant(input.grantType(), policy.grants());
        validate(input);
        final CompletionStage<Optional<RegisteredClient>> resolved = Assert.notNull(
                dependencies.clients().resolve(context, input.clientId()),
                "RegisteredClient resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final RegisteredClient client = OAuth2Validator.client(optional, input.clientId());
            confidential(client);
            final Set<String> scopes = OAuth2Validator.scopes(input.scopes(), policy.scopes(), limits);
            return OAuth2Validator.authenticate(context, client, input.clientSecret(), secrets).thenCompose(
                    authenticated -> AuthorizationCodeFlow.issueTokens(
                            context,
                            authenticated.id(),
                            authenticated.id(),
                            scopes,
                            false,
                            null,
                            policy,
                            dependencies,
                            states,
                            clock,
                            random,
                            json,
                            limits));
        });
    }

}
