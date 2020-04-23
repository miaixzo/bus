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

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.image.Device;
import org.aoju.bus.image.Status;
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.UID;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.Sequence;
import org.aoju.bus.image.galaxy.data.VR;
import org.aoju.bus.image.galaxy.io.DicomInputStream;
import org.aoju.bus.image.galaxy.io.DicomOutputStream;
import org.aoju.bus.image.galaxy.io.SAXReader;
import org.aoju.bus.image.galaxy.io.SAXWriter;
import org.aoju.bus.image.metric.*;
import org.aoju.bus.image.metric.pdu.AAssociateRQ;
import org.aoju.bus.image.metric.pdu.ExtendedNegotiation;
import org.aoju.bus.image.metric.pdu.PresentationContext;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Kimi Liu
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 * @since JDK 1.8+
 */
public class FindSCU {

    private static SAXTransformerFactory saxtf;
    private final Device device = new Device("findscu");
    private final ApplicationEntity ae = new ApplicationEntity("FINDSCU");
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private int priority;
    private int cancelAfter;
    private InformationModel model;
    private File outDir;
    private DecimalFormat outFileFormat;
    private int[] inFilter;
    private Attributes keys = new Attributes();
    private boolean catOut = false;
    private boolean xml = false;
    private boolean xmlIndent = false;
    private boolean xmlIncludeKeyword = true;
    private boolean xmlIncludeNamespaceDeclaration = false;
    private File xsltFile;
    private Templates xsltTpls;
    private OutputStream out;
    private Association as;
    private AtomicInteger totNumMatches = new AtomicInteger();

    public FindSCU() throws IOException {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
    }

    static void mergeKeys(Attributes attrs, Attributes keys) {
        try {
            attrs.accept(new MergeNested(keys), false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        attrs.addAll(keys);
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final void setInformationModel(InformationModel model, String[] tss,
                                          EnumSet<QueryOption> queryOptions) {
        this.model = model;
        rq.addPresentationContext(new PresentationContext(1, model.cuid, tss));
        if (!queryOptions.isEmpty()) {
            model.adjustQueryOptions(queryOptions);
            rq.addExtendedNegotiation(new ExtendedNegotiation(model.cuid,
                    QueryOption.toExtendedNegotiationInformation(queryOptions)));
        }
        if (model.level != null)
            addLevel(model.level);
    }

    public void addLevel(String s) {
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, s);
    }

    public final void setCancelAfter(int cancelAfter) {
        this.cancelAfter = cancelAfter;
    }

    public final void setOutputDirectory(File outDir) {
        outDir.mkdirs();
        this.outDir = outDir;
    }

    public final void setOutputFileFormat(String outFileFormat) {
        this.outFileFormat = new DecimalFormat(outFileFormat);
    }

    public final void setXSLT(File xsltFile) {
        this.xsltFile = xsltFile;
    }

    public final void setXML(boolean xml) {
        this.xml = xml;
    }

    public final void setXMLIndent(boolean indent) {
        this.xmlIndent = indent;
    }

    public final void setXMLIncludeKeyword(boolean includeKeyword) {
        this.xmlIncludeKeyword = includeKeyword;
    }

    public final void setXMLIncludeNamespaceDeclaration(
            boolean includeNamespaceDeclaration) {
        this.xmlIncludeNamespaceDeclaration = includeNamespaceDeclaration;
    }

    public final void setConcatenateOutputFiles(boolean catOut) {
        this.catOut = catOut;
    }

    public final void setInputFilter(int[] inFilter) {
        this.inFilter = inFilter;
    }

    public ApplicationEntity getApplicationEntity() {
        return ae;
    }

    public Connection getRemoteConnection() {
        return remote;
    }

    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    public Association getAssociation() {
        return as;
    }

    public Device getDevice() {
        return device;
    }

    public Attributes getKeys() {
        return keys;
    }

    public void open() throws IOException, InterruptedException,
            InstrumentException, GeneralSecurityException {
        as = ae.connect(conn, remote, rq);
    }

    public void close() throws IOException, InterruptedException {
        if (as != null && as.isReadyForDataTransfer()) {
            as.waitForOutstandingRSP();
            as.release();
        }
        IoUtils.close(out);
        out = null;
    }

    public void query(File f) throws Exception {
        Attributes attrs;
        String filePath = f.getPath();
        String fileExt = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
        DicomInputStream dis = null;
        try {
            attrs = fileExt.equals("xml")
                    ? SAXReader.parse(filePath)
                    : new DicomInputStream(f).readDataset(-1, -1);
            if (inFilter != null) {
                attrs = new Attributes(inFilter.length + 1);
                attrs.addSelected(attrs, inFilter);
            }
        } finally {
            IoUtils.close(dis);
        }
        mergeKeys(attrs, keys);
        query(attrs);
    }

    public void query() throws IOException, InterruptedException {
        query(keys);
    }

    private void query(Attributes keys) throws IOException, InterruptedException {
        DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            int cancelAfter = FindSCU.this.cancelAfter;
            int numMatches;

            @Override
            public void onDimseRSP(Association as, Attributes cmd,
                                   Attributes data) {
                super.onDimseRSP(as, cmd, data);
                int status = cmd.getInt(Tag.Status, -1);
                if (Status.isPending(status)) {
                    FindSCU.this.onResult(data);
                    ++numMatches;
                    if (cancelAfter != 0 && numMatches >= cancelAfter)
                        try {
                            cancel(as);
                            cancelAfter = 0;
                        } catch (IOException e) {
                            throw new InstrumentException(e);
                        }
                }
            }
        };

        query(keys, rspHandler);
    }

    public void query(DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        query(keys, rspHandler);
    }

    private void query(Attributes keys, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        as.cfind(model.cuid, priority, keys, null, rspHandler);
    }

    private void onResult(Attributes data) {
        int numMatches = totNumMatches.incrementAndGet();
        if (outDir == null)
            return;

        try {
            if (out == null) {
                File f = new File(outDir, fname(numMatches));
                out = new BufferedOutputStream(
                        new FileOutputStream(f));
            }
            if (xml) {
                writeAsXML(data, out);
            } else {
                DicomOutputStream dos =
                        new DicomOutputStream(out, UID.ImplicitVRLittleEndian);
                dos.writeDataset(null, data);
            }
            out.flush();
        } catch (Exception e) {
            IoUtils.close(out);
            out = null;
            throw new InstrumentException(e);
        } finally {
            if (!catOut) {
                IoUtils.close(out);
                out = null;
            }
        }
    }

    private String fname(int i) {
        synchronized (outFileFormat) {
            return outFileFormat.format(i);
        }
    }

    private void writeAsXML(Attributes attrs, OutputStream out) throws Exception {
        TransformerHandler th = getTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT,
                xmlIndent ? "yes" : "no");
        th.setResult(new StreamResult(out));
        SAXWriter saxWriter = new SAXWriter(th);
        saxWriter.setIncludeKeyword(xmlIncludeKeyword);
        saxWriter.setIncludeNamespaceDeclaration(xmlIncludeNamespaceDeclaration);
        saxWriter.write(attrs);
    }

