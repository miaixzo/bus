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
package org.miaixz.bus.auth.source.vendor.mi;

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

/**
 * Carries externally managed Xiaomi Open Platform client values.
 * <p>
 * Fixed Xiaomi endpoints and all query-wire deviations remain manifest- and adapter-owned. This record retains only the
 * public Client ID, external Client Secret reference, exact callback, and approved account scopes.
 * </p>
 *
 * @param vendor      exact Xiaomi platform identifier
 * @param variant     exact default Xiaomi variant
 * @param clientId    registered Xiaomi Client ID
 * @param credential  external Client Secret reference
 * @param redirectUri exact registered authorization callback URI
 * @param scopes      ordered Xiaomi scopes, or empty to use manifest defaults
 * @author Kimi Liu
 */
public record MiOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<MiOptions> {

    /**
     * Validates and freezes one Xiaomi registration without resolving its Client Secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, scope vocabulary, or identity coverage is
     *                                  invalid
     */
    public MiOptions {
        if (!MiManifest.ID.equals(vendor) || !MiManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("Xiaomi options must select mi/default");
        }
        Assert.notBlank(clientId, "Xiaomi client id must not be blank");
        Assert.notNull(credential, "Xiaomi credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Xiaomi credential must reference a Client Secret");
        }
        Assert.notNull(redirectUri, "Xiaomi redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Xiaomi options require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());

        Assert.notNull(scopes, "Xiaomi scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Xiaomi scope must not be blank");
            Scope.parse(checked);
            if (!approvedScope(checked) || copy.contains(checked)) {
                throw new ValidateException("Xiaomi scopes must be unique registered account scope values");
            }
            copy.add(checked);
        }
        if (!copy.isEmpty() && (!copy.contains("user/profile") || !copy.contains("user/openIdV2"))) {
            throw new ValidateException("Explicit Xiaomi scopes must include profile and OpenID access");
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Identifies a Xiaomi account permission retained by the Source contract.
     *
     * @param value parsed scope value
     * @return whether the scope is supported
     */
    private static boolean approvedScope(final String value) {
        return "user/profile".equals(value) || "user/openIdV2".equals(value) || "user/phoneAndEmail".equals(value);
    }

    /**
     * Validates one exact absolute credential-free callback without changing its lexical value.
     *
     * @param value registered callback URI
     * @throws IllegalArgumentException if the value is blank
     * @throws ValidateException        if the URI is not absolute or contains credentials or a fragment
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Xiaomi redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!uri.isAbsolute() || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException("Xiaomi redirect URI must be absolute, credential-free, and fragmentless");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Xiaomi redirect URI is invalid", cause);
        }
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<MiOptions> type() {
        return MiOptions.class;
    }

    /**
     * Returns a diagnostic representation without client, credential, or callback values.
     *
     * @return redacted options description
     */
    @Override
    public String toString() {
        return "MiOptions[vendor=" + vendor + Builder.VARIANT + variant + Builder.REDACTED_SOURCE_OPTIONS + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
