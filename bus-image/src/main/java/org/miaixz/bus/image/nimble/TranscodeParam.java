/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.nimble;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.nimble.opencv.op.MaskArea;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class TranscodeParam {

    private ImageReadParam readParam;
    private JpegWriteParam writeJpegParam;
    private String outputTsuid;
    private Map<String, MaskArea> maskMap;
    private boolean outputFmi;

    private Transcoder.Format format;

    private Integer jpegCompressionQuality;
    private Boolean preserveRawImage;

    public TranscodeParam(Transcoder.Format format) {
        this(null, format);
    }

    public TranscodeParam(String dstTsuid) {
        this(null, dstTsuid);
    }

    public TranscodeParam(ImageReadParam readParam, Transcoder.Format format) {
        this.readParam = readParam == null ? new ImageReadParam() : readParam;
        this.format = format == null ? Transcoder.Format.JPEG : format;
        this.preserveRawImage = null;
        this.jpegCompressionQuality = null;
    }

    public TranscodeParam(ImageReadParam readParam, String dstTsuid) {
        this.readParam = readParam == null ? new ImageReadParam() : readParam;
        this.outputTsuid = dstTsuid;
        this.maskMap = new HashMap<>();
        if (ImageOutputData.isNativeSyntax(dstTsuid)) {
            this.writeJpegParam = null;
        } else {
            this.writeJpegParam = JpegWriteParam.buildDicomImageWriteParam(dstTsuid);
        }
    }

    public ImageReadParam getReadParam() {
        return readParam;
    }

    public JpegWriteParam getWriteJpegParam() {
        return writeJpegParam;
    }

    public boolean isOutputFmi() {
        return outputFmi;
    }

    public void setOutputFmi(boolean outputFmi) {
        this.outputFmi = outputFmi;
    }

    public String getOutputTsuid() {
        return outputTsuid;
    }

    public void addMaskMap(Map<? extends String, ? extends MaskArea> maskMap) {
        this.maskMap.putAll(maskMap);
    }

    public MaskArea getMask(String key) {
        MaskArea mask = maskMap.get(key);
        if (mask == null) {
            mask = maskMap.get("*");
        }
        return mask;
    }

    public void addMask(String stationName, MaskArea maskArea) {
        this.maskMap.put(stationName, maskArea);
    }

    public Map<String, MaskArea> getMaskMap() {
        return maskMap;
    }

    public OptionalInt getJpegCompressionQuality() {
        return Builder.getOptionalInteger(jpegCompressionQuality);
    }

    /**
     * @param jpegCompressionQuality between 1 to 100 (100 is the best lossy quality).
     */
    public void setJpegCompressionQuality(int jpegCompressionQuality) {
        this.jpegCompressionQuality = jpegCompressionQuality;
    }

    public Optional<Boolean> isPreserveRawImage() {
        return Optional.ofNullable(preserveRawImage);
    }

    /**
     * It preserves the raw data when the pixel depth is more than 8 bit. The default value applies the W/L and is
     * FALSE, the output image will be always a 8-bit per sample image.
     *
     * @param preserveRawImage
     */
    public void setPreserveRawImage(Boolean preserveRawImage) {
        this.preserveRawImage = preserveRawImage;
    }

    public Transcoder.Format getFormat() {
        return format;
    }

}
