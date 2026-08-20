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
package org.miaixz.bus.auth.protocol.oauth2.codec;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;

/**
 * Encodes a successful OAuth 2.x token response as its standard HTTP JSON representation.
 * <p>
 * Registered members retain their RFC names and types, while unknown extension members retain their provider-neutral
 * JSON types. This encoder never emits an OAuth error response or a framework-specific envelope.
 * </p>
 *
 * @author Kimi Liu
 */
public final class TokenResponseEncoder {

    /**
     * Provider-neutral JSON service used for RFC 8259 serialization.
     */
    private final JsonProvider jsonProvider;

    /**
     * Creates a standard token response encoder.
     *
     * @param jsonProvider provider-neutral JSON service
     * @throws IllegalArgumentException if the provider is {@code null}
     */
    public TokenResponseEncoder(final JsonProvider jsonProvider) {
        this.jsonProvider = Assert.notNull(jsonProvider, "OAuth 2.x token response JSON provider must not be null");
    }

    /**
     * Adds one provider-neutral extension after defensively checking its member name.
     *
     * @param members mutable insertion-ordered response members
     * @param name    extension member name
     * @param value   immutable provider-neutral JSON value
     */
    private static void extension(final Map<String, JsonValue> members, final String name, final JsonValue value) {
        members.put(name, value);
    }

    /**
     * Adds the common successful token response components.
     *
     * @param members      mutable response members
     * @param accessToken  issued token value
     * @param tokenType    access-token usage type
     * @param expiresIn    optional lifetime in seconds
     * @param refreshToken optional refresh token
     * @param scope        optional effective scope
     */
    private static void common(
            final Map<String, JsonValue> members,
            final String accessToken,
            final TokenType tokenType,
            final Optional<Long> expiresIn,
            final Optional<String> refreshToken,
            final Optional<Scope> scope) {
        members.put(OAuth2.Parameters.ACCESS_TOKEN, new JsonValue.StringValue(accessToken));
        members.put(OAuth2.Parameters.TOKEN_TYPE, new JsonValue.StringValue(tokenType.value()));
        expiresIn.ifPresent(
                value -> members
                        .put(OAuth2.Parameters.EXPIRES_IN, new JsonValue.NumberValue(BigDecimal.valueOf(value))));
        refreshToken.ifPresent(value -> members.put(OAuth2.Parameters.REFRESH_TOKEN, new JsonValue.StringValue(value)));
        scope.ifPresent(value -> members.put(OAuth2.Parameters.SCOPE, new JsonValue.StringValue(value.format())));
    }

    /**
     * Encodes one successful token response with mandatory cache-prevention headers.
     *
     * @param request  originating token endpoint HTTP request
     * @param response standard successful token response
     * @return complete HTTP 200 JSON response
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if an extension duplicates a registered response member
     */
    public HttpResponse encode(final HttpRequest request, final TokenEndpointResponse response) {
        Assert.notNull(request, "OAuth 2.x token HTTP request must not be null");
        Assert.notNull(response, "OAuth 2.x token response must not be null");
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        if (response.getClass() == TokenResponse.class) {
            final TokenResponse token = (TokenResponse) response;
            common(
                    members,
                    token.accessToken(),
                    token.tokenType(),
                    token.expiresIn(),
                    token.refreshToken(),
                    token.scope());
            token.extensions().values().forEach((name, value) -> extension(members, name, value));
        } else if (response.getClass() == TokenExchangeResponse.class) {
            final TokenExchangeResponse exchange = (TokenExchangeResponse) response;
            common(
                    members,
                    exchange.accessToken(),
                    exchange.tokenType(),
                    exchange.expiresIn(),
                    exchange.refreshToken(),
                    exchange.scope());
            members.put(OAuth2.Parameters.ISSUED_TOKEN_TYPE, new JsonValue.StringValue(exchange.issuedTokenType()));
            exchange.extensions().values().forEach((name, value) -> extension(members, name, value));
        } else {
            throw new ValidateException("OAuth 2.x token response encoder accepts only OAuth success response types");
        }
        final byte[] body = jsonProvider.writeValue(new JsonValue.ObjectValue(members));
        return HttpResponse.builder().request(request).code(Http.Status.OK).headers(
                Headers.of(Http.Header.CACHE_CONTROL, Http.Cache.NO_STORE, Http.Header.PRAGMA, Http.Cache.NO_CACHE))
                .body(PayloadBody.of(Payload.of(body), MediaType.APPLICATION_JSON_TYPE)).build();
    }

}
