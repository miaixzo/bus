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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.extra.mq.MQConfig;
import org.miaixz.bus.extra.mq.MQFactory;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.Producer;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;

import jakarta.annotation.PreDestroy;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * A dedicated service for sending messages to a message queue. This class encapsulates the logic for initializing,
 * managing, and using an MQ Producer, including its own dedicated thread pool for non-blocking operations.
 */
public class MqService {

    /**
     * A dedicated thread pool for asynchronously handling MQ message sending operations. This prevents blocking the
     * main reactive threads.
     */
    private final ExecutorService executor;
    /**
     * The underlying message queue producer used for sending messages. It is initialized based on the provided MQ
     * properties.
     */
    private Producer producer;

    /**
     * Constructs a new MqProducerService.
     */
    public MqService() {
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2, r -> {
            Thread t = new Thread(r, "vortex-mq-producer-pool");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Initializes the MQ producer based on the given properties.
     * 
     * @param assets The configuration properties for the message queue.
     */
    private void init(Assets assets) {
        String brokerUrl = assets.getHost() + ":" + assets.getPort();
        if (brokerUrl == null) {
            throw new IllegalArgumentException("mq.broker.url property is missing.");
        }
        MQConfig config = MQConfig.of(brokerUrl);
        this.producer = MQFactory.createEngine(config).getProducer();
        Logger.info("MQ Producer initialized successfully for broker: {}", brokerUrl);
    }

    /**
     * Asynchronously sends a message to the message queue.
     *
     * @param topic   The topic to which the message will be sent.
     * @param payload The string content of the message.
     * @param timeout The timeout duration for the send operation.
     * @return A Mono that completes when the message is sent, or errors out on failure or timeout.
     */
    public Mono<Void> send(String topic, String payload, Duration timeout) {
        Message message = new Message() {

            @Override
            public String topic() {
                return topic;
            }

            @Override
            public byte[] content() {
                return payload.getBytes(Charset.UTF_8);
            }
        };

        return Mono.<Void>fromRunnable(() -> producer.send(message)).subscribeOn(Schedulers.fromExecutor(this.executor))
                .timeout(timeout).doOnError(e -> Logger.error("Failed to send message to topic '{}'", topic, e));
    }

    /**
     * Gracefully shuts down the producer and the executor service. This method is intended to be called when the
     * application is shutting down.
     */
    @PreDestroy
    public void destroy() {
        if (producer != null) {
            try {
                producer.close();
                Logger.info("MQ Producer closed successfully.");
            } catch (Exception e) {
                Logger.error("Failed to close MQ Producer", e);
            }
        }
        this.executor.shutdown();
        Logger.info("MQ Producer thread pool shut down.");
    }

}
