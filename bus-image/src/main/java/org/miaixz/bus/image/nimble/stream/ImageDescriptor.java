/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.nimble.stream;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.nimble.EmbeddedOverlay;
import org.miaixz.bus.image.nimble.OverlayData;
import org.miaixz.bus.image.nimble.Photometric;
import org.miaixz.bus.image.nimble.opencv.lut.ModalityLutModule;
import org.miaixz.bus.image.nimble.opencv.lut.VoiLutModule;

import java.util.List;

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

    public ImageDescriptor(Attributes dcm) {
        this(dcm, 0);
    }

    public ImageDescriptor(Attributes dcm, int bitsCompressed) {
        this.rows = dcm.getInt(Tag.Rows, 0);
        this.columns = dcm.getInt(Tag.Columns, 0);
        this.samples = dcm.getInt(Tag.SamplesPerPixel, 0);
        this.photometric =
                Photometric.fromString(
                        dcm.getString(Tag.PhotometricInterpretation, "MONOCHROME2"));
        this.pixelPresentation = dcm.getString(Tag.PixelPresentation);
        this.bitsAllocated = dcm.getInt(Tag.BitsAllocated, 8);
        this.bitsStored = dcm.getInt(Tag.BitsStored, bitsAllocated);
        this.highBit = dcm.getInt(Tag.HighBit, bitsStored - 1);
        this.bitsCompressed = bitsCompressed > 0 ? Math.min(bitsCompressed, bitsAllocated) : bitsStored;
        this.pixelRepresentation = dcm.getInt(Tag.PixelRepresentation, 0);
        this.planarConfiguration = dcm.getInt(Tag.PlanarConfiguration, 0);
        this.sopClassUID = dcm.getString(Tag.SOPClassUID);
        this.stationName = dcm.getString(Tag.StationName);
        this.frames = dcm.getInt(Tag.NumberOfFrames, 1);
        this.embeddedOverlay = EmbeddedOverlay.getEmbeddedOverlay(dcm);
        this.overlayData = OverlayData.getOverlayData(dcm, 0xffff);
        this.presentationLUTShape = dcm.getString(Tag.PresentationLUTShape);
        this.modality = dcm.getString(Tag.Modality);
        this.pixelPaddingValue =
                Builder.getIntegerFromDicomElement(dcm, Tag.PixelPaddingValue, null);
        this.pixelPaddingRangeLimit =
                Builder.getIntegerFromDicomElement(dcm, Tag.PixelPaddingRangeLimit, null);
        this.modalityLUT = new ModalityLutModule(dcm);
        this.voiLUT = new VoiLutModule(dcm);
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
        return photometric == Photometric.PALETTE_COLOR
                || "COLOR".equals(pixelPresentation);
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

}
