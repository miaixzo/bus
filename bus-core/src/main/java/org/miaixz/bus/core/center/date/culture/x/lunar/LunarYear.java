package org.miaixz.bus.core.center.date.culture.x.lunar;

import org.miaixz.bus.core.center.date.culture.Galaxy;
import org.miaixz.bus.core.center.date.culture.x.Literal;
import org.miaixz.bus.core.center.date.culture.x.Lunar;
import org.miaixz.bus.core.center.date.culture.x.NineStar;
import org.miaixz.bus.core.center.date.culture.x.Solar;
import org.miaixz.bus.core.lang.Normal;

import java.util.ArrayList;
import java.util.List;

/**
 * 农历年
 */
public class LunarYear {

    /**
     * 农历年
     */
    private final int year;

    /**
     * 天干下标
     */
    private final int ganIndex;

    /**
     * 地支下标
     */
    private final int zhiIndex;

    /**
     * 农历月们
     */
    private final List<LunarMonth> months = new ArrayList<>();

    /**
     * 节气儒略日们
     */
    private final List<Double> jieQiJulianDays = new ArrayList<>();

    /**
     * 初始化
     *
     * @param lunarYear 农历年
     */
    public LunarYear(int lunarYear) {
        this.year = lunarYear;
        int offset = lunarYear - 4;
        int yearGanIndex = offset % 10;
        int yearZhiIndex = offset % 12;
        if (yearGanIndex < 0) {
            yearGanIndex += 10;
        }
        if (yearZhiIndex < 0) {
            yearZhiIndex += 12;
        }
        this.ganIndex = yearGanIndex;
        this.zhiIndex = yearZhiIndex;
        compute();
    }

