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
package org.miaixz.bus.auth.vendor.twitter;

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
 * Carries externally managed Twitter.com OAuth App values.
 * <p>
 * Fixed Twitter.com endpoints, S256 PKCE, and REST versioning remain manifest- and adapter-owned. This record retains
 * only the public Client ID, an external Client Secret reference, the registered callback, and the approved OAuth
 * scopes.
 * </p>
 *
 * @param vendor      exact Twitter platform identifier
 * @param variant     exact default Twitter.com variant
 * @param clientId    registered Twitter OAuth App Client ID
 * @param credential  external Client Secret reference
 * @param redirectUri exact Authorization callback URL
 * @param scopes      ordered X OAuth scopes beginning with {@code tweet.read} and {@code users.read}
 * @author Kimi Liu
 */
public record TwitterOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<TwitterOptions> {

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<TwitterOptions> type() {
        return TwitterOptions.class;
    }

    /**
     * Validates and freezes one Twitter registration without resolving its Client Secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, or scopes differ from the frozen manifest
     */
    public TwitterOptions {
        if (!TwitterManifest.ID.equals(vendor) || !TwitterManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("Twitter options must select twitter/default");
        }
        Assert.notBlank(clientId, "Twitter client id must not be blank");
        Assert.notNull(credential, "Twitter credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Twitter credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Twitter redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Twitter options require an Authorization callback URL");
        }
        redirect(redirectUri.getOrNull());

        Assert.notNull(scopes, "Twitter scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Twitter scope must not be blank");
            Scope.parse(checked);
            if (!approvedScope(checked) || copy.contains(checked)) {
                throw new ValidateException("Twitter scope is unsupported or duplicated by the frozen login manifest");
            }
            copy.add(checked);
        }
        if (copy.isEmpty()) {
            copy.add("tweet.read");
            copy.add("users.read");
        }
        if (copy.size() < 2 || !"tweet.read".equals(copy.get(0)) || !"users.read".equals(copy.get(1))) {
            throw new ValidateException("Twitter scopes must begin with tweet.read and users.read");
        }
        scopes = List.copyOf(copy);
    }

    /**
     * Tests one scope against the Twitter identity and refresh permissions accepted by this Source.
     *
     * @param value exact scope value
     * @return whether the scope is supported
     */
    private static boolean approvedScope(final String value) {
        return "tweet.read".equals(value) || "users.read".equals(value) || "offline.access".equals(value);
    }

    /**
     * Validates one credential-free absolute HTTPS callback without changing its lexical value.
     *
     * @param value registered callback URI
     * @throws IllegalArgumentException if the callback text is blank
     * @throws ValidateException        if the callback is not an absolute credential-free fragmentless HTTPS URI
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Twitter redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Twitter redirect URI must be credential-free absolute HTTPS without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Twitter redirect URI is invalid", cause);
        }
    }

    /**
     * Returns a diagnostic representation without app, credential, or callback values.
     *
     * @return redacted options description
     */
    @Override
    public String toString() {
        return "TwitterOptions[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
