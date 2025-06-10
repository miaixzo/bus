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
package org.miaixz.bus.auth.nimble.huawei;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
     * @param callback 授权成功后的回调参数
     * @return token
     * @see AbstractProvider#authorize(String)
     */
    @Override
    public AccToken getAccessToken(Callback callback) {
        Map<String, String> form = new HashMap<>(8);
        form.put("grant_type", "authorization_code");
        form.put("code", callback.getCode());
        form.put("client_id", context.getAppKey());
        form.put("client_secret", context.getAppSecret());
        form.put("redirect_uri", context.getRedirectUri());

        if (context.isPkce()) {
            String cacheKey = this.complex.getName().concat(":code_verifier:").concat(callback.getState());
            String codeVerifier = (String) this.cache.get(cacheKey);
            form.put("code_verifier", codeVerifier);
        }

        Map<String, String> header = new HashMap<>(8);
        header.put(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");

        String response = Httpx.post(this.complex.getConfig().get(Builder.ACCESSTOKEN), header, form);

        return getAccToken(response);
    }

    /**
     * 使用token换取用户信息
     *
     * @param accToken token信息
     * @return 用户信息
     * @see AbstractProvider#getAccessToken(Callback)
     */
    @Override
    public Material getUserInfo(AccToken accToken) {
        String idToken = accToken.getIdToken();
        if (StringKit.isEmpty(idToken)) {
            Map<String, String> form = new HashMap<>(7);
            form.put("access_token", accToken.getAccessToken());
            form.put("getNickName", "1");
            form.put("nsp_svc", "GOpen.User.getInfo");

            Map<String, String> header = new HashMap<>(7);
            header.put(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
            String response = Httpx.post(this.complex.getConfig().get(Builder.USERINFO), header, form);
            try {
                Map<String, Object> object = JsonKit.toPojo(response, Map.class);
                if (object == null) {
                    throw new AuthorizedException("Failed to parse user info response: empty response");
                }
                this.checkResponse(object);

                String unionID = (String) object.get("unionID");
                if (unionID == null) {
                    throw new AuthorizedException("Missing unionID in user info response");
                }
                String displayName = (String) object.get("displayName");
                String headPictureURL = (String) object.get("headPictureURL");

                return Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(unionID).username(displayName)
                        .nickname(displayName).gender(Gender.UNKNOWN).avatar(headPictureURL).token(accToken)
                        .source(context.toString()).build();
            } catch (Exception e) {
                throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
            }
        }
        String payload = new String(Base64.getUrlDecoder().decode(idToken.split("\\.")[1]), StandardCharsets.UTF_8);
        try {
            Map<String, Object> object = JsonKit.toPojo(payload, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse id_token payload: empty response");
            }

            String sub = (String) object.get("sub");
            if (sub == null) {
                throw new AuthorizedException("Missing sub in id_token payload");
            }
            String name = (String) object.get("name");
            String nickname = (String) object.get("nickname");
            String picture = (String) object.get("picture");

            return Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(sub).username(name).nickname(nickname)
                    .gender(Gender.UNKNOWN).avatar(picture).token(accToken).source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse id_token payload: " + e.getMessage());
        }
    }

    /**
     * 刷新access token （续期）
     *
     * @param accToken 登录成功后返回的Token信息
     * @return AuthResponse
     */
    @Override
    public Message refresh(AccToken accToken) {
        Map<String, String> form = new HashMap<>(7);
        form.put("client_id", context.getAppKey());
        form.put("client_secret", context.getAppSecret());
        form.put("refresh_token", accToken.getRefreshToken());
        form.put("grant_type", "refresh_token");

        Map<String, String> header = new HashMap<>(7);
        header.put(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
        String response = Httpx.post(this.complex.getConfig().get(Builder.REFRESH), header, form);

        return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).data(getAccToken(response)).build();
    }

    private AccToken getAccToken(String response) {
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }
            this.checkResponse(object);

            String accessToken = (String) object.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String refreshToken = (String) object.get("refresh_token");
            String idToken = (String) object.get("id_token");

            return AccToken.builder().accessToken(accessToken).expireIn(expiresIn).refreshToken(refreshToken)
                    .idToken(idToken).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
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
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("NSP_STATUS")) {
            String error = (String) object.get("error");
            throw new AuthorizedException(error != null ? error : "Unknown error");
        }
        if (object.containsKey("error")) {
            String subError = (String) object.get("sub_error");
            String errorDescription = (String) object.get("error_description");
            throw new AuthorizedException((subError != null ? subError : "Unknown sub_error") + ":"
                    + (errorDescription != null ? errorDescription : "Unknown description"));
        }
    }

}