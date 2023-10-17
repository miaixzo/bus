/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.image.plugin;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.image.*;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.io.ImageOutputStream;
import org.aoju.bus.image.metric.*;
import org.aoju.bus.image.metric.internal.pdu.Presentation;
import org.aoju.bus.image.metric.service.AbstractService;
import org.aoju.bus.image.metric.service.BasicCEchoSCP;
import org.aoju.bus.image.metric.service.ImageService;
import org.aoju.bus.image.metric.service.ServiceHandler;
import org.aoju.bus.logger.Logger;

import java.io.File;
import java.io.IOException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class IanSCP extends Device {

    private final ApplicationEntity ae = new ApplicationEntity(Symbol.STAR);
    private final Connection conn = new Connection();
    private File storageDir;
    private int status;

    private final ImageService ianSCP =
            new AbstractService(UID.InstanceAvailabilityNotificationSOPClass) {

                @Override
                public void onDimse(Association as, Presentation pc,
                                    Dimse dimse, Attributes cmd, Attributes data)
                        throws IOException {
                    if (dimse != Dimse.N_CREATE_RQ)
                        throw new ImageException(Status.UnrecognizedOperation);
                    Attributes rsp = Commands.mkNCreateRSP(cmd, status);
                    Attributes rspAttrs = IanSCP.this.create(as, cmd, data);
                    as.tryWriteDimseRSP(pc, rsp, rspAttrs);
                }
            };

    public IanSCP() {
        super("ianscp");
        addConnection(conn);
        addApplicationEntity(ae);
        ae.setAssociationAcceptor(true);
        ae.addConnection(conn);
        ServiceHandler serviceHandler = new ServiceHandler();
        serviceHandler.addService(new BasicCEchoSCP());
        serviceHandler.addService(ianSCP);
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

    public void setStatus(int status) {
        this.status = status;
    }

    private Attributes create(Association as, Attributes rq, Attributes rqAttrs)
            throws ImageException {
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
            Logger.warn(as + ": Failed to store Instance Available Notification:", e);
            throw new ImageException(Status.ProcessingFailure, e);
        } finally {
            IoKit.close(out);
        }
        return null;
    }

}
