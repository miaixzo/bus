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

import java.util.*;

import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.shared.pkce.PkceMethod;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents OAuth 2.0 Authorization Server Metadata defined by RFC 8414, including extension members implemented by
 * the metadata service.
 * <p>
 * This record is a standards wire model, not a mutable capability builder. Runtime metadata services derive its lists
 * from the compiled Roster and therefore publish only operations and algorithms actually enabled for one authorization
 * server. Dynamic client registration and OIDC-only metadata are deliberately excluded from this OAuth model.
 * </p>
 *
 * @param issuer                                             authorization server issuer identifier
 * @param authorizationEndpoint                              optional authorization endpoint URL
 * @param tokenEndpoint                                      optional token endpoint URL
 * @param jwksUri                                            optional JWK Set document URL
 * @param scopesSupported                                    supported OAuth scope values
 * @param responseTypesSupported                             supported authorization response types
 * @param responseModesSupported                             supported authorization response modes
 * @param grantTypesSupported                                supported token endpoint grant types
 * @param tokenEndpointAuthMethodsSupported                  supported token endpoint client-authentication methods
 * @param tokenEndpointAuthSigningAlgValuesSupported         supported token endpoint client-assertion JWS algorithms
 * @param serviceDocumentation                               optional human-readable service documentation URL
 * @param uiLocalesSupported                                 supported BCP 47 user-interface language tags
 * @param opPolicyUri                                        optional authorization server policy URL
 * @param opTosUri                                           optional authorization server terms-of-service URL
 * @param revocationEndpoint                                 optional RFC 7009 revocation endpoint URL
 * @param revocationEndpointAuthMethodsSupported             supported revocation client-authentication methods
 * @param revocationEndpointAuthSigningAlgValuesSupported    supported revocation client-assertion JWS algorithms
 * @param introspectionEndpoint                              optional RFC 7662 introspection endpoint URL
 * @param introspectionEndpointAuthMethodsSupported          supported introspection client-authentication methods
 * @param introspectionEndpointAuthSigningAlgValuesSupported supported introspection client-assertion JWS algorithms
 * @param codeChallengeMethodsSupported                      supported RFC 7636 code challenge methods
 * @param signedMetadata                                     optional compact signed metadata JWT
 * @param deviceAuthorizationEndpoint                        optional RFC 8628 device authorization endpoint URL
 * @param authorizationResponseIssParameterSupported         optional RFC 9207 issuer-parameter support indicator
 * @param dpopSigningAlgValuesSupported                      supported RFC 9449 DPoP proof JWS algorithms
 * @param extensions                                         unknown metadata members that do not duplicate or enable
 *                                                           excluded registered members
 * @author Kimi Liu
 */
