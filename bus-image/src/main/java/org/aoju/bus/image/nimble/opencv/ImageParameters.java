/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.image.nimble.opencv;

import lombok.Data;
import org.aoju.bus.core.lang.Normal;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
public class ImageParameters {

    public static final int DEFAULT_TILE_SIZE = Normal._512;

    // List of supported color model format
    public static final int CM_S_RGB = 1;
    public static final int CM_S_RGBA = 2;
    public static final int CM_GRAY = 3;
    public static final int CM_GRAY_ALPHA = 4;
    public static final int CM_S_YCC = 4;
    public static final int CM_E_YCC = 6;
    public static final int CM_YCCK = 7;
    public static final int CM_CMYK = 8;

    // Extend type of DataBuffer
    public static final int TYPE_BIT = 6;

    // Basic image parameters
    private int height;
    private int width;
    //
    private int bitsPerSample;
    // Bands
    private int bands;
    // Nb of components
    private int samplesPerPixel;
    private int bytesPerLine;
    private boolean bigEndian;
    // DataBuffer types + TYPE_BIT
    private int dataType;
    // Data offset of binary data
    private int bitOffset;
    private int dataOffset;
    private int format;
    private boolean signedData;
    private boolean initSignedData;
    private boolean jfif;
    private int jpegMarker;

    public ImageParameters() {
        this(0, 0, 0, 0, false);
    }

    public ImageParameters(int height, int width, int bitsPerSample, int samplesPerPixel, boolean bigEndian) {
        this.height = height;
        this.width = width;
        this.bitsPerSample = bitsPerSample;
        this.samplesPerPixel = samplesPerPixel;
        this.bigEndian = bigEndian;
        this.bands = 1;
        this.dataType = -1;
        this.bytesPerLine = 0;
        this.bitOffset = 0;
        this.dataOffset = 0;
        this.format = CM_GRAY;
        this.signedData = false;
        this.initSignedData = false;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("Size:");
        buf.append(width);
        buf.append("x");
        buf.append(height);
        buf.append(" Bits/Sample:");
        buf.append(bitsPerSample);
        buf.append(" Samples/Pixel:");
        buf.append(samplesPerPixel);
        buf.append(" Bytes/Line:");
        buf.append(bytesPerLine);
        buf.append(" Signed:");
        buf.append(signedData);
        return buf.toString();
    }

}
