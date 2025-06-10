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
package org.miaixz.bus.auth.nimble.pinterest;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.AccToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.Map;
import java.util.Objects;

/**
 * Pinterest 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PinterestProvider extends AbstractProvider {

    private static final String FAILURE = "failure";

    public PinterestProvider(Context context) {
        super(context, Registry.PINTEREST);
    }

    public PinterestProvider(Context context, ExtendCache cache) {
        super(context, Registry.PINTEREST, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        String response = doPostAuthorizationCode(callback.getCode());
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
            String tokenType = (String) accessTokenObject.get("token_type");

            return AccToken.builder().accessToken(accessToken).tokenType(tokenType).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String userinfoUrl = userInfoUrl(accToken);
        // TODO: 是否需要 .setFollowRedirects(true)
        String response = Httpx.get(userinfoUrl);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            this.checkResponse(object);

            Map<String, Object> userObj = (Map<String, Object>) object.get("data");
            if (userObj == null) {
                throw new AuthorizedException("Missing data in user info response");
            }

            String id = (String) userObj.get("id");
            if (id == null) {
                throw new AuthorizedException("Missing id in user info response");
            }
            String username = (String) userObj.get("username");
            String firstName = (String) userObj.get("first_name");
            String lastName = (String) userObj.get("last_name");
            String bio = (String) userObj.get("bio");
            String avatar = getAvatarUrl(userObj);

            return Material.builder().rawJson(JsonKit.toJsonString(userObj)).uuid(id).avatar(avatar).username(username)
                    .nickname(firstName + Symbol.SPACE + lastName).gender(Gender.UNKNOWN).remark(bio).token(accToken)
                    .source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    private String getAvatarUrl(Map<String, Object> userObj) {
        // image is a map data structure
        Map<String, Object> jsonObject = (Map<String, Object>) userObj.get("image");
        if (Objects.isNull(jsonObject)) {
            return null;
        }
        Map<String, Object> avatarObj = (Map<String, Object>) jsonObject.get("60x60");
        return avatarObj != null ? (String) avatarObj.get("url") : null;
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state)).queryParam("scope",
                this.getScopes(Symbol.COMMA, false, this.getDefaultScopes(PinterestScope.values()))).build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(this.complex.getConfig().get(Builder.USERINFO))
                .queryParam("access_token", accToken.getAccessToken())
                .queryParam("fields", "id,username,first_name,last_name,bio,image").build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(Map<String, Object> object) {
        String status = (String) object.get("status");
        if (!object.containsKey("status") || FAILURE.equals(status)) {
            String message = (String) object.get("message");
            throw new AuthorizedException(message != null ? message : "Unknown error");
        }
    }

}