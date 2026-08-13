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
package org.miaixz.bus.auth.vendor.teambition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Teambition token, profile, and refresh operations.
 *
 * <p>
 * The provider has no authorization-URL override. Credential material is resolved only for token exchange, copied into
 * the immediate form request, and cleared by the shared vendor runtime after conversion.
 * </p>
 *
 * @author Kimi Liu
 */
public class TeambitionProvider extends AbstractProvider {

    /**
     * Creates a Teambition client from explicit runtime dependencies.
     *
     * @param configuration complete non-null vendor dependency aggregate
     */
    public TeambitionProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.TEAMBITION);
    }

    /**
     * Rejects a Teambition error envelope without exposing credentialed response content.
     *
     * @param name    optional vendor error name
     * @param message optional vendor error message
     * @throws AuthorizedException when both error fields are present
     */
    private static void check(final String name, final String message) {
        if (name != null && message != null) {
            throw new AuthorizedException(name + ", " + message);
        }
    }

    /**
     * Exchanges an authorization code using Teambition's form parameters.
     *
     * @param context  root operation context used for secret resolution
     * @param callback immutable inbound callback
     * @return successful message containing the mapped token set
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("client_id", registration.clientId());
        form.put("client_secret", secret(current));
        form.put("code", inbound.value("code").orElse(null));
        form.put("grant_type", "code");
        final TokenResponse response = JsonKit.toPojo(post(endpoint(VendorEndpoint.TOKEN), form), TokenResponse.class);
        check(response == null ? null : response.name(), response == null ? null : response.message());
        if (response == null || response.access_token() == null) {
            throw new AuthorizedException("Teambition token response is missing access_token");
        }
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).refresh(response.refresh_token()).build());
    }

    /**
     * Retrieves the Teambition profile using the vendor-specific OAuth2 authorization header.
     *
     * @param context root operation context
     * @param token   non-null token set
     * @return successful message containing the mapped identity
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Profile response = JsonKit.toPojo(
                get(
                        endpoint(VendorEndpoint.USERINFO),
                        null,
                        Map.of(Http.Header.AUTHORIZATION, "OAuth2 " + authorization.getToken())),
                Profile.class);
        check(response == null ? null : response.name(), response == null ? null : response.message());
        if (response == null || response._id() == null) {
            throw new AuthorizedException("Teambition profile response is missing _id");
        }
        authorization.setUid(response._id());
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response._id())
                        .username(response.name()).nickname(response.name()).avatar(response.avatarUrl())
                        .blog(response.website()).location(response.location()).email(response.email())
                        .gender(Gender.UNKNOWN).token(authorization).source(descriptor().id()).build());
    }

    /**
     * Refreshes a Teambition token using its user identifier and refresh token.
     *
     * @param context root operation context
     * @param token   non-null token set containing user and refresh identifiers
     * @return successful message containing the refreshed token set
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("_userId", authorization.getUid());
        form.put("refresh_token", authorization.getRefresh());
        final TokenResponse response = JsonKit
                .toPojo(post(endpoint(VendorEndpoint.REFRESH), form), TokenResponse.class);
        check(response == null ? null : response.name(), response == null ? null : response.message());
        if (response == null || response.access_token() == null) {
            throw new AuthorizedException("Teambition refresh response is missing access_token");
        }
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).refresh(response.refresh_token()).build());
    }

    /**
     * Teambition token response.
     *
     * @param access_token  access token
     * @param refresh_token refresh token
     * @param name          optional vendor error name
     * @param message       optional vendor error message
     * @author Kimi Liu
     */
    private record TokenResponse(String access_token, String refresh_token, String name, String message) {
    }

    /**
     * Teambition profile response.
     *
     * @param _id       stable Teambition user identifier
     * @param name      display name or vendor error name
     * @param avatarUrl avatar URL
     * @param website   personal website URL
     * @param location  location text
     * @param email     email address
     * @param message   optional vendor error message
     * @author Kimi Liu
     */
    private record Profile(String _id, String name, String avatarUrl, String website, String location, String email,
            String message) {
    }

}
