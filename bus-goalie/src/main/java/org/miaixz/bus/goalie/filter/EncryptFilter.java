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
package org.miaixz.bus.goalie.filter;

import java.nio.CharBuffer;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.crypto.Padding;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;
import org.miaixz.bus.crypto.center.AES;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.goalie.Config;
import org.miaixz.bus.goalie.Context;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 数据加密过滤器，负责对响应数据进行加密处理
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class EncryptFilter implements WebFilter {

    /**
     * 加密配置，包含是否启用、密钥、算法类型和偏移量
     */
    private final Config.Encrypt encrypt;

    /**
     * 对称加密实例，用于执行加密操作
     */
    private Crypto crypto;

    /**
     * 构造器，初始化加密配置
     *
     * @param encrypt 加密配置对象
     */
    public EncryptFilter(Config.Encrypt encrypt) {
        this.encrypt = encrypt;
    }

    /**
     * 初始化方法，在 bean 创建后执行，配置 AES 加密实例
     */
    @PostConstruct
    public void init() {
        if (Algorithm.AES.getValue().equals(encrypt.getType())) {
            // 使用 AES 算法，CBC 模式，PKCS7 填充
            crypto = new AES(Algorithm.Mode.CBC, Padding.PKCS7Padding, encrypt.getKey().getBytes(),
                    encrypt.getOffset().getBytes());
        }
    }

    /**
     * 过滤器主逻辑，检查是否需要加密并装饰响应
     *
     * @param exchange 当前的 ServerWebExchange 对象，包含请求和响应
     * @param chain    过滤器链，用于继续处理请求
     * @return {@link Mono<Void>} 表示异步处理完成
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 检查是否启用加密且响应格式为 XML 或 JSON
        if (encrypt.isEnabled() && (Context.Format.xml.equals(Context.get(exchange).getFormat())
                || Context.Format.json.equals(Context.get(exchange).getFormat()))) {
            // 装饰响应以支持加密
            exchange = exchange.mutate().response(process(exchange)).build();
        }
        // 继续执行过滤器链
        return chain.filter(exchange);
    }

    /**
     * 执行加密操作，加密消息中的数据
     *
     * @param message 消息对象，包含待加密的数据
     */
    private void doEncrypt(Message message) {
        if (ObjectKit.isNotNull(message.getData())) {
            if (Algorithm.AES.getValue().equals(encrypt.getType())) {
                // 将数据序列化为 JSON 后进行 AES 加密，并转换为 Base64
                message.setData(crypto.encryptBase64(JsonKit.toJsonString(message.getData()), Charset.UTF_8));
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
            /**
             * 重写响应写入逻辑，处理数据加密
             *
             * @param body 响应数据流
             * @return {@link Mono<Void>} 表示异步写入完成
             */
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                boolean isSign = Context.get(exchange).getAssets().isSign();
                if (isSign) {
                    // 将响应数据流合并为单个缓冲区
                    Flux<? extends DataBuffer> flux = Flux.from(body);
                    return super.writeWith(DataBufferUtils.join(flux).map(dataBuffer -> {
                        // 将缓冲区解码为字符串
                        CharBuffer charBuffer = Charset.UTF_8.decode(dataBuffer.asByteBuffer());
                        DataBufferUtils.release(dataBuffer); // 释放缓冲区
                        // 解析为 Message 对象
                        Message message = JsonKit.toPojo(charBuffer.toString(), Message.class);
                        // 加密消息数据
                        doEncrypt(message);
                        // 将加密后的消息序列化为 JSON 并写入新缓冲区
                        return bufferFactory().wrap(JsonKit.toJsonString(message).getBytes());
                    }));
                }
                // 未要求加密，直接写入原始数据
                return super.writeWith(body);
            }
        };
    }

}