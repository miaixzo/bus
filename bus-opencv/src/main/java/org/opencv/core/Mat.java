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
package org.opencv.core;

import java.nio.ByteBuffer;

// C++: class Mat
//javadoc: Mat
/**
 * Provides the {@code Mat} API.
 */
public class Mat extends CleanableMat {

    /**
     * Creates a new {@code Mat} instance.
     *
     * @param addr the {@code addr} value
     */
    public Mat(long addr) {
        super(addr);
    }

    //
    // C++: Mat::Mat()
    //

    // javadoc: Mat::Mat()
    /**
     * Creates a new {@code Mat} instance.
     */
    public Mat() {
        super(n_Mat());
    }

    //
    // C++: Mat::Mat(int rows, int cols, int type)
    //

    // javadoc: Mat::Mat(rows, cols, type)
    /**
     * Creates a new {@code Mat} instance.
     *
     * @param rows the {@code rows} value
     * @param cols the {@code cols} value
     * @param type the {@code type} value
     */
    public Mat(int rows, int cols, int type) {
        super(n_Mat(rows, cols, type));
    }

    //
    // C++: Mat::Mat(int rows, int cols, int type, void* data)
    //

    // javadoc: Mat::Mat(rows, cols, type, data)
    /**
     * Creates a new {@code Mat} instance.
     *
     * @param rows the {@code rows} value
     * @param cols the {@code cols} value
     * @param type the {@code type} value
     * @param data the {@code data} value
     */
    public Mat(int rows, int cols, int type, ByteBuffer data) {
        super(n_Mat(rows, cols, type, data));
    }

    //
    // C++: Mat::Mat(int rows, int cols, int type, void* data, size_t step)
    //

    // javadoc: Mat::Mat(rows, cols, type, data, step)
    /**
     * Creates a new {@code Mat} instance.
     *
     * @param rows the {@code rows} value
     * @param cols the {@code cols} value
     * @param type the {@code type} value
     * @param data the {@code data} value
     * @param step the {@code step} value
     */
    public Mat(int rows, int cols, int type, ByteBuffer data, long step) {
        super(n_Mat(rows, cols, type, data, step));
    }

    //
    // C++: Mat::Mat(Size size, int type)
    //

    // javadoc: Mat::Mat(size, type)
    /**
     * Creates a new {@code Mat} instance.
     *
     * @param size the {@code size} value
     * @param type the {@code type} value
     */
    public Mat(Size size, int type) {
        super(n_Mat(size.width, size.height, type));
    }

    //
    // C++: Mat::Mat(int ndims, const int* sizes, int type)
    //

    // javadoc: Mat::Mat(sizes, type)
    /**
     * Creates a new {@code Mat} instance.
     *
     * @param sizes the {@code sizes} value
     * @param type the {@code type} value
     */
    public Mat(int[] sizes, int type) {
        super(n_Mat(sizes.length, sizes, type));
    }

    //
    // C++: Mat::Mat(int rows, int cols, int type, Scalar s)
    //

    // javadoc: Mat::Mat(rows, cols, type, s)
    /**
     * Creates a new {@code Mat} instance.
     *
     * @param rows the {@code rows} value
     * @param cols the {@code cols} value
     * @param type the {@code type} value
     * @param s the {@code s} value
     */
    public Mat(int rows, int cols, int type, Scalar s) {
        super(n_Mat(rows, cols, type, s.val[0], s.val[1], s.val[2], s.val[3]));
    }

    //
    // C++: Mat::Mat(Size size, int type, Scalar s)
    //

    // javadoc: Mat::Mat(size, type, s)
    /**
     * Creates a new {@code Mat} instance.
     *
     * @param size the {@code size} value
     * @param type the {@code type} value
     * @param s the {@code s} value
     */
    public Mat(Size size, int type, Scalar s) {
        super(n_Mat(size.width, size.height, type, s.val[0], s.val[1], s.val[2], s.val[3]));
    }

    //
    // C++: Mat::Mat(int ndims, const int* sizes, int type, Scalar s)
    //

    // javadoc: Mat::Mat(sizes, type, s)
    /**
     * Creates a new {@code Mat} instance.
     *
     * @param sizes the {@code sizes} value
     * @param type the {@code type} value
     * @param s the {@code s} value
     */
    public Mat(int[] sizes, int type, Scalar s) {
        super(n_Mat(sizes.length, sizes, type, s.val[0], s.val[1], s.val[2], s.val[3]));
    }

    //
    // C++: Mat::Mat(Mat m, Range rowRange, Range colRange = Range::all())
    //

    // javadoc: Mat::Mat(m, rowRange, colRange)
    /**
     * Creates a new {@code Mat} instance.
     *
     * @param m the {@code m} value
     * @param rowRange the {@code rowRange} value
     * @param colRange the {@code colRange} value
     */
    public Mat(Mat m, Range rowRange, Range colRange) {
        super(n_Mat(m.nativeObj, rowRange.start, rowRange.end, colRange.start, colRange.end));
    }

    // javadoc: Mat::Mat(m, rowRange)
    /**
     * Creates a new {@code Mat} instance.
     *
     * @param m the {@code m} value
     * @param rowRange the {@code rowRange} value
     */
    public Mat(Mat m, Range rowRange) {
        super(n_Mat(m.nativeObj, rowRange.start, rowRange.end));
    }

    //
    // C++: Mat::Mat(const Mat& m, const std::vector<Range>& ranges)
    //

    // javadoc: Mat::Mat(m, ranges)
    /**
     * Creates a new {@code Mat} instance.
     *
     * @param m the {@code m} value
     * @param ranges the {@code ranges} value
     */
    public Mat(Mat m, Range[] ranges) {
        super(n_Mat(m.nativeObj, ranges));
    }

    //
    // C++: Mat::Mat(Mat m, Rect roi)
    //

    // javadoc: Mat::Mat(m, roi)
    /**
     * Creates a new {@code Mat} instance.
     *
     * @param m the {@code m} value
     * @param roi the {@code roi} value
     */
    public Mat(Mat m, Rect roi) {
        super(n_Mat(m.nativeObj, roi.y, roi.y + roi.height, roi.x, roi.x + roi.width));
    }

    //
    // C++: Mat Mat::adjustROI(int dtop, int dbottom, int dleft, int dright)
    //

    // javadoc: Mat::diag(d)
    /**
     * Performs the {@code diag} operation.
     *
     * @param d the {@code d} value
     * @return the operation result
     */
    public static Mat diag(Mat d) {
        return new Mat(n_diag(d.nativeObj));
    }

    //
    // C++: void Mat::assignTo(Mat m, int type = -1)
    //

    // javadoc: Mat::eye(rows, cols, type)
    /**
     * Performs the {@code eye} operation.
     *
     * @param rows the {@code rows} value
     * @param cols the {@code cols} value
     * @param type the {@code type} value
     * @return the operation result
     */
    public static Mat eye(int rows, int cols, int type) {
        return new Mat(n_eye(rows, cols, type));
    }

    // javadoc: Mat::eye(size, type)
    /**
     * Performs the {@code eye} operation.
     *
     * @param size the {@code size} value
     * @param type the {@code type} value
     * @return the operation result
     */
    public static Mat eye(Size size, int type) {
        return new Mat(n_eye(size.width, size.height, type));
    }

    //
    // C++: int Mat::channels()
    //

    // javadoc: Mat::ones(rows, cols, type)
    /**
     * Performs the {@code ones} operation.
     *
     * @param rows the {@code rows} value
     * @param cols the {@code cols} value
     * @param type the {@code type} value
     * @return the operation result
     */
    public static Mat ones(int rows, int cols, int type) {
        return new Mat(n_ones(rows, cols, type));
    }

    //
    // C++: int Mat::checkVector(int elemChannels, int depth = -1, bool
    // requireContinuous = true)
    //

    // javadoc: Mat::ones(size, type)
    /**
     * Performs the {@code ones} operation.
     *
     * @param size the {@code size} value
     * @param type the {@code type} value
     * @return the operation result
     */
    public static Mat ones(Size size, int type) {
        return new Mat(n_ones(size.width, size.height, type));
    }

    // javadoc: Mat::ones(sizes, type)
    /**
     * Performs the {@code ones} operation.
     *
     * @param sizes the {@code sizes} value
     * @param type the {@code type} value
     * @return the operation result
     */
    public static Mat ones(int[] sizes, int type) {
        return new Mat(n_ones(sizes.length, sizes, type));
    }

