/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org 6tail and other contributors.              *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.date;

import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 八字
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EightChar {

    /**
     * 长生十二神
     */
    public static final String[] CHANG_SHENG = {
            "长生", "沐浴", "冠带", "临官", "帝旺", "衰", "病", "死", "墓", "绝", "胎", "养"
    };
    /**
     * 月支，按正月起寅排列
     */
    private static final String[] MONTH_ZHI = {
            "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥", "子", "丑"
    };
    /**
     * 长生十二神日干偏移值，五阳干顺推，五阴干逆推
     */
    private static final Map<String, Integer> CHANG_SHENG_OFFSET = new HashMap<>() {
        private static final long serialVersionUID = 1L;

        {
            //阳
            put("甲", 1);
            put("丙", 10);
            put("戊", 10);
            put("庚", 7);
            put("壬", 4);
            //阴
            put("乙", 6);
            put("丁", 9);
            put("己", 9);
            put("辛", 0);
            put("癸", 3);
        }
    };

    /**
     * 流派，2晚子时日柱按当天，1晚子时日柱按明天
     */
    protected int sect = 2;
    /**
     * 农历信息
     */
    protected Lunar lunar;

    public EightChar(Lunar lunar) {
        this.lunar = lunar;
    }

    public static EightChar fromLunar(Lunar lunar) {
        return new EightChar(lunar);
    }

    /**
     * 获取流派
     *
     * @return 流派，2晚子时日柱按当天，1晚子时日柱按明天
     */
    public int getSect() {
        return sect;
    }

    /**
     * 设置流派
     *
     * @param sect 流派，2晚子时日柱按当天，1晚子时日柱按明天，其他值默认为2
     */
    public void setSect(int sect) {
        this.sect = (1 == sect) ? 1 : 2;
    }

    /**
     * 获取年柱
     *
     * @return 年柱
     */
    public String getYear() {
        return lunar.getYearInGanZhiExact();
    }

    /**
     * 获取年干
     *
     * @return 天干
     */
    public String getYearGan() {
        return lunar.getYearGanExact();
    }

    /**
     * 获取年支
     *
     * @return 地支
     */
    public String getYearZhi() {
        return lunar.getYearZhiExact();
    }

    /**
     * 获取年柱地支藏干，由于藏干分主气、余气、杂气，所以返回结果可能为1到3个元素
     *
     * @return 天干
     */
    public List<String> getYearHideGan() {
        return Lunar.ZHI_HIDE_GAN.get(getYearZhi());
    }

    /**
     * 获取年柱五行
     *
     * @return 五行
     */
    public String getYearWuXing() {
        return Lunar.WU_XING_GAN.get(getYearGan()) + Lunar.WU_XING_ZHI.get(getYearZhi());
    }

    /**
     * 获取年柱纳音
     *
     * @return 纳音
     */
    public String getYearNaYin() {
        return Lunar.NAYIN.get(getYear());
    }

    /**
     * 获取年柱天干十神
     *
     * @return 十神
     */
    public String getYearShiShenGan() {
        return Lunar.SHI_SHEN_GAN.get(getDayGan() + getYearGan());
    }

    private List<String> getShiShenZhi(String zhi) {
        List<String> hideGan = Lunar.ZHI_HIDE_GAN.get(zhi);
        List<String> l = new ArrayList<>(hideGan.size());
        for (String gan : hideGan) {
            l.add(Lunar.SHI_SHEN_ZHI.get(getDayGan() + gan));
        }
        return l;
    }

    /**
     * 获取年柱地支十神，由于藏干分主气、余气、杂气，所以返回结果可能为1到3个元素
     *
     * @return 十神
     */
    public List<String> getYearShiShenZhi() {
        return getShiShenZhi(getYearZhi());
    }

    /**
     * 获取日干下标
     *
     * @return 日干下标，0-9
     */
    public int getDayGanIndex() {
        return 2 == sect ? lunar.getDayGanIndexExact2() : lunar.getDayGanIndexExact();
    }

    /**
     * 获取日支下标
     *
     * @return 日支下标，0-11
     */
    public int getDayZhiIndex() {
        return 2 == sect ? lunar.getDayZhiIndexExact2() : lunar.getDayZhiIndexExact();
    }

    private String getDiShi(int zhiIndex) {
        int index = CHANG_SHENG_OFFSET.get(getDayGan()) + (getDayGanIndex() % 2 == 0 ? zhiIndex : -zhiIndex);
        if (index >= 12) {
            index -= 12;
        }
        if (index < 0) {
            index += 12;
        }
        return CHANG_SHENG[index];
    }

    /**
     * 获取年柱地势（长生十二神）
     *
     * @return 地势
     */
    public String getYearDiShi() {
        return getDiShi(lunar.getYearZhiIndexExact());
    }

    /**
     * 获取月柱
     *
     * @return 月柱
     */
    public String getMonth() {
        return lunar.getMonthInGanZhiExact();
    }

    /**
     * 获取月干
     *
     * @return 天干
     */
    public String getMonthGan() {
        return lunar.getMonthGanExact();
    }

    /**
     * 获取月支
     *
     * @return 地支
     */
    public String getMonthZhi() {
        return lunar.getMonthZhiExact();
    }

    /**
     * 获取月柱地支藏干，由于藏干分主气、余气、杂气，所以返回结果可能为1到3个元素
     *
     * @return 天干
     */
    public List<String> getMonthHideGan() {
        return Lunar.ZHI_HIDE_GAN.get(getMonthZhi());
    }

    /**
     * 获取月柱五行
     *
     * @return 五行
     */
    public String getMonthWuXing() {
        return Lunar.WU_XING_GAN.get(getMonthGan()) + Lunar.WU_XING_ZHI.get(getMonthZhi());
    }

    /**
     * 获取月柱纳音
     *
     * @return 纳音
     */
    public String getMonthNaYin() {
        return Lunar.NAYIN.get(getMonth());
    }

    /**
     * 获取月柱天干十神
     *
     * @return 十神
     */
    public String getMonthShiShenGan() {
        return Lunar.SHI_SHEN_GAN.get(getDayGan() + getMonthGan());
    }

    /**
     * 获取月柱地支十神，由于藏干分主气、余气、杂气，所以返回结果可能为1到3个元素
     *
     * @return 十神
     */
    public List<String> getMonthShiShenZhi() {
        return getShiShenZhi(getMonthZhi());
    }

    /**
     * 获取月柱地势（长生十二神）
     *
     * @return 地势
     */
    public String getMonthDiShi() {
        return getDiShi(lunar.getMonthZhiIndexExact());
    }

    /**
     * 获取日柱
     *
     * @return 日柱
     */
    public String getDay() {
        return 2 == sect ? lunar.getDayInGanZhiExact2() : lunar.getDayInGanZhiExact();
    }

    /**
     * 获取日干
     *
     * @return 天干
     */
    public String getDayGan() {
        return 2 == sect ? lunar.getDayGanExact2() : lunar.getDayGanExact();
    }

    /**
     * 获取日支
     *
     * @return 地支
     */
    public String getDayZhi() {
        return 2 == sect ? lunar.getDayZhiExact2() : lunar.getDayZhiExact();
    }

    /**
     * 获取日柱地支藏干，由于藏干分主气、余气、杂气，所以返回结果可能为1到3个元素
     *
     * @return 天干
     */
    public List<String> getDayHideGan() {
        return Lunar.ZHI_HIDE_GAN.get(getDayZhi());
    }

    /**
     * 获取日柱五行
     *
     * @return 五行
     */
    public String getDayWuXing() {
        return Lunar.WU_XING_GAN.get(getDayGan()) + Lunar.WU_XING_ZHI.get(getDayZhi());
    }

    /**
     * 获取日柱纳音
     *
     * @return 纳音
     */
    public String getDayNaYin() {
        return Lunar.NAYIN.get(getDay());
    }

    /**
     * 获取日柱天干十神，也称日元、日干
     *
     * @return 十神
     */
    public String getDayShiShenGan() {
        return "日主";
    }

    /**
     * 获取日柱地支十神，由于藏干分主气、余气、杂气，所以返回结果可能为1到3个元素
     *
     * @return 十神
     */
    public List<String> getDayShiShenZhi() {
        return getShiShenZhi(getDayZhi());
    }

    /**
     * 获取日柱地势（长生十二神）
     *
     * @return 地势
     */
    public String getDayDiShi() {
        return getDiShi(getDayZhiIndex());
    }

    /**
     * 获取时柱
     *
     * @return 时柱
     */
    public String getTime() {
        return lunar.getTimeInGanZhi();
    }

    /**
     * 获取时干
     *
     * @return 天干
     */
    public String getTimeGan() {
        return lunar.getTimeGan();
    }

    /**
     * 获取时支
     *
     * @return 地支
     */
    public String getTimeZhi() {
        return lunar.getTimeZhi();
    }

    /**
     * 获取时柱地支藏干，由于藏干分主气、余气、杂气，所以返回结果可能为1到3个元素
     *
     * @return 天干
     */
    public List<String> getTimeHideGan() {
        return Lunar.ZHI_HIDE_GAN.get(getTimeZhi());
    }

    /**
     * 获取时柱五行
     *
     * @return 五行
     */
    public String getTimeWuXing() {
        return Lunar.WU_XING_GAN.get(lunar.getTimeGan()) + Lunar.WU_XING_ZHI.get(lunar.getTimeZhi());
    }

    /**
     * 获取时柱纳音
     *
     * @return 纳音
     */
    public String getTimeNaYin() {
        return Lunar.NAYIN.get(getTime());
    }

    /**
     * 获取时柱天干十神
     *
     * @return 十神
     */
    public String getTimeShiShenGan() {
        return Lunar.SHI_SHEN_GAN.get(getDayGan() + getTimeGan());
    }

    /**
     * 获取时柱地支十神，由于藏干分主气、余气、杂气，所以返回结果可能为1到3个元素
     *
     * @return 十神
     */
    public List<String> getTimeShiShenZhi() {
        return getShiShenZhi(getTimeZhi());
    }

    /**
     * 获取时柱地势（长生十二神）
     *
     * @return 地势
     */
    public String getTimeDiShi() {
        return getDiShi(lunar.getTimeZhiIndex());
    }

    /**
     * 获取胎元
     *
     * @return 胎元
     */
    public String getTaiYuan() {
        int ganIndex = lunar.getMonthGanIndexExact() + 1;
        if (ganIndex >= 10) {
            ganIndex -= 10;
        }
        int zhiIndex = lunar.getMonthZhiIndexExact() + 3;
        if (zhiIndex >= 12) {
            zhiIndex -= 12;
        }
        return Fields.CN_GAN[ganIndex] + Fields.CN_ZHI[zhiIndex];
    }

    /**
     * 获取胎元纳音
     *
     * @return 纳音
     */
    public String getTaiYuanNaYin() {
        return Lunar.NAYIN.get(getTaiYuan());
    }

    /**
     * 获取胎息
     *
     * @return 胎息
     */
    public String getTaiXi() {
        int ganIndex = (2 == sect) ? lunar.getDayGanIndexExact2() : lunar.getDayGanIndexExact();
        int zhiIndex = (2 == sect) ? lunar.getDayZhiIndexExact2() : lunar.getDayZhiIndexExact();
        return Lunar.HE_GAN_5[ganIndex] + Lunar.HE_ZHI_6[zhiIndex];
    }

    /**
     * 获取胎息纳音
     *
     * @return 纳音
     */
    public String getTaiXiNaYin() {
        return Lunar.NAYIN.get(getTaiXi());
    }

    /**
     * 获取命宫
     *
     * @return 命宫
     */
    public String getMingGong() {
        int monthZhiIndex = 0;
        int timeZhiIndex = 0;
        for (int i = 0, j = MONTH_ZHI.length; i < j; i++) {
            String zhi = MONTH_ZHI[i];
            if (lunar.getMonthZhiExact().equals(zhi)) {
                monthZhiIndex = i;
            }
            if (lunar.getTimeZhi().equals(zhi)) {
                timeZhiIndex = i;
            }
        }
        int zhiIndex = 26 - (monthZhiIndex + timeZhiIndex);
        if (zhiIndex > 12) {
            zhiIndex -= 12;
        }
        int jiaZiIndex = Lunar.getJiaZiIndex(lunar.getMonthInGanZhiExact()) - (monthZhiIndex - zhiIndex);
        if (jiaZiIndex >= 60) {
            jiaZiIndex -= 60;
        }
        if (jiaZiIndex < 0) {
            jiaZiIndex += 60;
        }
        return Fields.CN_JIA_ZI[jiaZiIndex];
    }

    /**
     * 获取命宫纳音
     *
     * @return 纳音
     */
    public String getMingGongNaYin() {
        return Lunar.NAYIN.get(getMingGong());
    }

    /**
     * 获取身宫
     *
     * @return 身宫
     */
    public String getShenGong() {
        int monthZhiIndex = 0;
        int timeZhiIndex = 0;
        for (int i = 0, j = MONTH_ZHI.length; i < j; i++) {
            String zhi = MONTH_ZHI[i];
            if (lunar.getMonthZhiExact().equals(zhi)) {
                monthZhiIndex = i;
            }
            if (lunar.getTimeZhi().equals(zhi)) {
                timeZhiIndex = i;
            }
        }
        int zhiIndex = 2 + monthZhiIndex + timeZhiIndex;
        if (zhiIndex > 12) {
            zhiIndex -= 12;
        }
        int jiaZiIndex = Lunar.getJiaZiIndex(lunar.getMonthInGanZhiExact()) - (monthZhiIndex - zhiIndex);
        if (jiaZiIndex >= 60) {
            jiaZiIndex -= 60;
        }
        if (jiaZiIndex < 0) {
            jiaZiIndex += 60;
        }
        return Fields.CN_JIA_ZI[jiaZiIndex];
    }

    /**
     * 获取身宫纳音
     *
     * @return 纳音
     */
    public String getShenGongNaYin() {
        return Lunar.NAYIN.get(getShenGong());
    }

    public Lunar getLunar() {
        return lunar;
    }

    /**
     * 使用默认流派1获取运
     *
     * @param gender 性别：1男，0女
     * @return 运
     */
    public Yun getYun(int gender) {
        return getYun(gender, 1);
    }

    /**
     * 获取运
     *
     * @param gender 性别：1男，0女
     * @param sect   流派，1按天数和时辰数计算，3天1年，1天4个月，1时辰10天；2按分钟数计算
     * @return 运
     */
    public Yun getYun(int gender, int sect) {
        return new Yun(this, gender, sect);
    }

    /**
     * 获取年柱所在旬
     *
     * @return 旬
     */
    public String getYearXun() {
        return lunar.getYearXunExact();
    }

    /**
     * 获取年柱旬空(空亡)
     *
     * @return 旬空(空亡)
     */
    public String getYearXunKong() {
        return lunar.getYearXunKongExact();
    }

    /**
     * 获取月柱所在旬
     *
     * @return 旬
     */
    public String getMonthXun() {
        return lunar.getMonthXunExact();
    }

    /**
     * 获取月柱旬空(空亡)
     *
     * @return 旬空(空亡)
     */
    public String getMonthXunKong() {
        return lunar.getMonthXunKongExact();
    }

    /**
     * 获取日柱所在旬
     *
     * @return 旬
     */
    public String getDayXun() {
        return 2 == sect ? lunar.getDayXunExact2() : lunar.getDayXunExact();
    }

    /**
     * 获取日柱旬空(空亡)
     *
     * @return 旬空(空亡)
     */
    public String getDayXunKong() {
        return 2 == sect ? lunar.getDayXunKongExact2() : lunar.getDayXunKongExact();
    }

    /**
     * 获取时柱所在旬
     *
     * @return 旬
     */
    public String getTimeXun() {
        return lunar.getTimeXun();
    }

    /**
     * 获取时柱旬空(空亡)
     *
     * @return 旬空(空亡)
     */
    public String getTimeXunKong() {
        return lunar.getTimeXunKong();
    }

    /**
     * 构建字符串内容
     *
     * @param args 可选参数-简化输出
     * @return 字符串内容
     */
    public String build(boolean... args) {
        return getYear() + Symbol.SPACE + getMonth() + Symbol.SPACE + getDay() + Symbol.SPACE + getTime();
    }

    /**
     * 大运
     */
    public static class DaYun {

        /**
         * 开始年(含)
         */
        private final int startYear;
        /**
         * 结束年(含)
         */
        private final int endYear;
        /**
         * 开始年龄(含)
         */
        private final int startAge;
        /**
         * 结束年龄(含)
         */
        private final int endAge;
        /**
         * 序数，0-9
         */
        private final int index;
        /**
         * 运
         */
        private final Yun yun;
        private final Lunar lunar;

        public DaYun(Yun yun, int index) {
            this.yun = yun;
            this.lunar = yun.getLunar();
            this.index = index;
            int birthYear = lunar.getSolar().getYear();
            int year = yun.getStartSolar().getYear();
            if (index < 1) {
                this.startYear = birthYear;
                this.startAge = 1;
                this.endYear = year - 1;
                this.endAge = year - birthYear;
            } else {
                int add = (index - 1) * 10;
                this.startYear = year + add;
                this.startAge = this.startYear - birthYear + 1;
                this.endYear = this.startYear + 9;
                this.endAge = this.startAge + 9;
            }
        }

        public int getStartYear() {
            return startYear;
        }

        public int getEndYear() {
            return endYear;
        }

        public int getStartAge() {
            return startAge;
        }

        public int getEndAge() {
            return endAge;
        }

        public int getIndex() {
            return index;
        }

        public Lunar getLunar() {
            return lunar;
        }

        /**
         * 获取干支
         *
         * @return 干支
         */
        public String getGanZhi() {
            if (index < 1) {
                return Normal.EMPTY;
            }
            int offset = Lunar.getJiaZiIndex(lunar.getMonthInGanZhiExact());
            offset += yun.isForward() ? index : -index;
            int size = Fields.CN_JIA_ZI.length;
            if (offset >= size) {
                offset -= size;
            }
            if (offset < 0) {
                offset += size;
            }
            return Fields.CN_JIA_ZI[offset];
        }

        /**
         * 获取所在旬
         *
         * @return 旬
         */
        public String getXun() {
            return Lunar.getXun(getGanZhi());
        }

        /**
         * 获取旬空(空亡)
         *
         * @return 旬空(空亡)
         */
        public String getXunKong() {
            return Lunar.getXunKong(getGanZhi());
        }

        /**
         * 获取流年
         *
         * @return 流年
         */
        public LiuNian[] getLiuNian() {
            return getLiuNian(10);
        }

        /**
         * 获取流年
         *
         * @param n 轮数
         * @return 流年
         */
        public LiuNian[] getLiuNian(int n) {
            if (index < 1) {
                n = endYear - startYear + 1;
            }
            LiuNian[] l = new LiuNian[n];
            for (int i = 0; i < n; i++) {
                l[i] = new LiuNian(this, i);
            }
            return l;
        }

        /**
         * 获取10轮小运
         *
         * @return 小运
         */
        public XiaoYun[] getXiaoYun() {
            return getXiaoYun(10);
        }

        /**
         * 获取小运
         *
         * @param n 轮数
         * @return 小运
         */
        public XiaoYun[] getXiaoYun(int n) {
            if (index < 1) {
                n = endYear - startYear + 1;
            }
            XiaoYun[] l = new XiaoYun[n];
            for (int i = 0; i < n; i++) {
                l[i] = new XiaoYun(this, i, yun.isForward());
            }
            return l;
        }

    }

    /**
     * 流年
     */
    public static class LiuNian {

        /**
         * 序数，0-9
         */
        private final int index;
        /**
         * 大运
         */
        private final DaYun daYun;
        /**
         * 年
         */
        private final int year;
        /**
         * 年龄
         */
        private final int age;
        private final Lunar lunar;

        public LiuNian(DaYun daYun, int index) {
            this.daYun = daYun;
            this.lunar = daYun.getLunar();
            this.index = index;
            this.year = daYun.getStartYear() + index;
            this.age = daYun.getStartAge() + index;
        }

        public int getIndex() {
            return index;
        }

        public int getYear() {
            return year;
        }

        public int getAge() {
            return age;
        }

        /**
         * 获取干支
         *
         * @return 干支
         */
        public String getGanZhi() {
            // 干支与出生日期和起运日期都没关系
            int offset = Lunar.getJiaZiIndex(lunar.getSolarTermTable().get("立春").getLunar().getYearInGanZhiExact())
                    + this.index;
            if (daYun.getIndex() > 0) {
                offset += daYun.getStartAge() - 1;
            }
            offset %= Fields.CN_JIA_ZI.length;
            return Fields.CN_JIA_ZI[offset];
        }

        /**
         * 获取所在旬
         *
         * @return 旬
         */
        public String getXun() {
            return Lunar.getXun(getGanZhi());
        }

        /**
         * 获取旬空(空亡)
         *
         * @return 旬空(空亡)
         */
        public String getXunKong() {
            return Lunar.getXunKong(getGanZhi());
        }

        /**
         * 获取流月
         *
         * @return 流月
         */
        public LiuYue[] getLiuYue() {
            int n = 12;
            LiuYue[] l = new LiuYue[n];
            for (int i = 0; i < n; i++) {
                l[i] = new LiuYue(this, i);
            }
            return l;
        }

    }

    /**
     * 流月
     */
    public static class LiuYue {

        /**
         * 序数，0-9
         */
        private final int index;
        private final LiuNian liuNian;

        public LiuYue(LiuNian liuNian, int index) {
            this.liuNian = liuNian;
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        /**
         * 获取中文的月
         *
         * @return 中文月，如正
         */
        public String getMonthInChinese() {
            return Fields.CN_MONTH[index];
        }

        /**
         * 获取干支
         * 《五虎遁》
         * 甲己之年丙作首，
         * 乙庚之年戊为头，
         * 丙辛之年寻庚上，
         * 丁壬壬寅顺水流，
         * 若问戊癸何处走，
         * 甲寅之上好追求。
         *
         * @return 干支
         */
        public String getGanZhi() {
            int offset = 0;
            String yearGan = liuNian.getGanZhi().substring(0, 1);
            if ("甲".equals(yearGan) || "己".equals(yearGan)) {
                offset = 2;
            } else if ("乙".equals(yearGan) || "庚".equals(yearGan)) {
                offset = 4;
            } else if ("丙".equals(yearGan) || "辛".equals(yearGan)) {
                offset = 6;
            } else if ("丁".equals(yearGan) || "壬".equals(yearGan)) {
                offset = 8;
            }
            String gan = Fields.CN_GAN[(index + offset) % 10];
            String zhi = Fields.CN_ZHI[(index + Lunar.BASE_MONTH_ZHI_INDEX) % 12];
            return gan + zhi;
        }

        /**
         * 获取所在旬
         *
         * @return 旬
         */
        public String getXun() {
            return Lunar.getXun(getGanZhi());
        }

        /**
         * 获取旬空(空亡)
         *
         * @return 旬空(空亡)
         */
        public String getXunKong() {
            return Lunar.getXunKong(getGanZhi());
        }

    }

    /**
     * 小运
     */
    public static class XiaoYun {

        /**
         * 序数，0-9
         */
        private final int index;
        /**
         * 大运
         */
        private final DaYun daYun;
        /**
         * 年
         */
        private final int year;
        /**
         * 年龄
         */
        private final int age;
        /**
         * 是否顺推
         */
        private final boolean forward;
        private final Lunar lunar;

        public XiaoYun(DaYun daYun, int index, boolean forward) {
            this.daYun = daYun;
            this.lunar = daYun.getLunar();
            this.index = index;
            this.year = daYun.getStartYear() + index;
            this.age = daYun.getStartAge() + index;
            this.forward = forward;
        }

        public int getIndex() {
            return index;
        }

        public int getYear() {
            return year;
        }

        public int getAge() {
            return age;
        }

        /**
         * 获取干支
         *
         * @return 干支
         */
        public String getGanZhi() {
            int offset = Lunar.getJiaZiIndex(lunar.getTimeInGanZhi());
            int add = this.index + 1;
            if (daYun.getIndex() > 0) {
                add += daYun.getStartAge() - 1;
            }
            offset += forward ? add : -add;
            int size = Fields.CN_JIA_ZI.length;
            while (offset < 0) {
                offset += size;
            }
            offset %= size;
            return Fields.CN_JIA_ZI[offset];
        }

        /**
         * 获取所在旬
         *
         * @return 旬
         */
        public String getXun() {
            return Lunar.getXun(getGanZhi());
        }

        /**
         * 获取旬空(空亡)
         *
         * @return 旬空(空亡)
         */
        public String getXunKong() {
            return Lunar.getXunKong(getGanZhi());
        }

    }

    /**
     * 运
     */
    public static class Yun {

        /**
         * 性别(1男，0女)
         */
        private final int gender;
        /**
         * 是否顺推
         */
        private final boolean forward;
        /**
         * 农历信息
         */
        private final Lunar lunar;
        /**
         * 起运年数
         */
        private int startYear;
        /**
         * 起运月数
         */
        private int startMonth;
        /**
         * 起运天数
         */
        private int startDay;
        /**
         * 起运小时数
         */
        private int startHour;

        /**
         * 使用默认流派1初始化运
         *
         * @param eightChar 八字
         * @param gender    性别，1男，0女
         */
        public Yun(EightChar eightChar, int gender) {
            this(eightChar, gender, 1);
        }

        /**
         * 初始化运
         *
         * @param eightChar 八字
         * @param gender    性别，1男，0女
         * @param sect      流派，1按天数和时辰数计算，3天1年，1天4个月，1时辰10天；2按分钟数计算
         */
        public Yun(EightChar eightChar, int gender, int sect) {
            this.lunar = eightChar.getLunar();
            this.gender = gender;
            // 阳
            boolean yang = 0 == lunar.getYearGanIndexExact() % 2;
            // 男
            boolean man = 1 == gender;
            forward = (yang && man) || (!yang && !man);
            compute(sect);
        }

        /**
         * 起运计算
         */
        private void compute(int sect) {
            // 上节
            Lunar.SolarTerm prev = lunar.getPrevJie();
            // 下节
            Lunar.SolarTerm next = lunar.getNextJie();
            // 出生日期
            Solar current = lunar.getSolar();
            // 阳男阴女顺推，阴男阳女逆推
            Solar start = forward ? current : prev.getSolar();
            Solar end = forward ? next.getSolar() : current;

            int year;
            int month;
            int day;
            int hour = 0;

            if (2 == sect) {
                long minutes = end.subtractMinute(start);
                long y = minutes / 4320;
                minutes -= y * 4320;
                long m = minutes / 360;
                minutes -= m * 360;
                long d = minutes / 12;
                minutes -= d * 12;
                long h = minutes * 2;
                year = (int) y;
                month = (int) m;
                day = (int) d;
                hour = (int) h;
            } else {
                int endTimeZhiIndex = (end.getHour() == 23) ? 11 : Lunar.getTimeZhiIndex(end.build(false).substring(11, Normal._16));
                int startTimeZhiIndex = (start.getHour() == 23) ? 11 : Lunar.getTimeZhiIndex(start.build(false).substring(11, Normal._16));
                // 时辰差
                int hourDiff = endTimeZhiIndex - startTimeZhiIndex;
                // 天数差
                int dayDiff = end.subtract(start);
                if (hourDiff < 0) {
                    hourDiff += 12;
                    dayDiff--;
                }
                int monthDiff = hourDiff * 10 / 30;
                month = dayDiff * 4 + monthDiff;
                day = hourDiff * 10 - monthDiff * 30;
                year = month / 12;
                month = month - year * 12;
            }
            this.startYear = year;
            this.startMonth = month;
            this.startDay = day;
            this.startHour = hour;
        }

        /**
         * 获取性别
         *
         * @return 性别(1 : 男, 0 : 女)
         */
        public int getGender() {
            return gender;
        }

        /**
         * 获取起运年数
         *
         * @return 起运年数
         */
        public int getStartYear() {
            return startYear;
        }

        /**
         * 获取起运月数
         *
         * @return 起运月数
         */
        public int getStartMonth() {
            return startMonth;
        }

        /**
         * 获取起运天数
         *
         * @return 起运天数
         */
        public int getStartDay() {
            return startDay;
        }

        /**
         * 获取起运小时数
         *
         * @return 起运小时数
         */
        public int getStartHour() {
            return startHour;
        }

        /**
         * 是否顺推
         *
         * @return true/false
         */
        public boolean isForward() {
            return forward;
        }

        public Lunar getLunar() {
            return lunar;
        }

        /**
         * 获取起运的阳历日期
         *
         * @return 阳历日期
         */
        public Solar getStartSolar() {
            Solar solar = lunar.getSolar();
            solar = solar.nextYear(startYear);
            solar = solar.nextMonth(startMonth);
            solar = solar.next(startDay);
            return solar.nextHour(startHour);
        }

        /**
         * 获取10轮大运
         *
         * @return 大运
         */
        public DaYun[] getDaYun() {
            return getDaYun(10);
        }

        /**
         * 获取大运
         *
         * @param n 轮数
         * @return 大运
         */
        public DaYun[] getDaYun(int n) {
            DaYun[] l = new DaYun[n];
            for (int i = 0; i < n; i++) {
                l[i] = new DaYun(this, i);
            }
            return l;
        }

    }

}
