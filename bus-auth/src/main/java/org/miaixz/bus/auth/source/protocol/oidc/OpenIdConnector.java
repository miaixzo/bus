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
package org.miaixz.bus.auth.source.protocol.oidc;

import java.util.List;

import org.miaixz.bus.auth.source.protocol.ProtocolConnector;
import org.miaixz.bus.auth.source.protocol.ProtocolRegistry;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.net.Protocol;

/**
 * Connects the standard OpenID Connect relying-party and provider Source drivers.
 *
 * @author Kimi Liu
 */
public class OpenIdConnector implements ProtocolConnector {

    /**
     * Creates a stateless OpenID Connect SPI connector.
     */
    public OpenIdConnector() {
        // No initialization required.
    }

    /**
     * Returns the OpenID Connect protocol key that owns both registered drivers.
     *
     * @return OpenID Connect protocol key
     */
    @Override
    public Protocol key() {
        return Protocol.OIDC;
    }

    /**
     * Binds the OpenID Connect client and server drivers as one removable registration.
     *
     * @param registry active Source registry
     */
    @Override
    public void connect(final ProtocolRegistry registry) {
        Assert.notNull(registry, "Source registry must not be null")
                .bindAll(List.of(new OpenIdClientDriver(), new OpenIdServerDriver()));
    }

}
