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
package org.miaixz.bus.opencv.img_hash;

import org.miaixz.bus.opencv.core.Mat;

/**
 * Factory methods and utility functions for perceptual image hashing.
 */
public class Img_hash {

    /**
     * Creates a Java API container. The utility operations are available through static methods.
     */
    public Img_hash() {
        // No initialization required.
    }

    /**
     * Compute a block-mean image hash using the non-overlapping block mode.
     */
    public static final int BLOCK_MEAN_HASH_MODE_0 = 0;

    /**
     * Compute a block-mean image hash using overlapping blocks.
     */
    public static final int BLOCK_MEAN_HASH_MODE_1 = 1;

    /**
     * Calculates img_hash::AverageHash in one call.
     *
     * @param inputArr  input image want to compute hash value, type should be CV_8UC4, CV_8UC3 or CV_8UC1
     * @param outputArr Hash value of input, it will contain 16 hex decimal number, return type is CV_8U
     * @throws NullPointerException if inputArr, outputArr is null
     */
    public static void averageHash(Mat inputArr, Mat outputArr) {
        averageHash_0(inputArr.nativeObj, outputArr.nativeObj);
    }

    /**
     * Computes color moment hash of the input, the algorithm is come from the paper "Perceptual Hashing for Color
     * Images Using Invariant Moments".
     *
     * @param inputArr  input image want to compute hash value, type should be CV_8UC4, CV_8UC3 or CV_8UC1
     * @param outputArr 42 hash values with type CV_64F(double)
     * @throws NullPointerException if inputArr, outputArr is null
     */
    public static void colorMomentHash(Mat inputArr, Mat outputArr) {
        colorMomentHash_0(inputArr.nativeObj, outputArr.nativeObj);
    }

    /**
     * Computes radial variance hash of the input image.
     *
     * @param inputArr       input image want to compute hash value, type should be CV_8UC4, CV_8UC3, CV_8UC1
     * @param outputArr      Hash value of input
     * @param sigma          Gaussian kernel standard deviation
     * @param numOfAngleLine The number of angles to consider
     * @throws NullPointerException if inputArr, outputArr is null
     */
    public static void radialVarianceHash(Mat inputArr, Mat outputArr, double sigma, int numOfAngleLine) {
        radialVarianceHash_0(inputArr.nativeObj, outputArr.nativeObj, sigma, numOfAngleLine);
    }

    /**
     * Computes radial variance hash of the input image.
     *
     * @param inputArr  input image want to compute hash value, type should be CV_8UC4, CV_8UC3, CV_8UC1
     * @param outputArr Hash value of input
     * @param sigma     Gaussian kernel standard deviation
     * @throws NullPointerException if inputArr, outputArr is null
     */
    public static void radialVarianceHash(Mat inputArr, Mat outputArr, double sigma) {
        radialVarianceHash_1(inputArr.nativeObj, outputArr.nativeObj, sigma);
    }

    /**
     * Computes radial variance hash of the input image.
     *
     * @param inputArr  input image want to compute hash value, type should be CV_8UC4, CV_8UC3, CV_8UC1
     * @param outputArr Hash value of input
     * @throws NullPointerException if inputArr, outputArr is null
     */
    public static void radialVarianceHash(Mat inputArr, Mat outputArr) {
        radialVarianceHash_2(inputArr.nativeObj, outputArr.nativeObj);
    }

    /**
     * Computes pHash value of the input image.
     *
     * @param inputArr  input image want to compute hash value, type should be CV_8UC4, CV_8UC3, CV_8UC1
     * @param outputArr Hash value of input, it will contain 8 uchar value
     * @throws NullPointerException if inputArr, outputArr is null
     */
    public static void pHash(Mat inputArr, Mat outputArr) {
        pHash_0(inputArr.nativeObj, outputArr.nativeObj);
    }

    /**
     * Computes average hash value of the input image.
     *
     * @param inputArr  input image want to compute hash value, type should be CV_8UC4, CV_8UC3, CV_8UC1
     * @param outputArr Hash value of input, it will contain 16 hex decimal number, return type is CV_8U
     * @param alpha     int scale factor for marr wavelet (default=2)
     * @param scale     int level of scale factor (default = 1)
     * @throws NullPointerException if inputArr, outputArr is null
     */
    public static void marrHildrethHash(Mat inputArr, Mat outputArr, float alpha, float scale) {
        marrHildrethHash_0(inputArr.nativeObj, outputArr.nativeObj, alpha, scale);
    }

