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

import org.miaixz.bus.image.nimble.opencv.LookupTableCV;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public final class LutShape {

    public static final LutShape LINEAR = new LutShape(eFunction.LINEAR);
    public static final LutShape SIGMOID = new LutShape(eFunction.SIGMOID);
    public static final LutShape SIGMOID_NORM = new LutShape(eFunction.SIGMOID_NORM);
    public static final LutShape LOG = new LutShape(eFunction.LOG);
    public static final LutShape LOG_INV = new LutShape(eFunction.LOG_INV);
    /**
     * A LutShape can be either a predefined function or a custom shape with a provided lookup table. That is a LutShape
     * can be defined as a function or by a lookup but not both
     */
    private final eFunction function;
    private final String explanation;
    private final LookupTableCV lookup;

    public LutShape(LookupTableCV lookup, String explanation) {
        if (lookup == null) {
            throw new IllegalArgumentException();
        }
        this.function = null;
        this.explanation = explanation;
        this.lookup = lookup;
    }

    public LutShape(eFunction function) {
        this(function, function.toString());
    }

    public LutShape(eFunction function, String explanation) {
        if (function == null) {
            throw new IllegalArgumentException();
        }
        this.function = function;
        this.explanation = explanation;
        this.lookup = null;
    }

    public static LutShape getLutShape(String shape) {
        if (shape != null) {
            String val = shape.toUpperCase();
            return switch (val) {
                case "LINEAR" -> LutShape.LINEAR;
                case "SIGMOID" -> LutShape.SIGMOID;
                case "SIGMOID_NORM" -> LutShape.SIGMOID_NORM;
                case "LOG" -> LutShape.LOG;
                case "LOG_INV" -> LutShape.LOG_INV;
                default -> null;
            };
        }
        return null;
    }

    public eFunction getFunctionType() {
        return function;
    }

    public LookupTableCV getLookup() {
        return lookup;
    }

    @Override
    public String toString() {
        return explanation;
    }

    /**
     * LutShape objects are defined either by a factory function or by a custom LUT. They can be equal even if they have
     * different explanation property
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof LutShape shape) {
            return (function != null) ? function.equals(shape.function) : lookup.equals(shape.lookup);
        }
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return (function != null) ? function.hashCode() : lookup.hashCode();
    }

    /**
     * LINEAR and SIGMOID descriptors are defined as DICOM standard LUT function Other LUT functions have their own
     * custom implementation
     */
    public enum eFunction {

        LINEAR("Linear"), SIGMOID("Sigmoid"), SIGMOID_NORM("Sigmoid Normalize"), LOG("Logarithmic"),
        LOG_INV("Logarithmic Inverse");

        final String explanation;

        eFunction(String explanation) {
            this.explanation = explanation;
        }

        @Override
        public String toString() {
            return explanation;
        }
    }

}
