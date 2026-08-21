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
import org.miaixz.bus.auth.protocol.oauth2.DeviceAuthorizationRequest;
import org.miaixz.bus.auth.protocol.oauth2.DeviceAuthorizationResponse;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.codec.DeviceAuthorizationCodec;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
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
 * Calls the RFC 8628 device authorization endpoint for one compiled OAuth 2.x Source.
 *
 * @author Kimi Liu
 */
public final class DeviceAuthorizationClient {

    /**
     * Validated Source options that bind the client identifier and endpoint.
     */
    private final OAuth2ClientOptions options;

    /**
     * Caller-owned execution services and Fabric context.
     */
    private final DriverServices services;

    /**
     * Strict RFC 8628 request and response codec.
     */
    private final DeviceAuthorizationCodec codec;

    /**
     * Shared strict form representation codec.
     */
    private final FormCodec formCodec;

    /**
     * Creates a device authorization client for one compiled Source.
     *
     * @param options  validated OAuth 2.x client options
     * @param services externally owned runtime dependencies
     * @param codec    strict RFC 8628 codec
     * @throws IllegalArgumentException if a collaborator is {@code null} or no device endpoint is configured
     */
    public DeviceAuthorizationClient(final OAuth2ClientOptions options, final DriverServices services,
            final DeviceAuthorizationCodec codec) {
        this.options = Assert.notNull(options, "OAuth 2.x client options must not be null");
        Assert.notNull(
                options.deviceAuthorizationEndpoint().getOrNull(),
                "OAuth 2.x device authorization endpoint must be configured");
        this.services = Assert.notNull(services, "OAuth 2.x execution services must not be null");
        this.codec = Assert.notNull(codec, "OAuth 2.x device authorization codec must not be null");
        this.formCodec = new FormCodec();
    }

    /**
     * Converts codec branch discrimination into the framework outcome boundary.
     *
     * @param decoded standard device authorization success or OAuth error
     * @return successful response, rejected client error, or failed upstream server error
     */
    private static Outcome<DeviceAuthorizationResponse> decoded(final DeviceAuthorizationCodec.Decoded decoded) {
        return switch (decoded) {
            case DeviceAuthorizationCodec.Success success -> Outcome.succeeded(success.response());
            case DeviceAuthorizationCodec.Error error -> remote(error);
        };
    }

    /**
     * Maps a valid standard OAuth endpoint error without exposing its description or device credentials.
     *
     * @param error decoded OAuth error response and original status
     * @return rejected 4xx or failed 5xx outcome
     */
    private static Outcome<DeviceAuthorizationResponse> remote(final DeviceAuthorizationCodec.Error error) {
        final Errors code = error.status() >= Http.Status.INTERNAL_SERVER_ERROR ? ErrorCode._502
                : error.status() == Http.Status.UNAUTHORIZED ? ErrorCode._401 : ErrorCode._400;
        final Outcome.Failure failure = new Outcome.Failure(code,
                "OAuth 2.x device authorization endpoint returned a standard error response",
                new JsonValue.ObjectValue(Map.of()));
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
     * Obtains device and user codes for the registered public client.
     *
     * @param request standard device authorization request
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing the standard RFC 8628 response or closed framework failure
     */
    public CompletionStage<Outcome<DeviceAuthorizationResponse>> deviceAuthorization(
            final DeviceAuthorizationRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "OAuth 2.x device authorization request must not be null");
        Assert.notNull(context, "OAuth 2.x device authorization context must not be null");
        Assert.notNull(timeout, "OAuth 2.x device authorization time budget must not be null");
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(
                            failure(ErrorCode._408, "OAuth 2.x device authorization request has no time budget")));
        }
        if (!options.clientId().equals(request.clientId())) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    "OAuth 2.x device authorization client identifier does not match the Source")));
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
     * Encodes, sends, and decodes one device authorization request.
     *
     * @param request validated request bound to the Source client
     * @param secret  optional owned client-secret lease
     * @param timeout decreasing operation budget
     * @return asynchronous standard device authorization outcome
     */
    private CompletionStage<Outcome<DeviceAuthorizationResponse>> execute(
            final DeviceAuthorizationRequest request,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] body = null;
            try {
                if (timeout.expired()) {
                    return Outcome.<DeviceAuthorizationResponse>failed(
                            failure(
                                    ErrorCode._408,
                                    "OAuth 2.x device authorization request exhausted its time budget"));
                }
                body = formCodec.encode(authenticated(codec.encode(request), secret));
                final var endpoint = options.deviceAuthorizationEndpoint().getOrNull();
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
                return decoded(codec.decode(builder.execute()));
            } catch (RuntimeException exception) {
                return Outcome.<DeviceAuthorizationResponse>failed(
                        failure(ErrorCode._502, "OAuth 2.x device authorization endpoint request failed"));
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
     * Adds the body credential required by the selected client authentication method.
     *
     * @param encoded standard device authorization parameters containing client_id
     * @param secret  optional client-secret lease
     * @return immutable ordered form parameters
     */
    private List<Parameter> authenticated(final List<Parameter> encoded, final SecretLease secret) {
        final List<Parameter> parameters = new ArrayList<>(
                Assert.notNull(encoded, "OAuth 2.x encoded device authorization parameters must not be null"));
        if (Endpoint.Authentication.CLIENT_SECRET_POST.equals(options.clientAuthenticationMethod())) {
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
