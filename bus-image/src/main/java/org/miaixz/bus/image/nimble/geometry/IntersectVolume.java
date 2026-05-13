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
import java.util.ArrayList;
import java.util.List;

/**
 * Projects a source volume intersection onto a localizer slice.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class IntersectVolume extends LocalizerPoster {

    /**
     * Creates a new instance.
     *
     * @param row          the row.
     * @param column       the column.
     * @param tlhc         the tlhc.
     * @param voxelSpacing the voxel spacing.
     * @param dimensions   the dimensions.
     */
    public IntersectVolume(Vector3 row, Vector3 column, Vector3 tlhc, Vector3 voxelSpacing, Vector3 dimensions) {
        super(row, column, tlhc, voxelSpacing, dimensions);
    }

    /**
     * Creates a new instance.
     *
     * @param geometry the geometry.
     */
    public IntersectVolume(GeometryOfSlice geometry) {
        super(geometry);
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
        Vector3[] corners = getCornersOfSourceCubeInSourceSpace(
                row,
                column,
                tlhc,
                voxelSpacing,
                sliceThickness,
                dimensions);
        for (int i = 0; i < 8; ++i) {
            corners[i] = transformPointFromSourceSpaceIntoLocalizerSpace(corners[i]);
        }
        List<Vector3> intersections = getIntersectionsOfCubeWithZPlane(corners);
        if (intersections == null || intersections.isEmpty()) {
            return null;
        }
        List<Point2D> points = new ArrayList<>(intersections.size());
        for (Vector3 point : intersections) {
            points.add(transformPointInLocalizerPlaneIntoImageSpace(point));
        }
        return points;
    }

}
