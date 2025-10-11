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

import jakarta.json.Json;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonGenerator;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.BasicBulkDataDescriptor;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.metric.json.JSONWriter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code Dcm2Json} class provides functionality to convert a DICOM file into its JSON representation according to
 * the DICOM Part 18.F standard. It uses a {@link JSONWriter} to handle the conversion logic.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dcm2Json {

    /**
     * Describes how to handle bulk data.
     */
    private final BasicBulkDataDescriptor bulkDataDescriptor = new BasicBulkDataDescriptor();
    /**
     * Whether to format the JSON output with indentation.
     */
    private boolean indent = false;
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
     * Whether to encode numeric VRs (DS, IS, SV, UV) as JSON numbers instead of strings.
     */
    private boolean encodeAsNumber;

    /**
     * Sets whether to indent the JSON output for pretty-printing.
     *
     * @param indent {@code true} to enable indentation, {@code false} otherwise.
     */
    public final void setIndent(boolean indent) {
        this.indent = indent;
    }

    /**
     * Sets whether to encode DICOM value representations (VRs) of numeric type (DS, IS, SV, UV) as JSON numbers. The
     * default is to encode them as strings.
     *
     * @param encodeAsNumber {@code true} to encode as numbers, {@code false} to encode as strings.
     */
    public final void setEncodeAsNumber(boolean encodeAsNumber) {
        this.encodeAsNumber = encodeAsNumber;
    }

    /**
     * Sets the mode for including bulk data in the JSON output.
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
     * Parses a DICOM input stream and writes the corresponding JSON representation to standard output.
     *
     * @param dis The DICOM input stream to parse.
     * @throws IOException if an I/O error occurs during reading or writing.
     */
    public void parse(ImageInputStream dis) throws IOException {
        dis.setIncludeBulkData(includeBulkData);
        dis.setBulkDataDescriptor(bulkDataDescriptor);
        dis.setBulkDataDirectory(blkDirectory);
        dis.setBulkDataFilePrefix(blkFilePrefix);
        dis.setBulkDataFileSuffix(blkFileSuffix);
        dis.setConcatenateBulkDataFiles(catBlkFiles);
        JsonGenerator jsonGen = createGenerator(System.out);
        JSONWriter jsonWriter = new JSONWriter(jsonGen);
        if (encodeAsNumber) {
            jsonWriter.setJsonType(VR.DS, JsonValue.ValueType.NUMBER);
            jsonWriter.setJsonType(VR.IS, JsonValue.ValueType.NUMBER);
            jsonWriter.setJsonType(VR.SV, JsonValue.ValueType.NUMBER);
            jsonWriter.setJsonType(VR.UV, JsonValue.ValueType.NUMBER);
        }
        dis.setDicomInputHandler(jsonWriter);
        dis.readDataset();
        jsonGen.flush();
    }

    /**
     * Creates a {@link JsonGenerator} for writing to the specified output stream.
     *
     * @param out The output stream to write JSON to.
     * @return A configured {@code JsonGenerator}.
     */
    private JsonGenerator createGenerator(OutputStream out) {
        Map<String, ?> conf = new HashMap<>(2);
        if (indent)
            conf.put(JsonGenerator.PRETTY_PRINTING, null);
        return Json.createGeneratorFactory(conf).createGenerator(out);
    }

}
