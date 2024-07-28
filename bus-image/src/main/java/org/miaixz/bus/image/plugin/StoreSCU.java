/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.builtin.DicomFiles;
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
import java.text.MessageFormat;
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class StoreSCU implements AutoCloseable {

    public final RelatedSOPClasses relSOPClasses = new RelatedSOPClasses();
    private final ApplicationEntity ae;
    private final Connection remote;
    private final AAssociateRQ rq = new AAssociateRQ();
    private final List<Editors> dicomEditors;
    private final Status state;
    private Attributes attrs;
    private String uidSuffix;
    private boolean relExtNeg;
    private int priority;
    private String tmpPrefix = "storescu-";
    private String tmpSuffix;
    private File tmpDir;
    private File tmpFile;
    private Association as;
    private long totalSize = 0;
    private int filesScanned;
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

    public StoreSCU(ApplicationEntity ae, ImageProgress progress) {
        this(ae, progress, null);
    }

    public StoreSCU(ApplicationEntity ae, ImageProgress progress, List<Editors> dicomEditors) {
        this.remote = new Connection();
        this.ae = ae;
        rq.addPresentationContext(new PresentationContext(1, UID.Verification.uid, UID.ImplicitVRLittleEndian.uid));
        this.state = new Status(progress);
        this.dicomEditors = dicomEditors;
    }

    public void setRspHandlerFactory(RSPHandlerFactory rspHandlerFactory) {
        this.rspHandlerFactory = rspHandlerFactory;
    }

    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    public Connection getRemoteConnection() {
        return remote;
    }

    public Attributes getAttributes() {
        return attrs;
    }

    public void setAttributes(Attributes attrs) {
        this.attrs = attrs;
    }

    public void setTmpFile(File tmpFile) {
        this.tmpFile = tmpFile;
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final void setUIDSuffix(String uidSuffix) {
        this.uidSuffix = uidSuffix;
    }

    public final void setTmpFilePrefix(String prefix) {
        this.tmpPrefix = prefix;
    }

    public final void setTmpFileSuffix(String suffix) {
        this.tmpSuffix = suffix;
    }

    public final void setTmpFileDirectory(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    public final void enableSOPClassRelationshipExtNeg(boolean enable) {
        relExtNeg = enable;
    }

    public void scanFiles(List<String> fnames) throws IOException {
        this.scanFiles(fnames, true);
    }

    public void scanFiles(List<String> fnames, boolean printout) throws IOException {
        tmpFile = File.createTempFile(tmpPrefix, tmpSuffix, tmpDir);
        tmpFile.deleteOnExit();
        try (BufferedWriter fileInfos = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile)))) {
            DicomFiles.scan(fnames, printout, (f, fmi, dsPos, ds) -> {
                if (!addFile(fileInfos, f, dsPos, fmi, ds)) {
                    return false;
                }

                filesScanned++;
                return true;
            });
        }
    }

    public void sendFiles() throws IOException {
        BufferedReader fileInfos = new BufferedReader(new InputStreamReader(new FileInputStream(tmpFile)));
        try {
            String line;
            while (as.isReadyForDataTransfer() && (line = fileInfos.readLine()) != null) {
                ImageProgress p = state.getProgress();
                if (p != null) {
                    if (p.isCancel()) {
                        Logger.info("Aborting C-Store: {}", "cancel by progress");
                        as.abort();
                        break;
                    }
                }
                String[] ss = StringKit.splitToArray(line, Symbol.HT);
                try {
                    send(new File(ss[4]), Long.parseLong(ss[3]), ss[1], ss[0], ss[2]);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    Logger.error("Cannot send file", e);
                }
            }
            try {
                as.waitForOutstandingRSP();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.error("Waiting for RSP", e);
            }
        } finally {
            IoKit.close(fileInfos);
        }
    }

    public boolean addFile(BufferedWriter fileInfos, File f, long endFmi, Attributes fmi, Attributes ds)
            throws IOException {
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
                rq.addPresentationContext(new PresentationContext(rq.getNumberOfPresentationContexts() * 2 + 1, cuid,
                        UID.ExplicitVRLittleEndian.uid));
            }
            if (!ts.equals(UID.ImplicitVRLittleEndian.uid)) {
                rq.addPresentationContext(new PresentationContext(rq.getNumberOfPresentationContexts() * 2 + 1, cuid,
                        UID.ImplicitVRLittleEndian.uid));
            }
        }
        rq.addPresentationContext(new PresentationContext(rq.getNumberOfPresentationContexts() * 2 + 1, cuid, ts));
        return true;
    }

    public Attributes echo() throws IOException, InterruptedException {
        DimseRSP response = as.cecho();
        response.next();
        return response.getCommand();
    }

    public void send(final File f, long fmiEndPos, String cuid, String iuid, String tsuid)
            throws IOException, InterruptedException, ParserConfigurationException, SAXException {
        ImageAdapter.AdaptTransferSyntax syntax = new ImageAdapter.AdaptTransferSyntax(tsuid,
                StreamSCU.selectTransferSyntax(as, cuid, tsuid));
        boolean noChange = uidSuffix == null && attrs.isEmpty() && syntax.getRequested().equals(tsuid)
                && dicomEditors == null;
        DataWriter dataWriter = null;
        InputStream in = null;
        Attributes data = null;
        try {
            if (f.getName().endsWith(".xml")) {
                in = new FileInputStream(f);
                data = SAXReader.parse(in);
                noChange = false;
            } else if (noChange) {
                in = new FileInputStream(f);
                in.skip(fmiEndPos);
                dataWriter = new InputStreamDataWriter(in);
            } else {
                in = new ImageInputStream(f);
                ((ImageInputStream) in).setIncludeBulkData(ImageInputStream.IncludeBulkData.URI);
                data = ((ImageInputStream) in).readDataset();
            }

            if (!noChange) {
                EditorContext context = new EditorContext(syntax.getOriginal(), Node.buildLocalDicomNode(as),
                        Node.buildRemoteDicomNode(as));
                if (dicomEditors != null && !dicomEditors.isEmpty()) {
                    final Attributes attributes = data;
                    dicomEditors.forEach(e -> e.apply(attributes, context));
                    iuid = data.getString(Tag.SOPInstanceUID);
                    cuid = data.getString(Tag.SOPClassUID);
                }
                if (Builder.updateAttributes(data, attrs, uidSuffix)) {
                    iuid = data.getString(Tag.SOPInstanceUID);
                }

                BytesWithImageDescriptor desc = ImageAdapter.imageTranscode(data, syntax, context);
                dataWriter = ImageAdapter.buildDataWriter(data, syntax, context.getEditable(), desc);
            }
            as.cstore(cuid, iuid, priority, dataWriter, syntax.getSuitable(),
                    rspHandlerFactory.createDimseRSPHandler(f));
        } finally {
            IoKit.close(in);
        }
    }

    @Override
    public void close() throws IOException, InterruptedException {
        if (as != null) {
            if (as.isReadyForDataTransfer()) {
                as.release();
            }
            as.waitForSocketClose();
        }
    }

    public void open() throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        as = ae.connect(remote, rq);
    }

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
            Logger.error(MessageFormat.format("WARNING: Received C-STORE-RSP with Status {0}H for {1}",
                    Tag.shortToHexString(status), f));
            Logger.error(cmd.toString());
            break;
        default:
            ps = ProgressStatus.FAILED;
            Logger.error(MessageFormat.format("ERROR: Received C-STORE-RSP with Status {0}H for {1}",
                    Tag.shortToHexString(status), f));
            Logger.error(cmd.toString());
        }
        Builder.notifyProgession(state.getProgress(), cmd, ps, filesScanned);
    }

    public int getFilesScanned() {
        return filesScanned;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public Status getState() {
        return state;
    }

    public interface RSPHandlerFactory {
        DimseRSPHandler createDimseRSPHandler(File f);
    }

}
