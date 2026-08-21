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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.Parameter;
import org.miaixz.bus.auth.guard.ClientAuthenticator;
import org.miaixz.bus.auth.guard.SecretGuard;
import org.miaixz.bus.auth.resolver.ConsumerMetadata;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.worker.ConsumerLoader;
import org.miaixz.bus.auth.worker.SecretLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;

/**
 * Authenticates the OAuth 2.x client evidence carried by one Fabric HTTP endpoint request.
 * <p>
 * The implementation owns only RFC 6749 {@code none}, {@code client_secret_basic}, and
 * {@code client_secret_post} processing. Consumer and secret persistence remain project worker responsibilities;
 * comparison, method selection, duplicate rejection, and secret lifetime remain framework responsibilities.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OAuth2ClientAuthenticator implements ClientAuthenticator<HttpRequest> {

    private static final long MAXIMUM_FORM_BYTES = 64 * Normal.KIBI;

    private final Endpoint endpoint;
    private final DriverServices services;
    private final FormCodec formCodec;
    private final SecretGuard secretGuard;

    /**
     * Creates an authenticator for one exact configured endpoint.
     *
     * @param endpoint configured OAuth endpoint and its accepted methods
     * @param services selected Source execution services
     */
    public OAuth2ClientAuthenticator(final Endpoint endpoint, final DriverServices services) {
        this.endpoint = Assert.notNull(endpoint, "OAuth 2.x authenticated endpoint must not be null");
        this.services = Assert.notNull(services, "OAuth 2.x client authentication services must not be null");
        this.formCodec = new FormCodec();
        this.secretGuard = new SecretGuard();
    }

    private static Outcome.Failure failure(final boolean operational, final String description) {
        return new Outcome.Failure(operational ? ErrorCode._500 : ErrorCode._401, description,
                new JsonValue.ObjectValue(Map.of()));
    }

    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    private static String decoded(final String value) {
        final byte[] encoded = ("value=" + value).getBytes(Charset.UTF_8);
        final List<Parameter> parameters = new FormCodec().decode(encoded);
        if (parameters.size() != 1 || !"value".equals(parameters.getFirst().name())) {
            throw new ValidateException("OAuth 2.x Basic credential contains invalid form encoding");
        }
        return parameters.getFirst().value();
    }

    private static Evidence basic(final String authorization) {
        if (!authorization.regionMatches(true, 0, "Basic ", 0, 6) || authorization.length() == 6) {
            throw new ValidateException("OAuth 2.x client authorization scheme is unsupported");
        }
        final byte[] bytes;
        try {
            bytes = java.util.Base64.getDecoder().decode(authorization.substring(6));
        } catch (IllegalArgumentException cause) {
            throw new ValidateException("OAuth 2.x Basic credentials are malformed", cause);
        }
        try {
            final String credentials;
            try {
                credentials = Charset.newDecoder(Charset.UTF_8, CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(bytes)).toString();
            } catch (CharacterCodingException cause) {
                throw new ValidateException("OAuth 2.x Basic credentials are not valid UTF-8", cause);
            }
            final int separator = credentials.indexOf(':');
            if (separator <= 0) {
                throw new ValidateException("OAuth 2.x Basic credentials require client id and secret");
            }
            final String clientId = decoded(credentials.substring(0, separator));
            final char[] secret = decoded(credentials.substring(separator + 1)).toCharArray();
            if (clientId.isEmpty() || secret.length == 0) {
                Arrays.fill(secret, '\0');
                throw new ValidateException("OAuth 2.x Basic credentials require client id and secret");
            }
            return new Evidence(clientId, secret, Endpoint.Authentication.CLIENT_SECRET_BASIC);
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    private Map<String, String> form(final HttpRequest request) {
        if (!request.body().repeatable() || request.body().length() < 0L
                || request.body().length() > MAXIMUM_FORM_BYTES) {
            throw new ValidateException("OAuth 2.x client authentication requires a bounded buffered form");
        }
        final Map<String, String> values = new LinkedHashMap<>();
        for (Parameter parameter : formCodec.decode(request.body().bytes(MAXIMUM_FORM_BYTES))) {
            if (values.putIfAbsent(parameter.name(), parameter.value()) != null) {
                throw new ValidateException("OAuth 2.x client authentication parameters must not be repeated");
            }
        }
        return values;
    }

    private Evidence evidence(final HttpRequest request) {
        final List<String> authorization = request.headers().values(Http.Header.AUTHORIZATION);
        if (authorization.size() > 1) {
            throw new ValidateException("OAuth 2.x client request must not repeat Authorization");
        }
        final Map<String, String> values = form(request);
        final String clientId = values.get("client_id");
        final String clientSecret = values.get("client_secret");
        if (!authorization.isEmpty()) {
            if (clientId != null || clientSecret != null) {
                throw new ValidateException("OAuth 2.x client must use exactly one authentication method");
            }
            return basic(authorization.getFirst());
        }
        if (clientSecret != null) {
            if (clientId == null || clientId.isEmpty() || clientSecret.isEmpty()) {
                throw new ValidateException("OAuth 2.x form credentials require client id and secret");
            }
            return new Evidence(clientId, clientSecret.toCharArray(), Endpoint.Authentication.CLIENT_SECRET_POST);
        }
        if (clientId == null || clientId.isEmpty()) {
            throw new ValidateException("OAuth 2.x public client request requires client_id");
        }
        return new Evidence(clientId, null, Endpoint.Authentication.NONE);
    }

    private CompletionStage<Outcome<ConsumerMetadata>> load(
            final Evidence evidence,
            final Context context,
            final Timeout.Budget timeout) {
        final CompletionStage<Outcome<ConsumerLoader.Record>> loading;
        try {
            loading = services.consumerLoader().load(evidence.clientId(), context, timeout);
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
            };
        });
    }

    private CompletionStage<Outcome<ConsumerMetadata>> verify(
            final Evidence evidence,
            final ConsumerLoader.Record record,
            final Context context,
            final Timeout.Budget timeout) {
        final ConsumerMetadata consumer;
        try {
            consumer = services.consumerParser().parse(evidence.clientId(), record);
        } catch (RuntimeException cause) {
            return completed(Outcome.failed(failure(true, "OAuth 2.x consumer data is invalid")));
        }
        if (Endpoint.Authentication.NONE.equals(evidence.method())) {
            return consumer.credential().isEmpty() ? completed(Outcome.succeeded(consumer))
                    : completed(Outcome.rejected(failure(false, "OAuth 2.x confidential client requires credentials")));
        }
        final Credential.Reference reference = consumer.credential().getOrNull();
        if (reference == null || reference.type() != Credential.Type.CLIENT_SECRET) {
            return completed(Outcome.rejected(failure(false, "OAuth 2.x client authentication failed")));
        }
        final CompletionStage<Outcome<SecretLoader.Record>> loading;
        try {
            loading = services.secretLoader().load(reference, context, timeout);
        } catch (RuntimeException cause) {
            return completed(Outcome.failed(failure(true, "OAuth 2.x client secret loading failed")));
        }
        if (loading == null) {
            return completed(Outcome.failed(failure(true, "OAuth 2.x client secret loader returned no stage")));
        }
        return loading.handle((outcome, cause) -> cause == null ? outcome : null).thenApply(outcome -> {
            if (!(outcome instanceof Outcome.Succeeded<SecretLoader.Record> success) || success.value() == null) {
                return outcome instanceof Outcome.Failed<SecretLoader.Record>
                        ? Outcome.failed(failure(true, "OAuth 2.x client secret loading failed"))
                        : Outcome.rejected(failure(false, "OAuth 2.x client authentication failed"));
            }
            final SecretLoader.Record secretRecord = success.value();
            final SecretLease lease = secretRecord.lease();
            try (lease) {
                final SecretLease parsed = services.secretParser().parse(reference, secretRecord);
                return secretGuard.matches(parsed.material(), evidence.secret()) ? Outcome.succeeded(consumer)
                        : Outcome.rejected(failure(false, "OAuth 2.x client authentication failed"));
            } catch (RuntimeException cause) {
                return Outcome.failed(failure(true, "OAuth 2.x client secret data is invalid"));
            }
        });
    }

    /**
     * Authenticates one request using only methods declared by its configured endpoint.
     */
    @Override
    public CompletionStage<Outcome<ConsumerMetadata>> authenticate(
            final HttpRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "OAuth 2.x client authentication request must not be null");
        Assert.notNull(context, "OAuth 2.x client authentication context must not be null");
        Assert.notNull(timeout, "OAuth 2.x client authentication budget must not be null");
        if (timeout.expired()) {
            return completed(Outcome.failed(failure(true, "OAuth 2.x client authentication budget expired")));
        }
        final Evidence evidence;
        try {
            evidence = evidence(request);
            if (!endpoint.authentication().contains(evidence.method())) {
                evidence.close();
                return completed(Outcome.rejected(failure(false, "OAuth 2.x client authentication method is disabled")));
            }
        } catch (RuntimeException cause) {
            return completed(Outcome.rejected(failure(false, "OAuth 2.x client authentication evidence is invalid")));
        }
        return load(evidence, context, timeout).whenComplete((ignored, cause) -> evidence.close());
    }

    private record Evidence(String clientId, char[] secret, Endpoint.Authentication method) implements AutoCloseable {

        private Evidence {
            Assert.notBlank(clientId, "OAuth 2.x client identifier must not be blank");
            Assert.notNull(method, "OAuth 2.x client authentication method must not be null");
            if (Endpoint.Authentication.NONE.equals(method) != (secret == null)) {
                throw new ValidateException("OAuth 2.x client evidence does not match its method");
            }
        }

        @Override
        public void close() {
            if (secret != null) {
                Arrays.fill(secret, '\0');
            }
        }
    }
}
