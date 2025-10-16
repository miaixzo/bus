/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
import org.miaixz.bus.auth.magic.AuthToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Slack login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
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
     * @return the {@link AuthToken} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public AuthToken getAccessToken(Callback callback) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        String response = Httpx.get(accessTokenUrl(callback.getCode()), null, header);
        Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
        this.checkResponse(accessTokenObject);
        return AuthToken.builder().accessToken((String) accessTokenObject.get("access_token"))
                .scope((String) accessTokenObject.get("scope")).tokenType((String) accessTokenObject.get("token_type"))
                .uid(((Map<String, Object>) accessTokenObject.get("authed_user")).get("id").toString()).build();
    }

    /**
     * Retrieves user information from Slack's user info endpoint.
     *
     * @param authToken the {@link AuthToken} obtained after successful authorization
     * @return {@link Material} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Material getUserInfo(AuthToken authToken) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        header.put("Authorization", "Bearer ".concat(authToken.getAccessToken()));
        String userInfo = Httpx.get(userInfoUrl(authToken), null, header);
        Map<String, Object> object = JsonKit.toPojo(userInfo, Map.class);
        this.checkResponse(object);
        Map<String, Object> user = (Map<String, Object>) object.get("user");
        Map<String, Object> profile = (Map<String, Object>) user.get("profile");
        return Material.builder().rawJson(JsonKit.toJsonString(user)).uuid((String) user.get("id"))
                .username((String) user.get("name")).nickname((String) user.get("real_name"))
                .avatar((String) profile.get("image_original")).email((String) profile.get("email"))
                .gender(Gender.UNKNOWN).token(authToken).source(complex.toString()).build();
    }

    /**
     * Revokes the authorization for the given access token.
     *
     * @param authToken the token information to revoke
     * @return a {@link Message} indicating the result of the revocation
     * @throws AuthorizedException if parsing the response fails or an error occurs during revocation
     */
    @Override
    public Message revoke(AuthToken authToken) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        header.put("Authorization", "Bearer ".concat(authToken.getAccessToken()));
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
     * @param authToken the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    public String userInfoUrl(AuthToken authToken) {
        return Builder.fromUrl(this.complex.userinfo()).queryParam("user", authToken.getUid()).build();
    }

    /**
     * Returns the authorization URL with a {@code state} parameter. The {@code state} will be included in the
     * authorization callback.
     *
     * @param state the parameter to verify the authorization process, which can prevent CSRF attacks
     * @return the authorization URL
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize()).queryParam("client_id", context.getAppKey())
                .queryParam("state", getRealState(state)).queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", this.getScopes(Symbol.COMMA, true, this.getDefaultScopes(SlackScope.values())))
                .build();
    }

    /**
     * Returns the URL to obtain the access token.
     *
     * @param code the authorization code
     * @return the URL to obtain the access token
     */
    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(this.complex.accessToken()).queryParam("code", code)
                .queryParam("client_id", context.getAppKey()).queryParam("client_secret", context.getAppSecret())
                .queryParam("redirect_uri", context.getRedirectUri()).build();
    }

}
