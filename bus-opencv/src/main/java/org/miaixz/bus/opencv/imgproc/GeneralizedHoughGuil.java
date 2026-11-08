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

/**
 * Generalized Hough detector using the Guil template representation.
 */
public class GeneralizedHoughGuil extends GeneralizedHough {

    /**
     * Wraps an existing native GeneralizedHoughGuil handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected GeneralizedHoughGuil(long addr) {
        super(addr);
    }

    /**
     * Creates a Java wrapper for an existing native handle. This bridge is intended for binding and subclass
     * integration; the address must identify the native handle expected by this wrapper.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     * @return a new Java wrapper that retains the supplied native binding handle for cleanup
     */
    public static GeneralizedHoughGuil __fromPtr__(long addr) {
        return new GeneralizedHoughGuil(addr);
    }

    /**
     * Sets angle difference used to select feature-point pairs.
     *
     * @param xi angle difference used to select feature-point pairs
     */
    public void setXi(double xi) {
        setXi_0(nativeObj, xi);
    }

    /**
     * Returns angle difference used to select feature-point pairs.
     *
     * @return angle difference used to select feature-point pairs
     */
    public double getXi() {
        return getXi_0(nativeObj);
    }

    /**
     * Sets number of orientation quantization levels.
     *
     * @param levels number of orientation quantization levels
     */
    public void setLevels(int levels) {
        setLevels_0(nativeObj, levels);
    }

    /**
     * Returns number of orientation quantization levels.
     *
     * @return number of orientation quantization levels
     */
    public int getLevels() {
        return getLevels_0(nativeObj);
    }

    /**
     * Sets tolerance for comparing feature-pair angles.
     *
     * @param angleEpsilon tolerance for comparing feature-pair angles
     */
    public void setAngleEpsilon(double angleEpsilon) {
        setAngleEpsilon_0(nativeObj, angleEpsilon);
    }

    /**
     * Returns tolerance for comparing feature-pair angles.
     *
     * @return tolerance for comparing feature-pair angles
     */
    public double getAngleEpsilon() {
        return getAngleEpsilon_0(nativeObj);
    }

    /**
     * Sets minimum rotation angle searched, in degrees.
     *
     * @param minAngle minimum rotation angle searched, in degrees
     */
    public void setMinAngle(double minAngle) {
        setMinAngle_0(nativeObj, minAngle);
    }

    /**
     * Returns minimum rotation angle searched, in degrees.
     *
     * @return minimum rotation angle searched, in degrees
     */
    public double getMinAngle() {
        return getMinAngle_0(nativeObj);
    }

    /**
     * Sets maximum rotation angle searched, in degrees.
     *
     * @param maxAngle maximum rotation angle searched, in degrees
     */
    public void setMaxAngle(double maxAngle) {
        setMaxAngle_0(nativeObj, maxAngle);
    }

    /**
     * Returns maximum rotation angle searched, in degrees.
     *
     * @return maximum rotation angle searched, in degrees
     */
    public double getMaxAngle() {
        return getMaxAngle_0(nativeObj);
    }

    /**
     * Sets rotation-angle sampling step, in degrees.
     *
     * @param angleStep rotation-angle sampling step, in degrees
     */
    public void setAngleStep(double angleStep) {
        setAngleStep_0(nativeObj, angleStep);
    }

    /**
     * Returns rotation-angle sampling step, in degrees.
     *
     * @return rotation-angle sampling step, in degrees
     */
    public double getAngleStep() {
        return getAngleStep_0(nativeObj);
    }

    /**
     * Sets minimum vote count for a rotation-angle hypothesis.
     *
     * @param angleThresh minimum vote count for a rotation-angle hypothesis
     */
    public void setAngleThresh(int angleThresh) {
        setAngleThresh_0(nativeObj, angleThresh);
    }

    /**
     * Returns minimum vote count for a rotation-angle hypothesis.
     *
     * @return minimum vote count for a rotation-angle hypothesis
     */
    public int getAngleThresh() {
        return getAngleThresh_0(nativeObj);
    }

    /**
     * Sets minimum scale searched.
     *
     * @param minScale minimum scale searched
     */
    public void setMinScale(double minScale) {
        setMinScale_0(nativeObj, minScale);
    }

    /**
     * Returns minimum scale searched.
     *
     * @return minimum scale searched
     */
    public double getMinScale() {
        return getMinScale_0(nativeObj);
    }

    /**
     * Sets maximum scale searched.
     *
     * @param maxScale maximum scale searched
     */
    public void setMaxScale(double maxScale) {
        setMaxScale_0(nativeObj, maxScale);
    }

    /**
     * Returns maximum scale searched.
     *
     * @return maximum scale searched
     */
    public double getMaxScale() {
        return getMaxScale_0(nativeObj);
    }

    /**
     * Sets scale sampling step.
     *
     * @param scaleStep scale sampling step
     */
    public void setScaleStep(double scaleStep) {
        setScaleStep_0(nativeObj, scaleStep);
    }

