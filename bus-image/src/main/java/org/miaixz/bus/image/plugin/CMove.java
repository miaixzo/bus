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
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.ImageParam;
import org.miaixz.bus.image.galaxy.ImageProgress;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.QueryOption;
import org.miaixz.bus.logger.Logger;

import java.text.MessageFormat;

/**
 * The {@code CMove} class provides a simple way to perform a DICOM C-MOVE operation. It encapsulates the setup and
 * execution of a {@link MoveSCU} instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CMove {

    /**
     * Performs a DICOM C-MOVE operation.
     *
     * @param callingNode    the configuration of the calling DICOM node.
     * @param calledNode     the configuration of the called DICOM node.
     * @param destinationAet the AET of the destination node.
     * @param progress       the progress handler for the operation.
     * @param keys           the matching keys for the query. Keys without values are treated as return keys.
     * @return a {@link Status} instance containing the DICOM response, status, error message, and progress.
     */
    public static Status process(
            Node callingNode,
            Node calledNode,
            String destinationAet,
            ImageProgress progress,
            ImageParam... keys) {
        return CMove.process(null, callingNode, calledNode, destinationAet, progress, keys);
    }

    /**
     * Performs a DICOM C-MOVE operation with advanced options.
     *
     * @param args           optional advanced parameters (proxy, authentication, connection, and TLS).
     * @param callingNode    the configuration of the calling DICOM node.
     * @param calledNode     the configuration of the called DICOM node.
     * @param destinationAet the AET of the destination node.
     * @param progress       the progress handler for the operation.
     * @param keys           the matching keys for the query. Keys without values are treated as return keys.
     * @return a {@link Status} instance containing the DICOM response, status, error message, and progress.
     */
    public static Status process(
            Args args,
            Node callingNode,
            Node calledNode,
            String destinationAet,
            ImageProgress progress,
            ImageParam... keys) {
        if (callingNode == null || calledNode == null || destinationAet == null) {
            throw new IllegalArgumentException("callingNode, calledNode or destinationAet cannot be null!");
        }
        Args options = args == null ? new Args() : args;

        try (MoveSCU moveSCU = new MoveSCU(progress)) {
            Connection remote = moveSCU.getRemoteConnection();
            Connection conn = moveSCU.getConnection();
            options.configureConnect(moveSCU.getAAssociateRQ(), remote, calledNode);
            options.configureBind(moveSCU.getApplicationEntity(), conn, callingNode);
            Centre service = new Centre(moveSCU);

            // configure
            options.configure(conn);
            options.configureTLS(conn, remote);

            moveSCU.setInformationModel(
                    getInformationModel(options),
                    options.getTsuidOrder(),
                    options.getQueryOptions().contains(QueryOption.RELATIONAL));

            Status dcmState = moveSCU.getState();
            for (ImageParam p : keys) {
                String[] values = p.getValues();
                moveSCU.addKey(p.getTag(), values);
                if (values != null && values.length > 0) {
                    dcmState.addDicomMatchingKeys(p);
                }
            }
            moveSCU.setDestination(destinationAet);

            service.start();
            try {
                long t1 = System.currentTimeMillis();
                moveSCU.open();
                long t2 = System.currentTimeMillis();
                moveSCU.retrieve();
                Builder.forceGettingAttributes(dcmState, moveSCU);
                long t3 = System.currentTimeMillis();
                String timeMsg = MessageFormat.format(
                        "DICOM C-MOVE connected in {2}ms from {0} to {1}. Sent files in {3}ms.",
                        moveSCU.getAAssociateRQ().getCallingAET(),
                        moveSCU.getAAssociateRQ().getCalledAET(),
                        t2 - t1,
                        t3 - t2);
                dcmState = Status.buildMessage(dcmState, timeMsg, null);
                dcmState.addProcessTime(t1, t2, t3);
                return dcmState;
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                Logger.error("movescu", e);
                Builder.forceGettingAttributes(moveSCU.getState(), moveSCU);
                return Status.buildMessage(moveSCU.getState(), null, e);
            } finally {
                IoKit.close(moveSCU);
                service.stop();
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            Logger.error("movescu", e);
            return Status.buildMessage(
                    new Status(Status.UnableToProcess,
                            "DICOM Move failed" + Symbol.COLON + Symbol.SPACE + e.getMessage(), null),
                    null,
                    e);
        }
    }

    /**
     * Retrieves the information model from the given options.
     *
     * @param options the command-line arguments or options.
     * @return the {@link MoveSCU.InformationModel}, defaulting to {@code StudyRoot}.
     */
    private static MoveSCU.InformationModel getInformationModel(Args options) {
        Object model = options.getInformationModel();
        if (model instanceof MoveSCU.InformationModel) {
            return (MoveSCU.InformationModel) model;
        }
        return MoveSCU.InformationModel.StudyRoot;
    }

}
