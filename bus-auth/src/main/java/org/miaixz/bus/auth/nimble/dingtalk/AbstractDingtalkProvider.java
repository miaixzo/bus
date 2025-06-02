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
package org.miaixz.bus.auth.nimble.dingtalk;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Complex;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.magic.AccToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 钉钉 登录抽象类 负责处理使用钉钉账号登录第三方网站和扫码登录第三方网站两种钉钉的登录方式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractDingtalkProvider extends AbstractProvider {

    public AbstractDingtalkProvider(Context context, Complex complex) {
        super(context, complex);
    }

    public AbstractDingtalkProvider(Context context, Complex complex, ExtendCache cache) {
        super(context, complex, cache);
    }

    /**
     * 钉钉请求的签名
     *
     * @param secretKey 平台应用的授权密钥
     * @param timestamp 时间戳
     * @return Signature
     */
    public static String sign(String secretKey, String timestamp) {
        byte[] signData = Builder.sign(secretKey.getBytes(Charset.UTF_8), timestamp.getBytes(Charset.UTF_8),
                Algorithm.HMACSHA256.getValue());
        return UrlEncoder.encodeAll(new String(Base64.encode(signData, false)));
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        return AccToken.builder().accessCode(callback.getCode()).build();
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String code = accToken.getAccessCode();
        Map<String, Object> param = new HashMap<>();
        param.put("tmp_auth_code", code);
        String response = Httpx.post(userInfoUrl(accToken), JsonKit.toJsonString(param), MediaType.APPLICATION_JSON);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            Object errCodeObj = object.get("errcode");
            int errCode = errCodeObj instanceof Number ? ((Number) errCodeObj).intValue() : -1;
            if (errCode != 0) {
                String errMsg = (String) object.get("errmsg");
                throw new AuthorizedException(errMsg != null ? errMsg : "Unknown error");
            }

            Map<String, Object> userInfo = (Map<String, Object>) object.get("user_info");
            if (userInfo == null) {
                throw new AuthorizedException("Missing user_info in response");
            }

            String openId = (String) userInfo.get("openid");
            String unionId = (String) userInfo.get("unionid");
            if (unionId == null) {
                throw new AuthorizedException("Missing unionid in user_info");
            }
            String nick = (String) userInfo.get("nick");

            AccToken token = AccToken.builder().openId(openId).unionId(unionId).build();

            return Material.builder().rawJson(JsonKit.toJsonString(userInfo)).uuid(unionId).nickname(nick)
                    .username(nick).gender(Gender.UNKNOWN).source(complex.toString()).token(token).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
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
        return Builder.fromUrl(complex.getConfig().get(Builder.AUTHORIZE)).queryParam("response_type", "code")
                .queryParam("appid", context.getAppKey()).queryParam("scope", "snsapi_login")
                .queryParam("redirect_uri", context.getRedirectUri()).queryParam("state", getRealState(state)).build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        // 根据timestamp, appSecret计算签名值
        String timestamp = System.currentTimeMillis() + "";
        String urlEncodeSignature = sign(context.getAppSecret(), timestamp);

        return Builder.fromUrl(this.complex.getConfig().get(Builder.USERINFO))
                .queryParam("signature", urlEncodeSignature).queryParam("timestamp", timestamp)
                .queryParam("accessKey", context.getAppKey()).build();
    }

}