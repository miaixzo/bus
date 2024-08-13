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
package org.miaixz.bus.oauth.metric.linkedin;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;

/**
 * 领英 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LinkedinProvider extends AbstractProvider {

    public LinkedinProvider(Context context) {
        super(context, Registry.LINKEDIN);
    }

    public LinkedinProvider(Context context, ExtendCache cache) {
        super(context, Registry.LINKEDIN, cache);
    }

    @Override
    protected AccToken getAccessToken(Callback callback) {
        return this.getToken(accessTokenUrl(callback.getCode()));
    }

    @Override
    protected Material getUserInfo(AccToken accToken) {
        String accessToken = accToken.getAccessToken();
        Map<String, String> header = new HashMap<>();
        header.put("Host", "api.linkedin.com");
        header.put("Connection", "Keep-Alive");
        header.put("Authorization", "Bearer " + accessToken);

        String response = Httpx.get(userInfoUrl(accToken), null, header);
        JSONObject userInfoObject = JSONObject.parseObject(response);

        this.checkResponse(userInfoObject);

        String userName = getUserName(userInfoObject);

        // 获取用户头像
        String avatar = this.getAvatar(userInfoObject);

        // 获取用户邮箱地址
        String email = this.getUserEmail(accessToken);
        return Material.builder().rawJson(userInfoObject).uuid(userInfoObject.getString("id")).username(userName)
                .nickname(userName).avatar(avatar).email(email).token(accToken).gender(Gender.UNKNOWN)
                .source(complex.toString()).build();
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
        Map<String, String> header = new HashMap();
        header.put("Host", "api.linkedin.com");
        header.put("Connection", "Keep-Alive");
        header.put("Authorization", "Bearer " + accessToken);

        String emailResponse = Httpx.get(
                "https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))", null, header);
        JSONObject emailObj = JSONObject.parseObject(emailResponse);

        this.checkResponse(emailObj);

        Object obj = JSONPath.eval(emailObj, "$['elements'][0]['handle~']['emailAddress']");
        return null == obj ? null : (String) obj;
    }

    private String getUserName(JSONObject userInfoObject, String nameKey) {
        String firstName;
        JSONObject firstNameObj = userInfoObject.getJSONObject(nameKey);
        JSONObject localizedObj = firstNameObj.getJSONObject("localized");
        JSONObject preferredLocaleObj = firstNameObj.getJSONObject("preferredLocale");
        firstName = localizedObj.getString(
                preferredLocaleObj.getString("language") + Symbol.UNDERLINE + preferredLocaleObj.getString("country"));
        return firstName;
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("error")) {
            throw new AuthorizedException(ErrorCode.FAILURE.getCode(), object.getString("error_description"),
                    complex.getName());
        }
    }

    /**
     * 获取token，适用于获取access_token和刷新token
     *
     * @param accessTokenUrl 实际请求token的地址
     * @return token对象
     */
    private AccToken getToken(String accessTokenUrl) {
        Map<String, String> header = new HashMap();
        header.put("Host", "www.linkedin.com");
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);

        String response = Httpx.post(accessTokenUrl, null, header);
        JSONObject accessTokenObject = JSONObject.parseObject(response);

        this.checkResponse(accessTokenObject);

        return AccToken.builder().accessToken(accessTokenObject.getString("access_token"))
                .expireIn(accessTokenObject.getIntValue("expires_in"))
                .refreshToken(accessTokenObject.getString("refresh_token")).build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("scope", this.getScopes(Symbol.SPACE, false, this.getDefaultScopes(LinkedinScope.values())))
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.userInfo())
                .queryParam("projection", "(id,firstName,lastName,profilePicture(displayImage~:playableStreams))")
                .build();
    }
}
