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
package org.miaixz.bus.opencv.imgproc;

import org.miaixz.bus.opencv.core.Algorithm;
import org.miaixz.bus.opencv.core.Mat;
import org.miaixz.bus.opencv.core.Point;

/**
 * Base class for detecting template shapes with the generalized Hough transform.
 */
public class GeneralizedHough extends Algorithm {

    /**
     * Wraps an existing native GeneralizedHough handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected GeneralizedHough(long addr) {
        super(addr);
    }

    /**
     * Creates a Java wrapper for an existing native handle. This bridge is intended for binding and subclass
     * integration; the address must identify the native handle expected by this wrapper.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     * @return a new Java wrapper that retains the supplied native binding handle for cleanup
     */
    public static GeneralizedHough __fromPtr__(long addr) {
        return new GeneralizedHough(addr);
    }

    /**
     * Sets the template used by this GeneralizedHough instance.
     *
     * @param templ       template image used to build the generalized Hough model
     * @param templCenter templ center coordinates
     * @throws NullPointerException if templ, templCenter is null
     */
    public void setTemplate(Mat templ, Point templCenter) {
        setTemplate_0(nativeObj, templ.nativeObj, templCenter.x, templCenter.y);
    }

    /**
     * Sets the template used by this GeneralizedHough instance.
     *
     * @param templ template image used to build the generalized Hough model
     * @throws NullPointerException if templ is null
     */
    public void setTemplate(Mat templ) {
        setTemplate_1(nativeObj, templ.nativeObj);
    }

    /**
     * Sets the template used by this GeneralizedHough instance.
     *
     * @param edges       binary edge map; nonzero pixels identify edge locations
     * @param dx          horizontal image derivative
     * @param dy          vertical image derivative
     * @param templCenter templ center coordinates
     * @throws NullPointerException if edges, dx, dy, templCenter is null
     */
    public void setTemplate(Mat edges, Mat dx, Mat dy, Point templCenter) {
        setTemplate_2(nativeObj, edges.nativeObj, dx.nativeObj, dy.nativeObj, templCenter.x, templCenter.y);
    }

    /**
     * Sets the template used by this GeneralizedHough instance.
     *
     * @param edges binary edge map; nonzero pixels identify edge locations
     * @param dx    horizontal image derivative
     * @param dy    vertical image derivative
     * @throws NullPointerException if edges, dx, dy is null
     */
    public void setTemplate(Mat edges, Mat dx, Mat dy) {
        setTemplate_3(nativeObj, edges.nativeObj, dx.nativeObj, dy.nativeObj);
    }

    /**
     * Detects template occurrences and writes their positions, scales, and rotation angles to the output matrices.
     *
     * @param image     input image
     * @param positions output matrix of detected center coordinates, scales and rotation angles
     * @param votes     output matrix of vote counts supporting each detection
     * @throws NullPointerException if image, positions, votes is null
     */
    public void detect(Mat image, Mat positions, Mat votes) {
        detect_0(nativeObj, image.nativeObj, positions.nativeObj, votes.nativeObj);
    }

    /**
     * Detects template occurrences and writes their positions, scales, and rotation angles to the output matrices.
     *
     * @param image     input image
     * @param positions output matrix of detected center coordinates, scales and rotation angles
     * @throws NullPointerException if image, positions is null
     */
    public void detect(Mat image, Mat positions) {
        detect_1(nativeObj, image.nativeObj, positions.nativeObj);
    }

    /**
     * Detects template occurrences and writes their positions, scales, and rotation angles to the output matrices.
     *
     * @param edges     binary edge map; nonzero pixels identify edge locations
     * @param dx        horizontal image derivative
     * @param dy        vertical image derivative
     * @param positions output matrix of detected center coordinates, scales and rotation angles
     * @param votes     output matrix of vote counts supporting each detection
     * @throws NullPointerException if edges, dx, dy, positions, votes is null
     */
    public void detect(Mat edges, Mat dx, Mat dy, Mat positions, Mat votes) {
        detect_2(nativeObj, edges.nativeObj, dx.nativeObj, dy.nativeObj, positions.nativeObj, votes.nativeObj);
    }

    /**
     * Detects template occurrences and writes their positions, scales, and rotation angles to the output matrices.
     *
     * @param edges     binary edge map; nonzero pixels identify edge locations
     * @param dx        horizontal image derivative
     * @param dy        vertical image derivative
     * @param positions output matrix of detected center coordinates, scales and rotation angles
     * @throws NullPointerException if edges, dx, dy, positions is null
     */
    public void detect(Mat edges, Mat dx, Mat dy, Mat positions) {
        detect_3(nativeObj, edges.nativeObj, dx.nativeObj, dy.nativeObj, positions.nativeObj);
    }

