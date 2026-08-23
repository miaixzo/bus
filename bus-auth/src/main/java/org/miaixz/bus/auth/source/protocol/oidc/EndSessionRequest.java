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
package org.miaixz.bus.auth.source.protocol.oidc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents an OpenID Connect RP-Initiated Logout request.
 * <p>
 * Exact registration and session binding remain service responsibilities. The protocol model retains only parameters
 * registered by RP-Initiated Logout 1.0 and deliberately has no response model.
 * </p>
 *
 * @param idTokenHint           optional previously issued ID Token hint
 * @param logoutHint            optional OpenID Provider logout hint
 * @param clientId              optional relying-party client identifier
 * @param postLogoutRedirectUri optional registered post-logout redirection URI
 * @param state                 optional value returned unchanged during post-logout redirection
 * @param uiLocales             ordered preferred BCP 47 user-interface locales
 * @author Kimi Liu
 */
public record EndSessionRequest(Optional<String> idTokenHint, Optional<String> logoutHint, Optional<String> clientId,
        Optional<String> postLogoutRedirectUri, Optional<String> state, List<String> uiLocales) {

    /**
     * Creates and validates an immutable RP-Initiated Logout request.
     *
     * @throws IllegalArgumentException if an optional container or locale list is {@code null}
     * @throws ValidateException        if a present parameter, URI, or locale violates its wire syntax
     */
    public EndSessionRequest {
        idTokenHint = optionalText(idTokenHint, "OpenID Connect ID Token hint");
        logoutHint = optionalText(logoutHint, "OpenID Connect logout hint");
        clientId = optionalText(clientId, "OpenID Connect logout client identifier");
        postLogoutRedirectUri = optionalText(postLogoutRedirectUri, "OpenID Connect post-logout redirect URI");
        state = optionalText(state, "OpenID Connect logout state");
        uiLocales = locales(uiLocales);
        final String redirect = postLogoutRedirectUri.getOrNull();
        if (redirect != null) {
            validateRedirectUri(redirect);
            if (idTokenHint.isEmpty() && clientId.isEmpty()) {
                throw new ValidateException(
                        "OpenID Connect post-logout redirect requires an ID Token hint or client identifier");
            }
        }
    }

    /**
     * Normalizes an optional non-empty protocol string and rejects control characters.
     *
     * @param value optional source container
     * @param label safe diagnostic label
     * @return normalized Bus optional
     * @throws ValidateException if a present value contains a control character
     */
    private static Optional<String> optionalText(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final String present = value.getOrNull();
        if (present != null) {
            Assert.notEmpty(present, label + " must not be empty");
            for (int index = 0; index < present.length(); index++) {
                if (Character.isISOControl(present.charAt(index))) {
                    throw new ValidateException(label + " must not contain control characters");
                }
            }
        }
        return Optional.ofNullable(present);
    }

    /**
     * Validates the structural requirements of a post-logout redirection URI.
     *
     * @param value URI wire value
     * @throws ValidateException if the value is not absolute or contains a fragment
     */
    private static void validateRedirectUri(final String value) {
        try {
            final URI uri = new URI(value);
            if (!uri.isAbsolute() || uri.getFragment() != null) {
                throw new ValidateException(
                        "OpenID Connect post-logout redirect URI must be absolute and must not contain a fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("OpenID Connect post-logout redirect URI is invalid", cause);
        }
    }

    /**
     * Validates and freezes an ordered BCP 47 locale preference list.
     *
     * @param values locale tags in caller order
     * @return immutable ordered locale tags
     * @throws ValidateException if a locale is blank, malformed, or duplicated
     */
    private static List<String> locales(final List<String> values) {
        Assert.notNull(values, "OpenID Connect logout UI locale list must not be null");
        final Set<String> unique = new HashSet<>(values.size());
        for (String value : values) {
            Assert.notBlank(value, "OpenID Connect logout UI locale must not be blank");
            try {
                new Locale.Builder().setLanguageTag(value).build();
            } catch (IllformedLocaleException cause) {
                throw new ValidateException("OpenID Connect logout UI locale must be a valid BCP 47 tag", cause);
            }
            if (!unique.add(value)) {
                throw new ValidateException("OpenID Connect logout UI locale list must not contain duplicates");
            }
        }
        return List.copyOf(values);
    }

    /**
     * Returns a diagnostic summary without hints or state values.
     *
     * @return redacted end-session request summary
     */
    @Override
    public String toString() {
        return "EndSessionRequest[idTokenHint=[REDACTED], logoutHint=[REDACTED], clientId=" + clientId
                + ", postLogoutRedirectUri=" + postLogoutRedirectUri + ", state=[REDACTED], uiLocales=" + uiLocales
                + Symbol.BRACKET_RIGHT;
    }

}
