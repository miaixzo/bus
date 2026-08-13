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
package org.miaixz.bus.auth.vendor.weibo;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Weibo authorization, profile retrieval, and token revocation.
 *
 * @author Kimi Liu
 */
public class WeiboProvider extends AbstractProvider {

    /**
     * Default scope selected when the registration has no explicit scopes.
     */
    private static final List<String> DEFAULT_SCOPES = List.of("all");

    /**
     * Creates a Weibo client from explicit runtime dependencies.
     *
     * @param configuration complete non-null vendor dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     */
    public WeiboProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.WEIBO);
    }

    /**
     * Builds the Weibo authorization URL and atomically registers state.
     *
     * @param context immutable root operation context used to isolate state
     * @param state   optional caller-supplied authorization state
     * @return successful message containing the authorization URL
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE)).queryParam("response_type", "code")
                        .queryParam("client_id", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("state", state(current, state))
                        .queryParam("scope", scopes(Symbol.COMMA, false, DEFAULT_SCOPES)).build());
    }

    /**
     * Exchanges an authorization code through Weibo's query-bearing empty-form POST operation.
     *
     * @param context  root operation context used to resolve and clear the client secret
     * @param callback immutable inbound callback containing the authorization code
     * @return successful message containing access-token fields
     * @throws AuthorizedException if the response is absent, reports an error, or omits the token
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final TokenResponse response = JsonKit
                .toPojo(doPostToken(current, inbound.value("code").orElse(null)), TokenResponse.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse Weibo token response: empty response");
        }
        if (response.error() != null) {
            throw new AuthorizedException(
                    response.error_description() == null ? response.error() : response.error_description());
        }
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in Weibo response");
        }
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).uid(response.uid()).openId(response.uid())
                        .expireIn(response.expires_in()).build());
    }

    /**
     * Retrieves the Weibo profile with the historical query and OAuth2 headers.
     *
     * @param context immutable root operation context
     * @param token   non-null token set
     * @return successful message containing the mapped identity
     * @throws AuthorizedException if the response is absent, reports an error, or omits the identifier
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.USERINFO))
                .queryParam("access_token", authorization.getToken()).queryParam("uid", authorization.getUid()).build();
        final String oauth = "uid=" + authorization.getUid() + "&access_token=" + authorization.getToken();
        final ProfileResponse profile = JsonKit.toPojo(
                get(
                        url,
                        null,
                        Map.of(
                                Http.Header.AUTHORIZATION,
                                "OAuth2 " + oauth,
                                "API-RemoteIP",
                                NetKit.getLocalhostStringV4())),
                ProfileResponse.class);
        if (profile == null) {
            throw new AuthorizedException("Failed to parse Weibo profile response: empty response");
        }
        if (profile.error() != null) {
            throw new AuthorizedException(profile.error());
        }
        if (profile.id() == null) {
            throw new AuthorizedException("Missing id in Weibo profile response");
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(profile)).uuid(profile.id())
                        .username(profile.name()).avatar(profile.profile_image_url())
                        .blog(
                                StringKit.isEmpty(profile.url()) ? "https://weibo.com/" + profile.profile_url()
                                        : profile.url())
                        .nickname(profile.screen_name()).location(profile.location()).remark(profile.description())
                        .gender(Gender.of(profile.gender())).token(authorization).source(descriptor().id()).build());
    }

    /**
     * Revokes the access token through Weibo's query-authenticated GET operation.
     *
     * @param context immutable root operation context
     * @param token   non-null token set
     * @return success when Weibo returns {@code result=true}; otherwise a failure message
     * @throws AuthorizedException if the response cannot be parsed
     */
    @Override
    public Message<Void> revoke(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final RevokeResponse response = JsonKit.toPojo(
                get(
                        VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.REVOKE))
                                .queryParam("access_token", authorization.getToken()).build()),
                RevokeResponse.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse Weibo revoke response: empty response");
        }
        if (response.error() != null) {
            return Message.failure(ErrorCode._FAILURE.getKey(), response.error());
        }
        return Boolean.TRUE.equals(response.result()) ? Message.success(null) : Message.failure(ErrorCode._FAILURE);
    }

    /**
     * Typed Weibo token response.
     *
     * @param error             vendor error name
     * @param error_description vendor error diagnostic
     * @param access_token      access token
     * @param uid               user identifier
     * @param expires_in        token lifetime in seconds
     * @author Kimi Liu
     */
    private record TokenResponse(String error, String error_description, String access_token, String uid,
            int expires_in) {
    }

    /**
     * Typed Weibo profile response.
     *
     * @param error             vendor error diagnostic
     * @param id                user identifier
     * @param name              account name
     * @param profile_image_url avatar URL
     * @param url               blog URL
     * @param profile_url       fallback profile path
     * @param screen_name       display name
     * @param location          location text
     * @param description       profile description
     * @param gender            vendor gender code
     * @author Kimi Liu
     */
    private record ProfileResponse(String error, String id, String name, String profile_image_url, String url,
            String profile_url, String screen_name, String location, String description, String gender) {
    }

    /**
     * Typed Weibo revocation response.
     *
     * @param error  vendor error diagnostic
     * @param result revocation result
     * @author Kimi Liu
     */
    private record RevokeResponse(String error, Boolean result) {
    }

}