    /**
     * Sets lower Canny edge-linking threshold.
     *
     * @param cannyLowThresh lower Canny edge-linking threshold
     */
    public void setCannyLowThresh(int cannyLowThresh) {
        setCannyLowThresh_0(nativeObj, cannyLowThresh);
    }

    /**
     * Returns lower Canny edge-linking threshold.
     *
     * @return lower Canny edge-linking threshold
     */
    public int getCannyLowThresh() {
        return getCannyLowThresh_0(nativeObj);
    }

    /**
     * Sets upper Canny strong-edge threshold.
     *
     * @param cannyHighThresh upper Canny strong-edge threshold
     */
    public void setCannyHighThresh(int cannyHighThresh) {
        setCannyHighThresh_0(nativeObj, cannyHighThresh);
    }

    /**
     * Returns upper Canny strong-edge threshold.
     *
     * @return upper Canny strong-edge threshold
     */
    public int getCannyHighThresh() {
        return getCannyHighThresh_0(nativeObj);
    }

    /**
     * Sets minimum distance in image pixels between detected centers.
     *
     * @param minDist minimum distance in image pixels between detected centers
     */
    public void setMinDist(double minDist) {
        setMinDist_0(nativeObj, minDist);
    }

    /**
     * Returns minimum distance in image pixels between detected centers.
     *
     * @return minimum distance in image pixels between detected centers
     */
    public double getMinDist() {
        return getMinDist_0(nativeObj);
    }

    /**
     * Sets ratio of image resolution to accumulator resolution.
     *
     * @param dp ratio of image resolution to accumulator resolution
     */
    public void setDp(double dp) {
        setDp_0(nativeObj, dp);
    }

    /**
     * Returns ratio of image resolution to accumulator resolution.
     *
     * @return ratio of image resolution to accumulator resolution
     */
    public double getDp() {
        return getDp_0(nativeObj);
    }

    /**
     * Sets maximum number of entries retained in an internal detector buffer.
     *
     * @param maxBufferSize maximum number of entries retained in an internal detector buffer
     */
    public void setMaxBufferSize(int maxBufferSize) {
        setMaxBufferSize_0(nativeObj, maxBufferSize);
    }

