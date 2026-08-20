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
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.*;
import org.miaixz.bus.auth.protocol.oauth2.codec.*;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.internal.RuntimeProvider;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Compiles one validated generic OAuth 2.x Source registration into endpoint-accurate client operations.
 *
 * @author Kimi Liu
 */
public final class OAuth2SourceDriver implements SourceDriver<OAuth2ClientSettings> {

    /**
     * Immutable generic OAuth 2.x Source profile shared by compiled registrations.
     */
    private final OAuth2SourceProfile profile;

    /**
     * Creates a driver with the deterministic generic OAuth 2.x Source profile.
     */
    public OAuth2SourceDriver() {
        this.profile = new OAuth2SourceProfile();
    }

    /**
     * Builds the exact ordered capability set represented by configured Source endpoints.
     *
     * @param settings validated Source settings
     * @return non-empty endpoint-accurate manifest
     */
    private static Capability.Manifest manifest(final OAuth2ClientSettings settings) {
        final List<Capability<?, ?>> capabilities = new ArrayList<>();
        if (settings.authorizationEndpoint().isPresent()) {
            capabilities.add(OAuth2SourceProfile.AUTHORIZATION);
        }
        if (settings.tokenEndpoint().isPresent()) {
            capabilities.add(OAuth2SourceProfile.TOKEN);
        }
        if (settings.introspectionEndpoint().isPresent()) {
            capabilities.add(OAuth2SourceProfile.INTROSPECTION);
        }
        if (settings.revocationEndpoint().isPresent()) {
            capabilities.add(OAuth2SourceProfile.REVOCATION);
        }
        if (settings.deviceAuthorizationEndpoint().isPresent()) {
            capabilities.add(OAuth2SourceProfile.DEVICE_AUTHORIZATION);
        }
        if (settings.authorizationServerMetadataEndpoint().isPresent()) {
            capabilities.add(OAuth2SourceProfile.AUTHORIZATION_SERVER_METADATA);
        }
        return new Capability.Manifest(capabilities);
    }

    /**
     * Returns the OAuth 2.x client profile bound to this driver.
     *
     * @return immutable OAuth 2.x Source profile
     */
    @Override
    public OAuth2SourceProfile profile() {
        return profile;
    }

