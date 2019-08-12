## 请求数据格式
```json
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
```json
{
  "e": "heartbeat",
  "d": "ping"
}
```
- 客户端收到心跳数据时，请发送以下数据进行响应：
```json
{
  "e": "heartbeat",
  "d": "pong"
}
```
- 若服务端发送2次心跳事件仍无响应时，会断开连接

## 快速开始

- 定义每个topic的事件处理器，返回值是对客户端的响应数据，返回值为空则不响应

对于确定的topic，可实现WebSocketEventHandler事件处理器，通过注解管理topic
```java
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
```java
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
```java
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
```java
public class SendMessageHandler {
    
    public static void send(String topic, String message) {
        MessagePublisher.publish(topic, message);
    }
    
}
```
