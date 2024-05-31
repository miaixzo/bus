//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.img_hash;

// C++: class AverageHash
/**
 * Computes average hash value of the input image
 *
 * This is a fast image hashing algorithm, but only work on simple case. For more details, please
 * refer to CITE: lookslikeit
 */
public class AverageHash extends ImgHashBase {

    protected AverageHash(long addr) { super(addr); }

    // internal usage only
    public static AverageHash __fromPtr__(long addr) { return new AverageHash(addr); }

    //
    // C++: static Ptr_AverageHash cv::img_hash::AverageHash::create()
    //

    public static AverageHash create() {
        return AverageHash.__fromPtr__(create_0());
    }




    // C++: static Ptr_AverageHash cv::img_hash::AverageHash::create()
    private static native long create_0();

    // native support for deleting native object
    private static native void delete(long nativeObj);

}
