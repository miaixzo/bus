//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.img_hash;

import org.opencv.core.MatOfDouble;

// C++: class BlockMeanHash
/**
 * Image hash based on block mean.
 *
 * See CITE: zauner2010implementation for details.
 */
public class BlockMeanHash extends ImgHashBase {

    protected BlockMeanHash(long addr) { super(addr); }

    // internal usage only
    public static BlockMeanHash __fromPtr__(long addr) { return new BlockMeanHash(addr); }

    //
    // C++:  void cv::img_hash::BlockMeanHash::setMode(int mode)
    //

    /**
     * Create BlockMeanHash object
     *         @param mode the mode
     */
    public void setMode(int mode) {
        setMode_0(nativeObj, mode);
    }


    //
    // C++:  vector_double cv::img_hash::BlockMeanHash::getMean()
    //

    public MatOfDouble getMean() {
        return MatOfDouble.fromNativeAddr(getMean_0(nativeObj));
    }


    //
    // C++: static Ptr_BlockMeanHash cv::img_hash::BlockMeanHash::create(int mode = BLOCK_MEAN_HASH_MODE_0)
    //

    public static BlockMeanHash create(int mode) {
        return BlockMeanHash.__fromPtr__(create_0(mode));
    }

    public static BlockMeanHash create() {
        return BlockMeanHash.__fromPtr__(create_1());
    }




    // C++:  void cv::img_hash::BlockMeanHash::setMode(int mode)
    private static native void setMode_0(long nativeObj, int mode);

    // C++:  vector_double cv::img_hash::BlockMeanHash::getMean()
    private static native long getMean_0(long nativeObj);

    // C++: static Ptr_BlockMeanHash cv::img_hash::BlockMeanHash::create(int mode = BLOCK_MEAN_HASH_MODE_0)
    private static native long create_0(int mode);
    private static native long create_1();

    // native support for deleting native object
    private static native void delete(long nativeObj);

}
