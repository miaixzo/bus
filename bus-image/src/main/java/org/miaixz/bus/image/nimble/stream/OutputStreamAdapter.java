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
package org.miaixz.bus.image.nimble.stream;

import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.stream.ImageOutputStreamImpl;

/**
 * Represents the OutputStreamAdapter type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class OutputStreamAdapter extends ImageOutputStreamImpl {

    /**
     * The out value.
     */
    private final OutputStream out;

    /**
     * Creates a new instance.
     *
     * @param out the out.
     */
    public OutputStreamAdapter(OutputStream out) {
        this.out = out;
    }

    /**
     * Executes the write operation.
     *
     * @param b the b.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    /**
     * Executes the write operation.
     *
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    /**
     * Executes the read operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Executes the read operation.
     *
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

}
