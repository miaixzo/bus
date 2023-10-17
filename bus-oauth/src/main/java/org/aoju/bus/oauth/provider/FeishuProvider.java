/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.cache.metric.ExtendCache;
import org.aoju.bus.core.exception.AuthorizedException;
import org.aoju.bus.core.lang.Header;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.core.toolkit.UriKit;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;

import java.util.HashMap;
import java.util.Map;

/**
 * 飞书登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FeishuProvider extends AbstractProvider {

    public FeishuProvider(Context context) {
        super(context, Registry.FEISHU);
    }

    public FeishuProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.FEISHU, extendCache);
    }

    @Override
    protected AccToken getAccessToken(Callback callback) {
        JSONObject requestObject = new JSONObject();
        requestObject.put("app_access_token", this.getAppAccessToken());
        requestObject.put("grant_type", "authorization_code");
        requestObject.put("code", callback.getCode());
        return getToken(requestObject, this.source.accessToken());

    }

    @Override
    protected Property getUserInfo(AccToken accToken) {
        Map<String, String> header = new HashMap<>();
        header.put(Header.CONTENT_TYPE, "application/json");
        header.put(Header.AUTHORIZATION, "Bearer " + accToken.getAccessToken());

        String response = Httpx.post(source.userInfo(), null, header);

        JSONObject object = JSON.parseObject(response);
        this.checkResponse(object);
        JSONObject data = object.getJSONObject("data");
        return Property.builder()
                .rawJson(object)
                .uuid(data.getString("union_id"))
                .username(data.getString("name"))
                .nickname(data.getString("name"))
                .avatar(data.getString("avatar_url"))
                .email(data.getString("email"))
                .gender(Normal.Gender.UNKNOWN)
                .token(accToken)
                .source(source.toString())
                .build();
    }

    @Override
    public Message refresh(AccToken authToken) {
        JSONObject params = new JSONObject();
        params.put("app_access_token", this.getAppAccessToken());
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", authToken.getRefreshToken());
        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .data(getToken(params, this.source.refresh()))
                .build();
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(source.authorize())
                .queryParam("app_id", context.getAppKey())
                .queryParam("redirect_uri", UriKit.encode(context.getRedirectUri()))
                .queryParam("state", getRealState(state))
                .build();
    }

    /**
     * 获取 app_access_token（企业自建应用）
     * Token 有效期为 2 小时，在此期间调用该接口 token 不会改变。当 token 有效期小于 30 分的时候，再次请求获取 token 的时候，
     * 会生成一个新的 token，与此同时老的 token 依然有效。
     *
     * @return ths string
     */
    private String getAppAccessToken() {
        String cacheKey = this.source.getName().concat(":app_access_token:").concat(context.getAppKey());
        String cacheAppAccessToken = (String) this.extendCache.get(cacheKey);
        if (StringKit.isNotEmpty(cacheAppAccessToken)) {
            return cacheAppAccessToken;
        }
        String url = "https://open.feishu.cn/open-apis/auth/v3/app_access_token/internal/";
        Map<String, Object> params = new HashMap<>();
        params.put("app_id", context.getAppKey());
        params.put("app_secret", context.getAppSecret());

        Map<String, String> header = new HashMap<>();
        header.put(Header.CONTENT_TYPE, "application/json");

        String response = Httpx.post(url, params, header);
        JSONObject jsonObject = JSON.parseObject(response);
        this.checkResponse(jsonObject);
        String appAccessToken = jsonObject.getString("app_access_token");

        this.extendCache.cache(cacheKey, appAccessToken, jsonObject.getLongValue("expire") * 1000);
        return appAccessToken;
    }

    private AccToken getToken(JSONObject params, String url) {
        Map<String, String> header = new HashMap<>();
        header.put(Header.CONTENT_TYPE, "application/json");

        String response = Httpx.post(url, params, header);

        JSONObject jsonObject = JSON.parseObject(response);
        this.checkResponse(jsonObject);
        JSONObject data = jsonObject.getJSONObject("data");
        return AccToken.builder()
                .accessToken(data.getString("access_token"))
                .refreshToken(data.getString("refresh_token"))
                .expireIn(data.getIntValue("expires_in"))
                .tokenType(data.getString("token_type"))
                .openId(data.getString("open_id"))
                .build();
    }

    /**
     * 校验响应内容是否正确
     *
     * @param jsonObject 响应内容
     */
    private void checkResponse(JSONObject jsonObject) {
        if (jsonObject.getIntValue("code") != 0) {
            throw new AuthorizedException(jsonObject.getString("message"));
        }
    }

}
