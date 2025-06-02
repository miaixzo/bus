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
package org.miaixz.bus.auth.nimble.weibo;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.core.xyz.StringKit;
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
    public AccToken getAccessToken(Callback callback) {
        String response = doPostAuthorizationCode(callback.getCode());
        try {
            Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
            if (accessTokenObject == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }
            if (accessTokenObject.containsKey("error")) {
                String errorDescription = (String) accessTokenObject.get("error_description");
                throw new AuthorizedException(errorDescription != null ? errorDescription : "Unknown error");
            }

            String accessToken = (String) accessTokenObject.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String uid = (String) accessTokenObject.get("uid");
            Object expiresInObj = accessTokenObject.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

            return AccToken.builder().accessToken(accessToken).uid(uid).openId(uid).expireIn(expiresIn).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String accessToken = accToken.getAccessToken();
        String uid = accToken.getUid();
        String oauthParam = String.format("uid=%s&access_token=%s", uid, accessToken);

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "OAuth2 " + oauthParam);
        header.put("API-RemoteIP", NetKit.getLocalhostStringV4());
        String userInfo = Httpx.get(userInfoUrl(accToken), null, header);
        try {
            Map<String, Object> object = JsonKit.toPojo(userInfo, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            if (object.containsKey("error")) {
                String error = (String) object.get("error");
                throw new AuthorizedException(error != null ? error : "Unknown error");
            }

            String id = String.valueOf(object.get("id"));
            if (id == null) {
                throw new AuthorizedException("Missing id in user info response");
            }
            String name = (String) object.get("name");
            String profileImageUrl = (String) object.get("profile_image_url");
            String url = (String) object.get("url");
            String profileUrl = (String) object.get("profile_url");
            String screenName = (String) object.get("screen_name");
            String location = (String) object.get("location");
            String description = (String) object.get("description");
            String gender = (String) object.get("gender");

            return Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(id).username(name)
                    .avatar(profileImageUrl).blog(StringKit.isEmpty(url) ? "https://weibo.com/" + profileUrl : url)
                    .nickname(screenName).location(location).remark(description).gender(Gender.of(gender))
                    .token(accToken).source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken accToken
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(this.complex.getConfig().get(Builder.USERINFO))
                .queryParam("access_token", accToken.getAccessToken()).queryParam("uid", accToken.getUid()).build();
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
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse revoke response: empty response");
            }
            if (object.containsKey("error")) {
                String error = (String) object.get("error");
                return Message.builder().errcode(ErrorCode.FAILURE.getCode())
                        .errmsg(error != null ? error : "Unknown error").build();
            }

            Object resultObj = object.get("result");
            boolean result = resultObj instanceof Boolean ? (Boolean) resultObj : false;
            ErrorCode status = result ? ErrorCode.SUCCESS : ErrorCode.FAILURE;
            return Message.builder().errcode(status.getCode()).errmsg(status.getDesc()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse revoke response: " + e.getMessage());
        }
    }

}