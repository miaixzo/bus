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
package org.miaixz.bus.auth.nimble.facebook;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.Map;

/**
 * Facebook login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FacebookProvider extends AbstractProvider {

    /**
     * Constructs a {@code FacebookProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public FacebookProvider(Context context) {
        super(context, Registry.FACEBOOK);
    }

    /**
     * Constructs a {@code FacebookProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public FacebookProvider(Context context, CacheX cache) {
        super(context, Registry.FACEBOOK, cache);
    }

    /**
     * Retrieves the access token from Facebook's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        String response = doGetToken(callback.getCode());
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
            String tokenType = (String) object.get("token_type");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(Authorization.builder().token(token).expireIn(expiresIn).token_type(tokenType).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * Retrieves user information from Facebook's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Material} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String userInfo = doGetUserInfo(authorization);
        try {
            Map<String, Object> object = JsonKit.toPojo(userInfo, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(object);

            String id = (String) object.get("id");
            if (id == null) {
                throw new AuthorizedException("Missing id in user info response");
            }
            String name = (String) object.get("name");
            String link = (String) object.get("link");
            String locale = (String) object.get("locale");
            String email = (String) object.get("email");
            String gender = (String) object.get("gender");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                    Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(id).username(name).nickname(name)
                            .blog(link).avatar(getUserPicture(object)).location(locale).email(email)
                            .gender(Gender.of(gender)).token(authorization).source(complex.toString()).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Checks the completeness and validity of the context configuration for Facebook authentication. Specifically, it
     * ensures that the redirect URI uses HTTPS.
     *
     * @param context the authentication context
     * @throws AuthorizedException if the redirect URI is invalid
     */
    @Override
    protected void validate(Context context) {
        super.validate(context);
        // Facebook's redirect uri must use the HTTPS protocol
        if (Registry.FACEBOOK == this.complex && !Protocol.isHttps(this.context.getRedirectUri())) {
            throw new AuthorizedException(ErrorCode._110005.getKey(), this.complex);
        }
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authorization the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    protected String userInfoUrl(Authorization authorization) {
        return Builder.fromUrl(this.complex.userinfo()).queryParam("access_token", authorization.getToken())
                .queryParam("fields", "id,name,birthday,gender,hometown,email,devices,picture.width(400),link").build();
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
                                this.getScopes(Symbol.COMMA, false, this.getScopes(FacebookScope.values())))
                        .build())
                .build();
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response contains an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error")) {
            Map<String, Object> error = (Map<String, Object>) object.get("error");
            String message = error != null ? (String) error.get("message") : null;
            throw new AuthorizedException(message != null ? message : "Unknown error");
        }
    }

    /**
     * Retrieves the user's picture URL from the response object.
     *
     * @param object the map containing user information
     * @return the picture URL, or null if not found
     */
    private String getUserPicture(Map<String, Object> object) {
        String picture = null;
        if (object.containsKey("picture")) {
            Map<String, Object> pictureObj = (Map<String, Object>) object.get("picture");
            if (pictureObj != null) {
                Map<String, Object> dataObj = (Map<String, Object>) pictureObj.get(Consts.DATA);
                if (dataObj != null) {
                    picture = (String) dataObj.get("url");
                }
            }
        }
        return picture;
    }

}
