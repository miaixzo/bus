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
package org.miaixz.bus.auth.source.protocol.oauth2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.Scheme.Options;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.protocol.ProtocolDriver;
import org.miaixz.bus.auth.source.protocol.oauth2.client.*;
import org.miaixz.bus.auth.source.protocol.oauth2.codec.*;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.auth.worker.WorkerSlots;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Compiles one validated generic OAuth 2.x Source configuration into endpoint-accurate client operations.
 *
 * @author Kimi Liu
 */
public class OAuth2ClientDriver implements ProtocolDriver<OAuth2ClientOptions> {

    /**
     * Immutable generic OAuth 2.x Source scheme shared by compiled registrations.
     */
    private final OAuth2ClientScheme scheme;

    /**
     * Creates a driver with the deterministic generic OAuth 2.x Source scheme.
     */
    public OAuth2ClientDriver() {
        this.scheme = new OAuth2ClientScheme();
    }

    /**
     * Builds the exact ordered capability set represented by configured Source endpoints.
     *
     * @param options validated Source options
     * @return non-empty endpoint-accurate manifest
     */
    private static Capability.Manifest manifest(final OAuth2ClientOptions options) {
        final List<Capability<?, ?>> capabilities = new ArrayList<>();
        if (options.authorizationEndpoint().isPresent()) {
            capabilities.add(OAuth2ClientScheme.AUTHORIZATION);
        }
        if (options.tokenEndpoint().isPresent()) {
            capabilities.add(OAuth2ClientScheme.TOKEN);
        }
        if (options.introspectionEndpoint().isPresent()) {
            capabilities.add(OAuth2ClientScheme.INTROSPECTION);
        }
        if (options.revocationEndpoint().isPresent()) {
            capabilities.add(OAuth2ClientScheme.REVOCATION);
        }
        if (options.deviceAuthorizationEndpoint().isPresent()) {
            capabilities.add(OAuth2ClientScheme.DEVICE_AUTHORIZATION);
        }
        if (options.authorizationServerMetadataEndpoint().isPresent()) {
            capabilities.add(OAuth2ClientScheme.AUTHORIZATION_SERVER_METADATA);
        }
        return new Capability.Manifest(capabilities);
    }

    /**
     * Tests whether any enabled endpoint requires configured client-secret material.
     *
     * @param options validated OAuth client options
     * @return whether the compiled worker requires the secret-loading slot
     */
    private static boolean usesClientSecret(final OAuth2ClientOptions options) {
        if (Endpoint.Authentication.NONE.equals(options.clientAuthenticationMethod())) {
            return false;
        }
        return options.tokenEndpoint().isPresent() || options.introspectionEndpoint().isPresent()
                || options.revocationEndpoint().isPresent() || options.deviceAuthorizationEndpoint().isPresent();
    }

    /**
     * Returns the OAuth 2.x client scheme bound to this driver.
     *
     * @return immutable OAuth 2.x Source scheme
     */
    @Override
    public OAuth2ClientScheme scheme() {
        return scheme;
    }

    /**
     * Narrows generic Source options to OAuth 2.x client options.
     *
     * @param options generic Source options
     * @return validated OAuth 2.x client options
     */
    @Override
    public OAuth2ClientOptions require(final Options<?> options) {
        if (options instanceof OAuth2ClientOptions value) {
            return value;
        }
        throw new ValidateException("OAuth 2.x client driver requires OAuth2ClientOptions");
    }

    /**
     * Declares the secret slot only when the selected OAuth client authentication requires it.
     *
     * @param source  Source configuration
     * @param options validated OAuth 2.x client options
     * @return exact project integration slots
     */
    @Override
    public WorkerSlots slots(final Source source, final OAuth2ClientOptions options) {
        return usesClientSecret(options) ? WorkerSlots.of(WorkerSlots.Slot.SECRET) : WorkerSlots.none();
    }

    /**
     * Declares JSON, execution, and security-policy services used by the OAuth client.
     *
     * @param source  Source configuration
     * @param options validated OAuth 2.x client options
     * @return exact framework dependencies
     */
    @Override
    public Dependencies dependencies(final Source source, final OAuth2ClientOptions options) {
        return Dependencies.of(Dependencies.Service.EXECUTOR, Dependencies.Service.POLICIES);
    }

