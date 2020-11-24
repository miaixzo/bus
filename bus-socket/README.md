## 功能概述
bus-socket是一款开源的Java AIO框架，支持 TCP、UDP、SSL/TLS，追求代码量、性能、稳定性、接口设计各方面都达到极致。

## 运行环境
要求JDK1.8+

  <dependency>
      <groupId>org.aoju</groupId>
      <artifactId>bus-socket</artifactId>
      <version>6.1.2</version>
  </dependency>
  
  
  服务端开发主要分两步：

   1.构造服务端对象AioQuickServer。该类的构造方法有以下几个入参：
    port，服务端监听端口号；
    Protocol，协议解码类，正是上一步骤实现的解码算法类：StringProtocol；
    MessageProcessor，消息处理器，对Protocol解析出来的消息进行业务处理。 因为只是个简单示例，采用匿名内部类的形式做演示。实际业务场景中可能涉及到更复杂的逻辑，开发同学自行把控。
      
```java
 public class Server {
     public static void main(String[] args) throws IOException {
         // 1
         AioQuickServer<String> server = new AioQuickServer<String>(8080, new StringProtocol(), new MessageProcessor<String>() {
             public void process(AioSession<String> session, String msg) {
                 System.out.println("接受到客户端消息:" + msg);
 
                 byte[] response = "Hi Client!".getBytes();
                 byte[] head = {(byte) response.length};
                 try {
                     session.writeBuffer().write(head);
                     session.writeBuffer().write(response);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
 
             public void stateEvent(AioSession<String> session, StateMachineEnum stateMachineEnum, Throwable throwable) {
             }
         });
         //2
         server.start();
     }
 }
 ```

## 性能测试
- 环境准备
    1. 测试项目：[abarth](https://github.com/aoju/abarth) 
    2. 通信协议：Http
    3. 压测工具：[wrk](https://github.com/wg/wrk)
    4. 测试机：MacBook Pro, 2.9Ghz i5, 4核8G内存
    5. 测试命令：
    ```
    wrk -H 'Host: 10.0.0.1' -H 'Accept: text/plain,text/html;q=0.9,application/xhtml+xml;q=0.9,application/xml;q=0.8,*/*;q=0.7' -H 'Connection: keep-alive' --latency -d 15 -c 1024 --timeout 8 -t 4 http://127.0.0.1:8080/plaintext -s pipeline.lua -- 16
    ```
- 测试结果：bus-socket的性能表现基本稳定维持在 128MB/s 左右。

    |  连接数  | Requests/sec   |  Transfer/sec  |
    | -- | -- | -- |
    | 512 | 924343.47 | 128.70MB|
    | 1024 | 922967.92 | 128.51MB|
    | 2048 | 933479.41 | 129.97MB|
    | 4096 | 922589.53 | 128.46MB|

### 致谢
- 感谢 JetBrains 为 bus-socket 提供的 IDEA License