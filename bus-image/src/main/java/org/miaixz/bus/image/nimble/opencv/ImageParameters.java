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
package org.miaixz.bus.image.nimble.opencv;

/**
 * Represents the ImageParameters type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImageParameters {

    /**
     * The default tile size value.
     */
    public static final int DEFAULT_TILE_SIZE = 512;

    // List of supported color model format
    /**
     * The cm s rgb value.
     */
    public static final int CM_S_RGB = 1;

    /**
     * The cm s rgba value.
     */
    public static final int CM_S_RGBA = 2;

    /**
     * The cm gray value.
     */
    public static final int CM_GRAY = 3;

    /**
     * The cm gray alpha value.
     */
    public static final int CM_GRAY_ALPHA = 4;

    /**
     * The cm s ycc value.
     */
    public static final int CM_S_YCC = 4;

    /**
     * The cm e ycc value.
     */
    public static final int CM_E_YCC = 6;

    /**
     * The cm ycck value.
     */
    public static final int CM_YCCK = 7;

    /**
     * The cm cmyk value.
     */
    public static final int CM_CMYK = 8;

    // Extend type of DataBuffer
    /**
     * The type bit value.
     */
    public static final int TYPE_BIT = 6;

    // Basic image parameters
    /**
     * The height value.
     */
    private int height;

    /**
     * The width value.
     */
    private int width;
    //
    /**
     * The bits per sample value.
     */
    private int bitsPerSample;
    // Bands
    /**
     * The bands value.
     */
    private int bands;
    // Nb of components
    /**
     * The samples per pixel value.
     */
    private int samplesPerPixel;

    /**
     * The bytes per line value.
     */
    private int bytesPerLine;

    /**
     * The big endian value.
     */
    private boolean bigEndian;
    // DataBuffer types + TYPE_BIT
    /**
     * The data type value.
     */
    private int dataType;
    // Data offset of binary data
    /**
     * The bit offset value.
     */
    private int bitOffset;

    /**
     * The data offset value.
     */
    private int dataOffset;

    /**
     * The format value.
     */
    private int format;

    /**
     * The signed data value.
     */
    private boolean signedData;

    /**
     * The init signed data value.
     */
    private boolean initSignedData;

    /**
     * The jfif value.
     */
    private boolean jfif;

    /**
     * The jpeg marker value.
     */
    private int jpegMarker;

    /**
     * Creates a new instance.
     */
    public ImageParameters() {
        this(0, 0, 0, 0, false);
    }

    /**
     * Creates a new instance.
     *
     * @param height          the height.
     * @param width           the width.
     * @param bitsPerSample   the bits per sample.
     * @param samplesPerPixel the samples per pixel.
     * @param bigEndian       the big endian.
     */
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

    /**
     * Gets the height.
     *
     * @return the height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height.
     *
     * @param height the height.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Gets the width.
     *
     * @return the width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width.
     *
     * @param width the width.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Gets the bits per sample.
     *
     * @return the bits per sample.
     */
    public int getBitsPerSample() {
        return bitsPerSample;
    }

    /**
     * Sets the bits per sample.
     *
     * @param bitsPerSample the bits per sample.
     */
    public void setBitsPerSample(int bitsPerSample) {
        this.bitsPerSample = bitsPerSample;
    }

    /**
     * Gets the samples per pixel.
     *
     * @return the samples per pixel.
     */
    public int getSamplesPerPixel() {
        return samplesPerPixel;
    }

    /**
     * Sets the samples per pixel.
     *
     * @param samplesPerPixel the samples per pixel.
     */
    public void setSamplesPerPixel(int samplesPerPixel) {
        this.samplesPerPixel = samplesPerPixel;
    }

    /**
     * Gets the bytes per line.
     *
     * @return the bytes per line.
     */
    public int getBytesPerLine() {
        return bytesPerLine;
    }

    /**
     * Sets the bytes per line.
     *
     * @param bytesPerLine the bytes per line.
     */
    public void setBytesPerLine(int bytesPerLine) {
        this.bytesPerLine = bytesPerLine;
    }

    /**
     * Determines whether big endian.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isBigEndian() {
        return bigEndian;
    }

    /**
     * Sets the big endian.
     *
     * @param bigEndian the big endian.
     */
    public void setBigEndian(boolean bigEndian) {
        this.bigEndian = bigEndian;
    }

    /**
     * Gets the data type.
     *
     * @return the data type.
     */
    public int getDataType() {
        return dataType;
    }

    /**
     * Sets the data type.
     *
     * @param dataType the data type.
     */
    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    /**
     * Gets the format.
     *
     * @return the format.
     */
    public int getFormat() {
        return format;
    }

    /**
     * Sets the format.
     *
     * @param format the format.
     */
    public void setFormat(int format) {
        this.format = format;
    }

    /**
     * Gets the bit offset.
     *
     * @return the bit offset.
     */
    public int getBitOffset() {
        return bitOffset;
    }

    /**
     * Sets the bit offset.
     *
     * @param bitOffset the bit offset.
     */
    public void setBitOffset(int bitOffset) {
        this.bitOffset = bitOffset;
    }

    /**
     * Gets the data offset.
     *
     * @return the data offset.
     */
    public int getDataOffset() {
        return dataOffset;
    }

    /**
     * Sets the data offset.
     *
     * @param dataOffset the data offset.
     */
    public void setDataOffset(int dataOffset) {
        this.dataOffset = dataOffset;
    }

    /**
     * Determines whether signed data.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isSignedData() {
        return signedData;
    }

    /**
     * Sets the signed data.
     *
     * @param signedData the signed data.
     */
    public void setSignedData(boolean signedData) {
        this.signedData = signedData;
    }

    /**
     * Determines whether init signed data.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isInitSignedData() {
        return initSignedData;
    }

    /**
     * Sets the init signed data.
     *
     * @param initSignedData the init signed data.
     */
    public void setInitSignedData(boolean initSignedData) {
        this.initSignedData = initSignedData;
    }

    /**
     * Gets the bands.
     *
     * @return the bands.
     */
    public int getBands() {
        return bands;
    }

    /**
     * Sets the bands.
     *
     * @param bands the bands.
     */
    public void setBands(int bands) {
        this.bands = bands;
    }

    /**
     * Determines whether jfif.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isJFIF() {
        return jfif;
    }

    /**
     * Sets the jfif.
     *
     * @param jfif the jfif.
     */
    public void setJFIF(boolean jfif) {
        this.jfif = jfif;
    }

    /**
     * Gets the jpeg marker.
     *
     * @return the jpeg marker.
     */
    public int getJpegMarker() {
        return jpegMarker;
    }

    /**
     * Sets the jpeg marker.
     *
     * @param jpegMarker the jpeg marker.
     */
    public void setJpegMarker(int jpegMarker) {
        this.jpegMarker = jpegMarker;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        String buf = "Size:" + width + "x" + height + " Bits/Sample:" + bitsPerSample + " Samples/Pixel:"
                + samplesPerPixel + " Bytes/Line:" + bytesPerLine + " Signed:" + signedData;
        return buf;
    }

}
