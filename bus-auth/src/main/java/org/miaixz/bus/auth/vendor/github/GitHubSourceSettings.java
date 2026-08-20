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
package org.miaixz.bus.auth.vendor.github;

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
 * Carries externally managed GitHub.com OAuth App values.
 * <p>
 * Fixed GitHub.com endpoints, S256 PKCE, and REST versioning remain definition- and adapter-owned. This record retains
 * only the public Client ID, an external Client Secret reference, the registered callback, and the approved OAuth
 * scopes.
 * </p>
 *
 * @param vendor      exact GitHub platform identifier
 * @param variant     exact default GitHub.com variant
 * @param clientId    registered GitHub OAuth App Client ID
 * @param credential  external Client Secret reference
 * @param redirectUri exact Authorization callback URL
 * @param scopes      ordered GitHub OAuth scopes whose first value is {@code read:user}
 * @author Kimi Liu
 */
public record GitHubSourceSettings(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes) implements VendorSettings {

    /**
     * Stable identifier of the sole GitHub.com OAuth variant.
     */
    /**
     * Validates and freezes one GitHub registration without resolving its Client Secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, or scopes differ from the frozen definition
     */
    public GitHubSourceSettings {
        if (!GitHubDefinition.ID.equals(vendor) || !GitHubDefinition.DEFAULT.equals(variant)) {
            throw new ValidateException("GitHub settings must select github/default");
        }
        Assert.notBlank(clientId, "GitHub client id must not be blank");
        Assert.notNull(credential, "GitHub credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("GitHub credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "GitHub redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("GitHub settings require an Authorization callback URL");
        }
        redirect(redirectUri.getOrNull());

        Assert.notNull(scopes, "GitHub scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "GitHub scope must not be blank");
            Scope.parse(checked);
            if (!approvedScope(checked) || copy.contains(checked)) {
                throw new ValidateException("GitHub scope is unsupported or duplicated by the frozen login definition");
            }
            copy.add(checked);
        }
        if (copy.isEmpty()) {
            copy.add("read:user");
        }
        if (!"read:user".equals(copy.getFirst())) {
            throw new ValidateException("GitHub scopes must begin with read:user");
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Tests one scope against the GitHub identity and refresh permissions accepted by this Source.
     *
     * @param value exact scope value
     * @return whether the scope is supported
     */
    private static boolean approvedScope(final String value) {
        return "read:user".equals(value) || "user:email".equals(value) || "offline_access".equals(value);
    }

    /**
     * Validates one credential-free absolute HTTPS callback without changing its lexical value.
     *
     * @param value registered callback URI
     * @throws IllegalArgumentException if the callback text is blank
     * @throws ValidateException        if the callback is not an absolute credential-free fragmentless HTTPS URI
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "GitHub redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "GitHub redirect URI must be credential-free absolute HTTPS without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("GitHub redirect URI is invalid", cause);
        }
    }

    /**
     * Returns a diagnostic representation without app, credential, or callback values.
     *
     * @return redacted settings description
     */
    @Override
    public String toString() {
        return "GitHubSourceSettings[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
