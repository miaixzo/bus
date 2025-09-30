/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.cron.tempus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.io.Serial;
import java.io.Serializable;

/**
 * Temporal 工作流执行接口，提供与 Temporal 服务器交互的基本功能。
 * <p>
 * 该接口定义了执行 Temporal 工作流的规范，包括工作流启动、参数传递等功能。 实现类需要提供与 Temporal 服务器通信的具体实现。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Temporal {

    /**
     * 执行 Temporal 工作流任务。
     * <p>
     * 该方法通过 Temporal gRPC 发起一个工作流任务，支持 HTTP 和 HTTPS 协议， 并返回工作流执行 ID（Run ID）。工作流执行参数将被封装为 Message 对象， 并转换为 JSON
     * 格式传递给工作流。
     * </p>
     *
     * @param endpoint Temporal 服务器 Gateway 地址，格式为 "主机名:端口号"， 例如 "https://temporal.example.com:8080"。 必须非空且有效，否则抛出
     *                 IllegalArgumentException。
     * @param queue    任务队列名称，工作流将被分发到此队列，必须与 Worker 端监听的队列名称一致。 例如 "cron-queue"；非空字符串。
     * @param type     工作流类型名称，用于标识要执行的具体工作流，对应 Worker 端注册的工作流实现。 例如 "org.example.Workflow"；非空字符串。
     * @param args     工作流执行参数，可以是任何可序列化的 Java 对象（POJO、Map 等）， 将被转换为 JSON 格式传递给工作流。通过
     *                 Message.builder().data(args).build() 封装； 必须非空。
     * @return 返回工作流执行 ID (Run ID)，唯一标识本次执行，可用于后续查询工作流状态、历史或结果。 如果启动成功，返回非空字符串；失败则抛出异常。
     * @throws IllegalArgumentException 如果任何必需参数为 null
     * @throws RuntimeException         如果工作流启动失败
     */
    String execute(String endpoint, String queue, String type, Object args);

    /**
     * 工作流消息封装类，用于传递工作流执行参数。
     * <p>
     * 该类封装了工作流执行所需的参数信息，支持序列化以便在网络间传输。 使用 Builder 模式创建实例，提供便捷的参数设置方式。
     * </p>
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public class Message implements Serializable {

        @Serial
        private static final long serialVersionUID = 2852290719686L;

        /**
         * 工作流执行参数。
         * <p>
         * 可以是任何可序列化的 Java 对象（POJO、Map 等）， 将被转换为 JSON 格式传递给工作流。
         * </p>
         */
        public Object data;
    }

}
