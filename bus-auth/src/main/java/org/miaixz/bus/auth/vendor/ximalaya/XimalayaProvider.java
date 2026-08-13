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
package org.miaixz.bus.auth.vendor.ximalaya;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Ximalaya authorization and signed profile retrieval.
 *
 * @author Kimi Liu
 */
public class XimalayaProvider extends AbstractProvider {

    /**
     * Creates a Ximalaya client from explicit runtime dependencies.
     *
     * @param configuration complete non-null vendor dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     */
    public XimalayaProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.XIMALAYA);
    }

    /**
     * Generates Ximalaya's HMAC-SHA1 then MD5 signature using shared Bus cryptography components.
     *
     * @param parameters   sorted request parameters excluding the signature
     * @param clientSecret operation-scoped client secret
     * @return lowercase hexadecimal signature
     */
    private static String sign(final Map<String, String> parameters, final String clientSecret) {
        final String canonical = VendorRequestBuilder.parseMapToString(new TreeMap<>(parameters), false);
        final String encoded = Base64.encode(canonical);
        final byte[] hmac = VendorRequestBuilder
                .sign(clientSecret.getBytes(Charset.UTF_8), encoded.getBytes(Charset.UTF_8), Algorithm.HMACSHA1);
        return org.miaixz.bus.crypto.Builder.md5().digestHex(hmac);
    }

    /**
     * Validates the common typed Ximalaya response status.
     *
     * @param response typed response
     * @throws AuthorizedException if the response is absent or reports an error
     */
    private static void validate(final Response response) {
        if (response == null) {
            throw new AuthorizedException("Failed to parse Ximalaya response: empty response");
        }
        if (response.errcode() != null) {
            throw new AuthorizedException(response.error_no() == null ? response.errcode() : response.error_no(),
                    response.error_desc() == null ? "Unknown error" : response.error_desc());
        }
    }

    /**
     * Builds the Ximalaya authorization URL and atomically registers state.
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
                        .queryParam("state", state(current, state)).queryParam("client_os_type", Symbol.THREE)
                        .queryParam("device_id", registration.deviceId()).build());
    }

    /**
     * Exchanges an authorization code through Ximalaya's form POST operation.
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
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("code", inbound.value("code").orElse(null));
        form.put("client_id", registration.clientId());
        form.put("client_secret", secret(current));
        form.put("device_id", registration.deviceId());
        form.put("grant_type", "authorization_code");
        form.put("redirect_uri", registration.redirectUri());
        final TokenResponse response = JsonKit.toPojo(post(endpoint(VendorEndpoint.TOKEN), form), TokenResponse.class);
        validate(response);
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in Ximalaya response");
        }
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).refresh(response.refresh_token())
                        .expireIn(response.expires_in()).uid(response.uid()).build());
    }

    /**
     * Retrieves a signed Ximalaya user profile.
     *
     * @param context root operation context used to resolve and clear the signing secret
     * @param token   non-null token set
     * @return successful message containing the mapped identity
     * @throws AuthorizedException if signing or response validation fails
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> query = new TreeMap<>();
        query.put("app_key", registration.clientId());
        query.put("client_os_type", registration.type() == null ? String.valueOf(Normal._3) : registration.type());
        query.put("device_id", registration.deviceId());
        query.put("pack_id", registration.unionId());
        query.put("access_token", authorization.getToken());
        query.put("sig", sign(query, secret(current)));
        final ProfileResponse profile = JsonKit
                .toPojo(get(endpoint(VendorEndpoint.USERINFO), query), ProfileResponse.class);
        validate(profile);
        if (profile.id() == null) {
            throw new AuthorizedException("Missing id in Ximalaya profile response");
        }
        return Message.success(
                VendorIdentity.builder().uuid(profile.id()).nickname(profile.nickname()).avatar(profile.avatar_url())
                        .rawJson(JsonKit.toJsonString(profile)).source(descriptor().id()).token(authorization)
                        .gender(Gender.UNKNOWN).build());
    }

    /**
     * Common typed Ximalaya error envelope.
     *
     * @author Kimi Liu
     */
    private interface Response {

        /**
         * Returns the error-presence marker.
         *
         * @return error code field or null on success
         */
        String errcode();

        /**
         * Returns the vendor error number.
         *
         * @return vendor error number
         */
        String error_no();

        /**
         * Returns the vendor error description.
         *
         * @return vendor error description
         */
        String error_desc();
    }

    /**
     * Typed Ximalaya token response.
     *
     * @param errcode       error-presence marker
     * @param error_no      vendor error number
     * @param error_desc    vendor error description
     * @param access_token  access token
     * @param refresh_token refresh token
     * @param expires_in    token lifetime in seconds
     * @param uid           user identifier
     * @author Kimi Liu
     */
    private record TokenResponse(String errcode, String error_no, String error_desc, String access_token,
            String refresh_token, int expires_in, String uid) implements Response {
    }

    /**
     * Typed Ximalaya profile response.
     *
     * @param errcode    error-presence marker
     * @param error_no   vendor error number
     * @param error_desc vendor error description
     * @param id         user identifier
     * @param nickname   display name
     * @param avatar_url avatar URL
     * @author Kimi Liu
     */
    private record ProfileResponse(String errcode, String error_no, String error_desc, String id, String nickname,
            String avatar_url) implements Response {
    }

}
