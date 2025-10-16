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
import java.util.Map;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Padding;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;
import org.miaixz.bus.crypto.center.AES;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A strategy for data encryption and decryption, acting as a security layer in the processing chain.
 * <p>
 * This strategy is conditionally activated based on the API asset's configuration. Its primary responsibilities are:
 * <ol>
 * <li><b>Response Encryption:</b> For the same APIs, it intercepts the outgoing response and encrypts the main data
 * payload before sending it to the client.</li>
 * </ol>
 * It is ordered to run after {@link RequestStrategy} (to ensure parameters are available for decryption) but before
 * {@link AuthorizeStrategy} (to allow authorization logic to operate on decrypted, plaintext data).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class CipherStrategy extends AbstractStrategy {

    /**
     * Configuration for request decryption, injected from application properties.
     */
    private final Args.Decrypt decryptConfig;
    /**
     * Configuration for response encryption, injected from application properties.
     */
    private final Args.Encrypt encryptConfig;
    /**
     * A reusable crypto instance for decryption, initialized at startup.
     */
    private Crypto decryptCrypto;
    /**
     * A reusable crypto instance for encryption, initialized at startup.
     */
    private Crypto encryptCrypto;

    /**
     * Constructs a new {@code CipherStrategy}.
     *
     * @param decryptConfig The configuration for decryption.
     * @param encryptConfig The configuration for encryption.
     */
    public CipherStrategy(Args.Decrypt decryptConfig, Args.Encrypt encryptConfig) {
        this.decryptConfig = decryptConfig;
        this.encryptConfig = encryptConfig;
    }

    /**
     * Initializes the AES crypto instances based on the provided configuration. This method is automatically called by
     * the Spring container after the bean has been constructed.
     */
    @PostConstruct
    public void init() {
        if (decryptConfig != null && decryptConfig.isEnabled()
                && Algorithm.AES.getValue().equals(decryptConfig.getType())) {
            decryptCrypto = new AES(Algorithm.Mode.CBC, Padding.PKCS7Padding, decryptConfig.getKey().getBytes(),
                    decryptConfig.getOffset().getBytes());
        }
        if (encryptConfig != null && encryptConfig.isEnabled()
                && Algorithm.AES.getValue().equals(encryptConfig.getType())) {
            encryptCrypto = new AES(Algorithm.Mode.CBC, Padding.PKCS7Padding, encryptConfig.getKey().getBytes(),
                    encryptConfig.getOffset().getBytes());
        }
    }

    /**
     * Applies decryption and/or encryption logic based on the API's configuration.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @return A {@code Mono<Void>} that signals the completion of this strategy.
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, StrategyChain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            ServerWebExchange newExchange = exchange;

            // The 'sign' flag, derived from the API asset, indicates if encryption/decryption is active for this
            // request.
            if (context.getAssets() != null && Consts.ONE == context.getAssets().getSign()) {
                // 1. Handle request parameter decryption if enabled.
                if (decryptConfig != null && decryptConfig.isEnabled()) {
                    doDecrypt(context.getParameters());
                    Logger.info(
                            "==> Strategy: Decryption performed for path: {}",
                            exchange.getRequest().getURI().getPath());
                }

                // 2. Decorate the response to enable response body encryption if enabled.
                if (encryptConfig != null && encryptConfig.isEnabled()) {
                    newExchange = exchange.mutate().response(decorateResponse(exchange, context)).build();
                }
            }
            return chain.apply(newExchange);
        });
    }

    /**
     * Performs in-place decryption of the request parameter values.
     *
     * @param parameters The map of request parameters from the {@link Context}.
     */
    private void doDecrypt(Map<String, String> parameters) {
        if (null == decryptCrypto) {
            Logger.warn("==> Strategy: Decrypt crypto instance not initialized, skipping decryption.");
            return;
        }
        parameters.forEach((k, v) -> {
            if (StringKit.isNotBlank(v)) {
                parameters.put(k, decryptCrypto.decryptString(v.replaceAll(Symbol.SPACE, Symbol.PLUS), Charset.UTF_8));
            }
        });
    }

    /**
     * Performs in-place encryption of the {@code data} field within the response {@link Message} object.
     *
     * @param message The response message object, which will be mutated.
     */
    private void doEncrypt(Message message) {
        if (ObjectKit.isNotNull(message.getData()) && null != encryptCrypto) {
            if (Algorithm.AES.getValue().equals(encryptConfig.getType())) {
                message.setData(encryptCrypto.encryptBase64(JsonKit.toJsonString(message.getData()), Charset.UTF_8));
            }
        }
    }

    /**
     * Creates a response decorator to intercept and encrypt the response body before it is written to the client.
     *
     * @param exchange The current server exchange.
     * @param context  The request context, needed to check the encryption flag again inside the decorator.
     * @return The decorated {@link ServerHttpResponseDecorator}.
     */
    private ServerHttpResponseDecorator decorateResponse(ServerWebExchange exchange, Context context) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {

            /**
             * Intercepts the response body publisher to perform encryption.
             * <p>
             * This method implements the core reactive logic for response encryption:
             * <ol>
             * <li>It subscribes to the original response body publisher ({@code body}).</li>
             * <li>It uses {@code .collectList()} to buffer all data chunks of the response into memory.</li>
             * <li>Once the full body is received, it deserializes the JSON into a {@link Message} object.</li>
             * <li>It serializes the modified {@code Message} object back into a JSON string.</li>
             * <li>Finally, it wraps the new encrypted string in a new {@code DataBuffer} and passes it to the original
             * {@code writeWith} method to be sent to the client.</li>
             * </ol>
             *
             * @param body The original (plaintext) response body publisher.
             * @return A {@code Mono<Void>} that signals the completion of the write operation.
             */
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                // Double-check the sign flag. This is a safeguard, as the decorator is only applied if signing is
                // active,
                // but it ensures correctness if the context were to change unexpectedly.
                if (context.getAssets() != null && Consts.ONE == context.getAssets().getSign()) {
                    Flux<? extends DataBuffer> flux = Flux.from(body);
                    return flux.collectList().flatMap(dataBuffers -> {
                        byte[] allBytes = merge(dataBuffers);
                        String responseBody = new String(allBytes, Charset.UTF_8);
                        Message message = JsonKit.toPojo(responseBody, Message.class);
                        doEncrypt(message);
                        String result = JsonKit.toJsonString(message);
                        Logger.info(
                                "==> Strategy: Encryption performed for path: {}",
                                exchange.getRequest().getURI().getPath());
                        DataBufferFactory bufferFactory = bufferFactory();
                        DataBuffer encryptedBuffer = bufferFactory.wrap(result.getBytes(Charset.UTF_8));
                        return super.writeWith(Mono.just(encryptedBuffer));
                    });
                }
                // If encryption is not required for some reason, write the original body.
                return super.writeWith(body);
            }
        };
    }

    /**
     * A utility method to merge a list of {@link DataBuffer}s into a single byte array.
     *
     * @param dataBuffers The list of data buffers to merge.
     * @return A single byte array containing the merged data.
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
