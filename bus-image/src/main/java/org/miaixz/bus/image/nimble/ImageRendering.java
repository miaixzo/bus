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
package org.miaixz.bus.image.nimble;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.image.nimble.opencv.ImageCV;
import org.miaixz.bus.image.nimble.opencv.ImageTransformer;
import org.miaixz.bus.image.nimble.opencv.LookupTableCV;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.miaixz.bus.image.nimble.opencv.lut.WindLevelParameters;
import org.miaixz.bus.image.nimble.stream.ImageDescriptor;

/**
 * Performs various rendering operations on DICOM images.
 * <p>
 * This class provides static methods for applying transformations such as Modality LUTs, VOI LUTs (window/level), and
 * handling embedded overlays. It supports processing images with different data types.
 *
 * @author Kimi Liu
 */
public class ImageRendering {

    /**
     * Creates a rendering helper instance for compatibility with existing callers.
     */
    public ImageRendering() {
    }

    /**
     * Returns the raw rendered image with modality LUT applied and without embedded overlays.
     *
     * @param imageSource the source image (must not be null)
     * @param desc        the image descriptor containing modality LUT and overlay information (must not be null)
     * @param params      the window/level parameters, or null to use image defaults
     * @param frameIndex  the index of the frame to process (0 for single-frame images)
     * @return the raw rendered image with modality LUT applied
     * @throws NullPointerException if the source image or descriptor is null
     */
    public static PlanarImage getRawRenderedImage(
            PlanarImage imageSource,
            ImageDescriptor desc,
            ImageReadParam params,
            int frameIndex) {
        var imageWithoutOverlay = getImageWithoutEmbeddedOverlay(imageSource, desc, frameIndex);
        PlanarImage result = null;
        try {
            var adapter = new ImageAdapter(imageWithoutOverlay, desc, frameIndex);
            result = getModalityLutImage(imageWithoutOverlay, adapter, params);
            return result;
        } finally {
            releaseIntermediate(imageWithoutOverlay, imageSource, result);
        }
    }

    /**
     * Returns the image with modality LUT applied based on the provided adapter and parameters.
     *
     * @param img     the source image (must not be null)
     * @param adapter the ImageAdapter containing modality LUT information (must not be null)
     * @param params  the ImageReadParam containing window/level parameters
     * @return the image with modality LUT applied, or the original image if no transformation is needed
     * @throws NullPointerException if the source image or adapter is null
     */
    public static PlanarImage getModalityLutImage(PlanarImage img, ImageAdapter adapter, ImageReadParam params) {
        var windLevelParams = new WindLevelParameters(adapter, params);
        int dataType = Objects.requireNonNull(img).type();

        return isIntegerDataType(dataType) ? applyModalityLutToIntegerData(img, adapter, windLevelParams) : img;
    }

    /**
     * Returns the default rendered image with VOI LUT and overlays applied.
     *
     * @param imageSource the source image (must not be null)
     * @param desc        the image descriptor containing VOI LUT and overlay information (must not be null)
     * @param params      the window/level and overlay parameters, or null to use image defaults without painted
     *                    overlays
     * @param frameIndex  the index of the frame to process (0 for single-frame images)
     * @return the fully rendered image with VOI LUT and overlays applied
     * @throws NullPointerException if the source image or descriptor is null
     */
    public static PlanarImage getDefaultRenderedImage(
            PlanarImage imageSource,
            ImageDescriptor desc,
            ImageReadParam params,
            int frameIndex) {
        var imageWithoutOverlay = getImageWithoutEmbeddedOverlay(imageSource, desc, frameIndex);
        PlanarImage voiProcessedImage = null;
        PlanarImage result = null;
        try {
            voiProcessedImage = getVoiLutImage(imageWithoutOverlay, desc, params, frameIndex);
            result = OverlayData.getOverlayImage(imageSource, voiProcessedImage, desc, params, frameIndex);
            return result;
        } finally {
            releaseIntermediate(voiProcessedImage, imageSource, result);
            if (imageWithoutOverlay != voiProcessedImage) {
                releaseIntermediate(imageWithoutOverlay, imageSource, result);
            }
        }
    }

