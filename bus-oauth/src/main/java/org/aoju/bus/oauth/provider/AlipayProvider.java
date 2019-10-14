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

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.StateCache;

/**
 * 支付宝登录
 *
 * @author Kimi Liu
 * @version 5.0.2
 * @since JDK 1.8+
 */
public class AlipayProvider extends DefaultProvider {

    private AlipayClient alipayClient;

    public AlipayProvider(Context config) {
        super(config, Registry.ALIPAY);
        this.alipayClient = new DefaultAlipayClient(Registry.ALIPAY.accessToken(), config.getClientId(), config.getClientSecret(), "json", "UTF-8", config
                .getAlipayPublicKey(), "RSA2");
    }

    public AlipayProvider(Context config, StateCache stateCache) {
        super(config, Registry.ALIPAY, stateCache);
        this.alipayClient = new DefaultAlipayClient(Registry.ALIPAY.accessToken(), config.getClientId(), config.getClientSecret(), "json", "UTF-8", config
                .getAlipayPublicKey(), "RSA2");
    }

    @Override
    protected AccToken getAccessToken(Callback Callback) {
        AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
        request.setGrantType("authorization_code");
        request.setCode(Callback.getAuth_code());
        AlipaySystemOauthTokenResponse response;
        try {
            response = this.alipayClient.execute(request);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
        if (!response.isSuccess()) {
            throw new InstrumentException(response.getSubMsg());
        }
        return AccToken.builder()
                .accessToken(response.getAccessToken())
                .uid(response.getUserId())
                .expireIn(Integer.parseInt(response.getExpiresIn()))
                .refreshToken(response.getRefreshToken())
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken token) {
        String accessToken = token.getAccessToken();
        AlipayUserInfoShareRequest request = new AlipayUserInfoShareRequest();
        AlipayUserInfoShareResponse response;
        try {
            response = this.alipayClient.execute(request, accessToken);
        } catch (AlipayApiException e) {
            throw new InstrumentException(e.getErrMsg(), e);
        }
        if (!response.isSuccess()) {
            throw new InstrumentException(response.getSubMsg());
        }

        String province = response.getProvince(), city = response.getCity();
        String location = String.format("%s %s", StringUtils.isEmpty(province) ? "" : province, StringUtils.isEmpty(city) ? "" : city);

        return Property.builder()
                .uuid(response.getUserId())
                .username(StringUtils.isEmpty(response.getUserName()) ? response.getNickName() : response.getUserName())
                .nickname(response.getNickName())
                .avatar(response.getAvatar())
                .location(location)
                .gender(Normal.Gender.getGender(response.getGender()))
                .token(token)
                .source(source.toString())
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
                .queryParam("app_id", config.getClientId())
                .queryParam("scope", "auth_user")
                .queryParam("redirect_uri", config.getRedirectUri())
                .queryParam("state", getRealState(state))
                .build();
    }

}
