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

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 京东 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdProvider extends AbstractProvider {

    public JdProvider(Context context) {
        super(context, Registry.JD);
    }

    public JdProvider(Context context, ExtendCache cache) {
        super(context, Registry.JD, cache);
    }

    /**
     * 京东宙斯平台的签名字符串 宙斯签名规则过程如下: 将所有请求参数按照字母先后顺序排列，例如将access_token,app_key,method,timestamp,v
     * 排序为access_token,app_key,method,timestamp,v
     * 1.把所有参数名和参数值进行拼接，例如：access_tokenxxxapp_keyxxxmethodxxxxxxtimestampxxxxxxvx
     * 2.把appSecret夹在字符串的两端，例如：appSecret+XXXX+appSecret 3.使用MD5进行加密，再转化成大写 link:
     * http://open.jd.com/home/home#/doc/common?listId=890 link:
     * https://github.com/pingjiang/jd-open-api-sdk-src/blob/master/src/main/java/com/jd/open/api/sdk/DefaultJdClient.java
     *
     * @param appSecret 京东应用密钥
     * @param params    签名参数
     * @return 签名后的字符串
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

    @Override
    public AccToken getAccessToken(Callback callback) {
        Map<String, String> form = new HashMap<>(7);
        form.put("app_key", context.getAppKey());
        form.put("app_secret", context.getAppSecret());
        form.put("grant_type", "authorization_code");
        form.put("code", callback.getCode());
        String response = Httpx.post(this.complex.getConfig().get(Builder.ACCESSTOKEN), form);
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

            return AccToken.builder().accessToken(accessToken).expireIn(expiresIn).refreshToken(refreshToken)
                    .scope(scope).openId(openId).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * 个人用户无法申请应用 暂时只能参考官网给出的返回结果解析
     *
     * @param object 请求返回结果
     * @return data Map
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
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        if (data == null) {
            throw new AuthorizedException("Missing data in response");
        }
        return data;
    }

    @Override
    public Message refresh(AccToken oldToken) {
        Map<String, String> form = new HashMap<>(7);
        form.put("app_key", context.getAppKey());
        form.put("app_secret", context.getAppSecret());
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", oldToken.getRefreshToken());
        String response = Httpx.post(this.complex.getConfig().get(Builder.REFRESH), form);
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

            return Message
                    .builder().errcode(ErrorCode.SUCCESS.getCode()).data(AccToken.builder().accessToken(accessToken)
                            .expireIn(expiresIn).refreshToken(refreshToken).scope(scope).openId(openId).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse refresh token response: " + e.getMessage());
        }
    }

    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error_response")) {
            Map<String, Object> errorResponse = (Map<String, Object>) object.get("error_response");
            String zhDesc = errorResponse != null ? (String) errorResponse.get("zh_desc") : null;
            throw new AuthorizedException(zhDesc != null ? zhDesc : "Unknown error");
        }
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        Builder urlBuilder = Builder.fromUrl(this.complex.getConfig().get(Builder.USERINFO))
                .queryParam("access_token", accToken.getAccessToken()).queryParam("app_key", context.getAppKey())
                .queryParam("method", "jingdong.user.getUserInfoByOpenId")
                .queryParam("360buy_param_json", "{\"openId\":\"" + accToken.getOpenId() + "\"}")
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

            return Material.builder().rawJson(JsonKit.toJsonString(data)).uuid(accToken.getOpenId()).username(nickName)
                    .nickname(nickName).avatar(imageUrl).gender(Gender.of(gender)).token(accToken)
                    .source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.getConfig().get(Builder.AUTHORIZE)).queryParam("app_key", context.getAppKey())
                .queryParam("response_type", "code").queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getDefaultScopes(JdScope.values())))
                .queryParam("state", getRealState(state)).build();
    }

}