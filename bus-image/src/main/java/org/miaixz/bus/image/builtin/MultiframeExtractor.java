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
 * @since Java 17+
 */
public class MultiframeExtractor {

    /**
     * A map storing the implementations for different SOP Class UIDs.
     */
    private static final HashMap<String, Impl> impls = new HashMap<>(8);

    /**
     * An array of tags that should be excluded (not copied) during the extraction process.
     */
    private static final int[] EXCLUDE_TAGS = { Tag.ReferencedImageEvidenceSequence, Tag.SourceImageEvidenceSequence,
            Tag.DimensionIndexSequence, Tag.NumberOfFrames, Tag.SharedFunctionalGroupsSequence,
            Tag.PerFrameFunctionalGroupsSequence, Tag.PixelData };

    /**
     * A flag indicating whether the Series Instance UID should be preserved during extraction.
     */
    private boolean preserveSeriesInstanceUID;

    /**
     * The format string used to generate instance numbers for extracted single frames.
     */
    private String instanceNumberFormat = "%s%04d";

    /**
     * The UID mapper used to remap UIDs during extraction.
     */
    private UIDMapper uidMapper = new HashUIDMapper();

    /**
     * The accessor used to retrieve the number of frames from a DICOM dataset.
     */
    private NumberOfFramesAccessor nofAccessor = new NumberOfFramesAccessor();

    /**
     * Static initializer block to populate the {@link #impls} map with supported multi-frame SOP Class UIDs and their
     * corresponding extraction implementations.
     */
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
     * Checks if the specified SOP Class UID is supported for multi-frame extraction.
     *
     * @param cuid The SOP Class UID to check.
     * @return {@code true} if the SOP Class is supported, {@code false} otherwise.
     */
    public static boolean isSupportedSOPClass(String cuid) {
        return impls.containsKey(cuid);
    }

    /**
     * Returns the legacy single-frame SOP Class UID corresponding to a given multi-frame SOP Class UID.
     *
     * @param mfcuid The multi-frame SOP Class UID.
     * @return The legacy single-frame SOP Class UID, or {@code null} if the multi-frame SOP Class is not supported.
     */
    public static String legacySOPClassUID(String mfcuid) {
        Impl impl = impls.get(mfcuid);
        return impl != null ? impl.sfcuid : null;
    }

    /**
     * Retrieves the {@link Impl} (implementation) for a given multi-frame SOP Class UID.
     *
     * @param mfcuid The multi-frame SOP Class UID.
     * @return The {@link Impl} corresponding to the SOP Class.
     * @throws IllegalArgumentException if the SOP Class is not supported.
     */
    private static Impl implFor(String mfcuid) {
        Impl impl = impls.get(mfcuid);
        if (impl == null)
            throw new IllegalArgumentException("Unsupported SOP Class: " + mfcuid);
        return impl;
    }

    /**
     * Gets the flag indicating whether the Series Instance UID should be preserved.
     *
     * @return {@code true} if Series Instance UID is preserved, {@code false} otherwise.
     */
    public final boolean isPreserveSeriesInstanceUID() {
        return preserveSeriesInstanceUID;
    }

    /**
     * Sets the flag indicating whether the Series Instance UID should be preserved.
     *
     * @param preserveSeriesInstanceUID {@code true} to preserve Series Instance UID, {@code false} otherwise.
     */
    public final void setPreserveSeriesInstanceUID(boolean preserveSeriesInstanceUID) {
        this.preserveSeriesInstanceUID = preserveSeriesInstanceUID;
    }

    /**
     * Gets the format string used for generating instance numbers.
     *
     * @return The instance number format string.
     */
    public final String getInstanceNumberFormat() {
        return instanceNumberFormat;
    }

    /**
     * Sets the format string used for generating instance numbers. The format string must be compatible with
     * {@link String#format(String, Object...)} and accept two arguments: the original instance number string and the
     * frame index + 1.
     *
     * @param instanceNumberFormat The new instance number format string.
     * @throws IllegalArgumentException if the format string is invalid.
     */
    public final void setInstanceNumberFormat(String instanceNumberFormat) {
        String.format(instanceNumberFormat, "1", 1);
        this.instanceNumberFormat = instanceNumberFormat;
    }

