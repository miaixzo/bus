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

import java.time.Duration;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Formats;
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.Octets;
import org.miaixz.bus.vortex.Provider;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A filter strategy for response formatting. This strategy intercepts the response and serializes the body to the
 * format specified in the request context (e.g., XML).
 *
 * @author Kimi Liu
 */
@org.springframework.core.annotation.Order(Order.FIFTH)
public class ResponseStrategy extends AbstractStrategy {

    /**
     * Creates a response strategy.
     */
    public ResponseStrategy() {
        // No initialization required.
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
                    "Vortex",
                    "Response processing started: strategy=response, clientIp={}, format={}, channel={}, parameterCount={}",
                    ip,
                    context.getFormat(),
                    context.getChannel(),
                    context.getParameters().size());

            if (!context.getParameters().isEmpty()) {
                Logger.debug(
                        true,
                        "Vortex",
                        "Response parameters prepared: strategy=response, clientIp={}, parameterCount={}",
                        ip,
                        context.getParameters().size());
            }

            Logger.debug(
                    true,
                    "Vortex",
                    "Response strategy applying: strategy=response, clientIp={}, format={}",
                    ip,
                    context.getFormat());

            if (Formats.XML.equals(context.getFormat())) {
                Logger.debug(
                        true,
                        "Vortex",
                        "XML response transformation selected: strategy=response, clientIp={}",
                        ip);
                newExchange = exchange.mutate().response(processXml(exchange, context)).build();
            }

            if (Formats.BINARY.equals(context.getFormat())) {
                Logger.debug(
                        true,
                        "Vortex",
                        "Binary response stream handling selected: strategy=response, clientIp={}",
                        ip);
                newExchange = exchange.mutate().response(processBinary(exchange, context)).build();
            }

            return chain.apply(newExchange);
        });
    }

    /**
     * Creates a response decorator to serialize the response body to XML.
     * <p>
     * This method wraps the original response and intercepts its bounded JSON body. Input and worst-case output
     * capacity are acquired asynchronously from the transformation budget, retained through the actual network write,
     * and released on completion, failure or cancellation.
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
             * <li>Requires and validates the original Content-Length</li>
             * <li>Acquires exact input capacity and reads directly into one array</li>
             * <li>Acquires bounded output capacity before serialization</li>
             * <li>Serializes the JSON text through the XML provider</li>
             * <li>Retains both leases until the transformed response write terminates</li>
             * </ol>
             *
             * @param body publisher emitting the original response buffers
             * @return completion of the transformed network write
             */
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                Flux<? extends DataBuffer> flux = Flux.from(body);

                getDelegate().getHeaders().setContentType(Formats.XML.getMediaType());
                return Octets.readForParsing(
                        flux,
                        Math.toIntExact(Holder.get().getMaxTransformResponseSize()),
                        Holder.transformBufferBudget(),
                        getDelegate().getHeaders().getContentLength()).flatMap(bufferedBody -> {
                            String bodyString = new String(bufferedBody.bytes(), Charset.UTF_8);
                            Provider provider = Formats.XML.getProvider();
                            return Holder.transformBufferBudget().acquire(Holder.get().getMaxTransformResponseSize())
                                    .timeout(Duration.ofSeconds(Holder.get().getBufferAcquireTimeoutSeconds()))
                                    .onErrorMap(
                                            java.util.concurrent.TimeoutException.class,
                                            error -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                                                    "Transform output capacity wait timed out", error))
                                    .flatMap(outputLease -> provider.serialize(bodyString).flatMap(xmlBody -> {
                                        String xmlString = xmlBody.toString();
                                        byte[] xmlBytes = xmlString.getBytes(Charset.UTF_8);
                                        if (xmlBytes.length > Math
                                                .toIntExact(Holder.get().getMaxTransformResponseSize())) {
                                            return Mono.error(
                                                    new DataBufferLimitException("Exceeded XML response limit of "
                                                            + Math.toIntExact(
                                                                    Holder.get().getMaxTransformResponseSize())
                                                            + " bytes"));
                                        }
                                        getDelegate().getHeaders().setContentLength(xmlBytes.length);
                                        Logger.trace(
                                                false,
                                                "Vortex",
                                                "Response formatted to XML: strategy=response, clientIp={}, xml={}",
                                                context.getX_request_ip(),
                                                xmlString);
                                        return super.writeWith(Mono.just(bufferFactory().wrap(xmlBytes)));
                                    }).doFinally(signalType -> outputLease.close()))
                                    .doFinally(signalType -> bufferedBody.close());
                        });
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
                Logger.debug(
                        true,
                        "Vortex",
                        "Binary stream response processing started: strategy=response, clientIp={}",
                        context.getX_request_ip());

                getDelegate().getHeaders().setContentType(Formats.BINARY.getMediaType());

                Flux<? extends DataBuffer> flux = Flux.from(body).doOnNext(dataBuffer -> {
                    Logger.debug(
                            true,
                            "Vortex",
                            "Binary data chunk emitted: strategy=response, clientIp={}, bytes={}",
                            context.getX_request_ip(),
                            dataBuffer.readableByteCount());
                });
                return super.writeWith(flux);
            }
        };
    }

}
