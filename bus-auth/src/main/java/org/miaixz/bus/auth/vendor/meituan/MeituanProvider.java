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
package org.miaixz.bus.auth.vendor.meituan;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Meituan authorization, token, refresh, and profile operations.
 *
 * <p>
 * Remote requests use the injected Fabric context, state uses the injected atomic store, and each credentialed
 * operation resolves and clears its client secret without retaining it.
 * </p>
 *
 * @author Kimi Liu
 */
public class MeituanProvider extends AbstractProvider {

    /**
     * Creates a Meituan client from explicit dependencies.
     *
     * @param configuration complete non-null vendor dependency aggregate
     */
    public MeituanProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.MEITUAN);
    }

    /**
     * Parses and maps a Meituan token response.
     *
     * @param json response document
     * @return mapped token set
     */
    private static VendorTokenSet readToken(final String json) {
        final Response response = read(json);
        if (response.access_token() == null)
            throw new AuthorizedException("Missing access_token in Meituan response");
        return VendorTokenSet.builder().token(response.access_token()).refresh(response.refresh_token())
                .expireIn(response.expires_in()).build();
    }

    /**
     * Parses and validates the common Meituan response shape.
     *
     * @param json response document
     * @return validated response
     */
    private static Response read(final String json) {
        final Response response = JsonKit.toPojo(json, Response.class);
        if (response == null)
            throw new AuthorizedException("Failed to parse Meituan response: empty response");
        if (response.error_code() != null) {
            throw new AuthorizedException(response.erroe_msg() == null ? "Unknown error" : response.erroe_msg());
        }
        return response;
    }

    /**
     * Builds the authorization URL with the frozen empty scope.
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
                        .queryParam("state", state(current, state)).queryParam("scope", Normal.EMPTY).build());
    }

    /**
     * Exchanges a Meituan authorization code using a form POST.
     *
     * @param context  root operation context used for secret resolution
     * @param callback inbound authorization callback
     * @return successful message containing token fields
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        return Message.success(
                readToken(
                        post(
                                endpoint(VendorEndpoint.TOKEN),
                                tokenForm(current, "authorization_code", "code", inbound.value("code").orElse(null)))));
    }

    /**
     * Retrieves the Meituan user profile with application credentials.
     *
     * @param context root operation context used for secret resolution
     * @param token   non-null token set
     * @return successful message containing the identity
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("app_id", registration.clientId());
        form.put("secret", secret(current));
        form.put("access_token", authorization.getToken());
        final Response response = read(post(endpoint(VendorEndpoint.USERINFO), form));
        if (response.openid() == null)
            throw new AuthorizedException("Missing openid in Meituan profile response");
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.openid())
                        .username(response.nickname()).nickname(response.nickname()).avatar(response.avatar())
                        .gender(Gender.UNKNOWN).token(authorization).source(descriptor().id()).build());
    }

    /**
     * Refreshes a Meituan access token using a form POST.
     *
     * @param context root operation context used for secret resolution
     * @param token   non-null token set containing a refresh token
     * @return successful message containing refreshed token fields
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        return Message.success(
                readToken(
                        post(
                                endpoint(VendorEndpoint.REFRESH),
                                tokenForm(current, "refresh_token", "refresh_token", authorization.getRefresh()))));
    }

    /**
     * Builds one ordered Meituan credential form.
     *
     * @param context root context used for secret resolution
     * @param grant   grant type
     * @param name    credential field name
     * @param value   credential field value
     * @return ordered form fields
     */
    private Map<String, String> tokenForm(
            final Context context,
            final String grant,
            final String name,
            final String value) {
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("app_id", registration.clientId());
        form.put("secret", secret(context));
        form.put(name, value);
        form.put("grant_type", grant);
        return form;
    }

    /**
     * Typed union of Meituan token, profile, and error fields.
     *
     * @param access_token  access token
     * @param refresh_token refresh token
     * @param expires_in    access-token lifetime in seconds
     * @param openid        stable user identifier
     * @param nickname      display name
     * @param avatar        profile image URL
     * @param error_code    vendor error code
     * @param erroe_msg     frozen misspelled vendor error message
     * @author Kimi Liu
     */
    private record Response(String access_token, String refresh_token, int expires_in, String openid, String nickname,
            String avatar, Object error_code, String erroe_msg) {
    }

}
