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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum PatchJPEGLS {
    JAI2ISO,
    ISO2JAI,
    ISO2JAI_IF_APP_OR_COM;

    public JPEGLSCodingParam createJPEGLSCodingParam(byte[] jpeg) {
        JPEGHeader jpegHeader = new JPEGHeader(jpeg, JPEG.SOS);
        int soiOff = jpegHeader.offsetOf(JPEG.SOI);
        int sof55Off = jpegHeader.offsetOf(JPEG.SOF55);
        int lseOff = jpegHeader.offsetOf(JPEG.LSE);
        int sosOff = jpegHeader.offsetOf(JPEG.SOS);

        if (soiOff == -1)
            return null; // no JPEG

        if (sof55Off == -1)
            return null; // no JPEG-LS

        if (lseOff != -1)
            return null; // already patched

        if (sosOff == -1)
            return null;

        if (this == ISO2JAI_IF_APP_OR_COM
                && jpegHeader.numberOfMarkers() == 3)
            return null;

        int p = jpeg[sof55Off + 3] & 255;
        if (p <= 12)
            return null;

        JPEGLSCodingParam param = this == JAI2ISO
                ? JPEGLSCodingParam.getJAIJPEGLSCodingParam(p)
                : JPEGLSCodingParam.getDefaultJPEGLSCodingParam(p,
                jpeg[sosOff + 6] & 255);
        param.setOffset(sosOff - 1);
        return param;
    }
}
