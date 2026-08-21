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
package org.miaixz.bus.auth.vendor.kujiale;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.protocol.oauth2.Scope;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Carries externally managed Kujiale application registration values.
 * <p>
 * Official endpoints remain manifest-owned. This immutable value stores only a reference to the client secret and never
 * resolves, copies, or exposes the secret material.
 * </p>
 *
 * @param vendor      exact Kujiale platform identifier
 * @param variant     exact default Kujiale variant
 * @param clientId    registered Kujiale client identifier
 * @param credential  external client-secret reference
 * @param redirectUri exact registered callback URI
 * @param scopes      non-empty ordered Kujiale authorization scopes
 * @author Kimi Liu
 */
public record KujialeOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<KujialeOptions> {

    /**
     * Validates and freezes one Kujiale registration without resolving its secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential type, callback, or scopes differ from the frozen manifest
     */
    public KujialeOptions {
        if (!KujialeManifest.ID.equals(vendor) || !KujialeManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("Kujiale options must select kujiale/default");
        }
        Assert.notBlank(clientId, "Kujiale client identifier must not be blank");
        Assert.notNull(credential, "Kujiale client-secret reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Kujiale credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Kujiale redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Kujiale options require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());

        Assert.notNull(scopes, "Kujiale scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Kujiale scope must not be blank");
            Scope.parse(checked);
            if (!approvedScope(checked) || copy.contains(checked)) {
                throw new ValidateException("Kujiale scope is unsupported or duplicated");
            }
            copy.add(checked);
        }
        if (copy.isEmpty()) {
            throw new ValidateException("Kujiale options require at least one scope");
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Identifies a permission scope supported by the Kujiale Source contract.
     *
     * @param value parsed scope value
     * @return whether the scope is registered for Kujiale authorization
     */
    private static boolean approvedScope(final String value) {
        return "get_user_info".equals(value) || "get_design".equals(value) || "get_budget_list".equals(value);
    }

    /**
     * Validates one credential-free absolute callback without changing its lexical value.
     *
     * @param value registered callback URI
     * @throws IllegalArgumentException if callback text is blank
     * @throws ValidateException        if it is not absolute, credential-free, or fragmentless
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Kujiale redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!uri.isAbsolute() || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException("Kujiale redirect URI must be absolute, credential-free, and fragmentless");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Kujiale redirect URI is invalid", cause);
        }
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<KujialeOptions> type() {
        return KujialeOptions.class;
    }

    /**
     * Returns a diagnostic summary without client, secret-reference, or callback values.
     *
     * @return redacted immutable options summary
     */
    @Override
    public String toString() {
        return "KujialeOptions[vendor=" + vendor + Builder.VARIANT + variant + Builder.REDACTED_SOURCE_OPTIONS + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
