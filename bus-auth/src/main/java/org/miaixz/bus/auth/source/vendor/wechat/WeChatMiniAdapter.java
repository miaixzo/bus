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

import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Executes the direct WeChat Mini Program code-to-session identity flow.
 *
 * @author Kimi Liu
 */
public class WeChatMiniAdapter extends WeChatAdapterSupport {

    /**
     * Creates one Mini Program adapter for an exact compiled Source.
     *
     * @param spaceId  Source space identifier
     * @param sourceId Source identifier
     * @param manifest selected WeChat manifest
     * @param variant  selected Mini Program variant manifest
     * @param options  validated Mini Program options
     * @param services externally supplied execution services
     * @throws ValidateException if the selected variant is not {@code mini}
     */
    public WeChatMiniAdapter(final String spaceId, final String sourceId, final WeChatManifest manifest,
            final VendorManifest.Variant variant, final WeChatOptions options, final DriverServices services) {
        super(spaceId, sourceId, manifest, variant, options, services);
        if (!WeChatManifest.MINI.equals(options.variant())) {
            throw new ValidateException("WeChat Mini adapter requires the mini variant");
        }
    }

}
