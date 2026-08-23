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
import org.miaixz.bus.auth.source.protocol.oauth2.AuthorizationRequest;
import org.miaixz.bus.auth.source.protocol.oauth2.codec.AuthorizationRequestDecoder;
import org.miaixz.bus.auth.source.protocol.oauth2.codec.AuthorizationResponseEncoder;
import org.miaixz.bus.auth.source.protocol.oauth2.grant.AuthorizationCodeIssuer;
import org.miaixz.bus.core.lang.Assert;

/**
 * Adapts a Fabric HTTP authorization request to the typed OAuth 2.x authorization service.
 *
 * @author Kimi Liu
 */
public class AuthorizationEndpoint {

    /**
     * Strict authorization request decoder.
     */
    private final AuthorizationRequestDecoder decoder;

    /**
     * Typed authorization business service.
     */
    private final AuthorizationService service;

    /**
     * Strict successful authorization response encoder.
     */
    private final AuthorizationResponseEncoder encoder;

    /**
     * Endpoint-aware standard OAuth error mapper.
     */
    private final OAuth2ErrorMapper errorMapper;

    /**
     * Creates an authorization transport adapter from its four exact collaborators.
     *
     * @param decoder     strict request decoder
     * @param service     typed authorization service
     * @param encoder     strict success response encoder
     * @param errorMapper standard endpoint error mapper
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public AuthorizationEndpoint(final AuthorizationRequestDecoder decoder, final AuthorizationService service,
            final AuthorizationResponseEncoder encoder, final OAuth2ErrorMapper errorMapper) {
        this.decoder = Assert.notNull(decoder, "OAuth 2.x authorization decoder must not be null");
        this.service = Assert.notNull(service, "OAuth 2.x authorization service must not be null");
        this.encoder = Assert.notNull(encoder, "OAuth 2.x authorization encoder must not be null");
        this.errorMapper = Assert.notNull(errorMapper, "OAuth 2.x error mapper must not be null");
    }

    /**
     * Decodes, authorizes, and encodes one Fabric HTTP authorization request.
     *
     * @param request immutable Fabric HTTP request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing a complete standard HTTP response
     */
    public CompletionStage<Response> handle(final Request request, final Context context, final Timeout timeout) {
        Assert.notNull(request, "OAuth 2.x authorization HTTP request must not be null");
        Assert.notNull(context, "OAuth 2.x authorization context must not be null");
        Assert.notNull(timeout, "OAuth 2.x authorization timeout must not be null");
        final AuthorizationRequest decoded;
        try {
            decoded = decoder.decode(request);
        } catch (RuntimeException exception) {
            return CompletableFuture.completedFuture(errorMapper.authorizationMalformed(request));
        }
        return service.authorizeEndpoint(decoded, context, timeout).thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<AuthorizationCodeIssuer.Result> success -> encoder
                    .encode(request, success.value().redirectUri(), success.value().response());
            case Outcome.Rejected<AuthorizationCodeIssuer.Result> rejected -> errorMapper
                    .authorization(request, decoded, rejected.failure());
            case Outcome.Failed<AuthorizationCodeIssuer.Result> failed -> errorMapper
                    .authorization(request, decoded, failed.failure());
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

}
