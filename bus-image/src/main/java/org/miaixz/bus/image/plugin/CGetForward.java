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
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.EditorContext;
import org.miaixz.bus.image.galaxy.ImageProgress;
import org.miaixz.bus.image.galaxy.ProgressStatus;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.metric.*;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.net.InputStreamDataWriter;
import org.miaixz.bus.image.metric.net.PDVInputStream;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.pdu.ExtendedNegotiation;
import org.miaixz.bus.image.metric.pdu.PresentationContext;
import org.miaixz.bus.image.metric.pdu.RoleSelection;
import org.miaixz.bus.image.metric.service.BasicCStoreSCP;
import org.miaixz.bus.image.metric.service.ImageServiceException;
import org.miaixz.bus.image.metric.service.ImageServiceRegistry;
import org.miaixz.bus.image.nimble.stream.BytesWithImageDescriptor;
import org.miaixz.bus.image.nimble.stream.ImageAdapter;
import org.miaixz.bus.image.nimble.stream.ImageAdapter.AdaptTransferSyntax;
import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class CGetForward implements AutoCloseable {

    private final Device device = new Device("getscu");
    private final ApplicationEntity ae;
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private final Attributes keys = new Attributes();
    private final StreamSCU streamSCU;
    private final Args args;
    private final Centre streamSCUService;
    private int priority;
    private final BasicCStoreSCP storageSCP = new BasicCStoreSCP("*") {
        @Override
        protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data, Attributes rsp)
                throws IOException {

            ImageProgress p = streamSCU.getState().getProgress();
            if (p != null) {
                if (p.isCancel()) {
                    IoKit.close(CGetForward.this);
                    return;
                }
            }

            try {
                String cuid = rq.getString(Tag.AffectedSOPClassUID);
                String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
                String tsuid = pc.getTransferSyntax();

                if (streamSCU.hasAssociation()) {
                    // Handle dynamically new SOPClassUID
                    Set<String> tss = streamSCU.getTransferSyntaxesFor(cuid);
                    if (!tss.contains(tsuid)) {
                        streamSCU.close(true);
                    }

                    // Add Presentation Context for the association
                    streamSCU.addData(cuid, tsuid);

                    if (!streamSCU.isReadyForDataTransfer()) {
                        // If connection has been closed just reopen
                        streamSCU.open();
                    }
                } else {
                    streamSCUService.start();
                    // Add Presentation Context for the association
                    streamSCU.addData(cuid, tsuid);
                    streamSCU.open();
                }

                ImageInputStream in = null;
                try {
                    if (!streamSCU.isReadyForDataTransfer()) {
                        throw new IllegalStateException("Association not ready for transfer.");
                    }
                    DataWriter dataWriter;
                    AdaptTransferSyntax syntax = new AdaptTransferSyntax(tsuid,
                            streamSCU.selectTransferSyntax(cuid, tsuid));
                    if ((args == null || !args.hasEditors()) && syntax.getRequested().equals(tsuid)) {
                        dataWriter = new InputStreamDataWriter(data);
                    } else {
                        EditorContext context = new EditorContext(syntax.getOriginal(), Node.buildRemoteDicomNode(as),
                                streamSCU.getRemoteDicomNode());
                        in = new ImageInputStream(data, tsuid);
                        in.setIncludeBulkData(ImageInputStream.IncludeBulkData.URI);
                        Attributes attributes = in.readDataset();
                        if (args != null && args.hasEditors()) {
                            args.getEditors().forEach(e -> e.apply(attributes, context));
                            iuid = attributes.getString(Tag.SOPInstanceUID);
                            cuid = attributes.getString(Tag.SOPClassUID);
                        }

                        if (context.getAbort() == EditorContext.Abort.FILE_EXCEPTION) {
                            data.skipAll();
                            throw new IllegalStateException(context.getAbortMessage());
                        } else if (context.getAbort() == EditorContext.Abort.CONNECTION_EXCEPTION) {
                            as.abort();
                            throw new AbortException("DICOM associtation abort. " + context.getAbortMessage());
                        }

                        BytesWithImageDescriptor desc = ImageAdapter.imageTranscode(attributes, syntax, context);
                        dataWriter = ImageAdapter.buildDataWriter(attributes, syntax, context.getEditable(), desc);
                    }

                    streamSCU.cstore(cuid, iuid, priority, dataWriter, syntax.getSuitable());
                } catch (AbortException e) {
                    Builder.notifyProgession(streamSCU.getState(), rq.getString(Tag.AffectedSOPInstanceUID),
                            rq.getString(Tag.AffectedSOPClassUID), Status.ProcessingFailure, ProgressStatus.FAILED,
                            streamSCU.getNumberOfSuboperations());
                    throw e;
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    Logger.error("Error when forwarding to the final destination", e);
                    Builder.notifyProgession(streamSCU.getState(), rq.getString(Tag.AffectedSOPInstanceUID),
                            rq.getString(Tag.AffectedSOPClassUID), Status.ProcessingFailure, ProgressStatus.FAILED,
                            streamSCU.getNumberOfSuboperations());
                } finally {
                    IoKit.close(in);
                }

            } catch (Exception e) {
                throw new ImageServiceException(Status.ProcessingFailure, e);
            }
        }

        class AbortException extends IllegalStateException {
            private static final long serialVersionUID = -1153741718853819887L;

            public AbortException(String s) {
                super(s);
            }
        }
    };
    private InformationModel model;
    private Association as;

    /**
     * @param args            the optional advanced parameters (proxy, authentication, connection and TLS) for the final
     *                        destination
     * @param callingNode     the calling DICOM node configuration
     * @param destinationNode the final DICOM node configuration
     * @param progress        the progress handler
     * @throws IOException
     */
    public CGetForward(Args args, Node callingNode, Node destinationNode, ImageProgress progress) throws IOException {
        this.args = args;
        this.ae = new ApplicationEntity("GETSCU");
        this.device.addConnection(conn);
        this.device.addApplicationEntity(ae);
        this.ae.addConnection(conn);
        this.device.setDimseRQHandler(createServiceRegistry());
        this.streamSCU = new StreamSCU(args, callingNode, destinationNode, progress);
        this.streamSCUService = new Centre(streamSCU.getDevice());
    }

    /**
     * @param callingNode     the calling DICOM node configuration
     * @param calledNode      the called DICOM node configuration
     * @param destinationNode the final destination DICOM node configuration
     * @param progress        the progress handler
     * @param studyUID        the study instance UID to retrieve
     * @return The DicomSate instance which contains the DICOM response, the DICOM status, the error message and the
     *         progression.
     */
    public static Status processStudy(Node callingNode, Node calledNode, Node destinationNode, ImageProgress progress,
            String studyUID) {
        return process(null, null, callingNode, calledNode, destinationNode, progress, "STUDY", studyUID);
    }

    /**
     * @param args            the C-GET optional advanced parameters (proxy, authentication, connection and TLS)
     * @param forwardParams   the C-Store optional advanced parameters (proxy, authentication, connection and TLS)
     * @param callingNode     the calling DICOM node configuration
     * @param calledNode      the called DICOM node configuration
     * @param destinationNode the final destination DICOM node configuration
     * @param progress        the progress handler
     * @param studyUID        the study instance UID to retrieve
     * @return The DicomSate instance which contains the DICOM response, the DICOM status, the error message and the
     *         progression.
     */
    public static Status processStudy(Args args, Args forwardParams, Node callingNode, Node calledNode,
            Node destinationNode, ImageProgress progress, String studyUID) {
        return process(args, forwardParams, callingNode, calledNode, destinationNode, progress, "STUDY", studyUID);
    }

    /**
     * @param getParams       the C-GET optional advanced parameters (proxy, authentication, connection and TLS)
     * @param forwardParams   the C-Store optional advanced parameters (proxy, authentication, connection and TLS)
     * @param callingNode     the calling DICOM node configuration
     * @param calledNode      the called DICOM node configuration
     * @param destinationNode the final destination DICOM node configuration
     * @param progress        the progress handler
     * @param seriesUID       the series instance UID to retrieve
     * @return The DicomSate instance which contains the DICOM response, the DICOM status, the error message and the
     *         progression.
     */
    public static Status processSeries(Args getParams, Args forwardParams, Node callingNode, Node calledNode,
            Node destinationNode, ImageProgress progress, String seriesUID) {
        return process(getParams, forwardParams, callingNode, calledNode, destinationNode, progress, "SERIES",
                seriesUID);
    }

    private static Status process(Args args, Args forwardParams, Node callingNode, Node calledNode,
            Node destinationNode, ImageProgress progress, String queryRetrieveLevel, String queryUID) {
        if (callingNode == null || calledNode == null || destinationNode == null) {
            throw new IllegalArgumentException("callingNode, calledNode or destinationNode cannot be null!");
        }
        Args options = args == null ? new Args() : args;

        try {
            CGetForward forward = new CGetForward(forwardParams, callingNode, destinationNode, progress);
            Connection remote = forward.getRemoteConnection();
            Connection conn = forward.getConnection();
            options.configureConnect(forward.getAAssociateRQ(), remote, calledNode);
            options.configureBind(forward.getApplicationEntity(), conn, callingNode);
            Centre service = new Centre(forward.getDevice());

            // configure
            options.configure(conn);
            options.configureTLS(conn, remote);

            forward.setPriority(options.getPriority());

            forward.setInformationModel(getInformationModel(options), options.getTsuidOrder(),
                    options.getQueryOptions().contains(QueryOption.RELATIONAL));

            configureRelatedSOPClass(forward, null);

            if ("SERIES".equals(queryRetrieveLevel)) {
                forward.addKey(Tag.QueryRetrieveLevel, "SERIES");
                forward.addKey(Tag.SeriesInstanceUID, queryUID);
            } else if ("STUDY".equals(queryRetrieveLevel)) {
                forward.addKey(Tag.QueryRetrieveLevel, "STUDY");
                forward.addKey(Tag.StudyInstanceUID, queryUID);
            } else {
                throw new IllegalArgumentException(queryRetrieveLevel + " is not supported as query retrieve level!");
            }

            service.start();
            try {
                Status dcmState = forward.getState();
                long t1 = System.currentTimeMillis();
                forward.open();
                long t2 = System.currentTimeMillis();
                forward.retrieve();
                Builder.forceGettingAttributes(dcmState, forward);
                long t3 = System.currentTimeMillis();
                String timeMsg = MessageFormat.format(
                        "DICOM C-GET connected in {2}ms from {0} to {1}. Get files in {3}ms.",
                        forward.getAAssociateRQ().getCallingAET(), forward.getAAssociateRQ().getCalledAET(), t2 - t1,
                        t3 - t2);
                return Status.buildMessage(dcmState, timeMsg, null);
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                Logger.error("getscu", e);
                Builder.forceGettingAttributes(forward.getState(), forward);
                return Status.buildMessage(forward.getState(), null, e);
            } finally {
                IoKit.close(forward);
                service.stop();
                forward.getStreamSCUService().stop();
            }
        } catch (Exception e) {
            Logger.error("getscu", e);
            return new Status(Status.UnableToProcess, "DICOM Get failed" + Symbol.COLON + Symbol.SPACE + e.getMessage(),
                    null);
        }
    }

    private static void configureRelatedSOPClass(CGetForward getSCU, URL url) {
        Properties p = new Properties();
        try {
            if (url != null) {
                p.load(url.openStream());
            } else {
                url = ResourceKit.getResourceUrl("sop-classes-tcs.properties", CGet.class);
                p.load(url.openStream());
            }
        } catch (IOException e) {
            Logger.error("Cannot read sop-classes", e);
        }

        for (Entry<Object, Object> entry : p.entrySet()) {
            configureStorageSOPClass(getSCU, (String) entry.getKey(), (String) entry.getValue());
        }
    }

    private static void configureStorageSOPClass(CGetForward getSCU, String cuid, String tsuids) {
        String[] ts = StringKit.splitToArray(tsuids, ";");
        for (int i = 0; i < ts.length; i++) {
            ts[i] = UID.toUID(ts[i]);
        }
        getSCU.addOfferedStorageSOPClass(UID.toUID(cuid), ts);
    }

    private static InformationModel getInformationModel(Args options) {
        Object model = options.getInformationModel();
        if (model instanceof InformationModel) {
            return (InformationModel) model;
        }
        return InformationModel.StudyRoot;
    }

    public ApplicationEntity getApplicationEntity() {
        return ae;
    }

    public Connection getRemoteConnection() {
        return remote;
    }

    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    public Association getAssociation() {
        return as;
    }

    public Device getDevice() {
        return device;
    }

    public Attributes getKeys() {
        return keys;
    }

    private ImageServiceRegistry createServiceRegistry() {
        ImageServiceRegistry serviceRegistry = new ImageServiceRegistry();
        serviceRegistry.addDicomService(storageSCP);
        return serviceRegistry;
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final void setInformationModel(InformationModel model, String[] tss, boolean relational) {
        this.model = model;
        rq.addPresentationContext(new PresentationContext(1, model.cuid, tss));
        if (relational) {
            rq.addExtendedNegotiation(new ExtendedNegotiation(model.cuid, new byte[] { 1 }));
        }
        if (model.level != null) {
            addLevel(model.level);
        }
    }

    public void addLevel(String s) {
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, s);
    }

    public void addKey(int tag, String... ss) {
        VR vr = ElementDictionary.vrOf(tag, keys.getPrivateCreator(tag));
        keys.setString(tag, vr, ss);
    }

    public void addOfferedStorageSOPClass(String cuid, String... tsuids) {
        if (!rq.containsPresentationContextFor(cuid)) {
            rq.addRoleSelection(new RoleSelection(cuid, false, true));
        }
        rq.addPresentationContext(new PresentationContext(2 * rq.getNumberOfPresentationContexts() + 1, cuid, tsuids));
    }

    public void open() throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        as = ae.connect(conn, remote, rq);
    }

    @Override
    public void close() throws IOException, InterruptedException {
        if (as != null && as.isReadyForDataTransfer()) {
            as.waitForOutstandingRSP();
            as.release();
        }
        streamSCU.close(true);
    }

    public void retrieve() throws IOException, InterruptedException {
        retrieve(keys);
    }

    private void retrieve(Attributes keys) throws IOException, InterruptedException {
        DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                ImageProgress p = streamSCU.getState().getProgress();
                if (p != null) {
                    // Set only the initial state
                    if (streamSCU.getNumberOfSuboperations() == 0) {
                        streamSCU.setNumberOfSuboperations(Builder.getTotalOfSuboperations(cmd));
                    }
                    if (p.isCancel()) {
                        try {
                            this.cancel(as);
                        } catch (IOException e) {
                            Logger.error("Cancel C-GET", e);
                        }
                    }
                }
            }
        };

        retrieve(keys, rspHandler);
    }

    private void retrieve(Attributes keys, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        as.cget(model.cuid, priority, keys, null, rspHandler);
    }

    public Connection getConnection() {
        return conn;
    }

    public Centre getStreamSCUService() {
        return streamSCUService;
    }

    public StreamSCU getStreamSCU() {
        return streamSCU;
    }

    public Status getState() {
        return streamSCU.getState();
    }

    public enum InformationModel {
        PatientRoot(UID.PatientRootQueryRetrieveInformationModelGet.uid, "STUDY"),
        StudyRoot(UID.StudyRootQueryRetrieveInformationModelGet.uid, "STUDY"),
        PatientStudyOnly(UID.PatientStudyOnlyQueryRetrieveInformationModelGet.uid, "STUDY"),
        CompositeInstanceRoot(UID.CompositeInstanceRootRetrieveGet.uid, "IMAGE"),
        WithoutBulkData(UID.CompositeInstanceRetrieveWithoutBulkDataGet.uid, null),
        HangingProtocol(UID.HangingProtocolInformationModelGet.uid, null),
        ColorPalette(UID.ColorPaletteQueryRetrieveInformationModelGet.uid, null);

        final String cuid;
        final String level;

        InformationModel(String cuid, String level) {
            this.cuid = cuid;
            this.level = level;
        }
    }

}
