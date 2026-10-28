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
import java.awt.Color;
import java.awt.geom.Area;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.miaixz.bus.logger.Logger;

/**
 * Represents the PresentationLutObject type.
 *
 * @author Kimi Liu
 */
public class PresentationLutObject implements PresentationStateLut {

    /**
     * Defined term for an identity presentation LUT.
     */
    private static final String IDENTITY_LUT_SHAPE = "IDENTITY";

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
     * VOI module used by the first softcopy item or the presentation-state dataset.
     */
    private final VoiLutModule voiLUT;

    /**
     * Presentation lookup table, or null when only a LUT shape applies.
     */
    private final LookupTableCV prLut;

    /**
     * Presentation LUT explanation, or null when absent.
     */
    private final String prLutExplanation;

    /**
     * Presentation LUT shape defined term, or null when absent.
     */
    private final String prLUTShapeMode;

    /**
     * Ordered softcopy VOI items with their image and frame references.
     */
    private final List<VoiItem> voiItems;

    /**
     * Palette applied after VOI for pseudo-color or blending presentation states.
     */
    private final LookupTableCV paletteColorLut;

    /**
     * Ordered input layers of a blending presentation state.
     */
    private final List<BlendingLayer> blendingLayers;

    /**
     * Presentation-state SOP Class resolved from the dataset.
     */
    private final PresentationStateType presentationStateType;

    /**
     * Creates a new instance.
     *
     * @param dcmPR the dcm pr.
     */
    public PresentationLutObject(Attributes dcmPR) {
        this(dcmPR, null);
    }

    /**
     * Creates a presentation state object from DICOM attributes with optional image descriptor.
     *
     * @param dcmPR DICOM presentation state attributes
     * @param desc  optional image descriptor with pre-computed data
     * @throws NullPointerException  if dcmPR is null
     * @throws IllegalStateException if SOP class UID is not recognized
     */
    public PresentationLutObject(Attributes dcmPR, ImageDescriptor desc) {
        this.dcmPR = Objects.requireNonNull(dcmPR);
        this.presentationStateType = validateAndGetPresentationStateType();

        this.modalityLUT = desc == null ? new ModalityLutModule(dcmPR) : desc.getModalityLUT();
        boolean blending = presentationStateType == PresentationStateType.BLENDING_SOFTCOPY;
        this.blendingLayers = blending ? buildBlendingLayers(dcmPR) : List.of();
        // A blending state windows its underlying set through its own layer: that is the view's VOI.
        this.voiLUT = blending ? layer(blendingLayers, false).map(BlendingLayer::voiLut).orElse(null)
                : buildVoiLut(dcmPR);
        this.voiItems = blending ? List.of() : buildVoiItems(dcmPR);
        this.overlays = OverlayData.getPrOverlayData(dcmPR, -1);
        this.shutterOverlays = desc == null ? OverlayData.getOverlayData(dcmPR, 0xffff) : desc.getOverlayData();

        var lutData = initializePresentationLut();
        this.prLut = lutData.lut().orElse(null);
        this.prLutExplanation = lutData.explanation().orElse(null);
        this.prLUTShapeMode = lutData.shapeMode().orElse(null);
        this.paletteColorLut = buildPaletteColorLut(dcmPR, presentationStateType, blendingLayers);
    }

    /**
     * Reads the initial presentation-state VOI module, falling back to dataset-level VOI attributes.
     *
     * @param dcmPR the presentation-state dataset
     * @return the initial VOI module
     */
    private static VoiLutModule buildVoiLut(Attributes dcmPR) {
        Attributes seqDcm = dcmPR.getNestedDataset(Tag.SoftcopyVOILUTSequence);
        return seqDcm == null ? null : new VoiLutModule(seqDcm);
    }

    /**
     * Creates a presentation state object by reading DICOM file from the specified path.
     *
     * @param prPath file path to the DICOM presentation state file
     * @return presentation state object
     * @throws IOException if file cannot be read or parsed
     */
    public static PresentationLutObject getPresentationState(String prPath) throws IOException {
        return getPresentationState(Path.of(prPath));
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
        return Optional.ofNullable(prLut);
    }

