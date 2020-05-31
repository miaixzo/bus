# Netty
## 请求数据格式
```
{
  "e": "事件类型",
  "t": ["主题信息"],
  "d": "自定义数据"
}
```
- e:event, t:topic, d:data
- 事件类型目前支持：subscribe(订阅), message(通信), cancel(取消订阅), heartbeat(心跳，由系统自动发送)
- 除心跳事件外，topic为必填项，可同时发送多个topic
- 响应数据格式无要求，可自定义返回

## 心跳事件
- 当客户端和服务端在1分钟内无数据交互时，服务端会发送心跳事件，数据格式如下：
```
{
  "e": "heartbeat",
  "d": "ping"
}
```
- 客户端收到心跳数据时，请发送以下数据进行响应：
```
{
  "e": "heartbeat",
  "d": "pong"
}
```
- 若服务端发送2次心跳事件仍无响应时，会断开连接

## 快速开始

- 定义每个topic的事件处理器，返回值是对客户端的响应数据，返回值为空则不响应

对于确定的topic，可实现WebSocketEventHandler事件处理器，通过注解管理topic
```
@Websocket("test")
public class SampleMessageEventHandler implements EventHandler {
    
    @Override
    public String onSubscribe(String topic, String data) {
        return "subscribe success!";
    }

    @Override
    public String onMessage(String topic, String data) {
        return "message received!";
    }

    @Override
    public String onCancel(String topic, String data) {
        return "cancel success!";
    }
}
```
对应动态的topic，可实现CustomizeEventHandler事件处理器，通过equalsTopic管理topic
```
@WebsocketListener
public class SampleMessageCustomizeEventHandler implements CustomizeEventHandler {
    @Override
    public boolean equalsTopic(String topic) {
        return "test2".equals(topic);
    }

    @Override
    public String onSubscribe(String topic, String data) {
        return "subscribe success!";
    }

    @Override
    public String onMessage(String topic, String data) {
        return "message received!";
    }

    @Override
    public String onCancel(String topic, String data) {
        return "cancel success!";
    }
}
```

- 配置扫描路径
```
@EnableWebSocket("org.aoju.websocket.**")
public class WebSocketApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(WebSocketApplication.class).run(args);
    }
}
```

- 配置参数
```yaml
net:
  websocket:
    # 监听端口
    port: 80
    # 监听线程数，默认1个线程
    boss-group-threads: 1
    # 工作线程数，默认0为CPU核心数
    worker-group-threads: 0
    # 请求路径
    end-point: /ws
```
port: 监听端口

- 发送消息

使用MessagePublisher的publish，有两个参数：topic: 主题信息; message: 消息内容
```
public class SendMessageHandler {
    
    public static void send(String topic, String message) {
        MessagePublisher.publish(topic, message);
    }
    
}
```


# Spring

## 介绍
基于spring框架的WebSocket扩展，支持细粒度控制
## StompSubProtocolHandler 阅读源码可以发现spring原生的消息处理类不支持自定义拦截器

## interceptable-websocket
做这个组件是为了细粒度的动态控制WebSocket的权限，项目对StompSubProtocolHandler类和其他相关的类做了扩展，增加了对自定义拦截器的支持<br/>
具体实现在extension包，拦截器实现在interceptor包
### 使用方法
 
```
配置类：
```
package org.aoju.bus.socket.xxxx;

  // 增加注解
@EnableMessageBroker
public class SecurityWebSocketConfig extends AbstractMessageBrokerConfigurer {
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/endpoint")
                // 建立握手前将httpSession中的信息保存到webSocketSession，本案例将用户登录信息保存在httpSession
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                // 如果需要跨域连接webSocket，将这一行注解打开，参数可以指定域名
                // .setAllowedOrigins("*")
                .withSockJS();
        registry.setErrorHandler(stompSubProtocolErrorHandler());
        // 注册拦截器
        registry.addFromClientInterceptor(accessDecisionFromClientInterceptor()) // 消息授权决策
                .addFromClientInterceptor(sessionIdUnRegistryInterceptor()) // sessionId记录
                .addToClientInterceptor(sessionIdRegistryInterceptor()); // sessionId移除
    }
    
    // 异常处理器
    @Bean
    public StompSubProtocolErrorHandler stompSubProtocolErrorHandler() {
        return new StompSubProtocolErrorHandler();
    }

    // 授权决策拦截器
    @Bean
    public FromClientInterceptor accessDecisionFromClientInterceptor() {
        // 实现 org.aoju.bus.socket.spring.interceptor.FromClientInterceptor
        // #preHandle
        return null;
    }

    // sessionId登记拦截器
    @Bean
    public ToClientInterceptor sessionIdRegistryInterceptor() {
        // 实现 org.aoju.bus.socket.spring.interceptor.FromClientInterceptor
        // #postHandle
        return null;
    }

    // sessionId移除拦截器
    @Bean
    public FromClientInterceptor sessionIdUnRegistryInterceptor() {
        // 实现 org.aoju.bus.socket.spring.interceptor.FromClientInterceptor
        // #postHandle
        return null;
    }
    
}
```
