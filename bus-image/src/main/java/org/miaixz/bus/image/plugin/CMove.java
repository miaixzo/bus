/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.image.*;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.Progress;
import org.miaixz.bus.logger.Logger;

import java.text.MessageFormat;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class CMove {

    /**
     * @param callingNode    调用DICOM节点的配置
     * @param calledNode     被调用的DICOM节点配置
     * @param destinationAet 目标 aetitle
     * @param progress       处理的进度
     * @param keys           匹配和返回键。没有值的Args是返回键
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度
     */
    public static Status process(Node callingNode,
                                 Node calledNode,
                                 String destinationAet,
                                 Progress progress,
                                 Args... keys) {
        return process(new Args(), callingNode, calledNode, destinationAet, progress, keys);
    }

    /**
     * @param args           可选的高级参数(代理、身份验证、连接和TLS)
     * @param callingNode    调用DICOM节点的配置
     * @param calledNode     被调用的DICOM节点配置
     * @param destinationAet 目标 aetitle
     * @param progress       处理的进度
     * @param keys           匹配和返回键。没有值的Args是返回键
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度
     */
    public static Status process(Args args,
                                 Node callingNode,
                                 Node calledNode,
                                 String destinationAet,
                                 Progress progress,
                                 Args... keys) {
        if (null == callingNode || null == calledNode || null == destinationAet) {
            throw new IllegalArgumentException("callingNode, calledNode or destinationAet cannot be null!");
        }

        try (MoveSCU moveSCU = new MoveSCU(progress)) {
            Connection remote = moveSCU.getRemoteConnection();
            Connection conn = moveSCU.getConnection();
            args.configureBind(moveSCU.getAAssociateRQ(), remote, calledNode);
            args.configureBind(moveSCU.getApplicationEntity(), conn, callingNode);

            Centre centre = new Centre(moveSCU);

            args.configure(conn);
            args.configureTLS(conn, remote);

            moveSCU.setInformationModel(getInformationModel(args), args.getTsuidOrder(),
                    args.getTypes().contains(Option.Type.RELATIONAL));

            for (Args p : keys) {
                moveSCU.addKey(p.getTag(), p.getValues());
            }
            moveSCU.setDestination(destinationAet);

            centre.start(true);
            try {
                Status dcmState = moveSCU.getState();
                long t1 = System.currentTimeMillis();
                moveSCU.open();
                long t2 = System.currentTimeMillis();
                moveSCU.retrieve();
                Builder.forceGettingAttributes(dcmState, moveSCU);
                long t3 = System.currentTimeMillis();
                String timeMsg =
                        MessageFormat.format("DICOM C-MOVE connected in {2}ms from {0} to {1}. Sent files in {3}ms.",
                                moveSCU.getAAssociateRQ().getCallingAET(), moveSCU.getAAssociateRQ().getCalledAET(), t2 - t1,
                                t3 - t2);
                return Status.build(dcmState, timeMsg, null);
            } catch (Exception e) {
                Logger.error("movescu", e);
                Builder.forceGettingAttributes(moveSCU.getState(), moveSCU);
                return Status.build(moveSCU.getState(), null, e);
            } finally {
                Builder.close(moveSCU);
                centre.stop();
            }
        } catch (Exception e) {
            Logger.error("movescu", e);
            return new Status(Status.UnableToProcess,
                    "DICOM Move failed : " + e.getMessage(), null);
        }
    }

    private static MoveSCU.InformationModel getInformationModel(Args options) {
        Object model = options.getInformationModel();
        if (model instanceof MoveSCU.InformationModel) {
            return (MoveSCU.InformationModel) model;
        }
        return MoveSCU.InformationModel.StudyRoot;
    }

}
