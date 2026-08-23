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
package org.miaixz.bus.auth.source.protocol.oidc;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Request;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.Scheme.Options;
import org.miaixz.bus.auth.guard.RedirectUriValidator;
import org.miaixz.bus.auth.guard.ScopeValidator;
import org.miaixz.bus.auth.guard.SecretGuard;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.shared.jose.JweService;
import org.miaixz.bus.auth.shared.jose.JwsService;
import org.miaixz.bus.auth.shared.jwt.JwtVerifier;
import org.miaixz.bus.auth.shared.pkce.PkceValidator;
import org.miaixz.bus.auth.source.SourceServices;
import org.miaixz.bus.auth.source.protocol.ProtocolDriver;
import org.miaixz.bus.auth.source.protocol.oauth2.codec.*;
import org.miaixz.bus.auth.source.protocol.oauth2.grant.AccessTokenIssuer;
import org.miaixz.bus.auth.source.protocol.oauth2.grant.AuthorizationCodeIssuer;
import org.miaixz.bus.auth.source.protocol.oauth2.grant.RefreshTokenRotator;
import org.miaixz.bus.auth.source.protocol.oauth2.grant.TokenMaterial;
import org.miaixz.bus.auth.source.protocol.oauth2.server.*;
import org.miaixz.bus.auth.source.protocol.oidc.codec.*;
import org.miaixz.bus.auth.source.protocol.oidc.server.*;
import org.miaixz.bus.auth.worker.SessionCoordinator;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.auth.worker.WorkerSlots;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Compiles one validated server-role OpenID Source configuration into OIDC and composed OAuth server services.
 * <p>
 * The driver assembles each service directly over the same external stores and resolvers. It never compiles and invokes
 * a nested OAuth runtime, never creates an OIDC-specific authorization-code cache, and exposes only operations whose
 * deployment endpoints are present in the frozen options.
 * </p>
 *
 * @author Kimi Liu
 */
public class OpenIdServerDriver implements ProtocolDriver<OpenIdServerOptions> {

    /**
     * Creates a stateless OpenID Provider driver.
     */
    public OpenIdServerDriver() {
        // No initialization required.
    }

    /**
     * Builds the ordered capability set represented by configured OIDC and composed OAuth endpoints.
     *
     * @param options validated OpenID Provider options
     * @return non-empty endpoint-accurate capability manifest
     */
    private static Capability.Manifest manifest(final OpenIdServerOptions options) {
        final List<Capability<?, ?>> capabilities = new ArrayList<>();
        if (options.oauth2Options().authorizationEndpoint().isPresent()) {
            capabilities.add(OpenIdServerScheme.AUTHENTICATION);
        }
        if (options.oauth2Options().tokenEndpoint().isPresent()) {
            capabilities.add(OAuth2ServerScheme.TOKEN);
        }
        if (options.oauth2Options().introspectionEndpoint().isPresent()) {
            capabilities.add(OAuth2ServerScheme.INTROSPECTION);
        }
        if (options.oauth2Options().revocationEndpoint().isPresent()) {
            capabilities.add(OAuth2ServerScheme.REVOCATION);
        }
        if (options.oauth2Options().deviceAuthorizationEndpoint().isPresent()) {
            capabilities.add(OAuth2ServerScheme.DEVICE_AUTHORIZATION);
        }
        if (options.oauth2Options().authorizationServerMetadataEndpoint().isPresent()) {
            capabilities.add(OAuth2ServerScheme.AUTHORIZATION_SERVER_METADATA);
        }
        if (options.discoveryEndpoint().isPresent()) {
            capabilities.add(OpenIdServerScheme.DISCOVERY);
        }
        if (options.jwkSetEndpoint().isPresent()) {
            capabilities.add(OpenIdServerScheme.JWK_SET);
        }
        if (options.userInfoEndpoint().isPresent()) {
            capabilities.add(OpenIdServerScheme.USERINFO);
        }
        if (options.endSessionEndpoint().isPresent()) {
            capabilities.add(OpenIdServerScheme.END_SESSION);
        }
        return new Capability.Manifest(capabilities);
    }

