/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org justauth.cn and other contributors.        ~
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
package org.miaixz.bus.oauth.metric.alipay;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Checker;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;

/**
 * 支付宝 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AlipayProvider extends AbstractProvider {

    private static final String GATEWAY = "https://openapi.alipay.com/gateway.do";
    /**
     * 支付宝公钥：当选择支付宝登录时，该值可用 对应“RSA2(SHA256)密钥”中的“支付宝公钥”
     */
    private final AlipayClient alipayClient;

    /**
     * 构造方法，需要设置"alipayPublicKey"
     *
     * @param context 公共的OAuth配置
     * @see AlipayProvider#AlipayProvider(Context)
     */
    public AlipayProvider(Context context) {
        super(context, Registry.ALIPAY);
        check(context);
        this.alipayClient = new DefaultAlipayClient(GATEWAY, context.getAppKey(), context.getAppSecret(), "json",
                Charset.DEFAULT_UTF_8, context.getUnionId(), "RSA2");
    }

    /**
     * 构造方法，需要设置"alipayPublicKey"
     *
     * @param context 公共的OAuth配置
     * @see AlipayProvider#AlipayProvider(Context, ExtendCache)
     */
    public AlipayProvider(Context context, ExtendCache cache) {
        super(context, Registry.ALIPAY, cache);
        check(context);
        this.alipayClient = new DefaultAlipayClient(GATEWAY, context.getAppKey(), context.getAppSecret(), "json",
                Charset.DEFAULT_UTF_8, context.getUnionId(), "RSA2");
    }

    /**
     * 构造方法，需要设置"alipayPublicKey"
     *
     * @param context 公共的OAuth配置
     * @see AlipayProvider#AlipayProvider(Context, ExtendCache, java.lang.String, java.lang.Integer)
     */
    public AlipayProvider(Context context, ExtendCache cache, String proxyHost, Integer proxyPort) {
        super(context, Registry.ALIPAY, cache);
        check(context);
        this.alipayClient = new DefaultAlipayClient(GATEWAY, context.getAppKey(), context.getAppSecret(), "json",
                Charset.DEFAULT_UTF_8, context.getUnionId(), "RSA2", proxyHost, proxyPort);
    }

    protected void check(Context context) {
        Checker.checkConfig(context, Registry.ALIPAY);

        if (!StringKit.isNotEmpty(context.getUnionId())) {
            throw new AuthorizedException(ErrorCode.PARAMETER_INCOMPLETE.getCode(), Registry.ALIPAY);
        }

        // 支付宝在创建回调地址时，不允许使用localhost或者127.0.0.1
        if (Protocol.isLocalHost(context.getRedirectUri())) {
            // The redirect uri of alipay is forbidden to use localhost or 127.0.0.1
            throw new AuthorizedException(ErrorCode.ILLEGAL_REDIRECT_URI.getCode(), Registry.ALIPAY);
        }
    }

    @Override
    protected void checkCode(Callback callback) {
        if (StringKit.isEmpty(callback.getAuth_code())) {
            throw new AuthorizedException(ErrorCode.ILLEGAL_CODE.getCode(), complex);
        }
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
        return AccToken.builder().accessToken(response.getAccessToken()).uid(response.getUserId())
                .expireIn(Integer.parseInt(response.getExpiresIn())).refreshToken(response.getRefreshToken()).build();
    }

    /**
     * 刷新access token （续期）
     *
     * @param accToken 登录成功后返回的Token信息
     * @return Message
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
        return Message.builder().errcode(ErrorCode.SUCCESS.getCode())
                .data(AccToken.builder().accessToken(response.getAccessToken()).uid(response.getUserId())
                        .expireIn(Integer.parseInt(response.getExpiresIn())).refreshToken(response.getRefreshToken())
                        .build())
                .build();
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String accessToken = accToken.getAccessToken();
        AlipayUserInfoShareRequest request = new AlipayUserInfoShareRequest();
        AlipayUserInfoShareResponse response;
        try {
            response = this.alipayClient.execute(request, accessToken);
        } catch (AlipayApiException e) {
            throw new AuthorizedException(e.getErrMsg(), e);
        }
        if (!response.isSuccess()) {
            throw new AuthorizedException(response.getSubMsg());
        }

        String province = response.getProvince(), city = response.getCity();
        String location = String.format("%s %s", StringKit.isEmpty(province) ? "" : province,
                StringKit.isEmpty(city) ? "" : city);

        return Material.builder().rawJson(JSONObject.parseObject(JSONObject.toJSONString(response)))
                .uuid(response.getUserId())
                .username(StringKit.isEmpty(response.getUserName()) ? response.getNickName() : response.getUserName())
                .nickname(response.getNickName()).avatar(response.getAvatar()).location(location)
                .gender(Gender.of(response.getGender())).token(accToken).source(complex.toString()).build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize()).queryParam("app_id", context.getAppKey())
                .queryParam("scope", "auth_user").queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state)).build();
    }

}
