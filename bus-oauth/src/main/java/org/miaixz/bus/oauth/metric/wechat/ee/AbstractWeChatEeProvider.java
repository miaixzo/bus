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
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Complex;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.wechat.AbstractWeChatProvider;

import java.util.HashMap;
import java.util.Map;

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
    public AccToken getAccessToken(Callback callback) {
        String response = doGetAuthorizationCode(accessTokenUrl(null));
        Map<String, Object> object = this.checkResponse(response);

        String accessToken = (String) object.get("access_token");
        if (accessToken == null) {
            throw new AuthorizedException("Missing access_token in response");
        }
        Object expiresInObj = object.get("expires_in");
        int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

        return AccToken.builder().accessToken(accessToken).expireIn(expiresIn).code(callback.getCode()).build();
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String response = doGetUserInfo(accToken);
        Map<String, Object> object = this.checkResponse(response);

        // 返回 OpenId 或其他，均代表非当前企业用户，不支持
        if (!object.containsKey("UserId")) {
            throw new AuthorizedException(ErrorCode.UNIDENTIFIED_PLATFORM.getCode(), complex);
        }
        String userId = (String) object.get("UserId");
        String userTicket = (String) object.get("user_ticket");
        Map<String, Object> data = getUserDetail(accToken.getAccessToken(), userId, userTicket);

        String name = (String) data.get("name");
        String alias = (String) data.get("alias");
        String avatar = (String) data.get("avatar");
        String address = (String) data.get("address");
        String email = (String) data.get("email");
        String gender = (String) data.get("gender");

        return Material.builder().rawJson(JsonKit.toJsonString(data)).username(name).nickname(alias).avatar(avatar)
                .location(address).email(email).uuid(userId).gender(getWechatRealGender(gender)).token(accToken)
                .source(complex.toString()).build();
    }

    /**
     * 校验请求结果
     *
     * @param response 请求结果
     * @return 如果请求结果正常，则返回Map
     */
    private Map<String, Object> checkResponse(String response) {
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse response: empty response");
            }
            Object errcodeObj = object.get("errcode");
            if (errcodeObj != null && !errcodeObj.equals(0)) {
                String errmsg = (String) object.get("errmsg");
                throw new AuthorizedException(errmsg != null ? errmsg : "Unknown error", complex.getName());
            }
            return object;
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse response: " + e.getMessage());
        }
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    @Override
    public String accessTokenUrl(String code) {
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
    public String userInfoUrl(AccToken accToken) {
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
    private Map<String, Object> getUserDetail(String accessToken, String userId, String userTicket) {
        // 用户基础信息
        String userInfoUrl = Builder.fromUrl("https://qyapi.weixin.qq.com/cgi-bin/user/get")
                .queryParam("access_token", accessToken).queryParam("userid", userId).build();
        String userInfoResponse = Httpx.get(userInfoUrl);
        Map<String, Object> userInfo = checkResponse(userInfoResponse);

        // 用户敏感信息
        if (StringKit.isNotEmpty(userTicket)) {
            String userDetailUrl = Builder.fromUrl("https://qyapi.weixin.qq.com/cgi-bin/auth/getuserdetail")
                    .queryParam("access_token", accessToken).build();
            Map<String, Object> param = new HashMap<>();
            param.put("user_ticket", userTicket);
            String userDetailResponse = Httpx.post(userDetailUrl, JsonKit.toJsonString(param),
                    MediaType.APPLICATION_JSON);
            Map<String, Object> userDetail = checkResponse(userDetailResponse);

            userInfo.putAll(userDetail);
        }
        return userInfo;
    }

}