    /**
     * Gets the pr lut explanation.
     *
     * @return the pr lut explanation.
     */
    @Override
    public Optional<String> getPrLutExplanation() {
        return Optional.ofNullable(prLutExplanation);
    }

    /**
     * Gets the pr lut shape mode.
     *
     * @return the pr lut shape mode.
     */
    @Override
    public Optional<String> getPrLutShapeMode() {
        return Optional.ofNullable(prLUTShapeMode);
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
     * The VOI LUT of the first item of the Softcopy VOI LUT Sequence, whatever image it references, or the one of the
     * underlying layer of a blending state.
     *
     * @see #getVoiLUT(String, int)
     * @return vOI module used by the first softcopy item or the presentation-state dataset
     */
    public Optional<VoiLutModule> getVoiLUT() {
        return Optional.ofNullable(voiLUT);
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
     * The referenced series: the top-level sequence, or for a Blending Softcopy state the series of the underlying
     * layer, whose window is the state's VOI.
     *
     * @return The referenced series: the top-level sequence, or for a Blending Softcopy state the series of the
     *         underlying layer, whose window is the state's VOI
     */
    public List<Attributes> getReferencedSeriesSequence() {
        List<Attributes> topLevel = Builder.getSequence(dcmPR, Tag.ReferencedSeriesSequence);
        if (topLevel.isEmpty()) {
            return getUnderlyingLayer().map(BlendingLayer::referencedSeries).orElse(topLevel);
        }
        return topLevel;
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
     * Checks if this presentation state is applicable to a specific image frame.
     *
     * @param seriesInstanceUID the series UID of the target image
     * @param sopInstanceUID    the SOP instance UID of the target image
     * @param frame             the frame number (1-based)
     * @return true if this presentation state applies to the specified frame
     */
    public boolean isImageFrameApplicable(String seriesInstanceUID, String sopInstanceUID, int frame) {
        return isApplicableByReference(Tag.ReferencedFrameNumber, seriesInstanceUID, sopInstanceUID, frame);
    }

    /**
     * Checks if this presentation state is applicable to a specific segmentation segment.
     *
     * @param seriesInstanceUID the series UID of the target segmentation
     * @param sopInstanceUID    the SOP instance UID of the target segmentation
     * @param segment           the segment number
     * @return true if this presentation state applies to the specified segment
     */
    public boolean isSegmentationSegmentApplicable(String seriesInstanceUID, String sopInstanceUID, int segment) {
        return isApplicableByReference(Tag.ReferencedSegmentNumber, seriesInstanceUID, sopInstanceUID, segment);
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

    /**
     * Enumeration of all DICOM Presentation State SOP Classes (UIDs 11.1-11.12) grouped by functionality. Each type
     * defines specific capabilities and data structures for medical image presentation.
     *
     * @author Kimi Liu
     */
    public enum PresentationStateType {

        /**
         * The grayscale softcopy presentation-state category.
         */
        GRAYSCALE_SOFTCOPY("1.2.840.10008.5.1.4.1.1.11.1", "Grayscale Softcopy Presentation State Storage",
                PresentationGroup.BASIC_GRAYSCALE),

        /**
         * The color softcopy presentation-state category.
         */
        COLOR_SOFTCOPY("1.2.840.10008.5.1.4.1.1.11.2", "Color Softcopy Presentation State Storage",
                PresentationGroup.COLOR_BASED),

        /**
         * The pseudo color softcopy presentation-state category.
         */
        PSEUDO_COLOR_SOFTCOPY("1.2.840.10008.5.1.4.1.1.11.3", "Pseudo-Color Softcopy Presentation State Storage",
                PresentationGroup.COLOR_BASED),

        /**
         * The blending softcopy presentation-state category.
         */
        BLENDING_SOFTCOPY("1.2.840.10008.5.1.4.1.1.11.4", "Blending Softcopy Presentation State Storage",
                PresentationGroup.COLOR_BASED),

        /**
         * The xa xrf grayscale softcopy presentation-state category.
         */
        XA_XRF_GRAYSCALE_SOFTCOPY("1.2.840.10008.5.1.4.1.1.11.5",
                "XA/XRF Grayscale Softcopy Presentation State Storage", PresentationGroup.SPECIALIZED),

        /**
         * The grayscale planar mpr volumetric presentation-state category.
         */
        GRAYSCALE_PLANAR_MPR_VOLUMETRIC("1.2.840.10008.5.1.4.1.1.11.6",
                "Grayscale Planar MPR Volumetric Presentation State Storage", PresentationGroup.VOLUMETRIC),

        /**
         * The compositing planar mpr volumetric presentation-state category.
         */
        COMPOSITING_PLANAR_MPR_VOLUMETRIC("1.2.840.10008.5.1.4.1.1.11.7",
                "Compositing Planar MPR Volumetric Presentation State Storage", PresentationGroup.VOLUMETRIC),

        /**
         * The advanced blending presentation-state category.
         */
        ADVANCED_BLENDING("1.2.840.10008.5.1.4.1.1.11.8", "Advanced Blending Presentation State Storage",
                PresentationGroup.ADVANCED_BLENDING),

        /**
         * The volume rendering volumetric presentation-state category.
         */
        VOLUME_RENDERING_VOLUMETRIC("1.2.840.10008.5.1.4.1.1.11.9",
                "Volume Rendering Volumetric Presentation State Storage", PresentationGroup.VOLUMETRIC),

        /**
         * The segmented volume rendering volumetric presentation-state category.
         */
        SEGMENTED_VOLUME_RENDERING_VOLUMETRIC("1.2.840.10008.5.1.4.1.1.11.10",
                "Segmented Volume Rendering Volumetric Presentation State Storage", PresentationGroup.VOLUMETRIC),

        /**
         * The multiple volume rendering volumetric presentation-state category.
         */
        MULTIPLE_VOLUME_RENDERING_VOLUMETRIC("1.2.840.10008.5.1.4.1.1.11.11",
                "Multiple Volume Rendering Volumetric Presentation State Storage", PresentationGroup.VOLUMETRIC),

        /**
         * The variable modality lut softcopy presentation-state category.
         */
        VARIABLE_MODALITY_LUT_SOFTCOPY("1.2.840.10008.5.1.4.1.1.11.12",
                "Variable Modality LUT Softcopy Presentation State Storage", PresentationGroup.ADVANCED_LUT);

        /**
         * DICOM SOP Class UID identifying this presentation-state type.
         */
        private final String uid;
        /**
         * Human-readable description of this presentation-state category.
         */
        private final String description;
        /**
         * Imaging region matched by this selector.
         */
        private final PresentationGroup group;

        /**
         * Creates a presentation-state category with its coding information.
         *
         * @param uid         the presentation-state SOP Class UID
         * @param description the human-readable presentation-state description
         * @param group       the presentation-state rendering category
         */
        PresentationStateType(String uid, String description, PresentationGroup group) {
            this.uid = uid;
            this.description = description;
            this.group = group;
        }

        /**
         * Returns dICOM SOP Class UID identifying this presentation-state type.
         *
         * @return the DICOM SOP Class UID identifying this presentation-state type
         */
        public String getUid() {
            return uid;
        }

        /**
         * Returns human-readable description of this presentation-state category.
         *
         * @return human-readable description of this presentation-state category
         */
        public String getDescription() {
            return description;
        }

        /**
         * Returns imaging region matched by this selector.
         *
         * @return imaging region matched by this selector
         */
        public PresentationGroup getGroup() {
            return group;
        }

        /**
         * Finds a presentation-state type by its SOP Class UID.
         *
         * @param uid the presentation-state SOP Class UID
         * @return the matching type, or an empty result for an unknown UID
         */
        public static Optional<PresentationStateType> fromUid(String uid) {
            for (PresentationStateType type : values()) {
                if (type.uid.equals(uid)) {
                    return Optional.of(type);
                }
            }
            return Optional.empty();
        }

        /**
         * Finds all presentation-state types belonging to a rendering category.
         *
         * @param group the presentation-state rendering category
         * @return the matching presentation-state types
         */
        public static Set<PresentationStateType> getByGroup(PresentationGroup group) {
            return Set.of(values()).stream().filter(type -> type.group == group).collect(Collectors.toSet());
        }

    }

    /**
     * Functional groups that categorize presentation state types by their capabilities and use cases.
     *
     * @author Kimi Liu
     */
    public enum PresentationGroup {

        /**
         * The basic grayscale presentation-state category.
         */
        BASIC_GRAYSCALE("Basic grayscale presentation with standard windowing"),
        /**
         * The color based presentation-state category.
         */
        COLOR_BASED("Color and pseudo-color presentation states"),
        /**
         * The specialized presentation-state category.
         */
        SPECIALIZED("Specialized modality-specific presentation states"),
        /**
         * The volumetric presentation-state category.
         */
        VOLUMETRIC("3D volumetric rendering presentation states"),
        /**
         * The advanced blending presentation-state category.
         */
        ADVANCED_BLENDING("Advanced blending and compositing"),
        /**
         * The advanced lut presentation-state category.
         */
        ADVANCED_LUT("Advanced LUT manipulation and variable modality LUT");

        /**
         * Human-readable description of this presentation-state category.
         */
        private final String description;

        /**
         * Creates a presentation-state category with its coding information.
         *
         * @param description the human-readable presentation-state description
         */
        PresentationGroup(String description) {
            this.description = description;
        }

        /**
         * Returns human-readable description of this presentation-state category.
         *
         * @return human-readable description of this presentation-state category
         */
        public String getDescription() {
            return description;
        }

    }

    /**
     * Builds the palette supplied by a pseudo-color or blending presentation state.
     *
     * @param dcmPR  the presentation-state dataset
     * @param type   the resolved presentation-state SOP Class type
     * @param layers the blending input layers
     * @return the decoded palette, or null when no palette is supplied
     */
    private static LookupTableCV buildPaletteColorLut(
            Attributes dcmPR,
            PresentationStateType type,
            List<BlendingLayer> layers) {
        if (type == PresentationStateType.PSEUDO_COLOR_SOFTCOPY) {
            return RGBImageVoiLut.getPaletteColorLookupTable(dcmPR);
        }
        if (type != PresentationStateType.BLENDING_SOFTCOPY) {
            return null;
        }
        LookupTableCV lut = RGBImageVoiLut.getPaletteColorLookupTable(dcmPR);
        if (lut != null) {
            return lut;
        }
        return Builder.getSequence(dcmPR, Tag.BlendingSequence).stream()
                .filter(item -> BlendingLayer.SUPERIMPOSED.equals(item.getString(Tag.BlendingPosition))).findFirst()
                .map(RGBImageVoiLut::getPaletteColorLookupTable).orElse(null);
    }

    /**
     * The Palette Color LUT of a Pseudo-Color presentation state, applied to the P-Values after the VOI stage; empty
     * for the other presentation state types.
     *
     * @return palette applied after VOI for pseudo-color or blending presentation states
     */
    public Optional<LookupTableCV> getPaletteColorLut() {
        return Optional.ofNullable(paletteColorLut);
    }

    /**
     * Resolves the SOP Class UID and rejects unsupported presentation-state types.
     *
     * @return the supported presentation-state type
     */
    private PresentationStateType validateAndGetPresentationStateType() {
        String sopClassUid = dcmPR.getString(Tag.SOPClassUID, "");
        Optional<PresentationStateType> type = PresentationStateType.fromUid(sopClassUid);
        if (type.isEmpty()) {
            throw new IllegalStateException(
                    "SOPClassUID '" + sopClassUid + "' does not match any supported DICOM Presentation State");
        }

        PresentationGroup group = type.get().group;
        if (group != PresentationGroup.BASIC_GRAYSCALE && group != PresentationGroup.COLOR_BASED) {
            Logger.warn("Presentation State '{}' is not fully supported", type.get().name());
        }
        return type.get();
    }

    /**
     * Reads the presentation LUT sequence or its identity/inverse shape fallback.
     *
     * @return the presentation LUT values and explanatory attributes
     */
    private PresentationLutData initializePresentationLut() {
        Attributes dcmLut = dcmPR.getNestedDataset(Tag.PresentationLUTSequence);
        return dcmLut != null ? createPresentationLutFromSequence(dcmLut) : createPresentationLutFromShape();
    }

    /**
     * Decodes a presentation LUT sequence item and its explanation.
     *
     * @param dcmLut the presentation LUT sequence item
     * @return the decoded presentation LUT data
     */
    private PresentationLutData createPresentationLutFromSequence(Attributes dcmLut) {
        return new PresentationLutData(RGBImageVoiLut.createLut(dcmLut),
                Optional.ofNullable(dcmPR.getString(Tag.LUTExplanation)), Optional.of(IDENTITY_LUT_SHAPE));
    }

    /**
     * Creates presentation LUT metadata from a supported shape defined term.
     *
     * @return the presentation LUT shape data
     */
    private PresentationLutData createPresentationLutFromShape() {
        return new PresentationLutData(Optional.empty(), Optional.empty(),
                Optional.ofNullable(dcmPR.getString(Tag.PresentationLUTShape)));
    }

    /**
     * One item of the Softcopy VOI LUT Sequence and the images it applies to.
     *
     * @param referencedImages Referenced images and optional one-based frame numbers limiting this VOI item.
     * @param voiLut           the VOI module associated with this image or blending input
     * @author Kimi Liu
     */
    private record VoiItem(List<Attributes> referencedImages, VoiLutModule voiLut) {

        // Without Referenced Image Sequence, the item applies to all the images of the state
        /**
         * Tests whether this VOI item references the requested image and one-based frame.
         *
         * @param sopInstanceUID the referenced SOP Instance UID
         * @param frame          the one-based frame number used by DICOM references
         * @return whether this item applies to the requested frame
         */
        boolean appliesTo(String sopInstanceUID, int frame) {
            return referencedImages.isEmpty() || Builder.isImageFrameApplicableToReferencedImageSequence(
                    referencedImages,
                    Tag.ReferencedFrameNumber,
                    sopInstanceUID,
                    frame,
                    true);
        }

    }

    /**
     * Reads softcopy VOI items together with their image and frame restrictions.
     *
     * @param dcmPR the presentation-state dataset
     * @return the ordered softcopy VOI items
     */
    private static List<VoiItem> buildVoiItems(Attributes dcmPR) {
        return Builder.getSequence(dcmPR, Tag.SoftcopyVOILUTSequence).stream().map(
                item -> new VoiItem(Builder.getSequence(item, Tag.ReferencedImageSequence), new VoiLutModule(item)))
                .toList();
    }

    /**
     * Checks presentation-state series, image and frame references for applicability.
     *
     * @param childTag          the image or frame reference sequence tag
     * @param seriesInstanceUID the referenced Series Instance UID
     * @param sopInstanceUID    the referenced SOP Instance UID
     * @param referenceNumber   the one-based frame number or reference index
     * @return whether the requested reference is applicable
     */
    private boolean isApplicableByReference(
            int childTag,
            String seriesInstanceUID,
            String sopInstanceUID,
            int referenceNumber) {
        if (!StringKit.hasText(seriesInstanceUID)) {
            return false;
        }
        return getReferencedSeriesSequence().stream()
                .filter(refSeriesSeq -> seriesInstanceUID.equals(refSeriesSeq.getString(Tag.SeriesInstanceUID)))
                .anyMatch(
                        refSeriesSeq -> isImageFrameApplicable(
                                refSeriesSeq,
                                childTag,
                                sopInstanceUID,
                                referenceNumber));
    }

    /**
     * Checks image or frame references within a referenced-series item.
     *
     * @param refSeriesSeq    the referenced-series item
     * @param childTag        the image or frame reference sequence tag
     * @param sopInstanceUID  the referenced SOP Instance UID
     * @param referenceNumber the one-based frame number or reference index
     * @return whether the requested image or frame is referenced
     */
    private boolean isImageFrameApplicable(
            Attributes refSeriesSeq,
            int childTag,
            String sopInstanceUID,
            int referenceNumber) {
        List<Attributes> refImgSeq = Builder.getSequence(refSeriesSeq, Tag.ReferencedImageSequence);
        return Builder.isImageFrameApplicableToReferencedImageSequence(
                refImgSeq,
                childTag,
                sopInstanceUID,
                referenceNumber,
                true);
    }

    /**
     * One item of the Blending Sequence of a Blending Softcopy presentation state: the image set it addresses and the
     * grayscale transformation to apply to it before blending.
     *
     * @param position         {@code UNDERLYING} or {@code SUPERIMPOSED}
     * @param voiLut           the layer's VOI LUT, or null when none is given
     * @param studyInstanceUid Study UID containing the referenced blending inputs.
     * @param referencedSeries Series reference items identifying the blending inputs.
     * @author Kimi Liu
     */
    public record BlendingLayer(String position, String studyInstanceUid, List<Attributes> referencedSeries,
            VoiLutModule voiLut) {

        /**
         * Defined term identifying the superimposed blending input.
         */
        public static final String SUPERIMPOSED = "SUPERIMPOSED"; // NON-NLS
        /**
         * Defined term identifying the underlying blending input.
         */
        public static final String UNDERLYING = "UNDERLYING"; // NON-NLS

        /**
         * Tests whether this blending input is the superimposed layer.
         *
         * @return whether this layer is superimposed
         */
        public boolean isSuperimposed() {
            return SUPERIMPOSED.equals(position);
        }

        /**
         * Collects the series UIDs referenced by this blending input.
         *
         * @return the referenced series UIDs
         */
        public List<String> seriesInstanceUids() {
            return referencedSeries.stream().map(ref -> ref.getString(Tag.SeriesInstanceUID)).filter(StringKit::hasText)
                    .toList();
        }

        /**
         * Checks whether this blending layer references a series UID.
         *
         * @param seriesInstanceUid the Series Instance UID to match
         * @return whether the series belongs to this layer
         */
        public boolean references(String seriesInstanceUid) {
            return seriesInstanceUid != null && seriesInstanceUids().contains(seriesInstanceUid);
        }

    }

    /**
     * Reads the input layers, their references and layer-specific VOI modules.
     *
     * @param dcmPR the presentation-state dataset
     * @return the ordered blending inputs
     */
    private static List<BlendingLayer> buildBlendingLayers(Attributes dcmPR) {
        return Builder.getSequence(dcmPR, Tag.BlendingSequence).stream().map(item -> {
            Attributes voi = item.getNestedDataset(Tag.SoftcopyVOILUTSequence);
            return new BlendingLayer(item.getString(Tag.BlendingPosition), item.getString(Tag.StudyInstanceUID),
                    Builder.getSequence(item, Tag.ReferencedSeriesSequence),
                    voi == null ? null : new VoiLutModule(voi));
        }).toList();
    }

    /**
     * Finds the underlying or superimposed input from the blending layer list.
     *
     * @param layers       the blending input layers
     * @param superimposed whether to select the superimposed rather than underlying layer
     * @return the requested layer, or an empty result when absent
     */
    private static Optional<BlendingLayer> layer(List<BlendingLayer> layers, boolean superimposed) {
        return layers.stream().filter(l -> l.isSuperimposed() == superimposed).findFirst();
    }

    /**
     * The two layers of a Blending Softcopy presentation state; empty for the other types.
     *
     * @return ordered input layers of a blending presentation state
     */
    public List<BlendingLayer> getBlendingLayers() {
        return blendingLayers;
    }

    /**
     * Returns the underlying input layer, if present.
     *
     * @return the underlying input layer, if present
     */
    public Optional<BlendingLayer> getUnderlyingLayer() {
        return layer(blendingLayers, false);
    }

    /**
     * Returns the superimposed input layer, if present.
     *
     * @return the superimposed input layer, if present
     */
    public Optional<BlendingLayer> getSuperimposedLayer() {
        return layer(blendingLayers, true);
    }

    /**
     * Opacity of the superimposed set, 0 to 1; present on a Blending Softcopy presentation state.
     *
     * @return Opacity of the superimposed set, 0 to 1; present on a Blending Softcopy presentation state
     */
    public OptionalDouble getRelativeOpacity() {
        Double value = dcmPR.getDouble(Tag.RelativeOpacity, Double.NaN);
        return Double.isNaN(value) ? OptionalDouble.empty() : OptionalDouble.of(value);
    }

    /**
     * UID of a (well-known) palette the presentation state refers to instead of, or besides, data.
     *
     * @return UID of a (well-known) palette the presentation state refers to instead of, or besides, data
     */
    public Optional<String> getPaletteColorLutUid() {
        return Optional.ofNullable(dcmPR.getString(Tag.PaletteColorLookupTableUID));
    }

    /**
     * Returns presentation-state SOP Class resolved from the dataset.
     *
     * @return presentation-state SOP Class resolved from the dataset
     */
    public PresentationStateType getPresentationStateType() {
        return presentationStateType;
    }

    /**
     * Returns the rendering category of this presentation-state SOP Class.
     *
     * @return the rendering category of this presentation-state SOP Class
     */
    public PresentationGroup getPresentationGroup() {
        return presentationStateType.getGroup();
    }

    /**
     * Returns the display description of this presentation-state SOP Class.
     *
     * @return the display description of this presentation-state SOP Class
     */
    public String getSopClassDescription() {
        return presentationStateType.getDescription();
    }

    /**
     * The VOI LUT this presentation state applies to a frame: the first item of the Softcopy VOI LUT Sequence that
     * references the frame or has no Referenced Image Sequence (PS3.3 C.11.8). It is empty when no item applies, as
     * when the state has none: the VOI of the state then leaves the image alone.
     *
     * @param sopInstanceUID the SOP Instance UID of the image, null when unknown (the first item then applies, see
     *                       {@link #getVoiLUT()})
     * @param frame          the frame number, from 1
     * @return vOI module used by the first softcopy item or the presentation-state dataset
     */
    public Optional<VoiLutModule> getVoiLUT(String sopInstanceUID, int frame) {
        if (presentationStateType == PresentationStateType.BLENDING_SOFTCOPY || !StringKit.hasText(sopInstanceUID)) {
            return getVoiLUT();
        }
        return voiItems.stream().filter(item -> item.appliesTo(sopInstanceUID, frame)).map(VoiItem::voiLut).findFirst();
    }

    /**
     * Tests whether the presentation-state type belongs to the volumetric rendering group.
     *
     * @return whether this state represents volumetric rendering
     */
    public boolean supportsVolumetricRendering() {
        return presentationStateType.getGroup() == PresentationGroup.VOLUMETRIC;
    }

    /**
     * Tests whether this state represents advanced blending.
     *
     * @return whether the state is an advanced blending presentation
     */
    public boolean supportsAdvancedBlending() {
        return presentationStateType.getGroup() == PresentationGroup.ADVANCED_BLENDING;
    }

    /**
     * Creates a presentation state object by reading DICOM file from the specified path.
     *
     * @param prPath file path to the DICOM presentation state file
     * @return presentation state object
     * @throws IOException if file cannot be read or parsed
     */
    public static PresentationLutObject getPresentationState(Path prPath) throws IOException {
        try (var inputStream = new BufferedInputStream(Files.newInputStream(prPath), Builder.FILE_BUFFER_SIZE);
                var dis = new ImageInputStream(inputStream)) {
            return new PresentationLutObject(dis.readDataset());
        }
    }

    /**
     * Collects the presentation lookup table, explanation and output shape.
     *
     * @param lut         Optional native presentation lookup table.
     * @param explanation Optional presentation LUT explanation.
     * @param shapeMode   Optional presentation LUT shape defined term.
     * @author Kimi Liu
     */
    private record PresentationLutData(Optional<LookupTableCV> lut, Optional<String> explanation,
            Optional<String> shapeMode) {

    }

}
