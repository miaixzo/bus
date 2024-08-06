/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.nimble.codec;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.imageio.stream.MemoryCacheImageOutputStream;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
final class CacheImageOutputStream extends MemoryCacheImageOutputStream implements BytesWithImageImageDescriptor {

    private final ExtFilterOutputStream stream;
    private final ImageDescriptor imageDescriptor;

    public CacheImageOutputStream(ImageDescriptor imageDescriptor) {
        this(new ExtFilterOutputStream(), imageDescriptor);
    }

    private CacheImageOutputStream(ExtFilterOutputStream stream, ImageDescriptor imageDescriptor) {
        super(stream);
        this.stream = stream;
        this.imageDescriptor = imageDescriptor;
    }

    public void setOutputStream(OutputStream stream) {
        this.stream.setOutputStream(stream);
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    @Override
    public ByteBuffer getBytes() throws IOException {
        byte[] array = new byte[8192];
        int length = 0;
        int read;
        while ((read = this.read(array, length, array.length - length)) > 0) {
            if ((length += read) == array.length)
                array = Arrays.copyOf(array, array.length << 1);
        }
        return ByteBuffer.wrap(array, 0, length);
    }

    @Override
    public void flushBefore(long pos) throws IOException {
        if (stream.getOutputStream() != null)
            super.flushBefore(pos);
    }

    private static final class ExtFilterOutputStream extends FilterOutputStream {

        public ExtFilterOutputStream() {
            super(null);
        }

        public OutputStream getOutputStream() {
            return super.out;
        }

        public void setOutputStream(OutputStream out) {
            super.out = out;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }
    }

}
