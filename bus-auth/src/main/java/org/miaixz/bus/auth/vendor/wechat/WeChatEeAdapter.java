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
package org.miaixz.bus.auth.vendor.wechat;

import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Executes the three enterprise WeChat, or EE, browser identity variants.
 *
 * @author Kimi Liu
 */
public class WeChatEeAdapter extends WeChatAdapterSupport {

    /**
     * Creates one enterprise WeChat adapter for an exact compiled Source.
     *
     * @param namespaceId registration namespace identifier
     * @param sourceId    registered Source identifier
     * @param manifest    selected WeChat manifest
     * @param variant     selected enterprise variant manifest
     * @param options     validated enterprise options
     * @param services    externally supplied execution services
     * @throws ValidateException if the selected variant is not an EE product
     */
    public WeChatEeAdapter(final String namespaceId, final String sourceId, final WeChatManifest manifest,
            final VariantManifest.Variant variant, final WeChatOptions options, final DriverServices services) {
        super(namespaceId, sourceId, manifest, variant, options, services);
        if (!WeChatManifest.EE.equals(options.variant()) && !WeChatManifest.EE_QRCODE.equals(options.variant())
                && !WeChatManifest.EE_WEB.equals(options.variant())) {
            throw new ValidateException("WeChat EE adapter requires an enterprise variant");
        }
    }

}
