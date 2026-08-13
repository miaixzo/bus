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
package org.miaixz.bus.auth.vendor.douyin;

import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for the Douyin mini-program code-to-session flow.
 *
 * <p>
 * The remote token operation uses the injected Fabric context and resolves its app secret per root operation. User
 * information is a frozen local projection of the mini-program token fields and never performs an HTTP request.
 * </p>
 *
 * @author Kimi Liu
 */
public class DouyinMiniProvider extends AbstractProvider {

    /**
     * Creates a Douyin mini-program client from explicit runtime dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     * @throws AuthorizedException  if the Douyin mini-program registration is invalid
     */
    public DouyinMiniProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.DOUYIN_MINI);
    }

    /**
     * Validates the frozen Douyin response envelope and nested error status.
     *
     * @param response typed code-to-session response
     * @throws AuthorizedException if the response or nested data is absent or reports an error
     */
    private static void validate(final SessionResponse response) {
        if (response == null) {
            throw new AuthorizedException("Failed to parse Douyin mini-program response: empty response");
        }
        if (response.data() == null) {
            throw new AuthorizedException("Missing data field in Douyin mini-program response");
        }
        final String errorCode = response.data().error_code();
        if ("error".equals(response.message()) || !"0".equals(errorCode)) {
            final String description = response.data().description();
            throw new AuthorizedException(errorCode, description == null ? "Unknown error" : description);
        }
    }

    /**
     * Exchanges the callback code and optional anonymous code through the code-to-session endpoint.
     *
     * @param context  immutable root operation context used to resolve the app secret
     * @param callback immutable inbound callback containing {@code code} and optional {@code anonymous_code}
     * @return successful client message containing open, union, and session-key fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, response envelope, or vendor status is invalid
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.TOKEN))
                .queryParam("appid", registration.clientId()).queryParam("secret", secret(current))
                .queryParam("code", inbound.value("code").orElse(null))
                .queryParam("anonymous_code", inbound.value("anonymous_code").orElse(null)).build();
        final SessionResponse response = JsonKit.toPojo(get(url), SessionResponse.class);
        validate(response);
        return Message.success(
                VendorTokenSet.builder().openId(response.openid()).unionId(response.unionid())
                        .token(response.session_key()).build());
    }

    /**
     * Maps mini-program token fields to an identity locally without sending an HTTP request.
     *
     * @param context immutable root operation context accepted by the common vendor contract
     * @param token   non-null mini-program token set
     * @return successful identity message with empty profile text and the original token reference
     * @throws NullPointerException if an argument is null
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        return Message.success(
                VendorIdentity.builder().username(Normal.EMPTY).nickname(Normal.EMPTY).avatar(Normal.EMPTY)
                        .uuid(authorization.getOpenId()).token(authorization).source(descriptor().id()).build());
    }

    /**
     * Typed Douyin mini-program code-to-session response.
     *
     * @param message     vendor status text
     * @param data        nested error status
     * @param openid      application-scoped user identifier
     * @param unionid     cross-application user identifier
     * @param session_key mini-program session key carried in the common token field
     * @author Kimi Liu
     */
    private record SessionResponse(String message, ErrorData data, String openid, String unionid, String session_key) {
    }

    /**
     * Typed nested Douyin error status.
     *
     * @param error_code  vendor status code normalized as text
     * @param description vendor diagnostic text
     * @author Kimi Liu
     */
    private record ErrorData(String error_code, String description) {
    }

}
