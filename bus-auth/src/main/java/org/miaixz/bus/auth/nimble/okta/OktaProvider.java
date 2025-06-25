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
package org.miaixz.bus.auth.nimble.okta;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.AccToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

/**
 * Okta 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OktaProvider extends AbstractProvider {

    public OktaProvider(Context context) {
        super(context, Registry.OKTA);
    }

    public OktaProvider(Context context, ExtendCache cache) {
        super(context, Registry.OKTA, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        String tokenUrl = accessTokenUrl(callback.getCode());
        return getAuthToken(tokenUrl);
    }

    private AccToken getAuthToken(String tokenUrl) {
        Map<String, String> header = new HashMap<>();
        header.put("accept", MediaType.APPLICATION_JSON);
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        header.put("Authorization",
                "Basic " + Base64.encode(context.getAppKey().concat(Symbol.COLON).concat(context.getAppSecret())));

        String response = Httpx.post(tokenUrl, null, header);
        try {
            Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
            if (accessTokenObject == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }

            this.checkResponse(accessTokenObject);

            String accessToken = (String) accessTokenObject.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String tokenType = (String) accessTokenObject.get("token_type");
            Object expiresInObj = accessTokenObject.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String scope = (String) accessTokenObject.get("scope");
            String refreshToken = (String) accessTokenObject.get("refresh_token");
            String idToken = (String) accessTokenObject.get("id_token");

            return AccToken.builder().accessToken(accessToken).tokenType(tokenType).expireIn(expiresIn).scope(scope)
                    .refreshToken(refreshToken).idToken(idToken).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    @Override
    public Message refresh(AccToken accToken) {
        if (null == accToken.getRefreshToken()) {
            return Message.builder().errcode(ErrorCode.ILLEGAL_TOKEN.getKey())
                    .errmsg(ErrorCode.ILLEGAL_TOKEN.getValue()).build();
        }
        String refreshUrl = refreshTokenUrl(accToken.getRefreshToken());
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(this.getAuthToken(refreshUrl)).build();
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer " + accToken.getAccessToken());

        String response = Httpx.post(userInfoUrl(accToken), null, header);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            this.checkResponse(object);

            String sub = (String) object.get("sub");
            if (sub == null) {
                throw new AuthorizedException("Missing sub in user info response");
            }
            String name = (String) object.get("name");
            String nickname = (String) object.get("nickname");
            String email = (String) object.get("email");
            String sex = (String) object.get("sex");
            Map<String, Object> address = (Map<String, Object>) object.get("address");
            String streetAddress = address != null ? (String) address.get("street_address") : null;

            return Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(sub).username(name).nickname(nickname)
                    .email(email).location(streetAddress).gender(Gender.of(sex)).token(accToken)
                    .source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    @Override
    public Message revoke(AccToken accToken) {
        Map<String, String> params = new HashMap<>(4);
        params.put("token", accToken.getAccessToken());
        params.put("token_type_hint", "access_token");

        Map<String, String> header = new HashMap<>();
        header.put("Authorization",
                "Basic " + Base64.encode(context.getAppKey().concat(Symbol.COLON).concat(context.getAppSecret())));

        Httpx.post(revokeUrl(accToken), params, header);
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
    }

    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error")) {
            String errorDescription = (String) object.get("error_description");
            throw new AuthorizedException(errorDescription != null ? errorDescription : "Unknown error");
        }
    }

    @Override
    public String authorize(String state) {
        return Builder
                .fromUrl(String.format(complex.getConfig().get(Builder.AUTHORIZE), context.getPrefix(),
                        ObjectKit.defaultIfNull(context.getUnionId(), "default")))
                .queryParam("response_type", "code").queryParam("prompt", "consent")
                .queryParam("client_id", context.getAppKey()).queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getDefaultScopes(OktaScope.values())))
                .queryParam("state", getRealState(state)).build();
    }

    @Override
    public String accessTokenUrl(String code) {
        return Builder
                .fromUrl(String.format(this.complex.getConfig().get(Builder.ACCESSTOKEN), context.getPrefix(),
                        ObjectKit.defaultIfNull(context.getUnionId(), "default")))
                .queryParam("code", code).queryParam("grant_type", "authorization_code")
                .queryParam("redirect_uri", context.getRedirectUri()).build();
    }

    @Override
    protected String refreshTokenUrl(String refreshToken) {
        return Builder
                .fromUrl(String.format(this.complex.getConfig().get(Builder.REFRESH), context.getPrefix(),
                        ObjectKit.defaultIfNull(context.getUnionId(), "default")))
                .queryParam("refresh_token", refreshToken).queryParam("grant_type", "refresh_token").build();
    }

    @Override
    protected String revokeUrl(AccToken accToken) {
        return String.format(this.complex.getConfig().get(Builder.REVOKE), context.getPrefix(),
                ObjectKit.defaultIfNull(context.getUnionId(), "default"));
    }

    @Override
    public String userInfoUrl(AccToken accToken) {
        return String.format(this.complex.getConfig().get(Builder.USERINFO), context.getPrefix(),
                ObjectKit.defaultIfNull(context.getUnionId(), "default"));
    }

}