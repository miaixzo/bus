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
package org.miaixz.bus.auth.protocol.oidc.codec;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.FabricX.Body;
import org.miaixz.bus.auth.FabricX.Headers;
import org.miaixz.bus.auth.FabricX.Request;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.shared.jose.Jwk;
import org.miaixz.bus.auth.shared.jose.JwkSet;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Encodes and decodes RFC 7517 JWK Set resource representations for OpenID Connect.
 * <p>
 * Server encoding accepts only asymmetric public material and applies {@link Jwk#publicOnly()} to every member before
 * serialization. Client decoding is bounded, provider-neutral, and closes the owned response without selecting an
 * execution key or dereferencing certificate URLs.
 * </p>
 *
 * @author Kimi Liu
 */
public class JwkSetCodec {

    /**
     * Maximum accepted public JWK Set document size.
     */
    private static final long MAXIMUM_JSON_BYTES = Builder.MAXIMUM_DOCUMENT_BYTES;

    /**
     * Externally selected provider-neutral JSON implementation.
     */
    private final JsonProvider jsonProvider;

    /**
     * Creates a strict JWK Set resource codec.
     *
     * @param jsonProvider externally selected provider-neutral JSON implementation
     * @throws IllegalArgumentException if {@code jsonProvider} is {@code null}
     */
    public JwkSetCodec(final JsonProvider jsonProvider) {
        this.jsonProvider = Assert.notNull(jsonProvider, "OpenID Connect JWK Set JSON provider must not be null");
    }

    /**
     * Validates a public JWK Set resource request.
     *
     * @param request immutable Fabric HTTP request
     * @throws IllegalArgumentException if {@code request} is {@code null}
     * @throws ValidateException        if method, query, fragment, or body is invalid
     */
    public void validateRequest(final Request request) {
        Assert.notNull(request, "OpenID Connect JWK Set HTTP request must not be null");
        if (request.method() != Http.Method.GET || !request.url().query().isEmpty() || request.url().fragment() != null
                || request.body().length() != 0L) {
            throw new ValidateException(
                    "OpenID Connect JWK Set resource requires GET without query, fragment, or body");
        }
    }

    /**
     * Encodes one public-only JWK Set as HTTP 200 application/jwk-set+json.
     *
     * @param request originating Fabric HTTP request
     * @param jwkSet  validated public JWK Set
     * @return complete cache-prevented HTTP response
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if a key is private, symmetric, or not safely publishable
     */
    public Response encodeResponse(final Request request, final JwkSet jwkSet) {
        Assert.notNull(request, "OpenID Connect JWK Set HTTP request must not be null");
        Assert.notNull(jwkSet, "OpenID Connect JWK Set must not be null");
        final List<Jwk> publicKeys = new ArrayList<>(jwkSet.keys().size());
        for (Jwk key : jwkSet.keys()) {
            if (key.hasPrivateMaterial() || "oct".equals(key.keyType())) {
                throw new ValidateException("OpenID Connect JWK Set response cannot publish private or symmetric keys");
            }
            publicKeys.add(key.publicOnly());
        }
        final JwkSet publication = new JwkSet(publicKeys, jwkSet.extensions());
        final byte[] body = jsonProvider.writeValue(publication.toJson());
        return Response.builder().request(request).code(Http.Status.OK).headers(
                Headers.of(Http.Header.CACHE_CONTROL, Http.Cache.NO_STORE, Http.Header.PRAGMA, Http.Cache.NO_CACHE))
                .body(Body.of(body, MediaType.APPLICATION_JWK_SET_JSON_TYPE)).build();
    }

    /**
     * Decodes and closes one bounded HTTP 200 public JWK Set response.
     *
     * @param response owned Fabric HTTP response
     * @return validated RFC 7517 JWK Set
     * @throws IllegalArgumentException if {@code response} is {@code null}
     * @throws ValidateException        if status, media, size, JSON, or JWK shape is invalid
     */
    public JwkSet decode(final Response response) {
        final Response encoded = Assert.notNull(response, "OpenID Connect JWK Set HTTP response must not be null");
        try (encoded) {
            if (encoded.code() != Http.Status.OK) {
                throw new ValidateException("OpenID Connect JWK Set endpoint must return HTTP 200");
            }
            if (encoded.body().length() > MAXIMUM_JSON_BYTES) {
                throw new ValidateException("OpenID Connect JWK Set response exceeds one MiB");
            }
            final MediaType media = encoded.body().media();
            if (!MediaType.APPLICATION_JWK_SET_JSON_TYPE.isCompatible(media)) {
                throw new ValidateException("OpenID Connect JWK Set response must use application/jwk-set+json");
            }
            final String charset = media.parameter(MediaType.CHARSET_PARAMETER);
            if (charset != null && !Charset.UTF_8.equals(media.charset())) {
                throw new ValidateException("OpenID Connect JWK Set response charset must be UTF-8");
            }
            final JsonValue value = jsonProvider.readValue(encoded.bytes(MAXIMUM_JSON_BYTES));
            if (!(value instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("OpenID Connect JWK Set JSON root must be an object");
            }
            return JwkSet.fromJson(object);
        }
    }

}
