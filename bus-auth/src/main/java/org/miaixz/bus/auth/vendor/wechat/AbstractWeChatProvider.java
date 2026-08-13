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

import org.miaixz.bus.auth.vendor.AbstractProvider;
import org.miaixz.bus.auth.vendor.VendorConfiguration;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Shared client base for WeChat vendor families.
 *
 * @author Kimi Liu
 */
public abstract class AbstractWeChatProvider extends AbstractProvider {

    /**
     * Creates this provider from explicit vendor dependencies.
     *
     * @param configuration complete non-null vendor dependencies
     * @param definition    non-null vendor metadata supplied by the concrete family
     */
    protected AbstractWeChatProvider(final VendorConfiguration configuration, final VendorDefinition definition) {
        super(configuration, definition);
    }

    /**
     * Retrieves the actual gender of a WeChat platform user. 0 indicates undefined, 1 indicates male, 2 indicates
     * female.
     *
     * @param originalGender the original gender marked by the third-party platform
     * @return the user's gender
     */
    public static Gender getWechatRealGender(final String originalGender) {
        if (StringKit.isEmpty(originalGender) || Symbol.ZERO.equals(originalGender)) {
            return Gender.UNKNOWN;
        }
        if (Symbol.ONE.equals(originalGender)) {
            return Gender.MALE;
        }
        return "2".equals(originalGender) ? Gender.FEMALE : Gender.of(originalGender);
    }

}
