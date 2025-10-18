/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex.support;

import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.support.rest.RestService;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

/**
 * A {@link Router} implementation for forwarding requests to standard RESTful HTTP/HTTPS downstream services.
 * <p>
 * This class acts as a simple coordinator. Its sole responsibility is to retrieve the necessary context and asset
 * information for the current request and delegate the actual execution of the HTTP reverse proxy logic to the
 * {@link RestService}.
 *
 * @author Kimi Liu
 * @see RestService
 * @since Java 17+
 */
public class RestRouter implements Router {

    /**
     * The service responsible for executing the downstream HTTP request.
     */
    private final RestService service;

    /**
     * Constructs a new {@code RestRouter}.
     *
     * @param service The service that will perform the HTTP request execution.
     */
    public RestRouter(RestService service) {
        this.service = service;
    }

    /**
     * Routes the request to a downstream HTTP service.
     * <p>
     * This method retrieves the {@link Context} and its contained {@link Assets} from the reactive stream, then
     * delegates the execution to the {@link RestService}.
     *
     * @param request The current {@link ServerRequest}.
     * @return A {@code Mono<ServerResponse>} containing the response from the downstream service.
     */
    @NonNull
    @Override
    public Mono<ServerResponse> route(ServerRequest request) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            final Assets assets = context.getAssets();
            return this.service.execute(request, context, assets);
        });
    }

}
