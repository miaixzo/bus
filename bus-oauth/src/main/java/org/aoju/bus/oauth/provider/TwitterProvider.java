/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.cache.metric.ExtendCache;
import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.lang.Algorithm;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.DateKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Property;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * 今日头条登录
 *
 * @author Kimi Liu
 * @version 5.9.9
 * @since JDK 1.8+
 */
public class TwitterProvider extends DefaultProvider {

    private static final String PREAMBLE = "OAuth";

    public TwitterProvider(Context context) {
        super(context, Registry.TWITTER);
    }

    public TwitterProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.TOUTIAO, extendCache);
    }

    /**
     * 生成给定长度的随机数
     *
     * @param len 长度
     * @return 随机数字符串
     */
    private static String generateNonce(int len) {
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
    private static String sign(Map<String, String> params, String method, String baseUrl, String apiSecret, String tokenSecret) {
        TreeMap<String, Object> map = new TreeMap<>();
        for (Map.Entry<String, String> e : params.entrySet()) {
            map.put(urlEncode(e.getKey()), e.getValue());
        }
        String str = parseMapToString(map, true);
        String baseStr = method.toUpperCase() + Symbol.AND + urlEncode(baseUrl) + Symbol.AND + urlEncode(str);
        String signKey = apiSecret + Symbol.AND + (StringKit.isEmpty(tokenSecret) ? Normal.EMPTY : tokenSecret);
        byte[] signature = sign(signKey.getBytes(Charset.DEFAULT), baseStr.getBytes(Charset.DEFAULT), Algorithm.HmacSHA1);

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
        return Builder.fromUrl(source.authorize())
                .queryParam("oauth_token", token.getOauthToken())
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

    /**
     * Convert request token to access token
     * https://developer.twitter.com/en/docs/twitter-for-websites/log-in-with-twitter/guides/implementing-sign-in-with-twitter
     *
     * @return access token
     */
    @Override
    protected AccToken getAccessToken(Callback authCallback) {
        Map<String, String> oauthParams = buildOauthParams();
        oauthParams.put("oauth_token", authCallback.getOauthToken());
        oauthParams.put("oauth_verifier", authCallback.getOauthVerifier());
        oauthParams.put("oauth_signature", sign(oauthParams, "POST", source.accessToken(), context.getAppSecret(), authCallback
                .getOauthToken()));

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", buildHeader(oauthParams));
        header.put("Content-Type", "application/x-www-form-urlencoded");

        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("oauth_verifier", authCallback.getOauthVerifier());

        String response = Httpx.post(source.accessToken(), queryMap, header);

        Map<String, String> requestToken = parseStringToMap(response, false);

        return AccToken.builder()
                .oauthToken(requestToken.get("oauth_token"))
                .oauthTokenSecret(requestToken.get("oauth_token_secret"))
                .userId(requestToken.get("user_id"))
                .screenName(requestToken.get("screen_name"))
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken authToken) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("user_id", authToken.getUserId());
        queryParams.put("screen_name", authToken.getScreenName());
        queryParams.put("include_entities", Boolean.toString(true));

        Map<String, String> oauthParams = buildOauthParams();
        oauthParams.put("oauth_token", authToken.getOauthToken());

        Map<String, String> params = new HashMap<>(oauthParams);
        params.putAll(queryParams);
        oauthParams.put("oauth_signature", sign(params, "GET", source.userInfo(), context.getAppSecret(), authToken
                .getOauthTokenSecret()));
        String header = buildHeader(oauthParams);

        Map<String, String> map = new HashMap<>();
        map.put("Authorization", header);
        String response = Httpx.get(userInfoUrl(authToken), null, map);
        JSONObject userInfo = JSONObject.parseObject(response);

        return Property.builder()
                .uuid(userInfo.getString("id_str"))
                .username(userInfo.getString("screen_name"))
                .nickname(userInfo.getString("name"))
                .remark(userInfo.getString("description"))
                .avatar(userInfo.getString("profile_image_url_https"))
                .blog(userInfo.getString("url"))
                .location(userInfo.getString("location"))
                .source(source.toString())
                .token(authToken)
                .build();
    }

    private Map<String, String> buildOauthParams() {
        Map<String, String> params = new HashMap<>(5);
        params.put("oauth_consumer_key", context.getAppKey());
        params.put("oauth_nonce", generateNonce(32));
        params.put("oauth_signature_method", "HMAC-SHA1");
        params.put("oauth_timestamp", Normal.EMPTY + DateKit.timestamp());
        params.put("oauth_version", "1.0");
        return params;
    }

    private String buildHeader(Map<String, String> oauthParams) {
        final StringBuilder sb = new StringBuilder(PREAMBLE + " ");

        for (Map.Entry<String, String> param : oauthParams.entrySet()) {
            sb.append(param.getKey()).append("=\"").append(urlEncode(param.getValue())).append(Symbol.C_DOUBLE_QUOTES).append(", ");
        }

        return sb.deleteCharAt(sb.length() - 2).toString();
    }

    /**
     * Obtaining a request token
     * https://developer.twitter.com/en/docs/twitter-for-websites/log-in-with-twitter/guides/implementing-sign-in-with-twitter
     *
     * @return request token
     */
    private AccToken getRequestToken() {
        String baseUrl = "https://api.twitter.com/oauth/request_token";

        Map<String, String> oauthParams = buildOauthParams();
        oauthParams.put("oauth_callback", context.getRedirectUri());
        oauthParams.put("oauth_signature", sign(oauthParams, "POST", baseUrl, context.getAppSecret(), null));

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", buildHeader(oauthParams));

        String requestToken = Httpx.post(baseUrl, null, header);

        Map<String, String> res = parseStringToMap(requestToken, false);

        return AccToken.builder()
                .oauthToken(res.get("oauth_token"))
                .oauthTokenSecret(res.get("oauth_token_secret"))
                .oauthCallbackConfirmed(Boolean.valueOf(res.get("oauth_callback_confirmed")))
                .build();
    }

}
