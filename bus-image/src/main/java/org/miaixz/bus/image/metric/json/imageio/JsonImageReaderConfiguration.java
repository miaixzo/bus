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
package org.miaixz.bus.image.metric.json.imageio;

import java.util.Map;

import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.json.ConfigurationDelegate;
import org.miaixz.bus.image.metric.json.JSONReader;
import org.miaixz.bus.image.metric.json.JSONWriter;
import org.miaixz.bus.image.metric.json.JsonConfigurationExtension;
import org.miaixz.bus.image.nimble.codec.ImageReaderFactory;
import org.miaixz.bus.image.nimble.extend.ImageReaderExtension;

import jakarta.json.stream.JsonParser;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class JsonImageReaderConfiguration extends JsonConfigurationExtension {

    @Override
    protected void storeTo(Device device, JSONWriter writer) {
        ImageReaderExtension ext = device.getDeviceExtension(ImageReaderExtension.class);
        if (ext == null)
            return;

        writer.writeStartArray("dcmImageReader");
        for (Map.Entry<String, ImageReaderFactory.ImageReaderParam> entry : ext.getImageReaderFactory().getEntries()) {
            writer.writeStartObject();
            String tsuid = entry.getKey();
            ImageReaderFactory.ImageReaderParam param = entry.getValue();
            writer.writeNotNullOrDef("dicomTransferSyntax", tsuid, null);
            writer.writeNotNullOrDef("dcmIIOFormatName", param.formatName, null);
            writer.writeNotNullOrDef("dcmJavaClassName", param.className, null);
            writer.writeNotNullOrDef("dcmPatchJPEGLS", param.patchJPEGLS, null);
            writer.writeNotEmpty("dcmImageReadParam", param.imageReadParams);
            writer.writeEnd();
        }

        writer.writeEnd();
    }

    @Override
    public boolean loadDeviceExtension(Device device, JSONReader reader, ConfigurationDelegate config) {
        if (!reader.getString().equals("dcmImageReader"))
            return false;

        ImageReaderFactory factory = new ImageReaderFactory();
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            String tsuid = null;
            String formatName = null;
            String className = null;
            String patchJPEGLS = null;
            String[] imageReadParam = {};
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

                    case "dcmImageReadParam":
                        imageReadParam = reader.stringArray();
                        break;

                    default:
                        reader.skipUnknownProperty();
                }
            }
            reader.expect(JsonParser.Event.END_OBJECT);
            factory.put(
                    tsuid,
                    new ImageReaderFactory.ImageReaderParam(formatName, className, patchJPEGLS, imageReadParam));
        }
        device.addDeviceExtension(new ImageReaderExtension(factory));
        return true;
    }

}
