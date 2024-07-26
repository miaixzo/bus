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

// C++: class MarrHildrethHash
/**
 * Marr-Hildreth Operator Based Hash, slowest but more discriminative.
 *
 * See CITE: zauner2010implementation for details.
 */
public class MarrHildrethHash extends ImgHashBase {

    protected MarrHildrethHash(long addr) {
        super(addr);
    }

    // internal usage only
    public static MarrHildrethHash __fromPtr__(long addr) {
        return new MarrHildrethHash(addr);
    }

    //
    // C++: float cv::img_hash::MarrHildrethHash::getAlpha()
    //

    /**
     * self explain
     * 
     * @return automatically generated
     */
    public float getAlpha() {
        return getAlpha_0(nativeObj);
    }

    //
    // C++: float cv::img_hash::MarrHildrethHash::getScale()
    //

    /**
     * self explain
     * 
     * @return automatically generated
     */
    public float getScale() {
        return getScale_0(nativeObj);
    }

    //
    // C++: void cv::img_hash::MarrHildrethHash::setKernelParam(float alpha, float scale)
    //

    /**
     * Set Mh kernel parameters
     * 
     * @param alpha int scale factor for marr wavelet (default=2).
     * @param scale int level of scale factor (default = 1)
     */
    public void setKernelParam(float alpha, float scale) {
        setKernelParam_0(nativeObj, alpha, scale);
    }

    //
    // C++: static Ptr_MarrHildrethHash cv::img_hash::MarrHildrethHash::create(float alpha = 2.0f, float scale = 1.0f)
    //

    /**
     * @param alpha int scale factor for marr wavelet (default=2).
     * @param scale int level of scale factor (default = 1)
     * @return automatically generated
     */
    public static MarrHildrethHash create(float alpha, float scale) {
        return MarrHildrethHash.__fromPtr__(create_0(alpha, scale));
    }

    /**
     * @param alpha int scale factor for marr wavelet (default=2).
     * @return automatically generated
     */
    public static MarrHildrethHash create(float alpha) {
        return MarrHildrethHash.__fromPtr__(create_1(alpha));
    }

    /**
     * @return automatically generated
     */
    public static MarrHildrethHash create() {
        return MarrHildrethHash.__fromPtr__(create_2());
    }

    // C++: float cv::img_hash::MarrHildrethHash::getAlpha()
    private static native float getAlpha_0(long nativeObj);

    // C++: float cv::img_hash::MarrHildrethHash::getScale()
    private static native float getScale_0(long nativeObj);

    // C++: void cv::img_hash::MarrHildrethHash::setKernelParam(float alpha, float scale)
    private static native void setKernelParam_0(long nativeObj, float alpha, float scale);

    // C++: static Ptr_MarrHildrethHash cv::img_hash::MarrHildrethHash::create(float alpha = 2.0f, float scale = 1.0f)
    private static native long create_0(float alpha, float scale);

    private static native long create_1(float alpha);

    private static native long create_2();

    // native support for deleting native object
    private static native void delete(long nativeObj);

}
