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

import java.util.Objects;

/**
 * Implementation of the LUT parameters. No test is required for this class
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LutParameters {

    private final double intercept;
    private final double slope;
    private final Integer paddingMinValue;
    private final Integer paddingMaxValue;
    private final int bitsStored;
    private final boolean signed;
    private final boolean applyPadding;
    private final boolean outputSigned;
    private final int bitsOutput;
    private final boolean inversePaddingMLUT;

    public LutParameters(double intercept, double slope, boolean applyPadding, Integer paddingMinValue,
            Integer paddingMaxValue, int bitsStored, boolean signed, boolean outputSigned, int bitsOutput,
            boolean inversePaddingMLUT) {
        this.intercept = intercept;
        this.slope = slope;
        this.paddingMinValue = paddingMinValue;
        this.paddingMaxValue = paddingMaxValue;
        this.bitsStored = bitsStored;
        this.signed = signed;
        this.applyPadding = applyPadding;
        this.outputSigned = outputSigned;
        this.bitsOutput = bitsOutput;
        this.inversePaddingMLUT = inversePaddingMLUT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LutParameters that = (LutParameters) o;
        return Double.compare(that.intercept, intercept) == 0 && Double.compare(that.slope, slope) == 0
                && bitsStored == that.bitsStored && signed == that.signed && applyPadding == that.applyPadding
                && outputSigned == that.outputSigned && bitsOutput == that.bitsOutput
                && inversePaddingMLUT == that.inversePaddingMLUT
                && Objects.equals(paddingMinValue, that.paddingMinValue)
                && Objects.equals(paddingMaxValue, that.paddingMaxValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(intercept, slope, paddingMinValue, paddingMaxValue, bitsStored, signed, applyPadding,
                outputSigned, bitsOutput, inversePaddingMLUT);
    }

    public double getIntercept() {
        return intercept;
    }

    public double getSlope() {
        return slope;
    }

    public Integer getPaddingMinValue() {
        return paddingMinValue;
    }

    public Integer getPaddingMaxValue() {
        return paddingMaxValue;
    }

    public int getBitsStored() {
        return bitsStored;
    }

    public boolean isSigned() {
        return signed;
    }

    public boolean isApplyPadding() {
        return applyPadding;
    }

    public boolean isOutputSigned() {
        return outputSigned;
    }

    public int getBitsOutput() {
        return bitsOutput;
    }

    public boolean isInversePaddingMLUT() {
        return inversePaddingMLUT;
    }

}
