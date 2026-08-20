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
package org.miaixz.bus.auth.vendor.gitlab;

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
 * Carries externally managed GitLab.com OAuth application values.
 * <p>
 * Fixed GitLab.com endpoints and mandatory S256 PKCE remain definition- and adapter-owned. This record retains only the
 * public Client ID, an external Client Secret reference, the registered callback, and the approved OAuth scopes.
 * </p>
 *
 * @param vendor      exact GitLab platform identifier
 * @param variant     exact default GitLab.com variant
 * @param clientId    registered GitLab OAuth application ID
 * @param credential  external Client Secret reference
 * @param redirectUri exact OAuth application redirect URI
 * @param scopes      ordered GitLab OAuth scopes whose first value is {@code read_user}
 * @author Kimi Liu
 */
public record GitLabSourceSettings(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes) implements VendorSettings {

    /**
     * Stable identifier of the sole GitLab.com OAuth variant.
     */
    /**
     * Validates and freezes one GitLab registration without resolving its Client Secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, or scopes differ from the frozen definition
     */
    public GitLabSourceSettings {
        if (!GitLabDefinition.ID.equals(vendor) || !GitLabDefinition.DEFAULT.equals(variant)) {
            throw new ValidateException("GitLab settings must select gitlab/default");
        }
        Assert.notBlank(clientId, "GitLab client id must not be blank");
        Assert.notNull(credential, "GitLab credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("GitLab credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "GitLab redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("GitLab settings require an OAuth application redirect URI");
        }
        redirect(redirectUri.getOrNull());

        Assert.notNull(scopes, "GitLab scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "GitLab scope must not be blank");
            Scope.parse(checked);
            if (!"read_user".equals(checked) || copy.contains(checked)) {
                throw new ValidateException("GitLab scope is unsupported or duplicated by the frozen login definition");
            }
            copy.add(checked);
        }
        if (copy.isEmpty()) {
            copy.add("read_user");
        }
        if (!"read_user".equals(copy.getFirst())) {
            throw new ValidateException("GitLab scopes must begin with read_user");
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
        final String checked = Assert.notBlank(value, "GitLab redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "GitLab redirect URI must be credential-free absolute HTTPS without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("GitLab redirect URI is invalid", cause);
        }
    }

    /**
     * Returns a diagnostic representation without app, credential, or callback values.
     *
     * @return redacted settings description
     */
    @Override
    public String toString() {
        return "GitLabSourceSettings[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
