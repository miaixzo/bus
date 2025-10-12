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
package org.miaixz.bus.vortex.filter;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Filter;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * Abstract base class for filters, providing common methods and implementing the template method pattern. All concrete
 * filters should extend this class and implement the {@link #doFilter(ServerWebExchange, WebFilterChain, Context)}
 * method.
 *
 * @author Justubborn
 * @since Java 17+
 */
public abstract class AbstractFilter implements Filter {

    /**
     * The main logic of the filter, which obtains the context and calls the internal filtering method of the subclass.
     *
     * @param exchange The current {@link ServerWebExchange} object, containing the request and response.
     * @param chain    The filter chain, used to continue processing the request.
     * @return {@link Mono<Void>} indicating the asynchronous completion of processing.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Logger.info("==>     Filter: {}", this.getClass().getSimpleName());
        return doFilter(exchange, chain, getContext(exchange))
                .doOnTerminate(() -> Logger.debug("<==     Filter: {}", this.getClass().getSimpleName()))
                .doOnError(e -> Logger.error("Error in {}: {}", this.getClass().getSimpleName(), e.getMessage()));
    }

    /**
     * Internal filtering method, to be implemented by subclasses for specific logic.
     *
     * @param exchange The current {@link ServerWebExchange} object.
     * @param chain    The filter chain.
     * @param context  The request context.
     * @return {@link Mono<Void>} indicating the asynchronous completion of processing.
     */
    protected abstract Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context);

    /**
     * Retrieves the request context.
     *
     * @param exchange The {@link ServerWebExchange} object.
     * @return The request context.
     * @throws ValidateException if the context is null.
     */
    protected Context getContext(ServerWebExchange exchange) {
        Context context = Context.get(exchange);
        if (context == null) {
            throw new ValidateException(ErrorCode._100805);
        }
        context.setHttpMethod(exchange.getRequest().getMethod());
        return context;
    }

}