    /**
     * Consumes typed settings and creates only clients backed by configured endpoints.
     *
     * @param record   validated complete Source registration
     * @param provider resolved optional associated Provider
     * @param library  resolved owning Provider Library
     * @param services externally owned runtime dependencies
     * @return executable immutable runtime provider
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if registration routing fields do not match this driver
     */
    @Override
    public RuntimeProvider compile(
            final Registration.Record<Source> record,
            final Provider provider,
            final Library library,
            final ExecutionServices services) {
        Assert.notNull(record, "OAuth 2.x Source registration must not be null");
        Assert.notNull(provider, "OAuth 2.x Source Provider container must not be null");
        Assert.notNull(library, "OAuth 2.x Source Library container must not be null");
        Assert.notNull(services, "OAuth 2.x Source execution services must not be null");
        final Source source = record.resource();
        if (!profile().id().equals(source.getType()) || !supports(source.getProtocol())
                || source.getNamespace_id() == null || source.getNamespace_id().isBlank()
                || !provider.getId().equals(source.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("OAuth 2.x Source driver requires a matching Source registration");
        }
        final OAuth2ClientSettings settings = decode(source);
        final Endpoint authorizationEndpoint = settings.authorizationEndpoint().getOrNull();
        return new CompiledSource(manifest(settings),
                authorizationEndpoint == null ? null
                        : new AuthorizationClient(settings, new AuthorizationRequestEncoder(authorizationEndpoint)),
                settings.tokenEndpoint().isEmpty() ? null
                        : new TokenClient(settings, services, new TokenRequestEncoder(),
                                new TokenResponseDecoder(services.jsonProvider())),
                settings.introspectionEndpoint().isEmpty() ? null
                        : new IntrospectionClient(settings, services, new IntrospectionCodec(services.jsonProvider())),
                settings.revocationEndpoint().isEmpty() ? null
                        : new RevocationClient(settings, services, new RevocationRequestEncoder()),
                settings.deviceAuthorizationEndpoint().isEmpty() ? null
                        : new DeviceAuthorizationClient(settings, services,
                                new DeviceAuthorizationCodec(services.jsonProvider())),
                settings.authorizationServerMetadataEndpoint().isEmpty() ? null
                        : new AuthorizationServerMetadataClient(settings, services,
                                new AuthorizationServerMetadataCodec(services.jsonProvider())));
    }

    /**
     * Routes only Source capabilities whose endpoints were configured.
     *
     * @author Kimi Liu
     */
    private static final class CompiledSource implements RuntimeProvider {

        /**
         * Endpoint-accurate immutable manifest.
         */
        private final Capability.Manifest manifest;

        /**
         * Optional authorization client, present only with an authorization endpoint.
         */
        private final AuthorizationClient authorization;

        /**
         * Optional token client, present only with a token endpoint.
         */
        private final TokenClient token;

        /**
         * Optional introspection client, present only with an introspection endpoint.
         */
        private final IntrospectionClient introspection;

        /**
         * Optional revocation client, present only with a revocation endpoint.
         */
        private final RevocationClient revocation;

        /**
         * Optional device authorization client, present only with a device endpoint.
         */
        private final DeviceAuthorizationClient deviceAuthorization;

        /**
         * Optional metadata client, present only with a metadata endpoint.
         */
        private final AuthorizationServerMetadataClient metadata;

        /**
         * Creates a compiled Source with nullable clients controlled exclusively by its manifest.
         *
         * @param manifest            endpoint-accurate manifest
         * @param authorization       optional authorization client
         * @param token               optional token client
         * @param introspection       optional introspection client
         * @param revocation          optional revocation client
         * @param deviceAuthorization optional device authorization client
         * @param metadata            optional metadata client
         */
        private CompiledSource(final Capability.Manifest manifest, final AuthorizationClient authorization,
                final TokenClient token, final IntrospectionClient introspection, final RevocationClient revocation,
                final DeviceAuthorizationClient deviceAuthorization, final AuthorizationServerMetadataClient metadata) {
            this.manifest = Assert.notNull(manifest, "OAuth 2.x Source manifest must not be null");
            this.authorization = authorization;
            this.token = token;
            this.introspection = introspection;
            this.revocation = revocation;
            this.deviceAuthorization = deviceAuthorization;
            this.metadata = metadata;
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
                                    "OAuth 2.x Source does not implement the requested capability",
                                    new JsonValue.ObjectValue(Map.of()))));
        }

        /**
         * Returns capabilities backed by configured Source endpoints.
         *
         * @return immutable endpoint-accurate manifest
         */
        @Override
        public Capability.Manifest manifest() {
            return manifest;
        }

        /**
         * Invokes one exact declared OAuth 2.x Source capability.
         *
         * @param capability exact declared capability
         * @param request    exact request or {@code null} for metadata
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
            Assert.notNull(capability, "OAuth 2.x Source capability must not be null");
            Assert.notNull(context, "OAuth 2.x Source context must not be null");
            Assert.notNull(timeout, "OAuth 2.x Source time budget must not be null");
            if (!manifest.capabilities().contains(capability)) {
                return rejected();
            }
            if (capability.equals(OAuth2SourceProfile.AUTHORIZATION) && authorization != null) {
                return narrow(
                        authorization.authorize(AuthorizationRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth2SourceProfile.TOKEN) && token != null) {
                return narrow(
                        token.token(TokenRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth2SourceProfile.INTROSPECTION) && introspection != null) {
                return narrow(
                        introspection.introspect(IntrospectionRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth2SourceProfile.REVOCATION) && revocation != null) {
                return narrow(
                        revocation.revoke(RevocationRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth2SourceProfile.DEVICE_AUTHORIZATION) && deviceAuthorization != null) {
                return narrow(
                        deviceAuthorization
                                .deviceAuthorization(DeviceAuthorizationRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth2SourceProfile.AUTHORIZATION_SERVER_METADATA) && metadata != null
                    && request == null) {
                return narrow(metadata.metadata(context, timeout), capability.responseType());
            }
            return rejected();
        }

    }

}
