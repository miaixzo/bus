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
import org.miaixz.bus.auth.vendor.VendorErrors;
import org.miaixz.bus.auth.vendor.VendorRequestBuilder;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * WeChat Enterprise QR-code authorization client.
 *
 * @author Kimi Liu
 */
public class WeChatEeQrcodeProvider extends AbstractWeChatEeProvider {

    /**
     * Creates and validates a QR-code client from explicit dependencies.
     *
     * @param configuration complete non-null vendor dependencies
     * @throws AuthorizedException when a CorpApp registration has no agent identifier
     */
    public WeChatEeQrcodeProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.WECHAT_EE);
        if ("CorpApp".equals(registration.loginType()) && StringKit.isEmpty(registration.unionId())) {
            throw new AuthorizedException(VendorErrors._110012);
        }
    }

    /**
     * Builds the enterprise QR-code authorization URL and atomically registers state.
     *
     * @param context root operation context
     * @param state   optional authorization state
     * @return successful message containing the authorization URL with WeChat fragment
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE))
                .queryParam("login_type", registration.loginType()).queryParam("appid", registration.clientId())
                .queryParam("agentid", registration.unionId()).queryParam("redirect_uri", registration.redirectUri())
                .queryParam("state", state(current, state)).queryParam("lang", registration.lang()).build();
        return Message.success(url.concat("#wechat_redirect"));
    }

}
