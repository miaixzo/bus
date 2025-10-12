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
package org.miaixz.bus.vortex.strategy;

import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Represents a single, reusable filtering logic step (a "Strategy").
 * <p>
 * This is the core interface for the Strategy Pattern implementation within the filter chain. Each strategy
 * encapsulates a specific concern, such as request parsing, decryption, authorization, or rate limiting. Strategies are
 * composed into a dynamic chain by the {@code StrategyFactory} and executed by the {@code PrimaryFilter}.
 *
 * <p>
 * Implementations of this interface must also implement {@link Ordered} (usually by using the
 * {@link org.springframework.core.annotation.Order} annotation) to specify their execution priority within the chain.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Strategy {

    /**
     * Applies the filtering logic to the request.
     * <p>
     * An implementation of this method should perform its specific task and then delegate to the next strategy in the
     * chain by calling {@code chain.apply(exchange)}.
     * </p>
     *
     * @param exchange The current server exchange, which can be mutated by the strategy.
     * @param chain    The chain of remaining strategies to be executed.
     * @return A {@code Mono<Void>} that signals the completion of this strategy's execution.
     */
    Mono<Void> apply(ServerWebExchange exchange, StrategyChain chain);

}
