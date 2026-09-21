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
package org.opencv.imgproc;

import org.opencv.core.Algorithm;
import org.opencv.core.Mat;
import org.opencv.core.Point;

// C++: class GeneralizedHough

/**
 * finds arbitrary template in the grayscale image using Generalized Hough Transform
 */
public class GeneralizedHough extends Algorithm {

    /**
     * Creates a new {@code GeneralizedHough} instance.
     *
     * @param addr the {@code addr} value
     */
    protected GeneralizedHough(long addr) {
        super(addr);
    }

    // internal usage only
    /**
     * Performs the {@code __fromPtr__} operation.
     *
     * @param addr the {@code addr} value
     * @return the operation result
     */
    public static GeneralizedHough __fromPtr__(long addr) {
        return new GeneralizedHough(addr);
    }

    //
    // C++: void cv::GeneralizedHough::setTemplate(Mat templ, Point templCenter = Point(-1, -1))
    //

    // C++: void cv::GeneralizedHough::setTemplate(Mat templ, Point templCenter = Point(-1, -1))
    private static native void setTemplate_0(
            long nativeObj,
            long templ_nativeObj,
            double templCenter_x,
            double templCenter_y);

    private static native void setTemplate_1(long nativeObj, long templ_nativeObj);

    //
    // C++: void cv::GeneralizedHough::setTemplate(Mat edges, Mat dx, Mat dy, Point templCenter = Point(-1, -1))
    //

    // C++: void cv::GeneralizedHough::setTemplate(Mat edges, Mat dx, Mat dy, Point templCenter = Point(-1, -1))
    private static native void setTemplate_2(
            long nativeObj,
            long edges_nativeObj,
            long dx_nativeObj,
            long dy_nativeObj,
            double templCenter_x,
            double templCenter_y);

    private static native void setTemplate_3(
            long nativeObj,
            long edges_nativeObj,
            long dx_nativeObj,
            long dy_nativeObj);

    //
    // C++: void cv::GeneralizedHough::detect(Mat image, Mat& positions, Mat& votes = Mat())
    //

    // C++: void cv::GeneralizedHough::detect(Mat image, Mat& positions, Mat& votes = Mat())
    private static native void detect_0(
            long nativeObj,
            long image_nativeObj,
            long positions_nativeObj,
            long votes_nativeObj);

    private static native void detect_1(long nativeObj, long image_nativeObj, long positions_nativeObj);

    //
    // C++: void cv::GeneralizedHough::detect(Mat edges, Mat dx, Mat dy, Mat& positions, Mat& votes = Mat())
    //

    // C++: void cv::GeneralizedHough::detect(Mat edges, Mat dx, Mat dy, Mat& positions, Mat& votes = Mat())
    private static native void detect_2(
            long nativeObj,
            long edges_nativeObj,
            long dx_nativeObj,
            long dy_nativeObj,
            long positions_nativeObj,
            long votes_nativeObj);

    private static native void detect_3(
            long nativeObj,
            long edges_nativeObj,
            long dx_nativeObj,
            long dy_nativeObj,
            long positions_nativeObj);

    //
    // C++: void cv::GeneralizedHough::setCannyLowThresh(int cannyLowThresh)
    //

    // C++: void cv::GeneralizedHough::setCannyLowThresh(int cannyLowThresh)
    private static native void setCannyLowThresh_0(long nativeObj, int cannyLowThresh);

    //
    // C++: int cv::GeneralizedHough::getCannyLowThresh()
    //

    // C++: int cv::GeneralizedHough::getCannyLowThresh()
    private static native int getCannyLowThresh_0(long nativeObj);

    //
    // C++: void cv::GeneralizedHough::setCannyHighThresh(int cannyHighThresh)
    //

    // C++: void cv::GeneralizedHough::setCannyHighThresh(int cannyHighThresh)
    private static native void setCannyHighThresh_0(long nativeObj, int cannyHighThresh);

    //
    // C++: int cv::GeneralizedHough::getCannyHighThresh()
    //

    // C++: int cv::GeneralizedHough::getCannyHighThresh()
    private static native int getCannyHighThresh_0(long nativeObj);

    //
    // C++: void cv::GeneralizedHough::setMinDist(double minDist)
    //

    // C++: void cv::GeneralizedHough::setMinDist(double minDist)
    private static native void setMinDist_0(long nativeObj, double minDist);

    //
    // C++: double cv::GeneralizedHough::getMinDist()
    //

    // C++: double cv::GeneralizedHough::getMinDist()
    private static native double getMinDist_0(long nativeObj);

    //
    // C++: void cv::GeneralizedHough::setDp(double dp)
    //

    // C++: void cv::GeneralizedHough::setDp(double dp)
    private static native void setDp_0(long nativeObj, double dp);

    //
    // C++: double cv::GeneralizedHough::getDp()
    //

    // C++: double cv::GeneralizedHough::getDp()
    private static native double getDp_0(long nativeObj);

    //
    // C++: void cv::GeneralizedHough::setMaxBufferSize(int maxBufferSize)
    //

    // C++: void cv::GeneralizedHough::setMaxBufferSize(int maxBufferSize)
    private static native void setMaxBufferSize_0(long nativeObj, int maxBufferSize);

    //
    // C++: int cv::GeneralizedHough::getMaxBufferSize()
    //

