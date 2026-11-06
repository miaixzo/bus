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
package org.miaixz.bus.opencv.core;

import java.nio.ByteBuffer;

/**
 * A multidimensional matrix backed by native storage. Matrix views may share pixel storage; release a wrapper when it
 * is no longer needed. A Java wrapper does not make concurrent modifications to shared storage safe.
 */
public class Mat extends CleanableMat {

    /**
     * Wraps an existing native Mat handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; must be nonzero and must not be owned by another cleanup wrapper
     * @throws UnsupportedOperationException if the native binding handle is zero
     */
    public Mat(long addr) {
        super(addr);
    }

    /**
     * Creates an empty native matrix header.
     */
    public Mat() {
        super(n_Mat());
    }

    /**
     * Creates a native matrix with the supplied dimensions and element type.
     *
     * @param rows number of matrix rows
     * @param cols number of matrix columns
     * @param type packed matrix channel-depth and channel-count code
     */
    public Mat(int rows, int cols, int type) {
        super(n_Mat(rows, cols, type));
    }

    /**
     * Creates a matrix header over the supplied direct buffer. The buffer must remain valid while the matrix accesses
     * its storage.
     *
     * @param rows number of matrix rows
     * @param cols number of matrix columns
     * @param type packed matrix channel-depth and channel-count code
     * @param data array or buffer containing matrix channel values
     */
    public Mat(int rows, int cols, int type, ByteBuffer data) {
        super(n_Mat(rows, cols, type, data));
    }

    /**
     * Creates a matrix header over the supplied direct buffer. The buffer must remain valid while the matrix accesses
     * its storage.
     *
     * @param rows number of matrix rows
     * @param cols number of matrix columns
     * @param type packed matrix channel-depth and channel-count code
     * @param data array or buffer containing matrix channel values
     * @param step row stride in bytes; the caller-provided buffer must cover the addressed storage
     */
    public Mat(int rows, int cols, int type, ByteBuffer data, long step) {
        super(n_Mat(rows, cols, type, data, step));
    }

    /**
     * Creates a native matrix with the supplied dimensions and element type.
     *
     * @param size width and height of the requested region or matrix
     * @param type packed matrix channel-depth and channel-count code
     * @throws NullPointerException if size is null
     */
    public Mat(Size size, int type) {
        super(n_Mat(size.width, size.height, type));
    }

    /**
     * Creates a native matrix with the supplied dimensions and element type.
     *
     * @param sizes extent of each matrix dimension
     * @param type  packed matrix channel-depth and channel-count code
     * @throws NullPointerException if sizes is null
     */
    public Mat(int[] sizes, int type) {
        super(n_Mat(sizes.length, sizes, type));
    }

    /**
     * Creates a native matrix with the supplied dimensions and element type, initialized to the supplied channel
     * values.
     *
     * @param rows number of matrix rows
     * @param cols number of matrix columns
     * @param type packed matrix channel-depth and channel-count code
     * @param s    channel values used to initialize or update the matrix
     * @throws NullPointerException if s is null
     */
    public Mat(int rows, int cols, int type, Scalar s) {
        super(n_Mat(rows, cols, type, s.val[0], s.val[1], s.val[2], s.val[3]));
    }

    /**
     * Creates a native matrix with the supplied dimensions and element type, initialized to the supplied channel
     * values.
     *
     * @param size width and height of the requested region or matrix
     * @param type packed matrix channel-depth and channel-count code
     * @param s    channel values used to initialize or update the matrix
     * @throws NullPointerException if size, s is null
     */
    public Mat(Size size, int type, Scalar s) {
        super(n_Mat(size.width, size.height, type, s.val[0], s.val[1], s.val[2], s.val[3]));
    }

    /**
     * Creates a native matrix with the supplied dimensions and element type, initialized to the supplied channel
     * values.
     *
     * @param sizes extent of each matrix dimension
     * @param type  packed matrix channel-depth and channel-count code
     * @param s     channel values used to initialize or update the matrix
     * @throws NullPointerException if sizes, s is null
     */
    public Mat(int[] sizes, int type, Scalar s) {
        super(n_Mat(sizes.length, sizes, type, s.val[0], s.val[1], s.val[2], s.val[3]));
    }

    /**
     * Creates a matrix view over the selected source region. The view shares the source storage.
     *
     * @param m        parent matrix whose storage is shared by the selected region
     * @param rowRange half-open range of rows to select
     * @param colRange half-open range of columns to select
     * @throws NullPointerException if m, rowRange, colRange is null
     */
    public Mat(Mat m, Range rowRange, Range colRange) {
        super(n_Mat(m.nativeObj, rowRange.start, rowRange.end, colRange.start, colRange.end));
    }

    /**
     * Creates a matrix view over the selected source region. The view shares the source storage.
     *
     * @param m        parent matrix whose storage is shared by the selected region
     * @param rowRange half-open range of rows to select
     * @throws NullPointerException if m, rowRange is null
     */
    public Mat(Mat m, Range rowRange) {
        super(n_Mat(m.nativeObj, rowRange.start, rowRange.end));
    }

    /**
     * Creates a matrix view over the selected source region. The view shares the source storage.
     *
     * @param m      parent matrix whose storage is shared by the selected region
     * @param ranges half-open range to select along each dimension
     * @throws NullPointerException if m is null
     */
    public Mat(Mat m, Range[] ranges) {
        super(n_Mat(m.nativeObj, ranges));
    }

    /**
     * Creates a matrix view over the selected source region. The view shares the source storage.
     *
     * @param m   parent matrix whose storage is shared by the selected region
     * @param roi rectangle identifying the region of interest
     * @throws NullPointerException if m, roi is null
     */
    public Mat(Mat m, Rect roi) {
        super(n_Mat(m.nativeObj, roi.y, roi.y + roi.height, roi.x, roi.x + roi.width));
    }

    /**
     * Adjusts the boundaries of this matrix view within its parent matrix.
     *
     * @param dtop    number of rows by which to expand the top boundary; negative values shrink it
     * @param dbottom number of rows by which to expand the bottom boundary; negative values shrink it
     * @param dleft   number of columns by which to expand the left boundary; negative values shrink it
     * @param dright  number of columns by which to expand the right boundary; negative values shrink it
     * @return a new matrix header for the adjusted region, sharing this matrix's storage
     */
    public Mat adjustROI(int dtop, int dbottom, int dleft, int dright) {
        return new Mat(n_adjustROI(nativeObj, dtop, dbottom, dleft, dright));
    }

    /**
     * Assigns this matrix to another wrapper, optionally converting its element type.
     *
     * @param m    destination matrix header to assign
     * @param type packed matrix channel-depth and channel-count code
     * @throws NullPointerException if m is null
     */
    public void assignTo(Mat m, int type) {
        n_assignTo(nativeObj, m.nativeObj, type);
    }

    /**
     * Assigns this matrix to another wrapper, optionally converting its element type.
     *
     * @param m destination matrix header to assign
     * @throws NullPointerException if m is null
     */
    public void assignTo(Mat m) {
        n_assignTo(nativeObj, m.nativeObj);
    }

    /**
     * Returns the number of channels in each matrix element.
     *
     * @return the number of channels in one element
     */
    public int channels() {
        return n_channels(nativeObj);
    }

    /**
     * Checks whether the matrix can represent a vector with the requested element layout.
     *
     * @param elemChannels      required channel count of each vector element
     * @param depth             single-channel element depth code
     * @param requireContinuous whether vector storage must be contiguous
     * @return the vector element count, or -1 when the requested layout, depth or continuity requirements are not met
     */
    public int checkVector(int elemChannels, int depth, boolean requireContinuous) {
        return n_checkVector(nativeObj, elemChannels, depth, requireContinuous);
    }

    /**
     * Checks whether the matrix can represent a vector with the requested element layout.
     *
     * @param elemChannels required channel count of each vector element
     * @param depth        single-channel element depth code
     * @return the vector element count, or -1 when the requested layout, depth or continuity requirements are not met
     */
    public int checkVector(int elemChannels, int depth) {
        return n_checkVector(nativeObj, elemChannels, depth);
    }

    /**
     * Checks whether the matrix can represent a vector with the requested element layout.
     *
     * @param elemChannels required channel count of each vector element
     * @return the vector element count, or -1 when the requested layout, depth or continuity requirements are not met
     */
    public int checkVector(int elemChannels) {
        return n_checkVector(nativeObj, elemChannels);
    }

    /**
     * Creates an independent copy of this matrix and its data.
     *
     * @return a new matrix header with an independent copy of the element data
     */
    public Mat clone() {
        return new Mat(n_clone(nativeObj));
    }

    /**
     * Creates a view of one matrix column; the view shares storage with this matrix.
     *
     * @param x horizontal coordinate
     * @return a one-column view sharing this matrix's storage
     */
    public Mat col(int x) {
        return new Mat(n_col(nativeObj, x));
    }

    /**
     * Creates a view of a half-open range of columns; the view shares storage with this matrix.
     *
     * @param startcol inclusive zero-based index of the first selected column
     * @param endcol   exclusive zero-based index after the last selected column
     * @return a view of the selected half-open column interval, sharing storage
     */
    public Mat colRange(int startcol, int endcol) {
        return new Mat(n_colRange(nativeObj, startcol, endcol));
    }

    /**
     * Creates a view of a half-open range of columns; the view shares storage with this matrix.
     *
     * @param r r index interval
     * @return a view of the selected half-open column interval, sharing storage
     * @throws NullPointerException if r is null
     */
    public Mat colRange(Range r) {
        return new Mat(n_colRange(nativeObj, r.start, r.end));
    }

    /**
     * Returns the number of matrix dimensions.
     *
     * @return the number of dimensions
     */
    public int dims() {
        return n_dims(nativeObj);
    }

    /**
     * Returns the number of columns in a two-dimensional matrix.
     *
     * @return the number of columns
     */
    public int cols() {
        return n_cols(nativeObj);
    }

    /**
     * Converts matrix values to the requested element type and applies the specified scale and offset.
     *
     * @param m     destination matrix receiving the converted channel values
     * @param rtype desired result type or depth code
     * @param alpha multiplicative factor applied before conversion
     * @param beta  additive offset applied after multiplication and before conversion
     * @throws NullPointerException if m is null
     */
    public void convertTo(Mat m, int rtype, double alpha, double beta) {
        n_convertTo(nativeObj, m.nativeObj, rtype, alpha, beta);
    }

    /**
     * Converts matrix values to the requested element type and applies the specified scale and offset.
     *
     * @param m     destination matrix receiving the converted channel values
     * @param rtype desired result type or depth code
     * @param alpha multiplicative factor applied before conversion
     * @throws NullPointerException if m is null
     */
    public void convertTo(Mat m, int rtype, double alpha) {
        n_convertTo(nativeObj, m.nativeObj, rtype, alpha);
    }

