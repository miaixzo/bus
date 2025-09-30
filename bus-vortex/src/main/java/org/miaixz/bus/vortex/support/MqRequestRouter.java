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
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Format;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.miaixz.bus.extra.mq.MQConfig;
import org.miaixz.bus.extra.mq.MQFactory;
import org.miaixz.bus.extra.mq.Producer;
import org.miaixz.bus.extra.mq.Message;

/**
 * MQ策略路由器，负责将请求转发到消息队列
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MqRequestRouter implements Router {

    /**
     * MQ配置属性
     */
    @Resource
    private Properties mqProperties;

    /**
     * 消息队列生产者，用于发送消息到指定的主题
     */
    private Producer producer;

    /**
     * 专用线程池，用于异步处理MQ消息发送
     */
    private final ExecutorService mqExecutor = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2, r -> {
                Thread t = new Thread(r, "mq-producer-pool");
                t.setDaemon(true);
                return t;
            });

    /**
     * 初始化MQ资源
     */
    public void init() {
        // 从配置属性中创建MQ配置
        String brokerUrl = mqProperties.getProperty("mq.broker.url");
        MQConfig config = MQConfig.of(brokerUrl);

        // 添加额外配置属性
        mqProperties.forEach((key, value) -> {
            if (key instanceof String && value instanceof String) {
                String k = (String) key;
                if (!k.equals("mq.broker.url")) {
                    config.addProperty(k, (String) value);
                }
            }
        });

        // 创建MQ提供者和生产者
        this.producer = MQFactory.createEngine(config).getProducer();
    }

    /**
     * 销毁MQ资源
     */
    @PreDestroy
    public void destroy() {
        // 关闭生产者
        if (producer != null) {
            try {
                producer.close();
            } catch (Exception e) {
                Format.error(null, "MQ_PRODUCER_CLOSE_ERROR", "Failed to close MQ producer");
            }
        }

        // 关闭线程池
        mqExecutor.shutdown();
    }

    /**
     * 路由客户端请求到消息队列
     *
     * @param request 客户端的 {@link ServerRequest} 对象，包含请求信息
     * @param context 请求上下文，包含请求参数和配置信息
     * @param assets  配置资产，包含目标服务的配置信息
     * @return {@link Mono}<{@link ServerResponse}> 包含 JSON 格式的响应，表明消息已发送到 MQ
     */
    @Override
    public Mono<ServerResponse> route(ServerRequest request, Context context, Assets assets) {
        // 记录路由开始
        Format.info(
                request.exchange(),
                "MQ_ROUTE_START",
                "Method: " + assets.getMethod() + ", Topic: " + assets.getMethod());

        // 读取请求体并转发到 MQ
        long startTime = System.currentTimeMillis();
        return request.bodyToMono(String.class).flatMap(payload -> {
            // 记录消息发送
            Format.debug(
                    request.exchange(),
                    "MQ_MESSAGE_SEND",
                    "Method: " + assets.getMethod() + ", Payload size: " + payload.length());

            // 创建消息对象（使用匿名实现类）
            Message message = new Message() {

                private final String topic = assets.getMethod();
                private final byte[] content = payload.getBytes(Charset.UTF_8);

                @Override
                public String topic() {
                    return topic;
                }

                @Override
                public byte[] content() {
                    return content;
                }
            };

            // 异步发送消息
            return Mono.<Void>fromRunnable(() -> producer.send(message))
                    .subscribeOn(Schedulers.fromExecutor(mqExecutor)).timeout(Duration.ofMillis(assets.getTimeout()))
                    .thenReturn(payload);
        }).flatMap(payload -> {
            // 记录成功响应
            long duration = System.currentTimeMillis() - startTime;
            Format.info(
                    request.exchange(),
                    "MQ_ROUTE_SUCCESS",
                    "Method: " + assets.getMethod() + ", Duration: " + duration + "ms");
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue("Request forwarded to MQ");
        }).doOnTerminate(() -> {
            long duration = System.currentTimeMillis() - startTime;
            Format.info(
                    request.exchange(),
                    "MQ_ROUTE_COMPLETE",
                    "Method: " + assets.getMethod() + ", Duration: " + duration + "ms");
        }).onErrorResume(e -> {
            // 记录错误
            long duration = System.currentTimeMillis() - startTime;
            Format.error(
                    request.exchange(),
                    "MQ_ROUTE_ERROR",
                    "Method: " + assets.getMethod() + ", Duration: " + duration + "ms, Error: " + e.getMessage());

            // 返回错误响应
            return ServerResponse.status(500).contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"error\":\"Failed to forward request to MQ: " + e.getMessage() + "\"}");
        });
    }

}
