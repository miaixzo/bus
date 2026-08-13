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
package org.miaixz.bus.auth.vendor.afdian;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party authentication client for AfDian OAuth 2.0.
 *
 * <p>
 * The provider exchanges an authorization code through the injected Fabric HTTP context and maps the user identifier
 * returned by that exchange locally. AfDian has no separate user-information request, and this type never retains
 * resolved client secret material.
 * </p>
 *
 * @author Kimi Liu
 */
public class AfDianProvider extends AbstractProvider {

    /**
     * Creates an AfDian client from the complete immutable dependency aggregate.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException                                   if the configuration or one of its required
     *                                                                dependencies is null
     * @throws org.miaixz.bus.core.lang.exception.AuthorizedException if the AfDian registration is invalid
     */
    public AfDianProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.AFDIAN);
    }

    /**
     * Exchanges the inbound authorization code for the AfDian token response and retains its {@code data.user_id} value
     * in the returned token set.
     *
     * <p>
     * The request is a form POST containing, in order, {@code grant_type}, {@code client_id}, {@code client_secret},
     * {@code code}, and {@code redirect_uri}. The client secret is resolved for this operation from the configured
     * {@code SecretResolver}; it is not stored by this provider.
     * </p>
     *
     * @param context  immutable root operation context used for secret resolution
     * @param callback immutable inbound callback containing the authorization code
     * @return successful client message containing the AfDian user identifier
     * @throws NullPointerException                                   if {@code context} or {@code callback} is null
     * @throws org.miaixz.bus.core.lang.exception.AuthorizedException if the token endpoint or client secret is absent
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Map<String, String> params = new LinkedHashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", registration.clientId());
        params.put("client_secret", secret(current));
        params.put("code", inbound.value("code").orElse(null));
        params.put("redirect_uri", registration.redirectUri());

        final String response = post(endpoint(VendorEndpoint.TOKEN), params);
        final TokenResponse payload = JsonKit.toPojo(response, TokenResponse.class);
        final String userId = payload == null || payload.data() == null ? null : payload.data().user_id();

        return Message.success(VendorTokenSet.builder().userId(userId).build());
    }

    /**
     * Maps the AfDian user identifier already carried by a token set into a vendor identity without sending another
     * network request.
     *
     * @param context immutable root operation context; accepted for the common vendor contract
     * @param token   token set returned by {@link #token(Context, Callback.Inbound)}
     * @return successful client message containing an identity with unknown gender and the original token reference
     * @throws NullPointerException if {@code context} or {@code token} is null
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet current = Objects.requireNonNull(token, "Token set must not be null");
        return Message.success(
                VendorIdentity.builder().uuid(current.getUserId()).gender(Gender.UNKNOWN).token(current)
                        .source(descriptor().id()).build());
    }

    /**
     * Builds the AfDian authorization URL locally and atomically registers its state through the injected state store.
     *
     * <p>
     * The query parameters are emitted in the frozen order {@code response_type}, {@code scope}, {@code client_id},
     * {@code redirect_uri}, and {@code state}; the AfDian scope is always {@code basic}.
     * </p>
     *
     * @param context immutable root operation context used to register state
     * @param state   optional caller-supplied state; a generated state is used when absent
     * @return successful client message containing the complete authorization URL
     * @throws NullPointerException                                   if {@code context} is null
     * @throws org.miaixz.bus.core.lang.exception.AuthorizedException if the endpoint is absent or state registration
     *                                                                fails
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE)).queryParam("response_type", "code")
                        .queryParam("scope", "basic").queryParam("client_id", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("state", state(current, state)).build());
    }

    /**
     * Typed projection of the AfDian token response envelope.
     *
     * @param data nested response payload, or null when the vendor omits it
     * @author Kimi Liu
     */
    private record TokenResponse(TokenData data) {
    }

    /**
     * Typed projection of the AfDian token response data object.
     *
     * @param user_id vendor user identifier encoded with the wire field name
     * @author Kimi Liu
     */
    private record TokenData(String user_id) {
    }

}