    /**
     * Computes average hash value of the input image.
     *
     * @param inputArr  input image want to compute hash value, type should be CV_8UC4, CV_8UC3, CV_8UC1
     * @param outputArr Hash value of input, it will contain 16 hex decimal number, return type is CV_8U
     * @param alpha     int scale factor for marr wavelet (default=2)
     * @throws NullPointerException if inputArr, outputArr is null
     */
    public static void marrHildrethHash(Mat inputArr, Mat outputArr, float alpha) {
        marrHildrethHash_1(inputArr.nativeObj, outputArr.nativeObj, alpha);
    }

    /**
     * Computes average hash value of the input image.
     *
     * @param inputArr  input image want to compute hash value, type should be CV_8UC4, CV_8UC3, CV_8UC1
     * @param outputArr Hash value of input, it will contain 16 hex decimal number, return type is CV_8U
     * @throws NullPointerException if inputArr, outputArr is null
     */
    public static void marrHildrethHash(Mat inputArr, Mat outputArr) {
        marrHildrethHash_2(inputArr.nativeObj, outputArr.nativeObj);
    }

    /**
     * Computes block mean hash of the input image.
     *
     * @param inputArr  input image want to compute hash value, type should be CV_8UC4, CV_8UC3 or CV_8UC1
     * @param outputArr Hash value of input, it will contain 16 hex decimal number, return type is CV_8U
     * @param mode      block-mean hashing mode, selected by the BLOCK_MEAN_HASH_MODE constants
     * @throws NullPointerException if inputArr, outputArr is null
     */
    public static void blockMeanHash(Mat inputArr, Mat outputArr, int mode) {
        blockMeanHash_0(inputArr.nativeObj, outputArr.nativeObj, mode);
    }

