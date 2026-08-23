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
package org.miaixz.bus.auth.source.vendor.alipay;

import java.util.List;

import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorConnector;
import org.miaixz.bus.auth.source.vendor.VendorRegistry;
import org.miaixz.bus.core.lang.Assert;

/**
 * Connects the complete Alipay platform manifest and its exact variant factories.
 *
 * @author Kimi Liu
 */
public class AlipayConnector implements VendorConnector {

    /**
     * Creates a stateless Alipay SPI connector.
     */
    public AlipayConnector() {
        // No initialization required.
    }

    /**
     * Returns the stable Alipay platform key.
     *
     * @return stable Alipay platform key
     */
    @Override
    public Vendor.Id key() {
        return AlipayManifest.ID;
    }

    /**
     * Binds the Alipay manifest, Options factory, and all adapter factories as one registration.
     *
     * @param registry active Vendor registry
     */
    @Override
    public void connect(final VendorRegistry registry) {
        Assert.notNull(registry, "Vendor registry must not be null").bind(
                new AlipayManifest(),
                (variant, clientId, credential, callback, scopes, parameters) -> new AlipayOptions(variant.platform(),
                        variant.variant(), clientId, credential, callback, scopes,
                        VendorRegistry.requiredString(parameters, "verificationKeyId")),
                List.of(
                        VendorRegistry
                                .adapter(AlipayManifest.class, AlipaySourceAdapter::new, AlipayManifest.DEFAULT)));
    }

}
