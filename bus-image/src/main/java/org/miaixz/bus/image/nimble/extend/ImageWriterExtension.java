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
package org.miaixz.bus.image.nimble.extend;

import java.io.Serial;

import org.miaixz.bus.image.metric.net.DeviceExtension;
import org.miaixz.bus.image.nimble.codec.ImageWriterFactory;

/**
 * Represents the ImageWriterExtension type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImageWriterExtension extends DeviceExtension {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852289637107L;

    /**
     * The factory value.
     */
    public volatile ImageWriterFactory factory;

    /**
     * Creates a new instance.
     *
     * @param factory the factory.
     */
    public ImageWriterExtension(ImageWriterFactory factory) {
        if (factory == null)
            throw new NullPointerException();
        this.factory = factory;
    }

    /**
     * Gets the image writer factory.
     *
     * @return the image writer factory.
     */
    public final ImageWriterFactory getImageWriterFactory() {
        return factory;
    }

    /**
     * Executes the reconfigure operation.
     *
     * @param from the from.
     */
    @Override
    public void reconfigure(DeviceExtension from) {
        reconfigureImageWriter((ImageWriterExtension) from);
    }

    /**
     * Executes the reconfigure image writer operation.
     *
     * @param from the from.
     */
    private void reconfigureImageWriter(ImageWriterExtension from) {
        factory = from.factory;
    }

}
