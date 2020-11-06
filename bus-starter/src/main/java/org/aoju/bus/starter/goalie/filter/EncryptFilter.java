package org.aoju.bus.starter.goalie.filter;

import com.alibaba.fastjson.JSON;
import org.aoju.bus.base.consts.Consts;
import org.aoju.bus.base.entity.Message;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.goalie.reactor.ExchangeContext;
import org.aoju.bus.starter.goalie.GoalieProperties;
import org.aoju.bus.starter.goalie.ReactorConfiguration;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 加密拦截
 *
 * @author Justubborn
 * @since 2020/10/29
 */
@Component
@ConditionalOnBean(ReactorConfiguration.class)
@Order
public class EncryptFilter implements WebFilter {

    @Autowired
    GoalieProperties.Server.Encrypt encrypt;

    @Autowired
    GoalieProperties.Server.Decrypt decrypt;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerWebExchange.Builder builder = exchange.mutate();
        if (decrypt.isEnabled()) {
            doDecrypt(ExchangeContext.get(exchange).getRequestMap());
        }

        if (encrypt.isEnabled()) {
            builder.response(processResponse(exchange));
        }


        return chain.filter(builder.build());
    }

    /**
     * 解密
     *
     * @param multiValueMap 参数
     */
    private void doDecrypt(MultiValueMap<String, String> multiValueMap) {
        multiValueMap.forEach((k, list) -> multiValueMap.addAll(k, list.stream().map(v -> org.aoju.bus.crypto.Builder.decrypt(encrypt.getType(), encrypt.getKey(), v, Charset.UTF_8)).collect(Collectors.toList())));
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

    private ServerHttpResponseDecorator processResponse(ServerWebExchange exchange) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                boolean isSign = Consts.STATUS_ONE.equals(ExchangeContext.get(exchange).getAsset().getSign());
                if (isSign && body instanceof Mono) {
                    Mono<? extends DataBuffer> mono = (Mono<? extends DataBuffer>) body;
                    return super.writeWith(mono.map(dataBuffer -> {
                        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer());
                        DataBufferUtils.release(dataBuffer);
                        Message message = JSON.parseObject(charBuffer.toString(), Message.class);
                        doEncrypt(message);
                        return bufferFactory().wrap(JSON.toJSONString(message).getBytes());
                    }));
                }
                return super.writeWith(body);
            }
        };
    }

}
