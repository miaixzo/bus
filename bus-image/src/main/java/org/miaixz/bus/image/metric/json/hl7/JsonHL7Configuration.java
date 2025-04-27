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
package org.miaixz.bus.image.metric.json.hl7;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.hl7.net.HL7Application;
import org.miaixz.bus.image.metric.hl7.net.HL7DeviceExtension;
import org.miaixz.bus.image.metric.json.ConfigurationDelegate;
import org.miaixz.bus.image.metric.json.JSONReader;
import org.miaixz.bus.image.metric.json.JSONWriter;
import org.miaixz.bus.image.metric.json.JsonConfigurationExtension;

import jakarta.json.stream.JsonParser;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class JsonHL7Configuration extends JsonConfigurationExtension {

    private final List<JsonHL7ConfigurationExtension> extensions = new ArrayList<>();

    public void addHL7ConfigurationExtension(JsonHL7ConfigurationExtension ext) {
        extensions.add(ext);
    }

    public boolean removeHL7ConfigurationExtension(JsonHL7ConfigurationExtension ext) {
        return extensions.remove(ext);
    }

    @Override
    protected void storeTo(Device device, JSONWriter writer) {
        HL7DeviceExtension ext = device.getDeviceExtension(HL7DeviceExtension.class);
        if (ext == null)
            return;

        writer.writeStartArray("hl7Application");
        for (HL7Application hl7App : ext.getHL7Applications())
            writeTo(device, hl7App, writer);

        writer.writeEnd();
    }

    @Override
    public boolean loadDeviceExtension(Device device, JSONReader reader, ConfigurationDelegate config)
            throws InternalException {
        if (!reader.getString().equals("hl7Application"))
            return false;

        HL7DeviceExtension ext = new HL7DeviceExtension();
        loadFrom(ext, reader, device, config);
        device.addDeviceExtension(ext);
        return true;
    }

    private void writeTo(Device device, HL7Application hl7App, JSONWriter writer) {
        writer.writeStartObject();
        writer.writeNotNullOrDef("hl7ApplicationName", hl7App.getApplicationName(), null);
        writer.writeNotNull("dicomInstalled", hl7App.getInstalled());
        writer.writeConnRefs(device.listConnections(), hl7App.getConnections());
        writer.writeNotEmpty("hl7AcceptedSendingApplication", hl7App.getAcceptedSendingApplications());
        writer.writeNotEmpty("hl7OtherApplicationName", hl7App.getOtherApplicationNames());
        writer.writeNotEmpty("hl7AcceptedMessageType", hl7App.getAcceptedMessageTypes());
        writer.writeNotNullOrDef("hl7DefaultCharacterSet", hl7App.getHL7DefaultCharacterSet(), "ASCII");
        writer.writeNotNullOrDef("hl7SendingCharacterSet", hl7App.getHL7SendingCharacterSet(), "ASCII");
        writer.writeNotEmpty("hl7OptionalMSHField", hl7App.getOptionalMSHFields());
        writer.writeNotNullOrDef("dicomDescription", hl7App.getDescription(), null);
        writer.writeNotEmpty("dicomApplicationCluster", hl7App.getApplicationClusters());
        for (JsonHL7ConfigurationExtension ext : extensions)
            ext.storeTo(hl7App, device, writer);
        writer.writeEnd();
    }

    private void loadFrom(HL7DeviceExtension ext, JSONReader reader, Device device, ConfigurationDelegate config)
            throws InternalException {
        List<Connection> conns = device.listConnections();
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            HL7Application hl7App = new HL7Application();
            loadFrom(hl7App, reader, device, conns, config);
            reader.expect(JsonParser.Event.END_OBJECT);
            ext.addHL7Application(hl7App);
        }
        reader.expect(JsonParser.Event.END_ARRAY);
    }

    private void loadFrom(HL7Application hl7App, JSONReader reader, Device device, List<Connection> conns,
            ConfigurationDelegate config) throws InternalException {
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
            case "hl7ApplicationName":
                hl7App.setApplicationName(reader.stringValue());
                break;
            case "dicomInstalled":
                hl7App.setInstalled(reader.booleanValue());
                break;
            case "dicomNetworkConnectionReference":
                for (String connRef : reader.stringArray())
                    hl7App.addConnection(conns.get(JSONReader.toConnectionIndex(connRef)));
                break;
            case "hl7AcceptedSendingApplication":
                hl7App.setAcceptedSendingApplications(reader.stringArray());
                break;
            case "hl7OtherApplicationName":
                hl7App.setOtherApplicationNames(reader.stringArray());
                break;
            case "hl7AcceptedMessageType":
                hl7App.setAcceptedMessageTypes(reader.stringArray());
                break;
            case "hl7DefaultCharacterSet":
                hl7App.setHL7DefaultCharacterSet(reader.stringValue());
                break;
            case "hl7SendingCharacterSet":
                hl7App.setHL7SendingCharacterSet(reader.stringValue());
                break;
            case "hl7OptionalMSHField":
                hl7App.setOptionalMSHFields(reader.intArray());
                break;
            case "dicomDescription":
                hl7App.setDescription(reader.stringValue());
                break;
            case "dicomApplicationCluster":
                hl7App.setApplicationClusters(reader.stringArray());
                break;
            default:
                if (!loadHL7ApplicationExtension(device, hl7App, reader, config))
                    reader.skipUnknownProperty();
            }
        }
    }

    private boolean loadHL7ApplicationExtension(Device device, HL7Application hl7App, JSONReader reader,
            ConfigurationDelegate config) throws InternalException {
        for (JsonHL7ConfigurationExtension ext : extensions)
            if (ext.loadHL7ApplicationExtension(device, hl7App, reader, config))
                return true;
        return false;
    }

}
