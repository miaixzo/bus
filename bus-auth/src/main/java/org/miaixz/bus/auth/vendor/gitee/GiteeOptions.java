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
package org.miaixz.bus.auth.vendor.gitee;

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
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed Gitee third-party application values.
 * <p>
 * Fixed Gitee.com endpoints and the prohibition on PKCE remain manifest-owned. This record retains only the public
 * Client ID, an external Client Secret reference, the registered callback, and the ordered OAuth scopes.
 * </p>
 *
 * @param vendor      exact Gitee platform identifier
 * @param variant     exact default Gitee.com variant
 * @param clientId    registered Gitee Client ID
 * @param credential  external Client Secret reference
 * @param redirectUri exact registered callback URI
 * @param scopes      ordered Gitee OAuth scopes whose first value is {@code user_info}
 * @author Kimi Liu
 */
public record GiteeOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<GiteeOptions> {

    /**
     * Minimum identity scope required by the frozen Gitee login manifest.
     */
    private static final String USER_INFO = "user_info";

    /**
     * Validates and freezes one Gitee registration without resolving its Client Secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, or scopes differ from the frozen manifest
     */
    public GiteeOptions {
        if (!GiteeManifest.ID.equals(vendor) || !GiteeManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("Gitee options must select gitee/default");
        }
        Assert.notBlank(clientId, "Gitee client id must not be blank");
        Assert.notNull(credential, "Gitee credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Gitee credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Gitee redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Gitee options require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());

        Assert.notNull(scopes, "Gitee scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Gitee scope must not be blank");
            Scope.parse(checked);
            if (copy.contains(checked)) {
                throw new ValidateException("Gitee scopes must not contain duplicates");
            }
            copy.add(checked);
        }
        if (copy.isEmpty()) {
            copy.add(USER_INFO);
        }
        if (!USER_INFO.equals(copy.getFirst())) {
            throw new ValidateException("Gitee scopes must begin with user_info");
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Validates one credential-free absolute HTTPS callback without changing its lexical value.
     *
     * @param value registered callback URI
     * @throws IllegalArgumentException if the callback text is blank
     * @throws ValidateException        if the callback is not an absolute credential-free fragmentless HTTPS URI
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Gitee redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Gitee redirect URI must be credential-free absolute HTTPS without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Gitee redirect URI is invalid", cause);
        }
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<GiteeOptions> type() {
        return GiteeOptions.class;
    }

    /**
     * Returns a diagnostic representation without app, credential, or callback values.
     *
     * @return redacted options description
     */
    @Override
    public String toString() {
        return "GiteeOptions[vendor=" + vendor + Builder.VARIANT + variant + Builder.REDACTED_SOURCE_OPTIONS + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
