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
package org.miaixz.bus.auth.vendor.alipay;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Carries externally managed Alipay public-application and RSA2 key references.
 * <p>
 * This immutable record contains identifiers only. Private and public key material remains in the external key loader
 * and is acquired by the adapter for one gateway operation. The optional callback container follows the common Vendor
 * options shape; the Vendor Source driver requires it to be present for this browser variant.
 * </p>
 *
 * @param vendor            exact Alipay routing identifier
 * @param variant           exact Alipay variant
 * @param clientId          Alipay application identifier
 * @param credential        external private signing-key reference
 * @param redirectUri       exact registered callback URI
 * @param scopes            ordered authorization scopes
 * @param verificationKeyId external Alipay public verification-key identifier
 * @author Kimi Liu
 */
public record AlipayOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId, Credential.Reference credential,
        Optional<String> redirectUri, List<String> scopes, String verificationKeyId)
        implements VendorOptions<AlipayOptions> {

    /**
     * Validates and freezes one Alipay Source registration without resolving key material.
     *
     * @throws IllegalArgumentException if a required value is null or blank
     * @throws ValidateException        if routing or credential type differs from the frozen manifest
     */
    public AlipayOptions {
        if (!AlipayManifest.ID.equals(vendor) || !AlipayManifest.DEFAULT.equals(variant)) {
            throw new ValidateException("Alipay options routing keys must select alipay/default");
        }
        Assert.notBlank(clientId, "Alipay application id must not be blank");
        Assert.notNull(credential, "Alipay signing key reference must not be null");
        if (credential.type() != Credential.Type.PRIVATE_KEY) {
            throw new ValidateException("Alipay credential must reference a private key");
        }
        Assert.notNull(redirectUri, "Alipay redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isPresent())
            Assert.notBlank(redirectUri.getOrNull(), "Alipay redirect URI must not be blank");
        Assert.notNull(scopes, "Alipay scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Alipay scope must not be blank");
            if (!"auth_user".equals(checked) || copy.contains(checked)) {
                throw new ValidateException("Alipay scopes may contain auth_user at most once");
            }
            copy.add(checked);
        }
        scopes = List.copyOf(copy);
        Assert.notBlank(verificationKeyId, "Alipay verification key id must not be blank");
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<AlipayOptions> type() {
        return AlipayOptions.class;
    }

}
