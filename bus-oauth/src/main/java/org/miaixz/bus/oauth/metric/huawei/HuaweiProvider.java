/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org justauth.cn and other contributors.        ~
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
package org.miaixz.bus.oauth.metric.huawei;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
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

import com.alibaba.fastjson.JSONObject;

/**
 * 华为 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HuaweiProvider extends AbstractProvider {

    public HuaweiProvider(Context context) {
        super(context, Registry.HUAWEI);
    }

    public HuaweiProvider(Context context, ExtendCache cache) {
        super(context, Registry.HUAWEI, cache);
    }

    /**
     * 获取access token
     *
     * @param Callback 授权成功后的回调参数
     * @return token
     * @see AbstractProvider#authorize(String)
     */
    @Override
    public AccToken getAccessToken(Callback Callback) {
        Map<String, String> form = new HashMap<>(8);
        form.put("grant_type", "authorization_code");
        form.put("code", Callback.getCode());
        form.put("client_id", context.getAppKey());
        form.put("client_secret", context.getAppSecret());
        form.put("redirect_uri", context.getRedirectUri());

        if (context.isPkce()) {
            String cacheKey = this.complex.getName().concat(":code_verifier:").concat(Callback.getState());
            String codeVerifier = (String) this.cache.get(cacheKey);
            form.put("code_verifier", codeVerifier);
        }

        Map<String, String> header = new HashMap<>(8);
        header.put(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");

        String response = Httpx.post(complex.accessToken(), header, form);

        return getAccToken(response);
    }

    /**
     * 使用token换取用户信息
     *
     * @param AccToken token信息
     * @return 用户信息
     * @see AbstractProvider#getAccessToken(Callback)
     */
    @Override
    public Material getUserInfo(AccToken AccToken) {
        String idToken = AccToken.getIdToken();
        if (StringKit.isEmpty(idToken)) {
            Map<String, String> form = new HashMap<>(7);
            form.put("access_token", AccToken.getAccessToken());
            form.put("getNickName", "1");
            form.put("nsp_svc", "GOpen.User.getInfo");

            Map<String, String> header = new HashMap<>(7);
            header.put(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
            String response = Httpx.post(complex.userInfo(), header, form);
            JSONObject object = JSONObject.parseObject(response);

            this.checkResponse(object);

            return Material.builder().rawJson(object).uuid(object.getString("unionID"))
                    .username(object.getString("displayName")).nickname(object.getString("displayName"))
                    .gender(Gender.UNKNOWN).avatar(object.getString("headPictureURL")).token(AccToken)
                    .source(context.toString()).build();
        }
        String payload = new String(Base64.getUrlDecoder().decode(idToken.split("\\.")[1]), StandardCharsets.UTF_8);

        JSONObject object = JSONObject.parseObject(payload);
        return Material.builder().rawJson(object).uuid(object.getString("sub")).username(object.getString("name"))
                .nickname(object.getString("nickname")).gender(Gender.UNKNOWN).avatar(object.getString("picture"))
                .token(AccToken).source(complex.toString()).build();
    }

    /**
     * 刷新access token （续期）
     *
     * @param AccToken 登录成功后返回的Token信息
     * @return AuthResponse
     */
    @Override
    public Message refresh(AccToken AccToken) {
        Map<String, String> form = new HashMap<>(7);
        form.put("client_id", context.getAppKey());
        form.put("client_secret", context.getAppSecret());
        form.put("refresh_token", AccToken.getRefreshToken());
        form.put("grant_type", "refresh_token");

        Map<String, String> header = new HashMap<>(7);
        header.put(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
        String response = Httpx.post(complex.refresh(), header, form);

        return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).data(getAccToken(response)).build();
    }

    private AccToken getAccToken(String response) {
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return AccToken.builder().accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in")).refreshToken(object.getString("refresh_token"))
                .idToken(object.getString("id_token")).build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        String realState = getRealState(state);

        Builder builder = Builder.fromUrl(super.authorize(state)).queryParam("access_type", "offline")
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getDefaultScopes(HuaweiScope.values())));

        if (context.isPkce()) {
            String cacheKey = this.complex.getName().concat(":code_verifier:").concat(realState);
            String codeVerifier = Builder.codeVerifier();
            String codeChallengeMethod = "S256";
            String codeChallenge = Builder.codeChallenge(codeChallengeMethod, codeVerifier);
            builder.queryParam("code_challenge", codeChallenge).queryParam("code_challenge_method",
                    codeChallengeMethod);
            // 缓存 codeVerifier 十分钟
            this.cache.cache(cacheKey, codeVerifier, TimeUnit.MINUTES.toMillis(10));
        }
        return builder.build();
    }

    /**
     * 校验响应结果
     *
     * @param object 接口返回的结果
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("NSP_STATUS")) {
            throw new AuthorizedException(object.getString("error"));
        }
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("sub_error") + ":" + object.getString("error_description"));
        }
    }

}
