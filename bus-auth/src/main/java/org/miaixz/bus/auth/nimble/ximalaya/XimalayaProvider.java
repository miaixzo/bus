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
package org.miaixz.bus.auth.nimble.ximalaya;

import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Ximalaya login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XimalayaProvider extends AbstractProvider {

    /**
     * Constructs a {@code XimalayaProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public XimalayaProvider(Context context) {
        super(context, Registry.XIMALAYA);
    }

    /**
     * Constructs a {@code XimalayaProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public XimalayaProvider(Context context, CacheX cache) {
        super(context, Registry.XIMALAYA, cache);
    }

    /**
     * Generates a signature for Ximalaya requests. Reference:
     * {@code https://open.ximalaya.com/doc/detailApi?categoryId=6&articleId=69}
     *
     * @param params       the parameters to be signed
     * @param clientSecret the application secret key of the platform
     * @return the generated signature
     */
    private static String sign(Map<String, String> params, String clientSecret) {
        TreeMap<String, String> map = new TreeMap<>(params);
        String baseStr = Base64.encode(Builder.parseMapToString(map, false));
        byte[] sign = Builder.sign(
                clientSecret.getBytes(Charset.UTF_8),
                baseStr.getBytes(Charset.UTF_8),
                Algorithm.HMACSHA1.getValue());
        MessageDigest md5;
        StringBuilder builder = null;
        try {
            builder = new StringBuilder();
            md5 = MessageDigest.getInstance("MD5");
            md5.update(sign);
            byte[] byteData = md5.digest();
            for (byte byteDatum : byteData) {
                builder.append(Integer.toString((byteDatum & 0xff) + 0x100, 16).substring(1));
            }
        } catch (Exception ignored) {
        }
        return null == builder ? "" : builder.toString();
    }

    /**
     * Retrieves the access token from Ximalaya's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @see AbstractProvider#build(String)
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        Map<String, String> map = new HashMap<>(9);
        map.put("code", callback.getCode());
        map.put("client_id", context.getClientId());
        map.put("client_secret", context.getClientSecret());
        map.put("device_id", context.getDeviceId());
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", context.getRedirectUri());
        String response = Httpx.post(this.complex.token(), map);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }
            this.checkResponse(object);

            String token = (String) object.get("access_token");
            if (token == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String refresh = (String) object.get("refresh_token");
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String uid = (String) object.get("uid");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(Authorization.builder().token(token).refresh(refresh).expireIn(expiresIn).uid(uid).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
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
    public Message build(String state) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Builder.fromUrl(complex.authorize()).queryParam("response_type", "code")
                        .queryParam("client_id", context.getClientId())
                        .queryParam("redirect_uri", context.getRedirectUri()).queryParam("state", getRealState(state))
                        .queryParam("client_os_type", "3").queryParam("device_id", context.getDeviceId()).build())
                .build();
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response indicates an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey(Consts.ERRCODE)) {
            String errorNo = String.valueOf(object.get("error_no"));
            String errorDesc = (String) object.get("error_desc");
            throw new AuthorizedException(errorNo, errorDesc != null ? errorDesc : "Unknown error");
        }
    }

    /**
     * Retrieves user information from Ximalaya's user info endpoint.
     *
     * @param authorization the token information
     * @return {@link Material} containing the user's information
     * @see AbstractProvider#token(Callback)
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        Map<String, String> map = new TreeMap<>();
        map.put("app_key", context.getClientId());
        map.put("client_os_type", (String) ObjectKit.defaultIfNull(context.getType(), Normal._3));
        map.put("device_id", context.getDeviceId());
        map.put("pack_id", context.getUnionId());
        map.put("access_token", authorization.getToken());
        map.put("sig", sign(map, context.getClientSecret()));
        String rawUserInfo = Httpx.get(this.complex.userinfo(), map);
        try {
            Map<String, Object> object = JsonKit.toPojo(rawUserInfo, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            checkResponse(object);

            String id = (String) object.get("id");
            if (id == null) {
                throw new AuthorizedException("Missing id in user info response");
            }
            String nickname = (String) object.get("nickname");
            String avatarUrl = (String) object.get("avatar_url");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Material.builder().uuid(id).nickname(nickname).avatar(avatarUrl)
                                    .rawJson(JsonKit.toJsonString(object)).source(complex.toString())
                                    .token(authorization).gender(Gender.UNKNOWN).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

}
