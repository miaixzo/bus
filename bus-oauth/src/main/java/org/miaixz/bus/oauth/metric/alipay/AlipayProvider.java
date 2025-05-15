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
package org.miaixz.bus.oauth.metric.alipay;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Checker;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

/**
 * 支付宝 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AlipayProvider extends AbstractProvider {

    private static final String GATEWAY = "https://openapi.alipay.com/gateway.do";

    public AlipayProvider(Context context) {
        super(context, Registry.ALIPAY);
        check(context);
    }

    public AlipayProvider(Context context, ExtendCache cache) {
        super(context, Registry.ALIPAY, cache);
        check(context);
    }

    public AlipayProvider(Context context, ExtendCache cache, String proxyHost, Integer proxyPort) {
        super(context, Registry.ALIPAY, cache);
        check(context);
    }

    protected void check(Context context) {
        Checker.checkConfig(context, Registry.ALIPAY);

        if (!StringKit.isNotEmpty(context.getUnionId())) {
            throw new AuthorizedException(ErrorCode.PARAMETER_INCOMPLETE.getCode(), Registry.ALIPAY);
        }

        if (Protocol.isLocalHost(context.getRedirectUri())) {
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
        Map<String, String> params = new HashMap<>();
        params.put("app_id", context.getAppKey());
        params.put("method", "alipay.system.oauth.token");
        params.put("charset", Charset.DEFAULT_UTF_8);
        params.put("sign_type", "RSA2");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("version", "1.0");
        params.put("grant_type", "authorization_code");
        params.put("code", callback.getAuth_code());

        String response = Httpx.post(GATEWAY, params);
        Map<String, Object> json = JsonKit.toMap(response);
        Map<String, Object> tokenResponse = (Map<String, Object>) json.get("alipay_system_oauth_token_response");

        if (tokenResponse.containsKey("error_response")) {
            throw new AuthorizedException(
                    (String) ((Map<String, Object>) tokenResponse.get("error_response")).get("sub_msg"));
        }

        return AccToken.builder().accessToken((String) tokenResponse.get("access_token"))
                .uid((String) tokenResponse.get("user_id"))
                .expireIn(Integer.parseInt((String) tokenResponse.get("expires_in")))
                .refreshToken((String) tokenResponse.get("refresh_token")).build();
    }

    @Override
    public Message refresh(AccToken accToken) {
        Map<String, String> params = new HashMap<>();
        params.put("app_id", context.getAppKey());
        params.put("method", "alipay.system.oauth.token");
        params.put("charset", Charset.DEFAULT_UTF_8);
        params.put("sign_type", "RSA2");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("version", "1.0");
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", accToken.getRefreshToken());

        String response = Httpx.post(GATEWAY, params);
        Map<String, Object> json = JsonKit.toMap(response);
        Map<String, Object> tokenResponse = (Map<String, Object>) json.get("alipay_system_oauth_token_response");

        if (tokenResponse.containsKey("error_response")) {
            throw new AuthorizedException(
                    (String) ((Map<String, Object>) tokenResponse.get("error_response")).get("sub_msg"));
        }

        return Message.builder().errcode(ErrorCode.SUCCESS.getCode())
                .data(AccToken.builder().accessToken((String) tokenResponse.get("access_token"))
                        .uid((String) tokenResponse.get("user_id"))
                        .expireIn(Integer.parseInt((String) tokenResponse.get("expires_in")))
                        .refreshToken((String) tokenResponse.get("refresh_token")).build())
                .build();
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        Map<String, String> params = new HashMap<>();
        params.put("app_id", context.getAppKey());
        params.put("method", "alipay.user.info.share");
        params.put("charset", Charset.DEFAULT_UTF_8);
        params.put("sign_type", "RSA2");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("version", "1.0");
        params.put("auth_token", accToken.getAccessToken());

        String response = Httpx.post(GATEWAY, params);
        Map<String, Object> json = JsonKit.toMap(response);
        Map<String, Object> userResponse = (Map<String, Object>) json.get("alipay_user_info_share_response");

        if (userResponse.containsKey("error_response")) {
            throw new AuthorizedException(
                    (String) ((Map<String, Object>) userResponse.get("error_response")).get("sub_msg"));
        }

        String province = (String) userResponse.get("province");
        String city = (String) userResponse.get("city");
        String location = String.format("%s %s", StringKit.isEmpty(province) ? "" : province,
                StringKit.isEmpty(city) ? "" : city);

        return Material.builder().rawJson(JsonKit.toJsonString(userResponse)).uuid((String) userResponse.get("user_id"))
                .username(StringKit.isEmpty((String) userResponse.get("user_name"))
                        ? (String) userResponse.get("nick_name")
                        : (String) userResponse.get("user_name"))
                .nickname((String) userResponse.get("nick_name")).avatar((String) userResponse.get("avatar"))
                .location(location).gender(Gender.of((String) userResponse.get("gender"))).token(accToken)
                .source(complex.toString()).build();
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize()).queryParam("app_id", context.getAppKey())
                .queryParam("scope", "auth_user").queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state)).build();
    }

}