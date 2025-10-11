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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code StoreSCP} class implements a Service Class Provider (SCP) for the DICOM C-STORE service. It listens for
 * incoming DICOM associations, accepts C-STORE requests, and stores the received DICOM objects to a specified
 * directory.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StoreSCP {

    /**
     * The main device for this SCP.
     */
    public final Device device = new Device("storescp");
    /**
     * The Application Entity that accepts associations.
     */
    public final ApplicationEntity ae = new ApplicationEntity(Symbol.STAR);
    /**
     * The network connection configuration.
     */
    public final Connection conn = new Connection();
    /**
     * The base directory where received DICOM files are stored.
     */
    public final String storageDir;
    /**
     * A list of nodes that are authorized to send files.
     */
    public final List<Node> authorizedCallingNodes;
    /**
     * A progress handler to monitor the storage process.
     */
    public final ImageProgress progress;
    /**
     * A handler for post-processing of received files.
     */
    public Efforts efforts;
    /**
     * A format string for generating custom file paths.
     */
    public Format filePathFormat;
    /**
     * A regex pattern used with the file path format.
     */
    public Pattern regex;
    /**
     * The default status to be returned in C-STORE responses.
     */
    public volatile int status = Status.Success;
    /**
     * An array of delays (in ms) to simulate latency before receiving data.
     */
    private int[] receiveDelays;
    /**
     * An array of delays (in ms) to simulate processing time before sending a response.
     */
    private int[] responseDelays;
    /**
     * The core service implementation that handles C-STORE requests.
     */
    private final BasicCStoreSCP cstoreSCP = new BasicCStoreSCP(Symbol.STAR) {

        @Override
        protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data, Attributes rsp)
                throws IOException {
            if (authorizedCallingNodes != null && !authorizedCallingNodes.isEmpty()) {
                Node sourceNode = Node.buildRemoteDicomNode(as);
                boolean valid = authorizedCallingNodes.stream().anyMatch(
                        n -> n.getAet().equals(sourceNode.getAet())
                                && (!n.isValidateHostname() || n.equalsHostname(sourceNode.getHostname())));
                if (!valid) {
                    rsp.setInt(Tag.Status, VR.US, Status.NotAuthorized);
                    Logger.error(
                            "Refused: not authorized (124H). Source node: {}. SopUID: {}",
                            sourceNode,
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
     * Constructs a new {@code StoreSCP} with a specified storage directory.
     *
     * @param storageDir The base path of the storage folder.
     */
    public StoreSCP(String storageDir) {
        this(storageDir, null);
    }

    /**
     * Constructs a new {@code StoreSCP} with a storage directory and a list of authorized nodes.
     *
     * @param storageDir             The base path of the storage folder.
     * @param authorizedCallingNodes A list of nodes authorized to store files.
     */
    public StoreSCP(String storageDir, List<Node> authorizedCallingNodes) {
        this(storageDir, authorizedCallingNodes, null);
    }

    /**
     * Constructs a new {@code StoreSCP} with a storage directory, authorized nodes, and a progress handler.
     *
     * @param storageDir             The base path of the storage folder.
     * @param authorizedCallingNodes A list of nodes authorized to store files.
     * @param imageProgress          A progress handler to monitor storage operations.
     */
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
        if (!from.renameTo(dest)) {
            throw new IOException("Failed to rename " + from + " to " + dest);
        }
    }

    /**
     * Parses a DICOM file to read its dataset, excluding pixel data.
     *
     * @param file The file to parse.
     * @return The DICOM attributes from the file.
     * @throws IOException if an I/O error occurs.
     */
    private static Attributes parse(File file) throws IOException {
        try (ImageInputStream in = new ImageInputStream(file)) {
            in.setIncludeBulkData(ImageInputStream.IncludeBulkData.NO);
            return in.readDatasetUntilPixelData();
        }
    }

    /**
     * Pauses the current thread for a specified delay.
     *
     * @param as     The association, used to determine which delay to use from the array.
     * @param delays An array of delay values in milliseconds.
     */
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

    /**
     * Stores the incoming DICOM data to a file.
     *
     * @param as   The association.
     * @param fmi  The File Meta Information.
     * @param data The input stream containing the DICOM data.
     * @param file The file to store the data in.
     * @throws IOException if an I/O error occurs.
     */
    private void storeTo(Association as, Attributes fmi, PDVInputStream data, File file) throws IOException {
        Logger.debug("{}: M-WRITE {}", as, file);
        file.getParentFile().mkdirs();
        try (ImageOutputStream out = new ImageOutputStream(file)) {
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        }
    }

    /**
     * Creates the DICOM service registry and adds the C-ECHO and C-STORE SCP services.
     *
     * @return The configured service registry.
     */
    private ImageServiceRegistry createServiceRegistry() {
        ImageServiceRegistry serviceRegistry = new ImageServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(cstoreSCP);
        return serviceRegistry;
    }

    /**
     * Sets the format pattern for the storage file path.
     *
     * @param pattern The format pattern string.
     */
    public void setStorageFilePathFormat(String pattern) {
        if (StringKit.hasText(pattern)) {
            this.filePathFormat = new Format(pattern);
            this.regex = Pattern.compile("\\{(.*?)\\}");
        } else {
            this.filePathFormat = null;
            this.regex = null;
        }
    }

    /**
     * Sets the default status to be returned in C-STORE responses.
     *
     * @param status The DICOM status code.
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Sets an array of delays to be applied before receiving data for each C-STORE request.
     *
     * @param receiveDelays An array of delays in milliseconds.
     */
    public void setReceiveDelays(int[] receiveDelays) {
        this.receiveDelays = receiveDelays;
    }

    /**
     * Sets an array of delays to be applied before sending the response for each C-STORE request.
     *
     * @param responseDelays An array of delays in milliseconds.
     */
    public void setResponseDelays(int[] responseDelays) {
        this.responseDelays = responseDelays;
    }

    /**
     * Configures the accepted SOP Classes and Transfer Syntaxes from a properties file.
     *
     * @param url The URL of the properties file.
     */
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
            Logger.error("Cannot read sop-classes.properties", e);
        }

        for (String cuid : p.stringPropertyNames()) {
            String ts = p.getProperty(cuid);
            TransferCapability tc = new TransferCapability(null, UID.toUID(cuid), TransferCapability.Role.SCP,
                    UID.toUIDs(ts));
            ae.addTransferCapability(tc);
        }
    }

    /**
     * Gets the Application Entity of this SCP.
     *
     * @return The Application Entity.
     */
    public ApplicationEntity getApplicationEntity() {
        return ae;
    }

    /**
     * Gets the network connection configuration.
     *
     * @return The connection.
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * Gets the device associated with this SCP.
     *
     * @return The device.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Gets the post-processing handler.
     *
     * @return The efforts handler.
     */
    public Efforts getEfforts() {
        return efforts;
    }

    /**
     * Sets the post-processing handler.
     *
     * @param efforts The efforts handler.
     */
    public void setEfforts(Efforts efforts) {
        this.efforts = efforts;
    }

    /**
     * Gets the storage directory path.
     *
     * @return The storage directory path string.
     */
    public String getStorageDir() {
        return storageDir;
    }

    /**
     * Gets the progress handler.
     *
     * @return The image progress handler.
     */
    public ImageProgress getProgress() {
        return progress;
    }

}