    /**
     * Tests whether an enabled inherited OAuth endpoint accepts a client-secret method.
     *
     * @param endpoint optional configured endpoint
     * @return whether client-secret loading is required
     */
    private static boolean usesClientSecret(final Endpoint endpoint) {
        return endpoint != null && (endpoint.authentication().contains(Endpoint.Authentication.CLIENT_SECRET_BASIC)
                || endpoint.authentication().contains(Endpoint.Authentication.CLIENT_SECRET_POST));
    }

    /**
     * Returns the OpenID Provider scheme bound to this driver.
     *
     * @return immutable OpenID Provider scheme
     */
    @Override
    public OpenIdServerScheme scheme() {
        return new OpenIdServerScheme();
    }

    /**
     * Narrows generic Source options to OpenID Connect server options.
     *
     * @param options generic Source options
     * @return validated OpenID Connect server options
     */
    @Override
    public OpenIdServerOptions require(final Options<?> options) {
        if (options instanceof OpenIdServerOptions value) {
            return value;
        }
        throw new ValidateException("OpenID Connect server driver requires OpenIdServerOptions");
    }

    /**
     * Derives only the project slots used by enabled OpenID and composed OAuth endpoints.
     *
     * @param source  Source configuration
     * @param options validated OpenID Connect server options
     * @return exact project integration slots
     */
    @Override
    public WorkerSlots slots(final Source source, final OpenIdServerOptions options) {
        final OAuth2ServerOptions oauth = options.oauth2Options();
        WorkerSlots slots = WorkerSlots.none();
        if (oauth.authorizationEndpoint().isPresent()) {
            slots = slots.with(WorkerSlots.Slot.CONSUMER, WorkerSlots.Slot.CONSENT, WorkerSlots.Slot.SESSION);
        }
        if (oauth.tokenEndpoint().isPresent()) {
            slots = slots.with(
                    WorkerSlots.Slot.CONSUMER,
                    WorkerSlots.Slot.RESOURCE,
                    WorkerSlots.Slot.ATTRIBUTE,
                    WorkerSlots.Slot.KEY);
        }
        if (oauth.introspectionEndpoint().isPresent() || oauth.revocationEndpoint().isPresent()
                || oauth.deviceAuthorizationEndpoint().isPresent()) {
            slots = slots.with(WorkerSlots.Slot.CONSUMER);
        }
        if (usesClientSecret(oauth.tokenEndpoint().getOrNull())
                || usesClientSecret(oauth.introspectionEndpoint().getOrNull())
                || usesClientSecret(oauth.revocationEndpoint().getOrNull())
                || usesClientSecret(oauth.deviceAuthorizationEndpoint().getOrNull())) {
            slots = slots.with(WorkerSlots.Slot.CONSUMER_VERIFIER);
        }
        if (options.jwkSetEndpoint().isPresent()) {
            slots = slots.with(WorkerSlots.Slot.KEY);
        }
        if (options.userInfoEndpoint().isPresent()) {
            slots = slots.with(WorkerSlots.Slot.ATTRIBUTE);
        }
        if (options.endSessionEndpoint().isPresent()) {
            slots = slots.with(WorkerSlots.Slot.CONSUMER, WorkerSlots.Slot.SESSION);
        }
        if (oauth.federatedJwtEnabled()) {
            slots = slots.with(WorkerSlots.Slot.FEDERATION, WorkerSlots.Slot.KEY);
        }
        return slots;
    }

    /**
     * Declares the framework state, JSON, and security services used by the OpenID Provider.
     *
     * @param source  Source configuration
     * @param options validated OpenID Connect server options
     * @return exact framework dependencies
     */
    @Override
    public Dependencies dependencies(final Source source, final OpenIdServerOptions options) {
        return Dependencies.of(
                Dependencies.Service.AUTHORIZATION_CODE_CACHE,
                Dependencies.Service.DEVICE_CODE_CACHE,
                Dependencies.Service.AUTHORIZATION_CACHE,
                Dependencies.Service.ACCESS_TOKEN_CACHE,
                Dependencies.Service.REFRESH_TOKEN_CACHE,
                Dependencies.Service.SESSION_CACHE,
                Dependencies.Service.ID_TOKEN_CACHE,
                Dependencies.Service.REPLAY_CACHE,
                Dependencies.Service.POLICIES);
    }

