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

// C++: class GeneralizedHoughGuil

/**
 * finds arbitrary template in the grayscale image using Generalized Hough Transform
 * <p>
 * Detects position, translation and rotation CITE: Guil1999 .
 */
public class GeneralizedHoughGuil extends GeneralizedHough {

    /**
     * Creates a new {@code GeneralizedHoughGuil} instance.
     *
     * @param addr the {@code addr} value
     */
    protected GeneralizedHoughGuil(long addr) {
        super(addr);
    }

    // internal usage only
    /**
     * Performs the {@code __fromPtr__} operation.
     *
     * @param addr the {@code addr} value
     * @return the operation result
     */
    public static GeneralizedHoughGuil __fromPtr__(long addr) {
        return new GeneralizedHoughGuil(addr);
    }

    //
    // C++: void cv::GeneralizedHoughGuil::setXi(double xi)
    //

    // C++: void cv::GeneralizedHoughGuil::setXi(double xi)
    private static native void setXi_0(long nativeObj, double xi);

    //
    // C++: double cv::GeneralizedHoughGuil::getXi()
    //

    // C++: double cv::GeneralizedHoughGuil::getXi()
    private static native double getXi_0(long nativeObj);

    //
    // C++: void cv::GeneralizedHoughGuil::setLevels(int levels)
    //

    // C++: void cv::GeneralizedHoughGuil::setLevels(int levels)
    private static native void setLevels_0(long nativeObj, int levels);

    //
    // C++: int cv::GeneralizedHoughGuil::getLevels()
    //

    // C++: int cv::GeneralizedHoughGuil::getLevels()
    private static native int getLevels_0(long nativeObj);

    //
    // C++: void cv::GeneralizedHoughGuil::setAngleEpsilon(double angleEpsilon)
    //

    // C++: void cv::GeneralizedHoughGuil::setAngleEpsilon(double angleEpsilon)
    private static native void setAngleEpsilon_0(long nativeObj, double angleEpsilon);

    //
    // C++: double cv::GeneralizedHoughGuil::getAngleEpsilon()
    //

    // C++: double cv::GeneralizedHoughGuil::getAngleEpsilon()
    private static native double getAngleEpsilon_0(long nativeObj);

    //
    // C++: void cv::GeneralizedHoughGuil::setMinAngle(double minAngle)
    //

    // C++: void cv::GeneralizedHoughGuil::setMinAngle(double minAngle)
    private static native void setMinAngle_0(long nativeObj, double minAngle);

    //
    // C++: double cv::GeneralizedHoughGuil::getMinAngle()
    //

    // C++: double cv::GeneralizedHoughGuil::getMinAngle()
    private static native double getMinAngle_0(long nativeObj);

    //
    // C++: void cv::GeneralizedHoughGuil::setMaxAngle(double maxAngle)
    //

    // C++: void cv::GeneralizedHoughGuil::setMaxAngle(double maxAngle)
    private static native void setMaxAngle_0(long nativeObj, double maxAngle);

    //
    // C++: double cv::GeneralizedHoughGuil::getMaxAngle()
    //

    // C++: double cv::GeneralizedHoughGuil::getMaxAngle()
    private static native double getMaxAngle_0(long nativeObj);

    //
    // C++: void cv::GeneralizedHoughGuil::setAngleStep(double angleStep)
    //

    // C++: void cv::GeneralizedHoughGuil::setAngleStep(double angleStep)
    private static native void setAngleStep_0(long nativeObj, double angleStep);

    //
    // C++: double cv::GeneralizedHoughGuil::getAngleStep()
    //

    // C++: double cv::GeneralizedHoughGuil::getAngleStep()
    private static native double getAngleStep_0(long nativeObj);

    //
    // C++: void cv::GeneralizedHoughGuil::setAngleThresh(int angleThresh)
    //

    // C++: void cv::GeneralizedHoughGuil::setAngleThresh(int angleThresh)
    private static native void setAngleThresh_0(long nativeObj, int angleThresh);

