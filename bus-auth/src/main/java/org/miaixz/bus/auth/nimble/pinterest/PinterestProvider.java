/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.nimble.pinterest;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.Map;
import java.util.Objects;

/**
 * Pinterest login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PinterestProvider extends AbstractProvider {

    private static final String FAILURE = "failure";

    /**
     * Constructs a {@code PinterestProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public PinterestProvider(Context context) {
        super(context, Registry.PINTEREST);
    }

    /**
     * Constructs a {@code PinterestProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public PinterestProvider(Context context, CacheX cache) {
        super(context, Registry.PINTEREST, cache);
    }

    /**
     * Retrieves the access token from Pinterest's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        String response = doPostToken(callback.getCode());
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
            String tokenType = (String) object.get("token_type");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(Authorization.builder().token(token).token_type(tokenType).build()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * Retrieves user information from Pinterest's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String userinfoUrl = userInfoUrl(authorization);
        // TODO: Check if .setFollowRedirects(true) is needed
        String response = Httpx.get(userinfoUrl);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            this.checkResponse(object);

            Map<String, Object> userObj = (Map<String, Object>) object.get(Consts.DATA);
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

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                    Claims.builder().rawJson(JsonKit.toJsonString(userObj)).uuid(id).avatar(avatar).username(username)
                            .nickname(firstName + Symbol.SPACE + lastName).gender(Gender.UNKNOWN).remark(bio)
                            .token(authorization).source(complex.toString()).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Retrieves the avatar URL from the user object.
     *
     * @param userObj the map containing user information
     * @return the avatar URL, or null if not found
     */
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
                                this.getScopes(Symbol.COMMA, false, this.getScopes(PinterestScope.values())))
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
        return Builder.fromUrl(this.complex.userinfo()).queryParam("access_token", authorization.getToken())
                .queryParam("fields", "id,username,first_name,last_name,bio,image").build();
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response indicates an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        String status = (String) object.get("status");
        if (!object.containsKey("status") || FAILURE.equals(status)) {
            String message = (String) object.get("message");
            throw new AuthorizedException(message != null ? message : "Unknown error");
        }
    }

}
