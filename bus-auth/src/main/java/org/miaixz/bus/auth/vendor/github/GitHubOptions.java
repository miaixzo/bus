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
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Carries externally managed GitHub.com OAuth App or Enterprise management values.
 * <p>
 * Fixed GitHub.com endpoints, S256 PKCE, and REST versioning remain manifest- and adapter-owned. This record retains
 * only the public Client ID, an external Client Secret reference, the registered callback, and the approved OAuth
 * scopes. The Enterprise branch instead stores a non-secret enterprise slug and a reference to a separately managed
 * read-only administrator token.
 * </p>
 *
 * @param vendor      exact GitHub platform identifier
 * @param variant     exact default login or Enterprise management Variant
 * @param clientId    OAuth Client ID or Enterprise slug
 * @param credential  external Client Secret or administrator Token reference
 * @param redirectUri exact Authorization callback or empty for Enterprise management
 * @param scopes      ordered OAuth scopes or empty for Enterprise management
 * @author Kimi Liu
 */
public record GitHubOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<GitHubOptions> {

    /**
     * Exact conservative GitHub Enterprise slug grammar used by path-template resolution.
     */
    private static final String ENTERPRISE_SLUG_PATTERN = "[A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?";

    /**
     * Validates and freezes one GitHub registration without resolving its external credential.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, or scopes differ from the frozen manifest
     */
    public GitHubOptions {
        if (!GitHubManifest.ID.equals(vendor)
                || !(GitHubManifest.DEFAULT.equals(variant) || GitHubManifest.ENTERPRISE.equals(variant))) {
            throw new ValidateException("GitHub options must select github/default or github/enterprise");
        }
        clientId = Assert.notBlank(
                clientId,
                GitHubManifest.ENTERPRISE.equals(variant) ? "GitHub Enterprise slug must not be blank"
                        : "GitHub client id must not be blank");
        Assert.notNull(credential, "GitHub credential reference must not be null");
        Assert.notNull(redirectUri, "GitHub redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        Assert.notNull(scopes, "GitHub scopes must not be null");
        if (GitHubManifest.ENTERPRISE.equals(variant)) {
            if (!clientId.equals(StringKit.trim(clientId)) || !PatternKit.isMatch(ENTERPRISE_SLUG_PATTERN, clientId)) {
                throw new ValidateException(
                        "GitHub Enterprise slug must use one to 39 letters, digits, or inner hyphens");
            }
            if (credential.type() != Credential.Type.SHARED_SECRET) {
                throw new ValidateException("GitHub Enterprise credential must reference an administrator token");
            }
            if (redirectUri.isPresent() || !scopes.isEmpty()) {
                throw new ValidateException("GitHub Enterprise options must not contain login callback or scopes");
            }
            scopes = List.of();
        } else {
            if (credential.type() != Credential.Type.CLIENT_SECRET) {
                throw new ValidateException("GitHub login credential must reference a client secret");
            }
            if (redirectUri.isEmpty()) {
                throw new ValidateException("GitHub options require an Authorization callback URL");
            }
            redirect(redirectUri.getOrNull());
            final List<String> copy = new ArrayList<>(scopes.size());
            for (String scope : scopes) {
                final String checked = Assert.notBlank(scope, "GitHub scope must not be blank");
                Scope.parse(checked);
                if (!approvedScope(checked) || copy.contains(checked)) {
                    throw new ValidateException(
                            "GitHub scope is unsupported or duplicated by the frozen login manifest");
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
     * Returns the Enterprise slug only for constrained management target templates.
     *
     * @return Enterprise slug or empty for browser login
     */
    @Override
    public Optional<String> templateTenant() {
        return GitHubManifest.ENTERPRISE.equals(variant) ? Optional.of(clientId) : Optional.empty();
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<GitHubOptions> type() {
        return GitHubOptions.class;
    }

    /**
     * Returns a diagnostic representation without app, credential, or callback values.
     *
     * @return redacted options description
     */
    @Override
    public String toString() {
        return "GitHubOptions[vendor=" + vendor + Builder.VARIANT + variant + Builder.REDACTED_SOURCE_OPTIONS + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
