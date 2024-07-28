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
 * The JAI-ImageIO JPEG-LS CLibJPEGImageReader/CLibJPEGImageWriter contain a bug that makes them calculate non-default
 * JPEG-LS coding parameters for images with more than 12 bits per pixel, resulting in two problems:
 * <ol>
 * <li>JPEG-LS streams created by CLibJPEGImageWriter are not compliant with the JPEG-LS specification and can therefore
 * not be decoded using standard-compliant decoders. (Note: Some commercial decoders can automatically detect such
 * faulty JPEG-LS streams and are able to decode them correctly, e.g. Agfa/Pegasus.)</li>
 * <li>Reading a correct JPEG-LS stream using default coding parameters (i.e. not containing an LSE segment) with
 * CLibJPEGImageReader will result in a corrupted image.</li>
 * </ol>
 * <p>
 * This enum contains different options to use with a {@link PatchJPEGLSInputStream} or {@link PatchJPEGLSOutputStream}
 * to both patch faulty JPEG-LS streams created by JAI-ImageIO to make them readable by standard-compliant decoders and
 * to make correct JPEG-LS streams (created by other encoders) readable by JAI-ImageIO.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum PatchJPEGLS {

    /**
     * Amend JPEG-LS Coding parameters actually used by JAI-ImageIO.
     * <p>
     * Used to patch faulty JPEG-LS streams created by JAI-ImageIO CLibImageWriter, so the resulting JPEG-LS stream can
     * be decoded by JPEG-LS compliant decoders.
     * <p>
     * Warning: Patching a correct JPEG-LS (not created by JAI-ImageIO) with this option is likely to corrupt it (if it
     * has more than 12 bits per pixel and uses default coding parameters, i.e. doesn't contain an LSE segment). Use
     * JAI2ISO_IF_NO_APP_OR_COM to prevent this problem in some cases.
     */
    JAI2ISO,

    /**
     * Amend JPEG-LS Coding parameters actually used by JAI-ImageIO, but only if the stream does NOT contain APPn or COM
     * segments and is therefore more likely to have been actually created by JAI-ImageIO.
     * <p>
     * This option can be used, if you are not 100% sure whether the stream has actually been created by JAI-ImageIO and
     * you want to decrease the likeliness of corrupting a correct stream by incorrectly patching it. It will prevent
     * patching streams of some correct encoders (e.g. Agfa/Pegasus) that add APPn or COM segments. But as some correct
     * encoders (e.g. dcmtk in the current version 3.6.0) also do not create APPn or COM segments, this option might
     * still patch a correct stream and thereby make it corrupt.
     */
    JAI2ISO_IF_NO_APP_OR_COM,

    /**
     * Amend default JPEG-LS Coding parameters (for streams that do not contain them yet).
     * <p>
     * Used to patch correct JPEG-LS streams, so they can be decompressed by the faulty JAI-ImageIO CLibImageReader. The
     * resulting stream will still be correct JPEG-LS and can also be decoded by other decoders.
     * <p>
     * Warning: Patching faulty JPEG-LS streams created by JAI-ImageIO with this option will make them corrupt (i.e.
     * unreadable by both JAI-ImageIO and standard-compliant decoders).
     */
    ISO2JAI,

    /**
     * Amend default JPEG-LS Coding parameters (for streams that do not contain them yet), but only if the stream
     * contains APPn or COM segments - so it was certainly not created by JAI-ImageIO.
     * <p>
     * This option can be used to prevent adding those default parameters to faulty streams created by JAI-ImageIO which
     * would make them unreadable for both JAI-ImageIO and standard-compliant decoders. On the other hand some correct
     * encoders (e.g. dcmtk 3.6.0) also do not add APPn or COM segments and will therefore not be patched, which
     * prevents them from getting decompressed correctly with JAI-ImageIO, if this option is used. (Use the ISO2JAI
     * option for reading such streams with JAI-ImageIO.)
     */
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

        // additional markers (APPn or COM) besides SOI, SOF55 and SOS
        boolean additionalMarkers = jpegHeader.numberOfMarkers() > 3;

        if (this == ISO2JAI_IF_APP_OR_COM && !additionalMarkers)
            return null;

        if (this == JAI2ISO_IF_NO_APP_OR_COM && additionalMarkers)
            return null;

        int p = jpeg[sof55Off + 3] & 255;
        if (p <= 12)
            return null; // not more than 12 bits per pixel

        JPEGLSCodingParam param = this == JAI2ISO ? JPEGLSCodingParam.getJAIJPEGLSCodingParam(p)
                : JPEGLSCodingParam.getDefaultJPEGLSCodingParam(p, jpeg[sosOff + 6] & 255);
        param.setOffset(sosOff - 1);
        return param;
    }

}
