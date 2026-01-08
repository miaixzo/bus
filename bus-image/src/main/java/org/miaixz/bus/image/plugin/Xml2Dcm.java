/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * The {@code Xml2Dcm} class provides functionality to convert a DICOM XML representation (according to DICOM Part 19)
 * into a DICOM Part 10 file. It uses a SAX parser to read the XML and reconstructs the DICOM dataset, which can then be
 * written to an output stream.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Xml2Dcm {

    /**
     * Descriptor for handling bulk data.
     */
    private final BasicBulkDataDescriptor bulkDataDescriptor = new BasicBulkDataDescriptor();
    /**
     * Whether to allow non-compliant XML structure.
     */
    private boolean lenient = false;
    /**
     * How to handle bulk data (e.g., as URI).
     */
    private ImageInputStream.IncludeBulkData includeBulkData = ImageInputStream.IncludeBulkData.URI;
    /**
     * Whether to concatenate bulk data files.
     */
    private boolean catBlkFiles = false;
    /**
     * Prefix for bulk data file names.
     */
    private String blkFilePrefix = "blk";
    /**
     * Suffix for bulk data file names.
     */
    private String blkFileSuffix;
    /**
     * Directory for storing bulk data files.
     */
    private File blkDirectory;
    /**
     * The Transfer Syntax UID for the output DICOM file.
     */
    private String tsuid;
    /**
     * Whether to include File Meta Information if it's not present.
     */
    private boolean withfmi;
    /**
     * Whether to exclude File Meta Information from the output.
     */
    private boolean nofmi;
    /**
     * Encoding options for the output DICOM file.
     */
    private ImageEncodingOptions encOpts = ImageEncodingOptions.DEFAULT;
    /**
     * A list of generated bulk data files.
     */
    private List<File> bulkDataFiles;
    /**
     * The File Meta Information of the dataset.
     */
    private Attributes fmi;
    /**
     * The main DICOM dataset.
     */
    private Attributes dataset;

    /**
     * Parses a DICOM XML file and returns its content as an {@link Attributes} object.
     *
     * @param fname The name of the XML file, or "-" for standard input.
     * @return An {@code Attributes} object representing the dataset.
     * @throws Exception if a parsing error occurs.
     */
    public static Attributes parseXML(String fname) throws Exception {
        Attributes attrs = new Attributes();
        ContentHandlerAdapter ch = new ContentHandlerAdapter(attrs);
        parseXML(fname, ch);
        return attrs;
    }

    /**
     * Parses a DICOM XML file using a provided SAX content handler.
     *
     * @param fname The name of the XML file, or "-" for standard input.
     * @param ch    The {@link ContentHandlerAdapter} to handle SAX events.
     * @throws Exception if a parsing error occurs.
     */
    private static void parseXML(String fname, ContentHandlerAdapter ch) throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        if (fname.equals("-")) {
            p.parse(System.in, ch);
        } else {
            p.parse(new File(fname), ch);
        }
    }

    /**
     * Sets whether the XML parser should be lenient about the XML structure.
     *
     * @param lenient {@code true} for lenient parsing, {@code false} for strict.
     */
    public void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    /**
     * Sets the mode for including bulk data.
     *
     * @param includeBulkData The bulk data inclusion mode.
     */
    public final void setIncludeBulkData(ImageInputStream.IncludeBulkData includeBulkData) {
        this.includeBulkData = includeBulkData;
    }

    /**
     * Sets whether to concatenate bulk data into a single file.
     *
     * @param catBlkFiles {@code true} to concatenate.
     */
    public final void setConcatenateBulkDataFiles(boolean catBlkFiles) {
        this.catBlkFiles = catBlkFiles;
    }

    /**
     * Sets the prefix for bulk data file names.
     *
     * @param blkFilePrefix The file prefix.
     */
    public final void setBulkDataFilePrefix(String blkFilePrefix) {
        this.blkFilePrefix = blkFilePrefix;
    }

    /**
     * Sets the suffix for bulk data file names.
     *
     * @param blkFileSuffix The file suffix.
     */
    public final void setBulkDataFileSuffix(String blkFileSuffix) {
        this.blkFileSuffix = blkFileSuffix;
    }

    /**
     * Sets the directory for storing bulk data files.
     *
     * @param blkDirectory The bulk data directory.
     */
    public final void setBulkDataDirectory(File blkDirectory) {
        this.blkDirectory = blkDirectory;
    }

    /**
     * Sets whether to exclude default VRs from the bulk data descriptor.
     *
     * @param excludeDefaults {@code true} to exclude defaults.
     */
    public void setBulkDataNoDefaults(boolean excludeDefaults) {
        bulkDataDescriptor.excludeDefaults(excludeDefaults);
    }

    /**
     * Sets the length thresholds for treating data as bulk data.
     *
     * @param thresholds An array of threshold strings.
     */
    public void setBulkDataLengthsThresholdsFromStrings(String[] thresholds) {
        bulkDataDescriptor.setLengthsThresholdsFromStrings(thresholds);
    }

    /**
     * Sets the Transfer Syntax UID for the output DICOM file.
     *
     * @param uid The Transfer Syntax UID.
     */
    public final void setTransferSyntax(String uid) {
        this.tsuid = uid;
    }

    /**
     * Sets whether to create and include File Meta Information if it is not present.
     *
     * @param withfmi {@code true} to create FMI if absent.
     */
    public final void setWithFileMetaInformation(boolean withfmi) {
        this.withfmi = withfmi;
    }

    /**
     * Sets whether to exclude the File Meta Information from the output file.
     *
     * @param nofmi {@code true} to exclude FMI.
     */
    public final void setNoFileMetaInformation(boolean nofmi) {
        this.nofmi = nofmi;
    }

    /**
     * Sets the encoding options for writing the DICOM file.
     *
     * @param encOpts The encoding options.
     */
    public final void setEncodingOptions(ImageEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    /**
     * Writes the reconstructed DICOM object to an output stream.
     *
     * @param out The output stream to write to.
     * @throws IOException if an I/O error occurs.
     */
    public void writeTo(OutputStream out) throws IOException {
        if (nofmi) {
            fmi = null;
        } else if (fmi == null ? withfmi : tsuid != null && !tsuid.equals(fmi.getString(Tag.TransferSyntaxUID, null))) {
            fmi = dataset.createFileMetaInformation(tsuid);
        }
        String outputTsuid = fmi != null ? UID.ExplicitVRLittleEndian.uid
                : (tsuid != null ? tsuid : UID.ImplicitVRLittleEndian.uid);
        try (ImageOutputStream dos = new ImageOutputStream(new BufferedOutputStream(out), outputTsuid)) {
            dos.setEncodingOptions(encOpts);
            dos.writeDataset(fmi, dataset);
            dos.finish();
            dos.flush();
        }
    }

    /**
     * Deletes any temporary bulk data files that were created during the parsing process.
     */
    public void delBulkDataFiles() {
        if (bulkDataFiles != null) {
            for (File f : bulkDataFiles) {
                if (!f.delete()) {
                    // Log or handle the failure to delete the file
                }
            }
        }
    }

    /**
     * Parses a DICOM input stream to provide a base dataset.
     *
     * @param dis The DICOM input stream.
     * @throws IOException if an I/O error occurs.
     */
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

    /**
     * Reads a DICOM XML file and merges its content into the current dataset.
     *
     * @param fname The name of the XML file.
     * @throws Exception if an error occurs during parsing or merging.
     */
    public void mergeXML(String fname) throws Exception {
        ContentHandlerAdapter ch = new ContentHandlerAdapter(dataset, lenient);
        parseXML(fname, ch);
        dataset = ch.getDataset();
        Attributes fmi2 = ch.getFileMetaInformation();
        if (fmi2 != null)
            fmi = fmi2;
    }

}
