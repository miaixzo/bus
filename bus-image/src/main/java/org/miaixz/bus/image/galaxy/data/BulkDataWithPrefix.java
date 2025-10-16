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
