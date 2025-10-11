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
package org.miaixz.bus.vortex.filter;

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
import org.miaixz.bus.vortex.Formats;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Data encryption and decryption filter, responsible for decrypting request parameters and encrypting response data.
 * <p>
 * This filter is positioned at a higher priority in the filter chain (Ordered.HIGHEST_PRECEDENCE + 1). Its primary
 * purpose is to decrypt encrypted request parameters and encrypt response data.
 * </p>
 * <p>
 * **Request Processing Phase**: When decryption is enabled and the context indicates decryption is needed, the filter
 * iterates through all request parameters and decrypts non-empty parameter values. **Response Processing Phase**: When
 * encryption is enabled and the response format is XML or JSON, the filter intercepts the response data and encrypts
 * the data within the message.
 * </p>
 * <p>
 * Currently, it supports AES-CBC-PKCS7Padding encryption and decryption. The encryption key and offset can be
 * configured via configuration files.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class CipherFilter extends AbstractFilter {

    /**
     * Decryption configuration object, containing decryption-related configuration information.
     */
    private final Args.Decrypt decrypt;
    /**
     * Encryption configuration object, containing encryption-related configuration information.
     */
    private final Args.Encrypt encrypt;
    /**
     * Decryption utility instance, used to perform actual decryption operations.
     */
    private Crypto decryptCrypto;
    /**
     * Encryption utility instance, used to perform actual encryption operations.
     */
    private Crypto encryptCrypto;

    /**
     * Constructs a {@code CipherFilter} with the specified decryption and encryption configurations.
     *
     * @param decrypt The decryption configuration object.
     * @param encrypt The encryption configuration object.
     */
    public CipherFilter(Args.Decrypt decrypt, Args.Encrypt encrypt) {
        this.decrypt = decrypt;
        this.encrypt = encrypt;
    }

    /**
     * Initialization method, executed after the bean is created, to configure AES encryption and decryption instances.
     */
    @PostConstruct
    public void init() {
        // Initialize decryption instance
        if (decrypt.isEnabled() && Algorithm.AES.getValue().equals(decrypt.getType())) {
            decryptCrypto = new AES(Algorithm.Mode.CBC, Padding.PKCS7Padding, decrypt.getKey().getBytes(),
                    decrypt.getOffset().getBytes());
        }

        // Initialize encryption instance
        if (encrypt.isEnabled() && Algorithm.AES.getValue().equals(encrypt.getType())) {
            encryptCrypto = new AES(Algorithm.Mode.CBC, Padding.PKCS7Padding, encrypt.getKey().getBytes(),
                    encrypt.getOffset().getBytes());
        }
    }

    /**
     * Internal filtering method, executing the encryption and decryption logic.
     *
     * @param exchange The current {@link ServerWebExchange} object.
     * @param chain    The filter chain.
     * @param context  The request context.
     * @return {@link Mono<Void>} indicating the asynchronous completion of processing.
     */
    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        if (Consts.ONE == context.getSign()) {
            // 1. Handle request decryption
            if (decrypt.isEnabled()) {
                doDecrypt(exchange, getRequestMap(context));
                Logger.info(
                        "==>     Filter: Decryption performed for path: {}",
                        exchange.getRequest().getURI().getPath());
            }

            // 2. Handle response encryption
            if (encrypt.isEnabled()
                    && (Formats.XML.equals(context.getFormats()) || Formats.JSON.equals(context.getFormats()))) {
                exchange = exchange.mutate().response(process(exchange)).build();
            }
        }
        return chain.filter(exchange);
    }

    /**
     * Performs decryption operation, iterating through parameters and decrypting non-empty values.
     *
     * @param exchange The {@link ServerWebExchange} object.
     * @param map      The request parameter map.
     */
    private void doDecrypt(ServerWebExchange exchange, Map<String, String> map) {
        if (null == decryptCrypto) {
            Logger.warn("==>     Filter: Decrypt crypto instance not initialized");
            return;
        }

        map.forEach((k, v) -> {
            if (StringKit.isNotBlank(v)) {
                map.put(k, decryptCrypto.decryptString(v.replaceAll(Symbol.SPACE, Symbol.PLUS), Charset.UTF_8));
            }
        });
    }

    /**
     * Performs encryption operation, encrypting data within the message.
     *
     * @param message The message object containing data to be encrypted.
     */
    private void doEncrypt(Message message) {
        if (ObjectKit.isNotNull(message.getData())) {
            if (Algorithm.AES.getValue().equals(encrypt.getType())) {
                // Convert data to JSON string, then encrypt and convert to Base64 format
                message.setData(encryptCrypto.encryptBase64(JsonKit.toJsonString(message.getData()), Charset.UTF_8));
            }
        }
    }

    /**
     * Creates a response decorator to intercept and encrypt response data.
     *
     * @param exchange The {@link ServerWebExchange} object.
     * @return The decorated {@link ServerHttpResponseDecorator}.
     */
    private ServerHttpResponseDecorator process(ServerWebExchange exchange) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                // Get asset configuration, check if signing (encryption) is required
                Integer isSign = getAssets(getContext(exchange)).getSign();
                if (Consts.ONE == isSign) {
                    // Convert response data stream to Flux
                    Flux<? extends DataBuffer> flux = Flux.from(body);

                    // Collect all data buffers, then perform encryption processing
                    return flux.collectList().flatMap(dataBuffers -> {
                        // Merge all data buffers
                        byte[] allBytes = merge(dataBuffers);

                        // Convert byte array to string
                        String responseBody = new String(allBytes, Charset.UTF_8);

                        // Convert string to message object
                        Message message = JsonKit.toPojo(responseBody, Message.class);

                        // Encrypt message data
                        doEncrypt(message);

                        // Convert encrypted message to JSON string
                        String result = JsonKit.toJsonString(message);

                        // Log encryption operation
                        Logger.info(
                                "==>     Filter: Encryption performed for path: {}",
                                exchange.getRequest().getURI().getPath());

                        // Wrap encrypted data as new data buffer
                        DataBufferFactory bufferFactory = bufferFactory();
                        DataBuffer encryptedBuffer = bufferFactory.wrap(result.getBytes(Charset.UTF_8));

                        // Write encrypted response
                        return super.writeWith(Mono.just(encryptedBuffer));
                    });
                }
                // If signing (encryption) is not required, write original data directly
                return super.writeWith(body);
            }
        };
    }

    /**
     * Merges multiple data buffers into a single byte array.
     *
     * @param dataBuffers The list of data buffers.
     * @return The merged byte array.
     */
    private byte[] merge(java.util.List<? extends DataBuffer> dataBuffers) {
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
