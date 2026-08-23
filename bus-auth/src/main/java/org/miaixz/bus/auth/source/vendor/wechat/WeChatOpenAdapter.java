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

import org.miaixz.bus.auth.source.SourceServices;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Executes the WeChat Open Platform browser authorization and identity flow.
 *
 * @author Kimi Liu
 */
public class WeChatOpenAdapter extends WeChatAdapterSupport {

    /**
     * Creates one Open Platform adapter for an exact compiled Source.
     *
     * @param spaceId  Source space identifier
     * @param sourceId Source identifier
     * @param manifest selected WeChat manifest
     * @param variant  selected Open Platform variant manifest
     * @param options  validated Open Platform options
     * @param services capability-limited Source services
     * @throws ValidateException if the selected variant is not {@code open}
     */
    public WeChatOpenAdapter(final String spaceId, final String sourceId, final WeChatManifest manifest,
            final VendorManifest.Variant variant, final WeChatOptions options, final SourceServices services) {
        super(spaceId, sourceId, manifest, variant, options, services);
        if (!WeChatManifest.OPEN.equals(options.variant())) {
            throw new ValidateException("WeChat Open adapter requires the open variant");
        }
    }

}
