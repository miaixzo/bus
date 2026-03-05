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

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.ErrorCode;

/**
 * WeChat Enterprise QR code login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeChatEeQrcodeProvider extends AbstractWeChatEeProvider {

    /**
     * Constructs a {@code WeChatEeQrcodeProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public WeChatEeQrcodeProvider(Context context) {
        super(context, Registry.WECHAT_EE);
    }

    /**
     * Constructs a {@code WeChatEeQrcodeProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public WeChatEeQrcodeProvider(Context context, CacheX cache) {
        super(context, Registry.WECHAT_EE, cache);
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
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Builder.fromUrl(complex.authorize()).queryParam("login_type", context.getLoginType())
                        // When login_type is CorpApp/Service Provider developed app, fill in Enterprise CorpID; when
                        // third-party login, fill in Login Authorization SuiteID
                        .queryParam("appid", context.getClientId())
                        // Enterprise self-built application/service provider developed app AgentID, filled when
                        // login_type=CorpApp
                        .queryParam("agentid", context.getUnionId())
                        .queryParam("redirect_uri", context.getRedirectUri()).queryParam("state", getRealState(state))
                        .queryParam("lang", context.getLang()).build().concat("#wechat_redirect"))
                .build();
    }

    /**
     * Checks the completeness and validity of the context configuration for WeChat Enterprise QR code login.
     * Specifically, it ensures that if the login type is "CorpApp", the agent ID (unionId) is not empty.
     *
     * @param context the authentication context
     * @throws AuthorizedException if the agent ID is empty for "CorpApp" login type
     */
    @Override
    protected void validate(Context context) {
        super.validate(context);
        if ("CorpApp".equals(context.getLoginType()) && StringKit.isEmpty(context.getUnionId())) {
            throw new AuthorizedException(ErrorCode._110014.getKey(), this.complex);
        }
    }

}