    /**
     * Computes block mean hash of the input image.
     *
     * @param inputArr  input image want to compute hash value, type should be CV_8UC4, CV_8UC3 or CV_8UC1
     * @param outputArr Hash value of input, it will contain 16 hex decimal number, return type is CV_8U
     * @throws NullPointerException if inputArr, outputArr is null
     */
    public static void blockMeanHash(Mat inputArr, Mat outputArr) {
        blockMeanHash_1(inputArr.nativeObj, outputArr.nativeObj);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.Img_hash#averageHash(Mat,Mat)}.
     *
     * @param inputArr_nativeObj  native matrix or object handle for {@code inputArr}; input image want to compute hash
     *                            value, type should be CV_8UC4, CV_8UC3 or CV_8UC1
     * @param outputArr_nativeObj native matrix or object handle for {@code outputArr}; Hash value of input, it will
     *                            contain 16 hex decimal number, return type is CV_8U
     */
    private static native void averageHash_0(long inputArr_nativeObj, long outputArr_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.Img_hash#colorMomentHash(Mat,Mat)}.
     *
     * @param inputArr_nativeObj  native matrix or object handle for {@code inputArr}; input image want to compute hash
     *                            value, type should be CV_8UC4, CV_8UC3 or CV_8UC1
     * @param outputArr_nativeObj native matrix or object handle for {@code outputArr}; 42 hash values with type
     *                            CV_64F(double)
     */
    private static native void colorMomentHash_0(long inputArr_nativeObj, long outputArr_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.Img_hash#radialVarianceHash(Mat,Mat,double,int)}.
     *
     * @param inputArr_nativeObj  native matrix or object handle for {@code inputArr}; input image want to compute hash
     *                            value, type should be CV_8UC4, CV_8UC3, CV_8UC1
     * @param outputArr_nativeObj native matrix or object handle for {@code outputArr}; Hash value of input
     * @param sigma               Gaussian kernel standard deviation
     * @param numOfAngleLine      The number of angles to consider
     */
    private static native void radialVarianceHash_0(
            long inputArr_nativeObj,
            long outputArr_nativeObj,
            double sigma,
            int numOfAngleLine);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.Img_hash#radialVarianceHash(Mat,Mat,double)}.
     *
     * @param inputArr_nativeObj  native matrix or object handle for {@code inputArr}; input image want to compute hash
     *                            value, type should be CV_8UC4, CV_8UC3, CV_8UC1
     * @param outputArr_nativeObj native matrix or object handle for {@code outputArr}; Hash value of input
     * @param sigma               Gaussian kernel standard deviation
     */
    private static native void radialVarianceHash_1(long inputArr_nativeObj, long outputArr_nativeObj, double sigma);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.Img_hash#radialVarianceHash(Mat,Mat)}.
     *
     * @param inputArr_nativeObj  native matrix or object handle for {@code inputArr}; input image want to compute hash
     *                            value, type should be CV_8UC4, CV_8UC3, CV_8UC1
     * @param outputArr_nativeObj native matrix or object handle for {@code outputArr}; Hash value of input
     */
    private static native void radialVarianceHash_2(long inputArr_nativeObj, long outputArr_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.Img_hash#pHash(Mat,Mat)}.
     *
     * @param inputArr_nativeObj  native matrix or object handle for {@code inputArr}; input image want to compute hash
     *                            value, type should be CV_8UC4, CV_8UC3, CV_8UC1
     * @param outputArr_nativeObj native matrix or object handle for {@code outputArr}; Hash value of input, it will
     *                            contain 8 uchar value
     */
    private static native void pHash_0(long inputArr_nativeObj, long outputArr_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.Img_hash#marrHildrethHash(Mat,Mat,float,float)}.
     *
     * @param inputArr_nativeObj  native matrix or object handle for {@code inputArr}; input image want to compute hash
     *                            value, type should be CV_8UC4, CV_8UC3, CV_8UC1
     * @param outputArr_nativeObj native matrix or object handle for {@code outputArr}; Hash value of input, it will
     *                            contain 16 hex decimal number, return type is CV_8U
     * @param alpha               int scale factor for marr wavelet (default=2)
     * @param scale               int level of scale factor (default = 1)
     */
    private static native void marrHildrethHash_0(
            long inputArr_nativeObj,
            long outputArr_nativeObj,
            float alpha,
            float scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.Img_hash#marrHildrethHash(Mat,Mat,float)}.
     *
     * @param inputArr_nativeObj  native matrix or object handle for {@code inputArr}; input image want to compute hash
     *                            value, type should be CV_8UC4, CV_8UC3, CV_8UC1
     * @param outputArr_nativeObj native matrix or object handle for {@code outputArr}; Hash value of input, it will
     *                            contain 16 hex decimal number, return type is CV_8U
     * @param alpha               int scale factor for marr wavelet (default=2)
     */
    private static native void marrHildrethHash_1(long inputArr_nativeObj, long outputArr_nativeObj, float alpha);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.Img_hash#marrHildrethHash(Mat,Mat)}.
     *
     * @param inputArr_nativeObj  native matrix or object handle for {@code inputArr}; input image want to compute hash
     *                            value, type should be CV_8UC4, CV_8UC3, CV_8UC1
     * @param outputArr_nativeObj native matrix or object handle for {@code outputArr}; Hash value of input, it will
     *                            contain 16 hex decimal number, return type is CV_8U
     */
    private static native void marrHildrethHash_2(long inputArr_nativeObj, long outputArr_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.Img_hash#blockMeanHash(Mat,Mat,int)}.
     *
     * @param inputArr_nativeObj  native matrix or object handle for {@code inputArr}; input image want to compute hash
     *                            value, type should be CV_8UC4, CV_8UC3 or CV_8UC1
     * @param outputArr_nativeObj native matrix or object handle for {@code outputArr}; Hash value of input, it will
     *                            contain 16 hex decimal number, return type is CV_8U
     * @param mode                block-mean hashing mode, selected by the BLOCK_MEAN_HASH_MODE constants
     */
    private static native void blockMeanHash_0(long inputArr_nativeObj, long outputArr_nativeObj, int mode);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.Img_hash#blockMeanHash(Mat,Mat)}.
     *
     * @param inputArr_nativeObj  native matrix or object handle for {@code inputArr}; input image want to compute hash
     *                            value, type should be CV_8UC4, CV_8UC3 or CV_8UC1
     * @param outputArr_nativeObj native matrix or object handle for {@code outputArr}; Hash value of input, it will
     *                            contain 16 hex decimal number, return type is CV_8U
     */
    private static native void blockMeanHash_1(long inputArr_nativeObj, long outputArr_nativeObj);

}
