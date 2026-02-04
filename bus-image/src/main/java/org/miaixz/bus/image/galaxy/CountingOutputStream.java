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
package org.miaixz.bus.image.galaxy;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * An {@link OutputStream} that counts the number of bytes written to the underlying output stream. This class can be
 * used to monitor the progress or size of data being written.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class CountingOutputStream extends FilterOutputStream {

    /**
     * The total number of bytes written to this output stream.
     */
    private volatile long count;

    /**
     * Creates a {@code CountingOutputStream} by decorating the given output stream.
     * 
     * @param out The underlying output stream to be decorated. Must not be {@code null}.
     * @throws NullPointerException if {@code out} is {@code null}.
     */
    public CountingOutputStream(OutputStream out) {
        super(Objects.requireNonNull(out));
    }

    /**
     * Returns the total number of bytes written to this output stream.
     * 
     * @return The total count of bytes written.
     */
    public long getCount() {
        return count;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        count++;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        count += len;
    }

}
