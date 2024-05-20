//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.img_hash;

// C++: class MarrHildrethHash

/**
 * Marr-Hildreth Operator Based Hash, slowest but more discriminative.
 * <p>
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
    // C++:  float cv::img_hash::MarrHildrethHash::getAlpha()
    //

    /**
     * @param alpha int scale factor for marr wavelet (default=2).
     * @param scale int level of scale factor (default = 1)
     * @return automatically generated
     */
    public static MarrHildrethHash create(float alpha, float scale) {
        return MarrHildrethHash.__fromPtr__(create_0(alpha, scale));
    }


    //
    // C++:  float cv::img_hash::MarrHildrethHash::getScale()
    //

    /**
     * @param alpha int scale factor for marr wavelet (default=2).
     * @return automatically generated
     */
    public static MarrHildrethHash create(float alpha) {
        return MarrHildrethHash.__fromPtr__(create_1(alpha));
    }


    //
    // C++:  void cv::img_hash::MarrHildrethHash::setKernelParam(float alpha, float scale)
    //

    /**
     * @return automatically generated
     */
    public static MarrHildrethHash create() {
        return MarrHildrethHash.__fromPtr__(create_2());
    }


    //
    // C++: static Ptr_MarrHildrethHash cv::img_hash::MarrHildrethHash::create(float alpha = 2.0f, float scale = 1.0f)
    //

    // C++:  float cv::img_hash::MarrHildrethHash::getAlpha()
    private static native float getAlpha_0(long nativeObj);

    // C++:  float cv::img_hash::MarrHildrethHash::getScale()
    private static native float getScale_0(long nativeObj);

    // C++:  void cv::img_hash::MarrHildrethHash::setKernelParam(float alpha, float scale)
    private static native void setKernelParam_0(long nativeObj, float alpha, float scale);

    // C++: static Ptr_MarrHildrethHash cv::img_hash::MarrHildrethHash::create(float alpha = 2.0f, float scale = 1.0f)
    private static native long create_0(float alpha, float scale);

    private static native long create_1(float alpha);

    private static native long create_2();

    // native support for deleting native object
    private static native void delete(long nativeObj);

    /**
     * self explain
     *
     * @return automatically generated
     */
    public float getAlpha() {
        return getAlpha_0(nativeObj);
    }

    /**
     * self explain
     *
     * @return automatically generated
     */
    public float getScale() {
        return getScale_0(nativeObj);
    }

    /**
     * Set Mh kernel parameters
     *
     * @param alpha int scale factor for marr wavelet (default=2).
     * @param scale int level of scale factor (default = 1)
     */
    public void setKernelParam(float alpha, float scale) {
        setKernelParam_0(nativeObj, alpha, scale);
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
