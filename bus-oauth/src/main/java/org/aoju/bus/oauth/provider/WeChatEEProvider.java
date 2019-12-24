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
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.StateCache;

/**
 * 企业微信登录
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public class WeChatEEProvider extends DefaultProvider {

    public WeChatEEProvider(Context context) {
        super(context, Registry.WECHAT_EE);
    }

    public WeChatEEProvider(Context context, StateCache stateCache) {
        super(context, Registry.WECHAT_EE, stateCache);
    }

    /**
     * 微信的特殊性,此时返回的信息同时包含 openid 和 access_token
     *
     * @param Callback 回调返回的参数
     * @return 所有信息
     */
    @Override
    protected AccToken getAccessToken(Callback Callback) {
        String response = doGetAuthorizationCode(accessTokenUrl(Callback.getCode()));

        JSONObject object = this.checkResponse(response);

        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in"))
                .code(Callback.getCode())
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken token) {
        String response = doGetUserInfo(token);
        JSONObject object = this.checkResponse(response);

        // 返回 OpenId 或其他,均代表非当前企业用户,不支持
        if (!object.containsKey("UserId")) {
            throw new InstrumentException(Builder.Status.UNIDENTIFIED_PLATFORM.getCode());
        }
        String userId = object.getString("UserId");
        String userDetailResponse = getUserDetail(token.getAccessToken(), userId);
        JSONObject userDetail = this.checkResponse(userDetailResponse);

        String gender = getRealGender(userDetail);

        return Property.builder()
                .username(userDetail.getString("name"))
                .nickname(userDetail.getString("alias"))
                .avatar(userDetail.getString("avatar"))
                .location(userDetail.getString("address"))
                .email(userDetail.getString("email"))
                .uuid(userId)
                .gender(Normal.Gender.getGender(gender))
                .token(token)
                .source(source.toString())
                .build();
    }

    /**
     * 校验请求结果
     *
     * @param response 请求结果
     * @return 如果请求结果正常, 则返回JSONObject
     */
    private JSONObject checkResponse(String response) {
        JSONObject object = JSONObject.parseObject(response);

        if (object.containsKey("errcode") && object.getIntValue("errcode") != 0) {
            throw new InstrumentException(StringUtils.toString(object.getIntValue("errcode")), object.getString("errmsg"));
        }
        return object;
    }

    /**
     * 获取用户的实际性别,0表示未定义,1表示男性,2表示女性
     *
     * @param userDetail 用户详情
     * @return 用户性别
     */
    private String getRealGender(JSONObject userDetail) {
        int gender = userDetail.getIntValue("gender");
        if (Normal.Gender.MALE.getCode() == gender) {
            return "1";
        }
        return 2 == gender ? "0" : null;
    }

    /**
     * 返回带{@code state}参数的授权url,授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数,可以防止csrf
     * @return 返回授权地址
     * @since 1.9.3
     */
    @Override
    public String authorize(String state) {
        return Builder.fromBaseUrl(source.authorize())
                .queryParam("appid", context.getClientId())
                .queryParam("agentid", context.getAgentId())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state))
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromBaseUrl(source.accessToken())
                .queryParam("corpid", context.getClientId())
                .queryParam("corpsecret", context.getClientSecret())
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param token 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken token) {
        return Builder.fromBaseUrl(source.userInfo())
                .queryParam("access_token", token.getAccessToken())
                .queryParam("code", token.getCode())
                .build();
    }

    /**
     * 用户详情
     *
     * @param accessToken accessToken
     * @param userId      企业内用户id
     * @return 用户详情
     */
    private String getUserDetail(String accessToken, String userId) {
        String userDetailUrl = Builder.fromBaseUrl("https://qyapi.weixin.qq.com/cgi-bin/user/get")
                .queryParam("access_token", accessToken)
                .queryParam("userid", userId)
                .build();
        return Httpx.get(userDetailUrl);
    }

}