    /**
     * Converts matrix values to the requested element type and applies the specified scale and offset.
     *
     * @param m     destination matrix receiving the converted channel values
     * @param rtype desired result type or depth code
     * @throws NullPointerException if m is null
     */
    public void convertTo(Mat m, int rtype) {
        n_convertTo(nativeObj, m.nativeObj, rtype);
    }

    /**
     * Copies matrix data to the destination, using the supplied mask when present.
     *
     * @param m destination matrix receiving copied elements
     * @throws NullPointerException if m is null
     */
    public void copyTo(Mat m) {
        n_copyTo(nativeObj, m.nativeObj);
    }

    /**
     * Copies matrix data to the destination, using the supplied mask when present.
     *
     * @param m    destination matrix receiving copied elements
     * @param mask 8-bit selection mask whose nonzero entries select matrix elements; an empty mask selects all elements
     * @throws NullPointerException if m, mask is null
     */
    public void copyTo(Mat m, Mat mask) {
        n_copyTo(nativeObj, m.nativeObj, mask.nativeObj);
    }

    /**
     * Allocates storage for the requested matrix shape and type, reusing compatible existing storage.
     *
     * @param rows number of matrix rows
     * @param cols number of matrix columns
     * @param type packed matrix channel-depth and channel-count code
     */
    public void create(int rows, int cols, int type) {
        n_create(nativeObj, rows, cols, type);
    }

    /**
     * Allocates storage for the requested matrix shape and type, reusing compatible existing storage.
     *
     * @param size width and height of the requested region or matrix
     * @param type packed matrix channel-depth and channel-count code
     * @throws NullPointerException if size is null
     */
    public void create(Size size, int type) {
        n_create(nativeObj, size.width, size.height, type);
    }

    /**
     * Allocates storage for the requested matrix shape and type, reusing compatible existing storage.
     *
     * @param sizes extent of each matrix dimension
     * @param type  packed matrix channel-depth and channel-count code
     * @throws NullPointerException if sizes is null
     */
    public void create(int[] sizes, int type) {
        n_create(nativeObj, sizes.length, sizes, type);
    }

    /**
     * Copies the dimension information from another matrix.
     *
     * @param m matrix whose dimension sizes are copied
     * @throws NullPointerException if m is null
     */
    public void copySize(Mat m) {
        n_copySize(nativeObj, m.nativeObj);
    }

    /**
     * Computes the cross product of two three-element vectors.
     *
     * @param m three-element vector used as the second cross-product operand
     * @return a newly allocated matrix containing the three-dimensional cross product
     * @throws NullPointerException if m is null
     */
    public Mat cross(Mat m) {
        return new Mat(n_cross(nativeObj, m.nativeObj));
    }

    /**
     * Returns the native address of the first matrix data element.
     *
     * @return the address of the first data element
     */
    public long dataAddr() {
        return n_dataAddr(nativeObj);
    }

    /**
     * Returns the depth code of a single matrix channel.
     *
     * @return the single-channel depth code
     */
    public int depth() {
        return n_depth(nativeObj);
    }

    /**
     * Creates or selects a matrix diagonal, depending on the overload.
     *
     * @param d diagonal offset: zero selects the main diagonal, positive values select upper diagonals and negative
     *          values select lower diagonals
     * @return a column-vector view of the requested diagonal, sharing this matrix's storage
     */
    public Mat diag(int d) {
        return new Mat(n_diag(nativeObj, d));
    }

    /**
     * Creates or selects a matrix diagonal, depending on the overload.
     *
     * @return a column-vector view of the requested diagonal, sharing this matrix's storage
     */
    public Mat diag() {
        return new Mat(n_diag(nativeObj, 0));
    }

    /**
     * Creates or selects a matrix diagonal, depending on the overload.
     *
     * @param d diagonal offset: zero selects the main diagonal, positive values select upper diagonals and negative
     *          values select lower diagonals
     * @return a diagonal matrix initialized from the supplied vector
     * @throws NullPointerException if d is null
     */
    public static Mat diag(Mat d) {
        return new Mat(n_diag(d.nativeObj));
    }

    /**
     * Computes the dot product of this matrix and another matrix.
     *
     * @param m second operand of the vector dot product
     * @return the vector dot product
     * @throws NullPointerException if m is null
     */
    public double dot(Mat m) {
        return n_dot(nativeObj, m.nativeObj);
    }

    /**
     * Returns the size in bytes of a complete matrix element, including all channels.
     *
     * @return the byte size of a complete element
     */
    public long elemSize() {
        return n_elemSize(nativeObj);
    }

    /**
     * Returns the size in bytes of one matrix channel value.
     *
     * @return the byte size of one channel value
     */
    public long elemSize1() {
        return n_elemSize1(nativeObj);
    }

    /**
     * Reports whether the matrix contains no accessible data elements.
     *
     * @return true if the matrix has zero elements or no data storage
     */
    public boolean empty() {
        return n_empty(nativeObj);
    }

    /**
     * Creates a matrix initialized with ones on the main diagonal and zeros elsewhere.
     *
     * @param rows number of matrix rows
     * @param cols number of matrix columns
     * @param type packed matrix channel-depth and channel-count code
     * @return a matrix with ones on its main diagonal and zeros elsewhere
     */
    public static Mat eye(int rows, int cols, int type) {
        return new Mat(n_eye(rows, cols, type));
    }

    /**
     * Creates a matrix initialized with ones on the main diagonal and zeros elsewhere.
     *
     * @param size width and height of the requested region or matrix
     * @param type packed matrix channel-depth and channel-count code
     * @return a matrix with ones on its main diagonal and zeros elsewhere
     * @throws NullPointerException if size is null
     */
    public static Mat eye(Size size, int type) {
        return new Mat(n_eye(size.width, size.height, type));
    }

    /**
     * Computes a matrix inverse using the selected decomposition method.
     *
     * @param method algorithm or decomposition selector
     * @return the matrix inverse or pseudoinverse selected by the decomposition method
     */
    public Mat inv(int method) {
        return new Mat(n_inv(nativeObj, method));
    }

    /**
     * Computes a matrix inverse using the selected decomposition method.
     *
     * @return the matrix inverse or pseudoinverse selected by the decomposition method
     */
    public Mat inv() {
        return new Mat(n_inv(nativeObj));
    }

    /**
     * Reports whether the matrix data is stored without gaps between adjacent rows.
     *
     * @return true if adjacent rows have no storage gaps
     */
    public boolean isContinuous() {
        return n_isContinuous(nativeObj);
    }

    /**
     * Reports whether this matrix is a view into another matrix.
     *
     * @return true if this header represents a view into a parent matrix
     */
    public boolean isSubmatrix() {
        return n_isSubmatrix(nativeObj);
    }

    /**
     * Reports the size of the parent matrix and this view's offset within it.
     *
     * @param wholeSize whole size dimensions
     * @param ofs       ofs coordinates
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
     * Computes a scaled element-by-element product with another matrix.
     *
     * @param m     matrix supplying the selected storage or values
     * @param scale scale factor
     * @return reference to a new Mat object
     * @throws NullPointerException if m is null
     */
    public Mat mul(Mat m, double scale) {
        return new Mat(n_mul(nativeObj, m.nativeObj, scale));
    }

    /**
     * Computes a scaled element-by-element product with another matrix.
     *
     * @param m matrix supplying the selected storage or values
     * @return reference to a new Mat object
     * @throws NullPointerException if m is null
     */
    public Mat mul(Mat m) {
        return new Mat(n_mul(nativeObj, m.nativeObj));
    }

    /**
     * Matrix multiplication.
     *
     * @param m matrix supplying the selected storage or values
     * @return reference to a new Mat object
     * @throws NullPointerException if m is null
     */
    public Mat matMul(Mat m) {
        return new Mat(n_matMul(nativeObj, m.nativeObj));
    }

    /**
     * Creates a matrix initialized to one.
     *
     * @param rows number of matrix rows
     * @param cols number of matrix columns
     * @param type packed matrix channel-depth and channel-count code
     * @return a matrix initialized with Scalar(1); in a multichannel matrix only channel zero is set to one
     */
    public static Mat ones(int rows, int cols, int type) {
        return new Mat(n_ones(rows, cols, type));
    }

    /**
     * Creates a matrix initialized to one.
     *
     * @param size width and height of the requested region or matrix
     * @param type packed matrix channel-depth and channel-count code
     * @return a matrix initialized with Scalar(1); in a multichannel matrix only channel zero is set to one
     * @throws NullPointerException if size is null
     */
    public static Mat ones(Size size, int type) {
        return new Mat(n_ones(size.width, size.height, type));
    }

    /**
     * Creates a matrix initialized to one.
     *
     * @param sizes extent of each matrix dimension
     * @param type  packed matrix channel-depth and channel-count code
     * @return a matrix initialized with Scalar(1); in a multichannel matrix only channel zero is set to one
     * @throws NullPointerException if sizes is null
     */
    public static Mat ones(int[] sizes, int type) {
        return new Mat(n_ones(sizes.length, sizes, type));
    }

    /**
     * Appends the rows of another matrix to this matrix.
     *
     * @param m matrix whose rows are appended
     * @throws NullPointerException if m is null
     */
    public void push_back(Mat m) {
        n_push_back(nativeObj, m.nativeObj);
    }

    /**
     * Releases this header's reference to matrix data. Shared views may keep that storage alive. The native header
     * remains available for reuse and is deleted separately by the cleaner.
     */
    public void release() {
        n_release(nativeObj);
    }

    /**
     * Creates a matrix header with a different shape or channel count while reusing the underlying data.
     *
     * @param cn   number of channels per matrix element
     * @param rows number of matrix rows
     * @return a new matrix header with the requested shape and channels, sharing the original data
     */
    public Mat reshape(int cn, int rows) {
        return new Mat(n_reshape(nativeObj, cn, rows));
    }

    /**
     * Creates a matrix header with a different shape or channel count while reusing the underlying data.
     *
     * @param cn number of channels per matrix element
     * @return a new matrix header with the requested shape and channels, sharing the original data
     */
    public Mat reshape(int cn) {
        return new Mat(n_reshape(nativeObj, cn));
    }

    /**
     * Creates a matrix header with a different shape or channel count while reusing the underlying data.
     *
     * @param cn       number of channels per matrix element
     * @param newshape new extent of each matrix dimension; the total number of channel values must be preserved
     * @return a new matrix header with the requested shape and channels, sharing the original data
     * @throws NullPointerException if newshape is null
     */
    public Mat reshape(int cn, int[] newshape) {
        return new Mat(n_reshape_1(nativeObj, cn, newshape.length, newshape));
    }

