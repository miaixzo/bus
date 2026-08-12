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
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.guard.SignatureVerifier;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.provider.AuthorizeProvider;
import org.miaixz.bus.vortex.routing.slug.SlugRouteMatcher;
import org.miaixz.bus.vortex.strategy.QualifierStrategy;

import reactor.core.publisher.Mono;

/**
 * Qualifies public Slug forwarding requests and delegates optional signature verification to the shared verifier.
 *
 * @author Kimi Liu
 */
@org.springframework.core.annotation.Order(Order.THIRD)
public class SlugQualifierStrategy extends QualifierStrategy {

    /**
     * Matcher used to resolve public slug assets.
     */
    private final SlugRouteMatcher matcher;

    /**
     * Creates a slug qualifier strategy.
     *
     * @param matcher  slug route matcher
     * @param provider credential validation provider
     */
    public SlugQualifierStrategy(SlugRouteMatcher matcher, AuthorizeProvider provider) {
        super(provider, null);
        this.matcher = matcher;
    }

    /**
     * Returns this strategy's dynamic protocol.
     *
     * @return slug protocol number
     */
    @Override
    public Integer protocol() {
        return Args.PROTOCOL_SLUG;
    }

    /**
     * Resolves and qualifies a public slug asset.
     *
     * @param exchange current exchange
     * @param chain    remaining strategy chain
     * @return completion signal
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            SlugRouteMatcher.Match match = this.matcher.match(exchange);
            if (match == null) {
                return Mono.error(new ValidateException(ErrorCode._100800));
            }
            Assets assets = match.assets();
            context.setAssets(assets);
            Logger.info(
                    true,
                    "Vortex",
                    "Public slug asset resolved: clientIp={}, method={}, version={}, verb={}, policy={}, signing={}, host={}, port={}, path={}, url={}",
                    context.getX_request_ip(),
                    assets.getMethod(),
                    assets.getVersion(),
                    assets.getVerb(),
                    assets.getPolicy(),
                    assets.getSigning(),
                    assets.getHost(),
                    assets.getPort(),
                    assets.getPath(),
                    assets.getUrl());
            return method(context, assets).then(SignatureVerifier.verifySlug(context, assets)).then(authorize(context))
                    .then(finalizeParameters(exchange, context)).then(chain.apply(exchange));
        });
    }

}
