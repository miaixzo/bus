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
package org.miaixz.bus.auth.nimble.stackoverflow;

import org.miaixz.bus.auth.magic.AuthToken;
import org.miaixz.bus.cache.CacheX;
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
import org.miaixz.bus.auth.magic.Material;
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
     * @return the {@link AuthToken} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public AuthToken getAccessToken(Callback callback) {
        String accessTokenUrl = accessTokenUrl(callback.getCode());
        Map<String, String> form = new HashMap<>();
        UrlDecoder.decodeMap(accessTokenUrl, Charset.DEFAULT_UTF_8).forEach(form::put);

        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        String response = Httpx.post(accessTokenUrl, form, header);

        Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
        this.checkResponse(accessTokenObject);

        return AuthToken.builder().accessToken((String) accessTokenObject.get("access_token"))
                .expireIn(((Number) accessTokenObject.get("expires")).intValue()).build();
    }

    /**
     * Retrieves user information from Stack Overflow's user info endpoint.
     *
     * @param authToken the {@link AuthToken} obtained after successful authorization
     * @return {@link Material} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Material getUserInfo(AuthToken authToken) {
        String userInfoUrl = Builder.fromUrl(this.complex.userinfo())
                .queryParam("access_token", authToken.getAccessToken()).queryParam("site", "stackoverflow")
                .queryParam("key", this.context.getUnionId()).build();
        String response = Httpx.get(userInfoUrl);
        Map<String, Object> object = JsonKit.toPojo(response, Map.class);
        this.checkResponse(object);
        Map<String, Object> userObj = (Map<String, Object>) ((List<Object>) object.get("items")).get(0);

        return Material.builder().rawJson(JsonKit.toJsonString(userObj)).uuid((String) userObj.get("user_id"))
                .avatar((String) userObj.get("profile_image")).location((String) userObj.get("location"))
                .nickname((String) userObj.get("display_name")).blog((String) userObj.get("website_url"))
                .gender(Gender.UNKNOWN).token(authToken).source(complex.toString()).build();
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
        return Builder.fromUrl(super.authorize(state))
                .queryParam(
                        "scope",
                        this.getScopes(Symbol.COMMA, false, this.getDefaultScopes(StackoverflowScope.values())))
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
