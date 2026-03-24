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
package org.miaixz.bus.auth.nimble.wechat.ee;

import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;

/**
 * WeChat Enterprise web login provider.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class WeChatEeWebProvider extends AbstractWeChatEeProvider {

    /**
     * Constructs a {@code WeChatEeWebProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public WeChatEeWebProvider(Context context) {
        super(context, Registry.WECHAT_EE_WEB);
    }

    /**
     * Constructs a {@code WeChatEeWebProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public WeChatEeWebProvider(Context context, CacheX cache) {
        super(context, Registry.WECHAT_EE_WEB, cache);
    }

    /**
     * Returns the authorization URL with a {@code state} parameter. The {@code state} will be included in the
     * authorization callback.
     *
     * @param state the parameter to verify the authorization process, which can prevent CSRF attacks
     * @return the authorization URL
     */
    @Override
    public Message build(String state) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Builder.fromUrl(complex.authorize()).queryParam("appid", context.getClientId())
                                .queryParam("agentid", context.getUnionId())
                                .queryParam("redirect_uri", UrlEncoder.encodeAll(context.getRedirectUri()))
                                .queryParam("response_type", "code")
                                .queryParam(
                                        "scope",
                                        this.getScopes(Symbol.COMMA, false, this.getScopes(WeChatEeWebScope.values())))
                                .queryParam("state", getRealState(state).concat("#wechat_redirect")).build())
                .build();
    }

}
