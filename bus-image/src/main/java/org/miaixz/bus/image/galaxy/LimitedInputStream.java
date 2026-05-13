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
 * Represents the LimitedInputStream type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class LimitedInputStream extends FilterInputStream {

    /**
     * The close source value.
     */
    private final boolean closeSource;

    /**
     * The remaining value.
     */
    private long remaining;

    /**
     * The mark value.
     */
    private long mark = -1;

    /**
     * Creates a new instance.
     *
     * @param in          the in.
     * @param limit       the limit.
     * @param closeSource the close source.
     */
    public LimitedInputStream(InputStream in, long limit, boolean closeSource) {
        super(Objects.requireNonNull(in));
        if (limit <= 0)
            throw new IllegalArgumentException("limit must be > 0");
        this.remaining = limit;
        this.closeSource = closeSource;
    }

    /**
     * Executes the read operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public int read() throws IOException {
        int result;
        if (remaining == 0 || (result = in.read()) < 0) {
            return -1;
        }

        --remaining;
        return result;
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
        int result;
        if (remaining == 0 || (result = in.read(b, off, (int) Math.min(len, remaining))) < 0) {
            return -1;
        }

        remaining -= result;
        return result;
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
        long result = in.skip(Math.min(n, remaining));
        remaining -= result;
        return result;
    }

    /**
     * Executes the available operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public int available() throws IOException {
        return (int) Math.min(in.available(), remaining);
    }

    /**
     * Executes the mark operation.
     *
     * @param readlimit the readlimit.
     */
    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
        mark = remaining;
    }

    /**
     * Executes the reset operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public synchronized void reset() throws IOException {
        in.reset();
        remaining = mark;
    }

    /**
     * Executes the close operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void close() throws IOException {
        if (closeSource)
            in.close();
    }

    /**
     * Gets the remaining.
     *
     * @return the remaining.
     */
    public long getRemaining() {
        return remaining;
    }

}
