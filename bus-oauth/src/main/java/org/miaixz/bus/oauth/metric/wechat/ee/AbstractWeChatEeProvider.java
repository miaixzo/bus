/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Complex;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.wechat.AbstractWeChatProvider;

/**
 * 企业微信 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractWeChatEeProvider extends AbstractWeChatProvider {

    public AbstractWeChatEeProvider(Context context, Complex complex) {
        super(context, complex);
    }

    public AbstractWeChatEeProvider(Context context, Complex complex, ExtendCache cache) {
        super(context, complex, cache);
    }

    @Override
    protected AccToken getAccessToken(Callback callback) {
        String response = doGetAuthorizationCode(accessTokenUrl(null));

        JSONObject object = this.checkResponse(response);

        return AccToken.builder().accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in")).code(callback.getCode()).build();
    }

    @Override
    protected Material getUserInfo(AccToken accToken) {
        String response = doGetUserInfo(accToken);
        JSONObject object = this.checkResponse(response);

        // 返回 OpenId 或其他，均代表非当前企业用户，不支持
        if (!object.containsKey("UserId")) {
            throw new AuthorizedException(ErrorCode.UNIDENTIFIED_PLATFORM.getCode(), complex);
        }
        String userId = object.getString("UserId");
        String userTicket = object.getString("user_ticket");
        JSONObject userDetail = getUserDetail(accToken.getAccessToken(), userId, userTicket);

        return Material.builder().rawJson(userDetail).username(userDetail.getString("name"))
                .nickname(userDetail.getString("alias")).avatar(userDetail.getString("avatar"))
                .location(userDetail.getString("address")).email(userDetail.getString("email")).uuid(userId)
                .gender(getWechatRealGender(userDetail.getString("gender"))).token(accToken).source(complex.toString())
                .build();
    }

    /**
     * 校验请求结果
     *
     * @param response 请求结果
     * @return 如果请求结果正常，则返回JSONObject
     */
    private JSONObject checkResponse(String response) {
        JSONObject object = JSONObject.parseObject(response);

        if (object.containsKey("errcode") && object.getIntValue("errcode") != 0) {
            throw new AuthorizedException(object.getString("errmsg"), complex.getName());
        }

        return object;
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(complex.accessToken()).queryParam("corpid", context.getAppKey())
                .queryParam("corpsecret", context.getAppSecret()).build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.userInfo()).queryParam("access_token", accToken.getAccessToken())
                .queryParam("code", accToken.getCode()).build();
    }

    /**
     * 用户详情
     *
     * @param accessToken accessToken
     * @param userId      企业内用户id
     * @param userTicket  成员票据，用于获取用户信息或敏感信息
     * @return 用户详情
     */
    private JSONObject getUserDetail(String accessToken, String userId, String userTicket) {
        // 用户基础信息
        String userInfoUrl = Builder.fromUrl("https://qyapi.weixin.qq.com/cgi-bin/user/get")
                .queryParam("access_token", accessToken).queryParam("userid", userId).build();
        String userInfoResponse = Httpx.get(userInfoUrl);
        JSONObject userInfo = checkResponse(userInfoResponse);

        // 用户敏感信息
        if (StringKit.isNotEmpty(userTicket)) {
            String userDetailUrl = Builder.fromUrl("https://qyapi.weixin.qq.com/cgi-bin/auth/getuserdetail")
                    .queryParam("access_token", accessToken).build();
            JSONObject param = new JSONObject();
            param.put("user_ticket", userTicket);
            String userDetailResponse = Httpx.post(userDetailUrl, param.toJSONString(), MediaType.APPLICATION_JSON);
            JSONObject userDetail = checkResponse(userDetailResponse);

            userInfo.putAll(userDetail);
        }
        return userInfo;
    }

}
