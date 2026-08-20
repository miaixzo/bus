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

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.codec.Parameter;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Encodes every enabled OAuth grant as ordered standard token endpoint form parameters.
 * <p>
 * This codec deliberately stops before form serialization and client authentication so the Source transport can add its
 * registered authentication method without exposing client credentials to the token request model.
 * </p>
 *
 * @author Kimi Liu
 */
public final class TokenRequestEncoder implements Encoder<TokenRequest, List<Parameter>> {

    /**
     * Creates a stateless standard token request encoder.
     */
    public TokenRequestEncoder() {
        // No initialization required.
    }

    /**
     * Adds standard authorization-code grant fields.
     *
     * @param parameters destination parameter list
     * @param grant      authorization-code grant
     */
    private static void authorizationCode(final List<Parameter> parameters, final AuthorizationCodeGrant grant) {
        parameters.add(new Parameter(OAuth2.Parameters.CODE, grant.code()));
        grant.redirectUri().ifPresent(value -> parameters.add(new Parameter(OAuth2.Parameters.REDIRECT_URI, value)));
        grant.clientId().ifPresent(value -> parameters.add(new Parameter(OAuth2.Parameters.CLIENT_ID, value)));
        grant.codeVerifier().ifPresent(value -> parameters.add(new Parameter(OAuth2.Parameters.CODE_VERIFIER, value)));
    }

    /**
     * Adds standard refresh-token grant fields.
     *
     * @param parameters destination parameter list
     * @param grant      refresh-token grant
     */
    private static void refreshToken(final List<Parameter> parameters, final RefreshTokenGrant grant) {
        parameters.add(new Parameter(OAuth2.Parameters.REFRESH_TOKEN, grant.refreshToken()));
        grant.scope().ifPresent(value -> parameters.add(new Parameter(OAuth2.Parameters.SCOPE, value.format())));
    }

    /**
     * Adds standard client-credentials grant fields.
     *
     * @param parameters destination parameter list
     * @param grant      client-credentials grant
     */
    private static void clientCredentials(final List<Parameter> parameters, final ClientCredentialsGrant grant) {
        grant.scope().ifPresent(value -> parameters.add(new Parameter(OAuth2.Parameters.SCOPE, value.format())));
    }

    /**
     * Adds RFC 8693 token-exchange grant fields in registered wire order.
     *
     * @param parameters destination parameter list
     * @param grant      token-exchange grant
     */
    private static void exchange(final List<Parameter> parameters, final TokenExchangeGrant grant) {
        grant.resource().forEach(value -> parameters.add(new Parameter(OAuth2.Parameters.RESOURCE, value)));
        grant.audience().forEach(value -> parameters.add(new Parameter(OAuth2.Parameters.AUDIENCE, value)));
        parameters.add(new Parameter(OAuth2.Parameters.SUBJECT_TOKEN, grant.subjectToken()));
        parameters.add(new Parameter(OAuth2.Parameters.SUBJECT_TOKEN_TYPE, grant.subjectTokenType()));
        grant.requestedTokenType()
                .ifPresent(value -> parameters.add(new Parameter(OAuth2.Parameters.REQUESTED_TOKEN_TYPE, value)));
        grant.actorToken().ifPresent(value -> parameters.add(new Parameter(OAuth2.Parameters.ACTOR_TOKEN, value)));
        grant.actorTokenType()
                .ifPresent(value -> parameters.add(new Parameter(OAuth2.Parameters.ACTOR_TOKEN_TYPE, value)));
        grant.scope().ifPresent(value -> parameters.add(new Parameter(OAuth2.Parameters.SCOPE, value.format())));
    }

    /**
     * Adds RFC 8628 device-code grant fields.
     *
     * @param parameters destination parameter list
     * @param grant      device-code grant
     */
    private static void deviceCode(final List<Parameter> parameters, final DeviceCodeGrant grant) {
        parameters.add(new Parameter(OAuth2.Parameters.DEVICE_CODE, grant.deviceCode()));
        grant.clientId().ifPresent(value -> parameters.add(new Parameter(OAuth2.Parameters.CLIENT_ID, value)));
    }

    /**
     * Adds one non-registered scalar extension parameter.
     *
     * @param parameters destination parameter list
     * @param name       extension parameter name
     * @param value      provider-neutral extension value
     * @throws ValidateException if the name is registered or the value is not a scalar
     */
    private static void extension(final List<Parameter> parameters, final String name, final JsonValue value) {
        if (registered(name)) {
            throw new ValidateException("OAuth 2.x token request extension duplicates a registered parameter");
        }
        if (value instanceof JsonValue.StringValue text) {
            parameters.add(new Parameter(name, text.value()));
        } else if (value instanceof JsonValue.NumberValue number) {
            parameters.add(new Parameter(name, number.value().toString()));
        } else if (value instanceof JsonValue.BooleanValue flag) {
            parameters.add(new Parameter(name, Boolean.toString(flag.value())));
        } else {
            throw new ValidateException("OAuth 2.x token request extensions must be JSON scalars");
        }
    }

    /**
     * Identifies parameters represented by the token request and supported grant objects.
     *
     * @param name exact form parameter name
     * @return {@code true} for a typed token request component
     */
    private static boolean registered(final String name) {
        return switch (name) {
            case OAuth2.Parameters.GRANT_TYPE, OAuth2.Parameters.CODE, OAuth2.Parameters.REDIRECT_URI, OAuth2.Parameters.CLIENT_ID, OAuth2.Parameters.CODE_VERIFIER, OAuth2.Parameters.REFRESH_TOKEN, OAuth2.Parameters.SCOPE, OAuth2.Parameters.RESOURCE, OAuth2.Parameters.AUDIENCE, OAuth2.Parameters.SUBJECT_TOKEN, OAuth2.Parameters.SUBJECT_TOKEN_TYPE, OAuth2.Parameters.REQUESTED_TOKEN_TYPE, OAuth2.Parameters.ACTOR_TOKEN, OAuth2.Parameters.ACTOR_TOKEN_TYPE, OAuth2.Parameters.DEVICE_CODE -> true;
            default -> false;
        };
    }

    /**
     * Encodes one concrete grant followed by its scalar extension parameters.
     *
     * @param data validated standard token request
     * @return immutable ordered form parameter list
     * @throws IllegalArgumentException if data are {@code null}
     * @throws ValidateException        if an extension is registered or is not a JSON scalar
     */
    @Override
    public List<Parameter> encode(final TokenRequest data) {
        Assert.notNull(data, "OAuth 2.x token request must not be null");
        final List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter(OAuth2.Parameters.GRANT_TYPE, data.grant().type().value()));
        switch (data.grant()) {
            case AuthorizationCodeGrant grant -> authorizationCode(parameters, grant);
            case RefreshTokenGrant grant -> refreshToken(parameters, grant);
            case ClientCredentialsGrant grant -> clientCredentials(parameters, grant);
            case TokenExchangeGrant grant -> exchange(parameters, grant);
            case DeviceCodeGrant grant -> deviceCode(parameters, grant);
        }
        data.extensions().values().forEach((name, value) -> extension(parameters, name, value));
        return List.copyOf(parameters);
    }

}
