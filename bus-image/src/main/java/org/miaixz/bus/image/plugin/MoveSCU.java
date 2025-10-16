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

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.ImageProgress;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.DimseRSPHandler;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.pdu.ExtendedNegotiation;
import org.miaixz.bus.image.metric.pdu.PresentationContext;
import org.miaixz.bus.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;

/**
 * The {@code MoveSCU} class implements a Service Class User (SCU) for the DICOM C-MOVE service. It sends a C-MOVE
 * request to a Service Class Provider (SCP) to initiate the transfer of DICOM instances to a specified destination
 * Application Entity.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MoveSCU extends Device implements AutoCloseable {

    /**
     * Default filter for attributes to be read from an input file.
     */
    private static final int[] DEF_IN_FILTER = { Tag.SOPInstanceUID, Tag.StudyInstanceUID, Tag.SeriesInstanceUID };
    /**
     * The Application Entity for this SCU.
     */
    private final ApplicationEntity ae = new ApplicationEntity("MOVESCU");
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
    private final transient AAssociateRQ rq = new AAssociateRQ();
    /**
     * The query keys for the C-MOVE request.
     */
    private final Attributes keys = new Attributes();
    /**
     * The overall status of the C-MOVE operation.
     */
    private final transient Status state;
    /**
     * The priority of the C-MOVE request.
     */
    private int priority;
    /**
     * The AE Title of the destination for the C-STORE sub-operations.
     */
    private String destination;
    /**
     * The information model for the query.
     */
    private InformationModel model;
    /**
     * A filter for attributes to be included from an input file.
     */
    private int[] inFilter = DEF_IN_FILTER;
    /**
     * The active DICOM association.
     */
    private transient Association as;
    /**
     * The number of milliseconds after which to cancel the request.
     */
    private int cancelAfter;
    /**
     * A flag to release the association eagerly on cancellation.
     */
    private boolean releaseEager;

    /**
     * Constructs a new {@code MoveSCU} instance with no progress handler.
     */
    public MoveSCU() {
        this(null);
    }

    /**
     * Constructs a new {@code MoveSCU} instance with the specified progress handler.
     *
     * @param progress The progress handler to be notified of status changes.
     */
    public MoveSCU(ImageProgress progress) {
        super("movescu");
        addConnection(conn);
        addApplicationEntity(ae);
        ae.addConnection(conn);
        state = new Status(progress);
    }

    /**
     * Sets the priority for the C-MOVE operation.
     *
     * @param priority The priority value (0=Medium, 1=High, 2=Low).
     */
    public final void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Sets a timeout in milliseconds after which the C-MOVE request will be cancelled.
     *
     * @param cancelAfter The timeout in milliseconds.
     */
    public void setCancelAfter(int cancelAfter) {
        this.cancelAfter = cancelAfter;
    }

    /**
     * Sets whether to release the association eagerly when a cancellation is triggered.
     *
     * @param releaseEager {@code true} to release eagerly, {@code false} otherwise.
     */
    public void setReleaseEager(boolean releaseEager) {
        this.releaseEager = releaseEager;
    }

    /**
     * Sets the information model for the C-MOVE operation.
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
     * Sets the destination Application Entity Title for the C-STORE sub-operations.
     *
     * @param destination The destination AE Title.
     */
    public final void setDestination(String destination) {
        this.destination = destination;
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
     * Gets the query key attributes.
     *
     * @return The query keys.
     */
    public Attributes getKeys() {
        return keys;
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
     * Performs a C-MOVE retrieve operation using query keys from a file.
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
     * Performs a C-MOVE retrieve operation using the currently configured keys.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void retrieve() throws IOException, InterruptedException {
        retrieve(keys);
    }

    /**
     * Performs the C-MOVE retrieve operation with the given keys.
     *
     * @param keys The query keys.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    private void retrieve(Attributes keys) throws IOException, InterruptedException {
        DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                ImageProgress p = state.getProgress();
                if (p != null) {
                    p.setAttributes(cmd);
                    if (p.isCancel()) {
                        try {
                            this.cancel(as);
                        } catch (IOException e) {
                            Logger.error("Cancel C-MOVE", e);
                        }
                    }
                }
            }
        };
        as.cmove(model.cuid, priority, keys, null, destination, rspHandler);
        if (cancelAfter > 0) {
            schedule(() -> {
                try {
                    rspHandler.cancel(as);
                    if (releaseEager) {
                        as.release();
                    }
                } catch (IOException e) {
                    Logger.error("Cancel after C-MOVE", e);
                }
            }, cancelAfter, TimeUnit.MILLISECONDS);
        }
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
     * Gets the current status of the C-MOVE operation.
     *
     * @return The status.
     */
    public Status getState() {
        return state;
    }

    /**
     * Enumeration of the supported DICOM Information Models for C-MOVE.
     */
    public enum InformationModel {

        /**
         * Patient Root Query/Retrieve Information Model - MOVE.
         */
        PatientRoot(UID.PatientRootQueryRetrieveInformationModelMove.uid, "STUDY"),
        /**
         * Study Root Query/Retrieve Information Model - MOVE.
         */
        StudyRoot(UID.StudyRootQueryRetrieveInformationModelMove.uid, "STUDY"),
        /**
         * Patient/Study Only Query/Retrieve Information Model - MOVE.
         */
        PatientStudyOnly(UID.PatientStudyOnlyQueryRetrieveInformationModelMove.uid, "STUDY"),
        /**
         * Composite Instance Root Retrieve - MOVE.
         */
        CompositeInstanceRoot(UID.CompositeInstanceRootRetrieveMove.uid, "IMAGE"),
        /**
         * Hanging Protocol Information Model - MOVE.
         */
        HangingProtocol(UID.HangingProtocolInformationModelMove.uid, null),
        /**
         * Color Palette Query/Retrieve Information Model - MOVE.
         */
        ColorPalette(UID.ColorPaletteQueryRetrieveInformationModelMove.uid, null);

        /**
         * The SOP Class UID for the information model.
         */
        final String cuid;
        /**
         * The default query/retrieve level for the model.
         */
        final String level;

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
