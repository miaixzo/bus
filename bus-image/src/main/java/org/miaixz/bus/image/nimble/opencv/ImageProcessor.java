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

import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.logger.Logger;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.List;
import java.util.*;
/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageProcessor {

    public static final String UNSUPPORTED_SIZE = "Unsupported size: ";

    /**
     * Computes Min/Max values from Image.
     *
     * @param img              the image
     * @param exclude8bitImage if true, exclude 8 bit image and return 0 and 255.
     * @return the min/max values and their locations. Max is not inferior to min + 1 to avoid
     * division by 0.
     * @throws OutOfMemoryError if the image is too large to be processed.
     */
    public static MinMaxLocResult findRawMinMaxValues(PlanarImage img, boolean exclude8bitImage)
            throws OutOfMemoryError {
        MinMaxLocResult val;
        if (CvType.depth(Objects.requireNonNull(img).type()) <= 1 && exclude8bitImage) {
            val = new MinMaxLocResult();
            val.minVal = 0.0;
            val.maxVal = 255.0;
        } else {
            val = ImageProcessor.findMinMaxValues(img.toMat());
            if (val == null) {
                val = new MinMaxLocResult();
            }
            // Handle special case when min and max are equal, ex. black image
            // + 1 to max enables to display the correct value
            if (val.minVal == val.maxVal) {
                val.maxVal += 1.0;
            }
        }
        return val;
    }

    /**
     * Performs a look-up table transform of an image.
     *
     * @param source the image. Only 8-bit images are supported.
     * @param lut    the lookup table of 256 elements; in case of multi-channels input array, the table
     *               should either have a single channel (in this case the same table is used for all channels)
     *               or the same number of channels as in the input array.
     * @return the result image with the same size and number of channels as src, and the same depth
     * as lut.
     */
    public static ImageCV applyLUT(Mat source, byte[][] lut) {
        Mat srcImg = Objects.requireNonNull(source);
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
            if (srcImg.channels() < lut.length) {
                Imgproc.cvtColor(srcImg.clone(), srcImg, Imgproc.COLOR_GRAY2BGR);
            }
        } else {
            lutMat = new Mat(1, 256, CvType.CV_8UC1);
            lutMat.put(0, 0, lut[0]);
        }

        ImageCV dstImg = new ImageCV();
        Core.LUT(srcImg, lutMat, dstImg);
        return dstImg;
    }

    /**
     * Rescale image to 8 bit image with alpha and beta parameters.
     *
     * @param source the image.
     * @param alpha  the scale factor.
     * @param beta   the shift factor.
     * @return the rescaled image with 8 bit depth.
     */
    public static ImageCV rescaleToByte(Mat source, double alpha, double beta) {
        ImageCV dstImg = new ImageCV();
        Objects.requireNonNull(source).convertTo(dstImg, CvType.CV_8U, alpha, beta);
        return dstImg;
    }

    /**
     * Inverts every bit of the image data.
     *
     * @param source the image.
     * @return the image with inverted values.
     */
    public static ImageCV invertLUT(ImageCV source) {
        ImageCV dstImg = new ImageCV();
        Core.bitwise_not(Objects.requireNonNull(source), dstImg);
        return dstImg;
    }

    /**
     * Performs a look-up table transform of an image.
     *
     * @param source  the image.
     * @param src2Cst the mask value to apply the bitwise operation.
     * @return the result image.
     */
    public static ImageCV bitwiseAnd(Mat source, int src2Cst) {
        Objects.requireNonNull(source);
        ImageCV mask = new ImageCV(source.size(), source.type(), new Scalar(src2Cst));
        Core.bitwise_and(source, mask, mask);
        return mask;
    }

    /**
     * Crops the image to the specified rectangle.
     *
     * @param source the image.
     * @param area   the rectangle to crop.
     * @return the cropped image.
     */
    public static ImageCV crop(Mat source, Rectangle area) {
        Objects.requireNonNull(source);
        Rectangle rect =
                Objects.requireNonNull(area)
                        .intersection(new Rectangle(0, 0, source.width(), source.height()));
        if (area.width > 1 && area.height > 1) {
            return ImageCV.toImageCV(source.submat(new Rect(rect.x, rect.y, rect.width, rect.height)));
        }
        return ImageCV.toImageCV(source.clone());
    }

    /**
     * Returns Min/Max values and their locations.
     *
     * @param source the image.
     * @param area   the rectangle to search for min/max values.
     * @return the min/max values and their locations. Max is not inferior to min + 1 to avoid
     * division by 0.
     */
    public static MinMaxLocResult minMaxLoc(RenderedImage source, Rectangle area) {
        Mat srcImg = ImageConversion.toMat(Objects.requireNonNull(source), area);
        return Core.minMaxLoc(srcImg);
    }

    /**
     * Converts a shape to a list of contours compliant with OpenCV.
     *
     * @param shape                the shape to transform.
     * @param keepImageCoordinates if true, the coordinates are not translated to the shape bounds.
     * @return the list of contours.
     */
    public static List<MatOfPoint> transformShapeToContour(
            Shape shape, boolean keepImageCoordinates) {
        Rectangle b = shape.getBounds();
        if (keepImageCoordinates) {
            b.x = 0;
            b.y = 0;
        }
        List<MatOfPoint> points = new ArrayList<>();
        List<Point> cvPts = new ArrayList<>();

        PathIterator iterator = new FlatteningPathIterator(shape.getPathIterator(null), 2);
        double[] pts = new double[6];
        MatOfPoint p = null;
        while (!iterator.isDone()) {
            int segType = iterator.currentSegment(pts);
            switch (segType) {
                case PathIterator.SEG_MOVETO -> {
                    addSegment(p, cvPts, points);
                    p = new MatOfPoint();
                    cvPts.add(new Point(pts[0] - b.x, pts[1] - b.y));
                }
                case PathIterator.SEG_LINETO, PathIterator.SEG_CLOSE ->
                        cvPts.add(new Point(pts[0] - b.x, pts[1] - b.y));
                default -> {
                    // should never append with FlatteningPathIterator
                }
            }
            iterator.next();
        }

        addSegment(p, cvPts, points);
        return points;
    }

    private static void addSegment(MatOfPoint p, List<Point> cvPts, List<MatOfPoint> points) {
        if (p != null) {
            if (cvPts.size() > 1) {
                int last = cvPts.size() - 1;
                if (cvPts.get(last - 1).equals(cvPts.get(last))) {
                    cvPts.remove(last);
                }
            }
            p.fromList(cvPts);
            cvPts.clear();
            points.add(p);
        }
    }

    /**
     * See {@link #meanStdDev(Mat, Shape, Integer, Integer)}.
     */
    public static double[][] meanStdDev(Mat source) {
        return meanStdDev(source, (Shape) null, null, null);
    }

    /**
     * See {@link #meanStdDev(Mat, Shape, Integer, Integer)}.
     */
    public static double[][] meanStdDev(Mat source, Shape shape) {
        return meanStdDev(source, shape, null, null);
    }

    /**
     * @param source       the image.
     * @param shape        the shape to apply on the image. If null, the whole image is processed.
     * @param paddingValue the starting value to exclude. PaddingValue is applied only with one
     *                     channel image.
     * @param paddingLimit the last value to exclude. If null only paddingValue is excluded.
     * @return a 5 double arrays: min, max, mean, standard deviation, pixel count.
     */
    public static double[][] meanStdDev(
            Mat source, Shape shape, Integer paddingValue, Integer paddingLimit) {
        List<Mat> list = getMaskImage(source, shape, paddingValue, paddingLimit);
        if (list.size() < 2) {
            return null;
        }
        return buildMeanStdDev(list.get(0), list.get(1));
    }

    /**
     * @param source       the image.
     * @param mask         the shape to apply on the image. If null, the whole image is processed.
     * @param paddingValue the starting value to exclude. PaddingValue is applied only with one
     *                     channel image.
     * @param paddingLimit the last value to exclude. If null only paddingValue is excluded.
     * @return a 5 double arrays: min, max, mean, standard deviation, pixel count.
     */
    public static double[][] meanStdDev(
            Mat source, Mat mask, Integer paddingValue, Integer paddingLimit) {
        if (source == null) {
            return null;
        }
        Mat paddingMask = getPixelPaddingMask(source, mask, paddingValue, paddingLimit);
        return buildMeanStdDev(source, paddingMask);
    }

    private static double[][] buildMeanStdDev(Mat source, Mat mask) {
        if (source == null) {
            return null;
        }
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        if (mask == null) {
            Core.meanStdDev(source, mean, stddev);
        } else {
            Core.meanStdDev(source, mean, stddev, mask);
        }

        List<Mat> channels = new ArrayList<>();
        if (source.channels() > 1) {
            Core.split(source, channels);
        } else {
            channels.add(source);
        }

        double[][] val = new double[5][channels.size()];
        for (int i = 0; i < channels.size(); i++) {
            MinMaxLocResult minMax;
            if (mask == null) {
                minMax = Core.minMaxLoc(channels.get(i));
            } else {
                minMax = Core.minMaxLoc(channels.get(i), mask);
            }
            val[0][i] = minMax.minVal;
            val[1][i] = minMax.maxVal;
        }

        val[2] = mean.toArray();
        val[3] = stddev.toArray();
        if (mask == null) {
            val[4][0] = source.width() * (double) source.height();
        } else {
            val[4][0] = Core.countNonZero(mask);
        }
        return val;
    }

    /**
     * Create the mask image from the shape and exclude the pixel padding values.
     *
     * @param source       the image.
     * @param shape        the shape to apply on the image. If null, the whole image is processed.
     * @param paddingValue the starting value to exclude. PaddingValue is applied only with one
     *                     channel image.
     * @param paddingLimit the last value to exclude. If null only paddingValue is excluded.
     * @return the source and mask images.
     */
    public static List<Mat> getMaskImage(
            Mat source, Shape shape, Integer paddingValue, Integer paddingLimit) {
        Objects.requireNonNull(source);
        Mat srcImg;
        Mat mask = null;
        if (shape == null) {
            srcImg = source;
        } else {
            Rectangle b =
                    new Rectangle(0, 0, source.width(), source.height()).intersection(shape.getBounds());
            if (b.getWidth() < 1 || b.getHeight() < 1) {
                return Collections.emptyList();
            }

            srcImg = source.submat(new Rect(b.x, b.y, b.width, b.height));
            mask = Mat.zeros(srcImg.size(), CvType.CV_8UC1);
            List<MatOfPoint> pts = transformShapeToContour(shape, false);
            Imgproc.fillPoly(mask, pts, new Scalar(255));
        }

        mask = getPixelPaddingMask(srcImg, mask, paddingValue, paddingLimit);
        return Arrays.asList(srcImg, mask);
    }

    private static Mat getPixelPaddingMask(
            Mat source, Mat mask, Integer paddingValue, Integer paddingLimit) {
        if (paddingValue != null && source.channels() == 1) {
            if (paddingLimit == null) {
                paddingLimit = paddingValue;
            } else if (paddingLimit < paddingValue) {
                int temp = paddingValue;
                paddingValue = paddingLimit;
                paddingLimit = temp;
            }
            Mat maskPix = new Mat(source.size(), CvType.CV_8UC1, new Scalar(0));
            excludePaddingValue(source, maskPix, paddingValue, paddingLimit);
            Mat paddingMask;
            if (mask == null) {
                paddingMask = maskPix;
            } else {
                paddingMask = new ImageCV();
                Core.bitwise_and(mask, maskPix, paddingMask);
            }
            return paddingMask;
        }
        return mask;
    }

    /**
     * Returns the minimum and maximum pixel values and their locations according to the mask
     * selection.
     *
     * @param srcImg the image.
     * @param mask   the mask selection. If null, the whole image is processed.
     * @return the min/max values and their locations. Max is not inferior to min + 1 to avoid
     * division by 0.
     */
    public static MinMaxLocResult minMaxLoc(Mat srcImg, Mat mask) {
        List<Mat> channels = new ArrayList<>(Objects.requireNonNull(srcImg).channels());
        if (srcImg.channels() > 1) {
            Core.split(srcImg, channels);
        } else {
            channels.add(srcImg);
        }

        MinMaxLocResult result = new MinMaxLocResult();
        result.minVal = Double.MAX_VALUE;
        result.maxVal = -Double.MAX_VALUE;

        for (Mat channel : channels) {
            MinMaxLocResult minMax = Core.minMaxLoc(channel, mask);
            result.minVal = Math.min(result.minVal, minMax.minVal);
            if (result.minVal == minMax.minVal) {
                result.minLoc = minMax.minLoc;
            }
            result.maxVal = Math.max(result.maxVal, minMax.maxVal);
            if (result.maxVal == minMax.maxVal) {
                result.maxLoc = minMax.maxLoc;
            }
        }
        return result;
    }

    private static void excludePaddingValue(Mat src, Mat mask, int paddingValue, int paddingLimit) {
        Mat dst = new Mat();
        Core.inRange(src, new Scalar(paddingValue), new Scalar(paddingLimit), dst);
        Core.bitwise_not(dst, dst);
        Core.add(dst, mask, mask);
    }

    /**
     * Resize the image to the specified dimension.
     *
     * @param source the image.
     * @param dim    the expected dimension.
     * @return the resized image.
     */
    public static ImageCV scale(Mat source, Dimension dim) {
        if (Objects.requireNonNull(dim).width < 1 || dim.height < 1) {
            throw new IllegalArgumentException(UNSUPPORTED_SIZE + dim);
        }
        ImageCV dstImg = new ImageCV();
        Imgproc.resize(
                Objects.requireNonNull(source), dstImg, new Size(dim.getWidth(), dim.getHeight()));
        return dstImg;
    }

    /**
     * Resize the image to the specified dimension with a specific interpolation.
     *
     * @param source the image.
     * @param dim    the expected dimension. param interpolation the interpolation method. See
     *               cv.InterpolationFlags
     * @return the resized image.
     */
    public static ImageCV scale(Mat source, Dimension dim, Integer interpolation) {
        if (interpolation == null
                || interpolation < Imgproc.INTER_NEAREST
                || interpolation > Imgproc.INTER_LANCZOS4) {
            return scale(source, dim);
        }
        if (Objects.requireNonNull(dim).width < 1 || dim.height < 1) {
            throw new IllegalArgumentException(UNSUPPORTED_SIZE + dim);
        }
        ImageCV dstImg = new ImageCV();
        Imgproc.resize(
                Objects.requireNonNull(source),
                dstImg,
                new Size(dim.getWidth(), dim.getHeight()),
                0,
                0,
                interpolation);
        return dstImg;
    }

    /**
     * Merge two images with specific opacity values.
     *
     * @param source1  the first image.
     * @param source2  the second image.
     * @param opacity1 the opacity value between 0 and 1 applied to the first image.
     * @param opacity2 the opacity value between 0 and 1 applied to the second image.
     * @return the merged image.
     */
    public static ImageCV mergeImages(Mat source1, Mat source2, double opacity1, double opacity2) {
        Mat srcImg = Objects.requireNonNull(source1);
        Mat src2Img = Objects.requireNonNull(source2);
        ImageCV dstImg = new ImageCV();
        Core.addWeighted(srcImg, opacity1, src2Img, opacity2, 0.0, dstImg);
        return dstImg;
    }

    private static boolean isGray(Color color) {
        int r = color.getRed();
        return r == color.getGreen() && r == color.getBlue();
    }

    /**
     * Overlays an image on top of another image.
     *
     * @param source     the image.
     * @param imgOverlay the mask image to overlay. Byte image where 255 is the overlay selection.
     * @param color      the overlay color to use for the output image.
     * @return the image with overlays.
     */
    public static ImageCV overlay(Mat source, Mat imgOverlay, Color color) {
        ImageCV srcImg = ImageCV.toImageCV(Objects.requireNonNull(source));
        Objects.requireNonNull(imgOverlay);
        boolean type16bit =
                CvType.depth(srcImg.type()) == CvType.CV_16U
                        || CvType.depth(srcImg.type()) == CvType.CV_16S;

        if ((type16bit || isGray(color)) && srcImg.channels() == 1) {
            int maxColor = Math.max(color.getRed(), Math.max(color.getGreen(), color.getBlue()));
            if (type16bit) {
                int max = CvType.depth(srcImg.type()) == CvType.CV_16S ? Short.MAX_VALUE : 65535;
                maxColor = maxColor * max / 255;
            }
            Mat grayImg = new Mat(srcImg.size(), srcImg.type(), new Scalar(maxColor));
            ImageCV dstImg = new ImageCV();
            srcImg.copyTo(dstImg);
            grayImg.copyTo(dstImg, imgOverlay);
            return dstImg;
        }

        ImageCV dstImg = new ImageCV();
        if (srcImg.channels() < 3) {
            Imgproc.cvtColor(srcImg, dstImg, Imgproc.COLOR_GRAY2BGR);
        } else {
            srcImg.copyTo(dstImg);
        }

        Mat colorImg =
                new Mat(
                        dstImg.size(),
                        CvType.CV_8UC3,
                        new Scalar(color.getBlue(), color.getGreen(), color.getRed()));
        double alpha = color.getAlpha() / 255.0;
        if (alpha < 1.0) {
            ImageCV overlay = new ImageCV();
            dstImg.copyTo(overlay);
            Core.copyTo(colorImg, overlay, imgOverlay);
            Core.addWeighted(overlay, alpha, dstImg, 1 - alpha, 0, dstImg);
        } else {
            colorImg.copyTo(dstImg, imgOverlay);
        }
        return dstImg;
    }

    /**
     * See {@link #overlay(Mat, Mat, Color)}.
     */
    public static ImageCV overlay(Mat source, RenderedImage imgOverlay, Color color) {
        return overlay(source, ImageConversion.toMat(Objects.requireNonNull(imgOverlay)), color);
    }

    /**
     * Draw a shape on top of an image with a specific color.
     *
     * @param source the image.
     * @param shape  the shape to draw.
     * @param color  the color to use for the output image.
     * @return the image with drawings.
     */
    public static BufferedImage drawShape(RenderedImage source, Shape shape, Color color) {
        Mat srcImg = ImageConversion.toMat(Objects.requireNonNull(source));
        List<MatOfPoint> pts = transformShapeToContour(shape, true);
        Imgproc.fillPoly(srcImg, pts, getMaxColor(srcImg, color));
        return ImageConversion.toBufferedImage(srcImg);
    }

    /**
     * Returns an image with the area outside the rectangle set to a certain opacity.
     *
     * @param source the image.
     * @param b      the rectangle to draw.
     * @param alpha  the opacity value between 0 and 1 applied to the area outside the rectangle.
     * @return the resulting image.
     */
    public static ImageCV applyCropMask(Mat source, Rectangle b, double alpha) {
        Mat srcImg = Objects.requireNonNull(source);
        ImageCV dstImg = new ImageCV();
        source.copyTo(dstImg);
        b.grow(1, 1);
        if (b.getY() > 0) {
            Imgproc.rectangle(
                    dstImg, new Point(0.0, 0.0), new Point(dstImg.width(), b.getMinY()), new Scalar(0), -1);
        }
        if (b.getX() > 0) {
            Imgproc.rectangle(
                    dstImg,
                    new Point(0.0, b.getMinY()),
                    new Point(b.getMinX(), b.getMaxY()),
                    new Scalar(0),
                    -1);
        }
        if (b.getX() < dstImg.width()) {
            Imgproc.rectangle(
                    dstImg,
                    new Point(b.getMaxX(), b.getMinY()),
                    new Point(dstImg.width(), b.getMaxY()),
                    new Scalar(0),
                    -1);
        }
        if (b.getY() < dstImg.height()) {
            Imgproc.rectangle(
                    dstImg,
                    new Point(0.0, b.getMaxY()),
                    new Point(dstImg.width(), dstImg.height()),
                    new Scalar(0),
                    -1);
        }
        Core.addWeighted(dstImg, alpha, srcImg, 1 - alpha, 0.0, dstImg);
        return dstImg;
    }

    private static Scalar getMaxColor(Mat source, Color color) {
        int depth = CvType.depth(source.type());
        boolean type16bit = depth == CvType.CV_16U || depth == CvType.CV_16S;
        Scalar scalar;
        if (type16bit) {
            int maxColor = Math.max(color.getRed(), Math.max(color.getGreen(), color.getBlue()));
            int max = CvType.depth(source.type()) == CvType.CV_16S ? Short.MAX_VALUE : 65535;
            max = maxColor * max / 255;
            scalar = new Scalar(max);
        } else {
            scalar = new Scalar(color.getBlue(), color.getGreen(), color.getRed());
        }
        return scalar;
    }

    /**
     * Returns an image with the area outside the rectangle set to a certain color.
     *
     * @param source the image.
     * @param shape  the shape of the shutter
     * @param color  the shutter color.
     * @return the resulting image.
     */
    public static ImageCV applyShutter(Mat source, Shape shape, Color color) {
        Mat srcImg = Objects.requireNonNull(source);
        Mat mask = Mat.zeros(srcImg.size(), CvType.CV_8UC1);
        List<MatOfPoint> pts = transformShapeToContour(shape, true);
        Imgproc.fillPoly(mask, pts, new Scalar(1));

        ImageCV dstImg = new ImageCV(srcImg.size(), srcImg.type(), getMaxColor(srcImg, color));
        srcImg.copyTo(dstImg, mask);
        return dstImg;
    }

    /**
     * Apply a shutter effect on the image.
     *
     * @param source     the image.
     * @param imgOverlay the mask image. Byte image where 255 is the shutter.
     * @param color      the shutter color.
     * @return the image with overlays.
     */
    public static ImageCV applyShutter(Mat source, RenderedImage imgOverlay, Color color) {
        return overlay(source, imgOverlay, color);
    }

    /**
     * Rotates the image in multiples of 90 degrees in three different ways: Rotate by 90 degrees
     * clockwise (rotateCode = ROTATE_90_CLOCKWISE). Rotate by 180 degrees clockwise (rotateCode =
     * ROTATE_180). Rotate by 270 degrees clockwise (rotateCode = ROTATE_90_COUNTERCLOCKWISE).
     *
     * @param source       the image.
     * @param rotateCvType an enum to specify how to rotate the image; Core.ROTATE_90_CLOCKWISE,
     *                     Core.ROTATE_180 and Core.ROTATE_90_COUNTERCLOCKWISE.
     * @return the rotated image. The size is the same with ROTATE_180, and the rows and cols are
     * switched for ROTATE_90_CLOCKWISE and ROTATE_90_COUNTERCLOCKWISE.
     */
    public static ImageCV getRotatedImage(Mat source, int rotateCvType) {
        if (rotateCvType < 0 || rotateCvType > 2) {
            return ImageCV.toImageCV(source.clone());
        }
        Mat srcImg = Objects.requireNonNull(source);
        ImageCV dstImg = new ImageCV();
        Core.rotate(srcImg, dstImg, rotateCvType);
        return dstImg;
    }

    /**
     * Flips the image around vertical, horizontal, or both axes.
     *
     * @param source     the image.
     * @param flipCvType a flag to specify how to flip the array; 0 means flipping around the x-axis
     *                   (vertical flip), positive (e.g., 1) means flipping around y-axis (horizontal flip), and
     *                   negative (e.g., -1) means flipping around both axes.
     * @return the flipped image.
     */
    public static ImageCV flip(Mat source, int flipCvType) {
        Objects.requireNonNull(source);
        ImageCV dstImg = new ImageCV();
        Core.flip(source, dstImg, flipCvType);
        return dstImg;
    }

    /**
     * Applies an affine transformation to an image.
     *
     * @param source        the image.
     * @param matrix        the 2x3 transformation matrix.
     * @param boxSize       the size of the output image.
     * @param interpolation an interpolation method. See cv::InterpolationFlags
     * @return the transformed image.
     */
    public static ImageCV warpAffine(Mat source, Mat matrix, Size boxSize, Integer interpolation) {
        Mat srcImg = Objects.requireNonNull(source);
        ImageCV dstImg = new ImageCV();

        if (interpolation == null) {
            interpolation = Imgproc.INTER_LINEAR;
        }
        Imgproc.warpAffine(
                srcImg,
                dstImg,
                Objects.requireNonNull(matrix),
                Objects.requireNonNull(boxSize),
                interpolation);

        return dstImg;
    }

    /**
     * Returns Min/Max values and their locations.
     *
     * @param source the image.
     * @return the min/max values and their locations. Max is not inferior to min + 1 to avoid
     * division by 0.
     */
    public static MinMaxLocResult findMinMaxValues(Mat source) {
        if (source != null) {
            return minMaxLoc(source, null);
        }
        return null;
    }

    /**
     * Returns Min/Max values and their locations excluding range of values provided.
     *
     * @param source       the image.
     * @param paddingValue the starting value to exclude. PaddingValue is applied only with one
     *                     channel image.
     * @param paddingLimit the last value to exclude. If null only paddingValue is excluded.
     * @return the min/max values and their locations. Max is not inferior to min + 1 to avoid
     * division by 0.
     */
    public static MinMaxLocResult findMinMaxValues(
            Mat source, Integer paddingValue, Integer paddingLimit) {
        if (source != null) {
            Mat mask = null;
            if (paddingValue != null && source.channels() == 1) {
                mask = new Mat(source.size(), CvType.CV_8UC1, new Scalar(0));
                if (paddingLimit == null) {
                    paddingLimit = paddingValue;
                } else if (paddingLimit < paddingValue) {
                    int temp = paddingValue;
                    paddingValue = paddingLimit;
                    paddingLimit = temp;
                }
                excludePaddingValue(source, mask, paddingValue, paddingLimit);
            }
            return minMaxLoc(source, mask);
        }
        return null;
    }

    /**
     * Creates a thumbnail of the image with the specified dimension.
     *
     * @param source    the image.
     * @param iconDim   the expected dimension. If keepRatio is true, the min value is kept.
     * @param keepRatio if true, the ratio of the image is kept.
     * @return the thumbnail image. Do not return an image with a size larger than the original image.
     */
    public static ImageCV buildThumbnail(PlanarImage source, Dimension iconDim, boolean keepRatio) {
        Objects.requireNonNull(source);
        if (Objects.requireNonNull(iconDim).width < 1 || iconDim.height < 1) {
            throw new IllegalArgumentException(UNSUPPORTED_SIZE + iconDim);
        }

        final double scale =
                Math.min(iconDim.getHeight() / source.height(), iconDim.getWidth() / source.width());
        if (scale >= 1.0) {
            return ImageCV.toImageCV(source.toMat().clone());
        }

        Size dim =
                keepRatio
                        ? new Size((int) (scale * source.width()), (int) (scale * source.height()))
                        : new Size(iconDim.width, iconDim.height);

        Mat srcImg = Objects.requireNonNull(source).toMat();
        ImageCV dstImg = new ImageCV();
        Imgproc.resize(srcImg, dstImg, dim, 0, 0, Imgproc.INTER_AREA);
        return dstImg;
    }

    private static boolean writeExceptReadOnly(File file) {
        return !file.exists() || file.canWrite();
    }

    /**
     * Write image with OpenCV and returns false if the image cannot be written.
     *
     * @param source the image to write
     * @param file   the output image file
     * @return true if the image is written
     */
    public static boolean writeImage(Mat source, File file) {
        if (writeExceptReadOnly(file)) {
            try {
                return Imgcodecs.imwrite(file.getPath(), source);
            } catch (OutOfMemoryError | CvException e) {
                Logger.error("Writing Image", e);
                FileKit.remove(file);
            }
        }
        return false;
    }

    /**
     * Write a thumbnail image with OpenCV and returns false if the image cannot be written.
     *
     * @param source  the image to write
     * @param file    the output image file
     * @param maxSize the maximum size of the thumbnail. The ratio of the image is preserved.
     * @return true if the image is written
     */
    public static boolean writeThumbnail(Mat source, File file, int maxSize) {
        if (writeExceptReadOnly(file)) {
            try {
                final double scale =
                        Math.min(maxSize / (double) source.height(), (double) maxSize / source.width());
                if (scale < 1.0) {
                    Size dim = new Size((int) (scale * source.width()), (int) (scale * source.height()));
                    try (ImageCV thumbnail = new ImageCV()) {
                        Imgproc.resize(source, thumbnail, dim, 0, 0, Imgproc.INTER_AREA);
                        MatOfInt map = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 80);
                        return Imgcodecs.imwrite(file.getPath(), thumbnail, map);
                    }
                }
            } catch (OutOfMemoryError | CvException e) {
                Logger.error("Writing thumbnail", e);
                FileKit.remove(file);
            }
        }
        return false;
    }

    /**
     * Write a PNG image with OpenCV and returns false if the image cannot be written.
     *
     * @param source the image to write
     * @param file   the output image file
     * @return true if the image is written
     */
    public static boolean writePNG(Mat source, File file) {
        if (writeExceptReadOnly(file)) {
            // TODO handle binary
            Mat srcImg = Objects.requireNonNull(source);
            Mat dstImg = null;
            int type = srcImg.type();
            int elemSize = CvType.ELEM_SIZE(type);
            int channels = CvType.channels(type);
            int bpp = (elemSize * 8) / channels;
            if (bpp > 16 || !CvType.isInteger(type)) {
                dstImg = new Mat();
                srcImg.convertTo(dstImg, CvType.CV_16SC(channels));
                srcImg = dstImg;
            }
            try {
                return Imgcodecs.imwrite(file.getPath(), srcImg);
            } catch (OutOfMemoryError | CvException e) {
                Logger.error("", e);
                FileKit.remove(file);
            } finally {
                ImageConversion.releaseMat(dstImg);
            }
        }
        return false;
    }

    /**
     * Write image with OpenCV and returns false if the image cannot be written.
     *
     * @param source the image to write
     * @param file   the output image file
     * @return true if the image is written
     */
    public static boolean writeImage(RenderedImage source, File file) {
        if (writeExceptReadOnly(file)) {
            try (ImageCV dstImg = ImageConversion.toMat(source)) {
                return Imgcodecs.imwrite(file.getPath(), dstImg);
            } catch (OutOfMemoryError | CvException e) {
                Logger.error("", e);
                FileKit.remove(file);
            }
        }
        return false;
    }

    /**
     * Write image with OpenCV and returns false if the image cannot be written.
     *
     * @param source the image to write
     * @param file   the output image file
     * @param params the list of parameters to use for writing the image, see Imgcodecs.IMWRITE_*.
     *               Format-specific parameters encoded as pairs (pId_1, pValue_1, pId_2, pValue_2...) see
     *               cv::ImwriteFlags
     * @return true if the image is written
     */
    public static boolean writeImage(Mat source, File file, MatOfInt params) {
        if (writeExceptReadOnly(file)) {
            try {
                return Imgcodecs.imwrite(file.getPath(), source, params);
            } catch (OutOfMemoryError | CvException e) {
                Logger.error("Writing image", e);
                FileKit.remove(file);
            }
        }
        return false;
    }

    /**
     * Read image with OpenCV and returns null if the image cannot be read.
     *
     * @param file the image file
     * @param tags the list of basic tags to fill. Can be null.
     * @return the image or null if the image cannot be read
     */
    public static ImageCV readImage(File file, List<String> tags) {
        try {
            return readImageWithCvException(file, tags);
        } catch (OutOfMemoryError | CvException e) {
            Logger.error("Reading image", e);
            return null;
        }
    }

    /**
     * Read image with OpenCV and throw CvException if the image cannot be read.
     *
     * @param file the image file
     * @param tags the list of basic tags to fill. Can be null.
     * @return the image or null if the image cannot be read
     * @throws CvException if the image cannot be read
     */
    public static ImageCV readImageWithCvException(File file, List<String> tags) {
        if (!file.canRead()) {
            return null;
        }
        List<String> exifs = tags;
        if (exifs == null) {
            exifs = new ArrayList<>();
        }
        Mat img = Imgcodecs.imread(file.getPath(), exifs);
        if (img.width() < 1 || img.height() < 1) {
            throw new CvException("OpenCV cannot read " + file.getPath());
        }
        return ImageCV.toImageCV(img);
    }

}
