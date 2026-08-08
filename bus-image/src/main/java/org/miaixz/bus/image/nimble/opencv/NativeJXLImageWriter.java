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

import java.awt.image.RenderedImage;

import javax.imageio.ImageWriteParam;
import javax.imageio.spi.ImageWriterSpi;

import org.opencv.core.CvType;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import org.miaixz.bus.image.nimble.codec.ImageDescriptor;

/**
 * Native OpenCV JPEG XL image writer.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NativeJXLImageWriter extends AbstractNativeImageWriter {

    /**
     * Creates a new instance.
     *
     * @param originatingProvider the originating provider.
     */
    NativeJXLImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    /**
     * Gets the default write param.
     *
     * @return the default write param.
     */
    @Override
    public ImageWriteParam getDefaultWriteParam() {
        return new JXLImageWriteParam(getLocale());
    }

    /**
     * Gets the codec name.
     *
     * @return the codec name.
     */
    @Override
    String codecName() {
        return "JPEG XL";
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
        // JXL codec requires BGR or Gray.
        return ImageConversion.toMat(image, param.getSourceRegion(), true);
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
        JXLImageWriteParam jxlParams = (JXLImageWriteParam) param;
        int bitCompressed = ((desc.getBitsCompressed() + 7) / 8) * 8;
        int channels = CvType.channels(mat.type());
        int dcmFlags = desc.isSigned() ? Imgcodecs.DICOM_FLAG_SIGNED : Imgcodecs.DICOM_FLAG_UNSIGNED;

        int[] params = new int[18];
        params[Imgcodecs.DICOM_PARAM_IMREAD] = Imgcodecs.IMREAD_UNCHANGED; // Image flags
        params[Imgcodecs.DICOM_PARAM_DCM_IMREAD] = dcmFlags; // DICOM flags
        params[Imgcodecs.DICOM_PARAM_WIDTH] = mat.width(); // Image width
        params[Imgcodecs.DICOM_PARAM_HEIGHT] = mat.height(); // Image height
        params[Imgcodecs.DICOM_PARAM_COMPRESSION] = Imgcodecs.DICOM_CP_JXL; // Type of compression
        params[Imgcodecs.DICOM_PARAM_COMPONENTS] = channels; // Number of components
        params[Imgcodecs.DICOM_PARAM_BITS_PER_SAMPLE] = bitCompressed; // Bits per sample
        params[Imgcodecs.DICOM_PARAM_INTERLEAVE_MODE] = Imgcodecs.ILV_SAMPLE; // Interleave mode
        params[Imgcodecs.DICOM_PARAM_COLOR_MODEL] = monochromeOrRgb(channels); // Photometric interpretation
        params[Imgcodecs.DICOM_PARAM_JPEG_QUALITY] = (int) (jxlParams.getEffectiveQuality() * 100);
        params[Imgcodecs.DICOM_PARAM_JXL_EFFORT] = jxlParams.getEffort(); // Effort (1-9)
        params[Imgcodecs.DICOM_PARAM_JXL_DECODING_SPEED] = jxlParams.getDecodingSpeed();
        return new MatOfInt(params);
    }

}
