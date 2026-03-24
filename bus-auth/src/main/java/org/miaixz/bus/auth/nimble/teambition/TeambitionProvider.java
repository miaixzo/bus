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
package org.miaixz.bus.auth.nimble.teambition;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Teambition login provider.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TeambitionProvider extends AbstractProvider {

    /**
     * Constructs a {@code TeambitionProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public TeambitionProvider(Context context) {
        super(context, Registry.TEAMBITION);
    }

    /**
     * Constructs a {@code TeambitionProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public TeambitionProvider(Context context, CacheX cache) {
        super(context, Registry.TEAMBITION, cache);
    }

    /**
     * Retrieves the access token from Teambition's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        Map<String, String> form = new HashMap<>(7);
        form.put("client_id", context.getClientId());
        form.put("client_secret", context.getClientSecret());
        form.put("code", callback.getCode());
        form.put("grant_type", "code");

        String response = Httpx.post(this.complex.token(), form);
        Map<String, Object> object = JsonKit.toPojo(response, Map.class);

        this.checkResponse(object);

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Authorization.builder().token((String) object.get("access_token"))
                                .refresh((String) object.get("refresh_token")).build())
                .build();
    }

    /**
     * Retrieves user information from Teambition's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String token = authorization.getToken();
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.AUTHORIZATION, "OAuth2 " + token);

        String response = Httpx.get(this.complex.userinfo(), null, header);
        Map<String, Object> object = JsonKit.toPojo(response, Map.class);

        this.checkResponse(object);

        authorization.setUid((String) object.get("_id"));

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Claims.builder().rawJson(JsonKit.toJsonString(object)).uuid((String) object.get("_id"))
                                .username((String) object.get("name")).nickname((String) object.get("name"))
                                .avatar((String) object.get("avatarUrl")).blog((String) object.get("website"))
                                .location((String) object.get("location")).email((String) object.get("email"))
                                .gender(Gender.UNKNOWN).token(authorization).source(complex.toString()).build())
                .build();
    }

    /**
     * Refreshes the access token (renews its validity).
     *
     * @param authorization the token information returned after successful login
     * @return a {@link Message} containing the refreshed token information
     * @throws AuthorizedException if parsing the response fails or an error occurs during token refresh
     */
    @Override
    public Message refresh(Authorization authorization) {
        String uid = authorization.getUid();
        String refresh = authorization.getRefresh();

        Map<String, String> form = new HashMap<>(4);
        form.put("_userId", uid);
        form.put("refresh_token", refresh);
        String response = Httpx.post(this.complex.refresh(), form);
        Map<String, Object> object = JsonKit.toPojo(response, Map.class);

        this.checkResponse(object);

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Authorization.builder().token((String) object.get("access_token"))
                                .refresh((String) object.get("refresh_token")).build())
                .build();
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response indicates an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("message") && object.containsKey("name")) {
            throw new AuthorizedException((String) object.get("name") + ", " + (String) object.get("message"));
        }
    }

}
