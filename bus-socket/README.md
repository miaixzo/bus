#### 项目说明

bus-socket是一款开源的Java AIO框架，支持 TCP、UDP、SSL/TLS，追求代码量、性能、稳定性、接口设计各方面都达到极致。

## 运行环境

要求Java 17+

*
* 通常情况下仅需实现{@link org.miaixz.bus.socket.Protocol}、{@link org.miaixz.bus.socket.Handler}即可
* 如需仅需通讯层面的监控，bus-socket提供了接口{@link socket.org.miaixz.bus.Monitor}以供使用
*
* 完成本package的接口开发后，便可使用{@link org.miaixz.bus.socket.accord.AioClient} / {@link
  org.miaixz.bus.socket.accord.AioServer}提供AIO的客户端/服务端通信服务
*

服务端开发主要分两步：

1.构造服务端对象AioServer。该类的构造方法有以下几个入参： port，服务端监听端口号；
Protocol，协议解码类，正是上一步骤实现的解码算法类：StringProtocol；
Handler，消息处理器，对Protocol解析出来的消息进行业务处理。 因为只是个简单示例，采用匿名内部类的形式做演示。实际业务场景中可能涉及到更复杂的逻辑，开发同学自行把控。

```java
 public class AioServer {

    public static void main(String[] args) {
        AioServer<String> server = new AioServer<String>(8080, new DemoProtocol(), new DemoService() {
            public void process(Session<String> session, String msg) {
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

            public void stateEvent(Session<String> session, Status Status, Throwable throwable) {
            }
        });
        server.start();
    }

    class DemoProtocol implements Protocol<byte[]> {

        public byte[] decode(ByteBuffer readBuffer, Session<byte[]> session) {
            if (readBuffer.remaining() > 0) {
                byte[] data = new byte[readBuffer.remaining()];
                readBuffer.get(data);
                return data;
            }
            return null;
        }

        public ByteBuffer encode(byte[] msg, Session<byte[]> session) {
            ByteBuffer buffer = ByteBuffer.allocate(msg.length);
            buffer.put(msg);
            buffer.flip();
            return buffer;
        }
    }


    class DemoService implements Handler<byte[]>, Runnable {
        private HashMap<String, Session<byte[]>> clients = new HashMap<>();
        private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(12);

        public DemoService() {
            executorService.scheduleAtFixedRate(this, 2, 2, TimeUnit.SECONDS);
        }

        public void run() {
            if (this.clients.isEmpty()) return;
            for (Session<byte[]> session : this.clients.values()) {
                try {
                    session.write("Hey! bus-socket it's work...".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void process(Session<byte[]> session, byte[] msg) {
            JSONObject jsonObject = JSON.parseObject(msg, JSONObject.class);
            System.out.println(jsonObject.getString("content"));
            try {
                session.write("{\"result\": \"OK\"}".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stateEvent(Session<byte[]> session, Status Status, Throwable throwable) {
            switch (Status) {
                case NEW_SESSION:
                    System.out.println("Status.NEW_SESSION");
                    break;
                case INPUT_SHUTDOWN:
                    System.out.println("Status.INPUT_SHUTDOWN");
                    break;
                case PROCESS_EXCEPTION:
                    System.out.println("Status.PROCESS_EXCEPTION");
                    break;
                case DECODE_EXCEPTION:
                    System.out.println("Status.DECODE_EXCEPTION");
                    break;
                case INPUT_EXCEPTION:
                    System.out.println("Status.INPUT_EXCEPTION");
                    break;
                case OUTPUT_EXCEPTION:
                    System.out.println("Status.OUTPUT_EXCEPTION");
                    break;
                case SESSION_CLOSING:
                    System.out.println("Status.SESSION_CLOSING");
                    break;
                case SESSION_CLOSED:
                    System.out.println("Status.SESSION_CLOSED");
                    break;
                case FLOW_LIMIT:
                    System.out.println("Status.FLOW_LIMIT");
                    break;
                case RELEASE_FLOW_LIMIT:
                    System.out.println("Status.RELEASE_FLOW_LIMIT");
                    break;
                default:
                    System.out.println("Status.default");
            }
        }
    }

}
 ```

```java
public class AioClient {

    public static void main(String[] args) throws Exception {
        AioClient<String> client = new AioClient<>("localhost", 8888, new ClientProtocol(), new ClientProcessor());
        Session session = client.start();
        session.writeBuffer().writeInt(1);
      client.shutdownNow();
    }

  static class ClientProcessor implements Handler<String> {

    @Override
    public void process(Session session, String msg) {
      System.out.println("Receive data from server：" + msg);
    }

    @Override
    public void stateEvent(Session session, Status Status, Throwable throwable) {
      System.out.println("State:" + Status);
      if (Status == StateMachineEnum.OUTPUT_EXCEPTION) {
        throwable.printStackTrace();
      }
    }
  }

  static class ClientProtocol implements Protocol<String> {

    @Override
    public String decode(ByteBuffer data, Session session) {
      int remaining = data.remaining();
            if (remaining < 4) {
                return null;
            }
            data.mark();
            int length = data.getInt();
            if (length > data.remaining()) {
                data.reset();
                System.out.println("reset");
                return null;
            }
            byte[] b = new byte[length];
            data.get(b);
            data.mark();
            return new String(b);
        }

    }

}
```

## 性能测试

- 环境准备
    1. 测试项目：[abarth](https://github.com/839128/abarth)
    2. 通信协议：Http
    3. 压测工具：[wrk](https://github.com/wg/wrk)
    4. 测试机：MacBook Pro, 2.9Ghz i5, 4核8G内存
    5. 测试命令：
    ```
    wrk -H 'Host: 10.0.0.1' -H 'Accept: text/plain,text/html;q=0.9,application/xhtml+xml;q=0.9,application/xml;q=0.8,*/*;q=0.7' -H 'Connection: keep-alive' --latency -d 15 -c 1024 --timeout 8 -t 4 http://127.0.0.1:8080/plaintext -s pipeline.lua -- 16
    ```
- 测试结果：bus-socket的性能表现基本稳定维持在 128MB/s 左右。

  | 连接数 | Requests/sec | Transfer/sec | | -- | -- | -- | | 512 | 924343.47 | 128.70MB| | 1024 | 922967.92 |
  128.51MB| |
  2048 | 933479.41 | 129.97MB| | 4096 | 922589.53 | 128.46MB|

### 致谢

- 此项目部分程序来源于[smart-socket](https://gitee.com/smartboot/smart-socket) 经作者三刀(zhengjunweimail@163.com)
  同意后使用MIT开源，使用程序请遵守相关开源协议