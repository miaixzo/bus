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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.core.lang.Header;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Property;
import org.miaixz.bus.oauth.metric.OauthScope;

import java.util.HashMap;
import java.util.Map;

/**
 * 领英登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LinkedinProvider extends AbstractProvider {

    public LinkedinProvider(Context context) {
        super(context, Registry.LINKEDIN);
    }

    public LinkedinProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.LINKEDIN, extendCache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        return this.getToken(accessTokenUrl(callback.getCode()));
    }

    @Override
    public Property getUserInfo(AccToken accToken) {
        String accessToken = accToken.getAccessToken();

        Map<String, String> header = new HashMap<>();
        header.put(Header.HOST, "api.linkedin.com");
        header.put(Header.CONNECTION, "Keep-Alive");
        header.put(Header.AUTHORIZATION, "Bearer " + accessToken);

        String response = Httpx.get(userInfoUrl(accToken), null, header);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        String userName = getUserName(object);

        // 获取用户头像
        String avatar = this.getAvatar(object);

        // 获取用户邮箱地址
        String email = this.getUserEmail(accessToken);
        return Property.builder()
                .rawJson(object)
                .uuid(object.getString("id"))
                .username(userName)
                .nickname(userName)
                .avatar(avatar)
                .email(email)
                .token(accToken)
                .gender(Normal.Gender.UNKNOWN)
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
        return Builder.fromUrl(super.authorize(state))
                .queryParam("scope", this.getScopes(Symbol.SPACE, false, getScopes(true, OauthScope.Linkedin.values())))
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param token 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    public String userInfoUrl(AccToken token) {
        return Builder.fromUrl(source.userInfo())
                .queryParam("projection", "(id,firstName,lastName,profilePicture(displayImage~:playableStreams))")
                .build();
    }

    /**
     * 获取用户的真实名
     *
     * @param userInfoObject 用户json对象
     * @return 用户名
     */
    private String getUserName(JSONObject userInfoObject) {
        String firstName, lastName;
        // 获取firstName
        if (userInfoObject.containsKey("localizedFirstName")) {
            firstName = userInfoObject.getString("localizedFirstName");
        } else {
            firstName = getUserName(userInfoObject, "firstName");
        }
        // 获取lastName
        if (userInfoObject.containsKey("localizedLastName")) {
            lastName = userInfoObject.getString("localizedLastName");
        } else {
            lastName = getUserName(userInfoObject, "lastName");
        }
        return firstName + Symbol.SPACE + lastName;
    }

    /**
     * 获取用户的头像
     *
     * @param userInfoObject 用户json对象
     * @return 用户的头像地址
     */
    private String getAvatar(JSONObject userInfoObject) {
        JSONObject profilePictureObject = userInfoObject.getJSONObject("profilePicture");
        if (null == profilePictureObject || !profilePictureObject.containsKey("displayImage~")) {
            return null;
        }
        JSONObject displayImageObject = profilePictureObject.getJSONObject("displayImage~");
        if (null == displayImageObject || !displayImageObject.containsKey("elements")) {
            return null;
        }
        JSONArray displayImageElements = displayImageObject.getJSONArray("elements");
        if (null == displayImageElements || displayImageElements.isEmpty()) {
            return null;
        }
        JSONObject largestImageObj = displayImageElements.getJSONObject(displayImageElements.size() - 1);
        if (null == largestImageObj || !largestImageObj.containsKey("identifiers")) {
            return null;
        }
        JSONArray identifiers = largestImageObj.getJSONArray("identifiers");
        if (null == identifiers || identifiers.isEmpty()) {
            return null;
        }
        return identifiers.getJSONObject(0).getString("identifier");
    }

    /**
     * 获取用户的email
     *
     * @param accessToken 用户授权后返回的token
     * @return 用户的邮箱地址
     */
    private String getUserEmail(String accessToken) {
        Map<String, String> header = new HashMap<>();
        header.put(Header.HOST, "api.linkedin.com");
        header.put(Header.CONNECTION, "Keep-Alive");
        header.put(Header.AUTHORIZATION, "Bearer " + accessToken);

        String url = "https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))";
        String response = Httpx.get(url, null, header);

        JSONObject object = JSONObject.parseObject(response);
        this.checkResponse(object);
        Object value = JSONPath.eval(object, "$['elements'][0]['handle~']['emailAddress']");
        return null == value ? null : (String) value;
    }

    private String getUserName(JSONObject userInfoObject, String nameKey) {
        String firstName;
        JSONObject firstNameObj = userInfoObject.getJSONObject(nameKey);
        JSONObject localizedObj = firstNameObj.getJSONObject("localized");
        JSONObject preferredLocaleObj = firstNameObj.getJSONObject("preferredLocale");
        firstName = localizedObj.getString(preferredLocaleObj.getString("language") + Symbol.UNDERLINE + preferredLocaleObj.getString("country"));
        return firstName;
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("error_description"));
        }
    }

    /**
     * 获取token,适用于获取access_token和刷新token
     *
     * @param accessTokenUrl 实际请求token的地址
     * @return token对象
     */
    private AccToken getToken(String accessTokenUrl) {
        Map<String, String> header = new HashMap<>();
        header.put(Header.HOST, "www.linkedin.com");

        String response = Httpx.get(accessTokenUrl, null, header);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in"))
                .refreshToken(object.getString("refresh_token"))
                .build();
    }

}
