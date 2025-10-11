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
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.ImageProgress;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.DimseRSPHandler;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.net.PDVInputStream;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.pdu.ExtendedNegotiation;
import org.miaixz.bus.image.metric.pdu.PresentationContext;
import org.miaixz.bus.image.metric.pdu.RoleSelection;
import org.miaixz.bus.image.metric.service.BasicCStoreSCP;
import org.miaixz.bus.image.metric.service.ImageServiceException;
import org.miaixz.bus.image.metric.service.ImageServiceRegistry;
import org.miaixz.bus.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The {@code GetSCU} class implements a Service Class User (SCU) for the DICOM C-GET service. It handles the
 * negotiation and retrieval of DICOM objects from a Service Class Provider (SCP). The retrieved objects are stored
 * locally.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GetSCU implements AutoCloseable {

    /**
     * Default filter for attributes to be read from an input file.
     */
    private static final int[] DEF_IN_FILTER = { Tag.SOPInstanceUID, Tag.StudyInstanceUID, Tag.SeriesInstanceUID };
    /**
     * Temporary directory name for storing files during transfer.
     */
    private static final String TMP_DIR = "tmp";
    /**
     * The DICOM device associated with this SCU.
     */
    private final Device device = new Device("getscu");
    /**
     * The Application Entity used by this SCU.
     */
    private final ApplicationEntity ae;
    /**
     * The local network connection configuration.
     */
    private final Connection conn = new Connection();
    /**
     * The remote network connection configuration.
     */
    private final Connection remote = new Connection();
    /**
     * The A-ASSOCIATE-RQ message to be sent.
     */
    private final AAssociateRQ rq = new AAssociateRQ();
    /**
     * The query keys (matching attributes) for the C-GET request.
     */
    private final Attributes keys = new Attributes();
    /**
     * The overall status of the C-GET operation.
     */
    private final Status state;
    /**
     * The priority of the C-GET request.
     */
    private int priority;
    /**
     * The information model to be used for the query.
     */
    private InformationModel model;
    /**
     * The directory to store the retrieved DICOM files.
     */
    private File storageDir;
    /**
     * A filter for attributes to be included from an input file.
     */
    private int[] inFilter = DEF_IN_FILTER;
    /**
     * The active DICOM association.
     */
    private Association as;
    /**
     * The number of milliseconds after which to cancel the request.
     */
    private int cancelAfter;
    /**
     * The handler for DIMSE responses.
     */
    private DimseRSPHandler rspHandler;
    /**
     * The total size in bytes of all retrieved files.
     */
    private long totalSize = 0;
    /**
     * The C-STORE SCP implementation to handle incoming storage requests.
     */
    private final BasicCStoreSCP storageSCP = new BasicCStoreSCP(Symbol.STAR) {

        @Override
        protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data, Attributes rsp)
                throws IOException {
            if (storageDir == null) {
                return;
            }

            String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            String tsuid = pc.getTransferSyntax();
            File file = new File(storageDir, TMP_DIR + File.separator + iuid);
            try {
                storeTo(as, as.createFileMetaInformation(iuid, cuid, tsuid), data, file);
                totalSize += file.length();
                File rename = new File(storageDir, iuid);
                renameTo(as, file, rename);
                ImageProgress p = state.getProgress();
                if (p != null) {
                    p.setProcessedFile(rename);
                }
            } catch (Exception e) {
                throw new ImageServiceException(Status.ProcessingFailure, e);
            }
            updateProgress(as, null);
        }
    };

    /**
     * Constructs a new {@code GetSCU} instance with no progress handler.
     */
    public GetSCU() {
        this(null);
    }

    /**
     * Constructs a new {@code GetSCU} instance with the specified progress handler.
     *
     * @param progress The progress handler to be notified of status changes.
     */
    public GetSCU(ImageProgress progress) {
        ae = new ApplicationEntity("GETSCU");
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
        device.setDimseRQHandler(createServiceRegistry());
        state = new Status(progress);
    }

    /**
     * Stores the incoming DICOM data to a file.
     *
     * @param as   The association.
     * @param fmi  The File Meta Information.
     * @param data The input stream containing the DICOM data.
     * @param file The file to store the data in.
     * @throws IOException if an I/O error occurs.
     */
    public static void storeTo(Association as, Attributes fmi, PDVInputStream data, File file) throws IOException {
        Logger.debug("{}: M-WRITE {}", as, file);
        file.getParentFile().mkdirs();
        try (ImageOutputStream out = new ImageOutputStream(file)) {
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        }
    }

    /**
     * Renames a file.
     *
     * @param as   The association, used for logging.
     * @param from The source file.
     * @param dest The destination file.
     * @throws IOException if the rename operation fails.
     */
    private static void renameTo(Association as, File from, File dest) throws IOException {
        Logger.info("{}: M-RENAME {} to {}", as, from, dest);
        Builder.prepareToWriteFile(dest);
        if (!from.renameTo(dest))
            throw new IOException("Failed to rename " + from + " to " + dest);
    }

    /**
     * Gets the Application Entity of this SCU.
     *
     * @return The Application Entity.
     */
    public ApplicationEntity getApplicationEntity() {
        return ae;
    }

    /**
     * Gets the remote connection configuration.
     *
     * @return The remote connection.
     */
    public Connection getRemoteConnection() {
        return remote;
    }

    /**
     * Gets the A-ASSOCIATE-RQ message.
     *
     * @return The A-ASSOCIATE-RQ.
     */
    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    /**
     * Gets the current DICOM association.
     *
     * @return The association, or {@code null} if not connected.
     */
    public Association getAssociation() {
        return as;
    }

    /**
     * Gets the device associated with this SCU.
     *
     * @return The device.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Gets the query key attributes.
     *
     * @return The query keys.
     */
    public Attributes getKeys() {
        return keys;
    }

    /**
     * Creates the DICOM service registry and adds the C-STORE SCP service.
     *
     * @return The configured service registry.
     */
    private ImageServiceRegistry createServiceRegistry() {
        ImageServiceRegistry serviceRegistry = new ImageServiceRegistry();
        serviceRegistry.addDicomService(storageSCP);
        return serviceRegistry;
    }

    /**
     * Sets the directory where retrieved DICOM files will be stored.
     *
     * @param storageDir The storage directory.
     */
    public void setStorageDirectory(File storageDir) {
        if (storageDir != null) {
            if (storageDir.mkdirs()) {
                Logger.info("M-WRITE {}", storageDir);
            }
        }
        this.storageDir = storageDir;
    }

    /**
     * Sets the priority for the C-GET operation.
     *
     * @param priority The priority value (0=Medium, 1=High, 2=Low).
     */
    public final void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Sets a timeout in milliseconds after which the C-GET request will be cancelled.
     *
     * @param cancelAfter The timeout in milliseconds.
     */
    public void setCancelAfter(int cancelAfter) {
        this.cancelAfter = cancelAfter;
    }

    /**
     * Sets the information model for the C-GET operation.
     *
     * @param model      The information model to use.
     * @param tss        An array of transfer syntax UIDs to propose.
     * @param relational A flag indicating if relational queries are requested.
     */
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

    /**
     * Adds the Query/Retrieve Level key to the query attributes.
     *
     * @param s The level string (e.g., "STUDY", "SERIES").
     */
    public void addLevel(String s) {
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, s);
    }

    /**
     * Adds a key-value pair to the query attributes.
     *
     * @param tag The attribute tag.
     * @param ss  The string values for the attribute.
     */
    public void addKey(int tag, String... ss) {
        VR vr = ElementDictionary.vrOf(tag, keys.getPrivateCreator(tag));
        keys.setString(tag, vr, ss);
    }

    /**
     * Sets a filter for attributes to be read from an input file.
     *
     * @param inFilter An array of attribute tags to include.
     */
    public final void setInputFilter(int[] inFilter) {
        this.inFilter = inFilter;
    }

    /**
     * Adds an offered storage SOP class for the C-STORE sub-operations.
     *
     * @param cuid   The SOP Class UID.
     * @param tsuids The transfer syntax UIDs to offer for this SOP class.
     */
    public void addOfferedStorageSOPClass(String cuid, String... tsuids) {
        if (!rq.containsPresentationContextFor(cuid)) {
            rq.addRoleSelection(new RoleSelection(cuid, false, true));
        }
        rq.addPresentationContext(new PresentationContext(2 * rq.getNumberOfPresentationContexts() + 1, cuid, tsuids));
    }

    /**
     * Establishes a DICOM association with the remote AE.
     *
     * @throws IOException              if an I/O error occurs.
     * @throws InterruptedException     if the connection is interrupted.
     * @throws InternalException        if a configuration error occurs.
     * @throws GeneralSecurityException if a security error occurs.
     */
    public void open() throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        as = ae.connect(conn, remote, rq);
    }

    /**
     * Closes the DICOM association.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    @Override
    public void close() throws IOException, InterruptedException {
        if (as != null && as.isReadyForDataTransfer()) {
            as.waitForOutstandingRSP();
            as.release();
        }
    }

    /**
     * Performs a C-GET retrieve operation using query keys from a file.
     *
     * @param f The file containing the query keys.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void retrieve(File f) throws IOException, InterruptedException {
        Attributes attrs = new Attributes();
        try (ImageInputStream dis = new ImageInputStream(f)) {
            attrs.addSelected(dis.readDataset(), inFilter);
        }
        attrs.addAll(keys);
        retrieve(attrs);
    }

    /**
     * Performs a C-GET retrieve operation using the currently configured keys.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void retrieve() throws IOException, InterruptedException {
        retrieve(keys);
    }

    /**
     * Performs the C-GET retrieve operation with the given keys.
     *
     * @param keys The query keys.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    private void retrieve(Attributes keys) throws IOException, InterruptedException {
        final DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                updateProgress(as, cmd);
            }
        };

        retrieve(keys, rspHandler);
        if (cancelAfter > 0) {
            device.schedule(() -> {
                try {
                    rspHandler.cancel(as);
                } catch (IOException e) {
                    Logger.error("Cancel C-GET", e);
                }
            }, cancelAfter, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Performs a C-GET retrieve operation with a custom response handler.
     *
     * @param rspHandler The custom response handler.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void retrieve(DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        retrieve(keys, rspHandler);
    }

    /**
     * Performs the C-GET operation.
     *
     * @param keys       The query keys.
     * @param rspHandler The response handler.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    private void retrieve(Attributes keys, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        this.rspHandler = rspHandler;
        as.cget(model.getCuid(), priority, keys, null, rspHandler);
    }

    /**
     * Gets the local connection configuration.
     *
     * @return The connection.
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * Gets the current status of the C-GET operation.
     *
     * @return The status.
     */
    public Status getState() {
        return state;
    }

    /**
     * Gets the total size in bytes of all files retrieved so far.
     *
     * @return The total size.
     */
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * Stops the SCU by closing the association and shutting down the device's executors.
     */
    public void stop() {
        try {
            close();
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        Builder.shutdown((ExecutorService) device.getExecutor());
        Builder.shutdown(device.getScheduledExecutor());
    }

    /**
     * Updates the progress handler with the latest command attributes and checks for cancellation.
     *
     * @param as  The association.
     * @param cmd The DIMSE command attributes.
     */
    private void updateProgress(Association as, Attributes cmd) {
        ImageProgress p = state.getProgress();
        if (p != null) {
            p.setAttributes(cmd);
            if (p.isCancel() && rspHandler != null) {
                try {
                    rspHandler.cancel(as);
                } catch (IOException e) {
                    Logger.error("Cancel C-GET", e);
                }
            }
        }
    }

    /**
     * Enumeration of the supported DICOM Information Models for C-GET.
     */
    public enum InformationModel {

        /**
         * Patient Root Query/Retrieve Information Model - GET.
         */
        PatientRoot(UID.PatientRootQueryRetrieveInformationModelGet.uid, "STUDY"),
        /**
         * Study Root Query/Retrieve Information Model - GET.
         */
        StudyRoot(UID.StudyRootQueryRetrieveInformationModelGet.uid, "STUDY"),
        /**
         * Patient/Study Only Query/Retrieve Information Model - GET.
         */
        PatientStudyOnly(UID.PatientStudyOnlyQueryRetrieveInformationModelGet.uid, "STUDY"),
        /**
         * Composite Instance Root Retrieve - GET.
         */
        CompositeInstanceRoot(UID.CompositeInstanceRootRetrieveGet.uid, "IMAGE"),
        /**
         * Composite Instance Retrieve Without Bulk Data - GET.
         */
        WithoutBulkData(UID.CompositeInstanceRetrieveWithoutBulkDataGet.uid, null),
        /**
         * Hanging Protocol Information Model - GET.
         */
        HangingProtocol(UID.HangingProtocolInformationModelGet.uid, null),
        /**
         * Color Palette Query/Retrieve Information Model - GET.
         */
        ColorPalette(UID.ColorPaletteQueryRetrieveInformationModelGet.uid, null);

        /**
         * The default query/retrieve level for the model.
         */
        final String level;
        /**
         * The SOP Class UID for the information model.
         */
        private final String cuid;

        /**
         * Constructs a new InformationModel.
         *
         * @param cuid  The SOP Class UID.
         * @param level The default query/retrieve level.
         */
        InformationModel(String cuid, String level) {
            this.cuid = cuid;
            this.level = level;
        }

        /**
         * Gets the SOP Class UID of the information model.
         *
         * @return The SOP Class UID.
         */
        public String getCuid() {
            return cuid;
        }
    }

}
