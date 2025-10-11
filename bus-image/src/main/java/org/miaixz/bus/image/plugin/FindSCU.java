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

import javax.xml.XMLConstants;
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
 * The {@code FindSCU} class implements a Service Class User (SCU) for the DICOM C-FIND service. It supports various
 * query/retrieve information models, including Patient Root, Study Root, Modality Worklist, Unified Procedure Step, and
 * Hanging Protocol.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FindSCU implements AutoCloseable {

    /**
     * The DICOM device associated with this SCU.
     */
    private final Device device = new Device("findscu");
    /**
     * The Application Entity used by this SCU.
     */
    private final ApplicationEntity ae = new ApplicationEntity("FINDSCU");
    /**
     * The local network connection configuration.
     */
    private final Connection conn = new Connection();
    /**
     * The remote network connection configuration.
     */
    private final Connection remote = new Connection();
    /**
     * The A-ASSOCIATE-RQ message to be sent.
     */
    private final AAssociateRQ rq = new AAssociateRQ();
    /**
     * The query keys (matching attributes) for the C-FIND request.
     */
    private final Attributes keys = new Attributes();
    /**
     * A counter for the total number of matches received.
     */
    private final AtomicInteger totNumMatches = new AtomicInteger();
    /**
     * The overall status of the C-FIND operation.
     */
    private final Status state;
    /**
     * A factory for creating SAX transformers.
     */
    private SAXTransformerFactory saxtf;
    /**
     * The priority of the C-FIND request.
     */
    private int priority;
    /**
     * The number of matches after which to cancel the request.
     */
    private int cancelAfter;
    /**
     * The information model to be used for the query.
     */
    private InformationModel model;
    /**
     * The directory to store output files.
     */
    private File outDir;
    /**
     * A formatter for the output filenames.
     */
    private DecimalFormat outFileFormat;
    /**
     * A filter for attributes to be included from an input file.
     */
    private int[] inFilter;
    /**
     * A flag to concatenate all responses into a single output file.
     */
    private boolean catOut = false;
    /**
     * A flag to indicate if the output should be in XML format.
     */
    private boolean xml = false;
    /**
     * A flag to indent the XML output.
     */
    private boolean xmlIndent = false;
    /**
     * A flag to include DICOM keywords in the XML output.
     */
    private boolean xmlIncludeKeyword = true;
    /**
     * A flag to include the namespace declaration in the XML output.
     */
    private boolean xmlIncludeNamespaceDeclaration = false;
    /**
     * An XSLT file to be applied to the XML output.
     */
    private File xsltFile;
    /**
     * The compiled XSLT templates.
     */
    private Templates xsltTpls;
    /**
     * The output stream for writing results.
     */
    private OutputStream out;
    /**
     * The active DICOM association.
     */
    private Association as;

    /**
     * Constructs a new {@code FindSCU} instance, initializing the device and application entity.
     */
    public FindSCU() {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
        state = new Status(new ImageProgress());
    }

    /**
     * Merges query keys, with special handling for nested sequences.
     *
     * @param attrs The primary attributes.
     * @param keys  The keys to merge into the attributes.
     */
    static void mergeKeys(Attributes attrs, Attributes keys) {
        try {
            attrs.accept(new MergeNested(keys), false);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        attrs.addAll(keys);
    }

    /**
     * Sets the priority for the C-FIND operation.
     *
     * @param priority The priority value (0=Medium, 1=High, 2=Low).
     */
    public final void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Sets the information model and transfer syntaxes for the C-FIND operation.
     *
     * @param model        The information model to use.
     * @param tss          An array of transfer syntax UIDs to propose.
     * @param queryOptions A set of query options for extended negotiation.
     */
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

    /**
     * Adds the Query/Retrieve Level key to the query attributes.
     *
     * @param s The level string (e.g., "STUDY", "SERIES").
     */
    public void addLevel(String s) {
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, s);
    }

    /**
     * Sets the number of matches after which the C-FIND request should be automatically cancelled.
     *
     * @param cancelAfter The number of matches.
     */
    public final void setCancelAfter(int cancelAfter) {
        this.cancelAfter = cancelAfter;
    }

    /**
     * Sets the output directory for storing response files.
     *
     * @param outDir The output directory.
     */
    public final void setOutputDirectory(File outDir) {
        outDir.mkdirs();
        this.outDir = outDir;
    }

    /**
     * Sets the format for the output filenames.
     *
     * @param outFileFormat A {@link DecimalFormat} pattern.
     */
    public final void setOutputFileFormat(String outFileFormat) {
        this.outFileFormat = new DecimalFormat(outFileFormat);
    }

    /**
     * Sets an XSLT file to be applied to the XML output.
     *
     * @param xsltFile The XSLT file.
     */
    public final void setXSLT(File xsltFile) {
        this.xsltFile = xsltFile;
    }

    /**
     * Sets whether to output the results in XML format.
     *
     * @param xml {@code true} for XML output, {@code false} for DICOM.
     */
    public final void setXML(boolean xml) {
        this.xml = xml;
    }

    /**
     * Sets whether to indent the XML output.
     *
     * @param indent {@code true} to indent the XML.
     */
    public final void setXMLIndent(boolean indent) {
        this.xmlIndent = indent;
    }

    /**
     * Sets whether to include DICOM keywords in the XML output.
     *
     * @param includeKeyword {@code true} to include keywords.
     */
    public final void setXMLIncludeKeyword(boolean includeKeyword) {
        this.xmlIncludeKeyword = includeKeyword;
    }

    /**
     * Sets whether to include the namespace declaration in the XML output.
     *
     * @param includeNamespaceDeclaration {@code true} to include the namespace.
     */
    public final void setXMLIncludeNamespaceDeclaration(boolean includeNamespaceDeclaration) {
        this.xmlIncludeNamespaceDeclaration = includeNamespaceDeclaration;
    }

    /**
     * Sets whether to concatenate all responses into a single output file.
     *
     * @param catOut {@code true} to concatenate the output.
     */
    public final void setConcatenateOutputFiles(boolean catOut) {
        this.catOut = catOut;
    }

    /**
     * Sets a filter for attributes to be read from an input file.
     *
     * @param inFilter An array of attribute tags to include.
     */
    public final void setInputFilter(int[] inFilter) {
        this.inFilter = inFilter;
    }

    /**
     * Gets the Application Entity of this SCU.
     *
     * @return The Application Entity.
     */
    public ApplicationEntity getApplicationEntity() {
        return ae;
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
     * Gets the A-ASSOCIATE-RQ message.
     *
     * @return The A-ASSOCIATE-RQ.
     */
    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    /**
     * Gets the current DICOM association.
     *
     * @return The association, or {@code null} if not connected.
     */
    public Association getAssociation() {
        return as;
    }

    /**
     * Gets the device associated with this SCU.
     *
     * @return The device.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Gets the query key attributes.
     *
     * @return The query keys.
     */
    public Attributes getKeys() {
        return keys;
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
        as = ae.connect(conn, remote, rq);
    }

    /**
     * Closes the DICOM association and any open output streams.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    @Override
    public void close() throws IOException, InterruptedException {
        if (as != null && as.isReadyForDataTransfer()) {
            as.waitForOutstandingRSP();
            as.release();
        }
        IoKit.close(out);
        out = null;
    }

    /**
     * Performs a C-FIND query using attributes from a file.
     *
     * @param f The file containing query attributes (in DICOM or XML format).
     * @throws Exception if an error occurs during file parsing or query.
     */
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

    /**
     * Performs a C-FIND query using the currently configured keys.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void query() throws IOException, InterruptedException {
        query(keys);
    }

    /**
     * Performs a C-FIND query with the given keys and a default response handler.
     *
     * @param keys The query keys.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    private void query(Attributes keys) throws IOException, InterruptedException {
        DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

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

    /**
     * Performs a C-FIND query with the configured keys and a custom response handler.
     *
     * @param rspHandler The custom response handler.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void query(DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        query(keys, rspHandler);
    }

    /**
     * Performs the C-FIND operation.
     *
     * @param keys       The query keys.
     * @param rspHandler The response handler.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    private void query(Attributes keys, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        as.cfind(model.cuid, priority, keys, null, rspHandler);
    }

    /**
     * Handles a pending C-FIND response (a match).
     *
     * @param data The attributes of the matching entity.
     */
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

    /**
     * Generates a filename for a given match number.
     *
     * @param i The match number.
     * @return The formatted filename.
     */
    private String fname(int i) {
        synchronized (outFileFormat) {
            return outFileFormat.format(i);
        }
    }

    /**
     * Writes the given attributes to an output stream in XML format.
     *
     * @param attrs The attributes to write.
     * @param out   The output stream.
     * @throws Exception if an error occurs during XML transformation.
     */
    private void writeAsXML(Attributes attrs, OutputStream out) throws Exception {
        TransformerHandler th = getTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT, xmlIndent ? "yes" : "no");
        th.setResult(new StreamResult(out));
        SAXWriter saxWriter = new SAXWriter(th);
        saxWriter.setIncludeKeyword(xmlIncludeKeyword);
        saxWriter.setIncludeNamespaceDeclaration(xmlIncludeNamespaceDeclaration);
        saxWriter.write(attrs);
    }

    /**
     * Gets a configured {@link TransformerHandler} for XML processing, optionally with an XSLT.
     *
     * @return The configured transformer handler.
     * @throws Exception if an error occurs during handler creation.
     */
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

    /**
     * Gets the local connection configuration.
     *
     * @return The connection.
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * Gets the current status of the C-FIND operation.
     *
     * @return The status.
     */
    public Status getState() {
        return state;
    }

    /**
     * Enumeration of the supported DICOM Information Models for C-FIND.
     */
    public enum InformationModel {

        /**
         * Patient Root Query/Retrieve Information Model - FIND.
         */
        PatientRoot(UID.PatientRootQueryRetrieveInformationModelFind.uid, "STUDY"),
        /**
         * Study Root Query/Retrieve Information Model - FIND.
         */
        StudyRoot(UID.StudyRootQueryRetrieveInformationModelFind.uid, "STUDY"),
        /**
         * Patient/Study Only Query/Retrieve Information Model - FIND.
         */
        PatientStudyOnly(UID.PatientStudyOnlyQueryRetrieveInformationModelFind.uid, "STUDY"),
        /**
         * Modality Worklist Information Model - FIND.
         */
        MWL(UID.ModalityWorklistInformationModelFind.uid, null),
        /**
         * Unified Procedure Step - Pull SOP Class.
         */
        UPSPull(UID.UnifiedProcedureStepPull.uid, null),
        /**
         * Unified Procedure Step - Watch SOP Class.
         */
        UPSWatch(UID.UnifiedProcedureStepWatch.uid, null),
        /**
         * Unified Procedure Step - Query SOP Class.
         */
        UPSQuery(UID.UnifiedProcedureStepQuery.uid, null),
        /**
         * Hanging Protocol Information Model - FIND.
         */
        HangingProtocol(UID.HangingProtocolInformationModelFind.uid, null),
        /**
         * Color Palette Query/Retrieve Information Model - FIND.
         */
        ColorPalette(UID.ColorPaletteQueryRetrieveInformationModelFind.uid, null);

        /**
         * The SOP Class UID for the information model.
         */
        final String cuid;
        /**
         * The default query/retrieve level for the model.
         */
        final String level;

        /**
         * Constructs a new InformationModel.
         *
         * @param cuid  The SOP Class UID.
         * @param level The default query/retrieve level.
         */
        InformationModel(String cuid, String level) {
            this.cuid = cuid;
            this.level = level;
        }

        /**
         * Adjusts the query options for this information model.
         *
         * @param queryOptions The set of query options to adjust.
         */
        public void adjustQueryOptions(EnumSet<QueryOption> queryOptions) {
            if (level == null) {
                queryOptions.add(QueryOption.RELATIONAL);
                queryOptions.add(QueryOption.DATETIME);
            }
        }

        /**
         * Gets the SOP Class UID of the information model.
         *
         * @return The SOP Class UID.
         */
        public String getCuid() {
            return cuid;
        }
    }

    /**
     * A visitor to merge attributes in nested sequences.
     */
    private static class MergeNested implements Visitor {

        /**
         * The keys to merge.
         */
        private final Attributes keys;

        /**
         * Constructs a new MergeNested visitor.
         *
         * @param keys The attributes to merge from.
         */
        MergeNested(Attributes keys) {
            this.keys = keys;
        }

        /**
         * Checks if a value is a non-empty sequence.
         *
         * @param val The value to check.
         * @return {@code true} if it is a non-empty sequence.
         */
        private static boolean isNotEmptySequence(Object val) {
            return val instanceof Sequence && !((Sequence) val).isEmpty();
        }

        /**
         * Visits an attribute and merges sequence items if applicable.
         *
         * @param attrs The attributes being visited.
         * @param tag   The tag of the current attribute.
         * @param vr    The VR of the current attribute.
         * @param val   The value of the current attribute.
         * @return {@code true} to continue visiting.
         */
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
