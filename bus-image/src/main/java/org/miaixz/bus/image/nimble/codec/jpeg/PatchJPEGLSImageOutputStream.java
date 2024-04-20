/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image.nimble.codec.jpeg;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.logger.Logger;

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;
import java.io.IOException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PatchJPEGLSImageOutputStream extends ImageOutputStreamImpl {

    private final ImageOutputStream ios;
    private final PatchJPEGLS patchJpegLS;
    private byte[] jpegheader;
    private int jpegheaderIndex;

    public PatchJPEGLSImageOutputStream(ImageOutputStream ios,
                                        PatchJPEGLS patchJpegLS) throws IOException {
        if (null == ios)
            throw new NullPointerException("ios");
        super.streamPos = ios.getStreamPosition();
        super.flushedPos = ios.getFlushedPosition();
        this.ios = ios;
        this.patchJpegLS = patchJpegLS;
        this.jpegheader = null != patchJpegLS ? new byte[Normal._256] : null;
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (null == jpegheader) {
            ios.write(b, off, len);
        } else {
            int len0 = Math.min(jpegheader.length - jpegheaderIndex, len);
            System.arraycopy(b, off, jpegheader, jpegheaderIndex, len0);
            jpegheaderIndex += len0;
            if (jpegheaderIndex >= jpegheader.length) {
                JPEGLSCodingParam param =
                        patchJpegLS.createJPEGLSCodingParam(jpegheader);
                if (null == param)
                    ios.write(jpegheader);
                else {
                    Logger.debug("Patch JPEG-LS with {}", param);
                    int offset = param.getOffset();
                    ios.write(jpegheader, 0, offset);
                    ios.write(param.getBytes());
                    ios.write(jpegheader, offset, jpegheader.length - offset);
                }
                ios.write(b, off + len0, len - len0);
                jpegheader = null;
            }
        }
        streamPos += len;
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(int b) throws IOException {
        if (null == jpegheader) {
            ios.write(b);
            streamPos++;
        } else
            write(new byte[]{(byte) b}, 0, 1);
    }

    public int read() throws IOException {
        return ios.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return ios.read(b, off, len);
    }
}
