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
package org.miaixz.bus.auth.protocol.oauth2.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.guard.RedirectUriValidator;
import org.miaixz.bus.auth.guard.ScopeValidator;
import org.miaixz.bus.auth.guard.SecretGuard;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.server.*;
import org.miaixz.bus.auth.provider.ProviderDriver;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.internal.RuntimeProvider;
import org.miaixz.bus.auth.shared.pkce.PkceValidator;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Compiles one validated server-role OAuth 2.x Source registration into typed authorization-server services.
 *
 * @author Kimi Liu
 */
public final class OAuth2ProviderDriver implements ProviderDriver<OAuth2ProviderSettings> {

    /**
     * Immutable OAuth 2.x Provider profile shared by compiled registrations.
     */
    private final OAuth2ProviderProfile profile;

    /**
     * Creates a driver with the deterministic OAuth 2.x Provider profile.
     */
    public OAuth2ProviderDriver() {
        this.profile = new OAuth2ProviderProfile();
    }

    /**
     * Builds the exact ordered capability set represented by configured Provider endpoints.
     *
     * @param settings validated Provider settings
     * @return non-empty endpoint-accurate capability manifest
     */
    private static Capability.Manifest manifest(final OAuth2ProviderSettings settings) {
        final List<Capability<?, ?>> capabilities = new ArrayList<>();
        if (settings.authorizationEndpoint().isPresent()) {
            capabilities.add(OAuth2ProviderProfile.AUTHORIZATION);
        }
        if (settings.tokenEndpoint().isPresent()) {
            capabilities.add(OAuth2ProviderProfile.TOKEN);
        }
        if (settings.introspectionEndpoint().isPresent()) {
            capabilities.add(OAuth2ProviderProfile.INTROSPECTION);
        }
        if (settings.revocationEndpoint().isPresent()) {
            capabilities.add(OAuth2ProviderProfile.REVOCATION);
        }
        if (settings.deviceAuthorizationEndpoint().isPresent()) {
            capabilities.add(OAuth2ProviderProfile.DEVICE_AUTHORIZATION);
        }
        if (settings.authorizationServerMetadataEndpoint().isPresent()) {
            capabilities.add(OAuth2ProviderProfile.AUTHORIZATION_SERVER_METADATA);
        }
        return new Capability.Manifest(capabilities);
    }

    /**
     * Returns the OAuth 2.x server profile bound to this driver.
     *
     * @return immutable OAuth 2.x Provider profile
     */
    @Override
    public OAuth2ProviderProfile profile() {
        return profile;
    }

