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
package org.miaixz.bus.image.builtin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.*;

/**
 * The {@code MultiframeExtractor} class is designed to extract single-frame images from enhanced multi-frame DICOM
 * images. This class supports the extraction of multi-frame images from various DICOM SOP classes, including CT, MR,
 * XA, XRF, PET, X-Ray 3D Angiographic, Nuclear Medicine, Ultrasound Multi-frame, Multi-frame Grayscale Byte/Word/True
 * Color Secondary Capture, X-Ray Angiographic, X-Ray Radiofluoroscopic, and RT Image Storage. During the extraction
 * process, it handles functional group sequences, pixel data, referenced image sequences, and generates corresponding
 * legacy single-frame DICOM images.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MultiframeExtractor {

    /**
     * The impls value.
     */
    private static final HashMap<String, Impl> impls = new HashMap<>(8);

    /**
     * The exclude tags value.
     */
    private static final int[] EXCLUDE_TAGS = { Tag.ReferencedImageEvidenceSequence, Tag.SourceImageEvidenceSequence,
            Tag.DimensionIndexSequence, Tag.NumberOfFrames, Tag.SharedFunctionalGroupsSequence,
            Tag.PerFrameFunctionalGroupsSequence, Tag.PixelData };

    /**
     * The preserve series instance UID value.
     */
    private boolean preserveSeriesInstanceUID;

    /**
     * The instance number format value.
     */
    private String instanceNumberFormat = "%s%04d";

    /**
     * The UID mapper value.
     */
    private UIDMapper uidMapper = new HashUIDMapper();

    /**
     * The nof accessor value.
     */
    private NumberOfFramesAccessor nofAccessor = new NumberOfFramesAccessor();

    static {
        impls.put(UID.EnhancedCTImageStorage.uid, Impl.EnhancedCTImageExtractor);
        impls.put(UID.EnhancedMRImageStorage.uid, Impl.EnhancedMRImageExtractor);
        impls.put(UID.EnhancedXAImageStorage.uid, Impl.EnhancedXAImageExtractor);
        impls.put(UID.EnhancedXRFImageStorage.uid, Impl.EnhancedXRFImageExtractor);
        impls.put(UID.EnhancedPETImageStorage.uid, Impl.EnhancedPETImageExtractor);
        impls.put(UID.XRay3DAngiographicImageStorage.uid, Impl.XRay3DAngiographicImageExtractor);
        impls.put(UID.NuclearMedicineImageStorage.uid, Impl.NuclearMedicineImageExtractor);
        impls.put(UID.UltrasoundMultiFrameImageStorage.uid, Impl.UltrasoundMultiFrameImageExtractor);
        impls.put(
                UID.MultiFrameGrayscaleByteSecondaryCaptureImageStorage.uid,
                Impl.MultiFrameGrayscaleByteSecondaryCaptureImageExtractor);
        impls.put(
                UID.MultiFrameGrayscaleWordSecondaryCaptureImageStorage.uid,
                Impl.MultiFrameGrayscaleWordSecondaryCaptureImageExtractor);
        impls.put(
                UID.MultiFrameTrueColorSecondaryCaptureImageStorage.uid,
                Impl.MultiFrameTrueColorSecondaryCaptureImageExtractor);
        impls.put(UID.XRayAngiographicImageStorage.uid, Impl.XRayAngiographicImageExtractor);
        impls.put(UID.XRayRadiofluoroscopicImageStorage.uid, Impl.XRayRadiofluoroscopicImageExtractor);
        impls.put(UID.RTImageStorage.uid, Impl.RTImageExtractor);
    }

    /**
     * Checks whether the supported SOP class condition is true.
     *
     * @param cuid the cuid.
     * @return true if the supported SOP class condition is true; otherwise false.
     */
    public static boolean isSupportedSOPClass(String cuid) {
        return impls.containsKey(cuid);
    }

    /**
     * Executes the legacy SOP class UID operation.
     *
     * @param mfcuid the mfcuid.
     * @return the operation result.
     */
    public static String legacySOPClassUID(String mfcuid) {
        Impl impl = impls.get(mfcuid);
        return impl != null ? impl.sfcuid : null;
    }

    /**
     * Executes the impl for operation.
     *
     * @param mfcuid the mfcuid.
     * @return the operation result.
     */
    private static Impl implFor(String mfcuid) {
        Impl impl = impls.get(mfcuid);
        if (impl == null)
            throw new IllegalArgumentException("Unsupported SOP Class: " + mfcuid);
        return impl;
    }

    /**
     * Checks whether the preserve series instance UID condition is true.
     *
     * @return true if the preserve series instance UID condition is true; otherwise false.
     */
    public final boolean isPreserveSeriesInstanceUID() {
        return preserveSeriesInstanceUID;
    }

    /**
     * Sets the preserve series instance UID.
     *
     * @param preserveSeriesInstanceUID the preserve series instance UID.
     */
    public final void setPreserveSeriesInstanceUID(boolean preserveSeriesInstanceUID) {
        this.preserveSeriesInstanceUID = preserveSeriesInstanceUID;
    }

    /**
     * Returns the instance number format.
     *
     * @return the instance number format.
     */
    public final String getInstanceNumberFormat() {
        return instanceNumberFormat;
    }

    /**
     * Sets the instance number format.
     *
     * @param instanceNumberFormat the instance number format.
     */
    public final void setInstanceNumberFormat(String instanceNumberFormat) {
        String.format(instanceNumberFormat, "1", 1);
        this.instanceNumberFormat = instanceNumberFormat;
    }

    /**
     * Returns the UID mapper.
     *
     * @return the UID mapper.
     */
    public final UIDMapper getUIDMapper() {
        return uidMapper;
    }

    /**
     * Sets the UID mapper.
     *
     * @param uidMapper the UID mapper.
     */
    public final void setUIDMapper(UIDMapper uidMapper) {
        if (uidMapper == null)
            throw new NullPointerException();
        this.uidMapper = uidMapper;
    }

    /**
     * Returns the number of frames accessorr.
     *
     * @return the number of frames accessorr.
     */
    public final NumberOfFramesAccessor getNumberOfFramesAccessorr() {
        return nofAccessor;
    }

    /**
     * Sets the number of frames accessor.
     *
     * @param accessor the accessor.
     */
    public final void setNumberOfFramesAccessor(NumberOfFramesAccessor accessor) {
        if (accessor == null)
            throw new NullPointerException();
        this.nofAccessor = accessor;
    }

    /**
     * Extracts the extract.
     *
     * @param emf   the emf.
     * @param frame the frame.
     * @return the operation result.
     */
    public Attributes extract(Attributes emf, int frame) {
        return implFor(emf.getString(Tag.SOPClassUID)).extract(this, emf, frame);
    }

    /**
     * Extracts the extract.
     *
     * @param emf      the emf.
     * @param frame    the frame.
     * @param cuid     the cuid.
     * @param enhanced the enhanced.
     * @return the operation result.
     */
    private Attributes extract(Attributes emf, int frame, String cuid, boolean enhanced) {
        Attributes dest = new Attributes(emf.size() * 2);
        dest.addNotSelected(emf, EXCLUDE_TAGS);
        if (enhanced) {
            Attributes sfgs = emf.getNestedDataset(Tag.SharedFunctionalGroupsSequence);
            if (sfgs == null)
                throw new IllegalArgumentException("Missing (5200,9229) Shared Functional Groups Sequence");
            Attributes fgs = emf.getNestedDataset(Tag.PerFrameFunctionalGroupsSequence, frame);
            if (fgs == null)
                throw new IllegalArgumentException(
                        "Missing (5200,9230) Per-frame Functional Groups Sequence Item for frame #" + (frame + 1));
            addFunctionGroups(dest, sfgs);
            addFunctionGroups(dest, fgs);
            dest.setString(Tag.ImageType, VR.CS, dest.getStrings(Tag.FrameType));
            dest.remove(Tag.FrameType);
        }
        addPixelData(dest, emf, frame);
        dest.setString(Tag.SOPClassUID, VR.UI, cuid);
        dest.setString(
                Tag.SOPInstanceUID,
                VR.UI,
                uidMapper.get(dest.getString(Tag.SOPInstanceUID)) + Symbol.C_DOT + (frame + 1));
        dest.setString(
                Tag.InstanceNumber,
                VR.IS,
                createInstanceNumber(dest.getString(Tag.InstanceNumber, Normal.EMPTY), frame));
        if (!preserveSeriesInstanceUID)
            dest.setString(Tag.SeriesInstanceUID, VR.UI, uidMapper.get(dest.getString(Tag.SeriesInstanceUID)));
        adjustReferencedImages(dest, Tag.ReferencedImageSequence);
        adjustReferencedImages(dest, Tag.SourceImageSequence);
        return dest;
    }

    /**
     * Executes the adjust referenced images operation.
     *
     * @param attrs the attrs.
     * @param sqtag the sqtag.
     */
    private void adjustReferencedImages(Attributes attrs, int sqtag) {
        Sequence sq = attrs.getSequence(sqtag);
        if (sq == null)
            return;
        ArrayList<Attributes> newRefs = new ArrayList<>();
        for (Iterator<Attributes> itr = sq.iterator(); itr.hasNext();) {
            Attributes ref = itr.next();
            String cuid = legacySOPClassUID(ref.getString(Tag.ReferencedSOPClassUID));
            if (cuid == null)
                continue;
            itr.remove();
            String iuid = uidMapper.get(ref.getString(Tag.ReferencedSOPInstanceUID));
            int[] frames = ref.getInts(Tag.ReferencedFrameNumber);
            int n = frames == null ? nofAccessor.getNumberOfFrames(iuid) : frames.length;
            ref.remove(Tag.ReferencedFrameNumber);
            ref.setString(Tag.ReferencedSOPClassUID, VR.UI, cuid);
            for (int i = 0; i < n; i++) {
                Attributes newRef = new Attributes(ref);
                newRef.setString(
                        Tag.ReferencedSOPInstanceUID,
                        VR.UI,
                        iuid + '.' + (frames != null ? frames[i] : (i + 1)));
                newRefs.add(newRef);
            }
        }
        for (Attributes ref : newRefs)
            sq.add(ref);
    }

    /**
     * Adds the function groups.
     *
     * @param dest the dest.
     * @param fgs  the fgs.
     */
    private void addFunctionGroups(Attributes dest, Attributes fgs) {
        dest.addSelected(fgs, Tag.ReferencedImageSequence);
        Attributes fg;
        for (int sqTag : fgs.tags())
            if (sqTag != Tag.ReferencedImageSequence && (fg = fgs.getNestedDataset(sqTag)) != null)
                dest.addAll(fg);
    }

    /**
     * Adds the pixel data.
     *
     * @param dest  the dest.
     * @param src   the src.
     * @param frame the frame.
     */
    private void addPixelData(Attributes dest, Attributes src, int frame) {
        VR.Holder vr = new VR.Holder();
        Object pixelData = src.getValue(Tag.PixelData, vr);
        if (pixelData instanceof byte[]) {
            dest.setBytes(Tag.PixelData, vr.vr, extractPixelData((byte[]) pixelData, frame, calcFrameLength(src)));
        } else if (pixelData instanceof BulkData) {
            dest.setValue(Tag.PixelData, vr.vr, extractPixelData((BulkData) pixelData, frame, calcFrameLength(src)));
        } else {
            Fragments destFrags = dest.newFragments(Tag.PixelData, vr.vr, 2);
            destFrags.add(null);
            destFrags.add(((Fragments) pixelData).get(frame + 1));
        }
    }

    /**
     * Extracts the pixel data.
     *
     * @param src    the src.
     * @param frame  the frame.
     * @param length the length.
     * @return the operation result.
     */
    private BulkData extractPixelData(BulkData src, int frame, int length) {
        return new BulkData(src.uriWithoutQuery(), src.offset() + (long) frame * length, length, src.bigEndian());
    }

    /**
     * Extracts the pixel data.
     *
     * @param src    the src.
     * @param frame  the frame.
     * @param length the length.
     * @return the operation result.
     */
    private byte[] extractPixelData(byte[] src, int frame, int length) {
        byte[] dest = new byte[length];
        System.arraycopy(src, frame * length, dest, 0, length);
        return dest;
    }

    /**
     * Executes the calc frame length operation.
     *
     * @param src the src.
     * @return the operation result.
     */
    private int calcFrameLength(Attributes src) {
        return src.getInt(Tag.Rows, 0) * src.getInt(Tag.Columns, 0) * (src.getInt(Tag.BitsAllocated, 8) >> 3)
                * src.getInt(Tag.NumberOfSamples, 1);
    }

    /**
     * Creates the instance number.
     *
     * @param mfinstno the mfinstno.
     * @param frame    the frame.
     * @return the operation result.
     */
    private String createInstanceNumber(String mfinstno, int frame) {
        String s = String.format(instanceNumberFormat, mfinstno, frame + 1);
        return s.length() > 16 ? s.substring(s.length() - 16) : s;
    }

    /**
     * Enumeration defining implementations for extracting single frames from different multi-frame SOP Classes. Each
     * implementation specifies the target single-frame SOP Class UID and whether it's an enhanced image.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private enum Impl {

        /**
         * Implementation for Enhanced CT Image Storage extraction.
         */
        EnhancedCTImageExtractor(UID.CTImageStorage.uid, true),

        /**
         * Implementation for Enhanced MR Image Storage extraction.
         */
        EnhancedMRImageExtractor(UID.MRImageStorage.uid, true) {

            /**
             * Extracts the extract.
             *
             * @param mfe   the mfe.
             * @param emf   the emf.
             * @param frame the frame.
             * @return the operation result.
             */
            @Override
            Attributes extract(MultiframeExtractor mfe, Attributes emf, int frame) {
                Attributes sf = super.extract(mfe, emf, frame);
                setEchoTime(sf);
                setScanningSequence(sf);
                setSequenceVariant(sf);
                setScanOptions(sf);
                return sf;
            }

            /**
             * Sets the echo time.
             *
             * @param sf the sf.
             */
            void setEchoTime(Attributes sf) {
                double echoTime = sf.getDouble(Tag.EffectiveEchoTime, 0);
                if (echoTime == 0)
                    sf.setNull(Tag.EchoTime, VR.DS);
                else
                    sf.setDouble(Tag.EchoTime, VR.DS, echoTime);
            }

            /**
             * Sets the scanning sequence.
             *
             * @param sf the sf.
             */
            void setScanningSequence(Attributes sf) {
                List<String> list = new ArrayList<>(3);
                String eps = sf.getString(Tag.EchoPulseSequence);
                if (!"GRADIENT".equals(eps))
                    list.add("SE");
                if (!"SPIN".equals(eps))
                    list.add("GR");
                if ("YES".equals(sf.getString(Tag.InversionRecovery)))
                    list.add("IR");
                if ("YES".equals(sf.getString(Tag.EchoPlanarPulseSequence)))
                    list.add("EP");
                sf.setString(Tag.ScanningSequence, VR.CS, list.toArray(new String[list.size()]));
            }

            /**
             * Sets the sequence variant.
             *
             * @param sf the sf.
             */
            void setSequenceVariant(Attributes sf) {
                List<String> list = new ArrayList<>(5);
                if (!"SINGLE".equals(sf.getString(Tag.SegmentedKSpaceTraversal)))
                    list.add("SK");
                String mf = sf.getString(Tag.MagnetizationTransfer);
                if (mf != null && !"NONE".equals(mf))
                    list.add("MTC");
                String ssps = sf.getString(Tag.SteadyStatePulseSequence);
                if (ssps != null && !"NONE".equals(ssps))
                    list.add("TIME_REVERSED".equals(ssps) ? "TRSS" : "SS");
                String sp = sf.getString(Tag.Spoiling);
                if (sp != null && !"NONE".equals(sp))
                    list.add("SP");
                String op = sf.getString(Tag.OversamplingPhase);
                if (op != null && !"NONE".equals(op))
                    list.add("OSP");
                if (list.isEmpty())
                    list.add("NONE");
                sf.setString(Tag.SequenceVariant, VR.CS, list.toArray(new String[list.size()]));
            }

            /**
             * Sets the scan options.
             *
             * @param sf the sf.
             */
            void setScanOptions(Attributes sf) {
                List<String> list = new ArrayList<>(3);
                String per = sf.getString(Tag.RectilinearPhaseEncodeReordering);
                if (per != null && !"LINEAR".equals(per))
                    list.add("PER");
                String frameType3 = sf.getString(Tag.ImageType, 2);
                if ("ANGIO".equals(frameType3))
                    sf.setString(Tag.AngioFlag, VR.CS, "Y");
                if (frameType3.startsWith("CARD"))
                    list.add("CG");
                if (frameType3.endsWith("RESP_GATED"))
                    list.add("RG");
                String pfd = sf.getString(Tag.PartialFourierDirection);
                if ("PHASE".equals(pfd))
                    list.add("PFP");
                if ("FREQUENCY".equals(pfd))
                    list.add("PFF");
                String sp = sf.getString(Tag.SpatialPresaturation);
                if (sp != null && !"NONE".equals(sp))
                    list.add("SP");
                String sss = sf.getString(Tag.SpectrallySelectedSuppression);
                if (sss != null && sss.startsWith("FAT"))
                    list.add("FS");
                String fc = sf.getString(Tag.FlowCompensation);
                if (fc != null && !"NONE".equals(fc))
                    list.add("FC");
                sf.setString(Tag.ScanOptions, VR.CS, list.toArray(new String[list.size()]));
            }
        },

        /**
         * Implementation for Enhanced XA Image Storage extraction.
         */
        EnhancedXAImageExtractor(UID.XRayAngiographicImageStorage.uid, true),

        /**
         * Implementation for Enhanced XRF Image Storage extraction.
         */
        EnhancedXRFImageExtractor(UID.XRayRadiofluoroscopicImageStorage.uid, true),

        /**
         * Implementation for Enhanced PET Image Storage extraction.
         */
        EnhancedPETImageExtractor(UID.PositronEmissionTomographyImageStorage.uid, true),

        /**
         * Implementation for X-Ray 3D Angiographic Image Storage extraction.
         */
        XRay3DAngiographicImageExtractor(UID.XRay3DAngiographicImageStorage.uid, true),

        /**
         * Implementation for X-Ray 3D Craniofacial Image Storage extraction.
         */
        XRay3DCraniofacialImageStorage(UID.XRay3DCraniofacialImageStorage.uid, true),

        /**
         * Implementation for Breast Tomosynthesis Image Storage extraction.
         */
        BreastTomosynthesisImageStorage(UID.BreastTomosynthesisImageStorage.uid, true),

        /**
         * Implementation for Ophthalmic Tomography Image Storage extraction.
         */
        OphthalmicTomographyImageStorage(UID.OphthalmicTomographyImageStorage.uid, true),

        /**
         * Implementation for Nuclear Medicine Image Storage extraction.
         */
        NuclearMedicineImageExtractor(UID.NuclearMedicineImageStorage.uid, false),

        /**
         * Implementation for Ultrasound Multi-Frame Image Storage extraction.
         */
        UltrasoundMultiFrameImageExtractor(UID.UltrasoundImageStorage.uid, false),

        /**
         * Implementation for Multi-Frame Grayscale Byte Secondary Capture Image Storage extraction.
         */
        MultiFrameGrayscaleByteSecondaryCaptureImageExtractor(UID.SecondaryCaptureImageStorage.uid, false),

        /**
         * Implementation for Multi-Frame Grayscale Word Secondary Capture Image Storage extraction.
         */
        MultiFrameGrayscaleWordSecondaryCaptureImageExtractor(UID.SecondaryCaptureImageStorage.uid, false),

        /**
         * Implementation for Multi-Frame True Color Secondary Capture Image Storage extraction.
         */
        MultiFrameTrueColorSecondaryCaptureImageExtractor(UID.SecondaryCaptureImageStorage.uid, false),

        /**
         * Implementation for X-Ray Angiographic Image Storage extraction.
         */
        XRayAngiographicImageExtractor(UID.XRayAngiographicImageStorage.uid, false),

        /**
         * Implementation for X-Ray Radiofluoroscopic Image Storage extraction.
         */
        XRayRadiofluoroscopicImageExtractor(UID.XRayRadiofluoroscopicImageStorage.uid, false),

        /**
         * Implementation for RT Image Storage extraction.
         */
        RTImageExtractor(UID.RTImageStorage.uid, false);

        /**
         * The sfcuid value.
         */
        private final String sfcuid;

        /**
         * The enhanced value.
         */
        private final boolean enhanced;

        /**
         * Creates a new instance.
         *
         * @param sfcuid   the sfcuid.
         * @param enhanced the enhanced.
         */
        Impl(String sfcuid, boolean enhanced) {
            this.sfcuid = sfcuid;
            this.enhanced = enhanced;
        }

        /**
         * Extracts the extract.
         *
         * @param mfe   the mfe.
         * @param emf   the emf.
         * @param frame the frame.
         * @return the operation result.
         */
        Attributes extract(MultiframeExtractor mfe, Attributes emf, int frame) {
            return mfe.extract(emf, frame, sfcuid, enhanced);
        }

    }

}
