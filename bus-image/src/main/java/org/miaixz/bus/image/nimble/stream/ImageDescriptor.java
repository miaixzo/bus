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
package org.miaixz.bus.image.nimble.stream;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.nimble.EmbeddedOverlay;
import org.miaixz.bus.image.nimble.OverlayData;
import org.miaixz.bus.image.nimble.Photometric;
import org.miaixz.bus.image.nimble.opencv.lut.ModalityLutModule;
import org.miaixz.bus.image.nimble.opencv.lut.VoiLutModule;
import org.miaixz.bus.logger.Logger;

/**
 * Represents the ImageDescriptor type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ImageDescriptor {

    /**
     * The rows value.
     */
    private final int rows;

    /**
     * The columns value.
     */
    private final int columns;

    /**
     * The samples value.
     */
    private final int samples;

    /**
     * The photometric value.
     */
    private final Photometric photometric;

    /**
     * The bits allocated value.
     */
    private final int bitsAllocated;

    /**
     * The bits stored value.
     */
    private final int bitsStored;

    /**
     * The bits compressed value.
     */
    private final int bitsCompressed;

    /**
     * The pixel representation value.
     */
    private final int pixelRepresentation;

    /**
     * The sop class uid value.
     */
    private final String sopClassUID;

    /**
     * The frames value.
     */
    private final int frames;

    /**
     * The embedded overlay value.
     */
    private final List<EmbeddedOverlay> embeddedOverlay;

    /**
     * The overlay data value.
     */
    private final List<OverlayData> overlayData;

    /**
     * The planar configuration value.
     */
    private final int planarConfiguration;

    /**
     * The presentation lut shape value.
     */
    private final String presentationLUTShape;

    /**
     * The modality value.
     */
    private final String modality;

    /**
     * The pixel padding value value.
     */
    private final Integer pixelPaddingValue;

    /**
     * The pixel padding range limit value.
     */
    private final Integer pixelPaddingRangeLimit;

    /**
     * The modality lut value.
     */
    private final ModalityLutModule modalityLUT;

    /**
     * The voi lut value.
     */
    private final VoiLutModule voiLUT;

    /**
     * The high bit value.
     */
    private final int highBit;

    /**
     * The station name value.
     */
    private final String stationName;

    /**
     * The pixel presentation value.
     */
    private final String pixelPresentation;

    /**
     * The series instance uid value.
     */
    private final String seriesInstanceUID;

    /**
     * The min max pixel values value.
     */
    private final List<Core.MinMaxLocResult> minMaxPixelValues;

    /**
     * The voi lut per frame value.
     */
    private final List<VoiLutModule> voiLutPerFrame;

    /**
     * The modality lut per frame value.
     */
    private final List<ModalityLutModule> modalityLutPerFrame;

    /**
     * Creates a new instance.
     *
     * @param dcm the dcm.
     */
    public ImageDescriptor(Attributes dcm) {
        this(dcm, 0);
    }

    /**
     * Creates a new instance.
     *
     * @param dcm            the dcm.
     * @param bitsCompressed the bits compressed.
     */
    public ImageDescriptor(Attributes dcm, int bitsCompressed) {
        this.rows = dcm.getInt(Tag.Rows, 0);
        this.columns = dcm.getInt(Tag.Columns, 0);
        this.samples = dcm.getInt(Tag.SamplesPerPixel, 0);
        this.photometric = Photometric.fromString(dcm.getString(Tag.PhotometricInterpretation, "MONOCHROME2"));
        this.pixelPresentation = dcm.getString(Tag.PixelPresentation);
        this.bitsAllocated = Math.max(dcm.getInt(Tag.BitsAllocated, 8), 1);
        this.bitsStored = Math.min(Math.max(dcm.getInt(Tag.BitsStored, bitsAllocated), 1), bitsAllocated);
        this.highBit = dcm.getInt(Tag.HighBit, bitsStored - 1);
        this.bitsCompressed = bitsCompressed > 0 ? Math.min(bitsCompressed, bitsAllocated) : bitsStored;
        this.pixelRepresentation = dcm.getInt(Tag.PixelRepresentation, 0);
        this.planarConfiguration = dcm.getInt(Tag.PlanarConfiguration, 0);
        this.sopClassUID = dcm.getString(Tag.SOPClassUID);
        this.stationName = dcm.getString(Tag.StationName);
        this.frames = Math.max(dcm.getInt(Tag.NumberOfFrames, 1), 1);
        this.embeddedOverlay = EmbeddedOverlay.getEmbeddedOverlay(dcm);
        this.overlayData = OverlayData.getOverlayData(dcm, 0xffff);
        this.presentationLUTShape = dcm.getString(Tag.PresentationLUTShape);
        this.modality = dcm.getString(Tag.Modality);
        this.pixelPaddingValue = Builder.getIntegerFromDicomElement(dcm, Tag.PixelPaddingValue, null);
        this.pixelPaddingRangeLimit = Builder.getIntegerFromDicomElement(dcm, Tag.PixelPaddingRangeLimit, null);
        this.modalityLUT = new ModalityLutModule(dcm);
        this.voiLUT = new VoiLutModule(dcm);
        this.seriesInstanceUID = dcm.getString(Tag.SeriesInstanceUID);
        this.minMaxPixelValues = new ArrayList<>(frames);
        this.voiLutPerFrame = new ArrayList<>(frames);
        this.modalityLutPerFrame = new ArrayList<>(frames);
        for (int i = 0; i < frames; i++) {
            this.minMaxPixelValues.add(null);
            this.voiLutPerFrame.add(null);
            this.modalityLutPerFrame.add(null);
        }
    }

    /**
     * Gets the rows.
     *
     * @return the rows.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the columns.
     *
     * @return the columns.
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Gets the samples.
     *
     * @return the samples.
     */
    public int getSamples() {
        return samples;
    }

    /**
     * Gets the photometric interpretation.
     *
     * @return the photometric interpretation.
     */
    public Photometric getPhotometricInterpretation() {
        return photometric;
    }

    /**
     * Gets the bits allocated.
     *
     * @return the bits allocated.
     */
    public int getBitsAllocated() {
        return bitsAllocated;
    }

    /**
     * Gets the bits stored.
     *
     * @return the bits stored.
     */
    public int getBitsStored() {
        return bitsStored;
    }

    /**
     * Gets the bits compressed.
     *
     * @return the bits compressed.
     */
    public int getBitsCompressed() {
        return bitsCompressed;
    }

    /**
     * Gets the pixel presentation.
     *
     * @return the pixel presentation.
     */
    public String getPixelPresentation() {
        return pixelPresentation;
    }

    /**
     * Determines whether palette color lookup table.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean hasPaletteColorLookupTable() {
        return photometric == Photometric.PALETTE_COLOR || "COLOR".equals(pixelPresentation);
    }

    /**
     * Gets the pixel representation.
     *
     * @return the pixel representation.
     */
    public int getPixelRepresentation() {
        return pixelRepresentation;
    }

    /**
     * Gets the planar configuration.
     *
     * @return the planar configuration.
     */
    public int getPlanarConfiguration() {
        return planarConfiguration;
    }

    /**
     * Gets the sop class uid.
     *
     * @return the sop class uid.
     */
    public String getSopClassUID() {
        return sopClassUID;
    }

    /**
     * Gets the station name.
     *
     * @return the station name.
     */
    public String getStationName() {
        return stationName;
    }

    /**
     * Gets the frames.
     *
     * @return the frames.
     */
    public int getFrames() {
        return frames;
    }

    /**
     * Determines whether multiframe.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isMultiframe() {
        return frames > 1;
    }

    /**
     * Gets the frame length.
     *
     * @return the frame length.
     */
    public int getFrameLength() {
        return rows * columns * samples * bitsAllocated / 8;
    }

    /**
     * Gets the length.
     *
     * @return the length.
     */
    public int getLength() {
        return getFrameLength() * frames;
    }

    /**
     * Determines whether signed.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isSigned() {
        return pixelRepresentation != 0;
    }

    /**
     * Determines whether banded.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isBanded() {
        return planarConfiguration != 0;
    }

    /**
     * Gets the embedded overlay.
     *
     * @return the embedded overlay.
     */
    public List<EmbeddedOverlay> getEmbeddedOverlay() {
        return embeddedOverlay;
    }

    /**
     * Determines whether multiframe with embedded overlays.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isMultiframeWithEmbeddedOverlays() {
        return !embeddedOverlay.isEmpty() && frames > 1;
    }

    /**
     * Gets the presentation lut shape.
     *
     * @return the presentation lut shape.
     */
    public String getPresentationLUTShape() {
        return presentationLUTShape;
    }

    /**
     * Gets the modality.
     *
     * @return the modality.
     */
    public String getModality() {
        return modality;
    }

    /**
     * Gets the pixel padding value.
     *
     * @return the pixel padding value.
     */
    public Integer getPixelPaddingValue() {
        return pixelPaddingValue;
    }

    /**
     * Gets the pixel padding range limit.
     *
     * @return the pixel padding range limit.
     */
    public Integer getPixelPaddingRangeLimit() {
        return pixelPaddingRangeLimit;
    }

    /**
     * Gets the modality lut.
     *
     * @return the modality lut.
     */
    public ModalityLutModule getModalityLUT() {
        return modalityLUT;
    }

    /**
     * Gets the voi lut.
     *
     * @return the voi lut.
     */
    public VoiLutModule getVoiLUT() {
        return voiLUT;
    }

    /**
     * Determines whether float pixel data.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isFloatPixelData() {
        return (bitsAllocated == 32 && !"RTDOSE".equals(modality)) || bitsAllocated == 64;
    }

    /**
     * Gets the high bit.
     *
     * @return the high bit.
     */
    public int getHighBit() {
        return highBit;
    }

    /**
     * Gets the overlay data.
     *
     * @return the overlay data.
     */
    public List<OverlayData> getOverlayData() {
        return overlayData;
    }

    /**
     * Gets the series instance uid.
     *
     * @return the series instance uid.
     */
    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    /**
     * Gets the min max pixel value.
     *
     * @param frame the frame.
     * @return the min max pixel value.
     */
    public Core.MinMaxLocResult getMinMaxPixelValue(int frame) {
        if (frame < 0 || frame >= minMaxPixelValues.size()) {
            Logger.error(false, "Image", "Invalid frame index: frameIndex={}", frame);
            return null;
        }
        return minMaxPixelValues.get(frame);
    }

    /**
     * Sets the min max pixel value.
     *
     * @param frame            the frame.
     * @param minMaxPixelValue the min max pixel value.
     */
    public void setMinMaxPixelValue(int frame, Core.MinMaxLocResult minMaxPixelValue) {
        if (frame < 0 || frame >= minMaxPixelValues.size()) {
            Logger.error(
                    false,
                    "Image",
                    "Unable to set MinMaxPixelValue for invalid frame index: frameIndex={}",
                    frame);
            return;
        }
        minMaxPixelValues.set(frame, minMaxPixelValue);
    }

    /**
     * Gets the voi lut for frame.
     *
     * @param frame the frame.
     * @return the voi lut for frame.
     */
    public VoiLutModule getVoiLutForFrame(int frame) {
        return getLutModule(voiLutPerFrame, voiLUT, frame);
    }

    /**
     * Gets the lut module.
     *
     * @param list    the list.
     * @param baseLut the base lut.
     * @param frame   the frame.
     * @return the lut module.
     */
    private static <T> T getLutModule(List<T> list, T baseLut, int frame) {
        if (frame < 0 || frame >= list.size()) {
            if (frame != 0) {
                Logger.error(false, "Image", "Invalid frame index for LUT: frameIndex={}", frame);
            }
            return baseLut;
        }
        return list.get(frame) != null ? list.get(frame) : baseLut;
    }

    /**
     * Sets the voi lut for frame.
     *
     * @param frame  the frame.
     * @param voiLut the voi lut.
     */
    public void setVoiLutForFrame(int frame, VoiLutModule voiLut) {
        if (frame < 0 || frame >= voiLutPerFrame.size()) {
            Logger.error(false, "Image", "Unable to set VoiLutModule for invalid frame index: frameIndex={}", frame);
            return;
        }
        voiLutPerFrame.set(frame, voiLut);
    }

    /**
     * Gets the modality lut for frame.
     *
     * @param frame the frame.
     * @return the modality lut for frame.
     */
    public ModalityLutModule getModalityLutForFrame(int frame) {
        return getLutModule(modalityLutPerFrame, modalityLUT, frame);
    }

    /**
     * Sets the modality lut for frame.
     *
     * @param frame       the frame.
     * @param modalityLut the modality lut.
     */
    public void setModalityLutForFrame(int frame, ModalityLutModule modalityLut) {
        if (frame < 0 || frame >= modalityLutPerFrame.size()) {
            Logger.error(
                    false,
                    "Image",
                    "Unable to set ModalityLutModule for invalid frame index: frameIndex={}",
                    frame);
            return;
        }
        modalityLutPerFrame.set(frame, modalityLut);
    }

}
