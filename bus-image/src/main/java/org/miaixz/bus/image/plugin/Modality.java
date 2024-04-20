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

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.Sequence;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Modality {

    public static String calledAET;

    /**
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param keys        匹配和返回键。没有值的Args是返回键
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度信息
     */
    public static Status process(Node callingNode,
                                 Node calledNode,
                                 Args... keys) {
        return process(null, callingNode, calledNode, 0, keys);
    }

    /**
     * @param args        可选的高级参数(代理、身份验证、连接和TLS)
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param keys        匹配和返回键。没有值的Args是返回键
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度信息
     */
    public static Status process(Args args,
                                 Node callingNode,
                                 Node calledNode,
                                 Args... keys) {
        return process(args, callingNode, calledNode, 0, keys);
    }

    /**
     * @param args        可选的高级参数(代理、身份验证、连接和TLS)
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param cancelAfter 接收到指定数目的匹配项后，取消查询请求
     * @param keys        匹配和返回键。没有值的Args是返回键
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度信息
     */
    public static Status process(Args args,
                                 Node callingNode,
                                 Node calledNode,
                                 int cancelAfter,
                                 Args... keys) {
        if (null == callingNode || null == calledNode) {
            throw new IllegalArgumentException("callingNode or calledNode cannot be null!");
        }

        try (FindSCU findSCU = new FindSCU()) {
            Connection remote = findSCU.getRemoteConnection();
            Connection conn = findSCU.getConnection();
            args.configureBind(findSCU.getAAssociateRQ(), remote, calledNode);
            args.configureBind(findSCU.getApplicationEntity(), conn, callingNode);

            Centre centre = new Centre(findSCU.getDevice());

            args.configure(conn);
            args.configureTLS(conn, remote);

            findSCU.setInformationModel(getInformationModel(args), args.getTsuidOrder(),
                    args.getTypes());

            addKeys(findSCU, keys);

            findSCU.setCancelAfter(cancelAfter);
            findSCU.setPriority(args.getPriority());

            centre.start(true);
            try {
                Status dcmState = findSCU.getState();
                long t1 = System.currentTimeMillis();
                findSCU.open();
                long t2 = System.currentTimeMillis();
                findSCU.query();
                Builder.forceGettingAttributes(dcmState, findSCU);
                long t3 = System.currentTimeMillis();
                String timeMsg =
                        MessageFormat.format("DICOM C-Find connected in {2}ms from {0} to {1}. Query in {3}ms.",
                                findSCU.getAAssociateRQ().getCallingAET(), findSCU.getAAssociateRQ().getCalledAET(), t2 - t1,
                                t3 - t2);
                return Status.build(dcmState, timeMsg, null);
            } catch (Exception e) {
                Logger.error("findscu", e);
                Builder.forceGettingAttributes(findSCU.getState(), findSCU);
                return Status.build(findSCU.getState(), null, e);
            } finally {
                Builder.close(findSCU);
                centre.stop();
            }
        } catch (Exception e) {
            Logger.error("findscu", e);
            return new Status(Status.UnableToProcess,
                    "DICOM Find failed :" + e.getMessage(), null);
        }
    }

    private static void addKeys(FindSCU findSCU, Args[] keys) {
        for (Args p : keys) {
            int[] pSeq = p.getParentSeqTags();
            if (null == pSeq || pSeq.length == 0) {
                CFind.addAttributes(findSCU.getKeys(), p);
            } else {
                Attributes parent = findSCU.getKeys();
                for (int value : pSeq) {
                    Sequence lastSeq = parent.getSequence(value);
                    if (null == lastSeq || lastSeq.isEmpty()) {
                        lastSeq = parent.newSequence(value, 1);
                        lastSeq.add(new Attributes());
                    }
                    parent = lastSeq.get(0);
                }

                CFind.addAttributes(parent, p);
            }
        }
    }

    private static FindSCU.InformationModel getInformationModel(Args options) {
        Object model = options.getInformationModel();
        if (model instanceof FindSCU.InformationModel) {
            return (FindSCU.InformationModel) model;
        }
        return FindSCU.InformationModel.MWL;
    }

    public static void setTlsParams(Connection remote, Connection conn) {
        remote.setTlsProtocols(conn.getTlsProtocols());
        remote.setTlsCipherSuites(conn.getTlsCipherSuites());
    }

    private static void addReferencedPerformedProcedureStepSequence(String mppsiuid,
                                                                    StoreSCU storescu) {
        Attributes attrs = storescu.getAttributes();
        Sequence seq = attrs.newSequence(Tag.ReferencedPerformedProcedureStepSequence, 1);
        Attributes item = new Attributes(2);
        item.setString(Tag.ReferencedSOPClassUID, VR.UI, UID.ModalityPerformedProcedureStepSOPClass);
        item.setString(Tag.ReferencedSOPInstanceUID, VR.UI, mppsiuid);
        seq.add(item);
    }

    private static void nullifyReferencedPerformedProcedureStepSequence(StoreSCU storescu) {
        Attributes attrs = storescu.getAttributes();
        attrs.setNull(Tag.ReferencedPerformedProcedureStepSequence, VR.SQ);
    }

    private static void sendStgCmt(StgSCU stgcmtscu) throws IOException,
            InterruptedException, InternalException, GeneralSecurityException {
        printNextStepMessage("Will now send Storage Commitment to " + calledAET);
        try {
            stgcmtscu.open();
            stgcmtscu.sendRequests();
        } finally {
            stgcmtscu.close();
        }
    }

    private static void sendMpps(MppsSCU mppsscu, boolean sendNSet) throws IOException,
            InterruptedException, InternalException, GeneralSecurityException {
        try {
            printNextStepMessage("Will now send MPPS N-CREATE to " + calledAET);
            mppsscu.open();
            mppsscu.createMpps();
            if (sendNSet) {
                printNextStepMessage("Will now send MPPS N-SET to " + calledAET);
                mppsscu.updateMpps();
            }
        } finally {
            mppsscu.close();
        }
    }

    private static void sendMppsNSet(MppsSCU mppsscu) throws IOException, InterruptedException,
            InternalException, GeneralSecurityException {
        try {
            printNextStepMessage("Will now send MPPS N-SET to " + calledAET);
            mppsscu.open();
            mppsscu.updateMpps();
        } finally {
            mppsscu.close();
        }
    }

    private static void printNextStepMessage(String message) throws IOException {
        Logger.info("===========================================================");
        Logger.info(message + ". Press <enter> to continue.");
        Logger.info("===========================================================");
        new BufferedReader(new InputStreamReader(System.in)).read();
    }

    private static void sendObjects(StoreSCU storescu) throws IOException,
            InterruptedException, InternalException, GeneralSecurityException {
        printNextStepMessage("Will now send DICOM object(s) to " + calledAET);
        try {
            storescu.open();
            storescu.sendFiles();
        } finally {
            storescu.close();
        }
    }

    public static void setCalledAET(String calledAET) {
        Modality.calledAET = calledAET;
    }

}
