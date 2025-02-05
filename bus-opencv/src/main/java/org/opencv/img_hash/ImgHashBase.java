/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org opencv.org and other contributors.         ~
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
package org.opencv.img_hash;

import org.opencv.core.Algorithm;
import org.opencv.core.Mat;

// C++: class ImgHashBase
/**
 * The base class for image hash algorithms
 */
public class ImgHashBase extends Algorithm {

    protected ImgHashBase(long addr) {
        super(addr);
    }

    // internal usage only
    public static ImgHashBase __fromPtr__(long addr) {
        return new ImgHashBase(addr);
    }

    //
    // C++: void cv::img_hash::ImgHashBase::compute(Mat inputArr, Mat& outputArr)
    //

    // C++: void cv::img_hash::ImgHashBase::compute(Mat inputArr, Mat& outputArr)
    private static native void compute_0(long nativeObj, long inputArr_nativeObj, long outputArr_nativeObj);

    //
    // C++: double cv::img_hash::ImgHashBase::compare(Mat hashOne, Mat hashTwo)
    //

    // C++: double cv::img_hash::ImgHashBase::compare(Mat hashOne, Mat hashTwo)
    private static native double compare_0(long nativeObj, long hashOne_nativeObj, long hashTwo_nativeObj);

    // native support for deleting native object
    private static native void delete(long nativeObj);

    /**
     * Computes hash of the input image
     *
     * @param inputArr  input image want to compute hash value
     * @param outputArr hash of the image
     */
    public void compute(Mat inputArr, Mat outputArr) {
        compute_0(nativeObj, inputArr.nativeObj, outputArr.nativeObj);
    }

    /**
     * Compare the hash value between inOne and inTwo
     *
     * @param hashOne Hash value one
     * @param hashTwo Hash value two
     * @return value indicate similarity between inOne and inTwo, the meaning of the value vary from algorithms to
     *         algorithms
     */
    public double compare(Mat hashOne, Mat hashTwo) {
        return compare_0(nativeObj, hashOne.nativeObj, hashTwo.nativeObj);
    }

}
