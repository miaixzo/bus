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
package org.miaixz.bus.image.metric;

import java.io.IOException;

import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.metric.net.PDVOutputStream;

/**
 * Represents the DataWriterAdapter type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DataWriterAdapter implements DataWriter {

    /**
     * The data value.
     */
    private final Attributes data;

    /**
     * Creates a new instance.
     *
     * @param data the data.
     */
    public DataWriterAdapter(Attributes data) {
        if (data == null)
            throw new NullPointerException();
        this.data = data;
    }

    /**
     * Executes the for attributes operation.
     *
     * @param data the data.
     * @return the operation result.
     */
    public static DataWriterAdapter forAttributes(Attributes data) {
        return data != null ? new DataWriterAdapter(data) : null;
    }

    /**
     * Writes the to.
     *
     * @param out   the out.
     * @param tsuid the tsuid.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void writeTo(PDVOutputStream out, String tsuid) throws IOException {
        ImageOutputStream dos = new ImageOutputStream(out, tsuid);
        dos.writeDataset(null, data);
        dos.finish();
    }

    /**
     * Gets the dataset.
     *
     * @return the dataset.
     */
    public final Attributes getDataset() {
        return data;
    }

}
