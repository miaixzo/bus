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
package org.miaixz.bus.auth.vendor.google;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.protocol.oauth2.Scope;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorSettings;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed Google web-server OpenID Connect client values.
 * <p>
 * Fixed Google endpoints, issuer, S256 PKCE, and RS256 validation remain definition- and adapter-owned. This record
 * retains only the public Client ID, an external Client Secret reference, the authorized redirect URI, and exact OIDC
 * scopes.
 * </p>
 *
 * @param vendor      exact Google platform identifier
 * @param variant     exact default Google web-server variant
 * @param clientId    registered Google OAuth client ID
 * @param credential  external Client Secret reference
 * @param redirectUri exact Authorized redirect URI
 * @param scopes      ordered Google OAuth scopes which are exactly {@code openid}, {@code profile}, and {@code email}
 * @author Kimi Liu
 */
public record GoogleSourceSettings(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes) implements VendorSettings {

    /**
     * Stable identifier of the sole Google web-server OpenID Connect variant.
     */
    /**
     * Validates and freezes one Google registration without resolving its Client Secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, or scopes differ from the frozen definition
     */
    public GoogleSourceSettings {
        if (!GoogleDefinition.ID.equals(vendor) || !GoogleDefinition.DEFAULT.equals(variant)) {
            throw new ValidateException("Google settings must select google/default");
        }
        Assert.notBlank(clientId, "Google client id must not be blank");
        Assert.notNull(credential, "Google credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Google credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Google redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Google settings require an Authorized redirect URI");
        }
        redirect(redirectUri.getOrNull());

        Assert.notNull(scopes, "Google scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Google scope must not be blank");
            Scope.parse(checked);
            if (!approvedScope(checked) || copy.contains(checked)) {
                throw new ValidateException("Google scope is unsupported or duplicated by the frozen login definition");
            }
            copy.add(checked);
        }
        if (copy.isEmpty()) {
            copy.addAll(List.of("openid", "profile", "email"));
        }
        if (!copy.equals(List.of("openid", "profile", "email"))) {
            throw new ValidateException("Google scopes must be exactly openid, profile, and email in that order");
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Tests one value against the exact OpenID Connect identity scope set.
     *
     * @param value exact scope value
     * @return whether the scope belongs to the Google identity projection
     */
    private static boolean approvedScope(final String value) {
        return "openid".equals(value) || "profile".equals(value) || "email".equals(value);
    }

    /**
     * Validates one credential-free absolute HTTPS callback without changing its lexical value.
     *
     * @param value registered callback URI
     * @throws IllegalArgumentException if the callback text is blank
     * @throws ValidateException        if the callback is not an absolute credential-free fragmentless HTTPS URI
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Google redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Google redirect URI must be credential-free absolute HTTPS without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Google redirect URI is invalid", cause);
        }
    }

    /**
     * Returns a diagnostic representation without app, credential, or callback values.
     *
     * @return redacted settings description
     */
    @Override
    public String toString() {
        return "GoogleSourceSettings[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
