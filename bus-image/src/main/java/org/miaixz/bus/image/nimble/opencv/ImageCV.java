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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import org.miaixz.bus.image.nimble.KernelData;

/**
 * Enhanced Mat implementation with additional memory management features. Implements PlanarImage for consistent image
 * handling across the application.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ImageCV extends Mat implements PlanarImage {

    /**
     * The min stack size value.
     */
    private static final int MIN_STACK_SIZE = 2;

    /**
     * The default depth value.
     */
    private static final int DEFAULT_DEPTH = -1;

    /**
     * The released after processing value.
     */
    private boolean releasedAfterProcessing;

    /**
     * The released value.
     */
    private boolean released;

    /**
     * Creates a new instance.
     */
    public ImageCV() {
        super();
    }

    /**
     * Creates a new instance.
     *
     * @param rows the rows.
     * @param cols the cols.
     * @param type the type.
     */
    public ImageCV(int rows, int cols, int type) {
        super(rows, cols, type);
    }

    /**
     * Creates a new instance.
     *
     * @param size the size.
     * @param type the type.
     */
    public ImageCV(Size size, int type) {
        super(size, type);
    }

    /**
     * Creates a new instance.
     *
     * @param size the size.
     * @param type the type.
     * @param s    the s.
     */
    public ImageCV(Size size, int type, Scalar s) {
        super(size, type, s);
    }

    /**
     * Creates a new instance.
     *
     * @param rows the rows.
     * @param cols the cols.
     * @param type the type.
     * @param s    the s.
     */
    public ImageCV(int rows, int cols, int type, Scalar s) {
        super(rows, cols, type, s);
    }

    /**
     * Creates a new instance.
     *
     * @param m        the m.
     * @param rowRange the row range.
     * @param colRange the col range.
     */
    public ImageCV(Mat m, Range rowRange, Range colRange) {
        super(m, rowRange, colRange);
    }

    /**
     * Creates a new instance.
     *
     * @param m        the m.
     * @param rowRange the row range.
     */
    public ImageCV(Mat m, Range rowRange) {
        super(m, rowRange);
    }

    /**
     * Creates a new instance.
     *
     * @param m   the m.
     * @param roi the ROI.
     */
    public ImageCV(Mat m, Rect roi) {
        super(m, roi);
    }

    /**
     * Executes the physical bytes operation.
     *
     * @return the operation result.
     */
    @Override
    public long physicalBytes() {
        return total() * elemSize();
    }

    /**
     * Executes the release operation.
     */
    @Override
    public void release() {
        if (!released) {
            super.release();
            this.released = true;
        }
    }

    /**
     * Checks whether the released condition is true.
     *
     * @return true if the released condition is true; otherwise false.
     */
    @Override
    public boolean isReleased() {
        return released;
    }

    /**
     * Checks whether the released after processing condition is true.
     *
     * @return true if the released after processing condition is true; otherwise false.
     */
    @Override
    public boolean isReleasedAfterProcessing() {
        return releasedAfterProcessing;
    }

    /**
     * Sets the released after processing.
     *
     * @param releasedAfterProcessing the released after processing.
     */
    @Override
    public void setReleasedAfterProcessing(boolean releasedAfterProcessing) {
        this.releasedAfterProcessing = releasedAfterProcessing;
    }

    /**
     * Executes the close operation.
     */
    @Override
    public void close() {
        release();
    }

    /**
     * Executes the from mat operation.
     *
     * @param source the source.
     * @return the operation result.
     */
    public static ImageCV fromMat(Mat source) {
        Objects.requireNonNull(source, "Source Mat cannot be null");
        if (source instanceof ImageCV imageCV) {
            return imageCV;
        }
        var result = new ImageCV();
        source.assignTo(result);
        return result;
    }

    /**
     * Executes the to mat operation.
     *
     * @param source the source.
     * @return the operation result.
     */
    public static Mat toMat(PlanarImage source) {
        Objects.requireNonNull(source, "Source PlanarImage cannot be null");
        return source.toMat();
    }

    /**
     * Executes the run garbage collector and wait operation.
     *
     * @param ms the ms.
     */
    public static void runGarbageCollectorAndWait(long ms) {
        System.gc();
        System.gc();
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Executes the filter operation.
     *
     * @param source the source.
     * @param kernel the kernel.
     * @return the operation result.
     */
    public static ImageCV filter(Mat source, KernelData kernel) {
        Objects.requireNonNull(source, "Source cannot be null");
        Objects.requireNonNull(kernel, "Kernel cannot be null");
        Mat kernelMatrix = new Mat(kernel.getHeight(), kernel.getWidth(), CvType.CV_32F);
        kernelMatrix.put(0, 0, kernel.getData());
        ImageCV result = new ImageCV();
        Imgproc.filter2D(source, result, DEFAULT_DEPTH, kernelMatrix);
        return result;
    }

    /**
     * Executes the mean stack operation.
     *
     * @param sources the sources.
     * @return the operation result.
     */
    public static ImageCV meanStack(List<PlanarImage> sources) {
        if (isInvalidStackSize(sources)) {
            return null;
        }
        PlanarImage reference = sources.get(0);
        Mat accumulator = new Mat(reference.height(), reference.width(), CvType.CV_32F);
        reference.toMat().convertTo(accumulator, CvType.CV_32F);

        for (int i = 1; i < sources.size(); i++) {
            accumulateFloatStack(sources.get(i), reference, accumulator);
        }

        ImageCV result = new ImageCV();
        Core.divide(accumulator, Scalar.all(sources.size()), accumulator);
        accumulator.convertTo(result, reference.type());
        return result;
    }

    /**
     * Executes the min stack operation.
     *
     * @param sources the sources.
     * @return the operation result.
     */
    public static ImageCV minStack(List<PlanarImage> sources) {
        return processStackOperation(sources, (result, image) -> {
            Core.min(result, image, result);
            return result;
        });
    }

    /**
     * Executes the max stack operation.
     *
     * @param sources the sources.
     * @return the operation result.
     */
    public static ImageCV maxStack(List<PlanarImage> sources) {
        return processStackOperation(sources, (result, image) -> {
            Core.max(result, image, result);
            return result;
        });
    }

    /**
     * Executes the accumulate float stack operation.
     *
     * @param image       the image.
     * @param reference   the reference.
     * @param accumulator the accumulator.
     */
    public static void accumulateFloatStack(PlanarImage image, PlanarImage reference, Mat accumulator) {
        if (image == null || reference == null || accumulator == null || !hasSameDimensions(image, reference)) {
            return;
        }
        Mat mat = image.toMat();
        if (CvType.depth(image.type()) == CvType.CV_16S) {
            Mat floatImage = new Mat(reference.height(), reference.width(), CvType.CV_32F);
            mat.convertTo(floatImage, CvType.CV_32F);
            Imgproc.accumulate(floatImage, accumulator);
        } else {
            Imgproc.accumulate(mat, accumulator);
        }
    }

    /**
     * Executes the stack operation operation.
     *
     * @param sources   the sources.
     * @param operation the operation.
     * @return the operation result.
     */
    private static ImageCV processStackOperation(List<PlanarImage> sources, BinaryOperator<Mat> operation) {
        if (isInvalidStackSize(sources)) {
            return null;
        }
        PlanarImage reference = sources.get(0);
        ImageCV result = new ImageCV();
        reference.toMat().copyTo(result);

        for (int i = 1; i < sources.size(); i++) {
            PlanarImage image = sources.get(i);
            if (hasSameDimensions(image, result)) {
                operation.apply(result, image.toMat());
            }
        }
        return result;
    }

    /**
     * Checks whether the invalid stack size condition is true.
     *
     * @param sources the sources.
     * @return true if the invalid stack size condition is true; otherwise false.
     */
    private static boolean isInvalidStackSize(List<PlanarImage> sources) {
        return sources == null || sources.size() < MIN_STACK_SIZE;
    }

    /**
     * Checks whether the same dimensions condition is true.
     *
     * @param image     the image.
     * @param reference the reference.
     * @return true if the same dimensions condition is true; otherwise false.
     */
    private static boolean hasSameDimensions(PlanarImage image, PlanarImage reference) {
        return image.width() == reference.width() && image.height() == reference.height()
                && image.type() == reference.type();
    }

}
