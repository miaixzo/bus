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

import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.FabricX.Body;
import org.miaixz.bus.auth.FabricX.Request;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.protocol.oauth2.TokenErrorResponse;
import org.miaixz.bus.auth.protocol.oauth2.TokenResponse;
import org.miaixz.bus.auth.protocol.oauth2.codec.TokenResponseDecoder;
import org.miaixz.bus.auth.protocol.oauth2.codec.TokenResponseEncoder;
import org.miaixz.bus.auth.protocol.oidc.IdToken;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.protocol.oidc.OpenIdTokenResponse;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Composes the OAuth token response codecs with the OpenID Connect ID Token response member.
 *
 * @author Kimi Liu
 */
public class OpenIdTokenResponseCodec {

    /**
     * Provider-neutral JSON service.
     */
    private final JsonProvider jsonProvider;

    /**
     * Shared OAuth token response decoder.
     */
    private final TokenResponseDecoder oauthDecoder;

    /**
     * Shared OAuth token response encoder.
     */
    private final TokenResponseEncoder oauthEncoder;

    /**
     * Creates an OpenID Connect token response codec from shared OAuth collaborators.
     *
     * @param jsonProvider provider-neutral JSON service
     * @param oauthDecoder strict OAuth token response decoder
     * @param oauthEncoder strict OAuth token response encoder
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public OpenIdTokenResponseCodec(final JsonProvider jsonProvider, final TokenResponseDecoder oauthDecoder,
            final TokenResponseEncoder oauthEncoder) {
        this.jsonProvider = Assert.notNull(jsonProvider, "OpenID Connect token JSON provider must not be null");
        this.oauthDecoder = Assert.notNull(oauthDecoder, "OpenID Connect OAuth token decoder must not be null");
        this.oauthEncoder = Assert.notNull(oauthEncoder, "OpenID Connect OAuth token encoder must not be null");
    }

    /**
     * Validates JSON media metadata before materializing the response body.
     *
     * @param response response to validate
     */
    private static void validateMedia(final Response response) {
        if (response.body().length() > Builder.MAXIMUM_DOCUMENT_BYTES) {
            throw new ValidateException("OpenID Connect token response exceeds the maximum JSON size");
        }
        final MediaType media = response.body().media();
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(media)) {
            throw new ValidateException("OpenID Connect token response must use application/json");
        }
        final String charset = media.parameter(MediaType.CHARSET_PARAMETER);
        if (charset != null && !Charset.UTF_8.equals(media.charset())) {
            throw new ValidateException("OpenID Connect token response charset must be UTF-8");
        }
    }

    /**
     * Decodes one owned token endpoint response and closes it on every path.
     *
     * @param response owned token endpoint response
     * @return OpenID Connect success or standard token error branch
     */
    public Decoded decode(final Response response) {
        final Response encoded = Assert.notNull(response, "OpenID Connect token HTTP response must not be null");
        try (encoded) {
            validateMedia(encoded);
            final JsonValue value = jsonProvider.readValue(encoded.bytes(Builder.MAXIMUM_DOCUMENT_BYTES));
            if (!(value instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("OpenID Connect token response JSON root must be an object");
            }
            return decode(encoded.code(), object);
        }
    }

    /**
     * Decodes a parsed OpenID Connect token response by delegating OAuth members unchanged.
     *
     * @param status original HTTP status
     * @param body   parsed JSON body
     * @return OpenID Connect success or standard token error branch
     */
    public Decoded decode(final int status, final JsonValue.ObjectValue body) {
        Assert.notNull(body, "OpenID Connect token response object must not be null");
        final Map<String, JsonValue> oauthMembers = new LinkedHashMap<>(body.values());
        final JsonValue idTokenValue = oauthMembers.remove(OpenIdConnect.Parameters.ID_TOKEN);
        final TokenResponseDecoder.Decoded decoded = oauthDecoder
                .decode(status, new JsonValue.ObjectValue(oauthMembers));
        return switch (decoded) {
            case TokenResponseDecoder.Error error -> {
                if (idTokenValue != null) {
                    throw new ValidateException("OpenID Connect token error response must not contain id_token");
                }
                yield new Error(error.response(), error.status());
            }
            case TokenResponseDecoder.Success success -> {
                if (success.response().getClass() != TokenResponse.class) {
                    throw new ValidateException(
                            "OpenID Connect token response requires an ordinary OAuth token response");
                }
                if (!(idTokenValue instanceof JsonValue.StringValue text) || text.value().isBlank()) {
                    throw new ValidateException("OpenID Connect token response requires a non-empty id_token string");
                }
                yield new Success(
                        new OpenIdTokenResponse((TokenResponse) success.response(), new IdToken(text.value())));
            }
            default -> throw new IllegalStateException("Unsupported protocol model implementation");
        };
    }

    /**
     * Encodes an OpenID Connect token success by extending the shared OAuth JSON response.
     *
     * @param request  originating token endpoint request
     * @param response OpenID Connect token response
     * @return complete HTTP 200 JSON response
     */
    public Response encode(final Request request, final OpenIdTokenResponse response) {
        Assert.notNull(request, "OpenID Connect token HTTP request must not be null");
        Assert.notNull(response, "OpenID Connect token response must not be null");
        try (Response oauth = oauthEncoder.encode(request, response.tokenResponse())) {
            final JsonValue value = jsonProvider.readValue(oauth.bytes(Builder.MAXIMUM_DOCUMENT_BYTES));
            if (!(value instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("OAuth token response encoder returned a non-object JSON body");
            }
            final Map<String, JsonValue> members = new LinkedHashMap<>(object.values());
            if (members.putIfAbsent(
                    OpenIdConnect.Parameters.ID_TOKEN,
                    new JsonValue.StringValue(response.idToken().compact())) != null) {
                throw new ValidateException("OAuth token response unexpectedly contains id_token");
            }
            final byte[] body = jsonProvider.writeValue(new JsonValue.ObjectValue(members));
            return Response.builder().request(request).code(Http.Status.OK).headers(oauth.headers())
                    .body(Body.of(body, MediaType.APPLICATION_JSON_TYPE)).build();
        }
    }

    /**
     * Discriminates OpenID Connect token success from standard OAuth token error.
     *
     * @author Kimi Liu
     */
    public interface Decoded {

    }

    /**
     * Carries a decoded OpenID Connect token success.
     *
     * @param response OpenID Connect token response
     * @author Kimi Liu
     */
    public record Success(OpenIdTokenResponse response) implements Decoded {

        /**
         * Validates the successful response branch.
         */
        public Success {
            Assert.notNull(response, "Decoded OpenID Connect token response must not be null");
        }

    }

    /**
     * Carries a decoded standard OAuth token error.
     *
     * @param response standard token error response
     * @param status   original HTTP error status
     * @author Kimi Liu
     */
    public record Error(TokenErrorResponse response, int status) implements Decoded {

        /**
         * Validates the erroneous response branch.
         */
        public Error {
            Assert.notNull(response, "Decoded OpenID Connect token error must not be null");
            if (status < Http.Status.BAD_REQUEST || status >= Http.Status.INTERNAL_SERVER_ERROR + 100) {
                throw new ValidateException("OpenID Connect token error status must be 4xx or 5xx");
            }
        }

    }

}
