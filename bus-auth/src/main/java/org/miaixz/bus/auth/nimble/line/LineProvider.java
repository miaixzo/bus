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
package org.miaixz.bus.auth.nimble.line;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
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
 * Line 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LineProvider extends AbstractProvider {

    public LineProvider(Context context) {
        super(context, Registry.LINE);
    }

    public LineProvider(Context context, ExtendCache cache) {
        super(context, Registry.LINE, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", callback.getCode());
        params.put("redirect_uri", context.getRedirectUri());
        params.put("client_id", context.getAppKey());
        params.put("client_secret", context.getAppSecret());
        String response = Httpx.post(this.complex.getConfig().get(Builder.ACCESSTOKEN), params);
        try {
            Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
            if (accessTokenObject == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }

            String accessToken = (String) accessTokenObject.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String refreshToken = (String) accessTokenObject.get("refresh_token");
            Object expiresInObj = accessTokenObject.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String idToken = (String) accessTokenObject.get("id_token");
            String scope = (String) accessTokenObject.get("scope");
            String tokenType = (String) accessTokenObject.get("token_type");

            return AccToken.builder().accessToken(accessToken).refreshToken(refreshToken).expireIn(expiresIn)
                    .idToken(idToken).scope(scope).tokenType(tokenType).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        header.put("Authorization", "Bearer ".concat(accToken.getAccessToken()));

        String userInfo = Httpx.get(this.complex.getConfig().get(Builder.USERINFO), null, header);
        try {
            Map<String, Object> object = JsonKit.toPojo(userInfo, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            String userId = (String) object.get("userId");
            if (userId == null) {
                throw new AuthorizedException("Missing userId in user info response");
            }
            String displayName = (String) object.get("displayName");
            String pictureUrl = (String) object.get("pictureUrl");
            String statusMessage = (String) object.get("statusMessage");

            return Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(userId).username(displayName)
                    .nickname(displayName).avatar(pictureUrl).remark(statusMessage).gender(Gender.UNKNOWN)
                    .token(accToken).source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    @Override
    public Message revoke(AccToken accToken) {
        Map<String, String> form = new HashMap<>(5);
        form.put("access_token", accToken.getAccessToken());
        form.put("client_id", context.getAppKey());
        form.put("client_secret", context.getAppSecret());
        String userInfo = Httpx.post(this.complex.getConfig().get(Builder.REVOKE), form);
        try {
            Map<String, Object> object = JsonKit.toPojo(userInfo, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse revoke response: empty response");
            }

            Boolean revoked = (Boolean) object.get("revoked");
            // 返回1表示取消授权成功，否则失败
            ErrorCode status = (revoked != null && revoked) ? ErrorCode.SUCCESS : ErrorCode.FAILURE;
            return Message.builder().errcode(status.getCode()).errmsg(status.getDesc()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse revoke response: " + e.getMessage());
        }
    }

    @Override
    public Message refresh(AccToken oldToken) {
        Map<String, String> form = new HashMap<>();
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", oldToken.getRefreshToken());
        form.put("client_id", context.getAppKey());
        form.put("client_secret", context.getAppSecret());
        String response = Httpx.post(this.complex.getConfig().get(Builder.ACCESSTOKEN), form);
        try {
            Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
            if (accessTokenObject == null) {
                throw new AuthorizedException("Failed to parse refresh token response: empty response");
            }

            String accessToken = (String) accessTokenObject.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in refresh response");
            }
            String refreshToken = (String) accessTokenObject.get("refresh_token");
            Object expiresInObj = accessTokenObject.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String idToken = (String) accessTokenObject.get("id_token");
            String scope = (String) accessTokenObject.get("scope");
            String tokenType = (String) accessTokenObject.get("token_type");

            return Message.builder().errcode(ErrorCode.SUCCESS.getCode())
                    .data(AccToken.builder().accessToken(accessToken).refreshToken(refreshToken).expireIn(expiresIn)
                            .idToken(idToken).scope(scope).tokenType(tokenType).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse refresh token response: " + e.getMessage());
        }
    }

    @Override
    public String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(this.complex.getConfig().get(Builder.USERINFO)).queryParam("user", accToken.getUid())
                .build();
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state)).queryParam("nonce", state)
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getDefaultScopes(LineScope.values())))
                .build();
    }

}