    /**
     * Creates a view of one matrix row; the view shares storage with this matrix.
     *
     * @param y vertical coordinate
     * @return a one-row view sharing this matrix's storage
     */
    public Mat row(int y) {
        return new Mat(n_row(nativeObj, y));
    }

    /**
     * Creates a view of a half-open range of rows; the view shares storage with this matrix.
     *
     * @param startrow inclusive zero-based index of the first selected row
     * @param endrow   exclusive zero-based index after the last selected row
     * @return a view of the selected half-open row interval, sharing storage
     */
    public Mat rowRange(int startrow, int endrow) {
        return new Mat(n_rowRange(nativeObj, startrow, endrow));
    }

    /**
     * Creates a view of a half-open range of rows; the view shares storage with this matrix.
     *
     * @param r r index interval
     * @return a view of the selected half-open row interval, sharing storage
     * @throws NullPointerException if r is null
     */
    public Mat rowRange(Range r) {
        return new Mat(n_rowRange(nativeObj, r.start, r.end));
    }

    /**
     * Returns the number of rows in a two-dimensional matrix.
     *
     * @return the number of rows
     */
    public int rows() {
        return n_rows(nativeObj);
    }

    /**
     * Assigns channel values to all selected matrix elements.
     *
     * @param s channel values used to initialize or update the matrix
     * @return a new header sharing this matrix's storage after the selected values have been assigned
     * @throws NullPointerException if s is null
     */
    public Mat setTo(Scalar s) {
        return new Mat(n_setTo(nativeObj, s.val[0], s.val[1], s.val[2], s.val[3]));
    }

    /**
     * Assigns channel values to all selected matrix elements.
     *
     * @param value matrix of channel values used to fill the selected elements
     * @param mask  8-bit selection mask whose nonzero entries select matrix elements; an empty mask selects all
     *              elements
     * @return a new header sharing this matrix's storage after the selected values have been assigned
     * @throws NullPointerException if value, mask is null
     */
    public Mat setTo(Scalar value, Mat mask) {
        return new Mat(n_setTo(nativeObj, value.val[0], value.val[1], value.val[2], value.val[3], mask.nativeObj));
    }

    /**
     * Assigns channel values to all selected matrix elements.
     *
     * @param value matrix of channel values used to fill the selected elements
     * @param mask  8-bit selection mask whose nonzero entries select matrix elements; an empty mask selects all
     *              elements
     * @return a new header sharing this matrix's storage after the selected values have been assigned
     * @throws NullPointerException if value, mask is null
     */
    public Mat setTo(Mat value, Mat mask) {
        return new Mat(n_setTo(nativeObj, value.nativeObj, mask.nativeObj));
    }

    /**
     * Assigns channel values to all selected matrix elements.
     *
     * @param value matrix of channel values used to fill the selected elements
     * @return a new header sharing this matrix's storage after the selected values have been assigned
     * @throws NullPointerException if value is null
     */
    public Mat setTo(Mat value) {
        return new Mat(n_setTo(nativeObj, value.nativeObj));
    }

    /**
     * Returns the matrix extent for the requested dimension or its two-dimensional size.
     *
     * @return the matrix width and height
     */
    public Size size() {
        return new Size(n_size(nativeObj));
    }

    /**
     * Returns the matrix extent for the requested dimension or its two-dimensional size.
     *
     * @param i zero-based matrix dimension whose size or stride is requested
     * @return the requested dimension extent
     */
    public int size(int i) {
        return n_size_i(nativeObj, i);
    }

    /**
     * Returns the storage stride measured in single-channel elements.
     *
     * @param i zero-based matrix dimension whose size or stride is requested
     * @return the storage stride in single-channel elements along the requested dimension
     */
    public long step1(int i) {
        return n_step1(nativeObj, i);
    }

    /**
     * Returns the storage stride measured in single-channel elements.
     *
     * @return the storage stride in single-channel elements along the requested dimension
     */
    public long step1() {
        return n_step1(nativeObj);
    }

    /**
     * Creates a matrix view for the selected ranges or rectangle; the view shares storage.
     *
     * @param rowStart inclusive zero-based index of the first selected row
     * @param rowEnd   exclusive zero-based index after the last selected row
     * @param colStart inclusive zero-based index of the first selected column
     * @param colEnd   exclusive zero-based index after the last selected column
     * @return a new header for the selected region, sharing this matrix's storage
     */
    public Mat submat(int rowStart, int rowEnd, int colStart, int colEnd) {
        return new Mat(n_submat_rr(nativeObj, rowStart, rowEnd, colStart, colEnd));
    }

    /**
     * Creates a matrix view for the selected ranges or rectangle; the view shares storage.
     *
     * @param rowRange half-open range of rows to select
     * @param colRange half-open range of columns to select
     * @return a new header for the selected region, sharing this matrix's storage
     * @throws NullPointerException if rowRange, colRange is null
     */
    public Mat submat(Range rowRange, Range colRange) {
        return new Mat(n_submat_rr(nativeObj, rowRange.start, rowRange.end, colRange.start, colRange.end));
    }

    /**
     * Creates a matrix view for the selected ranges or rectangle; the view shares storage.
     *
     * @param ranges half-open range to select along each dimension
     * @return a new header for the selected region, sharing this matrix's storage
     */
    public Mat submat(Range[] ranges) {
        return new Mat(n_submat_ranges(nativeObj, ranges));
    }

    /**
     * Creates a matrix view for the selected ranges or rectangle; the view shares storage.
     *
     * @param roi rectangle identifying the region of interest
     * @return a new header for the selected region, sharing this matrix's storage
     * @throws NullPointerException if roi is null
     */
    public Mat submat(Rect roi) {
        return new Mat(n_submat(nativeObj, roi.x, roi.y, roi.width, roi.height));
    }

    /**
     * Returns the transposed matrix.
     *
     * @return a newly allocated transposed matrix
     */
    public Mat t() {
        return new Mat(n_t(nativeObj));
    }

    /**
     * Returns the number of matrix elements, excluding the channel count.
     *
     * @return the element count over the requested dimensions, excluding channels
     */
    public long total() {
        return n_total(nativeObj);
    }

    /**
     * Returns the packed matrix type code containing the element depth and channel count.
     *
     * @return the packed depth-and-channel type code
     */
    public int type() {
        return n_type(nativeObj);
    }

    /**
     * Creates a matrix initialized to zero.
     *
     * @param rows number of matrix rows
     * @param cols number of matrix columns
     * @param type packed matrix channel-depth and channel-count code
     * @return a matrix with every channel value initialized to zero
     */
    public static Mat zeros(int rows, int cols, int type) {
        return new Mat(n_zeros(rows, cols, type));
    }

    /**
     * Creates a matrix initialized to zero.
     *
     * @param size width and height of the requested region or matrix
     * @param type packed matrix channel-depth and channel-count code
     * @return a matrix with every channel value initialized to zero
     * @throws NullPointerException if size is null
     */
    public static Mat zeros(Size size, int type) {
        return new Mat(n_zeros(size.width, size.height, type));
    }

    /**
     * Creates a matrix initialized to zero.
     *
     * @param sizes extent of each matrix dimension
     * @param type  packed matrix channel-depth and channel-count code
     * @return a matrix with every channel value initialized to zero
     * @throws NullPointerException if sizes is null
     */
    public static Mat zeros(int[] sizes, int type) {
        return new Mat(n_zeros(sizes.length, sizes, type));
    }

    /**
     * Returns a textual representation of this object.
     *
     * @return the matrix shape, type, continuity, view status and native addresses
     */
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

    /**
     * Formats matrix element values as text.
     *
     * @return a formatted representation of the matrix element values
     */
    public String dump() {
        return nDump(nativeObj);
    }

    /**
     * Writes channel values starting at the specified matrix position.
     *
     * @param row  zero-based row index
     * @param col  zero-based column index
     * @param data source array of channel values to write
     * @return the number of channel values written after native numeric conversion
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count
     */
    public int put(int row, int col, double... data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new UnsupportedOperationException("Provided data element number (" + (data == null ? 0 : data.length)
                    + ") should be multiple of the Mat channels count (" + CvType.channels(t) + ")");
        return nPutD(nativeObj, row, col, data.length, data);
    }

