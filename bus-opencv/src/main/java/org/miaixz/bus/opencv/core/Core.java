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

import java.util.List;

import org.miaixz.bus.opencv.utils.Converters;

/**
 * Matrix arithmetic, numerical operations, type constants, and runtime information for the underlying OpenCV engine.
 */
public class Core {

    /**
     * Creates a Java API container. The utility operations are available through static methods.
     */
    public Core() {
        // No initialization required.
    }

    /**
     * Returns the OpenCV engine version recorded in these Java bindings.
     *
     * @return the OpenCV engine version embedded in the Java bindings
     */
    private static String getVersion() {
        return "5.1.0";
    }

    /**
     * Returns the base name used to load the native binding library.
     *
     * @return the unversioned library base name opencv_java
     */
    private static String getNativeLibraryName() {
        return "opencv_java";
    }

    /**
     * Returns the major version of the underlying OpenCV engine.
     *
     * @return the Java binding's OpenCV engine major version
     */
    private static int getVersionMajorJ() {
        return 5;
    }

    /**
     * Returns the minor version of the underlying OpenCV engine.
     *
     * @return the Java binding's OpenCV engine minor version
     */
    private static int getVersionMinorJ() {
        return 1;
    }

    /**
     * Returns the revision of the underlying OpenCV engine.
     *
     * @return the Java binding's OpenCV engine patch version
     */
    private static int getVersionRevisionJ() {
        return 0;
    }

    /**
     * Returns the status suffix of the underlying OpenCV engine version.
     *
     * @return the status suffix embedded in the OpenCV engine version
     */
    private static String getVersionStatusJ() {
        return "";
    }

    /**
     * Version string of the underlying OpenCV engine; independent of the bus component version.
     */
    public static final String VERSION = getVersion();
    /**
     * Unversioned base name used to load the packaged native binding library.
     */
    public static final String NATIVE_LIBRARY_NAME = getNativeLibraryName();
    /**
     * Major version of the underlying OpenCV engine.
     */
    public static final int VERSION_MAJOR = getVersionMajorJ();
    /**
     * Minor version of the underlying OpenCV engine.
     */
    public static final int VERSION_MINOR = getVersionMinorJ();
    /**
     * Revision of the underlying OpenCV engine.
     */
    public static final int VERSION_REVISION = getVersionRevisionJ();
    /**
     * Status suffix of the underlying OpenCV engine version.
     */
    public static final String VERSION_STATUS = getVersionStatusJ();

    /**
     * Depth code for unsigned 8-bit integer matrix channels.
     */
    private static final int CV_8U = 0;

    /**
     * Depth code for signed 8-bit integer matrix channels.
     */
    private static final int CV_8S = 1;

    /**
     * Depth code for unsigned 16-bit integer matrix channels.
     */
    private static final int CV_16U = 2;

    /**
     * Depth code for signed 16-bit integer matrix channels.
     */
    private static final int CV_16S = 3;

    /**
     * Depth code for signed 32-bit integer matrix channels.
     */
    private static final int CV_32S = 4;

    /**
     * Depth code for 32-bit floating-point matrix channels.
     */
    private static final int CV_32F = 5;

    /**
     * Depth code for 64-bit floating-point matrix channels.
     */
    private static final int CV_64F = 6;

    /**
     * Reserved legacy depth code; retained for compatibility.
     */
    private static final int CV_USRTYPE1 = 7;

    /**
     * Legacy request to allow modification of the source matrix during SVD; ignored by the current implementation.
     */
    public static final int SVD_MODIFY_A = 1;

    /**
     * Compute singular values without computing the left and right singular vectors.
     */
    public static final int SVD_NO_UV = 2;

    /**
     * Compute full square left and right singular-vector matrices rather than the reduced decomposition.
     */
    public static final int SVD_FULL_UV = 4;

    /**
     * Drawing thickness value that fills the interior of a shape.
     */
    public static final int FILLED = -1;

    /**
     * Reduce each selected row or column to the sum of its values.
     */
    public static final int REDUCE_SUM = 0;

    /**
     * The output is the mean vector of all rows/columns of the matrix.
     */
    public static final int REDUCE_AVG = 1;

    /**
     * The output is the maximum (column/row-wise) of all rows/columns of the matrix.
     */
    public static final int REDUCE_MAX = 2;

    /**
     * The output is the minimum (column/row-wise) of all rows/columns of the matrix.
     */
    public static final int REDUCE_MIN = 3;

    /**
     * Tensor layout is unspecified; no named dimension ordering is asserted.
     */
    public static final int DATA_LAYOUT_UNKNOWN = 0;

    /**
     * OpenCV data layout for 2D data.
     */
    public static final int DATA_LAYOUT_ND = 1;

    /**
     * OpenCV data layout for 4D data.
     */
    public static final int DATA_LAYOUT_NCHW = 2;

    /**
     * OpenCV data layout for 5D data.
     */
    public static final int DATA_LAYOUT_NCDHW = 3;

    /**
     * Tensorflow-like data layout for 4D data.
     */
    public static final int DATA_LAYOUT_NHWC = 4;

    /**
     * Tensorflow-like data layout for 5D data.
     */
    public static final int DATA_LAYOUT_NDHWC = 5;

    /**
     * Tensorflow-like data layout, it should only be used at tf or tflite model parsing.
     */
    public static final int DATA_LAYOUT_PLANAR = 6;

    /**
     * Block layout (also referred to as 'NC1HWC0'), which some accelerators need and even on CPU a better performance
     * may be achieved.
     */
    public static final int DATA_LAYOUT_BLOCK = 7;

    /**
     * Generate samples from a uniform distribution.
     */
    public static final int RNG_UNIFORM = 0;

    /**
     * Generate samples from a Gaussian distribution.
     */
    public static final int RNG_NORMAL = 1;

    /**
     * Default algorithm behaviour defined during OpenCV build.
     */
    public static final int ALGO_HINT_DEFAULT = 0;

    /**
     * Use generic portable implementation.
     */
    public static final int ALGO_HINT_ACCURATE = 1;

    /**
     * Allow alternative approximations to get faster implementation. Behaviour and result depends on a platform.
     */
    public static final int ALGO_HINT_APPROX = 2;

    /**
     * `iiiiii|abcdefgh|iiiiiii` with some specified `i`.
     */
    public static final int BORDER_CONSTANT = 0;

    /**
     * `aaaaaa|abcdefgh|hhhhhhh`.
     */
    public static final int BORDER_REPLICATE = 1;

    /**
     * `fedcba|abcdefgh|hgfedcb`.
     */
    public static final int BORDER_REFLECT = 2;

    /**
     * `cdefgh|abcdefgh|abcdefg`.
     */
    public static final int BORDER_WRAP = 3;

    /**
     * `gfedcb|abcdefgh|gfedcba`.
     */
    public static final int BORDER_REFLECT_101 = 4;

    /**
     * `uvwxyz|abcdefgh|ijklmno` - Treats outliers as transparent.
     */
    public static final int BORDER_TRANSPARENT = 5;

    /**
     * Same as BORDER_REFLECT_101.
     */
    public static final int BORDER_REFLECT101 = BORDER_REFLECT_101;

    /**
     * Same as BORDER_REFLECT_101.
     */
    public static final int BORDER_DEFAULT = BORDER_REFLECT_101;

    /**
     * Interpolation restricted within the ROI boundaries.
     */
    public static final int BORDER_ISOLATED = 16;

    /**
     * Set an output comparison element to 255 when the operands are equal, otherwise to zero.
     */
    public static final int CMP_EQ = 0;

    /**
     * Src1 is greater than src2.
     */
    public static final int CMP_GT = 1;

    /**
     * Src1 is greater than or equal to src2.
     */
    public static final int CMP_GE = 2;

    /**
     * Src1 is less than src2.
     */
    public static final int CMP_LT = 3;

    /**
     * Src1 is less than or equal to src2.
     */
    public static final int CMP_LE = 4;

    /**
     * Src1 is unequal to src2.
     */
    public static final int CMP_NE = 5;

    /**
     * Compute covariance between centered sample vectors, producing a square matrix whose dimension is the number of
     * samples.
     */
    public static final int COVAR_SCRAMBLED = 0;

    /**
     * Compute covariance between sample components, producing a square matrix whose dimension is the number of
     * components per sample.
     */
    public static final int COVAR_NORMAL = 1;

    /**
     * If the flag is specified, the function does not calculate mean from the input vectors but, instead, uses the
     * passed mean vector. This is useful if mean has been pre-calculated or known in advance, or if the covariance
     * matrix is calculated by parts. In this case, mean is not a mean vector of the input sub-set of vectors but rather
     * the mean vector of the whole set.
     */
    public static final int COVAR_USE_AVG = 2;

    /**
     * If the flag is specified, the covariance matrix is scaled. In the "normal" mode, scale is 1./nsamples . In the
     * "scrambled" mode, scale is the reciprocal of the total number of elements in each input vector. By default (if
     * the flag is not specified), the covariance matrix is not scaled ( scale=1 ).
     */
    public static final int COVAR_SCALE = 4;

    /**
     * If the flag is specified, all the input vectors are stored as rows of the samples matrix. mean should be a
     * single-row vector in this case.
     */
    public static final int COVAR_ROWS = 8;

    /**
     * If the flag is specified, all the input vectors are stored as columns of the samples matrix. mean should be a
     * single-column vector in this case.
     */
    public static final int COVAR_COLS = 16;

    /**
     * Gaussian elimination with the optimal pivot element chosen.
     */
    public static final int DECOMP_LU = 0;

    /**
     * Singular value decomposition (SVD) method; the system can be over-defined and/or the matrix src1 can be singular.
     */
    public static final int DECOMP_SVD = 1;

    /**
     * Eigenvalue decomposition; the matrix src1 must be symmetrical.
     */
    public static final int DECOMP_EIG = 2;

    /**
     * Cholesky LL^T factorization; the matrix src1 must be symmetrical and positively defined.
     */
    public static final int DECOMP_CHOLESKY = 3;

    /**
     * QR factorization; the system can be over-defined and/or the matrix src1 can be singular.
     */
    public static final int DECOMP_QR = 4;

    /**
     * Solve the normal equations formed from the transposed coefficient matrix. Combine this flag with a decomposition
     * method.
     */
    public static final int DECOMP_NORMAL = 16;

    /**
     * Performs an inverse 1D or 2D transform instead of the default forward transform.
     */
    public static final int DFT_INVERSE = 1;

    /**
     * Scales the result: divide it by the number of array elements. Normally, it is combined with DFT_INVERSE.
     */
    public static final int DFT_SCALE = 2;

    /**
     * Performs a forward or inverse transform of every individual row of the input matrix; this flag enables you to
     * transform multiple vectors simultaneously and can be used to decrease the overhead (which is sometimes several
     * times larger than the processing itself) to perform 3D and higher-dimensional transformations and so forth.
     */
    public static final int DFT_ROWS = 4;

    /**
     * Performs a forward transformation of 1D or 2D real array; the result, though being a complex array, has
     * complex-conjugate symmetry (*CCS*, see the function description below for details), and such an array can be
     * packed into a real array of the same size as input, which is the fastest option and which is what the function
     * does by default; however, you may wish to get a full complex array (for simpler spectrum analysis, and so on) -
     * pass the flag to enable the function to produce a full-size complex output array.
     */
    public static final int DFT_COMPLEX_OUTPUT = 16;

    /**
     * Performs an inverse transformation of a 1D or 2D complex array; the result is normally a complex array of the
     * same size, however, if the input array has conjugate-complex symmetry (for example, it is a result of forward
     * transformation with DFT_COMPLEX_OUTPUT flag), the output is a real array; while the function itself does not
     * check whether the input is symmetrical or not, you can pass the flag and then the function will assume the
     * symmetry and produce the real output array (note that when the input is packed into a real array and inverse
     * transformation is executed, the function treats the input as a packed complex-conjugate symmetrical array, and
     * the output will also be a real array).
     */
    public static final int DFT_REAL_OUTPUT = 32;

    /**
     * Specifies that input is complex input. If this flag is set, the input must have 2 channels. On the other hand,
     * for backwards compatibility reason, if input has 2 channels, input is already considered complex.
     */
    public static final int DFT_COMPLEX_INPUT = 64;

    /**
     * Performs an inverse 1D or 2D transform instead of the default forward transform.
     */
    public static final int DCT_INVERSE = DFT_INVERSE;

    /**
     * Performs a forward or inverse transform of every individual row of the input matrix. This flag enables you to
     * transform multiple vectors simultaneously and can be used to decrease the overhead (which is sometimes several
     * times larger than the processing itself) to perform 3D and higher-dimensional transforms and so forth.
     */
    public static final int DCT_ROWS = DFT_ROWS;

    /**
     * Matrix text-format selector for DEFAULT.
     */
    public static final int Formatter_FMT_DEFAULT = 0;

    /**
     * Matrix text-format selector for MATLAB.
     */
    public static final int Formatter_FMT_MATLAB = 1;

    /**
     * Matrix text-format selector for CSV.
     */
    public static final int Formatter_FMT_CSV = 2;

    /**
     * Matrix text-format selector for PYTHON.
     */
    public static final int Formatter_FMT_PYTHON = 3;

    /**
     * Matrix text-format selector for NUMPY.
     */
    public static final int Formatter_FMT_NUMPY = 4;

    /**
     * Matrix text-format selector for C.
     */
    public static final int Formatter_FMT_C = 5;

    /**
     * Transpose the first multiplicand before generalized matrix multiplication.
     */
    public static final int GEMM_1_T = 1;

    /**
     * Transposes src2.
     */
    public static final int GEMM_2_T = 2;

    /**
     * Transposes src3.
     */
    public static final int GEMM_3_T = 4;

    /**
     * Select random initial centers in each attempt.
     */
    public static final int KMEANS_RANDOM_CENTERS = 0;

    /**
     * Use kmeans++ center initialization by Arthur and Vassilvitskii [Arthur2007].
     */
    public static final int KMEANS_PP_CENTERS = 2;

    /**
     * During the first (and possibly the only) attempt, use the user-supplied labels instead of computing them from the
     * initial centers. For the second and further attempts, use the random or semi-random centers. Use one of
     * KMEANS_\*_CENTERS flag to specify the exact method.
     */
    public static final int KMEANS_USE_INITIAL_LABELS = 1;

    /**
     * Maximum absolute channel value, or maximum absolute channel difference for two inputs; NORM_RELATIVE divides by
     * the second input norm.
     */
    public static final int NORM_INF = 1;

    /**
     * Sum of absolute channel values, or of absolute channel differences for two inputs; NORM_RELATIVE divides by the
     * second input norm.
     */
    public static final int NORM_L1 = 2;

    /**
     * Square root of the sum of squared channel values or squared channel differences; NORM_RELATIVE divides by the
     * second input norm.
     */
    public static final int NORM_L2 = 4;

    /**
     * Sum of squared channel values or squared channel differences; NORM_RELATIVE divides by the squared norm of the
     * second input.
     */
    public static final int NORM_L2SQR = 5;

    /**
     * In the case of one input array, calculates the Hamming distance of the array from zero, In the case of two input
     * arrays, calculates the Hamming distance between the arrays.
     */
    public static final int NORM_HAMMING = 6;

    /**
     * Similar to NORM_HAMMING, but in the calculation, each two bits of the input sequence will be added and treated as
     * a single bit to be used in the same calculation as NORM_HAMMING.
     */
    public static final int NORM_HAMMING2 = 7;

    /**
     * Bit-mask which can be used to separate norm type from norm flags.
     */
    public static final int NORM_TYPE_MASK = 7;

    /**
     * Divide the norm of the difference by the norm of the second input.
     */
    public static final int NORM_RELATIVE = 8;

    /**
     * Normalize the selected source range to the requested lower and upper bounds.
     */
    public static final int NORM_MINMAX = 32;

    /**
     * Store each PCA sample in one row of the input matrix.
     */
    public static final int PCA_DATA_AS_ROW = 0;

    /**
     * Store each PCA sample in one column of the input matrix.
     */
    public static final int PCA_DATA_AS_COL = 1;

    /**
     * Use the caller-supplied mean rather than estimating the mean from the samples.
     */
    public static final int PCA_USE_AVG = 2;

    /**
     * Algorithm parameter type identifier for int.
     */
    public static final int Param_INT = 0;

    /**
     * Algorithm parameter type identifier for boolean.
     */
    public static final int Param_BOOLEAN = 1;

    /**
     * Algorithm parameter type identifier for real.
     */
    public static final int Param_REAL = 2;

    /**
     * Algorithm parameter type identifier for string.
     */
    public static final int Param_STRING = 3;

    /**
     * Algorithm parameter type identifier for mat.
     */
    public static final int Param_MAT = 4;

    /**
     * Algorithm parameter type identifier for mat vector.
     */
    public static final int Param_MAT_VECTOR = 5;

    /**
     * Algorithm parameter type identifier for algorithm.
     */
    public static final int Param_ALGORITHM = 6;

    /**
     * Algorithm parameter type identifier for float.
     */
    public static final int Param_FLOAT = 7;

    /**
     * Algorithm parameter type identifier for unsigned int.
     */
    public static final int Param_UNSIGNED_INT = 8;

    /**
     * Algorithm parameter type identifier for uint64.
     */
    public static final int Param_UINT64 = 9;

    /**
     * Algorithm parameter type identifier for uchar.
     */
    public static final int Param_UCHAR = 11;

    /**
     * Algorithm parameter type identifier for scalar.
     */
    public static final int Param_SCALAR = 12;

    /**
     * The output is the sum of all squared rows/columns of the matrix.
     */
    public static final int REDUCE_SUM2 = 4;

    /**
     * The output is the product of the elements.
     */
    public static final int REDUCE_PROD = 5;

    /**
     * The output is the sum of the absolute values.
     */
    public static final int REDUCE_L1 = 6;

    /**
     * The output is the square root of the sum of the squares.
     */
    public static final int REDUCE_L2 = 7;

    /**
     * The output is the logarithm of the sum.
     */
    public static final int REDUCE_LOG_SUM = 8;

    /**
     * The output is log(sum(exp(x))), computed without overflow.
     */
    public static final int REDUCE_LOG_SUM_EXP = 9;

    /**
     * Rotate 90 degrees clockwise.
     */
    public static final int ROTATE_90_CLOCKWISE = 0;

    /**
     * Rotate 180 degrees clockwise.
     */
    public static final int ROTATE_180 = 1;

    /**
     * Rotate 270 degrees clockwise.
     */
    public static final int ROTATE_90_COUNTERCLOCKWISE = 2;

    /**
     * Problem is feasible, but solver lost solution due to floating-point arithmetic errors.
     */
    public static final int SOLVELP_LOST = -3;

    /**
     * Problem is unbounded (target function can achieve arbitrary high values).
     */
    public static final int SOLVELP_UNBOUNDED = -2;

    /**
     * Problem is unfeasible (there are no points that satisfy all the constraints imposed).
     */
    public static final int SOLVELP_UNFEASIBLE = -1;

    /**
     * There is only one maximum for target function.
     */
    public static final int SOLVELP_SINGLE = 0;

    /**
     * There are multiple maxima for target function - the arbitrary one is returned.
     */
    public static final int SOLVELP_MULTI = 1;

    /**
     * Sort each matrix row independently.
     */
    public static final int SORT_EVERY_ROW = 0;

    /**
     * Each matrix column is sorted.
     */
    public static final int SORT_EVERY_COLUMN = 1;

    /**
     * Each matrix row is sorted in the ascending.
     */
    public static final int SORT_ASCENDING = 0;

    /**
     * Each matrix row is sorted in the.
     */
    public static final int SORT_DESCENDING = 16;

    /**
     * Computes the cube root of an argument.
     *
     * @param val A function argument
     * @return the real cube root of the argument
     */
    public static float cubeRoot(float val) {
        return cubeRoot_0(val);
    }

    /**
     * Calculates the angle of a 2D vector in degrees. The function fastAtan2 calculates the full-range angle of an
     * input 2D vector. The angle is measured in degrees and varies from 0 to 360 degrees. The accuracy is about 0.3
     * degrees.
     *
     * @param y y-coordinate of the vector
     * @param x x-coordinate of the vector
     * @return the vector angle in degrees in the range from 0 to 360
     */
    public static float fastAtan2(float y, float x) {
        return fastAtan2_0(y, x);
    }

    /**
     * Proxy for hal::Cholesky.
     *
     * @return true if IPP acceleration is enabled in this engine build
     */
    public static boolean useIPP() {
        return useIPP_0();
    }

    /**
     * Sets the use ipp used by this Core instance.
     *
     * @param flag whether the corresponding option is enabled
     */
    public static void setUseIPP(boolean flag) {
        setUseIPP_0(flag);
    }

    /**
     * Returns the IPP version string reported by this engine build.
     *
     * @return the IPP version string reported by this engine build
     */
    public static String getIppVersion() {
        return getIppVersion_0();
    }

    /**
     * Reports whether accelerated implementations that may differ in numerical precision are enabled.
     *
     * @return true if IPP implementations that can differ numerically are enabled
     */
    public static boolean useIPP_NotExact() {
        return useIPP_NotExact_0();
    }

    /**
     * Sets the use ipp not exact used by this Core instance.
     *
     * @param flag whether the corresponding option is enabled
     */
    public static void setUseIPP_NotExact(boolean flag) {
        setUseIPP_NotExact_0(flag);
    }

    /**
     * Computes the source location of an extrapolated pixel. The function computes and returns the coordinate of a
     * donor pixel corresponding to the specified extrapolated pixel when using the specified extrapolation border mode.
     * For example, if you use BORDER_WRAP mode in the horizontal direction, BORDER_REFLECT_101 in the vertical
     * direction and want to compute value of the "virtual" pixel Point(-5, 100) in a floating-point image img, it looks
     * like: float val = img.at&lt;float&gt;(borderInterpolate(100, img.rows, BORDER_REFLECT_101), borderInterpolate(-5,
     * img.cols, BORDER_WRAP)); Normally, the function is not called directly. It is used inside filtering functions and
     * also in copyMakeBorder.
     *
     * @param p          0-based coordinate of the extrapolated pixel along one of the axes, likely &lt;0 or &gt;= len
     * @param len        Length of the array along the corresponding axis
     * @param borderType Border type, one of the BorderTypes, except for BORDER_TRANSPARENT and BORDER_ISOLATED. When
     *                   borderType==BORDER_CONSTANT, the function always returns -1, regardless of p and len
     * @return the source coordinate selected by the border rule, or -1 for a constant border
     */
    public static int borderInterpolate(int p, int len, int borderType) {
        return borderInterpolate_0(p, len, borderType);
    }

    /**
     * Forms a border around an image. The function copies the source image into the middle of the destination image.
     * The areas to the left, to the right, above and below the copied source image will be filled with extrapolated
     * pixels. This is not what filtering functions based on it do (they extrapolate pixels on-fly), but what other more
     * complex functions, including your own, may do to simplify image boundary handling. The function supports the mode
     * when src is already in the middle of dst . In this case, the function does not copy src itself but simply
     * constructs the border, for example: // let border be the same in all directions int border=2; // constructs a
     * larger image to fit both the image and the border Mat gray_buf(rgb.rows + border*2, rgb.cols + border*2,
     * rgb.depth()); // select the middle part of it w/o copying data Mat gray(gray_canvas, Rect(border, border,
     * rgb.cols, rgb.rows)); // convert image from RGB to grayscale cvtColor(rgb, gray, COLOR_RGB2GRAY); // form a
     * border in-place copyMakeBorder(gray, gray_buf, border, border, border, border, BORDER_REPLICATE); // now do some
     * custom filtering ... ... Note: When the source image is a part (ROI) of a bigger image, the function will try to
     * use the pixels outside of the ROI to form a border. To disable this feature and always do extrapolation, as if
     * src was not a ROI, use borderType | BORDER_ISOLATED.
     *
     * @param src        Source image
     * @param dst        Destination image of the same type as src and the size Size(src.cols+left+right,
     *                   src.rows+top+bottom)
     * @param top        the top pixels
     * @param bottom     the bottom pixels
     * @param left       the left pixels
     * @param right      Parameter specifying how many pixels in each direction from the source image rectangle to
     *                   extrapolate. For example, top=1, bottom=1, left=1, right=1 mean that 1 pixel-wide border needs
     *                   to be built
     * @param borderType Border type. See borderInterpolate for details
     * @param value      Border value if borderType==BORDER_CONSTANT
     * @throws NullPointerException if src, dst, value is null
     */
    public static void copyMakeBorder(
            Mat src,
            Mat dst,
            int top,
            int bottom,
            int left,
            int right,
            int borderType,
            Scalar value) {
        copyMakeBorder_0(
                src.nativeObj,
                dst.nativeObj,
                top,
                bottom,
                left,
                right,
                borderType,
                value.val[0],
                value.val[1],
                value.val[2],
                value.val[3]);
    }

    /**
     * Forms a border around an image. The function copies the source image into the middle of the destination image.
     * The areas to the left, to the right, above and below the copied source image will be filled with extrapolated
     * pixels. This is not what filtering functions based on it do (they extrapolate pixels on-fly), but what other more
     * complex functions, including your own, may do to simplify image boundary handling. The function supports the mode
     * when src is already in the middle of dst . In this case, the function does not copy src itself but simply
     * constructs the border, for example: // let border be the same in all directions int border=2; // constructs a
     * larger image to fit both the image and the border Mat gray_buf(rgb.rows + border*2, rgb.cols + border*2,
     * rgb.depth()); // select the middle part of it w/o copying data Mat gray(gray_canvas, Rect(border, border,
     * rgb.cols, rgb.rows)); // convert image from RGB to grayscale cvtColor(rgb, gray, COLOR_RGB2GRAY); // form a
     * border in-place copyMakeBorder(gray, gray_buf, border, border, border, border, BORDER_REPLICATE); // now do some
     * custom filtering ... ... Note: When the source image is a part (ROI) of a bigger image, the function will try to
     * use the pixels outside of the ROI to form a border. To disable this feature and always do extrapolation, as if
     * src was not a ROI, use borderType | BORDER_ISOLATED.
     *
     * @param src        Source image
     * @param dst        Destination image of the same type as src and the size Size(src.cols+left+right,
     *                   src.rows+top+bottom)
     * @param top        the top pixels
     * @param bottom     the bottom pixels
     * @param left       the left pixels
     * @param right      Parameter specifying how many pixels in each direction from the source image rectangle to
     *                   extrapolate. For example, top=1, bottom=1, left=1, right=1 mean that 1 pixel-wide border needs
     *                   to be built
     * @param borderType Border type. See borderInterpolate for details
     * @throws NullPointerException if src, dst is null
     */
    public static void copyMakeBorder(Mat src, Mat dst, int top, int bottom, int left, int right, int borderType) {
        copyMakeBorder_1(src.nativeObj, dst.nativeObj, top, bottom, left, right, borderType);
    }

    /**
     * Calculates the per-element sum of two arrays or an array and a scalar.
     *
     * @param src1  first input array or a scalar
     * @param src2  second input array or a scalar
     * @param dst   output array that has the same size and number of channels as the input array(s); the depth is
     *              defined by dtype or src1/src2
     * @param mask  optional operation mask - CV_8U, CV_8S or CV_Bool single channel array, that specifies elements of
     *              the output array to be changed
     * @param dtype optional depth of the output array (see the discussion below)
     * @throws NullPointerException if src1, src2, dst, mask is null
     */
    public static void add(Mat src1, Mat src2, Mat dst, Mat mask, int dtype) {
        add_0(src1.nativeObj, src2.nativeObj, dst.nativeObj, mask.nativeObj, dtype);
    }

    /**
     * Calculates the per-element sum of two arrays or an array and a scalar.
     *
     * @param src1 first input array or a scalar
     * @param src2 second input array or a scalar
     * @param dst  output array that has the same size and number of channels as the input array(s); the depth is
     *             defined by dtype or src1/src2
     * @param mask optional operation mask - CV_8U, CV_8S or CV_Bool single channel array, that specifies elements of
     *             the output array to be changed
     * @throws NullPointerException if src1, src2, dst, mask is null
     */
    public static void add(Mat src1, Mat src2, Mat dst, Mat mask) {
        add_1(src1.nativeObj, src2.nativeObj, dst.nativeObj, mask.nativeObj);
    }

    /**
     * Calculates the per-element sum of two arrays or an array and a scalar.
     *
     * @param src1 first input array or a scalar
     * @param src2 second input array or a scalar
     * @param dst  output array that has the same size and number of channels as the input array(s); the depth is
     *             defined by dtype or src1/src2
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void add(Mat src1, Mat src2, Mat dst) {
        add_2(src1.nativeObj, src2.nativeObj, dst.nativeObj);
    }

    /**
     * Calculates the per-element difference between two arrays or array and a scalar.
     *
     * @param src1  first input array or a scalar
     * @param src2  second input array or a scalar
     * @param dst   output array of the same size and the same number of channels as the input array
     * @param mask  optional operation mask; this is CV_8U, CV8S or CV_Bool single channel array that specifies elements
     *              of the output array to be changed
     * @param dtype optional depth of the output array
     * @throws NullPointerException if src1, src2, dst, mask is null
     */
    public static void subtract(Mat src1, Mat src2, Mat dst, Mat mask, int dtype) {
        subtract_0(src1.nativeObj, src2.nativeObj, dst.nativeObj, mask.nativeObj, dtype);
    }

    /**
     * Calculates the per-element difference between two arrays or array and a scalar.
     *
     * @param src1 first input array or a scalar
     * @param src2 second input array or a scalar
     * @param dst  output array of the same size and the same number of channels as the input array
     * @param mask optional operation mask; this is CV_8U, CV8S or CV_Bool single channel array that specifies elements
     *             of the output array to be changed
     * @throws NullPointerException if src1, src2, dst, mask is null
     */
    public static void subtract(Mat src1, Mat src2, Mat dst, Mat mask) {
        subtract_1(src1.nativeObj, src2.nativeObj, dst.nativeObj, mask.nativeObj);
    }

    /**
     * Calculates the per-element difference between two arrays or array and a scalar.
     *
     * @param src1 first input array or a scalar
     * @param src2 second input array or a scalar
     * @param dst  output array of the same size and the same number of channels as the input array
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void subtract(Mat src1, Mat src2, Mat dst) {
        subtract_2(src1.nativeObj, src2.nativeObj, dst.nativeObj);
    }

    /**
     * Calculates the per-element scaled product of two arrays.
     *
     * @param src1  first input array
     * @param src2  second input array of the same size and the same type as src1
     * @param dst   output array of the same size and type as src1
     * @param scale optional scale factor
     * @param dtype optional depth of the output array
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void multiply(Mat src1, Mat src2, Mat dst, double scale, int dtype) {
        multiply_0(src1.nativeObj, src2.nativeObj, dst.nativeObj, scale, dtype);
    }

    /**
     * Calculates the per-element scaled product of two arrays.
     *
     * @param src1  first input array
     * @param src2  second input array of the same size and the same type as src1
     * @param dst   output array of the same size and type as src1
     * @param scale optional scale factor
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void multiply(Mat src1, Mat src2, Mat dst, double scale) {
        multiply_1(src1.nativeObj, src2.nativeObj, dst.nativeObj, scale);
    }

    /**
     * Calculates the per-element scaled product of two arrays.
     *
     * @param src1 first input array
     * @param src2 second input array of the same size and the same type as src1
     * @param dst  output array of the same size and type as src1
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void multiply(Mat src1, Mat src2, Mat dst) {
        multiply_2(src1.nativeObj, src2.nativeObj, dst.nativeObj);
    }

    /**
     * Performs per-element division of two arrays or a scalar by an array.
     *
     * @param src1  first input array
     * @param src2  second input array of the same size and type as src1
     * @param dst   output array of the same size and type as src2
     * @param scale scalar factor
     * @param dtype optional depth of the output array; if -1, dst will have depth src2.depth(), but in case of an
     *              array-by-array division, you can only pass -1 when src1.depth()==src2.depth()
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void divide(Mat src1, Mat src2, Mat dst, double scale, int dtype) {
        divide_0(src1.nativeObj, src2.nativeObj, dst.nativeObj, scale, dtype);
    }

    /**
     * Performs per-element division of two arrays or a scalar by an array.
     *
     * @param src1  first input array
     * @param src2  second input array of the same size and type as src1
     * @param dst   output array of the same size and type as src2
     * @param scale scalar factor
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void divide(Mat src1, Mat src2, Mat dst, double scale) {
        divide_1(src1.nativeObj, src2.nativeObj, dst.nativeObj, scale);
    }

    /**
     * Performs per-element division of two arrays or a scalar by an array.
     *
     * @param src1 first input array
     * @param src2 second input array of the same size and type as src1
     * @param dst  output array of the same size and type as src2
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void divide(Mat src1, Mat src2, Mat dst) {
        divide_2(src1.nativeObj, src2.nativeObj, dst.nativeObj);
    }

    /**
     * Performs per-element division of two arrays or a scalar by an array.
     *
     * @param scale scalar factor
     * @param src2  second input array of the same size and type as src1
     * @param dst   output array of the same size and type as src2
     * @param dtype optional depth of the output array; if -1, dst will have depth src2.depth(), but in case of an
     *              array-by-array division, you can only pass -1 when src1.depth()==src2.depth()
     * @throws NullPointerException if src2, dst is null
     */
    public static void divide(double scale, Mat src2, Mat dst, int dtype) {
        divide_3(scale, src2.nativeObj, dst.nativeObj, dtype);
    }

