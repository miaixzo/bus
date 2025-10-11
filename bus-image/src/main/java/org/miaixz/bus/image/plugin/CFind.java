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
 * The {@code CFind} class provides a simple way to perform a DICOM C-FIND operation. It encapsulates the setup and
 * execution of a {@link FindSCU} instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CFind {

    /**
     * Represents the Patient ID (0010,0020) DICOM tag.
     */
    public static final ImageParam PatientID = new ImageParam(Tag.PatientID);
    /**
     * Represents the Issuer of Patient ID (0010,0021) DICOM tag.
     */
    public static final ImageParam IssuerOfPatientID = new ImageParam(Tag.IssuerOfPatientID);
    /**
     * Represents the Patient's Name (0010,0010) DICOM tag.
     */
    public static final ImageParam PatientName = new ImageParam(Tag.PatientName);
    /**
     * Represents the Patient's Birth Date (0010,0030) DICOM tag.
     */
    public static final ImageParam PatientBirthDate = new ImageParam(Tag.PatientBirthDate);
    /**
     * Represents the Patient's Sex (0010,0040) DICOM tag.
     */
    public static final ImageParam PatientSex = new ImageParam(Tag.PatientSex);

    /**
     * Represents the Study Instance UID (0020,000D) DICOM tag.
     */
    public static final ImageParam StudyInstanceUID = new ImageParam(Tag.StudyInstanceUID);
    /**
     * Represents the Accession Number (0008,0050) DICOM tag.
     */
    public static final ImageParam AccessionNumber = new ImageParam(Tag.AccessionNumber);
    /**
     * Represents the Issuer of Accession Number Sequence (0008,0051) DICOM tag.
     */
    public static final ImageParam IssuerOfAccessionNumberSequence = new ImageParam(
            Tag.IssuerOfAccessionNumberSequence);
    /**
     * Represents the Study ID (0020,0010) DICOM tag.
     */
    public static final ImageParam StudyID = new ImageParam(Tag.StudyID);
    /**
     * Represents the Referring Physician's Name (0008,0090) DICOM tag.
     */
    public static final ImageParam ReferringPhysicianName = new ImageParam(Tag.ReferringPhysicianName);
    /**
     * Represents the Study Description (0008,1030) DICOM tag.
     */
    public static final ImageParam StudyDescription = new ImageParam(Tag.StudyDescription);
    /**
     * Represents the Study Date (0008,0020) DICOM tag.
     */
    public static final ImageParam StudyDate = new ImageParam(Tag.StudyDate);
    /**
     * Represents the Study Time (0008,0030) DICOM tag.
     */
    public static final ImageParam StudyTime = new ImageParam(Tag.StudyTime);

    /**
     * Represents the Series Instance UID (0020,000E) DICOM tag.
     */
    public static final ImageParam SeriesInstanceUID = new ImageParam(Tag.SeriesInstanceUID);
    /**
     * Represents the Modality (0008,0060) DICOM tag.
     */
    public static final ImageParam Modality = new ImageParam(Tag.Modality);
    /**
     * Represents the Series Number (0020,0011) DICOM tag.
     */
    public static final ImageParam SeriesNumber = new ImageParam(Tag.SeriesNumber);
    /**
     * Represents the Series Description (0008,103E) DICOM tag.
     */
    public static final ImageParam SeriesDescription = new ImageParam(Tag.SeriesDescription);
    /**
     * Represents the Series Date (0008,0021) DICOM tag.
     */
    public static final ImageParam SeriesDate = new ImageParam(Tag.SeriesDate);
    /**
     * Represents the Series Time (0008,0031) DICOM tag.
     */
    public static final ImageParam SeriesTime = new ImageParam(Tag.SeriesTime);

    /**
     * Represents the SOP Instance UID (0008,0018) DICOM tag.
     */
    public static final ImageParam SOPInstanceUID = new ImageParam(Tag.SOPInstanceUID);
    /**
     * Represents the Instance Number (0020,0013) DICOM tag.
     */
    public static final ImageParam InstanceNumber = new ImageParam(Tag.InstanceNumber);
    /**
     * Represents the SOP Class UID (0008,0016) DICOM tag.
     */
    public static final ImageParam SopClassUID = new ImageParam(Tag.SOPClassUID);

    /**
     * Performs a DICOM C-FIND operation with default settings.
     *
     * @param callingNode the configuration of the calling DICOM node.
     * @param calledNode  the configuration of the called DICOM node.
     * @param keys        the matching and return keys. Keys without values are treated as return keys.
     * @return a {@link Status} instance containing the DICOM response, status, error message, and progress information.
     */
    public static Status process(Node callingNode, Node calledNode, ImageParam... keys) {
        return process(null, callingNode, calledNode, 0, QueryRetrieveLevel.STUDY, keys);
    }

    /**
     * Performs a DICOM C-FIND operation with advanced options.
     *
     * @param args        optional advanced parameters (proxy, authentication, connection, and TLS).
     * @param callingNode the configuration of the calling DICOM node.
     * @param calledNode  the configuration of the called DICOM node.
     * @param keys        the matching and return keys. Keys without values are treated as return keys.
     * @return a {@link Status} instance containing the DICOM response, status, error message, and progress.
     */
    public static Status process(Args args, Node callingNode, Node calledNode, ImageParam... keys) {
        return process(args, callingNode, calledNode, 0, QueryRetrieveLevel.STUDY, keys);
    }

    /**
     * Performs a DICOM C-FIND operation with advanced options and cancellation settings.
     *
     * @param args        optional advanced parameters (proxy, authentication, connection, and TLS).
     * @param callingNode the configuration of the calling DICOM node.
     * @param calledNode  the configuration of the called DICOM node.
     * @param cancelAfter cancels the query request after receiving a specified number of matches.
     * @param level       specifies the retrieve level. Defaults to STUDY for PatientRoot, StudyRoot, and
     *                    PatientStudyOnly models.
     * @param keys        the matching and return keys. Keys without values are treated as return keys.
     * @return a {@link Status} instance containing the DICOM response, status, error message, and progress.
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
                    getInformationModel(options),
                    options.getTsuidOrder(),
                    options.getQueryOptions());
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
                String timeMsg = MessageFormat.format(
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
                            "DICOM Find failed" + Symbol.COLON + Symbol.SPACE + e.getMessage(), null),
                    null,
                    e);
        }
    }

    /**
     * Retrieves the information model from the given options.
     *
     * @param options the command-line arguments or options.
     * @return the {@link FindSCU.InformationModel}, defaulting to {@code StudyRoot}.
     */
    private static FindSCU.InformationModel getInformationModel(Args options) {
        Object model = options.getInformationModel();
        if (model instanceof FindSCU.InformationModel) {
            return (FindSCU.InformationModel) model;
        }
        return FindSCU.InformationModel.StudyRoot;
    }

    /**
     * Adds a DICOM attribute to an {@link Attributes} object based on an {@link ImageParam}. If the parameter has no
     * values, a null attribute is added. For sequence tags (VR=SQ), an empty item is added.
     *
     * @param attrs the {@link Attributes} object to which the attribute will be added.
     * @param param the {@link ImageParam} containing the tag and values.
     */
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
