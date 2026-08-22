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
package org.miaixz.bus.auth.vendor.okta;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.protocol.oauth2.Scope;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed Okta login-client or service-app management selectors.
 * <p>
 * This immutable record never stores credential material or an endpoint override. The default Variant combines a Client
 * Secret reference with a custom authorization-server selector and browser callback. The management Variant combines a
 * Private Key reference with the organization selector, fixed Management API scopes, and no browser or custom
 * authorization-server values.
 * </p>
 *
 * @param vendor                exact Okta platform identifier
 * @param variant               exact default login or management Variant
 * @param clientId              public OIDC client or service-app identifier issued by Okta
 * @param credential            external Client Secret or Private Key reference selected by the Variant
 * @param redirectUri           exact Sign-in callback for login, empty for management
 * @param scopes                ordered OIDC or fixed Management API scopes
 * @param instance              Okta organization DNS label preceding {@code .okta.com}
 * @param authorizationServerId custom authorization-server identifier for login, empty for management
 * @author Kimi Liu
 */
public record OktaOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes, String instance, String authorizationServerId)
        implements VendorOptions<OktaOptions> {

    /**
     * Validates and freezes one Okta login or management registration without resolving credential material.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential type, callback, scopes, or template selectors are invalid
     */
    public OktaOptions {
        if (!OktaManifest.ID.equals(vendor)
                || !OktaManifest.DEFAULT.equals(variant) && !OktaManifest.MANAGEMENT.equals(variant)) {
            throw new ValidateException("Okta options must select okta/default or okta/management");
        }
        clientId = Assert.notBlank(clientId, "Okta client identifier must not be blank");
        Assert.notNull(credential, "Okta credential reference must not be null");
        Assert.notNull(redirectUri, "Okta redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        Assert.notNull(scopes, "Okta scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Okta scope must not be blank");
            Scope.parse(checked);
            if (copy.contains(checked)) {
                throw new ValidateException("Okta scopes must not contain duplicates");
            }
            copy.add(checked);
        }
        instance = organization(instance);
        Assert.notNull(authorizationServerId, "Okta authorization server identifier must not be null");

        if (OktaManifest.DEFAULT.equals(variant)) {
            if (credential.type() != Credential.Type.CLIENT_SECRET) {
                throw new ValidateException("Okta login credential must reference a Client Secret");
            }
            if (redirectUri.isEmpty()) {
                throw new ValidateException("Okta login options require a Sign-in redirect URI");
            }
            redirect(redirectUri.getOrNull());
            if (!copy.isEmpty() && !copy.contains("openid")) {
                throw new ValidateException("Explicit Okta login scopes must include openid");
            }
            authorizationServerId = authorizationServer(authorizationServerId);
        } else {
            if (credential.type() != Credential.Type.PRIVATE_KEY) {
                throw new ValidateException("Okta management credential must reference a Private Key");
            }
            if (redirectUri.isPresent()) {
                throw new ValidateException("Okta management options prohibit a redirect URI");
            }
            if (!copy.equals(OktaManifest.MANAGEMENT_SCOPES)) {
                throw new ValidateException("Okta management scopes must match the frozen scope order");
            }
            if (!Normal.EMPTY.equals(authorizationServerId)) {
                throw new ValidateException("Okta management options prohibit an authorization server identifier");
            }
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Validates one exact registered callback URI for a confidential browser client.
     *
     * @param value Sign-in redirect URI registered in Okta
     * @throws IllegalArgumentException if callback text is blank
     * @throws ValidateException        if the callback is not credential-free, absolute, fragmentless HTTPS or loopback
     *                                  HTTP
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Okta redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            final String host = uri.getHost();
            final boolean secure = Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme());
            final boolean loopback = Protocol.HTTP.name.equalsIgnoreCase(uri.getScheme()) && loopback(host);
            if (!uri.isAbsolute() || host == null || !secure && !loopback || uri.getRawUserInfo() != null
                    || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Okta redirect URI must be credential-free HTTPS or loopback HTTP without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Okta redirect URI is invalid", cause);
        }
    }

    /**
     * Reports whether a parsed callback host is a literal local-development loopback.
     *
     * @param host parsed URI host
     * @return {@code true} only for accepted loopback host spellings
     */
    private static boolean loopback(final String host) {
        return host != null && (Protocol.HOST_LOCAL.equalsIgnoreCase(host) || Protocol.HOST_IPV4.equals(host)
                || Protocol.HOST_IPV6.equals(host) || Protocol.HOST_IPV6_BRACKETED.equals(host));
    }

    /**
     * Validates and canonicalizes the single DNS label preceding {@code okta.com}.
     *
     * @param value externally loaded Okta organization label
     * @return canonical lowercase ASCII DNS label
     * @throws IllegalArgumentException if the value is blank
     * @throws ValidateException        if the value is not one complete DNS label
     */
    private static String organization(final String value) {
        final String ascii;
        try {
            ascii = IDN.toASCII(Assert.notBlank(value, "Okta instance must not be blank"));
        } catch (IllegalArgumentException cause) {
            throw new ValidateException("Okta instance is not a valid DNS label", cause);
        }
        if (ascii.length() > 63 || ascii.indexOf(Symbol.C_DOT) >= 0 || ascii.startsWith(Symbol.MINUS)
                || ascii.endsWith(Symbol.MINUS) || !ascii.chars()
                        .allMatch(character -> Character.isLetterOrDigit(character) || character == Symbol.C_MINUS)) {
            throw new ValidateException("Okta instance must be one canonical DNS label");
        }
        return ascii.toLowerCase(Locale.ROOT);
    }

    /**
     * Validates one opaque Okta authorization-server path identifier without accepting a path or URL.
     *
     * @param value externally loaded authorization-server identifier
     * @return unchanged validated identifier
     * @throws IllegalArgumentException if the value is blank
     * @throws ValidateException        if the value is too long or contains unsupported identifier characters
     */
    private static String authorizationServer(final String value) {
        final String checked = Assert.notBlank(value, "Okta authorization server identifier must not be blank");
        if (checked.length() > Normal._128 || !checked.chars().allMatch(
                character -> Character.isLetterOrDigit(character) || character == Symbol.C_MINUS
                        || character == Symbol.C_UNDERLINE)) {
            throw new ValidateException("Okta authorization server identifier contains unsupported characters");
        }
        return checked;
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<OktaOptions> type() {
        return OktaOptions.class;
    }

    /**
     * Returns the organization label consumed by manifest-owned host templates.
     *
     * @return present validated Okta organization label
     */
    @Override
    public Optional<String> templateInstance() {
        return Optional.of(instance);
    }

    /**
     * Returns the identifier consumed only by the login authorization-server path templates.
     *
     * @return present validated login identifier or empty for management
     */
    @Override
    public Optional<String> templateAuthorizationServerId() {
        return OktaManifest.DEFAULT.equals(variant) ? Optional.of(authorizationServerId) : Optional.empty();
    }

    /**
     * Returns a diagnostic representation without client, credential, callback, scope, or selector values.
     *
     * @return redacted immutable options summary
     */
    @Override
    public String toString() {
        return "OktaOptions[vendor=" + vendor + Builder.VARIANT + variant + Builder.REDACTED_SOURCE_OPTIONS
                + Builder.REDACTED_VALUE + Symbol.C_BRACKET_RIGHT;
    }

}
