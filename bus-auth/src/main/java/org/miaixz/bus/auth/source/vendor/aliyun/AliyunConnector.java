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
package org.miaixz.bus.auth.source.vendor.aliyun;

import java.util.List;

import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorConnector;
import org.miaixz.bus.auth.source.vendor.VendorRegistry;
import org.miaixz.bus.core.lang.Assert;

/**
 * Connects the complete Aliyun platform manifest and its exact variant factories.
 *
 * @author Kimi Liu
 */
public class AliyunConnector implements VendorConnector {

    /**
     * Creates a stateless Aliyun SPI connector.
     */
    public AliyunConnector() {
        // No initialization required.
    }

    /**
     * Returns the stable Aliyun platform key.
     *
     * @return stable Aliyun platform key
     */
    @Override
    public Vendor.Id key() {
        return AliyunManifest.ID;
    }

    /**
     * Binds the Aliyun manifest, Options factory, and all adapter factories as one registration.
     *
     * @param registry active Vendor registry
     */
    @Override
    public void connect(final VendorRegistry registry) {
        Assert.notNull(registry, "Vendor registry must not be null").bind(
                new AliyunManifest(),
                VendorRegistry.options(AliyunOptions::new),
                List.of(
                        VendorRegistry.adapter(AliyunManifest.class, AliyunSourceAdapter::new, AliyunManifest.DEFAULT),
                        VendorRegistry.adapter(AliyunManifest.class, AliyunRealmAdapter::new, AliyunManifest.RAM)));
    }

}
