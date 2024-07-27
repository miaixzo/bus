/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org justauth.cn and other contributors.        ~
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
package org.miaixz.bus.oauth.metric.weibo;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 微博 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeiboProvider extends AbstractProvider {

    public WeiboProvider(Context context) {
        super(context, Registry.WEIBO);
    }

    public WeiboProvider(Context context, ExtendCache cache) {
        super(context, Registry.WEIBO, cache);
    }

    @Override
    protected AccToken getAccessToken(Callback callback) {
        String response = doPostAuthorizationCode(callback.getCode());
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        if (accessTokenObject.containsKey("error")) {
            throw new AuthorizedException(accessTokenObject.getString("error_description"));
        }
        return AccToken.builder().accessToken(accessTokenObject.getString("access_token"))
                .uid(accessTokenObject.getString("uid")).openId(accessTokenObject.getString("uid"))
                .expireIn(accessTokenObject.getIntValue("expires_in")).build();
    }

    @Override
    protected Material getUserInfo(AccToken accToken) {
        String accessToken = accToken.getAccessToken();
        String uid = accToken.getUid();
        String oauthParam = String.format("uid=%s&access_token=%s", uid, accessToken);

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "OAuth2 " + oauthParam);
        header.put("API-RemoteIP", NetKit.getLocalhostStringV4());
        String userInfo = Httpx.get(userInfoUrl(accToken), null, header);
        JSONObject object = JSONObject.parseObject(userInfo);
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("error"));
        }
        return Material.builder().rawJson(object).uuid(object.getString("id")).username(object.getString("name"))
                .avatar(object.getString("profile_image_url"))
                .blog(StringKit.isEmpty(object.getString("url"))
                        ? "https://weibo.com/" + object.getString("profile_url")
                        : object.getString("url"))
                .nickname(object.getString("screen_name")).location(object.getString("location"))
                .remark(object.getString("description")).gender(Gender.of(object.getString("gender"))).token(accToken)
                .source(complex.toString()).build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken accToken
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.userInfo()).queryParam("access_token", accToken.getAccessToken())
                .queryParam("uid", accToken.getUid()).build();
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("scope", this.getScopes(Symbol.COMMA, false, this.getDefaultScopes(WeiboScope.values())))
                .build();
    }

    @Override
    public Message revoke(AccToken accToken) {
        String response = doGetRevoke(accToken);
        JSONObject object = JSONObject.parseObject(response);
        if (object.containsKey("error")) {
            return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(object.getString("error")).build();
        }
        // 返回 result = true 表示取消授权成功，否则失败
        ErrorCode status = object.getBooleanValue("result") ? ErrorCode.SUCCESS : ErrorCode.FAILURE;
        return Message.builder().errcode(status.getCode()).errmsg(status.getDesc()).build();
    }

}
