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
package org.miaixz.bus.opencv.geometry;

import java.util.List;

import org.miaixz.bus.opencv.core.Mat;
import org.miaixz.bus.opencv.core.MatOfDouble;
import org.miaixz.bus.opencv.core.MatOfInt;
import org.miaixz.bus.opencv.core.MatOfInt4;
import org.miaixz.bus.opencv.core.MatOfPoint;
import org.miaixz.bus.opencv.core.MatOfPoint2f;
import org.miaixz.bus.opencv.core.MatOfPoint3f;
import org.miaixz.bus.opencv.core.Point;
import org.miaixz.bus.opencv.core.Rect;
import org.miaixz.bus.opencv.core.RotatedRect;
import org.miaixz.bus.opencv.core.Size;
import org.miaixz.bus.opencv.core.TermCriteria;
import org.miaixz.bus.opencv.utils.Converters;

/**
 * Geometric transformations, camera calibration, robust model estimation, and optimization utilities.
 */
public class Geometry {

    /**
     * Creates a Java API container. The utility operations are available through static methods.
     */
    public Geometry() {
        // No initialization required.
    }

    /**
     * Robust-estimation method based on the least median of squared residuals.
     */
    public static final int LMEDS = 4;

    /**
     * RANSAC algorithm.
     */
    public static final int RANSAC = 8;

    /**
     * RHO algorithm.
     */
    public static final int RHO = 16;

    /**
     * USAC algorithm, default settings.
     */
    public static final int USAC_DEFAULT = 32;

    /**
     * USAC, parallel version.
     */
    public static final int USAC_PARALLEL = 33;

    /**
     * USAC, fundamental matrix 8 points.
     */
    public static final int USAC_FM_8PTS = 34;

    /**
     * USAC, fast settings.
     */
    public static final int USAC_FAST = 35;

    /**
     * USAC, accurate settings.
     */
    public static final int USAC_ACCURATE = 36;

    /**
     * USAC, sorted points, runs PROSAC.
     */
    public static final int USAC_PROSAC = 37;

    /**
     * USAC, runs MAGSAC++.
     */
    public static final int USAC_MAGSAC = 38;

    /**
     * Estimate a fundamental matrix with the seven-point algorithm.
     */
    public static final int FM_7POINT = 1;

    /**
     * 8-point algorithm.
     */
    public static final int FM_8POINT = 2;

    /**
     * Least-median algorithm. 7-point algorithm is used.
     */
    public static final int FM_LMEDS = 4;

    /**
     * RANSAC algorithm. It needs at least 15 points. 7-point algorithm is used.
     */
    public static final int FM_RANSAC = 8;

    /**
     * User defined distance.
     */
    public static final int DIST_USER = -1;

    /**
     * Distance = |x1-x2| + |y1-y2|.
     */
    public static final int DIST_L1 = 1;

    /**
     * The simple euclidean distance.
     */
    public static final int DIST_L2 = 2;

    /**
     * Distance = max(|x1-x2|,|y1-y2|).
     */
    public static final int DIST_C = 3;

    /**
     * L1-L2 metric: distance = 2(sqrt(1+x*x/2) - 1)).
     */
    public static final int DIST_L12 = 4;

    /**
     * Distance = c^2(|x|/c-log(1+|x|/c)), c = 1.3998.
     */
    public static final int DIST_FAIR = 5;

    /**
     * Distance = c^2/2(1-exp(-(x/c)^2)), c = 2.9846.
     */
    public static final int DIST_WELSCH = 6;

    /**
     * Distance = |x|&lt;c ? x^2/2 : c(|x|-c/2), c=1.345.
     */
    public static final int DIST_HUBER = 7;

    /**
     * Disable local optimization of the robust model.
     */
    public static final int LOCAL_OPTIM_NULL = 0;

    /**
     * Refine robust models through inner local optimization.
     */
    public static final int LOCAL_OPTIM_INNER_LO = 1;

    /**
     * Use both inner and iterative local optimization.
     */
    public static final int LOCAL_OPTIM_INNER_AND_ITER_LO = 2;

    /**
     * Refine the robust model with graph-cut local optimization.
     */
    public static final int LOCAL_OPTIM_GC = 3;

    /**
     * Refine the robust model using sigma-consensus optimization.
     */
    public static final int LOCAL_OPTIM_SIGMA = 4;

    /**
     * Construct a minimum spanning tree using Prim's algorithm.
     */
    public static final int MST_PRIM = 0;

    /**
     * Construct a minimum spanning tree using Kruskal's algorithm.
     */
    public static final int MST_KRUSKAL = 1;

    /**
     * Optimization matrix representation selector for auto storage.
     */
    public static final int MatrixType_AUTO = 0;

    /**
     * Optimization matrix representation selector for dense storage.
     */
    public static final int MatrixType_DENSE = 1;

    /**
     * Optimization matrix representation selector for sparse storage.
     */
    public static final int MatrixType_SPARSE = 2;

    /**
     * Find neighboring samples using a FLANN k-nearest-neighbor search.
     */
    public static final int NEIGH_FLANN_KNN = 0;

    /**
     * Find neighboring samples using a spatial grid.
     */
    public static final int NEIGH_GRID = 1;

    /**
     * Find neighboring samples using a FLANN radius search.
     */
    public static final int NEIGH_FLANN_RADIUS = 2;

    /**
     * Disable the final robust-model polishing step.
     */
    public static final int NONE_POLISHER = 0;

    /**
     * Refine the final robust model by least-squares fitting.
     */
    public static final int LSQ_POLISHER = 1;

    /**
     * Refine the final robust model with MAGSAC polishing.
     */
    public static final int MAGSAC = 2;

    /**
     * Refine the final robust model using covariance-based polishing.
     */
    public static final int COV_POLISHER = 3;

    /**
     * No intersection.
     */
    public static final int INTERSECT_NONE = 0;

    /**
     * There is a partial intersection.
     */
    public static final int INTERSECT_PARTIAL = 1;

    /**
     * One of the rectangle is fully enclosed in the other.
     */
    public static final int INTERSECT_FULL = 2;

    /**
     * Use random sample consensus to estimate the segmentation model.
     */
    public static final int SAC_METHOD_RANSAC = 0;

    /**
     * Fit a plane model during sample-consensus segmentation.
     */
    public static final int SAC_MODEL_PLANE = 0;

    /**
     * Fit a sphere model during sample-consensus segmentation.
     */
    public static final int SAC_MODEL_SPHERE = 1;

    /**
     * Choose robust-estimation samples uniformly from all correspondences.
     */
    public static final int SAMPLING_UNIFORM = 0;

    /**
     * Choose spatially local samples with progressively expanding neighborhoods.
     */
    public static final int SAMPLING_PROGRESSIVE_NAPSAC = 1;

    /**
     * Choose robust-estimation samples from spatial neighborhoods.
     */
    public static final int SAMPLING_NAPSAC = 2;

    /**
     * Choose samples progressively from correspondences ordered by quality.
     */
    public static final int SAMPLING_PROSAC = 3;

    /**
     * Score robust models by their number of inliers.
     */
    public static final int SCORE_METHOD_RANSAC = 0;

    /**
     * Score robust models using truncated squared residuals.
     */
    public static final int SCORE_METHOD_MSAC = 1;

    /**
     * Score robust models using MAGSAC noise-scale marginalization.
     */
    public static final int SCORE_METHOD_MAGSAC = 2;

    /**
     * Score robust models by the median of their squared residuals.
     */
    public static final int SCORE_METHOD_LMEDS = 3;

    /**
     * Estimate camera pose by iterative reprojection-error minimization.
     */
    public static final int SOLVEPNP_ITERATIVE = 0;

    /**
     * Estimate camera pose with the efficient perspective-n-point method.
     */
    public static final int SOLVEPNP_EPNP = 1;

    /**
     * Estimate camera pose with the perspective-three-point method.
     */
    public static final int SOLVEPNP_P3P = 2;

    /**
     * Estimate camera pose with the algebraic perspective-three-point method.
     */
    public static final int SOLVEPNP_AP3P = 3;

    /**
     * Estimate pose for coplanar object points with infinitesimal plane-based pose estimation.
     */
    public static final int SOLVEPNP_IPPE = 4;

    /**
     * Estimate pose for the four ordered corners of a square planar marker.
     */
    public static final int SOLVEPNP_IPPE_SQUARE = 5;

    /**
     * Estimate camera pose with the SQPnP method.
     */
    public static final int SOLVEPNP_SQPNP = 6;

    /**
     * Upper-bound sentinel for camera-pose method identifiers; not a solver selection.
     */
    public static final int SOLVEPNP_MAX_COUNT = 6 + 1;

    /**
     * Optimization variable representation selector for LINEAR.
     */
    public static final int VariableType_LINEAR = 0;

    /**
     * Optimization variable representation selector for SO3.
     */
    public static final int VariableType_SO3 = 1;

    /**
     * Optimization variable representation selector for SE3.
     */
    public static final int VariableType_SE3 = 2;

    /**
     * Estimates a normal and local curvature for each point from the supplied nearest-neighbor information.
     *
     * @param normals          Normal of each point, support vector&lt;Point3f&gt; and Mat of size Nx3
     * @param curvatures       Curvature of each point, support vector&lt;float&gt; and Mat
     * @param input_pts        Original point cloud, support vector&lt;Point3f&gt; and Mat of size Nx3/3xN
     * @param nn_idx           Index information of nearest neighbors of all points. The first nearest neighbor of each
     *                         point is itself. Support vector&lt;vector&lt;int&gt;&gt;, List&lt;Mat&gt; and Mat of size
     *                         NxK. If the information in a row is [0, 2, 1, -5, -1, 4, 7 ... negative number], it will
     *                         use only non-negative indexes until it meets a negative number or bound of this row i.e.
     *                         [0, 2, 1]. If left empty (noArray()), the neighbors are computed internally with a
     *                         kd-tree and max_neighbor_num is used as the number of neighbors k (must be &gt;= 2)
     * @param max_neighbor_num The maximum number of neighbors want to use including itself. Setting to a non-positive
     *                         number or default will use the information from nn_idx. When nn_idx is empty this is the
     *                         number of neighbors k to search for and must be &gt;= 2
     * @throws NullPointerException if normals, curvatures, input_pts is null
     */
    public static void normalEstimate(
            Mat normals,
            Mat curvatures,
            Mat input_pts,
            List<Mat> nn_idx,
            int max_neighbor_num) {
        Mat nn_idx_mat = Converters.vector_Mat_to_Mat(nn_idx);
        normalEstimate_0(
                normals.nativeObj,
                curvatures.nativeObj,
                input_pts.nativeObj,
                nn_idx_mat.nativeObj,
                max_neighbor_num);
    }

    /**
     * Estimates a normal and local curvature for each point from the supplied nearest-neighbor information.
     *
     * @param normals    Normal of each point, support vector&lt;Point3f&gt; and Mat of size Nx3
     * @param curvatures Curvature of each point, support vector&lt;float&gt; and Mat
     * @param input_pts  Original point cloud, support vector&lt;Point3f&gt; and Mat of size Nx3/3xN
     * @param nn_idx     Index information of nearest neighbors of all points. The first nearest neighbor of each point
     *                   is itself. Support vector&lt;vector&lt;int&gt;&gt;, List&lt;Mat&gt; and Mat of size NxK. If the
     *                   information in a row is [0, 2, 1, -5, -1, 4, 7 ... negative number], it will use only
     *                   non-negative indexes until it meets a negative number or bound of this row i.e. [0, 2, 1]. If
     *                   left empty (noArray()), the neighbors are computed internally with a kd-tree and
     *                   max_neighbor_num is used as the number of neighbors k (must be &gt;= 2)
     * @throws NullPointerException if normals, curvatures, input_pts is null
     */
    public static void normalEstimate(Mat normals, Mat curvatures, Mat input_pts, List<Mat> nn_idx) {
        Mat nn_idx_mat = Converters.vector_Mat_to_Mat(nn_idx);
        normalEstimate_1(normals.nativeObj, curvatures.nativeObj, input_pts.nativeObj, nn_idx_mat.nativeObj);
    }

    /**
     * Approximates a polygonal curve(s) with the specified precision. T he function approxPolyDP approximates a curve
     * or a p*olygon with another curve/polygon with less vertices so that the distance between them is less or equal to
     * the specified precision. It uses the Douglas-Peucker algorithm
     * &lt;https://en.wikipedia.org/wiki/Ramer-Douglas-Peucker_algorithm&gt;.
     *
     * @param curve       Input vector of a 2D point stored in vector or Mat
     * @param approxCurve Result of the approximation. The type should match the type of the input curve
     * @param epsilon     Parameter specifying the approximation accuracy. This is the maximum distance between the
     *                    original curve and its approximation
     * @param closed      If true, the approximated curve is closed (its first and last vertices are connected).
     *                    Otherwise, it is not closed
     */
    public static void approxPolyDP(MatOfPoint2f curve, MatOfPoint2f approxCurve, double epsilon, boolean closed) {
        Mat curve_mat = curve;
        Mat approxCurve_mat = approxCurve;
        approxPolyDP_0(curve_mat.nativeObj, approxCurve_mat.nativeObj, epsilon, closed);
    }

    /**
     * Approximates a polygon with a convex hull with a specified accuracy and number of sides. T he approxPolyN
     * function approximates a polygon with *a convex hull so that the difference between the contour area of the
     * original contour and the new polygon is minimal. It uses a greedy algorithm for contracting two vertices into one
     * in such a way that the additional area is minimal. Straight lines formed by each edge of the convex contour are
     * drawn and the areas of the resulting triangles are considered. Each vertex will lie either on the original
     * contour or outside it. The algorithm based on the paper.
     *
     * @param curve              Input vector of a 2D points stored in vector or Mat, points must be float or integer
     * @param approxCurve        Result of the approximation. The type is vector of a 2D point (Point2f or Point) in
     *                           vector or Mat
     * @param nsides             The parameter defines the number of sides of the result polygon
     * @param epsilon_percentage defines the percentage of the maximum of additional area. If it equals -1, it is not
     *                           used. Otherwise algorithm stops if additional area is greater than contourArea(_curve)
     *                           * percentage. If additional area exceeds the limit, algorithm returns as many vertices
     *                           as there were at the moment the limit was exceeded
     * @param ensure_convex      If it is true, algorithm creates a convex hull of input contour. Otherwise input vector
     *                           should be convex
     * @throws NullPointerException if curve, approxCurve is null
     */
    public static void approxPolyN(
            Mat curve,
            Mat approxCurve,
            int nsides,
            float epsilon_percentage,
            boolean ensure_convex) {
        approxPolyN_0(curve.nativeObj, approxCurve.nativeObj, nsides, epsilon_percentage, ensure_convex);
    }

    /**
     * Approximates a polygon with a convex hull with a specified accuracy and number of sides. T he approxPolyN
     * function approximates a polygon with *a convex hull so that the difference between the contour area of the
     * original contour and the new polygon is minimal. It uses a greedy algorithm for contracting two vertices into one
     * in such a way that the additional area is minimal. Straight lines formed by each edge of the convex contour are
     * drawn and the areas of the resulting triangles are considered. Each vertex will lie either on the original
     * contour or outside it. The algorithm based on the paper.
     *
     * @param curve              Input vector of a 2D points stored in vector or Mat, points must be float or integer
     * @param approxCurve        Result of the approximation. The type is vector of a 2D point (Point2f or Point) in
     *                           vector or Mat
     * @param nsides             The parameter defines the number of sides of the result polygon
     * @param epsilon_percentage defines the percentage of the maximum of additional area. If it equals -1, it is not
     *                           used. Otherwise algorithm stops if additional area is greater than contourArea(_curve)
     *                           * percentage. If additional area exceeds the limit, algorithm returns as many vertices
     *                           as there were at the moment the limit was exceeded
     * @throws NullPointerException if curve, approxCurve is null
     */
    public static void approxPolyN(Mat curve, Mat approxCurve, int nsides, float epsilon_percentage) {
        approxPolyN_1(curve.nativeObj, approxCurve.nativeObj, nsides, epsilon_percentage);
    }

    /**
     * Approximates a polygon with a convex hull with a specified accuracy and number of sides. T he approxPolyN
     * function approximates a polygon with *a convex hull so that the difference between the contour area of the
     * original contour and the new polygon is minimal. It uses a greedy algorithm for contracting two vertices into one
     * in such a way that the additional area is minimal. Straight lines formed by each edge of the convex contour are
     * drawn and the areas of the resulting triangles are considered. Each vertex will lie either on the original
     * contour or outside it. The algorithm based on the paper.
     *
     * @param curve       Input vector of a 2D points stored in vector or Mat, points must be float or integer
     * @param approxCurve Result of the approximation. The type is vector of a 2D point (Point2f or Point) in vector or
     *                    Mat
     * @param nsides      The parameter defines the number of sides of the result polygon
     * @throws NullPointerException if curve, approxCurve is null
     */
    public static void approxPolyN(Mat curve, Mat approxCurve, int nsides) {
        approxPolyN_2(curve.nativeObj, approxCurve.nativeObj, nsides);
    }

    /**
     * Finds a rotated rectangle of the minimum area enclosing the input 2D point set. The function calculates and
     * returns the minimum-area bounding rectangle (possibly rotated) for a specified point set. The angle of rotation
     * represents the angle between the line connecting the starting and ending points (based on the clockwise order
     * with greatest index for the corner with greatest y) and the horizontal axis. This angle always falls between
     * [-90, 0) because, if the object rotates more than a rect angle, the next edge is used to measure the angle. The
     * starting and ending points change as the object rotates.Developer should keep in mind that the returned
     * RotatedRect can contain negative indices when data is close to the containing Mat element boundary.
     *
     * @param points Input vector of 2D points, stored in vector&lt;&gt; or Mat
     * @return a minimum-area rotated rectangle enclosing the points
     */
    public static RotatedRect minAreaRect(MatOfPoint2f points) {
        Mat points_mat = points;
        return new RotatedRect(minAreaRect_0(points_mat.nativeObj));
    }

    /**
     * Finds the four vertices of a rotated rect. Useful to draw the rotated rectangle. The function finds the four
     * vertices of a rotated rectangle. The four vertices are returned in clockwise order starting from the point with
     * greatest y. If two points have the same y coordinate the rightmost is the starting point. This function is useful
     * to draw the rectangle. In C++, instead of using this function, you can directly use RotatedRect::points method.
     * Please visit the REF: tutorial_bounding_rotated_ellipses "tutorial on Creating Bounding rotated boxes and
     * ellipses for contours" for more information.
     *
     * @param box    The input rotated rectangle. It may be the output of REF: minAreaRect
     * @param points The output array of four vertices of rectangles
     * @throws NullPointerException if box, points is null
     */
    public static void boxPoints(RotatedRect box, Mat points) {
        boxPoints_0(box.center.x, box.center.y, box.size.width, box.size.height, box.angle, points.nativeObj);
    }

    /**
     * Finds a circle of the minimum area enclosing a 2D point set. The function finds the minimal enclosing circle of a
     * 2D point set using an iterative algorithm.
     *
     * @param points Input vector of 2D points, stored in vector&lt;&gt; or Mat
     * @param center Output center of the circle
     * @param radius Output radius of the circle
     */
    public static void minEnclosingCircle(MatOfPoint2f points, Point center, float[] radius) {
        Mat points_mat = points;
        double[] center_out = new double[2];
        double[] radius_out = new double[1];
        minEnclosingCircle_0(points_mat.nativeObj, center_out, radius_out);
        if (center != null) {
            center.x = center_out[0];
            center.y = center_out[1];
        }
        if (radius != null)
            radius[0] = (float) radius_out[0];
    }

    /**
     * Finds a triangle of minimum area enclosing a 2D point set and returns its area. The function finds a triangle of
     * minimum area enclosing the given set of 2D points and returns its area. The output for a given 2D point set is
     * shown in the image below. 2D points are depicted in red* and the enclosing triangle in *yellow*. The
     * implementation of the algorithm is based on O'Rourke's and Klee and Laskowski's papers. O'Rourke provides a
     * theta(n) algorithm for finding the minimal enclosing triangle of a 2D convex polygon with n vertices. Since the
     * minEnclosingTriangle function takes a 2D point set as input an additional preprocessing step of computing the
     * convex hull of the 2D point set is required. The complexity of the convexHull function is O(n log(n)) which is
     * higher than theta(n). Thus the overall complexity of the function is O(n log(n)).
     *
     * @param points   Input vector of 2D points with depth CV_32S or CV_32F, stored in vector&lt;&gt; or Mat
     * @param triangle Output vector of three 2D points defining the vertices of the triangle. The depth of the matrix
     *                 must be CV_32F
     * @return the area of the enclosing triangle
     * @throws NullPointerException if points, triangle is null
     */
    public static double minEnclosingTriangle(Mat points, Mat triangle) {
        return minEnclosingTriangle_0(points.nativeObj, triangle.nativeObj);
    }

    /**
     * Finds a convex polygon of minimum area enclosing a 2D point set and returns its area. This function takes a given
     * set of 2D points and finds the enclosing polygon with k vertices and minimal area. It takes the set of points and
     * the parameter k as input and returns the area of the minimal enclosing polygon. The Implementation is based on a
     * paper by Aggarwal, Chang and Yap They provide a theta(n²log(n)log(k)) algorithm for finding the minimal convex
     * polygon with k vertices enclosing a 2D convex polygon with n vertices (k &lt; n). Since the
     * minEnclosingConvexPolygon function takes a 2D point set as input, an additional preprocessing step of computing
     * the convex hull of the 2D point set is required. The complexity of the convexHull function is O(n log(n)) which
     * is lower than theta(n²log(n)log(k)). Thus the overall complexity of the function is O(n²log(n)log(k)).
     *
     * @param points  Input vector of 2D points, stored in vector&lt;&gt; or Mat
     * @param polygon Output vector of 2D points defining the vertices of the enclosing polygon
     * @param k       Number of vertices of the output polygon
     * @return the area of the enclosing convex polygon
     * @throws NullPointerException if points, polygon is null
     */
    public static double minEnclosingConvexPolygon(Mat points, Mat polygon, int k) {
        return minEnclosingConvexPolygon_0(points.nativeObj, polygon.nativeObj, k);
    }

    /**
     * Calculates all of the moments up to the third order of a polygon or rasterized shape. The function computes
     * moments, up to the 3rd order, of a vector shape or a rasterized shape. The results are returned in the structure
     * Moments.
     *
     * @param array       Single channel raster image (CV_8U, CV_16U, CV_16S, CV_32F, CV_64F) or an array ( 1 times N or
     *                    N times 1 ) of 2D points (Point or Point2f)
     * @param binaryImage If it is true, all non-zero image pixels are treated as 1's. The parameter is used for images
     *                    only
     * @return moments. Note: Only applicable to contour moments calculations from Python bindings: Note that the numpy
     *         type for the input array should be either np.int32 or np.float32. Note: For contour-based moments, the
     *         zeroth-order moment c m00 represents the contour area. If the input contour is degenerate (for example, a
     *         single point or all points are collinear), the area is zero and therefore c m00 == 0. In this case, the
     *         centroid coordinates (c m10/m00, c m01/m00) are undefined and must be handled explicitly by the caller. A
     *         common workaround is to compute the center using boundingRect() or by averaging the input points
     * @throws NullPointerException if array is null
     */
    public static Moments moments(Mat array, boolean binaryImage) {
        return new Moments(moments_0(array.nativeObj, binaryImage));
    }

    /**
     * Calculates all of the moments up to the third order of a polygon or rasterized shape. The function computes
     * moments, up to the 3rd order, of a vector shape or a rasterized shape. The results are returned in the structure
     * Moments.
     *
     * @param array Single channel raster image (CV_8U, CV_16U, CV_16S, CV_32F, CV_64F) or an array ( 1 times N or N
     *              times 1 ) of 2D points (Point or Point2f)
     * @return moments. Note: Only applicable to contour moments calculations from Python bindings: Note that the numpy
     *         type for the input array should be either np.int32 or np.float32. Note: For contour-based moments, the
     *         zeroth-order moment c m00 represents the contour area. If the input contour is degenerate (for example, a
     *         single point or all points are collinear), the area is zero and therefore c m00 == 0. In this case, the
     *         centroid coordinates (c m10/m00, c m01/m00) are undefined and must be handled explicitly by the caller. A
     *         common workaround is to compute the center using boundingRect() or by averaging the input points
     * @throws NullPointerException if array is null
     */
    public static Moments moments(Mat array) {
        return new Moments(moments_1(array.nativeObj));
    }

    /**
     * Computes the seven Hu invariant moments from spatial image moments.
     *
     * @param m  spatial, central and normalized moments from which the seven invariants are calculated
     * @param hu output vector receiving the seven Hu invariant moments
     * @throws NullPointerException if m, hu is null
     */
    public static void HuMoments(Moments m, Mat hu) {
        HuMoments_0(m.m00, m.m10, m.m01, m.m20, m.m11, m.m02, m.m30, m.m21, m.m12, m.m03, hu.nativeObj);
    }

    /**
     * Compares two shapes. The function compares two shapes. All three implemented methods use the Hu invariants (see
     * HuMoments).
     *
     * @param contour1  First contour or grayscale image
     * @param contour2  Second contour or grayscale image
     * @param method    Comparison method, see ShapeMatchModes
     * @param parameter Method-specific parameter (not supported now)
     * @return the selected shape-difference measure
     * @throws NullPointerException if contour1, contour2 is null
     */
    public static double matchShapes(Mat contour1, Mat contour2, int method, double parameter) {
        return matchShapes_0(contour1.nativeObj, contour2.nativeObj, method, parameter);
    }

    /**
     * Finds the convex hull of a point set. The function convexHull finds the convex hull of a 2D point set using the
     * Sklansky's algorithm that has *O(N logN)* complexity in the current implementation.
     *
     * @param points    Input 2D point set, stored in vector or Mat
     * @param hull      Output convex hull. It is either an integer vector of indices or vector of points. In the first
     *                  case, the hull elements are 0-based indices of the convex hull points in the original array
     *                  (since the set of convex hull points is a subset of the original point set). In the second case,
     *                  hull elements are the convex hull points themselves
     * @param clockwise Orientation flag. If it is true, the output convex hull is oriented clockwise. Otherwise, it is
     *                  oriented counter-clockwise. The assumed coordinate system has its X axis pointing to the right,
     *                  and its Y axis pointing upwards. returns convex hull points. Otherwise, it returns indices of
     *                  the convex hull points. When the output array is vector, the flag is ignored, and the output
     *                  depends on the type of the vector: vector&lt;int&gt; implies returnPoints=false,
     *                  vector&lt;Point&gt; implies returnPoints=true. Note: points and hull should be different arrays,
     *                  inplace processing isn't supported. Check REF: tutorial_hull "the corresponding tutorial" for
     *                  more details. useful links:
     *                  https://www.learnopencv.com/convex-hull-using-opencv-in-python-and-c/
     */
    public static void convexHull(MatOfPoint points, MatOfInt hull, boolean clockwise) {
        Mat points_mat = points;
        Mat hull_mat = hull;
        convexHull_0(points_mat.nativeObj, hull_mat.nativeObj, clockwise);
    }

    /**
     * Finds the convex hull of a point set. The function convexHull finds the convex hull of a 2D point set using the
     * Sklansky's algorithm that has *O(N logN)* complexity in the current implementation.
     *
     * @param points Input 2D point set, stored in vector or Mat
     * @param hull   Output convex hull. It is either an integer vector of indices or vector of points. In the first
     *               case, the hull elements are 0-based indices of the convex hull points in the original array (since
     *               the set of convex hull points is a subset of the original point set). In the second case, hull
     *               elements are the convex hull points themselves
     */
    public static void convexHull(MatOfPoint points, MatOfInt hull) {
        Mat points_mat = points;
        Mat hull_mat = hull;
        convexHull_2(points_mat.nativeObj, hull_mat.nativeObj);
    }

    /**
     * Finds the convexity defects of a contour. The figure below displays convexity defects of a hand contour:.
     *
     * @param contour          Input contour
     * @param convexhull       Convex hull obtained using convexHull that should contain indices of the contour points
     *                         that make the hull
     * @param convexityDefects The output vector of convexity defects. In C++ and the new Python/Java interface each
     *                         convexity defect is represented as 4-element integer vector (a.k.a. #4-component integer
     *                         vector): (start_index, end_index, farthest_pt_index, fixpt_depth), where indices are
     *                         0-based indices in the original contour of the convexity defect beginning, end and the
     *                         farthest point, and fixpt_depth is fixed-point approximation (with 8 fractional bits) of
     *                         the distance between the farthest contour point and the hull. That is, to get the
     *                         floating-point value of the depth will be fixpt_depth/256.0
     */
    public static void convexityDefects(MatOfPoint contour, MatOfInt convexhull, MatOfInt4 convexityDefects) {
        Mat contour_mat = contour;
        Mat convexhull_mat = convexhull;
        Mat convexityDefects_mat = convexityDefects;
        convexityDefects_0(contour_mat.nativeObj, convexhull_mat.nativeObj, convexityDefects_mat.nativeObj);
    }

    /**
     * Tests a contour convexity. The function tests whether the input contour is convex or not. The contour must be
     * simple, that is, without self-intersections. Otherwise, the function output is undefined.
     *
     * @param contour Input vector of 2D points, stored in vector&lt;&gt; or Mat
     * @return true if the contour is convex
     */
    public static boolean isContourConvex(MatOfPoint contour) {
        Mat contour_mat = contour;
        return isContourConvex_0(contour_mat.nativeObj);
    }

    /**
     * Finds intersection of two convex polygons.
     *
     * @param p1           First polygon
     * @param p2           Second polygon
     * @param p12          Output polygon describing the intersecting area
     * @param handleNested When true, an intersection is found if one of the polygons is fully enclosed in the other.
     *                     When false, no intersection is found. If the polygons share a side or the vertex of one
     *                     polygon lies on an edge of the other, they are not considered nested and an intersection will
     *                     be found regardless of the value of handleNested
     * @return Area of intersecting polygon. May be negative, if algorithm has not converged, e.g. non-convex input.
     *         Note: intersectConvexConvex doesn't confirm that both polygons are convex and will return invalid results
     *         if they aren't
     * @throws NullPointerException if p1, p2, p12 is null
     */
    public static float intersectConvexConvex(Mat p1, Mat p2, Mat p12, boolean handleNested) {
        return intersectConvexConvex_0(p1.nativeObj, p2.nativeObj, p12.nativeObj, handleNested);
    }

    /**
     * Finds intersection of two convex polygons.
     *
     * @param p1  First polygon
     * @param p2  Second polygon
     * @param p12 Output polygon describing the intersecting area
     * @return Area of intersecting polygon. May be negative, if algorithm has not converged, e.g. non-convex input.
     *         Note: intersectConvexConvex doesn't confirm that both polygons are convex and will return invalid results
     *         if they aren't
     * @throws NullPointerException if p1, p2, p12 is null
     */
    public static float intersectConvexConvex(Mat p1, Mat p2, Mat p12) {
        return intersectConvexConvex_1(p1.nativeObj, p2.nativeObj, p12.nativeObj);
    }

    /**
     * Fits an ellipse around a set of 2D points. The function calculates the ellipse that fits (in a least-squares
     * sense) a set of 2D points best of all. It returns the rotated rectangle in which the ellipse is inscribed. The
     * first algorithm described by is used. Developer should keep in mind that it is possible that the returned
     * ellipse/rotatedRect data contains negative indices, due to the data points being close to the border of the
     * containing Mat element.
     *
     * @param points Input 2D point set, stored in vector&lt;&gt; or Mat Note: Input point types are REF: Point2i or
     *               REF: Point2f and at least 5 points are required. Note: REF: getClosestEllipsePoints function can be
     *               used to compute the ellipse fitting error
     * @return the rotated rectangle enclosing the fitted ellipse
     */
    public static RotatedRect fitEllipse(MatOfPoint2f points) {
        Mat points_mat = points;
        return new RotatedRect(fitEllipse_0(points_mat.nativeObj));
    }

    /**
     * Fits an ellipse to two-dimensional points with the approximate mean-square method. The returned rotated rectangle
     * describes the fitted ellipse.
     *
     * @param points Input 2D point set, stored in vector&lt;&gt; or Mat Note: Input point types are REF: Point2i or
     *               REF: Point2f and at least 5 points are required. Note: REF: getClosestEllipsePoints function can be
     *               used to compute the ellipse fitting error
     * @return the rotated rectangle enclosing the fitted ellipse
     * @throws NullPointerException if points is null
     */
    public static RotatedRect fitEllipseAMS(Mat points) {
        return new RotatedRect(fitEllipseAMS_0(points.nativeObj));
    }

    /**
     * Fits an ellipse to two-dimensional points with the direct least-squares method and its ellipse constraint.
     *
     * @param points Input 2D point set, stored in vector&lt;&gt; or Mat Note: Input point types are REF: Point2i or
     *               REF: Point2f and at least 5 points are required. Note: REF: getClosestEllipsePoints function can be
     *               used to compute the ellipse fitting error
     * @return the rotated rectangle enclosing the fitted ellipse
     * @throws NullPointerException if points is null
     */
    public static RotatedRect fitEllipseDirect(Mat points) {
        return new RotatedRect(fitEllipseDirect_0(points.nativeObj));
    }

    /**
     * Compute for each 2d point the nearest 2d point located on a given ellipse. The function computes the nearest 2d
     * location on a given ellipse for a vector of 2d points and is based on code. This function can be used to compute
     * for instance the ellipse fitting error.
     *
     * @param ellipse_params Ellipse parameters
     * @param points         Input 2d points
     * @param closest_pts    For each 2d point, their corresponding closest 2d point located on a given ellipse Note:
     *                       Input point types are REF: Point2i or REF: Point2f
     * @throws NullPointerException if ellipse_params, points, closest_pts is null
     */
    public static void getClosestEllipsePoints(RotatedRect ellipse_params, Mat points, Mat closest_pts) {
        getClosestEllipsePoints_0(
                ellipse_params.center.x,
                ellipse_params.center.y,
                ellipse_params.size.width,
                ellipse_params.size.height,
                ellipse_params.angle,
                points.nativeObj,
                closest_pts.nativeObj);
    }

    /**
     * Fits a line to a 2D or 3D point set.
     *
     * @param points   Input vector of 2D or 3D points, stored in vector&lt;&gt; or Mat
     * @param line     Output line parameters. In case of 2D fitting, it should be a vector of 4 elements (like
     *                 4-component float vector) - (vx, vy, x0, y0), where (vx, vy) is a normalized vector collinear to
     *                 the line and (x0, y0) is a point on the line. In case of 3D fitting, it should be a vector of 6
     *                 elements (like 6-component float vector) - (vx, vy, vz, x0, y0, z0), where (vx, vy, vz) is a
     *                 normalized vector collinear to the line and (x0, y0, z0) is a point on the line
     * @param distType Distance used by the M-estimator, see DistanceTypes
     * @param param    Numerical parameter ( C ) for some types of distances. If it is 0, an optimal value is chosen
     * @param reps     Sufficient accuracy for the radius (distance between the coordinate origin and the line)
     * @param aeps     Sufficient accuracy for the angle. 0.01 would be a good default value for reps and aeps
     * @throws NullPointerException if points, line is null
     */
    public static void fitLine(Mat points, Mat line, int distType, double param, double reps, double aeps) {
        fitLine_0(points.nativeObj, line.nativeObj, distType, param, reps, aeps);
    }

    /**
     * Performs a point-in-contour test. The function determines whether the point is inside a contour, outside, or lies
     * on an edge (or coincides with a vertex). It returns positive (inside), negative (outside), or zero (on an edge)
     * value, correspondingly. When measureDist=false , the return value is +1, -1, and 0, respectively. Otherwise, the
     * return value is a signed distance between the point and the nearest contour edge. See below a sample output of
     * the function where each image pixel is tested against the contour:.
     *
     * @param contour     Input contour
     * @param pt          Point tested against the contour
     * @param measureDist If true, the function estimates the signed distance from the point to the nearest contour
     *                    edge. Otherwise, the function only checks if the point is inside a contour or not
     * @return the inside/outside sign or signed boundary distance selected by measureDist
     * @throws NullPointerException if pt is null
     */
    public static double pointPolygonTest(MatOfPoint2f contour, Point pt, boolean measureDist) {
        Mat contour_mat = contour;
        return pointPolygonTest_0(contour_mat.nativeObj, pt.x, pt.y, measureDist);
    }

    /**
     * Finds out if there is any intersection between two rotated rectangles. If there is then the vertices of the
     * intersecting region are returned as well. Below are some examples of intersection configurations. The hatched
     * pattern indicates the intersecting region and the red vertices are returned by the function.
     *
     * @param rect1              First rectangle
     * @param rect2              Second rectangle
     * @param intersectingRegion The output array of the vertices of the intersecting region. It returns at most 8
     *                           vertices. Stored as vector&lt;Point2f&gt; or Mat as Mx1 of type CV_32FC2
     * @return One of RectanglesIntersectTypes
     * @throws NullPointerException if rect1, rect2, intersectingRegion is null
     */
    public static int rotatedRectangleIntersection(RotatedRect rect1, RotatedRect rect2, Mat intersectingRegion) {
        return rotatedRectangleIntersection_0(
                rect1.center.x,
                rect1.center.y,
                rect1.size.width,
                rect1.size.height,
                rect1.angle,
                rect2.center.x,
                rect2.center.y,
                rect2.size.width,
                rect2.size.height,
                rect2.angle,
                intersectingRegion.nativeObj);
    }

    /**
     * Calculates a contour perimeter or a curve length. The function computes a curve length or a closed contour
     * perimeter.
     *
     * @param curve  Input vector of 2D points, stored in vector or Mat
     * @param closed Flag indicating whether the curve is closed or not
     * @return the contour perimeter or open-curve length
     */
    public static double arcLength(MatOfPoint2f curve, boolean closed) {
        Mat curve_mat = curve;
        return arcLength_0(curve_mat.nativeObj, closed);
    }

    /**
     * Calculates a contour area. The function computes a contour area. Similarly to moments , the area is computed
     * using the Green formula. Thus, the returned area and the number of non-zero pixels, if you draw the contour using
     * drawContours or fillPoly , can be different. Also, the function will most certainly give a wrong results for
     * contours with self-intersections. Example: vector&lt;Point&gt; contour; contour.push_back(Point2f(0, 0));
     * contour.push_back(Point2f(10, 0)); contour.push_back(Point2f(10, 10)); contour.push_back(Point2f(5, 4)); double
     * area0 = contourArea(contour); vector&lt;Point&gt; approx; approxPolyDP(contour, approx, 5, true); double area1 =
     * contourArea(approx); cout &lt;&lt; "area0 =" &lt;&lt; area0 &lt;&lt; endl &lt;&lt; "area1 =" &lt;&lt; area1
     * &lt;&lt; endl &lt;&lt; "approx poly vertices" &lt;&lt; approx.size() &lt;&lt; endl;.
     *
     * @param contour  Input vector of 2D points (contour vertices), stored in vector or Mat
     * @param oriented Oriented area flag. If it is true, the function returns a signed area value, depending on the
     *                 contour orientation (clockwise or counter-clockwise). Using this feature you can determine
     *                 orientation of a contour by taking the sign of an area. By default, the parameter is false, which
     *                 means that the absolute value is returned
     * @return the signed or unsigned contour area selected by oriented
     * @throws NullPointerException if contour is null
     */
    public static double contourArea(Mat contour, boolean oriented) {
        return contourArea_0(contour.nativeObj, oriented);
    }

    /**
     * Calculates a contour area. The function computes a contour area. Similarly to moments , the area is computed
     * using the Green formula. Thus, the returned area and the number of non-zero pixels, if you draw the contour using
     * drawContours or fillPoly , can be different. Also, the function will most certainly give a wrong results for
     * contours with self-intersections. Example: vector&lt;Point&gt; contour; contour.push_back(Point2f(0, 0));
     * contour.push_back(Point2f(10, 0)); contour.push_back(Point2f(10, 10)); contour.push_back(Point2f(5, 4)); double
     * area0 = contourArea(contour); vector&lt;Point&gt; approx; approxPolyDP(contour, approx, 5, true); double area1 =
     * contourArea(approx); cout &lt;&lt; "area0 =" &lt;&lt; area0 &lt;&lt; endl &lt;&lt; "area1 =" &lt;&lt; area1
     * &lt;&lt; endl &lt;&lt; "approx poly vertices" &lt;&lt; approx.size() &lt;&lt; endl;.
     *
     * @param contour Input vector of 2D points (contour vertices), stored in vector or Mat
     * @return the signed or unsigned contour area selected by oriented
     * @throws NullPointerException if contour is null
     */
    public static double contourArea(Mat contour) {
        return contourArea_1(contour.nativeObj);
    }

    /**
     * Calculates the up-right bounding rectangle of a point set or non-zero pixels of gray-scale image. The function
     * calculates and returns the minimal up-right bounding rectangle for the specified point set or non-zero pixels of
     * gray-scale image.
     *
     * @param array Input gray-scale image or 2D point set, stored in vector or Mat. Note: Point coordinates that the
     *              resulting Rect cannot represent - outside of the int range, or infinite - are saturated to INT_MIN /
     *              INT_MAX, and its width and height are clamped to INT_MAX
     * @return the smallest axis-aligned integer bounding rectangle
     * @throws NullPointerException if array is null
     */
    public static Rect boundingRect(Mat array) {
        return new Rect(boundingRect_0(array.nativeObj));
    }

    /**
     * Calculates an affine matrix of 2D rotation.
     *
     * @param center Center of the rotation in the source image
     * @param angle  Rotation angle in degrees. Positive values mean counter-clockwise rotation (the coordinate origin
     *               is assumed to be the top-left corner)
     * @param scale  Isotropic scale factor
     * @return the 2-by-3 affine rotation-and-scale matrix about the specified center
     * @throws NullPointerException if center is null
     */
    public static Mat getRotationMatrix2D(Point center, double angle, double scale) {
        return new Mat(getRotationMatrix2D_0(center.x, center.y, angle, scale));
    }

    /**
     * Inverts an affine transformation. The function computes an inverse affine transformation represented by 2 times 3
     * matrix M: a_{11} &amp; a_{12} &amp; b_1 \\ a_{21} &amp; a_{22} &amp; b_2 The result is also a 2 times 3 matrix of
     * the same type as M.
     *
     * @param M  Original affine transformation
     * @param iM Output reverse affine transformation
     * @throws NullPointerException if M, iM is null
     */
    public static void invertAffineTransform(Mat M, Mat iM) {
        invertAffineTransform_0(M.nativeObj, iM.nativeObj);
    }

    /**
     * Calculates a perspective transform from four pairs of the corresponding points.
     *
     * @param src         Coordinates of quadrangle vertices in the source image
     * @param dst         Coordinates of the corresponding quadrangle vertices in the destination image
     * @param solveMethod method passed to solve (DecompTypes)
     * @return the 3-by-3 projective transform mapping the four source points to their corresponding destinations
     * @throws NullPointerException if src, dst is null
     */
    public static Mat getPerspectiveTransform(Mat src, Mat dst, int solveMethod) {
        return new Mat(getPerspectiveTransform_0(src.nativeObj, dst.nativeObj, solveMethod));
    }

    /**
     * Calculates a perspective transform from four pairs of the corresponding points.
     *
     * @param src Coordinates of quadrangle vertices in the source image
     * @param dst Coordinates of the corresponding quadrangle vertices in the destination image
     * @return the 3-by-3 projective transform mapping the four source points to their corresponding destinations
     * @throws NullPointerException if src, dst is null
     */
    public static Mat getPerspectiveTransform(Mat src, Mat dst) {
        return new Mat(getPerspectiveTransform_1(src.nativeObj, dst.nativeObj));
    }

    /**
     * Returns the 2-by-3 affine transform mapping the three source points to their corresponding destinations.
     *
     * @param src source matrix
     * @param dst destination matrix that receives the result
     * @return the 2-by-3 affine transform mapping the three source points to their corresponding destinations
     */
    public static Mat getAffineTransform(MatOfPoint2f src, MatOfPoint2f dst) {
        Mat src_mat = src;
        Mat dst_mat = dst;
        return new Mat(getAffineTransform_0(src_mat.nativeObj, dst_mat.nativeObj));
    }

    /**
     * Converts a rotation matrix to a rotation vector or vice versa.
     *
     * @param src      Input rotation vector (3x1 or 1x3) or rotation matrix (3x3)
     * @param dst      Output rotation matrix (3x3) or rotation vector (3x1 or 1x3), respectively
     * @param jacobian Optional output Jacobian matrix, 3x9 or 9x3, which is a matrix of partial derivatives of the
     *                 output array components with respect to the input array components. {l} theta from norm(r) \\ r
     *                 from r/ theta \\ R = cos(theta) I + (1- cos{theta} ) r r^T + sin(theta)
     *                 vecthreethree{0}{-r_z}{r_y}{r_z}{0}{-r_x}{-r_y}{r_x}{0} Inverse transformation can be also done
     *                 easily, since sin ( theta ) vecthreethree{0}{-r_z}{r_y}{r_z}{0}{-r_x}{-r_y}{r_x}{0} = (R -
     *                 R^T)/(2) A rotation vector is a convenient and most compact representation of a rotation matrix
     *                 (since any rotation matrix has just 3 degrees of freedom). The representation is used in the
     *                 global 3D geometry optimization procedures like REF: calibrateCamera, REF: stereoCalibrate, or
     *                 REF: solvePnP . Note: More information about the computation of the derivative of a 3D rotation
     *                 matrix with respect to its exponential coordinate can be found in: A Compact Formula for the
     *                 Derivative of a 3-D Rotation in Exponential Coordinates, Guillermo Gallego, Anthony J. Yezzi
     *                 Note: Useful information on SE(3) and Lie Groups can be found in: A tutorial on SE(3)
     *                 transformation parameterizations and on-manifold optimization, Jose-Luis Blanco Lie Groups for 2D
     *                 and 3D Transformation, Ethan Eade A micro Lie theory for state estimation in robotics, Joan Solà,
     *                 Jérémie Deray, Dinesh Atchuthan
     * @throws NullPointerException if src, dst, jacobian is null
     */
    public static void Rodrigues(Mat src, Mat dst, Mat jacobian) {
        Rodrigues_0(src.nativeObj, dst.nativeObj, jacobian.nativeObj);
    }

    /**
     * Converts a rotation matrix to a rotation vector or vice versa.
     *
     * @param src Input rotation vector (3x1 or 1x3) or rotation matrix (3x3)
     * @param dst Output rotation matrix (3x3) or rotation vector (3x1 or 1x3), respectively
     * @throws NullPointerException if src, dst is null
     */
    public static void Rodrigues(Mat src, Mat dst) {
        Rodrigues_1(src.nativeObj, dst.nativeObj);
    }

    /**
     * Finds a perspective transformation between two planes.
     *
     * @param srcPoints             Coordinates of the points in the original plane, a matrix of the type CV_32FC2 or
     *                              vector&lt;Point2f&gt;
     * @param dstPoints             Coordinates of the points in the target plane, a matrix of the type CV_32FC2 or a
     *                              vector&lt;Point2f&gt;
     * @param method                Method used to compute a homography matrix. The following methods are possible: 0 -
     *                              a regular method using all the points, i.e., the least squares method REF: RANSAC -
     *                              RANSAC-based robust method REF: LMEDS - Least-Median robust method REF: RHO -
     *                              PROSAC-based robust method
     * @param ransacReprojThreshold Maximum allowed reprojection error to treat a point pair as an inlier (used in the
     *                              RANSAC and RHO methods only). That is, if \| {@code dstPoints} _i - {@code
     *     convertPointsHomogeneous} ( {@code H} cdot {@code srcPoints} _i) \|_2 &gt; {@code
     *     ransacReprojThreshold} then the point i is considered as an outlier. If srcPoints and dstPoints are
     *                              measured in pixels, it usually makes sense to set this parameter somewhere in the
     *                              range of 1 to 10
     * @param mask                  Optional output mask set by a robust method ( RANSAC or LMeDS ). Note that the input
     *                              mask values are ignored
     * @param maxIters              The maximum number of RANSAC iterations
     * @param confidence            Confidence level, between 0 and 1. The function finds and returns the perspective
     *                              transformation H between the source and the destination planes: s_i
     *                              vecthree{x'_i}{y'_i}{1} sim H vecthree{x_i}{y_i}{1} so that the back-projection
     *                              error sum _i left ( x'_i- frac{h_{11} x_i + h_{12} y_i + h_{13}}{h_{31} x_i + h_{32}
     *                              y_i + h_{33}} right )^2+ left ( y'_i- frac{h_{21} x_i + h_{22} y_i + h_{23}}{h_{31}
     *                              x_i + h_{32} y_i + h_{33}} right )^2 is minimized. If the parameter method is set to
     *                              the default value 0, the function uses all the point pairs to compute an initial
     *                              homography estimate with a simple least-squares scheme. However, if not all of the
     *                              point pairs ( srcPoints_i, dstPoints_i ) fit the rigid perspective transformation
     *                              (that is, there are some outliers), this initial estimate will be poor. In this
     *                              case, you can use one of the three robust methods. The methods RANSAC, LMeDS and RHO
     *                              try many different random subsets of the corresponding point pairs (of four pairs
     *                              each, collinear pairs are discarded), estimate the homography matrix using this
     *                              subset and a simple least-squares algorithm, and then compute the quality/goodness
     *                              of the computed homography (which is the number of inliers for RANSAC or the least
     *                              median re-projection error for LMeDS). The best subset is then used to produce the
     *                              initial estimate of the homography matrix and the mask of inliers/outliers.
     *                              Regardless of the method, robust or not, the computed homography matrix is refined
     *                              further (using inliers only in case of a robust method) with the Levenberg-Marquardt
     *                              method to reduce the re-projection error even more. The methods RANSAC and RHO can
     *                              handle practically any ratio of outliers but need a threshold to distinguish inliers
     *                              from outliers. The method LMeDS does not need any threshold but it works correctly
     *                              only when there are more than 50% of inliers. Finally, if there are no outliers and
     *                              the noise is rather small, use the default method (method=0). The function is used
     *                              to find initial intrinsic and extrinsic matrices. Homography matrix is determined up
     *                              to a scale. If h_{33} is non-zero, the matrix is normalized so that h_{33}=1. Note:
     *                              Whenever an H matrix cannot be estimated, an empty one will be returned
     * @return the estimated homography, or an empty matrix when estimation fails
     * @throws NullPointerException if mask is null
     */
    public static Mat findHomography(
            MatOfPoint2f srcPoints,
            MatOfPoint2f dstPoints,
            int method,
            double ransacReprojThreshold,
            Mat mask,
            int maxIters,
            double confidence) {
        Mat srcPoints_mat = srcPoints;
        Mat dstPoints_mat = dstPoints;
        return new Mat(findHomography_0(
                srcPoints_mat.nativeObj,
                dstPoints_mat.nativeObj,
                method,
                ransacReprojThreshold,
                mask.nativeObj,
                maxIters,
                confidence));
    }

    /**
     * Finds a perspective transformation between two planes.
     *
     * @param srcPoints             Coordinates of the points in the original plane, a matrix of the type CV_32FC2 or
     *                              vector&lt;Point2f&gt;
     * @param dstPoints             Coordinates of the points in the target plane, a matrix of the type CV_32FC2 or a
     *                              vector&lt;Point2f&gt;
     * @param method                Method used to compute a homography matrix. The following methods are possible: 0 -
     *                              a regular method using all the points, i.e., the least squares method REF: RANSAC -
     *                              RANSAC-based robust method REF: LMEDS - Least-Median robust method REF: RHO -
     *                              PROSAC-based robust method
     * @param ransacReprojThreshold Maximum allowed reprojection error to treat a point pair as an inlier (used in the
     *                              RANSAC and RHO methods only). That is, if \| {@code dstPoints} _i - {@code
     *     convertPointsHomogeneous} ( {@code H} cdot {@code srcPoints} _i) \|_2 &gt; {@code
     *     ransacReprojThreshold} then the point i is considered as an outlier. If srcPoints and dstPoints are
     *                              measured in pixels, it usually makes sense to set this parameter somewhere in the
     *                              range of 1 to 10
     * @param mask                  Optional output mask set by a robust method ( RANSAC or LMeDS ). Note that the input
     *                              mask values are ignored
     * @param maxIters              The maximum number of RANSAC iterations
     * @return the estimated homography, or an empty matrix when estimation fails
     * @throws NullPointerException if mask is null
     */
    public static Mat findHomography(
            MatOfPoint2f srcPoints,
            MatOfPoint2f dstPoints,
            int method,
            double ransacReprojThreshold,
            Mat mask,
            int maxIters) {
        Mat srcPoints_mat = srcPoints;
        Mat dstPoints_mat = dstPoints;
        return new Mat(findHomography_1(
                srcPoints_mat.nativeObj,
                dstPoints_mat.nativeObj,
                method,
                ransacReprojThreshold,
                mask.nativeObj,
                maxIters));
    }

    /**
     * Finds a perspective transformation between two planes.
     *
     * @param srcPoints             Coordinates of the points in the original plane, a matrix of the type CV_32FC2 or
     *                              vector&lt;Point2f&gt;
     * @param dstPoints             Coordinates of the points in the target plane, a matrix of the type CV_32FC2 or a
     *                              vector&lt;Point2f&gt;
     * @param method                Method used to compute a homography matrix. The following methods are possible: 0 -
     *                              a regular method using all the points, i.e., the least squares method REF: RANSAC -
     *                              RANSAC-based robust method REF: LMEDS - Least-Median robust method REF: RHO -
     *                              PROSAC-based robust method
     * @param ransacReprojThreshold Maximum allowed reprojection error to treat a point pair as an inlier (used in the
     *                              RANSAC and RHO methods only). That is, if \| {@code dstPoints} _i - {@code
     *     convertPointsHomogeneous} ( {@code H} cdot {@code srcPoints} _i) \|_2 &gt; {@code
     *     ransacReprojThreshold} then the point i is considered as an outlier. If srcPoints and dstPoints are
     *                              measured in pixels, it usually makes sense to set this parameter somewhere in the
     *                              range of 1 to 10
     * @param mask                  Optional output mask set by a robust method ( RANSAC or LMeDS ). Note that the input
     *                              mask values are ignored
     * @return the estimated homography, or an empty matrix when estimation fails
     * @throws NullPointerException if mask is null
     */
    public static Mat findHomography(
            MatOfPoint2f srcPoints,
            MatOfPoint2f dstPoints,
            int method,
            double ransacReprojThreshold,
            Mat mask) {
        Mat srcPoints_mat = srcPoints;
        Mat dstPoints_mat = dstPoints;
        return new Mat(findHomography_2(
                srcPoints_mat.nativeObj,
                dstPoints_mat.nativeObj,
                method,
                ransacReprojThreshold,
                mask.nativeObj));
    }

    /**
     * Finds a perspective transformation between two planes.
     *
     * @param srcPoints             Coordinates of the points in the original plane, a matrix of the type CV_32FC2 or
     *                              vector&lt;Point2f&gt;
     * @param dstPoints             Coordinates of the points in the target plane, a matrix of the type CV_32FC2 or a
     *                              vector&lt;Point2f&gt;
     * @param method                Method used to compute a homography matrix. The following methods are possible: 0 -
     *                              a regular method using all the points, i.e., the least squares method REF: RANSAC -
     *                              RANSAC-based robust method REF: LMEDS - Least-Median robust method REF: RHO -
     *                              PROSAC-based robust method
     * @param ransacReprojThreshold Maximum allowed reprojection error to treat a point pair as an inlier (used in the
     *                              RANSAC and RHO methods only). That is, if \| {@code dstPoints} _i - {@code
     *     convertPointsHomogeneous} ( {@code H} cdot {@code srcPoints} _i) \|_2 &gt; {@code
     *     ransacReprojThreshold} then the point i is considered as an outlier. If srcPoints and dstPoints are
     *                              measured in pixels, it usually makes sense to set this parameter somewhere in the
     *                              range of 1 to 10
     * @return the estimated homography, or an empty matrix when estimation fails
     */
    public static Mat findHomography(
            MatOfPoint2f srcPoints,
            MatOfPoint2f dstPoints,
            int method,
            double ransacReprojThreshold) {
        Mat srcPoints_mat = srcPoints;
        Mat dstPoints_mat = dstPoints;
        return new Mat(
                findHomography_3(srcPoints_mat.nativeObj, dstPoints_mat.nativeObj, method, ransacReprojThreshold));
    }

    /**
     * Finds a perspective transformation between two planes.
     *
     * @param srcPoints Coordinates of the points in the original plane, a matrix of the type CV_32FC2 or
     *                  vector&lt;Point2f&gt;
     * @param dstPoints Coordinates of the points in the target plane, a matrix of the type CV_32FC2 or a
     *                  vector&lt;Point2f&gt;
     * @param method    Method used to compute a homography matrix. The following methods are possible: 0 - a regular
     *                  method using all the points, i.e., the least squares method REF: RANSAC - RANSAC-based robust
     *                  method REF: LMEDS - Least-Median robust method REF: RHO - PROSAC-based robust method
     * @return the estimated homography, or an empty matrix when estimation fails
     */
    public static Mat findHomography(MatOfPoint2f srcPoints, MatOfPoint2f dstPoints, int method) {
        Mat srcPoints_mat = srcPoints;
        Mat dstPoints_mat = dstPoints;
        return new Mat(findHomography_4(srcPoints_mat.nativeObj, dstPoints_mat.nativeObj, method));
    }

    /**
     * Finds a perspective transformation between two planes.
     *
     * @param srcPoints Coordinates of the points in the original plane, a matrix of the type CV_32FC2 or
     *                  vector&lt;Point2f&gt;
     * @param dstPoints Coordinates of the points in the target plane, a matrix of the type CV_32FC2 or a
     *                  vector&lt;Point2f&gt;
     * @return the estimated homography, or an empty matrix when estimation fails
     */
    public static Mat findHomography(MatOfPoint2f srcPoints, MatOfPoint2f dstPoints) {
        Mat srcPoints_mat = srcPoints;
        Mat dstPoints_mat = dstPoints;
        return new Mat(findHomography_5(srcPoints_mat.nativeObj, dstPoints_mat.nativeObj));
    }

    /**
     * Finds a perspective transformation between two planes.
     *
     * @param srcPoints Coordinates of the points in the original plane, a matrix of the type CV_32FC2 or
     *                  vector&lt;Point2f&gt;
     * @param dstPoints Coordinates of the points in the target plane, a matrix of the type CV_32FC2 or a
     *                  vector&lt;Point2f&gt;
     * @param mask      Optional output mask set by a robust method ( RANSAC or LMeDS ). Note that the input mask values
     *                  are ignored
     * @param params    robust-estimation sampling, scoring, threshold and refinement settings
     * @return the estimated homography, or an empty matrix when estimation fails
     * @throws NullPointerException if mask, params is null
     */
    public static Mat findHomography(MatOfPoint2f srcPoints, MatOfPoint2f dstPoints, Mat mask, UsacParams params) {
        Mat srcPoints_mat = srcPoints;
        Mat dstPoints_mat = dstPoints;
        return new Mat(findHomography_6(
                srcPoints_mat.nativeObj,
                dstPoints_mat.nativeObj,
                mask.nativeObj,
                params.getNativeObjAddr()));
    }

    /**
     * Computes an RQ decomposition of 3x3 matrices.
     *
     * @param src  3x3 input matrix
     * @param mtxR Output 3x3 upper-triangular matrix
     * @param mtxQ Output 3x3 orthogonal matrix
     * @param Qx   Optional output 3x3 rotation matrix around x-axis
     * @param Qy   Optional output 3x3 rotation matrix around y-axis
     * @param Qz   Optional output 3x3 rotation matrix around z-axis. The function computes a RQ decomposition using the
     *             given rotations. This function is used in decomposeProjectionMatrix to decompose the left 3x3
     *             submatrix of a projection matrix into a camera and a rotation matrix. It optionally returns three
     *             rotation matrices, one for each axis, and the three Euler angles in degrees (as the return value)
     *             that could be used in OpenGL. Note, there is always more than one sequence of rotations about the
     *             three principal axes that results in the same orientation of an object, e.g. see Returned three
     *             rotation matrices and corresponding three Euler angles are only one of the possible solutions
     * @return three Euler angles, in degrees, corresponding to the decomposed rotation
     * @throws NullPointerException if src, mtxR, mtxQ, Qx, Qy, Qz is null
     */
    public static double[] RQDecomp3x3(Mat src, Mat mtxR, Mat mtxQ, Mat Qx, Mat Qy, Mat Qz) {
        return RQDecomp3x3_0(src.nativeObj, mtxR.nativeObj, mtxQ.nativeObj, Qx.nativeObj, Qy.nativeObj, Qz.nativeObj);
    }

    /**
     * Computes an RQ decomposition of 3x3 matrices.
     *
     * @param src  3x3 input matrix
     * @param mtxR Output 3x3 upper-triangular matrix
     * @param mtxQ Output 3x3 orthogonal matrix
     * @param Qx   Optional output 3x3 rotation matrix around x-axis
     * @param Qy   Optional output 3x3 rotation matrix around y-axis
     * @return three Euler angles, in degrees, corresponding to the decomposed rotation
     * @throws NullPointerException if src, mtxR, mtxQ, Qx, Qy is null
     */
    public static double[] RQDecomp3x3(Mat src, Mat mtxR, Mat mtxQ, Mat Qx, Mat Qy) {
        return RQDecomp3x3_1(src.nativeObj, mtxR.nativeObj, mtxQ.nativeObj, Qx.nativeObj, Qy.nativeObj);
    }

    /**
     * Computes an RQ decomposition of 3x3 matrices.
     *
     * @param src  3x3 input matrix
     * @param mtxR Output 3x3 upper-triangular matrix
     * @param mtxQ Output 3x3 orthogonal matrix
     * @param Qx   Optional output 3x3 rotation matrix around x-axis
     * @return three Euler angles, in degrees, corresponding to the decomposed rotation
     * @throws NullPointerException if src, mtxR, mtxQ, Qx is null
     */
    public static double[] RQDecomp3x3(Mat src, Mat mtxR, Mat mtxQ, Mat Qx) {
        return RQDecomp3x3_2(src.nativeObj, mtxR.nativeObj, mtxQ.nativeObj, Qx.nativeObj);
    }

    /**
     * Computes an RQ decomposition of 3x3 matrices.
     *
     * @param src  3x3 input matrix
     * @param mtxR Output 3x3 upper-triangular matrix
     * @param mtxQ Output 3x3 orthogonal matrix
     * @return three Euler angles, in degrees, corresponding to the decomposed rotation
     * @throws NullPointerException if src, mtxR, mtxQ is null
     */
    public static double[] RQDecomp3x3(Mat src, Mat mtxR, Mat mtxQ) {
        return RQDecomp3x3_3(src.nativeObj, mtxR.nativeObj, mtxQ.nativeObj);
    }

    /**
     * Decomposes a projection matrix into a rotation matrix and a camera intrinsic matrix.
     *
     * @param projMatrix   3x4 input projection matrix P
     * @param cameraMatrix Output 3x3 camera intrinsic matrix cameramatrix{A}
     * @param rotMatrix    Output 3x3 external rotation matrix R
     * @param transVect    Output 4x1 vector representing the camera position in homogeneous coordinates. To obtain the
     *                     translation vector, use t = -rotMatrix * transVect[:3]
     * @param rotMatrixX   Optional 3x3 rotation matrix around x-axis
     * @param rotMatrixY   Optional 3x3 rotation matrix around y-axis
     * @param rotMatrixZ   Optional 3x3 rotation matrix around z-axis
     * @param eulerAngles  Optional three-element vector containing three Euler angles of rotation in degrees. The
     *                     function computes a decomposition of a projection matrix into a calibration and a rotation
     *                     matrix and the position of a camera. It optionally returns three rotation matrices, one for
     *                     each axis, and three Euler angles that could be used in OpenGL. Note, there is always more
     *                     than one sequence of rotations about the three principal axes that results in the same
     *                     orientation of an object, e.g. see Returned three rotation matrices and corresponding three
     *                     Euler angles are only one of the possible solutions. The function is based on RQDecomp3x3
     * @throws NullPointerException if projMatrix, cameraMatrix, rotMatrix, transVect, rotMatrixX, rotMatrixY,
     *                              rotMatrixZ, eulerAngles is null
     */
    public static void decomposeProjectionMatrix(
            Mat projMatrix,
            Mat cameraMatrix,
            Mat rotMatrix,
            Mat transVect,
            Mat rotMatrixX,
            Mat rotMatrixY,
            Mat rotMatrixZ,
            Mat eulerAngles) {
        decomposeProjectionMatrix_0(
                projMatrix.nativeObj,
                cameraMatrix.nativeObj,
                rotMatrix.nativeObj,
                transVect.nativeObj,
                rotMatrixX.nativeObj,
                rotMatrixY.nativeObj,
                rotMatrixZ.nativeObj,
                eulerAngles.nativeObj);
    }

    /**
     * Decomposes a projection matrix into a rotation matrix and a camera intrinsic matrix.
     *
     * @param projMatrix   3x4 input projection matrix P
     * @param cameraMatrix Output 3x3 camera intrinsic matrix cameramatrix{A}
     * @param rotMatrix    Output 3x3 external rotation matrix R
     * @param transVect    Output 4x1 vector representing the camera position in homogeneous coordinates. To obtain the
     *                     translation vector, use t = -rotMatrix * transVect[:3]
     * @param rotMatrixX   Optional 3x3 rotation matrix around x-axis
     * @param rotMatrixY   Optional 3x3 rotation matrix around y-axis
     * @param rotMatrixZ   Optional 3x3 rotation matrix around z-axis
     * @throws NullPointerException if projMatrix, cameraMatrix, rotMatrix, transVect, rotMatrixX, rotMatrixY,
     *                              rotMatrixZ is null
     */
    public static void decomposeProjectionMatrix(
            Mat projMatrix,
            Mat cameraMatrix,
            Mat rotMatrix,
            Mat transVect,
            Mat rotMatrixX,
            Mat rotMatrixY,
            Mat rotMatrixZ) {
        decomposeProjectionMatrix_1(
                projMatrix.nativeObj,
                cameraMatrix.nativeObj,
                rotMatrix.nativeObj,
                transVect.nativeObj,
                rotMatrixX.nativeObj,
                rotMatrixY.nativeObj,
                rotMatrixZ.nativeObj);
    }

    /**
     * Decomposes a projection matrix into a rotation matrix and a camera intrinsic matrix.
     *
     * @param projMatrix   3x4 input projection matrix P
     * @param cameraMatrix Output 3x3 camera intrinsic matrix cameramatrix{A}
     * @param rotMatrix    Output 3x3 external rotation matrix R
     * @param transVect    Output 4x1 vector representing the camera position in homogeneous coordinates. To obtain the
     *                     translation vector, use t = -rotMatrix * transVect[:3]
     * @param rotMatrixX   Optional 3x3 rotation matrix around x-axis
     * @param rotMatrixY   Optional 3x3 rotation matrix around y-axis
     * @throws NullPointerException if projMatrix, cameraMatrix, rotMatrix, transVect, rotMatrixX, rotMatrixY is null
     */
    public static void decomposeProjectionMatrix(
            Mat projMatrix,
            Mat cameraMatrix,
            Mat rotMatrix,
            Mat transVect,
            Mat rotMatrixX,
            Mat rotMatrixY) {
        decomposeProjectionMatrix_2(
                projMatrix.nativeObj,
                cameraMatrix.nativeObj,
                rotMatrix.nativeObj,
                transVect.nativeObj,
                rotMatrixX.nativeObj,
                rotMatrixY.nativeObj);
    }

    /**
     * Decomposes a projection matrix into a rotation matrix and a camera intrinsic matrix.
     *
     * @param projMatrix   3x4 input projection matrix P
     * @param cameraMatrix Output 3x3 camera intrinsic matrix cameramatrix{A}
     * @param rotMatrix    Output 3x3 external rotation matrix R
     * @param transVect    Output 4x1 vector representing the camera position in homogeneous coordinates. To obtain the
     *                     translation vector, use t = -rotMatrix * transVect[:3]
     * @param rotMatrixX   Optional 3x3 rotation matrix around x-axis
     * @throws NullPointerException if projMatrix, cameraMatrix, rotMatrix, transVect, rotMatrixX is null
     */
    public static void decomposeProjectionMatrix(
            Mat projMatrix,
            Mat cameraMatrix,
            Mat rotMatrix,
            Mat transVect,
            Mat rotMatrixX) {
        decomposeProjectionMatrix_3(
                projMatrix.nativeObj,
                cameraMatrix.nativeObj,
                rotMatrix.nativeObj,
                transVect.nativeObj,
                rotMatrixX.nativeObj);
    }

    /**
     * Decomposes a projection matrix into a rotation matrix and a camera intrinsic matrix.
     *
     * @param projMatrix   3x4 input projection matrix P
     * @param cameraMatrix Output 3x3 camera intrinsic matrix cameramatrix{A}
     * @param rotMatrix    Output 3x3 external rotation matrix R
     * @param transVect    Output 4x1 vector representing the camera position in homogeneous coordinates. To obtain the
     *                     translation vector, use t = -rotMatrix * transVect[:3]
     * @throws NullPointerException if projMatrix, cameraMatrix, rotMatrix, transVect is null
     */
    public static void decomposeProjectionMatrix(Mat projMatrix, Mat cameraMatrix, Mat rotMatrix, Mat transVect) {
        decomposeProjectionMatrix_4(
                projMatrix.nativeObj,
                cameraMatrix.nativeObj,
                rotMatrix.nativeObj,
                transVect.nativeObj);
    }

    /**
     * Computes partial derivatives of the matrix product for each multiplied matrix.
     *
     * @param A     First multiplied matrix
     * @param B     Second multiplied matrix
     * @param dABdA First output derivative matrix d(A*B)/dA of size {@code A.rows*B.cols} times {A.rows*A.cols}
     * @param dABdB Second output derivative matrix d(A*B)/dB of size {@code A.rows*B.cols} times {B.rows*B.cols} . The
     *              function computes partial derivatives of the elements of the matrix product A*B with regard to the
     *              elements of each of the two input matrices. The function is used to compute the Jacobian matrices in
     *              stereoCalibrate but can also be used in any other similar optimization function
     * @throws NullPointerException if A, B, dABdA, dABdB is null
     */
    public static void matMulDeriv(Mat A, Mat B, Mat dABdA, Mat dABdB) {
        matMulDeriv_0(A.nativeObj, B.nativeObj, dABdA.nativeObj, dABdB.nativeObj);
    }

    /**
     * Combines two rotation-and-shift transformations.
     *
     * @param rvec1  First rotation vector
     * @param tvec1  First translation vector
     * @param rvec2  Second rotation vector
     * @param tvec2  Second translation vector
     * @param rvec3  Output rotation vector of the superposition
     * @param tvec3  Output translation vector of the superposition
     * @param dr3dr1 Optional output derivative of rvec3 with regard to rvec1
     * @param dr3dt1 Optional output derivative of rvec3 with regard to tvec1
     * @param dr3dr2 Optional output derivative of rvec3 with regard to rvec2
     * @param dr3dt2 Optional output derivative of rvec3 with regard to tvec2
     * @param dt3dr1 Optional output derivative of tvec3 with regard to rvec1
     * @param dt3dt1 Optional output derivative of tvec3 with regard to tvec1
     * @param dt3dr2 Optional output derivative of tvec3 with regard to rvec2
     * @param dt3dt2 Optional output derivative of tvec3 with regard to tvec2 The functions compute: {l} {@code
     *     rvec3} = {@code rodrigues} ^{-1} left ( {@code rodrigues} ( {@code rvec2} ) cdot {@code rodrigues} (
     *               {@code rvec1} ) right ) \\ {@code tvec3} = {@code rodrigues} ( {@code rvec2} ) cdot {@code tvec1} +
     *               {@code tvec2} , where {@code rodrigues} denotes a rotation vector to a rotation matrix
     *               transformation, and {@code rodrigues}^{-1} denotes the inverse transformation. See Rodrigues for
     *               details. Also, the functions can compute the derivatives of the output vectors with regards to the
     *               input vectors (see matMulDeriv ). The functions are used inside stereoCalibrate but can also be
     *               used in your own code where Levenberg-Marquardt or another gradient-based solver is used to
     *               optimize a function that contains a matrix multiplication
     * @throws NullPointerException if rvec1, tvec1, rvec2, tvec2, rvec3, tvec3, dr3dr1, dr3dt1, dr3dr2, dr3dt2, dt3dr1,
     *                              dt3dt1, dt3dr2, dt3dt2 is null
     */
    public static void composeRT(
            Mat rvec1,
            Mat tvec1,
            Mat rvec2,
            Mat tvec2,
            Mat rvec3,
            Mat tvec3,
            Mat dr3dr1,
            Mat dr3dt1,
            Mat dr3dr2,
            Mat dr3dt2,
            Mat dt3dr1,
            Mat dt3dt1,
            Mat dt3dr2,
            Mat dt3dt2) {
        composeRT_0(
                rvec1.nativeObj,
                tvec1.nativeObj,
                rvec2.nativeObj,
                tvec2.nativeObj,
                rvec3.nativeObj,
                tvec3.nativeObj,
                dr3dr1.nativeObj,
                dr3dt1.nativeObj,
                dr3dr2.nativeObj,
                dr3dt2.nativeObj,
                dt3dr1.nativeObj,
                dt3dt1.nativeObj,
                dt3dr2.nativeObj,
                dt3dt2.nativeObj);
    }

    /**
     * Combines two rotation-and-shift transformations.
     *
     * @param rvec1  First rotation vector
     * @param tvec1  First translation vector
     * @param rvec2  Second rotation vector
     * @param tvec2  Second translation vector
     * @param rvec3  Output rotation vector of the superposition
     * @param tvec3  Output translation vector of the superposition
     * @param dr3dr1 Optional output derivative of rvec3 with regard to rvec1
     * @param dr3dt1 Optional output derivative of rvec3 with regard to tvec1
     * @param dr3dr2 Optional output derivative of rvec3 with regard to rvec2
     * @param dr3dt2 Optional output derivative of rvec3 with regard to tvec2
     * @param dt3dr1 Optional output derivative of tvec3 with regard to rvec1
     * @param dt3dt1 Optional output derivative of tvec3 with regard to tvec1
     * @param dt3dr2 Optional output derivative of tvec3 with regard to rvec2
     * @throws NullPointerException if rvec1, tvec1, rvec2, tvec2, rvec3, tvec3, dr3dr1, dr3dt1, dr3dr2, dr3dt2, dt3dr1,
     *                              dt3dt1, dt3dr2 is null
     */
    public static void composeRT(
            Mat rvec1,
            Mat tvec1,
            Mat rvec2,
            Mat tvec2,
            Mat rvec3,
            Mat tvec3,
            Mat dr3dr1,
            Mat dr3dt1,
            Mat dr3dr2,
            Mat dr3dt2,
            Mat dt3dr1,
            Mat dt3dt1,
            Mat dt3dr2) {
        composeRT_1(
                rvec1.nativeObj,
                tvec1.nativeObj,
                rvec2.nativeObj,
                tvec2.nativeObj,
                rvec3.nativeObj,
                tvec3.nativeObj,
                dr3dr1.nativeObj,
                dr3dt1.nativeObj,
                dr3dr2.nativeObj,
                dr3dt2.nativeObj,
                dt3dr1.nativeObj,
                dt3dt1.nativeObj,
                dt3dr2.nativeObj);
    }

    /**
     * Combines two rotation-and-shift transformations.
     *
     * @param rvec1  First rotation vector
     * @param tvec1  First translation vector
     * @param rvec2  Second rotation vector
     * @param tvec2  Second translation vector
     * @param rvec3  Output rotation vector of the superposition
     * @param tvec3  Output translation vector of the superposition
     * @param dr3dr1 Optional output derivative of rvec3 with regard to rvec1
     * @param dr3dt1 Optional output derivative of rvec3 with regard to tvec1
     * @param dr3dr2 Optional output derivative of rvec3 with regard to rvec2
     * @param dr3dt2 Optional output derivative of rvec3 with regard to tvec2
     * @param dt3dr1 Optional output derivative of tvec3 with regard to rvec1
     * @param dt3dt1 Optional output derivative of tvec3 with regard to tvec1
     * @throws NullPointerException if rvec1, tvec1, rvec2, tvec2, rvec3, tvec3, dr3dr1, dr3dt1, dr3dr2, dr3dt2, dt3dr1,
     *                              dt3dt1 is null
     */
    public static void composeRT(
            Mat rvec1,
            Mat tvec1,
            Mat rvec2,
            Mat tvec2,
            Mat rvec3,
            Mat tvec3,
            Mat dr3dr1,
            Mat dr3dt1,
            Mat dr3dr2,
            Mat dr3dt2,
            Mat dt3dr1,
            Mat dt3dt1) {
        composeRT_2(
                rvec1.nativeObj,
                tvec1.nativeObj,
                rvec2.nativeObj,
                tvec2.nativeObj,
                rvec3.nativeObj,
                tvec3.nativeObj,
                dr3dr1.nativeObj,
                dr3dt1.nativeObj,
                dr3dr2.nativeObj,
                dr3dt2.nativeObj,
                dt3dr1.nativeObj,
                dt3dt1.nativeObj);
    }

    /**
     * Combines two rotation-and-shift transformations.
     *
     * @param rvec1  First rotation vector
     * @param tvec1  First translation vector
     * @param rvec2  Second rotation vector
     * @param tvec2  Second translation vector
     * @param rvec3  Output rotation vector of the superposition
     * @param tvec3  Output translation vector of the superposition
     * @param dr3dr1 Optional output derivative of rvec3 with regard to rvec1
     * @param dr3dt1 Optional output derivative of rvec3 with regard to tvec1
     * @param dr3dr2 Optional output derivative of rvec3 with regard to rvec2
     * @param dr3dt2 Optional output derivative of rvec3 with regard to tvec2
     * @param dt3dr1 Optional output derivative of tvec3 with regard to rvec1
     * @throws NullPointerException if rvec1, tvec1, rvec2, tvec2, rvec3, tvec3, dr3dr1, dr3dt1, dr3dr2, dr3dt2, dt3dr1
     *                              is null
     */
    public static void composeRT(
            Mat rvec1,
            Mat tvec1,
            Mat rvec2,
            Mat tvec2,
            Mat rvec3,
            Mat tvec3,
            Mat dr3dr1,
            Mat dr3dt1,
            Mat dr3dr2,
            Mat dr3dt2,
            Mat dt3dr1) {
        composeRT_3(
                rvec1.nativeObj,
                tvec1.nativeObj,
                rvec2.nativeObj,
                tvec2.nativeObj,
                rvec3.nativeObj,
                tvec3.nativeObj,
                dr3dr1.nativeObj,
                dr3dt1.nativeObj,
                dr3dr2.nativeObj,
                dr3dt2.nativeObj,
                dt3dr1.nativeObj);
    }

    /**
     * Combines two rotation-and-shift transformations.
     *
     * @param rvec1  First rotation vector
     * @param tvec1  First translation vector
     * @param rvec2  Second rotation vector
     * @param tvec2  Second translation vector
     * @param rvec3  Output rotation vector of the superposition
     * @param tvec3  Output translation vector of the superposition
     * @param dr3dr1 Optional output derivative of rvec3 with regard to rvec1
     * @param dr3dt1 Optional output derivative of rvec3 with regard to tvec1
     * @param dr3dr2 Optional output derivative of rvec3 with regard to rvec2
     * @param dr3dt2 Optional output derivative of rvec3 with regard to tvec2
     * @throws NullPointerException if rvec1, tvec1, rvec2, tvec2, rvec3, tvec3, dr3dr1, dr3dt1, dr3dr2, dr3dt2 is null
     */
    public static void composeRT(
            Mat rvec1,
            Mat tvec1,
            Mat rvec2,
            Mat tvec2,
            Mat rvec3,
            Mat tvec3,
            Mat dr3dr1,
            Mat dr3dt1,
            Mat dr3dr2,
            Mat dr3dt2) {
        composeRT_4(
                rvec1.nativeObj,
                tvec1.nativeObj,
                rvec2.nativeObj,
                tvec2.nativeObj,
                rvec3.nativeObj,
                tvec3.nativeObj,
                dr3dr1.nativeObj,
                dr3dt1.nativeObj,
                dr3dr2.nativeObj,
                dr3dt2.nativeObj);
    }

    /**
     * Combines two rotation-and-shift transformations.
     *
     * @param rvec1  First rotation vector
     * @param tvec1  First translation vector
     * @param rvec2  Second rotation vector
     * @param tvec2  Second translation vector
     * @param rvec3  Output rotation vector of the superposition
     * @param tvec3  Output translation vector of the superposition
     * @param dr3dr1 Optional output derivative of rvec3 with regard to rvec1
     * @param dr3dt1 Optional output derivative of rvec3 with regard to tvec1
     * @param dr3dr2 Optional output derivative of rvec3 with regard to rvec2
     * @throws NullPointerException if rvec1, tvec1, rvec2, tvec2, rvec3, tvec3, dr3dr1, dr3dt1, dr3dr2 is null
     */
    public static void composeRT(
            Mat rvec1,
            Mat tvec1,
            Mat rvec2,
            Mat tvec2,
            Mat rvec3,
            Mat tvec3,
            Mat dr3dr1,
            Mat dr3dt1,
            Mat dr3dr2) {
        composeRT_5(
                rvec1.nativeObj,
                tvec1.nativeObj,
                rvec2.nativeObj,
                tvec2.nativeObj,
                rvec3.nativeObj,
                tvec3.nativeObj,
                dr3dr1.nativeObj,
                dr3dt1.nativeObj,
                dr3dr2.nativeObj);
    }

    /**
     * Combines two rotation-and-shift transformations.
     *
     * @param rvec1  First rotation vector
     * @param tvec1  First translation vector
     * @param rvec2  Second rotation vector
     * @param tvec2  Second translation vector
     * @param rvec3  Output rotation vector of the superposition
     * @param tvec3  Output translation vector of the superposition
     * @param dr3dr1 Optional output derivative of rvec3 with regard to rvec1
     * @param dr3dt1 Optional output derivative of rvec3 with regard to tvec1
     * @throws NullPointerException if rvec1, tvec1, rvec2, tvec2, rvec3, tvec3, dr3dr1, dr3dt1 is null
     */
    public static void composeRT(
            Mat rvec1,
            Mat tvec1,
            Mat rvec2,
            Mat tvec2,
            Mat rvec3,
            Mat tvec3,
            Mat dr3dr1,
            Mat dr3dt1) {
        composeRT_6(
                rvec1.nativeObj,
                tvec1.nativeObj,
                rvec2.nativeObj,
                tvec2.nativeObj,
                rvec3.nativeObj,
                tvec3.nativeObj,
                dr3dr1.nativeObj,
                dr3dt1.nativeObj);
    }

    /**
     * Combines two rotation-and-shift transformations.
     *
     * @param rvec1  First rotation vector
     * @param tvec1  First translation vector
     * @param rvec2  Second rotation vector
     * @param tvec2  Second translation vector
     * @param rvec3  Output rotation vector of the superposition
     * @param tvec3  Output translation vector of the superposition
     * @param dr3dr1 Optional output derivative of rvec3 with regard to rvec1
     * @throws NullPointerException if rvec1, tvec1, rvec2, tvec2, rvec3, tvec3, dr3dr1 is null
     */
    public static void composeRT(Mat rvec1, Mat tvec1, Mat rvec2, Mat tvec2, Mat rvec3, Mat tvec3, Mat dr3dr1) {
        composeRT_7(
                rvec1.nativeObj,
                tvec1.nativeObj,
                rvec2.nativeObj,
                tvec2.nativeObj,
                rvec3.nativeObj,
                tvec3.nativeObj,
                dr3dr1.nativeObj);
    }

    /**
     * Combines two rotation-and-shift transformations.
     *
     * @param rvec1 First rotation vector
     * @param tvec1 First translation vector
     * @param rvec2 Second rotation vector
     * @param tvec2 Second translation vector
     * @param rvec3 Output rotation vector of the superposition
     * @param tvec3 Output translation vector of the superposition
     * @throws NullPointerException if rvec1, tvec1, rvec2, tvec2, rvec3, tvec3 is null
     */
    public static void composeRT(Mat rvec1, Mat tvec1, Mat rvec2, Mat tvec2, Mat rvec3, Mat tvec3) {
        composeRT_8(
                rvec1.nativeObj,
                tvec1.nativeObj,
                rvec2.nativeObj,
                tvec2.nativeObj,
                rvec3.nativeObj,
                tvec3.nativeObj);
    }

    /**
     * Projects 3D points to an image plane. The function computes the 2D projections of 3D points to the image plane,
     * given intrinsic and extrinsic camera parameters. Optionally, the function computes Jacobians -matrices of partial
     * derivatives of image points coordinates (as functions of all the input parameters) with respect to the particular
     * parameters, intrinsic and/or extrinsic. The Jacobians are used during the global optimization in REF:
     * calibrateCamera, REF: solvePnP, and REF: stereoCalibrate. The function itself can also be used to compute a
     * re-projection error, given the current intrinsic and extrinsic parameters. Note: Coordinate Systems: Input
     * (objectPoints): 3D points in the world coordinate frame. Output (imagePoints): 2D projections in pixel
     * coordinates of the image plane, with distortion applied. The coordinates (u, v) are measured in pixels from the
     * top-left corner of the image. The transformation chain is: World coordinates u2192 Camera coordinates (via
     * rvec/tvec) u2192 Normalized camera coordinates u2192 Distortion applied u2192 Pixel coordinates (via
     * cameraMatrix).
     *
     * @param objectPoints Array of object points expressed wrt. the world coordinate frame. A 3xN/Nx3 1-channel or
     *                     1xN/Nx1 3-channel (or vector&lt;Point3f&gt; ), where N is the number of points in the view
     * @param rvec         The rotation vector (REF: Rodrigues) that, together with tvec, performs a change of basis
     *                     from world to camera coordinate system, see REF: calibrateCamera for details
     * @param tvec         The translation vector, see parameter description above
     * @param cameraMatrix Camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs . If the vector is empty, the zero
     *                     distortion coefficients are assumed
     * @param imagePoints  Output array of image points in pixel coordinates, 1xN/Nx1 2-channel, or
     *                     vector&lt;Point2f&gt;
     * @param jacobian     Optional output 2Nx(10+&lt;numDistCoeffs&gt;) jacobian matrix of derivatives of image points
     *                     with respect to components of the rotation vector, translation vector, focal lengths,
     *                     coordinates of the principal point and the distortion coefficients. In the old interface
     *                     different components of the jacobian are returned via different output parameters
     * @param aspectRatio  Optional "fixed aspect ratio" parameter. If the parameter is not 0, the function assumes that
     *                     the aspect ratio (f_x / f_y) is fixed and correspondingly adjusts the jacobian matrix. Note:
     *                     By setting rvec = tvec = [0, 0, 0], or by setting cameraMatrix to a 3x3 identity matrix, or
     *                     by passing zero distortion coefficients, one can get various useful partial cases of the
     *                     function. This means, one can compute the distorted coordinates for a sparse set of points or
     *                     apply a perspective transformation (and also compute the derivatives) in the ideal
     *                     zero-distortion setup
     * @throws NullPointerException if rvec, tvec, cameraMatrix, jacobian is null
     */
    public static void projectPoints(
            MatOfPoint3f objectPoints,
            Mat rvec,
            Mat tvec,
            Mat cameraMatrix,
            MatOfDouble distCoeffs,
            MatOfPoint2f imagePoints,
            Mat jacobian,
            double aspectRatio) {
        Mat objectPoints_mat = objectPoints;
        Mat distCoeffs_mat = distCoeffs;
        Mat imagePoints_mat = imagePoints;
        projectPoints_0(
                objectPoints_mat.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs_mat.nativeObj,
                imagePoints_mat.nativeObj,
                jacobian.nativeObj,
                aspectRatio);
    }

    /**
     * Projects 3D points to an image plane. The function computes the 2D projections of 3D points to the image plane,
     * given intrinsic and extrinsic camera parameters. Optionally, the function computes Jacobians -matrices of partial
     * derivatives of image points coordinates (as functions of all the input parameters) with respect to the particular
     * parameters, intrinsic and/or extrinsic. The Jacobians are used during the global optimization in REF:
     * calibrateCamera, REF: solvePnP, and REF: stereoCalibrate. The function itself can also be used to compute a
     * re-projection error, given the current intrinsic and extrinsic parameters. Note: Coordinate Systems: Input
     * (objectPoints): 3D points in the world coordinate frame. Output (imagePoints): 2D projections in pixel
     * coordinates of the image plane, with distortion applied. The coordinates (u, v) are measured in pixels from the
     * top-left corner of the image. The transformation chain is: World coordinates u2192 Camera coordinates (via
     * rvec/tvec) u2192 Normalized camera coordinates u2192 Distortion applied u2192 Pixel coordinates (via
     * cameraMatrix).
     *
     * @param objectPoints Array of object points expressed wrt. the world coordinate frame. A 3xN/Nx3 1-channel or
     *                     1xN/Nx1 3-channel (or vector&lt;Point3f&gt; ), where N is the number of points in the view
     * @param rvec         The rotation vector (REF: Rodrigues) that, together with tvec, performs a change of basis
     *                     from world to camera coordinate system, see REF: calibrateCamera for details
     * @param tvec         The translation vector, see parameter description above
     * @param cameraMatrix Camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs . If the vector is empty, the zero
     *                     distortion coefficients are assumed
     * @param imagePoints  Output array of image points in pixel coordinates, 1xN/Nx1 2-channel, or
     *                     vector&lt;Point2f&gt;
     * @param jacobian     Optional output 2Nx(10+&lt;numDistCoeffs&gt;) jacobian matrix of derivatives of image points
     *                     with respect to components of the rotation vector, translation vector, focal lengths,
     *                     coordinates of the principal point and the distortion coefficients. In the old interface
     *                     different components of the jacobian are returned via different output parameters
     * @throws NullPointerException if rvec, tvec, cameraMatrix, jacobian is null
     */
    public static void projectPoints(
            MatOfPoint3f objectPoints,
            Mat rvec,
            Mat tvec,
            Mat cameraMatrix,
            MatOfDouble distCoeffs,
            MatOfPoint2f imagePoints,
            Mat jacobian) {
        Mat objectPoints_mat = objectPoints;
        Mat distCoeffs_mat = distCoeffs;
        Mat imagePoints_mat = imagePoints;
        projectPoints_1(
                objectPoints_mat.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs_mat.nativeObj,
                imagePoints_mat.nativeObj,
                jacobian.nativeObj);
    }

    /**
     * Projects 3D points to an image plane. The function computes the 2D projections of 3D points to the image plane,
     * given intrinsic and extrinsic camera parameters. Optionally, the function computes Jacobians -matrices of partial
     * derivatives of image points coordinates (as functions of all the input parameters) with respect to the particular
     * parameters, intrinsic and/or extrinsic. The Jacobians are used during the global optimization in REF:
     * calibrateCamera, REF: solvePnP, and REF: stereoCalibrate. The function itself can also be used to compute a
     * re-projection error, given the current intrinsic and extrinsic parameters. Note: Coordinate Systems: Input
     * (objectPoints): 3D points in the world coordinate frame. Output (imagePoints): 2D projections in pixel
     * coordinates of the image plane, with distortion applied. The coordinates (u, v) are measured in pixels from the
     * top-left corner of the image. The transformation chain is: World coordinates u2192 Camera coordinates (via
     * rvec/tvec) u2192 Normalized camera coordinates u2192 Distortion applied u2192 Pixel coordinates (via
     * cameraMatrix).
     *
     * @param objectPoints Array of object points expressed wrt. the world coordinate frame. A 3xN/Nx3 1-channel or
     *                     1xN/Nx1 3-channel (or vector&lt;Point3f&gt; ), where N is the number of points in the view
     * @param rvec         The rotation vector (REF: Rodrigues) that, together with tvec, performs a change of basis
     *                     from world to camera coordinate system, see REF: calibrateCamera for details
     * @param tvec         The translation vector, see parameter description above
     * @param cameraMatrix Camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs . If the vector is empty, the zero
     *                     distortion coefficients are assumed
     * @param imagePoints  Output array of image points in pixel coordinates, 1xN/Nx1 2-channel, or
     *                     vector&lt;Point2f&gt;
     * @throws NullPointerException if rvec, tvec, cameraMatrix is null
     */
    public static void projectPoints(
            MatOfPoint3f objectPoints,
            Mat rvec,
            Mat tvec,
            Mat cameraMatrix,
            MatOfDouble distCoeffs,
            MatOfPoint2f imagePoints) {
        Mat objectPoints_mat = objectPoints;
        Mat distCoeffs_mat = distCoeffs;
        Mat imagePoints_mat = imagePoints;
        projectPoints_2(
                objectPoints_mat.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs_mat.nativeObj,
                imagePoints_mat.nativeObj);
    }

    /**
     * Projects three-dimensional object points into image coordinates and returns separately represented Jacobian data.
     *
     * @param objectPoints three-dimensional object coordinates expressed in the object reference frame
     * @param rvec         rotation vector in Rodrigues form
     * @param tvec         translation vector between object and camera coordinates
     * @param cameraMatrix 3-by-3 intrinsic camera matrix
     * @param distCoeffs   camera distortion coefficients
     * @param imagePoints  two-dimensional coordinates in the image plane
     * @param dpdr         output Jacobian of image coordinates with respect to rotation
     * @param dpdt         output Jacobian of image coordinates with respect to translation
     * @param dpdf         output Jacobian with respect to focal lengths
     * @param dpdc         output Jacobian with respect to the principal point
     * @param dpdk         output Jacobian with respect to distortion coefficients
     * @param dpdo         output Jacobian with respect to object-point coordinates
     * @param aspectRatio  fixed ratio between horizontal and vertical focal lengths; zero leaves them independent
     * @throws NullPointerException if objectPoints, rvec, tvec, cameraMatrix, distCoeffs, imagePoints, dpdr, dpdt,
     *                              dpdf, dpdc, dpdk, dpdo is null
     */
    public static void projectPointsSepJ(
            Mat objectPoints,
            Mat rvec,
            Mat tvec,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat imagePoints,
            Mat dpdr,
            Mat dpdt,
            Mat dpdf,
            Mat dpdc,
            Mat dpdk,
            Mat dpdo,
            double aspectRatio) {
        projectPointsSepJ_0(
                objectPoints.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                imagePoints.nativeObj,
                dpdr.nativeObj,
                dpdt.nativeObj,
                dpdf.nativeObj,
                dpdc.nativeObj,
                dpdk.nativeObj,
                dpdo.nativeObj,
                aspectRatio);
    }

    /**
     * Projects three-dimensional object points into image coordinates and returns separately represented Jacobian data.
     *
     * @param objectPoints three-dimensional object coordinates expressed in the object reference frame
     * @param rvec         rotation vector in Rodrigues form
     * @param tvec         translation vector between object and camera coordinates
     * @param cameraMatrix 3-by-3 intrinsic camera matrix
     * @param distCoeffs   camera distortion coefficients
     * @param imagePoints  two-dimensional coordinates in the image plane
     * @param dpdr         output Jacobian of image coordinates with respect to rotation
     * @param dpdt         output Jacobian of image coordinates with respect to translation
     * @param dpdf         output Jacobian with respect to focal lengths
     * @param dpdc         output Jacobian with respect to the principal point
     * @param dpdk         output Jacobian with respect to distortion coefficients
     * @param dpdo         output Jacobian with respect to object-point coordinates
     * @throws NullPointerException if objectPoints, rvec, tvec, cameraMatrix, distCoeffs, imagePoints, dpdr, dpdt,
     *                              dpdf, dpdc, dpdk, dpdo is null
     */
    public static void projectPointsSepJ(
            Mat objectPoints,
            Mat rvec,
            Mat tvec,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat imagePoints,
            Mat dpdr,
            Mat dpdt,
            Mat dpdf,
            Mat dpdc,
            Mat dpdk,
            Mat dpdo) {
        projectPointsSepJ_1(
                objectPoints.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                imagePoints.nativeObj,
                dpdr.nativeObj,
                dpdt.nativeObj,
                dpdf.nativeObj,
                dpdc.nativeObj,
                dpdk.nativeObj,
                dpdo.nativeObj);
    }

    /**
     * Projects three-dimensional object points into image coordinates and returns separately represented Jacobian data.
     *
     * @param objectPoints three-dimensional object coordinates expressed in the object reference frame
     * @param rvec         rotation vector in Rodrigues form
     * @param tvec         translation vector between object and camera coordinates
     * @param cameraMatrix 3-by-3 intrinsic camera matrix
     * @param distCoeffs   camera distortion coefficients
     * @param imagePoints  two-dimensional coordinates in the image plane
     * @param dpdr         output Jacobian of image coordinates with respect to rotation
     * @param dpdt         output Jacobian of image coordinates with respect to translation
     * @param dpdf         output Jacobian with respect to focal lengths
     * @param dpdc         output Jacobian with respect to the principal point
     * @param dpdk         output Jacobian with respect to distortion coefficients
     * @throws NullPointerException if objectPoints, rvec, tvec, cameraMatrix, distCoeffs, imagePoints, dpdr, dpdt,
     *                              dpdf, dpdc, dpdk is null
     */
    public static void projectPointsSepJ(
            Mat objectPoints,
            Mat rvec,
            Mat tvec,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat imagePoints,
            Mat dpdr,
            Mat dpdt,
            Mat dpdf,
            Mat dpdc,
            Mat dpdk) {
        projectPointsSepJ_2(
                objectPoints.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                imagePoints.nativeObj,
                dpdr.nativeObj,
                dpdt.nativeObj,
                dpdf.nativeObj,
                dpdc.nativeObj,
                dpdk.nativeObj);
    }

    /**
     * Projects three-dimensional object points into image coordinates and returns separately represented Jacobian data.
     *
     * @param objectPoints three-dimensional object coordinates expressed in the object reference frame
     * @param rvec         rotation vector in Rodrigues form
     * @param tvec         translation vector between object and camera coordinates
     * @param cameraMatrix 3-by-3 intrinsic camera matrix
     * @param distCoeffs   camera distortion coefficients
     * @param imagePoints  two-dimensional coordinates in the image plane
     * @param dpdr         output Jacobian of image coordinates with respect to rotation
     * @param dpdt         output Jacobian of image coordinates with respect to translation
     * @param dpdf         output Jacobian with respect to focal lengths
     * @param dpdc         output Jacobian with respect to the principal point
     * @throws NullPointerException if objectPoints, rvec, tvec, cameraMatrix, distCoeffs, imagePoints, dpdr, dpdt,
     *                              dpdf, dpdc is null
     */
    public static void projectPointsSepJ(
            Mat objectPoints,
            Mat rvec,
            Mat tvec,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat imagePoints,
            Mat dpdr,
            Mat dpdt,
            Mat dpdf,
            Mat dpdc) {
        projectPointsSepJ_3(
                objectPoints.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                imagePoints.nativeObj,
                dpdr.nativeObj,
                dpdt.nativeObj,
                dpdf.nativeObj,
                dpdc.nativeObj);
    }

    /**
     * Projects three-dimensional object points into image coordinates and returns separately represented Jacobian data.
     *
     * @param objectPoints three-dimensional object coordinates expressed in the object reference frame
     * @param rvec         rotation vector in Rodrigues form
     * @param tvec         translation vector between object and camera coordinates
     * @param cameraMatrix 3-by-3 intrinsic camera matrix
     * @param distCoeffs   camera distortion coefficients
     * @param imagePoints  two-dimensional coordinates in the image plane
     * @param dpdr         output Jacobian of image coordinates with respect to rotation
     * @param dpdt         output Jacobian of image coordinates with respect to translation
     * @param dpdf         output Jacobian with respect to focal lengths
     * @throws NullPointerException if objectPoints, rvec, tvec, cameraMatrix, distCoeffs, imagePoints, dpdr, dpdt, dpdf
     *                              is null
     */
    public static void projectPointsSepJ(
            Mat objectPoints,
            Mat rvec,
            Mat tvec,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat imagePoints,
            Mat dpdr,
            Mat dpdt,
            Mat dpdf) {
        projectPointsSepJ_4(
                objectPoints.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                imagePoints.nativeObj,
                dpdr.nativeObj,
                dpdt.nativeObj,
                dpdf.nativeObj);
    }

    /**
     * Projects three-dimensional object points into image coordinates and returns separately represented Jacobian data.
     *
     * @param objectPoints three-dimensional object coordinates expressed in the object reference frame
     * @param rvec         rotation vector in Rodrigues form
     * @param tvec         translation vector between object and camera coordinates
     * @param cameraMatrix 3-by-3 intrinsic camera matrix
     * @param distCoeffs   camera distortion coefficients
     * @param imagePoints  two-dimensional coordinates in the image plane
     * @param dpdr         output Jacobian of image coordinates with respect to rotation
     * @param dpdt         output Jacobian of image coordinates with respect to translation
     * @throws NullPointerException if objectPoints, rvec, tvec, cameraMatrix, distCoeffs, imagePoints, dpdr, dpdt is
     *                              null
     */
    public static void projectPointsSepJ(
            Mat objectPoints,
            Mat rvec,
            Mat tvec,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat imagePoints,
            Mat dpdr,
            Mat dpdt) {
        projectPointsSepJ_5(
                objectPoints.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                imagePoints.nativeObj,
                dpdr.nativeObj,
                dpdt.nativeObj);
    }

    /**
     * Estimates rotation and translation that map object-space three-dimensional points into the camera frame using
     * their image correspondences. The selected pose method determines the required point count and initialization.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the
     *                          zero distortion coefficients are assumed
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param flags             Method for solving a PnP problem: see REF: calib3d_solvePnP_flags More information about
     *                          Perspective-n-Points is described in REF: calib3d_solvePnP Note: An example of how to
     *                          use solvePnP for planar augmented reality can be found at
     *                          opencv_source_code/samples/python/plane_ar.py If you are using Python: Thus, given some
     *                          data D = np.array(...) where D.shape = (N,M), in order to use a subset of it as, e.g.,
     *                          imagePoints, one must effectively copy it into a new array: imagePoints =
     *                          np.ascontiguousarray(D[:,:2]).reshape((N,1,2)) The minimum number of points is 4 in the
     *                          general case. In the case of REF: SOLVEPNP_P3P and REF: SOLVEPNP_AP3P methods, it is
     *                          required to use exactly 4 points (the first 3 points are used to estimate all the
     *                          solutions of the P3P problem, the last one is used to retain the best solution that
     *                          minimizes the reprojection error). With REF: SOLVEPNP_ITERATIVE method and
     *                          useExtrinsicGuess=true, the minimum number of points is 3 (3 points are sufficient to
     *                          compute a pose but there are up to 4 solutions). The initial solution should be close to
     *                          the global solution to converge. The function returns true if some solution is found.
     *                          User code is responsible for solution quality assessment. With REF: SOLVEPNP_IPPE input
     *                          points must be &gt;= 4 and object points must be coplanar. With REF:
     *                          SOLVEPNP_IPPE_SQUARE this is a special case suitable for marker pose estimation. Number
     *                          of input points must be 4. Object points must be defined in the following order: point
     *                          0: [-squareLength / 2, squareLength / 2, 0] point 1: [ squareLength / 2, squareLength /
     *                          2, 0] point 2: [ squareLength / 2, -squareLength / 2, 0] point 3: [-squareLength / 2,
     *                          -squareLength / 2, 0] With REF: SOLVEPNP_SQPNP input points must be &gt;= 3
     * @return true if a pose is found
     * @throws NullPointerException if cameraMatrix, rvec, tvec is null
     */
    public static boolean solvePnP(
            MatOfPoint3f objectPoints,
            MatOfPoint2f imagePoints,
            Mat cameraMatrix,
            MatOfDouble distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess,
            int flags) {
        Mat objectPoints_mat = objectPoints;
        Mat imagePoints_mat = imagePoints;
        Mat distCoeffs_mat = distCoeffs;
        return solvePnP_0(
                objectPoints_mat.nativeObj,
                imagePoints_mat.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs_mat.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess,
                flags);
    }

    /**
     * Estimates rotation and translation that map object-space three-dimensional points into the camera frame using
     * their image correspondences. The selected pose method determines the required point count and initialization.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the
     *                          zero distortion coefficients are assumed
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @return true if a pose is found
     * @throws NullPointerException if cameraMatrix, rvec, tvec is null
     */
    public static boolean solvePnP(
            MatOfPoint3f objectPoints,
            MatOfPoint2f imagePoints,
            Mat cameraMatrix,
            MatOfDouble distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess) {
        Mat objectPoints_mat = objectPoints;
        Mat imagePoints_mat = imagePoints;
        Mat distCoeffs_mat = distCoeffs;
        return solvePnP_1(
                objectPoints_mat.nativeObj,
                imagePoints_mat.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs_mat.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess);
    }

    /**
     * Estimates rotation and translation that map object-space three-dimensional points into the camera frame using
     * their image correspondences. The selected pose method determines the required point count and initialization.
     *
     * @param objectPoints Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                     where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints  Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                     number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                     distortion coefficients are assumed
     * @param rvec         Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points from the
     *                     model coordinate system to the camera coordinate system
     * @param tvec         Output translation vector
     * @return true if a pose is found
     * @throws NullPointerException if cameraMatrix, rvec, tvec is null
     */
    public static boolean solvePnP(
            MatOfPoint3f objectPoints,
            MatOfPoint2f imagePoints,
            Mat cameraMatrix,
            MatOfDouble distCoeffs,
            Mat rvec,
            Mat tvec) {
        Mat objectPoints_mat = objectPoints;
        Mat imagePoints_mat = imagePoints;
        Mat distCoeffs_mat = distCoeffs;
        return solvePnP_2(
                objectPoints_mat.nativeObj,
                imagePoints_mat.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs_mat.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj);
    }

    /**
     * Estimates object-to-camera rotation and translation with robust rejection of incorrect point correspondences. The
     * inlier output identifies the accepted correspondences.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the
     *                          zero distortion coefficients are assumed
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for REF: SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                          rvec and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param iterationsCount   Number of iterations
     * @param reprojectionError reprojection-error threshold used to classify inliers
     * @param confidence        The probability that the algorithm produces a useful result
     * @param inliers           Output vector that contains indices of inliers in objectPoints and imagePoints
     * @param flags             Method for solving a PnP problem (see REF: solvePnP ). The function estimates an object
     *                          pose given a set of object points, their corresponding image projections, as well as the
     *                          camera intrinsic matrix and the distortion coefficients. This function finds such a pose
     *                          that minimizes reprojection error, that is, the sum of squared distances between the
     *                          observed projections imagePoints and the projected (using REF: projectPoints )
     *                          objectPoints. The use of RANSAC makes the function resistant to outliers. Note: An
     *                          example of how to use solvePnPRansac for object detection can be found at REF:
     *                          tutorial_real_time_pose The default method used to estimate the camera pose for the
     *                          Minimal Sample Sets step is SOLVEPNP_EPNP. Exceptions are: if you choose SOLVEPNP_P3P or
     *                          SOLVEPNP_AP3P, these methods will be used. if the number of input points is equal to 4,
     *                          SOLVEPNP_P3P is used. The method used to estimate the camera pose using all the inliers
     *                          is defined by the flags parameters unless it is equal to SOLVEPNP_P3P or SOLVEPNP_AP3P.
     *                          In this case, the method SOLVEPNP_EPNP will be used instead
     * @return true if robust pose estimation succeeds
     * @throws NullPointerException if cameraMatrix, rvec, tvec, inliers is null
     */
    public static boolean solvePnPRansac(
            MatOfPoint3f objectPoints,
            MatOfPoint2f imagePoints,
            Mat cameraMatrix,
            MatOfDouble distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError,
            double confidence,
            Mat inliers,
            int flags) {
        Mat objectPoints_mat = objectPoints;
        Mat imagePoints_mat = imagePoints;
        Mat distCoeffs_mat = distCoeffs;
        return solvePnPRansac_0(
                objectPoints_mat.nativeObj,
                imagePoints_mat.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs_mat.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess,
                iterationsCount,
                reprojectionError,
                confidence,
                inliers.nativeObj,
                flags);
    }

    /**
     * Estimates object-to-camera rotation and translation with robust rejection of incorrect point correspondences. The
     * inlier output identifies the accepted correspondences.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the
     *                          zero distortion coefficients are assumed
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for REF: SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                          rvec and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param iterationsCount   Number of iterations
     * @param reprojectionError reprojection-error threshold used to classify inliers
     * @param confidence        The probability that the algorithm produces a useful result
     * @param inliers           Output vector that contains indices of inliers in objectPoints and imagePoints
     * @return true if robust pose estimation succeeds
     * @throws NullPointerException if cameraMatrix, rvec, tvec, inliers is null
     */
    public static boolean solvePnPRansac(
            MatOfPoint3f objectPoints,
            MatOfPoint2f imagePoints,
            Mat cameraMatrix,
            MatOfDouble distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError,
            double confidence,
            Mat inliers) {
        Mat objectPoints_mat = objectPoints;
        Mat imagePoints_mat = imagePoints;
        Mat distCoeffs_mat = distCoeffs;
        return solvePnPRansac_1(
                objectPoints_mat.nativeObj,
                imagePoints_mat.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs_mat.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess,
                iterationsCount,
                reprojectionError,
                confidence,
                inliers.nativeObj);
    }

    /**
     * Estimates object-to-camera rotation and translation with robust rejection of incorrect point correspondences. The
     * inlier output identifies the accepted correspondences.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the
     *                          zero distortion coefficients are assumed
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for REF: SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                          rvec and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param iterationsCount   Number of iterations
     * @param reprojectionError reprojection-error threshold used to classify inliers
     * @param confidence        The probability that the algorithm produces a useful result
     * @return true if robust pose estimation succeeds
     * @throws NullPointerException if cameraMatrix, rvec, tvec is null
     */
    public static boolean solvePnPRansac(
            MatOfPoint3f objectPoints,
            MatOfPoint2f imagePoints,
            Mat cameraMatrix,
            MatOfDouble distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError,
            double confidence) {
        Mat objectPoints_mat = objectPoints;
        Mat imagePoints_mat = imagePoints;
        Mat distCoeffs_mat = distCoeffs;
        return solvePnPRansac_2(
                objectPoints_mat.nativeObj,
                imagePoints_mat.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs_mat.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess,
                iterationsCount,
                reprojectionError,
                confidence);
    }

    /**
     * Estimates object-to-camera rotation and translation with robust rejection of incorrect point correspondences. The
     * inlier output identifies the accepted correspondences.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the
     *                          zero distortion coefficients are assumed
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for REF: SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                          rvec and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param iterationsCount   Number of iterations
     * @param reprojectionError reprojection-error threshold used to classify inliers
     * @return true if robust pose estimation succeeds
     * @throws NullPointerException if cameraMatrix, rvec, tvec is null
     */
    public static boolean solvePnPRansac(
            MatOfPoint3f objectPoints,
            MatOfPoint2f imagePoints,
            Mat cameraMatrix,
            MatOfDouble distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError) {
        Mat objectPoints_mat = objectPoints;
        Mat imagePoints_mat = imagePoints;
        Mat distCoeffs_mat = distCoeffs;
        return solvePnPRansac_3(
                objectPoints_mat.nativeObj,
                imagePoints_mat.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs_mat.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess,
                iterationsCount,
                reprojectionError);
    }

    /**
     * Estimates object-to-camera rotation and translation with robust rejection of incorrect point correspondences. The
     * inlier output identifies the accepted correspondences.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the
     *                          zero distortion coefficients are assumed
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for REF: SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                          rvec and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param iterationsCount   Number of iterations
     * @return true if robust pose estimation succeeds
     * @throws NullPointerException if cameraMatrix, rvec, tvec is null
     */
    public static boolean solvePnPRansac(
            MatOfPoint3f objectPoints,
            MatOfPoint2f imagePoints,
            Mat cameraMatrix,
            MatOfDouble distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess,
            int iterationsCount) {
        Mat objectPoints_mat = objectPoints;
        Mat imagePoints_mat = imagePoints;
        Mat distCoeffs_mat = distCoeffs;
        return solvePnPRansac_4(
                objectPoints_mat.nativeObj,
                imagePoints_mat.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs_mat.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess,
                iterationsCount);
    }

    /**
     * Estimates object-to-camera rotation and translation with robust rejection of incorrect point correspondences. The
     * inlier output identifies the accepted correspondences.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the
     *                          zero distortion coefficients are assumed
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for REF: SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                          rvec and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @return true if robust pose estimation succeeds
     * @throws NullPointerException if cameraMatrix, rvec, tvec is null
     */
    public static boolean solvePnPRansac(
            MatOfPoint3f objectPoints,
            MatOfPoint2f imagePoints,
            Mat cameraMatrix,
            MatOfDouble distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess) {
        Mat objectPoints_mat = objectPoints;
        Mat imagePoints_mat = imagePoints;
        Mat distCoeffs_mat = distCoeffs;
        return solvePnPRansac_5(
                objectPoints_mat.nativeObj,
                imagePoints_mat.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs_mat.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess);
    }

    /**
     * Estimates object-to-camera rotation and translation with robust rejection of incorrect point correspondences. The
     * inlier output identifies the accepted correspondences.
     *
     * @param objectPoints Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                     where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints  Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                     number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                     distortion coefficients are assumed
     * @param rvec         Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points from the
     *                     model coordinate system to the camera coordinate system
     * @param tvec         Output translation vector
     * @return true if robust pose estimation succeeds
     * @throws NullPointerException if cameraMatrix, rvec, tvec is null
     */
    public static boolean solvePnPRansac(
            MatOfPoint3f objectPoints,
            MatOfPoint2f imagePoints,
            Mat cameraMatrix,
            MatOfDouble distCoeffs,
            Mat rvec,
            Mat tvec) {
        Mat objectPoints_mat = objectPoints;
        Mat imagePoints_mat = imagePoints;
        Mat distCoeffs_mat = distCoeffs;
        return solvePnPRansac_6(
                objectPoints_mat.nativeObj,
                imagePoints_mat.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs_mat.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj);
    }

    /**
     * Estimates object-to-camera rotation and translation with robust rejection of incorrect point correspondences. The
     * inlier output identifies the accepted correspondences.
     *
     * @param objectPoints Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                     where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints  Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                     number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                     distortion coefficients are assumed
     * @param rvec         Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points from the
     *                     model coordinate system to the camera coordinate system
     * @param tvec         Output translation vector
     * @param inliers      Output vector that contains indices of inliers in objectPoints and imagePoints
     * @param params       robust-estimation sampling, scoring, threshold and refinement settings
     * @return true if robust pose estimation succeeds
     * @throws NullPointerException if cameraMatrix, rvec, tvec, inliers, params is null
     */
    public static boolean solvePnPRansac(
            MatOfPoint3f objectPoints,
            MatOfPoint2f imagePoints,
            Mat cameraMatrix,
            MatOfDouble distCoeffs,
            Mat rvec,
            Mat tvec,
            Mat inliers,
            UsacParams params) {
        Mat objectPoints_mat = objectPoints;
        Mat imagePoints_mat = imagePoints;
        Mat distCoeffs_mat = distCoeffs;
        return solvePnPRansac_7(
                objectPoints_mat.nativeObj,
                imagePoints_mat.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs_mat.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                inliers.nativeObj,
                params.getNativeObjAddr());
    }

    /**
     * Estimates object-to-camera rotation and translation with robust rejection of incorrect point correspondences. The
     * inlier output identifies the accepted correspondences.
     *
     * @param objectPoints Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                     where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints  Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                     number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                     distortion coefficients are assumed
     * @param rvec         Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points from the
     *                     model coordinate system to the camera coordinate system
     * @param tvec         Output translation vector
     * @param inliers      Output vector that contains indices of inliers in objectPoints and imagePoints
     * @return true if robust pose estimation succeeds
     * @throws NullPointerException if cameraMatrix, rvec, tvec, inliers is null
     */
    public static boolean solvePnPRansac(
            MatOfPoint3f objectPoints,
            MatOfPoint2f imagePoints,
            Mat cameraMatrix,
            MatOfDouble distCoeffs,
            Mat rvec,
            Mat tvec,
            Mat inliers) {
        Mat objectPoints_mat = objectPoints;
        Mat imagePoints_mat = imagePoints;
        Mat distCoeffs_mat = distCoeffs;
        return solvePnPRansac_8(
                objectPoints_mat.nativeObj,
                imagePoints_mat.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs_mat.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                inliers.nativeObj);
    }

    /**
     * Solves the three-point perspective pose problem and returns the candidate object-to-camera rotations and
     * translations.
     *
     * @param objectPoints Array of object points in the object coordinate space, 3x3 1-channel or 1x3/3x1 3-channel.
     *                     vector&lt;Point3f&gt; can be also passed here
     * @param imagePoints  Array of corresponding image points, 3x2 1-channel or 1x3/3x1 2-channel.
     *                     vector&lt;Point2f&gt; can be also passed here
     * @param cameraMatrix Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                     distortion coefficients are assumed
     * @param rvecs        Output rotation vectors (see REF: Rodrigues ) that, together with tvecs, brings points from
     *                     the model coordinate system to the camera coordinate system. A P3P problem has up to 4
     *                     solutions
     * @param tvecs        Output translation vectors
     * @param flags        Method for solving a P3P problem: REF: SOLVEPNP_P3P Method is based on the paper of Ding, Y.,
     *                     Yang, J., Larsson, V., Olsson, C., &amp; u212bstrom, K. "Revisiting the P3P Problem" (). REF:
     *                     SOLVEPNP_AP3P Method is based on the paper of T. Ke and S. Roumeliotis. "An Efficient
     *                     Algebraic Solution to the Perspective-Three-Point Problem" (). The function estimates the
     *                     object pose given 3 object points, their corresponding image projections, as well as the
     *                     camera intrinsic matrix and the distortion coefficients. Note: The solutions are sorted by
     *                     reprojection errors (lowest to highest)
     * @return the number of returned pose solutions
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs is null
     */
    public static int solveP3P(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            List<Mat> rvecs,
            List<Mat> tvecs,
            int flags) {
        Mat rvecs_mat = new Mat();
        Mat tvecs_mat = new Mat();
        int retVal = solveP3P_0(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvecs_mat.nativeObj,
                tvecs_mat.nativeObj,
                flags);
        Converters.Mat_to_vector_Mat(rvecs_mat, rvecs);
        rvecs_mat.release();
        Converters.Mat_to_vector_Mat(tvecs_mat, tvecs);
        tvecs_mat.release();
        return retVal;
    }

    /**
     * Refine a pose (the translation and the rotation that transform a 3D point expressed in the object coordinate
     * frame to the camera coordinate frame) from a 3D-2D point correspondences and starting from an initial solution.
     *
     * @param objectPoints Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                     where N is the number of points. vector&lt;Point3d&gt; can also be passed here
     * @param imagePoints  Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                     number of points. vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                     distortion coefficients are assumed
     * @param rvec         Input/Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                     from the model coordinate system to the camera coordinate system. Input values are used as an
     *                     initial solution
     * @param tvec         Input/Output translation vector. Input values are used as an initial solution
     * @param criteria     Criteria when to stop the Levenberg-Marquard iterative algorithm. The function refines the
     *                     object pose given at least 3 object points, their corresponding image projections, an initial
     *                     solution for the rotation and translation vector, as well as the camera intrinsic matrix and
     *                     the distortion coefficients. The function minimizes the projection error with respect to the
     *                     rotation and the translation vectors, according to a Levenberg-Marquardt iterative
     *                     minimization process
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec, criteria is null
     */
    public static void solvePnPRefineLM(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec,
            TermCriteria criteria) {
        solvePnPRefineLM_0(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                criteria.type,
                criteria.maxCount,
                criteria.epsilon);
    }

    /**
     * Refine a pose (the translation and the rotation that transform a 3D point expressed in the object coordinate
     * frame to the camera coordinate frame) from a 3D-2D point correspondences and starting from an initial solution.
     *
     * @param objectPoints Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                     where N is the number of points. vector&lt;Point3d&gt; can also be passed here
     * @param imagePoints  Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                     number of points. vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                     distortion coefficients are assumed
     * @param rvec         Input/Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                     from the model coordinate system to the camera coordinate system. Input values are used as an
     *                     initial solution
     * @param tvec         Input/Output translation vector. Input values are used as an initial solution
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec is null
     */
    public static void solvePnPRefineLM(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec) {
        solvePnPRefineLM_1(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj);
    }

    /**
     * Refine a pose (the translation and the rotation that transform a 3D point expressed in the object coordinate
     * frame to the camera coordinate frame) from a 3D-2D point correspondences and starting from an initial solution.
     *
     * @param objectPoints Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                     where N is the number of points. vector&lt;Point3d&gt; can also be passed here
     * @param imagePoints  Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                     number of points. vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                     distortion coefficients are assumed
     * @param rvec         Input/Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                     from the model coordinate system to the camera coordinate system. Input values are used as an
     *                     initial solution
     * @param tvec         Input/Output translation vector. Input values are used as an initial solution
     * @param criteria     Criteria when to stop the Levenberg-Marquard iterative algorithm
     * @param VVSlambda    Gain for the virtual visual servoing control law, equivalent to the alpha gain in the Damped
     *                     Gauss-Newton formulation. The function refines the object pose given at least 3 object
     *                     points, their corresponding image projections, an initial solution for the rotation and
     *                     translation vector, as well as the camera intrinsic matrix and the distortion coefficients.
     *                     The function minimizes the projection error with respect to the rotation and the translation
     *                     vectors, using a virtual visual servoing (VVS) scheme
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec, criteria is null
     */
    public static void solvePnPRefineVVS(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec,
            TermCriteria criteria,
            double VVSlambda) {
        solvePnPRefineVVS_0(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                criteria.type,
                criteria.maxCount,
                criteria.epsilon,
                VVSlambda);
    }

    /**
     * Refine a pose (the translation and the rotation that transform a 3D point expressed in the object coordinate
     * frame to the camera coordinate frame) from a 3D-2D point correspondences and starting from an initial solution.
     *
     * @param objectPoints Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                     where N is the number of points. vector&lt;Point3d&gt; can also be passed here
     * @param imagePoints  Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                     number of points. vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                     distortion coefficients are assumed
     * @param rvec         Input/Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                     from the model coordinate system to the camera coordinate system. Input values are used as an
     *                     initial solution
     * @param tvec         Input/Output translation vector. Input values are used as an initial solution
     * @param criteria     Criteria when to stop the Levenberg-Marquard iterative algorithm
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec, criteria is null
     */
    public static void solvePnPRefineVVS(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec,
            TermCriteria criteria) {
        solvePnPRefineVVS_1(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                criteria.type,
                criteria.maxCount,
                criteria.epsilon);
    }

    /**
     * Refine a pose (the translation and the rotation that transform a 3D point expressed in the object coordinate
     * frame to the camera coordinate frame) from a 3D-2D point correspondences and starting from an initial solution.
     *
     * @param objectPoints Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                     where N is the number of points. vector&lt;Point3d&gt; can also be passed here
     * @param imagePoints  Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                     number of points. vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                     distortion coefficients are assumed
     * @param rvec         Input/Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                     from the model coordinate system to the camera coordinate system. Input values are used as an
     *                     initial solution
     * @param tvec         Input/Output translation vector. Input values are used as an initial solution
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec is null
     */
    public static void solvePnPRefineVVS(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec) {
        solvePnPRefineVVS_2(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj);
    }

    /**
     * Estimates the candidate object-to-camera poses supported by the selected perspective pose method and optionally
     * reports their reprojection errors.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the
     *                          zero distortion coefficients are assumed
     * @param rvecs             Vector of output rotation vectors (see REF: Rodrigues ) that, together with tvecs,
     *                          brings points from the model coordinate system to the camera coordinate system
     * @param tvecs             Vector of output translation vectors
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param flags             Method for solving a PnP problem: see REF: calib3d_solvePnP_flags
     * @param rvec              Rotation vector used to initialize an iterative PnP refinement algorithm, when flag is
     *                          REF: SOLVEPNP_ITERATIVE and useExtrinsicGuess is set to true
     * @param tvec              Translation vector used to initialize an iterative PnP refinement algorithm, when flag
     *                          is REF: SOLVEPNP_ITERATIVE and useExtrinsicGuess is set to true
     * @param reprojectionError output per-solution reprojection errors
     * @return the number of returned pose solutions
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec,
     *                              reprojectionError is null
     */
    public static int solvePnPGeneric(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            List<Mat> rvecs,
            List<Mat> tvecs,
            boolean useExtrinsicGuess,
            int flags,
            Mat rvec,
            Mat tvec,
            Mat reprojectionError) {
        Mat rvecs_mat = new Mat();
        Mat tvecs_mat = new Mat();
        int retVal = solvePnPGeneric_0(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvecs_mat.nativeObj,
                tvecs_mat.nativeObj,
                useExtrinsicGuess,
                flags,
                rvec.nativeObj,
                tvec.nativeObj,
                reprojectionError.nativeObj);
        Converters.Mat_to_vector_Mat(rvecs_mat, rvecs);
        rvecs_mat.release();
        Converters.Mat_to_vector_Mat(tvecs_mat, tvecs);
        tvecs_mat.release();
        return retVal;
    }

    /**
     * Estimates the candidate object-to-camera poses supported by the selected perspective pose method and optionally
     * reports their reprojection errors.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the
     *                          zero distortion coefficients are assumed
     * @param rvecs             Vector of output rotation vectors (see REF: Rodrigues ) that, together with tvecs,
     *                          brings points from the model coordinate system to the camera coordinate system
     * @param tvecs             Vector of output translation vectors
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param flags             Method for solving a PnP problem: see REF: calib3d_solvePnP_flags
     * @param rvec              Rotation vector used to initialize an iterative PnP refinement algorithm, when flag is
     *                          REF: SOLVEPNP_ITERATIVE and useExtrinsicGuess is set to true
     * @param tvec              Translation vector used to initialize an iterative PnP refinement algorithm, when flag
     *                          is REF: SOLVEPNP_ITERATIVE and useExtrinsicGuess is set to true
     * @return the number of returned pose solutions
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec is null
     */
    public static int solvePnPGeneric(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            List<Mat> rvecs,
            List<Mat> tvecs,
            boolean useExtrinsicGuess,
            int flags,
            Mat rvec,
            Mat tvec) {
        Mat rvecs_mat = new Mat();
        Mat tvecs_mat = new Mat();
        int retVal = solvePnPGeneric_1(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvecs_mat.nativeObj,
                tvecs_mat.nativeObj,
                useExtrinsicGuess,
                flags,
                rvec.nativeObj,
                tvec.nativeObj);
        Converters.Mat_to_vector_Mat(rvecs_mat, rvecs);
        rvecs_mat.release();
        Converters.Mat_to_vector_Mat(tvecs_mat, tvecs);
        tvecs_mat.release();
        return retVal;
    }

    /**
     * Estimates the candidate object-to-camera poses supported by the selected perspective pose method and optionally
     * reports their reprojection errors.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the
     *                          zero distortion coefficients are assumed
     * @param rvecs             Vector of output rotation vectors (see REF: Rodrigues ) that, together with tvecs,
     *                          brings points from the model coordinate system to the camera coordinate system
     * @param tvecs             Vector of output translation vectors
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param flags             Method for solving a PnP problem: see REF: calib3d_solvePnP_flags
     * @param rvec              Rotation vector used to initialize an iterative PnP refinement algorithm, when flag is
     *                          REF: SOLVEPNP_ITERATIVE and useExtrinsicGuess is set to true
     * @return the number of returned pose solutions
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec is null
     */
    public static int solvePnPGeneric(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            List<Mat> rvecs,
            List<Mat> tvecs,
            boolean useExtrinsicGuess,
            int flags,
            Mat rvec) {
        Mat rvecs_mat = new Mat();
        Mat tvecs_mat = new Mat();
        int retVal = solvePnPGeneric_2(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvecs_mat.nativeObj,
                tvecs_mat.nativeObj,
                useExtrinsicGuess,
                flags,
                rvec.nativeObj);
        Converters.Mat_to_vector_Mat(rvecs_mat, rvecs);
        rvecs_mat.release();
        Converters.Mat_to_vector_Mat(tvecs_mat, tvecs);
        tvecs_mat.release();
        return retVal;
    }

    /**
     * Estimates the candidate object-to-camera poses supported by the selected perspective pose method and optionally
     * reports their reprojection errors.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the
     *                          zero distortion coefficients are assumed
     * @param rvecs             Vector of output rotation vectors (see REF: Rodrigues ) that, together with tvecs,
     *                          brings points from the model coordinate system to the camera coordinate system
     * @param tvecs             Vector of output translation vectors
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param flags             Method for solving a PnP problem: see REF: calib3d_solvePnP_flags
     * @return the number of returned pose solutions
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs is null
     */
    public static int solvePnPGeneric(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            List<Mat> rvecs,
            List<Mat> tvecs,
            boolean useExtrinsicGuess,
            int flags) {
        Mat rvecs_mat = new Mat();
        Mat tvecs_mat = new Mat();
        int retVal = solvePnPGeneric_3(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvecs_mat.nativeObj,
                tvecs_mat.nativeObj,
                useExtrinsicGuess,
                flags);
        Converters.Mat_to_vector_Mat(rvecs_mat, rvecs);
        rvecs_mat.release();
        Converters.Mat_to_vector_Mat(tvecs_mat, tvecs);
        tvecs_mat.release();
        return retVal;
    }

    /**
     * Estimates the candidate object-to-camera poses supported by the selected perspective pose method and optionally
     * reports their reprojection errors.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the
     *                          zero distortion coefficients are assumed
     * @param rvecs             Vector of output rotation vectors (see REF: Rodrigues ) that, together with tvecs,
     *                          brings points from the model coordinate system to the camera coordinate system
     * @param tvecs             Vector of output translation vectors
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @return the number of returned pose solutions
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs is null
     */
    public static int solvePnPGeneric(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            List<Mat> rvecs,
            List<Mat> tvecs,
            boolean useExtrinsicGuess) {
        Mat rvecs_mat = new Mat();
        Mat tvecs_mat = new Mat();
        int retVal = solvePnPGeneric_4(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvecs_mat.nativeObj,
                tvecs_mat.nativeObj,
                useExtrinsicGuess);
        Converters.Mat_to_vector_Mat(rvecs_mat, rvecs);
        rvecs_mat.release();
        Converters.Mat_to_vector_Mat(tvecs_mat, tvecs);
        tvecs_mat.release();
        return retVal;
    }

    /**
     * Estimates the candidate object-to-camera poses supported by the selected perspective pose method and optionally
     * reports their reprojection errors.
     *
     * @param objectPoints Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                     where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints  Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                     number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                     distortion coefficients are assumed
     * @param rvecs        Vector of output rotation vectors (see REF: Rodrigues ) that, together with tvecs, brings
     *                     points from the model coordinate system to the camera coordinate system
     * @param tvecs        Vector of output translation vectors
     * @return the number of returned pose solutions
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs is null
     */
    public static int solvePnPGeneric(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            List<Mat> rvecs,
            List<Mat> tvecs) {
        Mat rvecs_mat = new Mat();
        Mat tvecs_mat = new Mat();
        int retVal = solvePnPGeneric_5(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvecs_mat.nativeObj,
                tvecs_mat.nativeObj);
        Converters.Mat_to_vector_Mat(rvecs_mat, rvecs);
        rvecs_mat.release();
        Converters.Mat_to_vector_Mat(tvecs_mat, tvecs);
        tvecs_mat.release();
        return retVal;
    }

    /**
     * Converts points from Euclidean to homogeneous space.
     *
     * @param src   Input vector of N-dimensional points
     * @param dst   Output vector of N+1-dimensional points
     * @param dtype The desired output array depth (either CV_32F or CV_64F are currently supported). If it's -1, then
     *              it's set automatically to CV_32F or CV_64F, depending on the input depth. The function converts
     *              points from Euclidean to homogeneous space by appending 1's to the tuple of point coordinates. That
     *              is, each point (x1, x2, ..., xn) is converted to (x1, x2, ..., xn, 1)
     * @throws NullPointerException if src, dst is null
     */
    public static void convertPointsToHomogeneous(Mat src, Mat dst, int dtype) {
        convertPointsToHomogeneous_0(src.nativeObj, dst.nativeObj, dtype);
    }

    /**
     * Converts points from Euclidean to homogeneous space.
     *
     * @param src Input vector of N-dimensional points
     * @param dst Output vector of N+1-dimensional points
     * @throws NullPointerException if src, dst is null
     */
    public static void convertPointsToHomogeneous(Mat src, Mat dst) {
        convertPointsToHomogeneous_1(src.nativeObj, dst.nativeObj);
    }

    /**
     * Converts points from homogeneous to Euclidean space.
     *
     * @param src   Input vector of N-dimensional points
     * @param dst   Output vector of N-1-dimensional points
     * @param dtype The desired output array depth (either CV_32F or CV_64F are currently supported). If it's -1, then
     *              it's set automatically to CV_32F or CV_64F, depending on the input depth. The function converts
     *              points homogeneous to Euclidean space using perspective projection. That is, each point (x1, x2, ...
     *              x(n-1), xn) is converted to (x1/xn, x2/xn, ..., x(n-1)/xn). When xn=0, the output point coordinates
     *              will be (0,0,0,...)
     * @throws NullPointerException if src, dst is null
     */
    public static void convertPointsFromHomogeneous(Mat src, Mat dst, int dtype) {
        convertPointsFromHomogeneous_0(src.nativeObj, dst.nativeObj, dtype);
    }

    /**
     * Converts points from homogeneous to Euclidean space.
     *
     * @param src Input vector of N-dimensional points
     * @param dst Output vector of N-1-dimensional points
     * @throws NullPointerException if src, dst is null
     */
    public static void convertPointsFromHomogeneous(Mat src, Mat dst) {
        convertPointsFromHomogeneous_1(src.nativeObj, dst.nativeObj);
    }

    /**
     * Calculates a fundamental matrix from the corresponding points in two images.
     *
     * @param points1               Array of N points from the first image. The point coordinates should be
     *                              floating-point (single or double precision)
     * @param points2               Array of the second image points of the same size and format as points1
     * @param method                Method for computing a fundamental matrix. REF: FM_7POINT for a 7-point algorithm. N
     *                              = 7 REF: FM_8POINT for an 8-point algorithm. N ge 8 REF: FM_RANSAC for the RANSAC
     *                              algorithm. N ge 8 REF: FM_LMEDS for the LMedS algorithm. N ge 8
     * @param ransacReprojThreshold Parameter used only for RANSAC. It is the maximum distance from a point to an
     *                              epipolar line in pixels, beyond which the point is considered an outlier and is not
     *                              used for computing the final fundamental matrix. It can be set to something like
     *                              1-3, depending on the accuracy of the point localization, image resolution, and the
     *                              image noise
     * @param confidence            Parameter used for the RANSAC and LMedS methods only. It specifies a desirable level
     *                              of confidence (probability) that the estimated matrix is correct
     * @param maxIters              The maximum number of robust method iterations. The epipolar geometry is described
     *                              by the following equation: [p_2; 1]^T F [p_1; 1] = 0 where F is a fundamental
     *                              matrix, p_1 and p_2 are corresponding points in the first and the second images,
     *                              respectively. The function calculates the fundamental matrix using one of four
     *                              methods listed above and returns the found fundamental matrix. Normally just one
     *                              matrix is found. But in case of the 7-point algorithm, the function may return up to
     *                              3 solutions ( 9 times 3 matrix that stores all 3 matrices sequentially). The
     *                              calculated fundamental matrix may be passed further to computeCorrespondEpilines
     *                              that finds the epipolar lines corresponding to the specified points. It can also be
     *                              passed to stereoRectifyUncalibrated to compute the rectification transformation. :
     *                              // Example. Estimation of fundamental matrix using the RANSAC algorithm int
     *                              point_count = 100; vector&lt;Point2f&gt; points1(point_count); vector&lt;Point2f&gt;
     *                              points2(point_count); // initialize the points here ... for( int i = 0; i &lt;
     *                              point_count; i++ ) { points1[i] = ...; points2[i] = ...; } Mat fundamental_matrix =
     *                              findFundamentalMat(points1, points2, FM_RANSAC, 3, 0.99);
     * @param mask                  optional output mask
     * @return the estimated fundamental matrix or candidate matrices, according to method
     * @throws NullPointerException if mask is null
     */
    public static Mat findFundamentalMat(
            MatOfPoint2f points1,
            MatOfPoint2f points2,
            int method,
            double ransacReprojThreshold,
            double confidence,
            int maxIters,
            Mat mask) {
        Mat points1_mat = points1;
        Mat points2_mat = points2;
        return new Mat(findFundamentalMat_0(
                points1_mat.nativeObj,
                points2_mat.nativeObj,
                method,
                ransacReprojThreshold,
                confidence,
                maxIters,
                mask.nativeObj));
    }

    /**
     * Calculates a fundamental matrix from the corresponding points in two images.
     *
     * @param points1               Array of N points from the first image. The point coordinates should be
     *                              floating-point (single or double precision)
     * @param points2               Array of the second image points of the same size and format as points1
     * @param method                Method for computing a fundamental matrix. REF: FM_7POINT for a 7-point algorithm. N
     *                              = 7 REF: FM_8POINT for an 8-point algorithm. N ge 8 REF: FM_RANSAC for the RANSAC
     *                              algorithm. N ge 8 REF: FM_LMEDS for the LMedS algorithm. N ge 8
     * @param ransacReprojThreshold Parameter used only for RANSAC. It is the maximum distance from a point to an
     *                              epipolar line in pixels, beyond which the point is considered an outlier and is not
     *                              used for computing the final fundamental matrix. It can be set to something like
     *                              1-3, depending on the accuracy of the point localization, image resolution, and the
     *                              image noise
     * @param confidence            Parameter used for the RANSAC and LMedS methods only. It specifies a desirable level
     *                              of confidence (probability) that the estimated matrix is correct
     * @param maxIters              The maximum number of robust method iterations. The epipolar geometry is described
     *                              by the following equation: [p_2; 1]^T F [p_1; 1] = 0 where F is a fundamental
     *                              matrix, p_1 and p_2 are corresponding points in the first and the second images,
     *                              respectively. The function calculates the fundamental matrix using one of four
     *                              methods listed above and returns the found fundamental matrix. Normally just one
     *                              matrix is found. But in case of the 7-point algorithm, the function may return up to
     *                              3 solutions ( 9 times 3 matrix that stores all 3 matrices sequentially). The
     *                              calculated fundamental matrix may be passed further to computeCorrespondEpilines
     *                              that finds the epipolar lines corresponding to the specified points. It can also be
     *                              passed to stereoRectifyUncalibrated to compute the rectification transformation. :
     *                              // Example. Estimation of fundamental matrix using the RANSAC algorithm int
     *                              point_count = 100; vector&lt;Point2f&gt; points1(point_count); vector&lt;Point2f&gt;
     *                              points2(point_count); // initialize the points here ... for( int i = 0; i &lt;
     *                              point_count; i++ ) { points1[i] = ...; points2[i] = ...; } Mat fundamental_matrix =
     *                              findFundamentalMat(points1, points2, FM_RANSAC, 3, 0.99);
     * @return the estimated fundamental matrix or candidate matrices, according to method
     */
    public static Mat findFundamentalMat(
            MatOfPoint2f points1,
            MatOfPoint2f points2,
            int method,
            double ransacReprojThreshold,
            double confidence,
            int maxIters) {
        Mat points1_mat = points1;
        Mat points2_mat = points2;
        return new Mat(findFundamentalMat_1(
                points1_mat.nativeObj,
                points2_mat.nativeObj,
                method,
                ransacReprojThreshold,
                confidence,
                maxIters));
    }

    /**
     * Calculates a fundamental matrix from the corresponding points in two images.
     *
     * @param points1               Array of N points from the first image. The point coordinates should be
     *                              floating-point (single or double precision)
     * @param points2               Array of the second image points of the same size and format as points1
     * @param method                Method for computing a fundamental matrix. REF: FM_7POINT for a 7-point algorithm. N
     *                              = 7 REF: FM_8POINT for an 8-point algorithm. N ge 8 REF: FM_RANSAC for the RANSAC
     *                              algorithm. N ge 8 REF: FM_LMEDS for the LMedS algorithm. N ge 8
     * @param ransacReprojThreshold Parameter used only for RANSAC. It is the maximum distance from a point to an
     *                              epipolar line in pixels, beyond which the point is considered an outlier and is not
     *                              used for computing the final fundamental matrix. It can be set to something like
     *                              1-3, depending on the accuracy of the point localization, image resolution, and the
     *                              image noise
     * @param confidence            Parameter used for the RANSAC and LMedS methods only. It specifies a desirable level
     *                              of confidence (probability) that the estimated matrix is correct
     * @param mask                  optional output mask
     * @return the estimated fundamental matrix or candidate matrices, according to method
     * @throws NullPointerException if mask is null
     */
    public static Mat findFundamentalMat(
            MatOfPoint2f points1,
            MatOfPoint2f points2,
            int method,
            double ransacReprojThreshold,
            double confidence,
            Mat mask) {
        Mat points1_mat = points1;
        Mat points2_mat = points2;
        return new Mat(findFundamentalMat_2(
                points1_mat.nativeObj,
                points2_mat.nativeObj,
                method,
                ransacReprojThreshold,
                confidence,
                mask.nativeObj));
    }

    /**
     * Calculates a fundamental matrix from the corresponding points in two images.
     *
     * @param points1               Array of N points from the first image. The point coordinates should be
     *                              floating-point (single or double precision)
     * @param points2               Array of the second image points of the same size and format as points1
     * @param method                Method for computing a fundamental matrix. REF: FM_7POINT for a 7-point algorithm. N
     *                              = 7 REF: FM_8POINT for an 8-point algorithm. N ge 8 REF: FM_RANSAC for the RANSAC
     *                              algorithm. N ge 8 REF: FM_LMEDS for the LMedS algorithm. N ge 8
     * @param ransacReprojThreshold Parameter used only for RANSAC. It is the maximum distance from a point to an
     *                              epipolar line in pixels, beyond which the point is considered an outlier and is not
     *                              used for computing the final fundamental matrix. It can be set to something like
     *                              1-3, depending on the accuracy of the point localization, image resolution, and the
     *                              image noise
     * @param confidence            Parameter used for the RANSAC and LMedS methods only. It specifies a desirable level
     *                              of confidence (probability) that the estimated matrix is correct
     * @return the estimated fundamental matrix or candidate matrices, according to method
     */
    public static Mat findFundamentalMat(
            MatOfPoint2f points1,
            MatOfPoint2f points2,
            int method,
            double ransacReprojThreshold,
            double confidence) {
        Mat points1_mat = points1;
        Mat points2_mat = points2;
        return new Mat(findFundamentalMat_3(
                points1_mat.nativeObj,
                points2_mat.nativeObj,
                method,
                ransacReprojThreshold,
                confidence));
    }

    /**
     * Calculates a fundamental matrix from the corresponding points in two images.
     *
     * @param points1               Array of N points from the first image. The point coordinates should be
     *                              floating-point (single or double precision)
     * @param points2               Array of the second image points of the same size and format as points1
     * @param method                Method for computing a fundamental matrix. REF: FM_7POINT for a 7-point algorithm. N
     *                              = 7 REF: FM_8POINT for an 8-point algorithm. N ge 8 REF: FM_RANSAC for the RANSAC
     *                              algorithm. N ge 8 REF: FM_LMEDS for the LMedS algorithm. N ge 8
     * @param ransacReprojThreshold Parameter used only for RANSAC. It is the maximum distance from a point to an
     *                              epipolar line in pixels, beyond which the point is considered an outlier and is not
     *                              used for computing the final fundamental matrix. It can be set to something like
     *                              1-3, depending on the accuracy of the point localization, image resolution, and the
     *                              image noise
     * @return the estimated fundamental matrix or candidate matrices, according to method
     */
    public static Mat findFundamentalMat(
            MatOfPoint2f points1,
            MatOfPoint2f points2,
            int method,
            double ransacReprojThreshold) {
        Mat points1_mat = points1;
        Mat points2_mat = points2;
        return new Mat(
                findFundamentalMat_4(points1_mat.nativeObj, points2_mat.nativeObj, method, ransacReprojThreshold));
    }

    /**
     * Calculates a fundamental matrix from the corresponding points in two images.
     *
     * @param points1 Array of N points from the first image. The point coordinates should be floating-point (single or
     *                double precision)
     * @param points2 Array of the second image points of the same size and format as points1
     * @param method  Method for computing a fundamental matrix. REF: FM_7POINT for a 7-point algorithm. N = 7 REF:
     *                FM_8POINT for an 8-point algorithm. N ge 8 REF: FM_RANSAC for the RANSAC algorithm. N ge 8 REF:
     *                FM_LMEDS for the LMedS algorithm. N ge 8
     * @return the estimated fundamental matrix or candidate matrices, according to method
     */
    public static Mat findFundamentalMat(MatOfPoint2f points1, MatOfPoint2f points2, int method) {
        Mat points1_mat = points1;
        Mat points2_mat = points2;
        return new Mat(findFundamentalMat_5(points1_mat.nativeObj, points2_mat.nativeObj, method));
    }

    /**
     * Calculates a fundamental matrix from the corresponding points in two images.
     *
     * @param points1 Array of N points from the first image. The point coordinates should be floating-point (single or
     *                double precision)
     * @param points2 Array of the second image points of the same size and format as points1
     * @return the estimated fundamental matrix or candidate matrices, according to method
     */
    public static Mat findFundamentalMat(MatOfPoint2f points1, MatOfPoint2f points2) {
        Mat points1_mat = points1;
        Mat points2_mat = points2;
        return new Mat(findFundamentalMat_6(points1_mat.nativeObj, points2_mat.nativeObj));
    }

    /**
     * Calculates a fundamental matrix from the corresponding points in two images.
     *
     * @param points1 Array of N points from the first image. The point coordinates should be floating-point (single or
     *                double precision)
     * @param points2 Array of the second image points of the same size and format as points1
     * @param mask    optional output mask
     * @param params  robust-estimation sampling, scoring, threshold and refinement settings
     * @return the estimated fundamental matrix or candidate matrices, according to method
     * @throws NullPointerException if mask, params is null
     */
    public static Mat findFundamentalMat(MatOfPoint2f points1, MatOfPoint2f points2, Mat mask, UsacParams params) {
        Mat points1_mat = points1;
        Mat points2_mat = points2;
        return new Mat(findFundamentalMat_7(
                points1_mat.nativeObj,
                points2_mat.nativeObj,
                mask.nativeObj,
                params.getNativeObjAddr()));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1      Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                     floating-point (single or double precision)
     * @param points2      Array of the second image points of the same size and format as points1
     * @param cameraMatrix Camera intrinsic matrix cameramatrix{A} . Note that this function assumes that points1 and
     *                     points2 are feature points from cameras with the same camera intrinsic matrix. If this
     *                     assumption does not hold for your use case, use another function overload or undistortPoints
     *                     with for both cameras to transform image points to normalized image coordinates, which are
     *                     valid for the identity camera intrinsic matrix. When passing these coordinates, pass the
     *                     identity matrix for this parameter
     * @param method       Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS
     *                     for the LMedS algorithm
     * @param prob         Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                     confidence (probability) that the estimated matrix is correct
     * @param threshold    Parameter used for RANSAC. It is the maximum distance from a point to an epipolar line in
     *                     pixels, beyond which the point is considered an outlier and is not used for computing the
     *                     final fundamental matrix. It can be set to something like 1-3, depending on the accuracy of
     *                     the point localization, image resolution, and the image noise
     * @param maxIters     The maximum number of robust method iterations. This function estimates essential matrix
     *                     based on the five-point algorithm solver in is also a related. The epipolar geometry is
     *                     described by the following equation: [p_2; 1]^T K^{-T} E K^{-1} [p_1; 1] = 0 where E is an
     *                     essential matrix, p_1 and p_2 are corresponding points in the first and the second images,
     *                     respectively. The result of this function may be passed further to decomposeEssentialMat or
     *                     recoverPose to recover the relative pose between cameras
     * @param mask         Output array of N elements, every element of which is set to 0 for outliers and to 1 for the
     *                     other points. The array is computed only in the RANSAC and LMedS methods. This function
     *                     estimates essential matrix based on the five-point algorithm solver in is also a related. The
     *                     epipolar geometry is described by the following equation: [p_2; 1]^T K^{-T} E K^{-1} [p_1; 1]
     *                     = 0 where E is an essential matrix, p_1 and p_2 are corresponding points in the first and the
     *                     second images, respectively. The result of this function may be passed further to
     *                     decomposeEssentialMat or recoverPose to recover the relative pose between cameras
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, cameraMatrix, mask is null
     */
    public static Mat findEssentialMat(
            Mat points1,
            Mat points2,
            Mat cameraMatrix,
            int method,
            double prob,
            double threshold,
            int maxIters,
            Mat mask) {
        return new Mat(findEssentialMat_0(
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix.nativeObj,
                method,
                prob,
                threshold,
                maxIters,
                mask.nativeObj));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1      Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                     floating-point (single or double precision)
     * @param points2      Array of the second image points of the same size and format as points1
     * @param cameraMatrix Camera intrinsic matrix cameramatrix{A} . Note that this function assumes that points1 and
     *                     points2 are feature points from cameras with the same camera intrinsic matrix. If this
     *                     assumption does not hold for your use case, use another function overload or undistortPoints
     *                     with for both cameras to transform image points to normalized image coordinates, which are
     *                     valid for the identity camera intrinsic matrix. When passing these coordinates, pass the
     *                     identity matrix for this parameter
     * @param method       Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS
     *                     for the LMedS algorithm
     * @param prob         Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                     confidence (probability) that the estimated matrix is correct
     * @param threshold    Parameter used for RANSAC. It is the maximum distance from a point to an epipolar line in
     *                     pixels, beyond which the point is considered an outlier and is not used for computing the
     *                     final fundamental matrix. It can be set to something like 1-3, depending on the accuracy of
     *                     the point localization, image resolution, and the image noise
     * @param maxIters     The maximum number of robust method iterations. This function estimates essential matrix
     *                     based on the five-point algorithm solver in is also a related. The epipolar geometry is
     *                     described by the following equation: [p_2; 1]^T K^{-T} E K^{-1} [p_1; 1] = 0 where E is an
     *                     essential matrix, p_1 and p_2 are corresponding points in the first and the second images,
     *                     respectively. The result of this function may be passed further to decomposeEssentialMat or
     *                     recoverPose to recover the relative pose between cameras
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, cameraMatrix is null
     */
    public static Mat findEssentialMat(
            Mat points1,
            Mat points2,
            Mat cameraMatrix,
            int method,
            double prob,
            double threshold,
            int maxIters) {
        return new Mat(findEssentialMat_1(
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix.nativeObj,
                method,
                prob,
                threshold,
                maxIters));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1      Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                     floating-point (single or double precision)
     * @param points2      Array of the second image points of the same size and format as points1
     * @param cameraMatrix Camera intrinsic matrix cameramatrix{A} . Note that this function assumes that points1 and
     *                     points2 are feature points from cameras with the same camera intrinsic matrix. If this
     *                     assumption does not hold for your use case, use another function overload or undistortPoints
     *                     with for both cameras to transform image points to normalized image coordinates, which are
     *                     valid for the identity camera intrinsic matrix. When passing these coordinates, pass the
     *                     identity matrix for this parameter
     * @param method       Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS
     *                     for the LMedS algorithm
     * @param prob         Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                     confidence (probability) that the estimated matrix is correct
     * @param threshold    Parameter used for RANSAC. It is the maximum distance from a point to an epipolar line in
     *                     pixels, beyond which the point is considered an outlier and is not used for computing the
     *                     final fundamental matrix. It can be set to something like 1-3, depending on the accuracy of
     *                     the point localization, image resolution, and the image noise
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, cameraMatrix is null
     */
    public static Mat findEssentialMat(
            Mat points1,
            Mat points2,
            Mat cameraMatrix,
            int method,
            double prob,
            double threshold) {
        return new Mat(findEssentialMat_2(
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix.nativeObj,
                method,
                prob,
                threshold));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1      Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                     floating-point (single or double precision)
     * @param points2      Array of the second image points of the same size and format as points1
     * @param cameraMatrix Camera intrinsic matrix cameramatrix{A} . Note that this function assumes that points1 and
     *                     points2 are feature points from cameras with the same camera intrinsic matrix. If this
     *                     assumption does not hold for your use case, use another function overload or undistortPoints
     *                     with for both cameras to transform image points to normalized image coordinates, which are
     *                     valid for the identity camera intrinsic matrix. When passing these coordinates, pass the
     *                     identity matrix for this parameter
     * @param method       Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS
     *                     for the LMedS algorithm
     * @param prob         Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                     confidence (probability) that the estimated matrix is correct
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, cameraMatrix is null
     */
    public static Mat findEssentialMat(Mat points1, Mat points2, Mat cameraMatrix, int method, double prob) {
        return new Mat(findEssentialMat_3(points1.nativeObj, points2.nativeObj, cameraMatrix.nativeObj, method, prob));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1      Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                     floating-point (single or double precision)
     * @param points2      Array of the second image points of the same size and format as points1
     * @param cameraMatrix Camera intrinsic matrix cameramatrix{A} . Note that this function assumes that points1 and
     *                     points2 are feature points from cameras with the same camera intrinsic matrix. If this
     *                     assumption does not hold for your use case, use another function overload or undistortPoints
     *                     with for both cameras to transform image points to normalized image coordinates, which are
     *                     valid for the identity camera intrinsic matrix. When passing these coordinates, pass the
     *                     identity matrix for this parameter
     * @param method       Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS
     *                     for the LMedS algorithm
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, cameraMatrix is null
     */
    public static Mat findEssentialMat(Mat points1, Mat points2, Mat cameraMatrix, int method) {
        return new Mat(findEssentialMat_4(points1.nativeObj, points2.nativeObj, cameraMatrix.nativeObj, method));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1      Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                     floating-point (single or double precision)
     * @param points2      Array of the second image points of the same size and format as points1
     * @param cameraMatrix Camera intrinsic matrix cameramatrix{A} . Note that this function assumes that points1 and
     *                     points2 are feature points from cameras with the same camera intrinsic matrix. If this
     *                     assumption does not hold for your use case, use another function overload or undistortPoints
     *                     with for both cameras to transform image points to normalized image coordinates, which are
     *                     valid for the identity camera intrinsic matrix. When passing these coordinates, pass the
     *                     identity matrix for this parameter. REF: RANSAC for the RANSAC algorithm. REF: LMEDS for the
     *                     LMedS algorithm. confidence (probability) that the estimated matrix is correct. line in
     *                     pixels, beyond which the point is considered an outlier and is not used for computing the
     *                     final fundamental matrix. It can be set to something like 1-3, depending on the accuracy of
     *                     the point localization, image resolution, and the image noise. for the other points. The
     *                     array is computed only in the RANSAC and LMedS methods. This function estimates essential
     *                     matrix based on the five-point algorithm solver in is also a related. The epipolar geometry
     *                     is described by the following equation: [p_2; 1]^T K^{-T} E K^{-1} [p_1; 1] = 0 where E is an
     *                     essential matrix, p_1 and p_2 are corresponding points in the first and the second images,
     *                     respectively. The result of this function may be passed further to decomposeEssentialMat or
     *                     recoverPose to recover the relative pose between cameras
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, cameraMatrix is null
     */
    public static Mat findEssentialMat(Mat points1, Mat points2, Mat cameraMatrix) {
        return new Mat(findEssentialMat_5(points1.nativeObj, points2.nativeObj, cameraMatrix.nativeObj));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1   Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                  floating-point (single or double precision)
     * @param points2   Array of the second image points of the same size and format as points1
     * @param focal     focal length of the camera. Note that this function assumes that points1 and points2 are feature
     *                  points from cameras with same focal length and principal point
     * @param pp        principal point of the camera
     * @param method    Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS for
     *                  the LMedS algorithm
     * @param prob      Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                  confidence (probability) that the estimated matrix is correct
     * @param threshold Parameter used for RANSAC. It is the maximum distance from a point to an epipolar line in
     *                  pixels, beyond which the point is considered an outlier and is not used for computing the final
     *                  fundamental matrix. It can be set to something like 1-3, depending on the accuracy of the point
     *                  localization, image resolution, and the image noise
     * @param maxIters  The maximum number of robust method iterations. This function differs from the one above that it
     *                  computes camera intrinsic matrix from focal length and principal point: A = f &amp; 0 &amp;
     *                  x_{pp} \\ 0 &amp; f &amp; y_{pp} \\ 0 &amp; 0 &amp; 1
     * @param mask      Output array of N elements, every element of which is set to 0 for outliers and to 1 for the
     *                  other points. The array is computed only in the RANSAC and LMedS methods. This function
     *                  estimates essential matrix based on the five-point algorithm solver in is also a related. The
     *                  epipolar geometry is described by the following equation: [p_2; 1]^T K^{-T} E K^{-1} [p_1; 1] =
     *                  0 where E is an essential matrix, p_1 and p_2 are corresponding points in the first and the
     *                  second images, respectively. The result of this function may be passed further to
     *                  decomposeEssentialMat or recoverPose to recover the relative pose between cameras
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, pp, mask is null
     */
    public static Mat findEssentialMat(
            Mat points1,
            Mat points2,
            double focal,
            Point pp,
            int method,
            double prob,
            double threshold,
            int maxIters,
            Mat mask) {
        return new Mat(findEssentialMat_6(
                points1.nativeObj,
                points2.nativeObj,
                focal,
                pp.x,
                pp.y,
                method,
                prob,
                threshold,
                maxIters,
                mask.nativeObj));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1   Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                  floating-point (single or double precision)
     * @param points2   Array of the second image points of the same size and format as points1
     * @param focal     focal length of the camera. Note that this function assumes that points1 and points2 are feature
     *                  points from cameras with same focal length and principal point
     * @param pp        principal point of the camera
     * @param method    Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS for
     *                  the LMedS algorithm
     * @param prob      Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                  confidence (probability) that the estimated matrix is correct
     * @param threshold Parameter used for RANSAC. It is the maximum distance from a point to an epipolar line in
     *                  pixels, beyond which the point is considered an outlier and is not used for computing the final
     *                  fundamental matrix. It can be set to something like 1-3, depending on the accuracy of the point
     *                  localization, image resolution, and the image noise
     * @param maxIters  The maximum number of robust method iterations. This function differs from the one above that it
     *                  computes camera intrinsic matrix from focal length and principal point: A = f &amp; 0 &amp;
     *                  x_{pp} \\ 0 &amp; f &amp; y_{pp} \\ 0 &amp; 0 &amp; 1
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, pp is null
     */
    public static Mat findEssentialMat(
            Mat points1,
            Mat points2,
            double focal,
            Point pp,
            int method,
            double prob,
            double threshold,
            int maxIters) {
        return new Mat(findEssentialMat_7(
                points1.nativeObj,
                points2.nativeObj,
                focal,
                pp.x,
                pp.y,
                method,
                prob,
                threshold,
                maxIters));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1   Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                  floating-point (single or double precision)
     * @param points2   Array of the second image points of the same size and format as points1
     * @param focal     focal length of the camera. Note that this function assumes that points1 and points2 are feature
     *                  points from cameras with same focal length and principal point
     * @param pp        principal point of the camera
     * @param method    Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS for
     *                  the LMedS algorithm
     * @param prob      Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                  confidence (probability) that the estimated matrix is correct
     * @param threshold Parameter used for RANSAC. It is the maximum distance from a point to an epipolar line in
     *                  pixels, beyond which the point is considered an outlier and is not used for computing the final
     *                  fundamental matrix. It can be set to something like 1-3, depending on the accuracy of the point
     *                  localization, image resolution, and the image noise
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, pp is null
     */
    public static Mat findEssentialMat(
            Mat points1,
            Mat points2,
            double focal,
            Point pp,
            int method,
            double prob,
            double threshold) {
        return new Mat(
                findEssentialMat_8(points1.nativeObj, points2.nativeObj, focal, pp.x, pp.y, method, prob, threshold));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1 Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                floating-point (single or double precision)
     * @param points2 Array of the second image points of the same size and format as points1
     * @param focal   focal length of the camera. Note that this function assumes that points1 and points2 are feature
     *                points from cameras with same focal length and principal point
     * @param pp      principal point of the camera
     * @param method  Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS for the
     *                LMedS algorithm
     * @param prob    Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of confidence
     *                (probability) that the estimated matrix is correct
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, pp is null
     */
    public static Mat findEssentialMat(Mat points1, Mat points2, double focal, Point pp, int method, double prob) {
        return new Mat(findEssentialMat_9(points1.nativeObj, points2.nativeObj, focal, pp.x, pp.y, method, prob));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1 Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                floating-point (single or double precision)
     * @param points2 Array of the second image points of the same size and format as points1
     * @param focal   focal length of the camera. Note that this function assumes that points1 and points2 are feature
     *                points from cameras with same focal length and principal point
     * @param pp      principal point of the camera
     * @param method  Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS for the
     *                LMedS algorithm
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, pp is null
     */
    public static Mat findEssentialMat(Mat points1, Mat points2, double focal, Point pp, int method) {
        return new Mat(findEssentialMat_10(points1.nativeObj, points2.nativeObj, focal, pp.x, pp.y, method));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1 Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                floating-point (single or double precision)
     * @param points2 Array of the second image points of the same size and format as points1
     * @param focal   focal length of the camera. Note that this function assumes that points1 and points2 are feature
     *                points from cameras with same focal length and principal point
     * @param pp      principal point of the camera. REF: RANSAC for the RANSAC algorithm. REF: LMEDS for the LMedS
     *                algorithm. line in pixels, beyond which the point is considered an outlier and is not used for
     *                computing the final fundamental matrix. It can be set to something like 1-3, depending on the
     *                accuracy of the point localization, image resolution, and the image noise. confidence
     *                (probability) that the estimated matrix is correct. for the other points. The array is computed
     *                only in the RANSAC and LMedS methods. This function differs from the one above that it computes
     *                camera intrinsic matrix from focal length and principal point: A = f &amp; 0 &amp; x_{pp} \\ 0
     *                &amp; f &amp; y_{pp} \\ 0 &amp; 0 &amp; 1
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, pp is null
     */
    public static Mat findEssentialMat(Mat points1, Mat points2, double focal, Point pp) {
        return new Mat(findEssentialMat_11(points1.nativeObj, points2.nativeObj, focal, pp.x, pp.y));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1 Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                floating-point (single or double precision)
     * @param points2 Array of the second image points of the same size and format as points1
     * @param focal   focal length of the camera. Note that this function assumes that points1 and points2 are feature
     *                points from cameras with same focal length and principal point. REF: RANSAC for the RANSAC
     *                algorithm. REF: LMEDS for the LMedS algorithm. line in pixels, beyond which the point is
     *                considered an outlier and is not used for computing the final fundamental matrix. It can be set to
     *                something like 1-3, depending on the accuracy of the point localization, image resolution, and the
     *                image noise. confidence (probability) that the estimated matrix is correct. for the other points.
     *                The array is computed only in the RANSAC and LMedS methods. This function differs from the one
     *                above that it computes camera intrinsic matrix from focal length and principal point: A = f &amp;
     *                0 &amp; x_{pp} \\ 0 &amp; f &amp; y_{pp} \\ 0 &amp; 0 &amp; 1
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2 is null
     */
    public static Mat findEssentialMat(Mat points1, Mat points2, double focal) {
        return new Mat(findEssentialMat_12(points1.nativeObj, points2.nativeObj, focal));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1 Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                floating-point (single or double precision)
     * @param points2 Array of the second image points of the same size and format as points1
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2 is null
     */
    public static Mat findEssentialMat(Mat points1, Mat points2) {
        return new Mat(findEssentialMat_13(points1.nativeObj, points2.nativeObj));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1       Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                      floating-point (single or double precision)
     * @param points2       Array of the second image points of the same size and format as points1
     * @param cameraMatrix1 Camera matrix for the first camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs1   Input vector of distortion coefficients for the first camera (k_1, k_2, p_1, p_2[, k_3[,
     *                      k_4, k_5, k_6[, s_1, s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the
     *                      vector is NULL/empty, the zero distortion coefficients are assumed
     * @param cameraMatrix2 Camera matrix for the second camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs2   Input vector of distortion coefficients for the second camera (k_1, k_2, p_1, p_2[, k_3[,
     *                      k_4, k_5, k_6[, s_1, s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the
     *                      vector is NULL/empty, the zero distortion coefficients are assumed
     * @param method        Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS
     *                      for the LMedS algorithm
     * @param prob          Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                      confidence (probability) that the estimated matrix is correct
     * @param threshold     Parameter used for RANSAC. It is the maximum distance from a point to an epipolar line in
     *                      pixels, beyond which the point is considered an outlier and is not used for computing the
     *                      final fundamental matrix. It can be set to something like 1-3, depending on the accuracy of
     *                      the point localization, image resolution, and the image noise
     * @param mask          Output array of N elements, every element of which is set to 0 for outliers and to 1 for the
     *                      other points. The array is computed only in the RANSAC and LMedS methods. This function
     *                      estimates essential matrix based on the five-point algorithm solver in is also a related.
     *                      The epipolar geometry is described by the following equation: [p_2; 1]^T K^{-T} E K^{-1}
     *                      [p_1; 1] = 0 where E is an essential matrix, p_1 and p_2 are corresponding points in the
     *                      first and the second images, respectively. The result of this function may be passed further
     *                      to decomposeEssentialMat or recoverPose to recover the relative pose between cameras
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2, mask is
     *                              null
     */
    public static Mat findEssentialMat(
            Mat points1,
            Mat points2,
            Mat cameraMatrix1,
            Mat distCoeffs1,
            Mat cameraMatrix2,
            Mat distCoeffs2,
            int method,
            double prob,
            double threshold,
            Mat mask) {
        return new Mat(findEssentialMat_14(
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix1.nativeObj,
                distCoeffs1.nativeObj,
                cameraMatrix2.nativeObj,
                distCoeffs2.nativeObj,
                method,
                prob,
                threshold,
                mask.nativeObj));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1       Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                      floating-point (single or double precision)
     * @param points2       Array of the second image points of the same size and format as points1
     * @param cameraMatrix1 Camera matrix for the first camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs1   Input vector of distortion coefficients for the first camera (k_1, k_2, p_1, p_2[, k_3[,
     *                      k_4, k_5, k_6[, s_1, s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the
     *                      vector is NULL/empty, the zero distortion coefficients are assumed
     * @param cameraMatrix2 Camera matrix for the second camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs2   Input vector of distortion coefficients for the second camera (k_1, k_2, p_1, p_2[, k_3[,
     *                      k_4, k_5, k_6[, s_1, s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the
     *                      vector is NULL/empty, the zero distortion coefficients are assumed
     * @param method        Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS
     *                      for the LMedS algorithm
     * @param prob          Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                      confidence (probability) that the estimated matrix is correct
     * @param threshold     Parameter used for RANSAC. It is the maximum distance from a point to an epipolar line in
     *                      pixels, beyond which the point is considered an outlier and is not used for computing the
     *                      final fundamental matrix. It can be set to something like 1-3, depending on the accuracy of
     *                      the point localization, image resolution, and the image noise
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2 is null
     */
    public static Mat findEssentialMat(
            Mat points1,
            Mat points2,
            Mat cameraMatrix1,
            Mat distCoeffs1,
            Mat cameraMatrix2,
            Mat distCoeffs2,
            int method,
            double prob,
            double threshold) {
        return new Mat(findEssentialMat_15(
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix1.nativeObj,
                distCoeffs1.nativeObj,
                cameraMatrix2.nativeObj,
                distCoeffs2.nativeObj,
                method,
                prob,
                threshold));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1       Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                      floating-point (single or double precision)
     * @param points2       Array of the second image points of the same size and format as points1
     * @param cameraMatrix1 Camera matrix for the first camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs1   Input vector of distortion coefficients for the first camera (k_1, k_2, p_1, p_2[, k_3[,
     *                      k_4, k_5, k_6[, s_1, s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the
     *                      vector is NULL/empty, the zero distortion coefficients are assumed
     * @param cameraMatrix2 Camera matrix for the second camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs2   Input vector of distortion coefficients for the second camera (k_1, k_2, p_1, p_2[, k_3[,
     *                      k_4, k_5, k_6[, s_1, s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the
     *                      vector is NULL/empty, the zero distortion coefficients are assumed
     * @param method        Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS
     *                      for the LMedS algorithm
     * @param prob          Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                      confidence (probability) that the estimated matrix is correct
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2 is null
     */
    public static Mat findEssentialMat(
            Mat points1,
            Mat points2,
            Mat cameraMatrix1,
            Mat distCoeffs1,
            Mat cameraMatrix2,
            Mat distCoeffs2,
            int method,
            double prob) {
        return new Mat(findEssentialMat_16(
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix1.nativeObj,
                distCoeffs1.nativeObj,
                cameraMatrix2.nativeObj,
                distCoeffs2.nativeObj,
                method,
                prob));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1       Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                      floating-point (single or double precision)
     * @param points2       Array of the second image points of the same size and format as points1
     * @param cameraMatrix1 Camera matrix for the first camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs1   Input vector of distortion coefficients for the first camera (k_1, k_2, p_1, p_2[, k_3[,
     *                      k_4, k_5, k_6[, s_1, s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the
     *                      vector is NULL/empty, the zero distortion coefficients are assumed
     * @param cameraMatrix2 Camera matrix for the second camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs2   Input vector of distortion coefficients for the second camera (k_1, k_2, p_1, p_2[, k_3[,
     *                      k_4, k_5, k_6[, s_1, s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the
     *                      vector is NULL/empty, the zero distortion coefficients are assumed
     * @param method        Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS
     *                      for the LMedS algorithm
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2 is null
     */
    public static Mat findEssentialMat(
            Mat points1,
            Mat points2,
            Mat cameraMatrix1,
            Mat distCoeffs1,
            Mat cameraMatrix2,
            Mat distCoeffs2,
            int method) {
        return new Mat(findEssentialMat_17(
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix1.nativeObj,
                distCoeffs1.nativeObj,
                cameraMatrix2.nativeObj,
                distCoeffs2.nativeObj,
                method));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1       Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                      floating-point (single or double precision)
     * @param points2       Array of the second image points of the same size and format as points1
     * @param cameraMatrix1 Camera matrix for the first camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs1   Input vector of distortion coefficients for the first camera (k_1, k_2, p_1, p_2[, k_3[,
     *                      k_4, k_5, k_6[, s_1, s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the
     *                      vector is NULL/empty, the zero distortion coefficients are assumed
     * @param cameraMatrix2 Camera matrix for the second camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs2   Input vector of distortion coefficients for the second camera (k_1, k_2, p_1, p_2[, k_3[,
     *                      k_4, k_5, k_6[, s_1, s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the
     *                      vector is NULL/empty, the zero distortion coefficients are assumed
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2 is null
     */
    public static Mat findEssentialMat(
            Mat points1,
            Mat points2,
            Mat cameraMatrix1,
            Mat distCoeffs1,
            Mat cameraMatrix2,
            Mat distCoeffs2) {
        return new Mat(findEssentialMat_18(
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix1.nativeObj,
                distCoeffs1.nativeObj,
                cameraMatrix2.nativeObj,
                distCoeffs2.nativeObj));
    }

    /**
     * Calculates an essential matrix from the corresponding points in two images from potentially two different
     * cameras.
     *
     * @param points1       Array of N (N &gt;= 5) 2D points from the first image. The point coordinates should be
     *                      floating-point (single or double precision)
     * @param points2       Array of the second image points of the same size and format as points1
     * @param cameraMatrix1 Camera matrix for the first camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param cameraMatrix2 Camera matrix for the second camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param dist_coeff1   distortion coefficients for the first camera
     * @param dist_coeff2   distortion coefficients for the second camera
     * @param mask          Output array of N elements, every element of which is set to 0 for outliers and to 1 for the
     *                      other points. The array is computed only in the RANSAC and LMedS methods. This function
     *                      estimates essential matrix based on the five-point algorithm solver in is also a related.
     *                      The epipolar geometry is described by the following equation: [p_2; 1]^T K^{-T} E K^{-1}
     *                      [p_1; 1] = 0 where E is an essential matrix, p_1 and p_2 are corresponding points in the
     *                      first and the second images, respectively. The result of this function may be passed further
     *                      to decomposeEssentialMat or recoverPose to recover the relative pose between cameras
     * @param params        robust-estimation sampling, scoring, threshold and refinement settings
     * @return the estimated essential matrix or stacked candidate essential matrices
     * @throws NullPointerException if points1, points2, cameraMatrix1, cameraMatrix2, dist_coeff1, dist_coeff2, mask,
     *                              params is null
     */
    public static Mat findEssentialMat(
            Mat points1,
            Mat points2,
            Mat cameraMatrix1,
            Mat cameraMatrix2,
            Mat dist_coeff1,
            Mat dist_coeff2,
            Mat mask,
            UsacParams params) {
        return new Mat(findEssentialMat_19(
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix1.nativeObj,
                cameraMatrix2.nativeObj,
                dist_coeff1.nativeObj,
                dist_coeff2.nativeObj,
                mask.nativeObj,
                params.getNativeObjAddr()));
    }

    /**
     * Decompose an essential matrix to possible rotations and translation.
     *
     * @param E  The input essential matrix
     * @param R1 One possible rotation matrix
     * @param R2 Another possible rotation matrix
     * @param t  One possible translation. This function decomposes the essential matrix E using svd decomposition In
     *           general, four possible poses exist for the decomposition of E. They are [R_1, t], [R_1, -t], [R_2, t],
     *           [R_2, -t]. If E gives the epipolar constraint [p_2; 1]^T A^{-T} E A^{-1} [p_1; 1] = 0 between the image
     *           points p_1 in the first image and p_2 in second image, then any of the tuples [R_1, t], [R_1, -t],
     *           [R_2, t], [R_2, -t] is a change of basis from the first camera's coordinate system to the second
     *           camera's coordinate system. However, by decomposing E, one can only get the direction of the
     *           translation. For this reason, the translation t is returned with unit length
     * @throws NullPointerException if E, R1, R2, t is null
     */
    public static void decomposeEssentialMat(Mat E, Mat R1, Mat R2, Mat t) {
        decomposeEssentialMat_0(E.nativeObj, R1.nativeObj, R2.nativeObj, t.nativeObj);
    }

    /**
     * Recovers the relative camera rotation and the translation from corresponding points in two images from two
     * different cameras, using chirality check. Returns the number of inliers that pass the check.
     *
     * @param points1       Array of N 2D points from the first image. The point coordinates should be floating-point
     *                      (single or double precision)
     * @param points2       Array of the second image points of the same size and format as points1
     * @param cameraMatrix1 intrinsic matrix of the first camera
     * @param distCoeffs1   Input/output vector of distortion coefficients, the same as in REF: calibrateCamera
     * @param cameraMatrix2 intrinsic matrix of the second camera
     * @param distCoeffs2   Input/output vector of distortion coefficients, the same as in REF: calibrateCamera
     * @param E             The output essential matrix
     * @param R             Output rotation matrix. Together with the translation vector, this matrix makes up a tuple
     *                      that performs a change of basis from the first camera's coordinate system to the second
     *                      camera's coordinate system. Note that, in general, t can not be used for this tuple, see the
     *                      parameter described below
     * @param t             Output translation vector. This vector is obtained by REF: decomposeEssentialMat and
     *                      therefore is only known up to scale, i.e. t is the direction of the translation vector and
     *                      has unit length
     * @param method        Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS
     *                      for the LMedS algorithm
     * @param prob          Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                      confidence (probability) that the estimated matrix is correct
     * @param threshold     Parameter used for RANSAC. It is the maximum distance from a point to an epipolar line in
     *                      pixels, beyond which the point is considered an outlier and is not used for computing the
     *                      final fundamental matrix. It can be set to something like 1-3, depending on the accuracy of
     *                      the point localization, image resolution, and the image noise
     * @param mask          Input/output mask for inliers in points1 and points2. If it is not empty, then it marks
     *                      inliers in points1 and points2 for the given essential matrix E. Only these inliers will be
     *                      used to recover pose. In the output mask only inliers which pass the chirality check. This
     *                      function decomposes an essential matrix using REF: decomposeEssentialMat and then verifies
     *                      possible pose hypotheses by doing chirality check. The chirality check means that the
     *                      triangulated 3D points should have positive depth. Some details can be found in This
     *                      function can be used to process the output E and mask from REF: findEssentialMat. In this
     *                      scenario, points1 and points2 are the same input for findEssentialMat.: // Example.
     *                      Estimation of fundamental matrix using the RANSAC algorithm int point_count = 100;
     *                      vector&lt;Point2f&gt; points1(point_count); vector&lt;Point2f&gt; points2(point_count); //
     *                      initialize the points here ... for( int i = 0; i &lt; point_count; i++ ) { points1[i] = ...;
     *                      points2[i] = ...; } // Input: camera calibration of both cameras, for example using
     *                      intrinsic chessboard calibration. Mat cameraMatrix1, distCoeffs1, cameraMatrix2,
     *                      distCoeffs2; // Output: Essential matrix, relative rotation and relative translation. Mat E,
     *                      R, t, mask; recoverPose(points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2,
     *                      distCoeffs2, E, R, t, mask);
     * @return the number of inlier correspondences passing the pose cheirality check
     * @throws NullPointerException if points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2, E, R,
     *                              t, mask is null
     */
    public static int recoverPose(
            Mat points1,
            Mat points2,
            Mat cameraMatrix1,
            Mat distCoeffs1,
            Mat cameraMatrix2,
            Mat distCoeffs2,
            Mat E,
            Mat R,
            Mat t,
            int method,
            double prob,
            double threshold,
            Mat mask) {
        return recoverPose_0(
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix1.nativeObj,
                distCoeffs1.nativeObj,
                cameraMatrix2.nativeObj,
                distCoeffs2.nativeObj,
                E.nativeObj,
                R.nativeObj,
                t.nativeObj,
                method,
                prob,
                threshold,
                mask.nativeObj);
    }

    /**
     * Recovers the relative camera rotation and the translation from corresponding points in two images from two
     * different cameras, using chirality check. Returns the number of inliers that pass the check.
     *
     * @param points1       Array of N 2D points from the first image. The point coordinates should be floating-point
     *                      (single or double precision)
     * @param points2       Array of the second image points of the same size and format as points1
     * @param cameraMatrix1 intrinsic matrix of the first camera
     * @param distCoeffs1   Input/output vector of distortion coefficients, the same as in REF: calibrateCamera
     * @param cameraMatrix2 intrinsic matrix of the second camera
     * @param distCoeffs2   Input/output vector of distortion coefficients, the same as in REF: calibrateCamera
     * @param E             The output essential matrix
     * @param R             Output rotation matrix. Together with the translation vector, this matrix makes up a tuple
     *                      that performs a change of basis from the first camera's coordinate system to the second
     *                      camera's coordinate system. Note that, in general, t can not be used for this tuple, see the
     *                      parameter described below
     * @param t             Output translation vector. This vector is obtained by REF: decomposeEssentialMat and
     *                      therefore is only known up to scale, i.e. t is the direction of the translation vector and
     *                      has unit length
     * @param method        Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS
     *                      for the LMedS algorithm
     * @param prob          Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                      confidence (probability) that the estimated matrix is correct
     * @param threshold     Parameter used for RANSAC. It is the maximum distance from a point to an epipolar line in
     *                      pixels, beyond which the point is considered an outlier and is not used for computing the
     *                      final fundamental matrix. It can be set to something like 1-3, depending on the accuracy of
     *                      the point localization, image resolution, and the image noise
     * @return the number of inlier correspondences passing the pose cheirality check
     * @throws NullPointerException if points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2, E, R, t
     *                              is null
     */
    public static int recoverPose(
            Mat points1,
            Mat points2,
            Mat cameraMatrix1,
            Mat distCoeffs1,
            Mat cameraMatrix2,
            Mat distCoeffs2,
            Mat E,
            Mat R,
            Mat t,
            int method,
            double prob,
            double threshold) {
        return recoverPose_1(
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix1.nativeObj,
                distCoeffs1.nativeObj,
                cameraMatrix2.nativeObj,
                distCoeffs2.nativeObj,
                E.nativeObj,
                R.nativeObj,
                t.nativeObj,
                method,
                prob,
                threshold);
    }

    /**
     * Recovers the relative camera rotation and the translation from corresponding points in two images from two
     * different cameras, using chirality check. Returns the number of inliers that pass the check.
     *
     * @param points1       Array of N 2D points from the first image. The point coordinates should be floating-point
     *                      (single or double precision)
     * @param points2       Array of the second image points of the same size and format as points1
     * @param cameraMatrix1 intrinsic matrix of the first camera
     * @param distCoeffs1   Input/output vector of distortion coefficients, the same as in REF: calibrateCamera
     * @param cameraMatrix2 intrinsic matrix of the second camera
     * @param distCoeffs2   Input/output vector of distortion coefficients, the same as in REF: calibrateCamera
     * @param E             The output essential matrix
     * @param R             Output rotation matrix. Together with the translation vector, this matrix makes up a tuple
     *                      that performs a change of basis from the first camera's coordinate system to the second
     *                      camera's coordinate system. Note that, in general, t can not be used for this tuple, see the
     *                      parameter described below
     * @param t             Output translation vector. This vector is obtained by REF: decomposeEssentialMat and
     *                      therefore is only known up to scale, i.e. t is the direction of the translation vector and
     *                      has unit length
     * @param method        Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS
     *                      for the LMedS algorithm
     * @param prob          Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                      confidence (probability) that the estimated matrix is correct
     * @return the number of inlier correspondences passing the pose cheirality check
     * @throws NullPointerException if points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2, E, R, t
     *                              is null
     */
    public static int recoverPose(
            Mat points1,
            Mat points2,
            Mat cameraMatrix1,
            Mat distCoeffs1,
            Mat cameraMatrix2,
            Mat distCoeffs2,
            Mat E,
            Mat R,
            Mat t,
            int method,
            double prob) {
        return recoverPose_2(
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix1.nativeObj,
                distCoeffs1.nativeObj,
                cameraMatrix2.nativeObj,
                distCoeffs2.nativeObj,
                E.nativeObj,
                R.nativeObj,
                t.nativeObj,
                method,
                prob);
    }

    /**
     * Recovers the relative camera rotation and the translation from corresponding points in two images from two
     * different cameras, using chirality check. Returns the number of inliers that pass the check.
     *
     * @param points1       Array of N 2D points from the first image. The point coordinates should be floating-point
     *                      (single or double precision)
     * @param points2       Array of the second image points of the same size and format as points1
     * @param cameraMatrix1 intrinsic matrix of the first camera
     * @param distCoeffs1   Input/output vector of distortion coefficients, the same as in REF: calibrateCamera
     * @param cameraMatrix2 intrinsic matrix of the second camera
     * @param distCoeffs2   Input/output vector of distortion coefficients, the same as in REF: calibrateCamera
     * @param E             The output essential matrix
     * @param R             Output rotation matrix. Together with the translation vector, this matrix makes up a tuple
     *                      that performs a change of basis from the first camera's coordinate system to the second
     *                      camera's coordinate system. Note that, in general, t can not be used for this tuple, see the
     *                      parameter described below
     * @param t             Output translation vector. This vector is obtained by REF: decomposeEssentialMat and
     *                      therefore is only known up to scale, i.e. t is the direction of the translation vector and
     *                      has unit length
     * @param method        Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF: LMEDS
     *                      for the LMedS algorithm
     * @return the number of inlier correspondences passing the pose cheirality check
     * @throws NullPointerException if points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2, E, R, t
     *                              is null
     */
    public static int recoverPose(
            Mat points1,
            Mat points2,
            Mat cameraMatrix1,
            Mat distCoeffs1,
            Mat cameraMatrix2,
            Mat distCoeffs2,
            Mat E,
            Mat R,
            Mat t,
            int method) {
        return recoverPose_3(
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix1.nativeObj,
                distCoeffs1.nativeObj,
                cameraMatrix2.nativeObj,
                distCoeffs2.nativeObj,
                E.nativeObj,
                R.nativeObj,
                t.nativeObj,
                method);
    }

    /**
     * Recovers the relative camera rotation and the translation from corresponding points in two images from two
     * different cameras, using chirality check. Returns the number of inliers that pass the check.
     *
     * @param points1       Array of N 2D points from the first image. The point coordinates should be floating-point
     *                      (single or double precision)
     * @param points2       Array of the second image points of the same size and format as points1
     * @param cameraMatrix1 intrinsic matrix of the first camera
     * @param distCoeffs1   Input/output vector of distortion coefficients, the same as in REF: calibrateCamera
     * @param cameraMatrix2 intrinsic matrix of the second camera
     * @param distCoeffs2   Input/output vector of distortion coefficients, the same as in REF: calibrateCamera
     * @param E             The output essential matrix
     * @param R             Output rotation matrix. Together with the translation vector, this matrix makes up a tuple
     *                      that performs a change of basis from the first camera's coordinate system to the second
     *                      camera's coordinate system. Note that, in general, t can not be used for this tuple, see the
     *                      parameter described below
     * @param t             Output translation vector. This vector is obtained by REF: decomposeEssentialMat and
     *                      therefore is only known up to scale, i.e. t is the direction of the translation vector and
     *                      has unit length
     * @return the number of inlier correspondences passing the pose cheirality check
     * @throws NullPointerException if points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2, E, R, t
     *                              is null
     */
    public static int recoverPose(
            Mat points1,
            Mat points2,
            Mat cameraMatrix1,
            Mat distCoeffs1,
            Mat cameraMatrix2,
            Mat distCoeffs2,
            Mat E,
            Mat R,
            Mat t) {
        return recoverPose_4(
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix1.nativeObj,
                distCoeffs1.nativeObj,
                cameraMatrix2.nativeObj,
                distCoeffs2.nativeObj,
                E.nativeObj,
                R.nativeObj,
                t.nativeObj);
    }

    /**
     * Recovers the relative camera rotation and the translation from corresponding points in two images from two
     * different cameras, using chirality check. Returns the number of inliers that pass the check.
     *
     * @param E            The output essential matrix
     * @param points1      Array of N 2D points from the first image. The point coordinates should be floating-point
     *                     (single or double precision)
     * @param points2      Array of the second image points of the same size and format as points1
     * @param cameraMatrix Camera intrinsic matrix cameramatrix{A} . Note that this function assumes that points1 and
     *                     points2 are feature points from cameras with the same camera intrinsic matrix
     * @param R            Output rotation matrix. Together with the translation vector, this matrix makes up a tuple
     *                     that performs a change of basis from the first camera's coordinate system to the second
     *                     camera's coordinate system. Note that, in general, t can not be used for this tuple, see the
     *                     parameter described below
     * @param t            Output translation vector. This vector is obtained by REF: decomposeEssentialMat and
     *                     therefore is only known up to scale, i.e. t is the direction of the translation vector and
     *                     has unit length
     * @param mask         Input/output mask for inliers in points1 and points2. If it is not empty, then it marks
     *                     inliers in points1 and points2 for the given essential matrix E. Only these inliers will be
     *                     used to recover pose. In the output mask only inliers which pass the chirality check. This
     *                     function decomposes an essential matrix using REF: decomposeEssentialMat and then verifies
     *                     possible pose hypotheses by doing chirality check. The chirality check means that the
     *                     triangulated 3D points should have positive depth. Some details can be found in This function
     *                     can be used to process the output E and mask from REF: findEssentialMat. In this scenario,
     *                     points1 and points2 are the same input for findEssentialMat.: // Example. Estimation of
     *                     fundamental matrix using the RANSAC algorithm int point_count = 100; vector&lt;Point2f&gt;
     *                     points1(point_count); vector&lt;Point2f&gt; points2(point_count); // initialize the points
     *                     here ... for( int i = 0; i &lt; point_count; i++ ) { points1[i] = ...; points2[i] = ...; } //
     *                     Input: camera calibration of both cameras, for example using intrinsic chessboard
     *                     calibration. Mat cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2; // Output: Essential
     *                     matrix, relative rotation and relative translation. Mat E, R, t, mask; recoverPose(points1,
     *                     points2, cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2, E, R, t, mask);
     * @return the number of inlier correspondences passing the pose cheirality check
     * @throws NullPointerException if E, points1, points2, cameraMatrix, R, t, mask is null
     */
    public static int recoverPose(Mat E, Mat points1, Mat points2, Mat cameraMatrix, Mat R, Mat t, Mat mask) {
        return recoverPose_5(
                E.nativeObj,
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix.nativeObj,
                R.nativeObj,
                t.nativeObj,
                mask.nativeObj);
    }

    /**
     * Recovers the relative camera rotation and the translation from corresponding points in two images from two
     * different cameras, using chirality check. Returns the number of inliers that pass the check.
     *
     * @param E            The output essential matrix
     * @param points1      Array of N 2D points from the first image. The point coordinates should be floating-point
     *                     (single or double precision)
     * @param points2      Array of the second image points of the same size and format as points1
     * @param cameraMatrix Camera intrinsic matrix cameramatrix{A} . Note that this function assumes that points1 and
     *                     points2 are feature points from cameras with the same camera intrinsic matrix
     * @param R            Output rotation matrix. Together with the translation vector, this matrix makes up a tuple
     *                     that performs a change of basis from the first camera's coordinate system to the second
     *                     camera's coordinate system. Note that, in general, t can not be used for this tuple, see the
     *                     parameter described below
     * @param t            Output translation vector. This vector is obtained by REF: decomposeEssentialMat and
     *                     therefore is only known up to scale, i.e. t is the direction of the translation vector and
     *                     has unit length
     * @return the number of inlier correspondences passing the pose cheirality check
     * @throws NullPointerException if E, points1, points2, cameraMatrix, R, t is null
     */
    public static int recoverPose(Mat E, Mat points1, Mat points2, Mat cameraMatrix, Mat R, Mat t) {
        return recoverPose_6(
                E.nativeObj,
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix.nativeObj,
                R.nativeObj,
                t.nativeObj);
    }

    /**
     * Recovers the relative camera rotation and the translation from corresponding points in two images from two
     * different cameras, using chirality check. Returns the number of inliers that pass the check.
     *
     * @param E       The output essential matrix
     * @param points1 Array of N 2D points from the first image. The point coordinates should be floating-point (single
     *                or double precision)
     * @param points2 Array of the second image points of the same size and format as points1
     * @param R       Output rotation matrix. Together with the translation vector, this matrix makes up a tuple that
     *                performs a change of basis from the first camera's coordinate system to the second camera's
     *                coordinate system. Note that, in general, t can not be used for this tuple, see the parameter
     *                described below
     * @param t       Output translation vector. This vector is obtained by REF: decomposeEssentialMat and therefore is
     *                only known up to scale, i.e. t is the direction of the translation vector and has unit length
     * @param focal   Focal length of the camera. Note that this function assumes that points1 and points2 are feature
     *                points from cameras with same focal length and principal point
     * @param pp      principal point of the camera
     * @param mask    Input/output mask for inliers in points1 and points2. If it is not empty, then it marks inliers in
     *                points1 and points2 for the given essential matrix E. Only these inliers will be used to recover
     *                pose. In the output mask only inliers which pass the chirality check. This function decomposes an
     *                essential matrix using REF: decomposeEssentialMat and then verifies possible pose hypotheses by
     *                doing chirality check. The chirality check means that the triangulated 3D points should have
     *                positive depth. Some details can be found in This function can be used to process the output E and
     *                mask from REF: findEssentialMat. In this scenario, points1 and points2 are the same input for
     *                findEssentialMat.: // Example. Estimation of fundamental matrix using the RANSAC algorithm int
     *                point_count = 100; vector&lt;Point2f&gt; points1(point_count); vector&lt;Point2f&gt;
     *                points2(point_count); // initialize the points here ... for( int i = 0; i &lt; point_count; i++ )
     *                { points1[i] = ...; points2[i] = ...; } // Input: camera calibration of both cameras, for example
     *                using intrinsic chessboard calibration. Mat cameraMatrix1, distCoeffs1, cameraMatrix2,
     *                distCoeffs2; // Output: Essential matrix, relative rotation and relative translation. Mat E, R, t,
     *                mask; recoverPose(points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2, E, R,
     *                t, mask);
     * @return the number of inlier correspondences passing the pose cheirality check
     * @throws NullPointerException if E, points1, points2, R, t, pp, mask is null
     */
    public static int recoverPose(Mat E, Mat points1, Mat points2, Mat R, Mat t, double focal, Point pp, Mat mask) {
        return recoverPose_7(
                E.nativeObj,
                points1.nativeObj,
                points2.nativeObj,
                R.nativeObj,
                t.nativeObj,
                focal,
                pp.x,
                pp.y,
                mask.nativeObj);
    }

    /**
     * Recovers the relative camera rotation and the translation from corresponding points in two images from two
     * different cameras, using chirality check. Returns the number of inliers that pass the check.
     *
     * @param E       The output essential matrix
     * @param points1 Array of N 2D points from the first image. The point coordinates should be floating-point (single
     *                or double precision)
     * @param points2 Array of the second image points of the same size and format as points1
     * @param R       Output rotation matrix. Together with the translation vector, this matrix makes up a tuple that
     *                performs a change of basis from the first camera's coordinate system to the second camera's
     *                coordinate system. Note that, in general, t can not be used for this tuple, see the parameter
     *                described below
     * @param t       Output translation vector. This vector is obtained by REF: decomposeEssentialMat and therefore is
     *                only known up to scale, i.e. t is the direction of the translation vector and has unit length
     * @param focal   Focal length of the camera. Note that this function assumes that points1 and points2 are feature
     *                points from cameras with same focal length and principal point
     * @param pp      principal point of the camera. inliers in points1 and points2 for the given essential matrix E.
     *                Only these inliers will be used to recover pose. In the output mask only inliers which pass the
     *                chirality check. This function differs from the one above that it computes camera intrinsic matrix
     *                from focal length and principal point: A = f &amp; 0 &amp; x_{pp} \\ 0 &amp; f &amp; y_{pp} \\ 0
     *                &amp; 0 &amp; 1
     * @return the number of inlier correspondences passing the pose cheirality check
     * @throws NullPointerException if E, points1, points2, R, t, pp is null
     */
    public static int recoverPose(Mat E, Mat points1, Mat points2, Mat R, Mat t, double focal, Point pp) {
        return recoverPose_8(
                E.nativeObj,
                points1.nativeObj,
                points2.nativeObj,
                R.nativeObj,
                t.nativeObj,
                focal,
                pp.x,
                pp.y);
    }

    /**
     * Recovers the relative camera rotation and the translation from corresponding points in two images from two
     * different cameras, using chirality check. Returns the number of inliers that pass the check.
     *
     * @param E       The output essential matrix
     * @param points1 Array of N 2D points from the first image. The point coordinates should be floating-point (single
     *                or double precision)
     * @param points2 Array of the second image points of the same size and format as points1
     * @param R       Output rotation matrix. Together with the translation vector, this matrix makes up a tuple that
     *                performs a change of basis from the first camera's coordinate system to the second camera's
     *                coordinate system. Note that, in general, t can not be used for this tuple, see the parameter
     *                described below
     * @param t       Output translation vector. This vector is obtained by REF: decomposeEssentialMat and therefore is
     *                only known up to scale, i.e. t is the direction of the translation vector and has unit length
     * @param focal   Focal length of the camera. Note that this function assumes that points1 and points2 are feature
     *                points from cameras with same focal length and principal point. inliers in points1 and points2 for
     *                the given essential matrix E. Only these inliers will be used to recover pose. In the output mask
     *                only inliers which pass the chirality check. This function differs from the one above that it
     *                computes camera intrinsic matrix from focal length and principal point: A = f &amp; 0 &amp; x_{pp}
     *                \\ 0 &amp; f &amp; y_{pp} \\ 0 &amp; 0 &amp; 1
     * @return the number of inlier correspondences passing the pose cheirality check
     * @throws NullPointerException if E, points1, points2, R, t is null
     */
    public static int recoverPose(Mat E, Mat points1, Mat points2, Mat R, Mat t, double focal) {
        return recoverPose_9(E.nativeObj, points1.nativeObj, points2.nativeObj, R.nativeObj, t.nativeObj, focal);
    }

    /**
     * Recovers the relative camera rotation and the translation from corresponding points in two images from two
     * different cameras, using chirality check. Returns the number of inliers that pass the check.
     *
     * @param E       The output essential matrix
     * @param points1 Array of N 2D points from the first image. The point coordinates should be floating-point (single
     *                or double precision)
     * @param points2 Array of the second image points of the same size and format as points1
     * @param R       Output rotation matrix. Together with the translation vector, this matrix makes up a tuple that
     *                performs a change of basis from the first camera's coordinate system to the second camera's
     *                coordinate system. Note that, in general, t can not be used for this tuple, see the parameter
     *                described below
     * @param t       Output translation vector. This vector is obtained by REF: decomposeEssentialMat and therefore is
     *                only known up to scale, i.e. t is the direction of the translation vector and has unit length
     * @return the number of inlier correspondences passing the pose cheirality check
     * @throws NullPointerException if E, points1, points2, R, t is null
     */
    public static int recoverPose(Mat E, Mat points1, Mat points2, Mat R, Mat t) {
        return recoverPose_10(E.nativeObj, points1.nativeObj, points2.nativeObj, R.nativeObj, t.nativeObj);
    }

    /**
     * Recovers the relative camera rotation and the translation from corresponding points in two images from two
     * different cameras, using chirality check. Returns the number of inliers that pass the check.
     *
     * @param E                  The output essential matrix
     * @param points1            Array of N 2D points from the first image. The point coordinates should be
     *                           floating-point (single or double precision)
     * @param points2            Array of the second image points of the same size and format as points1
     * @param cameraMatrix       Camera intrinsic matrix cameramatrix{A} . Note that this function assumes that points1
     *                           and points2 are feature points from cameras with the same camera intrinsic matrix
     * @param R                  Output rotation matrix. Together with the translation vector, this matrix makes up a
     *                           tuple that performs a change of basis from the first camera's coordinate system to the
     *                           second camera's coordinate system. Note that, in general, t can not be used for this
     *                           tuple, see the parameter described below
     * @param t                  Output translation vector. This vector is obtained by REF: decomposeEssentialMat and
     *                           therefore is only known up to scale, i.e. t is the direction of the translation vector
     *                           and has unit length
     * @param distanceThresh     threshold distance which is used to filter out far away points (i.e. infinite points)
     * @param mask               Input/output mask for inliers in points1 and points2. If it is not empty, then it marks
     *                           inliers in points1 and points2 for the given essential matrix E. Only these inliers
     *                           will be used to recover pose. In the output mask only inliers which pass the chirality
     *                           check. This function decomposes an essential matrix using REF: decomposeEssentialMat
     *                           and then verifies possible pose hypotheses by doing chirality check. The chirality
     *                           check means that the triangulated 3D points should have positive depth. Some details
     *                           can be found in This function can be used to process the output E and mask from REF:
     *                           findEssentialMat. In this scenario, points1 and points2 are the same input for
     *                           findEssentialMat.: // Example. Estimation of fundamental matrix using the RANSAC
     *                           algorithm int point_count = 100; vector&lt;Point2f&gt; points1(point_count);
     *                           vector&lt;Point2f&gt; points2(point_count); // initialize the points here ... for( int
     *                           i = 0; i &lt; point_count; i++ ) { points1[i] = ...; points2[i] = ...; } // Input:
     *                           camera calibration of both cameras, for example using intrinsic chessboard calibration.
     *                           Mat cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2; // Output: Essential
     *                           matrix, relative rotation and relative translation. Mat E, R, t, mask;
     *                           recoverPose(points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2,
     *                           E, R, t, mask);
     * @param triangulatedPoints 3D points which were reconstructed by triangulation. This function differs from the one
     *                           above that it outputs the triangulated 3D point that are used for the chirality check
     * @return the number of inlier correspondences passing the pose cheirality check
     * @throws NullPointerException if E, points1, points2, cameraMatrix, R, t, mask, triangulatedPoints is null
     */
    public static int recoverPose(
            Mat E,
            Mat points1,
            Mat points2,
            Mat cameraMatrix,
            Mat R,
            Mat t,
            double distanceThresh,
            Mat mask,
            Mat triangulatedPoints) {
        return recoverPose_11(
                E.nativeObj,
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix.nativeObj,
                R.nativeObj,
                t.nativeObj,
                distanceThresh,
                mask.nativeObj,
                triangulatedPoints.nativeObj);
    }

    /**
     * Recovers the relative camera rotation and the translation from corresponding points in two images from two
     * different cameras, using chirality check. Returns the number of inliers that pass the check.
     *
     * @param E              The output essential matrix
     * @param points1        Array of N 2D points from the first image. The point coordinates should be floating-point
     *                       (single or double precision)
     * @param points2        Array of the second image points of the same size and format as points1
     * @param cameraMatrix   Camera intrinsic matrix cameramatrix{A} . Note that this function assumes that points1 and
     *                       points2 are feature points from cameras with the same camera intrinsic matrix
     * @param R              Output rotation matrix. Together with the translation vector, this matrix makes up a tuple
     *                       that performs a change of basis from the first camera's coordinate system to the second
     *                       camera's coordinate system. Note that, in general, t can not be used for this tuple, see
     *                       the parameter described below
     * @param t              Output translation vector. This vector is obtained by REF: decomposeEssentialMat and
     *                       therefore is only known up to scale, i.e. t is the direction of the translation vector and
     *                       has unit length
     * @param distanceThresh threshold distance which is used to filter out far away points (i.e. infinite points)
     * @param mask           Input/output mask for inliers in points1 and points2. If it is not empty, then it marks
     *                       inliers in points1 and points2 for the given essential matrix E. Only these inliers will be
     *                       used to recover pose. In the output mask only inliers which pass the chirality check. This
     *                       function decomposes an essential matrix using REF: decomposeEssentialMat and then verifies
     *                       possible pose hypotheses by doing chirality check. The chirality check means that the
     *                       triangulated 3D points should have positive depth. Some details can be found in This
     *                       function can be used to process the output E and mask from REF: findEssentialMat. In this
     *                       scenario, points1 and points2 are the same input for findEssentialMat.: // Example.
     *                       Estimation of fundamental matrix using the RANSAC algorithm int point_count = 100;
     *                       vector&lt;Point2f&gt; points1(point_count); vector&lt;Point2f&gt; points2(point_count); //
     *                       initialize the points here ... for( int i = 0; i &lt; point_count; i++ ) { points1[i] =
     *                       ...; points2[i] = ...; } // Input: camera calibration of both cameras, for example using
     *                       intrinsic chessboard calibration. Mat cameraMatrix1, distCoeffs1, cameraMatrix2,
     *                       distCoeffs2; // Output: Essential matrix, relative rotation and relative translation. Mat
     *                       E, R, t, mask; recoverPose(points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2,
     *                       distCoeffs2, E, R, t, mask);
     * @return the number of inlier correspondences passing the pose cheirality check
     * @throws NullPointerException if E, points1, points2, cameraMatrix, R, t, mask is null
     */
    public static int recoverPose(
            Mat E,
            Mat points1,
            Mat points2,
            Mat cameraMatrix,
            Mat R,
            Mat t,
            double distanceThresh,
            Mat mask) {
        return recoverPose_12(
                E.nativeObj,
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix.nativeObj,
                R.nativeObj,
                t.nativeObj,
                distanceThresh,
                mask.nativeObj);
    }

    /**
     * Recovers the relative camera rotation and the translation from corresponding points in two images from two
     * different cameras, using chirality check. Returns the number of inliers that pass the check.
     *
     * @param E              The output essential matrix
     * @param points1        Array of N 2D points from the first image. The point coordinates should be floating-point
     *                       (single or double precision)
     * @param points2        Array of the second image points of the same size and format as points1
     * @param cameraMatrix   Camera intrinsic matrix cameramatrix{A} . Note that this function assumes that points1 and
     *                       points2 are feature points from cameras with the same camera intrinsic matrix
     * @param R              Output rotation matrix. Together with the translation vector, this matrix makes up a tuple
     *                       that performs a change of basis from the first camera's coordinate system to the second
     *                       camera's coordinate system. Note that, in general, t can not be used for this tuple, see
     *                       the parameter described below
     * @param t              Output translation vector. This vector is obtained by REF: decomposeEssentialMat and
     *                       therefore is only known up to scale, i.e. t is the direction of the translation vector and
     *                       has unit length
     * @param distanceThresh threshold distance which is used to filter out far away points (i.e. infinite points).
     *                       inliers in points1 and points2 for the given essential matrix E. Only these inliers will be
     *                       used to recover pose. In the output mask only inliers which pass the chirality check. This
     *                       function differs from the one above that it outputs the triangulated 3D point that are used
     *                       for the chirality check
     * @return the number of inlier correspondences passing the pose cheirality check
     * @throws NullPointerException if E, points1, points2, cameraMatrix, R, t is null
     */
    public static int recoverPose(
            Mat E,
            Mat points1,
            Mat points2,
            Mat cameraMatrix,
            Mat R,
            Mat t,
            double distanceThresh) {
        return recoverPose_13(
                E.nativeObj,
                points1.nativeObj,
                points2.nativeObj,
                cameraMatrix.nativeObj,
                R.nativeObj,
                t.nativeObj,
                distanceThresh);
    }

    /**
     * For points in an image of a stereo pair, computes the corresponding epilines in the other image.
     *
     * @param points     Input points. N times 1 or 1 times N matrix of type CV_32FC2 or vector&lt;Point2f&gt;
     * @param whichImage Index of the image (1 or 2) that contains the points
     * @param F          Fundamental matrix that can be estimated using findFundamentalMat or stereoRectify
     * @param lines      Output vector of the epipolar lines corresponding to the points in the other image. Each line
     *                   ax + by + c=0 is encoded by 3 numbers (a, b, c) . For every point in one of the two images of a
     *                   stereo pair, the function finds the equation of the corresponding epipolar line in the other
     *                   image. From the fundamental matrix definition (see findFundamentalMat ), line l^{(2)}_i in the
     *                   second image for the point p^{(1)}_i in the first image (when whichImage=1 ) is computed as:
     *                   l^{(2)}_i = F p^{(1)}_i And vice versa, when whichImage=2, l^{(1)}_i is computed from p^{(2)}_i
     *                   as: l^{(1)}_i = F^T p^{(2)}_i Line coefficients are defined up to a scale. They are normalized
     *                   so that a_i^2+b_i^2=1
     * @throws NullPointerException if points, F, lines is null
     */
    public static void computeCorrespondEpilines(Mat points, int whichImage, Mat F, Mat lines) {
        computeCorrespondEpilines_0(points.nativeObj, whichImage, F.nativeObj, lines.nativeObj);
    }

    /**
     * This function reconstructs 3-dimensional points (in homogeneous coordinates) by using their observations with a
     * stereo camera.
     *
     * @param projMatr1   3x4 projection matrix of the first camera, i.e. this matrix projects 3D points given in the
     *                    world's coordinate system into the first image
     * @param projMatr2   3x4 projection matrix of the second camera, i.e. this matrix projects 3D points given in the
     *                    world's coordinate system into the second image
     * @param projPoints1 2xN array of feature points in the first image. In the case of the c++ version, it can be also
     *                    a vector of feature points or two-channel matrix of size 1xN or Nx1
     * @param projPoints2 2xN array of corresponding points in the second image. In the case of the c++ version, it can
     *                    be also a vector of feature points or two-channel matrix of size 1xN or Nx1
     * @param points4D    4xN array of reconstructed points in homogeneous coordinates. These points are returned in the
     *                    world's coordinate system. Note: Keep in mind that all input data should be of float type in
     *                    order for this function to work. Note: If the projection matrices from REF: stereoRectify are
     *                    used, then the returned points are represented in the first camera's rectified coordinate
     *                    system
     * @throws NullPointerException if projMatr1, projMatr2, projPoints1, projPoints2, points4D is null
     */
    public static void triangulatePoints(Mat projMatr1, Mat projMatr2, Mat projPoints1, Mat projPoints2, Mat points4D) {
        triangulatePoints_0(
                projMatr1.nativeObj,
                projMatr2.nativeObj,
                projPoints1.nativeObj,
                projPoints2.nativeObj,
                points4D.nativeObj);
    }

    /**
     * Refines coordinates of corresponding points.
     *
     * @param F          3x3 fundamental matrix
     * @param points1    1xN array containing the first set of points
     * @param points2    1xN array containing the second set of points
     * @param newPoints1 The optimized points1
     * @param newPoints2 The optimized points2. The function implements the Optimal Triangulation Method (see Multiple
     *                   View Geometry for details). For each given point correspondence points1[i] &lt;-&gt;
     *                   points2[i], and a fundamental matrix F, it computes the corrected correspondences newPoints1[i]
     *                   &lt;-&gt; newPoints2[i] that minimize the geometric error d(points1[i], newPoints1[i])^2 +
     *                   d(points2[i],newPoints2[i])^2 (where d(a,b) is the geometric distance between points a and b )
     *                   subject to the epipolar constraint newPoints2^T cdot F cdot newPoints1 = 0
     * @throws NullPointerException if F, points1, points2, newPoints1, newPoints2 is null
     */
    public static void correctMatches(Mat F, Mat points1, Mat points2, Mat newPoints1, Mat newPoints2) {
        correctMatches_0(F.nativeObj, points1.nativeObj, points2.nativeObj, newPoints1.nativeObj, newPoints2.nativeObj);
    }

    /**
     * Computes the Sampson first-order approximation to geometric reprojection error for two corresponding points and a
     * fundamental matrix.
     *
     * @param pt1 first homogeneous 2d point
     * @param pt2 second homogeneous 2d point
     * @param F   fundamental matrix
     * @return The computed Sampson distance
     * @throws NullPointerException if pt1, pt2, F is null
     */
    public static double sampsonDistance(Mat pt1, Mat pt2, Mat F) {
        return sampsonDistance_0(pt1.nativeObj, pt2.nativeObj, F.nativeObj);
    }

    /**
     * Computes an optimal affine transformation between two 3D point sets. It computes x\\ y\\ z\\ = a_{11} &amp;
     * a_{12} &amp; a_{13}\\ a_{21} &amp; a_{22} &amp; a_{23}\\ a_{31} &amp; a_{32} &amp; a_{33}\\ X\\ Y\\ Z\\ + b_1\\
     * b_2\\ b_3\\.
     *
     * @param src             First input 3D point set containing (X,Y,Z)
     * @param dst             Second input 3D point set containing (x,y,z)
     * @param out             Output 3D affine transformation matrix 3 times 4 of the form a_{11} &amp; a_{12} &amp;
     *                        a_{13} &amp; b_1\\ a_{21} &amp; a_{22} &amp; a_{23} &amp; b_2\\ a_{31} &amp; a_{32} &amp;
     *                        a_{33} &amp; b_3\\
     * @param inliers         Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @param ransacThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier
     * @param confidence      Confidence level, between 0 and 1, for the estimated transformation. Anything between 0.95
     *                        and 0.99 is usually good enough. Values too close to 1 can slow down the estimation
     *                        significantly. Values lower than 0.8-0.9 can result in an incorrectly estimated
     *                        transformation
     * @return Whether a solution was found. The function estimates an optimal 3D affine transformation between two 3D
     *         point sets using the RANSAC algorithm
     * @throws NullPointerException if src, dst, out, inliers is null
     */
    public static boolean estimateAffine3D(
            Mat src,
            Mat dst,
            Mat out,
            Mat inliers,
            double ransacThreshold,
            double confidence) {
        return estimateAffine3D_0(
                src.nativeObj,
                dst.nativeObj,
                out.nativeObj,
                inliers.nativeObj,
                ransacThreshold,
                confidence);
    }

    /**
     * Computes an optimal affine transformation between two 3D point sets. It computes x\\ y\\ z\\ = a_{11} &amp;
     * a_{12} &amp; a_{13}\\ a_{21} &amp; a_{22} &amp; a_{23}\\ a_{31} &amp; a_{32} &amp; a_{33}\\ X\\ Y\\ Z\\ + b_1\\
     * b_2\\ b_3\\.
     *
     * @param src             First input 3D point set containing (X,Y,Z)
     * @param dst             Second input 3D point set containing (x,y,z)
     * @param out             Output 3D affine transformation matrix 3 times 4 of the form a_{11} &amp; a_{12} &amp;
     *                        a_{13} &amp; b_1\\ a_{21} &amp; a_{22} &amp; a_{23} &amp; b_2\\ a_{31} &amp; a_{32} &amp;
     *                        a_{33} &amp; b_3\\
     * @param inliers         Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @param ransacThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier
     * @return Whether a solution was found. The function estimates an optimal 3D affine transformation between two 3D
     *         point sets using the RANSAC algorithm
     * @throws NullPointerException if src, dst, out, inliers is null
     */
    public static boolean estimateAffine3D(Mat src, Mat dst, Mat out, Mat inliers, double ransacThreshold) {
        return estimateAffine3D_1(src.nativeObj, dst.nativeObj, out.nativeObj, inliers.nativeObj, ransacThreshold);
    }

    /**
     * Computes an optimal affine transformation between two 3D point sets. It computes x\\ y\\ z\\ = a_{11} &amp;
     * a_{12} &amp; a_{13}\\ a_{21} &amp; a_{22} &amp; a_{23}\\ a_{31} &amp; a_{32} &amp; a_{33}\\ X\\ Y\\ Z\\ + b_1\\
     * b_2\\ b_3\\.
     *
     * @param src     First input 3D point set containing (X,Y,Z)
     * @param dst     Second input 3D point set containing (x,y,z)
     * @param out     Output 3D affine transformation matrix 3 times 4 of the form a_{11} &amp; a_{12} &amp; a_{13}
     *                &amp; b_1\\ a_{21} &amp; a_{22} &amp; a_{23} &amp; b_2\\ a_{31} &amp; a_{32} &amp; a_{33} &amp;
     *                b_3\\
     * @param inliers Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @return Whether a solution was found. The function estimates an optimal 3D affine transformation between two 3D
     *         point sets using the RANSAC algorithm
     * @throws NullPointerException if src, dst, out, inliers is null
     */
    public static boolean estimateAffine3D(Mat src, Mat dst, Mat out, Mat inliers) {
        return estimateAffine3D_2(src.nativeObj, dst.nativeObj, out.nativeObj, inliers.nativeObj);
    }

    /**
     * Computes an optimal affine transformation between two 3D point sets. It computes x\\ y\\ z\\ = a_{11} &amp;
     * a_{12} &amp; a_{13}\\ a_{21} &amp; a_{22} &amp; a_{23}\\ a_{31} &amp; a_{32} &amp; a_{33}\\ X\\ Y\\ Z\\ + b_1\\
     * b_2\\ b_3\\.
     *
     * @param src            First input 3D point set containing (X,Y,Z)
     * @param dst            Second input 3D point set containing (x,y,z)
     * @param scale          If null is passed, the scale parameter c will be assumed to be 1.0. Else the pointed-to
     *                       variable will be set to the optimal scale
     * @param force_rotation If true, the returned rotation will never be a reflection. This might be unwanted, e.g.
     *                       when optimizing a transform between a right- and a left-handed coordinate system
     * @return Whether a solution was found. The function estimates an optimal 3D affine transformation between two 3D
     *         point sets using the RANSAC algorithm
     * @throws NullPointerException if src, dst is null
     */
    public static Mat estimateAffine3D(Mat src, Mat dst, double[] scale, boolean force_rotation) {
        double[] scale_out = new double[1];
        Mat retVal = new Mat(estimateAffine3D_3(src.nativeObj, dst.nativeObj, scale_out, force_rotation));
        if (scale != null)
            scale[0] = (double) scale_out[0];
        return retVal;
    }

    /**
     * Computes an optimal affine transformation between two 3D point sets. It computes x\\ y\\ z\\ = a_{11} &amp;
     * a_{12} &amp; a_{13}\\ a_{21} &amp; a_{22} &amp; a_{23}\\ a_{31} &amp; a_{32} &amp; a_{33}\\ X\\ Y\\ Z\\ + b_1\\
     * b_2\\ b_3\\.
     *
     * @param src   First input 3D point set containing (X,Y,Z)
     * @param dst   Second input 3D point set containing (x,y,z)
     * @param scale If null is passed, the scale parameter c will be assumed to be 1.0. Else the pointed-to variable
     *              will be set to the optimal scale. This might be unwanted, e.g. when optimizing a transform between a
     *              right- and a left-handed coordinate system
     * @return Whether a solution was found. The function estimates an optimal 3D affine transformation between two 3D
     *         point sets using the RANSAC algorithm
     * @throws NullPointerException if src, dst is null
     */
    public static Mat estimateAffine3D(Mat src, Mat dst, double[] scale) {
        double[] scale_out = new double[1];
        Mat retVal = new Mat(estimateAffine3D_4(src.nativeObj, dst.nativeObj, scale_out));
        if (scale != null)
            scale[0] = (double) scale_out[0];
        return retVal;
    }

    /**
     * Computes an optimal affine transformation between two 3D point sets. It computes x\\ y\\ z\\ = a_{11} &amp;
     * a_{12} &amp; a_{13}\\ a_{21} &amp; a_{22} &amp; a_{23}\\ a_{31} &amp; a_{32} &amp; a_{33}\\ X\\ Y\\ Z\\ + b_1\\
     * b_2\\ b_3\\.
     *
     * @param src First input 3D point set containing (X,Y,Z)
     * @param dst Second input 3D point set containing (x,y,z)
     * @return Whether a solution was found. The function estimates an optimal 3D affine transformation between two 3D
     *         point sets using the RANSAC algorithm
     * @throws NullPointerException if src, dst is null
     */
    public static Mat estimateAffine3D(Mat src, Mat dst) {
        return new Mat(estimateAffine3D_5(src.nativeObj, dst.nativeObj));
    }

    /**
     * Computes an optimal translation between two 3D point sets. It computes x\\ y\\ z\\ = X\\ Y\\ Z\\ + b_1\\ b_2\\
     * b_3\\.
     *
     * @param src             First input 3D point set containing (X,Y,Z)
     * @param dst             Second input 3D point set containing (x,y,z)
     * @param out             Output 3D translation vector 3 times 1 of the form b_1 \\ b_2 \\ b_3 \\
     * @param inliers         Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @param ransacThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier
     * @param confidence      Confidence level, between 0 and 1, for the estimated transformation. Anything between 0.95
     *                        and 0.99 is usually good enough. Values too close to 1 can slow down the estimation
     *                        significantly. Values lower than 0.8-0.9 can result in an incorrectly estimated
     *                        transformation
     * @return Whether a translation was found. The function estimates an optimal 3D translation between two 3D point
     *         sets using the RANSAC algorithm
     * @throws NullPointerException if src, dst, out, inliers is null
     */
    public static boolean estimateTranslation3D(
            Mat src,
            Mat dst,
            Mat out,
            Mat inliers,
            double ransacThreshold,
            double confidence) {
        return estimateTranslation3D_0(
                src.nativeObj,
                dst.nativeObj,
                out.nativeObj,
                inliers.nativeObj,
                ransacThreshold,
                confidence);
    }

    /**
     * Computes an optimal translation between two 3D point sets. It computes x\\ y\\ z\\ = X\\ Y\\ Z\\ + b_1\\ b_2\\
     * b_3\\.
     *
     * @param src             First input 3D point set containing (X,Y,Z)
     * @param dst             Second input 3D point set containing (x,y,z)
     * @param out             Output 3D translation vector 3 times 1 of the form b_1 \\ b_2 \\ b_3 \\
     * @param inliers         Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @param ransacThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier
     * @return Whether a translation was found. The function estimates an optimal 3D translation between two 3D point
     *         sets using the RANSAC algorithm
     * @throws NullPointerException if src, dst, out, inliers is null
     */
    public static boolean estimateTranslation3D(Mat src, Mat dst, Mat out, Mat inliers, double ransacThreshold) {
        return estimateTranslation3D_1(src.nativeObj, dst.nativeObj, out.nativeObj, inliers.nativeObj, ransacThreshold);
    }

    /**
     * Computes an optimal translation between two 3D point sets. It computes x\\ y\\ z\\ = X\\ Y\\ Z\\ + b_1\\ b_2\\
     * b_3\\.
     *
     * @param src     First input 3D point set containing (X,Y,Z)
     * @param dst     Second input 3D point set containing (x,y,z)
     * @param out     Output 3D translation vector 3 times 1 of the form b_1 \\ b_2 \\ b_3 \\
     * @param inliers Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @return Whether a translation was found. The function estimates an optimal 3D translation between two 3D point
     *         sets using the RANSAC algorithm
     * @throws NullPointerException if src, dst, out, inliers is null
     */
    public static boolean estimateTranslation3D(Mat src, Mat dst, Mat out, Mat inliers) {
        return estimateTranslation3D_2(src.nativeObj, dst.nativeObj, out.nativeObj, inliers.nativeObj);
    }

    /**
     * Computes an optimal affine transformation between two 2D point sets. It computes x\\ y\\ = a_{11} &amp; a_{12}\\
     * a_{21} &amp; a_{22}\\ X\\ Y\\ + b_1\\ b_2\\.
     *
     * @param from                  First input 2D point set containing (X,Y)
     * @param to                    Second input 2D point set containing (x,y)
     * @param inliers               Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @param method                Robust method used to compute transformation. The following methods are possible:
     *                              REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method
     *                              RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @param confidence            Confidence level, between 0 and 1, for the estimated transformation. Anything
     *                              between 0.95 and 0.99 is usually good enough. Values too close to 1 can slow down
     *                              the estimation significantly. Values lower than 0.8-0.9 can result in an incorrectly
     *                              estimated transformation
     * @param refineIters           Maximum number of iterations of refining algorithm (Levenberg-Marquardt). Passing 0
     *                              will disable refining, so the output matrix will be output of robust method
     * @return Output 2D affine transformation matrix 2 times 3 or empty matrix if transformation could not be
     *         estimated. The returned matrix has the following form: a_{11} &amp; a_{12} &amp; b_1\\ a_{21} &amp;
     *         a_{22} &amp; b_2\\ The function estimates an optimal 2D affine transformation between two 2D point sets
     *         using the selected robust algorithm. The computed transformation is then refined further (using only
     *         inliers) with the Levenberg-Marquardt method to reduce the re-projection error even more. Note: The
     *         RANSAC method can handle practically any ratio of outliers but needs a threshold to distinguish inliers
     *         from outliers. The method LMeDS does not need any threshold but it works correctly only when there are
     *         more than 50% of inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static Mat estimateAffine2D(
            Mat from,
            Mat to,
            Mat inliers,
            int method,
            double ransacReprojThreshold,
            long maxIters,
            double confidence,
            long refineIters) {
        return new Mat(estimateAffine2D_0(
                from.nativeObj,
                to.nativeObj,
                inliers.nativeObj,
                method,
                ransacReprojThreshold,
                maxIters,
                confidence,
                refineIters));
    }

    /**
     * Computes an optimal affine transformation between two 2D point sets. It computes x\\ y\\ = a_{11} &amp; a_{12}\\
     * a_{21} &amp; a_{22}\\ X\\ Y\\ + b_1\\ b_2\\.
     *
     * @param from                  First input 2D point set containing (X,Y)
     * @param to                    Second input 2D point set containing (x,y)
     * @param inliers               Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @param method                Robust method used to compute transformation. The following methods are possible:
     *                              REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method
     *                              RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @param confidence            Confidence level, between 0 and 1, for the estimated transformation. Anything
     *                              between 0.95 and 0.99 is usually good enough. Values too close to 1 can slow down
     *                              the estimation significantly. Values lower than 0.8-0.9 can result in an incorrectly
     *                              estimated transformation
     * @return Output 2D affine transformation matrix 2 times 3 or empty matrix if transformation could not be
     *         estimated. The returned matrix has the following form: a_{11} &amp; a_{12} &amp; b_1\\ a_{21} &amp;
     *         a_{22} &amp; b_2\\ The function estimates an optimal 2D affine transformation between two 2D point sets
     *         using the selected robust algorithm. The computed transformation is then refined further (using only
     *         inliers) with the Levenberg-Marquardt method to reduce the re-projection error even more. Note: The
     *         RANSAC method can handle practically any ratio of outliers but needs a threshold to distinguish inliers
     *         from outliers. The method LMeDS does not need any threshold but it works correctly only when there are
     *         more than 50% of inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static Mat estimateAffine2D(
            Mat from,
            Mat to,
            Mat inliers,
            int method,
            double ransacReprojThreshold,
            long maxIters,
            double confidence) {
        return new Mat(estimateAffine2D_1(
                from.nativeObj,
                to.nativeObj,
                inliers.nativeObj,
                method,
                ransacReprojThreshold,
                maxIters,
                confidence));
    }

    /**
     * Computes an optimal affine transformation between two 2D point sets. It computes x\\ y\\ = a_{11} &amp; a_{12}\\
     * a_{21} &amp; a_{22}\\ X\\ Y\\ + b_1\\ b_2\\.
     *
     * @param from                  First input 2D point set containing (X,Y)
     * @param to                    Second input 2D point set containing (x,y)
     * @param inliers               Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @param method                Robust method used to compute transformation. The following methods are possible:
     *                              REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method
     *                              RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @return Output 2D affine transformation matrix 2 times 3 or empty matrix if transformation could not be
     *         estimated. The returned matrix has the following form: a_{11} &amp; a_{12} &amp; b_1\\ a_{21} &amp;
     *         a_{22} &amp; b_2\\ The function estimates an optimal 2D affine transformation between two 2D point sets
     *         using the selected robust algorithm. The computed transformation is then refined further (using only
     *         inliers) with the Levenberg-Marquardt method to reduce the re-projection error even more. Note: The
     *         RANSAC method can handle practically any ratio of outliers but needs a threshold to distinguish inliers
     *         from outliers. The method LMeDS does not need any threshold but it works correctly only when there are
     *         more than 50% of inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static Mat estimateAffine2D(
            Mat from,
            Mat to,
            Mat inliers,
            int method,
            double ransacReprojThreshold,
            long maxIters) {
        return new Mat(estimateAffine2D_2(
                from.nativeObj,
                to.nativeObj,
                inliers.nativeObj,
                method,
                ransacReprojThreshold,
                maxIters));
    }

    /**
     * Computes an optimal affine transformation between two 2D point sets. It computes x\\ y\\ = a_{11} &amp; a_{12}\\
     * a_{21} &amp; a_{22}\\ X\\ Y\\ + b_1\\ b_2\\.
     *
     * @param from                  First input 2D point set containing (X,Y)
     * @param to                    Second input 2D point set containing (x,y)
     * @param inliers               Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @param method                Robust method used to compute transformation. The following methods are possible:
     *                              REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method
     *                              RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @return Output 2D affine transformation matrix 2 times 3 or empty matrix if transformation could not be
     *         estimated. The returned matrix has the following form: a_{11} &amp; a_{12} &amp; b_1\\ a_{21} &amp;
     *         a_{22} &amp; b_2\\ The function estimates an optimal 2D affine transformation between two 2D point sets
     *         using the selected robust algorithm. The computed transformation is then refined further (using only
     *         inliers) with the Levenberg-Marquardt method to reduce the re-projection error even more. Note: The
     *         RANSAC method can handle practically any ratio of outliers but needs a threshold to distinguish inliers
     *         from outliers. The method LMeDS does not need any threshold but it works correctly only when there are
     *         more than 50% of inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static Mat estimateAffine2D(Mat from, Mat to, Mat inliers, int method, double ransacReprojThreshold) {
        return new Mat(
                estimateAffine2D_3(from.nativeObj, to.nativeObj, inliers.nativeObj, method, ransacReprojThreshold));
    }

    /**
     * Computes an optimal affine transformation between two 2D point sets. It computes x\\ y\\ = a_{11} &amp; a_{12}\\
     * a_{21} &amp; a_{22}\\ X\\ Y\\ + b_1\\ b_2\\.
     *
     * @param from    First input 2D point set containing (X,Y)
     * @param to      Second input 2D point set containing (x,y)
     * @param inliers Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @param method  Robust method used to compute transformation. The following methods are possible: REF: RANSAC -
     *                RANSAC-based robust method REF: LMEDS - Least-Median robust method RANSAC is the default method
     * @return Output 2D affine transformation matrix 2 times 3 or empty matrix if transformation could not be
     *         estimated. The returned matrix has the following form: a_{11} &amp; a_{12} &amp; b_1\\ a_{21} &amp;
     *         a_{22} &amp; b_2\\ The function estimates an optimal 2D affine transformation between two 2D point sets
     *         using the selected robust algorithm. The computed transformation is then refined further (using only
     *         inliers) with the Levenberg-Marquardt method to reduce the re-projection error even more. Note: The
     *         RANSAC method can handle practically any ratio of outliers but needs a threshold to distinguish inliers
     *         from outliers. The method LMeDS does not need any threshold but it works correctly only when there are
     *         more than 50% of inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static Mat estimateAffine2D(Mat from, Mat to, Mat inliers, int method) {
        return new Mat(estimateAffine2D_4(from.nativeObj, to.nativeObj, inliers.nativeObj, method));
    }

    /**
     * Computes an optimal affine transformation between two 2D point sets. It computes x\\ y\\ = a_{11} &amp; a_{12}\\
     * a_{21} &amp; a_{22}\\ X\\ Y\\ + b_1\\ b_2\\.
     *
     * @param from    First input 2D point set containing (X,Y)
     * @param to      Second input 2D point set containing (x,y)
     * @param inliers Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @return Output 2D affine transformation matrix 2 times 3 or empty matrix if transformation could not be
     *         estimated. The returned matrix has the following form: a_{11} &amp; a_{12} &amp; b_1\\ a_{21} &amp;
     *         a_{22} &amp; b_2\\ The function estimates an optimal 2D affine transformation between two 2D point sets
     *         using the selected robust algorithm. The computed transformation is then refined further (using only
     *         inliers) with the Levenberg-Marquardt method to reduce the re-projection error even more. Note: The
     *         RANSAC method can handle practically any ratio of outliers but needs a threshold to distinguish inliers
     *         from outliers. The method LMeDS does not need any threshold but it works correctly only when there are
     *         more than 50% of inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static Mat estimateAffine2D(Mat from, Mat to, Mat inliers) {
        return new Mat(estimateAffine2D_5(from.nativeObj, to.nativeObj, inliers.nativeObj));
    }

    /**
     * Computes an optimal affine transformation between two 2D point sets. It computes x\\ y\\ = a_{11} &amp; a_{12}\\
     * a_{21} &amp; a_{22}\\ X\\ Y\\ + b_1\\ b_2\\.
     *
     * @param from First input 2D point set containing (X,Y)
     * @param to   Second input 2D point set containing (x,y)
     * @return Output 2D affine transformation matrix 2 times 3 or empty matrix if transformation could not be
     *         estimated. The returned matrix has the following form: a_{11} &amp; a_{12} &amp; b_1\\ a_{21} &amp;
     *         a_{22} &amp; b_2\\ The function estimates an optimal 2D affine transformation between two 2D point sets
     *         using the selected robust algorithm. The computed transformation is then refined further (using only
     *         inliers) with the Levenberg-Marquardt method to reduce the re-projection error even more. Note: The
     *         RANSAC method can handle practically any ratio of outliers but needs a threshold to distinguish inliers
     *         from outliers. The method LMeDS does not need any threshold but it works correctly only when there are
     *         more than 50% of inliers
     * @throws NullPointerException if from, to is null
     */
    public static Mat estimateAffine2D(Mat from, Mat to) {
        return new Mat(estimateAffine2D_6(from.nativeObj, to.nativeObj));
    }

    /**
     * Computes an optimal affine transformation between two 2D point sets. It computes x\\ y\\ = a_{11} &amp; a_{12}\\
     * a_{21} &amp; a_{22}\\ X\\ Y\\ + b_1\\ b_2\\.
     *
     * @param pts1    point coordinates in the first image
     * @param pts2    corresponding point coordinates in the second image
     * @param inliers Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @param params  robust-estimation sampling, scoring, threshold and refinement settings
     * @return Output 2D affine transformation matrix 2 times 3 or empty matrix if transformation could not be
     *         estimated. The returned matrix has the following form: a_{11} &amp; a_{12} &amp; b_1\\ a_{21} &amp;
     *         a_{22} &amp; b_2\\ The function estimates an optimal 2D affine transformation between two 2D point sets
     *         using the selected robust algorithm. The computed transformation is then refined further (using only
     *         inliers) with the Levenberg-Marquardt method to reduce the re-projection error even more. Note: The
     *         RANSAC method can handle practically any ratio of outliers but needs a threshold to distinguish inliers
     *         from outliers. The method LMeDS does not need any threshold but it works correctly only when there are
     *         more than 50% of inliers
     * @throws NullPointerException if pts1, pts2, inliers, params is null
     */
    public static Mat estimateAffine2D(Mat pts1, Mat pts2, Mat inliers, UsacParams params) {
        return new Mat(
                estimateAffine2D_7(pts1.nativeObj, pts2.nativeObj, inliers.nativeObj, params.getNativeObjAddr()));
    }

    /**
     * Computes an optimal limited affine transformation with 4 degrees of freedom between two 2D point sets.
     *
     * @param from                  First input 2D point set
     * @param to                    Second input 2D point set
     * @param inliers               Output vector indicating which points are inliers
     * @param method                Robust method used to compute transformation. The following methods are possible:
     *                              REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method
     *                              RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @param confidence            Confidence level, between 0 and 1, for the estimated transformation. Anything
     *                              between 0.95 and 0.99 is usually good enough. Values too close to 1 can slow down
     *                              the estimation significantly. Values lower than 0.8-0.9 can result in an incorrectly
     *                              estimated transformation
     * @param refineIters           Maximum number of iterations of refining algorithm (Levenberg-Marquardt). Passing 0
     *                              will disable refining, so the output matrix will be output of robust method
     * @return Output 2D affine transformation (4 degrees of freedom) matrix 2 times 3 or empty matrix if transformation
     *         could not be estimated. The function estimates an optimal 2D affine transformation with 4 degrees of
     *         freedom limited to combinations of translation, rotation, and uniform scaling. Uses the selected
     *         algorithm for robust estimation. The computed transformation is then refined further (using only inliers)
     *         with the Levenberg-Marquardt method to reduce the re-projection error even more. Estimated transformation
     *         matrix is: cos(theta) cdot s &amp; -sin(theta) cdot s &amp; t_x \\ sin(theta) cdot s &amp; cos(theta)
     *         cdot s &amp; t_y Where theta is the rotation angle, s the scaling factor and t_x, t_y are translations in
     *         x, y axes respectively. Note: The RANSAC method can handle practically any ratio of outliers but need a
     *         threshold to distinguish inliers from outliers. The method LMeDS does not need any threshold but it works
     *         correctly only when there are more than 50% of inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static Mat estimateAffinePartial2D(
            Mat from,
            Mat to,
            Mat inliers,
            int method,
            double ransacReprojThreshold,
            long maxIters,
            double confidence,
            long refineIters) {
        return new Mat(estimateAffinePartial2D_0(
                from.nativeObj,
                to.nativeObj,
                inliers.nativeObj,
                method,
                ransacReprojThreshold,
                maxIters,
                confidence,
                refineIters));
    }

    /**
     * Computes an optimal limited affine transformation with 4 degrees of freedom between two 2D point sets.
     *
     * @param from                  First input 2D point set
     * @param to                    Second input 2D point set
     * @param inliers               Output vector indicating which points are inliers
     * @param method                Robust method used to compute transformation. The following methods are possible:
     *                              REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method
     *                              RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @param confidence            Confidence level, between 0 and 1, for the estimated transformation. Anything
     *                              between 0.95 and 0.99 is usually good enough. Values too close to 1 can slow down
     *                              the estimation significantly. Values lower than 0.8-0.9 can result in an incorrectly
     *                              estimated transformation
     * @return Output 2D affine transformation (4 degrees of freedom) matrix 2 times 3 or empty matrix if transformation
     *         could not be estimated. The function estimates an optimal 2D affine transformation with 4 degrees of
     *         freedom limited to combinations of translation, rotation, and uniform scaling. Uses the selected
     *         algorithm for robust estimation. The computed transformation is then refined further (using only inliers)
     *         with the Levenberg-Marquardt method to reduce the re-projection error even more. Estimated transformation
     *         matrix is: cos(theta) cdot s &amp; -sin(theta) cdot s &amp; t_x \\ sin(theta) cdot s &amp; cos(theta)
     *         cdot s &amp; t_y Where theta is the rotation angle, s the scaling factor and t_x, t_y are translations in
     *         x, y axes respectively. Note: The RANSAC method can handle practically any ratio of outliers but need a
     *         threshold to distinguish inliers from outliers. The method LMeDS does not need any threshold but it works
     *         correctly only when there are more than 50% of inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static Mat estimateAffinePartial2D(
            Mat from,
            Mat to,
            Mat inliers,
            int method,
            double ransacReprojThreshold,
            long maxIters,
            double confidence) {
        return new Mat(estimateAffinePartial2D_1(
                from.nativeObj,
                to.nativeObj,
                inliers.nativeObj,
                method,
                ransacReprojThreshold,
                maxIters,
                confidence));
    }

    /**
     * Computes an optimal limited affine transformation with 4 degrees of freedom between two 2D point sets.
     *
     * @param from                  First input 2D point set
     * @param to                    Second input 2D point set
     * @param inliers               Output vector indicating which points are inliers
     * @param method                Robust method used to compute transformation. The following methods are possible:
     *                              REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method
     *                              RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @return Output 2D affine transformation (4 degrees of freedom) matrix 2 times 3 or empty matrix if transformation
     *         could not be estimated. The function estimates an optimal 2D affine transformation with 4 degrees of
     *         freedom limited to combinations of translation, rotation, and uniform scaling. Uses the selected
     *         algorithm for robust estimation. The computed transformation is then refined further (using only inliers)
     *         with the Levenberg-Marquardt method to reduce the re-projection error even more. Estimated transformation
     *         matrix is: cos(theta) cdot s &amp; -sin(theta) cdot s &amp; t_x \\ sin(theta) cdot s &amp; cos(theta)
     *         cdot s &amp; t_y Where theta is the rotation angle, s the scaling factor and t_x, t_y are translations in
     *         x, y axes respectively. Note: The RANSAC method can handle practically any ratio of outliers but need a
     *         threshold to distinguish inliers from outliers. The method LMeDS does not need any threshold but it works
     *         correctly only when there are more than 50% of inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static Mat estimateAffinePartial2D(
            Mat from,
            Mat to,
            Mat inliers,
            int method,
            double ransacReprojThreshold,
            long maxIters) {
        return new Mat(estimateAffinePartial2D_2(
                from.nativeObj,
                to.nativeObj,
                inliers.nativeObj,
                method,
                ransacReprojThreshold,
                maxIters));
    }

    /**
     * Computes an optimal limited affine transformation with 4 degrees of freedom between two 2D point sets.
     *
     * @param from                  First input 2D point set
     * @param to                    Second input 2D point set
     * @param inliers               Output vector indicating which points are inliers
     * @param method                Robust method used to compute transformation. The following methods are possible:
     *                              REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method
     *                              RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @return Output 2D affine transformation (4 degrees of freedom) matrix 2 times 3 or empty matrix if transformation
     *         could not be estimated. The function estimates an optimal 2D affine transformation with 4 degrees of
     *         freedom limited to combinations of translation, rotation, and uniform scaling. Uses the selected
     *         algorithm for robust estimation. The computed transformation is then refined further (using only inliers)
     *         with the Levenberg-Marquardt method to reduce the re-projection error even more. Estimated transformation
     *         matrix is: cos(theta) cdot s &amp; -sin(theta) cdot s &amp; t_x \\ sin(theta) cdot s &amp; cos(theta)
     *         cdot s &amp; t_y Where theta is the rotation angle, s the scaling factor and t_x, t_y are translations in
     *         x, y axes respectively. Note: The RANSAC method can handle practically any ratio of outliers but need a
     *         threshold to distinguish inliers from outliers. The method LMeDS does not need any threshold but it works
     *         correctly only when there are more than 50% of inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static Mat estimateAffinePartial2D(Mat from, Mat to, Mat inliers, int method, double ransacReprojThreshold) {
        return new Mat(estimateAffinePartial2D_3(
                from.nativeObj,
                to.nativeObj,
                inliers.nativeObj,
                method,
                ransacReprojThreshold));
    }

    /**
     * Computes an optimal limited affine transformation with 4 degrees of freedom between two 2D point sets.
     *
     * @param from    First input 2D point set
     * @param to      Second input 2D point set
     * @param inliers Output vector indicating which points are inliers
     * @param method  Robust method used to compute transformation. The following methods are possible: REF: RANSAC -
     *                RANSAC-based robust method REF: LMEDS - Least-Median robust method RANSAC is the default method
     * @return Output 2D affine transformation (4 degrees of freedom) matrix 2 times 3 or empty matrix if transformation
     *         could not be estimated. The function estimates an optimal 2D affine transformation with 4 degrees of
     *         freedom limited to combinations of translation, rotation, and uniform scaling. Uses the selected
     *         algorithm for robust estimation. The computed transformation is then refined further (using only inliers)
     *         with the Levenberg-Marquardt method to reduce the re-projection error even more. Estimated transformation
     *         matrix is: cos(theta) cdot s &amp; -sin(theta) cdot s &amp; t_x \\ sin(theta) cdot s &amp; cos(theta)
     *         cdot s &amp; t_y Where theta is the rotation angle, s the scaling factor and t_x, t_y are translations in
     *         x, y axes respectively. Note: The RANSAC method can handle practically any ratio of outliers but need a
     *         threshold to distinguish inliers from outliers. The method LMeDS does not need any threshold but it works
     *         correctly only when there are more than 50% of inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static Mat estimateAffinePartial2D(Mat from, Mat to, Mat inliers, int method) {
        return new Mat(estimateAffinePartial2D_4(from.nativeObj, to.nativeObj, inliers.nativeObj, method));
    }

    /**
     * Computes an optimal limited affine transformation with 4 degrees of freedom between two 2D point sets.
     *
     * @param from    First input 2D point set
     * @param to      Second input 2D point set
     * @param inliers Output vector indicating which points are inliers
     * @return Output 2D affine transformation (4 degrees of freedom) matrix 2 times 3 or empty matrix if transformation
     *         could not be estimated. The function estimates an optimal 2D affine transformation with 4 degrees of
     *         freedom limited to combinations of translation, rotation, and uniform scaling. Uses the selected
     *         algorithm for robust estimation. The computed transformation is then refined further (using only inliers)
     *         with the Levenberg-Marquardt method to reduce the re-projection error even more. Estimated transformation
     *         matrix is: cos(theta) cdot s &amp; -sin(theta) cdot s &amp; t_x \\ sin(theta) cdot s &amp; cos(theta)
     *         cdot s &amp; t_y Where theta is the rotation angle, s the scaling factor and t_x, t_y are translations in
     *         x, y axes respectively. Note: The RANSAC method can handle practically any ratio of outliers but need a
     *         threshold to distinguish inliers from outliers. The method LMeDS does not need any threshold but it works
     *         correctly only when there are more than 50% of inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static Mat estimateAffinePartial2D(Mat from, Mat to, Mat inliers) {
        return new Mat(estimateAffinePartial2D_5(from.nativeObj, to.nativeObj, inliers.nativeObj));
    }

    /**
     * Computes an optimal limited affine transformation with 4 degrees of freedom between two 2D point sets.
     *
     * @param from First input 2D point set
     * @param to   Second input 2D point set
     * @return Output 2D affine transformation (4 degrees of freedom) matrix 2 times 3 or empty matrix if transformation
     *         could not be estimated. The function estimates an optimal 2D affine transformation with 4 degrees of
     *         freedom limited to combinations of translation, rotation, and uniform scaling. Uses the selected
     *         algorithm for robust estimation. The computed transformation is then refined further (using only inliers)
     *         with the Levenberg-Marquardt method to reduce the re-projection error even more. Estimated transformation
     *         matrix is: cos(theta) cdot s &amp; -sin(theta) cdot s &amp; t_x \\ sin(theta) cdot s &amp; cos(theta)
     *         cdot s &amp; t_y Where theta is the rotation angle, s the scaling factor and t_x, t_y are translations in
     *         x, y axes respectively. Note: The RANSAC method can handle practically any ratio of outliers but need a
     *         threshold to distinguish inliers from outliers. The method LMeDS does not need any threshold but it works
     *         correctly only when there are more than 50% of inliers
     * @throws NullPointerException if from, to is null
     */
    public static Mat estimateAffinePartial2D(Mat from, Mat to) {
        return new Mat(estimateAffinePartial2D_6(from.nativeObj, to.nativeObj));
    }

    /**
     * Computes a pure 2D translation between two 2D point sets. It computes x\\ y = 1 &amp; 0\\ 0 &amp; 1 X\\ Y + t_x\\
     * t_y .
     *
     * @param from                  First input 2D point set containing (X,Y)
     * @param to                    Second input 2D point set containing (x,y)
     * @param inliers               Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @param method                Robust method used to compute the transformation. The following methods are
     *                              possible: REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust
     *                              method RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @param confidence            Confidence level, between 0 and 1, for the estimated transformation. Anything
     *                              between 0.95 and 0.99 is usually good enough. Values too close to 1 can slow down
     *                              the estimation significantly. Values lower than 0.8u20130.9 can result in an
     *                              incorrectly estimated transformation
     * @param refineIters           Maximum number of iterations of the refining algorithm. For pure translation the
     *                              least-squares solution on inliers is closed-form, so passing 0 is recommended (no
     *                              additional refine)
     * @return A 2D translation vector [t_x, t_y]^T as . If the translation could not be estimated, both components are
     *         set to NaN and, if inliers is provided, the mask is filled with zeros. par Converting to a 2x3
     *         transformation matrix: 1 &amp; 0 &amp; t_x\\ 0 &amp; 1 &amp; t_y 2-component double vector t =
     *         estimateTranslation2D(from, to, inliers); Mat T = Mat_&lt;double&gt;({2,3}, {1,0,t[0], 0,1,t[1]}); The
     *         function estimates a pure 2D translation between two 2D point sets using the selected robust algorithm.
     *         Inliers are determined by the reprojection error threshold. Note: The RANSAC method can handle
     *         practically any ratio of outliers but needs a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but works correctly only when there are more than 50% inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static double[] estimateTranslation2D(
            Mat from,
            Mat to,
            Mat inliers,
            int method,
            double ransacReprojThreshold,
            long maxIters,
            double confidence,
            long refineIters) {
        return estimateTranslation2D_0(
                from.nativeObj,
                to.nativeObj,
                inliers.nativeObj,
                method,
                ransacReprojThreshold,
                maxIters,
                confidence,
                refineIters);
    }

    /**
     * Computes a pure 2D translation between two 2D point sets. It computes x\\ y = 1 &amp; 0\\ 0 &amp; 1 X\\ Y + t_x\\
     * t_y .
     *
     * @param from                  First input 2D point set containing (X,Y)
     * @param to                    Second input 2D point set containing (x,y)
     * @param inliers               Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @param method                Robust method used to compute the transformation. The following methods are
     *                              possible: REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust
     *                              method RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @param confidence            Confidence level, between 0 and 1, for the estimated transformation. Anything
     *                              between 0.95 and 0.99 is usually good enough. Values too close to 1 can slow down
     *                              the estimation significantly. Values lower than 0.8u20130.9 can result in an
     *                              incorrectly estimated transformation
     * @return A 2D translation vector [t_x, t_y]^T as . If the translation could not be estimated, both components are
     *         set to NaN and, if inliers is provided, the mask is filled with zeros. par Converting to a 2x3
     *         transformation matrix: 1 &amp; 0 &amp; t_x\\ 0 &amp; 1 &amp; t_y 2-component double vector t =
     *         estimateTranslation2D(from, to, inliers); Mat T = Mat_&lt;double&gt;({2,3}, {1,0,t[0], 0,1,t[1]}); The
     *         function estimates a pure 2D translation between two 2D point sets using the selected robust algorithm.
     *         Inliers are determined by the reprojection error threshold. Note: The RANSAC method can handle
     *         practically any ratio of outliers but needs a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but works correctly only when there are more than 50% inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static double[] estimateTranslation2D(
            Mat from,
            Mat to,
            Mat inliers,
            int method,
            double ransacReprojThreshold,
            long maxIters,
            double confidence) {
        return estimateTranslation2D_1(
                from.nativeObj,
                to.nativeObj,
                inliers.nativeObj,
                method,
                ransacReprojThreshold,
                maxIters,
                confidence);
    }

    /**
     * Computes a pure 2D translation between two 2D point sets. It computes x\\ y = 1 &amp; 0\\ 0 &amp; 1 X\\ Y + t_x\\
     * t_y .
     *
     * @param from                  First input 2D point set containing (X,Y)
     * @param to                    Second input 2D point set containing (x,y)
     * @param inliers               Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @param method                Robust method used to compute the transformation. The following methods are
     *                              possible: REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust
     *                              method RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @return A 2D translation vector [t_x, t_y]^T as . If the translation could not be estimated, both components are
     *         set to NaN and, if inliers is provided, the mask is filled with zeros. par Converting to a 2x3
     *         transformation matrix: 1 &amp; 0 &amp; t_x\\ 0 &amp; 1 &amp; t_y 2-component double vector t =
     *         estimateTranslation2D(from, to, inliers); Mat T = Mat_&lt;double&gt;({2,3}, {1,0,t[0], 0,1,t[1]}); The
     *         function estimates a pure 2D translation between two 2D point sets using the selected robust algorithm.
     *         Inliers are determined by the reprojection error threshold. Note: The RANSAC method can handle
     *         practically any ratio of outliers but needs a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but works correctly only when there are more than 50% inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static double[] estimateTranslation2D(
            Mat from,
            Mat to,
            Mat inliers,
            int method,
            double ransacReprojThreshold,
            long maxIters) {
        return estimateTranslation2D_2(
                from.nativeObj,
                to.nativeObj,
                inliers.nativeObj,
                method,
                ransacReprojThreshold,
                maxIters);
    }

    /**
     * Computes a pure 2D translation between two 2D point sets. It computes x\\ y = 1 &amp; 0\\ 0 &amp; 1 X\\ Y + t_x\\
     * t_y .
     *
     * @param from                  First input 2D point set containing (X,Y)
     * @param to                    Second input 2D point set containing (x,y)
     * @param inliers               Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @param method                Robust method used to compute the transformation. The following methods are
     *                              possible: REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust
     *                              method RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @return A 2D translation vector [t_x, t_y]^T as . If the translation could not be estimated, both components are
     *         set to NaN and, if inliers is provided, the mask is filled with zeros. par Converting to a 2x3
     *         transformation matrix: 1 &amp; 0 &amp; t_x\\ 0 &amp; 1 &amp; t_y 2-component double vector t =
     *         estimateTranslation2D(from, to, inliers); Mat T = Mat_&lt;double&gt;({2,3}, {1,0,t[0], 0,1,t[1]}); The
     *         function estimates a pure 2D translation between two 2D point sets using the selected robust algorithm.
     *         Inliers are determined by the reprojection error threshold. Note: The RANSAC method can handle
     *         practically any ratio of outliers but needs a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but works correctly only when there are more than 50% inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static double[] estimateTranslation2D(
            Mat from,
            Mat to,
            Mat inliers,
            int method,
            double ransacReprojThreshold) {
        return estimateTranslation2D_3(from.nativeObj, to.nativeObj, inliers.nativeObj, method, ransacReprojThreshold);
    }

    /**
     * Computes a pure 2D translation between two 2D point sets. It computes x\\ y = 1 &amp; 0\\ 0 &amp; 1 X\\ Y + t_x\\
     * t_y .
     *
     * @param from    First input 2D point set containing (X,Y)
     * @param to      Second input 2D point set containing (x,y)
     * @param inliers Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @param method  Robust method used to compute the transformation. The following methods are possible: REF: RANSAC
     *                - RANSAC-based robust method REF: LMEDS - Least-Median robust method RANSAC is the default method
     * @return A 2D translation vector [t_x, t_y]^T as . If the translation could not be estimated, both components are
     *         set to NaN and, if inliers is provided, the mask is filled with zeros. par Converting to a 2x3
     *         transformation matrix: 1 &amp; 0 &amp; t_x\\ 0 &amp; 1 &amp; t_y 2-component double vector t =
     *         estimateTranslation2D(from, to, inliers); Mat T = Mat_&lt;double&gt;({2,3}, {1,0,t[0], 0,1,t[1]}); The
     *         function estimates a pure 2D translation between two 2D point sets using the selected robust algorithm.
     *         Inliers are determined by the reprojection error threshold. Note: The RANSAC method can handle
     *         practically any ratio of outliers but needs a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but works correctly only when there are more than 50% inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static double[] estimateTranslation2D(Mat from, Mat to, Mat inliers, int method) {
        return estimateTranslation2D_4(from.nativeObj, to.nativeObj, inliers.nativeObj, method);
    }

    /**
     * Computes a pure 2D translation between two 2D point sets. It computes x\\ y = 1 &amp; 0\\ 0 &amp; 1 X\\ Y + t_x\\
     * t_y .
     *
     * @param from    First input 2D point set containing (X,Y)
     * @param to      Second input 2D point set containing (x,y)
     * @param inliers Output vector indicating which points are inliers (1-inlier, 0-outlier)
     * @return A 2D translation vector [t_x, t_y]^T as . If the translation could not be estimated, both components are
     *         set to NaN and, if inliers is provided, the mask is filled with zeros. par Converting to a 2x3
     *         transformation matrix: 1 &amp; 0 &amp; t_x\\ 0 &amp; 1 &amp; t_y 2-component double vector t =
     *         estimateTranslation2D(from, to, inliers); Mat T = Mat_&lt;double&gt;({2,3}, {1,0,t[0], 0,1,t[1]}); The
     *         function estimates a pure 2D translation between two 2D point sets using the selected robust algorithm.
     *         Inliers are determined by the reprojection error threshold. Note: The RANSAC method can handle
     *         practically any ratio of outliers but needs a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but works correctly only when there are more than 50% inliers
     * @throws NullPointerException if from, to, inliers is null
     */
    public static double[] estimateTranslation2D(Mat from, Mat to, Mat inliers) {
        return estimateTranslation2D_5(from.nativeObj, to.nativeObj, inliers.nativeObj);
    }

    /**
     * Computes a pure 2D translation between two 2D point sets. It computes x\\ y = 1 &amp; 0\\ 0 &amp; 1 X\\ Y + t_x\\
     * t_y .
     *
     * @param from First input 2D point set containing (X,Y)
     * @param to   Second input 2D point set containing (x,y)
     * @return A 2D translation vector [t_x, t_y]^T as . If the translation could not be estimated, both components are
     *         set to NaN and, if inliers is provided, the mask is filled with zeros. par Converting to a 2x3
     *         transformation matrix: 1 &amp; 0 &amp; t_x\\ 0 &amp; 1 &amp; t_y 2-component double vector t =
     *         estimateTranslation2D(from, to, inliers); Mat T = Mat_&lt;double&gt;({2,3}, {1,0,t[0], 0,1,t[1]}); The
     *         function estimates a pure 2D translation between two 2D point sets using the selected robust algorithm.
     *         Inliers are determined by the reprojection error threshold. Note: The RANSAC method can handle
     *         practically any ratio of outliers but needs a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but works correctly only when there are more than 50% inliers
     * @throws NullPointerException if from, to is null
     */
    public static double[] estimateTranslation2D(Mat from, Mat to) {
        return estimateTranslation2D_6(from.nativeObj, to.nativeObj);
    }

    /**
     * Decompose a homography matrix to rotation(s), translation(s) and plane normal(s).
     *
     * @param H            The input homography matrix between two images
     * @param K            The input camera intrinsic matrix
     * @param rotations    Array of rotation matrices
     * @param translations Array of translation matrices
     * @param normals      Array of plane normal matrices. This function extracts relative camera motion between two
     *                     views of a planar object and returns up to four mathematical solution tuples of rotation,
     *                     translation, and plane normal. The decomposition of the homography matrix H is described in
     *                     detail in If the homography H, induced by the plane, gives the constraint s_i
     *                     vecthree{x'_i}{y'_i}{1} sim H vecthree{x_i}{y_i}{1} on the source image points p_i and the
     *                     destination image points p'_i, then the tuple of rotations[k] and translations[k] is a change
     *                     of basis from the source camera's coordinate system to the destination camera's coordinate
     *                     system. However, by decomposing H, one can only get the translation normalized by the
     *                     (typically unknown) depth of the scene, i.e. its direction but with normalized length. If
     *                     point correspondences are available, at least two solutions may further be invalidated, by
     *                     applying positive depth constraint, i.e. all points must be in front of the camera
     * @return the number of returned motion solutions
     * @throws NullPointerException if H, K is null
     */
    public static int decomposeHomographyMat(
            Mat H,
            Mat K,
            List<Mat> rotations,
            List<Mat> translations,
            List<Mat> normals) {
        Mat rotations_mat = new Mat();
        Mat translations_mat = new Mat();
        Mat normals_mat = new Mat();
        int retVal = decomposeHomographyMat_0(
                H.nativeObj,
                K.nativeObj,
                rotations_mat.nativeObj,
                translations_mat.nativeObj,
                normals_mat.nativeObj);
        Converters.Mat_to_vector_Mat(rotations_mat, rotations);
        rotations_mat.release();
        Converters.Mat_to_vector_Mat(translations_mat, translations);
        translations_mat.release();
        Converters.Mat_to_vector_Mat(normals_mat, normals);
        normals_mat.release();
        return retVal;
    }

    /**
     * Filters homography decompositions based on additional information.
     *
     * @param rotations         Vector of rotation matrices
     * @param normals           Vector of plane normal matrices
     * @param beforePoints      Vector of (rectified) visible reference points before the homography is applied
     * @param afterPoints       Vector of (rectified) visible reference points after the homography is applied
     * @param possibleSolutions Vector of int indices representing the viable solution set after filtering
     * @param pointsMask        optional Mat/Vector of CV_8U, CV_8S or CV_Bool type representing the mask for the
     *                          inliers as given by the findHomography function This function is intended to filter the
     *                          output of the decomposeHomographyMat based on additional information as described in The
     *                          summary of the method: the decomposeHomographyMat function returns 2 unique solutions
     *                          and their "opposites" for a total of 4 solutions. If we have access to the sets of
     *                          points visible in the camera frame before and after the homography transformation is
     *                          applied, we can determine which are the true potential solutions and which are the
     *                          opposites by verifying which homographies are consistent with all visible reference
     *                          points being in front of the camera. The inputs are left unchanged; the filtered
     *                          solution set is returned as indices into the existing one
     * @throws NullPointerException if beforePoints, afterPoints, possibleSolutions, pointsMask is null
     */
    public static void filterHomographyDecompByVisibleRefpoints(
            List<Mat> rotations,
            List<Mat> normals,
            Mat beforePoints,
            Mat afterPoints,
            Mat possibleSolutions,
            Mat pointsMask) {
        Mat rotations_mat = Converters.vector_Mat_to_Mat(rotations);
        Mat normals_mat = Converters.vector_Mat_to_Mat(normals);
        filterHomographyDecompByVisibleRefpoints_0(
                rotations_mat.nativeObj,
                normals_mat.nativeObj,
                beforePoints.nativeObj,
                afterPoints.nativeObj,
                possibleSolutions.nativeObj,
                pointsMask.nativeObj);
    }

    /**
     * Filters homography decompositions based on additional information.
     *
     * @param rotations         Vector of rotation matrices
     * @param normals           Vector of plane normal matrices
     * @param beforePoints      Vector of (rectified) visible reference points before the homography is applied
     * @param afterPoints       Vector of (rectified) visible reference points after the homography is applied
     * @param possibleSolutions Vector of int indices representing the viable solution set after filtering
     * @throws NullPointerException if beforePoints, afterPoints, possibleSolutions is null
     */
    public static void filterHomographyDecompByVisibleRefpoints(
            List<Mat> rotations,
            List<Mat> normals,
            Mat beforePoints,
            Mat afterPoints,
            Mat possibleSolutions) {
        Mat rotations_mat = Converters.vector_Mat_to_Mat(rotations);
        Mat normals_mat = Converters.vector_Mat_to_Mat(normals);
        filterHomographyDecompByVisibleRefpoints_1(
                rotations_mat.nativeObj,
                normals_mat.nativeObj,
                beforePoints.nativeObj,
                afterPoints.nativeObj,
                possibleSolutions.nativeObj);
    }

    /**
     * Computes useful camera characteristics from the camera intrinsic matrix.
     *
     * @param cameraMatrix   Input camera intrinsic matrix that can be estimated by calibrateCamera or stereoCalibrate
     * @param imageSize      Input image size in pixels
     * @param apertureWidth  Physical width in mm of the sensor
     * @param apertureHeight Physical height in mm of the sensor
     * @param fovx           Output field of view in degrees along the horizontal sensor axis
     * @param fovy           Output field of view in degrees along the vertical sensor axis
     * @param focalLength    Focal length of the lens in mm
     * @param principalPoint Principal point in mm
     * @param aspectRatio    f_y/f_x The function computes various useful camera characteristics from the previously
     *                       estimated camera matrix. Note: Do keep in mind that the unity measure 'mm' stands for
     *                       whatever unit of measure one chooses for the chessboard pitch (it can thus be any value)
     * @throws NullPointerException if cameraMatrix, imageSize is null
     */
    public static void calibrationMatrixValues(
            Mat cameraMatrix,
            Size imageSize,
            double apertureWidth,
            double apertureHeight,
            double[] fovx,
            double[] fovy,
            double[] focalLength,
            Point principalPoint,
            double[] aspectRatio) {
        double[] fovx_out = new double[1];
        double[] fovy_out = new double[1];
        double[] focalLength_out = new double[1];
        double[] principalPoint_out = new double[2];
        double[] aspectRatio_out = new double[1];
        calibrationMatrixValues_0(
                cameraMatrix.nativeObj,
                imageSize.width,
                imageSize.height,
                apertureWidth,
                apertureHeight,
                fovx_out,
                fovy_out,
                focalLength_out,
                principalPoint_out,
                aspectRatio_out);
        if (fovx != null)
            fovx[0] = (double) fovx_out[0];
        if (fovy != null)
            fovy[0] = (double) fovy_out[0];
        if (focalLength != null)
            focalLength[0] = (double) focalLength_out[0];
        if (principalPoint != null) {
            principalPoint.x = principalPoint_out[0];
            principalPoint.y = principalPoint_out[1];
        }
        if (aspectRatio != null)
            aspectRatio[0] = (double) aspectRatio_out[0];
    }

    /**
     * Returns the default new camera matrix.
     *
     * @param cameraMatrix         Input camera matrix
     * @param imgsize              Camera view image size in pixels
     * @param centerPrincipalPoint Location of the principal point in the new camera matrix. The parameter indicates
     *                             whether this location should be at the image center or not
     * @return a copy of the camera matrix, with the principal point optionally moved to the image center
     * @throws NullPointerException if cameraMatrix, imgsize is null
     */
    public static Mat getDefaultNewCameraMatrix(Mat cameraMatrix, Size imgsize, boolean centerPrincipalPoint) {
        return new Mat(getDefaultNewCameraMatrix_0(
                cameraMatrix.nativeObj,
                imgsize.width,
                imgsize.height,
                centerPrincipalPoint));
    }

    /**
     * Returns the default new camera matrix.
     *
     * @param cameraMatrix Input camera matrix
     * @param imgsize      Camera view image size in pixels
     * @return a copy of the camera matrix, with the principal point optionally moved to the image center
     * @throws NullPointerException if cameraMatrix, imgsize is null
     */
    public static Mat getDefaultNewCameraMatrix(Mat cameraMatrix, Size imgsize) {
        return new Mat(getDefaultNewCameraMatrix_1(cameraMatrix.nativeObj, imgsize.width, imgsize.height));
    }

    /**
     * Returns the default new camera matrix.
     *
     * @param cameraMatrix Input camera matrix
     * @return a copy of the camera matrix, with the principal point optionally moved to the image center
     * @throws NullPointerException if cameraMatrix is null
     */
    public static Mat getDefaultNewCameraMatrix(Mat cameraMatrix) {
        return new Mat(getDefaultNewCameraMatrix_2(cameraMatrix.nativeObj));
    }

    /**
     * Returns the new camera intrinsic matrix based on the free scaling parameter.
     *
     * @param cameraMatrix         Input camera intrinsic matrix
     * @param distCoeffs           Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the
     *                             zero distortion coefficients are assumed
     * @param imageSize            Original image size
     * @param alpha                Free scaling parameter between 0 (when all the pixels in the undistorted image are
     *                             valid) and 1 (when all the source image pixels are retained in the undistorted
     *                             image). See stereoRectify for details
     * @param newImgSize           Image size after rectification. By default, it is set to imageSize
     * @param validPixROI          Optional output rectangle that outlines all-good-pixels region in the undistorted
     *                             image. See roi1, roi2 description in stereoRectify
     * @param centerPrincipalPoint Optional flag that indicates whether in the new camera intrinsic matrix the principal
     *                             point should be at the image center or not. By default, the principal point is chosen
     *                             to best fit a subset of the source image (determined by alpha) to the corrected image
     * @return new_camera_matrix Output new camera intrinsic matrix. The function computes and returns the optimal new
     *         camera intrinsic matrix based on the free scaling parameter. By varying this parameter, you may retrieve
     *         only sensible pixels alpha=0 , keep all the original image pixels if there is valuable information in the
     *         corners alpha=1 , or get something in between. When alpha&gt;0 , the undistorted result is likely to have
     *         some black pixels corresponding to "virtual" pixels outside of the captured distorted image. The original
     *         camera intrinsic matrix, distortion coefficients, the computed new camera intrinsic matrix, and
     *         newImageSize should be passed to initUndistortRectifyMap to produce the maps for remap
     * @throws NullPointerException if cameraMatrix, distCoeffs, imageSize, newImgSize is null
     */
    public static Mat getOptimalNewCameraMatrix(
            Mat cameraMatrix,
            Mat distCoeffs,
            Size imageSize,
            double alpha,
            Size newImgSize,
            Rect validPixROI,
            boolean centerPrincipalPoint) {
        double[] validPixROI_out = new double[4];
        Mat retVal = new Mat(getOptimalNewCameraMatrix_0(
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                imageSize.width,
                imageSize.height,
                alpha,
                newImgSize.width,
                newImgSize.height,
                validPixROI_out,
                centerPrincipalPoint));
        if (validPixROI != null) {
            validPixROI.x = (int) validPixROI_out[0];
            validPixROI.y = (int) validPixROI_out[1];
            validPixROI.width = (int) validPixROI_out[2];
            validPixROI.height = (int) validPixROI_out[3];
        }
        return retVal;
    }

    /**
     * Returns the new camera intrinsic matrix based on the free scaling parameter.
     *
     * @param cameraMatrix Input camera intrinsic matrix
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                     distortion coefficients are assumed
     * @param imageSize    Original image size
     * @param alpha        Free scaling parameter between 0 (when all the pixels in the undistorted image are valid) and
     *                     1 (when all the source image pixels are retained in the undistorted image). See stereoRectify
     *                     for details
     * @param newImgSize   Image size after rectification. By default, it is set to imageSize
     * @param validPixROI  Optional output rectangle that outlines all-good-pixels region in the undistorted image. See
     *                     roi1, roi2 description in stereoRectify
     * @return new_camera_matrix Output new camera intrinsic matrix. The function computes and returns the optimal new
     *         camera intrinsic matrix based on the free scaling parameter. By varying this parameter, you may retrieve
     *         only sensible pixels alpha=0 , keep all the original image pixels if there is valuable information in the
     *         corners alpha=1 , or get something in between. When alpha&gt;0 , the undistorted result is likely to have
     *         some black pixels corresponding to "virtual" pixels outside of the captured distorted image. The original
     *         camera intrinsic matrix, distortion coefficients, the computed new camera intrinsic matrix, and
     *         newImageSize should be passed to initUndistortRectifyMap to produce the maps for remap
     * @throws NullPointerException if cameraMatrix, distCoeffs, imageSize, newImgSize is null
     */
    public static Mat getOptimalNewCameraMatrix(
            Mat cameraMatrix,
            Mat distCoeffs,
            Size imageSize,
            double alpha,
            Size newImgSize,
            Rect validPixROI) {
        double[] validPixROI_out = new double[4];
        Mat retVal = new Mat(getOptimalNewCameraMatrix_1(
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                imageSize.width,
                imageSize.height,
                alpha,
                newImgSize.width,
                newImgSize.height,
                validPixROI_out));
        if (validPixROI != null) {
            validPixROI.x = (int) validPixROI_out[0];
            validPixROI.y = (int) validPixROI_out[1];
            validPixROI.width = (int) validPixROI_out[2];
            validPixROI.height = (int) validPixROI_out[3];
        }
        return retVal;
    }

    /**
     * Returns the new camera intrinsic matrix based on the free scaling parameter.
     *
     * @param cameraMatrix Input camera intrinsic matrix
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                     distortion coefficients are assumed
     * @param imageSize    Original image size
     * @param alpha        Free scaling parameter between 0 (when all the pixels in the undistorted image are valid) and
     *                     1 (when all the source image pixels are retained in the undistorted image). See stereoRectify
     *                     for details
     * @param newImgSize   Image size after rectification. By default, it is set to imageSize
     * @return new_camera_matrix Output new camera intrinsic matrix. The function computes and returns the optimal new
     *         camera intrinsic matrix based on the free scaling parameter. By varying this parameter, you may retrieve
     *         only sensible pixels alpha=0 , keep all the original image pixels if there is valuable information in the
     *         corners alpha=1 , or get something in between. When alpha&gt;0 , the undistorted result is likely to have
     *         some black pixels corresponding to "virtual" pixels outside of the captured distorted image. The original
     *         camera intrinsic matrix, distortion coefficients, the computed new camera intrinsic matrix, and
     *         newImageSize should be passed to initUndistortRectifyMap to produce the maps for remap
     * @throws NullPointerException if cameraMatrix, distCoeffs, imageSize, newImgSize is null
     */
    public static Mat getOptimalNewCameraMatrix(
            Mat cameraMatrix,
            Mat distCoeffs,
            Size imageSize,
            double alpha,
            Size newImgSize) {
        return new Mat(getOptimalNewCameraMatrix_2(
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                imageSize.width,
                imageSize.height,
                alpha,
                newImgSize.width,
                newImgSize.height));
    }

    /**
     * Returns the new camera intrinsic matrix based on the free scaling parameter.
     *
     * @param cameraMatrix Input camera intrinsic matrix
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                     distortion coefficients are assumed
     * @param imageSize    Original image size
     * @param alpha        Free scaling parameter between 0 (when all the pixels in the undistorted image are valid) and
     *                     1 (when all the source image pixels are retained in the undistorted image). See stereoRectify
     *                     for details
     * @return new_camera_matrix Output new camera intrinsic matrix. The function computes and returns the optimal new
     *         camera intrinsic matrix based on the free scaling parameter. By varying this parameter, you may retrieve
     *         only sensible pixels alpha=0 , keep all the original image pixels if there is valuable information in the
     *         corners alpha=1 , or get something in between. When alpha&gt;0 , the undistorted result is likely to have
     *         some black pixels corresponding to "virtual" pixels outside of the captured distorted image. The original
     *         camera intrinsic matrix, distortion coefficients, the computed new camera intrinsic matrix, and
     *         newImageSize should be passed to initUndistortRectifyMap to produce the maps for remap
     * @throws NullPointerException if cameraMatrix, distCoeffs, imageSize is null
     */
    public static Mat getOptimalNewCameraMatrix(Mat cameraMatrix, Mat distCoeffs, Size imageSize, double alpha) {
        return new Mat(getOptimalNewCameraMatrix_3(
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                imageSize.width,
                imageSize.height,
                alpha));
    }

    /**
     * Computes the ideal point coordinates from the observed point coordinates.
     *
     * @param src          Observed point coordinates in pixel coordinates of the distorted image, 2xN/Nx2 1-channel or
     *                     1xN/Nx1 2-channel (CV_32FC2 or CV_64FC2) (or vector&lt;Point2f&gt; )
     * @param dst          Output ideal point coordinates (1xN/Nx1 2-channel or vector&lt;Point2f&gt; ) after
     *                     undistortion and reverse perspective transformation. If matrix P is identity or omitted, dst
     *                     will contain normalized point coordinates
     * @param cameraMatrix Camera matrix vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs   Input vector of distortion coefficients (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1, s_2,
     *                     s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is NULL/empty, the
     *                     zero distortion coefficients are assumed
     * @param R            Rectification transformation in the object space (3x3 matrix). R1 or R2 computed by
     *                     stereoRectify can be passed here. If the matrix is empty, the identity transformation is used
     * @param P            New camera matrix (3x3) or new projection matrix (3x4) {f'}_x &amp; 0 &amp; {c'}_x &amp; t_x
     *                     \\ 0 &amp; {f'}_y &amp; {c'}_y &amp; t_y \\ 0 &amp; 0 &amp; 1 &amp; t_z . P1 or P2 computed
     *                     by stereoRectify can be passed here. If the matrix is empty, the identity new camera matrix
     *                     is used and output will be in normalized coordinates
     * @param criteria     Termination criteria for the iterative point undistortion algorithm. The default runs a fixed
     *                     5 iterations, see the note above. Keep TermCriteria::COUNT in the type when using
     *                     TermCriteria::EPS: with EPS alone there is no iteration limit
     * @throws NullPointerException if cameraMatrix, distCoeffs, R, P, criteria is null
     */
    public static void undistortPoints(
            MatOfPoint2f src,
            MatOfPoint2f dst,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat R,
            Mat P,
            TermCriteria criteria) {
        Mat src_mat = src;
        Mat dst_mat = dst;
        undistortPoints_0(
                src_mat.nativeObj,
                dst_mat.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                R.nativeObj,
                P.nativeObj,
                criteria.type,
                criteria.maxCount,
                criteria.epsilon);
    }

    /**
     * Computes the ideal point coordinates from the observed point coordinates.
     *
     * @param src          Observed point coordinates in pixel coordinates of the distorted image, 2xN/Nx2 1-channel or
     *                     1xN/Nx1 2-channel (CV_32FC2 or CV_64FC2) (or vector&lt;Point2f&gt; )
     * @param dst          Output ideal point coordinates (1xN/Nx1 2-channel or vector&lt;Point2f&gt; ) after
     *                     undistortion and reverse perspective transformation. If matrix P is identity or omitted, dst
     *                     will contain normalized point coordinates
     * @param cameraMatrix Camera matrix vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs   Input vector of distortion coefficients (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1, s_2,
     *                     s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is NULL/empty, the
     *                     zero distortion coefficients are assumed
     * @param R            Rectification transformation in the object space (3x3 matrix). R1 or R2 computed by
     *                     stereoRectify can be passed here. If the matrix is empty, the identity transformation is used
     * @param P            New camera matrix (3x3) or new projection matrix (3x4) {f'}_x &amp; 0 &amp; {c'}_x &amp; t_x
     *                     \\ 0 &amp; {f'}_y &amp; {c'}_y &amp; t_y \\ 0 &amp; 0 &amp; 1 &amp; t_z . P1 or P2 computed
     *                     by stereoRectify can be passed here. If the matrix is empty, the identity new camera matrix
     *                     is used and output will be in normalized coordinates
     * @throws NullPointerException if cameraMatrix, distCoeffs, R, P is null
     */
    public static void undistortPoints(
            MatOfPoint2f src,
            MatOfPoint2f dst,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat R,
            Mat P) {
        Mat src_mat = src;
        Mat dst_mat = dst;
        undistortPoints_1(
                src_mat.nativeObj,
                dst_mat.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                R.nativeObj,
                P.nativeObj);
    }

    /**
     * Computes the ideal point coordinates from the observed point coordinates.
     *
     * @param src          Observed point coordinates in pixel coordinates of the distorted image, 2xN/Nx2 1-channel or
     *                     1xN/Nx1 2-channel (CV_32FC2 or CV_64FC2) (or vector&lt;Point2f&gt; )
     * @param dst          Output ideal point coordinates (1xN/Nx1 2-channel or vector&lt;Point2f&gt; ) after
     *                     undistortion and reverse perspective transformation. If matrix P is identity or omitted, dst
     *                     will contain normalized point coordinates
     * @param cameraMatrix Camera matrix vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs   Input vector of distortion coefficients (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1, s_2,
     *                     s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is NULL/empty, the
     *                     zero distortion coefficients are assumed
     * @param R            Rectification transformation in the object space (3x3 matrix). R1 or R2 computed by
     *                     stereoRectify can be passed here. If the matrix is empty, the identity transformation is used
     * @throws NullPointerException if cameraMatrix, distCoeffs, R is null
     */
    public static void undistortPoints(MatOfPoint2f src, MatOfPoint2f dst, Mat cameraMatrix, Mat distCoeffs, Mat R) {
        Mat src_mat = src;
        Mat dst_mat = dst;
        undistortPoints_2(
                src_mat.nativeObj,
                dst_mat.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                R.nativeObj);
    }

    /**
     * Computes the ideal point coordinates from the observed point coordinates.
     *
     * @param src          Observed point coordinates in pixel coordinates of the distorted image, 2xN/Nx2 1-channel or
     *                     1xN/Nx1 2-channel (CV_32FC2 or CV_64FC2) (or vector&lt;Point2f&gt; )
     * @param dst          Output ideal point coordinates (1xN/Nx1 2-channel or vector&lt;Point2f&gt; ) after
     *                     undistortion and reverse perspective transformation. If matrix P is identity or omitted, dst
     *                     will contain normalized point coordinates
     * @param cameraMatrix Camera matrix vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs   Input vector of distortion coefficients (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1, s_2,
     *                     s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is NULL/empty, the
     *                     zero distortion coefficients are assumed
     * @throws NullPointerException if cameraMatrix, distCoeffs is null
     */
    public static void undistortPoints(MatOfPoint2f src, MatOfPoint2f dst, Mat cameraMatrix, Mat distCoeffs) {
        Mat src_mat = src;
        Mat dst_mat = dst;
        undistortPoints_3(src_mat.nativeObj, dst_mat.nativeObj, cameraMatrix.nativeObj, distCoeffs.nativeObj);
    }

    /**
     * Compute undistorted image points position.
     *
     * @param src          Observed points position, 2xN/Nx2 1-channel or 1xN/Nx1 2-channel (CV_32FC2 or CV_64FC2) (or
     *                     vector&lt;Point2f&gt; )
     * @param dst          Output undistorted points position (1xN/Nx1 2-channel or vector&lt;Point2f&gt; )
     * @param cameraMatrix Camera matrix vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs   Distortion coefficients
     * @param criteria     Termination criteria for the iterative undistortion, as in undistortPoints. The default runs
     *                     a fixed 5 iterations and ignores epsilon
     * @throws NullPointerException if src, dst, cameraMatrix, distCoeffs, criteria is null
     */
    public static void undistortImagePoints(Mat src, Mat dst, Mat cameraMatrix, Mat distCoeffs, TermCriteria criteria) {
        undistortImagePoints_0(
                src.nativeObj,
                dst.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                criteria.type,
                criteria.maxCount,
                criteria.epsilon);
    }

    /**
     * Compute undistorted image points position.
     *
     * @param src          Observed points position, 2xN/Nx2 1-channel or 1xN/Nx1 2-channel (CV_32FC2 or CV_64FC2) (or
     *                     vector&lt;Point2f&gt; )
     * @param dst          Output undistorted points position (1xN/Nx1 2-channel or vector&lt;Point2f&gt; )
     * @param cameraMatrix Camera matrix vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs   Distortion coefficients
     * @throws NullPointerException if src, dst, cameraMatrix, distCoeffs is null
     */
    public static void undistortImagePoints(Mat src, Mat dst, Mat cameraMatrix, Mat distCoeffs) {
        undistortImagePoints_1(src.nativeObj, dst.nativeObj, cameraMatrix.nativeObj, distCoeffs.nativeObj);
    }

    /**
     * Projects three-dimensional points using the fisheye camera model.
     *
     * @param objectPoints three-dimensional object coordinates expressed in the object reference frame
     * @param imagePoints  two-dimensional coordinates in the image plane
     * @param rvec         rotation vector in Rodrigues form
     * @param tvec         translation vector between object and camera coordinates
     * @param K            number of nearest neighbors retained for each query row
     * @param D            fisheye distortion coefficient vector
     * @param alpha        fisheye camera skew coefficient
     * @param jacobian     output matrix of derivatives of the projected image coordinates
     * @throws NullPointerException if objectPoints, imagePoints, rvec, tvec, K, D, jacobian is null
     */
    public static void fisheye_projectPoints(
            Mat objectPoints,
            Mat imagePoints,
            Mat rvec,
            Mat tvec,
            Mat K,
            Mat D,
            double alpha,
            Mat jacobian) {
        fisheye_projectPoints_0(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                K.nativeObj,
                D.nativeObj,
                alpha,
                jacobian.nativeObj);
    }

    /**
     * Projects three-dimensional points using the fisheye camera model.
     *
     * @param objectPoints three-dimensional object coordinates expressed in the object reference frame
     * @param imagePoints  two-dimensional coordinates in the image plane
     * @param rvec         rotation vector in Rodrigues form
     * @param tvec         translation vector between object and camera coordinates
     * @param K            number of nearest neighbors retained for each query row
     * @param D            fisheye distortion coefficient vector
     * @param alpha        fisheye camera skew coefficient
     * @throws NullPointerException if objectPoints, imagePoints, rvec, tvec, K, D is null
     */
    public static void fisheye_projectPoints(
            Mat objectPoints,
            Mat imagePoints,
            Mat rvec,
            Mat tvec,
            Mat K,
            Mat D,
            double alpha) {
        fisheye_projectPoints_1(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                K.nativeObj,
                D.nativeObj,
                alpha);
    }

    /**
     * Projects three-dimensional points using the fisheye camera model.
     *
     * @param objectPoints three-dimensional object coordinates expressed in the object reference frame
     * @param imagePoints  two-dimensional coordinates in the image plane
     * @param rvec         rotation vector in Rodrigues form
     * @param tvec         translation vector between object and camera coordinates
     * @param K            number of nearest neighbors retained for each query row
     * @param D            fisheye distortion coefficient vector
     * @throws NullPointerException if objectPoints, imagePoints, rvec, tvec, K, D is null
     */
    public static void fisheye_projectPoints(Mat objectPoints, Mat imagePoints, Mat rvec, Mat tvec, Mat K, Mat D) {
        fisheye_projectPoints_2(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                K.nativeObj,
                D.nativeObj);
    }

    /**
     * Overload of distortPoints function to handle cases when undistorted points are got with non-identity camera
     * matrix, e.g. output of estimateNewCameraMatrixForUndistortRectify.
     *
     * @param undistorted Array of object points, 1xN/Nx1 2-channel (or vector&lt;Point2f&gt; ), where N is the number
     *                    of points in the view
     * @param distorted   Output array of image points, 1xN/Nx1 2-channel, or vector&lt;Point2f&gt;
     * @param K           Camera intrinsic matrix cameramatrix{K}
     * @param D           Input vector of distortion coefficients distcoeffsfisheye
     * @param alpha       The skew coefficient
     * @throws NullPointerException if undistorted, distorted, K, D is null
     */
    public static void fisheye_distortPoints(Mat undistorted, Mat distorted, Mat K, Mat D, double alpha) {
        fisheye_distortPoints_0(undistorted.nativeObj, distorted.nativeObj, K.nativeObj, D.nativeObj, alpha);
    }

    /**
     * Overload of distortPoints function to handle cases when undistorted points are got with non-identity camera
     * matrix, e.g. output of estimateNewCameraMatrixForUndistortRectify.
     *
     * @param undistorted Array of object points, 1xN/Nx1 2-channel (or vector&lt;Point2f&gt; ), where N is the number
     *                    of points in the view
     * @param distorted   Output array of image points, 1xN/Nx1 2-channel, or vector&lt;Point2f&gt;
     * @param K           Camera intrinsic matrix cameramatrix{K}
     * @param D           Input vector of distortion coefficients distcoeffsfisheye
     * @throws NullPointerException if undistorted, distorted, K, D is null
     */
    public static void fisheye_distortPoints(Mat undistorted, Mat distorted, Mat K, Mat D) {
        fisheye_distortPoints_1(undistorted.nativeObj, distorted.nativeObj, K.nativeObj, D.nativeObj);
    }

    /**
     * Overload of distortPoints function to handle cases when undistorted points are got with non-identity camera
     * matrix, e.g. output of estimateNewCameraMatrixForUndistortRectify.
     *
     * @param undistorted  Array of object points, 1xN/Nx1 2-channel (or vector&lt;Point2f&gt; ), where N is the number
     *                     of points in the view
     * @param distorted    Output array of image points, 1xN/Nx1 2-channel, or vector&lt;Point2f&gt;
     * @param Kundistorted Camera intrinsic matrix used as new camera matrix for undistortion
     * @param K            Camera intrinsic matrix cameramatrix{K}
     * @param D            Input vector of distortion coefficients distcoeffsfisheye
     * @param alpha        The skew coefficient
     * @throws NullPointerException if undistorted, distorted, Kundistorted, K, D is null
     */
    public static void fisheye_distortPoints(
            Mat undistorted,
            Mat distorted,
            Mat Kundistorted,
            Mat K,
            Mat D,
            double alpha) {
        fisheye_distortPoints_2(
                undistorted.nativeObj,
                distorted.nativeObj,
                Kundistorted.nativeObj,
                K.nativeObj,
                D.nativeObj,
                alpha);
    }

    /**
     * Overload of distortPoints function to handle cases when undistorted points are got with non-identity camera
     * matrix, e.g. output of estimateNewCameraMatrixForUndistortRectify.
     *
     * @param undistorted  Array of object points, 1xN/Nx1 2-channel (or vector&lt;Point2f&gt; ), where N is the number
     *                     of points in the view
     * @param distorted    Output array of image points, 1xN/Nx1 2-channel, or vector&lt;Point2f&gt;
     * @param Kundistorted Camera intrinsic matrix used as new camera matrix for undistortion
     * @param K            Camera intrinsic matrix cameramatrix{K}
     * @param D            Input vector of distortion coefficients distcoeffsfisheye
     * @throws NullPointerException if undistorted, distorted, Kundistorted, K, D is null
     */
    public static void fisheye_distortPoints(Mat undistorted, Mat distorted, Mat Kundistorted, Mat K, Mat D) {
        fisheye_distortPoints_3(
                undistorted.nativeObj,
                distorted.nativeObj,
                Kundistorted.nativeObj,
                K.nativeObj,
                D.nativeObj);
    }

    /**
     * Undistorts 2D points using fisheye camera model This function performs undistortion for fisheye camera models,
     * which use a different distortion model compared to the standard pinhole camera model used by undistortPoints. The
     * fisheye model is suitable for wide-angle cameras. The function transforms points from the distorted fisheye image
     * to undistorted coordinates, optionally applying a rectification transformation (R) and projecting to a new image
     * plane (P). Note: Coordinate Systems: Input (distorted): Points are expected in pixel coordinates of the distorted
     * fisheye image, i.e., coordinates measured in pixels from the top-left corner of the image. Output (undistorted):
     * The coordinate system depends on parameter P: If P is provided (not empty): Output points are in pixel
     * coordinates of the rectified/undistorted image plane, using the camera matrix P. If P is empty or identity:
     * Output points are in normalized camera coordinates (normalized image coordinates), which are dimensionless
     * coordinates in the camera's focal plane, independent of intrinsic parameters. Note: Fisheye vs. Standard Model:
     * Use this function (fisheye::undistortPoints) for fisheye cameras (wide-angle lenses). For standard pinhole
     * cameras, use undistortPoints instead. The fisheye model uses a different distortion parameterization (4
     * coefficients) compared to the standard model (4-14 coefficients).
     *
     * @param distorted   Array of distorted point coordinates in pixel coordinates of the fisheye image, 1xN/Nx1
     *                    2-channel (or vector&lt;Point2f&gt; ), where N is the number of points in the view
     * @param undistorted output coordinates after removing lens distortion
     * @param K           Camera intrinsic matrix cameramatrix{K} of the fisheye camera
     * @param D           Input vector of fisheye distortion coefficients distcoeffsfisheye (must contain exactly 4
     *                    coefficients)
     * @param R           Rectification transformation in the object space: 3x3 1-channel, or vector: 3x1/1x3 1-channel
     *                    or 1x1 3-channel. If empty, the identity transformation is used
     * @param P           New camera intrinsic matrix (3x3) or new projection matrix (3x4). If empty or identity, output
     *                    will be in normalized camera coordinates
     * @param criteria    Termination criteria for the iterative undistortion algorithm
     * @throws NullPointerException if distorted, undistorted, K, D, R, P, criteria is null
     */
    public static void fisheye_undistortPoints(
            Mat distorted,
            Mat undistorted,
            Mat K,
            Mat D,
            Mat R,
            Mat P,
            TermCriteria criteria) {
        fisheye_undistortPoints_0(
                distorted.nativeObj,
                undistorted.nativeObj,
                K.nativeObj,
                D.nativeObj,
                R.nativeObj,
                P.nativeObj,
                criteria.type,
                criteria.maxCount,
                criteria.epsilon);
    }

    /**
     * Undistorts 2D points using fisheye camera model This function performs undistortion for fisheye camera models,
     * which use a different distortion model compared to the standard pinhole camera model used by undistortPoints. The
     * fisheye model is suitable for wide-angle cameras. The function transforms points from the distorted fisheye image
     * to undistorted coordinates, optionally applying a rectification transformation (R) and projecting to a new image
     * plane (P). Note: Coordinate Systems: Input (distorted): Points are expected in pixel coordinates of the distorted
     * fisheye image, i.e., coordinates measured in pixels from the top-left corner of the image. Output (undistorted):
     * The coordinate system depends on parameter P: If P is provided (not empty): Output points are in pixel
     * coordinates of the rectified/undistorted image plane, using the camera matrix P. If P is empty or identity:
     * Output points are in normalized camera coordinates (normalized image coordinates), which are dimensionless
     * coordinates in the camera's focal plane, independent of intrinsic parameters. Note: Fisheye vs. Standard Model:
     * Use this function (fisheye::undistortPoints) for fisheye cameras (wide-angle lenses). For standard pinhole
     * cameras, use undistortPoints instead. The fisheye model uses a different distortion parameterization (4
     * coefficients) compared to the standard model (4-14 coefficients).
     *
     * @param distorted   Array of distorted point coordinates in pixel coordinates of the fisheye image, 1xN/Nx1
     *                    2-channel (or vector&lt;Point2f&gt; ), where N is the number of points in the view
     * @param undistorted output coordinates after removing lens distortion
     * @param K           Camera intrinsic matrix cameramatrix{K} of the fisheye camera
     * @param D           Input vector of fisheye distortion coefficients distcoeffsfisheye (must contain exactly 4
     *                    coefficients)
     * @param R           Rectification transformation in the object space: 3x3 1-channel, or vector: 3x1/1x3 1-channel
     *                    or 1x1 3-channel. If empty, the identity transformation is used
     * @param P           New camera intrinsic matrix (3x3) or new projection matrix (3x4). If empty or identity, output
     *                    will be in normalized camera coordinates
     * @throws NullPointerException if distorted, undistorted, K, D, R, P is null
     */
    public static void fisheye_undistortPoints(Mat distorted, Mat undistorted, Mat K, Mat D, Mat R, Mat P) {
        fisheye_undistortPoints_1(
                distorted.nativeObj,
                undistorted.nativeObj,
                K.nativeObj,
                D.nativeObj,
                R.nativeObj,
                P.nativeObj);
    }

    /**
     * Undistorts 2D points using fisheye camera model This function performs undistortion for fisheye camera models,
     * which use a different distortion model compared to the standard pinhole camera model used by undistortPoints. The
     * fisheye model is suitable for wide-angle cameras. The function transforms points from the distorted fisheye image
     * to undistorted coordinates, optionally applying a rectification transformation (R) and projecting to a new image
     * plane (P). Note: Coordinate Systems: Input (distorted): Points are expected in pixel coordinates of the distorted
     * fisheye image, i.e., coordinates measured in pixels from the top-left corner of the image. Output (undistorted):
     * The coordinate system depends on parameter P: If P is provided (not empty): Output points are in pixel
     * coordinates of the rectified/undistorted image plane, using the camera matrix P. If P is empty or identity:
     * Output points are in normalized camera coordinates (normalized image coordinates), which are dimensionless
     * coordinates in the camera's focal plane, independent of intrinsic parameters. Note: Fisheye vs. Standard Model:
     * Use this function (fisheye::undistortPoints) for fisheye cameras (wide-angle lenses). For standard pinhole
     * cameras, use undistortPoints instead. The fisheye model uses a different distortion parameterization (4
     * coefficients) compared to the standard model (4-14 coefficients).
     *
     * @param distorted   Array of distorted point coordinates in pixel coordinates of the fisheye image, 1xN/Nx1
     *                    2-channel (or vector&lt;Point2f&gt; ), where N is the number of points in the view
     * @param undistorted output coordinates after removing lens distortion
     * @param K           Camera intrinsic matrix cameramatrix{K} of the fisheye camera
     * @param D           Input vector of fisheye distortion coefficients distcoeffsfisheye (must contain exactly 4
     *                    coefficients)
     * @param R           Rectification transformation in the object space: 3x3 1-channel, or vector: 3x1/1x3 1-channel
     *                    or 1x1 3-channel. If empty, the identity transformation is used
     * @throws NullPointerException if distorted, undistorted, K, D, R is null
     */
    public static void fisheye_undistortPoints(Mat distorted, Mat undistorted, Mat K, Mat D, Mat R) {
        fisheye_undistortPoints_2(distorted.nativeObj, undistorted.nativeObj, K.nativeObj, D.nativeObj, R.nativeObj);
    }

    /**
     * Undistorts 2D points using fisheye camera model This function performs undistortion for fisheye camera models,
     * which use a different distortion model compared to the standard pinhole camera model used by undistortPoints. The
     * fisheye model is suitable for wide-angle cameras. The function transforms points from the distorted fisheye image
     * to undistorted coordinates, optionally applying a rectification transformation (R) and projecting to a new image
     * plane (P). Note: Coordinate Systems: Input (distorted): Points are expected in pixel coordinates of the distorted
     * fisheye image, i.e., coordinates measured in pixels from the top-left corner of the image. Output (undistorted):
     * The coordinate system depends on parameter P: If P is provided (not empty): Output points are in pixel
     * coordinates of the rectified/undistorted image plane, using the camera matrix P. If P is empty or identity:
     * Output points are in normalized camera coordinates (normalized image coordinates), which are dimensionless
     * coordinates in the camera's focal plane, independent of intrinsic parameters. Note: Fisheye vs. Standard Model:
     * Use this function (fisheye::undistortPoints) for fisheye cameras (wide-angle lenses). For standard pinhole
     * cameras, use undistortPoints instead. The fisheye model uses a different distortion parameterization (4
     * coefficients) compared to the standard model (4-14 coefficients).
     *
     * @param distorted   Array of distorted point coordinates in pixel coordinates of the fisheye image, 1xN/Nx1
     *                    2-channel (or vector&lt;Point2f&gt; ), where N is the number of points in the view
     * @param undistorted output coordinates after removing lens distortion
     * @param K           Camera intrinsic matrix cameramatrix{K} of the fisheye camera
     * @param D           Input vector of fisheye distortion coefficients distcoeffsfisheye (must contain exactly 4
     *                    coefficients)
     * @throws NullPointerException if distorted, undistorted, K, D is null
     */
    public static void fisheye_undistortPoints(Mat distorted, Mat undistorted, Mat K, Mat D) {
        fisheye_undistortPoints_3(distorted.nativeObj, undistorted.nativeObj, K.nativeObj, D.nativeObj);
    }

    /**
     * Estimates new camera intrinsic matrix for undistortion or rectification.
     *
     * @param K          Camera intrinsic matrix cameramatrix{K}
     * @param D          Input vector of distortion coefficients distcoeffsfisheye
     * @param image_size Size of the image
     * @param R          Rectification transformation in the object space: 3x3 1-channel, or vector: 3x1/1x3 1-channel
     *                   or 1x1 3-channel
     * @param P          New camera intrinsic matrix (3x3) or new projection matrix (3x4)
     * @param balance    Sets the new focal length in range between the min focal length and the max focal length.
     *                   Balance is in range of [0, 1]
     * @param new_size   the new size
     * @param fov_scale  Divisor for new focal length
     * @throws NullPointerException if K, D, image_size, R, P, new_size is null
     */
    public static void fisheye_estimateNewCameraMatrixForUndistortRectify(
            Mat K,
            Mat D,
            Size image_size,
            Mat R,
            Mat P,
            double balance,
            Size new_size,
            double fov_scale) {
        fisheye_estimateNewCameraMatrixForUndistortRectify_0(
                K.nativeObj,
                D.nativeObj,
                image_size.width,
                image_size.height,
                R.nativeObj,
                P.nativeObj,
                balance,
                new_size.width,
                new_size.height,
                fov_scale);
    }

    /**
     * Estimates new camera intrinsic matrix for undistortion or rectification.
     *
     * @param K          Camera intrinsic matrix cameramatrix{K}
     * @param D          Input vector of distortion coefficients distcoeffsfisheye
     * @param image_size Size of the image
     * @param R          Rectification transformation in the object space: 3x3 1-channel, or vector: 3x1/1x3 1-channel
     *                   or 1x1 3-channel
     * @param P          New camera intrinsic matrix (3x3) or new projection matrix (3x4)
     * @param balance    Sets the new focal length in range between the min focal length and the max focal length.
     *                   Balance is in range of [0, 1]
     * @param new_size   the new size
     * @throws NullPointerException if K, D, image_size, R, P, new_size is null
     */
    public static void fisheye_estimateNewCameraMatrixForUndistortRectify(
            Mat K,
            Mat D,
            Size image_size,
            Mat R,
            Mat P,
            double balance,
            Size new_size) {
        fisheye_estimateNewCameraMatrixForUndistortRectify_1(
                K.nativeObj,
                D.nativeObj,
                image_size.width,
                image_size.height,
                R.nativeObj,
                P.nativeObj,
                balance,
                new_size.width,
                new_size.height);
    }

    /**
     * Estimates new camera intrinsic matrix for undistortion or rectification.
     *
     * @param K          Camera intrinsic matrix cameramatrix{K}
     * @param D          Input vector of distortion coefficients distcoeffsfisheye
     * @param image_size Size of the image
     * @param R          Rectification transformation in the object space: 3x3 1-channel, or vector: 3x1/1x3 1-channel
     *                   or 1x1 3-channel
     * @param P          New camera intrinsic matrix (3x3) or new projection matrix (3x4)
     * @param balance    Sets the new focal length in range between the min focal length and the max focal length.
     *                   Balance is in range of [0, 1]
     * @throws NullPointerException if K, D, image_size, R, P is null
     */
    public static void fisheye_estimateNewCameraMatrixForUndistortRectify(
            Mat K,
            Mat D,
            Size image_size,
            Mat R,
            Mat P,
            double balance) {
        fisheye_estimateNewCameraMatrixForUndistortRectify_2(
                K.nativeObj,
                D.nativeObj,
                image_size.width,
                image_size.height,
                R.nativeObj,
                P.nativeObj,
                balance);
    }

    /**
     * Estimates new camera intrinsic matrix for undistortion or rectification.
     *
     * @param K          Camera intrinsic matrix cameramatrix{K}
     * @param D          Input vector of distortion coefficients distcoeffsfisheye
     * @param image_size Size of the image
     * @param R          Rectification transformation in the object space: 3x3 1-channel, or vector: 3x1/1x3 1-channel
     *                   or 1x1 3-channel
     * @param P          New camera intrinsic matrix (3x3) or new projection matrix (3x4)
     * @throws NullPointerException if K, D, image_size, R, P is null
     */
    public static void fisheye_estimateNewCameraMatrixForUndistortRectify(Mat K, Mat D, Size image_size, Mat R, Mat P) {
        fisheye_estimateNewCameraMatrixForUndistortRectify_3(
                K.nativeObj,
                D.nativeObj,
                image_size.width,
                image_size.height,
                R.nativeObj,
                P.nativeObj);
    }

    /**
     * Finds an object pose from 3D-2D point correspondences for fisheye camera model.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can also be passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients (4x1/1x4)
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param flags             Method for solving a PnP problem: see REF: calib3d_solvePnP_flags This function returns
     *                          the rotation and the translation vectors that transform a 3D point expressed in the
     *                          object coordinate frame to the camera coordinate frame, using different methods: P3P
     *                          methods (REF: SOLVEPNP_P3P, REF: SOLVEPNP_AP3P): need 4 input points to return a unique
     *                          solution. REF: SOLVEPNP_IPPE Input points must be &gt;= 4 and object points must be
     *                          coplanar. REF: SOLVEPNP_IPPE_SQUARE Special case suitable for marker pose estimation.
     *                          Number of input points must be 4. Object points must be defined in the following order:
     *                          point 0: [-squareLength / 2, squareLength / 2, 0] point 1: [ squareLength / 2,
     *                          squareLength / 2, 0] point 2: [ squareLength / 2, -squareLength / 2, 0] point 3:
     *                          [-squareLength / 2, -squareLength / 2, 0] for all the other flags, number of input
     *                          points must be &gt;= 4 and object points can be in any configuration
     * @param criteria          Termination criteria for internal undistortPoints call. The function internally
     *                          undistorts points with REF: undistortPoints and call REF: solvePnP, thus the input are
     *                          very similar. Check there and Perspective-n-Points is described in REF: calib3d_solvePnP
     *                          for more information
     * @return true if a pose is found with the fisheye model
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec, criteria is null
     */
    public static boolean fisheye_solvePnP(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess,
            int flags,
            TermCriteria criteria) {
        return fisheye_solvePnP_0(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess,
                flags,
                criteria.type,
                criteria.maxCount,
                criteria.epsilon);
    }

    /**
     * Finds an object pose from 3D-2D point correspondences for fisheye camera model.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can also be passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients (4x1/1x4)
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param flags             Method for solving a PnP problem: see REF: calib3d_solvePnP_flags This function returns
     *                          the rotation and the translation vectors that transform a 3D point expressed in the
     *                          object coordinate frame to the camera coordinate frame, using different methods: P3P
     *                          methods (REF: SOLVEPNP_P3P, REF: SOLVEPNP_AP3P): need 4 input points to return a unique
     *                          solution. REF: SOLVEPNP_IPPE Input points must be &gt;= 4 and object points must be
     *                          coplanar. REF: SOLVEPNP_IPPE_SQUARE Special case suitable for marker pose estimation.
     *                          Number of input points must be 4. Object points must be defined in the following order:
     *                          point 0: [-squareLength / 2, squareLength / 2, 0] point 1: [ squareLength / 2,
     *                          squareLength / 2, 0] point 2: [ squareLength / 2, -squareLength / 2, 0] point 3:
     *                          [-squareLength / 2, -squareLength / 2, 0] for all the other flags, number of input
     *                          points must be &gt;= 4 and object points can be in any configuration
     * @return true if a pose is found with the fisheye model
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec is null
     */
    public static boolean fisheye_solvePnP(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess,
            int flags) {
        return fisheye_solvePnP_1(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess,
                flags);
    }

    /**
     * Finds an object pose from 3D-2D point correspondences for fisheye camera model.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can also be passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients (4x1/1x4)
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @return true if a pose is found with the fisheye model
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec is null
     */
    public static boolean fisheye_solvePnP(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess) {
        return fisheye_solvePnP_2(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess);
    }

    /**
     * Finds an object pose from 3D-2D point correspondences for fisheye camera model.
     *
     * @param objectPoints Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                     where N is the number of points. vector&lt;Point3d&gt; can also be passed here
     * @param imagePoints  Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                     number of points. vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients (4x1/1x4)
     * @param rvec         Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points from the
     *                     model coordinate system to the camera coordinate system
     * @param tvec         Output translation vector
     * @return true if a pose is found with the fisheye model
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec is null
     */
    public static boolean fisheye_solvePnP(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec) {
        return fisheye_solvePnP_3(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj);
    }

    /**
     * Finds an object pose from 3D-2D point correspondences using the RANSAC scheme for fisheye camera moodel.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients (4x1/1x4)
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param iterationsCount   Number of iterations
     * @param reprojectionError reprojection-error threshold used to classify inliers
     * @param confidence        The probability that the algorithm produces a useful result
     * @param inliers           Output vector that contains indices of inliers in objectPoints and imagePoints
     * @param flags             Method for solving a PnP problem: see REF: calib3d_solvePnP_flags
     * @param criteria          Termination criteria for internal undistortPoints call. The function interally
     *                          undistorts points with REF: undistortPoints and call REF: solvePnP, thus the input are
     *                          very similar. More information about Perspective-n-Points is described in REF:
     *                          calib3d_solvePnP for more information
     * @return true if robust pose estimation succeeds with the fisheye model
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec, inliers,
     *                              criteria is null
     */
    public static boolean fisheye_solvePnPRansac(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError,
            double confidence,
            Mat inliers,
            int flags,
            TermCriteria criteria) {
        return fisheye_solvePnPRansac_0(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess,
                iterationsCount,
                reprojectionError,
                confidence,
                inliers.nativeObj,
                flags,
                criteria.type,
                criteria.maxCount,
                criteria.epsilon);
    }

    /**
     * Finds an object pose from 3D-2D point correspondences using the RANSAC scheme for fisheye camera moodel.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients (4x1/1x4)
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param iterationsCount   Number of iterations
     * @param reprojectionError reprojection-error threshold used to classify inliers
     * @param confidence        The probability that the algorithm produces a useful result
     * @param inliers           Output vector that contains indices of inliers in objectPoints and imagePoints
     * @param flags             Method for solving a PnP problem: see REF: calib3d_solvePnP_flags
     * @return true if robust pose estimation succeeds with the fisheye model
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec, inliers is null
     */
    public static boolean fisheye_solvePnPRansac(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError,
            double confidence,
            Mat inliers,
            int flags) {
        return fisheye_solvePnPRansac_1(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess,
                iterationsCount,
                reprojectionError,
                confidence,
                inliers.nativeObj,
                flags);
    }

    /**
     * Finds an object pose from 3D-2D point correspondences using the RANSAC scheme for fisheye camera moodel.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients (4x1/1x4)
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param iterationsCount   Number of iterations
     * @param reprojectionError reprojection-error threshold used to classify inliers
     * @param confidence        The probability that the algorithm produces a useful result
     * @param inliers           Output vector that contains indices of inliers in objectPoints and imagePoints
     * @return true if robust pose estimation succeeds with the fisheye model
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec, inliers is null
     */
    public static boolean fisheye_solvePnPRansac(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError,
            double confidence,
            Mat inliers) {
        return fisheye_solvePnPRansac_2(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess,
                iterationsCount,
                reprojectionError,
                confidence,
                inliers.nativeObj);
    }

    /**
     * Finds an object pose from 3D-2D point correspondences using the RANSAC scheme for fisheye camera moodel.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients (4x1/1x4)
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param iterationsCount   Number of iterations
     * @param reprojectionError reprojection-error threshold used to classify inliers
     * @param confidence        The probability that the algorithm produces a useful result
     * @return true if robust pose estimation succeeds with the fisheye model
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec is null
     */
    public static boolean fisheye_solvePnPRansac(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError,
            double confidence) {
        return fisheye_solvePnPRansac_3(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess,
                iterationsCount,
                reprojectionError,
                confidence);
    }

    /**
     * Finds an object pose from 3D-2D point correspondences using the RANSAC scheme for fisheye camera moodel.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients (4x1/1x4)
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param iterationsCount   Number of iterations
     * @param reprojectionError reprojection-error threshold used to classify inliers
     * @return true if robust pose estimation succeeds with the fisheye model
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec is null
     */
    public static boolean fisheye_solvePnPRansac(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError) {
        return fisheye_solvePnPRansac_4(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess,
                iterationsCount,
                reprojectionError);
    }

    /**
     * Finds an object pose from 3D-2D point correspondences using the RANSAC scheme for fisheye camera moodel.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients (4x1/1x4)
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @param iterationsCount   Number of iterations
     * @return true if robust pose estimation succeeds with the fisheye model
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec is null
     */
    public static boolean fisheye_solvePnPRansac(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess,
            int iterationsCount) {
        return fisheye_solvePnPRansac_5(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess,
                iterationsCount);
    }

    /**
     * Finds an object pose from 3D-2D point correspondences using the RANSAC scheme for fisheye camera moodel.
     *
     * @param objectPoints      Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1
     *                          3-channel, where N is the number of points. vector&lt;Point3d&gt; can be also passed
     *                          here
     * @param imagePoints       Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                          number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix      Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs        Input vector of distortion coefficients (4x1/1x4)
     * @param rvec              Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points
     *                          from the model coordinate system to the camera coordinate system
     * @param tvec              Output translation vector
     * @param useExtrinsicGuess Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided rvec
     *                          and tvec values as initial approximations of the rotation and translation vectors,
     *                          respectively, and further optimizes them
     * @return true if robust pose estimation succeeds with the fisheye model
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec is null
     */
    public static boolean fisheye_solvePnPRansac(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec,
            boolean useExtrinsicGuess) {
        return fisheye_solvePnPRansac_6(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                useExtrinsicGuess);
    }

    /**
     * Finds an object pose from 3D-2D point correspondences using the RANSAC scheme for fisheye camera moodel.
     *
     * @param objectPoints Array of object points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                     where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints  Array of corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                     number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix Input camera intrinsic matrix cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients (4x1/1x4)
     * @param rvec         Output rotation vector (see REF: Rodrigues ) that, together with tvec, brings points from the
     *                     model coordinate system to the camera coordinate system
     * @param tvec         Output translation vector
     * @return true if robust pose estimation succeeds with the fisheye model
     * @throws NullPointerException if objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec is null
     */
    public static boolean fisheye_solvePnPRansac(
            Mat objectPoints,
            Mat imagePoints,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec) {
        return fisheye_solvePnPRansac_7(
                objectPoints.nativeObj,
                imagePoints.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj);
    }

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#normalEstimate(Mat,Mat,Mat,List<Mat>,int)}.
     *
     * @param normals_nativeObj    native matrix or object handle for {@code normals}; Normal of each point, support
     *                             vector&lt;Point3f&gt; and Mat of size Nx3
     * @param curvatures_nativeObj native matrix or object handle for {@code curvatures}; Curvature of each point,
     *                             support vector&lt;float&gt; and Mat
     * @param input_pts_nativeObj  native matrix or object handle for {@code input_pts}; Original point cloud, support
     *                             vector&lt;Point3f&gt; and Mat of size Nx3/3xN
     * @param nn_idx_mat_nativeObj native matrix or object handle for {@code nn_idx_mat}; Index information of nearest
     *                             neighbors of all points. The first nearest neighbor of each point is itself. Support
     *                             vector&lt;vector&lt;int&gt;&gt;, List&lt;Mat&gt; and Mat of size NxK. If the
     *                             information in a row is [0, 2, 1, -5, -1, 4, 7 ... negative number], it will use only
     *                             non-negative indexes until it meets a negative number or bound of this row i.e. [0,
     *                             2, 1]. If left empty (noArray()), the neighbors are computed internally with a
     *                             kd-tree and max_neighbor_num is used as the number of neighbors k (must be &gt;= 2)
     * @param max_neighbor_num     The maximum number of neighbors want to use including itself. Setting to a
     *                             non-positive number or default will use the information from nn_idx. When nn_idx is
     *                             empty this is the number of neighbors k to search for and must be &gt;= 2
     */
    private static native void normalEstimate_0(
            long normals_nativeObj,
            long curvatures_nativeObj,
            long input_pts_nativeObj,
            long nn_idx_mat_nativeObj,
            int max_neighbor_num);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#normalEstimate(Mat,Mat,Mat,List<Mat>)}.
     *
     * @param normals_nativeObj    native matrix or object handle for {@code normals}; Normal of each point, support
     *                             vector&lt;Point3f&gt; and Mat of size Nx3
     * @param curvatures_nativeObj native matrix or object handle for {@code curvatures}; Curvature of each point,
     *                             support vector&lt;float&gt; and Mat
     * @param input_pts_nativeObj  native matrix or object handle for {@code input_pts}; Original point cloud, support
     *                             vector&lt;Point3f&gt; and Mat of size Nx3/3xN
     * @param nn_idx_mat_nativeObj native matrix or object handle for {@code nn_idx_mat}; Index information of nearest
     *                             neighbors of all points. The first nearest neighbor of each point is itself. Support
     *                             vector&lt;vector&lt;int&gt;&gt;, List&lt;Mat&gt; and Mat of size NxK. If the
     *                             information in a row is [0, 2, 1, -5, -1, 4, 7 ... negative number], it will use only
     *                             non-negative indexes until it meets a negative number or bound of this row i.e. [0,
     *                             2, 1]. If left empty (noArray()), the neighbors are computed internally with a
     *                             kd-tree and max_neighbor_num is used as the number of neighbors k (must be &gt;= 2)
     */
    private static native void normalEstimate_1(
            long normals_nativeObj,
            long curvatures_nativeObj,
            long input_pts_nativeObj,
            long nn_idx_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#approxPolyDP(MatOfPoint2f,MatOfPoint2f,double,boolean)}.
     *
     * @param curve_mat_nativeObj       native matrix or object handle for {@code curve_mat}; Input vector of a 2D point
     *                                  stored in vector or Mat
     * @param approxCurve_mat_nativeObj native matrix or object handle for {@code approxCurve_mat}; Result of the
     *                                  approximation. The type should match the type of the input curve
     * @param epsilon                   Parameter specifying the approximation accuracy. This is the maximum distance
     *                                  between the original curve and its approximation
     * @param closed                    If true, the approximated curve is closed (its first and last vertices are
     *                                  connected). Otherwise, it is not closed
     */
    private static native void approxPolyDP_0(
            long curve_mat_nativeObj,
            long approxCurve_mat_nativeObj,
            double epsilon,
            boolean closed);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#approxPolyN(Mat,Mat,int,float,boolean)}.
     *
     * @param curve_nativeObj       native matrix or object handle for {@code curve}; Input vector of a 2D points stored
     *                              in vector or Mat, points must be float or integer
     * @param approxCurve_nativeObj native matrix or object handle for {@code approxCurve}; Result of the approximation.
     *                              The type is vector of a 2D point (Point2f or Point) in vector or Mat
     * @param nsides                The parameter defines the number of sides of the result polygon
     * @param epsilon_percentage    defines the percentage of the maximum of additional area. If it equals -1, it is not
     *                              used. Otherwise algorithm stops if additional area is greater than
     *                              contourArea(_curve) * percentage. If additional area exceeds the limit, algorithm
     *                              returns as many vertices as there were at the moment the limit was exceeded
     * @param ensure_convex         If it is true, algorithm creates a convex hull of input contour. Otherwise input
     *                              vector should be convex
     */
    private static native void approxPolyN_0(
            long curve_nativeObj,
            long approxCurve_nativeObj,
            int nsides,
            float epsilon_percentage,
            boolean ensure_convex);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#approxPolyN(Mat,Mat,int,float)}.
     *
     * @param curve_nativeObj       native matrix or object handle for {@code curve}; Input vector of a 2D points stored
     *                              in vector or Mat, points must be float or integer
     * @param approxCurve_nativeObj native matrix or object handle for {@code approxCurve}; Result of the approximation.
     *                              The type is vector of a 2D point (Point2f or Point) in vector or Mat
     * @param nsides                The parameter defines the number of sides of the result polygon
     * @param epsilon_percentage    defines the percentage of the maximum of additional area. If it equals -1, it is not
     *                              used. Otherwise algorithm stops if additional area is greater than
     *                              contourArea(_curve) * percentage. If additional area exceeds the limit, algorithm
     *                              returns as many vertices as there were at the moment the limit was exceeded
     */
    private static native void approxPolyN_1(
            long curve_nativeObj,
            long approxCurve_nativeObj,
            int nsides,
            float epsilon_percentage);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#approxPolyN(Mat,Mat,int)}.
     *
     * @param curve_nativeObj       native matrix or object handle for {@code curve}; Input vector of a 2D points stored
     *                              in vector or Mat, points must be float or integer
     * @param approxCurve_nativeObj native matrix or object handle for {@code approxCurve}; Result of the approximation.
     *                              The type is vector of a 2D point (Point2f or Point) in vector or Mat
     * @param nsides                The parameter defines the number of sides of the result polygon
     */
    private static native void approxPolyN_2(long curve_nativeObj, long approxCurve_nativeObj, int nsides);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#minAreaRect(MatOfPoint2f)}.
     *
     * @param points_mat_nativeObj native matrix or object handle for {@code points_mat}; Input vector of 2D points,
     *                             stored in vector&lt;&gt; or Mat
     * @return packed numeric fields used by the Java wrapper to construct {@code RotatedRect}
     */
    private static native double[] minAreaRect_0(long points_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#boxPoints(RotatedRect,Mat)}.
     *
     * @param box_center_x     {@code box.center.x} component of The input rotated rectangle. It may be the output of
     *                         REF: minAreaRect
     * @param box_center_y     {@code box.center.y} component of The input rotated rectangle. It may be the output of
     *                         REF: minAreaRect
     * @param box_size_width   {@code box.size.width} component of The input rotated rectangle. It may be the output of
     *                         REF: minAreaRect
     * @param box_size_height  {@code box.size.height} component of The input rotated rectangle. It may be the output of
     *                         REF: minAreaRect
     * @param box_angle        {@code box.angle} component of The input rotated rectangle. It may be the output of REF:
     *                         minAreaRect
     * @param points_nativeObj native matrix or object handle for {@code points}; The output array of four vertices of
     *                         rectangles
     */
    private static native void boxPoints_0(
            double box_center_x,
            double box_center_y,
            double box_size_width,
            double box_size_height,
            double box_angle,
            long points_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#minEnclosingCircle(MatOfPoint2f,Point,float[])}.
     *
     * @param points_mat_nativeObj native matrix or object handle for {@code points_mat}; Input vector of 2D points,
     *                             stored in vector&lt;&gt; or Mat
     * @param center_out           output array receiving {@code center} from the native call
     * @param radius_out           output array receiving {@code radius} from the native call
     */
    private static native void minEnclosingCircle_0(
            long points_mat_nativeObj,
            double[] center_out,
            double[] radius_out);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#minEnclosingTriangle(Mat,Mat)}.
     *
     * @param points_nativeObj   native matrix or object handle for {@code points}; Input vector of 2D points with depth
     *                           CV_32S or CV_32F, stored in vector&lt;&gt; or Mat
     * @param triangle_nativeObj native matrix or object handle for {@code triangle}; Output vector of three 2D points
     *                           defining the vertices of the triangle. The depth of the matrix must be CV_32F
     * @return the area of the enclosing triangle
     */
    private static native double minEnclosingTriangle_0(long points_nativeObj, long triangle_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#minEnclosingConvexPolygon(Mat,Mat,int)}.
     *
     * @param points_nativeObj  native matrix or object handle for {@code points}; Input vector of 2D points, stored in
     *                          vector&lt;&gt; or Mat
     * @param polygon_nativeObj native matrix or object handle for {@code polygon}; Output vector of 2D points defining
     *                          the vertices of the enclosing polygon
     * @param k                 Number of vertices of the output polygon
     * @return the area of the enclosing convex polygon
     */
    private static native double minEnclosingConvexPolygon_0(long points_nativeObj, long polygon_nativeObj, int k);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#moments(Mat,boolean)}.
     *
     * @param array_nativeObj native matrix or object handle for {@code array}; Single channel raster image (CV_8U,
     *                        CV_16U, CV_16S, CV_32F, CV_64F) or an array ( 1 times N or N times 1 ) of 2D points (Point
     *                        or Point2f)
     * @param binaryImage     If it is true, all non-zero image pixels are treated as 1's. The parameter is used for
     *                        images only
     * @return packed numeric fields used by the Java wrapper to construct {@code Moments}
     */
    private static native double[] moments_0(long array_nativeObj, boolean binaryImage);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#moments(Mat)}.
     *
     * @param array_nativeObj native matrix or object handle for {@code array}; Single channel raster image (CV_8U,
     *                        CV_16U, CV_16S, CV_32F, CV_64F) or an array ( 1 times N or N times 1 ) of 2D points (Point
     *                        or Point2f)
     * @return packed numeric fields used by the Java wrapper to construct {@code Moments}
     */
    private static native double[] moments_1(long array_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#HuMoments(Moments,Mat)}.
     *
     * @param m_m00        {@code m.m00} component of spatial, central and normalized moments from which the seven
     *                     invariants are calculated
     * @param m_m10        {@code m.m10} component of spatial, central and normalized moments from which the seven
     *                     invariants are calculated
     * @param m_m01        {@code m.m01} component of spatial, central and normalized moments from which the seven
     *                     invariants are calculated
     * @param m_m20        {@code m.m20} component of spatial, central and normalized moments from which the seven
     *                     invariants are calculated
     * @param m_m11        {@code m.m11} component of spatial, central and normalized moments from which the seven
     *                     invariants are calculated
     * @param m_m02        {@code m.m02} component of spatial, central and normalized moments from which the seven
     *                     invariants are calculated
     * @param m_m30        {@code m.m30} component of spatial, central and normalized moments from which the seven
     *                     invariants are calculated
     * @param m_m21        {@code m.m21} component of spatial, central and normalized moments from which the seven
     *                     invariants are calculated
     * @param m_m12        {@code m.m12} component of spatial, central and normalized moments from which the seven
     *                     invariants are calculated
     * @param m_m03        {@code m.m03} component of spatial, central and normalized moments from which the seven
     *                     invariants are calculated
     * @param hu_nativeObj native matrix or object handle for {@code hu}; output vector receiving the seven Hu invariant
     *                     moments
     */
    private static native void HuMoments_0(
            double m_m00,
            double m_m10,
            double m_m01,
            double m_m20,
            double m_m11,
            double m_m02,
            double m_m30,
            double m_m21,
            double m_m12,
            double m_m03,
            long hu_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#matchShapes(Mat,Mat,int,double)}.
     *
     * @param contour1_nativeObj native matrix or object handle for {@code contour1}; First contour or grayscale image
     * @param contour2_nativeObj native matrix or object handle for {@code contour2}; Second contour or grayscale image
     * @param method             Comparison method, see ShapeMatchModes
     * @param parameter          Method-specific parameter (not supported now)
     * @return the selected shape-difference measure
     */
    private static native double matchShapes_0(
            long contour1_nativeObj,
            long contour2_nativeObj,
            int method,
            double parameter);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#convexHull(MatOfPoint,MatOfInt,boolean)}.
     *
     * @param points_mat_nativeObj native matrix or object handle for {@code points_mat}; Input 2D point set, stored in
     *                             vector or Mat
     * @param hull_mat_nativeObj   native matrix or object handle for {@code hull_mat}; Output convex hull. It is either
     *                             an integer vector of indices or vector of points. In the first case, the hull
     *                             elements are 0-based indices of the convex hull points in the original array (since
     *                             the set of convex hull points is a subset of the original point set). In the second
     *                             case, hull elements are the convex hull points themselves
     * @param clockwise            Orientation flag. If it is true, the output convex hull is oriented clockwise.
     *                             Otherwise, it is oriented counter-clockwise. The assumed coordinate system has its X
     *                             axis pointing to the right, and its Y axis pointing upwards. returns convex hull
     *                             points. Otherwise, it returns indices of the convex hull points. When the output
     *                             array is vector, the flag is ignored, and the output depends on the type of the
     *                             vector: vector&lt;int&gt; implies returnPoints=false, vector&lt;Point&gt; implies
     *                             returnPoints=true. Note: points and hull should be different arrays, inplace
     *                             processing isn't supported. Check REF: tutorial_hull "the corresponding tutorial" for
     *                             more details. useful links:
     *                             https://www.learnopencv.com/convex-hull-using-opencv-in-python-and-c/
     */
    private static native void convexHull_0(long points_mat_nativeObj, long hull_mat_nativeObj, boolean clockwise);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#convexHull(MatOfPoint,MatOfInt)}.
     *
     * @param points_mat_nativeObj native matrix or object handle for {@code points_mat}; Input 2D point set, stored in
     *                             vector or Mat
     * @param hull_mat_nativeObj   native matrix or object handle for {@code hull_mat}; Output convex hull. It is either
     *                             an integer vector of indices or vector of points. In the first case, the hull
     *                             elements are 0-based indices of the convex hull points in the original array (since
     *                             the set of convex hull points is a subset of the original point set). In the second
     *                             case, hull elements are the convex hull points themselves
     */
    private static native void convexHull_2(long points_mat_nativeObj, long hull_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#convexityDefects(MatOfPoint,MatOfInt,MatOfInt4)}.
     *
     * @param contour_mat_nativeObj          native matrix or object handle for {@code contour_mat}; Input contour
     * @param convexhull_mat_nativeObj       native matrix or object handle for {@code convexhull_mat}; Convex hull
     *                                       obtained using convexHull that should contain indices of the contour points
     *                                       that make the hull
     * @param convexityDefects_mat_nativeObj native matrix or object handle for {@code convexityDefects_mat}; The output
     *                                       vector of convexity defects. In C++ and the new Python/Java interface each
     *                                       convexity defect is represented as 4-element integer vector (a.k.a.
     *                                       #4-component integer vector): (start_index, end_index, farthest_pt_index,
     *                                       fixpt_depth), where indices are 0-based indices in the original contour of
     *                                       the convexity defect beginning, end and the farthest point, and fixpt_depth
     *                                       is fixed-point approximation (with 8 fractional bits) of the distance
     *                                       between the farthest contour point and the hull. That is, to get the
     *                                       floating-point value of the depth will be fixpt_depth/256.0
     */
    private static native void convexityDefects_0(
            long contour_mat_nativeObj,
            long convexhull_mat_nativeObj,
            long convexityDefects_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#isContourConvex(MatOfPoint)}.
     *
     * @param contour_mat_nativeObj native matrix or object handle for {@code contour_mat}; Input vector of 2D points,
     *                              stored in vector&lt;&gt; or Mat
     * @return true if the contour is convex
     */
    private static native boolean isContourConvex_0(long contour_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#intersectConvexConvex(Mat,Mat,Mat,boolean)}.
     *
     * @param p1_nativeObj  native matrix or object handle for {@code p1}; First polygon
     * @param p2_nativeObj  native matrix or object handle for {@code p2}; Second polygon
     * @param p12_nativeObj native matrix or object handle for {@code p12}; Output polygon describing the intersecting
     *                      area
     * @param handleNested  When true, an intersection is found if one of the polygons is fully enclosed in the other.
     *                      When false, no intersection is found. If the polygons share a side or the vertex of one
     *                      polygon lies on an edge of the other, they are not considered nested and an intersection
     *                      will be found regardless of the value of handleNested
     * @return Area of intersecting polygon. May be negative, if algorithm has not converged, e.g. non-convex input.
     *         Note: intersectConvexConvex doesn't confirm that both polygons are convex and will return invalid results
     *         if they aren't
     */
    private static native float intersectConvexConvex_0(
            long p1_nativeObj,
            long p2_nativeObj,
            long p12_nativeObj,
            boolean handleNested);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#intersectConvexConvex(Mat,Mat,Mat)}.
     *
     * @param p1_nativeObj  native matrix or object handle for {@code p1}; First polygon
     * @param p2_nativeObj  native matrix or object handle for {@code p2}; Second polygon
     * @param p12_nativeObj native matrix or object handle for {@code p12}; Output polygon describing the intersecting
     *                      area
     * @return Area of intersecting polygon. May be negative, if algorithm has not converged, e.g. non-convex input.
     *         Note: intersectConvexConvex doesn't confirm that both polygons are convex and will return invalid results
     *         if they aren't
     */
    private static native float intersectConvexConvex_1(long p1_nativeObj, long p2_nativeObj, long p12_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#fitEllipse(MatOfPoint2f)}.
     *
     * @param points_mat_nativeObj native matrix or object handle for {@code points_mat}; Input 2D point set, stored in
     *                             vector&lt;&gt; or Mat Note: Input point types are REF: Point2i or REF: Point2f and at
     *                             least 5 points are required. Note: REF: getClosestEllipsePoints function can be used
     *                             to compute the ellipse fitting error
     * @return packed numeric fields used by the Java wrapper to construct {@code RotatedRect}
     */
    private static native double[] fitEllipse_0(long points_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#fitEllipseAMS(Mat)}.
     *
     * @param points_nativeObj native matrix or object handle for {@code points}; Input 2D point set, stored in
     *                         vector&lt;&gt; or Mat Note: Input point types are REF: Point2i or REF: Point2f and at
     *                         least 5 points are required. Note: REF: getClosestEllipsePoints function can be used to
     *                         compute the ellipse fitting error
     * @return packed numeric fields used by the Java wrapper to construct {@code RotatedRect}
     */
    private static native double[] fitEllipseAMS_0(long points_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#fitEllipseDirect(Mat)}.
     *
     * @param points_nativeObj native matrix or object handle for {@code points}; Input 2D point set, stored in
     *                         vector&lt;&gt; or Mat Note: Input point types are REF: Point2i or REF: Point2f and at
     *                         least 5 points are required. Note: REF: getClosestEllipsePoints function can be used to
     *                         compute the ellipse fitting error
     * @return packed numeric fields used by the Java wrapper to construct {@code RotatedRect}
     */
    private static native double[] fitEllipseDirect_0(long points_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#getClosestEllipsePoints(RotatedRect,Mat,Mat)}.
     *
     * @param ellipse_params_center_x    {@code ellipse_params.center.x} component of Ellipse parameters
     * @param ellipse_params_center_y    {@code ellipse_params.center.y} component of Ellipse parameters
     * @param ellipse_params_size_width  {@code ellipse_params.size.width} component of Ellipse parameters
     * @param ellipse_params_size_height {@code ellipse_params.size.height} component of Ellipse parameters
     * @param ellipse_params_angle       {@code ellipse_params.angle} component of Ellipse parameters
     * @param points_nativeObj           native matrix or object handle for {@code points}; Input 2d points
     * @param closest_pts_nativeObj      native matrix or object handle for {@code closest_pts}; For each 2d point,
     *                                   their corresponding closest 2d point located on a given ellipse Note: Input
     *                                   point types are REF: Point2i or REF: Point2f
     */
    private static native void getClosestEllipsePoints_0(
            double ellipse_params_center_x,
            double ellipse_params_center_y,
            double ellipse_params_size_width,
            double ellipse_params_size_height,
            double ellipse_params_angle,
            long points_nativeObj,
            long closest_pts_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fitLine(Mat,Mat,int,double,double,double)}.
     *
     * @param points_nativeObj native matrix or object handle for {@code points}; Input vector of 2D or 3D points,
     *                         stored in vector&lt;&gt; or Mat
     * @param line_nativeObj   native matrix or object handle for {@code line}; Output line parameters. In case of 2D
     *                         fitting, it should be a vector of 4 elements (like 4-component float vector) - (vx, vy,
     *                         x0, y0), where (vx, vy) is a normalized vector collinear to the line and (x0, y0) is a
     *                         point on the line. In case of 3D fitting, it should be a vector of 6 elements (like
     *                         6-component float vector) - (vx, vy, vz, x0, y0, z0), where (vx, vy, vz) is a normalized
     *                         vector collinear to the line and (x0, y0, z0) is a point on the line
     * @param distType         Distance used by the M-estimator, see DistanceTypes
     * @param param            Numerical parameter ( C ) for some types of distances. If it is 0, an optimal value is
     *                         chosen
     * @param reps             Sufficient accuracy for the radius (distance between the coordinate origin and the line)
     * @param aeps             Sufficient accuracy for the angle. 0.01 would be a good default value for reps and aeps
     */
    private static native void fitLine_0(
            long points_nativeObj,
            long line_nativeObj,
            int distType,
            double param,
            double reps,
            double aeps);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#pointPolygonTest(MatOfPoint2f,Point,boolean)}.
     *
     * @param contour_mat_nativeObj native matrix or object handle for {@code contour_mat}; Input contour
     * @param pt_x                  {@code pt.x} component of Point tested against the contour
     * @param pt_y                  {@code pt.y} component of Point tested against the contour
     * @param measureDist           If true, the function estimates the signed distance from the point to the nearest
     *                              contour edge. Otherwise, the function only checks if the point is inside a contour
     *                              or not
     * @return the inside/outside sign or signed boundary distance selected by measureDist
     */
    private static native double pointPolygonTest_0(
            long contour_mat_nativeObj,
            double pt_x,
            double pt_y,
            boolean measureDist);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#rotatedRectangleIntersection(RotatedRect,RotatedRect,Mat)}.
     *
     * @param rect1_center_x               {@code rect1.center.x} component of First rectangle
     * @param rect1_center_y               {@code rect1.center.y} component of First rectangle
     * @param rect1_size_width             {@code rect1.size.width} component of First rectangle
     * @param rect1_size_height            {@code rect1.size.height} component of First rectangle
     * @param rect1_angle                  {@code rect1.angle} component of First rectangle
     * @param rect2_center_x               {@code rect2.center.x} component of Second rectangle
     * @param rect2_center_y               {@code rect2.center.y} component of Second rectangle
     * @param rect2_size_width             {@code rect2.size.width} component of Second rectangle
     * @param rect2_size_height            {@code rect2.size.height} component of Second rectangle
     * @param rect2_angle                  {@code rect2.angle} component of Second rectangle
     * @param intersectingRegion_nativeObj native matrix or object handle for {@code intersectingRegion}; The output
     *                                     array of the vertices of the intersecting region. It returns at most 8
     *                                     vertices. Stored as vector&lt;Point2f&gt; or Mat as Mx1 of type CV_32FC2
     * @return One of RectanglesIntersectTypes
     */
    private static native int rotatedRectangleIntersection_0(
            double rect1_center_x,
            double rect1_center_y,
            double rect1_size_width,
            double rect1_size_height,
            double rect1_angle,
            double rect2_center_x,
            double rect2_center_y,
            double rect2_size_width,
            double rect2_size_height,
            double rect2_angle,
            long intersectingRegion_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#arcLength(MatOfPoint2f,boolean)}.
     *
     * @param curve_mat_nativeObj native matrix or object handle for {@code curve_mat}; Input vector of 2D points,
     *                            stored in vector or Mat
     * @param closed              Flag indicating whether the curve is closed or not
     * @return the contour perimeter or open-curve length
     */
    private static native double arcLength_0(long curve_mat_nativeObj, boolean closed);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#contourArea(Mat,boolean)}.
     *
     * @param contour_nativeObj native matrix or object handle for {@code contour}; Input vector of 2D points (contour
     *                          vertices), stored in vector or Mat
     * @param oriented          Oriented area flag. If it is true, the function returns a signed area value, depending
     *                          on the contour orientation (clockwise or counter-clockwise). Using this feature you can
     *                          determine orientation of a contour by taking the sign of an area. By default, the
     *                          parameter is false, which means that the absolute value is returned
     * @return the signed or unsigned contour area selected by oriented
     */
    private static native double contourArea_0(long contour_nativeObj, boolean oriented);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#contourArea(Mat)}.
     *
     * @param contour_nativeObj native matrix or object handle for {@code contour}; Input vector of 2D points (contour
     *                          vertices), stored in vector or Mat
     * @return the signed or unsigned contour area selected by oriented
     */
    private static native double contourArea_1(long contour_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#boundingRect(Mat)}.
     *
     * @param array_nativeObj native matrix or object handle for {@code array}; Input gray-scale image or 2D point set,
     *                        stored in vector or Mat. Note: Point coordinates that the resulting Rect cannot represent
     *                        - outside of the int range, or infinite - are saturated to INT_MIN / INT_MAX, and its
     *                        width and height are clamped to INT_MAX
     * @return packed numeric fields used by the Java wrapper to construct {@code Rect}
     */
    private static native double[] boundingRect_0(long array_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#getRotationMatrix2D(Point,double,double)}.
     *
     * @param center_x {@code center.x} component of Center of the rotation in the source image
     * @param center_y {@code center.y} component of Center of the rotation in the source image
     * @param angle    Rotation angle in degrees. Positive values mean counter-clockwise rotation (the coordinate origin
     *                 is assumed to be the top-left corner)
     * @param scale    Isotropic scale factor
     * @return native binding handle for the 2-by-3 affine rotation-and-scale matrix about the specified center
     */
    private static native long getRotationMatrix2D_0(double center_x, double center_y, double angle, double scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#invertAffineTransform(Mat,Mat)}.
     *
     * @param M_nativeObj  native matrix or object handle for {@code M}; Original affine transformation
     * @param iM_nativeObj native matrix or object handle for {@code iM}; Output reverse affine transformation
     */
    private static native void invertAffineTransform_0(long M_nativeObj, long iM_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#getPerspectiveTransform(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Coordinates of quadrangle vertices in the
     *                      source image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Coordinates of the corresponding quadrangle
     *                      vertices in the destination image
     * @param solveMethod   method passed to solve (DecompTypes)
     * @return native binding handle for the 3-by-3 projective transform mapping the four source points to their
     *         corresponding destinations
     */
    private static native long getPerspectiveTransform_0(long src_nativeObj, long dst_nativeObj, int solveMethod);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#getPerspectiveTransform(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Coordinates of quadrangle vertices in the
     *                      source image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Coordinates of the corresponding quadrangle
     *                      vertices in the destination image
     * @return native binding handle for the 3-by-3 projective transform mapping the four source points to their
     *         corresponding destinations
     */
    private static native long getPerspectiveTransform_1(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#getAffineTransform(MatOfPoint2f,MatOfPoint2f)}.
     *
     * @param src_mat_nativeObj native matrix or object handle for {@code src_mat}; source matrix
     * @param dst_mat_nativeObj native matrix or object handle for {@code dst_mat}; destination matrix that receives the
     *                          result
     * @return native binding handle for the 2-by-3 affine transform mapping the three source points to their
     *         corresponding destinations
     */
    private static native long getAffineTransform_0(long src_mat_nativeObj, long dst_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#Rodrigues(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj      native matrix or object handle for {@code src}; Input rotation vector (3x1 or 1x3) or
     *                           rotation matrix (3x3)
     * @param dst_nativeObj      native matrix or object handle for {@code dst}; Output rotation matrix (3x3) or
     *                           rotation vector (3x1 or 1x3), respectively
     * @param jacobian_nativeObj native matrix or object handle for {@code jacobian}; Optional output Jacobian matrix,
     *                           3x9 or 9x3, which is a matrix of partial derivatives of the output array components
     *                           with respect to the input array components. {l} theta from norm(r) \\ r from r/ theta
     *                           \\ R = cos(theta) I + (1- cos{theta} ) r r^T + sin(theta)
     *                           vecthreethree{0}{-r_z}{r_y}{r_z}{0}{-r_x}{-r_y}{r_x}{0} Inverse transformation can be
     *                           also done easily, since sin ( theta )
     *                           vecthreethree{0}{-r_z}{r_y}{r_z}{0}{-r_x}{-r_y}{r_x}{0} = (R - R^T)/(2) A rotation
     *                           vector is a convenient and most compact representation of a rotation matrix (since any
     *                           rotation matrix has just 3 degrees of freedom). The representation is used in the
     *                           global 3D geometry optimization procedures like REF: calibrateCamera, REF:
     *                           stereoCalibrate, or REF: solvePnP . Note: More information about the computation of the
     *                           derivative of a 3D rotation matrix with respect to its exponential coordinate can be
     *                           found in: A Compact Formula for the Derivative of a 3-D Rotation in Exponential
     *                           Coordinates, Guillermo Gallego, Anthony J. Yezzi Note: Useful information on SE(3) and
     *                           Lie Groups can be found in: A tutorial on SE(3) transformation parameterizations and
     *                           on-manifold optimization, Jose-Luis Blanco Lie Groups for 2D and 3D Transformation,
     *                           Ethan Eade A micro Lie theory for state estimation in robotics, Joan Solà, Jérémie
     *                           Deray, Dinesh Atchuthan
     */
    private static native void Rodrigues_0(long src_nativeObj, long dst_nativeObj, long jacobian_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#Rodrigues(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Input rotation vector (3x1 or 1x3) or
     *                      rotation matrix (3x3)
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Output rotation matrix (3x3) or rotation
     *                      vector (3x1 or 1x3), respectively
     */
    private static native void Rodrigues_1(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findHomography(MatOfPoint2f,MatOfPoint2f,int,double,Mat,int,double)}.
     *
     * @param srcPoints_mat_nativeObj native matrix or object handle for {@code srcPoints_mat}; Coordinates of the
     *                                points in the original plane, a matrix of the type CV_32FC2 or
     *                                vector&lt;Point2f&gt;
     * @param dstPoints_mat_nativeObj native matrix or object handle for {@code dstPoints_mat}; Coordinates of the
     *                                points in the target plane, a matrix of the type CV_32FC2 or a
     *                                vector&lt;Point2f&gt;
     * @param method                  Method used to compute a homography matrix. The following methods are possible: 0
     *                                - a regular method using all the points, i.e., the least squares method REF:
     *                                RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method REF:
     *                                RHO - PROSAC-based robust method
     * @param ransacReprojThreshold   Maximum allowed reprojection error to treat a point pair as an inlier (used in the
     *                                RANSAC and RHO methods only). That is, if \| {@code dstPoints} _i - {@code
     *     convertPointsHomogeneous} ( {@code H} cdot {@code srcPoints} _i) \|_2 &gt; {@code
     *     ransacReprojThreshold}  then the point i is considered as an outlier. If srcPoints and dstPoints are
     *                                measured in pixels, it usually makes sense to set this parameter somewhere in the
     *                                range of 1 to 10
     * @param mask_nativeObj          native matrix or object handle for {@code mask}; Optional output mask set by a
     *                                robust method ( RANSAC or LMeDS ). Note that the input mask values are ignored
     * @param maxIters                The maximum number of RANSAC iterations
     * @param confidence              Confidence level, between 0 and 1. The function finds and returns the perspective
     *                                transformation H between the source and the destination planes: s_i
     *                                vecthree{x'_i}{y'_i}{1} sim H vecthree{x_i}{y_i}{1} so that the back-projection
     *                                error sum _i left ( x'_i- frac{h_{11} x_i + h_{12} y_i + h_{13}}{h_{31} x_i +
     *                                h_{32} y_i + h_{33}} right )^2+ left ( y'_i- frac{h_{21} x_i + h_{22} y_i +
     *                                h_{23}}{h_{31} x_i + h_{32} y_i + h_{33}} right )^2 is minimized. If the parameter
     *                                method is set to the default value 0, the function uses all the point pairs to
     *                                compute an initial homography estimate with a simple least-squares scheme.
     *                                However, if not all of the point pairs ( srcPoints_i, dstPoints_i ) fit the rigid
     *                                perspective transformation (that is, there are some outliers), this initial
     *                                estimate will be poor. In this case, you can use one of the three robust methods.
     *                                The methods RANSAC, LMeDS and RHO try many different random subsets of the
     *                                corresponding point pairs (of four pairs each, collinear pairs are discarded),
     *                                estimate the homography matrix using this subset and a simple least-squares
     *                                algorithm, and then compute the quality/goodness of the computed homography (which
     *                                is the number of inliers for RANSAC or the least median re-projection error for
     *                                LMeDS). The best subset is then used to produce the initial estimate of the
     *                                homography matrix and the mask of inliers/outliers. Regardless of the method,
     *                                robust or not, the computed homography matrix is refined further (using inliers
     *                                only in case of a robust method) with the Levenberg-Marquardt method to reduce the
     *                                re-projection error even more. The methods RANSAC and RHO can handle practically
     *                                any ratio of outliers but need a threshold to distinguish inliers from outliers.
     *                                The method LMeDS does not need any threshold but it works correctly only when
     *                                there are more than 50% of inliers. Finally, if there are no outliers and the
     *                                noise is rather small, use the default method (method=0). The function is used to
     *                                find initial intrinsic and extrinsic matrices. Homography matrix is determined up
     *                                to a scale. If h_{33} is non-zero, the matrix is normalized so that h_{33}=1.
     *                                Note: Whenever an H matrix cannot be estimated, an empty one will be returned
     * @return native binding handle for the estimated homography, or an empty matrix when estimation fails
     */
    private static native long findHomography_0(
            long srcPoints_mat_nativeObj,
            long dstPoints_mat_nativeObj,
            int method,
            double ransacReprojThreshold,
            long mask_nativeObj,
            int maxIters,
            double confidence);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findHomography(MatOfPoint2f,MatOfPoint2f,int,double,Mat,int)}.
     *
     * @param srcPoints_mat_nativeObj native matrix or object handle for {@code srcPoints_mat}; Coordinates of the
     *                                points in the original plane, a matrix of the type CV_32FC2 or
     *                                vector&lt;Point2f&gt;
     * @param dstPoints_mat_nativeObj native matrix or object handle for {@code dstPoints_mat}; Coordinates of the
     *                                points in the target plane, a matrix of the type CV_32FC2 or a
     *                                vector&lt;Point2f&gt;
     * @param method                  Method used to compute a homography matrix. The following methods are possible: 0
     *                                - a regular method using all the points, i.e., the least squares method REF:
     *                                RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method REF:
     *                                RHO - PROSAC-based robust method
     * @param ransacReprojThreshold   Maximum allowed reprojection error to treat a point pair as an inlier (used in the
     *                                RANSAC and RHO methods only). That is, if \| {@code dstPoints} _i - {@code
     *     convertPointsHomogeneous} ( {@code H} cdot {@code srcPoints} _i) \|_2 &gt; {@code
     *     ransacReprojThreshold}  then the point i is considered as an outlier. If srcPoints and dstPoints are
     *                                measured in pixels, it usually makes sense to set this parameter somewhere in the
     *                                range of 1 to 10
     * @param mask_nativeObj          native matrix or object handle for {@code mask}; Optional output mask set by a
     *                                robust method ( RANSAC or LMeDS ). Note that the input mask values are ignored
     * @param maxIters                The maximum number of RANSAC iterations
     * @return native binding handle for the estimated homography, or an empty matrix when estimation fails
     */
    private static native long findHomography_1(
            long srcPoints_mat_nativeObj,
            long dstPoints_mat_nativeObj,
            int method,
            double ransacReprojThreshold,
            long mask_nativeObj,
            int maxIters);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findHomography(MatOfPoint2f,MatOfPoint2f,int,double,Mat)}.
     *
     * @param srcPoints_mat_nativeObj native matrix or object handle for {@code srcPoints_mat}; Coordinates of the
     *                                points in the original plane, a matrix of the type CV_32FC2 or
     *                                vector&lt;Point2f&gt;
     * @param dstPoints_mat_nativeObj native matrix or object handle for {@code dstPoints_mat}; Coordinates of the
     *                                points in the target plane, a matrix of the type CV_32FC2 or a
     *                                vector&lt;Point2f&gt;
     * @param method                  Method used to compute a homography matrix. The following methods are possible: 0
     *                                - a regular method using all the points, i.e., the least squares method REF:
     *                                RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method REF:
     *                                RHO - PROSAC-based robust method
     * @param ransacReprojThreshold   Maximum allowed reprojection error to treat a point pair as an inlier (used in the
     *                                RANSAC and RHO methods only). That is, if \| {@code dstPoints} _i - {@code
     *     convertPointsHomogeneous} ( {@code H} cdot {@code srcPoints} _i) \|_2 &gt; {@code
     *     ransacReprojThreshold}  then the point i is considered as an outlier. If srcPoints and dstPoints are
     *                                measured in pixels, it usually makes sense to set this parameter somewhere in the
     *                                range of 1 to 10
     * @param mask_nativeObj          native matrix or object handle for {@code mask}; Optional output mask set by a
     *                                robust method ( RANSAC or LMeDS ). Note that the input mask values are ignored
     * @return native binding handle for the estimated homography, or an empty matrix when estimation fails
     */
    private static native long findHomography_2(
            long srcPoints_mat_nativeObj,
            long dstPoints_mat_nativeObj,
            int method,
            double ransacReprojThreshold,
            long mask_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findHomography(MatOfPoint2f,MatOfPoint2f,int,double)}.
     *
     * @param srcPoints_mat_nativeObj native matrix or object handle for {@code srcPoints_mat}; Coordinates of the
     *                                points in the original plane, a matrix of the type CV_32FC2 or
     *                                vector&lt;Point2f&gt;
     * @param dstPoints_mat_nativeObj native matrix or object handle for {@code dstPoints_mat}; Coordinates of the
     *                                points in the target plane, a matrix of the type CV_32FC2 or a
     *                                vector&lt;Point2f&gt;
     * @param method                  Method used to compute a homography matrix. The following methods are possible: 0
     *                                - a regular method using all the points, i.e., the least squares method REF:
     *                                RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method REF:
     *                                RHO - PROSAC-based robust method
     * @param ransacReprojThreshold   Maximum allowed reprojection error to treat a point pair as an inlier (used in the
     *                                RANSAC and RHO methods only). That is, if \| {@code dstPoints} _i - {@code
     *     convertPointsHomogeneous} ( {@code H} cdot {@code srcPoints} _i) \|_2 &gt; {@code
     *     ransacReprojThreshold}  then the point i is considered as an outlier. If srcPoints and dstPoints are
     *                                measured in pixels, it usually makes sense to set this parameter somewhere in the
     *                                range of 1 to 10
     * @return native binding handle for the estimated homography, or an empty matrix when estimation fails
     */
    private static native long findHomography_3(
            long srcPoints_mat_nativeObj,
            long dstPoints_mat_nativeObj,
            int method,
            double ransacReprojThreshold);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findHomography(MatOfPoint2f,MatOfPoint2f,int)}.
     *
     * @param srcPoints_mat_nativeObj native matrix or object handle for {@code srcPoints_mat}; Coordinates of the
     *                                points in the original plane, a matrix of the type CV_32FC2 or
     *                                vector&lt;Point2f&gt;
     * @param dstPoints_mat_nativeObj native matrix or object handle for {@code dstPoints_mat}; Coordinates of the
     *                                points in the target plane, a matrix of the type CV_32FC2 or a
     *                                vector&lt;Point2f&gt;
     * @param method                  Method used to compute a homography matrix. The following methods are possible: 0
     *                                - a regular method using all the points, i.e., the least squares method REF:
     *                                RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method REF:
     *                                RHO - PROSAC-based robust method
     * @return native binding handle for the estimated homography, or an empty matrix when estimation fails
     */
    private static native long findHomography_4(long srcPoints_mat_nativeObj, long dstPoints_mat_nativeObj, int method);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findHomography(MatOfPoint2f,MatOfPoint2f)}.
     *
     * @param srcPoints_mat_nativeObj native matrix or object handle for {@code srcPoints_mat}; Coordinates of the
     *                                points in the original plane, a matrix of the type CV_32FC2 or
     *                                vector&lt;Point2f&gt;
     * @param dstPoints_mat_nativeObj native matrix or object handle for {@code dstPoints_mat}; Coordinates of the
     *                                points in the target plane, a matrix of the type CV_32FC2 or a
     *                                vector&lt;Point2f&gt;
     * @return native binding handle for the estimated homography, or an empty matrix when estimation fails
     */
    private static native long findHomography_5(long srcPoints_mat_nativeObj, long dstPoints_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findHomography(MatOfPoint2f,MatOfPoint2f,Mat,UsacParams)}.
     *
     * @param srcPoints_mat_nativeObj native matrix or object handle for {@code srcPoints_mat}; Coordinates of the
     *                                points in the original plane, a matrix of the type CV_32FC2 or
     *                                vector&lt;Point2f&gt;
     * @param dstPoints_mat_nativeObj native matrix or object handle for {@code dstPoints_mat}; Coordinates of the
     *                                points in the target plane, a matrix of the type CV_32FC2 or a
     *                                vector&lt;Point2f&gt;
     * @param mask_nativeObj          native matrix or object handle for {@code mask}; Optional output mask set by a
     *                                robust method ( RANSAC or LMeDS ). Note that the input mask values are ignored
     * @param params_nativeObj        {@code params.getNativeObjAddr()} component of robust-estimation sampling,
     *                                scoring, threshold and refinement settings
     * @return native binding handle for the estimated homography, or an empty matrix when estimation fails
     */
    private static native long findHomography_6(
            long srcPoints_mat_nativeObj,
            long dstPoints_mat_nativeObj,
            long mask_nativeObj,
            long params_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#RQDecomp3x3(Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; 3x3 input matrix
     * @param mtxR_nativeObj native matrix or object handle for {@code mtxR}; Output 3x3 upper-triangular matrix
     * @param mtxQ_nativeObj native matrix or object handle for {@code mtxQ}; Output 3x3 orthogonal matrix
     * @param Qx_nativeObj   native matrix or object handle for {@code Qx}; Optional output 3x3 rotation matrix around
     *                       x-axis
     * @param Qy_nativeObj   native matrix or object handle for {@code Qy}; Optional output 3x3 rotation matrix around
     *                       y-axis
     * @param Qz_nativeObj   native matrix or object handle for {@code Qz}; Optional output 3x3 rotation matrix around
     *                       z-axis. The function computes a RQ decomposition using the given rotations. This function
     *                       is used in decomposeProjectionMatrix to decompose the left 3x3 submatrix of a projection
     *                       matrix into a camera and a rotation matrix. It optionally returns three rotation matrices,
     *                       one for each axis, and the three Euler angles in degrees (as the return value) that could
     *                       be used in OpenGL. Note, there is always more than one sequence of rotations about the
     *                       three principal axes that results in the same orientation of an object, e.g. see Returned
     *                       three rotation matrices and corresponding three Euler angles are only one of the possible
     *                       solutions
     * @return three Euler angles, in degrees, corresponding to the decomposed rotation
     */
    private static native double[] RQDecomp3x3_0(
            long src_nativeObj,
            long mtxR_nativeObj,
            long mtxQ_nativeObj,
            long Qx_nativeObj,
            long Qy_nativeObj,
            long Qz_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#RQDecomp3x3(Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; 3x3 input matrix
     * @param mtxR_nativeObj native matrix or object handle for {@code mtxR}; Output 3x3 upper-triangular matrix
     * @param mtxQ_nativeObj native matrix or object handle for {@code mtxQ}; Output 3x3 orthogonal matrix
     * @param Qx_nativeObj   native matrix or object handle for {@code Qx}; Optional output 3x3 rotation matrix around
     *                       x-axis
     * @param Qy_nativeObj   native matrix or object handle for {@code Qy}; Optional output 3x3 rotation matrix around
     *                       y-axis
     * @return three Euler angles, in degrees, corresponding to the decomposed rotation
     */
    private static native double[] RQDecomp3x3_1(
            long src_nativeObj,
            long mtxR_nativeObj,
            long mtxQ_nativeObj,
            long Qx_nativeObj,
            long Qy_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#RQDecomp3x3(Mat,Mat,Mat,Mat)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; 3x3 input matrix
     * @param mtxR_nativeObj native matrix or object handle for {@code mtxR}; Output 3x3 upper-triangular matrix
     * @param mtxQ_nativeObj native matrix or object handle for {@code mtxQ}; Output 3x3 orthogonal matrix
     * @param Qx_nativeObj   native matrix or object handle for {@code Qx}; Optional output 3x3 rotation matrix around
     *                       x-axis
     * @return three Euler angles, in degrees, corresponding to the decomposed rotation
     */
    private static native double[] RQDecomp3x3_2(
            long src_nativeObj,
            long mtxR_nativeObj,
            long mtxQ_nativeObj,
            long Qx_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#RQDecomp3x3(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; 3x3 input matrix
     * @param mtxR_nativeObj native matrix or object handle for {@code mtxR}; Output 3x3 upper-triangular matrix
     * @param mtxQ_nativeObj native matrix or object handle for {@code mtxQ}; Output 3x3 orthogonal matrix
     * @return three Euler angles, in degrees, corresponding to the decomposed rotation
     */
    private static native double[] RQDecomp3x3_3(long src_nativeObj, long mtxR_nativeObj, long mtxQ_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#decomposeProjectionMatrix(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param projMatrix_nativeObj   native matrix or object handle for {@code projMatrix}; 3x4 input projection matrix
     *                               P
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Output 3x3 camera
     *                               intrinsic matrix cameramatrix{A}
     * @param rotMatrix_nativeObj    native matrix or object handle for {@code rotMatrix}; Output 3x3 external rotation
     *                               matrix R
     * @param transVect_nativeObj    native matrix or object handle for {@code transVect}; Output 4x1 vector
     *                               representing the camera position in homogeneous coordinates. To obtain the
     *                               translation vector, use t = -rotMatrix * transVect[:3]
     * @param rotMatrixX_nativeObj   native matrix or object handle for {@code rotMatrixX}; Optional 3x3 rotation matrix
     *                               around x-axis
     * @param rotMatrixY_nativeObj   native matrix or object handle for {@code rotMatrixY}; Optional 3x3 rotation matrix
     *                               around y-axis
     * @param rotMatrixZ_nativeObj   native matrix or object handle for {@code rotMatrixZ}; Optional 3x3 rotation matrix
     *                               around z-axis
     * @param eulerAngles_nativeObj  native matrix or object handle for {@code eulerAngles}; Optional three-element
     *                               vector containing three Euler angles of rotation in degrees. The function computes
     *                               a decomposition of a projection matrix into a calibration and a rotation matrix and
     *                               the position of a camera. It optionally returns three rotation matrices, one for
     *                               each axis, and three Euler angles that could be used in OpenGL. Note, there is
     *                               always more than one sequence of rotations about the three principal axes that
     *                               results in the same orientation of an object, e.g. see Returned three rotation
     *                               matrices and corresponding three Euler angles are only one of the possible
     *                               solutions. The function is based on RQDecomp3x3
     */
    private static native void decomposeProjectionMatrix_0(
            long projMatrix_nativeObj,
            long cameraMatrix_nativeObj,
            long rotMatrix_nativeObj,
            long transVect_nativeObj,
            long rotMatrixX_nativeObj,
            long rotMatrixY_nativeObj,
            long rotMatrixZ_nativeObj,
            long eulerAngles_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#decomposeProjectionMatrix(Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param projMatrix_nativeObj   native matrix or object handle for {@code projMatrix}; 3x4 input projection matrix
     *                               P
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Output 3x3 camera
     *                               intrinsic matrix cameramatrix{A}
     * @param rotMatrix_nativeObj    native matrix or object handle for {@code rotMatrix}; Output 3x3 external rotation
     *                               matrix R
     * @param transVect_nativeObj    native matrix or object handle for {@code transVect}; Output 4x1 vector
     *                               representing the camera position in homogeneous coordinates. To obtain the
     *                               translation vector, use t = -rotMatrix * transVect[:3]
     * @param rotMatrixX_nativeObj   native matrix or object handle for {@code rotMatrixX}; Optional 3x3 rotation matrix
     *                               around x-axis
     * @param rotMatrixY_nativeObj   native matrix or object handle for {@code rotMatrixY}; Optional 3x3 rotation matrix
     *                               around y-axis
     * @param rotMatrixZ_nativeObj   native matrix or object handle for {@code rotMatrixZ}; Optional 3x3 rotation matrix
     *                               around z-axis
     */
    private static native void decomposeProjectionMatrix_1(
            long projMatrix_nativeObj,
            long cameraMatrix_nativeObj,
            long rotMatrix_nativeObj,
            long transVect_nativeObj,
            long rotMatrixX_nativeObj,
            long rotMatrixY_nativeObj,
            long rotMatrixZ_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#decomposeProjectionMatrix(Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param projMatrix_nativeObj   native matrix or object handle for {@code projMatrix}; 3x4 input projection matrix
     *                               P
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Output 3x3 camera
     *                               intrinsic matrix cameramatrix{A}
     * @param rotMatrix_nativeObj    native matrix or object handle for {@code rotMatrix}; Output 3x3 external rotation
     *                               matrix R
     * @param transVect_nativeObj    native matrix or object handle for {@code transVect}; Output 4x1 vector
     *                               representing the camera position in homogeneous coordinates. To obtain the
     *                               translation vector, use t = -rotMatrix * transVect[:3]
     * @param rotMatrixX_nativeObj   native matrix or object handle for {@code rotMatrixX}; Optional 3x3 rotation matrix
     *                               around x-axis
     * @param rotMatrixY_nativeObj   native matrix or object handle for {@code rotMatrixY}; Optional 3x3 rotation matrix
     *                               around y-axis
     */
    private static native void decomposeProjectionMatrix_2(
            long projMatrix_nativeObj,
            long cameraMatrix_nativeObj,
            long rotMatrix_nativeObj,
            long transVect_nativeObj,
            long rotMatrixX_nativeObj,
            long rotMatrixY_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#decomposeProjectionMatrix(Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param projMatrix_nativeObj   native matrix or object handle for {@code projMatrix}; 3x4 input projection matrix
     *                               P
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Output 3x3 camera
     *                               intrinsic matrix cameramatrix{A}
     * @param rotMatrix_nativeObj    native matrix or object handle for {@code rotMatrix}; Output 3x3 external rotation
     *                               matrix R
     * @param transVect_nativeObj    native matrix or object handle for {@code transVect}; Output 4x1 vector
     *                               representing the camera position in homogeneous coordinates. To obtain the
     *                               translation vector, use t = -rotMatrix * transVect[:3]
     * @param rotMatrixX_nativeObj   native matrix or object handle for {@code rotMatrixX}; Optional 3x3 rotation matrix
     *                               around x-axis
     */
    private static native void decomposeProjectionMatrix_3(
            long projMatrix_nativeObj,
            long cameraMatrix_nativeObj,
            long rotMatrix_nativeObj,
            long transVect_nativeObj,
            long rotMatrixX_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#decomposeProjectionMatrix(Mat,Mat,Mat,Mat)}.
     *
     * @param projMatrix_nativeObj   native matrix or object handle for {@code projMatrix}; 3x4 input projection matrix
     *                               P
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Output 3x3 camera
     *                               intrinsic matrix cameramatrix{A}
     * @param rotMatrix_nativeObj    native matrix or object handle for {@code rotMatrix}; Output 3x3 external rotation
     *                               matrix R
     * @param transVect_nativeObj    native matrix or object handle for {@code transVect}; Output 4x1 vector
     *                               representing the camera position in homogeneous coordinates. To obtain the
     *                               translation vector, use t = -rotMatrix * transVect[:3]
     */
    private static native void decomposeProjectionMatrix_4(
            long projMatrix_nativeObj,
            long cameraMatrix_nativeObj,
            long rotMatrix_nativeObj,
            long transVect_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#matMulDeriv(Mat,Mat,Mat,Mat)}.
     *
     * @param A_nativeObj     native matrix or object handle for {@code A}; First multiplied matrix
     * @param B_nativeObj     native matrix or object handle for {@code B}; Second multiplied matrix
     * @param dABdA_nativeObj native matrix or object handle for {@code dABdA}; First output derivative matrix d(A*B)/dA
     *                        of size {@code A.rows*B.cols} times {A.rows*A.cols}
     * @param dABdB_nativeObj native matrix or object handle for {@code dABdB}; Second output derivative matrix
     *                        d(A*B)/dB of size {@code A.rows*B.cols} times {B.rows*B.cols} . The function computes
     *                        partial derivatives of the elements of the matrix product A*B with regard to the elements
     *                        of each of the two input matrices. The function is used to compute the Jacobian matrices
     *                        in stereoCalibrate but can also be used in any other similar optimization function
     */
    private static native void matMulDeriv_0(
            long A_nativeObj,
            long B_nativeObj,
            long dABdA_nativeObj,
            long dABdB_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#composeRT(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param rvec1_nativeObj  native matrix or object handle for {@code rvec1}; First rotation vector
     * @param tvec1_nativeObj  native matrix or object handle for {@code tvec1}; First translation vector
     * @param rvec2_nativeObj  native matrix or object handle for {@code rvec2}; Second rotation vector
     * @param tvec2_nativeObj  native matrix or object handle for {@code tvec2}; Second translation vector
     * @param rvec3_nativeObj  native matrix or object handle for {@code rvec3}; Output rotation vector of the
     *                         superposition
     * @param tvec3_nativeObj  native matrix or object handle for {@code tvec3}; Output translation vector of the
     *                         superposition
     * @param dr3dr1_nativeObj native matrix or object handle for {@code dr3dr1}; Optional output derivative of rvec3
     *                         with regard to rvec1
     * @param dr3dt1_nativeObj native matrix or object handle for {@code dr3dt1}; Optional output derivative of rvec3
     *                         with regard to tvec1
     * @param dr3dr2_nativeObj native matrix or object handle for {@code dr3dr2}; Optional output derivative of rvec3
     *                         with regard to rvec2
     * @param dr3dt2_nativeObj native matrix or object handle for {@code dr3dt2}; Optional output derivative of rvec3
     *                         with regard to tvec2
     * @param dt3dr1_nativeObj native matrix or object handle for {@code dt3dr1}; Optional output derivative of tvec3
     *                         with regard to rvec1
     * @param dt3dt1_nativeObj native matrix or object handle for {@code dt3dt1}; Optional output derivative of tvec3
     *                         with regard to tvec1
     * @param dt3dr2_nativeObj native matrix or object handle for {@code dt3dr2}; Optional output derivative of tvec3
     *                         with regard to rvec2
     * @param dt3dt2_nativeObj native matrix or object handle for {@code dt3dt2}; Optional output derivative of tvec3
     *                         with regard to tvec2 The functions compute: {l} {@code rvec3} = {@code rodrigues} ^{-1}
     *                         left ( {@code rodrigues} ( {@code rvec2} ) cdot {@code rodrigues} ( {@code rvec1} ) right
     *                         ) \\ {@code tvec3} = {@code rodrigues} ( {@code rvec2} ) cdot {@code tvec1} +
     *                         {@code tvec2} , where {@code rodrigues} denotes a rotation vector to a rotation matrix
     *                         transformation, and {@code rodrigues}^{-1} denotes the inverse transformation. See
     *                         Rodrigues for details. Also, the functions can compute the derivatives of the output
     *                         vectors with regards to the input vectors (see matMulDeriv ). The functions are used
     *                         inside stereoCalibrate but can also be used in your own code where Levenberg-Marquardt or
     *                         another gradient-based solver is used to optimize a function that contains a matrix
     *                         multiplication
     */
    private static native void composeRT_0(
            long rvec1_nativeObj,
            long tvec1_nativeObj,
            long rvec2_nativeObj,
            long tvec2_nativeObj,
            long rvec3_nativeObj,
            long tvec3_nativeObj,
            long dr3dr1_nativeObj,
            long dr3dt1_nativeObj,
            long dr3dr2_nativeObj,
            long dr3dt2_nativeObj,
            long dt3dr1_nativeObj,
            long dt3dt1_nativeObj,
            long dt3dr2_nativeObj,
            long dt3dt2_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#composeRT(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param rvec1_nativeObj  native matrix or object handle for {@code rvec1}; First rotation vector
     * @param tvec1_nativeObj  native matrix or object handle for {@code tvec1}; First translation vector
     * @param rvec2_nativeObj  native matrix or object handle for {@code rvec2}; Second rotation vector
     * @param tvec2_nativeObj  native matrix or object handle for {@code tvec2}; Second translation vector
     * @param rvec3_nativeObj  native matrix or object handle for {@code rvec3}; Output rotation vector of the
     *                         superposition
     * @param tvec3_nativeObj  native matrix or object handle for {@code tvec3}; Output translation vector of the
     *                         superposition
     * @param dr3dr1_nativeObj native matrix or object handle for {@code dr3dr1}; Optional output derivative of rvec3
     *                         with regard to rvec1
     * @param dr3dt1_nativeObj native matrix or object handle for {@code dr3dt1}; Optional output derivative of rvec3
     *                         with regard to tvec1
     * @param dr3dr2_nativeObj native matrix or object handle for {@code dr3dr2}; Optional output derivative of rvec3
     *                         with regard to rvec2
     * @param dr3dt2_nativeObj native matrix or object handle for {@code dr3dt2}; Optional output derivative of rvec3
     *                         with regard to tvec2
     * @param dt3dr1_nativeObj native matrix or object handle for {@code dt3dr1}; Optional output derivative of tvec3
     *                         with regard to rvec1
     * @param dt3dt1_nativeObj native matrix or object handle for {@code dt3dt1}; Optional output derivative of tvec3
     *                         with regard to tvec1
     * @param dt3dr2_nativeObj native matrix or object handle for {@code dt3dr2}; Optional output derivative of tvec3
     *                         with regard to rvec2
     */
    private static native void composeRT_1(
            long rvec1_nativeObj,
            long tvec1_nativeObj,
            long rvec2_nativeObj,
            long tvec2_nativeObj,
            long rvec3_nativeObj,
            long tvec3_nativeObj,
            long dr3dr1_nativeObj,
            long dr3dt1_nativeObj,
            long dr3dr2_nativeObj,
            long dr3dt2_nativeObj,
            long dt3dr1_nativeObj,
            long dt3dt1_nativeObj,
            long dt3dr2_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#composeRT(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param rvec1_nativeObj  native matrix or object handle for {@code rvec1}; First rotation vector
     * @param tvec1_nativeObj  native matrix or object handle for {@code tvec1}; First translation vector
     * @param rvec2_nativeObj  native matrix or object handle for {@code rvec2}; Second rotation vector
     * @param tvec2_nativeObj  native matrix or object handle for {@code tvec2}; Second translation vector
     * @param rvec3_nativeObj  native matrix or object handle for {@code rvec3}; Output rotation vector of the
     *                         superposition
     * @param tvec3_nativeObj  native matrix or object handle for {@code tvec3}; Output translation vector of the
     *                         superposition
     * @param dr3dr1_nativeObj native matrix or object handle for {@code dr3dr1}; Optional output derivative of rvec3
     *                         with regard to rvec1
     * @param dr3dt1_nativeObj native matrix or object handle for {@code dr3dt1}; Optional output derivative of rvec3
     *                         with regard to tvec1
     * @param dr3dr2_nativeObj native matrix or object handle for {@code dr3dr2}; Optional output derivative of rvec3
     *                         with regard to rvec2
     * @param dr3dt2_nativeObj native matrix or object handle for {@code dr3dt2}; Optional output derivative of rvec3
     *                         with regard to tvec2
     * @param dt3dr1_nativeObj native matrix or object handle for {@code dt3dr1}; Optional output derivative of tvec3
     *                         with regard to rvec1
     * @param dt3dt1_nativeObj native matrix or object handle for {@code dt3dt1}; Optional output derivative of tvec3
     *                         with regard to tvec1
     */
    private static native void composeRT_2(
            long rvec1_nativeObj,
            long tvec1_nativeObj,
            long rvec2_nativeObj,
            long tvec2_nativeObj,
            long rvec3_nativeObj,
            long tvec3_nativeObj,
            long dr3dr1_nativeObj,
            long dr3dt1_nativeObj,
            long dr3dr2_nativeObj,
            long dr3dt2_nativeObj,
            long dt3dr1_nativeObj,
            long dt3dt1_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#composeRT(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param rvec1_nativeObj  native matrix or object handle for {@code rvec1}; First rotation vector
     * @param tvec1_nativeObj  native matrix or object handle for {@code tvec1}; First translation vector
     * @param rvec2_nativeObj  native matrix or object handle for {@code rvec2}; Second rotation vector
     * @param tvec2_nativeObj  native matrix or object handle for {@code tvec2}; Second translation vector
     * @param rvec3_nativeObj  native matrix or object handle for {@code rvec3}; Output rotation vector of the
     *                         superposition
     * @param tvec3_nativeObj  native matrix or object handle for {@code tvec3}; Output translation vector of the
     *                         superposition
     * @param dr3dr1_nativeObj native matrix or object handle for {@code dr3dr1}; Optional output derivative of rvec3
     *                         with regard to rvec1
     * @param dr3dt1_nativeObj native matrix or object handle for {@code dr3dt1}; Optional output derivative of rvec3
     *                         with regard to tvec1
     * @param dr3dr2_nativeObj native matrix or object handle for {@code dr3dr2}; Optional output derivative of rvec3
     *                         with regard to rvec2
     * @param dr3dt2_nativeObj native matrix or object handle for {@code dr3dt2}; Optional output derivative of rvec3
     *                         with regard to tvec2
     * @param dt3dr1_nativeObj native matrix or object handle for {@code dt3dr1}; Optional output derivative of tvec3
     *                         with regard to rvec1
     */
    private static native void composeRT_3(
            long rvec1_nativeObj,
            long tvec1_nativeObj,
            long rvec2_nativeObj,
            long tvec2_nativeObj,
            long rvec3_nativeObj,
            long tvec3_nativeObj,
            long dr3dr1_nativeObj,
            long dr3dt1_nativeObj,
            long dr3dr2_nativeObj,
            long dr3dt2_nativeObj,
            long dt3dr1_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#composeRT(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param rvec1_nativeObj  native matrix or object handle for {@code rvec1}; First rotation vector
     * @param tvec1_nativeObj  native matrix or object handle for {@code tvec1}; First translation vector
     * @param rvec2_nativeObj  native matrix or object handle for {@code rvec2}; Second rotation vector
     * @param tvec2_nativeObj  native matrix or object handle for {@code tvec2}; Second translation vector
     * @param rvec3_nativeObj  native matrix or object handle for {@code rvec3}; Output rotation vector of the
     *                         superposition
     * @param tvec3_nativeObj  native matrix or object handle for {@code tvec3}; Output translation vector of the
     *                         superposition
     * @param dr3dr1_nativeObj native matrix or object handle for {@code dr3dr1}; Optional output derivative of rvec3
     *                         with regard to rvec1
     * @param dr3dt1_nativeObj native matrix or object handle for {@code dr3dt1}; Optional output derivative of rvec3
     *                         with regard to tvec1
     * @param dr3dr2_nativeObj native matrix or object handle for {@code dr3dr2}; Optional output derivative of rvec3
     *                         with regard to rvec2
     * @param dr3dt2_nativeObj native matrix or object handle for {@code dr3dt2}; Optional output derivative of rvec3
     *                         with regard to tvec2
     */
    private static native void composeRT_4(
            long rvec1_nativeObj,
            long tvec1_nativeObj,
            long rvec2_nativeObj,
            long tvec2_nativeObj,
            long rvec3_nativeObj,
            long tvec3_nativeObj,
            long dr3dr1_nativeObj,
            long dr3dt1_nativeObj,
            long dr3dr2_nativeObj,
            long dr3dt2_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#composeRT(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param rvec1_nativeObj  native matrix or object handle for {@code rvec1}; First rotation vector
     * @param tvec1_nativeObj  native matrix or object handle for {@code tvec1}; First translation vector
     * @param rvec2_nativeObj  native matrix or object handle for {@code rvec2}; Second rotation vector
     * @param tvec2_nativeObj  native matrix or object handle for {@code tvec2}; Second translation vector
     * @param rvec3_nativeObj  native matrix or object handle for {@code rvec3}; Output rotation vector of the
     *                         superposition
     * @param tvec3_nativeObj  native matrix or object handle for {@code tvec3}; Output translation vector of the
     *                         superposition
     * @param dr3dr1_nativeObj native matrix or object handle for {@code dr3dr1}; Optional output derivative of rvec3
     *                         with regard to rvec1
     * @param dr3dt1_nativeObj native matrix or object handle for {@code dr3dt1}; Optional output derivative of rvec3
     *                         with regard to tvec1
     * @param dr3dr2_nativeObj native matrix or object handle for {@code dr3dr2}; Optional output derivative of rvec3
     *                         with regard to rvec2
     */
    private static native void composeRT_5(
            long rvec1_nativeObj,
            long tvec1_nativeObj,
            long rvec2_nativeObj,
            long tvec2_nativeObj,
            long rvec3_nativeObj,
            long tvec3_nativeObj,
            long dr3dr1_nativeObj,
            long dr3dt1_nativeObj,
            long dr3dr2_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#composeRT(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param rvec1_nativeObj  native matrix or object handle for {@code rvec1}; First rotation vector
     * @param tvec1_nativeObj  native matrix or object handle for {@code tvec1}; First translation vector
     * @param rvec2_nativeObj  native matrix or object handle for {@code rvec2}; Second rotation vector
     * @param tvec2_nativeObj  native matrix or object handle for {@code tvec2}; Second translation vector
     * @param rvec3_nativeObj  native matrix or object handle for {@code rvec3}; Output rotation vector of the
     *                         superposition
     * @param tvec3_nativeObj  native matrix or object handle for {@code tvec3}; Output translation vector of the
     *                         superposition
     * @param dr3dr1_nativeObj native matrix or object handle for {@code dr3dr1}; Optional output derivative of rvec3
     *                         with regard to rvec1
     * @param dr3dt1_nativeObj native matrix or object handle for {@code dr3dt1}; Optional output derivative of rvec3
     *                         with regard to tvec1
     */
    private static native void composeRT_6(
            long rvec1_nativeObj,
            long tvec1_nativeObj,
            long rvec2_nativeObj,
            long tvec2_nativeObj,
            long rvec3_nativeObj,
            long tvec3_nativeObj,
            long dr3dr1_nativeObj,
            long dr3dt1_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#composeRT(Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param rvec1_nativeObj  native matrix or object handle for {@code rvec1}; First rotation vector
     * @param tvec1_nativeObj  native matrix or object handle for {@code tvec1}; First translation vector
     * @param rvec2_nativeObj  native matrix or object handle for {@code rvec2}; Second rotation vector
     * @param tvec2_nativeObj  native matrix or object handle for {@code tvec2}; Second translation vector
     * @param rvec3_nativeObj  native matrix or object handle for {@code rvec3}; Output rotation vector of the
     *                         superposition
     * @param tvec3_nativeObj  native matrix or object handle for {@code tvec3}; Output translation vector of the
     *                         superposition
     * @param dr3dr1_nativeObj native matrix or object handle for {@code dr3dr1}; Optional output derivative of rvec3
     *                         with regard to rvec1
     */
    private static native void composeRT_7(
            long rvec1_nativeObj,
            long tvec1_nativeObj,
            long rvec2_nativeObj,
            long tvec2_nativeObj,
            long rvec3_nativeObj,
            long tvec3_nativeObj,
            long dr3dr1_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#composeRT(Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param rvec1_nativeObj native matrix or object handle for {@code rvec1}; First rotation vector
     * @param tvec1_nativeObj native matrix or object handle for {@code tvec1}; First translation vector
     * @param rvec2_nativeObj native matrix or object handle for {@code rvec2}; Second rotation vector
     * @param tvec2_nativeObj native matrix or object handle for {@code tvec2}; Second translation vector
     * @param rvec3_nativeObj native matrix or object handle for {@code rvec3}; Output rotation vector of the
     *                        superposition
     * @param tvec3_nativeObj native matrix or object handle for {@code tvec3}; Output translation vector of the
     *                        superposition
     */
    private static native void composeRT_8(
            long rvec1_nativeObj,
            long tvec1_nativeObj,
            long rvec2_nativeObj,
            long tvec2_nativeObj,
            long rvec3_nativeObj,
            long tvec3_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#projectPoints(MatOfPoint3f,Mat,Mat,Mat,MatOfDouble,MatOfPoint2f,Mat,double)}.
     *
     * @param objectPoints_mat_nativeObj native matrix or object handle for {@code objectPoints_mat}; Array of object
     *                                   points expressed wrt. the world coordinate frame. A 3xN/Nx3 1-channel or
     *                                   1xN/Nx1 3-channel (or vector&lt;Point3f&gt; ), where N is the number of points
     *                                   in the view
     * @param rvec_nativeObj             native matrix or object handle for {@code rvec}; The rotation vector (REF:
     *                                   Rodrigues) that, together with tvec, performs a change of basis from world to
     *                                   camera coordinate system, see REF: calibrateCamera for details
     * @param tvec_nativeObj             native matrix or object handle for {@code tvec}; The translation vector, see
     *                                   parameter description above
     * @param cameraMatrix_nativeObj     native matrix or object handle for {@code cameraMatrix}; Camera intrinsic
     *                                   matrix cameramatrix{A}
     * @param distCoeffs_mat_nativeObj   native matrix or object handle for {@code distCoeffs_mat}; Input vector of
     *                                   distortion coefficients distcoeffs . If the vector is empty, the zero
     *                                   distortion coefficients are assumed
     * @param imagePoints_mat_nativeObj  native matrix or object handle for {@code imagePoints_mat}; Output array of
     *                                   image points in pixel coordinates, 1xN/Nx1 2-channel, or vector&lt;Point2f&gt;
     * @param jacobian_nativeObj         native matrix or object handle for {@code jacobian}; Optional output
     *                                   2Nx(10+&lt;numDistCoeffs&gt;) jacobian matrix of derivatives of image points
     *                                   with respect to components of the rotation vector, translation vector, focal
     *                                   lengths, coordinates of the principal point and the distortion coefficients. In
     *                                   the old interface different components of the jacobian are returned via
     *                                   different output parameters
     * @param aspectRatio                Optional "fixed aspect ratio" parameter. If the parameter is not 0, the
     *                                   function assumes that the aspect ratio (f_x / f_y) is fixed and correspondingly
     *                                   adjusts the jacobian matrix. Note: By setting rvec = tvec = [0, 0, 0], or by
     *                                   setting cameraMatrix to a 3x3 identity matrix, or by passing zero distortion
     *                                   coefficients, one can get various useful partial cases of the function. This
     *                                   means, one can compute the distorted coordinates for a sparse set of points or
     *                                   apply a perspective transformation (and also compute the derivatives) in the
     *                                   ideal zero-distortion setup
     */
    private static native void projectPoints_0(
            long objectPoints_mat_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_mat_nativeObj,
            long imagePoints_mat_nativeObj,
            long jacobian_nativeObj,
            double aspectRatio);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#projectPoints(MatOfPoint3f,Mat,Mat,Mat,MatOfDouble,MatOfPoint2f,Mat)}.
     *
     * @param objectPoints_mat_nativeObj native matrix or object handle for {@code objectPoints_mat}; Array of object
     *                                   points expressed wrt. the world coordinate frame. A 3xN/Nx3 1-channel or
     *                                   1xN/Nx1 3-channel (or vector&lt;Point3f&gt; ), where N is the number of points
     *                                   in the view
     * @param rvec_nativeObj             native matrix or object handle for {@code rvec}; The rotation vector (REF:
     *                                   Rodrigues) that, together with tvec, performs a change of basis from world to
     *                                   camera coordinate system, see REF: calibrateCamera for details
     * @param tvec_nativeObj             native matrix or object handle for {@code tvec}; The translation vector, see
     *                                   parameter description above
     * @param cameraMatrix_nativeObj     native matrix or object handle for {@code cameraMatrix}; Camera intrinsic
     *                                   matrix cameramatrix{A}
     * @param distCoeffs_mat_nativeObj   native matrix or object handle for {@code distCoeffs_mat}; Input vector of
     *                                   distortion coefficients distcoeffs . If the vector is empty, the zero
     *                                   distortion coefficients are assumed
     * @param imagePoints_mat_nativeObj  native matrix or object handle for {@code imagePoints_mat}; Output array of
     *                                   image points in pixel coordinates, 1xN/Nx1 2-channel, or vector&lt;Point2f&gt;
     * @param jacobian_nativeObj         native matrix or object handle for {@code jacobian}; Optional output
     *                                   2Nx(10+&lt;numDistCoeffs&gt;) jacobian matrix of derivatives of image points
     *                                   with respect to components of the rotation vector, translation vector, focal
     *                                   lengths, coordinates of the principal point and the distortion coefficients. In
     *                                   the old interface different components of the jacobian are returned via
     *                                   different output parameters
     */
    private static native void projectPoints_1(
            long objectPoints_mat_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_mat_nativeObj,
            long imagePoints_mat_nativeObj,
            long jacobian_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#projectPoints(MatOfPoint3f,Mat,Mat,Mat,MatOfDouble,MatOfPoint2f)}.
     *
     * @param objectPoints_mat_nativeObj native matrix or object handle for {@code objectPoints_mat}; Array of object
     *                                   points expressed wrt. the world coordinate frame. A 3xN/Nx3 1-channel or
     *                                   1xN/Nx1 3-channel (or vector&lt;Point3f&gt; ), where N is the number of points
     *                                   in the view
     * @param rvec_nativeObj             native matrix or object handle for {@code rvec}; The rotation vector (REF:
     *                                   Rodrigues) that, together with tvec, performs a change of basis from world to
     *                                   camera coordinate system, see REF: calibrateCamera for details
     * @param tvec_nativeObj             native matrix or object handle for {@code tvec}; The translation vector, see
     *                                   parameter description above
     * @param cameraMatrix_nativeObj     native matrix or object handle for {@code cameraMatrix}; Camera intrinsic
     *                                   matrix cameramatrix{A}
     * @param distCoeffs_mat_nativeObj   native matrix or object handle for {@code distCoeffs_mat}; Input vector of
     *                                   distortion coefficients distcoeffs . If the vector is empty, the zero
     *                                   distortion coefficients are assumed
     * @param imagePoints_mat_nativeObj  native matrix or object handle for {@code imagePoints_mat}; Output array of
     *                                   image points in pixel coordinates, 1xN/Nx1 2-channel, or vector&lt;Point2f&gt;
     */
    private static native void projectPoints_2(
            long objectPoints_mat_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_mat_nativeObj,
            long imagePoints_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#projectPointsSepJ(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,double)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; three-dimensional object
     *                               coordinates expressed in the object reference frame
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; rotation vector in Rodrigues form
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; translation vector between object
     *                               and camera coordinates
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; 3-by-3 intrinsic camera
     *                               matrix
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; camera distortion
     *                               coefficients
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; two-dimensional coordinates
     *                               in the image plane
     * @param dpdr_nativeObj         native matrix or object handle for {@code dpdr}; output Jacobian of image
     *                               coordinates with respect to rotation
     * @param dpdt_nativeObj         native matrix or object handle for {@code dpdt}; output Jacobian of image
     *                               coordinates with respect to translation
     * @param dpdf_nativeObj         native matrix or object handle for {@code dpdf}; output Jacobian with respect to
     *                               focal lengths
     * @param dpdc_nativeObj         native matrix or object handle for {@code dpdc}; output Jacobian with respect to
     *                               the principal point
     * @param dpdk_nativeObj         native matrix or object handle for {@code dpdk}; output Jacobian with respect to
     *                               distortion coefficients
     * @param dpdo_nativeObj         native matrix or object handle for {@code dpdo}; output Jacobian with respect to
     *                               object-point coordinates
     * @param aspectRatio            fixed ratio between horizontal and vertical focal lengths; zero leaves them
     *                               independent
     */
    private static native void projectPointsSepJ_0(
            long objectPoints_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long imagePoints_nativeObj,
            long dpdr_nativeObj,
            long dpdt_nativeObj,
            long dpdf_nativeObj,
            long dpdc_nativeObj,
            long dpdk_nativeObj,
            long dpdo_nativeObj,
            double aspectRatio);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#projectPointsSepJ(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; three-dimensional object
     *                               coordinates expressed in the object reference frame
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; rotation vector in Rodrigues form
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; translation vector between object
     *                               and camera coordinates
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; 3-by-3 intrinsic camera
     *                               matrix
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; camera distortion
     *                               coefficients
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; two-dimensional coordinates
     *                               in the image plane
     * @param dpdr_nativeObj         native matrix or object handle for {@code dpdr}; output Jacobian of image
     *                               coordinates with respect to rotation
     * @param dpdt_nativeObj         native matrix or object handle for {@code dpdt}; output Jacobian of image
     *                               coordinates with respect to translation
     * @param dpdf_nativeObj         native matrix or object handle for {@code dpdf}; output Jacobian with respect to
     *                               focal lengths
     * @param dpdc_nativeObj         native matrix or object handle for {@code dpdc}; output Jacobian with respect to
     *                               the principal point
     * @param dpdk_nativeObj         native matrix or object handle for {@code dpdk}; output Jacobian with respect to
     *                               distortion coefficients
     * @param dpdo_nativeObj         native matrix or object handle for {@code dpdo}; output Jacobian with respect to
     *                               object-point coordinates
     */
    private static native void projectPointsSepJ_1(
            long objectPoints_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long imagePoints_nativeObj,
            long dpdr_nativeObj,
            long dpdt_nativeObj,
            long dpdf_nativeObj,
            long dpdc_nativeObj,
            long dpdk_nativeObj,
            long dpdo_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#projectPointsSepJ(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; three-dimensional object
     *                               coordinates expressed in the object reference frame
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; rotation vector in Rodrigues form
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; translation vector between object
     *                               and camera coordinates
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; 3-by-3 intrinsic camera
     *                               matrix
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; camera distortion
     *                               coefficients
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; two-dimensional coordinates
     *                               in the image plane
     * @param dpdr_nativeObj         native matrix or object handle for {@code dpdr}; output Jacobian of image
     *                               coordinates with respect to rotation
     * @param dpdt_nativeObj         native matrix or object handle for {@code dpdt}; output Jacobian of image
     *                               coordinates with respect to translation
     * @param dpdf_nativeObj         native matrix or object handle for {@code dpdf}; output Jacobian with respect to
     *                               focal lengths
     * @param dpdc_nativeObj         native matrix or object handle for {@code dpdc}; output Jacobian with respect to
     *                               the principal point
     * @param dpdk_nativeObj         native matrix or object handle for {@code dpdk}; output Jacobian with respect to
     *                               distortion coefficients
     */
    private static native void projectPointsSepJ_2(
            long objectPoints_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long imagePoints_nativeObj,
            long dpdr_nativeObj,
            long dpdt_nativeObj,
            long dpdf_nativeObj,
            long dpdc_nativeObj,
            long dpdk_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#projectPointsSepJ(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; three-dimensional object
     *                               coordinates expressed in the object reference frame
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; rotation vector in Rodrigues form
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; translation vector between object
     *                               and camera coordinates
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; 3-by-3 intrinsic camera
     *                               matrix
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; camera distortion
     *                               coefficients
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; two-dimensional coordinates
     *                               in the image plane
     * @param dpdr_nativeObj         native matrix or object handle for {@code dpdr}; output Jacobian of image
     *                               coordinates with respect to rotation
     * @param dpdt_nativeObj         native matrix or object handle for {@code dpdt}; output Jacobian of image
     *                               coordinates with respect to translation
     * @param dpdf_nativeObj         native matrix or object handle for {@code dpdf}; output Jacobian with respect to
     *                               focal lengths
     * @param dpdc_nativeObj         native matrix or object handle for {@code dpdc}; output Jacobian with respect to
     *                               the principal point
     */
    private static native void projectPointsSepJ_3(
            long objectPoints_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long imagePoints_nativeObj,
            long dpdr_nativeObj,
            long dpdt_nativeObj,
            long dpdf_nativeObj,
            long dpdc_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#projectPointsSepJ(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; three-dimensional object
     *                               coordinates expressed in the object reference frame
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; rotation vector in Rodrigues form
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; translation vector between object
     *                               and camera coordinates
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; 3-by-3 intrinsic camera
     *                               matrix
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; camera distortion
     *                               coefficients
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; two-dimensional coordinates
     *                               in the image plane
     * @param dpdr_nativeObj         native matrix or object handle for {@code dpdr}; output Jacobian of image
     *                               coordinates with respect to rotation
     * @param dpdt_nativeObj         native matrix or object handle for {@code dpdt}; output Jacobian of image
     *                               coordinates with respect to translation
     * @param dpdf_nativeObj         native matrix or object handle for {@code dpdf}; output Jacobian with respect to
     *                               focal lengths
     */
    private static native void projectPointsSepJ_4(
            long objectPoints_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long imagePoints_nativeObj,
            long dpdr_nativeObj,
            long dpdt_nativeObj,
            long dpdf_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#projectPointsSepJ(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; three-dimensional object
     *                               coordinates expressed in the object reference frame
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; rotation vector in Rodrigues form
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; translation vector between object
     *                               and camera coordinates
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; 3-by-3 intrinsic camera
     *                               matrix
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; camera distortion
     *                               coefficients
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; two-dimensional coordinates
     *                               in the image plane
     * @param dpdr_nativeObj         native matrix or object handle for {@code dpdr}; output Jacobian of image
     *                               coordinates with respect to rotation
     * @param dpdt_nativeObj         native matrix or object handle for {@code dpdt}; output Jacobian of image
     *                               coordinates with respect to translation
     */
    private static native void projectPointsSepJ_5(
            long objectPoints_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long imagePoints_nativeObj,
            long dpdr_nativeObj,
            long dpdt_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnP(MatOfPoint3f,MatOfPoint2f,Mat,MatOfDouble,Mat,Mat,boolean,int)}.
     *
     * @param objectPoints_mat_nativeObj native matrix or object handle for {@code objectPoints_mat}; Array of object
     *                                   points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                                   where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_mat_nativeObj  native matrix or object handle for {@code imagePoints_mat}; Array of
     *                                   corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                                   number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj     native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                                   matrix cameramatrix{A}
     * @param distCoeffs_mat_nativeObj   native matrix or object handle for {@code distCoeffs_mat}; Input vector of
     *                                   distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                                   distortion coefficients are assumed
     * @param rvec_nativeObj             native matrix or object handle for {@code rvec}; Output rotation vector (see
     *                                   REF: Rodrigues ) that, together with tvec, brings points from the model
     *                                   coordinate system to the camera coordinate system
     * @param tvec_nativeObj             native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess          Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the
     *                                   provided rvec and tvec values as initial approximations of the rotation and
     *                                   translation vectors, respectively, and further optimizes them
     * @param flags                      Method for solving a PnP problem: see REF: calib3d_solvePnP_flags More
     *                                   information about Perspective-n-Points is described in REF: calib3d_solvePnP
     *                                   Note: An example of how to use solvePnP for planar augmented reality can be
     *                                   found at opencv_source_code/samples/python/plane_ar.py If you are using Python:
     *                                   Thus, given some data D = np.array(...) where D.shape = (N,M), in order to use
     *                                   a subset of it as, e.g., imagePoints, one must effectively copy it into a new
     *                                   array: imagePoints = np.ascontiguousarray(D[:,:2]).reshape((N,1,2)) The minimum
     *                                   number of points is 4 in the general case. In the case of REF: SOLVEPNP_P3P and
     *                                   REF: SOLVEPNP_AP3P methods, it is required to use exactly 4 points (the first 3
     *                                   points are used to estimate all the solutions of the P3P problem, the last one
     *                                   is used to retain the best solution that minimizes the reprojection error).
     *                                   With REF: SOLVEPNP_ITERATIVE method and useExtrinsicGuess=true, the minimum
     *                                   number of points is 3 (3 points are sufficient to compute a pose but there are
     *                                   up to 4 solutions). The initial solution should be close to the global solution
     *                                   to converge. The function returns true if some solution is found. User code is
     *                                   responsible for solution quality assessment. With REF: SOLVEPNP_IPPE input
     *                                   points must be &gt;= 4 and object points must be coplanar. With REF:
     *                                   SOLVEPNP_IPPE_SQUARE this is a special case suitable for marker pose
     *                                   estimation. Number of input points must be 4. Object points must be defined in
     *                                   the following order: point 0: [-squareLength / 2, squareLength / 2, 0] point 1:
     *                                   [ squareLength / 2, squareLength / 2, 0] point 2: [ squareLength / 2,
     *                                   -squareLength / 2, 0] point 3: [-squareLength / 2, -squareLength / 2, 0] With
     *                                   REF: SOLVEPNP_SQPNP input points must be &gt;= 3
     * @return true if a pose is found
     */
    private static native boolean solvePnP_0(
            long objectPoints_mat_nativeObj,
            long imagePoints_mat_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_mat_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess,
            int flags);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnP(MatOfPoint3f,MatOfPoint2f,Mat,MatOfDouble,Mat,Mat,boolean)}.
     *
     * @param objectPoints_mat_nativeObj native matrix or object handle for {@code objectPoints_mat}; Array of object
     *                                   points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                                   where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_mat_nativeObj  native matrix or object handle for {@code imagePoints_mat}; Array of
     *                                   corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                                   number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj     native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                                   matrix cameramatrix{A}
     * @param distCoeffs_mat_nativeObj   native matrix or object handle for {@code distCoeffs_mat}; Input vector of
     *                                   distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                                   distortion coefficients are assumed
     * @param rvec_nativeObj             native matrix or object handle for {@code rvec}; Output rotation vector (see
     *                                   REF: Rodrigues ) that, together with tvec, brings points from the model
     *                                   coordinate system to the camera coordinate system
     * @param tvec_nativeObj             native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess          Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the
     *                                   provided rvec and tvec values as initial approximations of the rotation and
     *                                   translation vectors, respectively, and further optimizes them
     * @return true if a pose is found
     */
    private static native boolean solvePnP_1(
            long objectPoints_mat_nativeObj,
            long imagePoints_mat_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_mat_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnP(MatOfPoint3f,MatOfPoint2f,Mat,MatOfDouble,Mat,Mat)}.
     *
     * @param objectPoints_mat_nativeObj native matrix or object handle for {@code objectPoints_mat}; Array of object
     *                                   points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                                   where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_mat_nativeObj  native matrix or object handle for {@code imagePoints_mat}; Array of
     *                                   corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                                   number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj     native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                                   matrix cameramatrix{A}
     * @param distCoeffs_mat_nativeObj   native matrix or object handle for {@code distCoeffs_mat}; Input vector of
     *                                   distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                                   distortion coefficients are assumed
     * @param rvec_nativeObj             native matrix or object handle for {@code rvec}; Output rotation vector (see
     *                                   REF: Rodrigues ) that, together with tvec, brings points from the model
     *                                   coordinate system to the camera coordinate system
     * @param tvec_nativeObj             native matrix or object handle for {@code tvec}; Output translation vector
     * @return true if a pose is found
     */
    private static native boolean solvePnP_2(
            long objectPoints_mat_nativeObj,
            long imagePoints_mat_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_mat_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPRansac(MatOfPoint3f,MatOfPoint2f,Mat,MatOfDouble,Mat,Mat,boolean,int,float,double,Mat,int)}.
     *
     * @param objectPoints_mat_nativeObj native matrix or object handle for {@code objectPoints_mat}; Array of object
     *                                   points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                                   where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_mat_nativeObj  native matrix or object handle for {@code imagePoints_mat}; Array of
     *                                   corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                                   number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj     native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                                   matrix cameramatrix{A}
     * @param distCoeffs_mat_nativeObj   native matrix or object handle for {@code distCoeffs_mat}; Input vector of
     *                                   distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                                   distortion coefficients are assumed
     * @param rvec_nativeObj             native matrix or object handle for {@code rvec}; Output rotation vector (see
     *                                   REF: Rodrigues ) that, together with tvec, brings points from the model
     *                                   coordinate system to the camera coordinate system
     * @param tvec_nativeObj             native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess          Parameter used for REF: SOLVEPNP_ITERATIVE. If true (1), the function uses the
     *                                   provided rvec and tvec values as initial approximations of the rotation and
     *                                   translation vectors, respectively, and further optimizes them
     * @param iterationsCount            Number of iterations
     * @param reprojectionError          reprojection-error threshold used to classify inliers
     * @param confidence                 The probability that the algorithm produces a useful result
     * @param inliers_nativeObj          native matrix or object handle for {@code inliers}; Output vector that contains
     *                                   indices of inliers in objectPoints and imagePoints
     * @param flags                      Method for solving a PnP problem (see REF: solvePnP ). The function estimates
     *                                   an object pose given a set of object points, their corresponding image
     *                                   projections, as well as the camera intrinsic matrix and the distortion
     *                                   coefficients. This function finds such a pose that minimizes reprojection
     *                                   error, that is, the sum of squared distances between the observed projections
     *                                   imagePoints and the projected (using REF: projectPoints ) objectPoints. The use
     *                                   of RANSAC makes the function resistant to outliers. Note: An example of how to
     *                                   use solvePnPRansac for object detection can be found at REF:
     *                                   tutorial_real_time_pose The default method used to estimate the camera pose for
     *                                   the Minimal Sample Sets step is SOLVEPNP_EPNP. Exceptions are: if you choose
     *                                   SOLVEPNP_P3P or SOLVEPNP_AP3P, these methods will be used. if the number of
     *                                   input points is equal to 4, SOLVEPNP_P3P is used. The method used to estimate
     *                                   the camera pose using all the inliers is defined by the flags parameters unless
     *                                   it is equal to SOLVEPNP_P3P or SOLVEPNP_AP3P. In this case, the method
     *                                   SOLVEPNP_EPNP will be used instead
     * @return true if robust pose estimation succeeds
     */
    private static native boolean solvePnPRansac_0(
            long objectPoints_mat_nativeObj,
            long imagePoints_mat_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_mat_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError,
            double confidence,
            long inliers_nativeObj,
            int flags);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPRansac(MatOfPoint3f,MatOfPoint2f,Mat,MatOfDouble,Mat,Mat,boolean,int,float,double,Mat)}.
     *
     * @param objectPoints_mat_nativeObj native matrix or object handle for {@code objectPoints_mat}; Array of object
     *                                   points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                                   where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_mat_nativeObj  native matrix or object handle for {@code imagePoints_mat}; Array of
     *                                   corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                                   number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj     native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                                   matrix cameramatrix{A}
     * @param distCoeffs_mat_nativeObj   native matrix or object handle for {@code distCoeffs_mat}; Input vector of
     *                                   distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                                   distortion coefficients are assumed
     * @param rvec_nativeObj             native matrix or object handle for {@code rvec}; Output rotation vector (see
     *                                   REF: Rodrigues ) that, together with tvec, brings points from the model
     *                                   coordinate system to the camera coordinate system
     * @param tvec_nativeObj             native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess          Parameter used for REF: SOLVEPNP_ITERATIVE. If true (1), the function uses the
     *                                   provided rvec and tvec values as initial approximations of the rotation and
     *                                   translation vectors, respectively, and further optimizes them
     * @param iterationsCount            Number of iterations
     * @param reprojectionError          reprojection-error threshold used to classify inliers
     * @param confidence                 The probability that the algorithm produces a useful result
     * @param inliers_nativeObj          native matrix or object handle for {@code inliers}; Output vector that contains
     *                                   indices of inliers in objectPoints and imagePoints
     * @return true if robust pose estimation succeeds
     */
    private static native boolean solvePnPRansac_1(
            long objectPoints_mat_nativeObj,
            long imagePoints_mat_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_mat_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError,
            double confidence,
            long inliers_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPRansac(MatOfPoint3f,MatOfPoint2f,Mat,MatOfDouble,Mat,Mat,boolean,int,float,double)}.
     *
     * @param objectPoints_mat_nativeObj native matrix or object handle for {@code objectPoints_mat}; Array of object
     *                                   points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                                   where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_mat_nativeObj  native matrix or object handle for {@code imagePoints_mat}; Array of
     *                                   corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                                   number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj     native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                                   matrix cameramatrix{A}
     * @param distCoeffs_mat_nativeObj   native matrix or object handle for {@code distCoeffs_mat}; Input vector of
     *                                   distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                                   distortion coefficients are assumed
     * @param rvec_nativeObj             native matrix or object handle for {@code rvec}; Output rotation vector (see
     *                                   REF: Rodrigues ) that, together with tvec, brings points from the model
     *                                   coordinate system to the camera coordinate system
     * @param tvec_nativeObj             native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess          Parameter used for REF: SOLVEPNP_ITERATIVE. If true (1), the function uses the
     *                                   provided rvec and tvec values as initial approximations of the rotation and
     *                                   translation vectors, respectively, and further optimizes them
     * @param iterationsCount            Number of iterations
     * @param reprojectionError          reprojection-error threshold used to classify inliers
     * @param confidence                 The probability that the algorithm produces a useful result
     * @return true if robust pose estimation succeeds
     */
    private static native boolean solvePnPRansac_2(
            long objectPoints_mat_nativeObj,
            long imagePoints_mat_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_mat_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError,
            double confidence);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPRansac(MatOfPoint3f,MatOfPoint2f,Mat,MatOfDouble,Mat,Mat,boolean,int,float)}.
     *
     * @param objectPoints_mat_nativeObj native matrix or object handle for {@code objectPoints_mat}; Array of object
     *                                   points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                                   where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_mat_nativeObj  native matrix or object handle for {@code imagePoints_mat}; Array of
     *                                   corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                                   number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj     native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                                   matrix cameramatrix{A}
     * @param distCoeffs_mat_nativeObj   native matrix or object handle for {@code distCoeffs_mat}; Input vector of
     *                                   distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                                   distortion coefficients are assumed
     * @param rvec_nativeObj             native matrix or object handle for {@code rvec}; Output rotation vector (see
     *                                   REF: Rodrigues ) that, together with tvec, brings points from the model
     *                                   coordinate system to the camera coordinate system
     * @param tvec_nativeObj             native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess          Parameter used for REF: SOLVEPNP_ITERATIVE. If true (1), the function uses the
     *                                   provided rvec and tvec values as initial approximations of the rotation and
     *                                   translation vectors, respectively, and further optimizes them
     * @param iterationsCount            Number of iterations
     * @param reprojectionError          reprojection-error threshold used to classify inliers
     * @return true if robust pose estimation succeeds
     */
    private static native boolean solvePnPRansac_3(
            long objectPoints_mat_nativeObj,
            long imagePoints_mat_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_mat_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPRansac(MatOfPoint3f,MatOfPoint2f,Mat,MatOfDouble,Mat,Mat,boolean,int)}.
     *
     * @param objectPoints_mat_nativeObj native matrix or object handle for {@code objectPoints_mat}; Array of object
     *                                   points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                                   where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_mat_nativeObj  native matrix or object handle for {@code imagePoints_mat}; Array of
     *                                   corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                                   number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj     native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                                   matrix cameramatrix{A}
     * @param distCoeffs_mat_nativeObj   native matrix or object handle for {@code distCoeffs_mat}; Input vector of
     *                                   distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                                   distortion coefficients are assumed
     * @param rvec_nativeObj             native matrix or object handle for {@code rvec}; Output rotation vector (see
     *                                   REF: Rodrigues ) that, together with tvec, brings points from the model
     *                                   coordinate system to the camera coordinate system
     * @param tvec_nativeObj             native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess          Parameter used for REF: SOLVEPNP_ITERATIVE. If true (1), the function uses the
     *                                   provided rvec and tvec values as initial approximations of the rotation and
     *                                   translation vectors, respectively, and further optimizes them
     * @param iterationsCount            Number of iterations
     * @return true if robust pose estimation succeeds
     */
    private static native boolean solvePnPRansac_4(
            long objectPoints_mat_nativeObj,
            long imagePoints_mat_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_mat_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess,
            int iterationsCount);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPRansac(MatOfPoint3f,MatOfPoint2f,Mat,MatOfDouble,Mat,Mat,boolean)}.
     *
     * @param objectPoints_mat_nativeObj native matrix or object handle for {@code objectPoints_mat}; Array of object
     *                                   points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                                   where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_mat_nativeObj  native matrix or object handle for {@code imagePoints_mat}; Array of
     *                                   corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                                   number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj     native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                                   matrix cameramatrix{A}
     * @param distCoeffs_mat_nativeObj   native matrix or object handle for {@code distCoeffs_mat}; Input vector of
     *                                   distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                                   distortion coefficients are assumed
     * @param rvec_nativeObj             native matrix or object handle for {@code rvec}; Output rotation vector (see
     *                                   REF: Rodrigues ) that, together with tvec, brings points from the model
     *                                   coordinate system to the camera coordinate system
     * @param tvec_nativeObj             native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess          Parameter used for REF: SOLVEPNP_ITERATIVE. If true (1), the function uses the
     *                                   provided rvec and tvec values as initial approximations of the rotation and
     *                                   translation vectors, respectively, and further optimizes them
     * @return true if robust pose estimation succeeds
     */
    private static native boolean solvePnPRansac_5(
            long objectPoints_mat_nativeObj,
            long imagePoints_mat_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_mat_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPRansac(MatOfPoint3f,MatOfPoint2f,Mat,MatOfDouble,Mat,Mat)}.
     *
     * @param objectPoints_mat_nativeObj native matrix or object handle for {@code objectPoints_mat}; Array of object
     *                                   points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                                   where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_mat_nativeObj  native matrix or object handle for {@code imagePoints_mat}; Array of
     *                                   corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                                   number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj     native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                                   matrix cameramatrix{A}
     * @param distCoeffs_mat_nativeObj   native matrix or object handle for {@code distCoeffs_mat}; Input vector of
     *                                   distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                                   distortion coefficients are assumed
     * @param rvec_nativeObj             native matrix or object handle for {@code rvec}; Output rotation vector (see
     *                                   REF: Rodrigues ) that, together with tvec, brings points from the model
     *                                   coordinate system to the camera coordinate system
     * @param tvec_nativeObj             native matrix or object handle for {@code tvec}; Output translation vector
     * @return true if robust pose estimation succeeds
     */
    private static native boolean solvePnPRansac_6(
            long objectPoints_mat_nativeObj,
            long imagePoints_mat_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_mat_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPRansac(MatOfPoint3f,MatOfPoint2f,Mat,MatOfDouble,Mat,Mat,Mat,UsacParams)}.
     *
     * @param objectPoints_mat_nativeObj native matrix or object handle for {@code objectPoints_mat}; Array of object
     *                                   points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                                   where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_mat_nativeObj  native matrix or object handle for {@code imagePoints_mat}; Array of
     *                                   corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                                   number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj     native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                                   matrix cameramatrix{A}
     * @param distCoeffs_mat_nativeObj   native matrix or object handle for {@code distCoeffs_mat}; Input vector of
     *                                   distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                                   distortion coefficients are assumed
     * @param rvec_nativeObj             native matrix or object handle for {@code rvec}; Output rotation vector (see
     *                                   REF: Rodrigues ) that, together with tvec, brings points from the model
     *                                   coordinate system to the camera coordinate system
     * @param tvec_nativeObj             native matrix or object handle for {@code tvec}; Output translation vector
     * @param inliers_nativeObj          native matrix or object handle for {@code inliers}; Output vector that contains
     *                                   indices of inliers in objectPoints and imagePoints
     * @param params_nativeObj           {@code params.getNativeObjAddr()} component of robust-estimation sampling,
     *                                   scoring, threshold and refinement settings
     * @return true if robust pose estimation succeeds
     */
    private static native boolean solvePnPRansac_7(
            long objectPoints_mat_nativeObj,
            long imagePoints_mat_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_mat_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            long inliers_nativeObj,
            long params_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPRansac(MatOfPoint3f,MatOfPoint2f,Mat,MatOfDouble,Mat,Mat,Mat)}.
     *
     * @param objectPoints_mat_nativeObj native matrix or object handle for {@code objectPoints_mat}; Array of object
     *                                   points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                                   where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_mat_nativeObj  native matrix or object handle for {@code imagePoints_mat}; Array of
     *                                   corresponding image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the
     *                                   number of points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj     native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                                   matrix cameramatrix{A}
     * @param distCoeffs_mat_nativeObj   native matrix or object handle for {@code distCoeffs_mat}; Input vector of
     *                                   distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                                   distortion coefficients are assumed
     * @param rvec_nativeObj             native matrix or object handle for {@code rvec}; Output rotation vector (see
     *                                   REF: Rodrigues ) that, together with tvec, brings points from the model
     *                                   coordinate system to the camera coordinate system
     * @param tvec_nativeObj             native matrix or object handle for {@code tvec}; Output translation vector
     * @param inliers_nativeObj          native matrix or object handle for {@code inliers}; Output vector that contains
     *                                   indices of inliers in objectPoints and imagePoints
     * @return true if robust pose estimation succeeds
     */
    private static native boolean solvePnPRansac_8(
            long objectPoints_mat_nativeObj,
            long imagePoints_mat_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_mat_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            long inliers_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solveP3P(Mat,Mat,Mat,Mat,List<Mat>,List<Mat>,int)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, 3x3 1-channel or 1x3/3x1 3-channel.
     *                               vector&lt;Point3f&gt; can be also passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, 3x2 1-channel or 1x3/3x1 2-channel. vector&lt;Point2f&gt; can be also
     *                               passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is NULL/empty, the zero distortion
     *                               coefficients are assumed
     * @param rvecs_mat_nativeObj    native matrix or object handle for {@code rvecs_mat}; Output rotation vectors (see
     *                               REF: Rodrigues ) that, together with tvecs, brings points from the model coordinate
     *                               system to the camera coordinate system. A P3P problem has up to 4 solutions
     * @param tvecs_mat_nativeObj    native matrix or object handle for {@code tvecs_mat}; Output translation vectors
     * @param flags                  Method for solving a P3P problem: REF: SOLVEPNP_P3P Method is based on the paper of
     *                               Ding, Y., Yang, J., Larsson, V., Olsson, C., &amp; u212bstrom, K. "Revisiting the
     *                               P3P Problem" (). REF: SOLVEPNP_AP3P Method is based on the paper of T. Ke and S.
     *                               Roumeliotis. "An Efficient Algebraic Solution to the Perspective-Three-Point
     *                               Problem" (). The function estimates the object pose given 3 object points, their
     *                               corresponding image projections, as well as the camera intrinsic matrix and the
     *                               distortion coefficients. Note: The solutions are sorted by reprojection errors
     *                               (lowest to highest)
     * @return the number of returned pose solutions
     */
    private static native int solveP3P_0(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvecs_mat_nativeObj,
            long tvecs_mat_nativeObj,
            int flags);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPRefineLM(Mat,Mat,Mat,Mat,Mat,Mat,TermCriteria)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can also be passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is NULL/empty, the zero distortion
     *                               coefficients are assumed
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Input/Output rotation vector (see
     *                               REF: Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system. Input values are used as an initial
     *                               solution
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Input/Output translation vector.
     *                               Input values are used as an initial solution
     * @param criteria_type          {@code criteria.type} component of Criteria when to stop the Levenberg-Marquard
     *                               iterative algorithm. The function refines the object pose given at least 3 object
     *                               points, their corresponding image projections, an initial solution for the rotation
     *                               and translation vector, as well as the camera intrinsic matrix and the distortion
     *                               coefficients. The function minimizes the projection error with respect to the
     *                               rotation and the translation vectors, according to a Levenberg-Marquardt iterative
     *                               minimization process
     * @param criteria_maxCount      {@code criteria.maxCount} component of Criteria when to stop the Levenberg-Marquard
     *                               iterative algorithm. The function refines the object pose given at least 3 object
     *                               points, their corresponding image projections, an initial solution for the rotation
     *                               and translation vector, as well as the camera intrinsic matrix and the distortion
     *                               coefficients. The function minimizes the projection error with respect to the
     *                               rotation and the translation vectors, according to a Levenberg-Marquardt iterative
     *                               minimization process
     * @param criteria_epsilon       {@code criteria.epsilon} component of Criteria when to stop the Levenberg-Marquard
     *                               iterative algorithm. The function refines the object pose given at least 3 object
     *                               points, their corresponding image projections, an initial solution for the rotation
     *                               and translation vector, as well as the camera intrinsic matrix and the distortion
     *                               coefficients. The function minimizes the projection error with respect to the
     *                               rotation and the translation vectors, according to a Levenberg-Marquardt iterative
     *                               minimization process
     */
    private static native void solvePnPRefineLM_0(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            int criteria_type,
            int criteria_maxCount,
            double criteria_epsilon);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPRefineLM(Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can also be passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is NULL/empty, the zero distortion
     *                               coefficients are assumed
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Input/Output rotation vector (see
     *                               REF: Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system. Input values are used as an initial
     *                               solution
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Input/Output translation vector.
     *                               Input values are used as an initial solution
     */
    private static native void solvePnPRefineLM_1(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPRefineVVS(Mat,Mat,Mat,Mat,Mat,Mat,TermCriteria,double)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can also be passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is NULL/empty, the zero distortion
     *                               coefficients are assumed
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Input/Output rotation vector (see
     *                               REF: Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system. Input values are used as an initial
     *                               solution
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Input/Output translation vector.
     *                               Input values are used as an initial solution
     * @param criteria_type          {@code criteria.type} component of Criteria when to stop the Levenberg-Marquard
     *                               iterative algorithm
     * @param criteria_maxCount      {@code criteria.maxCount} component of Criteria when to stop the Levenberg-Marquard
     *                               iterative algorithm
     * @param criteria_epsilon       {@code criteria.epsilon} component of Criteria when to stop the Levenberg-Marquard
     *                               iterative algorithm
     * @param VVSlambda              Gain for the virtual visual servoing control law, equivalent to the alpha gain in
     *                               the Damped Gauss-Newton formulation. The function refines the object pose given at
     *                               least 3 object points, their corresponding image projections, an initial solution
     *                               for the rotation and translation vector, as well as the camera intrinsic matrix and
     *                               the distortion coefficients. The function minimizes the projection error with
     *                               respect to the rotation and the translation vectors, using a virtual visual
     *                               servoing (VVS) scheme
     */
    private static native void solvePnPRefineVVS_0(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            int criteria_type,
            int criteria_maxCount,
            double criteria_epsilon,
            double VVSlambda);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPRefineVVS(Mat,Mat,Mat,Mat,Mat,Mat,TermCriteria)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can also be passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is NULL/empty, the zero distortion
     *                               coefficients are assumed
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Input/Output rotation vector (see
     *                               REF: Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system. Input values are used as an initial
     *                               solution
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Input/Output translation vector.
     *                               Input values are used as an initial solution
     * @param criteria_type          {@code criteria.type} component of Criteria when to stop the Levenberg-Marquard
     *                               iterative algorithm
     * @param criteria_maxCount      {@code criteria.maxCount} component of Criteria when to stop the Levenberg-Marquard
     *                               iterative algorithm
     * @param criteria_epsilon       {@code criteria.epsilon} component of Criteria when to stop the Levenberg-Marquard
     *                               iterative algorithm
     */
    private static native void solvePnPRefineVVS_1(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            int criteria_type,
            int criteria_maxCount,
            double criteria_epsilon);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPRefineVVS(Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can also be passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is NULL/empty, the zero distortion
     *                               coefficients are assumed
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Input/Output rotation vector (see
     *                               REF: Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system. Input values are used as an initial
     *                               solution
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Input/Output translation vector.
     *                               Input values are used as an initial solution
     */
    private static native void solvePnPRefineVVS_2(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPGeneric(Mat,Mat,Mat,Mat,List<Mat>,List<Mat>,boolean,int,Mat,Mat,Mat)}.
     *
     * @param objectPoints_nativeObj      native matrix or object handle for {@code objectPoints}; Array of object
     *                                    points in the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel,
     *                                    where N is the number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_nativeObj       native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                                    image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of
     *                                    points. vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj      native matrix or object handle for {@code cameraMatrix}; Input camera
     *                                    intrinsic matrix cameramatrix{A}
     * @param distCoeffs_nativeObj        native matrix or object handle for {@code distCoeffs}; Input vector of
     *                                    distortion coefficients distcoeffs. If the vector is NULL/empty, the zero
     *                                    distortion coefficients are assumed
     * @param rvecs_mat_nativeObj         native matrix or object handle for {@code rvecs_mat}; Vector of output
     *                                    rotation vectors (see REF: Rodrigues ) that, together with tvecs, brings
     *                                    points from the model coordinate system to the camera coordinate system
     * @param tvecs_mat_nativeObj         native matrix or object handle for {@code tvecs_mat}; Vector of output
     *                                    translation vectors
     * @param useExtrinsicGuess           Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the
     *                                    provided rvec and tvec values as initial approximations of the rotation and
     *                                    translation vectors, respectively, and further optimizes them
     * @param flags                       Method for solving a PnP problem: see REF: calib3d_solvePnP_flags
     * @param rvec_nativeObj              native matrix or object handle for {@code rvec}; Rotation vector used to
     *                                    initialize an iterative PnP refinement algorithm, when flag is REF:
     *                                    SOLVEPNP_ITERATIVE and useExtrinsicGuess is set to true
     * @param tvec_nativeObj              native matrix or object handle for {@code tvec}; Translation vector used to
     *                                    initialize an iterative PnP refinement algorithm, when flag is REF:
     *                                    SOLVEPNP_ITERATIVE and useExtrinsicGuess is set to true
     * @param reprojectionError_nativeObj native matrix or object handle for {@code reprojectionError}; output
     *                                    per-solution reprojection errors
     * @return the number of returned pose solutions
     */
    private static native int solvePnPGeneric_0(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvecs_mat_nativeObj,
            long tvecs_mat_nativeObj,
            boolean useExtrinsicGuess,
            int flags,
            long rvec_nativeObj,
            long tvec_nativeObj,
            long reprojectionError_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPGeneric(Mat,Mat,Mat,Mat,List<Mat>,List<Mat>,boolean,int,Mat,Mat)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is NULL/empty, the zero distortion
     *                               coefficients are assumed
     * @param rvecs_mat_nativeObj    native matrix or object handle for {@code rvecs_mat}; Vector of output rotation
     *                               vectors (see REF: Rodrigues ) that, together with tvecs, brings points from the
     *                               model coordinate system to the camera coordinate system
     * @param tvecs_mat_nativeObj    native matrix or object handle for {@code tvecs_mat}; Vector of output translation
     *                               vectors
     * @param useExtrinsicGuess      Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                               rvec and tvec values as initial approximations of the rotation and translation
     *                               vectors, respectively, and further optimizes them
     * @param flags                  Method for solving a PnP problem: see REF: calib3d_solvePnP_flags
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Rotation vector used to initialize
     *                               an iterative PnP refinement algorithm, when flag is REF: SOLVEPNP_ITERATIVE and
     *                               useExtrinsicGuess is set to true
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Translation vector used to
     *                               initialize an iterative PnP refinement algorithm, when flag is REF:
     *                               SOLVEPNP_ITERATIVE and useExtrinsicGuess is set to true
     * @return the number of returned pose solutions
     */
    private static native int solvePnPGeneric_1(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvecs_mat_nativeObj,
            long tvecs_mat_nativeObj,
            boolean useExtrinsicGuess,
            int flags,
            long rvec_nativeObj,
            long tvec_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPGeneric(Mat,Mat,Mat,Mat,List<Mat>,List<Mat>,boolean,int,Mat)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is NULL/empty, the zero distortion
     *                               coefficients are assumed
     * @param rvecs_mat_nativeObj    native matrix or object handle for {@code rvecs_mat}; Vector of output rotation
     *                               vectors (see REF: Rodrigues ) that, together with tvecs, brings points from the
     *                               model coordinate system to the camera coordinate system
     * @param tvecs_mat_nativeObj    native matrix or object handle for {@code tvecs_mat}; Vector of output translation
     *                               vectors
     * @param useExtrinsicGuess      Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                               rvec and tvec values as initial approximations of the rotation and translation
     *                               vectors, respectively, and further optimizes them
     * @param flags                  Method for solving a PnP problem: see REF: calib3d_solvePnP_flags
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Rotation vector used to initialize
     *                               an iterative PnP refinement algorithm, when flag is REF: SOLVEPNP_ITERATIVE and
     *                               useExtrinsicGuess is set to true
     * @return the number of returned pose solutions
     */
    private static native int solvePnPGeneric_2(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvecs_mat_nativeObj,
            long tvecs_mat_nativeObj,
            boolean useExtrinsicGuess,
            int flags,
            long rvec_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPGeneric(Mat,Mat,Mat,Mat,List<Mat>,List<Mat>,boolean,int)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is NULL/empty, the zero distortion
     *                               coefficients are assumed
     * @param rvecs_mat_nativeObj    native matrix or object handle for {@code rvecs_mat}; Vector of output rotation
     *                               vectors (see REF: Rodrigues ) that, together with tvecs, brings points from the
     *                               model coordinate system to the camera coordinate system
     * @param tvecs_mat_nativeObj    native matrix or object handle for {@code tvecs_mat}; Vector of output translation
     *                               vectors
     * @param useExtrinsicGuess      Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                               rvec and tvec values as initial approximations of the rotation and translation
     *                               vectors, respectively, and further optimizes them
     * @param flags                  Method for solving a PnP problem: see REF: calib3d_solvePnP_flags
     * @return the number of returned pose solutions
     */
    private static native int solvePnPGeneric_3(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvecs_mat_nativeObj,
            long tvecs_mat_nativeObj,
            boolean useExtrinsicGuess,
            int flags);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPGeneric(Mat,Mat,Mat,Mat,List<Mat>,List<Mat>,boolean)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is NULL/empty, the zero distortion
     *                               coefficients are assumed
     * @param rvecs_mat_nativeObj    native matrix or object handle for {@code rvecs_mat}; Vector of output rotation
     *                               vectors (see REF: Rodrigues ) that, together with tvecs, brings points from the
     *                               model coordinate system to the camera coordinate system
     * @param tvecs_mat_nativeObj    native matrix or object handle for {@code tvecs_mat}; Vector of output translation
     *                               vectors
     * @param useExtrinsicGuess      Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                               rvec and tvec values as initial approximations of the rotation and translation
     *                               vectors, respectively, and further optimizes them
     * @return the number of returned pose solutions
     */
    private static native int solvePnPGeneric_4(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvecs_mat_nativeObj,
            long tvecs_mat_nativeObj,
            boolean useExtrinsicGuess);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#solvePnPGeneric(Mat,Mat,Mat,Mat,List<Mat>,List<Mat>)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is NULL/empty, the zero distortion
     *                               coefficients are assumed
     * @param rvecs_mat_nativeObj    native matrix or object handle for {@code rvecs_mat}; Vector of output rotation
     *                               vectors (see REF: Rodrigues ) that, together with tvecs, brings points from the
     *                               model coordinate system to the camera coordinate system
     * @param tvecs_mat_nativeObj    native matrix or object handle for {@code tvecs_mat}; Vector of output translation
     *                               vectors
     * @return the number of returned pose solutions
     */
    private static native int solvePnPGeneric_5(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvecs_mat_nativeObj,
            long tvecs_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#convertPointsToHomogeneous(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Input vector of N-dimensional points
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Output vector of N+1-dimensional points
     * @param dtype         The desired output array depth (either CV_32F or CV_64F are currently supported). If it's
     *                      -1, then it's set automatically to CV_32F or CV_64F, depending on the input depth. The
     *                      function converts points from Euclidean to homogeneous space by appending 1's to the tuple
     *                      of point coordinates. That is, each point (x1, x2, ..., xn) is converted to (x1, x2, ...,
     *                      xn, 1)
     */
    private static native void convertPointsToHomogeneous_0(long src_nativeObj, long dst_nativeObj, int dtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#convertPointsToHomogeneous(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Input vector of N-dimensional points
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Output vector of N+1-dimensional points
     */
    private static native void convertPointsToHomogeneous_1(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#convertPointsFromHomogeneous(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Input vector of N-dimensional points
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Output vector of N-1-dimensional points
     * @param dtype         The desired output array depth (either CV_32F or CV_64F are currently supported). If it's
     *                      -1, then it's set automatically to CV_32F or CV_64F, depending on the input depth. The
     *                      function converts points homogeneous to Euclidean space using perspective projection. That
     *                      is, each point (x1, x2, ... x(n-1), xn) is converted to (x1/xn, x2/xn, ..., x(n-1)/xn). When
     *                      xn=0, the output point coordinates will be (0,0,0,...)
     */
    private static native void convertPointsFromHomogeneous_0(long src_nativeObj, long dst_nativeObj, int dtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#convertPointsFromHomogeneous(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Input vector of N-dimensional points
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Output vector of N-1-dimensional points
     */
    private static native void convertPointsFromHomogeneous_1(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findFundamentalMat(MatOfPoint2f,MatOfPoint2f,int,double,double,int,Mat)}.
     *
     * @param points1_mat_nativeObj native matrix or object handle for {@code points1_mat}; Array of N points from the
     *                              first image. The point coordinates should be floating-point (single or double
     *                              precision)
     * @param points2_mat_nativeObj native matrix or object handle for {@code points2_mat}; Array of the second image
     *                              points of the same size and format as points1
     * @param method                Method for computing a fundamental matrix. REF: FM_7POINT for a 7-point algorithm. N
     *                              = 7 REF: FM_8POINT for an 8-point algorithm. N ge 8 REF: FM_RANSAC for the RANSAC
     *                              algorithm. N ge 8 REF: FM_LMEDS for the LMedS algorithm. N ge 8
     * @param ransacReprojThreshold Parameter used only for RANSAC. It is the maximum distance from a point to an
     *                              epipolar line in pixels, beyond which the point is considered an outlier and is not
     *                              used for computing the final fundamental matrix. It can be set to something like
     *                              1-3, depending on the accuracy of the point localization, image resolution, and the
     *                              image noise
     * @param confidence            Parameter used for the RANSAC and LMedS methods only. It specifies a desirable level
     *                              of confidence (probability) that the estimated matrix is correct
     * @param maxIters              The maximum number of robust method iterations. The epipolar geometry is described
     *                              by the following equation: [p_2; 1]^T F [p_1; 1] = 0 where F is a fundamental
     *                              matrix, p_1 and p_2 are corresponding points in the first and the second images,
     *                              respectively. The function calculates the fundamental matrix using one of four
     *                              methods listed above and returns the found fundamental matrix. Normally just one
     *                              matrix is found. But in case of the 7-point algorithm, the function may return up to
     *                              3 solutions ( 9 times 3 matrix that stores all 3 matrices sequentially). The
     *                              calculated fundamental matrix may be passed further to computeCorrespondEpilines
     *                              that finds the epipolar lines corresponding to the specified points. It can also be
     *                              passed to stereoRectifyUncalibrated to compute the rectification transformation. :
     *                              // Example. Estimation of fundamental matrix using the RANSAC algorithm int
     *                              point_count = 100; vector&lt;Point2f&gt; points1(point_count); vector&lt;Point2f&gt;
     *                              points2(point_count); // initialize the points here ... for( int i = 0; i &lt;
     *                              point_count; i++ ) { points1[i] = ...; points2[i] = ...; } Mat fundamental_matrix =
     *                              findFundamentalMat(points1, points2, FM_RANSAC, 3, 0.99);
     * @param mask_nativeObj        native matrix or object handle for {@code mask}; optional output mask
     * @return native binding handle for the estimated fundamental matrix or candidate matrices, according to method
     */
    private static native long findFundamentalMat_0(
            long points1_mat_nativeObj,
            long points2_mat_nativeObj,
            int method,
            double ransacReprojThreshold,
            double confidence,
            int maxIters,
            long mask_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findFundamentalMat(MatOfPoint2f,MatOfPoint2f,int,double,double,int)}.
     *
     * @param points1_mat_nativeObj native matrix or object handle for {@code points1_mat}; Array of N points from the
     *                              first image. The point coordinates should be floating-point (single or double
     *                              precision)
     * @param points2_mat_nativeObj native matrix or object handle for {@code points2_mat}; Array of the second image
     *                              points of the same size and format as points1
     * @param method                Method for computing a fundamental matrix. REF: FM_7POINT for a 7-point algorithm. N
     *                              = 7 REF: FM_8POINT for an 8-point algorithm. N ge 8 REF: FM_RANSAC for the RANSAC
     *                              algorithm. N ge 8 REF: FM_LMEDS for the LMedS algorithm. N ge 8
     * @param ransacReprojThreshold Parameter used only for RANSAC. It is the maximum distance from a point to an
     *                              epipolar line in pixels, beyond which the point is considered an outlier and is not
     *                              used for computing the final fundamental matrix. It can be set to something like
     *                              1-3, depending on the accuracy of the point localization, image resolution, and the
     *                              image noise
     * @param confidence            Parameter used for the RANSAC and LMedS methods only. It specifies a desirable level
     *                              of confidence (probability) that the estimated matrix is correct
     * @param maxIters              The maximum number of robust method iterations. The epipolar geometry is described
     *                              by the following equation: [p_2; 1]^T F [p_1; 1] = 0 where F is a fundamental
     *                              matrix, p_1 and p_2 are corresponding points in the first and the second images,
     *                              respectively. The function calculates the fundamental matrix using one of four
     *                              methods listed above and returns the found fundamental matrix. Normally just one
     *                              matrix is found. But in case of the 7-point algorithm, the function may return up to
     *                              3 solutions ( 9 times 3 matrix that stores all 3 matrices sequentially). The
     *                              calculated fundamental matrix may be passed further to computeCorrespondEpilines
     *                              that finds the epipolar lines corresponding to the specified points. It can also be
     *                              passed to stereoRectifyUncalibrated to compute the rectification transformation. :
     *                              // Example. Estimation of fundamental matrix using the RANSAC algorithm int
     *                              point_count = 100; vector&lt;Point2f&gt; points1(point_count); vector&lt;Point2f&gt;
     *                              points2(point_count); // initialize the points here ... for( int i = 0; i &lt;
     *                              point_count; i++ ) { points1[i] = ...; points2[i] = ...; } Mat fundamental_matrix =
     *                              findFundamentalMat(points1, points2, FM_RANSAC, 3, 0.99);
     * @return native binding handle for the estimated fundamental matrix or candidate matrices, according to method
     */
    private static native long findFundamentalMat_1(
            long points1_mat_nativeObj,
            long points2_mat_nativeObj,
            int method,
            double ransacReprojThreshold,
            double confidence,
            int maxIters);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findFundamentalMat(MatOfPoint2f,MatOfPoint2f,int,double,double,Mat)}.
     *
     * @param points1_mat_nativeObj native matrix or object handle for {@code points1_mat}; Array of N points from the
     *                              first image. The point coordinates should be floating-point (single or double
     *                              precision)
     * @param points2_mat_nativeObj native matrix or object handle for {@code points2_mat}; Array of the second image
     *                              points of the same size and format as points1
     * @param method                Method for computing a fundamental matrix. REF: FM_7POINT for a 7-point algorithm. N
     *                              = 7 REF: FM_8POINT for an 8-point algorithm. N ge 8 REF: FM_RANSAC for the RANSAC
     *                              algorithm. N ge 8 REF: FM_LMEDS for the LMedS algorithm. N ge 8
     * @param ransacReprojThreshold Parameter used only for RANSAC. It is the maximum distance from a point to an
     *                              epipolar line in pixels, beyond which the point is considered an outlier and is not
     *                              used for computing the final fundamental matrix. It can be set to something like
     *                              1-3, depending on the accuracy of the point localization, image resolution, and the
     *                              image noise
     * @param confidence            Parameter used for the RANSAC and LMedS methods only. It specifies a desirable level
     *                              of confidence (probability) that the estimated matrix is correct
     * @param mask_nativeObj        native matrix or object handle for {@code mask}; optional output mask
     * @return native binding handle for the estimated fundamental matrix or candidate matrices, according to method
     */
    private static native long findFundamentalMat_2(
            long points1_mat_nativeObj,
            long points2_mat_nativeObj,
            int method,
            double ransacReprojThreshold,
            double confidence,
            long mask_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findFundamentalMat(MatOfPoint2f,MatOfPoint2f,int,double,double)}.
     *
     * @param points1_mat_nativeObj native matrix or object handle for {@code points1_mat}; Array of N points from the
     *                              first image. The point coordinates should be floating-point (single or double
     *                              precision)
     * @param points2_mat_nativeObj native matrix or object handle for {@code points2_mat}; Array of the second image
     *                              points of the same size and format as points1
     * @param method                Method for computing a fundamental matrix. REF: FM_7POINT for a 7-point algorithm. N
     *                              = 7 REF: FM_8POINT for an 8-point algorithm. N ge 8 REF: FM_RANSAC for the RANSAC
     *                              algorithm. N ge 8 REF: FM_LMEDS for the LMedS algorithm. N ge 8
     * @param ransacReprojThreshold Parameter used only for RANSAC. It is the maximum distance from a point to an
     *                              epipolar line in pixels, beyond which the point is considered an outlier and is not
     *                              used for computing the final fundamental matrix. It can be set to something like
     *                              1-3, depending on the accuracy of the point localization, image resolution, and the
     *                              image noise
     * @param confidence            Parameter used for the RANSAC and LMedS methods only. It specifies a desirable level
     *                              of confidence (probability) that the estimated matrix is correct
     * @return native binding handle for the estimated fundamental matrix or candidate matrices, according to method
     */
    private static native long findFundamentalMat_3(
            long points1_mat_nativeObj,
            long points2_mat_nativeObj,
            int method,
            double ransacReprojThreshold,
            double confidence);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findFundamentalMat(MatOfPoint2f,MatOfPoint2f,int,double)}.
     *
     * @param points1_mat_nativeObj native matrix or object handle for {@code points1_mat}; Array of N points from the
     *                              first image. The point coordinates should be floating-point (single or double
     *                              precision)
     * @param points2_mat_nativeObj native matrix or object handle for {@code points2_mat}; Array of the second image
     *                              points of the same size and format as points1
     * @param method                Method for computing a fundamental matrix. REF: FM_7POINT for a 7-point algorithm. N
     *                              = 7 REF: FM_8POINT for an 8-point algorithm. N ge 8 REF: FM_RANSAC for the RANSAC
     *                              algorithm. N ge 8 REF: FM_LMEDS for the LMedS algorithm. N ge 8
     * @param ransacReprojThreshold Parameter used only for RANSAC. It is the maximum distance from a point to an
     *                              epipolar line in pixels, beyond which the point is considered an outlier and is not
     *                              used for computing the final fundamental matrix. It can be set to something like
     *                              1-3, depending on the accuracy of the point localization, image resolution, and the
     *                              image noise
     * @return native binding handle for the estimated fundamental matrix or candidate matrices, according to method
     */
    private static native long findFundamentalMat_4(
            long points1_mat_nativeObj,
            long points2_mat_nativeObj,
            int method,
            double ransacReprojThreshold);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findFundamentalMat(MatOfPoint2f,MatOfPoint2f,int)}.
     *
     * @param points1_mat_nativeObj native matrix or object handle for {@code points1_mat}; Array of N points from the
     *                              first image. The point coordinates should be floating-point (single or double
     *                              precision)
     * @param points2_mat_nativeObj native matrix or object handle for {@code points2_mat}; Array of the second image
     *                              points of the same size and format as points1
     * @param method                Method for computing a fundamental matrix. REF: FM_7POINT for a 7-point algorithm. N
     *                              = 7 REF: FM_8POINT for an 8-point algorithm. N ge 8 REF: FM_RANSAC for the RANSAC
     *                              algorithm. N ge 8 REF: FM_LMEDS for the LMedS algorithm. N ge 8
     * @return native binding handle for the estimated fundamental matrix or candidate matrices, according to method
     */
    private static native long findFundamentalMat_5(long points1_mat_nativeObj, long points2_mat_nativeObj, int method);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findFundamentalMat(MatOfPoint2f,MatOfPoint2f)}.
     *
     * @param points1_mat_nativeObj native matrix or object handle for {@code points1_mat}; Array of N points from the
     *                              first image. The point coordinates should be floating-point (single or double
     *                              precision)
     * @param points2_mat_nativeObj native matrix or object handle for {@code points2_mat}; Array of the second image
     *                              points of the same size and format as points1
     * @return native binding handle for the estimated fundamental matrix or candidate matrices, according to method
     */
    private static native long findFundamentalMat_6(long points1_mat_nativeObj, long points2_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findFundamentalMat(MatOfPoint2f,MatOfPoint2f,Mat,UsacParams)}.
     *
     * @param points1_mat_nativeObj native matrix or object handle for {@code points1_mat}; Array of N points from the
     *                              first image. The point coordinates should be floating-point (single or double
     *                              precision)
     * @param points2_mat_nativeObj native matrix or object handle for {@code points2_mat}; Array of the second image
     *                              points of the same size and format as points1
     * @param mask_nativeObj        native matrix or object handle for {@code mask}; optional output mask
     * @param params_nativeObj      {@code params.getNativeObjAddr()} component of robust-estimation sampling, scoring,
     *                              threshold and refinement settings
     * @return native binding handle for the estimated fundamental matrix or candidate matrices, according to method
     */
    private static native long findFundamentalMat_7(
            long points1_mat_nativeObj,
            long points2_mat_nativeObj,
            long mask_nativeObj,
            long params_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,Mat,int,double,double,int,Mat)}.
     *
     * @param points1_nativeObj      native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D
     *                               points from the first image. The point coordinates should be floating-point (single
     *                               or double precision)
     * @param points2_nativeObj      native matrix or object handle for {@code points2}; Array of the second image
     *                               points of the same size and format as points1
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Camera intrinsic matrix
     *                               cameramatrix{A} . Note that this function assumes that points1 and points2 are
     *                               feature points from cameras with the same camera intrinsic matrix. If this
     *                               assumption does not hold for your use case, use another function overload or
     *                               undistortPoints with for both cameras to transform image points to normalized image
     *                               coordinates, which are valid for the identity camera intrinsic matrix. When passing
     *                               these coordinates, pass the identity matrix for this parameter
     * @param method                 Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm.
     *                               REF: LMEDS for the LMedS algorithm
     * @param prob                   Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level
     *                               of confidence (probability) that the estimated matrix is correct
     * @param threshold              Parameter used for RANSAC. It is the maximum distance from a point to an epipolar
     *                               line in pixels, beyond which the point is considered an outlier and is not used for
     *                               computing the final fundamental matrix. It can be set to something like 1-3,
     *                               depending on the accuracy of the point localization, image resolution, and the
     *                               image noise
     * @param maxIters               The maximum number of robust method iterations. This function estimates essential
     *                               matrix based on the five-point algorithm solver in is also a related. The epipolar
     *                               geometry is described by the following equation: [p_2; 1]^T K^{-T} E K^{-1} [p_1;
     *                               1] = 0 where E is an essential matrix, p_1 and p_2 are corresponding points in the
     *                               first and the second images, respectively. The result of this function may be
     *                               passed further to decomposeEssentialMat or recoverPose to recover the relative pose
     *                               between cameras
     * @param mask_nativeObj         native matrix or object handle for {@code mask}; Output array of N elements, every
     *                               element of which is set to 0 for outliers and to 1 for the other points. The array
     *                               is computed only in the RANSAC and LMedS methods. This function estimates essential
     *                               matrix based on the five-point algorithm solver in is also a related. The epipolar
     *                               geometry is described by the following equation: [p_2; 1]^T K^{-T} E K^{-1} [p_1;
     *                               1] = 0 where E is an essential matrix, p_1 and p_2 are corresponding points in the
     *                               first and the second images, respectively. The result of this function may be
     *                               passed further to decomposeEssentialMat or recoverPose to recover the relative pose
     *                               between cameras
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_0(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix_nativeObj,
            int method,
            double prob,
            double threshold,
            int maxIters,
            long mask_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,Mat,int,double,double,int)}.
     *
     * @param points1_nativeObj      native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D
     *                               points from the first image. The point coordinates should be floating-point (single
     *                               or double precision)
     * @param points2_nativeObj      native matrix or object handle for {@code points2}; Array of the second image
     *                               points of the same size and format as points1
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Camera intrinsic matrix
     *                               cameramatrix{A} . Note that this function assumes that points1 and points2 are
     *                               feature points from cameras with the same camera intrinsic matrix. If this
     *                               assumption does not hold for your use case, use another function overload or
     *                               undistortPoints with for both cameras to transform image points to normalized image
     *                               coordinates, which are valid for the identity camera intrinsic matrix. When passing
     *                               these coordinates, pass the identity matrix for this parameter
     * @param method                 Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm.
     *                               REF: LMEDS for the LMedS algorithm
     * @param prob                   Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level
     *                               of confidence (probability) that the estimated matrix is correct
     * @param threshold              Parameter used for RANSAC. It is the maximum distance from a point to an epipolar
     *                               line in pixels, beyond which the point is considered an outlier and is not used for
     *                               computing the final fundamental matrix. It can be set to something like 1-3,
     *                               depending on the accuracy of the point localization, image resolution, and the
     *                               image noise
     * @param maxIters               The maximum number of robust method iterations. This function estimates essential
     *                               matrix based on the five-point algorithm solver in is also a related. The epipolar
     *                               geometry is described by the following equation: [p_2; 1]^T K^{-T} E K^{-1} [p_1;
     *                               1] = 0 where E is an essential matrix, p_1 and p_2 are corresponding points in the
     *                               first and the second images, respectively. The result of this function may be
     *                               passed further to decomposeEssentialMat or recoverPose to recover the relative pose
     *                               between cameras
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_1(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix_nativeObj,
            int method,
            double prob,
            double threshold,
            int maxIters);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,Mat,int,double,double)}.
     *
     * @param points1_nativeObj      native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D
     *                               points from the first image. The point coordinates should be floating-point (single
     *                               or double precision)
     * @param points2_nativeObj      native matrix or object handle for {@code points2}; Array of the second image
     *                               points of the same size and format as points1
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Camera intrinsic matrix
     *                               cameramatrix{A} . Note that this function assumes that points1 and points2 are
     *                               feature points from cameras with the same camera intrinsic matrix. If this
     *                               assumption does not hold for your use case, use another function overload or
     *                               undistortPoints with for both cameras to transform image points to normalized image
     *                               coordinates, which are valid for the identity camera intrinsic matrix. When passing
     *                               these coordinates, pass the identity matrix for this parameter
     * @param method                 Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm.
     *                               REF: LMEDS for the LMedS algorithm
     * @param prob                   Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level
     *                               of confidence (probability) that the estimated matrix is correct
     * @param threshold              Parameter used for RANSAC. It is the maximum distance from a point to an epipolar
     *                               line in pixels, beyond which the point is considered an outlier and is not used for
     *                               computing the final fundamental matrix. It can be set to something like 1-3,
     *                               depending on the accuracy of the point localization, image resolution, and the
     *                               image noise
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_2(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix_nativeObj,
            int method,
            double prob,
            double threshold);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,Mat,int,double)}.
     *
     * @param points1_nativeObj      native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D
     *                               points from the first image. The point coordinates should be floating-point (single
     *                               or double precision)
     * @param points2_nativeObj      native matrix or object handle for {@code points2}; Array of the second image
     *                               points of the same size and format as points1
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Camera intrinsic matrix
     *                               cameramatrix{A} . Note that this function assumes that points1 and points2 are
     *                               feature points from cameras with the same camera intrinsic matrix. If this
     *                               assumption does not hold for your use case, use another function overload or
     *                               undistortPoints with for both cameras to transform image points to normalized image
     *                               coordinates, which are valid for the identity camera intrinsic matrix. When passing
     *                               these coordinates, pass the identity matrix for this parameter
     * @param method                 Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm.
     *                               REF: LMEDS for the LMedS algorithm
     * @param prob                   Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level
     *                               of confidence (probability) that the estimated matrix is correct
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_3(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix_nativeObj,
            int method,
            double prob);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,Mat,int)}.
     *
     * @param points1_nativeObj      native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D
     *                               points from the first image. The point coordinates should be floating-point (single
     *                               or double precision)
     * @param points2_nativeObj      native matrix or object handle for {@code points2}; Array of the second image
     *                               points of the same size and format as points1
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Camera intrinsic matrix
     *                               cameramatrix{A} . Note that this function assumes that points1 and points2 are
     *                               feature points from cameras with the same camera intrinsic matrix. If this
     *                               assumption does not hold for your use case, use another function overload or
     *                               undistortPoints with for both cameras to transform image points to normalized image
     *                               coordinates, which are valid for the identity camera intrinsic matrix. When passing
     *                               these coordinates, pass the identity matrix for this parameter
     * @param method                 Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm.
     *                               REF: LMEDS for the LMedS algorithm
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_4(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix_nativeObj,
            int method);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,Mat)}.
     *
     * @param points1_nativeObj      native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D
     *                               points from the first image. The point coordinates should be floating-point (single
     *                               or double precision)
     * @param points2_nativeObj      native matrix or object handle for {@code points2}; Array of the second image
     *                               points of the same size and format as points1
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Camera intrinsic matrix
     *                               cameramatrix{A} . Note that this function assumes that points1 and points2 are
     *                               feature points from cameras with the same camera intrinsic matrix. If this
     *                               assumption does not hold for your use case, use another function overload or
     *                               undistortPoints with for both cameras to transform image points to normalized image
     *                               coordinates, which are valid for the identity camera intrinsic matrix. When passing
     *                               these coordinates, pass the identity matrix for this parameter. REF: RANSAC for the
     *                               RANSAC algorithm. REF: LMEDS for the LMedS algorithm. confidence (probability) that
     *                               the estimated matrix is correct. line in pixels, beyond which the point is
     *                               considered an outlier and is not used for computing the final fundamental matrix.
     *                               It can be set to something like 1-3, depending on the accuracy of the point
     *                               localization, image resolution, and the image noise. for the other points. The
     *                               array is computed only in the RANSAC and LMedS methods. This function estimates
     *                               essential matrix based on the five-point algorithm solver in is also a related. The
     *                               epipolar geometry is described by the following equation: [p_2; 1]^T K^{-T} E
     *                               K^{-1} [p_1; 1] = 0 where E is an essential matrix, p_1 and p_2 are corresponding
     *                               points in the first and the second images, respectively. The result of this
     *                               function may be passed further to decomposeEssentialMat or recoverPose to recover
     *                               the relative pose between cameras
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_5(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,double,Point,int,double,double,int,Mat)}.
     *
     * @param points1_nativeObj native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D points
     *                          from the first image. The point coordinates should be floating-point (single or double
     *                          precision)
     * @param points2_nativeObj native matrix or object handle for {@code points2}; Array of the second image points of
     *                          the same size and format as points1
     * @param focal             focal length of the camera. Note that this function assumes that points1 and points2 are
     *                          feature points from cameras with same focal length and principal point
     * @param pp_x              {@code pp.x} component of principal point of the camera
     * @param pp_y              {@code pp.y} component of principal point of the camera
     * @param method            Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF:
     *                          LMEDS for the LMedS algorithm
     * @param prob              Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                          confidence (probability) that the estimated matrix is correct
     * @param threshold         Parameter used for RANSAC. It is the maximum distance from a point to an epipolar line
     *                          in pixels, beyond which the point is considered an outlier and is not used for computing
     *                          the final fundamental matrix. It can be set to something like 1-3, depending on the
     *                          accuracy of the point localization, image resolution, and the image noise
     * @param maxIters          The maximum number of robust method iterations. This function differs from the one above
     *                          that it computes camera intrinsic matrix from focal length and principal point: A = f
     *                          &amp; 0 &amp; x_{pp} \\ 0 &amp; f &amp; y_{pp} \\ 0 &amp; 0 &amp; 1
     * @param mask_nativeObj    native matrix or object handle for {@code mask}; Output array of N elements, every
     *                          element of which is set to 0 for outliers and to 1 for the other points. The array is
     *                          computed only in the RANSAC and LMedS methods. This function estimates essential matrix
     *                          based on the five-point algorithm solver in is also a related. The epipolar geometry is
     *                          described by the following equation: [p_2; 1]^T K^{-T} E K^{-1} [p_1; 1] = 0 where E is
     *                          an essential matrix, p_1 and p_2 are corresponding points in the first and the second
     *                          images, respectively. The result of this function may be passed further to
     *                          decomposeEssentialMat or recoverPose to recover the relative pose between cameras
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_6(
            long points1_nativeObj,
            long points2_nativeObj,
            double focal,
            double pp_x,
            double pp_y,
            int method,
            double prob,
            double threshold,
            int maxIters,
            long mask_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,double,Point,int,double,double,int)}.
     *
     * @param points1_nativeObj native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D points
     *                          from the first image. The point coordinates should be floating-point (single or double
     *                          precision)
     * @param points2_nativeObj native matrix or object handle for {@code points2}; Array of the second image points of
     *                          the same size and format as points1
     * @param focal             focal length of the camera. Note that this function assumes that points1 and points2 are
     *                          feature points from cameras with same focal length and principal point
     * @param pp_x              {@code pp.x} component of principal point of the camera
     * @param pp_y              {@code pp.y} component of principal point of the camera
     * @param method            Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF:
     *                          LMEDS for the LMedS algorithm
     * @param prob              Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                          confidence (probability) that the estimated matrix is correct
     * @param threshold         Parameter used for RANSAC. It is the maximum distance from a point to an epipolar line
     *                          in pixels, beyond which the point is considered an outlier and is not used for computing
     *                          the final fundamental matrix. It can be set to something like 1-3, depending on the
     *                          accuracy of the point localization, image resolution, and the image noise
     * @param maxIters          The maximum number of robust method iterations. This function differs from the one above
     *                          that it computes camera intrinsic matrix from focal length and principal point: A = f
     *                          &amp; 0 &amp; x_{pp} \\ 0 &amp; f &amp; y_{pp} \\ 0 &amp; 0 &amp; 1
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_7(
            long points1_nativeObj,
            long points2_nativeObj,
            double focal,
            double pp_x,
            double pp_y,
            int method,
            double prob,
            double threshold,
            int maxIters);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,double,Point,int,double,double)}.
     *
     * @param points1_nativeObj native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D points
     *                          from the first image. The point coordinates should be floating-point (single or double
     *                          precision)
     * @param points2_nativeObj native matrix or object handle for {@code points2}; Array of the second image points of
     *                          the same size and format as points1
     * @param focal             focal length of the camera. Note that this function assumes that points1 and points2 are
     *                          feature points from cameras with same focal length and principal point
     * @param pp_x              {@code pp.x} component of principal point of the camera
     * @param pp_y              {@code pp.y} component of principal point of the camera
     * @param method            Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF:
     *                          LMEDS for the LMedS algorithm
     * @param prob              Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                          confidence (probability) that the estimated matrix is correct
     * @param threshold         Parameter used for RANSAC. It is the maximum distance from a point to an epipolar line
     *                          in pixels, beyond which the point is considered an outlier and is not used for computing
     *                          the final fundamental matrix. It can be set to something like 1-3, depending on the
     *                          accuracy of the point localization, image resolution, and the image noise
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_8(
            long points1_nativeObj,
            long points2_nativeObj,
            double focal,
            double pp_x,
            double pp_y,
            int method,
            double prob,
            double threshold);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,double,Point,int,double)}.
     *
     * @param points1_nativeObj native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D points
     *                          from the first image. The point coordinates should be floating-point (single or double
     *                          precision)
     * @param points2_nativeObj native matrix or object handle for {@code points2}; Array of the second image points of
     *                          the same size and format as points1
     * @param focal             focal length of the camera. Note that this function assumes that points1 and points2 are
     *                          feature points from cameras with same focal length and principal point
     * @param pp_x              {@code pp.x} component of principal point of the camera
     * @param pp_y              {@code pp.y} component of principal point of the camera
     * @param method            Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF:
     *                          LMEDS for the LMedS algorithm
     * @param prob              Parameter used for the RANSAC or LMedS methods only. It specifies a desirable level of
     *                          confidence (probability) that the estimated matrix is correct
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_9(
            long points1_nativeObj,
            long points2_nativeObj,
            double focal,
            double pp_x,
            double pp_y,
            int method,
            double prob);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,double,Point,int)}.
     *
     * @param points1_nativeObj native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D points
     *                          from the first image. The point coordinates should be floating-point (single or double
     *                          precision)
     * @param points2_nativeObj native matrix or object handle for {@code points2}; Array of the second image points of
     *                          the same size and format as points1
     * @param focal             focal length of the camera. Note that this function assumes that points1 and points2 are
     *                          feature points from cameras with same focal length and principal point
     * @param pp_x              {@code pp.x} component of principal point of the camera
     * @param pp_y              {@code pp.y} component of principal point of the camera
     * @param method            Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm. REF:
     *                          LMEDS for the LMedS algorithm
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_10(
            long points1_nativeObj,
            long points2_nativeObj,
            double focal,
            double pp_x,
            double pp_y,
            int method);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,double,Point)}.
     *
     * @param points1_nativeObj native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D points
     *                          from the first image. The point coordinates should be floating-point (single or double
     *                          precision)
     * @param points2_nativeObj native matrix or object handle for {@code points2}; Array of the second image points of
     *                          the same size and format as points1
     * @param focal             focal length of the camera. Note that this function assumes that points1 and points2 are
     *                          feature points from cameras with same focal length and principal point
     * @param pp_x              {@code pp.x} component of principal point of the camera. REF: RANSAC for the RANSAC
     *                          algorithm. REF: LMEDS for the LMedS algorithm. line in pixels, beyond which the point is
     *                          considered an outlier and is not used for computing the final fundamental matrix. It can
     *                          be set to something like 1-3, depending on the accuracy of the point localization, image
     *                          resolution, and the image noise. confidence (probability) that the estimated matrix is
     *                          correct. for the other points. The array is computed only in the RANSAC and LMedS
     *                          methods. This function differs from the one above that it computes camera intrinsic
     *                          matrix from focal length and principal point: A = f &amp; 0 &amp; x_{pp} \\ 0 &amp; f
     *                          &amp; y_{pp} \\ 0 &amp; 0 &amp; 1
     * @param pp_y              {@code pp.y} component of principal point of the camera. REF: RANSAC for the RANSAC
     *                          algorithm. REF: LMEDS for the LMedS algorithm. line in pixels, beyond which the point is
     *                          considered an outlier and is not used for computing the final fundamental matrix. It can
     *                          be set to something like 1-3, depending on the accuracy of the point localization, image
     *                          resolution, and the image noise. confidence (probability) that the estimated matrix is
     *                          correct. for the other points. The array is computed only in the RANSAC and LMedS
     *                          methods. This function differs from the one above that it computes camera intrinsic
     *                          matrix from focal length and principal point: A = f &amp; 0 &amp; x_{pp} \\ 0 &amp; f
     *                          &amp; y_{pp} \\ 0 &amp; 0 &amp; 1
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_11(
            long points1_nativeObj,
            long points2_nativeObj,
            double focal,
            double pp_x,
            double pp_y);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,double)}.
     *
     * @param points1_nativeObj native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D points
     *                          from the first image. The point coordinates should be floating-point (single or double
     *                          precision)
     * @param points2_nativeObj native matrix or object handle for {@code points2}; Array of the second image points of
     *                          the same size and format as points1
     * @param focal             focal length of the camera. Note that this function assumes that points1 and points2 are
     *                          feature points from cameras with same focal length and principal point. REF: RANSAC for
     *                          the RANSAC algorithm. REF: LMEDS for the LMedS algorithm. line in pixels, beyond which
     *                          the point is considered an outlier and is not used for computing the final fundamental
     *                          matrix. It can be set to something like 1-3, depending on the accuracy of the point
     *                          localization, image resolution, and the image noise. confidence (probability) that the
     *                          estimated matrix is correct. for the other points. The array is computed only in the
     *                          RANSAC and LMedS methods. This function differs from the one above that it computes
     *                          camera intrinsic matrix from focal length and principal point: A = f &amp; 0 &amp;
     *                          x_{pp} \\ 0 &amp; f &amp; y_{pp} \\ 0 &amp; 0 &amp; 1
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_12(long points1_nativeObj, long points2_nativeObj, double focal);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat)}.
     *
     * @param points1_nativeObj native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D points
     *                          from the first image. The point coordinates should be floating-point (single or double
     *                          precision)
     * @param points2_nativeObj native matrix or object handle for {@code points2}; Array of the second image points of
     *                          the same size and format as points1
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_13(long points1_nativeObj, long points2_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,Mat,Mat,Mat,Mat,int,double,double,Mat)}.
     *
     * @param points1_nativeObj       native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D
     *                                points from the first image. The point coordinates should be floating-point
     *                                (single or double precision)
     * @param points2_nativeObj       native matrix or object handle for {@code points2}; Array of the second image
     *                                points of the same size and format as points1
     * @param cameraMatrix1_nativeObj native matrix or object handle for {@code cameraMatrix1}; Camera matrix for the
     *                                first camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs1_nativeObj   native matrix or object handle for {@code distCoeffs1}; Input vector of distortion
     *                                coefficients for the first camera (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1,
     *                                s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is
     *                                NULL/empty, the zero distortion coefficients are assumed
     * @param cameraMatrix2_nativeObj native matrix or object handle for {@code cameraMatrix2}; Camera matrix for the
     *                                second camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs2_nativeObj   native matrix or object handle for {@code distCoeffs2}; Input vector of distortion
     *                                coefficients for the second camera (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[,
     *                                s_1, s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the
     *                                vector is NULL/empty, the zero distortion coefficients are assumed
     * @param method                  Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm.
     *                                REF: LMEDS for the LMedS algorithm
     * @param prob                    Parameter used for the RANSAC or LMedS methods only. It specifies a desirable
     *                                level of confidence (probability) that the estimated matrix is correct
     * @param threshold               Parameter used for RANSAC. It is the maximum distance from a point to an epipolar
     *                                line in pixels, beyond which the point is considered an outlier and is not used
     *                                for computing the final fundamental matrix. It can be set to something like 1-3,
     *                                depending on the accuracy of the point localization, image resolution, and the
     *                                image noise
     * @param mask_nativeObj          native matrix or object handle for {@code mask}; Output array of N elements, every
     *                                element of which is set to 0 for outliers and to 1 for the other points. The array
     *                                is computed only in the RANSAC and LMedS methods. This function estimates
     *                                essential matrix based on the five-point algorithm solver in is also a related.
     *                                The epipolar geometry is described by the following equation: [p_2; 1]^T K^{-T} E
     *                                K^{-1} [p_1; 1] = 0 where E is an essential matrix, p_1 and p_2 are corresponding
     *                                points in the first and the second images, respectively. The result of this
     *                                function may be passed further to decomposeEssentialMat or recoverPose to recover
     *                                the relative pose between cameras
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_14(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix1_nativeObj,
            long distCoeffs1_nativeObj,
            long cameraMatrix2_nativeObj,
            long distCoeffs2_nativeObj,
            int method,
            double prob,
            double threshold,
            long mask_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,Mat,Mat,Mat,Mat,int,double,double)}.
     *
     * @param points1_nativeObj       native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D
     *                                points from the first image. The point coordinates should be floating-point
     *                                (single or double precision)
     * @param points2_nativeObj       native matrix or object handle for {@code points2}; Array of the second image
     *                                points of the same size and format as points1
     * @param cameraMatrix1_nativeObj native matrix or object handle for {@code cameraMatrix1}; Camera matrix for the
     *                                first camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs1_nativeObj   native matrix or object handle for {@code distCoeffs1}; Input vector of distortion
     *                                coefficients for the first camera (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1,
     *                                s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is
     *                                NULL/empty, the zero distortion coefficients are assumed
     * @param cameraMatrix2_nativeObj native matrix or object handle for {@code cameraMatrix2}; Camera matrix for the
     *                                second camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs2_nativeObj   native matrix or object handle for {@code distCoeffs2}; Input vector of distortion
     *                                coefficients for the second camera (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[,
     *                                s_1, s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the
     *                                vector is NULL/empty, the zero distortion coefficients are assumed
     * @param method                  Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm.
     *                                REF: LMEDS for the LMedS algorithm
     * @param prob                    Parameter used for the RANSAC or LMedS methods only. It specifies a desirable
     *                                level of confidence (probability) that the estimated matrix is correct
     * @param threshold               Parameter used for RANSAC. It is the maximum distance from a point to an epipolar
     *                                line in pixels, beyond which the point is considered an outlier and is not used
     *                                for computing the final fundamental matrix. It can be set to something like 1-3,
     *                                depending on the accuracy of the point localization, image resolution, and the
     *                                image noise
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_15(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix1_nativeObj,
            long distCoeffs1_nativeObj,
            long cameraMatrix2_nativeObj,
            long distCoeffs2_nativeObj,
            int method,
            double prob,
            double threshold);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,Mat,Mat,Mat,Mat,int,double)}.
     *
     * @param points1_nativeObj       native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D
     *                                points from the first image. The point coordinates should be floating-point
     *                                (single or double precision)
     * @param points2_nativeObj       native matrix or object handle for {@code points2}; Array of the second image
     *                                points of the same size and format as points1
     * @param cameraMatrix1_nativeObj native matrix or object handle for {@code cameraMatrix1}; Camera matrix for the
     *                                first camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs1_nativeObj   native matrix or object handle for {@code distCoeffs1}; Input vector of distortion
     *                                coefficients for the first camera (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1,
     *                                s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is
     *                                NULL/empty, the zero distortion coefficients are assumed
     * @param cameraMatrix2_nativeObj native matrix or object handle for {@code cameraMatrix2}; Camera matrix for the
     *                                second camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs2_nativeObj   native matrix or object handle for {@code distCoeffs2}; Input vector of distortion
     *                                coefficients for the second camera (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[,
     *                                s_1, s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the
     *                                vector is NULL/empty, the zero distortion coefficients are assumed
     * @param method                  Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm.
     *                                REF: LMEDS for the LMedS algorithm
     * @param prob                    Parameter used for the RANSAC or LMedS methods only. It specifies a desirable
     *                                level of confidence (probability) that the estimated matrix is correct
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_16(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix1_nativeObj,
            long distCoeffs1_nativeObj,
            long cameraMatrix2_nativeObj,
            long distCoeffs2_nativeObj,
            int method,
            double prob);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,Mat,Mat,Mat,Mat,int)}.
     *
     * @param points1_nativeObj       native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D
     *                                points from the first image. The point coordinates should be floating-point
     *                                (single or double precision)
     * @param points2_nativeObj       native matrix or object handle for {@code points2}; Array of the second image
     *                                points of the same size and format as points1
     * @param cameraMatrix1_nativeObj native matrix or object handle for {@code cameraMatrix1}; Camera matrix for the
     *                                first camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs1_nativeObj   native matrix or object handle for {@code distCoeffs1}; Input vector of distortion
     *                                coefficients for the first camera (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1,
     *                                s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is
     *                                NULL/empty, the zero distortion coefficients are assumed
     * @param cameraMatrix2_nativeObj native matrix or object handle for {@code cameraMatrix2}; Camera matrix for the
     *                                second camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs2_nativeObj   native matrix or object handle for {@code distCoeffs2}; Input vector of distortion
     *                                coefficients for the second camera (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[,
     *                                s_1, s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the
     *                                vector is NULL/empty, the zero distortion coefficients are assumed
     * @param method                  Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm.
     *                                REF: LMEDS for the LMedS algorithm
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_17(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix1_nativeObj,
            long distCoeffs1_nativeObj,
            long cameraMatrix2_nativeObj,
            long distCoeffs2_nativeObj,
            int method);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param points1_nativeObj       native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D
     *                                points from the first image. The point coordinates should be floating-point
     *                                (single or double precision)
     * @param points2_nativeObj       native matrix or object handle for {@code points2}; Array of the second image
     *                                points of the same size and format as points1
     * @param cameraMatrix1_nativeObj native matrix or object handle for {@code cameraMatrix1}; Camera matrix for the
     *                                first camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs1_nativeObj   native matrix or object handle for {@code distCoeffs1}; Input vector of distortion
     *                                coefficients for the first camera (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1,
     *                                s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is
     *                                NULL/empty, the zero distortion coefficients are assumed
     * @param cameraMatrix2_nativeObj native matrix or object handle for {@code cameraMatrix2}; Camera matrix for the
     *                                second camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs2_nativeObj   native matrix or object handle for {@code distCoeffs2}; Input vector of distortion
     *                                coefficients for the second camera (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[,
     *                                s_1, s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the
     *                                vector is NULL/empty, the zero distortion coefficients are assumed
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_18(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix1_nativeObj,
            long distCoeffs1_nativeObj,
            long cameraMatrix2_nativeObj,
            long distCoeffs2_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#findEssentialMat(Mat,Mat,Mat,Mat,Mat,Mat,Mat,UsacParams)}.
     *
     * @param points1_nativeObj       native matrix or object handle for {@code points1}; Array of N (N &gt;= 5) 2D
     *                                points from the first image. The point coordinates should be floating-point
     *                                (single or double precision)
     * @param points2_nativeObj       native matrix or object handle for {@code points2}; Array of the second image
     *                                points of the same size and format as points1
     * @param cameraMatrix1_nativeObj native matrix or object handle for {@code cameraMatrix1}; Camera matrix for the
     *                                first camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param cameraMatrix2_nativeObj native matrix or object handle for {@code cameraMatrix2}; Camera matrix for the
     *                                second camera K = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param dist_coeff1_nativeObj   native matrix or object handle for {@code dist_coeff1}; distortion coefficients
     *                                for the first camera
     * @param dist_coeff2_nativeObj   native matrix or object handle for {@code dist_coeff2}; distortion coefficients
     *                                for the second camera
     * @param mask_nativeObj          native matrix or object handle for {@code mask}; Output array of N elements, every
     *                                element of which is set to 0 for outliers and to 1 for the other points. The array
     *                                is computed only in the RANSAC and LMedS methods. This function estimates
     *                                essential matrix based on the five-point algorithm solver in is also a related.
     *                                The epipolar geometry is described by the following equation: [p_2; 1]^T K^{-T} E
     *                                K^{-1} [p_1; 1] = 0 where E is an essential matrix, p_1 and p_2 are corresponding
     *                                points in the first and the second images, respectively. The result of this
     *                                function may be passed further to decomposeEssentialMat or recoverPose to recover
     *                                the relative pose between cameras
     * @param params_nativeObj        {@code params.getNativeObjAddr()} component of robust-estimation sampling,
     *                                scoring, threshold and refinement settings
     * @return native binding handle for the estimated essential matrix or stacked candidate essential matrices
     */
    private static native long findEssentialMat_19(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix1_nativeObj,
            long cameraMatrix2_nativeObj,
            long dist_coeff1_nativeObj,
            long dist_coeff2_nativeObj,
            long mask_nativeObj,
            long params_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#decomposeEssentialMat(Mat,Mat,Mat,Mat)}.
     *
     * @param E_nativeObj  native matrix or object handle for {@code E}; The input essential matrix
     * @param R1_nativeObj native matrix or object handle for {@code R1}; One possible rotation matrix
     * @param R2_nativeObj native matrix or object handle for {@code R2}; Another possible rotation matrix
     * @param t_nativeObj  native matrix or object handle for {@code t}; One possible translation. This function
     *                     decomposes the essential matrix E using svd decomposition In general, four possible poses
     *                     exist for the decomposition of E. They are [R_1, t], [R_1, -t], [R_2, t], [R_2, -t]. If E
     *                     gives the epipolar constraint [p_2; 1]^T A^{-T} E A^{-1} [p_1; 1] = 0 between the image
     *                     points p_1 in the first image and p_2 in second image, then any of the tuples [R_1, t], [R_1,
     *                     -t], [R_2, t], [R_2, -t] is a change of basis from the first camera's coordinate system to
     *                     the second camera's coordinate system. However, by decomposing E, one can only get the
     *                     direction of the translation. For this reason, the translation t is returned with unit length
     */
    private static native void decomposeEssentialMat_0(
            long E_nativeObj,
            long R1_nativeObj,
            long R2_nativeObj,
            long t_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#recoverPose(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,int,double,double,Mat)}.
     *
     * @param points1_nativeObj       native matrix or object handle for {@code points1}; Array of N 2D points from the
     *                                first image. The point coordinates should be floating-point (single or double
     *                                precision)
     * @param points2_nativeObj       native matrix or object handle for {@code points2}; Array of the second image
     *                                points of the same size and format as points1
     * @param cameraMatrix1_nativeObj native matrix or object handle for {@code cameraMatrix1}; intrinsic matrix of the
     *                                first camera
     * @param distCoeffs1_nativeObj   native matrix or object handle for {@code distCoeffs1}; Input/output vector of
     *                                distortion coefficients, the same as in REF: calibrateCamera
     * @param cameraMatrix2_nativeObj native matrix or object handle for {@code cameraMatrix2}; intrinsic matrix of the
     *                                second camera
     * @param distCoeffs2_nativeObj   native matrix or object handle for {@code distCoeffs2}; Input/output vector of
     *                                distortion coefficients, the same as in REF: calibrateCamera
     * @param E_nativeObj             native matrix or object handle for {@code E}; The output essential matrix
     * @param R_nativeObj             native matrix or object handle for {@code R}; Output rotation matrix. Together
     *                                with the translation vector, this matrix makes up a tuple that performs a change
     *                                of basis from the first camera's coordinate system to the second camera's
     *                                coordinate system. Note that, in general, t can not be used for this tuple, see
     *                                the parameter described below
     * @param t_nativeObj             native matrix or object handle for {@code t}; Output translation vector. This
     *                                vector is obtained by REF: decomposeEssentialMat and therefore is only known up to
     *                                scale, i.e. t is the direction of the translation vector and has unit length
     * @param method                  Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm.
     *                                REF: LMEDS for the LMedS algorithm
     * @param prob                    Parameter used for the RANSAC or LMedS methods only. It specifies a desirable
     *                                level of confidence (probability) that the estimated matrix is correct
     * @param threshold               Parameter used for RANSAC. It is the maximum distance from a point to an epipolar
     *                                line in pixels, beyond which the point is considered an outlier and is not used
     *                                for computing the final fundamental matrix. It can be set to something like 1-3,
     *                                depending on the accuracy of the point localization, image resolution, and the
     *                                image noise
     * @param mask_nativeObj          native matrix or object handle for {@code mask}; Input/output mask for inliers in
     *                                points1 and points2. If it is not empty, then it marks inliers in points1 and
     *                                points2 for the given essential matrix E. Only these inliers will be used to
     *                                recover pose. In the output mask only inliers which pass the chirality check. This
     *                                function decomposes an essential matrix using REF: decomposeEssentialMat and then
     *                                verifies possible pose hypotheses by doing chirality check. The chirality check
     *                                means that the triangulated 3D points should have positive depth. Some details can
     *                                be found in This function can be used to process the output E and mask from REF:
     *                                findEssentialMat. In this scenario, points1 and points2 are the same input for
     *                                findEssentialMat.: // Example. Estimation of fundamental matrix using the RANSAC
     *                                algorithm int point_count = 100; vector&lt;Point2f&gt; points1(point_count);
     *                                vector&lt;Point2f&gt; points2(point_count); // initialize the points here ... for(
     *                                int i = 0; i &lt; point_count; i++ ) { points1[i] = ...; points2[i] = ...; } //
     *                                Input: camera calibration of both cameras, for example using intrinsic chessboard
     *                                calibration. Mat cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2; //
     *                                Output: Essential matrix, relative rotation and relative translation. Mat E, R, t,
     *                                mask; recoverPose(points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2,
     *                                distCoeffs2, E, R, t, mask);
     * @return the number of inlier correspondences passing the pose cheirality check
     */
    private static native int recoverPose_0(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix1_nativeObj,
            long distCoeffs1_nativeObj,
            long cameraMatrix2_nativeObj,
            long distCoeffs2_nativeObj,
            long E_nativeObj,
            long R_nativeObj,
            long t_nativeObj,
            int method,
            double prob,
            double threshold,
            long mask_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#recoverPose(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,int,double,double)}.
     *
     * @param points1_nativeObj       native matrix or object handle for {@code points1}; Array of N 2D points from the
     *                                first image. The point coordinates should be floating-point (single or double
     *                                precision)
     * @param points2_nativeObj       native matrix or object handle for {@code points2}; Array of the second image
     *                                points of the same size and format as points1
     * @param cameraMatrix1_nativeObj native matrix or object handle for {@code cameraMatrix1}; intrinsic matrix of the
     *                                first camera
     * @param distCoeffs1_nativeObj   native matrix or object handle for {@code distCoeffs1}; Input/output vector of
     *                                distortion coefficients, the same as in REF: calibrateCamera
     * @param cameraMatrix2_nativeObj native matrix or object handle for {@code cameraMatrix2}; intrinsic matrix of the
     *                                second camera
     * @param distCoeffs2_nativeObj   native matrix or object handle for {@code distCoeffs2}; Input/output vector of
     *                                distortion coefficients, the same as in REF: calibrateCamera
     * @param E_nativeObj             native matrix or object handle for {@code E}; The output essential matrix
     * @param R_nativeObj             native matrix or object handle for {@code R}; Output rotation matrix. Together
     *                                with the translation vector, this matrix makes up a tuple that performs a change
     *                                of basis from the first camera's coordinate system to the second camera's
     *                                coordinate system. Note that, in general, t can not be used for this tuple, see
     *                                the parameter described below
     * @param t_nativeObj             native matrix or object handle for {@code t}; Output translation vector. This
     *                                vector is obtained by REF: decomposeEssentialMat and therefore is only known up to
     *                                scale, i.e. t is the direction of the translation vector and has unit length
     * @param method                  Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm.
     *                                REF: LMEDS for the LMedS algorithm
     * @param prob                    Parameter used for the RANSAC or LMedS methods only. It specifies a desirable
     *                                level of confidence (probability) that the estimated matrix is correct
     * @param threshold               Parameter used for RANSAC. It is the maximum distance from a point to an epipolar
     *                                line in pixels, beyond which the point is considered an outlier and is not used
     *                                for computing the final fundamental matrix. It can be set to something like 1-3,
     *                                depending on the accuracy of the point localization, image resolution, and the
     *                                image noise
     * @return the number of inlier correspondences passing the pose cheirality check
     */
    private static native int recoverPose_1(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix1_nativeObj,
            long distCoeffs1_nativeObj,
            long cameraMatrix2_nativeObj,
            long distCoeffs2_nativeObj,
            long E_nativeObj,
            long R_nativeObj,
            long t_nativeObj,
            int method,
            double prob,
            double threshold);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#recoverPose(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,int,double)}.
     *
     * @param points1_nativeObj       native matrix or object handle for {@code points1}; Array of N 2D points from the
     *                                first image. The point coordinates should be floating-point (single or double
     *                                precision)
     * @param points2_nativeObj       native matrix or object handle for {@code points2}; Array of the second image
     *                                points of the same size and format as points1
     * @param cameraMatrix1_nativeObj native matrix or object handle for {@code cameraMatrix1}; intrinsic matrix of the
     *                                first camera
     * @param distCoeffs1_nativeObj   native matrix or object handle for {@code distCoeffs1}; Input/output vector of
     *                                distortion coefficients, the same as in REF: calibrateCamera
     * @param cameraMatrix2_nativeObj native matrix or object handle for {@code cameraMatrix2}; intrinsic matrix of the
     *                                second camera
     * @param distCoeffs2_nativeObj   native matrix or object handle for {@code distCoeffs2}; Input/output vector of
     *                                distortion coefficients, the same as in REF: calibrateCamera
     * @param E_nativeObj             native matrix or object handle for {@code E}; The output essential matrix
     * @param R_nativeObj             native matrix or object handle for {@code R}; Output rotation matrix. Together
     *                                with the translation vector, this matrix makes up a tuple that performs a change
     *                                of basis from the first camera's coordinate system to the second camera's
     *                                coordinate system. Note that, in general, t can not be used for this tuple, see
     *                                the parameter described below
     * @param t_nativeObj             native matrix or object handle for {@code t}; Output translation vector. This
     *                                vector is obtained by REF: decomposeEssentialMat and therefore is only known up to
     *                                scale, i.e. t is the direction of the translation vector and has unit length
     * @param method                  Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm.
     *                                REF: LMEDS for the LMedS algorithm
     * @param prob                    Parameter used for the RANSAC or LMedS methods only. It specifies a desirable
     *                                level of confidence (probability) that the estimated matrix is correct
     * @return the number of inlier correspondences passing the pose cheirality check
     */
    private static native int recoverPose_2(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix1_nativeObj,
            long distCoeffs1_nativeObj,
            long cameraMatrix2_nativeObj,
            long distCoeffs2_nativeObj,
            long E_nativeObj,
            long R_nativeObj,
            long t_nativeObj,
            int method,
            double prob);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#recoverPose(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,int)}.
     *
     * @param points1_nativeObj       native matrix or object handle for {@code points1}; Array of N 2D points from the
     *                                first image. The point coordinates should be floating-point (single or double
     *                                precision)
     * @param points2_nativeObj       native matrix or object handle for {@code points2}; Array of the second image
     *                                points of the same size and format as points1
     * @param cameraMatrix1_nativeObj native matrix or object handle for {@code cameraMatrix1}; intrinsic matrix of the
     *                                first camera
     * @param distCoeffs1_nativeObj   native matrix or object handle for {@code distCoeffs1}; Input/output vector of
     *                                distortion coefficients, the same as in REF: calibrateCamera
     * @param cameraMatrix2_nativeObj native matrix or object handle for {@code cameraMatrix2}; intrinsic matrix of the
     *                                second camera
     * @param distCoeffs2_nativeObj   native matrix or object handle for {@code distCoeffs2}; Input/output vector of
     *                                distortion coefficients, the same as in REF: calibrateCamera
     * @param E_nativeObj             native matrix or object handle for {@code E}; The output essential matrix
     * @param R_nativeObj             native matrix or object handle for {@code R}; Output rotation matrix. Together
     *                                with the translation vector, this matrix makes up a tuple that performs a change
     *                                of basis from the first camera's coordinate system to the second camera's
     *                                coordinate system. Note that, in general, t can not be used for this tuple, see
     *                                the parameter described below
     * @param t_nativeObj             native matrix or object handle for {@code t}; Output translation vector. This
     *                                vector is obtained by REF: decomposeEssentialMat and therefore is only known up to
     *                                scale, i.e. t is the direction of the translation vector and has unit length
     * @param method                  Method for computing an essential matrix. REF: RANSAC for the RANSAC algorithm.
     *                                REF: LMEDS for the LMedS algorithm
     * @return the number of inlier correspondences passing the pose cheirality check
     */
    private static native int recoverPose_3(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix1_nativeObj,
            long distCoeffs1_nativeObj,
            long cameraMatrix2_nativeObj,
            long distCoeffs2_nativeObj,
            long E_nativeObj,
            long R_nativeObj,
            long t_nativeObj,
            int method);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#recoverPose(Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param points1_nativeObj       native matrix or object handle for {@code points1}; Array of N 2D points from the
     *                                first image. The point coordinates should be floating-point (single or double
     *                                precision)
     * @param points2_nativeObj       native matrix or object handle for {@code points2}; Array of the second image
     *                                points of the same size and format as points1
     * @param cameraMatrix1_nativeObj native matrix or object handle for {@code cameraMatrix1}; intrinsic matrix of the
     *                                first camera
     * @param distCoeffs1_nativeObj   native matrix or object handle for {@code distCoeffs1}; Input/output vector of
     *                                distortion coefficients, the same as in REF: calibrateCamera
     * @param cameraMatrix2_nativeObj native matrix or object handle for {@code cameraMatrix2}; intrinsic matrix of the
     *                                second camera
     * @param distCoeffs2_nativeObj   native matrix or object handle for {@code distCoeffs2}; Input/output vector of
     *                                distortion coefficients, the same as in REF: calibrateCamera
     * @param E_nativeObj             native matrix or object handle for {@code E}; The output essential matrix
     * @param R_nativeObj             native matrix or object handle for {@code R}; Output rotation matrix. Together
     *                                with the translation vector, this matrix makes up a tuple that performs a change
     *                                of basis from the first camera's coordinate system to the second camera's
     *                                coordinate system. Note that, in general, t can not be used for this tuple, see
     *                                the parameter described below
     * @param t_nativeObj             native matrix or object handle for {@code t}; Output translation vector. This
     *                                vector is obtained by REF: decomposeEssentialMat and therefore is only known up to
     *                                scale, i.e. t is the direction of the translation vector and has unit length
     * @return the number of inlier correspondences passing the pose cheirality check
     */
    private static native int recoverPose_4(
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix1_nativeObj,
            long distCoeffs1_nativeObj,
            long cameraMatrix2_nativeObj,
            long distCoeffs2_nativeObj,
            long E_nativeObj,
            long R_nativeObj,
            long t_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#recoverPose(Mat,Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param E_nativeObj            native matrix or object handle for {@code E}; The output essential matrix
     * @param points1_nativeObj      native matrix or object handle for {@code points1}; Array of N 2D points from the
     *                               first image. The point coordinates should be floating-point (single or double
     *                               precision)
     * @param points2_nativeObj      native matrix or object handle for {@code points2}; Array of the second image
     *                               points of the same size and format as points1
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Camera intrinsic matrix
     *                               cameramatrix{A} . Note that this function assumes that points1 and points2 are
     *                               feature points from cameras with the same camera intrinsic matrix
     * @param R_nativeObj            native matrix or object handle for {@code R}; Output rotation matrix. Together with
     *                               the translation vector, this matrix makes up a tuple that performs a change of
     *                               basis from the first camera's coordinate system to the second camera's coordinate
     *                               system. Note that, in general, t can not be used for this tuple, see the parameter
     *                               described below
     * @param t_nativeObj            native matrix or object handle for {@code t}; Output translation vector. This
     *                               vector is obtained by REF: decomposeEssentialMat and therefore is only known up to
     *                               scale, i.e. t is the direction of the translation vector and has unit length
     * @param mask_nativeObj         native matrix or object handle for {@code mask}; Input/output mask for inliers in
     *                               points1 and points2. If it is not empty, then it marks inliers in points1 and
     *                               points2 for the given essential matrix E. Only these inliers will be used to
     *                               recover pose. In the output mask only inliers which pass the chirality check. This
     *                               function decomposes an essential matrix using REF: decomposeEssentialMat and then
     *                               verifies possible pose hypotheses by doing chirality check. The chirality check
     *                               means that the triangulated 3D points should have positive depth. Some details can
     *                               be found in This function can be used to process the output E and mask from REF:
     *                               findEssentialMat. In this scenario, points1 and points2 are the same input for
     *                               findEssentialMat.: // Example. Estimation of fundamental matrix using the RANSAC
     *                               algorithm int point_count = 100; vector&lt;Point2f&gt; points1(point_count);
     *                               vector&lt;Point2f&gt; points2(point_count); // initialize the points here ... for(
     *                               int i = 0; i &lt; point_count; i++ ) { points1[i] = ...; points2[i] = ...; } //
     *                               Input: camera calibration of both cameras, for example using intrinsic chessboard
     *                               calibration. Mat cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2; // Output:
     *                               Essential matrix, relative rotation and relative translation. Mat E, R, t, mask;
     *                               recoverPose(points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2,
     *                               distCoeffs2, E, R, t, mask);
     * @return the number of inlier correspondences passing the pose cheirality check
     */
    private static native int recoverPose_5(
            long E_nativeObj,
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix_nativeObj,
            long R_nativeObj,
            long t_nativeObj,
            long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#recoverPose(Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param E_nativeObj            native matrix or object handle for {@code E}; The output essential matrix
     * @param points1_nativeObj      native matrix or object handle for {@code points1}; Array of N 2D points from the
     *                               first image. The point coordinates should be floating-point (single or double
     *                               precision)
     * @param points2_nativeObj      native matrix or object handle for {@code points2}; Array of the second image
     *                               points of the same size and format as points1
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Camera intrinsic matrix
     *                               cameramatrix{A} . Note that this function assumes that points1 and points2 are
     *                               feature points from cameras with the same camera intrinsic matrix
     * @param R_nativeObj            native matrix or object handle for {@code R}; Output rotation matrix. Together with
     *                               the translation vector, this matrix makes up a tuple that performs a change of
     *                               basis from the first camera's coordinate system to the second camera's coordinate
     *                               system. Note that, in general, t can not be used for this tuple, see the parameter
     *                               described below
     * @param t_nativeObj            native matrix or object handle for {@code t}; Output translation vector. This
     *                               vector is obtained by REF: decomposeEssentialMat and therefore is only known up to
     *                               scale, i.e. t is the direction of the translation vector and has unit length
     * @return the number of inlier correspondences passing the pose cheirality check
     */
    private static native int recoverPose_6(
            long E_nativeObj,
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix_nativeObj,
            long R_nativeObj,
            long t_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#recoverPose(Mat,Mat,Mat,Mat,Mat,double,Point,Mat)}.
     *
     * @param E_nativeObj       native matrix or object handle for {@code E}; The output essential matrix
     * @param points1_nativeObj native matrix or object handle for {@code points1}; Array of N 2D points from the first
     *                          image. The point coordinates should be floating-point (single or double precision)
     * @param points2_nativeObj native matrix or object handle for {@code points2}; Array of the second image points of
     *                          the same size and format as points1
     * @param R_nativeObj       native matrix or object handle for {@code R}; Output rotation matrix. Together with the
     *                          translation vector, this matrix makes up a tuple that performs a change of basis from
     *                          the first camera's coordinate system to the second camera's coordinate system. Note
     *                          that, in general, t can not be used for this tuple, see the parameter described below
     * @param t_nativeObj       native matrix or object handle for {@code t}; Output translation vector. This vector is
     *                          obtained by REF: decomposeEssentialMat and therefore is only known up to scale, i.e. t
     *                          is the direction of the translation vector and has unit length
     * @param focal             Focal length of the camera. Note that this function assumes that points1 and points2 are
     *                          feature points from cameras with same focal length and principal point
     * @param pp_x              {@code pp.x} component of principal point of the camera
     * @param pp_y              {@code pp.y} component of principal point of the camera
     * @param mask_nativeObj    native matrix or object handle for {@code mask}; Input/output mask for inliers in
     *                          points1 and points2. If it is not empty, then it marks inliers in points1 and points2
     *                          for the given essential matrix E. Only these inliers will be used to recover pose. In
     *                          the output mask only inliers which pass the chirality check. This function decomposes an
     *                          essential matrix using REF: decomposeEssentialMat and then verifies possible pose
     *                          hypotheses by doing chirality check. The chirality check means that the triangulated 3D
     *                          points should have positive depth. Some details can be found in This function can be
     *                          used to process the output E and mask from REF: findEssentialMat. In this scenario,
     *                          points1 and points2 are the same input for findEssentialMat.: // Example. Estimation of
     *                          fundamental matrix using the RANSAC algorithm int point_count = 100;
     *                          vector&lt;Point2f&gt; points1(point_count); vector&lt;Point2f&gt; points2(point_count);
     *                          // initialize the points here ... for( int i = 0; i &lt; point_count; i++ ) { points1[i]
     *                          = ...; points2[i] = ...; } // Input: camera calibration of both cameras, for example
     *                          using intrinsic chessboard calibration. Mat cameraMatrix1, distCoeffs1, cameraMatrix2,
     *                          distCoeffs2; // Output: Essential matrix, relative rotation and relative translation.
     *                          Mat E, R, t, mask; recoverPose(points1, points2, cameraMatrix1, distCoeffs1,
     *                          cameraMatrix2, distCoeffs2, E, R, t, mask);
     * @return the number of inlier correspondences passing the pose cheirality check
     */
    private static native int recoverPose_7(
            long E_nativeObj,
            long points1_nativeObj,
            long points2_nativeObj,
            long R_nativeObj,
            long t_nativeObj,
            double focal,
            double pp_x,
            double pp_y,
            long mask_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#recoverPose(Mat,Mat,Mat,Mat,Mat,double,Point)}.
     *
     * @param E_nativeObj       native matrix or object handle for {@code E}; The output essential matrix
     * @param points1_nativeObj native matrix or object handle for {@code points1}; Array of N 2D points from the first
     *                          image. The point coordinates should be floating-point (single or double precision)
     * @param points2_nativeObj native matrix or object handle for {@code points2}; Array of the second image points of
     *                          the same size and format as points1
     * @param R_nativeObj       native matrix or object handle for {@code R}; Output rotation matrix. Together with the
     *                          translation vector, this matrix makes up a tuple that performs a change of basis from
     *                          the first camera's coordinate system to the second camera's coordinate system. Note
     *                          that, in general, t can not be used for this tuple, see the parameter described below
     * @param t_nativeObj       native matrix or object handle for {@code t}; Output translation vector. This vector is
     *                          obtained by REF: decomposeEssentialMat and therefore is only known up to scale, i.e. t
     *                          is the direction of the translation vector and has unit length
     * @param focal             Focal length of the camera. Note that this function assumes that points1 and points2 are
     *                          feature points from cameras with same focal length and principal point
     * @param pp_x              {@code pp.x} component of principal point of the camera. inliers in points1 and points2
     *                          for the given essential matrix E. Only these inliers will be used to recover pose. In
     *                          the output mask only inliers which pass the chirality check. This function differs from
     *                          the one above that it computes camera intrinsic matrix from focal length and principal
     *                          point: A = f &amp; 0 &amp; x_{pp} \\ 0 &amp; f &amp; y_{pp} \\ 0 &amp; 0 &amp; 1
     * @param pp_y              {@code pp.y} component of principal point of the camera. inliers in points1 and points2
     *                          for the given essential matrix E. Only these inliers will be used to recover pose. In
     *                          the output mask only inliers which pass the chirality check. This function differs from
     *                          the one above that it computes camera intrinsic matrix from focal length and principal
     *                          point: A = f &amp; 0 &amp; x_{pp} \\ 0 &amp; f &amp; y_{pp} \\ 0 &amp; 0 &amp; 1
     * @return the number of inlier correspondences passing the pose cheirality check
     */
    private static native int recoverPose_8(
            long E_nativeObj,
            long points1_nativeObj,
            long points2_nativeObj,
            long R_nativeObj,
            long t_nativeObj,
            double focal,
            double pp_x,
            double pp_y);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#recoverPose(Mat,Mat,Mat,Mat,Mat,double)}.
     *
     * @param E_nativeObj       native matrix or object handle for {@code E}; The output essential matrix
     * @param points1_nativeObj native matrix or object handle for {@code points1}; Array of N 2D points from the first
     *                          image. The point coordinates should be floating-point (single or double precision)
     * @param points2_nativeObj native matrix or object handle for {@code points2}; Array of the second image points of
     *                          the same size and format as points1
     * @param R_nativeObj       native matrix or object handle for {@code R}; Output rotation matrix. Together with the
     *                          translation vector, this matrix makes up a tuple that performs a change of basis from
     *                          the first camera's coordinate system to the second camera's coordinate system. Note
     *                          that, in general, t can not be used for this tuple, see the parameter described below
     * @param t_nativeObj       native matrix or object handle for {@code t}; Output translation vector. This vector is
     *                          obtained by REF: decomposeEssentialMat and therefore is only known up to scale, i.e. t
     *                          is the direction of the translation vector and has unit length
     * @param focal             Focal length of the camera. Note that this function assumes that points1 and points2 are
     *                          feature points from cameras with same focal length and principal point. inliers in
     *                          points1 and points2 for the given essential matrix E. Only these inliers will be used to
     *                          recover pose. In the output mask only inliers which pass the chirality check. This
     *                          function differs from the one above that it computes camera intrinsic matrix from focal
     *                          length and principal point: A = f &amp; 0 &amp; x_{pp} \\ 0 &amp; f &amp; y_{pp} \\ 0
     *                          &amp; 0 &amp; 1
     * @return the number of inlier correspondences passing the pose cheirality check
     */
    private static native int recoverPose_9(
            long E_nativeObj,
            long points1_nativeObj,
            long points2_nativeObj,
            long R_nativeObj,
            long t_nativeObj,
            double focal);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#recoverPose(Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param E_nativeObj       native matrix or object handle for {@code E}; The output essential matrix
     * @param points1_nativeObj native matrix or object handle for {@code points1}; Array of N 2D points from the first
     *                          image. The point coordinates should be floating-point (single or double precision)
     * @param points2_nativeObj native matrix or object handle for {@code points2}; Array of the second image points of
     *                          the same size and format as points1
     * @param R_nativeObj       native matrix or object handle for {@code R}; Output rotation matrix. Together with the
     *                          translation vector, this matrix makes up a tuple that performs a change of basis from
     *                          the first camera's coordinate system to the second camera's coordinate system. Note
     *                          that, in general, t can not be used for this tuple, see the parameter described below
     * @param t_nativeObj       native matrix or object handle for {@code t}; Output translation vector. This vector is
     *                          obtained by REF: decomposeEssentialMat and therefore is only known up to scale, i.e. t
     *                          is the direction of the translation vector and has unit length
     * @return the number of inlier correspondences passing the pose cheirality check
     */
    private static native int recoverPose_10(
            long E_nativeObj,
            long points1_nativeObj,
            long points2_nativeObj,
            long R_nativeObj,
            long t_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#recoverPose(Mat,Mat,Mat,Mat,Mat,Mat,double,Mat,Mat)}.
     *
     * @param E_nativeObj                  native matrix or object handle for {@code E}; The output essential matrix
     * @param points1_nativeObj            native matrix or object handle for {@code points1}; Array of N 2D points from
     *                                     the first image. The point coordinates should be floating-point (single or
     *                                     double precision)
     * @param points2_nativeObj            native matrix or object handle for {@code points2}; Array of the second image
     *                                     points of the same size and format as points1
     * @param cameraMatrix_nativeObj       native matrix or object handle for {@code cameraMatrix}; Camera intrinsic
     *                                     matrix cameramatrix{A} . Note that this function assumes that points1 and
     *                                     points2 are feature points from cameras with the same camera intrinsic matrix
     * @param R_nativeObj                  native matrix or object handle for {@code R}; Output rotation matrix.
     *                                     Together with the translation vector, this matrix makes up a tuple that
     *                                     performs a change of basis from the first camera's coordinate system to the
     *                                     second camera's coordinate system. Note that, in general, t can not be used
     *                                     for this tuple, see the parameter described below
     * @param t_nativeObj                  native matrix or object handle for {@code t}; Output translation vector. This
     *                                     vector is obtained by REF: decomposeEssentialMat and therefore is only known
     *                                     up to scale, i.e. t is the direction of the translation vector and has unit
     *                                     length
     * @param distanceThresh               threshold distance which is used to filter out far away points (i.e. infinite
     *                                     points)
     * @param mask_nativeObj               native matrix or object handle for {@code mask}; Input/output mask for
     *                                     inliers in points1 and points2. If it is not empty, then it marks inliers in
     *                                     points1 and points2 for the given essential matrix E. Only these inliers will
     *                                     be used to recover pose. In the output mask only inliers which pass the
     *                                     chirality check. This function decomposes an essential matrix using REF:
     *                                     decomposeEssentialMat and then verifies possible pose hypotheses by doing
     *                                     chirality check. The chirality check means that the triangulated 3D points
     *                                     should have positive depth. Some details can be found in This function can be
     *                                     used to process the output E and mask from REF: findEssentialMat. In this
     *                                     scenario, points1 and points2 are the same input for findEssentialMat.: //
     *                                     Example. Estimation of fundamental matrix using the RANSAC algorithm int
     *                                     point_count = 100; vector&lt;Point2f&gt; points1(point_count);
     *                                     vector&lt;Point2f&gt; points2(point_count); // initialize the points here ...
     *                                     for( int i = 0; i &lt; point_count; i++ ) { points1[i] = ...; points2[i] =
     *                                     ...; } // Input: camera calibration of both cameras, for example using
     *                                     intrinsic chessboard calibration. Mat cameraMatrix1, distCoeffs1,
     *                                     cameraMatrix2, distCoeffs2; // Output: Essential matrix, relative rotation
     *                                     and relative translation. Mat E, R, t, mask; recoverPose(points1, points2,
     *                                     cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2, E, R, t, mask);
     * @param triangulatedPoints_nativeObj native matrix or object handle for {@code triangulatedPoints}; 3D points
     *                                     which were reconstructed by triangulation. This function differs from the one
     *                                     above that it outputs the triangulated 3D point that are used for the
     *                                     chirality check
     * @return the number of inlier correspondences passing the pose cheirality check
     */
    private static native int recoverPose_11(
            long E_nativeObj,
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix_nativeObj,
            long R_nativeObj,
            long t_nativeObj,
            double distanceThresh,
            long mask_nativeObj,
            long triangulatedPoints_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#recoverPose(Mat,Mat,Mat,Mat,Mat,Mat,double,Mat)}.
     *
     * @param E_nativeObj            native matrix or object handle for {@code E}; The output essential matrix
     * @param points1_nativeObj      native matrix or object handle for {@code points1}; Array of N 2D points from the
     *                               first image. The point coordinates should be floating-point (single or double
     *                               precision)
     * @param points2_nativeObj      native matrix or object handle for {@code points2}; Array of the second image
     *                               points of the same size and format as points1
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Camera intrinsic matrix
     *                               cameramatrix{A} . Note that this function assumes that points1 and points2 are
     *                               feature points from cameras with the same camera intrinsic matrix
     * @param R_nativeObj            native matrix or object handle for {@code R}; Output rotation matrix. Together with
     *                               the translation vector, this matrix makes up a tuple that performs a change of
     *                               basis from the first camera's coordinate system to the second camera's coordinate
     *                               system. Note that, in general, t can not be used for this tuple, see the parameter
     *                               described below
     * @param t_nativeObj            native matrix or object handle for {@code t}; Output translation vector. This
     *                               vector is obtained by REF: decomposeEssentialMat and therefore is only known up to
     *                               scale, i.e. t is the direction of the translation vector and has unit length
     * @param distanceThresh         threshold distance which is used to filter out far away points (i.e. infinite
     *                               points)
     * @param mask_nativeObj         native matrix or object handle for {@code mask}; Input/output mask for inliers in
     *                               points1 and points2. If it is not empty, then it marks inliers in points1 and
     *                               points2 for the given essential matrix E. Only these inliers will be used to
     *                               recover pose. In the output mask only inliers which pass the chirality check. This
     *                               function decomposes an essential matrix using REF: decomposeEssentialMat and then
     *                               verifies possible pose hypotheses by doing chirality check. The chirality check
     *                               means that the triangulated 3D points should have positive depth. Some details can
     *                               be found in This function can be used to process the output E and mask from REF:
     *                               findEssentialMat. In this scenario, points1 and points2 are the same input for
     *                               findEssentialMat.: // Example. Estimation of fundamental matrix using the RANSAC
     *                               algorithm int point_count = 100; vector&lt;Point2f&gt; points1(point_count);
     *                               vector&lt;Point2f&gt; points2(point_count); // initialize the points here ... for(
     *                               int i = 0; i &lt; point_count; i++ ) { points1[i] = ...; points2[i] = ...; } //
     *                               Input: camera calibration of both cameras, for example using intrinsic chessboard
     *                               calibration. Mat cameraMatrix1, distCoeffs1, cameraMatrix2, distCoeffs2; // Output:
     *                               Essential matrix, relative rotation and relative translation. Mat E, R, t, mask;
     *                               recoverPose(points1, points2, cameraMatrix1, distCoeffs1, cameraMatrix2,
     *                               distCoeffs2, E, R, t, mask);
     * @return the number of inlier correspondences passing the pose cheirality check
     */
    private static native int recoverPose_12(
            long E_nativeObj,
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix_nativeObj,
            long R_nativeObj,
            long t_nativeObj,
            double distanceThresh,
            long mask_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#recoverPose(Mat,Mat,Mat,Mat,Mat,Mat,double)}.
     *
     * @param E_nativeObj            native matrix or object handle for {@code E}; The output essential matrix
     * @param points1_nativeObj      native matrix or object handle for {@code points1}; Array of N 2D points from the
     *                               first image. The point coordinates should be floating-point (single or double
     *                               precision)
     * @param points2_nativeObj      native matrix or object handle for {@code points2}; Array of the second image
     *                               points of the same size and format as points1
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Camera intrinsic matrix
     *                               cameramatrix{A} . Note that this function assumes that points1 and points2 are
     *                               feature points from cameras with the same camera intrinsic matrix
     * @param R_nativeObj            native matrix or object handle for {@code R}; Output rotation matrix. Together with
     *                               the translation vector, this matrix makes up a tuple that performs a change of
     *                               basis from the first camera's coordinate system to the second camera's coordinate
     *                               system. Note that, in general, t can not be used for this tuple, see the parameter
     *                               described below
     * @param t_nativeObj            native matrix or object handle for {@code t}; Output translation vector. This
     *                               vector is obtained by REF: decomposeEssentialMat and therefore is only known up to
     *                               scale, i.e. t is the direction of the translation vector and has unit length
     * @param distanceThresh         threshold distance which is used to filter out far away points (i.e. infinite
     *                               points). inliers in points1 and points2 for the given essential matrix E. Only
     *                               these inliers will be used to recover pose. In the output mask only inliers which
     *                               pass the chirality check. This function differs from the one above that it outputs
     *                               the triangulated 3D point that are used for the chirality check
     * @return the number of inlier correspondences passing the pose cheirality check
     */
    private static native int recoverPose_13(
            long E_nativeObj,
            long points1_nativeObj,
            long points2_nativeObj,
            long cameraMatrix_nativeObj,
            long R_nativeObj,
            long t_nativeObj,
            double distanceThresh);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#computeCorrespondEpilines(Mat,int,Mat,Mat)}.
     *
     * @param points_nativeObj native matrix or object handle for {@code points}; Input points. N times 1 or 1 times N
     *                         matrix of type CV_32FC2 or vector&lt;Point2f&gt;
     * @param whichImage       Index of the image (1 or 2) that contains the points
     * @param F_nativeObj      native matrix or object handle for {@code F}; Fundamental matrix that can be estimated
     *                         using findFundamentalMat or stereoRectify
     * @param lines_nativeObj  native matrix or object handle for {@code lines}; Output vector of the epipolar lines
     *                         corresponding to the points in the other image. Each line ax + by + c=0 is encoded by 3
     *                         numbers (a, b, c) . For every point in one of the two images of a stereo pair, the
     *                         function finds the equation of the corresponding epipolar line in the other image. From
     *                         the fundamental matrix definition (see findFundamentalMat ), line l^{(2)}_i in the second
     *                         image for the point p^{(1)}_i in the first image (when whichImage=1 ) is computed as:
     *                         l^{(2)}_i = F p^{(1)}_i And vice versa, when whichImage=2, l^{(1)}_i is computed from
     *                         p^{(2)}_i as: l^{(1)}_i = F^T p^{(2)}_i Line coefficients are defined up to a scale. They
     *                         are normalized so that a_i^2+b_i^2=1
     */
    private static native void computeCorrespondEpilines_0(
            long points_nativeObj,
            int whichImage,
            long F_nativeObj,
            long lines_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#triangulatePoints(Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param projMatr1_nativeObj   native matrix or object handle for {@code projMatr1}; 3x4 projection matrix of the
     *                              first camera, i.e. this matrix projects 3D points given in the world's coordinate
     *                              system into the first image
     * @param projMatr2_nativeObj   native matrix or object handle for {@code projMatr2}; 3x4 projection matrix of the
     *                              second camera, i.e. this matrix projects 3D points given in the world's coordinate
     *                              system into the second image
     * @param projPoints1_nativeObj native matrix or object handle for {@code projPoints1}; 2xN array of feature points
     *                              in the first image. In the case of the c++ version, it can be also a vector of
     *                              feature points or two-channel matrix of size 1xN or Nx1
     * @param projPoints2_nativeObj native matrix or object handle for {@code projPoints2}; 2xN array of corresponding
     *                              points in the second image. In the case of the c++ version, it can be also a vector
     *                              of feature points or two-channel matrix of size 1xN or Nx1
     * @param points4D_nativeObj    native matrix or object handle for {@code points4D}; 4xN array of reconstructed
     *                              points in homogeneous coordinates. These points are returned in the world's
     *                              coordinate system. Note: Keep in mind that all input data should be of float type in
     *                              order for this function to work. Note: If the projection matrices from REF:
     *                              stereoRectify are used, then the returned points are represented in the first
     *                              camera's rectified coordinate system
     */
    private static native void triangulatePoints_0(
            long projMatr1_nativeObj,
            long projMatr2_nativeObj,
            long projPoints1_nativeObj,
            long projPoints2_nativeObj,
            long points4D_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#correctMatches(Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param F_nativeObj          native matrix or object handle for {@code F}; 3x3 fundamental matrix
     * @param points1_nativeObj    native matrix or object handle for {@code points1}; 1xN array containing the first
     *                             set of points
     * @param points2_nativeObj    native matrix or object handle for {@code points2}; 1xN array containing the second
     *                             set of points
     * @param newPoints1_nativeObj native matrix or object handle for {@code newPoints1}; The optimized points1
     * @param newPoints2_nativeObj native matrix or object handle for {@code newPoints2}; The optimized points2. The
     *                             function implements the Optimal Triangulation Method (see Multiple View Geometry for
     *                             details). For each given point correspondence points1[i] &lt;-&gt; points2[i], and a
     *                             fundamental matrix F, it computes the corrected correspondences newPoints1[i]
     *                             &lt;-&gt; newPoints2[i] that minimize the geometric error d(points1[i],
     *                             newPoints1[i])^2 + d(points2[i],newPoints2[i])^2 (where d(a,b) is the geometric
     *                             distance between points a and b ) subject to the epipolar constraint newPoints2^T
     *                             cdot F cdot newPoints1 = 0
     */
    private static native void correctMatches_0(
            long F_nativeObj,
            long points1_nativeObj,
            long points2_nativeObj,
            long newPoints1_nativeObj,
            long newPoints2_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#sampsonDistance(Mat,Mat,Mat)}.
     *
     * @param pt1_nativeObj native matrix or object handle for {@code pt1}; first homogeneous 2d point
     * @param pt2_nativeObj native matrix or object handle for {@code pt2}; second homogeneous 2d point
     * @param F_nativeObj   native matrix or object handle for {@code F}; fundamental matrix
     * @return The computed Sampson distance
     */
    private static native double sampsonDistance_0(long pt1_nativeObj, long pt2_nativeObj, long F_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffine3D(Mat,Mat,Mat,Mat,double,double)}.
     *
     * @param src_nativeObj     native matrix or object handle for {@code src}; First input 3D point set containing
     *                          (X,Y,Z)
     * @param dst_nativeObj     native matrix or object handle for {@code dst}; Second input 3D point set containing
     *                          (x,y,z)
     * @param out_nativeObj     native matrix or object handle for {@code out}; Output 3D affine transformation matrix 3
     *                          times 4 of the form a_{11} &amp; a_{12} &amp; a_{13} &amp; b_1\\ a_{21} &amp; a_{22}
     *                          &amp; a_{23} &amp; b_2\\ a_{31} &amp; a_{32} &amp; a_{33} &amp; b_3\\
     * @param inliers_nativeObj native matrix or object handle for {@code inliers}; Output vector indicating which
     *                          points are inliers (1-inlier, 0-outlier)
     * @param ransacThreshold   Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier
     * @param confidence        Confidence level, between 0 and 1, for the estimated transformation. Anything between
     *                          0.95 and 0.99 is usually good enough. Values too close to 1 can slow down the estimation
     *                          significantly. Values lower than 0.8-0.9 can result in an incorrectly estimated
     *                          transformation
     * @return Whether a solution was found. The function estimates an optimal 3D affine transformation between two 3D
     *         point sets using the RANSAC algorithm
     */
    private static native boolean estimateAffine3D_0(
            long src_nativeObj,
            long dst_nativeObj,
            long out_nativeObj,
            long inliers_nativeObj,
            double ransacThreshold,
            double confidence);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffine3D(Mat,Mat,Mat,Mat,double)}.
     *
     * @param src_nativeObj     native matrix or object handle for {@code src}; First input 3D point set containing
     *                          (X,Y,Z)
     * @param dst_nativeObj     native matrix or object handle for {@code dst}; Second input 3D point set containing
     *                          (x,y,z)
     * @param out_nativeObj     native matrix or object handle for {@code out}; Output 3D affine transformation matrix 3
     *                          times 4 of the form a_{11} &amp; a_{12} &amp; a_{13} &amp; b_1\\ a_{21} &amp; a_{22}
     *                          &amp; a_{23} &amp; b_2\\ a_{31} &amp; a_{32} &amp; a_{33} &amp; b_3\\
     * @param inliers_nativeObj native matrix or object handle for {@code inliers}; Output vector indicating which
     *                          points are inliers (1-inlier, 0-outlier)
     * @param ransacThreshold   Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier
     * @return Whether a solution was found. The function estimates an optimal 3D affine transformation between two 3D
     *         point sets using the RANSAC algorithm
     */
    private static native boolean estimateAffine3D_1(
            long src_nativeObj,
            long dst_nativeObj,
            long out_nativeObj,
            long inliers_nativeObj,
            double ransacThreshold);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffine3D(Mat,Mat,Mat,Mat)}.
     *
     * @param src_nativeObj     native matrix or object handle for {@code src}; First input 3D point set containing
     *                          (X,Y,Z)
     * @param dst_nativeObj     native matrix or object handle for {@code dst}; Second input 3D point set containing
     *                          (x,y,z)
     * @param out_nativeObj     native matrix or object handle for {@code out}; Output 3D affine transformation matrix 3
     *                          times 4 of the form a_{11} &amp; a_{12} &amp; a_{13} &amp; b_1\\ a_{21} &amp; a_{22}
     *                          &amp; a_{23} &amp; b_2\\ a_{31} &amp; a_{32} &amp; a_{33} &amp; b_3\\
     * @param inliers_nativeObj native matrix or object handle for {@code inliers}; Output vector indicating which
     *                          points are inliers (1-inlier, 0-outlier)
     * @return Whether a solution was found. The function estimates an optimal 3D affine transformation between two 3D
     *         point sets using the RANSAC algorithm
     */
    private static native boolean estimateAffine3D_2(
            long src_nativeObj,
            long dst_nativeObj,
            long out_nativeObj,
            long inliers_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffine3D(Mat,Mat,double[],boolean)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; First input 3D point set containing (X,Y,Z)
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; Second input 3D point set containing
     *                       (x,y,z)
     * @param scale_out      output array receiving {@code scale} from the native call
     * @param force_rotation If true, the returned rotation will never be a reflection. This might be unwanted, e.g.
     *                       when optimizing a transform between a right- and a left-handed coordinate system
     * @return native binding handle for Whether a solution was found. The function estimates an optimal 3D affine
     *         transformation between two 3D point sets using the RANSAC algorithm
     */
    private static native long estimateAffine3D_3(
            long src_nativeObj,
            long dst_nativeObj,
            double[] scale_out,
            boolean force_rotation);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffine3D(Mat,Mat,double[])}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; First input 3D point set containing (X,Y,Z)
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Second input 3D point set containing (x,y,z)
     * @param scale_out     output array receiving {@code scale} from the native call
     * @return native binding handle for Whether a solution was found. The function estimates an optimal 3D affine
     *         transformation between two 3D point sets using the RANSAC algorithm
     */
    private static native long estimateAffine3D_4(long src_nativeObj, long dst_nativeObj, double[] scale_out);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffine3D(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; First input 3D point set containing (X,Y,Z)
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Second input 3D point set containing (x,y,z)
     * @return native binding handle for Whether a solution was found. The function estimates an optimal 3D affine
     *         transformation between two 3D point sets using the RANSAC algorithm
     */
    private static native long estimateAffine3D_5(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateTranslation3D(Mat,Mat,Mat,Mat,double,double)}.
     *
     * @param src_nativeObj     native matrix or object handle for {@code src}; First input 3D point set containing
     *                          (X,Y,Z)
     * @param dst_nativeObj     native matrix or object handle for {@code dst}; Second input 3D point set containing
     *                          (x,y,z)
     * @param out_nativeObj     native matrix or object handle for {@code out}; Output 3D translation vector 3 times 1
     *                          of the form b_1 \\ b_2 \\ b_3 \\
     * @param inliers_nativeObj native matrix or object handle for {@code inliers}; Output vector indicating which
     *                          points are inliers (1-inlier, 0-outlier)
     * @param ransacThreshold   Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier
     * @param confidence        Confidence level, between 0 and 1, for the estimated transformation. Anything between
     *                          0.95 and 0.99 is usually good enough. Values too close to 1 can slow down the estimation
     *                          significantly. Values lower than 0.8-0.9 can result in an incorrectly estimated
     *                          transformation
     * @return Whether a translation was found. The function estimates an optimal 3D translation between two 3D point
     *         sets using the RANSAC algorithm
     */
    private static native boolean estimateTranslation3D_0(
            long src_nativeObj,
            long dst_nativeObj,
            long out_nativeObj,
            long inliers_nativeObj,
            double ransacThreshold,
            double confidence);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateTranslation3D(Mat,Mat,Mat,Mat,double)}.
     *
     * @param src_nativeObj     native matrix or object handle for {@code src}; First input 3D point set containing
     *                          (X,Y,Z)
     * @param dst_nativeObj     native matrix or object handle for {@code dst}; Second input 3D point set containing
     *                          (x,y,z)
     * @param out_nativeObj     native matrix or object handle for {@code out}; Output 3D translation vector 3 times 1
     *                          of the form b_1 \\ b_2 \\ b_3 \\
     * @param inliers_nativeObj native matrix or object handle for {@code inliers}; Output vector indicating which
     *                          points are inliers (1-inlier, 0-outlier)
     * @param ransacThreshold   Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier
     * @return Whether a translation was found. The function estimates an optimal 3D translation between two 3D point
     *         sets using the RANSAC algorithm
     */
    private static native boolean estimateTranslation3D_1(
            long src_nativeObj,
            long dst_nativeObj,
            long out_nativeObj,
            long inliers_nativeObj,
            double ransacThreshold);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#estimateTranslation3D(Mat,Mat,Mat,Mat)}.
     *
     * @param src_nativeObj     native matrix or object handle for {@code src}; First input 3D point set containing
     *                          (X,Y,Z)
     * @param dst_nativeObj     native matrix or object handle for {@code dst}; Second input 3D point set containing
     *                          (x,y,z)
     * @param out_nativeObj     native matrix or object handle for {@code out}; Output 3D translation vector 3 times 1
     *                          of the form b_1 \\ b_2 \\ b_3 \\
     * @param inliers_nativeObj native matrix or object handle for {@code inliers}; Output vector indicating which
     *                          points are inliers (1-inlier, 0-outlier)
     * @return Whether a translation was found. The function estimates an optimal 3D translation between two 3D point
     *         sets using the RANSAC algorithm
     */
    private static native boolean estimateTranslation3D_2(
            long src_nativeObj,
            long dst_nativeObj,
            long out_nativeObj,
            long inliers_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffine2D(Mat,Mat,Mat,int,double,long,double,long)}.
     *
     * @param from_nativeObj        native matrix or object handle for {@code from}; First input 2D point set containing
     *                              (X,Y)
     * @param to_nativeObj          native matrix or object handle for {@code to}; Second input 2D point set containing
     *                              (x,y)
     * @param inliers_nativeObj     native matrix or object handle for {@code inliers}; Output vector indicating which
     *                              points are inliers (1-inlier, 0-outlier)
     * @param method                Robust method used to compute transformation. The following methods are possible:
     *                              REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method
     *                              RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @param confidence            Confidence level, between 0 and 1, for the estimated transformation. Anything
     *                              between 0.95 and 0.99 is usually good enough. Values too close to 1 can slow down
     *                              the estimation significantly. Values lower than 0.8-0.9 can result in an incorrectly
     *                              estimated transformation
     * @param refineIters           Maximum number of iterations of refining algorithm (Levenberg-Marquardt). Passing 0
     *                              will disable refining, so the output matrix will be output of robust method
     * @return native binding handle for Output 2D affine transformation matrix 2 times 3 or empty matrix if
     *         transformation could not be estimated. The returned matrix has the following form: a_{11} &amp; a_{12}
     *         &amp; b_1\\ a_{21} &amp; a_{22} &amp; b_2\\ The function estimates an optimal 2D affine transformation
     *         between two 2D point sets using the selected robust algorithm. The computed transformation is then
     *         refined further (using only inliers) with the Levenberg-Marquardt method to reduce the re-projection
     *         error even more. Note: The RANSAC method can handle practically any ratio of outliers but needs a
     *         threshold to distinguish inliers from outliers. The method LMeDS does not need any threshold but it works
     *         correctly only when there are more than 50% of inliers
     */
    private static native long estimateAffine2D_0(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj,
            int method,
            double ransacReprojThreshold,
            long maxIters,
            double confidence,
            long refineIters);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffine2D(Mat,Mat,Mat,int,double,long,double)}.
     *
     * @param from_nativeObj        native matrix or object handle for {@code from}; First input 2D point set containing
     *                              (X,Y)
     * @param to_nativeObj          native matrix or object handle for {@code to}; Second input 2D point set containing
     *                              (x,y)
     * @param inliers_nativeObj     native matrix or object handle for {@code inliers}; Output vector indicating which
     *                              points are inliers (1-inlier, 0-outlier)
     * @param method                Robust method used to compute transformation. The following methods are possible:
     *                              REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method
     *                              RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @param confidence            Confidence level, between 0 and 1, for the estimated transformation. Anything
     *                              between 0.95 and 0.99 is usually good enough. Values too close to 1 can slow down
     *                              the estimation significantly. Values lower than 0.8-0.9 can result in an incorrectly
     *                              estimated transformation
     * @return native binding handle for Output 2D affine transformation matrix 2 times 3 or empty matrix if
     *         transformation could not be estimated. The returned matrix has the following form: a_{11} &amp; a_{12}
     *         &amp; b_1\\ a_{21} &amp; a_{22} &amp; b_2\\ The function estimates an optimal 2D affine transformation
     *         between two 2D point sets using the selected robust algorithm. The computed transformation is then
     *         refined further (using only inliers) with the Levenberg-Marquardt method to reduce the re-projection
     *         error even more. Note: The RANSAC method can handle practically any ratio of outliers but needs a
     *         threshold to distinguish inliers from outliers. The method LMeDS does not need any threshold but it works
     *         correctly only when there are more than 50% of inliers
     */
    private static native long estimateAffine2D_1(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj,
            int method,
            double ransacReprojThreshold,
            long maxIters,
            double confidence);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffine2D(Mat,Mat,Mat,int,double,long)}.
     *
     * @param from_nativeObj        native matrix or object handle for {@code from}; First input 2D point set containing
     *                              (X,Y)
     * @param to_nativeObj          native matrix or object handle for {@code to}; Second input 2D point set containing
     *                              (x,y)
     * @param inliers_nativeObj     native matrix or object handle for {@code inliers}; Output vector indicating which
     *                              points are inliers (1-inlier, 0-outlier)
     * @param method                Robust method used to compute transformation. The following methods are possible:
     *                              REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method
     *                              RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @return native binding handle for Output 2D affine transformation matrix 2 times 3 or empty matrix if
     *         transformation could not be estimated. The returned matrix has the following form: a_{11} &amp; a_{12}
     *         &amp; b_1\\ a_{21} &amp; a_{22} &amp; b_2\\ The function estimates an optimal 2D affine transformation
     *         between two 2D point sets using the selected robust algorithm. The computed transformation is then
     *         refined further (using only inliers) with the Levenberg-Marquardt method to reduce the re-projection
     *         error even more. Note: The RANSAC method can handle practically any ratio of outliers but needs a
     *         threshold to distinguish inliers from outliers. The method LMeDS does not need any threshold but it works
     *         correctly only when there are more than 50% of inliers
     */
    private static native long estimateAffine2D_2(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj,
            int method,
            double ransacReprojThreshold,
            long maxIters);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffine2D(Mat,Mat,Mat,int,double)}.
     *
     * @param from_nativeObj        native matrix or object handle for {@code from}; First input 2D point set containing
     *                              (X,Y)
     * @param to_nativeObj          native matrix or object handle for {@code to}; Second input 2D point set containing
     *                              (x,y)
     * @param inliers_nativeObj     native matrix or object handle for {@code inliers}; Output vector indicating which
     *                              points are inliers (1-inlier, 0-outlier)
     * @param method                Robust method used to compute transformation. The following methods are possible:
     *                              REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method
     *                              RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @return native binding handle for Output 2D affine transformation matrix 2 times 3 or empty matrix if
     *         transformation could not be estimated. The returned matrix has the following form: a_{11} &amp; a_{12}
     *         &amp; b_1\\ a_{21} &amp; a_{22} &amp; b_2\\ The function estimates an optimal 2D affine transformation
     *         between two 2D point sets using the selected robust algorithm. The computed transformation is then
     *         refined further (using only inliers) with the Levenberg-Marquardt method to reduce the re-projection
     *         error even more. Note: The RANSAC method can handle practically any ratio of outliers but needs a
     *         threshold to distinguish inliers from outliers. The method LMeDS does not need any threshold but it works
     *         correctly only when there are more than 50% of inliers
     */
    private static native long estimateAffine2D_3(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj,
            int method,
            double ransacReprojThreshold);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffine2D(Mat,Mat,Mat,int)}.
     *
     * @param from_nativeObj    native matrix or object handle for {@code from}; First input 2D point set containing
     *                          (X,Y)
     * @param to_nativeObj      native matrix or object handle for {@code to}; Second input 2D point set containing
     *                          (x,y)
     * @param inliers_nativeObj native matrix or object handle for {@code inliers}; Output vector indicating which
     *                          points are inliers (1-inlier, 0-outlier)
     * @param method            Robust method used to compute transformation. The following methods are possible: REF:
     *                          RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method RANSAC is
     *                          the default method
     * @return native binding handle for Output 2D affine transformation matrix 2 times 3 or empty matrix if
     *         transformation could not be estimated. The returned matrix has the following form: a_{11} &amp; a_{12}
     *         &amp; b_1\\ a_{21} &amp; a_{22} &amp; b_2\\ The function estimates an optimal 2D affine transformation
     *         between two 2D point sets using the selected robust algorithm. The computed transformation is then
     *         refined further (using only inliers) with the Levenberg-Marquardt method to reduce the re-projection
     *         error even more. Note: The RANSAC method can handle practically any ratio of outliers but needs a
     *         threshold to distinguish inliers from outliers. The method LMeDS does not need any threshold but it works
     *         correctly only when there are more than 50% of inliers
     */
    private static native long estimateAffine2D_4(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj,
            int method);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffine2D(Mat,Mat,Mat)}.
     *
     * @param from_nativeObj    native matrix or object handle for {@code from}; First input 2D point set containing
     *                          (X,Y)
     * @param to_nativeObj      native matrix or object handle for {@code to}; Second input 2D point set containing
     *                          (x,y)
     * @param inliers_nativeObj native matrix or object handle for {@code inliers}; Output vector indicating which
     *                          points are inliers (1-inlier, 0-outlier)
     * @return native binding handle for Output 2D affine transformation matrix 2 times 3 or empty matrix if
     *         transformation could not be estimated. The returned matrix has the following form: a_{11} &amp; a_{12}
     *         &amp; b_1\\ a_{21} &amp; a_{22} &amp; b_2\\ The function estimates an optimal 2D affine transformation
     *         between two 2D point sets using the selected robust algorithm. The computed transformation is then
     *         refined further (using only inliers) with the Levenberg-Marquardt method to reduce the re-projection
     *         error even more. Note: The RANSAC method can handle practically any ratio of outliers but needs a
     *         threshold to distinguish inliers from outliers. The method LMeDS does not need any threshold but it works
     *         correctly only when there are more than 50% of inliers
     */
    private static native long estimateAffine2D_5(long from_nativeObj, long to_nativeObj, long inliers_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffine2D(Mat,Mat)}.
     *
     * @param from_nativeObj native matrix or object handle for {@code from}; First input 2D point set containing (X,Y)
     * @param to_nativeObj   native matrix or object handle for {@code to}; Second input 2D point set containing (x,y)
     * @return native binding handle for Output 2D affine transformation matrix 2 times 3 or empty matrix if
     *         transformation could not be estimated. The returned matrix has the following form: a_{11} &amp; a_{12}
     *         &amp; b_1\\ a_{21} &amp; a_{22} &amp; b_2\\ The function estimates an optimal 2D affine transformation
     *         between two 2D point sets using the selected robust algorithm. The computed transformation is then
     *         refined further (using only inliers) with the Levenberg-Marquardt method to reduce the re-projection
     *         error even more. Note: The RANSAC method can handle practically any ratio of outliers but needs a
     *         threshold to distinguish inliers from outliers. The method LMeDS does not need any threshold but it works
     *         correctly only when there are more than 50% of inliers
     */
    private static native long estimateAffine2D_6(long from_nativeObj, long to_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffine2D(Mat,Mat,Mat,UsacParams)}.
     *
     * @param pts1_nativeObj    native matrix or object handle for {@code pts1}; point coordinates in the first image
     * @param pts2_nativeObj    native matrix or object handle for {@code pts2}; corresponding point coordinates in the
     *                          second image
     * @param inliers_nativeObj native matrix or object handle for {@code inliers}; Output vector indicating which
     *                          points are inliers (1-inlier, 0-outlier)
     * @param params_nativeObj  {@code params.getNativeObjAddr()} component of robust-estimation sampling, scoring,
     *                          threshold and refinement settings
     * @return native binding handle for Output 2D affine transformation matrix 2 times 3 or empty matrix if
     *         transformation could not be estimated. The returned matrix has the following form: a_{11} &amp; a_{12}
     *         &amp; b_1\\ a_{21} &amp; a_{22} &amp; b_2\\ The function estimates an optimal 2D affine transformation
     *         between two 2D point sets using the selected robust algorithm. The computed transformation is then
     *         refined further (using only inliers) with the Levenberg-Marquardt method to reduce the re-projection
     *         error even more. Note: The RANSAC method can handle practically any ratio of outliers but needs a
     *         threshold to distinguish inliers from outliers. The method LMeDS does not need any threshold but it works
     *         correctly only when there are more than 50% of inliers
     */
    private static native long estimateAffine2D_7(
            long pts1_nativeObj,
            long pts2_nativeObj,
            long inliers_nativeObj,
            long params_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffinePartial2D(Mat,Mat,Mat,int,double,long,double,long)}.
     *
     * @param from_nativeObj        native matrix or object handle for {@code from}; First input 2D point set
     * @param to_nativeObj          native matrix or object handle for {@code to}; Second input 2D point set
     * @param inliers_nativeObj     native matrix or object handle for {@code inliers}; Output vector indicating which
     *                              points are inliers
     * @param method                Robust method used to compute transformation. The following methods are possible:
     *                              REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method
     *                              RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @param confidence            Confidence level, between 0 and 1, for the estimated transformation. Anything
     *                              between 0.95 and 0.99 is usually good enough. Values too close to 1 can slow down
     *                              the estimation significantly. Values lower than 0.8-0.9 can result in an incorrectly
     *                              estimated transformation
     * @param refineIters           Maximum number of iterations of refining algorithm (Levenberg-Marquardt). Passing 0
     *                              will disable refining, so the output matrix will be output of robust method
     * @return native binding handle for Output 2D affine transformation (4 degrees of freedom) matrix 2 times 3 or
     *         empty matrix if transformation could not be estimated. The function estimates an optimal 2D affine
     *         transformation with 4 degrees of freedom limited to combinations of translation, rotation, and uniform
     *         scaling. Uses the selected algorithm for robust estimation. The computed transformation is then refined
     *         further (using only inliers) with the Levenberg-Marquardt method to reduce the re-projection error even
     *         more. Estimated transformation matrix is: cos(theta) cdot s &amp; -sin(theta) cdot s &amp; t_x \\
     *         sin(theta) cdot s &amp; cos(theta) cdot s &amp; t_y Where theta is the rotation angle, s the scaling
     *         factor and t_x, t_y are translations in x, y axes respectively. Note: The RANSAC method can handle
     *         practically any ratio of outliers but need a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but it works correctly only when there are more than 50% of inliers
     */
    private static native long estimateAffinePartial2D_0(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj,
            int method,
            double ransacReprojThreshold,
            long maxIters,
            double confidence,
            long refineIters);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffinePartial2D(Mat,Mat,Mat,int,double,long,double)}.
     *
     * @param from_nativeObj        native matrix or object handle for {@code from}; First input 2D point set
     * @param to_nativeObj          native matrix or object handle for {@code to}; Second input 2D point set
     * @param inliers_nativeObj     native matrix or object handle for {@code inliers}; Output vector indicating which
     *                              points are inliers
     * @param method                Robust method used to compute transformation. The following methods are possible:
     *                              REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method
     *                              RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @param confidence            Confidence level, between 0 and 1, for the estimated transformation. Anything
     *                              between 0.95 and 0.99 is usually good enough. Values too close to 1 can slow down
     *                              the estimation significantly. Values lower than 0.8-0.9 can result in an incorrectly
     *                              estimated transformation
     * @return native binding handle for Output 2D affine transformation (4 degrees of freedom) matrix 2 times 3 or
     *         empty matrix if transformation could not be estimated. The function estimates an optimal 2D affine
     *         transformation with 4 degrees of freedom limited to combinations of translation, rotation, and uniform
     *         scaling. Uses the selected algorithm for robust estimation. The computed transformation is then refined
     *         further (using only inliers) with the Levenberg-Marquardt method to reduce the re-projection error even
     *         more. Estimated transformation matrix is: cos(theta) cdot s &amp; -sin(theta) cdot s &amp; t_x \\
     *         sin(theta) cdot s &amp; cos(theta) cdot s &amp; t_y Where theta is the rotation angle, s the scaling
     *         factor and t_x, t_y are translations in x, y axes respectively. Note: The RANSAC method can handle
     *         practically any ratio of outliers but need a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but it works correctly only when there are more than 50% of inliers
     */
    private static native long estimateAffinePartial2D_1(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj,
            int method,
            double ransacReprojThreshold,
            long maxIters,
            double confidence);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffinePartial2D(Mat,Mat,Mat,int,double,long)}.
     *
     * @param from_nativeObj        native matrix or object handle for {@code from}; First input 2D point set
     * @param to_nativeObj          native matrix or object handle for {@code to}; Second input 2D point set
     * @param inliers_nativeObj     native matrix or object handle for {@code inliers}; Output vector indicating which
     *                              points are inliers
     * @param method                Robust method used to compute transformation. The following methods are possible:
     *                              REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method
     *                              RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @return native binding handle for Output 2D affine transformation (4 degrees of freedom) matrix 2 times 3 or
     *         empty matrix if transformation could not be estimated. The function estimates an optimal 2D affine
     *         transformation with 4 degrees of freedom limited to combinations of translation, rotation, and uniform
     *         scaling. Uses the selected algorithm for robust estimation. The computed transformation is then refined
     *         further (using only inliers) with the Levenberg-Marquardt method to reduce the re-projection error even
     *         more. Estimated transformation matrix is: cos(theta) cdot s &amp; -sin(theta) cdot s &amp; t_x \\
     *         sin(theta) cdot s &amp; cos(theta) cdot s &amp; t_y Where theta is the rotation angle, s the scaling
     *         factor and t_x, t_y are translations in x, y axes respectively. Note: The RANSAC method can handle
     *         practically any ratio of outliers but need a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but it works correctly only when there are more than 50% of inliers
     */
    private static native long estimateAffinePartial2D_2(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj,
            int method,
            double ransacReprojThreshold,
            long maxIters);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffinePartial2D(Mat,Mat,Mat,int,double)}.
     *
     * @param from_nativeObj        native matrix or object handle for {@code from}; First input 2D point set
     * @param to_nativeObj          native matrix or object handle for {@code to}; Second input 2D point set
     * @param inliers_nativeObj     native matrix or object handle for {@code inliers}; Output vector indicating which
     *                              points are inliers
     * @param method                Robust method used to compute transformation. The following methods are possible:
     *                              REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method
     *                              RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @return native binding handle for Output 2D affine transformation (4 degrees of freedom) matrix 2 times 3 or
     *         empty matrix if transformation could not be estimated. The function estimates an optimal 2D affine
     *         transformation with 4 degrees of freedom limited to combinations of translation, rotation, and uniform
     *         scaling. Uses the selected algorithm for robust estimation. The computed transformation is then refined
     *         further (using only inliers) with the Levenberg-Marquardt method to reduce the re-projection error even
     *         more. Estimated transformation matrix is: cos(theta) cdot s &amp; -sin(theta) cdot s &amp; t_x \\
     *         sin(theta) cdot s &amp; cos(theta) cdot s &amp; t_y Where theta is the rotation angle, s the scaling
     *         factor and t_x, t_y are translations in x, y axes respectively. Note: The RANSAC method can handle
     *         practically any ratio of outliers but need a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but it works correctly only when there are more than 50% of inliers
     */
    private static native long estimateAffinePartial2D_3(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj,
            int method,
            double ransacReprojThreshold);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffinePartial2D(Mat,Mat,Mat,int)}.
     *
     * @param from_nativeObj    native matrix or object handle for {@code from}; First input 2D point set
     * @param to_nativeObj      native matrix or object handle for {@code to}; Second input 2D point set
     * @param inliers_nativeObj native matrix or object handle for {@code inliers}; Output vector indicating which
     *                          points are inliers
     * @param method            Robust method used to compute transformation. The following methods are possible: REF:
     *                          RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method RANSAC is
     *                          the default method
     * @return native binding handle for Output 2D affine transformation (4 degrees of freedom) matrix 2 times 3 or
     *         empty matrix if transformation could not be estimated. The function estimates an optimal 2D affine
     *         transformation with 4 degrees of freedom limited to combinations of translation, rotation, and uniform
     *         scaling. Uses the selected algorithm for robust estimation. The computed transformation is then refined
     *         further (using only inliers) with the Levenberg-Marquardt method to reduce the re-projection error even
     *         more. Estimated transformation matrix is: cos(theta) cdot s &amp; -sin(theta) cdot s &amp; t_x \\
     *         sin(theta) cdot s &amp; cos(theta) cdot s &amp; t_y Where theta is the rotation angle, s the scaling
     *         factor and t_x, t_y are translations in x, y axes respectively. Note: The RANSAC method can handle
     *         practically any ratio of outliers but need a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but it works correctly only when there are more than 50% of inliers
     */
    private static native long estimateAffinePartial2D_4(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj,
            int method);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffinePartial2D(Mat,Mat,Mat)}.
     *
     * @param from_nativeObj    native matrix or object handle for {@code from}; First input 2D point set
     * @param to_nativeObj      native matrix or object handle for {@code to}; Second input 2D point set
     * @param inliers_nativeObj native matrix or object handle for {@code inliers}; Output vector indicating which
     *                          points are inliers
     * @return native binding handle for Output 2D affine transformation (4 degrees of freedom) matrix 2 times 3 or
     *         empty matrix if transformation could not be estimated. The function estimates an optimal 2D affine
     *         transformation with 4 degrees of freedom limited to combinations of translation, rotation, and uniform
     *         scaling. Uses the selected algorithm for robust estimation. The computed transformation is then refined
     *         further (using only inliers) with the Levenberg-Marquardt method to reduce the re-projection error even
     *         more. Estimated transformation matrix is: cos(theta) cdot s &amp; -sin(theta) cdot s &amp; t_x \\
     *         sin(theta) cdot s &amp; cos(theta) cdot s &amp; t_y Where theta is the rotation angle, s the scaling
     *         factor and t_x, t_y are translations in x, y axes respectively. Note: The RANSAC method can handle
     *         practically any ratio of outliers but need a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but it works correctly only when there are more than 50% of inliers
     */
    private static native long estimateAffinePartial2D_5(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#estimateAffinePartial2D(Mat,Mat)}.
     *
     * @param from_nativeObj native matrix or object handle for {@code from}; First input 2D point set
     * @param to_nativeObj   native matrix or object handle for {@code to}; Second input 2D point set
     * @return native binding handle for Output 2D affine transformation (4 degrees of freedom) matrix 2 times 3 or
     *         empty matrix if transformation could not be estimated. The function estimates an optimal 2D affine
     *         transformation with 4 degrees of freedom limited to combinations of translation, rotation, and uniform
     *         scaling. Uses the selected algorithm for robust estimation. The computed transformation is then refined
     *         further (using only inliers) with the Levenberg-Marquardt method to reduce the re-projection error even
     *         more. Estimated transformation matrix is: cos(theta) cdot s &amp; -sin(theta) cdot s &amp; t_x \\
     *         sin(theta) cdot s &amp; cos(theta) cdot s &amp; t_y Where theta is the rotation angle, s the scaling
     *         factor and t_x, t_y are translations in x, y axes respectively. Note: The RANSAC method can handle
     *         practically any ratio of outliers but need a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but it works correctly only when there are more than 50% of inliers
     */
    private static native long estimateAffinePartial2D_6(long from_nativeObj, long to_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateTranslation2D(Mat,Mat,Mat,int,double,long,double,long)}.
     *
     * @param from_nativeObj        native matrix or object handle for {@code from}; First input 2D point set containing
     *                              (X,Y)
     * @param to_nativeObj          native matrix or object handle for {@code to}; Second input 2D point set containing
     *                              (x,y)
     * @param inliers_nativeObj     native matrix or object handle for {@code inliers}; Output vector indicating which
     *                              points are inliers (1-inlier, 0-outlier)
     * @param method                Robust method used to compute the transformation. The following methods are
     *                              possible: REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust
     *                              method RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @param confidence            Confidence level, between 0 and 1, for the estimated transformation. Anything
     *                              between 0.95 and 0.99 is usually good enough. Values too close to 1 can slow down
     *                              the estimation significantly. Values lower than 0.8u20130.9 can result in an
     *                              incorrectly estimated transformation
     * @param refineIters           Maximum number of iterations of the refining algorithm. For pure translation the
     *                              least-squares solution on inliers is closed-form, so passing 0 is recommended (no
     *                              additional refine)
     * @return A 2D translation vector [t_x, t_y]^T as . If the translation could not be estimated, both components are
     *         set to NaN and, if inliers is provided, the mask is filled with zeros. par Converting to a 2x3
     *         transformation matrix: 1 &amp; 0 &amp; t_x\\ 0 &amp; 1 &amp; t_y 2-component double vector t =
     *         estimateTranslation2D(from, to, inliers); Mat T = Mat_&lt;double&gt;({2,3}, {1,0,t[0], 0,1,t[1]}); The
     *         function estimates a pure 2D translation between two 2D point sets using the selected robust algorithm.
     *         Inliers are determined by the reprojection error threshold. Note: The RANSAC method can handle
     *         practically any ratio of outliers but needs a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but works correctly only when there are more than 50% inliers
     */
    private static native double[] estimateTranslation2D_0(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj,
            int method,
            double ransacReprojThreshold,
            long maxIters,
            double confidence,
            long refineIters);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateTranslation2D(Mat,Mat,Mat,int,double,long,double)}.
     *
     * @param from_nativeObj        native matrix or object handle for {@code from}; First input 2D point set containing
     *                              (X,Y)
     * @param to_nativeObj          native matrix or object handle for {@code to}; Second input 2D point set containing
     *                              (x,y)
     * @param inliers_nativeObj     native matrix or object handle for {@code inliers}; Output vector indicating which
     *                              points are inliers (1-inlier, 0-outlier)
     * @param method                Robust method used to compute the transformation. The following methods are
     *                              possible: REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust
     *                              method RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @param confidence            Confidence level, between 0 and 1, for the estimated transformation. Anything
     *                              between 0.95 and 0.99 is usually good enough. Values too close to 1 can slow down
     *                              the estimation significantly. Values lower than 0.8u20130.9 can result in an
     *                              incorrectly estimated transformation
     * @return A 2D translation vector [t_x, t_y]^T as . If the translation could not be estimated, both components are
     *         set to NaN and, if inliers is provided, the mask is filled with zeros. par Converting to a 2x3
     *         transformation matrix: 1 &amp; 0 &amp; t_x\\ 0 &amp; 1 &amp; t_y 2-component double vector t =
     *         estimateTranslation2D(from, to, inliers); Mat T = Mat_&lt;double&gt;({2,3}, {1,0,t[0], 0,1,t[1]}); The
     *         function estimates a pure 2D translation between two 2D point sets using the selected robust algorithm.
     *         Inliers are determined by the reprojection error threshold. Note: The RANSAC method can handle
     *         practically any ratio of outliers but needs a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but works correctly only when there are more than 50% inliers
     */
    private static native double[] estimateTranslation2D_1(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj,
            int method,
            double ransacReprojThreshold,
            long maxIters,
            double confidence);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateTranslation2D(Mat,Mat,Mat,int,double,long)}.
     *
     * @param from_nativeObj        native matrix or object handle for {@code from}; First input 2D point set containing
     *                              (X,Y)
     * @param to_nativeObj          native matrix or object handle for {@code to}; Second input 2D point set containing
     *                              (x,y)
     * @param inliers_nativeObj     native matrix or object handle for {@code inliers}; Output vector indicating which
     *                              points are inliers (1-inlier, 0-outlier)
     * @param method                Robust method used to compute the transformation. The following methods are
     *                              possible: REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust
     *                              method RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @param maxIters              The maximum number of robust method iterations
     * @return A 2D translation vector [t_x, t_y]^T as . If the translation could not be estimated, both components are
     *         set to NaN and, if inliers is provided, the mask is filled with zeros. par Converting to a 2x3
     *         transformation matrix: 1 &amp; 0 &amp; t_x\\ 0 &amp; 1 &amp; t_y 2-component double vector t =
     *         estimateTranslation2D(from, to, inliers); Mat T = Mat_&lt;double&gt;({2,3}, {1,0,t[0], 0,1,t[1]}); The
     *         function estimates a pure 2D translation between two 2D point sets using the selected robust algorithm.
     *         Inliers are determined by the reprojection error threshold. Note: The RANSAC method can handle
     *         practically any ratio of outliers but needs a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but works correctly only when there are more than 50% inliers
     */
    private static native double[] estimateTranslation2D_2(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj,
            int method,
            double ransacReprojThreshold,
            long maxIters);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#estimateTranslation2D(Mat,Mat,Mat,int,double)}.
     *
     * @param from_nativeObj        native matrix or object handle for {@code from}; First input 2D point set containing
     *                              (X,Y)
     * @param to_nativeObj          native matrix or object handle for {@code to}; Second input 2D point set containing
     *                              (x,y)
     * @param inliers_nativeObj     native matrix or object handle for {@code inliers}; Output vector indicating which
     *                              points are inliers (1-inlier, 0-outlier)
     * @param method                Robust method used to compute the transformation. The following methods are
     *                              possible: REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust
     *                              method RANSAC is the default method
     * @param ransacReprojThreshold Maximum reprojection error in the RANSAC algorithm to consider a point as an inlier.
     *                              Applies only to RANSAC
     * @return A 2D translation vector [t_x, t_y]^T as . If the translation could not be estimated, both components are
     *         set to NaN and, if inliers is provided, the mask is filled with zeros. par Converting to a 2x3
     *         transformation matrix: 1 &amp; 0 &amp; t_x\\ 0 &amp; 1 &amp; t_y 2-component double vector t =
     *         estimateTranslation2D(from, to, inliers); Mat T = Mat_&lt;double&gt;({2,3}, {1,0,t[0], 0,1,t[1]}); The
     *         function estimates a pure 2D translation between two 2D point sets using the selected robust algorithm.
     *         Inliers are determined by the reprojection error threshold. Note: The RANSAC method can handle
     *         practically any ratio of outliers but needs a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but works correctly only when there are more than 50% inliers
     */
    private static native double[] estimateTranslation2D_3(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj,
            int method,
            double ransacReprojThreshold);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#estimateTranslation2D(Mat,Mat,Mat,int)}.
     *
     * @param from_nativeObj    native matrix or object handle for {@code from}; First input 2D point set containing
     *                          (X,Y)
     * @param to_nativeObj      native matrix or object handle for {@code to}; Second input 2D point set containing
     *                          (x,y)
     * @param inliers_nativeObj native matrix or object handle for {@code inliers}; Output vector indicating which
     *                          points are inliers (1-inlier, 0-outlier)
     * @param method            Robust method used to compute the transformation. The following methods are possible:
     *                          REF: RANSAC - RANSAC-based robust method REF: LMEDS - Least-Median robust method RANSAC
     *                          is the default method
     * @return A 2D translation vector [t_x, t_y]^T as . If the translation could not be estimated, both components are
     *         set to NaN and, if inliers is provided, the mask is filled with zeros. par Converting to a 2x3
     *         transformation matrix: 1 &amp; 0 &amp; t_x\\ 0 &amp; 1 &amp; t_y 2-component double vector t =
     *         estimateTranslation2D(from, to, inliers); Mat T = Mat_&lt;double&gt;({2,3}, {1,0,t[0], 0,1,t[1]}); The
     *         function estimates a pure 2D translation between two 2D point sets using the selected robust algorithm.
     *         Inliers are determined by the reprojection error threshold. Note: The RANSAC method can handle
     *         practically any ratio of outliers but needs a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but works correctly only when there are more than 50% inliers
     */
    private static native double[] estimateTranslation2D_4(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj,
            int method);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#estimateTranslation2D(Mat,Mat,Mat)}.
     *
     * @param from_nativeObj    native matrix or object handle for {@code from}; First input 2D point set containing
     *                          (X,Y)
     * @param to_nativeObj      native matrix or object handle for {@code to}; Second input 2D point set containing
     *                          (x,y)
     * @param inliers_nativeObj native matrix or object handle for {@code inliers}; Output vector indicating which
     *                          points are inliers (1-inlier, 0-outlier)
     * @return A 2D translation vector [t_x, t_y]^T as . If the translation could not be estimated, both components are
     *         set to NaN and, if inliers is provided, the mask is filled with zeros. par Converting to a 2x3
     *         transformation matrix: 1 &amp; 0 &amp; t_x\\ 0 &amp; 1 &amp; t_y 2-component double vector t =
     *         estimateTranslation2D(from, to, inliers); Mat T = Mat_&lt;double&gt;({2,3}, {1,0,t[0], 0,1,t[1]}); The
     *         function estimates a pure 2D translation between two 2D point sets using the selected robust algorithm.
     *         Inliers are determined by the reprojection error threshold. Note: The RANSAC method can handle
     *         practically any ratio of outliers but needs a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but works correctly only when there are more than 50% inliers
     */
    private static native double[] estimateTranslation2D_5(
            long from_nativeObj,
            long to_nativeObj,
            long inliers_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#estimateTranslation2D(Mat,Mat)}.
     *
     * @param from_nativeObj native matrix or object handle for {@code from}; First input 2D point set containing (X,Y)
     * @param to_nativeObj   native matrix or object handle for {@code to}; Second input 2D point set containing (x,y)
     * @return A 2D translation vector [t_x, t_y]^T as . If the translation could not be estimated, both components are
     *         set to NaN and, if inliers is provided, the mask is filled with zeros. par Converting to a 2x3
     *         transformation matrix: 1 &amp; 0 &amp; t_x\\ 0 &amp; 1 &amp; t_y 2-component double vector t =
     *         estimateTranslation2D(from, to, inliers); Mat T = Mat_&lt;double&gt;({2,3}, {1,0,t[0], 0,1,t[1]}); The
     *         function estimates a pure 2D translation between two 2D point sets using the selected robust algorithm.
     *         Inliers are determined by the reprojection error threshold. Note: The RANSAC method can handle
     *         practically any ratio of outliers but needs a threshold to distinguish inliers from outliers. The method
     *         LMeDS does not need any threshold but works correctly only when there are more than 50% inliers
     */
    private static native double[] estimateTranslation2D_6(long from_nativeObj, long to_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#decomposeHomographyMat(Mat,Mat,List<Mat>,List<Mat>,List<Mat>)}.
     *
     * @param H_nativeObj                native matrix or object handle for {@code H}; The input homography matrix
     *                                   between two images
     * @param K_nativeObj                native matrix or object handle for {@code K}; The input camera intrinsic matrix
     * @param rotations_mat_nativeObj    native matrix or object handle for {@code rotations_mat}; Array of rotation
     *                                   matrices
     * @param translations_mat_nativeObj native matrix or object handle for {@code translations_mat}; Array of
     *                                   translation matrices
     * @param normals_mat_nativeObj      native matrix or object handle for {@code normals_mat}; Array of plane normal
     *                                   matrices. This function extracts relative camera motion between two views of a
     *                                   planar object and returns up to four mathematical solution tuples of rotation,
     *                                   translation, and plane normal. The decomposition of the homography matrix H is
     *                                   described in detail in If the homography H, induced by the plane, gives the
     *                                   constraint s_i vecthree{x'_i}{y'_i}{1} sim H vecthree{x_i}{y_i}{1} on the
     *                                   source image points p_i and the destination image points p'_i, then the tuple
     *                                   of rotations[k] and translations[k] is a change of basis from the source
     *                                   camera's coordinate system to the destination camera's coordinate system.
     *                                   However, by decomposing H, one can only get the translation normalized by the
     *                                   (typically unknown) depth of the scene, i.e. its direction but with normalized
     *                                   length. If point correspondences are available, at least two solutions may
     *                                   further be invalidated, by applying positive depth constraint, i.e. all points
     *                                   must be in front of the camera
     * @return the number of returned motion solutions
     */
    private static native int decomposeHomographyMat_0(
            long H_nativeObj,
            long K_nativeObj,
            long rotations_mat_nativeObj,
            long translations_mat_nativeObj,
            long normals_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#filterHomographyDecompByVisibleRefpoints(List<Mat>,List<Mat>,Mat,Mat,Mat,Mat)}.
     *
     * @param rotations_mat_nativeObj     native matrix or object handle for {@code rotations_mat}; Vector of rotation
     *                                    matrices
     * @param normals_mat_nativeObj       native matrix or object handle for {@code normals_mat}; Vector of plane normal
     *                                    matrices
     * @param beforePoints_nativeObj      native matrix or object handle for {@code beforePoints}; Vector of (rectified)
     *                                    visible reference points before the homography is applied
     * @param afterPoints_nativeObj       native matrix or object handle for {@code afterPoints}; Vector of (rectified)
     *                                    visible reference points after the homography is applied
     * @param possibleSolutions_nativeObj native matrix or object handle for {@code possibleSolutions}; Vector of int
     *                                    indices representing the viable solution set after filtering
     * @param pointsMask_nativeObj        native matrix or object handle for {@code pointsMask}; optional Mat/Vector of
     *                                    CV_8U, CV_8S or CV_Bool type representing the mask for the inliers as given by
     *                                    the findHomography function This function is intended to filter the output of
     *                                    the decomposeHomographyMat based on additional information as described in The
     *                                    summary of the method: the decomposeHomographyMat function returns 2 unique
     *                                    solutions and their "opposites" for a total of 4 solutions. If we have access
     *                                    to the sets of points visible in the camera frame before and after the
     *                                    homography transformation is applied, we can determine which are the true
     *                                    potential solutions and which are the opposites by verifying which
     *                                    homographies are consistent with all visible reference points being in front
     *                                    of the camera. The inputs are left unchanged; the filtered solution set is
     *                                    returned as indices into the existing one
     */
    private static native void filterHomographyDecompByVisibleRefpoints_0(
            long rotations_mat_nativeObj,
            long normals_mat_nativeObj,
            long beforePoints_nativeObj,
            long afterPoints_nativeObj,
            long possibleSolutions_nativeObj,
            long pointsMask_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#filterHomographyDecompByVisibleRefpoints(List<Mat>,List<Mat>,Mat,Mat,Mat)}.
     *
     * @param rotations_mat_nativeObj     native matrix or object handle for {@code rotations_mat}; Vector of rotation
     *                                    matrices
     * @param normals_mat_nativeObj       native matrix or object handle for {@code normals_mat}; Vector of plane normal
     *                                    matrices
     * @param beforePoints_nativeObj      native matrix or object handle for {@code beforePoints}; Vector of (rectified)
     *                                    visible reference points before the homography is applied
     * @param afterPoints_nativeObj       native matrix or object handle for {@code afterPoints}; Vector of (rectified)
     *                                    visible reference points after the homography is applied
     * @param possibleSolutions_nativeObj native matrix or object handle for {@code possibleSolutions}; Vector of int
     *                                    indices representing the viable solution set after filtering
     */
    private static native void filterHomographyDecompByVisibleRefpoints_1(
            long rotations_mat_nativeObj,
            long normals_mat_nativeObj,
            long beforePoints_nativeObj,
            long afterPoints_nativeObj,
            long possibleSolutions_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#calibrationMatrixValues(Mat,Size,double,double,double[],double[],double[],Point,double[])}.
     *
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix that can be estimated by calibrateCamera or stereoCalibrate
     * @param imageSize_width        {@code imageSize.width} component of Input image size in pixels
     * @param imageSize_height       {@code imageSize.height} component of Input image size in pixels
     * @param apertureWidth          Physical width in mm of the sensor
     * @param apertureHeight         Physical height in mm of the sensor
     * @param fovx_out               output array receiving {@code fovx} from the native call
     * @param fovy_out               output array receiving {@code fovy} from the native call
     * @param focalLength_out        output array receiving {@code focalLength} from the native call
     * @param principalPoint_out     output array receiving {@code principalPoint} from the native call
     * @param aspectRatio_out        output array receiving {@code aspectRatio} from the native call
     */
    private static native void calibrationMatrixValues_0(
            long cameraMatrix_nativeObj,
            double imageSize_width,
            double imageSize_height,
            double apertureWidth,
            double apertureHeight,
            double[] fovx_out,
            double[] fovy_out,
            double[] focalLength_out,
            double[] principalPoint_out,
            double[] aspectRatio_out);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#getDefaultNewCameraMatrix(Mat,Size,boolean)}.
     *
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera matrix
     * @param imgsize_width          {@code imgsize.width} component of Camera view image size in pixels
     * @param imgsize_height         {@code imgsize.height} component of Camera view image size in pixels
     * @param centerPrincipalPoint   Location of the principal point in the new camera matrix. The parameter indicates
     *                               whether this location should be at the image center or not
     * @return native binding handle for a copy of the camera matrix, with the principal point optionally moved to the
     *         image center
     */
    private static native long getDefaultNewCameraMatrix_0(
            long cameraMatrix_nativeObj,
            double imgsize_width,
            double imgsize_height,
            boolean centerPrincipalPoint);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#getDefaultNewCameraMatrix(Mat,Size)}.
     *
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera matrix
     * @param imgsize_width          {@code imgsize.width} component of Camera view image size in pixels
     * @param imgsize_height         {@code imgsize.height} component of Camera view image size in pixels
     * @return native binding handle for a copy of the camera matrix, with the principal point optionally moved to the
     *         image center
     */
    private static native long getDefaultNewCameraMatrix_1(
            long cameraMatrix_nativeObj,
            double imgsize_width,
            double imgsize_height);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#getDefaultNewCameraMatrix(Mat)}.
     *
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera matrix
     * @return native binding handle for a copy of the camera matrix, with the principal point optionally moved to the
     *         image center
     */
    private static native long getDefaultNewCameraMatrix_2(long cameraMatrix_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#getOptimalNewCameraMatrix(Mat,Mat,Size,double,Size,Rect,boolean)}.
     *
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is NULL/empty, the zero distortion
     *                               coefficients are assumed
     * @param imageSize_width        {@code imageSize.width} component of Original image size
     * @param imageSize_height       {@code imageSize.height} component of Original image size
     * @param alpha                  Free scaling parameter between 0 (when all the pixels in the undistorted image are
     *                               valid) and 1 (when all the source image pixels are retained in the undistorted
     *                               image). See stereoRectify for details
     * @param newImgSize_width       {@code newImgSize.width} component of Image size after rectification. By default,
     *                               it is set to imageSize
     * @param newImgSize_height      {@code newImgSize.height} component of Image size after rectification. By default,
     *                               it is set to imageSize
     * @param validPixROI_out        output array receiving {@code validPixROI} from the native call
     * @param centerPrincipalPoint   Optional flag that indicates whether in the new camera intrinsic matrix the
     *                               principal point should be at the image center or not. By default, the principal
     *                               point is chosen to best fit a subset of the source image (determined by alpha) to
     *                               the corrected image
     * @return native binding handle for new_camera_matrix Output new camera intrinsic matrix. The function computes and
     *         returns the optimal new camera intrinsic matrix based on the free scaling parameter. By varying this
     *         parameter, you may retrieve only sensible pixels alpha=0 , keep all the original image pixels if there is
     *         valuable information in the corners alpha=1 , or get something in between. When alpha&gt;0 , the
     *         undistorted result is likely to have some black pixels corresponding to "virtual" pixels outside of the
     *         captured distorted image. The original camera intrinsic matrix, distortion coefficients, the computed new
     *         camera intrinsic matrix, and newImageSize should be passed to initUndistortRectifyMap to produce the maps
     *         for remap
     */
    private static native long getOptimalNewCameraMatrix_0(
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            double imageSize_width,
            double imageSize_height,
            double alpha,
            double newImgSize_width,
            double newImgSize_height,
            double[] validPixROI_out,
            boolean centerPrincipalPoint);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#getOptimalNewCameraMatrix(Mat,Mat,Size,double,Size,Rect)}.
     *
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is NULL/empty, the zero distortion
     *                               coefficients are assumed
     * @param imageSize_width        {@code imageSize.width} component of Original image size
     * @param imageSize_height       {@code imageSize.height} component of Original image size
     * @param alpha                  Free scaling parameter between 0 (when all the pixels in the undistorted image are
     *                               valid) and 1 (when all the source image pixels are retained in the undistorted
     *                               image). See stereoRectify for details
     * @param newImgSize_width       {@code newImgSize.width} component of Image size after rectification. By default,
     *                               it is set to imageSize
     * @param newImgSize_height      {@code newImgSize.height} component of Image size after rectification. By default,
     *                               it is set to imageSize
     * @param validPixROI_out        output array receiving {@code validPixROI} from the native call
     * @return native binding handle for new_camera_matrix Output new camera intrinsic matrix. The function computes and
     *         returns the optimal new camera intrinsic matrix based on the free scaling parameter. By varying this
     *         parameter, you may retrieve only sensible pixels alpha=0 , keep all the original image pixels if there is
     *         valuable information in the corners alpha=1 , or get something in between. When alpha&gt;0 , the
     *         undistorted result is likely to have some black pixels corresponding to "virtual" pixels outside of the
     *         captured distorted image. The original camera intrinsic matrix, distortion coefficients, the computed new
     *         camera intrinsic matrix, and newImageSize should be passed to initUndistortRectifyMap to produce the maps
     *         for remap
     */
    private static native long getOptimalNewCameraMatrix_1(
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            double imageSize_width,
            double imageSize_height,
            double alpha,
            double newImgSize_width,
            double newImgSize_height,
            double[] validPixROI_out);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#getOptimalNewCameraMatrix(Mat,Mat,Size,double,Size)}.
     *
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is NULL/empty, the zero distortion
     *                               coefficients are assumed
     * @param imageSize_width        {@code imageSize.width} component of Original image size
     * @param imageSize_height       {@code imageSize.height} component of Original image size
     * @param alpha                  Free scaling parameter between 0 (when all the pixels in the undistorted image are
     *                               valid) and 1 (when all the source image pixels are retained in the undistorted
     *                               image). See stereoRectify for details
     * @param newImgSize_width       {@code newImgSize.width} component of Image size after rectification. By default,
     *                               it is set to imageSize
     * @param newImgSize_height      {@code newImgSize.height} component of Image size after rectification. By default,
     *                               it is set to imageSize
     * @return native binding handle for new_camera_matrix Output new camera intrinsic matrix. The function computes and
     *         returns the optimal new camera intrinsic matrix based on the free scaling parameter. By varying this
     *         parameter, you may retrieve only sensible pixels alpha=0 , keep all the original image pixels if there is
     *         valuable information in the corners alpha=1 , or get something in between. When alpha&gt;0 , the
     *         undistorted result is likely to have some black pixels corresponding to "virtual" pixels outside of the
     *         captured distorted image. The original camera intrinsic matrix, distortion coefficients, the computed new
     *         camera intrinsic matrix, and newImageSize should be passed to initUndistortRectifyMap to produce the maps
     *         for remap
     */
    private static native long getOptimalNewCameraMatrix_2(
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            double imageSize_width,
            double imageSize_height,
            double alpha,
            double newImgSize_width,
            double newImgSize_height);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#getOptimalNewCameraMatrix(Mat,Mat,Size,double)}.
     *
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is NULL/empty, the zero distortion
     *                               coefficients are assumed
     * @param imageSize_width        {@code imageSize.width} component of Original image size
     * @param imageSize_height       {@code imageSize.height} component of Original image size
     * @param alpha                  Free scaling parameter between 0 (when all the pixels in the undistorted image are
     *                               valid) and 1 (when all the source image pixels are retained in the undistorted
     *                               image). See stereoRectify for details
     * @return native binding handle for new_camera_matrix Output new camera intrinsic matrix. The function computes and
     *         returns the optimal new camera intrinsic matrix based on the free scaling parameter. By varying this
     *         parameter, you may retrieve only sensible pixels alpha=0 , keep all the original image pixels if there is
     *         valuable information in the corners alpha=1 , or get something in between. When alpha&gt;0 , the
     *         undistorted result is likely to have some black pixels corresponding to "virtual" pixels outside of the
     *         captured distorted image. The original camera intrinsic matrix, distortion coefficients, the computed new
     *         camera intrinsic matrix, and newImageSize should be passed to initUndistortRectifyMap to produce the maps
     *         for remap
     */
    private static native long getOptimalNewCameraMatrix_3(
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            double imageSize_width,
            double imageSize_height,
            double alpha);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#undistortPoints(MatOfPoint2f,MatOfPoint2f,Mat,Mat,Mat,Mat,TermCriteria)}.
     *
     * @param src_mat_nativeObj      native matrix or object handle for {@code src_mat}; Observed point coordinates in
     *                               pixel coordinates of the distorted image, 2xN/Nx2 1-channel or 1xN/Nx1 2-channel
     *                               (CV_32FC2 or CV_64FC2) (or vector&lt;Point2f&gt; )
     * @param dst_mat_nativeObj      native matrix or object handle for {@code dst_mat}; Output ideal point coordinates
     *                               (1xN/Nx1 2-channel or vector&lt;Point2f&gt; ) after undistortion and reverse
     *                               perspective transformation. If matrix P is identity or omitted, dst will contain
     *                               normalized point coordinates
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Camera matrix
     *                               vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1, s_2, s_3, s_4[,
     *                               tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is NULL/empty, the
     *                               zero distortion coefficients are assumed
     * @param R_nativeObj            native matrix or object handle for {@code R}; Rectification transformation in the
     *                               object space (3x3 matrix). R1 or R2 computed by stereoRectify can be passed here.
     *                               If the matrix is empty, the identity transformation is used
     * @param P_nativeObj            native matrix or object handle for {@code P}; New camera matrix (3x3) or new
     *                               projection matrix (3x4) {f'}_x &amp; 0 &amp; {c'}_x &amp; t_x \\ 0 &amp; {f'}_y
     *                               &amp; {c'}_y &amp; t_y \\ 0 &amp; 0 &amp; 1 &amp; t_z . P1 or P2 computed by
     *                               stereoRectify can be passed here. If the matrix is empty, the identity new camera
     *                               matrix is used and output will be in normalized coordinates
     * @param criteria_type          {@code criteria.type} component of Termination criteria for the iterative point
     *                               undistortion algorithm. The default runs a fixed 5 iterations, see the note above.
     *                               Keep TermCriteria::COUNT in the type when using TermCriteria::EPS: with EPS alone
     *                               there is no iteration limit
     * @param criteria_maxCount      {@code criteria.maxCount} component of Termination criteria for the iterative point
     *                               undistortion algorithm. The default runs a fixed 5 iterations, see the note above.
     *                               Keep TermCriteria::COUNT in the type when using TermCriteria::EPS: with EPS alone
     *                               there is no iteration limit
     * @param criteria_epsilon       {@code criteria.epsilon} component of Termination criteria for the iterative point
     *                               undistortion algorithm. The default runs a fixed 5 iterations, see the note above.
     *                               Keep TermCriteria::COUNT in the type when using TermCriteria::EPS: with EPS alone
     *                               there is no iteration limit
     */
    private static native void undistortPoints_0(
            long src_mat_nativeObj,
            long dst_mat_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long R_nativeObj,
            long P_nativeObj,
            int criteria_type,
            int criteria_maxCount,
            double criteria_epsilon);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#undistortPoints(MatOfPoint2f,MatOfPoint2f,Mat,Mat,Mat,Mat)}.
     *
     * @param src_mat_nativeObj      native matrix or object handle for {@code src_mat}; Observed point coordinates in
     *                               pixel coordinates of the distorted image, 2xN/Nx2 1-channel or 1xN/Nx1 2-channel
     *                               (CV_32FC2 or CV_64FC2) (or vector&lt;Point2f&gt; )
     * @param dst_mat_nativeObj      native matrix or object handle for {@code dst_mat}; Output ideal point coordinates
     *                               (1xN/Nx1 2-channel or vector&lt;Point2f&gt; ) after undistortion and reverse
     *                               perspective transformation. If matrix P is identity or omitted, dst will contain
     *                               normalized point coordinates
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Camera matrix
     *                               vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1, s_2, s_3, s_4[,
     *                               tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is NULL/empty, the
     *                               zero distortion coefficients are assumed
     * @param R_nativeObj            native matrix or object handle for {@code R}; Rectification transformation in the
     *                               object space (3x3 matrix). R1 or R2 computed by stereoRectify can be passed here.
     *                               If the matrix is empty, the identity transformation is used
     * @param P_nativeObj            native matrix or object handle for {@code P}; New camera matrix (3x3) or new
     *                               projection matrix (3x4) {f'}_x &amp; 0 &amp; {c'}_x &amp; t_x \\ 0 &amp; {f'}_y
     *                               &amp; {c'}_y &amp; t_y \\ 0 &amp; 0 &amp; 1 &amp; t_z . P1 or P2 computed by
     *                               stereoRectify can be passed here. If the matrix is empty, the identity new camera
     *                               matrix is used and output will be in normalized coordinates
     */
    private static native void undistortPoints_1(
            long src_mat_nativeObj,
            long dst_mat_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long R_nativeObj,
            long P_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#undistortPoints(MatOfPoint2f,MatOfPoint2f,Mat,Mat,Mat)}.
     *
     * @param src_mat_nativeObj      native matrix or object handle for {@code src_mat}; Observed point coordinates in
     *                               pixel coordinates of the distorted image, 2xN/Nx2 1-channel or 1xN/Nx1 2-channel
     *                               (CV_32FC2 or CV_64FC2) (or vector&lt;Point2f&gt; )
     * @param dst_mat_nativeObj      native matrix or object handle for {@code dst_mat}; Output ideal point coordinates
     *                               (1xN/Nx1 2-channel or vector&lt;Point2f&gt; ) after undistortion and reverse
     *                               perspective transformation. If matrix P is identity or omitted, dst will contain
     *                               normalized point coordinates
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Camera matrix
     *                               vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1, s_2, s_3, s_4[,
     *                               tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is NULL/empty, the
     *                               zero distortion coefficients are assumed
     * @param R_nativeObj            native matrix or object handle for {@code R}; Rectification transformation in the
     *                               object space (3x3 matrix). R1 or R2 computed by stereoRectify can be passed here.
     *                               If the matrix is empty, the identity transformation is used
     */
    private static native void undistortPoints_2(
            long src_mat_nativeObj,
            long dst_mat_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long R_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#undistortPoints(MatOfPoint2f,MatOfPoint2f,Mat,Mat)}.
     *
     * @param src_mat_nativeObj      native matrix or object handle for {@code src_mat}; Observed point coordinates in
     *                               pixel coordinates of the distorted image, 2xN/Nx2 1-channel or 1xN/Nx1 2-channel
     *                               (CV_32FC2 or CV_64FC2) (or vector&lt;Point2f&gt; )
     * @param dst_mat_nativeObj      native matrix or object handle for {@code dst_mat}; Output ideal point coordinates
     *                               (1xN/Nx1 2-channel or vector&lt;Point2f&gt; ) after undistortion and reverse
     *                               perspective transformation. If matrix P is identity or omitted, dst will contain
     *                               normalized point coordinates
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Camera matrix
     *                               vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1, s_2, s_3, s_4[,
     *                               tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is NULL/empty, the
     *                               zero distortion coefficients are assumed
     */
    private static native void undistortPoints_3(
            long src_mat_nativeObj,
            long dst_mat_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#undistortImagePoints(Mat,Mat,Mat,Mat,TermCriteria)}.
     *
     * @param src_nativeObj          native matrix or object handle for {@code src}; Observed points position, 2xN/Nx2
     *                               1-channel or 1xN/Nx1 2-channel (CV_32FC2 or CV_64FC2) (or vector&lt;Point2f&gt; )
     * @param dst_nativeObj          native matrix or object handle for {@code dst}; Output undistorted points position
     *                               (1xN/Nx1 2-channel or vector&lt;Point2f&gt; )
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Camera matrix
     *                               vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Distortion coefficients
     * @param criteria_type          {@code criteria.type} component of Termination criteria for the iterative
     *                               undistortion, as in undistortPoints. The default runs a fixed 5 iterations and
     *                               ignores epsilon
     * @param criteria_maxCount      {@code criteria.maxCount} component of Termination criteria for the iterative
     *                               undistortion, as in undistortPoints. The default runs a fixed 5 iterations and
     *                               ignores epsilon
     * @param criteria_epsilon       {@code criteria.epsilon} component of Termination criteria for the iterative
     *                               undistortion, as in undistortPoints. The default runs a fixed 5 iterations and
     *                               ignores epsilon
     */
    private static native void undistortImagePoints_0(
            long src_nativeObj,
            long dst_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            int criteria_type,
            int criteria_maxCount,
            double criteria_epsilon);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#undistortImagePoints(Mat,Mat,Mat,Mat)}.
     *
     * @param src_nativeObj          native matrix or object handle for {@code src}; Observed points position, 2xN/Nx2
     *                               1-channel or 1xN/Nx1 2-channel (CV_32FC2 or CV_64FC2) (or vector&lt;Point2f&gt; )
     * @param dst_nativeObj          native matrix or object handle for {@code dst}; Output undistorted points position
     *                               (1xN/Nx1 2-channel or vector&lt;Point2f&gt; )
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Camera matrix
     *                               vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Distortion coefficients
     */
    private static native void undistortImagePoints_1(
            long src_nativeObj,
            long dst_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_projectPoints(Mat,Mat,Mat,Mat,Mat,Mat,double,Mat)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; three-dimensional object
     *                               coordinates expressed in the object reference frame
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; two-dimensional coordinates
     *                               in the image plane
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; rotation vector in Rodrigues form
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; translation vector between object
     *                               and camera coordinates
     * @param K_nativeObj            native matrix or object handle for {@code K}; number of nearest neighbors retained
     *                               for each query row
     * @param D_nativeObj            native matrix or object handle for {@code D}; fisheye distortion coefficient vector
     * @param alpha                  fisheye camera skew coefficient
     * @param jacobian_nativeObj     native matrix or object handle for {@code jacobian}; output matrix of derivatives
     *                               of the projected image coordinates
     */
    private static native void fisheye_projectPoints_0(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            long K_nativeObj,
            long D_nativeObj,
            double alpha,
            long jacobian_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_projectPoints(Mat,Mat,Mat,Mat,Mat,Mat,double)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; three-dimensional object
     *                               coordinates expressed in the object reference frame
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; two-dimensional coordinates
     *                               in the image plane
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; rotation vector in Rodrigues form
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; translation vector between object
     *                               and camera coordinates
     * @param K_nativeObj            native matrix or object handle for {@code K}; number of nearest neighbors retained
     *                               for each query row
     * @param D_nativeObj            native matrix or object handle for {@code D}; fisheye distortion coefficient vector
     * @param alpha                  fisheye camera skew coefficient
     */
    private static native void fisheye_projectPoints_1(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            long K_nativeObj,
            long D_nativeObj,
            double alpha);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_projectPoints(Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; three-dimensional object
     *                               coordinates expressed in the object reference frame
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; two-dimensional coordinates
     *                               in the image plane
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; rotation vector in Rodrigues form
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; translation vector between object
     *                               and camera coordinates
     * @param K_nativeObj            native matrix or object handle for {@code K}; number of nearest neighbors retained
     *                               for each query row
     * @param D_nativeObj            native matrix or object handle for {@code D}; fisheye distortion coefficient vector
     */
    private static native void fisheye_projectPoints_2(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            long K_nativeObj,
            long D_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_distortPoints(Mat,Mat,Mat,Mat,double)}.
     *
     * @param undistorted_nativeObj native matrix or object handle for {@code undistorted}; Array of object points,
     *                              1xN/Nx1 2-channel (or vector&lt;Point2f&gt; ), where N is the number of points in
     *                              the view
     * @param distorted_nativeObj   native matrix or object handle for {@code distorted}; Output array of image points,
     *                              1xN/Nx1 2-channel, or vector&lt;Point2f&gt;
     * @param K_nativeObj           native matrix or object handle for {@code K}; Camera intrinsic matrix
     *                              cameramatrix{K}
     * @param D_nativeObj           native matrix or object handle for {@code D}; Input vector of distortion
     *                              coefficients distcoeffsfisheye
     * @param alpha                 The skew coefficient
     */
    private static native void fisheye_distortPoints_0(
            long undistorted_nativeObj,
            long distorted_nativeObj,
            long K_nativeObj,
            long D_nativeObj,
            double alpha);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_distortPoints(Mat,Mat,Mat,Mat)}.
     *
     * @param undistorted_nativeObj native matrix or object handle for {@code undistorted}; Array of object points,
     *                              1xN/Nx1 2-channel (or vector&lt;Point2f&gt; ), where N is the number of points in
     *                              the view
     * @param distorted_nativeObj   native matrix or object handle for {@code distorted}; Output array of image points,
     *                              1xN/Nx1 2-channel, or vector&lt;Point2f&gt;
     * @param K_nativeObj           native matrix or object handle for {@code K}; Camera intrinsic matrix
     *                              cameramatrix{K}
     * @param D_nativeObj           native matrix or object handle for {@code D}; Input vector of distortion
     *                              coefficients distcoeffsfisheye
     */
    private static native void fisheye_distortPoints_1(
            long undistorted_nativeObj,
            long distorted_nativeObj,
            long K_nativeObj,
            long D_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_distortPoints(Mat,Mat,Mat,Mat,Mat,double)}.
     *
     * @param undistorted_nativeObj  native matrix or object handle for {@code undistorted}; Array of object points,
     *                               1xN/Nx1 2-channel (or vector&lt;Point2f&gt; ), where N is the number of points in
     *                               the view
     * @param distorted_nativeObj    native matrix or object handle for {@code distorted}; Output array of image points,
     *                               1xN/Nx1 2-channel, or vector&lt;Point2f&gt;
     * @param Kundistorted_nativeObj native matrix or object handle for {@code Kundistorted}; Camera intrinsic matrix
     *                               used as new camera matrix for undistortion
     * @param K_nativeObj            native matrix or object handle for {@code K}; Camera intrinsic matrix
     *                               cameramatrix{K}
     * @param D_nativeObj            native matrix or object handle for {@code D}; Input vector of distortion
     *                               coefficients distcoeffsfisheye
     * @param alpha                  The skew coefficient
     */
    private static native void fisheye_distortPoints_2(
            long undistorted_nativeObj,
            long distorted_nativeObj,
            long Kundistorted_nativeObj,
            long K_nativeObj,
            long D_nativeObj,
            double alpha);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_distortPoints(Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param undistorted_nativeObj  native matrix or object handle for {@code undistorted}; Array of object points,
     *                               1xN/Nx1 2-channel (or vector&lt;Point2f&gt; ), where N is the number of points in
     *                               the view
     * @param distorted_nativeObj    native matrix or object handle for {@code distorted}; Output array of image points,
     *                               1xN/Nx1 2-channel, or vector&lt;Point2f&gt;
     * @param Kundistorted_nativeObj native matrix or object handle for {@code Kundistorted}; Camera intrinsic matrix
     *                               used as new camera matrix for undistortion
     * @param K_nativeObj            native matrix or object handle for {@code K}; Camera intrinsic matrix
     *                               cameramatrix{K}
     * @param D_nativeObj            native matrix or object handle for {@code D}; Input vector of distortion
     *                               coefficients distcoeffsfisheye
     */
    private static native void fisheye_distortPoints_3(
            long undistorted_nativeObj,
            long distorted_nativeObj,
            long Kundistorted_nativeObj,
            long K_nativeObj,
            long D_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_undistortPoints(Mat,Mat,Mat,Mat,Mat,Mat,TermCriteria)}.
     *
     * @param distorted_nativeObj   native matrix or object handle for {@code distorted}; Array of distorted point
     *                              coordinates in pixel coordinates of the fisheye image, 1xN/Nx1 2-channel (or
     *                              vector&lt;Point2f&gt; ), where N is the number of points in the view
     * @param undistorted_nativeObj native matrix or object handle for {@code undistorted}; output coordinates after
     *                              removing lens distortion
     * @param K_nativeObj           native matrix or object handle for {@code K}; Camera intrinsic matrix
     *                              cameramatrix{K} of the fisheye camera
     * @param D_nativeObj           native matrix or object handle for {@code D}; Input vector of fisheye distortion
     *                              coefficients distcoeffsfisheye (must contain exactly 4 coefficients)
     * @param R_nativeObj           native matrix or object handle for {@code R}; Rectification transformation in the
     *                              object space: 3x3 1-channel, or vector: 3x1/1x3 1-channel or 1x1 3-channel. If
     *                              empty, the identity transformation is used
     * @param P_nativeObj           native matrix or object handle for {@code P}; New camera intrinsic matrix (3x3) or
     *                              new projection matrix (3x4). If empty or identity, output will be in normalized
     *                              camera coordinates
     * @param criteria_type         {@code criteria.type} component of Termination criteria for the iterative
     *                              undistortion algorithm
     * @param criteria_maxCount     {@code criteria.maxCount} component of Termination criteria for the iterative
     *                              undistortion algorithm
     * @param criteria_epsilon      {@code criteria.epsilon} component of Termination criteria for the iterative
     *                              undistortion algorithm
     */
    private static native void fisheye_undistortPoints_0(
            long distorted_nativeObj,
            long undistorted_nativeObj,
            long K_nativeObj,
            long D_nativeObj,
            long R_nativeObj,
            long P_nativeObj,
            int criteria_type,
            int criteria_maxCount,
            double criteria_epsilon);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_undistortPoints(Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param distorted_nativeObj   native matrix or object handle for {@code distorted}; Array of distorted point
     *                              coordinates in pixel coordinates of the fisheye image, 1xN/Nx1 2-channel (or
     *                              vector&lt;Point2f&gt; ), where N is the number of points in the view
     * @param undistorted_nativeObj native matrix or object handle for {@code undistorted}; output coordinates after
     *                              removing lens distortion
     * @param K_nativeObj           native matrix or object handle for {@code K}; Camera intrinsic matrix
     *                              cameramatrix{K} of the fisheye camera
     * @param D_nativeObj           native matrix or object handle for {@code D}; Input vector of fisheye distortion
     *                              coefficients distcoeffsfisheye (must contain exactly 4 coefficients)
     * @param R_nativeObj           native matrix or object handle for {@code R}; Rectification transformation in the
     *                              object space: 3x3 1-channel, or vector: 3x1/1x3 1-channel or 1x1 3-channel. If
     *                              empty, the identity transformation is used
     * @param P_nativeObj           native matrix or object handle for {@code P}; New camera intrinsic matrix (3x3) or
     *                              new projection matrix (3x4). If empty or identity, output will be in normalized
     *                              camera coordinates
     */
    private static native void fisheye_undistortPoints_1(
            long distorted_nativeObj,
            long undistorted_nativeObj,
            long K_nativeObj,
            long D_nativeObj,
            long R_nativeObj,
            long P_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_undistortPoints(Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param distorted_nativeObj   native matrix or object handle for {@code distorted}; Array of distorted point
     *                              coordinates in pixel coordinates of the fisheye image, 1xN/Nx1 2-channel (or
     *                              vector&lt;Point2f&gt; ), where N is the number of points in the view
     * @param undistorted_nativeObj native matrix or object handle for {@code undistorted}; output coordinates after
     *                              removing lens distortion
     * @param K_nativeObj           native matrix or object handle for {@code K}; Camera intrinsic matrix
     *                              cameramatrix{K} of the fisheye camera
     * @param D_nativeObj           native matrix or object handle for {@code D}; Input vector of fisheye distortion
     *                              coefficients distcoeffsfisheye (must contain exactly 4 coefficients)
     * @param R_nativeObj           native matrix or object handle for {@code R}; Rectification transformation in the
     *                              object space: 3x3 1-channel, or vector: 3x1/1x3 1-channel or 1x1 3-channel. If
     *                              empty, the identity transformation is used
     */
    private static native void fisheye_undistortPoints_2(
            long distorted_nativeObj,
            long undistorted_nativeObj,
            long K_nativeObj,
            long D_nativeObj,
            long R_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_undistortPoints(Mat,Mat,Mat,Mat)}.
     *
     * @param distorted_nativeObj   native matrix or object handle for {@code distorted}; Array of distorted point
     *                              coordinates in pixel coordinates of the fisheye image, 1xN/Nx1 2-channel (or
     *                              vector&lt;Point2f&gt; ), where N is the number of points in the view
     * @param undistorted_nativeObj native matrix or object handle for {@code undistorted}; output coordinates after
     *                              removing lens distortion
     * @param K_nativeObj           native matrix or object handle for {@code K}; Camera intrinsic matrix
     *                              cameramatrix{K} of the fisheye camera
     * @param D_nativeObj           native matrix or object handle for {@code D}; Input vector of fisheye distortion
     *                              coefficients distcoeffsfisheye (must contain exactly 4 coefficients)
     */
    private static native void fisheye_undistortPoints_3(
            long distorted_nativeObj,
            long undistorted_nativeObj,
            long K_nativeObj,
            long D_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_estimateNewCameraMatrixForUndistortRectify(Mat,Mat,Size,Mat,Mat,double,Size,double)}.
     *
     * @param K_nativeObj       native matrix or object handle for {@code K}; Camera intrinsic matrix cameramatrix{K}
     * @param D_nativeObj       native matrix or object handle for {@code D}; Input vector of distortion coefficients
     *                          distcoeffsfisheye
     * @param image_size_width  {@code image_size.width} component of Size of the image
     * @param image_size_height {@code image_size.height} component of Size of the image
     * @param R_nativeObj       native matrix or object handle for {@code R}; Rectification transformation in the object
     *                          space: 3x3 1-channel, or vector: 3x1/1x3 1-channel or 1x1 3-channel
     * @param P_nativeObj       native matrix or object handle for {@code P}; New camera intrinsic matrix (3x3) or new
     *                          projection matrix (3x4)
     * @param balance           Sets the new focal length in range between the min focal length and the max focal
     *                          length. Balance is in range of [0, 1]
     * @param new_size_width    {@code new_size.width} component of the new size
     * @param new_size_height   {@code new_size.height} component of the new size
     * @param fov_scale         Divisor for new focal length
     */
    private static native void fisheye_estimateNewCameraMatrixForUndistortRectify_0(
            long K_nativeObj,
            long D_nativeObj,
            double image_size_width,
            double image_size_height,
            long R_nativeObj,
            long P_nativeObj,
            double balance,
            double new_size_width,
            double new_size_height,
            double fov_scale);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_estimateNewCameraMatrixForUndistortRectify(Mat,Mat,Size,Mat,Mat,double,Size)}.
     *
     * @param K_nativeObj       native matrix or object handle for {@code K}; Camera intrinsic matrix cameramatrix{K}
     * @param D_nativeObj       native matrix or object handle for {@code D}; Input vector of distortion coefficients
     *                          distcoeffsfisheye
     * @param image_size_width  {@code image_size.width} component of Size of the image
     * @param image_size_height {@code image_size.height} component of Size of the image
     * @param R_nativeObj       native matrix or object handle for {@code R}; Rectification transformation in the object
     *                          space: 3x3 1-channel, or vector: 3x1/1x3 1-channel or 1x1 3-channel
     * @param P_nativeObj       native matrix or object handle for {@code P}; New camera intrinsic matrix (3x3) or new
     *                          projection matrix (3x4)
     * @param balance           Sets the new focal length in range between the min focal length and the max focal
     *                          length. Balance is in range of [0, 1]
     * @param new_size_width    {@code new_size.width} component of the new size
     * @param new_size_height   {@code new_size.height} component of the new size
     */
    private static native void fisheye_estimateNewCameraMatrixForUndistortRectify_1(
            long K_nativeObj,
            long D_nativeObj,
            double image_size_width,
            double image_size_height,
            long R_nativeObj,
            long P_nativeObj,
            double balance,
            double new_size_width,
            double new_size_height);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_estimateNewCameraMatrixForUndistortRectify(Mat,Mat,Size,Mat,Mat,double)}.
     *
     * @param K_nativeObj       native matrix or object handle for {@code K}; Camera intrinsic matrix cameramatrix{K}
     * @param D_nativeObj       native matrix or object handle for {@code D}; Input vector of distortion coefficients
     *                          distcoeffsfisheye
     * @param image_size_width  {@code image_size.width} component of Size of the image
     * @param image_size_height {@code image_size.height} component of Size of the image
     * @param R_nativeObj       native matrix or object handle for {@code R}; Rectification transformation in the object
     *                          space: 3x3 1-channel, or vector: 3x1/1x3 1-channel or 1x1 3-channel
     * @param P_nativeObj       native matrix or object handle for {@code P}; New camera intrinsic matrix (3x3) or new
     *                          projection matrix (3x4)
     * @param balance           Sets the new focal length in range between the min focal length and the max focal
     *                          length. Balance is in range of [0, 1]
     */
    private static native void fisheye_estimateNewCameraMatrixForUndistortRectify_2(
            long K_nativeObj,
            long D_nativeObj,
            double image_size_width,
            double image_size_height,
            long R_nativeObj,
            long P_nativeObj,
            double balance);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_estimateNewCameraMatrixForUndistortRectify(Mat,Mat,Size,Mat,Mat)}.
     *
     * @param K_nativeObj       native matrix or object handle for {@code K}; Camera intrinsic matrix cameramatrix{K}
     * @param D_nativeObj       native matrix or object handle for {@code D}; Input vector of distortion coefficients
     *                          distcoeffsfisheye
     * @param image_size_width  {@code image_size.width} component of Size of the image
     * @param image_size_height {@code image_size.height} component of Size of the image
     * @param R_nativeObj       native matrix or object handle for {@code R}; Rectification transformation in the object
     *                          space: 3x3 1-channel, or vector: 3x1/1x3 1-channel or 1x1 3-channel
     * @param P_nativeObj       native matrix or object handle for {@code P}; New camera intrinsic matrix (3x3) or new
     *                          projection matrix (3x4)
     */
    private static native void fisheye_estimateNewCameraMatrixForUndistortRectify_3(
            long K_nativeObj,
            long D_nativeObj,
            double image_size_width,
            double image_size_height,
            long R_nativeObj,
            long P_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_solvePnP(Mat,Mat,Mat,Mat,Mat,Mat,boolean,int,TermCriteria)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can also be passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (4x1/1x4)
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Output rotation vector (see REF:
     *                               Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess      Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                               rvec and tvec values as initial approximations of the rotation and translation
     *                               vectors, respectively, and further optimizes them
     * @param flags                  Method for solving a PnP problem: see REF: calib3d_solvePnP_flags This function
     *                               returns the rotation and the translation vectors that transform a 3D point
     *                               expressed in the object coordinate frame to the camera coordinate frame, using
     *                               different methods: P3P methods (REF: SOLVEPNP_P3P, REF: SOLVEPNP_AP3P): need 4
     *                               input points to return a unique solution. REF: SOLVEPNP_IPPE Input points must be
     *                               &gt;= 4 and object points must be coplanar. REF: SOLVEPNP_IPPE_SQUARE Special case
     *                               suitable for marker pose estimation. Number of input points must be 4. Object
     *                               points must be defined in the following order: point 0: [-squareLength / 2,
     *                               squareLength / 2, 0] point 1: [ squareLength / 2, squareLength / 2, 0] point 2: [
     *                               squareLength / 2, -squareLength / 2, 0] point 3: [-squareLength / 2, -squareLength
     *                               / 2, 0] for all the other flags, number of input points must be &gt;= 4 and object
     *                               points can be in any configuration
     * @param criteria_type          {@code criteria.type} component of Termination criteria for internal
     *                               undistortPoints call. The function internally undistorts points with REF:
     *                               undistortPoints and call REF: solvePnP, thus the input are very similar. Check
     *                               there and Perspective-n-Points is described in REF: calib3d_solvePnP for more
     *                               information
     * @param criteria_maxCount      {@code criteria.maxCount} component of Termination criteria for internal
     *                               undistortPoints call. The function internally undistorts points with REF:
     *                               undistortPoints and call REF: solvePnP, thus the input are very similar. Check
     *                               there and Perspective-n-Points is described in REF: calib3d_solvePnP for more
     *                               information
     * @param criteria_epsilon       {@code criteria.epsilon} component of Termination criteria for internal
     *                               undistortPoints call. The function internally undistorts points with REF:
     *                               undistortPoints and call REF: solvePnP, thus the input are very similar. Check
     *                               there and Perspective-n-Points is described in REF: calib3d_solvePnP for more
     *                               information
     * @return true if a pose is found with the fisheye model
     */
    private static native boolean fisheye_solvePnP_0(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess,
            int flags,
            int criteria_type,
            int criteria_maxCount,
            double criteria_epsilon);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_solvePnP(Mat,Mat,Mat,Mat,Mat,Mat,boolean,int)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can also be passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (4x1/1x4)
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Output rotation vector (see REF:
     *                               Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess      Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                               rvec and tvec values as initial approximations of the rotation and translation
     *                               vectors, respectively, and further optimizes them
     * @param flags                  Method for solving a PnP problem: see REF: calib3d_solvePnP_flags This function
     *                               returns the rotation and the translation vectors that transform a 3D point
     *                               expressed in the object coordinate frame to the camera coordinate frame, using
     *                               different methods: P3P methods (REF: SOLVEPNP_P3P, REF: SOLVEPNP_AP3P): need 4
     *                               input points to return a unique solution. REF: SOLVEPNP_IPPE Input points must be
     *                               &gt;= 4 and object points must be coplanar. REF: SOLVEPNP_IPPE_SQUARE Special case
     *                               suitable for marker pose estimation. Number of input points must be 4. Object
     *                               points must be defined in the following order: point 0: [-squareLength / 2,
     *                               squareLength / 2, 0] point 1: [ squareLength / 2, squareLength / 2, 0] point 2: [
     *                               squareLength / 2, -squareLength / 2, 0] point 3: [-squareLength / 2, -squareLength
     *                               / 2, 0] for all the other flags, number of input points must be &gt;= 4 and object
     *                               points can be in any configuration
     * @return true if a pose is found with the fisheye model
     */
    private static native boolean fisheye_solvePnP_1(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess,
            int flags);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_solvePnP(Mat,Mat,Mat,Mat,Mat,Mat,boolean)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can also be passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (4x1/1x4)
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Output rotation vector (see REF:
     *                               Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess      Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                               rvec and tvec values as initial approximations of the rotation and translation
     *                               vectors, respectively, and further optimizes them
     * @return true if a pose is found with the fisheye model
     */
    private static native boolean fisheye_solvePnP_2(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_solvePnP(Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can also be passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can also be passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (4x1/1x4)
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Output rotation vector (see REF:
     *                               Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Output translation vector
     * @return true if a pose is found with the fisheye model
     */
    private static native boolean fisheye_solvePnP_3(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_solvePnPRansac(Mat,Mat,Mat,Mat,Mat,Mat,boolean,int,float,double,Mat,int,TermCriteria)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (4x1/1x4)
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Output rotation vector (see REF:
     *                               Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess      Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                               rvec and tvec values as initial approximations of the rotation and translation
     *                               vectors, respectively, and further optimizes them
     * @param iterationsCount        Number of iterations
     * @param reprojectionError      reprojection-error threshold used to classify inliers
     * @param confidence             The probability that the algorithm produces a useful result
     * @param inliers_nativeObj      native matrix or object handle for {@code inliers}; Output vector that contains
     *                               indices of inliers in objectPoints and imagePoints
     * @param flags                  Method for solving a PnP problem: see REF: calib3d_solvePnP_flags
     * @param criteria_type          {@code criteria.type} component of Termination criteria for internal
     *                               undistortPoints call. The function interally undistorts points with REF:
     *                               undistortPoints and call REF: solvePnP, thus the input are very similar. More
     *                               information about Perspective-n-Points is described in REF: calib3d_solvePnP for
     *                               more information
     * @param criteria_maxCount      {@code criteria.maxCount} component of Termination criteria for internal
     *                               undistortPoints call. The function interally undistorts points with REF:
     *                               undistortPoints and call REF: solvePnP, thus the input are very similar. More
     *                               information about Perspective-n-Points is described in REF: calib3d_solvePnP for
     *                               more information
     * @param criteria_epsilon       {@code criteria.epsilon} component of Termination criteria for internal
     *                               undistortPoints call. The function interally undistorts points with REF:
     *                               undistortPoints and call REF: solvePnP, thus the input are very similar. More
     *                               information about Perspective-n-Points is described in REF: calib3d_solvePnP for
     *                               more information
     * @return true if robust pose estimation succeeds with the fisheye model
     */
    private static native boolean fisheye_solvePnPRansac_0(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError,
            double confidence,
            long inliers_nativeObj,
            int flags,
            int criteria_type,
            int criteria_maxCount,
            double criteria_epsilon);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_solvePnPRansac(Mat,Mat,Mat,Mat,Mat,Mat,boolean,int,float,double,Mat,int)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (4x1/1x4)
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Output rotation vector (see REF:
     *                               Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess      Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                               rvec and tvec values as initial approximations of the rotation and translation
     *                               vectors, respectively, and further optimizes them
     * @param iterationsCount        Number of iterations
     * @param reprojectionError      reprojection-error threshold used to classify inliers
     * @param confidence             The probability that the algorithm produces a useful result
     * @param inliers_nativeObj      native matrix or object handle for {@code inliers}; Output vector that contains
     *                               indices of inliers in objectPoints and imagePoints
     * @param flags                  Method for solving a PnP problem: see REF: calib3d_solvePnP_flags
     * @return true if robust pose estimation succeeds with the fisheye model
     */
    private static native boolean fisheye_solvePnPRansac_1(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError,
            double confidence,
            long inliers_nativeObj,
            int flags);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_solvePnPRansac(Mat,Mat,Mat,Mat,Mat,Mat,boolean,int,float,double,Mat)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (4x1/1x4)
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Output rotation vector (see REF:
     *                               Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess      Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                               rvec and tvec values as initial approximations of the rotation and translation
     *                               vectors, respectively, and further optimizes them
     * @param iterationsCount        Number of iterations
     * @param reprojectionError      reprojection-error threshold used to classify inliers
     * @param confidence             The probability that the algorithm produces a useful result
     * @param inliers_nativeObj      native matrix or object handle for {@code inliers}; Output vector that contains
     *                               indices of inliers in objectPoints and imagePoints
     * @return true if robust pose estimation succeeds with the fisheye model
     */
    private static native boolean fisheye_solvePnPRansac_2(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError,
            double confidence,
            long inliers_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_solvePnPRansac(Mat,Mat,Mat,Mat,Mat,Mat,boolean,int,float,double)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (4x1/1x4)
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Output rotation vector (see REF:
     *                               Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess      Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                               rvec and tvec values as initial approximations of the rotation and translation
     *                               vectors, respectively, and further optimizes them
     * @param iterationsCount        Number of iterations
     * @param reprojectionError      reprojection-error threshold used to classify inliers
     * @param confidence             The probability that the algorithm produces a useful result
     * @return true if robust pose estimation succeeds with the fisheye model
     */
    private static native boolean fisheye_solvePnPRansac_3(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError,
            double confidence);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_solvePnPRansac(Mat,Mat,Mat,Mat,Mat,Mat,boolean,int,float)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (4x1/1x4)
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Output rotation vector (see REF:
     *                               Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess      Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                               rvec and tvec values as initial approximations of the rotation and translation
     *                               vectors, respectively, and further optimizes them
     * @param iterationsCount        Number of iterations
     * @param reprojectionError      reprojection-error threshold used to classify inliers
     * @return true if robust pose estimation succeeds with the fisheye model
     */
    private static native boolean fisheye_solvePnPRansac_4(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess,
            int iterationsCount,
            float reprojectionError);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_solvePnPRansac(Mat,Mat,Mat,Mat,Mat,Mat,boolean,int)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (4x1/1x4)
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Output rotation vector (see REF:
     *                               Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess      Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                               rvec and tvec values as initial approximations of the rotation and translation
     *                               vectors, respectively, and further optimizes them
     * @param iterationsCount        Number of iterations
     * @return true if robust pose estimation succeeds with the fisheye model
     */
    private static native boolean fisheye_solvePnPRansac_5(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess,
            int iterationsCount);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_solvePnPRansac(Mat,Mat,Mat,Mat,Mat,Mat,boolean)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (4x1/1x4)
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Output rotation vector (see REF:
     *                               Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Output translation vector
     * @param useExtrinsicGuess      Parameter used for SOLVEPNP_ITERATIVE. If true (1), the function uses the provided
     *                               rvec and tvec values as initial approximations of the rotation and translation
     *                               vectors, respectively, and further optimizes them
     * @return true if robust pose estimation succeeds with the fisheye model
     */
    private static native boolean fisheye_solvePnPRansac_6(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            boolean useExtrinsicGuess);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Geometry#fisheye_solvePnPRansac(Mat,Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param objectPoints_nativeObj native matrix or object handle for {@code objectPoints}; Array of object points in
     *                               the object coordinate space, Nx3 1-channel or 1xN/Nx1 3-channel, where N is the
     *                               number of points. vector&lt;Point3d&gt; can be also passed here
     * @param imagePoints_nativeObj  native matrix or object handle for {@code imagePoints}; Array of corresponding
     *                               image points, Nx2 1-channel or 1xN/Nx1 2-channel, where N is the number of points.
     *                               vector&lt;Point2d&gt; can be also passed here
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera intrinsic
     *                               matrix cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (4x1/1x4)
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Output rotation vector (see REF:
     *                               Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Output translation vector
     * @return true if robust pose estimation succeeds with the fisheye model
     */
    private static native boolean fisheye_solvePnPRansac_7(
            long objectPoints_nativeObj,
            long imagePoints_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj);

}
