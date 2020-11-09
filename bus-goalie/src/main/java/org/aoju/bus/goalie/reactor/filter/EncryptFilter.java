package org.aoju.bus.goalie.reactor.filter;

import com.alibaba.fastjson.JSON;
import org.aoju.bus.base.entity.Message;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.goalie.reactor.ExchangeContext;
import org.aoju.bus.starter.goalie.GoalieProperties;
import org.aoju.bus.starter.goalie.ReactorConfiguration;
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
@ConditionalOnBean(ReactorConfiguration.class)
@Order(FilterOrders.ENCRYPT)
public class EncryptFilter implements WebFilter {

  @Autowired
  GoalieProperties.Server.Encrypt encrypt;

  @Autowired
  GoalieProperties.Server.Decrypt decrypt;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {


    Mono<Void> mono = chain.filter(exchange);
    if (encrypt.isEnabled()) {
      return mono.then(Mono.defer(() -> {
        boolean isSign = ExchangeContext.get(exchange).getAsset().isSign();
        Flux<DataBuffer> body = ExchangeContext.get(exchange).getBody();
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

//  private ServerHttpResponseDecorator processResponse(ServerWebExchange exchange) {
//    return new ServerHttpResponseDecorator(exchange.getResponse()) {
//      @Override
//      public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
//        boolean isSign = ExchangeContext.get(exchange).getAsset().isSign();
//        if (isSign && body instanceof Mono) {
//          Mono<? extends DataBuffer> mono = (Mono<? extends DataBuffer>) body;
//          return super.writeWith(mono.map(dataBuffer -> {
//            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer());
//            DataBufferUtils.release(dataBuffer);
//            Message message = JSON.parseObject(charBuffer.toString(), Message.class);
//            doEncrypt(message);
//            return bufferFactory().wrap(JSON.toJSONString(message).getBytes());
//          }));
//        }
//        return super.writeWith(body);
//      }
//    };
//  }

}
