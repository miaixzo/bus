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
package org.miaixz.bus.auth.source.vendor.feishu;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed Feishu OAuth or enterprise Contact API client values.
 * <p>
 * Fixed endpoints and wire behavior remain manifest-owned. The default browser Variant uses a registered callback and
 * optional scopes, while the enterprise Variant uses only an App ID and an external App Secret reference.
 * </p>
 *
 * @param vendor      exact Feishu platform identifier
 * @param variant     exact default or enterprise Feishu variant
 * @param clientId    registered Feishu App ID
 * @param credential  external App Secret reference
 * @param redirectUri exact registered callback for the default Variant, empty for enterprise
 * @param scopes      ordered requested login scopes, empty for enterprise
 * @author Kimi Liu
 */
public record FeishuOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<FeishuOptions> {

    /**
     * Validates and freezes one Feishu registration without resolving its App Secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, credential, callback, or scope differs from the selected manifest
     */
    public FeishuOptions {
        if (!FeishuManifest.ID.equals(vendor)
                || (!FeishuManifest.DEFAULT.equals(variant) && !FeishuManifest.ENTERPRISE.equals(variant))) {
            throw new ValidateException("Feishu options must select a supported Feishu variant");
        }
        Assert.notBlank(clientId, "Feishu client id must not be blank");
        Assert.notNull(credential, "Feishu credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Feishu credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Feishu redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        Assert.notNull(scopes, "Feishu scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Feishu scope must not be blank");
            if (copy.contains(checked)) {
                throw new ValidateException("Feishu scopes must be unique");
            }
            copy.add(checked);
        }
        scopes = List.copyOf(copy);
        if (FeishuManifest.ENTERPRISE.equals(variant)) {
            if (redirectUri.isPresent() || !scopes.isEmpty()) {
                throw new ValidateException("Feishu enterprise options prohibit redirect URI and scopes");
            }
        } else {
            if (redirectUri.isEmpty()) {
                throw new ValidateException("Feishu default options require a registered redirect URI");
            }
            redirect(redirectUri.getOrNull());
        }
    }

    /**
     * Validates one credential-free absolute HTTPS callback without changing its lexical value.
     *
     * @param value registered callback URI
     * @throws IllegalArgumentException if the callback text is blank
     * @throws ValidateException        if the callback is not an absolute credential-free fragmentless HTTPS URI
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Feishu redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            if (!Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Feishu redirect URI must be credential-free absolute HTTPS without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Feishu redirect URI is invalid", cause);
        }
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<FeishuOptions> type() {
        return FeishuOptions.class;
    }

    /**
     * Returns a diagnostic representation without app, credential, callback, or scope values.
     *
     * @return redacted options description
     */
    @Override
    public String toString() {
        return "FeishuOptions[vendor=" + vendor + Builder.VARIANT + variant + Builder.REDACTED_SOURCE_OPTIONS
                + Builder.REDACTED_VALUE + Symbol.C_BRACKET_RIGHT;
    }

}
