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
package org.miaixz.bus.image.metric.json;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.net.ApplicationEntity;

/**
 * Represents the JsonConfigurationExtension type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JsonConfigurationExtension {

    /**
     * Constructs a new JsonConfigurationExtension instance.
     */
    public JsonConfigurationExtension() {
        // No initialization required.
    }

    /**
     * The config value.
     */
    protected JsonConfiguration config;

    /**
     * Gets the json configuration.
     *
     * @return the json configuration.
     */
    public JsonConfiguration getJsonConfiguration() {
        return config;
    }

    /**
     * Sets the json configuration.
     *
     * @param config the config.
     */
    public void setJsonConfiguration(JsonConfiguration config) {
        if (config != null && this.config != null)
            throw new IllegalStateException("already owned by other Json Configuration");
        this.config = config;
    }

    /**
     * Stores the to.
     *
     * @param device the device.
     * @param writer the writer.
     */
    protected void storeTo(Device device, JSONWriter writer) {
    }

    /**
     * Stores the to.
     *
     * @param ae     the ae.
     * @param writer the writer.
     */
    protected void storeTo(ApplicationEntity ae, JSONWriter writer) {
    }

    /**
     * Loads the device extension.
     *
     * @param device the device.
     * @param reader the reader.
     * @param config the config.
     * @return true if the condition is met; otherwise false.
     * @throws InternalException if the operation cannot be completed.
     */
    public boolean loadDeviceExtension(Device device, JSONReader reader, ConfigurationDelegate config)
            throws InternalException {
        return false;
    }

    /**
     * Loads the application entity extension.
     *
     * @param device the device.
     * @param ae     the ae.
     * @param reader the reader.
     * @param config the config.
     * @return true if the condition is met; otherwise false.
     * @throws InternalException if the operation cannot be completed.
     */
    public boolean loadApplicationEntityExtension(
            Device device,
            ApplicationEntity ae,
            JSONReader reader,
            ConfigurationDelegate config) throws InternalException {
        return false;
    }

}
