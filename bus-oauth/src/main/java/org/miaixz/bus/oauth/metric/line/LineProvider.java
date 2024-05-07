/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org justauth and other contributors.           *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.oauth.metric.line;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.*;
import org.miaixz.bus.oauth.metric.DefaultProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Line 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LineProvider extends DefaultProvider {

    public LineProvider(Context context) {
        super(context, Registry.LINE);
    }

    public LineProvider(Context context, ExtendCache authorizeCache) {
        super(context, Registry.LINE, authorizeCache);
    }

    @Override
    protected AccToken getAccessToken(Callback authCallback) {
        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", authCallback.getCode());
        params.put("redirect_uri", context.getRedirectUri());
        params.put("client_id", context.getAppKey());
        params.put("client_secret", context.getAppSecret());
        String response = Httpx.post(complex.accessToken(), params);
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        return AccToken.builder()
                .accessToken(accessTokenObject.getString("access_token"))
                .refreshToken(accessTokenObject.getString("refresh_token"))
                .expireIn(accessTokenObject.getIntValue("expires_in"))
                .idToken(accessTokenObject.getString("id_token"))
                .scope(accessTokenObject.getString("scope"))
                .tokenType(accessTokenObject.getString("token_type"))
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken accToken) {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/x-www-form-urlencoded");
        header.put("Authorization", "Bearer ".concat(accToken.getAccessToken()));

        String userInfo = Httpx.get(complex.userInfo(), null, header);
        JSONObject object = JSONObject.parseObject(userInfo);
        return Property.builder()
                .rawJson(object)
                .uuid(object.getString("userId"))
                .username(object.getString("displayName"))
                .nickname(object.getString("displayName"))
                .avatar(object.getString("pictureUrl"))
                .remark(object.getString("statusMessage"))
                .gender(Gender.UNKNOWN)
                .token(accToken)
                .source(complex.toString())
                .build();
    }

    @Override
    public Message revoke(AccToken accToken) {
        Map<String, Object> form = new HashMap<>(5);
        form.put("access_token", accToken.getAccessToken());
        form.put("client_id", context.getAppKey());
        form.put("client_secret", context.getAppSecret());
        String userInfo = Httpx.post(complex.revoke(), form);
        JSONObject object = JSONObject.parseObject(userInfo);
        // 返回1表示取消授权成功，否则失败
        ErrorCode status = object.getBooleanValue("revoked") ? ErrorCode.SUCCESS : ErrorCode.FAILURE;
        return Message.builder().errcode(status.getCode()).errmsg(status.getDesc()).build();
    }

    @Override
    public Message refresh(AccToken oldToken) {
        Map<String, Object> form = new HashMap<>();
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", oldToken.getRefreshToken());
        form.put("client_id", context.getAppKey());
        form.put("client_secret", context.getAppSecret());
        String response = Httpx.post(complex.accessToken(), form);
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        return Message.builder()
                .errcode(ErrorCode.SUCCESS.getCode())
                .data(AccToken.builder()
                        .accessToken(accessTokenObject.getString("access_token"))
                        .refreshToken(accessTokenObject.getString("refresh_token"))
                        .expireIn(accessTokenObject.getIntValue("expires_in"))
                        .idToken(accessTokenObject.getString("id_token"))
                        .scope(accessTokenObject.getString("scope"))
                        .tokenType(accessTokenObject.getString("token_type"))
                        .build())
                .build();
    }

    @Override
    public String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.userInfo())
                .queryParam("user", accToken.getUid())
                .build();
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("nonce", state)
                .queryParam("scope", this.getScopes(" ", true, this.getDefaultScopes(LineScope.values())))
                .build();
    }

}
