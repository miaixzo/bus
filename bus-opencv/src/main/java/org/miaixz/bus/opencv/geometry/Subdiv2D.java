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
import org.miaixz.bus.opencv.core.MatOfFloat4;
import org.miaixz.bus.opencv.core.MatOfFloat6;
import org.miaixz.bus.opencv.core.MatOfInt;
import org.miaixz.bus.opencv.core.MatOfPoint2f;
import org.miaixz.bus.opencv.core.Point;
import org.miaixz.bus.opencv.core.Rect;
import org.miaixz.bus.opencv.utils.Converters;

/**
 * A two-dimensional subdivision used for Delaunay triangulation and Voronoi diagrams.
 */
public class Subdiv2D {

    /**
     * Opaque native handle retained by this wrapper and passed to JNI operations.
     */
    protected final long nativeObj;

    /**
     * Wraps an existing native Subdiv2D handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected Subdiv2D(long addr) {
        nativeObj = addr;
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
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
     * Creates a Java wrapper for an existing native handle. This bridge is intended for binding and subclass
     * integration; the address must identify the native handle expected by this wrapper.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     * @return a new Java wrapper that retains the supplied native binding handle for cleanup
     */
    public static Subdiv2D __fromPtr__(long addr) {
        return new Subdiv2D(addr);
    }

    /**
     * Point-location result indicating that the subdivision lookup failed.
     */
    public static final int PTLOC_ERROR = -2;

    /**
     * Point outside the subdivision bounding rect.
     */
    public static final int PTLOC_OUTSIDE_RECT = -1;

    /**
     * Point inside some facet.
     */
    public static final int PTLOC_INSIDE = 0;

    /**
     * Point coincides with one of the subdivision vertices.
     */
    public static final int PTLOC_VERTEX = 1;

    /**
     * Point on some edge.
     */
    public static final int PTLOC_ON_EDGE = 2;

    /**
     * Traverse to the next directed subdivision edge around the origin vertex.
     */
    public static final int NEXT_AROUND_ORG = 0;

    /**
     * Traverse to the next directed subdivision edge around the destination vertex.
     */
    public static final int NEXT_AROUND_DST = 34;

    /**
     * Traverse to the previous directed subdivision edge around the origin vertex.
     */
    public static final int PREV_AROUND_ORG = 17;

    /**
     * Traverse to the previous directed subdivision edge around the destination vertex.
     */
    public static final int PREV_AROUND_DST = 51;

    /**
     * Traverse to the next directed subdivision edge around the left face.
     */
    public static final int NEXT_AROUND_LEFT = 19;

    /**
     * Traverse to the next directed subdivision edge around the right face.
     */
    public static final int NEXT_AROUND_RIGHT = 49;

    /**
     * Traverse to the previous directed subdivision edge around the left face.
     */
    public static final int PREV_AROUND_LEFT = 32;

    /**
     * Traverse to the previous directed subdivision edge around the right face.
     */
    public static final int PREV_AROUND_RIGHT = 2;

