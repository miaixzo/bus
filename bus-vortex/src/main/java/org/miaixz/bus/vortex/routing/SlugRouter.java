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
package org.miaixz.bus.vortex.routing;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.routing.slug.SlugExecutor;

import reactor.core.publisher.Mono;

/**
 * Router for public slug forwarding requests.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SlugRouter implements Router<ServerRequest, ServerResponse> {

    /**
     * Executor responsible for public slug forwarding.
     */
    private final SlugExecutor executor;

    /**
     * Creates a slug router.
     *
     * @param executor slug executor
     */
    public SlugRouter(SlugExecutor executor) {
        this.executor = executor;
    }

    /**
     * Routes the request using the current context.
     *
     * @param request server request
     * @return downstream response
     */
    @Override
    public Mono<ServerResponse> route(ServerRequest request) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            return this.executor.execute(context, request);
        });
    }

}
