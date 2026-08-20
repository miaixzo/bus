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
package org.miaixz.bus.auth.vendor.wechat.internal.mp;

import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.wechat.WeChatDefinition;
import org.miaixz.bus.auth.vendor.wechat.WeChatSourceSettings;
import org.miaixz.bus.auth.vendor.wechat.internal.WeChatAdapterSupport;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Executes the WeChat Official Account, historically identified as MP, browser identity flow.
 *
 * @author Kimi Liu
 */
public final class WeChatMpAdapter extends WeChatAdapterSupport {

    /**
     * Creates one Official Account adapter for an exact compiled Source.
     *
     * @param namespaceId       registration namespace identifier
     * @param sourceId          registered Source identifier
     * @param vendorDefinition  selected WeChat definition
     * @param variantDefinition selected MP variant definition
     * @param settings          validated MP settings
     * @param services          externally supplied execution services
     * @throws ValidateException if the selected variant is not {@code mp}
     */
    public WeChatMpAdapter(final String namespaceId, final String sourceId, final WeChatDefinition vendorDefinition,
            final VendorDefinition.Definition variantDefinition, final WeChatSourceSettings settings,
            final ExecutionServices services) {
        super(namespaceId, sourceId, vendorDefinition, variantDefinition, settings, services);
        if (!WeChatDefinition.MP.equals(settings.variant())) {
            throw new ValidateException("WeChat MP adapter requires the mp variant");
        }
    }

}
