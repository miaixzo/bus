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
 * A filter strategy for data encryption and decryption.
 * <p>
 * This strategy is responsible for two main tasks:
 * <ol>
 * <li><b>Request Decryption:</b> It inspects the incoming request parameters and, if decryption is enabled and required
 * by the service, decrypts the parameter values.</li>
 * <li><b>Response Encryption:</b> It intercepts the outgoing response and, if encryption is enabled, encrypts the
 * response body before it is sent to the client.</li>
 * </ol>
 * Its order is set to run after {@link RequestStrategy} to ensure the request context and parameters are available, but
 * before strategies like {@link AuthorizeStrategy} that may need to inspect the decrypted data.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class CipherStrategy extends AbstractStrategy {

    private final Args.Decrypt decryptConfig;
    private final Args.Encrypt encryptConfig;
    private Crypto decryptCrypto;
    private Crypto encryptCrypto;

    /**
     * Constructs a new CipherStrategy.
     *
     * @param decryptConfig The configuration for decryption, typically from application properties.
     * @param encryptConfig The configuration for encryption, typically from application properties.
     */
    public CipherStrategy(Args.Decrypt decryptConfig, Args.Encrypt encryptConfig) {
        this.decryptConfig = decryptConfig;
        this.encryptConfig = encryptConfig;
    }

    /**
     * Initializes AES crypto instances based on the provided configuration after bean construction.
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
     * Applies the decryption and encryption logic. It decides whether to act based on the {@code sign} flag in the
     * request context.
     *
     * @param exchange The current server exchange.
     * @param chain    The chain of remaining strategies.
     * @return A {@code Mono<Void>} signaling completion.
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, StrategyChain chain, Context context) {
        ServerWebExchange mutatedExchange = exchange;
        // The 'sign' flag is used to indicate if encryption/decryption is active for this request.
        if (context.getAssets() != null && Consts.ONE == context.getAssets().getSign()) {
            // 1. Handle request decryption if enabled.
            if (decryptConfig != null && decryptConfig.isEnabled()) {
                doDecrypt(context.getRequestMap());
                Logger.info(
                        "==> Strategy: Decryption performed for path: {}",
                        exchange.getRequest().getURI().getPath());
            }

            // 2. Decorate the response for encryption if enabled.
            if (encryptConfig != null && encryptConfig.isEnabled()) {
                mutatedExchange = exchange.mutate().response(decorateResponse(exchange)).build();
            }
        }
        return chain.apply(mutatedExchange);
    }

    /**
     * Performs in-place decryption of the request parameter map.
     *
     * @param requestMap The map of request parameters from the context.
     */
    private void doDecrypt(Map<String, String> requestMap) {
        if (null == decryptCrypto) {
            Logger.warn("==> Strategy: Decrypt crypto instance not initialized, skipping decryption.");
            return;
        }
        requestMap.forEach((k, v) -> {
            if (StringKit.isNotBlank(v)) {
                requestMap.put(k, decryptCrypto.decryptString(v.replaceAll(Symbol.SPACE, Symbol.PLUS), Charset.UTF_8));
            }
        });
    }

    /**
     * Performs in-place encryption of the data within the response message object.
     *
     * @param message The response message object.
     */
    private void doEncrypt(Message message) {
        if (ObjectKit.isNotNull(message.getData()) && null != encryptCrypto) {
            if (Algorithm.AES.getValue().equals(encryptConfig.getType())) {
                message.setData(encryptCrypto.encryptBase64(JsonKit.toJsonString(message.getData()), Charset.UTF_8));
            }
        }
    }

    /**
     * Creates a response decorator to intercept and encrypt the response body before it is written.
     *
     * @param exchange The current server exchange.
     * @return The decorated ServerHttpResponse.
     */
    private ServerHttpResponseDecorator decorateResponse(ServerWebExchange exchange) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {

            /**
             * Intercepts the response body publisher, buffers the data, encrypts it, and writes the new body.
             */
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                Context context = getContext(exchange);
                // Double-check the sign flag in case the context has changed.
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
                // If encryption is not required, write the original body.
                return super.writeWith(body);
            }
        };
    }

    /**
     * Helper method to merge a list of DataBuffers into a single byte array.
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
