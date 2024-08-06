/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org opencv.org and other contributors.         ~
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
package org.opencv.imgproc;

// C++: class GeneralizedHoughBallard
/**
 * finds arbitrary template in the grayscale image using Generalized Hough Transform
 *
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
    // C++: void cv::GeneralizedHoughBallard::setLevels(int levels)
    //

    // C++: void cv::GeneralizedHoughBallard::setLevels(int levels)
    private static native void setLevels_0(long nativeObj, int levels);

    //
    // C++: int cv::GeneralizedHoughBallard::getLevels()
    //

    // C++: int cv::GeneralizedHoughBallard::getLevels()
    private static native int getLevels_0(long nativeObj);

    //
    // C++: void cv::GeneralizedHoughBallard::setVotesThreshold(int votesThreshold)
    //

    // C++: void cv::GeneralizedHoughBallard::setVotesThreshold(int votesThreshold)
    private static native void setVotesThreshold_0(long nativeObj, int votesThreshold);

    //
    // C++: int cv::GeneralizedHoughBallard::getVotesThreshold()
    //

    // C++: int cv::GeneralizedHoughBallard::getVotesThreshold()
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

}
