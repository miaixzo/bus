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
 * Represents the ImageReadParam type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImageReadParam extends javax.imageio.ImageReadParam {

    /**
     * The not compatible value.
     */
    private static final String NOT_COMPATIBLE = "Not compatible with the native DICOM Decoder";

    /**
     * The window center value.
     */
    private Double windowCenter;

    /**
     * The window width value.
     */
    private Double windowWidth;

    /**
     * The level min value.
     */
    private Double levelMin;

    /**
     * The level max value.
     */
    private Double levelMax;

    /**
     * The voi lut shape value.
     */
    private LutShape voiLutShape;

    /**
     * The apply pixel padding value.
     */
    private Boolean applyPixelPadding;

    /**
     * The inverse lut value.
     */
    private Boolean inverseLut;

    /**
     * The fill outside lut range value.
     */
    private Boolean fillOutsideLutRange;

    /**
     * The apply window level to color image value.
     */
    private Boolean applyWindowLevelToColorImage;

    /**
     * The keep rgb for lossy jpeg value.
     */
    private Boolean keepRgbForLossyJpeg;

    /**
     * The release image after processing value.
     */
    private Boolean releaseImageAfterProcessing;

    /**
     * The presentation state value.
     */
    private PresentationLutObject presentationState;

    /**
     * The window index value.
     */
    private int windowIndex;

    /**
     * The voi lut index value.
     */
    private int voiLUTIndex;

    /**
     * The overlay activation mask value.
     */
    private int overlayActivationMask = 0xf;

    /**
     * The overlay grayscale value value.
     */
    private int overlayGrayscaleValue = 0xffff;

    /**
     * The overlay color value.
     */
    private Color overlayColor;

    /**
     * Creates a new instance.
     */
    public ImageReadParam() {
        this.canSetSourceRenderSize = true;
    }

    /**
     * Creates a new instance.
     *
     * @param param the param.
     */
    public ImageReadParam(javax.imageio.ImageReadParam param) {
        this();
        this.sourceRegion = param.getSourceRegion();
        this.sourceRenderSize = param.getSourceRenderSize();
    }

    /**
     * Sets the destination offset.
     *
     * @param destinationOffset the destination offset.
     */
    @Override
    public void setDestinationOffset(Point destinationOffset) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    /**
     * Sets the controller.
     *
     * @param controller the controller.
     */
    @Override
    public void setController(IIOParamController controller) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    /**
     * Sets the source bands.
     *
     * @param sourceBands the source bands.
     */
    @Override
    public void setSourceBands(int[] sourceBands) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    /**
     * Sets the source subsampling.
     *
     * @param sourceXSubsampling the source x subsampling.
     * @param sourceYSubsampling the source y subsampling.
     * @param subsamplingXOffset the subsampling x offset.
     * @param subsamplingYOffset the subsampling y offset.
     */
    @Override
    public void setSourceSubsampling(
            int sourceXSubsampling,
            int sourceYSubsampling,
            int subsamplingXOffset,
            int subsamplingYOffset) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    /**
     * Sets the destination.
     *
     * @param destination the destination.
     */
    @Override
    public void setDestination(BufferedImage destination) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    /**
     * Sets the destination bands.
     *
     * @param destinationBands the destination bands.
     */
    @Override
    public void setDestinationBands(int[] destinationBands) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    /**
     * Sets the destination type.
     *
     * @param destinationType the destination type.
     */
    @Override
    public void setDestinationType(ImageTypeSpecifier destinationType) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    /**
     * Sets the source progressive passes.
     *
     * @param minPass   the min pass.
     * @param numPasses the num passes.
     */
    @Override
    public void setSourceProgressivePasses(int minPass, int numPasses) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    /**
     * Gets the window center.
     *
     * @return the window center.
     */
    public OptionalDouble getWindowCenter() {
        return Builder.getOptionalDouble(windowCenter);
    }

    /**
     * @param windowCenter the center of window DICOM values.
     */
    public void setWindowCenter(Double windowCenter) {
        this.windowCenter = windowCenter;
    }

    /**
     * Gets the window width.
     *
     * @return the window width.
     */
    public OptionalDouble getWindowWidth() {
        return Builder.getOptionalDouble(windowWidth);
    }

    /**
     * @param windowWidth the width from low to high input DICOM values around level.
     */
    public void setWindowWidth(Double windowWidth) {
        this.windowWidth = windowWidth;
    }

    /**
     * Gets the level min.
     *
     * @return the level min.
     */
    public OptionalDouble getLevelMin() {
        return Builder.getOptionalDouble(levelMin);
    }

    /**
     * @param levelMin the min DICOM value in the image.
     */
    public void setLevelMin(Double levelMin) {
        this.levelMin = levelMin;
    }

    /**
     * Gets the level max.
     *
     * @return the level max.
     */
    public OptionalDouble getLevelMax() {
        return Builder.getOptionalDouble(levelMax);
    }

    /**
     * @param levelMax the max DICOM value in the image.
     */
    public void setLevelMax(Double levelMax) {
        this.levelMax = levelMax;
    }

    /**
     * Gets the voi lut shape.
     *
     * @return the voi lut shape.
     */
    public Optional<LutShape> getVoiLutShape() {
        return Optional.ofNullable(voiLutShape);
    }

    /**
     * Sets the voi lut shape.
     *
     * @param voiLutShape the voi lut shape.
     */
    public void setVoiLutShape(LutShape voiLutShape) {
        this.voiLutShape = voiLutShape;
    }

    /**
     * Gets the apply pixel padding.
     *
     * @return the apply pixel padding.
     */
    public Optional<Boolean> getApplyPixelPadding() {
        return Optional.ofNullable(applyPixelPadding);
    }

    /**
     * Sets the apply pixel padding.
     *
     * @param applyPixelPadding the apply pixel padding.
     */
    public void setApplyPixelPadding(Boolean applyPixelPadding) {
        this.applyPixelPadding = applyPixelPadding;
    }

    /**
     * Gets the inverse lut.
     *
     * @return the inverse lut.
     */
    public Optional<Boolean> getInverseLut() {
        return Optional.ofNullable(inverseLut);
    }

    /**
     * Sets the inverse lut.
     *
     * @param inverseLut the inverse lut.
     */
    public void setInverseLut(Boolean inverseLut) {
        this.inverseLut = inverseLut;
    }

    /**
     * Gets the release image after processing.
     *
     * @return the release image after processing.
     */
    public Optional<Boolean> getReleaseImageAfterProcessing() {
        return Optional.ofNullable(releaseImageAfterProcessing);
    }

    /**
     * Sets the release image after processing.
     *
     * @param releaseImageAfterProcessing the release image after processing.
     */
    public void setReleaseImageAfterProcessing(Boolean releaseImageAfterProcessing) {
        this.releaseImageAfterProcessing = releaseImageAfterProcessing;
    }

    /**
     * Gets the fill outside lut range.
     *
     * @return the fill outside lut range.
     */
    public Optional<Boolean> getFillOutsideLutRange() {
        return Optional.ofNullable(fillOutsideLutRange);
    }

    /**
     * Sets the fill outside lut range.
     *
     * @param fillOutsideLutRange the fill outside lut range.
     */
    public void setFillOutsideLutRange(Boolean fillOutsideLutRange) {
        this.fillOutsideLutRange = fillOutsideLutRange;
    }

    /**
     * Gets the apply window level to color image.
     *
     * @return the apply window level to color image.
     */
    public Optional<Boolean> getApplyWindowLevelToColorImage() {
        return Optional.ofNullable(applyWindowLevelToColorImage);
    }

    /**
     * Sets the apply window level to color image.
     *
     * @param applyWindowLevelToColorImage the apply window level to color image.
     */
    public void setApplyWindowLevelToColorImage(Boolean applyWindowLevelToColorImage) {
        this.applyWindowLevelToColorImage = applyWindowLevelToColorImage;
    }

    /**
     * Gets the keep rgb for lossy jpeg.
     *
     * @return the keep rgb for lossy jpeg.
     */
    public Optional<Boolean> getKeepRgbForLossyJpeg() {
        return Optional.ofNullable(keepRgbForLossyJpeg);
    }

    /**
     * Sets the keep rgb for lossy jpeg.
     *
     * @param keepRgbForLossyJpeg the keep rgb for lossy jpeg.
     */
    public void setKeepRgbForLossyJpeg(Boolean keepRgbForLossyJpeg) {
        this.keepRgbForLossyJpeg = keepRgbForLossyJpeg;
    }

    /**
     * Gets the voi lut index.
     *
     * @return the voi lut index.
     */
    public int getVoiLUTIndex() {
        return voiLUTIndex;
    }

    /**
     * Sets the voi lut index.
     *
     * @param voiLUTIndex the voi lut index.
     */
    public void setVoiLUTIndex(int voiLUTIndex) {
        this.voiLUTIndex = Math.max(voiLUTIndex, 0);
    }

    /**
     * Gets the window index.
     *
     * @return the window index.
     */
    public int getWindowIndex() {
        return windowIndex;
    }

    /**
     * Sets the window index.
     *
     * @param windowIndex the window index.
     */
    public void setWindowIndex(int windowIndex) {
        this.windowIndex = Math.max(windowIndex, 0);
    }

    /**
     * Gets the presentation state.
     *
     * @return the presentation state.
     */
    public Optional<PresentationLutObject> getPresentationState() {
        return Optional.ofNullable(presentationState);
    }

    /**
     * Sets the presentation state.
     *
     * @param presentationState the presentation state.
     */
    public void setPresentationState(PresentationLutObject presentationState) {
        this.presentationState = presentationState;
    }

    /**
     * Gets the overlay activation mask.
     *
     * @return the overlay activation mask.
     */
    public int getOverlayActivationMask() {
        return overlayActivationMask;
    }

    /**
     * Sets the overlay activation mask.
     *
     * @param overlayActivationMask the overlay activation mask.
     */
    public void setOverlayActivationMask(int overlayActivationMask) {
        this.overlayActivationMask = overlayActivationMask;
    }

    /**
     * Gets the overlay grayscale value.
     *
     * @return the overlay grayscale value.
     */
    public int getOverlayGrayscaleValue() {
        return overlayGrayscaleValue;
    }

    /**
     * Sets the overlay grayscale value.
     *
     * @param overlayGrayscaleValue the overlay grayscale value.
     */
    public void setOverlayGrayscaleValue(int overlayGrayscaleValue) {
        this.overlayGrayscaleValue = overlayGrayscaleValue;
    }

    /**
     * Gets the overlay color.
     *
     * @return the overlay color.
     */
    public Optional<Color> getOverlayColor() {
        return Optional.ofNullable(overlayColor);
    }

    /**
     * Sets the overlay color.
     *
     * @param overlayColor the overlay color.
     */
    public void setOverlayColor(Color overlayColor) {
        this.overlayColor = overlayColor;
    }

}
