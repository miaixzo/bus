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
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dcm2Json {

    private final BasicBulkDataDescriptor bulkDataDescriptor = new BasicBulkDataDescriptor();
    private boolean indent = false;
    private ImageInputStream.IncludeBulkData includeBulkData = ImageInputStream.IncludeBulkData.URI;
    private boolean catBlkFiles = false;
    private String blkFilePrefix = "blk";
    private String blkFileSuffix;
    private File blkDirectory;
    private boolean encodeAsNumber;

    public final void setIndent(boolean indent) {
        this.indent = indent;
    }

    public final void setEncodeAsNumber(boolean encodeAsNumber) {
        this.encodeAsNumber = encodeAsNumber;
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

    private JsonGenerator createGenerator(OutputStream out) {
        Map<String, ?> conf = new HashMap<>(2);
        if (indent)
            conf.put(JsonGenerator.PRETTY_PRINTING, null);
        return Json.createGeneratorFactory(conf).createGenerator(out);
    }

}
