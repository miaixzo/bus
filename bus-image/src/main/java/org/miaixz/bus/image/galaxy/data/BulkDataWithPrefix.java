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
package org.miaixz.bus.image.galaxy.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

/**
 * Represents DICOM Bulk Data that includes a prefix byte array before the actual bulk data content. This class extends
 * {@link BulkData} and overrides the {@code openStream()} method to prepend the prefix to the input stream.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class BulkDataWithPrefix extends BulkData {

    /**
     * The byte array prefix to be prepended to the bulk data stream.
     */
    private final byte[] prefix;

    /**
     * Constructs a {@code BulkDataWithPrefix} object.
     * 
     * @param uri       The URI reference to the bulk data.
     * @param offset    The offset of the data within the resource (excluding the prefix).
     * @param length    The length of the actual bulk data (excluding the prefix).
     * @param bigEndian {@code true} if the data is big-endian, {@code false} otherwise.
     * @param prefix    The byte array to prepend to the bulk data.
     */
    public BulkDataWithPrefix(String uri, long offset, int length, boolean bigEndian, byte... prefix) {
        super(uri, offset, length + prefix.length, bigEndian);
        this.prefix = prefix.clone();
    }

    @Override
    public InputStream openStream() throws IOException {
        return new SequenceInputStream(new ByteArrayInputStream(prefix), super.openStream());
    }

}
