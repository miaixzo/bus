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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Xml2Dcm {

    private final BasicBulkDataDescriptor bulkDataDescriptor = new BasicBulkDataDescriptor();
    private boolean lenient = false;
    private ImageInputStream.IncludeBulkData includeBulkData = ImageInputStream.IncludeBulkData.URI;
    private boolean catBlkFiles = false;
    private String blkFilePrefix = "blk";
    private String blkFileSuffix;
    private File blkDirectory;
    private String tsuid;
    private boolean withfmi;
    private boolean nofmi;
    private ImageEncodingOptions encOpts = ImageEncodingOptions.DEFAULT;
    private List<File> bulkDataFiles;
    private Attributes fmi;
    private Attributes dataset;

    public static Attributes parseXML(String fname) throws Exception {
        Attributes attrs = new Attributes();
        ContentHandlerAdapter ch = new ContentHandlerAdapter(attrs);
        parseXML(fname, ch);
        return attrs;
    }

    private static void parseXML(String fname, ContentHandlerAdapter ch) throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        if (fname.equals("-")) {
            p.parse(System.in, ch);
        } else {
            p.parse(new File(fname), ch);
        }
    }

    public void setLenient(boolean lenient) {
        this.lenient = lenient;
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

    public final void setTransferSyntax(String uid) {
        this.tsuid = uid;
    }

    public final void setWithFileMetaInformation(boolean withfmi) {
        this.withfmi = withfmi;
    }

    public final void setNoFileMetaInformation(boolean nofmi) {
        this.nofmi = nofmi;
    }

    public final void setEncodingOptions(ImageEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    public void writeTo(OutputStream out) throws IOException {
        if (nofmi)
            fmi = null;
        else if (fmi == null ? withfmi : tsuid != null && !tsuid.equals(fmi.getString(Tag.TransferSyntaxUID, null))) {
            fmi = dataset.createFileMetaInformation(tsuid);
        }
        ImageOutputStream dos = new ImageOutputStream(new BufferedOutputStream(out),
                fmi != null ? UID.ExplicitVRLittleEndian.uid : tsuid != null ? tsuid : UID.ImplicitVRLittleEndian.uid);
        dos.setEncodingOptions(encOpts);
        dos.writeDataset(fmi, dataset);
        dos.finish();
        dos.flush();
    }

    public void delBulkDataFiles() {
        if (bulkDataFiles != null)
            for (File f : bulkDataFiles)
                f.delete();
    }

    public void parse(ImageInputStream dis) throws IOException {
        dis.setIncludeBulkData(includeBulkData);
        dis.setBulkDataDescriptor(bulkDataDescriptor);
        dis.setBulkDataDirectory(blkDirectory);
        dis.setBulkDataFilePrefix(blkFilePrefix);
        dis.setBulkDataFileSuffix(blkFileSuffix);
        dis.setConcatenateBulkDataFiles(catBlkFiles);
        dataset = dis.readDataset();
        fmi = dis.getFileMetaInformation();
        bulkDataFiles = dis.getBulkDataFiles();
    }

    public void mergeXML(String fname) throws Exception {
        ContentHandlerAdapter ch = new ContentHandlerAdapter(dataset, lenient);
        parseXML(fname, ch);
        dataset = ch.getDataset();
        Attributes fmi2 = ch.getFileMetaInformation();
        if (fmi2 != null)
            fmi = fmi2;
    }

}
