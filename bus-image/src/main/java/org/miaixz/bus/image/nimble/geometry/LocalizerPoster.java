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
 * Base class for projecting slice or volume outlines onto a localizer image plane.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class LocalizerPoster {

    /**
     * The epsilon value.
     */
    private static final double EPSILON = 1.0e-9;

    /**
     * The localizer row value.
     */
    protected Vector3 localizerRow;

    /**
     * The localizer column value.
     */
    protected Vector3 localizerColumn;

    /**
     * The localizer normal value.
     */
    protected Vector3 localizerNormal;

    /**
     * The localizer tlhc value.
     */
    protected Vector3 localizerTLHC;

    /**
     * The localizer voxel spacing value.
     */
    protected Vector3 localizerVoxelSpacing;

    /**
     * The localizer dimensions value.
     */
    protected Vector3 localizerDimensions;

    /**
     * Creates a new instance.
     *
     * @param row          the row.
     * @param column       the column.
     * @param tlhc         the tlhc.
     * @param voxelSpacing the voxel spacing.
     * @param dimensions   the dimensions.
     */
    public LocalizerPoster(Vector3 row, Vector3 column, Vector3 tlhc, Vector3 voxelSpacing, Vector3 dimensions) {
        localizerRow = row;
        localizerColumn = column;
        localizerTLHC = tlhc;
        localizerVoxelSpacing = voxelSpacing;
        localizerDimensions = dimensions;
        doCommonConstructorStuff();
    }

    /**
     * Creates a new instance.
     *
     * @param geometry the geometry.
     */
    public LocalizerPoster(GeometryOfSlice geometry) {
        this(geometry.getRow(), geometry.getColumn(), geometry.getTLHC(), geometry.getVoxelSpacing(),
                geometry.getDimensions());
    }

    /**
     * Validates the direction cosines.
     *
     * @param row    the row.
     * @param column the column.
     */
    public static void validateDirectionCosines(Vector3 row, Vector3 column) {
        if (Math.abs(row.magnitudeSquared() - 1) > 0.001) {
            throw new IllegalArgumentException("Row not a unit vector");
        }
        if (Math.abs(column.magnitudeSquared() - 1) > 0.001) {
            throw new IllegalArgumentException("Column not a unit vector");
        }
        if (Math.abs(row.dot(column)) > 0.005) {
            throw new IllegalArgumentException("Row and column vectors are not orthogonal");
        }
    }

    /**
     * Returns the corners of source rectangle in source space.
     *
     * @param row          the row.
     * @param column       the column.
     * @param originalTLHC the original tlhc.
     * @param voxelSpacing the voxel spacing.
     * @param dimensions   the dimensions.
     * @return the corners of source rectangle in source space.
     */
    public static Vector3[] getCornersOfSourceRectangleInSourceSpace(
            Vector3 row,
            Vector3 column,
            Vector3 originalTLHC,
            Vector3 voxelSpacing,
            Vector3 dimensions) {
        validateDirectionCosines(row, column);
        Vector3 distanceAlongRow = row.multiply(dimensions.y() * voxelSpacing.y());
        Vector3 distanceAlongColumn = column.multiply(dimensions.x() * voxelSpacing.x());
        Vector3 tlhc = originalTLHC;
        Vector3 trhc = tlhc.add(distanceAlongRow);
        Vector3 blhc = tlhc.add(distanceAlongColumn);
        Vector3 brhc = tlhc.add(distanceAlongRow).add(distanceAlongColumn);
        return new Vector3[] { tlhc, trhc, brhc, blhc };
    }

    /**
     * Returns the corners of source cube in source space.
     *
     * @param row            the row.
     * @param column         the column.
     * @param originalTLHC   the original tlhc.
     * @param voxelSpacing   the voxel spacing.
     * @param sliceThickness the slice thickness.
     * @param dimensions     the dimensions.
     * @return the corners of source cube in source space.
     */
    public static Vector3[] getCornersOfSourceCubeInSourceSpace(
            Vector3 row,
            Vector3 column,
            Vector3 originalTLHC,
            Vector3 voxelSpacing,
            double sliceThickness,
            Vector3 dimensions) {
        validateDirectionCosines(row, column);
        Vector3 normal = row.cross(column);
        Vector3 distanceAlongRow = row.multiply(dimensions.y() * voxelSpacing.y());
        Vector3 distanceAlongColumn = column.multiply(dimensions.x() * voxelSpacing.x());
        Vector3 distanceAlongNormal = normal.multiply(dimensions.z() / 2.0 * sliceThickness);

        Vector3 tlhcT = originalTLHC.add(distanceAlongNormal);
        Vector3 trhcT = tlhcT.add(distanceAlongRow);
        Vector3 blhcT = tlhcT.add(distanceAlongColumn);
        Vector3 brhcT = tlhcT.add(distanceAlongRow).add(distanceAlongColumn);

        Vector3 tlhcB = originalTLHC.subtract(distanceAlongNormal);
        Vector3 trhcB = tlhcB.add(distanceAlongRow);
        Vector3 blhcB = tlhcB.add(distanceAlongColumn);
        Vector3 brhcB = tlhcB.add(distanceAlongRow).add(distanceAlongColumn);
        return new Vector3[] { tlhcT, trhcT, brhcT, blhcT, tlhcB, trhcB, brhcB, blhcB };
    }

    /**
     * Executes the transform point from source space into localizer space operation.
     *
     * @param point the point.
     * @return the operation result.
     */
    protected Vector3 transformPointFromSourceSpaceIntoLocalizerSpace(Vector3 point) {
        Vector3 relative = point.subtract(localizerTLHC);
        return new Vector3(relative.dot(localizerRow), relative.dot(localizerColumn), relative.dot(localizerNormal));
    }

    /**
     * Executes the transform point in localizer plane into image space operation.
     *
     * @param point the point.
     * @return the operation result.
     */
    protected Point2D.Double transformPointInLocalizerPlaneIntoImageSpace(Vector3 point) {
        double scaleSubPixelHeightOfColumn = (localizerDimensions.x() - 1.0) / localizerDimensions.x();
        double scaleSubPixelWidthOfRow = (localizerDimensions.y() - 1.0) / localizerDimensions.y();
        return new Point2D.Double(point.x() / localizerVoxelSpacing.y() * scaleSubPixelHeightOfColumn + 0.5,
                point.y() / localizerVoxelSpacing.x() * scaleSubPixelWidthOfRow + 0.5);
    }

    /**
     * Executes the outline on localizer operation.
     *
     * @param corners the corners.
     * @return the operation result.
     */
    protected List<Point2D> drawOutlineOnLocalizer(List<Vector3> corners) {
        return corners == null || corners.isEmpty() ? null : drawOutlineOnLocalizer(corners.toArray(Vector3[]::new));
    }

    /**
     * Executes the outline on localizer operation.
     *
     * @param corners the corners.
     * @return the operation result.
     */
    protected List<Point2D> drawOutlineOnLocalizer(Vector3[] corners) {
        ArrayList<Point2D> shapes = new ArrayList<>();
        for (Vector3 corner : corners) {
            shapes.add(transformPointInLocalizerPlaneIntoImageSpace(corner));
        }
        return shapes;
    }

    /**
     * Executes the intersect line between two points with plane where z is zero operation.
     *
     * @param a the a.
     * @param b the b.
     * @return the operation result.
     */
    protected Vector3 intersectLineBetweenTwoPointsWithPlaneWhereZIsZero(Vector3 a, Vector3 b) {
        double y = isEqual(b.z(), a.z()) ? a.y() : (b.y() - a.y()) / (b.z() - a.z()) * (0 - a.z()) + a.y();
        double x = isEqual(b.y(), a.y()) ? a.x() : (b.x() - a.x()) / (b.y() - a.y()) * (y - a.y()) + a.x();
        return new Vector3(x, y, 0.0);
    }

    /**
     * Executes the lines between any points which intersect plane where z is zero operation.
     *
     * @param corners the corners.
     * @return the operation result.
     */
    protected List<Point2D> drawLinesBetweenAnyPointsWhichIntersectPlaneWhereZIsZero(Vector3[] corners) {
        int size = corners.length;
        ArrayList<Vector3> intersections = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            int next = (i == size - 1) ? 0 : i + 1;
            double thisZ = corners[i].z();
            double nextZ = corners[next].z();
            if ((thisZ <= 0 && nextZ >= 0) || (thisZ >= 0 && nextZ <= 0)) {
                intersections.add(intersectLineBetweenTwoPointsWithPlaneWhereZIsZero(corners[i], corners[next]));
            }
        }
        return intersections.isEmpty() ? null : drawOutlineOnLocalizer(intersections);
    }

    /**
     * Executes the classify corners into edge crossing z plane operation.
     *
     * @param startCorner the start corner.
     * @param endCorner   the end corner.
     * @return true if the classify corners into edge crossing z plane condition is true; otherwise false.
     */
    protected static boolean classifyCornersIntoEdgeCrossingZPlane(Vector3 startCorner, Vector3 endCorner) {
        double startZ = startCorner.z();
        double endZ = endCorner.z();
        return (startZ <= 0 && endZ >= 0) || (startZ >= 0 && endZ <= 0);
    }

    /**
     * Returns the intersections of cube with z plane.
     *
     * @param corners the corners.
     * @return the intersections of cube with z plane.
     */
    protected List<Vector3> getIntersectionsOfCubeWithZPlane(Vector3[] corners) {
        int[][] edges = { { 0, 1 }, { 1, 2 }, { 2, 3 }, { 3, 0 }, { 4, 5 }, { 5, 6 }, { 6, 7 }, { 7, 4 }, { 0, 4 },
                { 1, 5 }, { 2, 6 }, { 3, 7 } };
        ArrayList<Vector3> intersections = new ArrayList<>(6);
        for (int[] edge : edges) {
            if (classifyCornersIntoEdgeCrossingZPlane(corners[edge[0]], corners[edge[1]])) {
                intersections
                        .add(intersectLineBetweenTwoPointsWithPlaneWhereZIsZero(corners[edge[0]], corners[edge[1]]));
            }
        }
        if (intersections.size() < 2) {
            return intersections;
        }
        double cx = 0;
        double cy = 0;
        for (Vector3 point : intersections) {
            cx += point.x();
            cy += point.y();
        }
        final double centerX = cx / intersections.size();
        final double centerY = cy / intersections.size();
        intersections.sort(
                (a, b) -> Double.compare(
                        Math.atan2(a.y() - centerY, a.x() - centerX),
                        Math.atan2(b.y() - centerY, b.x() - centerX)));
        return intersections;
    }

    /**
     * Executes the do common constructor stuff operation.
     */
    protected void doCommonConstructorStuff() {
        validateDirectionCosines(localizerRow, localizerColumn);
        localizerNormal = localizerRow.cross(localizerColumn);
    }

    /**
     * Checks whether the equal condition is true.
     *
     * @param a the a.
     * @param b the b.
     * @return true if the equal condition is true; otherwise false.
     */
    private static boolean isEqual(double a, double b) {
        return Math.abs(a - b) <= EPSILON;
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
    public abstract List<Point2D> getOutlineOnLocalizerForThisGeometry(
            Vector3 row,
            Vector3 column,
            Vector3 tlhc,
            Vector3 voxelSpacing,
            double sliceThickness,
            Vector3 dimensions);

    /**
     * Returns the outline on localizer for this geometry.
     *
     * @param geometry the geometry.
     * @return the outline on localizer for this geometry.
     */
    public final List<Point2D> getOutlineOnLocalizerForThisGeometry(GeometryOfSlice geometry) {
        return getOutlineOnLocalizerForThisGeometry(
                geometry.getRow(),
                geometry.getColumn(),
                geometry.getTLHC(),
                geometry.getVoxelSpacing(),
                geometry.getSliceThickness(),
                geometry.getDimensions());
    }

}
