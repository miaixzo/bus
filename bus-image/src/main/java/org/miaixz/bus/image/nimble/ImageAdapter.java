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
package org.miaixz.bus.image.nimble;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.image.nimble.opencv.ImageProcessor;
import org.miaixz.bus.image.nimble.opencv.LookupTableCV;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.miaixz.bus.image.nimble.opencv.lut.*;
import org.miaixz.bus.image.nimble.stream.ImageDescriptor;
import org.miaixz.bus.logger.Logger;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;

/**
 * DICOM图像适配器类，用于处理DICOM图像的各种属性和转换。
 *
 * <p>
 * 该类提供了处理DICOM图像的模态LUT、VOI LUT、窗口/级别调整等功能。 它还负责计算图像的最小/最大值，处理像素填充值，以及管理预设的窗口/级别集合。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageAdapter {

    /**
     * LUT缓存映射
     */
    private static final Map<LutParameters, LookupTableCV> LUT_Cache = new ConcurrentHashMap();

    /**
     * 图像描述符
     */
    private final ImageDescriptor desc;
    /**
     * 最小/最大值结果
     */
    private final MinMaxLocResult minMax;
    /**
     * 帧索引
     */
    private final int frameIndex;
    /**
     * 存储的位数
     */
    private int bitsStored;
    /**
     * 窗口/级别预设集合
     */
    private List<PresetWindowLevel> windowingPresetCollection = null;

    /**
     * 构造方法
     *
     * @param image      平面图像
     * @param desc       图像描述符
     * @param frameIndex 帧索引
     */
    public ImageAdapter(PlanarImage image, ImageDescriptor desc, int frameIndex) {
        int depth = CvType.depth(Objects.requireNonNull(image).type());
        this.desc = Objects.requireNonNull(desc);
        this.bitsStored = depth > CvType.CV_16S ? (int) image.elemSize1() * 8 : desc.getBitsStored();
        this.frameIndex = frameIndex;
        MinMaxLocResult minMax = desc.getMinMaxPixelValue(frameIndex);
        if (minMax == null) {
            minMax = findMinMaxValues(image, frameIndex);
            desc.setMinMaxPixelValue(frameIndex, minMax);
        }
        this.minMax = minMax;
        // Deferred calculation of image pixel transformation, because inner class Load is called from a separate
        // dedicated worker thread. Additionally, it will only be calculated once.
        // Considering the default pixel padding option is true, and inverse LUT operation is false
        getModalityLookup(null, false);
    }

    /**
     * 获取图像的最小/最大值
     *
     * @param image      平面图像
     * @param desc       图像描述符
     * @param frameIndex 帧索引
     * @return 最小/最大值结果
     */
    public static MinMaxLocResult getMinMaxValues(PlanarImage image, ImageDescriptor desc, int frameIndex) {
        MinMaxLocResult val = desc.getMinMaxPixelValue(frameIndex);
        if (val != null) {
            return val;
        }
        boolean monochrome = desc.getPhotometricInterpretation().isMonochrome();
        if (monochrome) {
            Integer paddingValue = desc.getPixelPaddingValue();
            if (paddingValue != null) {
                Integer paddingLimit = desc.getPixelPaddingRangeLimit();
                Integer paddingValueMin = (paddingLimit == null) ? paddingValue : Math.min(paddingValue, paddingLimit);
                Integer paddingValueMax = (paddingLimit == null) ? paddingValue : Math.max(paddingValue, paddingLimit);
                val = findMinMaxValues(image, paddingValueMin, paddingValueMax);
            }
        }
        // When not monochrome and no padding value, use default minimum and maximum values
        if (val == null) {
            val = ImageProcessor.findRawMinMaxValues(image, !monochrome);
        }
        return val;
    }

    /**
     * 查找图像的最小/最大值
     *
     * @param image      平面图像
     * @param frameIndex 帧索引
     * @return 最小/最大值结果
     */
    private MinMaxLocResult findMinMaxValues(PlanarImage image, int frameIndex) {
        // This function can be called multiple times from inner class Load. min and max will only be calculated once.
        MinMaxLocResult val = getMinMaxValues(image, desc, frameIndex);
        // Cannot trust SmallestImagePixelValue and LargestImagePixelValue values! So need to search for minimum and
        // maximum values
        int bitsAllocated = desc.getBitsAllocated();
        if (bitsStored < bitsAllocated) {
            boolean isSigned = desc.isSigned();
            int minInValue = isSigned ? -(1 << (bitsStored - 1)) : 0;
            int maxInValue = isSigned ? (1 << (bitsStored - 1)) - 1 : (1 << bitsStored) - 1;
            if (val.minVal < minInValue || val.maxVal > maxInValue) {
                // When image contains values outside the stored bits, bits stored will be replaced by bits allocated,
                // to have a LUT that can handle all values.
                // Before finding minimum and maximum values, overlays in pixel data should be masked.
                setBitsStored(bitsAllocated);
            }
        }
        return val;
    }

    /**
     * 从图像计算最小/最大值，排除提供的值范围
     *
     * @param paddingValueMin 要从最小值中排除的填充值
     * @param paddingValueMax 要从最大值中排除的填充值
     * @return 最小/最大值结果
     */
    private static MinMaxLocResult findMinMaxValues(
            PlanarImage image,
            Integer paddingValueMin,
            Integer paddingValueMax) {
        MinMaxLocResult val;
        if (CvType.depth(image.type()) <= CvType.CV_8S) {
            val = new MinMaxLocResult();
            val.minVal = 0.0;
            val.maxVal = 255.0;
        } else {
            val = ImageProcessor.findMinMaxValues(image.toMat(), paddingValueMin, paddingValueMax);
            // Handle case where minimum and maximum are equal, e.g., black image
            // Maximum value+1 to display correct value
            if (val != null && val.minVal == val.maxVal) {
                val.maxVal += 1.0;
            }
        }
        return val;
    }

    /**
     * 获取存储的位数
     *
     * @return 存储的位数
     */
    public int getBitsStored() {
        return bitsStored;
    }

    /**
     * 设置存储的位数
     *
     * @param bitsStored 存储的位数
     */
    public void setBitsStored(int bitsStored) {
        this.bitsStored = bitsStored;
    }

    /**
     * 获取最小/最大值结果
     *
     * @return 最小/最大值结果
     */
    public MinMaxLocResult getMinMax() {
        return minMax;
    }

    /**
     * 获取图像描述符
     *
     * @return 图像描述符
     */
    public ImageDescriptor getImageDescriptor() {
        return desc;
    }

    /**
     * 获取帧索引
     *
     * @return 帧索引
     */
    public int getFrameIndex() {
        return frameIndex;
    }

    /**
     * 获取最小分配值
     *
     * @param wl 窗口/级别表示
     * @return 最小分配值
     */
    public int getMinAllocatedValue(WlPresentation wl) {
        boolean signed = isModalityLutOutSigned(wl);
        int bitsAllocated = desc.getBitsAllocated();
        int maxValue = signed ? (1 << (bitsAllocated - 1)) - 1 : ((1 << bitsAllocated) - 1);
        return signed ? -(maxValue + 1) : 0;
    }

    /**
     * 获取最大分配值
     *
     * @param wl 窗口/级别表示
     * @return 最大分配值
     */
    public int getMaxAllocatedValue(WlPresentation wl) {
        boolean signed = isModalityLutOutSigned(wl);
        int bitsAllocated = desc.getBitsAllocated();
        return signed ? (1 << (bitsAllocated - 1)) - 1 : ((1 << bitsAllocated) - 1);
    }

    /**
     * 在使用重缩放斜率和截距进行模态像素变换的情况下， 即使像素表示是无符号的，输出范围也可能是有符号的。
     *
     * @param wl 窗口/级别表示
     * @return 如果模态像素变换的输出可以是有符号的，则返回true
     */
    public boolean isModalityLutOutSigned(WlPresentation wl) {
        boolean signed = desc.isSigned();
        return getMinValue(wl) < 0 || signed;
    }

    /**
     * @return 返回模态像素变换后的最小值，如果存在填充，则在像素填充操作之后。
     */
    public double getMinValue(WlPresentation wl) {
        return minMaxValue(true, wl);
    }

    /**
     * @return 返回模态像素变换后的最大值，如果存在填充，则在像素填充操作之后。
     */
    public double getMaxValue(WlPresentation wl) {
        return minMaxValue(false, wl);
    }

    /**
     * 计算最小或最大值
     *
     * @param minVal 是否计算最小值
     * @param wl     窗口/级别表示
     * @return 最小或最大值
     */
    private double minMaxValue(boolean minVal, WlPresentation wl) {
        Number min = pixelToRealValue(minMax.minVal, wl);
        Number max = pixelToRealValue(minMax.maxVal, wl);
        if (min == null || max == null) {
            return 0;
        }
        // Calculate minimum and maximum values, because slope may be negative
        if (minVal) {
            return Math.min(min.doubleValue(), max.doubleValue());
        }
        return Math.max(min.doubleValue(), max.doubleValue());
    }

    /**
     * 获取重缩放截距
     *
     * @param dcm DICOM对象
     * @return 重缩放截距
     */
    public double getRescaleIntercept(PresentationLutObject dcm) {
        if (dcm != null) {
            OptionalDouble prIntercept = dcm.getModalityLutModule().getRescaleIntercept();
            if (prIntercept.isPresent()) {
                return prIntercept.getAsDouble();
            }
        }
        return desc.getModalityLutForFrame(frameIndex).getRescaleIntercept().orElse(0.0);
    }

    /**
     * 获取重缩放斜率
     *
     * @param dcm DICOM对象
     * @return 重缩放斜率
     */
    public double getRescaleSlope(PresentationLutObject dcm) {
        if (dcm != null) {
            OptionalDouble prSlope = dcm.getModalityLutModule().getRescaleSlope();
            if (prSlope.isPresent()) {
                return prSlope.getAsDouble();
            }
        }
        return desc.getModalityLutForFrame(frameIndex).getRescaleSlope().orElse(1.0);
    }

    /**
     * 获取全动态范围宽度
     *
     * @param wl 窗口/级别表示
     * @return 全动态范围宽度
     */
    public double getFullDynamicWidth(WlPresentation wl) {
        return getMaxValue(wl) - getMinValue(wl);
    }

    /**
     * 获取全动态范围中心
     *
     * @param wl 窗口/级别表示
     * @return 全动态范围中心
     */
    public double getFullDynamicCenter(WlPresentation wl) {
        double minValue = getMinValue(wl);
        double maxValue = getMaxValue(wl);
        return minValue + (maxValue - minValue) / 2.f;
    }

    /**
     * @return 默认预设作为预设列表的第一个元素 注意：永远不应该返回null，因为至少有一个预设是auto
     */
    public PresetWindowLevel getDefaultPreset(WlPresentation wlp) {
        List<PresetWindowLevel> presetList = getPresetList(wlp);
        return (presetList != null && !presetList.isEmpty()) ? presetList.get(0) : null;
    }

    /**
     * 获取预设列表
     *
     * @param wl 窗口/级别表示
     * @return 预设列表
     */
    public synchronized List<PresetWindowLevel> getPresetList(WlPresentation wl) {
        return getPresetList(wl, false);
    }

    /**
     * 获取预设列表
     *
     * @param wl     窗口/级别表示
     * @param reload 是否重新加载
     * @return 预设列表
     */
    public synchronized List<PresetWindowLevel> getPresetList(WlPresentation wl, boolean reload) {
        if (minMax != null && (windowingPresetCollection == null || reload)) {
            windowingPresetCollection = PresetWindowLevel.getPresetCollection(this, "[DICOM]", wl);
        }
        return windowingPresetCollection;
    }

    /**
     * 获取预设集合大小
     *
     * @return 预设集合大小
     */
    public int getPresetCollectionSize() {
        if (windowingPresetCollection == null) {
            return 0;
        }
        return windowingPresetCollection.size();
    }

    /**
     * 获取默认LUT形状
     *
     * @param wlp 窗口/级别表示
     * @return 默认LUT形状
     */
    public LutShape getDefaultShape(WlPresentation wlp) {
        PresetWindowLevel defaultPreset = getDefaultPreset(wlp);
        return (defaultPreset != null) ? defaultPreset.getLutShape() : LutShape.LINEAR;
    }

    /**
     * 获取默认窗口宽度
     *
     * @param wlp 窗口/级别表示
     * @return 默认窗口宽度
     */
    public double getDefaultWindow(WlPresentation wlp) {
        PresetWindowLevel defaultPreset = getDefaultPreset(wlp);
        return (defaultPreset != null) ? defaultPreset.getWindow()
                : minMax == null ? 0.0 : minMax.maxVal - minMax.minVal;
    }

    /**
     * 获取默认窗口级别
     *
     * @param wlp 窗口/级别表示
     * @return 默认窗口级别
     */
    public double getDefaultLevel(WlPresentation wlp) {
        PresetWindowLevel defaultPreset = getDefaultPreset(wlp);
        if (defaultPreset != null) {
            return defaultPreset.getLevel();
        }
        if (minMax != null) {
            return minMax.minVal + (minMax.maxVal - minMax.minVal) / 2.0;
        }
        return 0.0f;
    }

    /**
     * 将像素值转换为实际值
     *
     * @param pixelValue 像素值
     * @param wlp        窗口/级别表示
     * @return 实际值
     */
    public Number pixelToRealValue(Number pixelValue, WlPresentation wlp) {
        if (pixelValue != null) {
            LookupTableCV lookup = getModalityLookup(wlp, false);
            if (lookup != null) {
                int val = pixelValue.intValue();
                if (val >= lookup.getOffset() && val < lookup.getOffset() + lookup.getNumEntries()) {
                    return lookup.lookup(0, val);
                }
            }
        }
        return pixelValue;
    }

    /**
     * DICOM PS 3.3 $C.11.1 模态LUT模块
     *
     * @param wlp              窗口/级别表示
     * @param inverseLUTAction 是否反转LUT操作
     * @return 模态LUT
     */
    public LookupTableCV getModalityLookup(WlPresentation wlp, boolean inverseLUTAction) {
        Integer paddingValue = desc.getPixelPaddingValue();
        boolean pixelPadding = wlp == null || wlp.isPixelPadding();
        PresentationLutObject pr = wlp != null && wlp.getPresentationState() instanceof PresentationLutObject
                ? (PresentationLutObject) wlp.getPresentationState()
                : null;
        LookupTableCV prModLut = (pr != null ? pr.getModalityLutModule().getLut().orElse(null) : null);
        final LookupTableCV mLUTSeq = prModLut == null ? desc.getModalityLutForFrame(frameIndex).getLut().orElse(null)
                : prModLut;
        if (mLUTSeq != null) {
            if (!pixelPadding || paddingValue == null) {
                if (minMax.minVal >= mLUTSeq.getOffset()
                        && minMax.maxVal < mLUTSeq.getOffset() + mLUTSeq.getNumEntries()) {
                    return mLUTSeq;
                } else if (prModLut == null) {
                    Logger.warn(
                            "Pixel values doesn't match to Modality LUT sequence table. So the Modality LUT is not applied.");
                }
            } else {
                Logger.warn("Cannot apply Modality LUT sequence and Pixel Padding");
            }
        }
        boolean inverseLut = isPhotometricInterpretationInverse(pr);
        if (pixelPadding) {
            inverseLut ^= inverseLUTAction;
        }
        LutParameters lutParams = getLutParameters(pixelPadding, mLUTSeq, inverseLut, pr);
        // No modality lookup table needed
        if (lutParams == null) {
            return null;
        }
        LookupTableCV modalityLookup = LUT_Cache.get(lutParams);
        if (modalityLookup != null) {
            return modalityLookup;
        }
        if (mLUTSeq != null) {
            if (mLUTSeq.getNumBands() == 1) {
                if (mLUTSeq.getDataType() == DataBuffer.TYPE_BYTE) {
                    byte[] data = mLUTSeq.getByteData(0);
                    if (data != null) {
                        modalityLookup = new LookupTableCV(data, mLUTSeq.getOffset(0));
                    }
                } else {
                    short[] data = mLUTSeq.getShortData(0);
                    if (data != null) {
                        modalityLookup = new LookupTableCV(data, mLUTSeq.getOffset(0),
                                mLUTSeq.getData() instanceof DataBufferUShort);
                    }
                }
            }
            if (modalityLookup == null) {
                modalityLookup = mLUTSeq;
            }
        } else {
            modalityLookup = RGBImageVoiLut.createRescaleRampLut(lutParams);
        }
        if (desc.getPhotometricInterpretation().isMonochrome()) {
            RGBImageVoiLut.applyPixelPaddingToModalityLUT(modalityLookup, lutParams);
        }
        LUT_Cache.put(lutParams, modalityLookup);
        return modalityLookup;
    }

    /**
     * 判断光度解释是否需要反转
     *
     * @param pr 表现状态LUT
     * @return 如果需要反转返回true，否则返回false
     */
    public boolean isPhotometricInterpretationInverse(PresentationStateLut pr) {
        Optional<String> prLUTShape = pr == null ? Optional.empty() : pr.getPrLutShapeMode();
        Photometric p = desc.getPhotometricInterpretation();
        return prLUTShape.map("INVERSE"::equals).orElseGet(() -> p == Photometric.MONOCHROME1);
    }

    /**
     * 获取LUT参数
     *
     * @param pixelPadding       是否像素填充
     * @param mLUTSeq            模态LUT序列
     * @param inversePaddingMLUT 是否反转填充模态LUT
     * @param pr                 表现LUT对象
     * @return LUT参数
     */
    public LutParameters getLutParameters(
            boolean pixelPadding,
            LookupTableCV mLUTSeq,
            boolean inversePaddingMLUT,
            PresentationLutObject pr) {
        Integer paddingValue = desc.getPixelPaddingValue();
        boolean isSigned = desc.isSigned();
        double intercept = getRescaleIntercept(pr);
        double slope = getRescaleSlope(pr);
        // No modality lookup table needed
        if (bitsStored > 16
                || (MathKit.isEqual(slope, 1.0) && MathKit.isEqualToZero(intercept) && paddingValue == null)) {
            return null;
        }
        Integer paddingLimit = desc.getPixelPaddingRangeLimit();
        boolean outputSigned = false;
        int bitsOutputLut;
        if (mLUTSeq == null) {
            double minValue = minMax.minVal * slope + intercept;
            double maxValue = minMax.maxVal * slope + intercept;
            bitsOutputLut = Integer.SIZE - Integer.numberOfLeadingZeros((int) Math.round(maxValue - minValue));
            outputSigned = minValue < 0 || isSigned;
            if (outputSigned && bitsOutputLut <= 8) {
                // Allow 8-bit images to handle negative values
                bitsOutputLut = 9;
            }
        } else {
            bitsOutputLut = mLUTSeq.getDataType() == DataBuffer.TYPE_BYTE ? 8 : 16;
        }
        return new LutParameters(intercept, slope, pixelPadding, paddingValue, paddingLimit, bitsStored, isSigned,
                outputSigned, bitsOutputLut, inversePaddingMLUT);
    }

    /**
     * @return 8位无符号查找表
     *
     * @param wl 窗口/级别参数
     */
    public LookupTableCV getVOILookup(WlParams wl) {
        if (wl == null || wl.getLutShape() == null) {
            return null;
        }
        int minValue;
        int maxValue;
        // When pixel padding is activated, VOI LUT must be extended to minimum stored bit value (MONOCHROME2) and
        // maximum stored bit value (MONOCHROME1). See C.7.5.1.1.2
        if (wl.isFillOutsideLutRange()
                || (desc.getPixelPaddingValue() != null && desc.getPhotometricInterpretation().isMonochrome())) {
            minValue = getMinAllocatedValue(wl);
            maxValue = getMaxAllocatedValue(wl);
        } else {
            minValue = (int) wl.getLevelMin();
            maxValue = (int) wl.getLevelMax();
        }
        return RGBImageVoiLut.createVoiLut(
                wl.getLutShape(),
                wl.getWindow(),
                wl.getLevel(),
                minValue,
                maxValue,
                8,
                false,
                isPhotometricInterpretationInverse(wl.getPresentationState()));
    }

}
