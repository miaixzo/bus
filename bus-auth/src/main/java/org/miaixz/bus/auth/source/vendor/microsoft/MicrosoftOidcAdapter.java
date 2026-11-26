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
package org.miaixz.bus.auth.source.vendor.microsoft;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.source.SourceServices;
import org.miaixz.bus.auth.source.protocol.oauth2.AuthorizationCodeGrant;
import org.miaixz.bus.auth.source.protocol.oauth2.TokenEndpointResponse;
import org.miaixz.bus.auth.source.protocol.oauth2.TokenRequest;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientOptions;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.source.protocol.oidc.AuthenticationRequest;
import org.miaixz.bus.auth.source.protocol.oidc.OpenIdClientDriver;
import org.miaixz.bus.auth.source.protocol.oidc.client.OpenIdClientOptions;
import org.miaixz.bus.auth.source.protocol.oidc.client.OpenIdClientScheme;
import org.miaixz.bus.auth.source.protocol.oidc.codec.JwkSetCodec;
import org.miaixz.bus.auth.source.vendor.StandardAdapter;
import org.miaixz.bus.auth.source.vendor.VendorAdapter;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Exposes tenant-scoped Entra OIDC operations to an application that owns its browser interaction. The application
 * correlates and consumes state and verifies ID Tokens with its managed keys. This adapter restricts the standard
 * client to the configured browser code flow and never calls Graph.
 *
 * @author Kimi Liu
 */
final class MicrosoftOidcAdapter implements VendorAdapter {

    /**
     * A SHA-256 challenge is exactly 43 unpadded Base64url characters.
     */
    private static final Pattern S256_CHALLENGE = Pattern.compile("[A-Za-z0-9_-]{43}");
    /**
     * RFC 7636 verifier alphabet and length bounds.
     */
    private static final Pattern VERIFIER = Pattern.compile("[A-Za-z0-9._~-]{43,128}");

    /**
     * Trusted registration and allowed identity scopes.
     */
    private final MicrosoftOptions options;
    /**
     * Shared standard OpenID Connect client operations.
     */
    private final SourceWorker client;
    /**
     * Only the capabilities declared by the Microsoft OIDC manifest.
     */
    private final StandardAdapter operations;

    /**
     * Compiles the standard client with the selected Microsoft endpoints and execution services.
     *
     * @param tenantId local owning tenant selected by the Vendor compiler
     * @param sourceId local Source selected by the Vendor compiler
     * @param manifest Microsoft connector's immutable manifest
     * @param variant  exact registered OIDC variant
     * @param options  trusted deployment inputs containing a credential reference
     * @param services Source-bound policies, executor and Secret loader
     */
    MicrosoftOidcAdapter(final String tenantId, final String sourceId, final MicrosoftManifest manifest,
            final VendorManifest.Variant variant, final MicrosoftOptions options, final SourceServices services) {
        if (!MicrosoftManifest.oidc(options.variant()) || !manifest.variant(options.variant()).equals(variant)
                || variant.protocol() != Protocol.OIDC) {
            throw new ValidateException("Microsoft OIDC adapter requires a matching tenant-scoped variant");
        }
        this.options = options;
        var targets = variant.targets().resolve(options);
        var oauth = OAuth2ClientOptions.builder(options.clientId())
                .authorizationEndpoint(targets.authorization().getOrNull()).tokenEndpoint(targets.token().getOrNull())
                .redirectUri(options.redirectUri().getOrNull()).expectedIssuer(MicrosoftManifest.oidcIssuer(options))
                .pkceRequired(true).clientSecret(Endpoint.Authentication.CLIENT_SECRET_POST, options.credential())
                .build();
        client = OpenIdClientDriver.client(
                OpenIdClientOptions.builder(oauth).discoveryEndpoint(targets.discovery().getOrNull())
                        .jwkSetEndpoint(targets.jwks().getOrNull()).signingAlgorithm(JwaAlgorithm.RS256).build(),
                services,
                new JwkSetCodec(true)); // Both Entra clouds serve public signing keys as application/json.
        operations = new StandardAdapter(variant, options, Optional.empty(),
                List.of(
                        new StandardAdapter.Binding<>(OpenIdClientScheme.AUTHENTICATION, this::authorize),
                        new StandardAdapter.Binding<>(MicrosoftManifest.OIDC_TOKEN, this::exchange),
                        new StandardAdapter.Binding<>(OpenIdClientScheme.DISCOVERY,
                                (request, context, timeout) -> client
                                        .invoke(OpenIdClientScheme.DISCOVERY, request, context, timeout)),
                        new StandardAdapter.Binding<>(OpenIdClientScheme.JWK_SET, (request, context, timeout) -> client
                                .invoke(OpenIdClientScheme.JWK_SET, request, context, timeout))));
    }

