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

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.image.nimble.Photometric;
import org.miaixz.bus.image.nimble.codec.BytesWithImageImageDescriptor;
import org.miaixz.bus.image.nimble.codec.ImageDescriptor;

/**
 * Base class for native OpenCV DICOM image writers.
 *
 * @author Kimi Liu
 */
abstract class AbstractNativeImageWriter extends ImageWriter {

    static {
        new OpenCVNativeLoader().init();
    }

    /**
     * Creates a new instance.
     *
     * @param originatingProvider the originating provider
     */
    AbstractNativeImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    /**
     * Gets the codec name.
     *
     * @return the codec name
     */
    abstract String codecName();

    /**
     * Converts the rendered image to a native matrix.
     *
     * @param image the rendered image
     * @param param the write param
     * @param desc  the image descriptor
     * @return the native matrix
     */
    abstract ImageCV toMat(RenderedImage image, ImageWriteParam param, ImageDescriptor desc);

    /**
     * Builds native DICOM encoder parameters.
     *
     * @param mat   the native matrix
     * @param image the rendered image
     * @param param the write param
     * @param desc  the image descriptor
     * @return the native parameters
     */
    abstract MatOfInt buildDicomParams(ImageCV mat, RenderedImage image, ImageWriteParam param, ImageDescriptor desc);

    /**
     * Validates codec-specific write parameters.
     *
     * @param param the write param
     * @param desc  the image descriptor
     */
    void validate(ImageWriteParam param, ImageDescriptor desc) {
    }

    /**
     * Executes the write operation.
     *
     * @param streamMetadata the stream metadata
     * @param image          the image
     * @param param          the write param
     * @throws IOException if the operation cannot be completed
     */
    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        ImageOutputStream stream = requireOutputStream();
        stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        ImageDescriptor desc = requireImageDescriptor(stream);
        validate(param, desc);

        RenderedImage renderedImage = image.getRenderedImage();
        Mat buf = null;
        MatOfInt dicomParams = null;
        try {
            ImageCV mat = null;
            try {
                mat = toMat(renderedImage, param, desc);
                dicomParams = buildDicomParams(mat, renderedImage, param, desc);
                buf = Imgcodecs.dicomJpgWrite(mat, dicomParams, Normal.EMPTY);
                if (buf.empty()) {
                    throw new IIOException("Native " + codecName() + " encoding error: null image");
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
            throw new IIOException("Native " + codecName() + " encoding error", t);
        } finally {
            NativeImageReader.closeMat(dicomParams);
            NativeImageReader.closeMat(buf);
        }
    }

    /**
     * Requires an image output stream.
     *
     * @return the image output stream
     */
    private ImageOutputStream requireOutputStream() {
        if (output == null) {
            throw new IllegalStateException("output cannot be null");
        }
        if (!(output instanceof ImageOutputStream)) {
            throw new IllegalArgumentException("output is not an ImageOutputStream!");
        }
        return (ImageOutputStream) output;
    }

    /**
     * Requires an image descriptor from the stream.
     *
     * @param stream the image output stream
     * @return the image descriptor
     */
    private static ImageDescriptor requireImageDescriptor(ImageOutputStream stream) {
        if (!(stream instanceof BytesWithImageImageDescriptor)) {
            throw new IllegalArgumentException("stream does not implement BytesWithImageImageDescriptor!");
        }
        return ((BytesWithImageImageDescriptor) stream).getImageDescriptor();
    }

    /**
     * Gets the native color model for one or multiple channels.
     *
     * @param channels the channel count
     * @return the native color model
     */
    static int monochromeOrRgb(int channels) {
        return channels == 1 ? Imgcodecs.EPI_Monochrome2 : Imgcodecs.EPI_RGB;
    }

    /**
     * Rejects chroma-subsampled photometric interpretations for true-lossless encoding.
     *
     * @param lossless whether compression is lossless
     * @param pi       the photometric interpretation
     */
    static void rejectChromaSubsampledLossless(boolean lossless, Photometric pi) {
        if (lossless && (Photometric.YBR_FULL_422 == pi || Photometric.YBR_PARTIAL_422 == pi
                || Photometric.YBR_PARTIAL_420 == pi || Photometric.YBR_ICT == pi || Photometric.YBR_RCT == pi)) {
            throw new IllegalArgumentException(
                    "True lossless encoder: Photometric interpretation is not supported: " + pi);
        }
    }

    /**
     * Gets the default stream metadata.
     *
     * @param param the param
     * @return the default stream metadata
     */
    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return null;
    }

    /**
     * Gets the default image metadata.
     *
     * @param imageType the image type
     * @param param     the param
     * @return the default image metadata
     */
    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    /**
     * Converts stream metadata.
     *
     * @param inData the input metadata
     * @param param  the param
     * @return the converted metadata
     */
    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
        return null;
    }

    /**
     * Converts image metadata.
     *
     * @param inData    the input metadata
     * @param imageType the image type
     * @param param     the param
     * @return the converted metadata
     */
    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

}
