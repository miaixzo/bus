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
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.IOD;
import org.miaixz.bus.image.galaxy.data.ValidationResult;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.metric.ApplicationEntity;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.ImageException;
import org.miaixz.bus.image.metric.service.BasicCEchoSCP;
import org.miaixz.bus.image.metric.service.BasicMPPSSCP;
import org.miaixz.bus.image.metric.service.ServiceHandler;
import org.miaixz.bus.logger.Logger;

import java.io.File;
import java.io.IOException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class MppsSCP {

    private final Device device = new Device("mppsscp");
    private final ApplicationEntity ae = new ApplicationEntity(Symbol.STAR);
    private final Connection conn = new Connection();
    private File storageDir;
    private IOD mppsNCreateIOD;
    private IOD mppsNSetIOD;

    private final BasicMPPSSCP mppsSCP = new BasicMPPSSCP() {

        @Override
        protected Attributes create(Association as, Attributes rq,
                                    Attributes rqAttrs, Attributes rsp) throws ImageException {
            return MppsSCP.this.create(as, rq, rqAttrs);
        }

        @Override
        protected Attributes set(Association as, Attributes rq, Attributes rqAttrs,
                                 Attributes rsp) throws ImageException {
            return MppsSCP.this.set(as, rq, rqAttrs);
        }
    };

    public MppsSCP() {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.setAssociationAcceptor(true);
        ae.addConnection(conn);
        ServiceHandler serviceHandler = new ServiceHandler();
        serviceHandler.addService(new BasicCEchoSCP());
        serviceHandler.addService(mppsSCP);
        ae.setDimseRQHandler(serviceHandler);
    }

    public File getStorageDirectory() {
        return storageDir;
    }

    public void setStorageDirectory(File storageDir) {
        if (null != storageDir)
            storageDir.mkdirs();
        this.storageDir = storageDir;
    }

    private void setMppsNCreateIOD(IOD mppsNCreateIOD) {
        this.mppsNCreateIOD = mppsNCreateIOD;
    }

    private void setMppsNSetIOD(IOD mppsNSetIOD) {
        this.mppsNSetIOD = mppsNSetIOD;
    }

    private Attributes create(Association as, Attributes rq, Attributes rqAttrs)
            throws ImageException {
        if (null != mppsNCreateIOD) {
            ValidationResult result = rqAttrs.validate(mppsNCreateIOD);
            if (!result.isValid())
                throw ImageException.valueOf(result, rqAttrs);
        }
        if (null == storageDir)
            return null;
        String cuid = rq.getString(Tag.AffectedSOPClassUID);
        String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
        File file = new File(storageDir, iuid);
        if (file.exists())
            throw new ImageException(Status.DuplicateSOPinstance).
                    setUID(Tag.AffectedSOPInstanceUID, iuid);
        ImageOutputStream out = null;
        Logger.info("{}: M-WRITE {}", as, file);
        try {
            out = new ImageOutputStream(file);
            out.writeDataset(
                    Attributes.createFileMetaInformation(iuid, cuid,
                            UID.ExplicitVRLittleEndian),
                    rqAttrs);
        } catch (IOException e) {
            Logger.warn(as + ": Failed to store MPPS:", e);
            throw new ImageException(Status.ProcessingFailure, e);
        } finally {
            IoKit.close(out);
        }
        return null;
    }

    private Attributes set(Association as, Attributes rq, Attributes rqAttrs)
            throws ImageException {
        if (null != mppsNSetIOD) {
            ValidationResult result = rqAttrs.validate(mppsNSetIOD);
            if (!result.isValid())
                throw ImageException.valueOf(result, rqAttrs);
        }
        if (null == storageDir)
            return null;
        String cuid = rq.getString(Tag.RequestedSOPClassUID);
        String iuid = rq.getString(Tag.RequestedSOPInstanceUID);
        File file = new File(storageDir, iuid);
        if (!file.exists())
            throw new ImageException(Status.NoSuchObjectInstance).
                    setUID(Tag.AffectedSOPInstanceUID, iuid);
        Logger.info("{}: M-UPDATE {}", as, file);
        Attributes data;
        ImageInputStream in = null;
        try {
            in = new ImageInputStream(file);
            data = in.readDataset(-1, -1);
        } catch (IOException e) {
            Logger.warn(as + ": Failed to read MPPS:", e);
            throw new ImageException(Status.ProcessingFailure, e);
        } finally {
            IoKit.close(in);
        }
        if (!"IN PROGRESS".equals(data.getString(Tag.PerformedProcedureStepStatus)))
            BasicMPPSSCP.mayNoLongerBeUpdated();

        data.addAll(rqAttrs);
        ImageOutputStream out = null;
        try {
            out = new ImageOutputStream(file);
            out.writeDataset(
                    Attributes.createFileMetaInformation(iuid, cuid, UID.ExplicitVRLittleEndian),
                    data);
        } catch (IOException e) {
            Logger.warn(as + ": Failed to update MPPS:", e);
            throw new ImageException(Status.ProcessingFailure, e);
        } finally {
            IoKit.close(out);
        }
        return null;
    }

}
