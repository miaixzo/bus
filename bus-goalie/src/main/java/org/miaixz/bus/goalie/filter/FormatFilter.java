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
package org.miaixz.bus.goalie.filter;

import java.nio.charset.Charset;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.goalie.Context;
import org.miaixz.bus.logger.Logger;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 格式化
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.LOWEST_PRECEDENCE - 2)
public class FormatFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Context context = Context.get(exchange);
        if (Context.Format.xml.equals(context.getFormat())) {
            exchange = exchange.mutate().response(process(exchange)).build();
        }
        return chain.filter(exchange);
    }

    private ServerHttpResponseDecorator process(ServerWebExchange exchange) {
        Context context = Context.get(exchange);
        return new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                Flux<? extends DataBuffer> flux = Flux.from(body);
                return super.writeWith(DataBufferUtils.join(flux).map(dataBuffer -> {
                    exchange.getResponse().getHeaders().setContentType(context.getFormat().getMediaType());
                    String bodyString = Charset.defaultCharset().decode(dataBuffer.asByteBuffer()).toString();
                    DataBufferUtils.release(dataBuffer);
                    Message message = JsonKit.toPojo(bodyString, Message.class);
                    String formatBody = context.getFormat().getProvider().serialize(message);
                    if (Logger.isTraceEnabled()) {
                        Logger.trace("traceId:{},resp <= {}", exchange.getLogPrefix(), formatBody);
                    }
                    return bufferFactory().wrap(formatBody.getBytes());
                }));
            }
        };
    }

}
