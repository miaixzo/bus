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

import jakarta.annotation.PreDestroy;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.extra.mq.MQConfig;
import org.miaixz.bus.extra.mq.MQFactory;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.Producer;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A stateless service for sending messages to various message queues.
 * <p>
 * This service manages a cache of {@link Producer} instances, keyed by their broker URL. This allows the gateway to
 * efficiently route messages to multiple different MQ brokers without creating new producers for each request. The
 * actual message sending is performed asynchronously on a dedicated thread pool to avoid blocking reactive threads.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MqService {

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
     * Constructs a new {@code MqService} and initializes its dedicated thread pool.
     */
    public MqService() {
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2, r -> {
            Thread t = new Thread(r, "vortex-mq-producer-pool");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Asynchronously sends a message to a message queue, defined by the provided {@link Assets}.
     *
     * @param assets  The configuration containing the MQ broker details (host, port) and topic.
     * @param payload The string content of the message to be sent.
     * @return A {@code Mono<Void>} that completes when the message is sent, or errors out on failure or timeout.
     */
    public Mono<Void> send(Assets assets, String payload) {
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

        return Mono.<Void>fromRunnable(() -> {
            Producer producer = getOrCreateProducer(assets);
            producer.send(message);
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
     */
    @PreDestroy
    public void destroy() {
        Logger.info("Shutting down MqService...");
        producerCache.values().forEach(producer -> {
            try {
                producer.close();
            } catch (Exception e) {
                Logger.error("Failed to close an MQ Producer", e);
            }
        });
        producerCache.clear();
        this.executor.shutdown();
        Logger.info("MqService shut down successfully.");
    }

}
