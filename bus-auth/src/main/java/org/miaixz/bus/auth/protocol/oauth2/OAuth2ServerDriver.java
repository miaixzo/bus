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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.guard.RedirectUriValidator;
import org.miaixz.bus.auth.guard.ScopeValidator;
import org.miaixz.bus.auth.guard.SecretGuard;
import org.miaixz.bus.auth.protocol.oauth2.codec.*;
import org.miaixz.bus.auth.protocol.oauth2.grant.AccessTokenIssuer;
import org.miaixz.bus.auth.protocol.oauth2.grant.AuthorizationCodeIssuer;
import org.miaixz.bus.auth.protocol.oauth2.grant.RefreshTokenRotator;
import org.miaixz.bus.auth.protocol.oauth2.grant.TokenMaterial;
import org.miaixz.bus.auth.protocol.oauth2.server.*;
import org.miaixz.bus.auth.shared.pkce.PkceValidator;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.auth.worker.WorkerSlots;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Compiles one validated server-role OAuth 2.x Source registration into typed authorization-server services.
 *
 * @author Kimi Liu
 */
public final class OAuth2ServerDriver implements SourceDriver<OAuth2ServerOptions> {

    /**
     * Immutable OAuth 2.x Provider scheme shared by compiled registrations.
     */
    private final OAuth2ServerScheme scheme;

    /**
     * Creates a driver with the deterministic OAuth 2.x Provider scheme.
     */
    public OAuth2ServerDriver() {
        this.scheme = new OAuth2ServerScheme();
    }

    /**
     * Builds the exact ordered capability set represented by configured Provider endpoints.
     *
     * @param options validated Provider options
     * @return non-empty endpoint-accurate capability manifest
     */
    private static Capability.Manifest manifest(final OAuth2ServerOptions options) {
        final List<Capability<?, ?>> capabilities = new ArrayList<>();
        if (options.authorizationEndpoint().isPresent()) {
            capabilities.add(OAuth2ServerScheme.AUTHORIZATION);
        }
        if (options.tokenEndpoint().isPresent()) {
            capabilities.add(OAuth2ServerScheme.TOKEN);
        }
        if (options.introspectionEndpoint().isPresent()) {
            capabilities.add(OAuth2ServerScheme.INTROSPECTION);
        }
        if (options.revocationEndpoint().isPresent()) {
            capabilities.add(OAuth2ServerScheme.REVOCATION);
        }
        if (options.deviceAuthorizationEndpoint().isPresent()) {
            capabilities.add(OAuth2ServerScheme.DEVICE_AUTHORIZATION);
        }
        if (options.authorizationServerMetadataEndpoint().isPresent()) {
            capabilities.add(OAuth2ServerScheme.AUTHORIZATION_SERVER_METADATA);
        }
        return new Capability.Manifest(capabilities);
    }

    /**
     * Tests whether an enabled endpoint accepts either client-secret method.
     *
     * @param endpoint optional configured endpoint
     * @return whether client-secret loading is required
     */
    private static boolean usesClientSecret(final Endpoint endpoint) {
        return endpoint != null && (endpoint.authentication().contains(Endpoint.Authentication.CLIENT_SECRET_BASIC)
                || endpoint.authentication().contains(Endpoint.Authentication.CLIENT_SECRET_POST));
    }

    /**
     * Returns the OAuth 2.x server scheme bound to this driver.
     *
     * @return immutable OAuth 2.x Provider scheme
     */
    @Override
    public OAuth2ServerScheme scheme() {
        return scheme;
    }

    @Override
    public OAuth2ServerOptions require(final Options<?> options) {
        if (options instanceof OAuth2ServerOptions value) {
            return value;
        }
        throw new ValidateException("OAuth 2.x server driver requires OAuth2ServerOptions");
    }

    @Override
    public WorkerSlots slots(final Source source, final OAuth2ServerOptions options) {
        WorkerSlots slots = WorkerSlots.none();
        if (options.authorizationEndpoint().isPresent()) {
            slots = slots.with(WorkerSlots.Slot.CONSUMER, WorkerSlots.Slot.CONSENT);
        }
        if (options.tokenEndpoint().isPresent()) {
            slots = slots.with(WorkerSlots.Slot.CONSUMER, WorkerSlots.Slot.RESOURCE);
        }
        if (options.introspectionEndpoint().isPresent() || options.revocationEndpoint().isPresent()
                || options.deviceAuthorizationEndpoint().isPresent()) {
            slots = slots.with(WorkerSlots.Slot.CONSUMER);
        }
        if (usesClientSecret(options.tokenEndpoint().getOrNull())
                || usesClientSecret(options.introspectionEndpoint().getOrNull())
                || usesClientSecret(options.revocationEndpoint().getOrNull())
                || usesClientSecret(options.deviceAuthorizationEndpoint().getOrNull())) {
            slots = slots.with(WorkerSlots.Slot.SECRET);
        }
        return slots;
    }