    /**
     * Performs per-element division of two arrays or a scalar by an array.
     *
     * @param scale scalar factor
     * @param src2  second input array of the same size and type as src1
     * @param dst   output array of the same size and type as src2
     * @throws NullPointerException if src2, dst is null
     */
    public static void divide(double scale, Mat src2, Mat dst) {
        divide_4(scale, src2.nativeObj, dst.nativeObj);
    }

    /**
     * Calculates the sum of a scaled array and another array.
     *
     * @param src1  first input array
     * @param alpha scale factor for the first array
     * @param src2  second input array of the same size and type as src1
     * @param dst   output array of the same size and type as src1
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void scaleAdd(Mat src1, double alpha, Mat src2, Mat dst) {
        scaleAdd_0(src1.nativeObj, alpha, src2.nativeObj, dst.nativeObj);
    }

    /**
     * Calculates the weighted sum of two arrays.
     *
     * @param src1  first input array
     * @param alpha weight of the first array elements
     * @param src2  second input array of the same size and channel number as src1
     * @param beta  weight of the second array elements
     * @param gamma scalar added to each sum
     * @param dst   output array that has the same size and number of channels as the input arrays
     * @param dtype optional depth of the output array; when both input arrays have the same depth, dtype can be set to
     *              -1, which will be equivalent to src1.depth()
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void addWeighted(Mat src1, double alpha, Mat src2, double beta, double gamma, Mat dst, int dtype) {
        addWeighted_0(src1.nativeObj, alpha, src2.nativeObj, beta, gamma, dst.nativeObj, dtype);
    }

    /**
     * Calculates the weighted sum of two arrays.
     *
     * @param src1  first input array
     * @param alpha weight of the first array elements
     * @param src2  second input array of the same size and channel number as src1
     * @param beta  weight of the second array elements
     * @param gamma scalar added to each sum
     * @param dst   output array that has the same size and number of channels as the input arrays
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void addWeighted(Mat src1, double alpha, Mat src2, double beta, double gamma, Mat dst) {
        addWeighted_1(src1.nativeObj, alpha, src2.nativeObj, beta, gamma, dst.nativeObj);
    }

    /**
     * Scales, calculates absolute values, and converts the result to 8-bit.
     *
     * @param src   input array
     * @param dst   output array
     * @param alpha optional scale factor
     * @param beta  optional delta added to the scaled values
     * @throws NullPointerException if src, dst is null
     */
    public static void convertScaleAbs(Mat src, Mat dst, double alpha, double beta) {
        convertScaleAbs_0(src.nativeObj, dst.nativeObj, alpha, beta);
    }

    /**
     * Scales, calculates absolute values, and converts the result to 8-bit.
     *
     * @param src   input array
     * @param dst   output array
     * @param alpha optional scale factor
     * @throws NullPointerException if src, dst is null
     */
    public static void convertScaleAbs(Mat src, Mat dst, double alpha) {
        convertScaleAbs_1(src.nativeObj, dst.nativeObj, alpha);
    }

    /**
     * Scales, calculates absolute values, and converts the result to 8-bit.
     *
     * @param src input array
     * @param dst output array
     * @throws NullPointerException if src, dst is null
     */
    public static void convertScaleAbs(Mat src, Mat dst) {
        convertScaleAbs_2(src.nativeObj, dst.nativeObj);
    }

    /**
     * Performs a look-up table transform of an array.
     *
     * @param src input array of 8-bit or 16-bit integer elements
     * @param lut look-up table of 256 elements (if src has depth CV_8U or CV_8S) or 65536 elements(if src has depth
     *            CV_16U or CV_16S); in case of multi-channel input array, the table should either have a single channel
     *            (in this case the same table is used for all channels) or the same number of channels as in the input
     *            array
     * @param dst output array of the same size and number of channels as src, and the same depth as lut
     * @throws NullPointerException if src, lut, dst is null
     */
    public static void LUT(Mat src, Mat lut, Mat dst) {
        LUT_0(src.nativeObj, lut.nativeObj, dst.nativeObj);
    }

    /**
     * Calculates the sum of array elements. The function sum calculates and returns the sum of array elements,
     * independently for each channel.
     *
     * @param src input array that must have from 1 to 4 channels
     * @return the sum of each matrix channel
     * @throws NullPointerException if src is null
     */
    public static Scalar sumElems(Mat src) {
        return new Scalar(sumElems_0(src.nativeObj));
    }

    /**
     * Checks for the presence of at least one non-zero array element. The function returns whether there are non-zero
     * elements in src The function do not work with multi-channel arrays. If you need to check non-zero array elements
     * across all the channels, use Mat.reshape first to reinterpret the array as single-channel. Or you may extract the
     * particular channel using either extractImageCOI, or mixChannels, or split. Note:
     * CV_16F/CV_16BF/CV_Bool/CV_64U/CV_64S/CV_32U are not supported for src. If the location of non-zero array elements
     * is important, REF: findNonZero is helpful. If the count of non-zero array elements is important, REF:
     * countNonZero is helpful.
     *
     * @param src single-channel array
     * @return true if at least one source element is nonzero
     * @throws NullPointerException if src is null
     */
    public static boolean hasNonZero(Mat src) {
        return hasNonZero_0(src.nativeObj);
    }

    /**
     * Counts non-zero array elements.
     *
     * @param src single-channel array
     * @return the number of nonzero single-channel elements
     * @throws NullPointerException if src is null
     */
    public static int countNonZero(Mat src) {
        return countNonZero_0(src.nativeObj);
    }

    /**
     * Returns the list of locations of non-zero pixels Given a binary matrix (likely returned from an operation such as
     * threshold(), compare(), &gt;, ==, etc, return all of the non-zero indices as a Mat or vector&lt;Point&gt; (x,y)
     * For example: Mat binaryImage; // input, binary image Mat locations; // output, locations of non-zero pixels
     * findNonZero(binaryImage, locations); // access pixel coordinates Point pnt = locations.at&lt;Point&gt;(i); or Mat
     * binaryImage; // input, binary image vector&lt;Point&gt; locations; // output, locations of non-zero pixels
     * findNonZero(binaryImage, locations); // access pixel coordinates Point pnt = locations[i]; The function do not
     * work with multi-channel arrays. If you need to find non-zero elements across all the channels, use Mat.reshape
     * first to reinterpret the array as single-channel. Or you may extract the particular channel using either
     * extractImageCOI, or mixChannels, or split. Note: CV_16F/CV_16BF/CV_Bool/CV_64U/CV_64S/CV_32U are not supported
     * for src. If only count of non-zero array elements is important, REF: countNonZero is helpful. If only whether
     * there are non-zero elements is important, REF: hasNonZero is helpful.
     *
     * @param src single-channel array
     * @param idx the output array, type of Mat or vector&lt;Point&gt;, corresponding to non-zero indices in the input
     * @throws NullPointerException if src, idx is null
     */
    public static void findNonZero(Mat src, Mat idx) {
        findNonZero_0(src.nativeObj, idx.nativeObj);
    }

    /**
     * Calculates an average (mean) of array elements.
     *
     * @param src  input array that should have from 1 to 4 channels so that the result can be stored in Scalar_
     * @param mask optional operation mask ot type CV_8U, CV_8S or CV_Bool
     * @return the per-channel arithmetic means of the selected pixels
     * @throws NullPointerException if src, mask is null
     */
    public static Scalar mean(Mat src, Mat mask) {
        return new Scalar(mean_0(src.nativeObj, mask.nativeObj));
    }

    /**
     * Calculates an average (mean) of array elements.
     *
     * @param src input array that should have from 1 to 4 channels so that the result can be stored in Scalar_
     * @return the per-channel arithmetic means of the selected pixels
     * @throws NullPointerException if src is null
     */
    public static Scalar mean(Mat src) {
        return new Scalar(mean_1(src.nativeObj));
    }

    /**
     * Calculates a mean and standard deviation of array elements.
     *
     * @param src    input array that should have from 1 to 4 channels so that the results can be stored in Scalar_ 's
     * @param mean   output parameter: calculated mean value
     * @param stddev output parameter: calculated standard deviation
     * @param mask   optional operation mask of type CV_8U, CV_8S or CV_Bool
     * @throws NullPointerException if src, mask is null
     */
    public static void meanStdDev(Mat src, MatOfDouble mean, MatOfDouble stddev, Mat mask) {
        Mat mean_mat = mean;
        Mat stddev_mat = stddev;
        meanStdDev_0(src.nativeObj, mean_mat.nativeObj, stddev_mat.nativeObj, mask.nativeObj);
    }

    /**
     * Calculates a mean and standard deviation of array elements.
     *
     * @param src    input array that should have from 1 to 4 channels so that the results can be stored in Scalar_ 's
     * @param mean   output parameter: calculated mean value
     * @param stddev output parameter: calculated standard deviation
     * @throws NullPointerException if src is null
     */
    public static void meanStdDev(Mat src, MatOfDouble mean, MatOfDouble stddev) {
        Mat mean_mat = mean;
        Mat stddev_mat = stddev;
        meanStdDev_1(src.nativeObj, mean_mat.nativeObj, stddev_mat.nativeObj);
    }

    /**
     * Calculates an absolute difference norm or a relative difference norm. This version of norm calculates the
     * absolute difference norm or the relative difference norm of arrays src1 and src2. The type of norm to calculate
     * is specified using NormTypes.
     *
     * @param src1     first input array
     * @param normType type of the norm (see NormTypes)
     * @param mask     optional operation mask; it must have the same size as src1 and type CV_8UC1, CV_8S1 or CV_BoolC1
     * @return the selected norm of the input, or the norm of the difference between the inputs
     * @throws NullPointerException if src1, mask is null
     */
    public static double norm(Mat src1, int normType, Mat mask) {
        return norm_0(src1.nativeObj, normType, mask.nativeObj);
    }

    /**
     * Calculates an absolute difference norm or a relative difference norm. This version of norm calculates the
     * absolute difference norm or the relative difference norm of arrays src1 and src2. The type of norm to calculate
     * is specified using NormTypes.
     *
     * @param src1     first input array
     * @param normType type of the norm (see NormTypes)
     * @return the selected norm of the input, or the norm of the difference between the inputs
     * @throws NullPointerException if src1 is null
     */
    public static double norm(Mat src1, int normType) {
        return norm_1(src1.nativeObj, normType);
    }

    /**
     * Calculates an absolute difference norm or a relative difference norm. This version of norm calculates the
     * absolute difference norm or the relative difference norm of arrays src1 and src2. The type of norm to calculate
     * is specified using NormTypes.
     *
     * @param src1 first input array
     * @return the selected norm of the input, or the norm of the difference between the inputs
     * @throws NullPointerException if src1 is null
     */
    public static double norm(Mat src1) {
        return norm_2(src1.nativeObj);
    }

    /**
     * Calculates an absolute difference norm or a relative difference norm. This version of norm calculates the
     * absolute difference norm or the relative difference norm of arrays src1 and src2. The type of norm to calculate
     * is specified using NormTypes.
     *
     * @param src1     first input array
     * @param src2     second input array of the same size and the same type as src1
     * @param normType type of the norm (see NormTypes)
     * @param mask     optional operation mask; it must have the same size as src1 and type CV_8UC1, CV_8S1 or CV_BoolC1
     * @return the selected norm of the input, or the norm of the difference between the inputs
     * @throws NullPointerException if src1, src2, mask is null
     */
    public static double norm(Mat src1, Mat src2, int normType, Mat mask) {
        return norm_3(src1.nativeObj, src2.nativeObj, normType, mask.nativeObj);
    }

    /**
     * Calculates an absolute difference norm or a relative difference norm. This version of norm calculates the
     * absolute difference norm or the relative difference norm of arrays src1 and src2. The type of norm to calculate
     * is specified using NormTypes.
     *
     * @param src1     first input array
     * @param src2     second input array of the same size and the same type as src1
     * @param normType type of the norm (see NormTypes)
     * @return the selected norm of the input, or the norm of the difference between the inputs
     * @throws NullPointerException if src1, src2 is null
     */
    public static double norm(Mat src1, Mat src2, int normType) {
        return norm_4(src1.nativeObj, src2.nativeObj, normType);
    }

    /**
     * Calculates an absolute difference norm or a relative difference norm. This version of norm calculates the
     * absolute difference norm or the relative difference norm of arrays src1 and src2. The type of norm to calculate
     * is specified using NormTypes.
     *
     * @param src1 first input array
     * @param src2 second input array of the same size and the same type as src1
     * @return the selected norm of the input, or the norm of the difference between the inputs
     * @throws NullPointerException if src1, src2 is null
     */
    public static double norm(Mat src1, Mat src2) {
        return norm_5(src1.nativeObj, src2.nativeObj);
    }

    /**
     * Computes the Peak Signal-to-Noise Ratio (PSNR) image quality metric.
     *
     * @param src1 first input array
     * @param src2 second input array of the same size as src1
     * @param R    the maximum pixel value (255 by default)
     * @return the peak signal-to-noise ratio in decibels
     * @throws NullPointerException if src1, src2 is null
     */
    public static double PSNR(Mat src1, Mat src2, double R) {
        return PSNR_0(src1.nativeObj, src2.nativeObj, R);
    }

    /**
     * Computes the Peak Signal-to-Noise Ratio (PSNR) image quality metric.
     *
     * @param src1 first input array
     * @param src2 second input array of the same size as src1
     * @return the peak signal-to-noise ratio in decibels
     * @throws NullPointerException if src1, src2 is null
     */
    public static double PSNR(Mat src1, Mat src2) {
        return PSNR_1(src1.nativeObj, src2.nativeObj);
    }

    /**
     * Naive nearest neighbor finder see http://en.wikipedia.org/wiki/Nearest_neighbor_search.
     *
     * @param src1       first source matrix or channel value
     * @param src2       second source matrix or channel value
     * @param dist       output matrix of pairwise descriptor distances
     * @param dtype      desired output depth; a negative value keeps the source depth when supported
     * @param nidx       output matrix of nearest-neighbor indices
     * @param normType   distance or norm calculation mode
     * @param K          number of nearest neighbors retained for each query row
     * @param mask       8-bit matrix selecting permitted source-row pairs for distance calculation; an empty mask
     *                   permits all pairs
     * @param update     index offset used when updating an existing nearest-neighbor result
     * @param crosscheck whether to retain only mutually consistent nearest-neighbor matches
     * @throws NullPointerException if src1, src2, dist, nidx, mask is null
     */
    public static void batchDistance(
            Mat src1,
            Mat src2,
            Mat dist,
            int dtype,
            Mat nidx,
            int normType,
            int K,
            Mat mask,
            int update,
            boolean crosscheck) {
        batchDistance_0(
                src1.nativeObj,
                src2.nativeObj,
                dist.nativeObj,
                dtype,
                nidx.nativeObj,
                normType,
                K,
                mask.nativeObj,
                update,
                crosscheck);
    }

    /**
     * Naive nearest neighbor finder see http://en.wikipedia.org/wiki/Nearest_neighbor_search.
     *
     * @param src1     first source matrix or channel value
     * @param src2     second source matrix or channel value
     * @param dist     output matrix of pairwise descriptor distances
     * @param dtype    desired output depth; a negative value keeps the source depth when supported
     * @param nidx     output matrix of nearest-neighbor indices
     * @param normType distance or norm calculation mode
     * @param K        number of nearest neighbors retained for each query row
     * @param mask     8-bit matrix selecting permitted source-row pairs for distance calculation; an empty mask permits
     *                 all pairs
     * @param update   index offset used when updating an existing nearest-neighbor result
     * @throws NullPointerException if src1, src2, dist, nidx, mask is null
     */
    public static void batchDistance(
            Mat src1,
            Mat src2,
            Mat dist,
            int dtype,
            Mat nidx,
            int normType,
            int K,
            Mat mask,
            int update) {
        batchDistance_1(
                src1.nativeObj,
                src2.nativeObj,
                dist.nativeObj,
                dtype,
                nidx.nativeObj,
                normType,
                K,
                mask.nativeObj,
                update);
    }

    /**
     * Naive nearest neighbor finder see http://en.wikipedia.org/wiki/Nearest_neighbor_search.
     *
     * @param src1     first source matrix or channel value
     * @param src2     second source matrix or channel value
     * @param dist     output matrix of pairwise descriptor distances
     * @param dtype    desired output depth; a negative value keeps the source depth when supported
     * @param nidx     output matrix of nearest-neighbor indices
     * @param normType distance or norm calculation mode
     * @param K        number of nearest neighbors retained for each query row
     * @param mask     8-bit matrix selecting permitted source-row pairs for distance calculation; an empty mask permits
     *                 all pairs
     * @throws NullPointerException if src1, src2, dist, nidx, mask is null
     */
    public static void batchDistance(Mat src1, Mat src2, Mat dist, int dtype, Mat nidx, int normType, int K, Mat mask) {
        batchDistance_2(
                src1.nativeObj,
                src2.nativeObj,
                dist.nativeObj,
                dtype,
                nidx.nativeObj,
                normType,
                K,
                mask.nativeObj);
    }

    /**
     * Naive nearest neighbor finder see http://en.wikipedia.org/wiki/Nearest_neighbor_search.
     *
     * @param src1     first source matrix or channel value
     * @param src2     second source matrix or channel value
     * @param dist     output matrix of pairwise descriptor distances
     * @param dtype    desired output depth; a negative value keeps the source depth when supported
     * @param nidx     output matrix of nearest-neighbor indices
     * @param normType distance or norm calculation mode
     * @param K        number of nearest neighbors retained for each query row
     * @throws NullPointerException if src1, src2, dist, nidx is null
     */
    public static void batchDistance(Mat src1, Mat src2, Mat dist, int dtype, Mat nidx, int normType, int K) {
        batchDistance_3(src1.nativeObj, src2.nativeObj, dist.nativeObj, dtype, nidx.nativeObj, normType, K);
    }

    /**
     * Naive nearest neighbor finder see http://en.wikipedia.org/wiki/Nearest_neighbor_search.
     *
     * @param src1     first source matrix or channel value
     * @param src2     second source matrix or channel value
     * @param dist     output matrix of pairwise descriptor distances
     * @param dtype    desired output depth; a negative value keeps the source depth when supported
     * @param nidx     output matrix of nearest-neighbor indices
     * @param normType distance or norm calculation mode
     * @throws NullPointerException if src1, src2, dist, nidx is null
     */
    public static void batchDistance(Mat src1, Mat src2, Mat dist, int dtype, Mat nidx, int normType) {
        batchDistance_4(src1.nativeObj, src2.nativeObj, dist.nativeObj, dtype, nidx.nativeObj, normType);
    }

    /**
     * Naive nearest neighbor finder see http://en.wikipedia.org/wiki/Nearest_neighbor_search.
     *
     * @param src1  first source matrix or channel value
     * @param src2  second source matrix or channel value
     * @param dist  output matrix of pairwise descriptor distances
     * @param dtype desired output depth; a negative value keeps the source depth when supported
     * @param nidx  output matrix of nearest-neighbor indices
     * @throws NullPointerException if src1, src2, dist, nidx is null
     */
    public static void batchDistance(Mat src1, Mat src2, Mat dist, int dtype, Mat nidx) {
        batchDistance_5(src1.nativeObj, src2.nativeObj, dist.nativeObj, dtype, nidx.nativeObj);
    }

    /**
     * Normalizes the norm or value range of an array.
     *
     * @param src       input array
     * @param dst       output array of the same size as src
     * @param alpha     norm value to normalize to or the lower range boundary in case of the range normalization
     * @param beta      upper range boundary in case of the range normalization; it is not used for the norm
     *                  normalization
     * @param norm_type normalization type (see NormTypes)
     * @param dtype     when negative, the output array has the same type as src; otherwise, it has the same number of
     *                  channels as src and the depth =CV_MAT_DEPTH(dtype)
     * @param mask      optional operation mask of type CV_8U, CV_8S or CV_Bool
     * @throws NullPointerException if src, dst, mask is null
     */
    public static void normalize(Mat src, Mat dst, double alpha, double beta, int norm_type, int dtype, Mat mask) {
        normalize_0(src.nativeObj, dst.nativeObj, alpha, beta, norm_type, dtype, mask.nativeObj);
    }

    /**
     * Normalizes the norm or value range of an array.
     *
     * @param src       input array
     * @param dst       output array of the same size as src
     * @param alpha     norm value to normalize to or the lower range boundary in case of the range normalization
     * @param beta      upper range boundary in case of the range normalization; it is not used for the norm
     *                  normalization
     * @param norm_type normalization type (see NormTypes)
     * @param dtype     when negative, the output array has the same type as src; otherwise, it has the same number of
     *                  channels as src and the depth =CV_MAT_DEPTH(dtype)
     * @throws NullPointerException if src, dst is null
     */
    public static void normalize(Mat src, Mat dst, double alpha, double beta, int norm_type, int dtype) {
        normalize_1(src.nativeObj, dst.nativeObj, alpha, beta, norm_type, dtype);
    }

    /**
     * Normalizes the norm or value range of an array.
     *
     * @param src       input array
     * @param dst       output array of the same size as src
     * @param alpha     norm value to normalize to or the lower range boundary in case of the range normalization
     * @param beta      upper range boundary in case of the range normalization; it is not used for the norm
     *                  normalization
     * @param norm_type normalization type (see NormTypes)
     * @throws NullPointerException if src, dst is null
     */
    public static void normalize(Mat src, Mat dst, double alpha, double beta, int norm_type) {
        normalize_2(src.nativeObj, dst.nativeObj, alpha, beta, norm_type);
    }

    /**
     * Normalizes the norm or value range of an array.
     *
     * @param src   input array
     * @param dst   output array of the same size as src
     * @param alpha norm value to normalize to or the lower range boundary in case of the range normalization
     * @param beta  upper range boundary in case of the range normalization; it is not used for the norm normalization
     * @throws NullPointerException if src, dst is null
     */
    public static void normalize(Mat src, Mat dst, double alpha, double beta) {
        normalize_3(src.nativeObj, dst.nativeObj, alpha, beta);
    }

    /**
     * Normalizes the norm or value range of an array.
     *
     * @param src   input array
     * @param dst   output array of the same size as src
     * @param alpha norm value to normalize to or the lower range boundary in case of the range normalization
     * @throws NullPointerException if src, dst is null
     */
    public static void normalize(Mat src, Mat dst, double alpha) {
        normalize_4(src.nativeObj, dst.nativeObj, alpha);
    }

    /**
     * Normalizes the norm or value range of an array.
     *
     * @param src input array
     * @param dst output array of the same size as src
     * @throws NullPointerException if src, dst is null
     */
    public static void normalize(Mat src, Mat dst) {
        normalize_5(src.nativeObj, dst.nativeObj);
    }

    /**
     * Finds indices of min elements along provided axis Note: - If input or output array is not continuous, this
     * function will create an internal copy. - NaN handling is left unspecified, see patchNaNs(). - The returned index
     * is always in bounds of input matrix.
     *
     * @param src       input single-channel array
     * @param dst       output array of type CV_32SC1 with the same dimensionality as src, except for axis being reduced
     *                  - it should be set to 1
     * @param axis      axis to reduce along
     * @param lastIndex whether to get the index of first or last occurrence of min
     * @throws NullPointerException if src, dst is null
     */
    public static void reduceArgMin(Mat src, Mat dst, int axis, boolean lastIndex) {
        reduceArgMin_0(src.nativeObj, dst.nativeObj, axis, lastIndex);
    }

    /**
     * Finds indices of min elements along provided axis Note: - If input or output array is not continuous, this
     * function will create an internal copy. - NaN handling is left unspecified, see patchNaNs(). - The returned index
     * is always in bounds of input matrix.
     *
     * @param src  input single-channel array
     * @param dst  output array of type CV_32SC1 with the same dimensionality as src, except for axis being reduced - it
     *             should be set to 1
     * @param axis axis to reduce along
     * @throws NullPointerException if src, dst is null
     */
    public static void reduceArgMin(Mat src, Mat dst, int axis) {
        reduceArgMin_1(src.nativeObj, dst.nativeObj, axis);
    }

    /**
     * Finds indices of max elements along provided axis Note: - If input or output array is not continuous, this
     * function will create an internal copy. - NaN handling is left unspecified, see patchNaNs(). - The returned index
     * is always in bounds of input matrix.
     *
     * @param src       input single-channel array
     * @param dst       output array of type CV_32SC1 with the same dimensionality as src, except for axis being reduced
     *                  - it should be set to 1
     * @param axis      axis to reduce along
     * @param lastIndex whether to get the index of first or last occurrence of max
     * @throws NullPointerException if src, dst is null
     */
    public static void reduceArgMax(Mat src, Mat dst, int axis, boolean lastIndex) {
        reduceArgMax_0(src.nativeObj, dst.nativeObj, axis, lastIndex);
    }

    /**
     * Finds indices of max elements along provided axis Note: - If input or output array is not continuous, this
     * function will create an internal copy. - NaN handling is left unspecified, see patchNaNs(). - The returned index
     * is always in bounds of input matrix.
     *
     * @param src  input single-channel array
     * @param dst  output array of type CV_32SC1 with the same dimensionality as src, except for axis being reduced - it
     *             should be set to 1
     * @param axis axis to reduce along
     * @throws NullPointerException if src, dst is null
     */
    public static void reduceArgMax(Mat src, Mat dst, int axis) {
        reduceArgMax_1(src.nativeObj, dst.nativeObj, axis);
    }

    /**
     * Reduces a matrix to a vector. The function reduce reduces the matrix to a vector by treating the matrix
     * rows/columns as a set of 1D vectors and performing the specified operation on the vectors until a single
     * row/column is obtained. For example, the function can be used to compute horizontal and vertical projections of a
     * raster image. In case of REDUCE_MAX and REDUCE_MIN, the output image should have the same type as the source one.
     * In case of REDUCE_SUM, REDUCE_SUM2 and REDUCE_AVG, the output may have a larger element bit-depth to preserve
     * accuracy. And multi-channel arrays are also supported in these two reduction modes. example2.
     *
     * @param src   input matrix. It may also be n-dimensional, then dim is the axis to reduce and the output keeps it
     *              with size 1 (see the ReduceParams overload)
     * @param dst   output vector. Its size and type is defined by dim and dtype parameters
     * @param dim   dimension index along which the matrix is reduced. 0 means that the matrix is reduced to a single
     *              row. 1 means that the matrix is reduced to a single column
     * @param rtype reduction operation that could be one of ReduceTypes
     * @param dtype when negative, the output vector will have the same type as the input matrix, otherwise, its type
     *              will be CV_MAKE_TYPE(CV_MAT_DEPTH(dtype), src.channels())
     * @throws NullPointerException if src, dst is null
     */
    public static void reduce(Mat src, Mat dst, int dim, int rtype, int dtype) {
        reduce_0(src.nativeObj, dst.nativeObj, dim, rtype, dtype);
    }

    /**
     * Reduces a matrix to a vector. The function reduce reduces the matrix to a vector by treating the matrix
     * rows/columns as a set of 1D vectors and performing the specified operation on the vectors until a single
     * row/column is obtained. For example, the function can be used to compute horizontal and vertical projections of a
     * raster image. In case of REDUCE_MAX and REDUCE_MIN, the output image should have the same type as the source one.
     * In case of REDUCE_SUM, REDUCE_SUM2 and REDUCE_AVG, the output may have a larger element bit-depth to preserve
     * accuracy. And multi-channel arrays are also supported in these two reduction modes. example2.
     *
     * @param src   input matrix. It may also be n-dimensional, then dim is the axis to reduce and the output keeps it
     *              with size 1 (see the ReduceParams overload)
     * @param dst   output vector. Its size and type is defined by dim and dtype parameters
     * @param dim   dimension index along which the matrix is reduced. 0 means that the matrix is reduced to a single
     *              row. 1 means that the matrix is reduced to a single column
     * @param rtype reduction operation that could be one of ReduceTypes
     * @throws NullPointerException if src, dst is null
     */
    public static void reduce(Mat src, Mat dst, int dim, int rtype) {
        reduce_1(src.nativeObj, dst.nativeObj, dim, rtype);
    }

    /**
     * Reduces a matrix to a vector. The function reduce reduces the matrix to a vector by treating the matrix
     * rows/columns as a set of 1D vectors and performing the specified operation on the vectors until a single
     * row/column is obtained. For example, the function can be used to compute horizontal and vertical projections of a
     * raster image. In case of REDUCE_MAX and REDUCE_MIN, the output image should have the same type as the source one.
     * In case of REDUCE_SUM, REDUCE_SUM2 and REDUCE_AVG, the output may have a larger element bit-depth to preserve
     * accuracy. And multi-channel arrays are also supported in these two reduction modes. example2.
     *
     * @param src    input matrix. It may also be n-dimensional, then dim is the axis to reduce and the output keeps it
     *               with size 1 (see the ReduceParams overload)
     * @param dst    output vector. Its size and type is defined by dim and dtype parameters
     * @param params the reduction operation and the axes to reduce over, see ReduceParams
     * @throws NullPointerException if src, dst, params is null
     */
    public static void reduce(Mat src, Mat dst, ReduceParams params) {
        reduce_2(src.nativeObj, dst.nativeObj, params.getNativeObjAddr());
    }

    /**
     * Combines the channels of the source matrices into one multichannel destination matrix.
     *
     * @param mv  input vector of matrices to be merged; all the matrices in mv must have the same size and the same
     *            depth
     * @param dst output array of the same size and the same depth as mv[0]; The number of channels will be the total
     *            number of channels in the matrix array
     * @throws NullPointerException if dst is null
     */
    public static void merge(List<Mat> mv, Mat dst) {
        Mat mv_mat = Converters.vector_Mat_to_Mat(mv);
        merge_0(mv_mat.nativeObj, dst.nativeObj);
    }

    /**
     * Splits an n-dimensional array into several arrays along the given axis. This is the inverse of concat(). The
     * two-argument split() is a different operation: it separates the channels of the elements, while this one cuts the
     * array along one of its dimensions.
     *
     * @param m  input multi-channel array
     * @param mv output vector of arrays; the arrays themselves are reallocated, if needed
     * @throws NullPointerException if m is null
     */
    public static void split(Mat m, List<Mat> mv) {
        Mat mv_mat = new Mat();
        split_0(m.nativeObj, mv_mat.nativeObj);
        Converters.Mat_to_vector_Mat(mv_mat, mv);
        mv_mat.release();
    }

    /**
     * Copies selected source channels to selected destination channels according to the index mapping.
     *
     * @param src    input array or vector of matrices; all of the matrices must have the same size and the same depth
     * @param dst    output array or vector of matrices; all the matrices must be allocated; their size and depth must
     *               be the same as in src[0]
     * @param fromTo array of index pairs specifying which channels are copied and where; fromTo[k*2] is a 0-based index
     *               of the input channel in src, fromTo[k*2+1] is an index of the output channel in dst; the continuous
     *               channel numbering is used: the first input image channels are indexed from 0 to
     *               src[0].channels()-1, the second input image channels are indexed from src[0].channels() to
     *               src[0].channels() + src[1].channels()-1, and so on, the same scheme is used for the output image
     *               channels; as a special case, when fromTo[k*2] is negative, the corresponding output channel is
     *               filled with zero
     */
    public static void mixChannels(List<Mat> src, List<Mat> dst, MatOfInt fromTo) {
        Mat src_mat = Converters.vector_Mat_to_Mat(src);
        Mat dst_mat = Converters.vector_Mat_to_Mat(dst);
        Mat fromTo_mat = fromTo;
        mixChannels_0(src_mat.nativeObj, dst_mat.nativeObj, fromTo_mat.nativeObj);
    }

    /**
     * Extracts a single channel from src (coi is 0-based index).
     *
     * @param src input array
     * @param dst output array
     * @param coi index of channel to extract
     * @throws NullPointerException if src, dst is null
     */
    public static void extractChannel(Mat src, Mat dst, int coi) {
        extractChannel_0(src.nativeObj, dst.nativeObj, coi);
    }

    /**
     * Inserts a single channel to dst (coi is 0-based index).
     *
     * @param src input array
     * @param dst output array
     * @param coi index of channel for insertion
     * @throws NullPointerException if src, dst is null
     */
    public static void insertChannel(Mat src, Mat dst, int coi) {
        insertChannel_0(src.nativeObj, dst.nativeObj, coi);
    }

    /**
     * Flips a 2D array around vertical, horizontal, or both axes.
     *
     * @param src      input array
     * @param dst      output array of the same size and type as src
     * @param flipCode a flag to specify how to flip the array; 0 means flipping around the x-axis and positive value
     *                 (for example, 1) means flipping around y-axis. Negative value (for example, -1) means flipping
     *                 around both axes
     * @throws NullPointerException if src, dst is null
     */
    public static void flip(Mat src, Mat dst, int flipCode) {
        flip_0(src.nativeObj, dst.nativeObj, flipCode);
    }