    /**
     * Consumes typed options and creates only clients backed by configured endpoints.
     *
     * @param prepared one-time validated Source graph, Options and dependency declaration
     * @param services dependency-scoped runtime services
     * @return executable immutable Source worker
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if Source routing fields do not match this driver
     */
    @Override
    public SourceWorker compile(final Prepared<OAuth2ClientOptions> prepared, final DriverServices services) {
        Assert.notNull(prepared, "OAuth 2.x Source preparation must not be null");
        Assert.notNull(services, "OAuth 2.x Source execution services must not be null");
        final Blueprint.SourceEntry entry = prepared.entry();
        final Provider provider = prepared.provider();
        final Library library = prepared.library();
        final Source source = entry.resource();
        if (!scheme().id().equals(source.getType()) || !supports(source.getProtocol())
                || !provider.getId().equals(source.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("OAuth 2.x Source driver requires a matching Source configuration");
        }
        final OAuth2ClientOptions options = prepared.options();
        final Endpoint authorizationEndpoint = options.authorizationEndpoint().getOrNull();
        return new CompiledClient(manifest(options),
                authorizationEndpoint == null ? null
                        : new AuthorizationClient(options, new AuthorizationRequestEncoder(authorizationEndpoint)),
                options.tokenEndpoint().isEmpty() ? null
                        : new TokenClient(options, services, new TokenRequestEncoder(), new TokenResponseDecoder()),
                options.introspectionEndpoint().isEmpty() ? null
                        : new IntrospectionClient(options, services, new IntrospectionCodec()),
                options.revocationEndpoint().isEmpty() ? null
                        : new RevocationClient(options, services, new RevocationRequestEncoder()),
                options.deviceAuthorizationEndpoint().isEmpty() ? null
                        : new DeviceAuthorizationClient(options, services, new DeviceAuthorizationCodec()),
                options.authorizationServerMetadataEndpoint().isEmpty() ? null
                        : new AuthorizationServerMetadataClient(options, services,
                                new AuthorizationServerMetadataCodec()));
    }

    /**
     * Routes only Source capabilities whose endpoints were configured.
     *
     * @author Kimi Liu
     */
    private static final class CompiledClient implements SourceWorker {

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
        private CompiledClient(final Capability.Manifest manifest, final AuthorizationClient authorization,
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
                default -> throw new IllegalStateException("Unsupported Outcome implementation");
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
         * @param timeout    shared end-to-end timeout
         * @param <Q>        request type
         * @param <S>        success type
         * @return delegated typed outcome or rejected unsupported capability
         */
        @Override
        public <Q, S> CompletionStage<Outcome<S>> invoke(
                final Capability<Q, S> capability,
                final Q request,
                final Context context,
                final Timeout timeout) {
            Assert.notNull(capability, "OAuth 2.x Source capability must not be null");
            Assert.notNull(context, "OAuth 2.x Source context must not be null");
            Assert.notNull(timeout, "OAuth 2.x Source timeout must not be null");
            if (!manifest.capabilities().contains(capability)) {
                return rejected();
            }
            if (capability.equals(OAuth2ClientScheme.AUTHORIZATION) && authorization != null) {
                return narrow(
                        authorization.authorize(AuthorizationRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth2ClientScheme.TOKEN) && token != null) {
                return narrow(
                        token.token(TokenRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth2ClientScheme.INTROSPECTION) && introspection != null) {
                return narrow(
                        introspection.introspect(IntrospectionRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth2ClientScheme.REVOCATION) && revocation != null) {
                return narrow(
                        revocation.revoke(RevocationRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth2ClientScheme.DEVICE_AUTHORIZATION) && deviceAuthorization != null) {
                return narrow(
                        deviceAuthorization
                                .deviceAuthorization(DeviceAuthorizationRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth2ClientScheme.AUTHORIZATION_SERVER_METADATA) && metadata != null
                    && request == null) {
                return narrow(metadata.metadata(context, timeout), capability.responseType());
            }
            return rejected();
        }

    }

}
