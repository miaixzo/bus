/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org justauth.cn and other contributors.        ~
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
package org.miaixz.bus.oauth.metric.wechat.ee;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.ErrorCode;

/**
 * 企业微信 二维码登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeChatEeQrcodeProvider extends AbstractWeChatEeProvider {

    public WeChatEeQrcodeProvider(Context context) {
        super(context, Registry.WECHAT_EE);
    }

    public WeChatEeQrcodeProvider(Context context, ExtendCache cache) {
        super(context, Registry.WECHAT_EE, cache);
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize()).queryParam("login_type", context.getLoginType())
                // 登录类型为企业自建应用/服务商代开发应用时填企业 CorpID，第三方登录时填登录授权 SuiteID
                .queryParam("appid", context.getAppKey())
                // 企业自建应用/服务商代开发应用 AgentID，当login_type=CorpApp时填写
                .queryParam("agentid", context.getUnionId()).queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state)).queryParam("lang", context.getLang()).build()
                .concat("#wechat_redirect");
    }

    @Override
    protected void checkConfig(Context context) {
        super.checkConfig(context);
        if ("CorpApp".equals(context.getLoginType()) && StringKit.isEmpty(context.getUnionId())) {
            throw new AuthorizedException(ErrorCode.ILLEGAL_WECHAT_AGENT_ID.getCode(), this.complex);
        }
    }

}
