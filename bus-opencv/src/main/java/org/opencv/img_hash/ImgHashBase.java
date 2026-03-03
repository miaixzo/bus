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
