/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex.routing.mq;

import java.util.Map;
import java.util.Objects;

import org.springframework.web.reactive.function.server.ServerResponse;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.registry.ServiceRoutes;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Delivery;
import org.miaixz.bus.vortex.Egress;
import org.miaixz.bus.vortex.Executor;
import org.miaixz.bus.vortex.routing.Coordinator;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Translates MQ routes into broker messages and returns their JSON acknowledgments.
 * <p>
 * Producer resources are owned by {@link Egress.Mq}. The configured buffered and realtime modes both return one
 * acknowledgment. Download mode is rejected.
 *
 * @author Kimi Liu
 */
public class MqExecutor extends Coordinator<String, ServerResponse> {

    /**
     * Outbound message delivery and producer lifecycle.
     */
    private final Egress.Mq mq;

    /**
     * Creates an executor with its own MQ resources for standalone use.
     */
    public MqExecutor() {
        this(new Egress.Mq());
    }

    /**
     * Creates an executor using the configured MQ resource owner.
     *
     * @param mq outbound MQ resource owner
     */
    public MqExecutor(Egress.Mq mq) {
        this.mq = Objects.requireNonNull(mq);
    }

    /**
     * Executes an MQ request using the provided context and String payload.
     * <p>
     * This method is required by the {@link Executor} interface. It sends the message to the MQ broker and selects the
     * configured acknowledgment branch. MQ acknowledgments are always small, single JSON documents; they do not hold a
     * long-lived response stream.
     *
     * @param context The request context containing the assets configuration
     * @param input   The String payload to send to the message queue
     * @return A Mono emitting the ServerResponse acknowledgment
     */
    @Override
    public Mono<ServerResponse> execute(Context context, String input) {
        Assets assets = context.getAssets();
        String payload = input;

        Mono<String> ackMono = send(assets, payload);

        Delivery delivery = Delivery.of(assets.getStream());
        if (delivery == Delivery.DOWNLOAD) {
            return Mono.error(new IllegalStateException("MQ acknowledgements do not support download mode"));
        }
        boolean isStreaming = delivery == Delivery.STREAMING;

        if (isStreaming) {
            return executeStreaming(ackMono, assets);
        } else {
            return executeBuffering(ackMono);
        }
    }

    /**
     * Executes the realtime acknowledgment branch.
     * <p>
     * The broker API produces one acknowledgment value, so this branch returns one bounded JSON document rather than a
     * long-lived data-buffer stream.
     *
     * @param ackMono The mono containing the acknowledgment JSON
     * @param assets  The asset configuration
     * @return a single-value JSON response
     */
    private Mono<ServerResponse> executeStreaming(Mono<String> ackMono, Assets assets) {
        return ackMono.flatMap(ack -> {
            Logger.info(
                    false,
                    "Vortex",
                    "Message forwarded to MQ topic: protocol=mq, event=MQ_SUCCESS_STREAM, topic={}, mode=streaming",
                    assets.getMethod());

            return ServerResponse.ok().header(Http.Header.CONTENT_TYPE, MediaType.APPLICATION_JSON).bodyValue(ack);
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
            Logger.info(false, "Vortex", "Message accepted for buffered delivery: protocol=mq");

            return ServerResponse.ok().header(Http.Header.CONTENT_TYPE, MediaType.APPLICATION_JSON).bodyValue(ack);
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

            /**
             * Returns the MQ topic resolved from the asset method name.
             *
             * @return target topic name
             */
            @Override
            public String topic() {
                return assets.getMethod();
            }

            /**
             * Returns the outgoing MQ payload encoded as UTF-8 bytes.
             *
             * @return serialized payload bytes
             */
            @Override
            public byte[] content() {
                return payload.getBytes(Charset.UTF_8);
            }

        };

        String brokerUrl = ServiceRoutes.resolve(assets).getRawAuthority();
        return mq.send(brokerUrl, message)
                .then(Mono.fromSupplier(() -> JsonKit.toJsonString(Map.of("status", "Request forwarded to MQ"))));
    }

    /**
     * Releases this executor's MQ resources when the executor is disposed directly.
     *
     * @return completion after the MQ resource owner begins shutdown
     */
    @Override
    public Mono<ServerResponse> destroy() {
        return Mono.fromRunnable(mq::close).subscribeOn(Schedulers.boundedElastic()).then(Mono.empty());
    }

}
