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
package org.miaixz.bus.auth.nimble.oidc;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

import java.util.*;

/**
 * Generic OIDC (OpenID Connect) protocol provider. This provider allows supporting any OIDC-compatible authentication
 * service by dynamically configuring OIDC service endpoints in {@link Context}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OIDCProvider extends AbstractProvider {

    /**
     * Constructs a {@code MicrosoftCnProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public OIDCProvider(Context context) {
        super(context, Registry.OIDC);
    }

    /**
     * Constructs a {@code MicrosoftCnProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public OIDCProvider(Context context, CacheX cache) {
        super(context, Registry.OIDC, cache);
    }

    /**
     * Builds the authorization URL for OIDC authentication.
     *
     * @param state the state parameter for CSRF protection
     * @return a message containing the authorization URL
     */
    @Override
    public Message build(String state) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Builder.fromUrl(getEndpoint(Endpoint.AUTHORIZE)).queryParam("response_type", "code")
                                .queryParam("client_id", this.context.getClientId())
                                .queryParam("redirect_uri", this.context.getRedirectUri())
                                .queryParam("state", getRealState(state))
                                .queryParam("scope", getScopes(Symbol.SPACE, true, context.getScopes())).build())
                .build();
    }

    /**
     * Exchanges the authorization code for an access token.
     *
     * @param callback the callback containing the authorization code
     * @return a message containing the access token and related information
     */
    @Override
    public Message token(Callback callback) {
        String tokenUrl = getEndpoint(Endpoint.TOKEN);
        String response = Httpx.post(tokenUrl, this.tokenParams(callback));
        Map<String, Object> object = JsonKit.toMap(response);

        this.checkResponse(object);

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Authorization.builder().token((String) object.get("access_token"))
                                .token_type((String) object.get("token_type")).idToken((String) object.get("id_token"))
                                .scope((String) object.get("scope")).build())
                .build();
    }

    /**
     * Retrieves user information using the access token.
     *
     * @param authorization the authorization containing the access token
     * @return a message containing user claims and information
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String userInfoUrl = getEndpoint(Endpoint.USERINFO);
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.AUTHORIZATION, HTTP.BEARER + authorization.getToken());
        String response = Httpx.get(userInfoUrl, null, header);
        Map<String, Object> object = JsonKit.toMap(response);

        this.checkResponse(object);

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Claims.builder().rawJson(JsonKit.toJsonString(object)).uuid((String) object.get("sub"))
                        .username((String) object.get("preferred_username")).nickname((String) object.get("nickname"))
                        .avatar((String) object.get("picture")).blog((String) object.get("website"))
                        .email((String) object.get("email")).gender(Gender.of((String) object.get("gender")))
                        .token(authorization).source(complex.toString()).build())
                .build();
    }

    /**
     * Checks if the response contains error information
     *
     * @param object a {@link Map} object
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error")) {
            throw new AuthorizedException(ErrorCode._FAILURE.getKey(), this.complex,
                    object.get("error_description").toString());
        }
    }

    /**
     * Builds parameters for obtaining access token
     *
     * @param callback a {@link Callback} object
     * @return a {@link Map} object
     */
    private Map<String, String> tokenParams(Callback callback) {
        Map<String, String> params = new HashMap<>(5);
        params.put("code", callback.getCode());
        params.put("client_id", context.getClientId());
        params.put("client_secret", context.getClientSecret());
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", context.getRedirectUri());
        return params;
    }

}
