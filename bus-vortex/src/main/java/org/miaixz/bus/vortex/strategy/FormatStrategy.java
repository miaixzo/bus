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
package org.miaixz.bus.vortex.strategy;

import java.util.List;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Formats;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A filter strategy for response formatting. This strategy ensures that if a request asks for XML, the response is
 * converted to JSON format.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 6)
public class FormatStrategy extends AbstractStrategy {

    /**
     * Internal filtering method, executing the response formatting logic.
     * <p>
     * This method logs the start of the request. If the request explicitly asks for XML format, it intercepts the
     * response and ensures it is converted to JSON format before being sent.
     * </p>
     *
     * @param exchange The current {@link ServerWebExchange} object.
     * @param chain    The filter chain.
     * @param context  The request context.
     * @return {@link Mono<Void>} indicating the asynchronous completion of processing.
     */
    @Override
    protected Mono<Void> apply(ServerWebExchange exchange, StrategyChain chain, Context context) {
        Logger.info(
                "==>     Filter: Request started - Method: {}, Path: {}, Query: {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath().value(),
                exchange.getRequest().getQueryParams());

        // If the request explicitly asks for XML format, convert to JSON
        if (Formats.XML.equals(context.getFormats())) {
            Logger.info("==>     Filter: Converting XML request to JSON response");
            exchange = exchange.mutate().response(process(exchange)).build();
        }

        return chain.apply(exchange);
    }

    /**
     * Creates a response decorator to ensure response data is in JSON format.
     * <p>
     * This method wraps the original response and overrides the method to intercept the response body. It collects all
     * data buffers, merges them, and then uses the context's specified provider to serialize the message into the
     * desired format (JSON in this case). The formatted data is then written back to the response.
     * </p>
     *
     * @param exchange The {@link ServerWebExchange} object.
     * @return The decorated {@link ServerHttpResponseDecorator}.
     */
    private ServerHttpResponseDecorator process(ServerWebExchange exchange) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {

            /**
             * Overrides the response writing logic to handle data formatting.
             * <p>
             * This method converts the response data stream to a Flux, collects all data buffers, merges them into a
             * single byte array, and then converts it to a string. It then uses the context's specified provider to
             * serialize the message. The formatted data is then wrapped into a new data buffer and written to the
             * response.
             * </p>
             *
             * @param body The response data stream.
             * @return {@link Mono<Void>} indicating the asynchronous completion of writing.
             */
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                // Convert response data stream to Flux
                Flux<? extends DataBuffer> flux = Flux.from(body);

                // Collect all data buffers
                return flux.collectList().flatMap(dataBuffers -> {
                    // Merge all data buffers
                    byte[] allBytes = merge(dataBuffers);

                    // Get the context
                    Context context = Context.get(exchange);

                    // Set the response content type to the media type specified in the context
                    exchange.getResponse().getHeaders().setContentType(context.getFormats().getMediaType());

                    // Convert byte array to string
                    String bodyString = new String(allBytes, Charset.UTF_8);

                    // Serialize the message using the provider specified in the context
                    String formatBody = context.getFormats().getProvider().serialize(bodyString);

                    // Log TRACE (if enabled)
                    Logger.trace("==>     Filter: Response formatted: {}", formatBody);

                    // Wrap the formatted data into a new data buffer
                    DataBufferFactory bufferFactory = bufferFactory();
                    DataBuffer formattedBuffer = bufferFactory.wrap(formatBody.getBytes(Charset.UTF_8));

                    // Write the formatted response
                    return super.writeWith(Mono.just(formattedBuffer));
                });
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
        // Calculate total bytes
        int totalBytes = dataBuffers.stream().mapToInt(DataBuffer::readableByteCount).sum();

        // Create result array
        byte[] result = new byte[totalBytes];

        // Fill data
        int position = 0;
        for (DataBuffer buffer : dataBuffers) {
            int length = buffer.readableByteCount();
            buffer.read(result, position, length);
            position += length;
        }

        return result;
    }

}
