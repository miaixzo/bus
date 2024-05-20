/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ContentHandlerAdapter;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.SAXReader;
import org.miaixz.bus.image.metric.*;
import org.miaixz.bus.image.metric.internal.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.internal.pdu.CommonExtended;
import org.miaixz.bus.image.metric.internal.pdu.Presentation;
import org.miaixz.bus.image.nimble.codec.Decompressor;
import org.miaixz.bus.logger.Logger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class StoreSCU implements AutoCloseable {

    private static SAXParser saxParser;
    public final SOPClasses relSOPClasses = new SOPClasses();
    private final ApplicationEntity ae;
    private final Connection remote;
    private final AAssociateRQ rq = new AAssociateRQ();
    private final Editors attributesEditors;
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
            StoreSCU.this.onCStoreRSP(cmd, file);

            Progress progress = state.getProgress();
            if (null != progress) {
                progress.setProcessedFile(file);
                progress.setAttributes(cmd);
            }
        }
    };

    public StoreSCU(ApplicationEntity ae, Progress progress) throws IOException {
        this(ae, progress, null);
    }

    public StoreSCU(ApplicationEntity ae, Progress progress, Editors attributesEditors) {
        this.remote = new Connection();
        this.ae = ae;
        rq.addPresentationContext(new Presentation(1, UID.VerificationSOPClass, UID.ImplicitVRLittleEndian));
        this.state = new Status(progress);
        this.attributesEditors = attributesEditors;
    }

    public static boolean updateAttributes(Attributes data, Attributes attrs, String uidSuffix) {
        if (attrs.isEmpty() && null == uidSuffix) {
            return false;
        }
        if (null != uidSuffix) {
            data.setString(Tag.StudyInstanceUID, VR.UI, data.getString(Tag.StudyInstanceUID) + uidSuffix);
            data.setString(Tag.SeriesInstanceUID, VR.UI, data.getString(Tag.SeriesInstanceUID) + uidSuffix);
            data.setString(Tag.SOPInstanceUID, VR.UI, data.getString(Tag.SOPInstanceUID) + uidSuffix);
        }
        data.update(Attributes.UpdatePolicy.OVERWRITE, attrs, null);
        return true;
    }

    public static void scan(List<String> fnames, Callback scb) {
        scan(fnames, true, scb);
    }

    public static void scan(List<String> fnames, boolean printout, Callback scb) {
        for (String fname : fnames) {
            scan(new File(fname), printout, scb);
        }
    }

    private static void scan(File f, boolean printout, Callback scb) {
        if (f.isDirectory()) {
            for (String s : f.list()) {
                scan(new File(f, s), printout, scb);
            }
            return;
        }
        if (f.getName().endsWith(".xml")) {
            try {
                SAXParser p = saxParser;
                if (null == p) {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                    saxParser = p = factory.newSAXParser();
                }
                Attributes ds = new Attributes();
                ContentHandlerAdapter ch = new ContentHandlerAdapter(ds);
                p.parse(f, ch);
                Attributes fmi = ch.getFileMetaInformation();
                if (null == fmi) {
                    fmi = ds.createFileMetaInformation(UID.ExplicitVRLittleEndian);
                }
                boolean b = scb.dicomFile(f, fmi, -1, ds);
                if (printout) {
                    Logger.debug(String.valueOf(b ? Symbol.C_DOT : 'I'));
                }
            } catch (Exception e) {
                Logger.error("Failed to parse file " + f + ": " + e.getMessage());
                e.printStackTrace(System.out);
            }
        } else {
            ImageInputStream in = null;
            try {
                in = new ImageInputStream(f);
                in.setIncludeBulkData(ImageInputStream.IncludeBulkData.NO);
                Attributes fmi = in.readFileMetaInformation();
                long dsPos = in.getPosition();
                Attributes ds = in.readDataset(-1, Tag.PixelData);
                if (null == fmi || !fmi.containsValue(Tag.TransferSyntaxUID)
                        || !fmi.containsValue(Tag.MediaStorageSOPClassUID)
                        || !fmi.containsValue(Tag.MediaStorageSOPInstanceUID)) {
                    fmi = ds.createFileMetaInformation(in.getTransferSyntax());
                }
                boolean b = scb.dicomFile(f, fmi, dsPos, ds);
                if (printout) {
                    Logger.debug(String.valueOf(b ? Symbol.C_DOT : 'I'));
                }
            } catch (Exception e) {
                Logger.error("Failed to scan file " + f + ": " + e.getMessage());
            } finally {
                IoKit.close(in);
            }
        }
    }

    public static String selectTransferSyntax(Association as, String cuid, String filets) {
        Set<String> tss = as.getTransferSyntaxesFor(cuid);
        if (tss.contains(filets)) {
            return filets;
        }

        if (tss.contains(UID.ExplicitVRLittleEndian)) {
            return UID.ExplicitVRLittleEndian;
        }

        return UID.ImplicitVRLittleEndian;
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
            this.scan(fnames, printout, (f, fmi, dsPos, ds) -> {
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
            while (as.isReadyForDataTransfer() && null != (line = fileInfos.readLine())) {
                Progress p = state.getProgress();
                if (null != p) {
                    if (p.isCancel()) {
                        Logger.info("Aborting C-Store: {}", "cancel by progress");
                        as.abort();
                        break;
                    }
                }
                String[] ss = StringKit.splitToArray(line, Symbol.HT);
                try {
                    send(new File(ss[4]), Long.parseLong(ss[3]), ss[1], ss[0], ss[2]);
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
        if (null == cuid || null == iuid) {
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
                rq.addCommonExtendedNegotiation(relSOPClasses.getCommonExtendedNegotiation(cuid));
            }
            if (!ts.equals(UID.ExplicitVRLittleEndian)) {
                rq.addPresentationContext(new Presentation(rq.getNumberOfPresentationContexts() * 2 + 1, cuid,
                        UID.ExplicitVRLittleEndian));
            }
            if (!ts.equals(UID.ImplicitVRLittleEndian)) {
                rq.addPresentationContext(new Presentation(rq.getNumberOfPresentationContexts() * 2 + 1, cuid,
                        UID.ImplicitVRLittleEndian));
            }
        }
        rq.addPresentationContext(new Presentation(rq.getNumberOfPresentationContexts() * 2 + 1, cuid, ts));
        return true;
    }

    public Attributes echo() throws IOException, InterruptedException {
        DimseRSP response = as.cecho();
        response.next();
        return response.getCommand();
    }

    public void send(final File f, long fmiEndPos, String cuid, String iuid, String filets)
            throws IOException, InterruptedException, ParserConfigurationException, SAXException {
        String ts = selectTransferSyntax(as, cuid, filets);

        boolean noChange = null == uidSuffix && attrs.isEmpty() && ts.equals(filets) && null == attributesEditors;
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
                dataWriter = new InputStreamWriter(in);
            } else {
                in = new ImageInputStream(f);
                ((ImageInputStream) in).setIncludeBulkData(ImageInputStream.IncludeBulkData.URI);
                data = ((ImageInputStream) in).readDataset(-1, -1);
            }

            if (!noChange) {
                if (null != attributesEditors) {
                    AttributeContext context = new AttributeContext(ts, Node.buildLocalDicomNode(as),
                            Node.buildRemoteDicomNode(as));
                    if (attributesEditors.apply(data, context)) {
                        iuid = data.getString(Tag.SOPInstanceUID);
                    }
                }
                if (updateAttributes(data, attrs, uidSuffix)) {
                    iuid = data.getString(Tag.SOPInstanceUID);
                }
                if (!ts.equals(filets)) {
                    Decompressor.decompress(data, filets);
                }
                dataWriter = new DataWriterAdapter(data);
            }
            as.cstore(cuid, iuid, priority, dataWriter, ts, rspHandlerFactory.createDimseRSPHandler(f));
        } finally {
            IoKit.close(in);
        }
    }

    @Override
    public void close() throws IOException, InterruptedException {
        if (null != as) {
            if (as.isReadyForDataTransfer()) {
                as.release();
            }
            as.waitForSocketClose();
        }
    }

    public void open()
            throws IOException, InterruptedException, GeneralSecurityException {
        as = ae.connect(remote, rq);
    }

    private void onCStoreRSP(Attributes cmd, File f) {
        int status = cmd.getInt(Tag.Status, -1);
        state.setStatus(status);
        String ps;

        switch (status) {
            case Status.Success:
                totalSize += f.length();
                ps = Builder.COMPLETED;
                break;
            case Status.CoercionOfDataElements:
            case Status.ElementsDiscarded:
            case Status.DataSetDoesNotMatchSOPClassWarning:
                totalSize += f.length();
                ps = Builder.WARNING;
                System.err.println(MessageFormat.format("WARNING: Received C-STORE-RSP with Status {0}H for {1}",
                        Tag.shortToHexString(status), f));
                System.err.println(cmd);
                break;
            default:
                ps = Builder.FAILED;
                System.err.println(MessageFormat.format("ERROR: Received C-STORE-RSP with Status {0}H for {1}",
                        Tag.shortToHexString(status), f));
                System.err.println(cmd);
        }
        Builder.notify(state.getProgress(), cmd, ps, filesScanned);
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

    public interface Callback {
        boolean dicomFile(File f, Attributes fmi, long dsPos, Attributes ds) throws Exception;
    }

    public class SOPClasses {

        private final HashMap<String, CommonExtended> commonExtNegs = new HashMap<>();

        public void init(Properties props) {
            for (String cuid : props.stringPropertyNames()) {
                commonExtNegs.put(cuid, new CommonExtended(cuid, UID.StorageServiceClass,
                        StringKit.splitToArray(props.getProperty(cuid), Symbol.COMMA)));
            }
        }

        public CommonExtended getCommonExtendedNegotiation(String cuid) {
            CommonExtended commonExtNeg = commonExtNegs.get(cuid);
            return null != commonExtNeg ? commonExtNeg : new CommonExtended(cuid, UID.StorageServiceClass);
        }

    }

}
