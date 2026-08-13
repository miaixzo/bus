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
package org.miaixz.bus.auth.vendor.renren;

import java.util.List;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Renren authorization, token, profile, and refresh operations.
 *
 * <p>
 * All remote calls use the injected Fabric context. Credentialed operations resolve a client secret for the current
 * root context and clear its caller-owned character buffer after request construction.
 * </p>
 *
 * @author Kimi Liu
 */
public class RenrenProvider extends AbstractProvider {

    /**
     * Creates a Renren client from explicit runtime dependencies.
     *
     * @param configuration complete non-null vendor dependency aggregate
     */
    public RenrenProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.RENREN);
    }

    /**
     * Parses and validates a Renren token response.
     *
     * @param document JSON response document
     * @return mapped token set
     * @throws AuthorizedException if the response contains an error or lacks required fields
     */
    private static VendorTokenSet readToken(final String document) {
        final TokenResponse response = JsonKit.toPojo(document, TokenResponse.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse Renren token response: empty response");
        }
        if (response.error() != null) {
            throw new AuthorizedException("Renren token endpoint returned an error");
        }
        if (response.access_token() == null || response.refresh_token() == null || response.user() == null
                || response.user().id() == null) {
            throw new AuthorizedException("Renren token response is missing required fields");
        }
        return VendorTokenSet.builder().tokenType(response.token_type()).expireIn(response.expires_in())
                .token(UrlEncoder.encodeAll(response.access_token()))
                .refresh(UrlEncoder.encodeAll(response.refresh_token())).openId(response.user().id()).build();
    }

    /**
     * Parses and validates a Renren profile envelope.
     *
     * @param document JSON response document
     * @return non-null profile
     * @throws AuthorizedException if the response lacks a profile or stable identifier
     */
    private static Profile readProfile(final String document) {
        final ProfileEnvelope envelope = JsonKit.toPojo(document, ProfileEnvelope.class);
        if (envelope == null || envelope.response() == null || envelope.response().id() == null) {
            throw new AuthorizedException("Renren profile response is missing required fields");
        }
        return envelope.response();
    }

    /**
     * Returns the first avatar URL.
     *
     * @param avatars optional avatar entries
     * @return first URL, or {@code null}
     */
    private static String firstAvatar(final List<Avatar> avatars) {
        return avatars == null || avatars.isEmpty() ? null : avatars.get(0).url();
    }

    /**
     * Returns the first employer name.
     *
     * @param work optional work entries
     * @return first employer name, or {@code null}
     */
    private static String firstCompany(final List<Work> work) {
        return work == null || work.isEmpty() ? null : work.get(0).name();
    }

    /**
     * Builds the Renren authorization URL with comma-delimited scopes.
     *
     * @param context root operation context
     * @param state   optional authorization state
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
                        .queryParam("scope", scopes(Symbol.COMMA, false, getScopes(RenrenScope.values()))).build());
    }

    /**
     * Exchanges an authorization code through Renren's query-bearing empty form POST.
     *
     * @param context  root operation context used for secret resolution
     * @param callback immutable inbound callback
     * @return successful message containing the mapped token set
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        return Message.success(readToken(post(tokenUrl(current, inbound.value("code").orElse(null)))));
    }

    /**
     * Retrieves and maps the Renren profile identified by the token OpenID.
     *
     * @param context root operation context
     * @param token   non-null token set
     * @return successful message containing the mapped identity
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.USERINFO))
                .queryParam("access_token", authorization.getToken()).queryParam("userId", authorization.getOpenId())
                .build();
        final Profile profile = readProfile(get(url));
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(profile)).uuid(profile.id())
                        .avatar(firstAvatar(profile.avatar())).nickname(profile.name())
                        .company(firstCompany(profile.work()))
                        .gender(
                                profile.basicInformation() == null ? Gender.UNKNOWN
                                        : Gender.of(profile.basicInformation().sex()))
                        .token(authorization).source(descriptor().id()).build());
    }

    /**
     * Refreshes a token through Renren's query-bearing empty form POST.
     *
     * @param context root operation context used for secret resolution
     * @param token   non-null token set containing a refresh token
     * @return successful message containing the refreshed token set
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        return Message.success(readToken(post(refreshUrl(current, authorization.getRefresh()))));
    }

    /**
     * Renren token response.
     *
     * @param token_type    token type
     * @param expires_in    access-token lifetime in seconds
     * @param access_token  access token
     * @param refresh_token refresh token
     * @param user          token owner
     * @param error         optional error payload
     * @author Kimi Liu
     */
    private record TokenResponse(String token_type, int expires_in, String access_token, String refresh_token,
            TokenUser user, Object error) {
    }

    /**
     * Token owner.
     *
     * @param id stable Renren identifier
     * @author Kimi Liu
     */
    private record TokenUser(String id) {
    }

    /**
     * Renren profile envelope.
     *
     * @param response profile payload
     * @author Kimi Liu
     */
    private record ProfileEnvelope(Profile response) {
    }

    /**
     * Renren profile payload.
     *
     * @param id               stable identifier
     * @param name             display name
     * @param avatar           avatar entries
     * @param work             work entries
     * @param basicInformation basic demographic fields
     * @author Kimi Liu
     */
    private record Profile(String id, String name, List<Avatar> avatar, List<Work> work,
            BasicInformation basicInformation) {
    }

    /**
     * Avatar entry.
     *
     * @param url avatar URL
     * @author Kimi Liu
     */
    private record Avatar(String url) {
    }

    /**
     * Work entry.
     *
     * @param name employer name
     * @author Kimi Liu
     */
    private record Work(String name) {
    }

    /**
     * Basic profile information.
     *
     * @param sex vendor gender text
     * @author Kimi Liu
     */
    private record BasicInformation(String sex) {
    }

}
