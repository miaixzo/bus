package org.aoju.bus.starter.goalie.filter;

import com.alibaba.fastjson.JSON;
import org.aoju.bus.base.entity.Message;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.goalie.reactor.ExchangeContext;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.starter.goalie.GoalieProperties;
import org.aoju.bus.starter.goalie.ReactorConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

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

        if (decrypt.isEnabled()) {
            Object contextObj = exchange.getAttributes().get(ExchangeContext.$);
            if (contextObj instanceof ExchangeContext) {
                ExchangeContext context = (ExchangeContext) contextObj;
                doDecrypt(context.getRequestMap());
            }

        }

        if (encrypt.isEnabled()) {
            exchange.getResponse().beforeCommit(() -> {
                ServerHttpResponse response = exchange.getResponse();
                Object contextObj = exchange.getAttributes().get(ExchangeContext.$);
                if (contextObj instanceof ExchangeContext) {

                    ExchangeContext context = (ExchangeContext) contextObj;
                    Message message = context.getResponseMsg();
                    doEncrypt(message);
                    response.writeWith(Mono.just(response.bufferFactory().wrap(JSON.toJSONString(message).getBytes())));
                }
                return Mono.empty();
            });
        }


        return chain.filter(exchange);
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
        if (StringKit.isBlank((CharSequence) message.getData())) return;
        message.setData(org.aoju.bus.crypto.Builder.encrypt(encrypt.getType(), encrypt.getKey(), JSON.toJSONString(message.getData()), Charset.UTF_8));
        Logger.info("我加密了:{}", message);
    }


}
