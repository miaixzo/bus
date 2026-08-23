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
package org.miaixz.bus.auth.source.vendor.pinterest;

import java.util.List;

import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorConnector;
import org.miaixz.bus.auth.source.vendor.VendorRegistry;
import org.miaixz.bus.core.lang.Assert;

/**
 * Connects the complete Pinterest platform manifest and its exact variant factories.
 *
 * @author Kimi Liu
 */
public class PinterestConnector implements VendorConnector {

    /**
     * Creates a stateless Pinterest SPI connector.
     */
    public PinterestConnector() {
        // No initialization required.
    }

    /**
     * Returns the stable Pinterest platform key.
     *
     * @return stable Pinterest platform key
     */
    @Override
    public Vendor.Id key() {
        return PinterestManifest.ID;
    }

    /**
     * Binds the Pinterest manifest, Options factory, and all adapter factories as one registration.
     *
     * @param registry active Vendor registry
     */
    @Override
    public void connect(final VendorRegistry registry) {
        Assert.notNull(registry, "Vendor registry must not be null").bind(
                new PinterestManifest(),
                VendorRegistry.options(PinterestOptions::new),
                List.of(
                        VendorRegistry.adapter(
                                PinterestManifest.class,
                                PinterestSourceAdapter::new,
                                PinterestManifest.DEFAULT)));
    }

}
