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
package org.miaixz.bus.auth.nimble.amazon;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Amazon 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AmazonProvider extends AbstractProvider {

    public AmazonProvider(Context context) {
        super(context, Registry.AMAZON);
    }

    public AmazonProvider(Context context, ExtendCache cache) {
        super(context, Registry.AMAZON, cache);
    }

    /**
     * https://developer.amazon.com/zh/docs/login-with-amazon/authorization-code-grant.html#authorization-request
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return String
     */
    @Override
    public String authorize(String state) {
        String realState = getRealState(state);
        Builder builder = Builder.fromUrl(complex.getConfig().get(Builder.AUTHORIZE))
                .queryParam("client_id", context.getAppKey())
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getDefaultScopes(AmazonScope.values())))
                .queryParam("redirect_uri", context.getRedirectUri()).queryParam("response_type", "code")
                .queryParam("state", realState);

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
     * https://developer.amazon.com/zh/docs/login-with-amazon/authorization-code-grant.html#access-token-request
     *
     * @return access token
     */
    @Override
    public AccToken getAccessToken(Callback callback) {
        Map<String, String> form = new HashMap<>(9);
        form.put("grant_type", "authorization_code");
        form.put("code", callback.getCode());
        form.put("redirect_uri", context.getRedirectUri());
        form.put("client_id", context.getAppKey());
        form.put("client_secret", context.getAppSecret());

        if (context.isPkce()) {
            String cacheKey = this.complex.getName().concat(":code_verifier:").concat(callback.getState());
            String codeVerifier = String.valueOf(this.cache.get(cacheKey));
            form.put("code_verifier", codeVerifier);
        }
        return getToken(form, this.complex.getConfig().get(Builder.ACCESSTOKEN));
    }

    @Override
    public Message refresh(AccToken accToken) {
        Map<String, String> form = new HashMap<>(7);
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", accToken.getRefreshToken());
        form.put("client_id", context.getAppKey());
        form.put("client_secret", context.getAppSecret());
        return Message.builder().errcode(ErrorCode.SUCCESS.getCode())
                .data(getToken(form, this.complex.getConfig().get(Builder.REFRESH))).build();
    }

    private AccToken getToken(Map<String, String> param, String url) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.HOST, "api.amazon.com");
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED + ";charset=UTF-8");

        String response = Httpx.post(url, param, header);
        try {
            Map<String, Object> jsonObject = JsonKit.toPojo(response, Map.class);
            if (jsonObject == null) {
                throw new AuthorizedException("Failed to parse JSON response: empty response");
            }
            this.checkResponse(jsonObject);

            String accessToken = (String) jsonObject.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String tokenType = (String) jsonObject.get("token_type");
            Object expiresInObj = jsonObject.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String refreshToken = (String) jsonObject.get("refresh_token");

            return AccToken.builder().accessToken(accessToken).tokenType(tokenType).expireIn(expiresIn)
                    .refreshToken(refreshToken).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse token response: " + e.getMessage());
        }
    }

    /**
     * 校验响应内容是否正确
     *
     * @param jsonObject 响应内容
     */
    private void checkResponse(Map<String, Object> jsonObject) {
        if (jsonObject.containsKey("error")) {
            String errorDescription = (String) jsonObject.get("error_description");
            throw new AuthorizedException(errorDescription != null ? errorDescription : "Unknown error");
        }
    }

    /**
     * https://developer.amazon.com/zh/docs/login-with-amazon/obtain-customer-profile.html#call-profile-endpoint
     *
     * @param accToken token信息
     * @return Property
     */
    @Override
    public Material getUserInfo(AccToken accToken) {
        String accessToken = accToken.getAccessToken();
        this.checkToken(accessToken);
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.HOST, "api.amazon.com");
        header.put(HTTP.AUTHORIZATION, "bearer " + accessToken);

        String userInfo = Httpx.get(this.complex.getConfig().get(Builder.USERINFO), new HashMap<>(0), header);
        try {
            Map<String, Object> jsonObject = JsonKit.toPojo(userInfo, Map.class);
            if (jsonObject == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(jsonObject);

            String userId = (String) jsonObject.get("user_id");
            if (userId == null) {
                throw new AuthorizedException("Missing user_id in response");
            }
            String name = (String) jsonObject.get("name");
            String email = (String) jsonObject.get("email");

            return Material.builder().rawJson(JsonKit.toJsonString(jsonObject)).uuid(userId).username(name)
                    .nickname(name).email(email).gender(Gender.UNKNOWN).source(complex.toString()).token(accToken)
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    private void checkToken(String accessToken) {
        String tokenInfo = Httpx
                .get("https://api.amazon.com/auth/o2/tokeninfo?access_token=" + UrlEncoder.encodeAll(accessToken));
        try {
            Map<String, Object> jsonObject = JsonKit.toPojo(tokenInfo, Map.class);
            if (jsonObject == null) {
                throw new AuthorizedException("Failed to parse token info response: empty response");
            }
            String aud = (String) jsonObject.get("aud");
            if (!context.getAppKey().equals(aud)) {
                throw new AuthorizedException(ErrorCode.ILLEGAL_TOKEN.getCode());
            }
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse token info response: " + e.getMessage());
        }
    }

    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(this.complex.getConfig().get(Builder.USERINFO))
                .queryParam("user_id", accToken.getUserId()).queryParam("screen_name", accToken.getScreenName())
                .queryParam("include_entities", true).build();
    }

}