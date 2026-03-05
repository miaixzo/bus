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
package org.miaixz.bus.auth.nimble.taobao;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.Map;

/**
 * Taobao login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TaobaoProvider extends AbstractProvider {

    /**
     * Constructs a {@code TaobaoProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public TaobaoProvider(Context context) {
        super(context, Registry.TAOBAO);
    }

    /**
     * Constructs a {@code TaobaoProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public TaobaoProvider(Context context, CacheX cache) {
        super(context, Registry.TAOBAO, cache);
    }

    /**
     * Retrieves the access token from Taobao's authorization server. For Taobao, the access token is typically derived
     * from the authorization code.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     */
    @Override
    public Message token(Callback callback) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(Authorization.builder().token(callback.getCode()).build()).build();
    }

    /**
     * Parses the access token response map into an {@link Authorization} object.
     *
     * @param object the response map from the access token endpoint
     * @return the parsed {@link Authorization}
     * @throws AuthorizedException if the response indicates an error or is missing required token information
     */
    private Authorization getAuthToken(Map<String, Object> object) {
        this.checkResponse(object);

        return Authorization.builder().token((String) object.get("access_token"))
                .expireIn(((Number) object.get("expires_in")).intValue()).token_type((String) object.get("token_type"))
                .idToken((String) object.get("id_token")).refresh((String) object.get("refresh_token"))
                .uid((String) object.get("taobao_user_id")).openId((String) object.get("taobao_open_uid")).build();
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response contains an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error")) {
            throw new AuthorizedException((String) object.get("error_description"));
        }
    }

    /**
     * Retrieves user information from Taobao's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String response = doPostToken(authorization.getToken());
        Map<String, Object> object = JsonKit.toPojo(response, Map.class);
        if (object.containsKey("error")) {
            throw new AuthorizedException((String) object.get("error_description"));
        }
        authorization = this.getAuthToken(object);
        String nick = UrlDecoder.decode((String) object.get("taobao_user_nick"));

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Claims.builder().rawJson(JsonKit.toJsonString(object)).uuid(
                        StringKit.isEmpty(authorization.getUid()) ? authorization.getOpenId() : authorization.getUid())
                        .username(nick).nickname(nick).gender(Gender.UNKNOWN).token(authorization)
                        .source(complex.toString()).build())
                .build();
    }

    /**
     * Refreshes the access token (renews its validity).
     *
     * @param authorization the token information returned after successful login
     * @return a {@link Message} containing the refreshed token information
     */
    @Override
    public Message refresh(Authorization authorization) {
        String tokenUrl = refreshUrl(authorization.getRefresh());
        String response = Httpx.post(tokenUrl);
        Map<String, Object> object = JsonKit.toPojo(response, Map.class);
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(this.getAuthToken(object)).build();
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
                        Builder.fromUrl(complex.authorize()).queryParam("response_type", "code")
                                .queryParam("client_id", context.getClientId())
                                .queryParam("redirect_uri", context.getRedirectUri()).queryParam("view", "web")
                                .queryParam("state", getRealState(state)).build())
                .build();
    }

}
