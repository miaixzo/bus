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
package org.miaixz.bus.oauth.metric.qq;

import java.util.Map;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
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

import com.alibaba.fastjson.JSONObject;

/**
 * QQ 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QqProvider extends AbstractProvider {

    public QqProvider(Context context) {
        super(context, Registry.QQ);
    }

    public QqProvider(Context context, ExtendCache cache) {
        super(context, Registry.QQ, cache);
    }

    @Override
    protected AccToken getAccessToken(Callback callback) {
        String response = doGetAuthorizationCode(callback.getCode());
        return getAuthToken(response);
    }

    @Override
    public Message refresh(AccToken accToken) {
        String response = Httpx.get(refreshTokenUrl(accToken.getRefreshToken()));
        return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).data(getAuthToken(response)).build();
    }

    @Override
    protected Material getUserInfo(AccToken accToken) {
        String openId = this.getOpenId(accToken);
        String response = doGetUserInfo(accToken);
        JSONObject object = JSONObject.parseObject(response);
        if (object.getIntValue("ret") != 0) {
            throw new AuthorizedException(object.getString("msg"));
        }
        String avatar = object.getString("figureurl_qq_2");
        if (StringKit.isEmpty(avatar)) {
            avatar = object.getString("figureurl_qq_1");
        }

        String location = String.format("%s-%s", object.getString("province"), object.getString("city"));
        return Material.builder().rawJson(object).username(object.getString("nickname"))
                .nickname(object.getString("nickname")).avatar(avatar).location(location).uuid(openId)
                .gender(Gender.of(object.getString("gender"))).token(accToken).source(complex.toString()).build();
    }

    /**
     * 获取QQ用户的OpenId，支持自定义是否启用查询unionid的功能，如果启用查询unionid的功能， 那就需要开发者先通过邮件申请unionid功能，参考链接
     * {@see http://wiki.connect.qq.com/unionid%E4%BB%8B%E7%BB%8D}
     *
     * @param accToken 通过{@link QqProvider#getAccessToken(Callback)}获取到的{@code accToken}
     * @return openId
     */
    private String getOpenId(AccToken accToken) {
        String response = Httpx.get(Builder.fromUrl("https://graph.qq.com/oauth2.0/me")
                .queryParam("access_token", accToken.getAccessToken()).queryParam("unionid", context.isFlag() ? 1 : 0)
                .build());
        String removePrefix = response.replace("callback(", "");
        String removeSuffix = removePrefix.replace(");", "");
        String openId = removeSuffix.trim();
        JSONObject object = JSONObject.parseObject(openId);
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.get("error") + Symbol.COLON + object.get("error_description"));
        }
        accToken.setOpenId(object.getString("openid"));
        if (object.containsKey("unionid")) {
            accToken.setUnionId(object.getString("unionid"));
        }
        return StringKit.isEmpty(accToken.getUnionId()) ? accToken.getOpenId() : accToken.getUnionId();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken 用户授权token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.userInfo()).queryParam("access_token", accToken.getAccessToken())
                .queryParam("oauth_consumer_key", context.getAppKey()).queryParam("openid", accToken.getOpenId())
                .build();
    }

    private AccToken getAuthToken(String response) {
        Map<String, String> accessTokenObject = Builder.parseStringToMap(response);
        if (!accessTokenObject.containsKey("access_token") || accessTokenObject.containsKey("code")) {
            throw new AuthorizedException(accessTokenObject.get("msg"));
        }
        return AccToken.builder().accessToken(accessTokenObject.get("access_token"))
                .expireIn(Integer.parseInt(accessTokenObject.getOrDefault("expires_in", "0")))
                .refreshToken(accessTokenObject.get("refresh_token")).build();
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("scope", this.getScopes(Symbol.COMMA, false, this.getDefaultScopes(QqScope.values())))
                .build();
    }

}