    //
    // C++: int cv::GeneralizedHoughGuil::getAngleThresh()
    //

    // C++: int cv::GeneralizedHoughGuil::getAngleThresh()
    private static native int getAngleThresh_0(long nativeObj);

    //
    // C++: void cv::GeneralizedHoughGuil::setMinScale(double minScale)
    //

    // C++: void cv::GeneralizedHoughGuil::setMinScale(double minScale)
    private static native void setMinScale_0(long nativeObj, double minScale);

    //
    // C++: double cv::GeneralizedHoughGuil::getMinScale()
    //

    // C++: double cv::GeneralizedHoughGuil::getMinScale()
    private static native double getMinScale_0(long nativeObj);

    //
    // C++: void cv::GeneralizedHoughGuil::setMaxScale(double maxScale)
    //

    // C++: void cv::GeneralizedHoughGuil::setMaxScale(double maxScale)
    private static native void setMaxScale_0(long nativeObj, double maxScale);

    //
    // C++: double cv::GeneralizedHoughGuil::getMaxScale()
    //

    // C++: double cv::GeneralizedHoughGuil::getMaxScale()
    private static native double getMaxScale_0(long nativeObj);

    //
    // C++: void cv::GeneralizedHoughGuil::setScaleStep(double scaleStep)
    //

    // C++: void cv::GeneralizedHoughGuil::setScaleStep(double scaleStep)
    private static native void setScaleStep_0(long nativeObj, double scaleStep);

    //
    // C++: double cv::GeneralizedHoughGuil::getScaleStep()
    //

    // C++: double cv::GeneralizedHoughGuil::getScaleStep()
    private static native double getScaleStep_0(long nativeObj);

    //
    // C++: void cv::GeneralizedHoughGuil::setScaleThresh(int scaleThresh)
    //

    // C++: void cv::GeneralizedHoughGuil::setScaleThresh(int scaleThresh)
    private static native void setScaleThresh_0(long nativeObj, int scaleThresh);

    //
    // C++: int cv::GeneralizedHoughGuil::getScaleThresh()
    //

    // C++: int cv::GeneralizedHoughGuil::getScaleThresh()
    private static native int getScaleThresh_0(long nativeObj);

    //
    // C++: void cv::GeneralizedHoughGuil::setPosThresh(int posThresh)
    //

    // C++: void cv::GeneralizedHoughGuil::setPosThresh(int posThresh)
    private static native void setPosThresh_0(long nativeObj, int posThresh);

    //
    // C++: int cv::GeneralizedHoughGuil::getPosThresh()
    //

    // C++: int cv::GeneralizedHoughGuil::getPosThresh()
    private static native int getPosThresh_0(long nativeObj);

    // native support for java finalize() or cleaner
    private static native void delete(long nativeObj);

    /**
     * Performs the {@code getXi} operation.
     *
     * @return the operation result
     */
    public double getXi() {
        return getXi_0(nativeObj);
    }

    /**
     * Performs the {@code setXi} operation.
     *
     * @param xi the {@code xi} value
     */
    public void setXi(double xi) {
        setXi_0(nativeObj, xi);
    }

    /**
     * Performs the {@code getLevels} operation.
     *
     * @return the operation result
     */
    public int getLevels() {
        return getLevels_0(nativeObj);
    }

    /**
     * Performs the {@code setLevels} operation.
     *
     * @param levels the {@code levels} value
     */
    public void setLevels(int levels) {
        setLevels_0(nativeObj, levels);
    }

    /**
     * Performs the {@code getAngleEpsilon} operation.
     *
     * @return the operation result
     */
    public double getAngleEpsilon() {
        return getAngleEpsilon_0(nativeObj);
    }

    /**
     * Performs the {@code setAngleEpsilon} operation.
     *
     * @param angleEpsilon the {@code angleEpsilon} value
     */
    public void setAngleEpsilon(double angleEpsilon) {
        setAngleEpsilon_0(nativeObj, angleEpsilon);
    }