    /**
     * Applies VOI LUT transformation to the specified image.
     *
     * @param imageSource the source image (must not be null)
     * @param desc        the image descriptor containing VOI LUT information (must not be null)
     * @param params      the window/level parameters, or null to use image defaults
     * @param frameIndex  the index of the frame to process (0 for single-frame images)
     * @return the image with VOI LUT applied
     * @throws NullPointerException if the source image or descriptor is null
     */
    public static PlanarImage getVoiLutImage(
            PlanarImage imageSource,
            ImageDescriptor desc,
            ImageReadParam params,
            int frameIndex) {
        var adapter = new ImageAdapter(imageSource, desc, frameIndex);
        return getVoiLutImage(imageSource, adapter, params);
    }

    /**
     * Applies VOI LUT transformation using the provided adapter.
     *
     * @param imageSource the source image (must not be null)
     * @param adapter     the ImageAdapter containing VOI LUT information (must not be null)
     * @param params      the window/level parameters, or null to use image defaults
     * @return the image with VOI LUT applied, or null if the data type is not supported
     * @throws NullPointerException if the source image or adapter is null
     */
    public static PlanarImage getVoiLutImage(PlanarImage imageSource, ImageAdapter adapter, ImageReadParam params) {
        var windLevelParams = new WindLevelParameters(adapter, params);
        int dataType = Objects.requireNonNull(imageSource).type();

        PlanarImage gray = switch (getDataTypeCategory(dataType)) {
            case INTEGER -> processIntegerDataForVoi(imageSource, adapter, windLevelParams);
            case FLOATING_POINT -> processFloatingPointDataForVoi(imageSource, windLevelParams, dataType);
            case UNSUPPORTED -> null;
        };
        ImageDescriptor desc = adapter.getImageDescriptor();
        boolean supplemental = params == null || params.getApplySupplementalPalette().orElse(Boolean.TRUE);
        if (gray != null && supplemental && desc.hasSupplementalPaletteColorLookupTable()
                && desc.getPaletteColorLookupTable() != null && isIntegerDataType(dataType)) {
            try {
                return applySupplementalPalette(
                        imageSource,
                        gray,
                        desc.getPaletteColorLookupTable(),
                        windLevelParams.getOutputBits());
            } finally {
                releaseIntermediate(gray, imageSource, null);
            }
        }
        return gray;
    }

    /**
     * Removes embedded overlays from the image by masking overlay bits.
     *
     * <p>
     * For overlays encoded in Overlay Data Element (60xx,3000), Overlay Bits Allocated (60xx,0100) is always 1 and
     * Overlay Bit Position (60xx,0102) is always 0.
     *
     * @param img        the source image (must not be null)
     * @param desc       the image descriptor containing overlay information (must not be null)
     * @param frameIndex the index of the frame to process (0 for single-frame images)
     * @return the image without embedded overlays
     * @throws NullPointerException if any parameter is null
     * @see <a href="http://dicom.nema.org/medical/dicom/current/output/chtml/part05/chapter_8.html"> 8.1.2 Overlay data
     *      encoding of related data elements</a>
     */
    public static PlanarImage getImageWithoutEmbeddedOverlay(PlanarImage img, ImageDescriptor desc, int frameIndex) {
        Objects.requireNonNull(img);
        List<EmbeddedOverlay> embeddedOverlays = Objects.requireNonNull(desc).getEmbeddedOverlay();

        if (embeddedOverlays.isEmpty()) {
            return img;
        }

        int bitsStored = desc.getBitsStored();
        int bitsAllocated = desc.getBitsAllocated();

        if (shouldApplyOverlayMask(bitsStored, bitsAllocated)) {
            int overlayMask = calculateOverlayMask(desc, frameIndex, bitsStored);
            return ImageTransformer.bitwiseAnd(img.toMat(), overlayMask);
        }

        return img;
    }

