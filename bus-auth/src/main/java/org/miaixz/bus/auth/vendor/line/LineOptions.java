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
package org.miaixz.bus.auth.vendor.line;

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
 * Carries externally managed LINE Login channel registration values.
 * <p>
 * Official endpoints, issuer, PKCE policy, and ID Token algorithms remain manifest-owned. The immutable options keep
 * only a reference to the channel secret and never resolve or expose its material.
 * </p>
 *
 * @param vendor      exact LINE platform identifier
 * @param variant     exact default LINE variant
 * @param clientId    registered LINE channel identifier
 * @param credential  external channel-secret reference
 * @param redirectUri exact registered HTTPS callback URI
 * @param scopes      ordered scopes containing both {@code profile} and {@code openid}
 * @author Kimi Liu
 */
public record LineOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<LineOptions> {

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<LineOptions> type() {
        return LineOptions.class;
    }

    /**
     * Validates and freezes one LINE Login registration without resolving its channel secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential type, callback, or scopes differ from the frozen manifest
     */
    public LineOptions {
        if (!LineManifest.ID.equals(vendor) || !LineManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("LINE options must select line/default");
        }
        Assert.notBlank(clientId, "LINE channel identifier must not be blank");
        Assert.notNull(credential, "LINE channel-secret reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("LINE credential must reference a channel secret");
        }
        Assert.notNull(redirectUri, "LINE redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("LINE options require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());

        Assert.notNull(scopes, "LINE scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "LINE scope must not be blank");
            Scope.parse(checked);
            if (!approvedScope(checked) || copy.contains(checked)) {
                throw new ValidateException("LINE scope is unsupported or duplicated");
            }
            copy.add(checked);
        }
        if (!copy.contains("profile") || !copy.contains("openid")) {
            throw new ValidateException("LINE scopes must contain profile and openid");
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Identifies a scope supported by LINE Login's web authorization contract.
     *
     * @param value parsed scope value
     * @return whether LINE documents the scope for this Source
     */
    private static boolean approvedScope(final String value) {
        return "profile".equals(value) || "openid".equals(value) || "email".equals(value);
    }

    /**
     * Validates one credential-free absolute HTTPS callback without changing its lexical value.
     *
     * @param value registered callback URI
     * @throws IllegalArgumentException if callback text is blank
     * @throws ValidateException        if it is not an absolute HTTPS URI with a host, or contains credentials or a
     *                                  fragment
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "LINE redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!uri.isAbsolute() || !Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "LINE redirect URI must be an absolute credential-free fragmentless HTTPS URI");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("LINE redirect URI is invalid", cause);
        }
    }

    /**
     * Returns a diagnostic summary without channel, secret-reference, or callback values.
     *
     * @return redacted immutable options summary
     */
    @Override
    public String toString() {
        return "LineOptions[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
