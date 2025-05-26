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
 * 响应格式化过滤器，负责将响应数据格式化为指定格式（当前支持 XML）
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.LOWEST_PRECEDENCE - 2)
public class FormatFilter implements WebFilter {

    /**
     * 过滤器主逻辑，检查是否需要格式化响应并装饰响应
     *
     * @param exchange 当前的 ServerWebExchange 对象，包含请求和响应
     * @param chain    过滤器链，用于继续处理请求
     * @return {@link Mono<Void>} 表示异步处理完成
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Context context = Context.get(exchange);
        // 检查响应格式是否为 XML
        if (Context.Format.xml.equals(context.getFormat())) {
            // 装饰响应以支持格式化
            exchange = exchange.mutate().response(process(exchange)).build();
        }
        // 继续执行过滤器链
        return chain.filter(exchange);
    }

    /**
     * 创建响应装饰器，拦截并格式化响应数据
     *
     * @param exchange ServerWebExchange 对象
     * @return 装饰后的 ServerHttpResponseDecorator
     */
    private ServerHttpResponseDecorator process(ServerWebExchange exchange) {
        Context context = Context.get(exchange);
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
                    // 设置响应内容类型为上下文指定的媒体类型
                    exchange.getResponse().getHeaders().setContentType(context.getFormat().getMediaType());
                    // 将缓冲区解码为字符串
                    String bodyString = Charset.defaultCharset().decode(dataBuffer.asByteBuffer()).toString();
                    DataBufferUtils.release(dataBuffer); // 释放缓冲区
                    // 解析为 Message 对象
                    Message message = JsonKit.toPojo(bodyString, Message.class);
                    // 使用上下文指定的提供者序列化消息
                    String formatBody = context.getFormat().getProvider().serialize(message);
                    // 记录 TRACE 日志（如果启用）
                    if (Logger.isTraceEnabled()) {
                        Logger.trace("traceId:{},resp <= {}", exchange.getLogPrefix(), formatBody);
                    }
                    // 将格式化后的数据写入新缓冲区
                    return bufferFactory().wrap(formatBody.getBytes());
                }));
            }
        };
    }

}