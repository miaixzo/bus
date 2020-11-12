package org.aoju.bus.goalie.reactor;

import lombok.Data;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Optional;

/**
 * 上下文传参
 *
 * @author Justubborn
 * @since 2020/10/30
 */
@Data
public class ExchangeContext {

    /**
     * 交换内容
     */
    private final static String $ = "exchange_context";

    /**
     * 请求参数
     */
    private Map<String, String> requestMap;

    /**
     * 返回body
     */
    private Flux<DataBuffer> body;

    private Asset asset;

    public static ExchangeContext get(ServerWebExchange exchange) {
        ExchangeContext context = exchange.getAttribute(ExchangeContext.$);

        return Optional.ofNullable(context).orElseGet(() -> {
            ExchangeContext empty = new ExchangeContext();
            exchange.getAttributes().put(ExchangeContext.$, empty);
            return empty;
        });
    }

    public static ExchangeContext get(ServerRequest request) {
        return (ExchangeContext) request.attribute(ExchangeContext.$).orElseGet(() -> {
            ExchangeContext empty = new ExchangeContext();
            request.attributes().put(ExchangeContext.$, empty);
            return empty;
        });
    }

}
