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

import java.util.concurrent.*;

import org.miaixz.bus.core.cache.provider.LRUCache;
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
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.magic.Performance;
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
 * Performance optimizations:
 * <ul>
 * <li>Optimized thread pool with core size based on CPU cores, max size for burst handling</li>
 * <li>Producer cache with bounded size (configurable via {@code vortex.performance.max-producer-cache-size})</li>
 * <li>Graceful shutdown with timeout handling</li>
 * <li>Rejection policy that CallerRunsPolicy to prevent message loss under load</li>
 * </ul>
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
     * A dedicated thread pool for asynchronously handling MQ message sending operations.
     * <p>
     * Performance optimizations:
     * <ul>
     * <li>Core pool size: CPU cores * 2 for optimal throughput</li>
     * <li>Max pool size: CPU cores * 4 for burst handling</li>
     * <li>Keep-alive time: 60 seconds for idle threads</li>
     * <li>CallerRunsPolicy: Prevents message rejection by running in caller thread under load</li>
     * </ul>
     */
    private final ExecutorService executor;

    /**
     * A thread-safe LRU cache of {@link Producer} instances, keyed by their broker URL.
     * <p>
     * Bounded to {@link Holder#getMaxProducerCacheSize()} to prevent memory leaks from broker URL proliferation. Uses
     * {@link LRUCache} with automatic LRU eviction - least recently used entries are automatically evicted when the
     * cache reaches maximum size.
     * <p>
     * Performance optimizations:
     * <ul>
     * <li>Automatic LRU eviction prevents memory leaks</li>
     * <li>Thread-safe with built-in ReentrantLock</li>
     * <li>Automatic resource cleanup via CacheListener</li>
     * <li>Lazy initialization of producers on first access</li>
     * </ul>
     */
    private final LRUCache<String, Producer> producerCache;

    /**
     * Constructs a new {@code MqExecutor} with centralized performance configuration.
     * <p>
     * Uses {@link Holder#getMaxProducerCacheSize()} to obtain the globally configured cache size, which is initialized
     * during application startup via {@link Holder#of(Performance)}.
     */
    public MqExecutor() {
        // Initialize LRU cache with capacity limit from global Holder
        this.producerCache = new LRUCache<>(Holder.getMaxProducerCacheSize(), 0);
        this.producerCache.setListener((key, producer) -> {
            try {
                producer.close();
                Logger.info(true, "MqExecutor", "Producer evicted from cache (LRU) for broker: {}", key);
            } catch (Exception e) {
                Logger.error("Failed to close evicted MQ Producer for broker: {}", key, e);
            }
        });

        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        int maxPoolSize = Runtime.getRuntime().availableProcessors() * 4;

        this.executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000), r -> {
                    Thread t = new Thread(r, "vortex-mq-producer-pool");
                    t.setDaemon(true);
                    t.setPriority(Thread.NORM_PRIORITY);
                    return t;
                }, new ThreadPoolExecutor.CallerRunsPolicy());
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
        }).subscribeOn(Schedulers.fromExecutor(this.executor))
                .doOnError(e -> Logger.error("Failed to send message to topic '{}'", assets.getMethod(), e));
    }

    /**
     * Retrieves an existing {@link Producer} from the cache or creates a new one if it doesn't exist.
     * <p>
     * Uses {@link LRUCache} with automatic LRU eviction - when the cache reaches the configured maximum
     * ({@link Holder#getMaxProducerCacheSize()}), least recently used entries are automatically evicted. Thread-safe
     * with built-in ReentrantLock.
     *
     * @param assets The configuration for the target MQ broker.
     * @return A thread-safe, cached {@link Producer} instance.
     */
    private Producer getOrCreateProducer(Assets assets) {
        String brokerUrl = assets.getHost() + Symbol.COLON + assets.getPort();

        // Use LRUCache's get method with supplier for thread-safe lazy initialization
        // The cache handles locking and double-checking internally
        return producerCache.get(brokerUrl, true, 0, () -> {
            Logger.info(true, "MqExecutor", "No existing MQ Producer for broker '{}'. Creating a new one.", brokerUrl);
            try {
                MQConfig config = MQConfig.of(brokerUrl);
                Producer producer = MQFactory.createEngine(config).getProducer();
                return producer;
            } catch (Exception e) {
                Logger.error(
                        true,
                        "MqExecutor",
                        "Failed to get or create MQ Producer for broker '{}': {}",
                        brokerUrl,
                        e.getMessage());
                throw new RuntimeException("Failed to get or create MQ Producer", e);
            }
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
            // Clear the cache - the listener will automatically close all producers
            producerCache.clear();
            this.executor.shutdown();
            Logger.info("MqExecutor shut down successfully.");
        }).subscribeOn(Schedulers.boundedElastic()).then(Mono.empty());
    }

}
