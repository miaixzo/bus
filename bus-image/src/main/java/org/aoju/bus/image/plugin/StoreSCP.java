/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.image.plugin;

import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.image.Device;
import org.aoju.bus.image.Format;
import org.aoju.bus.image.Status;
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.VR;
import org.aoju.bus.image.galaxy.io.DicomInputStream;
import org.aoju.bus.image.galaxy.io.DicomOutputStream;
import org.aoju.bus.image.metric.ApplicationEntity;
import org.aoju.bus.image.metric.Association;
import org.aoju.bus.image.metric.Connection;
import org.aoju.bus.image.metric.PDVInputStream;
import org.aoju.bus.image.metric.pdu.PresentationContext;
import org.aoju.bus.image.metric.service.BasicCEchoSCP;
import org.aoju.bus.image.metric.service.BasicCStoreSCP;
import org.aoju.bus.image.metric.service.DicomServiceException;
import org.aoju.bus.image.metric.service.DicomServiceRegistry;
import org.aoju.bus.logger.Logger;

import java.io.File;
import java.io.IOException;

/**
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
public class StoreSCP {

    private static final String PART_EXT = ".part";
    private final Device device = new Device("storescp");
    private final ApplicationEntity ae = new ApplicationEntity("*");
    private final Connection conn = new Connection();
    private File storageDir;
    private Format filePathFormat;
    private int status;
    private int responseDelay;
    private final BasicCStoreSCP cstoreSCP = new BasicCStoreSCP("*") {

        @Override
        protected void store(Association as, PresentationContext pc,
                             Attributes rq, PDVInputStream data, Attributes rsp)
                throws IOException {
            try {
                rsp.setInt(Tag.Status, VR.US, status);
                if (storageDir == null)
                    return;

                String cuid = rq.getString(Tag.AffectedSOPClassUID);
                String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
                String tsuid = pc.getTransferSyntax();
                File file = new File(storageDir, iuid + PART_EXT);
                try {
                    storeTo(as, as.createFileMetaInformation(iuid, cuid, tsuid),
                            data, file);
                    renameTo(as, file, new File(storageDir,
                            filePathFormat == null
                                    ? iuid
                                    : filePathFormat.format(parse(file))));
                } catch (Exception e) {
                    deleteFile(as, file);
                    throw new DicomServiceException(Status.ProcessingFailure, e);
                }
            } finally {
                if (responseDelay > 0)
                    try {
                        Thread.sleep(responseDelay);
                    } catch (InterruptedException ignore) {
                    }
            }
        }

    };

    public StoreSCP() {
        device.setDimseRQHandler(createServiceRegistry());
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.setAssociationAcceptor(true);
        ae.addConnection(conn);
    }

    private static void renameTo(Association as, File from, File dest)
            throws IOException {
        Logger.info("{}: M-RENAME {} to {}", as, from, dest);
        if (!dest.getParentFile().mkdirs())
            dest.delete();
        if (!from.renameTo(dest))
            throw new IOException("Failed to rename " + from + " to " + dest);
    }

    private static Attributes parse(File file) throws IOException {
        DicomInputStream in = new DicomInputStream(file);
        try {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            return in.readDataset(-1, Tag.PixelData);
        } finally {
            IoUtils.close(in);
        }
    }

    private static void deleteFile(Association as, File file) {
        if (file.delete())
            Logger.info("{}: M-DELETE {}", as, file);
        else
            Logger.warn("{}: M-DELETE {} failed!", as, file);
    }

    private void storeTo(Association as, Attributes fmi,
                         PDVInputStream data, File file) throws IOException {
        Logger.info("{}: M-WRITE {}", as, file);
        file.getParentFile().mkdirs();
        DicomOutputStream out = new DicomOutputStream(file);
        try {
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        } finally {
            IoUtils.close(out);
        }
    }

    private DicomServiceRegistry createServiceRegistry() {
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(cstoreSCP);
        return serviceRegistry;
    }

    public void setStorageDirectory(File storageDir) {
        if (storageDir != null)
            storageDir.mkdirs();
        this.storageDir = storageDir;
    }

    public void setStorageFilePathFormat(String pattern) {
        this.filePathFormat = new Format(pattern);
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setResponseDelay(int responseDelay) {
        this.responseDelay = responseDelay;
    }

}
