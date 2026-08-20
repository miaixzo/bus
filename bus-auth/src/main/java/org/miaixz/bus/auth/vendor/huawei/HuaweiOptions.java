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
package org.miaixz.bus.auth.vendor.huawei;

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
 * Carries externally managed Huawei Account Kit web-client registration values.
 * <p>
 * Huawei's issuer, endpoints, S256 requirement, form-post response mode, and RS256 verification policy remain owned by
 * the manifest and adapter. This record never resolves or exposes the referenced Client Secret.
 * </p>
 *
 * @param vendor      exact Huawei platform identifier
 * @param variant     exact default Huawei web variant
 * @param clientId    registered AppGallery Connect client identifier
 * @param credential  external Client Secret reference
 * @param redirectUri exact registered HTTPS callback URI
 * @param scopes      ordered scopes which are exactly {@code openid}, {@code profile}, and {@code email}
 * @author Kimi Liu
 */
public record HuaweiOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<HuaweiOptions> {

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<HuaweiOptions> type() {
        return HuaweiOptions.class;
    }

    /**
     * Validates and freezes one Huawei registration without resolving secret material.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential type, callback, or scope order differs from the frozen
     *                                  profile
     */
    public HuaweiOptions {
        if (!HuaweiManifest.ID.equals(vendor) || !HuaweiManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("Huawei options must select huawei/default");
        }
        Assert.notBlank(clientId, "Huawei client id must not be blank");
        Assert.notNull(credential, "Huawei credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Huawei credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Huawei redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Huawei options require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());

        Assert.notNull(scopes, "Huawei scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Huawei scope must not be blank");
            Scope.parse(checked);
            if (!approvedScope(checked) || copy.contains(checked)) {
                throw new ValidateException("Huawei scope is unsupported or duplicated by the frozen manifest");
            }
            copy.add(checked);
        }
        if (copy.isEmpty()) {
            copy.addAll(List.of("openid", "profile", "email"));
        }
        if (!copy.equals(List.of("openid", "profile", "email"))) {
            throw new ValidateException("Huawei scopes must be exactly openid, profile, and email in that order");
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Tests one value against the exact Huawei OpenID Connect identity scopes.
     *
     * @param value exact scope value
     * @return whether the scope is supported
     */
    private static boolean approvedScope(final String value) {
        return "openid".equals(value) || "profile".equals(value) || "email".equals(value);
    }

    /**
     * Validates a credential-free absolute HTTPS callback without normalizing its lexical value.
     *
     * @param value registered callback URI
     * @throws IllegalArgumentException if the callback text is blank
     * @throws ValidateException        if it is not absolute credential-free fragmentless HTTPS
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Huawei redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Huawei redirect URI must be credential-free absolute HTTPS without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Huawei redirect URI is invalid", cause);
        }
    }

    /**
     * Returns a diagnostic summary without client, credential, or callback material.
     *
     * @return redacted immutable options summary
     */
    @Override
    public String toString() {
        return "HuaweiOptions[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
