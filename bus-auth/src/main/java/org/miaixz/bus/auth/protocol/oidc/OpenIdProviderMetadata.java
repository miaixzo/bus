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
package org.miaixz.bus.auth.protocol.oidc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.miaixz.bus.auth.protocol.oauth2.AuthorizationServerMetadata;
import org.miaixz.bus.auth.protocol.oauth2.GrantType;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.ResponseType;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Represents OpenID Provider Metadata for OpenID Connect Discovery 1.0 and RP-Initiated Logout 1.0.
 * <p>
 * The record preserves remote providers' additional registered values while requiring the capabilities needed by the
 * implemented Authorization Code Flow. Provider-side generation remains responsible for publishing only compiled
 * features. Dynamic client registration and unsupported Request Object endpoints cannot be represented as typed
 * members.
 * </p>
 *
 * @param authorizationServerMetadata          composed OAuth authorization server metadata
 * @param userInfoEndpoint                     optional UserInfo endpoint URL
 * @param acrValuesSupported                   supported Authentication Context Class References
 * @param subjectTypesSupported                supported subject identifier types
 * @param idTokenSigningAlgValuesSupported     supported ID Token JWS algorithms
 * @param idTokenEncryptionAlgValuesSupported  supported ID Token JWE key-management algorithms
 * @param idTokenEncryptionEncValuesSupported  supported ID Token JWE content-encryption algorithms
 * @param userInfoSigningAlgValuesSupported    supported signed UserInfo JWS algorithms
 * @param userInfoEncryptionAlgValuesSupported supported UserInfo JWE key-management algorithms
 * @param userInfoEncryptionEncValuesSupported supported UserInfo JWE content-encryption algorithms
 * @param displayValuesSupported               supported Authentication Request display values
 * @param claimTypesSupported                  supported OpenID Connect Claim Types
 * @param claimsSupported                      claim names that the OpenID Provider can supply
 * @param claimsLocalesSupported               supported BCP 47 Claim language tags
 * @param claimsParameterSupported             optional claims-request-parameter support indicator
 * @param requestParameterSupported            optional Request Object by-value support indicator
 * @param requestUriParameterSupported         optional Request Object by-reference support indicator
 * @param requireRequestUriRegistration        optional pre-registration requirement for request URI values
 * @param endSessionEndpoint                   optional RP-Initiated Logout endpoint URL
 * @param extensions                           unknown metadata members that do not duplicate typed or excluded members
 * @author Kimi Liu
 */
