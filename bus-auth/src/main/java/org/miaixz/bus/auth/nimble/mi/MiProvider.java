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
package org.miaixz.bus.auth.nimble.mi;

import org.miaixz.bus.auth.magic.AuthToken;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.text.MessageFormat;
import java.util.Map;

/**
 * Xiaomi (Mi) login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MiProvider extends AbstractProvider {

    private static final String PREFIX = "&&&START&&&";

    /**
     * Constructs a {@code MiProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public MiProvider(Context context) {
        super(context, Registry.MI);
    }

    /**
     * Constructs a {@code MiProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public MiProvider(Context context, CacheX cache) {
        super(context, Registry.MI, cache);
    }

    /**
     * Retrieves the access token from Xiaomi's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link AuthToken} containing access token details
     */
    @Override
    public AuthToken getAccessToken(Callback callback) {
        return getToken(accessTokenUrl(callback.getCode()));
    }

    /**
     * Retrieves the token from the given access token URL.
     *
     * @param accessTokenUrl the URL to fetch the access token from
     * @return the {@link AuthToken} containing token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    private AuthToken getToken(String accessTokenUrl) {
        String response = Httpx.get(accessTokenUrl);
        String jsonStr = response.replace(PREFIX, Normal.EMPTY);
        try {
            Map<String, Object> accessTokenObject = JsonKit.toPojo(jsonStr, Map.class);
            if (accessTokenObject == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }

            if (accessTokenObject.containsKey("error")) {
                String errorDescription = (String) accessTokenObject.get("error_description");
                throw new AuthorizedException(errorDescription != null ? errorDescription : "Unknown error");
            }

            String accessToken = (String) accessTokenObject.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            Object expiresInObj = accessTokenObject.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String scope = (String) accessTokenObject.get("scope");
            String tokenType = (String) accessTokenObject.get("token_type");
            String refreshToken = (String) accessTokenObject.get("refresh_token");
            String openId = (String) accessTokenObject.get("openId");
            String macAlgorithm = (String) accessTokenObject.get("mac_algorithm");
            String macKey = (String) accessTokenObject.get("mac_key");

            return AuthToken.builder().accessToken(accessToken).expireIn(expiresIn).scope(scope).tokenType(tokenType)
                    .refreshToken(refreshToken).openId(openId).macAlgorithm(macAlgorithm).macKey(macKey).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * Retrieves user information from Xiaomi's user info endpoint.
     *
     * @param authToken the {@link AuthToken} obtained after successful authorization
     * @return {@link Material} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Material getUserInfo(AuthToken authToken) {
        // Get user information
        String userResponse = doGetUserInfo(authToken);
        try {
            Map<String, Object> userProfile = JsonKit.toPojo(userResponse, Map.class);
            if (userProfile == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            String result = (String) userProfile.get("result");
            if ("error".equalsIgnoreCase(result)) {
                String description = (String) userProfile.get("description");
                throw new AuthorizedException(description != null ? description : "Unknown error");
            }

            Map<String, Object> object = (Map<String, Object>) userProfile.get(Consts.DATA);
            if (object == null) {
                throw new AuthorizedException("Missing data in user info response");
            }

            String miliaoNick = (String) object.get("miliaoNick");
            if (miliaoNick == null) {
                throw new AuthorizedException("Missing miliaoNick in user info response");
            }
            String miliaoIcon = (String) object.get("miliaoIcon");
            String mail = (String) object.get("mail");

            Material authUser = Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(authToken.getOpenId())
                    .username(miliaoNick).nickname(miliaoNick).avatar(miliaoIcon).email(mail).gender(Gender.UNKNOWN)
                    .token(authToken).source(complex.toString()).build();

            // Get user email and phone number information
            String emailPhoneUrl = MessageFormat.format(
                    "{0}?clientId={1}&token={2}",
                    "https://open.account.xiaomi.com/user/phoneAndEmail",
                    context.getAppKey(),
                    authToken.getAccessToken());

            String emailResponse = Httpx.get(emailPhoneUrl);
            try {
                Map<String, Object> userEmailPhone = JsonKit.toPojo(emailResponse, Map.class);
                if (userEmailPhone == null) {
                    Logger.warn("Failed to parse email/phone response: empty response");
                    return authUser;
                }

                String emailResult = (String) userEmailPhone.get("result");
                if (!"error".equalsIgnoreCase(emailResult)) {
                    Map<String, Object> emailPhone = (Map<String, Object>) userEmailPhone.get(Consts.DATA);
                    if (emailPhone != null) {
                        String email = (String) emailPhone.get("email");
                        authUser.setEmail(email);
                    }
                } else {
                    Logger.warn(
                            "Xiaomi developer platform currently does not provide access to user phone and email information");
                }
            } catch (Exception e) {
                Logger.warn("Failed to parse email/phone response: " + e.getMessage());
            }

            return authUser;
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Refreshes the access token (renews its validity).
     *
     * @param authToken the token information returned after successful login
     * @return a {@link Message} containing the refreshed token information
     */
    @Override
    public Message refresh(AuthToken authToken) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(getToken(refreshTokenUrl(authToken.getRefreshToken()))).build();
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
        return Builder.fromUrl(super.authorize(state)).queryParam("skip_confirm", "false")
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getDefaultScopes(MiScope.values())))
                .build();
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authToken the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    protected String userInfoUrl(AuthToken authToken) {
        return Builder.fromUrl(this.complex.userinfo()).queryParam("clientId", context.getAppKey())
                .queryParam("token", authToken.getAccessToken()).build();
    }

}
