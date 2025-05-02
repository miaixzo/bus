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
package org.miaixz.bus.oauth.metric.wechat.ee;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;

/**
 * 企业微信 网页登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeChatEeWebProvider extends AbstractWeChatEeProvider {

    public WeChatEeWebProvider(Context context) {
        super(context, Registry.WECHAT_EE_WEB);
    }

    public WeChatEeWebProvider(Context context, ExtendCache cache) {
        super(context, Registry.WECHAT_EE_WEB, cache);
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize()).queryParam("appid", context.getAppKey())
                .queryParam("agentid", context.getUnionId())
                .queryParam("redirect_uri", UrlEncoder.encodeAll(context.getRedirectUri()))
                .queryParam("response_type", "code")
                .queryParam("scope",
                        this.getScopes(Symbol.COMMA, false, this.getDefaultScopes(WeChatEeWebScope.values())))
                .queryParam("state", getRealState(state).concat("#wechat_redirect")).build();
    }

}
