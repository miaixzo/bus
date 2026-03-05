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
 * @since Java 17+
 */
public class CountingInputStream extends FilterInputStream {

    private volatile long count;
    private volatile long mark;

    public CountingInputStream(InputStream in) {
        super(Objects.requireNonNull(in));
    }

    public long getCount() {
        return count;
    }

    @Override
    public int read() throws IOException {
        return incCount(in.read());
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return addCount(in.read(b, off, len));
    }

    @Override
    public long skip(long n) throws IOException {
        return addCount(in.skip(n));
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
        mark = count;
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
        count = mark;
    }

    private int incCount(int read) {
        if (read >= 0)
            count++;
        return read;
    }

    private int addCount(int read) {
        if (read > 0)
            count += read;
        return read;
    }

    private long addCount(long skip) {
        if (skip > 0)
            count += skip;
        return skip;
    }

}
