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
package org.miaixz.bus.auth.source.protocol.oauth2.server;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.FabricX.Request;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.protocol.oauth2.TokenEndpointResponse;
import org.miaixz.bus.auth.source.protocol.oauth2.TokenRequest;
import org.miaixz.bus.auth.source.protocol.oauth2.codec.TokenRequestDecoder;
import org.miaixz.bus.auth.source.protocol.oauth2.codec.TokenResponseEncoder;
import org.miaixz.bus.core.lang.Assert;

/**
 * Adapts the single OAuth 2.x token endpoint to typed grant processing.
 *
 * @author Kimi Liu
 */
public class TokenEndpoint {

    /**
     * Strict standard token request decoder.
     */
    private final TokenRequestDecoder decoder;

    /**
     * Standard HTTP client authentication strategy for this endpoint.
     */
    private final ClientAuthenticator<Request> authenticator;

    /**
     * Typed token service shared by every enabled grant.
     */
    private final TokenService service;

    /**
     * Strict standard token response encoder.
     */
    private final TokenResponseEncoder encoder;

    /**
     * Endpoint-aware standard OAuth error mapper.
     */
    private final OAuth2ErrorMapper errorMapper;

    /**
     * Creates a token endpoint adapter from exact codec, service, and error collaborators.
     *
     * @param decoder       strict request decoder
     * @param authenticator standard HTTP client authenticator
     * @param service       typed token service
     * @param encoder       strict response encoder
     * @param errorMapper   standard endpoint error mapper
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public TokenEndpoint(final TokenRequestDecoder decoder, final ClientAuthenticator<Request> authenticator,
            final TokenService service, final TokenResponseEncoder encoder, final OAuth2ErrorMapper errorMapper) {
        this.decoder = Assert.notNull(decoder, "OAuth 2.x token decoder must not be null");
        this.authenticator = Assert.notNull(authenticator, "OAuth 2.x token client authenticator must not be null");
        this.service = Assert.notNull(service, "OAuth 2.x token service must not be null");
        this.encoder = Assert.notNull(encoder, "OAuth 2.x token encoder must not be null");
        this.errorMapper = Assert.notNull(errorMapper, "OAuth 2.x error mapper must not be null");
    }

    /**
     * Decodes, executes, and encodes one token endpoint request.
     *
     * @param request immutable Fabric HTTP request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing a complete standard HTTP response
     */
    public CompletionStage<Response> handle(final Request request, final Context context, final Timeout timeout) {
        Assert.notNull(request, "OAuth 2.x token HTTP request must not be null");
        Assert.notNull(context, "OAuth 2.x token context must not be null");
        Assert.notNull(timeout, "OAuth 2.x token timeout must not be null");
        final TokenRequest decoded;
        try {
            decoded = decoder.decode(request);
        } catch (RuntimeException exception) {
            return CompletableFuture.completedFuture(errorMapper.tokenMalformed(request, exception));
        }
        return authenticator.authenticate(request, context, timeout)
                .thenCompose(authenticated -> switch (authenticated) {
                    case Outcome.Succeeded<ClientAuthentication> success -> service.token(
                            decoded,
                            success.value(),
                            context.withClientId(success.value().consumer().id()),
                            timeout).thenApply(outcome -> switch (outcome) {
                                case Outcome.Succeeded<TokenEndpointResponse> value -> encoder
                                        .encode(request, value.value());
                                case Outcome.Rejected<TokenEndpointResponse> rejected -> errorMapper
                                        .token(request, rejected.failure());
                                case Outcome.Failed<TokenEndpointResponse> failed -> errorMapper
                                        .token(request, failed.failure());
                                default -> throw new IllegalStateException("Unsupported Outcome implementation");
                            });
                    case Outcome.Rejected<ClientAuthentication> rejected -> CompletableFuture
                            .completedFuture(errorMapper.token(request, rejected.failure()));
                    case Outcome.Failed<ClientAuthentication> failed -> CompletableFuture
                            .completedFuture(errorMapper.token(request, failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

}
