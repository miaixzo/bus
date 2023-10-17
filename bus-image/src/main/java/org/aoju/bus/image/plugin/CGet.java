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

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.image.*;
import org.aoju.bus.image.metric.Connection;
import org.aoju.bus.image.metric.Progress;
import org.aoju.bus.logger.Logger;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map.Entry;
import java.util.Properties;

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
    public static Status process(Node callingNode,
                                 Node calledNode,
                                 Progress progress,
                                 File outputDir,
                                 Args... keys) {
        return process(new Args(), callingNode, calledNode, progress, outputDir, keys);
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
    public static Status process(Args args,
                                 Node callingNode,
                                 Node calledNode,
                                 Progress progress,
                                 File outputDir,
                                 Args... keys) {
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
    public static Status process(Args args,
                                 Node callingNode,
                                 Node calledNode,
                                 Progress progress,
                                 File outputDir,
                                 URL sopClassURL,
                                 Args... keys) {
        if (null == callingNode || null == calledNode || null == outputDir) {
            throw new IllegalArgumentException("callingNode, calledNode or outputDir cannot be null!");
        }

        try {
            GetSCU getSCU = new GetSCU(progress);
            Connection remote = getSCU.getRemoteConnection();
            Connection conn = getSCU.getConnection();
            args.configureBind(getSCU.getAAssociateRQ(), remote, calledNode);
            args.configureBind(getSCU.getApplicationEntity(), conn, callingNode);

            Centre centre = new Centre(getSCU.getDevice());

            args.configure(conn);
            args.configureTLS(conn, remote);

            getSCU.setPriority(args.getPriority());

            getSCU.setStorageDirectory(outputDir);

            getSCU.setInformationModel(getInformationModel(args), args.getTsuidOrder(),
                    args.getTypes().contains(Option.Type.RELATIONAL));

            configureRelatedSOPClass(getSCU, sopClassURL);

            for (Args p : keys) {
                getSCU.addKey(p.getTag(), p.getValues());
            }

            centre.start(true);
            try {
                Status dcmState = getSCU.getState();
                long t1 = System.currentTimeMillis();
                getSCU.open();
                long t2 = System.currentTimeMillis();
                getSCU.retrieve();
                Builder.forceGettingAttributes(dcmState, getSCU);
                long t3 = System.currentTimeMillis();
                String timeMsg =
                        MessageFormat.format("DICOM C-GET connected in {2}ms from {0} to {1}. Get files in {3}ms.",
                                getSCU.getAAssociateRQ().getCallingAET(), getSCU.getAAssociateRQ().getCalledAET(), t2 - t1,
                                t3 - t2);
                return Status.build(dcmState, timeMsg, null);
            } catch (Exception e) {
                Logger.error("getscu", e);
                Builder.forceGettingAttributes(getSCU.getState(), getSCU);
                return Status.build(getSCU.getState(), null, e);
            } finally {
                Builder.close(getSCU);
                centre.stop();
            }
        } catch (Exception e) {
            Logger.error("getscu", e);
            return new Status(Status.UnableToProcess,
                    "DICOM Get failed : " + e.getMessage(), null);
        }
    }

    private static void configureRelatedSOPClass(GetSCU getSCU, URL url) {
        Properties p = new Properties();
        try {
            if (null != url) {
                p.load(url.openStream());
            }
            for (Entry<Object, Object> entry : p.entrySet()) {
                configureStorageSOPClass(getSCU, (String) entry.getKey(), (String) entry.getValue());
            }
        } catch (Exception e) {
            Logger.error("Read sop classes", e);
        }
    }

    private static void configureStorageSOPClass(GetSCU getSCU, String cuid, String tsuids) {
        String[] ts = StringKit.splitToArray(tsuids, Symbol.C_SEMICOLON);
        for (int i = 0; i < ts.length; i++) {
            ts[i] = Builder.toUID(ts[i]);
        }
        getSCU.addOfferedStorageSOPClass(Builder.toUID(cuid), ts);
    }

    private static GetSCU.InformationModel getInformationModel(Args options) {
        Object model = options.getInformationModel();
        if (model instanceof GetSCU.InformationModel) {
            return (GetSCU.InformationModel) model;
        }
        return GetSCU.InformationModel.StudyRoot;
    }

}
