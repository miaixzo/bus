/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
