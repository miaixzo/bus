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
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ValidationResult;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.service.BasicCEchoSCP;
import org.miaixz.bus.image.metric.service.BasicMPPSSCP;
import org.miaixz.bus.image.metric.service.ImageServiceException;
import org.miaixz.bus.image.metric.service.ImageServiceRegistry;
import org.miaixz.bus.logger.Logger;

import java.io.File;
import java.io.IOException;

/**
 * The {@code MppsSCP} class implements a Service Class Provider (SCP) for the Modality Performed Procedure Step (MPPS)
 * SOP Class. It handles N-CREATE and N-SET requests to create and update MPPS instances, storing them to a specified
 * directory.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MppsSCP {

    /**
     * The main device for this SCP.
     */
    private final Device device = new Device("mppsscp");
    /**
     * The Application Entity that accepts associations.
     */
    private final ApplicationEntity ae = new ApplicationEntity(Symbol.STAR);
    /**
     * The network connection configuration.
     */
    private final Connection conn = new Connection();
    /**
     * The directory to store received MPPS objects.
     */
    private File storageDir;
    /**
     * The Information Object Definition (IOD) for validating N-CREATE requests.
     */
    private IOD mppsNCreateIOD;
    /**
     * The Information Object Definition (IOD) for validating N-SET requests.
     */
    private IOD mppsNSetIOD;

    /**
     * The core service implementation that handles MPPS DIMSE messages.
     */
    private final BasicMPPSSCP mppsSCP = new BasicMPPSSCP() {

        @Override
        protected Attributes create(Association as, Attributes rq, Attributes rqAttrs, Attributes rsp)
                throws ImageServiceException {
            return MppsSCP.this.create(as, rq, rqAttrs);
        }

        @Override
        protected Attributes set(Association as, Attributes rq, Attributes rqAttrs, Attributes rsp)
                throws ImageServiceException {
            return MppsSCP.this.set(as, rq, rqAttrs);
        }
    };

    /**
     * Constructs a new {@code MppsSCP} device, initializing the Application Entity and registering the MPPS and C-ECHO
     * services.
     */
    public MppsSCP() {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.setAssociationAcceptor(true);
        ae.addConnection(conn);
        ImageServiceRegistry serviceRegistry = new ImageServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(mppsSCP);
        ae.setDimseRQHandler(serviceRegistry);
    }

    /**
     * Gets the directory where MPPS objects are stored.
     *
     * @return The storage directory.
     */
    public File getStorageDirectory() {
        return storageDir;
    }

    /**
     * Sets the directory for storing received MPPS objects. If the directory does not exist, it will be created.
     *
     * @param storageDir The storage directory.
     */
    public void setStorageDirectory(File storageDir) {
        if (storageDir != null)
            storageDir.mkdirs();
        this.storageDir = storageDir;
    }

    /**
     * Sets the IOD for validating MPPS N-CREATE request attributes.
     *
     * @param mppsNCreateIOD The IOD for N-CREATE.
     */
    private void setMppsNCreateIOD(IOD mppsNCreateIOD) {
        this.mppsNCreateIOD = mppsNCreateIOD;
    }

    /**
     * Sets the IOD for validating MPPS N-SET request attributes.
     *
     * @param mppsNSetIOD The IOD for N-SET.
     */
    private void setMppsNSetIOD(IOD mppsNSetIOD) {
        this.mppsNSetIOD = mppsNSetIOD;
    }

    /**
     * Handles an N-CREATE request. It validates the attributes against the configured IOD and stores the new MPPS
     * object to a file if a storage directory is set.
     *
     * @param as      The active association.
     * @param rq      The N-CREATE request command.
     * @param rqAttrs The dataset attributes from the N-CREATE request.
     * @return {@code null} as no attributes are returned in the response body.
     * @throws ImageServiceException if validation fails or a storage error occurs.
     */
    private Attributes create(Association as, Attributes rq, Attributes rqAttrs) throws ImageServiceException {
        if (mppsNCreateIOD != null) {
            ValidationResult result = rqAttrs.validate(mppsNCreateIOD);
            if (!result.isValid())
                throw ImageServiceException.valueOf(result, rqAttrs);
        }
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
            Logger.warn(as + ": Failed to store MPPS:", e);
            throw new ImageServiceException(Status.ProcessingFailure, e);
        }
        return null;
    }

    /**
     * Handles an N-SET request. It validates the attributes, reads the existing MPPS object, merges the changes, and
     * stores the updated object.
     *
     * @param as      The active association.
     * @param rq      The N-SET request command.
     * @param rqAttrs The dataset attributes from the N-SET request.
     * @return {@code null} as no attributes are returned in the response body.
     * @throws ImageServiceException if validation fails, the object doesn't exist, or a storage error occurs.
     */
    private Attributes set(Association as, Attributes rq, Attributes rqAttrs) throws ImageServiceException {
        if (mppsNSetIOD != null) {
            ValidationResult result = rqAttrs.validate(mppsNSetIOD);
            if (!result.isValid())
                throw ImageServiceException.valueOf(result, rqAttrs);
        }
        if (storageDir == null)
            return null;
        String cuid = rq.getString(Tag.RequestedSOPClassUID);
        String iuid = rq.getString(Tag.RequestedSOPInstanceUID);
        File file = new File(storageDir, iuid);
        if (!file.exists())
            throw new ImageServiceException(Status.NoSuchObjectInstance).setUID(Tag.AffectedSOPInstanceUID, iuid);

        Logger.info("{}: M-UPDATE {}", as, file);
        Attributes data;
        try (ImageInputStream in = new ImageInputStream(file)) {
            data = in.readDataset();
        } catch (IOException e) {
            Logger.warn(as + ": Failed to read MPPS:", e);
            throw new ImageServiceException(Status.ProcessingFailure, e);
        }

        if (!"IN PROGRESS".equals(data.getString(Tag.PerformedProcedureStepStatus)))
            BasicMPPSSCP.mayNoLongerBeUpdated();

        data.addAll(rqAttrs);
        try (ImageOutputStream out = new ImageOutputStream(file)) {
            out.writeDataset(Attributes.createFileMetaInformation(iuid, cuid, UID.ExplicitVRLittleEndian.uid), data);
        } catch (IOException e) {
            Logger.warn(as + ": Failed to update MPPS:", e);
            throw new ImageServiceException(Status.ProcessingFailure, e);
        }
        return null;
    }

}
