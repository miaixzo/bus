//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.imgproc;

// C++: class GeneralizedHoughGuil

/**
 * finds arbitrary template in the grayscale image using Generalized Hough Transform
 * <p>
 * Detects position, translation and rotation CITE: Guil1999 .
 */
public class GeneralizedHoughGuil extends GeneralizedHough {

    protected GeneralizedHoughGuil(long addr) {
        super(addr);
    }

    // internal usage only
    public static GeneralizedHoughGuil __fromPtr__(long addr) {
        return new GeneralizedHoughGuil(addr);
    }

    //
    // C++:  double cv::GeneralizedHoughGuil::getAngleEpsilon()
    //

    // C++:  double cv::GeneralizedHoughGuil::getAngleEpsilon()
    private static native double getAngleEpsilon_0(long nativeObj);


    //
    // C++:  double cv::GeneralizedHoughGuil::getAngleStep()
    //

    // C++:  double cv::GeneralizedHoughGuil::getAngleStep()
    private static native double getAngleStep_0(long nativeObj);


    //
    // C++:  double cv::GeneralizedHoughGuil::getMaxAngle()
    //

    // C++:  double cv::GeneralizedHoughGuil::getMaxAngle()
    private static native double getMaxAngle_0(long nativeObj);


    //
    // C++:  double cv::GeneralizedHoughGuil::getMaxScale()
    //

    // C++:  double cv::GeneralizedHoughGuil::getMaxScale()
    private static native double getMaxScale_0(long nativeObj);


    //
    // C++:  double cv::GeneralizedHoughGuil::getMinAngle()
    //

    // C++:  double cv::GeneralizedHoughGuil::getMinAngle()
    private static native double getMinAngle_0(long nativeObj);


    //
    // C++:  double cv::GeneralizedHoughGuil::getMinScale()
    //

    // C++:  double cv::GeneralizedHoughGuil::getMinScale()
    private static native double getMinScale_0(long nativeObj);


    //
    // C++:  double cv::GeneralizedHoughGuil::getScaleStep()
    //

    // C++:  double cv::GeneralizedHoughGuil::getScaleStep()
    private static native double getScaleStep_0(long nativeObj);


    //
    // C++:  double cv::GeneralizedHoughGuil::getXi()
    //

    // C++:  double cv::GeneralizedHoughGuil::getXi()
    private static native double getXi_0(long nativeObj);


    //
    // C++:  int cv::GeneralizedHoughGuil::getAngleThresh()
    //

    // C++:  int cv::GeneralizedHoughGuil::getAngleThresh()
    private static native int getAngleThresh_0(long nativeObj);


    //
    // C++:  int cv::GeneralizedHoughGuil::getLevels()
    //

    // C++:  int cv::GeneralizedHoughGuil::getLevels()
    private static native int getLevels_0(long nativeObj);


    //
    // C++:  int cv::GeneralizedHoughGuil::getPosThresh()
    //

    // C++:  int cv::GeneralizedHoughGuil::getPosThresh()
    private static native int getPosThresh_0(long nativeObj);


    //
    // C++:  int cv::GeneralizedHoughGuil::getScaleThresh()
    //

    // C++:  int cv::GeneralizedHoughGuil::getScaleThresh()
    private static native int getScaleThresh_0(long nativeObj);


    //
    // C++:  void cv::GeneralizedHoughGuil::setAngleEpsilon(double angleEpsilon)
    //

    // C++:  void cv::GeneralizedHoughGuil::setAngleEpsilon(double angleEpsilon)
    private static native void setAngleEpsilon_0(long nativeObj, double angleEpsilon);


    //
    // C++:  void cv::GeneralizedHoughGuil::setAngleStep(double angleStep)
    //

    // C++:  void cv::GeneralizedHoughGuil::setAngleStep(double angleStep)
    private static native void setAngleStep_0(long nativeObj, double angleStep);


    //
    // C++:  void cv::GeneralizedHoughGuil::setAngleThresh(int angleThresh)
    //

    // C++:  void cv::GeneralizedHoughGuil::setAngleThresh(int angleThresh)
    private static native void setAngleThresh_0(long nativeObj, int angleThresh);


    //
    // C++:  void cv::GeneralizedHoughGuil::setLevels(int levels)
    //

    // C++:  void cv::GeneralizedHoughGuil::setLevels(int levels)
    private static native void setLevels_0(long nativeObj, int levels);


    //
    // C++:  void cv::GeneralizedHoughGuil::setMaxAngle(double maxAngle)
    //

