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
import org.miaixz.bus.auth.source.protocol.oauth2.AuthorizationServerMetadata;
import org.miaixz.bus.auth.source.protocol.oauth2.codec.AuthorizationServerMetadataCodec;
import org.miaixz.bus.core.lang.Assert;

/**
 * Adapts the RFC 8414 metadata resource to typed authorization server metadata production.
 *
 * @author Kimi Liu
 */
public class AuthorizationServerMetadataEndpoint {

    /**
     * Strict RFC 8414 request validator and response codec.
     */
    private final AuthorizationServerMetadataCodec codec;

    /**
     * Typed authorization server metadata service.
     */
    private final AuthorizationServerMetadataService service;

    /**
     * Endpoint-aware standard OAuth error mapper.
     */
    private final OAuth2ErrorMapper errorMapper;

    /**
     * Creates a metadata endpoint adapter.
     *
     * @param codec       strict request validator and response codec
     * @param service     typed metadata service
     * @param errorMapper standard endpoint error mapper
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public AuthorizationServerMetadataEndpoint(final AuthorizationServerMetadataCodec codec,
            final AuthorizationServerMetadataService service, final OAuth2ErrorMapper errorMapper) {
        this.codec = Assert.notNull(codec, "OAuth 2.x authorization server metadata codec must not be null");
        this.service = Assert.notNull(service, "OAuth 2.x authorization server metadata service must not be null");
        this.errorMapper = Assert.notNull(errorMapper, "OAuth 2.x error mapper must not be null");
    }

    /**
     * Validates the resource request and returns current enabled authorization-server metadata.
     *
     * @param request immutable Fabric HTTP request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing a complete RFC 8414 HTTP response
     */
    public CompletionStage<Response> handle(final Request request, final Context context, final Timeout timeout) {
        Assert.notNull(request, "OAuth 2.x metadata HTTP request must not be null");
        Assert.notNull(context, "OAuth 2.x metadata context must not be null");
        Assert.notNull(timeout, "OAuth 2.x metadata timeout must not be null");
        try {
            codec.validateRequest(request);
        } catch (RuntimeException exception) {
            return CompletableFuture.completedFuture(errorMapper.metadataMalformed(request));
        }
        return service.metadata(context, timeout).thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<AuthorizationServerMetadata> success -> codec
                    .encodeResponse(request, success.value());
            case Outcome.Rejected<AuthorizationServerMetadata> rejected -> errorMapper
                    .metadata(request, rejected.failure());
            case Outcome.Failed<AuthorizationServerMetadata> failed -> errorMapper.metadata(request, failed.failure());
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

}
