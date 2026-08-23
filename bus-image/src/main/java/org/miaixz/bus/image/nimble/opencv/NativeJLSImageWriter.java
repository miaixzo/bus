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
package org.miaixz.bus.image.nimble.opencv;

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;

import javax.imageio.ImageWriteParam;
import javax.imageio.spi.ImageWriterSpi;

import org.opencv.core.CvType;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import org.miaixz.bus.image.nimble.codec.ImageDescriptor;
import org.miaixz.bus.logger.Logger;

/**
 * Represents the NativeJLSImageWriter type.
 *
 * @author Kimi Liu
 */
public class NativeJLSImageWriter extends AbstractNativeImageWriter {

    /**
     * Creates a new instance.
     *
     * @param originatingProvider the originating provider.
     */
    public NativeJLSImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    /**
     * Gets the default write param.
     *
     * @return the default write param.
     */
    @Override
    public ImageWriteParam getDefaultWriteParam() {
        return new JPEGLSImageWriteParam(getLocale());
    }

    /**
     * Gets the codec name.
     *
     * @return the codec name.
     */
    @Override
    String codecName() {
        return "JPEG-LS";
    }

    /**
     * Validates the write parameters.
     *
     * @param param the write param.
     * @param desc  the image descriptor.
     */
    @Override
    void validate(ImageWriteParam param, ImageDescriptor desc) {
        rejectChromaSubsampledLossless(param.isCompressionLossless(), desc.getPhotometricInterpretation());
    }

    /**
     * Converts the image to a native matrix.
     *
     * @param image the image.
     * @param param the write param.
     * @param desc  the image descriptor.
     * @return the native matrix.
     */
    @Override
    ImageCV toMat(RenderedImage image, ImageWriteParam param, ImageDescriptor desc) {
        // Band interleaved mode (PlanarConfiguration = 1) is converted to pixel interleaved.
        return ImageConversion.toMat(image, param.getSourceRegion(), false);
    }

    /**
     * Builds the native DICOM parameters.
     *
     * @param mat   the native matrix.
     * @param image the image.
     * @param param the write param.
     * @param desc  the image descriptor.
     * @return the native DICOM parameters.
     */
    @Override
    MatOfInt buildDicomParams(ImageCV mat, RenderedImage image, ImageWriteParam param, ImageDescriptor desc) {
        int jpeglsNLE = ((JPEGLSImageWriteParam) param).getNearLossless();
        int bitCompressed = desc.getBitsCompressed();
        int channels = CvType.channels(mat.type());
        boolean signed = desc.isSigned();
        int dcmFlags = signed ? Imgcodecs.DICOM_FLAG_SIGNED : Imgcodecs.DICOM_FLAG_UNSIGNED;
        if (signed) {
            Logger.warn(
                    false,
                    "Image",
                    "Force compression to JPEG-LS lossless as lossy is not adapted to signed data.");
            jpeglsNLE = 0;
            bitCompressed = 16; // Extend to bit allocated so negative values are not treated as large positives.
        }
        // The JPEG and JPEG-LS encoders can reduce non-byte streams to 8-bit unless promoted.
        if (bitCompressed == 8 && image.getSampleModel().getTransferType() != DataBuffer.TYPE_BYTE) {
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
        params[Imgcodecs.DICOM_PARAM_COLOR_MODEL] = monochromeOrRgb(channels); // Photometric interpretation
        params[Imgcodecs.DICOM_PARAM_JPEGLS_LOSSY_ERROR] = jpeglsNLE; // Lossy error for JPEG-LS
        return new MatOfInt(params);
    }

}
