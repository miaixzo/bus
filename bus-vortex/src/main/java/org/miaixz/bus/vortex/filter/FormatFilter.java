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

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Format;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应格式化过滤器，确保所有响应数据都是JSON格式
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.LOWEST_PRECEDENCE - 2)
public class FormatFilter extends AbstractFilter {

    /**
     * 内部过滤方法，执行响应格式化逻辑
     *
     * @param exchange 当前的 ServerWebExchange 对象
     * @param chain    过滤器链
     * @param context  请求上下文
     * @return {@link Mono<Void>} 表示异步处理完成
     */
    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        Format.info(exchange, "REQUEST_START", "Method: " + exchange.getRequest().getMethod() + ", Path: "
                + exchange.getRequest().getPath().value() + ", Query: " + exchange.getRequest().getQueryParams());
        // 如果请求明确要求XML格式，则转换为JSON
        if (Format.XML.equals(context.getFormat())) {
            Format.info(exchange, "FORMAT_CONVERT_TO_JSON", "Converting XML request to JSON response");
            exchange = exchange.mutate().response(process(exchange)).build();
        }
        return chain.filter(exchange);
    }

    /**
     * 创建响应装饰器，确保响应数据为JSON格式
     *
     * @param exchange ServerWebExchange 对象
     * @return 装饰后的 ServerHttpResponseDecorator
     */
    private ServerHttpResponseDecorator process(ServerWebExchange exchange) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {
            /**
             * 重写响应写入逻辑，处理数据格式化
             *
             * @param body 响应数据流
             * @return {@link Mono<Void>} 表示异步写入完成
             */
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                // 将响应数据流合并为单个缓冲区
                Flux<? extends DataBuffer> flux = Flux.from(body);
                return super.writeWith(DataBufferUtils.join(flux).map(dataBuffer -> {
                    Context context = Context.get(exchange);
                    // 设置响应内容类型为上下文指定的媒体类型
                    exchange.getResponse().getHeaders().setContentType(context.getFormat().getMediaType());
                    // 将缓冲区解码为字符串
                    String bodyString = java.nio.charset.Charset.defaultCharset().decode(dataBuffer.asByteBuffer())
                            .toString();
                    DataBufferUtils.release(dataBuffer); // 释放缓冲区
                    // 解析为 Message 对象
                    Message message = JsonKit.toPojo(bodyString, Message.class);
                    // 使用上下文指定的提供者序列化消息
                    String formatBody = context.getFormat().getProvider().serialize(message);
                    // 记录 TRACE 日志（如果启用）
                    Format.trace(exchange, "RESPONSE_FORMATTED", formatBody);
                    // 将格式化后的数据写入新缓冲区
                    return bufferFactory().wrap(formatBody.getBytes());
                }));
            }
        };
    }

}