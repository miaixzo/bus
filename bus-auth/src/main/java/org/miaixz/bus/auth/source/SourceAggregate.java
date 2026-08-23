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
package org.miaixz.bus.auth.source;

import java.util.List;

import org.miaixz.bus.auth.source.protocol.ProtocolModule;
import org.miaixz.bus.auth.source.vendor.VendorModule;
import org.miaixz.bus.core.lang.Assert;

/**
 * Holds the immutable protocol and Vendor module aggregate produced by one complete Source assembly.
 *
 * @param protocolModule immutable protocol module
 * @param vendorModule   immutable Vendor module
 * @author Kimi Liu
 */
public record SourceAggregate(ProtocolModule protocolModule, VendorModule vendorModule) {

    /**
     * Validates one complete Source aggregate.
     */
    public SourceAggregate {
        protocolModule = Assert.notNull(protocolModule, "Protocol module must not be null");
        vendorModule = Assert.notNull(vendorModule, "Vendor module must not be null");
    }

    /**
     * Returns the aggregate modules in deterministic runtime assembly order.
     *
     * @return protocol module followed by Vendor module
     */
    public List<SourceModule> modules() {
        return List.of(protocolModule, vendorModule);
    }

}
