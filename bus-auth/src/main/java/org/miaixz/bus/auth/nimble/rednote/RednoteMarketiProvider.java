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
package org.miaixz.bus.auth.nimble.rednote;

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
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

/**
 * Xiaohongshu Commercial Platform login provider.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RednoteMarketiProvider extends AbstractProvider {

    /**
     * Constructs a {@code RednoteMarketiProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public RednoteMarketiProvider(Context context) {
        super(context, Registry.REDNOTE_MARKET);
    }

    /**
     * Constructs a {@code RednoteMarketiProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public RednoteMarketiProvider(Context context, CacheX cache) {
        super(context, Registry.REDNOTE_MARKET, cache);
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
                        Builder.fromUrl(this.complex.authorize()).queryParam("appId", this.context.getClientId())
                                .queryParam("scope", this.getScopes(" ", true, getScopes(RednoteMarketiScope.values())))
                                .queryParam("redirectUri", this.context.getRedirectUri())
                                .queryParam("state", getRealState(state)).build())
                .build();
    }

    /**
     * Retrieves the access token from Xiaohongshu Commercial Platform's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        Map<String, String> form = new HashMap<>(7);
        form.put("app_id", this.context.getClientId());
        form.put("secret", this.context.getClientSecret());
        form.put("code", callback.getCode());
        String response = Httpx.post(this.complex.token(), form);
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

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                    Authorization.builder().token(token).expireIn((Integer) object.get("access_token_expires_in"))
                            .refresh((String) object.get("refresh_token")).scope((String) object.get("scope")).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * Retrieves user information from Xiaohongshu Commercial Platform's user info endpoint. Note: This platform does
     * not support direct user info retrieval via a dedicated URL.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws UnsupportedOperationException if this operation is not supported by the platform
     */
    @Override
    public Message userInfo(Authorization authorization) {
        throw new UnsupportedOperationException("User info URL is not supported");
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
        Map<String, String> form = new HashMap<>(7);
        form.put("app_id", this.context.getClientId());
        form.put("secret", this.context.getClientSecret());
        form.put("refresh_token", authorization.getRefresh());

        String response = Httpx.post(this.complex.refresh(), form);
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
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                    Authorization.builder().token(token).refresh((String) object.get("refresh_token"))
                            .scope((String) object.get("scope")).expireIn((Integer) object.get("expires_in")).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response indicates an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if ((Integer) object.get("code") != 0) {
            String error = (String) object.get("error");
            throw new AuthorizedException(error != null ? error : "Unknown error");
        }
        if (object.containsKey("error")) {
            String subError = (String) object.get("sub_error");
            String errorDescription = (String) object.get("error_description");
            throw new AuthorizedException((subError != null ? subError : "Unknown sub_error") + ":"
                    + (errorDescription != null ? errorDescription : "Unknown description"));
        }
    }

}
