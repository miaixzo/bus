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
 * Patient orientation utilities for DICOM geometry.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class PatientOrientation {

    /**
     * The blue value.
     */
    public static final Color BLUE = new Color(44783);

    /**
     * The red value.
     */
    public static final Color RED = new Color(15539236);

    /**
     * The green value.
     */
    public static final Color GREEN = new Color(897355);

    /**
     * The vector cannot be null value.
     */
    public static final String VECTOR_CANNOT_BE_NULL = "Vector cannot be null";

    /**
     * Creates a new instance.
     */
    private PatientOrientation() {
        // No initialization required.
    }

    /**
     * Human anatomical orientations.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Biped implements Orientation {

        /**
         * Constant for the r value.
         */
        R("Right", BLUE),
        /**
         * Constant for the l value.
         */
        L("Left", BLUE),
        /**
         * Constant for the a value.
         */
        A("Anterior", RED),
        /**
         * Constant for the p value.
         */
        P("Posterior", RED),
        /**
         * Constant for the f value.
         */
        F("Foot", GREEN),
        /**
         * Constant for the h value.
         */
        H("Head", GREEN);

        /**
         * The full name value.
         */
        private final String fullName;

        /**
         * The color value.
         */
        private final Color color;

        /**
         * Creates a new instance.
         *
         * @param fullName the full name.
         * @param color    the color.
         */
        Biped(String fullName, Color color) {
            this.fullName = fullName;
            this.color = color;
        }

        /**
         * Executes the from string operation.
         *
         * @param value the value.
         * @return the operation result.
         */
        public static Biped fromString(String value) {
            if (!hasText(value)) {
                return null;
            }
            try {
                return valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }

        /**
         * Returns the full name.
         *
         * @return the full name.
         */
        @Override
        public String getFullName() {
            return fullName;
        }

        /**
         * Returns the color.
         *
         * @return the color.
         */
        @Override
        public Color getColor() {
            return color;
        }

        /**
         * Returns the string representation.
         *
         * @return the string representation.
         */
        @Override
        public String toString() {
            return fullName;
        }

    }

    /**
     * Veterinary anatomical orientations.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Quadruped implements Orientation {

        /**
         * Constant for the rt value.
         */
        RT("Right", BLUE),
        /**
         * Constant for the le value.
         */
        LE("Left", BLUE),
        /**
         * Constant for the v value.
         */
        V("Ventral", RED),
        /**
         * Constant for the d value.
         */
        D("Dorsal", RED),
        /**
         * Constant for the cd value.
         */
        CD("Caudal", GREEN),
        /**
         * The full name value.
         */
        CR("Cranial", GREEN);

        /**
         * The full name value.
         */
        private final String fullName;

        /**
         * The color value.
         */
        private final Color color;

        /**
         * Creates a new instance.
         *
         * @param fullName the full name.
         * @param color    the color.
         */
        Quadruped(String fullName, Color color) {
            this.fullName = fullName;
            this.color = color;
        }

        /**
         * Executes the from string operation.
         *
         * @param value the value.
         * @return the operation result.
         */
        public static Quadruped fromString(String value) {
            if (!hasText(value)) {
                return null;
            }
            try {
                return valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }

        /**
         * Returns the full name.
         *
         * @return the full name.
         */
        @Override
        public String getFullName() {
            return fullName;
        }

        /**
         * Returns the color.
         *
         * @return the color.
         */
        @Override
        public Color getColor() {
            return color;
        }

        /**
         * Returns the string representation.
         *
         * @return the string representation.
         */
        @Override
        public String toString() {
            return fullName;
        }

    }

    /**
     * Coordinate axes used by patient orientation.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Axis {
        /**
         * Constant for the x value.
         */
        X,
        /**
         * Constant for the y value.
         */
        Y,
        /**
         * Constant for the z value.
         */
        Z

    }

    /**
     * Returns the biped x orientation.
     *
     * @param vector the vector.
     * @return the biped x orientation.
     */
    public static Biped getBipedXOrientation(Vector3 vector) {
        Objects.requireNonNull(vector, VECTOR_CANNOT_BE_NULL);
        return vector.x() < 0 ? Biped.R : Biped.L;
    }

    /**
     * Returns the biped y orientation.
     *
     * @param vector the vector.
     * @return the biped y orientation.
     */
    public static Biped getBipedYOrientation(Vector3 vector) {
        Objects.requireNonNull(vector, VECTOR_CANNOT_BE_NULL);
        return vector.y() < 0 ? Biped.A : Biped.P;
    }

    /**
     * Returns the biped z orientation.
     *
     * @param vector the vector.
     * @return the biped z orientation.
     */
    public static Biped getBipedZOrientation(Vector3 vector) {
        Objects.requireNonNull(vector, VECTOR_CANNOT_BE_NULL);
        return vector.z() < 0 ? Biped.F : Biped.H;
    }

    /**
     * Returns the quadruped x orientation.
     *
     * @param vector the vector.
     * @return the quadruped x orientation.
     */
    public static Quadruped getQuadrupedXOrientation(Vector3 vector) {
        Objects.requireNonNull(vector, VECTOR_CANNOT_BE_NULL);
        return vector.x() < 0 ? Quadruped.RT : Quadruped.LE;
    }

    /**
     * Returns the quadruped y orientation.
     *
     * @param vector the vector.
     * @return the quadruped y orientation.
     */
    public static Quadruped getQuadrupedYOrientation(Vector3 vector) {
        Objects.requireNonNull(vector, VECTOR_CANNOT_BE_NULL);
        return vector.y() < 0 ? Quadruped.V : Quadruped.D;
    }

    /**
     * Returns the quadruped z orientation.
     *
     * @param vector the vector.
     * @return the quadruped z orientation.
     */
    public static Quadruped getQuadrupedZOrientation(Vector3 vector) {
        Objects.requireNonNull(vector, VECTOR_CANNOT_BE_NULL);
        return vector.z() < 0 ? Quadruped.CD : Quadruped.CR;
    }

    /**
     * Returns the axis orientation.
     *
     * @param vector    the vector.
     * @param axis      the axis.
     * @param quadruped the quadruped.
     * @return the axis orientation.
     */
    public static Orientation getAxisOrientation(Vector3 vector, Axis axis, boolean quadruped) {
        Objects.requireNonNull(vector, VECTOR_CANNOT_BE_NULL);
        Objects.requireNonNull(axis, "Axis cannot be null");
        return switch (axis) {
            case X -> quadruped ? getQuadrupedXOrientation(vector) : getBipedXOrientation(vector);
            case Y -> quadruped ? getQuadrupedYOrientation(vector) : getBipedYOrientation(vector);
            case Z -> quadruped ? getQuadrupedZOrientation(vector) : getBipedZOrientation(vector);
        };
    }

    /**
     * Returns the opposite orientation.
     *
     * @param orientation the orientation.
     * @return the opposite orientation.
     */
    public static Biped getOppositeOrientation(Biped orientation) {
        Objects.requireNonNull(orientation, "Orientation cannot be null");
        return switch (orientation) {
            case R -> Biped.L;
            case L -> Biped.R;
            case A -> Biped.P;
            case P -> Biped.A;
            case F -> Biped.H;
            case H -> Biped.F;
        };
    }

    /**
     * Returns the opposite orientation.
     *
     * @param orientation the orientation.
     * @return the opposite orientation.
     */
    public static Quadruped getOppositeOrientation(Quadruped orientation) {
        Objects.requireNonNull(orientation, "Orientation cannot be null");
        return switch (orientation) {
            case RT -> Quadruped.LE;
            case LE -> Quadruped.RT;
            case V -> Quadruped.D;
            case D -> Quadruped.V;
            case CD -> Quadruped.CR;
            case CR -> Quadruped.CD;
        };
    }

    /**
     * Checks whether the text condition is true.
     *
     * @param value the value.
     * @return true if the text condition is true; otherwise false.
     */
    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
