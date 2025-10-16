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

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.builtin.DicomFiles;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.Sequence;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.logger.Logger;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * The {@code Modality} class provides a set of static utility methods to simulate the behavior of a DICOM modality. It
 * orchestrates the sending of DICOM objects (C-STORE), Modality Performed Procedure Step (MPPS) messages, and Storage
 * Commitment requests. This class is designed for testing and demonstration purposes, with methods that pause for user
 * interaction.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Modality {

    /**
     * The AE Title of the called application entity. Used for logging purposes.
     */
    private static String calledAET;

    /**
     * Copies TLS (Transport Layer Security) parameters from one connection object to another.
     *
     * @param remote The destination connection object to which the TLS settings will be copied.
     * @param conn   The source connection object from which to copy the TLS settings.
     */
    public static void setTlsParams(Connection remote, Connection conn) {
        remote.setTlsProtocols(conn.getTlsProtocols());
        remote.setTlsCipherSuites(conn.getTlsCipherSuites());
    }

    /**
     * Adds a Referenced Performed Procedure Step Sequence to the attributes of a {@link StoreSCU} instance.
     *
     * @param mppsiuid The SOP Instance UID of the MPPS to reference.
     * @param storescu The {@code StoreSCU} instance whose attributes will be modified.
     */
    private static void addReferencedPerformedProcedureStepSequence(String mppsiuid, StoreSCU storescu) {
        Attributes attrs = storescu.getAttributes();
        Sequence seq = attrs.newSequence(Tag.ReferencedPerformedProcedureStepSequence, 1);
        Attributes item = new Attributes(2);
        item.setString(Tag.ReferencedSOPClassUID, VR.UI, UID.ModalityPerformedProcedureStep.uid);
        item.setString(Tag.ReferencedSOPInstanceUID, VR.UI, mppsiuid);
        seq.add(item);
    }

    /**
     * Sets the Referenced Performed Procedure Step Sequence to a null value in the attributes of a {@link StoreSCU}.
     *
     * @param storescu The {@code StoreSCU} instance whose attributes will be modified.
     */
    private static void nullifyReferencedPerformedProcedureStepSequence(StoreSCU storescu) {
        Attributes attrs = storescu.getAttributes();
        attrs.setNull(Tag.ReferencedPerformedProcedureStepSequence, VR.SQ);
    }

    /**
     * Sends a Storage Commitment request using the provided {@link StgCmtSCU} instance.
     *
     * @param stgcmtscu The configured {@code StgCmtSCU} instance.
     * @throws IOException              if an I/O error occurs.
     * @throws InterruptedException     if the operation is interrupted.
     * @throws InternalException        if a configuration error occurs.
     * @throws GeneralSecurityException if a security error occurs.
     */
    private static void sendStgCmt(StgCmtSCU stgcmtscu)
            throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        printNextStepMessage("Will now send Storage Commitment to " + calledAET);
        try {
            stgcmtscu.open();
            stgcmtscu.sendRequests();
        } finally {
            stgcmtscu.close();
        }
    }

    /**
     * Sends MPPS N-CREATE and optionally N-SET messages using the provided {@link MppsSCU} instance.
     *
     * @param mppsscu  The configured {@code MppsSCU} instance.
     * @param sendNSet {@code true} to send an N-SET request after the N-CREATE request.
     * @throws IOException              if an I/O error occurs.
     * @throws InterruptedException     if the operation is interrupted.
     * @throws InternalException        if a configuration error occurs.
     * @throws GeneralSecurityException if a security error occurs.
     */
    private static void sendMpps(MppsSCU mppsscu, boolean sendNSet)
            throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        try {
            printNextStepMessage("Will now send MPPS N-CREATE to " + calledAET);
            mppsscu.open();
            mppsscu.createMpps();
            if (sendNSet) {
                printNextStepMessage("Will now send MPPS N-SET to " + calledAET);
                mppsscu.updateMpps();
            }
        } finally {
            mppsscu.close();
        }
    }

    /**
     * Sends an MPPS N-SET message using the provided {@link MppsSCU} instance.
     *
     * @param mppsscu The configured {@code MppsSCU} instance.
     * @throws IOException              if an I/O error occurs.
     * @throws InterruptedException     if the operation is interrupted.
     * @throws InternalException        if a configuration error occurs.
     * @throws GeneralSecurityException if a security error occurs.
     */
    private static void sendMppsNSet(MppsSCU mppsscu)
            throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        try {
            printNextStepMessage("Will now send MPPS N-SET to " + calledAET);
            mppsscu.open();
            mppsscu.updateMpps();
        } finally {
            mppsscu.close();
        }
    }

    /**
     * Prints a message to the console and waits for the user to press the Enter key.
     *
     * @param message The message to display.
     * @throws IOException if an I/O error occurs while reading from the console.
     */
    private static void printNextStepMessage(String message) throws IOException {
        Logger.info("===========================================================");
        Logger.info(message + ". Press <enter> to continue.");
        Logger.info("===========================================================");
        new BufferedReader(new InputStreamReader(System.in)).read();
    }

    /**
     * Sends DICOM objects using the provided {@link StoreSCU} instance.
     *
     * @param storescu The configured {@code StoreSCU} instance.
     * @throws IOException              if an I/O error occurs.
     * @throws InterruptedException     if the operation is interrupted.
     * @throws InternalException        if a configuration error occurs.
     * @throws GeneralSecurityException if a security error occurs.
     */
    private static void sendObjects(StoreSCU storescu)
            throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        printNextStepMessage("Will now send DICOM object(s) to " + calledAET);
        try {
            storescu.open();
            storescu.sendFiles();
        } finally {
            storescu.close();
        }
    }

    /**
     * Scans a list of DICOM files, adding the instance information to the MPPS, StoreSCU, and StgCmtSCU handlers.
     *
     * @param fnames    A list of file paths to scan.
     * @param tmpPrefix The prefix for the temporary file name.
     * @param tmpSuffix The suffix for the temporary file name.
     * @param tmpDir    The directory for the temporary file.
     * @param mppsscu   The {@code MppsSCU} handler to add instances to.
     * @param storescu  The {@code StoreSCU} handler to add files to.
     * @param stgcmtscu The {@code StgCmtSCU} handler to add instances to.
     * @throws IOException if an I/O error occurs during file scanning or writing.
     */
    private static void scanFiles(
            List<String> fnames,
            String tmpPrefix,
            String tmpSuffix,
            File tmpDir,
            final MppsSCU mppsscu,
            final StoreSCU storescu,
            final StgCmtSCU stgcmtscu) throws IOException {
        printNextStepMessage("Will now scan files in " + fnames);
        File tmpFile = File.createTempFile(tmpPrefix, tmpSuffix, tmpDir);
        tmpFile.deleteOnExit();
        try (BufferedWriter fileInfos = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile)))) {
            DicomFiles.scan(
                    fnames,
                    (f, fmi, dsPos, ds) -> mppsscu.addInstance(ds) && storescu.addFile(fileInfos, f, dsPos, fmi)
                            && stgcmtscu.addInstance(ds));
            storescu.setTmpFile(tmpFile);
        }
    }

}
