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
package org.opencv.img_hash;

// C++: class RadialVarianceHash
/**
 * Image hash based on Radon transform.
 *
 * See CITE: tang2012perceptual for details.
 */
public class RadialVarianceHash extends ImgHashBase {

    protected RadialVarianceHash(long addr) {
        super(addr);
    }

    // internal usage only
    public static RadialVarianceHash __fromPtr__(long addr) {
        return new RadialVarianceHash(addr);
    }

    //
    // C++: static Ptr_RadialVarianceHash cv::img_hash::RadialVarianceHash::create(double sigma = 1, int numOfAngleLine
    // = 180)
    //

    public static RadialVarianceHash create(double sigma, int numOfAngleLine) {
        return RadialVarianceHash.__fromPtr__(create_0(sigma, numOfAngleLine));
    }

    public static RadialVarianceHash create(double sigma) {
        return RadialVarianceHash.__fromPtr__(create_1(sigma));
    }

    public static RadialVarianceHash create() {
        return RadialVarianceHash.__fromPtr__(create_2());
    }

    //
    // C++: int cv::img_hash::RadialVarianceHash::getNumOfAngleLine()
    //

    // C++: static Ptr_RadialVarianceHash cv::img_hash::RadialVarianceHash::create(double sigma = 1, int numOfAngleLine
    // = 180)
    private static native long create_0(double sigma, int numOfAngleLine);

    //
    // C++: double cv::img_hash::RadialVarianceHash::getSigma()
    //

    private static native long create_1(double sigma);

    //
    // C++: void cv::img_hash::RadialVarianceHash::setNumOfAngleLine(int value)
    //

    private static native long create_2();

    //
    // C++: void cv::img_hash::RadialVarianceHash::setSigma(double value)
    //

    // C++: int cv::img_hash::RadialVarianceHash::getNumOfAngleLine()
    private static native int getNumOfAngleLine_0(long nativeObj);

    // C++: double cv::img_hash::RadialVarianceHash::getSigma()
    private static native double getSigma_0(long nativeObj);

    // C++: void cv::img_hash::RadialVarianceHash::setNumOfAngleLine(int value)
    private static native void setNumOfAngleLine_0(long nativeObj, int value);

    // C++: void cv::img_hash::RadialVarianceHash::setSigma(double value)
    private static native void setSigma_0(long nativeObj, double value);

    // native support for deleting native object
    private static native void delete(long nativeObj);

    public int getNumOfAngleLine() {
        return getNumOfAngleLine_0(nativeObj);
    }

    public void setNumOfAngleLine(int value) {
        setNumOfAngleLine_0(nativeObj, value);
    }

    public double getSigma() {
        return getSigma_0(nativeObj);
    }

    public void setSigma(double value) {
        setSigma_0(nativeObj, value);
    }

}
