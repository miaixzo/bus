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
package org.miaixz.bus.core.center.date.culture;

import org.miaixz.bus.core.center.date.culture.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.lunar.LunarDay;

/**
 * Represents Zao Ma Tou (the mount of the Kitchen God) in Chinese culture.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Vesta extends Loops {

    /**
     * Array of Chinese numbers for display.
     */
    public static final String[] NUMBERS = { "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二" };

    /**
     * The SixtyCycle (Gan Zhi) of the first day of the first lunar month.
     */
    protected SixtyCycle firstDaySixtyCycle;

    /**
     * Constructs a {@code Vesta} instance for a given lunar year.
     *
     * @param lunarYear The lunar year.
     */
    public Vesta(int lunarYear) {
        firstDaySixtyCycle = LunarDay.fromYmd(lunarYear, 1, 1).getSixtyCycle();
    }

    /**
     * Creates a {@code Vesta} instance from a lunar year.
     *
     * @param lunarYear The lunar year.
     * @return A new {@code Vesta} instance.
     */
    public static Vesta fromLunarYear(int lunarYear) {
        return new Vesta(lunarYear);
    }

    /**
     * Calculates a number based on the Heaven Stem of the first day of the first lunar month.
     *
     * @param n The number of steps to move from the Heaven Stem.
     * @return The Chinese number corresponding to the calculated position.
     */
    protected String byHeavenStem(int n) {
        return NUMBERS[firstDaySixtyCycle.getHeavenStem().stepsTo(n)];
    }

    /**
     * Calculates a number based on the Earth Branch of the first day of the first lunar month.
     *
     * @param n The number of steps to move from the Earth Branch.
     * @return The Chinese number corresponding to the calculated position.
     */
    protected String byEarthBranch(int n) {
        return NUMBERS[firstDaySixtyCycle.getEarthBranch().stepsTo(n)];
    }

    /**
     * Gets the "Mouse Stealing Grain" saying for the year.
     *
     * @return The "Mouse Stealing Grain" saying.
     */
    public String getMouse() {
        return String.format("%s鼠偷粮", byEarthBranch(0));
    }

    /**
     * Gets the "Grass Seed Points" saying for the year.
     *
     * @return The "Grass Seed Points" saying.
     */
    public String getGrass() {
        return String.format("草子%s分", byEarthBranch(0));
    }

    /**
     * Gets the "Cattle Plowing Field" saying for the year (based on the first Chou day of the first lunar month).
     *
     * @return The "Cattle Plowing Field" saying.
     */
    public String getCattle() {
        return String.format("%s牛耕田", byEarthBranch(1));
    }

    /**
     * Gets the "Flower Harvest Points" saying for the year.
     *
     * @return The "Flower Harvest Points" saying.
     */
    public String getFlower() {
        return String.format("花收%s分", byEarthBranch(3));
    }

    /**
     * Gets the "Dragon Governing Water" saying for the year (based on the first Chen day of the first lunar month).
     *
     * @return The "Dragon Governing Water" saying.
     */
    public String getDragon() {
        return String.format("%s龙治水", byEarthBranch(4));
    }

    /**
     * Gets the "Horse Carrying Grain" saying for the year.
     *
     * @return The "Horse Carrying Grain" saying.
     */
    public String getHorse() {
        return String.format("%s马驮谷", byEarthBranch(6));
    }

    /**
     * Gets the "Chicken Snatching Rice" saying for the year.
     *
     * @return The "Chicken Snatching Rice" saying.
     */
    public String getChicken() {
        return String.format("%s鸡抢米", byEarthBranch(9));
    }

    /**
     * Gets the "Aunt Watching Silkworms" saying for the year.
     *
     * @return The "Aunt Watching Silkworms" saying.
     */
    public String getSilkworm() {
        return String.format("%s姑看蚕", byEarthBranch(9));
    }

    /**
     * Gets the "Butcher Sharing Pig" saying for the year.
     *
     * @return The "Butcher Sharing Pig" saying.
     */
    public String getPig() {
        return String.format("%s屠共猪", byEarthBranch(11));
    }

    /**
     * Gets the "Jia Field Points" saying for the year.
     *
     * @return The "Jia Field Points" saying.
     */
    public String getField() {
        return String.format("甲田%s分", byHeavenStem(0));
    }

    /**
     * Gets the "People Sharing Cake" saying for the year (based on the first Bing day of the first lunar month).
     *
     * @return The "People Sharing Cake" saying.
     */
    public String getCake() {
        return String.format("%s人分饼", byHeavenStem(2));
    }

    /**
     * Gets the "Days to Get Gold" saying for the year (based on the first Xin day of the first lunar month).
     *
     * @return The "Days to Get Gold" saying.
     */
    public String getGold() {
        return String.format("%s日得金", byHeavenStem(7));
    }

    /**
     * Gets the "People and Cakes" saying for the year.
     *
     * @return The "People and Cakes" saying.
     */
    public String getPeopleCakes() {
        return String.format("%s人%s丙", byEarthBranch(2), byHeavenStem(2));
    }

    /**
     * Gets the "People and Hoes" saying for the year.
     *
     * @return The "People and Hoes" saying.
     */
    public String getPeopleHoes() {
        return String.format("%s人%s锄", byEarthBranch(2), byHeavenStem(3));
    }

    /**
     * Gets the name of this object.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return "灶马头";
    }

}
