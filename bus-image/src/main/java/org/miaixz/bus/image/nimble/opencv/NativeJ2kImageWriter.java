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
 * Represents the NativeJ2kImageWriter type.
 *
 * @author Kimi Liu
 */
public class NativeJ2kImageWriter extends AbstractNativeImageWriter {

    /**
     * Creates a new instance.
     *
     * @param originatingProvider the originating provider.
     */
    NativeJ2kImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    /**
     * Gets the default write param.
     *
     * @return the default write param.
     */
    @Override
    public ImageWriteParam getDefaultWriteParam() {
        return new J2kImageWriteParam(getLocale());
    }

    /**
     * Gets the codec name.
     *
     * @return the codec name.
     */
    @Override
    String codecName() {
        return "JPEG2000";
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
        // The J2K codec requires BGR as input color model.
        return ImageConversion.toMat(image, param.getSourceRegion(), true, desc.isSigned());
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
        J2kImageWriteParam j2kParams = (J2kImageWriteParam) param;
        int channels = CvType.channels(mat.type());
        int dcmFlags = desc.isSigned() ? Imgcodecs.DICOM_FLAG_SIGNED : Imgcodecs.DICOM_FLAG_UNSIGNED;

        int[] params = new int[16];
        params[Imgcodecs.DICOM_PARAM_IMREAD] = Imgcodecs.IMREAD_UNCHANGED; // Image flags
        params[Imgcodecs.DICOM_PARAM_DCM_IMREAD] = dcmFlags; // DICOM flags
        params[Imgcodecs.DICOM_PARAM_WIDTH] = mat.width(); // Image width
        params[Imgcodecs.DICOM_PARAM_HEIGHT] = mat.height(); // Image height
        params[Imgcodecs.DICOM_PARAM_COMPRESSION] = Imgcodecs.DICOM_CP_J2K; // Type of compression
        params[Imgcodecs.DICOM_PARAM_COMPONENTS] = channels; // Number of components
        params[Imgcodecs.DICOM_PARAM_BITS_PER_SAMPLE] = desc.getBitsCompressed(); // Bits per sample
        params[Imgcodecs.DICOM_PARAM_INTERLEAVE_MODE] = Imgcodecs.ILV_SAMPLE; // Interleave mode
        params[Imgcodecs.DICOM_PARAM_COLOR_MODEL] = monochromeOrRgb(channels); // Photometric interpretation
        params[Imgcodecs.DICOM_PARAM_J2K_COMPRESSION_FACTOR] = j2kParams.getCompressionRatiofactor();
        return new MatOfInt(params);
    }

}
