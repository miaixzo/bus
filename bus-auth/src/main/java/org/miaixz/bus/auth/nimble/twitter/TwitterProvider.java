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
package org.miaixz.bus.auth.nimble.twitter;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.AccToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Twitter 登录提供者，支持 OAuth 1.0a 认证流程。 实现 Twitter 的单点登录，获取用户访问令牌和用户信息。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TwitterProvider extends AbstractProvider {

    private static final String PREAMBLE = "OAuth";

    /**
     * 使用默认缓存构造 Twitter 提供者。
     *
     * @param context 上下文配置
     */
    public TwitterProvider(Context context) {
        super(context, Registry.TWITTER);
    }

    /**
     * 使用指定缓存构造 Twitter 提供者。
     *
     * @param context 上下文配置
     * @param cache   缓存实现
     */
    public TwitterProvider(Context context, ExtendCache cache) {
        super(context, Registry.TWITTER, cache);
    }

    /**
     * 生成指定长度的随机 nonce。
     *
     * @param len 长度
     * @return nonce 字符串
     */
    public static String generateNonce(int len) {
        String s = "0123456789QWERTYUIOPLKJHGFDSAZXCVBNMqwertyuioplkjhgfdsazxcvbnm";
        Random rng = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int index = rng.nextInt(62);
            sb.append(s, index, index + 1);
        }
        return sb.toString();
    }

    /**
     * 生成 Twitter 签名。 参考：https://developer.twitter.com/en/docs/basics/authentication/guides/creating-a-signature
     *
     * @param params      参数，包括 OAuth 头、查询参数、表单参数
     * @param method      HTTP 方法
     * @param baseUrl     基础 URL
     * @param apiSecret   API 密钥（可在开发者门户查看）
     * @param tokenSecret OAuth 令牌密钥
     * @return Base64 编码的签名字符串
     */
    public static String sign(Map<String, String> params, String method, String baseUrl, String apiSecret,
            String tokenSecret) {
        TreeMap<String, String> map = new TreeMap<>(params);

        String text = Builder.parseMapToString(map, true);
        String baseStr = method.toUpperCase() + Symbol.AND + UrlEncoder.encodeAll(baseUrl) + Symbol.AND
                + UrlEncoder.encodeAll(text);
        String signKey = apiSecret + Symbol.AND + (StringKit.isEmpty(tokenSecret) ? "" : tokenSecret);
        byte[] signature = Builder.sign(signKey.getBytes(Charset.UTF_8), baseStr.getBytes(Charset.UTF_8),
                Algorithm.HMACSHA1.getValue());

        return new String(Base64.encode(signature, false));
    }

    /**
     * 返回带状态参数的授权 URL，回调时会携带该状态。
     *
     * @param state 状态参数，用于防止 CSRF 攻击
     * @return 授权 URL
     */
    @Override
    public String authorize(String state) {
        AccToken token = this.getRequestToken();
        return Builder.fromUrl(complex.getConfig().get("authorize")).queryParam("oauth_token", token.getOauthToken())
                .build();
    }

    /**
     * 获取请求令牌（Request Token）。
     * 参考：https://developer.twitter.com/en/docs/twitter-for-websites/log-in-with-twitter/guides/implementing-sign-in-with-twitter
     *
     * @return 请求令牌对象
     */
    public AccToken getRequestToken() {
        String baseUrl = "https://api.twitter.com/oauth/request_token";

        Map<String, String> form = buildOauthParams();
        form.put("oauth_callback", context.getRedirectUri());
        form.put("oauth_signature", sign(form, "POST", baseUrl, context.getAppSecret(), null));

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", buildHeader(form));
        header.put("User-Agent", "'Httpx' HTTP Client Simple-Http");
        String requestToken = Httpx.post(baseUrl, null, header);

        Map<String, String> res = Builder.parseStringToMap(requestToken);

        return AccToken.builder().oauthToken(res.get("oauth_token")).oauthTokenSecret(res.get("oauth_token_secret"))
                .oauthCallbackConfirmed(Boolean.valueOf(res.get("oauth_callback_confirmed"))).build();
    }

    /**
     * 将请求令牌转换为访问令牌（Access Token）。
     * 参考：https://developer.twitter.com/en/docs/twitter-for-websites/log-in-with-twitter/guides/implementing-sign-in-with-twitter
     *
     * @param callback 回调数据
     * @return 访问令牌对象
     */
    @Override
    public AccToken getAccessToken(Callback callback) {
        Map<String, String> headerMap = buildOauthParams();
        headerMap.put("oauth_token", callback.getOauth_token());
        headerMap.put("oauth_verifier", callback.getOauth_verifier());
        headerMap.put("oauth_signature", sign(headerMap, "POST", complex.getConfig().get("accessToken"),
                context.getAppSecret(), callback.getOauth_token()));

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", buildHeader(headerMap));
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, String> form = new HashMap<>(3);
        form.put("oauth_verifier", callback.getOauth_verifier());
        String response = Httpx.post(complex.getConfig().get("accessToken"), form, header);

        Map<String, String> requestToken = Builder.parseStringToMap(response);

        return AccToken.builder().oauthToken(requestToken.get("oauth_token"))
                .oauthTokenSecret(requestToken.get("oauth_token_secret")).userId(requestToken.get("user_id"))
                .screenName(requestToken.get("screen_name")).build();
    }

    /**
     * 获取用户信息。
     *
     * @param accToken 访问令牌
     * @return 用户信息对象
     * @throws IllegalArgumentException 如果解析用户信息失败
     */
    @Override
    public Material getUserInfo(AccToken accToken) {
        Map<String, String> form = buildOauthParams();
        form.put("oauth_token", accToken.getOauthToken());

        Map<String, String> params = new HashMap<>(form);
        params.put("include_entities", Boolean.toString(true));
        params.put("include_email", Boolean.toString(true));

        form.put("oauth_signature", sign(params, "GET", complex.getConfig().get("userInfo"), context.getAppSecret(),
                accToken.getOauthTokenSecret()));

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", buildHeader(form));
        String response = Httpx.get(userInfoUrl(accToken), null, header);

        // 使用 JsonKit 解析 JSON 响应
        Map<String, Object> userInfo = JsonKit.toPojo(response, Map.class);

        // 验证响应数据
        if (userInfo == null || userInfo.isEmpty()) {
            throw new IllegalArgumentException("Failed to parse user info from response: " + response);
        }

        // 将用户信息转换为 JSON 字符串
        String rawJson = JsonKit.toJsonString(userInfo);

        // 构建用户信息对象
        return Material.builder().rawJson(rawJson).uuid((String) userInfo.get("id_str"))
                .username((String) userInfo.get("screen_name")).nickname((String) userInfo.get("name"))
                .remark((String) userInfo.get("description")).avatar((String) userInfo.get("profile_image_url_https"))
                .blog((String) userInfo.get("url")).location((String) userInfo.get("location"))
                .avatar((String) userInfo.get("profile_image_url")).email((String) userInfo.get("email"))
                .source(complex.getName()).token(accToken).build();
    }

    /**
     * 构建 OAuth 参数。
     *
     * @return OAuth 参数映射
     */
    private Map<String, String> buildOauthParams() {
        Map<String, String> params = new HashMap<>(12);
        params.put("oauth_consumer_key", context.getAppKey());
        params.put("oauth_nonce", generateNonce(32));
        params.put("oauth_signature_method", "HMAC-SHA1");
        params.put("oauth_timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("oauth_version", "1.0");
        return params;
    }

    /**
     * 构建 OAuth 授权头。
     *
     * @param oauthParams OAuth 参数
     * @return 授权头字符串
     */
    private String buildHeader(Map<String, String> oauthParams) {
        final StringBuilder sb = new StringBuilder(PREAMBLE + Symbol.SPACE);

        for (Map.Entry<String, String> param : oauthParams.entrySet()) {
            sb.append(param.getKey()).append("=\"").append(UrlEncoder.encodeAll(param.getValue())).append('"')
                    .append(", ");
        }

        return sb.deleteCharAt(sb.length() - 2).toString();
    }

    /**
     * 构造用户信息 URL。
     *
     * @param accToken 访问令牌
     * @return 用户信息 URL
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.getConfig().get("userInfo")).queryParam("include_entities", true)
                .queryParam("include_email", true).build();
    }

}