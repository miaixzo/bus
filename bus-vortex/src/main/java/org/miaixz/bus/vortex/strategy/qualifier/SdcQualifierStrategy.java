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

import java.util.Objects;

import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.cortex.Keying;
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
     * Configured bootstrap route, or {@code null} when bootstrap routing is disabled.
     */
    private final Bootstrap bootstrap;

    /**
     * Creates the SDC qualification stage.
     *
     * @param provider shared authorization provider
     * @param registry shared runtime asset registry
     */
    public SdcQualifierStrategy(AuthorizeProvider provider, AssetsRegistry registry) {
        super(provider, registry);
        this.bootstrap = null;
    }

    /**
     * Creates the SDC qualifier with the gateway identity adapter.
     *
     * @param provider   credential validation provider
     * @param registry   runtime asset registry
     * @param identities shared service identity adapter
     */
    public SdcQualifierStrategy(AuthorizeProvider provider, AssetsRegistry registry, IdentityGuard identities) {
        this(provider, registry, identities, null);
    }

    /**
     * Creates the SDC qualifier with optional bootstrap routing.
     *
     * @param provider   credential validation provider
     * @param registry   runtime asset registry
     * @param identities shared service identity adapter
     * @param bootstrap  configured route, or {@code null} to disable bootstrap routing
     */
    public SdcQualifierStrategy(AuthorizeProvider provider, AssetsRegistry registry, IdentityGuard identities,
            Bootstrap bootstrap) {
        super(provider, registry, identities);
        this.bootstrap = bootstrap;
    }

    /**
     * Resolves the configured bootstrap operation for eligible private requests.
     *
     * @param exchange current gateway exchange
     * @param route    structured route key
     * @return matching route, or {@code null} when the route or snapshot operation is unavailable
     */
    @Override
    protected AssetsRegistry.RouteMatch findRoute(ServerWebExchange exchange, Keying.RegistrySpec route) {
        if (bootstrap == null || !bootstrap.method().equals(route.methodPart())) {
            return super.findRoute(exchange, route);
        }
        if (!IdentityGuard.isPrivateSdc(exchange)
                || !Args.DEFAULT_VERSION.equals(route.versionPart())
                || !Integer.valueOf(Http.Method.GET.verb()).equals(route.verbPart())) {
            return null;
        }
        var match = bootstrap.route().get();
        if (match != null) {
            IdentityGuard.setBootstrapAudience(exchange, bootstrap.audience());
        }
        return match;
    }

    /**
     * Private SDC route available before the registry loads.
     *
     * @param method   configured SDC method
     * @param audience trusted downstream service code
     * @param route    supplies a route with a current synthetic instance heartbeat
     */
    public record Bootstrap(String method, String audience, SupplierX<AssetsRegistry.RouteMatch> route) {

        /**
         * Validates the required bootstrap route coordinates.
         */
        public Bootstrap {
            Objects.requireNonNull(method);
            Objects.requireNonNull(audience);
            Objects.requireNonNull(route);
        }

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
