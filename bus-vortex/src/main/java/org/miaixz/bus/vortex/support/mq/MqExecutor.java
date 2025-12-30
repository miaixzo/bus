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
package org.miaixz.bus.vortex.support.mq;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.extra.mq.MQConfig;
import org.miaixz.bus.extra.mq.MQFactory;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.Producer;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.support.Coordinator;
import org.springframework.web.reactive.function.server.ServerResponse;

import jakarta.annotation.PreDestroy;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * A stateless executor for executing message queue operations.
 * <p>
 * This executor manages a cache of {@link Producer} instances, keyed by their broker URL. This allows the gateway to
 * efficiently execute messages to multiple different MQ brokers without creating new producers for each request. The
 * actual message sending is performed asynchronously on a dedicated thread pool to avoid blocking reactive threads.
 * </p>
 * <p>
 * The executor supports two response modes controlled by {@link Assets#getStream()}:
 * <ul>
 * <li>Buffering mode (stream = 1 or null): Returns a simple JSON acknowledgment response</li>
 * <li>Streaming mode (stream = 2): Returns the acknowledgment as a streaming response</li>
 * </ul>
 * </p>
 * Generic type parameters: {@code Executor<String, ServerResponse>}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MqExecutor extends Coordinator<String, ServerResponse> {

    /**
     * A dedicated thread pool for asynchronously handling MQ message sending operations. This prevents blocking the
     * main reactive threads with potentially slow network I/O.
     */
    private final ExecutorService executor;
    /**
     * A thread-safe cache of {@link Producer} instances, keyed by their broker URL. This ensures that a producer for a
     * given MQ broker is reused, optimizing resource usage.
     */
    private final Map<String, Producer> producerCache = new ConcurrentHashMap<>();

    /**
     * Constructs a new {@code MqExecutor} and initializes its dedicated thread pool.
     */
    public MqExecutor() {
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2, r -> {
            Thread t = new Thread(r, "vortex-mq-producer-pool");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Executes an MQ request using the provided context and String payload.
     * <p>
     * This method is required by the {@link org.miaixz.bus.vortex.Executor} interface. It sends the message to the MQ
     * broker and returns an acknowledgment response. The response format (streaming vs buffering) is selected based on
     * {@link Assets#getStream()}.
     *
     * @param context The request context containing the assets configuration
     * @param input   The String payload to send to the message queue
     * @return A Mono emitting the ServerResponse acknowledgment
     */
    @Override
    public Mono<ServerResponse> execute(Context context, String input) {
        Assets assets = context.getAssets();
        String payload = input;

        // Send message to MQ and get acknowledgment
        Mono<String> ackMono = send(assets, payload);

        // Select response strategy based on assets.getStream()
        boolean isStreaming = assets.getStream() != null && assets.getStream() == 2;

        if (isStreaming) {
            return executeStreaming(ackMono, assets);
        } else {
            return executeBuffering(ackMono);
        }
    }

    /**
     * Executes the MQ acknowledgment in streaming mode.
     * <p>
     * Returns the acknowledgment as a streaming JSON response.
     *
     * @param ackMono The mono containing the acknowledgment JSON
     * @param assets  The asset configuration
     * @return A streaming ServerResponse
     */
    private Mono<ServerResponse> executeStreaming(Mono<String> ackMono, Assets assets) {
        return ackMono.flatMap(ack -> {
            Logger.info(
                    false,
                    "MQ",
                    "[MQ_SUCCESS_STREAM] - Message forwarded to MQ topic: {} (streaming)",
                    assets.getMethod());

            return ServerResponse.ok().header(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON).bodyValue(ack);
        });
    }

    /**
     * Executes the MQ acknowledgment in buffering mode.
     * <p>
     * Returns the acknowledgment as a buffered JSON response.
     *
     * @param ackMono The mono containing the acknowledgment JSON
     * @return A buffered ServerResponse
     */
    private Mono<ServerResponse> executeBuffering(Mono<String> ackMono) {
        return ackMono.flatMap(ack -> {
            Logger.info(false, "MQ", "[MQ_SUCCESS_ATOMIC] - Message forwarded to MQ (atomic)");

            return ServerResponse.ok().header(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON).bodyValue(ack);
        });
    }

    /**
     * Asynchronously executes a message queue operation, defined by the provided {@link Assets}.
     *
     * @param assets  The configuration containing the MQ broker details (host, port) and topic.
     * @param payload The string content of the message to be sent.
     * @return A {@code Mono<String>} that emits the acknowledgment JSON when the message is sent successfully.
     */
    public Mono<String> send(Assets assets, String payload) {
        Message message = new Message() {

            @Override
            public String topic() {
                return assets.getMethod();
            }

            @Override
            public byte[] content() {
                return payload.getBytes(Charset.UTF_8);
            }
        };

        return Mono.fromCallable(() -> {
            Producer producer = getOrCreateProducer(assets);
            producer.send(message);
            return "{\"status\": \"Request forwarded to MQ\"}";
        }).subscribeOn(Schedulers.fromExecutor(this.executor)).timeout(Duration.ofMillis(assets.getTimeout()))
                .doOnError(e -> Logger.error("Failed to send message to topic '{}'", assets.getMethod(), e));
    }

    /**
     * Retrieves an existing {@link Producer} from the cache or creates a new one if it doesn't exist.
     *
     * @param assets The configuration for the target MQ broker.
     * @return A thread-safe, cached {@link Producer} instance.
     */
    private Producer getOrCreateProducer(Assets assets) {
        String brokerUrl = assets.getHost() + Symbol.COLON + assets.getPort();
        return producerCache.computeIfAbsent(brokerUrl, key -> {
            Logger.info("No existing MQ Producer for broker '{}'. Creating a new one.", key);
            MQConfig config = MQConfig.of(key);
            return MQFactory.createEngine(config).getProducer();
        });
    }

    /**
     * Gracefully shuts down all cached producers and the dedicated executor service. This method is automatically
     * called by Spring during application shutdown.
     *
     * @return A {@link Mono<ServerResponse>} that completes when shutdown is finished.
     */
    @PreDestroy
    @Override
    public Mono<ServerResponse> destroy() {
        return Mono.fromRunnable(() -> {
            Logger.info("Shutting down MqExecutor...");
            producerCache.values().forEach(producer -> {
                try {
                    producer.close();
                } catch (Exception e) {
                    Logger.error("Failed to close an MQ Producer", e);
                }
            });
            producerCache.clear();
            this.executor.shutdown();
            Logger.info("MqExecutor shut down successfully.");
        }).subscribeOn(Schedulers.boundedElastic()).then(Mono.empty());
    }

}
