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

import org.miaixz.bus.auth.vendor.VendorTargets;
import org.miaixz.bus.core.lang.Assert;

/**
 * Associates the additional enterprise WeChat member endpoints that do not correspond to standard OAuth endpoints.
 *
 * @param member    endpoint used to read the visible enterprise member profile
 * @param sensitive endpoint used to exchange a user ticket for authorized sensitive member fields
 * @author Kimi Liu
 */
public record WeChatTargets(VendorTargets.Fixed member, VendorTargets.Fixed sensitive) {

    /**
     * Validates the immutable endpoint association.
     */
    public WeChatTargets {
        Assert.notNull(member, "WeChat member target must not be null");
        Assert.notNull(sensitive, "WeChat sensitive-member target must not be null");
    }

}
