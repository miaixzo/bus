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
package org.miaixz.bus.auth.vendor.dingtalk;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for the DingTalk OpenID Connect QR-code flow.
 *
 * <p>
 * The provider owns its distinct build, token, and user-information operations. HTTP calls use the injected Fabric
 * context, authorization state uses the injected atomic store, and the client secret is resolved only for the token
 * request.
 * </p>
 *
 * @author Kimi Liu
 */
public class DingTalkProvider extends AbstractDingtalkProvider {

    /**
     * Creates a DingTalk OpenID Connect client from explicit runtime dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     * @throws AuthorizedException  if the DingTalk registration is invalid
     */
    public DingTalkProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.DINGTALK);
    }

    /**
     * Builds the DingTalk consent URL with organization options and atomically registers state.
     *
     * @param context immutable root operation context used for state ownership
     * @param state   optional caller-supplied state; a generated state is used when absent
     * @return successful client message containing the complete authorization URL
     * @throws NullPointerException if {@code context} is null
     * @throws AuthorizedException  if the authorization endpoint is absent or state registration fails
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE)).queryParam("response_type", "code")
                        .queryParam("client_id", registration.clientId())
                        .queryParam("scope", scopes(Symbol.COMMA, true, getScopes(DingTalkScope.values())))
                        .queryParam("redirect_uri", registration.redirectUri()).queryParam("prompt", "consent")
                        .queryParam("org_type", registration.type()).queryParam("corpId", registration.unionId())
                        .queryParam("exclusiveLogin", registration.loginType())
                        .queryParam("exclusiveCorpId", registration.extId()).queryParam("state", state(current, state))
                        .build());
    }

    /**
     * Exchanges an authorization code using the DingTalk JSON token request.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing the standard {@code code} parameter
     * @return successful client message containing DingTalk token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the token endpoint, secret, response, or access token is absent
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("grantType", "authorization_code");
        parameters.put("clientId", registration.clientId());
        parameters.put("clientSecret", secret(current));
        parameters.put("code", inbound.value("code").orElse(null));
        final TokenResponse response = JsonKit.toPojo(
                post(endpoint(VendorEndpoint.TOKEN), JsonKit.toJsonString(parameters), MediaType.APPLICATION_JSON),
                TokenResponse.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse DingTalk token response: empty response");
        }
        if (response.accessToken() == null) {
            throw new AuthorizedException("Missing accessToken in DingTalk token response");
        }
        return Message.success(
                VendorTokenSet.builder().token(response.accessToken()).refresh(response.refreshToken())
                        .expireIn(response.expireIn()).unionId(response.corpId()).build());
    }

    /**
     * Retrieves the DingTalk profile using the access-token header and maps its identity fields.
     *
     * @param context immutable root operation context for this profile operation
     * @param token   non-null DingTalk token set
     * @return successful client message containing the mapped DingTalk identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the user-information endpoint, response, or union identifier is absent
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final UserResponse response = JsonKit.toPojo(
                get(
                        endpoint(VendorEndpoint.USERINFO),
                        Map.of(),
                        Map.of("x-acs-dingtalk-access-token", authorization.getToken())),
                UserResponse.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse DingTalk user response: empty response");
        }
        if (response.unionId() == null) {
            throw new AuthorizedException("Missing unionId in DingTalk user response");
        }
        authorization.setOpenId(response.openId());
        authorization.setUnionId(response.unionId());
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.unionId())
                        .username(response.nick()).nickname(response.nick()).avatar(response.avatarUrl())
                        .snapshotUser(response.visitor()).token(authorization).source(descriptor().id()).build());
    }

    /**
     * Typed DingTalk token response.
     *
     * @param accessToken  access token
     * @param refreshToken refresh token
     * @param expireIn     access-token lifetime in seconds
     * @param corpId       selected organization identifier
     * @author Kimi Liu
     */
    private record TokenResponse(String accessToken, String refreshToken, int expireIn, String corpId) {
    }

    /**
     * Typed DingTalk user-information response.
     *
     * @param openId    application-scoped user identifier
     * @param unionId   cross-application user identifier
     * @param nick      display name
     * @param avatarUrl avatar URL
     * @param visitor   whether DingTalk marked the identity as a visitor snapshot
     * @author Kimi Liu
     */
    private record UserResponse(String openId, String unionId, String nick, String avatarUrl, boolean visitor) {
    }

}
