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
 * DCM-XML转换
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dcm2Xml {

    private static final String XML_1_0 = "1.0";
    private static final String XML_1_1 = "1.1";
    private final BasicBulkDataDescriptor bulkDataDescriptor = new BasicBulkDataDescriptor();
    private String xsltURL;
    private boolean indent = false;
    private boolean includeKeyword = true;
    private boolean includeNamespaceDeclaration = false;
    private ImageInputStream.IncludeBulkData includeBulkData = ImageInputStream.IncludeBulkData.URI;
    private boolean catBlkFiles = false;
    private String blkFilePrefix = "blk";
    private String blkFileSuffix;
    private File blkDirectory;
    private String xmlVersion = XML_1_0;

    private static String toURL(String fileOrURL) {
        try {
            new URL(fileOrURL);
            return fileOrURL;
        } catch (MalformedURLException e) {
            return new File(fileOrURL).toURI().toString();
        }
    }

    public final void setXSLTURL(String xsltURL) {
        this.xsltURL = xsltURL;
    }

    public final void setIndent(boolean indent) {
        this.indent = indent;
    }

    public final void setIncludeKeyword(boolean includeKeyword) {
        this.includeKeyword = includeKeyword;
    }

    public final void setIncludeNamespaceDeclaration(boolean includeNamespaceDeclaration) {
        this.includeNamespaceDeclaration = includeNamespaceDeclaration;
    }

    public final void setIncludeBulkData(ImageInputStream.IncludeBulkData includeBulkData) {
        this.includeBulkData = includeBulkData;
    }

    public final void setConcatenateBulkDataFiles(boolean catBlkFiles) {
        this.catBlkFiles = catBlkFiles;
    }

    public final void setBulkDataFilePrefix(String blkFilePrefix) {
        this.blkFilePrefix = blkFilePrefix;
    }

    public final void setBulkDataFileSuffix(String blkFileSuffix) {
        this.blkFileSuffix = blkFileSuffix;
    }

    public final void setBulkDataDirectory(File blkDirectory) {
        this.blkDirectory = blkDirectory;
    }

    public void setBulkDataNoDefaults(boolean excludeDefaults) {
        bulkDataDescriptor.excludeDefaults(excludeDefaults);
    }

    public void setBulkDataLengthsThresholdsFromStrings(String[] thresholds) {
        bulkDataDescriptor.setLengthsThresholdsFromStrings(thresholds);
    }

    public final void setXMLVersion(String xmlVersion) {
        this.xmlVersion = xmlVersion;
    }

    public void parse(ImageInputStream dis) throws IOException,
            TransformerConfigurationException {
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

    private TransformerHandler getTransformerHandler()
            throws TransformerConfigurationException {
        SAXTransformerFactory tf = (SAXTransformerFactory)
                TransformerFactory.newInstance();
        if (xsltURL == null)
            return tf.newTransformerHandler();

        TransformerHandler th = tf.newTransformerHandler(
                new StreamSource(xsltURL));
        return th;
    }

}
