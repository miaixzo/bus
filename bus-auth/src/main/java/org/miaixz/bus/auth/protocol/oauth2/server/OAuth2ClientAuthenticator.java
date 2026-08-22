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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.FabricX.Request;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.guard.ClientAuthentication;
import org.miaixz.bus.auth.guard.ClientAuthenticator;
import org.miaixz.bus.auth.protocol.oauth2.ClientAuthenticationMethod;
import org.miaixz.bus.auth.protocol.oauth2.GrantType;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.resolver.ConsumerMetadata;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.worker.loader.ConsumerLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Authenticates the OAuth 2.x client evidence carried by one Fabric HTTP endpoint request.
 * <p>
 * The implementation selects RFC 6749 {@code none}, {@code client_secret_basic}, {@code client_secret_post}, or one JWT
 * assertion path from the request shape and endpoint policy. Secret verification remains project-owned through
 * {@code ConsumerVerifier}; standard {@code private_key_jwt} and explicitly enabled federated assertions delegate to
 * {@link JwtClientAuthenticator}. Duplicate rejection, single-profile selection, and evidence lifetime remain framework
 * responsibilities.
 * </p>
 *
 * @author Kimi Liu
 */
public class OAuth2ClientAuthenticator implements ClientAuthenticator<Request> {

    /**
     * Maximum buffered form body accepted for client authentication.
     */
    private static final long MAXIMUM_FORM_BYTES = 64 * Normal.KIBI;

    /**
     * Configured endpoint and its accepted client-authentication methods.
     */
    private final Endpoint endpoint;
    /**
     * Source-scoped project loaders and framework parsers.
     */
    private final DriverServices services;
    /**
     * Strict application/x-www-form-urlencoded codec.
     */
    private final FormCodec formCodec;

    /**
     * Optional token-endpoint JWT assertion verifier.
     */
    private final JwtClientAuthenticator jwtAuthenticator;

    /**
     * Creates an authenticator for one exact configured endpoint.
     *
     * @param endpoint configured OAuth endpoint and its accepted methods
     * @param services selected Source execution services
     */
    public OAuth2ClientAuthenticator(final Endpoint endpoint, final DriverServices services) {
        this(endpoint, services, null);
    }

    /**
     * Creates the token-endpoint authenticator with standard and federated JWT assertion support.
     *
     * @param options  frozen OAuth Provider options
     * @param services selected Source execution services
     */
    public OAuth2ClientAuthenticator(final OAuth2ServerOptions options, final DriverServices services) {
        this(Assert.notNull(options, "OAuth 2.x client options must not be null").tokenEndpoint().getOrNull(), services,
                jwt(options, services));
    }

    /**
     * Creates an authenticator with an optional JWT assertion verifier.
     *
     * @param endpoint         configured OAuth endpoint and its accepted methods
     * @param services         selected Source execution services
     * @param jwtAuthenticator optional token-endpoint JWT verifier
     */
    private OAuth2ClientAuthenticator(final Endpoint endpoint, final DriverServices services,
            final JwtClientAuthenticator jwtAuthenticator) {
        this.endpoint = Assert.notNull(endpoint, "OAuth 2.x authenticated endpoint must not be null");
        this.services = Assert.notNull(services, "OAuth 2.x client authentication services must not be null");
        this.formCodec = new FormCodec();
        this.jwtAuthenticator = jwtAuthenticator;
    }

    /**
     * Creates a JWT verifier only when one configured authentication profile requires it.
     *
     * @param options  frozen OAuth Provider options
     * @param services selected Source execution services
     * @return configured verifier or {@code null} when JWT authentication is disabled
     */
    private static JwtClientAuthenticator jwt(final OAuth2ServerOptions options, final DriverServices services) {
        return options.federatedJwtEnabled()
                || options.tokenEndpointAuthMethodsSupported().contains(ClientAuthenticationMethod.PRIVATE_KEY_JWT)
                        ? new JwtClientAuthenticator(options, services)
                        : null;
    }

