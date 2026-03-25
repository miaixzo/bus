/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.ImageParam;
import org.miaixz.bus.image.galaxy.ImageProgress;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.QueryOption;
import org.miaixz.bus.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * The {@code CGet} class provides a simple way to perform a DICOM C-GET operation. It encapsulates the setup and
 * execution of a {@link GetSCU} instance.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CGet {

    /**
     * Performs a DICOM C-GET operation.
     *
     * @param callingNode the configuration of the calling DICOM node.
     * @param calledNode  the configuration of the called DICOM node.
     * @param progress    the progress handler for the operation.
     * @param outputDir   the directory to store the retrieved files.
     * @param keys        the matching and return keys. Keys without values are treated as return keys.
     * @return a {@link Status} instance containing the DICOM response, status, error message, and progress.
     */
    public static Status process(
            Node callingNode,
            Node calledNode,
            ImageProgress progress,
            File outputDir,
            ImageParam... keys) {
        return process(null, callingNode, calledNode, progress, outputDir, keys);
    }

    /**
     * Performs a DICOM C-GET operation with advanced options.
     *
     * @param args        optional advanced parameters (proxy, authentication, connection, and TLS).
     * @param callingNode the configuration of the calling DICOM node.
     * @param calledNode  the configuration of the called DICOM node.
     * @param progress    the progress handler for the operation.
     * @param outputDir   the directory to store the retrieved files.
     * @param keys        the matching and return keys. Keys without values are treated as return keys.
     * @return a {@link Status} instance containing the DICOM response, status, error message, and progress.
     */
    public static Status process(
            Args args,
            Node callingNode,
            Node calledNode,
            ImageProgress progress,
            File outputDir,
            ImageParam... keys) {
        return process(args, callingNode, calledNode, progress, outputDir, null, keys);
    }

    /**
     * Performs a DICOM C-GET operation with advanced options and a specific SOP class URL.
     *
     * @param args        optional advanced parameters (proxy, authentication, connection, and TLS).
     * @param callingNode the configuration of the calling DICOM node.
     * @param calledNode  the configuration of the called DICOM node.
     * @param progress    the progress handler for the operation.
     * @param outputDir   the directory to store the retrieved files.
     * @param sopClassURL the URL to a properties file defining supported SOP classes.
     * @param keys        the matching and return keys. Keys without values are treated as return keys.
     * @return a {@link Status} instance containing the DICOM response, status, error message, and progress.
     */
    public static Status process(
            Args args,
            Node callingNode,
            Node calledNode,
            ImageProgress progress,
            File outputDir,
            URL sopClassURL,
            ImageParam... keys) {
        if (callingNode == null || calledNode == null || outputDir == null) {
            throw new IllegalArgumentException("callingNode, calledNode or outputDir cannot be null!");
        }
        GetSCU getSCU = null;
        Args options = args == null ? new Args() : args;

        try {
            getSCU = new GetSCU(progress);
            Connection remote = getSCU.getRemoteConnection();
            Connection conn = getSCU.getConnection();
            options.configureConnect(getSCU.getAAssociateRQ(), remote, calledNode);
            options.configureBind(getSCU.getApplicationEntity(), conn, callingNode);
            Centre service = new Centre(getSCU.getDevice());

            // configure
            options.configure(conn);
            options.configureTLS(conn, remote);

            getSCU.setPriority(options.getPriority());

            getSCU.setStorageDirectory(outputDir);

            getSCU.setInformationModel(
                    getInformationModel(options),
                    options.getTsuidOrder(),
                    options.getQueryOptions().contains(QueryOption.RELATIONAL));

            configureRelatedSOPClass(getSCU, sopClassURL);

            Status dcmState = getSCU.getState();
            for (ImageParam p : keys) {
                String[] values = p.getValues();
                getSCU.addKey(p.getTag(), values);
                if (values != null && values.length > 0) {
                    dcmState.addDicomMatchingKeys(p);
                }
            }

            service.start();
            try {
                long t1 = System.currentTimeMillis();
                getSCU.open();
                long t2 = System.currentTimeMillis();
                getSCU.retrieve();
                Builder.forceGettingAttributes(dcmState, getSCU);
                long t3 = System.currentTimeMillis();
                String timeMsg = MessageFormat.format(
                        "DICOM C-GET connected in {2}ms from {0} to {1}. Get files in {3}ms.",
                        getSCU.getAAssociateRQ().getCallingAET(),
                        getSCU.getAAssociateRQ().getCalledAET(),
                        t2 - t1,
                        t3 - t2);
                dcmState = Status.buildMessage(dcmState, timeMsg, null);
                dcmState.addProcessTime(t1, t2, t3);
                dcmState.setBytesSize(getSCU.getTotalSize());
                return dcmState;
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                Logger.error("getscu", e);
                Builder.forceGettingAttributes(getSCU.getState(), getSCU);
                return Status.buildMessage(getSCU.getState(), null, e);
            } finally {
                IoKit.close(getSCU);
                service.stop();
            }
        } catch (Exception e) {
            Logger.error("getscu", e);
            return Status.buildMessage(
                    new Status(Status.UnableToProcess,
                            "DICOM Get failed" + Symbol.COLON + Symbol.SPACE + e.getMessage(), null),
                    null,
                    e);
        }
    }

    /**
     * Configures the related SOP classes for the C-GET operation from a properties file.
     *
     * @param getSCU the {@link GetSCU} instance to configure.
     * @param url    the URL to the properties file. If null, a default resource is used.
     */
    private static void configureRelatedSOPClass(GetSCU getSCU, URL url) {
        Properties p = new Properties();
        try {
            if (url != null) {
                p.load(url.openStream());
            } else {
                url = ResourceKit.getResourceUrl("sop-classes-tcs.properties", StoreSCP.class);
                p.load(url.openStream());
            }
        } catch (IOException e) {
            Logger.error("Cannot read sop-classes", e);
        }

        for (Entry<Object, Object> entry : p.entrySet()) {
            configureStorageSOPClass(getSCU, (String) entry.getKey(), (String) entry.getValue());
        }
    }

    /**
     * Configures a single storage SOP class with its transfer syntaxes.
     *
     * @param getSCU the {@link GetSCU} instance to configure.
     * @param cuid   the SOP Class UID.
     * @param tsuids a semicolon-separated string of Transfer Syntax UIDs.
     */
    private static void configureStorageSOPClass(GetSCU getSCU, String cuid, String tsuids) {
        String[] ts = StringKit.splitToArray(tsuids, ";");
        for (int i = 0; i < ts.length; i++) {
            ts[i] = UID.toUID(ts[i]);
        }
        getSCU.addOfferedStorageSOPClass(UID.toUID(cuid), ts);
    }

    /**
     * Retrieves the information model from the given options.
     *
     * @param options the command-line arguments or options.
     * @return the {@link GetSCU.InformationModel}, defaulting to {@code StudyRoot}.
     */
    private static GetSCU.InformationModel getInformationModel(Args options) {
        Object model = options.getInformationModel();
        if (model instanceof GetSCU.InformationModel) {
            return (GetSCU.InformationModel) model;
        }
        return GetSCU.InformationModel.StudyRoot;
    }

}