    // javadoc: Mat::zeros(rows, cols, type)
    /**
     * Performs the {@code zeros} operation.
     *
     * @param rows the {@code rows} value
     * @param cols the {@code cols} value
     * @param type the {@code type} value
     * @return the operation result
     */
    public static Mat zeros(int rows, int cols, int type) {
        return new Mat(n_zeros(rows, cols, type));
    }

    //
    // C++: Mat Mat::clone()
    //

    // javadoc: Mat::zeros(size, type)
    /**
     * Performs the {@code zeros} operation.
     *
     * @param size the {@code size} value
     * @param type the {@code type} value
     * @return the operation result
     */
    public static Mat zeros(Size size, int type) {
        return new Mat(n_zeros(size.width, size.height, type));
    }

    //
    // C++: Mat Mat::col(int x)
    //

    // javadoc: Mat::zeros(sizes, type)
    /**
     * Performs the {@code zeros} operation.
     *
     * @param sizes the {@code sizes} value
     * @param type the {@code type} value
     * @return the operation result
     */
    public static Mat zeros(int[] sizes, int type) {
        return new Mat(n_zeros(sizes.length, sizes, type));
    }

    //
    // C++: Mat Mat::colRange(int startcol, int endcol)
    //

    // C++: Mat::Mat()
    private static native long n_Mat();

    //
    // C++: Mat Mat::colRange(Range r)
    //

    // C++: Mat::Mat(int rows, int cols, int type)
    private static native long n_Mat(int rows, int cols, int type);

    //
    // C++: int Mat::dims()
    //

    // C++: Mat::Mat(int ndims, const int* sizes, int type)
    private static native long n_Mat(int ndims, int[] sizes, int type);

    //
    // C++: int Mat::cols()
    //

    // C++: Mat::Mat(int rows, int cols, int type, void* data)
    private static native long n_Mat(int rows, int cols, int type, ByteBuffer data);

    //
    // C++: void Mat::convertTo(Mat& m, int rtype, double alpha = 1, double beta
    // = 0)
    //

    // C++: Mat::Mat(int rows, int cols, int type, void* data, size_t step)
    private static native long n_Mat(int rows, int cols, int type, ByteBuffer data, long step);

    // C++: Mat::Mat(Size size, int type)
    private static native long n_Mat(double size_width, double size_height, int type);

    // C++: Mat::Mat(int rows, int cols, int type, Scalar s)
    private static native long n_Mat(
            int rows,
            int cols,
            int type,
            double s_val0,
            double s_val1,
            double s_val2,
            double s_val3);

    //
    // C++: void Mat::copyTo(Mat& m)
    //

    // C++: Mat::Mat(Size size, int type, Scalar s)
    private static native long n_Mat(
            double size_width,
            double size_height,
            int type,
            double s_val0,
            double s_val1,
            double s_val2,
            double s_val3);

    //
    // C++: void Mat::copyTo(Mat& m, Mat mask)
    //

    // C++: Mat::Mat(int ndims, const int* sizes, int type, Scalar s)
    private static native long n_Mat(
            int ndims,
            int[] sizes,
            int type,
            double s_val0,
            double s_val1,
            double s_val2,
            double s_val3);

    //
    // C++: void Mat::create(int rows, int cols, int type)
    //

    // C++: Mat::Mat(Mat m, Range rowRange, Range colRange = Range::all())
    private static native long n_Mat(
            long m_nativeObj,
            int rowRange_start,
            int rowRange_end,
            int colRange_start,
            int colRange_end);

    //
    // C++: void Mat::create(Size size, int type)
    //

    private static native long n_Mat(long m_nativeObj, int rowRange_start, int rowRange_end);

    //
    // C++: void Mat::create(int ndims, const int* sizes, int type)
    //

    // C++: Mat::Mat(const Mat& m, const std::vector<Range>& ranges)
    private static native long n_Mat(long m_nativeObj, Range[] ranges);

    //
    // C++: void Mat::copySize(const Mat& m);
    //

    // C++: Mat Mat::adjustROI(int dtop, int dbottom, int dleft, int dright)
    private static native long n_adjustROI(long nativeObj, int dtop, int dbottom, int dleft, int dright);

    //
    // C++: Mat Mat::cross(Mat m)
    //

    // C++: void Mat::assignTo(Mat m, int type = -1)
    private static native void n_assignTo(long nativeObj, long m_nativeObj, int type);

    //
    // C++: long Mat::dataAddr()
    //

    private static native void n_assignTo(long nativeObj, long m_nativeObj);

    //
    // C++: int Mat::depth()
    //

    // C++: int Mat::channels()
    private static native int n_channels(long nativeObj);

    //
    // C++: Mat Mat::diag(int d = 0)
    //

    // C++: int Mat::checkVector(int elemChannels, int depth = -1, bool
    // requireContinuous = true)
    private static native int n_checkVector(long nativeObj, int elemChannels, int depth, boolean requireContinuous);

    private static native int n_checkVector(long nativeObj, int elemChannels, int depth);

    //
    // C++: static Mat Mat::diag(Mat d)
    //

    private static native int n_checkVector(long nativeObj, int elemChannels);

    //
    // C++: double Mat::dot(Mat m)
    //

    // C++: Mat Mat::clone()
    private static native long n_clone(long nativeObj);

    //
    // C++: size_t Mat::elemSize()
    //

    // C++: Mat Mat::col(int x)
    private static native long n_col(long nativeObj, int x);

    //
    // C++: size_t Mat::elemSize1()
    //

    // C++: Mat Mat::colRange(int startcol, int endcol)
    private static native long n_colRange(long nativeObj, int startcol, int endcol);

    //
    // C++: bool Mat::empty()
    //

    // C++: int Mat::dims()
    private static native int n_dims(long nativeObj);

    //
    // C++: static Mat Mat::eye(int rows, int cols, int type)
    //

    // C++: int Mat::cols()
    private static native int n_cols(long nativeObj);

    //
    // C++: static Mat Mat::eye(Size size, int type)
    //

    // C++: void Mat::convertTo(Mat& m, int rtype, double alpha = 1, double beta
    // = 0)
    private static native void n_convertTo(long nativeObj, long m_nativeObj, int rtype, double alpha, double beta);

    //
    // C++: Mat Mat::inv(int method = DECOMP_LU)
    //

    private static native void n_convertTo(long nativeObj, long m_nativeObj, int rtype, double alpha);

    private static native void n_convertTo(long nativeObj, long m_nativeObj, int rtype);

    //
    // C++: bool Mat::isContinuous()
    //

    // C++: void Mat::copyTo(Mat& m)
    private static native void n_copyTo(long nativeObj, long m_nativeObj);

    //
    // C++: bool Mat::isSubmatrix()
    //

    // C++: void Mat::copyTo(Mat& m, Mat mask)
    private static native void n_copyTo(long nativeObj, long m_nativeObj, long mask_nativeObj);

    //
    // C++: void Mat::locateROI(Size wholeSize, Point ofs)
    //

    // C++: void Mat::create(int rows, int cols, int type)
    private static native void n_create(long nativeObj, int rows, int cols, int type);

    //
    // C++: Mat Mat::mul(Mat m, double scale = 1)
    //

    // C++: void Mat::create(Size size, int type)
    private static native void n_create(long nativeObj, double size_width, double size_height, int type);

    // C++: void Mat::create(int ndims, const int* sizes, int type)
    private static native void n_create(long nativeObj, int ndims, int[] sizes, int type);

    // C++: void Mat::copySize(const Mat& m)
    private static native void n_copySize(long nativeObj, long m_nativeObj);

    //
    // C++: static Mat Mat::ones(int rows, int cols, int type)
    //

    // C++: Mat Mat::cross(Mat m)
    private static native long n_cross(long nativeObj, long m_nativeObj);

    //
    // C++: static Mat Mat::ones(Size size, int type)
    //

    // C++: long Mat::dataAddr()
    private static native long n_dataAddr(long nativeObj);

    //
    // C++: static Mat Mat::ones(int ndims, const int* sizes, int type)
    //

    // C++: int Mat::depth()
    private static native int n_depth(long nativeObj);

    //
    // C++: void Mat::push_back(Mat m)
    //

    // C++: Mat Mat::diag(int d = 0)
    private static native long n_diag(long nativeObj, int d);

