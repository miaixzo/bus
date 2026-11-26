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
package org.miaixz.bus.vortex.strategy.qualifier;

import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.guard.IdentityGuard;
import org.miaixz.bus.vortex.provider.AuthorizeProvider;
import org.miaixz.bus.vortex.registry.AssetsRegistry;
import org.miaixz.bus.vortex.strategy.QualifierStrategy;

import reactor.core.publisher.Mono;

/**
 * Qualifies the SDC route from the in-memory route assets after verifying the service identity.
 *
 * @author Kimi Liu
 */
@org.springframework.core.annotation.Order(Order.THIRD)
public class SdcQualifierStrategy extends QualifierStrategy {

    /**
     * Creates the SDC qualification stage.
     *
     * @param provider shared authorization provider
     * @param registry shared runtime asset registry
     */
    public SdcQualifierStrategy(AuthorizeProvider provider, AssetsRegistry registry) {
        super(provider, registry);
    }

    /**
     * Creates the SDC qualifier with the gateway identity adapter.
     *
     * @param provider   credential validation provider
     * @param registry   runtime asset registry
     * @param identities shared service identity adapter
     */
    public SdcQualifierStrategy(AuthorizeProvider provider, AssetsRegistry registry, IdentityGuard identities) {
        super(provider, registry, identities);
    }

    /**
     * Identifies this stage as part of the SDC strategy chain.
     *
     * @return the SDC route identifier
     */
    @Override
    public Integer protocol() {
        return Args.ROUTE_SDC;
    }

    /**
     * Resolves the SDC route version and supplies the shared default when absent.
     *
     * @param exchange current web exchange
     * @param context  current request context
     * @return resolved route version
     */
    @Override
    protected String version(ServerWebExchange exchange, Context context) {
        String version = value(context, Args.VERSION);
        return version == null || version.isBlank() ? Args.DEFAULT_VERSION : version;
    }

    /**
     * Completes authorization for a request whose service identity was verified during route qualification.
     *
     * @param context qualified SDC context
     * @return completion of the credential stage
     */
    @Override
    protected Mono<Void> authorize(Context context) {
        return Mono.empty();
    }

}
