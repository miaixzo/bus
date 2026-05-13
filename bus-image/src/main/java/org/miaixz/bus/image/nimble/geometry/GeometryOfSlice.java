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
import java.util.Objects;

/**
 * Spatial geometry of a single cross-sectional DICOM image slice.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class GeometryOfSlice {

    /**
     * The min spacing value.
     */
    public static final double MIN_SPACING = 0.00001;

    /**
     * The row value.
     */
    protected Vector3 row;

    /**
     * The column value.
     */
    protected Vector3 column;

    /**
     * The tlhc value.
     */
    protected Vector3 tlhc;

    /**
     * The voxel spacing value.
     */
    protected Vector3 voxelSpacing;

    /**
     * The slice thickness value.
     */
    protected double sliceThickness;

    /**
     * The dimensions value.
     */
    protected Vector3 dimensions;

    /**
     * Creates a new instance.
     *
     * @param row            the row.
     * @param column         the column.
     * @param tlhc           the tlhc.
     * @param voxelSpacing   the voxel spacing.
     * @param sliceThickness the slice thickness.
     * @param dimensions     the dimensions.
     */
    public GeometryOfSlice(Vector3 row, Vector3 column, Vector3 tlhc, Vector3 voxelSpacing, double sliceThickness,
            Vector3 dimensions) {
        this.row = Objects.requireNonNull(row, "Row vector cannot be null");
        this.column = Objects.requireNonNull(column, "Column vector cannot be null");
        this.tlhc = Objects.requireNonNull(tlhc, "Top left hand corner cannot be null");
        this.voxelSpacing = Objects.requireNonNull(voxelSpacing, "Voxel spacing cannot be null");
        this.sliceThickness = sliceThickness;
        this.dimensions = Objects.requireNonNull(dimensions, "Dimensions cannot be null");
    }

    /**
     * Creates a new instance.
     *
     * @param geometry the geometry.
     */
    public GeometryOfSlice(GeometryOfSlice geometry) {
        this(geometry.getRow(), geometry.getColumn(), geometry.getTLHC(), geometry.getVoxelSpacing(),
                geometry.getSliceThickness(), geometry.getDimensions());
    }

    /**
     * Returns the row.
     *
     * @return the row.
     */
    public final Vector3 getRow() {
        return row;
    }

    /**
     * Returns the column.
     *
     * @return the column.
     */
    public final Vector3 getColumn() {
        return column;
    }

    /**
     * Returns the normal.
     *
     * @return the normal.
     */
    public final Vector3 getNormal() {
        return Vector3.computeNormalOfSurface(row, column);
    }

    /**
     * Returns the tlhc.
     *
     * @return the tlhc.
     */
    public final Vector3 getTLHC() {
        return tlhc;
    }

    /**
     * Checks whether the row column orthogonal condition is true.
     *
     * @return true if the row column orthogonal condition is true; otherwise false.
     */
    public boolean isRowColumnOrthogonal() {
        return Math.abs(row.dot(column)) <= 0.005;
    }

    /**
     * Returns the position.
     *
     * @param point the point.
     * @return the position.
     */
    public final Vector3 getPosition(Point2D point) {
        return new Vector3(
                row.x() * voxelSpacing.x() * point.getX() + column.x() * voxelSpacing.y() * point.getY() + tlhc.x(),
                row.y() * voxelSpacing.x() * point.getX() + column.y() * voxelSpacing.y() * point.getY() + tlhc.y(),
                row.z() * voxelSpacing.x() * point.getX() + column.z() * voxelSpacing.y() * point.getY() + tlhc.z());
    }

    /**
     * Returns the image position.
     *
     * @param position the position.
     * @return the image position.
     */
    public final Point2D getImagePosition(Vector3 position) {
        if (voxelSpacing.x() < MIN_SPACING || voxelSpacing.y() < MIN_SPACING) {
            return null;
        }
        double ix = ((position.x() - tlhc.x()) * row.x() + (position.y() - tlhc.y()) * row.y()
                + (position.z() - tlhc.z()) * row.z()) / voxelSpacing.x();
        double iy = ((position.x() - tlhc.x()) * column.x() + (position.y() - tlhc.y()) * column.y()
                + (position.z() - tlhc.z()) * column.z()) / voxelSpacing.y();
        return new Point2D.Double(ix, iy);
    }

    /**
     * Returns the voxel spacing.
     *
     * @return the voxel spacing.
     */
    public final Vector3 getVoxelSpacing() {
        return voxelSpacing;
    }

    /**
     * Returns the slice thickness.
     *
     * @return the slice thickness.
     */
    public final double getSliceThickness() {
        return sliceThickness;
    }

    /**
     * Returns the dimensions.
     *
     * @return the dimensions.
     */
    public final Vector3 getDimensions() {
        return dimensions;
    }

    /**
     * Returns the orientation.
     *
     * @param orientation the orientation.
     * @param quadruped   the quadruped.
     * @return the orientation.
     */
    public static String getOrientation(Vector3 orientation, boolean quadruped) {
        StringBuilder builder = new StringBuilder();
        if (orientation != null) {
            String orientationX = orientation.x() < 0 ? (quadruped ? "Rt" : "R") : (quadruped ? "Le" : "L");
            String orientationY = orientation.y() < 0 ? (quadruped ? "V" : "A") : (quadruped ? "D" : "P");
            String orientationZ = orientation.z() < 0 ? (quadruped ? "Cd" : "F") : (quadruped ? "Cr" : "H");

            double absX = Math.abs(orientation.x());
            double absY = Math.abs(orientation.y());
            double absZ = Math.abs(orientation.z());
            for (int i = 0; i < 3; ++i) {
                if (absX > 0.0001 && absX > absY && absX > absZ) {
                    builder.append(orientationX);
                    absX = 0;
                } else if (absY > 0.0001 && absY > absX && absY > absZ) {
                    builder.append(orientationY);
                    absY = 0;
                } else if (absZ > 0.0001 && absZ > absX && absZ > absY) {
                    builder.append(orientationZ);
                    absZ = 0;
                }
            }
        }
        return builder.toString();
    }

    /**
     * Returns the row orientation.
     *
     * @param quadruped the quadruped.
     * @return the row orientation.
     */
    public final String getRowOrientation(boolean quadruped) {
        return getOrientation(row, quadruped);
    }

    /**
     * Returns the column orientation.
     *
     * @param quadruped the quadruped.
     * @return the column orientation.
     */
    public final String getColumnOrientation(boolean quadruped) {
        return getOrientation(column, quadruped);
    }

    /**
     * Returns the row orientation.
     *
     * @return the row orientation.
     */
    public final String getRowOrientation() {
        return getRowOrientation(false);
    }

    /**
     * Returns the column orientation.
     *
     * @return the column orientation.
     */
    public final String getColumnOrientation() {
        return getColumnOrientation(false);
    }

    /**
     * Returns the slice outline on localizer.
     *
     * @param sourceGeometry the source geometry.
     * @return the slice outline on localizer.
     */
    public final List<Point2D> getSliceOutlineOnLocalizer(GeometryOfSlice sourceGeometry) {
        return new IntersectSlice(this).getOutlineOnLocalizerForThisGeometry(sourceGeometry);
    }

    /**
     * Returns the volume outline on localizer.
     *
     * @param sourceGeometry the source geometry.
     * @return the volume outline on localizer.
     */
    public final List<Point2D> getVolumeOutlineOnLocalizer(GeometryOfSlice sourceGeometry) {
        return new IntersectVolume(this).getOutlineOnLocalizerForThisGeometry(sourceGeometry);
    }

    /**
     * Executes the equals operation.
     *
     * @param obj the obj.
     * @return true if the equals condition is true; otherwise false.
     */
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof GeometryOfSlice geometry && row.equals(geometry.row)
                && column.equals(geometry.column) && tlhc.equals(geometry.tlhc)
                && voxelSpacing.equals(geometry.voxelSpacing)
                && Double.compare(sliceThickness, geometry.sliceThickness) == 0
                && dimensions.equals(geometry.dimensions));
    }

    /**
     * Checks whether the hash code condition is true.
     *
     * @return true if the hash code condition is true; otherwise false.
     */
    @Override
    public int hashCode() {
        return Objects.hash(row, column, tlhc, voxelSpacing, sliceThickness, dimensions);
    }

}
