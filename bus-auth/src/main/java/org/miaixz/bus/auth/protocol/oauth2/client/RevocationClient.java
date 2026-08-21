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
package org.miaixz.bus.auth.protocol.oauth2.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.Parameter;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2ErrorCode;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2ErrorResponse;
import org.miaixz.bus.auth.protocol.oauth2.RevocationRequest;
import org.miaixz.bus.auth.protocol.oauth2.codec.RevocationRequestEncoder;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.auth.HttpAuth;

/**
 * Calls the RFC 7009 token revocation endpoint for one compiled OAuth 2.x Source.
 *
 * @author Kimi Liu
 */
public final class RevocationClient {

    /**
     * Maximum accepted RFC 7009 OAuth error document size.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum accepted nesting depth for a revocation OAuth error document.
     */
    private static final int MAXIMUM_JSON_DEPTH = 64;

    /**
     * Validated client registration and selected client authentication method.
     */
    private final OAuth2ClientOptions options;

    /**
     * Caller-owned execution services and Fabric context.
     */
    private final DriverServices services;

    /**
     * Strict RFC 7009 request encoder.
     */
    private final RevocationRequestEncoder encoder;

    /**
     * Shared strict form representation codec.
     */
    private final FormCodec formCodec;

    /**
     * Creates a revocation client for one compiled Source.
     *
     * @param options  validated OAuth 2.x client options
     * @param services externally owned runtime dependencies
     * @param encoder  strict RFC 7009 request encoder
     * @throws IllegalArgumentException if a collaborator is {@code null} or no revocation endpoint is configured
     */
    public RevocationClient(final OAuth2ClientOptions options, final DriverServices services,
            final RevocationRequestEncoder encoder) {
        this.options = Assert.notNull(options, "OAuth 2.x client options must not be null");
        Assert.notNull(options.revocationEndpoint().getOrNull(), "OAuth 2.x revocation endpoint must be configured");
        this.services = Assert.notNull(services, "OAuth 2.x execution services must not be null");
        this.encoder = Assert.notNull(encoder, "OAuth 2.x revocation request encoder must not be null");
        this.formCodec = new FormCodec();
    }

