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

import org.opencv.core.Algorithm;
import org.opencv.core.Mat;
import org.opencv.core.Size;

// C++: class CLAHE
/**
 * Base class for Contrast Limited Adaptive Histogram Equalization.
 */
public class CLAHE extends Algorithm {

    protected CLAHE(long addr) {
        super(addr);
    }

    // internal usage only
    public static CLAHE __fromPtr__(long addr) {
        return new CLAHE(addr);
    }

    //
    // C++: void cv::CLAHE::apply(Mat src, Mat& dst)
    //

    /**
     * Equalizes the histogram of a grayscale image using Contrast Limited Adaptive Histogram Equalization.
     *
     * @param src Source image of type CV_8UC1 or CV_16UC1.
     * @param dst Destination image.
     */
    public void apply(Mat src, Mat dst) {
        apply_0(nativeObj, src.nativeObj, dst.nativeObj);
    }

    //
    // C++: void cv::CLAHE::setClipLimit(double clipLimit)
    //

    /**
     * Sets threshold for contrast limiting.
     *
     * @param clipLimit threshold value.
     */
    public void setClipLimit(double clipLimit) {
        setClipLimit_0(nativeObj, clipLimit);
    }

    //
    // C++: double cv::CLAHE::getClipLimit()
    //

    public double getClipLimit() {
        return getClipLimit_0(nativeObj);
    }

    //
    // C++: void cv::CLAHE::setTilesGridSize(Size tileGridSize)
    //

    /**
     * Sets size of grid for histogram equalization. Input image will be divided into equally sized rectangular tiles.
     *
     * @param tileGridSize defines the number of tiles in row and column.
     */
    public void setTilesGridSize(Size tileGridSize) {
        setTilesGridSize_0(nativeObj, tileGridSize.width, tileGridSize.height);
    }

    //
    // C++: Size cv::CLAHE::getTilesGridSize()
    //

    public Size getTilesGridSize() {
        return new Size(getTilesGridSize_0(nativeObj));
    }

    //
    // C++: void cv::CLAHE::collectGarbage()
    //

    public void collectGarbage() {
        collectGarbage_0(nativeObj);
    }

    // C++: void cv::CLAHE::apply(Mat src, Mat& dst)
    private static native void apply_0(long nativeObj, long src_nativeObj, long dst_nativeObj);

    // C++: void cv::CLAHE::setClipLimit(double clipLimit)
    private static native void setClipLimit_0(long nativeObj, double clipLimit);

    // C++: double cv::CLAHE::getClipLimit()
    private static native double getClipLimit_0(long nativeObj);

    // C++: void cv::CLAHE::setTilesGridSize(Size tileGridSize)
    private static native void setTilesGridSize_0(long nativeObj, double tileGridSize_width,
            double tileGridSize_height);

    // C++: Size cv::CLAHE::getTilesGridSize()
    private static native double[] getTilesGridSize_0(long nativeObj);

    // C++: void cv::CLAHE::collectGarbage()
    private static native void collectGarbage_0(long nativeObj);

    // native support for deleting native object
    private static native void delete(long nativeObj);

}
