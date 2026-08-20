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
package org.miaixz.bus.auth.protocol.oidc.client;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.oidc.UserInfoRequest;
import org.miaixz.bus.auth.protocol.oidc.UserInfoResponse;
import org.miaixz.bus.auth.protocol.oidc.codec.UserInfoCodec;
import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;

/**
 * Retrieves a standard JSON UserInfo response with an OAuth bearer access token.
 *
 * @author Kimi Liu
 */
public final class UserInfoClient {

    /**
     * Validated relying-party options containing the UserInfo endpoint.
     */
    private final OpenIdClientOptions options;

    /**
     * Caller-owned runtime dependencies and Fabric context.
     */
    private final ExecutionServices services;

    /**
     * Strict UserInfo response and bearer-error codec.
     */
    private final UserInfoCodec codec;

    /**
     * Creates a UserInfo client for one compiled OpenID Connect Source.
     *
     * @param options  validated OpenID Connect client options
     * @param services externally owned runtime dependencies
     * @param codec    strict UserInfo codec
     * @throws IllegalArgumentException if a collaborator is {@code null} or UserInfo is not configured
     */
    public UserInfoClient(final OpenIdClientOptions options, final ExecutionServices services,
            final UserInfoCodec codec) {
        this.options = Assert.notNull(options, "OpenID Connect client options must not be null");
        Assert.notNull(options.userInfoEndpoint().getOrNull(), "OpenID Connect UserInfo endpoint must be configured");
        this.services = Assert.notNull(services, "OpenID Connect execution services must not be null");
        this.codec = Assert.notNull(codec, "OpenID Connect UserInfo codec must not be null");
    }

    /**
     * Converts codec-only standard success/error discrimination to the framework outcome boundary.
     *
     * @param decoded decoded UserInfo success or RFC 6750 error
     * @return successful UserInfo response, rejected client error, or failed upstream server error
     */
    private static Outcome<UserInfoResponse> decoded(final UserInfoCodec.Decoded decoded) {
        return switch (decoded) {
            case UserInfoCodec.Success success -> Outcome.succeeded(success.response());
            case UserInfoCodec.Error error -> remote(error);
        };
    }

    /**
     * Maps a valid RFC 6750 bearer error without exposing descriptions or bearer material.
     *
     * @param error decoded bearer error and HTTP status
     * @return rejected protocol 4xx, failed 429 rate limit, or failed 5xx outcome
     */
    private static Outcome<UserInfoResponse> remote(final UserInfoCodec.Error error) {
        final boolean rateLimited = error.status() == Http.Status.TOO_MANY_REQUESTS;
        final boolean upstreamFailure = error.status() >= Http.Status.INTERNAL_SERVER_ERROR;
        final Errors code = rateLimited ? ErrorCode._429
                : upstreamFailure ? ErrorCode._502
                        : error.status() == Http.Status.UNAUTHORIZED ? ErrorCode._401 : ErrorCode._400;
        final Outcome.Failure failure = failure(
                code,
                "OpenID Connect UserInfo endpoint returned a standard bearer error");
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
     * Creates a completed asynchronous result.
     *
     * @param <T>     success value type
     * @param outcome completed outcome
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Retrieves claims authorized by the supplied bearer access token.
     *
     * @param request standard UserInfo request
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing a standard UserInfo response or framework failure
     */
    public CompletionStage<Outcome<UserInfoResponse>> userInfo(
            final UserInfoRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "OpenID Connect UserInfo request must not be null");
        Assert.notNull(context, "OpenID Connect UserInfo context must not be null");
        Assert.notNull(timeout, "OpenID Connect UserInfo time budget must not be null");
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(failure(ErrorCode._408, "OpenID Connect UserInfo request has no time budget")));
        }
        return CompletableFuture.supplyAsync(() -> execute(request, timeout), services.executor());
    }

    /**
     * Executes one HTTPS UserInfo request and closes the response through the codec.
     *
     * @param request validated UserInfo request
     * @param timeout decreasing operation budget
     * @return decoded UserInfo outcome
     */
    private Outcome<UserInfoResponse> execute(final UserInfoRequest request, final Timeout.Budget timeout) {
        try {
            if (timeout.expired()) {
                return Outcome
                        .failed(failure(ErrorCode._408, "OpenID Connect UserInfo request exhausted its time budget"));
            }
            final var endpoint = options.userInfoEndpoint().getOrNull();
            final var response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.GET)
                    .header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + request.accessToken())
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OIDC).addressPolicy()).execute();
            return decoded(codec.decode(response));
        } catch (RuntimeException exception) {
            return Outcome.failed(failure(ErrorCode._502, "OpenID Connect UserInfo endpoint request failed"));
        }
    }

}
