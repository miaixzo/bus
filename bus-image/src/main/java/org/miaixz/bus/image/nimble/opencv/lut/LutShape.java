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
package org.miaixz.bus.image.nimble.opencv.lut;

import java.util.Objects;
import java.util.Set;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.nimble.opencv.LookupTableCV;

/**
 * Represents a lookup table transformation shape for medical image processing.
 * <p>
 * A {@code LutShape} defines how pixel values are transformed during window/level operations. It can be either:
 * <ul>
 * <li>A predefined mathematical function (LINEAR, SIGMOID, LOG, etc.)
 * <li>A custom lookup table with arbitrary transformation values
 * </ul>
 * <p>
 * The LINEAR and SIGMOID functions comply with DICOM Part 3 standard specifications for presentation LUT shapes, while
 * other functions provide enhanced visualization capabilities for specific medical imaging needs.
 * <p>
 * This class is immutable and thread-safe. Instances can be compared by their underlying function or lookup table
 * content, ignoring explanation differences.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class LutShape {

    /**
     * The linear value.
     */
    public static final LutShape LINEAR = new LutShape(Function.LINEAR);

    /**
     * The sigmoid value.
     */
    public static final LutShape SIGMOID = new LutShape(Function.SIGMOID);

    /**
     * The sigmoid norm value.
     */
    public static final LutShape SIGMOID_NORM = new LutShape(Function.SIGMOID_NORM);

    /**
     * The log value.
     */
    public static final LutShape LOG = new LutShape(Function.LOG);

    /**
     * The log inv value.
     */
    public static final LutShape LOG_INV = new LutShape(Function.LOG_INV);

    /**
     * Enumeration of predefined lookup table transformation functions.
     * <p>
     * LINEAR and SIGMOID are defined according to DICOM Part 3 standard. Other functions provide custom implementations
     * for specialized imaging needs.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Function {

        /**
         * Linear transformation: f(x) = x
         */
        LINEAR("Linear"),
        /**
         * Sigmoid transformation: f(x) = 1/(1+e^(-x))
         */
        SIGMOID("Sigmoid"),
        /**
         * Normalized sigmoid transformation with enhanced contrast
         */
        SIGMOID_NORM("Sigmoid Normalize"),
        /**
         * Logarithmic transformation: f(x) = log(x)
         */
        LOG("Logarithmic"),
        /**
         * Inverse logarithmic transformation: f(x) = e^x
         */
        LOG_INV("Logarithmic Inverse");

        /**
         * The description value.
         */
        private final String description;

        /**
         * Creates a new instance.
         *
         * @param description the description.
         */
        Function(String description) {
            this.description = description;
        }

        /**
         * Returns the description.
         *
         * @return the description.
         */
        public String getDescription() {
            return description;
        }

        /**
         * Returns the string representation.
         *
         * @return the string representation.
         */
        @Override
        public String toString() {
            return description;
        }

    }

    /**
     * The function value.
     */
    private final Function function;

    /**
     * The explanation value.
     */
    private final String explanation;

    /**
     * The lookup value.
     */
    private final LookupTableCV lookup;

    /**
     * Creates a new instance.
     *
     * @param lookup      the lookup.
     * @param explanation the explanation.
     */
    public LutShape(LookupTableCV lookup, String explanation) {
        this.function = null;
        this.explanation = normalizeExplanation(explanation);
        this.lookup = Objects.requireNonNull(lookup, "Lookup table cannot be null");
    }

    /**
     * Creates a new instance.
     *
     * @param function the function.
     */
    public LutShape(Function function) {
        this(function, function.getDescription());
    }

    /**
     * Creates a new instance.
     *
     * @param function    the function.
     * @param explanation the explanation.
     */
    public LutShape(Function function, String explanation) {
        this.function = Objects.requireNonNull(function, "Function cannot be null");
        this.explanation = normalizeExplanation(explanation);
        this.lookup = null;
    }

    /**
     * Normalizes the explanation.
     *
     * @param explanation the explanation.
     * @return the operation result.
     */
    private static String normalizeExplanation(String explanation) {
        return Objects.toString(explanation, "");
    }

    /**
     * Returns the function type.
     *
     * @return the function type.
     */
    public Function getFunctionType() {
        return function;
    }

    /**
     * Returns the lookup.
     *
     * @return the lookup.
     */
    public LookupTableCV getLookup() {
        return lookup;
    }

    /**
     * Returns the explanation.
     *
     * @return the explanation.
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return explanation;
    }

    /**
     * Checks whether the function condition is true.
     *
     * @return true if the function condition is true; otherwise false.
     */
    public boolean isFunction() {
        return function != null;
    }

    /**
     * Executes the equals operation.
     *
     * @param o the o.
     * @return true if the equals condition is true; otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof LutShape other && Objects.equals(function, other.function)
                && Objects.equals(explanation, other.explanation) && Objects.equals(lookup, other.lookup));
    }

    /**
     * Checks whether the hash code condition is true.
     *
     * @return true if the hash code condition is true; otherwise false.
     */
    @Override
    public int hashCode() {
        return Objects.hash(function, explanation, lookup);
    }

    /**
     * Returns the LUT shape.
     *
     * @param shape the shape.
     * @return the LUT shape.
     */
    public static LutShape getLutShape(String shape) {
        if (!StringKit.hasText(shape)) {
            return null;
        }
        return switch (shape.trim().toUpperCase()) {
            case "LINEAR" -> LINEAR;
            case "SIGMOID" -> SIGMOID;
            case "SIGMOID_NORM" -> SIGMOID_NORM;
            case "LOG" -> LOG;
            case "LOG_INV" -> LOG_INV;
            default -> null;
        };
    }

    /**
     * Returns the all predefined.
     *
     * @return the all predefined.
     */
    public static Set<LutShape> getAllPredefined() {
        return Set.of(LINEAR, SIGMOID, SIGMOID_NORM, LOG, LOG_INV);
    }

}
