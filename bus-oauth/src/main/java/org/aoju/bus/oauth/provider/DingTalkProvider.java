/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.cache.metric.ExtendCache;
import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.exception.AuthorizedException;
import org.aoju.bus.core.lang.Algorithm;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.UriKit;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Property;


/**
 * 钉钉登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DingTalkProvider extends AbstractProvider {

    public DingTalkProvider(Context context) {
        super(context, Registry.DINGTALK);
    }

    public DingTalkProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.DINGTALK, extendCache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        return AccToken.builder().accessCode(callback.getCode()).build();
    }

    @Override
    public Object getUserInfo(AccToken accToken) {
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
        return Property.builder()
                .rawJson(object)
                .uuid(object.getString("unionid"))
                .nickname(object.getString("nick"))
                .username(object.getString("nick"))
                .gender(Normal.Gender.UNKNOWN)
                .source(source.toString())
                .token(token)
                .build();
    }

    /**
     * 返回带{@code state}参数的授权url,授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数,可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(source.authorize())
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
    public String userInfoUrl(AccToken accToken) {
        // 根据timestamp, appSecret计算签名值
        String timestamp = System.currentTimeMillis() + Normal.EMPTY;

        byte[] signData = sign(context.getAppSecret().getBytes(Charset.UTF_8), timestamp.getBytes(Charset.UTF_8), Algorithm.HMACSHA256.getValue());
        String urlEncodeSignature = UriKit.encode(new String(Base64.encode(signData, false)));

        return Builder.fromUrl(source.userInfo())
                .queryParam("signature", urlEncodeSignature)
                .queryParam("timestamp", timestamp)
                .queryParam("accessKey", context.getAppKey())
                .build();
    }

}