    /**
     * Consumes typed options and assembles one endpoint-accurate OpenID Provider Source runtime.
     *
     * @param prepared one-time validated Source graph, Options and dependency declaration
     * @param services capability-limited Source services
     * @return immutable executable Source worker
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if Source routing, options, or signing policy is invalid
     */
    @Override
    public SourceWorker compile(final Prepared<OpenIdServerOptions> prepared, final SourceServices services) {
        Assert.notNull(prepared, "OpenID Provider Source preparation must not be null");
        Assert.notNull(services, "OpenID Provider execution services must not be null");
        final Blueprint.SourceEntry entry = prepared.entry();
        final Provider provider = prepared.provider();
        final Library library = prepared.library();
        final Source source = entry.resource();
        if (!scheme().id().equals(source.getType()) || !supports(source.getProtocol())
                || !provider.getId().equals(source.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("OpenID Connect server driver requires a matching Source configuration");
        }
        final OpenIdServerOptions options = prepared.options();
        if (!services.policies().require(Protocol.OIDC).algorithms()
                .contains(options.idTokenSigningAlgorithm().name())) {
            throw new ValidateException("OpenID Connect signing algorithm is not permitted by the security rules");
        }

        final ScopeValidator scopeValidator = new ScopeValidator();
        final RedirectUriValidator redirectUriValidator = new RedirectUriValidator();
        final PkceValidator pkceValidator = PkceValidator.strict(new SecretGuard());
        final JwsService jwsService = new JwsService(new org.miaixz.bus.auth.guard.AlgorithmGuard(),
                Set.of(options.idTokenSigningAlgorithm().name()));
        final JweService dormantJweService = new JweService(new org.miaixz.bus.auth.guard.AlgorithmGuard(),
                Set.of(JwaAlgorithm.RSA_OAEP_256.name()), Set.of(JwaAlgorithm.A256GCM.name()));
        final IdTokenCodec idTokenCodec = new IdTokenCodec(new JwtVerifier(jwsService, dormantJweService));
        final OAuth2ErrorMapper oauthErrorMapper = new OAuth2ErrorMapper();
        final OpenIdErrorMapper openIdErrorMapper = new OpenIdErrorMapper();
        final Map<Capability<?, ?>, EndpointHandler> endpoints = new LinkedHashMap<>();
        final SessionCoordinator sessions = new SessionCoordinator(source.getId(), services.sessionCache(),
                services.sessionWorker());
        final SubjectIssuer subjectIssuer = new SubjectIssuer(source.getId(), options, services);
        if (options.oauth2Options().authorizationEndpoint().isPresent()) {
            final AuthorizationCodeIssuer issuer = new AuthorizationCodeIssuer(source.getId(), options.oauth2Options(),
                    services, redirectUriValidator, scopeValidator,
                    (subject, consumer, binding, context, timeout) -> subjectIssuer
                            .issue(subject, consumer, context, timeout).thenApply(outcome -> switch (outcome) {
                                case Outcome.Succeeded<String> success -> Outcome
                                        .succeeded(binding.withSubject(success.value()));
                                case Outcome.Rejected<String> rejected -> Outcome.rejected(rejected.failure());
                                case Outcome.Failed<String> failed -> Outcome.failed(failed.failure());
                                default -> throw new IllegalStateException("Unsupported Outcome implementation");
                            }));
            final AuthenticationEndpoint endpoint = new AuthenticationEndpoint(
                    new AuthenticationRequestDecoder(new AuthorizationRequestDecoder()),
                    new AuthenticationService(issuer, sessions), new AuthorizationResponseEncoder(), oauthErrorMapper);
            endpoints.put(OpenIdServerScheme.AUTHENTICATION, endpoint::handle);
        }
        if (options.oauth2Options().tokenEndpoint().isPresent()) {
            final TokenMaterial tokenMaterial = new TokenMaterial(source.getId(), services);
            final IdTokenIssuer idTokenIssuer = new IdTokenIssuer(options, services, idTokenCodec);
            final AccessTokenIssuer issuer = new AccessTokenIssuer(source.getId(), options.oauth2Options(), services,
                    scopeValidator, pkceValidator, idTokenIssuer, tokenMaterial);
            final RefreshTokenRotator rotator = new RefreshTokenRotator(source.getId(), options.oauth2Options(),
                    services, scopeValidator, issuer, tokenMaterial);
            final TokenEndpoint endpoint = new TokenEndpoint(new TokenRequestDecoder(),
                    new OAuth2ClientAuthenticator(options.oauth2Options(), services), new TokenService(issuer, rotator),
                    new TokenResponseEncoder(), oauthErrorMapper);
            endpoints.put(OAuth2ServerScheme.TOKEN, endpoint::handle);
        }
        if (options.oauth2Options().introspectionEndpoint().isPresent()) {
            final IntrospectionEndpoint endpoint = new IntrospectionEndpoint(new IntrospectionCodec(),
                    new OAuth2ClientAuthenticator(options.oauth2Options().introspectionEndpoint().getOrNull(),
                            services),
                    new IntrospectionService(source.getId(), options.oauth2Options(), services), oauthErrorMapper);
            endpoints.put(OAuth2ServerScheme.INTROSPECTION, endpoint::handle);
        }
        if (options.oauth2Options().revocationEndpoint().isPresent()) {
            final RevocationEndpoint endpoint = new RevocationEndpoint(new RevocationRequestDecoder(),
                    new OAuth2ClientAuthenticator(options.oauth2Options().revocationEndpoint().getOrNull(), services),
                    new RevocationService(source.getId(), services), oauthErrorMapper);
            endpoints.put(OAuth2ServerScheme.REVOCATION, endpoint::handle);
        }
        if (options.oauth2Options().deviceAuthorizationEndpoint().isPresent()) {
            final DeviceAuthorizationEndpoint endpoint = new DeviceAuthorizationEndpoint(new DeviceAuthorizationCodec(),
                    new OAuth2ClientAuthenticator(options.oauth2Options().deviceAuthorizationEndpoint().getOrNull(),
                            services),
                    new DeviceAuthorizationService(source.getId(), options.oauth2Options(), services, scopeValidator),
                    oauthErrorMapper);
            endpoints.put(OAuth2ServerScheme.DEVICE_AUTHORIZATION, endpoint::handle);
        }
        if (options.oauth2Options().authorizationServerMetadataEndpoint().isPresent()) {
            final AuthorizationServerMetadataEndpoint endpoint = new AuthorizationServerMetadataEndpoint(
                    new AuthorizationServerMetadataCodec(),
                    new AuthorizationServerMetadataService(options.oauth2Options()), oauthErrorMapper);
            endpoints.put(OAuth2ServerScheme.AUTHORIZATION_SERVER_METADATA, endpoint::handle);
        }
        if (options.discoveryEndpoint().isPresent()) {
            final DiscoveryEndpoint endpoint = new DiscoveryEndpoint(new OpenIdProviderMetadataCodec(),
                    new DiscoveryService(options), openIdErrorMapper);
            endpoints.put(OpenIdServerScheme.DISCOVERY, endpoint::handle);
        }
        if (options.jwkSetEndpoint().isPresent()) {
            final JwkSetEndpoint endpoint = new JwkSetEndpoint(new JwkSetCodec(), new JwkSetService(options, services),
                    openIdErrorMapper);
            endpoints.put(OpenIdServerScheme.JWK_SET, endpoint::handle);
        }
        if (options.userInfoEndpoint().isPresent()) {
            final UserInfoEndpoint endpoint = new UserInfoEndpoint(new UserInfoCodec(),
                    new UserInfoService(source.getId(), options, services), openIdErrorMapper);
            endpoints.put(OpenIdServerScheme.USERINFO, endpoint::handle);
        }
        if (options.endSessionEndpoint().isPresent()) {
            final EndSessionEndpoint endpoint = new EndSessionEndpoint(new EndSessionRequestCodec(),
                    new EndSessionService(options, services, sessions), openIdErrorMapper);
            endpoints.put(OpenIdServerScheme.END_SESSION, endpoint::handle);
        }
        return new CompiledServer(manifest(options), endpoints);
    }

    /**
     * Adapts one compiled HTTP endpoint to the common Source-worker invocation shape.
     *
     * @author Kimi Liu
     */
    @FunctionalInterface
    private interface EndpointHandler {

        /**
         * Handles one validated endpoint request.
         *
         * @param request incoming HTTP request
         * @param context immutable invocation context
         * @param timeout shared operation timeout
         * @return asynchronous HTTP response
         */
        CompletionStage<Response> handle(Request request, Context context, Timeout timeout);

    }

    /**
     * Routes the exact enabled OpenID Provider capabilities to their compiled HTTP endpoints.
     *
     * @author Kimi Liu
     */
    private static final class CompiledServer implements SourceWorker {

        /**
         * Endpoint-accurate immutable capability manifest.
         */
        private final Capability.Manifest manifest;

        /**
         * Exact endpoint handler indexed by its declared capability.
         */
        private final Map<Capability<?, ?>, EndpointHandler> endpoints;

        /**
         * Creates one compiled OpenID Provider Source runtime from its exact manifest and endpoint handlers.
         *
         * @param manifest  endpoint-accurate capability manifest
         * @param endpoints endpoint handlers keyed by exact declared capability
         */
        private CompiledServer(final Capability.Manifest manifest,
                final Map<Capability<?, ?>, EndpointHandler> endpoints) {
            this.manifest = Assert.notNull(manifest, "OpenID Provider manifest must not be null");
            this.endpoints = Map.copyOf(Assert.notNull(endpoints, "OpenID Connect endpoints must not be null"));
        }

        /**
         * Creates a completed rejection for an undeclared or request-shape-mismatched capability.
         *
         * @param <S> expected success type
         * @return completed rejected outcome
         */
        private static <S> CompletionStage<Outcome<S>> rejected() {
            return CompletableFuture.completedFuture(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._400,
                                    "OpenID Provider does not implement the requested capability",
                                    new JsonValue.ObjectValue(Map.of()))));
        }