    /**
     * Colors a rendered grayscale image with a Supplemental Palette Color LUT (PS3.3 C.8.16.2): the pixels whose stored
     * value is at least the first value mapped by the palette take its color, the others keep their gray level from the
     * grayscale pipeline.
     *
     * @param stored   the stored pixel values
     * @param gray     the output of the grayscale pipeline for the same pixels
     * @param palette  the supplemental palette whose offset separates grayscale and color pixels
     * @param grayBits the bit depth of {@code gray} when it is not 8-bit
     * @return an 8-bit BGR image
     */
    public static ImageCV applySupplementalPalette(
            PlanarImage stored,
            PlanarImage gray,
            LookupTableCV palette,
            int grayBits) {
        Mat grayMat = gray.toMat();
        Mat gray8 = grayMat;
        ImageCV out = new ImageCV();
        Mat mask = new Mat();
        PlanarImage colors = null;
        try {
            if (grayMat.depth() != CvType.CV_8U) {
                gray8 = new Mat();
                int bits = Math.max(1, Math.min(grayBits, 16));
                grayMat.convertTo(gray8, CvType.CV_8U, 255.0 / ((1 << bits) - 1));
            }
            Imgproc.cvtColor(gray8, out, Imgproc.COLOR_GRAY2BGR);
            Core.compare(stored.toMat(), new Scalar(palette.getOffset()), mask, Core.CMP_GE);
            colors = RGBImageVoiLut.getRGBImageFromPaletteColorModel(stored, palette);
            colors.toMat().copyTo(out, mask);
            return out;
        } catch (RuntimeException | Error e) {
            out.release();
            throw e;
        } finally {
            if (gray8 != grayMat) {
                gray8.release();
            }
            mask.release();
            releaseIntermediate(colors, stored, gray);
        }
    }

    /**
     * Data type categories for processing logic
     *
     * @author Kimi Liu
     */
    private enum DataTypeCategory {
        /**
         * The integer pixel processing category.
         */
        INTEGER,
        /**
         * The floating point pixel processing category.
         */
        FLOATING_POINT,
        /**
         * The unsupported pixel processing category.
         */
        UNSUPPORTED

    }

    /**
     * Classifies an OpenCV pixel depth independently of its channel count.
     *
     * @param dataType the OpenCV pixel type, including its channel count
     * @return the rendering category of the pixel depth
     */
    private static DataTypeCategory getDataTypeCategory(int dataType) {
        int depth = CvType.depth(dataType);
        if (depth >= CvType.CV_8U && depth < CvType.CV_32S) {
            return DataTypeCategory.INTEGER;
        }
        if (depth == CvType.CV_32S || depth == CvType.CV_32F || depth == CvType.CV_64F) {
            return DataTypeCategory.FLOATING_POINT;
        }
        return DataTypeCategory.UNSUPPORTED;
    }

    /**
     * Tests whether a pixel type supports the 8-bit or 16-bit integer LUT pipeline.
     *
     * @param dataType the OpenCV pixel type, including its channel count
     * @return whether integer LUT processing is supported
     */
    private static boolean isIntegerDataType(int dataType) {
        return getDataTypeCategory(dataType) == DataTypeCategory.INTEGER;
    }

    /**
     * Applies the frame modality transform without taking ownership of the input image.
     *
     * @param img     the input image retained by the caller
     * @param adapter the image adapter providing frame-specific pixel and LUT metadata
     * @param params  the window, level, precision and palette options
     * @return a new transformed image, or the source when no lookup applies
     */
    private static ImageCV applyModalityLutToIntegerData(
            PlanarImage img,
            ImageAdapter adapter,
            WindLevelParameters params) {
        var modalityLookup = adapter.getModalityLookup(params, params.isInverseLut());
        return modalityLookup == null ? img.toImageCV() : modalityLookup.lookup(img.toMat());
    }

    /**
     * Fuses scalar modality, VOI and presentation lookups through a cached stored-value ramp when possible.
     *
     * @param imageSource the source image retained by the caller
     * @param adapter     the image adapter providing frame-specific pixel and LUT metadata
     * @param params      the window, level, precision and palette options
     * @return the rendered image; the caller retains ownership of the source
     */
    private static ImageCV processIntegerDataForVoi(
            PlanarImage imageSource,
            ImageAdapter adapter,
            WindLevelParameters params) {
        ImageCV storedValues = StoredValue.ramp(imageSource.type());
        if (storedValues == null) {
            return applyLookupChain(imageSource, adapter, params);
        }
        // The chain is a function of the stored value alone: run on every possible value it gives the
        // table of the whole transformation, which one native pass then applies to the image.
        ImageCV table = applyLookupChain(storedValues, adapter, params);
        if (table == storedValues) {
            return imageSource.toImageCV(); // no lookup applies
        }
        if (table.channels() != 1) {
            // A table that adds bands (color presentation LUT) cannot be applied by Core.LUT
            table.release();
            return applyLookupChain(imageSource, adapter, params);
        }
        var result = new ImageCV();
        try {
            Core.LUT(imageSource.toMat(), table, result);
            return result;
        } catch (RuntimeException | Error e) {
            result.release();
            throw e;
        } finally {
            table.release();
        }
    }

