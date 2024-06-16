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
package org.miaixz.bus.oauth.metric.dingtalk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Complex;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

/**
 * 钉钉 登录抽象类
 * 负责处理使用钉钉账号登录第三方网站和扫码登录第三方网站两种钉钉的登录方式
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
        byte[] signData = Builder.sign(secretKey.getBytes(Charset.UTF_8), timestamp.getBytes(Charset.UTF_8), Algorithm.HMACSHA256.getValue());
        return UrlEncoder.encodeAll(new String(Base64.encode(signData, false)));
    }

    @Override
    protected AccToken getAccessToken(Callback callback) {
        return AccToken.builder().accessCode(callback.getCode()).build();
    }

    @Override
    protected Material getUserInfo(AccToken accToken) {
        String code = accToken.getAccessCode();
        JSONObject param = new JSONObject();
        param.put("tmp_auth_code", code);
        String response = Httpx.post(userInfoUrl(accToken), param.toJSONString(), MediaType.APPLICATION_JSON);
        JSONObject object = JSON.parseObject(response);
        if (object.getIntValue("errcode") != 0) {
            throw new AuthorizedException(object.getString("errmsg"));
        }
        object = object.getJSONObject("user_info");
        AccToken token = AccToken.builder()
                .openId(object.getString("openid"))
                .unionId(object.getString("unionid"))
                .build();
        return Material.builder()
                .rawJson(object)
                .uuid(object.getString("unionid"))
                .nickname(object.getString("nick"))
                .username(object.getString("nick"))
                .gender(Gender.UNKNOWN)
                .source(complex.toString())
                .token(token)
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
        return Builder.fromUrl(complex.authorize())
                .queryParam("response_type", "code")
                .queryParam("appid", context.getAppKey())
                .queryParam("scope", "snsapi_login")
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state))
                .build();
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

        return Builder.fromUrl(complex.userInfo())
                .queryParam("signature", urlEncodeSignature)
                .queryParam("timestamp", timestamp)
                .queryParam("accessKey", context.getAppKey())
                .build();
    }

}
