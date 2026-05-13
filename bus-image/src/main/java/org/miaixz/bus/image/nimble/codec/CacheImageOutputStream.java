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
 * Represents the CacheImageOutputStream type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class CacheImageOutputStream extends MemoryCacheImageOutputStream implements BytesWithImageImageDescriptor {

    /**
     * The stream value.
     */
    private final ExtFilterOutputStream stream;

    /**
     * The image descriptor value.
     */
    private final ImageDescriptor imageDescriptor;

    /**
     * Creates a new instance.
     *
     * @param imageDescriptor the image descriptor.
     */
    public CacheImageOutputStream(ImageDescriptor imageDescriptor) {
        this(new ExtFilterOutputStream(), imageDescriptor);
    }

    /**
     * Creates a new instance.
     *
     * @param stream          the stream.
     * @param imageDescriptor the image descriptor.
     */
    private CacheImageOutputStream(ExtFilterOutputStream stream, ImageDescriptor imageDescriptor) {
        super(stream);
        this.stream = stream;
        this.imageDescriptor = imageDescriptor;
    }

    /**
     * Sets the output stream.
     *
     * @param stream the stream.
     */
    public void setOutputStream(OutputStream stream) {
        this.stream.setOutputStream(stream);
    }

    /**
     * Gets the image descriptor.
     *
     * @return the image descriptor.
     */
    @Override
    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    /**
     * Gets the bytes.
     *
     * @return the bytes.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Executes the flush before operation.
     *
     * @param pos the pos.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void flushBefore(long pos) throws IOException {
        if (stream.getOutputStream() != null)
            super.flushBefore(pos);
    }

    /**
     * Represents the ExtFilterOutputStream type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class ExtFilterOutputStream extends FilterOutputStream {

        /**
         * Creates a new instance.
         */
        public ExtFilterOutputStream() {
            super(null);
        }

        /**
         * Gets the output stream.
         *
         * @return the output stream.
         */
        public OutputStream getOutputStream() {
            return super.out;
        }

        /**
         * Sets the output stream.
         *
         * @param out the out.
         */
        public void setOutputStream(OutputStream out) {
            super.out = out;
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

    }

}