    /**
     * Flips a n-dimensional at given axis The input may be multi-channel and non-continuous (e.g. a sub-array); the
     * channels of each element are kept together. In-place operation (src and dst sharing data) is supported.
     *
     * @param src  input array
     * @param dst  output array that has the same shape of src
     * @param axis axis that performs a flip on. -src.dims &lt;= axis &lt; src.dims
     * @throws NullPointerException if src, dst is null
     */
    public static void flipND(Mat src, Mat dst, int axis) {
        flipND_0(src.nativeObj, dst.nativeObj, axis);
    }

    /**
     * Removes axes of size 1 from an n-dimensional array. The elements are not changed. When called in place (dst is
     * src) on a continuous array, only the header changes; otherwise the elements are copied into dst (Mat.reshape
     * gives a view without copying). The channels of the elements are kept, the axes are the array dimensions. The same
     * holds for unsqueeze, flatten and reshape.
     *
     * @param src  input array
     * @param dst  output array
     * @param axes axes to remove, each of size 1; negative values count from the end. When empty, all the axes of size
     *             1 are removed
     * @throws NullPointerException if src, dst is null
     */
    public static void squeeze(Mat src, Mat dst, MatOfInt axes) {
        Mat axes_mat = axes;
        squeeze_0(src.nativeObj, dst.nativeObj, axes_mat.nativeObj);
    }

    /**
     * Removes axes of size 1 from an n-dimensional array. The elements are not changed. When called in place (dst is
     * src) on a continuous array, only the header changes; otherwise the elements are copied into dst (Mat.reshape
     * gives a view without copying). The channels of the elements are kept, the axes are the array dimensions. The same
     * holds for unsqueeze, flatten and reshape.
     *
     * @param src input array
     * @param dst output array
     * @throws NullPointerException if src, dst is null
     */
    public static void squeeze(Mat src, Mat dst) {
        squeeze_1(src.nativeObj, dst.nativeObj);
    }

    /**
     * Inserts axes of size 1 into an n-dimensional array.
     *
     * @param src  input array
     * @param dst  output array with src.dims + axes.size() dimensions
     * @param axes positions of the new axes in dst; negative values count from the end of dst
     * @throws NullPointerException if src, dst is null
     */
    public static void unsqueeze(Mat src, Mat dst, MatOfInt axes) {
        Mat axes_mat = axes;
        unsqueeze_0(src.nativeObj, dst.nativeObj, axes_mat.nativeObj);
    }

    /**
     * Merges consecutive axes of an n-dimensional array into one. With the default arguments the result is
     * 1-dimensional.
     *
     * @param src       input array
     * @param dst       output array
     * @param startAxis first axis to merge; negative values count from the end
     * @param endAxis   last axis to merge (inclusive); negative values count from the end
     * @throws NullPointerException if src, dst is null
     */
    public static void flatten(Mat src, Mat dst, int startAxis, int endAxis) {
        flatten_0(src.nativeObj, dst.nativeObj, startAxis, endAxis);
    }

    /**
     * Merges consecutive axes of an n-dimensional array into one. With the default arguments the result is
     * 1-dimensional.
     *
     * @param src       input array
     * @param dst       output array
     * @param startAxis first axis to merge; negative values count from the end
     * @throws NullPointerException if src, dst is null
     */
    public static void flatten(Mat src, Mat dst, int startAxis) {
        flatten_1(src.nativeObj, dst.nativeObj, startAxis);
    }

    /**
     * Merges consecutive axes of an n-dimensional array into one. With the default arguments the result is
     * 1-dimensional.
     *
     * @param src input array
     * @param dst output array
     * @throws NullPointerException if src, dst is null
     */
    public static void flatten(Mat src, Mat dst) {
        flatten_2(src.nativeObj, dst.nativeObj);
    }

    /**
     * Changes the shape of an n-dimensional array. One element of newShape may be -1, it is then inferred from the
     * number of elements. An element equal to 0 copies the corresponding axis of src, unless allowZero is set, in which
     * case it is a zero-size axis (the ONNX Reshape convention).
     *
     * @param src       input array
     * @param dst       output array
     * @param newShape  the new shape
     * @param allowZero treat 0 in newShape as a size instead of "same as src"
     * @throws NullPointerException if src, dst is null
     */
    public static void reshape(Mat src, Mat dst, MatOfInt newShape, boolean allowZero) {
        Mat newShape_mat = newShape;
        reshape_0(src.nativeObj, dst.nativeObj, newShape_mat.nativeObj, allowZero);
    }

    /**
     * Changes the shape of an n-dimensional array. One element of newShape may be -1, it is then inferred from the
     * number of elements. An element equal to 0 copies the corresponding axis of src, unless allowZero is set, in which
     * case it is a zero-size axis (the ONNX Reshape convention).
     *
     * @param src      input array
     * @param dst      output array
     * @param newShape the new shape
     * @throws NullPointerException if src, dst is null
     */
    public static void reshape(Mat src, Mat dst, MatOfInt newShape) {
        Mat newShape_mat = newShape;
        reshape_1(src.nativeObj, dst.nativeObj, newShape_mat.nativeObj);
    }

    /**
     * Concatenates n-dimensional arrays along the given axis. All the arrays must have the same type and number of
     * dimensions, and the same size along every axis except axis. The arrays may be multi-channel and non-continuous;
     * the channels of each element are kept together, so axis refers to the array dimensions, not to the channels. For
     * 2D arrays concat(src, 0, dst) is equivalent to vconcat() and concat(src, 1, dst) to hconcat().
     *
     * @param src  input arrays
     * @param axis axis to concatenate along; -dims &lt;= axis &lt; dims
     * @param dst  output array
     * @throws NullPointerException if dst is null
     */
    public static void concat(List<Mat> src, int axis, Mat dst) {
        Mat src_mat = Converters.vector_Mat_to_Mat(src);
        concat_0(src_mat.nativeObj, axis, dst.nativeObj);
    }

    /**
     * Splits an n-dimensional array into several arrays along the given axis. This is the inverse of concat(). The
     * two-argument split() is a different operation: it separates the channels of the elements, while this one cuts the
     * array along one of its dimensions.
     *
     * @param src   input array; may be multi-channel and non-continuous
     * @param axis  axis to split along; -src.dims &lt;= axis &lt; src.dims
     * @param sizes size of each output array along axis; the values must sum up to src.size[axis]
     * @param dst   output arrays, one per element of sizes
     * @throws NullPointerException if src is null
     */
    public static void split(Mat src, int axis, MatOfInt sizes, List<Mat> dst) {
        Mat sizes_mat = sizes;
        Mat dst_mat = new Mat();
        split_1(src.nativeObj, axis, sizes_mat.nativeObj, dst_mat.nativeObj);
        Converters.Mat_to_vector_Mat(dst_mat, dst);
        dst_mat.release();
    }

    /**
     * Copies a strided slice of an n-dimensional array. Along axis i the output takes the elements src[starts[i]],
     * src[starts[i] + steps[i]], ... up to, but not including, ends[i]. Axes past starts.size() are copied whole.
     * Unlike Python, the indices are absolute: negative values are not counted from the end. For a positive step, 0
     * &lt;= start, end &lt;= src.size[i]. For a negative step, -1 &lt;= start, end &lt; src.size[i], and end == -1
     * means "down to and including element 0". When all the steps are 1, Mat(src, ranges) gives the same result without
     * copying.
     *
     * @param src    input array; may be multi-channel and non-continuous
     * @param starts first index along each sliced axis
     * @param ends   end index (exclusive) along each sliced axis
     * @param steps  step along each sliced axis (non-zero, may be negative); empty means all ones
     * @param dst    output array of the same type as src
     * @throws NullPointerException if src, dst is null
     */
    public static void slice(Mat src, MatOfInt starts, MatOfInt ends, MatOfInt steps, Mat dst) {
        Mat starts_mat = starts;
        Mat ends_mat = ends;
        Mat steps_mat = steps;
        slice_0(src.nativeObj, starts_mat.nativeObj, ends_mat.nativeObj, steps_mat.nativeObj, dst.nativeObj);
    }

    /**
     * Broadcast the given Mat to the given shape.
     *
     * @param src   input array
     * @param shape target shape. Should be a list of CV_32S numbers. Note that negative values are not supported
     * @param dst   output array that has the given shape
     * @throws NullPointerException if src, shape, dst is null
     */
    public static void broadcast(Mat src, Mat shape, Mat dst) {
        broadcast_0(src.nativeObj, shape.nativeObj, dst.nativeObj);
    }

    /**
     * Evaluates a fused element-wise expression with broadcasting over the supplied input matrices. The expression
     * supports indexed input placeholders, arithmetic, comparisons, bitwise operators, casts, math functions, named
     * temporaries and tuples of results.
     *
     * @param expr    the expression string, e.g. "{0 * 2.5 + {1}"} or "({0 + {1}, {0} - {1})"}
     * @param inputs  the arrays bound to {0}, {1},
     * @param outputs receives one array per top-level result (one entry, or several for a tuple)
     */
    public static void texpr(String expr, List<Mat> inputs, List<Mat> outputs) {
        Mat inputs_mat = Converters.vector_Mat_to_Mat(inputs);
        Mat outputs_mat = new Mat();
        texpr_0(expr, inputs_mat.nativeObj, outputs_mat.nativeObj);
        Converters.Mat_to_vector_Mat(outputs_mat, outputs);
        outputs_mat.release();
    }

    /**
     * Rotates a 2D array in multiples of 90 degrees. The function rotate rotates the array in one of three different
     * ways: Rotate by 90 degrees clockwise (rotateCode = ROTATE_90_CLOCKWISE). Rotate by 180 degrees clockwise
     * (rotateCode = ROTATE_180). Rotate by 270 degrees clockwise (rotateCode = ROTATE_90_COUNTERCLOCKWISE).
     *
     * @param src        input array
     * @param dst        output array of the same type as src. The size is the same with ROTATE_180, and the rows and
     *                   cols are switched for ROTATE_90_CLOCKWISE and ROTATE_90_COUNTERCLOCKWISE
     * @param rotateCode an enum to specify how to rotate the array; see the enum RotateFlags
     * @throws NullPointerException if src, dst is null
     */
    public static void rotate(Mat src, Mat dst, int rotateCode) {
        rotate_0(src.nativeObj, dst.nativeObj, rotateCode);
    }

    /**
     * Fills the output array with repeated copies of the input array.
     *
     * @param src input array to replicate
     * @param ny  Flag to specify how many times the src is repeated along the vertical axis
     * @param nx  Flag to specify how many times the src is repeated along the horizontal axis
     * @param dst output array of the same type as src
     * @throws NullPointerException if src, dst is null
     */
    public static void repeat(Mat src, int ny, int nx, Mat dst) {
        repeat_0(src.nativeObj, ny, nx, dst.nativeObj);
    }

    /**
     * Fills the output array with repeated copies of the input array.
     *
     * @param src     input array to replicate
     * @param repeats number of copies along each axis; must have src.dims non-negative elements
     * @param dst     output array of the same type as src
     * @throws NullPointerException if src, dst is null
     */
    public static void repeat(Mat src, MatOfInt repeats, Mat dst) {
        Mat repeats_mat = repeats;
        repeat_1(src.nativeObj, repeats_mat.nativeObj, dst.nativeObj);
    }

    /**
     * List&lt;Mat&gt; matrices = { Mat(4, 1, CV_8UC1, Scalar(1)), Mat(4, 1, CV_8UC1, Scalar(2)), Mat(4, 1, CV_8UC1,
     * Scalar(3)),}; Mat out; hconcat( matrices, out ); //out: //[1, 2, 3; // 1, 2, 3; // 1, 2, 3; // 1, 2, 3].
     *
     * @param src input array or vector of matrices. all of the matrices must have the same number of rows and the same
     *            depth
     * @param dst output array. It has the same number of rows and depth as the src, and the sum of cols of the src.
     *            same depth
     * @throws NullPointerException if dst is null
     */
    public static void hconcat(List<Mat> src, Mat dst) {
        Mat src_mat = Converters.vector_Mat_to_Mat(src);
        hconcat_0(src_mat.nativeObj, dst.nativeObj);
    }

    /**
     * List&lt;Mat&gt; matrices = { Mat(1, 4, CV_8UC1, Scalar(1)), Mat(1, 4, CV_8UC1, Scalar(2)), Mat(1, 4, CV_8UC1,
     * Scalar(3)),}; Mat out; vconcat( matrices, out ); //out: //[1, 1, 1, 1; // 2, 2, 2, 2; // 3, 3, 3, 3].
     *
     * @param src input array or vector of matrices. all of the matrices must have the same number of cols and the same
     *            depth
     * @param dst output array. It has the same number of cols and depth as the src, and the sum of rows of the src.
     *            same depth
     * @throws NullPointerException if dst is null
     */
    public static void vconcat(List<Mat> src, Mat dst) {
        Mat src_mat = Converters.vector_Mat_to_Mat(src);
        vconcat_0(src_mat.nativeObj, dst.nativeObj);
    }

    /**
     * Computes the bitwise conjunction of corresponding array elements or of array elements with scalar channel values.
     *
     * @param src1 first input array or a scalar
     * @param src2 second input array or a scalar
     * @param dst  output array that has the same size and type as the input arrays
     * @param mask optional operation mask, CV_8U, CV_8S or CV_Bool single channel array, that specifies elements of the
     *             output array to be changed
     * @throws NullPointerException if src1, src2, dst, mask is null
     */
    public static void bitwise_and(Mat src1, Mat src2, Mat dst, Mat mask) {
        bitwise_and_0(src1.nativeObj, src2.nativeObj, dst.nativeObj, mask.nativeObj);
    }

    /**
     * Computes the bitwise conjunction of corresponding array elements or of array elements with scalar channel values.
     *
     * @param src1 first input array or a scalar
     * @param src2 second input array or a scalar
     * @param dst  output array that has the same size and type as the input arrays
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void bitwise_and(Mat src1, Mat src2, Mat dst) {
        bitwise_and_1(src1.nativeObj, src2.nativeObj, dst.nativeObj);
    }

    /**
     * Calculates the per-element bit-wise disjunction of two arrays or an array and a scalar.
     *
     * @param src1 first input array or a scalar
     * @param src2 second input array or a scalar
     * @param dst  output array that has the same size and type as the input arrays
     * @param mask optional operation mask, CV_8U, CV_8S or CV_Bool single channel array, that specifies elements of the
     *             output array to be changed
     * @throws NullPointerException if src1, src2, dst, mask is null
     */
    public static void bitwise_or(Mat src1, Mat src2, Mat dst, Mat mask) {
        bitwise_or_0(src1.nativeObj, src2.nativeObj, dst.nativeObj, mask.nativeObj);
    }

    /**
     * Calculates the per-element bit-wise disjunction of two arrays or an array and a scalar.
     *
     * @param src1 first input array or a scalar
     * @param src2 second input array or a scalar
     * @param dst  output array that has the same size and type as the input arrays
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void bitwise_or(Mat src1, Mat src2, Mat dst) {
        bitwise_or_1(src1.nativeObj, src2.nativeObj, dst.nativeObj);
    }

    /**
     * Calculates the per-element bit-wise "exclusive or" operation on two arrays or an array and a scalar.
     *
     * @param src1 first input array or a scalar
     * @param src2 second input array or a scalar
     * @param dst  output array that has the same size and type as the input arrays
     * @param mask optional operation mask, CV_8U, CV_8S or CV_Bool single channel array, that specifies elements of the
     *             output array to be changed
     * @throws NullPointerException if src1, src2, dst, mask is null
     */
    public static void bitwise_xor(Mat src1, Mat src2, Mat dst, Mat mask) {
        bitwise_xor_0(src1.nativeObj, src2.nativeObj, dst.nativeObj, mask.nativeObj);
    }

    /**
     * Calculates the per-element bit-wise "exclusive or" operation on two arrays or an array and a scalar.
     *
     * @param src1 first input array or a scalar
     * @param src2 second input array or a scalar
     * @param dst  output array that has the same size and type as the input arrays
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void bitwise_xor(Mat src1, Mat src2, Mat dst) {
        bitwise_xor_1(src1.nativeObj, src2.nativeObj, dst.nativeObj);
    }

    /**
     * Inverts every bit of an array.
     *
     * @param src  input array
     * @param dst  output array that has the same size and type as the input array
     * @param mask optional operation mask, CV_8U, CV_8S or CV_Bool single channel array, that specifies elements of the
     *             output array to be changed
     * @throws NullPointerException if src, dst, mask is null
     */
    public static void bitwise_not(Mat src, Mat dst, Mat mask) {
        bitwise_not_0(src.nativeObj, dst.nativeObj, mask.nativeObj);
    }

    /**
     * Inverts every bit of an array.
     *
     * @param src input array
     * @param dst output array that has the same size and type as the input array
     * @throws NullPointerException if src, dst is null
     */
    public static void bitwise_not(Mat src, Mat dst) {
        bitwise_not_1(src.nativeObj, dst.nativeObj);
    }

    /**
     * Calculates the per-element absolute difference between two arrays or between an array and a scalar.
     *
     * @param src1 first input array or a scalar
     * @param src2 second input array or a scalar
     * @param dst  output array that has the same size and type as input arrays
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void absdiff(Mat src1, Mat src2, Mat dst) {
        absdiff_0(src1.nativeObj, src2.nativeObj, dst.nativeObj);
    }

    /**
     * This is an overloaded member function, provided for convenience (python) Copies the matrix to another one. When
     * the operation mask is specified, if the Mat.create call shown above reallocates the matrix, the newly allocated
     * matrix is initialized with all zeros before copying the data.
     *
     * @param src  source matrix
     * @param dst  Destination matrix. If it does not have a proper size or type before the operation, it is reallocated
     * @param mask Operation mask of the same size as *this. Its non-zero elements indicate which matrix elements need
     *             to be copied. The mask has to be of type CV_8U, CV_8S or CV_Bool and can have 1 or multiple channels
     * @throws NullPointerException if src, dst, mask is null
     */
    public static void copyTo(Mat src, Mat dst, Mat mask) {
        copyTo_0(src.nativeObj, dst.nativeObj, mask.nativeObj);
    }

    /**
     * Checks if array elements lie between the elements of two other arrays.
     *
     * @param src    first input array
     * @param lowerb inclusive lower boundary array or a scalar
     * @param upperb inclusive upper boundary array or a scalar
     * @param dst    output array of the same size as src and CV_8U type
     * @throws NullPointerException if src, lowerb, upperb, dst is null
     */
    public static void inRange(Mat src, Scalar lowerb, Scalar upperb, Mat dst) {
        inRange_0(
                src.nativeObj,
                lowerb.val[0],
                lowerb.val[1],
                lowerb.val[2],
                lowerb.val[3],
                upperb.val[0],
                upperb.val[1],
                upperb.val[2],
                upperb.val[3],
                dst.nativeObj);
    }

    /**
     * Performs the per-element comparison of two arrays or an array and scalar value.
     *
     * @param src1  first input array or a scalar; when it is an array, it must have a single channel
     * @param src2  second input array or a scalar; when it is an array, it must have a single channel
     * @param dst   output array of type ref CV_8U that has the same size and the same number of channels as the input
     *              arrays
     * @param cmpop a flag, that specifies correspondence between the arrays (CmpTypes)
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void compare(Mat src1, Mat src2, Mat dst, int cmpop) {
        compare_0(src1.nativeObj, src2.nativeObj, dst.nativeObj, cmpop);
    }

    /**
     * Calculates per-element minimum of two arrays or an array and a scalar.
     *
     * @param src1 first input array
     * @param src2 second input array of the same size and type as src1
     * @param dst  output array of the same size and type as src1
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void min(Mat src1, Mat src2, Mat dst) {
        min_0(src1.nativeObj, src2.nativeObj, dst.nativeObj);
    }

    /**
     * Calculates per-element maximum of two arrays or an array and a scalar.
     *
     * @param src1 first input array
     * @param src2 second input array of the same size and type as src1
     * @param dst  output array of the same size and type as src1
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void max(Mat src1, Mat src2, Mat dst) {
        max_0(src1.nativeObj, src2.nativeObj, dst.nativeObj);
    }

    /**
     * Calculates a square root of array elements. The function sqrt calculates a square root of each input array
     * element. In case of multi-channel arrays, each channel is processed independently. The accuracy is approximately
     * the same as of the built-in sqrt .
     *
     * @param src input floating-point array
     * @param dst output array of the same size and type as src
     * @throws NullPointerException if src, dst is null
     */
    public static void sqrt(Mat src, Mat dst) {
        sqrt_0(src.nativeObj, dst.nativeObj);
    }

    /**
     * Raises every array element to a power.
     *
     * @param src   input array
     * @param power exponent of power
     * @param dst   output array of the same size and type as src
     * @throws NullPointerException if src, dst is null
     */
    public static void pow(Mat src, double power, Mat dst) {
        pow_0(src.nativeObj, power, dst.nativeObj);
    }

    /**
     * Calculates the exponent of every array element.
     *
     * @param src input array
     * @param dst output array of the same size and type as src
     * @throws NullPointerException if src, dst is null
     */
    public static void exp(Mat src, Mat dst) {
        exp_0(src.nativeObj, dst.nativeObj);
    }

    /**
     * Calculates the natural logarithm of every array element.
     *
     * @param src input array
     * @param dst output array of the same size and type as src
     * @throws NullPointerException if src, dst is null
     */
    public static void log(Mat src, Mat dst) {
        log_0(src.nativeObj, dst.nativeObj);
    }

    /**
     * Calculates x and y coordinates of 2D vectors from their magnitude and angle.
     *
     * @param magnitude      input floating-point array of magnitudes of 2D vectors; it can be an empty matrix (=Mat()),
     *                       in this case, the function assumes that all the magnitudes are =1; if it is not empty, it
     *                       must have the same size and type as angle
     * @param angle          input floating-point array of angles of 2D vectors
     * @param x              output array of x-coordinates of 2D vectors; it has the same size and type as angle
     * @param y              output array of y-coordinates of 2D vectors; it has the same size and type as angle
     * @param angleInDegrees when true, the input angles are measured in degrees, otherwise, they are measured in
     *                       radians
     * @throws NullPointerException if magnitude, angle, x, y is null
     */
    public static void polarToCart(Mat magnitude, Mat angle, Mat x, Mat y, boolean angleInDegrees) {
        polarToCart_0(magnitude.nativeObj, angle.nativeObj, x.nativeObj, y.nativeObj, angleInDegrees);
    }

    /**
     * Calculates x and y coordinates of 2D vectors from their magnitude and angle.
     *
     * @param magnitude input floating-point array of magnitudes of 2D vectors; it can be an empty matrix (=Mat()), in
     *                  this case, the function assumes that all the magnitudes are =1; if it is not empty, it must have
     *                  the same size and type as angle
     * @param angle     input floating-point array of angles of 2D vectors
     * @param x         output array of x-coordinates of 2D vectors; it has the same size and type as angle
     * @param y         output array of y-coordinates of 2D vectors; it has the same size and type as angle
     * @throws NullPointerException if magnitude, angle, x, y is null
     */
    public static void polarToCart(Mat magnitude, Mat angle, Mat x, Mat y) {
        polarToCart_1(magnitude.nativeObj, angle.nativeObj, x.nativeObj, y.nativeObj);
    }

    /**
     * Calculates the magnitude and angle of 2D vectors.
     *
     * @param x              array of x-coordinates; this must be a single-precision or double-precision floating-point
     *                       array
     * @param y              array of y-coordinates, that must have the same size and same type as x
     * @param magnitude      output array of magnitudes of the same size and type as x
     * @param angle          output array of angles that has the same size and type as x; the angles are measured in
     *                       radians (from 0 to 2*Pi) or in degrees (0 to 360 degrees)
     * @param angleInDegrees a flag, indicating whether the angles are measured in radians (which is by default), or in
     *                       degrees
     * @throws NullPointerException if x, y, magnitude, angle is null
     */
    public static void cartToPolar(Mat x, Mat y, Mat magnitude, Mat angle, boolean angleInDegrees) {
        cartToPolar_0(x.nativeObj, y.nativeObj, magnitude.nativeObj, angle.nativeObj, angleInDegrees);
    }

    /**
     * Calculates the magnitude and angle of 2D vectors.
     *
     * @param x         array of x-coordinates; this must be a single-precision or double-precision floating-point array
     * @param y         array of y-coordinates, that must have the same size and same type as x
     * @param magnitude output array of magnitudes of the same size and type as x
     * @param angle     output array of angles that has the same size and type as x; the angles are measured in radians
     *                  (from 0 to 2*Pi) or in degrees (0 to 360 degrees)
     * @throws NullPointerException if x, y, magnitude, angle is null
     */
    public static void cartToPolar(Mat x, Mat y, Mat magnitude, Mat angle) {
        cartToPolar_1(x.nativeObj, y.nativeObj, magnitude.nativeObj, angle.nativeObj);
    }

    /**
     * Calculates the rotation angle of 2D vectors.
     *
     * @param x              input floating-point array of x-coordinates of 2D vectors
     * @param y              input array of y-coordinates of 2D vectors; it must have the same size and the same type as
     *                       x
     * @param angle          output array of vector angles; it has the same size and same type as x
     * @param angleInDegrees when true, the function calculates the angle in degrees, otherwise, they are measured in
     *                       radians
     * @throws NullPointerException if x, y, angle is null
     */
    public static void phase(Mat x, Mat y, Mat angle, boolean angleInDegrees) {
        phase_0(x.nativeObj, y.nativeObj, angle.nativeObj, angleInDegrees);
    }

    /**
     * Calculates the rotation angle of 2D vectors.
     *
     * @param x     input floating-point array of x-coordinates of 2D vectors
     * @param y     input array of y-coordinates of 2D vectors; it must have the same size and the same type as x
     * @param angle output array of vector angles; it has the same size and same type as x
     * @throws NullPointerException if x, y, angle is null
     */
    public static void phase(Mat x, Mat y, Mat angle) {
        phase_1(x.nativeObj, y.nativeObj, angle.nativeObj);
    }

    /**
     * Calculates the magnitude of 2D vectors.
     *
     * @param x         floating-point array of x-coordinates of the vectors
     * @param y         floating-point array of y-coordinates of the vectors; it must have the same size as x
     * @param magnitude output array of the same size and type as x
     * @throws NullPointerException if x, y, magnitude is null
     */
    public static void magnitude(Mat x, Mat y, Mat magnitude) {
        magnitude_0(x.nativeObj, y.nativeObj, magnitude.nativeObj);
    }

    /**
     * Checks every element of an input array for invalid values. The function checkRange checks that every array
     * element is neither NaN nor infinite. When minVal &gt; DBL_MAX and maxVal &lt; DBL_MAX, the function also checks
     * that each value is between minVal and maxVal. In case of multi-channel arrays, each channel is processed
     * independently. If some values are out of range, position of the first outlier is stored in pos (when pos !=
     * NULL). Then, the function either returns false (when quiet=true) or throws an exception.
     *
     * @param a      input array
     * @param quiet  a flag, indicating whether the functions quietly return false when the array elements are out of
     *               range or they throw an exception. elements
     * @param minVal inclusive lower boundary of valid values range
     * @param maxVal exclusive upper boundary of valid values range
     * @return true if all checked values are finite and within the specified interval
     * @throws NullPointerException if a is null
     */
    public static boolean checkRange(Mat a, boolean quiet, double minVal, double maxVal) {
        return checkRange_0(a.nativeObj, quiet, minVal, maxVal);
    }

    /**
     * Checks every element of an input array for invalid values. The function checkRange checks that every array
     * element is neither NaN nor infinite. When minVal &gt; DBL_MAX and maxVal &lt; DBL_MAX, the function also checks
     * that each value is between minVal and maxVal. In case of multi-channel arrays, each channel is processed
     * independently. If some values are out of range, position of the first outlier is stored in pos (when pos !=
     * NULL). Then, the function either returns false (when quiet=true) or throws an exception.
     *
     * @param a      input array
     * @param quiet  a flag, indicating whether the functions quietly return false when the array elements are out of
     *               range or they throw an exception. elements
     * @param minVal inclusive lower boundary of valid values range
     * @return true if all checked values are finite and within the specified interval
     * @throws NullPointerException if a is null
     */
    public static boolean checkRange(Mat a, boolean quiet, double minVal) {
        return checkRange_1(a.nativeObj, quiet, minVal);
    }

    /**
     * Checks every element of an input array for invalid values. The function checkRange checks that every array
     * element is neither NaN nor infinite. When minVal &gt; DBL_MAX and maxVal &lt; DBL_MAX, the function also checks
     * that each value is between minVal and maxVal. In case of multi-channel arrays, each channel is processed
     * independently. If some values are out of range, position of the first outlier is stored in pos (when pos !=
     * NULL). Then, the function either returns false (when quiet=true) or throws an exception.
     *
     * @param a     input array
     * @param quiet a flag, indicating whether the functions quietly return false when the array elements are out of
     *              range or they throw an exception. elements
     * @return true if all checked values are finite and within the specified interval
     * @throws NullPointerException if a is null
     */
    public static boolean checkRange(Mat a, boolean quiet) {
        return checkRange_2(a.nativeObj, quiet);
    }

    /**
     * Checks every element of an input array for invalid values. The function checkRange checks that every array
     * element is neither NaN nor infinite. When minVal &gt; DBL_MAX and maxVal &lt; DBL_MAX, the function also checks
     * that each value is between minVal and maxVal. In case of multi-channel arrays, each channel is processed
     * independently. If some values are out of range, position of the first outlier is stored in pos (when pos !=
     * NULL). Then, the function either returns false (when quiet=true) or throws an exception.
     *
     * @param a input array
     * @return true if all checked values are finite and within the specified interval
     * @throws NullPointerException if a is null
     */
    public static boolean checkRange(Mat a) {
        return checkRange_4(a.nativeObj);
    }

    /**
     * Replaces NaNs (Not-a-Number values) in a matrix with the specified value. This function modifies the input matrix
     * in-place. The input matrix must be of type CV_16F, CV_16BF, CV_32F or CV_64F; other types are not supported.
     *
     * @param a   Input/output matrix (CV_16F, CV_16BF, CV_32F or CV_64F type)
     * @param val Value used to replace NaNs (defaults to 0)
     * @throws NullPointerException if a is null
     */
    public static void patchNaNs(Mat a, double val) {
        patchNaNs_0(a.nativeObj, val);
    }

    /**
     * Replaces NaNs (Not-a-Number values) in a matrix with the specified value. This function modifies the input matrix
     * in-place. The input matrix must be of type CV_16F, CV_16BF, CV_32F or CV_64F; other types are not supported.
     *
     * @param a Input/output matrix (CV_16F, CV_16BF, CV_32F or CV_64F type)
     * @throws NullPointerException if a is null
     */
    public static void patchNaNs(Mat a) {
        patchNaNs_1(a.nativeObj);
    }

    /**
     * Generates a mask of finite float values, i.e. not NaNs nor Infs. An element is set to 255 (all 1-bits) if all
     * channels are finite.
     *
     * @param src  Input matrix, should contain float or double elements of 1 to 4 channels
     * @param mask Output matrix of the same size as input of type CV_8UC1
     * @throws NullPointerException if src, mask is null
     */
    public static void finiteMask(Mat src, Mat mask) {
        finiteMask_0(src.nativeObj, mask.nativeObj);
    }

    /**
     * Performs generalized matrix multiplication.
     *
     * @param src1  first multiplied input matrix that could be real(CV_32FC1, CV_64FC1) or complex(CV_32FC2, CV_64FC2)
     * @param src2  second multiplied input matrix of the same type as src1
     * @param alpha weight of the matrix product
     * @param src3  third optional delta matrix added to the matrix product; it should have the same type as src1 and
     *              src2
     * @param beta  weight of src3
     * @param dst   output matrix; it has the proper size and the same type as input matrices
     * @param flags operation flags (GemmFlags)
     * @throws NullPointerException if src1, src2, src3, dst is null
     */
    public static void gemm(Mat src1, Mat src2, double alpha, Mat src3, double beta, Mat dst, int flags) {
        gemm_0(src1.nativeObj, src2.nativeObj, alpha, src3.nativeObj, beta, dst.nativeObj, flags);
    }

    /**
     * Performs generalized matrix multiplication.
     *
     * @param src1  first multiplied input matrix that could be real(CV_32FC1, CV_64FC1) or complex(CV_32FC2, CV_64FC2)
     * @param src2  second multiplied input matrix of the same type as src1
     * @param alpha weight of the matrix product
     * @param src3  third optional delta matrix added to the matrix product; it should have the same type as src1 and
     *              src2
     * @param beta  weight of src3
     * @param dst   output matrix; it has the proper size and the same type as input matrices
     * @throws NullPointerException if src1, src2, src3, dst is null
     */
    public static void gemm(Mat src1, Mat src2, double alpha, Mat src3, double beta, Mat dst) {
        gemm_1(src1.nativeObj, src2.nativeObj, alpha, src3.nativeObj, beta, dst.nativeObj);
    }

