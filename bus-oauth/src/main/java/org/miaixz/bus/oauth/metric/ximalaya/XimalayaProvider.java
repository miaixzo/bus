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
package org.miaixz.bus.oauth.metric.ximalaya;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 喜马拉雅 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XimalayaProvider extends AbstractProvider {

    public XimalayaProvider(Context context) {
        super(context, Registry.XIMALAYA);
    }

    public XimalayaProvider(Context context, ExtendCache cache) {
        super(context, Registry.XIMALAYA, cache);
    }

    /**
     * 签名算法 {@code https://open.ximalaya.com/doc/detailApi?categoryId=6&articleId=69}
     *
     * @param params       加密参数
     * @param clientSecret 平台应用的授权key
     * @return Signature
     */
    private static String sign(Map<String, String> params, String clientSecret) {
        TreeMap<String, String> map = new TreeMap<>(params);
        String baseStr = Base64.encode(Builder.parseMapToString(map, false));
        byte[] sign = Builder.sign(clientSecret.getBytes(Charset.UTF_8), baseStr.getBytes(Charset.UTF_8),
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
     * 获取access token
     *
     * @param callback 授权成功后的回调参数
     * @return token
     * @see AbstractProvider#authorize(String)
     */
    @Override
    public AccToken getAccessToken(Callback callback) {
        Map<String, String> map = new HashMap<>(9);
        map.put("code", callback.getCode());
        map.put("client_id", context.getAppKey());
        map.put("client_secret", context.getAppSecret());
        map.put("device_id", context.getDeviceId());
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", context.getRedirectUri());
        String response = Httpx.post(complex.accessToken(), map);
        try {
            Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
            if (accessTokenObject == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }
            this.checkResponse(accessTokenObject);

            String accessToken = (String) accessTokenObject.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String refreshToken = (String) accessTokenObject.get("refresh_token");
            Object expiresInObj = accessTokenObject.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String uid = (String) accessTokenObject.get("uid");

            return AccToken.builder().accessToken(accessToken).refreshToken(refreshToken).expireIn(expiresIn).uid(uid)
                    .build();
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
        return Builder.fromUrl(complex.authorize()).queryParam("response_type", "code")
                .queryParam("client_id", context.getAppKey()).queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state)).queryParam("client_os_type", "3")
                .queryParam("device_id", context.getDeviceId()).build();
    }

    /**
     * 校验响应结果
     *
     * @param object 接口返回的结果
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("errcode")) {
            String errorNo = String.valueOf(object.get("error_no"));
            String errorDesc = (String) object.get("error_desc");
            throw new AuthorizedException(errorNo, errorDesc != null ? errorDesc : "Unknown error");
        }
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
        Map<String, String> map = new TreeMap<>();
        map.put("app_key", context.getAppKey());
        map.put("client_os_type", (String) ObjectKit.defaultIfNull(context.getType(), Normal._3));
        map.put("device_id", context.getDeviceId());
        map.put("pack_id", context.getUnionId());
        map.put("access_token", accToken.getAccessToken());
        map.put("sig", sign(map, context.getAppSecret()));
        String rawUserInfo = Httpx.get(complex.userInfo(), map);
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

            return Material.builder().uuid(id).nickname(nickname).avatar(avatarUrl)
                    .rawJson(JsonKit.toJsonString(object)).source(complex.toString()).token(accToken)
                    .gender(Gender.UNKNOWN).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

}