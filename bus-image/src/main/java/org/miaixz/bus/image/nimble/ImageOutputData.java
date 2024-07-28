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

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.SupplierEx;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.nimble.codec.TransferSyntaxType;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.miaixz.bus.image.nimble.stream.ImageDescriptor;
import org.miaixz.bus.logger.Logger;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageOutputData {

    private final List<SupplierEx<PlanarImage, IOException>> images;
    private final ImageDescriptor desc;
    private final String tsuid;

    public ImageOutputData(List<SupplierEx<PlanarImage, IOException>> images, ImageDescriptor desc, String tsuid)
            throws IOException {
        if (Objects.requireNonNull(images).isEmpty()) {
            throw new IllegalStateException("No image found!");
        }
        this.images = new ArrayList<>(images);
        this.desc = Objects.requireNonNull(desc);
        int type = CvType.depth(getFirstImage().get().type());
        this.tsuid = ImageOutputData.adaptSuitableSyntax(desc.getBitsStored(), type, Objects.requireNonNull(tsuid));
        if (!isSupportedSyntax(this.tsuid)) {
            throw new IllegalStateException(this.tsuid + " is not supported as encoding syntax!");
        }
    }

    public ImageOutputData(SupplierEx<PlanarImage, IOException> image, ImageDescriptor desc, String tsuid)
            throws IOException {
        this(Collections.singletonList(image), desc, tsuid);
    }

    public ImageOutputData(PlanarImage image, ImageDescriptor desc, String tsuid) throws IOException {
        this(Collections.singletonList(() -> image), desc, tsuid);
    }

    public static void adaptTagsToRawImage(Attributes data, PlanarImage img, ImageDescriptor desc) {
        int cvType = img.type();
        int channels = CvType.channels(cvType);
        int signed = CvType.depth(cvType) == CvType.CV_16S || desc.isSigned() ? 1 : 0;
        data.setInt(Tag.Columns, VR.US, img.width());
        data.setInt(Tag.Rows, VR.US, img.height());
        data.setInt(Tag.SamplesPerPixel, VR.US, channels);
        data.setInt(Tag.BitsAllocated, VR.US, desc.getBitsAllocated());
        data.setInt(Tag.BitsStored, VR.US, desc.getBitsStored());
        data.setInt(Tag.HighBit, VR.US, desc.getBitsStored() - 1);
        data.setInt(Tag.PixelRepresentation, VR.US, signed);
        String pmi = desc.getPhotometricInterpretation().toString();
        if (img.channels() > 1) {
            pmi = Photometric.RGB.toString();
            data.setInt(Tag.PlanarConfiguration, VR.US, 0);
        }
        data.setString(Tag.PhotometricInterpretation, VR.CS, pmi);
    }

    public static String adaptSuitableSyntax(int bitStored, int type, String dstTsuid) {
        switch (UID.from(dstTsuid)) {
        case UID.ImplicitVRLittleEndian:
        case UID.ExplicitVRLittleEndian:
            return UID.ExplicitVRLittleEndian.uid;
        case UID.JPEGBaseline8Bit:
            return type <= CvType.CV_8S ? dstTsuid
                    : type <= CvType.CV_16S ? UID.JPEGLosslessSV1.uid : UID.ExplicitVRLittleEndian.uid;
        case UID.JPEGExtended12Bit:
        case UID.JPEGSpectralSelectionNonHierarchical68:
        case UID.JPEGFullProgressionNonHierarchical1012:
            return type <= CvType.CV_16U && bitStored <= 12 ? dstTsuid
                    : type <= CvType.CV_16S ? UID.JPEGLosslessSV1.uid : UID.ExplicitVRLittleEndian.uid;
        case UID.JPEGLossless:
        case UID.JPEGLosslessSV1:
        case UID.JPEGLSLossless:
        case UID.JPEGLSNearLossless:
        case UID.JPEG2000Lossless:
        case UID.JPEG2000:
            return type <= CvType.CV_16S ? dstTsuid : UID.ExplicitVRLittleEndian.uid;
        default:
            return dstTsuid;
        }
    }

    public static boolean isAdaptableSyntax(String uid) {
        switch (UID.from(uid)) {
        case UID.JPEGBaseline8Bit:
        case UID.JPEGExtended12Bit:
        case UID.JPEGSpectralSelectionNonHierarchical68:
        case UID.JPEGFullProgressionNonHierarchical1012:
            return true;
        default:
            return false;
        }
    }

    public static boolean isNativeSyntax(String uid) {
        switch (UID.from(uid)) {
        case UID.ImplicitVRLittleEndian:
        case UID.ExplicitVRLittleEndian:
            return true;
        default:
            return false;
        }
    }

    public static boolean isSupportedSyntax(String uid) {
        switch (UID.from(uid)) {
        case UID.ImplicitVRLittleEndian:
        case UID.ExplicitVRLittleEndian:
        case UID.JPEGBaseline8Bit:
        case UID.JPEGExtended12Bit:
        case UID.JPEGSpectralSelectionNonHierarchical68:
        case UID.JPEGFullProgressionNonHierarchical1012:
        case UID.JPEGLossless:
        case UID.JPEGLosslessSV1:
        case UID.JPEGLSLossless:
        case UID.JPEGLSNearLossless:
        case UID.JPEG2000Lossless:
        case UID.JPEG2000:
            // case UID.JPEG2000Part2MultiComponentLosslessOnly:
            // case UID.JPEG2000Part2MultiComponent:
            return true;
        default:
            return false;
        }
    }

    public SupplierEx<PlanarImage, IOException> getFirstImage() {
        return images.get(0);
    }

    public List<SupplierEx<PlanarImage, IOException>> getImages() {
        return images;
    }

    public String getTsuid() {
        return tsuid;
    }

    public void writeCompressedImageData(ImageOutputStream dos, Attributes dataSet, int[] params) throws IOException {
        Mat buf = null;
        MatOfInt dicomParams = null;
        try {
            dicomParams = new MatOfInt(params);
            for (int i = 0; i < images.size(); i++) {
                PlanarImage image = images.get(i).get();
                boolean releaseSrc = image.isReleasedAfterProcessing();
                PlanarImage writeImg = Builder.isJpeg2000(tsuid) ? image : RGBImageVoiLut.bgr2rgb(image);
                if (releaseSrc && !writeImg.equals(image)) {
                    image.release();
                }
                buf = Imgcodecs.dicomJpgWrite(writeImg.toMat(), dicomParams, "");
                if (buf.empty()) {
                    writeImg.release();
                    throw new IOException("Native encoding error: null image");
                }
                int compressedLength = buf.width() * buf.height() * (int) buf.elemSize();
                if (i == 0) {
                    double uncompressed = writeImg.width() * writeImg.height() * (double) writeImg.elemSize();
                    adaptCompressionRatio(dataSet, params, uncompressed / compressedLength);
                    dos.writeDataset(null, dataSet);
                    dos.writeHeader(Tag.PixelData, VR.OB, -1);
                    dos.writeHeader(Tag.Item, null, 0);
                }
                if (releaseSrc) {
                    writeImg.release();
                }

                byte[] bSrcData = new byte[compressedLength];
                buf.get(0, 0, bSrcData);
                dos.writeHeader(Tag.Item, null, bSrcData.length);
                dos.write(bSrcData);
            }
            dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
        } catch (Throwable t) {
            throw new IOException("Native encoding error", t);
        } finally {
            ImageReader.closeMat(dicomParams);
            ImageReader.closeMat(buf);
        }
    }

    private void adaptCompressionRatio(Attributes dataSet, int[] params, double ratio) {
        int compressType = params[Imgcodecs.DICOM_PARAM_COMPRESSION];
        int jpeglsNLE = params[Imgcodecs.DICOM_PARAM_JPEGLS_LOSSY_ERROR];
        int jpeg2000CompRatio = params[Imgcodecs.DICOM_PARAM_J2K_COMPRESSION_FACTOR];
        int jpegQuality = params[Imgcodecs.DICOM_PARAM_JPEG_QUALITY];
        if ((compressType == Imgcodecs.DICOM_CP_JPG && jpegQuality > 0)
                || (compressType == Imgcodecs.DICOM_CP_J2K && jpeg2000CompRatio > 0)
                || (compressType == Imgcodecs.DICOM_CP_JPLS && jpeglsNLE > 0)) {
            dataSet.setString(Tag.LossyImageCompression, VR.CS, "01");
            String method = compressType == Imgcodecs.DICOM_CP_J2K ? "ISO_15444_1"
                    : compressType == Imgcodecs.DICOM_CP_JPLS ? "ISO_14495_1" : "ISO_10918_1";
            double[] old = dataSet.getDoubles(Tag.LossyImageCompressionRatio);
            double[] destArray;
            String[] methods;
            if (old == null) {
                destArray = new double[] { ratio };
                methods = new String[] { method };
            } else {
                destArray = Arrays.copyOf(old, old.length + 1);
                destArray[destArray.length - 1] = ratio;
                String[] oldM = Builder.getStringArrayFromDicomElement(dataSet, Tag.LossyImageCompressionMethod,
                        new String[0]);
                methods = Arrays.copyOf(oldM, old.length + 1);
                methods[methods.length - 1] = method;
                for (int i = 0; i < methods.length; i++) {
                    if (!StringKit.hasText(methods[i])) {
                        methods[i] = "unknown";
                    }
                }
            }
            dataSet.setDouble(Tag.LossyImageCompressionRatio, VR.DS, destArray);
            dataSet.setString(Tag.LossyImageCompressionMethod, VR.CS, methods);
        }
    }

    public void writRawImageData(ImageOutputStream dos, Attributes data) {
        try {
            PlanarImage fistImage = getFirstImage().get();
            adaptTagsToRawImage(data, fistImage, desc);
            dos.writeDataset(null, data);

            int type = CvType.depth(fistImage.type());
            int imgSize = fistImage.width() * fistImage.height();
            int channels = CvType.channels(fistImage.type());
            int length = images.size() * imgSize * (int) fistImage.elemSize();
            dos.writeHeader(Tag.PixelData, VR.OB, length);

            if (type <= CvType.CV_8S) {
                byte[] srcData = new byte[imgSize * channels];
                for (SupplierEx<PlanarImage, IOException> image : images) {
                    PlanarImage img = RGBImageVoiLut.bgr2rgb(image.get());
                    img.get(0, 0, srcData);
                    dos.write(srcData);
                }
            } else if (type <= CvType.CV_16S) {
                short[] srcData = new short[imgSize * channels];
                ByteBuffer bb = ByteBuffer.allocate(srcData.length * 2);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                for (SupplierEx<PlanarImage, IOException> image : images) {
                    PlanarImage img = RGBImageVoiLut.bgr2rgb(image.get());
                    img.get(0, 0, srcData);
                    bb.asShortBuffer().put(srcData);
                    dos.write(bb.array());
                }
            } else if (type == CvType.CV_32S) {
                int[] srcData = new int[imgSize * channels];
                ByteBuffer bb = ByteBuffer.allocate(srcData.length * 4);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                for (SupplierEx<PlanarImage, IOException> image : images) {
                    PlanarImage img = RGBImageVoiLut.bgr2rgb(image.get());
                    img.get(0, 0, srcData);
                    bb.asIntBuffer().put(srcData);
                    dos.write(bb.array());
                }
            } else if (type == CvType.CV_32F) {
                float[] srcData = new float[imgSize * channels];
                ByteBuffer bb = ByteBuffer.allocate(srcData.length * 4);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                for (SupplierEx<PlanarImage, IOException> image : images) {
                    PlanarImage img = RGBImageVoiLut.bgr2rgb(image.get());
                    img.get(0, 0, srcData);
                    bb.asFloatBuffer().put(srcData);
                    dos.write(bb.array());
                }
            } else if (type == CvType.CV_64F) {
                double[] srcData = new double[imgSize * channels];
                ByteBuffer bb = ByteBuffer.allocate(srcData.length * 8);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                for (SupplierEx<PlanarImage, IOException> image : images) {
                    PlanarImage img = RGBImageVoiLut.bgr2rgb(image.get());
                    img.get(0, 0, srcData);
                    bb.asDoubleBuffer().put(srcData);
                    dos.write(bb.array());
                }
            } else {
                throw new IllegalStateException("Cannot write this unknown image type");
            }
        } catch (Exception e) {
            Logger.error("Writing raw pixel data", e);
        }
    }

    public int[] adaptTagsToCompressedImage(Attributes data, PlanarImage img, ImageDescriptor desc,
            JpegWriteParam param) {
        int cvType = img.type();
        int elemSize = (int) img.elemSize1();
        int channels = CvType.channels(cvType);
        int depth = CvType.depth(cvType);
        boolean signed = depth != CvType.CV_8U && (depth != CvType.CV_16U || desc.isSigned());
        int dcmFlags = signed ? Imgcodecs.DICOM_FLAG_SIGNED : Imgcodecs.DICOM_FLAG_UNSIGNED;
        Photometric pmi = desc.getPhotometricInterpretation();
        int epi = channels == 1 ? Imgcodecs.EPI_Monochrome2 : Imgcodecs.EPI_RGB;
        int bitAllocated = elemSize * 8;
        int bitCompressed = desc.getBitsCompressed();
        if (bitCompressed > bitAllocated) {
            bitCompressed = bitAllocated;
        }
        int bitCompressedForEncoder = bitCompressed;
        int jpeglsNLE = param.getNearLosslessError();
        TransferSyntaxType ts = param.getType();
        int compressType = Imgcodecs.DICOM_CP_JPG;
        if (ts == TransferSyntaxType.JPEG_2000) {
            compressType = Imgcodecs.DICOM_CP_J2K;
        } else if (ts == TransferSyntaxType.JPEG_LS) {
            compressType = Imgcodecs.DICOM_CP_JPLS;
            if (signed) {
                Logger.warn("Force compression to JPEG-LS lossless as lossy is not adapted to signed data.");
                jpeglsNLE = 0;
                // Extend to bit allocated to avoid exception as negative values are treated as large
                // positive values
                bitCompressedForEncoder = 16;
            }
        } else {
            // JPEG encoder
            if (bitCompressed <= 8) {
                bitCompressedForEncoder = bitCompressed = 8;
            } else if (bitCompressed <= 12) {
                if (signed && param.getPrediction() > 1) {
                    Logger.warn("Force JPEGLosslessNonHierarchical14 compression to 16-bit with signed data.");
                    bitCompressed = 12;
                    bitCompressedForEncoder = 16;
                } else {
                    bitCompressedForEncoder = bitCompressed = 12;
                }
            } else {
                bitCompressedForEncoder = bitCompressed = 16;
            }
        }

        // Specific case not well supported by jpeg and jpeg-ls encoder that reduce the stream to 8-bit
        if (ts != TransferSyntaxType.JPEG_2000 && bitCompressed == 8 && bitAllocated == 16) {
            bitCompressedForEncoder = 12;
        }

        int[] params = new int[16];
        params[Imgcodecs.DICOM_PARAM_IMREAD] = Imgcodecs.IMREAD_UNCHANGED; // Image flags
        params[Imgcodecs.DICOM_PARAM_DCM_IMREAD] = dcmFlags; // DICOM flags
        params[Imgcodecs.DICOM_PARAM_WIDTH] = img.width(); // Image width
        params[Imgcodecs.DICOM_PARAM_HEIGHT] = img.height(); // Image height
        params[Imgcodecs.DICOM_PARAM_COMPRESSION] = compressType; // Type of compression
        params[Imgcodecs.DICOM_PARAM_COMPONENTS] = channels; // Number of components
        params[Imgcodecs.DICOM_PARAM_BITS_PER_SAMPLE] = bitCompressedForEncoder; // Bits per sample
        params[Imgcodecs.DICOM_PARAM_INTERLEAVE_MODE] = Imgcodecs.ILV_SAMPLE; // Interleave mode
        params[Imgcodecs.DICOM_PARAM_COLOR_MODEL] = epi; // Photometric interpretation
        params[Imgcodecs.DICOM_PARAM_JPEG_MODE] = param.getJpegMode(); // JPEG Codec mode
        params[Imgcodecs.DICOM_PARAM_JPEGLS_LOSSY_ERROR] = jpeglsNLE; // Lossy error for jpeg-ls
        params[Imgcodecs.DICOM_PARAM_J2K_COMPRESSION_FACTOR] = param.getCompressionRatioFactor(); // JPEG2000 factor of
                                                                                                  // compression ratio
        params[Imgcodecs.DICOM_PARAM_JPEG_QUALITY] = param.getCompressionQuality(); // JPEG lossy quality
        params[Imgcodecs.DICOM_PARAM_JPEG_PREDICTION] = param.getPrediction(); // JPEG lossless prediction
        params[Imgcodecs.DICOM_PARAM_JPEG_PT_TRANSFORM] = param.getPointTransform(); // JPEG lossless transformation
                                                                                     // point

        data.setInt(Tag.Columns, VR.US, img.width());
        data.setInt(Tag.Rows, VR.US, img.height());
        data.setInt(Tag.SamplesPerPixel, VR.US, channels);
        data.setInt(Tag.BitsAllocated, VR.US, bitAllocated);
        data.setInt(Tag.BitsStored, VR.US, bitCompressed);
        data.setInt(Tag.HighBit, VR.US, bitCompressed - 1);
        data.setInt(Tag.PixelRepresentation, VR.US, signed ? 1 : 0);
        if (img.channels() > 1) {
            data.setInt(Tag.PlanarConfiguration, VR.US, 0);
            pmi = Photometric.RGB.compress(tsuid);
        }
        data.setString(Tag.PhotometricInterpretation, VR.CS, pmi.toString());
        return params;
    }

}
