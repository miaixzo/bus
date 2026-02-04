/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.ImageProgress;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

/**
 * The {@code CStore} class provides a simple way to perform a DICOM C-STORE operation. It encapsulates the setup and
 * execution of a {@link StoreSCU} instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CStore {

    /**
     * Performs a DICOM C-STORE operation.
     *
     * @param callingNode the configuration of the calling DICOM node.
     * @param calledNode  the configuration of the called DICOM node.
     * @param files       a list of file paths to be stored.
     * @return a {@link Status} instance containing the DICOM response, status, error message, and progress information.
     */
    public static Status process(Node callingNode, Node calledNode, List<String> files) {
        return process(null, callingNode, calledNode, files);
    }

    /**
     * Performs a DICOM C-STORE operation with a progress handler.
     *
     * @param callingNode the configuration of the calling DICOM node.
     * @param calledNode  the configuration of the called DICOM node.
     * @param files       a list of file paths to be stored.
     * @param progress    the progress handler for the operation.
     * @return a {@link Status} instance containing the DICOM response, status, error message, and progress information.
     */
    public static Status process(Node callingNode, Node calledNode, List<String> files, ImageProgress progress) {
        return process(null, callingNode, calledNode, files, progress);
    }

    /**
     * Performs a DICOM C-STORE operation with advanced options.
     *
     * @param args        optional advanced parameters (proxy, authentication, connection, and TLS).
     * @param callingNode the configuration of the calling DICOM node.
     * @param calledNode  the configuration of the called DICOM node.
     * @param files       a list of file paths to be stored.
     * @return a {@link Status} instance containing the DICOM response, status, error message, and progress information.
     */
    public static Status process(Args args, Node callingNode, Node calledNode, List<String> files) {
        return process(args, callingNode, calledNode, files, null);
    }

    /**
     * Performs a DICOM C-STORE operation with advanced options and a progress handler.
     *
     * @param args        optional advanced parameters (proxy, authentication, connection, and TLS).
     * @param callingNode the configuration of the calling DICOM node.
     * @param calledNode  the configuration of the called DICOM node.
     * @param files       a list of file paths to be stored.
     * @param progress    the progress handler for the operation.
     * @return a {@link Status} instance containing the DICOM response, status, error message, and progress information.
     */
    public static Status process(
            Args args,
            Node callingNode,
            Node calledNode,
            List<String> files,
            ImageProgress progress) {
        if (null == callingNode || null == calledNode) {
            throw new IllegalArgumentException("callingNode or calledNode cannot be null!");
        }

        Args options = args == null ? new Args() : args;

        StoreSCU storeSCU = null;

        try {
            Device device = new Device("storescu");
            Connection conn = new Connection();
            device.addConnection(conn);
            ApplicationEntity ae = new ApplicationEntity(callingNode.getAet());
            device.addApplicationEntity(ae);
            ae.addConnection(conn);
            storeSCU = new StoreSCU(ae, progress, options.getEditors());
            Connection remote = storeSCU.getRemoteConnection();
            Centre service = new Centre(device);

            options.configureConnect(storeSCU.getAAssociateRQ(), remote, calledNode);
            options.configureBind(ae, conn, callingNode);

            options.configure(conn);
            options.configureTLS(conn, remote);

            storeSCU.setAttributes(new Attributes());

            if (options.isNegociation()) {
                configureRelatedSOPClass(storeSCU, options.getSopClasses());
            }
            storeSCU.setPriority(options.getPriority());

            storeSCU.scanFiles(files);

            Status dcmState = storeSCU.getState();

            int n = storeSCU.getFilesScanned();
            if (n == 0) {
                return new Status(Status.UnableToProcess, "No DICOM file has been found!", null);
            } else {
                service.start();
                try {
                    long t1 = System.currentTimeMillis();
                    storeSCU.open();
                    long t2 = System.currentTimeMillis();
                    storeSCU.sendFiles();
                    Builder.forceGettingAttributes(dcmState, storeSCU);
                    long t3 = System.currentTimeMillis();
                    String timeMsg = MessageFormat.format(
                            "DICOM C-STORE connected in {2}ms from {0} to {1}. Stored files in {3}ms. Total size {4}",
                            storeSCU.getAAssociateRQ().getCallingAET(),
                            storeSCU.getAAssociateRQ().getCalledAET(),
                            t2 - t1,
                            t3 - t2,
                            Builder.humanReadableByte(storeSCU.getTotalSize(), false));
                    dcmState = Status.buildMessage(dcmState, timeMsg, null);
                    dcmState.addProcessTime(t1, t2, t3);
                    dcmState.setBytesSize(storeSCU.getTotalSize());
                    return dcmState;
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    Logger.error("storescu", e);
                    Builder.forceGettingAttributes(storeSCU.getState(), storeSCU);
                    return Status.buildMessage(storeSCU.getState(), null, e);
                } finally {
                    IoKit.close(storeSCU);
                    service.stop();
                }
            }
        } catch (Exception e) {
            Logger.error("storescu", e);
            return Status.buildMessage(
                    new Status(Status.UnableToProcess,
                            "DICOM Store failed" + Symbol.COLON + Symbol.SPACE + e.getMessage(), null),
                    null,
                    e);
        } finally {
            IoKit.close(storeSCU);
        }
    }

    /**
     * Configures the related SOP classes for the C-STORE operation from a properties file. This enables the SOP Class
     * Relationship Extended Negotiation.
     *
     * @param storescu the {@link StoreSCU} instance to configure.
     * @param url      the URL to the properties file. If null, a default resource is used.
     */
    private static void configureRelatedSOPClass(StoreSCU storescu, URL url) {
        storescu.enableSOPClassRelationshipExtNeg(true);
        Properties p = new Properties();
        try {
            if (url != null) {
                p.load(url.openStream());
            } else {
                url = ResourceKit.getResourceUrl("sop-classes-uid.properties", CStore.class);
                p.load(url.openStream());
            }
        } catch (IOException e) {
            Logger.error("Cannot read sop-class", e);
        }
        storescu.relSOPClasses.init(p);
    }

}
