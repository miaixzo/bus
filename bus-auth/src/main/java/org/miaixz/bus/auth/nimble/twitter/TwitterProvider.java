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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
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

/**
 * Twitter login provider, supporting OAuth 1.0a authentication flow. Implements Twitter's single sign-on to obtain user
 * access tokens and user information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TwitterProvider extends AbstractProvider {

    private static final String PREAMBLE = "OAuth";

    /**
     * Constructs a {@code TwitterProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public TwitterProvider(Context context) {
        super(context, Registry.TWITTER);
    }

    /**
     * Constructs a {@code TwitterProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public TwitterProvider(Context context, CacheX cache) {
        super(context, Registry.TWITTER, cache);
    }

    /**
     * Generates a random nonce of the specified length.
     *
     * @param len the length of the nonce
     * @return the generated nonce string
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
     * Generates a Twitter signature. Reference:
     * https://developer.twitter.com/en/docs/basics/authentication/guides/creating-a-signature
     *
     * @param params      parameters including OAuth headers, query parameters, and form parameters
     * @param method      the HTTP method
     * @param baseUrl     the base URL
     * @param apiSecret   the API secret (viewable in the developer portal)
     * @param tokenSecret the OAuth token secret
     * @return the Base64 encoded signature string
     */
    public static String sign(
            Map<String, String> params,
            String method,
            String baseUrl,
            String apiSecret,
            String tokenSecret) {
        TreeMap<String, String> map = new TreeMap<>(params);

        String text = Builder.parseMapToString(map, true);
        String baseStr = method.toUpperCase() + Symbol.AND + UrlEncoder.encodeAll(baseUrl) + Symbol.AND
                + UrlEncoder.encodeAll(text);
        String signKey = apiSecret + Symbol.AND + (StringKit.isEmpty(tokenSecret) ? "" : tokenSecret);
        byte[] signature = Builder
                .sign(signKey.getBytes(Charset.UTF_8), baseStr.getBytes(Charset.UTF_8), Algorithm.HMACSHA1.getValue());

        return new String(Base64.encode(signature, false));
    }

    /**
     * Returns the authorization URL with a state parameter, which will be carried during the callback.
     *
     * @param state the state parameter, used to prevent CSRF attacks
     * @return the authorization URL
     */
    @Override
    public Message build(String state) {
        Authorization token = this.getRequestToken();
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Builder.fromUrl(this.complex.authorize()).queryParam("oauth_token", token.getOauthToken()).build())
                .build();
    }

    /**
     * Retrieves the request token. Reference:
     * https://developer.twitter.com/en/docs/twitter-for-websites/log-in-with-twitter/guides/implementing-sign-in-with-twitter
     *
     * @return the request token object
     */
    public Authorization getRequestToken() {
        String baseUrl = "https://api.twitter.com/oauth/request_token";

        Map<String, String> form = buildOauthParams();
        form.put("oauth_callback", context.getRedirectUri());
        form.put("oauth_signature", sign(form, "POST", baseUrl, context.getClientSecret(), null));

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", buildHeader(form));
        header.put("User-Agent", "'Httpx' HTTP Client Simple-Http");
        String requestToken = Httpx.post(baseUrl, null, header);

        Map<String, String> res = Builder.parseStringToMap(requestToken);

        return Authorization.builder().oauthToken(res.get("oauth_token"))
                .oauthTokenSecret(res.get("oauth_token_secret"))
                .oauthCallbackConfirmed(Boolean.valueOf(res.get("oauth_callback_confirmed"))).build();
    }

    /**
     * Converts the request token to an access token. Reference:
     * https://developer.twitter.com/en/docs/twitter-for-websites/log-in-with-twitter/guides/implementing-sign-in-with-twitter
     *
     * @param callback the callback data
     * @return the access token object
     */
    @Override
    public Message token(Callback callback) {
        Map<String, String> headerMap = buildOauthParams();
        headerMap.put("oauth_token", callback.getOauth_token());
        headerMap.put("oauth_verifier", callback.getOauth_verifier());
        headerMap.put(
                "oauth_signature",
                sign(headerMap, "POST", this.complex.token(), context.getClientSecret(), callback.getOauth_token()));

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", buildHeader(headerMap));
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, String> form = new HashMap<>(3);
        form.put("oauth_verifier", callback.getOauth_verifier());
        String response = Httpx.post(this.complex.token(), form, header);

        Map<String, String> requestToken = Builder.parseStringToMap(response);

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Authorization.builder().oauthToken(requestToken.get("oauth_token"))
                        .oauthTokenSecret(requestToken.get("oauth_token_secret")).userId(requestToken.get("user_id"))
                        .screenName(requestToken.get("screen_name")).build())
                .build();
    }

    /**
     * Retrieves user information.
     *
     * @param authorization the access token
     * @return the user information object
     * @throws IllegalArgumentException if parsing user information fails
     */
    @Override
    public Message userInfo(Authorization authorization) {
        Map<String, String> form = buildOauthParams();
        form.put("oauth_token", authorization.getOauthToken());

        Map<String, String> params = new HashMap<>(form);
        params.put("include_entities", Boolean.toString(true));
        params.put("include_email", Boolean.toString(true));

        form.put(
                "oauth_signature",
                sign(
                        params,
                        "GET",
                        this.complex.userinfo(),
                        context.getClientSecret(),
                        authorization.getOauthTokenSecret()));

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", buildHeader(form));
        String response = Httpx.get(userInfoUrl(authorization), null, header);

        // Parse JSON response using JsonKit
        Map<String, Object> userInfo = JsonKit.toPojo(response, Map.class);

        // Validate response data
        if (userInfo == null || userInfo.isEmpty()) {
            throw new IllegalArgumentException("Failed to parse user info from response: " + response);
        }

        // Convert user information to JSON string
        String rawJson = JsonKit.toJsonString(userInfo);

        // Build user information object
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Material.builder().rawJson(rawJson).uuid((String) userInfo.get("id_str"))
                        .username((String) userInfo.get("screen_name")).nickname((String) userInfo.get("name"))
                        .remark((String) userInfo.get("description"))
                        .avatar((String) userInfo.get("profile_image_url_https")).blog((String) userInfo.get("url"))
                        .location((String) userInfo.get("location")).avatar((String) userInfo.get("profile_image_url"))
                        .email((String) userInfo.get("email")).source(complex.getName()).token(authorization).build())
                .build();
    }

    /**
     * Builds OAuth parameters.
     *
     * @return a map of OAuth parameters
     */
    private Map<String, String> buildOauthParams() {
        Map<String, String> params = new HashMap<>(12);
        params.put("oauth_consumer_key", context.getClientId());
        params.put("oauth_nonce", generateNonce(32));
        params.put("oauth_signature_method", "HMAC-SHA1");
        params.put("oauth_timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("oauth_version", "1.0");
        return params;
    }

    /**
     * Builds the OAuth authorization header.
     *
     * @param oauthParams OAuth parameters
     * @return the authorization header string
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
     * Constructs the user information URL.
     *
     * @param authorization the access token
     * @return the user information URL
     */
    @Override
    protected String userInfoUrl(Authorization authorization) {
        return Builder.fromUrl(this.complex.userinfo()).queryParam("include_entities", true)
                .queryParam("include_email", true).build();
    }

}
