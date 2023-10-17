#### 项目说明

bus-socket是一款开源的Java AIO框架，支持 TCP、UDP、SSL/TLS，追求代码量、性能、稳定性、接口设计各方面都达到极致。

## 运行环境

要求Java 17+

*
* 通常情况下仅需实现{@link org.aoju.bus.socket.Protocol}、{@link org.aoju.bus.socket.process.MessageProcessor}即可
* 如需仅需通讯层面的监控，bus-socket提供了接口{@link org.aoju.bus.socket.NetMonitor}以供使用
*
* 完成本package的接口开发后，便可使用{@link org.aoju.bus.socket.AioQuickClient} / {@link
  org.aoju.bus.socket.AioQuickServer}提供AIO的客户端/服务端通信服务
*

服务端开发主要分两步：

1.构造服务端对象AioQuickServer。该类的构造方法有以下几个入参： port，服务端监听端口号；
Protocol，协议解码类，正是上一步骤实现的解码算法类：StringProtocol；
MessageProcessor，消息处理器，对Protocol解析出来的消息进行业务处理。 因为只是个简单示例，采用匿名内部类的形式做演示。实际业务场景中可能涉及到更复杂的逻辑，开发同学自行把控。

```java
 public class AioServer {

    public static void main(String[] args) {
        AioQuickServer<String> server = new AioQuickServer<String>(8080, new DemoProtocol(), new DemoService() {
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

            public void stateEvent(AioSession<String> session, SocketStatus SocketStatus, Throwable throwable) {
            }
        });
        server.start();
    }

    class DemoProtocol implements Protocol<byte[]> {

        public byte[] decode(ByteBuffer readBuffer, AioSession<byte[]> session) {
            if (readBuffer.remaining() > 0) {
                byte[] data = new byte[readBuffer.remaining()];
                readBuffer.get(data);
                return data;
            }
            return null;
        }

        public ByteBuffer encode(byte[] msg, AioSession<byte[]> session) {
            ByteBuffer buffer = ByteBuffer.allocate(msg.length);
            buffer.put(msg);
            buffer.flip();
            return buffer;
        }
    }


    class DemoService implements MessageProcessor<byte[]>, Runnable {
        private HashMap<String, AioSession<byte[]>> clients = new HashMap<String, AioSession<byte[]>>();
        private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(12);

        public DemoService() {
            executorService.scheduleAtFixedRate(this, 2, 2, TimeUnit.SECONDS);
        }

        public void run() {
            if (this.clients.isEmpty()) return;
            for (AioSession<byte[]> session : this.clients.values()) {
                try {
                    session.write("Hey! bus-socket it's work...".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void process(AioSession<byte[]> session, byte[] msg) {
            JSONObject jsonObject = JSON.parseObject(msg, JSONObject.class);
            System.out.println(jsonObject.getString("content"));
            try {
                session.write("{\"result\": \"OK\"}".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stateEvent(AioSession<byte[]> session, SocketStatus SocketStatus, Throwable throwable) {
            switch (SocketStatus) {
                case NEW_SESSION:
                    System.out.println("SocketStatus.NEW_SESSION");
                    break;
                case INPUT_SHUTDOWN:
                    System.out.println("SocketStatus.INPUT_SHUTDOWN");
                    break;
                case PROCESS_EXCEPTION:
                    System.out.println("SocketStatus.PROCESS_EXCEPTION");
                    break;
                case DECODE_EXCEPTION:
                    System.out.println("SocketStatus.DECODE_EXCEPTION");
                    break;
                case INPUT_EXCEPTION:
                    System.out.println("SocketStatus.INPUT_EXCEPTION");
                    break;
                case OUTPUT_EXCEPTION:
                    System.out.println("SocketStatus.OUTPUT_EXCEPTION");
                    break;
                case SESSION_CLOSING:
                    System.out.println("SocketStatus.SESSION_CLOSING");
                    break;
                case SESSION_CLOSED:
                    System.out.println("SocketStatus.SESSION_CLOSED");
                    break;
                case FLOW_LIMIT:
                    System.out.println("SocketStatus.FLOW_LIMIT");
                    break;
                case RELEASE_FLOW_LIMIT:
                    System.out.println("SocketStatus.RELEASE_FLOW_LIMIT");
                    break;
                default:
                    System.out.println("SocketStatus.default");
            }
        }
    }

}
 ```

