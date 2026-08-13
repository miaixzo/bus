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
package org.miaixz.bus.auth.vendor.jd;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for JD authorization, token, refresh, and Zeus account-profile operations.
 *
 * <p>
 * Every remote call uses the injected Fabric context. Authorization state uses the injected atomic store, Zeus
 * timestamps use the injected Fabric clock in UTC, and application secrets are resolved per operation and never
 * retained in fields.
 * </p>
 *
 * @author Kimi Liu
 */
public class JdProvider extends AbstractProvider {

    /**
     * JD Zeus timestamp wire formatter.
     */
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern(Fields.NORM_DATETIME);

    /**
     * JD Zeus user-profile method name.
     */
    private static final String USER_METHOD = "jingdong.user.getUserInfoByOpenId";

    /**
     * Creates a JD client from the complete immutable dependency aggregate.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     * @throws AuthorizedException  if the JD registration is invalid
     */
    public JdProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.JD);
    }

    /**
     * Produces the JD Zeus uppercase MD5 signature from sorted non-empty parameters.
     *
     * @param appSecret  sensitive operation-scoped JD application secret
     * @param parameters immutable request parameters to sign
     * @return uppercase hexadecimal MD5 signature
     */
    private static String sign(final String appSecret, final Map<String, Object> parameters) {
        final StringBuilder value = new StringBuilder(appSecret);
        new TreeMap<>(parameters).forEach((name, field) -> {
            final String text = String.valueOf(field);
            if (StringKit.isNotEmpty(name) && StringKit.isNotEmpty(text)) {
                value.append(name).append(text);
            }
        });
        value.append(appSecret);
        return org.miaixz.bus.crypto.Builder.md5Hex(value.toString()).toUpperCase(Locale.ROOT);
    }

    /**
     * Parses and validates one JD token response.
     *
     * @param json      token response document
     * @param operation diagnostic operation label
     * @return mapped JD token set
     * @throws AuthorizedException if the response is empty, reports an error, or omits the access token
     */
    private static VendorTokenSet readToken(final String json, final String operation) {
        final TokenResponse response = JsonKit.toPojo(json, TokenResponse.class);
        validate(response);
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in JD " + operation + " response");
        }
        return VendorTokenSet.builder().token(response.access_token()).expireIn(response.expires_in())
                .refresh(response.refresh_token()).scope(response.scope()).openId(response.open_id()).build();
    }

    /**
     * Extracts the frozen nested JD profile data object.
     *
     * @param response validated outer response
     * @return nested non-null user data
     * @throws AuthorizedException if any required envelope level is absent
     */
    private static UserData extract(final ProfileEnvelope response) {
        final ProfileResponse profile = response.jingdong_user_getUserInfoByOpenId_response();
        if (profile == null) {
            throw new AuthorizedException("Missing jingdong_user_getUserInfoByOpenId_response in JD response");
        }
        final ProfileResult result = profile.getuserinfobyappidandopenid_result();
        if (result == null) {
            throw new AuthorizedException("Missing getuserinfobyappidandopenid_result in JD response");
        }
        if (result.data() == null) {
            throw new AuthorizedException("Missing data in JD response");
        }
        return result.data();
    }

    /**
     * Rejects an empty JD response or the frozen {@code error_response} shape.
     *
     * @param response typed JD response
     * @throws AuthorizedException if the response is null or reports an error
     */
    private static void validate(final JdResponse response) {
        if (response == null) {
            throw new AuthorizedException("Failed to parse JD response: empty response");
        }
        if (response.error_response() != null) {
            final String description = response.error_response().zh_desc();
            throw new AuthorizedException(description == null ? "Unknown error" : description);
        }
    }

    /**
     * Builds the JD authorization URL and atomically registers its state.
     *
     * @param context immutable root operation context used for state ownership
     * @param state   optional caller-supplied state; a generated state is used when absent
     * @return successful client message containing the complete authorization URL
     * @throws NullPointerException if {@code context} is null
     * @throws AuthorizedException  if the endpoint or state registration is invalid
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE))
                        .queryParam("app_key", registration.clientId()).queryParam("response_type", "code")
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("scope", scopes(Symbol.SPACE, true, getScopes(JdScope.values())))
                        .queryParam("state", state(current, state)).build());
    }

    /**
     * Exchanges a JD authorization code through the vendor form endpoint.
     *
     * @param context  immutable root operation context used to resolve the application secret
     * @param callback immutable inbound callback containing the standard {@code code} parameter
     * @return successful client message containing the mapped JD token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("app_key", registration.clientId());
        form.put("app_secret", secret(current));
        form.put("grant_type", "authorization_code");
        form.put("code", inbound.value("code").orElse(null));
        return Message.success(readToken(post(endpoint(VendorEndpoint.TOKEN), form), "access token"));
    }

    /**
     * Retrieves and maps a signed JD Zeus account profile.
     *
     * @param context immutable root operation context used for time and secret ownership
     * @param token   non-null JD token set
     * @return successful client message containing the mapped JD identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, nested response, or profile is invalid
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final VendorRequestBuilder request = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.USERINFO))
                .queryParam("access_token", authorization.getToken()).queryParam("app_key", registration.clientId())
                .queryParam("method", USER_METHOD)
                .queryParam("360buy_param_json", "{\"openId\":\"" + authorization.getOpenId() + "\"}")
                .queryParam("timestamp", LocalDateTime.ofInstant(clock.now(), ZoneOffset.UTC).format(TIMESTAMP_FORMAT))
                .queryParam("v", "2.0");
        request.queryParam("sign", sign(secret(current), request.getReadOnlyParams()));
        final ProfileEnvelope response = JsonKit.toPojo(post(request.build(true)), ProfileEnvelope.class);
        validate(response);
        final UserData data = extract(response);
        if (data.nickName() == null) {
            throw new AuthorizedException("Missing nickName in JD profile response");
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(data)).uuid(authorization.getOpenId())
                        .username(data.nickName()).nickname(data.nickName()).avatar(data.imageUrl())
                        .gender(Gender.of(data.gendar())).token(authorization).source(descriptor().id()).build());
    }

    /**
     * Refreshes a JD access token through the configured refresh endpoint.
     *
     * @param context immutable root operation context used to resolve the application secret
     * @param token   non-null token set containing the refresh token
     * @return successful client message containing the refreshed JD token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("app_key", registration.clientId());
        form.put("app_secret", secret(current));
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", authorization.getRefresh());
        return Message.success(readToken(post(endpoint(VendorEndpoint.REFRESH), form), "refresh token"));
    }

    /**
     * Common JD error envelope contract.
     *
     * @author Kimi Liu
     */
    private interface JdResponse {

        /**
         * Returns the optional JD error envelope.
         *
         * @return error envelope, or null for success
         */
        ErrorResponse error_response();
    }

    /**
     * Typed JD error response.
     *
     * @param zh_desc localized vendor diagnostic text
     * @author Kimi Liu
     */
    private record ErrorResponse(String zh_desc) {
    }

    /**
     * Typed JD token response.
     *
     * @param access_token   sensitive access token
     * @param expires_in     access-token lifetime in seconds
     * @param refresh_token  sensitive refresh token
     * @param scope          granted scope text
     * @param open_id        stable JD open identifier
     * @param error_response optional vendor error envelope
     * @author Kimi Liu
     */
    private record TokenResponse(String access_token, int expires_in, String refresh_token, String scope,
            String open_id, ErrorResponse error_response) implements JdResponse {
    }

    /**
     * Typed outer JD profile response.
     *
     * @param jingdong_user_getUserInfoByOpenId_response profile method envelope
     * @param error_response                             optional vendor error envelope
     * @author Kimi Liu
     */
    private record ProfileEnvelope(ProfileResponse jingdong_user_getUserInfoByOpenId_response,
            ErrorResponse error_response) implements JdResponse {
    }

    /**
     * Typed JD profile method envelope.
     *
     * @param getuserinfobyappidandopenid_result profile result envelope
     * @author Kimi Liu
     */
    private record ProfileResponse(ProfileResult getuserinfobyappidandopenid_result) {
    }

    /**
     * Typed JD profile result envelope.
     *
     * @param data user profile data
     * @author Kimi Liu
     */
    private record ProfileResult(UserData data) {
    }

    /**
     * Typed JD user profile fields.
     *
     * @param nickName account nickname
     * @param imageUrl profile image URL
     * @param gendar   vendor gender text using JD's frozen misspelled field name
     * @author Kimi Liu
     */
    private record UserData(String nickName, String imageUrl, String gendar) {
    }

}
