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
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Property;

/**
 * 今日头条登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ToutiaoProvider extends AbstractProvider {

    public ToutiaoProvider(Context context) {
        super(context, Registry.TOUTIAO);
    }

    public ToutiaoProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.TOUTIAO, extendCache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        JSONObject object = JSONObject.parseObject(doGetAuthorizationCode(callback.getCode()));

        this.checkResponse(object);

        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in"))
                .openId(object.getString("open_id"))
                .build();
    }

    @Override
    public Property getUserInfo(AccToken accToken) {
        JSONObject jsonObject = JSONObject.parseObject(doGetUserInfo(accToken));

        this.checkResponse(jsonObject);

        JSONObject object = jsonObject.getJSONObject("data");

        boolean isAnonymousUser = object.getIntValue("uid_type") == 14;
        String anonymousUserName = "匿名用户";

        return Property.builder()
                .rawJson(object)
                .uuid(object.getString("uid"))
                .username(isAnonymousUser ? anonymousUserName : object.getString("screen_name"))
                .nickname(isAnonymousUser ? anonymousUserName : object.getString("screen_name"))
                .avatar(object.getString("avatar_url"))
                .remark(object.getString("description"))
                .gender(Normal.Gender.of(object.getString("gender")))
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
                .queryParam("response_type", "code")
                .queryParam("client_key", context.getAppKey())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("auth_only", 1)
                .queryParam("display", 0)
                .queryParam("state", getRealState(state))
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    @Override
    public String accessTokenUrl(String code) {
        return Builder.fromUrl(source.accessToken())
                .queryParam("code", code)
                .queryParam("client_key", context.getAppKey())
                .queryParam("client_secret", context.getAppSecret())
                .queryParam("grant_type", "authorization_code")
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
        return Builder.fromUrl(source.userInfo())
                .queryParam("client_key", context.getAppKey())
                .queryParam("access_token", accToken.getAccessToken())
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("error_code")) {
            throw new AuthorizedException(Error.getErrorCode(object.getString("error_code")).getDesc());
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Error {
        /**
         * 0：正常；
         * other：调用异常,具体异常内容见{@code desc}
         */
        EC0(Symbol.ZERO, "接口调用成功"),
        EC1(Symbol.ONE, "API配置错误,未传入Client Key"),
        EC2(Symbol.TWO, "API配置错误,Client Key错误,请检查是否和开放平台的ClientKey一致"),
        EC3(Symbol.THREE, "没有授权信息"),
        EC4(Symbol.FOUR, "响应类型错误"),
        EC5(Symbol.FIVE, "授权类型错误"),
        EC6(Symbol.SIX, "client_secret错误"),
        EC7(Symbol.SEVEN, "authorize_code过期"),
        EC8(Symbol.EIGHT, "指定url的scheme不是https"),
        EC9("9", "接口内部错误,请联系头条技术"),
        EC10("10", "access_token过期"),
        EC11("11", "缺少access_token"),
        EC12("12", "参数缺失"),
        EC13("13", "url错误"),
        EC21("21", "域名与登记域名不匹配"),
        EC999("999", "未知错误,请联系头条技术");

        private final String code;
        private final String desc;

        public static Error getErrorCode(String errorCode) {
            Error[] errorCodes = Error.values();
            for (Error code : errorCodes) {
                if (code.getCode() == errorCode) {
                    return code;
                }
            }
            return EC999;
        }
    }

}