    /**
     * Applies modality, VOI and presentation lookups while releasing superseded intermediates.
     *
     * @param imageSource the source image retained by the caller
     * @param adapter     the image adapter providing frame-specific pixel and LUT metadata
     * @param params      the window, level, precision and palette options
     * @return the rendered image, which may alias the source
     */
    static ImageCV applyLookupChain(PlanarImage imageSource, ImageAdapter adapter, WindLevelParameters params) {
        var modalityLookup = adapter.getModalityLookup(params, params.isInverseLut());
        var imageModalityTransformed = applyModalityTransformation(imageSource, modalityLookup);

        ImageCV result = null;
        try {
            result = shouldSkipVoiLutForColorImage(params, adapter.getImageDescriptor()) ? imageModalityTransformed
                    : applyVoiAndPresentationLuts(imageModalityTransformed, adapter, params);
            return result;
        } finally {
            releaseIntermediate(imageModalityTransformed, imageSource, result);
        }
    }

    /**
     * Applies a modality lookup when present without modifying the source image.
     *
     * @param imageSource    the source image retained by the caller
     * @param modalityLookup the modality LUT, or null when the modality stage is an identity
     * @return the transformed image, or the source when no LUT applies
     */
    private static ImageCV applyModalityTransformation(PlanarImage imageSource, LookupTableCV modalityLookup) {
        return modalityLookup == null ? imageSource.toImageCV() : modalityLookup.lookup(imageSource.toMat());
    }

    /**
     * Determines whether the color-image policy bypasses monochrome windowing.
     *
     * @param params the window, level, precision and palette options
     * @param desc   the image metadata and frame-specific LUT state
     * @return whether the VOI stage must be skipped
     */
    private static boolean shouldSkipVoiLutForColorImage(WindLevelParameters params, ImageDescriptor desc) {
        /*
         * C.11.2.1.2 Window center and window width
         *
         * These Attributes shall be used only for Images with Photometric Interpretation (0028,0004) values of
         * MONOCHROME1 and MONOCHROME2. They have no meaning for other Images.
         */
        var photometricInterpretation = desc.getPhotometricInterpretation();
        if (photometricInterpretation.isMonochrome()) {
            return false;
        }

        boolean isDefaultWindow = MathKit.isEqual(params.getWindow(), 255.0)
                && MathKit.isEqual(params.getLevel(), 127.5);
        return !params.isAllowWinLevelOnColorImage() || isDefaultWindow;
    }

    /**
     * Applies the selected VOI and presentation tables, releasing superseded VOI intermediates.
     *
     * @param imageModalityTransformed the caller-owned output of the modality stage
     * @param adapter                  the image adapter providing frame-specific pixel and LUT metadata
     * @param params                   the window, level, precision and palette options
     * @return the transformed image, which may alias the input
     */
    private static ImageCV applyVoiAndPresentationLuts(
            ImageCV imageModalityTransformed,
            ImageAdapter adapter,
            WindLevelParameters params) {
        var presentationState = params.getPresentationState();
        Optional<LookupTableCV> presentationLut = presentationState == null ? Optional.empty()
                : presentationState.getPrLut();
        var lutShape = params.getLutShape();
        LookupTableCV voiLookup = null;
        if (presentationLut.isEmpty() || (lutShape != null && lutShape.getLookup() != null)) {
            voiLookup = adapter.getVOILookup(params);
        }

        var imageVoiTransformed = voiLookup == null ? imageModalityTransformed
                : voiLookup.lookup(imageModalityTransformed);

        ImageCV result = null;
        try {
            result = presentationLut.map(lut -> lut.lookup(imageVoiTransformed)).orElse(imageVoiTransformed);
            return result;
        } finally {
            releaseIntermediate(imageVoiTransformed, imageModalityTransformed, result);
        }
    }