    // C++:  void cv::GeneralizedHoughGuil::setMaxAngle(double maxAngle)
    private static native void setMaxAngle_0(long nativeObj, double maxAngle);


    //
    // C++:  void cv::GeneralizedHoughGuil::setMaxScale(double maxScale)
    //

    // C++:  void cv::GeneralizedHoughGuil::setMaxScale(double maxScale)
    private static native void setMaxScale_0(long nativeObj, double maxScale);


    //
    // C++:  void cv::GeneralizedHoughGuil::setMinAngle(double minAngle)
    //

    // C++:  void cv::GeneralizedHoughGuil::setMinAngle(double minAngle)
    private static native void setMinAngle_0(long nativeObj, double minAngle);


    //
    // C++:  void cv::GeneralizedHoughGuil::setMinScale(double minScale)
    //

    // C++:  void cv::GeneralizedHoughGuil::setMinScale(double minScale)
    private static native void setMinScale_0(long nativeObj, double minScale);


    //
    // C++:  void cv::GeneralizedHoughGuil::setPosThresh(int posThresh)
    //

    // C++:  void cv::GeneralizedHoughGuil::setPosThresh(int posThresh)
    private static native void setPosThresh_0(long nativeObj, int posThresh);


    //
    // C++:  void cv::GeneralizedHoughGuil::setScaleStep(double scaleStep)
    //

    // C++:  void cv::GeneralizedHoughGuil::setScaleStep(double scaleStep)
    private static native void setScaleStep_0(long nativeObj, double scaleStep);


    //
    // C++:  void cv::GeneralizedHoughGuil::setScaleThresh(int scaleThresh)
    //

    // C++:  void cv::GeneralizedHoughGuil::setScaleThresh(int scaleThresh)
    private static native void setScaleThresh_0(long nativeObj, int scaleThresh);


    //
    // C++:  void cv::GeneralizedHoughGuil::setXi(double xi)
    //

    // C++:  void cv::GeneralizedHoughGuil::setXi(double xi)
    private static native void setXi_0(long nativeObj, double xi);

    // native support for java finalize()
    private static native void delete(long nativeObj);

    public double getAngleEpsilon() {
        return getAngleEpsilon_0(nativeObj);
    }

    public void setAngleEpsilon(double angleEpsilon) {
        setAngleEpsilon_0(nativeObj, angleEpsilon);
    }

    public double getAngleStep() {
        return getAngleStep_0(nativeObj);
    }

    public void setAngleStep(double angleStep) {
        setAngleStep_0(nativeObj, angleStep);
    }

    public double getMaxAngle() {
        return getMaxAngle_0(nativeObj);
    }

    public void setMaxAngle(double maxAngle) {
        setMaxAngle_0(nativeObj, maxAngle);
    }

    public double getMaxScale() {
        return getMaxScale_0(nativeObj);
    }

    public void setMaxScale(double maxScale) {
        setMaxScale_0(nativeObj, maxScale);
    }

    public double getMinAngle() {
        return getMinAngle_0(nativeObj);
    }

    public void setMinAngle(double minAngle) {
        setMinAngle_0(nativeObj, minAngle);
    }

    public double getMinScale() {
        return getMinScale_0(nativeObj);
    }

    public void setMinScale(double minScale) {
        setMinScale_0(nativeObj, minScale);
    }

    public double getScaleStep() {
        return getScaleStep_0(nativeObj);
    }

    public void setScaleStep(double scaleStep) {
        setScaleStep_0(nativeObj, scaleStep);
    }

    public double getXi() {
        return getXi_0(nativeObj);
    }

    public void setXi(double xi) {
        setXi_0(nativeObj, xi);
    }

    public int getAngleThresh() {
        return getAngleThresh_0(nativeObj);
    }

    public void setAngleThresh(int angleThresh) {
        setAngleThresh_0(nativeObj, angleThresh);
    }

    public int getLevels() {
        return getLevels_0(nativeObj);
    }

    public void setLevels(int levels) {
        setLevels_0(nativeObj, levels);
    }

    public int getPosThresh() {
        return getPosThresh_0(nativeObj);
    }

    public void setPosThresh(int posThresh) {
        setPosThresh_0(nativeObj, posThresh);
    }

    public int getScaleThresh() {
        return getScaleThresh_0(nativeObj);
    }

    public void setScaleThresh(int scaleThresh) {
        setScaleThresh_0(nativeObj, scaleThresh);
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
