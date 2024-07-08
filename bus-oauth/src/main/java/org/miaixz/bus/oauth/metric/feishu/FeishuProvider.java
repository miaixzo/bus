/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org justauth and other contributors.           ~
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
package org.miaixz.bus.oauth.metric.feishu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basics.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 飞书 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FeishuProvider extends AbstractProvider {

    public FeishuProvider(Context context) {
        super(context, Registry.FEISHU);
    }

    public FeishuProvider(Context context, ExtendCache cache) {
        super(context, Registry.FEISHU, cache);
    }

    /**
     * 获取 app_access_token（企业自建应用）
     * <p>
     * Token 有效期为 2 小时，在此期间调用该接口 token 不会改变。当 token 有效期小于 30 分的时候，再次请求获取 token 的时候，
     * 会生成一个新的 token，与此同时老的 token 依然有效。
     *
     * @return appAccessToken
     */
    private String getAppAccessToken() {
        String cacheKey = this.complex.getName().concat(":app_access_token:").concat(context.getAppKey());
        String cacheAppAccessToken = String.valueOf(this.cache.get(cacheKey));
        if (StringKit.isNotEmpty(cacheAppAccessToken)) {
            return cacheAppAccessToken;
        }
        String url = "https://open.feishu.cn/open-apis/auth/v3/app_access_token/internal/";
        JSONObject requestObject = new JSONObject();
        requestObject.put("app_id", context.getAppKey());
        requestObject.put("app_secret", context.getAppSecret());

        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        String response = Httpx.post(url, requestObject.toJSONString(), header, MediaType.APPLICATION_JSON);
        JSONObject jsonObject = JSON.parseObject(response);
        this.checkResponse(jsonObject);
        String appAccessToken = jsonObject.getString("app_access_token");
        // 缓存 app access token
        this.cache.cache(cacheKey, appAccessToken, jsonObject.getLongValue("expire") * 1000);
        return appAccessToken;
    }

    @Override
    protected AccToken getAccessToken(Callback callback) {
        JSONObject requestObject = new JSONObject();
        requestObject.put("app_access_token", this.getAppAccessToken());
        requestObject.put("grant_type", "authorization_code");
        requestObject.put("code", callback.getCode());
        return getToken(requestObject, this.complex.accessToken());

    }

    @Override
    protected Material getUserInfo(AccToken accToken) {
        String accessToken = accToken.getAccessToken();
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        header.put("Authorization", "Bearer " + accessToken);
        String response = Httpx.get(complex.userInfo(), null, header);
        JSONObject object = JSON.parseObject(response);
        this.checkResponse(object);
        JSONObject data = object.getJSONObject("data");
        return Material.builder()
                .rawJson(object)
                .uuid(data.getString("union_id"))
                .username(data.getString("name"))
                .nickname(data.getString("name"))
                .avatar(data.getString("avatar_url"))
                .email(data.getString("email"))
                .gender(Gender.UNKNOWN)
                .token(accToken)
                .source(complex.toString())
                .build();
    }

    @Override
    public Message refresh(AccToken accToken) {
        JSONObject requestObject = new JSONObject();
        requestObject.put("app_access_token", this.getAppAccessToken());
        requestObject.put("grant_type", "refresh_token");
        requestObject.put("refresh_token", accToken.getRefreshToken());
        return Message.builder()
                .errcode(ErrorCode.SUCCESS.getCode())
                .data(getToken(requestObject, this.complex.refresh()))
                .build();

    }

    private AccToken getToken(JSONObject param, String url) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        String response = Httpx.post(url, param.toJSONString(), header, MediaType.APPLICATION_JSON);
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

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize())
                .queryParam("app_id", context.getAppKey())
                .queryParam("redirect_uri", UrlEncoder.encodeAll(context.getRedirectUri()))
                .queryParam("state", getRealState(state))
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
