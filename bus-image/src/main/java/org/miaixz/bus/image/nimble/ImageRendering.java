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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.image.nimble.opencv.ImageCV;
import org.miaixz.bus.image.nimble.opencv.ImageProcessor;
import org.miaixz.bus.image.nimble.opencv.LookupTableCV;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.miaixz.bus.image.nimble.opencv.lut.PresentationStateLut;
import org.miaixz.bus.image.nimble.opencv.lut.WindLevelParameters;
import org.miaixz.bus.image.nimble.stream.ImageDescriptor;
import org.opencv.core.CvType;

/**
 * DICOM图像渲染类，用于处理DICOM图像的各种渲染操作。
 *
 * <p>
 * 该类提供了多种图像渲染方法，包括原始图像渲染、模态LUT应用、VOI LUT应用、 嵌入式覆盖层处理等。它支持不同数据类型的图像处理，并提供窗口/级别调整功能。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageRendering {

    /**
     * 返回应用了模态LUT且不包含嵌入式覆盖层的原始渲染图像。
     *
     * @param imageSource 源图像
     * @param desc        包含模态LUT和覆盖层信息的图像描述符
     * @param params      包含窗口/级别参数的DicomImageReadParam
     * @param frameIndex  要处理的帧索引（单帧图像为0）
     * @return 应用了模态LUT的原始渲染图像
     */
    public static PlanarImage getRawRenderedImage(final PlanarImage imageSource, ImageDescriptor desc,
            ImageReadParam params, int frameIndex) {
        PlanarImage img = getImageWithoutEmbeddedOverlay(imageSource, desc, frameIndex);
        ImageAdapter adapter = new ImageAdapter(img, desc, frameIndex);
        return getModalityLutImage(imageSource, adapter, params);
    }

    /**
     * 返回应用了模态LUT的原始渲染图像。
     *
     * @param img     源图像
     * @param adapter 包含模态LUT信息的DicomImageAdapter
     * @param params  包含窗口/级别参数的DicomImageReadParam
     * @return 应用了模态LUT的原始渲染图像
     */
    public static PlanarImage getModalityLutImage(PlanarImage img, ImageAdapter adapter, ImageReadParam params) {
        WindLevelParameters p = new WindLevelParameters(adapter, params);
        int datatype = Objects.requireNonNull(img).type();
        if (datatype >= CvType.CV_8U && datatype < CvType.CV_32S) {
            LookupTableCV modalityLookup = adapter.getModalityLookup(p, p.isInverseLut());
            return modalityLookup == null ? img.toImageCV() : modalityLookup.lookup(img.toMat());
        }
        return img;
    }

    /**
     * 返回应用了VOI LUT且不包含嵌入式覆盖层的默认渲染图像。
     *
     * @param imageSource 源图像
     * @param desc        包含VOI LUT和覆盖层信息的图像描述符
     * @param params      包含窗口/级别参数的DicomImageReadParam
     * @param frameIndex  要处理的帧索引（单帧图像为0）
     * @return 应用了VOI LUT和覆盖层的默认渲染图像
     */
    public static PlanarImage getDefaultRenderedImage(final PlanarImage imageSource, ImageDescriptor desc,
            ImageReadParam params, int frameIndex) {
        PlanarImage img = getImageWithoutEmbeddedOverlay(imageSource, desc, frameIndex);
        img = getVoiLutImage(img, desc, params, frameIndex);
        return OverlayData.getOverlayImage(imageSource, img, desc, params, frameIndex);
    }

    /**
     * 返回应用了VOI LUT的图像。
     *
     * @param imageSource 源图像
     * @param desc        包含VOI LUT信息的图像描述符
     * @param params      包含窗口/级别参数的DicomImageReadParam
     * @param frameIndex  要处理的帧索引（单帧图像为0）
     * @return 应用了VOI LUT的图像
     */
    public static PlanarImage getVoiLutImage(final PlanarImage imageSource, ImageDescriptor desc, ImageReadParam params,
            int frameIndex) {
        ImageAdapter adapter = new ImageAdapter(imageSource, desc, frameIndex);
        return getVoiLutImage(imageSource, adapter, params);
    }

    /**
     * 返回应用了VOI LUT的图像。
     *
     * @param imageSource 源图像
     * @param adapter     包含VOI LUT信息的DicomImageAdapter
     * @param params      包含窗口/级别参数的DicomImageReadParam
     * @return 应用了VOI LUT的图像
     */
    public static PlanarImage getVoiLutImage(PlanarImage imageSource, ImageAdapter adapter, ImageReadParam params) {
        WindLevelParameters p = new WindLevelParameters(adapter, params);
        int datatype = Objects.requireNonNull(imageSource).type();
        if (datatype >= CvType.CV_8U && datatype < CvType.CV_32S) {
            return getImageForByteOrShortData(imageSource, adapter, p);
        } else if (datatype >= CvType.CV_32S) {
            return getImageWithFloatOrIntData(imageSource, p, datatype);
        }
        return null;
    }

    /**
     * 处理字节或短数据的图像。
     *
     * @param imageSource 源图像
     * @param adapter     图像适配器
     * @param p           窗口/级别参数
     * @return 处理后的图像
     */
    private static ImageCV getImageForByteOrShortData(PlanarImage imageSource, ImageAdapter adapter,
            WindLevelParameters p) {
        ImageDescriptor desc = adapter.getImageDescriptor();
        LookupTableCV modalityLookup = adapter.getModalityLookup(p, p.isInverseLut());
        ImageCV imageModalityTransformed = modalityLookup == null ? imageSource.toImageCV()
                : modalityLookup.lookup(imageSource.toMat());

        /*
         * C.11.2.1.2 窗位和窗宽
         *
         * 这些属性仅用于光度解释(0028,0004)值为MONOCHROME1和MONOCHROME2的图像。 对于其他图像没有意义。
         */
        if ((!p.isAllowWinLevelOnColorImage()
                || MathKit.isEqual(p.getWindow(), 255.0) && MathKit.isEqual(p.getLevel(), 127.5))
                && !desc.getPhotometricInterpretation().isMonochrome()) {
            /*
             * 如果光度解释不是单色的，不要应用VOI LUT。这对于PALETTE_COLOR是必要的。
             */
            return imageModalityTransformed;
        }

        PresentationStateLut prDcm = p.getPresentationState();
        Optional<LookupTableCV> prLut = prDcm == null ? Optional.empty() : prDcm.getPrLut();
        LookupTableCV voiLookup = null;
        if (prLut.isEmpty() || p.getLutShape().getLookup() != null) {
            voiLookup = adapter.getVOILookup(p);
        }
        if (prLut.isEmpty()) {
            return voiLookup.lookup(imageModalityTransformed);
        }
        ImageCV imageVoiTransformed = voiLookup == null ? imageModalityTransformed
                : voiLookup.lookup(imageModalityTransformed);
        return prLut.get().lookup(imageVoiTransformed);
    }

    /**
     * 处理浮点或整型数据的图像。
     *
     * @param imageSource 源图像
     * @param p           窗口/级别参数
     * @param datatype    数据类型
     * @return 处理后的图像
     */
    private static ImageCV getImageWithFloatOrIntData(PlanarImage imageSource, WindLevelParameters p, int datatype) {
        double low = p.getLevel() - p.getWindow() / 2.0;
        double high = p.getLevel() + p.getWindow() / 2.0;
        double range = high - low;
        if (range < 1.0 && datatype == DataBuffer.TYPE_INT) {
            range = 1.0;
        }
        double slope = 255.0 / range;
        double yint = 255.0 - slope * high;
        return ImageProcessor.rescaleToByte(ImageCV.toMat(imageSource), slope, yint);
    }

    /**
     * 返回不包含嵌入式覆盖层的图像。如果图像有嵌入式覆盖层，它将清除bitsStored和highBit定义范围之外的位。
     *
     * <p>
     * 对于在覆盖数据元素(60xx,3000)中编码的覆盖层，覆盖位分配(60xx,0100)始终为1， 覆盖位位置(60xx,0102)始终为0。
     *
     * @param img        源图像
     * @param desc       包含覆盖层信息的图像描述符
     * @param frameIndex 要处理的帧索引（单帧图像为0）
     * @return 不包含嵌入式覆盖层的图像
     * @see <a href="http://dicom.nema.org/medical/dicom/current/output/chtml/part05/chapter_8.html">8.1.2
     *      覆盖数据和相关数据元素的编码</a>
     */
    public static PlanarImage getImageWithoutEmbeddedOverlay(PlanarImage img, ImageDescriptor desc, int frameIndex) {
        Objects.requireNonNull(img);
        List<EmbeddedOverlay> embeddedOverlays = Objects.requireNonNull(desc).getEmbeddedOverlay();
        if (!embeddedOverlays.isEmpty()) {
            int bitsStored = desc.getBitsStored();
            int bitsAllocated = desc.getBitsAllocated();
            if (bitsStored < desc.getBitsAllocated() && bitsAllocated >= 8 && bitsAllocated <= 16) {
                int highBit = desc.getHighBit();
                int high = highBit + 1;
                int val = (1 << high) - 1;
                if (high > bitsStored) {
                    val -= (1 << (high - bitsStored)) - 1;
                }
                // 将高于highBit和低于high-bitsStored的所有位设置为0（即bitsStored之外的所有位）
                if (high > bitsStored) {
                    desc.getModalityLutForFrame(frameIndex).adaptWithOverlayBitMask(high - bitsStored);
                }
                // 将bitsStored之外的所有位设置为0
                return ImageProcessor.bitwiseAnd(img.toMat(), val);
            }
        }
        return img;
    }

}