    //
    // C++: void Mat::release()
    //

    // C++: static Mat Mat::diag(Mat d)
    private static native long n_diag(long d_nativeObj);

    //
    // C++: Mat Mat::reshape(int cn, int rows = 0)
    //

    // C++: double Mat::dot(Mat m)
    private static native double n_dot(long nativeObj, long m_nativeObj);

    // C++: size_t Mat::elemSize()
    private static native long n_elemSize(long nativeObj);

    //
    // C++: Mat Mat::reshape(int cn, int newndims, const int* newsz)
    //

    // C++: size_t Mat::elemSize1()
    private static native long n_elemSize1(long nativeObj);

    //
    // C++: Mat Mat::row(int y)
    //

    // C++: bool Mat::empty()
    private static native boolean n_empty(long nativeObj);

    //
    // C++: Mat Mat::rowRange(int startrow, int endrow)
    //

    // C++: static Mat Mat::eye(int rows, int cols, int type)
    private static native long n_eye(int rows, int cols, int type);

    //
    // C++: Mat Mat::rowRange(Range r)
    //

    // C++: static Mat Mat::eye(Size size, int type)
    private static native long n_eye(double size_width, double size_height, int type);

    //
    // C++: int Mat::rows()
    //

    // C++: Mat Mat::inv(int method = DECOMP_LU)
    private static native long n_inv(long nativeObj, int method);

    //
    // C++: Mat Mat::operator =(Scalar s)
    //

    private static native long n_inv(long nativeObj);

    //
    // C++: Mat Mat::setTo(Scalar value, Mat mask = Mat())
    //

    // C++: bool Mat::isContinuous()
    private static native boolean n_isContinuous(long nativeObj);

    //
    // C++: Mat Mat::setTo(Mat value, Mat mask = Mat())
    //

    // C++: bool Mat::isSubmatrix()
    private static native boolean n_isSubmatrix(long nativeObj);

    // C++: void Mat::locateROI(Size wholeSize, Point ofs)
    private static native void locateROI_0(long nativeObj, double[] wholeSize_out, double[] ofs_out);

    //
    // C++: Size Mat::size()
    //

    // C++: Mat Mat::mul(Mat m, double scale = 1)
    private static native long n_mul(long nativeObj, long m_nativeObj, double scale);

    //
    // C++: int Mat::size(int i)
    //

    private static native long n_mul(long nativeObj, long m_nativeObj);

    //
    // C++: size_t Mat::step1(int i = 0)
    //

    private static native long n_matMul(long nativeObj, long m_nativeObj);

    // C++: static Mat Mat::ones(int rows, int cols, int type)
    private static native long n_ones(int rows, int cols, int type);

    //
    // C++: Mat Mat::operator()(int rowStart, int rowEnd, int colStart, int
    // colEnd)
    //

    // C++: static Mat Mat::ones(Size size, int type)
    private static native long n_ones(double size_width, double size_height, int type);

    //
    // C++: Mat Mat::operator()(Range rowRange, Range colRange)
    //

    // C++: static Mat Mat::ones(int ndims, const int* sizes, int type)
    private static native long n_ones(int ndims, int[] sizes, int type);

    //
    // C++: Mat Mat::operator()(const std::vector<Range>& ranges)
    //

    // C++: void Mat::push_back(Mat m)
    private static native void n_push_back(long nativeObj, long m_nativeObj);

    //
    // C++: Mat Mat::operator()(Rect roi)
    //

    // C++: void Mat::release()
    private static native void n_release(long nativeObj);

    //
    // C++: Mat Mat::t()
    //

    // C++: Mat Mat::reshape(int cn, int rows = 0)
    private static native long n_reshape(long nativeObj, int cn, int rows);

    //
    // C++: size_t Mat::total()
    //

    private static native long n_reshape(long nativeObj, int cn);

    //
    // C++: int Mat::type()
    //

    // C++: Mat Mat::reshape(int cn, int newndims, const int* newsz)
    private static native long n_reshape_1(long nativeObj, int cn, int newndims, int[] newsz);

    //
    // C++: static Mat Mat::zeros(int rows, int cols, int type)
    //

    // C++: Mat Mat::row(int y)
    private static native long n_row(long nativeObj, int y);

    //
    // C++: static Mat Mat::zeros(Size size, int type)
    //

    // C++: Mat Mat::rowRange(int startrow, int endrow)
    private static native long n_rowRange(long nativeObj, int startrow, int endrow);

    //
    // C++: static Mat Mat::zeros(int ndims, const int* sizes, int type)
    //

    // C++: int Mat::rows()
    private static native int n_rows(long nativeObj);

    // C++: Mat Mat::operator =(Scalar s)
    private static native long n_setTo(long nativeObj, double s_val0, double s_val1, double s_val2, double s_val3);

    // C++: Mat Mat::setTo(Scalar value, Mat mask = Mat())
    private static native long n_setTo(
            long nativeObj,
            double s_val0,
            double s_val1,
            double s_val2,
            double s_val3,
            long mask_nativeObj);

    // C++: Mat Mat::setTo(Mat value, Mat mask = Mat())
    private static native long n_setTo(long nativeObj, long value_nativeObj, long mask_nativeObj);

    private static native long n_setTo(long nativeObj, long value_nativeObj);

    // C++: Size Mat::size()
    private static native double[] n_size(long nativeObj);

    // C++: int Mat::size(int i)
    private static native int n_size_i(long nativeObj, int i);

    // C++: size_t Mat::step1(int i = 0)
    private static native long n_step1(long nativeObj, int i);

    private static native long n_step1(long nativeObj);

    // C++: Mat Mat::operator()(Range rowRange, Range colRange)
    private static native long n_submat_rr(
            long nativeObj,
            int rowRange_start,
            int rowRange_end,
            int colRange_start,
            int colRange_end);

    // C++: Mat Mat::operator()(const std::vector<Range>& ranges)
    private static native long n_submat_ranges(long nativeObj, Range[] ranges);

    // C++: Mat Mat::operator()(Rect roi)
    private static native long n_submat(long nativeObj, int roi_x, int roi_y, int roi_width, int roi_height);

    // C++: Mat Mat::t()
    private static native long n_t(long nativeObj);

    // C++: size_t Mat::total()
    private static native long n_total(long nativeObj);

    // C++: int Mat::type()
    private static native int n_type(long nativeObj);

    // C++: static Mat Mat::zeros(int rows, int cols, int type)
    private static native long n_zeros(int rows, int cols, int type);

    // C++: static Mat Mat::zeros(Size size, int type)
    private static native long n_zeros(double size_width, double size_height, int type);

    // C++: static Mat Mat::zeros(int ndims, const int* sizes, int type)
    private static native long n_zeros(int ndims, int[] sizes, int type);

    private static native int nPutD(long self, int row, int col, int count, double[] data);

    private static native int nPutDIdx(long self, int[] idx, int count, double[] data);

    private static native int nPutF(long self, int row, int col, int count, float[] data);

    private static native int nPutFIdx(long self, int[] idx, int count, float[] data);

    private static native int nPutI(long self, int row, int col, int count, int[] data);

    private static native int nPutIIdx(long self, int[] idx, int count, int[] data);

    private static native int nPutS(long self, int row, int col, int count, short[] data);

    private static native int nPutSIdx(long self, int[] idx, int count, short[] data);

    private static native int nPutB(long self, int row, int col, int count, byte[] data);

    private static native int nPutBIdx(long self, int[] idx, int count, byte[] data);

    private static native int nPutBwOffset(long self, int row, int col, int count, int offset, byte[] data);

    private static native int nPutBwIdxOffset(long self, int[] idx, int count, int offset, byte[] data);

    private static native int nGetB(long self, int row, int col, int count, byte[] vals);

    private static native int nGetBIdx(long self, int[] idx, int count, byte[] vals);

    private static native int nGetS(long self, int row, int col, int count, short[] vals);

    private static native int nGetSIdx(long self, int[] idx, int count, short[] vals);

    private static native int nGetI(long self, int row, int col, int count, int[] vals);

    private static native int nGetIIdx(long self, int[] idx, int count, int[] vals);

    private static native int nGetF(long self, int row, int col, int count, float[] vals);

    private static native int nGetFIdx(long self, int[] idx, int count, float[] vals);

    private static native int nGetD(long self, int row, int col, int count, double[] vals);

    private static native int nGetDIdx(long self, int[] idx, int count, double[] vals);