    private static boolean contains(int[] arr, int n) {
        for (int o : arr) {
            if (n == o) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过农历年初始化
     *
     * @param lunarYear 农历年
     * @return 农历年
     */
    public synchronized static LunarYear from(int lunarYear) {
        LunarYear y;
        if (null == Literal.LUNAR_YEAR_CACHE || Literal.LUNAR_YEAR_CACHE.getYear() != lunarYear) {
            y = new LunarYear(lunarYear);
            Literal.LUNAR_YEAR_CACHE = y;
        } else {
            y = Literal.LUNAR_YEAR_CACHE;
        }
        return y;
    }

    private void compute() {
        // 节气
        double[] jq = new double[27];
        // 合朔，即每月初一
        double[] hs = new double[16];
        // 每月天数
        int[] dayCounts = new int[15];
        int[] months = new int[15];

        int currentYear = this.year;

        double jd = Math.floor((currentYear - 2000) * 365.2422 + 180);
        // 355是2000.12冬至，得到较靠近jd的冬至估计值
        double w = Math.floor((jd - 355 + 183) / 365.2422) * 365.2422 + 355;
        if (Galaxy.calcQi(w) > jd) {
            w -= 365.2422;
        }
        // 25个节气时刻(北京时间)，从冬至开始到下一个冬至以后
        for (int i = 0; i < 26; i++) {
            jq[i] = Galaxy.calcQi(w + 15.2184 * i);
        }
        // 从上年的大雪到下年的立春
        for (int i = 0, j = Literal.LUNAR_JIE_QI_IN_USE.length; i < j; i++) {
            if (i == 0) {
                jd = Galaxy.qiAccurate2(jq[0] - 15.2184);
            } else if (i <= 26) {
                jd = Galaxy.qiAccurate2(jq[i - 1]);
            } else {
                jd = Galaxy.qiAccurate2(jq[25] + 15.2184 * (i - 26));
            }
            jieQiJulianDays.add(jd + Solar.J2000);
        }

        // 冬至前的初一，今年"首朔"的日月黄经差w
        w = Galaxy.calcShuo(jq[0]);
        if (w > jq[0]) {
            w -= 29.53;
        }
        // 递推每月初一
        for (int i = 0; i < 16; i++) {
            hs[i] = Galaxy.calcShuo(w + 29.5306 * i);
        }
        // 每月
        for (int i = 0; i < 15; i++) {
            dayCounts[i] = (int) (hs[i + 1] - hs[i]);
            months[i] = i;
        }

        int prevYear = currentYear - 1;
        int leapIndex = 16;
        if (contains(Literal.LUNAR_YEAR_LEAP_11, currentYear)) {
            leapIndex = 13;
        } else if (contains(Literal.LUNAR_YEAR_LEAP_12, currentYear)) {
            leapIndex = 14;
        } else if (hs[13] <= jq[24]) {
            int i = 1;
            while (hs[i + 1] > jq[2 * i] && i < 13) {
                i++;
            }
            leapIndex = i;
        }
        for (int i = leapIndex; i < 15; i++) {
            months[i] -= 1;
        }

        int fm = -1;
        int index = -1;
        int y = prevYear;
        for (int i = 0; i < 15; i++) {
            double dm = hs[i] + Solar.J2000;
            int v2 = months[i];
            int mc = Literal.LUNAR_YEAR_YMC[v2 % 12];
            if (1724360 <= dm && dm < 1729794) {
                mc = Literal.LUNAR_YEAR_YMC[(v2 + 1) % 12];
            } else if (1807724 <= dm && dm < 1808699) {
                mc = Literal.LUNAR_YEAR_YMC[(v2 + 1) % 12];
            } else if (dm == 1729794 || dm == 1808699) {
                mc = 12;
            }
            if (fm == -1) {
                fm = mc;
                index = mc;
            }
            if (mc < fm) {
                y += 1;
                index = 1;
            }
            fm = mc;
            if (i == leapIndex) {
                mc = -mc;
            } else if (dm == 1729794 || dm == 1808699) {
                mc = -11;
            }
            this.months.add(new LunarMonth(y, mc, dayCounts[i], hs[i] + Solar.J2000, index));
            index++;
        }
    }

    /**
     * 获取农历年
     *
     * @return 农历年
     */
    public int getYear() {
        return year;
    }

    /**
     * 获取总天数
     *
     * @return 天数
     */
    public int getDayCount() {
        int n = 0;
        for (LunarMonth m : months) {
            if (m.getYear() == year) {
                n += m.getDayCount();
            }
        }
        return n;
    }

    /**
     * 获取当年的农历月们
     *
     * @return 农历月们
     */
    public List<LunarMonth> getMonthInYear() {
        List<LunarMonth> l = new ArrayList<>();
        for (LunarMonth m : months) {
            if (m.getYear() == year) {
                l.add(m);
            }
        }
        return l;
    }

    /**
     * 获取农历月们
     *
     * @return 农历月们
     */
    public List<LunarMonth> getMonths() {
        return months;
    }

    /**
     * 获取节气儒略日们
     *
     * @return 节气儒略日们
     */
    public List<Double> getJieQiJulianDays() {
        return jieQiJulianDays;
    }

    /**
     * 获取天干序号，从0开始
     *
     * @return 序号
     */
    public int getGanIndex() {
        return ganIndex;
    }

    /**
     * 获取地支序号，从0开始
     *
     * @return 序号
     */
    public int getZhiIndex() {
        return zhiIndex;
    }

    /**
     * 获取天干
     *
     * @return 天干
     */
    public String getGan() {
        return Literal.LUNAR_GAN[ganIndex + 1];
    }

    /**
     * 获取地支
     *
     * @return 地支
     */
    public String getZhi() {
        return Literal.LUNAR_ZHI[zhiIndex];
    }

    /**
     * 获取干支
     *
     * @return 干支
     */
    public String getGanZhi() {
        return getGan() + getZhi();
    }

    /**
     * 获取农历月
     *
     * @param lunarMonth 月，1-12，闰月为负数，如闰2月为-2
     * @return 农历月
     */
    public LunarMonth getMonth(int lunarMonth) {
        for (LunarMonth m : months) {
            if (m.getYear() == year && m.getMonth() == lunarMonth) {
                return m;
            }
        }
        return null;
    }

    /**
     * 获取闰月
     *
     * @return 闰月数字，1代表闰1月，0代表无闰月
     */
    public int getLeapMonth() {
        for (LunarMonth m : months) {
            if (m.getYear() == year && m.isLeap()) {
                return Math.abs(m.getMonth());
            }
        }
        return 0;
    }

    protected String getZaoByGan(int index, String name) {
        int offset = index - Solar.fromJulianDay(getMonth(1).getFirstJulianDay()).getLunar().getDayGanIndex();
        if (offset < 0) {
            offset += 10;
        }
        return name.replaceFirst("几", Literal.LUNAR_NUMBER[offset + 1]);
    }

    protected String getZaoByZhi(int index, String name) {
        int offset = index - Solar.fromJulianDay(getMonth(1).getFirstJulianDay()).getLunar().getDayZhiIndex();
        if (offset < 0) {
            offset += 12;
        }
        return name.replaceFirst("几", Literal.LUNAR_NUMBER[offset + 1]);
    }

    /**
     * 获取几鼠偷粮
     *
     * @return 几鼠偷粮
     */
    public String getTouLiang() {
        return getZaoByZhi(0, "几鼠偷粮");
    }

    /**
     * 获取草子几分
     *
     * @return 草子几分
     */
    public String getCaoZi() {
        return getZaoByZhi(0, "草子几分");
    }

    /**
     * 获取耕田（正月第一个丑日是初几，就是几牛耕田）
     *
     * @return 耕田，如：六牛耕田
     */
    public String getGengTian() {
        return getZaoByZhi(1, "几牛耕田");
    }

    /**
     * 获取花收几分
     *
     * @return 花收几分
     */
    public String getHuaShou() {
        return getZaoByZhi(3, "花收几分");
    }

    /**
     * 获取治水（正月第一个辰日是初几，就是几龙治水）
     *
     * @return 治水，如：二龙治水
     */
    public String getZhiShui() {
        return getZaoByZhi(4, "几龙治水");
    }

    /**
     * 获取几马驮谷
     *
     * @return 几马驮谷
     */
    public String getTuoGu() {
        return getZaoByZhi(6, "几马驮谷");
    }

    /**
     * 获取几鸡抢米
     *
     * @return 几鸡抢米
     */
    public String getQiangMi() {
        return getZaoByZhi(9, "几鸡抢米");
    }

    /**
     * 获取几姑看蚕
     *
     * @return 几姑看蚕
     */
    public String getKanCan() {
        return getZaoByZhi(9, "几姑看蚕");
    }

    /**
     * 获取几屠共猪
     *
     * @return 几屠共猪
     */
    public String getGongZhu() {
        return getZaoByZhi(11, "几屠共猪");
    }

    /**
     * 获取甲田几分
     *
     * @return 甲田几分
     */
    public String getJiaTian() {
        return getZaoByGan(0, "甲田几分");
    }

    /**
     * 获取分饼（正月第一个丙日是初几，就是几人分饼）
     *
     * @return 分饼，如：六人分饼
     */
    public String getFenBing() {
        return getZaoByGan(2, "几人分饼");
    }

    /**
     * 获取得金（正月第一个辛日是初几，就是几日得金）
     *
     * @return 得金，如：一日得金
     */
    public String getDeJin() {
        return getZaoByGan(7, "几日得金");
    }

    /**
     * 获取几人几丙
     *
     * @return 几人几丙
     */
    public String getRenBing() {
        return getZaoByGan(2, getZaoByZhi(2, "几人几丙"));
    }

    /**
     * 获取几人几锄
     *
     * @return 几人几锄
     */
    public String getRenChu() {
        return getZaoByGan(3, getZaoByZhi(2, "几人几锄"));
    }

    /**
     * 获取三元
     *
     * @return 元
     */
    public String getYuan() {
        return Literal.LUNAR_YEAR_YUAN[((year + 2696) / 60) % 3] + "元";
    }

    /**
     * 获取九运
     *
     * @return 运
     */
    public String getYun() {
        return Literal.LUNAR_YEAR_YUN[((year + 2696) / 20) % 9] + "运";
    }

    /**
     * 获取九星
     *
     * @return 九星
     */
    public NineStar getNineStar() {
        int index = Lunar.getJiaZiIndex(getGanZhi()) + 1;
        int yuan = ((this.year + 2696) / 60) % 3;
        int offset = (62 + yuan * 3 - index) % 9;
        if (0 == offset) {
            offset = 9;
        }
        return NineStar.fromIndex(offset - 1);
    }

    /**
     * 获取喜神方位
     *
     * @return 方位，如艮
     */
    public String getPositionXi() {
        return Literal.LUNAR_POSITION_XI[ganIndex + 1];
    }

    /**
     * 获取喜神方位描述
     *
     * @return 方位描述，如东北
     */
    public String getPositionXiDesc() {
        return Literal.LUNAR_POSITION_DESC.get(getPositionXi());
    }

    /**
     * 获取阳贵神方位
     *
     * @return 方位，如艮
     */
    public String getPositionYangGui() {
        return Literal.LUNAR_POSITION_YANG_GUI[ganIndex + 1];
    }

    /**
     * 获取阳贵神方位描述
     *
     * @return 方位描述，如东北
     */
    public String getPositionYangGuiDesc() {
        return Literal.LUNAR_POSITION_DESC.get(getPositionYangGui());
    }

    /**
     * 获取阴贵神方位
     *
     * @return 方位，如艮
     */
    public String getPositionYinGui() {
        return Literal.LUNAR_POSITION_YIN_GUI[ganIndex + 1];
    }

    /**
     * 获取阴贵神方位描述
     *
     * @return 方位描述，如东北
     */
    public String getPositionYinGuiDesc() {
        return Literal.LUNAR_POSITION_DESC.get(getPositionYinGui());
    }

    /**
     * 获取福神方位（默认流派：2）
     *
     * @return 方位，如艮
     */
    public String getPositionFu() {
        return getPositionFu(2);
    }

    /**
     * 获取福神方位
     *
     * @param sect 流派，1或2
     * @return 方位，如艮
     */
    public String getPositionFu(int sect) {
        return (1 == sect ? Literal.LUNAR_POSITION_FU : Literal.LUNAR_POSITION_FU_2)[ganIndex + 1];
    }

    /**
     * 获取福神方位描述（默认流派：2）
     *
     * @return 方位描述，如东北
     */
    public String getPositionFuDesc() {
        return getPositionFuDesc(2);
    }

    /**
     * 获取福神方位描述
     *
     * @param sect 流派，1或2
     * @return 方位描述，如东北
     */
    public String getPositionFuDesc(int sect) {
        return Literal.LUNAR_POSITION_DESC.get(getPositionFu(sect));
    }

    /**
     * 获取财神方位
     *
     * @return 财神方位，如艮
     */
    public String getPositionCai() {
        return Literal.LUNAR_POSITION_CAI[ganIndex + 1];
    }

    /**
     * 获取财神方位描述
     *
     * @return 方位描述，如东北
     */
    public String getPositionCaiDesc() {
        return Literal.LUNAR_POSITION_DESC.get(getPositionCai());
    }

    /**
     * 获取太岁方位
     *
     * @return 方位，如艮
     */
    public String getPositionTaiSui() {
        return Literal.LUNAR_POSITION_TAI_SUI_YEAR[zhiIndex];
    }

    /**
     * 获取太岁方位描述
     *
     * @return 方位描述，如东北
     */
    public String getPositionTaiSuiDesc() {
        return Literal.LUNAR_POSITION_DESC.get(getPositionTaiSui());
    }

    @Override
    public String toString() {
        return year + Normal.EMPTY;
    }

    public String toFullString() {
        return year + "年";
    }

    /**
     * 获取往后推几年的阴历年，如果要往前推，则年数用负数
     *
     * @param n 年数
     * @return 阴历年
     */
    public LunarYear next(int n) {
        return LunarYear.from(year + n);
    }

}
