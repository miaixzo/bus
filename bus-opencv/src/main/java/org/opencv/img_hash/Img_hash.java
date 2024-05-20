//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.img_hash;

import org.opencv.core.Mat;

// C++: class Img_hash

public class Img_hash {

    // C++: enum BlockMeanHashMode (cv.img_hash.BlockMeanHashMode)
    public static final int
            BLOCK_MEAN_HASH_MODE_0 = 0,
            BLOCK_MEAN_HASH_MODE_1 = 1;


    //
    // C++:  void cv::img_hash::averageHash(Mat inputArr, Mat& outputArr)
    //

    /**
     * Calculates img_hash::AverageHash in one call
     *
     * @param inputArr  input image want to compute hash value, type should be CV_8UC4, CV_8UC3 or CV_8UC1.
     * @param outputArr Hash value of input, it will contain 16 hex decimal number, return type is CV_8U
     */
    public static void averageHash(Mat inputArr, Mat outputArr) {
        averageHash_0(inputArr.nativeObj, outputArr.nativeObj);
    }


    //
    // C++:  void cv::img_hash::blockMeanHash(Mat inputArr, Mat& outputArr, int mode = BLOCK_MEAN_HASH_MODE_0)
    //

    /**
     * Computes block mean hash of the input image
     *
     * @param inputArr  input image want to compute hash value, type should be CV_8UC4, CV_8UC3 or CV_8UC1.
     * @param outputArr Hash value of input, it will contain 16 hex decimal number, return type is CV_8U
     * @param mode      the mode
     */
    public static void blockMeanHash(Mat inputArr, Mat outputArr, int mode) {
        blockMeanHash_0(inputArr.nativeObj, outputArr.nativeObj, mode);
    }

    /**
     * Computes block mean hash of the input image
     *
     * @param inputArr  input image want to compute hash value, type should be CV_8UC4, CV_8UC3 or CV_8UC1.
     * @param outputArr Hash value of input, it will contain 16 hex decimal number, return type is CV_8U
     */
    public static void blockMeanHash(Mat inputArr, Mat outputArr) {
        blockMeanHash_1(inputArr.nativeObj, outputArr.nativeObj);
    }


    //
    // C++:  void cv::img_hash::colorMomentHash(Mat inputArr, Mat& outputArr)
    //

    /**
     * Computes color moment hash of the input, the algorithm
     * is come from the paper "Perceptual  Hashing  for  Color  Images
     * Using  Invariant Moments"
     *
     * @param inputArr  input image want to compute hash value,
     *                  type should be CV_8UC4, CV_8UC3 or CV_8UC1.
     * @param outputArr 42 hash values with type CV_64F(double)
     */
    public static void colorMomentHash(Mat inputArr, Mat outputArr) {
        colorMomentHash_0(inputArr.nativeObj, outputArr.nativeObj);
    }


    //
    // C++:  void cv::img_hash::marrHildrethHash(Mat inputArr, Mat& outputArr, float alpha = 2.0f, float scale = 1.0f)
    //

    /**
     * Computes average hash value of the input image
     *
     * @param inputArr  input image want to compute hash value,
     *                  type should be CV_8UC4, CV_8UC3, CV_8UC1.
     * @param outputArr Hash value of input, it will contain 16 hex
     *                  decimal number, return type is CV_8U
     * @param alpha     int scale factor for marr wavelet (default=2).
     * @param scale     int level of scale factor (default = 1)
     */
    public static void marrHildrethHash(Mat inputArr, Mat outputArr, float alpha, float scale) {
        marrHildrethHash_0(inputArr.nativeObj, outputArr.nativeObj, alpha, scale);
    }

    /**
     * Computes average hash value of the input image
     *
     * @param inputArr  input image want to compute hash value,
     *                  type should be CV_8UC4, CV_8UC3, CV_8UC1.
     * @param outputArr Hash value of input, it will contain 16 hex
     *                  decimal number, return type is CV_8U
     * @param alpha     int scale factor for marr wavelet (default=2).
     */
    public static void marrHildrethHash(Mat inputArr, Mat outputArr, float alpha) {
        marrHildrethHash_1(inputArr.nativeObj, outputArr.nativeObj, alpha);
    }

