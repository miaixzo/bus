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
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.service.Level;
import org.miaixz.bus.logger.Logger;

import java.text.MessageFormat;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class CFind {

    public static final Args PatientID = new Args(Tag.PatientID);
    public static final Args IssuerOfPatientID = new Args(Tag.IssuerOfPatientID);
    public static final Args PatientName = new Args(Tag.PatientName);
    public static final Args PatientBirthDate = new Args(Tag.PatientBirthDate);
    public static final Args PatientSex = new Args(Tag.PatientSex);

    public static final Args StudyInstanceUID = new Args(Tag.StudyInstanceUID);
    public static final Args AccessionNumber = new Args(Tag.AccessionNumber);
    public static final Args IssuerOfAccessionNumberSequence = new Args(Tag.IssuerOfAccessionNumberSequence);
    public static final Args StudyID = new Args(Tag.StudyID);
    public static final Args ReferringPhysicianName = new Args(Tag.ReferringPhysicianName);
    public static final Args StudyDescription = new Args(Tag.StudyDescription);
    public static final Args StudyDate = new Args(Tag.StudyDate);
    public static final Args StudyTime = new Args(Tag.StudyTime);

    public static final Args SeriesInstanceUID = new Args(Tag.SeriesInstanceUID);
    public static final Args Modality = new Args(Tag.Modality);
    public static final Args SeriesNumber = new Args(Tag.SeriesNumber);
    public static final Args SeriesDescription = new Args(Tag.SeriesDescription);

    public static final Args SOPInstanceUID = new Args(Tag.SOPInstanceUID);
    public static final Args InstanceNumber = new Args(Tag.InstanceNumber);

    /**
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param keys        用于匹配和返回键。 没有值的Args是返回键
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度信息
     */
    public static Status process(Node callingNode,
                                 Node calledNode,
                                 Args... keys) {
        return process(new Args(), callingNode, calledNode, 0, Level.STUDY, keys);
    }

    /**
     * @param args        可选的高级参数(代理、身份验证、连接和TLS)
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param keys        用于匹配和返回键。 没有值的Args是返回键
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度
     */
    public static Status process(Args args,
                                 Node callingNode,
                                 Node calledNode,
                                 Args... keys) {
        return process(args, callingNode, calledNode, 0, Level.STUDY, keys);
    }

    /**
     * @param args        可选的高级参数(代理、身份验证、连接和TLS)
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param cancelAfter 接收到指定数目的匹配项后，取消查询请求
     * @param level       指定检索级别。默认使用PatientRoot、StudyRoot、PatientStudyOnly模型进行研究
     * @param keys        用于匹配和返回键。 没有值的Args是返回键
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度
     */
    public static Status process(Args args,
                                 Node callingNode,
                                 Node calledNode,
                                 int cancelAfter,
                                 Level level,
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
            if (null != level) {
                findSCU.addLevel(level.name());
            }

            for (Args p : keys) {
                addAttributes(findSCU.getKeys(), p);
            }
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

    private static FindSCU.InformationModel getInformationModel(Args options) {
        Object model = options.getInformationModel();
        if (model instanceof FindSCU.InformationModel) {
            return (FindSCU.InformationModel) model;
        }
        return FindSCU.InformationModel.StudyRoot;
    }

    public static void addAttributes(Attributes attrs, Args param) {
        int tag = param.getTag();
        String[] ss = param.getValues();
        VR vr = ElementDictionary.vrOf(tag, attrs.getPrivateCreator(tag));
        if (null == ss || ss.length == 0) {
            if (vr == VR.SQ) {
                attrs.newSequence(tag, 1).add(new Attributes(0));
            } else {
                attrs.setNull(tag, vr);
            }
        } else {
            attrs.setString(tag, vr, ss);
        }
    }

}
