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

import org.miaixz.bus.auth.magic.Authorization;
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
import org.miaixz.bus.auth.magic.Claims;
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
     * @return the {@link Authorization} containing access token details
     */
    @Override
    public Message token(Callback callback) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(getToken(tokenUrl(callback.getCode())))
                .build();
    }

    /**
     * Retrieves the token from the given access token URL.
     *
     * @param tokenUrl the URL to fetch the access token from
     * @return the {@link Authorization} containing token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    private Authorization getToken(String tokenUrl) {
        String response = Httpx.get(tokenUrl);
        String jsonStr = response.replace(PREFIX, Normal.EMPTY);
        try {
            Map<String, Object> object = JsonKit.toPojo(jsonStr, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }

            if (object.containsKey("error")) {
                String errorDescription = (String) object.get("error_description");
                throw new AuthorizedException(errorDescription != null ? errorDescription : "Unknown error");
            }

            String token = (String) object.get("access_token");
            if (token == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String scope = (String) object.get("scope");
            String tokenType = (String) object.get("token_type");
            String refresh = (String) object.get("refresh_token");
            String openId = (String) object.get("openId");
            String macAlgorithm = (String) object.get("mac_algorithm");
            String macKey = (String) object.get("mac_key");

            return Authorization.builder().token(token).expireIn(expiresIn).scope(scope).token_type(tokenType)
                    .refresh(refresh).openId(openId).macAlgorithm(macAlgorithm).macKey(macKey).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * Retrieves user information from Xiaomi's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        // Get user information
        String userResponse = doGetUserInfo(authorization);
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

            Claims authUser = Claims.builder().rawJson(JsonKit.toJsonString(object)).uuid(authorization.getOpenId())
                    .username(miliaoNick).nickname(miliaoNick).avatar(miliaoIcon).email(mail).gender(Gender.UNKNOWN)
                    .token(authorization).source(complex.toString()).build();

            // Get user email and phone number information
            String emailPhoneUrl = MessageFormat.format(
                    "{0}?clientId={1}&token={2}",
                    "https://open.account.xiaomi.com/user/phoneAndEmail",
                    context.getClientId(),
                    authorization.getToken());

            String emailResponse = Httpx.get(emailPhoneUrl);
            try {
                Map<String, Object> userEmailPhone = JsonKit.toPojo(emailResponse, Map.class);
                if (userEmailPhone == null) {
                    Logger.warn("Failed to parse email/phone response: empty response");
                    return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(authUser).build();
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

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(authUser).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Refreshes the access token (renews its validity).
     *
     * @param authorization the token information returned after successful login
     * @return a {@link Message} containing the refreshed token information
     */
    @Override
    public Message refresh(Authorization authorization) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(getToken(refreshUrl(authorization.getRefresh()))).build();
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
                Builder.fromUrl((String) super.build(state).getData()).queryParam("skip_confirm", "false")
                        .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getScopes(MiScope.values())))
                        .build())
                .build();
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authorization the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    protected String userInfoUrl(Authorization authorization) {
        return Builder.fromUrl(this.complex.userinfo()).queryParam("clientId", context.getClientId())
                .queryParam("token", authorization.getToken()).build();
    }

}