    /**
     * Calculates the product of a matrix and its transposition.
     *
     * @param src   input single-channel matrix. Note that unlike gemm, the function can multiply not only
     *              floating-point matrices
     * @param dst   output square matrix
     * @param aTa   Flag specifying the multiplication ordering. See the description below
     * @param delta Optional delta matrix subtracted from src before the multiplication. When the matrix is empty (
     *              delta=noArray() ), it is assumed to be zero, that is, nothing is subtracted. If it has the same size
     *              as src, it is simply subtracted. Otherwise, it is "repeated" (see repeat ) to cover the full src and
     *              then subtracted. Type of the delta matrix, when it is not empty, must be the same as the type of
     *              created output matrix. See the dtype parameter description below
     * @param scale Optional scale factor for the matrix product
     * @param dtype Optional type of the output matrix. When it is negative, the output matrix will have the same type
     *              as src . Otherwise, it will be type=CV_MAT_DEPTH(dtype) that should be either CV_32F or CV_64F
     * @throws NullPointerException if src, dst, delta is null
     */
    public static void mulTransposed(Mat src, Mat dst, boolean aTa, Mat delta, double scale, int dtype) {
        mulTransposed_0(src.nativeObj, dst.nativeObj, aTa, delta.nativeObj, scale, dtype);
    }

    /**
     * Calculates the product of a matrix and its transposition.
     *
     * @param src   input single-channel matrix. Note that unlike gemm, the function can multiply not only
     *              floating-point matrices
     * @param dst   output square matrix
     * @param aTa   Flag specifying the multiplication ordering. See the description below
     * @param delta Optional delta matrix subtracted from src before the multiplication. When the matrix is empty (
     *              delta=noArray() ), it is assumed to be zero, that is, nothing is subtracted. If it has the same size
     *              as src, it is simply subtracted. Otherwise, it is "repeated" (see repeat ) to cover the full src and
     *              then subtracted. Type of the delta matrix, when it is not empty, must be the same as the type of
     *              created output matrix. See the dtype parameter description below
     * @param scale Optional scale factor for the matrix product
     * @throws NullPointerException if src, dst, delta is null
     */
    public static void mulTransposed(Mat src, Mat dst, boolean aTa, Mat delta, double scale) {
        mulTransposed_1(src.nativeObj, dst.nativeObj, aTa, delta.nativeObj, scale);
    }

    /**
     * Calculates the product of a matrix and its transposition.
     *
     * @param src   input single-channel matrix. Note that unlike gemm, the function can multiply not only
     *              floating-point matrices
     * @param dst   output square matrix
     * @param aTa   Flag specifying the multiplication ordering. See the description below
     * @param delta Optional delta matrix subtracted from src before the multiplication. When the matrix is empty (
     *              delta=noArray() ), it is assumed to be zero, that is, nothing is subtracted. If it has the same size
     *              as src, it is simply subtracted. Otherwise, it is "repeated" (see repeat ) to cover the full src and
     *              then subtracted. Type of the delta matrix, when it is not empty, must be the same as the type of
     *              created output matrix. See the dtype parameter description below
     * @throws NullPointerException if src, dst, delta is null
     */
    public static void mulTransposed(Mat src, Mat dst, boolean aTa, Mat delta) {
        mulTransposed_2(src.nativeObj, dst.nativeObj, aTa, delta.nativeObj);
    }

    /**
     * Calculates the product of a matrix and its transposition.
     *
     * @param src input single-channel matrix. Note that unlike gemm, the function can multiply not only floating-point
     *            matrices
     * @param dst output square matrix
     * @param aTa Flag specifying the multiplication ordering. See the description below
     * @throws NullPointerException if src, dst is null
     */
    public static void mulTransposed(Mat src, Mat dst, boolean aTa) {
        mulTransposed_3(src.nativeObj, dst.nativeObj, aTa);
    }

    /**
     * Transpose for n-dimensional matrices. The input may be multi-channel and non-continuous (e.g. a sub-array); the
     * channels of each element are kept together, so order permutes the array dimensions only.
     *
     * @param src input array
     * @param dst output array of the same type as src
     * @throws NullPointerException if src, dst is null
     */
    public static void transpose(Mat src, Mat dst) {
        transpose_0(src.nativeObj, dst.nativeObj);
    }

    /**
     * Transpose for n-dimensional matrices. The input may be multi-channel and non-continuous (e.g. a sub-array); the
     * channels of each element are kept together, so order permutes the array dimensions only.
     *
     * @param src   input array
     * @param order a permutation of [0,1,..,N-1] where N is the number of axes of src. The i'th axis of dst will
     *              correspond to the axis numbered order[i] of the input
     * @param dst   output array of the same type as src
     * @throws NullPointerException if src, dst is null
     */
    public static void transpose(Mat src, MatOfInt order, Mat dst) {
        Mat order_mat = order;
        transpose_1(src.nativeObj, order_mat.nativeObj, dst.nativeObj);
    }

    /**
     * Permutes matrix axes according to the supplied dimension order.
     *
     * @param src   source matrix
     * @param order axis indices in the order required for the transposed tensor
     * @param dst   destination matrix that receives the result
     * @deprecated Use transpose(matrix, const vector&lt;int&gt;&amp;, matrix) instead.
     * @throws NullPointerException if src, dst is null
     */
    @Deprecated
    public static void transposeND(Mat src, MatOfInt order, Mat dst) {
        Mat order_mat = order;
        transposeND_0(src.nativeObj, order_mat.nativeObj, dst.nativeObj);
    }

    /**
     * Performs the matrix transformation of every array element.
     *
     * @param src input array that must have as many channels (1 to 4) as m.cols or m.cols-1
     * @param dst output array of the same size and depth as src; it has as many channels as m.rows
     * @param m   transformation 2x2 or 2x3 floating-point matrix
     * @throws NullPointerException if src, dst, m is null
     */
    public static void transform(Mat src, Mat dst, Mat m) {
        transform_0(src.nativeObj, dst.nativeObj, m.nativeObj);
    }

    /**
     * Performs the perspective matrix transformation of vectors.
     *
     * @param src input two-channel or three-channel floating-point array; each element is a 2D/3D vector to be
     *            transformed
     * @param dst output array of the same size and type as src
     * @param m   3x3 or 4x4 floating-point transformation matrix
     * @throws NullPointerException if src, dst, m is null
     */
    public static void perspectiveTransform(Mat src, Mat dst, Mat m) {
        perspectiveTransform_0(src.nativeObj, dst.nativeObj, m.nativeObj);
    }

    /**
     * Copies the selected triangular half of a square matrix into the opposite half to make it symmetric.
     *
     * @param m            input-output floating-point square matrix
     * @param lowerToUpper operation flag; if true, the lower half is copied to the upper half. Otherwise, the upper
     *                     half is copied to the lower half
     * @throws NullPointerException if m is null
     */
    public static void completeSymm(Mat m, boolean lowerToUpper) {
        completeSymm_0(m.nativeObj, lowerToUpper);
    }

    /**
     * Copies the selected triangular half of a square matrix into the opposite half to make it symmetric.
     *
     * @param m input-output floating-point square matrix
     * @throws NullPointerException if m is null
     */
    public static void completeSymm(Mat m) {
        completeSymm_1(m.nativeObj);
    }

    /**
     * Initializes a scaled identity matrix.
     *
     * @param mtx matrix to initialize (not necessarily square)
     * @param s   value to assign to diagonal elements
     * @throws NullPointerException if mtx, s is null
     */
    public static void setIdentity(Mat mtx, Scalar s) {
        setIdentity_0(mtx.nativeObj, s.val[0], s.val[1], s.val[2], s.val[3]);
    }

    /**
     * Initializes a scaled identity matrix.
     *
     * @param mtx matrix to initialize (not necessarily square)
     * @throws NullPointerException if mtx is null
     */
    public static void setIdentity(Mat mtx) {
        setIdentity_1(mtx.nativeObj);
    }

    /**
     * Returns the determinant of a square floating-point matrix. The function determinant calculates and returns the
     * determinant of the specified matrix. For small matrices ( mtx.cols=mtx.rows&lt;=3 ), the direct method is used.
     * For larger matrices, the function uses LU factorization with partial pivoting. For symmetric
     * positively-determined matrices, it is also possible to use eigen decomposition to calculate the determinant.
     *
     * @param mtx input matrix that must have CV_32FC1 or CV_64FC1 type and square size
     * @return the matrix determinant
     * @throws NullPointerException if mtx is null
     */
    public static double determinant(Mat mtx) {
        return determinant_0(mtx.nativeObj);
    }

    /**
     * Returns the trace of a matrix.
     *
     * @param mtx input matrix
     * @return the sum of the diagonal elements of each channel
     * @throws NullPointerException if mtx is null
     */
    public static Scalar trace(Mat mtx) {
        return new Scalar(trace_0(mtx.nativeObj));
    }

    /**
     * Finds the inverse or pseudo-inverse of a matrix.
     *
     * @param src   input floating-point M x N matrix
     * @param dst   output matrix of N x M size and the same type as src
     * @param flags inversion method (DecompTypes)
     * @return a method-dependent inversion status: success indicator or reciprocal condition estimate
     * @throws NullPointerException if src, dst is null
     */
    public static double invert(Mat src, Mat dst, int flags) {
        return invert_0(src.nativeObj, dst.nativeObj, flags);
    }

    /**
     * Finds the inverse or pseudo-inverse of a matrix.
     *
     * @param src input floating-point M x N matrix
     * @param dst output matrix of N x M size and the same type as src
     * @return a method-dependent inversion status: success indicator or reciprocal condition estimate
     * @throws NullPointerException if src, dst is null
     */
    public static double invert(Mat src, Mat dst) {
        return invert_1(src.nativeObj, dst.nativeObj);
    }

    /**
     * Solves one or more linear systems or least-squares problems.
     *
     * @param src1  input matrix on the left-hand side of the system
     * @param src2  input matrix on the right-hand side of the system
     * @param dst   output solution
     * @param flags solution (matrix inversion) method (DecompTypes)
     * @return true if the requested linear-system solution is found
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static boolean solve(Mat src1, Mat src2, Mat dst, int flags) {
        return solve_0(src1.nativeObj, src2.nativeObj, dst.nativeObj, flags);
    }

    /**
     * Solves one or more linear systems or least-squares problems.
     *
     * @param src1 input matrix on the left-hand side of the system
     * @param src2 input matrix on the right-hand side of the system
     * @param dst  output solution
     * @return true if the requested linear-system solution is found
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static boolean solve(Mat src1, Mat src2, Mat dst) {
        return solve_1(src1.nativeObj, src2.nativeObj, dst.nativeObj);
    }

    /**
     * Sorts each row or each column of a matrix. The function sort sorts each matrix row or each matrix column in
     * ascending or descending order. So you should pass two operation flags to get desired behaviour. If you want to
     * sort matrix rows or columns lexicographically, you can use STL sort generic function with the proper comparison
     * predicate.
     *
     * @param src   input single-channel array
     * @param dst   output array of the same size and type as src
     * @param flags operation flags, a combination of SortFlags
     * @throws NullPointerException if src, dst is null
     */
    public static void sort(Mat src, Mat dst, int flags) {
        sort_0(src.nativeObj, dst.nativeObj, flags);
    }

    /**
     * Sorts each row or each column of a matrix. The function sortIdx sorts each matrix row or each matrix column in
     * the ascending or descending order. So you should pass two operation flags to get desired behaviour. Instead of
     * reordering the elements themselves, it stores the indices of sorted elements in the output array. For example:
     * Mat A = Mat.eye(3,3,CV_32F), B; sortIdx(A, B, SORT_EVERY_ROW + SORT_ASCENDING); // B will probably contain //
     * (because of equal elements in A some permutations are possible): // [[1, 2, 0], [0, 2, 1], [0, 1, 2]].
     *
     * @param src   input single-channel array
     * @param dst   output integer array of the same size as src
     * @param flags operation flags that could be a combination of SortFlags
     * @throws NullPointerException if src, dst is null
     */
    public static void sortIdx(Mat src, Mat dst, int flags) {
        sortIdx_0(src.nativeObj, dst.nativeObj, flags);
    }

    /**
     * Finds the real roots of a cubic equation.
     *
     * @param coeffs equation coefficients, an array of 3 or 4 elements
     * @param roots  output array of real roots that has 0, 1, 2 or 3 elements
     * @return number of real roots. It can be -1 (all real numbers), 0, 1, 2 or 3
     * @throws NullPointerException if coeffs, roots is null
     */
    public static int solveCubic(Mat coeffs, Mat roots) {
        return solveCubic_0(coeffs.nativeObj, roots.nativeObj);
    }

    /**
     * Finds the real or complex roots of a polynomial equation.
     *
     * @param coeffs   array of polynomial coefficients
     * @param roots    output (complex) array of roots
     * @param maxIters maximum number of iterations the algorithm does
     * @return the maximum root-update error reached by the polynomial solver
     * @throws NullPointerException if coeffs, roots is null
     */
    public static double solvePoly(Mat coeffs, Mat roots, int maxIters) {
        return solvePoly_0(coeffs.nativeObj, roots.nativeObj, maxIters);
    }

    /**
     * Finds the real or complex roots of a polynomial equation.
     *
     * @param coeffs array of polynomial coefficients
     * @param roots  output (complex) array of roots
     * @return the maximum root-update error reached by the polynomial solver
     * @throws NullPointerException if coeffs, roots is null
     */
    public static double solvePoly(Mat coeffs, Mat roots) {
        return solvePoly_1(coeffs.nativeObj, roots.nativeObj);
    }

    /**
     * Calculates eigenvalues and eigenvectors of a symmetric matrix. The function eigen calculates just eigenvalues, or
     * eigenvalues and eigenvectors of the symmetric matrix src: src*eigenvectors.row(i).t() =
     * eigenvalues.at&lt;srcType&gt;(i)*eigenvectors.row(i).t() Note: Use eigenNonSymmetric for calculation of real
     * eigenvalues and eigenvectors of non-symmetric matrix.
     *
     * @param src          input matrix that must have CV_32FC1 or CV_64FC1 type, square size and be symmetrical (src
     *                     ^T^ == src)
     * @param eigenvalues  output vector of eigenvalues of the same type as src; the eigenvalues are stored in the
     *                     descending order
     * @param eigenvectors output matrix of eigenvectors; it has the same size and type as src; the eigenvectors are
     *                     stored as subsequent matrix rows, in the same order as the corresponding eigenvalues
     * @return true if the eigenvalue decomposition succeeds
     * @throws NullPointerException if src, eigenvalues, eigenvectors is null
     */
    public static boolean eigen(Mat src, Mat eigenvalues, Mat eigenvectors) {
        return eigen_0(src.nativeObj, eigenvalues.nativeObj, eigenvectors.nativeObj);
    }

    /**
     * Calculates eigenvalues and eigenvectors of a symmetric matrix. The function eigen calculates just eigenvalues, or
     * eigenvalues and eigenvectors of the symmetric matrix src: src*eigenvectors.row(i).t() =
     * eigenvalues.at&lt;srcType&gt;(i)*eigenvectors.row(i).t() Note: Use eigenNonSymmetric for calculation of real
     * eigenvalues and eigenvectors of non-symmetric matrix.
     *
     * @param src         input matrix that must have CV_32FC1 or CV_64FC1 type, square size and be symmetrical (src ^T^
     *                    == src)
     * @param eigenvalues output vector of eigenvalues of the same type as src; the eigenvalues are stored in the
     *                    descending order
     * @return true if the eigenvalue decomposition succeeds
     * @throws NullPointerException if src, eigenvalues is null
     */
    public static boolean eigen(Mat src, Mat eigenvalues) {
        return eigen_1(src.nativeObj, eigenvalues.nativeObj);
    }

    /**
     * Calculates eigenvalues and eigenvectors of a non-symmetric matrix (real eigenvalues only). Note: Assumes real
     * eigenvalues. The function calculates eigenvalues and eigenvectors (optional) of the square matrix src:
     * src*eigenvectors.row(i).t() = eigenvalues.at&lt;srcType&gt;(i)*eigenvectors.row(i).t().
     *
     * @param src          input matrix (CV_32FC1 or CV_64FC1 type)
     * @param eigenvalues  output vector of eigenvalues (type is the same type as src)
     * @param eigenvectors output matrix of eigenvectors (type is the same type as src). The eigenvectors are stored as
     *                     subsequent matrix rows, in the same order as the corresponding eigenvalues
     * @throws NullPointerException if src, eigenvalues, eigenvectors is null
     */
    public static void eigenNonSymmetric(Mat src, Mat eigenvalues, Mat eigenvectors) {
        eigenNonSymmetric_0(src.nativeObj, eigenvalues.nativeObj, eigenvectors.nativeObj);
    }

    /**
     * Note: use COVAR_ROWS or COVAR_COLS flag.
     *
     * @param samples samples stored as rows/columns of a single matrix
     * @param covar   output covariance matrix of the type ctype and square size
     * @param mean    input or output (depending on the flags) array as the average value of the input vectors
     * @param flags   operation flags as a combination of CovarFlags
     * @param ctype   type of the matrixl; it equals 'CV_64F' by default
     * @throws NullPointerException if samples, covar, mean is null
     */
    public static void calcCovarMatrix(Mat samples, Mat covar, Mat mean, int flags, int ctype) {
        calcCovarMatrix_0(samples.nativeObj, covar.nativeObj, mean.nativeObj, flags, ctype);
    }

    /**
     * Note: use COVAR_ROWS or COVAR_COLS flag.
     *
     * @param samples samples stored as rows/columns of a single matrix
     * @param covar   output covariance matrix of the type ctype and square size
     * @param mean    input or output (depending on the flags) array as the average value of the input vectors
     * @param flags   operation flags as a combination of CovarFlags
     * @throws NullPointerException if samples, covar, mean is null
     */
    public static void calcCovarMatrix(Mat samples, Mat covar, Mat mean, int flags) {
        calcCovarMatrix_1(samples.nativeObj, covar.nativeObj, mean.nativeObj, flags);
    }

    /**
     * Wrap PCA::operator().
     *
     * @param data          array or buffer containing matrix channel values
     * @param mean          mean vector associated with the principal-component basis
     * @param eigenvectors  principal-component basis vectors stored as matrix rows
     * @param maxComponents maximum number of principal components to retain; zero retains all available components
     * @throws NullPointerException if data, mean, eigenvectors is null
     */
    public static void PCACompute(Mat data, Mat mean, Mat eigenvectors, int maxComponents) {
        PCACompute_0(data.nativeObj, mean.nativeObj, eigenvectors.nativeObj, maxComponents);
    }

    /**
     * Wrap PCA::operator().
     *
     * @param data         array or buffer containing matrix channel values
     * @param mean         mean vector associated with the principal-component basis
     * @param eigenvectors principal-component basis vectors stored as matrix rows
     * @throws NullPointerException if data, mean, eigenvectors is null
     */
    public static void PCACompute(Mat data, Mat mean, Mat eigenvectors) {
        PCACompute_1(data.nativeObj, mean.nativeObj, eigenvectors.nativeObj);
    }

    /**
     * Wrap PCA::operator() and add eigenvalues output parameter.
     *
     * @param data          array or buffer containing matrix channel values
     * @param mean          mean vector associated with the principal-component basis
     * @param eigenvectors  principal-component basis vectors stored as matrix rows
     * @param eigenvalues   eigenvalues corresponding to the retained principal components
     * @param maxComponents maximum number of principal components to retain; zero retains all available components
     * @throws NullPointerException if data, mean, eigenvectors, eigenvalues is null
     */
    public static void PCACompute2(Mat data, Mat mean, Mat eigenvectors, Mat eigenvalues, int maxComponents) {
        PCACompute2_0(data.nativeObj, mean.nativeObj, eigenvectors.nativeObj, eigenvalues.nativeObj, maxComponents);
    }

    /**
     * Wrap PCA::operator() and add eigenvalues output parameter.
     *
     * @param data         array or buffer containing matrix channel values
     * @param mean         mean vector associated with the principal-component basis
     * @param eigenvectors principal-component basis vectors stored as matrix rows
     * @param eigenvalues  eigenvalues corresponding to the retained principal components
     * @throws NullPointerException if data, mean, eigenvectors, eigenvalues is null
     */
    public static void PCACompute2(Mat data, Mat mean, Mat eigenvectors, Mat eigenvalues) {
        PCACompute2_1(data.nativeObj, mean.nativeObj, eigenvectors.nativeObj, eigenvalues.nativeObj);
    }

    /**
     * Wrap PCA::operator().
     *
     * @param data             array or buffer containing matrix channel values
     * @param mean             mean vector associated with the principal-component basis
     * @param eigenvectors     principal-component basis vectors stored as matrix rows
     * @param retainedVariance fraction of total variance that the retained principal components should explain
     * @throws NullPointerException if data, mean, eigenvectors is null
     */
    public static void PCACompute(Mat data, Mat mean, Mat eigenvectors, double retainedVariance) {
        PCACompute_2(data.nativeObj, mean.nativeObj, eigenvectors.nativeObj, retainedVariance);
    }

    /**
     * Wrap PCA::operator() and add eigenvalues output parameter.
     *
     * @param data             array or buffer containing matrix channel values
     * @param mean             mean vector associated with the principal-component basis
     * @param eigenvectors     principal-component basis vectors stored as matrix rows
     * @param eigenvalues      eigenvalues corresponding to the retained principal components
     * @param retainedVariance fraction of total variance that the retained principal components should explain
     * @throws NullPointerException if data, mean, eigenvectors, eigenvalues is null
     */
    public static void PCACompute2(Mat data, Mat mean, Mat eigenvectors, Mat eigenvalues, double retainedVariance) {
        PCACompute2_2(data.nativeObj, mean.nativeObj, eigenvectors.nativeObj, eigenvalues.nativeObj, retainedVariance);
    }

    /**
     * Wrap PCA::project.
     *
     * @param data         array or buffer containing matrix channel values
     * @param mean         mean vector associated with the principal-component basis
     * @param eigenvectors principal-component basis vectors stored as matrix rows
     * @param result       destination for projected coordinates or reconstructed samples
     * @throws NullPointerException if data, mean, eigenvectors, result is null
     */
    public static void PCAProject(Mat data, Mat mean, Mat eigenvectors, Mat result) {
        PCAProject_0(data.nativeObj, mean.nativeObj, eigenvectors.nativeObj, result.nativeObj);
    }

    /**
     * Wrap PCA::backProject.
     *
     * @param data         array or buffer containing matrix channel values
     * @param mean         mean vector associated with the principal-component basis
     * @param eigenvectors principal-component basis vectors stored as matrix rows
     * @param result       destination for projected coordinates or reconstructed samples
     * @throws NullPointerException if data, mean, eigenvectors, result is null
     */
    public static void PCABackProject(Mat data, Mat mean, Mat eigenvectors, Mat result) {
        PCABackProject_0(data.nativeObj, mean.nativeObj, eigenvectors.nativeObj, result.nativeObj);
    }

    /**
     * Wrap SVD::compute.
     *
     * @param src   source matrix
     * @param w     singular values of the matrix decomposition
     * @param u     left singular vectors of the matrix decomposition
     * @param vt    transposed right singular vectors of the matrix decomposition
     * @param flags combination of SVD_MODIFY_A, SVD_NO_UV and SVD_FULL_UV flags
     * @throws NullPointerException if src, w, u, vt is null
     */
    public static void SVDecomp(Mat src, Mat w, Mat u, Mat vt, int flags) {
        SVDecomp_0(src.nativeObj, w.nativeObj, u.nativeObj, vt.nativeObj, flags);
    }

    /**
     * Wrap SVD::compute.
     *
     * @param src source matrix
     * @param w   singular values of the matrix decomposition
     * @param u   left singular vectors of the matrix decomposition
     * @param vt  transposed right singular vectors of the matrix decomposition
     * @throws NullPointerException if src, w, u, vt is null
     */
    public static void SVDecomp(Mat src, Mat w, Mat u, Mat vt) {
        SVDecomp_1(src.nativeObj, w.nativeObj, u.nativeObj, vt.nativeObj);
    }

    /**
     * Wrap SVD::backSubst.
     *
     * @param w   singular values of the matrix decomposition
     * @param u   left singular vectors of the matrix decomposition
     * @param vt  transposed right singular vectors of the matrix decomposition
     * @param rhs right-hand-side matrix of the linear system
     * @param dst destination matrix that receives the result
     * @throws NullPointerException if w, u, vt, rhs, dst is null
     */
    public static void SVBackSubst(Mat w, Mat u, Mat vt, Mat rhs, Mat dst) {
        SVBackSubst_0(w.nativeObj, u.nativeObj, vt.nativeObj, rhs.nativeObj, dst.nativeObj);
    }

    /**
     * Calculates the Mahalanobis distance between two vectors.
     *
     * @param v1     first 1D input vector
     * @param v2     second 1D input vector
     * @param icovar inverse covariance matrix
     * @return the Mahalanobis distance between the two vectors
     * @throws NullPointerException if v1, v2, icovar is null
     */
    public static double Mahalanobis(Mat v1, Mat v2, Mat icovar) {
        return Mahalanobis_0(v1.nativeObj, v2.nativeObj, icovar.nativeObj);
    }

    /**
     * Performs a forward or inverse Discrete Fourier transform of a 1D or 2D floating-point array.
     *
     * @param src         input array that could be real or complex
     * @param dst         output array whose size and type depends on the flags
     * @param flags       transformation flags, representing a combination of the DftFlags
     * @param nonzeroRows when the parameter is not zero, the function assumes that only the first nonzeroRows rows of
     *                    the input array (DFT_INVERSE is not set) or only the first nonzeroRows of the output array
     *                    (DFT_INVERSE is set) contain non-zeros, thus, the function can handle the rest of the rows
     *                    more efficiently and save some time; this technique is very useful for calculating array
     *                    cross-correlation or convolution using DFT
     * @throws NullPointerException if src, dst is null
     */
    public static void dft(Mat src, Mat dst, int flags, int nonzeroRows) {
        dft_0(src.nativeObj, dst.nativeObj, flags, nonzeroRows);
    }

    /**
     * Performs a forward or inverse Discrete Fourier transform of a 1D or 2D floating-point array.
     *
     * @param src   input array that could be real or complex
     * @param dst   output array whose size and type depends on the flags
     * @param flags transformation flags, representing a combination of the DftFlags
     * @throws NullPointerException if src, dst is null
     */
    public static void dft(Mat src, Mat dst, int flags) {
        dft_1(src.nativeObj, dst.nativeObj, flags);
    }

    /**
     * Performs a forward or inverse Discrete Fourier transform of a 1D or 2D floating-point array.
     *
     * @param src input array that could be real or complex
     * @param dst output array whose size and type depends on the flags
     * @throws NullPointerException if src, dst is null
     */
    public static void dft(Mat src, Mat dst) {
        dft_2(src.nativeObj, dst.nativeObj);
    }

    /**
     * Calculates the inverse Discrete Fourier Transform of a 1D or 2D array. idft(src, dst, flags) is equivalent to
     * dft(src, dst, flags | DFT_INVERSE) . Note: None of dft and idft scales the result by default. So, you should pass
     * DFT_SCALE to one of dft or idft explicitly to make these transforms mutually inverse.
     *
     * @param src         input floating-point real or complex array
     * @param dst         output array whose size and type depend on the flags
     * @param flags       operation flags (see dft and DftFlags)
     * @param nonzeroRows number of dst rows to process; the rest of the rows have undefined content (see the
     *                    convolution sample in dft description
     * @throws NullPointerException if src, dst is null
     */
    public static void idft(Mat src, Mat dst, int flags, int nonzeroRows) {
        idft_0(src.nativeObj, dst.nativeObj, flags, nonzeroRows);
    }

    /**
     * Calculates the inverse Discrete Fourier Transform of a 1D or 2D array. idft(src, dst, flags) is equivalent to
     * dft(src, dst, flags | DFT_INVERSE) . Note: None of dft and idft scales the result by default. So, you should pass
     * DFT_SCALE to one of dft or idft explicitly to make these transforms mutually inverse.
     *
     * @param src   input floating-point real or complex array
     * @param dst   output array whose size and type depend on the flags
     * @param flags operation flags (see dft and DftFlags)
     * @throws NullPointerException if src, dst is null
     */
    public static void idft(Mat src, Mat dst, int flags) {
        idft_1(src.nativeObj, dst.nativeObj, flags);
    }

    /**
     * Calculates the inverse Discrete Fourier Transform of a 1D or 2D array. idft(src, dst, flags) is equivalent to
     * dft(src, dst, flags | DFT_INVERSE) . Note: None of dft and idft scales the result by default. So, you should pass
     * DFT_SCALE to one of dft or idft explicitly to make these transforms mutually inverse.
     *
     * @param src input floating-point real or complex array
     * @param dst output array whose size and type depend on the flags
     * @throws NullPointerException if src, dst is null
     */
    public static void idft(Mat src, Mat dst) {
        idft_2(src.nativeObj, dst.nativeObj);
    }

    /**
     * Performs a forward or inverse discrete Cosine transform of 1D or 2D array.
     *
     * @param src   input floating-point array
     * @param dst   output array of the same size and type as src
     * @param flags transformation flags as a combination of DftFlags (DCT_*)
     * @throws NullPointerException if src, dst is null
     */
    public static void dct(Mat src, Mat dst, int flags) {
        dct_0(src.nativeObj, dst.nativeObj, flags);
    }

    /**
     * Performs a forward or inverse discrete Cosine transform of 1D or 2D array.
     *
     * @param src input floating-point array
     * @param dst output array of the same size and type as src
     * @throws NullPointerException if src, dst is null
     */
    public static void dct(Mat src, Mat dst) {
        dct_1(src.nativeObj, dst.nativeObj);
    }

    /**
     * Calculates the inverse Discrete Cosine Transform of a 1D or 2D array. idct(src, dst, flags) is equivalent to
     * dct(src, dst, flags | DCT_INVERSE).
     *
     * @param src   input floating-point single-channel array
     * @param dst   output array of the same size and type as src
     * @param flags operation flags
     * @throws NullPointerException if src, dst is null
     */
    public static void idct(Mat src, Mat dst, int flags) {
        idct_0(src.nativeObj, dst.nativeObj, flags);
    }

    /**
     * Calculates the inverse Discrete Cosine Transform of a 1D or 2D array. idct(src, dst, flags) is equivalent to
     * dct(src, dst, flags | DCT_INVERSE).
     *
     * @param src input floating-point single-channel array
     * @param dst output array of the same size and type as src
     * @throws NullPointerException if src, dst is null
     */
    public static void idct(Mat src, Mat dst) {
        idct_1(src.nativeObj, dst.nativeObj);
    }

    /**
     * Performs the per-element multiplication of two Fourier spectrums. The function mulSpectrums performs the
     * per-element multiplication of the two CCS-packed or complex matrices that are results of a real or complex
     * Fourier transform. The function, together with dft and idft, may be used to calculate convolution (pass
     * conjB=false ) or correlation (pass conjB=true ) of two arrays rapidly. When the arrays are complex, they are
     * simply multiplied (per element) with an optional conjugation of the second-array elements. When the arrays are
     * real, they are assumed to be CCS-packed (see dft for details).
     *
     * @param a     first input array
     * @param b     second input array of the same size and type as src1
     * @param c     output array of the same size and type as src1
     * @param flags operation flags; currently, the only supported flag is DFT_ROWS, which indicates that each row of
     *              src1 and src2 is an independent 1D Fourier spectrum. If you do not want to use this flag, then
     *              simply add a 0 as value
     * @param conjB optional flag that conjugates the second input array before the multiplication (true) or not (false)
     * @throws NullPointerException if a, b, c is null
     */
    public static void mulSpectrums(Mat a, Mat b, Mat c, int flags, boolean conjB) {
        mulSpectrums_0(a.nativeObj, b.nativeObj, c.nativeObj, flags, conjB);
    }

    /**
     * Performs the per-element multiplication of two Fourier spectrums. The function mulSpectrums performs the
     * per-element multiplication of the two CCS-packed or complex matrices that are results of a real or complex
     * Fourier transform. The function, together with dft and idft, may be used to calculate convolution (pass
     * conjB=false ) or correlation (pass conjB=true ) of two arrays rapidly. When the arrays are complex, they are
     * simply multiplied (per element) with an optional conjugation of the second-array elements. When the arrays are
     * real, they are assumed to be CCS-packed (see dft for details).
     *
     * @param a     first input array
     * @param b     second input array of the same size and type as src1
     * @param c     output array of the same size and type as src1
     * @param flags operation flags; currently, the only supported flag is DFT_ROWS, which indicates that each row of
     *              src1 and src2 is an independent 1D Fourier spectrum. If you do not want to use this flag, then
     *              simply add a 0 as value
     * @throws NullPointerException if a, b, c is null
     */
    public static void mulSpectrums(Mat a, Mat b, Mat c, int flags) {
        mulSpectrums_1(a.nativeObj, b.nativeObj, c.nativeObj, flags);
    }

    /**
     * Performs the per-element division of the first Fourier spectrum by the second Fourier spectrum. The function
     * divSpectrums performs the per-element division of the first array by the second array. The arrays are CCS-packed
     * or complex matrices that are results of a real or complex Fourier transform.
     *
     * @param a     first input array
     * @param b     second input array of the same size and type as src1
     * @param c     output array of the same size and type as src1
     * @param flags operation flags; currently, the only supported flag is DFT_ROWS, which indicates that each row of
     *              src1 and src2 is an independent 1D Fourier spectrum. If you do not want to use this flag, then
     *              simply add a 0 as value
     * @param conjB optional flag that conjugates the second input array before the multiplication (true) or not (false)
     * @throws NullPointerException if a, b, c is null
     */
    public static void divSpectrums(Mat a, Mat b, Mat c, int flags, boolean conjB) {
        divSpectrums_0(a.nativeObj, b.nativeObj, c.nativeObj, flags, conjB);
    }

