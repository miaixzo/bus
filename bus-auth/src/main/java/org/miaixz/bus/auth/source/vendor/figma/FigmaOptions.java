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
package org.miaixz.bus.auth.source.vendor.figma;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.source.protocol.oauth2.Scope;
import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Carries externally managed Figma.com OAuth app or administrator SCIM values.
 * <p>
 * Fixed Figma endpoints and mandatory S256 PKCE remain manifest-owned. This record retains only the public Client ID,
 * an external Client Secret reference, the registered callback, and the ordered OAuth scopes.
 * </p>
 *
 * @param vendor      exact Figma platform identifier
 * @param variant     exact default login or SCIM Variant
 * @param clientId    OAuth Client ID or SCIM Tenant ID
 * @param credential  external Client Secret or SCIM Token reference
 * @param redirectUri exact OAuth callback or empty for SCIM
 * @param scopes      ordered OAuth scopes or empty for SCIM
 * @author Kimi Liu
 */
public record FigmaOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<FigmaOptions> {

    /**
     * Minimum identity scope required by the frozen Figma login manifest.
     */
    private static final String CURRENT_USER_READ = "current_user:read";

    /**
     * Validates and freezes one Figma registration without resolving its external credential.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, or scopes differ from the frozen manifest
     */
    public FigmaOptions {
        if (!FigmaManifest.ID.equals(vendor)
                || !(FigmaManifest.DEFAULT.equals(variant) || FigmaManifest.SCIM.equals(variant))) {
            throw new ValidateException("Figma options must select figma/default or figma/scim");
        }
        clientId = Assert.notBlank(
                clientId,
                FigmaManifest.SCIM.equals(variant) ? "Figma SCIM Tenant ID must not be blank"
                        : "Figma client id must not be blank");
        Assert.notNull(credential, "Figma credential reference must not be null");
        Assert.notNull(redirectUri, "Figma redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        Assert.notNull(scopes, "Figma scopes must not be null");
        if (FigmaManifest.SCIM.equals(variant)) {
            if (!clientId.equals(StringKit.trim(clientId))) {
                throw new ValidateException("Figma SCIM Tenant ID must not contain surrounding whitespace");
            }
            if (credential.type() != Credential.Type.SHARED_SECRET) {
                throw new ValidateException("Figma SCIM credential must reference a tenant token");
            }
            if (redirectUri.isPresent() || !scopes.isEmpty()) {
                throw new ValidateException("Figma SCIM options must not contain login callback or scope values");
            }
            scopes = List.of();
        } else {
            if (credential.type() != Credential.Type.CLIENT_SECRET) {
                throw new ValidateException("Figma login credential must reference a client secret");
            }
            if (redirectUri.isEmpty()) {
                throw new ValidateException("Figma options require a registered redirect URI");
            }
            redirect(redirectUri.getOrNull());
            final List<String> copy = new ArrayList<>(scopes.size());
            for (String scope : scopes) {
                final String checked = Assert.notBlank(scope, "Figma scope must not be blank");
                Scope.parse(checked);
                if (copy.contains(checked)) {
                    throw new ValidateException("Figma scopes must not contain duplicates");
                }
                copy.add(checked);
            }
            if (copy.isEmpty()) {
                copy.add(CURRENT_USER_READ);
            }
            if (!copy.contains(CURRENT_USER_READ)) {
                throw new ValidateException("Figma scopes must contain current_user:read");
            }
            scopes = List.copyOf(copy);
        }
    }

    /**
     * Validates one credential-free absolute HTTPS callback without changing its lexical value.
     *
     * @param value registered callback URI
     * @throws IllegalArgumentException if the callback text is blank
     * @throws ValidateException        if the callback is not an absolute credential-free fragmentless HTTPS URI
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Figma redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Figma redirect URI must be credential-free absolute HTTPS without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Figma redirect URI is invalid", cause);
        }
    }

    /**
     * Returns the SCIM Tenant ID only for the manifest-owned tenant path template.
     *
     * @return Tenant ID for SCIM or empty for browser login
     */
    @Override
    public Optional<String> templateTenant() {
        return FigmaManifest.SCIM.equals(variant) ? Optional.of(clientId) : Optional.empty();
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<FigmaOptions> type() {
        return FigmaOptions.class;
    }

    /**
     * Returns a diagnostic representation without app, credential, or callback values.
     *
     * @return redacted options description
     */
    @Override
    public String toString() {
        return "FigmaOptions[vendor=" + vendor + Builder.VARIANT + variant + Builder.REDACTED_SOURCE_OPTIONS + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
