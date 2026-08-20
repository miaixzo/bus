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
package org.miaixz.bus.auth.vendor.weibo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed values for one Weibo OAuth client.
 * <p>
 * Fixed endpoints and wire deviations remain manifest-owned. This record contains the six common Vendor components,
 * resolves no secret material, and normalizes an empty scope list to the historical {@code all} default.
 * </p>
 *
 * @param vendor      exact Weibo platform identifier
 * @param variant     exact {@code default} variant
 * @param clientId    Weibo App Key
 * @param credential  external App Secret reference
 * @param redirectUri exact registered browser callback
 * @param scopes      ordered Weibo authorization scopes
 * @author Kimi Liu
 */
public record WeiboOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<WeiboOptions> {

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<WeiboOptions> type() {
        return WeiboOptions.class;
    }

    /**
     * Complete historical Weibo scope vocabulary.
     */
    private static final Set<String> REGISTERED_SCOPES = Set.of(
            "all",
            "email",
            "direct_messages_write",
            "direct_messages_read",
            "invitation_write",
            "friendships_groups_read",
            "friendships_groups_write",
            "statuses_to_me_read",
            "follow_app_official_microblog");

    /**
     * Validates and freezes one Weibo registration without resolving App Secret material.
     *
     * @throws IllegalArgumentException if a required component, collection, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential type, callback, or scope violates the frozen manifest
     */
    public WeiboOptions {
        if (!WeiboManifest.ID.equals(vendor) || !WeiboManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("Weibo options must select weibo/default");
        }
        clientId = Assert.notBlank(clientId, "Weibo client identifier must not be blank");
        Assert.notNull(credential, "Weibo credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Weibo credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Weibo redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        callback(redirectUri);
        Assert.notNull(scopes, "Weibo scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String value : scopes) {
            final String scope = Assert.notBlank(value, "Weibo scope must not be blank");
            if (!REGISTERED_SCOPES.contains(scope) || copy.contains(scope)) {
                throw new ValidateException("Weibo scopes must be unique registered values");
            }
            copy.add(scope);
        }
        scopes = copy.isEmpty() ? List.of("all") : List.copyOf(copy);
    }

    /**
     * Requires one exact credential-free absolute HTTP(S) callback without fragment.
     *
     * @param redirectUri registered callback container
     * @throws ValidateException if the callback is absent or ineligible for browser authorization
     */
    private static void callback(final Optional<String> redirectUri) {
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Weibo options require a registered redirect URI");
        }
        final String value = Assert.notBlank(redirectUri.getOrNull(), "Weibo redirect URI must not be blank");
        try {
            final URI uri = new URI(value);
            final String scheme = uri.getScheme();
            if (!uri.isAbsolute() || scheme == null
                    || !Protocol.HTTPS.name.equalsIgnoreCase(scheme) && !Protocol.HTTP.name.equalsIgnoreCase(scheme)
                    || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Weibo redirect URI must be credential-free absolute HTTP(S) without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Weibo redirect URI is invalid", cause);
        }
    }

    /**
     * Returns a diagnostic representation without App Key, credential-reference, or callback values.
     *
     * @return redacted immutable options summary
     */
    @Override
    public String toString() {
        return "WeiboOptions[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
