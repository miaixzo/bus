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
package org.miaixz.bus.image.nimble.codec.jpeg;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

import org.miaixz.bus.image.nimble.codec.BytesWithImageImageDescriptor;
import org.miaixz.bus.image.nimble.codec.ImageDescriptor;
import org.miaixz.bus.image.nimble.stream.EncapsulatedPixelDataImageInputStream;
import org.miaixz.bus.image.nimble.stream.SegmentedInputImageStream;
import org.miaixz.bus.logger.Logger;

/**
 * Represents the PatchJPEGLSInputStream type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PatchJPEGLSInputStream extends ImageInputStreamImpl implements BytesWithImageImageDescriptor {

    /**
     * The iis value.
     */
    private final ImageInputStream iis;

    /**
     * The patch pos value.
     */
    private long patchPos;

    /**
     * The patch value.
     */
    private byte[] patch;

    /**
     * Creates a new instance.
     *
     * @param iis         the iis.
     * @param patchJPEGLS the patch jpegls.
     * @throws IOException if the operation cannot be completed.
     */
    public PatchJPEGLSInputStream(ImageInputStream iis, PatchJPEGLS patchJPEGLS) throws IOException {
        if (iis == null)
            throw new NullPointerException("iis");

        super.streamPos = iis.getStreamPosition();
        super.flushedPos = iis.getFlushedPosition();
        this.iis = iis;
        if (patchJPEGLS == null)
            return;

        JPEGLSCodingParam param = patchJPEGLS.createJPEGLSCodingParam(firstBytesOf(iis));
        if (param != null) {
            Logger.debug(false, "Image", "Patch JPEG-LS with {}", param);
            this.patchPos = streamPos + param.getOffset();
            this.patch = param.getBytes();
        }
    }

    /**
     * Gets the image descriptor.
     *
     * @return the image descriptor.
     */
    @Override
    public ImageDescriptor getImageDescriptor() {
        return (iis instanceof EncapsulatedPixelDataImageInputStream)
                ? ((EncapsulatedPixelDataImageInputStream) iis).getImageDescriptor()
                : (iis instanceof SegmentedInputImageStream) ? ((SegmentedInputImageStream) iis).getImageDescriptor()
                        : null;
    }

    /**
     * Executes the first bytes of operation.
     *
     * @param iis the iis.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private byte[] firstBytesOf(ImageInputStream iis) throws IOException {
        byte[] b = new byte[256];
        int n, off = 0, len = b.length;
        iis.mark();
        while (len > 0 && (n = iis.read(b, off, len)) > 0) {
            off += n;
            len -= n;
        }
        iis.reset();
        return len > 0 ? Arrays.copyOf(b, b.length - len) : b;
    }

    /**
     * Executes the close operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void close() throws IOException {
        super.close();
        iis.close();
    }

    /**
     * Executes the flush before operation.
     *
     * @param pos the pos.
     * @throws IOException if the operation cannot be completed.
     */
    public void flushBefore(long pos) throws IOException {
        super.flushBefore(pos);
        iis.flushBefore(adjustStreamPosition(pos));
    }

    /**
     * Executes the adjust stream position operation.
     *
     * @param pos the pos.
     * @return the operation result.
     */
    private long adjustStreamPosition(long pos) {
        if (patch == null)
            return pos;
        long index = pos - patchPos;
        return index < 0 ? pos : index < patch.length ? patchPos : pos - patch.length;
    }

    /**
     * Determines whether cached.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isCached() {
        return iis.isCached();
    }

    /**
     * Determines whether cached file.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isCachedFile() {
        return iis.isCachedFile();
    }

    /**
     * Determines whether cached memory.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isCachedMemory() {
        return iis.isCachedMemory();
    }

    /**
     * Executes the length operation.
     *
     * @return the operation result.
     */
    public long length() {
        try {
            long len = iis.length();
            return patch == null || len < 0 ? len : len + patch.length;
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * Executes the read operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public int read() throws IOException {
        int ch;
        long index;
        if (patch != null && (index = streamPos - patchPos) >= 0 && index < patch.length)
            ch = patch[(int) index];
        else
            ch = iis.read();
        if (ch >= 0)
            streamPos++;
        return ch;
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
    public int read(byte[] b, int off, int len) throws IOException {
        int r = 0;
        if (patch != null && streamPos < patchPos + patch.length) {
            if (streamPos < patchPos) {
                r = iis.read(b, off, (int) Math.min(patchPos - streamPos, len));
                if (r < 0)
                    return r;
                streamPos += r;
                if (streamPos < patchPos)
                    return r;
                off += r;
                len -= r;
            }
            int index = (int) (patchPos - streamPos);
            int r2 = Math.min(patch.length - index, len);
            System.arraycopy(patch, index, b, off, r2);
            streamPos += r2;
            r += r2;
            off += r2;
            len -= r2;
        }
        if (len > 0) {
            int r3 = iis.read(b, off, len);
            if (r3 < 0)
                return r3;
            streamPos += r3;
            r += r3;
        }
        return r;
    }

    /**
     * Executes the mark operation.
     */
    public void mark() {
        super.mark();
        iis.mark();
    }

    /**
     * Executes the reset operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void reset() throws IOException {
        super.reset();
        iis.reset();
    }

    /**
     * Executes the seek operation.
     *
     * @param pos the pos.
     * @throws IOException if the operation cannot be completed.
     */
    public void seek(long pos) throws IOException {
        super.seek(pos);
        iis.seek(adjustStreamPosition(pos));
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

}
