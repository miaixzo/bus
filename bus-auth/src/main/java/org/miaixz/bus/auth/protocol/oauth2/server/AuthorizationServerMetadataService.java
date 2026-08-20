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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.shared.pkce.PkceMethod;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Publishes RFC 8414 authorization server metadata derived solely from one validated Provider policy.
 * <p>
 * The service reports only endpoints and features that the compiled OAuth Provider can actually execute. Deployment
 * information that is not represented by {@link OAuth2ProviderSettings}, including JWK Set, documentation, policy,
 * signed metadata, dynamic registration, and DPoP transport support, remains absent.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AuthorizationServerMetadataService {

    /**
     * Immutable Provider settings used as the single metadata source of truth.
     */
    private final OAuth2ProviderSettings settings;

    /**
     * Creates a metadata service for one validated OAuth Provider.
     *
     * @param settings validated immutable Provider settings
     * @throws IllegalArgumentException if settings are {@code null}
     */
    public AuthorizationServerMetadataService(final OAuth2ProviderSettings settings) {
        this.settings = Assert.notNull(settings, "OAuth 2.x Provider settings must not be null");
    }

    /**
     * Converts an optional endpoint into its exact validated URL string.
     *
     * @param endpoint optional endpoint declaration
     * @return optional endpoint URL
     */
    private static Optional<String> url(final Endpoint endpoint) {
        return endpoint == null ? Optional.empty() : Optional.of(endpoint.url().toString());
    }

    /**
     * Maps declared endpoint authentication identifiers to deterministic OAuth metadata values.
     *
     * @param endpoint optional endpoint declaration
     * @return sorted immutable client-authentication method list
     */
    private static List<ClientAuthenticationMethod> authentication(final Endpoint endpoint) {
        if (endpoint == null) {
            return List.of();
        }
        return endpoint.authentication().stream().map(Endpoint.Authentication::value)
                .map(ClientAuthenticationMethod::new).sorted(Comparator.comparing(ClientAuthenticationMethod::value))
                .toList();
    }

    /**
     * Creates a non-sensitive framework failure without a custom exception type.
     *
     * @param code        shared Bus error code
     * @param description safe diagnostic description
     * @return immutable framework failure
     */
    private static Outcome.Failure failure(final Errors code, final String description) {
        return new Outcome.Failure(code, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates an already-completed asynchronous outcome.
     *
     * @param <T>     successful value type
     * @param outcome closed operation outcome
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Returns the exact standard metadata currently implemented by this Provider.
     *
     * @param context invocation context retained for the uniform service contract
     * @param timeout shared end-to-end operation budget
     * @return completed asynchronous metadata outcome
     */
    public CompletionStage<Outcome<AuthorizationServerMetadata>> metadata(
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "OAuth 2.x metadata context must not be null");
        Assert.notNull(timeout, "OAuth 2.x metadata time budget must not be null");
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(
                            failure(ErrorCode._408, "OAuth 2.x metadata operation has no remaining time budget")));
        }
        try {
            return completed(Outcome.succeeded(build()));
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.failed(
                            failure(ErrorCode._500, "OAuth 2.x authorization server metadata construction failed")));
        }
    }

    /**
     * Builds the RFC 8414 wire model without consulting mutable runtime state.
     *
     * @return immutable authorization server metadata
     */
    private AuthorizationServerMetadata build() {
        final Endpoint authorization = settings.authorizationEndpoint().getOrNull();
        final Endpoint token = settings.tokenEndpoint().getOrNull();
        final Endpoint revocation = settings.revocationEndpoint().getOrNull();
        final Endpoint introspection = settings.introspectionEndpoint().getOrNull();
        final Endpoint device = settings.deviceAuthorizationEndpoint().getOrNull();
        final boolean authorizationEnabled = authorization != null;
        return new AuthorizationServerMetadata(settings.issuer(), url(authorization), url(token), Optional.empty(),
                settings.scopesSupported().stream().sorted().toList(),
                authorizationEnabled ? List.of(ResponseType.CODE) : List.of(),
                authorizationEnabled ? List.of(OAuth2.ResponseModes.QUERY) : List.of(),
                settings.grantTypesSupported().stream().sorted(Comparator.comparing(GrantType::value)).toList(),
                settings.tokenEndpointAuthMethodsSupported().stream()
                        .sorted(Comparator.comparing(ClientAuthenticationMethod::value)).toList(),
                List.of(), Optional.empty(), List.of(), Optional.empty(), Optional.empty(), url(revocation),
                authentication(revocation), List.of(), url(introspection), authentication(introspection), List.of(),
                authorizationEnabled && settings.pkceRequired() ? List.of(PkceMethod.S256) : List.of(),
                Optional.empty(), url(device), authorizationEnabled ? Optional.of(Boolean.TRUE) : Optional.empty(),
                List.of(), new JsonValue.ObjectValue(Map.of()));
    }

}
