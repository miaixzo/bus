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
package org.miaixz.bus.image.metric.hl7;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents the MLLPOutputStream type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MLLPOutputStream extends FilterOutputStream {

    /**
     * Provides DICOM processing details.
     */
    private static final int SOM = 0x0b;

    /**
     * Provides DICOM processing details.
     */
    private static final byte[] EOM = { 0x1c, 0x0d };

    /**
     * The som written value.
     */
    private boolean somWritten;

    /**
     * Creates a new instance.
     *
     * @param out the out.
     */
    public MLLPOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Executes the write operation.
     *
     * @param b the b.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public synchronized void write(int b) throws IOException {
        writeStartBlock();
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
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        writeStartBlock();
        out.write(b, off, len);
    }

    /**
     * Writes the message.
     *
     * @param b the b.
     * @throws IOException if the operation cannot be completed.
     */
    public void writeMessage(byte[] b) throws IOException {
        writeMessage(b, 0, b.length);
    }

    /**
     * Writes the message.
     *
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     * @throws IOException if the operation cannot be completed.
     */
    public synchronized void writeMessage(byte[] b, int off, int len) throws IOException {
        if (somWritten)
            throw new IllegalStateException();

        byte[] msg = new byte[len + 3];
        msg[0] = SOM;
        System.arraycopy(b, off, msg, 1, len);
        System.arraycopy(EOM, 0, msg, len + 1, 2);
        out.write(msg);
        out.flush();
    }

    /**
     * Writes the start block.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void writeStartBlock() throws IOException {
        if (!somWritten) {
            out.write(SOM);
            somWritten = true;
        }
    }

    /**
     * Executes the finish operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public synchronized void finish() throws IOException {
        if (!somWritten)
            throw new IllegalStateException();
        out.write(EOM);
        out.flush();
        somWritten = false;
    }

}
