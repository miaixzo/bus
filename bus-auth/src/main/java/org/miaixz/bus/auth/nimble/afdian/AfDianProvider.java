/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.nimble.afdian;

import java.util.HashMap;
import java.util.Map;

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
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

/**
 * AfDian login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AfDianProvider extends AbstractProvider {

    /**
     * Constructs an {@code AfDianProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public AfDianProvider(Context context) {
        super(context, Registry.AFDIAN);
    }

    /**
     * Constructs an {@code AfDianProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public AfDianProvider(Context context, CacheX cache) {
        super(context, Registry.AFDIAN, cache);
    }

    /**
     * Retrieves the access token from AfDian's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     */
    @Override
    public Message token(Callback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", this.context.getClientId());
        params.put("client_secret", this.context.getClientSecret());
        params.put("code", callback.getCode());
        params.put("redirect_uri", this.context.getRedirectUri());

        String response = Httpx.post(this.complex.token(), params);

        String userId = JsonKit.getValue(JsonKit.getValue(response, Consts.DATA), ("user_id"));

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(Authorization.builder().userId(userId).build()).build();
    }

    /**
     * Retrieves user information from AfDian's user info endpoint. Note: AfDian does not provide a direct user info
     * endpoint. User ID is extracted from the access token response.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     */
    @Override
    public Message userInfo(Authorization authorization) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Claims.builder().uuid(authorization.getUserId()).gender(Gender.UNKNOWN).token(authorization)
                                .source(complex.toString()).build())
                .build();
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
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Builder.fromUrl(this.complex.authorize()).queryParam("response_type", "code")
                                .queryParam("scope", "basic").queryParam("client_id", context.getClientId())
                                .queryParam("redirect_uri", context.getRedirectUri())
                                .queryParam("state", getRealState(state)).build())
                .build();
    }

}