    private TransformerHandler getTransformerHandler() throws Exception {
        SAXTransformerFactory tf = saxtf;
        if (tf == null)
            saxtf = tf = (SAXTransformerFactory) TransformerFactory
                    .newInstance();
        if (xsltFile == null)
            return tf.newTransformerHandler();

        Templates tpls = xsltTpls;
        if (tpls == null) ;
        xsltTpls = tpls = tf.newTemplates(new StreamSource(xsltFile));

        return tf.newTransformerHandler(tpls);
    }

    public enum InformationModel {
        PatientRoot(UID.PatientRootQueryRetrieveInformationModelFIND, "STUDY"),
        StudyRoot(UID.StudyRootQueryRetrieveInformationModelFIND, "STUDY"),
        PatientStudyOnly(UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired, "STUDY"),
        MWL(UID.ModalityWorklistInformationModelFIND, null),
        UPSPull(UID.UnifiedProcedureStepPullSOPClass, null),
        UPSWatch(UID.UnifiedProcedureStepWatchSOPClass, null),
        HangingProtocol(UID.HangingProtocolInformationModelFIND, null),
        ColorPalette(UID.ColorPaletteQueryRetrieveInformationModelFIND, null);

        final String cuid;
        final String level;

        InformationModel(String cuid, String level) {
            this.cuid = cuid;
            this.level = level;
        }

        public void adjustQueryOptions(EnumSet<QueryOption> queryOptions) {
            if (level == null) {
                queryOptions.add(QueryOption.RELATIONAL);
                queryOptions.add(QueryOption.DATETIME);
            }
        }
    }

    private static class MergeNested implements Attributes.Visitor {
        private final Attributes keys;

        MergeNested(Attributes keys) {
            this.keys = keys;
        }

        private static boolean isNotEmptySequence(Object val) {
            return val instanceof Sequence && !((Sequence) val).isEmpty();
        }

        @Override
        public boolean visit(Attributes attrs, int tag, VR vr, Object val) {
            if (isNotEmptySequence(val)) {
                Object o = keys.remove(tag);
                if (isNotEmptySequence(o))
                    ((Sequence) val).get(0).addAll(((Sequence) o).get(0));
            }
            return true;
        }
    }

}
