/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.http.HttpClient;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.cache.StateCache;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;

import java.util.Map;

/**
 * qq登录
 *
 * @author Kimi Liu
 * @version 3.6.3
 * @since JDK 1.8
 */
public class QqProvider extends DefaultProvider {
    public QqProvider(Context config) {
        super(config, Registry.QQ);
    }

    public QqProvider(Context config, StateCache stateCache) {
        super(config, Registry.QQ, stateCache);
    }

    @Override
    protected AccToken getAccessToken(Callback Callback) {
        return getAuthToken(doGetAuthorizationCode(Callback.getCode()));
    }

    @Override
    public Message refresh(AccToken token) {
        String response = HttpClient.get(refreshTokenUrl(token.getRefreshToken()));
        return Message.builder().errcode(Builder.Status.SUCCESS.getCode()).data(getAuthToken(response)).build();
    }

    @Override
    protected Property getUserInfo(AccToken token) {
        String openId = this.getOpenId(token);
        JSONObject object = JSONObject.parseObject(doGetUserInfo(token));
        if (object.getIntValue("ret") != 0) {
            throw new InstrumentException(object.getString("msg"));
        }
        String avatar = object.getString("figureurl_qq_2");
        if (StringUtils.isEmpty(avatar)) {
            avatar = object.getString("figureurl_qq_1");
        }

        String location = String.format("%s-%s", object.getString("province"), object.getString("city"));
        return Property.builder()
                .username(object.getString("nickname"))
                .nickname(object.getString("nickname"))
                .avatar(avatar)
                .location(location)
                .uuid(openId)
                .gender(Normal.Gender.getGender(object.getString("gender")))
                .token(token)
                .source(source.toString())
                .build();
    }

    /**
     * 获取QQ用户的OpenId，支持自定义是否启用查询unionid的功能，如果启用查询unionid的功能，
     * 那就需要开发者先通过邮件申请unionid功能，参考链接 {@see http://wiki.connect.qq.com/unionid%E4%BB%8B%E7%BB%8D}
     *
     * @param token 通过{@link QqProvider#getAccessToken(Callback)}获取到的{@code authToken}
     * @return openId
     */
    private String getOpenId(AccToken token) {
        String response = HttpClient.get(Builder.fromBaseUrl("https://graph.qq.com/oauth2.0/me")
                .queryParam("access_token", token.getAccessToken())
                .queryParam("unionid", config.isUnionId() ? 1 : 0)
                .build());

        String removePrefix = StringUtils.replace(response, "callback(", "");
        String removeSuffix = StringUtils.replace(removePrefix, ");", "");
        String openId = StringUtils.trim(removeSuffix);
        JSONObject object = JSONObject.parseObject(openId);
        if (object.containsKey("error")) {
            throw new InstrumentException(object.get("error") + ":" + object.get("error_description"));
        }
        token.setOpenId(object.getString("openid"));
        if (object.containsKey("unionid")) {
            token.setUnionId(object.getString("unionid"));
        }
        return StringUtils.isEmpty(token.getUnionId()) ? token.getOpenId() : token.getUnionId();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param token 用户授权token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken token) {
        return Builder.fromBaseUrl(source.userInfo())
                .queryParam("access_token", token.getAccessToken())
                .queryParam("oauth_consumer_key", config.getClientId())
                .queryParam("openid", token.getOpenId())
                .build();
    }

    private AccToken getAuthToken(String response) {
        Map<String, String> accessTokenObject = parseStringToMap(response);
        if (!accessTokenObject.containsKey("access_token") || accessTokenObject.containsKey("code")) {
            throw new InstrumentException(accessTokenObject.get("msg"));
        }
        return AccToken.builder()
                .accessToken(accessTokenObject.get("access_token"))
                .expireIn(Integer.valueOf(accessTokenObject.get("expires_in")))
                .refreshToken(accessTokenObject.get("refresh_token"))
                .build();
    }

}
