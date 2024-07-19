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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.ImageParam;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.service.QueryRetrieveLevel;
import org.miaixz.bus.logger.Logger;

import java.text.MessageFormat;
/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class CFind {

    public static final ImageParam PatientID = new ImageParam(Tag.PatientID);
    public static final ImageParam IssuerOfPatientID = new ImageParam(Tag.IssuerOfPatientID);
    public static final ImageParam PatientName = new ImageParam(Tag.PatientName);
    public static final ImageParam PatientBirthDate = new ImageParam(Tag.PatientBirthDate);
    public static final ImageParam PatientSex = new ImageParam(Tag.PatientSex);

    public static final ImageParam StudyInstanceUID = new ImageParam(Tag.StudyInstanceUID);
    public static final ImageParam AccessionNumber = new ImageParam(Tag.AccessionNumber);
    public static final ImageParam IssuerOfAccessionNumberSequence =
            new ImageParam(Tag.IssuerOfAccessionNumberSequence);
    public static final ImageParam StudyID = new ImageParam(Tag.StudyID);
    public static final ImageParam ReferringPhysicianName =
            new ImageParam(Tag.ReferringPhysicianName);
    public static final ImageParam StudyDescription = new ImageParam(Tag.StudyDescription);
    public static final ImageParam StudyDate = new ImageParam(Tag.StudyDate);
    public static final ImageParam StudyTime = new ImageParam(Tag.StudyTime);

    public static final ImageParam SeriesInstanceUID = new ImageParam(Tag.SeriesInstanceUID);
    public static final ImageParam Modality = new ImageParam(Tag.Modality);
    public static final ImageParam SeriesNumber = new ImageParam(Tag.SeriesNumber);
    public static final ImageParam SeriesDescription = new ImageParam(Tag.SeriesDescription);

    public static final ImageParam SOPInstanceUID = new ImageParam(Tag.SOPInstanceUID);
    public static final ImageParam InstanceNumber = new ImageParam(Tag.InstanceNumber);

    /**
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param keys        用于匹配和返回键。 没有值的Args是返回键
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度信息
     */
    public static Status process(
            Node callingNode, Node calledNode, ImageParam... keys) {
        return process(null, callingNode, calledNode, 0, QueryRetrieveLevel.STUDY, keys);
    }

    /**
     * @param args        可选的高级参数(代理、身份验证、连接和TLS)
     * @param callingNode 调用DICOM节点的配置
     * @param calledNode  被调用的DICOM节点配置
     * @param keys        用于匹配和返回键。 没有值的Args是返回键
     * @return Status实例，其中包含DICOM响应，DICOM状态，错误消息和进度
     */
    public static Status process(
            Args args, Node callingNode, Node calledNode, ImageParam... keys) {
        return process(args, callingNode, calledNode, 0, QueryRetrieveLevel.STUDY, keys);
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
    public static Status process(
            Args args,
            Node callingNode,
            Node calledNode,
            int cancelAfter,
            QueryRetrieveLevel level,
            ImageParam... keys) {
        if (callingNode == null || calledNode == null) {
            throw new IllegalArgumentException("callingNode or calledNode cannot be null!");
        }

        Args options = args == null ? new Args() : args;

        try (FindSCU findSCU = new FindSCU()) {
            Connection remote = findSCU.getRemoteConnection();
            Connection conn = findSCU.getConnection();
            options.configureConnect(findSCU.getAAssociateRQ(), remote, calledNode);
            options.configureBind(findSCU.getApplicationEntity(), conn, callingNode);
            Centre service = new Centre(findSCU.getDevice());

            // configure
            options.configure(conn);
            options.configureTLS(conn, remote);

            findSCU.setInformationModel(
                    getInformationModel(options), options.getTsuidOrder(), options.getQueryOptions());
            if (level != null) {
                findSCU.addLevel(level.name());
            }

            Status dcmState = findSCU.getState();
            for (ImageParam p : keys) {
                addAttributes(findSCU.getKeys(), p);
                String[] values = p.getValues();
                if (values != null && values.length > 0) {
                    dcmState.addDicomMatchingKeys(p);
                }
            }
            findSCU.setCancelAfter(cancelAfter);
            findSCU.setPriority(options.getPriority());

            service.start();
            try {
                long t1 = System.currentTimeMillis();
                findSCU.open();
                long t2 = System.currentTimeMillis();
                findSCU.query();
                Builder.forceGettingAttributes(dcmState, findSCU);
                long t3 = System.currentTimeMillis();
                String timeMsg =
                        MessageFormat.format(
                                "DICOM C-Find connected in {2}ms from {0} to {1}. Query in {3}ms.",
                                findSCU.getAAssociateRQ().getCallingAET(),
                                findSCU.getAAssociateRQ().getCalledAET(),
                                t2 - t1,
                                t3 - t2);
                dcmState = Status.buildMessage(dcmState, timeMsg, null);
                dcmState.addProcessTime(t1, t2, t3);
                return dcmState;
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                Logger.error("findscu", e);
                Builder.forceGettingAttributes(findSCU.getState(), findSCU);
                return Status.buildMessage(findSCU.getState(), null, e);
            } finally {
                IoKit.close(findSCU);
                service.stop();
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            Logger.error("findscu", e);
            return Status.buildMessage(
                    new Status(Status.UnableToProcess,
                            "DICOM Find failed" + Symbol.COLON + Symbol.SPACE + e.getMessage(),
                            null),
                    null,
                    e);
        }
    }

    private static FindSCU.InformationModel getInformationModel(Args options) {
        Object model = options.getInformationModel();
        if (model instanceof FindSCU.InformationModel) {
            return (FindSCU.InformationModel) model;
        }
        return FindSCU.InformationModel.StudyRoot;
    }

    public static void addAttributes(Attributes attrs, ImageParam param) {
        int tag = param.getTag();
        String[] ss = param.getValues();
        VR vr = ElementDictionary.vrOf(tag, attrs.getPrivateCreator(tag));
        if (ss == null || ss.length == 0) {
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
