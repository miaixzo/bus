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
package org.miaixz.bus.auth.nimble.stackoverflow;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stack Overflow login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StackOverflowProvider extends AbstractProvider {

    /**
     * Constructs a {@code StackOverflowProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public StackOverflowProvider(Context context) {
        super(context, Registry.STACK_OVERFLOW);
    }

    /**
     * Constructs a {@code StackOverflowProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public StackOverflowProvider(Context context, CacheX cache) {
        super(context, Registry.STACK_OVERFLOW, cache);
    }

    /**
     * Retrieves the access token from Stack Overflow's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        String tokenUrl = tokenUrl(callback.getCode());
        Map<String, String> form = new HashMap<>();
        UrlDecoder.decodeMap(tokenUrl, Charset.DEFAULT_UTF_8).forEach(form::put);

        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        String response = Httpx.post(tokenUrl, form, header);

        Map<String, Object> object = JsonKit.toPojo(response, Map.class);
        this.checkResponse(object);

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Authorization.builder().token((String) object.get("access_token"))
                                .expireIn(((Number) object.get("expires")).intValue()).build())
                .build();
    }

    /**
     * Retrieves user information from Stack Overflow's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String userInfoUrl = Builder.fromUrl(this.complex.userinfo())
                .queryParam("access_token", authorization.getToken()).queryParam("site", "stackoverflow")
                .queryParam("key", this.context.getUnionId()).build();
        String response = Httpx.get(userInfoUrl);
        Map<String, Object> object = JsonKit.toPojo(response, Map.class);
        this.checkResponse(object);
        Map<String, Object> userObj = (Map<String, Object>) ((List<Object>) object.get("items")).get(0);

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Claims.builder().rawJson(JsonKit.toJsonString(userObj)).uuid((String) userObj.get("user_id"))
                        .avatar((String) userObj.get("profile_image")).location((String) userObj.get("location"))
                        .nickname((String) userObj.get("display_name")).blog((String) userObj.get("website_url"))
                        .gender(Gender.UNKNOWN).token(authorization).source(complex.toString()).build())
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
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Builder.fromUrl((String) super.build(state).getData())
                        .queryParam(
                                "scope",
                                this.getScopes(Symbol.COMMA, false, this.getScopes(StackoverflowScope.values())))
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
            throw new AuthorizedException((String) object.get("error_description"));
        }
    }

}