    private static native double[] nGet(long self, int row, int col);

    private static native double[] nGetIdx(long self, int[] idx);

    private static native String nDump(long self);

    // javadoc: Mat::adjustROI(dtop, dbottom, dleft, dright)
    /**
     * Performs the {@code adjustROI} operation.
     *
     * @param dtop the {@code dtop} value
     * @param dbottom the {@code dbottom} value
     * @param dleft the {@code dleft} value
     * @param dright the {@code dright} value
     * @return the operation result
     */
    public Mat adjustROI(int dtop, int dbottom, int dleft, int dright) {
        return new Mat(n_adjustROI(nativeObj, dtop, dbottom, dleft, dright));
    }

    // javadoc: Mat::assignTo(m, type)
    /**
     * Performs the {@code assignTo} operation.
     *
     * @param m the {@code m} value
     * @param type the {@code type} value
     */
    public void assignTo(Mat m, int type) {
        n_assignTo(nativeObj, m.nativeObj, type);
    }

    // javadoc: Mat::assignTo(m)
    /**
     * Performs the {@code assignTo} operation.
     *
     * @param m the {@code m} value
     */
    public void assignTo(Mat m) {
        n_assignTo(nativeObj, m.nativeObj);
    }

    // javadoc: Mat::channels()
    /**
     * Performs the {@code channels} operation.
     *
     * @return the operation result
     */
    public int channels() {
        return n_channels(nativeObj);
    }

    // javadoc: Mat::checkVector(elemChannels, depth, requireContinuous)
    /**
     * Performs the {@code checkVector} operation.
     *
     * @param elemChannels the {@code elemChannels} value
     * @param depth the {@code depth} value
     * @param requireContinuous the {@code requireContinuous} value
     * @return the operation result
     */
    public int checkVector(int elemChannels, int depth, boolean requireContinuous) {
        return n_checkVector(nativeObj, elemChannels, depth, requireContinuous);
    }

    // javadoc: Mat::checkVector(elemChannels, depth)
    /**
     * Performs the {@code checkVector} operation.
     *
     * @param elemChannels the {@code elemChannels} value
     * @param depth the {@code depth} value
     * @return the operation result
     */
    public int checkVector(int elemChannels, int depth) {
        return n_checkVector(nativeObj, elemChannels, depth);
    }

    // javadoc: Mat::checkVector(elemChannels)
    /**
     * Performs the {@code checkVector} operation.
     *
     * @param elemChannels the {@code elemChannels} value
     * @return the operation result
     */
    public int checkVector(int elemChannels) {
        return n_checkVector(nativeObj, elemChannels);
    }

    // javadoc: Mat::clone()
    public Mat clone() {
        return new Mat(n_clone(nativeObj));
    }

    // javadoc: Mat::col(x)
    /**
     * Performs the {@code col} operation.
     *
     * @param x the {@code x} value
     * @return the operation result
     */
    public Mat col(int x) {
        return new Mat(n_col(nativeObj, x));
    }

    // javadoc: Mat::colRange(startcol, endcol)
    /**
     * Performs the {@code colRange} operation.
     *
     * @param startcol the {@code startcol} value
     * @param endcol the {@code endcol} value
     * @return the operation result
     */
    public Mat colRange(int startcol, int endcol) {
        return new Mat(n_colRange(nativeObj, startcol, endcol));
    }

    // javadoc: Mat::colRange(r)
    /**
     * Performs the {@code colRange} operation.
     *
     * @param r the {@code r} value
     * @return the operation result
     */
    public Mat colRange(Range r) {
        return new Mat(n_colRange(nativeObj, r.start, r.end));
    }

    // javadoc: Mat::dims()
    /**
     * Performs the {@code dims} operation.
     *
     * @return the operation result
     */
    public int dims() {
        return n_dims(nativeObj);
    }

    // javadoc: Mat::cols()
    /**
     * Performs the {@code cols} operation.
     *
     * @return the operation result
     */
    public int cols() {
        return n_cols(nativeObj);
    }

    // javadoc: Mat::convertTo(m, rtype, alpha, beta)
    /**
     * Performs the {@code convertTo} operation.
     *
     * @param m the {@code m} value
     * @param rtype the {@code rtype} value
     * @param alpha the {@code alpha} value
     * @param beta the {@code beta} value
     */
    public void convertTo(Mat m, int rtype, double alpha, double beta) {
        n_convertTo(nativeObj, m.nativeObj, rtype, alpha, beta);
    }

    // javadoc: Mat::convertTo(m, rtype, alpha)
    /**
     * Performs the {@code convertTo} operation.
     *
     * @param m the {@code m} value
     * @param rtype the {@code rtype} value
     * @param alpha the {@code alpha} value
     */
    public void convertTo(Mat m, int rtype, double alpha) {
        n_convertTo(nativeObj, m.nativeObj, rtype, alpha);
    }

    // javadoc: Mat::convertTo(m, rtype)
    /**
     * Performs the {@code convertTo} operation.
     *
     * @param m the {@code m} value
     * @param rtype the {@code rtype} value
     */
    public void convertTo(Mat m, int rtype) {
        n_convertTo(nativeObj, m.nativeObj, rtype);
    }

    // javadoc: Mat::copyTo(m)
    /**
     * Performs the {@code copyTo} operation.
     *
     * @param m the {@code m} value
     */
    public void copyTo(Mat m) {
        n_copyTo(nativeObj, m.nativeObj);
    }

    // javadoc: Mat::copyTo(m, mask)
    /**
     * Performs the {@code copyTo} operation.
     *
     * @param m the {@code m} value
     * @param mask the {@code mask} value
     */
    public void copyTo(Mat m, Mat mask) {
        n_copyTo(nativeObj, m.nativeObj, mask.nativeObj);
    }

    // javadoc: Mat::create(rows, cols, type)
    /**
     * Performs the {@code create} operation.
     *
     * @param rows the {@code rows} value
     * @param cols the {@code cols} value
     * @param type the {@code type} value
     */
    public void create(int rows, int cols, int type) {
        n_create(nativeObj, rows, cols, type);
    }

    // javadoc: Mat::create(size, type)
    /**
     * Performs the {@code create} operation.
     *
     * @param size the {@code size} value
     * @param type the {@code type} value
     */
    public void create(Size size, int type) {
        n_create(nativeObj, size.width, size.height, type);
    }

    // javadoc: Mat::create(sizes, type)
    /**
     * Performs the {@code create} operation.
     *
     * @param sizes the {@code sizes} value
     * @param type the {@code type} value
     */
    public void create(int[] sizes, int type) {
        n_create(nativeObj, sizes.length, sizes, type);
    }

    // javadoc: Mat::copySize(m)
    /**
     * Performs the {@code copySize} operation.
     *
     * @param m the {@code m} value
     */
    public void copySize(Mat m) {
        n_copySize(nativeObj, m.nativeObj);
    }

    // javadoc: Mat::cross(m)
    /**
     * Performs the {@code cross} operation.
     *
     * @param m the {@code m} value
     * @return the operation result
     */
    public Mat cross(Mat m) {
        return new Mat(n_cross(nativeObj, m.nativeObj));
    }

    // javadoc: Mat::dataAddr()
    /**
     * Performs the {@code dataAddr} operation.
     *
     * @return the operation result
     */
    public long dataAddr() {
        return n_dataAddr(nativeObj);
    }

    // javadoc: Mat::depth()
    /**
     * Performs the {@code depth} operation.
     *
     * @return the operation result
     */
    public int depth() {
        return n_depth(nativeObj);
    }

    // javadoc: Mat::diag(d)
    /**
     * Performs the {@code diag} operation.
     *
     * @param d the {@code d} value
     * @return the operation result
     */
    public Mat diag(int d) {
        return new Mat(n_diag(nativeObj, d));
    }

    // javadoc: Mat::diag()
    /**
     * Performs the {@code diag} operation.
     *
     * @return the operation result
     */
    public Mat diag() {
        return new Mat(n_diag(nativeObj, 0));
    }

    // javadoc: Mat::dot(m)
    /**
     * Performs the {@code dot} operation.
     *
     * @param m the {@code m} value
     * @return the operation result
     */
    public double dot(Mat m) {
        return n_dot(nativeObj, m.nativeObj);
    }

    // javadoc: Mat::elemSize()
    /**
     * Performs the {@code elemSize} operation.
     *
     * @return the operation result
     */
    public long elemSize() {
        return n_elemSize(nativeObj);
    }