    /**
     * Writes channel values starting at the specified matrix position.
     *
     * @param idx  zero-based indices of the matrix element
     * @param data source array of channel values to write
     * @return the number of channel values written after native numeric conversion
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count
     * @throws IllegalArgumentException      if the index array length differs from the number of matrix dimensions
     * @throws NullPointerException          if idx is null
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

    /**
     * Writes channel values starting at the specified matrix position.
     *
     * @param row  zero-based row index
     * @param col  zero-based column index
     * @param data source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
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

    /**
     * Writes channel values starting at the specified matrix position.
     *
     * @param idx  zero-based indices of the matrix element
     * @param data source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
     * @throws IllegalArgumentException      if the index array length differs from the number of matrix dimensions
     * @throws NullPointerException          if idx is null
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

    /**
     * Writes channel values starting at the specified matrix position.
     *
     * @param row  zero-based row index
     * @param col  zero-based column index
     * @param data source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
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

    /**
     * Writes channel values starting at the specified matrix position.
     *
     * @param idx  zero-based indices of the matrix element
     * @param data source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
     * @throws IllegalArgumentException      if the index array length differs from the number of matrix dimensions
     * @throws NullPointerException          if idx is null
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

    /**
     * Writes channel values starting at the specified matrix position.
     *
     * @param row  zero-based row index
     * @param col  zero-based column index
     * @param data source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
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

    /**
     * Writes channel values starting at the specified matrix position.
     *
     * @param idx  zero-based indices of the matrix element
     * @param data source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
     * @throws IllegalArgumentException      if the index array length differs from the number of matrix dimensions
     * @throws NullPointerException          if idx is null
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

    /**
     * Writes channel values starting at the specified matrix position.
     *
     * @param row  zero-based row index
     * @param col  zero-based column index
     * @param data source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
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

    /**
     * Writes channel values starting at the specified matrix position.
     *
     * @param idx  zero-based indices of the matrix element
     * @param data source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
     * @throws IllegalArgumentException      if the index array length differs from the number of matrix dimensions
     * @throws NullPointerException          if idx is null
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

    /**
     * Writes channel values starting at the specified matrix position.
     *
     * @param row    zero-based row index
     * @param col    zero-based column index
     * @param data   source array of channel values to write
     * @param offset zero-based offset of the first source-array entry to copy
     * @param length number of array entries to copy
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
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

    /**
     * Writes channel values starting at the specified matrix position.
     *
     * @param idx    zero-based indices of the matrix element
     * @param data   source array of channel values to write
     * @param offset zero-based offset of the first source-array entry to copy
     * @param length number of array entries to copy
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
     * @throws IllegalArgumentException      if the index array length differs from the number of matrix dimensions
     * @throws NullPointerException          if idx is null
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

    /**
     * Reads channel values starting at the specified matrix position.
     *
     * @param row  zero-based row index
     * @param col  zero-based column index
     * @param data destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
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

    /**
     * Reads channel values starting at the specified matrix position.
     *
     * @param idx  zero-based indices of the matrix element
     * @param data destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
     * @throws IllegalArgumentException      if the index array length differs from the number of matrix dimensions
     * @throws NullPointerException          if idx is null
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

    /**
     * Reads channel values starting at the specified matrix position.
     *
     * @param row  zero-based row index
     * @param col  zero-based column index
     * @param data destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
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

    /**
     * Reads channel values starting at the specified matrix position.
     *
     * @param idx  zero-based indices of the matrix element
     * @param data destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
     * @throws IllegalArgumentException      if the index array length differs from the number of matrix dimensions
     * @throws NullPointerException          if idx is null
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

    /**
     * Reads channel values starting at the specified matrix position.
     *
     * @param row  zero-based row index
     * @param col  zero-based column index
     * @param data destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
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

    /**
     * Reads channel values starting at the specified matrix position.
     *
     * @param idx  zero-based indices of the matrix element
     * @param data destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
     * @throws IllegalArgumentException      if the index array length differs from the number of matrix dimensions
     * @throws NullPointerException          if idx is null
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

    /**
     * Reads channel values starting at the specified matrix position.
     *
     * @param row  zero-based row index
     * @param col  zero-based column index
     * @param data destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
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

    /**
     * Reads channel values starting at the specified matrix position.
     *
     * @param idx  zero-based indices of the matrix element
     * @param data destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
     * @throws IllegalArgumentException      if the index array length differs from the number of matrix dimensions
     * @throws NullPointerException          if idx is null
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

    /**
     * Reads channel values starting at the specified matrix position.
     *
     * @param row  zero-based row index
     * @param col  zero-based column index
     * @param data destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
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

    /**
     * Reads channel values starting at the specified matrix position.
     *
     * @param idx  zero-based indices of the matrix element
     * @param data destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     * @throws UnsupportedOperationException if data is null or length is not a multiple of the matrix channel count; or
     *                                       if the array element type does not match the matrix channel depth
     * @throws IllegalArgumentException      if the index array length differs from the number of matrix dimensions
     * @throws NullPointerException          if idx is null
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

    /**
     * Reads channel values starting at the specified matrix position.
     *
     * @param row zero-based row index
     * @param col zero-based column index
     * @return the selected element's channel values converted to doubles, or null if the native accessor cannot address
     *         that element
     */
    public double[] get(int row, int col) {
        return nGet(nativeObj, row, col);
    }

    /**
     * Reads channel values starting at the specified matrix position.
     *
     * @param idx zero-based indices of the matrix element
     * @return the selected element's channel values converted to doubles, or null if the native accessor cannot address
     *         that element
     * @throws IllegalArgumentException if the index array length differs from the number of matrix dimensions
     * @throws NullPointerException     if idx is null
     */
    public double[] get(int[] idx) {
        if (idx.length != dims())
            throw new IllegalArgumentException("Incorrect number of indices");
        return nGetIdx(nativeObj, idx);
    }

    /**
     * Returns the matrix height in rows.
     *
     * @return the number of rows
     */
    public int height() {
        return rows();
    }

    /**
     * Returns the matrix width in columns.
     *
     * @return the number of columns
     */
    public int width() {
        return cols();
    }

    /**
     * Creates a typed accessor for the matrix element at the specified indices.
     *
     * @param <T>   type of values represented by this declaration
     * @param clazz boxed or primitive class selecting byte, short, int, float, or double element access
     * @param row   zero-based row index
     * @param col   zero-based column index
     * @return a typed accessor bound to the supplied matrix indices
     * @throws RuntimeException if clazz is not one of the supported boxed numeric element classes
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

    /**
     * Creates a typed accessor for the matrix element at the specified indices.
     *
     * @param <T>   type of values represented by this declaration
     * @param clazz boxed or primitive class selecting byte, short, int, float, or double element access
     * @param idx   zero-based indices of the matrix element
     * @return a typed accessor bound to the supplied matrix indices
     * @throws RuntimeException if clazz is not one of the supported boxed numeric element classes
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

    /**
     * A 2-component tuple containing channel values of the same element type.
     *
     * @param <T> type of values represented by this declaration
     */
    public static class Tuple2<T> {

        /**
         * Creates a tuple containing the supplied channel values.
         *
         * @param _0 channel value at tuple position 0
         * @param _1 channel value at tuple position 1
         */
        public Tuple2(T _0, T _1) {
            this._0 = _0;
            this._1 = _1;
        }

        /**
         * Returns the channel value at tuple position 0.
         *
         * @return the tuple channel value at index 0
         */
        public T get_0() {
            return _0;
        }

        /**
         * Returns the channel value at tuple position 1.
         *
         * @return the tuple channel value at index 1
         */
        public T get_1() {
            return _1;
        }

        /**
         * Channel value at tuple position 0.
         */
        private final T _0;
        /**
         * Channel value at tuple position 1.
         */
        private final T _1;
    }

    /**
     * A 3-component tuple containing channel values of the same element type.
     *
     * @param <T> type of values represented by this declaration
     */
    public static class Tuple3<T> {

        /**
         * Creates a tuple containing the supplied channel values.
         *
         * @param _0 channel value at tuple position 0
         * @param _1 channel value at tuple position 1
         * @param _2 channel value at tuple position 2
         */
        public Tuple3(T _0, T _1, T _2) {
            this._0 = _0;
            this._1 = _1;
            this._2 = _2;
        }

        /**
         * Returns the channel value at tuple position 0.
         *
         * @return the tuple channel value at index 0
         */
        public T get_0() {
            return _0;
        }

        /**
         * Returns the channel value at tuple position 1.
         *
         * @return the tuple channel value at index 1
         */
        public T get_1() {
            return _1;
        }

        /**
         * Returns the channel value at tuple position 2.
         *
         * @return the tuple channel value at index 2
         */
        public T get_2() {
            return _2;
        }

        /**
         * Channel value at tuple position 0.
         */
        private final T _0;
        /**
         * Channel value at tuple position 1.
         */
        private final T _1;
        /**
         * Channel value at tuple position 2.
         */
        private final T _2;
    }

    /**
     * A 4-component tuple containing channel values of the same element type.
     *
     * @param <T> type of values represented by this declaration
     */
    public static class Tuple4<T> {

        /**
         * Creates a tuple containing the supplied channel values.
         *
         * @param _0 channel value at tuple position 0
         * @param _1 channel value at tuple position 1
         * @param _2 channel value at tuple position 2
         * @param _3 channel value at tuple position 3
         */
        public Tuple4(T _0, T _1, T _2, T _3) {
            this._0 = _0;
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
        }

        /**
         * Returns the channel value at tuple position 0.
         *
         * @return the tuple channel value at index 0
         */
        public T get_0() {
            return _0;
        }

        /**
         * Returns the channel value at tuple position 1.
         *
         * @return the tuple channel value at index 1
         */
        public T get_1() {
            return _1;
        }

        /**
         * Returns the channel value at tuple position 2.
         *
         * @return the tuple channel value at index 2
         */
        public T get_2() {
            return _2;
        }

        /**
         * Returns the channel value at tuple position 3.
         *
         * @return the tuple channel value at index 3
         */
        public T get_3() {
            return _3;
        }

        /**
         * Channel value at tuple position 0.
         */
        private final T _0;
        /**
         * Channel value at tuple position 1.
         */
        private final T _1;
        /**
         * Channel value at tuple position 2.
         */
        private final T _2;
        /**
         * Channel value at tuple position 3.
         */
        private final T _3;
    }

    /**
     * Typed access to a matrix element and its channel values.
     *
     * @param <T> type of values represented by this declaration
     */
    public interface Atable<T> {

        /**
         * Reads 1 channel value at the selected matrix element.
         *
         * @return the selected element's channel-zero value
         */
        T getV();

        /**
         * Writes 1 channel value at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         */
        void setV(T v);

        /**
         * Reads 2 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 2 channel values
         */
        Tuple2<T> getV2c();

        /**
         * Writes 2 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         */
        void setV2c(Tuple2<T> v);

        /**
         * Reads 3 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 3 channel values
         */
        Tuple3<T> getV3c();

        /**
         * Writes 3 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         */
        void setV3c(Tuple3<T> v);

        /**
         * Reads 4 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 4 channel values
         */
        Tuple4<T> getV4c();

        /**
         * Writes 4 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         */
        void setV4c(Tuple4<T> v);
    }

    /**
     * Shared matrix and index state for typed element-access adapters.
     */
    private static class AtableBase {

        /**
         * Creates a typed accessor for the matrix element at the supplied indices.
         *
         * @param mat matrix whose selected element is accessed
         * @param row zero-based row index
         * @param col zero-based column index
         */
        protected AtableBase(Mat mat, int row, int col) {
            this.mat = mat;
            indices = new int[2];
            indices[0] = row;
            indices[1] = col;
        }

        /**
         * Creates a typed accessor for the matrix element at the supplied indices.
         *
         * @param mat     matrix whose selected element is accessed
         * @param indices zero-based indices of the matrix element
         */
        protected AtableBase(Mat mat, int[] indices) {
            this.mat = mat;
            this.indices = indices;
        }

        /**
         * Matrix accessed by this typed element adapter.
         */
        protected final Mat mat;
        /**
         * Indices of the matrix element accessed by this adapter.
         */
        protected final int[] indices;
    }

    /**
     * Typed matrix element access using boxed byte channel values.
     */
    private static class AtableByte extends AtableBase implements Atable<Byte> {

        /**
         * Creates a typed accessor for the matrix element at the supplied indices.
         *
         * @param mat matrix whose selected element is accessed
         * @param row zero-based row index
         * @param col zero-based column index
         */
        public AtableByte(Mat mat, int row, int col) {
            super(mat, row, col);
        }

        /**
         * Creates a typed accessor for the matrix element at the supplied indices.
         *
         * @param mat     matrix whose selected element is accessed
         * @param indices zero-based indices of the matrix element
         */
        public AtableByte(Mat mat, int[] indices) {
            super(mat, indices);
        }

        /**
         * Reads 1 channel value at the selected matrix element.
         *
         * @return the selected element's channel-zero value
         */
        @Override
        public Byte getV() {
            byte[] data = new byte[1];
            mat.get(indices, data);
            return data[0];
        }

        /**
         * Writes 1 channel value at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         */
        @Override
        public void setV(Byte v) {
            byte[] data = new byte[] { v };
            mat.put(indices, data);
        }

