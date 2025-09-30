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
 * 多帧提取器类，用于从增强型多帧DICOM图像中提取单帧图像。 该类支持多种DICOM SOP类的多帧图像提取，包括CT、MR、XA、XRF、PET、X射线3D血管造影、
 * 核医学、超声多帧、多帧灰度字节/字词/真色二次捕获、X射线血管造影、X射线透视和放射治疗图像等。 提取过程中会处理功能组序列、像素数据、引用图像序列等，并生成对应的旧式单帧DICOM图像。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MultiframeExtractor {

    /**
     * 实现映射表，存储SOP类UID与对应的实现
     */
    private static final HashMap<String, Impl> impls = new HashMap<>(8);

    /**
     * 排除标签数组，这些标签在提取过程中不会被复制到目标图像
     */
    private static final int[] EXCLUDE_TAGS = { Tag.ReferencedImageEvidenceSequence, Tag.SourceImageEvidenceSequence,
            Tag.DimensionIndexSequence, Tag.NumberOfFrames, Tag.SharedFunctionalGroupsSequence,
            Tag.PerFrameFunctionalGroupsSequence, Tag.PixelData };

    /**
     * 是否保留序列实例UID标志
     */
    private boolean preserveSeriesInstanceUID;

    /**
     * 实例编号格式字符串
     */
    private String instanceNumberFormat = "%s%04d";

    /**
     * UID映射器
     */
    private UIDMapper uidMapper = new HashUIDMapper();

    /**
     * 帧数访问器
     */
    private NumberOfFramesAccessor nofAccessor = new NumberOfFramesAccessor();

    // 初始化实现映射表
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
     * 检查指定的SOP类是否支持多帧提取
     *
     * @param cuid SOP类UID
     * @return 如果支持则返回true，否则返回false
     */
    public static boolean isSupportedSOPClass(String cuid) {
        return impls.containsKey(cuid);
    }

    /**
     * 获取多帧SOP类对应的旧式单帧SOP类UID
     *
     * @param mfcuid 多帧SOP类UID
     * @return 旧式单帧SOP类UID，如果不支持则返回null
     */
    public static String legacySOPClassUID(String mfcuid) {
        Impl impl = impls.get(mfcuid);
        return impl != null ? impl.sfcuid : null;
    }

    /**
     * 获取指定多帧SOP类UID对应的实现
     *
     * @param mfcuid 多帧SOP类UID
     * @return 对应的实现
     * @throws IllegalArgumentException 如果不支持的SOP类
     */
    private static Impl implFor(String mfcuid) {
        Impl impl = impls.get(mfcuid);
        if (impl == null)
            throw new IllegalArgumentException("Unsupported SOP Class: " + mfcuid);
        return impl;
    }

    /**
     * 获取是否保留序列实例UID
     *
     * @return 如果保留序列实例UID则返回true，否则返回false
     */
    public final boolean isPreserveSeriesInstanceUID() {
        return preserveSeriesInstanceUID;
    }

    /**
     * 设置是否保留序列实例UID
     *
     * @param preserveSeriesInstanceUID 是否保留序列实例UID
     */
    public final void setPreserveSeriesInstanceUID(boolean preserveSeriesInstanceUID) {
        this.preserveSeriesInstanceUID = preserveSeriesInstanceUID;
    }

    /**
     * 获取实例编号格式字符串
     *
     * @return 实例编号格式字符串
     */
    public final String getInstanceNumberFormat() {
        return instanceNumberFormat;
    }

    /**
     * 设置实例编号格式字符串
     *
     * @param instanceNumberFormat 实例编号格式字符串
     * @throws IllegalArgumentException 如果格式字符串无效
     */
    public final void setInstanceNumberFormat(String instanceNumberFormat) {
        String.format(instanceNumberFormat, "1", 1);
        this.instanceNumberFormat = instanceNumberFormat;
    }

    /**
     * 获取UID映射器
     *
     * @return UID映射器
     */
    public final UIDMapper getUIDMapper() {
        return uidMapper;
    }

    /**
     * 设置UID映射器
     *
     * @param uidMapper UID映射器
     * @throws NullPointerException 如果uidMapper为null
     */
    public final void setUIDMapper(UIDMapper uidMapper) {
        if (uidMapper == null)
            throw new NullPointerException();
        this.uidMapper = uidMapper;
    }

    /**
     * 获取帧数访问器
     *
     * @return 帧数访问器
     */
    public final NumberOfFramesAccessor getNumberOfFramesAccessorr() {
        return nofAccessor;
    }

    /**
     * 设置帧数访问器
     *
     * @param accessor 帧数访问器
     * @throws NullPointerException 如果accessor为null
     */
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
        return implFor(emf.getString(Tag.SOPClassUID)).extract(this, emf, frame);
    }

    /**
     * 从多帧图像中提取指定帧
     *
     * @param emf      多帧图像
     * @param frame    帧索引（从0开始）
     * @param cuid     目标SOP类UID
     * @param enhanced 是否为增强型多帧图像
     * @return 提取的单帧图像
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
     * 调整引用图像序列
     *
     * @param attrs 属性集
     * @param sqtag 序列标签
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
     * 添加功能组到目标属性集
     *
     * @param dest 目标属性集
     * @param fgs  功能组
     */
    private void addFunctionGroups(Attributes dest, Attributes fgs) {
        dest.addSelected(fgs, Tag.ReferencedImageSequence);
        Attributes fg;
        for (int sqTag : fgs.tags())
            if (sqTag != Tag.ReferencedImageSequence && (fg = fgs.getNestedDataset(sqTag)) != null)
                dest.addAll(fg);
    }

    /**
     * 添加像素数据到目标属性集
     *
     * @param dest  目标属性集
     * @param src   源属性集
     * @param frame 帧索引
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
     * 从大数据中提取像素数据
     *
     * @param src    源大数据
     * @param frame  帧索引
     * @param length 帧长度
     * @return 提取的像素数据
     */
    private BulkData extractPixelData(BulkData src, int frame, int length) {
        return new BulkData(src.uriWithoutQuery(), src.offset() + (long) frame * length, length, src.bigEndian());
    }

    /**
     * 从字节数组中提取像素数据
     *
     * @param src    源字节数组
     * @param frame  帧索引
     * @param length 帧长度
     * @return 提取的像素数据
     */
    private byte[] extractPixelData(byte[] src, int frame, int length) {
        byte[] dest = new byte[length];
        System.arraycopy(src, frame * length, dest, 0, length);
        return dest;
    }

    /**
     * 计算帧长度
     *
     * @param src 源属性集
     * @return 帧长度（字节数）
     */
    private int calcFrameLength(Attributes src) {
        return src.getInt(Tag.Rows, 0) * src.getInt(Tag.Columns, 0) * (src.getInt(Tag.BitsAllocated, 8) >> 3)
                * src.getInt(Tag.NumberOfSamples, 1);
    }

    /**
     * 创建实例编号
     *
     * @param mfinstno 多帧实例编号
     * @param frame    帧索引
     * @return 实例编号字符串
     */
    private String createInstanceNumber(String mfinstno, int frame) {
        String s = String.format(instanceNumberFormat, mfinstno, frame + 1);
        return s.length() > 16 ? s.substring(s.length() - 16) : s;
    }

    /**
     * 实现枚举，定义了不同SOP类的多帧提取实现
     */
    private enum Impl {

        /**
         * 增强CT图像提取器
         */
        EnhancedCTImageExtractor(UID.CTImageStorage.uid, true),

        /**
         * 增强MR图像提取器
         */
        EnhancedMRImageExtractor(UID.MRImageStorage.uid, true) {

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
             * 设置回波时间
             *
             * @param sf 单帧图像属性
             */
            void setEchoTime(Attributes sf) {
                double echoTime = sf.getDouble(Tag.EffectiveEchoTime, 0);
                if (echoTime == 0)
                    sf.setNull(Tag.EchoTime, VR.DS);
                else
                    sf.setDouble(Tag.EchoTime, VR.DS, echoTime);
            }

            /**
             * 设置扫描序列
             *
             * @param sf 单帧图像属性
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
             * 设置序列变体
             *
             * @param sf 单帧图像属性
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
             * 设置扫描选项
             *
             * @param sf 单帧图像属性
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
         * 增强XA图像提取器
         */
        EnhancedXAImageExtractor(UID.XRayAngiographicImageStorage.uid, true),

        /**
         * 增强XRF图像提取器
         */
        EnhancedXRFImageExtractor(UID.XRayRadiofluoroscopicImageStorage.uid, true),

        /**
         * 增强PET图像提取器
         */
        EnhancedPETImageExtractor(UID.PositronEmissionTomographyImageStorage.uid, true),

        /**
         * X射线3D血管造影图像提取器
         */
        XRay3DAngiographicImageExtractor(UID.XRay3DAngiographicImageStorage.uid, true),

        /**
         * X射线3D颅面图像提取器
         */
        XRay3DCraniofacialImageStorage(UID.XRay3DCraniofacialImageStorage.uid, true),

        /**
         * 乳房断层合成图像提取器
         */
        BreastTomosynthesisImageStorage(UID.BreastTomosynthesisImageStorage.uid, true),

        /**
         * 眼科断层图像提取器
         */
        OphthalmicTomographyImageStorage(UID.OphthalmicTomographyImageStorage.uid, true),

        /**
         * 核医学图像提取器
         */
        NuclearMedicineImageExtractor(UID.NuclearMedicineImageStorage.uid, false),

        /**
         * 超声多帧图像提取器
         */
        UltrasoundMultiFrameImageExtractor(UID.UltrasoundImageStorage.uid, false),

        /**
         * 多帧灰度字节二次捕获图像提取器
         */
        MultiFrameGrayscaleByteSecondaryCaptureImageExtractor(UID.SecondaryCaptureImageStorage.uid, false),

        /**
         * 多帧灰度字词二次捕获图像提取器
         */
        MultiFrameGrayscaleWordSecondaryCaptureImageExtractor(UID.SecondaryCaptureImageStorage.uid, false),

        /**
         * 多帧真色二次捕获图像提取器
         */
        MultiFrameTrueColorSecondaryCaptureImageExtractor(UID.SecondaryCaptureImageStorage.uid, false),

        /**
         * X射线血管造影图像提取器
         */
        XRayAngiographicImageExtractor(UID.XRayAngiographicImageStorage.uid, false),

        /**
         * X射线透视图像提取器
         */
        XRayRadiofluoroscopicImageExtractor(UID.XRayRadiofluoroscopicImageStorage.uid, false),

        /**
         * 放射治疗图像提取器
         */
        RTImageExtractor(UID.RTImageStorage.uid, false);

        /**
         * 单帧SOP类UID
         */
        private final String sfcuid;

        /**
         * 是否为增强型多帧图像
         */
        private final boolean enhanced;

        /**
         * 构造一个实现
         *
         * @param sfcuid   单帧SOP类UID
         * @param enhanced 是否为增强型多帧图像
         */
        Impl(String sfcuid, boolean enhanced) {
            this.sfcuid = sfcuid;
            this.enhanced = enhanced;
        }

        /**
         * 提取单帧图像
         *
         * @param mfe   多帧提取器
         * @param emf   增强型多帧图像
         * @param frame 帧索引
         * @return 提取的单帧图像
         */
        Attributes extract(MultiframeExtractor mfe, Attributes emf, int frame) {
            return mfe.extract(emf, frame, sfcuid, enhanced);
        }
    }

}
