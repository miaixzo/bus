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
package org.miaixz.bus.vortex.strategy.vetting;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpMethod;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.guard.IdentityGuard;
import org.miaixz.bus.vortex.strategy.VettingStrategy;
import org.miaixz.bus.vortex.strategy.request.SdcRequestStrategy.Input;

import reactor.core.publisher.Mono;

/**
 * Validates public SDC requests before credential qualification.
 *
 * @author Kimi Liu
 */
@org.springframework.core.annotation.Order(Order.SECOND)
public class SdcVettingStrategy extends VettingStrategy {

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
     * Validates the SDC request method and content type before applying shared validation.
     * <p>
     * POST requests accept JSON and private ingress additionally accepts form data. GET requests retain valueless
     * selectors for route and configuration queries.
     *
     * @param exchange prepared SDC exchange
     * @param context  shared gateway request context
     * @return completion of SDC and shared request validation
     */
    @Override
    protected Mono<Void> validateAndEnrich(ServerWebExchange exchange, Context context) {
        return Mono.deferContextual(view -> {
            Input input = view.get(Input.class);
            Http.Method method = context.getHttpMethod();
            if (method != Http.Method.GET && method != Http.Method.POST) {
                return Mono.error(
                        new MethodNotAllowedException(HttpMethod.valueOf(method.value()),
                                Set.of(HttpMethod.GET, HttpMethod.POST)));
            }
            boolean privateForm = IdentityGuard.isPrivateSdc(exchange)
                    && input.contentType() != null
                    && input.contentType().startsWith("application/x-www-form-urlencoded");
            if (method == Http.Method.POST && !MediaType.isJson(input.contentType()) && !privateForm) {
                return Mono.error(new UnsupportedMediaTypeStatusException("SDC POST requires JSON"));
            }
            if (method == Http.Method.GET) {
                for (String selector : List.of("routes", "configs")) {
                    if (input.queryNames().contains(selector)) {
                        context.getParameters().putIfAbsent(selector, "");
                    }
                }
            }
            return super.validateAndEnrich(exchange, context);
        });
    }

}
