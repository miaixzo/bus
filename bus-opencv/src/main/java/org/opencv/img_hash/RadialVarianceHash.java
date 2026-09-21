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

// C++: class RadialVarianceHash

/**
 * Image hash based on Radon transform.
 * <p>
 * See CITE: tang2012perceptual for details.
 */
public class RadialVarianceHash extends ImgHashBase {

    /**
     * Creates a new {@code RadialVarianceHash} instance.
     *
     * @param addr the {@code addr} value
     */
    protected RadialVarianceHash(long addr) {
        super(addr);
    }

    // internal usage only
    /**
     * Performs the {@code __fromPtr__} operation.
     *
     * @param addr the {@code addr} value
     * @return the operation result
     */
    public static RadialVarianceHash __fromPtr__(long addr) {
        return new RadialVarianceHash(addr);
    }

    //
    // C++: static Ptr_RadialVarianceHash cv::img_hash::RadialVarianceHash::create(double sigma = 1, int numOfAngleLine
    // = 180)
    //

    /**
     * Performs the {@code create} operation.
     *
     * @param sigma the {@code sigma} value
     * @param numOfAngleLine the {@code numOfAngleLine} value
     * @return the operation result
     */
    public static RadialVarianceHash create(double sigma, int numOfAngleLine) {
        return RadialVarianceHash.__fromPtr__(create_0(sigma, numOfAngleLine));
    }

    /**
     * Performs the {@code create} operation.
     *
     * @param sigma the {@code sigma} value
     * @return the operation result
     */
    public static RadialVarianceHash create(double sigma) {
        return RadialVarianceHash.__fromPtr__(create_1(sigma));
    }

    /**
     * Performs the {@code create} operation.
     *
     * @return the operation result
     */
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

    // native support for java finalize() or cleaner
    private static native void delete(long nativeObj);

    /**
     * Performs the {@code getNumOfAngleLine} operation.
     *
     * @return the operation result
     */
    public int getNumOfAngleLine() {
        return getNumOfAngleLine_0(nativeObj);
    }

    /**
     * Performs the {@code setNumOfAngleLine} operation.
     *
     * @param value the {@code value} value
     */
    public void setNumOfAngleLine(int value) {
        setNumOfAngleLine_0(nativeObj, value);
    }

    /**
     * Performs the {@code getSigma} operation.
     *
     * @return the operation result
     */
    public double getSigma() {
        return getSigma_0(nativeObj);
    }

    /**
     * Performs the {@code setSigma} operation.
     *
     * @param value the {@code value} value
     */
    public void setSigma(double value) {
        setSigma_0(nativeObj, value);
    }

}
