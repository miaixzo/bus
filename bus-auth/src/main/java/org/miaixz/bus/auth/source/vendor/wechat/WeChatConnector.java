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
package org.miaixz.bus.auth.source.vendor.wechat;

import java.util.List;

import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorConnector;
import org.miaixz.bus.auth.source.vendor.VendorRegistry;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;

/**
 * Connects the complete WeChat platform manifest and its exact variant factories.
 *
 * @author Kimi Liu
 */
public class WeChatConnector implements VendorConnector {

    /**
     * Creates a stateless WeChat SPI connector.
     */
    public WeChatConnector() {
        // No initialization required.
    }

    /**
     * Returns the stable WeChat platform key.
     *
     * @return stable WeChat platform key
     */
    @Override
    public Vendor.Id key() {
        return WeChatManifest.ID;
    }

    /**
     * Binds the WeChat manifest, Options factory, and all adapter factories as one registration.
     *
     * @param registry active Vendor registry
     */
    @Override
    public void connect(final VendorRegistry registry) {
        Assert.notNull(registry, "Vendor registry must not be null").bind(
                new WeChatManifest(),
                (variant, clientId, credential, callback, scopes, parameters) -> new WeChatOptions(variant.platform(),
                        variant.variant(), clientId, credential, callback, scopes,
                        VendorRegistry.string(parameters, "loginType", Normal.EMPTY),
                        VendorRegistry.string(parameters, "agentId", Normal.EMPTY),
                        VendorRegistry.string(parameters, "language", Normal.EMPTY),
                        VendorRegistry.string(parameters, "userType", Normal.EMPTY)),
                List.of(
                        VendorRegistry.adapter(WeChatManifest.class, WeChatOpenAdapter::new, WeChatManifest.OPEN),
                        VendorRegistry.adapter(WeChatManifest.class, WeChatMpAdapter::new, WeChatManifest.MP),
                        VendorRegistry.adapter(WeChatManifest.class, WeChatMiniAdapter::new, WeChatManifest.MINI),
                        VendorRegistry.adapter(
                                WeChatManifest.class,
                                WeChatEeAdapter::new,
                                WeChatManifest.EE,
                                WeChatManifest.EE_QRCODE,
                                WeChatManifest.EE_WEB),
                        VendorRegistry.adapter(
                                WeChatManifest.class,
                                WeChatEeRealmAdapter::new,
                                WeChatManifest.EE_ENTERPRISE)));
    }

}
