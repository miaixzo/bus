/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.awt.image.DataBuffer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.image.nimble.opencv.ImageCV;
import org.miaixz.bus.image.nimble.opencv.ImageProcessor;
import org.miaixz.bus.image.nimble.opencv.LookupTableCV;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.miaixz.bus.image.nimble.opencv.lut.PresentationStateLut;
import org.miaixz.bus.image.nimble.opencv.lut.WindLevelParameters;
import org.miaixz.bus.image.nimble.stream.ImageDescriptor;
import org.opencv.core.CvType;

/**
 * A utility class for performing various rendering operations on DICOM images.
 * <p>
 * This class provides static methods for applying transformations such as Modality LUTs, VOI LUTs (window/level), and
 * handling embedded overlays. It supports processing images with different data types.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageRendering {

    /**
     * Returns the raw rendered image with the Modality LUT applied, after clearing any embedded overlay bits.
     *
     * @param imageSource The source image.
     * @param desc        The image descriptor containing Modality LUT and overlay information.
     * @param params      The image read parameters, which may influence LUT selection.
     * @param frameIndex  The index of the frame to process (0 for single-frame images).
     * @return The raw rendered image with the Modality LUT applied.
     */
    public static PlanarImage getRawRenderedImage(
            final PlanarImage imageSource,
            ImageDescriptor desc,
            ImageReadParam params,
            int frameIndex) {
        PlanarImage img = getImageWithoutEmbeddedOverlay(imageSource, desc, frameIndex);
        ImageAdapter adapter = new ImageAdapter(img, desc, frameIndex);
        return getModalityLutImage(imageSource, adapter, params);
    }

    /**
     * Applies the Modality LUT to a given image.
     *
     * @param img     The source image.
     * @param adapter An adapter providing access to the Modality LUT.
     * @param params  The image read parameters, which may influence LUT selection.
     * @return The image with the Modality LUT applied.
     */
    public static PlanarImage getModalityLutImage(PlanarImage img, ImageAdapter adapter, ImageReadParam params) {
        WindLevelParameters p = new WindLevelParameters(adapter, params);
        int datatype = Objects.requireNonNull(img).type();
        if (datatype >= CvType.CV_8U && datatype < CvType.CV_32S) {
            LookupTableCV modalityLookup = adapter.getModalityLookup(p, p.isInverseLut());
            return modalityLookup == null ? img.toImageCV() : modalityLookup.lookup(img.toMat());
        }
        return img;
    }

    /**
     * Returns the default rendered image, which includes the application of a VOI LUT and any overlays.
     *
     * @param imageSource The source image.
     * @param desc        The image descriptor containing VOI LUT and overlay information.
     * @param params      The image read parameters, containing window/level settings.
     * @param frameIndex  The index of the frame to process (0 for single-frame images).
     * @return The default rendered image with VOI LUT and overlays applied.
     */
    public static PlanarImage getDefaultRenderedImage(
            final PlanarImage imageSource,
            ImageDescriptor desc,
            ImageReadParam params,
            int frameIndex) {
        PlanarImage img = getImageWithoutEmbeddedOverlay(imageSource, desc, frameIndex);
        img = getVoiLutImage(img, desc, params, frameIndex);
        return OverlayData.getOverlayImage(imageSource, img, desc, params, frameIndex);
    }

    /**
     * Returns the image with the VOI (Value of Interest) LUT applied.
     *
     * @param imageSource The source image.
     * @param desc        The image descriptor containing VOI LUT information.
     * @param params      The image read parameters, containing window/level settings.
     * @param frameIndex  The index of the frame to process (0 for single-frame images).
     * @return The image with the VOI LUT applied.
     */
    public static PlanarImage getVoiLutImage(
            final PlanarImage imageSource,
            ImageDescriptor desc,
            ImageReadParam params,
            int frameIndex) {
        ImageAdapter adapter = new ImageAdapter(imageSource, desc, frameIndex);
        return getVoiLutImage(imageSource, adapter, params);
    }

    /**
     * Returns the image with the VOI (Value of Interest) LUT applied.
     *
     * @param imageSource The source image.
     * @param adapter     An adapter providing access to the VOI LUT.
     * @param params      The image read parameters, containing window/level settings.
     * @return The image with the VOI LUT applied.
     */
    public static PlanarImage getVoiLutImage(PlanarImage imageSource, ImageAdapter adapter, ImageReadParam params) {
        WindLevelParameters p = new WindLevelParameters(adapter, params);
        int datatype = Objects.requireNonNull(imageSource).type();
        if (datatype >= CvType.CV_8U && datatype < CvType.CV_32S) {
            return getImageForByteOrShortData(imageSource, adapter, p);
        } else if (datatype >= CvType.CV_32S) {
            return getImageWithFloatOrIntData(imageSource, p, datatype);
        }
        return null;
    }

    /**
     * Processes an image with byte or short integer pixel data by applying Modality and VOI LUTs.
     *
     * @param imageSource The source image.
     * @param adapter     The image adapter for accessing LUTs.
     * @param p           The window and level parameters.
     * @return The processed image.
     */
    private static ImageCV getImageForByteOrShortData(
            PlanarImage imageSource,
            ImageAdapter adapter,
            WindLevelParameters p) {
        ImageDescriptor desc = adapter.getImageDescriptor();
        LookupTableCV modalityLookup = adapter.getModalityLookup(p, p.isInverseLut());
        ImageCV imageModalityTransformed = modalityLookup == null ? imageSource.toImageCV()
                : modalityLookup.lookup(imageSource.toMat());

        /*
         * Per DICOM Standard PS3.3 C.11.2.1.2 Window Center and Width:
         *
         * These Attributes are applied to the pixel data values of the image (or the result of the Modality LUT
         * transformation) to transform them to output values. The output values are specified by VOI LUT Function
         * (0028,1056). They are used only for images with Photometric Interpretation (0028,0004) values of MONOCHROME1
         * and MONOCHROME2. They have no meaning for other images.
         */
        if ((!p.isAllowWinLevelOnColorImage()
                || MathKit.isEqual(p.getWindow(), 255.0) && MathKit.isEqual(p.getLevel(), 127.5))
                && !desc.getPhotometricInterpretation().isMonochrome()) {
            // Do not apply VOI LUT if photometric interpretation is not monochrome (necessary for PALETTE COLOR).
            return imageModalityTransformed;
        }

        PresentationStateLut prDcm = p.getPresentationState();
        Optional<LookupTableCV> prLut = prDcm == null ? Optional.empty() : prDcm.getPrLut();
        LookupTableCV voiLookup = null;
        if (prLut.isEmpty() || p.getLutShape().getLookup() != null) {
            voiLookup = adapter.getVOILookup(p);
        }
        if (prLut.isEmpty()) {
            return voiLookup.lookup(imageModalityTransformed);
        }
        ImageCV imageVoiTransformed = voiLookup == null ? imageModalityTransformed
                : voiLookup.lookup(imageModalityTransformed);
        return prLut.get().lookup(imageVoiTransformed);
    }

    /**
     * Processes an image with float or integer pixel data by applying a window/level rescale operation.
     *
     * @param imageSource The source image.
     * @param p           The window and level parameters.
     * @param datatype    The data type of the image pixel data.
     * @return The processed image, rescaled to 8-bit.
     */
    private static ImageCV getImageWithFloatOrIntData(PlanarImage imageSource, WindLevelParameters p, int datatype) {
        double low = p.getLevel() - p.getWindow() / 2.0;
        double high = p.getLevel() + p.getWindow() / 2.0;
        double range = high - low;
        if (range < 1.0 && datatype == DataBuffer.TYPE_INT) {
            range = 1.0;
        }
        double slope = 255.0 / range;
        double yint = 255.0 - slope * high;
        return ImageProcessor.rescaleToByte(ImageCV.toMat(imageSource), slope, yint);
    }

    /**
     * Returns an image with embedded overlay bits cleared. If the image contains embedded overlays, this method zeroes
     * out any bits that are outside the range defined by Bits Stored and High Bit.
     * <p>
     * Per DICOM Standard PS3.5, Chapter 8.1.2, for overlays encoded in the Overlay Data element (60xx,3000), Overlay
     * Bits Allocated (60xx,0100) is always 1, and Overlay Bit Position (60xx,0102) is always 0. This method handles
     * overlays embedded in the high bits of the pixel data.
     *
     * @param img        The source image.
     * @param desc       The image descriptor containing overlay and pixel information.
     * @param frameIndex The index of the frame to process (0 for single-frame images).
     * @return A new image with overlay bits cleared, or the original image if no embedded overlays are present.
     */
    public static PlanarImage getImageWithoutEmbeddedOverlay(PlanarImage img, ImageDescriptor desc, int frameIndex) {
        Objects.requireNonNull(img);
        List<EmbeddedOverlay> embeddedOverlays = Objects.requireNonNull(desc).getEmbeddedOverlay();
        if (!embeddedOverlays.isEmpty()) {
            int bitsStored = desc.getBitsStored();
            int bitsAllocated = desc.getBitsAllocated();
            if (bitsStored < desc.getBitsAllocated() && bitsAllocated >= 8 && bitsAllocated <= 16) {
                int highBit = desc.getHighBit();
                int high = highBit + 1;
                int val = (1 << high) - 1;
                if (high > bitsStored) {
                    val -= (1 << (high - bitsStored)) - 1;
                }
                // Set all bits higher than highBit and lower than (high - bitsStored) to 0.
                if (high > bitsStored) {
                    desc.getModalityLutForFrame(frameIndex).adaptWithOverlayBitMask(high - bitsStored);
                }
                // Set all bits outside of Bits Stored to 0.
                return ImageProcessor.bitwiseAnd(img.toMat(), val);
            }
        }
        return img;
    }

}
