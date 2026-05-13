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

import java.util.Objects;

/**
 * Immutable three-dimensional vector used by DICOM geometry calculations.
 *
 * @param x the x.
 * @param y the y.
 * @param z the z.
 * @author Kimi Liu
 * @since Java 21+
 */
public record Vector3(double x, double y, double z) {

    /**
     * The zero value.
     */
    public static final Vector3 ZERO = new Vector3(0.0, 0.0, 0.0);

    /**
     * The unit x value.
     */
    public static final Vector3 UNIT_X = new Vector3(1.0, 0.0, 0.0);

    /**
     * The unit y value.
     */
    public static final Vector3 UNIT_Y = new Vector3(0.0, 1.0, 0.0);

    /**
     * The unit z value.
     */
    public static final Vector3 UNIT_Z = new Vector3(0.0, 0.0, 1.0);

    /**
     * The epsilon value.
     */
    private static final double EPSILON = 1e-10;

    /**
     * The vector required value.
     */
    private static final String VECTOR_REQUIRED = "Other vector cannot be null";

    /**
     * Creates a new instance.
     *
     * @param x the x.
     * @param y the y.
     * @param z the z.
     */
    public Vector3 {
        x = normalizeZero(x);
        y = normalizeZero(y);
        z = normalizeZero(z);
    }

    /**
     * Create a vector from the first three entries of a coordinate array.
     *
     * @param coordinates coordinate array
     * @return a vector using entries 0, 1 and 2
     */
    public static Vector3 of(double[] coordinates) {
        return of(coordinates, 0);
    }

    /**
     * Create a vector from three entries in a coordinate array.
     *
     * @param coordinates coordinate array
     * @param offset      start index
     * @return a vector using entries offset, offset + 1 and offset + 2
     */
    public static Vector3 of(double[] coordinates, int offset) {
        Objects.requireNonNull(coordinates, "Coordinate array cannot be null");
        validateArrayBounds(coordinates, offset);
        return new Vector3(coordinates[offset], coordinates[offset + 1], coordinates[offset + 2]);
    }

    /**
     * Executes the compute normal of surface operation.
     *
     * @param v1 the v1.
     * @param v2 the v2.
     * @return the operation result.
     */
    public static Vector3 computeNormalOfSurface(Vector3 v1, Vector3 v2) {
        Vector3 normal = Objects.requireNonNull(v1, VECTOR_REQUIRED).cross(Objects.requireNonNull(v2, VECTOR_REQUIRED));
        return normal.magnitudeSquared() > 0.0 ? normal.normalize() : normal;
    }

    /**
     * Executes the compute normal of surface operation.
     *
     * @param origin the origin.
     * @param v1     the v1.
     * @param v2     the v2.
     * @return the operation result.
     */
    public static Vector3 computeNormalOfSurface(Vector3 origin, Vector3 v1, Vector3 v2) {
        Objects.requireNonNull(origin, "Origin vector cannot be null");
        Vector3 u = Objects.requireNonNull(v1, VECTOR_REQUIRED).subtract(origin);
        Vector3 w = Objects.requireNonNull(v2, VECTOR_REQUIRED).subtract(origin);
        Vector3 normal = u.cross(w);
        return normal.magnitudeSquared() > 0.0 ? normal.normalize() : normal;
    }

    /**
     * Executes the orient normal to dominant positive axis operation.
     *
     * @param normal the normal.
     * @return the operation result.
     */
    public static Vector3 orientNormalToDominantPositiveAxis(Vector3 normal) {
        if (normal == null) {
            return null;
        }
        double ax = Math.abs(normal.x());
        double ay = Math.abs(normal.y());
        double az = Math.abs(normal.z());
        double dominant = ax >= ay && ax >= az ? normal.x() : ay >= az ? normal.y() : normal.z();
        return dominant < 0.0 ? normal.negate() : normal;
    }