    /**
     * Performs the per-element division of the first Fourier spectrum by the second Fourier spectrum. The function
     * divSpectrums performs the per-element division of the first array by the second array. The arrays are CCS-packed
     * or complex matrices that are results of a real or complex Fourier transform.
     *
     * @param a     first input array
     * @param b     second input array of the same size and type as src1
     * @param c     output array of the same size and type as src1
     * @param flags operation flags; currently, the only supported flag is DFT_ROWS, which indicates that each row of
     *              src1 and src2 is an independent 1D Fourier spectrum. If you do not want to use this flag, then
     *              simply add a 0 as value
     * @throws NullPointerException if a, b, c is null
     */
    public static void divSpectrums(Mat a, Mat b, Mat c, int flags) {
        divSpectrums_1(a.nativeObj, b.nativeObj, c.nativeObj, flags);
    }

    /**
     * Returns the optimal DFT size for a given vector size.
     *
     * @param vecsize vector size
     * @return the smallest supported efficient transform length at least as large as vecsize, or -1 if no length can be
     *         represented
     */
    public static int getOptimalDFTSize(int vecsize) {
        return getOptimalDFTSize_0(vecsize);
    }

    /**
     * Sets state of default random number generator. The function setRNGSeed sets state of default random number
     * generator to custom value.
     *
     * @param seed new state for default random number generator
     */
    public static void setRNGSeed(int seed) {
        setRNGSeed_0(seed);
    }

    /**
     * Fills the destination matrix with uniformly distributed random values within the supplied lower and upper bounds.
     *
     * @param dst  output array of random numbers; the array must be pre-allocated
     * @param low  inclusive lower boundary of the generated random numbers
     * @param high exclusive upper boundary of the generated random numbers
     * @throws NullPointerException if dst is null
     */
    public static void randu(Mat dst, double low, double high) {
        randu_0(dst.nativeObj, low, high);
    }

    /**
     * Fills the array with normally distributed random numbers. The function randn fills the matrix dst with normally
     * distributed random numbers with the specified mean vector and the standard deviation matrix. The generated random
     * numbers are clipped to fit the value range of the output array data type.
     *
     * @param dst    output array of random numbers; the array must be pre-allocated and have 1 to 4 channels
     * @param mean   mean value (expectation) of the generated random numbers
     * @param stddev standard deviation of the generated random numbers; it can be either a vector (in which case a
     *               diagonal standard deviation matrix is assumed) or a square matrix
     * @throws NullPointerException if dst is null
     */
    public static void randn(Mat dst, double mean, double stddev) {
        randn_0(dst.nativeObj, mean, stddev);
    }

    /**
     * Shuffles the array elements randomly.
     *
     * @param dst        input/output numerical 1D array
     * @param iterFactor scale factor that determines the number of random swap operations (see the details below).
     *                   instead
     * @throws NullPointerException if dst is null
     */
    public static void randShuffle(Mat dst, double iterFactor) {
        randShuffle_0(dst.nativeObj, iterFactor);
    }

    /**
     * Shuffles the array elements randomly.
     *
     * @param dst input/output numerical 1D array
     * @throws NullPointerException if dst is null
     */
    public static void randShuffle(Mat dst) {
        randShuffle_2(dst.nativeObj);
    }

    /**
     * Finds centers of clusters and groups input samples around the clusters.
     *
     * @param data       Data for clustering. An array of N-Dimensional points with float coordinates is needed.
     *                   Examples of this array can be: Mat points(count, 2, CV_32F); Mat points(count, 1, CV_32FC2);
     *                   Mat points(1, count, CV_32FC2); vector&lt;Point2f&gt; points(sampleCount);
     * @param K          Number of clusters to split the set by
     * @param bestLabels Input/output integer array that stores the cluster indices for every sample
     * @param criteria   The algorithm termination criteria, that is, the maximum number of iterations and/or the
     *                   desired accuracy. The accuracy is specified as criteria.epsilon. As soon as each of the cluster
     *                   centers moves by less than criteria.epsilon on some iteration, the algorithm stops
     * @param attempts   Flag to specify the number of times the algorithm is executed using different initial
     *                   labellings. The algorithm returns the labels that yield the best compactness (see the last
     *                   function parameter)
     * @param flags      Flag that can take values of KmeansFlags
     * @param centers    Output matrix of the cluster centers, one row per each cluster center
     * @return the sum of squared distances from each sample to its assigned cluster center
     * @throws NullPointerException if data, bestLabels, criteria, centers is null
     */
    public static double kmeans(
            Mat data,
            int K,
            Mat bestLabels,
            TermCriteria criteria,
            int attempts,
            int flags,
            Mat centers) {
        return kmeans_0(
                data.nativeObj,
                K,
                bestLabels.nativeObj,
                criteria.type,
                criteria.maxCount,
                criteria.epsilon,
                attempts,
                flags,
                centers.nativeObj);
    }

    /**
     * Finds centers of clusters and groups input samples around the clusters.
     *
     * @param data       Data for clustering. An array of N-Dimensional points with float coordinates is needed.
     *                   Examples of this array can be: Mat points(count, 2, CV_32F); Mat points(count, 1, CV_32FC2);
     *                   Mat points(1, count, CV_32FC2); vector&lt;Point2f&gt; points(sampleCount);
     * @param K          Number of clusters to split the set by
     * @param bestLabels Input/output integer array that stores the cluster indices for every sample
     * @param criteria   The algorithm termination criteria, that is, the maximum number of iterations and/or the
     *                   desired accuracy. The accuracy is specified as criteria.epsilon. As soon as each of the cluster
     *                   centers moves by less than criteria.epsilon on some iteration, the algorithm stops
     * @param attempts   Flag to specify the number of times the algorithm is executed using different initial
     *                   labellings. The algorithm returns the labels that yield the best compactness (see the last
     *                   function parameter)
     * @param flags      Flag that can take values of KmeansFlags
     * @return the sum of squared distances from each sample to its assigned cluster center
     * @throws NullPointerException if data, bestLabels, criteria is null
     */
    public static double kmeans(Mat data, int K, Mat bestLabels, TermCriteria criteria, int attempts, int flags) {
        return kmeans_1(
                data.nativeObj,
                K,
                bestLabels.nativeObj,
                criteria.type,
                criteria.maxCount,
                criteria.epsilon,
                attempts,
                flags);
    }

    /**
     * Solve given (non-integer) linear programming problem using the Simplex Algorithm (Simplex Method).
     *
     * @param Func       coefficient vector of the linear objective
     * @param Constr     m-by-n+1 matrix, whose rightmost column corresponds to b in formulation above and the remaining
     *                   to A. It should contain 32- or 64-bit floating point numbers
     * @param z          The solution will be returned here as a column-vector - it corresponds to c in the formulation
     *                   above. It will contain 64-bit floating point numbers
     * @param constr_eps allowed numeric disparity for constraints
     * @return the linear-program solver status identified by the SOLVELP constants
     * @throws NullPointerException if Func, Constr, z is null
     */
    public static int solveLP(Mat Func, Mat Constr, Mat z, double constr_eps) {
        return solveLP_0(Func.nativeObj, Constr.nativeObj, z.nativeObj, constr_eps);
    }

    /**
     * Solve given (non-integer) linear programming problem using the Simplex Algorithm (Simplex Method).
     *
     * @param Func   coefficient vector of the linear objective
     * @param Constr m-by-n+1 matrix, whose rightmost column corresponds to b in formulation above and the remaining to
     *               A. It should contain 32- or 64-bit floating point numbers
     * @param z      The solution will be returned here as a column-vector - it corresponds to c in the formulation
     *               above. It will contain 64-bit floating point numbers
     * @return the linear-program solver status identified by the SOLVELP constants
     * @throws NullPointerException if Func, Constr, z is null
     */
    public static int solveLP(Mat Func, Mat Constr, Mat z) {
        return solveLP_1(Func.nativeObj, Constr.nativeObj, z.nativeObj);
    }

    /**
     * Solves the rectangular linear assignment problem.
     *
     * @param cost          M-by-N cost matrix, CV_32FC1, CV_64FC1 or CV_32SC1. Must not be empty. Infinite and NaN
     *                      cells are forbidden pairs; an integer matrix has none, so only costThreshold can forbid a
     *                      pair there
     * @param assignment    Output vector of size M. Element i is the column matched to row i, or -1
     * @param costThreshold Maximum cost of a pair, and the price of each pair left unmade
     * @return Sum of cost over the matched pairs only, without the unassignment term; 0 if no pair was made. Note:
     *         Implemented from which extends to rectangular matrices
     * @throws NullPointerException if cost is null
     */
    public static double linearAssignment(Mat cost, MatOfInt assignment, double costThreshold) {
        Mat assignment_mat = assignment;
        return linearAssignment_0(cost.nativeObj, assignment_mat.nativeObj, costThreshold);
    }

    /**
     * Solves the rectangular linear assignment problem.
     *
     * @param cost       M-by-N cost matrix, CV_32FC1, CV_64FC1 or CV_32SC1. Must not be empty. Infinite and NaN cells
     *                   are forbidden pairs; an integer matrix has none, so only costThreshold can forbid a pair there
     * @param assignment Output vector of size M. Element i is the column matched to row i, or -1
     * @return Sum of cost over the matched pairs only, without the unassignment term; 0 if no pair was made. Note:
     *         Implemented from which extends to rectangular matrices
     * @throws NullPointerException if cost is null
     */
    public static double linearAssignment(Mat cost, MatOfInt assignment) {
        Mat assignment_mat = assignment;
        return linearAssignment_1(cost.nativeObj, assignment_mat.nativeObj);
    }

    /**
     * OpenCV will try to set the number of threads for subsequent parallel regions. If threads == 1, OpenCV will
     * disable threading optimizations and run all it's functions sequentially. Passing threads &lt; 0 will reset
     * threads number to system default. The function is not thread-safe. It must not be called in parallel region or
     * concurrent threads. OpenCV will try to run its functions with specified threads number, but some behaviour
     * differs from framework: TBB - User-defined parallel constructions will run with the same threads number, if
     * another is not specified. If later on user creates his own scheduler, OpenCV will use it. OpenMP - No special
     * defined behaviour. Concurrency - If threads == 1, OpenCV will disable threading optimizations and run its
     * functions sequentially. GCD - Supports only values &lt;= 0. C= - No special defined behaviour.
     *
     * @param nthreads Number of threads used by OpenCV
     */
    public static void setNumThreads(int nthreads) {
        setNumThreads_0(nthreads);
    }

    /**
     * Returns the number of threads used by OpenCV for parallel regions. Always returns 1 if OpenCV is built without
     * threading support. The exact meaning of return value depends on the threading framework used by OpenCV library:
     * TBB - The number of threads, that OpenCV will try to use for parallel regions. If there is any
     * tbb::thread_scheduler_init in user code conflicting with OpenCV, then function returns default number of threads
     * used by TBB library. OpenMP - An upper bound on the number of threads that could be used to form a new team.
     * Concurrency - The number of threads, that OpenCV will try to use for parallel regions. GCD - Unsupported; returns
     * the GCD thread pool limit (512) for compatibility. C= - The number of threads, that OpenCV will try to use for
     * parallel regions, if before called setNumThreads with threads &gt; 0, otherwise returns the number of logical
     * CPUs, available for the process.
     *
     * @return the thread-count setting reported by the active parallel backend
     */
    public static int getNumThreads() {
        return getNumThreads_0();
    }

    /**
     * Returns the index of the currently executed thread within the current parallel region. Always returns 0 if called
     * outside of parallel region.
     *
     * @return the calling worker's index reported by the active parallel backend
     * @deprecated Current implementation doesn't corresponding to this documentation.
     */
    @Deprecated
    public static int getThreadNum() {
        return getThreadNum_0();
    }

    /**
     * Returns full configuration time cmake output. Returned value is raw cmake output including version control system
     * revision, compiler version, compiler flags, enabled modules and third party libraries, etc. Output format depends
     * on target architecture.
     *
     * @return the build configuration of the linked OpenCV engine
     */
    public static String getBuildInformation() {
        return getBuildInformation_0();
    }

    /**
     * Returns library version string For example "3.4.1-dev".
     *
     * @return the version string reported by the linked native engine
     */
    public static String getVersionString() {
        return getVersionString_0();
    }

    /**
     * Returns major library version.
     *
     * @return the linked native engine's major version
     */
    public static int getVersionMajor() {
        return getVersionMajor_0();
    }

    /**
     * Returns minor library version.
     *
     * @return the linked native engine's minor version
     */
    public static int getVersionMinor() {
        return getVersionMinor_0();
    }

    /**
     * Returns revision field of the library version.
     *
     * @return the linked native engine's patch version
     */
    public static int getVersionRevision() {
        return getVersionRevision_0();
    }

    /**
     * Returns the number of ticks. The function returns the number of ticks after the certain event (for example, when
     * the machine was turned on). It can be used to initialize RNG or to measure a function execution time by reading
     * the tick count before and after the function call.
     *
     * @return the platform timer's current tick counter
     */
    public static long getTickCount() {
        return getTickCount_0();
    }

    /**
     * Returns the number of ticks per second. The function returns the number of ticks per second. That is, the
     * following code computes the execution time in seconds: double t = (double)getTickCount(); // do something ... t =
     * ((double)getTickCount() - t)/getTickFrequency();.
     *
     * @return the number of platform timer ticks per second
     */
    public static double getTickFrequency() {
        return getTickFrequency_0();
    }

    /**
     * Returns the number of CPU ticks. The function returns the current number of CPU ticks on some architectures (such
     * as x86, x64, PowerPC). On other platforms the function is equivalent to getTickCount. It can also be used for
     * very accurate time measurements, as well as for RNG initialization. Note that in case of multi-CPU systems a
     * thread, from which getCPUTickCount is called, can be suspended and resumed at another CPU with its own counter.
     * So, theoretically (and practically) the subsequent calls to the function do not necessary return the monotonously
     * increasing values. Also, since a modern CPU varies the CPU frequency depending on the load, the number of CPU
     * clocks spent in some code cannot be directly converted to time units. Therefore, getTickCount is generally a
     * preferable solution for measuring execution time.
     *
     * @return the architecture-dependent CPU tick counter
     */
    public static long getCPUTickCount() {
        return getCPUTickCount_0();
    }

    /**
     * Returns true if the specified feature is supported by the host hardware. The function returns true if the host
     * hardware supports the specified feature. When user calls setUseOptimized(false), the subsequent calls to
     * checkHardwareSupport() will return false until setUseOptimized(true) is called. This way user can dynamically
     * switch on and off the optimized code in OpenCV.
     *
     * @param feature The feature of interest, one of CpuFeatures
     * @return true if the requested CPU feature is enabled for the current runtime
     */
    public static boolean checkHardwareSupport(int feature) {
        return checkHardwareSupport_0(feature);
    }

    /**
     * Returns feature name by ID Returns empty string if feature is not defined.
     *
     * @param feature CPU feature identifier to query
     * @return the name associated with the feature identifier
     */
    public static String getHardwareFeatureName(int feature) {
        return getHardwareFeatureName_0(feature);
    }

    /**
     * Returns list of CPU features enabled during compilation. Returned value is a string containing space separated
     * list of CPU features with following markers: no markers - baseline features prefix * - features enabled in
     * dispatcher suffix ? - features enabled but not available in HW Example: SSE SSE2 SSE3 *SSE4.1 *SSE4.2 *FP16 *AVX
     * *AVX2 *AVX512-SKX?
     *
     * @return CPU feature names and their runtime availability indicators
     */
    public static String getCPUFeaturesLine() {
        return getCPUFeaturesLine_0();
    }

    /**
     * Returns the number of logical CPUs available for the process.
     *
     * @return the logical CPU count available to this runtime
     */
    public static int getNumberOfCPUs() {
        return getNumberOfCPUs_0();
    }

    /**
     * Returns the default algorithm accuracy/performance hint selected for this engine build.
     *
     * @return the default algorithm accuracy/performance hint selected for this engine build
     */
    public static int getDefaultAlgorithmHint() {
        return getDefaultAlgorithmHint_0();
    }

    /**
     * Enables or disables the optimized code. The function can be used to dynamically turn on and off optimized
     * dispatched code (code that uses SSE4.2, AVX/AVX2, and other instructions on the platforms that support it). It
     * sets a global flag that is further checked by OpenCV functions. Since the flag is not checked in the inner OpenCV
     * loops, it is only safe to call the function on the very top level in your application where you can be sure that
     * no other OpenCV function is currently executed. By default, the optimized code is enabled unless you disable it
     * in CMake. The current status can be retrieved using useOptimized.
     *
     * @param onoff The boolean flag specifying whether the optimized code should be used (onoff=true) or not
     *              (onoff=false)
     */
    public static void setUseOptimized(boolean onoff) {
        setUseOptimized_0(onoff);
    }

    /**
     * Returns the status of optimized code usage. The function returns true if the optimized code is enabled.
     * Otherwise, it returns false.
     *
     * @return true if optimized runtime implementations are enabled
     */
    public static boolean useOptimized() {
        return useOptimized_0();
    }

    /**
     * Try to find requested data file Search directories: 1. Directories passed via addSamplesDataSearchPath() 2.
     * OPENCV_SAMPLES_DATA_PATH_HINT environment variable 3. OPENCV_SAMPLES_DATA_PATH environment variable If parameter
     * value is not empty and nothing is found then stop searching. 4. Detects build/install path based on: a. current
     * working directory (CWD) b. and/or binary module location (opencv_core/opencv_world, doesn't work with static
     * linkage) 5. Scan } directories if build directory is detected or the current directory is in source tree. 6. Scan
     * directory if install directory is detected.
     *
     * @param relative_path Relative path to data file
     * @param required      Specify "file not found" handling. If true, function prints information message and raises
     *                      Exception. If false, function returns empty result
     * @param silentMode    Disables messages
     * @return the located path, or an empty string when no path is found and required is false
     */
    public static String findFile(String relative_path, boolean required, boolean silentMode) {
        return findFile_0(relative_path, required, silentMode);
    }

    /**
     * Try to find requested data file Search directories: 1. Directories passed via addSamplesDataSearchPath() 2.
     * OPENCV_SAMPLES_DATA_PATH_HINT environment variable 3. OPENCV_SAMPLES_DATA_PATH environment variable If parameter
     * value is not empty and nothing is found then stop searching. 4. Detects build/install path based on: a. current
     * working directory (CWD) b. and/or binary module location (opencv_core/opencv_world, doesn't work with static
     * linkage) 5. Scan } directories if build directory is detected or the current directory is in source tree. 6. Scan
     * directory if install directory is detected.
     *
     * @param relative_path Relative path to data file
     * @param required      Specify "file not found" handling. If true, function prints information message and raises
     *                      Exception. If false, function returns empty result
     * @return the located path, or an empty string when no path is found and required is false
     */
    public static String findFile(String relative_path, boolean required) {
        return findFile_1(relative_path, required);
    }

    /**
     * Try to find requested data file Search directories: 1. Directories passed via addSamplesDataSearchPath() 2.
     * OPENCV_SAMPLES_DATA_PATH_HINT environment variable 3. OPENCV_SAMPLES_DATA_PATH environment variable If parameter
     * value is not empty and nothing is found then stop searching. 4. Detects build/install path based on: a. current
     * working directory (CWD) b. and/or binary module location (opencv_core/opencv_world, doesn't work with static
     * linkage) 5. Scan } directories if build directory is detected or the current directory is in source tree. 6. Scan
     * directory if install directory is detected.
     *
     * @param relative_path Relative path to data file
     * @return the located path, or an empty string when no path is found and required is false
     */
    public static String findFile(String relative_path) {
        return findFile_2(relative_path);
    }

    /**
     * Searches the configured data locations for a relative path and keeps the original path when no match is found.
     *
     * @param relative_path relative path to locate
     * @param silentMode    whether file-search diagnostics are suppressed
     * @return the located file path, or the original relative path when no file is found
     */
    public static String findFileOrKeep(String relative_path, boolean silentMode) {
        return findFileOrKeep_0(relative_path, silentMode);
    }

    /**
     * Searches the configured data locations for a relative path and keeps the original path when no match is found.
     *
     * @param relative_path relative path to locate
     * @return the located file path, or the original relative path when no file is found
     */
    public static String findFileOrKeep(String relative_path) {
        return findFileOrKeep_1(relative_path);
    }

    /**
     * Override search data path by adding new search location Use this only to override default behavior Passed paths
     * are used in LIFO order.
     *
     * @param path Path to used samples data
     */
    public static void addSamplesDataSearchPath(String path) {
        addSamplesDataSearchPath_0(path);
    }

    /**
     * Append samples search data sub directory General usage is to add OpenCV modules name ( -&gt; + ). Passed
     * subdirectories are used in LIFO order.
     *
     * @param subdir samples data sub directory
     */
    public static void addSamplesDataSearchSubDirectory(String subdir) {
        addSamplesDataSearchSubDirectory_0(subdir);
    }

    /**
     * Sets the error verbosity used by this Core instance.
     *
     * @param verbose whether detailed error reporting is enabled
     */
    public static void setErrorVerbosity(boolean verbose) {
        setErrorVerbosity_0(verbose);
    }

    /**
     * Calculates the per-element sum of two arrays or an array and a scalar.
     *
     * @param src1  first input array or a scalar
     * @param src2  second input array or a scalar
     * @param dst   output array that has the same size and number of channels as the input array(s); the depth is
     *              defined by dtype or src1/src2
     * @param mask  optional operation mask - CV_8U, CV_8S or CV_Bool single channel array, that specifies elements of
     *              the output array to be changed
     * @param dtype optional depth of the output array (see the discussion below)
     * @throws NullPointerException if src1, src2, dst, mask is null
     */
    public static void add(Mat src1, Scalar src2, Mat dst, Mat mask, int dtype) {
        add_3(src1.nativeObj, src2.val[0], src2.val[1], src2.val[2], src2.val[3], dst.nativeObj, mask.nativeObj, dtype);
    }

    /**
     * Calculates the per-element sum of two arrays or an array and a scalar.
     *
     * @param src1 first input array or a scalar
     * @param src2 second input array or a scalar
     * @param dst  output array that has the same size and number of channels as the input array(s); the depth is
     *             defined by dtype or src1/src2
     * @param mask optional operation mask - CV_8U, CV_8S or CV_Bool single channel array, that specifies elements of
     *             the output array to be changed
     * @throws NullPointerException if src1, src2, dst, mask is null
     */
    public static void add(Mat src1, Scalar src2, Mat dst, Mat mask) {
        add_4(src1.nativeObj, src2.val[0], src2.val[1], src2.val[2], src2.val[3], dst.nativeObj, mask.nativeObj);
    }

    /**
     * Calculates the per-element sum of two arrays or an array and a scalar.
     *
     * @param src1 first input array or a scalar
     * @param src2 second input array or a scalar
     * @param dst  output array that has the same size and number of channels as the input array(s); the depth is
     *             defined by dtype or src1/src2
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void add(Mat src1, Scalar src2, Mat dst) {
        add_5(src1.nativeObj, src2.val[0], src2.val[1], src2.val[2], src2.val[3], dst.nativeObj);
    }

    /**
     * Calculates the per-element difference between two arrays or array and a scalar.
     *
     * @param src1  first input array or a scalar
     * @param src2  second input array or a scalar
     * @param dst   output array of the same size and the same number of channels as the input array
     * @param mask  optional operation mask; this is CV_8U, CV8S or CV_Bool single channel array that specifies elements
     *              of the output array to be changed
     * @param dtype optional depth of the output array
     * @throws NullPointerException if src1, src2, dst, mask is null
     */
    public static void subtract(Mat src1, Scalar src2, Mat dst, Mat mask, int dtype) {
        subtract_3(
                src1.nativeObj,
                src2.val[0],
                src2.val[1],
                src2.val[2],
                src2.val[3],
                dst.nativeObj,
                mask.nativeObj,
                dtype);
    }

    /**
     * Calculates the per-element difference between two arrays or array and a scalar.
     *
     * @param src1 first input array or a scalar
     * @param src2 second input array or a scalar
     * @param dst  output array of the same size and the same number of channels as the input array
     * @param mask optional operation mask; this is CV_8U, CV8S or CV_Bool single channel array that specifies elements
     *             of the output array to be changed
     * @throws NullPointerException if src1, src2, dst, mask is null
     */
    public static void subtract(Mat src1, Scalar src2, Mat dst, Mat mask) {
        subtract_4(src1.nativeObj, src2.val[0], src2.val[1], src2.val[2], src2.val[3], dst.nativeObj, mask.nativeObj);
    }

    /**
     * Calculates the per-element difference between two arrays or array and a scalar.
     *
     * @param src1 first input array or a scalar
     * @param src2 second input array or a scalar
     * @param dst  output array of the same size and the same number of channels as the input array
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void subtract(Mat src1, Scalar src2, Mat dst) {
        subtract_5(src1.nativeObj, src2.val[0], src2.val[1], src2.val[2], src2.val[3], dst.nativeObj);
    }

    /**
     * Calculates the per-element scaled product of two arrays.
     *
     * @param src1  first input array
     * @param src2  second input array of the same size and the same type as src1
     * @param dst   output array of the same size and type as src1
     * @param scale optional scale factor
     * @param dtype optional depth of the output array
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void multiply(Mat src1, Scalar src2, Mat dst, double scale, int dtype) {
        multiply_3(src1.nativeObj, src2.val[0], src2.val[1], src2.val[2], src2.val[3], dst.nativeObj, scale, dtype);
    }

    /**
     * Calculates the per-element scaled product of two arrays.
     *
     * @param src1  first input array
     * @param src2  second input array of the same size and the same type as src1
     * @param dst   output array of the same size and type as src1
     * @param scale optional scale factor
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void multiply(Mat src1, Scalar src2, Mat dst, double scale) {
        multiply_4(src1.nativeObj, src2.val[0], src2.val[1], src2.val[2], src2.val[3], dst.nativeObj, scale);
    }

    /**
     * Calculates the per-element scaled product of two arrays.
     *
     * @param src1 first input array
     * @param src2 second input array of the same size and the same type as src1
     * @param dst  output array of the same size and type as src1
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void multiply(Mat src1, Scalar src2, Mat dst) {
        multiply_5(src1.nativeObj, src2.val[0], src2.val[1], src2.val[2], src2.val[3], dst.nativeObj);
    }

    /**
     * Performs per-element division of two arrays or a scalar by an array.
     *
     * @param src1  first input array
     * @param src2  second input array of the same size and type as src1
     * @param dst   output array of the same size and type as src2
     * @param scale scalar factor
     * @param dtype optional depth of the output array; if -1, dst will have depth src2.depth(), but in case of an
     *              array-by-array division, you can only pass -1 when src1.depth()==src2.depth()
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void divide(Mat src1, Scalar src2, Mat dst, double scale, int dtype) {
        divide_5(src1.nativeObj, src2.val[0], src2.val[1], src2.val[2], src2.val[3], dst.nativeObj, scale, dtype);
    }

    /**
     * Performs per-element division of two arrays or a scalar by an array.
     *
     * @param src1  first input array
     * @param src2  second input array of the same size and type as src1
     * @param dst   output array of the same size and type as src2
     * @param scale scalar factor
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void divide(Mat src1, Scalar src2, Mat dst, double scale) {
        divide_6(src1.nativeObj, src2.val[0], src2.val[1], src2.val[2], src2.val[3], dst.nativeObj, scale);
    }

    /**
     * Performs per-element division of two arrays or a scalar by an array.
     *
     * @param src1 first input array
     * @param src2 second input array of the same size and type as src1
     * @param dst  output array of the same size and type as src2
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void divide(Mat src1, Scalar src2, Mat dst) {
        divide_7(src1.nativeObj, src2.val[0], src2.val[1], src2.val[2], src2.val[3], dst.nativeObj);
    }

    /**
     * Calculates the per-element absolute difference between two arrays or between an array and a scalar.
     *
     * @param src1 first input array or a scalar
     * @param src2 second input array or a scalar
     * @param dst  output array that has the same size and type as input arrays
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void absdiff(Mat src1, Scalar src2, Mat dst) {
        absdiff_1(src1.nativeObj, src2.val[0], src2.val[1], src2.val[2], src2.val[3], dst.nativeObj);
    }

    /**
     * Performs the per-element comparison of two arrays or an array and scalar value.
     *
     * @param src1  first input array or a scalar; when it is an array, it must have a single channel
     * @param src2  second input array or a scalar; when it is an array, it must have a single channel
     * @param dst   output array of type ref CV_8U that has the same size and the same number of channels as the input
     *              arrays
     * @param cmpop a flag, that specifies correspondence between the arrays (CmpTypes)
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void compare(Mat src1, Scalar src2, Mat dst, int cmpop) {
        compare_1(src1.nativeObj, src2.val[0], src2.val[1], src2.val[2], src2.val[3], dst.nativeObj, cmpop);
    }

    /**
     * Calculates per-element minimum of two arrays or an array and a scalar.
     *
     * @param src1 first input array
     * @param src2 second input array of the same size and type as src1
     * @param dst  output array of the same size and type as src1
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void min(Mat src1, Scalar src2, Mat dst) {
        min_1(src1.nativeObj, src2.val[0], src2.val[1], src2.val[2], src2.val[3], dst.nativeObj);
    }

    /**
     * Calculates per-element maximum of two arrays or an array and a scalar.
     *
     * @param src1 first input array
     * @param src2 second input array of the same size and type as src1
     * @param dst  output array of the same size and type as src1
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void max(Mat src1, Scalar src2, Mat dst) {
        max_1(src1.nativeObj, src2.val[0], src2.val[1], src2.val[2], src2.val[3], dst.nativeObj);
    }

    /**
     * Minimum and maximum values together with their locations in a matrix.
     */
    public static class MinMaxLocResult {

        /**
         * Minimum value found in the source matrix.
         */
        public double minVal;
        /**
         * Maximum value found in the source matrix.
         */
        public double maxVal;
        /**
         * Location of the minimum value in the source matrix.
         */
        public Point minLoc;
        /**
         * Location of the maximum value in the source matrix.
         */
        public Point maxLoc;

        /**
         * Creates an empty MinMaxLocResult value.
         */
        public MinMaxLocResult() {
            minVal = 0;
            maxVal = 0;
            minLoc = new Point();
            maxLoc = new Point();
        }
    }

    /**
     * Finds minimum and maximum matrix values and their locations, using the mask when supplied.
     *
     * @param src  source matrix
     * @param mask 8-bit selection mask whose nonzero entries select matrix elements; an empty mask selects all elements
     * @return minimum and maximum values with their matrix locations
     * @throws NullPointerException if src is null
     */
    public static MinMaxLocResult minMaxLoc(Mat src, Mat mask) {
        MinMaxLocResult res = new MinMaxLocResult();
        long maskNativeObj = 0;
        if (mask != null) {
            maskNativeObj = mask.nativeObj;
        }
        double resarr[] = n_minMaxLocManual(src.nativeObj, maskNativeObj);
        res.minVal = resarr[0];
        res.maxVal = resarr[1];
        res.minLoc.x = resarr[2];
        res.minLoc.y = resarr[3];
        res.maxLoc.x = resarr[4];
        res.maxLoc.y = resarr[5];
        return res;
    }

