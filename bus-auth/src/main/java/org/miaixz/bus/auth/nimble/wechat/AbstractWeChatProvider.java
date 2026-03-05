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
package org.miaixz.bus.auth.nimble.wechat;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.auth.Complex;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.nimble.AbstractProvider;

/**
 * Abstract base class for WeChat login providers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractWeChatProvider extends AbstractProvider {

    /**
     * Constructs an {@code AbstractWeChatProvider} with the specified context and complex configuration.
     *
     * @param context the authentication context
     * @param complex the complex configuration for WeChat
     */
    public AbstractWeChatProvider(Context context, Complex complex) {
        super(context, complex);
    }

    /**
     * Constructs an {@code AbstractWeChatProvider} with the specified context, complex configuration, and cache.
     *
     * @param context the authentication context
     * @param complex the complex configuration for WeChat
     * @param cache   the cache implementation
     */
    public AbstractWeChatProvider(Context context, Complex complex, CacheX cache) {
        super(context, complex, cache);
    }

    /**
     * Retrieves the actual gender of a WeChat platform user. 0 indicates undefined, 1 indicates male, 2 indicates
     * female.
     *
     * @param originalGender the original gender marked by the third-party platform
     * @return the user's gender
     */
    public static Gender getWechatRealGender(String originalGender) {
        if (StringKit.isEmpty(originalGender) || "0".equals(originalGender)) {
            return Gender.UNKNOWN;
        }
        return Gender.of(originalGender);
    }

}