public record AuthorizationServerMetadata(String issuer, Optional<String> authorizationEndpoint,
        Optional<String> tokenEndpoint, Optional<String> jwksUri, List<String> scopesSupported,
        List<ResponseType> responseTypesSupported, List<String> responseModesSupported,
        List<GrantType> grantTypesSupported, List<ClientAuthenticationMethod> tokenEndpointAuthMethodsSupported,
        List<JwaAlgorithm> tokenEndpointAuthSigningAlgValuesSupported, Optional<String> serviceDocumentation,
        List<String> uiLocalesSupported, Optional<String> opPolicyUri, Optional<String> opTosUri,
        Optional<String> revocationEndpoint, List<ClientAuthenticationMethod> revocationEndpointAuthMethodsSupported,
        List<JwaAlgorithm> revocationEndpointAuthSigningAlgValuesSupported, Optional<String> introspectionEndpoint,
        List<ClientAuthenticationMethod> introspectionEndpointAuthMethodsSupported,
        List<JwaAlgorithm> introspectionEndpointAuthSigningAlgValuesSupported,
        List<PkceMethod> codeChallengeMethodsSupported, Optional<String> signedMetadata,
        Optional<String> deviceAuthorizationEndpoint, Optional<Boolean> authorizationResponseIssParameterSupported,
        List<JwaAlgorithm> dpopSigningAlgValuesSupported, JsonValue.ObjectValue extensions) {

    /**
     * Creates and validates immutable RFC 8414 metadata.
     *
     * @throws IllegalArgumentException if a required component, optional container, list, or list entry is null
     * @throws ValidateException        if a URL, scope, locale, duplicate list entry, or extension member violates the
     *                                  applicable metadata specification
     */
    public AuthorizationServerMetadata {
        issuer = validateSecureUrl(issuer, false, "OAuth 2.x metadata issuer");
        Assert.notNull(authorizationEndpoint, "OAuth 2.x authorization endpoint container must not be null");
        Assert.notNull(tokenEndpoint, "OAuth 2.x token endpoint container must not be null");
        Assert.notNull(jwksUri, "OAuth 2.x JWK Set URI container must not be null");
        final String authorization = optionalSecureUrl(authorizationEndpoint, true, "OAuth 2.x authorization endpoint");
        final String token = optionalSecureUrl(tokenEndpoint, true, "OAuth 2.x token endpoint");
        final String jwks = optionalSecureUrl(jwksUri, true, "OAuth 2.x JWK Set URI");

        scopesSupported = copyScopes(scopesSupported);
        responseTypesSupported = copyUnique(responseTypesSupported, "OAuth 2.x supported response type");
        responseModesSupported = copyText(responseModesSupported, "OAuth 2.x supported response mode");
        grantTypesSupported = copyUnique(grantTypesSupported, "OAuth 2.x supported grant type");
        tokenEndpointAuthMethodsSupported = copyUnique(
                tokenEndpointAuthMethodsSupported,
                "OAuth 2.x token endpoint authentication method");
        tokenEndpointAuthSigningAlgValuesSupported = copyUnique(
                tokenEndpointAuthSigningAlgValuesSupported,
                "OAuth 2.x token endpoint signing algorithm");

        Assert.notNull(serviceDocumentation, "OAuth 2.x service documentation container must not be null");
        final String documentation = optionalDocumentUrl(serviceDocumentation, "OAuth 2.x service documentation");
        uiLocalesSupported = copyLocales(uiLocalesSupported);
        Assert.notNull(opPolicyUri, "OAuth 2.x policy URI container must not be null");
        Assert.notNull(opTosUri, "OAuth 2.x terms-of-service URI container must not be null");
        final String policy = optionalDocumentUrl(opPolicyUri, "OAuth 2.x policy URI");
        final String terms = optionalDocumentUrl(opTosUri, "OAuth 2.x terms-of-service URI");

        Assert.notNull(revocationEndpoint, "OAuth 2.x revocation endpoint container must not be null");
        final String revocation = optionalSecureUrl(revocationEndpoint, true, "OAuth 2.x revocation endpoint");
        revocationEndpointAuthMethodsSupported = copyUnique(
                revocationEndpointAuthMethodsSupported,
                "OAuth 2.x revocation endpoint authentication method");
        revocationEndpointAuthSigningAlgValuesSupported = copyUnique(
                revocationEndpointAuthSigningAlgValuesSupported,
                "OAuth 2.x revocation endpoint signing algorithm");

        Assert.notNull(introspectionEndpoint, "OAuth 2.x introspection endpoint container must not be null");
        final String introspection = optionalSecureUrl(introspectionEndpoint, true, "OAuth 2.x introspection endpoint");
        introspectionEndpointAuthMethodsSupported = copyUnique(
                introspectionEndpointAuthMethodsSupported,
                "OAuth 2.x introspection endpoint authentication method");
        introspectionEndpointAuthSigningAlgValuesSupported = copyUnique(
                introspectionEndpointAuthSigningAlgValuesSupported,
                "OAuth 2.x introspection endpoint signing algorithm");

        codeChallengeMethodsSupported = copyUnique(codeChallengeMethodsSupported, "OAuth 2.x code challenge method");
        Assert.notNull(signedMetadata, "OAuth 2.x signed metadata container must not be null");
        final String signed = signedMetadata.getOrNull();
        if (signed != null) {
            Assert.notBlank(signed, "OAuth 2.x signed metadata must not be blank when present");
        }
        Assert.notNull(
                deviceAuthorizationEndpoint,
                "OAuth 2.x device authorization endpoint container must not be null");
        final String device = optionalSecureUrl(
                deviceAuthorizationEndpoint,
                true,
                "OAuth 2.x device authorization endpoint");
        Assert.notNull(
                authorizationResponseIssParameterSupported,
                "OAuth 2.x authorization response issuer support container must not be null");
        final Boolean issuerParameter = authorizationResponseIssParameterSupported.getOrNull();
        dpopSigningAlgValuesSupported = copyUnique(dpopSigningAlgValuesSupported, "OAuth 2.x DPoP signing algorithm");
        Assert.notNull(extensions, "OAuth 2.x metadata extensions must not be null");
        for (String name : extensions.values().keySet()) {
            Assert.notBlank(name, "OAuth metadata extension member name must not be blank");
            if (reserved(name)) {
                throw new ValidateException("OAuth 2.x metadata extensions contain reserved member: " + name);
            }
        }

        authorizationEndpoint = Optional.ofNullable(authorization);
        tokenEndpoint = Optional.ofNullable(token);
        jwksUri = Optional.ofNullable(jwks);
        serviceDocumentation = Optional.ofNullable(documentation);
        opPolicyUri = Optional.ofNullable(policy);
        opTosUri = Optional.ofNullable(terms);
        revocationEndpoint = Optional.ofNullable(revocation);
        introspectionEndpoint = Optional.ofNullable(introspection);
        signedMetadata = Optional.ofNullable(signed);
        deviceAuthorizationEndpoint = Optional.ofNullable(device);
        authorizationResponseIssParameterSupported = Optional.ofNullable(issuerParameter);
        extensions = new JsonValue.ObjectValue(extensions.values());
    }

    /**
     * Identifies RFC 8414 and implemented extension metadata names represented by explicit components.
     *
     * @param name exact case-sensitive metadata member name
     * @return {@code true} when the member is reserved from the extension object
     */
    private static boolean reserved(final String name) {
        return switch (name) {
            case OAuth2.Metadata.ISSUER, OAuth2.Metadata.AUTHORIZATION_ENDPOINT, OAuth2.Metadata.TOKEN_ENDPOINT, OAuth2.Metadata.JWKS_URI, OAuth2.Metadata.REGISTRATION_ENDPOINT, OAuth2.Metadata.SCOPES_SUPPORTED, OAuth2.Metadata.RESPONSE_TYPES_SUPPORTED, OAuth2.Metadata.RESPONSE_MODES_SUPPORTED, OAuth2.Metadata.GRANT_TYPES_SUPPORTED, OAuth2.Metadata.TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED, OAuth2.Metadata.TOKEN_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED, OAuth2.Metadata.SERVICE_DOCUMENTATION, OAuth2.Metadata.UI_LOCALES_SUPPORTED, OAuth2.Metadata.OP_POLICY_URI, OAuth2.Metadata.OP_TOS_URI, OAuth2.Metadata.REVOCATION_ENDPOINT, OAuth2.Metadata.REVOCATION_ENDPOINT_AUTH_METHODS_SUPPORTED, OAuth2.Metadata.REVOCATION_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED, OAuth2.Metadata.INTROSPECTION_ENDPOINT, OAuth2.Metadata.INTROSPECTION_ENDPOINT_AUTH_METHODS_SUPPORTED, OAuth2.Metadata.INTROSPECTION_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED, OAuth2.Metadata.CODE_CHALLENGE_METHODS_SUPPORTED, OAuth2.Metadata.SIGNED_METADATA, OAuth2.Metadata.DEVICE_AUTHORIZATION_ENDPOINT, OAuth2.Metadata.AUTHORIZATION_RESPONSE_ISSUER_SUPPORTED, OAuth2.Metadata.DPOP_SIGNING_ALGORITHMS_SUPPORTED -> true;
            default -> false;
        };
    }

    /**
     * Validates an optional HTTPS protocol URL.
     *
     * @param value        optional URL container
     * @param queryAllowed whether the URL may contain a query component
     * @param label        safe component label used in diagnostics
     * @return contained validated URL or null
     */
    private static String optionalSecureUrl(
            final Optional<String> value,
            final boolean queryAllowed,
            final String label) {
        final String url = value.getOrNull();
        return url == null ? null : validateSecureUrl(url, queryAllowed, label);
    }

    /**
     * Validates an HTTPS URL with no userinfo or fragment.
     *
     * @param value        URL wire value
     * @param queryAllowed whether the URL may contain a query component
     * @param label        safe component label used in diagnostics
     * @return original validated URL
     * @throws ValidateException if the value violates RFC 8414 URL requirements
     */
    private static String validateSecureUrl(final String value, final boolean queryAllowed, final String label) {
        Assert.notBlank(value, label + " must not be blank");
        final Url url;
        try {
            url = Url.parse(value);
        } catch (RuntimeException exception) {
            throw new ValidateException(label + " must be a valid URL", exception);
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
     * Validates an optional HTTP(S) documentation URL.
     *
     * @param value optional URL container
     * @param label safe component label used in diagnostics
     * @return contained validated URL or null
     */
    private static String optionalDocumentUrl(final Optional<String> value, final String label) {
        final String text = value.getOrNull();
        if (text == null) {
            return null;
        }
        Assert.notBlank(text, label + " must not be blank when present");
        final Url url;
        try {
            url = Url.parse(text);
        } catch (RuntimeException exception) {
            throw new ValidateException(label + " must be a valid URL", exception);
        }
        if (!text.equals(text.trim())
                || !Protocol.HTTP.name.equals(url.scheme()) && !Protocol.HTTPS.name.equals(url.scheme())
                || url.host().isEmpty() || !url.username().isEmpty() || !url.password().isEmpty()) {
            throw new ValidateException(label + " must be an HTTP or HTTPS URL without userinfo");
        }
        return text;
    }

    /**
     * Copies unique scope names after applying the shared scope-token grammar.
     *
     * @param values scope names to copy
     * @return immutable ordered scope-name list
     */
    private static List<String> copyScopes(final List<String> values) {
        final List<String> copy = copyText(values, "OAuth 2.x supported scope");
        for (String value : copy) {
            new Scope(List.of(value));
        }
        return copy;
    }

    /**
     * Copies and validates unique BCP 47 language tags.
     *
     * @param values locale tags to copy
     * @return immutable ordered locale-tag list
     * @throws ValidateException if a tag is not well formed
     */
    private static List<String> copyLocales(final List<String> values) {
        final List<String> copy = copyText(values, "OAuth 2.x supported UI locale");
        for (String value : copy) {
            try {
                new Locale.Builder().setLanguageTag(value).build();
            } catch (IllformedLocaleException exception) {
                throw new ValidateException("OAuth 2.x supported UI locale must be a valid BCP 47 tag", exception);
            }
        }
        return copy;
    }

    /**
     * Copies a list of unique non-empty strings while retaining order.
     *
     * @param values string values to copy
     * @param label  safe element label used in diagnostics
     * @return immutable ordered value list
     */
    private static List<String> copyText(final List<String> values, final String label) {
        final List<String> copy = copyUnique(values, label);
        for (String value : copy) {
            Assert.notEmpty(value, label + " must not be empty");
        }
        return copy;
    }

    /**
     * Copies a list and rejects null or duplicate elements without changing order.
     *
     * @param <T>    element type
     * @param values values to copy
     * @param label  safe element label used in diagnostics
     * @return immutable ordered unique list
     * @throws IllegalArgumentException if the list or an element is null
     * @throws ValidateException        if an element is duplicated
     */
    private static <T> List<T> copyUnique(final List<T> values, final String label) {
        Assert.notNull(values, label + " list must not be null");
        final List<T> copy = new ArrayList<>(values.size());
        final Set<T> unique = new HashSet<>(values.size());
        for (T value : values) {
            final T item = Assert.notNull(value, label + " must not be null");
            if (!unique.add(item)) {
                throw new ValidateException(label + " list must not contain duplicates");
            }
            copy.add(item);
        }
        return List.copyOf(copy);
    }

    /**
     * Returns a diagnostic representation that omits signed and extension metadata content.
     *
     * @return metadata summary without compact JWT or extension values
     */
    @Override
    public String toString() {
        return "AuthorizationServerMetadata[issuer=" + issuer + ", authorizationEndpoint=" + authorizationEndpoint
                + ", tokenEndpoint=" + tokenEndpoint + ", jwksUri=" + jwksUri + ", responseTypesSupported="
                + responseTypesSupported + ", grantTypesSupported=" + grantTypesSupported
                + ", signedMetadata=[REDACTED], extensions=[REDACTED]]";
    }

}
