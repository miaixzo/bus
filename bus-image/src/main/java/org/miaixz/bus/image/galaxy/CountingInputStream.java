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
package org.miaixz.bus.image.galaxy;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * An {@link InputStream} that counts the number of bytes read.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CountingInputStream extends FilterInputStream {

    /**
     * The count value.
     */
    private volatile long count;

    /**
     * The mark value.
     */
    private volatile long mark;

    /**
     * Creates a new instance.
     *
     * @param in the in.
     */
    public CountingInputStream(InputStream in) {
        super(Objects.requireNonNull(in));
    }

    /**
     * Gets the count.
     *
     * @return the count.
     */
    public long getCount() {
        return count;
    }

    /**
     * Executes the read operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public int read() throws IOException {
        return incCount(in.read());
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
        return addCount(in.read(b, off, len));
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
        return addCount(in.skip(n));
    }

    /**
     * Executes the mark operation.
     *
     * @param readlimit the readlimit.
     */
    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
        mark = count;
    }

    /**
     * Executes the reset operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public synchronized void reset() throws IOException {
        in.reset();
        count = mark;
    }

    /**
     * Executes the inc count operation.
     *
     * @param read the read.
     * @return the operation result.
     */
    private int incCount(int read) {
        if (read >= 0)
            count++;
        return read;
    }

    /**
     * Adds the count.
     *
     * @param read the read.
     * @return the operation result.
     */
    private int addCount(int read) {
        if (read > 0)
            count += read;
        return read;
    }

    /**
     * Adds the count.
     *
     * @param skip the skip.
     * @return the operation result.
     */
    private long addCount(long skip) {
        if (skip > 0)
            count += skip;
        return skip;
    }

}
