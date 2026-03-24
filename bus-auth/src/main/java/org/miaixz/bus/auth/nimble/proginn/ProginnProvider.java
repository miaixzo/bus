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
package org.miaixz.bus.auth.nimble.proginn;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
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

/**
 * Proginn (Programmer's Inn) login provider.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ProginnProvider extends AbstractProvider {

    /**
     * Constructs a {@code ProginnProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public ProginnProvider(Context context) {
        super(context, Registry.PROGINN);
    }

    /**
     * Constructs a {@code ProginnProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public ProginnProvider(Context context, CacheX cache) {
        super(context, Registry.PROGINN, cache);
    }

    /**
     * Retrieves the access token from Proginn's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("code", callback.getCode());
        params.put("client_id", context.getClientId());
        params.put("client_secret", context.getClientSecret());
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", context.getRedirectUri());
        String response = Httpx.post(this.complex.token(), params);
        Map<String, Object> object = JsonKit.toPojo(response, Map.class);
        this.checkResponse(object);

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Authorization.builder().token((String) object.get("access_token"))
                                .refresh((String) object.get("refresh_token")).uid((String) object.get("uid"))
                                .token_type((String) object.get("token_type"))
                                .expireIn(((Number) object.get("expires_in")).intValue()).build())
                .build();
    }

    /**
     * Retrieves user information from Proginn's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String userInfo = doGetUserInfo(authorization);
        Map<String, Object> object = JsonKit.toPojo(userInfo, Map.class);
        this.checkResponse(object);

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Claims.builder().rawJson(JsonKit.toJsonString(object)).uuid((String) object.get("uid"))
                                .username((String) object.get("nickname")).nickname((String) object.get("nickname"))
                                .avatar((String) object.get("avatar")).email((String) object.get("email"))
                                .gender(Gender.UNKNOWN).token(authorization).source(complex.toString()).build())
                .build();
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response indicates an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error")) {
            throw new AuthorizedException((String) object.get("error_description"));
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
                        .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getScopes(ProginnScope.values())))
                        .build())
                .build();
    }

}
