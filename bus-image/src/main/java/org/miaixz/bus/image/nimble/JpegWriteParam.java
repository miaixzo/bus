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

import java.awt.*;

import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.nimble.codec.TransferSyntaxType;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class JpegWriteParam {

    private final TransferSyntaxType type;
    private final String transferSyntaxUid;
    /**
     * JPEG lossless point transform (0..15, default: 0)
     */
    private int prediction;
    private int pointTransform;
    private int nearLosslessError;
    private int compressionQuality;
    private boolean losslessCompression;
    private Rectangle sourceRegion;
    private int compressionRatioFactor;

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

    public String getTransferSyntaxUid() {
        return transferSyntaxUid;
    }

    public int getPrediction() {
        return prediction;
    }

    public void setPrediction(int prediction) {
        this.prediction = prediction;
    }

    public int getPointTransform() {
        return pointTransform;
    }

    public void setPointTransform(int pointTransform) {
        this.pointTransform = pointTransform;
    }

    public int getNearLosslessError() {
        return nearLosslessError;
    }

    public void setNearLosslessError(int nearLosslessError) {
        if (nearLosslessError < 0)
            throw new IllegalArgumentException("nearLossless invalid value: " + nearLosslessError);
        this.nearLosslessError = nearLosslessError;
    }

    public int getCompressionQuality() {
        return compressionQuality;
    }

    /**
     * @param compressionQuality between 1 and 100 (100 is the best lossy quality).
     */
    public void setCompressionQuality(int compressionQuality) {
        this.compressionQuality = compressionQuality;
    }

    public int getCompressionRatioFactor() {
        return compressionRatioFactor;
    }

    /**
     * JPEG-2000 Lossy compression ratio factor.
     *
     * <p>
     * Visually near-lossless typically achieves compression ratios of 10:1 to 20:1 (e.g. compressionRatioFactor = 10)
     *
     * <p>
     * Lossy compression with acceptable degradation can have ratios of 50:1 to 100:1 (e.g. compressionRatioFactor = 50)
     *
     * @param compressionRatioFactor the compression ratio
     */
    public void setCompressionRatioFactor(int compressionRatioFactor) {
        this.compressionRatioFactor = compressionRatioFactor;
    }

    public TransferSyntaxType getType() {
        return type;
    }

    public boolean isCompressionLossless() {
        return losslessCompression;
    }

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

    public Rectangle getSourceRegion() {
        return sourceRegion;
    }

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
