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
package org.miaixz.bus.image.nimble.opencv;

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.nio.ByteOrder;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

import org.miaixz.bus.image.nimble.Photometric;
import org.miaixz.bus.image.nimble.codec.BytesWithImageImageDescriptor;
import org.miaixz.bus.image.nimble.codec.ImageDescriptor;
import org.miaixz.bus.logger.Logger;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class NativeJLSImageWriter extends ImageWriter {

    NativeJLSImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public ImageWriteParam getDefaultWriteParam() {
        return new JPEGLSImageWriteParam(getLocale());
    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        if (output == null) {
            throw new IllegalStateException("input cannot be null");
        }

        if (!(output instanceof ImageOutputStream stream)) {
            throw new IllegalArgumentException("input is not an ImageInputStream!");
        }
        stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        JPEGLSImageWriteParam jpegParams = (JPEGLSImageWriteParam) param;

        if (!(stream instanceof BytesWithImageImageDescriptor)) {
            throw new IllegalArgumentException("stream does not implement BytesWithImageImageDescriptor!");
        }
        ImageDescriptor desc = ((BytesWithImageImageDescriptor) stream).getImageDescriptor();
        Photometric pi = desc.getPhotometricInterpretation();

        if (jpegParams.isCompressionLossless() && (Photometric.YBR_FULL_422 == pi || Photometric.YBR_PARTIAL_422 == pi
                || Photometric.YBR_PARTIAL_420 == pi || Photometric.YBR_ICT == pi || Photometric.YBR_RCT == pi)) {
            throw new IllegalArgumentException(
                    "True lossless encoder: Photometric interpretation is not supported: " + pi);
        }

        RenderedImage renderedImage = image.getRenderedImage();
        Mat buf = null;
        MatOfInt dicomParams = null;
        try {
            ImageCV mat = null;
            try {
                // Band interleaved mode (PlanarConfiguration = 1) is converted to pixel interleaved
                // So the input image has always a pixel interleaved mode mode((PlanarConfiguration = 0)
                mat = ImageConversion.toMat(renderedImage, param.getSourceRegion(), false);

                int jpeglsNLE = jpegParams.getNearLossless();
                int bitCompressed = desc.getBitsCompressed();
                int cvType = mat.type();
                int channels = CvType.channels(cvType);
                int epi = channels == 1 ? Imgcodecs.EPI_Monochrome2 : Imgcodecs.EPI_RGB;
                boolean signed = desc.isSigned();
                int dcmFlags = signed ? Imgcodecs.DICOM_FLAG_SIGNED : Imgcodecs.DICOM_FLAG_UNSIGNED;
                if (signed) {
                    Logger.warn("Force compression to JPEG-LS lossless as lossy is not adapted to signed data.");
                    jpeglsNLE = 0;
                    bitCompressed = 16; // Extend to bit allocated to avoid exception as negative values are treated as
                                        // large positive values
                }
                // Specific case not well supported by jpeg and jpeg-ls encoder that reduce the stream to 8-bit
                if (bitCompressed == 8 && renderedImage.getSampleModel().getTransferType() != DataBuffer.TYPE_BYTE) {
                    bitCompressed = 12;
                }

                int[] params = new int[16];
                params[Imgcodecs.DICOM_PARAM_IMREAD] = Imgcodecs.IMREAD_UNCHANGED; // Image flags
                params[Imgcodecs.DICOM_PARAM_DCM_IMREAD] = dcmFlags; // DICOM flags
                params[Imgcodecs.DICOM_PARAM_WIDTH] = mat.width(); // Image width
                params[Imgcodecs.DICOM_PARAM_HEIGHT] = mat.height(); // Image height
                params[Imgcodecs.DICOM_PARAM_COMPRESSION] = Imgcodecs.DICOM_CP_JPLS; // Type of compression
                params[Imgcodecs.DICOM_PARAM_COMPONENTS] = channels; // Number of components
                params[Imgcodecs.DICOM_PARAM_BITS_PER_SAMPLE] = bitCompressed; // Bits per sample
                params[Imgcodecs.DICOM_PARAM_INTERLEAVE_MODE] = Imgcodecs.ILV_SAMPLE; // Interleave mode
                params[Imgcodecs.DICOM_PARAM_COLOR_MODEL] = epi; // Photometric interpretation
                params[Imgcodecs.DICOM_PARAM_JPEGLS_LOSSY_ERROR] = jpeglsNLE; // Lossy error for jpeg-ls

                dicomParams = new MatOfInt(params);
                buf = Imgcodecs.dicomJpgWrite(mat, dicomParams, "");
                if (buf.empty()) {
                    throw new IIOException("Native JPEG-LS encoding error: null image");
                }
            } finally {
                if (mat != null) {
                    mat.release();
                }
            }
            byte[] bSrcData = new byte[buf.width() * buf.height() * (int) buf.elemSize()];
            buf.get(0, 0, bSrcData);
            stream.write(bSrcData);
        } catch (Throwable t) {
            throw new IIOException("Native JPEG-LS encoding error", t);
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
