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

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.osgi.OpenCVNativeLoader;

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
import org.miaixz.bus.image.nimble.opencv.ImageTransformer;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.miaixz.bus.image.nimble.stream.*;
import org.miaixz.bus.logger.Logger;

/**
 * DICOM image reader for reading image data from DICOM objects.
 * <p>
 * This class supports DICOM objects that contain pixel data and uses the OpenCV native library to read compressed and
 * uncompressed pixel data. It provides image reading operations for raw images, modality LUT application, VOI LUT
 * application, and related workflows.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImageReader extends javax.imageio.ImageReader {

    /**
     * DICOM tags that should be handled as bulk data.
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
     * Bulk data descriptor.
     */
    public static final BulkDataDescriptor BULK_DATA_DESCRIPTOR = (itemPointer, privateCreator, tag, vr, length) -> {
        int tagNormalized = Tag.normalizeRepeatingGroup(tag);
        if (tagNormalized == Tag.WaveformData) {
            return itemPointer.size() == 1 && itemPointer.get(0).sequenceTag == Tag.WaveformSequence;
        } else if (BULK_TAGS.contains(tagNormalized)) {
            return itemPointer.isEmpty();
        }
        if (Tag.isPrivateTag(tag)) {
            return length > 1000; // Do not read private values exceeding 1KB into memory
        }
        return switch (vr) {
            case OB, OD, OF, OL, OW, UN -> length > 64;
            default -> false;
        };
    };

    /**
     * Mapping from series UID to float image conversion state.
     */
    private static final Map<String, Boolean> series2FloatImages = new ConcurrentHashMap<>();

    /**
     * Whether float image conversion is allowed.
     */
    private static boolean allowFloatImageConversion = false;

    /**
     * Fragment position list.
     */
    private final ArrayList<Integer> fragmentsPositions = new ArrayList<>();

    /**
     * Byte data with image descriptor.
     */
    private BytesWithImageDescriptor bdis;

    /**
     * DICOM image file input stream.
     */
    private ImageFileInputStream dis;

    static {
        // Load local OpenCV library
        OpenCVNativeLoader loader = new OpenCVNativeLoader();
        loader.init();
    }

    /**
     * Creates a new instance.
     *
     * @param originatingProvider Image reader service provider.
     */
    public ImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    /**
     * Determines whether the image uses a YBR color model.
     *
     * @param channel Channel.
     * @param pmi     Photometric interpretation.
     * @param param   Image read parameters.
     * @return true if the image uses a YBR color model; otherwise false.
     * @throws IOException if an I/O error occurs.
     */
    private static boolean isYbrModel(SeekableByteChannel channel, Photometric pmi, ImageReadParam param)
            throws IOException {
        JPEGParser parser = new JPEGParser(channel);
        String tsuid = null;
        try {
            tsuid = parser.getTransferSyntaxUID();
        } catch (InternalException e) {
            Logger.warn(false, "Image", "Cannot parse jpeg type", e);
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
            // When RGB has JFIF header or is not RGB component (error from some manufacturers), force JPEG baseline
            // (1.2.840.10008.1.2.4.50) to convert to YBR_FULL_422 color model.
            return !"RGB".equals(parser.getParams().colorPhotometricInterpretation());
        }
        return false;
    }

    /**
     * Determines whether YBR data should be converted to RGB.
     *
     * @param pmi        Photometric interpretation.
     * @param tsuid      Transfer syntax UID.
     * @param isYbrModel Supplier that determines whether the image uses a YBR model.
     * @return true if conversion is required; otherwise false.
     */
    private static boolean ybr2rgb(Photometric pmi, String tsuid, BooleanSupplier isYbrModel) {
        // Options only applicable to IJG native decoder
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
     * Applies the release-after-processing setting to the image.
     *
     * @param imageCV ImageCV object.
     * @param param   Image read parameters.
     * @return Processed ImageCV object.
     */
    public static ImageCV applyReleaseImageAfterProcessing(ImageCV imageCV, ImageReadParam param) {
        if (isReleaseImageAfterProcessing(param)) {
            imageCV.setReleasedAfterProcessing(true);
        }
        return imageCV;
    }

    /**
     * Determines whether the image should be released after processing.
     *
     * @param param Image read parameters.
     * @return true if the image should be released after processing; otherwise false.
     */
    public static boolean isReleaseImageAfterProcessing(ImageReadParam param) {
        return param != null && param.getReleaseImageAfterProcessing().orElse(Boolean.FALSE);
    }

    /**
     * Closes the Mat object.
     *
     * @param mat Mat object to close.
     */
    public static void closeMat(Mat mat) {
        if (mat != null) {
            mat.release();
        }
    }

    /**
     * Determines whether the transfer syntax is supported.
     *
     * @param uid Transfer syntax UID.
     * @return true if the syntax is supported; otherwise false.
     */
    public static boolean isSupportedSyntax(String uid) {
        return switch (UID.from(uid)) {
            case UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndian, UID.RLELossless, UID.JPEGBaseline8Bit, UID.JPEGExtended12Bit, UID.JPEGSpectralSelectionNonHierarchical68, UID.JPEGFullProgressionNonHierarchical1012, UID.JPEGLossless, UID.JPEGLosslessSV1, UID.JPEGLSLossless, UID.JPEGLSNearLossless, UID.JPEG2000Lossless, UID.JPEG2000, UID.JPEG2000MCLossless, UID.JPEG2000MC -> true;
            default -> false;
        };
    }

    /**
     * Sets the input source.
     *
     * @param input           Input source.
     * @param seekForwardOnly Whether seeking is forward-only.
     * @param ignoreMetadata  Whether metadata is ignored.
     */
    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        resetInternalState();
        if (input instanceof ImageFileInputStream) {
            super.setInput(input, seekForwardOnly, ignoreMetadata);
            this.dis = (ImageFileInputStream) input;
            dis.setIncludeBulkData(ImageInputStream.IncludeBulkData.URI);
            dis.setBulkDataDescriptor(BULK_DATA_DESCRIPTOR);
            // Avoid copying pixelData to temporary file
            dis.setURI(dis.getPath().toUri().toString());
        } else if (input instanceof BytesWithImageDescriptor) {
            this.bdis = (BytesWithImageDescriptor) input;
        } else {
            throw new IllegalArgumentException("Unsupported inputStream: " + input.getClass().getName());
        }
    }

    /**
     * Gets the image descriptor.
     *
     * @return Image descriptor.
     */
    public ImageDescriptor getImageDescriptor() {
        if (bdis != null)
            return bdis.getImageDescriptor();
        return dis.getImageDescriptor();
    }

    /**
     * Returns the number of regular images in the study, excluding overlays.
     *
     * @param allowSearch Whether searching is allowed.
     * @return Image count.
     */
    @Override
    public int getNumImages(boolean allowSearch) {
        return getImageDescriptor().getFrames();
    }

    /**
     * Gets the image width.
     *
     * @param frameIndex Frame index.
     * @return Image width.
     */
    @Override
    public int getWidth(int frameIndex) {
        checkIndex(frameIndex);
        return getImageDescriptor().getColumns();
    }

    /**
     * Gets the image height.
     *
     * @param frameIndex Frame index.
     * @return Image height.
     */
    @Override
    public int getHeight(int frameIndex) {
        checkIndex(frameIndex);
        return getImageDescriptor().getRows();
    }

    /**
     * Gets the image types.
     *
     * @param frameIndex Frame index.
     * @return Image type iterator.
     */
    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int frameIndex) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Gets the default read parameters.
     *
     * @return Default read parameters.
     */
    @Override
    public javax.imageio.ImageReadParam getDefaultReadParam() {
        return new ImageReadParam();
    }

    /**
     * Gets the stream metadata. Metadata after pixel data may be omitted unless no image exists or getStreamMetadata
     * has been called with a node after pixel data.
     *
     * @return Stream metadata.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public ImageMetaData getStreamMetadata() throws IOException {
        return dis == null ? null : dis.getMetadata();
    }

    /**
     * Gets the image metadata.
     *
     * @param frameIndex Frame index.
     * @return Image metadata.
     */
    @Override
    public IIOMetadata getImageMetadata(int frameIndex) {
        return null;
    }

    /**
     * Determines whether rasters can be read.
     *
     * @return true if rasters can be read; otherwise false.
     */
    @Override
    public boolean canReadRaster() {
        return true;
    }

    /**
     * Reads the raster.
     *
     * @param frameIndex Frame index.
     * @param param      Image read parameters.
     * @return Raster.
     */
    @Override
    public Raster readRaster(int frameIndex, javax.imageio.ImageReadParam param) {
        try {
            PlanarImage img = getPlanarImage(frameIndex, getDefaultReadParam(param));
            return ImageConversion.toBufferedImage(img).getRaster();
        } catch (Exception e) {
            Logger.error(false, "Image", "Reading image", e);
            return null;
        }
    }

    /**
     * Reads the image.
     *
     * @param frameIndex Frame index.
     * @param param      Image read parameters.
     * @return Buffered image.
     */
    @Override
    public BufferedImage read(int frameIndex, javax.imageio.ImageReadParam param) {
        try {
            PlanarImage img = getPlanarImage(frameIndex, getDefaultReadParam(param));
            return ImageConversion.toBufferedImage(img);
        } catch (Exception e) {
            Logger.error(false, "Image", "Reading image", e);
            return null;
        }
    }

    /**
     * Gets the default read parameters.
     *
     * @param param Image read parameters.
     * @return DICOMImage read parameters.
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
     * Resets the internal state.
     */
    private void resetInternalState() {
        IoKit.close(dis);
        dis = null;
        bdis = null;
        fragmentsPositions.clear();
    }

    /**
     * Checks whether the index is valid.
     *
     * @param frameIndex Frame index.
     */
    private void checkIndex(int frameIndex) {
        if (frameIndex < 0 || frameIndex >= getImageDescriptor().getFrames())
            throw new IndexOutOfBoundsException("imageIndex: " + frameIndex);
    }

    /**
     * Releases resources.
     */
    @Override
    public void dispose() {
        resetInternalState();
    }

    /**
     * Determines whether an image read from a file should be converted from YBR to RGB.
     *
     * @param pmi   Photometric interpretation.
     * @param tsuid Transfer syntax UID.
     * @param seg   Segmented input image stream.
     * @param frame Frame index.
     * @param param Image read parameters.
     * @return true if conversion is required; otherwise false.
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
                Logger.error(false, "Image", "Cannot read jpeg header", e);
            }
            return false;
        };
        return ybr2rgb(pmi, tsuid, isYbrModel);
    }

    /**
     * Determines whether an image read from bytes should be converted from YBR to RGB.
     *
     * @param pmi   Photometric interpretation.
     * @param tsuid Transfer syntax UID.
     * @param frame Frame index.
     * @param param Image read parameters.
     * @return true if conversion is required; otherwise false.
     */
    private boolean byteYbr2rgb(Photometric pmi, String tsuid, int frame, ImageReadParam param) {
        BooleanSupplier isYbrModel = () -> {
            try (SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(bdis.getBytes(frame).array())) {
                return isYbrModel(channel, pmi, param);
            } catch (Exception e) {
                Logger.error(false, "Image", "Cannot read jpeg header", e);
            }
            return false;
        };
        return ybr2rgb(pmi, tsuid, isYbrModel);
    }

    /**
     * Gets the lazily loaded planar image list.
     *
     * @param param  Image read parameters.
     * @param editor Image editor.
     * @return List of lazily loaded planar image suppliers.
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
     * Gets all planar images.
     *
     * @return Planar image list.
     * @throws IOException if an I/O error occurs.
     */
    public List<PlanarImage> getPlanarImages() throws IOException {
        return getPlanarImages(null);
    }

    /**
     * Gets all planar images.
     *
     * @param param Image read parameters.
     * @return Planar image list.
     * @throws IOException if an I/O error occurs.
     */
    public List<PlanarImage> getPlanarImages(ImageReadParam param) throws IOException {
        List<PlanarImage> list = new ArrayList<>();
        for (int i = 0; i < getImageDescriptor().getFrames(); i++) {
            list.add(getPlanarImage(i, param));
        }
        return list;
    }

    /**
     * Gets the planar image.
     *
     * @return Planar image.
     * @throws IOException if an I/O error occurs.
     */
    public PlanarImage getPlanarImage() throws IOException {
        return getPlanarImage(0, null);
    }

    /**
     * Gets the planar image.
     *
     * @param frame Frame index.
     * @param param Image read parameters.
     * @return Planar image.
     * @throws IOException if an I/O error occurs.
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
            out = ImageTransformer.crop(out.toMat(), param.getSourceRegion());
        }
        if (param != null && param.getSourceRenderSize() != null) {
            out = ImageTransformer.scale(out.toMat(), param.getSourceRenderSize(), Imgproc.INTER_LANCZOS4);
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
     * Handles values outside the LUT range.
     *
     * @param input      Input image.
     * @param desc       Image descriptor.
     * @param frameIndex Frame index.
     * @param forceFloat Whether conversion to float is forced.
     * @return Processed image.
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
     * Determines whether values are outside the LUT range.
     *
     * @param rescale Rescale parameters.
     * @param desc    Image descriptor.
     * @return true if values are outside the LUT range; otherwise false.
     */
    private static boolean rangeOutsideLut(Pair<Double, Double> rescale, ImageDescriptor desc) {
        boolean outputSigned = rescale.getLeft() < 0 || desc.isSigned();
        Pair<Double, Double> minMax = RGBImageVoiLut.getMinMax(desc.getBitsAllocated(), outputSigned);
        return rescale.getLeft() + 1 < minMax.getLeft() || rescale.getRight() - 1 > minMax.getRight();
    }

    /**
     * Gets the rescale slope and intercept.
     *
     * @param slope     Slope.
     * @param intercept Intercept.
     * @param minMax    Minimum and maximum value result.
     * @return Rescale slope and intercept pair.
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
     * Gets the raw image.
     *
     * @param frame Frame index.
     * @param param Image read parameters.
     * @return Raw image.
     * @throws IOException if an I/O error occurs.
     */
    public PlanarImage getRawImage(int frame, ImageReadParam param) throws IOException {
        if (dis == null) {
            return getRawImageFromBytes(frame, param);
        } else {
            return getRawImageFromFile(frame, param);
        }
    }

    /**
     * Gets the raw image from a file.
     *
     * @param frame Frame index.
     * @param param Image read parameters.
     * @return Raw image.
     * @throws IOException if an I/O error occurs.
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
            // TODO Handle JPIP
            // Always little endian:
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
                ImageCV imageCV = ImageCV.fromMat(
                        Imgcodecs.dicomRawFileRead(seg.path().toString(), positions, lengths, dicomparams, pmi.name()));
                return applyReleaseImageAfterProcessing(imageCV, param);
            }
            ImageCV imageCV = ImageCV.fromMat(
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
     * Gets the raw image from bytes.
     *
     * @param frame Frame index.
     * @param param Image read parameters.
     * @return Raw image.
     * @throws IOException if an I/O error occurs.
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
                int bits = bitsStored <= 8 && desc.getBitsAllocated() > 8 ? 9 : bitsStored; // Fix #94
                int streamVR = bdis.getPixelDataVR().numEndianBytes();
                MatOfInt dicomparams = new MatOfInt(Imgcodecs.IMREAD_UNCHANGED, dcmFlags, desc.getColumns(),
                        desc.getRows(), Imgcodecs.DICOM_CP_UNKNOWN, desc.getSamples(), bits,
                        desc.isBanded() ? Imgcodecs.ILV_NONE : Imgcodecs.ILV_SAMPLE, streamVR);
                ImageCV imageCV = ImageCV.fromMat(Imgcodecs.dicomRawMatRead(buf, dicomparams, pmi.name()));
                return applyReleaseImageAfterProcessing(imageCV, param);
            }
            ImageCV imageCV = ImageCV.fromMat(Imgcodecs.dicomJpgMatRead(buf, dcmFlags, Imgcodecs.IMREAD_UNCHANGED));
            return applyReleaseImageAfterProcessing(imageCV, param);
        } finally {
            closeMat(buf);
        }
    }

    /**
     * Builds a segmented image input stream.
     *
     * @param frameIndex Frame index.
     * @param fragments  Fragments.
     * @param bulkData   Bulk data.
     * @return Segmented image input stream.
     * @throws IOException if an I/O error occurs.
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
                // nbFrames > nbFragments should never happen
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
                    // Multi-frame images, each frame can have multiple fragments.
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
                                    // Not jpeg stream
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
     * Adds a series-to-float-image mapping.
     *
     * @param seriesInstanceUID  Series instance UID.
     * @param forceToFloatImages Whether conversion to float images is forced.
     */
    public static void addSeriesToFloatImages(String seriesInstanceUID, Boolean forceToFloatImages) {
        series2FloatImages.put(seriesInstanceUID, forceToFloatImages);
    }

    /**
     * Gets the force-to-float-image setting.
     *
     * @param seriesInstanceUID Series instance UID.
     * @return Whether conversion to float images is forced.
     */
    public static Boolean getForceToFloatImages(String seriesInstanceUID) {
        return series2FloatImages.get(seriesInstanceUID);
    }

    /**
     * Removes a series-to-float-image mapping.
     *
     * @param seriesInstanceUID Series instance UID.
     */
    public static void removeSeriesToFloatImages(String seriesInstanceUID) {
        series2FloatImages.remove(seriesInstanceUID);
    }

    /**
     * Allows conversion to float images when the modality LUT result falls outside the original image type range.
     * <p>
     * Note: conversion is disabled by default. If conversion is enabled, <code>
     * removeSeriesToFloatImages()</code> must be called when the series is released.
     *
     * @param allowFloatImageConversion Whether conversion to float images is allowed.
     */
    public static void setAllowFloatImageConversion(boolean allowFloatImageConversion) {
        ImageReader.allowFloatImageConversion = allowFloatImageConversion;
    }

}
