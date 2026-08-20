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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.oauth2.DeviceAuthorizationRequest;
import org.miaixz.bus.auth.protocol.oauth2.IntrospectionRequest;
import org.miaixz.bus.auth.protocol.oauth2.RevocationRequest;
import org.miaixz.bus.auth.protocol.oauth2.TokenRequest;
import org.miaixz.bus.auth.protocol.oauth2.client.*;
import org.miaixz.bus.auth.protocol.oauth2.codec.*;
import org.miaixz.bus.auth.protocol.oidc.client.*;
import org.miaixz.bus.auth.protocol.oidc.codec.*;
import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.auth.shared.jose.JwkSet;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;

/**
 * Compiles one OpenID Connect relying-party Source into endpoint-accurate standard client operations.
 * <p>
 * The driver composes OAuth operations rather than reimplementing their wire contracts. Public-key retrieval uses only
 * the explicitly configured {@code jwks_uri}; callback decoding, state correlation, key selection, and ID Token
 * verification remain explicit protocol steps because their trusted inputs are supplied by the callback owner.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OpenIdSourceDriver implements SourceDriver<OpenIdClientOptions> {

    /**
     * Creates a stateless OpenID Connect Source driver.
     */
    public OpenIdSourceDriver() {
        // No initialization required.
    }

    /**
     * Ensures every configured ID Token algorithm is enabled by the OIDC security baseline.
     *
     * @param options  validated Source options
     * @param services runtime security dependencies
     * @throws ValidateException if the local registration enables a baseline-prohibited algorithm
     */
    private static void validateAlgorithms(final OpenIdClientOptions options, final ExecutionServices services) {
        final var allowed = services.securityBaseline().require(Protocol.OIDC).algorithms();
        for (var algorithm : options.idTokenSigningAlgorithms()) {
            if (!allowed.contains(algorithm.name())) {
                throw new ValidateException(
                        "OpenID Connect ID Token algorithm is not enabled by the security baseline");
            }
        }
    }

    /**
     * Creates the optional token introspection client.
     *
     * @param options  composed OAuth client options
     * @param services runtime dependencies
     * @return normalized optional client
     */
    private static Optional<IntrospectionClient> optionalIntrospection(
            final OAuth2ClientOptions options,
            final ExecutionServices services) {
        return options.introspectionEndpoint().isEmpty() ? Optional.empty()
                : Optional.of(
                        new IntrospectionClient(options, services, new IntrospectionCodec(services.jsonProvider())));
    }

    /**
     * Creates the optional token revocation client.
     *
     * @param options  composed OAuth client options
     * @param services runtime dependencies
     * @return normalized optional client
     */
    private static Optional<RevocationClient> optionalRevocation(
            final OAuth2ClientOptions options,
            final ExecutionServices services) {
        return options.revocationEndpoint().isEmpty() ? Optional.empty()
                : Optional.of(new RevocationClient(options, services, new RevocationRequestEncoder()));
    }

    /**
     * Creates the optional device authorization client.
     *
     * @param options  composed OAuth client options
     * @param services runtime dependencies
     * @return normalized optional client
     */
    private static Optional<DeviceAuthorizationClient> optionalDeviceAuthorization(
            final OAuth2ClientOptions options,
            final ExecutionServices services) {
        return options.deviceAuthorizationEndpoint().isEmpty() ? Optional.empty()
                : Optional.of(
                        new DeviceAuthorizationClient(options, services,
                                new DeviceAuthorizationCodec(services.jsonProvider())));
    }

    /**
     * Creates the optional Authorization Server Metadata client.
     *
     * @param options  composed OAuth client options
     * @param services runtime dependencies
     * @return normalized optional client
     */
    private static Optional<AuthorizationServerMetadataClient> optionalMetadata(
            final OAuth2ClientOptions options,
            final ExecutionServices services) {
        return options.authorizationServerMetadataEndpoint().isEmpty() ? Optional.empty()
                : Optional.of(
                        new AuthorizationServerMetadataClient(options, services,
                                new AuthorizationServerMetadataCodec(services.jsonProvider())));
    }

    /**
     * Creates the optional UserInfo client.
     *
     * @param options  OpenID Connect Source options
     * @param services runtime dependencies
     * @return normalized optional client
     */
    private static Optional<UserInfoClient> optionalUserInfo(
            final OpenIdClientOptions options,
            final ExecutionServices services) {
        return options.userInfoEndpoint().isEmpty() ? Optional.empty()
                : Optional.of(new UserInfoClient(options, services, new UserInfoCodec(services.jsonProvider())));
    }

    /**
     * Creates the optional RP-Initiated Logout client.
     *
     * @param options OpenID Connect Source options
     * @return normalized optional client
     */
    private static Optional<EndSessionClient> optionalEndSession(final OpenIdClientOptions options) {
        return options.endSessionEndpoint().isEmpty() ? Optional.empty()
                : Optional.of(new EndSessionClient(options, new EndSessionRequestCodec()));
    }

    /**
     * Builds the ordered capabilities represented by the exact configured endpoints.
     *
     * @param options validated Source options
     * @return non-empty endpoint-accurate capability manifest
     */
    private static Capability.Manifest manifest(final OpenIdClientOptions options) {
        final List<Capability<?, ?>> capabilities = new ArrayList<>();
        capabilities.add(OpenIdClientScheme.AUTHENTICATION);
        capabilities.add(OpenIdClientScheme.TOKEN);
        if (options.oauth2Options().introspectionEndpoint().isPresent()) {
            capabilities.add(OpenIdClientScheme.INTROSPECTION);
        }
        if (options.oauth2Options().revocationEndpoint().isPresent()) {
            capabilities.add(OpenIdClientScheme.REVOCATION);
        }
        if (options.oauth2Options().deviceAuthorizationEndpoint().isPresent()) {
            capabilities.add(OpenIdClientScheme.DEVICE_AUTHORIZATION);
        }
        if (options.oauth2Options().authorizationServerMetadataEndpoint().isPresent()) {
            capabilities.add(OpenIdClientScheme.AUTHORIZATION_SERVER_METADATA);
        }
        capabilities.add(OpenIdClientScheme.DISCOVERY);
        capabilities.add(OpenIdClientScheme.JWK_SET);
        if (options.userInfoEndpoint().isPresent()) {
            capabilities.add(OpenIdClientScheme.USERINFO);
        }
        if (options.endSessionEndpoint().isPresent()) {
            capabilities.add(OpenIdClientScheme.END_SESSION);
        }
        return new Capability.Manifest(capabilities);
    }

    /**
     * Creates a non-sensitive framework failure shared by private driver operations.
     *
     * @param code        shared Bus error code
     * @param description safe diagnostic text
     * @return closed failure value
     */
    private static Outcome.Failure failure(final Errors code, final String description) {
        return new Outcome.Failure(code, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Returns the OpenID relying-party scheme bound to this driver.
     *
     * @return immutable OpenID Source scheme
     */
    @Override
    public OpenIdClientScheme scheme() {
        return new OpenIdClientScheme();
    }

    @Override
    public OpenIdClientOptions require(final Options<?> options) {
        if (options instanceof OpenIdClientOptions value) {
            return value;
        }
        throw new ValidateException("OpenID Connect client driver requires OpenIdClientOptions");
    }

    /**
     * Consumes typed options and assembles only operations backed by declared endpoints.
     *
     * @param record   validated complete Source registration
     * @param provider resolved optional associated Provider
     * @param library  resolved owning Provider Library
     * @param services externally owned runtime dependencies
     * @return immutable executable Source runtime
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if routing, options, or signing policy is invalid
     */
    @Override
    public SourceWorker compile(
            final Registration.Record<Source> record,
            final Provider provider,
            final Library library,
            final ExecutionServices services) {
        Assert.notNull(record, "OpenID Connect Source registration must not be null");
        Assert.notNull(provider, "OpenID Connect Source Provider container must not be null");
        Assert.notNull(library, "OpenID Connect Source Library container must not be null");
        Assert.notNull(services, "OpenID Connect Source execution services must not be null");
        final Source source = record.resource();
        if (!scheme().id().equals(source.getType()) || !supports(source.getProtocol())
                || source.getNamespace_id() == null || source.getNamespace_id().isBlank()
                || !provider.getId().equals(source.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("OpenID Connect Source driver requires a matching Source registration");
        }
        final OpenIdClientOptions options = require(source.getOptions());
        validateAlgorithms(options, services);

        final OAuth2ClientOptions oauth = options.oauth2Options();
        final Endpoint authorizationEndpoint = oauth.authorizationEndpoint().getOrNull();
        final OAuth2Client oauthClient = new OAuth2Client(
                new AuthorizationClient(oauth, new AuthorizationRequestEncoder(authorizationEndpoint)),
                new TokenClient(oauth, services, new TokenRequestEncoder(),
                        new TokenResponseDecoder(services.jsonProvider())),
                optionalIntrospection(oauth, services), optionalRevocation(oauth, services),
                optionalDeviceAuthorization(oauth, services), optionalMetadata(oauth, services));
        final OpenIdClient client = new OpenIdClient(oauthClient,
                new AuthenticationRequestEncoder(services.jsonProvider()),
                new DiscoveryClient(options, services, new OpenIdProviderMetadataCodec(services.jsonProvider())),
                optionalUserInfo(options, services), optionalEndSession(options));
        return new CompiledSource(manifest(options), client,
                new JwkSetOperation(options, services, new JwkSetCodec(services.jsonProvider())));
    }

    /**
     * Owns the single remote JWK Set retrieval operation for a compiled Source.
     *
     * @author Kimi Liu
     */
    private static final class JwkSetOperation {

        /**
         * Explicitly configured JWK Set resource endpoint.
         */
        private final Endpoint endpoint;

        /**
         * Externally owned runtime dependencies and HTTP execution context.
         */
        private final ExecutionServices services;

        /**
         * Strict public JWK Set response codec.
         */
        private final JwkSetCodec codec;

        /**
         * Creates one operation bound to the Source's exact configured JWK Set URI.
         *
         * @param options  validated OpenID Connect Source options
         * @param services externally owned runtime dependencies
         * @param codec    strict public-key resource codec
         */
        private JwkSetOperation(final OpenIdClientOptions options, final ExecutionServices services,
                final JwkSetCodec codec) {
            this.endpoint = Assert.notNull(
                    options.jwkSetEndpoint().getOrNull(),
                    "OpenID Connect JWK Set endpoint must be configured");
            this.services = Assert.notNull(services, "OpenID Connect JWK Set execution services must not be null");
            this.codec = Assert.notNull(codec, "OpenID Connect JWK Set codec must not be null");
        }

        /**
         * Retrieves the configured issuer's public JWK Set.
         *
         * @param context immutable invocation context
         * @param timeout shared end-to-end time budget
         * @return stage containing a standard public JWK Set or closed framework failure
         */
        private CompletionStage<Outcome<JwkSet>> jwks(final Context context, final Timeout.Budget timeout) {
            Assert.notNull(context, "OpenID Connect JWK Set context must not be null");
            Assert.notNull(timeout, "OpenID Connect JWK Set time budget must not be null");
            if (timeout.expired()) {
                return CompletableFuture.completedFuture(
                        Outcome.failed(failure(ErrorCode._408, "OpenID Connect JWK Set request has no time budget")));
            }
            return CompletableFuture.supplyAsync(() -> execute(timeout), services.executor());
        }

        /**
         * Executes and decodes one public-key resource request.
         *
         * @param timeout decreasing operation budget
         * @return decoded public-key outcome
         */
        private Outcome<JwkSet> execute(final Timeout.Budget timeout) {
            try {
                if (timeout.expired()) {
                    return Outcome.failed(
                            failure(ErrorCode._408, "OpenID Connect JWK Set request exhausted its time budget"));
                }
                final var response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                        .method(Http.Method.GET).timeout(timeout.forFabric())
                        .addressPolicy(services.securityBaseline().require(Protocol.OIDC).addressPolicy()).execute();
                return Outcome.succeeded(codec.decode(response));
            } catch (RuntimeException exception) {
                return Outcome.failed(failure(ErrorCode._502, "OpenID Connect JWK Set endpoint request failed"));
            }
        }

    }

    /**
     * Routes exact Source capabilities to the compiled OpenID Connect and composed OAuth clients.
     *
     * @author Kimi Liu
     */
    private static final class CompiledSource implements SourceWorker {

        /**
         * Endpoint-accurate immutable capability manifest.
         */
        private final Capability.Manifest manifest;

        /**
         * OpenID Connect facade owning user-agent and remote endpoint operations.
         */
        private final OpenIdClient client;

        /**
         * Explicitly bound public JWK Set retrieval operation.
         */
        private final JwkSetOperation jwkSet;

        /**
         * Creates one compiled Source runtime.
         *
         * @param manifest endpoint-accurate capability manifest
         * @param client   OpenID Connect client facade
         * @param jwkSet   public-key retrieval operation
         */
        private CompiledSource(final Capability.Manifest manifest, final OpenIdClient client,
                final JwkSetOperation jwkSet) {
            this.manifest = Assert.notNull(manifest, "OpenID Connect Source manifest must not be null");
            this.client = Assert.notNull(client, "OpenID Connect client must not be null");
            this.jwkSet = Assert.notNull(jwkSet, "OpenID Connect JWK Set operation must not be null");
        }

        /**
         * Narrows a delegated outcome through the capability's exact response class.
         *
         * @param stage        delegated outcome stage
         * @param responseType declared response class
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
         * Creates a completed rejection for undeclared or request-shape-mismatched capabilities.
         *
         * @param <S> expected success type
         * @return completed rejected outcome
         */
        private static <S> CompletionStage<Outcome<S>> rejected() {
            return CompletableFuture.completedFuture(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    "OpenID Connect Source does not implement the requested capability")));
        }

        /**
         * Returns the exact endpoint-backed capability manifest.
         *
         * @return immutable capability manifest
         */
        @Override
        public Capability.Manifest manifest() {
            return manifest;
        }

        /**
         * Invokes one exact declared OpenID Connect Source capability.
         *
         * @param capability exact declared capability
         * @param request    exact request value or {@code null} for metadata resources
         * @param context    immutable invocation context
         * @param timeout    shared end-to-end time budget
         * @param <Q>        request type
         * @param <S>        success type
         * @return delegated typed outcome or unsupported-capability rejection
         */
        @Override
        public <Q, S> CompletionStage<Outcome<S>> invoke(
                final Capability<Q, S> capability,
                final Q request,
                final Context context,
                final Timeout.Budget timeout) {
            Assert.notNull(capability, "OpenID Connect Source capability must not be null");
            Assert.notNull(context, "OpenID Connect Source context must not be null");
            Assert.notNull(timeout, "OpenID Connect Source time budget must not be null");
            if (!manifest.capabilities().contains(capability)) {
                return rejected();
            }
            if (capability.equals(OpenIdClientScheme.AUTHENTICATION)) {
                return narrow(
                        client.authorize(AuthenticationRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OpenIdClientScheme.TOKEN)) {
                return narrow(
                        client.token(TokenRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OpenIdClientScheme.INTROSPECTION)) {
                return narrow(
                        client.introspect(IntrospectionRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OpenIdClientScheme.REVOCATION)) {
                return narrow(
                        client.revoke(RevocationRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OpenIdClientScheme.DEVICE_AUTHORIZATION)) {
                return narrow(
                        client.deviceAuthorization(DeviceAuthorizationRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OpenIdClientScheme.AUTHORIZATION_SERVER_METADATA) && request == null) {
                return narrow(client.metadata(context, timeout), capability.responseType());
            }
            if (capability.equals(OpenIdClientScheme.DISCOVERY) && request == null) {
                return narrow(client.discover(context, timeout), capability.responseType());
            }
            if (capability.equals(OpenIdClientScheme.JWK_SET) && request == null) {
                return narrow(jwkSet.jwks(context, timeout), capability.responseType());
            }
            if (capability.equals(OpenIdClientScheme.USERINFO)) {
                return narrow(
                        client.userInfo(UserInfoRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OpenIdClientScheme.END_SESSION)) {
                return narrow(
                        client.endSession(EndSessionRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            return rejected();
        }

    }

}
