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
package org.miaixz.bus.auth.vendor.qq;

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
 * Carries externally managed QQ Open Platform or Mini Program client values.
 * <p>
 * The open variant requires a registered callback and accepts the historical QQ scope vocabulary. The mini-program
 * variant prohibits callback, scope, and UnionID preference because its direct identity remains keyed by OpenID.
 * </p>
 *
 * @param vendor        exact QQ platform identifier
 * @param variant       exact {@code open} or {@code mini-program} variant
 * @param clientId      registered App ID
 * @param credential    external App Key or App Secret reference
 * @param redirectUri   exact registered open-platform callback, empty for mini-program
 * @param scopes        ordered open-platform scopes, empty for mini-program
 * @param preferUnionId whether open-platform identity prefers a returned UnionID over OpenID
 * @author Kimi Liu
 */
public record QqSourceSettings(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes, boolean preferUnionId)
        implements VendorSettings {

    /**
     * Validates and freezes one QQ registration without resolving secret material.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, scope, or variant-only fields are invalid
     */
    public QqSourceSettings {
        if (!QqDefinition.ID.equals(vendor)
                || !QqDefinition.OPEN.equals(variant) && !QqDefinition.MINI_PROGRAM.equals(variant)) {
            throw new ValidateException("QQ settings must select qq/open or qq/mini-program");
        }
        clientId = Assert.notBlank(clientId, "QQ client identifier must not be blank");
        Assert.notNull(credential, "QQ credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("QQ credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "QQ redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        Assert.notNull(scopes, "QQ scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "QQ scope must not be blank");
            if (!openScope(checked) || copy.contains(checked)) {
                throw new ValidateException("QQ scopes must be unique registered QQ values");
            }
            copy.add(checked);
        }
        scopes = List.copyOf(copy);
        if (QqDefinition.OPEN.equals(variant)) {
            open(redirectUri, scopes);
        } else if (redirectUri.isPresent() || !scopes.isEmpty() || preferUnionId) {
            throw new ValidateException("QQ mini-program settings prohibit callback, scopes, and UnionID preference");
        }
    }

    /**
     * Determines whether one requested value belongs to QQ Open Platform's preserved scope vocabulary.
     *
     * @param value validated non-blank scope value
     * @return {@code true} when QQ Open Platform registers the scope
     */
    private static boolean openScope(final String value) {
        return switch (value) {
            case "get_user_info", "get_vip_info", "get_vip_rich_info", "list_album", "upload_pic", "add_album", "list_photo" -> true;
            default -> false;
        };
    }

    /**
     * Validates open-platform callback and identity scope coverage.
     *
     * @param redirectUri registered callback container
     * @param scopes      immutable explicit scope values, possibly empty for definition defaults
     * @throws ValidateException if callback or explicit scope coverage is invalid
     */
    private static void open(final Optional<String> redirectUri, final List<String> scopes) {
        if (redirectUri.isEmpty()) {
            throw new ValidateException("QQ open settings require a registered redirect URI");
        }
        final String value = Assert.notBlank(redirectUri.getOrNull(), "QQ redirect URI must not be blank");
        try {
            final URI uri = new URI(value);
            final String scheme = uri.getScheme();
            if (!uri.isAbsolute() || scheme == null
                    || !Protocol.HTTPS.name.equalsIgnoreCase(scheme) && !Protocol.HTTP.name.equalsIgnoreCase(scheme)
                    || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "QQ redirect URI must be credential-free absolute HTTP(S) without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("QQ redirect URI is invalid", cause);
        }
        if (!scopes.isEmpty() && !scopes.contains("get_user_info")) {
            throw new ValidateException("Explicit QQ open scopes must contain get_user_info");
        }
    }

    /**
     * Returns a diagnostic representation without client, credential-reference, or callback values.
     *
     * @return redacted immutable settings summary
     */
    @Override
    public String toString() {
        return "QqSourceSettings[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + ", preferUnionId=" + preferUnionId + Symbol.C_BRACKET_RIGHT;
    }

}