    /**
     * Gets the currently configured {@link UIDMapper}.
     *
     * @return The {@link UIDMapper} instance.
     */
    public final UIDMapper getUIDMapper() {
        return uidMapper;
    }

    /**
     * Sets the {@link UIDMapper} to be used for remapping UIDs.
     *
     * @param uidMapper The new {@link UIDMapper} instance.
     * @throws NullPointerException if {@code uidMapper} is {@code null}.
     */
    public final void setUIDMapper(UIDMapper uidMapper) {
        if (uidMapper == null)
            throw new NullPointerException();
        this.uidMapper = uidMapper;
    }

    /**
     * Gets the currently configured {@link NumberOfFramesAccessor}.
     *
     * @return The {@link NumberOfFramesAccessor} instance.
     */
    public final NumberOfFramesAccessor getNumberOfFramesAccessorr() {
        return nofAccessor;
    }

    /**
     * Sets the {@link NumberOfFramesAccessor} to be used for retrieving the number of frames.
     *
     * @param accessor The new {@link NumberOfFramesAccessor} instance.
     * @throws NullPointerException if {@code accessor} is {@code null}.
     */
    public final void setNumberOfFramesAccessor(NumberOfFramesAccessor accessor) {
        if (accessor == null)
            throw new NullPointerException();
        this.nofAccessor = accessor;
    }

    /**
     * Extracts a specific frame from an enhanced multi-frame image and returns it as a legacy single-frame image.
     *
     * @param emf   The enhanced multi-frame {@link Attributes} from which to extract the frame.
     * @param frame The 0-based index of the frame to extract.
     * @return An {@link Attributes} object representing the extracted single-frame image.
     */
    public Attributes extract(Attributes emf, int frame) {
        return implFor(emf.getString(Tag.SOPClassUID)).extract(this, emf, frame);
    }

    /**
     * Extracts a specific frame from a multi-frame image. This private helper method performs the core logic of
     * extracting a single frame, handling functional groups, pixel data, and updating UIDs and instance numbers.
     *
     * @param emf      The multi-frame {@link Attributes} object.
     * @param frame    The 0-based index of the frame to extract.
     * @param cuid     The SOP Class UID for the target single-frame image.
     * @param enhanced A boolean indicating if the source image is an enhanced multi-frame image.
     * @return An {@link Attributes} object representing the extracted single-frame image.
     * @throws IllegalArgumentException if required functional group sequences are missing for enhanced images.
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
     * Adjusts the Referenced Image Sequence or Source Image Sequence in the given attributes. It converts multi-frame
     * references into multiple single-frame references based on the number of frames.
     *
     * @param attrs The {@link Attributes} object containing the sequence to adjust.
     * @param sqtag The tag of the sequence to adjust (e.g., {@link Tag#ReferencedImageSequence}).
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
     * Adds functional group attributes to the destination attributes. This method copies attributes from shared or
     * per-frame functional group sequences.
     *
     * @param dest The destination {@link Attributes} object.
     * @param fgs  The source {@link Attributes} object representing functional groups.
     */
    private void addFunctionGroups(Attributes dest, Attributes fgs) {
        dest.addSelected(fgs, Tag.ReferencedImageSequence);
        Attributes fg;
        for (int sqTag : fgs.tags())
            if (sqTag != Tag.ReferencedImageSequence && (fg = fgs.getNestedDataset(sqTag)) != null)
                dest.addAll(fg);
    }

    /**
     * Adds the pixel data for a specific frame to the destination attributes. It handles both byte array and
     * {@link BulkData} pixel data representations.
     *
     * @param dest  The destination {@link Attributes} object.
     * @param src   The source {@link Attributes} object containing the multi-frame pixel data.
     * @param frame The 0-based index of the frame whose pixel data is to be added.
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
     * Extracts pixel data for a specific frame from a {@link BulkData} object.
     *
     * @param src    The source {@link BulkData} object.
     * @param frame  The 0-based index of the frame to extract.
     * @param length The length of a single frame's pixel data in bytes.
     * @return A new {@link BulkData} object representing the extracted frame's pixel data.
     */
    private BulkData extractPixelData(BulkData src, int frame, int length) {
        return new BulkData(src.uriWithoutQuery(), src.offset() + (long) frame * length, length, src.bigEndian());
    }

