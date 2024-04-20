package org.miaixz.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.toolkit.RandomKit;
import org.miaixz.bus.core.toolkit.UriKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Message;
import org.miaixz.bus.oauth.magic.Property;
import org.miaixz.bus.oauth.metric.OauthScope;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AmazonProvider extends AbstractProvider {

    public AmazonProvider(Context context) {
        super(context, Registry.AMAZON);
    }

    public AmazonProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.AMAZON, extendCache);
    }

    public static String generateCodeVerifier() {
        String randomStr = RandomKit.randomString(50);
        return Base64.encodeUrlSafe(randomStr);
    }

    /**
     * 适用于 OAuth 2.0 PKCE 增强协议
     *
     * @param codeChallengeMethod s256 / plain
     * @param codeVerifier        客户端生产的校验码
     * @return code challenge
     */
    public static String generateCodeChallenge(String codeChallengeMethod, String codeVerifier) {
        if ("S256".equalsIgnoreCase(codeChallengeMethod)) {
            return new String(Base64.encode(digest(codeVerifier), true, true), Charset.US_ASCII);
        } else {
            return codeVerifier;
        }
    }

    public static byte[] digest(String text) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(Algorithm.SHA256.getValue());
            messageDigest.update(text.getBytes(Charset.UTF_8));
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * https://developer.amazon.com/zh/docs/login-with-amazon/authorization-code-grant.html#authorization-request
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return String
     */
    @Override
    public String authorize(String state) {
        Builder builder = Builder.fromUrl(source.authorize())
                .queryParam("client_id", context.getAppKey())
                .queryParam("scope", this.getScopes(" ", true, getScopes(false, OauthScope.Amazon.values())))
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("state", getRealState(state));

        if (context.isPkce()) {
            String cacheKey = this.source.getName().concat(":code_verifier:").concat(context.getAppKey());
            String codeVerifier = generateCodeVerifier();
            String codeChallengeMethod = "S256";
            String codeChallenge = generateCodeChallenge(codeChallengeMethod, codeVerifier);
            builder.queryParam("code_challenge", codeChallenge)
                    .queryParam("code_challenge_method", codeChallengeMethod);
            // 缓存 codeVerifier 十分钟
            this.extendCache.cache(cacheKey, codeVerifier, TimeUnit.MINUTES.toMillis(10));
        }
        return builder.build();
    }

    /**
     * https://developer.amazon.com/zh/docs/login-with-amazon/authorization-code-grant.html#access-token-request
     *
     * @return access token
     */
    @Override
    protected AccToken getAccessToken(Callback callback) {
        Map<String, Object> form = new HashMap<>(8);
        form.put("grant_type", "authorization_code");
        form.put("code", callback.getCode());
        form.put("redirect_uri", context.getRedirectUri());
        form.put("client_id", context.getAppKey());
        form.put("client_secret", context.getAppSecret());

        if (context.isPkce()) {
            String cacheKey = this.source.getName().concat(":code_verifier:").concat(context.getAppKey());
            String codeVerifier = (String) this.extendCache.get(cacheKey);
            form.put("code_verifier", codeVerifier);
        }
        return getToken(form, this.source.accessToken());
    }

    @Override
    public Message refresh(AccToken authToken) {
        Map<String, Object> form = new HashMap<>(6);
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", authToken.getRefreshToken());
        form.put("client_id", context.getAppKey());
        form.put("client_secret", context.getAppSecret());
        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .data(getToken(form, this.source.refresh()))
                .build();
    }

    /**
     * https://developer.amazon.com/zh/docs/login-with-amazon/obtain-customer-profile.html#call-profile-endpoint
     *
     * @param accToken token信息
     * @return AuthUser
     */
    @Override
    protected Property getUserInfo(AccToken accToken) {
        String accessToken = accToken.getAccessToken();
        this.checkToken(accessToken);

        Map<String, String> header = new HashMap<>();
        header.put(Header.HOST, "api.amazon.com");
        header.put(Header.AUTHORIZATION, "bearer " + accessToken);

        String userInfo = Httpx.get(this.source.userInfo(), new HashMap<>(0), header);
        JSONObject jsonObject = JSONObject.parseObject(userInfo);
        this.checkResponse(jsonObject);

        return Property.builder()
                .rawJson(jsonObject)
                .uuid(jsonObject.getString("user_id"))
                .username(jsonObject.getString("name"))
                .nickname(jsonObject.getString("name"))
                .email(jsonObject.getString("email"))
                .gender(Normal.Gender.UNKNOWN)
                .source(source.toString())
                .token(accToken)
                .build();
    }

    @Override
    protected String userInfoUrl(AccToken authToken) {
        return Builder.fromUrl(source.userInfo())
                .queryParam("user_id", authToken.getUserId())
                .queryParam("screen_name", authToken.getScreenName())
                .queryParam("include_entities", true)
                .build();
    }

    private void checkToken(String accessToken) {
        String tokenInfo = Httpx.get("https://api.amazon.com/auth/o2/tokeninfo?access_token=" + UriKit.encode(accessToken));
        JSONObject jsonObject = JSONObject.parseObject(tokenInfo);
        if (!context.getAppKey().equals(jsonObject.getString("aud"))) {
            throw new AuthorizedException(Builder.ErrorCode.ILLEGAL_TOKEN.getMsg());
        }
    }

    private AccToken getToken(Map<String, Object> param, String url) {
        Map<String, String> header = new HashMap<>();
        header.put(Header.HOST, "api.amazon.com");
        header.put(Header.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED + ";charset=UTF-8");

        String response = Httpx.post(url, param, header);
        JSONObject jsonObject = JSONObject.parseObject(response);
        this.checkResponse(jsonObject);
        return AccToken.builder()
                .accessToken(jsonObject.getString("access_token"))
                .tokenType(jsonObject.getString("token_type"))
                .expireIn(jsonObject.getIntValue("expires_in"))
                .refreshToken(jsonObject.getString("refresh_token"))
                .build();
    }

    /**
     * 校验响应内容是否正确
     *
     * @param jsonObject 响应内容
     */
    private void checkResponse(JSONObject jsonObject) {
        if (jsonObject.containsKey("error")) {
            throw new AuthorizedException(jsonObject.getString("error_description").concat(" ") + jsonObject.getString("error_description"));
        }
    }

}
