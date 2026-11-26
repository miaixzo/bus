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

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.guard.IdentityGuard;
import org.miaixz.bus.vortex.guard.SignatureVerifier;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.provider.AuthorizeProvider;
import org.miaixz.bus.vortex.registry.AssetsRegistry;
import org.miaixz.bus.vortex.strategy.QualifierStrategy;

import reactor.core.publisher.Mono;

/**
 * Adds REST request signature validation and its route lookup error to shared qualification.
 *
 * @author Kimi Liu
 */
@org.springframework.core.annotation.Order(Order.THIRD)
public class RestQualifierStrategy extends QualifierStrategy {

    /**
     * Creates the REST qualifier for clients without a configured service identity.
     *
     * @param provider credential validation provider
     * @param registry runtime asset registry
     */
    public RestQualifierStrategy(AuthorizeProvider provider, AssetsRegistry registry) {
        super(provider, registry);
    }

    /**
     * Creates the REST qualifier with the gateway's service identity adapter.
     *
     * @param provider   credential validation provider
     * @param registry   runtime asset registry
     * @param identities shared service identity adapter
     */
    public RestQualifierStrategy(AuthorizeProvider provider, AssetsRegistry registry, IdentityGuard identities) {
        super(provider, registry, identities);
    }

    /**
     * Validates the REST signature configured by the resolved asset.
     *
     * @param context current gateway request context
     * @param assets  selected REST asset
     * @return REST signature validation completion
     */
    @Override
    protected Mono<Void> signature(Context context, Assets assets) {
        return SignatureVerifier.verifyRest(context, assets);
    }

    /**
     * Provides the REST route lookup error retained for legacy callers.
     *
     * @return REST asset lookup error
     */
    @Override
    protected Errors missingRoute() {
        return ErrorCode._100103;
    }

}
