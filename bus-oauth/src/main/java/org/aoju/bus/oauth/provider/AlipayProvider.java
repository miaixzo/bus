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

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import org.aoju.bus.cache.metric.ExtendCache;
import org.aoju.bus.core.exception.AuthorizedException;
import org.aoju.bus.core.lang.Algorithm;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;

/**
 * 支付宝登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AlipayProvider extends AbstractProvider {

    private final AlipayClient alipayClient;

    public AlipayProvider(Context context) {
        super(context, Registry.ALIPAY);
        this.alipayClient = new DefaultAlipayClient(Registry.ALIPAY.accessToken(), context.getAppKey(), context.getAppSecret(), "json", Charset.DEFAULT_UTF_8, context
                .getPublicKey(), Algorithm.RSA2.getValue());
    }

    public AlipayProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.ALIPAY, extendCache);
        this.alipayClient = new DefaultAlipayClient(Registry.ALIPAY.accessToken(), context.getAppKey(), context.getAppSecret(),
                "json", Charset.DEFAULT_UTF_8, context.getPublicKey(), Algorithm.RSA2.getValue());
    }

    public AlipayProvider(Context context, ExtendCache extendCache, String proxyHost, Integer proxyPort) {
        super(context, Registry.ALIPAY, extendCache);
        this.alipayClient = new DefaultAlipayClient(Registry.ALIPAY.accessToken(), context.getAppKey(), context.getAppSecret(),
                "json", Charset.DEFAULT_UTF_8, context.getPublicKey(), Algorithm.RSA2.getValue(), proxyHost, proxyPort);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
        request.setGrantType("authorization_code");
        request.setCode(callback.getAuth_code());
        AlipaySystemOauthTokenResponse response;
        try {
            response = this.alipayClient.execute(request);
        } catch (Exception e) {
            throw new AuthorizedException(e);
        }
        if (!response.isSuccess()) {
            throw new AuthorizedException(response.getSubMsg());
        }
        return AccToken.builder()
                .accessToken(response.getAccessToken())
                .uid(response.getUserId())
                .expireIn(Integer.parseInt(response.getExpiresIn()))
                .refreshToken(response.getRefreshToken())
                .build();
    }

    /**
     * 刷新access token （续期）
     *
     * @param accToken 登录成功后返回的Token信息
     * @return the message
     */
    @Override
    public Message refresh(AccToken accToken) {
        AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
        request.setGrantType("refresh_token");
        request.setRefreshToken(accToken.getRefreshToken());
        AlipaySystemOauthTokenResponse response;
        try {
            response = this.alipayClient.execute(request);
        } catch (Exception e) {
            throw new AuthorizedException(e);
        }
        if (!response.isSuccess()) {
            throw new AuthorizedException(response.getSubMsg());
        }
        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .data(AccToken.builder()
                        .accessToken(response.getAccessToken())
                        .uid(response.getUserId())
                        .expireIn(Integer.parseInt(response.getExpiresIn()))
                        .refreshToken(response.getRefreshToken())
                        .build())
                .build();
    }

    @Override
    public Property getUserInfo(AccToken accToken) {
        String accessToken = accToken.getAccessToken();
        AlipayUserInfoShareRequest request = new AlipayUserInfoShareRequest();
        AlipayUserInfoShareResponse object;
        try {
            object = this.alipayClient.execute(request, accessToken);
        } catch (AlipayApiException e) {
            throw new AuthorizedException(e.getErrMsg(), e);
        }
        if (!object.isSuccess()) {
            throw new AuthorizedException(object.getSubMsg());
        }

        String province = object.getProvince(), city = object.getCity();
        String location = String.format("%s %s", StringKit.isEmpty(province) ? Normal.EMPTY : province, StringKit.isEmpty(city) ? Normal.EMPTY : city);

        return Property.builder()
                .rawJson(JSONObject.parseObject(JSONObject.toJSONString(object)))
                .uuid(object.getUserId())
                .username(StringKit.isEmpty(object.getUserName()) ? object.getNickName() : object.getUserName())
                .nickname(object.getNickName())
                .avatar(object.getAvatar())
                .location(location)
                .gender(Normal.Gender.of(object.getGender()))
                .token(accToken)
                .source(source.toString())
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
                .queryParam("app_id", context.getAppKey())
                .queryParam("scope", "auth_user")
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state))
                .build();
    }

}
