/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.nimble.opencv;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.image.nimble.Photometric;
import org.aoju.bus.image.nimble.codec.BytesWithImageDescriptor;
import org.aoju.bus.image.nimble.codec.ImageDescriptor;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class NativeJPEGImageWriter extends ImageWriter {

    NativeJPEGImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    private static int getCodecColorSpace(Photometric pi) {
        if (Photometric.MONOCHROME1 == pi) {
            return Imgcodecs.EPI_Monochrome1;
        } else if (Photometric.MONOCHROME2 == pi) {
            return Imgcodecs.EPI_Monochrome2;
        } else if (Photometric.RGB == pi) {
            return Imgcodecs.EPI_RGB;
        } else if (Photometric.YBR_FULL == pi) {
            return Imgcodecs.EPI_YBR_Full;
        } else if (Photometric.YBR_FULL_422 == pi) {
            return Imgcodecs.EPI_YBR_Full_422;
        } else if (Photometric.YBR_PARTIAL_422 == pi) {
            return Imgcodecs.EPI_YBR_Partial_422;
        } else { // Palette, HSV, ARGB, CMYK
            return Imgcodecs.EPI_Unknown;
        }
    }

    @Override
    public ImageWriteParam getDefaultWriteParam() {
        return new JPEGImageWriteParam(getLocale());
    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        if (null == output) {
            throw new IllegalStateException("input cannot be null");
        }

        if (!(output instanceof ImageOutputStream)) {
            throw new IllegalArgumentException("input is not an ImageInputStream!");
        }
        ImageOutputStream stream = (ImageOutputStream) output;
        stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) param;

        if (!(stream instanceof BytesWithImageDescriptor)) {
            throw new IllegalArgumentException("stream does not implement BytesWithImageImageDescriptor!");
        }
        ImageDescriptor desc = ((BytesWithImageDescriptor) stream).getImageDescriptor();
        Photometric pi = desc.getPhotometric();

        if (jpegParams.isCompressionLossless() && (Photometric.YBR_FULL_422 == pi
                || Photometric.YBR_PARTIAL_422 == pi || Photometric.YBR_PARTIAL_420 == pi
                || Photometric.YBR_ICT == pi || Photometric.YBR_RCT == pi)) {
            throw new IllegalArgumentException(
                    "True lossless encoder: Photometric interpretation is not supported: " + pi);
        }
        int epi = getCodecColorSpace(pi);

        RenderedImage renderedImage = image.getRenderedImage();
        Mat buf = null;
        MatOfInt dicomParams = null;
        try {
            ImageCV mat = null;
            try {
                // Band interleaved mode (PlanarConfiguration = 1) is converted to pixel interleaved
                // So the input image has always a pixel interleaved mode mode((PlanarConfiguration = 0)
                mat = ImageConversion.toMat(renderedImage, param.getSourceRegion(), false);

                int cvType = mat.type();
                int channels = CvType.channels(cvType);
                boolean signed = desc.isSigned();
                int dcmFlags = signed ? Imgcodecs.DICOM_FLAG_SIGNED : Imgcodecs.DICOM_FLAG_UNSIGNED;

                int[] params = new int[15];
                params[Imgcodecs.DICOM_PARAM_IMREAD] = Imgcodecs.IMREAD_UNCHANGED; // Image flags
                params[Imgcodecs.DICOM_PARAM_DCM_IMREAD] = dcmFlags; // DICOM flags
                params[Imgcodecs.DICOM_PARAM_WIDTH] = mat.width(); // Image width
                params[Imgcodecs.DICOM_PARAM_HEIGHT] = mat.height(); // Image height
                params[Imgcodecs.DICOM_PARAM_COMPRESSION] = Imgcodecs.DICOM_CP_JPG; // Type of compression
                params[Imgcodecs.DICOM_PARAM_COMPONENTS] = channels; // Number of components
                params[Imgcodecs.DICOM_PARAM_BITS_PER_SAMPLE] = desc.getBitsCompressed(); // Bits per sample
                params[Imgcodecs.DICOM_PARAM_INTERLEAVE_MODE] = Imgcodecs.ILV_SAMPLE; // Interleave mode
                params[Imgcodecs.DICOM_PARAM_COLOR_MODEL] = epi; // Photometric interpretation
                params[Imgcodecs.DICOM_PARAM_JPEG_MODE] = jpegParams.getMode(); // JPEG Codec mode
                params[Imgcodecs.DICOM_PARAM_JPEG_QUALITY] = (int) (jpegParams.getCompressionQuality() * 100); // JPEG lossy quality
                params[Imgcodecs.DICOM_PARAM_JPEG_PREDICTION] = jpegParams.getPrediction(); // JPEG lossless prediction
                params[Imgcodecs.DICOM_PARAM_JPEG_PT_TRANSFORM] = jpegParams.getPointTransform(); // JPEG lossless transformation point

                dicomParams = new MatOfInt(params);
                buf = Imgcodecs.dicomJpgWrite(mat, dicomParams, Normal.EMPTY);
                if (buf.empty()) {
                    throw new IIOException("Native JPEG encoding error: null image");
                }
            } finally {
                if (null != mat) {
                    mat.release();
                }
            }

            byte[] bSrcData = new byte[buf.width() * buf.height() * (int) buf.elemSize()];
            buf.get(0, 0, bSrcData);
            stream.write(bSrcData);
        } catch (Throwable t) {
            throw new IIOException("Native JPEG encoding error", t);
        } finally {
            NativeImageReader.closeMat(dicomParams);
            NativeImageReader.closeMat(buf);
        }
    }

    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

}
