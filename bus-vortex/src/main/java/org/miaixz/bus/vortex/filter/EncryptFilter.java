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
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ObjectKit;
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
 * 数据加密过滤器，负责对响应数据进行加密处理
 * <p>
 * 该过滤器在过滤器链中位于较低优先级位置（Ordered.LOWEST_PRECEDENCE - 1）， 主要用于对响应数据进行加密处理。当加密功能启用且响应格式为XML或JSON时， 过滤器会拦截响应数据，对消息中的数据进行加密处理。
 * </p>
 * <p>
 * 目前支持AES-CBC-PKCS7Padding加密方式，通过配置文件可以设置加密密钥和偏移量。 加密后的数据将替换原始数据，然后返回给客户端。
 * </p>
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class EncryptFilter extends AbstractFilter {

    /**
     * 加密配置对象，包含加密相关的配置信息
     * <p>
     * 该对象存储了加密功能是否启用、加密算法类型、加密密钥和偏移量等配置信息。 这些配置通常从应用程序的配置文件中加载，用于初始化加密实例。
     * </p>
     */
    private final Config.Encrypt encrypt;

    /**
     * 加密/解密工具实例，用于执行实际的加密操作
     * <p>
     * 该实例在初始化阶段根据配置创建，目前支持AES-CBC-PKCS7Padding加密方式。 如果加密功能未启用或配置不正确，该实例可能为null。
     * </p>
     */
    private Crypto crypto;

    /**
     * 构造器，初始化加密配置
     *
     * @param encrypt 加密配置对象，包含加密相关的配置信息
     */
    public EncryptFilter(Config.Encrypt encrypt) {
        this.encrypt = encrypt;
    }

    /**
     * 初始化方法，在 bean 创建后执行，配置 AES 加密实例
     * <p>
     * 该方法使用@PostConstruct注解，在bean实例化并完成依赖注入后由容器自动调用。 它根据加密配置创建相应的加密/解密实例，目前仅支持AES算法。
     * 如果配置中指定的是AES算法，将使用CBC模式、PKCS7Padding填充方式， 以及配置中提供的密钥和偏移量来初始化AES实例。
     * </p>
     */
    @PostConstruct
    public void init() {
        if (Algorithm.AES.getValue().equals(encrypt.getType())) {
            crypto = new AES(Algorithm.Mode.CBC, Padding.PKCS7Padding, encrypt.getKey().getBytes(),
                    encrypt.getOffset().getBytes());
        }
    }

    /**
     * 内部过滤方法，执行加密逻辑
     * <p>
     * 该方法是过滤器的核心实现，负责执行响应数据的加密处理。 它首先检查加密功能是否启用以及响应格式是否为XML或JSON， 如果满足条件，则创建一个响应装饰器来拦截并加密响应数据。
     * </p>
     * <p>
     * 加密操作通过装饰响应对象来实现，在数据写入响应之前进行加密处理。 如果加密功能未启用或响应格式不是XML或JSON，则直接跳过加密步骤。
     * </p>
     *
     * @param exchange 当前的 ServerWebExchange 对象，包含请求和响应信息
     * @param chain    过滤器链，用于将请求传递给下一个过滤器
     * @param context  请求上下文，包含请求相关的状态信息
     * @return {@link Mono<Void>} 表示异步处理完成，继续执行过滤器链
     */
    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        if (encrypt.isEnabled()
                && (Format.XML.equals(context.getFormat()) || Format.JSON.equals(context.getFormat()))) {
            exchange = exchange.mutate().response(process(exchange)).build();
        }
        return chain.filter(exchange);
    }

    /**
     * 执行加密操作，加密消息中的数据
     * <p>
     * 该方法检查消息对象中是否包含数据，如果存在数据且加密算法为AES， 则将数据转换为JSON字符串，然后使用AES加密并转换为Base64格式， 最后将加密后的数据替换原始数据。
     * </p>
     *
     * @param message 消息对象，包含待加密的数据
     */
    private void doEncrypt(Message message) {
        if (ObjectKit.isNotNull(message.getData())) {
            if (Algorithm.AES.getValue().equals(encrypt.getType())) {
                // 将数据转换为JSON字符串，然后加密并转换为Base64格式
                message.setData(crypto.encryptBase64(JsonKit.toJsonString(message.getData()), Charset.UTF_8));
            }
        }
    }

    /**
     * 创建响应装饰器，拦截并加密响应数据
     * <p>
     * 该方法创建一个ServerHttpResponseDecorator实例，用于装饰原始的响应对象。 装饰器会重写writeWith方法，在数据写入响应之前进行拦截和处理。
     * </p>
     * <p>
     * 在writeWith方法中，首先检查资产配置是否需要签名（isSign）， 如果需要，则将响应数据缓冲区连接起来，解析为消息对象， 然后调用doEncrypt方法对消息数据进行加密，最后将加密后的消息重新写入响应。
     * </p>
     *
     * @param exchange ServerWebExchange 对象，包含请求和响应信息
     * @return 装饰后的 ServerHttpResponseDecorator，用于拦截并加密响应数据
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