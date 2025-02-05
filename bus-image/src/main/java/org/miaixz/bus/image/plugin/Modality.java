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

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.List;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.builtin.DicomFiles;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.Sequence;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Modality {

    private static String calledAET;

    public static void setTlsParams(Connection remote, Connection conn) {
        remote.setTlsProtocols(conn.getTlsProtocols());
        remote.setTlsCipherSuites(conn.getTlsCipherSuites());
    }

    private static void addReferencedPerformedProcedureStepSequence(String mppsiuid, StoreSCU storescu) {
        Attributes attrs = storescu.getAttributes();
        Sequence seq = attrs.newSequence(Tag.ReferencedPerformedProcedureStepSequence, 1);
        Attributes item = new Attributes(2);
        item.setString(Tag.ReferencedSOPClassUID, VR.UI, UID.ModalityPerformedProcedureStep.uid);
        item.setString(Tag.ReferencedSOPInstanceUID, VR.UI, mppsiuid);
        seq.add(item);
    }

    private static void nullifyReferencedPerformedProcedureStepSequence(StoreSCU storescu) {
        Attributes attrs = storescu.getAttributes();
        attrs.setNull(Tag.ReferencedPerformedProcedureStepSequence, VR.SQ);
    }

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

    private static void printNextStepMessage(String message) throws IOException {
        Logger.info("===========================================================");
        Logger.info(message + ". Press <enter> to continue.");
        Logger.info("===========================================================");
        new BufferedReader(new InputStreamReader(System.in)).read();
    }

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

    private static void scanFiles(List<String> fnames, String tmpPrefix, String tmpSuffix, File tmpDir,
            final MppsSCU mppsscu, final StoreSCU storescu, final StgCmtSCU stgcmtscu) throws IOException {
        printNextStepMessage("Will now scan files in " + fnames);
        File tmpFile = File.createTempFile(tmpPrefix, tmpSuffix, tmpDir);
        tmpFile.deleteOnExit();
        final BufferedWriter fileInfos = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile)));
        try {
            DicomFiles.scan(fnames, (f, fmi, dsPos, ds) -> mppsscu.addInstance(ds)
                    && storescu.addFile(fileInfos, f, dsPos, fmi, ds) && stgcmtscu.addInstance(ds));
            storescu.setTmpFile(tmpFile);
        } finally {
            fileInfos.close();
        }
    }

}