        /**
         * Returns the exact endpoint-backed capability manifest.
         *
         * @return immutable manifest
         */
        @Override
        public Capability.Manifest manifest() {
            return manifest;
        }

        /**
         * Invokes one exact declared OpenID server-role Source capability.
         *
         * @param capability exact declared capability
         * @param request    exact standard request or {@code null} for metadata resources
         * @param context    immutable invocation context
         * @param timeout    shared end-to-end timeout
         * @param <Q>        request type
         * @param <S>        success type
         * @return delegated typed outcome or an unsupported-capability rejection
         */
        @Override
        public <Q, S> CompletionStage<Outcome<S>> invoke(
                final Capability<Q, S> capability,
                final Q request,
                final Context context,
                final Timeout timeout) {
            Assert.notNull(capability, "OpenID Provider capability must not be null");
            Assert.notNull(context, "OpenID Provider context must not be null");
            Assert.notNull(timeout, "OpenID Provider timeout must not be null");
            if (!manifest.capabilities().contains(capability)) {
                return rejected();
            }
            final EndpointHandler endpoint = endpoints.get(capability);
            if (endpoint == null || !(request instanceof Request httpRequest)) {
                return rejected();
            }
            return endpoint.handle(httpRequest, context, timeout)
                    .thenApply(response -> Outcome.succeeded(capability.responseType().cast(response)));
        }

    }

}
