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
package org.miaixz.bus.auth.vendor.douyin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorSettings;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed Douyin client values for the open and ordinary mini-program variants.
 * <p>
 * The open variant requires an exact HTTPS callback and explicit scope set containing {@code user_info}. The
 * mini-program variant prohibits callback and scope data because its one-time code arrives only at runtime.
 * </p>
 *
 * @param vendor      exact Douyin platform identifier
 * @param variant     exact {@code open} or {@code mini-program} variant
 * @param clientId    registered open-platform client key or mini-program appid
 * @param credential  external client-secret or App Secret reference
 * @param redirectUri exact registered open-platform callback, empty for mini-program
 * @param scopes      ordered open-platform scopes, empty for mini-program
 * @author Kimi Liu
 */
public record DouyinSourceSettings(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes) implements VendorSettings {

    /**
     * Validates and freezes one Douyin registration without resolving secret material.
     *
     * @throws IllegalArgumentException if a required component or collection member is {@code null} or blank
     * @throws ValidateException        if routing, credential, redirect URI, or scope data violate the selected variant
     */
    public DouyinSourceSettings {
        if (!DouyinDefinition.ID.equals(vendor)
                || !DouyinDefinition.OPEN.equals(variant) && !DouyinDefinition.MINI_PROGRAM.equals(variant)) {
            throw new ValidateException("Douyin settings must select douyin/open or douyin/mini-program");
        }
        Assert.notBlank(clientId, "Douyin client id must not be blank");
        Assert.notNull(credential, "Douyin credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Douyin credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Douyin redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        Assert.notNull(scopes, "Douyin scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Douyin scope must not be blank");
            if (!scope(checked) || copy.contains(checked)) {
                throw new ValidateException("Douyin scopes must be unique registered values");
            }
            copy.add(checked);
        }
        scopes = List.copyOf(copy);
        if (DouyinDefinition.OPEN.equals(variant)) {
            open(redirectUri, scopes);
        } else if (redirectUri.isPresent() || !scopes.isEmpty()) {
            throw new ValidateException("Douyin mini-program settings prohibit redirect URI and scopes");
        }
    }

    /**
     * Tests one value against the historical Douyin open-platform scope vocabulary.
     *
     * @param value scope value
     * @return whether Douyin accepts the scope
     */
    private static boolean scope(final String value) {
        return switch (value) {
            case "user_info", "aweme.share", "im.share", "renew_refresh_token", "following.list", "fans.list", "video.create", "video.delete", "video.data", "video.list", "share_with_source", "mobile", "mobile_alert", "video.search", "poi.search", "login_id", "data.external.user", "data.external.item", "fans.data", "hotsearch", "star_top_score_display", "star_tops", "star_author_score_display", "data.external.sdk_share", "discovery.ent" -> true;
            default -> false;
        };
    }

    /**
     * Validates the open-platform callback and mandatory identity scope.
     *
     * @param redirectUri registered callback container
     * @param scopes      immutable explicit scope set
     */
    private static void open(final Optional<String> redirectUri, final List<String> scopes) {
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Douyin open settings require a registered redirect URI");
        }
        final String value = Assert.notBlank(redirectUri.getOrNull(), "Douyin redirect URI must not be blank");
        try {
            final URI uri = new URI(value);
            if (!Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Douyin open redirect URI must be credential-free HTTPS without query or fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Douyin open redirect URI is invalid", cause);
        }
        if (scopes.isEmpty() || !scopes.contains("user_info")) {
            throw new ValidateException("Douyin open scopes must explicitly contain user_info");
        }
    }

    /**
     * Returns a diagnostic description without client, credential, or callback values.
     *
     * @return redacted settings description
     */
    @Override
    public String toString() {
        return "DouyinSourceSettings[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + Symbol.C_BRACKET_RIGHT;
    }

}
