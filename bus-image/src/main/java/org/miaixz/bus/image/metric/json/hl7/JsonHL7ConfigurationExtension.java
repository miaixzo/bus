/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.metric.json.hl7;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.hl7.net.HL7Application;
import org.miaixz.bus.image.metric.json.ConfigurationDelegate;
import org.miaixz.bus.image.metric.json.JSONReader;
import org.miaixz.bus.image.metric.json.JSONWriter;

/**
 * Defines the JsonHL7ConfigurationExtension contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface JsonHL7ConfigurationExtension {

    /**
     * Stores the to.
     *
     * @param hl7App the hl7 app.
     * @param device the device.
     * @param writer the writer.
     */
    void storeTo(HL7Application hl7App, Device device, JSONWriter writer);

    /**
     * Loads the hl7 application extension.
     *
     * @param device the device.
     * @param hl7App the hl7 app.
     * @param reader the reader.
     * @param config the config.
     * @return true if the condition is met; otherwise false.
     * @throws InternalException if the operation cannot be completed.
     */
    boolean loadHL7ApplicationExtension(
            Device device,
            HL7Application hl7App,
            JSONReader reader,
            ConfigurationDelegate config) throws InternalException;

}
