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

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.protocol.oauth2.IntrospectionRequest;
import org.miaixz.bus.auth.protocol.oauth2.IntrospectionResponse;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.codec.IntrospectionCodec;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.worker.loader.SecretLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Calls the RFC 7662 token introspection endpoint for one compiled OAuth 2.x Source.
 *
 * @author Kimi Liu
 */
public class IntrospectionClient {

    /**
     * Validated client registration and selected client authentication method.
     */
    private final OAuth2ClientOptions options;

    /**
     * Caller-owned execution services and Fabric context.
     */
    private final DriverServices services;

    /**
     * Strict RFC 7662 request and response codec.
     */
    private final IntrospectionCodec codec;

    /**
     * Shared strict form representation codec.
     */
    private final FormCodec formCodec;

    /**
     * Creates an introspection client for one compiled Source.
     *
     * @param options  validated OAuth 2.x client options
     * @param services externally owned runtime dependencies
     * @param codec    strict RFC 7662 codec
     * @throws IllegalArgumentException if a collaborator is {@code null} or no introspection endpoint is configured
     */
    public IntrospectionClient(final OAuth2ClientOptions options, final DriverServices services,
            final IntrospectionCodec codec) {
        this.options = Assert.notNull(options, "OAuth 2.x client options must not be null");
        Assert.notNull(
                options.introspectionEndpoint().getOrNull(),
                "OAuth 2.x introspection endpoint must be configured");
        this.services = Assert.notNull(services, "OAuth 2.x execution services must not be null");
        this.codec = Assert.notNull(codec, "OAuth 2.x introspection codec must not be null");
        this.formCodec = new FormCodec();
    }

    /**
     * Converts codec branch discrimination into the framework outcome boundary.
     *
     * @param decoded standard introspection success or OAuth error
     * @return successful response, rejected client error, or failed upstream server error
     */
    private static Outcome<IntrospectionResponse> decoded(final IntrospectionCodec.Decoded decoded) {
        return switch (decoded) {
            case IntrospectionCodec.Success success -> Outcome.succeeded(success.response());
            case IntrospectionCodec.Error error -> remote(error);
            default -> throw new IllegalStateException("Unsupported protocol model implementation");
        };
    }

    /**
     * Maps a valid standard OAuth endpoint error without exposing its description or inspected token.
     *
     * @param error decoded OAuth error response and original status
     * @return rejected 4xx or failed 5xx outcome
     */
    private static Outcome<IntrospectionResponse> remote(final IntrospectionCodec.Error error) {
        final Errors code = error.status() >= Http.Status.INTERNAL_SERVER_ERROR ? ErrorCode._502
                : error.status() == Http.Status.UNAUTHORIZED ? ErrorCode._401 : ErrorCode._400;
        final Outcome.Failure failure = failure(
                code,
                "OAuth 2.x introspection endpoint returned a standard error response");
        return error.status() >= Http.Status.INTERNAL_SERVER_ERROR ? Outcome.failed(failure)
                : Outcome.rejected(failure);
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
     * Introspects one opaque token without exposing it in a URL or diagnostic value.
     *
     * @param request standard introspection request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing the standard introspection response or closed framework failure
     */
    public CompletionStage<Outcome<IntrospectionResponse>> introspect(
            final IntrospectionRequest request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "OAuth 2.x introspection request must not be null");
        Assert.notNull(context, "OAuth 2.x introspection context must not be null");
        Assert.notNull(timeout, "OAuth 2.x introspection timeout must not be null");
        if (timeout.expired()) {
            return completed(Outcome.failed(failure(ErrorCode._408, "OAuth 2.x introspection request has no timeout")));
        }
        if (Endpoint.Authentication.NONE.equals(options.clientAuthenticationMethod())) {
            return execute(request, null, timeout);
        }
        return Outcome
                .mapStage(
                        () -> services.secretLoader().load(
                                new SecretLoader.Request(services.registration(),
                                        options.clientCredential().getOrNull()),
                                context,
                                timeout),
                        loaded -> services.secretParser()
                                .parse(services.registration(), options.clientCredential().getOrNull(), loaded))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> execute(request, success.value(), timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Encodes, authenticates, sends, and decodes one introspection request.
     *
     * @param request validated introspection request
     * @param secret  optional owned client-secret lease
     * @param timeout decreasing operation timeout
     * @return asynchronous introspection outcome
     */
    private CompletionStage<Outcome<IntrospectionResponse>> execute(
            final IntrospectionRequest request,
            final SecretLease secret,
            final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] body = null;
            try {
                if (timeout.expired()) {
                    return Outcome.<IntrospectionResponse>failed(
                            failure(ErrorCode._408, "OAuth 2.x introspection request exhausted its timeout"));
                }
                body = formCodec.encode(authenticated(codec.encode(request), secret));
                final var endpoint = options.introspectionEndpoint().getOrNull();
                final var builder = FabricX.http(services.fabric(), Protocol.OAUTH2, timeout)
                        .url(endpoint.url().toString()).method(Http.Method.POST)
                        .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
                if (Endpoint.Authentication.CLIENT_SECRET_BASIC.equals(options.clientAuthenticationMethod())) {
                    builder.header(
                            Http.Header.AUTHORIZATION,
                            FabricX.basic(
                                    formComponent(options.clientId()),
                                    formComponent(new String(secret.material()))));
                }
                return decoded(codec.decode(builder.execute()));
            } catch (RuntimeException exception) {
                return Outcome.<IntrospectionResponse>failed(
                        failure(ErrorCode._502, "OAuth 2.x introspection endpoint request failed"));
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
     * Adds exactly the configured OAuth client authentication form parameters.
     *
     * @param encoded standard introspection parameters
     * @param secret  optional client-secret lease
     * @return immutable ordered form parameters
     */
    private List<NameValue> authenticated(final List<NameValue> encoded, final SecretLease secret) {
        final List<NameValue> parameters = new ArrayList<>(
                Assert.notNull(encoded, "OAuth 2.x encoded introspection parameters must not be null"));
        if (Endpoint.Authentication.NONE.equals(options.clientAuthenticationMethod())) {
            parameters.add(new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId()));
        } else if (Endpoint.Authentication.CLIENT_SECRET_POST.equals(options.clientAuthenticationMethod())) {
            parameters.add(new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId()));
            parameters.add(new NameValue(OAuth2.Parameters.CLIENT_SECRET, new String(secret.material())));
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
        final byte[] encoded = formCodec.encode(List.of(new NameValue(Normal.EMPTY, value)));
        try {
            return new String(encoded, 1, encoded.length - 1, Charset.UTF_8);
        } finally {
            Arrays.fill(encoded, (byte) 0);
        }
    }

}
