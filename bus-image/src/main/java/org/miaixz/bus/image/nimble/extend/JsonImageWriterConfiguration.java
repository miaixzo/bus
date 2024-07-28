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
package org.miaixz.bus.image.nimble.extend;

import jakarta.json.stream.JsonParser;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.json.ConfigurationDelegate;
import org.miaixz.bus.image.metric.json.JSONReader;
import org.miaixz.bus.image.metric.json.JSONWriter;
import org.miaixz.bus.image.metric.json.JsonConfigurationExtension;
import org.miaixz.bus.image.nimble.codec.ImageWriterFactory;

import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class JsonImageWriterConfiguration extends JsonConfigurationExtension {

    @Override
    protected void storeTo(Device device, JSONWriter writer) {
        ImageWriterExtension ext = device.getDeviceExtension(ImageWriterExtension.class);
        if (ext == null)
            return;

        writer.writeStartArray("dcmImageWriter");
        for (Map.Entry<String, ImageWriterFactory.ImageWriterParam> entry : ext.getImageWriterFactory().getEntries()) {
            writer.writeStartObject();
            String tsuid = entry.getKey();
            ImageWriterFactory.ImageWriterParam param = entry.getValue();
            writer.writeNotNullOrDef("dicomTransferSyntax", tsuid, null);
            writer.writeNotNullOrDef("dcmIIOFormatName", param.formatName, null);
            writer.writeNotNullOrDef("dcmJavaClassName", param.className, null);
            writer.writeNotNullOrDef("dcmPatchJPEGLS", param.patchJPEGLS, null);
            writer.writeNotEmpty("dcmImageWriteParam", param.imageWriteParams);

            writer.writeEnd();
        }

        writer.writeEnd();
    }

    @Override
    public boolean loadDeviceExtension(Device device, JSONReader reader, ConfigurationDelegate config) {
        if (!reader.getString().equals("dcmImageWriter"))
            return false;

        ImageWriterFactory factory = new ImageWriterFactory();
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            String tsuid = null;
            String formatName = null;
            String className = null;
            String patchJPEGLS = null;
            String[] imageWriteParam = {};
            while (reader.next() == JsonParser.Event.KEY_NAME) {
                switch (reader.getString()) {
                case "dicomTransferSyntax":
                    tsuid = reader.stringValue();
                    break;
                case "dcmIIOFormatName":
                    formatName = reader.stringValue();
                    break;
                case "dcmJavaClassName":
                    className = reader.stringValue();
                    break;
                case "dcmPatchJPEGLS":
                    patchJPEGLS = reader.stringValue();
                    break;
                case "dcmImageWriteParam":
                    imageWriteParam = reader.stringArray();
                    break;
                default:
                    reader.skipUnknownProperty();
                }
            }
            reader.expect(JsonParser.Event.END_OBJECT);
            factory.put(tsuid,
                    new ImageWriterFactory.ImageWriterParam(formatName, className, patchJPEGLS, imageWriteParam));
        }
        device.addDeviceExtension(new ImageWriterExtension(factory));
        return true;
    }

}
