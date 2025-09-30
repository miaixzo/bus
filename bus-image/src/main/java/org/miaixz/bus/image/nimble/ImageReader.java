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

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.SupplierEx;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.BulkData;
import org.miaixz.bus.image.galaxy.data.Fragments;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.BulkDataDescriptor;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.metric.Editable;
import org.miaixz.bus.image.nimble.codec.TransferSyntaxType;
import org.miaixz.bus.image.nimble.codec.jpeg.JPEGParser;
import org.miaixz.bus.image.nimble.opencv.ImageCV;
import org.miaixz.bus.image.nimble.opencv.ImageConversion;
import org.miaixz.bus.image.nimble.opencv.ImageProcessor;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.miaixz.bus.image.nimble.stream.*;
import org.miaixz.bus.logger.Logger;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.osgi.OpenCVNativeLoader;

/**
 * DICOM图像读取器，用于从DICOM对象中读取图像数据。
 *
 * <p>
 * 该类支持包含像素数据的所有DICOM对象，使用OpenCV本地库来读取压缩和未压缩的像素数据。 它提供了多种图像读取方法，包括原始图像读取、模态LUT应用、VOI LUT应用等。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageReader extends javax.imageio.ImageReader {

    /**
     * 需要批量处理的DICOM标签集合
     */
    public static Set<Integer> BULK_TAGS = Set.of(
            Tag.PixelDataProviderURL,
            Tag.AudioSampleData,
            Tag.CurveData,
            Tag.SpectroscopyData,
            Tag.RedPaletteColorLookupTableData,
            Tag.GreenPaletteColorLookupTableData,
            Tag.BluePaletteColorLookupTableData,
            Tag.AlphaPaletteColorLookupTableData,
            Tag.LargeRedPaletteColorLookupTableData,
            Tag.LargeGreenPaletteColorLookupTableData,
            Tag.LargeBluePaletteColorLookupTableData,
            Tag.SegmentedRedPaletteColorLookupTableData,
            Tag.SegmentedGreenPaletteColorLookupTableData,
            Tag.SegmentedBluePaletteColorLookupTableData,
            Tag.SegmentedAlphaPaletteColorLookupTableData,
            Tag.OverlayData,
            Tag.EncapsulatedDocument,
            Tag.FloatPixelData,
            Tag.DoubleFloatPixelData,
            Tag.PixelData);

    /**
     * 批量数据描述符
     */
    public static final BulkDataDescriptor BULK_DATA_DESCRIPTOR = (itemPointer, privateCreator, tag, vr, length) -> {
        int tagNormalized = Tag.normalizeRepeatingGroup(tag);
        if (tagNormalized == Tag.WaveformData) {
            return itemPointer.size() == 1 && itemPointer.get(0).sequenceTag == Tag.WaveformSequence;
        } else if (BULK_TAGS.contains(tagNormalized)) {
            return itemPointer.isEmpty();
        }
        if (Tag.isPrivateTag(tag)) {
            return length > 1000; // 不将超过1KB的私有值读入内存
        }
        return switch (vr) {
            case OB, OD, OF, OL, OW, UN -> length > 64;
            default -> false;
        };
    };

    /**
     * 存储系列UID到浮点图像转换状态的映射
     */
    private static final Map<String, Boolean> series2FloatImages = new ConcurrentHashMap<>();
    /**
     * 是否允许浮点图像转换
     */
    private static boolean allowFloatImageConversion = false;
    /**
     * 片段位置列表
     */
    private final ArrayList<Integer> fragmentsPositions = new ArrayList<>();
    /**
     * 带图像描述符的字节数据
     */
    private BytesWithImageDescriptor bdis;
    /**
     * DICOM图像文件输入流
     */
    private ImageFileInputStream dis;

    static {
        // 加载本地OpenCV库
        OpenCVNativeLoader loader = new OpenCVNativeLoader();
        loader.init();
    }

    /**
     * 构造方法
     *
     * @param originatingProvider 图像读取器服务提供者
     */
    public ImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    /**
     * 判断是否为YBR颜色模型
     *
     * @param channel 通道
     * @param pmi     光度解释
     * @param param   图像读取参数
     * @return 如果是YBR颜色模型返回true，否则返回false
     * @throws IOException 如果发生I/O错误
     */
    private static boolean isYbrModel(SeekableByteChannel channel, Photometric pmi, ImageReadParam param)
            throws IOException {
        JPEGParser parser = new JPEGParser(channel);
        String tsuid = null;
        try {
            tsuid = parser.getTransferSyntaxUID();
        } catch (InternalException e) {
            Logger.warn("Cannot parse jpeg type", e);
        }
        if (tsuid != null && !TransferSyntaxType.isLossyCompression(tsuid)) {
            return false;
        }
        boolean keepRgbForLossyJpeg;
        if (param == null) {
            keepRgbForLossyJpeg = false;
        } else {
            keepRgbForLossyJpeg = param.getKeepRgbForLossyJpeg().orElse(Boolean.FALSE);
        }
        if (pmi == Photometric.RGB && !keepRgbForLossyJpeg) {
            // 当RGB带有JFIF头或不是RGB组件时（某些制造商的错误），强制JPEG基线(1.2.840.10008.1.2.4.50)转换为YBR_FULL_422颜色模型。
            return !"RGB".equals(parser.getParams().colorPhotometricInterpretation());
        }
        return false;
    }

    /**
     * 判断是否需要将YBR转换为RGB
     *
     * @param pmi        光度解释
     * @param tsuid      传输语法UID
     * @param isYbrModel 判断是否为YBR模型的函数
     * @return 如果需要转换返回true，否则返回false
     */
    private static boolean ybr2rgb(Photometric pmi, String tsuid, BooleanSupplier isYbrModel) {
        // 仅适用于IJG本地解码器的选项
        switch (pmi) {
            case MONOCHROME1:
            case MONOCHROME2:
            case PALETTE_COLOR:
            case YBR_ICT:
            case YBR_RCT:
                return false;

            default:
                break;
        }
        return switch (UID.from(tsuid)) {
            case UID.JPEGBaseline8Bit, UID.JPEGExtended12Bit, UID.JPEGSpectralSelectionNonHierarchical68, UID.JPEGFullProgressionNonHierarchical1012 -> {
                if (pmi == Photometric.RGB) {
                    yield isYbrModel.getAsBoolean();
                }
                yield true;
            }
            default -> pmi.name().startsWith("YBR");
        };
    }

    /**
     * 应用处理后释放图像的设置
     *
     * @param imageCV 图像CV对象
     * @param param   图像读取参数
     * @return 处理后的图像CV对象
     */
    public static ImageCV applyReleaseImageAfterProcessing(ImageCV imageCV, ImageReadParam param) {
        if (isReleaseImageAfterProcessing(param)) {
            imageCV.setReleasedAfterProcessing(true);
        }
        return imageCV;
    }

    /**
     * 判断是否在处理后释放图像
     *
     * @param param 图像读取参数
     * @return 如果在处理后释放图像返回true，否则返回false
     */
    public static boolean isReleaseImageAfterProcessing(ImageReadParam param) {
        return param != null && param.getReleaseImageAfterProcessing().orElse(Boolean.FALSE);
    }

    /**
     * 关闭Mat对象
     *
     * @param mat 要关闭的Mat对象
     */
    public static void closeMat(Mat mat) {
        if (mat != null) {
            mat.release();
        }
    }

    /**
     * 判断传输语法是否受支持
     *
     * @param uid 传输语法UID
     * @return 如果受支持返回true，否则返回false
     */
    public static boolean isSupportedSyntax(String uid) {
        return switch (UID.from(uid)) {
            case UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndian, UID.RLELossless, UID.JPEGBaseline8Bit, UID.JPEGExtended12Bit, UID.JPEGSpectralSelectionNonHierarchical68, UID.JPEGFullProgressionNonHierarchical1012, UID.JPEGLossless, UID.JPEGLosslessSV1, UID.JPEGLSLossless, UID.JPEGLSNearLossless, UID.JPEG2000Lossless, UID.JPEG2000, UID.JPEG2000MCLossless, UID.JPEG2000MC -> true;
            default -> false;
        };
    }

    /**
     * 设置输入源
     *
     * @param input           输入源
     * @param seekForwardOnly 是否只向前查找
     * @param ignoreMetadata  是否忽略元数据
     */
    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        resetInternalState();
        if (input instanceof ImageFileInputStream) {
            super.setInput(input, seekForwardOnly, ignoreMetadata);
            this.dis = (ImageFileInputStream) input;
            dis.setIncludeBulkData(ImageInputStream.IncludeBulkData.URI);
            dis.setBulkDataDescriptor(BULK_DATA_DESCRIPTOR);
            // 避免将pixelData复制到临时文件
            dis.setURI(dis.getPath().toUri().toString());
        } else if (input instanceof BytesWithImageDescriptor) {
            this.bdis = (BytesWithImageDescriptor) input;
        } else {
            throw new IllegalArgumentException("Unsupported inputStream: " + input.getClass().getName());
        }
    }

    /**
     * 获取图像描述符
     *
     * @return 图像描述符
     */
    public ImageDescriptor getImageDescriptor() {
        if (bdis != null)
            return bdis.getImageDescriptor();
        return dis.getImageDescriptor();
    }

    /**
     * 返回研究中的常规图像数量。不包括覆盖层。
     *
     * @param allowSearch 是否允许搜索
     * @return 图像数量
     */
    @Override
    public int getNumImages(boolean allowSearch) {
        return getImageDescriptor().getFrames();
    }

    /**
     * 获取图像宽度
     *
     * @param frameIndex 帧索引
     * @return 图像宽度
     */
    @Override
    public int getWidth(int frameIndex) {
        checkIndex(frameIndex);
        return getImageDescriptor().getColumns();
    }

    /**
     * 获取图像高度
     *
     * @param frameIndex 帧索引
     * @return 图像高度
     */
    @Override
    public int getHeight(int frameIndex) {
        checkIndex(frameIndex);
        return getImageDescriptor().getRows();
    }

    /**
     * 获取图像类型
     *
     * @param frameIndex 帧索引
     * @return 图像类型迭代器
     */
    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int frameIndex) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * 获取默认读取参数
     *
     * @return 默认读取参数
     */
    @Override
    public javax.imageio.ImageReadParam getDefaultReadParam() {
        return new ImageReadParam();
    }

    /**
     * 获取流元数据。除非没有图像或已调用getStreamMetadata并指定了像素数据后节点， 否则可能不包含像素数据后的元数据。
     *
     * @return 流元数据
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public ImageMetaData getStreamMetadata() throws IOException {
        return dis == null ? null : dis.getMetadata();
    }

    /**
     * 获取图像元数据
     *
     * @param frameIndex 帧索引
     * @return 图像元数据
     */
    @Override
    public IIOMetadata getImageMetadata(int frameIndex) {
        return null;
    }

    /**
     * 判断是否可以读取光栅
     *
     * @return 如果可以读取光栅返回true，否则返回false
     */
    @Override
    public boolean canReadRaster() {
        return true;
    }

    /**
     * 读取光栅
     *
     * @param frameIndex 帧索引
     * @param param      图像读取参数
     * @return 光栅
     */
    @Override
    public Raster readRaster(int frameIndex, javax.imageio.ImageReadParam param) {
        try {
            PlanarImage img = getPlanarImage(frameIndex, getDefaultReadParam(param));
            return ImageConversion.toBufferedImage(img).getRaster();
        } catch (Exception e) {
            Logger.error("Reading image", e);
            return null;
        }
    }

    /**
     * 读取图像
     *
     * @param frameIndex 帧索引
     * @param param      图像读取参数
     * @return 缓冲图像
     */
    @Override
    public BufferedImage read(int frameIndex, javax.imageio.ImageReadParam param) {
        try {
            PlanarImage img = getPlanarImage(frameIndex, getDefaultReadParam(param));
            return ImageConversion.toBufferedImage(img);
        } catch (Exception e) {
            Logger.error("Reading image", e);
            return null;
        }
    }

    /**
     * 获取默认读取参数
     *
     * @param param 图像读取参数
     * @return DICOM图像读取参数
     */
    protected ImageReadParam getDefaultReadParam(javax.imageio.ImageReadParam param) {
        ImageReadParam dcmParam;
        if (param instanceof ImageReadParam readParam) {
            dcmParam = readParam;
        } else {
            if (param == null) {
                dcmParam = new ImageReadParam();
            } else {
                dcmParam = new ImageReadParam(param);
            }
        }
        return dcmParam;
    }

    /**
     * 重置内部状态
     */
    private void resetInternalState() {
        IoKit.close(dis);
        dis = null;
        bdis = null;
        fragmentsPositions.clear();
    }

    /**
     * 检查索引是否有效
     *
     * @param frameIndex 帧索引
     */
    private void checkIndex(int frameIndex) {
        if (frameIndex < 0 || frameIndex >= getImageDescriptor().getFrames())
            throw new IndexOutOfBoundsException("imageIndex: " + frameIndex);
    }

    /**
     * 释放资源
     */
    @Override
    public void dispose() {
        resetInternalState();
    }

    /**
     * 判断文件中的图像是否需要从YBR转换为RGB
     *
     * @param pmi   光度解释
     * @param tsuid 传输语法UID
     * @param seg   分段输入图像流
     * @param frame 帧索引
     * @param param 图像读取参数
     * @return 如果需要转换返回true，否则返回false
     */
    private boolean fileYbr2rgb(
            Photometric pmi,
            String tsuid,
            ExtendSegmentedInputImageStream seg,
            int frame,
            ImageReadParam param) {
        BooleanSupplier isYbrModel = () -> {
            try (SeekableByteChannel channel = Files.newByteChannel(dis.getPath(), StandardOpenOption.READ)) {
                channel.position(seg.segmentPositions()[frame]);
                return isYbrModel(channel, pmi, param);
            } catch (IOException e) {
                Logger.error("Cannot read jpeg header", e);
            }
            return false;
        };
        return ybr2rgb(pmi, tsuid, isYbrModel);
    }

    /**
     * 判断字节数组中的图像是否需要从YBR转换为RGB
     *
     * @param pmi   光度解释
     * @param tsuid 传输语法UID
     * @param frame 帧索引
     * @param param 图像读取参数
     * @return 如果需要转换返回true，否则返回false
     */
    private boolean byteYbr2rgb(Photometric pmi, String tsuid, int frame, ImageReadParam param) {
        BooleanSupplier isYbrModel = () -> {
            try (SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(bdis.getBytes(frame).array())) {
                return isYbrModel(channel, pmi, param);
            } catch (Exception e) {
                Logger.error("Cannot read jpeg header", e);
            }
            return false;
        };
        return ybr2rgb(pmi, tsuid, isYbrModel);
    }

    /**
     * 获取延迟加载的平面图像列表
     *
     * @param param  图像读取参数
     * @param editor 图像编辑器
     * @return 延迟加载的平面图像供应者列表
     */
    public List<SupplierEx<PlanarImage, IOException>> getLazyPlanarImages(
            ImageReadParam param,
            Editable<PlanarImage> editor) {
        int size = getImageDescriptor().getFrames();
        List<SupplierEx<PlanarImage, IOException>> suppliers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            final int index = i;
            suppliers.add(new SupplierEx<>() {

                boolean initialized;

                @Override
                public PlanarImage get() throws IOException {
                    return delegate.get();
                }

                private synchronized PlanarImage firstTime() throws IOException {
                    if (!initialized) {
                        PlanarImage img = getPlanarImage(index, param);
                        PlanarImage value;
                        if (editor == null) {
                            value = img;
                        } else {
                            value = editor.process(img);
                            img.release();
                        }
                        delegate = () -> value;
                        initialized = true;
                    }
                    return delegate.get();
                }

                SupplierEx<PlanarImage, IOException> delegate = this::firstTime;
            });
        }
        return suppliers;
    }

    /**
     * 获取所有平面图像
     *
     * @return 平面图像列表
     * @throws IOException 如果发生I/O错误
     */
    public List<PlanarImage> getPlanarImages() throws IOException {
        return getPlanarImages(null);
    }

    /**
     * 获取所有平面图像
     *
     * @param param 图像读取参数
     * @return 平面图像列表
     * @throws IOException 如果发生I/O错误
     */
    public List<PlanarImage> getPlanarImages(ImageReadParam param) throws IOException {
        List<PlanarImage> list = new ArrayList<>();
        for (int i = 0; i < getImageDescriptor().getFrames(); i++) {
            list.add(getPlanarImage(i, param));
        }
        return list;
    }

    /**
     * 获取平面图像
     *
     * @return 平面图像
     * @throws IOException 如果发生I/O错误
     */
    public PlanarImage getPlanarImage() throws IOException {
        return getPlanarImage(0, null);
    }

    /**
     * 获取平面图像
     *
     * @param frame 帧索引
     * @param param 图像读取参数
     * @return 平面图像
     * @throws IOException 如果发生I/O错误
     */
    public PlanarImage getPlanarImage(int frame, ImageReadParam param) throws IOException {
        PlanarImage img = getRawImage(frame, param);
        ImageDescriptor desc = dis == null ? bdis.getImageDescriptor() : dis.getMetadata().getImageDescriptor();
        PlanarImage out = img;
        if (getImageDescriptor().hasPaletteColorLookupTable()) {
            if (dis == null) {
                out = RGBImageVoiLut.getRGBImageFromPaletteColorModel(out, bdis.getPaletteColorLookupTable());
            } else {
                out = RGBImageVoiLut.getRGBImageFromPaletteColorModel(out, dis.getMetadata().getDicomObject());
            }
        }
        if (param != null && param.getSourceRegion() != null) {
            out = ImageProcessor.crop(out.toMat(), param.getSourceRegion());
        }
        if (param != null && param.getSourceRenderSize() != null) {
            out = ImageProcessor.scale(out.toMat(), param.getSourceRenderSize(), Imgproc.INTER_LANCZOS4);
        }
        String seriesUID = desc.getSeriesInstanceUID();
        if (allowFloatImageConversion && StringKit.hasText(seriesUID)) {
            Boolean isFloatPixelData = series2FloatImages.get(seriesUID);
            if (isFloatPixelData != Boolean.FALSE) {
                if (isFloatPixelData == null) {
                    out = rangeOutsideLut(out, desc, frame, false);
                    series2FloatImages.put(seriesUID, CvType.depth(out.type()) == CvType.CV_32F);
                } else {
                    out = rangeOutsideLut(out, desc, frame, true);
                }
            }
        }
        if (!img.equals(out)) {
            img.release();
        }
        return out;
    }

    /**
     * 处理范围外的LUT
     *
     * @param input      输入图像
     * @param desc       图像描述符
     * @param frameIndex 帧索引
     * @param forceFloat 是否强制转换为浮点型
     * @return 处理后的图像
     */
    static PlanarImage rangeOutsideLut(PlanarImage input, ImageDescriptor desc, int frameIndex, boolean forceFloat) {
        OptionalDouble rescaleSlope = desc.getModalityLutForFrame(frameIndex).getRescaleSlope();
        if (forceFloat || rescaleSlope.isPresent()) {
            double slope = rescaleSlope.orElse(1.0);
            double intercept = desc.getModalityLutForFrame(frameIndex).getRescaleIntercept().orElse(0.0);
            Core.MinMaxLocResult minMax = ImageAdapter.getMinMaxValues(input, desc, frameIndex);
            Pair<Double, Double> rescale = getRescaleSlopeAndIntercept(slope, intercept, minMax);
            if (forceFloat || slope < 0.5 || rangeOutsideLut(rescale, desc)) {
                ImageCV dstImg = new ImageCV();
                boolean invertLUT = desc.getPhotometricInterpretation() == Photometric.MONOCHROME1;
                double alpha = slope;
                double beta = intercept;
                if (invertLUT) {
                    alpha = -slope;
                    beta = rescale.getRight() + rescale.getLeft() - intercept;
                }
                input.toImageCV().convertTo(dstImg, CvType.CV_32F, alpha, beta);
                return dstImg;
            }
        }
        return input;
    }

    /**
     * 判断是否在LUT范围外
     *
     * @param rescale 重缩放参数
     * @param desc    图像描述符
     * @return 如果在LUT范围外返回true，否则返回false
     */
    private static boolean rangeOutsideLut(Pair<Double, Double> rescale, ImageDescriptor desc) {
        boolean outputSigned = rescale.getLeft() < 0 || desc.isSigned();
        Pair<Double, Double> minMax = RGBImageVoiLut.getMinMax(desc.getBitsAllocated(), outputSigned);
        return rescale.getLeft() + 1 < minMax.getLeft() || rescale.getRight() - 1 > minMax.getRight();
    }

    /**
     * 获取重缩放斜率和截距
     *
     * @param slope     斜率
     * @param intercept 截距
     * @param minMax    最小最大值结果
     * @return 重缩放斜率和截距对
     */
    private static Pair<Double, Double> getRescaleSlopeAndIntercept(
            double slope,
            double intercept,
            Core.MinMaxLocResult minMax) {
        double min = minMax.minVal * slope + intercept;
        double max = minMax.maxVal * slope + intercept;
        return new Pair<>(Math.min(min, max), Math.max(min, max));
    }

    /**
     * 获取原始图像
     *
     * @param frame 帧索引
     * @param param 图像读取参数
     * @return 原始图像
     * @throws IOException 如果发生I/O错误
     */
    public PlanarImage getRawImage(int frame, ImageReadParam param) throws IOException {
        if (dis == null) {
            return getRawImageFromBytes(frame, param);
        } else {
            return getRawImageFromFile(frame, param);
        }
    }

    /**
     * 从文件获取原始图像
     *
     * @param frame 帧索引
     * @param param 图像读取参数
     * @return 原始图像
     * @throws IOException 如果发生I/O错误
     */
    protected PlanarImage getRawImageFromFile(int frame, ImageReadParam param) throws IOException {
        if (dis == null) {
            throw new IOException("No DicomInputStream found");
        }
        Attributes dcm = dis.getMetadata().getDicomObject();
        boolean floatPixData = false;
        VR.Holder pixeldataVR = new VR.Holder();
        Object pixdata = dcm.getValue(Tag.PixelData, pixeldataVR);
        if (pixdata == null) {
            pixdata = dcm.getValue(Tag.FloatPixelData, pixeldataVR);
            if (pixdata != null) {
                floatPixData = true;
            }
        }
        if (pixdata == null) {
            pixdata = dcm.getValue(Tag.DoubleFloatPixelData, pixeldataVR);
            if (pixdata != null) {
                floatPixData = true;
            }
        }
        ImageDescriptor desc = getImageDescriptor();
        int bitsStored = desc.getBitsStored();
        if (pixdata == null || bitsStored < 1) {
            throw new IllegalStateException("No pixel data in this DICOM object");
        }
        Fragments pixeldataFragments = null;
        BulkData bulkData = null;
        boolean bigendian = false;
        if (pixdata instanceof BulkData) {
            bulkData = (BulkData) pixdata;
            bigendian = bulkData.bigEndian();
        } else if (dcm.getString(Tag.PixelDataProviderURL) != null) {
            // TODO 处理JPIP
            // 始终是小端序：
            // http://dicom.nema.org/medical/dicom/2017b/output/chtml/part05/sect_A.6.html
        } else if (pixdata instanceof Fragments) {
            pixeldataFragments = (Fragments) pixdata;
            bigendian = pixeldataFragments.bigEndian();
        }
        ExtendSegmentedInputImageStream seg = buildSegmentedImageInputStream(frame, pixeldataFragments, bulkData);
        if (seg.segmentPositions() == null) {
            return null;
        }
        if (seg.segmentPositions().length <= frame) {
            frame = 0;
        }
        String tsuid = dis.getMetadata().getTransferSyntaxUID();
        TransferSyntaxType type = TransferSyntaxType.forUID(tsuid);
        Photometric pmi = desc.getPhotometricInterpretation();
        boolean rawData = pixeldataFragments == null || type == TransferSyntaxType.NATIVE
                || type == TransferSyntaxType.RLE;
        int dcmFlags = (type.canEncodeSigned() && desc.isSigned()) ? Imgcodecs.DICOM_FLAG_SIGNED
                : Imgcodecs.DICOM_FLAG_UNSIGNED;
        if (!rawData && fileYbr2rgb(pmi, tsuid, seg, frame, param)) {
            dcmFlags |= Imgcodecs.DICOM_FLAG_YBR;
            if (type == TransferSyntaxType.JPEG_LS) {
                dcmFlags |= Imgcodecs.DICOM_FLAG_FORCE_RGB_CONVERSION;
            }
        }
        if (bigendian) {
            dcmFlags |= Imgcodecs.DICOM_FLAG_BIGENDIAN;
        }
        if (floatPixData) {
            dcmFlags |= Imgcodecs.DICOM_FLAG_FLOAT;
        }
        if (UID.RLELossless.equals(tsuid)) {
            dcmFlags |= Imgcodecs.DICOM_FLAG_RLE;
        }
        MatOfDouble positions = null;
        MatOfDouble lengths = null;
        try {
            positions = new MatOfDouble(Arrays.stream(seg.segmentPositions()).asDoubleStream().toArray());
            lengths = new MatOfDouble(Arrays.stream(seg.segmentLengths()).asDoubleStream().toArray());
            if (rawData) {
                int bits = bitsStored <= 8 && desc.getBitsAllocated() > 8 ? 9 : bitsStored;
                int streamVR = pixeldataVR.vr.numEndianBytes();
                MatOfInt dicomparams = new MatOfInt(Imgcodecs.IMREAD_UNCHANGED, dcmFlags, desc.getColumns(),
                        desc.getRows(), Imgcodecs.DICOM_CP_UNKNOWN, desc.getSamples(), bits,
                        desc.isBanded() ? Imgcodecs.ILV_NONE : Imgcodecs.ILV_SAMPLE, streamVR);
                ImageCV imageCV = ImageCV.toImageCV(
                        Imgcodecs.dicomRawFileRead(seg.path().toString(), positions, lengths, dicomparams, pmi.name()));
                return applyReleaseImageAfterProcessing(imageCV, param);
            }
            ImageCV imageCV = ImageCV.toImageCV(
                    Imgcodecs.dicomJpgFileRead(
                            seg.path().toString(),
                            positions,
                            lengths,
                            dcmFlags,
                            Imgcodecs.IMREAD_UNCHANGED));
            return applyReleaseImageAfterProcessing(imageCV, param);
        } finally {
            closeMat(positions);
            closeMat(lengths);
        }
    }

    /**
     * 从字节数组获取原始图像
     *
     * @param frame 帧索引
     * @param param 图像读取参数
     * @return 原始图像
     * @throws IOException 如果发生I/O错误
     */
    protected PlanarImage getRawImageFromBytes(int frame, ImageReadParam param) throws IOException {
        if (bdis == null) {
            throw new IOException("No BytesWithImageDescriptor found");
        }
        ImageDescriptor desc = getImageDescriptor();
        int bitsStored = desc.getBitsStored();
        String tsuid = bdis.getTransferSyntax();
        TransferSyntaxType type = TransferSyntaxType.forUID(tsuid);
        Photometric pmi = desc.getPhotometricInterpretation();
        boolean rawData = type == TransferSyntaxType.NATIVE || type == TransferSyntaxType.RLE;
        int dcmFlags = (type.canEncodeSigned() && desc.isSigned()) ? Imgcodecs.DICOM_FLAG_SIGNED
                : Imgcodecs.DICOM_FLAG_UNSIGNED;
        if (!rawData && byteYbr2rgb(pmi, tsuid, frame, param)) {
            dcmFlags |= Imgcodecs.DICOM_FLAG_YBR;
            if (type == TransferSyntaxType.JPEG_LS) {
                dcmFlags |= Imgcodecs.DICOM_FLAG_FORCE_RGB_CONVERSION;
            }
        }
        if (bdis.bigEndian()) {
            dcmFlags |= Imgcodecs.DICOM_FLAG_BIGENDIAN;
        }
        if (bdis.floatPixelData()) {
            dcmFlags |= Imgcodecs.DICOM_FLAG_FLOAT;
        }
        if (UID.RLELossless.equals(tsuid)) {
            dcmFlags |= Imgcodecs.DICOM_FLAG_RLE;
        }
        Mat buf = null;
        try {
            ByteBuffer b = bdis.getBytes(frame);
            buf = new Mat(1, b.limit(), CvType.CV_8UC1);
            buf.put(0, 0, b.array());
            if (rawData) {
                int bits = bitsStored <= 8 && desc.getBitsAllocated() > 8 ? 9 : bitsStored; // 修复#94
                int streamVR = bdis.getPixelDataVR().numEndianBytes();
                MatOfInt dicomparams = new MatOfInt(Imgcodecs.IMREAD_UNCHANGED, dcmFlags, desc.getColumns(),
                        desc.getRows(), Imgcodecs.DICOM_CP_UNKNOWN, desc.getSamples(), bits,
                        desc.isBanded() ? Imgcodecs.ILV_NONE : Imgcodecs.ILV_SAMPLE, streamVR);
                ImageCV imageCV = ImageCV.toImageCV(Imgcodecs.dicomRawMatRead(buf, dicomparams, pmi.name()));
                return applyReleaseImageAfterProcessing(imageCV, param);
            }
            ImageCV imageCV = ImageCV.toImageCV(Imgcodecs.dicomJpgMatRead(buf, dcmFlags, Imgcodecs.IMREAD_UNCHANGED));
            return applyReleaseImageAfterProcessing(imageCV, param);
        } finally {
            closeMat(buf);
        }
    }

    /**
     * 构建分段图像输入流
     *
     * @param frameIndex 帧索引
     * @param fragments  片段
     * @param bulkData   批量数据
     * @return 分段图像输入流
     * @throws IOException 如果发生I/O错误
     */
    private ExtendSegmentedInputImageStream buildSegmentedImageInputStream(
            int frameIndex,
            Fragments fragments,
            BulkData bulkData) throws IOException {
        long[] offsets;
        int[] length;
        ImageDescriptor desc = getImageDescriptor();
        boolean hasFragments = fragments != null;
        if (!hasFragments && bulkData != null) {
            int frameLength = desc.getPhotometricInterpretation()
                    .frameLength(desc.getColumns(), desc.getRows(), desc.getSamples(), desc.getBitsAllocated());
            offsets = new long[1];
            length = new int[offsets.length];
            offsets[0] = bulkData.offset() + (long) frameIndex * frameLength;
            length[0] = frameLength;
        } else if (hasFragments) {
            int nbFragments = fragments.size();
            int numberOfFrame = desc.getFrames();
            if (numberOfFrame >= nbFragments - 1) {
                // nbFrames > nbFragments 永远不应该发生
                offsets = new long[1];
                length = new int[offsets.length];
                int index = frameIndex < nbFragments - 1 ? frameIndex + 1 : nbFragments - 1;
                BulkData b = (BulkData) fragments.get(index);
                offsets[0] = b.offset();
                length[0] = b.length();
            } else {
                if (numberOfFrame == 1) {
                    offsets = new long[nbFragments - 1];
                    length = new int[offsets.length];
                    for (int i = 0; i < length.length; i++) {
                        BulkData b = (BulkData) fragments.get(i + frameIndex + 1);
                        offsets[i] = b.offset();
                        length[i] = b.length();
                    }
                } else {
                    // 多帧图像，每帧可以有多个片段。
                    if (fragmentsPositions.isEmpty()) {
                        try (SeekableByteChannel channel = Files
                                .newByteChannel(dis.getPath(), StandardOpenOption.READ)) {
                            for (int i = 1; i < nbFragments; i++) {
                                BulkData b = (BulkData) fragments.get(i);
                                channel.position(b.offset());
                                try {
                                    new JPEGParser(channel);
                                    fragmentsPositions.add(i);
                                } catch (Exception e) {
                                    // 不是jpeg流
                                }
                            }
                        }
                    }
                    if (fragmentsPositions.size() == numberOfFrame) {
                        int start = fragmentsPositions.get(frameIndex);
                        int end = (frameIndex + 1) >= fragmentsPositions.size() ? nbFragments
                                : fragmentsPositions.get(frameIndex + 1);
                        offsets = new long[end - start];
                        length = new int[offsets.length];
                        for (int i = 0; i < offsets.length; i++) {
                            BulkData b = (BulkData) fragments.get(start + i);
                            offsets[i] = b.offset();
                            length[i] = b.length();
                        }
                    } else {
                        throw new IOException("Cannot match all the fragments to all the frames!");
                    }
                }
            }
        } else {
            throw new IOException("Neither fragments nor BulkData!");
        }
        return new ExtendSegmentedInputImageStream(dis.getPath(), offsets, length, desc);
    }

    /**
     * 添加系列到浮点图像映射
     *
     * @param seriesInstanceUID  系列实例UID
     * @param forceToFloatImages 是否强制转换为浮点图像
     */
    public static void addSeriesToFloatImages(String seriesInstanceUID, Boolean forceToFloatImages) {
        series2FloatImages.put(seriesInstanceUID, forceToFloatImages);
    }

    /**
     * 获取强制转换为浮点图像的设置
     *
     * @param seriesInstanceUID 系列实例UID
     * @return 是否强制转换为浮点图像
     */
    public static Boolean getForceToFloatImages(String seriesInstanceUID) {
        return series2FloatImages.get(seriesInstanceUID);
    }

    /**
     * 移除系列到浮点图像映射
     *
     * @param seriesInstanceUID 系列实例UID
     */
    public static void removeSeriesToFloatImages(String seriesInstanceUID) {
        series2FloatImages.remove(seriesInstanceUID);
    }

    /**
     * 允许在模态LUT的结果超出原始图像类型范围时将图像转换为浮点图像。
     *
     * <p>
     * 注意：默认情况下，不允许转换。如果转换设置为true，当系列被释放时必须调用<code>
     * removeSeriesToFloatImages()</code>。
     *
     * @param allowFloatImageConversion 是否允许转换为浮点图像
     */
    public static void setAllowFloatImageConversion(boolean allowFloatImageConversion) {
        ImageReader.allowFloatImageConversion = allowFloatImageConversion;
    }

}