    /**
     * Performs the {@code getMinAngle} operation.
     *
     * @return the operation result
     */
    public double getMinAngle() {
        return getMinAngle_0(nativeObj);
    }

    /**
     * Performs the {@code setMinAngle} operation.
     *
     * @param minAngle the {@code minAngle} value
     */
    public void setMinAngle(double minAngle) {
        setMinAngle_0(nativeObj, minAngle);
    }

    /**
     * Performs the {@code getMaxAngle} operation.
     *
     * @return the operation result
     */
    public double getMaxAngle() {
        return getMaxAngle_0(nativeObj);
    }

    /**
     * Performs the {@code setMaxAngle} operation.
     *
     * @param maxAngle the {@code maxAngle} value
     */
    public void setMaxAngle(double maxAngle) {
        setMaxAngle_0(nativeObj, maxAngle);
    }

    /**
     * Performs the {@code getAngleStep} operation.
     *
     * @return the operation result
     */
    public double getAngleStep() {
        return getAngleStep_0(nativeObj);
    }

    /**
     * Performs the {@code setAngleStep} operation.
     *
     * @param angleStep the {@code angleStep} value
     */
    public void setAngleStep(double angleStep) {
        setAngleStep_0(nativeObj, angleStep);
    }

    /**
     * Performs the {@code getAngleThresh} operation.
     *
     * @return the operation result
     */
    public int getAngleThresh() {
        return getAngleThresh_0(nativeObj);
    }

    /**
     * Performs the {@code setAngleThresh} operation.
     *
     * @param angleThresh the {@code angleThresh} value
     */
    public void setAngleThresh(int angleThresh) {
        setAngleThresh_0(nativeObj, angleThresh);
    }

    /**
     * Performs the {@code getMinScale} operation.
     *
     * @return the operation result
     */
    public double getMinScale() {
        return getMinScale_0(nativeObj);
    }

    /**
     * Performs the {@code setMinScale} operation.
     *
     * @param minScale the {@code minScale} value
     */
    public void setMinScale(double minScale) {
        setMinScale_0(nativeObj, minScale);
    }

    /**
     * Performs the {@code getMaxScale} operation.
     *
     * @return the operation result
     */
    public double getMaxScale() {
        return getMaxScale_0(nativeObj);
    }

    /**
     * Performs the {@code setMaxScale} operation.
     *
     * @param maxScale the {@code maxScale} value
     */
    public void setMaxScale(double maxScale) {
        setMaxScale_0(nativeObj, maxScale);
    }

    /**
     * Performs the {@code getScaleStep} operation.
     *
     * @return the operation result
     */
    public double getScaleStep() {
        return getScaleStep_0(nativeObj);
    }

    /**
     * Performs the {@code setScaleStep} operation.
     *
     * @param scaleStep the {@code scaleStep} value
     */
    public void setScaleStep(double scaleStep) {
        setScaleStep_0(nativeObj, scaleStep);
    }

    /**
     * Performs the {@code getScaleThresh} operation.
     *
     * @return the operation result
     */
    public int getScaleThresh() {
        return getScaleThresh_0(nativeObj);
    }

    /**
     * Performs the {@code setScaleThresh} operation.
     *
     * @param scaleThresh the {@code scaleThresh} value
     */
    public void setScaleThresh(int scaleThresh) {
        setScaleThresh_0(nativeObj, scaleThresh);
    }

    /**
     * Performs the {@code getPosThresh} operation.
     *
     * @return the operation result
     */
    public int getPosThresh() {
        return getPosThresh_0(nativeObj);
    }

    /**
     * Performs the {@code setPosThresh} operation.
     *
     * @param posThresh the {@code posThresh} value
     */
    public void setPosThresh(int posThresh) {
        setPosThresh_0(nativeObj, posThresh);
    }

}
