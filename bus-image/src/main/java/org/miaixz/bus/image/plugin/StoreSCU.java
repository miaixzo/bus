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
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.EditorContext;
import org.miaixz.bus.image.galaxy.ImageProgress;
import org.miaixz.bus.image.galaxy.ProgressStatus;
import org.miaixz.bus.image.galaxy.RelatedSOPClasses;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.SAXReader;
import org.miaixz.bus.image.metric.*;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.net.InputStreamDataWriter;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.pdu.PresentationContext;
import org.miaixz.bus.image.nimble.stream.BytesWithImageDescriptor;
import org.miaixz.bus.image.nimble.stream.ImageAdapter;
import org.miaixz.bus.logger.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * The {@code StoreSCU} class implements a Service Class User (SCU) for the DICOM C-STORE service. It is responsible for
 * sending DICOM objects to a remote Service Class Provider (SCP). This class handles scanning files, managing
 * presentation contexts, establishing an association, and sending the C-STORE requests. It also supports custom DICOM
 * attribute editing and transfer syntax adaptation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StoreSCU implements AutoCloseable {

    /**
     * A managing SOP Class Relationship extended negotiation.
     */
    public final RelatedSOPClasses relSOPClasses = new RelatedSOPClasses();
    /**
     * The Application Entity used by this SCU.
     */
    private final ApplicationEntity ae;
    /**
     * The remote connection configuration.
     */
    private final Connection remote;
    /**
     * The A-ASSOCIATE-RQ message to be sent.
     */
    private final AAssociateRQ rq = new AAssociateRQ();
    /**
     * A list of editors to be applied to the DICOM attributes before sending.
     */
    private final List<Editors> dicomEditors;
    /**
     * The overall status and progress of the C-STORE operation.
     */
    private final Status state;
    /**
     * Additional attributes to be merged into each object before sending.
     */
    private Attributes attrs;
    /**
     * A suffix to be appended to SOP Instance UIDs if they are modified.
     */
    private String uidSuffix;
    /**
     * A flag to enable SOP Class Relationship extended negotiation.
     */
    private boolean relExtNeg;
    /**
     * The priority of the C-STORE request.
     */
    private int priority;
    /**
     * The prefix for the temporary file name.
     */
    private String tmpPrefix = "storescu-";
    /**
     * The suffix for the temporary file name.
     */
    private String tmpSuffix;
    /**
     * The directory for the temporary file.
     */
    private File tmpDir;
    /**
     * The temporary file used to store the list of files to be sent.
     */
    private File tmpFile;
    /**
     * The active DICOM association.
     */
    private Association as;
    /**
     * The total size in bytes of all successfully sent files.
     */
    private long totalSize = 0;
    /**
     * The total number of files scanned.
     */
    private int filesScanned;
    /**
     * A factory for creating DIMSE response handlers for each C-STORE request.
     */
    private RSPHandlerFactory rspHandlerFactory = file -> new DimseRSPHandler(as.nextMessageID()) {

        @Override
        public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
            super.onDimseRSP(as, cmd, data);
            onCStoreRSP(cmd, file);

            ImageProgress progress = state.getProgress();
            if (progress != null) {
                progress.setProcessedFile(file);
                progress.setAttributes(cmd);
            }
        }
    };

    /**
     * Constructs a new {@code StoreSCU} instance.
     *
     * @param ae       The Application Entity for this SCU.
     * @param progress A progress handler to monitor the operation.
     */
    public StoreSCU(ApplicationEntity ae, ImageProgress progress) {
        this(ae, progress, null);
    }

    /**
     * Constructs a new {@code StoreSCU} instance with custom DICOM editors.
     *
     * @param ae           The Application Entity for this SCU.
     * @param progress     A progress handler to monitor the operation.
     * @param dicomEditors A list of editors to apply to the DICOM objects before sending.
     */
    public StoreSCU(ApplicationEntity ae, ImageProgress progress, List<Editors> dicomEditors) {
        this.remote = new Connection();
        this.ae = ae;
        rq.addPresentationContext(new PresentationContext(1, UID.Verification.uid, UID.ImplicitVRLittleEndian.uid));
        this.state = new Status(progress);
        this.dicomEditors = dicomEditors;
    }

    /**
     * Sets a custom factory for creating DIMSE response handlers.
     *
     * @param rspHandlerFactory The response handler factory.
     */
    public void setRspHandlerFactory(RSPHandlerFactory rspHandlerFactory) {
        this.rspHandlerFactory = rspHandlerFactory;
    }

    /**
     * Gets the A-ASSOCIATE-RQ message.
     *
     * @return The A-ASSOCIATE-RQ.
     */
    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    /**
     * Gets the remote connection configuration.
     *
     * @return The remote connection.
     */
    public Connection getRemoteConnection() {
        return remote;
    }

    /**
     * Gets the attributes that will be merged with each object before sending.
     *
     * @return The attributes to merge.
     */
    public Attributes getAttributes() {
        return attrs;
    }

    /**
     * Sets the attributes to be merged with each object before sending.
     *
     * @param attrs The attributes to merge.
     */
    public void setAttributes(Attributes attrs) {
        this.attrs = attrs;
    }

    /**
     * Sets the temporary file used to store the list of files to send.
     *
     * @param tmpFile The temporary file.
     */
    public void setTmpFile(File tmpFile) {
        this.tmpFile = tmpFile;
    }

    /**
     * Sets the priority for the C-STORE operation.
     *
     * @param priority The priority value (0=Medium, 1=High, 2=Low).
     */
    public final void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Sets a suffix to be appended to SOP Instance UIDs when they are modified.
     *
     * @param uidSuffix The UID suffix.
     */
    public final void setUIDSuffix(String uidSuffix) {
        this.uidSuffix = uidSuffix;
    }

    /**
     * Sets the prefix for the temporary file name.
     *
     * @param prefix The file prefix.
     */
    public final void setTmpFilePrefix(String prefix) {
        this.tmpPrefix = prefix;
    }

    /**
     * Sets the suffix for the temporary file name.
     *
     * @param suffix The file suffix.
     */
    public final void setTmpFileSuffix(String suffix) {
        this.tmpSuffix = suffix;
    }

    /**
     * Sets the directory for the temporary file.
     *
     * @param tmpDir The temporary directory.
     */
    public final void setTmpFileDirectory(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    /**
     * Enables or disables the SOP Class Relationship extended negotiation.
     *
     * @param enable {@code true} to enable, {@code false} to disable.
     */
    public final void enableSOPClassRelationshipExtNeg(boolean enable) {
        relExtNeg = enable;
    }

    /**
     * Scans a list of file paths and prepares them for sending.
     *
     * @param fnames A list of file or directory paths.
     * @throws IOException if an I/O error occurs creating the temporary file.
     */
    public void scanFiles(List<String> fnames) throws IOException {
        this.scanFiles(fnames, true);
    }

    /**
     * Scans a list of file paths and prepares them for sending.
     *
     * @param fnames   A list of file or directory paths.
     * @param printout {@code true} to print progress to the console.
     * @throws IOException if an I/O error occurs creating the temporary file.
     */
    public void scanFiles(List<String> fnames, boolean printout) throws IOException {
        tmpFile = File.createTempFile(tmpPrefix, tmpSuffix, tmpDir);
        tmpFile.deleteOnExit();
        try (BufferedWriter fileInfos = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile)))) {
            for (String fname : fnames) {
                scan(new File(fname), fileInfos, printout);
            }
        }
    }

    /**
     * Recursively scans a file or directory, adding valid DICOM files to the list for sending.
     *
     * @param f         The file or directory to scan.
     * @param fileInfos The writer for the temporary file list.
     * @param printout  {@code true} to print progress to the console.
     */
    private void scan(File f, BufferedWriter fileInfos, boolean printout) {
        if (f.isDirectory() && f.canRead()) {
            String[] fileList = f.list();
            if (fileList != null) {
                for (String s : fileList) {
                    scan(new File(f, s), fileInfos, printout);
                }
            }
            return;
        }

        try (ImageInputStream in = new ImageInputStream(f)) {
            in.setIncludeBulkData(ImageInputStream.IncludeBulkData.NO);
            Attributes fmi = in.readFileMetaInformation();
            long dsPos = in.getPosition();
            if (fmi == null || !fmi.containsValue(Tag.TransferSyntaxUID)
                    || !fmi.containsValue(Tag.MediaStorageSOPClassUID)
                    || !fmi.containsValue(Tag.MediaStorageSOPInstanceUID)) {
                Attributes ds = in.readDataset(Tag.SOPInstanceUID + 1);
                fmi = ds.createFileMetaInformation(in.getTransferSyntax());
            }
            boolean b = addFile(fileInfos, f, dsPos, fmi);
            if (b)
                filesScanned++;
            if (printout) {
                System.out.print(b ? '.' : 'I');
            }
        } catch (Exception e) {
            Logger.error("Failed to scan file {}", f, e);
        }
    }

    /**
     * Reads the list of files from the temporary file and sends them one by one over the active association.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void sendFiles() throws IOException {
        try (BufferedReader fileInfos = new BufferedReader(new InputStreamReader(new FileInputStream(tmpFile)))) {
            String line;
            while (as.isReadyForDataTransfer() && (line = fileInfos.readLine()) != null) {
                String[] ss = StringKit.splitToArray(line, Symbol.HT);
                try {
                    send(new File(ss[4]), Long.parseLong(ss[3]), ss[1], ss[0], ss[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                as.waitForOutstandingRSP();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.error("Wait for outstanding RSP interrupted", e);
            }
        }
    }

    /**
     * Adds a file's information to the temporary file and updates presentation contexts if necessary.
     *
     * @param fileInfos The writer for the temporary file list.
     * @param f         The file to add.
     * @param endFmi    The position in the file where the File Meta Information ends.
     * @param fmi       The File Meta Information attributes.
     * @return {@code true} if the file was added successfully, {@code false} otherwise.
     * @throws IOException if an I/O error occurs writing to the temporary file.
     */
    public boolean addFile(BufferedWriter fileInfos, File f, long endFmi, Attributes fmi) throws IOException {
        String cuid = fmi.getString(Tag.MediaStorageSOPClassUID);
        String iuid = fmi.getString(Tag.MediaStorageSOPInstanceUID);
        String ts = fmi.getString(Tag.TransferSyntaxUID);
        if (cuid == null || iuid == null) {
            return false;
        }

        fileInfos.write(iuid);
        fileInfos.write(Symbol.C_HT);
        fileInfos.write(cuid);
        fileInfos.write(Symbol.C_HT);
        fileInfos.write(ts);
        fileInfos.write(Symbol.C_HT);
        fileInfos.write(Long.toString(endFmi));
        fileInfos.write(Symbol.C_HT);
        fileInfos.write(f.getPath());
        fileInfos.newLine();

        if (rq.containsPresentationContextFor(cuid, ts)) {
            return true;
        }

        if (!rq.containsPresentationContextFor(cuid)) {
            if (relExtNeg) {
                rq.addCommonExtendedNegotiation(relSOPClasses.getCommonExtended(cuid));
            }
            if (!ts.equals(UID.ExplicitVRLittleEndian.uid)) {
                rq.addPresentationContext(
                        new PresentationContext(rq.getNumberOfPresentationContexts() * 2 + 1, cuid,
                                UID.ExplicitVRLittleEndian.uid));
            }
            if (!ts.equals(UID.ImplicitVRLittleEndian.uid)) {
                rq.addPresentationContext(
                        new PresentationContext(rq.getNumberOfPresentationContexts() * 2 + 1, cuid,
                                UID.ImplicitVRLittleEndian.uid));
            }
        }
        rq.addPresentationContext(new PresentationContext(rq.getNumberOfPresentationContexts() * 2 + 1, cuid, ts));
        return true;
    }

    /**
     * Performs a C-ECHO verification to check the connection.
     *
     * @return The C-ECHO response command attributes.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public Attributes echo() throws IOException, InterruptedException {
        DimseRSP response = as.cecho();
        response.next();
        return response.getCommand();
    }

    /**
     * Sends a single DICOM object.
     *
     * @param f         The file to send.
     * @param fmiEndPos The position in the file where the File Meta Information ends.
     * @param cuid      The SOP Class UID.
     * @param iuid      The SOP Instance UID.
     * @param tsuid     The Transfer Syntax UID.
     * @throws IOException                  if an I/O error occurs.
     * @throws InterruptedException         if the operation is interrupted.
     * @throws ParserConfigurationException if a parser configuration error occurs.
     * @throws SAXException                 if a SAX error occurs.
     */
    public void send(final File f, long fmiEndPos, String cuid, String iuid, String tsuid)
            throws IOException, InterruptedException, ParserConfigurationException, SAXException {
        ImageAdapter.AdaptTransferSyntax syntax = new ImageAdapter.AdaptTransferSyntax(tsuid,
                StreamSCU.selectTransferSyntax(as, cuid, tsuid));
        boolean noChange = uidSuffix == null && (attrs == null || attrs.isEmpty())
                && syntax.getRequested().equals(tsuid) && dicomEditors == null;
        DataWriter dataWriter;
        try (InputStream in = noChange ? new FileInputStream(f) : new ImageInputStream(f)) {
            if (f.getName().endsWith(".xml")) {
                Attributes data = SAXReader.parse(new FileInputStream(f));
                processAndSend(data, cuid, iuid, syntax, f);
            } else if (noChange) {
                in.skip(fmiEndPos);
                dataWriter = new InputStreamDataWriter(in);
                as.cstore(
                        cuid,
                        iuid,
                        priority,
                        dataWriter,
                        syntax.getSuitable(),
                        rspHandlerFactory.createDimseRSPHandler(f));
            } else {
                ImageInputStream dicomIn = (ImageInputStream) in;
                dicomIn.setIncludeBulkData(ImageInputStream.IncludeBulkData.URI);
                Attributes data = dicomIn.readDataset();
                processAndSend(data, cuid, iuid, syntax, f);
            }
        }
    }

    private void processAndSend(
            Attributes data,
            String cuid,
            String iuid,
            ImageAdapter.AdaptTransferSyntax syntax,
            File f) throws IOException, InterruptedException {
        EditorContext context = new EditorContext(syntax.getOriginal(), Node.buildLocalDicomNode(as),
                Node.buildRemoteDicomNode(as));
        if (dicomEditors != null && !dicomEditors.isEmpty()) {
            dicomEditors.forEach(e -> e.apply(data, context));
            iuid = data.getString(Tag.SOPInstanceUID);
            cuid = data.getString(Tag.SOPClassUID);
        }
        if (attrs != null && Builder.updateAttributes(data, attrs, uidSuffix)) {
            iuid = data.getString(Tag.SOPInstanceUID);
        }

        BytesWithImageDescriptor desc = ImageAdapter.imageTranscode(data, syntax, context);
        DataWriter dataWriter = ImageAdapter.buildDataWriter(data, syntax, context.getEditable(), desc);
        as.cstore(cuid, iuid, priority, dataWriter, syntax.getSuitable(), rspHandlerFactory.createDimseRSPHandler(f));
    }

    /**
     * Closes the DICOM association.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    @Override
    public void close() throws IOException, InterruptedException {
        if (as != null) {
            if (as.isReadyForDataTransfer()) {
                as.release();
            }
            as.waitForSocketClose();
        }
    }

    /**
     * Establishes a DICOM association with the remote AE.
     *
     * @throws IOException              if an I/O error occurs.
     * @throws InterruptedException     if the connection is interrupted.
     * @throws InternalException        if a configuration error occurs.
     * @throws GeneralSecurityException if a security error occurs.
     */
    public void open() throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        as = ae.connect(remote, rq);
    }

    /**
     * Handles the C-STORE response from the SCP, updating status and logging information.
     *
     * @param cmd The C-STORE response command attributes.
     * @param f   The file that was sent.
     */
    private void onCStoreRSP(Attributes cmd, File f) {
        int status = cmd.getInt(Tag.Status, -1);
        state.setStatus(status);
        ProgressStatus ps;

        switch (status) {
            case Status.Success:
                totalSize += f.length();
                ps = ProgressStatus.COMPLETED;
                break;

            case Status.CoercionOfDataElements:
            case Status.ElementsDiscarded:
            case Status.DataSetDoesNotMatchSOPClassWarning:
                totalSize += f.length();
                ps = ProgressStatus.WARNING;
                Logger.warn("Received C-STORE-RSP with Status {}H for {}.", Tag.shortToHexString(status), f);
                Logger.warn(cmd.toString());
                break;

            default:
                ps = ProgressStatus.FAILED;
                Logger.error("Received C-STORE-RSP with Status {}H for {}.", Tag.shortToHexString(status), f);
                Logger.error(cmd.toString());
        }
        Builder.notifyProgession(state.getProgress(), cmd, ps, filesScanned);
    }

    /**
     * Gets the total number of files scanned.
     *
     * @return The number of files scanned.
     */
    public int getFilesScanned() {
        return filesScanned;
    }

    /**
     * Gets the total size in bytes of all successfully sent files.
     *
     * @return The total size.
     */
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * Gets the current status of the C-STORE operation.
     *
     * @return The status.
     */
    public Status getState() {
        return state;
    }

    /**
     * A factory for creating DIMSE response handlers for C-STORE operations.
     */
    public interface RSPHandlerFactory {

        /**
         * Creates a response handler for a C-STORE operation on a specific file.
         *
         * @param f The file being sent.
         * @return A new {@link DimseRSPHandler}.
         */
        DimseRSPHandler createDimseRSPHandler(File f);
    }

}
