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
import org.miaixz.bus.extra.mq.MQConfig;
import org.miaixz.bus.extra.mq.MQFactory;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.Producer;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.*;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * MQ strategy router, responsible for forwarding requests to a message queue.
 * <p>
 * This class implements the {@link Router} interface to handle requests by sending them to a configured message queue.
 * It initializes MQ resources, manages message producers, and uses a dedicated thread pool for asynchronous message
 * sending. The router supports various message queue implementations through the MQFactory abstraction.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MqRequestRouter implements Router {

    /**
     * MQ configuration properties, injected via Spring's {@code @Resource}.
     * <p>
     * These properties are used to configure the message queue broker and other MQ-related settings. The properties
     * typically include the broker URL, connection parameters, authentication credentials, and other vendor-specific
     * configuration options. The Properties object provides a flexible way to configure different MQ implementations
     * without changing the code.
     */
    @Resource
    private Properties mqProperties;

    /**
     * The message queue producer, used for sending messages to a specified topic.
     * <p>
     * This producer is initialized based on the {@code mqProperties} and is responsible for creating and sending
     * messages to the message queue. The producer implementation is abstracted by the MQFactory, allowing for different
     * MQ providers (such as RabbitMQ, Kafka, ActiveMQ, etc.) to be used interchangeably. The producer is lazily
     * initialized during the first use or explicitly via the init() method.
     */
    private Producer producer;

    /**
     * A dedicated thread pool for asynchronously handling MQ message sending operations.
     * <p>
     * The pool size is set to twice the number of available processors to optimize concurrent message dispatch. This
     * sizing strategy provides a good balance between throughput and resource usage, allowing multiple messages to be
     * sent concurrently without overwhelming the system. All threads in this pool are daemon threads, meaning they
     * won't prevent JVM shutdown. The pool is used to offload message sending operations from the main request
     * processing thread, improving responsiveness and throughput.
     */
    private final ExecutorService mqExecutor = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2, r -> {
                Thread t = new Thread(r, "mq-producer-pool");
                t.setDaemon(true);
                return t;
            });

    /**
     * Initializes MQ resources.
     * <p>
     * This method extracts the broker URL from {@code mqProperties}, creates an {@link MQConfig}, adds any additional
     * properties, and then initializes the {@link Producer} using {@link MQFactory}. The initialization process
     * validates the configuration and establishes connections to the message queue broker. This method should be called
     * before any routing operations are performed.
     */
    public void init() {
        // Create MQ configuration from properties
        String brokerUrl = mqProperties.getProperty("mq.broker.url");
        MQConfig config = MQConfig.of(brokerUrl);

        // Add additional configuration properties
        mqProperties.forEach((key, value) -> {
            if (key instanceof String && value instanceof String) {
                String k = (String) key;
                if (!k.equals("mq.broker.url")) {
                    config.addProperty(k, (String) value);
                }
            }
        });

        // Create MQ provider and producer
        this.producer = MQFactory.createEngine(config).getProducer();
        Logger.info("==>       MQ: [N/A] [N/A] [N/A] [MQ_INIT] - MQ producer initialized with broker: {}", brokerUrl);
    }

    /**
     * Destroys MQ resources.
     * <p>
     * This method is annotated with {@code @PreDestroy} to ensure that MQ producer and the thread pool are properly
     * shut down when the application context is closed. It performs graceful shutdown of resources to prevent resource
     * leaks and ensure that all pending messages are properly handled before termination.
     */
    @PreDestroy
    public void destroy() {
        // Close the producer
        if (producer != null) {
            try {
                producer.close();
                Logger.info("==>       MQ: [N/A] [N/A] [N/A] [MQ_DESTROY] - MQ producer closed successfully");
            } catch (Exception e) {
                Logger.info(
                        "==>       MQ: [N/A] [N/A] [N/A] [MQ_DESTROY_ERROR] - Failed to close MQ producer: {}",
                        e.getMessage());
            }
        }

        // Shut down the thread pool
        mqExecutor.shutdown();
        Logger.info("==>       MQ: [N/A] [N/A] [N/A] [MQ_DESTROY] - MQ thread pool shutdown completed");
    }

    /**
     * Routes a client request to the message queue.
     * <p>
     * This method reads the request body, constructs an MQ {@link Message} with the asset's method as the topic, and
     * asynchronously sends it using the configured {@link Producer}. It handles timeouts and error conditions,
     * returning a JSON response indicating the status of the message forwarding. The method uses reactive programming
     * patterns to handle the request asynchronously and efficiently.
     *
     * @param request The client's {@link ServerRequest} object, containing request information.
     * @param context The request context, containing request parameters and configuration information.
     * @param assets  The configuration assets, containing configuration information for the target service.
     * @return {@link Mono} {@link ServerResponse} containing a JSON-formatted response, indicating that the message has
     *         been forwarded to the MQ.
     */
    @Override
    public Mono<ServerResponse> route(ServerRequest request, Context context, Assets assets) {
        // Get request method and path for logging
        String method = request.methodName();
        String path = request.path();

        // Log the start of routing
        Logger.info(
                "==>       MQ: [N/A] [{}] [{}] [MQ_ROUTE_START] - Method: {}, Topic: {}",
                method,
                path,
                assets.getMethod(),
                assets.getMethod());

        // Read the request body and forward to MQ
        long startTime = System.currentTimeMillis();
        return request.bodyToMono(String.class).flatMap(payload -> {
            // Log message sending
            Logger.info(
                    "==>       MQ: [N/A] [{}] [{}] [MQ_MESSAGE_SEND] - Method: {}, Payload size: {}",
                    method,
                    path,
                    assets.getMethod(),
                    payload.length());

            // Create message object (using anonymous implementation class)
            Message message = new Message() {

                /**
                 * The topic to which this message will be sent.
                 * <p>
                 * The topic is derived from the asset's method name, which serves as a routing key in the message
                 * queue. This allows messages to be routed to different consumers based on the method that generated
                 * them. The topic is final and immutable, as it should not change after message creation.
                 */
                private final String topic = assets.getMethod();

                /**
                 * The content of the message as a byte array.
                 * <p>
                 * The message content is derived from the request body, converted to bytes using UTF-8 encoding. This
                 * allows arbitrary data to be sent through the message queue, as long as it can be represented as a
                 * string. The byte array is final and immutable to ensure thread safety.
                 */
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

            // Asynchronously send the message
            return Mono.<Void>fromRunnable(() -> producer.send(message))
                    .subscribeOn(Schedulers.fromExecutor(mqExecutor)).timeout(Duration.ofMillis(assets.getTimeout()))
                    .thenReturn(payload);
        }).flatMap(payload -> {
            // Log successful response
            long duration = System.currentTimeMillis() - startTime;
            Logger.info(
                    "==>       MQ: [N/A] [{}] [{}] [MQ_ROUTE_SUCCESS] - Method: {}, Duration: {}ms",
                    method,
                    path,
                    assets.getMethod(),
                    duration);
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue("Request forwarded to MQ");
        }).doOnTerminate(() -> {
            long duration = System.currentTimeMillis() - startTime;
            Logger.info(
                    "==>       MQ: [N/A] [{}] [{}] [MQ_ROUTE_COMPLETE] - Method: {}, Duration: {}ms",
                    method,
                    path,
                    assets.getMethod(),
                    duration);
        }).onErrorResume(e -> {
            // Log error
            long duration = System.currentTimeMillis() - startTime;
            Logger.info(
                    "==>       MQ: [N/A] [{}] [{}] [MQ_ROUTE_ERROR] - Method: {}, Duration: {}ms, Error: {}",
                    method,
                    path,
                    assets.getMethod(),
                    duration,
                    e.getMessage());

            // Return error response
            return ServerResponse.status(500).contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"error\":\"Failed to forward request to MQ: " + e.getMessage() + "\"}");
        });
    }

}
