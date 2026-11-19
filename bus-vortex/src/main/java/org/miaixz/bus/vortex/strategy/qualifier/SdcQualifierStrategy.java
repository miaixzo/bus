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

import java.net.URI;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.guard.GuardPolicy;
import org.miaixz.bus.cortex.registry.api.ApiAssets;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.provider.AuthorizeProvider;
import org.miaixz.bus.vortex.registry.AssetsRegistry;
import org.miaixz.bus.vortex.strategy.QualifierStrategy;
import org.miaixz.bus.vortex.strategy.request.SdcRequestStrategy.Input;

import reactor.core.publisher.Mono;

/**
 * Qualifies an SDC request with the configured application authentication and forwarding endpoint.
 *
 * @author Kimi Liu
 */
@org.springframework.core.annotation.Order(Order.THIRD)
public class SdcQualifierStrategy extends QualifierStrategy {

    /**
     * Supplies the authenticated upstream endpoint.
     */
    private final Supplier<URI> endpoint;

    /**
     * Exchanges an application credential for the credential accepted by the upstream.
     */
    private final Function<String, String> authenticator;

    /**
     * Creates the SDC qualification stage.
     *
     * @param provider      shared authorization provider
     * @param registry      shared runtime asset registry
     * @param endpoint      authenticated upstream endpoint
     * @param authenticator application credential verifier and upstream credential issuer
     */
    public SdcQualifierStrategy(AuthorizeProvider provider, AssetsRegistry registry, Supplier<URI> endpoint,
            Function<String, String> authenticator) {
        super(provider, registry);
        this.endpoint = Objects.requireNonNull(endpoint);
        this.authenticator = Objects.requireNonNull(authenticator);
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
     * Authenticates the application, assigns the configured upstream as the route target, and marks the response with
     * {@code Cache-Control: no-store}.
     *
     * @param exchange prepared SDC exchange
     * @param chain    remaining strategies in the SDC chain
     * @return completion of credential qualification and the remaining strategy chain
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(view -> {
            Context context = view.get(Context.class);
            return authorize(context).then(Mono.defer(() -> {
                URI target = endpoint.get();
                Assets assets = ApiAssets.builder().type(Type.API.key()).method(Args.SDC_PATH_PREFIX)
                        .version(Args.DEFAULT_VERSION).verb(context.getHttpMethod().verb()).protocol(Args.ROUTE_SDC)
                        .policy(GuardPolicy.CREDENTIAL.code()).scheme(target.getScheme()).url(target.toASCIIString())
                        .stream(1).timeout(60).retries(0).build();
                context.setAssets(assets);
                exchange.getResponse().beforeCommit(() -> {
                    exchange.getResponse().getHeaders().setCacheControl(CacheControl.noStore());
                    return Mono.empty();
                });
                return method(context, assets).then(Mono.defer(() -> chain.apply(exchange)));
            }));
        });
    }

    /**
     * Exchanges exactly one application API key for the upstream credential and stores it for the executor.
     *
     * @param context shared gateway request context
     * @return completion after credential exchange, or an unauthorized error
     */
    @Override
    protected Mono<Void> authorize(Context context) {
        return Mono.deferContextual(view -> {
            var keys = view.get(Input.class).apiKeys();
            String credential = keys.size() == 1 ? authenticator.apply(keys.getFirst()) : null;
            if (credential == null) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
            }

            context.setHeader(Http.Auth.API_KEY_HEADER, credential);
            return Mono.empty();
        });
    }

}
