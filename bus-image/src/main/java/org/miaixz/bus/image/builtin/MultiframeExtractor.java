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
package org.miaixz.bus.image.builtin;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class MultiframeExtractor {

    private static final HashMap<String, Impl> impls = new HashMap<>(8);
    private static final int[] EXCLUDE_TAGS = {
            Tag.ReferencedImageEvidenceSequence,
            Tag.SourceImageEvidenceSequence,
            Tag.DimensionIndexSequence,
            Tag.NumberOfFrames,
            Tag.SharedFunctionalGroupsSequence,
            Tag.PerFrameFunctionalGroupsSequence,
            Tag.PixelData};

    static {
        impls.put(UID.EnhancedCTImageStorage.uid, Impl.EnhancedCTImageExtractor);
        impls.put(UID.EnhancedMRImageStorage.uid, Impl.EnhancedMRImageExtractor);
        impls.put(UID.EnhancedXAImageStorage.uid, Impl.EnhancedXAImageExtractor);
        impls.put(UID.EnhancedXRFImageStorage.uid, Impl.EnhancedXRFImageExtractor);
        impls.put(UID.EnhancedPETImageStorage.uid, Impl.EnhancedPETImageExtractor);
        impls.put(UID.XRay3DAngiographicImageStorage.uid, Impl.XRay3DAngiographicImageExtractor);
        impls.put(UID.NuclearMedicineImageStorage.uid, Impl.NuclearMedicineImageExtractor);
        impls.put(UID.UltrasoundMultiFrameImageStorage.uid, Impl.UltrasoundMultiFrameImageExtractor);
        impls.put(UID.MultiFrameGrayscaleByteSecondaryCaptureImageStorage.uid, Impl.MultiFrameGrayscaleByteSecondaryCaptureImageExtractor);
        impls.put(UID.MultiFrameGrayscaleWordSecondaryCaptureImageStorage.uid, Impl.MultiFrameGrayscaleWordSecondaryCaptureImageExtractor);
        impls.put(UID.MultiFrameTrueColorSecondaryCaptureImageStorage.uid, Impl.MultiFrameTrueColorSecondaryCaptureImageExtractor);
        impls.put(UID.XRayAngiographicImageStorage.uid, Impl.XRayAngiographicImageExtractor);
        impls.put(UID.XRayRadiofluoroscopicImageStorage.uid, Impl.XRayRadiofluoroscopicImageExtractor);
        impls.put(UID.RTImageStorage.uid, Impl.RTImageExtractor);
    }

    private boolean preserveSeriesInstanceUID;
    private String instanceNumberFormat = "%s%04d";
    private UIDMapper uidMapper = new HashUIDMapper();
    private NumberOfFramesAccessor nofAccessor = new NumberOfFramesAccessor();

    public static boolean isSupportedSOPClass(String cuid) {
        return impls.containsKey(cuid);
    }

    public static String legacySOPClassUID(String mfcuid) {
        Impl impl = impls.get(mfcuid);
        return impl != null ? impl.sfcuid : null;
    }

    private static Impl implFor(String mfcuid) {
        Impl impl = impls.get(mfcuid);
        if (impl == null)
            throw new IllegalArgumentException(
                    "Unsupported SOP Class: " + mfcuid);
        return impl;
    }

    public final boolean isPreserveSeriesInstanceUID() {
        return preserveSeriesInstanceUID;
    }

    public final void setPreserveSeriesInstanceUID(
            boolean preserveSeriesInstanceUID) {
        this.preserveSeriesInstanceUID = preserveSeriesInstanceUID;
    }

    public final String getInstanceNumberFormat() {
        return instanceNumberFormat;
    }

    public final void setInstanceNumberFormat(String instanceNumberFormat) {
        String.format(instanceNumberFormat, "1", 1);
        this.instanceNumberFormat = instanceNumberFormat;
    }

    public final UIDMapper getUIDMapper() {
        return uidMapper;
    }

    public final void setUIDMapper(UIDMapper uidMapper) {
        if (uidMapper == null)
            throw new NullPointerException();
        this.uidMapper = uidMapper;
    }

    public final NumberOfFramesAccessor getNumberOfFramesAccessorr() {
        return nofAccessor;
    }

    public final void setNumberOfFramesAccessor(NumberOfFramesAccessor accessor) {
        if (accessor == null)
            throw new NullPointerException();
        this.nofAccessor = accessor;
    }

    /**
     * 从增强型多帧图像中提取特定帧，并将其作为对应的旧式单帧图像返回。
     *
     * @param emf   增强型多帧图像
     * @param frame 基于0的帧索引
     * @return 旧式单幅图像
     */
    public Attributes extract(Attributes emf, int frame) {
        return implFor(emf.getString(Tag.SOPClassUID))
                .extract(this, emf, frame);
    }

    private Attributes extract(Attributes emf, int frame, String cuid, boolean enhanced) {
        Attributes dest = new Attributes(emf.size() * 2);
        dest.addNotSelected(emf, EXCLUDE_TAGS);
        if (enhanced) {
            Attributes sfgs = emf.getNestedDataset(Tag.SharedFunctionalGroupsSequence);
            if (sfgs == null)
                throw new IllegalArgumentException(
                        "Missing (5200,9229) Shared Functional Groups Sequence");
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
        dest.setString(Tag.SOPInstanceUID, VR.UI, uidMapper.get(
                dest.getString(Tag.SOPInstanceUID)) + Symbol.C_DOT + (frame + 1));
        dest.setString(Tag.InstanceNumber, VR.IS,
                createInstanceNumber(dest.getString(Tag.InstanceNumber, Normal.EMPTY), frame));
        if (!preserveSeriesInstanceUID)
            dest.setString(Tag.SeriesInstanceUID, VR.UI, uidMapper.get(
                    dest.getString(Tag.SeriesInstanceUID)));
        adjustReferencedImages(dest, Tag.ReferencedImageSequence);
        adjustReferencedImages(dest, Tag.SourceImageSequence);
        return dest;
    }

    private void adjustReferencedImages(Attributes attrs, int sqtag) {
        Sequence sq = attrs.getSequence(sqtag);
        if (sq == null)
            return;

        ArrayList<Attributes> newRefs = new ArrayList<>();
        for (Iterator<Attributes> itr = sq.iterator(); itr.hasNext(); ) {
            Attributes ref = itr.next();
            String cuid = legacySOPClassUID(ref.getString(Tag.ReferencedSOPClassUID));
            if (cuid == null)
                continue;

            itr.remove();
            String iuid = uidMapper.get(ref.getString(Tag.ReferencedSOPInstanceUID));
            int[] frames = ref.getInts(Tag.ReferencedFrameNumber);
            int n = frames == null ? nofAccessor.getNumberOfFrames(iuid)
                    : frames.length;
            ref.remove(Tag.ReferencedFrameNumber);
            ref.setString(Tag.ReferencedSOPClassUID, VR.UI, cuid);
            for (int i = 0; i < n; i++) {
                Attributes newRef = new Attributes(ref);
                newRef.setString(Tag.ReferencedSOPInstanceUID, VR.UI,
                        iuid + '.' + (frames != null ? frames[i] : (i + 1)));
                newRefs.add(newRef);
            }
        }
        for (Attributes ref : newRefs)
            sq.add(ref);
    }

    private void addFunctionGroups(Attributes dest, Attributes fgs) {
        dest.addSelected(fgs, Tag.ReferencedImageSequence);
        Attributes fg;
        for (int sqTag : fgs.tags())
            if (sqTag != Tag.ReferencedImageSequence
                    && (fg = fgs.getNestedDataset(sqTag)) != null)
                dest.addAll(fg);
    }

    private void addPixelData(Attributes dest, Attributes src, int frame) {
        VR.Holder vr = new VR.Holder();
        Object pixelData = src.getValue(Tag.PixelData, vr);
        if (pixelData instanceof byte[]) {
            dest.setBytes(Tag.PixelData, vr.vr, extractPixelData(
                    (byte[]) pixelData, frame, calcFrameLength(src)));
        } else if (pixelData instanceof BulkData) {
            dest.setValue(Tag.PixelData, vr.vr, extractPixelData(
                    (BulkData) pixelData, frame, calcFrameLength(src)));
        } else {
            Fragments destFrags = dest.newFragments(Tag.PixelData, vr.vr, 2);
            destFrags.add(null);
            destFrags.add(((Fragments) pixelData).get(frame + 1));
        }
    }

    private BulkData extractPixelData(BulkData src, int frame,
                                      int length) {
        return new BulkData(src.uriWithoutOffsetAndLength(),
                src.offset() + (long) frame * length, length,
                src.bigEndian());
    }

    private byte[] extractPixelData(byte[] src, int frame, int length) {
        byte[] dest = new byte[length];
        System.arraycopy(src, frame * length, dest, 0, length);
        return dest;
    }

    private int calcFrameLength(Attributes src) {
        return src.getInt(Tag.Rows, 0)
                * src.getInt(Tag.Columns, 0)
                * (src.getInt(Tag.BitsAllocated, 8) >> 3)
                * src.getInt(Tag.NumberOfSamples, 1);
    }

    private String createInstanceNumber(String mfinstno, int frame) {
        String s = String.format(instanceNumberFormat, mfinstno, frame + 1);
        return s.length() > 16 ? s.substring(s.length() - 16) : s;
    }

    private enum Impl {
        EnhancedCTImageExtractor(UID.CTImageStorage.uid, true),
        EnhancedMRImageExtractor(UID.MRImageStorage.uid, true) {
            Attributes extract(MultiframeExtractor mfe, Attributes emf, int frame) {
                Attributes sf = super.extract(mfe, emf, frame);
                setEchoTime(sf);
                setScanningSequence(sf);
                setSequenceVariant(sf);
                setScanOptions(sf);
                return sf;
            }

            void setEchoTime(Attributes sf) {
                double echoTime = sf.getDouble(Tag.EffectiveEchoTime, 0);
                if (echoTime == 0)
                    sf.setNull(Tag.EchoTime, VR.DS);
                else
                    sf.setDouble(Tag.EchoTime, VR.DS, echoTime);
            }

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
                sf.setString(Tag.ScanningSequence, VR.CS,
                        list.toArray(new String[list.size()]));
            }

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
                sf.setString(Tag.SequenceVariant, VR.CS,
                        list.toArray(new String[list.size()]));
            }

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
                sf.setString(Tag.ScanOptions, VR.CS,
                        list.toArray(new String[list.size()]));
            }

        },
        EnhancedXAImageExtractor(UID.XRayAngiographicImageStorage.uid, true),
        EnhancedXRFImageExtractor(UID.XRayRadiofluoroscopicImageStorage.uid, true),
        EnhancedPETImageExtractor(UID.PositronEmissionTomographyImageStorage.uid, true),
        XRay3DAngiographicImageExtractor(UID.XRay3DAngiographicImageStorage.uid, true),
        NuclearMedicineImageExtractor(UID.NuclearMedicineImageStorage.uid, false),
        UltrasoundMultiFrameImageExtractor(UID.UltrasoundImageStorage.uid, false),
        MultiFrameGrayscaleByteSecondaryCaptureImageExtractor(UID.SecondaryCaptureImageStorage.uid, false),
        MultiFrameGrayscaleWordSecondaryCaptureImageExtractor(UID.SecondaryCaptureImageStorage.uid, false),
        MultiFrameTrueColorSecondaryCaptureImageExtractor(UID.SecondaryCaptureImageStorage.uid, false),
        XRayAngiographicImageExtractor(UID.XRayAngiographicImageStorage.uid, false),
        XRayRadiofluoroscopicImageExtractor(UID.XRayRadiofluoroscopicImageStorage.uid, false),
        RTImageExtractor(UID.RTImageStorage.uid, false);

        private final String sfcuid;
        private final boolean enhanced;

        Impl(String sfcuid, boolean enhanced) {
            this.sfcuid = sfcuid;
            this.enhanced = enhanced;
        }

        Attributes extract(MultiframeExtractor mfe, Attributes emf, int frame) {
            return mfe.extract(emf, frame, sfcuid, enhanced);
        }
    }

}
