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
package org.miaixz.bus.image.nimble;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.nimble.opencv.LookupTableCV;
import org.miaixz.bus.image.nimble.opencv.lut.ModalityLutModule;
import org.miaixz.bus.image.nimble.opencv.lut.PresentationStateLut;
import org.miaixz.bus.image.nimble.opencv.lut.VoiLutModule;
import org.miaixz.bus.image.nimble.stream.ImageDescriptor;

import java.awt.*;
import java.awt.geom.Area;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PresentationLutObject implements PresentationStateLut {

    private final Attributes dcmPR;
    private final ModalityLutModule modalityLUT;
    private final List<OverlayData> overlays;
    private final List<OverlayData> shutterOverlays;
    private final Optional<VoiLutModule> voiLUT;
    private final Optional<LookupTableCV> prLut;
    private final Optional<String> prLutExplanation;
    private final Optional<String> prLUTShapeMode;

    public PresentationLutObject(Attributes dcmPR) {
        this(dcmPR, null);
    }

    public PresentationLutObject(Attributes dcmPR, ImageDescriptor desc) {
        this.dcmPR = Objects.requireNonNull(dcmPR);
        // TODO handle sopclassUID
        // http://dicom.nema.org/medical/dicom/current/output/chtml/part04/sect_B.5.html
        // http://dicom.nema.org/medical/dicom/current/output/chtml/part03/sect_A.33.2.3.html
        // http://dicom.nema.org/medical/dicom/current/output/chtml/part03/sect_A.33.3.3.html
        // http://dicom.nema.org/medical/dicom/current/output/chtml/part03/sect_A.33.4.3.html
        // http://dicom.nema.org/medical/dicom/current/output/chtml/part03/sect_A.33.6.3.html
        if (!dcmPR.getString(Tag.SOPClassUID, "").startsWith("1.2.840.10008.5.1.4.1.1.11.")) {
            throw new IllegalStateException("SOPClassUID does not match to a DICOM Presentation State");
        }
        this.modalityLUT = desc == null ? new ModalityLutModule(dcmPR) : desc.getModalityLUT();
        this.voiLUT = buildVoiLut(dcmPR);
        this.overlays = OverlayData.getPrOverlayData(dcmPR, -1);
        this.shutterOverlays = desc == null ? OverlayData.getOverlayData(dcmPR, 0xffff) : desc.getOverlayData();
        // Implement graphics
        // http://dicom.nema.org/medical/dicom/current/output/chtml/part03/sect_A.33.2.3.html
        // Implement mask module
        // http://dicom.nema.org/medical/dicom/current/output/chtml/part03/sect_C.11.13.html

        Attributes dcmLut = dcmPR.getNestedDataset(Tag.PresentationLUTSequence);
        if (dcmLut != null) {
            /*
             * @see <a href="http://dicom.nema.org/medical/Dicom/current/output/chtml/part03/sect_C.11.6.html">C.11.6
             * Softcopy Presentation LUT Module</a> <p>Presentation LUT Module is always implicitly specified to apply
             * over the full range of output of the preceding transformation, and it never selects a subset or superset
             * of the that range (unlike the VOI LUT).
             */
            this.prLut = RGBImageVoiLut.createLut(dcmLut);
            this.prLutExplanation = Optional.ofNullable(dcmPR.getString(Tag.LUTExplanation));
            this.prLUTShapeMode = Optional.of("IDENTITY");
        } else {
            // value: INVERSE, IDENTITY
            // INVERSE => must inverse values (same as monochrome 1)
            this.prLUTShapeMode = Optional.ofNullable(dcmPR.getString(Tag.PresentationLUTShape));
            this.prLut = Optional.empty();
            this.prLutExplanation = Optional.empty();
        }
    }

    private static Optional<VoiLutModule> buildVoiLut(Attributes dcmPR) {
        Attributes seqDcm = dcmPR.getNestedDataset(Tag.SoftcopyVOILUTSequence);
        return seqDcm == null ? Optional.empty() : Optional.of(new VoiLutModule(seqDcm));
    }

    public static PresentationLutObject getPresentationState(String prPath) throws IOException {
        try (ImageInputStream dis = new ImageInputStream(new FileInputStream(prPath))) {
            return new PresentationLutObject(dis.readDataset());
        }
    }

    public Attributes getDicomObject() {
        return dcmPR;
    }

    public LocalDateTime getPresentationCreationDateTime() {
        return Builder.dateTime(dcmPR, Tag.PresentationCreationDate, Tag.PresentationCreationTime);
    }

    @Override
    public Optional<LookupTableCV> getPrLut() {
        return prLut;
    }

    @Override
    public Optional<String> getPrLutExplanation() {
        return prLutExplanation;
    }

    @Override
    public Optional<String> getPrLutShapeMode() {
        return prLUTShapeMode;
    }

    public ModalityLutModule getModalityLutModule() {
        return modalityLUT;
    }

    public Optional<VoiLutModule> getVoiLUT() {
        return voiLUT;
    }

    public List<OverlayData> getOverlays() {
        return overlays;
    }

    public List<OverlayData> getShutterOverlays() {
        return shutterOverlays;
    }

    public String getPrContentLabel() {
        return dcmPR.getString(Tag.ContentLabel, "PR " + dcmPR.getInt(Tag.InstanceNumber, 0));
    }

    public boolean hasOverlay() {
        return !overlays.isEmpty();
    }

    public List<Attributes> getReferencedSeriesSequence() {
        return Builder.getSequence(dcmPR, Tag.ReferencedSeriesSequence);
    }

    public List<Attributes> getGraphicAnnotationSequence() {
        return Builder.getSequence(dcmPR, Tag.GraphicAnnotationSequence);
    }

    public List<Attributes> getGraphicLayerSequence() {
        return Builder.getSequence(dcmPR, Tag.GraphicLayerSequence);
    }

    public Area getShutterShape() {
        return Builder.getShutterShape(dcmPR);
    }

    public Color getShutterColor() {
        return Builder.getShutterColor(dcmPR);
    }

    public boolean isImageFrameApplicable(String seriesInstanceUID, String sopInstanceUID, int frame) {
        return isImageFrameApplicable(Tag.ReferencedFrameNumber, seriesInstanceUID, sopInstanceUID, frame);
    }

    public boolean isSegmentationSegmentApplicable(String seriesInstanceUID, String sopInstanceUID, int segment) {
        return isImageFrameApplicable(Tag.ReferencedSegmentNumber, seriesInstanceUID, sopInstanceUID, segment);
    }

    private boolean isImageFrameApplicable(int childTag, String seriesInstanceUID, String sopInstanceUID, int frame) {
        if (StringKit.hasText(seriesInstanceUID)) {
            for (Attributes refSeriesSeq : getReferencedSeriesSequence()) {
                if (seriesInstanceUID.equals(refSeriesSeq.getString(Tag.SeriesInstanceUID))) {
                    List<Attributes> refImgSeq = Builder.getSequence(Objects.requireNonNull(refSeriesSeq),
                            Tag.ReferencedImageSequence);
                    return Builder.isImageFrameApplicableToReferencedImageSequence(refImgSeq, childTag, sopInstanceUID,
                            frame, true);
                }
            }
        }
        return false;
    }

}
