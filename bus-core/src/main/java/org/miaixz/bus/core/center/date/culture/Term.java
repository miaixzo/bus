package org.miaixz.bus.core.center.date.culture;

import org.miaixz.bus.core.xyz.MapKit;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


/**
 * 节气类型
 *
 * <pre>
 * 计算公式:[Y*D+C]-L
 * 公式解读:
 * Y值 = 年数%100
 * D值 = 0.2422
 * C值 = 世纪-值
 * L值 = 闰年数
 * </pre>
 */
public enum Term {

    /**
     * 小寒
     */
    XIAOHAN(6.11F, 5.4055F),
    /**
     * 大寒
     */
    DAHAN(20.84F, 20.12F),
    /**
     * 立春
     */
    LICHUN(4.6295F, 3.87F),
    /**
     * 雨水
     */
    YUSHUI(19.4599F, 18.73F),
    /**
     * 惊蛰
     */
    JINGZHE(6.3826F, 5.63F),
    /**
     * 春分
     */
    CHUNFEN(21.4155F, 20.646F),
    /**
     * 清明
     */
    QINGMING(5.59F, 4.81F),
    /**
     * 谷雨
     */
    GUYU(20.888F, 20.1F),
    /**
     * 立夏
     */
    LIXIA(6.318F, 5.52F),
    /**
     * 小满
     */
    XIAOMAN(21.86F, 21.04F),
    /**
     * 芒种
     */
    MANGZHONG(6.5F, 5.678F),
    /**
     * 夏至
     */
    XIAZHI(22.2F, 21.37F),
    /**
     * 小暑
     */
    XIAOSHU(7.928F, 7.108F),
    /**
     * 大暑
     */
    DASHU(23.65F, 22.83F),
    /**
     * 立秋
     */
    LIQIU(8.35F, 7.5F),
    /**
     * 处暑
     */
    CHUSHU(23.95F, 23.13F),
    /**
     * 白露
     */
    BAILU(8.44F, 7.646F),
    /**
     * 秋分
     */
    QIUFEN(23.822F, 23.042F),
    /**
     * 寒露
     */
    HANLU(9.098F, 8.318F),
    /**
     * 霜降
     */
    SHUANGJIANG(24.218F, 23.438F),
    /**
     * 立冬
     */
    LIDONG(8.218F, 7.438F),
    /**
     * 小雪
     */
    XIAOXUE(23.08F, 22.36F),
    /**
     * 大雪
     */
    DAXUE(7.9F, 7.18F),
    /**
     * 冬至
     */
    DONGZHI(22.6F, 21.94F);

    private final static Map<Integer, Integer> TERM_DATA = new HashMap<>();

    static {
        TERM_DATA.put(1902 * 100 + Term.MANGZHONG.ordinal(), 1);
        TERM_DATA.put(1911 * 100 + Term.LIXIA.ordinal(), 1);
        TERM_DATA.put(1914 * 100 + Term.LICHUN.ordinal(), -1);
        TERM_DATA.put(1914 * 100 + Term.DONGZHI.ordinal(), 1);
        TERM_DATA.put(1915 * 100 + Term.JINGZHE.ordinal(), -1);
        TERM_DATA.put(1922 * 100 + Term.DASHU.ordinal(), 1);
        TERM_DATA.put(1925 * 100 + Term.XIAOSHU.ordinal(), 1);
        TERM_DATA.put(1927 * 100 + Term.BAILU.ordinal(), 1);
        TERM_DATA.put(1928 * 100 + Term.XIAZHI.ordinal(), 1);
        TERM_DATA.put(1942 * 100 + Term.QIUFEN.ordinal(), 1);
        TERM_DATA.put(1947 * 100 + Term.LICHUN.ordinal(), -1);
        TERM_DATA.put(1947 * 100 + Term.DONGZHI.ordinal(), 1);
        TERM_DATA.put(1948 * 100 + Term.JINGZHE.ordinal(), -1);
        TERM_DATA.put(1951 * 100 + Term.DONGZHI.ordinal(), 1);
        TERM_DATA.put(1978 * 100 + Term.XIAOXUE.ordinal(), 1);
        TERM_DATA.put(1979 * 100 + Term.DAHAN.ordinal(), 1);
        TERM_DATA.put(1980 * 100 + Term.DONGZHI.ordinal(), 1);
        TERM_DATA.put(1982 * 100 + Term.XIAOHAN.ordinal(), 1);
        TERM_DATA.put(1984 * 100 + Term.DONGZHI.ordinal(), 1);
        TERM_DATA.put(2000 * 100 + Term.XIAOHAN.ordinal(), 1);
        TERM_DATA.put(2000 * 100 + Term.DAHAN.ordinal(), 1);
        TERM_DATA.put(2000 * 100 + Term.LICHUN.ordinal(), 1);
        TERM_DATA.put(2000 * 100 + Term.YUSHUI.ordinal(), 1);
        TERM_DATA.put(2002 * 100 + Term.LIQIU.ordinal(), 1);
        TERM_DATA.put(2008 * 100 + Term.XIAOMAN.ordinal(), 1);
        TERM_DATA.put(2016 * 100 + Term.XIAOSHU.ordinal(), 1);
        TERM_DATA.put(2019 * 100 + Term.XIAOHAN.ordinal(), -1);
        TERM_DATA.put(2021 * 100 + Term.DONGZHI.ordinal(), -1);
        TERM_DATA.put(2026 * 100 + Term.YUSHUI.ordinal(), -1);
        TERM_DATA.put(2082 * 100 + Term.DAHAN.ordinal(), 1);
        TERM_DATA.put(2084 * 100 + Term.CHUNFEN.ordinal(), 1);
        TERM_DATA.put(2089 * 100 + Term.SHUANGJIANG.ordinal(), 1);
        TERM_DATA.put(2089 * 100 + Term.LIDONG.ordinal(), 1);
    }

    /**
     * 世纪值
     */
    private float[] centuries;

    Term(float... centuries) {
        this.centuries = centuries;
    }

    public static void main(String[] args) {
        System.out.println(Term.LIDONG.getDate(2199));
    }

    /**
     * 获取指定年份的世纪值
     *
     * @param year
     * @return the float
     */
    public float getCentury(int year) {
        return centuries[year / 100 - 19];
    }

    /**
     * 获取指定年份的节气日期
     *
     * @param year 年份
     * @return {@link LocalDate}
     */
    public LocalDate getDate(int year) {
        // 步骤1:年数%100
        int y = year % 100;
        int month = ordinal() / 2 + 1;
        int l;
        // 步骤2:凡闰年3月1日前闰年数要减1,即:L=[(Y-1)/4],因为小寒,大寒,立春,雨水4个节气都小于3月1日
        if (((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) && month < 3) {// 闰年
            l = (y - 1) / 4;
        } else {
            l = y / 4;
        }
        double c = getCentury(year);
        // 步骤3:使用公式[Y*D+C]-L计算
        int day = (int) (y * 0.2422D + c) - l;
        // 步骤4:加上特殊的年分的节气偏移量
        System.out.println(TERM_DATA);
        day += MapKit.getInt(TERM_DATA, year * 100 + ordinal(), 0);
        return LocalDate.of(year, month, day);
    }

}
