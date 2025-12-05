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
package org.miaixz.bus.auth.nimble.dingtalk;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

/**
 * DingTalk QR code login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DingTalkProvider extends AbstractDingtalkProvider {

    /**
     * Constructs a {@code DingTalkProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public DingTalkProvider(Context context) {
        super(context, Registry.DINGTALK);
    }

    /**
     * Constructs a {@code DingTalkProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public DingTalkProvider(Context context, CacheX cache) {
        super(context, Registry.DINGTALK, cache);
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
                Builder.fromUrl(this.complex.authorize()).queryParam("response_type", "code")
                        .queryParam("client_id", context.getClientId())
                        .queryParam("scope", this.getScopes(Symbol.COMMA, true, getScopes(DingTalkScope.values())))
                        .queryParam("redirect_uri", context.getRedirectUri()).queryParam("prompt", "consent")
                        .queryParam("org_type", context.getType()).queryParam("corpId", context.getUnionId())
                        .queryParam("exclusiveLogin", context.getLoginType())
                        .queryParam("exclusiveCorpId", context.getExtId()).queryParam("state", getRealState(state))
                        .build())
                .build();
    }

    /**
     * Retrieves the access token from DingTalk's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("grantType", "authorization_code");
        params.put("clientId", context.getClientId());
        params.put("clientSecret", context.getClientSecret());
        params.put("code", callback.getCode());
        String response = Httpx.get(this.complex.token(), JsonKit.toJsonString(params));
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }
            if (!object.containsKey("accessToken")) {
                throw new AuthorizedException("Missing token in response: " + JsonKit.toJsonString(object));
            }

            String token = (String) object.get("accessToken");
            String refresh = (String) object.get("refreshToken");
            Object expireInObj = object.get("expireIn");
            int expireIn = expireInObj instanceof Number ? ((Number) expireInObj).intValue() : 0;
            String corpId = (String) object.get("corpId");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                    Authorization.builder().token(token).refresh(refresh).expireIn(expireIn).unionId(corpId).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * Retrieves user information from DingTalk's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        Map<String, String> header = new HashMap<>();
        header.put("x-acs-dingtalk-access-token", authorization.getToken());
        String response = Httpx.get(this.complex.userinfo(), new HashMap<>(0), header);

        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            String openId = (String) object.get("openId");
            String unionId = (String) object.get("unionId");
            if (unionId == null) {
                throw new AuthorizedException("Missing unionId in user info response");
            }
            String nick = (String) object.get("nick");
            String avatarUrl = (String) object.get("avatarUrl");
            Object visitorObj = object.get("visitor");
            boolean visitor = visitorObj instanceof Boolean ? (Boolean) visitorObj : false;

            authorization.setOpenId(openId);
            authorization.setUnionId(unionId);

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Claims.builder().rawJson(JsonKit.toJsonString(object)).uuid(unionId).username(nick)
                                    .nickname(nick).avatar(avatarUrl).snapshotUser(visitor).token(authorization)
                                    .source(complex.toString()).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
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
                .queryParam("clientId", context.getClientId()).queryParam("clientSecret", context.getClientSecret())
                .queryParam("grantType", "authorization_code").build();
    }

}
