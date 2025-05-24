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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.ImageProgress;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.TransferCapability;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.net.PDVInputStream;
import org.miaixz.bus.image.metric.pdu.PresentationContext;
import org.miaixz.bus.image.metric.service.BasicCEchoSCP;
import org.miaixz.bus.image.metric.service.BasicCStoreSCP;
import org.miaixz.bus.image.metric.service.ImageServiceException;
import org.miaixz.bus.image.metric.service.ImageServiceRegistry;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class StoreSCP {

    public final Device device = new Device("storescp");
    public final ApplicationEntity ae = new ApplicationEntity(Symbol.STAR);
    public final Connection conn = new Connection();
    public final String storageDir;
    public final List<Node> authorizedCallingNodes;
    public final ImageProgress progress;
    public Efforts efforts;
    public Format filePathFormat;
    public Pattern regex;
    public volatile int status = Status.Success;
    private int[] receiveDelays;
    private int[] responseDelays;
    private final BasicCStoreSCP cstoreSCP = new BasicCStoreSCP(Symbol.STAR) {
        @Override
        protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data, Attributes rsp)
                throws IOException {
            if (authorizedCallingNodes != null && !authorizedCallingNodes.isEmpty()) {
                Node sourceNode = Node.buildRemoteDicomNode(as);
                boolean valid = authorizedCallingNodes.stream().anyMatch(n -> n.getAet().equals(sourceNode.getAet())
                        && (!n.isValidateHostname() || n.equalsHostname(sourceNode.getHostname())));
                if (!valid) {
                    rsp.setInt(Tag.Status, VR.US, Status.NotAuthorized);
                    Logger.error("Refused: not authorized (124H). Source node: {}. SopUID: {}", sourceNode,
                            rq.getString(Tag.AffectedSOPInstanceUID));
                    return;
                }
            }
            sleep(as, receiveDelays);
            try {
                rsp.setInt(Tag.Status, VR.US, status);

                String cuid = rq.getString(Tag.AffectedSOPClassUID);
                String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
                String tsuid = pc.getTransferSyntax();
                File file = new File(storageDir, File.separator + iuid);
                try {
                    Attributes fmi = as.createFileMetaInformation(iuid, cuid, tsuid);
                    storeTo(as, fmi, data, file);
                    String filename;
                    if (filePathFormat == null) {
                        filename = iuid;
                    } else {
                        Attributes a = fmi;
                        Matcher regexMatcher = regex.matcher(filePathFormat.toString());
                        while (regexMatcher.find()) {
                            if (!regexMatcher.group(1).startsWith("0002")) {
                                a = parse(file);
                                a.addAll(fmi);
                                break;
                            }
                        }
                        filename = filePathFormat.format(a);
                    }
                    File rename = new File(storageDir, filename);
                    renameTo(as, file, rename);
                    if (progress != null) {
                        progress.setProcessedFile(rename);
                        progress.setAttributes(null);
                    }
                    if (ObjectKit.isNotEmpty(efforts)) {
                        efforts.supports(fmi, file, this.getClass());
                    }
                } catch (Exception e) {
                    FileKit.remove(file);
                    throw new ImageServiceException(Status.ProcessingFailure, e);
                }
            } finally {
                sleep(as, responseDelays);
            }
        }
    };

    /**
     * @param storageDir 存储文件夹的基本路径
     */
    public StoreSCP(String storageDir) {
        this(storageDir, null);
    }

    /**
     * @param storageDir             the base path of storage folder
     * @param authorizedCallingNodes the list of authorized nodes to call store files (authorizedCallingNodes allow to
     *                               check hostname unlike acceptedCallingAETitles)
     */
    public StoreSCP(String storageDir, List<Node> authorizedCallingNodes) {
        this(storageDir, authorizedCallingNodes, null);
    }

    public StoreSCP(String storageDir, List<Node> authorizedCallingNodes, ImageProgress imageProgress) {
        this.storageDir = Objects.requireNonNull(storageDir);
        device.setDimseRQHandler(createServiceRegistry());
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.setAssociationAcceptor(true);
        ae.addConnection(conn);
        this.authorizedCallingNodes = authorizedCallingNodes;
        this.progress = imageProgress;
    }

    private static void renameTo(Association as, File from, File dest) throws IOException {
        Logger.info("{}: M-RENAME {} to {}", as, from, dest);
        Builder.prepareToWriteFile(dest);
        if (!from.renameTo(dest))
            throw new IOException("Failed to rename " + from + " to " + dest);
    }

    private static Attributes parse(File file) throws IOException {
        try (ImageInputStream in = new ImageInputStream(file)) {
            in.setIncludeBulkData(ImageInputStream.IncludeBulkData.NO);
            return in.readDatasetUntilPixelData();
        }
    }

    private void sleep(Association as, int[] delays) {
        int responseDelay = delays != null ? delays[(as.getNumberOfReceived(Dimse.C_STORE_RQ) - 1) % delays.length] : 0;
        if (responseDelay > 0) {
            try {
                Thread.sleep(responseDelay);
            } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void storeTo(Association as, Attributes fmi, PDVInputStream data, File file) throws IOException {
        Logger.debug("{}: M-WRITE {}", as, file);
        file.getParentFile().mkdirs();
        try (ImageOutputStream out = new ImageOutputStream(file)) {
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        }
    }

    private ImageServiceRegistry createServiceRegistry() {
        ImageServiceRegistry serviceRegistry = new ImageServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(cstoreSCP);
        return serviceRegistry;
    }

    public void setStorageFilePathFormat(String pattern) {
        if (StringKit.hasText(pattern)) {
            this.filePathFormat = new Format(pattern);
            this.regex = Pattern.compile("\\{(.*?)\\}");
        } else {
            this.filePathFormat = null;
            this.regex = null;
        }
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setReceiveDelays(int[] receiveDelays) {
        this.receiveDelays = receiveDelays;
    }

    public void setResponseDelays(int[] responseDelays) {
        this.responseDelays = responseDelays;
    }

    public void sopClassesTCS(URL url) {
        Properties p = new Properties();
        try {
            if (url != null) {
                p.load(url.openStream());
            } else {
                url = ResourceKit.getResourceUrl("sop-classes.properties", StoreSCP.class);
                p.load(url.openStream());
            }
        } catch (IOException e) {
            Logger.error("Cannot read sop-class", e);
        }

        for (String cuid : p.stringPropertyNames()) {
            String ts = p.getProperty(cuid);
            TransferCapability tc = new TransferCapability(null, UID.toUID(cuid), TransferCapability.Role.SCP,
                    UID.toUIDs(ts));
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

    public String getStorageDir() {
        return storageDir;
    }

    public ImageProgress getProgress() {
        return progress;
    }

}
