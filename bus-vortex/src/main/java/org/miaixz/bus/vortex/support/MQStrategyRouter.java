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

import java.time.Duration;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Format;
import org.miaixz.bus.vortex.Strategy;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import jakarta.annotation.Resource;
import reactor.core.publisher.Mono;

/**
 * MQ策略路由器，负责将请求转发到消息队列
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MQStrategyRouter implements Strategy {
    /**
     * 消息队列生产者，用于发送消息到指定的主题。
     * <p>
     * 通过 {@code @Resource} 注入，负责与消息队列系统交互。
     * </p>
     */
    @Resource
    private MqProducer mqProducer;

    /**
     * 路由客户端请求到消息队列。
     *
     * @param request 客户端的 {@link ServerRequest} 对象，包含请求信息
     * @param context 请求上下文，包含请求参数和配置信息
     * @param assets  配置资产，包含目标服务的配置信息
     * @return {@link Mono}<{@link ServerResponse}> 包含 JSON 格式的响应，表明消息已发送到 MQ
     */
    @Override
    public Mono<ServerResponse> route(ServerRequest request, Context context, Assets assets) {
        // 记录路由开始
        Format.info(request.exchange(), "MQ_ROUTE_START",
                "Method: " + assets.getMethod() + ", Topic: " + assets.getMethod());

        // 读取请求体并转发到 MQ
        long startTime = System.currentTimeMillis();
        return request.bodyToMono(String.class).flatMap(payload -> {
            // 记录消息发送
            Format.debug(request.exchange(), "MQ_MESSAGE_SEND",
                    "Method: " + assets.getMethod() + ", Payload size: " + payload.length());
            return mqProducer.send(assets.getMethod(), payload).timeout(Duration.ofMillis(assets.getTimeout()))
                    .thenReturn(payload);
        }).flatMap(payload -> {
            // 记录成功响应
            long duration = System.currentTimeMillis() - startTime;
            Format.info(request.exchange(), "MQ_ROUTE_SUCCESS",
                    "Method: " + assets.getMethod() + ", Duration: " + duration + "ms");
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue("Request forwarded to MQ");
        }).doOnTerminate(() -> {
            long duration = System.currentTimeMillis() - startTime;
            Format.info(request.exchange(), "MQ_ROUTE_COMPLETE",
                    "Method: " + assets.getMethod() + ", Duration: " + duration + "ms");
        });
    }

}