    /**
     * Returns the four capabilities exposed by this tenant-scoped Entra OIDC variant.
     *
     * @return immutable manifest for authorization, code exchange, discovery and signing keys
     */
    @Override
    public Capability.Manifest manifest() {
        return operations.manifest();
    }

    /**
     * Dispatches an operation through the exact typed binding registered for this Source.
     *
     * @param capability the declared client capability
     * @param request    the typed request, or {@code null} for discovery and signing keys
     * @param context    immutable invocation context
     * @param timeout    shared operation deadline
     * @param <Q>        request type
     * @param <S>        success type
     * @return asynchronous result or a rejection when the capability or request is unsupported
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        return operations.invoke(capability, request, context, timeout);
    }

    /**
     * Encodes a registered OIDC code request with state, nonce and an S256 challenge.
     *
     * @param request typed input from the owner of the browser interaction
     * @param context immutable invocation context
     * @param timeout shared operation deadline
     * @return URL or a sanitized rejection before any credentials are loaded
     */
    private CompletionStage<Outcome<Url>> authorize(
            final AuthenticationRequest request,
            final Context context,
            final Timeout timeout) {
        var authorization = request.authorizationRequest();
        var scopes = authorization.scope().getOrNull();
        if (!options.clientId().equals(authorization.clientId())
                || !options.redirectUri().equals(authorization.redirectUri())
                || !authorization.state().filter(value -> !value.isBlank()).isPresent()
                || !request.nonce().filter(value -> !value.isBlank()).isPresent()
                || !authorization.codeChallenge().filter(value -> S256_CHALLENGE.matcher(value).matches()).isPresent()
                || !authorization.codeChallengeMethod().filter("S256"::equals).isPresent() || scopes == null
                || !scopes.values().containsAll(MicrosoftManifest.OIDC_SCOPES)
                || !options.scopes().containsAll(scopes.values())
                || !"query".equals(request.responseMode().orElse("query"))) {
            return rejected("Microsoft OIDC authentication does not match the registered code flow");
        }
        return client.invoke(OpenIdClientScheme.AUTHENTICATION, request, context, timeout);
    }

    /**
     * Exchanges only a code from the caller's previously claimed interaction.
     *
     * @param request code, exact registered callback and verifier with no credential overrides
     * @param context immutable invocation context
     * @param timeout shared operation deadline
     * @return decoded OIDC result, which the caller must verify before accepting an identity
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> exchange(
            final TokenRequest request,
            final Context context,
            final Timeout timeout) {
        if (!(request.grant() instanceof AuthorizationCodeGrant grant) || grant.clientId().isPresent()
                || !options.redirectUri().equals(grant.redirectUri())
                || !grant.codeVerifier().filter(value -> VERIFIER.matcher(value).matches()).isPresent()
                || !request.extensions().values().isEmpty()) {
            return rejected("Microsoft OIDC exchange requires a registered authorization-code grant");
        }
        return client.invoke(OAuth2ClientScheme.TOKEN, request, context, timeout);
    }

    /**
     * Rejects an input without recording code, nonce or credential values.
     *
     * @param message static diagnostic explanation
     * @param <T>     expected success value
     * @return completed rejected outcome
     */
    private static <T> CompletionStage<Outcome<T>> rejected(final String message) {
        return CompletableFuture.completedFuture(
                Outcome.rejected(new Outcome.Failure(ErrorCode._400, message, new JsonValue.ObjectValue(Map.of()))));
    }

}