    // javadoc: Mat::elemSize1()
    /**
     * Performs the {@code elemSize1} operation.
     *
     * @return the operation result
     */
    public long elemSize1() {
        return n_elemSize1(nativeObj);
    }

    // javadoc: Mat::empty()
    /**
     * Performs the {@code empty} operation.
     *
     * @return the operation result
     */
    public boolean empty() {
        return n_empty(nativeObj);
    }

    // javadoc: Mat::inv(method)
    /**
     * Performs the {@code inv} operation.
     *
     * @param method the {@code method} value
     * @return the operation result
     */
    public Mat inv(int method) {
        return new Mat(n_inv(nativeObj, method));
    }

    // javadoc: Mat::inv()
    /**
     * Performs the {@code inv} operation.
     *
     * @return the operation result
     */
    public Mat inv() {
        return new Mat(n_inv(nativeObj));
    }

    // javadoc: Mat::isContinuous()
    /**
     * Performs the {@code isContinuous} operation.
     *
     * @return the operation result
     */
    public boolean isContinuous() {
        return n_isContinuous(nativeObj);
    }

    // javadoc: Mat::isSubmatrix()
    /**
     * Performs the {@code isSubmatrix} operation.
     *
     * @return the operation result
     */
    public boolean isSubmatrix() {
        return n_isSubmatrix(nativeObj);
    }

    // javadoc: Mat::locateROI(wholeSize, ofs)
    /**
     * Performs the {@code locateROI} operation.
     *
     * @param wholeSize the {@code wholeSize} value
     * @param ofs the {@code ofs} value
     */
    public void locateROI(Size wholeSize, Point ofs) {
        double[] wholeSize_out = new double[2];
        double[] ofs_out = new double[2];
        locateROI_0(nativeObj, wholeSize_out, ofs_out);
        if (wholeSize != null) {
            wholeSize.width = wholeSize_out[0];
            wholeSize.height = wholeSize_out[1];
        }
        if (ofs != null) {
            ofs.x = ofs_out[0];
            ofs.y = ofs_out[1];
        }
    }

    /**
     * Element-wise multiplication with scale factor
     *
     * @param m     operand with with which to perform element-wise multiplication
     * @param scale scale factor
     * @return reference to a new Mat object
     */
    public Mat mul(Mat m, double scale) {
        return new Mat(n_mul(nativeObj, m.nativeObj, scale));
    }

    /**
     * Element-wise multiplication
     *
     * @param m operand with with which to perform element-wise multiplication
     * @return reference to a new Mat object
     */
    public Mat mul(Mat m) {
        return new Mat(n_mul(nativeObj, m.nativeObj));
    }

    /**
     * Matrix multiplication
     *
     * @param m operand with with which to perform matrix multiplication
     * @return reference to a new Mat object
     * @see Core#gemm(Mat, Mat, double, Mat, double, Mat, int)
     */
    public Mat matMul(Mat m) {
        return new Mat(n_matMul(nativeObj, m.nativeObj));
    }

    // javadoc: Mat::push_back(m)
    /**
     * Performs the {@code push_back} operation.
     *
     * @param m the {@code m} value
     */
    public void push_back(Mat m) {
        n_push_back(nativeObj, m.nativeObj);
    }

    // javadoc: Mat::release()
    /**
     * Performs the {@code release} operation.
     */
    public void release() {
        n_release(nativeObj);
    }

    // javadoc: Mat::reshape(cn, rows)
    /**
     * Performs the {@code reshape} operation.
     *
     * @param cn the {@code cn} value
     * @param rows the {@code rows} value
     * @return the operation result
     */
    public Mat reshape(int cn, int rows) {
        return new Mat(n_reshape(nativeObj, cn, rows));
    }

    // javadoc: Mat::reshape(cn)
    /**
     * Performs the {@code reshape} operation.
     *
     * @param cn the {@code cn} value
     * @return the operation result
     */
    public Mat reshape(int cn) {
        return new Mat(n_reshape(nativeObj, cn));
    }

    // javadoc: Mat::reshape(cn, newshape)
    /**
     * Performs the {@code reshape} operation.
     *
     * @param cn the {@code cn} value
     * @param newshape the {@code newshape} value
     * @return the operation result
     */
    public Mat reshape(int cn, int[] newshape) {
        return new Mat(n_reshape_1(nativeObj, cn, newshape.length, newshape));
    }

    // javadoc: Mat::row(y)
    /**
     * Performs the {@code row} operation.
     *
     * @param y the {@code y} value
     * @return the operation result
     */
    public Mat row(int y) {
        return new Mat(n_row(nativeObj, y));
    }

    // javadoc: Mat::rowRange(startrow, endrow)
    /**
     * Performs the {@code rowRange} operation.
     *
     * @param startrow the {@code startrow} value
     * @param endrow the {@code endrow} value
     * @return the operation result
     */
    public Mat rowRange(int startrow, int endrow) {
        return new Mat(n_rowRange(nativeObj, startrow, endrow));
    }

    // javadoc: Mat::rowRange(r)
    /**
     * Performs the {@code rowRange} operation.
     *
     * @param r the {@code r} value
     * @return the operation result
     */
    public Mat rowRange(Range r) {
        return new Mat(n_rowRange(nativeObj, r.start, r.end));
    }

    // javadoc: Mat::rows()
    /**
     * Performs the {@code rows} operation.
     *
     * @return the operation result
     */
    public int rows() {
        return n_rows(nativeObj);
    }

    // javadoc: Mat::operator =(s)
    /**
     * Performs the {@code setTo} operation.
     *
     * @param s the {@code s} value
     * @return the operation result
     */
    public Mat setTo(Scalar s) {
        return new Mat(n_setTo(nativeObj, s.val[0], s.val[1], s.val[2], s.val[3]));
    }

    // javadoc: Mat::setTo(value, mask)
    /**
     * Performs the {@code setTo} operation.
     *
     * @param value the {@code value} value
     * @param mask the {@code mask} value
     * @return the operation result
     */
    public Mat setTo(Scalar value, Mat mask) {
        return new Mat(n_setTo(nativeObj, value.val[0], value.val[1], value.val[2], value.val[3], mask.nativeObj));
    }

    // javadoc: Mat::setTo(value, mask)
    /**
     * Performs the {@code setTo} operation.
     *
     * @param value the {@code value} value
     * @param mask the {@code mask} value
     * @return the operation result
     */
    public Mat setTo(Mat value, Mat mask) {
        return new Mat(n_setTo(nativeObj, value.nativeObj, mask.nativeObj));
    }

    // javadoc: Mat::setTo(value)
    /**
     * Performs the {@code setTo} operation.
     *
     * @param value the {@code value} value
     * @return the operation result
     */
    public Mat setTo(Mat value) {
        return new Mat(n_setTo(nativeObj, value.nativeObj));
    }

    // javadoc: Mat::size()
    /**
     * Performs the {@code size} operation.
     *
     * @return the operation result
     */
    public Size size() {
        return new Size(n_size(nativeObj));
    }

    // javadoc: Mat::size(int i)
    /**
     * Performs the {@code size} operation.
     *
     * @param i the {@code i} value
     * @return the operation result
     */
    public int size(int i) {
        return n_size_i(nativeObj, i);
    }

    // javadoc: Mat::step1(i)
    /**
     * Performs the {@code step1} operation.
     *
     * @param i the {@code i} value
     * @return the operation result
     */
    public long step1(int i) {
        return n_step1(nativeObj, i);
    }

    // javadoc: Mat::step1()
    /**
     * Performs the {@code step1} operation.
     *
     * @return the operation result
     */
    public long step1() {
        return n_step1(nativeObj);
    }

    // javadoc: Mat::operator()(rowStart, rowEnd, colStart, colEnd)
    /**
     * Performs the {@code submat} operation.
     *
     * @param rowStart the {@code rowStart} value
     * @param rowEnd the {@code rowEnd} value
     * @param colStart the {@code colStart} value
     * @param colEnd the {@code colEnd} value
     * @return the operation result
     */
    public Mat submat(int rowStart, int rowEnd, int colStart, int colEnd) {
        return new Mat(n_submat_rr(nativeObj, rowStart, rowEnd, colStart, colEnd));
    }

