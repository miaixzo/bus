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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map.Entry;
import java.util.Properties;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.ImageParam;
import org.miaixz.bus.image.galaxy.ImageProgress;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.QueryOption;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class CGet {

    /**
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param progress    处理的进度
     * @param outputDir   文件输出路径
     * @param keys        匹配和返回键。没有值的Args是返回键
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度
     */
    public static Status process(Node callingNode, Node calledNode, ImageProgress progress, File outputDir,
            ImageParam... keys) {
        return process(null, callingNode, calledNode, progress, outputDir, keys);
    }

    /**
     * @param args        可选的高级参数(代理、身份验证、连接和TLS)
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param progress    处理的进度
     * @param outputDir   文件输出路径
     * @param keys        匹配和返回键。没有值的keys是返回键
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度
     */
    public static Status process(Args args, Node callingNode, Node calledNode, ImageProgress progress, File outputDir,
            ImageParam... keys) {
        return process(args, callingNode, calledNode, progress, outputDir, null, keys);
    }

    /**
     * @param args        可选的高级参数(代理、身份验证、连接和TLS)
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param progress    处理的进度
     * @param outputDir   文件输出路径
     * @param sopClassURL the url
     * @param keys        匹配和返回键。没有值的keys是返回键
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度
     */
    public static Status process(Args args, Node callingNode, Node calledNode, ImageProgress progress, File outputDir,
            URL sopClassURL, ImageParam... keys) {
        if (callingNode == null || calledNode == null || outputDir == null) {
            throw new IllegalArgumentException("callingNode, calledNode or outputDir cannot be null!");
        }
        GetSCU getSCU = null;
        Args options = args == null ? new Args() : args;

        try {
            getSCU = new GetSCU(progress);
            Connection remote = getSCU.getRemoteConnection();
            Connection conn = getSCU.getConnection();
            options.configureConnect(getSCU.getAAssociateRQ(), remote, calledNode);
            options.configureBind(getSCU.getApplicationEntity(), conn, callingNode);
            Centre service = new Centre(getSCU.getDevice());

            // configure
            options.configure(conn);
            options.configureTLS(conn, remote);

            getSCU.setPriority(options.getPriority());

            getSCU.setStorageDirectory(outputDir);

            getSCU.setInformationModel(getInformationModel(options), options.getTsuidOrder(),
                    options.getQueryOptions().contains(QueryOption.RELATIONAL));

            configureRelatedSOPClass(getSCU, sopClassURL);

            Status dcmState = getSCU.getState();
            for (ImageParam p : keys) {
                String[] values = p.getValues();
                getSCU.addKey(p.getTag(), values);
                if (values != null && values.length > 0) {
                    dcmState.addDicomMatchingKeys(p);
                }
            }

            service.start();
            try {
                long t1 = System.currentTimeMillis();
                getSCU.open();
                long t2 = System.currentTimeMillis();
                getSCU.retrieve();
                Builder.forceGettingAttributes(dcmState, getSCU);
                long t3 = System.currentTimeMillis();
                String timeMsg = MessageFormat.format(
                        "DICOM C-GET connected in {2}ms from {0} to {1}. Get files in {3}ms.",
                        getSCU.getAAssociateRQ().getCallingAET(), getSCU.getAAssociateRQ().getCalledAET(), t2 - t1,
                        t3 - t2);
                dcmState = Status.buildMessage(dcmState, timeMsg, null);
                dcmState.addProcessTime(t1, t2, t3);
                dcmState.setBytesSize(getSCU.getTotalSize());
                return dcmState;
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                Logger.error("getscu", e);
                Builder.forceGettingAttributes(getSCU.getState(), getSCU);
                return Status.buildMessage(getSCU.getState(), null, e);
            } finally {
                IoKit.close(getSCU);
                service.stop();
            }
        } catch (Exception e) {
            Logger.error("getscu", e);
            return Status.buildMessage(new Status(Status.UnableToProcess,
                    "DICOM Get failed" + Symbol.COLON + Symbol.SPACE + e.getMessage(), null), null, e);
        }
    }

    private static void configureRelatedSOPClass(GetSCU getSCU, URL url) {
        Properties p = new Properties();
        try {
            if (url != null) {
                p.load(url.openStream());
            } else {
                url = ResourceKit.getResourceUrl("sop-classes-tcs.properties", StoreSCP.class);
                p.load(url.openStream());
            }
        } catch (IOException e) {
            Logger.error("Cannot read sop-classes", e);
        }

        for (Entry<Object, Object> entry : p.entrySet()) {
            configureStorageSOPClass(getSCU, (String) entry.getKey(), (String) entry.getValue());
        }
    }

    private static void configureStorageSOPClass(GetSCU getSCU, String cuid, String tsuids) {
        String[] ts = StringKit.splitToArray(tsuids, ";");
        for (int i = 0; i < ts.length; i++) {
            ts[i] = UID.toUID(ts[i]);
        }
        getSCU.addOfferedStorageSOPClass(UID.toUID(cuid), ts);
    }

    private static GetSCU.InformationModel getInformationModel(Args options) {
        Object model = options.getInformationModel();
        if (model instanceof GetSCU.InformationModel) {
            return (GetSCU.InformationModel) model;
        }
        return GetSCU.InformationModel.StudyRoot;
    }

}
