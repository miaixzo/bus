/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
    public boolean equals(Object obj) {
        if (obj instanceof LutShape shape) {
            return (function != null) ? function.equals(shape.function) : lookup.equals(shape.lookup);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return (function != null) ? function.hashCode() : lookup.hashCode();
    }

    /**
     * LINEAR and SIGMOID descriptors are defined as DICOM standard LUT function <br>
     * Other LUT functions have their own custom implementation
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
