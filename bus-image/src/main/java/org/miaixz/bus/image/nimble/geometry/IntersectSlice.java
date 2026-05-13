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
package org.miaixz.bus.image.nimble.geometry;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Projects a source slice intersection onto a localizer slice.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class IntersectSlice extends LocalizerPoster {

    /**
     * Creates a new instance.
     *
     * @param row          the row.
     * @param column       the column.
     * @param tlhc         the tlhc.
     * @param voxelSpacing the voxel spacing.
     * @param dimensions   the dimensions.
     */
    public IntersectSlice(Vector3 row, Vector3 column, Vector3 tlhc, Vector3 voxelSpacing, Vector3 dimensions) {
        super(row, column, tlhc, voxelSpacing, dimensions);
    }

    /**
     * Creates a new instance.
     *
     * @param geometry the geometry.
     */
    public IntersectSlice(GeometryOfSlice geometry) {
        super(geometry);
    }

    /**
     * Executes the all true operation.
     *
     * @param array the array.
     * @return true if the all true condition is true; otherwise false.
     */
    private static boolean allTrue(boolean[] array) {
        for (boolean value : array) {
            if (!value) {
                return false;
            }
        }
        return true;
    }

    /**
     * Executes the opposite edges operation.
     *
     * @param array the array.
     * @return true if the opposite edges condition is true; otherwise false.
     */
    private static boolean oppositeEdges(boolean[] array) {
        return array[0] && array[2] || array[1] && array[3];
    }

    /**
     * Executes the adjacent edges operation.
     *
     * @param array the array.
     * @return true if the adjacent edges condition is true; otherwise false.
     */
    private static boolean adjacentEdges(boolean[] array) {
        return array[0] && array[1] || array[1] && array[2] || array[2] && array[3] || array[3] && array[0];
    }

    /**
     * Executes the classify corners of rectangle into edges crossing z plane operation.
     *
     * @param corners the corners.
     * @return the operation result.
     */
    private static boolean[] classifyCornersOfRectangleIntoEdgesCrossingZPlane(Vector3[] corners) {
        int size = corners.length;
        boolean[] classification = new boolean[size];
        for (int i = 0; i < size; ++i) {
            int next = (i == size - 1) ? 0 : i + 1;
            classification[i] = classifyCornersIntoEdgeCrossingZPlane(corners[i], corners[next]);
        }
        return classification;
    }

    /**
     * Returns the outline on localizer for this geometry.
     *
     * @param row            the row.
     * @param column         the column.
     * @param tlhc           the tlhc.
     * @param voxelSpacing   the voxel spacing.
     * @param sliceThickness the slice thickness.
     * @param dimensions     the dimensions.
     * @return the outline on localizer for this geometry.
     */
    @Override
    public List<Point2D> getOutlineOnLocalizerForThisGeometry(
            Vector3 row,
            Vector3 column,
            Vector3 tlhc,
            Vector3 voxelSpacing,
            double sliceThickness,
            Vector3 dimensions) {
        Vector3[] corners = getCornersOfSourceRectangleInSourceSpace(row, column, tlhc, voxelSpacing, dimensions);
        for (int i = 0; i < 4; ++i) {
            corners[i] = transformPointFromSourceSpaceIntoLocalizerSpace(corners[i]);
        }
        boolean[] edges = classifyCornersOfRectangleIntoEdgesCrossingZPlane(corners);
        if (allTrue(edges)) {
            return drawOutlineOnLocalizer(corners);
        }
        if (oppositeEdges(edges) || adjacentEdges(edges)) {
            return drawLinesBetweenAnyPointsWhichIntersectPlaneWhereZIsZero(corners);
        }
        return null;
    }

}
