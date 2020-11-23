/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.starter.goalie.filter;

import com.alibaba.fastjson.JSON;
import org.aoju.bus.base.entity.Message;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.goalie.Context;
import org.aoju.bus.starter.goalie.GoalieConfiguration;
import org.aoju.bus.starter.goalie.GoalieProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 加密拦截
 *
 * @author Justubborn
 * @since 2020/10/29
 */
@Component
@ConditionalOnBean(GoalieConfiguration.class)
@Order(FilterOrders.ENCRYPT)
public class EncryptFilter implements WebFilter {

    @Autowired
    GoalieProperties.Server.Encrypt encrypt;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {


        Mono<Void> mono = chain.filter(exchange);
        if (encrypt.isEnabled()) {
            return mono.then(Mono.defer(() -> {
                boolean isSign = Context.get(exchange).getAssets().isSign();
                Flux<DataBuffer> body = Context.get(exchange).getBody();
                return Mono.from(body).flatMap(dataBuffer -> {
                    if (isSign && dataBuffer instanceof Message) {
                        Message message = (Message) dataBuffer;
                        doEncrypt(message);
                        ServerHttpResponse response = exchange.getResponse();

                        return response
                                .writeWith(Mono.just(response.bufferFactory().wrap(JSON.toJSONString(message).getBytes())));
                    }
                    return mono;
                });

            }));
        }
        return mono;
    }

    /**
     * 加密
     *
     * @param message 消息
     */
    private void doEncrypt(Message message) {
        if (ObjectKit.isNotNull(message.getData())) {
            message.setData(org.aoju.bus.crypto.Builder.encrypt(encrypt.getType(), encrypt.getKey(), JSON.toJSONString(message.getData()), Charset.UTF_8));
        }
    }

}
