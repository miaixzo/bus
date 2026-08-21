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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.RevocationRequest;
import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;

/**
 * Decodes the standard RFC 7009 token revocation form request.
 * <p>
 * Client credentials remain on the repeatable HTTP body for the following authenticator and never enter the typed
 * revocation request.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RevocationRequestDecoder implements Decoder<HttpRequest, RevocationRequest> {

    /**
     * Maximum form request size materialized by the decoder.
     */
    private static final long MAXIMUM_FORM_BYTES = 64 * Normal.KIBI;

    /**
     * Shared strict UTF-8 form codec.
     */
    private final FormCodec formCodec;

    /**
     * Creates a strict stateless revocation request decoder.
     */
    public RevocationRequestDecoder() {
        this.formCodec = new FormCodec();
    }

    /**
     * Validates the RFC 7009 HTTP request representation.
     *
     * @param request request to inspect
     * @throws ValidateException if method, URL, media, size, or repeatability is invalid
     */
    private static void validateTransport(final HttpRequest request) {
        if (request.method() != Http.Method.POST) {
            throw new ValidateException("OAuth 2.x revocation endpoint requires HTTP POST");
        }
        if (!request.url().query().isEmpty() || request.url().fragment() != null) {
            throw new ValidateException("OAuth 2.x revocation parameters must not use URL query or fragment");
        }
        final MediaType media = request.body().media();
        if (!MediaType.APPLICATION_FORM_URLENCODED_TYPE.isCompatible(media)) {
            throw new ValidateException("OAuth 2.x revocation request must be form encoded");
        }
        final String charset = media.parameter(MediaType.CHARSET_PARAMETER);
        if (charset != null && !Charset.UTF_8.equals(media.charset())) {
            throw new ValidateException("OAuth 2.x revocation request charset must be UTF-8");
        }
        if (request.body().length() < 0L || request.body().length() > MAXIMUM_FORM_BYTES) {
            throw new ValidateException("OAuth 2.x revocation request exceeds the maximum form size");
        }
        if (!request.body().repeatable()) {
            throw new ValidateException(
                    "OAuth 2.x revocation request must be buffered for decoding and authentication reuse");
        }
    }

    /**
     * Copies form parameters to a unique insertion-ordered map.
     *
     * @param decoded ordered form parameters
     * @return mutable unique parameter map
     * @throws ValidateException if a parameter name occurs more than once
     */
    private static Map<String, String> unique(final List<NameValue> decoded) {
        final Map<String, String> values = new LinkedHashMap<>(decoded.size());
        for (NameValue parameter : decoded) {
            if (values.putIfAbsent(parameter.name(), parameter.value()) != null) {
                throw new ValidateException("OAuth 2.x revocation parameters must not be repeated");
            }
        }
        return values;
    }

    /**
     * Decodes one buffered RFC 7009 revocation request without retaining transport credentials.
     *
     * @param encoded immutable Fabric HTTP request
     * @return validated standard revocation request
     * @throws IllegalArgumentException if encoded is {@code null}
     * @throws ValidateException        if transport, form, multiplicity, or parameter syntax is invalid
     */
    @Override
    public RevocationRequest decode(final HttpRequest encoded) {
        Assert.notNull(encoded, "OAuth 2.x revocation HTTP request must not be null");
        validateTransport(encoded);
        final Map<String, String> parameters = unique(formCodec.decode(encoded.body().bytes(MAXIMUM_FORM_BYTES)));
        final String token = parameters.remove(OAuth2.Parameters.TOKEN);
        if (token == null || token.isEmpty()) {
            throw new ValidateException("OAuth 2.x revocation request requires non-empty token");
        }
        final String hint = parameters.remove(OAuth2.Parameters.TOKEN_TYPE_HINT);
        parameters.remove(OAuth2.Parameters.CLIENT_ID);
        parameters.remove(OAuth2.Parameters.CLIENT_SECRET);
        if (!parameters.isEmpty()) {
            throw new ValidateException("OAuth 2.x revocation request contains an unsupported parameter");
        }
        return new RevocationRequest(token, Optional.ofNullable(hint));
    }

}
