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
package org.miaixz.bus.oauth.metric.twitter;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Twitter 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TwitterProvider extends AbstractProvider {

    private static final String PREAMBLE = "OAuth";

    public TwitterProvider(Context context) {
        super(context, Registry.TWITTER);
    }

    public TwitterProvider(Context context, ExtendCache cache) {
        super(context, Registry.TWITTER, cache);
    }

    /**
     * Generate nonce with given length
     *
     * @param len length
     * @return nonce string
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
     * Generate Twitter signature
     * https://developer.twitter.com/en/docs/basics/authentication/guides/creating-a-signature
     *
     * @param params      parameters including: oauth headers, query params, body params
     * @param method      HTTP method
     * @param baseUrl     base url
     * @param apiSecret   api key secret can be found in the developer portal by viewing the app details page
     * @param tokenSecret oauth token secret
     * @return BASE64 encoded signature string
     */
    public static String sign(Map<String, String> params, String method, String baseUrl, String apiSecret, String tokenSecret) {
        TreeMap<String, String> map = new TreeMap<>(params);
        String str = Builder.parseMapToString(map, true);
        String baseStr = method.toUpperCase() + Symbol.AND + UrlEncoder.encodeAll(baseUrl) + Symbol.AND + UrlEncoder.encodeAll(str);
        String signKey = apiSecret + Symbol.AND + (StringKit.isEmpty(tokenSecret) ? "" : tokenSecret);
        byte[] signature = Builder.sign(signKey.getBytes(Charset.UTF_8), baseStr.getBytes(Charset.UTF_8), Algorithm.HMACSHA1.getValue());

        return new String(Base64.encode(signature, false));
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        AccToken token = this.getRequestToken();
        return Builder.fromUrl(complex.authorize())
                .queryParam("oauth_token", token.getOauthToken())
                .build();
    }

    /**
     * Obtaining a provider token
     * https://developer.twitter.com/en/docs/twitter-for-websites/log-in-with-twitter/guides/implementing-sign-in-with-twitter
     *
     * @return provider token
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

        return AccToken.builder()
                .oauthToken(res.get("oauth_token"))
                .oauthTokenSecret(res.get("oauth_token_secret"))
                .oauthCallbackConfirmed(Boolean.valueOf(res.get("oauth_callback_confirmed")))
                .build();
    }

    /**
     * Convert provider token to access token
     * https://developer.twitter.com/en/docs/twitter-for-websites/log-in-with-twitter/guides/implementing-sign-in-with-twitter
     *
     * @return access token
     */
    @Override
    protected AccToken getAccessToken(Callback callback) {
        Map<String, String> headerMap = buildOauthParams();
        headerMap.put("oauth_token", callback.getOauth_token());
        headerMap.put("oauth_verifier", callback.getOauth_verifier());
        headerMap.put("oauth_signature", sign(headerMap, "POST", complex.accessToken(), context.getAppSecret(), callback
                .getOauth_token()));

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", buildHeader(headerMap));
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, String> form = new HashMap<>(3);
        form.put("oauth_verifier", callback.getOauth_verifier());
        String response = Httpx.post(complex.accessToken(), form, header);

        Map<String, String> requestToken = Builder.parseStringToMap(response);

        return AccToken.builder()
                .oauthToken(requestToken.get("oauth_token"))
                .oauthTokenSecret(requestToken.get("oauth_token_secret"))
                .userId(requestToken.get("user_id"))
                .screenName(requestToken.get("screen_name"))
                .build();
    }

    @Override
    protected Material getUserInfo(AccToken accToken) {
        Map<String, String> form = buildOauthParams();
        form.put("oauth_token", accToken.getOauthToken());

        Map<String, String> params = new HashMap<>(form);
        params.put("include_entities", Boolean.toString(true));
        params.put("include_email", Boolean.toString(true));

        form.put("oauth_signature", sign(params, "GET", complex.userInfo(), context.getAppSecret(), accToken.getOauthTokenSecret()));

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", buildHeader(form));
        String response = Httpx.get(userInfoUrl(accToken), null, header);
        JSONObject userInfo = JSONObject.parseObject(response);

        return Material.builder()
                .rawJson(userInfo)
                .uuid(userInfo.getString("id_str"))
                .username(userInfo.getString("screen_name"))
                .nickname(userInfo.getString("name"))
                .remark(userInfo.getString("description"))
                .avatar(userInfo.getString("profile_image_url_https"))
                .blog(userInfo.getString("url"))
                .location(userInfo.getString("location"))
                .avatar(userInfo.getString("profile_image_url"))
                .email(userInfo.getString("email"))
                .source(complex.toString())
                .token(accToken)
                .build();
    }

    private Map<String, String> buildOauthParams() {
        Map<String, String> params = new HashMap<>(12);
        params.put("oauth_consumer_key", context.getAppKey());
        params.put("oauth_nonce", generateNonce(32));
        params.put("oauth_signature_method", "HMAC-SHA1");
        params.put("oauth_timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("oauth_version", "1.0");
        return params;
    }

    private String buildHeader(Map<String, String> oauthParams) {
        final StringBuilder sb = new StringBuilder(PREAMBLE + Symbol.SPACE);

        for (Map.Entry<String, String> param : oauthParams.entrySet()) {
            sb.append(param.getKey()).append("=\"").append(UrlEncoder.encodeAll(param.getValue())).append('"').append(", ");
        }

        return sb.deleteCharAt(sb.length() - 2).toString();
    }

    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.userInfo())
                .queryParam("include_entities", true)
                .queryParam("include_email", true)
                .build();
    }

}
