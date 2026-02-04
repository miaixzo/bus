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
package org.miaixz.bus.core.io.stream;

import java.io.OutputStream;

/**
 * An {@link OutputStream} implementation that discards all data written to it. This stream acts like writing to
 * {@code /dev/null} on Unix-like systems, effectively ignoring all output. This class is useful for scenarios where
 * output is required but should be suppressed.
 * <p>
 * Inspired by Apache Commons IO.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EmptyOutputStream extends OutputStream {

    /**
     * The singleton instance of {@code EmptyOutputStream}.
     */
    public static final EmptyOutputStream INSTANCE = new EmptyOutputStream();

    /**
     * Private constructor to enforce the singleton pattern.
     */
    private EmptyOutputStream() {
    }

    /**
     * Writes {@code len} bytes from the specified byte array starting at offset {@code off} to this output stream. This
     * method does nothing, effectively discarding the data.
     *
     * @param b   The data.
     * @param off The start offset in the data.
     * @param len The number of bytes to write.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) {
        // to /dev/null
    }

    /**
     * Writes the specified byte to this output stream. This method does nothing, effectively discarding the byte.
     *
     * @param b The byte to write.
     */
    @Override
    public void write(final int b) {
        // to /dev/null
    }

    /**
     * Writes {@code b.length} bytes from the specified byte array to this output stream. This method does nothing,
     * effectively discarding the data.
     *
     * @param b The data to write.
     */
    @Override
    public void write(final byte[] b) {
        // to /dev/null
    }

}
