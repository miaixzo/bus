/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.plugin;

import jakarta.json.Json;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.BasicBulkDataDescriptor;
import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.metric.json.JSONReader;

import java.io.*;
import java.util.List;

/**
 * The {@code Json2Dcm} class provides functionality to convert a DICOM JSON model into a DICOM Part 10 file. It reads a
 * JSON file, reconstructs the DICOM dataset, and writes it to a DICOM file format, handling bulk data and file meta
 * information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Json2Dcm {

    /**
     * Descriptor for handling bulk data.
     */
    private final BasicBulkDataDescriptor bulkDataDescriptor = new BasicBulkDataDescriptor();
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
     * Parses a JSON file and populates an {@link Attributes} object.
     *
     * @param fname The name of the JSON file, or "-" for standard input.
     * @param attrs The {@link Attributes} object to populate.
     * @return The {@link JSONReader} used for parsing.
     * @throws IOException if an I/O error occurs.
     */
    private static JSONReader parseJSON(String fname, Attributes attrs) throws IOException {
        try (InputStream in = fname.equals("-") ? System.in : new FileInputStream(fname)) {
            JSONReader reader = new JSONReader(Json.createParser(new InputStreamReader(in, Charset.UTF_8)));
            reader.readDataset(attrs);
            return reader;
        }
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
     * Reads a DICOM JSON file and merges its content into the current dataset.
     *
     * @param fname The name of the JSON file.
     * @throws Exception if an error occurs during parsing or merging.
     */
    public void mergeJSON(String fname) throws Exception {
        if (dataset == null) {
            dataset = new Attributes();
        }
        JSONReader reader = parseJSON(fname, dataset);
        Attributes fmi2 = reader.getFileMetaInformation();
        if (fmi2 != null) {
            if (fmi != null) {
                fmi.addAll(fmi2);
            } else {
                fmi = fmi2;
            }
        }
    }

}
