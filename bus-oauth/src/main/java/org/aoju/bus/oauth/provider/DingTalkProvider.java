/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.http.HttpClient;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.StateCache;

import java.util.HashMap;
import java.util.Map;

/**
 * 钉钉登录
 *
 * @author Kimi Liu
 * @version 5.0.2
 * @since JDK 1.8+
 */
public class DingTalkProvider extends DefaultProvider {

    public DingTalkProvider(Context config) {
        super(config, Registry.DINGTALK);
    }

    public DingTalkProvider(Context config, StateCache stateCache) {
        super(config, Registry.DINGTALK, stateCache);
    }

    @Override
    protected AccToken getAccessToken(Callback Callback) {
        return AccToken.builder().accessCode(Callback.getCode()).build();
    }

    @Override
    protected Object getUserInfo(AccToken oauthToken) {
        String code = oauthToken.getAccessCode();
        Map param = new HashMap<>();
        param.put("tmp_auth_code", code);
        String response = HttpClient.post(userInfoUrl(oauthToken), param);
        JSONObject object = JSON.parseObject(response);
        if (object.getIntValue("errcode") != 0) {
            throw new InstrumentException(object.getString("errmsg"));
        }
        object = object.getJSONObject("user_info");
        AccToken token = AccToken.builder()
                .openId(object.getString("openid"))
                .unionId(object.getString("unionid"))
                .build();
        return Property.builder()
                .uuid(object.getString("unionid"))
                .nickname(object.getString("nick"))
                .username(object.getString("nick"))
                .gender(Normal.Gender.UNKNOWN)
                .source(source.toString())
                .token(token)
                .build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     * @since 1.9.3
     */
    @Override
    public String authorize(String state) {
        return Builder.fromBaseUrl(source.authorize())
                .queryParam("response_type", "code")
                .queryParam("appid", config.getClientId())
                .queryParam("scope", "snsapi_login")
                .queryParam("redirect_uri", config.getRedirectUri())
                .queryParam("state", getRealState(state))
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param token 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken token) {
        // 根据timestamp, appSecret计算签名值
        String timestamp = System.currentTimeMillis() + "";
        String urlEncodeSignature = generateDingTalkSignature(config.getClientSecret(), timestamp);

        return Builder.fromBaseUrl(source.userInfo())
                .queryParam("signature", urlEncodeSignature)
                .queryParam("timestamp", timestamp)
                .queryParam("accessKey", config.getClientId())
                .build();
    }

}