    /**
     * Computes average hash value of the input image
     *
     * @param inputArr  input image want to compute hash value,
     *                  type should be CV_8UC4, CV_8UC3, CV_8UC1.
     * @param outputArr Hash value of input, it will contain 16 hex
     *                  decimal number, return type is CV_8U
     */
    public static void marrHildrethHash(Mat inputArr, Mat outputArr) {
        marrHildrethHash_2(inputArr.nativeObj, outputArr.nativeObj);
    }


    //
    // C++:  void cv::img_hash::pHash(Mat inputArr, Mat& outputArr)
    //

    /**
     * Computes pHash value of the input image
     *
     * @param inputArr  input image want to compute hash value,
     *                  type should be CV_8UC4, CV_8UC3, CV_8UC1.
     * @param outputArr Hash value of input, it will contain 8 uchar value
     */
    public static void pHash(Mat inputArr, Mat outputArr) {
        pHash_0(inputArr.nativeObj, outputArr.nativeObj);
    }


    //
    // C++:  void cv::img_hash::radialVarianceHash(Mat inputArr, Mat& outputArr, double sigma = 1, int numOfAngleLine = 180)
    //

    /**
     * Computes radial variance hash of the input image
     *
     * @param inputArr       input image want to compute hash value,
     *                       type should be CV_8UC4, CV_8UC3, CV_8UC1.
     * @param outputArr      Hash value of input
     * @param sigma          Gaussian kernel standard deviation
     * @param numOfAngleLine The number of angles to consider
     */
    public static void radialVarianceHash(Mat inputArr, Mat outputArr, double sigma, int numOfAngleLine) {
        radialVarianceHash_0(inputArr.nativeObj, outputArr.nativeObj, sigma, numOfAngleLine);
    }

    /**
     * Computes radial variance hash of the input image
     *
     * @param inputArr  input image want to compute hash value,
     *                  type should be CV_8UC4, CV_8UC3, CV_8UC1.
     * @param outputArr Hash value of input
     * @param sigma     Gaussian kernel standard deviation
     */
    public static void radialVarianceHash(Mat inputArr, Mat outputArr, double sigma) {
        radialVarianceHash_1(inputArr.nativeObj, outputArr.nativeObj, sigma);
    }

    /**
     * Computes radial variance hash of the input image
     *
     * @param inputArr  input image want to compute hash value,
     *                  type should be CV_8UC4, CV_8UC3, CV_8UC1.
     * @param outputArr Hash value of input
     */
    public static void radialVarianceHash(Mat inputArr, Mat outputArr) {
        radialVarianceHash_2(inputArr.nativeObj, outputArr.nativeObj);
    }


    // C++:  void cv::img_hash::averageHash(Mat inputArr, Mat& outputArr)
    private static native void averageHash_0(long inputArr_nativeObj, long outputArr_nativeObj);

    // C++:  void cv::img_hash::blockMeanHash(Mat inputArr, Mat& outputArr, int mode = BLOCK_MEAN_HASH_MODE_0)
    private static native void blockMeanHash_0(long inputArr_nativeObj, long outputArr_nativeObj, int mode);

    private static native void blockMeanHash_1(long inputArr_nativeObj, long outputArr_nativeObj);

    // C++:  void cv::img_hash::colorMomentHash(Mat inputArr, Mat& outputArr)
    private static native void colorMomentHash_0(long inputArr_nativeObj, long outputArr_nativeObj);

    // C++:  void cv::img_hash::marrHildrethHash(Mat inputArr, Mat& outputArr, float alpha = 2.0f, float scale = 1.0f)
    private static native void marrHildrethHash_0(long inputArr_nativeObj, long outputArr_nativeObj, float alpha, float scale);

    private static native void marrHildrethHash_1(long inputArr_nativeObj, long outputArr_nativeObj, float alpha);

    private static native void marrHildrethHash_2(long inputArr_nativeObj, long outputArr_nativeObj);

    // C++:  void cv::img_hash::pHash(Mat inputArr, Mat& outputArr)
    private static native void pHash_0(long inputArr_nativeObj, long outputArr_nativeObj);

    // C++:  void cv::img_hash::radialVarianceHash(Mat inputArr, Mat& outputArr, double sigma = 1, int numOfAngleLine = 180)
    private static native void radialVarianceHash_0(long inputArr_nativeObj, long outputArr_nativeObj, double sigma, int numOfAngleLine);

    private static native void radialVarianceHash_1(long inputArr_nativeObj, long outputArr_nativeObj, double sigma);

    private static native void radialVarianceHash_2(long inputArr_nativeObj, long outputArr_nativeObj);

}
