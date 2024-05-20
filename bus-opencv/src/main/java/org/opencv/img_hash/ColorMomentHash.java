//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.img_hash;

// C++: class ColorMomentHash

/**
 * Image hash based on color moments.
 * <p>
 * See CITE: tang2012perceptual for details.
 */
public class ColorMomentHash extends ImgHashBase {

    protected ColorMomentHash(long addr) {
        super(addr);
    }

    // internal usage only
    public static ColorMomentHash __fromPtr__(long addr) {
        return new ColorMomentHash(addr);
    }

    //
    // C++: static Ptr_ColorMomentHash cv::img_hash::ColorMomentHash::create()
    //

    public static ColorMomentHash create() {
        return ColorMomentHash.__fromPtr__(create_0());
    }

    // C++: static Ptr_ColorMomentHash cv::img_hash::ColorMomentHash::create()
    private static native long create_0();

    // native support for deleting native object
    private static native void delete(long nativeObj);

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
