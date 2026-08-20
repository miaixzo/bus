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
package org.miaixz.bus.auth.protocol.oidc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.guard.RedirectUriValidator;
import org.miaixz.bus.auth.guard.ScopeValidator;
import org.miaixz.bus.auth.guard.SecretGuard;
import org.miaixz.bus.auth.protocol.oauth2.DeviceAuthorizationRequest;
import org.miaixz.bus.auth.protocol.oauth2.IntrospectionRequest;
import org.miaixz.bus.auth.protocol.oauth2.RevocationRequest;
import org.miaixz.bus.auth.protocol.oauth2.TokenRequest;
import org.miaixz.bus.auth.protocol.oauth2.grant.AccessTokenIssuer;
import org.miaixz.bus.auth.protocol.oauth2.grant.AuthorizationCodeIssuer;
import org.miaixz.bus.auth.protocol.oauth2.grant.RefreshTokenRotator;
import org.miaixz.bus.auth.protocol.oauth2.server.*;
import org.miaixz.bus.auth.protocol.oidc.codec.IdTokenCodec;
import org.miaixz.bus.auth.protocol.oidc.server.*;
import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.shared.jose.JweService;
import org.miaixz.bus.auth.shared.jose.JwsService;
import org.miaixz.bus.auth.shared.jwt.JwtVerifier;
import org.miaixz.bus.auth.shared.pkce.PkceValidator;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Compiles one validated server-role OpenID Source registration into OIDC and composed OAuth server services.
 * <p>
 * The driver assembles each service directly over the same external stores and resolvers. It never compiles and invokes
 * a nested OAuth runtime, never creates an OIDC-specific authorization-code cache, and exposes only operations whose
 * deployment endpoints are present in the frozen options.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OpenIdProviderDriver implements SourceDriver<OpenIdServerOptions> {

    /**
     * Creates a stateless OpenID Provider driver.
     */
    public OpenIdProviderDriver() {
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
            capabilities.add(OpenIdServerScheme.TOKEN);
        }
        if (options.oauth2Options().introspectionEndpoint().isPresent()) {
            capabilities.add(OpenIdServerScheme.INTROSPECTION);
        }
        if (options.oauth2Options().revocationEndpoint().isPresent()) {
            capabilities.add(OpenIdServerScheme.REVOCATION);
        }
        if (options.oauth2Options().deviceAuthorizationEndpoint().isPresent()) {
            capabilities.add(OpenIdServerScheme.DEVICE_AUTHORIZATION);
        }
        if (options.oauth2Options().authorizationServerMetadataEndpoint().isPresent()) {
            capabilities.add(OpenIdServerScheme.AUTHORIZATION_SERVER_METADATA);
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
     * Returns the OpenID Provider scheme bound to this driver.
     *
     * @return immutable OpenID Provider scheme
     */
    @Override
    public OpenIdServerScheme scheme() {
        return new OpenIdServerScheme();
    }

    @Override
    public OpenIdServerOptions require(final Options<?> options) {
        if (options instanceof OpenIdServerOptions value) {
            return value;
        }
        throw new ValidateException("OpenID Connect server driver requires OpenIdServerOptions");
    }

    /**
     * Consumes typed options and assembles one endpoint-accurate OpenID Provider Source runtime.
     *
     * @param record   validated complete server-role Source registration
     * @param library  resolved Library owned by the Provider
     * @param services externally owned runtime dependencies
     * @return immutable executable Source worker
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if registration routing, options, or signing policy is invalid
     */
    @Override
    public SourceWorker compile(
            final Registration.Record<Source> record,
            final Provider provider,
            final Library library,
            final ExecutionServices services) {
        Assert.notNull(record, "OpenID Connect Provider registration must not be null");
        Assert.notNull(library, "OpenID Connect Provider Library must not be null");
        Assert.notNull(services, "OpenID Connect Provider execution services must not be null");
        final Source source = record.resource();
        if (!scheme().id().equals(source.getType()) || !supports(source.getProtocol())
                || !provider.getId().equals(source.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("OpenID Connect server driver requires a matching Source registration");
        }
        final OpenIdServerOptions options = require(source.getOptions());
        if (!services.securityBaseline().require(Protocol.OIDC).algorithms()
                .contains(options.idTokenSigningAlgorithm().name())) {
            throw new ValidateException("OpenID Connect signing algorithm is not enabled by the security baseline");
        }

        final ScopeValidator scopeValidator = new ScopeValidator();
        final RedirectUriValidator redirectUriValidator = new RedirectUriValidator();
        final PkceValidator pkceValidator = PkceValidator.strict(new SecretGuard());
        final JwsService jwsService = new JwsService(services.jsonProvider(),
                services.securityBaseline().algorithmGuard(), Set.of(options.idTokenSigningAlgorithm().name()));
        final JweService dormantJweService = new JweService(services.jsonProvider(),
                services.securityBaseline().algorithmGuard(), Set.of(JwaAlgorithm.RSA_OAEP_256.name()),
                Set.of(JwaAlgorithm.A256GCM.name()));
        final IdTokenCodec idTokenCodec = new IdTokenCodec(
                new JwtVerifier(services.jsonProvider(), jwsService, dormantJweService));
        final AuthorizationCodeIssuer codeIssuer = new AuthorizationCodeIssuer(source.getId(), options.oauth2Options(),
                services, redirectUriValidator, scopeValidator);
        final IdTokenIssuer idTokenIssuer = new IdTokenIssuer(options, services, idTokenCodec);
        final AccessTokenIssuer accessTokenIssuer = new AccessTokenIssuer(source.getId(), options.oauth2Options(),
                services, scopeValidator, pkceValidator, idTokenIssuer);
        final RefreshTokenRotator refreshTokenRotator = new RefreshTokenRotator(source.getId(), options.oauth2Options(),
                services, scopeValidator, accessTokenIssuer);
        return new CompiledProvider(manifest(options), new AuthenticationService(codeIssuer),
                new TokenService(accessTokenIssuer, refreshTokenRotator),
                new IntrospectionService(source.getId(), options.oauth2Options(), services),
                new RevocationService(source.getId(), services),
                new DeviceAuthorizationService(source.getId(), options.oauth2Options(), services, scopeValidator),
                new AuthorizationServerMetadataService(options.oauth2Options()), new DiscoveryService(options),
                new JwkSetService(options, services), new UserInfoService(source.getId(), options, services),
                new EndSessionService(source.getId(), options, services, idTokenCodec));
    }

    /**
     * Routes the exact enabled OpenID Provider capabilities to their single typed service owners.
     *
     * @author Kimi Liu
     */
    private static final class CompiledProvider implements SourceWorker {

        /**
         * Endpoint-accurate immutable capability manifest.
         */
        private final Capability.Manifest manifest;

        /**
         * OIDC Authentication Code Flow operation.
         */
        private final AuthenticationService authentication;

        /**
         * Composed OAuth token operation with OIDC ID Token augmentation.
         */
        private final TokenService token;

        /**
         * Composed OAuth token introspection operation.
         */
        private final IntrospectionService introspection;

        /**
         * Composed OAuth token revocation operation.
         */
        private final RevocationService revocation;

        /**
         * Composed OAuth device authorization operation.
         */
        private final DeviceAuthorizationService deviceAuthorization;

        /**
         * Composed OAuth Authorization Server Metadata operation.
         */
        private final AuthorizationServerMetadataService authorizationServerMetadata;

        /**
         * OpenID Provider Discovery operation.
         */
        private final DiscoveryService discovery;

        /**
         * Public signing-key publication operation.
         */
        private final JwkSetService jwkSet;

        /**
         * Bearer-authenticated UserInfo operation.
         */
        private final UserInfoService userInfo;

        /**
         * RP-Initiated Logout operation.
         */
        private final EndSessionService endSession;

        /**
         * Creates one compiled OpenID Provider Source runtime from its exact manifest and service owners.
         *
         * @param manifest                    endpoint-accurate capability manifest
         * @param authentication              OIDC Authentication service
         * @param token                       composed OAuth token service
         * @param introspection               OAuth introspection service
         * @param revocation                  OAuth revocation service
         * @param deviceAuthorization         OAuth device authorization service
         * @param authorizationServerMetadata OAuth metadata service
         * @param discovery                   OIDC Discovery service
         * @param jwkSet                      public JWK Set service
         * @param userInfo                    OIDC UserInfo service
         * @param endSession                  RP-Initiated Logout service
         */
        private CompiledProvider(final Capability.Manifest manifest, final AuthenticationService authentication,
                final TokenService token, final IntrospectionService introspection, final RevocationService revocation,
                final DeviceAuthorizationService deviceAuthorization,
                final AuthorizationServerMetadataService authorizationServerMetadata, final DiscoveryService discovery,
                final JwkSetService jwkSet, final UserInfoService userInfo, final EndSessionService endSession) {
            this.manifest = Assert.notNull(manifest, "OpenID Connect Provider manifest must not be null");
            this.authentication = Assert
                    .notNull(authentication, "OpenID Connect Authentication service must not be null");
            this.token = Assert.notNull(token, "OpenID Connect token service must not be null");
            this.introspection = Assert
                    .notNull(introspection, "OpenID Connect OAuth introspection service must not be null");
            this.revocation = Assert.notNull(revocation, "OpenID Connect OAuth revocation service must not be null");
            this.deviceAuthorization = Assert
                    .notNull(deviceAuthorization, "OpenID Connect OAuth device authorization service must not be null");
            this.authorizationServerMetadata = Assert
                    .notNull(authorizationServerMetadata, "OpenID Connect OAuth metadata service must not be null");
            this.discovery = Assert.notNull(discovery, "OpenID Connect Discovery service must not be null");
            this.jwkSet = Assert.notNull(jwkSet, "OpenID Connect JWK Set service must not be null");
            this.userInfo = Assert.notNull(userInfo, "OpenID Connect UserInfo service must not be null");
            this.endSession = Assert.notNull(endSession, "OpenID Connect end-session service must not be null");
        }

        /**
         * Narrows a delegated outcome through the declared capability response class.
         *
         * @param stage        delegated outcome stage
         * @param responseType exact response class
         * @param <S>          required success type
         * @return type-safe delegated outcome
         */
        private static <S> CompletionStage<Outcome<S>> narrow(
                final CompletionStage<? extends Outcome<?>> stage,
                final Class<S> responseType) {
            return stage.thenApply(outcome -> switch (outcome) {
                case Outcome.Succeeded<?> success -> Outcome.succeeded(responseType.cast(success.value()));
                case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
            });
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
                                    "OpenID Connect Provider does not implement the requested capability",
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
         * @param timeout    shared end-to-end time budget
         * @param <Q>        request type
         * @param <S>        success type
         * @return delegated typed outcome or an unsupported-capability rejection
         */
        @Override
        public <Q, S> CompletionStage<Outcome<S>> invoke(
                final Capability<Q, S> capability,
                final Q request,
                final Context context,
                final Timeout.Budget timeout) {
            Assert.notNull(capability, "OpenID Connect Provider capability must not be null");
            Assert.notNull(context, "OpenID Connect Provider context must not be null");
            Assert.notNull(timeout, "OpenID Connect Provider time budget must not be null");
            if (!manifest.capabilities().contains(capability)) {
                return rejected();
            }
            if (capability.equals(OpenIdServerScheme.AUTHENTICATION)) {
                return narrow(
                        authentication.authorize(AuthenticationRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OpenIdServerScheme.TOKEN)) {
                return narrow(
                        token.token(TokenRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OpenIdServerScheme.INTROSPECTION)) {
                return narrow(
                        introspection.introspect(IntrospectionRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OpenIdServerScheme.REVOCATION)) {
                return narrow(
                        revocation.revoke(RevocationRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OpenIdServerScheme.DEVICE_AUTHORIZATION)) {
                return narrow(
                        deviceAuthorization
                                .deviceAuthorization(DeviceAuthorizationRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OpenIdServerScheme.AUTHORIZATION_SERVER_METADATA) && request == null) {
                return narrow(authorizationServerMetadata.metadata(context, timeout), capability.responseType());
            }
            if (capability.equals(OpenIdServerScheme.DISCOVERY) && request == null) {
                return narrow(discovery.discover(context, timeout), capability.responseType());
            }
            if (capability.equals(OpenIdServerScheme.JWK_SET) && request == null) {
                return narrow(jwkSet.jwks(context, timeout), capability.responseType());
            }
            if (capability.equals(OpenIdServerScheme.USERINFO)) {
                return narrow(
                        userInfo.userInfo(UserInfoRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OpenIdServerScheme.END_SESSION)) {
                return narrow(
                        endSession.endSession(EndSessionRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            return rejected();
        }

    }

}