    /**
     * Returns scale sampling step.
     *
     * @return scale sampling step
     */
    public double getScaleStep() {
        return getScaleStep_0(nativeObj);
    }

    /**
     * Sets minimum vote count for a scale hypothesis.
     *
     * @param scaleThresh minimum vote count for a scale hypothesis
     */
    public void setScaleThresh(int scaleThresh) {
        setScaleThresh_0(nativeObj, scaleThresh);
    }

    /**
     * Returns minimum vote count for a scale hypothesis.
     *
     * @return minimum vote count for a scale hypothesis
     */
    public int getScaleThresh() {
        return getScaleThresh_0(nativeObj);
    }

    /**
     * Sets minimum vote count for a position hypothesis.
     *
     * @param posThresh minimum vote count for a position hypothesis
     */
    public void setPosThresh(int posThresh) {
        setPosThresh_0(nativeObj, posThresh);
    }

    /**
     * Returns minimum vote count for a position hypothesis.
     *
     * @return minimum vote count for a position hypothesis
     */
    public int getPosThresh() {
        return getPosThresh_0(nativeObj);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#setXi(double)}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @param xi        angle difference used to select feature-point pairs
     */
    private static native void setXi_0(long nativeObj, double xi);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#getXi()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @return angle difference used to select feature-point pairs
     */
    private static native double getXi_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#setLevels(int)}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @param levels    number of orientation quantization levels
     */
    private static native void setLevels_0(long nativeObj, int levels);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#getLevels()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @return number of orientation quantization levels
     */
    private static native int getLevels_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#setAngleEpsilon(double)}.
     *
     * @param nativeObj    native binding handle of this {@code GeneralizedHoughGuil} instance
     * @param angleEpsilon tolerance for comparing feature-pair angles
     */
    private static native void setAngleEpsilon_0(long nativeObj, double angleEpsilon);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#getAngleEpsilon()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @return tolerance for comparing feature-pair angles
     */
    private static native double getAngleEpsilon_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#setMinAngle(double)}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @param minAngle  minimum rotation angle searched, in degrees
     */
    private static native void setMinAngle_0(long nativeObj, double minAngle);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#getMinAngle()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @return minimum rotation angle searched, in degrees
     */
    private static native double getMinAngle_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#setMaxAngle(double)}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @param maxAngle  maximum rotation angle searched, in degrees
     */
    private static native void setMaxAngle_0(long nativeObj, double maxAngle);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#getMaxAngle()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @return maximum rotation angle searched, in degrees
     */
    private static native double getMaxAngle_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#setAngleStep(double)}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @param angleStep rotation-angle sampling step, in degrees
     */
    private static native void setAngleStep_0(long nativeObj, double angleStep);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#getAngleStep()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @return rotation-angle sampling step, in degrees
     */
    private static native double getAngleStep_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#setAngleThresh(int)}.
     *
     * @param nativeObj   native binding handle of this {@code GeneralizedHoughGuil} instance
     * @param angleThresh minimum vote count for a rotation-angle hypothesis
     */
    private static native void setAngleThresh_0(long nativeObj, int angleThresh);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#getAngleThresh()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @return minimum vote count for a rotation-angle hypothesis
     */
    private static native int getAngleThresh_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#setMinScale(double)}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @param minScale  minimum scale searched
     */
    private static native void setMinScale_0(long nativeObj, double minScale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#getMinScale()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @return minimum scale searched
     */
    private static native double getMinScale_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#setMaxScale(double)}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @param maxScale  maximum scale searched
     */
    private static native void setMaxScale_0(long nativeObj, double maxScale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#getMaxScale()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @return maximum scale searched
     */
    private static native double getMaxScale_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#setScaleStep(double)}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @param scaleStep scale sampling step
     */
    private static native void setScaleStep_0(long nativeObj, double scaleStep);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#getScaleStep()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @return scale sampling step
     */
    private static native double getScaleStep_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#setScaleThresh(int)}.
     *
     * @param nativeObj   native binding handle of this {@code GeneralizedHoughGuil} instance
     * @param scaleThresh minimum vote count for a scale hypothesis
     */
    private static native void setScaleThresh_0(long nativeObj, int scaleThresh);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#getScaleThresh()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @return minimum vote count for a scale hypothesis
     */
    private static native int getScaleThresh_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#setPosThresh(int)}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @param posThresh minimum vote count for a position hypothesis
     */
    private static native void setPosThresh_0(long nativeObj, int posThresh);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughGuil#getPosThresh()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughGuil} instance
     * @return minimum vote count for a position hypothesis
     */
    private static native int getPosThresh_0(long nativeObj);

    /**
     * Deletes the native {@code GeneralizedHoughGuil} binding handle. This compatibility declaration is retained in
     * addition to cleanup inherited from Algorithm.
     *
     * @param nativeObj native binding handle of the matching algorithm type
     */
    private static native void delete(long nativeObj);

}
