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

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Carries externally managed Alibaba Cloud OpenID Connect client values.
 * <p>
 * This immutable record keeps only public registration values and an external client-secret reference. Fixed platform
 * endpoints remain in {@link AliyunManifest}; secret material is resolved only for an adapter operation.
 * </p>
 *
 * @param vendor      exact Alibaba Cloud identifier
 * @param variant     exact default variant
 * @param clientId    registered OpenID Connect client identifier
 * @param credential  external client-secret reference
 * @param redirectUri exact registered redirect URI
 * @param scopes      ordered requested scopes, or empty to select the manifest defaults
 * @author Kimi Liu
 */
public record AliyunOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes) implements VendorOptions<AliyunOptions> {

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
     * Validates and freezes one Alibaba Cloud Source registration without resolving its client secret.
     *
     * @throws IllegalArgumentException if a required component, container, or collection member is null or blank
     * @throws ValidateException        if routing, credential type, scope vocabulary, scope uniqueness, or required
     *                                  OpenID Connect scope coverage is invalid
     */
    public AliyunOptions {
        if (!AliyunManifest.ID.equals(vendor) || !AliyunManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("Alibaba Cloud options must select aliyun/default");
        }
        Assert.notBlank(clientId, "Alibaba Cloud client id must not be blank");
        Assert.notNull(credential, "Alibaba Cloud credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Alibaba Cloud credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Alibaba Cloud redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isPresent()) {
            Assert.notBlank(redirectUri.getOrNull(), "Alibaba Cloud redirect URI must not be blank");
        }
        Assert.notNull(scopes, "Alibaba Cloud scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Alibaba Cloud scope must not be blank");
            if (!scope(checked) || copy.contains(checked)) {
                throw new ValidateException("Alibaba Cloud scopes must be unique openid, profile, or aliuid values");
            }
            copy.add(checked);
        }
        if (!copy.isEmpty()
                && (!copy.contains(OpenIdConnect.Scopes.OPENID) || !copy.contains(OpenIdConnect.Scopes.PROFILE))) {
            throw new ValidateException("Explicit Alibaba Cloud scopes must contain openid and profile");
        }
        scopes = List.copyOf(copy);
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
     * Returns a diagnostic representation that never exposes the client identifier, credential reference, or callback
     * target.
     *
     * @return redacted options description
     */
    @Override
    public String toString() {
        return "AliyunOptions[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes
                + Symbol.BRACKET_RIGHT;
    }

}