public record OpenIdProviderMetadata(AuthorizationServerMetadata authorizationServerMetadata,
        Optional<String> userInfoEndpoint, List<String> acrValuesSupported, List<SubjectType> subjectTypesSupported,
        List<JwaAlgorithm> idTokenSigningAlgValuesSupported, List<JwaAlgorithm> idTokenEncryptionAlgValuesSupported,
        List<JwaAlgorithm> idTokenEncryptionEncValuesSupported, List<JwaAlgorithm> userInfoSigningAlgValuesSupported,
        List<JwaAlgorithm> userInfoEncryptionAlgValuesSupported,
        List<JwaAlgorithm> userInfoEncryptionEncValuesSupported, List<Display> displayValuesSupported,
        List<ClaimType> claimTypesSupported, List<String> claimsSupported, List<String> claimsLocalesSupported,
        Optional<Boolean> claimsParameterSupported, Optional<Boolean> requestParameterSupported,
        Optional<Boolean> requestUriParameterSupported, Optional<Boolean> requireRequestUriRegistration,
        Optional<String> endSessionEndpoint, JsonValue.ObjectValue extensions) {

    /**
     * Creates and validates immutable OpenID Provider Metadata.
     *
     * @throws IllegalArgumentException if a required component, optional container, list, or list item is null
     * @throws ValidateException        if a URL, required capability, locale, duplicate item, or extension member is
     *                                  invalid
     */
    public OpenIdProviderMetadata {
        Assert.notNull(
                authorizationServerMetadata,
                "OpenID Connect composed authorization server metadata must not be null");
        userInfoEndpoint = optionalSecureUrl(userInfoEndpoint, "OpenID Connect UserInfo endpoint");
        requireContains(
                authorizationServerMetadata.scopesSupported(),
                OpenIdConnect.Scopes.OPENID,
                "OpenID Connect supported scopes");
        requireContains(
                authorizationServerMetadata.responseTypesSupported(),
                ResponseType.CODE,
                "OpenID Connect supported response types");
        if (!authorizationServerMetadata.responseModesSupported().contains(OpenIdConnect.ResponseModes.QUERY)
                && !authorizationServerMetadata.responseModesSupported()
                        .contains(OpenIdConnect.ResponseModes.FORM_POST)) {
            throw new ValidateException("OpenID Connect supported response modes must contain query or form_post");
        }
        requireContains(
                authorizationServerMetadata.grantTypesSupported(),
                GrantType.AUTHORIZATION_CODE,
                "OpenID Connect supported grant types");
        acrValuesSupported = stringOrUriList(acrValuesSupported, "OpenID Connect supported ACR");
        subjectTypesSupported = uniqueList(subjectTypesSupported, "OpenID Connect supported subject type", true);
        idTokenSigningAlgValuesSupported = uniqueList(
                idTokenSigningAlgValuesSupported,
                "OpenID Connect ID Token signing algorithm",
                true);
        idTokenEncryptionAlgValuesSupported = uniqueList(
                idTokenEncryptionAlgValuesSupported,
                "OpenID Connect ID Token key-management algorithm",
                false);
        idTokenEncryptionEncValuesSupported = uniqueList(
                idTokenEncryptionEncValuesSupported,
                "OpenID Connect ID Token content-encryption algorithm",
                false);
        userInfoSigningAlgValuesSupported = uniqueList(
                userInfoSigningAlgValuesSupported,
                "OpenID Connect UserInfo signing algorithm",
                false);
        userInfoEncryptionAlgValuesSupported = uniqueList(
                userInfoEncryptionAlgValuesSupported,
                "OpenID Connect UserInfo key-management algorithm",
                false);
        userInfoEncryptionEncValuesSupported = uniqueList(
                userInfoEncryptionEncValuesSupported,
                "OpenID Connect UserInfo content-encryption algorithm",
                false);
        displayValuesSupported = uniqueList(displayValuesSupported, "OpenID Connect supported display value", false);
        claimTypesSupported = uniqueList(claimTypesSupported, "OpenID Connect supported claim type", false);
        claimsSupported = textList(claimsSupported, "OpenID Connect supported claim", false);

        claimsLocalesSupported = localeList(claimsLocalesSupported, "OpenID Connect supported claims locale");
        claimsParameterSupported = optionalBoolean(claimsParameterSupported, "OpenID Connect claims-parameter support");
        requestParameterSupported = optionalBoolean(
                requestParameterSupported,
                "OpenID Connect request-parameter support");
        requestUriParameterSupported = optionalBoolean(
                requestUriParameterSupported,
                "OpenID Connect request-URI-parameter support");
        requireRequestUriRegistration = optionalBoolean(
                requireRequestUriRegistration,
                "OpenID Connect request-URI registration requirement");
        endSessionEndpoint = optionalSecureUrl(endSessionEndpoint, "OpenID Connect end-session endpoint");

        Assert.notNull(extensions, "OpenID Connect metadata extensions must not be null");
        for (String name : extensions.values().keySet()) {
            if (registered(name)) {
                throw new ValidateException("OpenID Connect metadata extensions contain reserved member: " + name);
            }
        }
        extensions = new JsonValue.ObjectValue(extensions.values());
    }

    /**
     * Identifies OAuth and OpenID Provider metadata members represented by the composed model.
     *
     * @param name exact metadata member name
     * @return {@code true} for a typed OAuth or OpenID Connect metadata component
     */
    private static boolean registered(final String name) {
        return switch (name) {
            case OAuth2.Metadata.ISSUER, OAuth2.Metadata.AUTHORIZATION_ENDPOINT, OAuth2.Metadata.TOKEN_ENDPOINT, OAuth2.Metadata.JWKS_URI, OAuth2.Metadata.REGISTRATION_ENDPOINT, OAuth2.Metadata.SCOPES_SUPPORTED, OAuth2.Metadata.RESPONSE_TYPES_SUPPORTED, OAuth2.Metadata.RESPONSE_MODES_SUPPORTED, OAuth2.Metadata.GRANT_TYPES_SUPPORTED, OAuth2.Metadata.TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED, OAuth2.Metadata.TOKEN_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED, OAuth2.Metadata.SERVICE_DOCUMENTATION, OAuth2.Metadata.UI_LOCALES_SUPPORTED, OAuth2.Metadata.OP_POLICY_URI, OAuth2.Metadata.OP_TOS_URI, OpenIdConnect.Metadata.USERINFO_ENDPOINT, OpenIdConnect.Metadata.ACR_VALUES_SUPPORTED, OpenIdConnect.Metadata.SUBJECT_TYPES_SUPPORTED, OpenIdConnect.Metadata.ID_TOKEN_SIGNING_ALGORITHMS_SUPPORTED, OpenIdConnect.Metadata.ID_TOKEN_ENCRYPTION_ALGORITHMS_SUPPORTED, OpenIdConnect.Metadata.ID_TOKEN_ENCRYPTION_METHODS_SUPPORTED, OpenIdConnect.Metadata.USERINFO_SIGNING_ALGORITHMS_SUPPORTED, OpenIdConnect.Metadata.USERINFO_ENCRYPTION_ALGORITHMS_SUPPORTED, OpenIdConnect.Metadata.USERINFO_ENCRYPTION_METHODS_SUPPORTED, OpenIdConnect.Metadata.DISPLAY_VALUES_SUPPORTED, OpenIdConnect.Metadata.CLAIM_TYPES_SUPPORTED, OpenIdConnect.Metadata.CLAIMS_SUPPORTED, OpenIdConnect.Metadata.CLAIMS_LOCALES_SUPPORTED, OpenIdConnect.Metadata.CLAIMS_PARAMETER_SUPPORTED, OpenIdConnect.Metadata.REQUEST_PARAMETER_SUPPORTED, OpenIdConnect.Metadata.REQUEST_URI_PARAMETER_SUPPORTED, OpenIdConnect.Metadata.REQUIRE_REQUEST_URI_REGISTRATION, OpenIdConnect.Metadata.END_SESSION_ENDPOINT -> true;
            default -> false;
        };
    }

    /**
     * Validates a mandatory HTTPS protocol URL.
     *
     * @param value        URL wire value
     * @param queryAllowed whether query parameters are permitted
     * @param label        safe diagnostic label
     * @return unchanged validated URL
     * @throws ValidateException if the URL is insecure or has forbidden components
     */
    private static String secureUrl(final String value, final boolean queryAllowed, final String label) {
        Assert.notBlank(value, label + " must not be blank");
        final UnoUrl url;
        try {
            url = UnoUrl.parse(value);
        } catch (RuntimeException cause) {
            throw new ValidateException(label + " must be a valid URL", cause);
        }
        if (!value.equals(value.trim()) || !Protocol.HTTPS.name.equals(url.scheme()) || url.host().isEmpty()
                || !url.username().isEmpty() || !url.password().isEmpty() || url.fragment() != null
                || !queryAllowed && !url.query().isEmpty()) {
            throw new ValidateException(label + " must be an HTTPS URL without userinfo or fragment"
                    + (queryAllowed ? Normal.EMPTY : " or query"));
        }
        return value;
    }

    /**
     * Normalizes an optional HTTPS protocol URL.
     *
     * @param value optional URL container
     * @param label safe diagnostic label
     * @return normalized optional URL
     */
    private static Optional<String> optionalSecureUrl(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final String present = value.getOrNull();
        return Optional.ofNullable(present == null ? null : secureUrl(present, true, label));
    }

    /**
     * Normalizes an optional HTTP or HTTPS documentation URL.
     *
     * @param value optional URL container
     * @param label safe diagnostic label
     * @return normalized optional URL
     * @throws ValidateException if a present URL is not absolute HTTP(S)
     */
    private static Optional<String> optionalDocumentUrl(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final String present = value.getOrNull();
        if (present == null) {
            return Optional.empty();
        }
        try {
            final URI uri = new URI(present);
            if (!uri.isAbsolute() || !(Protocol.HTTP.name.equalsIgnoreCase(uri.getScheme())
                    || Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()))) {
                throw new ValidateException(label + " must be an absolute HTTP or HTTPS URL");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException(label + " must be a valid URL", cause);
        }
        return Optional.of(present);
    }

    /**
     * Normalizes one optional Boolean metadata member.
     *
     * @param value optional Boolean container
     * @param label safe diagnostic label
     * @return normalized Bus optional
     */
    private static Optional<Boolean> optionalBoolean(final Optional<Boolean> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        return Optional.ofNullable(value.getOrNull());
    }

    /**
     * Freezes a unique ordered list of typed metadata values.
     *
     * @param <T>      immutable metadata value type
     * @param values   source values
     * @param label    safe item label
     * @param required whether at least one value is required
     * @return immutable ordered values
     * @throws ValidateException if the list is empty when required or contains duplicates
     */
    private static <T> List<T> uniqueList(final List<T> values, final String label, final boolean required) {
        Assert.notNull(values, label + " list must not be null");
        if (required && values.isEmpty()) {
            throw new ValidateException(label + " list must not be empty");
        }
        final Set<T> unique = new HashSet<>(values.size());
        for (T value : values) {
            Assert.notNull(value, label + " must not be null");
            if (!unique.add(value)) {
                throw new ValidateException(label + " list must not contain duplicates");
            }
        }
        return List.copyOf(values);
    }

    /**
     * Freezes a unique ordered list of non-blank string values.
     *
     * @param values   source values
     * @param label    safe item label
     * @param required whether at least one value is required
     * @return immutable ordered values
     */
    private static List<String> textList(final List<String> values, final String label, final boolean required) {
        Assert.notNull(values, label + " list must not be null");
        if (required && values.isEmpty()) {
            throw new ValidateException(label + " list must not be empty");
        }
        final Set<String> unique = new HashSet<>(values.size());
        for (String value : values) {
            Assert.notBlank(value, label + " must not be blank");
            if (!unique.add(value)) {
                throw new ValidateException(label + " list must not contain duplicates");
            }
        }
        return List.copyOf(values);
    }

    /**
     * Freezes a unique ordered list of JWT StringOrURI values.
     *
     * @param values source values
     * @param label  safe item label
     * @return immutable ordered values
     */
    private static List<String> stringOrUriList(final List<String> values, final String label) {
        final List<String> frozen = textList(values, label, false);
        for (String value : frozen) {
            if (value.indexOf(Symbol.C_COLON) >= 0) {
                try {
                    new URI(value);
                } catch (URISyntaxException cause) {
                    throw new ValidateException(label + " must satisfy JWT StringOrURI syntax", cause);
                }
            }
        }
        return frozen;
    }

    /**
     * Validates and freezes BCP 47 language tags.
     *
     * @param values source language tags
     * @param label  safe item label
     * @return immutable ordered language tags
     */
    private static List<String> localeList(final List<String> values, final String label) {
        final List<String> frozen = textList(values, label, false);
        for (String value : frozen) {
            try {
                new Locale.Builder().setLanguageTag(value).build();
            } catch (IllformedLocaleException cause) {
                throw new ValidateException(label + " must be a valid BCP 47 tag", cause);
            }
        }
        return frozen;
    }

    /**
     * Requires one exact value in a metadata list.
     *
     * @param <T>      metadata value type
     * @param values   immutable source list
     * @param required required value
     * @param label    safe list label
     * @throws ValidateException if the required value is absent
     */
    private static <T> void requireContains(final List<T> values, final T required, final String label) {
        if (!values.contains(required)) {
            throw new ValidateException(label + " must contain " + required);
        }
    }

    /**
     * Returns the composed authorization-server issuer.
     *
     * @return issuer identifier
     */
    public String issuer() {
        return authorizationServerMetadata.issuer();
    }

    /**
     * Returns the composed OAuth authorization endpoint.
     *
     * @return optional authorization endpoint
     */
    public Optional<String> authorizationEndpoint() {
        return authorizationServerMetadata.authorizationEndpoint();
    }

    /**
     * Returns the composed OAuth token endpoint.
     *
     * @return optional token endpoint
     */
    public Optional<String> tokenEndpoint() {
        return authorizationServerMetadata.tokenEndpoint();
    }

    /**
     * Returns the composed JWK Set endpoint.
     *
     * @return optional JWK Set URI
     */
    public Optional<String> jwksUri() {
        return authorizationServerMetadata.jwksUri();
    }

    /**
     * Returns the composed supported scopes.
     *
     * @return immutable supported scopes
     */
    public List<String> scopesSupported() {
        return authorizationServerMetadata.scopesSupported();
    }

    /**
     * Returns the composed supported response types.
     *
     * @return immutable supported response types
     */
    public List<ResponseType> responseTypesSupported() {
        return authorizationServerMetadata.responseTypesSupported();
    }

    /**
     * Returns the composed supported response modes.
     *
     * @return immutable supported response modes
     */
    public List<String> responseModesSupported() {
        return authorizationServerMetadata.responseModesSupported();
    }

    /**
     * Returns the composed token-endpoint client authentication methods.
     *
     * @return immutable client authentication methods
     */
    public List<org.miaixz.bus.auth.protocol.oauth2.ClientAuthenticationMethod> tokenEndpointAuthMethodsSupported() {
        return authorizationServerMetadata.tokenEndpointAuthMethodsSupported();
    }

    /**
     * Returns a diagnostic summary without extension values.
     *
     * @return non-sensitive metadata summary
     */
    @Override
    public String toString() {
        return "OpenIdProviderMetadata[authorizationServerMetadata=" + authorizationServerMetadata
                + ", userInfoEndpoint=" + userInfoEndpoint + ", subjectTypesSupported=" + subjectTypesSupported
                + ", endSessionEndpoint=" + endSessionEndpoint + ", extensions=[REDACTED]]";
    }

}
