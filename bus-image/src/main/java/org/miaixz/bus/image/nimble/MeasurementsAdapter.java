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
package org.miaixz.bus.image.nimble;

import java.util.Objects;

/**
 * Adapter for converting between pixel coordinates and calibrated measurements.
 *
 * @param calibrationRatio the calibration ratio.
 * @param offsetX          the offset x.
 * @param offsetY          the offset y.
 * @param upYAxis          the up yaxis.
 * @param imageHeight      the image height.
 * @param unit             the unit.
 * @author Kimi Liu
 * @since Java 21+
 */
public record MeasurementsAdapter(double calibrationRatio, int offsetX, int offsetY, boolean upYAxis, int imageHeight,
        String unit) {

    /**
     * The default calibration ratio value.
     */
    private static final double DEFAULT_CALIBRATION_RATIO = 1.0;

    /**
     * The default unit value.
     */
    private static final String DEFAULT_UNIT = "px";

    /**
     * Creates a new instance.
     *
     * @param calibrationRatio the calibration ratio.
     * @param offsetX          the offset x.
     * @param offsetY          the offset y.
     * @param upYAxis          the up y axis.
     * @param imageHeight      the image height.
     * @param unit             the unit.
     */
    public MeasurementsAdapter {
        if (Double.isNaN(calibrationRatio) || Double.isInfinite(calibrationRatio) || calibrationRatio <= 0) {
            throw new IllegalArgumentException("Calibration ratio must be a positive finite number");
        }
        imageHeight = Math.max(0, imageHeight - 1);
        unit = Objects.requireNonNullElse(unit, DEFAULT_UNIT);
    }

    /**
     * Creates a new instance.
     *
     * @param calibrationRatio the calibration ratio.
     * @param offsetX          the offset x.
     * @param offsetY          the offset y.
     * @param upYAxis          the up y axis.
     * @param imageHeight      the image height.
     * @param unit             the unit.
     */
    public MeasurementsAdapter(double calibrationRatio, int offsetX, int offsetY, boolean upYAxis, int imageHeight,
            Unit unit) {
        this(calibrationRatio, offsetX, offsetY, upYAxis, imageHeight,
                unit == null ? DEFAULT_UNIT : unit.getAbbreviation());
    }

    /**
     * Returns the x uncalibrated value.
     *
     * @param xVal the x val.
     * @return the x uncalibrated value.
     */
    public double getXUncalibratedValue(double xVal) {
        return xVal + offsetX;
    }

    /**
     * Returns the y uncalibrated value.
     *
     * @param yVal the y val.
     * @return the y uncalibrated value.
     */
    public double getYUncalibratedValue(double yVal) {
        return (upYAxis ? imageHeight - yVal : yVal) + offsetY;
    }

    /**
     * Returns the x calibrated value.
     *
     * @param xVal the x val.
     * @return the x calibrated value.
     */
    public double getXCalibratedValue(double xVal) {
        return calibrationRatio * getXUncalibratedValue(xVal);
    }

    /**
     * Returns the y calibrated value.
     *
     * @param yVal the y val.
     * @return the y calibrated value.
     */
    public double getYCalibratedValue(double yVal) {
        return calibrationRatio * getYUncalibratedValue(yVal);
    }

    /**
     * Checks whether the calibrated condition is true.
     *
     * @return true if the calibrated condition is true; otherwise false.
     */
    public boolean isCalibrated() {
        return Double.compare(calibrationRatio, DEFAULT_CALIBRATION_RATIO) != 0;
    }

}
