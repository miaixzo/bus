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
package org.miaixz.bus.auth.source.vendor.facebook;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed Facebook Login OAuth client values.
 * <p>
 * Graph API v26.0 endpoints and app-secret-proof behavior remain manifest-owned. This record retains only the public
 * app identifier, an external app-secret reference, the exact registered callback, and requested login permissions.
 * </p>
 *
 * @param vendor      exact Facebook platform identifier
 * @param variant     exact default Facebook Login variant
 * @param clientId    registered Meta app identifier
 * @param credential  external app-secret reference
 * @param redirectUri exact Valid OAuth Redirect URI
 * @param scopes      ordered requested permissions, or empty to use manifest defaults
 * @author Kimi Liu
 */
public record FacebookOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes)
        implements VendorOptions<FacebookOptions> {

    /**
     * Validates and freezes one Facebook Login registration without resolving its app secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, scope vocabulary, uniqueness, or identity
     *                                  coverage differs from the frozen manifest
     */
    public FacebookOptions {
        if (!FacebookManifest.ID.equals(vendor) || !FacebookManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("Facebook options must select facebook/default");
        }
        Assert.notBlank(clientId, "Facebook client id must not be blank");
        Assert.notNull(credential, "Facebook credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Facebook credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Facebook redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Facebook Login options require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());

        Assert.notNull(scopes, "Facebook scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Facebook scope must not be blank");
            if (!scope(checked) || copy.contains(checked)) {
                throw new ValidateException("Facebook scopes must be unique public_profile or email values");
            }
            copy.add(checked);
        }
        if (!copy.isEmpty() && !"public_profile".equals(copy.get(0))) {
            throw new ValidateException("Explicit Facebook scopes must begin with public_profile");
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Tests one value against the Facebook Login permissions used by this identity projection.
     *
     * @param value permission value
     * @return whether the permission is supported
     */
    private static boolean scope(final String value) {
        return "public_profile".equals(value) || "email".equals(value);
    }

    /**
     * Validates one credential-free absolute HTTPS callback without changing its lexical value.
     *
     * @param value registered callback URI
     * @throws IllegalArgumentException if the callback text is blank
     * @throws ValidateException        if the callback is not an absolute credential-free fragmentless HTTPS URI
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Facebook redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Facebook redirect URI must be credential-free absolute HTTPS without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Facebook redirect URI is invalid", cause);
        }
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<FacebookOptions> type() {
        return FacebookOptions.class;
    }

    /**
     * Returns a diagnostic representation without app, credential, or callback values.
     *
     * @return redacted options description
     */
    @Override
    public String toString() {
        return "FacebookOptions[vendor=" + vendor + Builder.VARIANT + variant + Builder.REDACTED_SOURCE_OPTIONS + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
