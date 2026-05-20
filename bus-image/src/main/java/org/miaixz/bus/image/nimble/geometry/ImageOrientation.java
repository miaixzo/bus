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

import java.awt.Color;
import java.util.Objects;

/**
 * Utilities for resolving DICOM image orientation from direction cosine vectors.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ImageOrientation {

    /**
     * The default obliquity threshold value.
     */
    public static final double DEFAULT_OBLIQUITY_THRESHOLD = 0.8;

    /**
     * Creates a new instance.
     */
    private ImageOrientation() {
        // No initialization required.
    }

    /**
     * Anatomical planes.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Plan {

        /**
         * Constant for the unknown value.
         */
        UNKNOWN("Unknown"),
        /**
         * Constant for the transverse value.
         */
        TRANSVERSE("Axial"),
        /**
         * Constant for the sagittal value.
         */
        SAGITTAL("Sagittal"),
        /**
         * Constant for the coronal value.
         */
        CORONAL("Coronal"),
        /**
         * Constant for the oblique value.
         */
        OBLIQUE("Oblique");

        /**
         * The display name value.
         */
        private final String displayName;

        /**
         * Creates a new instance.
         *
         * @param displayName the display name.
         */
        Plan(String displayName) {
            this.displayName = displayName;
        }

        /**
         * Returns the string representation.
         *
         * @return the string representation.
         */
        @Override
        public String toString() {
            return displayName;
        }

    }

    /**
     * Returns the plan.
     *
     * @param rowVector    the row vector.
     * @param columnVector the column vector.
     * @return the plan.
     */
    public static Plan getPlan(Vector3 rowVector, Vector3 columnVector) {
        return getPlan(rowVector, columnVector, DEFAULT_OBLIQUITY_THRESHOLD);
    }

    /**
     * Returns the plan.
     *
     * @param rowVector    the row vector.
     * @param columnVector the column vector.
     * @param threshold    the threshold.
     * @return the plan.
     */
    public static Plan getPlan(Vector3 rowVector, Vector3 columnVector, double threshold) {
        Objects.requireNonNull(rowVector, "Row vector cannot be null");
        Objects.requireNonNull(columnVector, "Column vector cannot be null");
        validateThreshold(threshold);

        Orientation rowAxis = getPatientOrientation(rowVector, threshold, false);
        Orientation columnAxis = getPatientOrientation(columnVector, threshold, false);
        return determinePlanFromOrientations(rowAxis, columnAxis);
    }

    /**
     * Returns the plan.
     *
     * @param rowOrientation    the row orientation.
     * @param columnOrientation the column orientation.
     * @return the plan.
     */
    public static Plan getPlan(Orientation rowOrientation, Orientation columnOrientation) {
        return determinePlanFromOrientations(rowOrientation, columnOrientation);
    }

    /**
     * Returns the patient orientation.
     *
     * @param vector    the vector.
     * @param minCosine the min cosine.
     * @param quadruped the quadruped.
     * @return the patient orientation.
     */
    public static Orientation getPatientOrientation(Vector3 vector, double minCosine, boolean quadruped) {
        Objects.requireNonNull(vector, PatientOrientation.VECTOR_CANNOT_BE_NULL);
        validateThreshold(minCosine);

        double absX = Math.abs(vector.x());
        double absY = Math.abs(vector.y());
        double absZ = Math.abs(vector.z());

        if (isDominantAxis(absX, absY, absZ, minCosine)) {
            return getXAxisOrientation(vector, quadruped);
        }
        if (isDominantAxis(absY, absX, absZ, minCosine)) {
            return getYAxisOrientation(vector, quadruped);
        }
        if (isDominantAxis(absZ, absX, absY, minCosine)) {
            return getZAxisOrientation(vector, quadruped);
        }
        return null;
    }

    /**
     * Returns the x axis orientation.
     *
     * @param vector    the vector.
     * @param quadruped the quadruped.
     * @return the x axis orientation.
     */
    public static Orientation getXAxisOrientation(Vector3 vector, boolean quadruped) {
        return quadruped ? PatientOrientation.getQuadrupedXOrientation(vector)
                : PatientOrientation.getBipedXOrientation(vector);
    }

    /**
     * Returns the y axis orientation.
     *
     * @param vector    the vector.
     * @param quadruped the quadruped.
     * @return the y axis orientation.
     */
    public static Orientation getYAxisOrientation(Vector3 vector, boolean quadruped) {
        return quadruped ? PatientOrientation.getQuadrupedYOrientation(vector)
                : PatientOrientation.getBipedYOrientation(vector);
    }

    /**
     * Returns the z axis orientation.
     *
     * @param vector    the vector.
     * @param quadruped the quadruped.
     * @return the z axis orientation.
     */
    public static Orientation getZAxisOrientation(Vector3 vector, boolean quadruped) {
        return quadruped ? PatientOrientation.getQuadrupedZOrientation(vector)
                : PatientOrientation.getBipedZOrientation(vector);
    }

    /**
     * Validates the threshold.
     *
     * @param threshold the threshold.
     */
    private static void validateThreshold(double threshold) {
        if (Double.isNaN(threshold) || threshold < 0.0 || threshold > 1.0) {
            throw new IllegalArgumentException("Threshold must be between 0.0 and 1.0, got: " + threshold);
        }
    }

    /**
     * Checks whether the dominant axis condition is true.
     *
     * @param primary    the primary.
     * @param secondary1 the secondary 1.
     * @param secondary2 the secondary 2.
     * @param threshold  the threshold.
     * @return true if the dominant axis condition is true; otherwise false.
     */
    private static boolean isDominantAxis(double primary, double secondary1, double secondary2, double threshold) {
        return primary > threshold && primary > secondary1 && primary > secondary2;
    }

    /**
     * Executes the determine plan from orientations operation.
     *
     * @param rowAxis    the row axis.
     * @param columnAxis the column axis.
     * @return the operation result.
     */
    private static Plan determinePlanFromOrientations(Orientation rowAxis, Orientation columnAxis) {
        if (rowAxis == null || columnAxis == null) {
            return Plan.OBLIQUE;
        }
        return switch (getColorPairType(rowAxis.getColor(), columnAxis.getColor())) {
            case BLUE_RED -> Plan.TRANSVERSE;
            case BLUE_GREEN -> Plan.CORONAL;
            case RED_GREEN -> Plan.SAGITTAL;
            case UNKNOWN -> Plan.OBLIQUE;
        };
    }

    /**
     * Returns the color pair type.
     *
     * @param rowColor    the row color.
     * @param columnColor the column color.
     * @return the color pair type.
     */
    private static ColorPairType getColorPairType(Color rowColor, Color columnColor) {
        if (isColorCombination(rowColor, columnColor, PatientOrientation.BLUE, PatientOrientation.RED)) {
            return ColorPairType.BLUE_RED;
        }
        if (isColorCombination(rowColor, columnColor, PatientOrientation.BLUE, PatientOrientation.GREEN)) {
            return ColorPairType.BLUE_GREEN;
        }
        if (isColorCombination(rowColor, columnColor, PatientOrientation.RED, PatientOrientation.GREEN)) {
            return ColorPairType.RED_GREEN;
        }
        return ColorPairType.UNKNOWN;
    }

    /**
     * Checks whether the color combination condition is true.
     *
     * @param first          the first.
     * @param second         the second.
     * @param expectedFirst  the expected first.
     * @param expectedSecond the expected second.
     * @return true if the color combination condition is true; otherwise false.
     */
    private static boolean isColorCombination(Color first, Color second, Color expectedFirst, Color expectedSecond) {
        return (first.equals(expectedFirst) && second.equals(expectedSecond))
                || (first.equals(expectedSecond) && second.equals(expectedFirst));
    }

    /**
     * Defines the ColorPairType values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private enum ColorPairType {
        /**
         * Constant for the blue red value.
         */
        BLUE_RED,
        /**
         * Constant for the blue green value.
         */
        BLUE_GREEN,
        /**
         * Constant for the red green value.
         */
        RED_GREEN,
        /**
         * Constant for the unknown value.
         */
        UNKNOWN

    }

}
