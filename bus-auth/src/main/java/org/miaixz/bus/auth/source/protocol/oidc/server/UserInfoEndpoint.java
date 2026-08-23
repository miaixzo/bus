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
import org.miaixz.bus.auth.source.protocol.oidc.UserInfoRequest;
import org.miaixz.bus.auth.source.protocol.oidc.UserInfoResponse;
import org.miaixz.bus.auth.source.protocol.oidc.codec.UserInfoCodec;
import org.miaixz.bus.core.lang.Assert;

/**
 * Adapts the OpenID Connect UserInfo protected resource to typed service execution.
 *
 * @author Kimi Liu
 */
public class UserInfoEndpoint {

    /**
     * Strict bearer request and JSON response codec.
     */
    private final UserInfoCodec codec;

    /**
     * Typed UserInfo service.
     */
    private final UserInfoService service;

    /**
     * Endpoint-aware standard OpenID Connect error mapper.
     */
    private final OpenIdErrorMapper errorMapper;

    /**
     * Creates a UserInfo endpoint adapter.
     *
     * @param codec       strict UserInfo codec
     * @param service     typed UserInfo service
     * @param errorMapper standard endpoint error mapper
     * @throws IllegalArgumentException if any collaborator is {@code null}
     */
    public UserInfoEndpoint(final UserInfoCodec codec, final UserInfoService service,
            final OpenIdErrorMapper errorMapper) {
        this.codec = Assert.notNull(codec, "OpenID Connect UserInfo codec must not be null");
        this.service = Assert.notNull(service, "OpenID Connect UserInfo service must not be null");
        this.errorMapper = Assert.notNull(errorMapper, "OpenID Connect error mapper must not be null");
    }

    /**
     * Decodes, executes, and encodes one bearer-authenticated UserInfo request.
     *
     * @param request immutable Fabric HTTP request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing a complete standard HTTP response
     */
    public CompletionStage<Response> handle(final Request request, final Context context, final Timeout timeout) {
        Assert.notNull(request, "OpenID Connect UserInfo HTTP request must not be null");
        Assert.notNull(context, "OpenID Connect UserInfo context must not be null");
        Assert.notNull(timeout, "OpenID Connect UserInfo timeout must not be null");
        final UserInfoRequest decoded;
        try {
            decoded = codec.decodeRequest(request);
        } catch (RuntimeException exception) {
            return CompletableFuture.completedFuture(errorMapper.userInfoMalformed(request));
        }
        return service.userInfo(decoded, context, timeout).thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<UserInfoResponse> success -> codec.encodeResponse(request, success.value());
            case Outcome.Rejected<UserInfoResponse> rejected -> errorMapper.userInfo(request, rejected.failure());
            case Outcome.Failed<UserInfoResponse> failed -> errorMapper.userInfo(request, failed.failure());
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

}
