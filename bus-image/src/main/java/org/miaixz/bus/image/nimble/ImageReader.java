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
package org.miaixz.bus.image.nimble;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.osgi.OpenCVNativeLoader;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * Reads image data from a DICOM object. Supports all the DICOM objects containing pixel data. Use the OpenCV native
 * library to read compressed and uncompressed pixel data.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageReader extends javax.imageio.ImageReader {

    public static Set<Integer> BULK_TAGS = Set.of(Tag.PixelDataProviderURL, Tag.AudioSampleData, Tag.CurveData,
            Tag.SpectroscopyData, Tag.RedPaletteColorLookupTableData, Tag.GreenPaletteColorLookupTableData,
            Tag.BluePaletteColorLookupTableData, Tag.AlphaPaletteColorLookupTableData,
            Tag.LargeRedPaletteColorLookupTableData, Tag.LargeGreenPaletteColorLookupTableData,
            Tag.LargeBluePaletteColorLookupTableData, Tag.SegmentedRedPaletteColorLookupTableData,
            Tag.SegmentedGreenPaletteColorLookupTableData, Tag.SegmentedBluePaletteColorLookupTableData,
            Tag.SegmentedAlphaPaletteColorLookupTableData, Tag.OverlayData, Tag.EncapsulatedDocument,
            Tag.FloatPixelData, Tag.DoubleFloatPixelData, Tag.PixelData);
    public static final BulkDataDescriptor BULKDATA_DESCRIPTOR = (itemPointer, privateCreator, tag, vr, length) -> {
        var tagNormalized = Tag.normalizeRepeatingGroup(tag);
        if (tagNormalized == Tag.WaveformData) {
            return itemPointer.size() == 1 && itemPointer.get(0).sequenceTag == Tag.WaveformSequence;
        } else if (BULK_TAGS.contains(tagNormalized)) {
            return itemPointer.isEmpty();
        }

        if (Tag.isPrivateTag(tag)) {
            return length > 1000; // Do no read in memory private value more than 1 KB
        }

        return switch (vr) {
        case OB, OD, OF, OL, OW, UN -> length > 64;
        default -> false;
        };
    };

    static {
        // Load the native OpenCV library
        OpenCVNativeLoader loader = new OpenCVNativeLoader();
        loader.init();
    }

    private final ArrayList<Integer> fragmentsPositions = new ArrayList<>();

    private BytesWithImageDescriptor bdis;
    private ImageFileInputStream dis;

    public ImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

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
            keepRgbForLossyJpeg = param.getKeepRgbForLossyJpeg().orElse(false);
        }

        if (pmi == Photometric.RGB && !keepRgbForLossyJpeg) {
            // Force JPEG Baseline (1.2.840.10008.1.2.4.50) to YBR_FULL_422 color model when RGB with
            // JFIF header or not RGB components (error made by some constructors).
            return !"RGB".equals(parser.getParams().colorPhotometricInterpretation());
        }
        return false;
    }

    private static boolean ybr2rgb(Photometric pmi, String tsuid, BooleanSupplier isYbrModel) {
        // Option only for IJG native decoder
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

    public static ImageCV applyReleaseImageAfterProcessing(ImageCV imageCV, ImageReadParam param) {
        if (isReleaseImageAfterProcessing(param)) {
            imageCV.setReleasedAfterProcessing(true);
        }
        return imageCV;
    }

    public static boolean isReleaseImageAfterProcessing(ImageReadParam param) {
        return param != null && param.getReleaseImageAfterProcessing().orElse(Boolean.FALSE);
    }

    public static void closeMat(Mat mat) {
        if (mat != null) {
            mat.release();
        }
    }

    public static boolean isSupportedSyntax(String uid) {
        return switch (UID.from(uid)) {
        case UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndian, UID.RLELossless, UID.JPEGBaseline8Bit, UID.JPEGExtended12Bit, UID.JPEGSpectralSelectionNonHierarchical68, UID.JPEGFullProgressionNonHierarchical1012, UID.JPEGLossless, UID.JPEGLosslessSV1, UID.JPEGLSLossless, UID.JPEGLSNearLossless, UID.JPEG2000Lossless, UID.JPEG2000, UID.JPEG2000MCLossless, UID.JPEG2000MC -> true;
        default -> false;
        };
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        resetInternalState();
        if (input instanceof ImageFileInputStream) {
            super.setInput(input, seekForwardOnly, ignoreMetadata);
            this.dis = (ImageFileInputStream) input;
            dis.setIncludeBulkData(ImageInputStream.IncludeBulkData.URI);
            dis.setBulkDataDescriptor(BULKDATA_DESCRIPTOR);
            // avoid a copy of pixeldata into temporary file
            dis.setURI(dis.getPath().toUri().toString());
        } else if (input instanceof BytesWithImageDescriptor) {
            this.bdis = (BytesWithImageDescriptor) input;
        } else {
            throw new IllegalArgumentException("Unsupported inputStream: " + input.getClass().getName());
        }
    }

    public ImageDescriptor getImageDescriptor() {
        if (bdis != null)
            return bdis.getImageDescriptor();
        return dis.getImageDescriptor();
    }

    /**
     * Returns the number of regular images in the study. This excludes overlays.
     */
    @Override
    public int getNumImages(boolean allowSearch) {
        return getImageDescriptor().getFrames();
    }

    @Override
    public int getWidth(int frameIndex) {
        checkIndex(frameIndex);
        return getImageDescriptor().getColumns();
    }

    @Override
    public int getHeight(int frameIndex) {
        checkIndex(frameIndex);
        return getImageDescriptor().getRows();
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int frameIndex) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public javax.imageio.ImageReadParam getDefaultReadParam() {
        return new ImageReadParam();
    }

    /**
     * Gets the stream metadata. May not contain post pixel data unless there are no images or the getStreamMetadata has
     * been called with the post pixel data node being specified.
     */
    @Override
    public ImageMetaData getStreamMetadata() throws IOException {
        return dis == null ? null : dis.getMetadata();
    }

    @Override
    public IIOMetadata getImageMetadata(int frameIndex) {
        return null;
    }

    @Override
    public boolean canReadRaster() {
        return true;
    }

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

    private void resetInternalState() {
        IoKit.close(dis);
        dis = null;
        bdis = null;
        fragmentsPositions.clear();
    }

    private void checkIndex(int frameIndex) {
        if (frameIndex < 0 || frameIndex >= getImageDescriptor().getFrames())
            throw new IndexOutOfBoundsException("imageIndex: " + frameIndex);
    }

    @Override
    public void dispose() {
        resetInternalState();
    }

    private boolean fileYbr2rgb(Photometric pmi, String tsuid, ExtendSegmentedInputImageStream seg, int frame,
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

    public List<SupplierEx<PlanarImage, IOException>> getLazyPlanarImages(ImageReadParam param,
            Editable<PlanarImage> editor) {
        int size = getImageDescriptor().getFrames();
        List<SupplierEx<PlanarImage, IOException>> suppliers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            final int index = i;
            suppliers.add(new SupplierEx<>() {
                boolean initialized;

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

    public List<PlanarImage> getPlanarImages() throws IOException {
        return getPlanarImages(null);
    }

    public List<PlanarImage> getPlanarImages(ImageReadParam param) throws IOException {
        List<PlanarImage> list = new ArrayList<>();
        for (int i = 0; i < getImageDescriptor().getFrames(); i++) {
            list.add(getPlanarImage(i, param));
        }
        return list;
    }

    public PlanarImage getPlanarImage() throws IOException {
        return getPlanarImage(0, null);
    }

    public PlanarImage getPlanarImage(int frame, ImageReadParam param) throws IOException {
        PlanarImage img = getRawImage(frame, param);
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
        if (!img.equals(out)) {
            img.release();
        }
        return out;
    }

    public PlanarImage getRawImage(int frame, ImageReadParam param) throws IOException {
        if (dis == null) {
            return getRawImageFromBytes(frame, param);
        } else {
            return getRawImageFromFile(frame, param);
        }
    }

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
            // always little endian:
            // http://dicom.nema.org/medical/dicom/2017b/output/chtml/part05/sect_A.6.html
        } else if (pixdata instanceof Fragments) {
            pixeldataFragments = (Fragments) pixdata;
            bigendian = pixeldataFragments.bigEndian();
        }

        ExtendSegmentedInputImageStream seg = buildSegmentedImageInputStream(frame, pixeldataFragments, bulkData);
        if (seg.segmentPositions() == null) {
            return null;
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
            ImageCV imageCV = ImageCV.toImageCV(Imgcodecs.dicomJpgFileRead(seg.path().toString(), positions, lengths,
                    dcmFlags, Imgcodecs.IMREAD_UNCHANGED));
            return applyReleaseImageAfterProcessing(imageCV, param);
        } finally {
            closeMat(positions);
            closeMat(lengths);
        }
    }

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
                ImageCV imageCV = ImageCV.toImageCV(Imgcodecs.dicomRawMatRead(buf, dicomparams, pmi.name()));
                return applyReleaseImageAfterProcessing(imageCV, param);
            }
            ImageCV imageCV = ImageCV.toImageCV(Imgcodecs.dicomJpgMatRead(buf, dcmFlags, Imgcodecs.IMREAD_UNCHANGED));
            return applyReleaseImageAfterProcessing(imageCV, param);
        } finally {
            closeMat(buf);
        }
    }

    private ExtendSegmentedInputImageStream buildSegmentedImageInputStream(int frameIndex, Fragments fragments,
            BulkData bulkData) throws IOException {
        long[] offsets;
        int[] length;
        ImageDescriptor desc = getImageDescriptor();
        boolean hasFragments = fragments != null;
        if (!hasFragments && bulkData != null) {
            int frameLength = desc.getPhotometricInterpretation().frameLength(desc.getColumns(), desc.getRows(),
                    desc.getSamples(), desc.getBitsAllocated());
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
                    // Multi-frames where each frames can have multiple fragments.
                    if (fragmentsPositions.isEmpty()) {
                        try (SeekableByteChannel channel = Files.newByteChannel(dis.getPath(),
                                StandardOpenOption.READ)) {
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

}
