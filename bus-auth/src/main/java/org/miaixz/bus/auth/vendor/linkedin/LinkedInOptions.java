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
package org.miaixz.bus.auth.vendor.linkedin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.protocol.oauth2.Scope;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed LinkedIn OpenID Connect client registration values.
 * <p>
 * LinkedIn endpoints, issuer compatibility, token behavior, and RS256 validation remain manifest- and adapter-owned.
 * This immutable record retains only the public client identifier, an external client-secret reference, the exact
 * registered callback, and the requested current-product scopes.
 * </p>
 *
 * @param vendor      exact LinkedIn platform identifier
 * @param variant     exact default LinkedIn OpenID Connect variant
 * @param clientId    public client identifier issued by LinkedIn
 * @param credential  external client-secret reference
 * @param redirectUri exact callback URI registered in the LinkedIn Developer Portal
 * @param scopes      ordered current-product scopes containing {@code openid} and {@code profile}
 * @author Kimi Liu
 */
public record LinkedInOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes)
        implements VendorOptions<LinkedInOptions> {

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<LinkedInOptions> type() {
        return LinkedInOptions.class;
    }

    /**
     * Validates and freezes one LinkedIn registration without resolving its client secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential type, callback, or scopes differ from the frozen manifest
     */
    public LinkedInOptions {
        if (!LinkedInManifest.ID.equals(vendor) || !LinkedInManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("LinkedIn options must select linkedin/default");
        }
        Assert.notBlank(clientId, "LinkedIn client identifier must not be blank");
        Assert.notNull(credential, "LinkedIn client-secret reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("LinkedIn credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "LinkedIn redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("LinkedIn options require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());

        Assert.notNull(scopes, "LinkedIn scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "LinkedIn scope must not be blank");
            Scope.parse(checked);
            if (!approvedScope(checked) || copy.contains(checked)) {
                throw new ValidateException("LinkedIn scope is unsupported or duplicated");
            }
            copy.add(checked);
        }
        if (!copy.contains("openid") || !copy.contains("profile")) {
            throw new ValidateException("LinkedIn scopes must contain openid and profile");
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Identifies a scope in LinkedIn's current OpenID Connect sign-in product.
     *
     * @param value parsed scope value
     * @return whether the scope is supported
     */
    private static boolean approvedScope(final String value) {
        return "openid".equals(value) || "profile".equals(value) || "email".equals(value);
    }

    /**
     * Validates one credential-free absolute HTTPS callback without changing its lexical value.
     *
     * @param value callback URI registered in the LinkedIn Developer Portal
     * @throws IllegalArgumentException if callback text is blank
     * @throws ValidateException        if it is not an absolute HTTPS URI with a host, or contains credentials or a
     *                                  fragment
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "LinkedIn redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!uri.isAbsolute() || !Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "LinkedIn redirect URI must be an absolute credential-free fragmentless HTTPS URI");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("LinkedIn redirect URI is invalid", cause);
        }
    }

    /**
     * Returns a diagnostic summary without client, credential-reference, or callback values.
     *
     * @return redacted immutable options summary
     */
    @Override
    public String toString() {
        return "LinkedInOptions[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
