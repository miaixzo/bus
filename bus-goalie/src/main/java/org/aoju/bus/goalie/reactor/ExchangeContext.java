/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.goalie.reactor;

import lombok.Data;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Optional;

/**
 * 上下文传参
 *
 * @author Justubborn
 * @since 2020/10/30
 */
@Data
public class ExchangeContext {

    /**
     * 交换内容
     */
    private final static String $ = "exchange_context";

    /**
     * 请求参数
     */
    private Map<String, String> requestMap;

    /**
     * 返回body
     */
    private Flux<DataBuffer> body;

    private Asset asset;

    public static ExchangeContext get(ServerWebExchange exchange) {
        ExchangeContext context = exchange.getAttribute(ExchangeContext.$);

        return Optional.ofNullable(context).orElseGet(() -> {
            ExchangeContext empty = new ExchangeContext();
            exchange.getAttributes().put(ExchangeContext.$, empty);
            return empty;
        });
    }

    public static ExchangeContext get(ServerRequest request) {
        return (ExchangeContext) request.attribute(ExchangeContext.$).orElseGet(() -> {
            ExchangeContext empty = new ExchangeContext();
            request.attributes().put(ExchangeContext.$, empty);
            return empty;
        });
    }

}