    @Override
    public Dependencies dependencies(final Source source, final OAuth2ServerOptions options) {
        return Dependencies.of(
                Dependencies.Service.JSON_PROVIDER,
                Dependencies.Service.AUTHORIZATION_CODE_CACHE,
                Dependencies.Service.DEVICE_CODE_CACHE,
                Dependencies.Service.AUTHORIZATION_CACHE,
                Dependencies.Service.ACCESS_TOKEN_CACHE,
                Dependencies.Service.REFRESH_TOKEN_CACHE,
                Dependencies.Service.SECURITY_BASELINE);
    }

    /**
     * Consumes typed options and assembles one endpoint-accurate OAuth 2.x server-role Source runtime.
     *
     * @param prepared one-time validated Source graph, Options and dependency declaration
     * @param services dependency-scoped runtime services
     * @return executable immutable Source worker
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if the registration routing fields do not match this driver
     */
    @Override
    public SourceWorker compile(final Prepared<OAuth2ServerOptions> prepared, final DriverServices services) {
        Assert.notNull(prepared, "OAuth 2.x Provider preparation must not be null");
        Assert.notNull(services, "OAuth 2.x Provider execution services must not be null");
        final Registration.SourceEntry record = prepared.registration();
        final Provider provider = prepared.provider();
        final Library library = prepared.library();
        final Source source = record.resource();
        if (!scheme().id().equals(source.getType()) || !supports(source.getProtocol())
                || !provider.getId().equals(source.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("OAuth 2.x server driver requires a matching Source registration");
        }
        final OAuth2ServerOptions options = prepared.options();

        final ScopeValidator scopeValidator = new ScopeValidator();
        final OAuth2ErrorMapper errorMapper = new OAuth2ErrorMapper(services.jsonProvider());
        final Map<Capability<?, ?>, EndpointHandler> endpoints = new LinkedHashMap<>();
        if (options.authorizationEndpoint().isPresent()) {
            final AuthorizationCodeIssuer issuer = new AuthorizationCodeIssuer(source.getId(), options, services,
                    new RedirectUriValidator(), scopeValidator);
            final AuthorizationEndpoint endpoint = new AuthorizationEndpoint(new AuthorizationRequestDecoder(),
                    new AuthorizationService(issuer), new AuthorizationResponseEncoder(), errorMapper);
            endpoints.put(OAuth2ServerScheme.AUTHORIZATION, endpoint::handle);
        }
        if (options.tokenEndpoint().isPresent()) {
            final TokenMaterial tokenMaterial = new TokenMaterial(source.getId(), services);
            final AccessTokenIssuer issuer = new AccessTokenIssuer(source.getId(), options, services, scopeValidator,
                    PkceValidator.strict(new SecretGuard()), AccessTokenIssuer.Augmenter.passThrough(), tokenMaterial);
            final RefreshTokenRotator rotator = new RefreshTokenRotator(source.getId(), options, services,
                    scopeValidator, issuer, tokenMaterial);
            final TokenEndpoint endpoint = new TokenEndpoint(new TokenRequestDecoder(),
                    new OAuth2ClientAuthenticator(options.tokenEndpoint().getOrNull(), services),
                    new TokenService(issuer, rotator), new TokenResponseEncoder(services.jsonProvider()), errorMapper);
            endpoints.put(OAuth2ServerScheme.TOKEN, endpoint::handle);
        }
        if (options.introspectionEndpoint().isPresent()) {
            final IntrospectionEndpoint endpoint = new IntrospectionEndpoint(
                    new IntrospectionCodec(services.jsonProvider()),
                    new OAuth2ClientAuthenticator(options.introspectionEndpoint().getOrNull(), services),
                    new IntrospectionService(source.getId(), options, services), errorMapper);
            endpoints.put(OAuth2ServerScheme.INTROSPECTION, endpoint::handle);
        }
        if (options.revocationEndpoint().isPresent()) {
            final RevocationEndpoint endpoint = new RevocationEndpoint(new RevocationRequestDecoder(),
                    new OAuth2ClientAuthenticator(options.revocationEndpoint().getOrNull(), services),
                    new RevocationService(source.getId(), services), errorMapper);
            endpoints.put(OAuth2ServerScheme.REVOCATION, endpoint::handle);
        }
        if (options.deviceAuthorizationEndpoint().isPresent()) {
            final DeviceAuthorizationEndpoint endpoint = new DeviceAuthorizationEndpoint(
                    new DeviceAuthorizationCodec(services.jsonProvider()),
                    new OAuth2ClientAuthenticator(options.deviceAuthorizationEndpoint().getOrNull(), services),
                    new DeviceAuthorizationService(source.getId(), options, services, scopeValidator), errorMapper);
            endpoints.put(OAuth2ServerScheme.DEVICE_AUTHORIZATION, endpoint::handle);
        }
        if (options.authorizationServerMetadataEndpoint().isPresent()) {
            final AuthorizationServerMetadataEndpoint endpoint = new AuthorizationServerMetadataEndpoint(
                    new AuthorizationServerMetadataCodec(services.jsonProvider()),
                    new AuthorizationServerMetadataService(options), errorMapper);
            endpoints.put(OAuth2ServerScheme.AUTHORIZATION_SERVER_METADATA, endpoint::handle);
        }
        return new CompiledServer(manifest(options), endpoints);
    }

    /**
     * Routes the exact enabled OAuth 2.x Provider capabilities to compiled HTTP endpoints.
     *
     * @author Kimi Liu
     */
    private static final class CompiledServer implements SourceWorker {

        /**
         * Endpoint-accurate immutable capability manifest.
         */
        private final Capability.Manifest manifest;

        /** Exact endpoint handler indexed by its declared capability. */
        private final Map<Capability<?, ?>, EndpointHandler> endpoints;

        /**
         * Creates one compiled authorization-server Source runtime from its exact manifest and HTTP endpoints.
         *
         * @param manifest  endpoint-accurate manifest
         * @param endpoints endpoint handlers keyed by the exact declared capability
         */
        private CompiledServer(final Capability.Manifest manifest,
                final Map<Capability<?, ?>, EndpointHandler> endpoints) {
            this.manifest = Assert.notNull(manifest, "OAuth 2.x Provider manifest must not be null");
            this.endpoints = Map.copyOf(Assert.notNull(endpoints, "OAuth 2.x endpoints must not be null"));
        }

        /**
         * Creates an already completed rejection for an undeclared or mismatched capability.
         *
         * @param <S> expected success type
         * @return completed rejected outcome
         */
        private static <S> CompletionStage<Outcome<S>> rejected() {
            return CompletableFuture.completedFuture(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._400,
                                    "OAuth 2.x Provider does not implement the requested capability",
                                    new JsonValue.ObjectValue(Map.of()))));
        }

