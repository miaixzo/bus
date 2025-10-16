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
package org.miaixz.bus.auth.nimble.jd;

import org.miaixz.bus.auth.magic.AuthToken;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * JD (Jingdong) login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdProvider extends AbstractProvider {

    /**
     * Constructs a {@code JdProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public JdProvider(Context context) {
        super(context, Registry.JD);
    }

    /**
     * Constructs a {@code JdProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public JdProvider(Context context, CacheX cache) {
        super(context, Registry.JD, cache);
    }

    /**
     * Generates the signature string for JD Zeus platform. The Zeus signature rule process is as follows: 1. Sort all
     * request parameters alphabetically, e.g., access_token, app_key, method, timestamp, v. 2. Concatenate all
     * parameter names and values, e.g., access_tokenxxxapp_keyxxxmethodxxxxxxtimestampxxxxxxvx. 3. Enclose the
     * concatenated string with appSecret on both ends, e.g., appSecret+XXXX+appSecret. 4. Use MD5 encryption and
     * convert to uppercase. Links: <a href="http://open.jd.com/home/home#/doc/common?listId=890">JD Open API Common
     * Document</a> <a href=
     * "https://github.com/pingjiang/jd-open-api-sdk-src/blob/master/src/main/java/com/jd/open/api/sdk/DefaultJdClient.java">DefaultJdClient.java
     * Source</a>
     *
     * @param appSecret the JD application secret
     * @param params    the parameters for signing
     * @return the signed string
     */
    public static String sign(String appSecret, Map<String, Object> params) {
        Map<String, Object> treeMap = new TreeMap<>(params);
        StringBuilder signBuilder = new StringBuilder(appSecret);
        for (Map.Entry<String, Object> entry : treeMap.entrySet()) {
            String name = entry.getKey();
            String value = String.valueOf(entry.getValue());
            if (StringKit.isNotEmpty(name) && StringKit.isNotEmpty(value)) {
                signBuilder.append(name).append(value);
            }
        }
        signBuilder.append(appSecret);
        return org.miaixz.bus.crypto.Builder.md5Hex(signBuilder.toString()).toUpperCase();
    }

    /**
     * Retrieves the access token from JD's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link AuthToken} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public AuthToken getAccessToken(Callback callback) {
        Map<String, String> form = new HashMap<>(7);
        form.put("app_key", context.getAppKey());
        form.put("app_secret", context.getAppSecret());
        form.put("grant_type", "authorization_code");
        form.put("code", callback.getCode());
        String response = Httpx.post(this.complex.accessToken(), form);
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
            String scope = (String) object.get("scope");
            String openId = (String) object.get("open_id");

            return AuthToken.builder().accessToken(accessToken).expireIn(expiresIn).refreshToken(refreshToken)
                    .scope(scope).openId(openId).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * Retrieves user data from the JSON object returned by the API. Note: Individual users cannot apply for
     * applications. Currently, only parsing based on the official return results is supported.
     *
     * @param object the JSON object returned by the API
     * @return a Map containing user data
     * @throws AuthorizedException if required data fields are missing in the response
     */
    private Map<String, Object> getUserDataJsonObject(Map<String, Object> object) {
        Map<String, Object> response = (Map<String, Object>) object.get("jingdong_user_getUserInfoByOpenId_response");
        if (response == null) {
            throw new AuthorizedException("Missing jingdong_user_getUserInfoByOpenId_response in response");
        }
        Map<String, Object> result = (Map<String, Object>) response.get("getuserinfobyappidandopenid_result");
        if (result == null) {
            throw new AuthorizedException("Missing getuserinfobyappidandopenid_result in response");
        }
        Map<String, Object> data = (Map<String, Object>) result.get(Consts.DATA);
        if (data == null) {
            throw new AuthorizedException("Missing data in response");
        }
        return data;
    }

    /**
     * Refreshes the access token (renews its validity).
     *
     * @param authToken the token information returned after successful login
     * @return a {@link Message} containing the refreshed token information
     * @throws AuthorizedException if parsing the response fails or an error occurs during token refresh
     */
    @Override
    public Message refresh(AuthToken authToken) {
        Map<String, String> form = new HashMap<>(7);
        form.put("app_key", context.getAppKey());
        form.put("app_secret", context.getAppSecret());
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", authToken.getRefreshToken());
        String response = Httpx.post(this.complex.refresh(), form);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse refresh token response: empty response");
            }
            this.checkResponse(object);

            String accessToken = (String) object.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in refresh response");
            }
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String refreshToken = (String) object.get("refresh_token");
            String scope = (String) object.get("scope");
            String openId = (String) object.get("open_id");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            AuthToken.builder().accessToken(accessToken).expireIn(expiresIn).refreshToken(refreshToken)
                                    .scope(scope).openId(openId).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse refresh token response: " + e.getMessage());
        }
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response contains an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error_response")) {
            Map<String, Object> errorResponse = (Map<String, Object>) object.get("error_response");
            String zhDesc = errorResponse != null ? (String) errorResponse.get("zh_desc") : null;
            throw new AuthorizedException(zhDesc != null ? zhDesc : "Unknown error");
        }
    }

    /**
     * Retrieves user information from JD's user info endpoint.
     *
     * @param authToken the {@link AuthToken} obtained after successful authorization
     * @return {@link Material} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Material getUserInfo(AuthToken authToken) {
        Builder urlBuilder = Builder.fromUrl(this.complex.userinfo())
                .queryParam("access_token", authToken.getAccessToken()).queryParam("app_key", context.getAppKey())
                .queryParam("method", "jingdong.user.getUserInfoByOpenId")
                .queryParam("360buy_param_json", "{\"openId\":\"" + authToken.getOpenId() + "\"}")
                .queryParam("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .queryParam("v", "2.0");
        urlBuilder.queryParam("sign", sign(context.getAppSecret(), urlBuilder.getReadOnlyParams()));
        String response = Httpx.post(urlBuilder.build(true));
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(object);

            Map<String, Object> data = this.getUserDataJsonObject(object);

            String nickName = (String) data.get("nickName");
            if (nickName == null) {
                throw new AuthorizedException("Missing nickName in user info response");
            }
            String imageUrl = (String) data.get("imageUrl");
            String gender = (String) data.get("gendar");

            return Material.builder().rawJson(JsonKit.toJsonString(data)).uuid(authToken.getOpenId()).username(nickName)
                    .nickname(nickName).avatar(imageUrl).gender(Gender.of(gender)).token(authToken)
                    .source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Returns the authorization URL with a {@code state} parameter. The {@code state} will be included in the
     * authorization callback.
     *
     * @param state the parameter to verify the authorization process, which can prevent CSRF attacks
     * @return the authorization URL
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize()).queryParam("app_key", context.getAppKey())
                .queryParam("response_type", "code").queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getDefaultScopes(JdScope.values())))
                .queryParam("state", getRealState(state)).build();
    }

}
