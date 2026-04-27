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
package org.miaixz.bus.vortex.strategy;

import java.util.List;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Formats;
import org.miaixz.bus.vortex.Provider;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A filter strategy for response formatting. This strategy intercepts the response and serializes the body to the
 * format specified in the request context (e.g., XML).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class ResponseStrategy extends AbstractStrategy {

    /**
     * Creates a response strategy.
     */
    public ResponseStrategy() {
    }

    /**
     * Applies the response formatting strategy based on the requested format.
     * <p>
     * This method checks the format specified in the context and decorates the response accordingly:
     * <ul>
     * <li>XML format: Applies XML transformation decorator</li>
     * <li>BINARY format: Applies binary stream handling decorator</li>
     * <li>JSON format: No transformation needed (default)</li>
     * </ul>
     *
     * @param exchange The current server exchange containing the request and response
     * @param chain    The next strategy in the chain
     * @return A Mono signaling completion of this strategy's processing
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            ServerWebExchange newExchange = exchange;
            final String ip = context.getX_request_ip();

            Logger.info(
                    true,
                    "Response",
                    "[{}] Processing response - Format: {}, Channel: {}, Parameters count: {}",
                    ip,
                    context.getFormat(),
                    context.getChannel(),
                    context.getParameters().size());

            if (!context.getParameters().isEmpty()) {
                Logger.info(true, "ResponseStrategy", "[{}] Parameters in response: {}", ip, context.getParameters());
            }

            Logger.debug(true, "Response", "[{}] Strategy applying for format: {}", ip, context.getFormat());

            if (Formats.XML.equals(context.getFormat())) {
                Logger.debug(true, "Response", "[{}] Format is XML, applying response transformation.", ip);
                newExchange = exchange.mutate().response(processXml(exchange, context)).build();
            }

            if (Formats.BINARY.equals(context.getFormat())) {
                Logger.debug(true, "Response", "[{}] Format is BINARY, applying binary stream handling.", ip);
                newExchange = exchange.mutate().response(processBinary(exchange, context)).build();
            }

            return chain.apply(newExchange);
        });
    }

    /**
     * Creates a response decorator to serialize the response body to XML.
     * <p>
     * This method wraps the original response and overrides the writeWith method to intercept the response body. It
     * assumes the original body is a JSON string, converts it to XML, and sets the Content-Type header to
     * 'application/xml'.
     * </p>
     *
     * @param exchange The {@link ServerWebExchange} object.
     * @param context  The request context (for logging).
     * @return The decorated {@link ServerHttpResponseDecorator}.
     */
    private ServerHttpResponseDecorator processXml(ServerWebExchange exchange, Context context) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {

            /**
             * Intercepts and transforms the response body to XML format.
             * <p>
             * This override:
             * <ol>
             * <li>Collects the original response body buffers</li>
             * <li>Merges them into a single byte array</li>
             * <li>Converts to string (assumes original is JSON)</li>
             * <li>Serializes to XML using the XML provider</li>
             * <li>Wraps the XML result in a new DataBuffer</li>
             * </ol>
             *
             * @param body The publisher emitting the original response body data buffers
             * @return A Mono signaling completion of the write operation
             */
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                Flux<? extends DataBuffer> flux = Flux.from(body);

                Mono<List<? extends DataBuffer>> collectedBuffers = flux.collectList().map(list -> list);

                Mono<DataBuffer> formattedBufferMono = collectedBuffers.flatMap(dataBuffers -> {
                    byte[] allBytes = merge(dataBuffers);
                    String bodyString = new String(allBytes, Charset.UTF_8);

                    Provider provider = Formats.XML.getProvider();

                    return provider.serialize(bodyString).map(xmlBody -> {
                        String xmlString = xmlBody.toString();
                        Logger.trace(
                                false,
                                "Response",
                                "[{}] Response formatted to XML: {}",
                                context.getX_request_ip(),
                                xmlString);
                        return bufferFactory().wrap(xmlString.getBytes(Charset.UTF_8));
                    });
                });

                getDelegate().getHeaders().setContentType(Formats.XML.getMediaType());

                return super.writeWith(formattedBufferMono);
            }
        };
    }

    /**
     * Creates a response decorator to handle binary data streams.
     * <p>
     * This method wraps the original response and ensures binary data is properly handled without string conversion. It
     * sets the appropriate Content-Type header and preserves the original binary data stream.
     * </p>
     *
     * @param exchange The {@link ServerWebExchange} object.
     * @param context  The request context (for logging).
     * @return The decorated {@link ServerHttpResponseDecorator}.
     */
    private ServerHttpResponseDecorator processBinary(ServerWebExchange exchange, Context context) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {

            /**
             * Handles binary data streams without conversion.
             * <p>
             * This override ensures binary data (files, images, PDFs) is streamed directly without string conversion
             * that could corrupt the data. It preserves the original binary stream and sets appropriate headers.
             *
             * @param body The publisher emitting the binary response data buffers
             * @return A Mono signaling completion of the write operation
             */
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                Logger.debug(true, "Response", "[{}] Processing binary stream response", context.getX_request_ip());

                getDelegate().getHeaders().setContentType(Formats.BINARY.getMediaType());

                if (body instanceof Flux) {
                    Logger.debug(
                            true,
                            "Response",
                            "[{}] Binary Flux detected, streaming directly",
                            context.getX_request_ip());
                    return super.writeWith(body);
                }

                Flux<? extends DataBuffer> flux = Flux.from(body);

                return flux.doOnNext(dataBuffer -> {
                    Logger.debug(
                            true,
                            "Response",
                            "[{}] Binary data chunk: {} bytes",
                            context.getX_request_ip(),
                            dataBuffer.readableByteCount());
                }).then(super.writeWith(flux));
            }
        };
    }

    /**
     * Merges multiple data buffers into a single byte array.
     *
     * @param dataBuffers The list of data buffers.
     * @return The merged byte array.
     */
    private byte[] merge(List<? extends DataBuffer> dataBuffers) {
        int totalBytes = dataBuffers.stream().mapToInt(DataBuffer::readableByteCount).sum();

        byte[] result = new byte[totalBytes];

        int position = 0;
        for (DataBuffer buffer : dataBuffers) {
            int length = buffer.readableByteCount();
            buffer.read(result, position, length);
            position += length;
        }

        return result;
    }

}
