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
package org.miaixz.bus.oauth.metric.baidu;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
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
 * 百度 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BaiduProvider extends AbstractProvider {

    public BaiduProvider(Context context) {
        super(context, Registry.BAIDU);
    }

    public BaiduProvider(Context context, ExtendCache cache) {
        super(context, Registry.BAIDU, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        String response = doPostAuthorizationCode(callback.getCode());
        return getAuthToken(response);
    }

    /**
     * https://openapi.baidu.com/rest/2.0/passport/users/getInfo?access_token=121.c86e87cc0828cc1dabb8faee540531d4.YsUIAWvYbgqVni1VhkgKgyLh8nEyELbDOEZs_OA.OgDgmA
     * https://openapi.baidu.com/rest/2.0/passport/users/getInfo?access_token=121.2907d9facf9fb97adf7287fa75496eda.Y3NSjR3-3HKt1RgT0HEl7GgxRXT5gOOVdngXezY.OcC_7g
     * 新旧应用返回的用户信息不一致
     *
     * @param accToken token信息
     * @return Property
     */
    @Override
    public Material getUserInfo(AccToken accToken) {
        String userInfo = doGetUserInfo(accToken);
        try {
            Map<String, Object> object = JsonKit.toPojo(userInfo, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(object);

            String userId = object.containsKey("userid") ? (String) object.get("userid")
                    : (String) object.get("openid");
            if (userId == null) {
                throw new AuthorizedException("Missing userid or openid in response");
            }
            String username = (String) object.get("username");
            String userDetail = (String) object.get("userdetail");
            String sex = (String) object.get("sex");

            return Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(userId).username(username)
                    .nickname(username).avatar(getAvatar(object)).remark(userDetail).gender(Gender.of(sex))
                    .token(accToken).source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    private String getAvatar(Map<String, Object> object) {
        String portrait = (String) object.get("portrait");
        return StringKit.isEmpty(portrait) ? null
                : String.format("http://himg.bdimg.com/sys/portrait/item/%s.jpg", portrait);
    }

    @Override
    public Message revoke(AccToken accToken) {
        String response = doGetRevoke(accToken);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse revoke response: empty response");
            }
            this.checkResponse(object);

            // 返回1表示取消授权成功，否则失败
            Object resultObj = object.get("result");
            int result = resultObj instanceof Number ? ((Number) resultObj).intValue() : 0;
            ErrorCode status = result == 1 ? ErrorCode.SUCCESS : ErrorCode.FAILURE;
            return Message.builder().errcode(status.getCode()).errmsg(status.getDesc()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse revoke response: " + e.getMessage());
        }
    }

    @Override
    public Message refresh(AccToken accToken) {
        String refreshUrl = Builder.fromUrl(this.complex.refresh()).queryParam("grant_type", "refresh_token")
                .queryParam("refresh_token", accToken.getRefreshToken())
                .queryParam("client_id", this.context.getAppKey())
                .queryParam("client_secret", this.context.getAppSecret()).build();
        String response = Httpx.get(refreshUrl);
        return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).data(this.getAuthToken(response)).build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state)).queryParam("display", "popup")
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getDefaultScopes(BaiduScope.values())))
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error") || object.containsKey("error_code")) {
            String msg = object.containsKey("error_description") ? (String) object.get("error_description")
                    : (String) object.get("error_msg");
            throw new AuthorizedException(msg != null ? msg : "Unknown error");
        }
    }

    private AccToken getAuthToken(String response) {
        try {
            Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
            if (accessTokenObject == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }
            this.checkResponse(accessTokenObject);

            String accessToken = (String) accessTokenObject.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String refreshToken = (String) accessTokenObject.get("refresh_token");
            String scope = (String) accessTokenObject.get("scope");
            Object expiresInObj = accessTokenObject.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

            return AccToken.builder().accessToken(accessToken).refreshToken(refreshToken).scope(scope)
                    .expireIn(expiresIn).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

}