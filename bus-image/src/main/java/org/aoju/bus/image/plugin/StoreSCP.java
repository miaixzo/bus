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
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.image.*;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.VR;
import org.aoju.bus.image.galaxy.io.ImageInputStream;
import org.aoju.bus.image.galaxy.io.ImageOutputStream;
import org.aoju.bus.image.metric.*;
import org.aoju.bus.image.metric.internal.pdu.Presentation;
import org.aoju.bus.image.metric.service.BasicCEchoSCP;
import org.aoju.bus.image.metric.service.BasicCStoreSCP;
import org.aoju.bus.image.metric.service.ServiceHandler;
import org.aoju.bus.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class StoreSCP extends BasicCStoreSCP {

    public final Device device = new Device("storescp");
    public final ApplicationEntity ae = new ApplicationEntity(Symbol.STAR);
    public final Connection conn = new Connection();
    public final String storageDir;
    public final List<Node> authorizedCallingNodes;
    public Efforts efforts;
    private volatile int status = Status.Success;

    /**
     * @param storageDir 存储文件夹的基本路径
     */
    public StoreSCP(String storageDir) {
        this(storageDir, null);
    }

    /**
     * @param storageDir             the base path of storage folder
     * @param authorizedCallingNodes the list of authorized nodes to call store files (authorizedCallingNodes allow to check hostname
     *                               unlike acceptedCallingAETitles)
     */
    public StoreSCP(String storageDir,
                    List<Node> authorizedCallingNodes) {
        this.storageDir = Objects.requireNonNull(storageDir);
        device.setDimseRQHandler(createServiceRegistry());
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.setAssociationAcceptor(true);
        ae.addConnection(conn);
        this.authorizedCallingNodes = authorizedCallingNodes;
    }

    private static void renameTo(Association as, File from, File dest) throws IOException {
        Logger.info("{}: M-RENAME {} to {}", as, from, dest);
        Builder.prepareToWriteFile(dest);
        if (!from.renameTo(dest))
            throw new IOException("Failed to rename " + from + " to " + dest);
    }

    private static Attributes parse(File file) throws IOException {
        ImageInputStream in = new ImageInputStream(file);
        try {
            in.setIncludeBulkData(ImageInputStream.IncludeBulkData.NO);
            return in.readDataset(-1, Tag.PixelData);
        } finally {
            IoKit.close(in);
        }
    }

    private static void deleteFile(Association as, File file) {
        if (file.delete())
            Logger.info("{}: M-DELETE {}", as, file);
        else
            Logger.warn("{}: M-DELETE {} failed!", as, file);
    }

    @Override
    protected void store(Association as, Presentation pc, Attributes rq, PDVInputStream data, Attributes rsp)
            throws IOException {
        if (null != authorizedCallingNodes && !authorizedCallingNodes.isEmpty()) {
            Node sourceNode = Node.buildRemoteDicomNode(as);
            boolean valid = authorizedCallingNodes.stream().anyMatch(n -> n.getAet().equals(sourceNode.getAet())
                    && (!n.isValidate() || n.equalsHostname(sourceNode.getHostname())));
            if (!valid) {
                rsp.setInt(Tag.Status, VR.US, Status.NotAuthorized);
                Logger.error("Refused: not authorized (124H). Source node: {}. SopUID: {}", sourceNode,
                        rq.getString(Tag.AffectedSOPInstanceUID));
                return;
            }
        }

        rsp.setInt(Tag.Status, VR.US, status);

        String cuid = rq.getString(Tag.AffectedSOPClassUID);
        String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
        String tsuid = pc.getTransferSyntax();
        File file = new File(storageDir, File.separator + iuid + Builder.IMAGE_ORIGINAL_SUFFIX);
        try {
            Attributes fmi = as.createFileMetaInformation(iuid, cuid, tsuid);
            storeTo(as, fmi, data, file);
            if (ObjectKit.isNotEmpty(efforts)) {
                efforts.supports(fmi, file, this.getClass());
            }
        } catch (Exception e) {
            throw new ImageException(Status.ProcessingFailure, e);
        }
    }

    private void storeTo(Association as, Attributes fmi, PDVInputStream data, File file) throws IOException {
        Logger.debug("{}: M-WRITE {}", as, file);
        file.getParentFile().mkdirs();
        ImageOutputStream out = new ImageOutputStream(file);
        try {
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        } finally {
            IoKit.close(out);
        }
    }

    private ServiceHandler createServiceRegistry() {
        ServiceHandler serviceHandler = new ServiceHandler();
        serviceHandler.addService(new BasicCEchoSCP());
        serviceHandler.addService(this);
        return serviceHandler;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void loadDefaultTransferCapability(URL url) {
        Properties p = new Properties();

        try {
            if (url != null) {
                try (InputStream in = url.openStream()) {
                    p.load(in);
                }
            } else {
                p.load(this.getClass().getResourceAsStream("sop-classes.properties"));
            }
        } catch (IOException e) {
            Logger.error("Cannot read sop-classes", e);
        }

        for (String cuid : p.stringPropertyNames()) {
            String ts = p.getProperty(cuid);
            TransferCapability tc =
                    new TransferCapability(null, Builder.toUID(cuid), TransferCapability.Role.SCP, Builder.toUIDs(ts));
            ae.addTransferCapability(tc);
        }
    }

    public ApplicationEntity getApplicationEntity() {
        return ae;
    }

    public Connection getConnection() {
        return conn;
    }

    public Device getDevice() {
        return device;
    }

    public Efforts getEfforts() {
        return efforts;
    }

    public void setEfforts(Efforts efforts) {
        this.efforts = efforts;
    }

}
