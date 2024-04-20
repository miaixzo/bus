/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Property;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * 喜马拉雅登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XmlyProvider extends AbstractProvider {

    public XmlyProvider(Context context) {
        super(context, Registry.XMLY);
    }

    public XmlyProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.XMLY, extendCache);
    }

    /**
     * 喜马拉雅签名算法
     *
     * @param params       加密参数
     * @param clientSecret 平台应用的授权key
     * @return 签名
     */
    public static String sign(Map<String, String> params, String clientSecret) {
        TreeMap<String, Object> map = new TreeMap<>(params);
        String baseStr = Base64.encode(Builder.parseMapToString(map, false));
        byte[] sign = sign(clientSecret.getBytes(Charset.UTF_8), baseStr.getBytes(Charset.UTF_8), Algorithm.HMACSHA1.getValue());
        MessageDigest md5;
        StringBuilder builder = null;
        try {
            builder = new StringBuilder();
            md5 = MessageDigest.getInstance(Algorithm.MD5.getValue());
            md5.update(sign);
            byte[] byteData = md5.digest();
            for (byte byteDatum : byteData) {
                builder.append(Integer.toString((byteDatum & 0xff) + 0x100, Normal._16).substring(1));
            }
        } catch (Exception ignored) {
        }
        return null == builder ? Normal.EMPTY : builder.toString();
    }

    /**
     * 获取access token
     *
     * @param callback 授权成功后的回调参数
     * @return 授权信息
     */
    @Override
    protected AccToken getAccessToken(Callback callback) {
        Map<String, Object> params = new HashMap<>(6);
        params.put("code", callback.getCode());
        params.put("client_id", context.getAppKey());
        params.put("client_secret", context.getAppSecret());
        params.put("device_id", context.getDeviceId());
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", context.getRedirectUri());
        String response = Httpx.post(source.accessToken(), params);
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        this.checkResponse(accessTokenObject);

        return AccToken.builder()
                .accessToken(accessTokenObject.getString("access_token"))
                .refreshToken(accessTokenObject.getString("refresh_token"))
                .expireIn(accessTokenObject.getIntValue("expires_in"))
                .uid(accessTokenObject.getString("uid"))
                .build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(source.authorize())
                .queryParam("response_type", "code")
                .queryParam("client_id", context.getAppKey())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state))
                .queryParam("client_os_type", "3")
                .queryParam("device_id", context.getDeviceId())
                .build();
    }

    /**
     * 使用token换取用户信息
     *
     * @param authToken token信息
     * @return 用户信息
     */
    @Override
    public Property getUserInfo(AccToken authToken) {
        Map<String, String> params = new TreeMap<>();
        params.put("app_key", context.getAppKey());
        params.put("client_os_type", Optional.ofNullable(context.getClientOsType()).orElse(3).toString());
        params.put("device_id", context.getDeviceId());
        params.put("pack_id", context.getPackId());
        params.put("access_token", authToken.getAccessToken());
        params.put("sig", sign(params, context.getAppSecret()));

        String response = Httpx.post(source.userInfo(), null, params);
        JSONObject object = JSONObject.parseObject(response);
        checkResponse(object);
        return Property.builder()
                .uuid(object.getString("id"))
                .nickname(object.getString("nickname"))
                .avatar(object.getString("avatar_url"))
                .rawJson(object)
                .source(source.toString())
                .token(authToken)
                .gender(Normal.Gender.UNKNOWN)
                .build();
    }

    /**
     * 校验响应结果
     *
     * @param object 接口返回的结果
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("errcode")) {
            throw new AuthorizedException(object.getString("error_no"), object.getString("error_desc"));
        }
    }

}
