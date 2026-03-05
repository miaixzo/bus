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
 * @author Kimi Liu
 * @since Java 17+
 */
public final class LimitedInputStream extends FilterInputStream {

    private final boolean closeSource;
    private long remaining;
    private long mark = -1;

    public LimitedInputStream(InputStream in, long limit, boolean closeSource) {
        super(Objects.requireNonNull(in));
        if (limit <= 0)
            throw new IllegalArgumentException("limit must be > 0");
        this.remaining = limit;
        this.closeSource = closeSource;
    }

    @Override
    public int read() throws IOException {
        int result;
        if (remaining == 0 || (result = in.read()) < 0) {
            return -1;
        }

        --remaining;
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result;
        if (remaining == 0 || (result = in.read(b, off, (int) Math.min(len, remaining))) < 0) {
            return -1;
        }

        remaining -= result;
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        long result = in.skip(Math.min(n, remaining));
        remaining -= result;
        return result;
    }

    @Override
    public int available() throws IOException {
        return (int) Math.min(in.available(), remaining);
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
        mark = remaining;
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
        remaining = mark;
    }

    @Override
    public void close() throws IOException {
        if (closeSource)
            in.close();
    }

    public long getRemaining() {
        return remaining;
    }

}