    /**
     * Finds minimum and maximum matrix values and their locations, using the mask when supplied.
     *
     * @param src source matrix
     * @return minimum and maximum values with their matrix locations
     */
    public static MinMaxLocResult minMaxLoc(Mat src) {
        return minMaxLoc(src, null);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#cubeRoot(float)}.
     *
     * @param val A function argument
     * @return the real cube root of the argument
     */
    private static native float cubeRoot_0(float val);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#fastAtan2(float,float)}.
     *
     * @param y y-coordinate of the vector
     * @param x x-coordinate of the vector
     * @return the vector angle in degrees in the range from 0 to 360
     */
    private static native float fastAtan2_0(float y, float x);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#useIPP()}.
     *
     * @return true if IPP acceleration is enabled in this engine build
     */
    private static native boolean useIPP_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#setUseIPP(boolean)}.
     *
     * @param flag whether the corresponding option is enabled
     */
    private static native void setUseIPP_0(boolean flag);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#getIppVersion()}.
     *
     * @return the IPP version string reported by this engine build
     */
    private static native String getIppVersion_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#useIPP_NotExact()}.
     *
     * @return true if IPP implementations that can differ numerically are enabled
     */
    private static native boolean useIPP_NotExact_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#setUseIPP_NotExact(boolean)}.
     *
     * @param flag whether the corresponding option is enabled
     */
    private static native void setUseIPP_NotExact_0(boolean flag);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#borderInterpolate(int,int,int)}.
     *
     * @param p          0-based coordinate of the extrapolated pixel along one of the axes, likely &lt;0 or &gt;= len
     * @param len        Length of the array along the corresponding axis
     * @param borderType Border type, one of the BorderTypes, except for BORDER_TRANSPARENT and BORDER_ISOLATED. When
     *                   borderType==BORDER_CONSTANT, the function always returns -1, regardless of p and len
     * @return the source coordinate selected by the border rule, or -1 for a constant border
     */
    private static native int borderInterpolate_0(int p, int len, int borderType);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.core.Core#copyMakeBorder(Mat,Mat,int,int,int,int,int,Scalar)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Source image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Destination image of the same type as src
     *                      and the size Size(src.cols+left+right, src.rows+top+bottom)
     * @param top           the top pixels
     * @param bottom        the bottom pixels
     * @param left          the left pixels
     * @param right         Parameter specifying how many pixels in each direction from the source image rectangle to
     *                      extrapolate. For example, top=1, bottom=1, left=1, right=1 mean that 1 pixel-wide border
     *                      needs to be built
     * @param borderType    Border type. See borderInterpolate for details
     * @param value_val0    {@code value.val[0]} component of Border value if borderType==BORDER_CONSTANT
     * @param value_val1    {@code value.val[1]} component of Border value if borderType==BORDER_CONSTANT
     * @param value_val2    {@code value.val[2]} component of Border value if borderType==BORDER_CONSTANT
     * @param value_val3    {@code value.val[3]} component of Border value if borderType==BORDER_CONSTANT
     */
    private static native void copyMakeBorder_0(
            long src_nativeObj,
            long dst_nativeObj,
            int top,
            int bottom,
            int left,
            int right,
            int borderType,
            double value_val0,
            double value_val1,
            double value_val2,
            double value_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#copyMakeBorder(Mat,Mat,int,int,int,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Source image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Destination image of the same type as src
     *                      and the size Size(src.cols+left+right, src.rows+top+bottom)
     * @param top           the top pixels
     * @param bottom        the bottom pixels
     * @param left          the left pixels
     * @param right         Parameter specifying how many pixels in each direction from the source image rectangle to
     *                      extrapolate. For example, top=1, bottom=1, left=1, right=1 mean that 1 pixel-wide border
     *                      needs to be built
     * @param borderType    Border type. See borderInterpolate for details
     */
    private static native void copyMakeBorder_1(
            long src_nativeObj,
            long dst_nativeObj,
            int top,
            int bottom,
            int left,
            int right,
            int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#add(Mat,Mat,Mat,Mat,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       number of channels as the input array(s); the depth is defined by dtype or src1/src2
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional operation mask - CV_8U, CV_8S or
     *                       CV_Bool single channel array, that specifies elements of the output array to be changed
     * @param dtype          optional depth of the output array (see the discussion below)
     */
    private static native void add_0(
            long src1_nativeObj,
            long src2_nativeObj,
            long dst_nativeObj,
            long mask_nativeObj,
            int dtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#add(Mat,Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       number of channels as the input array(s); the depth is defined by dtype or src1/src2
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional operation mask - CV_8U, CV_8S or
     *                       CV_Bool single channel array, that specifies elements of the output array to be changed
     */
    private static native void add_1(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj, long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#add(Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       number of channels as the input array(s); the depth is defined by dtype or src1/src2
     */
    private static native void add_2(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#subtract(Mat,Mat,Mat,Mat,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and the same
     *                       number of channels as the input array
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional operation mask; this is CV_8U,
     *                       CV8S or CV_Bool single channel array that specifies elements of the output array to be
     *                       changed
     * @param dtype          optional depth of the output array
     */
    private static native void subtract_0(
            long src1_nativeObj,
            long src2_nativeObj,
            long dst_nativeObj,
            long mask_nativeObj,
            int dtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#subtract(Mat,Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and the same
     *                       number of channels as the input array
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional operation mask; this is CV_8U,
     *                       CV8S or CV_Bool single channel array that specifies elements of the output array to be
     *                       changed
     */
    private static native void subtract_1(
            long src1_nativeObj,
            long src2_nativeObj,
            long dst_nativeObj,
            long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#subtract(Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and the same
     *                       number of channels as the input array
     */
    private static native void subtract_2(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#multiply(Mat,Mat,Mat,double,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size and
     *                       the same type as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src1
     * @param scale          optional scale factor
     * @param dtype          optional depth of the output array
     */
    private static native void multiply_0(
            long src1_nativeObj,
            long src2_nativeObj,
            long dst_nativeObj,
            double scale,
            int dtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#multiply(Mat,Mat,Mat,double)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size and
     *                       the same type as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src1
     * @param scale          optional scale factor
     */
    private static native void multiply_1(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj, double scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#multiply(Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size and
     *                       the same type as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src1
     */
    private static native void multiply_2(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#divide(Mat,Mat,Mat,double,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size and
     *                       type as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src2
     * @param scale          scalar factor
     * @param dtype          optional depth of the output array; if -1, dst will have depth src2.depth(), but in case of
     *                       an array-by-array division, you can only pass -1 when src1.depth()==src2.depth()
     */
    private static native void divide_0(
            long src1_nativeObj,
            long src2_nativeObj,
            long dst_nativeObj,
            double scale,
            int dtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#divide(Mat,Mat,Mat,double)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size and
     *                       type as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src2
     * @param scale          scalar factor
     */
    private static native void divide_1(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj, double scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#divide(Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size and
     *                       type as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src2
     */
    private static native void divide_2(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#divide(double,Mat,Mat,int)}.
     *
     * @param scale          scalar factor
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size and
     *                       type as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src2
     * @param dtype          optional depth of the output array; if -1, dst will have depth src2.depth(), but in case of
     *                       an array-by-array division, you can only pass -1 when src1.depth()==src2.depth()
     */
    private static native void divide_3(double scale, long src2_nativeObj, long dst_nativeObj, int dtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#divide(double,Mat,Mat)}.
     *
     * @param scale          scalar factor
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size and
     *                       type as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src2
     */
    private static native void divide_4(double scale, long src2_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#scaleAdd(Mat,double,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param alpha          scale factor for the first array
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size and
     *                       type as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src1
     */
    private static native void scaleAdd_0(long src1_nativeObj, double alpha, long src2_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.core.Core#addWeighted(Mat,double,Mat,double,double,Mat,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param alpha          weight of the first array elements
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size and
     *                       channel number as src1
     * @param beta           weight of the second array elements
     * @param gamma          scalar added to each sum
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       number of channels as the input arrays
     * @param dtype          optional depth of the output array; when both input arrays have the same depth, dtype can
     *                       be set to -1, which will be equivalent to src1.depth()
     */
    private static native void addWeighted_0(
            long src1_nativeObj,
            double alpha,
            long src2_nativeObj,
            double beta,
            double gamma,
            long dst_nativeObj,
            int dtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#addWeighted(Mat,double,Mat,double,double,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param alpha          weight of the first array elements
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size and
     *                       channel number as src1
     * @param beta           weight of the second array elements
     * @param gamma          scalar added to each sum
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       number of channels as the input arrays
     */
    private static native void addWeighted_1(
            long src1_nativeObj,
            double alpha,
            long src2_nativeObj,
            double beta,
            double gamma,
            long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#convertScaleAbs(Mat,Mat,double,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array
     * @param alpha         optional scale factor
     * @param beta          optional delta added to the scaled values
     */
    private static native void convertScaleAbs_0(long src_nativeObj, long dst_nativeObj, double alpha, double beta);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#convertScaleAbs(Mat,Mat,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array
     * @param alpha         optional scale factor
     */
    private static native void convertScaleAbs_1(long src_nativeObj, long dst_nativeObj, double alpha);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#convertScaleAbs(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array
     */
    private static native void convertScaleAbs_2(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#LUT(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array of 8-bit or 16-bit integer
     *                      elements
     * @param lut_nativeObj native matrix or object handle for {@code lut}; look-up table of 256 elements (if src has
     *                      depth CV_8U or CV_8S) or 65536 elements(if src has depth CV_16U or CV_16S); in case of
     *                      multi-channel input array, the table should either have a single channel (in this case the
     *                      same table is used for all channels) or the same number of channels as in the input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size and number of
     *                      channels as src, and the same depth as lut
     */
    private static native void LUT_0(long src_nativeObj, long lut_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#sumElems(Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array that must have from 1 to 4
     *                      channels
     * @return packed numeric fields used by the Java wrapper to construct {@code Scalar}
     */
    private static native double[] sumElems_0(long src_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#hasNonZero(Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; single-channel array
     * @return true if at least one source element is nonzero
     */
    private static native boolean hasNonZero_0(long src_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#countNonZero(Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; single-channel array
     * @return the number of nonzero single-channel elements
     */
    private static native int countNonZero_0(long src_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#findNonZero(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; single-channel array
     * @param idx_nativeObj native matrix or object handle for {@code idx}; the output array, type of Mat or
     *                      vector&lt;Point&gt;, corresponding to non-zero indices in the input
     */
    private static native void findNonZero_0(long src_nativeObj, long idx_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#mean(Mat,Mat)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; input array that should have from 1 to 4
     *                       channels so that the result can be stored in Scalar_
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional operation mask ot type CV_8U,
     *                       CV_8S or CV_Bool
     * @return packed numeric fields used by the Java wrapper to construct {@code Scalar}
     */
    private static native double[] mean_0(long src_nativeObj, long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#mean(Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array that should have from 1 to 4
     *                      channels so that the result can be stored in Scalar_
     * @return packed numeric fields used by the Java wrapper to construct {@code Scalar}
     */
    private static native double[] mean_1(long src_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#meanStdDev(Mat,MatOfDouble,MatOfDouble,Mat)}.
     *
     * @param src_nativeObj        native matrix or object handle for {@code src}; input array that should have from 1
     *                             to 4 channels so that the results can be stored in Scalar_ 's
     * @param mean_mat_nativeObj   native matrix or object handle for {@code mean_mat}; output parameter: calculated
     *                             mean value
     * @param stddev_mat_nativeObj native matrix or object handle for {@code stddev_mat}; output parameter: calculated
     *                             standard deviation
     * @param mask_nativeObj       native matrix or object handle for {@code mask}; optional operation mask of type
     *                             CV_8U, CV_8S or CV_Bool
     */
    private static native void meanStdDev_0(
            long src_nativeObj,
            long mean_mat_nativeObj,
            long stddev_mat_nativeObj,
            long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#meanStdDev(Mat,MatOfDouble,MatOfDouble)}.
     *
     * @param src_nativeObj        native matrix or object handle for {@code src}; input array that should have from 1
     *                             to 4 channels so that the results can be stored in Scalar_ 's
     * @param mean_mat_nativeObj   native matrix or object handle for {@code mean_mat}; output parameter: calculated
     *                             mean value
     * @param stddev_mat_nativeObj native matrix or object handle for {@code stddev_mat}; output parameter: calculated
     *                             standard deviation
     */
    private static native void meanStdDev_1(long src_nativeObj, long mean_mat_nativeObj, long stddev_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#norm(Mat,int,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param normType       type of the norm (see NormTypes)
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional operation mask; it must have the
     *                       same size as src1 and type CV_8UC1, CV_8S1 or CV_BoolC1
     * @return the selected norm of the input, or the norm of the difference between the inputs
     */
    private static native double norm_0(long src1_nativeObj, int normType, long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#norm(Mat,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param normType       type of the norm (see NormTypes)
     * @return the selected norm of the input, or the norm of the difference between the inputs
     */
    private static native double norm_1(long src1_nativeObj, int normType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#norm(Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @return the selected norm of the input, or the norm of the difference between the inputs
     */
    private static native double norm_2(long src1_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#norm(Mat,Mat,int,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size and
     *                       the same type as src1
     * @param normType       type of the norm (see NormTypes)
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional operation mask; it must have the
     *                       same size as src1 and type CV_8UC1, CV_8S1 or CV_BoolC1
     * @return the selected norm of the input, or the norm of the difference between the inputs
     */
    private static native double norm_3(long src1_nativeObj, long src2_nativeObj, int normType, long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#norm(Mat,Mat,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size and
     *                       the same type as src1
     * @param normType       type of the norm (see NormTypes)
     * @return the selected norm of the input, or the norm of the difference between the inputs
     */
    private static native double norm_4(long src1_nativeObj, long src2_nativeObj, int normType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#norm(Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size and
     *                       the same type as src1
     * @return the selected norm of the input, or the norm of the difference between the inputs
     */
    private static native double norm_5(long src1_nativeObj, long src2_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#PSNR(Mat,Mat,double)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size as
     *                       src1
     * @param R              the maximum pixel value (255 by default)
     * @return the peak signal-to-noise ratio in decibels
     */
    private static native double PSNR_0(long src1_nativeObj, long src2_nativeObj, double R);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#PSNR(Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size as
     *                       src1
     * @return the peak signal-to-noise ratio in decibels
     */
    private static native double PSNR_1(long src1_nativeObj, long src2_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.core.Core#batchDistance(Mat,Mat,Mat,int,Mat,int,int,Mat,int,boolean)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first source matrix or channel value
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second source matrix or channel value
     * @param dist_nativeObj native matrix or object handle for {@code dist}; output matrix of pairwise descriptor
     *                       distances
     * @param dtype          desired output depth; a negative value keeps the source depth when supported
     * @param nidx_nativeObj native matrix or object handle for {@code nidx}; output matrix of nearest-neighbor indices
     * @param normType       distance or norm calculation mode
     * @param K              number of nearest neighbors retained for each query row
     * @param mask_nativeObj native matrix or object handle for {@code mask}; 8-bit matrix selecting permitted
     *                       source-row pairs for distance calculation; an empty mask permits all pairs
     * @param update         index offset used when updating an existing nearest-neighbor result
     * @param crosscheck     whether to retain only mutually consistent nearest-neighbor matches
     */
    private static native void batchDistance_0(
            long src1_nativeObj,
            long src2_nativeObj,
            long dist_nativeObj,
            int dtype,
            long nidx_nativeObj,
            int normType,
            int K,
            long mask_nativeObj,
            int update,
            boolean crosscheck);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.core.Core#batchDistance(Mat,Mat,Mat,int,Mat,int,int,Mat,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first source matrix or channel value
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second source matrix or channel value
     * @param dist_nativeObj native matrix or object handle for {@code dist}; output matrix of pairwise descriptor
     *                       distances
     * @param dtype          desired output depth; a negative value keeps the source depth when supported
     * @param nidx_nativeObj native matrix or object handle for {@code nidx}; output matrix of nearest-neighbor indices
     * @param normType       distance or norm calculation mode
     * @param K              number of nearest neighbors retained for each query row
     * @param mask_nativeObj native matrix or object handle for {@code mask}; 8-bit matrix selecting permitted
     *                       source-row pairs for distance calculation; an empty mask permits all pairs
     * @param update         index offset used when updating an existing nearest-neighbor result
     */
    private static native void batchDistance_1(
            long src1_nativeObj,
            long src2_nativeObj,
            long dist_nativeObj,
            int dtype,
            long nidx_nativeObj,
            int normType,
            int K,
            long mask_nativeObj,
            int update);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#batchDistance(Mat,Mat,Mat,int,Mat,int,int,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first source matrix or channel value
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second source matrix or channel value
     * @param dist_nativeObj native matrix or object handle for {@code dist}; output matrix of pairwise descriptor
     *                       distances
     * @param dtype          desired output depth; a negative value keeps the source depth when supported
     * @param nidx_nativeObj native matrix or object handle for {@code nidx}; output matrix of nearest-neighbor indices
     * @param normType       distance or norm calculation mode
     * @param K              number of nearest neighbors retained for each query row
     * @param mask_nativeObj native matrix or object handle for {@code mask}; 8-bit matrix selecting permitted
     *                       source-row pairs for distance calculation; an empty mask permits all pairs
     */
    private static native void batchDistance_2(
            long src1_nativeObj,
            long src2_nativeObj,
            long dist_nativeObj,
            int dtype,
            long nidx_nativeObj,
            int normType,
            int K,
            long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#batchDistance(Mat,Mat,Mat,int,Mat,int,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first source matrix or channel value
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second source matrix or channel value
     * @param dist_nativeObj native matrix or object handle for {@code dist}; output matrix of pairwise descriptor
     *                       distances
     * @param dtype          desired output depth; a negative value keeps the source depth when supported
     * @param nidx_nativeObj native matrix or object handle for {@code nidx}; output matrix of nearest-neighbor indices
     * @param normType       distance or norm calculation mode
     * @param K              number of nearest neighbors retained for each query row
     */
    private static native void batchDistance_3(
            long src1_nativeObj,
            long src2_nativeObj,
            long dist_nativeObj,
            int dtype,
            long nidx_nativeObj,
            int normType,
            int K);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#batchDistance(Mat,Mat,Mat,int,Mat,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first source matrix or channel value
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second source matrix or channel value
     * @param dist_nativeObj native matrix or object handle for {@code dist}; output matrix of pairwise descriptor
     *                       distances
     * @param dtype          desired output depth; a negative value keeps the source depth when supported
     * @param nidx_nativeObj native matrix or object handle for {@code nidx}; output matrix of nearest-neighbor indices
     * @param normType       distance or norm calculation mode
     */
    private static native void batchDistance_4(
            long src1_nativeObj,
            long src2_nativeObj,
            long dist_nativeObj,
            int dtype,
            long nidx_nativeObj,
            int normType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#batchDistance(Mat,Mat,Mat,int,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first source matrix or channel value
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second source matrix or channel value
     * @param dist_nativeObj native matrix or object handle for {@code dist}; output matrix of pairwise descriptor
     *                       distances
     * @param dtype          desired output depth; a negative value keeps the source depth when supported
     * @param nidx_nativeObj native matrix or object handle for {@code nidx}; output matrix of nearest-neighbor indices
     */
    private static native void batchDistance_5(
            long src1_nativeObj,
            long src2_nativeObj,
            long dist_nativeObj,
            int dtype,
            long nidx_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#normalize(Mat,Mat,double,double,int,int,Mat)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size as src
     * @param alpha          norm value to normalize to or the lower range boundary in case of the range normalization
     * @param beta           upper range boundary in case of the range normalization; it is not used for the norm
     *                       normalization
     * @param norm_type      normalization type (see NormTypes)
     * @param dtype          when negative, the output array has the same type as src; otherwise, it has the same number
     *                       of channels as src and the depth =CV_MAT_DEPTH(dtype)
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional operation mask of type CV_8U,
     *                       CV_8S or CV_Bool
     */
    private static native void normalize_0(
            long src_nativeObj,
            long dst_nativeObj,
            double alpha,
            double beta,
            int norm_type,
            int dtype,
            long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#normalize(Mat,Mat,double,double,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size as src
     * @param alpha         norm value to normalize to or the lower range boundary in case of the range normalization
     * @param beta          upper range boundary in case of the range normalization; it is not used for the norm
     *                      normalization
     * @param norm_type     normalization type (see NormTypes)
     * @param dtype         when negative, the output array has the same type as src; otherwise, it has the same number
     *                      of channels as src and the depth =CV_MAT_DEPTH(dtype)
     */
    private static native void normalize_1(
            long src_nativeObj,
            long dst_nativeObj,
            double alpha,
            double beta,
            int norm_type,
            int dtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#normalize(Mat,Mat,double,double,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size as src
     * @param alpha         norm value to normalize to or the lower range boundary in case of the range normalization
     * @param beta          upper range boundary in case of the range normalization; it is not used for the norm
     *                      normalization
     * @param norm_type     normalization type (see NormTypes)
     */
    private static native void normalize_2(
            long src_nativeObj,
            long dst_nativeObj,
            double alpha,
            double beta,
            int norm_type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#normalize(Mat,Mat,double,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size as src
     * @param alpha         norm value to normalize to or the lower range boundary in case of the range normalization
     * @param beta          upper range boundary in case of the range normalization; it is not used for the norm
     *                      normalization
     */
    private static native void normalize_3(long src_nativeObj, long dst_nativeObj, double alpha, double beta);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#normalize(Mat,Mat,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size as src
     * @param alpha         norm value to normalize to or the lower range boundary in case of the range normalization
     */
    private static native void normalize_4(long src_nativeObj, long dst_nativeObj, double alpha);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#normalize(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size as src
     */
    private static native void normalize_5(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#reduceArgMin(Mat,Mat,int,boolean)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input single-channel array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of type CV_32SC1 with the same
     *                      dimensionality as src, except for axis being reduced - it should be set to 1
     * @param axis          axis to reduce along
     * @param lastIndex     whether to get the index of first or last occurrence of min
     */
    private static native void reduceArgMin_0(long src_nativeObj, long dst_nativeObj, int axis, boolean lastIndex);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#reduceArgMin(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input single-channel array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of type CV_32SC1 with the same
     *                      dimensionality as src, except for axis being reduced - it should be set to 1
     * @param axis          axis to reduce along
     */
    private static native void reduceArgMin_1(long src_nativeObj, long dst_nativeObj, int axis);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#reduceArgMax(Mat,Mat,int,boolean)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input single-channel array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of type CV_32SC1 with the same
     *                      dimensionality as src, except for axis being reduced - it should be set to 1
     * @param axis          axis to reduce along
     * @param lastIndex     whether to get the index of first or last occurrence of max
     */
    private static native void reduceArgMax_0(long src_nativeObj, long dst_nativeObj, int axis, boolean lastIndex);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#reduceArgMax(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input single-channel array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of type CV_32SC1 with the same
     *                      dimensionality as src, except for axis being reduced - it should be set to 1
     * @param axis          axis to reduce along
     */
    private static native void reduceArgMax_1(long src_nativeObj, long dst_nativeObj, int axis);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#reduce(Mat,Mat,int,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input matrix. It may also be n-dimensional,
     *                      then dim is the axis to reduce and the output keeps it with size 1 (see the ReduceParams
     *                      overload)
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output vector. Its size and type is defined
     *                      by dim and dtype parameters
     * @param dim           dimension index along which the matrix is reduced. 0 means that the matrix is reduced to a
     *                      single row. 1 means that the matrix is reduced to a single column
     * @param rtype         reduction operation that could be one of ReduceTypes
     * @param dtype         when negative, the output vector will have the same type as the input matrix, otherwise, its
     *                      type will be CV_MAKE_TYPE(CV_MAT_DEPTH(dtype), src.channels())
     */
    private static native void reduce_0(long src_nativeObj, long dst_nativeObj, int dim, int rtype, int dtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#reduce(Mat,Mat,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input matrix. It may also be n-dimensional,
     *                      then dim is the axis to reduce and the output keeps it with size 1 (see the ReduceParams
     *                      overload)
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output vector. Its size and type is defined
     *                      by dim and dtype parameters
     * @param dim           dimension index along which the matrix is reduced. 0 means that the matrix is reduced to a
     *                      single row. 1 means that the matrix is reduced to a single column
     * @param rtype         reduction operation that could be one of ReduceTypes
     */
    private static native void reduce_1(long src_nativeObj, long dst_nativeObj, int dim, int rtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#reduce(Mat,Mat,ReduceParams)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input matrix. It may also be
     *                         n-dimensional, then dim is the axis to reduce and the output keeps it with size 1 (see
     *                         the ReduceParams overload)
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output vector. Its size and type is
     *                         defined by dim and dtype parameters
     * @param params_nativeObj {@code params.getNativeObjAddr()} component of the reduction operation and the axes to
     *                         reduce over, see ReduceParams
     */
    private static native void reduce_2(long src_nativeObj, long dst_nativeObj, long params_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#merge(List<Mat>,Mat)}.
     *
     * @param mv_mat_nativeObj native matrix or object handle for {@code mv_mat}; input vector of matrices to be merged;
     *                         all the matrices in mv must have the same size and the same depth
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output array of the same size and the
     *                         same depth as mv[0]; The number of channels will be the total number of channels in the
     *                         matrix array
     */
    private static native void merge_0(long mv_mat_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#split(Mat,List<Mat>)}.
     *
     * @param m_nativeObj      native matrix or object handle for {@code m}; input multi-channel array
     * @param mv_mat_nativeObj native matrix or object handle for {@code mv_mat}; output vector of arrays; the arrays
     *                         themselves are reallocated, if needed
     */
    private static native void split_0(long m_nativeObj, long mv_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#mixChannels(List<Mat>,List<Mat>,MatOfInt)}.
     *
     * @param src_mat_nativeObj    native matrix or object handle for {@code src_mat}; input array or vector of
     *                             matrices; all of the matrices must have the same size and the same depth
     * @param dst_mat_nativeObj    native matrix or object handle for {@code dst_mat}; output array or vector of
     *                             matrices; all the matrices must be allocated; their size and depth must be the same
     *                             as in src[0]
     * @param fromTo_mat_nativeObj native matrix or object handle for {@code fromTo_mat}; array of index pairs
     *                             specifying which channels are copied and where; fromTo[k*2] is a 0-based index of the
     *                             input channel in src, fromTo[k*2+1] is an index of the output channel in dst; the
     *                             continuous channel numbering is used: the first input image channels are indexed from
     *                             0 to src[0].channels()-1, the second input image channels are indexed from
     *                             src[0].channels() to src[0].channels() + src[1].channels()-1, and so on, the same
     *                             scheme is used for the output image channels; as a special case, when fromTo[k*2] is
     *                             negative, the corresponding output channel is filled with zero
     */
    private static native void mixChannels_0(long src_mat_nativeObj, long dst_mat_nativeObj, long fromTo_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#extractChannel(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array
     * @param coi           index of channel to extract
     */
    private static native void extractChannel_0(long src_nativeObj, long dst_nativeObj, int coi);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#insertChannel(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array
     * @param coi           index of channel for insertion
     */
    private static native void insertChannel_0(long src_nativeObj, long dst_nativeObj, int coi);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#flip(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size and type as
     *                      src
     * @param flipCode      a flag to specify how to flip the array; 0 means flipping around the x-axis and positive
     *                      value (for example, 1) means flipping around y-axis. Negative value (for example, -1) means
     *                      flipping around both axes
     */
    private static native void flip_0(long src_nativeObj, long dst_nativeObj, int flipCode);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#flipND(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array that has the same shape of src
     * @param axis          axis that performs a flip on. -src.dims &lt;= axis &lt; src.dims
     */
    private static native void flipND_0(long src_nativeObj, long dst_nativeObj, int axis);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#squeeze(Mat,Mat,MatOfInt)}.
     *
     * @param src_nativeObj      native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj      native matrix or object handle for {@code dst}; output array
     * @param axes_mat_nativeObj native matrix or object handle for {@code axes_mat}; axes to remove, each of size 1;
     *                           negative values count from the end. When empty, all the axes of size 1 are removed
     */
    private static native void squeeze_0(long src_nativeObj, long dst_nativeObj, long axes_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#squeeze(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array
     */
    private static native void squeeze_1(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#unsqueeze(Mat,Mat,MatOfInt)}.
     *
     * @param src_nativeObj      native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj      native matrix or object handle for {@code dst}; output array with src.dims +
     *                           axes.size() dimensions
     * @param axes_mat_nativeObj native matrix or object handle for {@code axes_mat}; positions of the new axes in dst;
     *                           negative values count from the end of dst
     */
    private static native void unsqueeze_0(long src_nativeObj, long dst_nativeObj, long axes_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#flatten(Mat,Mat,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array
     * @param startAxis     first axis to merge; negative values count from the end
     * @param endAxis       last axis to merge (inclusive); negative values count from the end
     */
    private static native void flatten_0(long src_nativeObj, long dst_nativeObj, int startAxis, int endAxis);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#flatten(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array
     * @param startAxis     first axis to merge; negative values count from the end
     */
    private static native void flatten_1(long src_nativeObj, long dst_nativeObj, int startAxis);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#flatten(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array
     */
    private static native void flatten_2(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#reshape(Mat,Mat,MatOfInt,boolean)}.
     *
     * @param src_nativeObj          native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj          native matrix or object handle for {@code dst}; output array
     * @param newShape_mat_nativeObj native matrix or object handle for {@code newShape_mat}; the new shape
     * @param allowZero              treat 0 in newShape as a size instead of "same as src"
     */
    private static native void reshape_0(
            long src_nativeObj,
            long dst_nativeObj,
            long newShape_mat_nativeObj,
            boolean allowZero);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#reshape(Mat,Mat,MatOfInt)}.
     *
     * @param src_nativeObj          native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj          native matrix or object handle for {@code dst}; output array
     * @param newShape_mat_nativeObj native matrix or object handle for {@code newShape_mat}; the new shape
     */
    private static native void reshape_1(long src_nativeObj, long dst_nativeObj, long newShape_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#concat(List<Mat>,int,Mat)}.
     *
     * @param src_mat_nativeObj native matrix or object handle for {@code src_mat}; input arrays
     * @param axis              axis to concatenate along; -dims &lt;= axis &lt; dims
     * @param dst_nativeObj     native matrix or object handle for {@code dst}; output array
     */
    private static native void concat_0(long src_mat_nativeObj, int axis, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#split(Mat,int,MatOfInt,List<Mat>)}.
     *
     * @param src_nativeObj       native matrix or object handle for {@code src}; input array; may be multi-channel and
     *                            non-continuous
     * @param axis                axis to split along; -src.dims &lt;= axis &lt; src.dims
     * @param sizes_mat_nativeObj native matrix or object handle for {@code sizes_mat}; size of each output array along
     *                            axis; the values must sum up to src.size[axis]
     * @param dst_mat_nativeObj   native matrix or object handle for {@code dst_mat}; output arrays, one per element of
     *                            sizes
     */
    private static native void split_1(long src_nativeObj, int axis, long sizes_mat_nativeObj, long dst_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#slice(Mat,MatOfInt,MatOfInt,MatOfInt,Mat)}.
     *
     * @param src_nativeObj        native matrix or object handle for {@code src}; input array; may be multi-channel and
     *                             non-continuous
     * @param starts_mat_nativeObj native matrix or object handle for {@code starts_mat}; first index along each sliced
     *                             axis
     * @param ends_mat_nativeObj   native matrix or object handle for {@code ends_mat}; end index (exclusive) along each
     *                             sliced axis
     * @param steps_mat_nativeObj  native matrix or object handle for {@code steps_mat}; step along each sliced axis
     *                             (non-zero, may be negative); empty means all ones
     * @param dst_nativeObj        native matrix or object handle for {@code dst}; output array of the same type as src
     */
    private static native void slice_0(
            long src_nativeObj,
            long starts_mat_nativeObj,
            long ends_mat_nativeObj,
            long steps_mat_nativeObj,
            long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#broadcast(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj   native matrix or object handle for {@code src}; input array
     * @param shape_nativeObj native matrix or object handle for {@code shape}; target shape. Should be a list of CV_32S
     *                        numbers. Note that negative values are not supported
     * @param dst_nativeObj   native matrix or object handle for {@code dst}; output array that has the given shape
     */
    private static native void broadcast_0(long src_nativeObj, long shape_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#texpr(String,List<Mat>,List<Mat>)}.
     *
     * @param expr                  the expression string, e.g. "{0 * 2.5 + {1}"} or "({0 + {1}, {0} - {1})"}
     * @param inputs_mat_nativeObj  native matrix or object handle for {@code inputs_mat}; the arrays bound to {0}, {1},
     * @param outputs_mat_nativeObj native matrix or object handle for {@code outputs_mat}; receives one array per
     *                              top-level result (one entry, or several for a tuple)
     */
    private static native void texpr_0(String expr, long inputs_mat_nativeObj, long outputs_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#rotate(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same type as src. The
     *                      size is the same with ROTATE_180, and the rows and cols are switched for ROTATE_90_CLOCKWISE
     *                      and ROTATE_90_COUNTERCLOCKWISE
     * @param rotateCode    an enum to specify how to rotate the array; see the enum RotateFlags
     */
    private static native void rotate_0(long src_nativeObj, long dst_nativeObj, int rotateCode);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#repeat(Mat,int,int,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array to replicate
     * @param ny            Flag to specify how many times the src is repeated along the vertical axis
     * @param nx            Flag to specify how many times the src is repeated along the horizontal axis
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same type as src
     */
    private static native void repeat_0(long src_nativeObj, int ny, int nx, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#repeat(Mat,MatOfInt,Mat)}.
     *
     * @param src_nativeObj         native matrix or object handle for {@code src}; input array to replicate
     * @param repeats_mat_nativeObj native matrix or object handle for {@code repeats_mat}; number of copies along each
     *                              axis; must have src.dims non-negative elements
     * @param dst_nativeObj         native matrix or object handle for {@code dst}; output array of the same type as src
     */
    private static native void repeat_1(long src_nativeObj, long repeats_mat_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#hconcat(List<Mat>,Mat)}.
     *
     * @param src_mat_nativeObj native matrix or object handle for {@code src_mat}; input array or vector of matrices.
     *                          all of the matrices must have the same number of rows and the same depth
     * @param dst_nativeObj     native matrix or object handle for {@code dst}; output array. It has the same number of
     *                          rows and depth as the src, and the sum of cols of the src. same depth
     */
    private static native void hconcat_0(long src_mat_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#vconcat(List<Mat>,Mat)}.
     *
     * @param src_mat_nativeObj native matrix or object handle for {@code src_mat}; input array or vector of matrices.
     *                          all of the matrices must have the same number of cols and the same depth
     * @param dst_nativeObj     native matrix or object handle for {@code dst}; output array. It has the same number of
     *                          cols and depth as the src, and the sum of rows of the src. same depth
     */
    private static native void vconcat_0(long src_mat_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#bitwise_and(Mat,Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       type as the input arrays
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional operation mask, CV_8U, CV_8S or
     *                       CV_Bool single channel array, that specifies elements of the output array to be changed
     */
    private static native void bitwise_and_0(
            long src1_nativeObj,
            long src2_nativeObj,
            long dst_nativeObj,
            long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#bitwise_and(Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       type as the input arrays
     */
    private static native void bitwise_and_1(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#bitwise_or(Mat,Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       type as the input arrays
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional operation mask, CV_8U, CV_8S or
     *                       CV_Bool single channel array, that specifies elements of the output array to be changed
     */
    private static native void bitwise_or_0(
            long src1_nativeObj,
            long src2_nativeObj,
            long dst_nativeObj,
            long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#bitwise_or(Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       type as the input arrays
     */
    private static native void bitwise_or_1(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#bitwise_xor(Mat,Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       type as the input arrays
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional operation mask, CV_8U, CV_8S or
     *                       CV_Bool single channel array, that specifies elements of the output array to be changed
     */
    private static native void bitwise_xor_0(
            long src1_nativeObj,
            long src2_nativeObj,
            long dst_nativeObj,
            long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#bitwise_xor(Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       type as the input arrays
     */
    private static native void bitwise_xor_1(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#bitwise_not(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       type as the input array
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional operation mask, CV_8U, CV_8S or
     *                       CV_Bool single channel array, that specifies elements of the output array to be changed
     */
    private static native void bitwise_not_0(long src_nativeObj, long dst_nativeObj, long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#bitwise_not(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array that has the same size and type
     *                      as the input array
     */
    private static native void bitwise_not_1(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#absdiff(Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       type as input arrays
     */
    private static native void absdiff_0(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#copyTo(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; source matrix
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; Destination matrix. If it does not have a
     *                       proper size or type before the operation, it is reallocated
     * @param mask_nativeObj native matrix or object handle for {@code mask}; Operation mask of the same size as *this.
     *                       Its non-zero elements indicate which matrix elements need to be copied. The mask has to be
     *                       of type CV_8U, CV_8S or CV_Bool and can have 1 or multiple channels
     */
    private static native void copyTo_0(long src_nativeObj, long dst_nativeObj, long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#inRange(Mat,Scalar,Scalar,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; first input array
     * @param lowerb_val0   {@code lowerb.val[0]} component of inclusive lower boundary array or a scalar
     * @param lowerb_val1   {@code lowerb.val[1]} component of inclusive lower boundary array or a scalar
     * @param lowerb_val2   {@code lowerb.val[2]} component of inclusive lower boundary array or a scalar
     * @param lowerb_val3   {@code lowerb.val[3]} component of inclusive lower boundary array or a scalar
     * @param upperb_val0   {@code upperb.val[0]} component of inclusive upper boundary array or a scalar
     * @param upperb_val1   {@code upperb.val[1]} component of inclusive upper boundary array or a scalar
     * @param upperb_val2   {@code upperb.val[2]} component of inclusive upper boundary array or a scalar
     * @param upperb_val3   {@code upperb.val[3]} component of inclusive upper boundary array or a scalar
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size as src and
     *                      CV_8U type
     */
    private static native void inRange_0(
            long src_nativeObj,
            double lowerb_val0,
            double lowerb_val1,
            double lowerb_val2,
            double lowerb_val3,
            double upperb_val0,
            double upperb_val1,
            double upperb_val2,
            double upperb_val3,
            long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#compare(Mat,Mat,Mat,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar; when it is
     *                       an array, it must have a single channel
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array or a scalar; when it is
     *                       an array, it must have a single channel
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of type ref CV_8U that has the
     *                       same size and the same number of channels as the input arrays
     * @param cmpop          a flag, that specifies correspondence between the arrays (CmpTypes)
     */
    private static native void compare_0(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj, int cmpop);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#min(Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size and
     *                       type as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src1
     */
    private static native void min_0(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#max(Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second input array of the same size and
     *                       type as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src1
     */
    private static native void max_0(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#sqrt(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input floating-point array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size and type as
     *                      src
     */
    private static native void sqrt_0(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#pow(Mat,double,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param power         exponent of power
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size and type as
     *                      src
     */
    private static native void pow_0(long src_nativeObj, double power, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#exp(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size and type as
     *                      src
     */
    private static native void exp_0(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#log(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size and type as
     *                      src
     */
    private static native void log_0(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#polarToCart(Mat,Mat,Mat,Mat,boolean)}.
     *
     * @param magnitude_nativeObj native matrix or object handle for {@code magnitude}; input floating-point array of
     *                            magnitudes of 2D vectors; it can be an empty matrix (=Mat()), in this case, the
     *                            function assumes that all the magnitudes are =1; if it is not empty, it must have the
     *                            same size and type as angle
     * @param angle_nativeObj     native matrix or object handle for {@code angle}; input floating-point array of angles
     *                            of 2D vectors
     * @param x_nativeObj         native matrix or object handle for {@code x}; output array of x-coordinates of 2D
     *                            vectors; it has the same size and type as angle
     * @param y_nativeObj         native matrix or object handle for {@code y}; output array of y-coordinates of 2D
     *                            vectors; it has the same size and type as angle
     * @param angleInDegrees      when true, the input angles are measured in degrees, otherwise, they are measured in
     *                            radians
     */
    private static native void polarToCart_0(
            long magnitude_nativeObj,
            long angle_nativeObj,
            long x_nativeObj,
            long y_nativeObj,
            boolean angleInDegrees);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#polarToCart(Mat,Mat,Mat,Mat)}.
     *
     * @param magnitude_nativeObj native matrix or object handle for {@code magnitude}; input floating-point array of
     *                            magnitudes of 2D vectors; it can be an empty matrix (=Mat()), in this case, the
     *                            function assumes that all the magnitudes are =1; if it is not empty, it must have the
     *                            same size and type as angle
     * @param angle_nativeObj     native matrix or object handle for {@code angle}; input floating-point array of angles
     *                            of 2D vectors
     * @param x_nativeObj         native matrix or object handle for {@code x}; output array of x-coordinates of 2D
     *                            vectors; it has the same size and type as angle
     * @param y_nativeObj         native matrix or object handle for {@code y}; output array of y-coordinates of 2D
     *                            vectors; it has the same size and type as angle
     */
    private static native void polarToCart_1(
            long magnitude_nativeObj,
            long angle_nativeObj,
            long x_nativeObj,
            long y_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#cartToPolar(Mat,Mat,Mat,Mat,boolean)}.
     *
     * @param x_nativeObj         native matrix or object handle for {@code x}; array of x-coordinates; this must be a
     *                            single-precision or double-precision floating-point array
     * @param y_nativeObj         native matrix or object handle for {@code y}; array of y-coordinates, that must have
     *                            the same size and same type as x
     * @param magnitude_nativeObj native matrix or object handle for {@code magnitude}; output array of magnitudes of
     *                            the same size and type as x
     * @param angle_nativeObj     native matrix or object handle for {@code angle}; output array of angles that has the
     *                            same size and type as x; the angles are measured in radians (from 0 to 2*Pi) or in
     *                            degrees (0 to 360 degrees)
     * @param angleInDegrees      a flag, indicating whether the angles are measured in radians (which is by default),
     *                            or in degrees
     */
    private static native void cartToPolar_0(
            long x_nativeObj,
            long y_nativeObj,
            long magnitude_nativeObj,
            long angle_nativeObj,
            boolean angleInDegrees);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#cartToPolar(Mat,Mat,Mat,Mat)}.
     *
     * @param x_nativeObj         native matrix or object handle for {@code x}; array of x-coordinates; this must be a
     *                            single-precision or double-precision floating-point array
     * @param y_nativeObj         native matrix or object handle for {@code y}; array of y-coordinates, that must have
     *                            the same size and same type as x
     * @param magnitude_nativeObj native matrix or object handle for {@code magnitude}; output array of magnitudes of
     *                            the same size and type as x
     * @param angle_nativeObj     native matrix or object handle for {@code angle}; output array of angles that has the
     *                            same size and type as x; the angles are measured in radians (from 0 to 2*Pi) or in
     *                            degrees (0 to 360 degrees)
     */
    private static native void cartToPolar_1(
            long x_nativeObj,
            long y_nativeObj,
            long magnitude_nativeObj,
            long angle_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#phase(Mat,Mat,Mat,boolean)}.
     *
     * @param x_nativeObj     native matrix or object handle for {@code x}; input floating-point array of x-coordinates
     *                        of 2D vectors
     * @param y_nativeObj     native matrix or object handle for {@code y}; input array of y-coordinates of 2D vectors;
     *                        it must have the same size and the same type as x
     * @param angle_nativeObj native matrix or object handle for {@code angle}; output array of vector angles; it has
     *                        the same size and same type as x
     * @param angleInDegrees  when true, the function calculates the angle in degrees, otherwise, they are measured in
     *                        radians
     */
    private static native void phase_0(
            long x_nativeObj,
            long y_nativeObj,
            long angle_nativeObj,
            boolean angleInDegrees);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#phase(Mat,Mat,Mat)}.
     *
     * @param x_nativeObj     native matrix or object handle for {@code x}; input floating-point array of x-coordinates
     *                        of 2D vectors
     * @param y_nativeObj     native matrix or object handle for {@code y}; input array of y-coordinates of 2D vectors;
     *                        it must have the same size and the same type as x
     * @param angle_nativeObj native matrix or object handle for {@code angle}; output array of vector angles; it has
     *                        the same size and same type as x
     */
    private static native void phase_1(long x_nativeObj, long y_nativeObj, long angle_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#magnitude(Mat,Mat,Mat)}.
     *
     * @param x_nativeObj         native matrix or object handle for {@code x}; floating-point array of x-coordinates of
     *                            the vectors
     * @param y_nativeObj         native matrix or object handle for {@code y}; floating-point array of y-coordinates of
     *                            the vectors; it must have the same size as x
     * @param magnitude_nativeObj native matrix or object handle for {@code magnitude}; output array of the same size
     *                            and type as x
     */
    private static native void magnitude_0(long x_nativeObj, long y_nativeObj, long magnitude_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#checkRange(Mat,boolean,double,double)}.
     *
     * @param a_nativeObj native matrix or object handle for {@code a}; input array
     * @param quiet       a flag, indicating whether the functions quietly return false when the array elements are out
     *                    of range or they throw an exception. elements
     * @param minVal      inclusive lower boundary of valid values range
     * @param maxVal      exclusive upper boundary of valid values range
     * @return true if all checked values are finite and within the specified interval
     */
    private static native boolean checkRange_0(long a_nativeObj, boolean quiet, double minVal, double maxVal);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#checkRange(Mat,boolean,double)}.
     *
     * @param a_nativeObj native matrix or object handle for {@code a}; input array
     * @param quiet       a flag, indicating whether the functions quietly return false when the array elements are out
     *                    of range or they throw an exception. elements
     * @param minVal      inclusive lower boundary of valid values range
     * @return true if all checked values are finite and within the specified interval
     */
    private static native boolean checkRange_1(long a_nativeObj, boolean quiet, double minVal);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#checkRange(Mat,boolean)}.
     *
     * @param a_nativeObj native matrix or object handle for {@code a}; input array
     * @param quiet       a flag, indicating whether the functions quietly return false when the array elements are out
     *                    of range or they throw an exception. elements
     * @return true if all checked values are finite and within the specified interval
     */
    private static native boolean checkRange_2(long a_nativeObj, boolean quiet);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#checkRange(Mat)}.
     *
     * @param a_nativeObj native matrix or object handle for {@code a}; input array
     * @return true if all checked values are finite and within the specified interval
     */
    private static native boolean checkRange_4(long a_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#patchNaNs(Mat,double)}.
     *
     * @param a_nativeObj native matrix or object handle for {@code a}; Input/output matrix (CV_16F, CV_16BF, CV_32F or
     *                    CV_64F type)
     * @param val         Value used to replace NaNs (defaults to 0)
     */
    private static native void patchNaNs_0(long a_nativeObj, double val);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#patchNaNs(Mat)}.
     *
     * @param a_nativeObj native matrix or object handle for {@code a}; Input/output matrix (CV_16F, CV_16BF, CV_32F or
     *                    CV_64F type)
     */
    private static native void patchNaNs_1(long a_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#finiteMask(Mat,Mat)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; Input matrix, should contain float or
     *                       double elements of 1 to 4 channels
     * @param mask_nativeObj native matrix or object handle for {@code mask}; Output matrix of the same size as input of
     *                       type CV_8UC1
     */
    private static native void finiteMask_0(long src_nativeObj, long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#gemm(Mat,Mat,double,Mat,double,Mat,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first multiplied input matrix that could
     *                       be real(CV_32FC1, CV_64FC1) or complex(CV_32FC2, CV_64FC2)
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second multiplied input matrix of the same
     *                       type as src1
     * @param alpha          weight of the matrix product
     * @param src3_nativeObj native matrix or object handle for {@code src3}; third optional delta matrix added to the
     *                       matrix product; it should have the same type as src1 and src2
     * @param beta           weight of src3
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output matrix; it has the proper size and
     *                       the same type as input matrices
     * @param flags          operation flags (GemmFlags)
     */
    private static native void gemm_0(
            long src1_nativeObj,
            long src2_nativeObj,
            double alpha,
            long src3_nativeObj,
            double beta,
            long dst_nativeObj,
            int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#gemm(Mat,Mat,double,Mat,double,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first multiplied input matrix that could
     *                       be real(CV_32FC1, CV_64FC1) or complex(CV_32FC2, CV_64FC2)
     * @param src2_nativeObj native matrix or object handle for {@code src2}; second multiplied input matrix of the same
     *                       type as src1
     * @param alpha          weight of the matrix product
     * @param src3_nativeObj native matrix or object handle for {@code src3}; third optional delta matrix added to the
     *                       matrix product; it should have the same type as src1 and src2
     * @param beta           weight of src3
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output matrix; it has the proper size and
     *                       the same type as input matrices
     */
    private static native void gemm_1(
            long src1_nativeObj,
            long src2_nativeObj,
            double alpha,
            long src3_nativeObj,
            double beta,
            long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#mulTransposed(Mat,Mat,boolean,Mat,double,int)}.
     *
     * @param src_nativeObj   native matrix or object handle for {@code src}; input single-channel matrix. Note that
     *                        unlike gemm, the function can multiply not only floating-point matrices
     * @param dst_nativeObj   native matrix or object handle for {@code dst}; output square matrix
     * @param aTa             Flag specifying the multiplication ordering. See the description below
     * @param delta_nativeObj native matrix or object handle for {@code delta}; Optional delta matrix subtracted from
     *                        src before the multiplication. When the matrix is empty ( delta=noArray() ), it is assumed
     *                        to be zero, that is, nothing is subtracted. If it has the same size as src, it is simply
     *                        subtracted. Otherwise, it is "repeated" (see repeat ) to cover the full src and then
     *                        subtracted. Type of the delta matrix, when it is not empty, must be the same as the type
     *                        of created output matrix. See the dtype parameter description below
     * @param scale           Optional scale factor for the matrix product
     * @param dtype           Optional type of the output matrix. When it is negative, the output matrix will have the
     *                        same type as src . Otherwise, it will be type=CV_MAT_DEPTH(dtype) that should be either
     *                        CV_32F or CV_64F
     */
    private static native void mulTransposed_0(
            long src_nativeObj,
            long dst_nativeObj,
            boolean aTa,
            long delta_nativeObj,
            double scale,
            int dtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#mulTransposed(Mat,Mat,boolean,Mat,double)}.
     *
     * @param src_nativeObj   native matrix or object handle for {@code src}; input single-channel matrix. Note that
     *                        unlike gemm, the function can multiply not only floating-point matrices
     * @param dst_nativeObj   native matrix or object handle for {@code dst}; output square matrix
     * @param aTa             Flag specifying the multiplication ordering. See the description below
     * @param delta_nativeObj native matrix or object handle for {@code delta}; Optional delta matrix subtracted from
     *                        src before the multiplication. When the matrix is empty ( delta=noArray() ), it is assumed
     *                        to be zero, that is, nothing is subtracted. If it has the same size as src, it is simply
     *                        subtracted. Otherwise, it is "repeated" (see repeat ) to cover the full src and then
     *                        subtracted. Type of the delta matrix, when it is not empty, must be the same as the type
     *                        of created output matrix. See the dtype parameter description below
     * @param scale           Optional scale factor for the matrix product
     */
    private static native void mulTransposed_1(
            long src_nativeObj,
            long dst_nativeObj,
            boolean aTa,
            long delta_nativeObj,
            double scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#mulTransposed(Mat,Mat,boolean,Mat)}.
     *
     * @param src_nativeObj   native matrix or object handle for {@code src}; input single-channel matrix. Note that
     *                        unlike gemm, the function can multiply not only floating-point matrices
     * @param dst_nativeObj   native matrix or object handle for {@code dst}; output square matrix
     * @param aTa             Flag specifying the multiplication ordering. See the description below
     * @param delta_nativeObj native matrix or object handle for {@code delta}; Optional delta matrix subtracted from
     *                        src before the multiplication. When the matrix is empty ( delta=noArray() ), it is assumed
     *                        to be zero, that is, nothing is subtracted. If it has the same size as src, it is simply
     *                        subtracted. Otherwise, it is "repeated" (see repeat ) to cover the full src and then
     *                        subtracted. Type of the delta matrix, when it is not empty, must be the same as the type
     *                        of created output matrix. See the dtype parameter description below
     */
    private static native void mulTransposed_2(
            long src_nativeObj,
            long dst_nativeObj,
            boolean aTa,
            long delta_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#mulTransposed(Mat,Mat,boolean)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input single-channel matrix. Note that
     *                      unlike gemm, the function can multiply not only floating-point matrices
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output square matrix
     * @param aTa           Flag specifying the multiplication ordering. See the description below
     */
    private static native void mulTransposed_3(long src_nativeObj, long dst_nativeObj, boolean aTa);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#transpose(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same type as src
     */
    private static native void transpose_0(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#transpose(Mat,MatOfInt,Mat)}.
     *
     * @param src_nativeObj       native matrix or object handle for {@code src}; input array
     * @param order_mat_nativeObj native matrix or object handle for {@code order_mat}; a permutation of [0,1,..,N-1]
     *                            where N is the number of axes of src. The i'th axis of dst will correspond to the axis
     *                            numbered order[i] of the input
     * @param dst_nativeObj       native matrix or object handle for {@code dst}; output array of the same type as src
     */
    private static native void transpose_1(long src_nativeObj, long order_mat_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#transposeND(Mat,MatOfInt,Mat)}.
     *
     * @param src_nativeObj       native matrix or object handle for {@code src}; source matrix
     * @param order_mat_nativeObj native matrix or object handle for {@code order_mat}; axis indices in the order
     *                            required for the transposed tensor
     * @param dst_nativeObj       native matrix or object handle for {@code dst}; destination matrix that receives the
     *                            result
     */
    private static native void transposeND_0(long src_nativeObj, long order_mat_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#transform(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array that must have as many channels
     *                      (1 to 4) as m.cols or m.cols-1
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size and depth as
     *                      src; it has as many channels as m.rows
     * @param m_nativeObj   native matrix or object handle for {@code m}; transformation 2x2 or 2x3 floating-point
     *                      matrix
     */
    private static native void transform_0(long src_nativeObj, long dst_nativeObj, long m_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#perspectiveTransform(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input two-channel or three-channel
     *                      floating-point array; each element is a 2D/3D vector to be transformed
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size and type as
     *                      src
     * @param m_nativeObj   native matrix or object handle for {@code m}; 3x3 or 4x4 floating-point transformation
     *                      matrix
     */
    private static native void perspectiveTransform_0(long src_nativeObj, long dst_nativeObj, long m_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#completeSymm(Mat,boolean)}.
     *
     * @param m_nativeObj  native matrix or object handle for {@code m}; input-output floating-point square matrix
     * @param lowerToUpper operation flag; if true, the lower half is copied to the upper half. Otherwise, the upper
     *                     half is copied to the lower half
     */
    private static native void completeSymm_0(long m_nativeObj, boolean lowerToUpper);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#completeSymm(Mat)}.
     *
     * @param m_nativeObj native matrix or object handle for {@code m}; input-output floating-point square matrix
     */
    private static native void completeSymm_1(long m_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#setIdentity(Mat,Scalar)}.
     *
     * @param mtx_nativeObj native matrix or object handle for {@code mtx}; matrix to initialize (not necessarily
     *                      square)
     * @param s_val0        {@code s.val[0]} component of value to assign to diagonal elements
     * @param s_val1        {@code s.val[1]} component of value to assign to diagonal elements
     * @param s_val2        {@code s.val[2]} component of value to assign to diagonal elements
     * @param s_val3        {@code s.val[3]} component of value to assign to diagonal elements
     */
    private static native void setIdentity_0(
            long mtx_nativeObj,
            double s_val0,
            double s_val1,
            double s_val2,
            double s_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#setIdentity(Mat)}.
     *
     * @param mtx_nativeObj native matrix or object handle for {@code mtx}; matrix to initialize (not necessarily
     *                      square)
     */
    private static native void setIdentity_1(long mtx_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#determinant(Mat)}.
     *
     * @param mtx_nativeObj native matrix or object handle for {@code mtx}; input matrix that must have CV_32FC1 or
     *                      CV_64FC1 type and square size
     * @return the matrix determinant
     */
    private static native double determinant_0(long mtx_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#trace(Mat)}.
     *
     * @param mtx_nativeObj native matrix or object handle for {@code mtx}; input matrix
     * @return packed numeric fields used by the Java wrapper to construct {@code Scalar}
     */
    private static native double[] trace_0(long mtx_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#invert(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input floating-point M x N matrix
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output matrix of N x M size and the same
     *                      type as src
     * @param flags         inversion method (DecompTypes)
     * @return a method-dependent inversion status: success indicator or reciprocal condition estimate
     */
    private static native double invert_0(long src_nativeObj, long dst_nativeObj, int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#invert(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input floating-point M x N matrix
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output matrix of N x M size and the same
     *                      type as src
     * @return a method-dependent inversion status: success indicator or reciprocal condition estimate
     */
    private static native double invert_1(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#solve(Mat,Mat,Mat,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; input matrix on the left-hand side of the
     *                       system
     * @param src2_nativeObj native matrix or object handle for {@code src2}; input matrix on the right-hand side of the
     *                       system
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output solution
     * @param flags          solution (matrix inversion) method (DecompTypes)
     * @return true if the requested linear-system solution is found
     */
    private static native boolean solve_0(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj, int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#solve(Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; input matrix on the left-hand side of the
     *                       system
     * @param src2_nativeObj native matrix or object handle for {@code src2}; input matrix on the right-hand side of the
     *                       system
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output solution
     * @return true if the requested linear-system solution is found
     */
    private static native boolean solve_1(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#sort(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input single-channel array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size and type as
     *                      src
     * @param flags         operation flags, a combination of SortFlags
     */
    private static native void sort_0(long src_nativeObj, long dst_nativeObj, int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#sortIdx(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input single-channel array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output integer array of the same size as src
     * @param flags         operation flags that could be a combination of SortFlags
     */
    private static native void sortIdx_0(long src_nativeObj, long dst_nativeObj, int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#solveCubic(Mat,Mat)}.
     *
     * @param coeffs_nativeObj native matrix or object handle for {@code coeffs}; equation coefficients, an array of 3
     *                         or 4 elements
     * @param roots_nativeObj  native matrix or object handle for {@code roots}; output array of real roots that has 0,
     *                         1, 2 or 3 elements
     * @return number of real roots. It can be -1 (all real numbers), 0, 1, 2 or 3
     */
    private static native int solveCubic_0(long coeffs_nativeObj, long roots_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#solvePoly(Mat,Mat,int)}.
     *
     * @param coeffs_nativeObj native matrix or object handle for {@code coeffs}; array of polynomial coefficients
     * @param roots_nativeObj  native matrix or object handle for {@code roots}; output (complex) array of roots
     * @param maxIters         maximum number of iterations the algorithm does
     * @return the maximum root-update error reached by the polynomial solver
     */
    private static native double solvePoly_0(long coeffs_nativeObj, long roots_nativeObj, int maxIters);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#solvePoly(Mat,Mat)}.
     *
     * @param coeffs_nativeObj native matrix or object handle for {@code coeffs}; array of polynomial coefficients
     * @param roots_nativeObj  native matrix or object handle for {@code roots}; output (complex) array of roots
     * @return the maximum root-update error reached by the polynomial solver
     */
    private static native double solvePoly_1(long coeffs_nativeObj, long roots_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#eigen(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj          native matrix or object handle for {@code src}; input matrix that must have
     *                               CV_32FC1 or CV_64FC1 type, square size and be symmetrical (src ^T^ == src)
     * @param eigenvalues_nativeObj  native matrix or object handle for {@code eigenvalues}; output vector of
     *                               eigenvalues of the same type as src; the eigenvalues are stored in the descending
     *                               order
     * @param eigenvectors_nativeObj native matrix or object handle for {@code eigenvectors}; output matrix of
     *                               eigenvectors; it has the same size and type as src; the eigenvectors are stored as
     *                               subsequent matrix rows, in the same order as the corresponding eigenvalues
     * @return true if the eigenvalue decomposition succeeds
     */
    private static native boolean eigen_0(long src_nativeObj, long eigenvalues_nativeObj, long eigenvectors_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#eigen(Mat,Mat)}.
     *
     * @param src_nativeObj         native matrix or object handle for {@code src}; input matrix that must have CV_32FC1
     *                              or CV_64FC1 type, square size and be symmetrical (src ^T^ == src)
     * @param eigenvalues_nativeObj native matrix or object handle for {@code eigenvalues}; output vector of eigenvalues
     *                              of the same type as src; the eigenvalues are stored in the descending order
     * @return true if the eigenvalue decomposition succeeds
     */
    private static native boolean eigen_1(long src_nativeObj, long eigenvalues_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#eigenNonSymmetric(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj          native matrix or object handle for {@code src}; input matrix (CV_32FC1 or CV_64FC1
     *                               type)
     * @param eigenvalues_nativeObj  native matrix or object handle for {@code eigenvalues}; output vector of
     *                               eigenvalues (type is the same type as src)
     * @param eigenvectors_nativeObj native matrix or object handle for {@code eigenvectors}; output matrix of
     *                               eigenvectors (type is the same type as src). The eigenvectors are stored as
     *                               subsequent matrix rows, in the same order as the corresponding eigenvalues
     */
    private static native void eigenNonSymmetric_0(
            long src_nativeObj,
            long eigenvalues_nativeObj,
            long eigenvectors_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#calcCovarMatrix(Mat,Mat,Mat,int,int)}.
     *
     * @param samples_nativeObj native matrix or object handle for {@code samples}; samples stored as rows/columns of a
     *                          single matrix
     * @param covar_nativeObj   native matrix or object handle for {@code covar}; output covariance matrix of the type
     *                          ctype and square size
     * @param mean_nativeObj    native matrix or object handle for {@code mean}; input or output (depending on the
     *                          flags) array as the average value of the input vectors
     * @param flags             operation flags as a combination of CovarFlags
     * @param ctype             type of the matrixl; it equals 'CV_64F' by default
     */
    private static native void calcCovarMatrix_0(
            long samples_nativeObj,
            long covar_nativeObj,
            long mean_nativeObj,
            int flags,
            int ctype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#calcCovarMatrix(Mat,Mat,Mat,int)}.
     *
     * @param samples_nativeObj native matrix or object handle for {@code samples}; samples stored as rows/columns of a
     *                          single matrix
     * @param covar_nativeObj   native matrix or object handle for {@code covar}; output covariance matrix of the type
     *                          ctype and square size
     * @param mean_nativeObj    native matrix or object handle for {@code mean}; input or output (depending on the
     *                          flags) array as the average value of the input vectors
     * @param flags             operation flags as a combination of CovarFlags
     */
    private static native void calcCovarMatrix_1(
            long samples_nativeObj,
            long covar_nativeObj,
            long mean_nativeObj,
            int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#PCACompute(Mat,Mat,Mat,int)}.
     *
     * @param data_nativeObj         native matrix or object handle for {@code data}; array or buffer containing matrix
     *                               channel values
     * @param mean_nativeObj         native matrix or object handle for {@code mean}; mean vector associated with the
     *                               principal-component basis
     * @param eigenvectors_nativeObj native matrix or object handle for {@code eigenvectors}; principal-component basis
     *                               vectors stored as matrix rows
     * @param maxComponents          maximum number of principal components to retain; zero retains all available
     *                               components
     */
    private static native void PCACompute_0(
            long data_nativeObj,
            long mean_nativeObj,
            long eigenvectors_nativeObj,
            int maxComponents);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#PCACompute(Mat,Mat,Mat)}.
     *
     * @param data_nativeObj         native matrix or object handle for {@code data}; array or buffer containing matrix
     *                               channel values
     * @param mean_nativeObj         native matrix or object handle for {@code mean}; mean vector associated with the
     *                               principal-component basis
     * @param eigenvectors_nativeObj native matrix or object handle for {@code eigenvectors}; principal-component basis
     *                               vectors stored as matrix rows
     */
    private static native void PCACompute_1(long data_nativeObj, long mean_nativeObj, long eigenvectors_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#PCACompute2(Mat,Mat,Mat,Mat,int)}.
     *
     * @param data_nativeObj         native matrix or object handle for {@code data}; array or buffer containing matrix
     *                               channel values
     * @param mean_nativeObj         native matrix or object handle for {@code mean}; mean vector associated with the
     *                               principal-component basis
     * @param eigenvectors_nativeObj native matrix or object handle for {@code eigenvectors}; principal-component basis
     *                               vectors stored as matrix rows
     * @param eigenvalues_nativeObj  native matrix or object handle for {@code eigenvalues}; eigenvalues corresponding
     *                               to the retained principal components
     * @param maxComponents          maximum number of principal components to retain; zero retains all available
     *                               components
     */
    private static native void PCACompute2_0(
            long data_nativeObj,
            long mean_nativeObj,
            long eigenvectors_nativeObj,
            long eigenvalues_nativeObj,
            int maxComponents);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#PCACompute2(Mat,Mat,Mat,Mat)}.
     *
     * @param data_nativeObj         native matrix or object handle for {@code data}; array or buffer containing matrix
     *                               channel values
     * @param mean_nativeObj         native matrix or object handle for {@code mean}; mean vector associated with the
     *                               principal-component basis
     * @param eigenvectors_nativeObj native matrix or object handle for {@code eigenvectors}; principal-component basis
     *                               vectors stored as matrix rows
     * @param eigenvalues_nativeObj  native matrix or object handle for {@code eigenvalues}; eigenvalues corresponding
     *                               to the retained principal components
     */
    private static native void PCACompute2_1(
            long data_nativeObj,
            long mean_nativeObj,
            long eigenvectors_nativeObj,
            long eigenvalues_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#PCACompute(Mat,Mat,Mat,double)}.
     *
     * @param data_nativeObj         native matrix or object handle for {@code data}; array or buffer containing matrix
     *                               channel values
     * @param mean_nativeObj         native matrix or object handle for {@code mean}; mean vector associated with the
     *                               principal-component basis
     * @param eigenvectors_nativeObj native matrix or object handle for {@code eigenvectors}; principal-component basis
     *                               vectors stored as matrix rows
     * @param retainedVariance       fraction of total variance that the retained principal components should explain
     */
    private static native void PCACompute_2(
            long data_nativeObj,
            long mean_nativeObj,
            long eigenvectors_nativeObj,
            double retainedVariance);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#PCACompute2(Mat,Mat,Mat,Mat,double)}.
     *
     * @param data_nativeObj         native matrix or object handle for {@code data}; array or buffer containing matrix
     *                               channel values
     * @param mean_nativeObj         native matrix or object handle for {@code mean}; mean vector associated with the
     *                               principal-component basis
     * @param eigenvectors_nativeObj native matrix or object handle for {@code eigenvectors}; principal-component basis
     *                               vectors stored as matrix rows
     * @param eigenvalues_nativeObj  native matrix or object handle for {@code eigenvalues}; eigenvalues corresponding
     *                               to the retained principal components
     * @param retainedVariance       fraction of total variance that the retained principal components should explain
     */
    private static native void PCACompute2_2(
            long data_nativeObj,
            long mean_nativeObj,
            long eigenvectors_nativeObj,
            long eigenvalues_nativeObj,
            double retainedVariance);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#PCAProject(Mat,Mat,Mat,Mat)}.
     *
     * @param data_nativeObj         native matrix or object handle for {@code data}; array or buffer containing matrix
     *                               channel values
     * @param mean_nativeObj         native matrix or object handle for {@code mean}; mean vector associated with the
     *                               principal-component basis
     * @param eigenvectors_nativeObj native matrix or object handle for {@code eigenvectors}; principal-component basis
     *                               vectors stored as matrix rows
     * @param result_nativeObj       native matrix or object handle for {@code result}; destination for projected
     *                               coordinates or reconstructed samples
     */
    private static native void PCAProject_0(
            long data_nativeObj,
            long mean_nativeObj,
            long eigenvectors_nativeObj,
            long result_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#PCABackProject(Mat,Mat,Mat,Mat)}.
     *
     * @param data_nativeObj         native matrix or object handle for {@code data}; array or buffer containing matrix
     *                               channel values
     * @param mean_nativeObj         native matrix or object handle for {@code mean}; mean vector associated with the
     *                               principal-component basis
     * @param eigenvectors_nativeObj native matrix or object handle for {@code eigenvectors}; principal-component basis
     *                               vectors stored as matrix rows
     * @param result_nativeObj       native matrix or object handle for {@code result}; destination for projected
     *                               coordinates or reconstructed samples
     */
    private static native void PCABackProject_0(
            long data_nativeObj,
            long mean_nativeObj,
            long eigenvectors_nativeObj,
            long result_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#SVDecomp(Mat,Mat,Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; source matrix
     * @param w_nativeObj   native matrix or object handle for {@code w}; singular values of the matrix decomposition
     * @param u_nativeObj   native matrix or object handle for {@code u}; left singular vectors of the matrix
     *                      decomposition
     * @param vt_nativeObj  native matrix or object handle for {@code vt}; transposed right singular vectors of the
     *                      matrix decomposition
     * @param flags         combination of SVD_MODIFY_A, SVD_NO_UV and SVD_FULL_UV flags
     */
    private static native void SVDecomp_0(
            long src_nativeObj,
            long w_nativeObj,
            long u_nativeObj,
            long vt_nativeObj,
            int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#SVDecomp(Mat,Mat,Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; source matrix
     * @param w_nativeObj   native matrix or object handle for {@code w}; singular values of the matrix decomposition
     * @param u_nativeObj   native matrix or object handle for {@code u}; left singular vectors of the matrix
     *                      decomposition
     * @param vt_nativeObj  native matrix or object handle for {@code vt}; transposed right singular vectors of the
     *                      matrix decomposition
     */
    private static native void SVDecomp_1(long src_nativeObj, long w_nativeObj, long u_nativeObj, long vt_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#SVBackSubst(Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param w_nativeObj   native matrix or object handle for {@code w}; singular values of the matrix decomposition
     * @param u_nativeObj   native matrix or object handle for {@code u}; left singular vectors of the matrix
     *                      decomposition
     * @param vt_nativeObj  native matrix or object handle for {@code vt}; transposed right singular vectors of the
     *                      matrix decomposition
     * @param rhs_nativeObj native matrix or object handle for {@code rhs}; right-hand-side matrix of the linear system
     * @param dst_nativeObj native matrix or object handle for {@code dst}; destination matrix that receives the result
     */
    private static native void SVBackSubst_0(
            long w_nativeObj,
            long u_nativeObj,
            long vt_nativeObj,
            long rhs_nativeObj,
            long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#Mahalanobis(Mat,Mat,Mat)}.
     *
     * @param v1_nativeObj     native matrix or object handle for {@code v1}; first 1D input vector
     * @param v2_nativeObj     native matrix or object handle for {@code v2}; second 1D input vector
     * @param icovar_nativeObj native matrix or object handle for {@code icovar}; inverse covariance matrix
     * @return the Mahalanobis distance between the two vectors
     */
    private static native double Mahalanobis_0(long v1_nativeObj, long v2_nativeObj, long icovar_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#dft(Mat,Mat,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array that could be real or complex
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array whose size and type depends on
     *                      the flags
     * @param flags         transformation flags, representing a combination of the DftFlags
     * @param nonzeroRows   when the parameter is not zero, the function assumes that only the first nonzeroRows rows of
     *                      the input array (DFT_INVERSE is not set) or only the first nonzeroRows of the output array
     *                      (DFT_INVERSE is set) contain non-zeros, thus, the function can handle the rest of the rows
     *                      more efficiently and save some time; this technique is very useful for calculating array
     *                      cross-correlation or convolution using DFT
     */
    private static native void dft_0(long src_nativeObj, long dst_nativeObj, int flags, int nonzeroRows);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#dft(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array that could be real or complex
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array whose size and type depends on
     *                      the flags
     * @param flags         transformation flags, representing a combination of the DftFlags
     */
    private static native void dft_1(long src_nativeObj, long dst_nativeObj, int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#dft(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array that could be real or complex
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array whose size and type depends on
     *                      the flags
     */
    private static native void dft_2(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#idft(Mat,Mat,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input floating-point real or complex array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array whose size and type depend on
     *                      the flags
     * @param flags         operation flags (see dft and DftFlags)
     * @param nonzeroRows   number of dst rows to process; the rest of the rows have undefined content (see the
     *                      convolution sample in dft description
     */
    private static native void idft_0(long src_nativeObj, long dst_nativeObj, int flags, int nonzeroRows);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#idft(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input floating-point real or complex array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array whose size and type depend on
     *                      the flags
     * @param flags         operation flags (see dft and DftFlags)
     */
    private static native void idft_1(long src_nativeObj, long dst_nativeObj, int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#idft(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input floating-point real or complex array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array whose size and type depend on
     *                      the flags
     */
    private static native void idft_2(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#dct(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input floating-point array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size and type as
     *                      src
     * @param flags         transformation flags as a combination of DftFlags (DCT_*)
     */
    private static native void dct_0(long src_nativeObj, long dst_nativeObj, int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#dct(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input floating-point array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size and type as
     *                      src
     */
    private static native void dct_1(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#idct(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input floating-point single-channel array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size and type as
     *                      src
     * @param flags         operation flags
     */
    private static native void idct_0(long src_nativeObj, long dst_nativeObj, int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#idct(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input floating-point single-channel array
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size and type as
     *                      src
     */
    private static native void idct_1(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#mulSpectrums(Mat,Mat,Mat,int,boolean)}.
     *
     * @param a_nativeObj native matrix or object handle for {@code a}; first input array
     * @param b_nativeObj native matrix or object handle for {@code b}; second input array of the same size and type as
     *                    src1
     * @param c_nativeObj native matrix or object handle for {@code c}; output array of the same size and type as src1
     * @param flags       operation flags; currently, the only supported flag is DFT_ROWS, which indicates that each row
     *                    of src1 and src2 is an independent 1D Fourier spectrum. If you do not want to use this flag,
     *                    then simply add a 0 as value
     * @param conjB       optional flag that conjugates the second input array before the multiplication (true) or not
     *                    (false)
     */
    private static native void mulSpectrums_0(
            long a_nativeObj,
            long b_nativeObj,
            long c_nativeObj,
            int flags,
            boolean conjB);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#mulSpectrums(Mat,Mat,Mat,int)}.
     *
     * @param a_nativeObj native matrix or object handle for {@code a}; first input array
     * @param b_nativeObj native matrix or object handle for {@code b}; second input array of the same size and type as
     *                    src1
     * @param c_nativeObj native matrix or object handle for {@code c}; output array of the same size and type as src1
     * @param flags       operation flags; currently, the only supported flag is DFT_ROWS, which indicates that each row
     *                    of src1 and src2 is an independent 1D Fourier spectrum. If you do not want to use this flag,
     *                    then simply add a 0 as value
     */
    private static native void mulSpectrums_1(long a_nativeObj, long b_nativeObj, long c_nativeObj, int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#divSpectrums(Mat,Mat,Mat,int,boolean)}.
     *
     * @param a_nativeObj native matrix or object handle for {@code a}; first input array
     * @param b_nativeObj native matrix or object handle for {@code b}; second input array of the same size and type as
     *                    src1
     * @param c_nativeObj native matrix or object handle for {@code c}; output array of the same size and type as src1
     * @param flags       operation flags; currently, the only supported flag is DFT_ROWS, which indicates that each row
     *                    of src1 and src2 is an independent 1D Fourier spectrum. If you do not want to use this flag,
     *                    then simply add a 0 as value
     * @param conjB       optional flag that conjugates the second input array before the multiplication (true) or not
     *                    (false)
     */
    private static native void divSpectrums_0(
            long a_nativeObj,
            long b_nativeObj,
            long c_nativeObj,
            int flags,
            boolean conjB);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#divSpectrums(Mat,Mat,Mat,int)}.
     *
     * @param a_nativeObj native matrix or object handle for {@code a}; first input array
     * @param b_nativeObj native matrix or object handle for {@code b}; second input array of the same size and type as
     *                    src1
     * @param c_nativeObj native matrix or object handle for {@code c}; output array of the same size and type as src1
     * @param flags       operation flags; currently, the only supported flag is DFT_ROWS, which indicates that each row
     *                    of src1 and src2 is an independent 1D Fourier spectrum. If you do not want to use this flag,
     *                    then simply add a 0 as value
     */
    private static native void divSpectrums_1(long a_nativeObj, long b_nativeObj, long c_nativeObj, int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#getOptimalDFTSize(int)}.
     *
     * @param vecsize vector size
     * @return the smallest supported efficient transform length at least as large as vecsize, or -1 if no length can be
     *         represented
     */
    private static native int getOptimalDFTSize_0(int vecsize);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#setRNGSeed(int)}.
     *
     * @param seed new state for default random number generator
     */
    private static native void setRNGSeed_0(int seed);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#randu(Mat,double,double)}.
     *
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of random numbers; the array
     *                      must be pre-allocated
     * @param low           inclusive lower boundary of the generated random numbers
     * @param high          exclusive upper boundary of the generated random numbers
     */
    private static native void randu_0(long dst_nativeObj, double low, double high);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#randn(Mat,double,double)}.
     *
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of random numbers; the array
     *                      must be pre-allocated and have 1 to 4 channels
     * @param mean          mean value (expectation) of the generated random numbers
     * @param stddev        standard deviation of the generated random numbers; it can be either a vector (in which case
     *                      a diagonal standard deviation matrix is assumed) or a square matrix
     */
    private static native void randn_0(long dst_nativeObj, double mean, double stddev);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#randShuffle(Mat,double)}.
     *
     * @param dst_nativeObj native matrix or object handle for {@code dst}; input/output numerical 1D array
     * @param iterFactor    scale factor that determines the number of random swap operations (see the details below).
     *                      instead
     */
    private static native void randShuffle_0(long dst_nativeObj, double iterFactor);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#randShuffle(Mat)}.
     *
     * @param dst_nativeObj native matrix or object handle for {@code dst}; input/output numerical 1D array
     */
    private static native void randShuffle_2(long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#kmeans(Mat,int,Mat,TermCriteria,int,int,Mat)}.
     *
     * @param data_nativeObj       native matrix or object handle for {@code data}; Data for clustering. An array of
     *                             N-Dimensional points with float coordinates is needed. Examples of this array can be:
     *                             Mat points(count, 2, CV_32F); Mat points(count, 1, CV_32FC2); Mat points(1, count,
     *                             CV_32FC2); vector&lt;Point2f&gt; points(sampleCount);
     * @param K                    Number of clusters to split the set by
     * @param bestLabels_nativeObj native matrix or object handle for {@code bestLabels}; Input/output integer array
     *                             that stores the cluster indices for every sample
     * @param criteria_type        {@code criteria.type} component of The algorithm termination criteria, that is, the
     *                             maximum number of iterations and/or the desired accuracy. The accuracy is specified
     *                             as criteria.epsilon. As soon as each of the cluster centers moves by less than
     *                             criteria.epsilon on some iteration, the algorithm stops
     * @param criteria_maxCount    {@code criteria.maxCount} component of The algorithm termination criteria, that is,
     *                             the maximum number of iterations and/or the desired accuracy. The accuracy is
     *                             specified as criteria.epsilon. As soon as each of the cluster centers moves by less
     *                             than criteria.epsilon on some iteration, the algorithm stops
     * @param criteria_epsilon     {@code criteria.epsilon} component of The algorithm termination criteria, that is,
     *                             the maximum number of iterations and/or the desired accuracy. The accuracy is
     *                             specified as criteria.epsilon. As soon as each of the cluster centers moves by less
     *                             than criteria.epsilon on some iteration, the algorithm stops
     * @param attempts             Flag to specify the number of times the algorithm is executed using different initial
     *                             labellings. The algorithm returns the labels that yield the best compactness (see the
     *                             last function parameter)
     * @param flags                Flag that can take values of KmeansFlags
     * @param centers_nativeObj    native matrix or object handle for {@code centers}; Output matrix of the cluster
     *                             centers, one row per each cluster center
     * @return the sum of squared distances from each sample to its assigned cluster center
     */
    private static native double kmeans_0(
            long data_nativeObj,
            int K,
            long bestLabels_nativeObj,
            int criteria_type,
            int criteria_maxCount,
            double criteria_epsilon,
            int attempts,
            int flags,
            long centers_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#kmeans(Mat,int,Mat,TermCriteria,int,int)}.
     *
     * @param data_nativeObj       native matrix or object handle for {@code data}; Data for clustering. An array of
     *                             N-Dimensional points with float coordinates is needed. Examples of this array can be:
     *                             Mat points(count, 2, CV_32F); Mat points(count, 1, CV_32FC2); Mat points(1, count,
     *                             CV_32FC2); vector&lt;Point2f&gt; points(sampleCount);
     * @param K                    Number of clusters to split the set by
     * @param bestLabels_nativeObj native matrix or object handle for {@code bestLabels}; Input/output integer array
     *                             that stores the cluster indices for every sample
     * @param criteria_type        {@code criteria.type} component of The algorithm termination criteria, that is, the
     *                             maximum number of iterations and/or the desired accuracy. The accuracy is specified
     *                             as criteria.epsilon. As soon as each of the cluster centers moves by less than
     *                             criteria.epsilon on some iteration, the algorithm stops
     * @param criteria_maxCount    {@code criteria.maxCount} component of The algorithm termination criteria, that is,
     *                             the maximum number of iterations and/or the desired accuracy. The accuracy is
     *                             specified as criteria.epsilon. As soon as each of the cluster centers moves by less
     *                             than criteria.epsilon on some iteration, the algorithm stops
     * @param criteria_epsilon     {@code criteria.epsilon} component of The algorithm termination criteria, that is,
     *                             the maximum number of iterations and/or the desired accuracy. The accuracy is
     *                             specified as criteria.epsilon. As soon as each of the cluster centers moves by less
     *                             than criteria.epsilon on some iteration, the algorithm stops
     * @param attempts             Flag to specify the number of times the algorithm is executed using different initial
     *                             labellings. The algorithm returns the labels that yield the best compactness (see the
     *                             last function parameter)
     * @param flags                Flag that can take values of KmeansFlags
     * @return the sum of squared distances from each sample to its assigned cluster center
     */
    private static native double kmeans_1(
            long data_nativeObj,
            int K,
            long bestLabels_nativeObj,
            int criteria_type,
            int criteria_maxCount,
            double criteria_epsilon,
            int attempts,
            int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#solveLP(Mat,Mat,Mat,double)}.
     *
     * @param Func_nativeObj   native matrix or object handle for {@code Func}; coefficient vector of the linear
     *                         objective
     * @param Constr_nativeObj native matrix or object handle for {@code Constr}; m-by-n+1 matrix, whose rightmost
     *                         column corresponds to b in formulation above and the remaining to A. It should contain
     *                         32- or 64-bit floating point numbers
     * @param z_nativeObj      native matrix or object handle for {@code z}; The solution will be returned here as a
     *                         column-vector - it corresponds to c in the formulation above. It will contain 64-bit
     *                         floating point numbers
     * @param constr_eps       allowed numeric disparity for constraints
     * @return the linear-program solver status identified by the SOLVELP constants
     */
    private static native int solveLP_0(
            long Func_nativeObj,
            long Constr_nativeObj,
            long z_nativeObj,
            double constr_eps);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#solveLP(Mat,Mat,Mat)}.
     *
     * @param Func_nativeObj   native matrix or object handle for {@code Func}; coefficient vector of the linear
     *                         objective
     * @param Constr_nativeObj native matrix or object handle for {@code Constr}; m-by-n+1 matrix, whose rightmost
     *                         column corresponds to b in formulation above and the remaining to A. It should contain
     *                         32- or 64-bit floating point numbers
     * @param z_nativeObj      native matrix or object handle for {@code z}; The solution will be returned here as a
     *                         column-vector - it corresponds to c in the formulation above. It will contain 64-bit
     *                         floating point numbers
     * @return the linear-program solver status identified by the SOLVELP constants
     */
    private static native int solveLP_1(long Func_nativeObj, long Constr_nativeObj, long z_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#linearAssignment(Mat,MatOfInt,double)}.
     *
     * @param cost_nativeObj           native matrix or object handle for {@code cost}; M-by-N cost matrix, CV_32FC1,
     *                                 CV_64FC1 or CV_32SC1. Must not be empty. Infinite and NaN cells are forbidden
     *                                 pairs; an integer matrix has none, so only costThreshold can forbid a pair there
     * @param assignment_mat_nativeObj native matrix or object handle for {@code assignment_mat}; Output vector of size
     *                                 M. Element i is the column matched to row i, or -1
     * @param costThreshold            Maximum cost of a pair, and the price of each pair left unmade
     * @return Sum of cost over the matched pairs only, without the unassignment term; 0 if no pair was made. Note:
     *         Implemented from which extends to rectangular matrices
     */
    private static native double linearAssignment_0(
            long cost_nativeObj,
            long assignment_mat_nativeObj,
            double costThreshold);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#linearAssignment(Mat,MatOfInt)}.
     *
     * @param cost_nativeObj           native matrix or object handle for {@code cost}; M-by-N cost matrix, CV_32FC1,
     *                                 CV_64FC1 or CV_32SC1. Must not be empty. Infinite and NaN cells are forbidden
     *                                 pairs; an integer matrix has none, so only costThreshold can forbid a pair there
     * @param assignment_mat_nativeObj native matrix or object handle for {@code assignment_mat}; Output vector of size
     *                                 M. Element i is the column matched to row i, or -1
     * @return Sum of cost over the matched pairs only, without the unassignment term; 0 if no pair was made. Note:
     *         Implemented from which extends to rectangular matrices
     */
    private static native double linearAssignment_1(long cost_nativeObj, long assignment_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#setNumThreads(int)}.
     *
     * @param nthreads Number of threads used by OpenCV
     */
    private static native void setNumThreads_0(int nthreads);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#getNumThreads()}.
     *
     * @return the thread-count setting reported by the active parallel backend
     */
    private static native int getNumThreads_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#getThreadNum()}.
     *
     * @return the calling worker's index reported by the active parallel backend
     */
    private static native int getThreadNum_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#getBuildInformation()}.
     *
     * @return the build configuration of the linked OpenCV engine
     */
    private static native String getBuildInformation_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#getVersionString()}.
     *
     * @return the version string reported by the linked native engine
     */
    private static native String getVersionString_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#getVersionMajor()}.
     *
     * @return the linked native engine's major version
     */
    private static native int getVersionMajor_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#getVersionMinor()}.
     *
     * @return the linked native engine's minor version
     */
    private static native int getVersionMinor_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#getVersionRevision()}.
     *
     * @return the linked native engine's patch version
     */
    private static native int getVersionRevision_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#getTickCount()}.
     *
     * @return the platform timer's current tick counter
     */
    private static native long getTickCount_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#getTickFrequency()}.
     *
     * @return the number of platform timer ticks per second
     */
    private static native double getTickFrequency_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#getCPUTickCount()}.
     *
     * @return the architecture-dependent CPU tick counter
     */
    private static native long getCPUTickCount_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#checkHardwareSupport(int)}.
     *
     * @param feature The feature of interest, one of CpuFeatures
     * @return true if the requested CPU feature is enabled for the current runtime
     */
    private static native boolean checkHardwareSupport_0(int feature);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#getHardwareFeatureName(int)}.
     *
     * @param feature CPU feature identifier to query
     * @return the name associated with the feature identifier
     */
    private static native String getHardwareFeatureName_0(int feature);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#getCPUFeaturesLine()}.
     *
     * @return CPU feature names and their runtime availability indicators
     */
    private static native String getCPUFeaturesLine_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#getNumberOfCPUs()}.
     *
     * @return the logical CPU count available to this runtime
     */
    private static native int getNumberOfCPUs_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#getDefaultAlgorithmHint()}.
     *
     * @return the default algorithm accuracy/performance hint selected for this engine build
     */
    private static native int getDefaultAlgorithmHint_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#setUseOptimized(boolean)}.
     *
     * @param onoff The boolean flag specifying whether the optimized code should be used (onoff=true) or not
     *              (onoff=false)
     */
    private static native void setUseOptimized_0(boolean onoff);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#useOptimized()}.
     *
     * @return true if optimized runtime implementations are enabled
     */
    private static native boolean useOptimized_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#findFile(String,boolean,boolean)}.
     *
     * @param relative_path Relative path to data file
     * @param required      Specify "file not found" handling. If true, function prints information message and raises
     *                      Exception. If false, function returns empty result
     * @param silentMode    Disables messages
     * @return the located path, or an empty string when no path is found and required is false
     */
    private static native String findFile_0(String relative_path, boolean required, boolean silentMode);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#findFile(String,boolean)}.
     *
     * @param relative_path Relative path to data file
     * @param required      Specify "file not found" handling. If true, function prints information message and raises
     *                      Exception. If false, function returns empty result
     * @return the located path, or an empty string when no path is found and required is false
     */
    private static native String findFile_1(String relative_path, boolean required);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#findFile(String)}.
     *
     * @param relative_path Relative path to data file
     * @return the located path, or an empty string when no path is found and required is false
     */
    private static native String findFile_2(String relative_path);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#findFileOrKeep(String,boolean)}.
     *
     * @param relative_path relative path to locate
     * @param silentMode    whether file-search diagnostics are suppressed
     * @return the located file path, or the original relative path when no file is found
     */
    private static native String findFileOrKeep_0(String relative_path, boolean silentMode);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#findFileOrKeep(String)}.
     *
     * @param relative_path relative path to locate
     * @return the located file path, or the original relative path when no file is found
     */
    private static native String findFileOrKeep_1(String relative_path);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#addSamplesDataSearchPath(String)}.
     *
     * @param path Path to used samples data
     */
    private static native void addSamplesDataSearchPath_0(String path);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#addSamplesDataSearchSubDirectory(String)}.
     *
     * @param subdir samples data sub directory
     */
    private static native void addSamplesDataSearchSubDirectory_0(String subdir);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#setErrorVerbosity(boolean)}.
     *
     * @param verbose whether detailed error reporting is enabled
     */
    private static native void setErrorVerbosity_0(boolean verbose);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#add(Mat,Scalar,Mat,Mat,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_val0      {@code src2.val[0]} component of second input array or a scalar
     * @param src2_val1      {@code src2.val[1]} component of second input array or a scalar
     * @param src2_val2      {@code src2.val[2]} component of second input array or a scalar
     * @param src2_val3      {@code src2.val[3]} component of second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       number of channels as the input array(s); the depth is defined by dtype or src1/src2
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional operation mask - CV_8U, CV_8S or
     *                       CV_Bool single channel array, that specifies elements of the output array to be changed
     * @param dtype          optional depth of the output array (see the discussion below)
     */
    private static native void add_3(
            long src1_nativeObj,
            double src2_val0,
            double src2_val1,
            double src2_val2,
            double src2_val3,
            long dst_nativeObj,
            long mask_nativeObj,
            int dtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#add(Mat,Scalar,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_val0      {@code src2.val[0]} component of second input array or a scalar
     * @param src2_val1      {@code src2.val[1]} component of second input array or a scalar
     * @param src2_val2      {@code src2.val[2]} component of second input array or a scalar
     * @param src2_val3      {@code src2.val[3]} component of second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       number of channels as the input array(s); the depth is defined by dtype or src1/src2
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional operation mask - CV_8U, CV_8S or
     *                       CV_Bool single channel array, that specifies elements of the output array to be changed
     */
    private static native void add_4(
            long src1_nativeObj,
            double src2_val0,
            double src2_val1,
            double src2_val2,
            double src2_val3,
            long dst_nativeObj,
            long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#add(Mat,Scalar,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_val0      {@code src2.val[0]} component of second input array or a scalar
     * @param src2_val1      {@code src2.val[1]} component of second input array or a scalar
     * @param src2_val2      {@code src2.val[2]} component of second input array or a scalar
     * @param src2_val3      {@code src2.val[3]} component of second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       number of channels as the input array(s); the depth is defined by dtype or src1/src2
     */
    private static native void add_5(
            long src1_nativeObj,
            double src2_val0,
            double src2_val1,
            double src2_val2,
            double src2_val3,
            long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#subtract(Mat,Scalar,Mat,Mat,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_val0      {@code src2.val[0]} component of second input array or a scalar
     * @param src2_val1      {@code src2.val[1]} component of second input array or a scalar
     * @param src2_val2      {@code src2.val[2]} component of second input array or a scalar
     * @param src2_val3      {@code src2.val[3]} component of second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and the same
     *                       number of channels as the input array
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional operation mask; this is CV_8U,
     *                       CV8S or CV_Bool single channel array that specifies elements of the output array to be
     *                       changed
     * @param dtype          optional depth of the output array
     */
    private static native void subtract_3(
            long src1_nativeObj,
            double src2_val0,
            double src2_val1,
            double src2_val2,
            double src2_val3,
            long dst_nativeObj,
            long mask_nativeObj,
            int dtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#subtract(Mat,Scalar,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_val0      {@code src2.val[0]} component of second input array or a scalar
     * @param src2_val1      {@code src2.val[1]} component of second input array or a scalar
     * @param src2_val2      {@code src2.val[2]} component of second input array or a scalar
     * @param src2_val3      {@code src2.val[3]} component of second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and the same
     *                       number of channels as the input array
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional operation mask; this is CV_8U,
     *                       CV8S or CV_Bool single channel array that specifies elements of the output array to be
     *                       changed
     */
    private static native void subtract_4(
            long src1_nativeObj,
            double src2_val0,
            double src2_val1,
            double src2_val2,
            double src2_val3,
            long dst_nativeObj,
            long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#subtract(Mat,Scalar,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_val0      {@code src2.val[0]} component of second input array or a scalar
     * @param src2_val1      {@code src2.val[1]} component of second input array or a scalar
     * @param src2_val2      {@code src2.val[2]} component of second input array or a scalar
     * @param src2_val3      {@code src2.val[3]} component of second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and the same
     *                       number of channels as the input array
     */
    private static native void subtract_5(
            long src1_nativeObj,
            double src2_val0,
            double src2_val1,
            double src2_val2,
            double src2_val3,
            long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#multiply(Mat,Scalar,Mat,double,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_val0      {@code src2.val[0]} component of second input array of the same size and the same type as
     *                       src1
     * @param src2_val1      {@code src2.val[1]} component of second input array of the same size and the same type as
     *                       src1
     * @param src2_val2      {@code src2.val[2]} component of second input array of the same size and the same type as
     *                       src1
     * @param src2_val3      {@code src2.val[3]} component of second input array of the same size and the same type as
     *                       src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src1
     * @param scale          optional scale factor
     * @param dtype          optional depth of the output array
     */
    private static native void multiply_3(
            long src1_nativeObj,
            double src2_val0,
            double src2_val1,
            double src2_val2,
            double src2_val3,
            long dst_nativeObj,
            double scale,
            int dtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#multiply(Mat,Scalar,Mat,double)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_val0      {@code src2.val[0]} component of second input array of the same size and the same type as
     *                       src1
     * @param src2_val1      {@code src2.val[1]} component of second input array of the same size and the same type as
     *                       src1
     * @param src2_val2      {@code src2.val[2]} component of second input array of the same size and the same type as
     *                       src1
     * @param src2_val3      {@code src2.val[3]} component of second input array of the same size and the same type as
     *                       src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src1
     * @param scale          optional scale factor
     */
    private static native void multiply_4(
            long src1_nativeObj,
            double src2_val0,
            double src2_val1,
            double src2_val2,
            double src2_val3,
            long dst_nativeObj,
            double scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#multiply(Mat,Scalar,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_val0      {@code src2.val[0]} component of second input array of the same size and the same type as
     *                       src1
     * @param src2_val1      {@code src2.val[1]} component of second input array of the same size and the same type as
     *                       src1
     * @param src2_val2      {@code src2.val[2]} component of second input array of the same size and the same type as
     *                       src1
     * @param src2_val3      {@code src2.val[3]} component of second input array of the same size and the same type as
     *                       src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src1
     */
    private static native void multiply_5(
            long src1_nativeObj,
            double src2_val0,
            double src2_val1,
            double src2_val2,
            double src2_val3,
            long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#divide(Mat,Scalar,Mat,double,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_val0      {@code src2.val[0]} component of second input array of the same size and type as src1
     * @param src2_val1      {@code src2.val[1]} component of second input array of the same size and type as src1
     * @param src2_val2      {@code src2.val[2]} component of second input array of the same size and type as src1
     * @param src2_val3      {@code src2.val[3]} component of second input array of the same size and type as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src2
     * @param scale          scalar factor
     * @param dtype          optional depth of the output array; if -1, dst will have depth src2.depth(), but in case of
     *                       an array-by-array division, you can only pass -1 when src1.depth()==src2.depth()
     */
    private static native void divide_5(
            long src1_nativeObj,
            double src2_val0,
            double src2_val1,
            double src2_val2,
            double src2_val3,
            long dst_nativeObj,
            double scale,
            int dtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#divide(Mat,Scalar,Mat,double)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_val0      {@code src2.val[0]} component of second input array of the same size and type as src1
     * @param src2_val1      {@code src2.val[1]} component of second input array of the same size and type as src1
     * @param src2_val2      {@code src2.val[2]} component of second input array of the same size and type as src1
     * @param src2_val3      {@code src2.val[3]} component of second input array of the same size and type as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src2
     * @param scale          scalar factor
     */
    private static native void divide_6(
            long src1_nativeObj,
            double src2_val0,
            double src2_val1,
            double src2_val2,
            double src2_val3,
            long dst_nativeObj,
            double scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#divide(Mat,Scalar,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_val0      {@code src2.val[0]} component of second input array of the same size and type as src1
     * @param src2_val1      {@code src2.val[1]} component of second input array of the same size and type as src1
     * @param src2_val2      {@code src2.val[2]} component of second input array of the same size and type as src1
     * @param src2_val3      {@code src2.val[3]} component of second input array of the same size and type as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src2
     */
    private static native void divide_7(
            long src1_nativeObj,
            double src2_val0,
            double src2_val1,
            double src2_val2,
            double src2_val3,
            long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#absdiff(Mat,Scalar,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar
     * @param src2_val0      {@code src2.val[0]} component of second input array or a scalar
     * @param src2_val1      {@code src2.val[1]} component of second input array or a scalar
     * @param src2_val2      {@code src2.val[2]} component of second input array or a scalar
     * @param src2_val3      {@code src2.val[3]} component of second input array or a scalar
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array that has the same size and
     *                       type as input arrays
     */
    private static native void absdiff_1(
            long src1_nativeObj,
            double src2_val0,
            double src2_val1,
            double src2_val2,
            double src2_val3,
            long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#compare(Mat,Scalar,Mat,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array or a scalar; when it is
     *                       an array, it must have a single channel
     * @param src2_val0      {@code src2.val[0]} component of second input array or a scalar; when it is an array, it
     *                       must have a single channel
     * @param src2_val1      {@code src2.val[1]} component of second input array or a scalar; when it is an array, it
     *                       must have a single channel
     * @param src2_val2      {@code src2.val[2]} component of second input array or a scalar; when it is an array, it
     *                       must have a single channel
     * @param src2_val3      {@code src2.val[3]} component of second input array or a scalar; when it is an array, it
     *                       must have a single channel
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of type ref CV_8U that has the
     *                       same size and the same number of channels as the input arrays
     * @param cmpop          a flag, that specifies correspondence between the arrays (CmpTypes)
     */
    private static native void compare_1(
            long src1_nativeObj,
            double src2_val0,
            double src2_val1,
            double src2_val2,
            double src2_val3,
            long dst_nativeObj,
            int cmpop);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#min(Mat,Scalar,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_val0      {@code src2.val[0]} component of second input array of the same size and type as src1
     * @param src2_val1      {@code src2.val[1]} component of second input array of the same size and type as src1
     * @param src2_val2      {@code src2.val[2]} component of second input array of the same size and type as src1
     * @param src2_val3      {@code src2.val[3]} component of second input array of the same size and type as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src1
     */
    private static native void min_1(
            long src1_nativeObj,
            double src2_val0,
            double src2_val1,
            double src2_val2,
            double src2_val3,
            long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#max(Mat,Scalar,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; first input array
     * @param src2_val0      {@code src2.val[0]} component of second input array of the same size and type as src1
     * @param src2_val1      {@code src2.val[1]} component of second input array of the same size and type as src1
     * @param src2_val2      {@code src2.val[2]} component of second input array of the same size and type as src1
     * @param src2_val3      {@code src2.val[3]} component of second input array of the same size and type as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type as
     *                       src1
     */
    private static native void max_1(
            long src1_nativeObj,
            double src2_val0,
            double src2_val1,
            double src2_val2,
            double src2_val3,
            long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Core#minMaxLoc(Mat,Mat)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; source matrix
     * @param mask_nativeObj native mask handle, or zero when the Java caller supplied a null mask
     * @return packed numeric fields used by the Java wrapper to construct {@code MinMaxLocResult}
     */
    private static native double[] n_minMaxLocManual(long src_nativeObj, long mask_nativeObj);

}
