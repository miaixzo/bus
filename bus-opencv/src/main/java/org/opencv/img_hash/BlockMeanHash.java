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

import org.opencv.core.MatOfDouble;

// C++: class BlockMeanHash
/**
 * Image hash based on block mean.
 *
 * See CITE: zauner2010implementation for details.
 */
public class BlockMeanHash extends ImgHashBase {

    protected BlockMeanHash(long addr) {
        super(addr);
    }

    // internal usage only
    public static BlockMeanHash __fromPtr__(long addr) {
        return new BlockMeanHash(addr);
    }

    //
    // C++: void cv::img_hash::BlockMeanHash::setMode(int mode)
    //

    public static BlockMeanHash create(int mode) {
        return BlockMeanHash.__fromPtr__(create_0(mode));
    }

    //
    // C++: vector_double cv::img_hash::BlockMeanHash::getMean()
    //

    public static BlockMeanHash create() {
        return BlockMeanHash.__fromPtr__(create_1());
    }

    //
    // C++: static Ptr_BlockMeanHash cv::img_hash::BlockMeanHash::create(int mode = BLOCK_MEAN_HASH_MODE_0)
    //

    // C++: void cv::img_hash::BlockMeanHash::setMode(int mode)
    private static native void setMode_0(long nativeObj, int mode);

    // C++: vector_double cv::img_hash::BlockMeanHash::getMean()
    private static native long getMean_0(long nativeObj);

    // C++: static Ptr_BlockMeanHash cv::img_hash::BlockMeanHash::create(int mode = BLOCK_MEAN_HASH_MODE_0)
    private static native long create_0(int mode);

    private static native long create_1();

    // native support for deleting native object
    private static native void delete(long nativeObj);

    /**
     * Create BlockMeanHash object
     *
     * @param mode the mode
     */
    public void setMode(int mode) {
        setMode_0(nativeObj, mode);
    }

    public MatOfDouble getMean() {
        return MatOfDouble.fromNativeAddr(getMean_0(nativeObj));
    }

}
