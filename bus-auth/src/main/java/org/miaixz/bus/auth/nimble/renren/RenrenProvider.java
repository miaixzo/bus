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
package org.miaixz.bus.auth.nimble.renren;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.AccToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 人人网 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RenrenProvider extends AbstractProvider {

    public RenrenProvider(Context context) {
        super(context, Registry.RENREN);
    }

    public RenrenProvider(Context context, ExtendCache cache) {
        super(context, Registry.RENREN, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        return this.getToken(accessTokenUrl(callback.getCode()));
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String response = doGetUserInfo(accToken);
        Map<String, Object> userObj = (Map<String, Object>) JsonKit.toPojo(response, Map.class).get("response");

        return Material.builder().rawJson(JsonKit.toJsonString(userObj)).uuid((String) userObj.get("id"))
                .avatar(getAvatarUrl(userObj)).nickname((String) userObj.get("name")).company(getCompany(userObj))
                .gender(getGender(userObj)).token(accToken).source(complex.toString()).build();
    }

    @Override
    public Message refresh(AccToken accToken) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(getToken(this.refreshTokenUrl(accToken.getRefreshToken()))).build();
    }

    private AccToken getToken(String url) {
        String response = Httpx.post(url);
        Map<String, Object> jsonObject = JsonKit.toPojo(response, Map.class);
        if (jsonObject.containsKey("error")) {
            throw new AuthorizedException("Failed to get token from Renren: " + jsonObject);
        }

        return AccToken.builder().tokenType((String) jsonObject.get("token_type"))
                .expireIn(((Number) jsonObject.get("expires_in")).intValue())
                .accessToken(UrlEncoder.encodeAll((String) jsonObject.get("access_token")))
                .refreshToken(UrlEncoder.encodeAll((String) jsonObject.get("refresh_token")))
                .openId(((Map<String, Object>) jsonObject.get("user")).get("id").toString()).build();
    }

    private String getAvatarUrl(Map<String, Object> userObj) {
        List<Map<String, Object>> jsonArray = (List<Map<String, Object>>) userObj.get("avatar");
        if (Objects.isNull(jsonArray) || jsonArray.isEmpty()) {
            return null;
        }
        return jsonArray.get(0).get("url").toString();
    }

    private Gender getGender(Map<String, Object> userObj) {
        Map<String, Object> basicInformation = (Map<String, Object>) userObj.get("basicInformation");
        if (Objects.isNull(basicInformation)) {
            return Gender.UNKNOWN;
        }
        return Gender.of((String) basicInformation.get("sex"));
    }

    private String getCompany(Map<String, Object> userObj) {
        List<Map<String, Object>> jsonArray = (List<Map<String, Object>>) userObj.get("work");
        if (Objects.isNull(jsonArray) || jsonArray.isEmpty()) {
            return null;
        }
        return jsonArray.get(0).get("name").toString();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(this.complex.getConfig().get(Builder.USERINFO))
                .queryParam("access_token", accToken.getAccessToken()).queryParam("userId", accToken.getOpenId())
                .build();
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("scope", this.getScopes(Symbol.COMMA, false, this.getDefaultScopes(RenrenScope.values())))
                .build();
    }

}