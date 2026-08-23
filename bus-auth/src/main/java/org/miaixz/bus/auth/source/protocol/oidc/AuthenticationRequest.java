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

import org.miaixz.bus.auth.source.protocol.oauth2.AuthorizationRequest;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.source.protocol.oauth2.ResponseType;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents an OpenID Connect Authorization Code Flow authentication request over the OAuth authorization model.
 *
 * @param authorizationRequest standard OAuth authorization request containing response type, client, redirect, scope,
 *                             state and OAuth extensions
 * @param nonce                optional replay-binding value returned in the ID Token when supplied
 * @param display              optional requested authorization-server display mode
 * @param prompt               optional ordered prompt value
 * @param maxAge               optional maximum authentication age in seconds
 * @param uiLocales            ordered preferred BCP 47 user-interface locales
 * @param idTokenHint          optional prior ID Token hint
 * @param loginHint            optional login identifier hint
 * @param acrValues            ordered requested authentication context class references
 * @param claims               optional OpenID Connect claims request object
 * @param responseMode         optional response mode, restricted to the code-flow {@code query} and {@code form_post}
 *                             registrations accepted by this authorization-code request model
 * @param extensions           unknown OpenID Connect request parameters
 * @author Kimi Liu
 */
public record AuthenticationRequest(AuthorizationRequest authorizationRequest, Optional<String> nonce,
        Optional<Display> display, Optional<Prompt> prompt, Optional<Long> maxAge, List<String> uiLocales,
        Optional<String> idTokenHint, Optional<String> loginHint, List<String> acrValues,
        Optional<JsonValue.ObjectValue> claims, Optional<String> responseMode, JsonValue.ObjectValue extensions) {

    /**
     * Creates and validates an immutable OpenID Connect code-flow authentication request.
     *
     * @throws IllegalArgumentException if a required component, container, list, or list entry is {@code null}
     * @throws ValidateException        if flow, scope, nonce, locale, ACR, response mode, or extension syntax is
     *                                  invalid
     */
    public AuthenticationRequest {
        Assert.notNull(authorizationRequest, "OpenID Connect OAuth authorization request must not be null");
        if (!ResponseType.CODE.equals(authorizationRequest.responseType())) {
            throw new ValidateException("OpenID Connect authorization-code requests require response_type=code");
        }
        if (authorizationRequest.scope().isEmpty()
                || !authorizationRequest.scope().getOrNull().values().contains("openid")) {
            throw new ValidateException("OpenID Connect authentication scope must contain openid");
        }
        Assert.notNull(nonce, "OpenID Connect nonce container must not be null");
        final String nonceValue = nonce.getOrNull();
        if (nonceValue != null) {
            Assert.notEmpty(nonceValue, "OpenID Connect nonce must not be empty when present");
        }
        nonce = Optional.ofNullable(nonceValue);
        Assert.notNull(display, "OpenID Connect display container must not be null");
        Assert.notNull(prompt, "OpenID Connect prompt container must not be null");
        Assert.notNull(maxAge, "OpenID Connect maximum age container must not be null");
        final Long age = maxAge.getOrNull();
        if (age != null && age < 0L) {
            throw new ValidateException("OpenID Connect maximum authentication age must not be negative");
        }
        uiLocales = locales(uiLocales);
        idTokenHint = optionalText(idTokenHint, "OpenID Connect ID Token hint");
        loginHint = optionalText(loginHint, "OpenID Connect login hint");
        acrValues = stringOrUris(acrValues);
        Assert.notNull(claims, "OpenID Connect claims container must not be null");
        final JsonValue.ObjectValue claimsValue = claims.getOrNull();
        claims = claimsValue == null ? Optional.empty() : Optional.of(new JsonValue.ObjectValue(claimsValue.values()));
        Assert.notNull(responseMode, "OpenID Connect response mode container must not be null");
        final String mode = responseMode.getOrNull();
        if (mode != null && !OpenIdConnect.ResponseModes.QUERY.equals(mode)
                && !OpenIdConnect.ResponseModes.FORM_POST.equals(mode)) {
            throw new ValidateException(
                    "OpenID Connect authorization-code requests support only query or form_post response mode");
        }
        responseMode = Optional.ofNullable(mode);
        Assert.notNull(extensions, "OpenID Connect authentication extensions must not be null");
        reject(authorizationRequest.extensions(), true, false);
        reject(extensions, true, true);
        extensions = new JsonValue.ObjectValue(extensions.values());
        display = Optional.ofNullable(display.getOrNull());
        prompt = Optional.ofNullable(prompt.getOrNull());
        maxAge = Optional.ofNullable(age);
    }

    /**
     * Normalizes optional non-empty text.
     *
     * @param value optional text container
     * @param label safe field label
     * @return normalized optional text
     */
    private static Optional<String> optionalText(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final String text = value.getOrNull();
        if (text != null) {
            Assert.notEmpty(text, label + " must not be empty when present");
        }
        return Optional.ofNullable(text);
    }

    /**
     * Copies and validates unique BCP 47 locale tags.
     *
     * @param values locale tags
     * @return immutable ordered locale tags
     */
    private static List<String> locales(final List<String> values) {
        final List<String> copy = unique(values, "OpenID Connect UI locale");
        for (String value : copy) {
            try {
                new Locale.Builder().setLanguageTag(value).build();
            } catch (IllformedLocaleException exception) {
                throw new ValidateException("OpenID Connect UI locale must be a valid BCP 47 tag", exception);
            }
        }
        return copy;
    }

    /**
     * Copies and validates requested ACR StringOrURI values.
     *
     * @param values requested ACR values
     * @return immutable ordered ACR values
     */
    private static List<String> stringOrUris(final List<String> values) {
        final List<String> copy = unique(values, "OpenID Connect ACR value");
        for (String value : copy) {
            if (value.indexOf(Symbol.C_COLON) >= 0) {
                try {
                    new URI(value);
                } catch (URISyntaxException exception) {
                    throw new ValidateException("OpenID Connect colon-containing ACR value must be a valid URI",
                            exception);
                }
            }
        }
        return copy;
    }

    /**
     * Copies unique non-empty strings while preserving order.
     *
     * @param values source values
     * @param label  safe element label
     * @return immutable ordered values
     */
    private static List<String> unique(final List<String> values, final String label) {
        Assert.notNull(values, label + " list must not be null");
        final List<String> copy = new ArrayList<>(values.size());
        final Set<String> unique = new HashSet<>(values.size());
        for (String value : values) {
            final String item = Assert.notEmpty(value, label + " must not be empty");
            if (!unique.add(item)) {
                throw new ValidateException(label + " list must not contain duplicates");
            }
            copy.add(item);
        }
        return List.copyOf(copy);
    }

    /**
     * Rejects registered members from an extension object.
     *
     * @param extensions extension object
     * @param oidc       whether OpenID Connect members are prohibited
     * @param oauth      whether OAuth authorization members are prohibited
     * @throws ValidateException if a reserved member is present
     */
    private static void reject(final JsonValue.ObjectValue extensions, final boolean oidc, final boolean oauth) {
        for (String name : extensions.values().keySet()) {
            if (oidc && oidcMember(name) || oauth && oauthMember(name)) {
                throw new ValidateException("OpenID Connect extensions contain reserved member: " + name);
            }
        }
    }

    /**
     * Identifies OpenID Connect request members represented by explicit components.
     *
     * @param name exact request parameter name
     * @return {@code true} for an OpenID Connect component
     */
    private static boolean oidcMember(final String name) {
        return switch (name) {
            case OpenIdConnect.Parameters.NONCE, OpenIdConnect.Parameters.DISPLAY, OpenIdConnect.Parameters.PROMPT, OpenIdConnect.Parameters.MAX_AGE, OpenIdConnect.Parameters.UI_LOCALES, OpenIdConnect.Parameters.ID_TOKEN_HINT, OpenIdConnect.Parameters.LOGIN_HINT, OpenIdConnect.Parameters.ACR_VALUES, OpenIdConnect.Parameters.CLAIMS, OpenIdConnect.Parameters.RESPONSE_MODE, OpenIdConnect.Parameters.REQUEST, OpenIdConnect.Parameters.REQUEST_URI -> true;
            default -> false;
        };
    }

    /**
     * Identifies OAuth authorization members represented by the composed request.
     *
     * @param name exact request parameter name
     * @return {@code true} for an OAuth authorization component
     */
    private static boolean oauthMember(final String name) {
        return switch (name) {
            case OAuth2.Parameters.RESPONSE_TYPE, OAuth2.Parameters.CLIENT_ID, OAuth2.Parameters.REDIRECT_URI, OAuth2.Parameters.SCOPE, OAuth2.Parameters.STATE, OAuth2.Parameters.CODE_CHALLENGE, OAuth2.Parameters.CODE_CHALLENGE_METHOD -> true;
            default -> false;
        };
    }

    /**
     * Returns a diagnostic summary without nonce, hints, claims, or extension values.
     *
     * @return redacted authentication request summary
     */
    @Override
    public String toString() {
        return "AuthenticationRequest[authorizationRequest=" + authorizationRequest + ", nonce=[REDACTED], display="
                + display + ", prompt=" + prompt + ", maxAge=" + maxAge + ", uiLocales=" + uiLocales
                + ", idTokenHint=[REDACTED], loginHint=[REDACTED], acrValues=" + acrValues
                + ", claims=[REDACTED], responseMode=" + responseMode + ", extensions=[REDACTED]]";
    }

}
