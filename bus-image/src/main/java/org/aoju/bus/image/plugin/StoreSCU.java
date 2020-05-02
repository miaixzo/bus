/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.image.plugin;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.image.Status;
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.UID;
import org.aoju.bus.image.galaxy.Property;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.io.ImageInputStream;
import org.aoju.bus.image.galaxy.io.SAXReader;
import org.aoju.bus.image.metric.*;
import org.aoju.bus.image.metric.internal.pdu.AAssociateRQ;
import org.aoju.bus.image.metric.internal.pdu.CommonExtended;
import org.aoju.bus.image.metric.internal.pdu.Presentation;
import org.aoju.bus.image.nimble.codec.Decompressor;
import org.aoju.bus.logger.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class StoreSCU {

    private final ApplicationEntity ae;
    private final Connection remote;
    private final AAssociateRQ rq = new AAssociateRQ();
    private final RelatedGeneralSOPClasses relSOPClasses = new RelatedGeneralSOPClasses();
    private Attributes attrs;
    private String uidSuffix;
    private boolean relExtNeg;
    private int priority;
    private String tmpPrefix = "storescu-";
    private String tmpSuffix;
    private File tmpDir;
    private File tmpFile;
    private Association as;
    private long totalSize;
    private int filesScanned;
    private int filesSent;
    private RSPHandlerFactory rspHandlerFactory = new RSPHandlerFactory() {

        @Override
        public DimseRSPHandler createDimseRSPHandler(final File f) {

            return new DimseRSPHandler(as.nextMessageID()) {

                @Override
                public void onDimseRSP(Association as, Attributes cmd,
                                       Attributes data) {
                    super.onDimseRSP(as, cmd, data);
                    StoreSCU.this.onCStoreRSP(cmd, f);
                }
            };
        }
    };

    public StoreSCU(ApplicationEntity ae) {
        this.remote = new Connection();
        this.ae = ae;
        rq.addPresentationContext(new Presentation(1,
                UID.VerificationSOPClass, UID.ImplicitVRLittleEndian));
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

    public void scanFiles(List<String> fnames, boolean printout)
            throws IOException {
        tmpFile = File.createTempFile(tmpPrefix, tmpSuffix, tmpDir);
        tmpFile.deleteOnExit();
        final BufferedWriter fileInfos = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(tmpFile)));
        try {
            Common.scan(fnames, printout, (f, fmi, dsPos, ds) -> {
                if (!addFile(fileInfos, f, dsPos, fmi, ds))
                    return false;

                filesScanned++;
                return true;
            });
        } finally {
            fileInfos.close();
        }
    }

    public void sendFiles() throws IOException {
        BufferedReader fileInfos = new BufferedReader(new InputStreamReader(
                new FileInputStream(tmpFile)));
        try {
            String line;
            while (as.isReadyForDataTransfer()
                    && (line = fileInfos.readLine()) != null) {
                String[] ss = Property.split(line, '\t');
                try {
                    send(new File(ss[4]), Long.parseLong(ss[3]), ss[1], ss[0],
                            ss[2]);
                } catch (Exception e) {
                    throw new InstrumentException(e);
                }
            }
            try {
                as.waitForOutstandingRSP();
            } catch (InterruptedException e) {
                throw new InstrumentException(e);
            }
        } finally {
            IoUtils.close(fileInfos);
        }
    }

    public boolean addFile(BufferedWriter fileInfos, File f, long endFmi,
                           Attributes fmi, Attributes ds) throws IOException {
        String cuid = fmi.getString(Tag.MediaStorageSOPClassUID);
        String iuid = fmi.getString(Tag.MediaStorageSOPInstanceUID);
        String ts = fmi.getString(Tag.TransferSyntaxUID);
        if (cuid == null || iuid == null)
            return false;

        fileInfos.write(iuid);
        fileInfos.write('\t');
        fileInfos.write(cuid);
        fileInfos.write('\t');
        fileInfos.write(ts);
        fileInfos.write('\t');
        fileInfos.write(Long.toString(endFmi));
        fileInfos.write('\t');
        fileInfos.write(f.getPath());
        fileInfos.newLine();

        if (rq.containsPresentationContextFor(cuid, ts))
            return true;

        if (!rq.containsPresentationContextFor(cuid)) {
            if (relExtNeg)
                rq.addCommonExtendedNegotiation(relSOPClasses
                        .getCommonExtendedNegotiation(cuid));
            if (!ts.equals(UID.ExplicitVRLittleEndian))
                rq.addPresentationContext(new Presentation(rq
                        .getNumberOfPresentationContexts() * 2 + 1, cuid,
                        UID.ExplicitVRLittleEndian));
            if (!ts.equals(UID.ImplicitVRLittleEndian))
                rq.addPresentationContext(new Presentation(rq
                        .getNumberOfPresentationContexts() * 2 + 1, cuid,
                        UID.ImplicitVRLittleEndian));
        }
        rq.addPresentationContext(new Presentation(rq
                .getNumberOfPresentationContexts() * 2 + 1, cuid, ts));
        return true;
    }

    public void echo() throws IOException, InterruptedException {
        as.cecho().next();
    }

    public void send(final File f, long fmiEndPos, String cuid, String iuid,
                     String filets) throws IOException, InterruptedException,
            ParserConfigurationException, SAXException {
        String ts = selectTransferSyntax(cuid, filets);

        if (f.getName().endsWith(".xml")) {
            Attributes parsedDicomFile = SAXReader.parse(new FileInputStream(f));
            if (Common.updateAttributes(parsedDicomFile, attrs, uidSuffix))
                iuid = parsedDicomFile.getString(Tag.SOPInstanceUID);
            if (!ts.equals(filets)) {
                Decompressor.decompress(parsedDicomFile, filets);
            }
            as.cstore(cuid, iuid, priority,
                    new DataWriterAdapter(parsedDicomFile), ts,
                    rspHandlerFactory.createDimseRSPHandler(f));
        } else {
            if (uidSuffix == null && attrs.isEmpty() && ts.equals(filets)) {
                FileInputStream in = new FileInputStream(f);
                try {
                    in.skip(fmiEndPos);
                    InputStreamWriter data = new InputStreamWriter(in);
                    as.cstore(cuid, iuid, priority, data, ts,
                            rspHandlerFactory.createDimseRSPHandler(f));
                } finally {
                    IoUtils.close(in);
                }
            } else {
                ImageInputStream in = new ImageInputStream(f);
                try {
                    in.setIncludeBulkData(ImageInputStream.IncludeBulkData.URI);
                    Attributes data = in.readDataset(-1, -1);
                    if (Common.updateAttributes(data, attrs, uidSuffix))
                        iuid = data.getString(Tag.SOPInstanceUID);
                    if (!ts.equals(filets)) {
                        Decompressor.decompress(data, filets);
                    }
                    as.cstore(cuid, iuid, priority,
                            new DataWriterAdapter(data), ts,
                            rspHandlerFactory.createDimseRSPHandler(f));
                } finally {
                    IoUtils.close(in);
                }
            }
        }
    }

    private String selectTransferSyntax(String cuid, String filets) {
        Set<String> tss = as.getTransferSyntaxesFor(cuid);
        if (tss.contains(filets))
            return filets;

        if (tss.contains(UID.ExplicitVRLittleEndian))
            return UID.ExplicitVRLittleEndian;

        return UID.ImplicitVRLittleEndian;
    }

    public void close() throws IOException, InterruptedException {
        if (as != null) {
            if (as.isReadyForDataTransfer())
                as.release();
            as.waitForSocketClose();
        }
    }

    public void open() throws IOException, InterruptedException,
            InstrumentException, GeneralSecurityException {
        as = ae.connect(remote, rq);
    }

    private void onCStoreRSP(Attributes cmd, File f) {
        int status = cmd.getInt(Tag.Status, -1);
        switch (status) {
            case Status.Success:
                totalSize += f.length();
                ++filesSent;
                break;
            case Status.CoercionOfDataElements:
            case Status.ElementsDiscarded:
            case Status.DataSetDoesNotMatchSOPClassWarning:
                totalSize += f.length();
                ++filesSent;
                Logger.warn(Tag.shortToHexString(status));
                break;
            default:
                Logger.error(Tag.shortToHexString(status));
        }
    }

    public interface RSPHandlerFactory {
        DimseRSPHandler createDimseRSPHandler(File f);
    }

    class RelatedGeneralSOPClasses {

        private final HashMap<String, CommonExtended> commonExtNegs = new HashMap<>();

        public void init(Properties props) {
            for (String cuid : props.stringPropertyNames())
                commonExtNegs.put(cuid, new CommonExtended(cuid,
                        UID.StorageServiceClass,
                        Property.split(props.getProperty(cuid), Symbol.C_COMMA)));
        }

        public CommonExtended getCommonExtendedNegotiation(String cuid) {
            CommonExtended commonExtNeg = commonExtNegs.get(cuid);
            return commonExtNeg != null
                    ? commonExtNeg
                    : new CommonExtended(cuid, UID.StorageServiceClass);
        }
    }

}
