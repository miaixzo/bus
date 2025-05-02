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
package org.miaixz.bus.oauth.metric.wechat.open;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.wechat.AbstractWeChatProvider;

import java.util.Map;

/**
 * 微信开放平台 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeChatOpenProvider extends AbstractWeChatProvider {

    public WeChatOpenProvider(Context context) {
        super(context, Registry.WECHAT_OPEN);
    }

    public WeChatOpenProvider(Context context, ExtendCache cache) {
        super(context, Registry.WECHAT_OPEN, cache);
    }

    /**
     * 微信的特殊性，此时返回的信息同时包含 openid 和 access_token
     *
     * @param callback 回调返回的参数
     * @return 所有信息
     */
    @Override
    public AccToken getAccessToken(Callback callback) {
        return this.getToken(accessTokenUrl(callback.getCode()));
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String openId = accToken.getOpenId();
        String response = doGetUserInfo(accToken);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            this.checkResponse(object);

            String country = (String) object.get("country");
            String province = (String) object.get("province");
            String city = (String) object.get("city");
            String location = String.format("%s-%s-%s", country, province, city);

            String unionId = (String) object.get("unionid");
            if (unionId != null) {
                accToken.setUnionId(unionId);
            }

            String nickname = (String) object.get("nickname");
            String headimgurl = (String) object.get("headimgurl");
            String sex = (String) object.get("sex");

            return Material.builder().rawJson(JsonKit.toJsonString(object)).username(nickname).nickname(nickname)
                    .avatar(headimgurl).location(location).uuid(openId).gender(getWechatRealGender(sex)).token(accToken)
                    .source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    @Override
    public Message refresh(AccToken oldToken) {
        return Message.builder().errcode(ErrorCode.SUCCESS.getCode())
                .data(this.getToken(refreshTokenUrl(oldToken.getRefreshToken()))).build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("errcode")) {
            String errcode = String.valueOf(object.get("errcode"));
            String errmsg = (String) object.get("errmsg");
            throw new AuthorizedException(errcode, errmsg != null ? errmsg : "Unknown error");
        }
    }

    /**
     * 获取token，适用于获取access_token和刷新token
     *
     * @param accessTokenUrl 实际请求token的地址
     * @return token对象
     */
    private AccToken getToken(String accessTokenUrl) {
        String response = Httpx.get(accessTokenUrl);
        try {
            Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
            if (accessTokenObject == null) {
                throw new AuthorizedException("Failed to parse token response: empty response");
            }

            this.checkResponse(accessTokenObject);

            String accessToken = (String) accessTokenObject.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String refreshToken = (String) accessTokenObject.get("refresh_token");
            Object expiresInObj = accessTokenObject.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String openId = (String) accessTokenObject.get("openid");

            return AccToken.builder().accessToken(accessToken).refreshToken(refreshToken).expireIn(expiresIn)
                    .openId(openId).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse token response: " + e.getMessage());
        }
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize()).queryParam("response_type", "code")
                .queryParam("appid", context.getAppKey()).queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", "snsapi_login").queryParam("state", getRealState(state)).build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(complex.accessToken()).queryParam("code", code).queryParam("appid", context.getAppKey())
                .queryParam("secret", context.getAppSecret()).queryParam("grant_type", "authorization_code").build();
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
                .queryParam("openid", accToken.getOpenId()).queryParam("lang", "zh_CN").build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param refreshToken getAccessToken方法返回的refreshToken
     * @return 返回获取userInfo的url
     */
    @Override
    protected String refreshTokenUrl(String refreshToken) {
        return Builder.fromUrl(complex.refresh()).queryParam("appid", context.getAppKey())
                .queryParam("refresh_token", refreshToken).queryParam("grant_type", "refresh_token").build();
    }

}