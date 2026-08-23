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
package org.miaixz.bus.auth.source.vendor.microsoft;

import java.util.List;

import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorConnector;
import org.miaixz.bus.auth.source.vendor.VendorRegistry;
import org.miaixz.bus.core.lang.Assert;

/**
 * Connects the complete Microsoft platform manifest and its exact variant factories.
 *
 * @author Kimi Liu
 */
public class MicrosoftConnector implements VendorConnector {

    /**
     * Creates a stateless Microsoft SPI connector.
     */
    public MicrosoftConnector() {
        // No initialization required.
    }

    /**
     * Returns the stable Microsoft platform key.
     *
     * @return stable Microsoft platform key
     */
    @Override
    public Vendor.Id key() {
        return MicrosoftManifest.ID;
    }

    /**
     * Binds the Microsoft manifest, Options factory, and all adapter factories as one registration.
     *
     * @param registry active Vendor registry
     */
    @Override
    public void connect(final VendorRegistry registry) {
        Assert.notNull(registry, "Vendor registry must not be null").bind(
                new MicrosoftManifest(),
                (variant, clientId, credential, callback, scopes, parameters) -> new MicrosoftOptions(
                        variant.platform(), variant.variant(), clientId, credential, callback, scopes,
                        MicrosoftManifest.ENTERPRISE_GLOBAL.equals(variant.variant())
                                || MicrosoftManifest.ENTERPRISE_CHINA.equals(variant.variant())
                                        ? VendorRegistry.requiredString(parameters, "tenant")
                                        : VendorRegistry.string(
                                                parameters,
                                                "tenant",
                                                MicrosoftManifest.CHINA.equals(variant.variant()) ? "organizations"
                                                        : "common")),
                List.of(
                        VendorRegistry.adapter(
                                MicrosoftManifest.class,
                                MicrosoftSourceAdapter::new,
                                MicrosoftManifest.GLOBAL,
                                MicrosoftManifest.CHINA),
                        VendorRegistry.adapter(
                                MicrosoftManifest.class,
                                MicrosoftRealmAdapter::new,
                                MicrosoftManifest.ENTERPRISE_GLOBAL,
                                MicrosoftManifest.ENTERPRISE_CHINA)));
    }

}
