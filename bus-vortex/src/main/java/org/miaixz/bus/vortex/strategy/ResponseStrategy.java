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
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class ResponseStrategy extends AbstractStrategy {

    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            ServerWebExchange newExchange = exchange;
            final String ip = context.getX_request_ipv4();

            Logger.debug(true, "Response", "[{}] Strategy applying for format: {}", ip, context.getFormat());

            // If the request asks for XML format, apply the transformation.
            if (Formats.XML.equals(context.getFormat())) {
                Logger.debug(true, "Response", "[{}] Format is XML, applying response transformation.", ip);
                newExchange = exchange.mutate().response(processXml(exchange, context)).build();
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

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                // Convert response data stream to Flux
                Flux<? extends DataBuffer> flux = Flux.from(body);

                // Collect all data buffers (necessary for non-streaming provider.serialize())
                Mono<List<? extends DataBuffer>> collectedBuffers = flux.collectList().map(list -> list);

                // Chain the transformation
                Mono<DataBuffer> formattedBufferMono = collectedBuffers.flatMap(dataBuffers -> {
                    // 1. Synchronously merge buffers and convert to string.
                    // This is fast, in-memory work.
                    byte[] allBytes = merge(dataBuffers);
                    String bodyString = new String(allBytes, Charset.UTF_8);

                    // 2. Explicitly use XML provider and media type
                    Provider provider = Formats.XML.getProvider();

                    // 3. Call the *asynchronous* serialize method, which returns a Mono<String>
                    // and handles its own thread scheduling.
                    return provider.serialize(bodyString).map(xmlBody -> {
                        // 4. This logic now runs after the async serialization is complete
                        Logger.trace(
                                false,
                                "Response",
                                "[{}] Response formatted to XML: {}",
                                context.getX_request_ipv4(),
                                xmlBody);
                        // Wrap the formatted data into a new data buffer
                        return bufferFactory().wrap(xmlBody.getBytes(Charset.UTF_8));
                    });
                });
                // The Mono.fromCallable and .subscribeOn are no longer needed here,
                // as the Provider handles its own asynchronicity.

                // Set headers *before* writing
                getDelegate().getHeaders().setContentType(Formats.XML.getMediaType());

                // Write the formatted response. super.writeWith subscribes to the Mono.
                return super.writeWith(formattedBufferMono);
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
            // Consider releasing pooled buffers here if applicable,
            // e.g., DataBufferUtils.release(buffer);
        }

        return result;
    }

}
