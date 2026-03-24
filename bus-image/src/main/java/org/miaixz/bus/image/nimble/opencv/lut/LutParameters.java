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

/**
 * Implementation of the LUT parameters. No test is required for this class
 *
 * @author Kimi Liu
 * @since Java 21+
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
        return Objects.hash(
                intercept,
                slope,
                paddingMinValue,
                paddingMaxValue,
                bitsStored,
                signed,
                applyPadding,
                outputSigned,
                bitsOutput,
                inversePaddingMLUT);
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
