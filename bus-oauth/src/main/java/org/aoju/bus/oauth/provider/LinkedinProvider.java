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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.http.HttpClient;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.StateCache;

import java.util.HashMap;
import java.util.Map;


/**
 * 领英登录
 *
 * @author Kimi Liu
 * @version 5.2.9
 * @since JDK 1.8+
 */
public class LinkedinProvider extends DefaultProvider {

    public LinkedinProvider(Context context) {
        super(context, Registry.LINKEDIN);
    }

    public LinkedinProvider(Context context, StateCache stateCache) {
        super(context, Registry.LINKEDIN, stateCache);
    }

    @Override
    protected AccToken getAccessToken(Callback Callback) {
        return this.getToken(accessTokenUrl(Callback.getCode()));
    }

    @Override
    protected Property getUserInfo(AccToken token) {
        String accessToken = token.getAccessToken();

        Map<String, String> header = new HashMap<>();
        header.put("Host", "api.linkedin.com");
        header.put("Connection", "Keep-Alive");
        header.put("Authorization", "Bearer " + accessToken);

        String response = HttpClient.get(userInfoUrl(token), null, header);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        String userName = getUserName(object);

        // 获取用户头像
        String avatar = this.getAvatar(object);

        // 获取用户邮箱地址
        String email = this.getUserEmail(accessToken);
        return Property.builder()
                .uuid(object.getString("id"))
                .username(userName)
                .nickname(userName)
                .avatar(avatar)
                .email(email)
                .token(token)
                .gender(Normal.Gender.UNKNOWN)
                .source(source.toString())
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
        return firstName + " " + lastName;
    }

    /**
     * 获取用户的头像
     *
     * @param userInfoObject 用户json对象
     * @return 用户的头像地址
     */
    private String getAvatar(JSONObject userInfoObject) {
        String avatar = null;
        JSONObject profilePictureObject = userInfoObject.getJSONObject("profilePicture");
        if (profilePictureObject.containsKey("displayImage~")) {
            JSONArray displayImageElements = profilePictureObject.getJSONObject("displayImage~")
                    .getJSONArray("elements");
            if (null != displayImageElements && displayImageElements.size() > 0) {
                JSONObject largestImageObj = displayImageElements.getJSONObject(displayImageElements.size() - 1);
                avatar = largestImageObj.getJSONArray("identifiers").getJSONObject(0).getString("identifier");
            }
        }
        return avatar;
    }

    /**
     * 获取用户的email
     *
     * @param accessToken 用户授权后返回的token
     * @return 用户的邮箱地址
     */
    private String getUserEmail(String accessToken) {
        Map<String, String> header = new HashMap<>();
        header.put("Host", "api.linkedin.com");
        header.put("Connection", "Keep-Alive");
        header.put("Authorization", "Bearer " + accessToken);

        String url = "https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))";
        String response = HttpClient.get(url, null, header);

        JSONObject object = JSONObject.parseObject(response);
        this.checkResponse(object);
        Object obj = JSONPath.eval(object, "$['elements'][0]['handle~']['emailAddress']");
        return null == obj ? null : (String) obj;
    }

    private String getUserName(JSONObject userInfoObject, String nameKey) {
        String firstName;
        JSONObject firstNameObj = userInfoObject.getJSONObject(nameKey);
        JSONObject localizedObj = firstNameObj.getJSONObject("localized");
        JSONObject preferredLocaleObj = firstNameObj.getJSONObject("preferredLocale");
        firstName = localizedObj.getString(preferredLocaleObj.getString("language") + "_" + preferredLocaleObj.getString("country"));
        return firstName;
    }

    @Override
    public Message refresh(AccToken oldToken) {
        String refreshToken = oldToken.getRefreshToken();
        if (StringUtils.isEmpty(refreshToken)) {
            throw new InstrumentException(Builder.Status.UNSUPPORTED.getCode());
        }
        String refreshTokenUrl = refreshTokenUrl(refreshToken);
        return Message.builder()
                .errcode(Builder.Status.SUCCESS.getCode())
                .data(this.getToken(refreshTokenUrl))
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("error")) {
            throw new InstrumentException(object.getString("error_description"));
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
        header.put("Host", "www.linkedin.com");

        String response = HttpClient.get(accessTokenUrl, null, header);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in"))
                .refreshToken(object.getString("refresh_token"))
                .build();
    }

    /**
     * 返回带{@code state}参数的授权url,授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数,可以防止csrf
     * @return 返回授权地址
     * @since 1.9.3
     */
    @Override
    public String authorize(String state) {
        return Builder.fromBaseUrl(source.authorize())
                .queryParam("response_type", "code")
                .queryParam("client_id", context.getClientId())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", "r_liteprofile%20r_emailaddress%20w_member_social")
                .queryParam("state", getRealState(state))
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param token 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken token) {
        return Builder.fromBaseUrl(source.userInfo())
                .queryParam("projection", "(id,firstName,lastName,profilePicture(displayImage~:playableStreams))")
                .build();
    }

}
