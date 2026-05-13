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

/**
 * Immutable record representing comprehensive lookup table transformation parameters.
 * <p>
 * This record encapsulates all parameters required for DICOM-compliant image pixel value transformations, including:
 * <ul>
 * <li>Linear transformation coefficients (intercept and slope)
 * <li>Pixel padding configuration and values
 * <li>Bit depth and signedness specifications for input and output
 * <li>Modality LUT padding inversion settings
 * </ul>
 * <p>
 * The transformation follows the DICOM standard formula: {@code output = input * slope +
 * intercept}
 * <p>
 * Padding values are handled according to DICOM PS3.3 specifications, where pixels matching the padding range are
 * excluded from display calculations.
 *
 * @param intercept          the intercept value for linear transformation (DICOM tag 0028,1052)
 * @param slope              the slope value for linear transformation (DICOM tag 0028,1053)
 * @param applyPadding       whether pixel padding should be applied during transformation
 * @param paddingMinValue    the minimum padding value, or {@code null} if not specified
 * @param paddingMaxValue    the maximum padding value, or {@code null} if not specified
 * @param bitsStored         the number of bits used to store each pixel value (1-32)
 * @param signed             whether the input pixel values are signed
 * @param outputSigned       whether the output pixel values should be signed
 * @param bitsOutput         the number of bits in the output pixel values (1-32)
 * @param inversePaddingMLUT whether to inverse padding for modality LUT
 * @author Kimi Liu
 * @since Java 21+
 */
public record LutParameters(double intercept, double slope, boolean applyPadding, Integer paddingMinValue,
        Integer paddingMaxValue, int bitsStored, boolean signed, boolean outputSigned, int bitsOutput,
        boolean inversePaddingMLUT) {

    /**
     * Creates a new instance.
     *
     * @param intercept          the intercept.
     * @param slope              the slope.
     * @param applyPadding       the apply padding.
     * @param paddingMinValue    the padding min value.
     * @param paddingMaxValue    the padding max value.
     * @param bitsStored         the bits stored.
     * @param signed             the signed.
     * @param outputSigned       the output signed.
     * @param bitsOutput         the bits output.
     * @param inversePaddingMLUT the inverse padding mlut.
     */
    public LutParameters {
        if (bitsStored < 1 || bitsStored > 32) {
            throw new IllegalArgumentException("bitsStored must be between 1 and 32, got: " + bitsStored);
        }
        if (bitsOutput < 1 || bitsOutput > 32) {
            throw new IllegalArgumentException("bitsOutput must be between 1 and 32, got: " + bitsOutput);
        }
    }

    /**
     * Returns the intercept.
     *
     * @return the intercept.
     */
    public double getIntercept() {
        return intercept;
    }

    /**
     * Returns the slope.
     *
     * @return the slope.
     */
    public double getSlope() {
        return slope;
    }

    /**
     * Checks whether the apply padding condition is true.
     *
     * @return true if the apply padding condition is true; otherwise false.
     */
    public boolean isApplyPadding() {
        return applyPadding;
    }

    /**
     * Returns the padding min value.
     *
     * @return the padding min value.
     */
    public Integer getPaddingMinValue() {
        return paddingMinValue;
    }

    /**
     * Returns the padding max value.
     *
     * @return the padding max value.
     */
    public Integer getPaddingMaxValue() {
        return paddingMaxValue;
    }

    /**
     * Returns the bits stored.
     *
     * @return the bits stored.
     */
    public int getBitsStored() {
        return bitsStored;
    }

    /**
     * Checks whether the signed condition is true.
     *
     * @return true if the signed condition is true; otherwise false.
     */
    public boolean isSigned() {
        return signed;
    }

    /**
     * Checks whether the output signed condition is true.
     *
     * @return true if the output signed condition is true; otherwise false.
     */
    public boolean isOutputSigned() {
        return outputSigned;
    }

    /**
     * Returns the bits output.
     *
     * @return the bits output.
     */
    public int getBitsOutput() {
        return bitsOutput;
    }

    /**
     * Checks whether the inverse padding mlut condition is true.
     *
     * @return true if the inverse padding mlut condition is true; otherwise false.
     */
    public boolean isInversePaddingMLUT() {
        return inversePaddingMLUT;
    }

}
