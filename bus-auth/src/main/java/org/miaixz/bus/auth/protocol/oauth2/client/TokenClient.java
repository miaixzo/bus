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
import org.miaixz.bus.auth.protocol.oauth2.TokenEndpointResponse;
import org.miaixz.bus.auth.protocol.oauth2.TokenRequest;
import org.miaixz.bus.auth.protocol.oauth2.codec.TokenRequestEncoder;
import org.miaixz.bus.auth.protocol.oauth2.codec.TokenResponseDecoder;
import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.protocol.http.auth.HttpAuth;

/**
 * Executes every supported OAuth 2.x grant through the single registered token endpoint.
 *
 * @author Kimi Liu
 */
public final class TokenClient {

    /**
     * Validated client registration and selected client authentication method.
     */
    private final OAuth2ClientOptions options;

    /**
     * Caller-owned execution services and Fabric context.
     */
    private final ExecutionServices services;

    /**
     * Strict standard token request encoder.
     */
    private final TokenRequestEncoder requestEncoder;

    /**
     * Strict standard token response decoder.
     */
    private final TokenResponseDecoder responseDecoder;

    /**
     * Shared strict form representation codec.
     */
    private final FormCodec formCodec;

    /**
     * Creates a token client for one compiled OAuth 2.x Source.
     *
     * @param options         validated OAuth 2.x client options
     * @param services        externally owned runtime dependencies
     * @param requestEncoder  standard token request encoder
     * @param responseDecoder standard token response decoder
     * @throws IllegalArgumentException if a collaborator is {@code null} or no token endpoint is configured
     */
    public TokenClient(final OAuth2ClientOptions options, final ExecutionServices services,
            final TokenRequestEncoder requestEncoder, final TokenResponseDecoder responseDecoder) {
        this.options = Assert.notNull(options, "OAuth 2.x client options must not be null");
        Assert.notNull(options.tokenEndpoint().getOrNull(), "OAuth 2.x token endpoint must be configured");
        this.services = Assert.notNull(services, "OAuth 2.x execution services must not be null");
        this.requestEncoder = Assert.notNull(requestEncoder, "OAuth 2.x token request encoder must not be null");
        this.responseDecoder = Assert.notNull(responseDecoder, "OAuth 2.x token response decoder must not be null");
        this.formCodec = new FormCodec();
    }

    /**
     * Converts the codec-only success/error discrimination to the framework outcome boundary.
     *
     * @param decoded standard token success or OAuth error response
     * @return successful token response, rejected client error, or failed upstream server error
     */
    private static Outcome<TokenEndpointResponse> decoded(final TokenResponseDecoder.Decoded decoded) {
        return switch (decoded) {
            case TokenResponseDecoder.Success success -> Outcome.succeeded(success.response());
            case TokenResponseDecoder.Error error -> remote(error);
        };
    }

    /**
     * Maps a valid standard OAuth error response without exposing its diagnostic description or token material.
     *
     * @param error decoded OAuth error response and original HTTP status
     * @return rejected protocol 4xx, failed 429 rate limit, or failed 5xx framework outcome
     */
    private static Outcome<TokenEndpointResponse> remote(final TokenResponseDecoder.Error error) {
        final boolean rateLimited = error.status() == Http.Status.TOO_MANY_REQUESTS;
        final boolean upstreamFailure = error.status() >= Http.Status.INTERNAL_SERVER_ERROR;
        final Errors code = rateLimited ? ErrorCode._429
                : upstreamFailure ? ErrorCode._502
                        : error.status() == Http.Status.UNAUTHORIZED ? ErrorCode._401 : ErrorCode._400;
        final Outcome.Failure failure = failure(code, "OAuth 2.x token endpoint returned a standard error response");
        return rateLimited || upstreamFailure ? Outcome.failed(failure) : Outcome.rejected(failure);
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
     * Sends one standard grant request to the token endpoint.
     *
     * @param request standard token request
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing a standard token response or closed framework failure
     */
    public CompletionStage<Outcome<TokenEndpointResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "OAuth 2.x token request must not be null");
        Assert.notNull(context, "OAuth 2.x token invocation context must not be null");
        Assert.notNull(timeout, "OAuth 2.x token time budget must not be null");
        if (timeout.expired()) {
            return completed(Outcome.failed(failure(ErrorCode._408, "OAuth 2.x token request has no time budget")));
        }
        if (Endpoint.Authentication.NONE.equals(options.clientAuthenticationMethod())) {
            return execute(request, null, timeout);
        }
        return org.miaixz.bus.auth.runtime.LoadResult
                .parse(
                        services.secretLoader().load(options.clientCredential().getOrNull(), context, timeout),
                        loaded -> services.secretParser().parse(options.clientCredential().getOrNull(), loaded))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> execute(request, success.value(), timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Encodes, authenticates, sends, and decodes one token request.
     *
     * @param request validated token request
     * @param secret  optional owned client-secret lease
     * @param timeout decreasing operation budget
     * @return asynchronous token response outcome
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> execute(
            final TokenRequest request,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] body = null;
            try {
                if (timeout.expired()) {
                    return Outcome.<TokenEndpointResponse>failed(
                            failure(ErrorCode._408, "OAuth 2.x token request exhausted its time budget"));
                }
                final List<Parameter> parameters = authenticated(requestEncoder.encode(request), secret);
                body = formCodec.encode(parameters);
                final var endpoint = options.tokenEndpoint().getOrNull();
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
                return decoded(responseDecoder.decode(builder.execute()));
            } catch (RuntimeException exception) {
                return Outcome.<TokenEndpointResponse>failed(
                        failure(ErrorCode._502, "OAuth 2.x token endpoint request failed"));
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
     * Adds exactly the client authentication parameters selected by the Source options.
     *
     * @param encoded standard grant parameters
     * @param secret  optional client-secret lease
     * @return immutable ordered form parameters
     */
    private List<Parameter> authenticated(final List<Parameter> encoded, final SecretLease secret) {
        final List<Parameter> parameters = new ArrayList<>(
                Assert.notNull(encoded, "OAuth 2.x encoded token parameters must not be null"));
        if (Endpoint.Authentication.NONE.equals(options.clientAuthenticationMethod())) {
            parameters.add(new Parameter(OAuth2.Parameters.CLIENT_ID, options.clientId()));
        } else if (Endpoint.Authentication.CLIENT_SECRET_POST.equals(options.clientAuthenticationMethod())) {
            parameters.add(new Parameter(OAuth2.Parameters.CLIENT_ID, options.clientId()));
            parameters.add(new Parameter(OAuth2.Parameters.CLIENT_SECRET, new String(secret.material())));
        }
        return List.copyOf(parameters);
    }

    /**
     * Applies the RFC 6749 Appendix B encoding required before HTTP Basic credential construction.
     *
     * @param value decoded client identifier or password
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
