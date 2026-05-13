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
import java.awt.geom.Area;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

/**
 * Represents the PresentationLutObject type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PresentationLutObject implements PresentationStateLut {

    /**
     * The dcm pr value.
     */
    private final Attributes dcmPR;

    /**
     * The modality lut value.
     */
    private final ModalityLutModule modalityLUT;

    /**
     * The overlays value.
     */
    private final List<OverlayData> overlays;

    /**
     * The shutter overlays value.
     */
    private final List<OverlayData> shutterOverlays;

    /**
     * The voi lut value.
     */
    private final Optional<VoiLutModule> voiLUT;

    /**
     * The pr lut value.
     */
    private final Optional<LookupTableCV> prLut;

    /**
     * The pr lut explanation value.
     */
    private final Optional<String> prLutExplanation;

    /**
     * The pr lut shape mode value.
     */
    private final Optional<String> prLUTShapeMode;

    /**
     * Creates a new instance.
     *
     * @param dcmPR the dcm pr.
     */
    public PresentationLutObject(Attributes dcmPR) {
        this(dcmPR, null);
    }

    /**
     * Creates a new instance.
     *
     * @param dcmPR the dcm pr.
     * @param desc  the desc.
     */
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

    /**
     * Builds the voi lut.
     *
     * @param dcmPR the dcm pr.
     * @return the operation result.
     */
    private static Optional<VoiLutModule> buildVoiLut(Attributes dcmPR) {
        Attributes seqDcm = dcmPR.getNestedDataset(Tag.SoftcopyVOILUTSequence);
        return seqDcm == null ? Optional.empty() : Optional.of(new VoiLutModule(seqDcm));
    }

    /**
     * Gets the presentation state.
     *
     * @param prPath the pr path.
     * @return the presentation state.
     * @throws IOException if the operation cannot be completed.
     */
    public static PresentationLutObject getPresentationState(String prPath) throws IOException {
        try (ImageInputStream dis = new ImageInputStream(new FileInputStream(prPath))) {
            return new PresentationLutObject(dis.readDataset());
        }
    }

    /**
     * Gets the dicom object.
     *
     * @return the dicom object.
     */
    public Attributes getDicomObject() {
        return dcmPR;
    }

    /**
     * Gets the presentation creation date time.
     *
     * @return the presentation creation date time.
     */
    public LocalDateTime getPresentationCreationDateTime() {
        return Builder.dateTime(dcmPR, Tag.PresentationCreationDate, Tag.PresentationCreationTime);
    }

    /**
     * Gets the pr lut.
     *
     * @return the pr lut.
     */
    @Override
    public Optional<LookupTableCV> getPrLut() {
        return prLut;
    }

    /**
     * Gets the pr lut explanation.
     *
     * @return the pr lut explanation.
     */
    @Override
    public Optional<String> getPrLutExplanation() {
        return prLutExplanation;
    }

    /**
     * Gets the pr lut shape mode.
     *
     * @return the pr lut shape mode.
     */
    @Override
    public Optional<String> getPrLutShapeMode() {
        return prLUTShapeMode;
    }

    /**
     * Gets the modality lut module.
     *
     * @return the modality lut module.
     */
    public ModalityLutModule getModalityLutModule() {
        return modalityLUT;
    }

    /**
     * Gets the voi lut.
     *
     * @return the voi lut.
     */
    public Optional<VoiLutModule> getVoiLUT() {
        return voiLUT;
    }

    /**
     * Gets the overlays.
     *
     * @return the overlays.
     */
    public List<OverlayData> getOverlays() {
        return overlays;
    }

    /**
     * Gets the shutter overlays.
     *
     * @return the shutter overlays.
     */
    public List<OverlayData> getShutterOverlays() {
        return shutterOverlays;
    }

    /**
     * Gets the pr content label.
     *
     * @return the pr content label.
     */
    public String getPrContentLabel() {
        return dcmPR.getString(Tag.ContentLabel, "PR " + dcmPR.getInt(Tag.InstanceNumber, 0));
    }

    /**
     * Determines whether overlay.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean hasOverlay() {
        return !overlays.isEmpty();
    }

    /**
     * Gets the referenced series sequence.
     *
     * @return the referenced series sequence.
     */
    public List<Attributes> getReferencedSeriesSequence() {
        return Builder.getSequence(dcmPR, Tag.ReferencedSeriesSequence);
    }

    /**
     * Gets the graphic annotation sequence.
     *
     * @return the graphic annotation sequence.
     */
    public List<Attributes> getGraphicAnnotationSequence() {
        return Builder.getSequence(dcmPR, Tag.GraphicAnnotationSequence);
    }

    /**
     * Gets the graphic layer sequence.
     *
     * @return the graphic layer sequence.
     */
    public List<Attributes> getGraphicLayerSequence() {
        return Builder.getSequence(dcmPR, Tag.GraphicLayerSequence);
    }

    /**
     * Gets the shutter shape.
     *
     * @return the shutter shape.
     */
    public Area getShutterShape() {
        return Builder.getShutterShape(dcmPR);
    }

    /**
     * Gets the shutter color.
     *
     * @return the shutter color.
     */
    public Color getShutterColor() {
        return Builder.getShutterColor(dcmPR);
    }

    /**
     * Determines whether image frame applicable.
     *
     * @param seriesInstanceUID the series instance uid.
     * @param sopInstanceUID    the sop instance uid.
     * @param frame             the frame.
     * @return true if the condition is met; otherwise false.
     */
    public boolean isImageFrameApplicable(String seriesInstanceUID, String sopInstanceUID, int frame) {
        return isImageFrameApplicable(Tag.ReferencedFrameNumber, seriesInstanceUID, sopInstanceUID, frame);
    }

    /**
     * Determines whether segmentation segment applicable.
     *
     * @param seriesInstanceUID the series instance uid.
     * @param sopInstanceUID    the sop instance uid.
     * @param segment           the segment.
     * @return true if the condition is met; otherwise false.
     */
    public boolean isSegmentationSegmentApplicable(String seriesInstanceUID, String sopInstanceUID, int segment) {
        return isImageFrameApplicable(Tag.ReferencedSegmentNumber, seriesInstanceUID, sopInstanceUID, segment);
    }

    /**
     * Determines whether image frame applicable.
     *
     * @param childTag          the child tag.
     * @param seriesInstanceUID the series instance uid.
     * @param sopInstanceUID    the sop instance uid.
     * @param frame             the frame.
     * @return true if the condition is met; otherwise false.
     */
    private boolean isImageFrameApplicable(int childTag, String seriesInstanceUID, String sopInstanceUID, int frame) {
        if (StringKit.hasText(seriesInstanceUID)) {
            for (Attributes refSeriesSeq : getReferencedSeriesSequence()) {
                if (seriesInstanceUID.equals(refSeriesSeq.getString(Tag.SeriesInstanceUID))) {
                    List<Attributes> refImgSeq = Builder
                            .getSequence(Objects.requireNonNull(refSeriesSeq), Tag.ReferencedImageSequence);
                    return Builder.isImageFrameApplicableToReferencedImageSequence(
                            refImgSeq,
                            childTag,
                            sopInstanceUID,
                            frame,
                            true);
                }
            }
        }
        return false;
    }

}
