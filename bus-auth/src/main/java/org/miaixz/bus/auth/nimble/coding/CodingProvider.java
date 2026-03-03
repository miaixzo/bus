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
package org.miaixz.bus.auth.nimble.coding;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;

import java.util.Map;

/**
 * Coding login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CodingProvider extends AbstractProvider {

    /**
     * Constructs a {@code CodingProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public CodingProvider(Context context) {
        super(context, Registry.CODING);
    }

    /**
     * Constructs a {@code CodingProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public CodingProvider(Context context, CacheX cache) {
        super(context, Registry.CODING, cache);
    }

    /**
     * Retrieves the access token from Coding's authorization server.
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
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String refresh = (String) object.get("refresh_token");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(Authorization.builder().token(token).expireIn(expiresIn).refresh(refresh).build()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * Retrieves user information from Coding's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String response = doGetUserInfo(authorization);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(object);

            Map<String, Object> data = (Map<String, Object>) object.get(Consts.DATA);
            if (data == null) {
                throw new AuthorizedException("Missing data field in user info response");
            }

            String id = String.valueOf(data.get("id"));
            if (id == null) {
                throw new AuthorizedException("Missing id in user info response");
            }
            String name = (String) data.get("name");
            String avatar = (String) data.get("avatar");
            String path = (String) data.get("path");
            String company = (String) data.get("company");
            String location = (String) data.get("location");
            String sex = (String) data.get("sex");
            String email = (String) data.get("email");
            String slogan = (String) data.get("slogan");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Claims.builder().rawJson(JsonKit.toJsonString(data)).uuid(id).username(name)
                                    .avatar(avatar != null ? "https://coding.net" + avatar : null)
                                    .blog(path != null ? "https://coding.net" + path : null).nickname(name)
                                    .company(company).location(location).gender(Gender.of(sex)).email(email)
                                    .remark(slogan).token(authorization).source(complex.toString()).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response indicates an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if ((int) object.get("code") != 0) {
            throw new AuthorizedException((String) object.get("msg"));
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
                Builder.fromUrl(String.format(this.complex.authorize(), this.context.getPrefix()))
                        .queryParam("response_type", "code").queryParam("client_id", this.context.getClientId())
                        .queryParam("redirect_uri", this.context.getRedirectUri())
                        .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getScopes(CodingScope.values())))
                        .queryParam("state", getRealState(state)).build())
                .build();
    }

    /**
     * Returns the URL to obtain the access token.
     *
     * @param code the authorization code
     * @return the URL to obtain the access token
     */
    @Override
    public String tokenUrl(String code) {
        return Builder.fromUrl(String.format(this.complex.token(), this.context.getPrefix())).queryParam("code", code)
                .queryParam("client_id", this.context.getClientId())
                .queryParam("client_secret", this.context.getClientSecret())
                .queryParam("grant_type", "authorization_code")
                .queryParam("redirect_uri", this.context.getRedirectUri()).build();
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authorization the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    public String userInfoUrl(Authorization authorization) {
        return Builder.fromUrl(String.format(this.complex.userinfo(), this.context.getPrefix()))
                .queryParam("access_token", authorization.getToken()).build();
    }

}
