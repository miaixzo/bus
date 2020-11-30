package org.aoju.bus.starter.goalie.filter;

import org.aoju.bus.base.entity.Message;
import org.aoju.bus.extra.json.JsonKit;
import org.aoju.bus.goalie.Context;
import org.aoju.bus.starter.goalie.GoalieConfiguration;
import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

/**
 * 格式化
 *
 * @author Justubborn
 * @since 2020/11/26
 */
@Component
@ConditionalOnBean(GoalieConfiguration.class)
@Order(Ordered.LOWEST_PRECEDENCE - 2)
public class FormatFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Context context = Context.get(exchange);
        if (!Context.Format.binary.equals(context.getFormat())) {
            exchange = exchange.mutate().response(process(exchange)).build();
        }
        return chain.filter(exchange);

    }

    private ServerHttpResponseDecorator process(ServerWebExchange exchange) {
        Context context = Context.get(exchange);
        return new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                Flux<? extends DataBuffer> flux = Flux.from(body);
                return super.writeWith(DataBufferUtils.join(flux).map(dataBuffer -> {
                    exchange.getResponse().getHeaders().setContentType(context.getFormat().getMediaType());

                    String bodyString = Charset.defaultCharset().decode(dataBuffer.asByteBuffer()).toString();
                    Message message = JsonKit.toPojo(bodyString,Message.class);
                    String formatBody = context.getFormat().getProvider().serialize(message);
                    return bufferFactory().wrap(formatBody.getBytes());
                }));
            }
        };
    }
}
