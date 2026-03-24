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

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;

import org.miaixz.bus.logger.Logger;

/**
 * Output stream for patching JPEG-LS compressed image data.
 * <p>
 * This class extends {@link ImageOutputStreamImpl} to provide special handling for JPEG-LS (Lossless JPEG) compression,
 * allowing modification of JPEG headers while preserving the compressed image data.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PatchJPEGLSOutputStream extends ImageOutputStreamImpl {

    private final ImageOutputStream ios;
    private final PatchJPEGLS patchJpegLS;
    private byte[] jpegheader;
    private int jpegheaderIndex;

    public PatchJPEGLSOutputStream(ImageOutputStream ios, PatchJPEGLS patchJpegLS) throws IOException {
        if (ios == null)
            throw new NullPointerException("ios");
        super.streamPos = ios.getStreamPosition();
        super.flushedPos = ios.getFlushedPosition();
        this.ios = ios;
        this.patchJpegLS = patchJpegLS;
        this.jpegheader = patchJpegLS != null ? new byte[256] : null;
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (jpegheader == null) {
            ios.write(b, off, len);
        } else {
            int len0 = Math.min(jpegheader.length - jpegheaderIndex, len);
            System.arraycopy(b, off, jpegheader, jpegheaderIndex, len0);
            jpegheaderIndex += len0;
            if (jpegheaderIndex >= jpegheader.length) {
                JPEGLSCodingParam param = patchJpegLS.createJPEGLSCodingParam(jpegheader);
                if (param == null)
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
        if (jpegheader == null) {
            ios.write(b);
            streamPos++;
        } else
            write(new byte[] { (byte) b }, 0, 1);
    }

    public int read() throws IOException {
        return ios.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return ios.read(b, off, len);
    }

}
