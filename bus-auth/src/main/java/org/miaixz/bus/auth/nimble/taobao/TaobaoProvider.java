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
import org.miaixz.bus.auth.magic.AuthToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
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
     * @return the {@link AuthToken} containing access token details
     */
    @Override
    public AuthToken getAccessToken(Callback callback) {
        return AuthToken.builder().accessCode(callback.getCode()).build();
    }

    /**
     * Parses the access token response map into an {@link AuthToken} object.
     *
     * @param object the response map from the access token endpoint
     * @return the parsed {@link AuthToken}
     * @throws AuthorizedException if the response indicates an error or is missing required token information
     */
    private AuthToken getAuthToken(Map<String, Object> object) {
        this.checkResponse(object);

        return AuthToken.builder().accessToken((String) object.get("access_token"))
                .expireIn(((Number) object.get("expires_in")).intValue()).tokenType((String) object.get("token_type"))
                .idToken((String) object.get("id_token")).refreshToken((String) object.get("refresh_token"))
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
     * @param authToken the {@link AuthToken} obtained after successful authorization
     * @return {@link Material} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Material getUserInfo(AuthToken authToken) {
        String response = doPostAuthorizationCode(authToken.getAccessCode());
        Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
        if (accessTokenObject.containsKey("error")) {
            throw new AuthorizedException((String) accessTokenObject.get("error_description"));
        }
        authToken = this.getAuthToken(accessTokenObject);

        String nick = UrlDecoder.decode((String) accessTokenObject.get("taobao_user_nick"));
        return Material.builder().rawJson(JsonKit.toJsonString(accessTokenObject))
                .uuid(StringKit.isEmpty(authToken.getUid()) ? authToken.getOpenId() : authToken.getUid()).username(nick)
                .nickname(nick).gender(Gender.UNKNOWN).token(authToken).source(complex.toString()).build();
    }

    /**
     * Refreshes the access token (renews its validity).
     *
     * @param authToken the token information returned after successful login
     * @return a {@link Message} containing the refreshed token information
     */
    @Override
    public Message refresh(AuthToken authToken) {
        String tokenUrl = refreshTokenUrl(authToken.getRefreshToken());
        String response = Httpx.post(tokenUrl);
        Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(this.getAuthToken(accessTokenObject))
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
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize()).queryParam("response_type", "code")
                .queryParam("client_id", context.getAppKey()).queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("view", "web").queryParam("state", getRealState(state)).build();
    }

}
