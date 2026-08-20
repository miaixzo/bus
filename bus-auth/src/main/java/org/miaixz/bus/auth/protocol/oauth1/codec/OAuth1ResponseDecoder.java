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
package org.miaixz.bus.auth.protocol.oauth1.codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.protocol.oauth1.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Strictly decodes RFC 5849 credentials responses and resource-owner callback parameters.
 *
 * @author Kimi Liu
 */
public final class OAuth1ResponseDecoder {

    /**
     * OAuth credential shared-secret response parameter.
     */
    private static final String TOKEN_SECRET = "oauth_token_secret";

    /**
     * Temporary credentials callback confirmation parameter.
     */
    private static final String CALLBACK_CONFIRMED = "oauth_callback_confirmed";

    /**
     * Strict form representation codec.
     */
    private final OAuth1FormCodec formCodec;

    /**
     * Maximum response bytes accepted from the protocol security baseline.
     */
    private final long maximumMessageBytes;

    /**
     * Creates a bounded response decoder.
     *
     * @param formCodec           strict OAuth 1.0 form codec
     * @param maximumMessageBytes positive maximum response size
     */
    public OAuth1ResponseDecoder(final OAuth1FormCodec formCodec, final long maximumMessageBytes) {
        this.formCodec = Assert.notNull(formCodec, "OAuth 1.0 form codec must not be null");
        Assert.isTrue(maximumMessageBytes > 0, "OAuth 1.0 maximum response bytes must be positive");
        this.maximumMessageBytes = maximumMessageBytes;
    }

    /**
     * Returns the only value for a required standard response parameter.
     *
     * @param parameters decoded response parameters
     * @param name       required parameter name
     * @return required parameter value
     */
    private static String exactlyOne(final List<OAuth1Parameter> parameters, final String name) {
        String result = null;
        int count = 0;
        for (OAuth1Parameter parameter : parameters) {
            if (name.equals(parameter.name())) {
                result = parameter.value();
                count++;
            }
        }
        if (count != 1 || result == null || result.isBlank()) {
            throw new ProtocolException("OAuth 1.0 response requires exactly one non-blank " + name);
        }
        return result;
    }

    /**
     * Preserves response parameters not represented by dedicated standard fields.
     *
     * @param parameters complete decoded parameters
     * @param reserved   standard dedicated field names
     * @return immutable ordered extension parameters
     */
    private static List<OAuth1Parameter> extensions(
            final List<OAuth1Parameter> parameters,
            final Set<String> reserved) {
        final List<OAuth1Parameter> result = new ArrayList<>();
        for (OAuth1Parameter parameter : parameters) {
            if (!reserved.contains(parameter.name())) {
                result.add(parameter);
            }
        }
        return List.copyOf(result);
    }

    /**
     * Decodes a successful temporary credentials response according to RFC 5849 section 2.1.
     *
     * @param response Fabric HTTP response owned by this call
     * @return standard temporary credentials response
     */
    public TemporaryCredentialsResponse temporaryCredentials(final HttpResponse response) {
        final List<OAuth1Parameter> parameters = response(response);
        final String token = exactlyOne(parameters, OAuth1.Parameters.TOKEN);
        final String secret = exactlyOne(parameters, TOKEN_SECRET);
        final String confirmed = exactlyOne(parameters, CALLBACK_CONFIRMED);
        if (!Normal.TRUE.equals(confirmed)) {
            throw new ProtocolException("Temporary credentials response did not confirm the callback");
        }
        return new TemporaryCredentialsResponse(token, secret, true,
                extensions(parameters, Set.of(OAuth1.Parameters.TOKEN, TOKEN_SECRET, CALLBACK_CONFIRMED)));
    }

    /**
     * Decodes a resource owner authorization callback according to RFC 5849 section 2.2.
     *
     * @param callback raw callback transport captured by the external Web project
     * @return standard resource owner authorization response
     */
    public ResourceOwnerAuthorizationResponse authorization(final Callback.Inbound callback) {
        Assert.notNull(callback, "OAuth 1.0 callback must not be null");
        if (callback.method() != Http.Method.GET) {
            throw new ValidateException("OAuth 1.0 resource owner callback must use GET");
        }
        final List<OAuth1Parameter> parameters = new ArrayList<>(callback.parameters().size());
        for (Callback.Parameter parameter : callback.parameters()) {
            parameters.add(new OAuth1Parameter(parameter.name(), parameter.value()));
        }
        return new ResourceOwnerAuthorizationResponse(exactlyOne(parameters, OAuth1.Parameters.TOKEN),
                exactlyOne(parameters, OAuth1.Parameters.VERIFIER),
                extensions(parameters, Set.of(OAuth1.Parameters.TOKEN, OAuth1.Parameters.VERIFIER)));
    }

    /**
     * Decodes a successful token credentials response according to RFC 5849 section 2.3.
     *
     * @param response Fabric HTTP response owned by this call
     * @return standard token credentials response
     */
    public TokenCredentialsResponse tokenCredentials(final HttpResponse response) {
        final List<OAuth1Parameter> parameters = response(response);
        return new TokenCredentialsResponse(exactlyOne(parameters, OAuth1.Parameters.TOKEN),
                exactlyOne(parameters, TOKEN_SECRET),
                extensions(parameters, Set.of(OAuth1.Parameters.TOKEN, TOKEN_SECRET)));
    }

    /**
     * Validates status and media type, materializes a bounded body, and closes the response.
     *
     * @param response Fabric response owned by this method
     * @return decoded ordered form parameters
     */
    private List<OAuth1Parameter> response(final HttpResponse response) {
        Assert.notNull(response, "OAuth 1.0 HTTP response must not be null");
        try {
            if (!response.successful()) {
                throw new ProtocolException("OAuth 1.0 endpoint returned HTTP status " + response.code());
            }
            final MediaType media = response.body().media();
            if (media == null || !media.isCompatible(MediaType.APPLICATION_FORM_URLENCODED_TYPE)) {
                throw new ProtocolException(
                        "OAuth 1.0 credentials response must use application/x-www-form-urlencoded");
            }
            return formCodec.decode(response.bytes(maximumMessageBytes));
        } finally {
            response.close();
        }
    }

}
