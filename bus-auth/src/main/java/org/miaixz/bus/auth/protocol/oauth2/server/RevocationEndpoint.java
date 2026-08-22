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
import org.miaixz.bus.auth.protocol.oauth2.RevocationRequest;
import org.miaixz.bus.auth.protocol.oauth2.codec.RevocationRequestDecoder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.net.Http;

/**
 * Adapts RFC 7009 Fabric HTTP requests to typed token revocation processing.
 *
 * @author Kimi Liu
 */
public class RevocationEndpoint {

    /**
     * Strict RFC 7009 request decoder.
     */
    private final RevocationRequestDecoder decoder;

    /**
     * Standard HTTP client authentication or public-client identification strategy.
     */
    private final ClientAuthenticator<Request> authenticator;

    /**
     * Typed token revocation service.
     */
    private final RevocationService service;

    /**
     * Endpoint-aware standard OAuth error mapper.
     */
    private final OAuth2ErrorMapper errorMapper;

    /**
     * Creates a revocation endpoint adapter.
     *
     * @param decoder       strict request decoder
     * @param authenticator standard HTTP client authenticator
     * @param service       typed revocation service
     * @param errorMapper   standard endpoint error mapper
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public RevocationEndpoint(final RevocationRequestDecoder decoder, final ClientAuthenticator<Request> authenticator,
            final RevocationService service, final OAuth2ErrorMapper errorMapper) {
        this.decoder = Assert.notNull(decoder, "OAuth 2.x revocation decoder must not be null");
        this.authenticator = Assert
                .notNull(authenticator, "OAuth 2.x revocation client authenticator must not be null");
        this.service = Assert.notNull(service, "OAuth 2.x revocation service must not be null");
        this.errorMapper = Assert.notNull(errorMapper, "OAuth 2.x error mapper must not be null");
    }

    /**
     * Decodes and executes one revocation request and returns the mandatory empty success response.
     *
     * @param request immutable Fabric HTTP request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing a complete RFC 7009 HTTP response
     */
    public CompletionStage<Response> handle(final Request request, final Context context, final Timeout timeout) {
        Assert.notNull(request, "OAuth 2.x revocation HTTP request must not be null");
        Assert.notNull(context, "OAuth 2.x revocation context must not be null");
        Assert.notNull(timeout, "OAuth 2.x revocation timeout must not be null");
        final RevocationRequest decoded;
        try {
            decoded = decoder.decode(request);
        } catch (RuntimeException exception) {
            return CompletableFuture.completedFuture(errorMapper.revocationMalformed(request));
        }
        return authenticator.authenticate(request, context, timeout)
                .thenCompose(authenticated -> switch (authenticated) {
                    case Outcome.Succeeded<ClientAuthentication> success -> service
                            .revoke(decoded, context.withClientId(success.value().consumer().id()), timeout)
                            .thenApply(outcome -> switch (outcome) {
                                case Outcome.Succeeded<Void> value -> Response.builder().request(request)
                                        .code(Http.Status.OK).build();
                                case Outcome.Rejected<Void> rejected -> errorMapper
                                        .revocation(request, rejected.failure());
                                case Outcome.Failed<Void> failed -> errorMapper.revocation(request, failed.failure());
                                default -> throw new IllegalStateException("Unsupported Outcome implementation");
                            });
                    case Outcome.Rejected<ClientAuthentication> rejected -> CompletableFuture
                            .completedFuture(errorMapper.revocation(request, rejected.failure()));
                    case Outcome.Failed<ClientAuthentication> failed -> CompletableFuture
                            .completedFuture(errorMapper.revocation(request, failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

}