    /**
     * Reads one mandatory non-empty string member without JSON type coercion.
     *
     * @param object parsed OAuth error object
     * @param name   exact registered member name
     * @return required non-empty member value
     * @throws ValidateException if the member is absent, empty, or not a string
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final String value = optionalString(object, name);
        if (value == null || value.isEmpty()) {
            throw new ValidateException("OAuth 2.x revocation error requires non-empty " + name);
        }
        return value;
    }

    /**
     * Reads one optional registered string member without JSON type coercion.
     *
     * @param object parsed OAuth error object
     * @param name   exact registered member name
     * @return exact string value, or {@code null} when absent
     * @throws ValidateException if a present member is not a string
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("OAuth 2.x revocation error member must be a string: " + name);
        }
        return string.value();
    }

    /**
     * Creates a non-sensitive framework failure.
     *
     * @param code        shared Bus error code
     * @param description safe diagnostic text
     * @return closed failure value
     */
    private static Outcome.Failure failure(final Errors code, final String description) {
        return new Outcome.Failure(code, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates a completed stage with inferred success type.
     *
     * @param <T>     outcome success type
     * @param outcome completed outcome
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Revokes one opaque access or refresh token without exposing it in diagnostics.
     *
     * @param request standard revocation request
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage whose successful value is {@code null}
     */
    public CompletionStage<Outcome<Void>> revoke(
            final RevocationRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "OAuth 2.x revocation request must not be null");
        Assert.notNull(context, "OAuth 2.x revocation context must not be null");
        Assert.notNull(timeout, "OAuth 2.x revocation time budget must not be null");
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(failure(ErrorCode._408, "OAuth 2.x revocation request has no time budget")));
        }
        if (Endpoint.Authentication.NONE.equals(options.clientAuthenticationMethod())) {
            return execute(request, null, timeout);
        }
        return Outcome.mapStage(
                        () -> services.secretLoader().load(options.clientCredential().getOrNull(), context, timeout),
                        loaded -> services.secretParser().parse(options.clientCredential().getOrNull(), loaded))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> execute(request, success.value(), timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Encodes, authenticates, sends, and closes one revocation request.
     *
     * @param request validated revocation request
     * @param secret  optional owned client-secret lease
     * @param timeout decreasing operation budget
     * @return asynchronous revocation outcome
     */
    private CompletionStage<Outcome<Void>> execute(
            final RevocationRequest request,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] body = null;
            try {
                if (timeout.expired()) {
                    return Outcome.<Void>failed(
                            failure(ErrorCode._408, "OAuth 2.x revocation request exhausted its time budget"));
                }
                body = formCodec.encode(authenticated(encoder.encode(request), secret));
                final var endpoint = options.revocationEndpoint().getOrNull();
                final var builder = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                        .method(Http.Method.POST).timeout(timeout.forFabric())
                        .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                        .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
                if (Endpoint.Authentication.CLIENT_SECRET_BASIC.equals(options.clientAuthenticationMethod())) {
                    builder.header(
                            Http.Header.AUTHORIZATION,
                            HttpAuth.basic(
                                    formComponent(options.clientId()),
                                    formComponent(new String(secret.material()))).value());
                }
                return remote(builder.execute());
            } catch (RuntimeException exception) {
                return Outcome.<Void>failed(failure(ErrorCode._502, "OAuth 2.x revocation endpoint request failed"));
            } finally {
                if (body != null) {
                    Arrays.fill(body, (byte) 0);
                }
                if (secret != null) {
                    secret.close();
                }
            }
        }, services.executor());
    }

    /**
     * Closes and maps one RFC 7009 response without interpreting a successful response entity.
     *
     * @param response owned revocation endpoint response
     * @return successful empty result, rejected standard OAuth error, or failed non-standard response
     */
    private Outcome<Void> remote(final HttpResponse response) {
        try (response) {
            if (response.code() == Http.Status.OK) {
                return Outcome.succeeded(null);
            }
            if (response.code() != Http.Status.BAD_REQUEST && response.code() != Http.Status.UNAUTHORIZED) {
                return Outcome.failed(
                        failure(ErrorCode._502, "OAuth 2.x revocation endpoint returned a non-standard status"));
            }
            error(response);
            final Errors code = response.code() == Http.Status.UNAUTHORIZED ? ErrorCode._401 : ErrorCode._400;
            return Outcome.rejected(failure(code, "OAuth 2.x revocation endpoint returned a standard error response"));
        }
    }

    /**
     * Strictly decodes the standard JSON OAuth error members allowed by RFC 7009.
     *
     * @param response owned response while its body remains available
     * @return validated standard OAuth error response
     * @throws ValidateException if media metadata, size, JSON shape, or a registered member is invalid
     */
    private OAuth2ErrorResponse error(final HttpResponse response) {
        if (response.body().length() > MAXIMUM_JSON_BYTES) {
            throw new ValidateException("OAuth 2.x revocation error response exceeds one MiB");
        }
        final MediaType media = response.body().media();
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(media)) {
            throw new ValidateException("OAuth 2.x revocation error response must use application/json");
        }
        final String charset = media.parameter(MediaType.CHARSET_PARAMETER);
        if (charset != null && !Charset.UTF_8.equals(media.charset())) {
            throw new ValidateException("OAuth 2.x revocation error response charset must be UTF-8");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("OAuth 2.x revocation error JSON root must be an object");
        }
        return new OAuth2ErrorResponse(new OAuth2ErrorCode(requiredString(object, OAuth2.Parameters.ERROR)),
                Optional.ofNullable(optionalString(object, OAuth2.Parameters.ERROR_DESCRIPTION)),
                Optional.ofNullable(optionalString(object, OAuth2.Parameters.ERROR_URI)), Optional.empty());
    }

    /**
     * Adds exactly the configured OAuth client authentication form parameters.
     *
     * @param encoded standard revocation parameters
     * @param secret  optional client-secret lease
     * @return immutable ordered form parameters
     */
    private List<Parameter> authenticated(final List<Parameter> encoded, final SecretLease secret) {
        final List<Parameter> parameters = new ArrayList<>(
                Assert.notNull(encoded, "OAuth 2.x encoded revocation parameters must not be null"));
        if (Endpoint.Authentication.NONE.equals(options.clientAuthenticationMethod())) {
            parameters.add(new Parameter(OAuth2.Parameters.CLIENT_ID, options.clientId()));
        } else if (Endpoint.Authentication.CLIENT_SECRET_POST.equals(options.clientAuthenticationMethod())) {
            parameters.add(new Parameter(OAuth2.Parameters.CLIENT_ID, options.clientId()));
            parameters.add(new Parameter(OAuth2.Parameters.CLIENT_SECRET, new String(secret.material())));
        }
        return List.copyOf(parameters);
    }

    /**
     * Applies RFC 6749 Appendix B encoding before Basic credential construction.
     *
     * @param value decoded credential component
     * @return form-encoded credential component
     */
    private String formComponent(final String value) {
        final byte[] encoded = formCodec.encode(List.of(new Parameter(Normal.EMPTY, value)));
        try {
            return new String(encoded, 1, encoded.length - 1, Charset.UTF_8);
        } finally {
            Arrays.fill(encoded, (byte) 0);
        }
    }

}
