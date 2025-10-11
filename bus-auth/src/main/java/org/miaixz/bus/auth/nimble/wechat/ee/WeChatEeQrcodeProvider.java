/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.nimble.wechat.ee;

import org.miaixz.bus.cache.CacheX;
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
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize()).queryParam("login_type", context.getLoginType())
                // When login_type is CorpApp/Service Provider developed app, fill in Enterprise CorpID; when
                // third-party login, fill in Login Authorization SuiteID
                .queryParam("appid", context.getAppKey())
                // Enterprise self-built application/service provider developed app AgentID, filled when
                // login_type=CorpApp
                .queryParam("agentid", context.getUnionId()).queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state)).queryParam("lang", context.getLang()).build()
                .concat("#wechat_redirect");
    }

    /**
     * Checks the completeness and validity of the context configuration for WeChat Enterprise QR code login.
     * Specifically, it ensures that if the login type is "CorpApp", the agent ID (unionId) is not empty.
     *
     * @param context the authentication context
     * @throws AuthorizedException if the agent ID is empty for "CorpApp" login type
     */
    @Override
    protected void check(Context context) {
        super.check(context);
        if ("CorpApp".equals(context.getLoginType()) && StringKit.isEmpty(context.getUnionId())) {
            throw new AuthorizedException(ErrorCode.ILLEGAL_WECHAT_AGENT_ID.getKey(), this.complex);
        }
    }

}
