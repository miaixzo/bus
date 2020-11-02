package org.aoju.bus.goalie.reactor;

import lombok.Data;
import org.aoju.bus.base.entity.Message;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

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
    public final static String $ = "exchange_context";
    /**
     * 请求参数
     */
    private MultiValueMap<String, String> requestMap;

    /**
     * 返回消息
     */
    private Message responseMsg;

    public static Message getMessage(ServerWebExchange exchange) {
       return exchange.getAttribute(ExchangeContext.$);
    }

    public static void setMessage() {

    }

}