    /**
     * Returns maximum number of entries retained in an internal detector buffer.
     *
     * @return maximum number of entries retained in an internal detector buffer
     */
    public int getMaxBufferSize() {
        return getMaxBufferSize_0(nativeObj);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#setTemplate(Mat,Point)}.
     *
     * @param nativeObj       native binding handle of this {@code GeneralizedHough} instance
     * @param templ_nativeObj native matrix or object handle for {@code templ}; template image used to build the
     *                        generalized Hough model
     * @param templCenter_x   {@code templCenter.x} component of templ center coordinates
     * @param templCenter_y   {@code templCenter.y} component of templ center coordinates
     */
    private static native void setTemplate_0(
            long nativeObj,
            long templ_nativeObj,
            double templCenter_x,
            double templCenter_y);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#setTemplate(Mat)}.
     *
     * @param nativeObj       native binding handle of this {@code GeneralizedHough} instance
     * @param templ_nativeObj native matrix or object handle for {@code templ}; template image used to build the
     *                        generalized Hough model
     */
    private static native void setTemplate_1(long nativeObj, long templ_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#setTemplate(Mat,Mat,Mat,Point)}.
     *
     * @param nativeObj       native binding handle of this {@code GeneralizedHough} instance
     * @param edges_nativeObj native matrix or object handle for {@code edges}; binary edge map; nonzero pixels identify
     *                        edge locations
     * @param dx_nativeObj    native matrix or object handle for {@code dx}; horizontal image derivative
     * @param dy_nativeObj    native matrix or object handle for {@code dy}; vertical image derivative
     * @param templCenter_x   {@code templCenter.x} component of templ center coordinates
     * @param templCenter_y   {@code templCenter.y} component of templ center coordinates
     */
    private static native void setTemplate_2(
            long nativeObj,
            long edges_nativeObj,
            long dx_nativeObj,
            long dy_nativeObj,
            double templCenter_x,
            double templCenter_y);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#setTemplate(Mat,Mat,Mat)}.
     *
     * @param nativeObj       native binding handle of this {@code GeneralizedHough} instance
     * @param edges_nativeObj native matrix or object handle for {@code edges}; binary edge map; nonzero pixels identify
     *                        edge locations
     * @param dx_nativeObj    native matrix or object handle for {@code dx}; horizontal image derivative
     * @param dy_nativeObj    native matrix or object handle for {@code dy}; vertical image derivative
     */
    private static native void setTemplate_3(
            long nativeObj,
            long edges_nativeObj,
            long dx_nativeObj,
            long dy_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#detect(Mat,Mat,Mat)}.
     *
     * @param nativeObj           native binding handle of this {@code GeneralizedHough} instance
     * @param image_nativeObj     native matrix or object handle for {@code image}; input image
     * @param positions_nativeObj native matrix or object handle for {@code positions}; output matrix of detected center
     *                            coordinates, scales and rotation angles
     * @param votes_nativeObj     native matrix or object handle for {@code votes}; output matrix of vote counts
     *                            supporting each detection
     */
    private static native void detect_0(
            long nativeObj,
            long image_nativeObj,
            long positions_nativeObj,
            long votes_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#detect(Mat,Mat)}.
     *
     * @param nativeObj           native binding handle of this {@code GeneralizedHough} instance
     * @param image_nativeObj     native matrix or object handle for {@code image}; input image
     * @param positions_nativeObj native matrix or object handle for {@code positions}; output matrix of detected center
     *                            coordinates, scales and rotation angles
     */
    private static native void detect_1(long nativeObj, long image_nativeObj, long positions_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#detect(Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param nativeObj           native binding handle of this {@code GeneralizedHough} instance
     * @param edges_nativeObj     native matrix or object handle for {@code edges}; binary edge map; nonzero pixels
     *                            identify edge locations
     * @param dx_nativeObj        native matrix or object handle for {@code dx}; horizontal image derivative
     * @param dy_nativeObj        native matrix or object handle for {@code dy}; vertical image derivative
     * @param positions_nativeObj native matrix or object handle for {@code positions}; output matrix of detected center
     *                            coordinates, scales and rotation angles
     * @param votes_nativeObj     native matrix or object handle for {@code votes}; output matrix of vote counts
     *                            supporting each detection
     */
    private static native void detect_2(
            long nativeObj,
            long edges_nativeObj,
            long dx_nativeObj,
            long dy_nativeObj,
            long positions_nativeObj,
            long votes_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#detect(Mat,Mat,Mat,Mat)}.
     *
     * @param nativeObj           native binding handle of this {@code GeneralizedHough} instance
     * @param edges_nativeObj     native matrix or object handle for {@code edges}; binary edge map; nonzero pixels
     *                            identify edge locations
     * @param dx_nativeObj        native matrix or object handle for {@code dx}; horizontal image derivative
     * @param dy_nativeObj        native matrix or object handle for {@code dy}; vertical image derivative
     * @param positions_nativeObj native matrix or object handle for {@code positions}; output matrix of detected center
     *                            coordinates, scales and rotation angles
     */
    private static native void detect_3(
            long nativeObj,
            long edges_nativeObj,
            long dx_nativeObj,
            long dy_nativeObj,
            long positions_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#setCannyLowThresh(int)}.
     *
     * @param nativeObj      native binding handle of this {@code GeneralizedHough} instance
     * @param cannyLowThresh lower Canny edge-linking threshold
     */
    private static native void setCannyLowThresh_0(long nativeObj, int cannyLowThresh);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#getCannyLowThresh()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHough} instance
     * @return lower Canny edge-linking threshold
     */
    private static native int getCannyLowThresh_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#setCannyHighThresh(int)}.
     *
     * @param nativeObj       native binding handle of this {@code GeneralizedHough} instance
     * @param cannyHighThresh upper Canny strong-edge threshold
     */
    private static native void setCannyHighThresh_0(long nativeObj, int cannyHighThresh);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#getCannyHighThresh()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHough} instance
     * @return upper Canny strong-edge threshold
     */
    private static native int getCannyHighThresh_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#setMinDist(double)}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHough} instance
     * @param minDist   minimum distance in image pixels between detected centers
     */
    private static native void setMinDist_0(long nativeObj, double minDist);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#getMinDist()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHough} instance
     * @return minimum distance in image pixels between detected centers
     */
    private static native double getMinDist_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#setDp(double)}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHough} instance
     * @param dp        ratio of image resolution to accumulator resolution
     */
    private static native void setDp_0(long nativeObj, double dp);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#getDp()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHough} instance
     * @return ratio of image resolution to accumulator resolution
     */
    private static native double getDp_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#setMaxBufferSize(int)}.
     *
     * @param nativeObj     native binding handle of this {@code GeneralizedHough} instance
     * @param maxBufferSize maximum number of entries retained in an internal detector buffer
     */
    private static native void setMaxBufferSize_0(long nativeObj, int maxBufferSize);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHough#getMaxBufferSize()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHough} instance
     * @return maximum number of entries retained in an internal detector buffer
     */
    private static native int getMaxBufferSize_0(long nativeObj);

    /**
     * Deletes the native {@code GeneralizedHough} binding handle. This compatibility declaration is retained in
     * addition to cleanup inherited from Algorithm.
     *
     * @param nativeObj native binding handle of the matching algorithm type
     */
    private static native void delete(long nativeObj);

}