```java
public class AioClient {

    public static void main(String[] args) throws Exception {
        AioQuickClient<String> aioQuickClient = new AioQuickClient<>("localhost", 8888, new ClientProtocol(), new ClientProcessor());
        AioSession session = aioQuickClient.start();
        session.writeBuffer().writeInt(1);
        aioQuickClient.shutdownNow();
    }

  static class ClientProcessor implements MessageProcessor<String> {

    @Override
    public void process(AioSession session, String msg) {
      System.out.println("Receive data from server：" + msg);
    }

    @Override
    public void stateEvent(AioSession session, StateMachineEnum socketStatus, Throwable throwable) {
      System.out.println("State:" + socketStatus);
      if (socketStatus == StateMachineEnum.OUTPUT_EXCEPTION) {
        throwable.printStackTrace();
      }
    }
  }

  static class ClientProtocol implements Protocol<String> {

    @Override
    public String decode(ByteBuffer data, AioSession session) {
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

```java
public class NioServer {

    public static void main(String[] args) {
        QuickNioServer server = new QuickNioServer(8080);
        server.setChannelHandler((sc) -> {
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            try {
                //从channel读数据到缓冲区
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    //Flips this buffer.  The limit is set to the current position and then
                    // the position is set to zero，就是表示要从起始位置开始读取数据
                    readBuffer.flip();
                    //eturns the number of elements between the current position and the  limit.
                    // 要读取的字节长度
                    byte[] bytes = new byte[readBuffer.remaining()];
                    //将缓冲区的数据读到bytes数组
                    readBuffer.get(bytes);
                    String body = StringKit.toString(bytes);
                    Logger.info("[{}]: {}", sc.getRemoteAddress(), body);

                    doWrite(sc, body);
                } else if (readBytes < 0) {
                    IoKit.close(sc);
                }
            } catch (IOException e) {
                throw new InternalException(e);
            }
        });
        server.listen();
    }

    public static void doWrite(SocketChannel channel, String response) throws IOException {
        response = "收到消息：" + response;
        //将缓冲数据写入渠道，返回给客户端
        channel.write(BufferKit.create(response));
    }

}

```

```java
 public class NioClient {

    public static void main(String[] args) {
        QuickNioClient client = new QuickNioClient("127.0.0.1", 8080);
        client.setChannelHandler((sc) -> {
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            //从channel读数据到缓冲区
            int readBytes = sc.read(readBuffer);
            if (readBytes > 0) {
                //Flips this buffer.  The limit is set to the current position and then
                // the position is set to zero，就是表示要从起始位置开始读取数据
                readBuffer.flip();
                //returns the number of elements between the current position and the  limit.
                // 要读取的字节长度
                byte[] bytes = new byte[readBuffer.remaining()];
                //将缓冲区的数据读到bytes数组
                readBuffer.get(bytes);
                String body = StringKit.toString(bytes);
                Logger.info("[{}]: {}", sc.getRemoteAddress(), body);
            } else if (readBytes < 0) {
                sc.close();
            }
        });

        client.listen();
        client.write(BufferKit.create("你好。\n"));
        client.write(BufferKit.create("你好2。"));

        // 在控制台向服务器端发送数据
        Logger.info("请输入发送的消息：");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String request = scanner.nextLine();
            if (null != request && request.trim().length() > 0) {
                client.write(BufferKit.create(request));
            }
        }
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

  | 连接数 | Requests/sec | Transfer/sec | | -- | -- | -- | | 512 | 924343.47 | 128.70MB| | 1024 | 922967.92 |
  128.51MB| |
  2048 | 933479.41 | 129.97MB| | 4096 | 922589.53 | 128.46MB|

### 致谢

- 此项目部分程序来源于[smart-socket](https://gitee.com/smartboot/smart-socket) 经作者三刀(zhengjunweimail@163.com)
  同意后使用MIT开源，使用程序请遵守相关开源协议