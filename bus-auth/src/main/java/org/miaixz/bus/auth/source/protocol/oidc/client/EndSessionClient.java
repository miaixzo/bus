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
package org.miaixz.bus.auth.source.protocol.oidc.client;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.protocol.oidc.EndSessionRequest;
import org.miaixz.bus.auth.source.protocol.oidc.codec.EndSessionRequestCodec;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Builds an RP-Initiated Logout URL for user-agent navigation.
 * <p>
 * The client does not issue a back-channel HTTP request because the user agent carries the OpenID Provider session and
 * receives any registered post-logout redirect. The specification defines no generic end-session response entity.
 * </p>
 *
 * @author Kimi Liu
 */
public class EndSessionClient {

    /**
     * Validated relying-party options containing the end-session endpoint.
     */
    private final OpenIdClientOptions options;

    /**
     * Strict RP-Initiated Logout query encoder.
     */
    private final EndSessionRequestCodec codec;

    /**
     * Creates an end-session client for one compiled OpenID Connect Source.
     *
     * @param options validated OpenID Connect client options
     * @param codec   strict RP-Initiated Logout request codec
     * @throws IllegalArgumentException if a collaborator is {@code null} or logout is not configured
     */
    public EndSessionClient(final OpenIdClientOptions options, final EndSessionRequestCodec codec) {
        this.options = Assert.notNull(options, "OpenID Connect client options must not be null");
        Assert.notNull(
                options.endSessionEndpoint().getOrNull(),
                "OpenID Connect end-session endpoint must be configured");
        this.codec = Assert.notNull(codec, "OpenID Connect end-session request codec must not be null");
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
     * Encodes an RP-Initiated Logout request into an absolute user-agent URL.
     *
     * @param request standard end-session request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return completed stage containing the logout URL or framework failure
     */
    public CompletionStage<Outcome<Url>> endSession(
            final EndSessionRequest request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "OpenID Connect end-session request must not be null");
        Assert.notNull(context, "OpenID Connect end-session context must not be null");
        Assert.notNull(timeout, "OpenID Connect end-session timeout must not be null");
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(failure(ErrorCode._408, "OpenID Connect end-session request has no timeout")));
        }
        try {
            final Url endpoint = options.endSessionEndpoint().getOrNull().url();
            return completed(Outcome.succeeded(codec.encode(endpoint, request)));
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.rejected(failure(ErrorCode._400, "OpenID Connect end-session request encoding failed")));
        }
    }

}
