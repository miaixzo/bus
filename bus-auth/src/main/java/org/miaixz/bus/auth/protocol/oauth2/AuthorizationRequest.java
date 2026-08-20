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
package org.miaixz.bus.auth.protocol.oauth2;

import java.net.URI;
import java.net.URISyntaxException;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents an OAuth 2.x authorization request for the authorization-code response flow.
 * <p>
 * Registered parameters use typed components while unrecognized extension parameters remain in a provider-neutral JSON
 * object for codec-controlled wire conversion. This model does not select an endpoint or execute a redirect.
 * </p>
 *
 * @param responseType        requested registered response type
 * @param clientId            authorization server-issued client identifier
 * @param redirectUri         optional absolute redirection endpoint URI
 * @param scope               optional requested scope
 * @param state               optional client correlation and CSRF-mitigation value
 * @param codeChallenge       optional RFC 7636 proof-key challenge
 * @param codeChallengeMethod optional RFC 7636 proof-key transformation method
 * @param extensions          unknown extension parameters that do not duplicate registered components
 * @author Kimi Liu
 */
public record AuthorizationRequest(ResponseType responseType, String clientId, Optional<String> redirectUri,
        Optional<Scope> scope, Optional<String> state, Optional<String> codeChallenge,
        Optional<String> codeChallengeMethod, JsonValue.ObjectValue extensions) {

    /**
     * Creates and validates an immutable authorization request.
     *
     * @throws IllegalArgumentException if a required component or optional container is {@code null}
     * @throws ValidateException        if a component violates RFC 6749 or RFC 7636 wire syntax
     */
    public AuthorizationRequest {
        Assert.notNull(responseType, "OAuth 2.x authorization response type must not be null");
        Assert.notNull(clientId, "OAuth 2.x authorization client identifier must not be null");
        requireVisibleAscii(clientId, true, "OAuth 2.x authorization client identifier");
        Assert.notNull(redirectUri, "OAuth 2.x authorization redirect URI container must not be null");
        Assert.notNull(scope, "OAuth 2.x authorization scope container must not be null");
        Assert.notNull(state, "OAuth 2.x authorization state container must not be null");
        Assert.notNull(codeChallenge, "OAuth 2.x authorization code challenge container must not be null");
        Assert.notNull(codeChallengeMethod, "OAuth 2.x authorization code challenge method container must not be null");
        Assert.notNull(extensions, "OAuth 2.x authorization extensions must not be null");

        final String redirect = redirectUri.getOrNull();
        if (redirect != null) {
            validateRedirectUri(redirect);
        }
        final Scope requestedScope = scope.getOrNull();
        final String stateValue = state.getOrNull();
        if (stateValue != null) {
            requireVisibleAscii(stateValue, true, "OAuth 2.x authorization state");
        }
        final String challengeValue = codeChallenge.getOrNull();
        final String challengeMethodValue = codeChallengeMethod.getOrNull();
        if (challengeValue != null) {
            requireVisibleAscii(challengeValue, true, "OAuth 2.x authorization code challenge");
        }
        if (challengeMethodValue != null) {
            requireVisibleAscii(challengeMethodValue, true, "OAuth 2.x authorization code challenge method");
        }
        if (challengeValue == null && challengeMethodValue != null) {
            throw new ValidateException("OAuth 2.x code challenge method requires a code challenge");
        }
        for (String name : extensions.values().keySet()) {
            Assert.notBlank(name, "OAuth authorization request extension name must not be blank");
            if (standard(name)) {
                throw new ValidateException("OAuth authorization extension replaces a standard request component");
            }
        }

        redirectUri = Optional.ofNullable(redirect);
        scope = Optional.ofNullable(requestedScope);
        state = Optional.ofNullable(stateValue);
        codeChallenge = Optional.ofNullable(challengeValue);
        codeChallengeMethod = Optional.ofNullable(challengeMethodValue);
        extensions = new JsonValue.ObjectValue(extensions.values());
    }

    /**
     * Identifies the RFC 6749 and RFC 7636 authorization request parameters modeled as components.
     *
     * @param name exact parameter name
     * @return {@code true} for a dedicated request component
     */
    private static boolean standard(final String name) {
        return switch (name) {
            case OAuth2.Parameters.RESPONSE_TYPE, OAuth2.Parameters.CLIENT_ID, OAuth2.Parameters.REDIRECT_URI, OAuth2.Parameters.SCOPE, OAuth2.Parameters.STATE, OAuth2.Parameters.CODE_CHALLENGE, OAuth2.Parameters.CODE_CHALLENGE_METHOD -> true;
            default -> false;
        };
    }

    /**
     * Validates a no-fragment absolute redirect URI as required by RFC 6749.
     *
     * @param value redirect URI wire value
     * @throws ValidateException if the value is not an absolute URI or contains a fragment
     */
    private static void validateRedirectUri(final String value) {
        try {
            final URI uri = new URI(value);
            if (!uri.isAbsolute() || uri.getRawFragment() != null) {
                throw new ValidateException("OAuth 2.x redirect URI must be absolute and must not contain a fragment");
            }
        } catch (URISyntaxException exception) {
            throw new ValidateException("OAuth 2.x redirect URI must be a valid absolute URI", exception);
        }
    }

    /**
     * Validates an RFC 6749 visible-ASCII component.
     *
     * @param value    value to validate
     * @param nonEmpty whether an empty string is prohibited
     * @param label    safe component label used in diagnostics
     * @throws ValidateException if the value is empty when prohibited or contains a non-visible ASCII character
     */
    private static void requireVisibleAscii(final String value, final boolean nonEmpty, final String label) {
        if (nonEmpty && value.isEmpty()) {
            throw new ValidateException(label + " must not be empty");
        }
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character < 0x20 || character > 0x7e) {
                throw new ValidateException(label + " contains a character outside RFC 6749 VSCHAR");
            }
        }
    }

}
