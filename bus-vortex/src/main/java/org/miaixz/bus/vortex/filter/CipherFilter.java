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

import java.nio.CharBuffer;
import java.util.Map;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.vortex.Config;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Format;
import org.miaixz.bus.crypto.Padding;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;
import org.miaixz.bus.crypto.center.AES;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 数据加解密过滤器，负责对请求参数进行解密和对响应数据进行加密处理
 * <p>
 * 该过滤器在过滤器链中位于较高优先级位置（Ordered.HIGHEST_PRECEDENCE + 1）， 主要用于对加密的请求参数进行解密操作，以及对响应数据进行加密处理。
 * </p>
 * <p>
 * 请求处理阶段：当解密功能启用且上下文标记需要解密时，过滤器会遍历所有请求参数，对非空参数值进行解密处理。 响应处理阶段：当加密功能启用且响应格式为XML或JSON时，过滤器会拦截响应数据，对消息中的数据进行加密处理。
 * </p>
 * <p>
 * 目前支持AES-CBC-PKCS7Padding加解密方式，通过配置文件可以设置加解密密钥和偏移量。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class CipherFilter extends AbstractFilter {

    /**
     * 解密配置对象，包含解密相关的配置信息
     */
    private final Config.Decrypt decrypt;
    /**
     * 加密配置对象，包含加密相关的配置信息
     */
    private final Config.Encrypt encrypt;
    /**
     * 解密工具实例，用于执行实际的解密操作
     */
    private Crypto decryptCrypto;
    /**
     * 加密工具实例，用于执行实际的加密操作
     */
    private Crypto encryptCrypto;

    /**
     * 构造器，初始化加解密配置
     *
     * @param decrypt 解密配置对象
     * @param encrypt 加密配置对象
     */
    public CipherFilter(Config.Decrypt decrypt, Config.Encrypt encrypt) {
        this.decrypt = decrypt;
        this.encrypt = encrypt;
    }

    /**
     * 初始化方法，在 bean 创建后执行，配置 AES 加解密实例
     */
    @PostConstruct
    public void init() {
        // 初始化解密实例
        if (decrypt.isEnabled() && Algorithm.AES.getValue().equals(decrypt.getType())) {
            decryptCrypto = new AES(Algorithm.Mode.CBC, Padding.PKCS7Padding, decrypt.getKey().getBytes(),
                    decrypt.getOffset().getBytes());
        }

        // 初始化加密实例
        if (encrypt.isEnabled() && Algorithm.AES.getValue().equals(encrypt.getType())) {
            encryptCrypto = new AES(Algorithm.Mode.CBC, Padding.PKCS7Padding, encrypt.getKey().getBytes(),
                    encrypt.getOffset().getBytes());
        }
    }

    /**
     * 内部过滤方法，执行加解密逻辑
     *
     * @param exchange 当前的 ServerWebExchange 对象
     * @param chain    过滤器链
     * @param context  请求上下文
     * @return {@link Mono<Void>} 表示异步处理完成
     */
    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        // 1. 处理请求解密
        if (decrypt.isEnabled() && context.isNeedDecrypt()) {
            doDecrypt(exchange, getRequestMap(context));
            Format.info(exchange, "DECRYPT_PERFORMED", "Path: " + exchange.getRequest().getURI().getPath());
        } else {
            // 2. 处理响应加密
            if (encrypt.isEnabled()
                    && (Format.XML.equals(context.getFormat()) || Format.JSON.equals(context.getFormat()))) {
                exchange = exchange.mutate().response(process(exchange)).build();
            }
        }
        return chain.filter(exchange);
    }

    /**
     * 执行解密操作，遍历参数并解密非空值
     *
     * @param exchange ServerWebExchange 对象
     * @param map      请求参数映射
     */
    private void doDecrypt(ServerWebExchange exchange, Map<String, String> map) {
        if (null == decryptCrypto) {
            Format.warn(exchange, "DECRYPT_SKIPPED", "Decrypt crypto instance not initialized");
            return;
        }

        map.forEach((k, v) -> {
            if (StringKit.isNotBlank(v)) {
                map.put(k, decryptCrypto.decryptString(v.replaceAll(Symbol.SPACE, Symbol.PLUS), Charset.UTF_8));
            }
        });
    }

    /**
     * 执行加密操作，加密消息中的数据
     *
     * @param message 消息对象
     */
    private void doEncrypt(Message message) {
        if (ObjectKit.isNotNull(message.getData())) {
            if (Algorithm.AES.getValue().equals(encrypt.getType())) {
                // 将数据转换为JSON字符串，然后加密并转换为Base64格式
                message.setData(encryptCrypto.encryptBase64(JsonKit.toJsonString(message.getData()), Charset.UTF_8));
            }
        }
    }

    /**
     * 创建响应装饰器，拦截并加密响应数据
     *
     * @param exchange ServerWebExchange 对象
     * @return 装饰后的 ServerHttpResponseDecorator
     */
    private ServerHttpResponseDecorator process(ServerWebExchange exchange) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                // 获取资产配置，检查是否需要签名（加密）
                boolean isSign = getAssets(getContext(exchange)).isSign();
                if (isSign) {
                    // 将响应数据流转换为Flux
                    Flux<? extends DataBuffer> flux = Flux.from(body);
                    // 连接所有数据缓冲区，然后进行加密处理
                    return super.writeWith(DataBufferUtils.join(flux).map(dataBuffer -> {
                        // 将数据缓冲区解码为字符缓冲区
                        CharBuffer charBuffer = Charset.UTF_8.decode(dataBuffer.asByteBuffer());
                        // 释放原始数据缓冲区
                        DataBufferUtils.release(dataBuffer);
                        // 将字符缓冲区转换为消息对象
                        Message message = JsonKit.toPojo(charBuffer.toString(), Message.class);
                        // 加密消息数据
                        doEncrypt(message);
                        // 将加密后的消息转换为JSON字符串
                        String result = JsonKit.toJsonString(message);
                        // 记录加密操作日志
                        Format.info(exchange, "ENCRYPT_PERFORMED", "Path: " + exchange.getRequest().getURI().getPath());
                        // 将加密后的数据包装为新的数据缓冲区并返回
                        return bufferFactory().wrap(result.getBytes());
                    }));
                }
                // 如果不需要签名（加密），直接写入原始数据
                return super.writeWith(body);
            }
        };
    }

}