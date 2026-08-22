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
package org.miaixz.bus.auth.protocol.oidc.server;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationServerMetadata;
import org.miaixz.bus.auth.protocol.oauth2.ResponseType;
import org.miaixz.bus.auth.protocol.oauth2.server.OAuth2ServerOptions;
import org.miaixz.bus.auth.protocol.oidc.ClaimType;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.protocol.oidc.OpenIdProviderMetadata;
import org.miaixz.bus.auth.shared.pkce.PkceMethod;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Produces OpenID Provider Metadata exclusively from one frozen server-role Source options object.
 * <p>
 * The service performs no Registry, loader, parser, cache, or network access. It advertises only the Authorization Code
 * Flow, endpoint operations, client authentication methods, and extensions enabled in the compiled Source runtime.
 * </p>
 *
 * @author Kimi Liu
 */
public class DiscoveryService {

    /**
     * Frozen OpenID Provider options used as the only metadata source.
     */
    private final OpenIdServerOptions options;

    /**
     * Creates a metadata service for one compiled OpenID Provider.
     *
     * @param options validated and frozen OpenID Provider options
     * @throws IllegalArgumentException if {@code options} is {@code null}
     */
    public DiscoveryService(final OpenIdServerOptions options) {
        this.options = Assert.notNull(options, "OpenID Connect discovery options must not be null");
    }

    /**
     * Extracts a required endpoint URL from validated options.
     *
     * @param endpoint endpoint container
     * @param label    safe endpoint label
     * @return exact configured URL
     * @throws IllegalStateException if compiled options violate their required endpoint invariant
     */
    private static String requiredUrl(final Optional<Endpoint> endpoint, final String label) {
        final Endpoint value = endpoint.getOrNull();
        if (value == null) {
            throw new IllegalStateException("Compiled OpenID Provider has no " + label + " endpoint");
        }
        return value.url().toString();
    }

    /**
     * Converts an optional endpoint into an optional exact URL.
     *
     * @param endpoint endpoint container
     * @return optional configured URL
     */
    private static Optional<String> optionalUrl(final Optional<Endpoint> endpoint) {
        final Endpoint value = endpoint.getOrNull();
        return value == null ? Optional.empty() : Optional.of(value.url().toString());
    }

    /**
     * Returns the deterministic OpenID Provider Metadata document model.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end operation timeout
     * @return completed metadata outcome
     */
    public CompletionStage<Outcome<OpenIdProviderMetadata>> discover(final Context context, final Timeout timeout) {
        Assert.notNull(context, "OpenID Connect discovery context must not be null");
        Assert.notNull(timeout, "OpenID Connect discovery timeout must not be null");
        if (timeout.expired()) {
            return CompletableFuture.completedFuture(
                    Outcome.failed(
                            new Outcome.Failure(ErrorCode._408, "OpenID Connect discovery has no remaining timeout",
                                    new JsonValue.ObjectValue(Map.of()))));
        }
        final OAuth2ServerOptions oauth = options.oauth2Options();
        final AuthorizationServerMetadata authorizationServer = new AuthorizationServerMetadata(options.issuer(),
                Optional.of(requiredUrl(oauth.authorizationEndpoint(), "authorization")),
                Optional.of(requiredUrl(oauth.tokenEndpoint(), "token")),
                Optional.of(requiredUrl(options.jwkSetEndpoint(), "JWK Set")),
                oauth.scopesSupported().stream().sorted().toList(), List.of(ResponseType.CODE),
                List.of(OpenIdConnect.ResponseModes.QUERY), oauth.grantTypesSupported().stream().toList(),
                oauth.tokenEndpointAuthMethodsSupported().stream().toList(), List.of(), Optional.empty(), List.of(),
                Optional.empty(), Optional.empty(), optionalUrl(oauth.revocationEndpoint()), List.of(), List.of(),
                optionalUrl(oauth.introspectionEndpoint()), List.of(), List.of(),
                oauth.pkceRequired() ? List.of(PkceMethod.S256) : List.of(), Optional.empty(),
                optionalUrl(oauth.deviceAuthorizationEndpoint()), Optional.of(Boolean.TRUE), List.of(),
                new JsonValue.ObjectValue(Map.of()));
        final OpenIdProviderMetadata metadata = new OpenIdProviderMetadata(authorizationServer,
                optionalUrl(options.userInfoEndpoint()), List.of(), List.copyOf(options.subjectTypesSupported()),
                List.of(options.idTokenSigningAlgorithm()), List.copyOf(options.idTokenEncryptionAlgorithmsSupported()),
                List.copyOf(options.idTokenEncryptionMethodsSupported()), List.of(), List.of(), List.of(), List.of(),
                List.of(ClaimType.NORMAL), List.copyOf(options.claimsSupported()), List.of(), Optional.of(Boolean.TRUE),
                Optional.of(Boolean.FALSE), Optional.of(Boolean.FALSE), Optional.empty(),
                optionalUrl(options.endSessionEndpoint()), new JsonValue.ObjectValue(Map.of()));
        return CompletableFuture.completedFuture(Outcome.succeeded(metadata));
    }

}
