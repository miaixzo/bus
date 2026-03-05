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

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.nimble.EmbeddedOverlay;
import org.miaixz.bus.image.nimble.OverlayData;
import org.miaixz.bus.image.nimble.Photometric;
import org.miaixz.bus.image.nimble.opencv.lut.ModalityLutModule;
import org.miaixz.bus.image.nimble.opencv.lut.VoiLutModule;
import org.miaixz.bus.logger.Logger;
import org.opencv.core.Core;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public final class ImageDescriptor {

    private final int rows;
    private final int columns;
    private final int samples;
    private final Photometric photometric;
    private final int bitsAllocated;
    private final int bitsStored;
    private final int bitsCompressed;
    private final int pixelRepresentation;
    private final String sopClassUID;
    private final int frames;
    private final List<EmbeddedOverlay> embeddedOverlay;
    private final List<OverlayData> overlayData;
    private final int planarConfiguration;
    private final String presentationLUTShape;
    private final String modality;
    private final Integer pixelPaddingValue;
    private final Integer pixelPaddingRangeLimit;
    private final ModalityLutModule modalityLUT;
    private final VoiLutModule voiLUT;
    private final int highBit;
    private final String stationName;
    private final String pixelPresentation;
    private final String seriesInstanceUID;
    private final List<Core.MinMaxLocResult> minMaxPixelValues;
    private final List<VoiLutModule> voiLutPerFrame;
    private final List<ModalityLutModule> modalityLutPerFrame;

    public ImageDescriptor(Attributes dcm) {
        this(dcm, 0);
    }

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

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getSamples() {
        return samples;
    }

    public Photometric getPhotometricInterpretation() {
        return photometric;
    }

    public int getBitsAllocated() {
        return bitsAllocated;
    }

    public int getBitsStored() {
        return bitsStored;
    }

    public int getBitsCompressed() {
        return bitsCompressed;
    }

    public String getPixelPresentation() {
        return pixelPresentation;
    }

    public boolean hasPaletteColorLookupTable() {
        return photometric == Photometric.PALETTE_COLOR || "COLOR".equals(pixelPresentation);
    }

    public int getPixelRepresentation() {
        return pixelRepresentation;
    }

    public int getPlanarConfiguration() {
        return planarConfiguration;
    }

    public String getSopClassUID() {
        return sopClassUID;
    }

    public String getStationName() {
        return stationName;
    }

    public int getFrames() {
        return frames;
    }

    public boolean isMultiframe() {
        return frames > 1;
    }

    public int getFrameLength() {
        return rows * columns * samples * bitsAllocated / 8;
    }

    public int getLength() {
        return getFrameLength() * frames;
    }

    public boolean isSigned() {
        return pixelRepresentation != 0;
    }

    public boolean isBanded() {
        return planarConfiguration != 0;
    }

    public List<EmbeddedOverlay> getEmbeddedOverlay() {
        return embeddedOverlay;
    }

    public boolean isMultiframeWithEmbeddedOverlays() {
        return !embeddedOverlay.isEmpty() && frames > 1;
    }

    public String getPresentationLUTShape() {
        return presentationLUTShape;
    }

    public String getModality() {
        return modality;
    }

    public Integer getPixelPaddingValue() {
        return pixelPaddingValue;
    }

    public Integer getPixelPaddingRangeLimit() {
        return pixelPaddingRangeLimit;
    }

    public ModalityLutModule getModalityLUT() {
        return modalityLUT;
    }

    public VoiLutModule getVoiLUT() {
        return voiLUT;
    }

    public boolean isFloatPixelData() {
        return (bitsAllocated == 32 && !"RTDOSE".equals(modality)) || bitsAllocated == 64;
    }

    public int getHighBit() {
        return highBit;
    }

    public List<OverlayData> getOverlayData() {
        return overlayData;
    }

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    public Core.MinMaxLocResult getMinMaxPixelValue(int frame) {
        if (frame < 0 || frame >= minMaxPixelValues.size()) {
            Logger.error("Invalid frame index: {}", frame);
            return null;
        }
        return minMaxPixelValues.get(frame);
    }

    public void setMinMaxPixelValue(int frame, Core.MinMaxLocResult minMaxPixelValue) {
        if (frame < 0 || frame >= minMaxPixelValues.size()) {
            Logger.error("Unable to set MinMaxPixelValue for invalid frame index: {}", frame);
            return;
        }
        minMaxPixelValues.set(frame, minMaxPixelValue);
    }

    public VoiLutModule getVoiLutForFrame(int frame) {
        return getLutModule(voiLutPerFrame, voiLUT, frame);
    }

    private static <T> T getLutModule(List<T> list, T baseLut, int frame) {
        if (frame < 0 || frame >= list.size()) {
            if (frame != 0) {
                Logger.error("Invalid frame index for LUT: {}", frame);
            }
            return baseLut;
        }
        return list.get(frame) != null ? list.get(frame) : baseLut;
    }

    public void setVoiLutForFrame(int frame, VoiLutModule voiLut) {
        if (frame < 0 || frame >= voiLutPerFrame.size()) {
            Logger.error("Unable to set VoiLutModule for invalid frame index: {}", frame);
            return;
        }
        voiLutPerFrame.set(frame, voiLut);
    }

    public ModalityLutModule getModalityLutForFrame(int frame) {
        return getLutModule(modalityLutPerFrame, modalityLUT, frame);
    }

    public void setModalityLutForFrame(int frame, ModalityLutModule modalityLut) {
        if (frame < 0 || frame >= modalityLutPerFrame.size()) {
            Logger.error("Unable to set ModalityLutModule for invalid frame index: {}", frame);
            return;
        }
        modalityLutPerFrame.set(frame, modalityLut);
    }

}