    // C++: int cv::GeneralizedHough::getMaxBufferSize()
    private static native int getMaxBufferSize_0(long nativeObj);

    // native support for java finalize() or cleaner
    private static native void delete(long nativeObj);

    /**
     * Performs the {@code setTemplate} operation.
     *
     * @param templ the {@code templ} value
     * @param templCenter the {@code templCenter} value
     */
    public void setTemplate(Mat templ, Point templCenter) {
        setTemplate_0(nativeObj, templ.nativeObj, templCenter.x, templCenter.y);
    }

    /**
     * Performs the {@code setTemplate} operation.
     *
     * @param templ the {@code templ} value
     */
    public void setTemplate(Mat templ) {
        setTemplate_1(nativeObj, templ.nativeObj);
    }

    /**
     * Performs the {@code setTemplate} operation.
     *
     * @param edges the {@code edges} value
     * @param dx the {@code dx} value
     * @param dy the {@code dy} value
     * @param templCenter the {@code templCenter} value
     */
    public void setTemplate(Mat edges, Mat dx, Mat dy, Point templCenter) {
        setTemplate_2(nativeObj, edges.nativeObj, dx.nativeObj, dy.nativeObj, templCenter.x, templCenter.y);
    }

    /**
     * Performs the {@code setTemplate} operation.
     *
     * @param edges the {@code edges} value
     * @param dx the {@code dx} value
     * @param dy the {@code dy} value
     */
    public void setTemplate(Mat edges, Mat dx, Mat dy) {
        setTemplate_3(nativeObj, edges.nativeObj, dx.nativeObj, dy.nativeObj);
    }

    /**
     * Performs the {@code detect} operation.
     *
     * @param image the {@code image} value
     * @param positions the {@code positions} value
     * @param votes the {@code votes} value
     */
    public void detect(Mat image, Mat positions, Mat votes) {
        detect_0(nativeObj, image.nativeObj, positions.nativeObj, votes.nativeObj);
    }

    /**
     * Performs the {@code detect} operation.
     *
     * @param image the {@code image} value
     * @param positions the {@code positions} value
     */
    public void detect(Mat image, Mat positions) {
        detect_1(nativeObj, image.nativeObj, positions.nativeObj);
    }

    /**
     * Performs the {@code detect} operation.
     *
     * @param edges the {@code edges} value
     * @param dx the {@code dx} value
     * @param dy the {@code dy} value
     * @param positions the {@code positions} value
     * @param votes the {@code votes} value
     */
    public void detect(Mat edges, Mat dx, Mat dy, Mat positions, Mat votes) {
        detect_2(nativeObj, edges.nativeObj, dx.nativeObj, dy.nativeObj, positions.nativeObj, votes.nativeObj);
    }

    /**
     * Performs the {@code detect} operation.
     *
     * @param edges the {@code edges} value
     * @param dx the {@code dx} value
     * @param dy the {@code dy} value
     * @param positions the {@code positions} value
     */
    public void detect(Mat edges, Mat dx, Mat dy, Mat positions) {
        detect_3(nativeObj, edges.nativeObj, dx.nativeObj, dy.nativeObj, positions.nativeObj);
    }

    /**
     * Performs the {@code getCannyLowThresh} operation.
     *
     * @return the operation result
     */
    public int getCannyLowThresh() {
        return getCannyLowThresh_0(nativeObj);
    }

    /**
     * Performs the {@code setCannyLowThresh} operation.
     *
     * @param cannyLowThresh the {@code cannyLowThresh} value
     */
    public void setCannyLowThresh(int cannyLowThresh) {
        setCannyLowThresh_0(nativeObj, cannyLowThresh);
    }

    /**
     * Performs the {@code getCannyHighThresh} operation.
     *
     * @return the operation result
     */
    public int getCannyHighThresh() {
        return getCannyHighThresh_0(nativeObj);
    }

    /**
     * Performs the {@code setCannyHighThresh} operation.
     *
     * @param cannyHighThresh the {@code cannyHighThresh} value
     */
    public void setCannyHighThresh(int cannyHighThresh) {
        setCannyHighThresh_0(nativeObj, cannyHighThresh);
    }

    /**
     * Performs the {@code getMinDist} operation.
     *
     * @return the operation result
     */
    public double getMinDist() {
        return getMinDist_0(nativeObj);
    }

    /**
     * Performs the {@code setMinDist} operation.
     *
     * @param minDist the {@code minDist} value
     */
    public void setMinDist(double minDist) {
        setMinDist_0(nativeObj, minDist);
    }

    /**
     * Performs the {@code getDp} operation.
     *
     * @return the operation result
     */
    public double getDp() {
        return getDp_0(nativeObj);
    }

    /**
     * Performs the {@code setDp} operation.
     *
     * @param dp the {@code dp} value
     */
    public void setDp(double dp) {
        setDp_0(nativeObj, dp);
    }

    /**
     * Performs the {@code getMaxBufferSize} operation.
     *
     * @return the operation result
     */
    public int getMaxBufferSize() {
        return getMaxBufferSize_0(nativeObj);
    }

    /**
     * Performs the {@code setMaxBufferSize} operation.
     *
     * @param maxBufferSize the {@code maxBufferSize} value
     */
    public void setMaxBufferSize(int maxBufferSize) {
        setMaxBufferSize_0(nativeObj, maxBufferSize);
    }

}
