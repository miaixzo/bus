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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.FabricX.Request;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.guard.ClientAuthentication;
import org.miaixz.bus.auth.guard.ClientAuthenticator;
import org.miaixz.bus.auth.protocol.oauth2.DeviceAuthorizationRequest;
import org.miaixz.bus.auth.protocol.oauth2.DeviceAuthorizationResponse;
import org.miaixz.bus.auth.protocol.oauth2.codec.DeviceAuthorizationCodec;
import org.miaixz.bus.core.lang.Assert;

/**
 * Adapts RFC 8628 Fabric HTTP messages to typed device authorization processing.
 *
 * @author Kimi Liu
 */
public class DeviceAuthorizationEndpoint {

    /**
     * Bidirectional RFC 8628 transport codec.
     */
    private final DeviceAuthorizationCodec codec;

    /**
     * Standard HTTP client authentication or public-client identification strategy.
     */
    private final ClientAuthenticator<Request> authenticator;

    /**
     * Typed device authorization service.
     */
    private final DeviceAuthorizationService service;

    /**
     * Endpoint-aware standard OAuth error mapper.
     */
    private final OAuth2ErrorMapper errorMapper;

    /**
     * Creates a device authorization endpoint adapter.
     *
     * @param codec         strict request and response codec
     * @param authenticator standard HTTP client authenticator
     * @param service       typed device authorization service
     * @param errorMapper   standard endpoint error mapper
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public DeviceAuthorizationEndpoint(final DeviceAuthorizationCodec codec,
            final ClientAuthenticator<Request> authenticator, final DeviceAuthorizationService service,
            final OAuth2ErrorMapper errorMapper) {
        this.codec = Assert.notNull(codec, "OAuth 2.x device authorization codec must not be null");
        this.authenticator = Assert
                .notNull(authenticator, "OAuth 2.x device authorization client authenticator must not be null");
        this.service = Assert.notNull(service, "OAuth 2.x device authorization service must not be null");
        this.errorMapper = Assert.notNull(errorMapper, "OAuth 2.x error mapper must not be null");
    }

    /**
     * Decodes, executes, and encodes one device authorization request.
     *
     * @param request immutable Fabric HTTP request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing a complete standard HTTP response
     */
    public CompletionStage<Response> handle(final Request request, final Context context, final Timeout timeout) {
        Assert.notNull(request, "OAuth 2.x device authorization HTTP request must not be null");
        Assert.notNull(context, "OAuth 2.x device authorization context must not be null");
        Assert.notNull(timeout, "OAuth 2.x device authorization timeout must not be null");
        final DeviceAuthorizationRequest decoded;
        try {
            decoded = codec.decodeRequest(request);
        } catch (RuntimeException exception) {
            return CompletableFuture.completedFuture(errorMapper.deviceAuthorizationMalformed(request));
        }
        return authenticator.authenticate(request, context, timeout)
                .thenCompose(authenticated -> switch (authenticated) {
                    case Outcome.Succeeded<ClientAuthentication> success -> service.deviceAuthorization(
                            decoded,
                            success.value().consumer(),
                            context.withClientId(success.value().consumer().id()),
                            timeout).thenApply(outcome -> switch (outcome) {
                                case Outcome.Succeeded<DeviceAuthorizationResponse> value -> codec
                                        .encodeResponse(request, value.value());
                                case Outcome.Rejected<DeviceAuthorizationResponse> rejected -> errorMapper
                                        .deviceAuthorization(request, rejected.failure());
                                case Outcome.Failed<DeviceAuthorizationResponse> failed -> errorMapper
                                        .deviceAuthorization(request, failed.failure());
                                default -> throw new IllegalStateException("Unsupported Outcome implementation");
                            });
                    case Outcome.Rejected<ClientAuthentication> rejected -> CompletableFuture
                            .completedFuture(errorMapper.deviceAuthorization(request, rejected.failure()));
                    case Outcome.Failed<ClientAuthentication> failed -> CompletableFuture
                            .completedFuture(errorMapper.deviceAuthorization(request, failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

}
