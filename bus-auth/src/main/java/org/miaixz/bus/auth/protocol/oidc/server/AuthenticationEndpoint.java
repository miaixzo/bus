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
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationResponse;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationResponseEncoder;
import org.miaixz.bus.auth.protocol.oauth2.server.OAuth2ErrorMapper;
import org.miaixz.bus.auth.protocol.oidc.AuthenticationRequest;
import org.miaixz.bus.auth.protocol.oidc.codec.AuthenticationRequestDecoder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Adapts one OpenID Connect Authentication Request HTTP boundary to the typed authentication service.
 *
 * @author Kimi Liu
 */
public final class AuthenticationEndpoint {

    /**
     * Strict decoder for the incoming OpenID Connect Authentication Request.
     */
    private final AuthenticationRequestDecoder decoder;

    /**
     * Typed service that validates and authorizes the decoded request.
     */
    private final AuthenticationService service;

    /**
     * Encoder for successful OAuth authorization endpoint responses.
     */
    private final AuthorizationResponseEncoder encoder;

    /**
     * Mapper for safe malformed-request and authorization error responses.
     */
    private final OAuth2ErrorMapper errorMapper;

    /**
     * Creates the complete OpenID Connect authorization endpoint adapter.
     *
     * @param decoder     strict Authentication Request decoder
     * @param service     typed OpenID Connect authentication service
     * @param encoder     successful authorization response encoder
     * @param errorMapper safe OAuth authorization error mapper
     */
    public AuthenticationEndpoint(final AuthenticationRequestDecoder decoder, final AuthenticationService service,
            final AuthorizationResponseEncoder encoder, final OAuth2ErrorMapper errorMapper) {
        this.decoder = Assert.notNull(decoder, "OpenID Connect Authentication Request decoder must not be null");
        this.service = Assert.notNull(service, "OpenID Connect authentication service must not be null");
        this.encoder = Assert.notNull(encoder, "OpenID Connect authorization response encoder must not be null");
        this.errorMapper = Assert.notNull(errorMapper, "OpenID Connect authorization error mapper must not be null");
    }

    /**
     * Decodes, authorizes, and encodes one OpenID Connect authorization endpoint request.
     *
     * @param request incoming authorization endpoint HTTP request
     * @param context immutable invocation context containing authenticated subject state
     * @param timeout shared end-to-end operation budget
     * @return stage containing the complete HTTP response
     */
    public CompletionStage<HttpResponse> handle(
            final HttpRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "OpenID Connect authentication HTTP request must not be null");
        Assert.notNull(context, "OpenID Connect authentication context must not be null");
        Assert.notNull(timeout, "OpenID Connect authentication budget must not be null");
        final AuthenticationRequest decoded;
        try {
            decoded = decoder.decode(request);
        } catch (RuntimeException cause) {
            return CompletableFuture.completedFuture(errorMapper.authorizationMalformed(request));
        }
        return service.authorize(decoded, context, timeout).thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<AuthorizationResponse> success -> encoder
                    .encode(request, decoded.authorizationRequest().redirectUri().getOrNull(), success.value());
            case Outcome.Rejected<AuthorizationResponse> rejected -> errorMapper
                    .authorization(request, decoded.authorizationRequest(), rejected.failure());
            case Outcome.Failed<AuthorizationResponse> failed -> errorMapper
                    .authorization(request, decoded.authorizationRequest(), failed.failure());
        });
    }

}
