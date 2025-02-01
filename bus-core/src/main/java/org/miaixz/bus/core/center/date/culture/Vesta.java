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
package org.miaixz.bus.core.center.date.culture;

import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.lunar.LunarDay;

/**
 * 灶马头(灶神的坐骑)
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Vesta extends Loops {

    public static final String[] NUMBERS = { "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二" };

    /**
     * 正月初一的干支
     */
    protected SixtyCycle firstDaySixtyCycle;

    public Vesta(int lunarYear) {
        firstDaySixtyCycle = LunarDay.fromYmd(lunarYear, 1, 1).getSixtyCycle();
    }

    public static Vesta fromLunarYear(int lunarYear) {
        return new Vesta(lunarYear);
    }

    protected String byHeavenStem(int n) {
        return NUMBERS[firstDaySixtyCycle.getHeavenStem().stepsTo(n)];
    }

    protected String byEarthBranch(int n) {
        return NUMBERS[firstDaySixtyCycle.getEarthBranch().stepsTo(n)];
    }

    /**
     * 几鼠偷粮
     *
     * @return 几鼠偷粮
     */
    public String getMouse() {
        return String.format("%s鼠偷粮", byEarthBranch(0));
    }

    /**
     * 草子几分
     *
     * @return 草子几分
     */
    public String getGrass() {
        return String.format("草子%s分", byEarthBranch(0));
    }

    /**
     * 几牛耕田（正月第一个丑日是初几，就是几牛耕田）
     *
     * @return 几牛耕田
     */
    public String getCattle() {
        return String.format("%s牛耕田", byEarthBranch(1));
    }

    /**
     * 花收几分
     *
     * @return 花收几分
     */
    public String getFlower() {
        return String.format("花收%s分", byEarthBranch(3));
    }

    /**
     * 几龙治水（正月第一个辰日是初几，就是几龙治水）
     *
     * @return 几龙治水
     */
    public String getDragon() {
        return String.format("%s龙治水", byEarthBranch(4));
    }

    /**
     * 几马驮谷
     *
     * @return 几马驮谷
     */
    public String getHorse() {
        return String.format("%s马驮谷", byEarthBranch(6));
    }

    /**
     * 几鸡抢米
     *
     * @return 几鸡抢米
     */
    public String getChicken() {
        return String.format("%s鸡抢米", byEarthBranch(9));
    }

    /**
     * 几姑看蚕
     *
     * @return 几姑看蚕
     */
    public String getSilkworm() {
        return String.format("%s姑看蚕", byEarthBranch(9));
    }

    /**
     * 几屠共猪
     *
     * @return 几屠共猪
     */
    public String getPig() {
        return String.format("%s屠共猪", byEarthBranch(11));
    }

    /**
     * 甲田几分
     *
     * @return 甲田几分
     */
    public String getField() {
        return String.format("甲田%s分", byHeavenStem(0));
    }

    /**
     * 几人分饼（正月第一个丙日是初几，就是几人分饼）
     *
     * @return 几人分饼
     */
    public String getCake() {
        return String.format("%s人分饼", byHeavenStem(2));
    }

    /**
     * 几日得金（正月第一个辛日是初几，就是几日得金）
     *
     * @return 几日得金
     */
    public String getGold() {
        return String.format("%s日得金", byHeavenStem(7));
    }

    /**
     * 几人几丙
     *
     * @return 几人几丙
     */
    public String getPeopleCakes() {
        return String.format("%s人%s丙", byEarthBranch(2), byHeavenStem(2));
    }

    /**
     * 几人几锄
     *
     * @return 几人几锄
     */
    public String getPeopleHoes() {
        return String.format("%s人%s锄", byEarthBranch(2), byHeavenStem(3));
    }

    @Override
    public String getName() {
        return "灶马头";
    }
}