    /**
     * Consumes typed settings and assembles one endpoint-accurate OAuth 2.x server-role Source runtime.
     *
     * @param record   validated complete server-role Source registration
     * @param library  resolved Library owned by the Provider
     * @param services externally owned runtime dependencies
     * @return executable immutable runtime provider
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if the registration routing fields do not match this driver
     */
    @Override
    public RuntimeProvider compile(
            final Registration.Record<Source> record,
            final Provider provider,
            final Library library,
            final ExecutionServices services) {
        Assert.notNull(record, "OAuth 2.x Provider registration must not be null");
        Assert.notNull(library, "OAuth 2.x Provider Library must not be null");
        Assert.notNull(services, "OAuth 2.x Provider execution services must not be null");
        final Source source = record.resource();
        if (!profile().id().equals(source.getType()) || !supports(source.getProtocol())
                || !provider.getId().equals(source.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("OAuth 2.x server driver requires a matching Source registration");
        }
        final OAuth2ProviderSettings settings = decode(source);

        final ScopeValidator scopeValidator = new ScopeValidator();
        final RedirectUriValidator redirectUriValidator = new RedirectUriValidator();
        final PkceValidator pkceValidator = PkceValidator.strict(new SecretGuard());
        final AuthorizationCodeIssuer codeIssuer = new AuthorizationCodeIssuer(source.getId(), settings, services,
                redirectUriValidator, scopeValidator);
        final AccessTokenIssuer accessTokenIssuer = new AccessTokenIssuer(source.getId(), settings, services,
                scopeValidator, pkceValidator, AccessTokenIssuer.Augmenter.passThrough());
        final RefreshTokenRotator refreshTokenRotator = new RefreshTokenRotator(source.getId(), settings, services,
                scopeValidator, accessTokenIssuer);
        return new CompiledProvider(manifest(settings), new AuthorizationService(codeIssuer),
                new TokenService(accessTokenIssuer, refreshTokenRotator),
                new IntrospectionService(source.getId(), settings, services),
                new RevocationService(source.getId(), services),
                new DeviceAuthorizationService(source.getId(), settings, services, scopeValidator),
                new AuthorizationServerMetadataService(settings));
    }

    /**
     * Routes the exact enabled OAuth 2.x Provider capabilities to typed services.
     *
     * @author Kimi Liu
     */
    private static final class CompiledProvider implements RuntimeProvider {

        /**
         * Endpoint-accurate immutable capability manifest.
         */
        private final Capability.Manifest manifest;

        /**
         * Typed authorization operation.
         */
        private final AuthorizationService authorization;

        /**
         * Typed token operation.
         */
        private final TokenService token;

        /**
         * Typed introspection operation.
         */
        private final IntrospectionService introspection;

        /**
         * Typed revocation operation.
         */
        private final RevocationService revocation;

        /**
         * Typed device authorization operation.
         */
        private final DeviceAuthorizationService deviceAuthorization;

        /**
         * Typed authorization server metadata operation.
         */
        private final AuthorizationServerMetadataService metadata;

        /**
         * Creates one compiled authorization-server Source runtime from its exact manifest and typed services.
         *
         * @param manifest            endpoint-accurate manifest
         * @param authorization       authorization service
         * @param token               token service
         * @param introspection       introspection service
         * @param revocation          revocation service
         * @param deviceAuthorization device authorization service
         * @param metadata            metadata service
         */
        private CompiledProvider(final Capability.Manifest manifest, final AuthorizationService authorization,
                final TokenService token, final IntrospectionService introspection, final RevocationService revocation,
                final DeviceAuthorizationService deviceAuthorization,
                final AuthorizationServerMetadataService metadata) {
            this.manifest = Assert.notNull(manifest, "OAuth 2.x Provider manifest must not be null");
            this.authorization = Assert.notNull(authorization, "OAuth 2.x authorization service must not be null");
            this.token = Assert.notNull(token, "OAuth 2.x token service must not be null");
            this.introspection = Assert.notNull(introspection, "OAuth 2.x introspection service must not be null");
            this.revocation = Assert.notNull(revocation, "OAuth 2.x revocation service must not be null");
            this.deviceAuthorization = Assert
                    .notNull(deviceAuthorization, "OAuth 2.x device authorization service must not be null");
            this.metadata = Assert.notNull(metadata, "OAuth 2.x metadata service must not be null");
        }

        /**
         * Narrows a delegated outcome through the declared capability response type.
         *
         * @param stage        delegated outcome stage
         * @param responseType exact response class
         * @param <S>          required response type
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
            if (capability.equals(OAuth2ProviderProfile.AUTHORIZATION)) {
                return narrow(
                        authorization.authorize(AuthorizationRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth2ProviderProfile.TOKEN)) {
                return narrow(
                        token.token(TokenRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth2ProviderProfile.INTROSPECTION)) {
                return narrow(
                        introspection.introspect(IntrospectionRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth2ProviderProfile.REVOCATION)) {
                return narrow(
                        revocation.revoke(RevocationRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth2ProviderProfile.DEVICE_AUTHORIZATION)) {
                return narrow(
                        deviceAuthorization
                                .deviceAuthorization(DeviceAuthorizationRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth2ProviderProfile.AUTHORIZATION_SERVER_METADATA) && request == null) {
                return narrow(metadata.metadata(context, timeout), capability.responseType());
            }
            return rejected();
        }

    }

}
