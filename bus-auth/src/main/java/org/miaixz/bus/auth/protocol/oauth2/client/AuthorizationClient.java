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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationRequest;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationRequestEncoder;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Builds an OAuth 2.x authorization URL for a user-agent interaction.
 * <p>
 * This client never calls the authorization endpoint directly. The external Web project later captures the browser
 * callback as {@code Callback.Inbound}, after which the protocol response decoder processes it independently.
 * </p>
 *
 * @author Kimi Liu
 */
public class AuthorizationClient {

    /**
     * Immutable registration options used to bind the outgoing request to one Source.
     */
    private final OAuth2ClientOptions options;

    /**
     * Encoder bound to the compiled Source authorization endpoint.
     */
    private final AuthorizationRequestEncoder encoder;

    /**
     * Creates an authorization client for one compiled Source.
     *
     * @param options validated Source client options
     * @param encoder standard request-to-URL encoder
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public AuthorizationClient(final OAuth2ClientOptions options, final AuthorizationRequestEncoder encoder) {
        this.options = Assert.notNull(options, "OAuth 2.x client options must not be null");
        this.encoder = Assert.notNull(encoder, "OAuth 2.x authorization request encoder must not be null");
    }

    /**
     * Creates a safe framework failure without retaining the caught exception or request values.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive diagnostic text
     * @return closed failure value
     */
    private static Outcome.Failure failure(final Errors code, final String description) {
        return new Outcome.Failure(code, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates a type-inferred completed stage.
     *
     * @param <T>     success value type
     * @param outcome completed outcome
     * @return completed asynchronous stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Encodes a standard authorization request into an absolute user-agent URL.
     *
     * @param request standard authorization request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return completed stage containing an authorization URL or closed framework failure
     */
    public CompletionStage<Outcome<Url>> authorize(
            final AuthorizationRequest request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "OAuth 2.x authorization request must not be null");
        Assert.notNull(context, "OAuth 2.x authorization invocation context must not be null");
        Assert.notNull(timeout, "OAuth 2.x authorization timeout must not be null");
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(failure(ErrorCode._408, "OAuth 2.x authorization has no remaining timeout")));
        }
        if (!options.clientId().equals(request.clientId())) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    "OAuth 2.x authorization client identifier does not match the Source")));
        }
        final String redirectUri = request.redirectUri().getOrNull();
        if (redirectUri != null && !options.redirectUris().contains(redirectUri)) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    "OAuth 2.x authorization redirect URI is not registered for the Source")));
        }
        if (options.pkceRequired() && (request.codeChallenge().isEmpty() || request.codeChallengeMethod().isEmpty())) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    "OAuth 2.x authorization requires an explicit PKCE challenge and method")));
        }
        try {
            return completed(Outcome.succeeded(encoder.encode(request)));
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.failed(failure(ErrorCode._400, "OAuth 2.x authorization request encoding failed")));
        }
    }

}
