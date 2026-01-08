/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.data;

import java.time.Year;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.PatternKit;

/**
 * VIN is an abbreviation for Vehicle Identification Number. A VIN is a globally unique identifier for a vehicle,
 * consisting of 17 digits and letters.
 * <p>
 * Different digits represent different meanings, as explained below:
 * <ul>
 * <li>Digits 1-3: WMI (World Manufacturer Identifier), representing vehicle manufacturer information.</li>
 * <li>Digits 4-8: VDS (Vehicle Descriptor Section), representing vehicle brand, series, model, displacement, and other
 * information.</li>
 * <li>Digit 9: Check digit, calculated by a formula to verify the correctness of the VIN.</li>
 * <li>Digit 10: Year code, representing the year of vehicle production.</li>
 * <li>Digit 11: Plant code, representing the vehicle production plant information.</li>
 * <li>Digits 12-17: Sequence number, representing the vehicle's production sequence number.</li>
 * </ul>
 * The VIN can be used to find detailed personal, engineering, and manufacturing information about a car, and is an
 * important basis for determining the legality and history of a car.
 * <p>
 * This implementation refers to the following standards:
 * <ul>
 * <li><a href="https://www.iso.org/standard/52200.html">ISO 3779</a></li>
 * <li><a href="http://www.catarc.org.cn/upload/202004/24/202004241005284241.pdf">Vehicle Identification Number
 * Management Measures</a></li>
 * <li><a href="https://en.wikipedia.org/wiki/Vehicle_identification_number">Wikipedia</a></li>
 * <li><a href="https://openstd.samr.gov.cn/bzgk/gb/newGbInfo?hcno=E2EBF667F8C032B1EDFD6DF9C1114E02">GB
 * 16735-2019</a></li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VIN {

    /**
     * Weighting factors, see Appendix A, Table A.3
     */
    private static final int[] WEIGHT = { 8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2 };
    /**
     * The cycle of the year code.
     */
    private static final int YEAR_LOOP = 30;
    /**
     * The characters used to represent the year.
     */
    private static final char[] YEAR_ID = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'R',
            'S', 'T', 'V', 'W', 'X', 'Y', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
    /**
     * A map from year characters to their corresponding index.
     */
    private static final Map<Character, Integer> YEAR_MAP;

    static {
        final Map<Character, Integer> yearMap = new HashMap<>(YEAR_ID.length, 1);
        for (int i = 0; i < YEAR_ID.length; i++) {
            yearMap.put(YEAR_ID[i], i);
        }
        YEAR_MAP = MapKit.view(yearMap);
    }

    /**
     * The VIN code.
     */
    private final String code;

    /**
     * Constructor.
     *
     * @param vinCode The VIN code.
     */
    public VIN(final String vinCode) {
        Assert.isTrue(verify(vinCode), "Invalid VIN code!");
        this.code = vinCode;
    }

    /**
     * Creates a VIN object.
     *
     * @param vinCode The VIN code.
     * @return A VIN object.
     */
    public static VIN of(final String vinCode) {
        return new VIN(vinCode);
    }

    /**
     * Verifies the validity of the VIN code. It requires:
     * <ul>
     * <li>Matching the regular expression: {@link Pattern#CAR_VIN_PATTERN}</li>
     * <li>The 9th digit (check digit) must match the calculated check digit.</li>
     * </ul>
     *
     * @param vinCode The VIN code.
     * @return {@code true} if the VIN code is valid, {@code false} otherwise.
     */
    public static boolean verify(final String vinCode) {
        Assert.notBlank(vinCode, "VIN code must be not blank!");
        if (!PatternKit.isMatch(Pattern.CAR_VIN_PATTERN, vinCode)) {
            return false;
        }

        return vinCode.charAt(8) == calculateVerifyCode(vinCode);
    }

    /**
     * Calculates the check digit, see Appendix A.
     *
     * @param vinCode The VIN code.
     * @return The check digit.
     */
    private static char calculateVerifyCode(final String vinCode) {
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += getWeightFactor(vinCode, i);
        }

        final int factor = sum % 11;
        return factor != 10 ? (char) (factor + '0') : 'X';
    }

    /**
     * Gets the weight factor for the character at the specified position.
     *
     * @param vinCode The VIN code.
     * @param i       The position.
     * @return The weight factor.
     */
    private static int getWeightFactor(final String vinCode, final int i) {
        final char c = vinCode.charAt(i);
        return getVinValue(c) * WEIGHT[i];
    }

    /**
     * Gets the corresponding value for the letter (see Appendix A, Table A.2).
     *
     * @param vinCodeChar The character in the VIN code.
     * @return The corresponding value.
     */
    private static int getVinValue(final char vinCodeChar) {
        switch (vinCodeChar) {
            case '0':
                return 0;

            case '1':
            case 'J':
            case 'A':
                return 1;

            case '2':
            case 'S':
            case 'K':
            case 'B':
                return 2;

            case '3':
            case 'T':
            case 'L':
            case 'C':
                return 3;

            case '4':
            case 'U':
            case 'M':
            case 'D':
                return 4;

            case '5':
            case 'V':
            case 'N':
            case 'E':
                return 5;

            case '6':
            case 'W':
            case 'F':
                return 6;

            case '7':
            case 'P':
            case 'X':
            case 'G':
                return 7;

            case '8':
            case 'Y':
            case 'H':
                return 8;

            case '9':
            case 'Z':
            case 'R':
                return 9;
        }
        throw new IllegalArgumentException("Invalid VIN char: " + vinCodeChar);
    }

    /**
     * Gets the VIN code.
     *
     * @return The VIN code.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Gets the country or region code.
     *
     * @return The country or region code.
     */
    public String getCountryCode() {
        return this.code.substring(0, 2);
    }

    /**
     * Gets the World Manufacturer Identifier (WMI). For manufacturers of 1000 or more complete or incomplete vehicles
     * per year, the first part of the VIN is the WMI. For manufacturers of less than 1000 complete and/or incomplete
     * vehicles per year, the third, fourth, and fifth digits of the third part, together with the three digits of the
     * first part, constitute the WMI.
     *
     * @return The WMI.
     */
    public String getWMI() {
        final String wmi = this.code.substring(0, 3);
        return isLessThan1000() ? wmi + this.code.substring(11, 14) : wmi;
    }

    /**
     * Checks if the manufacturer produces less than 1000 vehicles per year.
     *
     * @return {@code true} if the annual production is less than 1000, {@code false} otherwise.
     */
    public boolean isLessThan1000() {
        return '9' == this.code.charAt(2);
    }

    /**
     * Gets the Vehicle Descriptor Section (VDS).
     *
     * @return The VDS value.
     */
    public String getVDS() {
        return this.code.substring(3, 9);
    }

    /**
     * Gets the Vehicle Descriptor Code, which, relative to the VDS, does not include the check digit.
     *
     * @return The Vehicle Descriptor Code.
     */
    public String getVehicleDescriptorCode() {
        return this.code.substring(3, 8);
    }

    /**
     * Gets the Vehicle Indicator Section (VIS).
     *
     * @return The VIS.
     */
    public String getVIS() {
        return this.code.substring(9);
    }

    /**
     * Get year.
     *
     * @return the year
     */
    public Year getYear() {
        return getYear(1);
    }

    /**
     * Gets the assembly plant code defined by the manufacturer.
     *
     * @return The assembly plant code.
     */
    public char getOemCode() {
        return this.code.charAt(10);
    }

    /**
     * Gets year.
     *
     * @param multiple 1 represents the first 30-year cycle starting from 1980.
     * @return the year
     */
    public Year getYear(final int multiple) {
        final int year = 1980 + YEAR_LOOP * multiple + YEAR_MAP.get(this.code.charAt(9)) % YEAR_LOOP;
        return Year.of(year);
    }

    /**
     * Production sequence number. 6 digits for annual output greater than 1000, 3 digits for annual output less than
     * 1000.
     *
     * @return The production sequence number.
     */
    public String getProdNo() {
        return this.code.substring(isLessThan1000() ? 14 : 11, 17);
    }

}
