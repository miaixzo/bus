//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.img_hash;

// C++: class PHash

/**
 * pHash
 * <p>
 * Slower than average_hash, but tolerant of minor modifications
 * <p>
 * This algorithm can combat more variation than averageHash, for more details please refer to CITE: lookslikeit
 */
public class PHash extends ImgHashBase {

    protected PHash(long addr) {
        super(addr);
    }

    // internal usage only
    public static PHash __fromPtr__(long addr) {
        return new PHash(addr);
    }

    //
    // C++: static Ptr_PHash cv::img_hash::PHash::create()
    //

    public static PHash create() {
        return PHash.__fromPtr__(create_0());
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }


    // C++: static Ptr_PHash cv::img_hash::PHash::create()
    private static native long create_0();

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
