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

import java.awt.*;

import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.nimble.codec.TransferSyntaxType;

/**
 * Represents the JpegWriteParam type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JpegWriteParam {

    /**
     * The type value.
     */
    private final TransferSyntaxType type;

    /**
     * The transfer syntax uid value.
     */
    private final String transferSyntaxUid;

    /**
     * JPEG lossless point transform (0..15, default: 0)
     */
    private int prediction;

    /**
     * The point transform value.
     */
    private int pointTransform;

    /**
     * The near lossless error value.
     */
    private int nearLosslessError;

    /**
     * The compression quality value.
     */
    private int compressionQuality;

    /**
     * The lossless compression value.
     */
    private boolean losslessCompression;

    /**
     * The source region value.
     */
    private Rectangle sourceRegion;

    /**
     * The compression ratio factor value.
     */
    private int compressionRatioFactor;

    /**
     * Creates a new instance.
     *
     * @param type              the type.
     * @param transferSyntaxUid the transfer syntax uid.
     */
    JpegWriteParam(TransferSyntaxType type, String transferSyntaxUid) {
        this.type = type;
        this.transferSyntaxUid = transferSyntaxUid;
        this.prediction = 1;
        this.pointTransform = 0;
        this.nearLosslessError = 0;
        this.compressionQuality = 85;
        this.losslessCompression = true;
        this.sourceRegion = null;
        this.compressionRatioFactor = 0;
    }

    /**
     * Builds the dicom image write param.
     *
     * @param tsuid the tsuid.
     * @return the operation result.
     */
    public static JpegWriteParam buildDicomImageWriteParam(String tsuid) {
        TransferSyntaxType type = TransferSyntaxType.forUID(tsuid);
        switch (type) {
            case NATIVE:
            case RLE:
            case JPIP:
            case MPEG:
                throw new IllegalStateException(tsuid + " is not supported for compression!");
        }
        JpegWriteParam param = new JpegWriteParam(type, tsuid);
        param.losslessCompression = !TransferSyntaxType.isLossyCompression(tsuid);
        param.setNearLosslessError(param.losslessCompression ? 0 : 2);
        param.setCompressionRatioFactor(param.losslessCompression ? 0 : 10);
        param.setCompressionQuality(param.losslessCompression ? 0 : param.getCompressionQuality());
        if (type == TransferSyntaxType.JPEG_LOSSLESS) {
            param.setPointTransform(0);
            if (UID.JPEGLossless.equals(tsuid)) {
                param.setPrediction(6);
            } else {
                param.setPrediction(1);
            }
        }

        return param;
    }

    /**
     * Gets the transfer syntax uid.
     *
     * @return the transfer syntax uid.
     */
    public String getTransferSyntaxUid() {
        return transferSyntaxUid;
    }

    /**
     * Gets the prediction.
     *
     * @return the prediction.
     */
    public int getPrediction() {
        return prediction;
    }

    /**
     * Sets the prediction.
     *
     * @param prediction the prediction.
     */
    public void setPrediction(int prediction) {
        this.prediction = prediction;
    }

    /**
     * Gets the point transform.
     *
     * @return the point transform.
     */
    public int getPointTransform() {
        return pointTransform;
    }

    /**
     * Sets the point transform.
     *
     * @param pointTransform the point transform.
     */
    public void setPointTransform(int pointTransform) {
        this.pointTransform = pointTransform;
    }

    /**
     * Gets the near lossless error.
     *
     * @return the near lossless error.
     */
    public int getNearLosslessError() {
        return nearLosslessError;
    }

    /**
     * Sets the near lossless error.
     *
     * @param nearLosslessError the near lossless error.
     */
    public void setNearLosslessError(int nearLosslessError) {
        if (nearLosslessError < 0)
            throw new IllegalArgumentException("nearLossless invalid value: " + nearLosslessError);
        this.nearLosslessError = nearLosslessError;
    }

    /**
     * Gets the compression quality.
     *
     * @return the compression quality.
     */
    public int getCompressionQuality() {
        return compressionQuality;
    }

    /**
     * @param compressionQuality between 1 and 100 (100 is the best lossy quality).
     */
    public void setCompressionQuality(int compressionQuality) {
        this.compressionQuality = compressionQuality;
    }

    /**
     * Gets the compression ratio factor.
     *
     * @return the compression ratio factor.
     */
    public int getCompressionRatioFactor() {
        return compressionRatioFactor;
    }

    /**
     * JPEG-2000 Lossy compression ratio factor.
     * <p>
     * Visually near-lossless typically achieves compression ratios of 10:1 to 20:1 (e.g. compressionRatioFactor = 10)
     * <p>
     * Lossy compression with acceptable degradation can have ratios of 50:1 to 100:1 (e.g. compressionRatioFactor = 50)
     *
     * @param compressionRatioFactor the compression ratio
     */
    public void setCompressionRatioFactor(int compressionRatioFactor) {
        this.compressionRatioFactor = compressionRatioFactor;
    }

    /**
     * Gets the type.
     *
     * @return the type.
     */
    public TransferSyntaxType getType() {
        return type;
    }

    /**
     * Determines whether compression lossless.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isCompressionLossless() {
        return losslessCompression;
    }

    /**
     * Gets the jpeg mode.
     *
     * @return the jpeg mode.
     */
    public int getJpegMode() {
        switch (type) {
            case JPEG_BASELINE:
                return 0;

            case JPEG_EXTENDED:
                return 1;

            case JPEG_SPECTRAL:
                return 2;

            case JPEG_PROGRESSIVE:
                return 3;

            case JPEG_LOSSLESS:
                return 4;

            default:
                return 0;
        }
    }

    /**
     * Gets the source region.
     *
     * @return the source region.
     */
    public Rectangle getSourceRegion() {
        return sourceRegion;
    }

    /**
     * Sets the source region.
     *
     * @param sourceRegion the source region.
     */
    public void setSourceRegion(Rectangle sourceRegion) {
        this.sourceRegion = sourceRegion;
        if (sourceRegion == null) {
            return;
        }
        if (sourceRegion.x < 0 || sourceRegion.y < 0 || sourceRegion.width <= 0 || sourceRegion.height <= 0) {
            throw new IllegalArgumentException("sourceRegion has illegal values!");
        }
    }

}
