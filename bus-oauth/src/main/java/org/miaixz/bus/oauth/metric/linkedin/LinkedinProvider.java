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
package org.miaixz.bus.oauth.metric.linkedin;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public AccToken getAccessToken(Callback callback) {
        return this.getToken(accessTokenUrl(callback.getCode()));
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String accessToken = accToken.getAccessToken();
        Map<String, String> header = new HashMap<>();
        header.put("Host", "api.linkedin.com");
        header.put("Connection", "Keep-Alive");
        header.put("Authorization", "Bearer " + accessToken);

        String response = Httpx.get(userInfoUrl(accToken), null, header);
        try {
            Map<String, Object> data = JsonKit.toPojo(response, Map.class);
            if (data == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            this.checkResponse(data);

            String id = (String) data.get("id");
            if (id == null) {
                throw new AuthorizedException("Missing id in user info response");
            }
            String userName = getUserName(data);
            String avatar = this.getAvatar(data);
            String email = this.getUserEmail(accessToken);

            return Material.builder().rawJson(JsonKit.toJsonString(data)).uuid(id).username(userName).nickname(userName)
                    .avatar(avatar).email(email).token(accToken).gender(Gender.UNKNOWN).source(complex.toString())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * 获取用户的真实名
     *
     * @param data 用户json对象
     * @return 用户名
     */
    private String getUserName(Map<String, Object> data) {
        String firstName, lastName;
        // 获取firstName
        if (data.containsKey("localizedFirstName")) {
            firstName = (String) data.get("localizedFirstName");
        } else {
            firstName = getUserName(data, "firstName");
        }
        // 获取lastName
        if (data.containsKey("localizedLastName")) {
            lastName = (String) data.get("localizedLastName");
        } else {
            lastName = getUserName(data, "lastName");
        }
        return firstName + Symbol.SPACE + lastName;
    }

    /**
     * 获取用户的头像
     *
     * @param data 用户json对象
     * @return 用户的头像地址
     */
    private String getAvatar(Map<String, Object> data) {
        Map<String, Object> profilePictureObject = (Map<String, Object>) data.get("profilePicture");
        if (profilePictureObject == null || !profilePictureObject.containsKey("displayImage~")) {
            return null;
        }
        Map<String, Object> displayImageObject = (Map<String, Object>) profilePictureObject.get("displayImage~");
        if (displayImageObject == null || !displayImageObject.containsKey("elements")) {
            return null;
        }
        List<Object> displayImageElements = (List<Object>) displayImageObject.get("elements");
        if (displayImageElements == null || displayImageElements.isEmpty()) {
            return null;
        }
        Map<String, Object> largestImageObj = (Map<String, Object>) displayImageElements
                .get(displayImageElements.size() - 1);
        if (largestImageObj == null || !largestImageObj.containsKey("identifiers")) {
            return null;
        }
        List<Object> identifiers = (List<Object>) largestImageObj.get("identifiers");
        if (identifiers == null || identifiers.isEmpty()) {
            return null;
        }
        Map<String, Object> identifierObj = (Map<String, Object>) identifiers.get(0);
        return (String) identifierObj.get("identifier");
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

        String emailResponse = Httpx.get(
                "https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))", null, header);
        try {
            Map<String, Object> emailObj = JsonKit.toPojo(emailResponse, Map.class);
            if (emailObj == null) {
                throw new AuthorizedException("Failed to parse email response: empty response");
            }

            this.checkResponse(emailObj);

            List<Object> elements = (List<Object>) emailObj.get("elements");
            if (elements == null || elements.isEmpty()) {
                return null;
            }
            Map<String, Object> handleObj = (Map<String, Object>) elements.get(0);
            Map<String, Object> handleInnerObj = (Map<String, Object>) handleObj.get("handle~");
            return handleInnerObj != null ? (String) handleInnerObj.get("emailAddress") : null;
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse email response: " + e.getMessage());
        }
    }

    private String getUserName(Map<String, Object> data, String nameKey) {
        Map<String, Object> nameObj = (Map<String, Object>) data.get(nameKey);
        if (nameObj == null) {
            return "";
        }
        Map<String, Object> localizedObj = (Map<String, Object>) nameObj.get("localized");
        Map<String, Object> preferredLocaleObj = (Map<String, Object>) nameObj.get("preferredLocale");
        if (localizedObj == null || preferredLocaleObj == null) {
            return "";
        }
        String language = (String) preferredLocaleObj.get("language");
        String country = (String) preferredLocaleObj.get("country");
        if (language == null || country == null) {
            return "";
        }
        return (String) localizedObj.get(language + Symbol.UNDERLINE + country);
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error")) {
            String errorDescription = (String) object.get("error_description");
            throw new AuthorizedException(ErrorCode.FAILURE.getCode(),
                    errorDescription != null ? errorDescription : "Unknown error", complex.getName());
        }
    }

    /**
     * 获取token，适用于获取access_token和刷新token
     *
     * @param accessTokenUrl 实际请求token的地址
     * @return token对象
     */
    private AccToken getToken(String accessTokenUrl) {
        Map<String, String> header = new HashMap<>();
        header.put("Host", "www.linkedin.com");
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);

        String response = Httpx.post(accessTokenUrl, null, header);
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
            Object expiresInObj = accessTokenObject.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String refreshToken = (String) accessTokenObject.get("refresh_token");

            return AccToken.builder().accessToken(accessToken).expireIn(expiresIn).refreshToken(refreshToken).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
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