    // javadoc: Mat::operator()(rowRange, colRange)
    /**
     * Performs the {@code submat} operation.
     *
     * @param rowRange the {@code rowRange} value
     * @param colRange the {@code colRange} value
     * @return the operation result
     */
    public Mat submat(Range rowRange, Range colRange) {
        return new Mat(n_submat_rr(nativeObj, rowRange.start, rowRange.end, colRange.start, colRange.end));
    }

    // javadoc: Mat::operator()(ranges[])
    /**
     * Performs the {@code submat} operation.
     *
     * @param ranges the {@code ranges} value
     * @return the operation result
     */
    public Mat submat(Range[] ranges) {
        return new Mat(n_submat_ranges(nativeObj, ranges));
    }

    // javadoc: Mat::operator()(roi)
    /**
     * Performs the {@code submat} operation.
     *
     * @param roi the {@code roi} value
     * @return the operation result
     */
    public Mat submat(Rect roi) {
        return new Mat(n_submat(nativeObj, roi.x, roi.y, roi.width, roi.height));
    }

    // javadoc: Mat::t()
    /**
     * Performs the {@code t} operation.
     *
     * @return the operation result
     */
    public Mat t() {
        return new Mat(n_t(nativeObj));
    }

    // javadoc: Mat::total()
    /**
     * Performs the {@code total} operation.
     *
     * @return the operation result
     */
    public long total() {
        return n_total(nativeObj);
    }

    // javadoc: Mat::type()
    /**
     * Performs the {@code type} operation.
     *
     * @return the operation result
     */
    public int type() {
        return n_type(nativeObj);
    }

    // javadoc:Mat::toString()
    @Override
    public String toString() {
        String _dims = (dims() > 0) ? "" : "-1*-1*";
        for (int i = 0; i < dims(); i++) {
            _dims += size(i) + "*";
        }
        return "Mat [ " + _dims + CvType.typeToString(type()) + ", isCont=" + isContinuous() + ", isSubmat="
                + isSubmatrix() + ", nativeObj=0x" + Long.toHexString(nativeObj) + ", dataAddr=0x"
                + Long.toHexString(dataAddr()) + " ]";
    }

    // javadoc:Mat::dump()
    /**
     * Performs the {@code dump} operation.
     *
     * @return the operation result
     */
    public String dump() {
        return nDump(nativeObj);
    }

