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
package org.miaixz.bus.image.plugin;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.ImageProgress;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class CStore {

    /**
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param files       文件路径的列表
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度信息
     */
    public static Status process(Node callingNode, Node calledNode, List<String> files) {
        return process(null, callingNode, calledNode, files);
    }

    /**
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param files       文件路径的列表
     * @param progress    处理的进度
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度信息
     */
    public static Status process(Node callingNode, Node calledNode, List<String> files, ImageProgress progress) {
        return process(null, callingNode, calledNode, files, progress);
    }

    /**
     * @param args        可选的高级参数(代理、身份验证、连接和TLS)
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param files       文件路径的列表
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度信息
     */
    public static Status process(Args args, Node callingNode, Node calledNode, List<String> files) {
        return process(args, callingNode, calledNode, files, null);
    }

    /**
     * @param args        可选的高级参数(代理、身份验证、连接和TLS)
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param files       文件路径的列表
     * @param progress    处理的进度
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度信息
     */
    public static Status process(Args args, Node callingNode, Node calledNode, List<String> files,
            ImageProgress progress) {
        if (null == callingNode || null == calledNode) {
            throw new IllegalArgumentException("callingNode or calledNode cannot be null!");
        }

        Args options = args == null ? new Args() : args;

        StoreSCU storeSCU = null;

        try {
            Device device = new Device("storescu");
            Connection conn = new Connection();
            device.addConnection(conn);
            ApplicationEntity ae = new ApplicationEntity(callingNode.getAet());
            device.addApplicationEntity(ae);
            ae.addConnection(conn);
            storeSCU = new StoreSCU(ae, progress, options.getEditors());
            Connection remote = storeSCU.getRemoteConnection();
            Centre service = new Centre(device);

            options.configureConnect(storeSCU.getAAssociateRQ(), remote, calledNode);
            options.configureBind(ae, conn, callingNode);

            options.configure(conn);
            options.configureTLS(conn, remote);

            storeSCU.setAttributes(new Attributes());

            if (options.isNegociation()) {
                configureRelatedSOPClass(storeSCU, options.getSopClasses());
            }
            storeSCU.setPriority(options.getPriority());

            storeSCU.scanFiles(files);

            Status dcmState = storeSCU.getState();

            int n = storeSCU.getFilesScanned();
            if (n == 0) {
                return new Status(Status.UnableToProcess, "No DICOM file has been found!", null);
            } else {
                service.start();
                try {
                    long t1 = System.currentTimeMillis();
                    storeSCU.open();
                    long t2 = System.currentTimeMillis();
                    storeSCU.sendFiles();
                    Builder.forceGettingAttributes(dcmState, storeSCU);
                    long t3 = System.currentTimeMillis();
                    String timeMsg = MessageFormat.format(
                            "DICOM C-STORE connected in {2}ms from {0} to {1}. Stored files in {3}ms. Total size {4}",
                            storeSCU.getAAssociateRQ().getCallingAET(), storeSCU.getAAssociateRQ().getCalledAET(),
                            t2 - t1, t3 - t2, Builder.humanReadableByte(storeSCU.getTotalSize(), false));
                    dcmState = Status.buildMessage(dcmState, timeMsg, null);
                    dcmState.addProcessTime(t1, t2, t3);
                    dcmState.setBytesSize(storeSCU.getTotalSize());
                    return dcmState;
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    Logger.error("storescu", e);
                    Builder.forceGettingAttributes(storeSCU.getState(), storeSCU);
                    return Status.buildMessage(storeSCU.getState(), null, e);
                } finally {
                    IoKit.close(storeSCU);
                    service.stop();
                }
            }
        } catch (Exception e) {
            Logger.error("storescu", e);
            return Status.buildMessage(new Status(Status.UnableToProcess,
                    "DICOM Store failed" + Symbol.COLON + Symbol.SPACE + e.getMessage(), null), null, e);
        } finally {
            IoKit.close(storeSCU);
        }
    }

    private static void configureRelatedSOPClass(StoreSCU storescu, URL url) {
        storescu.enableSOPClassRelationshipExtNeg(true);
        Properties p = new Properties();
        try {
            if (url != null) {
                p.load(url.openStream());
            } else {
                url = ResourceKit.getResourceUrl("sop-classes-uid.properties", CStore.class);
                p.load(url.openStream());
            }
        } catch (IOException e) {
            Logger.error("Cannot read sop-class", e);
        }
        storescu.relSOPClasses.init(p);
    }

}
