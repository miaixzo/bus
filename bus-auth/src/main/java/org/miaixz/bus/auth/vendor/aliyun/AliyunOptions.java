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
package org.miaixz.bus.auth.vendor.aliyun;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Carries externally managed Alibaba Cloud OpenID Connect or RAM access values.
 * <p>
 * This immutable record keeps only public registration values and an external client-secret reference. Fixed platform
 * endpoints remain in {@link AliyunManifest}; secret material is resolved only for an adapter operation. The RAM branch
 * stores an AccessKey ID and an external AccessKey Secret reference without OAuth callback or scopes.
 * </p>
 *
 * @param vendor      exact Alibaba Cloud identifier
 * @param variant     exact default login or RAM Variant
 * @param clientId    OpenID Connect client identifier or RAM AccessKey ID
 * @param credential  external client-secret or AccessKey Secret reference
 * @param redirectUri login redirect URI or empty for RAM
 * @param scopes      ordered login scopes or empty for RAM
 * @author Kimi Liu
 */
public record AliyunOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<AliyunOptions> {

    /**
     * Conservative lexical grammar for Alibaba Cloud AccessKey identifiers.
     */
    private static final String ACCESS_KEY_ID_PATTERN = "[A-Za-z0-9]{8,128}";

    /**
     * Validates and freezes one Alibaba Cloud Source registration without resolving its external secret.
     *
     * @throws IllegalArgumentException if a required component, container, or collection member is null or blank
     * @throws ValidateException        if routing, credential type, scope vocabulary, scope uniqueness, or required
     *                                  OpenID Connect scope coverage is invalid
     */
    public AliyunOptions {
        if (!AliyunManifest.ID.equals(vendor)
                || !(AliyunManifest.DEFAULT.equals(variant) || AliyunManifest.RAM.equals(variant))) {
            throw new ValidateException("Alibaba Cloud options must select aliyun/default or aliyun/ram");
        }
        clientId = Assert.notBlank(
                clientId,
                AliyunManifest.RAM.equals(variant) ? "Alibaba Cloud AccessKey ID must not be blank"
                        : "Alibaba Cloud client id must not be blank");
        Assert.notNull(credential, "Alibaba Cloud credential reference must not be null");
        Assert.notNull(redirectUri, "Alibaba Cloud redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        Assert.notNull(scopes, "Alibaba Cloud scopes must not be null");
        if (AliyunManifest.RAM.equals(variant)) {
            if (!clientId.equals(StringKit.trim(clientId)) || !PatternKit.isMatch(ACCESS_KEY_ID_PATTERN, clientId)) {
                throw new ValidateException("Alibaba Cloud AccessKey ID has an invalid lexical form");
            }
            if (credential.type() != Credential.Type.SHARED_SECRET) {
                throw new ValidateException("Alibaba Cloud RAM credential must reference an AccessKey Secret");
            }
            if (redirectUri.isPresent() || !scopes.isEmpty()) {
                throw new ValidateException("Alibaba Cloud RAM options must not contain login callback or scopes");
            }
            scopes = List.of();
        } else {
            if (credential.type() != Credential.Type.CLIENT_SECRET) {
                throw new ValidateException("Alibaba Cloud login credential must reference a client secret");
            }
            if (redirectUri.isPresent()) {
                Assert.notBlank(redirectUri.getOrNull(), "Alibaba Cloud redirect URI must not be blank");
            }
            final List<String> copy = new ArrayList<>(scopes.size());
            for (String scope : scopes) {
                final String checked = Assert.notBlank(scope, "Alibaba Cloud scope must not be blank");
                if (!scope(checked) || copy.contains(checked)) {
                    throw new ValidateException(
                            "Alibaba Cloud scopes must be unique openid, profile, or aliuid values");
                }
                copy.add(checked);
            }
            if (!copy.isEmpty()
                    && (!copy.contains(OpenIdConnect.Scopes.OPENID) || !copy.contains(OpenIdConnect.Scopes.PROFILE))) {
                throw new ValidateException("Explicit Alibaba Cloud scopes must contain openid and profile");
            }
            scopes = List.copyOf(copy);
        }
    }

    /**
     * Tests one standard or Alibaba Cloud registered scope without maintaining a parallel field set.
     *
     * @param value scope value
     * @return whether the value is accepted
     */
    private static boolean scope(final String value) {
        return switch (value) {
            case OpenIdConnect.Scopes.OPENID, OpenIdConnect.Scopes.PROFILE, "aliuid" -> true;
            default -> false;
        };
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<AliyunOptions> type() {
        return AliyunOptions.class;
    }

    /**
     * Returns a diagnostic representation that never exposes the client identifier, credential reference, or callback
     * target.
     *
     * @return redacted options description
     */
    @Override
    public String toString() {
        return "AliyunOptions[vendor=" + vendor + Builder.VARIANT + variant + Builder.REDACTED_SOURCE_OPTIONS + scopes
                + Symbol.BRACKET_RIGHT;
    }

}