    /**
     * Creates an empty Subdiv2D value.
     */
    public Subdiv2D() {
        nativeObj = Subdiv2D_0();
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Creates a configured Subdiv2D value using the supplied values.
     *
     * @param rect Rectangle that includes all of the 2D points that are to be added to the subdivision. The function
     *             creates an empty Delaunay subdivision where 2D points can be added using the function insert() . All
     *             of the points to be added must be within the specified rectangle, otherwise a runtime error is raised
     * @throws NullPointerException if rect is null
     */
    public Subdiv2D(Rect rect) {
        nativeObj = Subdiv2D_1(rect.x, rect.y, rect.width, rect.height);
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Creates a new empty Delaunay subdivision.
     *
     * @param rect Rectangle that includes all of the 2D points that are to be added to the subdivision
     * @throws NullPointerException if rect is null
     */
    public void initDelaunay(Rect rect) {
        initDelaunay_0(nativeObj, rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Insert a single point into a Delaunay triangulation.
     *
     * @param pt Point to insert. The function inserts a single point into a subdivision and modifies the subdivision
     *           topology appropriately. If a point with the same coordinates exists already, no new point is added
     * @return the ID of the point. Note: If the point is outside of the triangulation specified rect a runtime error is
     *         raised
     * @throws NullPointerException if pt is null
     */
    public int insert(Point pt) {
        return insert_0(nativeObj, pt.x, pt.y);
    }

    /**
     * Insert a single point into a Delaunay triangulation.
     *
     * @param ptvec Points to insert. The function inserts a vector of points into a subdivision and modifies the
     *              subdivision topology appropriately
     */
    public void insert(MatOfPoint2f ptvec) {
        Mat ptvec_mat = ptvec;
        insert_1(nativeObj, ptvec_mat.nativeObj);
    }

    /**
     * Returns the location of a point within a Delaunay triangulation.
     *
     * @param pt     Point to locate
     * @param edge   encoded directed subdivision edge identifier
     * @param vertex Optional output vertex the input point coincides with. The function locates the input point within
     *               the subdivision and gives one of the triangle edges or vertices
     * @return an integer which specify one of the following five cases for point location: - The point falls into some
     *         facet. The function returns PTLOC_INSIDE and edge will contain one of edges of the facet. - The point
     *         falls onto the edge. The function returns PTLOC_ON_EDGE and edge will contain this edge. - The point
     *         coincides with one of the subdivision vertices. The function returns PTLOC_VERTEX and vertex will contain
     *         a pointer to the vertex. - The point is outside the subdivision reference rectangle. The function returns
     *         PTLOC_OUTSIDE_RECT and no pointers are filled. - One of input arguments is invalid. A runtime error is
     *         raised or, if silent or "parent" error processing mode is selected, PTLOC_ERROR is returned
     * @throws NullPointerException if pt is null
     */
    public int locate(Point pt, int[] edge, int[] vertex) {
        double[] edge_out = new double[1];
        double[] vertex_out = new double[1];
        int retVal = locate_0(nativeObj, pt.x, pt.y, edge_out, vertex_out);
        if (edge != null)
            edge[0] = (int) edge_out[0];
        if (vertex != null)
            vertex[0] = (int) vertex_out[0];
        return retVal;
    }

    /**
     * Finds the subdivision vertex closest to the given point.
     *
     * @param pt        Input point
     * @param nearestPt Output subdivision vertex point. The function is another function that locates the input point
     *                  within the subdivision. It finds the subdivision vertex that is the closest to the input point.
     *                  It is not necessarily one of vertices of the facet containing the input point, though the facet
     *                  (located using locate() ) is used as a starting point
     * @return vertex ID
     * @throws NullPointerException if pt is null
     */
    public int findNearest(Point pt, Point nearestPt) {
        double[] nearestPt_out = new double[2];
        int retVal = findNearest_0(nativeObj, pt.x, pt.y, nearestPt_out);
        if (nearestPt != null) {
            nearestPt.x = nearestPt_out[0];
            nearestPt.y = nearestPt_out[1];
        }
        return retVal;
    }

    /**
     * Finds the subdivision vertex closest to the given point.
     *
     * @param pt Input point
     * @return vertex ID
     * @throws NullPointerException if pt is null
     */
    public int findNearest(Point pt) {
        return findNearest_1(nativeObj, pt.x, pt.y);
    }

    /**
     * Returns a list of all edges.
     *
     * @param edgeList Output vector. The function gives each edge as a 4 numbers vector, where each two are one of the
     *                 edge vertices. i.e. org_x = v[0], org_y = v[1], dst_x = v[2], dst_y = v[3]
     */
    public void getEdgeList(MatOfFloat4 edgeList) {
        Mat edgeList_mat = edgeList;
        getEdgeList_0(nativeObj, edgeList_mat.nativeObj);
    }

    /**
     * Returns a list of the leading edge ID connected to each triangle.
     *
     * @param leadingEdgeList Output vector. The function gives one edge ID for each triangle
     */
    public void getLeadingEdgeList(MatOfInt leadingEdgeList) {
        Mat leadingEdgeList_mat = leadingEdgeList;
        getLeadingEdgeList_0(nativeObj, leadingEdgeList_mat.nativeObj);
    }

    /**
     * Returns a list of all triangles.
     *
     * @param triangleList Output vector. The function gives each triangle as a 6 numbers vector, where each two are one
     *                     of the triangle vertices. i.e. p1_x = v[0], p1_y = v[1], p2_x = v[2], p2_y = v[3], p3_x =
     *                     v[4], p3_y = v[5]
     */
    public void getTriangleList(MatOfFloat6 triangleList) {
        Mat triangleList_mat = triangleList;
        getTriangleList_0(nativeObj, triangleList_mat.nativeObj);
    }

    /**
     * Returns a list of all Voronoi facets.
     *
     * @param idx          Vector of vertices IDs to consider. For all vertices you can pass empty vector
     * @param facetList    Output vector of the Voronoi facets
     * @param facetCenters Output vector of the Voronoi facets center points
     */
    public void getVoronoiFacetList(MatOfInt idx, List<MatOfPoint2f> facetList, MatOfPoint2f facetCenters) {
        Mat idx_mat = idx;
        Mat facetList_mat = new Mat();
        Mat facetCenters_mat = facetCenters;
        getVoronoiFacetList_0(nativeObj, idx_mat.nativeObj, facetList_mat.nativeObj, facetCenters_mat.nativeObj);
        Converters.Mat_to_vector_vector_Point2f(facetList_mat, facetList);
        facetList_mat.release();
    }

    /**
     * Returns vertex location from vertex ID.
     *
     * @param vertex    vertex ID
     * @param firstEdge Optional. The first edge ID which is connected to the vertex
     * @return vertex (x,y)
     */
    public Point getVertex(int vertex, int[] firstEdge) {
        double[] firstEdge_out = new double[1];
        Point retVal = new Point(getVertex_0(nativeObj, vertex, firstEdge_out));
        if (firstEdge != null)
            firstEdge[0] = (int) firstEdge_out[0];
        return retVal;
    }

    /**
     * Returns vertex location from vertex ID.
     *
     * @param vertex vertex ID
     * @return vertex (x,y)
     */
    public Point getVertex(int vertex) {
        return new Point(getVertex_1(nativeObj, vertex));
    }

    /**
     * Returns one of the edges related to the given edge.
     *
     * @param edge         encoded directed subdivision edge identifier
     * @param nextEdgeType Parameter specifying which of the related edges to return. The following values are possible:
     *                     - NEXT_AROUND_ORG next around the edge origin ( eOnext on the picture below if e is the input
     *                     edge) - NEXT_AROUND_DST next around the edge vertex ( eDnext ) - PREV_AROUND_ORG previous
     *                     around the edge origin (reversed eRnext ) - PREV_AROUND_DST previous around the edge
     *                     destination (reversed eLnext ) - NEXT_AROUND_LEFT next around the left facet ( eLnext ) -
     *                     NEXT_AROUND_RIGHT next around the right facet ( eRnext ) - PREV_AROUND_LEFT previous around
     *                     the left facet (reversed eOnext ) - PREV_AROUND_RIGHT previous around the right facet
     *                     (reversed eDnext )
     * @return edge ID related to the input edge
     */
    public int getEdge(int edge, int nextEdgeType) {
        return getEdge_0(nativeObj, edge, nextEdgeType);
    }

    /**
     * Returns next edge around the edge origin.
     *
     * @param edge encoded directed subdivision edge identifier
     * @return an integer which is next edge ID around the edge origin: eOnext on the picture above if e is the input
     *         edge)
     */
    public int nextEdge(int edge) {
        return nextEdge_0(nativeObj, edge);
    }

    /**
     * Returns another edge of the same quad-edge.
     *
     * @param edge   encoded directed subdivision edge identifier
     * @param rotate Parameter specifying which of the edges of the same quad-edge as the input one to return. The
     *               following values are possible: - 0 - the input edge ( e on the picture below if e is the input
     *               edge) - 1 - the rotated edge ( eRot ) - 2 - the reversed edge (reversed e (in green)) - 3 - the
     *               reversed rotated edge (reversed eRot (in green))
     * @return one of the edges ID of the same quad-edge as the input edge
     */
    public int rotateEdge(int edge, int rotate) {
        return rotateEdge_0(nativeObj, edge, rotate);
    }

    /**
     * Returns the directed edge opposite to the supplied directed subdivision edge.
     *
     * @param edge encoded directed subdivision edge identifier
     * @return the same geometric edge with the opposite direction
     */
    public int symEdge(int edge) {
        return symEdge_0(nativeObj, edge);
    }

    /**
     * Returns the edge origin.
     *
     * @param edge  encoded directed subdivision edge identifier
     * @param orgpt Output vertex location
     * @return vertex ID
     */
    public int edgeOrg(int edge, Point orgpt) {
        double[] orgpt_out = new double[2];
        int retVal = edgeOrg_0(nativeObj, edge, orgpt_out);
        if (orgpt != null) {
            orgpt.x = orgpt_out[0];
            orgpt.y = orgpt_out[1];
        }
        return retVal;
    }

    /**
     * Returns the edge origin.
     *
     * @param edge encoded directed subdivision edge identifier
     * @return vertex ID
     */
    public int edgeOrg(int edge) {
        return edgeOrg_1(nativeObj, edge);
    }

    /**
     * Returns the edge destination.
     *
     * @param edge  encoded directed subdivision edge identifier
     * @param dstpt Output vertex location
     * @return vertex ID
     */
    public int edgeDst(int edge, Point dstpt) {
        double[] dstpt_out = new double[2];
        int retVal = edgeDst_0(nativeObj, edge, dstpt_out);
        if (dstpt != null) {
            dstpt.x = dstpt_out[0];
            dstpt.y = dstpt_out[1];
        }
        return retVal;
    }

    /**
     * Returns the edge destination.
     *
     * @param edge encoded directed subdivision edge identifier
     * @return vertex ID
     */
    public int edgeDst(int edge) {
        return edgeDst_1(nativeObj, edge);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#Subdiv2D()}.
     *
     * @return a newly allocated native {@code Subdiv2D} binding handle
     */
    private static native long Subdiv2D_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#Subdiv2D(Rect)}.
     *
     * @param rect_x      {@code rect.x} component of Rectangle that includes all of the 2D points that are to be added
     *                    to the subdivision. The function creates an empty Delaunay subdivision where 2D points can be
     *                    added using the function insert() . All of the points to be added must be within the specified
     *                    rectangle, otherwise a runtime error is raised
     * @param rect_y      {@code rect.y} component of Rectangle that includes all of the 2D points that are to be added
     *                    to the subdivision. The function creates an empty Delaunay subdivision where 2D points can be
     *                    added using the function insert() . All of the points to be added must be within the specified
     *                    rectangle, otherwise a runtime error is raised
     * @param rect_width  {@code rect.width} component of Rectangle that includes all of the 2D points that are to be
     *                    added to the subdivision. The function creates an empty Delaunay subdivision where 2D points
     *                    can be added using the function insert() . All of the points to be added must be within the
     *                    specified rectangle, otherwise a runtime error is raised
     * @param rect_height {@code rect.height} component of Rectangle that includes all of the 2D points that are to be
     *                    added to the subdivision. The function creates an empty Delaunay subdivision where 2D points
     *                    can be added using the function insert() . All of the points to be added must be within the
     *                    specified rectangle, otherwise a runtime error is raised
     * @return a newly allocated native {@code Subdiv2D} binding handle
     */
    private static native long Subdiv2D_1(int rect_x, int rect_y, int rect_width, int rect_height);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#initDelaunay(Rect)}.
     *
     * @param nativeObj   native binding handle of this {@code Subdiv2D} instance
     * @param rect_x      {@code rect.x} component of Rectangle that includes all of the 2D points that are to be added
     *                    to the subdivision
     * @param rect_y      {@code rect.y} component of Rectangle that includes all of the 2D points that are to be added
     *                    to the subdivision
     * @param rect_width  {@code rect.width} component of Rectangle that includes all of the 2D points that are to be
     *                    added to the subdivision
     * @param rect_height {@code rect.height} component of Rectangle that includes all of the 2D points that are to be
     *                    added to the subdivision
     */
    private static native void initDelaunay_0(long nativeObj, int rect_x, int rect_y, int rect_width, int rect_height);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#insert(Point)}.
     *
     * @param nativeObj native binding handle of this {@code Subdiv2D} instance
     * @param pt_x      {@code pt.x} component of Point to insert. The function inserts a single point into a
     *                  subdivision and modifies the subdivision topology appropriately. If a point with the same
     *                  coordinates exists already, no new point is added
     * @param pt_y      {@code pt.y} component of Point to insert. The function inserts a single point into a
     *                  subdivision and modifies the subdivision topology appropriately. If a point with the same
     *                  coordinates exists already, no new point is added
     * @return the ID of the point. Note: If the point is outside of the triangulation specified rect a runtime error is
     *         raised
     */
    private static native int insert_0(long nativeObj, double pt_x, double pt_y);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#insert(MatOfPoint2f)}.
     *
     * @param nativeObj           native binding handle of this {@code Subdiv2D} instance
     * @param ptvec_mat_nativeObj native matrix or object handle for {@code ptvec_mat}; Points to insert. The function
     *                            inserts a vector of points into a subdivision and modifies the subdivision topology
     *                            appropriately
     */
    private static native void insert_1(long nativeObj, long ptvec_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#locate(Point,int[],int[])}.
     *
     * @param nativeObj  native binding handle of this {@code Subdiv2D} instance
     * @param pt_x       {@code pt.x} component of Point to locate
     * @param pt_y       {@code pt.y} component of Point to locate
     * @param edge_out   output array receiving {@code edge} from the native call
     * @param vertex_out output array receiving {@code vertex} from the native call
     * @return an integer which specify one of the following five cases for point location: - The point falls into some
     *         facet. The function returns PTLOC_INSIDE and edge will contain one of edges of the facet. - The point
     *         falls onto the edge. The function returns PTLOC_ON_EDGE and edge will contain this edge. - The point
     *         coincides with one of the subdivision vertices. The function returns PTLOC_VERTEX and vertex will contain
     *         a pointer to the vertex. - The point is outside the subdivision reference rectangle. The function returns
     *         PTLOC_OUTSIDE_RECT and no pointers are filled. - One of input arguments is invalid. A runtime error is
     *         raised or, if silent or "parent" error processing mode is selected, PTLOC_ERROR is returned
     */
    private static native int locate_0(
            long nativeObj,
            double pt_x,
            double pt_y,
            double[] edge_out,
            double[] vertex_out);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#findNearest(Point,Point)}.
     *
     * @param nativeObj     native binding handle of this {@code Subdiv2D} instance
     * @param pt_x          {@code pt.x} component of Input point
     * @param pt_y          {@code pt.y} component of Input point
     * @param nearestPt_out output array receiving {@code nearestPt} from the native call
     * @return vertex ID
     */
    private static native int findNearest_0(long nativeObj, double pt_x, double pt_y, double[] nearestPt_out);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#findNearest(Point)}.
     *
     * @param nativeObj native binding handle of this {@code Subdiv2D} instance
     * @param pt_x      {@code pt.x} component of Input point
     * @param pt_y      {@code pt.y} component of Input point
     * @return vertex ID
     */
    private static native int findNearest_1(long nativeObj, double pt_x, double pt_y);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#getEdgeList(MatOfFloat4)}.
     *
     * @param nativeObj              native binding handle of this {@code Subdiv2D} instance
     * @param edgeList_mat_nativeObj native matrix or object handle for {@code edgeList_mat}; Output vector. The
     *                               function gives each edge as a 4 numbers vector, where each two are one of the edge
     *                               vertices. i.e. org_x = v[0], org_y = v[1], dst_x = v[2], dst_y = v[3]
     */
    private static native void getEdgeList_0(long nativeObj, long edgeList_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#getLeadingEdgeList(MatOfInt)}.
     *
     * @param nativeObj                     native binding handle of this {@code Subdiv2D} instance
     * @param leadingEdgeList_mat_nativeObj native matrix or object handle for {@code leadingEdgeList_mat}; Output
     *                                      vector. The function gives one edge ID for each triangle
     */
    private static native void getLeadingEdgeList_0(long nativeObj, long leadingEdgeList_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#getTriangleList(MatOfFloat6)}.
     *
     * @param nativeObj                  native binding handle of this {@code Subdiv2D} instance
     * @param triangleList_mat_nativeObj native matrix or object handle for {@code triangleList_mat}; Output vector. The
     *                                   function gives each triangle as a 6 numbers vector, where each two are one of
     *                                   the triangle vertices. i.e. p1_x = v[0], p1_y = v[1], p2_x = v[2], p2_y = v[3],
     *                                   p3_x = v[4], p3_y = v[5]
     */
    private static native void getTriangleList_0(long nativeObj, long triangleList_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.geometry.Subdiv2D#getVoronoiFacetList(MatOfInt,List<MatOfPoint2f>,MatOfPoint2f)}.
     *
     * @param nativeObj                  native binding handle of this {@code Subdiv2D} instance
     * @param idx_mat_nativeObj          native matrix or object handle for {@code idx_mat}; Vector of vertices IDs to
     *                                   consider. For all vertices you can pass empty vector
     * @param facetList_mat_nativeObj    native matrix or object handle for {@code facetList_mat}; Output vector of the
     *                                   Voronoi facets
     * @param facetCenters_mat_nativeObj native matrix or object handle for {@code facetCenters_mat}; Output vector of
     *                                   the Voronoi facets center points
     */
    private static native void getVoronoiFacetList_0(
            long nativeObj,
            long idx_mat_nativeObj,
            long facetList_mat_nativeObj,
            long facetCenters_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#getVertex(int,int[])}.
     *
     * @param nativeObj     native binding handle of this {@code Subdiv2D} instance
     * @param vertex        vertex ID
     * @param firstEdge_out output array receiving {@code firstEdge} from the native call
     * @return packed numeric fields used by the Java wrapper to construct {@code Point}
     */
    private static native double[] getVertex_0(long nativeObj, int vertex, double[] firstEdge_out);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#getVertex(int)}.
     *
     * @param nativeObj native binding handle of this {@code Subdiv2D} instance
     * @param vertex    vertex ID
     * @return packed numeric fields used by the Java wrapper to construct {@code Point}
     */
    private static native double[] getVertex_1(long nativeObj, int vertex);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#getEdge(int,int)}.
     *
     * @param nativeObj    native binding handle of this {@code Subdiv2D} instance
     * @param edge         encoded directed subdivision edge identifier
     * @param nextEdgeType Parameter specifying which of the related edges to return. The following values are possible:
     *                     - NEXT_AROUND_ORG next around the edge origin ( eOnext on the picture below if e is the input
     *                     edge) - NEXT_AROUND_DST next around the edge vertex ( eDnext ) - PREV_AROUND_ORG previous
     *                     around the edge origin (reversed eRnext ) - PREV_AROUND_DST previous around the edge
     *                     destination (reversed eLnext ) - NEXT_AROUND_LEFT next around the left facet ( eLnext ) -
     *                     NEXT_AROUND_RIGHT next around the right facet ( eRnext ) - PREV_AROUND_LEFT previous around
     *                     the left facet (reversed eOnext ) - PREV_AROUND_RIGHT previous around the right facet
     *                     (reversed eDnext )
     * @return edge ID related to the input edge
     */
    private static native int getEdge_0(long nativeObj, int edge, int nextEdgeType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#nextEdge(int)}.
     *
     * @param nativeObj native binding handle of this {@code Subdiv2D} instance
     * @param edge      encoded directed subdivision edge identifier
     * @return an integer which is next edge ID around the edge origin: eOnext on the picture above if e is the input
     *         edge)
     */
    private static native int nextEdge_0(long nativeObj, int edge);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#rotateEdge(int,int)}.
     *
     * @param nativeObj native binding handle of this {@code Subdiv2D} instance
     * @param edge      encoded directed subdivision edge identifier
     * @param rotate    Parameter specifying which of the edges of the same quad-edge as the input one to return. The
     *                  following values are possible: - 0 - the input edge ( e on the picture below if e is the input
     *                  edge) - 1 - the rotated edge ( eRot ) - 2 - the reversed edge (reversed e (in green)) - 3 - the
     *                  reversed rotated edge (reversed eRot (in green))
     * @return one of the edges ID of the same quad-edge as the input edge
     */
    private static native int rotateEdge_0(long nativeObj, int edge, int rotate);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#symEdge(int)}.
     *
     * @param nativeObj native binding handle of this {@code Subdiv2D} instance
     * @param edge      encoded directed subdivision edge identifier
     * @return the same geometric edge with the opposite direction
     */
    private static native int symEdge_0(long nativeObj, int edge);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#edgeOrg(int,Point)}.
     *
     * @param nativeObj native binding handle of this {@code Subdiv2D} instance
     * @param edge      encoded directed subdivision edge identifier
     * @param orgpt_out output array receiving {@code orgpt} from the native call
     * @return vertex ID
     */
    private static native int edgeOrg_0(long nativeObj, int edge, double[] orgpt_out);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#edgeOrg(int)}.
     *
     * @param nativeObj native binding handle of this {@code Subdiv2D} instance
     * @param edge      encoded directed subdivision edge identifier
     * @return vertex ID
     */
    private static native int edgeOrg_1(long nativeObj, int edge);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#edgeDst(int,Point)}.
     *
     * @param nativeObj native binding handle of this {@code Subdiv2D} instance
     * @param edge      encoded directed subdivision edge identifier
     * @param dstpt_out output array receiving {@code dstpt} from the native call
     * @return vertex ID
     */
    private static native int edgeDst_0(long nativeObj, int edge, double[] dstpt_out);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.Subdiv2D#edgeDst(int)}.
     *
     * @param nativeObj native binding handle of this {@code Subdiv2D} instance
     * @param edge      encoded directed subdivision edge identifier
     * @return vertex ID
     */
    private static native int edgeDst_1(long nativeObj, int edge);

    /**
     * Deletes the native binding handle after the owning Java wrapper becomes unreachable.
     *
     * @param nativeObj native binding handle of this {@code Subdiv2D} instance
     */
    private static native void delete(long nativeObj);

}
