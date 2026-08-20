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

import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.auth.vendor.VariantManifest;
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
     * @param namespaceId registration namespace identifier
     * @param sourceId    registered Source identifier
     * @param manifest    selected WeChat manifest
     * @param variant     selected MP variant manifest
     * @param options     validated MP options
     * @param services    externally supplied execution services
     * @throws ValidateException if the selected variant is not {@code mp}
     */
    public WeChatMpAdapter(final String namespaceId, final String sourceId, final WeChatManifest manifest,
            final VariantManifest.Variant variant, final WeChatOptions options, final ExecutionServices services) {
        super(namespaceId, sourceId, manifest, variant, options, services);
        if (!WeChatManifest.MP.equals(options.variant())) {
            throw new ValidateException("WeChat MP adapter requires the mp variant");
        }
    }

}
