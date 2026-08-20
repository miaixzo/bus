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
package org.miaixz.bus.auth.protocol.oidc.server;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.oidc.OpenIdProviderMetadata;
import org.miaixz.bus.auth.protocol.oidc.codec.OpenIdProviderMetadataCodec;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Adapts the OpenID Provider Metadata resource to typed discovery service execution.
 *
 * @author Kimi Liu
 */
public final class DiscoveryEndpoint {

    /**
     * Strict discovery request validator and metadata response codec.
     */
    private final OpenIdProviderMetadataCodec codec;

    /**
     * Typed discovery metadata service.
     */
    private final DiscoveryService service;

    /**
     * Endpoint-aware standard OpenID Connect error mapper.
     */
    private final OpenIdErrorMapper errorMapper;

    /**
     * Creates a discovery endpoint adapter.
     *
     * @param codec       strict metadata codec
     * @param service     typed discovery service
     * @param errorMapper standard endpoint error mapper
     * @throws IllegalArgumentException if any collaborator is {@code null}
     */
    public DiscoveryEndpoint(final OpenIdProviderMetadataCodec codec, final DiscoveryService service,
            final OpenIdErrorMapper errorMapper) {
        this.codec = Assert.notNull(codec, "OpenID Connect metadata codec must not be null");
        this.service = Assert.notNull(service, "OpenID Connect discovery service must not be null");
        this.errorMapper = Assert.notNull(errorMapper, "OpenID Connect error mapper must not be null");
    }

    /**
     * Validates the discovery request and returns current Provider metadata.
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
        Assert.notNull(request, "OpenID Connect discovery HTTP request must not be null");
        Assert.notNull(context, "OpenID Connect discovery context must not be null");
        Assert.notNull(timeout, "OpenID Connect discovery time budget must not be null");
        try {
            codec.validateRequest(request);
        } catch (RuntimeException exception) {
            return CompletableFuture.completedFuture(errorMapper.discoveryMalformed(request));
        }
        return service.discover(context, timeout).thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<OpenIdProviderMetadata> success -> codec.encodeResponse(request, success.value());
            case Outcome.Rejected<OpenIdProviderMetadata> rejected -> errorMapper
                    .discovery(request, rejected.failure());
            case Outcome.Failed<OpenIdProviderMetadata> failed -> errorMapper.discovery(request, failed.failure());
        });
    }

}
