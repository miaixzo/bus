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

    /**
     * The ios value.
     */
    private final ImageOutputStream ios;

    /**
     * The patch jpeg ls value.
     */
    private final PatchJPEGLS patchJpegLS;

    /**
     * The jpegheader value.
     */
    private byte[] jpegheader;

    /**
     * The jpegheader index value.
     */
    private int jpegheaderIndex;

    /**
     * Creates a new instance.
     *
     * @param ios         the ios.
     * @param patchJpegLS the patch jpeg ls.
     * @throws IOException if the operation cannot be completed.
     */
    public PatchJPEGLSOutputStream(ImageOutputStream ios, PatchJPEGLS patchJpegLS) throws IOException {
        if (ios == null)
            throw new NullPointerException("ios");
        super.streamPos = ios.getStreamPosition();
        super.flushedPos = ios.getFlushedPosition();
        this.ios = ios;
        this.patchJpegLS = patchJpegLS;
        this.jpegheader = patchJpegLS != null ? new byte[256] : null;
    }

    /**
     * Executes the write operation.
     *
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     * @throws IOException if the operation cannot be completed.
     */
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
                    Logger.debug(false, "Image", "Patch JPEG-LS with {}", param);
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

    /**
     * Executes the write operation.
     *
     * @param b the b.
     * @throws IOException if the operation cannot be completed.
     */
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Executes the write operation.
     *
     * @param b the b.
     * @throws IOException if the operation cannot be completed.
     */
    public void write(int b) throws IOException {
        if (jpegheader == null) {
            ios.write(b);
            streamPos++;
        } else
            write(new byte[] { (byte) b }, 0, 1);
    }

    /**
     * Executes the read operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public int read() throws IOException {
        return ios.read();
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
        return ios.read(b, off, len);
    }

}
