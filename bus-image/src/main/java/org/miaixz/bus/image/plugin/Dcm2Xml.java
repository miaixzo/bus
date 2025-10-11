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

import org.miaixz.bus.image.galaxy.io.BasicBulkDataDescriptor;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.SAXWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The {@code Dcm2Xml} class provides functionality to convert a DICOM file into its XML representation according to the
 * DICOM Part 19 standard. It uses a {@link SAXWriter} to handle the conversion logic.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dcm2Xml {

    /**
     * Constant for XML version 1.0.
     */
    private static final String XML_1_0 = "1.0";
    /**
     * Constant for XML version 1.1.
     */
    private static final String XML_1_1 = "1.1";
    /**
     * Describes how to handle bulk data.
     */
    private final BasicBulkDataDescriptor bulkDataDescriptor = new BasicBulkDataDescriptor();
    /**
     * URL of the XSLT stylesheet to apply.
     */
    private String xsltURL;
    /**
     * Whether to format the XML output with indentation.
     */
    private boolean indent = false;
    /**
     * Whether to include the DICOM keyword in the XML elements.
     */
    private boolean includeKeyword = true;
    /**
     * Whether to include the XML namespace declaration.
     */
    private boolean includeNamespaceDeclaration = false;
    /**
     * Specifies how to include bulk data in the output.
     */
    private ImageInputStream.IncludeBulkData includeBulkData = ImageInputStream.IncludeBulkData.URI;
    /**
     * Whether to concatenate bulk data into a single file.
     */
    private boolean catBlkFiles = false;
    /**
     * The prefix for bulk data file names.
     */
    private String blkFilePrefix = "blk";
    /**
     * The suffix for bulk data file names.
     */
    private String blkFileSuffix;
    /**
     * The directory to store bulk data files.
     */
    private File blkDirectory;
    /**
     * The XML version to be used in the output.
     */
    private String xmlVersion = XML_1_0;

    /**
     * Converts a file path or URL string into a URL string.
     *
     * @param fileOrURL The file path or URL string.
     * @return A valid URL string.
     */
    private static String toURL(String fileOrURL) {
        try {
            new URL(fileOrURL);
            return fileOrURL;
        } catch (MalformedURLException e) {
            return new File(fileOrURL).toURI().toString();
        }
    }

    /**
     * Sets the URL of an XSLT stylesheet to be applied to the generated XML.
     *
     * @param xsltURL The URL of the XSLT stylesheet.
     */
    public final void setXSLTURL(String xsltURL) {
        this.xsltURL = xsltURL;
    }

    /**
     * Sets whether to indent the XML output for pretty-printing.
     *
     * @param indent {@code true} to enable indentation, {@code false} otherwise.
     */
    public final void setIndent(boolean indent) {
        this.indent = indent;
    }

    /**
     * Sets whether to include the DICOM keyword as an attribute in the XML elements.
     *
     * @param includeKeyword {@code true} to include keywords, {@code false} otherwise.
     */
    public final void setIncludeKeyword(boolean includeKeyword) {
        this.includeKeyword = includeKeyword;
    }

    /**
     * Sets whether to include the XML namespace declaration in the root element.
     *
     * @param includeNamespaceDeclaration {@code true} to include the namespace, {@code false} otherwise.
     */
    public final void setIncludeNamespaceDeclaration(boolean includeNamespaceDeclaration) {
        this.includeNamespaceDeclaration = includeNamespaceDeclaration;
    }

    /**
     * Sets the mode for including bulk data in the XML output.
     *
     * @param includeBulkData The bulk data inclusion mode (e.g., URI, inline).
     * @see org.miaixz.bus.image.galaxy.io.ImageInputStream.IncludeBulkData
     */
    public final void setIncludeBulkData(ImageInputStream.IncludeBulkData includeBulkData) {
        this.includeBulkData = includeBulkData;
    }

    /**
     * Sets whether to concatenate all bulk data into a single file.
     *
     * @param catBlkFiles {@code true} to concatenate, {@code false} to create separate files.
     */
    public final void setConcatenateBulkDataFiles(boolean catBlkFiles) {
        this.catBlkFiles = catBlkFiles;
    }

    /**
     * Sets the prefix for generated bulk data file names.
     *
     * @param blkFilePrefix The file prefix.
     */
    public final void setBulkDataFilePrefix(String blkFilePrefix) {
        this.blkFilePrefix = blkFilePrefix;
    }

    /**
     * Sets the suffix for generated bulk data file names.
     *
     * @param blkFileSuffix The file suffix.
     */
    public final void setBulkDataFileSuffix(String blkFileSuffix) {
        this.blkFileSuffix = blkFileSuffix;
    }

    /**
     * Sets the directory where bulk data files will be stored.
     *
     * @param blkDirectory The directory for bulk data files.
     */
    public final void setBulkDataDirectory(File blkDirectory) {
        this.blkDirectory = blkDirectory;
    }

    /**
     * Sets whether to exclude default VRs from the bulk data descriptor.
     *
     * @param excludeDefaults {@code true} to exclude defaults, {@code false} otherwise.
     */
    public void setBulkDataNoDefaults(boolean excludeDefaults) {
        bulkDataDescriptor.excludeDefaults(excludeDefaults);
    }

    /**
     * Sets the length thresholds for treating data as bulk data from an array of strings.
     *
     * @param thresholds An array of strings representing the thresholds.
     */
    public void setBulkDataLengthsThresholdsFromStrings(String[] thresholds) {
        bulkDataDescriptor.setLengthsThresholdsFromStrings(thresholds);
    }

    /**
     * Sets the XML version for the output document.
     *
     * @param xmlVersion The XML version string (e.g., "1.0" or "1.1").
     */
    public final void setXMLVersion(String xmlVersion) {
        this.xmlVersion = xmlVersion;
    }

    /**
     * Parses a DICOM input stream and writes the corresponding XML representation to standard output.
     *
     * @param dis The DICOM input stream to parse.
     * @throws IOException                       if an I/O error occurs during reading.
     * @throws TransformerConfigurationException if there is a problem with the transformer configuration.
     */
    public void parse(ImageInputStream dis) throws IOException, TransformerConfigurationException {
        dis.setIncludeBulkData(includeBulkData);
        dis.setBulkDataDescriptor(bulkDataDescriptor);
        dis.setBulkDataDirectory(blkDirectory);
        dis.setBulkDataFilePrefix(blkFilePrefix);
        dis.setBulkDataFileSuffix(blkFileSuffix);
        dis.setConcatenateBulkDataFiles(catBlkFiles);
        TransformerHandler th = getTransformerHandler();
        Transformer t = th.getTransformer();
        t.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
        if (indent) {
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        }
        t.setOutputProperty(OutputKeys.VERSION, xmlVersion);
        th.setResult(new StreamResult(System.out));
        SAXWriter saxWriter = new SAXWriter(th);
        saxWriter.setIncludeKeyword(includeKeyword);
        saxWriter.setIncludeNamespaceDeclaration(includeNamespaceDeclaration);
        dis.setDicomInputHandler(saxWriter);
        dis.readDataset();
    }

    /**
     * Creates and configures a {@link TransformerHandler} for SAX-based XML processing. If an XSLT URL is provided, the
     * handler is configured to apply the transformation.
     *
     * @return A configured {@code TransformerHandler}.
     * @throws TransformerConfigurationException if a suitable {@code TransformerFactory} cannot be created or if the
     *                                           handler cannot be created.
     */
    private TransformerHandler getTransformerHandler() throws TransformerConfigurationException {
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        if (xsltURL == null)
            return tf.newTransformerHandler();

        return tf.newTransformerHandler(new StreamSource(xsltURL));
    }

}