    // javadoc:Mat::put(row,col,data)
    /**
     * Performs the {@code put} operation.
     *
     * @param row the {@code row} value
     * @param col the {@code col} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int put(int row, int col, double... data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        return nPutD(nativeObj, row, col, data.length, data);
    }

    // javadoc:Mat::put(idx,data)
    /**
     * Performs the {@code put} operation.
     *
     * @param idx the {@code idx} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int put(int[] idx, double... data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (idx.length != dims())
            throw new IllegalArgumentException("Incorrect number of indices");
        return nPutDIdx(nativeObj, idx, data.length, data);
    }

    // javadoc:Mat::put(row,col,data)
    /**
     * Performs the {@code put} operation.
     *
     * @param row the {@code row} value
     * @param col the {@code col} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int put(int row, int col, float[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_32F) {
            return nPutF(nativeObj, row, col, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::put(idx,data)
    /**
     * Performs the {@code put} operation.
     *
     * @param idx the {@code idx} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int put(int[] idx, float[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (idx.length != dims())
            throw new IllegalArgumentException("Incorrect number of indices");
        if (CvType.depth(t) == CvType.CV_32F) {
            return nPutFIdx(nativeObj, idx, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::put(row,col,data)
    /**
     * Performs the {@code put} operation.
     *
     * @param row the {@code row} value
     * @param col the {@code col} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int put(int row, int col, int[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_32S) {
            return nPutI(nativeObj, row, col, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::put(idx,data)
    /**
     * Performs the {@code put} operation.
     *
     * @param idx the {@code idx} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int put(int[] idx, int[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (idx.length != dims())
            throw new IllegalArgumentException("Incorrect number of indices");
        if (CvType.depth(t) == CvType.CV_32S) {
            return nPutIIdx(nativeObj, idx, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::put(row,col,data)
    /**
     * Performs the {@code put} operation.
     *
     * @param row the {@code row} value
     * @param col the {@code col} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int put(int row, int col, short[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_16U || CvType.depth(t) == CvType.CV_16S) {
            return nPutS(nativeObj, row, col, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::put(idx,data)
    /**
     * Performs the {@code put} operation.
     *
     * @param idx the {@code idx} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int put(int[] idx, short[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (idx.length != dims())
            throw new IllegalArgumentException("Incorrect number of indices");
        if (CvType.depth(t) == CvType.CV_16U || CvType.depth(t) == CvType.CV_16S) {
            return nPutSIdx(nativeObj, idx, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::put(row,col,data)
    /**
     * Performs the {@code put} operation.
     *
     * @param row the {@code row} value
     * @param col the {@code col} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int put(int row, int col, byte[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_8U || CvType.depth(t) == CvType.CV_8S) {
            return nPutB(nativeObj, row, col, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::put(idx,data)
    /**
     * Performs the {@code put} operation.
     *
     * @param idx the {@code idx} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int put(int[] idx, byte[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (idx.length != dims())
            throw new IllegalArgumentException("Incorrect number of indices");
        if (CvType.depth(t) == CvType.CV_8U || CvType.depth(t) == CvType.CV_8S) {
            return nPutBIdx(nativeObj, idx, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::put(row,col,data,offset,length)
    /**
     * Performs the {@code put} operation.
     *
     * @param row the {@code row} value
     * @param col the {@code col} value
     * @param data the {@code data} value
     * @param offset the {@code offset} value
     * @param length the {@code length} value
     * @return the operation result
     */
    public int put(int row, int col, byte[] data, int offset, int length) {
        int t = type();
        if (data == null || length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_8U || CvType.depth(t) == CvType.CV_8S) {
            return nPutBwOffset(nativeObj, row, col, length, offset, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::put(idx,data,offset,length)
    /**
     * Performs the {@code put} operation.
     *
     * @param idx the {@code idx} value
     * @param data the {@code data} value
     * @param offset the {@code offset} value
     * @param length the {@code length} value
     * @return the operation result
     */
    public int put(int[] idx, byte[] data, int offset, int length) {
        int t = type();
        if (data == null || length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (idx.length != dims())
            throw new IllegalArgumentException("Incorrect number of indices");
        if (CvType.depth(t) == CvType.CV_8U || CvType.depth(t) == CvType.CV_8S) {
            return nPutBwIdxOffset(nativeObj, idx, length, offset, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::get(row,col,data)
    /**
     * Performs the {@code get} operation.
     *
     * @param row the {@code row} value
     * @param col the {@code col} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int get(int row, int col, byte[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_8U || CvType.depth(t) == CvType.CV_8S) {
            return nGetB(nativeObj, row, col, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::get(idx,data)
    /**
     * Performs the {@code get} operation.
     *
     * @param idx the {@code idx} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int get(int[] idx, byte[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (idx.length != dims())
            throw new IllegalArgumentException("Incorrect number of indices");
        if (CvType.depth(t) == CvType.CV_8U || CvType.depth(t) == CvType.CV_8S) {
            return nGetBIdx(nativeObj, idx, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::get(row,col,data)
    /**
     * Performs the {@code get} operation.
     *
     * @param row the {@code row} value
     * @param col the {@code col} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int get(int row, int col, short[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_16U || CvType.depth(t) == CvType.CV_16S) {
            return nGetS(nativeObj, row, col, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::get(idx,data)
    /**
     * Performs the {@code get} operation.
     *
     * @param idx the {@code idx} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int get(int[] idx, short[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (idx.length != dims())
            throw new IllegalArgumentException("Incorrect number of indices");
        if (CvType.depth(t) == CvType.CV_16U || CvType.depth(t) == CvType.CV_16S) {
            return nGetSIdx(nativeObj, idx, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::get(row,col,data)
    /**
     * Performs the {@code get} operation.
     *
     * @param row the {@code row} value
     * @param col the {@code col} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int get(int row, int col, int[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_32S) {
            return nGetI(nativeObj, row, col, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::get(idx,data)
    /**
     * Performs the {@code get} operation.
     *
     * @param idx the {@code idx} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int get(int[] idx, int[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (idx.length != dims())
            throw new IllegalArgumentException("Incorrect number of indices");
        if (CvType.depth(t) == CvType.CV_32S) {
            return nGetIIdx(nativeObj, idx, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::get(row,col,data)
    /**
     * Performs the {@code get} operation.
     *
     * @param row the {@code row} value
     * @param col the {@code col} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int get(int row, int col, float[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_32F) {
            return nGetF(nativeObj, row, col, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::get(idx,data)
    /**
     * Performs the {@code get} operation.
     *
     * @param idx the {@code idx} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int get(int[] idx, float[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (idx.length != dims())
            throw new IllegalArgumentException("Incorrect number of indices");
        if (CvType.depth(t) == CvType.CV_32F) {
            return nGetFIdx(nativeObj, idx, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::get(row,col,data)
    /**
     * Performs the {@code get} operation.
     *
     * @param row the {@code row} value
     * @param col the {@code col} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int get(int row, int col, double[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_64F) {
            return nGetD(nativeObj, row, col, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::get(idx,data)
    /**
     * Performs the {@code get} operation.
     *
     * @param idx the {@code idx} value
     * @param data the {@code data} value
     * @return the operation result
     */
    public int get(int[] idx, double[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        if (idx.length != dims())
            throw new IllegalArgumentException("Incorrect number of indices");
        if (CvType.depth(t) == CvType.CV_64F) {
            return nGetDIdx(nativeObj, idx, data.length, data);
        }
        throw new UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    // javadoc:Mat::get(row,col)
    /**
     * Performs the {@code get} operation.
     *
     * @param row the {@code row} value
     * @param col the {@code col} value
     * @return the operation result
     */
    public double[] get(int row, int col) {
        return nGet(nativeObj, row, col);
    }

    // javadoc:Mat::get(idx)
    /**
     * Performs the {@code get} operation.
     *
     * @param idx the {@code idx} value
     * @return the operation result
     */
    public double[] get(int[] idx) {
        if (idx.length != dims())
            throw new IllegalArgumentException("Incorrect number of indices");
        return nGetIdx(nativeObj, idx);
    }

    // javadoc:Mat::height()
    /**
     * Performs the {@code height} operation.
     *
     * @return the operation result
     */
    public int height() {
        return rows();
    }

    // javadoc:Mat::width()
    /**
     * Performs the {@code width} operation.
     *
     * @return the operation result
     */
    public int width() {
        return cols();
    }

    // javadoc:Mat::at(clazz, row, col)
    /**
     * Performs the {@code at} operation.
     *
     * @param <T> the generic value type
     * @param clazz the {@code clazz} value
     * @param row the {@code row} value
     * @param col the {@code col} value
     * @return the operation result
     */
    @SuppressWarnings("unchecked")
    public <T> Atable<T> at(Class<T> clazz, int row, int col) {
        if (clazz == Byte.class || clazz == byte.class) {
            return (Atable<T>) new AtableByte(this, row, col);
        } else if (clazz == Double.class || clazz == double.class) {
            return (Atable<T>) new AtableDouble(this, row, col);
        } else if (clazz == Float.class || clazz == float.class) {
            return (Atable<T>) new AtableFloat(this, row, col);
        } else if (clazz == Integer.class || clazz == int.class) {
            return (Atable<T>) new AtableInteger(this, row, col);
        } else if (clazz == Short.class || clazz == short.class) {
            return (Atable<T>) new AtableShort(this, row, col);
        } else {
            throw new RuntimeException("Unsupported class type");
        }
    }

    // javadoc:Mat::at(clazz, idx)
    /**
     * Performs the {@code at} operation.
     *
     * @param <T> the generic value type
     * @param clazz the {@code clazz} value
     * @param idx the {@code idx} value
     * @return the operation result
     */
    @SuppressWarnings("unchecked")
    public <T> Atable<T> at(Class<T> clazz, int[] idx) {
        if (clazz == Byte.class || clazz == byte.class) {
            return (Atable<T>) new AtableByte(this, idx);
        } else if (clazz == Double.class || clazz == double.class) {
            return (Atable<T>) new AtableDouble(this, idx);
        } else if (clazz == Float.class || clazz == float.class) {
            return (Atable<T>) new AtableFloat(this, idx);
        } else if (clazz == Integer.class || clazz == int.class) {
            return (Atable<T>) new AtableInteger(this, idx);
        } else if (clazz == Short.class || clazz == short.class) {
            return (Atable<T>) new AtableShort(this, idx);
        } else {
            throw new RuntimeException("Unsupported class parameter");
        }
    }

    // javadoc:Mat::getNativeObjAddr()
    /**
     * Performs the {@code getNativeObjAddr} operation.
     *
     * @return the operation result
     */
    public long getNativeObjAddr() {
        return nativeObj;
    }

    /**
     * Provides the {@code Atable} API.
     *
     * @param <T> the generic value type
     */
    public interface Atable<T> {

        /**
         * Performs the {@code getV} operation.
         *
         * @return the operation result
         */
        T getV();

        /**
         * Performs the {@code setV} operation.
         *
         * @param v the {@code v} value
         */
        void setV(T v);

        /**
         * Performs the {@code getV2c} operation.
         *
         * @return the operation result
         */
        Tuple2<T> getV2c();

        /**
         * Performs the {@code setV2c} operation.
         *
         * @param v the {@code v} value
         */
        void setV2c(Tuple2<T> v);

        /**
         * Performs the {@code getV3c} operation.
         *
         * @return the operation result
         */
        Tuple3<T> getV3c();

        /**
         * Performs the {@code setV3c} operation.
         *
         * @param v the {@code v} value
         */
        void setV3c(Tuple3<T> v);

        /**
         * Performs the {@code getV4c} operation.
         *
         * @return the operation result
         */
        Tuple4<T> getV4c();

        /**
         * Performs the {@code setV4c} operation.
         *
         * @param v the {@code v} value
         */
        void setV4c(Tuple4<T> v);
    }

    /**
     * Provides the {@code Tuple2} API.
     *
     * @param <T> the generic value type
     */
    public static class Tuple2<T> {

        private final T _0;
        private final T _1;

        /**
         * Creates a new {@code Tuple2} instance.
         *
         * @param _0 the {@code _0} value
         * @param _1 the {@code _1} value
         */
        public Tuple2(T _0, T _1) {
            this._0 = _0;
            this._1 = _1;
        }

        /**
         * Performs the {@code get_0} operation.
         *
         * @return the operation result
         */
        public T get_0() {
            return _0;
        }

        /**
         * Performs the {@code get_1} operation.
         *
         * @return the operation result
         */
        public T get_1() {
            return _1;
        }
    }

    /**
     * Provides the {@code Tuple3} API.
     *
     * @param <T> the generic value type
     */
    public static class Tuple3<T> {

        private final T _0;
        private final T _1;
        private final T _2;

        /**
         * Creates a new {@code Tuple3} instance.
         *
         * @param _0 the {@code _0} value
         * @param _1 the {@code _1} value
         * @param _2 the {@code _2} value
         */
        public Tuple3(T _0, T _1, T _2) {
            this._0 = _0;
            this._1 = _1;
            this._2 = _2;
        }

        /**
         * Performs the {@code get_0} operation.
         *
         * @return the operation result
         */
        public T get_0() {
            return _0;
        }

        /**
         * Performs the {@code get_1} operation.
         *
         * @return the operation result
         */
        public T get_1() {
            return _1;
        }

        /**
         * Performs the {@code get_2} operation.
         *
         * @return the operation result
         */
        public T get_2() {
            return _2;
        }
    }

    /**
     * Provides the {@code Tuple4} API.
     *
     * @param <T> the generic value type
     */
    public static class Tuple4<T> {

        private final T _0;
        private final T _1;
        private final T _2;
        private final T _3;

        /**
         * Creates a new {@code Tuple4} instance.
         *
         * @param _0 the {@code _0} value
         * @param _1 the {@code _1} value
         * @param _2 the {@code _2} value
         * @param _3 the {@code _3} value
         */
        public Tuple4(T _0, T _1, T _2, T _3) {
            this._0 = _0;
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
        }

        /**
         * Performs the {@code get_0} operation.
         *
         * @return the operation result
         */
        public T get_0() {
            return _0;
        }

        /**
         * Performs the {@code get_1} operation.
         *
         * @return the operation result
         */
        public T get_1() {
            return _1;
        }

        /**
         * Performs the {@code get_2} operation.
         *
         * @return the operation result
         */
        public T get_2() {
            return _2;
        }

        /**
         * Performs the {@code get_3} operation.
         *
         * @return the operation result
         */
        public T get_3() {
            return _3;
        }
    }

    private static class AtableBase {

        protected final Mat mat;
        protected final int[] indices;

        protected AtableBase(Mat mat, int row, int col) {
            this.mat = mat;
            indices = new int[2];
            indices[0] = row;
            indices[1] = col;
        }

        protected AtableBase(Mat mat, int[] indices) {
            this.mat = mat;
            this.indices = indices;
        }
    }

    private static class AtableByte extends AtableBase implements Atable<Byte> {

        public AtableByte(Mat mat, int row, int col) {
            super(mat, row, col);
        }

        public AtableByte(Mat mat, int[] indices) {
            super(mat, indices);
        }

        @Override
        public Byte getV() {
            byte[] data = new byte[1];
            mat.get(indices, data);
            return data[0];
        }

        @Override
        public void setV(Byte v) {
            byte[] data = new byte[] { v };
            mat.put(indices, data);
        }

        @Override
        public Tuple2<Byte> getV2c() {
            byte[] data = new byte[2];
            mat.get(indices, data);
            return new Tuple2<Byte>(data[0], data[1]);
        }

        @Override
        public void setV2c(Tuple2<Byte> v) {
            byte[] data = new byte[] { v._0, v._1 };
            mat.put(indices, data);
        }

        @Override
        public Tuple3<Byte> getV3c() {
            byte[] data = new byte[3];
            mat.get(indices, data);
            return new Tuple3<Byte>(data[0], data[1], data[2]);
        }

        @Override
        public void setV3c(Tuple3<Byte> v) {
            byte[] data = new byte[] { v._0, v._1, v._2 };
            mat.put(indices, data);
        }

        @Override
        public Tuple4<Byte> getV4c() {
            byte[] data = new byte[4];
            mat.get(indices, data);
            return new Tuple4<Byte>(data[0], data[1], data[2], data[3]);
        }

        @Override
        public void setV4c(Tuple4<Byte> v) {
            byte[] data = new byte[] { v._0, v._1, v._2, v._3 };
            mat.put(indices, data);
        }
    }

    private static class AtableDouble extends AtableBase implements Atable<Double> {

        public AtableDouble(Mat mat, int row, int col) {
            super(mat, row, col);
        }

        public AtableDouble(Mat mat, int[] indices) {
            super(mat, indices);
        }

        @Override
        public Double getV() {
            double[] data = new double[1];
            mat.get(indices, data);
            return data[0];
        }

        @Override
        public void setV(Double v) {
            double[] data = new double[] { v };
            mat.put(indices, data);
        }

        @Override
        public Tuple2<Double> getV2c() {
            double[] data = new double[2];
            mat.get(indices, data);
            return new Tuple2<Double>(data[0], data[1]);
        }

        @Override
        public void setV2c(Tuple2<Double> v) {
            double[] data = new double[] { v._0, v._1 };
            mat.put(indices, data);
        }

        @Override
        public Tuple3<Double> getV3c() {
            double[] data = new double[3];
            mat.get(indices, data);
            return new Tuple3<Double>(data[0], data[1], data[2]);
        }

        @Override
        public void setV3c(Tuple3<Double> v) {
            double[] data = new double[] { v._0, v._1, v._2 };
            mat.put(indices, data);
        }

        @Override
        public Tuple4<Double> getV4c() {
            double[] data = new double[4];
            mat.get(indices, data);
            return new Tuple4<Double>(data[0], data[1], data[2], data[3]);
        }

        @Override
        public void setV4c(Tuple4<Double> v) {
            double[] data = new double[] { v._0, v._1, v._2, v._3 };
            mat.put(indices, data);
        }
    }

    private static class AtableFloat extends AtableBase implements Atable<Float> {

        public AtableFloat(Mat mat, int row, int col) {
            super(mat, row, col);
        }

        public AtableFloat(Mat mat, int[] indices) {
            super(mat, indices);
        }

        @Override
        public Float getV() {
            float[] data = new float[1];
            mat.get(indices, data);
            return data[0];
        }

        @Override
        public void setV(Float v) {
            float[] data = new float[] { v };
            mat.put(indices, data);
        }

        @Override
        public Tuple2<Float> getV2c() {
            float[] data = new float[2];
            mat.get(indices, data);
            return new Tuple2<Float>(data[0], data[1]);
        }

        @Override
        public void setV2c(Tuple2<Float> v) {
            float[] data = new float[] { v._0, v._1 };
            mat.put(indices, data);
        }

        @Override
        public Tuple3<Float> getV3c() {
            float[] data = new float[3];
            mat.get(indices, data);
            return new Tuple3<Float>(data[0], data[1], data[2]);
        }

        @Override
        public void setV3c(Tuple3<Float> v) {
            float[] data = new float[] { v._0, v._1, v._2 };
            mat.put(indices, data);
        }

        @Override
        public Tuple4<Float> getV4c() {
            float[] data = new float[4];
            mat.get(indices, data);
            return new Tuple4<Float>(data[0], data[1], data[2], data[3]);
        }

        @Override
        public void setV4c(Tuple4<Float> v) {
            double[] data = new double[] { v._0, v._1, v._2, v._3 };
            mat.put(indices, data);
        }
    }

    private static class AtableInteger extends AtableBase implements Atable<Integer> {

        public AtableInteger(Mat mat, int row, int col) {
            super(mat, row, col);
        }

        public AtableInteger(Mat mat, int[] indices) {
            super(mat, indices);
        }

        @Override
        public Integer getV() {
            int[] data = new int[1];
            mat.get(indices, data);
            return data[0];
        }

        @Override
        public void setV(Integer v) {
            int[] data = new int[] { v };
            mat.put(indices, data);
        }

        @Override
        public Tuple2<Integer> getV2c() {
            int[] data = new int[2];
            mat.get(indices, data);
            return new Tuple2<Integer>(data[0], data[1]);
        }

        @Override
        public void setV2c(Tuple2<Integer> v) {
            int[] data = new int[] { v._0, v._1 };
            mat.put(indices, data);
        }

        @Override
        public Tuple3<Integer> getV3c() {
            int[] data = new int[3];
            mat.get(indices, data);
            return new Tuple3<Integer>(data[0], data[1], data[2]);
        }

        @Override
        public void setV3c(Tuple3<Integer> v) {
            int[] data = new int[] { v._0, v._1, v._2 };
            mat.put(indices, data);
        }

        @Override
        public Tuple4<Integer> getV4c() {
            int[] data = new int[4];
            mat.get(indices, data);
            return new Tuple4<Integer>(data[0], data[1], data[2], data[3]);
        }

        @Override
        public void setV4c(Tuple4<Integer> v) {
            int[] data = new int[] { v._0, v._1, v._2, v._3 };
            mat.put(indices, data);
        }
    }

    private static class AtableShort extends AtableBase implements Atable<Short> {

        public AtableShort(Mat mat, int row, int col) {
            super(mat, row, col);
        }

        public AtableShort(Mat mat, int[] indices) {
            super(mat, indices);
        }

        @Override
        public Short getV() {
            short[] data = new short[1];
            mat.get(indices, data);
            return data[0];
        }

        @Override
        public void setV(Short v) {
            short[] data = new short[] { v };
            mat.put(indices, data);
        }

        @Override
        public Tuple2<Short> getV2c() {
            short[] data = new short[2];
            mat.get(indices, data);
            return new Tuple2<Short>(data[0], data[1]);
        }

        @Override
        public void setV2c(Tuple2<Short> v) {
            short[] data = new short[] { v._0, v._1 };
            mat.put(indices, data);
        }

        @Override
        public Tuple3<Short> getV3c() {
            short[] data = new short[3];
            mat.get(indices, data);
            return new Tuple3<Short>(data[0], data[1], data[2]);
        }

        @Override
        public void setV3c(Tuple3<Short> v) {
            short[] data = new short[] { v._0, v._1, v._2 };
            mat.put(indices, data);
        }

        @Override
        public Tuple4<Short> getV4c() {
            short[] data = new short[4];
            mat.get(indices, data);
            return new Tuple4<Short>(data[0], data[1], data[2], data[3]);
        }

        @Override
        public void setV4c(Tuple4<Short> v) {
            short[] data = new short[] { v._0, v._1, v._2, v._3 };
            mat.put(indices, data);
        }
    }
}
