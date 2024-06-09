/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.ApplicationEntity;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.logger.Logger;

import java.text.MessageFormat;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Echo {

    /**
     * @param callingAET 调用DICOM节点的AET
     * @param calledNode 被调用的DICOM节点配置
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度信息
     */
    public static Status process(String callingAET,
                                 Node calledNode) {
        return process(new Node(callingAET), calledNode);
    }

    /**
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度信息
     */
    public static Status process(Node callingNode,
                                 Node calledNode) {
        return process(null, callingNode, calledNode);
    }

    /**
     * @param args        可选的高级参数(代理、身份验证、连接和TLS)
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度信息
     */
    public static Status process(Args args,
                                 Node callingNode,
                                 Node calledNode) {
        if (null == callingNode || null == calledNode) {
            throw new IllegalArgumentException("callingNode or calledNode cannot be null!");
        }

        try {
            Device device = new Device("storescu");
            Connection conn = new Connection();
            device.addConnection(conn);
            ApplicationEntity ae = new ApplicationEntity(callingNode.getAet());
            device.addApplicationEntity(ae);
            ae.addConnection(conn);
            StoreSCU storeSCU = new StoreSCU(ae, null);
            Connection remote = storeSCU.getRemoteConnection();

            Centre centre = new Centre(device);

            args.configureBind(storeSCU.getAAssociateRQ(), remote, calledNode);
            args.configureBind(ae, conn, callingNode);

            args.configure(conn);
            args.configureTLS(conn, remote);

            storeSCU.setPriority(args.getPriority());

            centre.start(true);
            try {
                long t1 = System.currentTimeMillis();
                storeSCU.open();
                long t2 = System.currentTimeMillis();
                Attributes rsp = storeSCU.echo();
                long t3 = System.currentTimeMillis();
                String message = MessageFormat.format(
                        "Successful DICOM Echo. Connected in {2}ms from {0} to {1}. Service execution in {3}ms.",
                        storeSCU.getAAssociateRQ().getCallingAET(), storeSCU.getAAssociateRQ().getCalledAET(), t2 - t1,
                        t3 - t2);
                return new Status(rsp.getInt(Tag.Status, Status.Success), message, null);
            } finally {
                Builder.close(storeSCU);
                centre.stop();
            }
        } catch (Exception e) {
            String message = "DICOM Echo failed, storescu: " + e.getMessage();
            Logger.error(message, e);
            return new Status(Status.UnableToProcess, message, null);
        }
    }

}