    /**
     * @return vector magnitude
     */
    public double magnitude() {
        return Math.sqrt(magnitudeSquared());
    }

    /**
     * @return squared vector magnitude
     */
    public double magnitudeSquared() {
        return x * x + y * y + z * z;
    }

    /**
     * @param other other vector
     * @return dot product
     */
    public double dot(Vector3 other) {
        Objects.requireNonNull(other, VECTOR_REQUIRED);
        return x * other.x + y * other.y + z * other.z;
    }

    /**
     * @return unit vector in the same direction, or {@link #ZERO}
     */
    public Vector3 normalize() {
        double magnitude = magnitude();
        if (isZero(magnitude)) {
            return ZERO;
        }
        return multiply(1.0 / magnitude);
    }

    /**
     * @param other other vector
     * @return cross product
     */
    public Vector3 cross(Vector3 other) {
        Objects.requireNonNull(other, VECTOR_REQUIRED);
        return new Vector3(normalizeZero(y * other.z - z * other.y), normalizeZero(z * other.x - x * other.z),
                normalizeZero(x * other.y - y * other.x));
    }

    /**
     * @param other vector to add
     * @return vector sum
     */
    public Vector3 add(Vector3 other) {
        Objects.requireNonNull(other, VECTOR_REQUIRED);
        return new Vector3(normalizeZero(x + other.x), normalizeZero(y + other.y), normalizeZero(z + other.z));
    }

    /**
     * @param other vector to subtract
     * @return vector difference
     */
    public Vector3 subtract(Vector3 other) {
        Objects.requireNonNull(other, VECTOR_REQUIRED);
        return new Vector3(normalizeZero(x - other.x), normalizeZero(y - other.y), normalizeZero(z - other.z));
    }

    /**
     * @param scalar scalar multiplier
     * @return scaled vector
     */
    public Vector3 multiply(double scalar) {
        return new Vector3(normalizeZero(x * scalar), normalizeZero(y * scalar), normalizeZero(z * scalar));
    }

    /**
     * @return negated vector
     */
    public Vector3 negate() {
        return new Vector3(normalizeZero(-x), normalizeZero(-y), normalizeZero(-z));
    }

    /**
     * @return a new coordinate array in x, y, z order
     */
    public double[] toArray() {
        return new double[] { x, y, z };
    }

    /**
     * Compare vector coordinates within a tolerance.
     *
     * @param other     other vector
     * @param tolerance accepted coordinate difference
     * @return true when all coordinates are within tolerance
     */
    public boolean equals(Vector3 other, double tolerance) {
        Objects.requireNonNull(other, VECTOR_REQUIRED);
        if (tolerance < 0.0) {
            throw new IllegalArgumentException("Tolerance cannot be negative: " + tolerance);
        }
        return Math.abs(x - other.x) <= tolerance && Math.abs(y - other.y) <= tolerance
                && Math.abs(z - other.z) <= tolerance;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "Vector3(%.6f, %.6f, %.6f)".formatted(x, y, z);
    }

    /**
     * Executes the normalize zero operation.
     *
     * @param value the value.
     * @return the operation result.
     */
    private static double normalizeZero(double value) {
        return value == 0.0 ? 0.0 : value;
    }

    /**
     * Validates the array bounds.
     *
     * @param coordinates the coordinates.
     * @param offset      the offset.
     */
    private static void validateArrayBounds(double[] coordinates, int offset) {
        if (offset < 0) {
            throw new ArrayIndexOutOfBoundsException("Offset cannot be negative: " + offset);
        }
        if (coordinates.length < offset + 3) {
            throw new IllegalArgumentException(
                    "Array must contain at least %d elements, but has %d".formatted(offset + 3, coordinates.length));
        }
    }

    /**
     * Determines whether zero.
     *
     * @param value the value.
     * @return true if the condition is met; otherwise false.
     */
    private static boolean isZero(double value) {
        return Math.abs(value) < EPSILON;
    }

}
