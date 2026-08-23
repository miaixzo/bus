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
package org.miaixz.bus.auth.source.protocol.oauth2;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents one OAuth 2.x token request whose grant variant determines the standard {@code grant_type} value.
 * <p>
 * Client authentication belongs to endpoint transport processing and is deliberately absent. RFC 8693 token exchange
 * and RFC 8628 device polling use the same token request and endpoint as every other supported grant.
 * </p>
 *
 * @param grant      exactly one supported standard grant parameter set
 * @param extensions unknown token request parameters that do not duplicate registered components
 * @author Kimi Liu
 */
public record TokenRequest(Grant grant, JsonValue.ObjectValue extensions) {

    /**
     * Creates an immutable token request and rejects registered fields from its extension container.
     *
     * @throws IllegalArgumentException if the grant or extensions object is {@code null}
     * @throws ValidateException        if extensions duplicate a registered token request parameter
     */
    public TokenRequest {
        Assert.notNull(grant, "OAuth 2.x token request grant must not be null");
        Assert.notNull(extensions, "OAuth 2.x token request extensions must not be null");
        for (String name : extensions.values().keySet()) {
            Assert.notBlank(name, "OAuth token request extension parameter name must not be blank");
            if (standard(name)) {
                throw new ValidateException("OAuth 2.x token request extensions duplicate parameter: " + name);
            }
        }
        extensions = new JsonValue.ObjectValue(extensions.values());
    }

    /**
     * Identifies standard parameters owned by the token request or one supported grant object.
     *
     * @param name exact parameter name
     * @return {@code true} for a parameter that cannot occur in extensions
     */
    private static boolean standard(final String name) {
        return switch (name) {
            case OAuth2.Parameters.GRANT_TYPE, OAuth2.Parameters.CODE, OAuth2.Parameters.REDIRECT_URI, OAuth2.Parameters.CLIENT_ID, OAuth2.Parameters.CODE_VERIFIER, OAuth2.Parameters.REFRESH_TOKEN, OAuth2.Parameters.SCOPE, OAuth2.Parameters.RESOURCE, OAuth2.Parameters.AUDIENCE, OAuth2.Parameters.SUBJECT_TOKEN, OAuth2.Parameters.SUBJECT_TOKEN_TYPE, OAuth2.Parameters.REQUESTED_TOKEN_TYPE, OAuth2.Parameters.ACTOR_TOKEN, OAuth2.Parameters.ACTOR_TOKEN_TYPE, OAuth2.Parameters.DEVICE_CODE -> true;
            default -> false;
        };
    }

    /**
     * Restricts token requests to the grant parameter sets implemented by the OAuth 2.x token endpoint.
     * <p>
     * The interface is intentionally a marker: the concrete record determines the registered {@code grant_type}, and
     * codecs perform that fixed mapping without accepting an arbitrary caller-supplied value.
     * </p>
     *
     * @author Kimi Liu
     */
    public interface Grant {

        /**
         * Returns the exact standard grant type determined by the concrete parameter object.
         *
         * @return fixed grant type associated with this object
         */
        default GrantType type() {
            return switch (this) {
                case AuthorizationCodeGrant ignored -> GrantType.AUTHORIZATION_CODE;
                case RefreshTokenGrant ignored -> GrantType.REFRESH_TOKEN;
                case ClientCredentialsGrant ignored -> GrantType.CLIENT_CREDENTIALS;
                case TokenExchangeGrant ignored -> GrantType.TOKEN_EXCHANGE;
                case DeviceCodeGrant ignored -> GrantType.DEVICE_CODE;
                default -> throw new IllegalStateException("Unsupported OAuth grant implementation");
            };
        }

    }

}