        /**
         * Reads 2 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 2 channel values
         */
        @Override
        public Tuple2<Byte> getV2c() {
            byte[] data = new byte[2];
            mat.get(indices, data);
            return new Tuple2<Byte>(data[0], data[1]);
        }

        /**
         * Writes 2 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         * @throws NullPointerException if v is null
         */
        @Override
        public void setV2c(Tuple2<Byte> v) {
            byte[] data = new byte[] { v._0, v._1 };
            mat.put(indices, data);
        }

        /**
         * Reads 3 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 3 channel values
         */
        @Override
        public Tuple3<Byte> getV3c() {
            byte[] data = new byte[3];
            mat.get(indices, data);
            return new Tuple3<Byte>(data[0], data[1], data[2]);
        }

        /**
         * Writes 3 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         * @throws NullPointerException if v is null
         */
        @Override
        public void setV3c(Tuple3<Byte> v) {
            byte[] data = new byte[] { v._0, v._1, v._2 };
            mat.put(indices, data);
        }

        /**
         * Reads 4 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 4 channel values
         */
        @Override
        public Tuple4<Byte> getV4c() {
            byte[] data = new byte[4];
            mat.get(indices, data);
            return new Tuple4<Byte>(data[0], data[1], data[2], data[3]);
        }

        /**
         * Writes 4 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         * @throws NullPointerException if v is null
         */
        @Override
        public void setV4c(Tuple4<Byte> v) {
            byte[] data = new byte[] { v._0, v._1, v._2, v._3 };
            mat.put(indices, data);
        }
    }

    /**
     * Typed matrix element access using boxed double channel values.
     */
    private static class AtableDouble extends AtableBase implements Atable<Double> {

        /**
         * Creates a typed accessor for the matrix element at the supplied indices.
         *
         * @param mat matrix whose selected element is accessed
         * @param row zero-based row index
         * @param col zero-based column index
         */
        public AtableDouble(Mat mat, int row, int col) {
            super(mat, row, col);
        }

        /**
         * Creates a typed accessor for the matrix element at the supplied indices.
         *
         * @param mat     matrix whose selected element is accessed
         * @param indices zero-based indices of the matrix element
         */
        public AtableDouble(Mat mat, int[] indices) {
            super(mat, indices);
        }

        /**
         * Reads 1 channel value at the selected matrix element.
         *
         * @return the selected element's channel-zero value
         */
        @Override
        public Double getV() {
            double[] data = new double[1];
            mat.get(indices, data);
            return data[0];
        }

        /**
         * Writes 1 channel value at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         */
        @Override
        public void setV(Double v) {
            double[] data = new double[] { v };
            mat.put(indices, data);
        }

        /**
         * Reads 2 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 2 channel values
         */
        @Override
        public Tuple2<Double> getV2c() {
            double[] data = new double[2];
            mat.get(indices, data);
            return new Tuple2<Double>(data[0], data[1]);
        }

        /**
         * Writes 2 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         * @throws NullPointerException if v is null
         */
        @Override
        public void setV2c(Tuple2<Double> v) {
            double[] data = new double[] { v._0, v._1 };
            mat.put(indices, data);
        }

        /**
         * Reads 3 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 3 channel values
         */
        @Override
        public Tuple3<Double> getV3c() {
            double[] data = new double[3];
            mat.get(indices, data);
            return new Tuple3<Double>(data[0], data[1], data[2]);
        }

        /**
         * Writes 3 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         * @throws NullPointerException if v is null
         */
        @Override
        public void setV3c(Tuple3<Double> v) {
            double[] data = new double[] { v._0, v._1, v._2 };
            mat.put(indices, data);
        }

        /**
         * Reads 4 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 4 channel values
         */
        @Override
        public Tuple4<Double> getV4c() {
            double[] data = new double[4];
            mat.get(indices, data);
            return new Tuple4<Double>(data[0], data[1], data[2], data[3]);
        }

        /**
         * Writes 4 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         * @throws NullPointerException if v is null
         */
        @Override
        public void setV4c(Tuple4<Double> v) {
            double[] data = new double[] { v._0, v._1, v._2, v._3 };
            mat.put(indices, data);
        }
    }

    /**
     * Typed matrix element access using boxed float channel values.
     */
    private static class AtableFloat extends AtableBase implements Atable<Float> {

        /**
         * Creates a typed accessor for the matrix element at the supplied indices.
         *
         * @param mat matrix whose selected element is accessed
         * @param row zero-based row index
         * @param col zero-based column index
         */
        public AtableFloat(Mat mat, int row, int col) {
            super(mat, row, col);
        }

        /**
         * Creates a typed accessor for the matrix element at the supplied indices.
         *
         * @param mat     matrix whose selected element is accessed
         * @param indices zero-based indices of the matrix element
         */
        public AtableFloat(Mat mat, int[] indices) {
            super(mat, indices);
        }

        /**
         * Reads 1 channel value at the selected matrix element.
         *
         * @return the selected element's channel-zero value
         */
        @Override
        public Float getV() {
            float[] data = new float[1];
            mat.get(indices, data);
            return data[0];
        }

        /**
         * Writes 1 channel value at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         */
        @Override
        public void setV(Float v) {
            float[] data = new float[] { v };
            mat.put(indices, data);
        }

        /**
         * Reads 2 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 2 channel values
         */
        @Override
        public Tuple2<Float> getV2c() {
            float[] data = new float[2];
            mat.get(indices, data);
            return new Tuple2<Float>(data[0], data[1]);
        }

        /**
         * Writes 2 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         * @throws NullPointerException if v is null
         */
        @Override
        public void setV2c(Tuple2<Float> v) {
            float[] data = new float[] { v._0, v._1 };
            mat.put(indices, data);
        }

        /**
         * Reads 3 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 3 channel values
         */
        @Override
        public Tuple3<Float> getV3c() {
            float[] data = new float[3];
            mat.get(indices, data);
            return new Tuple3<Float>(data[0], data[1], data[2]);
        }

        /**
         * Writes 3 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         * @throws NullPointerException if v is null
         */
        @Override
        public void setV3c(Tuple3<Float> v) {
            float[] data = new float[] { v._0, v._1, v._2 };
            mat.put(indices, data);
        }

        /**
         * Reads 4 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 4 channel values
         */
        @Override
        public Tuple4<Float> getV4c() {
            float[] data = new float[4];
            mat.get(indices, data);
            return new Tuple4<Float>(data[0], data[1], data[2], data[3]);
        }

        /**
         * Writes 4 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         * @throws NullPointerException if v is null
         */
        @Override
        public void setV4c(Tuple4<Float> v) {
            double[] data = new double[] { v._0, v._1, v._2, v._3 };
            mat.put(indices, data);
        }
    }

    /**
     * Typed matrix element access using boxed integer channel values.
     */
    private static class AtableInteger extends AtableBase implements Atable<Integer> {

        /**
         * Creates a typed accessor for the matrix element at the supplied indices.
         *
         * @param mat matrix whose selected element is accessed
         * @param row zero-based row index
         * @param col zero-based column index
         */
        public AtableInteger(Mat mat, int row, int col) {
            super(mat, row, col);
        }

        /**
         * Creates a typed accessor for the matrix element at the supplied indices.
         *
         * @param mat     matrix whose selected element is accessed
         * @param indices zero-based indices of the matrix element
         */
        public AtableInteger(Mat mat, int[] indices) {
            super(mat, indices);
        }

        /**
         * Reads 1 channel value at the selected matrix element.
         *
         * @return the selected element's channel-zero value
         */
        @Override
        public Integer getV() {
            int[] data = new int[1];
            mat.get(indices, data);
            return data[0];
        }

        /**
         * Writes 1 channel value at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         */
        @Override
        public void setV(Integer v) {
            int[] data = new int[] { v };
            mat.put(indices, data);
        }

        /**
         * Reads 2 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 2 channel values
         */
        @Override
        public Tuple2<Integer> getV2c() {
            int[] data = new int[2];
            mat.get(indices, data);
            return new Tuple2<Integer>(data[0], data[1]);
        }

        /**
         * Writes 2 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         * @throws NullPointerException if v is null
         */
        @Override
        public void setV2c(Tuple2<Integer> v) {
            int[] data = new int[] { v._0, v._1 };
            mat.put(indices, data);
        }

        /**
         * Reads 3 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 3 channel values
         */
        @Override
        public Tuple3<Integer> getV3c() {
            int[] data = new int[3];
            mat.get(indices, data);
            return new Tuple3<Integer>(data[0], data[1], data[2]);
        }

        /**
         * Writes 3 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         * @throws NullPointerException if v is null
         */
        @Override
        public void setV3c(Tuple3<Integer> v) {
            int[] data = new int[] { v._0, v._1, v._2 };
            mat.put(indices, data);
        }

        /**
         * Reads 4 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 4 channel values
         */
        @Override
        public Tuple4<Integer> getV4c() {
            int[] data = new int[4];
            mat.get(indices, data);
            return new Tuple4<Integer>(data[0], data[1], data[2], data[3]);
        }

        /**
         * Writes 4 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         * @throws NullPointerException if v is null
         */
        @Override
        public void setV4c(Tuple4<Integer> v) {
            int[] data = new int[] { v._0, v._1, v._2, v._3 };
            mat.put(indices, data);
        }
    }

    /**
     * Typed matrix element access using boxed short channel values.
     */
    private static class AtableShort extends AtableBase implements Atable<Short> {

        /**
         * Creates a typed accessor for the matrix element at the supplied indices.
         *
         * @param mat matrix whose selected element is accessed
         * @param row zero-based row index
         * @param col zero-based column index
         */
        public AtableShort(Mat mat, int row, int col) {
            super(mat, row, col);
        }

        /**
         * Creates a typed accessor for the matrix element at the supplied indices.
         *
         * @param mat     matrix whose selected element is accessed
         * @param indices zero-based indices of the matrix element
         */
        public AtableShort(Mat mat, int[] indices) {
            super(mat, indices);
        }

        /**
         * Reads 1 channel value at the selected matrix element.
         *
         * @return the selected element's channel-zero value
         */
        @Override
        public Short getV() {
            short[] data = new short[1];
            mat.get(indices, data);
            return data[0];
        }

        /**
         * Writes 1 channel value at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         */
        @Override
        public void setV(Short v) {
            short[] data = new short[] { v };
            mat.put(indices, data);
        }

        /**
         * Reads 2 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 2 channel values
         */
        @Override
        public Tuple2<Short> getV2c() {
            short[] data = new short[2];
            mat.get(indices, data);
            return new Tuple2<Short>(data[0], data[1]);
        }

        /**
         * Writes 2 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         * @throws NullPointerException if v is null
         */
        @Override
        public void setV2c(Tuple2<Short> v) {
            short[] data = new short[] { v._0, v._1 };
            mat.put(indices, data);
        }

