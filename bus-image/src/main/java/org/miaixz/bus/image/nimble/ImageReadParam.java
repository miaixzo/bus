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
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.OptionalDouble;

import javax.imageio.IIOParamController;
import javax.imageio.ImageTypeSpecifier;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.nimble.opencv.lut.LutShape;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageReadParam extends javax.imageio.ImageReadParam {

    private static final String NOT_COMPATIBLE = "Not compatible with the native DICOM Decoder";

    private Double windowCenter;
    private Double windowWidth;
    private Double levelMin;
    private Double levelMax;
    private LutShape voiLutShape;
    private Boolean applyPixelPadding;
    private Boolean inverseLut;
    private Boolean fillOutsideLutRange;
    private Boolean applyWindowLevelToColorImage;
    private Boolean keepRgbForLossyJpeg;

    private Boolean releaseImageAfterProcessing;
    private PresentationLutObject presentationState;

    private int windowIndex;
    private int voiLUTIndex;
    private int overlayActivationMask = 0xf;
    private int overlayGrayscaleValue = 0xffff;
    private Color overlayColor;

    public ImageReadParam() {
        this.canSetSourceRenderSize = true;
    }

    public ImageReadParam(javax.imageio.ImageReadParam param) {
        this();
        this.sourceRegion = param.getSourceRegion();
        this.sourceRenderSize = param.getSourceRenderSize();
    }

    @Override
    public void setDestinationOffset(Point destinationOffset) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    @Override
    public void setController(IIOParamController controller) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    @Override
    public void setSourceBands(int[] sourceBands) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    @Override
    public void setSourceSubsampling(
            int sourceXSubsampling,
            int sourceYSubsampling,
            int subsamplingXOffset,
            int subsamplingYOffset) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    @Override
    public void setDestination(BufferedImage destination) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    @Override
    public void setDestinationBands(int[] destinationBands) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    @Override
    public void setDestinationType(ImageTypeSpecifier destinationType) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    @Override
    public void setSourceProgressivePasses(int minPass, int numPasses) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    public OptionalDouble getWindowCenter() {
        return Builder.getOptionalDouble(windowCenter);
    }

    /**
     * @param windowCenter the center of window DICOM values.
     */
    public void setWindowCenter(Double windowCenter) {
        this.windowCenter = windowCenter;
    }

    public OptionalDouble getWindowWidth() {
        return Builder.getOptionalDouble(windowWidth);
    }

    /**
     * @param windowWidth the width from low to high input DICOM values around level.
     */
    public void setWindowWidth(Double windowWidth) {
        this.windowWidth = windowWidth;
    }

    public OptionalDouble getLevelMin() {
        return Builder.getOptionalDouble(levelMin);
    }

    /**
     * @param levelMin the min DICOM value in the image.
     */
    public void setLevelMin(Double levelMin) {
        this.levelMin = levelMin;
    }

    public OptionalDouble getLevelMax() {
        return Builder.getOptionalDouble(levelMax);
    }

    /**
     * @param levelMax the max DICOM value in the image.
     */
    public void setLevelMax(Double levelMax) {
        this.levelMax = levelMax;
    }

    public Optional<LutShape> getVoiLutShape() {
        return Optional.ofNullable(voiLutShape);
    }

    public void setVoiLutShape(LutShape voiLutShape) {
        this.voiLutShape = voiLutShape;
    }

    public Optional<Boolean> getApplyPixelPadding() {
        return Optional.ofNullable(applyPixelPadding);
    }

    public void setApplyPixelPadding(Boolean applyPixelPadding) {
        this.applyPixelPadding = applyPixelPadding;
    }

    public Optional<Boolean> getInverseLut() {
        return Optional.ofNullable(inverseLut);
    }

    public void setInverseLut(Boolean inverseLut) {
        this.inverseLut = inverseLut;
    }

    public Optional<Boolean> getReleaseImageAfterProcessing() {
        return Optional.ofNullable(releaseImageAfterProcessing);
    }

    public void setReleaseImageAfterProcessing(Boolean releaseImageAfterProcessing) {
        this.releaseImageAfterProcessing = releaseImageAfterProcessing;
    }

    public Optional<Boolean> getFillOutsideLutRange() {
        return Optional.ofNullable(fillOutsideLutRange);
    }

    public void setFillOutsideLutRange(Boolean fillOutsideLutRange) {
        this.fillOutsideLutRange = fillOutsideLutRange;
    }

    public Optional<Boolean> getApplyWindowLevelToColorImage() {
        return Optional.ofNullable(applyWindowLevelToColorImage);
    }

    public void setApplyWindowLevelToColorImage(Boolean applyWindowLevelToColorImage) {
        this.applyWindowLevelToColorImage = applyWindowLevelToColorImage;
    }

    public Optional<Boolean> getKeepRgbForLossyJpeg() {
        return Optional.ofNullable(keepRgbForLossyJpeg);
    }

    public void setKeepRgbForLossyJpeg(Boolean keepRgbForLossyJpeg) {
        this.keepRgbForLossyJpeg = keepRgbForLossyJpeg;
    }

    public int getVoiLUTIndex() {
        return voiLUTIndex;
    }

    public void setVoiLUTIndex(int voiLUTIndex) {
        this.voiLUTIndex = Math.max(voiLUTIndex, 0);
    }

    public int getWindowIndex() {
        return windowIndex;
    }

    public void setWindowIndex(int windowIndex) {
        this.windowIndex = Math.max(windowIndex, 0);
    }

    public Optional<PresentationLutObject> getPresentationState() {
        return Optional.ofNullable(presentationState);
    }

    public void setPresentationState(PresentationLutObject presentationState) {
        this.presentationState = presentationState;
    }

    public int getOverlayActivationMask() {
        return overlayActivationMask;
    }

    public void setOverlayActivationMask(int overlayActivationMask) {
        this.overlayActivationMask = overlayActivationMask;
    }

    public int getOverlayGrayscaleValue() {
        return overlayGrayscaleValue;
    }

    public void setOverlayGrayscaleValue(int overlayGrayscaleValue) {
        this.overlayGrayscaleValue = overlayGrayscaleValue;
    }

    public Optional<Color> getOverlayColor() {
        return Optional.ofNullable(overlayColor);
    }

    public void setOverlayColor(Color overlayColor) {
        this.overlayColor = overlayColor;
    }

}
