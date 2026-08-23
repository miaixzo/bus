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
package org.miaixz.bus.auth.source.vendor.jd;

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
 * Carries externally managed JD application configuration values.
 * <p>
 * Fixed authorization, token, refresh, and Zeus router endpoints remain manifest-owned. The application secret remains
 * an external reference and is never resolved or exposed by this immutable options value.
 * </p>
 *
 * @param vendor      exact JD platform identifier
 * @param variant     exact default JD variant
 * @param clientId    registered JD application key
 * @param credential  external application-secret reference
 * @param redirectUri exact registered callback URI
 * @param scopes      ordered JD authorization scopes
 * @author Kimi Liu
 */
public record JdOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<JdOptions> {

    /**
     * Validates and freezes one JD application registration without resolving its secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential type, callback, or scopes differ from the frozen manifest
     */
    public JdOptions {
        if (!JdManifest.ID.equals(vendor) || !JdManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("JD options must select jd/default");
        }
        Assert.notBlank(clientId, "JD application key must not be blank");
        Assert.notNull(credential, "JD application-secret reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("JD credential must reference an application secret");
        }
        Assert.notNull(redirectUri, "JD redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("JD options require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());
        Assert.notNull(scopes, "JD scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "JD scope must not be blank");
            Scope.parse(checked);
            if (!approvedScope(checked) || copy.contains(checked)) {
                throw new ValidateException("JD scope is unsupported or duplicated");
            }
            copy.add(checked);
        }
        if (copy.isEmpty()) {
            copy.add("snsapi_base");
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Identifies a scope supported by JD's registered authorization variants.
     *
     * @param value parsed scope value
     * @return whether JD documents the scope for this Source
     */
    private static boolean approvedScope(final String value) {
        return "snsapi_base".equals(value) || "snsapi_union_login".equals(value);
    }

    /**
     * Validates one credential-free absolute callback without changing its lexical value.
     *
     * @param value registered callback URI
     * @throws IllegalArgumentException if callback text is blank
     * @throws ValidateException        if it is not absolute, credential-free, or fragmentless
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "JD redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!uri.isAbsolute() || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException("JD redirect URI must be absolute, credential-free, and fragmentless");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("JD redirect URI is invalid", cause);
        }
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<JdOptions> type() {
        return JdOptions.class;
    }

    /**
     * Returns a diagnostic summary without application, secret, or callback values.
     *
     * @return redacted immutable options summary
     */
    @Override
    public String toString() {
        return "JdOptions[vendor=" + vendor + Builder.VARIANT + variant + Builder.REDACTED_SOURCE_OPTIONS + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
