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
package org.miaixz.bus.auth.nimble.dingtalk;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;

/**
 * DingTalk account login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DingTalkAccountProvider extends AbstractDingtalkProvider {

    /**
     * Constructs a {@code DingTalkAccountProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public DingTalkAccountProvider(Context context) {
        super(context, Registry.DINGTALK_ACCOUNT);
    }

    /**
     * Constructs a {@code DingTalkAccountProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public DingTalkAccountProvider(Context context, CacheX cache) {
        super(context, Registry.DINGTALK_ACCOUNT, cache);
    }

}
