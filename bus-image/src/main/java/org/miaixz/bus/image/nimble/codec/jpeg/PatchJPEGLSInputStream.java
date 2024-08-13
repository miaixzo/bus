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
 * @author Kimi Liu
 * @since Java 17+
 */
public class PatchJPEGLSInputStream extends ImageInputStreamImpl implements BytesWithImageImageDescriptor {

    private final ImageInputStream iis;
    private long patchPos;
    private byte[] patch;

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
            Logger.debug("Patch JPEG-LS with {}", param);
            this.patchPos = streamPos + param.getOffset();
            this.patch = param.getBytes();
        }
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return (iis instanceof EncapsulatedPixelDataImageInputStream)
                ? ((EncapsulatedPixelDataImageInputStream) iis).getImageDescriptor()
                : (iis instanceof SegmentedInputImageStream) ? ((SegmentedInputImageStream) iis).getImageDescriptor()
                        : null;
    }

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

    public void close() throws IOException {
        super.close();
        iis.close();
    }

    public void flushBefore(long pos) throws IOException {
        super.flushBefore(pos);
        iis.flushBefore(adjustStreamPosition(pos));
    }

    private long adjustStreamPosition(long pos) {
        if (patch == null)
            return pos;
        long index = pos - patchPos;
        return index < 0 ? pos : index < patch.length ? patchPos : pos - patch.length;
    }

    public boolean isCached() {
        return iis.isCached();
    }

    public boolean isCachedFile() {
        return iis.isCachedFile();
    }

    public boolean isCachedMemory() {
        return iis.isCachedMemory();
    }

    public long length() {
        try {
            long len = iis.length();
            return patch == null || len < 0 ? len : len + patch.length;
        } catch (IOException e) {
            return -1;
        }
    }

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

    public void mark() {
        super.mark();
        iis.mark();
    }

    public void reset() throws IOException {
        super.reset();
        iis.reset();
    }

    public void seek(long pos) throws IOException {
        super.seek(pos);
        iis.seek(adjustStreamPosition(pos));
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

}
