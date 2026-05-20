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
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.provider.AuthorizeProvider;
import org.miaixz.bus.vortex.registry.AssetsRegistry;
import org.miaixz.bus.vortex.strategy.QualifierStrategy;

import reactor.core.publisher.Mono;

/**
 * Qualifies REST/API style requests by resolving route assets and applying route-level authorization.
 * <p>
 * The REST qualifier owns asset lookup, HTTP verb matching, and policy authorization. Signature and timestamp checks
 * are intentionally left to {@code RestVettingStrategy}, which runs after the asset has been resolved in the
 * {@link Context}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.SECOND)
public class RestQualifierStrategy extends QualifierStrategy {

    /**
     * Creates a REST qualifier strategy.
     *
     * @param provider credential validation provider
     * @param registry asset registry
     */
    public RestQualifierStrategy(AuthorizeProvider provider, AssetsRegistry registry) {
        super(provider, registry);
    }

    /**
     * Resolves the request route and stores the matched asset in the request context.
     *
     * @param exchange current exchange
     * @param chain    remaining strategy chain
     * @return qualification completion signal
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            Keying.RegistrySpec requestRoute = route(exchange, context);
            String namespace = requestRoute.namespacePart();
            String type = requestRoute.typeKeyPart();
            String appId = requestRoute.appIdPart();
            String method = requestRoute.methodPart();
            String version = requestRoute.versionPart();
            Integer verb = requestRoute.verbPart();

            return Mono.defer(() -> Mono.justOrEmpty(this.registry.get(requestRoute))).switchIfEmpty(Mono.defer(() -> {
                Logger.warn(
                        false,
                        "Vortex",
                        "REST asset not found: strategy=rest-qualifier, clientIp={}, namespace={}, type={}, appId={}, method={}, version={}, verb={}",
                        context.getX_request_ip(),
                        namespace,
                        type,
                        appId,
                        method,
                        version,
                        verb);
                return Mono.error(new ValidateException(ErrorCode._100800));
            })).flatMap(match -> {
                Assets assets = match.assets();
                context.setAssets(assets);
                Logger.info(
                        true,
                        "Vortex",
                        "REST asset resolved: strategy=rest-qualifier, clientIp={}, namespace={}, type={}, appId={}, method={}, version={}, verb={}, policy={}, sign={}, mode={}, host={}, port={}, path={}, url={}",
                        context.getX_request_ip(),
                        assets.getNamespace_id(),
                        assets.getType(),
                        assets.getApp_id(),
                        assets.getMethod(),
                        assets.getVersion(),
                        assets.getVerb(),
                        assets.getPolicy(),
                        assets.getSign(),
                        assets.getProtocol(),
                        assets.getHost(),
                        assets.getPort(),
                        assets.getPath(),
                        assets.getUrl());
                Mono<Void> validationMono = this.method(context, assets);
                Mono<Void> authMono = !Consts.ZERO.equals(assets.getPolicy())
                        ? this.authorize(
                                context,
                                attributes -> exchange.getAttributes().put(AUTHORIZATION_ATTRIBUTES, attributes))
                        : Mono.empty();
                return validationMono.then(authMono);
            }).then(
                    Mono.fromRunnable(
                            () -> Logger.info(
                                    true,
                                    "Vortex",
                                    "REST qualifier completed: strategy=rest-qualifier, clientIp={}, namespace={}, type={}, appId={}, method={}, version={}",
                                    context.getX_request_ip(),
                                    namespace,
                                    type,
                                    appId,
                                    method,
                                    version)))
                    .then(chain.apply(exchange));
        });
    }

}
