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
package org.miaixz.bus.image.galaxy.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Represents the RAFInputStreamAdapter type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RAFInputStreamAdapter extends InputStream {

    /**
     * The raf value.
     */
    private final RandomAccessFile raf;

    /**
     * The marked pos value.
     */
    private long markedPos;

    /**
     * The mark exception value.
     */
    private IOException markException;

    /**
     * Creates a new instance.
     *
     * @param raf the raf.
     */
    public RAFInputStreamAdapter(RandomAccessFile raf) {
        if (raf == null)
            throw new NullPointerException();
        this.raf = raf;
    }

    /**
     * Executes the read operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public int read() throws IOException {
        return raf.read();
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
        return raf.read(b, off, len);
    }

    /**
     * Executes the skip operation.
     *
     * @param n the n.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public long skip(long n) throws IOException {
        return raf.skipBytes((int) n);
    }

    /**
     * Executes the mark operation.
     *
     * @param readlimit the readlimit.
     */
    @Override
    public synchronized void mark(int readlimit) {
        try {
            this.markedPos = raf.getFilePointer();
            this.markException = null;
        } catch (IOException e) {
            this.markException = e;
        }
    }

    /**
     * Executes the reset operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public synchronized void reset() throws IOException {
        if (markException != null)
            throw markException;
        raf.seek(markedPos);
    }

    /**
     * Executes the mark supported operation.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean markSupported() {
        return true;
    }

}
