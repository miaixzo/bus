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

import static org.miaixz.bus.image.nimble.opencv.ImageIOHandler.NULL_SOURCE_IMAGE_ERROR;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Provides comprehensive image transformation operations for medical imaging applications.
 * <p>
 * This class encompasses all geometric and visual transformations including:
 * <ul>
 * <li>Geometric transformations (scaling, rotation, flipping, affine transforms)
 * <li>Pixel value transformations (LUT application, rescaling, inversion)
 * <li>Visual effects (overlay, merging, shutter effects)
 * <li>Region operations (cropping, masking)
 * </ul>
 * <p>
 * All methods are optimized for medical image processing workflows and maintain precision required for diagnostic
 * applications. The class supports both OpenCV Mat objects and standard Java imaging interfaces.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ImageTransformer {

    /**
     * Creates a new instance.
     */
    private ImageTransformer() {
        // No initialization required.
    }

    /**
     * Crops the image to the specified rectangular region.
     * <p>
     * Extracts a rectangular portion from the source image, creating a new image with the specified dimensions. This
     * operation is fundamental for creating region-of-interest views and reducing processing overhead.
     *
     * @param source the source Mat to crop
     * @param area   the rectangular area to extract. Coordinates are relative to source image origin
     * @return a new {@link ImageCV} containing only the specified rectangular region
     * @throws IllegalArgumentException if source is null, area is null, or area extends beyond image bounds
     */
    public static ImageCV crop(Mat source, Rectangle area) {
        ImageIOHandler.validateSource(source);
        Objects.requireNonNull(area, "Crop area cannot be null");

        var imageBounds = new Rectangle(0, 0, source.width(), source.height());
        var cropRect = area.intersection(imageBounds);

        if (cropRect.width > 1 && cropRect.height > 1) {
            return ImageCV.fromMat(source.submat(new Rect(cropRect.x, cropRect.y, cropRect.width, cropRect.height)));
        }
        return ImageCV.fromMat(source.clone());
    }

    /**
     * Resizes the image to the specified dimensions using default interpolation.
     * <p>
     * Scales the image to exact target dimensions using bilinear interpolation. This method does not preserve aspect
     * ratio and will stretch/compress the image as needed to match the target size exactly.
     *
     * @param source the source Mat to resize
     * @param dim    the target dimensions (width x height)
     * @return a new {@link ImageCV} scaled to the specified dimensions
     * @throws IllegalArgumentException if source is null, dim is null, or dimensions are not positive
     */
    public static ImageCV scale(Mat source, Dimension dim) {
        return scale(source, dim, Imgproc.INTER_LINEAR);
    }

    /**
     * Resizes the image to specified dimensions using custom interpolation method.
     * <p>
     * Provides full control over the scaling process by allowing selection of interpolation algorithm. Different
     * methods offer trade-offs between quality, speed, and specific visual characteristics.
     *
     * @param source        the source Mat to resize
     * @param dim           the target dimensions (width x height)
     * @param interpolation the interpolation method from {@link Imgproc} constants:
     *                      <ul>
     *                      <li>{@code INTER_NEAREST} - Nearest neighbor (fastest, blocky)
     *                      <li>{@code INTER_LINEAR} - Bilinear (good quality/speed balance)
     *                      <li>{@code INTER_CUBIC} - Bicubic (highest quality, slower)
     *                      <li>{@code INTER_LANCZOS4} - Lanczos (best for downscaling)
     *                      </ul>
     * @return a new {@link ImageCV} scaled to the specified dimensions
     * @throws IllegalArgumentException if source is null, dim is null, dimensions are not positive, or interpolation
     *                                  method is invalid
     */
    public static ImageCV scale(Mat source, Dimension dim, Integer interpolation) {
        ImageIOHandler.validateSource(source);
        Objects.requireNonNull(dim, "Target dimensions cannot be null");

        if (dim.width <= 0 || dim.height <= 0) {
            throw new IllegalArgumentException("Target dimensions must be positive: " + dim);
        }

        int interp = Objects.requireNonNullElse(interpolation, Imgproc.INTER_LINEAR);
        var resized = new Mat();
        Imgproc.resize(source, resized, new Size(dim.width, dim.height), 0, 0, interp);

        return ImageCV.fromMat(resized);
    }

    /**
     * Applies a lookup table transformation to pixel values.
     * <p>
     * Transforms pixel intensities using predefined mapping tables, commonly used for:
     * <ul>
     * <li>Window/level adjustments in medical imaging
     * <li>Contrast enhancement and histogram equalization
     * <li>Color space transformations and corrections
     * <li>Gamma correction and tone mapping
     * </ul>
     *
     * @param source the source Mat (must be 8-bit for optimal performance)
     * @param lut    the lookup table as a 2D byte array where: - First dimension represents color channels (1 or 3) -
     *               Second dimension contains 256 mapping values (0-255 input → output) - For single-channel LUT:
     *               applied to all channels - For multi-channel LUT: must match source channel count
     * @return a new {@link ImageCV} with transformed pixel values
     * @throws IllegalArgumentException      if source is null, LUT is null or malformed
     * @throws UnsupportedOperationException if source image format is not supported
     */
    public static ImageCV applyLUT(Mat source, byte[][] lut) {
        ImageIOHandler.validateSource(source);
        Objects.requireNonNull(lut, "LUT cannot be null");

        if (lut.length == 0 || lut[0].length != 256) {
            throw new IllegalArgumentException("LUT must have 256 entries per channel");
        }

        Mat lutMat = createLutMat(source, lut);
        var result = new ImageCV();
        Core.LUT(source, lutMat, result);

        return result;
    }

    /**
     * Rescales image pixel values to 8-bit range using linear transformation.
     * <p>
     * Applies the linear transformation: output = (input * alpha) + beta This is essential for converting images with
     * arbitrary dynamic ranges to standard 8-bit display format while preserving relative intensities.
     *
     * @param source the source Mat with any bit depth
     * @param alpha  the scaling factor (multiplier) - controls contrast
     * @param beta   the offset value (additive) - controls brightness
     * @return a new {@link ImageCV} with 8-bit pixel depth (0-255 range)
     * @throws IllegalArgumentException if source is null
     */
    public static ImageCV rescaleToByte(Mat source, double alpha, double beta) {
        ImageIOHandler.validateSource(source);

        var result = new ImageCV();
        source.convertTo(result, CvType.CV_8U, alpha, beta);

        return result;
    }

    /**
     * Inverts all pixel values using bitwise NOT operation.
     * <p>
     * Creates a negative image by inverting every bit in the pixel data. For 8-bit images: output = 255 - input. This
     * operation is commonly used in:
     * <ul>
     * <li>Medical imaging for different contrast preferences
     * <li>Creating negative views of X-rays and other modalities
     * <li>Preprocessing for certain analysis algorithms
     * </ul>
     *
     * @param source the source ImageCV to invert
     * @return a new {@link ImageCV} with inverted pixel values
     * @throws IllegalArgumentException if source is null
     */
    public static ImageCV invertLUT(ImageCV source) {
        ImageIOHandler.validateSource(source);

        var result = new ImageCV();
        Core.bitwise_not(source, result);

        return result;
    }

    /**
     * Performs bitwise AND operation with a constant value mask.
     * <p>
     * Applies bitwise AND between each pixel and the specified constant, effectively masking out specific bits. Useful
     * for:
     * <ul>
     * <li>Bit-plane extraction and analysis
     * <li>Noise reduction by removing least significant bits
     * <li>Creating binary masks from intensity images
     * </ul>
     *
     * @param source  the source Mat
     * @param src2Cst the constant value to AND with each pixel
     * @return a new {@link ImageCV} with masked pixel values
     * @throws IllegalArgumentException if source is null
     */
    public static ImageCV bitwiseAnd(Mat source, int src2Cst) {
        ImageIOHandler.validateSource(source);

        try (var mask = new ImageCV(source.size(), source.type(), new Scalar(src2Cst))) {
            var result = new ImageCV();

            Core.bitwise_and(source, mask, result);

            return result;
        }
    }

    /**
     * Rotates image in 90-degree increments using efficient matrix operations.
     * <p>
     * Performs fast rotation for common orientations without interpolation artifacts. Supports the three standard
     * rotation modes defined by OpenCV core constants.
     *
     * @param source       the source Mat to rotate
     * @param rotateCvType the rotation type:
     *                     <ul>
     *                     <li>{@code Core.ROTATE_90_CLOCKWISE} - 90° clockwise
     *                     <li>{@code Core.ROTATE_90_COUNTERCLOCKWISE} - 90° counter-clockwise
     *                     <li>{@code Core.ROTATE_180} - 180° rotation
     *                     </ul>
     * @return a new {@link ImageCV} with rotated orientation. For 90° rotations, width and height are swapped
     * @throws IllegalArgumentException if source is null or rotation type is invalid
     */
    public static ImageCV getRotatedImage(Mat source, int rotateCvType) {
        ImageIOHandler.validateSource(source);
        if (rotateCvType < 0 || rotateCvType > 2) {
            return ImageCV.fromMat(source.clone());
        }
        var result = new ImageCV();
        Core.rotate(source, result, rotateCvType);

        return result;
    }

    /**
     * Flips the image along specified axes.
     * <p>
     * Provides mirror transformations along horizontal, vertical, or both axes. This operation is lossless and commonly
     * used for image orientation correction.
     *
     * @param source     the source Mat to flip
     * @param flipCvType the flip direction:
     *                   <ul>
     *                   <li>{@code 0} - Vertical flip (around x-axis)
     *                   <li>{@code 1} - Horizontal flip (around y-axis)
     *                   <li>{@code -1} - Both vertical and horizontal flip
     *                   </ul>
     * @return a new {@link ImageCV} with flipped orientation
     * @throws IllegalArgumentException if source is null
     */
    public static ImageCV flip(Mat source, int flipCvType) {
        ImageIOHandler.validateSource(source);

        var result = new ImageCV();
        Core.flip(source, result, flipCvType);

        return result;
    }

    /**
     * Applies an affine transformation using a 2x3 transformation matrix.
     * <p>
     * Performs general geometric transformations including combinations of: translation, rotation, scaling, and
     * shearing. The transformation matrix format follows standard computer vision conventions.
     *
     * @param source        the source Mat to transform
     * @param matrix        the 2x3 affine transformation matrix:
     *
     *                      <pre>
     *               [a b tx]
     *               [c d ty]
     *                      </pre>
     *
     *                      where (a,b,c,d) define rotation/scaling/shearing and (tx,ty) define translation
     * @param boxSize       the size of the output image canvas
     * @param interpolation the interpolation method (see {@link #scale(Mat, Dimension, Integer)})
     * @return a new {@link ImageCV} with the applied geometric transformation
     * @throws IllegalArgumentException if any parameter is null or matrix has wrong dimensions
     */
    public static ImageCV warpAffine(Mat source, Mat matrix, Size boxSize, Integer interpolation) {
        ImageIOHandler.validateSource(source);
        Objects.requireNonNull(matrix, "Transformation matrix cannot be null");
        Objects.requireNonNull(boxSize, "Output size cannot be null");

        if (matrix.rows() != 2 || matrix.cols() != 3) {
            throw new IllegalArgumentException("Affine matrix must be 2x3");
        }

        int interp = Objects.requireNonNullElse(interpolation, Imgproc.INTER_LINEAR);
        var result = new ImageCV();
        Imgproc.warpAffine(source, result, matrix, boxSize, interp);

        return result;
    }

    /**
     * Blends two images with specified opacity values for each source.
     * <p>
     * Creates a weighted combination of two images using the formula: output = (source1 * opacity1) + (source2 *
     * opacity2)
     * <p>
     * This is useful for creating overlay effects, comparing images, or implementing fade transitions.
     *
     * @param source1  the first source Mat
     * @param source2  the second source Mat (must have same dimensions as source1)
     * @param opacity1 the weight for source1 (0.0 = transparent, 1.0 = opaque)
     * @param opacity2 the weight for source2 (0.0 = transparent, 1.0 = opaque)
     * @return a new {@link ImageCV} containing the blended result
     * @throws IllegalArgumentException if sources are null, have different dimensions, or opacity values are invalid
     */
    public static ImageCV mergeImages(Mat source1, Mat source2, double opacity1, double opacity2) {
        Objects.requireNonNull(source1, "First source image cannot be null");
        Objects.requireNonNull(source2, "Second source image cannot be null");

        if (!source1.size().equals(source2.size())) {
            throw new IllegalArgumentException("Source images must have the same dimensions");
        }

        validateOpacity(opacity1);
        validateOpacity(opacity2);

        var result = new ImageCV();
        Core.addWeighted(source1, opacity1, source2, opacity2, 0.0, result);

        return result;
    }

    /**
     * Creates an overlay effect using a binary mask and solid color.
     * <p>
     * Applies a colored overlay to regions specified by the mask image. The mask determines which pixels receive the
     * overlay color, creating highlighting or annotation effects commonly used in medical imaging.
     *
     * @param source     the base image Mat
     * @param imgOverlay the binary mask Mat where 255 indicates overlay regions
     * @param color      the overlay color to apply
     * @return a new {@link ImageCV} with colored overlay applied
     * @throws IllegalArgumentException if any parameter is null
     */
    public static ImageCV overlay(Mat source, Mat imgOverlay, Color color) {
        ImageIOHandler.validateSource(source);
        Objects.requireNonNull(imgOverlay, "Overlay mask cannot be null");
        Objects.requireNonNull(color, "Overlay color cannot be null");

        var maxVal = getMaxColorForOverlay(source, color);
        if (maxVal != null) {
            return applyGrayscaleOverlay(source, imgOverlay, maxVal);
        }

        return applyColorOverlay(source, imgOverlay, color);
    }

    /**
     * Creates an overlay effect from a RenderedImage mask.
     * <p>
     * Convenience method that converts a Java RenderedImage to OpenCV format before applying the overlay operation.
     *
     * @param source     the base image Mat
     * @param imgOverlay the binary mask as RenderedImage where white pixels indicate overlay regions
     * @param color      the overlay color to apply
     * @return a new {@link ImageCV} with colored overlay applied
     * @throws IllegalArgumentException if any parameter is null
     */
    public static ImageCV overlay(Mat source, RenderedImage imgOverlay, Color color) {
        Objects.requireNonNull(imgOverlay, "Overlay image cannot be null");

        var overlayMat = ImageConversion.toMat(imgOverlay);
        return overlay(source, overlayMat, color);
    }

    /**
     * Draws a geometric shape on the image with specified color.
     * <p>
     * Renders vector graphics directly onto the image using Java2D operations. This method is useful for adding
     * annotations, measurements, or region markers to medical images.
     *
     * @param source the base RenderedImage
     * @param shape  the geometric Shape to draw (can be any Java2D shape)
     * @param color  the drawing color
     * @return a new {@link BufferedImage} with the shape drawn on it
     * @throws IllegalArgumentException if any parameter is null
     */
    public static BufferedImage drawShape(RenderedImage source, Shape shape, Color color) {
        Objects.requireNonNull(source, NULL_SOURCE_IMAGE_ERROR);
        Objects.requireNonNull(shape, "Shape cannot be null");
        Objects.requireNonNull(color, "Color cannot be null");

        var srcImg = ImageConversion.toMat(source);
        var contours = ImageAnalyzer.transformShapeToContour(shape, true);
        Imgproc.fillPoly(srcImg, contours, getMaxColor(srcImg, color));
        return ImageConversion.toBufferedImage((PlanarImage) srcImg);
    }

    /**
     * Applies a crop mask with transparency outside the specified rectangle.
     * <p>
     * Creates a "spotlight" effect by reducing opacity outside the target rectangle, drawing attention to a specific
     * region while keeping the context visible.
     *
     * @param source the source Mat
     * @param bounds the rectangle defining the focus area
     * @param alpha  the opacity level for areas outside the rectangle (0.0 = transparent, 1.0 = opaque)
     * @return a new {@link ImageCV} with dimmed areas outside the rectangle
     * @throws IllegalArgumentException if source is null, bounds is null, or alpha is invalid
     */
    public static ImageCV applyCropMask(Mat source, Rectangle bounds, double alpha) {
        ImageIOHandler.validateSource(source);
        Objects.requireNonNull(bounds, "Bounds rectangle cannot be null");
        validateOpacity(alpha);

        var result = new ImageCV();
        source.copyTo(result);

        drawMaskRegions(result, bounds);
        Core.addWeighted(result, alpha, source, 1 - alpha, 0.0, result);

        return result;
    }

    /**
     * Applies a shutter effect using a geometric shape mask.
     * <p>
     * Implements the DICOM display shutter concept by setting pixels outside the defined shape to a specified color,
     * typically black for medical imaging applications.
     *
     * @param source the source Mat
     * @param shape  the geometric Shape defining the visible area
     * @param color  the shutter color (typically black for medical displays)
     * @return a new {@link ImageCV} with shutter effect applied
     * @throws IllegalArgumentException if any parameter is null
     */
    public static ImageCV applyShutter(Mat source, Shape shape, Color color) {
        ImageIOHandler.validateSource(source);
        Objects.requireNonNull(shape, "Shutter shape cannot be null");
        Objects.requireNonNull(color, "Shutter color cannot be null");

        // Convert shape to contour mask
        List<MatOfPoint> contours = ImageAnalyzer.transformShapeToContour(shape, true);

        Mat mask = Mat.zeros(source.size(), CvType.CV_8UC1);
        Imgproc.fillPoly(mask, contours, new Scalar(1));

        // Apply shutter color outside the shape
        Scalar scalar = getMaxColor(source, color);
        ImageCV dstImg = new ImageCV(source.size(), source.type(), scalar);
        source.copyTo(dstImg, mask);
        return dstImg;
    }

    /**
     * Applies a shutter effect using a RenderedImage mask.
     * <p>
     * Convenience method for applying shutter effects when the mask is provided as a standard Java image rather than a
     * geometric shape.
     *
     * @param source     the source Mat
     * @param imgOverlay the mask RenderedImage where white pixels define the visible area
     * @param color      the shutter color
     * @return a new {@link ImageCV} with shutter effect applied
     * @throws IllegalArgumentException if any parameter is null
     */
    public static ImageCV applyShutter(Mat source, RenderedImage imgOverlay, Color color) {
        return overlay(source, imgOverlay, color);
    }

    // Private helper methods

    /**
     * Validates the opacity.
     *
     * @param opacity the opacity.
     */
    private static void validateOpacity(double opacity) {
        if (opacity < 0.0 || opacity > 1.0) {
            throw new IllegalArgumentException("Opacity must be between 0.0 and 1.0");
        }
    }

    /**
     * Creates the lut mat.
     *
     * @param source the source.
     * @param lut    the lut.
     * @return the operation result.
     */
    private static Mat createLutMat(Mat source, byte[][] lut) {
        int lutCh = Objects.requireNonNull(lut).length;
        Mat lutMat;

        if (lutCh > 1) {
            lutMat = new Mat();
            List<Mat> lutList = new ArrayList<>(lutCh);
            for (int i = 0; i < lutCh; i++) {
                Mat l = new Mat(1, 256, CvType.CV_8U);
                l.put(0, 0, lut[i]);
                lutList.add(l);
            }
            Core.merge(lutList, lutMat);
            if (source.channels() < lut.length) {
                Imgproc.cvtColor(source.clone(), source, Imgproc.COLOR_GRAY2BGR);
            }
        } else {
            lutMat = new Mat(1, 256, CvType.CV_8UC1);
            lutMat.put(0, 0, lut[0]);
        }
        return lutMat;
    }

    /**
     * Applies the grayscale overlay.
     *
     * @param source     the source.
     * @param imgOverlay the img overlay.
     * @param maxVal     the max val.
     * @return the operation result.
     */
    private static ImageCV applyGrayscaleOverlay(Mat source, Mat imgOverlay, Integer maxVal) {
        var colorMat = new Mat(source.size(), source.type(), new Scalar(maxVal));
        var result = new ImageCV();
        source.copyTo(result);
        colorMat.copyTo(result, imgOverlay);
        return result;
    }

    /**
     * Applies the color overlay.
     *
     * @param source     the source.
     * @param imgOverlay the img overlay.
     * @param color      the color.
     * @return the operation result.
     */
    private static ImageCV applyColorOverlay(Mat source, Mat imgOverlay, Color color) {
        var result = new ImageCV();

        if (source.channels() < 3) {
            Imgproc.cvtColor(source, result, Imgproc.COLOR_GRAY2BGR);
        } else {
            source.copyTo(result);
        }

        var colorImg = new Mat(result.size(), CvType.CV_8UC3,
                new Scalar(color.getBlue(), color.getGreen(), color.getRed()));
        double alpha = color.getAlpha() / 255.0;

        if (alpha < 1.0) {
            var overlay = new ImageCV();
            result.copyTo(overlay);
            colorImg.copyTo(overlay, imgOverlay);
            Core.addWeighted(overlay, alpha, result, 1 - alpha, 0, result);
        } else {
            colorImg.copyTo(result, imgOverlay);
        }

        return result;
    }

    /**
     * Executes the draw mask regions operation.
     *
     * @param dstImg the dst img.
     * @param bounds the bounds.
     */
    private static void drawMaskRegions(ImageCV dstImg, Rectangle bounds) {
        var expandedBounds = new Rectangle(bounds);
        expandedBounds.grow(1, 1);

        var blackScalar = new Scalar(0);

        // Top region
        if (expandedBounds.getY() > 0) {
            Imgproc.rectangle(
                    dstImg,
                    new Point(0.0, 0.0),
                    new Point(dstImg.width(), expandedBounds.getMinY()),
                    blackScalar,
                    -1);
        }

        // Left region
        if (expandedBounds.getX() > 0) {
            Imgproc.rectangle(
                    dstImg,
                    new Point(0.0, expandedBounds.getMinY()),
                    new Point(expandedBounds.getMinX(), expandedBounds.getMaxY()),
                    blackScalar,
                    -1);
        }

        // Right region
        if (expandedBounds.getX() < dstImg.width()) {
            Imgproc.rectangle(
                    dstImg,
                    new Point(expandedBounds.getMaxX(), expandedBounds.getMinY()),
                    new Point(dstImg.width(), expandedBounds.getMaxY()),
                    blackScalar,
                    -1);
        }

        // Bottom region
        if (expandedBounds.getY() < dstImg.height()) {
            Imgproc.rectangle(
                    dstImg,
                    new Point(0.0, expandedBounds.getMaxY()),
                    new Point(dstImg.width(), dstImg.height()),
                    blackScalar,
                    -1);
        }
    }

    /**
     * Gets the max color.
     *
     * @param source the source.
     * @param color  the color.
     * @return the max color.
     */
    private static Scalar getMaxColor(Mat source, Color color) {
        var depth = CvType.depth(source.type());
        var is16Bit = depth == CvType.CV_16U || depth == CvType.CV_16S;

        if (is16Bit) {
            var maxColorComponent = Math.max(color.getRed(), Math.max(color.getGreen(), color.getBlue()));
            var maxValue = depth == CvType.CV_16S ? Short.MAX_VALUE : 0xFFFF;
            var scaledValue = maxColorComponent * maxValue / 255;
            return new Scalar(scaledValue);
        }
        return new Scalar(color.getBlue(), color.getGreen(), color.getRed());
    }

    /**
     * Gets the max color for overlay.
     *
     * @param source the source.
     * @param color  the color.
     * @return the max color for overlay.
     */
    private static Integer getMaxColorForOverlay(Mat source, Color color) {
        var depth = CvType.depth(source.type());
        var is16Bit = depth == CvType.CV_16U || depth == CvType.CV_16S;
        var isSingleChannel = source.channels() == 1;

        // Return scaled color value for 16-bit single-channel or grayscale images
        if ((is16Bit || isGray(color)) && isSingleChannel) {
            var maxColorComponent = Math.max(color.getRed(), Math.max(color.getGreen(), color.getBlue()));

            if (is16Bit) {
                var maxValue = depth == CvType.CV_16S ? Short.MAX_VALUE : 0xFFFF;
                return maxColorComponent * maxValue / 255;
            }
            return maxColorComponent;
        }
        return null;
    }

    /**
     * Determines whether gray.
     *
     * @param color the color.
     * @return true if the condition is met; otherwise false.
     */
    private static boolean isGray(Color color) {
        return color.getRed() == color.getGreen() && color.getGreen() == color.getBlue();
    }

}
