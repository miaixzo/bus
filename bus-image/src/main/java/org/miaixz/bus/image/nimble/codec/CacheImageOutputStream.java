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
package org.miaixz.bus.image.nimble.codec;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.imageio.stream.MemoryCacheImageOutputStream;

/**
 * @author Kimi Liu
 * @since Java 21+
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