        /**
         * Returns the exact capabilities backed by configured endpoints.
         *
         * @return immutable capability manifest
         */
        @Override
        public Capability.Manifest manifest() {
            return manifest;
        }

        /**
         * Invokes one exact declared OAuth 2.x server-role Source capability.
         *
         * @param capability exact declared capability
         * @param request    exact standard request or {@code null} for metadata
         * @param context    immutable invocation context
         * @param timeout    shared end-to-end time budget
         * @param <Q>        request type
         * @param <S>        success type
         * @return delegated typed outcome or rejected unsupported capability
         */
        @Override
        public <Q, S> CompletionStage<Outcome<S>> invoke(
                final Capability<Q, S> capability,
                final Q request,
                final Context context,
                final Timeout.Budget timeout) {
            Assert.notNull(capability, "OAuth 2.x Provider capability must not be null");
            Assert.notNull(context, "OAuth 2.x Provider context must not be null");
            Assert.notNull(timeout, "OAuth 2.x Provider time budget must not be null");
            if (!manifest.capabilities().contains(capability)) {
                return rejected();
            }
            final EndpointHandler endpoint = endpoints.get(capability);
            if (endpoint == null || !(request instanceof HttpRequest httpRequest)) {
                return rejected();
            }
            return endpoint.handle(httpRequest, context, timeout)
                    .thenApply(response -> Outcome.succeeded(capability.responseType().cast(response)));
        }

    }

    /** Adapts one compiled HTTP endpoint to the common Source-worker invocation shape. */
    @FunctionalInterface
    private interface EndpointHandler {

        /**
         * Handles one validated endpoint request.
         *
         * @param request incoming HTTP request
         * @param context immutable invocation context
         * @param timeout shared operation budget
         * @return asynchronous HTTP response
         */
        CompletionStage<HttpResponse> handle(HttpRequest request, Context context, Timeout.Budget timeout);

    }

}
