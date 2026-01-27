/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.auth.nimble.linkedin;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LinkedIn login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LinkedinProvider extends AbstractProvider {

    /**
     * Constructs a {@code LinkedinProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public LinkedinProvider(Context context) {
        super(context, Registry.LINKEDIN);
    }

    /**
     * Constructs a {@code LinkedinProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public LinkedinProvider(Context context, CacheX cache) {
        super(context, Registry.LINKEDIN, cache);
    }

    /**
     * Retrieves the access token from LinkedIn's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     */
    @Override
    public Message token(Callback callback) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(this.getToken(tokenUrl(callback.getCode())))
                .build();
    }

    /**
     * Retrieves user information from LinkedIn's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String token = authorization.getToken();
        Map<String, String> header = new HashMap<>();
        header.put("Host", "api.linkedin.com");
        header.put("Connection", "Keep-Alive");
        header.put(HTTP.AUTHORIZATION, HTTP.BEARER + token);

        String response = Httpx.get(userInfoUrl(authorization), null, header);
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
            String email = this.getUserEmail(token);

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Claims.builder().rawJson(JsonKit.toJsonString(data)).uuid(id).username(userName)
                                    .nickname(userName).avatar(avatar).email(email).token(authorization)
                                    .gender(Gender.UNKNOWN).source(complex.toString()).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Retrieves the user's full name from the user data map.
     *
     * @param data the user data map
     * @return the user's full name
     */
    private String getUserName(Map<String, Object> data) {
        String firstName, lastName;
        // Get firstName
        if (data.containsKey("localizedFirstName")) {
            firstName = (String) data.get("localizedFirstName");
        } else {
            firstName = getUserName(data, "firstName");
        }
        // Get lastName
        if (data.containsKey("localizedLastName")) {
            lastName = (String) data.get("localizedLastName");
        } else {
            lastName = getUserName(data, "lastName");
        }
        return firstName + Symbol.SPACE + lastName;
    }

    /**
     * Retrieves the user's avatar URL from the user data map.
     *
     * @param data the user data map
     * @return the user's avatar URL, or null if not found
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
     * Retrieves the user's email address using a separate API call.
     *
     * @param token the user's access token
     * @return the user's email address
     * @throws AuthorizedException if parsing the email response fails
     */
    private String getUserEmail(String token) {
        Map<String, String> header = new HashMap<>();
        header.put("Host", "api.linkedin.com");
        header.put("Connection", "Keep-Alive");
        header.put(HTTP.AUTHORIZATION, HTTP.BEARER + token);

        String emailResponse = Httpx.get(
                "https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))",
                null,
                header);
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

    /**
     * Retrieves a specific part of the user's name (e.g., firstName, lastName) from the data map.
     *
     * @param data    the user data map
     * @param nameKey the key for the name component (e.g., "firstName", "lastName")
     * @return the name component, or an empty string if not found
     */
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
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response indicates an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error")) {
            String errorDescription = (String) object.get("error_description");
            throw new AuthorizedException(ErrorCode._FAILURE.getKey(),
                    errorDescription != null ? errorDescription : "Unknown error", complex.getName());
        }
    }

    /**
     * Retrieves the token, applicable for both obtaining access tokens and refreshing tokens.
     *
     * @param tokenUrl the actual URL to request the token from
     * @return the {@link Authorization} object
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    private Authorization getToken(String tokenUrl) {
        Map<String, String> header = new HashMap<>();
        header.put("Host", "www.linkedin.com");
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);

        String response = Httpx.post(tokenUrl, null, header);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }

            this.checkResponse(object);

            String token = (String) object.get("access_token");
            if (token == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String refresh = (String) object.get("refresh_token");

            return Authorization.builder().token(token).expireIn(expiresIn).refresh(refresh).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * Returns the authorization URL with a {@code state} parameter. The {@code state} will be included in the
     * authorization callback.
     *
     * @param state the parameter to verify the authorization process, which can prevent CSRF attacks
     * @return the authorization URL
     */
    @Override
    public Message build(String state) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Builder.fromUrl((String) super.build(state).getData())
                        .queryParam(
                                "scope",
                                this.getScopes(Symbol.SPACE, false, this.getScopes(LinkedinScope.values())))
                        .build())
                .build();
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authorization the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    protected String userInfoUrl(Authorization authorization) {
        return Builder.fromUrl(this.complex.userinfo())
                .queryParam("projection", "(id,firstName,lastName,profilePicture(displayImage~:playableStreams))")
                .build();
    }

}
