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
package org.miaixz.bus.auth.vendor.wechat.ee;

import java.util.Objects;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.VendorConfiguration;
import org.miaixz.bus.auth.vendor.VendorEndpoint;
import org.miaixz.bus.auth.vendor.VendorRequestBuilder;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.url.UrlEncoder;

/**
 * WeChat Enterprise web authorization client.
 *
 * <p>
 * The client builds the vendor-specific authorization request locally and delegates token and identity operations to
 * the enterprise family implementation. Authorization state is registered through the injected atomic state store
 * before the URL is returned.
 * </p>
 *
 * @author Kimi Liu
 */
public class WeChatEeWebProvider extends AbstractWeChatEeProvider {

    /**
     * Creates a web authorization client from explicit vendor dependencies.
     *
     * @param configuration complete non-null vendor dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     */
    public WeChatEeWebProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.WECHAT_EE_WEB);
    }

    /**
     * Builds the WeChat Enterprise web authorization URL and atomically registers its state.
     *
     * @param context immutable root operation context used to isolate the stored state
     * @param state   optional caller-supplied authorization state
     * @return successful message containing the complete authorization URL
     * @throws NullPointerException if the context is null
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE))
                .queryParam("appid", registration.clientId()).queryParam("agentid", registration.unionId())
                .queryParam("redirect_uri", UrlEncoder.encodeAll(registration.redirectUri()))
                .queryParam("response_type", "code")
                .queryParam("scope", scopes(Symbol.COMMA, false, getScopes(WeChatEeWebScope.values())))
                .queryParam("state", state(current, state)).build();
        return Message.success(url.concat("#wechat_redirect"));
    }

}
