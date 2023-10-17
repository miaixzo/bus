/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.plugin;

import org.aoju.bus.image.*;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.metric.ApplicationEntity;
import org.aoju.bus.image.metric.Connection;
import org.aoju.bus.image.metric.Progress;
import org.aoju.bus.logger.Logger;

import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

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
    public static Status process(Node callingNode,
                                 Node calledNode,
                                 List<String> files) {
        return process(null, callingNode, calledNode, files);
    }

    /**
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param files       文件路径的列表
     * @param progress    处理的进度
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度信息
     */
    public static Status process(Node callingNode,
                                 Node calledNode,
                                 List<String> files,
                                 Progress progress) {
        return process(null, callingNode, calledNode, files, progress);
    }

    /**
     * @param args        可选的高级参数(代理、身份验证、连接和TLS)
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param files       文件路径的列表
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度信息
     */
    public static Status process(Args args,
                                 Node callingNode,
                                 Node calledNode,
                                 List<String> files) {
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
    public static Status process(Args args,
                                 Node callingNode,
                                 Node calledNode,
                                 List<String> files,
                                 Progress progress) {
        if (null == callingNode || null == calledNode) {
            throw new IllegalArgumentException("callingNode or calledNode cannot be null!");
        }

        StoreSCU storeSCU = null;

        try {
            Device device = new Device("storescu");
            Connection conn = new Connection();
            device.addConnection(conn);
            ApplicationEntity ae = new ApplicationEntity(callingNode.getAet());
            device.addApplicationEntity(ae);
            ae.addConnection(conn);
            storeSCU = new StoreSCU(ae, progress, args.getEditors());
            Connection remote = storeSCU.getRemoteConnection();

            Centre centre = new Centre(device);

            args.configureBind(storeSCU.getAAssociateRQ(), remote, calledNode);
            args.configureBind(ae, conn, callingNode);

            args.configure(conn);
            args.configureTLS(conn, remote);

            storeSCU.setAttributes(new Attributes());

            if (args.isExtendNegociation()) {
                configureRelatedSOPClass(storeSCU, args.getExtendSopClassesURL());
            }
            storeSCU.setPriority(args.getPriority());

            storeSCU.scanFiles(files, false);

            Status dcmState = storeSCU.getState();

            int n = storeSCU.getFilesScanned();
            if (n == 0) {
                return new Status(Status.UnableToProcess, "No DICOM file has been found!", null);
            } else {
                centre.start(true);
                try {
                    long t1 = System.currentTimeMillis();
                    storeSCU.open();
                    long t2 = System.currentTimeMillis();
                    storeSCU.sendFiles();
                    Builder.forceGettingAttributes(dcmState, storeSCU);
                    long t3 = System.currentTimeMillis();
                    String timeMsg = MessageFormat.format(
                            "DICOM C-STORE connected in {2}ms from {0} to {1}. Stored files in {3}ms. Total size {4}",
                            storeSCU.getAAssociateRQ().getCallingAET(), storeSCU.getAAssociateRQ().getCalledAET(), t2 - t1,
                            t3 - t2, Builder.humanReadableByte(storeSCU.getTotalSize(), false));
                    return Status.build(dcmState, timeMsg, null);
                } catch (Exception e) {
                    Logger.error("storescu", e);
                    Builder.forceGettingAttributes(storeSCU.getState(), storeSCU);
                    return Status.build(storeSCU.getState(), null, e);
                } finally {
                    Builder.close(storeSCU);
                    centre.stop();
                }
            }
        } catch (Exception e) {
            Logger.error("storescu", e);
            return new Status(Status.UnableToProcess,
                    "DICOM Store failed : " + e.getMessage(), null);
        } finally {
            Builder.close(storeSCU);
        }
    }

    private static void configureRelatedSOPClass(StoreSCU storescu, URL url) {
        storescu.enableSOPClassRelationshipExtNeg(true);
        Properties p = new Properties();
        try {
            if (null != url) {
                p.load(url.openStream());
            }
        } catch (Exception e) {
            Logger.error("Read sop classes", e);
        }
        storescu.relSOPClasses.init(p);
    }

}
