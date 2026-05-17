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
import java.io.IOException;
import java.nio.ByteOrder;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import org.miaixz.bus.image.nimble.Photometric;
import org.miaixz.bus.image.nimble.codec.BytesWithImageImageDescriptor;
import org.miaixz.bus.image.nimble.codec.ImageDescriptor;

/**
 * Native OpenCV JPEG XL image writer.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NativeJXLImageWriter extends ImageWriter {

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
     * Executes the write operation.
     *
     * @param streamMetadata the stream metadata.
     * @param image          the image.
     * @param param          the param.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        if (output == null)
            throw new IllegalStateException("output cannot be null");
        if (!(output instanceof ImageOutputStream stream))
            throw new IllegalArgumentException("output is not an ImageOutputStream!");

        stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        JXLImageWriteParam jxlParams = param instanceof JXLImageWriteParam p ? p
                : (JXLImageWriteParam) getDefaultWriteParam();
        ImageWriteParam writeParam = param != null ? param : jxlParams;

        if (!(stream instanceof BytesWithImageImageDescriptor descriptorStream))
            throw new IllegalArgumentException("stream does not implement BytesWithImageImageDescriptor!");

        ImageDescriptor desc = descriptorStream.getImageDescriptor();
        Photometric pi = desc.getPhotometricInterpretation();
        if (jxlParams.isCompressionLossless() && (Photometric.YBR_FULL_422 == pi || Photometric.YBR_PARTIAL_422 == pi
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
                mat = ImageConversion.toMat(renderedImage, writeParam.getSourceRegion(), true);
                int bitCompressed = ((desc.getBitsCompressed() + 7) / 8) * 8;
                int channels = CvType.channels(mat.type());
                int epi = channels == 1 ? Imgcodecs.EPI_Monochrome2 : Imgcodecs.EPI_RGB;
                int dcmFlags = desc.isSigned() ? Imgcodecs.DICOM_FLAG_SIGNED : Imgcodecs.DICOM_FLAG_UNSIGNED;

                int[] params = new int[18];
                params[Imgcodecs.DICOM_PARAM_IMREAD] = Imgcodecs.IMREAD_UNCHANGED;
                params[Imgcodecs.DICOM_PARAM_DCM_IMREAD] = dcmFlags;
                params[Imgcodecs.DICOM_PARAM_WIDTH] = mat.width();
                params[Imgcodecs.DICOM_PARAM_HEIGHT] = mat.height();
                params[Imgcodecs.DICOM_PARAM_COMPRESSION] = Imgcodecs.DICOM_CP_JXL;
                params[Imgcodecs.DICOM_PARAM_COMPONENTS] = channels;
                params[Imgcodecs.DICOM_PARAM_BITS_PER_SAMPLE] = bitCompressed;
                params[Imgcodecs.DICOM_PARAM_INTERLEAVE_MODE] = Imgcodecs.ILV_SAMPLE;
                params[Imgcodecs.DICOM_PARAM_COLOR_MODEL] = epi;
                params[Imgcodecs.DICOM_PARAM_JPEG_QUALITY] = (int) (jxlParams.getEffectiveQuality() * 100);
                params[Imgcodecs.DICOM_PARAM_JXL_EFFORT] = jxlParams.getEffort();
                params[Imgcodecs.DICOM_PARAM_JXL_DECODING_SPEED] = jxlParams.getDecodingSpeed();

                dicomParams = new MatOfInt(params);
                buf = Imgcodecs.dicomJpgWrite(mat, dicomParams, "");
                if (buf.empty())
                    throw new IIOException("Native JPEG XL encoding error: null image");
            } finally {
                if (mat != null)
                    mat.release();
            }

            byte[] bSrcData = new byte[buf.width() * buf.height() * (int) buf.elemSize()];
            buf.get(0, 0, bSrcData);
            stream.write(bSrcData);
        } catch (Throwable t) {
            throw new IIOException("Native JPEG XL encoding error", t);
        } finally {
            NativeImageReader.closeMat(dicomParams);
            NativeImageReader.closeMat(buf);
        }
    }

    /**
     * Gets the default stream metadata.
     *
     * @param param the param.
     * @return the default stream metadata.
     */
    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return null;
    }

    /**
     * Gets the default image metadata.
     *
     * @param imageType the image type.
     * @param param     the param.
     * @return the default image metadata.
     */
    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    /**
     * Executes the convert stream metadata operation.
     *
     * @param inData the in data.
     * @param param  the param.
     * @return the operation result.
     */
    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
        return null;
    }

    /**
     * Executes the convert image metadata operation.
     *
     * @param inData    the in data.
     * @param imageType the image type.
     * @param param     the param.
     * @return the operation result.
     */
    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

}
