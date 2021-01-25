/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.goalie.filter;

import org.aoju.bus.base.entity.Message;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.crypto.Mode;
import org.aoju.bus.crypto.Padding;
import org.aoju.bus.crypto.symmetric.AES;
import org.aoju.bus.crypto.symmetric.Symmetric;
import org.aoju.bus.extra.json.JsonKit;
import org.aoju.bus.goalie.Context;
import org.aoju.bus.goalie.ServerConfig;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 数据加密
 *
 * @author Justubborn
 * @version 6.1.9
 * @since JDK 1.8+
 */
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class EncryptFilter implements WebFilter {

    ServerConfig.Encrypt encrypt;

    private Symmetric symmetric;

    public EncryptFilter(ServerConfig.Encrypt encrypt) {
        this.encrypt = encrypt;
    }

    @PostConstruct
    public void init() {
        if ("AES".equals(encrypt.getType())) {
            symmetric = new AES(Mode.CBC, Padding.PKCS7Padding, encrypt.getKey().getBytes(), encrypt.getOffset().getBytes());
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        if (encrypt.isEnabled()
                && (Context.Format.xml.equals(Context.get(exchange).getFormat())
                || Context.Format.json.equals(Context.get(exchange).getFormat()))) {
            exchange = exchange.mutate().response(process(exchange)).build();
        }
        return chain.filter(exchange);
    }

    /**
     * 加密
     *
     * @param message 消息
     */
    private void doEncrypt(Message message) {
        if (ObjectKit.isNotNull(message.getData())) {
            if ("AES".equals(encrypt.getType())) {
                message.setData(symmetric.encryptBase64(JsonKit.toJsonString(message.getData()), Charset.UTF_8));
            }

        }
    }

    private ServerHttpResponseDecorator process(ServerWebExchange exchange) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                boolean isSign = Context.get(exchange).getAssets().isSign();
                if (isSign) {
                    Flux<? extends DataBuffer> flux = Flux.from(body);
                    return super.writeWith(DataBufferUtils.join(flux).map(dataBuffer -> {
                        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer());
                        DataBufferUtils.release(dataBuffer);
                        Message message = JsonKit.toPojo(charBuffer.toString(), Message.class);
                        doEncrypt(message);
                        return bufferFactory().wrap(JsonKit.toJsonString(message).getBytes());
                    }));
                }
                return super.writeWith(body);
            }
        };
    }

}
