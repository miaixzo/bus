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
package org.miaixz.bus.oauth.metric.toutiao;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import java.util.Map;

/**
 * 今日头条 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ToutiaoProvider extends AbstractProvider {

    public ToutiaoProvider(Context context) {
        super(context, Registry.TOUTIAO);
    }

    public ToutiaoProvider(Context context, ExtendCache cache) {
        super(context, Registry.TOUTIAO, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        String response = doGetAuthorizationCode(callback.getCode());
        Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);

        this.checkResponse(accessTokenObject);

        return AccToken.builder().accessToken((String) accessTokenObject.get("access_token"))
                .expireIn(((Number) accessTokenObject.get("expires_in")).intValue())
                .openId((String) accessTokenObject.get("open_id")).build();
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String userResponse = doGetUserInfo(accToken);
        Map<String, Object> userProfile = JsonKit.toPojo(userResponse, Map.class);

        this.checkResponse(userProfile);

        Map<String, Object> user = (Map<String, Object>) userProfile.get("data");

        boolean isAnonymousUser = "14".equals(user.get("uid_type"));
        String anonymousUserName = "匿名用户";

        return Material.builder().rawJson(JsonKit.toJsonString(userProfile)).uuid((String) user.get("uid"))
                .username(isAnonymousUser ? anonymousUserName : (String) user.get("screen_name"))
                .nickname(isAnonymousUser ? anonymousUserName : (String) user.get("screen_name"))
                .avatar((String) user.get("avatar_url")).remark((String) user.get("description"))
                .gender(Gender.of((String) user.get("gender"))).token(accToken).source(complex.toString()).build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize()).queryParam("response_type", "code")
                .queryParam("client_key", context.getAppKey()).queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("auth_only", 1).queryParam("display", 0).queryParam("state", getRealState(state)).build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(complex.accessToken()).queryParam("code", code)
                .queryParam("client_key", context.getAppKey()).queryParam("client_secret", context.getAppSecret())
                .queryParam("grant_type", "authorization_code").build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.userInfo()).queryParam("client_key", context.getAppKey())
                .queryParam("access_token", accToken.getAccessToken()).build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error_code")) {
            throw new AuthorizedException(ErrorCode.Toutiao.getErrorCode((String) object.get("error_code")).getDesc());
        }
    }

}