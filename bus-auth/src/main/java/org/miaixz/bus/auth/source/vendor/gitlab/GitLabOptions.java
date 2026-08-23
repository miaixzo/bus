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
package org.miaixz.bus.auth.source.vendor.gitlab;

import java.net.IDN;
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
import org.miaixz.bus.core.lang.Regex;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Carries externally managed GitLab.com OAuth application or Enterprise REST values.
 * <p>
 * Fixed GitLab.com endpoints and mandatory S256 PKCE remain manifest- and adapter-owned. This record retains only the
 * public Client ID, an external Client Secret reference, the registered callback, and the approved OAuth scopes. The
 * Enterprise branch appends a constrained deployment host and top-level group while keeping its read-only Token in an
 * external Secret reference.
 * </p>
 *
 * @param vendor        exact GitLab platform identifier
 * @param variant       exact default login or Enterprise REST Variant
 * @param clientId      OAuth application ID or empty for Enterprise management
 * @param credential    external Client Secret or read-only Token reference
 * @param redirectUri   exact OAuth redirect or empty for Enterprise management
 * @param scopes        ordered OAuth scopes or empty for Enterprise management
 * @param instance      empty for login or constrained GitLab deployment DNS host
 * @param topLevelGroup empty for login or stable top-level group slug
 * @author Kimi Liu
 */
public record GitLabOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes, String instance, String topLevelGroup)
        implements VendorOptions<GitLabOptions> {

    /**
     * Exact conservative GitLab top-level group slug grammar.
     */
    private static final String GROUP_PATTERN = "[A-Za-z0-9](?:[A-Za-z0-9_.-]{0,253}[A-Za-z0-9])?";

    /**
     * Validates and freezes one GitLab registration without resolving its external credential.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, or scopes differ from the frozen manifest
     */
    public GitLabOptions {
        if (!GitLabManifest.ID.equals(vendor)
                || !(GitLabManifest.DEFAULT.equals(variant) || GitLabManifest.ENTERPRISE.equals(variant))) {
            throw new ValidateException("GitLab options must select gitlab/default or gitlab/enterprise");
        }
        Assert.notNull(clientId, "GitLab client id must not be null");
        Assert.notNull(credential, "GitLab credential reference must not be null");
        Assert.notNull(redirectUri, "GitLab redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        Assert.notNull(scopes, "GitLab scopes must not be null");
        Assert.notNull(instance, "GitLab instance must not be null");
        Assert.notNull(topLevelGroup, "GitLab top-level group must not be null");
        if (GitLabManifest.ENTERPRISE.equals(variant)) {
            if (!clientId.isEmpty()) {
                throw new ValidateException("GitLab Enterprise options must not contain an OAuth client id");
            }
            instance = instance(instance);
            topLevelGroup = group(topLevelGroup);
            if (credential.type() != Credential.Type.SHARED_SECRET) {
                throw new ValidateException("GitLab Enterprise credential must reference a read-only token");
            }
            if (redirectUri.isPresent() || !scopes.isEmpty()) {
                throw new ValidateException("GitLab Enterprise options must not contain login callback or scopes");
            }
            scopes = List.of();
        } else {
            Assert.notBlank(clientId, "GitLab client id must not be blank");
            if (!instance.isEmpty() || !topLevelGroup.isEmpty()) {
                throw new ValidateException("GitLab login options must not contain Enterprise selectors");
            }
            if (credential.type() != Credential.Type.CLIENT_SECRET) {
                throw new ValidateException("GitLab login credential must reference a client secret");
            }
            if (redirectUri.isEmpty()) {
                throw new ValidateException("GitLab options require an OAuth application redirect URI");
            }
            redirect(redirectUri.getOrNull());
            final List<String> copy = new ArrayList<>(scopes.size());
            for (String scope : scopes) {
                final String checked = Assert.notBlank(scope, "GitLab scope must not be blank");
                Scope.parse(checked);
                if (!"read_user".equals(checked) || copy.contains(checked)) {
                    throw new ValidateException(
                            "GitLab scope is unsupported or duplicated by the frozen login manifest");
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
    }

    /**
     * Validates a credential-free canonical DNS deployment host.
     *
     * @param value externally supplied host
     * @return original validated host
     */
    private static String instance(final String value) {
        final String checked = Assert.notBlank(value, "GitLab instance must not be blank");
        if (!checked.equals(StringKit.trim(checked))) {
            throw new ValidateException("GitLab instance must not contain surrounding whitespace");
        }
        final String ascii;
        try {
            ascii = IDN.toASCII(checked);
        } catch (IllegalArgumentException cause) {
            throw new ValidateException("GitLab instance is not a valid DNS host", cause);
        }
        if (ascii.length() > 253 || ascii.endsWith(Symbol.DOT) || PatternKit.isMatch(Regex.IP_ADDRESS, ascii)) {
            throw new ValidateException("GitLab instance must be a canonical DNS host rather than an IP address");
        }
        for (String label : ascii.split("\\.", -1)) {
            if (label.isEmpty() || label.length() > 63 || label.startsWith(Symbol.MINUS) || label.endsWith(Symbol.MINUS)
                    || !label.chars().allMatch(
                            character -> Character.isLetterOrDigit(character) || character == Symbol.C_MINUS)) {
                throw new ValidateException("GitLab instance contains an invalid DNS label");
            }
        }
        return checked;
    }

    /**
     * Validates one stable top-level group slug without trimming it.
     *
     * @param value externally supplied group slug
     * @return original validated slug
     */
    private static String group(final String value) {
        final String checked = Assert.notBlank(value, "GitLab top-level group must not be blank");
        if (!checked.equals(StringKit.trim(checked)) || !PatternKit.isMatch(GROUP_PATTERN, checked)) {
            throw new ValidateException("GitLab top-level group must be one safe path segment");
        }
        return checked;
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
     * Returns the constrained GitLab deployment only for Enterprise target templates.
     *
     * @return deployment DNS host or empty for login
     */
    @Override
    public Optional<String> templateInstance() {
        return GitLabManifest.ENTERPRISE.equals(variant) ? Optional.of(instance) : Optional.empty();
    }

    /**
     * Returns the top-level group slug only for Enterprise target templates.
     *
     * @return group slug or empty for login
     */
    @Override
    public Optional<String> templateTenant() {
        return GitLabManifest.ENTERPRISE.equals(variant) ? Optional.of(topLevelGroup) : Optional.empty();
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<GitLabOptions> type() {
        return GitLabOptions.class;
    }

    /**
     * Returns a diagnostic representation without app, credential, or callback values.
     *
     * @return redacted options description
     */
    @Override
    public String toString() {
        return "GitLabOptions[vendor=" + vendor + Builder.VARIANT + variant + Builder.REDACTED_SOURCE_OPTIONS + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