    /**
     * Extracts pixel data for a specific frame from a byte array.
     *
     * @param src    The source byte array containing multi-frame pixel data.
     * @param frame  The 0-based index of the frame to extract.
     * @param length The length of a single frame's pixel data in bytes.
     * @return A byte array containing the extracted frame's pixel data.
     */
    private byte[] extractPixelData(byte[] src, int frame, int length) {
        byte[] dest = new byte[length];
        System.arraycopy(src, frame * length, dest, 0, length);
        return dest;
    }

    /**
     * Calculates the length of a single frame's pixel data in bytes.
     *
     * @param src The source {@link Attributes} object containing image dimensions and pixel information.
     * @return The calculated frame length in bytes.
     */
    private int calcFrameLength(Attributes src) {
        return src.getInt(Tag.Rows, 0) * src.getInt(Tag.Columns, 0) * (src.getInt(Tag.BitsAllocated, 8) >> 3)
                * src.getInt(Tag.NumberOfSamples, 1);
    }

    /**
     * Creates an instance number string for a single frame. It formats the instance number using the configured
     * {@link #instanceNumberFormat}.
     *
     * @param mfinstno The original multi-frame instance number string.
     * @param frame    The 0-based index of the frame.
     * @return The formatted instance number string.
     */
    private String createInstanceNumber(String mfinstno, int frame) {
        String s = String.format(instanceNumberFormat, mfinstno, frame + 1);
        return s.length() > 16 ? s.substring(s.length() - 16) : s;
    }

    /**
     * Enumeration defining implementations for extracting single frames from different multi-frame SOP Classes. Each
     * implementation specifies the target single-frame SOP Class UID and whether it's an enhanced image.
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
             * Extracts a single frame from an Enhanced MR Image and applies MR-specific corrections.
             *
             * @param mfe   The {@link MultiframeExtractor} instance.
             * @param emf   The enhanced multi-frame MR image attributes.
             * @param frame The 0-based index of the frame to extract.
             * @return The extracted single-frame MR image attributes.
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
             * Sets the Echo Time (Tag.EchoTime) in the single-frame MR image attributes. If Effective Echo Time
             * (Tag.EffectiveEchoTime) is 0, Echo Time is set to null.
             *
             * @param sf The single-frame MR image attributes.
             */
            void setEchoTime(Attributes sf) {
                double echoTime = sf.getDouble(Tag.EffectiveEchoTime, 0);
                if (echoTime == 0)
                    sf.setNull(Tag.EchoTime, VR.DS);
                else
                    sf.setDouble(Tag.EchoTime, VR.DS, echoTime);
            }

            /**
             * Sets the Scanning Sequence (Tag.ScanningSequence) in the single-frame MR image attributes based on
             * various pulse sequence related tags.
             *
             * @param sf The single-frame MR image attributes.
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
             * Sets the Sequence Variant (Tag.SequenceVariant) in the single-frame MR image attributes based on various
             * sequence related tags.
             *
             * @param sf The single-frame MR image attributes.
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
             * Sets the Scan Options (Tag.ScanOptions) in the single-frame MR image attributes based on various
             * scan-related tags and image type.
             *
             * @param sf The single-frame MR image attributes.
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
         * The SOP Class UID of the corresponding single-frame image.
         */
        private final String sfcuid;

        /**
         * Indicates whether the image is an enhanced multi-frame image.
         */
        private final boolean enhanced;

        /**
         * Constructs an {@code Impl} with the specified single-frame SOP Class UID and enhanced flag.
         *
         * @param sfcuid   The single-frame SOP Class UID.
         * @param enhanced {@code true} if it's an enhanced multi-frame image, {@code false} otherwise.
         */
        Impl(String sfcuid, boolean enhanced) {
            this.sfcuid = sfcuid;
            this.enhanced = enhanced;
        }

        /**
         * Extracts a single frame from the multi-frame image using the provided {@link MultiframeExtractor}.
         *
         * @param mfe   The {@link MultiframeExtractor} instance.
         * @param emf   The multi-frame image attributes.
         * @param frame The 0-based index of the frame to extract.
         * @return The extracted single-frame image attributes.
         */
        Attributes extract(MultiframeExtractor mfe, Attributes emf, int frame) {
            return mfe.extract(emf, frame, sfcuid, enhanced);
        }
    }

}
