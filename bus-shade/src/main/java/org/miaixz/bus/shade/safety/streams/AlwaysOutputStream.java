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
package org.miaixz.bus.shade.safety.streams;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link OutputStream} that wraps another output stream but prevents it from being closed. The {@link #close()}
 * method is overridden to do nothing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AlwaysOutputStream extends OutputStream {

    /**
     * The underlying output stream.
     */
    private final OutputStream out;

    /**
     * Constructs a new {@code AlwaysOutputStream}.
     *
     * @param out The output stream to wrap.
     */
    public AlwaysOutputStream(OutputStream out) {
        this.out = out;
    }

    /**
     * Writes the specified byte to this output stream.
     *
     * @param b The byte to be written.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    /**
     * Writes {@code b.length} bytes from the specified byte array to this output stream.
     *
     * @param b The data.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    /**
     * Writes {@code len} bytes from the specified byte array starting at offset {@code off} to this output stream.
     *
     * @param b   The data.
     * @param off The start offset in the data.
     * @param len The number of bytes to write.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Does nothing. This method is overridden to prevent the underlying stream from being closed.
     */
    @Override
    public void close() {
        // Do nothing, intentionally left blank to prevent closing the underlying stream.
    }

}