        /**
         * Reads 3 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 3 channel values
         */
        @Override
        public Tuple3<Short> getV3c() {
            short[] data = new short[3];
            mat.get(indices, data);
            return new Tuple3<Short>(data[0], data[1], data[2]);
        }

        /**
         * Writes 3 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         * @throws NullPointerException if v is null
         */
        @Override
        public void setV3c(Tuple3<Short> v) {
            short[] data = new short[] { v._0, v._1, v._2 };
            mat.put(indices, data);
        }

        /**
         * Reads 4 channel values at the selected matrix element.
         *
         * @return a tuple containing the selected element's first 4 channel values
         */
        @Override
        public Tuple4<Short> getV4c() {
            short[] data = new short[4];
            mat.get(indices, data);
            return new Tuple4<Short>(data[0], data[1], data[2], data[3]);
        }

        /**
         * Writes 4 channel values at the selected matrix element.
         *
         * @param v value assigned to or compared with this object
         * @throws NullPointerException if v is null
         */
        @Override
        public void setV4c(Tuple4<Short> v) {
            short[] data = new short[] { v._0, v._1, v._2, v._3 };
            mat.put(indices, data);
        }
    }

    /**
     * Returns the opaque native handle associated with this Java wrapper.
     *
     * @return the opaque native handle
     */
    public long getNativeObjAddr() {
        return nativeObj;
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#Mat()}.
     *
     * @return a newly allocated native {@code Mat} binding handle
     */
    private static native long n_Mat();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#Mat(int,int,int)}.
     *
     * @param rows number of matrix rows
     * @param cols number of matrix columns
     * @param type packed matrix channel-depth and channel-count code
     * @return a newly allocated native {@code Mat} binding handle
     */
    private static native long n_Mat(int rows, int cols, int type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#Mat(int[],int)}.
     *
     * @param ndims number of entries in {@code sizes}
     * @param sizes extent of each matrix dimension
     * @param type  packed matrix channel-depth and channel-count code
     * @return a newly allocated native {@code Mat} binding handle
     */
    private static native long n_Mat(int ndims, int[] sizes, int type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#Mat(int,int,int,ByteBuffer)}.
     *
     * @param rows number of matrix rows
     * @param cols number of matrix columns
     * @param type packed matrix channel-depth and channel-count code
     * @param data array or buffer containing matrix channel values
     * @return a newly allocated native {@code Mat} binding handle
     */
    private static native long n_Mat(int rows, int cols, int type, ByteBuffer data);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#Mat(int,int,int,ByteBuffer,long)}.
     *
     * @param rows number of matrix rows
     * @param cols number of matrix columns
     * @param type packed matrix channel-depth and channel-count code
     * @param data array or buffer containing matrix channel values
     * @param step row stride in bytes; the caller-provided buffer must cover the addressed storage
     * @return a newly allocated native {@code Mat} binding handle
     */
    private static native long n_Mat(int rows, int cols, int type, ByteBuffer data, long step);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#Mat(Size,int)}.
     *
     * @param size_width  {@code size.width} component of width and height of the requested region or matrix
     * @param size_height {@code size.height} component of width and height of the requested region or matrix
     * @param type        packed matrix channel-depth and channel-count code
     * @return a newly allocated native {@code Mat} binding handle
     */
    private static native long n_Mat(double size_width, double size_height, int type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#Mat(int,int,int,Scalar)}.
     *
     * @param rows   number of matrix rows
     * @param cols   number of matrix columns
     * @param type   packed matrix channel-depth and channel-count code
     * @param s_val0 {@code s.val[0]} component of channel values used to initialize or update the matrix
     * @param s_val1 {@code s.val[1]} component of channel values used to initialize or update the matrix
     * @param s_val2 {@code s.val[2]} component of channel values used to initialize or update the matrix
     * @param s_val3 {@code s.val[3]} component of channel values used to initialize or update the matrix
     * @return a newly allocated native {@code Mat} binding handle
     */
    private static native long n_Mat(
            int rows,
            int cols,
            int type,
            double s_val0,
            double s_val1,
            double s_val2,
            double s_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#Mat(Size,int,Scalar)}.
     *
     * @param size_width  {@code size.width} component of width and height of the requested region or matrix
     * @param size_height {@code size.height} component of width and height of the requested region or matrix
     * @param type        packed matrix channel-depth and channel-count code
     * @param s_val0      {@code s.val[0]} component of channel values used to initialize or update the matrix
     * @param s_val1      {@code s.val[1]} component of channel values used to initialize or update the matrix
     * @param s_val2      {@code s.val[2]} component of channel values used to initialize or update the matrix
     * @param s_val3      {@code s.val[3]} component of channel values used to initialize or update the matrix
     * @return a newly allocated native {@code Mat} binding handle
     */
    private static native long n_Mat(
            double size_width,
            double size_height,
            int type,
            double s_val0,
            double s_val1,
            double s_val2,
            double s_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#Mat(int[],int,Scalar)}.
     *
     * @param ndims  number of entries in {@code sizes}
     * @param sizes  extent of each matrix dimension
     * @param type   packed matrix channel-depth and channel-count code
     * @param s_val0 {@code s.val[0]} component of channel values used to initialize or update the matrix
     * @param s_val1 {@code s.val[1]} component of channel values used to initialize or update the matrix
     * @param s_val2 {@code s.val[2]} component of channel values used to initialize or update the matrix
     * @param s_val3 {@code s.val[3]} component of channel values used to initialize or update the matrix
     * @return a newly allocated native {@code Mat} binding handle
     */
    private static native long n_Mat(
            int ndims,
            int[] sizes,
            int type,
            double s_val0,
            double s_val1,
            double s_val2,
            double s_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#Mat(Mat,Range,Range)}.
     *
     * @param m_nativeObj    native matrix or object handle for {@code m}; parent matrix whose storage is shared by the
     *                       selected region
     * @param rowRange_start {@code rowRange.start} component of half-open range of rows to select
     * @param rowRange_end   {@code rowRange.end} component of half-open range of rows to select
     * @param colRange_start {@code colRange.start} component of half-open range of columns to select
     * @param colRange_end   {@code colRange.end} component of half-open range of columns to select
     * @return a newly allocated native {@code Mat} binding handle
     */
    private static native long n_Mat(
            long m_nativeObj,
            int rowRange_start,
            int rowRange_end,
            int colRange_start,
            int colRange_end);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#Mat(Mat,Range)}.
     *
     * @param m_nativeObj    native matrix or object handle for {@code m}; parent matrix whose storage is shared by the
     *                       selected region
     * @param rowRange_start {@code rowRange.start} component of half-open range of rows to select
     * @param rowRange_end   {@code rowRange.end} component of half-open range of rows to select
     * @return a newly allocated native {@code Mat} binding handle
     */
    private static native long n_Mat(long m_nativeObj, int rowRange_start, int rowRange_end);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#Mat(Mat,Range[])}.
     *
     * @param m_nativeObj native matrix or object handle for {@code m}; parent matrix whose storage is shared by the
     *                    selected region
     * @param ranges      half-open range to select along each dimension
     * @return a newly allocated native {@code Mat} binding handle
     */
    private static native long n_Mat(long m_nativeObj, Range[] ranges);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#adjustROI(int,int,int,int)}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @param dtop      number of rows by which to expand the top boundary; negative values shrink it
     * @param dbottom   number of rows by which to expand the bottom boundary; negative values shrink it
     * @param dleft     number of columns by which to expand the left boundary; negative values shrink it
     * @param dright    number of columns by which to expand the right boundary; negative values shrink it
     * @return native binding handle for a new matrix header for the adjusted region, sharing this matrix's storage
     */
    private static native long n_adjustROI(long nativeObj, int dtop, int dbottom, int dleft, int dright);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#assignTo(Mat,int)}.
     *
     * @param nativeObj   native binding handle of this {@code Mat} instance
     * @param m_nativeObj native matrix or object handle for {@code m}; destination matrix header to assign
     * @param type        packed matrix channel-depth and channel-count code
     */
    private static native void n_assignTo(long nativeObj, long m_nativeObj, int type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#assignTo(Mat)}.
     *
     * @param nativeObj   native binding handle of this {@code Mat} instance
     * @param m_nativeObj native matrix or object handle for {@code m}; destination matrix header to assign
     */
    private static native void n_assignTo(long nativeObj, long m_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#channels()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return the number of channels in one element
     */
    private static native int n_channels(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#checkVector(int,int,boolean)}.
     *
     * @param nativeObj         native binding handle of this {@code Mat} instance
     * @param elemChannels      required channel count of each vector element
     * @param depth             single-channel element depth code
     * @param requireContinuous whether vector storage must be contiguous
     * @return the vector element count, or -1 when the requested layout, depth or continuity requirements are not met
     */
    private static native int n_checkVector(long nativeObj, int elemChannels, int depth, boolean requireContinuous);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#checkVector(int,int)}.
     *
     * @param nativeObj    native binding handle of this {@code Mat} instance
     * @param elemChannels required channel count of each vector element
     * @param depth        single-channel element depth code
     * @return the vector element count, or -1 when the requested layout, depth or continuity requirements are not met
     */
    private static native int n_checkVector(long nativeObj, int elemChannels, int depth);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#checkVector(int)}.
     *
     * @param nativeObj    native binding handle of this {@code Mat} instance
     * @param elemChannels required channel count of each vector element
     * @return the vector element count, or -1 when the requested layout, depth or continuity requirements are not met
     */
    private static native int n_checkVector(long nativeObj, int elemChannels);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#clone()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return native binding handle for a new matrix header with an independent copy of the element data
     */
    private static native long n_clone(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#col(int)}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @param x         horizontal coordinate
     * @return native binding handle for a one-column view sharing this matrix's storage
     */
    private static native long n_col(long nativeObj, int x);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#colRange(int,int)}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @param startcol  inclusive zero-based index of the first selected column
     * @param endcol    exclusive zero-based index after the last selected column
     * @return native binding handle for a view of the selected half-open column interval, sharing storage
     */
    private static native long n_colRange(long nativeObj, int startcol, int endcol);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#dims()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return the number of dimensions
     */
    private static native int n_dims(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#cols()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return the number of columns
     */
    private static native int n_cols(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#convertTo(Mat,int,double,double)}.
     *
     * @param nativeObj   native binding handle of this {@code Mat} instance
     * @param m_nativeObj native matrix or object handle for {@code m}; destination matrix receiving the converted
     *                    channel values
     * @param rtype       desired result type or depth code
     * @param alpha       multiplicative factor applied before conversion
     * @param beta        additive offset applied after multiplication and before conversion
     */
    private static native void n_convertTo(long nativeObj, long m_nativeObj, int rtype, double alpha, double beta);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#convertTo(Mat,int,double)}.
     *
     * @param nativeObj   native binding handle of this {@code Mat} instance
     * @param m_nativeObj native matrix or object handle for {@code m}; destination matrix receiving the converted
     *                    channel values
     * @param rtype       desired result type or depth code
     * @param alpha       multiplicative factor applied before conversion
     */
    private static native void n_convertTo(long nativeObj, long m_nativeObj, int rtype, double alpha);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#convertTo(Mat,int)}.
     *
     * @param nativeObj   native binding handle of this {@code Mat} instance
     * @param m_nativeObj native matrix or object handle for {@code m}; destination matrix receiving the converted
     *                    channel values
     * @param rtype       desired result type or depth code
     */
    private static native void n_convertTo(long nativeObj, long m_nativeObj, int rtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#copyTo(Mat)}.
     *
     * @param nativeObj   native binding handle of this {@code Mat} instance
     * @param m_nativeObj native matrix or object handle for {@code m}; destination matrix receiving copied elements
     */
    private static native void n_copyTo(long nativeObj, long m_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#copyTo(Mat,Mat)}.
     *
     * @param nativeObj      native binding handle of this {@code Mat} instance
     * @param m_nativeObj    native matrix or object handle for {@code m}; destination matrix receiving copied elements
     * @param mask_nativeObj native matrix or object handle for {@code mask}; 8-bit selection mask whose nonzero entries
     *                       select matrix elements; an empty mask selects all elements
     */
    private static native void n_copyTo(long nativeObj, long m_nativeObj, long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#create(int,int,int)}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @param rows      number of matrix rows
     * @param cols      number of matrix columns
     * @param type      packed matrix channel-depth and channel-count code
     */
    private static native void n_create(long nativeObj, int rows, int cols, int type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#create(Size,int)}.
     *
     * @param nativeObj   native binding handle of this {@code Mat} instance
     * @param size_width  {@code size.width} component of width and height of the requested region or matrix
     * @param size_height {@code size.height} component of width and height of the requested region or matrix
     * @param type        packed matrix channel-depth and channel-count code
     */
    private static native void n_create(long nativeObj, double size_width, double size_height, int type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#create(int[],int)}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @param ndims     number of entries in {@code sizes}
     * @param sizes     extent of each matrix dimension
     * @param type      packed matrix channel-depth and channel-count code
     */
    private static native void n_create(long nativeObj, int ndims, int[] sizes, int type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#copySize(Mat)}.
     *
     * @param nativeObj   native binding handle of this {@code Mat} instance
     * @param m_nativeObj native matrix or object handle for {@code m}; matrix whose dimension sizes are copied
     */
    private static native void n_copySize(long nativeObj, long m_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#cross(Mat)}.
     *
     * @param nativeObj   native binding handle of this {@code Mat} instance
     * @param m_nativeObj native matrix or object handle for {@code m}; three-element vector used as the second
     *                    cross-product operand
     * @return native binding handle for a newly allocated matrix containing the three-dimensional cross product
     */
    private static native long n_cross(long nativeObj, long m_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#dataAddr()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return the address of the first data element
     */
    private static native long n_dataAddr(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#depth()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return the single-channel depth code
     */
    private static native int n_depth(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#diag(int)}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @param d         diagonal offset: zero selects the main diagonal, positive values select upper diagonals and
     *                  negative values select lower diagonals
     * @return native binding handle for a column-vector view of the requested diagonal, sharing this matrix's storage
     */
    private static native long n_diag(long nativeObj, int d);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#diag(Mat)}.
     *
     * @param d_nativeObj native matrix or object handle for {@code d}; diagonal offset: zero selects the main diagonal,
     *                    positive values select upper diagonals and negative values select lower diagonals
     * @return native binding handle for a diagonal matrix initialized from the supplied vector
     */
    private static native long n_diag(long d_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#dot(Mat)}.
     *
     * @param nativeObj   native binding handle of this {@code Mat} instance
     * @param m_nativeObj native matrix or object handle for {@code m}; second operand of the vector dot product
     * @return the vector dot product
     */
    private static native double n_dot(long nativeObj, long m_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#elemSize()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return the byte size of a complete element
     */
    private static native long n_elemSize(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#elemSize1()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return the byte size of one channel value
     */
    private static native long n_elemSize1(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#empty()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return true if the matrix has zero elements or no data storage
     */
    private static native boolean n_empty(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#eye(int,int,int)}.
     *
     * @param rows number of matrix rows
     * @param cols number of matrix columns
     * @param type packed matrix channel-depth and channel-count code
     * @return native binding handle for a matrix with ones on its main diagonal and zeros elsewhere
     */
    private static native long n_eye(int rows, int cols, int type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#eye(Size,int)}.
     *
     * @param size_width  {@code size.width} component of width and height of the requested region or matrix
     * @param size_height {@code size.height} component of width and height of the requested region or matrix
     * @param type        packed matrix channel-depth and channel-count code
     * @return native binding handle for a matrix with ones on its main diagonal and zeros elsewhere
     */
    private static native long n_eye(double size_width, double size_height, int type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#inv(int)}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @param method    algorithm or decomposition selector
     * @return native binding handle for the matrix inverse or pseudoinverse selected by the decomposition method
     */
    private static native long n_inv(long nativeObj, int method);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#inv()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return native binding handle for the matrix inverse or pseudoinverse selected by the decomposition method
     */
    private static native long n_inv(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#isContinuous()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return true if adjacent rows have no storage gaps
     */
    private static native boolean n_isContinuous(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#isSubmatrix()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return true if this header represents a view into a parent matrix
     */
    private static native boolean n_isSubmatrix(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#locateROI(Size,Point)}.
     *
     * @param nativeObj     native binding handle of this {@code Mat} instance
     * @param wholeSize_out output array receiving {@code wholeSize} from the native call
     * @param ofs_out       output array receiving {@code ofs} from the native call
     */
    private static native void locateROI_0(long nativeObj, double[] wholeSize_out, double[] ofs_out);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#mul(Mat,double)}.
     *
     * @param nativeObj   native binding handle of this {@code Mat} instance
     * @param m_nativeObj native matrix or object handle for {@code m}; matrix supplying the selected storage or values
     * @param scale       scale factor
     * @return native binding handle for reference to a new Mat object
     */
    private static native long n_mul(long nativeObj, long m_nativeObj, double scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#mul(Mat)}.
     *
     * @param nativeObj   native binding handle of this {@code Mat} instance
     * @param m_nativeObj native matrix or object handle for {@code m}; matrix supplying the selected storage or values
     * @return native binding handle for reference to a new Mat object
     */
    private static native long n_mul(long nativeObj, long m_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#matMul(Mat)}.
     *
     * @param nativeObj   native binding handle of this {@code Mat} instance
     * @param m_nativeObj native matrix or object handle for {@code m}; matrix supplying the selected storage or values
     * @return native binding handle for reference to a new Mat object
     */
    private static native long n_matMul(long nativeObj, long m_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#ones(int,int,int)}.
     *
     * @param rows number of matrix rows
     * @param cols number of matrix columns
     * @param type packed matrix channel-depth and channel-count code
     * @return native binding handle for a matrix initialized with Scalar(1); in a multichannel matrix only channel zero
     *         is set to one
     */
    private static native long n_ones(int rows, int cols, int type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#ones(Size,int)}.
     *
     * @param size_width  {@code size.width} component of width and height of the requested region or matrix
     * @param size_height {@code size.height} component of width and height of the requested region or matrix
     * @param type        packed matrix channel-depth and channel-count code
     * @return native binding handle for a matrix initialized with Scalar(1); in a multichannel matrix only channel zero
     *         is set to one
     */
    private static native long n_ones(double size_width, double size_height, int type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#ones(int[],int)}.
     *
     * @param ndims number of entries in {@code sizes}
     * @param sizes extent of each matrix dimension
     * @param type  packed matrix channel-depth and channel-count code
     * @return native binding handle for a matrix initialized with Scalar(1); in a multichannel matrix only channel zero
     *         is set to one
     */
    private static native long n_ones(int ndims, int[] sizes, int type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#push_back(Mat)}.
     *
     * @param nativeObj   native binding handle of this {@code Mat} instance
     * @param m_nativeObj native matrix or object handle for {@code m}; matrix whose rows are appended
     */
    private static native void n_push_back(long nativeObj, long m_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#release()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     */
    private static native void n_release(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#reshape(int,int)}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @param cn        number of channels per matrix element
     * @param rows      number of matrix rows
     * @return native binding handle for a new matrix header with the requested shape and channels, sharing the original
     *         data
     */
    private static native long n_reshape(long nativeObj, int cn, int rows);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#reshape(int)}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @param cn        number of channels per matrix element
     * @return native binding handle for a new matrix header with the requested shape and channels, sharing the original
     *         data
     */
    private static native long n_reshape(long nativeObj, int cn);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#reshape(int,int[])}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @param cn        number of channels per matrix element
     * @param newndims  number of entries in {@code newshape}
     * @param newsz     new extent of each matrix dimension; the total number of channel values must be preserved
     * @return native binding handle for a new matrix header with the requested shape and channels, sharing the original
     *         data
     */
    private static native long n_reshape_1(long nativeObj, int cn, int newndims, int[] newsz);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#row(int)}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @param y         vertical coordinate
     * @return native binding handle for a one-row view sharing this matrix's storage
     */
    private static native long n_row(long nativeObj, int y);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#rowRange(int,int)}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @param startrow  inclusive zero-based index of the first selected row
     * @param endrow    exclusive zero-based index after the last selected row
     * @return native binding handle for a view of the selected half-open row interval, sharing storage
     */
    private static native long n_rowRange(long nativeObj, int startrow, int endrow);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#rows()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return the number of rows
     */
    private static native int n_rows(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#setTo(Scalar)}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @param s_val0    {@code s.val[0]} component of channel values used to initialize or update the matrix
     * @param s_val1    {@code s.val[1]} component of channel values used to initialize or update the matrix
     * @param s_val2    {@code s.val[2]} component of channel values used to initialize or update the matrix
     * @param s_val3    {@code s.val[3]} component of channel values used to initialize or update the matrix
     * @return native binding handle for a new header sharing this matrix's storage after the selected values have been
     *         assigned
     */
    private static native long n_setTo(long nativeObj, double s_val0, double s_val1, double s_val2, double s_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#setTo(Scalar,Mat)}.
     *
     * @param nativeObj      native binding handle of this {@code Mat} instance
     * @param s_val0         {@code value.val[0]} component of matrix of channel values used to fill the selected
     *                       elements
     * @param s_val1         {@code value.val[1]} component of matrix of channel values used to fill the selected
     *                       elements
     * @param s_val2         {@code value.val[2]} component of matrix of channel values used to fill the selected
     *                       elements
     * @param s_val3         {@code value.val[3]} component of matrix of channel values used to fill the selected
     *                       elements
     * @param mask_nativeObj native matrix or object handle for {@code mask}; 8-bit selection mask whose nonzero entries
     *                       select matrix elements; an empty mask selects all elements
     * @return native binding handle for a new header sharing this matrix's storage after the selected values have been
     *         assigned
     */
    private static native long n_setTo(
            long nativeObj,
            double s_val0,
            double s_val1,
            double s_val2,
            double s_val3,
            long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#setTo(Mat,Mat)}.
     *
     * @param nativeObj       native binding handle of this {@code Mat} instance
     * @param value_nativeObj native matrix or object handle for {@code value}; matrix of channel values used to fill
     *                        the selected elements
     * @param mask_nativeObj  native matrix or object handle for {@code mask}; 8-bit selection mask whose nonzero
     *                        entries select matrix elements; an empty mask selects all elements
     * @return native binding handle for a new header sharing this matrix's storage after the selected values have been
     *         assigned
     */
    private static native long n_setTo(long nativeObj, long value_nativeObj, long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#setTo(Mat)}.
     *
     * @param nativeObj       native binding handle of this {@code Mat} instance
     * @param value_nativeObj native matrix or object handle for {@code value}; matrix of channel values used to fill
     *                        the selected elements
     * @return native binding handle for a new header sharing this matrix's storage after the selected values have been
     *         assigned
     */
    private static native long n_setTo(long nativeObj, long value_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#size()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return packed numeric fields used by the Java wrapper to construct {@code Size}
     */
    private static native double[] n_size(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#size(int)}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @param i         zero-based matrix dimension whose size or stride is requested
     * @return the requested dimension extent
     */
    private static native int n_size_i(long nativeObj, int i);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#step1(int)}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @param i         zero-based matrix dimension whose size or stride is requested
     * @return the storage stride in single-channel elements along the requested dimension
     */
    private static native long n_step1(long nativeObj, int i);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#step1()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return the storage stride in single-channel elements along the requested dimension
     */
    private static native long n_step1(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#submat(int,int,int,int)}.
     *
     * @param nativeObj      native binding handle of this {@code Mat} instance
     * @param rowRange_start inclusive zero-based index of the first selected row
     * @param rowRange_end   exclusive zero-based index after the last selected row
     * @param colRange_start inclusive zero-based index of the first selected column
     * @param colRange_end   exclusive zero-based index after the last selected column
     * @return native binding handle for a new header for the selected region, sharing this matrix's storage
     */
    private static native long n_submat_rr(
            long nativeObj,
            int rowRange_start,
            int rowRange_end,
            int colRange_start,
            int colRange_end);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#submat(Range[])}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @param ranges    half-open range to select along each dimension
     * @return native binding handle for a new header for the selected region, sharing this matrix's storage
     */
    private static native long n_submat_ranges(long nativeObj, Range[] ranges);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#submat(Rect)}.
     *
     * @param nativeObj  native binding handle of this {@code Mat} instance
     * @param roi_x      {@code roi.x} component of rectangle identifying the region of interest
     * @param roi_y      {@code roi.y} component of rectangle identifying the region of interest
     * @param roi_width  {@code roi.width} component of rectangle identifying the region of interest
     * @param roi_height {@code roi.height} component of rectangle identifying the region of interest
     * @return native binding handle for a new header for the selected region, sharing this matrix's storage
     */
    private static native long n_submat(long nativeObj, int roi_x, int roi_y, int roi_width, int roi_height);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#t()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return native binding handle for a newly allocated transposed matrix
     */
    private static native long n_t(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#total()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return the element count over the requested dimensions, excluding channels
     */
    private static native long n_total(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#type()}.
     *
     * @param nativeObj native binding handle of this {@code Mat} instance
     * @return the packed depth-and-channel type code
     */
    private static native int n_type(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#zeros(int,int,int)}.
     *
     * @param rows number of matrix rows
     * @param cols number of matrix columns
     * @param type packed matrix channel-depth and channel-count code
     * @return native binding handle for a matrix with every channel value initialized to zero
     */
    private static native long n_zeros(int rows, int cols, int type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#zeros(Size,int)}.
     *
     * @param size_width  {@code size.width} component of width and height of the requested region or matrix
     * @param size_height {@code size.height} component of width and height of the requested region or matrix
     * @param type        packed matrix channel-depth and channel-count code
     * @return native binding handle for a matrix with every channel value initialized to zero
     */
    private static native long n_zeros(double size_width, double size_height, int type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#zeros(int[],int)}.
     *
     * @param ndims number of entries in {@code sizes}
     * @param sizes extent of each matrix dimension
     * @param type  packed matrix channel-depth and channel-count code
     * @return native binding handle for a matrix with every channel value initialized to zero
     */
    private static native long n_zeros(int ndims, int[] sizes, int type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#put(int,int,double[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param row   zero-based row index
     * @param col   zero-based column index
     * @param count number of entries in {@code data}
     * @param data  source array of channel values to write
     * @return the number of channel values written after native numeric conversion
     */
    private static native int nPutD(long self, int row, int col, int count, double[] data);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#put(int[],double[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param idx   zero-based indices of the matrix element
     * @param count number of entries in {@code data}
     * @param data  source array of channel values to write
     * @return the number of channel values written after native numeric conversion
     */
    private static native int nPutDIdx(long self, int[] idx, int count, double[] data);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#put(int,int,float[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param row   zero-based row index
     * @param col   zero-based column index
     * @param count number of entries in {@code data}
     * @param data  source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nPutF(long self, int row, int col, int count, float[] data);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#put(int[],float[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param idx   zero-based indices of the matrix element
     * @param count number of entries in {@code data}
     * @param data  source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nPutFIdx(long self, int[] idx, int count, float[] data);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#put(int,int,int[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param row   zero-based row index
     * @param col   zero-based column index
     * @param count number of entries in {@code data}
     * @param data  source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nPutI(long self, int row, int col, int count, int[] data);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#put(int[],int[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param idx   zero-based indices of the matrix element
     * @param count number of entries in {@code data}
     * @param data  source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nPutIIdx(long self, int[] idx, int count, int[] data);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#put(int,int,short[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param row   zero-based row index
     * @param col   zero-based column index
     * @param count number of entries in {@code data}
     * @param data  source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nPutS(long self, int row, int col, int count, short[] data);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#put(int[],short[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param idx   zero-based indices of the matrix element
     * @param count number of entries in {@code data}
     * @param data  source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nPutSIdx(long self, int[] idx, int count, short[] data);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#put(int,int,byte[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param row   zero-based row index
     * @param col   zero-based column index
     * @param count number of entries in {@code data}
     * @param data  source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nPutB(long self, int row, int col, int count, byte[] data);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#put(int[],byte[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param idx   zero-based indices of the matrix element
     * @param count number of entries in {@code data}
     * @param data  source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nPutBIdx(long self, int[] idx, int count, byte[] data);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#put(int,int,byte[],int,int)}.
     *
     * @param self   native binding handle of this {@code Mat} instance
     * @param row    zero-based row index
     * @param col    zero-based column index
     * @param count  number of array entries to copy
     * @param offset zero-based offset of the first source-array entry to copy
     * @param data   source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nPutBwOffset(long self, int row, int col, int count, int offset, byte[] data);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#put(int[],byte[],int,int)}.
     *
     * @param self   native binding handle of this {@code Mat} instance
     * @param idx    zero-based indices of the matrix element
     * @param count  number of array entries to copy
     * @param offset zero-based offset of the first source-array entry to copy
     * @param data   source array of channel values to write
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nPutBwIdxOffset(long self, int[] idx, int count, int offset, byte[] data);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#get(int,int,byte[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param row   zero-based row index
     * @param col   zero-based column index
     * @param count number of entries in {@code data}
     * @param vals  destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nGetB(long self, int row, int col, int count, byte[] vals);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#get(int[],byte[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param idx   zero-based indices of the matrix element
     * @param count number of entries in {@code data}
     * @param vals  destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nGetBIdx(long self, int[] idx, int count, byte[] vals);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#get(int,int,short[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param row   zero-based row index
     * @param col   zero-based column index
     * @param count number of entries in {@code data}
     * @param vals  destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nGetS(long self, int row, int col, int count, short[] vals);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#get(int[],short[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param idx   zero-based indices of the matrix element
     * @param count number of entries in {@code data}
     * @param vals  destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nGetSIdx(long self, int[] idx, int count, short[] vals);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#get(int,int,int[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param row   zero-based row index
     * @param col   zero-based column index
     * @param count number of entries in {@code data}
     * @param vals  destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nGetI(long self, int row, int col, int count, int[] vals);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#get(int[],int[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param idx   zero-based indices of the matrix element
     * @param count number of entries in {@code data}
     * @param vals  destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nGetIIdx(long self, int[] idx, int count, int[] vals);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#get(int,int,float[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param row   zero-based row index
     * @param col   zero-based column index
     * @param count number of entries in {@code data}
     * @param vals  destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nGetF(long self, int row, int col, int count, float[] vals);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#get(int[],float[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param idx   zero-based indices of the matrix element
     * @param count number of entries in {@code data}
     * @param vals  destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nGetFIdx(long self, int[] idx, int count, float[] vals);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#get(int,int,double[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param row   zero-based row index
     * @param col   zero-based column index
     * @param count number of entries in {@code data}
     * @param vals  destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nGetD(long self, int row, int col, int count, double[] vals);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#get(int[],double[])}.
     *
     * @param self  native binding handle of this {@code Mat} instance
     * @param idx   zero-based indices of the matrix element
     * @param count number of entries in {@code data}
     * @param vals  destination array receiving the channel values read
     * @return the number of bytes transferred, which can be smaller than the array capacity
     */
    private static native int nGetDIdx(long self, int[] idx, int count, double[] vals);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#get(int,int)}.
     *
     * @param self native binding handle of this {@code Mat} instance
     * @param row  zero-based row index
     * @param col  zero-based column index
     * @return the selected element's channel values converted to doubles, or null if the native accessor cannot address
     *         that element
     */
    private static native double[] nGet(long self, int row, int col);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#get(int[])}.
     *
     * @param self native binding handle of this {@code Mat} instance
     * @param idx  zero-based indices of the matrix element
     * @return the selected element's channel values converted to doubles, or null if the native accessor cannot address
     *         that element
     */
    private static native double[] nGetIdx(long self, int[] idx);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Mat#dump()}.
     *
     * @param self native binding handle of this {@code Mat} instance
     * @return a formatted representation of the matrix element values
     */
    private static native String nDump(long self);
}
