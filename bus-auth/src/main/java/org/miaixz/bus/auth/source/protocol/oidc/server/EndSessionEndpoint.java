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
package org.miaixz.bus.auth.source.protocol.oidc.server;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.FabricX.Request;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.protocol.oidc.EndSessionRequest;
import org.miaixz.bus.auth.source.protocol.oidc.codec.EndSessionRequestCodec;
import org.miaixz.bus.core.lang.Assert;

/**
 * Adapts the RP-Initiated Logout endpoint without creating a non-standard response entity.
 *
 * @author Kimi Liu
 */
public class EndSessionEndpoint {

    /**
     * Strict end-session query and success-response codec.
     */
    private final EndSessionRequestCodec codec;

    /**
     * Typed end-session validation and session-termination service.
     */
    private final EndSessionService service;

    /**
     * Endpoint-aware standard OpenID Connect error mapper.
     */
    private final OpenIdErrorMapper errorMapper;

    /**
     * Creates an RP-Initiated Logout endpoint adapter.
     *
     * @param codec       strict end-session request codec
     * @param service     typed end-session service
     * @param errorMapper standard endpoint error mapper
     * @throws IllegalArgumentException if any collaborator is {@code null}
     */
    public EndSessionEndpoint(final EndSessionRequestCodec codec, final EndSessionService service,
            final OpenIdErrorMapper errorMapper) {
        this.codec = Assert.notNull(codec, "OpenID Connect end-session codec must not be null");
        this.service = Assert.notNull(service, "OpenID Connect end-session service must not be null");
        this.errorMapper = Assert.notNull(errorMapper, "OpenID Connect error mapper must not be null");
    }

    /**
     * Decodes, validates, terminates, and completes one RP-Initiated Logout request.
     *
     * @param request immutable Fabric HTTP request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing an empty success or validated post-logout redirect response
     */
    public CompletionStage<Response> handle(final Request request, final Context context, final Timeout timeout) {
        Assert.notNull(request, "OpenID Connect end-session HTTP request must not be null");
        Assert.notNull(context, "OpenID Connect end-session context must not be null");
        Assert.notNull(timeout, "OpenID Connect end-session timeout must not be null");
        final EndSessionRequest decoded;
        try {
            decoded = codec.decodeRequest(request);
        } catch (RuntimeException exception) {
            return CompletableFuture.completedFuture(errorMapper.endSessionMalformed(request));
        }
        return service.endSession(decoded, context, timeout).thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<Void> success -> codec.encodeSuccess(request, decoded);
            case Outcome.Rejected<Void> rejected -> errorMapper.endSession(request, rejected.failure());
            case Outcome.Failed<Void> failed -> errorMapper.endSession(request, failed.failure());
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

}
