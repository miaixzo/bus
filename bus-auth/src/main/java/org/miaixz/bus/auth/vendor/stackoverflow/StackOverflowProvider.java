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
package org.miaixz.bus.auth.vendor.stackoverflow;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Stack Overflow authorization, token, and profile operations.
 *
 * @author Kimi Liu
 */
public class StackOverflowProvider extends AbstractProvider {

    /**
     * Creates a Stack Overflow client from explicit runtime dependencies.
     *
     * @param configuration complete non-null vendor dependency aggregate
     */
    public StackOverflowProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.STACK_OVERFLOW);
    }

    /**
     * Rejects a vendor error response.
     *
     * @param error       optional error identifier
     * @param description optional public diagnostic
     * @throws AuthorizedException when an error identifier is present
     */
    private static void check(final Object error, final String description) {
        if (error != null) {
            throw new AuthorizedException(description == null ? "Stack Overflow request failed" : description);
        }
    }

    /**
     * Builds the authorization URL with comma-delimited scopes.
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
                        .queryParam("scope", scopes(Symbol.COMMA, false, getScopes(StackoverflowScope.values())))
                        .build());
    }

    /**
     * Exchanges an authorization code using the historical query-bearing form POST.
     *
     * @param context  root operation context used for secret resolution
     * @param callback immutable inbound callback
     * @return successful message containing the mapped token set
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final String url = tokenUrl(current, inbound.value("code").orElse(null));
        final Map<String, String> form = new LinkedHashMap<>();
        UrlDecoder.decodeMap(url, Charset.DEFAULT_UTF_8).forEach(form::put);
        final Map<String, String> headers = Map.of(Http.Header.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        final TokenResponse response = JsonKit.toPojo(post(url, form, headers), TokenResponse.class);
        if (response == null) {
            throw new AuthorizedException("Stack Overflow token response is empty");
        }
        check(response.error(), response.error_description());
        if (response.access_token() == null) {
            throw new AuthorizedException("Stack Overflow token response is missing access_token");
        }
        return Message
                .success(VendorTokenSet.builder().token(response.access_token()).expireIn(response.expires()).build());
    }

    /**
     * Retrieves the first Stack Overflow profile using access token, site, and registration key query fields.
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
                .queryParam("access_token", authorization.getToken()).queryParam("site", "stackoverflow")
                .queryParam("key", registration.unionId()).build();
        final UserResponse response = JsonKit.toPojo(get(url), UserResponse.class);
        if (response == null) {
            throw new AuthorizedException("Stack Overflow profile response is empty");
        }
        check(response.error(), response.error_description());
        if (response.items() == null || response.items().isEmpty() || response.items().get(0).user_id() == null) {
            throw new AuthorizedException("Stack Overflow profile response is missing items");
        }
        final User user = response.items().get(0);
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(user)).uuid(user.user_id())
                        .avatar(user.profile_image()).location(user.location()).nickname(user.display_name())
                        .blog(user.website_url()).gender(Gender.UNKNOWN).token(authorization).source(descriptor().id())
                        .build());
    }

    /**
     * Stack Overflow token response.
     *
     * @param error             optional error identifier
     * @param error_description optional error description
     * @param access_token      access token
     * @param expires           access-token lifetime in seconds
     * @author Kimi Liu
     */
    private record TokenResponse(Object error, String error_description, String access_token, int expires) {
    }

    /**
     * Stack Overflow profile response.
     *
     * @param error             optional error identifier
     * @param error_description optional error description
     * @param items             profile items
     * @author Kimi Liu
     */
    private record UserResponse(Object error, String error_description, List<User> items) {
    }

    /**
     * Stack Overflow user item.
     *
     * @param user_id       stable user identifier
     * @param profile_image avatar URL
     * @param location      location text
     * @param display_name  display name
     * @param website_url   personal website URL
     * @author Kimi Liu
     */
    private record User(String user_id, String profile_image, String location, String display_name,
            String website_url) {
    }

}