    /**
     * Rescales floating-point or 32-bit integer pixels to the requested unsigned output precision.
     *
     * @param imageSource the source image retained by the caller
     * @param params      the window, level, precision and palette options
     * @param dataType    the OpenCV pixel type, including its channel count
     * @return a new rendered image with the source channel count
     */
    private static ImageCV processFloatingPointDataForVoi(
            PlanarImage imageSource,
            WindLevelParameters params,
            int dataType) {
        double window = params.getWindow();
        double level = params.getLevel();

        double low = level - window / 2.0;
        double high = level + window / 2.0;
        double range = calculateRange(high, low, dataType);
        int bits = ImageAdapter.voiOutputBits(params);
        if (bits > 8) {
            double maxOut = (1 << bits) - 1.0;
            double slope = maxOut / range;
            var result = new ImageCV();
            ImageCV.toMat(imageSource).convertTo(result, CvType.CV_16U, slope, maxOut - slope * high);
            return result;
        }
        double slope = 255.0 / range;
        double yIntercept = 255.0 - slope * high;

        return ImageTransformer.rescaleToByte(ImageCV.toMat(imageSource), slope, yIntercept);
    }

    /**
     * Calculates a finite positive rescale range, enforcing a one-unit minimum for 32-bit integer pixels.
     *
     * @param high     the upper window bound
     * @param low      the lower window bound
     * @param dataType the OpenCV pixel type, including its channel count
     * @return the positive rescale range
     */
    private static double calculateRange(double high, double low, int dataType) {
        double range = high - low;
        if (!Double.isFinite(range) || range <= 0.0) {
            return 1.0;
        }
        return range < 1.0 && CvType.depth(dataType) == CvType.CV_32S ? 1.0 : range;
    }

    /**
     * Checks whether unused bits can be masked in an 8-bit or 16-bit stored sample.
     *
     * @param bitsStored    the number of meaningful stored bits per sample
     * @param bitsAllocated the number of allocated bits per sample
     * @return whether an embedded-overlay mask is required
     */
    private static boolean shouldApplyOverlayMask(int bitsStored, int bitsAllocated) {
        return bitsStored < bitsAllocated && bitsAllocated >= 8 && bitsAllocated <= 16;
    }

    /**
     * Builds the stored-sample mask and adapts the frame-local modality slope for discarded low bits.
     *
     * @param desc       the image metadata and frame-specific LUT state
     * @param frameIndex the zero-based image frame index
     * @param bitsStored the number of meaningful stored bits per sample
     * @return the bit mask retaining stored pixel bits
     */
    private static int calculateOverlayMask(ImageDescriptor desc, int frameIndex, int bitsStored) {
        int highBit = desc.getHighBit();
        int high = highBit + 1;
        int mask = (1 << high) - 1;
        if (high > bitsStored) {
            mask -= (1 << (high - bitsStored)) - 1;
            adaptModalityLutForOverlay(desc, frameIndex, high - bitsStored);
        }
        return mask;
    }

    /**
     * Installs a frame-local overlay-compensated module without mutating the shared modality defaults.
     *
     * @param desc        the image metadata and frame-specific LUT state
     * @param frameIndex  the zero-based image frame index
     * @param overlayBits the low-order overlay bits excluded from the stored sample
     */
    private static void adaptModalityLutForOverlay(ImageDescriptor desc, int frameIndex, int overlayBits) {
        var modalityLut = desc.getModalityLutForFrame(frameIndex);
        if (modalityLut != null) {
            desc.setModalityLutForFrame(frameIndex, modalityLut.withOverlayBitMask(overlayBits));
        }
    }

    /**
     * Releases an owned intermediate unless it aliases the caller's source or the returned result.
     *
     * @param image  the temporary image, or {@code null} if no image was created
     * @param source the image owned by the caller
     * @param result the image whose ownership is transferred to the caller, or {@code null} on failure
     */
    private static void releaseIntermediate(PlanarImage image, PlanarImage source, PlanarImage result) {
        if (image != null && image != source && image != result) {
            image.release();
        }
    }

}
