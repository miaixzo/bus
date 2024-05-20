//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.imgproc;

// C++: class GeneralizedHoughBallard

/**
 * finds arbitrary template in the grayscale image using Generalized Hough Transform
 * <p>
 * Detects position only without translation and rotation CITE: Ballard1981 .
 */
public class GeneralizedHoughBallard extends GeneralizedHough {

    protected GeneralizedHoughBallard(long addr) {
        super(addr);
    }

    // internal usage only
    public static GeneralizedHoughBallard __fromPtr__(long addr) {
        return new GeneralizedHoughBallard(addr);
    }

    //
    // C++:  void cv::GeneralizedHoughBallard::setLevels(int levels)
    //

    // C++:  void cv::GeneralizedHoughBallard::setLevels(int levels)
    private static native void setLevels_0(long nativeObj, int levels);


    //
    // C++:  int cv::GeneralizedHoughBallard::getLevels()
    //

    // C++:  int cv::GeneralizedHoughBallard::getLevels()
    private static native int getLevels_0(long nativeObj);


    //
    // C++:  void cv::GeneralizedHoughBallard::setVotesThreshold(int votesThreshold)
    //

    // C++:  void cv::GeneralizedHoughBallard::setVotesThreshold(int votesThreshold)
    private static native void setVotesThreshold_0(long nativeObj, int votesThreshold);


    //
    // C++:  int cv::GeneralizedHoughBallard::getVotesThreshold()
    //

    // C++:  int cv::GeneralizedHoughBallard::getVotesThreshold()
    private static native int getVotesThreshold_0(long nativeObj);

    // native support for deleting native object
    private static native void delete(long nativeObj);

    public int getLevels() {
        return getLevels_0(nativeObj);
    }

    public void setLevels(int levels) {
        setLevels_0(nativeObj, levels);
    }

    public int getVotesThreshold() {
        return getVotesThreshold_0(nativeObj);
    }

    public void setVotesThreshold(int votesThreshold) {
        setVotesThreshold_0(nativeObj, votesThreshold);
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
