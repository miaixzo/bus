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
package org.miaixz.bus.image.nimble;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.nimble.opencv.op.MaskArea;

/**
 * Represents the TranscodeParam type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TranscodeParam {

    /**
     * The read param value.
     */
    private ImageReadParam readParam;

    /**
     * The write jpeg param value.
     */
    private JpegWriteParam writeJpegParam;

    /**
     * The output tsuid value.
     */
    private String outputTsuid;

    /**
     * The mask map value.
     */
    private Map<String, MaskArea> maskMap;

    /**
     * The output fmi value.
     */
    private boolean outputFmi;

    /**
     * The format value.
     */
    private Transcoder.Format format;

    /**
     * The jpeg compression quality value.
     */
    private Integer jpegCompressionQuality;

    /**
     * The preserve raw image value.
     */
    private Boolean preserveRawImage;

    /**
     * Creates a new instance.
     *
     * @param format the format.
     */
    public TranscodeParam(Transcoder.Format format) {
        this(null, format);
    }

    /**
     * Creates a new instance.
     *
     * @param dstTsuid the dst tsuid.
     */
    public TranscodeParam(String dstTsuid) {
        this(null, dstTsuid);
    }

    /**
     * Creates a new instance.
     *
     * @param readParam the read param.
     * @param format    the format.
     */
    public TranscodeParam(ImageReadParam readParam, Transcoder.Format format) {
        this.readParam = readParam == null ? new ImageReadParam() : readParam;
        this.format = format == null ? Transcoder.Format.JPEG : format;
        this.preserveRawImage = null;
        this.jpegCompressionQuality = null;
    }

    /**
     * Creates a new instance.
     *
     * @param readParam the read param.
     * @param dstTsuid  the dst tsuid.
     */
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

    /**
     * Gets the read param.
     *
     * @return the read param.
     */
    public ImageReadParam getReadParam() {
        return readParam;
    }

    /**
     * Gets the write jpeg param.
     *
     * @return the write jpeg param.
     */
    public JpegWriteParam getWriteJpegParam() {
        return writeJpegParam;
    }

    /**
     * Determines whether output fmi.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isOutputFmi() {
        return outputFmi;
    }

    /**
     * Sets the output fmi.
     *
     * @param outputFmi the output fmi.
     */
    public void setOutputFmi(boolean outputFmi) {
        this.outputFmi = outputFmi;
    }

    /**
     * Gets the output tsuid.
     *
     * @return the output tsuid.
     */
    public String getOutputTsuid() {
        return outputTsuid;
    }

    /**
     * Adds the mask map.
     *
     * @param maskMap the mask map.
     */
    public void addMaskMap(Map<? extends String, ? extends MaskArea> maskMap) {
        this.maskMap.putAll(maskMap);
    }

    /**
     * Gets the mask.
     *
     * @param key the key.
     * @return the mask.
     */
    public MaskArea getMask(String key) {
        MaskArea mask = maskMap.get(key);
        if (mask == null) {
            mask = maskMap.get("*");
        }
        return mask;
    }

    /**
     * Adds the mask.
     *
     * @param stationName the station name.
     * @param maskArea    the mask area.
     */
    public void addMask(String stationName, MaskArea maskArea) {
        this.maskMap.put(stationName, maskArea);
    }

    /**
     * Gets the mask map.
     *
     * @return the mask map.
     */
    public Map<String, MaskArea> getMaskMap() {
        return maskMap;
    }

    /**
     * Gets the jpeg compression quality.
     *
     * @return the jpeg compression quality.
     */
    public OptionalInt getJpegCompressionQuality() {
        return Builder.getOptionalInteger(jpegCompressionQuality);
    }

    /**
     * @param jpegCompressionQuality between 1 to 100 (100 is the best lossy quality).
     */
    public void setJpegCompressionQuality(int jpegCompressionQuality) {
        this.jpegCompressionQuality = jpegCompressionQuality;
    }

    /**
     * Determines whether preserve raw image.
     *
     * @return true if the condition is met; otherwise false.
     */
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

    /**
     * Gets the format.
     *
     * @return the format.
     */
    public Transcoder.Format getFormat() {
        return format;
    }

}
