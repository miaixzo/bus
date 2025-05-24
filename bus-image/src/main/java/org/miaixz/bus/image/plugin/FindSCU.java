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
import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.ImageProgress;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.Sequence;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.data.Visitor;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.galaxy.io.SAXReader;
import org.miaixz.bus.image.galaxy.io.SAXWriter;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.DimseRSPHandler;
import org.miaixz.bus.image.metric.QueryOption;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.pdu.ExtendedNegotiation;
import org.miaixz.bus.image.metric.pdu.PresentationContext;
import org.miaixz.bus.logger.Logger;

/**
 * findscu应用程序为查询/检索、Modality工作列表管理、统一工作列表和过程步骤 挂起协议Query/Retrieve 支持Query/Retrieve服务类实现一个服务类用户(SCU)
 * findscu只支持使用C-FIND消息的查询功能
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FindSCU implements AutoCloseable {

    private final Device device = new Device("findscu");
    private final ApplicationEntity ae = new ApplicationEntity("FINDSCU");
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private final Attributes keys = new Attributes();
    private final AtomicInteger totNumMatches = new AtomicInteger();
    private final Status state;
    private SAXTransformerFactory saxtf;
    private int priority;
    private int cancelAfter;
    private InformationModel model;
    private File outDir;
    private DecimalFormat outFileFormat;
    private int[] inFilter;
    private boolean catOut = false;
    private boolean xml = false;
    private boolean xmlIndent = false;
    private boolean xmlIncludeKeyword = true;
    private boolean xmlIncludeNamespaceDeclaration = false;
    private File xsltFile;
    private Templates xsltTpls;
    private OutputStream out;
    private Association as;

    public FindSCU() {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
        state = new Status(new ImageProgress());
    }

    static void mergeKeys(Attributes attrs, Attributes keys) {
        try {
            attrs.accept(new MergeNested(keys), false);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        attrs.addAll(keys);
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final void setInformationModel(InformationModel model, String[] tss, EnumSet<QueryOption> queryOptions) {
        this.model = model;
        rq.addPresentationContext(new PresentationContext(1, model.cuid, tss));
        if (!queryOptions.isEmpty()) {
            model.adjustQueryOptions(queryOptions);
            rq.addExtendedNegotiation(
                    new ExtendedNegotiation(model.cuid, QueryOption.toExtendedNegotiationInformation(queryOptions)));
        }
        if (model.level != null) {
            addLevel(model.level);
        }
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

    public final void setXMLIncludeNamespaceDeclaration(boolean includeNamespaceDeclaration) {
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

    public void open() throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        as = ae.connect(conn, remote, rq);
    }

    @Override
    public void close() throws IOException, InterruptedException {
        if (as != null && as.isReadyForDataTransfer()) {
            as.waitForOutstandingRSP();
            as.release();
        }
        IoKit.close(out);
        out = null;
    }

    public void query(File f) throws Exception {
        Attributes attrs;
        String filePath = f.getPath();
        String fileExt = filePath.substring(filePath.lastIndexOf(Symbol.C_DOT) + 1).toLowerCase();

        if (fileExt.equals("xml")) {
            attrs = SAXReader.parse(filePath);
        } else {
            try (ImageInputStream dis = new ImageInputStream(f)) {
                attrs = dis.readDataset();
            }
        }
        if (inFilter != null) {
            attrs = new Attributes(inFilter.length + 1);
            attrs.addSelected(attrs, inFilter);
        }
        mergeKeys(attrs, keys);
        query(attrs);
    }

    public void query() throws IOException, InterruptedException {
        query(keys);
    }

    private void query(Attributes keys) throws IOException, InterruptedException {
        DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            int cancelAfter;
            int numMatches;

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                int status = cmd.getInt(Tag.Status, -1);
                if (Status.isPending(status)) {
                    onResult(data);
                    ++numMatches;
                    if (cancelAfter != 0 && numMatches >= cancelAfter) {
                        try {
                            cancel(as);
                            cancelAfter = 0;
                        } catch (IOException e) {
                            Logger.error("Building response", e);
                        }
                    }
                } else {
                    state.setStatus(status);
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
        state.addDicomRSP(data);
        int numMatches = totNumMatches.incrementAndGet();
        if (outDir == null) {
            return;
        }

        try {
            if (out == null) {
                File f = new File(outDir, fname(numMatches));
                out = new BufferedOutputStream(new FileOutputStream(f));
            }
            if (xml) {
                writeAsXML(data, out);
            } else {
                // Do not close DicomOutputStream until catOut is false. Only "out" needs to be closed
                ImageOutputStream dos = new ImageOutputStream(out, UID.ImplicitVRLittleEndian.uid); // NOSONAR
                dos.writeDataset(null, data);
            }
            out.flush();
        } catch (Exception e) {
            Logger.error("Building response", e);
            IoKit.close(out);
            out = null;
        } finally {
            if (!catOut) {
                IoKit.close(out);
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
        th.getTransformer().setOutputProperty(OutputKeys.INDENT, xmlIndent ? "yes" : "no");
        th.setResult(new StreamResult(out));
        SAXWriter saxWriter = new SAXWriter(th);
        saxWriter.setIncludeKeyword(xmlIncludeKeyword);
        saxWriter.setIncludeNamespaceDeclaration(xmlIncludeNamespaceDeclaration);
        saxWriter.write(attrs);
    }

    private TransformerHandler getTransformerHandler() throws Exception {
        SAXTransformerFactory tf = saxtf;
        if (tf == null) {
            saxtf = tf = (SAXTransformerFactory) TransformerFactory.newInstance();
            tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        }
        if (xsltFile == null) {
            return tf.newTransformerHandler();
        }

        Templates tpls = xsltTpls;
        if (tpls == null) {
            xsltTpls = tpls = tf.newTemplates(new StreamSource(xsltFile));
        }

        return tf.newTransformerHandler(tpls);
    }

    public Connection getConnection() {
        return conn;
    }

    public Status getState() {
        return state;
    }

    public enum InformationModel {
        PatientRoot(UID.PatientRootQueryRetrieveInformationModelFind.uid, "STUDY"),
        StudyRoot(UID.StudyRootQueryRetrieveInformationModelFind.uid, "STUDY"),
        PatientStudyOnly(UID.PatientStudyOnlyQueryRetrieveInformationModelFind.uid, "STUDY"),
        MWL(UID.ModalityWorklistInformationModelFind.uid, null), UPSPull(UID.UnifiedProcedureStepPull.uid, null),
        UPSWatch(UID.UnifiedProcedureStepWatch.uid, null), UPSQuery(UID.UnifiedProcedureStepQuery.uid, null),
        HangingProtocol(UID.HangingProtocolInformationModelFind.uid, null),
        ColorPalette(UID.ColorPaletteQueryRetrieveInformationModelFind.uid, null);

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

        public String getCuid() {
            return cuid;
        }
    }

    private static class MergeNested implements Visitor {
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
