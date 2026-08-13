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
package org.miaixz.bus.auth.vendor.wechat.mini;

import java.util.LinkedHashMap;
import java.util.Map;
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
 * Third-party client for WeChat Mini Program code exchange and local identity projection.
 *
 * @author Kimi Liu
 */
public class WeChatMiniProvider extends AbstractProvider {

    /**
     * Creates a WeChat Mini Program client from explicit runtime dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     */
    public WeChatMiniProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.WECHAT_MINI);
    }

    /**
     * Validates a typed code-to-session response.
     *
     * @param response typed vendor response
     * @throws AuthorizedException if the response is absent or reports a nonzero error code
     */
    private static void validate(final SessionResponse response) {
        if (response == null) {
            throw new AuthorizedException("Failed to parse WeChat Mini Program response: empty response");
        }
        if (response.errcode() != null && !"0".equals(response.errcode())) {
            throw new AuthorizedException(response.errcode(), response.errmsg());
        }
        if (response.openid() == null || response.session_key() == null) {
            throw new AuthorizedException("Missing WeChat Mini Program session fields");
        }
    }

    /**
     * Exchanges a mini-program code through WeChat's query-authenticated code-to-session GET operation.
     *
     * @param context  root context used to resolve and clear the application secret
     * @param callback immutable inbound callback containing the mini-program code
     * @return successful message containing the session key, OpenID, and UnionID
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if WeChat reports an error or the response cannot be parsed
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Map<String, String> query = new LinkedHashMap<>();
        query.put("appid", registration.clientId());
        query.put("secret", secret(current));
        query.put("js_code", inbound.value("code").orElse(null));
        query.put("grant_type", "authorization_code");
        final SessionResponse response = JsonKit
                .toPojo(get(endpoint(VendorEndpoint.TOKEN), query), SessionResponse.class);
        validate(response);
        return Message.success(
                VendorTokenSet.builder().openId(response.openid()).unionId(response.unionid())
                        .token(response.session_key()).build());
    }

    /**
     * Projects locally available mini-program token fields into an identity without a network request.
     *
     * @param context immutable root operation context
     * @param token   non-null mini-program token set
     * @return successful message containing the local identity projection
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
     * Typed WeChat Mini Program code-to-session response.
     *
     * @param errcode     vendor error code, absent or zero for success
     * @param errmsg      vendor error diagnostic
     * @param session_key session key carried in the common token field
     * @param openid      application-scoped user identifier
     * @param unionid     cross-application user identifier
     * @author Kimi Liu
     */
    private record SessionResponse(String errcode, String errmsg, String session_key, String openid, String unionid) {
    }

}
