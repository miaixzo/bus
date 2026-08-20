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
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.guard.ClientAuthenticator;
import org.miaixz.bus.auth.protocol.oauth2.IntrospectionRequest;
import org.miaixz.bus.auth.protocol.oauth2.IntrospectionResponse;
import org.miaixz.bus.auth.protocol.oauth2.codec.IntrospectionCodec;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Adapts RFC 7662 Fabric HTTP messages to typed token introspection processing.
 *
 * @author Kimi Liu
 */
public final class IntrospectionEndpoint {

    /**
     * Bidirectional RFC 7662 transport codec.
     */
    private final IntrospectionCodec codec;

    /**
     * Standard HTTP client authentication strategy for this protected endpoint.
     */
    private final ClientAuthenticator<HttpRequest> authenticator;

    /**
     * Typed token introspection service.
     */
    private final IntrospectionService service;

    /**
     * Endpoint-aware standard OAuth error mapper.
     */
    private final OAuth2ErrorMapper errorMapper;

    /**
     * Creates an introspection endpoint adapter.
     *
     * @param codec         strict request and response codec
     * @param authenticator standard HTTP client authenticator
     * @param service       typed introspection service
     * @param errorMapper   standard endpoint error mapper
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public IntrospectionEndpoint(final IntrospectionCodec codec, final ClientAuthenticator<HttpRequest> authenticator,
            final IntrospectionService service, final OAuth2ErrorMapper errorMapper) {
        this.codec = Assert.notNull(codec, "OAuth 2.x introspection codec must not be null");
        this.authenticator = Assert
                .notNull(authenticator, "OAuth 2.x introspection client authenticator must not be null");
        this.service = Assert.notNull(service, "OAuth 2.x introspection service must not be null");
        this.errorMapper = Assert.notNull(errorMapper, "OAuth 2.x error mapper must not be null");
    }

    /**
     * Decodes, executes, and encodes one introspection request.
     *
     * @param request immutable Fabric HTTP request
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing a complete standard HTTP response
     */
    public CompletionStage<HttpResponse> handle(
            final HttpRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "OAuth 2.x introspection HTTP request must not be null");
        Assert.notNull(context, "OAuth 2.x introspection context must not be null");
        Assert.notNull(timeout, "OAuth 2.x introspection time budget must not be null");
        final IntrospectionRequest decoded;
        try {
            decoded = codec.decodeRequest(request);
        } catch (RuntimeException exception) {
            return CompletableFuture.completedFuture(errorMapper.introspectionMalformed(request));
        }
        return authenticator.authenticate(request, context, timeout)
                .thenCompose(authenticated -> switch (authenticated) {
                    case Outcome.Succeeded<org.miaixz.bus.auth.resolver.ConsumerMetadata> success -> service
                            .introspect(decoded, context.withClientId(success.value().id()), timeout)
                            .thenApply(outcome -> switch (outcome) {
                                case Outcome.Succeeded<IntrospectionResponse> value -> codec
                                        .encodeResponse(request, value.value());
                                case Outcome.Rejected<IntrospectionResponse> rejected -> errorMapper
                                        .introspection(request, rejected.failure());
                                case Outcome.Failed<IntrospectionResponse> failed -> errorMapper
                                        .introspection(request, failed.failure());
                            });
                    case Outcome.Rejected<org.miaixz.bus.auth.resolver.ConsumerMetadata> rejected -> CompletableFuture
                            .completedFuture(errorMapper.introspection(request, rejected.failure()));
                    case Outcome.Failed<org.miaixz.bus.auth.resolver.ConsumerMetadata> failed -> CompletableFuture
                            .completedFuture(errorMapper.introspection(request, failed.failure()));
                });
    }

}