    /**
     * Creates a safe client-authentication rejection or operational failure.
     *
     * @param operational whether the failure belongs to an unavailable dependency
     * @param description safe failure description
     * @return immutable failure value
     */
    private static Outcome.Failure failure(final boolean operational, final String description) {
        return new Outcome.Failure(operational ? ErrorCode._500 : ErrorCode._401, description,
                new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Wraps an outcome in an already completed stage.
     *
     * @param <T>     outcome value type
     * @param outcome outcome to expose
     * @return completed outcome stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Applies RFC 6749 form decoding to one Basic credential component.
     *
     * @param value encoded credential component
     * @return decoded component
     */
    private static String decoded(final String value) {
        final byte[] encoded = ("value=" + value).getBytes(Charset.UTF_8);
        final List<NameValue> parameters = new FormCodec().decode(encoded);
        if (parameters.size() != 1 || !"value".equals(parameters.getFirst().name())) {
            throw new ValidateException("OAuth 2.x Basic credential contains invalid form encoding");
        }
        return parameters.getFirst().value();
    }

    /**
     * Decodes and validates one client_secret_basic Authorization value.
     *
     * @param authorization complete Authorization header value
     * @param grantType     requested OAuth grant type
     * @return caller-owned client evidence
     */
    private static Evidence basic(final String authorization, final GrantType grantType) {
        if (!authorization.regionMatches(true, 0, "Basic ", 0, 6) || authorization.length() == 6) {
            throw new ValidateException("OAuth 2.x client authorization scheme is unsupported");
        }
        final byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(authorization.substring(6));
        } catch (IllegalArgumentException cause) {
            throw new ValidateException("OAuth 2.x Basic credentials are malformed", cause);
        }
        try {
            final String credentials;
            try {
                credentials = Charset.newDecoder(Charset.UTF_8, CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes))
                        .toString();
            } catch (CharacterCodingException cause) {
                throw new ValidateException("OAuth 2.x Basic credentials are not valid UTF-8", cause);
            }
            final int separator = credentials.indexOf(Symbol.C_COLON);
            if (separator <= 0) {
                throw new ValidateException("OAuth 2.x Basic credentials require client id and secret");
            }
            final String clientId = decoded(credentials.substring(0, separator));
            final char[] secret = decoded(credentials.substring(separator + 1)).toCharArray();
            if (clientId.isEmpty() || secret.length == 0) {
                Arrays.fill(secret, Symbol.C_NUL);
                throw new ValidateException("OAuth 2.x Basic credentials require client id and secret");
            }
            return new Evidence(clientId, secret, null, Endpoint.Authentication.CLIENT_SECRET_BASIC, grantType);
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    /**
     * Decodes a bounded request form and rejects repeated parameters.
     *
     * @param request buffered endpoint request
     * @return unique form parameter map
     */
    private Map<String, String> form(final Request request) {
        if (!request.body().repeatable() || request.body().length() < 0L
                || request.body().length() > MAXIMUM_FORM_BYTES) {
            throw new ValidateException("OAuth 2.x client authentication requires a bounded buffered form");
        }
        final Map<String, String> values = new LinkedHashMap<>();
        for (NameValue parameter : formCodec.decode(request.body().bytes(MAXIMUM_FORM_BYTES))) {
            if (values.putIfAbsent(parameter.name(), parameter.value()) != null) {
                throw new ValidateException("OAuth 2.x client authentication parameters must not be repeated");
            }
        }
        return values;
    }

    /**
     * Selects exactly one supported client-authentication evidence source.
     *
     * @param request endpoint request
     * @return validated caller-owned evidence
     */
    private Evidence evidence(final Request request) {
        final List<String> authorization = request.headers().values(Http.Header.AUTHORIZATION);
        if (authorization.size() > 1) {
            throw new ValidateException("OAuth 2.x client request must not repeat Authorization");
        }
        final Map<String, String> values = form(request);
        final String grant = values.get(OAuth2.Parameters.GRANT_TYPE);
        if (grant == null || grant.isEmpty()) {
            throw new ValidateException("OAuth 2.x client authentication requires grant_type");
        }
        final GrantType grantType = new GrantType(grant);
        final String clientId = values.get(OAuth2.Parameters.CLIENT_ID);
        final String clientSecret = values.get(OAuth2.Parameters.CLIENT_SECRET);
        final String assertion = values.get(OAuth2.Parameters.CLIENT_ASSERTION);
        final String assertionType = values.get(OAuth2.Parameters.CLIENT_ASSERTION_TYPE);
        if (assertion != null || assertionType != null) {
            if (!authorization.isEmpty() || clientSecret != null || clientId == null || clientId.isEmpty()
                    || assertion == null || assertion.isEmpty()
                    || !OAuth2.Parameters.JWT_BEARER_ASSERTION_TYPE.equals(assertionType)) {
                throw new ValidateException("OAuth 2.x JWT client assertion evidence is invalid");
            }
            return new Evidence(clientId, null, assertion, Endpoint.Authentication.PRIVATE_KEY_JWT, grantType);
        }
        if (!authorization.isEmpty()) {
            if (clientId != null || clientSecret != null) {
                throw new ValidateException("OAuth 2.x client must use exactly one authentication method");
            }
            return basic(authorization.getFirst(), grantType);
        }
        if (clientSecret != null) {
            if (clientId == null || clientId.isEmpty() || clientSecret.isEmpty()) {
                throw new ValidateException("OAuth 2.x form credentials require client id and secret");
            }
            return new Evidence(clientId, clientSecret.toCharArray(), null, Endpoint.Authentication.CLIENT_SECRET_POST,
                    grantType);
        }
        if (clientId == null || clientId.isEmpty()) {
            throw new ValidateException("OAuth 2.x public client request requires client_id");
        }
        return new Evidence(clientId, null, null, Endpoint.Authentication.NONE, grantType);
    }

    /**
     * Loads the exact registered consumer identified by client evidence.
     *
     * @param evidence validated client evidence
     * @param context  invocation context
     * @param timeout  operation timeout
     * @return authenticated consumer outcome stage
     */
    private CompletionStage<Outcome<ClientAuthentication>> load(
            final Evidence evidence,
            final Context context,
            final Timeout timeout) {
        final CompletionStage<Outcome<ConsumerLoader.Record>> loading;
        try {
            loading = services.consumerLoader()
                    .load(new ConsumerLoader.Request(services.registration(), evidence.clientId()), context, timeout);
        } catch (RuntimeException cause) {
            return completed(Outcome.failed(failure(true, "OAuth 2.x consumer loading failed")));
        }
        if (loading == null) {
            return completed(Outcome.failed(failure(true, "OAuth 2.x consumer loader returned no stage")));
        }
        return loading.handle((outcome, cause) -> cause == null ? outcome : null).thenCompose(outcome -> {
            if (outcome == null) {
                return completed(Outcome.failed(failure(true, "OAuth 2.x consumer loading failed")));
            }
            return switch (outcome) {
                case Outcome.Succeeded<ConsumerLoader.Record> success -> verify(
                        evidence,
                        success.value(),
                        context,
                        timeout);
                case Outcome.Rejected<ConsumerLoader.Record> ignored -> completed(
                        Outcome.rejected(failure(false, "OAuth 2.x client authentication failed")));
                case Outcome.Failed<ConsumerLoader.Record> ignored -> completed(
                        Outcome.failed(failure(true, "OAuth 2.x consumer loading failed")));
                default -> throw new IllegalStateException("Unsupported Outcome implementation");
            };
        });
    }

    /**
     * Parses consumer metadata and verifies public or confidential client evidence.
     *
     * @param evidence validated client evidence
     * @param record   project-loaded consumer record
     * @param context  invocation context
     * @param timeout  operation timeout
     * @return authenticated consumer outcome stage
     */
    private CompletionStage<Outcome<ClientAuthentication>> verify(
            final Evidence evidence,
            final ConsumerLoader.Record record,
            final Context context,
            final Timeout timeout) {
        final ConsumerMetadata consumer;
        try {
            consumer = services.consumerParser().parse(services.registration(), evidence.clientId(), record);
        } catch (RuntimeException cause) {
            return completed(Outcome.failed(failure(true, "OAuth 2.x consumer data is invalid")));
        }
        final ClientAuthenticationMethod method = new ClientAuthenticationMethod(evidence.method().value());
        if (!consumer.authenticationMethods().contains(method)) {
            return completed(Outcome.rejected(failure(false, "OAuth 2.x client authentication failed")));
        }
        if (Endpoint.Authentication.NONE.equals(evidence.method())) {
            return consumer.publicClient()
                    ? completed(Outcome.succeeded(ClientAuthentication.standard(consumer, method)))
                    : completed(Outcome.rejected(failure(false, "OAuth 2.x confidential client requires credentials")));
        }
        if (Endpoint.Authentication.PRIVATE_KEY_JWT.equals(evidence.method())) {
            return jwtAuthenticator == null
                    ? completed(Outcome.rejected(failure(false, "OAuth 2.x JWT client authentication is disabled")))
                    : jwtAuthenticator
                            .authenticate(consumer, evidence.grantType(), evidence.assertion(), context, timeout);
        }
        final CompletionStage<Outcome<Void>> verification;
        final SecretLease lease = new SecretLease(evidence.secret());
        try {
            verification = services.consumerVerifier()
                    .verify(services.registration(), consumer.id(), method, lease, context, timeout);
        } catch (RuntimeException cause) {
            return completed(Outcome.failed(failure(true, "OAuth 2.x client verification failed")));
        } finally {
            lease.close();
        }
        if (verification == null) {
            return completed(Outcome.failed(failure(true, "OAuth 2.x client verifier returned no stage")));
        }
        return verification.handle((outcome, cause) -> cause == null ? outcome : null).thenApply(outcome -> {
            if (outcome instanceof Outcome.Succeeded<Void>) {
                return Outcome.succeeded(ClientAuthentication.standard(consumer, method));
            }
            return outcome instanceof Outcome.Failed<Void>
                    ? Outcome.failed(failure(true, "OAuth 2.x client verification failed"))
                    : Outcome.rejected(failure(false, "OAuth 2.x client authentication failed"));
        });
    }

    /**
     * Authenticates one request using only methods declared by its configured endpoint.
     */
    @Override
    public CompletionStage<Outcome<ClientAuthentication>> authenticate(
            final Request request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "OAuth 2.x client authentication request must not be null");
        Assert.notNull(context, "OAuth 2.x client authentication context must not be null");
        Assert.notNull(timeout, "OAuth 2.x client authentication timeout must not be null");
        if (timeout.expired()) {
            return completed(Outcome.failed(failure(true, "OAuth 2.x client authentication timeout expired")));
        }
        final Evidence evidence;
        try {
            evidence = evidence(request);
            if (!endpoint.authentication().contains(evidence.method())
                    && !(Endpoint.Authentication.PRIVATE_KEY_JWT.equals(evidence.method())
                            && jwtAuthenticator != null)) {
                evidence.close();
                return completed(
                        Outcome.rejected(failure(false, "OAuth 2.x client authentication method is disabled")));
            }
        } catch (RuntimeException cause) {
            return completed(Outcome.rejected(failure(false, "OAuth 2.x client authentication evidence is invalid")));
        }
        return load(evidence, context, timeout).whenComplete((ignored, cause) -> evidence.close());
    }

    /**
     * Holds one selected client-authentication method and caller-owned secret material.
     *
     * @param clientId  asserted client identifier
     * @param secret    mutable client secret, or {@code null} for public clients
     * @param assertion submitted compact client assertion, or {@code null} for non-JWT methods
     * @param method    selected endpoint authentication method
     * @param grantType requested token grant type
     * @author Kimi Liu
     */
    private record Evidence(String clientId, char[] secret, String assertion, Endpoint.Authentication method,
            GrantType grantType) implements AutoCloseable {

        /**
         * Validates that evidence shape matches the selected authentication method.
         */
        private Evidence {
            Assert.notBlank(clientId, "OAuth 2.x client identifier must not be blank");
            Assert.notNull(method, "OAuth 2.x client authentication method must not be null");
            Assert.notNull(grantType, "OAuth 2.x client authentication grant must not be null");
            final boolean secretMethod = Endpoint.Authentication.CLIENT_SECRET_BASIC.equals(method)
                    || Endpoint.Authentication.CLIENT_SECRET_POST.equals(method);
            final boolean assertionMethod = Endpoint.Authentication.PRIVATE_KEY_JWT.equals(method);
            if (secretMethod != (secret != null) || assertionMethod != (assertion != null)
                    || !secretMethod && !assertionMethod && (secret != null || assertion != null)) {
                throw new ValidateException("OAuth 2.x client evidence does not match its method");
            }
        }

        /**
         * Erases caller-owned secret material after authentication completes.
         */
        @Override
        public void close() {
            if (secret != null) {
                Arrays.fill(secret, Symbol.C_NUL);
            }
        }

    }

}
