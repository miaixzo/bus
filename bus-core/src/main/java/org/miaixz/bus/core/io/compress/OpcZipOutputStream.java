/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.io.compress;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Excel兼容的ZIP64 OutputStream实现 来自并见： https://github.com/rzymek/opczip
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OpcZipOutputStream extends ZipOutputStream {

    private final Zip64 spec;
    private final List<Zip64.Entry> entries = new ArrayList<>();
    private final CRC32 crc = new CRC32();
    private Zip64.Entry current;
    private int written = 0;
    private boolean finished = false;

    /**
     * 构造
     *
     * @param out 写出压缩数据额输出流
     */
    public OpcZipOutputStream(final OutputStream out) {
        super(out);
        this.spec = new Zip64(out);
    }

    @Override
    public void putNextEntry(final ZipEntry e) throws IOException {
        if (current != null) {
            closeEntry();
        }
        current = new Zip64.Entry(e.getName());
        current.offset = written;
        written += spec.writeLFH(current);
        entries.add(current);
    }

    @Override
    public void closeEntry() throws IOException {
        if (current == null) {
            throw new IllegalStateException("not current zip current");
        }
        def.finish();
        while (!def.finished()) {
            deflate();
        }

        current.size = def.getBytesRead();
        current.compressedSize = (int) def.getBytesWritten();
        current.crc = crc.getValue();

        written += current.compressedSize;
        written += spec.writeDAT(current);
        current = null;
        def.reset();
        crc.reset();
    }

    @Override
    public void finish() throws IOException {
        if (finished) {
            return;
        }
        if (current != null) {
            closeEntry();
        }
        final int offset = written;
        for (final Zip64.Entry entry : entries) {
            written += spec.writeCEN(entry);
        }
        written += spec.writeEND(entries.size(), offset, written - offset);
        finished = true;
    }

    /**
     * @see ZipOutputStream#write(byte[], int, int)
     */
    @Override
    public synchronized void write(final byte[] b, final int off, final int len) throws IOException {
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        super.write(b, off, len);
        crc.update(b, off, len);
    }

    @Override
    public void close() throws IOException {
        finish();
        out.close();
    }

}
