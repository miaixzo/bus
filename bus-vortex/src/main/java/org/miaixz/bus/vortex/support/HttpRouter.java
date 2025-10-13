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
import org.miaixz.bus.vortex.support.http.HttpService;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

/**
 * HTTP 协议的请求路由器。 它的职责是协调请求处理，并将实际的 HTTP 执行工作委托给 HttpExecutor。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HttpRouter implements Router {

    /**
     * 在一个完整的Spring应用中，HttpExecutor 可能会被声明为@Component并在这里注入。
     * <p>
     * 在当前 VortexHandler 的设计中，每次请求都创建一个新的执行器实例是可行的。
     */
    private HttpService service;

    public HttpRouter(HttpService service) {
        this.service = service;
    }

    /**
     * 将 HTTP 请求路由到下游服务。 此方法仅作为协调者，将所有实际工作委托给 HttpExecutor。
     *
     * @param request The client's {@link ServerRequest} object.
     * @param context The request context, containing request parameters and configuration information.
     * @param assets  The configuration assets, containing configuration information for the target service.
     * @return {@link Mono<ServerResponse>} containing the response from the target service.
     */
    @NonNull
    @Override
    public Mono<ServerResponse> route(ServerRequest request, Context context, Assets assets) {
        return this.service.execute(request, context, assets);
    }

}
