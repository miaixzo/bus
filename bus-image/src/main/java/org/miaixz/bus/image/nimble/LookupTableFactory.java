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

import java.awt.image.*;

import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * 查找表工厂类，用于创建和管理DICOM图像的查找表(LUT)。
 *
 * <p>
 * 该类负责处理DICOM图像的模态LUT、VOI LUT(值感兴趣查找表)和表现LUT， 以便正确地将存储的像素值转换为显示值。它支持自动窗口设置、手动窗口设置， 以及各种DICOM图像类型的特殊处理。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LookupTableFactory {

    /**
     * X射线血管造影和X射线透视相关的SOP类UID数组
     */
    private static final String[] XA_XRF_CUIDS = { UID.XRayAngiographicImageStorage.uid,
            UID.XRayRadiofluoroscopicImageStorage.uid, UID.XRayAngiographicBiPlaneImageStorage.uid };
    /**
     * 像素强度关系关键字数组
     */
    private static final String[] LOG_DISP = { "LOG", "DISP" };
    /**
     * 存储值对象
     */
    private final StoredValue storedValue;
    /**
     * 重置斜率
     */
    private float rescaleSlope = 1;
    /**
     * 重置截距
     */
    private float rescaleIntercept = 0;
    /**
     * 模态LUT
     */
    private org.miaixz.bus.image.nimble.LookupTable modalityLUT;
    /**
     * 窗位
     */
    private float windowCenter;
    /**
     * 窗宽
     */
    private float windowWidth;
    /**
     * VOI LUT
     */
    private org.miaixz.bus.image.nimble.LookupTable voiLUT;
    /**
     * 表现LUT
     */
    private org.miaixz.bus.image.nimble.LookupTable presentationLUT;
    /**
     * 是否反转
     */
    private boolean inverse;

    /**
     * 构造方法
     *
     * @param storedValue 存储值对象
     */
    public LookupTableFactory(StoredValue storedValue) {
        this.storedValue = storedValue;
    }

    /**
     * 判断是否应用模态LUT
     *
     * @param attrs DICOM属性对象
     * @return 如果应用模态LUT返回true，否则返回false
     */
    public static boolean applyModalityLUT(Attributes attrs) {
        return !(Builder.contains(XA_XRF_CUIDS, attrs.getString(Tag.SOPClassUID))
                && Builder.contains(LOG_DISP, attrs.getString(Tag.PixelIntensityRelationship)));
    }

    /**
     * 将数据长度减半
     *
     * @param data 原始数据
     * @param hilo 高低位标志
     * @return 减半后的数据
     */
    static byte[] halfLength(byte[] data, int hilo) {
        byte[] bs = new byte[data.length >> 1];
        for (int i = 0; i < bs.length; i++)
            bs[i] = data[(i << 1) | hilo];
        return bs;
    }

    /**
     * 计算以2为底的对数
     *
     * @param value 输入值
     * @return 以2为底的对数值
     */
    private static int log2(int value) {
        int i = 0;
        while ((value >>> i) != 0)
            ++i;
        return i - 1;
    }

    /**
     * 设置模态LUT
     *
     * @param attrs DICOM属性对象
     */
    public void setModalityLUT(Attributes attrs) {
        rescaleIntercept = attrs.getFloat(Tag.RescaleIntercept, 0);
        rescaleSlope = attrs.getFloat(Tag.RescaleSlope, 1);
        modalityLUT = createLUT(storedValue, attrs.getNestedDataset(Tag.ModalityLUTSequence));
    }

    /**
     * 设置表现LUT
     *
     * @param attrs DICOM属性对象
     */
    public void setPresentationLUT(Attributes attrs) {
        setPresentationLUT(attrs, false);
    }

    /**
     * 设置表现LUT
     *
     * @param attrs                      DICOM属性对象
     * @param ignorePresentationLUTShape 是否忽略表现LUT形状
     */
    public void setPresentationLUT(Attributes attrs, boolean ignorePresentationLUTShape) {
        Attributes pLUT = attrs.getNestedDataset(Tag.PresentationLUTSequence);
        if (pLUT != null) {
            int[] desc = pLUT.getInts(Tag.LUTDescriptor);
            if (desc != null && desc.length == 3) {
                int len = desc[0] == 0 ? 0x10000 : desc[0];
                presentationLUT = createLUT(new StoredValue.Unsigned(log2(len)), resetOffset(desc),
                        pLUT.getSafeBytes(Tag.LUTData), pLUT.bigEndian());
            }
        } else {
            String pShape;
            inverse = (ignorePresentationLUTShape || (pShape = attrs.getString(Tag.PresentationLUTShape)) == null
                    ? "MONOCHROME1".equals(attrs.getString(Tag.PhotometricInterpretation))
                    : "INVERSE".equals(pShape));
        }
    }

    /**
     * 重置偏移量
     *
     * @param desc LUT描述符数组
     * @return 重置偏移量后的LUT描述符数组
     */
    private int[] resetOffset(int[] desc) {
        if (desc[1] == 0)
            return desc;
        int[] copy = desc.clone();
        copy[1] = 0;
        return copy;
    }

    /**
     * 设置窗位
     *
     * @param windowCenter 窗位值
     */
    public void setWindowCenter(float windowCenter) {
        this.windowCenter = windowCenter;
    }

    /**
     * 设置窗宽
     *
     * @param windowWidth 窗宽值
     */
    public void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;
    }

    /**
     * 设置VOI(值感兴趣)参数
     *
     * @param img          DICOM图像属性
     * @param windowIndex  窗口索引
     * @param voiLUTIndex  VOI LUT索引
     * @param preferWindow 是否优先使用窗口
     */
    public void setVOI(Attributes img, int windowIndex, int voiLUTIndex, boolean preferWindow) {
        if (img == null)
            return;
        Attributes vLUT = img.getNestedDataset(Tag.VOILUTSequence, voiLUTIndex);
        if (preferWindow || vLUT == null) {
            float[] wcs = img.getFloats(Tag.WindowCenter);
            float[] wws = img.getFloats(Tag.WindowWidth);
            if (wcs != null && wcs.length != 0 && wws != null && wws.length != 0) {
                int index = windowIndex < Math.min(wcs.length, wws.length) ? windowIndex : 0;
                windowCenter = wcs[index];
                windowWidth = wws[index];
                return;
            }
        }
        if (vLUT != null) {
            adjustVOILUTDescriptor(vLUT);
            voiLUT = createLUT(modalityLUT != null ? new StoredValue.Unsigned(modalityLUT.outBits) : storedValue, vLUT);
        }
    }

    /**
     * 调整VOI LUT描述符
     *
     * @param vLUT VOI LUT属性
     */
    private void adjustVOILUTDescriptor(Attributes vLUT) {
        int[] desc = vLUT.getInts(Tag.LUTDescriptor);
        byte[] data;
        if (desc != null && desc.length == 3 && desc[2] == 16 && (data = vLUT.getSafeBytes(Tag.LUTData)) != null) {
            int hiByte = 0;
            for (int i = vLUT.bigEndian() ? 0 : 1; i < data.length; i++, i++)
                hiByte |= data[i];
            if ((hiByte & 0x80) == 0) {
                desc[2] = 40 - Integer.numberOfLeadingZeros(hiByte & 0xFF);
                vLUT.setInt(Tag.LUTDescriptor, VR.SS, desc);
            }
        }
    }

    /**
     * 创建查找表
     *
     * @param inBits 输入位数
     * @param attrs  DICOM属性对象
     * @return 创建的查找表
     */
    private LookupTable createLUT(StoredValue inBits, Attributes attrs) {
        if (attrs == null)
            return null;
        return createLUT(inBits, attrs.getInts(Tag.LUTDescriptor), attrs.getSafeBytes(Tag.LUTData), attrs.bigEndian());
    }

    /**
     * 创建查找表
     *
     * @param inBits    输入位数
     * @param desc      LUT描述符数组
     * @param data      LUT数据
     * @param bigEndian 是否大端序
     * @return 创建的查找表
     */
    private LookupTable createLUT(StoredValue inBits, int[] desc, byte[] data, boolean bigEndian) {
        if (desc == null)
            return null;
        if (desc.length != 3)
            return null;
        int len = desc[0] == 0 ? 0x10000 : desc[0];
        int offset = (short) desc[1];
        int outBits = desc[2];
        if (data == null)
            return null;
        if (data.length == len << 1) {
            if (outBits > 8) {
                if (outBits > 16)
                    return null;
                short[] ss = new short[len];
                if (bigEndian)
                    for (int i = 0; i < ss.length; i++)
                        ss[i] = (short) ByteKit.bytesToShortBE(data, i << 1);
                else
                    for (int i = 0; i < ss.length; i++)
                        ss[i] = (short) ByteKit.bytesToShortLE(data, i << 1);
                return new ShortLookupTable(inBits, outBits, offset, ss);
            }
            // 填充高位 -> 使用低位
            data = halfLength(data, bigEndian ? 1 : 0);
        }
        if (data.length != len)
            return null;
        if (outBits > 8)
            return null;
        return new ByteLookupTable(inBits, outBits, offset, data);
    }

    /**
     * 创建查找表
     *
     * @param outBits 输出位数
     * @return 创建的查找表
     */
    public LookupTable createLUT(int outBits) {
        LookupTable lut = combineModalityVOILUT(presentationLUT != null ? log2(presentationLUT.length()) : outBits);
        if (presentationLUT != null) {
            lut = lut.combine(presentationLUT.adjustOutBits(outBits));
        } else if (inverse)
            lut.inverse();
        return lut;
    }

    /**
     * 组合模态LUT和VOI LUT
     *
     * @param outBits 输出位数
     * @return 组合后的查找表
     */
    private LookupTable combineModalityVOILUT(int outBits) {
        float m = rescaleSlope;
        float b = rescaleIntercept;
        LookupTable modalityLUT = this.modalityLUT;
        LookupTable lut = this.voiLUT;
        if (lut == null) {
            float c = windowCenter;
            float w = windowWidth;
            if (w == 0 && modalityLUT != null)
                return modalityLUT.adjustOutBits(outBits);
            int size, offset;
            StoredValue inBits = modalityLUT != null ? new StoredValue.Unsigned(modalityLUT.outBits) : storedValue;
            int minOut = 0;
            int maxOut = (1 << outBits) - 1;
            if (w != 0) {
                float M = Math.abs(m);
                size = Math.max(2, Math.round(w / M));
                offset = Math.round((c - w / 2 - b) / M);
                int minIndex = inBits.minValue() - offset;
                int maxIndex = inBits.maxValue() - offset;
                int size_1 = size - 1;
                int midIndex = size_1 / 2;
                if (minIndex > 0) {
                    offset += minIndex;
                    size -= minIndex;
                    minOut = (minIndex * maxOut + midIndex) / size_1;
                }
                if (maxIndex < size_1) {
                    size -= size_1 - maxIndex;
                    maxOut = (maxIndex * maxOut + midIndex) / size_1;
                }
            } else {
                offset = inBits.minValue();
                size = inBits.maxValue() - inBits.minValue() + 1;
            }
            lut = outBits > 8 ? new ShortLookupTable(inBits, outBits, minOut, maxOut, offset, size, m < 0)
                    : new ByteLookupTable(inBits, outBits, minOut, maxOut, offset, size, m < 0);
        } else {
            lut = lut.adjustOutBits(outBits);
        }
        return modalityLUT != null ? modalityLUT.combine(lut) : lut;
    }

    /**
     * 自动窗口设置
     *
     * @param img    DICOM图像属性
     * @param raster 图像光栅
     * @return 如果成功设置自动窗口返回true，否则返回false
     */
    public boolean autoWindowing(Attributes img, Raster raster) {
        return autoWindowing(img, raster, false);
    }

    /**
     * 自动窗口设置
     *
     * @param img           DICOM图像属性
     * @param raster        图像光栅
     * @param addAutoWindow 是否添加自动窗口信息到图像属性
     * @return 如果成功设置自动窗口返回true，否则返回false
     */
    public boolean autoWindowing(Attributes img, Raster raster, boolean addAutoWindow) {
        if (modalityLUT != null || voiLUT != null || windowWidth != 0)
            return false;
        int[] min_max = calcMinMax(raster);
        if (min_max[0] == min_max[1])
            return false;
        windowCenter = (min_max[0] + min_max[1] + 1) / 2 * rescaleSlope + rescaleIntercept;
        windowWidth = Math.abs((min_max[1] + 1 - min_max[0]) * rescaleSlope);
        if (addAutoWindow) {
            img.setFloat(Tag.WindowCenter, VR.DS, windowCenter);
            img.setFloat(Tag.WindowWidth, VR.DS, windowWidth);
        }
        return true;
    }

    /**
     * 计算光栅数据的最小值和最大值
     *
     * @param raster 图像光栅
     * @return 包含最小值和最大值的数组
     */
    private int[] calcMinMax(Raster raster) {
        ComponentSampleModel sm = (ComponentSampleModel) raster.getSampleModel();
        DataBuffer dataBuffer = raster.getDataBuffer();
        switch (dataBuffer.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            return calcMinMax(storedValue, sm, ((DataBufferByte) dataBuffer).getData());
        case DataBuffer.TYPE_USHORT:
            return calcMinMax(storedValue, sm, ((DataBufferUShort) dataBuffer).getData());
        case DataBuffer.TYPE_SHORT:
            return calcMinMax(storedValue, sm, ((DataBufferShort) dataBuffer).getData());
        default:
            throw new UnsupportedOperationException("DataBuffer: " + dataBuffer.getClass() + " not supported");
        }
    }

    /**
     * 计算字节数组的最小值和最大值
     *
     * @param storedValue 存储值对象
     * @param sm          组件采样模型
     * @param data        字节数组数据
     * @return 包含最小值和最大值的数组
     */
    private int[] calcMinMax(StoredValue storedValue, ComponentSampleModel sm, byte[] data) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        for (int y = 0; y < h; y++)
            for (int i = y * stride, end = i + w; i < end;) {
                int val = storedValue.valueOf(data[i++]);
                if (val < min)
                    min = val;
                if (val > max)
                    max = val;
            }
        return new int[] { min, max };
    }

    /**
     * 计算短整型数组的最小值和最大值
     *
     * @param storedValue 存储值对象
     * @param sm          组件采样模型
     * @param data        短整型数组数据
     * @return 包含最小值和最大值的数组
     */
    private int[] calcMinMax(StoredValue storedValue, ComponentSampleModel sm, short[] data) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        for (int y = 0; y < h; y++)
            for (int i = y * stride, end = i + w; i < end;) {
                int val = storedValue.valueOf(data[i++]);
                if (val < min)
                    min = val;
                if (val > max)
                    max = val;
            }
        return new int[] { min, max };
    }

}