/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Commands;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.pdu.PresentationContext;
import org.miaixz.bus.image.metric.service.*;
import org.miaixz.bus.logger.Logger;

import java.io.File;
import java.io.IOException;

/**
 * The {@code IanSCP} class implements a Service Class Provider (SCP) for the Instance Availability Notification (IAN)
 * SOP Class. It listens for N-CREATE-RQ messages and, if configured, stores the notification attributes to a file.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IanSCP extends Device {

    /**
     * The Application Entity that accepts associations.
     */
    private final ApplicationEntity ae = new ApplicationEntity(Symbol.STAR);
    /**
     * The network connection configuration.
     */
    private final Connection conn = new Connection();
    /**
     * The directory to store received IAN objects.
     */
    private File storageDir;
    /**
     * The status code to be returned in the N-CREATE-RSP.
     */
    private int status;

    /**
     * The ImageService implementation that handles the N-CREATE-RQ for IAN.
     */
    private final ImageService ianSCP = new AbstractImageService(UID.InstanceAvailabilityNotification.uid) {

        @Override
        public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, Attributes data)
                throws IOException {
            if (dimse != Dimse.N_CREATE_RQ)
                throw new ImageServiceException(Status.UnrecognizedOperation);
            Attributes rsp = Commands.mkNCreateRSP(cmd, status);
            Attributes rspAttrs = IanSCP.this.create(as, cmd, data);
            as.tryWriteDimseRSP(pc, rsp, rspAttrs);
        }
    };

    /**
     * Constructs a new {@code IanSCP} device, initializing the Application Entity and registering the IAN and C-ECHO
     * services.
     */
    public IanSCP() {
        super("ianscp");
        addConnection(conn);
        addApplicationEntity(ae);
        ae.setAssociationAcceptor(true);
        ae.addConnection(conn);
        ImageServiceRegistry serviceRegistry = new ImageServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(ianSCP);
        ae.setDimseRQHandler(serviceRegistry);
    }

    /**
     * Gets the directory where IAN notifications are stored.
     *
     * @return The storage directory.
     */
    public File getStorageDirectory() {
        return storageDir;
    }

    /**
     * Sets the directory for storing received IAN notifications. If the directory does not exist, it will be created.
     *
     * @param storageDir The storage directory.
     */
    public void setStorageDirectory(File storageDir) {
        if (storageDir != null)
            storageDir.mkdirs();
        this.storageDir = storageDir;
    }

    /**
     * Sets the status code to be returned in the N-CREATE response.
     *
     * @param status The DICOM status code (e.g., {@link Status#Success}).
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Handles the creation and storage of the IAN object from an N-CREATE request. If a storage directory is
     * configured, the attributes are written to a file named after the SOP Instance UID.
     *
     * @param as      The active association.
     * @param rq      The N-CREATE request command.
     * @param rqAttrs The dataset attributes from the N-CREATE request.
     * @return {@code null} as no attributes are returned in the response body.
     * @throws ImageServiceException if an error occurs, such as a duplicate SOP instance.
     */
    private Attributes create(Association as, Attributes rq, Attributes rqAttrs) throws ImageServiceException {
        if (storageDir == null)
            return null;
        String cuid = rq.getString(Tag.AffectedSOPClassUID);
        String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
        File file = new File(storageDir, iuid);
        if (file.exists())
            throw new ImageServiceException(Status.DuplicateSOPinstance).setUID(Tag.AffectedSOPInstanceUID, iuid);

        Logger.info("{}: M-WRITE {}", as, file);
        try (ImageOutputStream out = new ImageOutputStream(file)) {
            out.writeDataset(Attributes.createFileMetaInformation(iuid, cuid, UID.ExplicitVRLittleEndian.uid), rqAttrs);
        } catch (IOException e) {
            Logger.warn(as + ": Failed to store Instance Available Notification:", e);
            throw new ImageServiceException(Status.ProcessingFailure, e);
        } finally {
            // The try-with-resources statement handles closing the stream.
        }
        return null;
    }

}
