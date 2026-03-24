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
package org.miaixz.bus.auth.nimble.slack;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Slack login provider.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SlackProvider extends AbstractProvider {

    /**
     * Constructs a {@code SlackProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public SlackProvider(Context context) {
        super(context, Registry.SLACK);
    }

    /**
     * Constructs a {@code SlackProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public SlackProvider(Context context, CacheX cache) {
        super(context, Registry.SLACK, cache);
    }

    /**
     * Retrieves the access token from Slack's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        String response = Httpx.get(tokenUrl(callback.getCode()), null, header);
        Map<String, Object> object = JsonKit.toPojo(response, Map.class);

        this.checkResponse(object);

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Authorization.builder().token((String) object.get("access_token"))
                                .scope((String) object.get("scope")).token_type((String) object.get("token_type"))
                                .uid(((Map<String, Object>) object.get("authed_user")).get("id").toString()).build())
                .build();
    }

    /**
     * Retrieves user information from Slack's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        header.put(HTTP.AUTHORIZATION, HTTP.BEARER.concat(authorization.getToken()));
        String userInfo = Httpx.get(userInfoUrl(authorization), null, header);
        Map<String, Object> object = JsonKit.toPojo(userInfo, Map.class);
        this.checkResponse(object);
        Map<String, Object> user = (Map<String, Object>) object.get("user");
        Map<String, Object> profile = (Map<String, Object>) user.get("profile");

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Claims.builder().rawJson(JsonKit.toJsonString(user)).uuid((String) user.get("id"))
                                .username((String) user.get("name")).nickname((String) user.get("real_name"))
                                .avatar((String) profile.get("image_original")).email((String) profile.get("email"))
                                .gender(Gender.UNKNOWN).token(authorization).source(complex.toString()).build())
                .build();
    }

    /**
     * Revokes the authorization for the given access token.
     *
     * @param authorization the token information to revoke
     * @return a {@link Message} indicating the result of the revocation
     * @throws AuthorizedException if parsing the response fails or an error occurs during revocation
     */
    @Override
    public Message revoke(Authorization authorization) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        header.put(HTTP.AUTHORIZATION, HTTP.BEARER.concat(authorization.getToken()));
        String userInfo = Httpx.get(this.complex.revoke(), null, header);
        Map<String, Object> object = JsonKit.toPojo(userInfo, Map.class);
        this.checkResponse(object);
        // Returns true for successful authorization cancellation, otherwise false
        Errors status = (Boolean) object.get("revoked") ? ErrorCode._SUCCESS : ErrorCode._FAILURE;
        return Message.builder().errcode(status.getKey()).errmsg(status.getValue()).build();
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response indicates an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if (!((Boolean) object.get("ok"))) {
            String errorMsg = (String) object.get("error");
            if (object.containsKey("response_metadata")) {
                Map<String, Object> responseMetadata = (Map<String, Object>) object.get("response_metadata");
                if (responseMetadata.containsKey("messages")) {
                    List<String> messages = (List<String>) responseMetadata.get("messages");
                    if (messages != null && !messages.isEmpty()) {
                        errorMsg += "; " + StringKit.join(Symbol.COMMA, messages.toArray(new String[0]));
                    }
                }
            }
            throw new AuthorizedException(errorMsg);
        }
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authorization the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    public String userInfoUrl(Authorization authorization) {
        return Builder.fromUrl(this.complex.userinfo()).queryParam("user", authorization.getUid()).build();
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
                Builder.fromUrl(complex.authorize()).queryParam("client_id", context.getClientId())
                        .queryParam("state", getRealState(state)).queryParam("redirect_uri", context.getRedirectUri())
                        .queryParam("scope", this.getScopes(Symbol.COMMA, true, this.getScopes(SlackScope.values())))
                        .build())
                .build();
    }

    /**
     * Returns the URL to obtain the access token.
     *
     * @param code the authorization code
     * @return the URL to obtain the access token
     */
    @Override
    protected String tokenUrl(String code) {
        return Builder.fromUrl(this.complex.token()).queryParam("code", code)
                .queryParam("client_id", context.getClientId()).queryParam("client_secret", context.getClientSecret())
                .queryParam("redirect_uri", context.getRedirectUri()).build();
    }

}
